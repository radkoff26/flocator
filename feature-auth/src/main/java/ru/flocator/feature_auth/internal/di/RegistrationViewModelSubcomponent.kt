package ru.flocator.feature_auth.internal.di

import dagger.Subcomponent
import ru.flocator.feature_auth.internal.di.annotations.ViewModelScope

@Subcomponent(
    modules = [
        AuthModule::class
    ]
)
@ViewModelScope
class RegistrationViewModelSubcomponent {

}