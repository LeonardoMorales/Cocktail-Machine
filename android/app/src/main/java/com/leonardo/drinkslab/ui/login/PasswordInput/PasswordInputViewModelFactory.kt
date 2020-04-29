package com.leonardo.drinkslab.ui.login.PasswordInput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leonardo.drinkslab.data.repositories.DocumentsRepository

@Suppress("UNCHECKED_CAST")
class PasswordInputViewModelFactory(
    private val documentsRepository: DocumentsRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PasswordInputViewModel(
            documentsRepository
        ) as T
    }

}