package com.po4yka.heauton.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier annotation for Application-level CoroutineScope.
 * This scope lives for the entire lifetime of the application.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/**
 * Qualifier annotation for IO-bound CoroutineScope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoScope

/**
 * Hilt module for providing properly managed CoroutineScopes.
 *
 * These scopes are tied to the application lifecycle and are properly
 * supervised to prevent memory leaks.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopesModule {

    /**
     * Provides an application-level CoroutineScope.
     *
     * This scope uses:
     * - SupervisorJob: Child coroutine failures don't cancel the parent
     * - Dispatchers.Default: General-purpose dispatcher for CPU-intensive work
     *
     * Use this for long-lived operations that should survive individual component lifecycles.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /**
     * Provides an IO-bound CoroutineScope.
     *
     * This scope uses:
     * - SupervisorJob: Child coroutine failures don't cancel the parent
     * - Dispatchers.IO: Optimized for IO operations (network, disk, etc.)
     *
     * Use this for IO-intensive operations.
     */
    @Provides
    @Singleton
    @IoScope
    fun provideIoScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
