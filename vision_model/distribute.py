import os
import shutil
from sklearn.model_selection import train_test_split

# 設定資料夾路徑
images_path = 'data/images'
labels_path = 'data/labels'
masks_path = 'data/masks'

# 輸出資料夾路徑
output_base_path = 'maskrcnn_dataset'
train_images_path = os.path.join(output_base_path, 'train/images')
train_masks_path = os.path.join(output_base_path, 'train/masks')
val_images_path = os.path.join(output_base_path, 'val/images')
val_masks_path = os.path.join(output_base_path, 'val/masks')
test_images_path = os.path.join(output_base_path, 'test/images')
test_masks_path = os.path.join(output_base_path, 'test/masks')

# 確保輸出資料夾存在
os.makedirs(train_images_path, exist_ok=True)
os.makedirs(train_masks_path, exist_ok=True)
os.makedirs(val_images_path, exist_ok=True)
os.makedirs(val_masks_path, exist_ok=True)
os.makedirs(test_images_path, exist_ok=True)
os.makedirs(test_masks_path, exist_ok=True)

# 取得所有圖片檔名
image_files = [f for f in os.listdir(images_path) if f.endswith('.jpg')]

# 使用 sklearn 的 train_test_split 來分割數據
train_files, temp_files = train_test_split(image_files, test_size=0.3, random_state=42)
val_files, test_files = train_test_split(temp_files, test_size=0.5, random_state=42)

# 定義一個函數來複製檔案
def copy_files(file_list, dest_images_path, dest_masks_path):
    for file_name in file_list:
        base_name = os.path.splitext(file_name)[0]
        mask_file_name = f"{base_name}_mask.png"

        # 複製圖片和遮罩
        shutil.copy(os.path.join(images_path, file_name), os.path.join(dest_images_path, file_name))
        shutil.copy(os.path.join(masks_path, mask_file_name), os.path.join(dest_masks_path, mask_file_name))

# 複製檔案到各個資料夾
copy_files(train_files, train_images_path, train_masks_path)
copy_files(val_files, val_images_path, val_masks_path)
copy_files(test_files, test_images_path, test_masks_path)

print("資料分配完成！")
