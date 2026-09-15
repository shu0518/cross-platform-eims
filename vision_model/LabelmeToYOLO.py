import json
import os
from pathlib import Path
from glob import glob
from tqdm import tqdm
import shutil

class LabelmeToYOLOConverter:
    def __init__(self):
        # 定義類別映射
        self.class_map = {
            'window': 0
        }
        # 支援的圖片格式
        self.image_extensions = ('.jpg', '.jpeg', '.png', '.bmp')
        
    def find_image_file(self, json_file, json_dir):
        """
        查找與JSON檔案對應的圖片
        
        Args:
            json_file: JSON檔案的完整路徑
            json_dir: JSON檔案所在的目錄
            
        Returns:
            找到的圖片檔案路徑或None
        """
        try:
            # 從JSON讀取圖片檔名
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            image_filename = data['imagePath']
            
            # 檢查直接使用JSON中的檔名
            direct_path = os.path.join(json_dir, image_filename)
            if os.path.exists(direct_path):
                return direct_path
                
            # 檢查不同的圖片格式
            basename = Path(image_filename).stem
            for ext in self.image_extensions:
                image_path = os.path.join(json_dir, basename + ext)
                if os.path.exists(image_path):
                    return image_path
                    
            return None
        except Exception as e:
            print(f"查找圖片時發生錯誤 {json_file}: {str(e)}")
            return None

    def convert_single_file(self, json_file, json_dir, output_dir, index):
        
        try:
            # 查找對應的圖片檔案
            image_file = self.find_image_file(json_file, json_dir)
            if not image_file:
                print(f"找不到對應的圖片檔案: {json_file}")
                return False, None, None

            # 讀取JSON檔案
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            # 獲取圖片尺寸
            img_height = data['imageHeight']
            img_width = data['imageWidth']
            
            # 創建輸出目錄
            images_dir = os.path.join(output_dir, 'images')
            labels_dir = os.path.join(output_dir, 'labels')
            os.makedirs(images_dir, exist_ok=True)
            os.makedirs(labels_dir, exist_ok=True)
            
            # 生成新的檔名
            new_basename = f"image{index:06d}"
            new_image_path = os.path.join(images_dir, f"{new_basename}{Path(image_file).suffix}")
            new_label_path = os.path.join(labels_dir, f"{new_basename}.txt")
            
            # 複製並重命名圖片
            shutil.copy2(image_file, new_image_path)
            
            # 寫入YOLO格式的標籤檔
            with open(new_label_path, 'w') as f:
                for shape in data['shapes']:
                    label = shape['label']
                    points = shape['points']
                    
                    # 轉換標籤為類別ID
                    class_id = self.class_map.get(label, -1)
                    if class_id == -1:
                        continue
                    
                    # 標準化座標
                    normalized_points = []
                    for x, y in points:
                        normalized_x = x / img_width
                        normalized_y = y / img_height
                        normalized_points.extend([normalized_x, normalized_y])
                    
                    # 格式化輸出
                    line = f"{class_id}"
                    for coord in normalized_points:
                        line += f" {coord:.6f}"
                    f.write(line + '\n')
            
            return True, new_image_path, new_label_path
            
        except Exception as e:
            print(f"處理檔案 {json_file} 時發生錯誤: {str(e)}")
            return False, None, None

    def batch_convert(self, input_dir, output_dir):
        """
        批次轉換目錄中的所有JSON檔案
        
        Args:
            input_dir: 輸入目錄路徑
            output_dir: 輸出目錄路徑
        """
        # 獲取所有JSON檔案
        json_files = glob(os.path.join(input_dir, "*.json"))
        
        if not json_files:
            print(f"在 {input_dir} 中沒有找到JSON檔案")
            return
        
        # 創建輸出目錄
        os.makedirs(output_dir, exist_ok=True)
        
        # 轉換計數器
        success_count = 0
        failed_files = []
        converted_files = []
        
        print(f"開始轉換 {len(json_files)} 個檔案...")
        for index, json_file in enumerate(tqdm(json_files, desc="轉換進度")):
            success, new_image, new_label = self.convert_single_file(
                json_file, input_dir, output_dir, index + 1
            )
            
            if success:
                success_count += 1
                converted_files.append({
                    'original_json': json_file,
                    'new_image': new_image,
                    'new_label': new_label
                })
            else:
                failed_files.append(json_file)
        
        # 輸出處理結果
        print(f"\n轉換完成:")
        print(f"- 成功轉換: {success_count} 個檔案")
        print(f"- 失敗: {len(failed_files)} 個檔案")
        
        if failed_files:
            print("\n失敗的檔案:")
            for file in failed_files:
                print(f"- {file}")
                
        print("\n成功轉換的檔案:")
        for file_info in converted_files:
            print(f"\n原始JSON: {file_info['original_json']}")
            print(f"新圖片: {file_info['new_image']}")
            print(f"新標籤: {file_info['new_label']}")

def main():
    
    # 設定輸入和輸出目錄
    input_dir = "C:/Users/LYS81/Downloads/origin_images-20241114T155943Z-001/origin_images"    # 修改為你的資料集目錄
    os.chdir(os.path.dirname(__file__))
    output_dir = "yolo_dataset"  # 修改為你想要的輸出目錄
    
    # 創建轉換器實例並執行批次轉換
    converter = LabelmeToYOLOConverter()
    converter.batch_convert(input_dir, output_dir)

if __name__ == "__main__":
    main()