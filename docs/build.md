# Сборка APK

## Требования к окружению

| Инструмент | Версия |
|------------|--------|
| Android Studio | Hedgehog 2023.1.1+ / Iguana / Koala |
| JDK | 17 |
| Android SDK | compileSdk 34, build-tools 34.x |
| Gradle | 8.2 (wrapper) |
| Устройство / эмулятор | API 26+, ОЗУ желательно ≥ 3 ГБ |

## Открытие проекта

1. File → Open → папка `FINANCIAL_GAME`.
2. Дождаться Gradle Sync.
3. При запросе `local.properties` Studio создаст путь к SDK сама.

## Debug-сборка

```bash
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`  
applicationId: `ru.finny.pet.debug`

На Windows:

```bat
gradlew.bat :app:assembleDebug
```

Если wrapper-jar отсутствует, откройте проект в Android Studio — IDE подтянет wrapper — либо выполните `gradle wrapper` при установленном Gradle 8.2.

## Release (подписанный)

1. Создайте keystore **вне репозитория** (не коммитить!):

```bash
keytool -genkeypair -v -keystore finny-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias finny
```

2. Добавьте в `app/build.gradle.kts` `signingConfigs` (или используйте UI Studio: Build → Generate Signed Bundle / APK).
3. Соберите:

```bash
./gradlew :app:assembleRelease
```

4. Проверьте установку на устройстве:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

## Юнит-тесты

```bash
./gradlew :app:testDebugUnitTest
```

## Запуск

Run ▶ app в Android Studio или `adb shell am start -n ru.finny.pet.debug/ru.finny.pet.MainActivity`.
