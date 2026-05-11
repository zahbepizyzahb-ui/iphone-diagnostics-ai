from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import os
from datetime import datetime

# استيراد المحللات
from parsers.ios_crash_parser import IOSCrashParser
from ai_services.component_detector import ComponentDetector
from ai_services.log_analyzer import LogAnalyzer

app = Flask(__name__)
CORS(app)

# إنشاء المجلدات
UPLOAD_FOLDER = 'uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# تهيئة الخدمات
crash_parser = IOSCrashParser()
component_detector = ComponentDetector()
log_analyzer = LogAnalyzer()

@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({"status": "ok", "timestamp": datetime.now().isoformat(), "version": "1.1.0"})

@app.route('/api/analyze-log', methods=['POST'])
def analyze_log():
    try:
        data = request.get_json()
        log_text = data.get('log_text', '')

        if not log_text:
            return jsonify({"error": "No log text provided"}), 400

        # تحليل السجل
        result = crash_parser.parse(log_text)

        # تحليل AI إضافي
        ai_analysis = log_analyzer.analyze(result)
        result.update(ai_analysis)

        return jsonify(result)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/analyze-board', methods=['POST'])
def analyze_board():
    try:
        if 'image' not in request.files:
            return jsonify({"error": "No image provided"}), 400

        file = request.files['image']
        filepath = os.path.join(UPLOAD_FOLDER, file.filename)
        file.save(filepath)

        # اكتشاف المكونات
        components = component_detector.detect(filepath)

        # تنظيف
        os.remove(filepath)

        return jsonify(components)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ✅ endpoint جديد: تحليل AI شامل للصور
@app.route('/api/analyze-board-ai', methods=['POST'])
def analyze_board_ai():
    try:
        if 'image' not in request.files:
            return jsonify({"error": "No image provided"}), 400

        file = request.files['image']
        ocr_text = request.form.get('ocr_text', '')

        filepath = os.path.join(UPLOAD_FOLDER, file.filename)
        file.save(filepath)

        # 1. اكتشاف المكونات
        components = component_detector.detect(filepath)

        # 2. OCR إضافي إذا لم يكن موجود
        if not ocr_text:
            ocr_text = component_detector.perform_ocr(filepath)

        # 3. تحليل AI
        ai_insights = generate_ai_insights(components, ocr_text)

        # تنظيف
        os.remove(filepath)

        return jsonify({
            "components": components,
            "ocr_text": ocr_text,
            "ai_insights": ai_insights,
            "analyzed_at": datetime.now().isoformat()
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

def generate_ai_insights(components, ocr_text):
    """توليد تحليل AI للمكونات المكتشفة"""
    insights = []

    # تحليل المكونات التالفة
    faulty = [c for c in components if c.get('isFaulty', False)]
    if faulty:
        insights.append(f"⚠️ تم اكتشاف {len(faulty)} مكون(ات) قد تكون تالفة:")
        for comp in faulty:
            insights.append(f"   - {comp['name']}: {comp.get('notes', 'يرجى الفحص')}")

    # تحليل أرقام القطع
    if ocr_text:
        insights.append(f"🔍 نص OCR: {ocr_text[:200]}...")

        # البحث عن أرقام قطع معروفة
        known_parts = {
            '338S': 'دائرة إدارة طاقة Apple',
            'APL1': 'معالج Apple A-series',
            'SN20': 'دائرة شحن / طاقة',
            '616-': 'بطارية iPhone'
        }

        for code, desc in known_parts.items():
            if code in ocr_text:
                insights.append(f"✅ تم التعرف على: {desc}")

    # توصيات عامة
    insights.append("💡 توصيات:")
    insights.append("   - فحص المكونات المحددة بأجهزة اختبار متخصصة")
    insights.append("   - مقارنة أرقام القطع مع datasheet الرسمي")
    insights.append("   - فحص اللحامات والمسارات المحيطة")

    return "\n".join(insights)

@app.route('/api/ocr', methods=['POST'])
def ocr_endpoint():
    try:
        if 'image' not in request.files:
            return jsonify({"error": "No image provided"}), 400

        file = request.files['image']
        filepath = os.path.join(UPLOAD_FOLDER, file.filename)
        file.save(filepath)

        # OCR
        text = component_detector.perform_ocr(filepath)

        os.remove(filepath)

        return jsonify({"text": text})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 8000))
    debug = os.environ.get('FLASK_ENV', 'production') == 'development'
    app.run(host='0.0.0.0', port=port, debug=debug)
