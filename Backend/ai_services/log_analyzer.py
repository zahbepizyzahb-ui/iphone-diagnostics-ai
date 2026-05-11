import re

class LogAnalyzer:
    def __init__(self):
        self.error_patterns = {
            r'panic\(cpu \d+ caller': {
                'issue': 'Kernel Panic',
                'severity': 'critical',
                'likely_cause': 'خلل في النواة - غالباً مشكلة hardware',
                'action': 'فحص اللوحة الأم، تحديث iOS، استعادة المصنع'
            },
            r'watchdog timeout': {
                'issue': 'Watchdog Timeout',
                'severity': 'high',
                'likely_cause': 'تطبيق يستغرق وقتاً طويلاً',
                'action': 'تحديث التطبيق، إعادة تثبيت، فحص الأداء'
            },
            r'memory pressure': {
                'issue': 'ضغط الذاكرة',
                'severity': 'high',
                'likely_cause': 'ذاكرة غير كافية',
                'action': 'إغلاق التطبيقات، إعادة التشغيل، فحص RAM'
            },
            r'thermal level': {
                'issue': 'ارتفاع الحرارة',
                'severity': 'medium',
                'likely_cause': 'سخونة مفرطة',
                'action': 'تبريد الجهاز، فحص البطارية، إزالة الغبار'
            }
        }

    def analyze(self, parsed_log):
        raw = parsed_log.get('rawLog', '')

        ai_result = {
            'ai_diagnosis': '',
            'confidence': 0.0,
            'related_issues': [],
            'preventive_actions': []
        }

        for pattern, info in self.error_patterns.items():
            if re.search(pattern, raw, re.IGNORECASE):
                ai_result['related_issues'].append(info['issue'])
                ai_result['ai_diagnosis'] = info['likely_cause']
                ai_result['preventive_actions'].append(info['action'])
                ai_result['confidence'] = max(ai_result['confidence'], 0.8)

        if not ai_result['ai_diagnosis']:
            ai_result['ai_diagnosis'] = 'تحليل AI: لا يوجد تشخيص محدد - يرجى مراجعة الخبراء'
            ai_result['confidence'] = 0.3

        return ai_result
