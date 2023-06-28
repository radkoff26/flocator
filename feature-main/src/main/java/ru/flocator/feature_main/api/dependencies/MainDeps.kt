package ru.flocator.feature_main.api.dependencies

import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_controller.NavController
import ru.flocator.core_dependency.Dependencies

interface MainDeps: Dependencies {
    val connectionLiveData: ConnectionLiveData
    val navController: NavController
}
