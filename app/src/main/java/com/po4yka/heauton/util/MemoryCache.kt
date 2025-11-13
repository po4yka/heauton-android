package com.po4yka.heauton.util

import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cache for frequently accessed data.
 *
 * Uses LRU eviction policy to manage memory usage.
 * Useful for caching quotes, journal entries, and other domain objects.
 */
@Singleton
class MemoryCache @Inject constructor() {

    companion object {
        private const val MAX_CACHE_SIZE = 50 // Maximum number of items per cache
    }

    // Separate caches for different data types
    private val quoteCache = LruCache<String, Any>(MAX_CACHE_SIZE)
    private val journalCache = LruCache<String, Any>(MAX_CACHE_SIZE)
    private val exerciseCache = LruCache<String, Any>(MAX_CACHE_SIZE)
    private val achievementCache = LruCache<String, Any>(MAX_CACHE_SIZE)
    private val scheduleCache = LruCache<String, Any>(MAX_CACHE_SIZE)

    /**
     * Cache types.
     */
    enum class CacheType {
        QUOTE,
        JOURNAL,
        EXERCISE,
        ACHIEVEMENT,
        SCHEDULE
    }

    /**
     * Puts an item in the cache.
     *
     * @param type Cache type
     * @param key Unique key for the item
     * @param value Value to cache
     */
    fun <T> put(type: CacheType, key: String, value: T) {
        getCache(type).put(key, value as Any)
    }

    /**
     * Gets an item from the cache.
     *
     * @param type Cache type
     * @param key Unique key for the item
     * @return Cached value or null if not found
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(type: CacheType, key: String): T? {
        return getCache(type).get(key) as? T
    }

    /**
     * Removes an item from the cache.
     *
     * @param type Cache type
     * @param key Unique key for the item
     */
    fun remove(type: CacheType, key: String) {
        getCache(type).remove(key)
    }

    /**
     * Clears a specific cache.
     *
     * @param type Cache type to clear
     */
    fun clear(type: CacheType) {
        getCache(type).evictAll()
    }

    /**
     * Clears all caches.
     */
    fun clearAll() {
        quoteCache.evictAll()
        journalCache.evictAll()
        exerciseCache.evictAll()
        achievementCache.evictAll()
        scheduleCache.evictAll()
    }

    /**
     * Gets the size of a cache.
     *
     * @param type Cache type
     * @return Number of items in the cache
     */
    fun size(type: CacheType): Int {
        return getCache(type).size()
    }

    /**
     * Checks if a key exists in the cache.
     *
     * @param type Cache type
     * @param key Unique key for the item
     * @return True if the key exists
     */
    fun contains(type: CacheType, key: String): Boolean {
        return getCache(type).get(key) != null
    }

    /**
     * Gets cache statistics.
     *
     * @param type Cache type
     * @return CacheStats with hit/miss information
     */
    fun getStats(type: CacheType): CacheStats {
        val cache = getCache(type)
        return CacheStats(
            type = type,
            size = cache.size(),
            maxSize = cache.maxSize(),
            hitCount = cache.hitCount(),
            missCount = cache.missCount(),
            putCount = cache.putCount(),
            evictionCount = cache.evictionCount()
        )
    }

    /**
     * Gets all cache statistics.
     *
     * @return List of CacheStats for all caches
     */
    fun getAllStats(): List<CacheStats> {
        return CacheType.values().map { getStats(it) }
    }

    /**
     * Gets the LruCache for a given type.
     */
    private fun getCache(type: CacheType): LruCache<String, Any> {
        return when (type) {
            CacheType.QUOTE -> quoteCache
            CacheType.JOURNAL -> journalCache
            CacheType.EXERCISE -> exerciseCache
            CacheType.ACHIEVEMENT -> achievementCache
            CacheType.SCHEDULE -> scheduleCache
        }
    }
}

/**
 * Cache statistics.
 */
data class CacheStats(
    val type: MemoryCache.CacheType,
    val size: Int,
    val maxSize: Int,
    val hitCount: Int,
    val missCount: Int,
    val putCount: Int,
    val evictionCount: Int
) {
    val hitRate: Float
        get() = if (hitCount + missCount > 0) {
            hitCount.toFloat() / (hitCount + missCount)
        } else {
            0f
        }

    override fun toString(): String {
        return "${type.name}: size=$size/$maxSize, " +
                "hits=$hitCount, misses=$missCount, " +
                "hitRate=${String.format(java.util.Locale.getDefault(), "%.2f%%", hitRate * 100)}, " +
                "puts=$putCount, evictions=$evictionCount"
    }
}
