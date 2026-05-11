# 🚀 دليل البدء السريع

## في 5 دقائق!

### 1. حمل المشروع
```bash
git clone https://github.com/yourusername/iPhoneDiagnosticsAI.git
cd iPhoneDiagnosticsAI_Android
```

### 2. شغّل Backend
```bash
cd Backend
pip install -r requirements.txt
python app.py
```

### 3. افتح في Android Studio
- File → Open → اختر مجلد `iPhoneDiagnosticsAI_Android`
- انتظر Gradle Sync (~2-5 دقائق أول مرة)

### 4. اضبط IP الجهاز
في `Settings` → `Backend URL`:
- للمحاكي: `http://10.0.2.2:8000`
- للجهاز الحقيقي: `http://YOUR_COMPUTER_IP:8000`

### 5. اضغط Run! 🎉

---

## 🧪 اختبار سريع

### اختبار السجلات
1. اذهب إلى تبويب "السجلات"
2. اضغط "لصق نص"
3. الصق هذا السجل:
```
Exception Type: EXC_BAD_ACCESS (SIGSEGV)
Exception Subtype: KERN_INVALID_ADDRESS at 0x0000000000000000
Thread 0 Crashed:
0   libsystem_kernel.dylib
```
4. اضغط "تحليل"

### اختبار الماسح
1. اذهب إلى تبويب "الماسح"
2. اضغط "كاميرا"
3. صوّر أي لوحة إلكترونية
4. انتظر التحليل

---

## 🔧 حلول سريعة

| المشكلة | الحل |
|---------|------|
| Gradle sync failed | File → Invalidate Caches → Restart |
| Cannot resolve symbol | Build → Clean Project → Rebuild |
| Backend not connected | تأكد من IP وتطابق المنفذ |
| Camera not working | تأكد من صلاحيات الكاميرا |
| ML Kit not working | يحتاج Google Play Services |

---

## 📞 دعم

- 🐛 Issue: [GitHub Issues](https://github.com/yourusername/iPhoneDiagnosticsAI/issues)
- 💬 Discussion: [GitHub Discussions](https://github.com/yourusername/iPhoneDiagnosticsAI/discussions)
- 📧 Email: support@iphonediagnostics.ai
