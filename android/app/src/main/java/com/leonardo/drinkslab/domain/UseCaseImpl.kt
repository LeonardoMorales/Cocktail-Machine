package com.leonardo.drinkslab.domain

import com.leonardo.drinkslab.data.repositories.IRepository
import com.leonardo.drinkslab.util.vo.Resource
import kotlinx.coroutines.flow.Flow

class UseCaseImpl(
    private val iRepository: IRepository
) : IUseCase {

    override suspend fun getVersionCode(): Flow<Resource<Int>> = iRepository.getVersionCode()

    override suspend fun getMachineState(idDocument: String): Flow<Resource<Boolean>> = iRepository.getMachineState(idDocument)

}