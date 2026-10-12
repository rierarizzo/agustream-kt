package com.misgus.agustream.ui.support

import com.misgus.agustream.core.model.Addon
import com.misgus.agustream.core.model.LibraryItem
import com.misgus.agustream.core.model.MetaDetail
import com.misgus.agustream.core.model.MetaPreview
import com.misgus.agustream.core.model.WatchProgress
import com.misgus.agustream.core.model.WatchedEntry
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.AccountState
import com.misgus.agustream.core.repository.AddonRepository
import com.misgus.agustream.core.repository.CatalogRef
import com.misgus.agustream.core.repository.CatalogRepository
import com.misgus.agustream.core.repository.LibraryRepository
import com.misgus.agustream.core.repository.MetadataRepository
import com.misgus.agustream.core.repository.ProgressRepository
import com.misgus.agustream.core.repository.StreamGroup
import com.misgus.agustream.core.repository.StreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [AccountRepository] for tests. */
class FakeAccountRepository(
    initial: AccountState = AccountState(isSignedIn = true),
) : AccountRepository {
    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<AccountState> = _state

    override suspend fun signIn(
        email: String,
        password: String,
    ) {
        _state.value = AccountState(isSignedIn = true, email = email)
    }

    override suspend fun restoreSession(): Boolean = _state.value.isSignedIn

    override suspend fun signOut() {
        _state.value = AccountState(isSignedIn = false)
    }

    override suspend fun loadProfiles() = Unit

    override suspend fun selectProfile(profile: com.misgus.agustream.core.model.BackendProfile) {
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
) : CatalogRepository {
    override suspend fun catalogs(): List<CatalogRef> = refs

    override suspend fun items(
        ref: CatalogRef,
        extra: Map<String, String>?,
    ): List<MetaPreview> = items[ref.key].orEmpty()
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
