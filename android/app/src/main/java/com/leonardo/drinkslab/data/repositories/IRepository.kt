package com.leonardo.drinkslab.data.repositories

import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.util.vo.Resource
import kotlinx.coroutines.flow.Flow

interface IRepository {

    suspend fun getVersionCode(): Flow<Resource<Int>>

    suspend fun getMachineState(idDocument: String): Flow<Resource<Boolean>>

    suspend fun getDrinksList(idDocument: String): Flow<Resource<MutableList<Drink>>>

}