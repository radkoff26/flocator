package ru.flocator.app.di.modules.app

import dagger.Module
import dagger.Provides
import ru.flocator.core_receivers.NetworkReceiver
import javax.inject.Singleton

@Module
class ReceiverModule {

    @Provides
    @Singleton
    fun provideNetworkReceiver(): NetworkReceiver = NetworkReceiver()
}