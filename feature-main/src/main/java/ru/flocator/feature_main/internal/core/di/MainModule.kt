package ru.flocator.feature_main.internal.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.flocator.core.config.Constants
import ru.flocator.core.view_model.ViewModelFactory
import ru.flocator.core.view_model.ViewModelsMap
import ru.flocator.core.view_model.annotations.ViewModelKey
import ru.flocator.data.models.address.AddressDeserializer
import ru.flocator.data.models.address.AddressResponse
import ru.flocator.feature_main.api.dependencies.MainDependencies
import ru.flocator.feature_main.internal.data.data_source.MainDataSource
import ru.flocator.feature_main.internal.data.data_source.GeocoderDataSource
import ru.flocator.feature_main.internal.core.di.annotations.FragmentScope
import ru.flocator.feature_main.internal.core.di.annotations.Geocoder
import ru.flocator.feature_main.internal.ui.view_models.AddMarkFragmentViewModel
import ru.flocator.feature_main.internal.ui.view_models.MainFragmentViewModel
import ru.flocator.feature_main.internal.ui.view_models.MarkFragmentViewModel
import ru.flocator.feature_main.internal.ui.view_models.MarksListFragmentViewModel

@Module
internal abstract class MainModule {

    companion object {
        @Provides
        @FragmentScope
        fun provideMainDataSource(dependencies: MainDependencies): MainDataSource =
            dependencies.retrofit.create()

        @Provides
        @FragmentScope
        fun provideGeocoderGson(): Gson =
            GsonBuilder()
                .registerTypeAdapter(
                    AddressResponse::class.java,
                    AddressDeserializer()
                )
                .setLenient()
                .create()

        @Provides
        @FragmentScope
        @Geocoder
        fun provideGeocoderRetrofit(gson: Gson): Retrofit =
            Retrofit.Builder()
                .baseUrl(Constants.GEOCODER_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        @Provides
        @FragmentScope
        fun provideGeocoderDataSource(@Geocoder retrofit: Retrofit): GeocoderDataSource =
            retrofit.create()

        @Provides
        @FragmentScope
        fun provideViewModelFactory(map: ViewModelsMap): ViewModelProvider.Factory =
            ViewModelFactory(map)
    }

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(AddMarkFragmentViewModel::class)
    abstract fun bindAddMarkFragmentViewModel(impl: AddMarkFragmentViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(MainFragmentViewModel::class)
    abstract fun bindMainFragmentViewModel(impl: MainFragmentViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(MarkFragmentViewModel::class)
    abstract fun bindMarkFragmentViewModel(impl: MarkFragmentViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(MarksListFragmentViewModel::class)
    abstract fun bindMarksListFragmentViewModel(impl: MarksListFragmentViewModel): ViewModel
}