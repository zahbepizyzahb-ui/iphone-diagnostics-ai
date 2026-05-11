
try:
    from parsers.ios_crash_parser import IOSCrashParser
    print("   ✅ IOSCrashParser")

    from ai_services.log_analyzer import LogAnalyzer
    print("   ✅ LogAnalyzer")

    # اختبار تحليل
    parser = IOSCrashParser()
    test_log = '''Exception Type: EXC_BAD_ACCESS (SIGSEGV)
Exception Subtype: KERN_INVALID_ADDRESS at 0x0000000000000000
Thread 0 Crashed:
0   libsystem_kernel.dylib'''

    result = parser.parse(test_log)
    print(f"   ✅ تحليل ناجح: {result['exceptionType']}")
    print(f"      الخطورة: {result['severity']}")
    print(f"      المكون: {result['affectedComponent']}")

    # اختبار AI
    analyzer = LogAnalyzer()
    ai_result = analyzer.analyze(result)
    print(f"   ✅ AI Analysis: {ai_result['ai_diagnosis'][:50]}...")

except Exception as e:
    print(f"   ❌ خطأ: {e}")
