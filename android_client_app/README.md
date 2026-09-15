# Android Client App

Native Android client (Java) for window/door contractor management: customers, contractors, quotations, order forms, measured-data entry, and window-frame calculators (2/3/4-leaf layouts), plus on-device object detection for window segmentation via the YOLOv11 model trained in [`../vision_model`](../vision_model).

## Requirements

- Android Studio, `compileSdk 34`, `minSdk 24`, `targetSdk 34`
- Build only targets `arm64-v8a` (see `abiFilters` in `app/build.gradle`)
- Runtime dependency: `org.pytorch:pytorch_android:1.13.1` + `pytorch_android_torchvision` (see `app/build.gradle`)

## Model weights (not included in this repo)

`WindowDetectionActivity` loads a TorchScript model from the app's assets at runtime:

```java
private static final String MODEL_NAME = "model_1119.torchscript.pt";
```

This file is **not committed** (see the repo root README for why). To run detection locally:

1. Train or obtain the YOLOv11 TorchScript export from [`../vision_model`](../vision_model).
2. Place it at `app/src/main/assets/model_1119.torchscript.pt`.
3. Rebuild the app.

Without the model file, every screen except `WindowDetectionActivity`/`TestCameraActivity` works normally.

## Structure

    app/src/main/java/com/example/a0731/   Activities, adapters, calculators
    app/src/main/assets/models/             Reference diagrams for 2/3/4-leaf window layouts (included)
    app/src/main/res/                       Layouts, drawables, strings
