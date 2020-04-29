package com.leonardo.drinkslab.ui.main.DrinkDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leonardo.drinkslab.data.repositories.DocumentsRepository
import com.leonardo.drinkslab.domain.IUseCase
import com.leonardo.drinkslab.domain.UseCaseImpl
import java.lang.IllegalArgumentException


@Suppress("UNCHECKED_CAST")
class DrinkDetailViewModelFactory(
    private val documentsRepository: DocumentsRepository,
    private val idDocument: String
) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DrinkDetailViewModel::class.java)){
            return DrinkDetailViewModel(documentsRepository, idDocument) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }


}