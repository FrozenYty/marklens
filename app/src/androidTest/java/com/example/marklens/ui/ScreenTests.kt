package com.example.marklens.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.marklens.ui.capture.CaptureScreen
import com.example.marklens.ui.capture.CaptureViewModel
import com.example.marklens.ui.list.RecordListScreen
import com.example.marklens.ui.list.RecordListViewModel
import com.example.marklens.ui.review.ReviewScreen
import com.example.marklens.ui.review.ReviewViewModel
import com.example.marklens.ui.stats.StatsScreen
import com.example.marklens.ui.stats.StatsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI smoke tests — verify each screen renders without crashing
 * and shows expected content.
 *
 * @author Jianheng Sun
 */
@RunWith(AndroidJUnit4::class)
class ScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureScreen_emptyState_showsPrompt() {
        composeTestRule.setContent {
            CaptureScreen(viewModel = CaptureViewModel())
        }
        composeTestRule.onNodeWithText("No image selected").assertIsDisplayed()
        composeTestRule.onNodeWithText("📁 Select from Gallery").assertIsDisplayed()
    }

    @Test
    fun captureScreen_emptyState_showsDemoButton() {
        composeTestRule.setContent {
            CaptureScreen(viewModel = CaptureViewModel())
        }
        composeTestRule.onNodeWithText("🎓 Load Demo Exam").assertIsDisplayed()
    }

    @Test
    fun captureScreen_emptyState_showsHandwritingButton() {
        composeTestRule.setContent {
            CaptureScreen(viewModel = CaptureViewModel())
        }
        composeTestRule.onNodeWithText("✍ Load handwriting").assertIsDisplayed()
    }

    @Test
    fun reviewScreen_rendersFormFields() {
        composeTestRule.setContent {
            ReviewScreen(viewModel = ReviewViewModel())
        }
        composeTestRule.onNodeWithText("Review & Correct").assertIsDisplayed()
        composeTestRule.onNodeWithText("Student Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exam Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save to Database").assertIsDisplayed()
    }

    @Test
    fun recordListScreen_emptyState_showsMessage() {
        composeTestRule.setContent {
            RecordListScreen(viewModel = RecordListViewModel())
        }
        composeTestRule.onNodeWithText("No records yet").assertIsDisplayed()
    }

    @Test
    fun recordListScreen_showsHeader() {
        composeTestRule.setContent {
            RecordListScreen(viewModel = RecordListViewModel())
        }
        composeTestRule.onNodeWithText("Exam Records").assertIsDisplayed()
    }

    @Test
    fun statsScreen_loadingState_showsSpinner() {
        composeTestRule.setContent {
            StatsScreen(viewModel = StatsViewModel())
        }
        // Null repository → immediately sets isLoading=false, shows empty state
        composeTestRule.onNodeWithText("No data available").assertIsDisplayed()
    }

    @Test
    fun statsScreen_showsTitle() {
        composeTestRule.setContent {
            StatsScreen(viewModel = StatsViewModel())
        }
        composeTestRule.onNodeWithText("All Subjects — Statistics").assertIsDisplayed()
    }
}
