# SOlauncher (32-bit `armeabi-v7a` Android `.so` Mod Loader)

## Что внутри
- Автопоиск установленного пакета Minecraft Bedrock (`com.mojang.minecraftpe` и сторонних сборок с `libminecraftpe.so`).
- Импорт и проверка 32-битных `.so` модов (проверка ELF-заголовка `32-bit EM_ARM`) с переключателями.
- Нативный C++ JNI-загрузчик (`solauncher.cpp` + `CMakeLists.txt`), собранный строго под `armeabi-v7a`.
- Тёмно-изумрудное оформление Material 3 и готовая иконка с деревом во всех разрешениях `mipmap-*`.

## Как собрать APK в Android Studio
1. Распакуйте архив `SOlauncher_AndroidProject.zip`.
2. Откройте папку `SOlauncher` в **Android Studio**.
3. В **SDK Manager -> SDK Tools** установите **NDK (Side by side)** и **CMake**.
4. Дождитесь синхронизации Gradle и выберите **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
5. Готовый файл появится в `app/build/outputs/apk/debug/app-debug.apk`.
