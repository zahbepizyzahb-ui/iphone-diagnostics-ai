# 🧪 دليل اختبار التطبيق

## ✅ Backend (مختبر وجاهز)

```bash
cd Backend
python app.py
```

سيعمل على: `http://localhost:8000`

## 📱 Android App (3 طرق للاختبار)

### الطريقة 1: Android Studio (موصى)
```bash
# 1. افتح المشروع في Android Studio
# 2. انتظر Gradle Sync
# 3. اضغط Run ▶️
```

### الطريقة 2: GitHub Actions (أسهل)
```bash
# 1. ارفع المشروع على GitHub
# 2. اذهب إلى Actions → Build Android APK
# 3. اضغط "Run workflow"
# 4. بعد 5-10 دقائق، حمل APK من Artifacts
```

### الطريقة 3: Docker (للمطورين)
```bash
# Backend
docker-compose up --build

# Android (يحتاج Android SDK في Docker)
# غير موصى للمبتدئين
```

## 🔧 حلول المشاكل الشائعة

| المشكلة | الحل |
|---------|------|
| Gradle sync failed | File → Invalidate Caches → Restart |
| Cannot resolve symbol | Build → Clean Project → Rebuild |
| Backend connection | تأكد من تشغيل Backend وتطابق IP |
| Camera not working | تأكد من صلاحيات الكاميرا |
| ML Kit not working | يحتاج Google Play Services |

## 📋 قائمة الاختبار

### ✅ اختبار السجلات
- [ ] استيراد ملف .ips
- [ ] لصق نص السجل
- [ ] عرض نتائج التحليل
- [ ] توسيع/تصغير التفاصيل

### ✅ اختبار الماسح
- [ ] فتح الكاميرا
- [ ] التقاط صورة
- [ ] اختيار من المعرض
- [ ] عرض المكونات المكتشفة

### ✅ اختبار التشخيص
- [ ] تشخيص شامل جديد
- [ ] عرض صحة الجهاز
- [ ] مراجعة النتائج السابقة

### ✅ اختبار الإعدادات
- [ ] تغيير مزود OCR
- [ ] إدخال API Keys
- [ ] حفظ الإعدادات
- [ ] إعادة التعيين
