package com.marketpos.data.network

class ActivationConflictException(
    val existingCompanyName: String,
    val existingCompanyCode: String,
    message: String
) : IllegalStateException(message)
