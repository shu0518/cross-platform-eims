import torch
import numpy as np
from PIL import Image
import cv2
from torchvision import transforms
import time
import os
from tqdm import tqdm

class WindowSegmenter:
    def __init__(self, model_path):
        # 確保 CUDA 可用
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        
        if self.device.type == 'cuda':
            print(f"使用 GPU: {torch.cuda.get_device_name(0)}")
            print(f"GPU 記憶體使用: {torch.cuda.memory_allocated()/1024**2:.1f}MB")
        else:
            print("警告: 未檢測到 GPU，使用 CPU 進行推論")
        
        # 載入模型並移至 GPU
        self.model = torch.jit.load(model_path).to(self.device)
        self.model.eval()
        
        # 再次檢查 GPU 記憶體
        if self.device.type == 'cuda':
            print(f"模型載入後 GPU 記憶體使用: {torch.cuda.memory_allocated()/1024**2:.1f}MB")
        
        self.transform = transforms.Compose([
            transforms.Resize((640, 640)),
            transforms.ToTensor(),
        ])

    def process_mask(self, proto, mask_coeffs):
        # 確保在 GPU 上進行計算
        mask = (mask_coeffs @ proto.reshape(32, -1)).sigmoid()
        mask = mask.reshape(160, 160)
        # 移回 CPU 進行 OpenCV 操作
        mask = cv2.resize(mask.cpu().numpy(), self.original_size, interpolation=cv2.INTER_LINEAR)
        mask = mask > 0.5
        
        kernel = np.ones((3,3), np.uint8)
        mask = cv2.morphologyEx(mask.astype(np.uint8), cv2.MORPH_CLOSE, kernel)
        return mask > 0
    
    def process_predictions(self, predictions, proto, conf_thres=0.5):
        # 在 GPU 上處理預測結果
        predictions = predictions.squeeze(0).t()  # [8400, 37]
        proto = proto.squeeze(0)  # [32, 160, 160]
        
        boxes = predictions[:, :4]
        scores = predictions[:, 4]
        mask_coeffs = predictions[:, 5:37]
        
        # 在 GPU 上進行過濾
        mask = scores > conf_thres
        boxes = boxes[mask]
        scores = scores[mask]
        mask_coeffs = mask_coeffs[mask]
        
        detections = []
        if len(boxes) > 0:
            # 在 GPU 上進行座標轉換
            boxes = self.xywh2xyxy(boxes)
            boxes[:, [0, 2]] *= self.original_size[0] / 640
            boxes[:, [1, 3]] *= self.original_size[1] / 640
            
            # 移至 CPU 進行後續處理
            boxes = boxes.cpu()
            scores = scores.cpu()
            mask_coeffs = mask_coeffs.cpu()
            proto = proto.cpu()
            
            for box, score, mask_coeff in zip(boxes, scores, mask_coeffs):
                mask = self.process_mask(proto, mask_coeff)
                x1, y1, x2, y2 = map(int, box.tolist())
                mask_bbox = np.zeros_like(mask)
                mask_bbox[max(0, y1):min(mask.shape[0], y2), 
                         max(0, x1):min(mask.shape[1], x2)] = 1
                mask = mask & mask_bbox
                
                detections.append({
                    'bbox': box.tolist(),
                    'score': score.item(),
                    'mask': mask
                })
        
        return self.non_max_suppression(detections)
    
    def segment(self, image_path):
        # 載入和預處理圖像
        image = Image.open(image_path).convert('RGB')
        self.original_size = image.size
        original_image = np.array(image)
        
        # 將輸入移至 GPU
        input_tensor = self.transform(image).unsqueeze(0).to(self.device)
        
        # 在 GPU 上進行推理
        with torch.no_grad():
            output = self.model(input_tensor)
        
        # 處理預測結果
        predictions, proto = output
        results = self.process_predictions(predictions, proto)
        
        # 視覺化結果
        return self.visualize_results(original_image, results)

    # ... [其他方法保持不變] ...
    def non_max_suppression(self, detections, iou_threshold=0.8):
        if not detections:
            return []
        
        detections = sorted(detections, key=lambda x: x['score'], reverse=True)
        keep = []
        indices_to_remove = set()
        
        for i, current in enumerate(detections):
            if i in indices_to_remove:
                continue
                
            keep.append(current)
            current_box = current['bbox']
            
            for j, other in enumerate(detections[i+1:], i+1):
                if j in indices_to_remove:
                    continue
                    
                other_box = other['bbox']
                
                x1 = max(current_box[0], other_box[0])
                y1 = max(current_box[1], other_box[1])
                x2 = min(current_box[2], other_box[2])
                y2 = min(current_box[3], other_box[3])
                
                intersection = max(0, x2 - x1) * max(0, y2 - y1)
                area1 = (current_box[2] - current_box[0]) * (current_box[3] - current_box[1])
                area2 = (other_box[2] - other_box[0]) * (other_box[3] - other_box[1])
                union = area1 + area2 - intersection
                
                if intersection / union > iou_threshold:
                    indices_to_remove.add(j)
        
        return keep
    
    def visualize_results(self, image, results):
        mask_overlay = np.zeros_like(image)
        combined_mask = np.zeros((image.shape[0], image.shape[1]), dtype=bool)
        
        for result in results:
            combined_mask |= result['mask']
        
        mask_overlay[combined_mask] = [0, 0, 255]
        result_image = cv2.addWeighted(image, 1, mask_overlay, 0.4, 0)
        
        return Image.fromarray(result_image)
    
    def xywh2xyxy(self, x):
        y = x.clone()
        y[:, 0] = x[:, 0] - x[:, 2] / 2
        y[:, 1] = x[:, 1] - x[:, 3] / 2
        y[:, 2] = x[:, 0] + x[:, 2] / 2
        y[:, 3] = x[:, 1] + x[:, 3] / 2
        return y
    
    def segment(self, image_path):
        # 載入和預處理圖像
        image = Image.open(image_path).convert('RGB')
        self.original_size = image.size
        original_image = np.array(image)
        
        # 轉換輸入
        input_tensor = self.transform(image)
        input_tensor = input_tensor.unsqueeze(0).to(self.device)
        
        # 執行推理
        with torch.no_grad():
            output = self.model(input_tensor)
        
        # 處理預測結果
        predictions, proto = output
        results = self.process_predictions(predictions, proto)
        
        # 視覺化結果
        return self.visualize_results(original_image, results)
    
def process_directory(input_dir, output_dir, model_path):
    """批次處理資料夾中的所有圖片"""
    # 確保輸出資料夾存在
    os.makedirs(output_dir, exist_ok=True)
    
    # 初始化分割器
    segmenter = WindowSegmenter(model_path)
    
    # 獲取所有圖片檔案
    image_files = [f for f in os.listdir(input_dir) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]
    total_files = len(image_files)
    print(f"找到 {total_files} 個圖片檔案")
    
    # 使用tqdm顯示進度條
    start_time = time.time()
    for i, filename in enumerate(tqdm(image_files, desc="處理圖片")):
        input_path = os.path.join(input_dir, filename)
        output_path = os.path.join(output_dir, f"{i+1:06d}.jpg")
        
        try:
            # 處理圖片
            result_image = segmenter.segment(input_path)
            # 儲存結果
            result_image.save(output_path)
            
            # 定期清理 GPU 記憶體
            if torch.cuda.is_available():
                if (i + 1) % 20 == 0:  # 每處理50張圖片
                    torch.cuda.empty_cache()
        except Exception as e:
            print(f"\n處理 {filename} 時發生錯誤: {str(e)}")
            continue
    
    # 計算並顯示處理效能
    total_time = time.time() - start_time
    avg_time = total_time / total_files
    print(f"\n處理完成！")
    print(f"總處理時間: {total_time:.2f} 秒")
    print(f"平均每張圖片處理時間: {avg_time:.3f} 秒")
    if torch.cuda.is_available():
        print(f"最終 GPU 記憶體使用: {torch.cuda.memory_allocated()/1024**2:.1f}MB")

def main():
    # 設定路徑
    model_path = r"C:/Users/LYS81/window_detection/model_1119.torchscript.pt"
    input_dir = r"C:/Users/LYS81/Downloads/2la/2la"
    output_dir = r"C:/Users/LYS81/window_detection/output"
    
    try:
        # 開始處理
        process_directory(input_dir, output_dir, model_path)
        
    except Exception as e:
        print(f"\n錯誤: {str(e)}")
        import traceback
        traceback.print_exc()
    finally:
        # 清理 GPU 記憶體
        if torch.cuda.is_available():
            torch.cuda.empty_cache()

if __name__ == "__main__":
    main()