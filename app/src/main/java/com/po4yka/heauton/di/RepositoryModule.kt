package com.po4yka.heauton.di

import com.po4yka.heauton.data.repository.ExerciseRepositoryImpl
import com.po4yka.heauton.data.repository.JournalRepositoryImpl
import com.po4yka.heauton.data.repository.ProgressRepositoryImpl
import com.po4yka.heauton.data.repository.QuotesRepositoryImpl
import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.domain.repository.ProgressRepository
import com.po4yka.heauton.domain.repository.QuotesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the QuotesRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindQuotesRepository(
        impl: QuotesRepositoryImpl
    ): QuotesRepository

    /**
     * Binds the JournalRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindJournalRepository(
        impl: JournalRepositoryImpl
    ): JournalRepository

    /**
     * Binds the ExerciseRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        impl: ExerciseRepositoryImpl
    ): ExerciseRepository

    /**
     * Binds the ProgressRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): ProgressRepository
}
