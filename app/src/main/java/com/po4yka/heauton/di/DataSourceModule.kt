package com.po4yka.heauton.di

import com.po4yka.heauton.data.source.QuotesDataSource
import com.po4yka.heauton.data.source.RoomQuotesDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing data source implementations.
 *
 * Following Dependency Inversion Principle:
 * - Repositories depend on abstractions (DataSource interfaces)
 * - This module binds concrete implementations
 * - Easy to swap implementations (Room → Network, Firestore, etc.)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    /**
     * Binds the QuotesDataSource implementation.
     *
     * Currently uses Room, but can be easily swapped to:
     * - NetworkQuotesDataSource for remote data
     * - FirestoreQuotesDataSource for Firebase
     * - CompositeQuotesDataSource for offline-first strategy
     */
    @Binds
    @Singleton
    abstract fun bindQuotesDataSource(
        impl: RoomQuotesDataSource
    ): QuotesDataSource
}
