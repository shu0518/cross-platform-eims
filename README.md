# Cross-Platform EIMS 

> 一個整合 AI 視覺邊緣運算與跨平台管理後台的企業資訊管理系統。

![Version](https://img.shields.io/badge/version-v1.0.0-blue.svg)
![Last Updated](https://img.shields.io/badge/Last_Updated-2026.03.11-brightgreen)
![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Vue](https://img.shields.io/badge/Frontend-Vue.js-4FC08D?logo=vuedotjs&logoColor=white)

## Key Features

* **跨平台客戶端部署**：原生 Android 客戶端，針對行動裝置優化。
* **邊緣 AI 視覺辨識**：整合 PyTorch Mobile，支援設備端 YOLO 模型即時影像推理，不須依賴雲端運算即可完成辨識任務。
* **現代化管理後台**：基於 Vue 3 與 Vuex 構建的 Web 控制中心，提供流暢的數據可視化與資訊管理。
* **端到端流程整合**：涵蓋從模型訓練轉換（`LabelmeToYOLO`、`runPytorchMobile`）到終端部署的完整解決方案。

## Tech Stack

**Client App (Android)**
* Java / Kotlin
* PyTorch Android (`1.13.1`)

**Admin Dashboard (Web)**
* Vue 3
* Vuex 4 / Vue Router
* Axios

**Vision Model (AI)**
* PyTorch / TorchScript
* YOLO

## Quick Start

### 1. Web Admin Dashboard (後台管理系統)

```bash
# 進入目錄
cd Cross_Platform_EIMS/web_admin_dashboard

# 安裝依賴套件
npm install

# 啟動開發伺服器 (包含熱重載)
npm run serve

# 生產環境編譯打包
npm run build
```

### 2. Android Client App (行動端)

1. 使用 Android Studio 開啟 `android_client_app` 目錄。
2. 同步 Gradle (`Sync Project with Gradle Files`)。
3. 模型權重未包含在此 repo，需自行放置，詳見 [`android_client_app/README.md`](android_client_app/README.md)。
4. 點擊 **Run** 部署至 Android 實體機或模擬器。

### 3. Web Admin Dashboard 的後端 DB 設定

`web_admin_dashboard/db/` 是 Express 後端，需要資料庫連線資訊：

```bash
cd web_admin_dashboard/db
cp .env.example .env
# 填入 DB_HOST / DB_USER / DB_PASSWORD / EMAIL_USER / EMAIL_PASSWORD 等實際值
```

> 這份原本的 `.env`（含真實資料庫密碼與 Gmail 應用程式密碼）曾經不小心被 commit 進舊版 repo，已從這裡移除。**該組密碼建議盡快更換/撤銷**（跟這次重建 repo 無關，是獨立要處理的事）。

## Project Structure

```text
cross-platform-eims/
├── android_client_app/    # Android 客戶端 (整合 PyTorch Mobile)
│   ├── app/               # 應用程式主程式碼與 UI 邏輯
│   ├── README.md          # 模型權重放置說明
│   └── build.gradle       # Gradle 構建配置
├── vision_model/          # 電腦視覺模型訓練/轉換腳本（不含權重與訓練圖片，見下）
│   ├── LabelmeToYOLO.py   # 資料集格式轉換工具
│   ├── runPytorchMobile.py# 模型轉換為 TorchScript 格式的腳本
│   └── distribute.py, runpt.py, testGPU.py
├── web_admin_dashboard/   # Web 企業管理後台
│   ├── src/               # Vue 元件、Vuex 狀態管理與路由
│   ├── db/                # Express 後端（含 .env.example）
│   ├── package.json       # 專案相依套件 (Vue 3, Axios)
│   └── vue.config.js      # Vue CLI 設置
└── demo_assets/           # 展示截圖 + app_demo.gif（原始 mp4 未包含）
```

## 模型權重與訓練資料（未包含在此 repo）

`vision_model/` 只保留訓練/轉換用的腳本，以下內容**不包含在版本控制中**（本地開發時自行準備）：

* 模型權重：`model_1119.torchscript.pt`、`model_v11.pt`、`best_float32.tflite`
* 訓練資料：`yolo_dataset/`（YOLO 格式訓練集）、`images/`（215 張原始標註圖片）、`output/`（150 張推論輸出圖）
* Android 端同款的 `model_1119.torchscript.pt`（原本在 `android_client_app` 的 assets 裡也放了一份重複的）

原因：這些檔案合計超過 300MB，且不需要進版控就能重現——`vision_model/` 裡的腳本本身就是用來重新產生它們的工具鏈。如果需要這些檔案，請聯絡我另外取得。

## Environment Variables

若要在本地環境正確運行 Web 管理後台，請在 `web_admin_dashboard` 根目錄下建立 `.env` 檔案，並配置以下變數（範例）：

```env
# API 伺服器基礎路徑
VUE_APP_BASE_API=http://localhost:8080/api/v1

# 其他環境配置
NODE_ENV=development
```
*(註：目前系統未直接包含 `.env` 文件，建議依據實際 API 部署端點自行配置。)*

## 🔌 API Reference (預期接口)

| Method | Endpoint              | Description                    | Auth Required |
| :---   | :---                  | :---                           | :---          |
| `POST` | `/api/v1/auth/login`  | 管理員登入並取得 Token         | No            |
| `GET`  | `/api/v1/dashboard`   | 獲取系統概覽與辨識統計數據     | Yes           |
| `POST` | `/api/v1/records`     | 接收來自 Android 端的辨識結果  | Yes           |
| `GET`  | `/api/v1/records`     | 查詢歷史辨識與管理紀錄         | Yes           |

