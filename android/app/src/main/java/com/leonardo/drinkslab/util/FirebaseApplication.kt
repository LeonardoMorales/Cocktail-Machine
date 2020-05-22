package com.leonardo.drinkslab.util

import androidx.lifecycle.ViewModelProvider
import androidx.multidex.MultiDexApplication
import com.leonardo.drinkslab.data.firebase.FirebaseSource
import com.leonardo.drinkslab.data.repositories.DocumentsRepository
import com.leonardo.drinkslab.data.repositories.IRepository
import com.leonardo.drinkslab.data.repositories.RepositoryImpl
import com.leonardo.drinkslab.domain.IUseCase
import com.leonardo.drinkslab.domain.UseCaseImpl
import com.leonardo.drinkslab.ui.ViewModelFactory
import com.leonardo.drinkslab.ui.bindViewModel
import com.leonardo.drinkslab.ui.login.PasswordInput.PasswordInputViewModel
import com.leonardo.drinkslab.ui.main.DrinkSelector.DrinksSelectorViewModel
import com.leonardo.drinkslab.ui.main.OwnDrink.OwnDrinkViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class FirebaseApplication : MultiDexApplication(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@FirebaseApplication))

        bind() from singleton { FirebaseSource() }

        bind() from singleton { DocumentsRepository(instance()) }

        bind<IRepository>() with singleton { RepositoryImpl(instance()) }
        bind<IUseCase>() with singleton { UseCaseImpl(instance()) }

        bind<ViewModelProvider.Factory>() with singleton { ViewModelFactory(kodein.direct) }

        bindViewModel<PasswordInputViewModel>() with provider { PasswordInputViewModel(instance()) }
        bindViewModel<DrinksSelectorViewModel>() with provider { DrinksSelectorViewModel(instance(), instance(), instance()) }
        bindViewModel<OwnDrinkViewModel>() with provider { OwnDrinkViewModel(instance()) }
    }
}