# Changelog

## [1.1.0] - 2024-01-20

### Added
- ✅ **اختبار API الذكاء الاصطناعي**: زر لاختبار الاتصال بـ Backend وOCR
- ✅ **تحليل AI للصور**: تحليل شامل للبورد بالذكاء الاصطناعي
- ✅ **دعم ملفات TXT**: استيراد سجلات الأخطاء كملفات .txt و .crash و .log
- ✅ **OCR محلي**: Google ML Kit (مجاني - يعمل بدون إنترنت)
- ✅ **عرض نص OCR**: عرض النص المكتشف على البورد
- ✅ **تحليل AI Insights**: توصيات ذكية للمكونات المكتشفة
- ✅ **Backend AI Endpoint**: /api/analyze-board-ai

### Improved
- 🚀 تحسين محلل السجلات: دعم كلمات مفتاحية إضافية
- 🚀 تحسين UI: عرض السجل الخام في التفاصيل
- 🚀 تحسين Settings: اختبار API مباشر
- 🚀 دعم 4 مزودي OCR: ML Kit, OCR.space, Google Vision, Azure

### Fixed
- 🐛 إصلاح دعم ملفات .txt
- 🐛 إصلاح عرض المكونات التالفة

## [1.0.0] - 2024-01-15

### Added
- ✅ تحليل سجلات أعطال iPhone (.ips)
- ✅ ماسح بورد iPhone بالكاميرا
- ✅ التشخيص الذكي AI
- ✅ دعم 4 مزودي OCR
- ✅ واجهة مستخدم عربية
- ✅ Backend Python (Flask)
- ✅ Docker + Docker Compose
- ✅ GitHub Actions CI/CD
