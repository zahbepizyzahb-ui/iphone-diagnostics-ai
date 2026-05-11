import pytest
from ai_services.component_detector import ComponentDetector
from unittest.mock import patch
import numpy as np

class TestComponentDetector:
    def setup_method(self):
        self.detector = ComponentDetector()

    @patch("cv2.imread")
    @patch("cv2.inRange")
    @patch("cv2.findContours")
    def test_detect_components(self, mock_contours, mock_inrange, mock_imread):
        mock_image = np.zeros((100, 100, 3), dtype=np.uint8)
        mock_imread.return_value = mock_image
        mock_contour = np.array([[10, 10], [20, 10], [20, 20], [10, 20]])
        mock_contours.return_value = ([mock_contour], None)
        mock_inrange.return_value = np.zeros((100, 100), dtype=np.uint8)
        result = self.detector.detect("fake_path.jpg")
        assert isinstance(result, list)

    def test_identify_component(self):
        result = self.detector._identify_component("black_ic", None, 0, 0, 10, 10)
        assert "Unknown" in result or "cpu" in result.lower()
