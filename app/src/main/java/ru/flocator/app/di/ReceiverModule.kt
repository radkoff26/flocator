package ru.flocator.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.flocator.core_receivers.NetworkReceiver

@InstallIn(SingletonComponent::class)
@Module
object ReceiverModule {

    @Provides
    fun provideNetworkReceiver(): NetworkReceiver = NetworkReceiver()
}