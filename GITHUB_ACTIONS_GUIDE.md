# 🚀 بناء APK عبر GitHub Actions

## الطريقة السهلة (بدون Android Studio)

### الخطوة 1: رفع المشروع على GitHub
```bash
# 1. أنشئ repository جديد على GitHub
# 2. ارفع المشروع:
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/iPhoneDiagnosticsAI.git
git push -u origin main
```

### الخطوة 2: تشغيل GitHub Actions
```
1. اذهب إلى repository على GitHub
2. اضغط على تبويب "Actions"
3. اختر "Build Android APK"
4. اضغط "Run workflow"
5. اختر نوع البناء:
   - debug: APK للتجربة
   - release: APK للنشر
   - both: الاثنين معاً
6. اضغط "Run workflow"
```

### الخطوة 3: تحميل APK
```
1. انتظر 5-10 دقائق حتى ينتهي البناء
2. اذهب إلى تبويب "Actions"
3. اضغط على آخر workflow
4. في قسم "Artifacts" اسفل الصفحة
5. اضغط "app-debug" أو "app-release-unsigned"
6. سيتم تحميل ملف ZIP يحتوي على APK
```

---

## الطريقة المتقدمة (مع توقيع APK)

### إنشاء Keystore
```bash
keytool -genkey -v -keystore iphone-diagnostics.keystore \
  -alias iphoneai -keyalg RSA -keysize 2048 -validity 10000
```

### إضافة Secrets في GitHub
```
1. اذهب إلى Settings → Secrets and variables → Actions
2. اضف New repository secret:
   - Name: KEYSTORE_BASE64
   - Value: base64 iphone-diagnostics.keystore
3. اضف:
   - Name: KEYSTORE_PASSWORD
   - Value: كلمة المرور
4. اضف:
   - Name: KEY_ALIAS
   - Value: iphoneai
5. اضف:
   - Name: KEY_PASSWORD
   - Value: كلمة المرور
```

### تحديث workflow للتوقيع
```yaml
- name: 🔏 Sign Release APK
  uses: r0adkll/sign-android-release@v1
  with:
    releaseDirectory: app/build/outputs/apk/release
    signingKeyBase64: ${{ secrets.KEYSTORE_BASE64 }}
    alias: ${{ secrets.KEY_ALIAS }}
    keyStorePassword: ${{ secrets.KEYSTORE_PASSWORD }}
    keyPassword: ${{ secrets.KEY_PASSWORD }}
```

---

## 🐳 بناء APK عبر Docker (للمطورين)

```bash
# بناء Docker image
docker build -t iphone-diagnostics-builder .

# تشغيل البناء
docker run -v $(pwd)/output:/output iphone-diagnostics-builder

# APK سيكون في مجلد output/
```

---

## 📱 تثبيت APK على الجهاز

### عبر ADB
```bash
# تفعيل USB Debugging على الجهاز
# توصيل الجهاز بالكمبيوتر
adb install iPhoneDiagnosticsAI-debug.apk
```

### عبر Android Studio
```
1. افتح Android Studio
2. File → Profile or Debug APK
3. اختر ملف APK
4. اضغط Run
```

### عبر الجهاز مباشرة
```
1. انقل APK للجهاز
2. افتح الملف
3. اضغط "تثبيت"
4. قد تحتاج تفعيل "مصادر غير معروفة"
```

---

## ⚡ نصائح سريعة

| المشكلة | الحل |
|---------|------|
| البناء بطيء | تفعيل Gradle Daemon |
| ذاكرة غير كافية | زيادة heap size: `org.gradle.jvmargs=-Xmx4g` |
| Gradle sync failed | حذف `.gradle` folder |
| cannot resolve | `./gradlew clean build` |

---

## 🎯 النتيجة المتوقعة

بعد 5-10 دقائق من تشغيل GitHub Actions:

```
✅ app-debug.apk (حوالي 15-25 MB)
✅ app-release-unsigned.apk (حوالى 12-20 MB)
✅ test-results/
✅ lint-results/
```

---

**🎉 APK جاهز للاستخدام!**
