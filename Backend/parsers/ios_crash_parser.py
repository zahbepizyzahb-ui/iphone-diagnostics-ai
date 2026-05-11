import re
import json
from datetime import datetime

class IOSCrashParser:
    def __init__(self):
        self.exception_patterns = {
            'EXC_BAD_ACCESS': {
                'severity': 'critical',
                'reason': 'وصول لذاكرة غير صالحة',
                'component': 'ذاكرة النظام / المعالج',
                'fix': [
                    'فحص ذاكرة الجهاز',
                    'إعادة تعيين إعدادات الشبكة',
                    'تحديث iOS',
                    'فحص اللوحة الأم'
                ]
            },
            'EXC_CRASH': {
                'severity': 'critical',
                'reason': 'تعطل النظام',
                'component': 'النظام العام',
                'fix': [
                    'تحديث iOS',
                    'استعادة إعدادات المصنع',
                    'فحص اللوحة الأم'
                ]
            },
            'EXC_RESOURCE': {
                'severity': 'high',
                'reason': 'استهلاك مفرط للموارد',
                'component': 'البطارية / المعالج',
                'fix': [
                    'إغلاق التطبيقات في الخلفية',
                    'تقليل سطوع الشاشة',
                    'تعطيل خدمات الموقع',
                    'فحص البطارية'
                ]
            },
            'EXC_BREAKPOINT': {
                'severity': 'medium',
                'reason': 'نقطة توقف في الكود',
                'component': 'البرمجيات',
                'fix': [
                    'تحديث التطبيق المتسبب',
                    'إعادة تثبيت التطبيق',
                    'مسح ذاكرة التخزين المؤقت'
                ]
            },
            'EXC_GUARD': {
                'severity': 'high',
                'reason': 'انتهاك حماية الملفات',
                'component': 'نظام الملفات',
                'fix': [
                    'فحص صلاحيات التطبيقات',
                    'إعادة تشغيل الجهاز',
                    'تحديث iOS'
                ]
            }
        }

        self.component_keywords = {
            'PowerUI': 'دائرة الطاقة / البطارية',
            'battery': 'دائرة الطاقة / البطارية',
            'WiFi': 'وحدة الواي فاي',
            '80211': 'وحدة الواي فاي',
            'Bluetooth': 'وحدة البلوتوث',
            'Audio': 'دائرة الصوت',
            'CoreAudio': 'دائرة الصوت',
            'Display': 'دائرة الشاشة',
            'Backlight': 'دائرة الشاشة',
            'Touch': 'دائرة اللمس',
            'Multitouch': 'دائرة اللمس',
            'Camera': 'كاميرا',
            'ISP': 'معالج الصور',
            'NAND': 'ذاكرة التخزين',
            'DRAM': 'ذاكرة الوصول العشوائي'
        }

    def parse(self, log_text):
        lines = log_text.split('\n')

        result = {
            'timestamp': datetime.now().isoformat(),
            'exceptionType': 'Unknown',
            'exceptionSubtype': '',
            'faultingThread': 0,
            'crashReason': 'غير محدد',
            'affectedComponent': 'غير معروف',
            'suggestedFix': '',
            'severity': 'medium',
            'rawLog': log_text[:1000]  # أول 1000 حرف
        }

        for line in lines:
            # Exception Type
            if 'Exception Type:' in line:
                exc_type = line.split(':')[-1].strip()
                result['exceptionType'] = exc_type

                # تحديد الخطورة والسبب
                for pattern, info in self.exception_patterns.items():
                    if pattern in exc_type:
                        result['severity'] = info['severity']
                        result['crashReason'] = info['reason']
                        result['affectedComponent'] = info['component']
                        result['suggestedFix'] = '\n'.join(info['fix'])
                        break

            # Exception Subtype
            if 'Exception Subtype:' in line:
                result['exceptionSubtype'] = line.split(':')[-1].strip()

            # Faulting Thread
            thread_match = re.search(r'Thread\s+(\d+)\s+Crashed', line)
            if thread_match:
                result['faultingThread'] = int(thread_match.group(1))

            # تحديد المكون من المكدس
            for keyword, component in self.component_keywords.items():
                if keyword in line:
                    result['affectedComponent'] = component
                    break

        return result
