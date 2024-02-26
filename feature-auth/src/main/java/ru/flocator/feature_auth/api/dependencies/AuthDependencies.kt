package ru.flocator.feature_auth.api.dependencies

import retrofit2.Retrofit
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.data.token.TokenPreferences

interface AuthDependencies : Dependencies {
    val tokenPreferences: TokenPreferences
    val retrofit: Retrofit
}