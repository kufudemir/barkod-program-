package com.marketpos.domain.usecase

import com.marketpos.domain.model.CompanyActivationState
import com.marketpos.domain.repository.ActivationRepository
import javax.inject.Inject

class ActivateCompanyUseCase @Inject constructor(
    private val activationRepository: ActivationRepository
) {
    suspend operator fun invoke(companyName: String): Result<CompanyActivationState> {
        return activationRepository.activate(companyName)
    }

    suspend fun createNewCompany(companyName: String): Result<CompanyActivationState> {
        return activationRepository.activateAsNewCompany(companyName)
    }

    suspend fun continueWithExistingCompany(companyCode: String): Result<CompanyActivationState> {
        return activationRepository.activateWithCompanyCode(companyCode)
    }
}
