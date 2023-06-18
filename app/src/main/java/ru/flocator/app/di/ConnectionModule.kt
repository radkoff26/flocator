package ru.flocator.app.di

import ru.flocator.core_receivers.NetworkReceiver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ConnectionModule {
    @Provides
    @Singleton
    fun provideConnectionLiveData(networkReceiver: NetworkReceiver): ru.flocator.core_connection.live_data.ConnectionLiveData {
        return networkReceiver.networkState
    }
}