package ru.flocator.app.di.modules.app

import dagger.Module
import dagger.Provides
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_receivers.NetworkReceiver
import javax.inject.Singleton

@Module
object ConnectionModule {

    @Provides
    @Singleton
    fun provideConnectionLiveData(networkReceiver: NetworkReceiver): ConnectionLiveData =
        networkReceiver.networkState
}