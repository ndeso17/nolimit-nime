# NimeNoLimit

Modul **LSPosed** untuk menembus limitasi aplikasi: membuka fitur premium,
menghilangkan batas kuota/tiket, mematikan iklan, dan menekan popup izin
serta notifikasi — **tanpa memodifikasi APK** (signature asli tetap utuh).

> Proyek ini untuk penggunaan pribadi & pembelajaran. Gunakan dengan bijak.

---

## Apa yang dilakukan

Modul ini meng-hook komponen framework di dalam proses aplikasi target:

| Hook | Efek |
|---|---|
| `SharedPreferences` (`getAll`/`getInt`/`getLong`) | Memaksa status premium aktif & kuota tak terbatas |
| `Activity#requestPermissions` | Menjawab permintaan izin tanpa memunculkan dialog |
| `NotificationManager#notify` | Memblokir semua notifikasi |
| Channel plugin iklan (AdMob) | Mematikan seluruh iklan AdMob |
| `UnityAds.show` / `UnityAdsImplementation.show` | Mematikan seluruh iklan Unity Ads |

Karena APK tidak disentuh, signature aplikasi tetap asli sehingga
login Google (Google Sign-In) tetap berfungsi.

---

## Persyaratan

- Perangkat **rooted** (KernelSU / Magisk).
- **LSPosed** (via Zygisk) sudah terpasang dan aktif.
- Android 11+ (karena memakai `forceQueryable`).

---

## Cara install

1. Ambil APK modul hasil build:
   `work/lsposed-module/build/NimeNoLimit.apk`
   (atau build sendiri, lihat bagian **Build dari source**).

2. Pasang di perangkat:
   ```bash
   adb install -r NimeNoLimit.apk
   ```
   atau buka file APK langsung di HP.

3. Buka **LSPosed Manager** → tab **Modules**.
   Modul *NimeNoLimit* akan muncul di daftar.

4. Aktifkan modul (toggle ON).

5. Buka **Scope** modul, centang aplikasi target yang ingin dipatch.

6. **Restart** aplikasi target:
   ```bash
   adb shell am force-stop <package.target>
   adb shell am start -n <package.target>/.MainActivity
   ```

---

## Cara pakai / verifikasi

- Setelah aplikasi target dibuka ulang, fitur premium & kuota tak terbatas
  langsung aktif, iklan hilang, dan tidak ada popup izin / notifikasi.
- Cek log hook aktif:
  ```bash
  adb logcat -d | grep -i "NimeNoLimit"
  ```
  Harus muncul baris seperti:
  ```
  NimeNoLimit: hooked SharedPreferencesImpl#getAll
  NimeNoLimit: blocked google_mobile_ads channel handler
  ```
- Cek tidak ada crash:
  ```bash
  adb logcat -d | grep -iE "FATAL|AndroidRuntime"
  ```

---

## Build dari source

Butuh: JDK, Android SDK build-tools, `aapt2`, `d8`, `zipalign`, `apksigner`.

```bash
cd work/lsposed-module
SDK=~/Expo
D=$SDK/build-tools/36.0.0

# compile
rm -rf build && mkdir -p build/classes
javac -source 8 -target 8 -d build/classes \
  -cp $SDK/platforms/android-34/android.jar \
  $(find stub src -name '*.java')

# dex
mkdir -p build/module_classes build/dex
cp -r build/classes/com build/module_classes/
$D/d8 --min-api 24 --output build/dex/ $(find build/module_classes -name '*.class')

# resources
$D/aapt2 compile --dir res -o build/res.zip
$D/aapt2 link -o build/resources.apk -I $SDK/platforms/android-34/android.jar \
  --manifest AndroidManifest.xml build/res.zip --min-sdk-version 24 --target-sdk-version 34

# assemble (resources.arsc harus STORED + aligned)
rm -rf build/apk && mkdir -p build/apk/assets
cd build/apk && unzip -q ../resources.apk
cp ../dex/classes.dex classes.dex
cp ../../assets/xposed_init assets/xposed_init
rm -f ../NimeNoLimit_unsigned.apk
zip -q -X -r ../NimeNoLimit_unsigned.apk . -0 "resources.arsc"
cd ..

# sign
$D/zipalign -f 4 NimeNoLimit_unsigned.apk NimeNoLimit_aligned.apk
$D/apksigner sign --ks ../patch.keystore --ks-pass pass:android --key-pass pass:android \
  --ks-key-alias patched --out NimeNoLimit.apk NimeNoLimit_aligned.apk
```

---

## Struktur repo

```
work/lsposed-module/
├── AndroidManifest.xml          # manifest modul
├── assets/xposed_init           # entry point modul
├── res/values/arrays.xml        # daftar scope
├── src/com/ndeso17/nimesnolimit/
│   ├── MainHook.java            # hook prefs + izin + notifikasi
│   ├── AdBlocker.java           # blokir iklan
│   └── SettingsActivity.java    # activity info
├── stub/de/robv/android/xposed/ # API stub (compile-only)
└── build/NimeNoLimit.apk # hasil build siap install
```

---

## Disclaimer

Proyek ini dibuat untuk pembelajaran reverse engineering pada aplikasi yang
kamu miliki/kelola sendiri. Kamu bertanggung jawab mematuhi hukum dan
ketentuan layanan yang berlaku.
