package ru.flocator.feature_community.api.dependencies

import retrofit2.Retrofit
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.data.data_store.info.UserInfoMediator
import ru.flocator.data.database.dao.UserDao

interface CommunityDependencies : Dependencies {
    val userDao: UserDao
    val userInfoMediator: UserInfoMediator
    val retrofit: Retrofit
}