import pytest
from parsers.ios_crash_parser import IOSCrashParser

class TestIOSCrashParser:
    def setup_method(self):
        self.parser = IOSCrashParser()

    def test_parse_bad_access(self):
        log = "Exception Type: EXC_BAD_ACCESS (SIGSEGV)\nException Subtype: KERN_INVALID_ADDRESS at 0x0000000000000000\nThread 0 Crashed:\n0   libsystem_kernel.dylib"
        result = self.parser.parse(log)
        assert result["exceptionType"] == "EXC_BAD_ACCESS (SIGSEGV)"
        assert result["severity"] == "critical"
        assert "memory" in result["affectedComponent"] or "ذاكرة" in result["affectedComponent"]

    def test_parse_resource(self):
        log = "Exception Type: EXC_RESOURCE"
        result = self.parser.parse(log)
        assert result["severity"] == "high"
        assert "battery" in result["affectedComponent"].lower() or "بطارية" in result["affectedComponent"]

    def test_empty_log(self):
        result = self.parser.parse("")
        assert result["exceptionType"] == "Unknown"
        assert result["severity"] == "medium"
