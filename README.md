# novGPU 🎮

> Real-time Android system monitor with neon/gaming UI — 2026 style

![Platform](https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-purple?style=flat-square&logo=kotlin)
![Min SDK](https://img.shields.io/badge/minSDK-26-blue?style=flat-square)
![Architecture](https://img.shields.io/badge/Architecture-MVI-cyan?style=flat-square)
![Build](https://github.com/XenorKing/Nov-GPU/actions/workflows/build.yml/badge.svg)

---

## Features

| Module | Description |
|--------|-------------|
| **CPU** | Real-time usage, per-core breakdown, frequency, architecture |
| **GPU** | Usage via sysfs (Adreno/Mali/PowerVR), renderer info |
| **RAM** | Used/Free/Total + Swap monitoring |
| **Temperature** | Battery, CPU, all thermal zones |
| **Network** | Download/Upload speed, history, connection type |

## Design

- **Neon gaming aesthetic** — dark background + glowing cyan/purple/green accents
- **Custom Canvas charts** — smooth animated line graphs with glow effects
- **Neon arc gauges** — animated circular gauges with needle-dot
- **Monospace typography** — terminal/HUD feel
- **MVI architecture** — predictable state, real-time Flow updates

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVI + Hilt DI |
| Async | Kotlin Coroutines + Flow |
| Data | /proc filesystem + Android API |
| Build | Gradle KTS + Version Catalog |
| CI/CD | GitHub Actions |

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## GitHub Actions

The CI/CD pipeline automatically:
1. **Build** — compiles debug APK on every push
2. **Lint** — runs Android lint checks
3. **Release** — builds + signs release APK when you create a GitHub Release
4. **Artifacts** — uploads APK as downloadable build artifact

## Requirements

- Android 8.0+ (API 26)
- No root required
- Permissions: `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_PHONE_STATE`

## Screens

| Dashboard | CPU | GPU/RAM | Network/Temp |
|-----------|-----|---------|--------------|
| Overview of all metrics | Detailed CPU with per-core | GPU renderer + RAM breakdown | Speed graphs + thermal zones |

---

Made with 💜 by XenorKing
