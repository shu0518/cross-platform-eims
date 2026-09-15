# Cross-Platform Window/Door Contractor Management System

> A window/door installation business's contractor-management system: a Vue 3 admin dashboard and a native Android client both talk to one Express/MySQL API, and the Android client also runs on-device YOLOv11-seg window detection via PyTorch Mobile.

`Capstone project` · NCKU
**Stack:** Vue 3 · Express · MySQL · Android (Java, PyTorch Mobile) · YOLOv11-seg (TorchScript/ONNX/TFLite)

## Overview

The system covers a window/door contractor's daily workflow: managing customers and contractors, recording window/door measurements, generating quotations and processing/material-request documents (from the business's own Excel templates, via `exceljs`/`docx`), and emailing signup/password-reset verification codes (`nodemailer`). Both the Vue web dashboard and the Android app call the same 36-route Express API backed by MySQL. On the Android side, `WindowDetectionActivity` additionally runs a YOLOv11-seg model on-device (via PyTorch Mobile) to detect and segment windows from the camera feed, so window measurement can be assisted by live object detection instead of manual entry only. `vision_model/` holds the training-side scripts (Labelme-to-YOLO conversion, a format-agnostic inference wrapper for PyTorch/ONNX/TensorFlow/TFLite) used to produce that model.

## Model Weights & Training Data (not included in this repo)

`vision_model/` only keeps the scripts, not their outputs. Not included:

- Model weights: `model_1119.torchscript.pt`, `model_v11.pt`, `best_float32.tflite`
- Training data: the YOLO-format dataset (`yolo_dataset/`, train/val/test splits), 215 raw labeled images, 150 inference-output images

Together these were 300+ MB and are exactly what `vision_model/`'s scripts (`LabelmeToYOLO.py`, `runPytorchMobile.py`, `runpt.py`) exist to regenerate — they're reproducible from a Labelme-annotated image set, not one-off assets, so keeping them out of git was a deliberate size/reproducibility trade-off rather than an oversight. To run detection locally: label your own images with Labelme, run `LabelmeToYOLO.py` to convert to YOLO format, train a YOLOv11-seg model, export to TorchScript, and place the export at `android_client_app/app/src/main/assets/model_1119.torchscript.pt` (see [`android_client_app/README.md`](android_client_app/README.md)). Without the model file, every screen except `WindowDetectionActivity`/`TestCameraActivity` works normally.

## Key Design Decisions

| Decision | Rationale |
| --- | --- |
| Documents generated from the business's real Excel templates (`db/src/template/*.xlsx`) via `exceljs`/`docx`, not hand-rolled layout code | Output matches the paperwork the business already uses (quotation/processing/material-request forms) instead of reinventing the format |
| Vision inference wrapped behind a single class (`runpt.py: YOLOSegInference`) that dispatches on `model_format` (pytorch/onnx/tensorflow/tflite) | One inference call site regardless of which export format is deployed, so swapping the Android TFLite model for a server-side PyTorch model doesn't change calling code |
| Android build restricted to `arm64-v8a` only (`ndk { abiFilters 'arm64-v8a' }`) | PyTorch Mobile's native libraries are large per-ABI; a single-ABI build keeps the APK size down at the cost of not running on x86/other-ABI devices |
| Both Vue dashboard and Android app call the same Express API rather than each having their own backend | One MySQL-backed source of truth for customers/contractors/measurements/orders across both clients |

## Limitations

- `npm start` in `web_admin_dashboard/db` is broken: `package.json`'s `start` script runs `node server.js`, but that file doesn't exist — the actual entry point with `app.listen()` is `app.js`.
- `app.js` calls `require("body-parser")`, but `body-parser` is not listed in `package.json`'s dependencies — a clean `npm install` will not install it, and the server will fail to start until it's added manually.
- `jsonwebtoken` is a listed backend dependency but is never imported anywhere in `db/src` — despite being present in `package.json`, authentication is not actually implemented as token-based auth in the current routes/controller.
- The backend API base URL (`http://163.17.135.120`) is hardcoded directly inside 17 separate Android `Activity` files rather than centralized in one config/constants file, so pointing the app at a different server means editing all 17.
- Android build only targets `arm64-v8a` (see Key Design Decisions) — it will not install on an x86/x86_64 emulator or a device with a different ABI.
- No automated tests: the backend's `package.json` test script is a stub (`echo "Error: no test specified" && exit 1`), and there's no test setup in the Vue dashboard or the Android app.

## Running It

```bash
# Web admin dashboard (Vue 3)
cd web_admin_dashboard
npm install
npm run serve

# Backend API (Express + MySQL)
cd web_admin_dashboard/db
npm install
npm install body-parser        # required by app.js but missing from package.json
cp .env.example .env           # fill in DB_HOST / DB_USER / DB_PASSWORD / EMAIL_USER / EMAIL_PASSWORD
node app.js                    # `npm start` is broken, see Limitations — run app.js directly
```

```text
# Android client
1. Open android_client_app/ in Android Studio, sync Gradle.
2. Place a trained model at app/src/main/assets/model_1119.torchscript.pt (optional, see above).
3. Run on an arm64-v8a device or emulator.
```

## Structure

    android_client_app/       Java + PyTorch Mobile client: customers, contractors, quotations, on-device detection
    vision_model/              YOLOv11-seg training/export scripts (weights and datasets not included, see above)
    web_admin_dashboard/       Vue 3 admin frontend
    web_admin_dashboard/db/    Express + MySQL backend API (36 routes: user/contract/client/measure/machine/download)
    demo_assets/               Screenshots + app_demo.gif
