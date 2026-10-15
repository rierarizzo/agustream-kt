package com.misgus.pengu.ui.support

import com.misgus.pengu.core.model.Addon
import com.misgus.pengu.core.model.BackendProfile
import com.misgus.pengu.core.model.LibraryItem
import com.misgus.pengu.core.model.MetaDetail
import com.misgus.pengu.core.model.MetaPreview
import com.misgus.pengu.core.model.WatchProgress
import com.misgus.pengu.core.model.WatchedEntry
import com.misgus.pengu.core.repository.AccountRepository
import com.misgus.pengu.core.repository.AccountState
import com.misgus.pengu.core.repository.AddonRepository
import com.misgus.pengu.core.repository.CatalogRef
import com.misgus.pengu.core.repository.CatalogRepository
import com.misgus.pengu.core.repository.LibraryRepository
import com.misgus.pengu.core.repository.MetadataRepository
import com.misgus.pengu.core.repository.ProgressRepository
import com.misgus.pengu.core.repository.SignInMethod
import com.misgus.pengu.core.repository.SignInRequest
import com.misgus.pengu.core.repository.StreamGroup
import com.misgus.pengu.core.repository.StreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [AccountRepository] for tests. */
class FakeAccountRepository(
    initial: AccountState = AccountState(isSignedIn = true, requiresProfile = false),
) : AccountRepository {
    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<AccountState> = _state

    override val signInMethods: List<SignInMethod> = listOf(SignInMethod.EmailPassword)

    override suspend fun signIn(request: SignInRequest) {
        val email =
            when (request) {
                is SignInRequest.EmailPassword -> request.email
            }
        _state.value = AccountState(isSignedIn = true, email = email)
    }

    override suspend fun restoreSession(): Boolean = _state.value.isSignedIn

    override suspend fun signOut() {
        _state.value = AccountState(isSignedIn = false)
    }

    override suspend fun loadProfiles() = Unit

    override suspend fun selectProfile(profile: BackendProfile) {
        _state.value = _state.value.copy(activeProfile = profile)
    }
}

/** In-memory [LibraryRepository] for tests. */
class FakeLibraryRepository(
    initial: List<LibraryItem> = emptyList(),
) : LibraryRepository {
    private val items = initial.toMutableList()
    private val _changes = MutableStateFlow(0L)
    override val changes: StateFlow<Long> = _changes

    override suspend fun all(): List<LibraryItem> = items.toList()

    override suspend fun contains(contentId: String): Boolean = items.any { it.contentId == contentId }

    override suspend fun add(item: LibraryItem) {
        if (contains(item.contentId)) return
        items.add(item)
        _changes.value += 1
    }

    override suspend fun remove(contentId: String) {
        items.removeAll { it.contentId == contentId }
        _changes.value += 1
    }
}

/** In-memory [ProgressRepository] for tests. */
class FakeProgressRepository(
    private val entries: List<WatchedEntry> = emptyList(),
    private val progress: List<WatchProgress> = emptyList(),
) : ProgressRepository {
    private val _changes = MutableStateFlow(0L)
    override val changes: StateFlow<Long> = _changes

    override suspend fun all(): List<WatchProgress> = progress

    override suspend fun save(progress: WatchProgress) = Unit

    override suspend fun watchedEntries(candidateIds: Collection<String>): List<WatchedEntry> =
        entries.filter { it.contentId in candidateIds }

    override suspend fun markWatched(
        contentId: String,
        contentType: String,
        season: Int?,
        episode: Int?,
    ) = Unit

    override suspend fun unmarkWatched(
        contentId: String,
        season: Int?,
        episode: Int?,
    ) = Unit
}

/** In-memory [MetadataRepository] for tests. */
class FakeMetadataRepository(
    private val detail: MetaDetail? = null,
    private val details: Map<String, MetaDetail> = emptyMap(),
) : MetadataRepository {
    override suspend fun detail(
        type: String,
        id: String,
    ): MetaDetail? = details[id] ?: detail

    override suspend fun similar(
        type: String,
        id: String,
        genre: String?,
    ): List<MetaPreview> = emptyList()
}

/** In-memory [CatalogRepository] for tests. */
class FakeCatalogRepository(
    private val refs: List<CatalogRef> = emptyList(),
    private val items: Map<String, List<MetaPreview>> = emptyMap(),
    private val detailed: Map<String, List<MetaDetail>> = emptyMap(),
) : CatalogRepository {
    override suspend fun catalogs(): List<CatalogRef> = refs

    override suspend fun items(
        ref: CatalogRef,
        extra: Map<String, String>?,
    ): List<MetaPreview> = items[ref.key].orEmpty()

    override suspend fun detailedItems(
        ref: CatalogRef,
        extra: Map<String, String>?,
    ): List<MetaDetail> = detailed[ref.key].orEmpty()
}

/** In-memory [StreamRepository] for tests. */
class FakeStreamRepository(
    private val groups: List<StreamGroup> = emptyList(),
) : StreamRepository {
    override suspend fun all(
        type: String,
        id: String,
    ): List<StreamGroup> = groups
}

/** In-memory [AddonRepository] for tests. */
class FakeAddonRepository(
    private val addons: List<Addon> = emptyList(),
) : AddonRepository {
    override suspend fun all(): List<Addon> = addons
}
