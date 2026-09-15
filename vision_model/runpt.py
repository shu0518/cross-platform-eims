from typing import Union, Dict
import numpy as np
import cv2
from pathlib import Path

class YOLOSegInference:
    def __init__(self, model_path: Union[str, Path], model_format: str):
        self.model_path = Path(model_path)
        self.model_format = model_format.lower()
        self.model = self._load_model()
        
    def _load_model(self):
        if self.model_format == 'pytorch':
            import torch
            from ultralytics import YOLO  # 使用 ultralytics 的 YOLO 類
            model = YOLO(str(self.model_path))
            return model
            
        elif self.model_format == 'onnx':
            import onnxruntime
            return onnxruntime.InferenceSession(str(self.model_path))
            
        elif self.model_format == 'tensorflow':
            import tensorflow as tf
            return tf.saved_model.load(str(self.model_path))
            
        elif self.model_format == 'tflite':
            import tensorflow as tf
            interpreter = tf.lite.Interpreter(model_path=str(self.model_path))
            interpreter.allocate_tensors()
            return interpreter
        
        else:
            raise ValueError(f"Unsupported model format: {self.model_format}")

    def process_image(self, image_path: Union[str, Path], 
                     confidence_threshold: float = 0.5,
                     alpha: float = 0.5) -> np.ndarray:
        
        # 讀取原始圖片
        original_image = cv2.imread(str(image_path))
        if original_image is None:
            raise ValueError(f"Cannot read image at {image_path}")
        
        if self.model_format == 'pytorch':
            # 使用 YOLO 模型直接預測
            results = self.model.predict(
                source=image_path,
                conf=confidence_threshold,
                save=False,
                show=False
            )
            
            # 獲取第一張圖片的結果
            result = results[0]
            
            # 如果有分割遮罩
            output_image = original_image.copy()
            if hasattr(result, 'masks') and result.masks is not None:
                masks = result.masks.data.cpu().numpy()
                # 處理每個遮罩
                for mask in masks:
                    # 調整遮罩尺寸至原始圖片大小
                    mask = cv2.resize(mask, (original_image.shape[1], original_image.shape[0]))
                    
                    # 創建彩色遮罩
                    colored_mask = np.zeros_like(original_image)
                    colored_mask[mask > confidence_threshold] = [0, 255, 0]  # 綠色遮罩
                    
                    # 將遮罩疊加到圖片上
                    output_image = cv2.addWeighted(output_image, 1, colored_mask, alpha, 0)
            
        elif self.model_format == 'onnx':
            # ONNX 模型處理
            input_image = cv2.resize(original_image, (640, 640))
            input_image = input_image.astype(np.float32) / 255.0
            input_image = input_image.transpose(2, 0, 1)[np.newaxis, ...]
            
            # 獲取輸入輸出名稱
            input_name = self.model.get_inputs()[0].name
            results = self.model.run(None, {input_name: input_image})
            
            # 處理結果
            output_image = self._process_masks(original_image, results[0], confidence_threshold, alpha)
            
        elif self.model_format == 'tensorflow':
            # TensorFlow SavedModel 處理
            input_image = cv2.resize(original_image, (640, 640))
            input_image = input_image.astype(np.float32) / 255.0
            input_image = input_image[np.newaxis, ...]
            
            results = self.model(input_image)
            if isinstance(results, dict) and 'output0' in results:
                masks = results['output0'].numpy()
            else:
                masks = results[0].numpy()
            
            output_image = self._process_masks(original_image, masks, confidence_threshold, alpha)
            
        elif self.model_format == 'tflite':
            # TFLite 模型處理
            input_details = self.model.get_input_details()
            output_details = self.model.get_output_details()
            
            input_image = cv2.resize(original_image, (640, 640))
            input_image = input_image.astype(np.float32) / 255.0
            input_image = input_image[np.newaxis, ...]
            
            self.model.set_tensor(input_details[0]['index'], input_image)
            self.model.invoke()
            masks = self.model.get_tensor(output_details[0]['index'])
            
            output_image = self._process_masks(original_image, masks, confidence_threshold, alpha)
            
        return output_image
    
    def _process_masks(self, original_image: np.ndarray, masks: np.ndarray,
                      confidence_threshold: float, alpha: float) -> np.ndarray:
        
        output_image = original_image.copy()
        height, width = original_image.shape[:2]
        
        # 確保 masks 是三維數組 [num_masks, height, width]
        if len(masks.shape) == 4:  # [batch, num_masks, height, width]
            masks = masks[0]
        
        # 處理每個遮罩
        for mask in masks:
            mask = cv2.resize(mask, (width, height))
            colored_mask = np.zeros_like(original_image)
            colored_mask[mask > confidence_threshold] = [0, 255, 0]
            output_image = cv2.addWeighted(output_image, 1, colored_mask, alpha, 0)
            
        return output_image

def main():
    # 各種格式的模型使用示例
    models = {
        'pytorch': ('C:/Users/LYS81/window_detection/model_v11.pt', 'pytorch'),
        'onnx': ('C:/Users/LYS81/window_detection/model_v11.onnx', 'onnx'),
        'tensorflow': ('C:/Users/LYS81/window_detection/model_v11_saved_model', 'tensorflow'),
        'tflite': ('C:/Users/LYS81/window_detection/model_v11.tflite', 'tflite')
    }
    
    image_path = "C:/Users/LYS81/window_detection/yolo_dataset/images/train/image000141.jpg"
    
    for format_name, (model_path, format_type) in models.items():
        try:
            # 初始化推論器
            inferencer = YOLOSegInference(model_path, format_type)
            
            # 處理圖片
            result = inferencer.process_image(
                image_path,
                confidence_threshold=0.5,
                alpha=0.5
            )
            
            # 保存結果
            output_path = f"result_{format_name}.jpg"
            cv2.imwrite(output_path, result)
            print(f"Processed {format_name} model, saved result to {output_path}")
            
        except Exception as e:
            print(f"Error processing {format_name} model: {str(e)}")

if __name__ == "__main__":
    main()