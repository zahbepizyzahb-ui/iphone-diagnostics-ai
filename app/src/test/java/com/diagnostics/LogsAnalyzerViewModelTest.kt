package com.diagnostics

import com.diagnostics.model.LogAnalysisResult
import com.diagnostics.viewmodel.LogsAnalyzerViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LogsAnalyzerViewModelTest {

    private lateinit var viewModel: LogsAnalyzerViewModel

    @Before
    fun setup() {
        viewModel = LogsAnalyzerViewModel()
    }

    @Test
    fun `parse EXC_BAD_ACCESS log correctly`() = runBlocking {
        val log = """Exception Type: EXC_BAD_ACCESS (SIGSEGV)
Exception Subtype: KERN_INVALID_ADDRESS at 0x0000000000000000
Thread 0 Crashed:
0   libsystem_kernel.dylib"""

        viewModel.analyzeText(log)

        val results = viewModel.results.value
        assertTrue("Should have results", results.isNotEmpty())

        val result = results.first()
        assertEquals("EXC_BAD_ACCESS (SIGSEGV)", result.exceptionType)
        assertEquals(LogAnalysisResult.SeverityLevel.CRITICAL, result.severity)
        assertTrue("Should mention memory", result.affectedComponent.contains("ذاكرة"))
    }

    @Test
    fun `parse EXC_RESOURCE log correctly`() = runBlocking {
        val log = "Exception Type: EXC_RESOURCE"

        viewModel.analyzeText(log)

        val results = viewModel.results.value
        assertTrue("Should have results", results.isNotEmpty())

        val result = results.first()
        assertEquals(LogAnalysisResult.SeverityLevel.HIGH, result.severity)
        assertTrue("Should mention battery", 
            result.affectedComponent.contains("بطارية") || result.affectedComponent.contains("battery"))
    }

    @Test
    fun `delete result removes it from list`() = runBlocking {
        val log = "Exception Type: EXC_BREAKPOINT"
        viewModel.analyzeText(log)

        val result = viewModel.results.value.first()
        viewModel.deleteResult(result)

        assertTrue("Should be empty", viewModel.results.value.isEmpty())
    }
}
