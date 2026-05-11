#!/bin/bash
# build-apk.sh - سكريبت بناء APK محلياً

set -e

echo "═══════════════════════════════════════════════════════════════"
echo "  🔧 iPhone Diagnostics AI - APK Builder"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# التحقق من المتطلبات
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo "❌ $1 غير مثبت. يرجى تثبيته أولاً."
        exit 1
    fi
    echo "✅ $1 موجود"
}

echo "📦 التحقق من المتطلبات..."
check_command java
check_command git

if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️ ANDROID_HOME غير محدد"
    echo "   يرجى تعيينه: export ANDROID_HOME=/path/to/android-sdk"
    exit 1
fi
echo "✅ ANDROID_HOME: $ANDROID_HOME"

echo ""
echo "🧹 تنظيف المشروع..."
./gradlew clean

echo ""
echo "🔨 بناء Debug APK..."
./gradlew assembleDebug --stacktrace

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ تم البناء بنجاح!"
    echo ""
    echo "📦 Debug APK:"
    ls -lh app/build/outputs/apk/debug/*.apk
    echo ""
    echo "💡 لتثبيت على جهاز:"
    echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""

    # نسخ APK للمجلد الرئيسي
    cp app/build/outputs/apk/debug/app-debug.apk ./iPhoneDiagnosticsAI-debug.apk
    echo "📥 تم نسخ APK: ./iPhoneDiagnosticsAI-debug.apk"
else
    echo ""
    echo "❌ فشل البناء"
    exit 1
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  ✅ انتهى البناء!"
echo "═══════════════════════════════════════════════════════════════"
