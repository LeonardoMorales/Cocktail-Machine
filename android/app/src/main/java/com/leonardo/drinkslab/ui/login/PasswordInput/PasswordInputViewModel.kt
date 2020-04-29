package com.leonardo.drinkslab.ui.login.PasswordInput

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.leonardo.drinkslab.data.model.Machine
import com.leonardo.drinkslab.data.repositories.DocumentsRepository

class PasswordInputViewModel (
    private val documentsRepository: DocumentsRepository
): ViewModel()
{

    fun getMachine(idDocument: String): LiveData<Machine> {
        val mutableData = MutableLiveData<Machine>()
        documentsRepository.getMachine(idDocument).observeForever { machine ->
            mutableData.value = machine
        }
        return mutableData
    }
}