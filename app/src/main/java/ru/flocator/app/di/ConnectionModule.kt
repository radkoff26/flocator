package ru.flocator.app.di

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.flocator.app.di.annotations.DependencyKey
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_receivers.NetworkReceiver
import javax.inject.Singleton

@Module
@Singleton
class ConnectionModule {

    @Provides
    @Singleton
    @IntoMap
    @DependencyKey(ConnectionLiveData::class)
    fun provideConnectionLiveData(networkReceiver: NetworkReceiver): ConnectionLiveData =
        networkReceiver.networkState
}