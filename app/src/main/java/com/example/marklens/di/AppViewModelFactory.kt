package com.example.marklens.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.marklens.data.ExamRepository
import com.example.marklens.ocr.OcrProvider
import com.example.marklens.ui.list.RecordListViewModel
import com.example.marklens.ui.review.ReviewViewModel
import com.example.marklens.ui.stats.StatsViewModel

/**
 * Manual DI factory — creates ViewModels with their required dependencies.
 *
 * @author Jianheng Sun
 */
class AppViewModelFactory(
    private val repository: ExamRepository,
    private val ocrProvider: OcrProvider? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ReviewViewModel::class.java) ->
                ReviewViewModel(repository) as T
            modelClass.isAssignableFrom(RecordListViewModel::class.java) ->
                RecordListViewModel(repository) as T
            modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                StatsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
