package com.po4yka.heauton.data.local.search.appsearch

import android.content.Context
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.RemoveByDocumentIdRequest
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import androidx.appsearch.platformstorage.PlatformStorage
import com.google.common.util.concurrent.ListenableFuture
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.Quote
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val SEARCH_DB_NAME = "heauton_appsearch"

/**
 * Centralizes AppSearch session management and indexing for the app.
 */
@Singleton
class AppSearchManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val mutex = Mutex()
    private var session: AppSearchSession? = null

    suspend fun initialize() {
        ensureSession()
    }

    suspend fun indexJournalEntry(entry: JournalEntry) {
        if (entry.isEncrypted) return

        val currentSession = ensureSession()
        val document = JournalEntryDocument.from(entry)
        val request = PutDocumentsRequest.Builder()
            .addDocuments(document)
            .build()

        runCatching { currentSession.putAsync(request).await() }
    }

    suspend fun indexQuote(quote: Quote) {
        val currentSession = ensureSession()
        val document = QuoteDocument.from(quote)
        val request = PutDocumentsRequest.Builder()
            .addDocuments(document)
            .build()

        runCatching { currentSession.putAsync(request).await() }
    }

    suspend fun removeJournalEntry(entryId: String) {
        runCatching {
            val currentSession = ensureSession()
            val request = RemoveByDocumentIdRequest.Builder(JournalEntryDocument.NAMESPACE)
                .addIds(entryId)
                .build()
            currentSession.removeAsync(request).await()
        }
    }

    suspend fun removeQuote(quoteId: String) {
        runCatching {
            val currentSession = ensureSession()
            val request = RemoveByDocumentIdRequest.Builder(QuoteDocument.NAMESPACE)
                .addIds(quoteId)
                .build()
            currentSession.removeAsync(request).await()
        }
    }

    suspend fun searchJournalEntries(query: String): List<AppSearchMatch<JournalEntry>> {
        if (query.isBlank()) return emptyList()

        val currentSession = ensureSession()
        val spec = SearchSpec.Builder()
            .setTermMatch(SearchSpec.TERM_MATCH_PREFIX)
            .setRankingStrategy(SearchSpec.RANKING_STRATEGY_DOCUMENT_SCORE)
            .setResultCountPerPage(30)
            .setFilterNamespaces(listOf(JournalEntryDocument.NAMESPACE))
            .build()

        val searchResults = currentSession.search(query, spec)
        val page = runCatching { searchResults.nextPageAsync().await() }.getOrDefault(emptyList())
        return page.mapNotNull { result ->
            result.getDocument(JournalEntryDocument::class.java)?.let { document ->
                AppSearchMatch(
                    id = document.id,
                    data = document.toDomain(),
                    score = result.score.toDouble()
                )
            }
        }
    }

    suspend fun searchQuotes(query: String): List<AppSearchMatch<Quote>> {
        if (query.isBlank()) return emptyList()

        val currentSession = ensureSession()
        val spec = SearchSpec.Builder()
            .setTermMatch(SearchSpec.TERM_MATCH_PREFIX)
            .setRankingStrategy(SearchSpec.RANKING_STRATEGY_DOCUMENT_SCORE)
            .setResultCountPerPage(30)
            .setFilterNamespaces(listOf(QuoteDocument.NAMESPACE))
            .build()

        val searchResults = currentSession.search(query, spec)
        val page = runCatching { searchResults.nextPageAsync().await() }.getOrDefault(emptyList())
        return page.mapNotNull { result ->
            result.getDocument(QuoteDocument::class.java)?.let { document ->
                AppSearchMatch(
                    id = document.id,
                    data = document.toDomain(),
                    score = result.score.toDouble()
                )
            }
        }
    }

    private suspend fun ensureSession(): AppSearchSession {
        session?.let { return it }

        return mutex.withLock {
            session?.let { return@withLock it }

            val createdSession = createSession()
            val setSchemaRequest = SetSchemaRequest.Builder()
                .addDocumentClasses(JournalEntryDocument::class.java, QuoteDocument::class.java)
                .build()
            createdSession.setSchemaAsync(setSchemaRequest).await()
            session = createdSession
            createdSession
        }
    }

    private suspend fun createSession(): AppSearchSession {
        return withContext(Dispatchers.IO) {
            runCatching {
                PlatformStorage.createSearchSessionAsync(
                    PlatformStorage.SearchContext.Builder(context, SEARCH_DB_NAME).build()
                ).await()
            }.getOrElse {
                LocalStorage.createSearchSessionAsync(
                    LocalStorage.SearchContext.Builder(context, SEARCH_DB_NAME).build()
                ).await()
            }
        }
    }
}

/**
 * Lightweight wrapper for AppSearch results to pass scores along with domain data.
 */
data class AppSearchMatch<T>(
    val id: String,
    val data: T,
    val score: Double
)

private suspend fun <T> ListenableFuture<T>.await(): T = suspendCancellableCoroutine { cont ->
    this.addListener({
        try {
            cont.resume(this.get())
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }, Runnable::run)

    cont.invokeOnCancellation { this.cancel(true) }
}
