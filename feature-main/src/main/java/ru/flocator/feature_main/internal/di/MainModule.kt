package ru.flocator.feature_main.internal.di

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import retrofit2.create
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.feature_main.api.dependencies.MainDependencies
import ru.flocator.feature_main.internal.data_source.ClientAPI
import ru.flocator.feature_main.internal.di.annotations.FragmentScope

@Module
internal object MainModule {

    @Provides
    @FragmentScope
    fun provideNavController(fragment: Fragment): NavController =
        fragment.findNavController()

    @Provides
    @FragmentScope
    fun provideMainDependencies(fragment: Fragment): MainDependencies =
        fragment.findDependencies()

    @Provides
    @FragmentScope
    fun provideClientAPI(dependencies: MainDependencies): ClientAPI =
        dependencies.retrofit.create()

    @Provides
    @FragmentScope
    fun provideAppRepository(dependencies: MainDependencies): AppRepository =
        dependencies.appRepository
}