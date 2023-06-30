package ru.flocator.feature_main.internal.di

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Component
import ru.flocator.feature_main.api.ui.MainFragment
import ru.flocator.feature_main.internal.ui.AddMarkFragment
import ru.flocator.feature_main.internal.ui.MarkFragment
import ru.flocator.feature_main.internal.ui.MarksListFragment

@Component(
    modules = [
        MainModule::class
    ],
    dependencies = [
        Fragment::class
    ]
)
internal interface MainComponent {

    @Component.Builder
    abstract class Builder {
        @BindsInstance
        abstract fun fragment(fragment: Fragment): Builder
        abstract fun build(): MainComponent
    }

    fun inject(addMarkFragment: AddMarkFragment)
    fun inject(mainFragment: MainFragment)
    fun inject(markFragment: MarkFragment)
    fun inject(marksListFragment: MarksListFragment)
}
