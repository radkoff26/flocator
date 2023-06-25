package ru.flocator.app.di

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.flocator.app.di.annotations.DependencyKey
import ru.flocator.core_receivers.NetworkReceiver
import javax.inject.Singleton

@Module
@Singleton
class ReceiverModule {

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(NetworkReceiver::class)
    fun provideNetworkReceiver(): NetworkReceiver = NetworkReceiver()
}