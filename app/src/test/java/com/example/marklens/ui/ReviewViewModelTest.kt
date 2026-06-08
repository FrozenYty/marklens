package com.example.marklens.ui

import com.example.marklens.parser.ParsedStudentInfo
import com.example.marklens.ui.review.ReviewViewModel
import com.example.marklens.ui.review.ScoreField
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class ReviewViewModelTest {

    @Test
    fun initialState_shouldHaveEmptyFields() {
        val vm = ReviewViewModel()
        val state = vm.uiState.value
        assertEquals(null, state.studentInfo.name)
        assertTrue(state.scores.isEmpty())
        assertFalse(state.isSaving)
    }

    @Test
    fun setParsedData_shouldPopulateFields() {
        val vm = ReviewViewModel()
        val info = ParsedStudentInfo("Alice", "2024001", "Class 1")
        val scores = listOf(
            ScoreField(1, "8.5", 10.0),
            ScoreField(2, "9.0", 10.0)
        )
        vm.setParsedData(info, "Math", "17.5", scores)
        val state = vm.uiState.value
        assertEquals("Alice", state.studentInfo.name)
        assertEquals("Math", state.subject)
        assertEquals("17.5", state.totalScore)
        assertEquals(2, state.scores.size)
    }

    @Test
    fun updateName_shouldChangeField() {
        val vm = ReviewViewModel()
        vm.setParsedData(ParsedStudentInfo("Old", "", ""), "", "", emptyList())
        vm.updateName("New")
        assertEquals("New", vm.uiState.value.studentInfo.name)
    }

    @Test
    fun updateScore_shouldChangeSpecificQuestion() {
        val vm = ReviewViewModel()
        vm.setParsedData(ParsedStudentInfo("", "", ""), "", "", listOf(
            ScoreField(1, "5.0", 10.0),
            ScoreField(2, "8.0", 10.0)
        ))
        vm.updateScore(1, "9.5")
        assertEquals("9.5", vm.uiState.value.scores[0].score)
        assertEquals("8.0", vm.uiState.value.scores[1].score)
    }

    @Test
    fun markSaveComplete_shouldSetFlag() {
        val vm = ReviewViewModel()
        vm.setParsedData(ParsedStudentInfo("A", "1", "C"), "S", "10", emptyList())
        vm.markSaveComplete()
        assertTrue(vm.uiState.value.saveComplete)
    }
}
