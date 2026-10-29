package com.cncverse;

import android.content.Context;
import com.cncverse.donation.DonationManager;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.module.kotlin.ExtensionsKt;
import com.lagradost.cloudstream3.Episode;
import com.lagradost.cloudstream3.HomePageList;
import com.lagradost.cloudstream3.HomePageResponse;
import com.lagradost.cloudstream3.LoadResponse;
import com.lagradost.cloudstream3.MainAPI;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.MainPageData;
import com.lagradost.cloudstream3.MainPageRequest;
import com.lagradost.cloudstream3.MovieSearchResponse;
import com.lagradost.cloudstream3.SearchQuality;
import com.lagradost.cloudstream3.SearchResponse;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.TvSeriesSearchResponse;
import com.lagradost.cloudstream3.TvType;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import com.lagradost.cloudstream3.utils.Qualities;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.collections.SetsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.io.CloseableKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DoFlixProvider.kt */
@Metadata(d1 = {"\u0000\u008e\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0018\u0018\u0000 G2\u00020\u0001:\u0016GHIJKLMNOPQRSTUVWXYZ[\\B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\"\u001a\u00020\u00052\u0006\u0010#\u001a\u00020\u0005H\u0082@¢\u0006\u0002\u0010$J\u0014\u0010*\u001a\u0004\u0018\u00010+2\b\u0010,\u001a\u0004\u0018\u00010\u0005H\u0002J\u0012\u0010-\u001a\u00020(2\b\u0010,\u001a\u0004\u0018\u00010\u0005H\u0002J \u00101\u001a\u0004\u0018\u0001032\u0006\u00104\u001a\u00020(2\u0006\u00105\u001a\u000206H\u0096@¢\u0006\u0002\u00107J\u001c\u00108\u001a\b\u0012\u0004\u0012\u0002090/2\u0006\u0010:\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010$J\u0018\u0010;\u001a\u0004\u0018\u00010<2\u0006\u0010#\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010$JF\u0010=\u001a\u00020\u000f2\u0006\u0010>\u001a\u00020\u00052\u0006\u0010?\u001a\u00020\u000f2\u0012\u0010@\u001a\u000e\u0012\u0004\u0012\u00020B\u0012\u0004\u0012\u00020C0A2\u0012\u0010D\u001a\u000e\u0012\u0004\u0012\u00020E\u0012\u0004\u0012\u00020C0AH\u0096@¢\u0006\u0002\u0010FR\u001a\u0010\u0004\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u000e\u0010\r\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\u00020\u000fX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u001a\u0010\u0012\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0007\"\u0004\b\u0014\u0010\tR\u001a\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u001a\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u001bX\u0082\u0004¢\u0006\u0002\n\u0000R\u001b\u0010\u001c\u001a\u00020\u001d8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b \u0010!\u001a\u0004\b\u001e\u0010\u001fR\u000e\u0010%\u001a\u00020&X\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010'\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00050\u001bX\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010)\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00050\u001bX\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010.\u001a\b\u0012\u0004\u0012\u0002000/X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b1\u00102¨\u0006]"}, d2 = {"Lcom/cncverse/DoFlixProvider;", "Lcom/lagradost/cloudstream3/MainAPI;", "<init>", "()V", "mainUrl", "", "getMainUrl", "()Ljava/lang/String;", "setMainUrl", "(Ljava/lang/String;)V", "name", "getName", "setName", "apiKey", "hasMainPage", "", "getHasMainPage", "()Z", "lang", "getLang", "setLang", "supportedTypes", "", "Lcom/lagradost/cloudstream3/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "headers", "", "customClient", "Lokhttp3/OkHttpClient;", "getCustomClient", "()Lokhttp3/OkHttpClient;", "customClient$delegate", "Lkotlin/Lazy;", "getWithCustomHeaders", "url", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "movieGenres", "", "seriesGenres", "getQualityFromString", "Lcom/lagradost/cloudstream3/SearchQuality;", "qualityString", "getQualityValue", "mainPage", "", "Lcom/lagradost/cloudstream3/MainPageData;", "getMainPage", "()Ljava/util/List;", "Lcom/lagradost/cloudstream3/HomePageResponse;", "page", "request", "Lcom/lagradost/cloudstream3/MainPageRequest;", "(ILcom/lagradost/cloudstream3/MainPageRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "Lcom/lagradost/cloudstream3/SearchResponse;", "query", "load", "Lcom/lagradost/cloudstream3/LoadResponse;", "loadLinks", "data", "isCasting", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "MovieResult", "SeriesResult", "HomePageApiResponse", "SeriesPageApiResponse", "VideoItem", "SearchApiResponse", "TMDBGenre", "ProductionCountry", "CastMember", "CrewMember", "Credits", "SimilarResults", "MultiSearchResponse", "MovieDetails", "SeasonInfo", "TvDetails", "TvEpisode", "SeasonDetails", "StreamLink", "MovieLinksResponse", "EpisodeLinksResponse", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nDoFlixProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DoFlixProvider.kt\ncom/cncverse/DoFlixProvider\n+ 2 Extensions.kt\ncom/fasterxml/jackson/module/kotlin/ExtensionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 4 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 5 Maps.kt\nkotlin/collections/MapsKt__MapsKt\n*L\n1#1,836:1\n116#2:837\n54#2:838\n117#2:839\n61#2,8:840\n71#2:849\n116#2:854\n54#2:855\n117#2:856\n61#2,8:857\n71#2:866\n116#2:889\n54#2:890\n117#2:891\n61#2,8:892\n71#2:901\n116#2:906\n54#2:907\n117#2:908\n61#2,8:909\n71#2:918\n116#2:941\n54#2:942\n117#2:943\n61#2,8:944\n71#2:953\n116#2:954\n54#2:955\n117#2:956\n61#2,8:957\n71#2:966\n116#2:971\n54#2:972\n117#2:973\n61#2,8:974\n71#2:983\n116#2:984\n54#2:985\n117#2:986\n61#2,8:987\n71#2:996\n116#2:1000\n54#2:1001\n117#2:1002\n61#2,8:1003\n71#2:1012\n116#2:1015\n54#2:1016\n117#2:1017\n61#2,8:1018\n71#2:1027\n116#2:1030\n54#2:1031\n117#2:1032\n61#2,8:1033\n71#2:1042\n1#3:848\n1#3:865\n1#3:900\n1#3:917\n1#3:952\n1#3:965\n1#3:982\n1#3:995\n1#3:1011\n1#3:1026\n1#3:1041\n1586#4:850\n1661#4,3:851\n1586#4:867\n1661#4,3:868\n1915#4:871\n1915#4:872\n1916#4:880\n1916#4:881\n1080#4:882\n1915#4:883\n1586#4:884\n1661#4,3:885\n1916#4:888\n1586#4:902\n1661#4,3:903\n1586#4:919\n1661#4,3:920\n1915#4:923\n1915#4:924\n1916#4:932\n1916#4:933\n1080#4:934\n1915#4:935\n1586#4:936\n1661#4,3:937\n1916#4:940\n1915#4,2:967\n1915#4,2:969\n777#4:997\n873#4,2:998\n1915#4,2:1013\n1915#4,2:1028\n1915#4,2:1043\n383#5,7:873\n383#5,7:925\n*S KotlinDebug\n*F\n+ 1 DoFlixProvider.kt\ncom/cncverse/DoFlixProvider\n*L\n461#1:837\n461#1:838\n461#1:839\n461#1:840,8\n461#1:849\n482#1:854\n482#1:855\n482#1:856\n482#1:857,8\n482#1:866\n535#1:889\n535#1:890\n535#1:891\n535#1:892,8\n535#1:901\n556#1:906\n556#1:907\n556#1:908\n556#1:909,8\n556#1:918\n620#1:941\n620#1:942\n620#1:943\n620#1:944,8\n620#1:953\n625#1:954\n625#1:955\n625#1:956\n625#1:957,8\n625#1:966\n671#1:971\n671#1:972\n671#1:973\n671#1:974,8\n671#1:983\n710#1:984\n710#1:985\n710#1:986\n710#1:987,8\n710#1:996\n718#1:1000\n718#1:1001\n718#1:1002\n718#1:1003,8\n718#1:1012\n788#1:1015\n788#1:1016\n788#1:1017\n788#1:1018,8\n788#1:1027\n813#1:1030\n813#1:1031\n813#1:1032\n813#1:1033,8\n813#1:1042\n461#1:848\n482#1:865\n535#1:900\n556#1:917\n620#1:952\n625#1:965\n671#1:982\n710#1:995\n718#1:1011\n788#1:1026\n813#1:1041\n463#1:850\n463#1:851,3\n484#1:867\n484#1:868,3\n503#1:871\n504#1:872\n504#1:880\n503#1:881\n511#1:882\n513#1:883\n515#1:884\n515#1:885,3\n513#1:888\n537#1:902\n537#1:903,3\n558#1:919\n558#1:920,3\n577#1:923\n578#1:924\n578#1:932\n577#1:933\n585#1:934\n587#1:935\n589#1:936\n589#1:937,3\n587#1:940\n628#1:967,2\n641#1:969,2\n714#1:997\n714#1:998,2\n720#1:1013,2\n790#1:1028,2\n815#1:1043,2\n505#1:873,7\n579#1:925,7\n*E\n"})
/* loaded from: classes.dex */
public final class DoFlixProvider extends MainAPI {

    /* renamed from: Companion, reason: from kotlin metadata */
    @NotNull
    public static final Companion INSTANCE = new Companion(null);

    @Nullable
    private static Context context;

    @NotNull
    private String mainUrl = "https://panel.watchkaroabhi.com";

    @NotNull
    private String name = "DoFlix";

    @NotNull
    private final String apiKey = "qNhKLJiZVyoKdi9NCQGz8CIGrpUijujE";
    private final boolean hasMainPage = true;

    @NotNull
    private String lang = "ta";

    @NotNull
    private final Set<TvType> supportedTypes = SetsKt.setOf(new TvType[]{TvType.Movie, TvType.TvSeries});

    @NotNull
    private final Map<String, String> headers = MapsKt.mapOf(new Pair[]{TuplesKt.to("Connection", "Keep-Alive"), TuplesKt.to("User-Agent", "dooflix"), TuplesKt.to("X-App-Version", "305"), TuplesKt.to("X-Package-Name", "com.king.moja")});

    /* renamed from: customClient$delegate, reason: from kotlin metadata */
    @NotNull
    private final Lazy customClient = LazyKt.lazy(new DoFlixProvider$$ExternalSyntheticLambda8(this));

    @NotNull
    private final ObjectMapper mapper = ExtensionsKt.jacksonObjectMapper();

    @NotNull
    private final Map<Integer, String> movieGenres = MapsKt.mapOf(new Pair[]{TuplesKt.to(28, "Action"), TuplesKt.to(12, "Adventure"), TuplesKt.to(16, "Animation"), TuplesKt.to(35, "Comedy"), TuplesKt.to(80, "Crime"), TuplesKt.to(99, "Documentary"), TuplesKt.to(18, "Drama"), TuplesKt.to(10751, "Family"), TuplesKt.to(14, "Fantasy"), TuplesKt.to(36, "History"), TuplesKt.to(27, "Horror"), TuplesKt.to(10402, "Music"), TuplesKt.to(9648, "Mystery"), TuplesKt.to(10749, "Romance"), TuplesKt.to(878, "Science Fiction"), TuplesKt.to(10770, "TV Movie"), TuplesKt.to(53, "Thriller"), TuplesKt.to(10752, "War"), TuplesKt.to(37, "Western")});

    @NotNull
    private final Map<Integer, String> seriesGenres = MapsKt.mapOf(new Pair[]{TuplesKt.to(10765, "Sci-Fi & Fantasy"), TuplesKt.to(9648, "Mystery"), TuplesKt.to(35, "Comedy"), TuplesKt.to(18, "Drama"), TuplesKt.to(10759, "Action & Adventure"), TuplesKt.to(80, "Crime"), TuplesKt.to(10767, "Talk"), TuplesKt.to(10768, "War & Politics"), TuplesKt.to(16, "Animation"), TuplesKt.to(10751, "Family"), TuplesKt.to(10764, "Reality"), TuplesKt.to(10762, "Kids"), TuplesKt.to(99, "Documentary"), TuplesKt.to(37, "Western"), TuplesKt.to(10763, "News"), TuplesKt.to(10766, "Soap")});

    @NotNull
    private final List<MainPageData> mainPage = MainAPIKt.mainPageOf(new Pair[]{TuplesKt.to(getMainUrl() + "/api/3/discover/movie?api_key=qNhKLJiZVyoKdi9NCQGz8CIGrpUijujE&language=en&sort_by=primary_release_date.desc&watch_region=IN&page=1", "RecentMovies"), TuplesKt.to(getMainUrl() + "/api/3/discover/tv?api_key=qNhKLJiZVyoKdi9NCQGz8CIGrpUijujE&language=en&page=1&sort_by=first_air_date.desc", "RecentSeries"), TuplesKt.to(getMainUrl() + "/api/3/discover/movie?api_key=qNhKLJiZVyoKdi9NCQGz8CIGrpUijujE&language=en&sort_by=popularity.desc&watch_region=IN&page=1", "TrendingMovies"), TuplesKt.to(getMainUrl() + "/api/3/discover/tv?api_key=qNhKLJiZVyoKdi9NCQGz8CIGrpUijujE&language=en&page=1&sort_by=popularity.desc", "TrendingSeries")});

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b:\b\u0087\b\u0018\u00002\u00020\u0001Bõ\u0001\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u0014\u0012\n\b\u0003\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u0017\u0012\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0019\u001a\u0004\u0018\u00010\u0005¢\u0006\u0004\b\u001a\u0010\u001bJ\u0010\u00107\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001dJ\t\u00108\u001a\u00020\u0005HÆ\u0003J\u000b\u00109\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010;\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010<\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010>\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010?\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001dJ\u000b\u0010@\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010A\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001dJ\u000b\u0010B\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010D\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010E\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001dJ\u0010\u0010F\u001a\u0004\u0018\u00010\u0014HÆ\u0003¢\u0006\u0002\u0010/J\u000b\u0010G\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010H\u001a\u0004\u0018\u00010\u0017HÆ\u0003¢\u0006\u0002\u00103J\u0010\u0010I\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001dJ\u000b\u0010J\u001a\u0004\u0018\u00010\u0005HÆ\u0003Jü\u0001\u0010K\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0011\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0012\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u00142\n\b\u0003\u0010\u0015\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u00172\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0019\u001a\u0004\u0018\u00010\u0005HÆ\u0001¢\u0006\u0002\u0010LJ\u0014\u0010M\u001a\u00020\u00142\b\u0010N\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010O\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010P\u001a\u00020\u0005HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001e\u001a\u0004\b\u001c\u0010\u001dR\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b!\u0010 R\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\"\u0010 R\u0018\u0010\b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b#\u0010 R\u0018\u0010\t\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b$\u0010 R\u0018\u0010\n\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b%\u0010 R\u0018\u0010\u000b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b&\u0010 R\u001a\u0010\f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001e\u001a\u0004\b'\u0010\u001dR\u0018\u0010\r\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b(\u0010 R\u001a\u0010\u000e\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001e\u001a\u0004\b)\u0010\u001dR\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b*\u0010 R\u0018\u0010\u0010\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b+\u0010 R\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b,\u0010 R\u001a\u0010\u0012\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001e\u001a\u0004\b-\u0010\u001dR\u001a\u0010\u0013\u001a\u0004\u0018\u00010\u00148\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00100\u001a\u0004\b.\u0010/R\u0018\u0010\u0015\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b1\u0010 R\u001a\u0010\u0016\u001a\u0004\u0018\u00010\u00178\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00104\u001a\u0004\b2\u00103R\u001a\u0010\u0018\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001e\u001a\u0004\b5\u0010\u001dR\u0018\u0010\u0019\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b6\u0010 ¨\u0006Q"}, d2 = {"Lcom/cncverse/DoFlixProvider$CastMember;", "", "id", "", "name", "", "character", "profilePath", "biography", "birthday", "deathday", "placeOfBirth", "gender", "knownForDepartment", "tmdbId", "createdAt", "updatedAt", "metadata", "order", "adult", "", "originalName", "popularity", "", "castId", "creditId", "<init>", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;)V", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getName", "()Ljava/lang/String;", "getCharacter", "getProfilePath", "getBiography", "getBirthday", "getDeathday", "getPlaceOfBirth", "getGender", "getKnownForDepartment", "getTmdbId", "getCreatedAt", "getUpdatedAt", "getMetadata", "getOrder", "getAdult", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getOriginalName", "getPopularity", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getCastId", "getCreditId", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "copy", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;)Lcom/cncverse/DoFlixProvider$CastMember;", "equals", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class CastMember {

        @JsonProperty("adult")
        @Nullable
        private final Boolean adult;

        @JsonProperty("biography")
        @Nullable
        private final String biography;

        @JsonProperty("birthday")
        @Nullable
        private final String birthday;

        @JsonProperty("cast_id")
        @Nullable
        private final Integer castId;

        @JsonProperty("character")
        @Nullable
        private final String character;

        @JsonProperty("created_at")
        @Nullable
        private final String createdAt;

        @JsonProperty("credit_id")
        @Nullable
        private final String creditId;

        @JsonProperty("deathday")
        @Nullable
        private final String deathday;

        @JsonProperty("gender")
        @Nullable
        private final Integer gender;

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        @JsonProperty("known_for_department")
        @Nullable
        private final String knownForDepartment;

        @JsonProperty("metadata")
        @Nullable
        private final String metadata;

        @JsonProperty("name")
        @NotNull
        private final String name;

        @JsonProperty("order")
        @Nullable
        private final Integer order;

        @JsonProperty("original_name")
        @Nullable
        private final String originalName;

        @JsonProperty("place_of_birth")
        @Nullable
        private final String placeOfBirth;

        @JsonProperty("popularity")
        @Nullable
        private final Double popularity;

        @JsonProperty("profile_path")
        @Nullable
        private final String profilePath;

        @JsonProperty("tmdb_id")
        @Nullable
        private final Integer tmdbId;

        @JsonProperty("updated_at")
        @Nullable
        private final String updatedAt;

        public CastMember(@JsonProperty("id") @Nullable Integer num, @JsonProperty("name") @NotNull String str, @JsonProperty("character") @Nullable String str2, @JsonProperty("profile_path") @Nullable String str3, @JsonProperty("biography") @Nullable String str4, @JsonProperty("birthday") @Nullable String str5, @JsonProperty("deathday") @Nullable String str6, @JsonProperty("place_of_birth") @Nullable String str7, @JsonProperty("gender") @Nullable Integer num2, @JsonProperty("known_for_department") @Nullable String str8, @JsonProperty("tmdb_id") @Nullable Integer num3, @JsonProperty("created_at") @Nullable String str9, @JsonProperty("updated_at") @Nullable String str10, @JsonProperty("metadata") @Nullable String str11, @JsonProperty("order") @Nullable Integer num4, @JsonProperty("adult") @Nullable Boolean bool, @JsonProperty("original_name") @Nullable String str12, @JsonProperty("popularity") @Nullable Double d, @JsonProperty("cast_id") @Nullable Integer num5, @JsonProperty("credit_id") @Nullable String str13) {
            this.id = num;
            this.name = str;
            this.character = str2;
            this.profilePath = str3;
            this.biography = str4;
            this.birthday = str5;
            this.deathday = str6;
            this.placeOfBirth = str7;
            this.gender = num2;
            this.knownForDepartment = str8;
            this.tmdbId = num3;
            this.createdAt = str9;
            this.updatedAt = str10;
            this.metadata = str11;
            this.order = num4;
            this.adult = bool;
            this.originalName = str12;
            this.popularity = d;
            this.castId = num5;
            this.creditId = str13;
        }

        public /* synthetic */ CastMember(Integer num, String str, String str2, String str3, String str4, String str5, String str6, String str7, Integer num2, String str8, Integer num3, String str9, String str10, String str11, Integer num4, Boolean bool, String str12, Double d, Integer num5, String str13, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : num, str, str2, str3, (i & 16) != 0 ? null : str4, (i & 32) != 0 ? null : str5, (i & 64) != 0 ? null : str6, (i & 128) != 0 ? null : str7, (i & 256) != 0 ? null : num2, (i & 512) != 0 ? null : str8, (i & 1024) != 0 ? null : num3, (i & 2048) != 0 ? null : str9, (i & 4096) != 0 ? null : str10, (i & 8192) != 0 ? null : str11, (i & 16384) != 0 ? null : num4, (32768 & i) != 0 ? null : bool, (65536 & i) != 0 ? null : str12, (131072 & i) != 0 ? null : d, (262144 & i) != 0 ? null : num5, (i & 524288) != 0 ? null : str13);
        }

        public static /* synthetic */ CastMember copy$default(CastMember castMember, Integer num, String str, String str2, String str3, String str4, String str5, String str6, String str7, Integer num2, String str8, Integer num3, String str9, String str10, String str11, Integer num4, Boolean bool, String str12, Double d, Integer num5, String str13, int i, Object obj) {
            String str14;
            Integer num6;
            Integer num7 = (i & 1) != 0 ? castMember.id : num;
            String str15 = (i & 2) != 0 ? castMember.name : str;
            String str16 = (i & 4) != 0 ? castMember.character : str2;
            String str17 = (i & 8) != 0 ? castMember.profilePath : str3;
            String str18 = (i & 16) != 0 ? castMember.biography : str4;
            String str19 = (i & 32) != 0 ? castMember.birthday : str5;
            String str20 = (i & 64) != 0 ? castMember.deathday : str6;
            String str21 = (i & 128) != 0 ? castMember.placeOfBirth : str7;
            Integer num8 = (i & 256) != 0 ? castMember.gender : num2;
            String str22 = (i & 512) != 0 ? castMember.knownForDepartment : str8;
            Integer num9 = (i & 1024) != 0 ? castMember.tmdbId : num3;
            String str23 = (i & 2048) != 0 ? castMember.createdAt : str9;
            String str24 = (i & 4096) != 0 ? castMember.updatedAt : str10;
            String str25 = (i & 8192) != 0 ? castMember.metadata : str11;
            Integer num10 = num7;
            Integer num11 = (i & 16384) != 0 ? castMember.order : num4;
            Boolean bool2 = (i & 32768) != 0 ? castMember.adult : bool;
            String str26 = (i & 65536) != 0 ? castMember.originalName : str12;
            Double d2 = (i & 131072) != 0 ? castMember.popularity : d;
            Integer num12 = (i & 262144) != 0 ? castMember.castId : num5;
            if ((i & 524288) != 0) {
                num6 = num12;
                str14 = castMember.creditId;
            } else {
                str14 = str13;
                num6 = num12;
            }
            return castMember.copy(num10, str15, str16, str17, str18, str19, str20, str21, num8, str22, num9, str23, str24, str25, num11, bool2, str26, d2, num6, str14);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        /* renamed from: component10, reason: from getter */
        public final String getKnownForDepartment() {
            return this.knownForDepartment;
        }

        @Nullable
        /* renamed from: component11, reason: from getter */
        public final Integer getTmdbId() {
            return this.tmdbId;
        }

        @Nullable
        /* renamed from: component12, reason: from getter */
        public final String getCreatedAt() {
            return this.createdAt;
        }

        @Nullable
        /* renamed from: component13, reason: from getter */
        public final String getUpdatedAt() {
            return this.updatedAt;
        }

        @Nullable
        /* renamed from: component14, reason: from getter */
        public final String getMetadata() {
            return this.metadata;
        }

        @Nullable
        /* renamed from: component15, reason: from getter */
        public final Integer getOrder() {
            return this.order;
        }

        @Nullable
        /* renamed from: component16, reason: from getter */
        public final Boolean getAdult() {
            return this.adult;
        }

        @Nullable
        /* renamed from: component17, reason: from getter */
        public final String getOriginalName() {
            return this.originalName;
        }

        @Nullable
        /* renamed from: component18, reason: from getter */
        public final Double getPopularity() {
            return this.popularity;
        }

        @Nullable
        /* renamed from: component19, reason: from getter */
        public final Integer getCastId() {
            return this.castId;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @Nullable
        /* renamed from: component20, reason: from getter */
        public final String getCreditId() {
            return this.creditId;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final String getCharacter() {
            return this.character;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final String getProfilePath() {
            return this.profilePath;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getBiography() {
            return this.biography;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final String getBirthday() {
            return this.birthday;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final String getDeathday() {
            return this.deathday;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final String getPlaceOfBirth() {
            return this.placeOfBirth;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final Integer getGender() {
            return this.gender;
        }

        @NotNull
        public final CastMember copy(@JsonProperty("id") @Nullable Integer id, @JsonProperty("name") @NotNull String name, @JsonProperty("character") @Nullable String character, @JsonProperty("profile_path") @Nullable String profilePath, @JsonProperty("biography") @Nullable String biography, @JsonProperty("birthday") @Nullable String birthday, @JsonProperty("deathday") @Nullable String deathday, @JsonProperty("place_of_birth") @Nullable String placeOfBirth, @JsonProperty("gender") @Nullable Integer gender, @JsonProperty("known_for_department") @Nullable String knownForDepartment, @JsonProperty("tmdb_id") @Nullable Integer tmdbId, @JsonProperty("created_at") @Nullable String createdAt, @JsonProperty("updated_at") @Nullable String updatedAt, @JsonProperty("metadata") @Nullable String metadata, @JsonProperty("order") @Nullable Integer order, @JsonProperty("adult") @Nullable Boolean adult, @JsonProperty("original_name") @Nullable String originalName, @JsonProperty("popularity") @Nullable Double popularity, @JsonProperty("cast_id") @Nullable Integer castId, @JsonProperty("credit_id") @Nullable String creditId) {
            return new CastMember(id, name, character, profilePath, biography, birthday, deathday, placeOfBirth, gender, knownForDepartment, tmdbId, createdAt, updatedAt, metadata, order, adult, originalName, popularity, castId, creditId);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CastMember)) {
                return false;
            }
            CastMember castMember = (CastMember) other;
            return Intrinsics.areEqual(this.id, castMember.id) && Intrinsics.areEqual(this.name, castMember.name) && Intrinsics.areEqual(this.character, castMember.character) && Intrinsics.areEqual(this.profilePath, castMember.profilePath) && Intrinsics.areEqual(this.biography, castMember.biography) && Intrinsics.areEqual(this.birthday, castMember.birthday) && Intrinsics.areEqual(this.deathday, castMember.deathday) && Intrinsics.areEqual(this.placeOfBirth, castMember.placeOfBirth) && Intrinsics.areEqual(this.gender, castMember.gender) && Intrinsics.areEqual(this.knownForDepartment, castMember.knownForDepartment) && Intrinsics.areEqual(this.tmdbId, castMember.tmdbId) && Intrinsics.areEqual(this.createdAt, castMember.createdAt) && Intrinsics.areEqual(this.updatedAt, castMember.updatedAt) && Intrinsics.areEqual(this.metadata, castMember.metadata) && Intrinsics.areEqual(this.order, castMember.order) && Intrinsics.areEqual(this.adult, castMember.adult) && Intrinsics.areEqual(this.originalName, castMember.originalName) && Intrinsics.areEqual(this.popularity, castMember.popularity) && Intrinsics.areEqual(this.castId, castMember.castId) && Intrinsics.areEqual(this.creditId, castMember.creditId);
        }

        @Nullable
        public final Boolean getAdult() {
            return this.adult;
        }

        @Nullable
        public final String getBiography() {
            return this.biography;
        }

        @Nullable
        public final String getBirthday() {
            return this.birthday;
        }

        @Nullable
        public final Integer getCastId() {
            return this.castId;
        }

        @Nullable
        public final String getCharacter() {
            return this.character;
        }

        @Nullable
        public final String getCreatedAt() {
            return this.createdAt;
        }

        @Nullable
        public final String getCreditId() {
            return this.creditId;
        }

        @Nullable
        public final String getDeathday() {
            return this.deathday;
        }

        @Nullable
        public final Integer getGender() {
            return this.gender;
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        public final String getKnownForDepartment() {
            return this.knownForDepartment;
        }

        @Nullable
        public final String getMetadata() {
            return this.metadata;
        }

        @NotNull
        public final String getName() {
            return this.name;
        }

        @Nullable
        public final Integer getOrder() {
            return this.order;
        }

        @Nullable
        public final String getOriginalName() {
            return this.originalName;
        }

        @Nullable
        public final String getPlaceOfBirth() {
            return this.placeOfBirth;
        }

        @Nullable
        public final Double getPopularity() {
            return this.popularity;
        }

        @Nullable
        public final String getProfilePath() {
            return this.profilePath;
        }

        @Nullable
        public final Integer getTmdbId() {
            return this.tmdbId;
        }

        @Nullable
        public final String getUpdatedAt() {
            return this.updatedAt;
        }

        public int hashCode() {
            return ((((((((((((((((((((((((((((((((((((((this.id == null ? 0 : this.id.hashCode()) * 31) + this.name.hashCode()) * 31) + (this.character == null ? 0 : this.character.hashCode())) * 31) + (this.profilePath == null ? 0 : this.profilePath.hashCode())) * 31) + (this.biography == null ? 0 : this.biography.hashCode())) * 31) + (this.birthday == null ? 0 : this.birthday.hashCode())) * 31) + (this.deathday == null ? 0 : this.deathday.hashCode())) * 31) + (this.placeOfBirth == null ? 0 : this.placeOfBirth.hashCode())) * 31) + (this.gender == null ? 0 : this.gender.hashCode())) * 31) + (this.knownForDepartment == null ? 0 : this.knownForDepartment.hashCode())) * 31) + (this.tmdbId == null ? 0 : this.tmdbId.hashCode())) * 31) + (this.createdAt == null ? 0 : this.createdAt.hashCode())) * 31) + (this.updatedAt == null ? 0 : this.updatedAt.hashCode())) * 31) + (this.metadata == null ? 0 : this.metadata.hashCode())) * 31) + (this.order == null ? 0 : this.order.hashCode())) * 31) + (this.adult == null ? 0 : this.adult.hashCode())) * 31) + (this.originalName == null ? 0 : this.originalName.hashCode())) * 31) + (this.popularity == null ? 0 : this.popularity.hashCode())) * 31) + (this.castId == null ? 0 : this.castId.hashCode())) * 31) + (this.creditId != null ? this.creditId.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "CastMember(id=" + this.id + ", name=" + this.name + ", character=" + this.character + ", profilePath=" + this.profilePath + ", biography=" + this.biography + ", birthday=" + this.birthday + ", deathday=" + this.deathday + ", placeOfBirth=" + this.placeOfBirth + ", gender=" + this.gender + ", knownForDepartment=" + this.knownForDepartment + ", tmdbId=" + this.tmdbId + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", metadata=" + this.metadata + ", order=" + this.order + ", adult=" + this.adult + ", originalName=" + this.originalName + ", popularity=" + this.popularity + ", castId=" + this.castId + ", creditId=" + this.creditId + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @Metadata(d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t¨\u0006\n"}, d2 = {"Lcom/cncverse/DoFlixProvider$Companion;", "", "<init>", "()V", "context", "Landroid/content/Context;", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @Nullable
        public final Context getContext() {
            return DoFlixProvider.context;
        }

        public final void setContext(@Nullable Context context) {
            DoFlixProvider.context = context;
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B3\u0012\u000e\b\u0001\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0001\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\b¢\u0006\u0004\b\t\u0010\nJ\u000f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003HÆ\u0003J\u0010\u0010\u0013\u001a\u0004\u0018\u00010\bHÆ\u0003¢\u0006\u0002\u0010\u000fJ:\u0010\u0014\u001a\u00020\u00002\u000e\b\u0003\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0003\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u00032\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\bHÆ\u0001¢\u0006\u0002\u0010\u0015J\u0014\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0019\u001a\u00020\bHÖ\u0081\u0004J\n\u0010\u001a\u001a\u00020\u001bHÖ\u0081\u0004R\u001c\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001c\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u001a\u0010\u0007\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0010\u001a\u0004\b\u000e\u0010\u000f¨\u0006\u001c"}, d2 = {"Lcom/cncverse/DoFlixProvider$Credits;", "", "cast", "", "Lcom/cncverse/DoFlixProvider$CastMember;", "crew", "Lcom/cncverse/DoFlixProvider$CrewMember;", "id", "", "<init>", "(Ljava/util/List;Ljava/util/List;Ljava/lang/Integer;)V", "getCast", "()Ljava/util/List;", "getCrew", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "component1", "component2", "component3", "copy", "(Ljava/util/List;Ljava/util/List;Ljava/lang/Integer;)Lcom/cncverse/DoFlixProvider$Credits;", "equals", "", "other", "hashCode", "toString", "", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class Credits {

        @JsonProperty("cast")
        @NotNull
        private final List<CastMember> cast;

        @JsonProperty("crew")
        @NotNull
        private final List<CrewMember> crew;

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        public Credits(@JsonProperty("cast") @NotNull List<CastMember> list, @JsonProperty("crew") @NotNull List<CrewMember> list2, @JsonProperty("id") @Nullable Integer num) {
            this.cast = list;
            this.crew = list2;
            this.id = num;
        }

        public /* synthetic */ Credits(List list, List list2, Integer num, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(list, list2, (i & 4) != 0 ? null : num);
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ Credits copy$default(Credits credits, List list, List list2, Integer num, int i, Object obj) {
            if ((i & 1) != 0) {
                list = credits.cast;
            }
            if ((i & 2) != 0) {
                list2 = credits.crew;
            }
            if ((i & 4) != 0) {
                num = credits.id;
            }
            return credits.copy(list, list2, num);
        }

        @NotNull
        public final List<CastMember> component1() {
            return this.cast;
        }

        @NotNull
        public final List<CrewMember> component2() {
            return this.crew;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @NotNull
        public final Credits copy(@JsonProperty("cast") @NotNull List<CastMember> cast, @JsonProperty("crew") @NotNull List<CrewMember> crew, @JsonProperty("id") @Nullable Integer id) {
            return new Credits(cast, crew, id);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Credits)) {
                return false;
            }
            Credits credits = (Credits) other;
            return Intrinsics.areEqual(this.cast, credits.cast) && Intrinsics.areEqual(this.crew, credits.crew) && Intrinsics.areEqual(this.id, credits.id);
        }

        @NotNull
        public final List<CastMember> getCast() {
            return this.cast;
        }

        @NotNull
        public final List<CrewMember> getCrew() {
            return this.crew;
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        public int hashCode() {
            return (((this.cast.hashCode() * 31) + this.crew.hashCode()) * 31) + (this.id == null ? 0 : this.id.hashCode());
        }

        @NotNull
        public String toString() {
            return "Credits(cast=" + this.cast + ", crew=" + this.crew + ", id=" + this.id + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b7\b\u0087\b\u0018\u00002\u00020\u0001Bé\u0001\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u0014\u0012\n\b\u0003\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u0017\u0012\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u0005¢\u0006\u0004\b\u0019\u0010\u001aJ\u0010\u00105\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001cJ\t\u00106\u001a\u00020\u0005HÆ\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u00108\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u00109\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010;\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010<\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010>\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001cJ\u000b\u0010?\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010@\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001cJ\u000b\u0010A\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010B\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010D\u001a\u0004\u0018\u00010\u0014HÆ\u0003¢\u0006\u0002\u0010.J\u000b\u0010E\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010F\u001a\u0004\u0018\u00010\u0017HÆ\u0003¢\u0006\u0002\u00102J\u000b\u0010G\u001a\u0004\u0018\u00010\u0005HÆ\u0003Jð\u0001\u0010H\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0011\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0012\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u00142\n\b\u0003\u0010\u0015\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u00172\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u0005HÆ\u0001¢\u0006\u0002\u0010IJ\u0014\u0010J\u001a\u00020\u00142\b\u0010K\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010L\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010M\u001a\u00020\u0005HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001d\u001a\u0004\b\u001b\u0010\u001cR\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b \u0010\u001fR\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001fR\u0018\u0010\b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001fR\u0018\u0010\t\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001fR\u0018\u0010\n\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001fR\u0018\u0010\u000b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001fR\u0018\u0010\f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001fR\u001a\u0010\r\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001d\u001a\u0004\b'\u0010\u001cR\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001fR\u001a\u0010\u000f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001d\u001a\u0004\b)\u0010\u001cR\u0018\u0010\u0010\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001fR\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001fR\u0018\u0010\u0012\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001fR\u001a\u0010\u0013\u001a\u0004\u0018\u00010\u00148\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010/\u001a\u0004\b-\u0010.R\u0018\u0010\u0015\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b0\u0010\u001fR\u001a\u0010\u0016\u001a\u0004\u0018\u00010\u00178\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00103\u001a\u0004\b1\u00102R\u0018\u0010\u0018\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b4\u0010\u001f¨\u0006N"}, d2 = {"Lcom/cncverse/DoFlixProvider$CrewMember;", "", "id", "", "name", "", "job", "department", "profilePath", "biography", "birthday", "deathday", "placeOfBirth", "gender", "knownForDepartment", "tmdbId", "createdAt", "updatedAt", "metadata", "adult", "", "originalName", "popularity", "", "creditId", "<init>", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;)V", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getName", "()Ljava/lang/String;", "getJob", "getDepartment", "getProfilePath", "getBiography", "getBirthday", "getDeathday", "getPlaceOfBirth", "getGender", "getKnownForDepartment", "getTmdbId", "getCreatedAt", "getUpdatedAt", "getMetadata", "getAdult", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getOriginalName", "getPopularity", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getCreditId", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "copy", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;)Lcom/cncverse/DoFlixProvider$CrewMember;", "equals", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class CrewMember {

        @JsonProperty("adult")
        @Nullable
        private final Boolean adult;

        @JsonProperty("biography")
        @Nullable
        private final String biography;

        @JsonProperty("birthday")
        @Nullable
        private final String birthday;

        @JsonProperty("created_at")
        @Nullable
        private final String createdAt;

        @JsonProperty("credit_id")
        @Nullable
        private final String creditId;

        @JsonProperty("deathday")
        @Nullable
        private final String deathday;

        @JsonProperty("department")
        @Nullable
        private final String department;

        @JsonProperty("gender")
        @Nullable
        private final Integer gender;

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        @JsonProperty("job")
        @Nullable
        private final String job;

        @JsonProperty("known_for_department")
        @Nullable
        private final String knownForDepartment;

        @JsonProperty("metadata")
        @Nullable
        private final String metadata;

        @JsonProperty("name")
        @NotNull
        private final String name;

        @JsonProperty("original_name")
        @Nullable
        private final String originalName;

        @JsonProperty("place_of_birth")
        @Nullable
        private final String placeOfBirth;

        @JsonProperty("popularity")
        @Nullable
        private final Double popularity;

        @JsonProperty("profile_path")
        @Nullable
        private final String profilePath;

        @JsonProperty("tmdb_id")
        @Nullable
        private final Integer tmdbId;

        @JsonProperty("updated_at")
        @Nullable
        private final String updatedAt;

        public CrewMember(@JsonProperty("id") @Nullable Integer num, @JsonProperty("name") @NotNull String str, @JsonProperty("job") @Nullable String str2, @JsonProperty("department") @Nullable String str3, @JsonProperty("profile_path") @Nullable String str4, @JsonProperty("biography") @Nullable String str5, @JsonProperty("birthday") @Nullable String str6, @JsonProperty("deathday") @Nullable String str7, @JsonProperty("place_of_birth") @Nullable String str8, @JsonProperty("gender") @Nullable Integer num2, @JsonProperty("known_for_department") @Nullable String str9, @JsonProperty("tmdb_id") @Nullable Integer num3, @JsonProperty("created_at") @Nullable String str10, @JsonProperty("updated_at") @Nullable String str11, @JsonProperty("metadata") @Nullable String str12, @JsonProperty("adult") @Nullable Boolean bool, @JsonProperty("original_name") @Nullable String str13, @JsonProperty("popularity") @Nullable Double d, @JsonProperty("credit_id") @Nullable String str14) {
            this.id = num;
            this.name = str;
            this.job = str2;
            this.department = str3;
            this.profilePath = str4;
            this.biography = str5;
            this.birthday = str6;
            this.deathday = str7;
            this.placeOfBirth = str8;
            this.gender = num2;
            this.knownForDepartment = str9;
            this.tmdbId = num3;
            this.createdAt = str10;
            this.updatedAt = str11;
            this.metadata = str12;
            this.adult = bool;
            this.originalName = str13;
            this.popularity = d;
            this.creditId = str14;
        }

        public /* synthetic */ CrewMember(Integer num, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, Integer num2, String str9, Integer num3, String str10, String str11, String str12, Boolean bool, String str13, Double d, String str14, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : num, str, str2, str3, (i & 16) != 0 ? null : str4, (i & 32) != 0 ? null : str5, (i & 64) != 0 ? null : str6, (i & 128) != 0 ? null : str7, (i & 256) != 0 ? null : str8, (i & 512) != 0 ? null : num2, (i & 1024) != 0 ? null : str9, (i & 2048) != 0 ? null : num3, (i & 4096) != 0 ? null : str10, (i & 8192) != 0 ? null : str11, (i & 16384) != 0 ? null : str12, (32768 & i) != 0 ? null : bool, (65536 & i) != 0 ? null : str13, (131072 & i) != 0 ? null : d, (i & 262144) != 0 ? null : str14);
        }

        public static /* synthetic */ CrewMember copy$default(CrewMember crewMember, Integer num, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, Integer num2, String str9, Integer num3, String str10, String str11, String str12, Boolean bool, String str13, Double d, String str14, int i, Object obj) {
            String str15;
            Double d2;
            Integer num4 = (i & 1) != 0 ? crewMember.id : num;
            String str16 = (i & 2) != 0 ? crewMember.name : str;
            String str17 = (i & 4) != 0 ? crewMember.job : str2;
            String str18 = (i & 8) != 0 ? crewMember.department : str3;
            String str19 = (i & 16) != 0 ? crewMember.profilePath : str4;
            String str20 = (i & 32) != 0 ? crewMember.biography : str5;
            String str21 = (i & 64) != 0 ? crewMember.birthday : str6;
            String str22 = (i & 128) != 0 ? crewMember.deathday : str7;
            String str23 = (i & 256) != 0 ? crewMember.placeOfBirth : str8;
            Integer num5 = (i & 512) != 0 ? crewMember.gender : num2;
            String str24 = (i & 1024) != 0 ? crewMember.knownForDepartment : str9;
            Integer num6 = (i & 2048) != 0 ? crewMember.tmdbId : num3;
            String str25 = (i & 4096) != 0 ? crewMember.createdAt : str10;
            String str26 = (i & 8192) != 0 ? crewMember.updatedAt : str11;
            Integer num7 = num4;
            String str27 = (i & 16384) != 0 ? crewMember.metadata : str12;
            Boolean bool2 = (i & 32768) != 0 ? crewMember.adult : bool;
            String str28 = (i & 65536) != 0 ? crewMember.originalName : str13;
            Double d3 = (i & 131072) != 0 ? crewMember.popularity : d;
            if ((i & 262144) != 0) {
                d2 = d3;
                str15 = crewMember.creditId;
            } else {
                str15 = str14;
                d2 = d3;
            }
            return crewMember.copy(num7, str16, str17, str18, str19, str20, str21, str22, str23, num5, str24, num6, str25, str26, str27, bool2, str28, d2, str15);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        /* renamed from: component10, reason: from getter */
        public final Integer getGender() {
            return this.gender;
        }

        @Nullable
        /* renamed from: component11, reason: from getter */
        public final String getKnownForDepartment() {
            return this.knownForDepartment;
        }

        @Nullable
        /* renamed from: component12, reason: from getter */
        public final Integer getTmdbId() {
            return this.tmdbId;
        }

        @Nullable
        /* renamed from: component13, reason: from getter */
        public final String getCreatedAt() {
            return this.createdAt;
        }

        @Nullable
        /* renamed from: component14, reason: from getter */
        public final String getUpdatedAt() {
            return this.updatedAt;
        }

        @Nullable
        /* renamed from: component15, reason: from getter */
        public final String getMetadata() {
            return this.metadata;
        }

        @Nullable
        /* renamed from: component16, reason: from getter */
        public final Boolean getAdult() {
            return this.adult;
        }

        @Nullable
        /* renamed from: component17, reason: from getter */
        public final String getOriginalName() {
            return this.originalName;
        }

        @Nullable
        /* renamed from: component18, reason: from getter */
        public final Double getPopularity() {
            return this.popularity;
        }

        @Nullable
        /* renamed from: component19, reason: from getter */
        public final String getCreditId() {
            return this.creditId;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final String getJob() {
            return this.job;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final String getDepartment() {
            return this.department;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getProfilePath() {
            return this.profilePath;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final String getBiography() {
            return this.biography;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final String getBirthday() {
            return this.birthday;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final String getDeathday() {
            return this.deathday;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final String getPlaceOfBirth() {
            return this.placeOfBirth;
        }

        @NotNull
        public final CrewMember copy(@JsonProperty("id") @Nullable Integer id, @JsonProperty("name") @NotNull String name, @JsonProperty("job") @Nullable String job, @JsonProperty("department") @Nullable String department, @JsonProperty("profile_path") @Nullable String profilePath, @JsonProperty("biography") @Nullable String biography, @JsonProperty("birthday") @Nullable String birthday, @JsonProperty("deathday") @Nullable String deathday, @JsonProperty("place_of_birth") @Nullable String placeOfBirth, @JsonProperty("gender") @Nullable Integer gender, @JsonProperty("known_for_department") @Nullable String knownForDepartment, @JsonProperty("tmdb_id") @Nullable Integer tmdbId, @JsonProperty("created_at") @Nullable String createdAt, @JsonProperty("updated_at") @Nullable String updatedAt, @JsonProperty("metadata") @Nullable String metadata, @JsonProperty("adult") @Nullable Boolean adult, @JsonProperty("original_name") @Nullable String originalName, @JsonProperty("popularity") @Nullable Double popularity, @JsonProperty("credit_id") @Nullable String creditId) {
            return new CrewMember(id, name, job, department, profilePath, biography, birthday, deathday, placeOfBirth, gender, knownForDepartment, tmdbId, createdAt, updatedAt, metadata, adult, originalName, popularity, creditId);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CrewMember)) {
                return false;
            }
            CrewMember crewMember = (CrewMember) other;
            return Intrinsics.areEqual(this.id, crewMember.id) && Intrinsics.areEqual(this.name, crewMember.name) && Intrinsics.areEqual(this.job, crewMember.job) && Intrinsics.areEqual(this.department, crewMember.department) && Intrinsics.areEqual(this.profilePath, crewMember.profilePath) && Intrinsics.areEqual(this.biography, crewMember.biography) && Intrinsics.areEqual(this.birthday, crewMember.birthday) && Intrinsics.areEqual(this.deathday, crewMember.deathday) && Intrinsics.areEqual(this.placeOfBirth, crewMember.placeOfBirth) && Intrinsics.areEqual(this.gender, crewMember.gender) && Intrinsics.areEqual(this.knownForDepartment, crewMember.knownForDepartment) && Intrinsics.areEqual(this.tmdbId, crewMember.tmdbId) && Intrinsics.areEqual(this.createdAt, crewMember.createdAt) && Intrinsics.areEqual(this.updatedAt, crewMember.updatedAt) && Intrinsics.areEqual(this.metadata, crewMember.metadata) && Intrinsics.areEqual(this.adult, crewMember.adult) && Intrinsics.areEqual(this.originalName, crewMember.originalName) && Intrinsics.areEqual(this.popularity, crewMember.popularity) && Intrinsics.areEqual(this.creditId, crewMember.creditId);
        }

        @Nullable
        public final Boolean getAdult() {
            return this.adult;
        }

        @Nullable
        public final String getBiography() {
            return this.biography;
        }

        @Nullable
        public final String getBirthday() {
            return this.birthday;
        }

        @Nullable
        public final String getCreatedAt() {
            return this.createdAt;
        }

        @Nullable
        public final String getCreditId() {
            return this.creditId;
        }

        @Nullable
        public final String getDeathday() {
            return this.deathday;
        }

        @Nullable
        public final String getDepartment() {
            return this.department;
        }

        @Nullable
        public final Integer getGender() {
            return this.gender;
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        public final String getJob() {
            return this.job;
        }

        @Nullable
        public final String getKnownForDepartment() {
            return this.knownForDepartment;
        }

        @Nullable
        public final String getMetadata() {
            return this.metadata;
        }

        @NotNull
        public final String getName() {
            return this.name;
        }

        @Nullable
        public final String getOriginalName() {
            return this.originalName;
        }

        @Nullable
        public final String getPlaceOfBirth() {
            return this.placeOfBirth;
        }

        @Nullable
        public final Double getPopularity() {
            return this.popularity;
        }

        @Nullable
        public final String getProfilePath() {
            return this.profilePath;
        }

        @Nullable
        public final Integer getTmdbId() {
            return this.tmdbId;
        }

        @Nullable
        public final String getUpdatedAt() {
            return this.updatedAt;
        }

        public int hashCode() {
            return ((((((((((((((((((((((((((((((((((((this.id == null ? 0 : this.id.hashCode()) * 31) + this.name.hashCode()) * 31) + (this.job == null ? 0 : this.job.hashCode())) * 31) + (this.department == null ? 0 : this.department.hashCode())) * 31) + (this.profilePath == null ? 0 : this.profilePath.hashCode())) * 31) + (this.biography == null ? 0 : this.biography.hashCode())) * 31) + (this.birthday == null ? 0 : this.birthday.hashCode())) * 31) + (this.deathday == null ? 0 : this.deathday.hashCode())) * 31) + (this.placeOfBirth == null ? 0 : this.placeOfBirth.hashCode())) * 31) + (this.gender == null ? 0 : this.gender.hashCode())) * 31) + (this.knownForDepartment == null ? 0 : this.knownForDepartment.hashCode())) * 31) + (this.tmdbId == null ? 0 : this.tmdbId.hashCode())) * 31) + (this.createdAt == null ? 0 : this.createdAt.hashCode())) * 31) + (this.updatedAt == null ? 0 : this.updatedAt.hashCode())) * 31) + (this.metadata == null ? 0 : this.metadata.hashCode())) * 31) + (this.adult == null ? 0 : this.adult.hashCode())) * 31) + (this.originalName == null ? 0 : this.originalName.hashCode())) * 31) + (this.popularity == null ? 0 : this.popularity.hashCode())) * 31) + (this.creditId != null ? this.creditId.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "CrewMember(id=" + this.id + ", name=" + this.name + ", job=" + this.job + ", department=" + this.department + ", profilePath=" + this.profilePath + ", biography=" + this.biography + ", birthday=" + this.birthday + ", deathday=" + this.deathday + ", placeOfBirth=" + this.placeOfBirth + ", gender=" + this.gender + ", knownForDepartment=" + this.knownForDepartment + ", tmdbId=" + this.tmdbId + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", metadata=" + this.metadata + ", adult=" + this.adult + ", originalName=" + this.originalName + ", popularity=" + this.popularity + ", creditId=" + this.creditId + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B#\u0012\u000e\b\u0001\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006¢\u0006\u0004\b\u0007\u0010\bJ\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\fJ*\u0010\u0010\u001a\u00020\u00002\u000e\b\u0003\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006HÆ\u0001¢\u0006\u0002\u0010\u0011J\u0014\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0015\u001a\u00020\u0006HÖ\u0081\u0004J\n\u0010\u0016\u001a\u00020\u0017HÖ\u0081\u0004R\u001c\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001a\u0010\u0005\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\r\u001a\u0004\b\u000b\u0010\f¨\u0006\u0018"}, d2 = {"Lcom/cncverse/DoFlixProvider$EpisodeLinksResponse;", "", "results", "", "Lcom/cncverse/DoFlixProvider$StreamLink;", "id", "", "<init>", "(Ljava/util/List;Ljava/lang/Integer;)V", "getResults", "()Ljava/util/List;", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "component1", "component2", "copy", "(Ljava/util/List;Ljava/lang/Integer;)Lcom/cncverse/DoFlixProvider$EpisodeLinksResponse;", "equals", "", "other", "hashCode", "toString", "", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class EpisodeLinksResponse {

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        @JsonProperty("results")
        @NotNull
        private final List<StreamLink> results;

        public EpisodeLinksResponse(@JsonProperty("results") @NotNull List<StreamLink> list, @JsonProperty("id") @Nullable Integer num) {
            this.results = list;
            this.id = num;
        }

        public /* synthetic */ EpisodeLinksResponse(List list, Integer num, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(list, (i & 2) != 0 ? null : num);
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ EpisodeLinksResponse copy$default(EpisodeLinksResponse episodeLinksResponse, List list, Integer num, int i, Object obj) {
            if ((i & 1) != 0) {
                list = episodeLinksResponse.results;
            }
            if ((i & 2) != 0) {
                num = episodeLinksResponse.id;
            }
            return episodeLinksResponse.copy(list, num);
        }

        @NotNull
        public final List<StreamLink> component1() {
            return this.results;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @NotNull
        public final EpisodeLinksResponse copy(@JsonProperty("results") @NotNull List<StreamLink> results, @JsonProperty("id") @Nullable Integer id) {
            return new EpisodeLinksResponse(results, id);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof EpisodeLinksResponse)) {
                return false;
            }
            EpisodeLinksResponse episodeLinksResponse = (EpisodeLinksResponse) other;
            return Intrinsics.areEqual(this.results, episodeLinksResponse.results) && Intrinsics.areEqual(this.id, episodeLinksResponse.id);
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        @NotNull
        public final List<StreamLink> getResults() {
            return this.results;
        }

        public int hashCode() {
            return (this.results.hashCode() * 31) + (this.id == null ? 0 : this.id.hashCode());
        }

        @NotNull
        public String toString() {
            return "EpisodeLinksResponse(results=" + this.results + ", id=" + this.id + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B5\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0001\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0001\u0010\b\u001a\u00020\u0003¢\u0006\u0004\b\t\u0010\nJ\t\u0010\u0011\u001a\u00020\u0003HÆ\u0003J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005HÆ\u0003J\t\u0010\u0013\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0014\u001a\u00020\u0003HÆ\u0003J7\u0010\u0015\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\u000e\b\u0003\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0003\u0010\u0007\u001a\u00020\u00032\b\b\u0003\u0010\b\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0019\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010\u001a\u001a\u00020\u001bHÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001c\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u0007\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\fR\u0016\u0010\b\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\f¨\u0006\u001c"}, d2 = {"Lcom/cncverse/DoFlixProvider$HomePageApiResponse;", "", "page", "", "results", "", "Lcom/cncverse/DoFlixProvider$MovieResult;", "totalPages", "totalResults", "<init>", "(ILjava/util/List;II)V", "getPage", "()I", "getResults", "()Ljava/util/List;", "getTotalPages", "getTotalResults", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "toString", "", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class HomePageApiResponse {

        @JsonProperty("page")
        private final int page;

        @JsonProperty("results")
        @NotNull
        private final List<MovieResult> results;

        @JsonProperty("total_pages")
        private final int totalPages;

        @JsonProperty("total_results")
        private final int totalResults;

        public HomePageApiResponse(@JsonProperty("page") int i, @JsonProperty("results") @NotNull List<MovieResult> list, @JsonProperty("total_pages") int i2, @JsonProperty("total_results") int i3) {
            this.page = i;
            this.results = list;
            this.totalPages = i2;
            this.totalResults = i3;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ HomePageApiResponse copy$default(HomePageApiResponse homePageApiResponse, int i, List list, int i2, int i3, int i4, Object obj) {
            if ((i4 & 1) != 0) {
                i = homePageApiResponse.page;
            }
            if ((i4 & 2) != 0) {
                list = homePageApiResponse.results;
            }
            if ((i4 & 4) != 0) {
                i2 = homePageApiResponse.totalPages;
            }
            if ((i4 & 8) != 0) {
                i3 = homePageApiResponse.totalResults;
            }
            return homePageApiResponse.copy(i, list, i2, i3);
        }

        /* renamed from: component1, reason: from getter */
        public final int getPage() {
            return this.page;
        }

        @NotNull
        public final List<MovieResult> component2() {
            return this.results;
        }

        /* renamed from: component3, reason: from getter */
        public final int getTotalPages() {
            return this.totalPages;
        }

        /* renamed from: component4, reason: from getter */
        public final int getTotalResults() {
            return this.totalResults;
        }

        @NotNull
        public final HomePageApiResponse copy(@JsonProperty("page") int page, @JsonProperty("results") @NotNull List<MovieResult> results, @JsonProperty("total_pages") int totalPages, @JsonProperty("total_results") int totalResults) {
            return new HomePageApiResponse(page, results, totalPages, totalResults);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof HomePageApiResponse)) {
                return false;
            }
            HomePageApiResponse homePageApiResponse = (HomePageApiResponse) other;
            return this.page == homePageApiResponse.page && Intrinsics.areEqual(this.results, homePageApiResponse.results) && this.totalPages == homePageApiResponse.totalPages && this.totalResults == homePageApiResponse.totalResults;
        }

        public final int getPage() {
            return this.page;
        }

        @NotNull
        public final List<MovieResult> getResults() {
            return this.results;
        }

        public final int getTotalPages() {
            return this.totalPages;
        }

        public final int getTotalResults() {
            return this.totalResults;
        }

        public int hashCode() {
            return (((((this.page * 31) + this.results.hashCode()) * 31) + this.totalPages) * 31) + this.totalResults;
        }

        @NotNull
        public String toString() {
            return "HomePageApiResponse(page=" + this.page + ", results=" + this.results + ", totalPages=" + this.totalPages + ", totalResults=" + this.totalResults + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\bF\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B±\u0002\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0001\u0010\b\u001a\u00020\u0003\u0012\b\b\u0001\u0010\t\u001a\u00020\u0005\u0012\b\b\u0001\u0010\n\u001a\u00020\u0005\u0012\u000e\b\u0001\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f\u0012\n\b\u0001\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0001\u0010\u0011\u001a\u00020\u0012\u0012\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\u0010\b\u0003\u0010\u0016\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\f\u0012\n\b\u0003\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u0012\u0010\b\u0003\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u001c\u0018\u00010\f\u0012\n\b\u0003\u0010\u001d\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u001f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010 \u001a\u0004\u0018\u00010\u001a\u0012\n\b\u0003\u0010!\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\"\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b#\u0010$J\t\u0010G\u001a\u00020\u0003HÆ\u0003J\t\u0010H\u001a\u00020\u0005HÆ\u0003J\t\u0010I\u001a\u00020\u0005HÆ\u0003J\t\u0010J\u001a\u00020\u0005HÆ\u0003J\t\u0010K\u001a\u00020\u0003HÆ\u0003J\t\u0010L\u001a\u00020\u0005HÆ\u0003J\t\u0010M\u001a\u00020\u0005HÆ\u0003J\u000f\u0010N\u001a\b\u0012\u0004\u0012\u00020\r0\fHÆ\u0003J\u000b\u0010O\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\t\u0010P\u001a\u00020\u0010HÆ\u0003J\t\u0010Q\u001a\u00020\u0012HÆ\u0003J\u0010\u0010R\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u00106J\u000b\u0010S\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010T\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0011\u0010U\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\fHÆ\u0003J\u000b\u0010V\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010W\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010X\u001a\u0004\u0018\u00010\u001aHÆ\u0003¢\u0006\u0002\u0010>J\u0011\u0010Y\u001a\n\u0012\u0004\u0012\u00020\u001c\u0018\u00010\fHÆ\u0003J\u0010\u0010Z\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u00106J\u000b\u0010[\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010\\\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010]\u001a\u0004\u0018\u00010\u001aHÆ\u0003¢\u0006\u0002\u0010>J\u0010\u0010^\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u00106J\u0010\u0010_\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u00106J¸\u0002\u0010`\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\b\b\u0003\u0010\u0006\u001a\u00020\u00052\b\b\u0003\u0010\u0007\u001a\u00020\u00052\b\b\u0003\u0010\b\u001a\u00020\u00032\b\b\u0003\u0010\t\u001a\u00020\u00052\b\b\u0003\u0010\n\u001a\u00020\u00052\u000e\b\u0003\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u00052\b\b\u0003\u0010\u000f\u001a\u00020\u00102\b\b\u0003\u0010\u0011\u001a\u00020\u00122\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0014\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0015\u001a\u0004\u0018\u00010\u00052\u0010\b\u0003\u0010\u0016\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\f2\n\b\u0003\u0010\u0017\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0010\b\u0003\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u001c\u0018\u00010\f2\n\b\u0003\u0010\u001d\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u001f\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010 \u001a\u0004\u0018\u00010\u001a2\n\b\u0003\u0010!\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\"\u001a\u0004\u0018\u00010\u0003HÆ\u0001¢\u0006\u0002\u0010aJ\u0014\u0010b\u001a\u00020c2\b\u0010d\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010e\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010f\u001a\u00020\u0005HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b'\u0010(R\u0016\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b)\u0010(R\u0016\u0010\u0007\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b*\u0010(R\u0016\u0010\b\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b+\u0010&R\u0016\u0010\t\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b,\u0010(R\u0016\u0010\n\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b-\u0010(R\u001c\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b.\u0010/R\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b0\u0010(R\u0016\u0010\u000f\u001a\u00020\u00108\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b1\u00102R\u0016\u0010\u0011\u001a\u00020\u00128\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b3\u00104R\u001a\u0010\u0013\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00107\u001a\u0004\b5\u00106R\u0018\u0010\u0014\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b8\u0010(R\u0018\u0010\u0015\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b9\u0010(R\u001e\u0010\u0016\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\f8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b:\u0010/R\u0018\u0010\u0017\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b;\u0010(R\u0018\u0010\u0018\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b<\u0010(R\u001a\u0010\u0019\u001a\u0004\u0018\u00010\u001a8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010?\u001a\u0004\b=\u0010>R\u001e\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u001c\u0018\u00010\f8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b@\u0010/R\u001a\u0010\u001d\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00107\u001a\u0004\bA\u00106R\u0018\u0010\u001e\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bB\u0010(R\u0018\u0010\u001f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bC\u0010(R\u001a\u0010 \u001a\u0004\u0018\u00010\u001a8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010?\u001a\u0004\bD\u0010>R\u001a\u0010!\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00107\u001a\u0004\bE\u00106R\u001a\u0010\"\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00107\u001a\u0004\bF\u00106¨\u0006g"}, d2 = {"Lcom/cncverse/DoFlixProvider$MovieDetails;", "", "id", "", "title", "", "overview", "releaseDate", "runtime", "posterPath", "backdropPath", "genres", "", "Lcom/cncverse/DoFlixProvider$TMDBGenre;", "customPosterTag", "credits", "Lcom/cncverse/DoFlixProvider$Credits;", "similar", "Lcom/cncverse/DoFlixProvider$SimilarResults;", "budget", "homepage", "imdbId", "originCountry", "originalLanguage", "originalTitle", "popularity", "", "productionCountries", "Lcom/cncverse/DoFlixProvider$ProductionCountry;", "revenue", "status", "tagline", "voteAverage", "voteCount", "publish", "<init>", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Lcom/cncverse/DoFlixProvider$Credits;Lcom/cncverse/DoFlixProvider$SimilarResults;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/util/List;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/Integer;)V", "getId", "()I", "getTitle", "()Ljava/lang/String;", "getOverview", "getReleaseDate", "getRuntime", "getPosterPath", "getBackdropPath", "getGenres", "()Ljava/util/List;", "getCustomPosterTag", "getCredits", "()Lcom/cncverse/DoFlixProvider$Credits;", "getSimilar", "()Lcom/cncverse/DoFlixProvider$SimilarResults;", "getBudget", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getHomepage", "getImdbId", "getOriginCountry", "getOriginalLanguage", "getOriginalTitle", "getPopularity", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getProductionCountries", "getRevenue", "getStatus", "getTagline", "getVoteAverage", "getVoteCount", "getPublish", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "component22", "component23", "component24", "component25", "copy", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Lcom/cncverse/DoFlixProvider$Credits;Lcom/cncverse/DoFlixProvider$SimilarResults;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/util/List;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/Integer;)Lcom/cncverse/DoFlixProvider$MovieDetails;", "equals", "", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class MovieDetails {

        @JsonProperty("backdrop_path")
        @NotNull
        private final String backdropPath;

        @JsonProperty("budget")
        @Nullable
        private final Integer budget;

        @JsonProperty("credits")
        @NotNull
        private final Credits credits;

        @JsonProperty("custom_poster_tag")
        @Nullable
        private final String customPosterTag;

        @JsonProperty("genres")
        @NotNull
        private final List<TMDBGenre> genres;

        @JsonProperty("homepage")
        @Nullable
        private final String homepage;

        @JsonProperty("id")
        private final int id;

        @JsonProperty("imdb_id")
        @Nullable
        private final String imdbId;

        @JsonProperty("origin_country")
        @Nullable
        private final List<String> originCountry;

        @JsonProperty("original_language")
        @Nullable
        private final String originalLanguage;

        @JsonProperty("original_title")
        @Nullable
        private final String originalTitle;

        @JsonProperty("overview")
        @NotNull
        private final String overview;

        @JsonProperty("popularity")
        @Nullable
        private final Double popularity;

        @JsonProperty("poster_path")
        @NotNull
        private final String posterPath;

        @JsonProperty("production_countries")
        @Nullable
        private final List<ProductionCountry> productionCountries;

        @JsonProperty("publish")
        @Nullable
        private final Integer publish;

        @JsonProperty("release_date")
        @NotNull
        private final String releaseDate;

        @JsonProperty("revenue")
        @Nullable
        private final Integer revenue;

        @JsonProperty("runtime")
        private final int runtime;

        @JsonProperty("similar")
        @NotNull
        private final SimilarResults similar;

        @JsonProperty("status")
        @Nullable
        private final String status;

        @JsonProperty("tagline")
        @Nullable
        private final String tagline;

        @JsonProperty("title")
        @NotNull
        private final String title;

        @JsonProperty("vote_average")
        @Nullable
        private final Double voteAverage;

        @JsonProperty("vote_count")
        @Nullable
        private final Integer voteCount;

        public MovieDetails(@JsonProperty("id") int i, @JsonProperty("title") @NotNull String str, @JsonProperty("overview") @NotNull String str2, @JsonProperty("release_date") @NotNull String str3, @JsonProperty("runtime") int i2, @JsonProperty("poster_path") @NotNull String str4, @JsonProperty("backdrop_path") @NotNull String str5, @JsonProperty("genres") @NotNull List<TMDBGenre> list, @JsonProperty("custom_poster_tag") @Nullable String str6, @JsonProperty("credits") @NotNull Credits credits, @JsonProperty("similar") @NotNull SimilarResults similarResults, @JsonProperty("budget") @Nullable Integer num, @JsonProperty("homepage") @Nullable String str7, @JsonProperty("imdb_id") @Nullable String str8, @JsonProperty("origin_country") @Nullable List<String> list2, @JsonProperty("original_language") @Nullable String str9, @JsonProperty("original_title") @Nullable String str10, @JsonProperty("popularity") @Nullable Double d, @JsonProperty("production_countries") @Nullable List<ProductionCountry> list3, @JsonProperty("revenue") @Nullable Integer num2, @JsonProperty("status") @Nullable String str11, @JsonProperty("tagline") @Nullable String str12, @JsonProperty("vote_average") @Nullable Double d2, @JsonProperty("vote_count") @Nullable Integer num3, @JsonProperty("publish") @Nullable Integer num4) {
            this.id = i;
            this.title = str;
            this.overview = str2;
            this.releaseDate = str3;
            this.runtime = i2;
            this.posterPath = str4;
            this.backdropPath = str5;
            this.genres = list;
            this.customPosterTag = str6;
            this.credits = credits;
            this.similar = similarResults;
            this.budget = num;
            this.homepage = str7;
            this.imdbId = str8;
            this.originCountry = list2;
            this.originalLanguage = str9;
            this.originalTitle = str10;
            this.popularity = d;
            this.productionCountries = list3;
            this.revenue = num2;
            this.status = str11;
            this.tagline = str12;
            this.voteAverage = d2;
            this.voteCount = num3;
            this.publish = num4;
        }

        public /* synthetic */ MovieDetails(int i, String str, String str2, String str3, int i2, String str4, String str5, List list, String str6, Credits credits, SimilarResults similarResults, Integer num, String str7, String str8, List list2, String str9, String str10, Double d, List list3, Integer num2, String str11, String str12, Double d2, Integer num3, Integer num4, int i3, DefaultConstructorMarker defaultConstructorMarker) {
            this(i, str, str2, str3, i2, str4, str5, list, str6, credits, similarResults, (i3 & 2048) != 0 ? null : num, (i3 & 4096) != 0 ? null : str7, (i3 & 8192) != 0 ? null : str8, (i3 & 16384) != 0 ? null : list2, (32768 & i3) != 0 ? null : str9, (65536 & i3) != 0 ? null : str10, (131072 & i3) != 0 ? null : d, (262144 & i3) != 0 ? null : list3, (524288 & i3) != 0 ? null : num2, (1048576 & i3) != 0 ? null : str11, (2097152 & i3) != 0 ? null : str12, (4194304 & i3) != 0 ? null : d2, (8388608 & i3) != 0 ? null : num3, (i3 & 16777216) != 0 ? null : num4);
        }

        public static /* synthetic */ MovieDetails copy$default(MovieDetails movieDetails, int i, String str, String str2, String str3, int i2, String str4, String str5, List list, String str6, Credits credits, SimilarResults similarResults, Integer num, String str7, String str8, List list2, String str9, String str10, Double d, List list3, Integer num2, String str11, String str12, Double d2, Integer num3, Integer num4, int i3, Object obj) {
            Integer num5;
            Integer num6;
            int i4 = (i3 & 1) != 0 ? movieDetails.id : i;
            String str13 = (i3 & 2) != 0 ? movieDetails.title : str;
            String str14 = (i3 & 4) != 0 ? movieDetails.overview : str2;
            String str15 = (i3 & 8) != 0 ? movieDetails.releaseDate : str3;
            int i5 = (i3 & 16) != 0 ? movieDetails.runtime : i2;
            String str16 = (i3 & 32) != 0 ? movieDetails.posterPath : str4;
            String str17 = (i3 & 64) != 0 ? movieDetails.backdropPath : str5;
            List list4 = (i3 & 128) != 0 ? movieDetails.genres : list;
            String str18 = (i3 & 256) != 0 ? movieDetails.customPosterTag : str6;
            Credits credits2 = (i3 & 512) != 0 ? movieDetails.credits : credits;
            SimilarResults similarResults2 = (i3 & 1024) != 0 ? movieDetails.similar : similarResults;
            Integer num7 = (i3 & 2048) != 0 ? movieDetails.budget : num;
            String str19 = (i3 & 4096) != 0 ? movieDetails.homepage : str7;
            String str20 = (i3 & 8192) != 0 ? movieDetails.imdbId : str8;
            int i6 = i4;
            List list5 = (i3 & 16384) != 0 ? movieDetails.originCountry : list2;
            String str21 = (i3 & 32768) != 0 ? movieDetails.originalLanguage : str9;
            String str22 = (i3 & 65536) != 0 ? movieDetails.originalTitle : str10;
            Double d3 = (i3 & 131072) != 0 ? movieDetails.popularity : d;
            List list6 = (i3 & 262144) != 0 ? movieDetails.productionCountries : list3;
            Integer num8 = (i3 & 524288) != 0 ? movieDetails.revenue : num2;
            String str23 = (i3 & 1048576) != 0 ? movieDetails.status : str11;
            String str24 = (i3 & 2097152) != 0 ? movieDetails.tagline : str12;
            Double d4 = (i3 & 4194304) != 0 ? movieDetails.voteAverage : d2;
            Integer num9 = (i3 & 8388608) != 0 ? movieDetails.voteCount : num3;
            if ((i3 & 16777216) != 0) {
                num6 = num9;
                num5 = movieDetails.publish;
            } else {
                num5 = num4;
                num6 = num9;
            }
            return movieDetails.copy(i6, str13, str14, str15, i5, str16, str17, list4, str18, credits2, similarResults2, num7, str19, str20, list5, str21, str22, d3, list6, num8, str23, str24, d4, num6, num5);
        }

        /* renamed from: component1, reason: from getter */
        public final int getId() {
            return this.id;
        }

        @NotNull
        /* renamed from: component10, reason: from getter */
        public final Credits getCredits() {
            return this.credits;
        }

        @NotNull
        /* renamed from: component11, reason: from getter */
        public final SimilarResults getSimilar() {
            return this.similar;
        }

        @Nullable
        /* renamed from: component12, reason: from getter */
        public final Integer getBudget() {
            return this.budget;
        }

        @Nullable
        /* renamed from: component13, reason: from getter */
        public final String getHomepage() {
            return this.homepage;
        }

        @Nullable
        /* renamed from: component14, reason: from getter */
        public final String getImdbId() {
            return this.imdbId;
        }

        @Nullable
        public final List<String> component15() {
            return this.originCountry;
        }

        @Nullable
        /* renamed from: component16, reason: from getter */
        public final String getOriginalLanguage() {
            return this.originalLanguage;
        }

        @Nullable
        /* renamed from: component17, reason: from getter */
        public final String getOriginalTitle() {
            return this.originalTitle;
        }

        @Nullable
        /* renamed from: component18, reason: from getter */
        public final Double getPopularity() {
            return this.popularity;
        }

        @Nullable
        public final List<ProductionCountry> component19() {
            return this.productionCountries;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        /* renamed from: component20, reason: from getter */
        public final Integer getRevenue() {
            return this.revenue;
        }

        @Nullable
        /* renamed from: component21, reason: from getter */
        public final String getStatus() {
            return this.status;
        }

        @Nullable
        /* renamed from: component22, reason: from getter */
        public final String getTagline() {
            return this.tagline;
        }

        @Nullable
        /* renamed from: component23, reason: from getter */
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @Nullable
        /* renamed from: component24, reason: from getter */
        public final Integer getVoteCount() {
            return this.voteCount;
        }

        @Nullable
        /* renamed from: component25, reason: from getter */
        public final Integer getPublish() {
            return this.publish;
        }

        @NotNull
        /* renamed from: component3, reason: from getter */
        public final String getOverview() {
            return this.overview;
        }

        @NotNull
        /* renamed from: component4, reason: from getter */
        public final String getReleaseDate() {
            return this.releaseDate;
        }

        /* renamed from: component5, reason: from getter */
        public final int getRuntime() {
            return this.runtime;
        }

        @NotNull
        /* renamed from: component6, reason: from getter */
        public final String getPosterPath() {
            return this.posterPath;
        }

        @NotNull
        /* renamed from: component7, reason: from getter */
        public final String getBackdropPath() {
            return this.backdropPath;
        }

        @NotNull
        public final List<TMDBGenre> component8() {
            return this.genres;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final String getCustomPosterTag() {
            return this.customPosterTag;
        }

        @NotNull
        public final MovieDetails copy(@JsonProperty("id") int id, @JsonProperty("title") @NotNull String title, @JsonProperty("overview") @NotNull String overview, @JsonProperty("release_date") @NotNull String releaseDate, @JsonProperty("runtime") int runtime, @JsonProperty("poster_path") @NotNull String posterPath, @JsonProperty("backdrop_path") @NotNull String backdropPath, @JsonProperty("genres") @NotNull List<TMDBGenre> genres, @JsonProperty("custom_poster_tag") @Nullable String customPosterTag, @JsonProperty("credits") @NotNull Credits credits, @JsonProperty("similar") @NotNull SimilarResults similar, @JsonProperty("budget") @Nullable Integer budget, @JsonProperty("homepage") @Nullable String homepage, @JsonProperty("imdb_id") @Nullable String imdbId, @JsonProperty("origin_country") @Nullable List<String> originCountry, @JsonProperty("original_language") @Nullable String originalLanguage, @JsonProperty("original_title") @Nullable String originalTitle, @JsonProperty("popularity") @Nullable Double popularity, @JsonProperty("production_countries") @Nullable List<ProductionCountry> productionCountries, @JsonProperty("revenue") @Nullable Integer revenue, @JsonProperty("status") @Nullable String status, @JsonProperty("tagline") @Nullable String tagline, @JsonProperty("vote_average") @Nullable Double voteAverage, @JsonProperty("vote_count") @Nullable Integer voteCount, @JsonProperty("publish") @Nullable Integer publish) {
            return new MovieDetails(id, title, overview, releaseDate, runtime, posterPath, backdropPath, genres, customPosterTag, credits, similar, budget, homepage, imdbId, originCountry, originalLanguage, originalTitle, popularity, productionCountries, revenue, status, tagline, voteAverage, voteCount, publish);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MovieDetails)) {
                return false;
            }
            MovieDetails movieDetails = (MovieDetails) other;
            return this.id == movieDetails.id && Intrinsics.areEqual(this.title, movieDetails.title) && Intrinsics.areEqual(this.overview, movieDetails.overview) && Intrinsics.areEqual(this.releaseDate, movieDetails.releaseDate) && this.runtime == movieDetails.runtime && Intrinsics.areEqual(this.posterPath, movieDetails.posterPath) && Intrinsics.areEqual(this.backdropPath, movieDetails.backdropPath) && Intrinsics.areEqual(this.genres, movieDetails.genres) && Intrinsics.areEqual(this.customPosterTag, movieDetails.customPosterTag) && Intrinsics.areEqual(this.credits, movieDetails.credits) && Intrinsics.areEqual(this.similar, movieDetails.similar) && Intrinsics.areEqual(this.budget, movieDetails.budget) && Intrinsics.areEqual(this.homepage, movieDetails.homepage) && Intrinsics.areEqual(this.imdbId, movieDetails.imdbId) && Intrinsics.areEqual(this.originCountry, movieDetails.originCountry) && Intrinsics.areEqual(this.originalLanguage, movieDetails.originalLanguage) && Intrinsics.areEqual(this.originalTitle, movieDetails.originalTitle) && Intrinsics.areEqual(this.popularity, movieDetails.popularity) && Intrinsics.areEqual(this.productionCountries, movieDetails.productionCountries) && Intrinsics.areEqual(this.revenue, movieDetails.revenue) && Intrinsics.areEqual(this.status, movieDetails.status) && Intrinsics.areEqual(this.tagline, movieDetails.tagline) && Intrinsics.areEqual(this.voteAverage, movieDetails.voteAverage) && Intrinsics.areEqual(this.voteCount, movieDetails.voteCount) && Intrinsics.areEqual(this.publish, movieDetails.publish);
        }

        @NotNull
        public final String getBackdropPath() {
            return this.backdropPath;
        }

        @Nullable
        public final Integer getBudget() {
            return this.budget;
        }

        @NotNull
        public final Credits getCredits() {
            return this.credits;
        }

        @Nullable
        public final String getCustomPosterTag() {
            return this.customPosterTag;
        }

        @NotNull
        public final List<TMDBGenre> getGenres() {
            return this.genres;
        }

        @Nullable
        public final String getHomepage() {
            return this.homepage;
        }

        public final int getId() {
            return this.id;
        }

        @Nullable
        public final String getImdbId() {
            return this.imdbId;
        }

        @Nullable
        public final List<String> getOriginCountry() {
            return this.originCountry;
        }

        @Nullable
        public final String getOriginalLanguage() {
            return this.originalLanguage;
        }

        @Nullable
        public final String getOriginalTitle() {
            return this.originalTitle;
        }

        @NotNull
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        public final Double getPopularity() {
            return this.popularity;
        }

        @NotNull
        public final String getPosterPath() {
            return this.posterPath;
        }

        @Nullable
        public final List<ProductionCountry> getProductionCountries() {
            return this.productionCountries;
        }

        @Nullable
        public final Integer getPublish() {
            return this.publish;
        }

        @NotNull
        public final String getReleaseDate() {
            return this.releaseDate;
        }

        @Nullable
        public final Integer getRevenue() {
            return this.revenue;
        }

        public final int getRuntime() {
            return this.runtime;
        }

        @NotNull
        public final SimilarResults getSimilar() {
            return this.similar;
        }

        @Nullable
        public final String getStatus() {
            return this.status;
        }

        @Nullable
        public final String getTagline() {
            return this.tagline;
        }

        @NotNull
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @Nullable
        public final Integer getVoteCount() {
            return this.voteCount;
        }

        public int hashCode() {
            return (((((((((((((((((((((((((((((((((((((((((((((((this.id * 31) + this.title.hashCode()) * 31) + this.overview.hashCode()) * 31) + this.releaseDate.hashCode()) * 31) + this.runtime) * 31) + this.posterPath.hashCode()) * 31) + this.backdropPath.hashCode()) * 31) + this.genres.hashCode()) * 31) + (this.customPosterTag == null ? 0 : this.customPosterTag.hashCode())) * 31) + this.credits.hashCode()) * 31) + this.similar.hashCode()) * 31) + (this.budget == null ? 0 : this.budget.hashCode())) * 31) + (this.homepage == null ? 0 : this.homepage.hashCode())) * 31) + (this.imdbId == null ? 0 : this.imdbId.hashCode())) * 31) + (this.originCountry == null ? 0 : this.originCountry.hashCode())) * 31) + (this.originalLanguage == null ? 0 : this.originalLanguage.hashCode())) * 31) + (this.originalTitle == null ? 0 : this.originalTitle.hashCode())) * 31) + (this.popularity == null ? 0 : this.popularity.hashCode())) * 31) + (this.productionCountries == null ? 0 : this.productionCountries.hashCode())) * 31) + (this.revenue == null ? 0 : this.revenue.hashCode())) * 31) + (this.status == null ? 0 : this.status.hashCode())) * 31) + (this.tagline == null ? 0 : this.tagline.hashCode())) * 31) + (this.voteAverage == null ? 0 : this.voteAverage.hashCode())) * 31) + (this.voteCount == null ? 0 : this.voteCount.hashCode())) * 31) + (this.publish != null ? this.publish.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "MovieDetails(id=" + this.id + ", title=" + this.title + ", overview=" + this.overview + ", releaseDate=" + this.releaseDate + ", runtime=" + this.runtime + ", posterPath=" + this.posterPath + ", backdropPath=" + this.backdropPath + ", genres=" + this.genres + ", customPosterTag=" + this.customPosterTag + ", credits=" + this.credits + ", similar=" + this.similar + ", budget=" + this.budget + ", homepage=" + this.homepage + ", imdbId=" + this.imdbId + ", originCountry=" + this.originCountry + ", originalLanguage=" + this.originalLanguage + ", originalTitle=" + this.originalTitle + ", popularity=" + this.popularity + ", productionCountries=" + this.productionCountries + ", revenue=" + this.revenue + ", status=" + this.status + ", tagline=" + this.tagline + ", voteAverage=" + this.voteAverage + ", voteCount=" + this.voteCount + ", publish=" + this.publish + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B#\u0012\u000e\b\u0001\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006¢\u0006\u0004\b\u0007\u0010\bJ\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\fJ*\u0010\u0010\u001a\u00020\u00002\u000e\b\u0003\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006HÆ\u0001¢\u0006\u0002\u0010\u0011J\u0014\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0015\u001a\u00020\u0006HÖ\u0081\u0004J\n\u0010\u0016\u001a\u00020\u0017HÖ\u0081\u0004R\u001c\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001a\u0010\u0005\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\r\u001a\u0004\b\u000b\u0010\f¨\u0006\u0018"}, d2 = {"Lcom/cncverse/DoFlixProvider$MovieLinksResponse;", "", "links", "", "Lcom/cncverse/DoFlixProvider$StreamLink;", "id", "", "<init>", "(Ljava/util/List;Ljava/lang/Integer;)V", "getLinks", "()Ljava/util/List;", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "component1", "component2", "copy", "(Ljava/util/List;Ljava/lang/Integer;)Lcom/cncverse/DoFlixProvider$MovieLinksResponse;", "equals", "", "other", "hashCode", "toString", "", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class MovieLinksResponse {

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        @JsonProperty("links")
        @NotNull
        private final List<StreamLink> links;

        public MovieLinksResponse(@JsonProperty("links") @NotNull List<StreamLink> list, @JsonProperty("id") @Nullable Integer num) {
            this.links = list;
            this.id = num;
        }

        public /* synthetic */ MovieLinksResponse(List list, Integer num, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(list, (i & 2) != 0 ? null : num);
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ MovieLinksResponse copy$default(MovieLinksResponse movieLinksResponse, List list, Integer num, int i, Object obj) {
            if ((i & 1) != 0) {
                list = movieLinksResponse.links;
            }
            if ((i & 2) != 0) {
                num = movieLinksResponse.id;
            }
            return movieLinksResponse.copy(list, num);
        }

        @NotNull
        public final List<StreamLink> component1() {
            return this.links;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @NotNull
        public final MovieLinksResponse copy(@JsonProperty("links") @NotNull List<StreamLink> links, @JsonProperty("id") @Nullable Integer id) {
            return new MovieLinksResponse(links, id);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MovieLinksResponse)) {
                return false;
            }
            MovieLinksResponse movieLinksResponse = (MovieLinksResponse) other;
            return Intrinsics.areEqual(this.links, movieLinksResponse.links) && Intrinsics.areEqual(this.id, movieLinksResponse.id);
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        @NotNull
        public final List<StreamLink> getLinks() {
            return this.links;
        }

        public int hashCode() {
            return (this.links.hashCode() * 31) + (this.id == null ? 0 : this.id.hashCode());
        }

        @NotNull
        public String toString() {
            return "MovieLinksResponse(links=" + this.links + ", id=" + this.id + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u0006\n\u0002\b\u0006\n\u0002\u0010 \n\u0000\n\u0002\u0010\u000b\n\u0002\bI\b\u0087\b\u0018\u00002\u00020\u0001B·\u0002\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u000e\u0012\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u000e\u0012\n\b\u0003\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\u0010\b\u0003\u0010\u0014\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0015\u0012\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u0017\u0012\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u0005\u0012\u0010\b\u0003\u0010\u0019\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u0015\u0012\n\b\u0003\u0010\u001a\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0003\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0015\u0012\n\b\u0003\u0010\u001c\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u001d\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u0017¢\u0006\u0004\b\u001f\u0010 J\t\u0010B\u001a\u00020\u0003HÆ\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010D\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010E\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010F\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010G\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010H\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010I\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010J\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010K\u001a\u0004\u0018\u00010\u000eHÆ\u0003¢\u0006\u0002\u0010-J\u0010\u0010L\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u00100J\u0010\u0010M\u001a\u0004\u0018\u00010\u000eHÆ\u0003¢\u0006\u0002\u0010-J\u000b\u0010N\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010O\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010P\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0011\u0010Q\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0015HÆ\u0003J\u0010\u0010R\u001a\u0004\u0018\u00010\u0017HÆ\u0003¢\u0006\u0002\u00109J\u000b\u0010S\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0011\u0010T\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u0015HÆ\u0003J\u0010\u0010U\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u00100J\u0011\u0010V\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0015HÆ\u0003J\u000b\u0010W\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010X\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010Y\u001a\u0004\u0018\u00010\u0017HÆ\u0003¢\u0006\u0002\u00109J¾\u0002\u0010Z\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u000e2\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u000e2\n\b\u0003\u0010\u0011\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0012\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u00052\u0010\b\u0003\u0010\u0014\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00152\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u00172\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u00052\u0010\b\u0003\u0010\u0019\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u00152\n\b\u0003\u0010\u001a\u001a\u0004\u0018\u00010\u00032\u0010\b\u0003\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00152\n\b\u0003\u0010\u001c\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u001d\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u0017HÆ\u0001¢\u0006\u0002\u0010[J\u0014\u0010\\\u001a\u00020\u00172\b\u0010]\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010^\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010_\u001a\u00020\u0005HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b%\u0010$R\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b&\u0010$R\u0018\u0010\b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b'\u0010$R\u0018\u0010\t\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b(\u0010$R\u0018\u0010\n\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b)\u0010$R\u0018\u0010\u000b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b*\u0010$R\u0018\u0010\f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b+\u0010$R\u001a\u0010\r\u001a\u0004\u0018\u00010\u000e8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010.\u001a\u0004\b,\u0010-R\u001a\u0010\u000f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00101\u001a\u0004\b/\u00100R\u001a\u0010\u0010\u001a\u0004\u0018\u00010\u000e8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010.\u001a\u0004\b2\u0010-R\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b3\u0010$R\u0018\u0010\u0012\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b4\u0010$R\u0018\u0010\u0013\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b5\u0010$R\u001e\u0010\u0014\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00158\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b6\u00107R\u001a\u0010\u0016\u001a\u0004\u0018\u00010\u00178\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010:\u001a\u0004\b8\u00109R\u0018\u0010\u0018\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b;\u0010$R\u001e\u0010\u0019\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u00158\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b<\u00107R\u001a\u0010\u001a\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u00101\u001a\u0004\b=\u00100R\u001e\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00158\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b>\u00107R\u0018\u0010\u001c\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b?\u0010$R\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b@\u0010$R\u001a\u0010\u001e\u001a\u0004\u0018\u00010\u00178\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010:\u001a\u0004\bA\u00109¨\u0006`"}, d2 = {"Lcom/cncverse/DoFlixProvider$MovieResult;", "", "id", "", "title", "", "name", "customPosterTag", "originalTitle", "originalName", "overview", "releaseDate", "firstAirDate", "popularity", "", "voteCount", "voteAverage", "posterPath", "backdropPath", "originalLanguage", "genreIds", "", "adult", "", "mediaType", "originCountry", "gender", "knownFor", "knownForDepartment", "profilePath", "video", "<init>", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/Boolean;Ljava/lang/String;Ljava/util/List;Ljava/lang/Integer;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;)V", "getId", "()I", "getTitle", "()Ljava/lang/String;", "getName", "getCustomPosterTag", "getOriginalTitle", "getOriginalName", "getOverview", "getReleaseDate", "getFirstAirDate", "getPopularity", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getVoteCount", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getVoteAverage", "getPosterPath", "getBackdropPath", "getOriginalLanguage", "getGenreIds", "()Ljava/util/List;", "getAdult", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getMediaType", "getOriginCountry", "getGender", "getKnownFor", "getKnownForDepartment", "getProfilePath", "getVideo", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "component22", "component23", "component24", "copy", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/Boolean;Ljava/lang/String;Ljava/util/List;Ljava/lang/Integer;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;)Lcom/cncverse/DoFlixProvider$MovieResult;", "equals", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class MovieResult {

        @JsonProperty("adult")
        @Nullable
        private final Boolean adult;

        @JsonProperty("backdrop_path")
        @Nullable
        private final String backdropPath;

        @JsonProperty("custom_poster_tag")
        @Nullable
        private final String customPosterTag;

        @JsonProperty("first_air_date")
        @Nullable
        private final String firstAirDate;

        @JsonProperty("gender")
        @Nullable
        private final Integer gender;

        @JsonProperty("genre_ids")
        @Nullable
        private final List<Integer> genreIds;

        @JsonProperty("id")
        private final int id;

        @JsonProperty("known_for")
        @Nullable
        private final List<Object> knownFor;

        @JsonProperty("known_for_department")
        @Nullable
        private final String knownForDepartment;

        @JsonProperty("media_type")
        @Nullable
        private final String mediaType;

        @JsonProperty("name")
        @Nullable
        private final String name;

        @JsonProperty("origin_country")
        @Nullable
        private final List<String> originCountry;

        @JsonProperty("original_language")
        @Nullable
        private final String originalLanguage;

        @JsonProperty("original_name")
        @Nullable
        private final String originalName;

        @JsonProperty("original_title")
        @Nullable
        private final String originalTitle;

        @JsonProperty("overview")
        @Nullable
        private final String overview;

        @JsonProperty("popularity")
        @Nullable
        private final Double popularity;

        @JsonProperty("poster_path")
        @Nullable
        private final String posterPath;

        @JsonProperty("profile_path")
        @Nullable
        private final String profilePath;

        @JsonProperty("release_date")
        @Nullable
        private final String releaseDate;

        @JsonProperty("title")
        @Nullable
        private final String title;

        @JsonProperty("video")
        @Nullable
        private final Boolean video;

        @JsonProperty("vote_average")
        @Nullable
        private final Double voteAverage;

        @JsonProperty("vote_count")
        @Nullable
        private final Integer voteCount;

        public MovieResult(@JsonProperty("id") int i, @JsonProperty("title") @Nullable String str, @JsonProperty("name") @Nullable String str2, @JsonProperty("custom_poster_tag") @Nullable String str3, @JsonProperty("original_title") @Nullable String str4, @JsonProperty("original_name") @Nullable String str5, @JsonProperty("overview") @Nullable String str6, @JsonProperty("release_date") @Nullable String str7, @JsonProperty("first_air_date") @Nullable String str8, @JsonProperty("popularity") @Nullable Double d, @JsonProperty("vote_count") @Nullable Integer num, @JsonProperty("vote_average") @Nullable Double d2, @JsonProperty("poster_path") @Nullable String str9, @JsonProperty("backdrop_path") @Nullable String str10, @JsonProperty("original_language") @Nullable String str11, @JsonProperty("genre_ids") @Nullable List<Integer> list, @JsonProperty("adult") @Nullable Boolean bool, @JsonProperty("media_type") @Nullable String str12, @JsonProperty("origin_country") @Nullable List<String> list2, @JsonProperty("gender") @Nullable Integer num2, @JsonProperty("known_for") @Nullable List<? extends Object> list3, @JsonProperty("known_for_department") @Nullable String str13, @JsonProperty("profile_path") @Nullable String str14, @JsonProperty("video") @Nullable Boolean bool2) {
            this.id = i;
            this.title = str;
            this.name = str2;
            this.customPosterTag = str3;
            this.originalTitle = str4;
            this.originalName = str5;
            this.overview = str6;
            this.releaseDate = str7;
            this.firstAirDate = str8;
            this.popularity = d;
            this.voteCount = num;
            this.voteAverage = d2;
            this.posterPath = str9;
            this.backdropPath = str10;
            this.originalLanguage = str11;
            this.genreIds = list;
            this.adult = bool;
            this.mediaType = str12;
            this.originCountry = list2;
            this.gender = num2;
            this.knownFor = list3;
            this.knownForDepartment = str13;
            this.profilePath = str14;
            this.video = bool2;
        }

        public /* synthetic */ MovieResult(int i, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, Double d, Integer num, Double d2, String str9, String str10, String str11, List list, Boolean bool, String str12, List list2, Integer num2, List list3, String str13, String str14, Boolean bool2, int i2, DefaultConstructorMarker defaultConstructorMarker) {
            this(i, (i2 & 2) != 0 ? null : str, (i2 & 4) != 0 ? null : str2, str3, (i2 & 16) != 0 ? null : str4, (i2 & 32) != 0 ? null : str5, (i2 & 64) != 0 ? null : str6, (i2 & 128) != 0 ? null : str7, (i2 & 256) != 0 ? null : str8, (i2 & 512) != 0 ? null : d, (i2 & 1024) != 0 ? null : num, (i2 & 2048) != 0 ? null : d2, (i2 & 4096) != 0 ? null : str9, (i2 & 8192) != 0 ? null : str10, (i2 & 16384) != 0 ? null : str11, (32768 & i2) != 0 ? null : list, (65536 & i2) != 0 ? null : bool, (131072 & i2) != 0 ? null : str12, (262144 & i2) != 0 ? null : list2, (524288 & i2) != 0 ? null : num2, (1048576 & i2) != 0 ? null : list3, (2097152 & i2) != 0 ? null : str13, (4194304 & i2) != 0 ? null : str14, (i2 & 8388608) != 0 ? null : bool2);
        }

        public static /* synthetic */ MovieResult copy$default(MovieResult movieResult, int i, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, Double d, Integer num, Double d2, String str9, String str10, String str11, List list, Boolean bool, String str12, List list2, Integer num2, List list3, String str13, String str14, Boolean bool2, int i2, Object obj) {
            Boolean bool3;
            String str15;
            int i3 = (i2 & 1) != 0 ? movieResult.id : i;
            String str16 = (i2 & 2) != 0 ? movieResult.title : str;
            String str17 = (i2 & 4) != 0 ? movieResult.name : str2;
            String str18 = (i2 & 8) != 0 ? movieResult.customPosterTag : str3;
            String str19 = (i2 & 16) != 0 ? movieResult.originalTitle : str4;
            String str20 = (i2 & 32) != 0 ? movieResult.originalName : str5;
            String str21 = (i2 & 64) != 0 ? movieResult.overview : str6;
            String str22 = (i2 & 128) != 0 ? movieResult.releaseDate : str7;
            String str23 = (i2 & 256) != 0 ? movieResult.firstAirDate : str8;
            Double d3 = (i2 & 512) != 0 ? movieResult.popularity : d;
            Integer num3 = (i2 & 1024) != 0 ? movieResult.voteCount : num;
            Double d4 = (i2 & 2048) != 0 ? movieResult.voteAverage : d2;
            String str24 = (i2 & 4096) != 0 ? movieResult.posterPath : str9;
            String str25 = (i2 & 8192) != 0 ? movieResult.backdropPath : str10;
            int i4 = i3;
            String str26 = (i2 & 16384) != 0 ? movieResult.originalLanguage : str11;
            List list4 = (i2 & 32768) != 0 ? movieResult.genreIds : list;
            Boolean bool4 = (i2 & 65536) != 0 ? movieResult.adult : bool;
            String str27 = (i2 & 131072) != 0 ? movieResult.mediaType : str12;
            List list5 = (i2 & 262144) != 0 ? movieResult.originCountry : list2;
            Integer num4 = (i2 & 524288) != 0 ? movieResult.gender : num2;
            List list6 = (i2 & 1048576) != 0 ? movieResult.knownFor : list3;
            String str28 = (i2 & 2097152) != 0 ? movieResult.knownForDepartment : str13;
            String str29 = (i2 & 4194304) != 0 ? movieResult.profilePath : str14;
            if ((i2 & 8388608) != 0) {
                str15 = str29;
                bool3 = movieResult.video;
            } else {
                bool3 = bool2;
                str15 = str29;
            }
            return movieResult.copy(i4, str16, str17, str18, str19, str20, str21, str22, str23, d3, num3, d4, str24, str25, str26, list4, bool4, str27, list5, num4, list6, str28, str15, bool3);
        }

        /* renamed from: component1, reason: from getter */
        public final int getId() {
            return this.id;
        }

        @Nullable
        /* renamed from: component10, reason: from getter */
        public final Double getPopularity() {
            return this.popularity;
        }

        @Nullable
        /* renamed from: component11, reason: from getter */
        public final Integer getVoteCount() {
            return this.voteCount;
        }

        @Nullable
        /* renamed from: component12, reason: from getter */
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @Nullable
        /* renamed from: component13, reason: from getter */
        public final String getPosterPath() {
            return this.posterPath;
        }

        @Nullable
        /* renamed from: component14, reason: from getter */
        public final String getBackdropPath() {
            return this.backdropPath;
        }

        @Nullable
        /* renamed from: component15, reason: from getter */
        public final String getOriginalLanguage() {
            return this.originalLanguage;
        }

        @Nullable
        public final List<Integer> component16() {
            return this.genreIds;
        }

        @Nullable
        /* renamed from: component17, reason: from getter */
        public final Boolean getAdult() {
            return this.adult;
        }

        @Nullable
        /* renamed from: component18, reason: from getter */
        public final String getMediaType() {
            return this.mediaType;
        }

        @Nullable
        public final List<String> component19() {
            return this.originCountry;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        /* renamed from: component20, reason: from getter */
        public final Integer getGender() {
            return this.gender;
        }

        @Nullable
        public final List<Object> component21() {
            return this.knownFor;
        }

        @Nullable
        /* renamed from: component22, reason: from getter */
        public final String getKnownForDepartment() {
            return this.knownForDepartment;
        }

        @Nullable
        /* renamed from: component23, reason: from getter */
        public final String getProfilePath() {
            return this.profilePath;
        }

        @Nullable
        /* renamed from: component24, reason: from getter */
        public final Boolean getVideo() {
            return this.video;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final String getCustomPosterTag() {
            return this.customPosterTag;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getOriginalTitle() {
            return this.originalTitle;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final String getOriginalName() {
            return this.originalName;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final String getReleaseDate() {
            return this.releaseDate;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final String getFirstAirDate() {
            return this.firstAirDate;
        }

        @NotNull
        public final MovieResult copy(@JsonProperty("id") int id, @JsonProperty("title") @Nullable String title, @JsonProperty("name") @Nullable String name, @JsonProperty("custom_poster_tag") @Nullable String customPosterTag, @JsonProperty("original_title") @Nullable String originalTitle, @JsonProperty("original_name") @Nullable String originalName, @JsonProperty("overview") @Nullable String overview, @JsonProperty("release_date") @Nullable String releaseDate, @JsonProperty("first_air_date") @Nullable String firstAirDate, @JsonProperty("popularity") @Nullable Double popularity, @JsonProperty("vote_count") @Nullable Integer voteCount, @JsonProperty("vote_average") @Nullable Double voteAverage, @JsonProperty("poster_path") @Nullable String posterPath, @JsonProperty("backdrop_path") @Nullable String backdropPath, @JsonProperty("original_language") @Nullable String originalLanguage, @JsonProperty("genre_ids") @Nullable List<Integer> genreIds, @JsonProperty("adult") @Nullable Boolean adult, @JsonProperty("media_type") @Nullable String mediaType, @JsonProperty("origin_country") @Nullable List<String> originCountry, @JsonProperty("gender") @Nullable Integer gender, @JsonProperty("known_for") @Nullable List<? extends Object> knownFor, @JsonProperty("known_for_department") @Nullable String knownForDepartment, @JsonProperty("profile_path") @Nullable String profilePath, @JsonProperty("video") @Nullable Boolean video) {
            return new MovieResult(id, title, name, customPosterTag, originalTitle, originalName, overview, releaseDate, firstAirDate, popularity, voteCount, voteAverage, posterPath, backdropPath, originalLanguage, genreIds, adult, mediaType, originCountry, gender, knownFor, knownForDepartment, profilePath, video);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MovieResult)) {
                return false;
            }
            MovieResult movieResult = (MovieResult) other;
            return this.id == movieResult.id && Intrinsics.areEqual(this.title, movieResult.title) && Intrinsics.areEqual(this.name, movieResult.name) && Intrinsics.areEqual(this.customPosterTag, movieResult.customPosterTag) && Intrinsics.areEqual(this.originalTitle, movieResult.originalTitle) && Intrinsics.areEqual(this.originalName, movieResult.originalName) && Intrinsics.areEqual(this.overview, movieResult.overview) && Intrinsics.areEqual(this.releaseDate, movieResult.releaseDate) && Intrinsics.areEqual(this.firstAirDate, movieResult.firstAirDate) && Intrinsics.areEqual(this.popularity, movieResult.popularity) && Intrinsics.areEqual(this.voteCount, movieResult.voteCount) && Intrinsics.areEqual(this.voteAverage, movieResult.voteAverage) && Intrinsics.areEqual(this.posterPath, movieResult.posterPath) && Intrinsics.areEqual(this.backdropPath, movieResult.backdropPath) && Intrinsics.areEqual(this.originalLanguage, movieResult.originalLanguage) && Intrinsics.areEqual(this.genreIds, movieResult.genreIds) && Intrinsics.areEqual(this.adult, movieResult.adult) && Intrinsics.areEqual(this.mediaType, movieResult.mediaType) && Intrinsics.areEqual(this.originCountry, movieResult.originCountry) && Intrinsics.areEqual(this.gender, movieResult.gender) && Intrinsics.areEqual(this.knownFor, movieResult.knownFor) && Intrinsics.areEqual(this.knownForDepartment, movieResult.knownForDepartment) && Intrinsics.areEqual(this.profilePath, movieResult.profilePath) && Intrinsics.areEqual(this.video, movieResult.video);
        }

        @Nullable
        public final Boolean getAdult() {
            return this.adult;
        }

        @Nullable
        public final String getBackdropPath() {
            return this.backdropPath;
        }

        @Nullable
        public final String getCustomPosterTag() {
            return this.customPosterTag;
        }

        @Nullable
        public final String getFirstAirDate() {
            return this.firstAirDate;
        }

        @Nullable
        public final Integer getGender() {
            return this.gender;
        }

        @Nullable
        public final List<Integer> getGenreIds() {
            return this.genreIds;
        }

        public final int getId() {
            return this.id;
        }

        @Nullable
        public final List<Object> getKnownFor() {
            return this.knownFor;
        }

        @Nullable
        public final String getKnownForDepartment() {
            return this.knownForDepartment;
        }

        @Nullable
        public final String getMediaType() {
            return this.mediaType;
        }

        @Nullable
        public final String getName() {
            return this.name;
        }

        @Nullable
        public final List<String> getOriginCountry() {
            return this.originCountry;
        }

        @Nullable
        public final String getOriginalLanguage() {
            return this.originalLanguage;
        }

        @Nullable
        public final String getOriginalName() {
            return this.originalName;
        }

        @Nullable
        public final String getOriginalTitle() {
            return this.originalTitle;
        }

        @Nullable
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        public final Double getPopularity() {
            return this.popularity;
        }

        @Nullable
        public final String getPosterPath() {
            return this.posterPath;
        }

        @Nullable
        public final String getProfilePath() {
            return this.profilePath;
        }

        @Nullable
        public final String getReleaseDate() {
            return this.releaseDate;
        }

        @Nullable
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        public final Boolean getVideo() {
            return this.video;
        }

        @Nullable
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @Nullable
        public final Integer getVoteCount() {
            return this.voteCount;
        }

        public int hashCode() {
            return (((((((((((((((((((((((((((((((((((((((((((((this.id * 31) + (this.title == null ? 0 : this.title.hashCode())) * 31) + (this.name == null ? 0 : this.name.hashCode())) * 31) + (this.customPosterTag == null ? 0 : this.customPosterTag.hashCode())) * 31) + (this.originalTitle == null ? 0 : this.originalTitle.hashCode())) * 31) + (this.originalName == null ? 0 : this.originalName.hashCode())) * 31) + (this.overview == null ? 0 : this.overview.hashCode())) * 31) + (this.releaseDate == null ? 0 : this.releaseDate.hashCode())) * 31) + (this.firstAirDate == null ? 0 : this.firstAirDate.hashCode())) * 31) + (this.popularity == null ? 0 : this.popularity.hashCode())) * 31) + (this.voteCount == null ? 0 : this.voteCount.hashCode())) * 31) + (this.voteAverage == null ? 0 : this.voteAverage.hashCode())) * 31) + (this.posterPath == null ? 0 : this.posterPath.hashCode())) * 31) + (this.backdropPath == null ? 0 : this.backdropPath.hashCode())) * 31) + (this.originalLanguage == null ? 0 : this.originalLanguage.hashCode())) * 31) + (this.genreIds == null ? 0 : this.genreIds.hashCode())) * 31) + (this.adult == null ? 0 : this.adult.hashCode())) * 31) + (this.mediaType == null ? 0 : this.mediaType.hashCode())) * 31) + (this.originCountry == null ? 0 : this.originCountry.hashCode())) * 31) + (this.gender == null ? 0 : this.gender.hashCode())) * 31) + (this.knownFor == null ? 0 : this.knownFor.hashCode())) * 31) + (this.knownForDepartment == null ? 0 : this.knownForDepartment.hashCode())) * 31) + (this.profilePath == null ? 0 : this.profilePath.hashCode())) * 31) + (this.video != null ? this.video.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "MovieResult(id=" + this.id + ", title=" + this.title + ", name=" + this.name + ", customPosterTag=" + this.customPosterTag + ", originalTitle=" + this.originalTitle + ", originalName=" + this.originalName + ", overview=" + this.overview + ", releaseDate=" + this.releaseDate + ", firstAirDate=" + this.firstAirDate + ", popularity=" + this.popularity + ", voteCount=" + this.voteCount + ", voteAverage=" + this.voteAverage + ", posterPath=" + this.posterPath + ", backdropPath=" + this.backdropPath + ", originalLanguage=" + this.originalLanguage + ", genreIds=" + this.genreIds + ", adult=" + this.adult + ", mediaType=" + this.mediaType + ", originCountry=" + this.originCountry + ", gender=" + this.gender + ", knownFor=" + this.knownFor + ", knownForDepartment=" + this.knownForDepartment + ", profilePath=" + this.profilePath + ", video=" + this.video + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B5\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0001\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0001\u0010\b\u001a\u00020\u0003¢\u0006\u0004\b\t\u0010\nJ\t\u0010\u0011\u001a\u00020\u0003HÆ\u0003J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005HÆ\u0003J\t\u0010\u0013\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0014\u001a\u00020\u0003HÆ\u0003J7\u0010\u0015\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\u000e\b\u0003\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0003\u0010\u0007\u001a\u00020\u00032\b\b\u0003\u0010\b\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0019\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010\u001a\u001a\u00020\u001bHÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001c\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u0007\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\fR\u0016\u0010\b\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\f¨\u0006\u001c"}, d2 = {"Lcom/cncverse/DoFlixProvider$MultiSearchResponse;", "", "page", "", "results", "", "Lcom/cncverse/DoFlixProvider$MovieResult;", "totalPages", "totalResults", "<init>", "(ILjava/util/List;II)V", "getPage", "()I", "getResults", "()Ljava/util/List;", "getTotalPages", "getTotalResults", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "toString", "", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class MultiSearchResponse {

        @JsonProperty("page")
        private final int page;

        @JsonProperty("results")
        @NotNull
        private final List<MovieResult> results;

        @JsonProperty("total_pages")
        private final int totalPages;

        @JsonProperty("total_results")
        private final int totalResults;

        public MultiSearchResponse(@JsonProperty("page") int i, @JsonProperty("results") @NotNull List<MovieResult> list, @JsonProperty("total_pages") int i2, @JsonProperty("total_results") int i3) {
            this.page = i;
            this.results = list;
            this.totalPages = i2;
            this.totalResults = i3;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ MultiSearchResponse copy$default(MultiSearchResponse multiSearchResponse, int i, List list, int i2, int i3, int i4, Object obj) {
            if ((i4 & 1) != 0) {
                i = multiSearchResponse.page;
            }
            if ((i4 & 2) != 0) {
                list = multiSearchResponse.results;
            }
            if ((i4 & 4) != 0) {
                i2 = multiSearchResponse.totalPages;
            }
            if ((i4 & 8) != 0) {
                i3 = multiSearchResponse.totalResults;
            }
            return multiSearchResponse.copy(i, list, i2, i3);
        }

        /* renamed from: component1, reason: from getter */
        public final int getPage() {
            return this.page;
        }

        @NotNull
        public final List<MovieResult> component2() {
            return this.results;
        }

        /* renamed from: component3, reason: from getter */
        public final int getTotalPages() {
            return this.totalPages;
        }

        /* renamed from: component4, reason: from getter */
        public final int getTotalResults() {
            return this.totalResults;
        }

        @NotNull
        public final MultiSearchResponse copy(@JsonProperty("page") int page, @JsonProperty("results") @NotNull List<MovieResult> results, @JsonProperty("total_pages") int totalPages, @JsonProperty("total_results") int totalResults) {
            return new MultiSearchResponse(page, results, totalPages, totalResults);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MultiSearchResponse)) {
                return false;
            }
            MultiSearchResponse multiSearchResponse = (MultiSearchResponse) other;
            return this.page == multiSearchResponse.page && Intrinsics.areEqual(this.results, multiSearchResponse.results) && this.totalPages == multiSearchResponse.totalPages && this.totalResults == multiSearchResponse.totalResults;
        }

        public final int getPage() {
            return this.page;
        }

        @NotNull
        public final List<MovieResult> getResults() {
            return this.results;
        }

        public final int getTotalPages() {
            return this.totalPages;
        }

        public final int getTotalResults() {
            return this.totalResults;
        }

        public int hashCode() {
            return (((((this.page * 31) + this.results.hashCode()) * 31) + this.totalPages) * 31) + this.totalResults;
        }

        @NotNull
        public String toString() {
            return "MultiSearchResponse(page=" + this.page + ", results=" + this.results + ", totalPages=" + this.totalPages + ", totalResults=" + this.totalResults + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u001f\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u000b\u0010\n\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u000b\u001a\u0004\u0018\u00010\u0003HÆ\u0003J!\u0010\f\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003HÆ\u0001J\u0014\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004J\n\u0010\u0012\u001a\u00020\u0003HÖ\u0081\u0004R\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0013"}, d2 = {"Lcom/cncverse/DoFlixProvider$ProductionCountry;", "", "iso31661", "", "name", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getIso31661", "()Ljava/lang/String;", "getName", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class ProductionCountry {

        @JsonProperty("iso_3166_1")
        @Nullable
        private final String iso31661;

        @JsonProperty("name")
        @Nullable
        private final String name;

        public ProductionCountry() {
            this(null, null, 3, null);
        }

        public ProductionCountry(@JsonProperty("iso_3166_1") @Nullable String str, @JsonProperty("name") @Nullable String str2) {
            this.iso31661 = str;
            this.name = str2;
        }

        public /* synthetic */ ProductionCountry(String str, String str2, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : str, (i & 2) != 0 ? null : str2);
        }

        public static /* synthetic */ ProductionCountry copy$default(ProductionCountry productionCountry, String str, String str2, int i, Object obj) {
            if ((i & 1) != 0) {
                str = productionCountry.iso31661;
            }
            if ((i & 2) != 0) {
                str2 = productionCountry.name;
            }
            return productionCountry.copy(str, str2);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getIso31661() {
            return this.iso31661;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @NotNull
        public final ProductionCountry copy(@JsonProperty("iso_3166_1") @Nullable String iso31661, @JsonProperty("name") @Nullable String name) {
            return new ProductionCountry(iso31661, name);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ProductionCountry)) {
                return false;
            }
            ProductionCountry productionCountry = (ProductionCountry) other;
            return Intrinsics.areEqual(this.iso31661, productionCountry.iso31661) && Intrinsics.areEqual(this.name, productionCountry.name);
        }

        @Nullable
        public final String getIso31661() {
            return this.iso31661;
        }

        @Nullable
        public final String getName() {
            return this.name;
        }

        public int hashCode() {
            return ((this.iso31661 == null ? 0 : this.iso31661.hashCode()) * 31) + (this.name != null ? this.name.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "ProductionCountry(iso31661=" + this.iso31661 + ", name=" + this.name + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B7\u0012\u000e\b\u0001\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0001\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0001\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003¢\u0006\u0004\b\u0007\u0010\bJ\u000f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u000f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003HÆ\u0003J9\u0010\u0010\u001a\u00020\u00002\u000e\b\u0003\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0003\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0003\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003HÆ\u0001J\u0014\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0014\u001a\u00020\u0015HÖ\u0081\u0004J\n\u0010\u0016\u001a\u00020\u0017HÖ\u0081\u0004R\u001c\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001c\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u001c\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\n¨\u0006\u0018"}, d2 = {"Lcom/cncverse/DoFlixProvider$SearchApiResponse;", "", "movie", "", "Lcom/cncverse/DoFlixProvider$VideoItem;", "tvseries", "tvChannels", "<init>", "(Ljava/util/List;Ljava/util/List;Ljava/util/List;)V", "getMovie", "()Ljava/util/List;", "getTvseries", "getTvChannels", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class SearchApiResponse {

        @JsonProperty("movie")
        @NotNull
        private final List<VideoItem> movie;

        @JsonProperty("tv_channels")
        @NotNull
        private final List<Object> tvChannels;

        @JsonProperty("tvseries")
        @NotNull
        private final List<VideoItem> tvseries;

        public SearchApiResponse(@JsonProperty("movie") @NotNull List<VideoItem> list, @JsonProperty("tvseries") @NotNull List<VideoItem> list2, @JsonProperty("tv_channels") @NotNull List<? extends Object> list3) {
            this.movie = list;
            this.tvseries = list2;
            this.tvChannels = list3;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ SearchApiResponse copy$default(SearchApiResponse searchApiResponse, List list, List list2, List list3, int i, Object obj) {
            if ((i & 1) != 0) {
                list = searchApiResponse.movie;
            }
            if ((i & 2) != 0) {
                list2 = searchApiResponse.tvseries;
            }
            if ((i & 4) != 0) {
                list3 = searchApiResponse.tvChannels;
            }
            return searchApiResponse.copy(list, list2, list3);
        }

        @NotNull
        public final List<VideoItem> component1() {
            return this.movie;
        }

        @NotNull
        public final List<VideoItem> component2() {
            return this.tvseries;
        }

        @NotNull
        public final List<Object> component3() {
            return this.tvChannels;
        }

        @NotNull
        public final SearchApiResponse copy(@JsonProperty("movie") @NotNull List<VideoItem> movie, @JsonProperty("tvseries") @NotNull List<VideoItem> tvseries, @JsonProperty("tv_channels") @NotNull List<? extends Object> tvChannels) {
            return new SearchApiResponse(movie, tvseries, tvChannels);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SearchApiResponse)) {
                return false;
            }
            SearchApiResponse searchApiResponse = (SearchApiResponse) other;
            return Intrinsics.areEqual(this.movie, searchApiResponse.movie) && Intrinsics.areEqual(this.tvseries, searchApiResponse.tvseries) && Intrinsics.areEqual(this.tvChannels, searchApiResponse.tvChannels);
        }

        @NotNull
        public final List<VideoItem> getMovie() {
            return this.movie;
        }

        @NotNull
        public final List<Object> getTvChannels() {
            return this.tvChannels;
        }

        @NotNull
        public final List<VideoItem> getTvseries() {
            return this.tvseries;
        }

        public int hashCode() {
            return (((this.movie.hashCode() * 31) + this.tvseries.hashCode()) * 31) + this.tvChannels.hashCode();
        }

        @NotNull
        public String toString() {
            return "SearchApiResponse(movie=" + this.movie + ", tvseries=" + this.tvseries + ", tvChannels=" + this.tvChannels + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u001e\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001Bu\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0001\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\n\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\n\u0012\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\n\u0012\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\n\u0012\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u000f¢\u0006\u0004\b\u0010\u0010\u0011J\t\u0010\"\u001a\u00020\u0003HÆ\u0003J\u000f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005HÆ\u0003J\u0010\u0010$\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u0017J\u0010\u0010%\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u0017J\u000b\u0010&\u001a\u0004\u0018\u00010\nHÆ\u0003J\u000b\u0010'\u001a\u0004\u0018\u00010\nHÆ\u0003J\u000b\u0010(\u001a\u0004\u0018\u00010\nHÆ\u0003J\u000b\u0010)\u001a\u0004\u0018\u00010\nHÆ\u0003J\u0010\u0010*\u001a\u0004\u0018\u00010\u000fHÆ\u0003¢\u0006\u0002\u0010 J|\u0010+\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\u000e\b\u0003\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\n2\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\n2\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\n2\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\n2\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u000fHÆ\u0001¢\u0006\u0002\u0010,J\u0014\u0010-\u001a\u00020.2\b\u0010/\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u00100\u001a\u00020\u0003HÖ\u0081\u0004J\n\u00101\u001a\u00020\nHÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001c\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u001a\u0010\u0007\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0018\u001a\u0004\b\u0016\u0010\u0017R\u001a\u0010\b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0018\u001a\u0004\b\u0019\u0010\u0017R\u0018\u0010\t\u001a\u0004\u0018\u00010\n8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0018\u0010\u000b\u001a\u0004\u0018\u00010\n8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001bR\u0018\u0010\f\u001a\u0004\u0018\u00010\n8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001bR\u0018\u0010\r\u001a\u0004\u0018\u00010\n8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001bR\u001a\u0010\u000e\u001a\u0004\u0018\u00010\u000f8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010!\u001a\u0004\b\u001f\u0010 ¨\u00062"}, d2 = {"Lcom/cncverse/DoFlixProvider$SeasonDetails;", "", "seasonNumber", "", "episodes", "", "Lcom/cncverse/DoFlixProvider$TvEpisode;", "id", "mongoId", "name", "", "overview", "posterPath", "airDate", "voteAverage", "", "<init>", "(ILjava/util/List;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;)V", "getSeasonNumber", "()I", "getEpisodes", "()Ljava/util/List;", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getMongoId", "getName", "()Ljava/lang/String;", "getOverview", "getPosterPath", "getAirDate", "getVoteAverage", "()Ljava/lang/Double;", "Ljava/lang/Double;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(ILjava/util/List;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;)Lcom/cncverse/DoFlixProvider$SeasonDetails;", "equals", "", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class SeasonDetails {

        @JsonProperty("air_date")
        @Nullable
        private final String airDate;

        @JsonProperty("episodes")
        @NotNull
        private final List<TvEpisode> episodes;

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        @JsonProperty("_id")
        @Nullable
        private final Integer mongoId;

        @JsonProperty("name")
        @Nullable
        private final String name;

        @JsonProperty("overview")
        @Nullable
        private final String overview;

        @JsonProperty("poster_path")
        @Nullable
        private final String posterPath;

        @JsonProperty("season_number")
        private final int seasonNumber;

        @JsonProperty("vote_average")
        @Nullable
        private final Double voteAverage;

        public SeasonDetails(@JsonProperty("season_number") int i, @JsonProperty("episodes") @NotNull List<TvEpisode> list, @JsonProperty("id") @Nullable Integer num, @JsonProperty("_id") @Nullable Integer num2, @JsonProperty("name") @Nullable String str, @JsonProperty("overview") @Nullable String str2, @JsonProperty("poster_path") @Nullable String str3, @JsonProperty("air_date") @Nullable String str4, @JsonProperty("vote_average") @Nullable Double d) {
            this.seasonNumber = i;
            this.episodes = list;
            this.id = num;
            this.mongoId = num2;
            this.name = str;
            this.overview = str2;
            this.posterPath = str3;
            this.airDate = str4;
            this.voteAverage = d;
        }

        public /* synthetic */ SeasonDetails(int i, List list, Integer num, Integer num2, String str, String str2, String str3, String str4, Double d, int i2, DefaultConstructorMarker defaultConstructorMarker) {
            this(i, list, (i2 & 4) != 0 ? null : num, (i2 & 8) != 0 ? null : num2, (i2 & 16) != 0 ? null : str, (i2 & 32) != 0 ? null : str2, (i2 & 64) != 0 ? null : str3, (i2 & 128) != 0 ? null : str4, (i2 & 256) != 0 ? null : d);
        }

        public static /* synthetic */ SeasonDetails copy$default(SeasonDetails seasonDetails, int i, List list, Integer num, Integer num2, String str, String str2, String str3, String str4, Double d, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                i = seasonDetails.seasonNumber;
            }
            if ((i2 & 2) != 0) {
                list = seasonDetails.episodes;
            }
            if ((i2 & 4) != 0) {
                num = seasonDetails.id;
            }
            if ((i2 & 8) != 0) {
                num2 = seasonDetails.mongoId;
            }
            if ((i2 & 16) != 0) {
                str = seasonDetails.name;
            }
            if ((i2 & 32) != 0) {
                str2 = seasonDetails.overview;
            }
            if ((i2 & 64) != 0) {
                str3 = seasonDetails.posterPath;
            }
            if ((i2 & 128) != 0) {
                str4 = seasonDetails.airDate;
            }
            if ((i2 & 256) != 0) {
                d = seasonDetails.voteAverage;
            }
            String str5 = str4;
            Double d2 = d;
            String str6 = str2;
            String str7 = str3;
            String str8 = str;
            Integer num3 = num;
            return seasonDetails.copy(i, list, num3, num2, str8, str6, str7, str5, d2);
        }

        /* renamed from: component1, reason: from getter */
        public final int getSeasonNumber() {
            return this.seasonNumber;
        }

        @NotNull
        public final List<TvEpisode> component2() {
            return this.episodes;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final Integer getMongoId() {
            return this.mongoId;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final String getPosterPath() {
            return this.posterPath;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final String getAirDate() {
            return this.airDate;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @NotNull
        public final SeasonDetails copy(@JsonProperty("season_number") int seasonNumber, @JsonProperty("episodes") @NotNull List<TvEpisode> episodes, @JsonProperty("id") @Nullable Integer id, @JsonProperty("_id") @Nullable Integer mongoId, @JsonProperty("name") @Nullable String name, @JsonProperty("overview") @Nullable String overview, @JsonProperty("poster_path") @Nullable String posterPath, @JsonProperty("air_date") @Nullable String airDate, @JsonProperty("vote_average") @Nullable Double voteAverage) {
            return new SeasonDetails(seasonNumber, episodes, id, mongoId, name, overview, posterPath, airDate, voteAverage);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SeasonDetails)) {
                return false;
            }
            SeasonDetails seasonDetails = (SeasonDetails) other;
            return this.seasonNumber == seasonDetails.seasonNumber && Intrinsics.areEqual(this.episodes, seasonDetails.episodes) && Intrinsics.areEqual(this.id, seasonDetails.id) && Intrinsics.areEqual(this.mongoId, seasonDetails.mongoId) && Intrinsics.areEqual(this.name, seasonDetails.name) && Intrinsics.areEqual(this.overview, seasonDetails.overview) && Intrinsics.areEqual(this.posterPath, seasonDetails.posterPath) && Intrinsics.areEqual(this.airDate, seasonDetails.airDate) && Intrinsics.areEqual(this.voteAverage, seasonDetails.voteAverage);
        }

        @Nullable
        public final String getAirDate() {
            return this.airDate;
        }

        @NotNull
        public final List<TvEpisode> getEpisodes() {
            return this.episodes;
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        public final Integer getMongoId() {
            return this.mongoId;
        }

        @Nullable
        public final String getName() {
            return this.name;
        }

        @Nullable
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        public final String getPosterPath() {
            return this.posterPath;
        }

        public final int getSeasonNumber() {
            return this.seasonNumber;
        }

        @Nullable
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        public int hashCode() {
            return (((((((((((((((this.seasonNumber * 31) + this.episodes.hashCode()) * 31) + (this.id == null ? 0 : this.id.hashCode())) * 31) + (this.mongoId == null ? 0 : this.mongoId.hashCode())) * 31) + (this.name == null ? 0 : this.name.hashCode())) * 31) + (this.overview == null ? 0 : this.overview.hashCode())) * 31) + (this.posterPath == null ? 0 : this.posterPath.hashCode())) * 31) + (this.airDate == null ? 0 : this.airDate.hashCode())) * 31) + (this.voteAverage != null ? this.voteAverage.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "SeasonDetails(seasonNumber=" + this.seasonNumber + ", episodes=" + this.episodes + ", id=" + this.id + ", mongoId=" + this.mongoId + ", name=" + this.name + ", overview=" + this.overview + ", posterPath=" + this.posterPath + ", airDate=" + this.airDate + ", voteAverage=" + this.voteAverage + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0006\n\u0002\b\u001b\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001Ba\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\f¢\u0006\u0004\b\r\u0010\u000eJ\t\u0010\u001d\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001e\u001a\u00020\u0005HÆ\u0003J\t\u0010\u001f\u001a\u00020\u0003HÆ\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010\"\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u0017J\u000b\u0010#\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010$\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010\u001bJh\u0010%\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\b\b\u0003\u0010\u0006\u001a\u00020\u00032\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\fHÆ\u0001¢\u0006\u0002\u0010&J\u0014\u0010'\u001a\u00020(2\b\u0010)\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010*\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010+\u001a\u00020\u0005HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0016\u0010\u0006\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0010R\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0012R\u0018\u0010\b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0012R\u001a\u0010\t\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0018\u001a\u0004\b\u0016\u0010\u0017R\u0018\u0010\n\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0012R\u001a\u0010\u000b\u001a\u0004\u0018\u00010\f8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001c\u001a\u0004\b\u001a\u0010\u001b¨\u0006,"}, d2 = {"Lcom/cncverse/DoFlixProvider$SeasonInfo;", "", "seasonNumber", "", "name", "", "episodeCount", "posterPath", "airDate", "id", "overview", "voteAverage", "", "<init>", "(ILjava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Double;)V", "getSeasonNumber", "()I", "getName", "()Ljava/lang/String;", "getEpisodeCount", "getPosterPath", "getAirDate", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getOverview", "getVoteAverage", "()Ljava/lang/Double;", "Ljava/lang/Double;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "(ILjava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Double;)Lcom/cncverse/DoFlixProvider$SeasonInfo;", "equals", "", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class SeasonInfo {

        @JsonProperty("air_date")
        @Nullable
        private final String airDate;

        @JsonProperty("episode_count")
        private final int episodeCount;

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        @JsonProperty("name")
        @NotNull
        private final String name;

        @JsonProperty("overview")
        @Nullable
        private final String overview;

        @JsonProperty("poster_path")
        @Nullable
        private final String posterPath;

        @JsonProperty("season_number")
        private final int seasonNumber;

        @JsonProperty("vote_average")
        @Nullable
        private final Double voteAverage;

        public SeasonInfo(@JsonProperty("season_number") int i, @JsonProperty("name") @NotNull String str, @JsonProperty("episode_count") int i2, @JsonProperty("poster_path") @Nullable String str2, @JsonProperty("air_date") @Nullable String str3, @JsonProperty("id") @Nullable Integer num, @JsonProperty("overview") @Nullable String str4, @JsonProperty("vote_average") @Nullable Double d) {
            this.seasonNumber = i;
            this.name = str;
            this.episodeCount = i2;
            this.posterPath = str2;
            this.airDate = str3;
            this.id = num;
            this.overview = str4;
            this.voteAverage = d;
        }

        public /* synthetic */ SeasonInfo(int i, String str, int i2, String str2, String str3, Integer num, String str4, Double d, int i3, DefaultConstructorMarker defaultConstructorMarker) {
            this(i, str, i2, str2, str3, (i3 & 32) != 0 ? null : num, (i3 & 64) != 0 ? null : str4, (i3 & 128) != 0 ? null : d);
        }

        public static /* synthetic */ SeasonInfo copy$default(SeasonInfo seasonInfo, int i, String str, int i2, String str2, String str3, Integer num, String str4, Double d, int i3, Object obj) {
            if ((i3 & 1) != 0) {
                i = seasonInfo.seasonNumber;
            }
            if ((i3 & 2) != 0) {
                str = seasonInfo.name;
            }
            if ((i3 & 4) != 0) {
                i2 = seasonInfo.episodeCount;
            }
            if ((i3 & 8) != 0) {
                str2 = seasonInfo.posterPath;
            }
            if ((i3 & 16) != 0) {
                str3 = seasonInfo.airDate;
            }
            if ((i3 & 32) != 0) {
                num = seasonInfo.id;
            }
            if ((i3 & 64) != 0) {
                str4 = seasonInfo.overview;
            }
            if ((i3 & 128) != 0) {
                d = seasonInfo.voteAverage;
            }
            String str5 = str4;
            Double d2 = d;
            String str6 = str3;
            Integer num2 = num;
            return seasonInfo.copy(i, str, i2, str2, str6, num2, str5, d2);
        }

        /* renamed from: component1, reason: from getter */
        public final int getSeasonNumber() {
            return this.seasonNumber;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getName() {
            return this.name;
        }

        /* renamed from: component3, reason: from getter */
        public final int getEpisodeCount() {
            return this.episodeCount;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final String getPosterPath() {
            return this.posterPath;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getAirDate() {
            return this.airDate;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @NotNull
        public final SeasonInfo copy(@JsonProperty("season_number") int seasonNumber, @JsonProperty("name") @NotNull String name, @JsonProperty("episode_count") int episodeCount, @JsonProperty("poster_path") @Nullable String posterPath, @JsonProperty("air_date") @Nullable String airDate, @JsonProperty("id") @Nullable Integer id, @JsonProperty("overview") @Nullable String overview, @JsonProperty("vote_average") @Nullable Double voteAverage) {
            return new SeasonInfo(seasonNumber, name, episodeCount, posterPath, airDate, id, overview, voteAverage);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SeasonInfo)) {
                return false;
            }
            SeasonInfo seasonInfo = (SeasonInfo) other;
            return this.seasonNumber == seasonInfo.seasonNumber && Intrinsics.areEqual(this.name, seasonInfo.name) && this.episodeCount == seasonInfo.episodeCount && Intrinsics.areEqual(this.posterPath, seasonInfo.posterPath) && Intrinsics.areEqual(this.airDate, seasonInfo.airDate) && Intrinsics.areEqual(this.id, seasonInfo.id) && Intrinsics.areEqual(this.overview, seasonInfo.overview) && Intrinsics.areEqual(this.voteAverage, seasonInfo.voteAverage);
        }

        @Nullable
        public final String getAirDate() {
            return this.airDate;
        }

        public final int getEpisodeCount() {
            return this.episodeCount;
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        @NotNull
        public final String getName() {
            return this.name;
        }

        @Nullable
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        public final String getPosterPath() {
            return this.posterPath;
        }

        public final int getSeasonNumber() {
            return this.seasonNumber;
        }

        @Nullable
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        public int hashCode() {
            return (((((((((((((this.seasonNumber * 31) + this.name.hashCode()) * 31) + this.episodeCount) * 31) + (this.posterPath == null ? 0 : this.posterPath.hashCode())) * 31) + (this.airDate == null ? 0 : this.airDate.hashCode())) * 31) + (this.id == null ? 0 : this.id.hashCode())) * 31) + (this.overview == null ? 0 : this.overview.hashCode())) * 31) + (this.voteAverage != null ? this.voteAverage.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "SeasonInfo(seasonNumber=" + this.seasonNumber + ", name=" + this.name + ", episodeCount=" + this.episodeCount + ", posterPath=" + this.posterPath + ", airDate=" + this.airDate + ", id=" + this.id + ", overview=" + this.overview + ", voteAverage=" + this.voteAverage + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B5\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0001\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0001\u0010\b\u001a\u00020\u0003¢\u0006\u0004\b\t\u0010\nJ\t\u0010\u0011\u001a\u00020\u0003HÆ\u0003J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005HÆ\u0003J\t\u0010\u0013\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0014\u001a\u00020\u0003HÆ\u0003J7\u0010\u0015\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\u000e\b\u0003\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0003\u0010\u0007\u001a\u00020\u00032\b\b\u0003\u0010\b\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0019\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010\u001a\u001a\u00020\u001bHÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001c\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u0007\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\fR\u0016\u0010\b\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\f¨\u0006\u001c"}, d2 = {"Lcom/cncverse/DoFlixProvider$SeriesPageApiResponse;", "", "page", "", "results", "", "Lcom/cncverse/DoFlixProvider$SeriesResult;", "totalPages", "totalResults", "<init>", "(ILjava/util/List;II)V", "getPage", "()I", "getResults", "()Ljava/util/List;", "getTotalPages", "getTotalResults", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "toString", "", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class SeriesPageApiResponse {

        @JsonProperty("page")
        private final int page;

        @JsonProperty("results")
        @NotNull
        private final List<SeriesResult> results;

        @JsonProperty("total_pages")
        private final int totalPages;

        @JsonProperty("total_results")
        private final int totalResults;

        public SeriesPageApiResponse(@JsonProperty("page") int i, @JsonProperty("results") @NotNull List<SeriesResult> list, @JsonProperty("total_pages") int i2, @JsonProperty("total_results") int i3) {
            this.page = i;
            this.results = list;
            this.totalPages = i2;
            this.totalResults = i3;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ SeriesPageApiResponse copy$default(SeriesPageApiResponse seriesPageApiResponse, int i, List list, int i2, int i3, int i4, Object obj) {
            if ((i4 & 1) != 0) {
                i = seriesPageApiResponse.page;
            }
            if ((i4 & 2) != 0) {
                list = seriesPageApiResponse.results;
            }
            if ((i4 & 4) != 0) {
                i2 = seriesPageApiResponse.totalPages;
            }
            if ((i4 & 8) != 0) {
                i3 = seriesPageApiResponse.totalResults;
            }
            return seriesPageApiResponse.copy(i, list, i2, i3);
        }

        /* renamed from: component1, reason: from getter */
        public final int getPage() {
            return this.page;
        }

        @NotNull
        public final List<SeriesResult> component2() {
            return this.results;
        }

        /* renamed from: component3, reason: from getter */
        public final int getTotalPages() {
            return this.totalPages;
        }

        /* renamed from: component4, reason: from getter */
        public final int getTotalResults() {
            return this.totalResults;
        }

        @NotNull
        public final SeriesPageApiResponse copy(@JsonProperty("page") int page, @JsonProperty("results") @NotNull List<SeriesResult> results, @JsonProperty("total_pages") int totalPages, @JsonProperty("total_results") int totalResults) {
            return new SeriesPageApiResponse(page, results, totalPages, totalResults);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SeriesPageApiResponse)) {
                return false;
            }
            SeriesPageApiResponse seriesPageApiResponse = (SeriesPageApiResponse) other;
            return this.page == seriesPageApiResponse.page && Intrinsics.areEqual(this.results, seriesPageApiResponse.results) && this.totalPages == seriesPageApiResponse.totalPages && this.totalResults == seriesPageApiResponse.totalResults;
        }

        public final int getPage() {
            return this.page;
        }

        @NotNull
        public final List<SeriesResult> getResults() {
            return this.results;
        }

        public final int getTotalPages() {
            return this.totalPages;
        }

        public final int getTotalResults() {
            return this.totalResults;
        }

        public int hashCode() {
            return (((((this.page * 31) + this.results.hashCode()) * 31) + this.totalPages) * 31) + this.totalResults;
        }

        @NotNull
        public String toString() {
            return "SeriesPageApiResponse(page=" + this.page + ", results=" + this.results + ", totalPages=" + this.totalPages + ", totalResults=" + this.totalResults + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b+\b\u0087\b\u0018\u00002\u00020\u0001B«\u0001\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0001\u0010\b\u001a\u00020\u0005\u0012\b\b\u0001\u0010\t\u001a\u00020\u0005\u0012\b\b\u0001\u0010\n\u001a\u00020\u000b\u0012\b\b\u0001\u0010\f\u001a\u00020\u0003\u0012\b\b\u0001\u0010\r\u001a\u00020\u000b\u0012\b\b\u0001\u0010\u000e\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u000f\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0010\u001a\u00020\u0005\u0012\u000e\b\u0001\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00030\u0012\u0012\u000e\b\u0001\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00050\u0012\u0012\b\b\u0001\u0010\u0014\u001a\u00020\u0015¢\u0006\u0004\b\u0016\u0010\u0017J\t\u0010,\u001a\u00020\u0003HÆ\u0003J\t\u0010-\u001a\u00020\u0005HÆ\u0003J\u000b\u0010.\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\t\u0010/\u001a\u00020\u0005HÆ\u0003J\t\u00100\u001a\u00020\u0005HÆ\u0003J\t\u00101\u001a\u00020\u0005HÆ\u0003J\t\u00102\u001a\u00020\u000bHÆ\u0003J\t\u00103\u001a\u00020\u0003HÆ\u0003J\t\u00104\u001a\u00020\u000bHÆ\u0003J\t\u00105\u001a\u00020\u0005HÆ\u0003J\t\u00106\u001a\u00020\u0005HÆ\u0003J\t\u00107\u001a\u00020\u0005HÆ\u0003J\u000f\u00108\u001a\b\u0012\u0004\u0012\u00020\u00030\u0012HÆ\u0003J\u000f\u00109\u001a\b\u0012\u0004\u0012\u00020\u00050\u0012HÆ\u0003J\t\u0010:\u001a\u00020\u0015HÆ\u0003J\u00ad\u0001\u0010;\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u00052\b\b\u0003\u0010\u0007\u001a\u00020\u00052\b\b\u0003\u0010\b\u001a\u00020\u00052\b\b\u0003\u0010\t\u001a\u00020\u00052\b\b\u0003\u0010\n\u001a\u00020\u000b2\b\b\u0003\u0010\f\u001a\u00020\u00032\b\b\u0003\u0010\r\u001a\u00020\u000b2\b\b\u0003\u0010\u000e\u001a\u00020\u00052\b\b\u0003\u0010\u000f\u001a\u00020\u00052\b\b\u0003\u0010\u0010\u001a\u00020\u00052\u000e\b\u0003\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00030\u00122\u000e\b\u0003\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00050\u00122\b\b\u0003\u0010\u0014\u001a\u00020\u0015HÆ\u0001J\u0014\u0010<\u001a\u00020\u00152\b\u0010=\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010>\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010?\u001a\u00020\u0005HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001bR\u0016\u0010\u0007\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001bR\u0016\u0010\b\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001bR\u0016\u0010\t\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001bR\u0016\u0010\n\u001a\u00020\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0016\u0010\f\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0019R\u0016\u0010\r\u001a\u00020\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b#\u0010!R\u0016\u0010\u000e\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001bR\u0016\u0010\u000f\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001bR\u0016\u0010\u0010\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001bR\u001c\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00030\u00128\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b'\u0010(R\u001c\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00050\u00128\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b)\u0010(R\u0016\u0010\u0014\u001a\u00020\u00158\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b*\u0010+¨\u0006@"}, d2 = {"Lcom/cncverse/DoFlixProvider$SeriesResult;", "", "id", "", "name", "", "customPosterTag", "originalName", "overview", "firstAirDate", "popularity", "", "voteCount", "voteAverage", "posterPath", "backdropPath", "originalLanguage", "genreIds", "", "originCountry", "adult", "", "<init>", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DIDLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Z)V", "getId", "()I", "getName", "()Ljava/lang/String;", "getCustomPosterTag", "getOriginalName", "getOverview", "getFirstAirDate", "getPopularity", "()D", "getVoteCount", "getVoteAverage", "getPosterPath", "getBackdropPath", "getOriginalLanguage", "getGenreIds", "()Ljava/util/List;", "getOriginCountry", "getAdult", "()Z", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "copy", "equals", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class SeriesResult {

        @JsonProperty("adult")
        private final boolean adult;

        @JsonProperty("backdrop_path")
        @NotNull
        private final String backdropPath;

        @JsonProperty("custom_poster_tag")
        @Nullable
        private final String customPosterTag;

        @JsonProperty("first_air_date")
        @NotNull
        private final String firstAirDate;

        @JsonProperty("genre_ids")
        @NotNull
        private final List<Integer> genreIds;

        @JsonProperty("id")
        private final int id;

        @JsonProperty("name")
        @NotNull
        private final String name;

        @JsonProperty("origin_country")
        @NotNull
        private final List<String> originCountry;

        @JsonProperty("original_language")
        @NotNull
        private final String originalLanguage;

        @JsonProperty("original_name")
        @NotNull
        private final String originalName;

        @JsonProperty("overview")
        @NotNull
        private final String overview;

        @JsonProperty("popularity")
        private final double popularity;

        @JsonProperty("poster_path")
        @NotNull
        private final String posterPath;

        @JsonProperty("vote_average")
        private final double voteAverage;

        @JsonProperty("vote_count")
        private final int voteCount;

        public SeriesResult(@JsonProperty("id") int i, @JsonProperty("name") @NotNull String str, @JsonProperty("custom_poster_tag") @Nullable String str2, @JsonProperty("original_name") @NotNull String str3, @JsonProperty("overview") @NotNull String str4, @JsonProperty("first_air_date") @NotNull String str5, @JsonProperty("popularity") double d, @JsonProperty("vote_count") int i2, @JsonProperty("vote_average") double d2, @JsonProperty("poster_path") @NotNull String str6, @JsonProperty("backdrop_path") @NotNull String str7, @JsonProperty("original_language") @NotNull String str8, @JsonProperty("genre_ids") @NotNull List<Integer> list, @JsonProperty("origin_country") @NotNull List<String> list2, @JsonProperty("adult") boolean z) {
            this.id = i;
            this.name = str;
            this.customPosterTag = str2;
            this.originalName = str3;
            this.overview = str4;
            this.firstAirDate = str5;
            this.popularity = d;
            this.voteCount = i2;
            this.voteAverage = d2;
            this.posterPath = str6;
            this.backdropPath = str7;
            this.originalLanguage = str8;
            this.genreIds = list;
            this.originCountry = list2;
            this.adult = z;
        }

        public static /* synthetic */ SeriesResult copy$default(SeriesResult seriesResult, int i, String str, String str2, String str3, String str4, String str5, double d, int i2, double d2, String str6, String str7, String str8, List list, List list2, boolean z, int i3, Object obj) {
            int i4 = (i3 & 1) != 0 ? seriesResult.id : i;
            return seriesResult.copy(i4, (i3 & 2) != 0 ? seriesResult.name : str, (i3 & 4) != 0 ? seriesResult.customPosterTag : str2, (i3 & 8) != 0 ? seriesResult.originalName : str3, (i3 & 16) != 0 ? seriesResult.overview : str4, (i3 & 32) != 0 ? seriesResult.firstAirDate : str5, (i3 & 64) != 0 ? seriesResult.popularity : d, (i3 & 128) != 0 ? seriesResult.voteCount : i2, (i3 & 256) != 0 ? seriesResult.voteAverage : d2, (i3 & 512) != 0 ? seriesResult.posterPath : str6, (i3 & 1024) != 0 ? seriesResult.backdropPath : str7, (i3 & 2048) != 0 ? seriesResult.originalLanguage : str8, (i3 & 4096) != 0 ? seriesResult.genreIds : list, (i3 & 8192) != 0 ? seriesResult.originCountry : list2, (i3 & 16384) != 0 ? seriesResult.adult : z);
        }

        /* renamed from: component1, reason: from getter */
        public final int getId() {
            return this.id;
        }

        @NotNull
        /* renamed from: component10, reason: from getter */
        public final String getPosterPath() {
            return this.posterPath;
        }

        @NotNull
        /* renamed from: component11, reason: from getter */
        public final String getBackdropPath() {
            return this.backdropPath;
        }

        @NotNull
        /* renamed from: component12, reason: from getter */
        public final String getOriginalLanguage() {
            return this.originalLanguage;
        }

        @NotNull
        public final List<Integer> component13() {
            return this.genreIds;
        }

        @NotNull
        public final List<String> component14() {
            return this.originCountry;
        }

        /* renamed from: component15, reason: from getter */
        public final boolean getAdult() {
            return this.adult;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final String getCustomPosterTag() {
            return this.customPosterTag;
        }

        @NotNull
        /* renamed from: component4, reason: from getter */
        public final String getOriginalName() {
            return this.originalName;
        }

        @NotNull
        /* renamed from: component5, reason: from getter */
        public final String getOverview() {
            return this.overview;
        }

        @NotNull
        /* renamed from: component6, reason: from getter */
        public final String getFirstAirDate() {
            return this.firstAirDate;
        }

        /* renamed from: component7, reason: from getter */
        public final double getPopularity() {
            return this.popularity;
        }

        /* renamed from: component8, reason: from getter */
        public final int getVoteCount() {
            return this.voteCount;
        }

        /* renamed from: component9, reason: from getter */
        public final double getVoteAverage() {
            return this.voteAverage;
        }

        @NotNull
        public final SeriesResult copy(@JsonProperty("id") int id, @JsonProperty("name") @NotNull String name, @JsonProperty("custom_poster_tag") @Nullable String customPosterTag, @JsonProperty("original_name") @NotNull String originalName, @JsonProperty("overview") @NotNull String overview, @JsonProperty("first_air_date") @NotNull String firstAirDate, @JsonProperty("popularity") double popularity, @JsonProperty("vote_count") int voteCount, @JsonProperty("vote_average") double voteAverage, @JsonProperty("poster_path") @NotNull String posterPath, @JsonProperty("backdrop_path") @NotNull String backdropPath, @JsonProperty("original_language") @NotNull String originalLanguage, @JsonProperty("genre_ids") @NotNull List<Integer> genreIds, @JsonProperty("origin_country") @NotNull List<String> originCountry, @JsonProperty("adult") boolean adult) {
            return new SeriesResult(id, name, customPosterTag, originalName, overview, firstAirDate, popularity, voteCount, voteAverage, posterPath, backdropPath, originalLanguage, genreIds, originCountry, adult);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SeriesResult)) {
                return false;
            }
            SeriesResult seriesResult = (SeriesResult) other;
            return this.id == seriesResult.id && Intrinsics.areEqual(this.name, seriesResult.name) && Intrinsics.areEqual(this.customPosterTag, seriesResult.customPosterTag) && Intrinsics.areEqual(this.originalName, seriesResult.originalName) && Intrinsics.areEqual(this.overview, seriesResult.overview) && Intrinsics.areEqual(this.firstAirDate, seriesResult.firstAirDate) && Double.compare(this.popularity, seriesResult.popularity) == 0 && this.voteCount == seriesResult.voteCount && Double.compare(this.voteAverage, seriesResult.voteAverage) == 0 && Intrinsics.areEqual(this.posterPath, seriesResult.posterPath) && Intrinsics.areEqual(this.backdropPath, seriesResult.backdropPath) && Intrinsics.areEqual(this.originalLanguage, seriesResult.originalLanguage) && Intrinsics.areEqual(this.genreIds, seriesResult.genreIds) && Intrinsics.areEqual(this.originCountry, seriesResult.originCountry) && this.adult == seriesResult.adult;
        }

        public final boolean getAdult() {
            return this.adult;
        }

        @NotNull
        public final String getBackdropPath() {
            return this.backdropPath;
        }

        @Nullable
        public final String getCustomPosterTag() {
            return this.customPosterTag;
        }

        @NotNull
        public final String getFirstAirDate() {
            return this.firstAirDate;
        }

        @NotNull
        public final List<Integer> getGenreIds() {
            return this.genreIds;
        }

        public final int getId() {
            return this.id;
        }

        @NotNull
        public final String getName() {
            return this.name;
        }

        @NotNull
        public final List<String> getOriginCountry() {
            return this.originCountry;
        }

        @NotNull
        public final String getOriginalLanguage() {
            return this.originalLanguage;
        }

        @NotNull
        public final String getOriginalName() {
            return this.originalName;
        }

        @NotNull
        public final String getOverview() {
            return this.overview;
        }

        public final double getPopularity() {
            return this.popularity;
        }

        @NotNull
        public final String getPosterPath() {
            return this.posterPath;
        }

        public final double getVoteAverage() {
            return this.voteAverage;
        }

        public final int getVoteCount() {
            return this.voteCount;
        }

        public int hashCode() {
            return (((((((((((((((((((((((((((this.id * 31) + this.name.hashCode()) * 31) + (this.customPosterTag == null ? 0 : this.customPosterTag.hashCode())) * 31) + this.originalName.hashCode()) * 31) + this.overview.hashCode()) * 31) + this.firstAirDate.hashCode()) * 31) + DoFlixProvider$SeriesResult$$ExternalSyntheticBackport0.m(this.popularity)) * 31) + this.voteCount) * 31) + DoFlixProvider$SeriesResult$$ExternalSyntheticBackport0.m(this.voteAverage)) * 31) + this.posterPath.hashCode()) * 31) + this.backdropPath.hashCode()) * 31) + this.originalLanguage.hashCode()) * 31) + this.genreIds.hashCode()) * 31) + this.originCountry.hashCode()) * 31) + DoFlixProvider$SeriesResult$$ExternalSyntheticBackport1.m(this.adult);
        }

        @NotNull
        public String toString() {
            return "SeriesResult(id=" + this.id + ", name=" + this.name + ", customPosterTag=" + this.customPosterTag + ", originalName=" + this.originalName + ", overview=" + this.overview + ", firstAirDate=" + this.firstAirDate + ", popularity=" + this.popularity + ", voteCount=" + this.voteCount + ", voteAverage=" + this.voteAverage + ", posterPath=" + this.posterPath + ", backdropPath=" + this.backdropPath + ", originalLanguage=" + this.originalLanguage + ", genreIds=" + this.genreIds + ", originCountry=" + this.originCountry + ", adult=" + this.adult + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B;\u0012\u000e\b\u0001\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0006¢\u0006\u0004\b\t\u0010\nJ\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\u000eJ\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\u000eJ\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\u000eJB\u0010\u0016\u001a\u00020\u00002\u000e\b\u0003\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u00062\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00062\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0006HÆ\u0001¢\u0006\u0002\u0010\u0017J\u0014\u0010\u0018\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u001b\u001a\u00020\u0006HÖ\u0081\u0004J\n\u0010\u001c\u001a\u00020\u001dHÖ\u0081\u0004R\u001c\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001a\u0010\u0005\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\r\u0010\u000eR\u001a\u0010\u0007\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\u0010\u0010\u000eR\u001a\u0010\b\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\u0011\u0010\u000e¨\u0006\u001e"}, d2 = {"Lcom/cncverse/DoFlixProvider$SimilarResults;", "", "results", "", "Lcom/cncverse/DoFlixProvider$MovieResult;", "page", "", "totalPages", "totalResults", "<init>", "(Ljava/util/List;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;)V", "getResults", "()Ljava/util/List;", "getPage", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getTotalPages", "getTotalResults", "component1", "component2", "component3", "component4", "copy", "(Ljava/util/List;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;)Lcom/cncverse/DoFlixProvider$SimilarResults;", "equals", "", "other", "hashCode", "toString", "", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class SimilarResults {

        @JsonProperty("page")
        @Nullable
        private final Integer page;

        @JsonProperty("results")
        @NotNull
        private final List<MovieResult> results;

        @JsonProperty("total_pages")
        @Nullable
        private final Integer totalPages;

        @JsonProperty("total_results")
        @Nullable
        private final Integer totalResults;

        public SimilarResults(@JsonProperty("results") @NotNull List<MovieResult> list, @JsonProperty("page") @Nullable Integer num, @JsonProperty("total_pages") @Nullable Integer num2, @JsonProperty("total_results") @Nullable Integer num3) {
            this.results = list;
            this.page = num;
            this.totalPages = num2;
            this.totalResults = num3;
        }

        public /* synthetic */ SimilarResults(List list, Integer num, Integer num2, Integer num3, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(list, (i & 2) != 0 ? null : num, (i & 4) != 0 ? null : num2, (i & 8) != 0 ? null : num3);
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ SimilarResults copy$default(SimilarResults similarResults, List list, Integer num, Integer num2, Integer num3, int i, Object obj) {
            if ((i & 1) != 0) {
                list = similarResults.results;
            }
            if ((i & 2) != 0) {
                num = similarResults.page;
            }
            if ((i & 4) != 0) {
                num2 = similarResults.totalPages;
            }
            if ((i & 8) != 0) {
                num3 = similarResults.totalResults;
            }
            return similarResults.copy(list, num, num2, num3);
        }

        @NotNull
        public final List<MovieResult> component1() {
            return this.results;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final Integer getPage() {
            return this.page;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final Integer getTotalPages() {
            return this.totalPages;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final Integer getTotalResults() {
            return this.totalResults;
        }

        @NotNull
        public final SimilarResults copy(@JsonProperty("results") @NotNull List<MovieResult> results, @JsonProperty("page") @Nullable Integer page, @JsonProperty("total_pages") @Nullable Integer totalPages, @JsonProperty("total_results") @Nullable Integer totalResults) {
            return new SimilarResults(results, page, totalPages, totalResults);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SimilarResults)) {
                return false;
            }
            SimilarResults similarResults = (SimilarResults) other;
            return Intrinsics.areEqual(this.results, similarResults.results) && Intrinsics.areEqual(this.page, similarResults.page) && Intrinsics.areEqual(this.totalPages, similarResults.totalPages) && Intrinsics.areEqual(this.totalResults, similarResults.totalResults);
        }

        @Nullable
        public final Integer getPage() {
            return this.page;
        }

        @NotNull
        public final List<MovieResult> getResults() {
            return this.results;
        }

        @Nullable
        public final Integer getTotalPages() {
            return this.totalPages;
        }

        @Nullable
        public final Integer getTotalResults() {
            return this.totalResults;
        }

        public int hashCode() {
            return (((((this.results.hashCode() * 31) + (this.page == null ? 0 : this.page.hashCode())) * 31) + (this.totalPages == null ? 0 : this.totalPages.hashCode())) * 31) + (this.totalResults != null ? this.totalResults.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "SimilarResults(results=" + this.results + ", page=" + this.page + ", totalPages=" + this.totalPages + ", totalResults=" + this.totalResults + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b'\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B\u008f\u0001\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0003\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\b\u0012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\b\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\b\u0012\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\b\u0012\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\b\u0012\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0010\u0010\u0011J\t\u0010!\u001a\u00020\u0003HÆ\u0003J\t\u0010\"\u001a\u00020\u0003HÆ\u0003J\t\u0010#\u001a\u00020\u0003HÆ\u0003J\t\u0010$\u001a\u00020\u0003HÆ\u0003J\u0010\u0010%\u001a\u0004\u0018\u00010\bHÆ\u0003¢\u0006\u0002\u0010\u0018J\u0010\u0010&\u001a\u0004\u0018\u00010\bHÆ\u0003¢\u0006\u0002\u0010\u0018J\u0010\u0010'\u001a\u0004\u0018\u00010\bHÆ\u0003¢\u0006\u0002\u0010\u0018J\u0010\u0010(\u001a\u0004\u0018\u00010\bHÆ\u0003¢\u0006\u0002\u0010\u0018J\u0010\u0010)\u001a\u0004\u0018\u00010\bHÆ\u0003¢\u0006\u0002\u0010\u0018J\u0010\u0010*\u001a\u0004\u0018\u00010\bHÆ\u0003¢\u0006\u0002\u0010\u0018J\u000b\u0010+\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010,\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0096\u0001\u0010-\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00032\b\b\u0003\u0010\u0005\u001a\u00020\u00032\b\b\u0003\u0010\u0006\u001a\u00020\u00032\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\b2\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\b2\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\b2\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\b2\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\b2\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0003HÆ\u0001¢\u0006\u0002\u0010.J\u0014\u0010/\u001a\u0002002\b\u00101\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u00102\u001a\u00020\bHÖ\u0081\u0004J\n\u00103\u001a\u00020\u0003HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0016\u0010\u0004\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0013R\u0016\u0010\u0005\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u0016\u0010\u0006\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0013R\u001a\u0010\u0007\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u0017\u0010\u0018R\u001a\u0010\t\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u001a\u0010\u0018R\u001a\u0010\n\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u001b\u0010\u0018R\u001a\u0010\u000b\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u001c\u0010\u0018R\u001a\u0010\f\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u001d\u0010\u0018R\u001a\u0010\r\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u001e\u0010\u0018R\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0013R\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b \u0010\u0013¨\u00064"}, d2 = {"Lcom/cncverse/DoFlixProvider$StreamLink;", "", "host", "", "url", "quality", "size", "id", "", "movieId", "tvShowId", "seasonNumber", "episodeNumber", "order", "createdAt", "updatedAt", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;)V", "getHost", "()Ljava/lang/String;", "getUrl", "getQuality", "getSize", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getMovieId", "getTvShowId", "getSeasonNumber", "getEpisodeNumber", "getOrder", "getCreatedAt", "getUpdatedAt", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;)Lcom/cncverse/DoFlixProvider$StreamLink;", "equals", "", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class StreamLink {

        @JsonProperty("created_at")
        @Nullable
        private final String createdAt;

        @JsonProperty("episode_number")
        @Nullable
        private final Integer episodeNumber;

        @JsonProperty("host")
        @NotNull
        private final String host;

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        @JsonProperty("movie_id")
        @Nullable
        private final Integer movieId;

        @JsonProperty("order")
        @Nullable
        private final Integer order;

        @JsonProperty("quality")
        @NotNull
        private final String quality;

        @JsonProperty("season_number")
        @Nullable
        private final Integer seasonNumber;

        @JsonProperty("size")
        @NotNull
        private final String size;

        @JsonProperty("tv_show_id")
        @Nullable
        private final Integer tvShowId;

        @JsonProperty("updated_at")
        @Nullable
        private final String updatedAt;

        @JsonProperty("url")
        @NotNull
        private final String url;

        public StreamLink(@JsonProperty("host") @NotNull String str, @JsonProperty("url") @NotNull String str2, @JsonProperty("quality") @NotNull String str3, @JsonProperty("size") @NotNull String str4, @JsonProperty("id") @Nullable Integer num, @JsonProperty("movie_id") @Nullable Integer num2, @JsonProperty("tv_show_id") @Nullable Integer num3, @JsonProperty("season_number") @Nullable Integer num4, @JsonProperty("episode_number") @Nullable Integer num5, @JsonProperty("order") @Nullable Integer num6, @JsonProperty("created_at") @Nullable String str5, @JsonProperty("updated_at") @Nullable String str6) {
            this.host = str;
            this.url = str2;
            this.quality = str3;
            this.size = str4;
            this.id = num;
            this.movieId = num2;
            this.tvShowId = num3;
            this.seasonNumber = num4;
            this.episodeNumber = num5;
            this.order = num6;
            this.createdAt = str5;
            this.updatedAt = str6;
        }

        public /* synthetic */ StreamLink(String str, String str2, String str3, String str4, Integer num, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6, String str5, String str6, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(str, str2, str3, str4, (i & 16) != 0 ? null : num, (i & 32) != 0 ? null : num2, (i & 64) != 0 ? null : num3, (i & 128) != 0 ? null : num4, (i & 256) != 0 ? null : num5, (i & 512) != 0 ? null : num6, (i & 1024) != 0 ? null : str5, (i & 2048) != 0 ? null : str6);
        }

        public static /* synthetic */ StreamLink copy$default(StreamLink streamLink, String str, String str2, String str3, String str4, Integer num, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6, String str5, String str6, int i, Object obj) {
            if ((i & 1) != 0) {
                str = streamLink.host;
            }
            if ((i & 2) != 0) {
                str2 = streamLink.url;
            }
            if ((i & 4) != 0) {
                str3 = streamLink.quality;
            }
            if ((i & 8) != 0) {
                str4 = streamLink.size;
            }
            if ((i & 16) != 0) {
                num = streamLink.id;
            }
            if ((i & 32) != 0) {
                num2 = streamLink.movieId;
            }
            if ((i & 64) != 0) {
                num3 = streamLink.tvShowId;
            }
            if ((i & 128) != 0) {
                num4 = streamLink.seasonNumber;
            }
            if ((i & 256) != 0) {
                num5 = streamLink.episodeNumber;
            }
            if ((i & 512) != 0) {
                num6 = streamLink.order;
            }
            if ((i & 1024) != 0) {
                str5 = streamLink.createdAt;
            }
            if ((i & 2048) != 0) {
                str6 = streamLink.updatedAt;
            }
            String str7 = str5;
            String str8 = str6;
            Integer num7 = num5;
            Integer num8 = num6;
            Integer num9 = num3;
            Integer num10 = num4;
            Integer num11 = num;
            Integer num12 = num2;
            return streamLink.copy(str, str2, str3, str4, num11, num12, num9, num10, num7, num8, str7, str8);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getHost() {
            return this.host;
        }

        @Nullable
        /* renamed from: component10, reason: from getter */
        public final Integer getOrder() {
            return this.order;
        }

        @Nullable
        /* renamed from: component11, reason: from getter */
        public final String getCreatedAt() {
            return this.createdAt;
        }

        @Nullable
        /* renamed from: component12, reason: from getter */
        public final String getUpdatedAt() {
            return this.updatedAt;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getUrl() {
            return this.url;
        }

        @NotNull
        /* renamed from: component3, reason: from getter */
        public final String getQuality() {
            return this.quality;
        }

        @NotNull
        /* renamed from: component4, reason: from getter */
        public final String getSize() {
            return this.size;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final Integer getMovieId() {
            return this.movieId;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final Integer getTvShowId() {
            return this.tvShowId;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final Integer getSeasonNumber() {
            return this.seasonNumber;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final Integer getEpisodeNumber() {
            return this.episodeNumber;
        }

        @NotNull
        public final StreamLink copy(@JsonProperty("host") @NotNull String host, @JsonProperty("url") @NotNull String url, @JsonProperty("quality") @NotNull String quality, @JsonProperty("size") @NotNull String size, @JsonProperty("id") @Nullable Integer id, @JsonProperty("movie_id") @Nullable Integer movieId, @JsonProperty("tv_show_id") @Nullable Integer tvShowId, @JsonProperty("season_number") @Nullable Integer seasonNumber, @JsonProperty("episode_number") @Nullable Integer episodeNumber, @JsonProperty("order") @Nullable Integer order, @JsonProperty("created_at") @Nullable String createdAt, @JsonProperty("updated_at") @Nullable String updatedAt) {
            return new StreamLink(host, url, quality, size, id, movieId, tvShowId, seasonNumber, episodeNumber, order, createdAt, updatedAt);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof StreamLink)) {
                return false;
            }
            StreamLink streamLink = (StreamLink) other;
            return Intrinsics.areEqual(this.host, streamLink.host) && Intrinsics.areEqual(this.url, streamLink.url) && Intrinsics.areEqual(this.quality, streamLink.quality) && Intrinsics.areEqual(this.size, streamLink.size) && Intrinsics.areEqual(this.id, streamLink.id) && Intrinsics.areEqual(this.movieId, streamLink.movieId) && Intrinsics.areEqual(this.tvShowId, streamLink.tvShowId) && Intrinsics.areEqual(this.seasonNumber, streamLink.seasonNumber) && Intrinsics.areEqual(this.episodeNumber, streamLink.episodeNumber) && Intrinsics.areEqual(this.order, streamLink.order) && Intrinsics.areEqual(this.createdAt, streamLink.createdAt) && Intrinsics.areEqual(this.updatedAt, streamLink.updatedAt);
        }

        @Nullable
        public final String getCreatedAt() {
            return this.createdAt;
        }

        @Nullable
        public final Integer getEpisodeNumber() {
            return this.episodeNumber;
        }

        @NotNull
        public final String getHost() {
            return this.host;
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        public final Integer getMovieId() {
            return this.movieId;
        }

        @Nullable
        public final Integer getOrder() {
            return this.order;
        }

        @NotNull
        public final String getQuality() {
            return this.quality;
        }

        @Nullable
        public final Integer getSeasonNumber() {
            return this.seasonNumber;
        }

        @NotNull
        public final String getSize() {
            return this.size;
        }

        @Nullable
        public final Integer getTvShowId() {
            return this.tvShowId;
        }

        @Nullable
        public final String getUpdatedAt() {
            return this.updatedAt;
        }

        @NotNull
        public final String getUrl() {
            return this.url;
        }

        public int hashCode() {
            return (((((((((((((((((((((this.host.hashCode() * 31) + this.url.hashCode()) * 31) + this.quality.hashCode()) * 31) + this.size.hashCode()) * 31) + (this.id == null ? 0 : this.id.hashCode())) * 31) + (this.movieId == null ? 0 : this.movieId.hashCode())) * 31) + (this.tvShowId == null ? 0 : this.tvShowId.hashCode())) * 31) + (this.seasonNumber == null ? 0 : this.seasonNumber.hashCode())) * 31) + (this.episodeNumber == null ? 0 : this.episodeNumber.hashCode())) * 31) + (this.order == null ? 0 : this.order.hashCode())) * 31) + (this.createdAt == null ? 0 : this.createdAt.hashCode())) * 31) + (this.updatedAt != null ? this.updatedAt.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "StreamLink(host=" + this.host + ", url=" + this.url + ", quality=" + this.quality + ", size=" + this.size + ", id=" + this.id + ", movieId=" + this.movieId + ", tvShowId=" + this.tvShowId + ", seasonNumber=" + this.seasonNumber + ", episodeNumber=" + this.episodeNumber + ", order=" + this.order + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B\u001b\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0005HÆ\u0003J\u001d\u0010\u000e\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u0005HÆ\u0001J\u0014\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0012\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010\u0013\u001a\u00020\u0005HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b¨\u0006\u0014"}, d2 = {"Lcom/cncverse/DoFlixProvider$TMDBGenre;", "", "id", "", "name", "", "<init>", "(ILjava/lang/String;)V", "getId", "()I", "getName", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class TMDBGenre {

        @JsonProperty("id")
        private final int id;

        @JsonProperty("name")
        @NotNull
        private final String name;

        public TMDBGenre(@JsonProperty("id") int i, @JsonProperty("name") @NotNull String str) {
            this.id = i;
            this.name = str;
        }

        public static /* synthetic */ TMDBGenre copy$default(TMDBGenre tMDBGenre, int i, String str, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                i = tMDBGenre.id;
            }
            if ((i2 & 2) != 0) {
                str = tMDBGenre.name;
            }
            return tMDBGenre.copy(i, str);
        }

        /* renamed from: component1, reason: from getter */
        public final int getId() {
            return this.id;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @NotNull
        public final TMDBGenre copy(@JsonProperty("id") int id, @JsonProperty("name") @NotNull String name) {
            return new TMDBGenre(id, name);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TMDBGenre)) {
                return false;
            }
            TMDBGenre tMDBGenre = (TMDBGenre) other;
            return this.id == tMDBGenre.id && Intrinsics.areEqual(this.name, tMDBGenre.name);
        }

        public final int getId() {
            return this.id;
        }

        @NotNull
        public final String getName() {
            return this.name;
        }

        public int hashCode() {
            return (this.id * 31) + this.name.hashCode();
        }

        @NotNull
        public String toString() {
            return "TMDBGenre(id=" + this.id + ", name=" + this.name + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\bf\b\u0087\b\u0018\u00002\u00020\u0001BÓ\u0003\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0001\u0010\b\u001a\u00020\u0005\u0012\b\b\u0001\u0010\t\u001a\u00020\u0005\u0012\u000e\b\u0001\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b\u0012\u000e\b\u0001\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u000b\u0012\n\b\u0001\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\u0010\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0011\u001a\u00020\u0012\u0012\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0014\u001a\u0004\u0018\u00010\u0015\u0012\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\u0010\b\u0003\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000b\u0012\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0019\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u001a\u001a\u0004\u0018\u00010\u001b\u0012\n\b\u0003\u0010\u001c\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u001d\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u001b\u0012\n\b\u0003\u0010\u001f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010 \u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010!\u001a\u0004\u0018\u00010\"\u0012\n\b\u0003\u0010#\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010$\u001a\u0004\u0018\u00010\"\u0012\u0010\b\u0003\u0010%\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b\u0012\u0010\b\u0003\u0010&\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000b\u0012\u0010\b\u0003\u0010'\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000b\u0012\n\b\u0003\u0010(\u001a\u0004\u0018\u00010\u0001\u0012\u0010\b\u0003\u0010)\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b\u0012\n\b\u0003\u0010*\u001a\u0004\u0018\u00010\u0001\u0012\u0010\b\u0003\u0010+\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b\u0012\u0010\b\u0003\u0010,\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b\u0012\u0010\b\u0003\u0010-\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b¢\u0006\u0004\b.\u0010/J\t\u0010_\u001a\u00020\u0003HÆ\u0003J\t\u0010`\u001a\u00020\u0005HÆ\u0003J\t\u0010a\u001a\u00020\u0005HÆ\u0003J\t\u0010b\u001a\u00020\u0005HÆ\u0003J\t\u0010c\u001a\u00020\u0005HÆ\u0003J\t\u0010d\u001a\u00020\u0005HÆ\u0003J\u000f\u0010e\u001a\b\u0012\u0004\u0012\u00020\f0\u000bHÆ\u0003J\u000f\u0010f\u001a\b\u0012\u0004\u0012\u00020\u000e0\u000bHÆ\u0003J\u000b\u0010g\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\t\u0010h\u001a\u00020\u0003HÆ\u0003J\t\u0010i\u001a\u00020\u0012HÆ\u0003J\u0010\u0010j\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010@J\u000b\u0010k\u001a\u0004\u0018\u00010\u0015HÆ\u0003J\u000b\u0010l\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0011\u0010m\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000bHÆ\u0003J\u000b\u0010n\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010o\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010p\u001a\u0004\u0018\u00010\u001bHÆ\u0003¢\u0006\u0002\u0010IJ\u000b\u0010q\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010r\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010s\u001a\u0004\u0018\u00010\u001bHÆ\u0003¢\u0006\u0002\u0010IJ\u0010\u0010t\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010@J\u000b\u0010u\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010v\u001a\u0004\u0018\u00010\"HÆ\u0003¢\u0006\u0002\u0010QJ\u000b\u0010w\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010x\u001a\u0004\u0018\u00010\"HÆ\u0003¢\u0006\u0002\u0010QJ\u0011\u0010y\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000bHÆ\u0003J\u0011\u0010z\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000bHÆ\u0003J\u0011\u0010{\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000bHÆ\u0003J\u000b\u0010|\u001a\u0004\u0018\u00010\u0001HÆ\u0003J\u0011\u0010}\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000bHÆ\u0003J\u000b\u0010~\u001a\u0004\u0018\u00010\u0001HÆ\u0003J\u0011\u0010\u007f\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000bHÆ\u0003J\u0012\u0010\u0080\u0001\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000bHÆ\u0003J\u0012\u0010\u0081\u0001\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000bHÆ\u0003JÜ\u0003\u0010\u0082\u0001\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\b\b\u0003\u0010\u0006\u001a\u00020\u00052\b\b\u0003\u0010\u0007\u001a\u00020\u00052\b\b\u0003\u0010\b\u001a\u00020\u00052\b\b\u0003\u0010\t\u001a\u00020\u00052\u000e\b\u0003\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b2\u000e\b\u0003\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u000b2\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u00052\b\b\u0003\u0010\u0010\u001a\u00020\u00032\b\b\u0003\u0010\u0011\u001a\u00020\u00122\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0014\u001a\u0004\u0018\u00010\u00152\n\b\u0003\u0010\u0016\u001a\u0004\u0018\u00010\u00052\u0010\b\u0003\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000b2\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0019\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\n\b\u0003\u0010\u001c\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u001d\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u001b2\n\b\u0003\u0010\u001f\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010 \u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010!\u001a\u0004\u0018\u00010\"2\n\b\u0003\u0010#\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010$\u001a\u0004\u0018\u00010\"2\u0010\b\u0003\u0010%\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b2\u0010\b\u0003\u0010&\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000b2\u0010\b\u0003\u0010'\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000b2\n\b\u0003\u0010(\u001a\u0004\u0018\u00010\u00012\u0010\b\u0003\u0010)\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b2\n\b\u0003\u0010*\u001a\u0004\u0018\u00010\u00012\u0010\b\u0003\u0010+\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b2\u0010\b\u0003\u0010,\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b2\u0010\b\u0003\u0010-\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000bHÆ\u0001¢\u0006\u0003\u0010\u0083\u0001J\u0016\u0010\u0084\u0001\u001a\u00020\"2\t\u0010\u0085\u0001\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\u000b\u0010\u0086\u0001\u001a\u00020\u0003HÖ\u0081\u0004J\u000b\u0010\u0087\u0001\u001a\u00020\u0005HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0016\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b4\u00103R\u0016\u0010\u0007\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b5\u00103R\u0016\u0010\b\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b6\u00103R\u0016\u0010\t\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b7\u00103R\u001c\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b8\u00109R\u001c\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b:\u00109R\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b;\u00103R\u0016\u0010\u0010\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b<\u00101R\u0016\u0010\u0011\u001a\u00020\u00128\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b=\u0010>R\u001a\u0010\u0013\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010A\u001a\u0004\b?\u0010@R\u0018\u0010\u0014\u001a\u0004\u0018\u00010\u00158\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bB\u0010CR\u0018\u0010\u0016\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bD\u00103R\u001e\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bE\u00109R\u0018\u0010\u0018\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bF\u00103R\u0018\u0010\u0019\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bG\u00103R\u001a\u0010\u001a\u001a\u0004\u0018\u00010\u001b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010J\u001a\u0004\bH\u0010IR\u0018\u0010\u001c\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bK\u00103R\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bL\u00103R\u001a\u0010\u001e\u001a\u0004\u0018\u00010\u001b8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010J\u001a\u0004\bM\u0010IR\u001a\u0010\u001f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010A\u001a\u0004\bN\u0010@R\u0018\u0010 \u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bO\u00103R\u001a\u0010!\u001a\u0004\u0018\u00010\"8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010R\u001a\u0004\bP\u0010QR\u0018\u0010#\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bS\u00103R\u001a\u0010$\u001a\u0004\u0018\u00010\"8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010R\u001a\u0004\bT\u0010QR\u001e\u0010%\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bU\u00109R\u001e\u0010&\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bV\u00109R\u001e\u0010'\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bW\u00109R\u0018\u0010(\u001a\u0004\u0018\u00010\u00018\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bX\u0010YR\u001e\u0010)\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bZ\u00109R\u0018\u0010*\u001a\u0004\u0018\u00010\u00018\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b[\u0010YR\u001e\u0010+\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\\\u00109R\u001e\u0010,\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b]\u00109R\u001e\u0010-\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b^\u00109¨\u0006\u0088\u0001"}, d2 = {"Lcom/cncverse/DoFlixProvider$TvDetails;", "", "id", "", "name", "", "overview", "firstAirDate", "posterPath", "backdropPath", "genres", "", "Lcom/cncverse/DoFlixProvider$TMDBGenre;", "seasons", "Lcom/cncverse/DoFlixProvider$SeasonInfo;", "customPosterTag", "numberOfSeasons", "credits", "Lcom/cncverse/DoFlixProvider$Credits;", "numberOfEpisodes", "similar", "Lcom/cncverse/DoFlixProvider$SimilarResults;", "homepage", "originCountry", "originalLanguage", "originalName", "popularity", "", "status", "tagline", "voteAverage", "voteCount", "lastAirDate", "inProduction", "", "type", "adult", "createdBy", "episodeRunTime", "languages", "lastEpisodeToAir", "networks", "nextEpisodeToAir", "productionCompanies", "productionCountries", "spokenLanguages", "<init>", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/lang/String;ILcom/cncverse/DoFlixProvider$Credits;Ljava/lang/Integer;Lcom/cncverse/DoFlixProvider$SimilarResults;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Boolean;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/lang/Object;Ljava/util/List;Ljava/lang/Object;Ljava/util/List;Ljava/util/List;Ljava/util/List;)V", "getId", "()I", "getName", "()Ljava/lang/String;", "getOverview", "getFirstAirDate", "getPosterPath", "getBackdropPath", "getGenres", "()Ljava/util/List;", "getSeasons", "getCustomPosterTag", "getNumberOfSeasons", "getCredits", "()Lcom/cncverse/DoFlixProvider$Credits;", "getNumberOfEpisodes", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getSimilar", "()Lcom/cncverse/DoFlixProvider$SimilarResults;", "getHomepage", "getOriginCountry", "getOriginalLanguage", "getOriginalName", "getPopularity", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getStatus", "getTagline", "getVoteAverage", "getVoteCount", "getLastAirDate", "getInProduction", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getType", "getAdult", "getCreatedBy", "getEpisodeRunTime", "getLanguages", "getLastEpisodeToAir", "()Ljava/lang/Object;", "getNetworks", "getNextEpisodeToAir", "getProductionCompanies", "getProductionCountries", "getSpokenLanguages", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component30", "component31", "component32", "component33", "component34", "component35", "copy", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/lang/String;ILcom/cncverse/DoFlixProvider$Credits;Ljava/lang/Integer;Lcom/cncverse/DoFlixProvider$SimilarResults;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Boolean;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/lang/Object;Ljava/util/List;Ljava/lang/Object;Ljava/util/List;Ljava/util/List;Ljava/util/List;)Lcom/cncverse/DoFlixProvider$TvDetails;", "equals", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class TvDetails {

        @JsonProperty("adult")
        @Nullable
        private final Boolean adult;

        @JsonProperty("backdrop_path")
        @NotNull
        private final String backdropPath;

        @JsonProperty("created_by")
        @Nullable
        private final List<Object> createdBy;

        @JsonProperty("credits")
        @NotNull
        private final Credits credits;

        @JsonProperty("custom_poster_tag")
        @Nullable
        private final String customPosterTag;

        @JsonProperty("episode_run_time")
        @Nullable
        private final List<Integer> episodeRunTime;

        @JsonProperty("first_air_date")
        @NotNull
        private final String firstAirDate;

        @JsonProperty("genres")
        @NotNull
        private final List<TMDBGenre> genres;

        @JsonProperty("homepage")
        @Nullable
        private final String homepage;

        @JsonProperty("id")
        private final int id;

        @JsonProperty("in_production")
        @Nullable
        private final Boolean inProduction;

        @JsonProperty("languages")
        @Nullable
        private final List<String> languages;

        @JsonProperty("last_air_date")
        @Nullable
        private final String lastAirDate;

        @JsonProperty("last_episode_to_air")
        @Nullable
        private final Object lastEpisodeToAir;

        @JsonProperty("name")
        @NotNull
        private final String name;

        @JsonProperty("networks")
        @Nullable
        private final List<Object> networks;

        @JsonProperty("next_episode_to_air")
        @Nullable
        private final Object nextEpisodeToAir;

        @JsonProperty("number_of_episodes")
        @Nullable
        private final Integer numberOfEpisodes;

        @JsonProperty("number_of_seasons")
        private final int numberOfSeasons;

        @JsonProperty("origin_country")
        @Nullable
        private final List<String> originCountry;

        @JsonProperty("original_language")
        @Nullable
        private final String originalLanguage;

        @JsonProperty("original_name")
        @Nullable
        private final String originalName;

        @JsonProperty("overview")
        @NotNull
        private final String overview;

        @JsonProperty("popularity")
        @Nullable
        private final Double popularity;

        @JsonProperty("poster_path")
        @NotNull
        private final String posterPath;

        @JsonProperty("production_companies")
        @Nullable
        private final List<Object> productionCompanies;

        @JsonProperty("production_countries")
        @Nullable
        private final List<Object> productionCountries;

        @JsonProperty("seasons")
        @NotNull
        private final List<SeasonInfo> seasons;

        @JsonProperty("similar")
        @Nullable
        private final SimilarResults similar;

        @JsonProperty("spoken_languages")
        @Nullable
        private final List<Object> spokenLanguages;

        @JsonProperty("status")
        @Nullable
        private final String status;

        @JsonProperty("tagline")
        @Nullable
        private final String tagline;

        @JsonProperty("type")
        @Nullable
        private final String type;

        @JsonProperty("vote_average")
        @Nullable
        private final Double voteAverage;

        @JsonProperty("vote_count")
        @Nullable
        private final Integer voteCount;

        public TvDetails(@JsonProperty("id") int i, @JsonProperty("name") @NotNull String str, @JsonProperty("overview") @NotNull String str2, @JsonProperty("first_air_date") @NotNull String str3, @JsonProperty("poster_path") @NotNull String str4, @JsonProperty("backdrop_path") @NotNull String str5, @JsonProperty("genres") @NotNull List<TMDBGenre> list, @JsonProperty("seasons") @NotNull List<SeasonInfo> list2, @JsonProperty("custom_poster_tag") @Nullable String str6, @JsonProperty("number_of_seasons") int i2, @JsonProperty("credits") @NotNull Credits credits, @JsonProperty("number_of_episodes") @Nullable Integer num, @JsonProperty("similar") @Nullable SimilarResults similarResults, @JsonProperty("homepage") @Nullable String str7, @JsonProperty("origin_country") @Nullable List<String> list3, @JsonProperty("original_language") @Nullable String str8, @JsonProperty("original_name") @Nullable String str9, @JsonProperty("popularity") @Nullable Double d, @JsonProperty("status") @Nullable String str10, @JsonProperty("tagline") @Nullable String str11, @JsonProperty("vote_average") @Nullable Double d2, @JsonProperty("vote_count") @Nullable Integer num2, @JsonProperty("last_air_date") @Nullable String str12, @JsonProperty("in_production") @Nullable Boolean bool, @JsonProperty("type") @Nullable String str13, @JsonProperty("adult") @Nullable Boolean bool2, @JsonProperty("created_by") @Nullable List<? extends Object> list4, @JsonProperty("episode_run_time") @Nullable List<Integer> list5, @JsonProperty("languages") @Nullable List<String> list6, @JsonProperty("last_episode_to_air") @Nullable Object obj, @JsonProperty("networks") @Nullable List<? extends Object> list7, @JsonProperty("next_episode_to_air") @Nullable Object obj2, @JsonProperty("production_companies") @Nullable List<? extends Object> list8, @JsonProperty("production_countries") @Nullable List<? extends Object> list9, @JsonProperty("spoken_languages") @Nullable List<? extends Object> list10) {
            this.id = i;
            this.name = str;
            this.overview = str2;
            this.firstAirDate = str3;
            this.posterPath = str4;
            this.backdropPath = str5;
            this.genres = list;
            this.seasons = list2;
            this.customPosterTag = str6;
            this.numberOfSeasons = i2;
            this.credits = credits;
            this.numberOfEpisodes = num;
            this.similar = similarResults;
            this.homepage = str7;
            this.originCountry = list3;
            this.originalLanguage = str8;
            this.originalName = str9;
            this.popularity = d;
            this.status = str10;
            this.tagline = str11;
            this.voteAverage = d2;
            this.voteCount = num2;
            this.lastAirDate = str12;
            this.inProduction = bool;
            this.type = str13;
            this.adult = bool2;
            this.createdBy = list4;
            this.episodeRunTime = list5;
            this.languages = list6;
            this.lastEpisodeToAir = obj;
            this.networks = list7;
            this.nextEpisodeToAir = obj2;
            this.productionCompanies = list8;
            this.productionCountries = list9;
            this.spokenLanguages = list10;
        }

        public /* synthetic */ TvDetails(int i, String str, String str2, String str3, String str4, String str5, List list, List list2, String str6, int i2, Credits credits, Integer num, SimilarResults similarResults, String str7, List list3, String str8, String str9, Double d, String str10, String str11, Double d2, Integer num2, String str12, Boolean bool, String str13, Boolean bool2, List list4, List list5, List list6, Object obj, List list7, Object obj2, List list8, List list9, List list10, int i3, int i4, DefaultConstructorMarker defaultConstructorMarker) {
            this(i, str, str2, str3, str4, str5, list, list2, str6, i2, credits, (i3 & 2048) != 0 ? null : num, (i3 & 4096) != 0 ? null : similarResults, (i3 & 8192) != 0 ? null : str7, (i3 & 16384) != 0 ? null : list3, (32768 & i3) != 0 ? null : str8, (65536 & i3) != 0 ? null : str9, (131072 & i3) != 0 ? null : d, (262144 & i3) != 0 ? null : str10, (524288 & i3) != 0 ? null : str11, (1048576 & i3) != 0 ? null : d2, (2097152 & i3) != 0 ? null : num2, (4194304 & i3) != 0 ? null : str12, (8388608 & i3) != 0 ? null : bool, (16777216 & i3) != 0 ? null : str13, (33554432 & i3) != 0 ? null : bool2, (67108864 & i3) != 0 ? null : list4, (134217728 & i3) != 0 ? null : list5, (268435456 & i3) != 0 ? null : list6, (536870912 & i3) != 0 ? null : obj, (1073741824 & i3) != 0 ? null : list7, (i3 & Integer.MIN_VALUE) != 0 ? null : obj2, (i4 & 1) != 0 ? null : list8, (i4 & 2) != 0 ? null : list9, (i4 & 4) != 0 ? null : list10);
        }

        public static /* synthetic */ TvDetails copy$default(TvDetails tvDetails, int i, String str, String str2, String str3, String str4, String str5, List list, List list2, String str6, int i2, Credits credits, Integer num, SimilarResults similarResults, String str7, List list3, String str8, String str9, Double d, String str10, String str11, Double d2, Integer num2, String str12, Boolean bool, String str13, Boolean bool2, List list4, List list5, List list6, Object obj, List list7, Object obj2, List list8, List list9, List list10, int i3, int i4, Object obj3) {
            List list11;
            List list12;
            String str14;
            String str15;
            Double d3;
            Integer num3;
            String str16;
            Boolean bool3;
            String str17;
            Boolean bool4;
            List list13;
            List list14;
            List list15;
            Object obj4;
            List list16;
            Object obj5;
            List list17;
            List list18;
            String str18;
            String str19;
            String str20;
            List list19;
            List list20;
            String str21;
            int i5;
            Credits credits2;
            Integer num4;
            SimilarResults similarResults2;
            String str22;
            String str23;
            String str24;
            Double d4;
            String str25;
            String str26;
            int i6 = (i3 & 1) != 0 ? tvDetails.id : i;
            String str27 = (i3 & 2) != 0 ? tvDetails.name : str;
            String str28 = (i3 & 4) != 0 ? tvDetails.overview : str2;
            String str29 = (i3 & 8) != 0 ? tvDetails.firstAirDate : str3;
            String str30 = (i3 & 16) != 0 ? tvDetails.posterPath : str4;
            String str31 = (i3 & 32) != 0 ? tvDetails.backdropPath : str5;
            List list21 = (i3 & 64) != 0 ? tvDetails.genres : list;
            List list22 = (i3 & 128) != 0 ? tvDetails.seasons : list2;
            String str32 = (i3 & 256) != 0 ? tvDetails.customPosterTag : str6;
            int i7 = (i3 & 512) != 0 ? tvDetails.numberOfSeasons : i2;
            Credits credits3 = (i3 & 1024) != 0 ? tvDetails.credits : credits;
            Integer num5 = (i3 & 2048) != 0 ? tvDetails.numberOfEpisodes : num;
            SimilarResults similarResults3 = (i3 & 4096) != 0 ? tvDetails.similar : similarResults;
            String str33 = (i3 & 8192) != 0 ? tvDetails.homepage : str7;
            int i8 = i6;
            List list23 = (i3 & 16384) != 0 ? tvDetails.originCountry : list3;
            String str34 = (i3 & 32768) != 0 ? tvDetails.originalLanguage : str8;
            String str35 = (i3 & 65536) != 0 ? tvDetails.originalName : str9;
            Double d5 = (i3 & 131072) != 0 ? tvDetails.popularity : d;
            String str36 = (i3 & 262144) != 0 ? tvDetails.status : str10;
            String str37 = (i3 & 524288) != 0 ? tvDetails.tagline : str11;
            Double d6 = (i3 & 1048576) != 0 ? tvDetails.voteAverage : d2;
            Integer num6 = (i3 & 2097152) != 0 ? tvDetails.voteCount : num2;
            String str38 = (i3 & 4194304) != 0 ? tvDetails.lastAirDate : str12;
            Boolean bool5 = (i3 & 8388608) != 0 ? tvDetails.inProduction : bool;
            String str39 = (i3 & 16777216) != 0 ? tvDetails.type : str13;
            Boolean bool6 = (i3 & 33554432) != 0 ? tvDetails.adult : bool2;
            List list24 = (i3 & 67108864) != 0 ? tvDetails.createdBy : list4;
            List list25 = (i3 & 134217728) != 0 ? tvDetails.episodeRunTime : list5;
            List list26 = (i3 & 268435456) != 0 ? tvDetails.languages : list6;
            Object obj6 = (i3 & 536870912) != 0 ? tvDetails.lastEpisodeToAir : obj;
            List list27 = (i3 & 1073741824) != 0 ? tvDetails.networks : list7;
            Object obj7 = (i3 & Integer.MIN_VALUE) != 0 ? tvDetails.nextEpisodeToAir : obj2;
            List list28 = (i4 & 1) != 0 ? tvDetails.productionCompanies : list8;
            List list29 = (i4 & 2) != 0 ? tvDetails.productionCountries : list9;
            if ((i4 & 4) != 0) {
                list12 = list29;
                list11 = tvDetails.spokenLanguages;
                str15 = str37;
                d3 = d6;
                num3 = num6;
                str16 = str38;
                bool3 = bool5;
                str17 = str39;
                bool4 = bool6;
                list13 = list24;
                list14 = list25;
                list15 = list26;
                obj4 = obj6;
                list16 = list27;
                obj5 = obj7;
                list17 = list28;
                list18 = list23;
                str19 = str30;
                str20 = str31;
                list19 = list21;
                list20 = list22;
                str21 = str32;
                i5 = i7;
                credits2 = credits3;
                num4 = num5;
                similarResults2 = similarResults3;
                str22 = str33;
                str23 = str34;
                str24 = str35;
                d4 = d5;
                str14 = str36;
                str25 = str27;
                str26 = str28;
                str18 = str29;
            } else {
                list11 = list10;
                list12 = list29;
                str14 = str36;
                str15 = str37;
                d3 = d6;
                num3 = num6;
                str16 = str38;
                bool3 = bool5;
                str17 = str39;
                bool4 = bool6;
                list13 = list24;
                list14 = list25;
                list15 = list26;
                obj4 = obj6;
                list16 = list27;
                obj5 = obj7;
                list17 = list28;
                list18 = list23;
                str18 = str29;
                str19 = str30;
                str20 = str31;
                list19 = list21;
                list20 = list22;
                str21 = str32;
                i5 = i7;
                credits2 = credits3;
                num4 = num5;
                similarResults2 = similarResults3;
                str22 = str33;
                str23 = str34;
                str24 = str35;
                d4 = d5;
                str25 = str27;
                str26 = str28;
            }
            return tvDetails.copy(i8, str25, str26, str18, str19, str20, list19, list20, str21, i5, credits2, num4, similarResults2, str22, list18, str23, str24, d4, str14, str15, d3, num3, str16, bool3, str17, bool4, list13, list14, list15, obj4, list16, obj5, list17, list12, list11);
        }

        /* renamed from: component1, reason: from getter */
        public final int getId() {
            return this.id;
        }

        /* renamed from: component10, reason: from getter */
        public final int getNumberOfSeasons() {
            return this.numberOfSeasons;
        }

        @NotNull
        /* renamed from: component11, reason: from getter */
        public final Credits getCredits() {
            return this.credits;
        }

        @Nullable
        /* renamed from: component12, reason: from getter */
        public final Integer getNumberOfEpisodes() {
            return this.numberOfEpisodes;
        }

        @Nullable
        /* renamed from: component13, reason: from getter */
        public final SimilarResults getSimilar() {
            return this.similar;
        }

        @Nullable
        /* renamed from: component14, reason: from getter */
        public final String getHomepage() {
            return this.homepage;
        }

        @Nullable
        public final List<String> component15() {
            return this.originCountry;
        }

        @Nullable
        /* renamed from: component16, reason: from getter */
        public final String getOriginalLanguage() {
            return this.originalLanguage;
        }

        @Nullable
        /* renamed from: component17, reason: from getter */
        public final String getOriginalName() {
            return this.originalName;
        }

        @Nullable
        /* renamed from: component18, reason: from getter */
        public final Double getPopularity() {
            return this.popularity;
        }

        @Nullable
        /* renamed from: component19, reason: from getter */
        public final String getStatus() {
            return this.status;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @Nullable
        /* renamed from: component20, reason: from getter */
        public final String getTagline() {
            return this.tagline;
        }

        @Nullable
        /* renamed from: component21, reason: from getter */
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @Nullable
        /* renamed from: component22, reason: from getter */
        public final Integer getVoteCount() {
            return this.voteCount;
        }

        @Nullable
        /* renamed from: component23, reason: from getter */
        public final String getLastAirDate() {
            return this.lastAirDate;
        }

        @Nullable
        /* renamed from: component24, reason: from getter */
        public final Boolean getInProduction() {
            return this.inProduction;
        }

        @Nullable
        /* renamed from: component25, reason: from getter */
        public final String getType() {
            return this.type;
        }

        @Nullable
        /* renamed from: component26, reason: from getter */
        public final Boolean getAdult() {
            return this.adult;
        }

        @Nullable
        public final List<Object> component27() {
            return this.createdBy;
        }

        @Nullable
        public final List<Integer> component28() {
            return this.episodeRunTime;
        }

        @Nullable
        public final List<String> component29() {
            return this.languages;
        }

        @NotNull
        /* renamed from: component3, reason: from getter */
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        /* renamed from: component30, reason: from getter */
        public final Object getLastEpisodeToAir() {
            return this.lastEpisodeToAir;
        }

        @Nullable
        public final List<Object> component31() {
            return this.networks;
        }

        @Nullable
        /* renamed from: component32, reason: from getter */
        public final Object getNextEpisodeToAir() {
            return this.nextEpisodeToAir;
        }

        @Nullable
        public final List<Object> component33() {
            return this.productionCompanies;
        }

        @Nullable
        public final List<Object> component34() {
            return this.productionCountries;
        }

        @Nullable
        public final List<Object> component35() {
            return this.spokenLanguages;
        }

        @NotNull
        /* renamed from: component4, reason: from getter */
        public final String getFirstAirDate() {
            return this.firstAirDate;
        }

        @NotNull
        /* renamed from: component5, reason: from getter */
        public final String getPosterPath() {
            return this.posterPath;
        }

        @NotNull
        /* renamed from: component6, reason: from getter */
        public final String getBackdropPath() {
            return this.backdropPath;
        }

        @NotNull
        public final List<TMDBGenre> component7() {
            return this.genres;
        }

        @NotNull
        public final List<SeasonInfo> component8() {
            return this.seasons;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final String getCustomPosterTag() {
            return this.customPosterTag;
        }

        @NotNull
        public final TvDetails copy(@JsonProperty("id") int id, @JsonProperty("name") @NotNull String name, @JsonProperty("overview") @NotNull String overview, @JsonProperty("first_air_date") @NotNull String firstAirDate, @JsonProperty("poster_path") @NotNull String posterPath, @JsonProperty("backdrop_path") @NotNull String backdropPath, @JsonProperty("genres") @NotNull List<TMDBGenre> genres, @JsonProperty("seasons") @NotNull List<SeasonInfo> seasons, @JsonProperty("custom_poster_tag") @Nullable String customPosterTag, @JsonProperty("number_of_seasons") int numberOfSeasons, @JsonProperty("credits") @NotNull Credits credits, @JsonProperty("number_of_episodes") @Nullable Integer numberOfEpisodes, @JsonProperty("similar") @Nullable SimilarResults similar, @JsonProperty("homepage") @Nullable String homepage, @JsonProperty("origin_country") @Nullable List<String> originCountry, @JsonProperty("original_language") @Nullable String originalLanguage, @JsonProperty("original_name") @Nullable String originalName, @JsonProperty("popularity") @Nullable Double popularity, @JsonProperty("status") @Nullable String status, @JsonProperty("tagline") @Nullable String tagline, @JsonProperty("vote_average") @Nullable Double voteAverage, @JsonProperty("vote_count") @Nullable Integer voteCount, @JsonProperty("last_air_date") @Nullable String lastAirDate, @JsonProperty("in_production") @Nullable Boolean inProduction, @JsonProperty("type") @Nullable String type, @JsonProperty("adult") @Nullable Boolean adult, @JsonProperty("created_by") @Nullable List<? extends Object> createdBy, @JsonProperty("episode_run_time") @Nullable List<Integer> episodeRunTime, @JsonProperty("languages") @Nullable List<String> languages, @JsonProperty("last_episode_to_air") @Nullable Object lastEpisodeToAir, @JsonProperty("networks") @Nullable List<? extends Object> networks, @JsonProperty("next_episode_to_air") @Nullable Object nextEpisodeToAir, @JsonProperty("production_companies") @Nullable List<? extends Object> productionCompanies, @JsonProperty("production_countries") @Nullable List<? extends Object> productionCountries, @JsonProperty("spoken_languages") @Nullable List<? extends Object> spokenLanguages) {
            return new TvDetails(id, name, overview, firstAirDate, posterPath, backdropPath, genres, seasons, customPosterTag, numberOfSeasons, credits, numberOfEpisodes, similar, homepage, originCountry, originalLanguage, originalName, popularity, status, tagline, voteAverage, voteCount, lastAirDate, inProduction, type, adult, createdBy, episodeRunTime, languages, lastEpisodeToAir, networks, nextEpisodeToAir, productionCompanies, productionCountries, spokenLanguages);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TvDetails)) {
                return false;
            }
            TvDetails tvDetails = (TvDetails) other;
            return this.id == tvDetails.id && Intrinsics.areEqual(this.name, tvDetails.name) && Intrinsics.areEqual(this.overview, tvDetails.overview) && Intrinsics.areEqual(this.firstAirDate, tvDetails.firstAirDate) && Intrinsics.areEqual(this.posterPath, tvDetails.posterPath) && Intrinsics.areEqual(this.backdropPath, tvDetails.backdropPath) && Intrinsics.areEqual(this.genres, tvDetails.genres) && Intrinsics.areEqual(this.seasons, tvDetails.seasons) && Intrinsics.areEqual(this.customPosterTag, tvDetails.customPosterTag) && this.numberOfSeasons == tvDetails.numberOfSeasons && Intrinsics.areEqual(this.credits, tvDetails.credits) && Intrinsics.areEqual(this.numberOfEpisodes, tvDetails.numberOfEpisodes) && Intrinsics.areEqual(this.similar, tvDetails.similar) && Intrinsics.areEqual(this.homepage, tvDetails.homepage) && Intrinsics.areEqual(this.originCountry, tvDetails.originCountry) && Intrinsics.areEqual(this.originalLanguage, tvDetails.originalLanguage) && Intrinsics.areEqual(this.originalName, tvDetails.originalName) && Intrinsics.areEqual(this.popularity, tvDetails.popularity) && Intrinsics.areEqual(this.status, tvDetails.status) && Intrinsics.areEqual(this.tagline, tvDetails.tagline) && Intrinsics.areEqual(this.voteAverage, tvDetails.voteAverage) && Intrinsics.areEqual(this.voteCount, tvDetails.voteCount) && Intrinsics.areEqual(this.lastAirDate, tvDetails.lastAirDate) && Intrinsics.areEqual(this.inProduction, tvDetails.inProduction) && Intrinsics.areEqual(this.type, tvDetails.type) && Intrinsics.areEqual(this.adult, tvDetails.adult) && Intrinsics.areEqual(this.createdBy, tvDetails.createdBy) && Intrinsics.areEqual(this.episodeRunTime, tvDetails.episodeRunTime) && Intrinsics.areEqual(this.languages, tvDetails.languages) && Intrinsics.areEqual(this.lastEpisodeToAir, tvDetails.lastEpisodeToAir) && Intrinsics.areEqual(this.networks, tvDetails.networks) && Intrinsics.areEqual(this.nextEpisodeToAir, tvDetails.nextEpisodeToAir) && Intrinsics.areEqual(this.productionCompanies, tvDetails.productionCompanies) && Intrinsics.areEqual(this.productionCountries, tvDetails.productionCountries) && Intrinsics.areEqual(this.spokenLanguages, tvDetails.spokenLanguages);
        }

        @Nullable
        public final Boolean getAdult() {
            return this.adult;
        }

        @NotNull
        public final String getBackdropPath() {
            return this.backdropPath;
        }

        @Nullable
        public final List<Object> getCreatedBy() {
            return this.createdBy;
        }

        @NotNull
        public final Credits getCredits() {
            return this.credits;
        }

        @Nullable
        public final String getCustomPosterTag() {
            return this.customPosterTag;
        }

        @Nullable
        public final List<Integer> getEpisodeRunTime() {
            return this.episodeRunTime;
        }

        @NotNull
        public final String getFirstAirDate() {
            return this.firstAirDate;
        }

        @NotNull
        public final List<TMDBGenre> getGenres() {
            return this.genres;
        }

        @Nullable
        public final String getHomepage() {
            return this.homepage;
        }

        public final int getId() {
            return this.id;
        }

        @Nullable
        public final Boolean getInProduction() {
            return this.inProduction;
        }

        @Nullable
        public final List<String> getLanguages() {
            return this.languages;
        }

        @Nullable
        public final String getLastAirDate() {
            return this.lastAirDate;
        }

        @Nullable
        public final Object getLastEpisodeToAir() {
            return this.lastEpisodeToAir;
        }

        @NotNull
        public final String getName() {
            return this.name;
        }

        @Nullable
        public final List<Object> getNetworks() {
            return this.networks;
        }

        @Nullable
        public final Object getNextEpisodeToAir() {
            return this.nextEpisodeToAir;
        }

        @Nullable
        public final Integer getNumberOfEpisodes() {
            return this.numberOfEpisodes;
        }

        public final int getNumberOfSeasons() {
            return this.numberOfSeasons;
        }

        @Nullable
        public final List<String> getOriginCountry() {
            return this.originCountry;
        }

        @Nullable
        public final String getOriginalLanguage() {
            return this.originalLanguage;
        }

        @Nullable
        public final String getOriginalName() {
            return this.originalName;
        }

        @NotNull
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        public final Double getPopularity() {
            return this.popularity;
        }

        @NotNull
        public final String getPosterPath() {
            return this.posterPath;
        }

        @Nullable
        public final List<Object> getProductionCompanies() {
            return this.productionCompanies;
        }

        @Nullable
        public final List<Object> getProductionCountries() {
            return this.productionCountries;
        }

        @NotNull
        public final List<SeasonInfo> getSeasons() {
            return this.seasons;
        }

        @Nullable
        public final SimilarResults getSimilar() {
            return this.similar;
        }

        @Nullable
        public final List<Object> getSpokenLanguages() {
            return this.spokenLanguages;
        }

        @Nullable
        public final String getStatus() {
            return this.status;
        }

        @Nullable
        public final String getTagline() {
            return this.tagline;
        }

        @Nullable
        public final String getType() {
            return this.type;
        }

        @Nullable
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @Nullable
        public final Integer getVoteCount() {
            return this.voteCount;
        }

        public int hashCode() {
            return (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((this.id * 31) + this.name.hashCode()) * 31) + this.overview.hashCode()) * 31) + this.firstAirDate.hashCode()) * 31) + this.posterPath.hashCode()) * 31) + this.backdropPath.hashCode()) * 31) + this.genres.hashCode()) * 31) + this.seasons.hashCode()) * 31) + (this.customPosterTag == null ? 0 : this.customPosterTag.hashCode())) * 31) + this.numberOfSeasons) * 31) + this.credits.hashCode()) * 31) + (this.numberOfEpisodes == null ? 0 : this.numberOfEpisodes.hashCode())) * 31) + (this.similar == null ? 0 : this.similar.hashCode())) * 31) + (this.homepage == null ? 0 : this.homepage.hashCode())) * 31) + (this.originCountry == null ? 0 : this.originCountry.hashCode())) * 31) + (this.originalLanguage == null ? 0 : this.originalLanguage.hashCode())) * 31) + (this.originalName == null ? 0 : this.originalName.hashCode())) * 31) + (this.popularity == null ? 0 : this.popularity.hashCode())) * 31) + (this.status == null ? 0 : this.status.hashCode())) * 31) + (this.tagline == null ? 0 : this.tagline.hashCode())) * 31) + (this.voteAverage == null ? 0 : this.voteAverage.hashCode())) * 31) + (this.voteCount == null ? 0 : this.voteCount.hashCode())) * 31) + (this.lastAirDate == null ? 0 : this.lastAirDate.hashCode())) * 31) + (this.inProduction == null ? 0 : this.inProduction.hashCode())) * 31) + (this.type == null ? 0 : this.type.hashCode())) * 31) + (this.adult == null ? 0 : this.adult.hashCode())) * 31) + (this.createdBy == null ? 0 : this.createdBy.hashCode())) * 31) + (this.episodeRunTime == null ? 0 : this.episodeRunTime.hashCode())) * 31) + (this.languages == null ? 0 : this.languages.hashCode())) * 31) + (this.lastEpisodeToAir == null ? 0 : this.lastEpisodeToAir.hashCode())) * 31) + (this.networks == null ? 0 : this.networks.hashCode())) * 31) + (this.nextEpisodeToAir == null ? 0 : this.nextEpisodeToAir.hashCode())) * 31) + (this.productionCompanies == null ? 0 : this.productionCompanies.hashCode())) * 31) + (this.productionCountries == null ? 0 : this.productionCountries.hashCode())) * 31) + (this.spokenLanguages != null ? this.spokenLanguages.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "TvDetails(id=" + this.id + ", name=" + this.name + ", overview=" + this.overview + ", firstAirDate=" + this.firstAirDate + ", posterPath=" + this.posterPath + ", backdropPath=" + this.backdropPath + ", genres=" + this.genres + ", seasons=" + this.seasons + ", customPosterTag=" + this.customPosterTag + ", numberOfSeasons=" + this.numberOfSeasons + ", credits=" + this.credits + ", numberOfEpisodes=" + this.numberOfEpisodes + ", similar=" + this.similar + ", homepage=" + this.homepage + ", originCountry=" + this.originCountry + ", originalLanguage=" + this.originalLanguage + ", originalName=" + this.originalName + ", popularity=" + this.popularity + ", status=" + this.status + ", tagline=" + this.tagline + ", voteAverage=" + this.voteAverage + ", voteCount=" + this.voteCount + ", lastAirDate=" + this.lastAirDate + ", inProduction=" + this.inProduction + ", type=" + this.type + ", adult=" + this.adult + ", createdBy=" + this.createdBy + ", episodeRunTime=" + this.episodeRunTime + ", languages=" + this.languages + ", lastEpisodeToAir=" + this.lastEpisodeToAir + ", networks=" + this.networks + ", nextEpisodeToAir=" + this.nextEpisodeToAir + ", productionCompanies=" + this.productionCompanies + ", productionCountries=" + this.productionCountries + ", spokenLanguages=" + this.spokenLanguages + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b)\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B·\u0001\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0003\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0012\u0012\u0010\b\u0003\u0010\u0013\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0012¢\u0006\u0004\b\u0014\u0010\u0015J\t\u0010+\u001a\u00020\u0003HÆ\u0003J\t\u0010,\u001a\u00020\u0005HÆ\u0003J\u000b\u0010-\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010.\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010/\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u00100\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001eJ\u0010\u00101\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001eJ\u0010\u00102\u001a\u0004\u0018\u00010\fHÆ\u0003¢\u0006\u0002\u0010\"J\u0010\u00103\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001eJ\u000b\u00104\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u00105\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001eJ\u0010\u00106\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u001eJ\u0011\u00107\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0012HÆ\u0003J\u0011\u00108\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0012HÆ\u0003J¾\u0001\u00109\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u00032\u0010\b\u0003\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00122\u0010\b\u0003\u0010\u0013\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0012HÆ\u0001¢\u0006\u0002\u0010:J\u0014\u0010;\u001a\u00020<2\b\u0010=\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010>\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010?\u001a\u00020\u0005HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0018\u0010\b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0019R\u001a\u0010\t\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001f\u001a\u0004\b\u001d\u0010\u001eR\u001a\u0010\n\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001f\u001a\u0004\b \u0010\u001eR\u001a\u0010\u000b\u001a\u0004\u0018\u00010\f8\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010#\u001a\u0004\b!\u0010\"R\u001a\u0010\r\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001f\u001a\u0004\b$\u0010\u001eR\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0019R\u001a\u0010\u000f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001f\u001a\u0004\b&\u0010\u001eR\u001a\u0010\u0010\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u001f\u001a\u0004\b'\u0010\u001eR\u001e\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00128\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u001e\u0010\u0013\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00128\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b*\u0010)¨\u0006@"}, d2 = {"Lcom/cncverse/DoFlixProvider$TvEpisode;", "", "episodeNumber", "", "name", "", "overview", "stillPath", "airDate", "id", "runtime", "voteAverage", "", "voteCount", "productionCode", "seasonNumber", "showId", "crew", "", "guestStars", "<init>", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/util/List;Ljava/util/List;)V", "getEpisodeNumber", "()I", "getName", "()Ljava/lang/String;", "getOverview", "getStillPath", "getAirDate", "getId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getRuntime", "getVoteAverage", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getVoteCount", "getProductionCode", "getSeasonNumber", "getShowId", "getCrew", "()Ljava/util/List;", "getGuestStars", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "copy", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Double;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/util/List;Ljava/util/List;)Lcom/cncverse/DoFlixProvider$TvEpisode;", "equals", "", "other", "hashCode", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class TvEpisode {

        @JsonProperty("air_date")
        @Nullable
        private final String airDate;

        @JsonProperty("crew")
        @Nullable
        private final List<Object> crew;

        @JsonProperty("episode_number")
        private final int episodeNumber;

        @JsonProperty("guest_stars")
        @Nullable
        private final List<Object> guestStars;

        @JsonProperty("id")
        @Nullable
        private final Integer id;

        @JsonProperty("name")
        @NotNull
        private final String name;

        @JsonProperty("overview")
        @Nullable
        private final String overview;

        @JsonProperty("production_code")
        @Nullable
        private final String productionCode;

        @JsonProperty("runtime")
        @Nullable
        private final Integer runtime;

        @JsonProperty("season_number")
        @Nullable
        private final Integer seasonNumber;

        @JsonProperty("show_id")
        @Nullable
        private final Integer showId;

        @JsonProperty("still_path")
        @Nullable
        private final String stillPath;

        @JsonProperty("vote_average")
        @Nullable
        private final Double voteAverage;

        @JsonProperty("vote_count")
        @Nullable
        private final Integer voteCount;

        public TvEpisode(@JsonProperty("episode_number") int i, @JsonProperty("name") @NotNull String str, @JsonProperty("overview") @Nullable String str2, @JsonProperty("still_path") @Nullable String str3, @JsonProperty("air_date") @Nullable String str4, @JsonProperty("id") @Nullable Integer num, @JsonProperty("runtime") @Nullable Integer num2, @JsonProperty("vote_average") @Nullable Double d, @JsonProperty("vote_count") @Nullable Integer num3, @JsonProperty("production_code") @Nullable String str5, @JsonProperty("season_number") @Nullable Integer num4, @JsonProperty("show_id") @Nullable Integer num5, @JsonProperty("crew") @Nullable List<? extends Object> list, @JsonProperty("guest_stars") @Nullable List<? extends Object> list2) {
            this.episodeNumber = i;
            this.name = str;
            this.overview = str2;
            this.stillPath = str3;
            this.airDate = str4;
            this.id = num;
            this.runtime = num2;
            this.voteAverage = d;
            this.voteCount = num3;
            this.productionCode = str5;
            this.seasonNumber = num4;
            this.showId = num5;
            this.crew = list;
            this.guestStars = list2;
        }

        public /* synthetic */ TvEpisode(int i, String str, String str2, String str3, String str4, Integer num, Integer num2, Double d, Integer num3, String str5, Integer num4, Integer num5, List list, List list2, int i2, DefaultConstructorMarker defaultConstructorMarker) {
            this(i, str, str2, str3, str4, (i2 & 32) != 0 ? null : num, (i2 & 64) != 0 ? null : num2, (i2 & 128) != 0 ? null : d, (i2 & 256) != 0 ? null : num3, (i2 & 512) != 0 ? null : str5, (i2 & 1024) != 0 ? null : num4, (i2 & 2048) != 0 ? null : num5, (i2 & 4096) != 0 ? null : list, (i2 & 8192) != 0 ? null : list2);
        }

        /* renamed from: component1, reason: from getter */
        public final int getEpisodeNumber() {
            return this.episodeNumber;
        }

        @Nullable
        /* renamed from: component10, reason: from getter */
        public final String getProductionCode() {
            return this.productionCode;
        }

        @Nullable
        /* renamed from: component11, reason: from getter */
        public final Integer getSeasonNumber() {
            return this.seasonNumber;
        }

        @Nullable
        /* renamed from: component12, reason: from getter */
        public final Integer getShowId() {
            return this.showId;
        }

        @Nullable
        public final List<Object> component13() {
            return this.crew;
        }

        @Nullable
        public final List<Object> component14() {
            return this.guestStars;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getName() {
            return this.name;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final String getStillPath() {
            return this.stillPath;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getAirDate() {
            return this.airDate;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final Integer getId() {
            return this.id;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final Integer getRuntime() {
            return this.runtime;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final Integer getVoteCount() {
            return this.voteCount;
        }

        @NotNull
        public final TvEpisode copy(@JsonProperty("episode_number") int episodeNumber, @JsonProperty("name") @NotNull String name, @JsonProperty("overview") @Nullable String overview, @JsonProperty("still_path") @Nullable String stillPath, @JsonProperty("air_date") @Nullable String airDate, @JsonProperty("id") @Nullable Integer id, @JsonProperty("runtime") @Nullable Integer runtime, @JsonProperty("vote_average") @Nullable Double voteAverage, @JsonProperty("vote_count") @Nullable Integer voteCount, @JsonProperty("production_code") @Nullable String productionCode, @JsonProperty("season_number") @Nullable Integer seasonNumber, @JsonProperty("show_id") @Nullable Integer showId, @JsonProperty("crew") @Nullable List<? extends Object> crew, @JsonProperty("guest_stars") @Nullable List<? extends Object> guestStars) {
            return new TvEpisode(episodeNumber, name, overview, stillPath, airDate, id, runtime, voteAverage, voteCount, productionCode, seasonNumber, showId, crew, guestStars);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TvEpisode)) {
                return false;
            }
            TvEpisode tvEpisode = (TvEpisode) other;
            return this.episodeNumber == tvEpisode.episodeNumber && Intrinsics.areEqual(this.name, tvEpisode.name) && Intrinsics.areEqual(this.overview, tvEpisode.overview) && Intrinsics.areEqual(this.stillPath, tvEpisode.stillPath) && Intrinsics.areEqual(this.airDate, tvEpisode.airDate) && Intrinsics.areEqual(this.id, tvEpisode.id) && Intrinsics.areEqual(this.runtime, tvEpisode.runtime) && Intrinsics.areEqual(this.voteAverage, tvEpisode.voteAverage) && Intrinsics.areEqual(this.voteCount, tvEpisode.voteCount) && Intrinsics.areEqual(this.productionCode, tvEpisode.productionCode) && Intrinsics.areEqual(this.seasonNumber, tvEpisode.seasonNumber) && Intrinsics.areEqual(this.showId, tvEpisode.showId) && Intrinsics.areEqual(this.crew, tvEpisode.crew) && Intrinsics.areEqual(this.guestStars, tvEpisode.guestStars);
        }

        @Nullable
        public final String getAirDate() {
            return this.airDate;
        }

        @Nullable
        public final List<Object> getCrew() {
            return this.crew;
        }

        public final int getEpisodeNumber() {
            return this.episodeNumber;
        }

        @Nullable
        public final List<Object> getGuestStars() {
            return this.guestStars;
        }

        @Nullable
        public final Integer getId() {
            return this.id;
        }

        @NotNull
        public final String getName() {
            return this.name;
        }

        @Nullable
        public final String getOverview() {
            return this.overview;
        }

        @Nullable
        public final String getProductionCode() {
            return this.productionCode;
        }

        @Nullable
        public final Integer getRuntime() {
            return this.runtime;
        }

        @Nullable
        public final Integer getSeasonNumber() {
            return this.seasonNumber;
        }

        @Nullable
        public final Integer getShowId() {
            return this.showId;
        }

        @Nullable
        public final String getStillPath() {
            return this.stillPath;
        }

        @Nullable
        public final Double getVoteAverage() {
            return this.voteAverage;
        }

        @Nullable
        public final Integer getVoteCount() {
            return this.voteCount;
        }

        public int hashCode() {
            return (((((((((((((((((((((((((this.episodeNumber * 31) + this.name.hashCode()) * 31) + (this.overview == null ? 0 : this.overview.hashCode())) * 31) + (this.stillPath == null ? 0 : this.stillPath.hashCode())) * 31) + (this.airDate == null ? 0 : this.airDate.hashCode())) * 31) + (this.id == null ? 0 : this.id.hashCode())) * 31) + (this.runtime == null ? 0 : this.runtime.hashCode())) * 31) + (this.voteAverage == null ? 0 : this.voteAverage.hashCode())) * 31) + (this.voteCount == null ? 0 : this.voteCount.hashCode())) * 31) + (this.productionCode == null ? 0 : this.productionCode.hashCode())) * 31) + (this.seasonNumber == null ? 0 : this.seasonNumber.hashCode())) * 31) + (this.showId == null ? 0 : this.showId.hashCode())) * 31) + (this.crew == null ? 0 : this.crew.hashCode())) * 31) + (this.guestStars != null ? this.guestStars.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "TvEpisode(episodeNumber=" + this.episodeNumber + ", name=" + this.name + ", overview=" + this.overview + ", stillPath=" + this.stillPath + ", airDate=" + this.airDate + ", id=" + this.id + ", runtime=" + this.runtime + ", voteAverage=" + this.voteAverage + ", voteCount=" + this.voteCount + ", productionCode=" + this.productionCode + ", seasonNumber=" + this.seasonNumber + ", showId=" + this.showId + ", crew=" + this.crew + ", guestStars=" + this.guestStars + ")";
        }
    }

    /* compiled from: DoFlixProvider.kt */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b#\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001By\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0003\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0001\u0010\n\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0001\u0010\f\u001a\u00020\u0003\u0012\b\b\u0001\u0010\r\u001a\u00020\u0003¢\u0006\u0004\b\u000e\u0010\u000fJ\t\u0010\u001a\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001b\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001c\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001d\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001e\u001a\u00020\u0003HÆ\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0003HÆ\u0003J\t\u0010!\u001a\u00020\u0003HÆ\u0003J\t\u0010\"\u001a\u00020\u0003HÆ\u0003J\t\u0010#\u001a\u00020\u0003HÆ\u0003J\t\u0010$\u001a\u00020\u0003HÆ\u0003J{\u0010%\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00032\b\b\u0003\u0010\u0005\u001a\u00020\u00032\b\b\u0003\u0010\u0006\u001a\u00020\u00032\b\b\u0003\u0010\u0007\u001a\u00020\u00032\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00032\b\b\u0003\u0010\n\u001a\u00020\u00032\b\b\u0003\u0010\u000b\u001a\u00020\u00032\b\b\u0003\u0010\f\u001a\u00020\u00032\b\b\u0003\u0010\r\u001a\u00020\u0003HÆ\u0001J\u0014\u0010&\u001a\u00020'2\b\u0010(\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010)\u001a\u00020*HÖ\u0081\u0004J\n\u0010+\u001a\u00020\u0003HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0016\u0010\u0004\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0016\u0010\u0005\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u0016\u0010\u0006\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u0016\u0010\u0007\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011R\u0018\u0010\b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0011R\u0018\u0010\t\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0011R\u0016\u0010\n\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0011R\u0016\u0010\u000b\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0011R\u0016\u0010\f\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0011R\u0016\u0010\r\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0011¨\u0006,"}, d2 = {"Lcom/cncverse/DoFlixProvider$VideoItem;", "", "videosId", "", "title", "description", "slug", "release", "isTvseries", "isPaid", "runtime", "videoQuality", "thumbnailUrl", "posterUrl", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getVideosId", "()Ljava/lang/String;", "getTitle", "getDescription", "getSlug", "getRelease", "getRuntime", "getVideoQuality", "getThumbnailUrl", "getPosterUrl", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "copy", "equals", "", "other", "hashCode", "", "toString", "DoFlixProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class VideoItem {

        @JsonProperty("description")
        @NotNull
        private final String description;

        @JsonProperty("is_paid")
        @Nullable
        private final String isPaid;

        @JsonProperty("is_tvseries")
        @Nullable
        private final String isTvseries;

        @JsonProperty("poster_url")
        @NotNull
        private final String posterUrl;

        @JsonProperty("release")
        @NotNull
        private final String release;

        @JsonProperty("runtime")
        @NotNull
        private final String runtime;

        @JsonProperty("slug")
        @NotNull
        private final String slug;

        @JsonProperty("thumbnail_url")
        @NotNull
        private final String thumbnailUrl;

        @JsonProperty("title")
        @NotNull
        private final String title;

        @JsonProperty("video_quality")
        @NotNull
        private final String videoQuality;

        @JsonProperty("videos_id")
        @NotNull
        private final String videosId;

        public VideoItem(@JsonProperty("videos_id") @NotNull String str, @JsonProperty("title") @NotNull String str2, @JsonProperty("description") @NotNull String str3, @JsonProperty("slug") @NotNull String str4, @JsonProperty("release") @NotNull String str5, @JsonProperty("is_tvseries") @Nullable String str6, @JsonProperty("is_paid") @Nullable String str7, @JsonProperty("runtime") @NotNull String str8, @JsonProperty("video_quality") @NotNull String str9, @JsonProperty("thumbnail_url") @NotNull String str10, @JsonProperty("poster_url") @NotNull String str11) {
            this.videosId = str;
            this.title = str2;
            this.description = str3;
            this.slug = str4;
            this.release = str5;
            this.isTvseries = str6;
            this.isPaid = str7;
            this.runtime = str8;
            this.videoQuality = str9;
            this.thumbnailUrl = str10;
            this.posterUrl = str11;
        }

        public /* synthetic */ VideoItem(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(str, str2, str3, str4, str5, (i & 32) != 0 ? null : str6, (i & 64) != 0 ? "0" : str7, str8, str9, str10, str11);
        }

        public static /* synthetic */ VideoItem copy$default(VideoItem videoItem, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, int i, Object obj) {
            if ((i & 1) != 0) {
                str = videoItem.videosId;
            }
            if ((i & 2) != 0) {
                str2 = videoItem.title;
            }
            if ((i & 4) != 0) {
                str3 = videoItem.description;
            }
            if ((i & 8) != 0) {
                str4 = videoItem.slug;
            }
            if ((i & 16) != 0) {
                str5 = videoItem.release;
            }
            if ((i & 32) != 0) {
                str6 = videoItem.isTvseries;
            }
            if ((i & 64) != 0) {
                str7 = videoItem.isPaid;
            }
            if ((i & 128) != 0) {
                str8 = videoItem.runtime;
            }
            if ((i & 256) != 0) {
                str9 = videoItem.videoQuality;
            }
            if ((i & 512) != 0) {
                str10 = videoItem.thumbnailUrl;
            }
            if ((i & 1024) != 0) {
                str11 = videoItem.posterUrl;
            }
            String str12 = str10;
            String str13 = str11;
            String str14 = str8;
            String str15 = str9;
            String str16 = str6;
            String str17 = str7;
            String str18 = str5;
            String str19 = str3;
            return videoItem.copy(str, str2, str19, str4, str18, str16, str17, str14, str15, str12, str13);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getVideosId() {
            return this.videosId;
        }

        @NotNull
        /* renamed from: component10, reason: from getter */
        public final String getThumbnailUrl() {
            return this.thumbnailUrl;
        }

        @NotNull
        /* renamed from: component11, reason: from getter */
        public final String getPosterUrl() {
            return this.posterUrl;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        @NotNull
        /* renamed from: component3, reason: from getter */
        public final String getDescription() {
            return this.description;
        }

        @NotNull
        /* renamed from: component4, reason: from getter */
        public final String getSlug() {
            return this.slug;
        }

        @NotNull
        /* renamed from: component5, reason: from getter */
        public final String getRelease() {
            return this.release;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final String getIsTvseries() {
            return this.isTvseries;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final String getIsPaid() {
            return this.isPaid;
        }

        @NotNull
        /* renamed from: component8, reason: from getter */
        public final String getRuntime() {
            return this.runtime;
        }

        @NotNull
        /* renamed from: component9, reason: from getter */
        public final String getVideoQuality() {
            return this.videoQuality;
        }

        @NotNull
        public final VideoItem copy(@JsonProperty("videos_id") @NotNull String videosId, @JsonProperty("title") @NotNull String title, @JsonProperty("description") @NotNull String description, @JsonProperty("slug") @NotNull String slug, @JsonProperty("release") @NotNull String release, @JsonProperty("is_tvseries") @Nullable String isTvseries, @JsonProperty("is_paid") @Nullable String isPaid, @JsonProperty("runtime") @NotNull String runtime, @JsonProperty("video_quality") @NotNull String videoQuality, @JsonProperty("thumbnail_url") @NotNull String thumbnailUrl, @JsonProperty("poster_url") @NotNull String posterUrl) {
            return new VideoItem(videosId, title, description, slug, release, isTvseries, isPaid, runtime, videoQuality, thumbnailUrl, posterUrl);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof VideoItem)) {
                return false;
            }
            VideoItem videoItem = (VideoItem) other;
            return Intrinsics.areEqual(this.videosId, videoItem.videosId) && Intrinsics.areEqual(this.title, videoItem.title) && Intrinsics.areEqual(this.description, videoItem.description) && Intrinsics.areEqual(this.slug, videoItem.slug) && Intrinsics.areEqual(this.release, videoItem.release) && Intrinsics.areEqual(this.isTvseries, videoItem.isTvseries) && Intrinsics.areEqual(this.isPaid, videoItem.isPaid) && Intrinsics.areEqual(this.runtime, videoItem.runtime) && Intrinsics.areEqual(this.videoQuality, videoItem.videoQuality) && Intrinsics.areEqual(this.thumbnailUrl, videoItem.thumbnailUrl) && Intrinsics.areEqual(this.posterUrl, videoItem.posterUrl);
        }

        @NotNull
        public final String getDescription() {
            return this.description;
        }

        @NotNull
        public final String getPosterUrl() {
            return this.posterUrl;
        }

        @NotNull
        public final String getRelease() {
            return this.release;
        }

        @NotNull
        public final String getRuntime() {
            return this.runtime;
        }

        @NotNull
        public final String getSlug() {
            return this.slug;
        }

        @NotNull
        public final String getThumbnailUrl() {
            return this.thumbnailUrl;
        }

        @NotNull
        public final String getTitle() {
            return this.title;
        }

        @NotNull
        public final String getVideoQuality() {
            return this.videoQuality;
        }

        @NotNull
        public final String getVideosId() {
            return this.videosId;
        }

        public int hashCode() {
            return (((((((((((((((((((this.videosId.hashCode() * 31) + this.title.hashCode()) * 31) + this.description.hashCode()) * 31) + this.slug.hashCode()) * 31) + this.release.hashCode()) * 31) + (this.isTvseries == null ? 0 : this.isTvseries.hashCode())) * 31) + (this.isPaid != null ? this.isPaid.hashCode() : 0)) * 31) + this.runtime.hashCode()) * 31) + this.videoQuality.hashCode()) * 31) + this.thumbnailUrl.hashCode()) * 31) + this.posterUrl.hashCode();
        }

        @Nullable
        public final String isPaid() {
            return this.isPaid;
        }

        @Nullable
        public final String isTvseries() {
            return this.isTvseries;
        }

        @NotNull
        public String toString() {
            return "VideoItem(videosId=" + this.videosId + ", title=" + this.title + ", description=" + this.description + ", slug=" + this.slug + ", release=" + this.release + ", isTvseries=" + this.isTvseries + ", isPaid=" + this.isPaid + ", runtime=" + this.runtime + ", videoQuality=" + this.videoQuality + ", thumbnailUrl=" + this.thumbnailUrl + ", posterUrl=" + this.posterUrl + ")";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final OkHttpClient customClient_delegate$lambda$0(DoFlixProvider doFlixProvider) {
        return new OkHttpClient.Builder().addInterceptor(new HeaderReplacementInterceptor(doFlixProvider.headers)).build();
    }

    private final OkHttpClient getCustomClient() {
        return (OkHttpClient) this.customClient.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$0$0(MovieResult movieResult, MovieSearchResponse movieSearchResponse) {
        String substringBefore$default;
        movieSearchResponse.setPosterUrl(movieResult.getPosterPath());
        String releaseDate = movieResult.getReleaseDate();
        if (releaseDate == null) {
            releaseDate = movieResult.getFirstAirDate();
        }
        SearchQuality searchQuality = null;
        movieSearchResponse.setYear((releaseDate == null || (substringBefore$default = StringsKt.substringBefore$default(releaseDate, "-", (String) null, 2, (Object) null)) == null) ? null : StringsKt.toIntOrNull(substringBefore$default));
        Double voteAverage = movieResult.getVoteAverage();
        if ((voteAverage != null ? voteAverage.doubleValue() : 0.0d) >= 8.0d) {
            searchQuality = SearchQuality.HD;
        } else {
            Double voteAverage2 = movieResult.getVoteAverage();
            if ((voteAverage2 != null ? voteAverage2.doubleValue() : 0.0d) >= 7.0d) {
                searchQuality = SearchQuality.HD;
            }
        }
        movieSearchResponse.setQuality(searchQuality);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$1$0(MovieResult movieResult, MovieSearchResponse movieSearchResponse) {
        String substringBefore$default;
        movieSearchResponse.setPosterUrl(movieResult.getPosterPath());
        String releaseDate = movieResult.getReleaseDate();
        if (releaseDate == null) {
            releaseDate = movieResult.getFirstAirDate();
        }
        SearchQuality searchQuality = null;
        movieSearchResponse.setYear((releaseDate == null || (substringBefore$default = StringsKt.substringBefore$default(releaseDate, "-", (String) null, 2, (Object) null)) == null) ? null : StringsKt.toIntOrNull(substringBefore$default));
        Double voteAverage = movieResult.getVoteAverage();
        if ((voteAverage != null ? voteAverage.doubleValue() : 0.0d) >= 8.0d) {
            searchQuality = SearchQuality.HD;
        } else {
            Double voteAverage2 = movieResult.getVoteAverage();
            if ((voteAverage2 != null ? voteAverage2.doubleValue() : 0.0d) >= 7.0d) {
                searchQuality = SearchQuality.HD;
            }
        }
        movieSearchResponse.setQuality(searchQuality);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$4$0$0(MovieResult movieResult, MovieSearchResponse movieSearchResponse) {
        String substringBefore$default;
        movieSearchResponse.setPosterUrl(movieResult.getPosterPath());
        String releaseDate = movieResult.getReleaseDate();
        if (releaseDate == null) {
            releaseDate = movieResult.getFirstAirDate();
        }
        SearchQuality searchQuality = null;
        movieSearchResponse.setYear((releaseDate == null || (substringBefore$default = StringsKt.substringBefore$default(releaseDate, "-", (String) null, 2, (Object) null)) == null) ? null : StringsKt.toIntOrNull(substringBefore$default));
        Double voteAverage = movieResult.getVoteAverage();
        if ((voteAverage != null ? voteAverage.doubleValue() : 0.0d) >= 8.0d) {
            searchQuality = SearchQuality.HD;
        } else {
            Double voteAverage2 = movieResult.getVoteAverage();
            if ((voteAverage2 != null ? voteAverage2.doubleValue() : 0.0d) >= 7.0d) {
                searchQuality = SearchQuality.HD;
            }
        }
        movieSearchResponse.setQuality(searchQuality);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$5$0(SeriesResult seriesResult, TvSeriesSearchResponse tvSeriesSearchResponse) {
        tvSeriesSearchResponse.setPosterUrl(seriesResult.getPosterPath());
        SearchQuality searchQuality = null;
        tvSeriesSearchResponse.setYear(StringsKt.toIntOrNull(StringsKt.substringBefore$default(seriesResult.getFirstAirDate(), "-", (String) null, 2, (Object) null)));
        if (seriesResult.getVoteAverage() >= 8.0d) {
            searchQuality = SearchQuality.HD;
        } else if (seriesResult.getVoteAverage() >= 7.0d) {
            searchQuality = SearchQuality.HD;
        }
        tvSeriesSearchResponse.setQuality(searchQuality);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$6$0(SeriesResult seriesResult, TvSeriesSearchResponse tvSeriesSearchResponse) {
        tvSeriesSearchResponse.setPosterUrl(seriesResult.getPosterPath());
        SearchQuality searchQuality = null;
        tvSeriesSearchResponse.setYear(StringsKt.toIntOrNull(StringsKt.substringBefore$default(seriesResult.getFirstAirDate(), "-", (String) null, 2, (Object) null)));
        if (seriesResult.getVoteAverage() >= 8.0d) {
            searchQuality = SearchQuality.HD;
        } else if (seriesResult.getVoteAverage() >= 7.0d) {
            searchQuality = SearchQuality.HD;
        }
        tvSeriesSearchResponse.setQuality(searchQuality);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$9$0$0(SeriesResult seriesResult, TvSeriesSearchResponse tvSeriesSearchResponse) {
        tvSeriesSearchResponse.setPosterUrl(seriesResult.getPosterPath());
        SearchQuality searchQuality = null;
        tvSeriesSearchResponse.setYear(StringsKt.toIntOrNull(StringsKt.substringBefore$default(seriesResult.getFirstAirDate(), "-", (String) null, 2, (Object) null)));
        if (seriesResult.getVoteAverage() >= 8.0d) {
            searchQuality = SearchQuality.HD;
        } else if (seriesResult.getVoteAverage() >= 7.0d) {
            searchQuality = SearchQuality.HD;
        }
        tvSeriesSearchResponse.setQuality(searchQuality);
        return Unit.INSTANCE;
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:6:0x0016. Please report as an issue. */
    private final SearchQuality getQualityFromString(String qualityString) {
        String str;
        if (qualityString != null) {
            str = qualityString.toUpperCase(Locale.ROOT);
            Intrinsics.checkNotNullExpressionValue(str, "toUpperCase(...)");
        } else {
            str = null;
        }
        if (str == null) {
            return null;
        }
        switch (str.hashCode()) {
            case -577968313:
                if (!str.equals("TELECINE")) {
                    return null;
                }
                return SearchQuality.Telecine;
            case -577476283:
                if (!str.equals("TELESYNC")) {
                    return null;
                }
                return SearchQuality.Telesync;
            case -402211300:
                if (!str.equals("WORKPRINT")) {
                    return null;
                }
                return SearchQuality.WorkPrint;
            case 1687:
                if (!str.equals("4K")) {
                    return null;
                }
                return SearchQuality.HD;
            case 2300:
                if (!str.equals("HD")) {
                    return null;
                }
                return SearchQuality.HD;
            case 2671:
                if (!str.equals("TC")) {
                    return null;
                }
                return SearchQuality.Telecine;
            case 2687:
                if (!str.equals("TS")) {
                    return null;
                }
                return SearchQuality.Telesync;
            case 2777:
                if (!str.equals("WP")) {
                    return null;
                }
                return SearchQuality.WorkPrint;
            case 66479:
                if (!str.equals("CAM")) {
                    return null;
                }
                return SearchQuality.Cam;
            case 69570:
                if (!str.equals("FHD")) {
                    return null;
                }
                return SearchQuality.HD;
            case 1688123:
                if (!str.equals("720P")) {
                    return null;
                }
                return SearchQuality.HD;
            case 46737881:
                if (!str.equals("1080P")) {
                    return null;
                }
                return SearchQuality.HD;
            case 47689271:
                if (!str.equals("2160P")) {
                    return null;
                }
                return SearchQuality.HD;
            case 68585779:
                if (str.equals("HDCAM")) {
                    return SearchQuality.HdCam;
                }
                return null;
            case 1980557034:
                if (!str.equals("CAMRIP")) {
                    return null;
                }
                return SearchQuality.Cam;
            default:
                return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x002a, code lost:
    
        if (r0.equals("1080P") == false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:?, code lost:
    
        return com.lagradost.cloudstream3.utils.Qualities.P1080.getValue();
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0034, code lost:
    
        if (r0.equals("720P") == false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:?, code lost:
    
        return com.lagradost.cloudstream3.utils.Qualities.P720.getValue();
     */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x003d, code lost:
    
        if (r0.equals("480P") == false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:?, code lost:
    
        return com.lagradost.cloudstream3.utils.Qualities.P480.getValue();
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x0056, code lost:
    
        if (r0.equals("FHD") == false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x0066, code lost:
    
        if (r0.equals("SD") == false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x0076, code lost:
    
        if (r0.equals("HD") == false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x0086, code lost:
    
        if (r0.equals("4K") == false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:7:0x0020, code lost:
    
        if (r0.equals("2160P") == false) goto L41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:?, code lost:
    
        return com.lagradost.cloudstream3.utils.Qualities.P2160.getValue();
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:5:0x0015. Please report as an issue. */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final int getQualityValue(String qualityString) {
        String str;
        if (qualityString != null) {
            str = qualityString.toUpperCase(Locale.ROOT);
            Intrinsics.checkNotNullExpressionValue(str, "toUpperCase(...)");
        } else {
            str = null;
        }
        if (str != null) {
            switch (str.hashCode()) {
                case 1687:
                    break;
                case 2300:
                    break;
                case 2641:
                    break;
                case 69570:
                    break;
                case 1572803:
                    if (str.equals("360P")) {
                        return Qualities.P360.getValue();
                    }
                    break;
                case 1604516:
                    break;
                case 1688123:
                    break;
                case 46737881:
                    break;
                case 47689271:
                    break;
            }
        }
        return Qualities.Unknown.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Object getWithCustomHeaders(String str, Continuation<? super String> continuation) {
        Response response = (Closeable) getCustomClient().newCall(new Request.Builder().url(str).build()).execute();
        try {
            String string = response.body().string();
            CloseableKt.closeFinally(response, (Throwable) null);
            return string;
        } finally {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit load$lambda$1$0(TvEpisode tvEpisode, SeasonInfo seasonInfo, Episode episode) {
        episode.setName(tvEpisode.getName());
        episode.setSeason(Integer.valueOf(seasonInfo.getSeasonNumber()));
        episode.setEpisode(Integer.valueOf(tvEpisode.getEpisodeNumber()));
        episode.setPosterUrl(tvEpisode.getStillPath());
        episode.setDescription(tvEpisode.getOverview());
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit search$lambda$0$0(MovieResult movieResult, MovieSearchResponse movieSearchResponse) {
        String substringBefore$default;
        movieSearchResponse.setPosterUrl(movieResult.getPosterPath());
        String releaseDate = movieResult.getReleaseDate();
        Integer num = null;
        if (releaseDate != null && (substringBefore$default = StringsKt.substringBefore$default(releaseDate, "-", (String) null, 2, (Object) null)) != null) {
            num = StringsKt.toIntOrNull(substringBefore$default);
        }
        movieSearchResponse.setYear(num);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit search$lambda$1$0(MovieResult movieResult, TvSeriesSearchResponse tvSeriesSearchResponse) {
        String substringBefore$default;
        tvSeriesSearchResponse.setPosterUrl(movieResult.getPosterPath());
        String firstAirDate = movieResult.getFirstAirDate();
        Integer num = null;
        if (firstAirDate != null && (substringBefore$default = StringsKt.substringBefore$default(firstAirDate, "-", (String) null, 2, (Object) null)) != null) {
            num = StringsKt.toIntOrNull(substringBefore$default);
        }
        tvSeriesSearchResponse.setYear(num);
        return Unit.INSTANCE;
    }

    public boolean getHasMainPage() {
        return this.hasMainPage;
    }

    @NotNull
    public String getLang() {
        return this.lang;
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:13:0x007e. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:113:0x0515  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0030  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0081  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0089  */
    /* JADX WARN: Removed duplicated region for block: B:183:0x003b  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x0194  */
    /* JADX WARN: Removed duplicated region for block: B:65:0x029d  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0028  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getMainPage(int i, @NotNull MainPageRequest mainPageRequest, @NotNull Continuation<? super HomePageResponse> continuation) {
        DoFlixProvider$getMainPage$1 doFlixProvider$getMainPage$1;
        Object obj;
        MainPageRequest mainPageRequest2;
        int i2;
        String name;
        LinkedHashMap linkedHashMap;
        List<MovieResult> list;
        boolean z;
        boolean z2;
        Object obj2;
        boolean z3;
        Object obj3;
        DoFlixProvider doFlixProvider = this;
        if (continuation instanceof DoFlixProvider$getMainPage$1) {
            doFlixProvider$getMainPage$1 = (DoFlixProvider$getMainPage$1) continuation;
            if ((doFlixProvider$getMainPage$1.label & Integer.MIN_VALUE) != 0) {
                doFlixProvider$getMainPage$1.label -= Integer.MIN_VALUE;
                Object obj4 = doFlixProvider$getMainPage$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (doFlixProvider$getMainPage$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj4);
                        DonationManager.INSTANCE.checkAndShow(doFlixProvider.getName());
                        String data = mainPageRequest.getData();
                        doFlixProvider$getMainPage$1.L$0 = mainPageRequest;
                        doFlixProvider$getMainPage$1.I$0 = i;
                        doFlixProvider$getMainPage$1.label = 1;
                        Object withCustomHeaders = doFlixProvider.getWithCustomHeaders(data, doFlixProvider$getMainPage$1);
                        if (withCustomHeaders == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        obj = withCustomHeaders;
                        mainPageRequest2 = mainPageRequest;
                        i2 = i;
                        break;
                    case 1:
                        i2 = doFlixProvider$getMainPage$1.I$0;
                        mainPageRequest2 = (MainPageRequest) doFlixProvider$getMainPage$1.L$0;
                        ResultKt.throwOnFailure(obj4);
                        obj = obj4;
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                String str = (String) obj;
                ArrayList arrayList = new ArrayList();
                name = mainPageRequest2.getName();
                switch (name.hashCode()) {
                    case 139953384:
                        if (name.equals("TrendingMovies")) {
                            Object readValue = doFlixProvider.mapper.readValue(str, new DoFlixProvider$getMainPage$$inlined$readValue$2());
                            if (!(readValue instanceof HomePageApiResponse)) {
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(HomePageApiResponse.class).getQualifiedName() + "(non-null) but was " + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : null));
                            }
                            HomePageApiResponse homePageApiResponse = (HomePageApiResponse) readValue;
                            if (!homePageApiResponse.getResults().isEmpty()) {
                                List<MovieResult> results = homePageApiResponse.getResults();
                                ArrayList arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(results, 10));
                                for (MovieResult movieResult : results) {
                                    DoFlixProvider doFlixProvider2 = doFlixProvider;
                                    String title = movieResult.getTitle();
                                    if (title == null && (title = movieResult.getName()) == null) {
                                        title = "Unknown";
                                    }
                                    arrayList2.add(MainAPIKt.newMovieSearchResponse$default(doFlixProvider2, title, "movie," + movieResult.getId(), TvType.Movie, false, new DoFlixProvider$$ExternalSyntheticLambda3(movieResult), 8, (Object) null));
                                    homePageApiResponse = homePageApiResponse;
                                    results = results;
                                }
                                HomePageApiResponse homePageApiResponse2 = homePageApiResponse;
                                arrayList.add(new HomePageList("🔥 Trending Movies", arrayList2, false, 4, (DefaultConstructorMarker) null));
                                LinkedHashMap linkedHashMap2 = new LinkedHashMap();
                                List<MovieResult> results2 = homePageApiResponse2.getResults();
                                boolean z4 = false;
                                for (MovieResult movieResult2 : results2) {
                                    List<Integer> genreIds = movieResult2.getGenreIds();
                                    if (genreIds != null) {
                                        Iterator<T> it = genreIds.iterator();
                                        while (it.hasNext()) {
                                            LinkedHashMap linkedHashMap3 = linkedHashMap2;
                                            Integer boxInt = Boxing.boxInt(((Number) it.next()).intValue());
                                            List<MovieResult> list2 = results2;
                                            Object obj5 = linkedHashMap3.get(boxInt);
                                            if (obj5 == null) {
                                                obj2 = new ArrayList();
                                                z2 = z4;
                                                linkedHashMap3.put(boxInt, obj2);
                                            } else {
                                                z2 = z4;
                                                obj2 = obj5;
                                            }
                                            ((List) obj2).add(movieResult2);
                                            linkedHashMap2 = linkedHashMap3;
                                            results2 = list2;
                                            z4 = z2;
                                        }
                                        linkedHashMap = linkedHashMap2;
                                        list = results2;
                                        z = z4;
                                        Unit unit = Unit.INSTANCE;
                                    } else {
                                        linkedHashMap = linkedHashMap2;
                                        list = results2;
                                        z = z4;
                                    }
                                    linkedHashMap2 = linkedHashMap;
                                    results2 = list;
                                    z4 = z;
                                }
                                List take = CollectionsKt.take(CollectionsKt.sortedWith(linkedHashMap2.entrySet(), new DoFlixProvider$getMainPage$$inlined$sortedByDescending$1()), 5);
                                boolean z5 = false;
                                Iterator it2 = take.iterator();
                                while (it2.hasNext()) {
                                    Map.Entry entry = (Map.Entry) it2.next();
                                    int intValue = ((Number) entry.getKey()).intValue();
                                    List list3 = (List) entry.getValue();
                                    String str2 = doFlixProvider.movieGenres.get(Boxing.boxInt(intValue));
                                    if (str2 == null) {
                                        str2 = "Other";
                                    }
                                    String str3 = str2;
                                    List list4 = list3;
                                    List list5 = take;
                                    boolean z6 = z5;
                                    ArrayList arrayList3 = new ArrayList(CollectionsKt.collectionSizeOrDefault(list4, 10));
                                    List<MovieResult> list6 = list4;
                                    for (MovieResult movieResult3 : list6) {
                                        DoFlixProvider doFlixProvider3 = this;
                                        String title2 = movieResult3.getTitle();
                                        arrayList3.add(MainAPIKt.newMovieSearchResponse$default(doFlixProvider3, (title2 == null && (title2 = movieResult3.getName()) == null) ? "Unknown" : title2, "movie," + movieResult3.getId(), TvType.Movie, false, new DoFlixProvider$$ExternalSyntheticLambda4(movieResult3), 8, (Object) null));
                                        list6 = list6;
                                        it2 = it2;
                                    }
                                    arrayList.add(new HomePageList(str3, arrayList3, false, 4, (DefaultConstructorMarker) null));
                                    doFlixProvider = this;
                                    take = list5;
                                    z5 = z6;
                                }
                            }
                        }
                        return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                    case 302373916:
                        if (name.equals("TrendingSeries")) {
                            Object readValue2 = doFlixProvider.mapper.readValue(str, new DoFlixProvider$getMainPage$$inlined$readValue$4());
                            if (!(readValue2 instanceof SeriesPageApiResponse)) {
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(SeriesPageApiResponse.class).getQualifiedName() + "(non-null) but was " + (readValue2 != null ? Reflection.getOrCreateKotlinClass(readValue2.getClass()).getQualifiedName() : null));
                            }
                            SeriesPageApiResponse seriesPageApiResponse = (SeriesPageApiResponse) readValue2;
                            if (!seriesPageApiResponse.getResults().isEmpty()) {
                                List<SeriesResult> results3 = seriesPageApiResponse.getResults();
                                ArrayList arrayList4 = new ArrayList(CollectionsKt.collectionSizeOrDefault(results3, 10));
                                for (SeriesResult seriesResult : results3) {
                                    arrayList4.add(MainAPIKt.newTvSeriesSearchResponse$default(doFlixProvider, seriesResult.getName(), "tvseries," + seriesResult.getId(), TvType.TvSeries, false, new DoFlixProvider$$ExternalSyntheticLambda6(seriesResult), 8, (Object) null));
                                    seriesPageApiResponse = seriesPageApiResponse;
                                    results3 = results3;
                                }
                                SeriesPageApiResponse seriesPageApiResponse2 = seriesPageApiResponse;
                                arrayList.add(new HomePageList("🔥 Trending Series", arrayList4, false, 4, (DefaultConstructorMarker) null));
                                LinkedHashMap linkedHashMap4 = new LinkedHashMap();
                                List<SeriesResult> results4 = seriesPageApiResponse2.getResults();
                                boolean z7 = false;
                                for (SeriesResult seriesResult2 : results4) {
                                    Iterator<T> it3 = seriesResult2.getGenreIds().iterator();
                                    while (it3.hasNext()) {
                                        LinkedHashMap linkedHashMap5 = linkedHashMap4;
                                        Integer boxInt2 = Boxing.boxInt(((Number) it3.next()).intValue());
                                        List<SeriesResult> list7 = results4;
                                        Object obj6 = linkedHashMap5.get(boxInt2);
                                        if (obj6 == null) {
                                            obj3 = new ArrayList();
                                            z3 = z7;
                                            linkedHashMap5.put(boxInt2, obj3);
                                        } else {
                                            z3 = z7;
                                            obj3 = obj6;
                                        }
                                        ((List) obj3).add(seriesResult2);
                                        linkedHashMap4 = linkedHashMap5;
                                        results4 = list7;
                                        z7 = z3;
                                    }
                                }
                                List take2 = CollectionsKt.take(CollectionsKt.sortedWith(linkedHashMap4.entrySet(), new DoFlixProvider$getMainPage$$inlined$sortedByDescending$2()), 5);
                                boolean z8 = false;
                                Iterator it4 = take2.iterator();
                                while (it4.hasNext()) {
                                    Map.Entry entry2 = (Map.Entry) it4.next();
                                    int intValue2 = ((Number) entry2.getKey()).intValue();
                                    List list8 = (List) entry2.getValue();
                                    String str4 = doFlixProvider.seriesGenres.get(Boxing.boxInt(intValue2));
                                    if (str4 == null) {
                                        str4 = "Other";
                                    }
                                    String str5 = str4;
                                    List list9 = list8;
                                    List list10 = take2;
                                    boolean z9 = z8;
                                    Iterator it5 = it4;
                                    ArrayList arrayList5 = new ArrayList(CollectionsKt.collectionSizeOrDefault(list9, 10));
                                    List<SeriesResult> list11 = list9;
                                    boolean z10 = false;
                                    for (SeriesResult seriesResult3 : list11) {
                                        List list12 = list11;
                                        arrayList5.add(MainAPIKt.newTvSeriesSearchResponse$default(doFlixProvider, seriesResult3.getName(), "tvseries," + seriesResult3.getId(), TvType.TvSeries, false, new DoFlixProvider$$ExternalSyntheticLambda7(seriesResult3), 8, (Object) null));
                                        list11 = list12;
                                        z10 = z10;
                                        i2 = i2;
                                    }
                                    arrayList.add(new HomePageList(str5, arrayList5, false, 4, (DefaultConstructorMarker) null));
                                    take2 = list10;
                                    z8 = z9;
                                    it4 = it5;
                                }
                            }
                            return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                        }
                        return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                    case 1364508062:
                        if (name.equals("RecentMovies")) {
                            Object readValue3 = doFlixProvider.mapper.readValue(str, new DoFlixProvider$getMainPage$$inlined$readValue$1());
                            if (!(readValue3 instanceof HomePageApiResponse)) {
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(HomePageApiResponse.class).getQualifiedName() + "(non-null) but was " + (readValue3 != null ? Reflection.getOrCreateKotlinClass(readValue3.getClass()).getQualifiedName() : null));
                            }
                            HomePageApiResponse homePageApiResponse3 = (HomePageApiResponse) readValue3;
                            if (!homePageApiResponse3.getResults().isEmpty()) {
                                List<MovieResult> results5 = homePageApiResponse3.getResults();
                                ArrayList arrayList6 = new ArrayList(CollectionsKt.collectionSizeOrDefault(results5, 10));
                                for (MovieResult movieResult4 : results5) {
                                    DoFlixProvider doFlixProvider4 = doFlixProvider;
                                    String title3 = movieResult4.getTitle();
                                    if (title3 == null && (title3 = movieResult4.getName()) == null) {
                                        title3 = "Unknown";
                                    }
                                    arrayList6.add(MainAPIKt.newMovieSearchResponse$default(doFlixProvider4, title3, "movie," + movieResult4.getId(), TvType.Movie, false, new DoFlixProvider$$ExternalSyntheticLambda2(movieResult4), 8, (Object) null));
                                    homePageApiResponse3 = homePageApiResponse3;
                                    results5 = results5;
                                }
                                arrayList.add(new HomePageList("📅 Recently Added Movies", arrayList6, false, 4, (DefaultConstructorMarker) null));
                            }
                            return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                        }
                        return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                    case 1526928594:
                        if (name.equals("RecentSeries")) {
                            Object readValue4 = doFlixProvider.mapper.readValue(str, new DoFlixProvider$getMainPage$$inlined$readValue$3());
                            if (!(readValue4 instanceof SeriesPageApiResponse)) {
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(SeriesPageApiResponse.class).getQualifiedName() + "(non-null) but was " + (readValue4 != null ? Reflection.getOrCreateKotlinClass(readValue4.getClass()).getQualifiedName() : null));
                            }
                            SeriesPageApiResponse seriesPageApiResponse3 = (SeriesPageApiResponse) readValue4;
                            if (!seriesPageApiResponse3.getResults().isEmpty()) {
                                List<SeriesResult> results6 = seriesPageApiResponse3.getResults();
                                ArrayList arrayList7 = new ArrayList(CollectionsKt.collectionSizeOrDefault(results6, 10));
                                for (SeriesResult seriesResult4 : results6) {
                                    SeriesPageApiResponse seriesPageApiResponse4 = seriesPageApiResponse3;
                                    arrayList7.add(MainAPIKt.newTvSeriesSearchResponse$default(doFlixProvider, seriesResult4.getName(), "tvseries," + seriesResult4.getId(), TvType.TvSeries, false, new DoFlixProvider$$ExternalSyntheticLambda5(seriesResult4), 8, (Object) null));
                                    seriesPageApiResponse3 = seriesPageApiResponse4;
                                    doFlixProvider$getMainPage$1 = doFlixProvider$getMainPage$1;
                                    obj4 = obj4;
                                }
                                arrayList.add(new HomePageList("📅 Recently Added Series", arrayList7, false, 4, (DefaultConstructorMarker) null));
                            }
                        }
                        return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                    default:
                        return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                }
            }
        }
        doFlixProvider$getMainPage$1 = new DoFlixProvider$getMainPage$1(doFlixProvider, continuation);
        Object obj42 = doFlixProvider$getMainPage$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (doFlixProvider$getMainPage$1.label) {
        }
        String str6 = (String) obj;
        ArrayList arrayList8 = new ArrayList();
        name = mainPageRequest2.getName();
        switch (name.hashCode()) {
            case 139953384:
                break;
            case 302373916:
                break;
            case 1364508062:
                break;
            case 1526928594:
                break;
        }
    }

    @NotNull
    public List<MainPageData> getMainPage() {
        return this.mainPage;
    }

    @NotNull
    public String getMainUrl() {
        return this.mainUrl;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public Set<TvType> getSupportedTypes() {
        return this.supportedTypes;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(10:60|(1:18)|19|20|21|22|23|24|25|(5:84|85|(1:87)(1:91)|88|89)(7:27|28|(6:31|32|33|34|35|29)|82|83|41|(2:78|(1:80)(3:81|12|13))(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(16:43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|(1:59)(10:60|18|19|20|21|22|23|24|25|(5:84|85|(1:87)(1:91)|88|89)(7:27|28|(6:31|32|33|34|35|29)|82|83|41|(2:78|(1:80)(3:81|12|13))(0)))) */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x058b, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x0590, code lost:
    
        r5 = r17;
     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x0599, code lost:
    
        r18 = r13;
        r15 = r25;
        r20 = r28;
        r13 = r3;
        r25 = r14;
        r14 = r1;
        r0 = r35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:66:0x058d, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:67:0x058e, code lost:
    
        r3 = r34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:70:0x0595, code lost:
    
        r5 = r3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:71:0x05d6, code lost:
    
        r18 = r13;
        r15 = r25;
        r20 = r28;
        r13 = r34;
        r25 = r14;
        r14 = r1;
        r0 = r35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:74:0x05a8, code lost:
    
        r5 = r3;
        r18 = r13;
        r20 = r14;
        r13 = r34;
        r25 = r25;
        r14 = r1;
        r0 = r35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:95:0x0559, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:96:0x055a, code lost:
    
        r5 = r17;
     */
    /* JADX WARN: Code restructure failed: missing block: B:99:0x056a, code lost:
    
        r5 = r17;
        r27 = r10;
        r4 = r15;
        r15 = r21;
        r0 = r34;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0034. Please report as an issue. */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:106:0x00cd  */
    /* JADX WARN: Removed duplicated region for block: B:109:0x0302  */
    /* JADX WARN: Removed duplicated region for block: B:115:0x0346  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x003f  */
    /* JADX WARN: Removed duplicated region for block: B:129:0x00e8  */
    /* JADX WARN: Removed duplicated region for block: B:132:0x010b  */
    /* JADX WARN: Removed duplicated region for block: B:135:0x01f1  */
    /* JADX WARN: Removed duplicated region for block: B:141:0x0235  */
    /* JADX WARN: Removed duplicated region for block: B:145:0x0129  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0069  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x04c0  */
    /* JADX WARN: Removed duplicated region for block: B:43:0x03ae  */
    /* JADX WARN: Removed duplicated region for block: B:78:0x05e6  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x046b A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0037  */
    /* JADX WARN: Type inference failed for: r14v12 */
    /* JADX WARN: Type inference failed for: r14v14 */
    /* JADX WARN: Type inference failed for: r14v16 */
    /* JADX WARN: Type inference failed for: r14v17 */
    /* JADX WARN: Type inference failed for: r14v18 */
    /* JADX WARN: Type inference failed for: r14v19 */
    /* JADX WARN: Type inference failed for: r14v21, types: [java.util.List] */
    /* JADX WARN: Type inference failed for: r14v24, types: [java.util.List] */
    /* JADX WARN: Type inference failed for: r14v25 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:60:0x042f -> B:18:0x0443). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:73:0x05a8 -> B:40:0x05d6). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:76:0x05bf -> B:40:0x05d6). Please report as a decompilation issue!!! */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object load(@NotNull String str, @NotNull Continuation<? super LoadResponse> continuation) {
        DoFlixProvider$load$1 doFlixProvider$load$1;
        DoFlixProvider$load$1 doFlixProvider$load$12;
        List split$default;
        String str2;
        String str3;
        Object withCustomHeaders;
        String str4;
        Object withCustomHeaders2;
        String str5;
        String str6;
        String str7;
        String str8;
        Object readValue;
        Object newMovieLoadResponse;
        String str9;
        Object readValue2;
        ArrayList arrayList;
        DoFlixProvider doFlixProvider;
        Object obj;
        String str10;
        DoFlixProvider$load$1 doFlixProvider$load$13;
        String str11;
        String str12;
        Object obj2;
        TvDetails tvDetails;
        Iterator it;
        Object obj3;
        String str13;
        List list;
        String str14;
        Object obj4;
        String str15;
        String str16;
        Object obj5;
        String str17;
        Iterator it2;
        ?? r14;
        Continuation<? super LoadResponse> continuation2;
        String str18;
        TvDetails tvDetails2;
        List list2;
        SeasonInfo seasonInfo;
        String str19;
        Continuation<? super LoadResponse> continuation3;
        Object readValue3;
        DoFlixProvider doFlixProvider2 = this;
        String str20 = str;
        String str21 = "|";
        if (continuation instanceof DoFlixProvider$load$1) {
            doFlixProvider$load$1 = (DoFlixProvider$load$1) continuation;
            if ((doFlixProvider$load$1.label & Integer.MIN_VALUE) != 0) {
                doFlixProvider$load$1.label -= Integer.MIN_VALUE;
                doFlixProvider$load$12 = doFlixProvider$load$1;
                Object obj6 = doFlixProvider$load$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (doFlixProvider$load$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj6);
                        split$default = StringsKt.startsWith$default(str20, "https", false, 2, (Object) null) ? StringsKt.split$default(StringsKt.substringAfter$default(StringsKt.substringAfter$default(str20, "https://", (String) null, 2, (Object) null), "/", (String) null, 2, (Object) null), new String[]{","}, false, 0, 6, (Object) null) : StringsKt.split$default(str20, new String[]{","}, false, 0, 6, (Object) null);
                        String str22 = (String) split$default.get(0);
                        str2 = (String) split$default.get(1);
                        if (Intrinsics.areEqual(str22, "movie")) {
                            String str23 = doFlixProvider2.getMainUrl() + "/api/3/movie/" + str2 + "?api_key=" + doFlixProvider2.apiKey + "&append_to_response=credits%2Csimilar";
                            doFlixProvider$load$12.L$0 = str20;
                            doFlixProvider$load$12.L$1 = SpillingKt.nullOutSpilledVariable(split$default);
                            doFlixProvider$load$12.L$2 = SpillingKt.nullOutSpilledVariable(str22);
                            doFlixProvider$load$12.L$3 = str2;
                            doFlixProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(str23);
                            doFlixProvider$load$12.label = 1;
                            withCustomHeaders2 = doFlixProvider2.getWithCustomHeaders(str23, doFlixProvider$load$12);
                            if (withCustomHeaders2 == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            str5 = str20;
                            str6 = str22;
                            str7 = str23;
                            str8 = str2;
                            String str24 = (String) withCustomHeaders2;
                            readValue = doFlixProvider2.mapper.readValue(str24, new DoFlixProvider$load$$inlined$readValue$1());
                            String str25 = str6;
                            if (readValue instanceof MovieDetails) {
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(MovieDetails.class).getQualifiedName() + "(non-null) but was " + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : null));
                            }
                            MovieDetails movieDetails = (MovieDetails) readValue;
                            String title = movieDetails.getTitle();
                            TvType tvType = TvType.Movie;
                            DoFlixProvider$load$2 doFlixProvider$load$2 = new DoFlixProvider$load$2(movieDetails, doFlixProvider2, null);
                            doFlixProvider$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str5);
                            doFlixProvider$load$12.L$1 = SpillingKt.nullOutSpilledVariable(split$default);
                            doFlixProvider$load$12.L$2 = SpillingKt.nullOutSpilledVariable(str25);
                            doFlixProvider$load$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                            doFlixProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(str7);
                            doFlixProvider$load$12.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                            doFlixProvider$load$12.L$6 = SpillingKt.nullOutSpilledVariable(movieDetails);
                            doFlixProvider$load$12.label = 2;
                            newMovieLoadResponse = MainAPIKt.newMovieLoadResponse(doFlixProvider2, title, str5, tvType, str8, doFlixProvider$load$2, doFlixProvider$load$12);
                            return newMovieLoadResponse == coroutine_suspended ? coroutine_suspended : (LoadResponse) newMovieLoadResponse;
                        }
                        str3 = doFlixProvider2.getMainUrl() + "/api/3/tv/" + str2 + "?api_key=" + doFlixProvider2.apiKey + "&append_to_response=credits%2Csimilar";
                        doFlixProvider$load$12.L$0 = str20;
                        doFlixProvider$load$12.L$1 = SpillingKt.nullOutSpilledVariable(split$default);
                        doFlixProvider$load$12.L$2 = SpillingKt.nullOutSpilledVariable(str22);
                        doFlixProvider$load$12.L$3 = str2;
                        doFlixProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(str3);
                        doFlixProvider$load$12.label = 3;
                        withCustomHeaders = doFlixProvider2.getWithCustomHeaders(str3, doFlixProvider$load$12);
                        if (withCustomHeaders == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        str4 = str22;
                        str9 = (String) withCustomHeaders;
                        String str26 = str20;
                        readValue2 = doFlixProvider2.mapper.readValue(str9, new DoFlixProvider$load$$inlined$readValue$2());
                        if (readValue2 instanceof TvDetails) {
                            throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(TvDetails.class).getQualifiedName() + "(non-null) but was " + (readValue2 != null ? Reflection.getOrCreateKotlinClass(readValue2.getClass()).getQualifiedName() : null));
                        }
                        TvDetails tvDetails3 = (TvDetails) readValue2;
                        arrayList = new ArrayList();
                        List<SeasonInfo> seasons = tvDetails3.getSeasons();
                        ArrayList arrayList2 = new ArrayList();
                        for (Object obj7 : seasons) {
                            ArrayList arrayList3 = arrayList;
                            if (((SeasonInfo) obj7).getSeasonNumber() > 0) {
                                arrayList2.add(obj7);
                            }
                            arrayList = arrayList3;
                        }
                        doFlixProvider = this;
                        obj = coroutine_suspended;
                        str10 = str3;
                        doFlixProvider$load$13 = doFlixProvider$load$12;
                        str11 = str26;
                        str12 = str2;
                        obj2 = obj6;
                        tvDetails = tvDetails3;
                        it = arrayList2.iterator();
                        if (!it.hasNext()) {
                            obj4 = obj2;
                            SeasonInfo seasonInfo2 = (SeasonInfo) it.next();
                            try {
                            } catch (Exception e) {
                                str15 = str9;
                                str16 = str21;
                                obj5 = obj;
                                str17 = str4;
                                it2 = it;
                                doFlixProvider = doFlixProvider;
                                r14 = arrayList;
                                continuation2 = continuation;
                            }
                            String mainUrl = doFlixProvider.getMainUrl();
                            str15 = str9;
                            String str27 = str4;
                            DoFlixProvider doFlixProvider3 = doFlixProvider;
                            List list3 = split$default;
                            str18 = str21;
                            String str28 = mainUrl + "/api/3/tv/" + str12 + "/season/" + seasonInfo2.getSeasonNumber() + "?api_key=" + doFlixProvider3.apiKey + "&language=en";
                            doFlixProvider$load$13.L$0 = str11;
                            doFlixProvider$load$13.L$1 = SpillingKt.nullOutSpilledVariable(list3);
                            doFlixProvider$load$13.L$2 = SpillingKt.nullOutSpilledVariable(str27);
                            doFlixProvider$load$13.L$3 = str12;
                            doFlixProvider$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str10);
                            doFlixProvider$load$13.L$5 = SpillingKt.nullOutSpilledVariable(str15);
                            doFlixProvider$load$13.L$6 = tvDetails;
                            doFlixProvider$load$13.L$7 = arrayList;
                            Iterator it3 = it;
                            doFlixProvider$load$13.L$8 = it3;
                            doFlixProvider$load$13.L$9 = seasonInfo2;
                            doFlixProvider$load$13.L$10 = SpillingKt.nullOutSpilledVariable(str28);
                            doFlixProvider$load$13.label = 4;
                            Object withCustomHeaders3 = doFlixProvider3.getWithCustomHeaders(str28, doFlixProvider$load$13);
                            if (withCustomHeaders3 == obj) {
                                return obj;
                            }
                            r14 = arrayList;
                            doFlixProvider2 = doFlixProvider3;
                            tvDetails2 = tvDetails;
                            obj6 = withCustomHeaders3;
                            obj5 = obj;
                            list2 = list3;
                            str9 = str15;
                            str17 = str27;
                            it2 = it3;
                            seasonInfo = seasonInfo2;
                            str19 = str28;
                            continuation3 = continuation;
                            try {
                            } catch (Exception e2) {
                                doFlixProvider = doFlixProvider2;
                                str16 = str18;
                                str15 = str9;
                                tvDetails = tvDetails2;
                                split$default = list2;
                                continuation2 = continuation3;
                            }
                            String str29 = (String) obj6;
                            doFlixProvider = doFlixProvider2;
                            String str30 = str11;
                            readValue3 = doFlixProvider2.mapper.readValue(str29, new DoFlixProvider$load$$inlined$readValue$3());
                            if (readValue3 instanceof SeasonDetails) {
                                try {
                                } catch (Exception e3) {
                                    str15 = str9;
                                    tvDetails = tvDetails2;
                                    str16 = str18;
                                    split$default = list2;
                                    str11 = str30;
                                    continuation2 = continuation3;
                                    continuation = continuation2;
                                    str21 = str16;
                                    it = it2;
                                    arrayList = r14;
                                    obj = obj5;
                                    str4 = str17;
                                    obj2 = obj4;
                                    str9 = str15;
                                    if (!it.hasNext()) {
                                    }
                                }
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(SeasonDetails.class).getQualifiedName() + "(non-null) but was " + (readValue3 != null ? Reflection.getOrCreateKotlinClass(readValue3.getClass()).getQualifiedName() : null));
                            }
                            SeasonDetails seasonDetails = (SeasonDetails) readValue3;
                            List<TvEpisode> episodes = seasonDetails.getEpisodes();
                            boolean z = false;
                            for (TvEpisode tvEpisode : episodes) {
                                SeasonDetails seasonDetails2 = seasonDetails;
                                List<TvEpisode> list4 = episodes;
                                boolean z2 = z;
                                String str31 = str29;
                                String str32 = str19;
                                str16 = str18;
                                try {
                                } catch (Exception e4) {
                                    e = e4;
                                    str15 = str9;
                                    tvDetails = tvDetails2;
                                    split$default = list2;
                                    str11 = str30;
                                    continuation2 = continuation3;
                                    continuation = continuation2;
                                    str21 = str16;
                                    it = it2;
                                    arrayList = r14;
                                    obj = obj5;
                                    str4 = str17;
                                    obj2 = obj4;
                                    str9 = str15;
                                    if (!it.hasNext()) {
                                    }
                                }
                                r14.add(MainAPIKt.newEpisode(doFlixProvider, str12 + str16 + seasonInfo.getSeasonNumber() + str16 + tvEpisode.getEpisodeNumber(), new DoFlixProvider$$ExternalSyntheticLambda9(tvEpisode, seasonInfo)));
                                str18 = str16;
                                seasonDetails = seasonDetails2;
                                episodes = list4;
                                z = z2;
                                str29 = str31;
                                str19 = str32;
                            }
                            continuation = continuation3;
                            str21 = str18;
                            it = it2;
                            arrayList = r14;
                            tvDetails = tvDetails2;
                            obj = obj5;
                            str4 = str17;
                            split$default = list2;
                            str11 = str30;
                            obj2 = obj4;
                            if (!it.hasNext()) {
                                obj3 = obj2;
                                String str33 = str4;
                                DoFlixProvider doFlixProvider4 = doFlixProvider;
                                List list5 = split$default;
                                String name = tvDetails.getName();
                                TvType tvType2 = TvType.TvSeries;
                                DoFlixProvider$load$5 doFlixProvider$load$5 = new DoFlixProvider$load$5(tvDetails, doFlixProvider4, null);
                                doFlixProvider$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str11);
                                doFlixProvider$load$13.L$1 = SpillingKt.nullOutSpilledVariable(list5);
                                doFlixProvider$load$13.L$2 = SpillingKt.nullOutSpilledVariable(str33);
                                doFlixProvider$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str12);
                                doFlixProvider$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str10);
                                doFlixProvider$load$13.L$5 = SpillingKt.nullOutSpilledVariable(str9);
                                doFlixProvider$load$13.L$6 = SpillingKt.nullOutSpilledVariable(tvDetails);
                                doFlixProvider$load$13.L$7 = SpillingKt.nullOutSpilledVariable(arrayList);
                                doFlixProvider$load$13.L$8 = null;
                                doFlixProvider$load$13.L$9 = null;
                                doFlixProvider$load$13.L$10 = null;
                                doFlixProvider$load$13.label = 5;
                                Object newTvSeriesLoadResponse = MainAPIKt.newTvSeriesLoadResponse(doFlixProvider4, name, str11, tvType2, arrayList, doFlixProvider$load$5, doFlixProvider$load$13);
                                if (newTvSeriesLoadResponse == obj) {
                                    return obj;
                                }
                                obj6 = newTvSeriesLoadResponse;
                                str13 = str12;
                                list = list5;
                                str14 = str33;
                                return (LoadResponse) obj6;
                            }
                        }
                    case 1:
                        str7 = (String) doFlixProvider$load$12.L$4;
                        str8 = (String) doFlixProvider$load$12.L$3;
                        String str34 = (String) doFlixProvider$load$12.L$2;
                        List list6 = (List) doFlixProvider$load$12.L$1;
                        String str35 = (String) doFlixProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj6);
                        str5 = str35;
                        str6 = str34;
                        split$default = list6;
                        withCustomHeaders2 = obj6;
                        String str242 = (String) withCustomHeaders2;
                        readValue = doFlixProvider2.mapper.readValue(str242, new DoFlixProvider$load$$inlined$readValue$1());
                        String str252 = str6;
                        if (readValue instanceof MovieDetails) {
                        }
                        break;
                    case 2:
                        ResultKt.throwOnFailure(obj6);
                        newMovieLoadResponse = obj6;
                    case 3:
                        str3 = (String) doFlixProvider$load$12.L$4;
                        str2 = (String) doFlixProvider$load$12.L$3;
                        str4 = (String) doFlixProvider$load$12.L$2;
                        split$default = (List) doFlixProvider$load$12.L$1;
                        str20 = (String) doFlixProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj6);
                        withCustomHeaders = obj6;
                        str9 = (String) withCustomHeaders;
                        String str262 = str20;
                        readValue2 = doFlixProvider2.mapper.readValue(str9, new DoFlixProvider$load$$inlined$readValue$2());
                        if (readValue2 instanceof TvDetails) {
                        }
                        break;
                    case 4:
                        str19 = (String) doFlixProvider$load$12.L$10;
                        SeasonInfo seasonInfo3 = (SeasonInfo) doFlixProvider$load$12.L$9;
                        it2 = (Iterator) doFlixProvider$load$12.L$8;
                        r14 = (List) doFlixProvider$load$12.L$7;
                        tvDetails2 = (TvDetails) doFlixProvider$load$12.L$6;
                        String str36 = (String) doFlixProvider$load$12.L$5;
                        String str37 = (String) doFlixProvider$load$12.L$4;
                        String str38 = (String) doFlixProvider$load$12.L$3;
                        str17 = (String) doFlixProvider$load$12.L$2;
                        list2 = (List) doFlixProvider$load$12.L$1;
                        str11 = (String) doFlixProvider$load$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj6);
                            str18 = "|";
                            seasonInfo = seasonInfo3;
                            str9 = str36;
                            obj4 = obj6;
                            str10 = str37;
                            obj5 = coroutine_suspended;
                            doFlixProvider$load$13 = doFlixProvider$load$12;
                            str12 = str38;
                            continuation3 = continuation;
                            r14 = r14;
                        } catch (Exception e5) {
                            doFlixProvider = doFlixProvider2;
                            str16 = "|";
                            obj4 = obj6;
                            tvDetails = tvDetails2;
                            str15 = str36;
                            str10 = str37;
                            split$default = list2;
                            obj5 = coroutine_suspended;
                            continuation2 = continuation;
                            doFlixProvider$load$13 = doFlixProvider$load$12;
                            str12 = str38;
                            continuation = continuation2;
                            str21 = str16;
                            it = it2;
                            arrayList = r14;
                            obj = obj5;
                            str4 = str17;
                            obj2 = obj4;
                            str9 = str15;
                            if (!it.hasNext()) {
                            }
                        }
                        String str292 = (String) obj6;
                        doFlixProvider = doFlixProvider2;
                        String str302 = str11;
                        readValue3 = doFlixProvider2.mapper.readValue(str292, new DoFlixProvider$load$$inlined$readValue$3());
                        if (readValue3 instanceof SeasonDetails) {
                        }
                        break;
                    case 5:
                        str13 = (String) doFlixProvider$load$12.L$3;
                        str14 = (String) doFlixProvider$load$12.L$2;
                        list = (List) doFlixProvider$load$12.L$1;
                        ResultKt.throwOnFailure(obj6);
                        obj3 = obj6;
                        return (LoadResponse) obj6;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        doFlixProvider$load$1 = new DoFlixProvider$load$1(doFlixProvider2, continuation);
        doFlixProvider$load$12 = doFlixProvider$load$1;
        Object obj62 = doFlixProvider$load$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (doFlixProvider$load$12.label) {
        }
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002f. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x003a  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0476  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0552  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x009e  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x040d  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x0451  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x00ca  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x0260  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0329  */
    /* JADX WARN: Removed duplicated region for block: B:41:0x012e  */
    /* JADX WARN: Removed duplicated region for block: B:44:0x01f8  */
    /* JADX WARN: Removed duplicated region for block: B:50:0x023c  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x0150  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0032  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:18:0x051b -> B:12:0x0534). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:38:0x02f9 -> B:32:0x030f). Please report as a decompilation issue!!! */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object loadLinks(@NotNull String str, boolean z, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Boolean> continuation) {
        DoFlixProvider$loadLinks$1 doFlixProvider$loadLinks$1;
        List split$default;
        String str2;
        Object obj;
        String str3;
        Function1<? super SubtitleFile, Unit> function13;
        boolean z2;
        String str4;
        String str5;
        String str6;
        Object withCustomHeaders;
        String str7;
        String str8;
        Function1<? super SubtitleFile, Unit> function14;
        List list;
        String str9;
        String str10;
        Object readValue;
        Function1<? super ExtractorLink, Unit> function15;
        DoFlixProvider doFlixProvider;
        Object obj2;
        boolean z3;
        Iterator it;
        int i;
        MovieLinksResponse movieLinksResponse;
        Continuation<? super Boolean> continuation2;
        String str11;
        String str12;
        DoFlixProvider doFlixProvider2;
        String str13;
        Function1<? super ExtractorLink, Unit> function16;
        Function1<? super SubtitleFile, Unit> function17;
        String str14;
        List list2;
        Iterator it2;
        int i2;
        Function1<? super ExtractorLink, Unit> function18;
        Object obj3;
        Object obj4;
        String str15;
        Object readValue2;
        EpisodeLinksResponse episodeLinksResponse;
        DoFlixProvider doFlixProvider3;
        Continuation<? super Boolean> continuation3;
        Object obj5;
        Function1<? super ExtractorLink, Unit> function19;
        boolean z4;
        String str16;
        Iterator it3;
        int i3;
        Object obj6;
        Object obj7;
        String str17;
        String str18;
        String str19;
        Function1<? super ExtractorLink, Unit> function110;
        String str20;
        String str21;
        Function1<? super SubtitleFile, Unit> function111;
        EpisodeLinksResponse episodeLinksResponse2;
        Iterator it4;
        List list3;
        DoFlixProvider doFlixProvider4 = this;
        boolean z5 = z;
        Function1<? super ExtractorLink, Unit> function112 = function12;
        Continuation<? super Boolean> continuation4 = continuation;
        if (continuation4 instanceof DoFlixProvider$loadLinks$1) {
            doFlixProvider$loadLinks$1 = (DoFlixProvider$loadLinks$1) continuation4;
            if ((doFlixProvider$loadLinks$1.label & Integer.MIN_VALUE) != 0) {
                doFlixProvider$loadLinks$1.label -= Integer.MIN_VALUE;
                Object obj8 = doFlixProvider$loadLinks$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (doFlixProvider$loadLinks$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj8);
                        split$default = StringsKt.split$default(str, new String[]{"|"}, false, 0, 6, (Object) null);
                        if (split$default.size() == 1) {
                            String str22 = (String) split$default.get(0);
                            String str23 = doFlixProvider4.getMainUrl() + "/api/3/movie/" + str22 + "/links?api_key=" + doFlixProvider4.apiKey;
                            doFlixProvider$loadLinks$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                            doFlixProvider$loadLinks$1.L$1 = SpillingKt.nullOutSpilledVariable(function1);
                            doFlixProvider$loadLinks$1.L$2 = function112;
                            doFlixProvider$loadLinks$1.L$3 = SpillingKt.nullOutSpilledVariable(split$default);
                            doFlixProvider$loadLinks$1.L$4 = SpillingKt.nullOutSpilledVariable(str22);
                            doFlixProvider$loadLinks$1.L$5 = SpillingKt.nullOutSpilledVariable(str23);
                            doFlixProvider$loadLinks$1.Z$0 = z5;
                            doFlixProvider$loadLinks$1.label = 1;
                            withCustomHeaders = doFlixProvider4.getWithCustomHeaders(str23, doFlixProvider$loadLinks$1);
                            if (withCustomHeaders == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            str7 = str22;
                            str8 = str23;
                            function14 = function1;
                            list = split$default;
                            str9 = str;
                            str10 = (String) withCustomHeaders;
                            boolean z6 = z5;
                            Function1<? super ExtractorLink, Unit> function113 = function112;
                            readValue = doFlixProvider4.mapper.readValue(str10, new DoFlixProvider$loadLinks$$inlined$readValue$1());
                            if (readValue instanceof MovieLinksResponse) {
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(MovieLinksResponse.class).getQualifiedName() + "(non-null) but was " + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : null));
                            }
                            MovieLinksResponse movieLinksResponse2 = (MovieLinksResponse) readValue;
                            List<StreamLink> links = movieLinksResponse2.getLinks();
                            Iterator it5 = links.iterator();
                            function15 = function113;
                            doFlixProvider = doFlixProvider4;
                            obj2 = links;
                            z3 = z6;
                            it = it5;
                            i = 0;
                            movieLinksResponse = movieLinksResponse2;
                            continuation2 = continuation;
                            if (it.hasNext()) {
                                Object next = it.next();
                                Continuation<? super Boolean> continuation5 = continuation2;
                                StreamLink streamLink = (StreamLink) next;
                                Object obj9 = obj8;
                                String name = doFlixProvider.getName();
                                String str24 = str8;
                                str11 = str7;
                                str12 = str10;
                                String str25 = streamLink.getHost() + " - " + streamLink.getQuality();
                                String url = streamLink.getUrl();
                                ExtractorLinkType extractorLinkType = ExtractorLinkType.M3U8;
                                doFlixProvider2 = doFlixProvider;
                                DoFlixProvider$loadLinks$2$1 doFlixProvider$loadLinks$2$1 = new DoFlixProvider$loadLinks$2$1(doFlixProvider2, streamLink, null);
                                doFlixProvider$loadLinks$1.L$0 = SpillingKt.nullOutSpilledVariable(str9);
                                doFlixProvider$loadLinks$1.L$1 = SpillingKt.nullOutSpilledVariable(function14);
                                doFlixProvider$loadLinks$1.L$2 = function15;
                                doFlixProvider$loadLinks$1.L$3 = SpillingKt.nullOutSpilledVariable(list);
                                doFlixProvider$loadLinks$1.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                                doFlixProvider$loadLinks$1.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                doFlixProvider$loadLinks$1.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                doFlixProvider$loadLinks$1.L$7 = movieLinksResponse;
                                doFlixProvider$loadLinks$1.L$8 = SpillingKt.nullOutSpilledVariable(obj2);
                                Iterator it6 = it;
                                doFlixProvider$loadLinks$1.L$9 = it6;
                                doFlixProvider$loadLinks$1.L$10 = SpillingKt.nullOutSpilledVariable(next);
                                doFlixProvider$loadLinks$1.L$11 = SpillingKt.nullOutSpilledVariable(streamLink);
                                doFlixProvider$loadLinks$1.L$12 = function15;
                                doFlixProvider$loadLinks$1.Z$0 = z3;
                                doFlixProvider$loadLinks$1.I$0 = i;
                                doFlixProvider$loadLinks$1.I$1 = 0;
                                doFlixProvider$loadLinks$1.label = 2;
                                DoFlixProvider$loadLinks$1 doFlixProvider$loadLinks$12 = doFlixProvider$loadLinks$1;
                                Object newExtractorLink = ExtractorApiKt.newExtractorLink(name, str25, url, extractorLinkType, doFlixProvider$loadLinks$2$1, doFlixProvider$loadLinks$12);
                                if (newExtractorLink == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                str13 = str24;
                                function16 = function15;
                                function17 = function14;
                                str14 = str9;
                                list2 = list;
                                it2 = it6;
                                i2 = i;
                                function18 = function16;
                                continuation2 = continuation5;
                                obj3 = coroutine_suspended;
                                obj4 = obj9;
                                obj8 = newExtractorLink;
                                doFlixProvider$loadLinks$1 = doFlixProvider$loadLinks$12;
                                function16.invoke(obj8);
                                obj8 = obj4;
                                coroutine_suspended = obj3;
                                i = i2;
                                doFlixProvider = doFlixProvider2;
                                function15 = function18;
                                it = it2;
                                function14 = function17;
                                str8 = str13;
                                str9 = str14;
                                list = list2;
                                str7 = str11;
                                str10 = str12;
                                if (it.hasNext()) {
                                    return Boxing.boxBoolean(!movieLinksResponse.getLinks().isEmpty());
                                }
                            }
                        } else {
                            if (split$default.size() != 3) {
                                return Boxing.boxBoolean(false);
                            }
                            String substringAfterLast$default = StringsKt.substringAfterLast$default((String) split$default.get(0), "/", (String) null, 2, (Object) null);
                            str2 = (String) split$default.get(1);
                            String str26 = (String) split$default.get(2);
                            obj = obj8;
                            str3 = doFlixProvider4.getMainUrl() + "/api/3/tv/" + substringAfterLast$default + "/season/" + str2 + "/episode/" + str26 + "/links?api_key=" + doFlixProvider4.apiKey;
                            doFlixProvider$loadLinks$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                            doFlixProvider$loadLinks$1.L$1 = SpillingKt.nullOutSpilledVariable(function1);
                            doFlixProvider$loadLinks$1.L$2 = function112;
                            doFlixProvider$loadLinks$1.L$3 = SpillingKt.nullOutSpilledVariable(split$default);
                            doFlixProvider$loadLinks$1.L$4 = SpillingKt.nullOutSpilledVariable(substringAfterLast$default);
                            doFlixProvider$loadLinks$1.L$5 = SpillingKt.nullOutSpilledVariable(str2);
                            doFlixProvider$loadLinks$1.L$6 = SpillingKt.nullOutSpilledVariable(str26);
                            doFlixProvider$loadLinks$1.L$7 = SpillingKt.nullOutSpilledVariable(str3);
                            doFlixProvider$loadLinks$1.Z$0 = z5;
                            doFlixProvider$loadLinks$1.label = 3;
                            obj8 = doFlixProvider4.getWithCustomHeaders(str3, doFlixProvider$loadLinks$1);
                            if (obj8 == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            function13 = function1;
                            z2 = z5;
                            str4 = str26;
                            str5 = str;
                            str6 = substringAfterLast$default;
                            str15 = (String) obj8;
                            String str27 = str5;
                            readValue2 = doFlixProvider4.mapper.readValue(str15, new DoFlixProvider$loadLinks$$inlined$readValue$2());
                            if (readValue2 instanceof EpisodeLinksResponse) {
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(EpisodeLinksResponse.class).getQualifiedName() + "(non-null) but was " + (readValue2 != null ? Reflection.getOrCreateKotlinClass(readValue2.getClass()).getQualifiedName() : null));
                            }
                            episodeLinksResponse = (EpisodeLinksResponse) readValue2;
                            List<StreamLink> results = episodeLinksResponse.getResults();
                            Iterator it7 = results.iterator();
                            doFlixProvider3 = this;
                            continuation3 = continuation;
                            obj5 = results;
                            function19 = function112;
                            z4 = z2;
                            str16 = str27;
                            it3 = it7;
                            i3 = 0;
                            obj6 = obj;
                            if (it3.hasNext()) {
                                Object next2 = it3.next();
                                String str28 = str16;
                                StreamLink streamLink2 = (StreamLink) next2;
                                String str29 = str15;
                                String name2 = doFlixProvider3.getName();
                                obj7 = obj6;
                                str17 = str3;
                                str18 = str4;
                                String str30 = streamLink2.getHost() + " - " + streamLink2.getQuality();
                                String url2 = streamLink2.getUrl();
                                ExtractorLinkType extractorLinkType2 = ExtractorLinkType.M3U8;
                                DoFlixProvider doFlixProvider5 = doFlixProvider3;
                                DoFlixProvider$loadLinks$3$1 doFlixProvider$loadLinks$3$1 = new DoFlixProvider$loadLinks$3$1(doFlixProvider5, streamLink2, null);
                                doFlixProvider$loadLinks$1.L$0 = SpillingKt.nullOutSpilledVariable(str28);
                                doFlixProvider$loadLinks$1.L$1 = SpillingKt.nullOutSpilledVariable(function13);
                                doFlixProvider$loadLinks$1.L$2 = function19;
                                doFlixProvider$loadLinks$1.L$3 = SpillingKt.nullOutSpilledVariable(split$default);
                                doFlixProvider$loadLinks$1.L$4 = SpillingKt.nullOutSpilledVariable(str6);
                                doFlixProvider$loadLinks$1.L$5 = SpillingKt.nullOutSpilledVariable(str2);
                                doFlixProvider$loadLinks$1.L$6 = SpillingKt.nullOutSpilledVariable(str18);
                                doFlixProvider$loadLinks$1.L$7 = SpillingKt.nullOutSpilledVariable(str17);
                                doFlixProvider$loadLinks$1.L$8 = SpillingKt.nullOutSpilledVariable(str29);
                                doFlixProvider$loadLinks$1.L$9 = episodeLinksResponse;
                                doFlixProvider$loadLinks$1.L$10 = SpillingKt.nullOutSpilledVariable(obj5);
                                Iterator it8 = it3;
                                doFlixProvider$loadLinks$1.L$11 = it8;
                                doFlixProvider$loadLinks$1.L$12 = SpillingKt.nullOutSpilledVariable(next2);
                                doFlixProvider$loadLinks$1.L$13 = SpillingKt.nullOutSpilledVariable(streamLink2);
                                doFlixProvider$loadLinks$1.L$14 = function19;
                                doFlixProvider$loadLinks$1.Z$0 = z4;
                                doFlixProvider$loadLinks$1.I$0 = i3;
                                doFlixProvider$loadLinks$1.I$1 = 0;
                                doFlixProvider$loadLinks$1.label = 4;
                                DoFlixProvider$loadLinks$1 doFlixProvider$loadLinks$13 = doFlixProvider$loadLinks$1;
                                Object newExtractorLink2 = ExtractorApiKt.newExtractorLink(name2, str30, url2, extractorLinkType2, doFlixProvider$loadLinks$3$1, doFlixProvider$loadLinks$13);
                                if (newExtractorLink2 == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                str19 = str29;
                                function110 = function19;
                                str20 = str2;
                                str21 = str6;
                                function111 = function13;
                                episodeLinksResponse2 = episodeLinksResponse;
                                it4 = it8;
                                doFlixProvider4 = doFlixProvider5;
                                str16 = str28;
                                obj8 = newExtractorLink2;
                                doFlixProvider$loadLinks$1 = doFlixProvider$loadLinks$13;
                                continuation4 = continuation3;
                                list3 = split$default;
                                function19.invoke(obj8);
                                doFlixProvider3 = doFlixProvider4;
                                continuation3 = continuation4;
                                function19 = function110;
                                episodeLinksResponse = episodeLinksResponse2;
                                it3 = it4;
                                str15 = str19;
                                function13 = function111;
                                str2 = str20;
                                str6 = str21;
                                split$default = list3;
                                obj6 = obj7;
                                str3 = str17;
                                str4 = str18;
                                if (it3.hasNext()) {
                                    return Boxing.boxBoolean(!episodeLinksResponse.getResults().isEmpty());
                                }
                            }
                        }
                    case 1:
                        z5 = doFlixProvider$loadLinks$1.Z$0;
                        str8 = (String) doFlixProvider$loadLinks$1.L$5;
                        str7 = (String) doFlixProvider$loadLinks$1.L$4;
                        List list4 = (List) doFlixProvider$loadLinks$1.L$3;
                        function112 = (Function1) doFlixProvider$loadLinks$1.L$2;
                        function14 = (Function1) doFlixProvider$loadLinks$1.L$1;
                        str9 = (String) doFlixProvider$loadLinks$1.L$0;
                        ResultKt.throwOnFailure(obj8);
                        list = list4;
                        withCustomHeaders = obj8;
                        str10 = (String) withCustomHeaders;
                        boolean z62 = z5;
                        Function1<? super ExtractorLink, Unit> function1132 = function112;
                        readValue = doFlixProvider4.mapper.readValue(str10, new DoFlixProvider$loadLinks$$inlined$readValue$1());
                        if (readValue instanceof MovieLinksResponse) {
                        }
                        break;
                    case 2:
                        int i4 = doFlixProvider$loadLinks$1.I$1;
                        int i5 = doFlixProvider$loadLinks$1.I$0;
                        boolean z7 = doFlixProvider$loadLinks$1.Z$0;
                        Function1<? super ExtractorLink, Unit> function114 = (Function1) doFlixProvider$loadLinks$1.L$12;
                        Object obj10 = doFlixProvider$loadLinks$1.L$10;
                        it2 = (Iterator) doFlixProvider$loadLinks$1.L$9;
                        Object obj11 = (Iterable) doFlixProvider$loadLinks$1.L$8;
                        MovieLinksResponse movieLinksResponse3 = (MovieLinksResponse) doFlixProvider$loadLinks$1.L$7;
                        String str31 = (String) doFlixProvider$loadLinks$1.L$6;
                        str13 = (String) doFlixProvider$loadLinks$1.L$5;
                        String str32 = (String) doFlixProvider$loadLinks$1.L$4;
                        list2 = (List) doFlixProvider$loadLinks$1.L$3;
                        Function1<? super ExtractorLink, Unit> function115 = (Function1) doFlixProvider$loadLinks$1.L$2;
                        Function1<? super SubtitleFile, Unit> function116 = (Function1) doFlixProvider$loadLinks$1.L$1;
                        String str33 = (String) doFlixProvider$loadLinks$1.L$0;
                        ResultKt.throwOnFailure(obj8);
                        function17 = function116;
                        z3 = z7;
                        str12 = str31;
                        obj2 = obj11;
                        str11 = str32;
                        function18 = function115;
                        str14 = str33;
                        continuation2 = continuation4;
                        movieLinksResponse = movieLinksResponse3;
                        doFlixProvider2 = doFlixProvider4;
                        function16 = function114;
                        i2 = i5;
                        obj3 = coroutine_suspended;
                        obj4 = obj8;
                        function16.invoke(obj8);
                        obj8 = obj4;
                        coroutine_suspended = obj3;
                        i = i2;
                        doFlixProvider = doFlixProvider2;
                        function15 = function18;
                        it = it2;
                        function14 = function17;
                        str8 = str13;
                        str9 = str14;
                        list = list2;
                        str7 = str11;
                        str10 = str12;
                        if (it.hasNext()) {
                        }
                        break;
                    case 3:
                        boolean z8 = doFlixProvider$loadLinks$1.Z$0;
                        str3 = (String) doFlixProvider$loadLinks$1.L$7;
                        str4 = (String) doFlixProvider$loadLinks$1.L$6;
                        str2 = (String) doFlixProvider$loadLinks$1.L$5;
                        str6 = (String) doFlixProvider$loadLinks$1.L$4;
                        split$default = (List) doFlixProvider$loadLinks$1.L$3;
                        function112 = (Function1) doFlixProvider$loadLinks$1.L$2;
                        function13 = (Function1) doFlixProvider$loadLinks$1.L$1;
                        z2 = z8;
                        str5 = (String) doFlixProvider$loadLinks$1.L$0;
                        ResultKt.throwOnFailure(obj8);
                        obj = obj8;
                        str15 = (String) obj8;
                        String str272 = str5;
                        readValue2 = doFlixProvider4.mapper.readValue(str15, new DoFlixProvider$loadLinks$$inlined$readValue$2());
                        if (readValue2 instanceof EpisodeLinksResponse) {
                        }
                        break;
                    case 4:
                        int i6 = doFlixProvider$loadLinks$1.I$1;
                        i3 = doFlixProvider$loadLinks$1.I$0;
                        boolean z9 = doFlixProvider$loadLinks$1.Z$0;
                        function19 = (Function1) doFlixProvider$loadLinks$1.L$14;
                        Object obj12 = doFlixProvider$loadLinks$1.L$12;
                        it4 = (Iterator) doFlixProvider$loadLinks$1.L$11;
                        Object obj13 = (Iterable) doFlixProvider$loadLinks$1.L$10;
                        episodeLinksResponse2 = (EpisodeLinksResponse) doFlixProvider$loadLinks$1.L$9;
                        str19 = (String) doFlixProvider$loadLinks$1.L$8;
                        String str34 = (String) doFlixProvider$loadLinks$1.L$7;
                        String str35 = (String) doFlixProvider$loadLinks$1.L$6;
                        str20 = (String) doFlixProvider$loadLinks$1.L$5;
                        str21 = (String) doFlixProvider$loadLinks$1.L$4;
                        list3 = (List) doFlixProvider$loadLinks$1.L$3;
                        Function1<? super ExtractorLink, Unit> function117 = (Function1) doFlixProvider$loadLinks$1.L$2;
                        Function1<? super SubtitleFile, Unit> function118 = (Function1) doFlixProvider$loadLinks$1.L$1;
                        str16 = (String) doFlixProvider$loadLinks$1.L$0;
                        ResultKt.throwOnFailure(obj8);
                        obj7 = obj8;
                        obj5 = obj13;
                        str17 = str34;
                        str18 = str35;
                        function111 = function118;
                        z4 = z9;
                        function110 = function117;
                        function19.invoke(obj8);
                        doFlixProvider3 = doFlixProvider4;
                        continuation3 = continuation4;
                        function19 = function110;
                        episodeLinksResponse = episodeLinksResponse2;
                        it3 = it4;
                        str15 = str19;
                        function13 = function111;
                        str2 = str20;
                        str6 = str21;
                        split$default = list3;
                        obj6 = obj7;
                        str3 = str17;
                        str4 = str18;
                        if (it3.hasNext()) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        doFlixProvider$loadLinks$1 = new DoFlixProvider$loadLinks$1(doFlixProvider4, continuation4);
        Object obj82 = doFlixProvider$loadLinks$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (doFlixProvider$loadLinks$1.label) {
        }
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002d. Please report as an issue. */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0038  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0181  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x01c7  */
    /* JADX WARN: Removed duplicated region for block: B:47:0x0059  */
    /* JADX WARN: Removed duplicated region for block: B:50:0x00cd  */
    /* JADX WARN: Removed duplicated region for block: B:56:0x0111  */
    /* JADX WARN: Removed duplicated region for block: B:60:0x006e  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0030  */
    /* JADX WARN: Type inference failed for: r11v9, types: [java.util.List] */
    /* JADX WARN: Type inference failed for: r12v10, types: [java.util.List] */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object search(@NotNull String str, @NotNull Continuation<? super List<? extends SearchResponse>> continuation) {
        DoFlixProvider$search$1 doFlixProvider$search$1;
        ArrayList arrayList;
        String str2;
        Object withCustomHeaders;
        Object readValue;
        Object withCustomHeaders2;
        MultiSearchResponse multiSearchResponse;
        ArrayList arrayList2;
        Object readValue2;
        String str3 = str;
        if (continuation instanceof DoFlixProvider$search$1) {
            doFlixProvider$search$1 = (DoFlixProvider$search$1) continuation;
            if ((doFlixProvider$search$1.label & Integer.MIN_VALUE) != 0) {
                doFlixProvider$search$1.label -= Integer.MIN_VALUE;
                Object obj = doFlixProvider$search$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (doFlixProvider$search$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        arrayList = new ArrayList();
                        str2 = getMainUrl() + "/api/3/search/movie?api_key=" + this.apiKey + "&language=en&page=1&query=" + str3;
                        doFlixProvider$search$1.L$0 = str3;
                        doFlixProvider$search$1.L$1 = arrayList;
                        doFlixProvider$search$1.L$2 = SpillingKt.nullOutSpilledVariable(str2);
                        doFlixProvider$search$1.label = 1;
                        withCustomHeaders = getWithCustomHeaders(str2, doFlixProvider$search$1);
                        if (withCustomHeaders == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        String str4 = (String) withCustomHeaders;
                        readValue = this.mapper.readValue(str4, new DoFlixProvider$search$$inlined$readValue$1());
                        if (readValue instanceof MultiSearchResponse) {
                            throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(MultiSearchResponse.class).getQualifiedName() + "(non-null) but was " + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : null));
                        }
                        MultiSearchResponse multiSearchResponse2 = (MultiSearchResponse) readValue;
                        String str5 = getMainUrl() + "/api/3/search/tv?api_key=" + this.apiKey + "&language=en&page=1&query=" + str3;
                        doFlixProvider$search$1.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                        doFlixProvider$search$1.L$1 = arrayList;
                        doFlixProvider$search$1.L$2 = SpillingKt.nullOutSpilledVariable(str2);
                        doFlixProvider$search$1.L$3 = SpillingKt.nullOutSpilledVariable(str4);
                        doFlixProvider$search$1.L$4 = multiSearchResponse2;
                        doFlixProvider$search$1.L$5 = SpillingKt.nullOutSpilledVariable(str5);
                        doFlixProvider$search$1.label = 2;
                        withCustomHeaders2 = getWithCustomHeaders(str5, doFlixProvider$search$1);
                        if (withCustomHeaders2 == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        ArrayList arrayList3 = arrayList;
                        multiSearchResponse = multiSearchResponse2;
                        arrayList2 = arrayList3;
                        readValue2 = this.mapper.readValue((String) withCustomHeaders2, new DoFlixProvider$search$$inlined$readValue$2());
                        if (readValue2 instanceof MultiSearchResponse) {
                            throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(MultiSearchResponse.class).getQualifiedName() + "(non-null) but was " + (readValue2 != null ? Reflection.getOrCreateKotlinClass(readValue2.getClass()).getQualifiedName() : null));
                        }
                        MultiSearchResponse multiSearchResponse3 = (MultiSearchResponse) readValue2;
                        List<MovieResult> results = multiSearchResponse.getResults();
                        for (MovieResult movieResult : results) {
                            DoFlixProvider doFlixProvider = this;
                            String title = movieResult.getTitle();
                            if (title == null && (title = movieResult.getName()) == null) {
                                title = "Unknown";
                            }
                            arrayList2.add(MainAPIKt.newMovieSearchResponse$default(doFlixProvider, title, "movie," + movieResult.getId(), TvType.Movie, false, new DoFlixProvider$$ExternalSyntheticLambda0(movieResult), 8, (Object) null));
                            multiSearchResponse3 = multiSearchResponse3;
                            results = results;
                        }
                        List<MovieResult> results2 = multiSearchResponse3.getResults();
                        boolean z = false;
                        for (MovieResult movieResult2 : results2) {
                            DoFlixProvider doFlixProvider2 = this;
                            String name = movieResult2.getName();
                            if (name == null && (name = movieResult2.getTitle()) == null) {
                                name = "Unknown";
                            }
                            arrayList2.add(MainAPIKt.newTvSeriesSearchResponse$default(doFlixProvider2, name, "tvseries," + movieResult2.getId(), TvType.TvSeries, false, new DoFlixProvider$$ExternalSyntheticLambda1(movieResult2), 8, (Object) null));
                            results2 = results2;
                            z = z;
                            doFlixProvider$search$1 = doFlixProvider$search$1;
                        }
                        return arrayList2;
                    case 1:
                        String str6 = (String) doFlixProvider$search$1.L$2;
                        ?? r11 = (List) doFlixProvider$search$1.L$1;
                        str3 = (String) doFlixProvider$search$1.L$0;
                        ResultKt.throwOnFailure(obj);
                        str2 = str6;
                        arrayList = r11;
                        withCustomHeaders = obj;
                        String str42 = (String) withCustomHeaders;
                        readValue = this.mapper.readValue(str42, new DoFlixProvider$search$$inlined$readValue$1());
                        if (readValue instanceof MultiSearchResponse) {
                        }
                        break;
                    case 2:
                        multiSearchResponse = (MultiSearchResponse) doFlixProvider$search$1.L$4;
                        ?? r12 = (List) doFlixProvider$search$1.L$1;
                        str3 = (String) doFlixProvider$search$1.L$0;
                        ResultKt.throwOnFailure(obj);
                        arrayList2 = r12;
                        withCustomHeaders2 = obj;
                        readValue2 = this.mapper.readValue((String) withCustomHeaders2, new DoFlixProvider$search$$inlined$readValue$2());
                        if (readValue2 instanceof MultiSearchResponse) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        doFlixProvider$search$1 = new DoFlixProvider$search$1(this, continuation);
        Object obj2 = doFlixProvider$search$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (doFlixProvider$search$1.label) {
        }
    }

    public void setLang(@NotNull String str) {
        this.lang = str;
    }

    public void setMainUrl(@NotNull String str) {
        this.mainUrl = str;
    }

    public void setName(@NotNull String str) {
        this.name = str;
    }
}
