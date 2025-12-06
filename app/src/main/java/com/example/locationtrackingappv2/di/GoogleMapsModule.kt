package com.example.locationtrackingappv2.di

import android.content.Context
import com.example.locationtrackingappv2.BuildConfig
import com.example.locationtrackingappv2.data.repository.GoogleMapsRepositoryImpl
import com.example.locationtrackingappv2.domain.repository.GoogleMapsRepository
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleMapsModule {

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        // initialize with BuildConfig.MAPS_API_KEY (or other secure storage)
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }

    @Provides
    @Singleton
    fun provideGoogleMapsRepository(impl: GoogleMapsRepositoryImpl): GoogleMapsRepository = impl
}
