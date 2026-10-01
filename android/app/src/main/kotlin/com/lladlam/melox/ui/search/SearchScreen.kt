package com.lladlam.melox.ui.search

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.lladlam.melox.R
import com.lladlam.melox.core.account.NeteaseSessionStore
import com.lladlam.melox.core.download.MeloXProviderDownloadStore
import com.lladlam.melox.core.library.NeteaseLibraryClient
import com.lladlam.melox.core.library.NeteasePlaylistSummary
import com.lladlam.melox.core.model.SearchSong
import com.lladlam.melox.core.music.model.MusicAlbumSummary
import com.lladlam.melox.core.music.model.MusicArtistSummary
import com.lladlam.melox.core.music.model.MusicPlaylistSummary
import com.lladlam.melox.core.music.model.MusicSource
import com.lladlam.melox.core.music.model.MusicTrack
import com.lladlam.melox.core.music.provider.AlbumCapability
import com.lladlam.melox.core.music.provider.ArtistCapability
import com.lladlam.melox.core.music.provider.CatalogSearchCapability
import com.lladlam.melox.core.music.provider.HomeFeedCapability
import com.lladlam.melox.core.music.provider.MeloXMusicProviders
import com.lladlam.melox.core.music.provider.loadAllPlaylistTracks
import com.lladlam.melox.core.music.provider.MusicProviderSelectionStore
import com.lladlam.melox.core.music.provider.PlaylistCapability
import com.lladlam.melox.core.music.provider.SearchCapability
import com.lladlam.melox.core.music.provider.UnifiedMusicService
import com.lladlam.melox.core.network.MeloXSearchKind
import com.lladlam.melox.core.network.MeloXSearchMediaItem
import com.lladlam.melox.core.network.NeteaseSearchClient
import com.lladlam.melox.core.network.NeteaseMusicOperationsClient
import com.lladlam.melox.core.network.NeteaseUniversalSearchClient
import com.lladlam.melox.playback.PlaybackCommands
import com.lladlam.melox.playback.ProviderPlaybackCommands
import com.lladlam.melox.ui.MeloXBottomContentClearance
import com.lladlam.melox.ui.account.MeloXAccountActivity
import com.lladlam.melox.ui.collection.MeloXCollectionDetailActivity
import com.lladlam.melox.ui.glass.MeloXGlassButton
import com.lladlam.melox.ui.glass.MeloXGlassButtonStyle
import com.lladlam.melox.ui.glass.MeloXGlassTextField
import com.lladlam.melox.ui.glass.MeloXShapes
import com.lladlam.melox.ui.glass.MeloXTypography
import com.lladlam.melox.ui.glass.meloXContentSurface
import com.lladlam.melox.ui.glass.MeloXIosTopBar
import com.lladlam.melox.ui.glass.MeloXActionIcon
import com.lladlam.melox.ui.glass.MeloXSymbol
import com.lladlam.melox.ui.glass.MeloXSearchBackMorphIcon
import com.lladlam.melox.ui.glass.MeloXSymbolIcon
import com.lladlam.melox.ui.glass.MeloXSystemColors
import com.lladlam.melox.ui.glass.MeloXSwipeAction
import com.lladlam.melox.ui.glass.MeloXSwipeActionRow
import com.lladlam.melox.ui.podcast.MeloXPodcastScreen
import com.lladlam.melox.ui.library.MeloXUnifiedPlaylistDetailScreen
import com.lladlam.melox.ui.library.MeloXUnifiedProviderAlbumDetailScreen
import com.lladlam.melox.core.music.provider.MeloXLegacyUiBridge
import com.lladlam.melox.ui.settings.MeloXSettingsRuntime
import com.lladlam.melox.ui.settings.MeloXSwipeFullAction
import com.lladlam.melox.ui.player.MeloXSongActionsOverlay
import com.lladlam.melox.ui.animation.MeloXMotion
import com.lladlam.melox.ui.animation.meloXPageEnter
import com.lladlam.melox.ui.animation.meloXPageExit
import com.lladlam.melox.ui.layout.rememberMeloXWindowInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SearchBackAction { ClearOverlay, ClearQuery, SwitchToHome }

data class MeloXSearchLaunch(val query: String, val kind: MeloXSearchKind, val nonce: Long = System.nanoTime())
object MeloXSearchLaunchBus {
    var request by mutableStateOf<MeloXSearchLaunch?>(null)
        private set
    fun post(query: String, kind: MeloXSearchKind) { request = MeloXSearchLaunch(query, kind) }
    fun consume(request: MeloXSearchLaunch) { if (this.request == request) this.request = null }
}

private val SearchAccent = MeloXSystemColors.Blue
// 详情覆盖层过渡的收尾余量：动画时长之外再留一点，等 sharedElement 的 overlay
// 层真正收干净，再解锁并放开卡片。给少了会在切换瞬间看到 overlay 残影。
private const val OverlayTransitionSettleMillis = 60L
private val SearchCategories = listOf("排行榜", "播客", "华语", "欧美", "日语", "韩语", "粤语", "流行", "摇滚", "民谣", "电子", "说唱", "R&B/Soul", "古典", "ACG", "影视原声", "学习", "工作", "放松", "夜晚")

/** Display label for a browse-category id. The id itself stays Chinese. */
@Composable
private fun searchCategoryLabel(id: String): String = when (id) {
    "排行榜" -> stringResource(R.string.search_cat_charts)
    "播客" -> stringResource(R.string.search_cat_podcasts)
    "华语" -> stringResource(R.string.search_cat_chinese)
    "欧美" -> stringResource(R.string.search_cat_western)
    "日语" -> stringResource(R.string.search_cat_japanese)
    "韩语" -> stringResource(R.string.search_cat_korean)
    "粤语" -> stringResource(R.string.search_cat_cantonese)
    "流行" -> stringResource(R.string.search_cat_pop)
    "摇滚" -> stringResource(R.string.search_cat_rock)
    "民谣" -> stringResource(R.string.search_cat_folk)
    "电子" -> stringResource(R.string.search_cat_electronic)
    "说唱" -> stringResource(R.string.search_cat_hiphop)
    "R&B/Soul" -> stringResource(R.string.search_cat_rnb)
    "古典" -> stringResource(R.string.search_cat_classical)
    "ACG" -> stringResource(R.string.search_cat_acg)
    "影视原声" -> stringResource(R.string.search_cat_soundtrack)
    "学习" -> stringResource(R.string.search_cat_study)
    "工作" -> stringResource(R.string.search_cat_work)
    "放松" -> stringResource(R.string.search_cat_relax)
    "夜晚" -> stringResource(R.string.search_cat_night)
    else -> id
}

private sealed interface ProviderSearchDestination {
    val source: MusicSource
    val key: String
    val kind: MeloXSearchKind
    val title: String
    val subtitle: String
    val artworkUrl: String?

    data class Playlist(val value: MusicPlaylistSummary) : ProviderSearchDestination {
        override val source = value.id.source
        override val key = "playlist:${source.storageValue}:${value.id.value}"
        override val kind = MeloXSearchKind.Playlists
        override val title = value.title
        override val subtitle = value.creatorName.orEmpty()
        override val artworkUrl = value.artworkUrl
    }

    data class Album(val value: MusicAlbumSummary) : ProviderSearchDestination {
        override val source = value.id.source
        override val key = "album:${source.storageValue}:${value.id.value}"
        override val kind = MeloXSearchKind.Albums
        override val title = value.title
        override val subtitle = value.artists.joinToString(" / ") { it.name }
        override val artworkUrl = value.artworkUrl
    }

    data class Artist(val value: MusicArtistSummary) : ProviderSearchDestination {
        override val source = value.id.source
        override val key = "artist:${source.storageValue}:${value.id.value}"
        override val kind = MeloXSearchKind.Artists
        override val title = value.name
        override val subtitle = ""
        override val artworkUrl = value.artworkUrl
    }
}

private sealed interface SearchDetailDestination {
    val key: String
    val kind: MeloXSearchKind
    val title: String
    val subtitle: String
    val artworkUrl: String?

    data class Netease(val value: MeloXSearchMediaItem) : SearchDetailDestination {
        override val key = "netease:${value.kind}:${value.id}"
        override val kind = value.kind
        override val title = value.title
        override val subtitle = value.subtitle
        override val artworkUrl = value.artworkUrl
    }

    data class Provider(val value: ProviderSearchDestination) : SearchDetailDestination {
        override val key = value.key
        override val kind = value.kind
        override val title = value.title
        override val subtitle = value.subtitle
        override val artworkUrl = value.artworkUrl
    }
}

/**
 * 有没有可 morph 的 hero 封面 —— 决定这个目的地走「带一镜到底的详情覆盖层」还是
 * 「整页子页面」。
 *
 * 网易云专辑/歌手/播客根本不会走到这里（它们在 `SearchMediaResults` 里直接
 * `MeloXCollectionDetailActivity.launch`，是另一个 Activity）；provider 歌手详情
 * 只有标题 + 歌曲列表、没有 hero，所以归到整页子页面。
 */
private fun SearchDetailDestination.hasHeroOverlay(): Boolean = when (this) {
    is SearchDetailDestination.Netease -> value.kind == MeloXSearchKind.Playlists
    is SearchDetailDestination.Provider ->
        value is ProviderSearchDestination.Playlist || value is ProviderSearchDestination.Album
}

/**
 * 搜索页的「整页子页面」：从搜索主页推进去、返回时退回。它们没有可 morph 的 hero，
 * 走整页推入（`meloXPageEnter`/`meloXPageExit` + 自绘不透明背板），不用共享元素。
 *
 * ⚠ 这里的 `title`/`destination` 是**内容**，打开时写入、返回时不清 —— 可见性由
 * `subPageOpen` 实时推导。两边共用一个 state 的话，返回瞬间内容会先变空，子页面
 * 直接消失、退场动画什么都画不出来（和详情覆盖层同一个坑）。
 */
private sealed interface SearchSubPage {
    data object Podcast : SearchSubPage

    data class Category(val title: String) : SearchSubPage

    data class Detail(val destination: SearchDetailDestination) : SearchSubPage
}

@Composable
fun SearchScreen(
    source: MusicSource = MusicSource.Netease,
    backClearSignal: MutableState<SearchBackAction> = remember { mutableStateOf(SearchBackAction.SwitchToHome) },
    onSearchBackState: (SearchBackAction) -> Unit = {},
    onSearchExit: () -> Unit = {},
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val songClient = remember(appContext) { NeteaseSearchClient(cookieProvider = { NeteaseSessionStore.readCookie(appContext) }) }
    val universal = remember(appContext) { NeteaseUniversalSearchClient(cookieProvider = { NeteaseSessionStore.readCookie(appContext) }) }
    val library = remember(appContext) { NeteaseLibraryClient({ NeteaseSessionStore.readCookie(appContext) }) }
    val operations = remember(appContext) { NeteaseMusicOperationsClient(cookieProvider = { NeteaseSessionStore.readCookie(appContext) }) }
    val providerRegistry = remember(appContext) { MeloXMusicProviders.create(appContext) }
    val currentProvider = remember(source, providerRegistry) { providerRegistry.require(source) }
    val providerSongSearch = currentProvider as? SearchCapability
    val providerCatalog = currentProvider as? CatalogSearchCapability
    val providerHome = currentProvider as? HomeFeedCapability
    val unifiedService = remember(providerRegistry) { UnifiedMusicService(providerRegistry) }
    val unifiedEnabled = MusicProviderSelectionStore.unifiedEnabled(appContext)
    val unifiedSources = MusicProviderSelectionStore.unifiedSources(appContext)

    val availableKinds = remember(source, providerCatalog) {
        if (source == MusicSource.Netease) {
            MeloXSearchKind.entries.filter { it != MeloXSearchKind.Podcasts || MeloXSettingsRuntime.podcastsEnabled }
        } else {
            buildList {
                add(MeloXSearchKind.Songs)
                if (providerCatalog != null) {
                    add(MeloXSearchKind.Playlists)
                    add(MeloXSearchKind.Albums)
                    add(MeloXSearchKind.Artists)
                }
            }
        }
    }

    var query by rememberSaveable(source.name) { mutableStateOf("") }
    var searchTrigger by rememberSaveable(source.name) { mutableIntStateOf(0) }
    var skipSearchDebounce by rememberSaveable(source.name) { mutableStateOf(false) }
    var kind by rememberSaveable(source.name) { mutableStateOf(MeloXSearchKind.Songs) }
    var songs by remember(source) { mutableStateOf<List<SearchSong>>(emptyList()) }
    var providerSongs by remember(source) { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var providerPlaylists by remember(source) { mutableStateOf<List<MusicPlaylistSummary>>(emptyList()) }
    var providerAlbums by remember(source) { mutableStateOf<List<MusicAlbumSummary>>(emptyList()) }
    var providerArtists by remember(source) { mutableStateOf<List<MusicArtistSummary>>(emptyList()) }
    var providerRecommendations by remember(source) { mutableStateOf<List<MusicPlaylistSummary>>(emptyList()) }
    var unifiedFailures by remember(source) { mutableStateOf<List<UnifiedMusicService.SearchFailure>>(emptyList()) }
    var media by remember(source) { mutableStateOf<List<MeloXSearchMediaItem>>(emptyList()) }
    var recommendations by remember(source) { mutableStateOf<List<NeteasePlaylistSummary>>(emptyList()) }
    var categoryTitle by remember(source) { mutableStateOf<String?>(null) }
    var categoryPlaylists by remember(source) { mutableStateOf<List<NeteasePlaylistSummary>>(emptyList()) }
    var selectedDetail by remember(source) { mutableStateOf<SearchDetailDestination?>(null) }
    // 详情覆盖层的内容与可见性必须拆成两个 state：返回时只关 selectedDetail，
    // overlayDetail 一直保留到退出过渡跑完。否则返回瞬间 let 取到空、详情子树被
    // 立刻拆掉，共享元素封面就只有进入、没有返回。
    var overlayDetail by remember(source) { mutableStateOf<SearchDetailDestination?>(null) }
    // 卡片可见性必须与 selectedDetail 同帧同步：点击即退出、返回即进入，才能与详情
    // hero 的 AnimatedVisibility 完全重叠。原「占用 key」方案把它压后到 LaunchedEffect
    // 里赋值 —— 返回时要等 PageExitMillis + settle(280ms) 才放开，而 hero 的退场只有
    // 220ms，卡片 enter 落在它结束之后，两端永不重叠 → sharedElement 无从配对。
    // 过渡串行化已由 overlayTransitionBusy 排队负责，不再需要占用 key。
    // 详情覆盖层的进入/退出过渡是否仍在进行。并行动画（退出未跑完就点下一张卡）会让
    // 单个 AnimatedVisibility 取消旧过渡、直接跳到新目标，旧卡就被遗弃在半途。过渡期间
    // 直接忽略新的打开请求，把连续切换串行化，代价是一次点击延迟，收益是不会有卡死态。
    var overlayTransitionBusy by remember(source) { mutableStateOf(false) }
    // 整页子页面（分类页 / 播客页 / 没有 hero 的详情页）的**内容**：同样的道理，
    // 打开时写入、返回时不清，可见性由 subPageOpen 实时推导。
    var subPageContent by remember(source) { mutableStateOf<SearchSubPage?>(null) }
    val detailBackProgress = remember { Animatable(0f) }
    var podcastDiscovery by remember(source) { mutableStateOf(false) }
    var loading by remember(source) { mutableStateOf(false) }
    var error by remember(source) { mutableStateOf<String?>(null) }
    var selectedActionSong by remember(source) { mutableStateOf<SearchSong?>(null) }
    val launchRequest = MeloXSearchLaunchBus.request

    LaunchedEffect(source, availableKinds) {
        if (kind !in availableKinds) kind = MeloXSearchKind.Songs
    }

    LaunchedEffect(launchRequest, source) {
        launchRequest?.let { request ->
            query = request.query
            kind = request.kind.takeIf { it in availableKinds } ?: MeloXSearchKind.Songs
            MeloXSearchLaunchBus.consume(request)
        }
    }

    LaunchedEffect(source) {
        if (source == MusicSource.Netease) {
            runCatching { library.explorePlaylists("推荐歌单", 10) }
                .onSuccess { recommendations = it }
        } else {
            runCatching {
                withContext(Dispatchers.IO) {
                    providerHome?.homeFeed(playlistLimit = 10, newSongLimit = 0, rankingLimit = 0)
                }
            }.onSuccess { providerRecommendations = it?.recommendedPlaylists.orEmpty() }
        }
    }

    LaunchedEffect(query, kind, source, unifiedEnabled, unifiedSources, searchTrigger) {
        val keyword = query.trim()
        if (keyword.isBlank()) {
            songs = emptyList()
            providerSongs = emptyList()
            providerPlaylists = emptyList()
            providerAlbums = emptyList()
            providerArtists = emptyList()
            unifiedFailures = emptyList()
            media = emptyList()
            error = null
            loading = false
            return@LaunchedEffect
        }
        if (!skipSearchDebounce) delay(1500)
        skipSearchDebounce = false
        loading = true
        error = null

        val linkedId = if (source == MusicSource.Netease) parseSongLink(keyword) else null
        if (linkedId != null) {
            providerSongs = emptyList()
            unifiedFailures = emptyList()
            runCatching { universal.songDetail(linkedId) }
                .onSuccess {
                    ensureActive()
                    songs = listOfNotNull(it)
                    media = emptyList()
                    kind = MeloXSearchKind.Songs
                }
                .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_link_failed) }
            loading = false
            return@LaunchedEffect
        }

        when (kind) {
            MeloXSearchKind.Songs -> {
                media = emptyList()
                providerPlaylists = emptyList()
                providerAlbums = emptyList()
                providerArtists = emptyList()
                if (unifiedEnabled && unifiedSources.size > 1) {
                    songs = emptyList()
                    runCatching {
                        withContext(Dispatchers.IO) {
                            unifiedService.searchSongs(
                                query = keyword,
                                sources = unifiedSources,
                                page = 1,
                                pageSizePerProvider = 25,
                            )
                        }
                    }.onSuccess { result ->
                        ensureActive()
                        providerSongs = result.aggregated.mapNotNull { it.recommendation?.track }.ifEmpty { result.tracks }
                        unifiedFailures = result.failures
                    }.onFailure { failure ->
                        if (failure is CancellationException) throw failure
                        providerSongs = emptyList()
                        unifiedFailures = emptyList()
                        error = failure.message ?: appContext.getString(R.string.search_failed)
                    }
                } else if (source == MusicSource.Netease) {
                    providerSongs = emptyList()
                    unifiedFailures = emptyList()
                    runCatching { songClient.ensureArtwork(songClient.searchSongs(keyword)) }
                        .onSuccess { ensureActive(); songs = it }
                        .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                } else {
                    songs = emptyList()
                    unifiedFailures = emptyList()
                    val capability = providerSongSearch
                    if (capability == null) {
                        providerSongs = emptyList()
                        error = appContext.getString(R.string.search_songs_unavailable, source.displayName)
                    } else {
                        runCatching {
                            withContext(Dispatchers.IO) { capability.searchSongs(keyword, page = 1, pageSize = 50).items }
                        }.onSuccess { ensureActive(); providerSongs = it }
                            .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                    }
                }
            }

            MeloXSearchKind.Playlists -> {
                songs = emptyList(); providerSongs = emptyList(); media = emptyList(); unifiedFailures = emptyList()
                if (source == MusicSource.Netease) {
                    runCatching { universal.searchMedia(keyword, kind) }
                        .onSuccess { ensureActive(); media = it }
                        .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                } else {
                    val capability = providerCatalog
                    if (capability == null || currentProvider !is PlaylistCapability) error = appContext.getString(R.string.search_playlists_unavailable, source.displayName)
                    else runCatching {
                        withContext(Dispatchers.IO) { capability.searchPlaylists(keyword, page = 1, pageSize = 40).items }
                    }.onSuccess { ensureActive(); providerPlaylists = it }
                        .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                }
            }

            MeloXSearchKind.Albums -> {
                songs = emptyList(); providerSongs = emptyList(); media = emptyList(); unifiedFailures = emptyList()
                if (source == MusicSource.Netease) {
                    runCatching { universal.searchMedia(keyword, kind) }
                        .onSuccess { ensureActive(); media = it }
                        .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                } else {
                    val capability = providerCatalog
                    if (capability == null || currentProvider !is AlbumCapability) error = appContext.getString(R.string.search_albums_unavailable, source.displayName)
                    else runCatching {
                        withContext(Dispatchers.IO) { capability.searchAlbums(keyword, page = 1, pageSize = 40).items }
                    }.onSuccess { ensureActive(); providerAlbums = it }
                        .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                }
            }

            MeloXSearchKind.Artists -> {
                songs = emptyList(); providerSongs = emptyList(); media = emptyList(); unifiedFailures = emptyList()
                if (source == MusicSource.Netease) {
                    runCatching { universal.searchMedia(keyword, kind) }
                        .onSuccess { ensureActive(); media = it }
                        .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                } else {
                    val capability = providerCatalog
                    if (capability == null || currentProvider !is ArtistCapability) error = appContext.getString(R.string.search_artists_unavailable, source.displayName)
                    else runCatching {
                        withContext(Dispatchers.IO) { capability.searchArtists(keyword, page = 1, pageSize = 40).items }
                    }.onSuccess { ensureActive(); providerArtists = it }
                        .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                }
            }

            else -> {
                providerSongs = emptyList(); unifiedFailures = emptyList()
                if (source != MusicSource.Netease) {
                    media = emptyList()
                    error = appContext.getString(R.string.search_kind_unavailable, source.displayName, kind.title)
                } else {
                    runCatching { universal.searchMedia(keyword, kind) }
                        .onSuccess { ensureActive(); media = it; songs = emptyList() }
                        .onFailure { if (it is CancellationException) throw it else error = it.message ?: appContext.getString(R.string.search_failed) }
                }
            }
        }
        loading = false
    }

    // 走「右侧滑入覆盖层」的详情：封面能从搜索结果卡片一镜到底接进详情 hero 的那几类。
    // 网易云专辑/歌手/播客打开的是另一个 Activity（共享元素不能跨 Activity 配对），
    // provider 歌手详情没有 hero 封面 —— 都不在此列，改走下面的整页子页面层。
    val overlayDestination = selectedDetail?.takeIf { it.hasHeroOverlay() }
    // 整页子页面是否展开（实时可见性；内容在 subPageContent 那一侧保留）。
    val subPageOpen = podcastDiscovery ||
        (categoryTitle != null) ||
        (selectedDetail != null && overlayDestination == null)
    // 手势返回走完后位移停在 1（页面已在屏外）。下一次进入必须归零，
    // 否则新页面会带着满位移进来、永远停在屏幕右侧外面。
    LaunchedEffect(overlayDestination) {
        if (overlayDestination != null) detailBackProgress.snapTo(0f)
    }
    // 打开详情时可见性（selectedDetail）与内容（overlayDetail）同时写入。
    // 返回只清 selectedDetail，内容留到退出过渡跑完再自然 dispose。
    // ⚠ 这几个声明必须在下面的过渡生命周期 effect 之前（effect 里要消费待打开的详情）。
    var pendingDetail by remember(source) { mutableStateOf<SearchDetailDestination?>(null) }
    fun applyDetail(destination: SearchDetailDestination) {
        selectedDetail = destination
        overlayDetail = destination
        // 没有 hero 的详情（provider 歌手）落在整页子页面层，内容同样要留到退场结束。
        if (!destination.hasHeroOverlay()) subPageContent = SearchSubPage.Detail(destination)
    }
    fun openDetail(destination: SearchDetailDestination) {
        // 上一段过渡（进入 / 退出）尚未跑完时先排队，等本次过渡跑完立刻执行 —— 直接
        // 忽略会让「退出后马上点下一张卡」丢输入，直接执行又会掐断旧过渡把卡片遗弃在
        // 半途。排队两头都避开。
        if (overlayTransitionBusy) {
            pendingDetail = destination
            return
        }
        applyDetail(destination)
    }
    // 详情覆盖层的过渡生命周期：只负责上锁/解锁与排队。卡片可见性已由 selectedDetail
    // 同帧同步驱动（见声明处注释），与详情 hero 完全对齐。
    LaunchedEffect(overlayDestination) {
        overlayTransitionBusy = true
        if (overlayDestination != null) {
            // 进入过渡跑完（含 sharedElement morph 收尾）再解锁。
            delay(MeloXMotion.PageEnterMillis.toLong() + OverlayTransitionSettleMillis)
        } else if (overlayDetail != null) {
            // 退出过渡：详情内容（overlayDetail）保留到过渡跑完，退场期间详情子树仍在组合。
            delay(MeloXMotion.PageExitMillis.toLong() + OverlayTransitionSettleMillis)
            overlayDetail = null
        }
        // 被新的打开请求取消时停在 delay，不解锁、也不清空排队。这段自己跑完
        // 才会接到下一次，避免连续点卡时第一次点击丢失。
        overlayTransitionBusy = false
        pendingDetail?.let {
            pendingDetail = null
            applyDetail(it)
        }
    }
    // The host-level BackHandler always wins over the system back button, so the
    // search page reports its current back action to the host through
    // onSearchBackState. The host reads it inside its own BackHandler and either
    // clears the query/overlay (via backClearSignal) or switches to the home
    // page. The search page never writes backClearSignal itself, otherwise
    // reacting to an overlay would clear the overlay and loop forever.
    LaunchedEffect(query, podcastDiscovery, selectedDetail, categoryTitle, overlayDestination) {
        onSearchBackState(
            when {
                overlayDestination != null || podcastDiscovery || selectedDetail != null || categoryTitle != null ->
                    SearchBackAction.ClearOverlay
                query.isNotBlank() -> SearchBackAction.ClearQuery
                else -> SearchBackAction.SwitchToHome
            },
        )
    }
    var backActionConsumed by remember { mutableStateOf(SearchBackAction.SwitchToHome) }
    LaunchedEffect(backClearSignal.value, backActionConsumed) {
        val action = backClearSignal.value
        if (action == SearchBackAction.SwitchToHome || action == backActionConsumed) return@LaunchedEffect
        backActionConsumed = action
        when (action) {
            SearchBackAction.ClearOverlay -> {
                podcastDiscovery = false
                selectedDetail = null
                categoryTitle = null
                // ⚠ 这里**不清** categoryPlaylists：分类页要留着列表才播得完退场动画，
                // 下次打开会先 loading=true 再覆盖，不会闪到旧数据。
                error = null
            }
            SearchBackAction.ClearQuery -> {
                query = ""
                skipSearchDebounce = false
                searchTrigger += 1
            }
            SearchBackAction.SwitchToHome -> Unit
        }
        backClearSignal.value = SearchBackAction.SwitchToHome
    }
    PredictiveBackHandler(enabled = overlayDestination != null) {
        try {
            it.collect { event -> detailBackProgress.snapTo(event.progress) }
            // 先启动退出过渡：此刻位移仍停在手势落点上，封面从这里一镜到底
            // morph 回结果卡片，剩余位移随后收尾。不能在关闭详情之后 snapTo(0f)：
            // 那会在页面已经在屏外时把位移瞬间归零，满屏不透明的详情页会闪回
            // 屏幕正中再滑出去。
            selectedDetail = null
            detailBackProgress.animateTo(1f, tween(160))
        } catch (_: CancellationException) {
            detailBackProgress.animateTo(0f, tween(160))
        }
    }
    BackHandler(enabled = overlayDestination == null && (podcastDiscovery || selectedDetail != null || categoryTitle != null)) {
        when {
            podcastDiscovery -> podcastDiscovery = false
            selectedDetail != null -> selectedDetail = null
            // 只关可见性；categoryPlaylists 留给退场动画用（下次打开先 loading=true 再覆盖）。
            else -> categoryTitle = null
        }
    }

    // 顶层提供共享元素作用域：下面的结果卡片与右侧滑入的详情 hero 都在它里面，
    // 封面才能从卡片位置一镜到底 morph 到详情。
    SharedTransitionLayout(Modifier.fillMaxSize()) {
    val searchSharedScope = this
    Box(Modifier.fillMaxSize()) {
        // 搜索主页层。整页子页面推进来的时候向左做视差退让（-1/4 位移 + 淡出），
        // 退回时反向复位 —— 与「设置 → 详情页」用的是同一套 [meloXPageEnter]/[meloXPageExit]。
        AnimatedVisibility(
            visible = !subPageOpen,
            enter = meloXPageEnter(fromRight = false),
            exit = meloXPageExit(toRight = false),
        ) {
                val window = rememberMeloXWindowInfo()
                Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 26.dp)
            .padding(horizontal = if (window.supportsTwoPane) window.gutter else 0.dp),
                ) {
        MeloXIosTopBar(title = stringResource(R.string.tab_search))
        Spacer(Modifier.height(16.dp))
        SearchField(
            value = query,
            onValueChange = { query = it; skipSearchDebounce = false },
            onSearch = { skipSearchDebounce = true; searchTrigger += 1 },
            source = source,
            onBack = { if (query.isNotBlank()) query = "" else onSearchExit() },
        )
        if (query.isNotBlank()) {
            SearchScopes(kind = kind, availableKinds = availableKinds, onKind = { kind = it })
        }

        Box(Modifier.weight(1f)) {
            when {
                query.isBlank() && source == MusicSource.Netease -> SearchDiscovery(
                    recommendations = recommendations,
                    onPlaylist = { openDetail(SearchDetailDestination.Netease(it.asSearchItem())) },
                    sharedTransitionScope = searchSharedScope,
                    selectedKey = selectedDetail?.key,
                    onCategory = { category ->
                        if (category == "播客") {
                            subPageContent = SearchSubPage.Podcast
                            podcastDiscovery = true
                        } else {
                            subPageContent = SearchSubPage.Category(category)
                            categoryTitle = category
                            loading = true; error = null
                            scope.launch {
                                runCatching { library.explorePlaylists(category, 50) }
                                    .onSuccess { categoryPlaylists = it }
                                    .onFailure { error = it.message ?: context.getString(R.string.search_category_failed) }
                                loading = false
                            }
                        }
                    },
                )
                query.isBlank() -> ProviderSearchDiscovery(
                    source = source,
                    recommendations = providerRecommendations,
                    onPlaylist = { openDetail(SearchDetailDestination.Provider(ProviderSearchDestination.Playlist(it))) },
                    sharedTransitionScope = searchSharedScope,
                    selectedKey = selectedDetail?.key,
                )
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SearchAccent)
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                kind == MeloXSearchKind.Songs && (unifiedEnabled || source != MusicSource.Netease) -> ProviderSearchSongResults(
                    values = providerSongs,
                    failures = unifiedFailures,
                    showSource = unifiedEnabled,
                    onPlay = { track ->
                        ProviderPlaybackCommands.playQueue(
                            context = context,
                            tracks = providerSongs,
                            selectedTrackId = track.id,
                            onFailure = { failure -> error = failure.message ?: context.getString(R.string.search_play_failed) },
                        )
                    },
                )
                kind == MeloXSearchKind.Songs -> SearchSongResults(
                    values = songs,
                    onPlay = { song -> PlaybackCommands.playQueue(context, songs, song.id) },
                    onMore = { selectedActionSong = it },
                    onLike = { song ->
                        scope.launch {
                            runCatching { operations.setSongLiked(song.id, true) }
                                .onFailure { error = it.message ?: context.getString(R.string.library_add_failed) }
                        }
                    },
                )
                source != MusicSource.Netease && kind == MeloXSearchKind.Playlists -> ProviderSearchMediaResults(
                    values = providerPlaylists.map { ProviderSearchDestination.Playlist(it) },
                    onOpen = { openDetail(SearchDetailDestination.Provider(it)) },
                    sharedTransitionScope = searchSharedScope,
                    selectedKey = selectedDetail?.key,
                )
                source != MusicSource.Netease && kind == MeloXSearchKind.Albums -> ProviderSearchMediaResults(
                    values = providerAlbums.map { ProviderSearchDestination.Album(it) },
                    onOpen = { openDetail(SearchDetailDestination.Provider(it)) },
                    sharedTransitionScope = searchSharedScope,
                    selectedKey = selectedDetail?.key,
                )
                source != MusicSource.Netease && kind == MeloXSearchKind.Artists -> ProviderSearchMediaResults(
                    values = providerArtists.map { ProviderSearchDestination.Artist(it) },
                    onOpen = { openDetail(SearchDetailDestination.Provider(it)) },
                )
                else -> SearchMediaResults(
                    values = media,
                    onOpen = { item ->
                        when (item.kind) {
                            MeloXSearchKind.Albums, MeloXSearchKind.Artists, MeloXSearchKind.Podcasts -> MeloXCollectionDetailActivity.launch(context, item)
                            MeloXSearchKind.Users -> MeloXAccountActivity.launch(context, item.id)
                            else -> openDetail(SearchDetailDestination.Netease(item))
                        }
                    },
                    sharedTransitionScope = searchSharedScope,
                    selectedKey = selectedDetail?.key,
                )
            }
        }
            }
        }
        // 整页子页面层：分类页（浏览类别 → 排行榜等）/ 播客页 / 没有 hero 的详情页（provider 歌手）。
        // 这几类没有可 morph 的 hero 封面，走「整页推入」：自带不透明背板 + 从右侧满宽滑入
        // 盖住主页，主页同步向左视差退让；退回时反向滑出。和「设置 → 详情页」同一套动作。
        // ⚠ 背板必须在这里自己画：MeloX 的页面本身都是透明底、不透明背板只在 MeloXApp 根部
        //   画一次；不补背板，整页滑入的全程会把下层列表透出来，观感是撕裂而不是推入。
        // 内容取 subPageContent（返回时不清），退场动画期间才有东西可画。
        AnimatedVisibility(
            visible = subPageOpen,
            enter = meloXPageEnter(fromRight = true),
            exit = meloXPageExit(toRight = true),
            modifier = Modifier.fillMaxSize().zIndex(1f),
        ) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (val page = subPageContent) {
                SearchSubPage.Podcast -> MeloXPodcastScreen()
                is SearchSubPage.Category -> SearchCategoryPage(
                    title = page.title,
                    values = categoryPlaylists,
                    loading = loading,
                    error = error,
                    onBack = { categoryTitle = null; error = null },
                    onPlaylist = { openDetail(SearchDetailDestination.Netease(it.asSearchItem())) },
                    sharedTransitionScope = searchSharedScope,
                    selectedKey = selectedDetail?.key,
                )
                is SearchSubPage.Detail -> SearchCollectionDetail(
                    destination = page.destination,
                    universal = universal,
                    library = library,
                    providerRegistry = providerRegistry,
                    onBack = { selectedDetail = null },
                )
                null -> Unit
            }
            }
        }
        // 详情覆盖层。zIndex 要压在整页子页面层之上（从分类页点歌单时两层同时在场）。
        // visible 只由 selectedDetail 驱动，内容取 overlayDetail（返回时不清），
        // 于是退出过渡期间详情子树仍在组合里，封面才有机会一镜到底 morph 回结果卡片。
        AnimatedVisibility(
            visible = overlayDestination != null,
            enter = meloXPageEnter(fromRight = true),
            exit = meloXPageExit(toRight = true),
            modifier = Modifier.fillMaxSize().zIndex(2f),
        ) {
            val detailVisibilityScope = this
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = size.width * detailBackProgress.value
                        val scale = 1f - 0.08f * detailBackProgress.value
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    },
            ) {
                overlayDetail?.let { destination ->
                    SearchCollectionDetail(
                        destination = destination,
                        universal = universal,
                        library = library,
                        providerRegistry = providerRegistry,
                        onBack = { selectedDetail = null },
                        sharedTransitionScope = searchSharedScope,
                        animatedVisibilityScope = detailVisibilityScope,
                        artworkSharedKey = searchArtworkSharedKey(destination),
                    )
                }
            }
        }
    }
    } // SharedTransitionLayout
    selectedActionSong?.let { song ->
        MeloXSongActionsOverlay(
            song = song,
            queue = songs,
            visible = true,
            onDismiss = { selectedActionSong = null },
        )
    }
}

/**
 * 结果卡片封面与详情 hero 共用的配对 key。带前缀是为了避免同一作用域内撞 key
 * （「热门推荐」与结果列表可能含同一个歌单）。key 直接复用
 * [SearchDetailDestination.key]，于是「点了哪张卡」和「详情开到哪个目的地」
 * 必然算出同一个 key。
 */
private const val SearchArtworkKeyPrefix = "search-collection-artwork-"

private fun searchArtworkSharedKey(destination: SearchDetailDestination): String =
    SearchArtworkKeyPrefix + destination.key

private fun searchArtworkSharedKey(destinationKey: String): String =
    SearchArtworkKeyPrefix + destinationKey

/**
 * 把结果卡片封面挂到宿主顶层的 SharedTransitionLayout 上，使搜索页打开歌单/专辑时
 * 封面能从卡片位置一镜到底 morph 到详情 hero。scope 为空（未接入共享元素的场景）
 * 时原样返回，不影响常规渲染。
 */
@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun Modifier.meloXSearchSharedArtwork(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    key: String,
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    return with(sharedTransitionScope) {
        this@meloXSearchSharedArtwork.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope,
            // 过渡期间封面留在共享 overlay 层，避免被列表/详情的裁剪吃掉。
            renderInOverlayDuringTransition = true,
            zIndexInOverlay = 1f,
        )
    }
}

/**
 * 被点选中的那张卡必须真的进入 exit 过渡，共享元素才会配对：
 * 恒可见（`AnimatedVisibility(visible = true)`）是配不上的。
 */
@Composable
private fun SearchCardVisibility(
    destinationKey: String,
    selectedKey: String?,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = destinationKey != selectedKey,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
        label = "search-card-$destinationKey",
        content = content,
    )
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    source: MusicSource,
    onBack: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val clearDescription = stringResource(R.string.search_clear)
    val searchDescription = stringResource(R.string.search_content_description)
    MeloXGlassTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .padding(horizontal = 20.dp),
        leadingContent = {
            MeloXSearchBackMorphIcon(
                focused = focused,
                modifier = Modifier
                    .size(44.dp)
                    .clickable(role = Role.Button, onClick = onBack)
                    .padding(11.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f),
                contentDescription = stringResource(if (focused) R.string.action_back else R.string.tab_search),
            )
        },
        placeholder = {
            Text(
                stringResource(R.string.search_placeholder),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .42f),
                fontSize = 17.sp,
            )
        },
        trailingContent = {
            Row {
                if (value.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(role = Role.Button) { onSearch() }
                            .semantics { contentDescription = searchDescription },
                        contentAlignment = Alignment.Center,
                    ) {
                        MeloXSymbolIcon(
                            symbol = MeloXSymbol.Search,
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .56f),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(role = Role.Button) { onValueChange("") }
                            .semantics { contentDescription = clearDescription },
                        contentAlignment = Alignment.Center,
                    ) {
                        MeloXSymbolIcon(
                            symbol = MeloXSymbol.Xmark,
                            modifier = Modifier.size(15.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .56f),
                        )
                    }
                }
            }
        },
        textStyle = androidx.compose.ui.text.TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 17.sp,
            lineHeight = 22.sp,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        onFocusChanged = { focused = it },
    )
}

@Composable
private fun SearchScopes(
    kind: MeloXSearchKind,
    availableKinds: List<MeloXSearchKind>,
    onKind: (MeloXSearchKind) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(availableKinds) { item ->
            MeloXGlassButton(
                onClick = { onKind(item) },
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 0.dp),
                style = MeloXGlassButtonStyle.Bordered,
                shape = MeloXShapes.capsule,
                tint = if (item == kind) MeloXSystemColors.Blue.copy(alpha = .28f) else Color.Transparent,
                surfaceColor = if (item == kind) MeloXSystemColors.Blue.copy(alpha = .16f) else MaterialTheme.colorScheme.onBackground.copy(alpha = .045f),
                contentPadding = PaddingValues(horizontal = 15.dp),
            ) {
                Text(
                    item.title,
                    color = if (item == kind) SearchAccent else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (item == kind) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SearchDiscovery(
    recommendations: List<NeteasePlaylistSummary>,
    onPlaylist: (NeteasePlaylistSummary) -> Unit,
    onCategory: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    selectedKey: String? = null,
) {
    val window = rememberMeloXWindowInfo()
    val categoryColumns = window.gridColumns.coerceIn(2, 4)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = MeloXBottomContentClearance),
    ) {
        if (recommendations.isNotEmpty()) {
            item { Text(stringResource(R.string.search_trending), modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 12.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(recommendations, key = { it.id }) { p ->
                        val destinationKey = SearchDetailDestination.Netease(p.asSearchItem()).key
                        SearchCardVisibility(
                            destinationKey = destinationKey,
                            selectedKey = selectedKey,
                        ) {
                            val cardVisibilityScope = this
                            Column(Modifier.width(160.dp).clickable { onPlaylist(p) }) {
                                // 封面单独包一层 Box 承载 sharedElement：size/clip 放在
                                // sharedElement 之后，阴影之类的外部效果严禁进这条链。
                                Box(
                                    Modifier
                                        .meloXSearchSharedArtwork(
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedVisibilityScope = cardVisibilityScope,
                                            key = searchArtworkSharedKey(destinationKey),
                                        )
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                ) {
                                    AsyncImage(p.coverUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                                Text(p.name, modifier = Modifier.padding(top = 7.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        item { Text(stringResource(R.string.search_browse), modifier = Modifier.padding(start = 20.dp, top = 26.dp, bottom = 12.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        items(SearchCategories.filter { it != "播客" || MeloXSettingsRuntime.podcastsEnabled }.chunked(categoryColumns)) { pair ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { category -> SearchCategoryCard(category, Modifier.weight(1f)) { onCategory(category) } }
                repeat(categoryColumns - pair.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ProviderSearchDiscovery(
    source: MusicSource,
    recommendations: List<MusicPlaylistSummary>,
    onPlaylist: (MusicPlaylistSummary) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    selectedKey: String? = null,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = MeloXBottomContentClearance),
    ) {
        if (recommendations.isNotEmpty()) {
            item { Text(stringResource(R.string.search_trending), modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 12.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(recommendations, key = { "${it.id.source.storageValue}:${it.id.value}" }) { p ->
                        val destinationKey = SearchDetailDestination.Provider(ProviderSearchDestination.Playlist(p)).key
                        SearchCardVisibility(
                            destinationKey = destinationKey,
                            selectedKey = selectedKey,
                        ) {
                            val cardVisibilityScope = this
                            Column(Modifier.width(160.dp).clickable { onPlaylist(p) }) {
                                Box(
                                    Modifier
                                        .meloXSearchSharedArtwork(
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedVisibilityScope = cardVisibilityScope,
                                            key = searchArtworkSharedKey(destinationKey),
                                        )
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                ) {
                                    AsyncImage(p.artworkUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                                Text(p.title, modifier = Modifier.padding(top = 7.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        item {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.search_source_hint, source.displayName), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
            }
        }
    }
}

@Composable
private fun SearchCategoryCard(title: String, modifier: Modifier, onClick: () -> Unit) {
    val tint = when (title.hashCode().mod(5)) {
        0 -> Color(0xFFE76F51); 1 -> Color(0xFF7B61FF); 2 -> Color(0xFF2A9D8F); 3 -> Color(0xFFE84A8A); else -> Color(0xFF3A86FF)
    }
    Box(
        modifier
            .height(96.dp)
            .clip(MeloXShapes.compact)
            .background(tint)
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart,
    ) { Text(searchCategoryLabel(title), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun ProviderSearchSongResults(
    values: List<MusicTrack>,
    failures: List<UnifiedMusicService.SearchFailure>,
    showSource: Boolean,
    onPlay: (MusicTrack) -> Unit,
) {
    val context = LocalContext.current
    val providerDownloads = remember(context) { MeloXProviderDownloadStore.get(context) }
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = MeloXBottomContentClearance),
    ) {
        if (failures.isNotEmpty()) {
            item {
                Text(
                    failures.joinToString("；") { "${it.source.displayName}：${it.message}" },
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .46f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        }
        if (values.isEmpty()) {
            item { SearchEmptyInline(stringResource(R.string.search_no_songs)) }
        } else {
            items(values, key = { "provider:${it.id.source.storageValue}:${it.id.value}" }) { track ->
                SearchSwipeSongRow(
                    song = MeloXLegacyUiBridge.track(track),
                    onPlay = { onPlay(track) },
                    onMore = null,
                    endAction = MeloXSwipeAction(stringResource(R.string.search_download_local), MeloXSymbol.Download, Color(0xFF0EA5E9)) {
                        providerDownloads.start(track)
                    },
                    sourceLabel = track.id.source.displayName.takeIf { showSource },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
            }
        }
    }
}

@Composable
private fun providerSearchSubtitle(item: ProviderSearchDestination): String {
    val artist = item as? ProviderSearchDestination.Artist ?: return item.subtitle
    return buildList {
        artist.value.songCount?.let { add(stringResource(R.string.search_song_count, it)) }
        artist.value.albumCount?.let { add(stringResource(R.string.search_album_count, it)) }
    }.joinToString(" · ")
}

@Composable
private fun ProviderSearchMediaResults(
    values: List<ProviderSearchDestination>,
    onOpen: (ProviderSearchDestination) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    selectedKey: String? = null,
) {
    if (values.isEmpty()) { SearchEmpty(stringResource(R.string.search_no_results)); return }
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = MeloXBottomContentClearance)) {
        items(values, key = ProviderSearchDestination::key) { item ->
            val artworkShape = if (item.kind == MeloXSearchKind.Artists) CircleShape else RoundedCornerShape(8.dp)
            SearchCardVisibility(
                destinationKey = item.key,
                selectedKey = selectedKey,
            ) {
                val cardVisibilityScope = this
                val subtitle = providerSearchSubtitle(item).ifBlank { item.kind.title }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(item) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .meloXSearchSharedArtwork(
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = cardVisibilityScope,
                                key = searchArtworkSharedKey(item.key),
                            )
                            .size(54.dp)
                            .clip(artworkShape),
                    ) {
                        AsyncImage(item.artworkUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 17.sp)
                        Text(
                            subtitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                            fontSize = 13.sp,
                        )
                    }
                    MeloXActionIcon("›", Modifier.size(18.dp), MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))
                }
            }
        }
    }
}

@Composable
private fun SearchSongResults(
    values: List<SearchSong>,
    onPlay: (SearchSong) -> Unit,
    onMore: (SearchSong) -> Unit,
    onLike: (SearchSong) -> Unit,
) {
    if (values.isEmpty()) { SearchEmpty(stringResource(R.string.search_no_songs)); return }
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = MeloXBottomContentClearance)) {
        items(values, key = { it.id }) { song ->
            SearchSwipeSongRow(
                song = song,
                onPlay = { onPlay(song) },
                onMore = { onMore(song) },
                endAction = MeloXSwipeAction(stringResource(R.string.artist_add_library), MeloXSymbol.Heart, Color(0xFFFF3B30)) { onLike(song) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
        }
    }
}

@Composable
private fun SearchSwipeSongRow(
    song: SearchSong,
    onPlay: () -> Unit,
    onMore: (() -> Unit)?,
    endAction: MeloXSwipeAction?,
    sourceLabel: String? = null,
) {
    val context = LocalContext.current
    MeloXSwipeActionRow(
        startActions = listOf(
            MeloXSwipeAction(stringResource(R.string.player_play_next), MeloXSymbol.Next, Color(0xFF8E5AF7)) { PlaybackCommands.playNext(context, song) },
            MeloXSwipeAction(stringResource(R.string.artist_play_later), MeloXSymbol.Queue, Color(0xFFFF9F0A)) { PlaybackCommands.addToQueue(context, song) },
        ),
        endActions = listOfNotNull(endAction),
        startFullSwipeActionIndex = if (MeloXSettingsRuntime.swipeFullAction == MeloXSwipeFullAction.AddToQueue) 1 else 0,
        onClick = onPlay,
        onLongClick = onMore,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(song.artworkUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(song.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text(
                    buildList {
                        add(song.artists)
                        song.album.takeIf(String::isNotBlank)?.let(::add)
                        sourceLabel?.let(::add)
                    }.joinToString(" · "),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                )
            }
        }
    }
}

@Composable
private fun SearchMediaResults(
    values: List<MeloXSearchMediaItem>,
    onOpen: (MeloXSearchMediaItem) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    selectedKey: String? = null,
) {
    if (values.isEmpty()) { SearchEmpty(stringResource(R.string.search_no_results)); return }
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = MeloXBottomContentClearance)) {
        items(values, key = { "${it.kind}-${it.id}" }) { item ->
            val destinationKey = SearchDetailDestination.Netease(item).key
            val artworkShape = if (item.kind == MeloXSearchKind.Artists || item.kind == MeloXSearchKind.Users) CircleShape else RoundedCornerShape(8.dp)
            SearchCardVisibility(
                destinationKey = destinationKey,
                selectedKey = selectedKey,
            ) {
                val cardVisibilityScope = this
                val subtitle = item.subtitle.ifBlank {
                    if (item.trackCount > 0) stringResource(R.string.search_song_count_short, item.trackCount) else item.kind.title
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(item) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .meloXSearchSharedArtwork(
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = cardVisibilityScope,
                                key = searchArtworkSharedKey(destinationKey),
                            )
                            .size(54.dp)
                            .clip(artworkShape),
                    ) {
                        AsyncImage(item.artworkUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 17.sp)
                        Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f), fontSize = 13.sp)
                    }
                    MeloXActionIcon("›", Modifier.size(18.dp), MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))
                }
            }
        }
    }
}

@Composable
private fun SearchCategoryPage(
    title: String,
    values: List<NeteasePlaylistSummary>,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onPlaylist: (NeteasePlaylistSummary) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    selectedKey: String? = null,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(top = 16.dp)) {
        SearchDetailHeader(searchCategoryLabel(title), onBack)
        when {
            title == "播客" -> SearchEmpty(stringResource(R.string.search_podcast_hint))
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> SearchEmpty(error)
            values.isEmpty() -> SearchEmpty(stringResource(R.string.melox_state_empty))
            else -> LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = MeloXBottomContentClearance)) {
                items(values, key = { it.id }) { p ->
                    val destinationKey = SearchDetailDestination.Netease(p.asSearchItem()).key
                    val songCount = stringResource(R.string.library_song_count, p.trackCount)
                    SearchCardVisibility(
                        destinationKey = destinationKey,
                        selectedKey = selectedKey,
                    ) {
                        val cardVisibilityScope = this
                        Row(Modifier.fillMaxWidth().clickable { onPlaylist(p) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .meloXSearchSharedArtwork(
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = cardVisibilityScope,
                                        key = searchArtworkSharedKey(destinationKey),
                                    )
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(9.dp)),
                            ) {
                                AsyncImage(p.coverUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(p.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                Text(songCount, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCollectionDetail(
    destination: SearchDetailDestination,
    universal: NeteaseUniversalSearchClient,
    library: NeteaseLibraryClient,
    providerRegistry: com.lladlam.melox.core.music.provider.MusicProviderRegistry,
    onBack: () -> Unit,
    // 覆盖层打开时把宿主的共享元素作用域与配对 key 透传给详情 hero，封面才能
    // 从结果卡片一镜到底 morph 进来/回去。不传则保持「自建作用域 + 淡入」。
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    artworkSharedKey: String? = null,
) {
    when (destination) {
        is SearchDetailDestination.Netease -> if (destination.value.kind == MeloXSearchKind.Playlists) {
            val value = destination.value
            MeloXUnifiedPlaylistDetailScreen(
                playlist = com.lladlam.melox.core.library.NeteasePlaylistSummary(
                    id = value.id,
                    name = value.title,
                    coverUrl = value.artworkUrl,
                    trackCount = value.trackCount,
                    creatorName = value.subtitle,
                ),
                onBack = onBack,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                artworkSharedKey = artworkSharedKey,
            )
            return
        }
        is SearchDetailDestination.Provider -> {
            val value = destination.value
            if (value is ProviderSearchDestination.Playlist) {
                MeloXUnifiedPlaylistDetailScreen(
                    playlist = MeloXLegacyUiBridge.playlist(value.value),
                    onBack = onBack,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    artworkSharedKey = artworkSharedKey,
                )
                return
            }
            if (value is ProviderSearchDestination.Album) {
                MeloXUnifiedProviderAlbumDetailScreen(
                    album = value.value,
                    onBack = onBack,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    artworkSharedKey = artworkSharedKey,
                )
                return
            }
        }
    }
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val operations = remember(appContext) { NeteaseMusicOperationsClient(cookieProvider = { NeteaseSessionStore.readCookie(appContext) }) }
    var songs by remember(destination.key) { mutableStateOf<List<SearchSong>>(emptyList()) }
    var providerTracks by remember(destination.key) { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var loading by remember(destination.key) { mutableStateOf(true) }
    var error by remember(destination.key) { mutableStateOf<String?>(null) }
    var selectedActionSong by remember(destination.key) { mutableStateOf<SearchSong?>(null) }

    LaunchedEffect(destination.key) {
        loading = true
        error = null
        runCatching {
            withContext(Dispatchers.IO) {
                when (destination) {
                    is SearchDetailDestination.Netease -> {
                        val item = destination.value
                        val values = if (item.kind == MeloXSearchKind.Playlists) {
                            library.playlistDetail(item.id).songs
                        } else {
                            universal.collectionSongs(item)
                        }
                        values to emptyList<MusicTrack>()
                    }
                    is SearchDetailDestination.Provider -> {
                        val item = destination.value
                        val provider = providerRegistry.require(item.source)
                        val tracks = when (item) {
                            is ProviderSearchDestination.Playlist -> {
                                val capability = provider as? PlaylistCapability
                                    ?: throw IllegalStateException(appContext.getString(R.string.search_playlist_unavailable, item.source.displayName))
                                capability.loadAllPlaylistTracks(item.value, pageSize = 200).tracks
                            }
                            is ProviderSearchDestination.Album -> {
                                val capability = provider as? AlbumCapability
                                    ?: throw IllegalStateException(appContext.getString(R.string.search_album_unavailable, item.source.displayName))
                                capability.albumDetail(item.value, page = 1, pageSize = 150).tracks
                            }
                            is ProviderSearchDestination.Artist -> {
                                val capability = provider as? ArtistCapability
                                    ?: throw IllegalStateException(appContext.getString(R.string.search_artist_unavailable, item.source.displayName))
                                capability.artistDetail(item.value, page = 1, pageSize = 150).tracks
                            }
                        }
                        emptyList<SearchSong>() to tracks
                    }
                }
            }
        }.onSuccess { (neteaseSongs, commonTracks) ->
            songs = neteaseSongs
            providerTracks = commonTracks
        }.onFailure { error = it.message ?: appContext.getString(R.string.search_content_failed) }
        loading = false
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(top = 16.dp)) {
        SearchDetailHeader(destination.title, onBack)
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = MeloXBottomContentClearance)) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        destination.artworkUrl,
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(210.dp).clip(if (destination.kind == MeloXSearchKind.Artists) CircleShape else RoundedCornerShape(15.dp)),
                    )
                    Text(
                        destination.title,
                        modifier = Modifier.padding(top = 16.dp),
                        fontSize = 23.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (destination.subtitle.isNotBlank()) {
                        Text(destination.subtitle, modifier = Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f))
                    }
                    val hasTracks = songs.isNotEmpty() || providerTracks.isNotEmpty()
                    if (hasTracks) {
                        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SearchPlayButton(stringResource(R.string.search_shuffle)) {
                                if (providerTracks.isNotEmpty()) {
                                    val shuffled = providerTracks.shuffled()
                                    shuffled.firstOrNull()?.let { ProviderPlaybackCommands.playQueue(context, shuffled, it.id) }
                                } else {
                                    val shuffled = songs.shuffled()
                                    shuffled.firstOrNull()?.let { PlaybackCommands.playQueue(context, shuffled, it.id) }
                                }
                            }
                            SearchPlayButton(stringResource(R.string.action_play)) {
                                if (providerTracks.isNotEmpty()) {
                                    providerTracks.firstOrNull()?.let { ProviderPlaybackCommands.playQueue(context, providerTracks, it.id) }
                                } else {
                                    songs.firstOrNull()?.let { PlaybackCommands.playQueue(context, songs, it.id) }
                                }
                            }
                        }
                    }
                }
            }
            when {
                loading -> item { Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                error != null -> item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }
                providerTracks.isNotEmpty() -> items(providerTracks, key = { "detail:${it.id.source.storageValue}:${it.id.value}" }) { track ->
                    SearchSwipeSongRow(
                        song = MeloXLegacyUiBridge.track(track),
                        onPlay = { ProviderPlaybackCommands.playQueue(context, providerTracks, track.id) },
                        onMore = null,
                        endAction = MeloXSwipeAction(stringResource(R.string.search_download_local), MeloXSymbol.Download, Color(0xFF0EA5E9)) {
                            MeloXProviderDownloadStore.get(context).start(track)
                        },
                        sourceLabel = track.id.source.displayName,
                    )
                }
                else -> items(songs, key = { it.id }) { song ->
                    SearchSwipeSongRow(
                        song = song,
                        onPlay = { PlaybackCommands.playQueue(context, songs, song.id) },
                        onMore = { selectedActionSong = song },
                        endAction = MeloXSwipeAction(stringResource(R.string.artist_add_library), MeloXSymbol.Heart, Color(0xFFFF3B30)) {
                            scope.launch {
                                runCatching { operations.setSongLiked(song.id, true) }
                                    .onFailure { error = it.message ?: context.getString(R.string.library_add_failed) }
                            }
                        },
                    )
                }
            }
        }
    }
    selectedActionSong?.let { song ->
        MeloXSongActionsOverlay(
            song = song,
            queue = songs,
            visible = true,
            onDismiss = { selectedActionSong = null },
        )
    }
}

@Composable
private fun SearchDetailHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        MeloXGlassButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(11.dp),
        ) {
            MeloXSymbolIcon(MeloXSymbol.ChevronLeft, Modifier.fillMaxSize(), MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, Modifier.weight(1f), style = MeloXTypography.title2, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SearchPlayButton(title: String, onClick: () -> Unit) {
    MeloXGlassButton(
        onClick = onClick,
        modifier = Modifier.height(44.dp).width(120.dp),
        style = MeloXGlassButtonStyle.BorderedProminent,
        shape = MeloXShapes.capsule,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun SearchEmpty(message: String) {
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f))
    }
}

@Composable
private fun SearchEmptyInline(message: String) {
    Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f))
    }
}

private fun parseSongLink(value: String): Long? {
    val patterns = listOf(Regex("[?&]id=(\\d+)"), Regex("/song/(\\d+)"))
    return patterns.firstNotNullOfOrNull { it.find(value)?.groupValues?.getOrNull(1)?.toLongOrNull() }
}

private fun NeteasePlaylistSummary.asSearchItem() = MeloXSearchMediaItem(
    id = id,
    kind = MeloXSearchKind.Playlists,
    title = name,
    subtitle = creatorName,
    artworkUrl = coverUrl,
    trackCount = trackCount,
)
