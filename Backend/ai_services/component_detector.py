import cv2
import numpy as np
from PIL import Image
import pytesseract

class ComponentDetector:
    def __init__(self):
        # أنواع المكونات المعروفة
        self.component_types = {
            'cpu': ['A16', 'A17', 'A15', 'A14', 'A13', 'APL'],
            'power_ic': ['338S', 'SN2012', 'TPS'],
            'charging_ic': ['SN2012', '1610A', '610A'],
            'wifi': ['339S', 'BCM', 'WIFI'],
            'audio': ['338S00105', 'CS35L'],
            'storage': ['K3LK', 'K3LH', 'NAND']
        }

        # ألوان المكونات في HSV
        self.color_ranges = {
            'black_ic': ([0, 0, 0], [180, 255, 50]),
            'silver_cap': ([0, 0, 180], [180, 30, 255]),
            'gold_connector': ([15, 100, 100], [35, 255, 255]),
            'green_resistor': ([35, 50, 50], [85, 255, 200])
        }

    def detect(self, image_path):
        image = cv2.imread(image_path)
        if image is None:
            return []

        components = []
        height, width = image.shape[:2]

        # 1. اكتشاف المكونات باللون
        for color_name, (lower, upper) in self.color_ranges.items():
            lower = np.array(lower)
            upper = np.array(upper)

            mask = cv2.inRange(cv2.cvtColor(image, cv2.COLOR_BGR2HSV), lower, upper)
            contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

            for cnt in contours:
                area = cv2.contourArea(cnt)
                if area > 100:  # تجاهل المكونات الصغيرة جداً
                    x, y, w, h = cv2.boundingRect(cnt)

                    component = {
                        'id': len(components),
                        'name': self._identify_component(color_name, image, x, y, w, h),
                        'type': self._get_component_type(color_name),
                        'position': {
                            'x': (x + w/2) / width,
                            'y': (y + h/2) / height
                        },
                        'confidence': min(area / 1000, 0.95),
                        'partNumber': self._read_part_number(image, x, y, w, h),
                        'isFaulty': False,
                        'notes': f'Detected by color: {color_name}'
                    }
                    components.append(component)

        # 2. OCR للأرقام
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        text = pytesseract.image_to_string(gray)

        # 3. تحديد المكونات التالفة
        components = self._check_faults(components, image)

        return components

    def _identify_component(self, color_name, image, x, y, w, h):
        # استخراج المنطقة
        roi = image[y:y+h, x:x+w]

        # محاولة قراءة النص
        gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY)
        text = pytesseract.image_to_string(gray, config='--psm 7')

        # تحديد بناءً على النص
        for comp_type, keywords in self.component_types.items():
            for keyword in keywords:
                if keyword in text:
                    return f"{comp_type.upper()} - {keyword}"

        return f"Unknown {color_name}"

    def _get_component_type(self, color_name):
        type_map = {
            'black_ic': 'cpu',
            'silver_cap': 'capacitor',
            'gold_connector': 'connector',
            'green_resistor': 'resistor'
        }
        return type_map.get(color_name, 'unknown')

    def _read_part_number(self, image, x, y, w, h):
        roi = image[y:y+h, x:x+w]
        gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY)
        text = pytesseract.image_to_string(gray, config='--psm 7 -c tessedit_char_whitelist=0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ')
        return text.strip() if text.strip() else None

    def _check_faults(self, components, image):
        # فحص العيوب المرئية
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        for comp in components:
            x = int(comp['position']['x'] * image.shape[1])
            y = int(comp['position']['y'] * image.shape[0])

            # فحص الحروق أو التشققات
            roi = gray[max(0, y-20):y+20, max(0, x-20):x+20]
            if roi.size > 0:
                mean_val = np.mean(roi)
                std_val = np.std(roi)

                # إذا كان التباين عالياً جداً، قد يكون هناك تلف
                if std_val > 80:
                    comp['isFaulty'] = True
                    comp['notes'] += ' | Possible physical damage detected'

        return components

    def perform_ocr(self, image_path):
        image = Image.open(image_path)
        text = pytesseract.image_to_string(image)
        return text
