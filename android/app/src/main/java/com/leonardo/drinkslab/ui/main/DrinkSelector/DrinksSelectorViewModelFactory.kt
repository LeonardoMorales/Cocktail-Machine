package com.leonardo.drinkslab.ui.main.DrinkSelector

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leonardo.drinkslab.data.repositories.DocumentsRepository
import com.leonardo.drinkslab.domain.IUseCase
import com.leonardo.drinkslab.domain.UseCaseImpl


@Suppress("UNCHECKED_CAST")
class DrinksSelectorViewModelFactory(
    private val application: Application,
    private val iUseCase: IUseCase,
    private val documentsRepository: DocumentsRepository
) : ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DrinksSelectorViewModel(application, iUseCase, documentsRepository) as T
    }
}