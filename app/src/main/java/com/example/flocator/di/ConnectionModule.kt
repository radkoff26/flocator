package com.example.flocator.di

import com.example.flocator.common.connection.live_data.ConnectionLiveData
import com.example.flocator.common.receivers.NetworkReceiver
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