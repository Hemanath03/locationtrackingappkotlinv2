package com.example.locationtrackingappv2.di

import com.example.locationtrackingappv2.data.local.RouteStoreImpl
import com.example.locationtrackingappv2.data.repository.DirectionsRepositoryImpl
import com.example.locationtrackingappv2.data.repository.GoogleMapsRepositoryImpl
import com.example.locationtrackingappv2.data.repository.ITollRepositoryImpl
import com.example.locationtrackingappv2.domain.repository.DirectionsRepository
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import com.example.locationtrackingappv2.domain.repository.IRouteStore
import com.example.locationtrackingappv2.domain.repository.ITollRepository
import com.example.locationtrackingappv2.domain.util.INotificationHelper
import com.example.locationtrackingappv2.domain.util.NotificationHelperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindGoogleMapsRepository(
        impl: GoogleMapsRepositoryImpl
    ): GoogleMapsRepository

    @Binds
    abstract fun bindDirectionsRepo(
        impl: DirectionsRepositoryImpl
    ): DirectionsRepository

    @Binds
    abstract fun bindTollRepo(
        impl: ITollRepositoryImpl
    ): ITollRepository

    @Binds
    abstract fun bindRouteStore(impl: RouteStoreImpl): IRouteStore

    @Binds
    abstract fun bindNotificationHelper(impl: NotificationHelperImpl): INotificationHelper
}
