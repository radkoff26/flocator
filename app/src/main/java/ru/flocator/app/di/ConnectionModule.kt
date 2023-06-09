package ru.flocator.app.di

import ru.flocator.app.common.connection.live_data.ConnectionLiveData
import ru.flocator.app.common.receivers.NetworkReceiver
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
    fun provideConnectionLiveData(networkReceiver: NetworkReceiver): ConnectionLiveData {
        return networkReceiver.networkState
    }
}