import torch
import time

def test_gpu():
    print("===== GPU 測試開始 =====")
    
    # 檢查 CUDA 是否可用
    if torch.cuda.is_available():
        device = torch.device('cuda')
        print(f"CUDA 可用，使用的 GPU: {torch.cuda.get_device_name(0)}")
    else:
        device = torch.device('cpu')
        print("警告: CUDA 不可用，將使用 CPU")
    
    # 測試 GPU 運算
    try:
        # 測試隨機張量運算
        print("\n測試張量運算...")
        tensor_size = (1000, 1000)
        test_tensor = torch.randn(tensor_size).to(device)
        start_time = time.time()
        result = test_tensor @ test_tensor  # 矩陣乘法
        end_time = time.time()
        print(f"矩陣大小: {tensor_size}")
        print(f"運算時間: {end_time - start_time:.6f} 秒")
        print("運算成功！")
    except Exception as e:
        print(f"運算失敗: {e}") 

    # 測試 GPU 記憶體使用情況
    if device.type == 'cuda':
        memory_allocated = torch.cuda.memory_allocated() / 1024**2
        memory_reserved = torch.cuda.memory_reserved() / 1024**2
        print(f"\nGPU 記憶體使用狀況:")
        print(f"已分配記憶體: {memory_allocated:.2f} MB")
        print(f"已保留記憶體: {memory_reserved:.2f} MB")
        
        # 釋放未使用的記憶體
        torch.cuda.empty_cache()
        print("清理後記憶體使用已更新")
    
    print("===== GPU 測試結束 =====")

if __name__ == "__main__":
    test_gpu()
