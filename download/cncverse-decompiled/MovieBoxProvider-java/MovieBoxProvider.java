package com.cncverse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import com.cncverse.donation.DonationManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.ExtensionsKt;
import com.lagradost.cloudstream3.Actor;
import com.lagradost.cloudstream3.ActorData;
import com.lagradost.cloudstream3.ActorRole;
import com.lagradost.cloudstream3.Episode;
import com.lagradost.cloudstream3.ErrorLoadingException;
import com.lagradost.cloudstream3.HomePageList;
import com.lagradost.cloudstream3.HomePageResponse;
import com.lagradost.cloudstream3.LoadResponse;
import com.lagradost.cloudstream3.MainAPI;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.MainPageData;
import com.lagradost.cloudstream3.MainPageRequest;
import com.lagradost.cloudstream3.MovieSearchResponse;
import com.lagradost.cloudstream3.Score;
import com.lagradost.cloudstream3.SearchResponse;
import com.lagradost.cloudstream3.SearchResponseList;
import com.lagradost.cloudstream3.TvType;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.collections.SetsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.random.Random;
import kotlin.text.Charsets;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MovieBoxProvider.kt */
@Metadata(d1 = {"\u0000¤\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010$\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 _2\u00020\u0001:\u0002_`B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u001b\u001a\u00020\u00052\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J\u0010\u0010\u001e\u001a\u00020\u00052\u0006\u0010\u001c\u001a\u00020\u0005H\u0002J\u0019\u0010\u001f\u001a\u00020\u00052\n\b\u0002\u0010 \u001a\u0004\u0018\u00010!H\u0002¢\u0006\u0002\u0010\"J\u0006\u0010%\u001a\u00020\u0005J\u0006\u0010-\u001a\u00020.J>\u0010/\u001a\u00020\u00052\u0006\u00100\u001a\u00020\u00052\b\u00101\u001a\u0004\u0018\u00010\u00052\b\u00102\u001a\u0004\u0018\u00010\u00052\u0006\u00103\u001a\u00020\u00052\b\u00104\u001a\u0004\u0018\u00010\u00052\u0006\u00105\u001a\u00020!H\u0003JS\u00106\u001a\u00020\u00052\u0006\u00100\u001a\u00020\u00052\b\u00101\u001a\u0004\u0018\u00010\u00052\b\u00102\u001a\u0004\u0018\u00010\u00052\u0006\u00103\u001a\u00020\u00052\n\b\u0002\u00104\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u00107\u001a\u00020\u000e2\n\b\u0002\u0010 \u001a\u0004\u0018\u00010!H\u0002¢\u0006\u0002\u00108J\u0012\u00109\u001a\u0004\u0018\u00010\u00052\u0006\u0010:\u001a\u00020\u0005H\u0002J\u001e\u0010=\u001a\u00020?2\u0006\u0010@\u001a\u00020A2\u0006\u0010B\u001a\u00020CH\u0096@¢\u0006\u0002\u0010DJ\u001c\u0010E\u001a\b\u0012\u0004\u0012\u00020F0,2\u0006\u0010G\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010HJ\u001e\u0010E\u001a\u00020I2\u0006\u0010G\u001a\u00020\u00052\u0006\u0010@\u001a\u00020AH\u0096@¢\u0006\u0002\u0010JJ\u0016\u0010K\u001a\u00020L2\u0006\u00103\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010HJF\u0010M\u001a\u00020\u000e2\u0006\u0010N\u001a\u00020\u00052\u0006\u0010O\u001a\u00020\u000e2\u0012\u0010P\u001a\u000e\u0012\u0004\u0012\u00020R\u0012\u0004\u0012\u00020S0Q2\u0012\u0010T\u001a\u000e\u0012\u0004\u0012\u00020U\u0012\u0004\u0012\u00020S0QH\u0096@¢\u0006\u0002\u0010VJ*\u0010W\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050+2\u0006\u0010X\u001a\u00020\u00052\u0006\u0010Y\u001a\u00020ZH\u0082@¢\u0006\u0002\u0010[J\u001a\u0010\\\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010]\u001a\u00020\u000eH\u0082@¢\u0006\u0002\u0010^R\u001a\u0010\u0004\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u0014\u0010\r\u001a\u00020\u000eX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0011\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0007\"\u0004\b\u0013\u0010\tR\u001a\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u000e\u0010\u0019\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010&\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b'\u0010\u0007R\u0010\u0010(\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020!X\u0082\u000e¢\u0006\u0002\n\u0000R \u0010*\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050,0+X\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010;\u001a\b\u0012\u0004\u0012\u00020<0,X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b=\u0010>¨\u0006a"}, d2 = {"Lcom/cncverse/MovieBoxProvider;", "Lcom/lagradost/cloudstream3/MainAPI;", "<init>", "()V", "mainUrl", "", "getMainUrl", "()Ljava/lang/String;", "setMainUrl", "(Ljava/lang/String;)V", "name", "getName", "setName", "hasMainPage", "", "getHasMainPage", "()Z", "lang", "getLang", "setLang", "supportedTypes", "", "Lcom/lagradost/cloudstream3/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "secretKeyDefault", "secretKeyAlt", "md5", "input", "", "reverseString", "generateXClientToken", "hardcodedTimestamp", "", "(Ljava/lang/Long;)Ljava/lang/String;", "random", "Ljava/security/SecureRandom;", "generateDeviceId", "deviceId", "getDeviceId", "cachedGuestToken", "tokenLastFetchMs", "brandModels", "", "", "randomBrandModel", "Lcom/cncverse/MovieBoxProvider$BrandModel;", "buildCanonicalString", "method", "accept", "contentType", "url", "body", "timestamp", "generateXTrSignature", "useAltKey", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Long;)Ljava/lang/String;", "extractPolicyResource", "cookie", "mainPage", "Lcom/lagradost/cloudstream3/MainPageData;", "getMainPage", "()Ljava/util/List;", "Lcom/lagradost/cloudstream3/HomePageResponse;", "page", "", "request", "Lcom/lagradost/cloudstream3/MainPageRequest;", "(ILcom/lagradost/cloudstream3/MainPageRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "Lcom/lagradost/cloudstream3/SearchResponse;", "query", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lcom/lagradost/cloudstream3/SearchResponseList;", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "load", "Lcom/lagradost/cloudstream3/LoadResponse;", "loadLinks", "data", "isCasting", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchDubDetailPaths", "detailPath", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "(Ljava/lang/String;Lcom/fasterxml/jackson/databind/ObjectMapper;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchAnonymousToken", "forceRefresh", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "BrandModel", "MovieBoxProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nMovieBoxProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MovieBoxProvider.kt\ncom/cncverse/MovieBoxProvider\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 4 Maps.kt\nkotlin/collections/MapsKt__MapsKt\n+ 5 _Maps.kt\nkotlin/collections/MapsKt___MapsKt\n+ 6 _Strings.kt\nkotlin/text/StringsKt___StringsKt\n*L\n1#1,1242:1\n1915#2,2:1243\n1642#2,10:1245\n1915#2:1255\n1916#2:1257\n1652#2:1258\n1642#2,10:1259\n1915#2:1269\n1916#2:1271\n1652#2:1272\n1696#2,8:1273\n1586#2:1281\n1661#2,3:1282\n1915#2,2:1285\n1915#2:1287\n1916#2:1295\n1915#2:1297\n296#2,2:1298\n1916#2:1307\n1#3:1256\n1#3:1270\n1#3:1300\n383#4,7:1288\n221#5:1296\n222#5:1308\n437#6:1301\n513#6,5:1302\n*S KotlinDebug\n*F\n+ 1 MovieBoxProvider.kt\ncom/cncverse/MovieBoxProvider\n*L\n281#1:1243,2\n336#1:1245,10\n336#1:1255\n336#1:1257\n336#1:1258\n499#1:1259,10\n499#1:1269\n499#1:1271\n499#1:1272\n511#1:1273,8\n515#1:1281\n515#1:1282,3\n559#1:1285,2\n586#1:1287\n586#1:1295\n601#1:1297\n603#1:1298,2\n601#1:1307\n336#1:1256\n499#1:1270\n590#1:1288,7\n600#1:1296\n600#1:1308\n620#1:1301\n620#1:1302,5\n*E\n"})
/* loaded from: classes.dex */
public final class MovieBoxProvider extends MainAPI {

    /* renamed from: Companion, reason: from kotlin metadata */
    @NotNull
    public static final Companion INSTANCE = new Companion(null);

    @Nullable
    private static Context context;

    @Nullable
    private volatile String cachedGuestToken;
    private volatile long tokenLastFetchMs;

    @NotNull
    private String mainUrl = "https://api3.aoneroom.com";

    @NotNull
    private String name = "MovieBox";
    private final boolean hasMainPage = true;

    @NotNull
    private String lang = "hi";

    @NotNull
    private final Set<TvType> supportedTypes = SetsKt.setOf(new TvType[]{TvType.Movie, TvType.TvSeries});

    @NotNull
    private final String secretKeyDefault = MainAPIKt.base64Decode("NzZpUmwwN3MweFNOOWpxbUVXQXQ3OUVCSlp1bElRSXNWNjRGWnIyTw==");

    @NotNull
    private final String secretKeyAlt = MainAPIKt.base64Decode("WHFuMm5uTzQxL0w5Mm8xaXVYaFNMSFRiWHZZNFo1Wlo2Mm04bVNMQQ==");

    @NotNull
    private final SecureRandom random = new SecureRandom();

    @NotNull
    private final String deviceId = generateDeviceId();

    @NotNull
    private final Map<String, List<String>> brandModels = MapsKt.mapOf(new Pair[]{TuplesKt.to("Samsung", CollectionsKt.listOf(new String[]{"SM-S918B", "SM-A528B", "SM-M336B"})), TuplesKt.to("Xiaomi", CollectionsKt.listOf(new String[]{"2201117TI", "M2012K11AI", "Redmi Note 11"})), TuplesKt.to("OnePlus", CollectionsKt.listOf(new String[]{"LE2111", "CPH2449", "IN2023"})), TuplesKt.to("Google", CollectionsKt.listOf(new String[]{"Pixel 6", "Pixel 7", "Pixel 8"})), TuplesKt.to("Realme", CollectionsKt.listOf(new String[]{"RMX3085", "RMX3360", "RMX3551"}))});

    @NotNull
    private final List<MainPageData> mainPage = MainAPIKt.mainPageOf(new Pair[]{TuplesKt.to("4516404531735022304", "Trending"), TuplesKt.to("5692654647815587592", "Trending in Cinema"), TuplesKt.to("414907768299210008", "Bollywood"), TuplesKt.to("3859721901924910512", "South Indian"), TuplesKt.to("8019599703232971616", "Hollywood"), TuplesKt.to("4741626294545400336", "Top Series This Week"), TuplesKt.to("8434602210994128512", "Anime"), TuplesKt.to("1255898847918934600", "Reality TV"), TuplesKt.to("4903182713986896328", "Indian Drama"), TuplesKt.to("7878715743607948784", "Korean Drama"), TuplesKt.to("8788126208987989488", "Chinese Drama"), TuplesKt.to("3910636007619709856", "Western TV"), TuplesKt.to("5177200225164885656", "Turkish Drama"), TuplesKt.to("1|1", "Movies"), TuplesKt.to("1|2", "Series"), TuplesKt.to("1|1006", "Anime"), TuplesKt.to("1|1;country=India", "Indian (Movies)"), TuplesKt.to("1|2;country=India", "Indian (Series)"), TuplesKt.to("1|1;classify=Hindi dub;country=United States", "USA (Movies)"), TuplesKt.to("1|2;classify=Hindi dub;country=United States", "USA (Series)"), TuplesKt.to("1|1;country=Japan", "Japan (Movies)"), TuplesKt.to("1|2;country=Japan", "Japan (Series)"), TuplesKt.to("1|1;country=China", "China (Movies)"), TuplesKt.to("1|2;country=China", "China (Series)"), TuplesKt.to("1|1;country=Philippines", "Philippines (Movies)"), TuplesKt.to("1|2;country=Philippines", "Philippines (Series)"), TuplesKt.to("1|1;country=Thailand", "Thailand(Movies)"), TuplesKt.to("1|2;country=Thailand", "Thailand(Series)"), TuplesKt.to("1|1;country=Nigeria", "Nollywood (Movies)"), TuplesKt.to("1|2;country=Nigeria", "Nollywood (Series)"), TuplesKt.to("1|1;country=Korea", "South Korean (Movies)"), TuplesKt.to("1|2;country=Korea", "South Korean (Series)"), TuplesKt.to("1|1;classify=Hindi dub;genre=Action", "Action (Movies)"), TuplesKt.to("1|1;classify=Hindi dub;genre=Crime", "Crime (Movies)"), TuplesKt.to("1|1;classify=Hindi dub;genre=Comedy", "Comedy (Movies)"), TuplesKt.to("1|1;classify=Hindi dub;genre=Romance", "Romance (Movies)"), TuplesKt.to("1|2;classify=Hindi dub;genre=Crime", "Crime (Series)"), TuplesKt.to("1|2;classify=Hindi dub;genre=Comedy", "Comedy (Series)"), TuplesKt.to("1|2;classify=Hindi dub;genre=Romance", "Romance (Series)")});

    /* compiled from: MovieBoxProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\n\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000b\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004J\n\u0010\u0012\u001a\u00020\u0003HÖ\u0081\u0004R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0013"}, d2 = {"Lcom/cncverse/MovieBoxProvider$BrandModel;", "", "brand", "", "model", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getBrand", "()Ljava/lang/String;", "getModel", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "MovieBoxProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class BrandModel {

        @NotNull
        private final String brand;

        @NotNull
        private final String model;

        public BrandModel(@NotNull String str, @NotNull String str2) {
            this.brand = str;
            this.model = str2;
        }

        public static /* synthetic */ BrandModel copy$default(BrandModel brandModel, String str, String str2, int i, Object obj) {
            if ((i & 1) != 0) {
                str = brandModel.brand;
            }
            if ((i & 2) != 0) {
                str2 = brandModel.model;
            }
            return brandModel.copy(str, str2);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getBrand() {
            return this.brand;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getModel() {
            return this.model;
        }

        @NotNull
        public final BrandModel copy(@NotNull String brand, @NotNull String model) {
            return new BrandModel(brand, model);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof BrandModel)) {
                return false;
            }
            BrandModel brandModel = (BrandModel) other;
            return Intrinsics.areEqual(this.brand, brandModel.brand) && Intrinsics.areEqual(this.model, brandModel.model);
        }

        @NotNull
        public final String getBrand() {
            return this.brand;
        }

        @NotNull
        public final String getModel() {
            return this.model;
        }

        public int hashCode() {
            return (this.brand.hashCode() * 31) + this.model.hashCode();
        }

        @NotNull
        public String toString() {
            return "BrandModel(brand=" + this.brand + ", model=" + this.model + ")";
        }
    }

    /* compiled from: MovieBoxProvider.kt */
    @Metadata(d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t¨\u0006\n"}, d2 = {"Lcom/cncverse/MovieBoxProvider$Companion;", "", "<init>", "()V", "context", "Landroid/content/Context;", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "MovieBoxProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @Nullable
        public final Context getContext() {
            return MovieBoxProvider.context;
        }

        public final void setContext(@Nullable Context context) {
            MovieBoxProvider.context = context;
        }
    }

    /* compiled from: MovieBoxProvider.kt */
    @Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[ExtractorLinkType.values().length];
            try {
                iArr[ExtractorLinkType.VIDEO.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ExtractorLinkType.DASH.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            $EnumSwitchMapping$0 = iArr;
        }
    }

    @SuppressLint({"UseKtx"})
    private final String buildCanonicalString(String method, String accept, String contentType, String url, String body, long timestamp) {
        byte[] bArr;
        String str;
        String str2;
        Uri parse = Uri.parse(url);
        String path = parse.getPath();
        if (path == null) {
            path = "";
        }
        String joinToString$default = !parse.getQueryParameterNames().isEmpty() ? CollectionsKt.joinToString$default(CollectionsKt.sorted(parse.getQueryParameterNames()), "&", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new MovieBoxProvider$$ExternalSyntheticLambda7(parse), 30, (Object) null) : "";
        String str3 = joinToString$default.length() > 0 ? path + "?" + joinToString$default : path;
        if (body != null) {
            bArr = body.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bArr, "getBytes(...)");
        } else {
            bArr = null;
        }
        if (bArr != null) {
            str = md5(bArr.length > 102400 ? ArraysKt.copyOfRange(bArr, 0, 102400) : bArr);
        } else {
            str = "";
        }
        if (bArr == null || (str2 = String.valueOf(bArr.length)) == null) {
            str2 = "";
        }
        String upperCase = method.toUpperCase(Locale.ROOT);
        Intrinsics.checkNotNullExpressionValue(upperCase, "toUpperCase(...)");
        return upperCase + "\n" + (accept == null ? "" : accept) + "\n" + (contentType != null ? contentType : "") + "\n" + str2 + "\n" + timestamp + "\n" + str + "\n" + str3;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence buildCanonicalString$lambda$0(Uri uri, String str) {
        return CollectionsKt.joinToString$default(uri.getQueryParameters(str), "&", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new MovieBoxProvider$$ExternalSyntheticLambda1(str), 30, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence buildCanonicalString$lambda$0$0(String str, String str2) {
        return str + "=" + str2;
    }

    /* JADX WARN: Code restructure failed: missing block: B:69:0x019d, code lost:
    
        if (r13.length() == 0) goto L80;
     */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00b0 A[Catch: Exception -> 0x01a5, TryCatch #0 {Exception -> 0x01a5, blocks: (B:3:0x0005, B:5:0x001b, B:8:0x002d, B:12:0x003b, B:14:0x0041, B:16:0x0047, B:18:0x004d, B:20:0x0053, B:23:0x006c, B:26:0x0080, B:28:0x0086, B:30:0x008e, B:32:0x009a, B:34:0x00a1, B:37:0x00a8, B:39:0x00b0, B:40:0x00cc, B:42:0x00fa, B:50:0x0105, B:52:0x0116, B:54:0x011c, B:56:0x0128, B:58:0x0130, B:59:0x014b, B:61:0x0180, B:63:0x0186, B:65:0x018e, B:66:0x0194, B:68:0x0199, B:79:0x005a, B:81:0x0060, B:83:0x0033), top: B:2:0x0005 }] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x00cb  */
    /* JADX WARN: Removed duplicated region for block: B:56:0x0128 A[Catch: Exception -> 0x01a5, TryCatch #0 {Exception -> 0x01a5, blocks: (B:3:0x0005, B:5:0x001b, B:8:0x002d, B:12:0x003b, B:14:0x0041, B:16:0x0047, B:18:0x004d, B:20:0x0053, B:23:0x006c, B:26:0x0080, B:28:0x0086, B:30:0x008e, B:32:0x009a, B:34:0x00a1, B:37:0x00a8, B:39:0x00b0, B:40:0x00cc, B:42:0x00fa, B:50:0x0105, B:52:0x0116, B:54:0x011c, B:56:0x0128, B:58:0x0130, B:59:0x014b, B:61:0x0180, B:63:0x0186, B:65:0x018e, B:66:0x0194, B:68:0x0199, B:79:0x005a, B:81:0x0060, B:83:0x0033), top: B:2:0x0005 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private final String extractPolicyResource(String cookie) {
        String obj;
        boolean z;
        String str;
        MatchResult find$default;
        String str2;
        JsonNode jsonNode;
        JsonNode jsonNode2;
        List groupValues;
        List groupValues2;
        String str3;
        String trim;
        String str4;
        JsonNode jsonNode3;
        JsonNode jsonNode4;
        try {
            obj = StringsKt.trim(cookie).toString();
            z = false;
        } catch (Exception e) {
        }
        if (StringsKt.startsWith$default(obj, "{", false, 2, (Object) null)) {
            JsonNode readTree = MainAPIKt.getMapper().readTree(obj);
            JsonNode jsonNode5 = readTree.get("data");
            if (jsonNode5 == null || (jsonNode3 = jsonNode5.get("streams")) == null) {
                jsonNode3 = readTree.get("streams");
            }
            if (jsonNode3 == null || !jsonNode3.isArray() || jsonNode3.size() <= 0) {
                JsonNode jsonNode6 = readTree.get("signCookie");
                str = jsonNode6 != null ? jsonNode6.asText() : null;
            } else {
                JsonNode jsonNode7 = jsonNode3.get(0);
                str = (jsonNode7 == null || (jsonNode4 = jsonNode7.get("signCookie")) == null) ? null : jsonNode4.asText();
            }
            if (str == null) {
            }
            find$default = Regex.find$default(new Regex("urlprefix=([^:;\\s]+)"), str, 0, 2, (Object) null);
            if (find$default != null && (groupValues2 = find$default.getGroupValues()) != null && (str3 = (String) groupValues2.get(1)) != null && (trim = StringsKt.trim(str3, new char[]{'\"', '\'', ' '})) != null) {
                if (!StringsKt.startsWith$default(trim, "http://", false, 2, (Object) null) && !StringsKt.startsWith$default(trim, "https://", false, 2, (Object) null)) {
                    int length = trim.length() % 4;
                    str4 = new String(MainAPIKt.base64DecodeArray(StringsKt.replace$default(StringsKt.replace$default(length <= 0 ? trim + StringsKt.repeat("=", 4 - length) : trim, '-', '+', false, 4, (Object) null), '_', '/', false, 4, (Object) null)), Charsets.UTF_8);
                    if (!StringsKt.startsWith$default(str4, "http://", false, 2, (Object) null) && !StringsKt.startsWith$default(str4, "https://", false, 2, (Object) null)) {
                    }
                    return str4;
                }
                return trim;
            }
            MatchResult find$default2 = Regex.find$default(new Regex("(?:CloudFront-)?Policy=([^;]+)"), str, 0, 2, (Object) null);
            str2 = (find$default2 != null || (groupValues = find$default2.getGroupValues()) == null) ? null : (String) groupValues.get(1);
            if (str2 != null) {
                int length2 = str2.length() % 4;
                JsonNode jsonNode8 = MainAPIKt.getMapper().readTree(new String(MainAPIKt.base64DecodeArray(StringsKt.replace$default(StringsKt.replace$default(length2 > 0 ? str2 + StringsKt.repeat("=", 4 - length2) : str2, '-', '+', false, 4, (Object) null), '_', '/', false, 4, (Object) null)), Charsets.UTF_8)).get("Statement");
                String asText = (jsonNode8 == null || (jsonNode = jsonNode8.get(0)) == null || (jsonNode2 = jsonNode.get("Resource")) == null) ? null : jsonNode2.asText();
                String str5 = asText;
                if (str5 != null) {
                }
                z = true;
                if (!z) {
                    return asText;
                }
            }
            return null;
        }
        str = obj;
        find$default = Regex.find$default(new Regex("urlprefix=([^:;\\s]+)"), str, 0, 2, (Object) null);
        if (find$default != null) {
            if (!StringsKt.startsWith$default(trim, "http://", false, 2, (Object) null)) {
                int length3 = trim.length() % 4;
                str4 = new String(MainAPIKt.base64DecodeArray(StringsKt.replace$default(StringsKt.replace$default(length3 <= 0 ? trim + StringsKt.repeat("=", 4 - length3) : trim, '-', '+', false, 4, (Object) null), '_', '/', false, 4, (Object) null)), Charsets.UTF_8);
                if (!StringsKt.startsWith$default(str4, "http://", false, 2, (Object) null)) {
                }
                return str4;
            }
            return trim;
        }
        MatchResult find$default22 = Regex.find$default(new Regex("(?:CloudFront-)?Policy=([^;]+)"), str, 0, 2, (Object) null);
        if (find$default22 != null) {
        }
        if (str2 != null) {
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002f. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x003a  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0187 A[Catch: Exception -> 0x01b4, TryCatch #5 {Exception -> 0x01b4, blocks: (B:16:0x016a, B:18:0x017b, B:23:0x0187, B:25:0x0197, B:26:0x019e, B:28:0x01a3, B:31:0x01ad), top: B:15:0x016a }] */
    /* JADX WARN: Removed duplicated region for block: B:44:0x0064  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0032  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object fetchAnonymousToken(boolean z, Continuation<? super String> continuation) {
        MovieBoxProvider$fetchAnonymousToken$1 movieBoxProvider$fetchAnonymousToken$1;
        MovieBoxProvider$fetchAnonymousToken$1 movieBoxProvider$fetchAnonymousToken$12;
        long currentTimeMillis;
        Map mapOf;
        Requests app;
        long j;
        Object obj;
        String str;
        String str2;
        boolean z2;
        if (continuation instanceof MovieBoxProvider$fetchAnonymousToken$1) {
            movieBoxProvider$fetchAnonymousToken$1 = (MovieBoxProvider$fetchAnonymousToken$1) continuation;
            if ((movieBoxProvider$fetchAnonymousToken$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProvider$fetchAnonymousToken$1.label -= Integer.MIN_VALUE;
                movieBoxProvider$fetchAnonymousToken$12 = movieBoxProvider$fetchAnonymousToken$1;
                Object obj2 = movieBoxProvider$fetchAnonymousToken$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProvider$fetchAnonymousToken$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj2);
                        currentTimeMillis = System.currentTimeMillis();
                        if (!z && this.cachedGuestToken != null && currentTimeMillis - this.tokenLastFetchMs < 3600000) {
                            return this.cachedGuestToken;
                        }
                        try {
                            String generateXClientToken = generateXClientToken(Boxing.boxLong(currentTimeMillis));
                            try {
                                try {
                                    String generateXTrSignature$default = generateXTrSignature$default(this, "GET", "application/json", "application/json", "https://apig.inmoviebox.com/wefeed-mobile-bff/tab/ranking-list?tabId=0&categoryType=4516404531735022304&page=1&perPage=1", null, false, Boxing.boxLong(currentTimeMillis), 48, null);
                                    mapOf = MapsKt.mapOf(new Pair[]{TuplesKt.to("user-agent", "com.community.mbox.in/50020042 (Linux; U; Android 16; en_IN; sdk_gphone64_x86_64; Build/BP22.250325.006; Cronet/133.0.6876.3)"), TuplesKt.to("accept", "application/json"), TuplesKt.to("content-type", "application/json"), TuplesKt.to("x-client-token", generateXClientToken), TuplesKt.to("x-tr-signature", generateXTrSignature$default), TuplesKt.to("x-client-info", "{\"package_name\":\"com.community.mbox.in\",\"version_name\":\"3.0.03.0529.03\",\"version_code\":50020042,\"os\":\"android\",\"os_version\":\"16\",\"device_id\":\"" + this.deviceId + "\",\"install_store\":\"ps\",\"gaid\":\"d7578036d13336cc\",\"brand\":\"google\",\"model\":\"SM-S918B\",\"system_language\":\"en\",\"net\":\"NETWORK_WIFI\",\"region\":\"IN\",\"timezone\":\"Asia/Calcutta\",\"sp_code\":\"\"}"), TuplesKt.to("x-client-status", "0")});
                                    app = MainActivityKt.getApp();
                                    movieBoxProvider$fetchAnonymousToken$12.L$0 = SpillingKt.nullOutSpilledVariable("https://apig.inmoviebox.com/wefeed-mobile-bff/tab/ranking-list?tabId=0&categoryType=4516404531735022304&page=1&perPage=1");
                                    movieBoxProvider$fetchAnonymousToken$12.L$1 = SpillingKt.nullOutSpilledVariable(generateXClientToken);
                                    movieBoxProvider$fetchAnonymousToken$12.L$2 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                                    movieBoxProvider$fetchAnonymousToken$12.L$3 = SpillingKt.nullOutSpilledVariable(mapOf);
                                    movieBoxProvider$fetchAnonymousToken$12.Z$0 = z;
                                    j = currentTimeMillis;
                                    try {
                                        movieBoxProvider$fetchAnonymousToken$12.J$0 = j;
                                        movieBoxProvider$fetchAnonymousToken$12.label = 1;
                                    } catch (Exception e) {
                                        currentTimeMillis = j;
                                    }
                                } catch (Exception e2) {
                                    currentTimeMillis = currentTimeMillis;
                                }
                                try {
                                    obj = Requests.get$default(app, "https://apig.inmoviebox.com/wefeed-mobile-bff/tab/ranking-list?tabId=0&categoryType=4516404531735022304&page=1&perPage=1", mapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$fetchAnonymousToken$12, 4092, (Object) null);
                                } catch (Exception e3) {
                                    currentTimeMillis = j;
                                    return null;
                                }
                            } catch (Exception e4) {
                                currentTimeMillis = currentTimeMillis;
                            }
                        } catch (Exception e5) {
                        }
                        if (obj == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        obj2 = obj;
                        try {
                            str = ((NiceResponse) obj2).getHeaders().get("x-user");
                            str2 = str;
                        } catch (Exception e6) {
                            currentTimeMillis = j;
                            return null;
                        }
                        if (str2 != null && !StringsKt.isBlank(str2)) {
                            z2 = false;
                            if (!z2) {
                                JsonNode jsonNode = ExtensionsKt.jacksonObjectMapper().readTree(str).get("token");
                                String asText = jsonNode != null ? jsonNode.asText() : null;
                                String str3 = asText;
                                if (!(str3 == null || StringsKt.isBlank(str3))) {
                                    this.cachedGuestToken = asText;
                                    this.tokenLastFetchMs = j;
                                    return asText;
                                }
                            }
                            return null;
                        }
                        z2 = true;
                        if (!z2) {
                        }
                        return null;
                    case 1:
                        currentTimeMillis = movieBoxProvider$fetchAnonymousToken$12.J$0;
                        boolean z3 = movieBoxProvider$fetchAnonymousToken$12.Z$0;
                        try {
                            ResultKt.throwOnFailure(obj2);
                            j = currentTimeMillis;
                            str = ((NiceResponse) obj2).getHeaders().get("x-user");
                            str2 = str;
                        } catch (Exception e7) {
                            return null;
                        }
                        if (str2 != null) {
                            z2 = false;
                            if (!z2) {
                            }
                            return null;
                        }
                        z2 = true;
                        if (!z2) {
                        }
                        return null;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        movieBoxProvider$fetchAnonymousToken$1 = new MovieBoxProvider$fetchAnonymousToken$1(this, continuation);
        movieBoxProvider$fetchAnonymousToken$12 = movieBoxProvider$fetchAnonymousToken$1;
        Object obj22 = movieBoxProvider$fetchAnonymousToken$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProvider$fetchAnonymousToken$12.label) {
        }
    }

    static /* synthetic */ Object fetchAnonymousToken$default(MovieBoxProvider movieBoxProvider, boolean z, Continuation continuation, int i, Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        return movieBoxProvider.fetchAnonymousToken(z, continuation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0028. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0126 A[Catch: Exception -> 0x0164, TryCatch #3 {Exception -> 0x0164, blocks: (B:16:0x00e7, B:18:0x00fe, B:20:0x0106, B:21:0x010e, B:23:0x0116, B:25:0x011c, B:26:0x0120, B:28:0x0126, B:31:0x0134, B:34:0x013b, B:37:0x0143, B:40:0x0149, B:45:0x015a, B:59:0x015e), top: B:15:0x00e7 }] */
    /* JADX WARN: Removed duplicated region for block: B:69:0x004f  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object fetchDubDetailPaths(String str, ObjectMapper objectMapper, Continuation<? super Map<String, String>> continuation) {
        MovieBoxProvider$fetchDubDetailPaths$1 movieBoxProvider$fetchDubDetailPaths$1;
        MovieBoxProvider$fetchDubDetailPaths$1 movieBoxProvider$fetchDubDetailPaths$12;
        Requests app;
        String str2;
        Map mapOf;
        ObjectMapper objectMapper2;
        JsonNode jsonNode;
        Iterator it;
        String asText;
        JsonNode jsonNode2;
        String asText2;
        JsonNode jsonNode3;
        if (continuation instanceof MovieBoxProvider$fetchDubDetailPaths$1) {
            movieBoxProvider$fetchDubDetailPaths$1 = (MovieBoxProvider$fetchDubDetailPaths$1) continuation;
            if ((movieBoxProvider$fetchDubDetailPaths$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProvider$fetchDubDetailPaths$1.label -= Integer.MIN_VALUE;
                movieBoxProvider$fetchDubDetailPaths$12 = movieBoxProvider$fetchDubDetailPaths$1;
                Object obj = movieBoxProvider$fetchDubDetailPaths$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProvider$fetchDubDetailPaths$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        try {
                            app = MainActivityKt.getApp();
                            str2 = "https://movie-box.co/wefeed-h5api-bff/detail?detailPath=" + str;
                            mapOf = MapsKt.mapOf(new Pair[]{TuplesKt.to("Accept", "application/json"), TuplesKt.to("Origin", "https://movie-box.co"), TuplesKt.to("Referer", "https://mzfi.me/"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Safari/537.36"), TuplesKt.to("x-client-info", "{\"timezone\":\"Asia/Calcutta\"}")});
                            movieBoxProvider$fetchDubDetailPaths$12.L$0 = SpillingKt.nullOutSpilledVariable(str);
                            movieBoxProvider$fetchDubDetailPaths$12.L$1 = objectMapper;
                            movieBoxProvider$fetchDubDetailPaths$12.label = 1;
                        } catch (Exception e) {
                        }
                        try {
                            obj = Requests.get$default(app, str2, mapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$fetchDubDetailPaths$12, 4092, (Object) null);
                            if (obj == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            objectMapper2 = objectMapper;
                            try {
                                JsonNode jsonNode4 = objectMapper2.readTree(((NiceResponse) obj).getBody().string()).get("data");
                                jsonNode = (jsonNode4 != null || (jsonNode3 = jsonNode4.get("subject")) == null) ? null : jsonNode3.get("dubs");
                                Map createMapBuilder = MapsKt.createMapBuilder();
                                if (jsonNode != null && jsonNode.isArray()) {
                                    it = jsonNode.iterator();
                                    while (it.hasNext()) {
                                        JsonNode jsonNode5 = (JsonNode) it.next();
                                        JsonNode jsonNode6 = jsonNode5.get("subjectId");
                                        if (jsonNode6 != null && (asText = jsonNode6.asText()) != null && (jsonNode2 = jsonNode5.get("detailPath")) != null && (asText2 = jsonNode2.asText()) != null) {
                                            if (StringsKt.isBlank(asText2)) {
                                                asText2 = null;
                                            }
                                            if (asText2 != null) {
                                                createMapBuilder.put(asText, asText2);
                                            }
                                        }
                                    }
                                }
                                return MapsKt.build(createMapBuilder);
                            } catch (Exception e2) {
                                return MapsKt.emptyMap();
                            }
                        } catch (Exception e3) {
                            return MapsKt.emptyMap();
                        }
                    case 1:
                        objectMapper2 = (ObjectMapper) movieBoxProvider$fetchDubDetailPaths$12.L$1;
                        try {
                            ResultKt.throwOnFailure(obj);
                            JsonNode jsonNode42 = objectMapper2.readTree(((NiceResponse) obj).getBody().string()).get("data");
                            if (jsonNode42 != null) {
                                break;
                            }
                            Map createMapBuilder2 = MapsKt.createMapBuilder();
                            if (jsonNode != null) {
                                it = jsonNode.iterator();
                                while (it.hasNext()) {
                                }
                                break;
                            }
                            return MapsKt.build(createMapBuilder2);
                        } catch (Exception e4) {
                            return MapsKt.emptyMap();
                        }
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        movieBoxProvider$fetchDubDetailPaths$1 = new MovieBoxProvider$fetchDubDetailPaths$1(this, continuation);
        movieBoxProvider$fetchDubDetailPaths$12 = movieBoxProvider$fetchDubDetailPaths$1;
        Object obj2 = movieBoxProvider$fetchDubDetailPaths$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProvider$fetchDubDetailPaths$12.label) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence generateDeviceId$lambda$0(byte b) {
        String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }

    private final String generateXClientToken(Long hardcodedTimestamp) {
        String valueOf = String.valueOf(hardcodedTimestamp != null ? hardcodedTimestamp.longValue() : System.currentTimeMillis());
        byte[] bytes = reverseString(valueOf).getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
        return valueOf + "," + md5(bytes);
    }

    static /* synthetic */ String generateXClientToken$default(MovieBoxProvider movieBoxProvider, Long l, int i, Object obj) {
        if ((i & 1) != 0) {
            l = null;
        }
        return movieBoxProvider.generateXClientToken(l);
    }

    private final String generateXTrSignature(String method, String accept, String contentType, String url, String body, boolean useAltKey, Long hardcodedTimestamp) {
        long longValue = hardcodedTimestamp != null ? hardcodedTimestamp.longValue() : System.currentTimeMillis();
        String buildCanonicalString = buildCanonicalString(method, accept, contentType, url, body, longValue);
        byte[] base64DecodeArray = MainAPIKt.base64DecodeArray(useAltKey ? this.secretKeyAlt : this.secretKeyDefault);
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(new SecretKeySpec(base64DecodeArray, "HmacMD5"));
        byte[] bytes = buildCanonicalString.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
        return longValue + "|2|" + MainAPIKt.base64Encode(mac.doFinal(bytes));
    }

    static /* synthetic */ String generateXTrSignature$default(MovieBoxProvider movieBoxProvider, String str, String str2, String str3, String str4, String str5, boolean z, Long l, int i, Object obj) {
        if ((i & 16) != 0) {
            str5 = null;
        }
        if ((i & 32) != 0) {
            z = false;
        }
        if ((i & 64) != 0) {
            l = null;
        }
        return movieBoxProvider.generateXTrSignature(str, str2, str3, str4, str5, z, l);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$1$0(String str, JsonNode jsonNode, MovieSearchResponse movieSearchResponse) {
        movieSearchResponse.setPosterUrl(str);
        Score.Companion companion = Score.Companion;
        JsonNode jsonNode2 = jsonNode.get("imdbRatingValue");
        movieSearchResponse.setScore(companion.from10(jsonNode2 != null ? jsonNode2.asText() : null));
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit load$lambda$7$0$5(String str, int i, int i2, String str2, String str3, Integer num, String str4, Episode episode) {
        episode.setName(str);
        episode.setSeason(Integer.valueOf(i));
        episode.setEpisode(Integer.valueOf(i2));
        episode.setPosterUrl(str2);
        episode.setDescription(str3);
        episode.setRunTime(num);
        MainAPIKt.addDate$default(episode, str4, (String) null, 2, (Object) null);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit load$lambda$8(String str, Episode episode) {
        episode.setName("Episode 1");
        episode.setSeason(1);
        episode.setEpisode(1);
        episode.setPosterUrl(str);
        return Unit.INSTANCE;
    }

    /*  JADX ERROR: Types fix failed
        java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryPossibleTypes(FixTypesVisitor.java:183)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.deduceType(FixTypesVisitor.java:242)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryDeduceTypes(FixTypesVisitor.java:221)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:91)
        */
    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Not initialized variable reg: 33, insn: 0x01d5: MOVE (r17 I:??[OBJECT, ARRAY]) = (r33 I:??[OBJECT, ARRAY]), block:B:452:0x01d3 */
    /* JADX WARN: Not initialized variable reg: 37, insn: 0x03b7: MOVE (r40 I:??[OBJECT, ARRAY]) = (r37 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Not initialized variable reg: 38, insn: 0x03bd: MOVE (r43 I:??[OBJECT, ARRAY]) = (r38 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Not initialized variable reg: 40, insn: 0x03b1: MOVE (r45 I:??[OBJECT, ARRAY]) = (r40 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Not initialized variable reg: 41, insn: 0x03b3: MOVE (r46 I:??[OBJECT, ARRAY]) = (r41 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Not initialized variable reg: 42, insn: 0x03b5: MOVE (r47 I:??[OBJECT, ARRAY]) = (r42 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Not initialized variable reg: 43, insn: 0x03bb: MOVE (r3 I:??[OBJECT, ARRAY]) = (r43 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Not initialized variable reg: 45, insn: 0x03a5: MOVE (r17 I:??[OBJECT, ARRAY]) = (r45 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Not initialized variable reg: 46, insn: 0x03a7: MOVE (r18 I:??[OBJECT, ARRAY]) = (r46 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Not initialized variable reg: 47, insn: 0x03a9: MOVE (r7 I:??[OBJECT, ARRAY]) = (r47 I:??[OBJECT, ARRAY]), block:B:450:0x038c */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:147:0x0a7f -> B:97:0x0a9b). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:50:0x105e -> B:15:0x107b). Please report as a decompilation issue!!! */
    public static final java.lang.Object loadLinks$emitNetStream(java.lang.String r81, int r82, int r83, kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> r84, com.cncverse.MovieBoxProvider r85, java.lang.String r86, kotlin.jvm.internal.Ref.ObjectRef<java.lang.String> r87, com.fasterxml.jackson.databind.ObjectMapper r88, kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> r89, java.lang.String r90, java.lang.String r91, com.fasterxml.jackson.databind.JsonNode r92, java.lang.String r93, kotlin.coroutines.Continuation<? super kotlin.Unit> r94) {
        /*
            Method dump skipped, instructions count: 4648
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.cncverse.MovieBoxProvider.loadLinks$emitNetStream(java.lang.String, int, int, kotlin.jvm.functions.Function1, com.cncverse.MovieBoxProvider, java.lang.String, kotlin.jvm.internal.Ref$ObjectRef, com.fasterxml.jackson.databind.ObjectMapper, kotlin.jvm.functions.Function1, java.lang.String, java.lang.String, com.fasterxml.jackson.databind.JsonNode, java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private final String md5(byte[] input) {
        return ArraysKt.joinToString$default(MessageDigest.getInstance("MD5").digest(input), "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new MovieBoxProvider$$ExternalSyntheticLambda5(), 30, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence md5$lambda$0(byte b) {
        String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }

    private final String reverseString(String input) {
        return StringsKt.reversed(input).toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit search$lambda$0(String str, JsonNode jsonNode, MovieSearchResponse movieSearchResponse) {
        movieSearchResponse.setPosterUrl(str);
        Score.Companion companion = Score.Companion;
        JsonNode jsonNode2 = jsonNode.get("imdbRatingValue");
        movieSearchResponse.setScore(companion.from10(jsonNode2 != null ? jsonNode2.asText() : null));
        return Unit.INSTANCE;
    }

    @NotNull
    public final String generateDeviceId() {
        byte[] bArr = new byte[16];
        this.random.nextBytes(bArr);
        return ArraysKt.joinToString$default(bArr, "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new MovieBoxProvider$$ExternalSyntheticLambda2(), 30, (Object) null);
    }

    @NotNull
    public final String getDeviceId() {
        return this.deviceId;
    }

    public boolean getHasMainPage() {
        return this.hasMainPage;
    }

    @NotNull
    public String getLang() {
        return this.lang;
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x0671, code lost:
    
        if (r2 == null) goto L154;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002a. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:100:0x0131  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x0669  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0758 A[Catch: Exception -> 0x0781, TryCatch #4 {Exception -> 0x0781, blocks: (B:34:0x06e4, B:39:0x0758, B:44:0x06ec, B:46:0x06f6, B:49:0x06fd, B:51:0x0705, B:53:0x070d, B:54:0x0713, B:56:0x071b, B:57:0x0721, B:58:0x0724, B:60:0x0733, B:61:0x0727, B:62:0x072c, B:74:0x076f), top: B:33:0x06e4 }] */
    /* JADX WARN: Removed duplicated region for block: B:42:0x075f  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x0797  */
    /* JADX WARN: Removed duplicated region for block: B:82:0x0681 A[Catch: Exception -> 0x0674, TRY_ENTER, TryCatch #2 {Exception -> 0x0674, blocks: (B:22:0x066b, B:82:0x0681, B:84:0x068b), top: B:21:0x066b }] */
    /* JADX WARN: Removed duplicated region for block: B:84:0x068b A[Catch: Exception -> 0x0674, TRY_LEAVE, TryCatch #2 {Exception -> 0x0674, blocks: (B:22:0x066b, B:82:0x0681, B:84:0x068b), top: B:21:0x066b }] */
    /* JADX WARN: Removed duplicated region for block: B:87:0x0696  */
    /* JADX WARN: Removed duplicated region for block: B:88:0x0688  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002d  */
    /* JADX WARN: Removed duplicated region for block: B:91:0x0679  */
    /* JADX WARN: Removed duplicated region for block: B:98:0x00b4  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getMainPage(int i, @NotNull MainPageRequest mainPageRequest, @NotNull Continuation<? super HomePageResponse> continuation) {
        MovieBoxProvider$getMainPage$1 movieBoxProvider$getMainPage$1;
        MovieBoxProvider$getMainPage$1 movieBoxProvider$getMainPage$12;
        String str;
        String str2;
        MainPageRequest mainPageRequest2;
        Object obj;
        String str3;
        int i2;
        Object obj2;
        String str4;
        Map map;
        Map map2;
        String str5;
        MainPageRequest mainPageRequest3;
        int i3;
        RequestBody requestBody;
        String str6;
        Integer intOrNull;
        NiceResponse niceResponse;
        int i4;
        MainPageRequest mainPageRequest4;
        MainPageRequest mainPageRequest5;
        ArrayList arrayList;
        JsonNode readTree;
        JsonNode jsonNode;
        JsonNode jsonNode2;
        JsonNode jsonNode3;
        JsonNode jsonNode4;
        boolean z;
        int i5;
        MovieSearchResponse movieSearchResponse;
        ArrayList arrayList2;
        String asText;
        String asText2;
        TvType tvType;
        JsonNode jsonNode5;
        if (continuation instanceof MovieBoxProvider$getMainPage$1) {
            movieBoxProvider$getMainPage$1 = (MovieBoxProvider$getMainPage$1) continuation;
            if ((movieBoxProvider$getMainPage$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProvider$getMainPage$1.label -= Integer.MIN_VALUE;
                movieBoxProvider$getMainPage$12 = movieBoxProvider$getMainPage$1;
                Object obj3 = movieBoxProvider$getMainPage$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProvider$getMainPage$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj3);
                        DonationManager.INSTANCE.checkAndShow(getName());
                        String sb = (StringsKt.contains$default(mainPageRequest.getData(), "|", false, 2, (Object) null) ? new StringBuilder().append(getMainUrl()).append("/wefeed-mobile-bff/subject-api/list") : new StringBuilder().append(getMainUrl()).append("/wefeed-mobile-bff/tab/ranking-list?tabId=0&categoryType=").append(mainPageRequest.getData()).append("&page=").append(i).append("&perPage=").append(15)).toString();
                        String data = mainPageRequest.getData();
                        List split$default = StringsKt.split$default(StringsKt.substringBefore$default(data, ";", (String) null, 2, (Object) null), new String[]{"|"}, false, 0, 6, (Object) null);
                        String str7 = (String) CollectionsKt.getOrNull(split$default, 0);
                        int intValue = (str7 == null || (intOrNull = StringsKt.toIntOrNull(str7)) == null) ? 1 : intOrNull.intValue();
                        String str8 = (String) CollectionsKt.getOrNull(split$default, 1);
                        LinkedHashMap linkedHashMap = new LinkedHashMap();
                        List split$default2 = StringsKt.split$default(StringsKt.substringAfter(data, ";", ""), new String[]{";"}, false, 0, 6, (Object) null);
                        Iterator it = split$default2.iterator();
                        while (it.hasNext()) {
                            Object obj4 = coroutine_suspended;
                            List split$default3 = StringsKt.split$default((String) it.next(), new String[]{"="}, false, 0, 6, (Object) null);
                            String str9 = data;
                            String str10 = sb;
                            List list = split$default2;
                            Pair pair = TuplesKt.to(CollectionsKt.getOrNull(split$default3, 0), CollectionsKt.getOrNull(split$default3, 1));
                            String str11 = (String) pair.component1();
                            String str12 = (String) pair.component2();
                            String str13 = str11;
                            if (!(str13 == null || StringsKt.isBlank(str13))) {
                                String str14 = str12;
                                if (!(str14 == null || StringsKt.isBlank(str14))) {
                                    linkedHashMap.put(str11, str12);
                                }
                            }
                            sb = str10;
                            data = str9;
                            split$default2 = list;
                            coroutine_suspended = obj4;
                        }
                        Object obj5 = coroutine_suspended;
                        String str15 = data;
                        String str16 = sb;
                        String str17 = (String) linkedHashMap.get("classify");
                        if (str17 == null) {
                            str17 = "All";
                        }
                        String str18 = (String) linkedHashMap.get("country");
                        if (str18 == null) {
                            str18 = "All";
                        }
                        String str19 = (String) linkedHashMap.get("year");
                        if (str19 == null) {
                            str19 = "All";
                        }
                        String str20 = (String) linkedHashMap.get("genre");
                        String str21 = str20 != null ? str20 : "All";
                        String str22 = (String) linkedHashMap.get("sort");
                        if (str22 == null) {
                            str22 = "ForYou";
                        }
                        String str23 = "{\"page\":" + intValue + ",\"perPage\":15,\"channelId\":\"" + str8 + "\",\"classify\":\"" + str17 + "\",\"country\":\"" + str18 + "\",\"year\":\"" + str19 + "\",\"genre\":\"" + str21 + "\",\"sort\":\"" + str22 + "\"}";
                        String str24 = str17;
                        String str25 = str22;
                        String generateXClientToken$default = generateXClientToken$default(this, null, 1, null);
                        String str26 = str19;
                        int i6 = intValue;
                        String str27 = str21;
                        String str28 = str18;
                        String generateXTrSignature$default = generateXTrSignature$default(this, "POST", "application/json", "application/json; charset=utf-8", str16, str23, false, null, 96, null);
                        str = "data";
                        String generateXTrSignature$default2 = generateXTrSignature$default(this, "GET", "application/json", "application/json", str16, null, false, null, 112, null);
                        Map mapOf = MapsKt.mapOf(new Pair[]{TuplesKt.to("user-agent", "com.community.mbox.in/50020042 (Linux; U; Android 16; en_IN; sdk_gphone64_x86_64; Build/BP22.250325.006; Cronet/133.0.6876.3)"), TuplesKt.to("accept", "application/json"), TuplesKt.to("content-type", "application/json"), TuplesKt.to("connection", "keep-alive"), TuplesKt.to("x-client-token", generateXClientToken$default), TuplesKt.to("x-tr-signature", generateXTrSignature$default), TuplesKt.to("x-client-info", "{\"package_name\":\"com.community.mbox.in\",\"version_name\":\"3.0.03.0529.03\",\"version_code\":50020042,\"os\":\"android\",\"os_version\":\"16\",\"device_id\":\"" + this.deviceId + "\",\"install_store\":\"ps\",\"gaid\":\"d7578036d13336cc\",\"brand\":\"google\",\"model\":\"" + randomBrandModel() + "\",\"system_language\":\"en\",\"net\":\"NETWORK_WIFI\",\"region\":\"IN\",\"timezone\":\"Asia/Calcutta\",\"sp_code\":\"\"}"), TuplesKt.to("x-client-status", "0"), TuplesKt.to("x-play-mode", "2")});
                        Map mapOf2 = MapsKt.mapOf(new Pair[]{TuplesKt.to("user-agent", "com.community.mbox.in/50020042 (Linux; U; Android 16; en_IN; sdk_gphone64_x86_64; Build/BP22.250325.006; Cronet/133.0.6876.3)"), TuplesKt.to("accept", "application/json"), TuplesKt.to("content-type", "application/json"), TuplesKt.to("connection", "keep-alive"), TuplesKt.to("x-client-token", generateXClientToken$default), TuplesKt.to("x-tr-signature", generateXTrSignature$default2), TuplesKt.to("x-client-info", "{\"package_name\":\"com.community.mbox.in\",\"version_name\":\"3.0.03.0529.03\",\"version_code\":50020042,\"os\":\"android\",\"os_version\":\"16\",\"device_id\":\"" + this.deviceId + "\",\"install_store\":\"ps\",\"gaid\":\"d7578036d13336cc\",\"brand\":\"google\",\"model\":\"sdk_gphone64_x86_64\",\"system_language\":\"en\",\"net\":\"NETWORK_WIFI\",\"region\":\"IN\",\"timezone\":\"Asia/Calcutta\",\"sp_code\":\"\"}"), TuplesKt.to("x-client-status", "0")});
                        str2 = str23;
                        RequestBody create = RequestBody.Companion.create(str2, MediaType.Companion.get("application/json"));
                        if (StringsKt.contains$default(mainPageRequest.getData(), "|", false, 2, (Object) null)) {
                            Requests app = MainActivityKt.getApp();
                            mainPageRequest3 = mainPageRequest;
                            movieBoxProvider$getMainPage$12.L$0 = mainPageRequest3;
                            movieBoxProvider$getMainPage$12.L$1 = SpillingKt.nullOutSpilledVariable(str16);
                            movieBoxProvider$getMainPage$12.L$2 = SpillingKt.nullOutSpilledVariable(str15);
                            movieBoxProvider$getMainPage$12.L$3 = SpillingKt.nullOutSpilledVariable(split$default);
                            movieBoxProvider$getMainPage$12.L$4 = SpillingKt.nullOutSpilledVariable(str8);
                            movieBoxProvider$getMainPage$12.L$5 = SpillingKt.nullOutSpilledVariable(linkedHashMap);
                            movieBoxProvider$getMainPage$12.L$6 = SpillingKt.nullOutSpilledVariable(str24);
                            movieBoxProvider$getMainPage$12.L$7 = SpillingKt.nullOutSpilledVariable(str28);
                            movieBoxProvider$getMainPage$12.L$8 = SpillingKt.nullOutSpilledVariable(str26);
                            movieBoxProvider$getMainPage$12.L$9 = SpillingKt.nullOutSpilledVariable(str27);
                            movieBoxProvider$getMainPage$12.L$10 = SpillingKt.nullOutSpilledVariable(str25);
                            movieBoxProvider$getMainPage$12.L$11 = SpillingKt.nullOutSpilledVariable(str2);
                            movieBoxProvider$getMainPage$12.L$12 = SpillingKt.nullOutSpilledVariable(generateXClientToken$default);
                            movieBoxProvider$getMainPage$12.L$13 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                            movieBoxProvider$getMainPage$12.L$14 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default2);
                            movieBoxProvider$getMainPage$12.L$15 = SpillingKt.nullOutSpilledVariable(mapOf);
                            movieBoxProvider$getMainPage$12.L$16 = SpillingKt.nullOutSpilledVariable(mapOf2);
                            movieBoxProvider$getMainPage$12.L$17 = SpillingKt.nullOutSpilledVariable(create);
                            movieBoxProvider$getMainPage$12.I$0 = i;
                            movieBoxProvider$getMainPage$12.I$1 = 15;
                            movieBoxProvider$getMainPage$12.I$2 = i6;
                            movieBoxProvider$getMainPage$12.label = 1;
                            obj = obj3;
                            Object post$default = Requests.post$default(app, str16, mapOf, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, create, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$getMainPage$12, 65276, (Object) null);
                            movieBoxProvider$getMainPage$12 = movieBoxProvider$getMainPage$12;
                            if (post$default == obj5) {
                                return obj5;
                            }
                            i3 = 15;
                            requestBody = create;
                            str6 = str16;
                            obj3 = post$default;
                            niceResponse = (NiceResponse) obj3;
                            i4 = i3;
                            mainPageRequest4 = mainPageRequest3;
                            try {
                                String str29 = str;
                                readTree = ExtensionsKt.jacksonObjectMapper().readTree(niceResponse.getBody().string());
                                try {
                                    jsonNode = readTree.get(str29);
                                    if (jsonNode == null) {
                                        mainPageRequest5 = mainPageRequest4;
                                        try {
                                            jsonNode3 = jsonNode.get("items");
                                            break;
                                        } catch (Exception e) {
                                            arrayList = null;
                                            if (arrayList == null) {
                                            }
                                            return MainAPIKt.newHomePageResponse$default(CollectionsKt.listOf(new HomePageList(mainPageRequest5.getName(), arrayList, false, 4, (DefaultConstructorMarker) null)), (Boolean) null, 2, (Object) null);
                                        }
                                    } else {
                                        mainPageRequest5 = mainPageRequest4;
                                    }
                                } catch (Exception e2) {
                                    mainPageRequest5 = mainPageRequest4;
                                }
                            } catch (Exception e3) {
                                mainPageRequest5 = mainPageRequest4;
                            }
                            try {
                                JsonNode jsonNode6 = readTree.get(str29);
                                jsonNode2 = jsonNode6 == null ? jsonNode6.get("subjects") : null;
                            } catch (Exception e4) {
                                arrayList = null;
                                if (arrayList == null) {
                                }
                                return MainAPIKt.newHomePageResponse$default(CollectionsKt.listOf(new HomePageList(mainPageRequest5.getName(), arrayList, false, 4, (DefaultConstructorMarker) null)), (Boolean) null, 2, (Object) null);
                            }
                            if (jsonNode2 != null) {
                                return MainAPIKt.newHomePageResponse$default(CollectionsKt.emptyList(), (Boolean) null, 2, (Object) null);
                            }
                            jsonNode3 = jsonNode2;
                            Iterable<JsonNode> iterable = (Iterable) jsonNode3;
                            boolean z2 = false;
                            ArrayList arrayList3 = new ArrayList();
                            for (JsonNode jsonNode7 : iterable) {
                                JsonNode jsonNode8 = readTree;
                                Iterable iterable2 = iterable;
                                JsonNode jsonNode9 = jsonNode7.get("title");
                                if (jsonNode9 == null || (asText = jsonNode9.asText()) == null) {
                                    jsonNode4 = jsonNode3;
                                    z = z2;
                                    i5 = i4;
                                } else {
                                    jsonNode4 = jsonNode3;
                                    z = z2;
                                    i5 = i4;
                                    try {
                                        String substringBefore$default = StringsKt.substringBefore$default(asText, "[", (String) null, 2, (Object) null);
                                        if (substringBefore$default != null) {
                                            JsonNode jsonNode10 = jsonNode7.get("subjectId");
                                            if (jsonNode10 != null && (asText2 = jsonNode10.asText()) != null) {
                                                JsonNode jsonNode11 = jsonNode7.get("cover");
                                                String asText3 = (jsonNode11 == null || (jsonNode5 = jsonNode11.get("url")) == null) ? null : jsonNode5.asText();
                                                JsonNode jsonNode12 = jsonNode7.get("subjectType");
                                                switch (jsonNode12 != null ? jsonNode12.asInt() : 1) {
                                                    case 1:
                                                        tvType = TvType.Movie;
                                                        break;
                                                    case 2:
                                                        tvType = TvType.TvSeries;
                                                        break;
                                                    default:
                                                        tvType = TvType.Movie;
                                                        break;
                                                }
                                                movieSearchResponse = MainAPIKt.newMovieSearchResponse$default(this, substringBefore$default, asText2, tvType, false, new MovieBoxProvider$$ExternalSyntheticLambda0(asText3, jsonNode7), 8, (Object) null);
                                                if (movieSearchResponse != null) {
                                                    arrayList2 = arrayList3;
                                                    arrayList2.add(movieSearchResponse);
                                                } else {
                                                    arrayList2 = arrayList3;
                                                }
                                                arrayList3 = arrayList2;
                                                jsonNode3 = jsonNode4;
                                                readTree = jsonNode8;
                                                iterable = iterable2;
                                                z2 = z;
                                                i4 = i5;
                                            }
                                            movieSearchResponse = null;
                                            if (movieSearchResponse != null) {
                                            }
                                            arrayList3 = arrayList2;
                                            jsonNode3 = jsonNode4;
                                            readTree = jsonNode8;
                                            iterable = iterable2;
                                            z2 = z;
                                            i4 = i5;
                                        }
                                    } catch (Exception e5) {
                                        arrayList = null;
                                        if (arrayList == null) {
                                        }
                                        return MainAPIKt.newHomePageResponse$default(CollectionsKt.listOf(new HomePageList(mainPageRequest5.getName(), arrayList, false, 4, (DefaultConstructorMarker) null)), (Boolean) null, 2, (Object) null);
                                    }
                                }
                                movieSearchResponse = null;
                                if (movieSearchResponse != null) {
                                }
                                arrayList3 = arrayList2;
                                jsonNode3 = jsonNode4;
                                readTree = jsonNode8;
                                iterable = iterable2;
                                z2 = z;
                                i4 = i5;
                            }
                            arrayList = arrayList3;
                            if (arrayList == null) {
                                arrayList = CollectionsKt.emptyList();
                            }
                            return MainAPIKt.newHomePageResponse$default(CollectionsKt.listOf(new HomePageList(mainPageRequest5.getName(), arrayList, false, 4, (DefaultConstructorMarker) null)), (Boolean) null, 2, (Object) null);
                        }
                        mainPageRequest2 = mainPageRequest;
                        obj = obj3;
                        str3 = str16;
                        Requests app2 = MainActivityKt.getApp();
                        movieBoxProvider$getMainPage$12.L$0 = mainPageRequest2;
                        movieBoxProvider$getMainPage$12.L$1 = SpillingKt.nullOutSpilledVariable(str3);
                        movieBoxProvider$getMainPage$12.L$2 = SpillingKt.nullOutSpilledVariable(str15);
                        movieBoxProvider$getMainPage$12.L$3 = SpillingKt.nullOutSpilledVariable(split$default);
                        movieBoxProvider$getMainPage$12.L$4 = SpillingKt.nullOutSpilledVariable(str8);
                        movieBoxProvider$getMainPage$12.L$5 = SpillingKt.nullOutSpilledVariable(linkedHashMap);
                        movieBoxProvider$getMainPage$12.L$6 = SpillingKt.nullOutSpilledVariable(str24);
                        movieBoxProvider$getMainPage$12.L$7 = SpillingKt.nullOutSpilledVariable(str28);
                        movieBoxProvider$getMainPage$12.L$8 = SpillingKt.nullOutSpilledVariable(str26);
                        movieBoxProvider$getMainPage$12.L$9 = SpillingKt.nullOutSpilledVariable(str27);
                        movieBoxProvider$getMainPage$12.L$10 = SpillingKt.nullOutSpilledVariable(str25);
                        movieBoxProvider$getMainPage$12.L$11 = SpillingKt.nullOutSpilledVariable(str2);
                        movieBoxProvider$getMainPage$12.L$12 = SpillingKt.nullOutSpilledVariable(generateXClientToken$default);
                        movieBoxProvider$getMainPage$12.L$13 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProvider$getMainPage$12.L$14 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default2);
                        movieBoxProvider$getMainPage$12.L$15 = SpillingKt.nullOutSpilledVariable(mapOf);
                        movieBoxProvider$getMainPage$12.L$16 = SpillingKt.nullOutSpilledVariable(mapOf2);
                        movieBoxProvider$getMainPage$12.L$17 = SpillingKt.nullOutSpilledVariable(create);
                        movieBoxProvider$getMainPage$12.I$0 = i;
                        movieBoxProvider$getMainPage$12.I$1 = 15;
                        movieBoxProvider$getMainPage$12.I$2 = i6;
                        movieBoxProvider$getMainPage$12.label = 2;
                        Object obj6 = Requests.get$default(app2, str3, mapOf2, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$getMainPage$12, 4092, (Object) null);
                        if (obj6 == obj5) {
                            return obj5;
                        }
                        i2 = 15;
                        obj2 = obj6;
                        str4 = generateXClientToken$default;
                        map = mapOf2;
                        map2 = mapOf;
                        str5 = generateXTrSignature$default;
                        niceResponse = (NiceResponse) obj2;
                        i4 = i2;
                        mainPageRequest4 = mainPageRequest2;
                        String str292 = str;
                        readTree = ExtensionsKt.jacksonObjectMapper().readTree(niceResponse.getBody().string());
                        jsonNode = readTree.get(str292);
                        if (jsonNode == null) {
                        }
                        JsonNode jsonNode62 = readTree.get(str292);
                        if (jsonNode62 == null) {
                        }
                        if (jsonNode2 != null) {
                        }
                        break;
                    case 1:
                        int i7 = movieBoxProvider$getMainPage$12.I$2;
                        i3 = movieBoxProvider$getMainPage$12.I$1;
                        int i8 = movieBoxProvider$getMainPage$12.I$0;
                        requestBody = (RequestBody) movieBoxProvider$getMainPage$12.L$17;
                        str6 = (String) movieBoxProvider$getMainPage$12.L$1;
                        mainPageRequest3 = (MainPageRequest) movieBoxProvider$getMainPage$12.L$0;
                        ResultKt.throwOnFailure(obj3);
                        str = "data";
                        obj = obj3;
                        niceResponse = (NiceResponse) obj3;
                        i4 = i3;
                        mainPageRequest4 = mainPageRequest3;
                        String str2922 = str;
                        readTree = ExtensionsKt.jacksonObjectMapper().readTree(niceResponse.getBody().string());
                        jsonNode = readTree.get(str2922);
                        if (jsonNode == null) {
                        }
                        JsonNode jsonNode622 = readTree.get(str2922);
                        if (jsonNode622 == null) {
                        }
                        if (jsonNode2 != null) {
                        }
                        break;
                    case 2:
                        int i9 = movieBoxProvider$getMainPage$12.I$2;
                        i2 = movieBoxProvider$getMainPage$12.I$1;
                        int i10 = movieBoxProvider$getMainPage$12.I$0;
                        map = (Map) movieBoxProvider$getMainPage$12.L$16;
                        map2 = (Map) movieBoxProvider$getMainPage$12.L$15;
                        str5 = (String) movieBoxProvider$getMainPage$12.L$13;
                        str4 = (String) movieBoxProvider$getMainPage$12.L$12;
                        str2 = (String) movieBoxProvider$getMainPage$12.L$11;
                        String str30 = (String) movieBoxProvider$getMainPage$12.L$1;
                        mainPageRequest2 = (MainPageRequest) movieBoxProvider$getMainPage$12.L$0;
                        ResultKt.throwOnFailure(obj3);
                        str = "data";
                        obj2 = obj3;
                        str3 = str30;
                        obj = obj2;
                        niceResponse = (NiceResponse) obj2;
                        i4 = i2;
                        mainPageRequest4 = mainPageRequest2;
                        String str29222 = str;
                        readTree = ExtensionsKt.jacksonObjectMapper().readTree(niceResponse.getBody().string());
                        jsonNode = readTree.get(str29222);
                        if (jsonNode == null) {
                        }
                        JsonNode jsonNode6222 = readTree.get(str29222);
                        if (jsonNode6222 == null) {
                        }
                        if (jsonNode2 != null) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        movieBoxProvider$getMainPage$1 = new MovieBoxProvider$getMainPage$1(this, continuation);
        movieBoxProvider$getMainPage$12 = movieBoxProvider$getMainPage$1;
        Object obj32 = movieBoxProvider$getMainPage$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProvider$getMainPage$12.label) {
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

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0040. Please report as an issue. */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:101:0x1588  */
    /* JADX WARN: Removed duplicated region for block: B:107:0x15a3  */
    /* JADX WARN: Removed duplicated region for block: B:109:0x15ad A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:11:0x004b  */
    /* JADX WARN: Removed duplicated region for block: B:122:0x14bd  */
    /* JADX WARN: Removed duplicated region for block: B:123:0x14df  */
    /* JADX WARN: Removed duplicated region for block: B:135:0x146a  */
    /* JADX WARN: Removed duplicated region for block: B:136:0x148e  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0131  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0228  */
    /* JADX WARN: Removed duplicated region for block: B:182:0x1328  */
    /* JADX WARN: Removed duplicated region for block: B:183:0x0364  */
    /* JADX WARN: Removed duplicated region for block: B:186:0x0f8b  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x125b  */
    /* JADX WARN: Removed duplicated region for block: B:192:0x0fa2  */
    /* JADX WARN: Removed duplicated region for block: B:198:0x0fb8  */
    /* JADX WARN: Removed duplicated region for block: B:204:0x0fce  */
    /* JADX WARN: Removed duplicated region for block: B:210:0x0fe4  */
    /* JADX WARN: Removed duplicated region for block: B:215:0x0ff9  */
    /* JADX WARN: Removed duplicated region for block: B:242:0x17a2  */
    /* JADX WARN: Removed duplicated region for block: B:252:0x042d  */
    /* JADX WARN: Removed duplicated region for block: B:259:0x0e96  */
    /* JADX WARN: Removed duplicated region for block: B:263:0x0f5b  */
    /* JADX WARN: Removed duplicated region for block: B:265:0x0500  */
    /* JADX WARN: Removed duplicated region for block: B:268:0x0e55 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:269:0x0e56  */
    /* JADX WARN: Removed duplicated region for block: B:270:0x05c4  */
    /* JADX WARN: Removed duplicated region for block: B:274:0x09bd  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x10a6  */
    /* JADX WARN: Removed duplicated region for block: B:312:0x0a99  */
    /* JADX WARN: Removed duplicated region for block: B:317:0x0ab3  */
    /* JADX WARN: Removed duplicated region for block: B:320:0x0ac5  */
    /* JADX WARN: Removed duplicated region for block: B:330:0x0b70  */
    /* JADX WARN: Removed duplicated region for block: B:333:0x0b79  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x1348  */
    /* JADX WARN: Removed duplicated region for block: B:371:0x0bf9  */
    /* JADX WARN: Removed duplicated region for block: B:380:0x0c61  */
    /* JADX WARN: Removed duplicated region for block: B:390:0x0ccf  */
    /* JADX WARN: Removed duplicated region for block: B:393:0x0ce9  */
    /* JADX WARN: Removed duplicated region for block: B:397:0x0cf8  */
    /* JADX WARN: Removed duplicated region for block: B:400:0x0d82 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:401:0x0d83  */
    /* JADX WARN: Removed duplicated region for block: B:402:0x0d05  */
    /* JADX WARN: Removed duplicated region for block: B:404:0x0cd2  */
    /* JADX WARN: Removed duplicated region for block: B:405:0x0cd5  */
    /* JADX WARN: Removed duplicated region for block: B:406:0x0cd8  */
    /* JADX WARN: Removed duplicated region for block: B:410:0x0cc7  */
    /* JADX WARN: Removed duplicated region for block: B:412:0x0bee  */
    /* JADX WARN: Removed duplicated region for block: B:413:0x0ab8  */
    /* JADX WARN: Removed duplicated region for block: B:415:0x0aa8  */
    /* JADX WARN: Removed duplicated region for block: B:431:0x18e1  */
    /* JADX WARN: Removed duplicated region for block: B:433:0x060a  */
    /* JADX WARN: Removed duplicated region for block: B:440:0x0908  */
    /* JADX WARN: Removed duplicated region for block: B:444:0x09a6  */
    /* JADX WARN: Removed duplicated region for block: B:446:0x0655  */
    /* JADX WARN: Removed duplicated region for block: B:449:0x08a3  */
    /* JADX WARN: Removed duplicated region for block: B:454:0x08e6 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:455:0x08e7  */
    /* JADX WARN: Removed duplicated region for block: B:456:0x069b  */
    /* JADX WARN: Removed duplicated region for block: B:463:0x0810  */
    /* JADX WARN: Removed duplicated region for block: B:466:0x0898 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:468:0x06b3  */
    /* JADX WARN: Removed duplicated region for block: B:57:0x141d A[LOOP:2: B:41:0x13db->B:57:0x141d, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:58:0x141a A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:80:0x1501  */
    /* JADX WARN: Removed duplicated region for block: B:82:0x1505  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x1510  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0043  */
    /* JADX WARN: Type inference failed for: r43v13, types: [java.lang.Object] */
    /* JADX WARN: Type inference failed for: r6v87, types: [java.util.Map] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:33:0x1213 -> B:16:0x124e). Please report as a decompilation issue!!! */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object load(@NotNull String str, @NotNull Continuation<? super LoadResponse> continuation) {
        MovieBoxProvider$load$1 movieBoxProvider$load$1;
        MovieBoxProvider$load$1 movieBoxProvider$load$12;
        String substringAfterLast$default;
        int i;
        Long l;
        Object fetchAnonymousToken$default;
        String str2;
        String str3;
        String str4;
        List groupValues;
        String str5;
        String str6;
        String generateXTrSignature$default;
        String str7;
        String str8;
        String str9;
        Object obj;
        String str10;
        String str11;
        String str12;
        Object obj2;
        Object obj3;
        Object obj4;
        int i2;
        Object obj5;
        Map map;
        String str13;
        MovieBoxProvider$load$1 movieBoxProvider$load$13;
        NiceResponse niceResponse;
        Object fetchAnonymousToken;
        String str14;
        Object obj6;
        NiceResponse niceResponse2;
        String str15;
        String str16;
        String str17;
        Map map2;
        MovieBoxProvider$load$1 movieBoxProvider$load$14;
        String str18;
        String str19;
        NiceResponse niceResponse3;
        String str20;
        Object obj7;
        String str21;
        String str22;
        Map map3;
        Object obj8;
        String str23;
        ObjectMapper jacksonObjectMapper;
        NiceResponse niceResponse4;
        String str24;
        String str25;
        String str26;
        String asText;
        String str27;
        String str28;
        Integer num;
        JsonNode jsonNode;
        Integer num2;
        JsonNode jsonNode2;
        String str29;
        String str30;
        Object obj9;
        Iterable iterable;
        Object obj10;
        int i3;
        ArrayList emptyList;
        List list;
        String str31;
        ArrayList emptyList2;
        List list2;
        String str32;
        Integer num3;
        TvType tvType;
        String str33;
        String str34;
        Double d;
        Object identifyID;
        Object obj11;
        Object obj12;
        JsonNode jsonNode3;
        TvType tvType2;
        String str35;
        String str36;
        Object obj13;
        Integer num4;
        String str37;
        String str38;
        String str39;
        int i4;
        String str40;
        List list3;
        String str41;
        Integer num5;
        String str42;
        Integer num6;
        List list4;
        String take;
        List split$default;
        int i5;
        ActorData actorData;
        String asText2;
        ArrayList arrayList;
        JsonNode jsonNode4;
        String asText3;
        Double doubleOrNull;
        MovieBoxProvider$load$1 movieBoxProvider$load$15;
        Integer num7;
        Object obj14;
        Object fetchTmdbLogoUrl;
        TvType tvType3;
        String str43;
        String str44;
        String str45;
        Integer num8;
        String str46;
        Object obj15;
        List list5;
        Integer num9;
        JsonNode jsonNode5;
        List list6;
        String str47;
        int i6;
        String str48;
        Object obj16;
        String str49;
        String str50;
        Object obj17;
        String str51;
        List list7;
        List list8;
        String str52;
        Integer num10;
        Integer num11;
        String str53;
        int i7;
        String str54;
        String str55;
        Integer num12;
        TvType tvType4;
        String str56;
        JsonNode jsonNode6;
        String str57;
        String str58;
        JsonNode jsonNode7;
        String str59;
        Integer num13;
        String str60;
        String str61;
        Object fetchMetaData;
        String str62;
        String str63;
        String str64;
        Integer num14;
        int i8;
        List list9;
        Integer num15;
        JsonNode jsonNode8;
        String str65;
        List list10;
        TvType tvType5;
        String str66;
        String str67;
        Integer num16;
        String str68;
        String str69;
        String str70;
        Collection collection;
        String str71;
        MovieBoxProvider$load$1 movieBoxProvider$load$16;
        JsonNode jsonNode9;
        String str72;
        String str73;
        String str74;
        String str75;
        String str76;
        String str77;
        String str78;
        Integer num17;
        List list11;
        List list12;
        String str79;
        Integer num18;
        Integer num19;
        String str80;
        Integer num20;
        LinkedHashMap linkedHashMap;
        Iterator it;
        String str81;
        TvType tvType6;
        int i9;
        List list13;
        MovieBoxProvider movieBoxProvider;
        String str82;
        JsonNode jsonNode10;
        Object obj18;
        Continuation<? super LoadResponse> continuation2;
        JsonNode jsonNode11;
        JsonNode jsonNode12;
        String asText4;
        JsonNode jsonNode13;
        String asText5;
        JsonNode jsonNode14;
        String asText6;
        Iterable iterable2;
        Integer num21;
        boolean z;
        JsonNode jsonNode15;
        String str83;
        String str84;
        String str85;
        List list14;
        String str86;
        String str87;
        String str88;
        String str89;
        String str90;
        List list15;
        String str91;
        Integer num22;
        String str92;
        JsonNode jsonNode16;
        JsonNode jsonNode17;
        JsonNode jsonNode18;
        String asText7;
        JsonNode jsonNode19;
        JsonNode jsonNode20;
        String asText8;
        boolean z2;
        LinkedHashMap linkedHashMap2;
        Object obj19;
        String str93;
        Map map4;
        JsonNode jsonNode21;
        String str94;
        Iterator it2;
        Map map5;
        int i10;
        MovieBoxProvider movieBoxProvider2;
        Continuation<? super LoadResponse> continuation3;
        NiceResponse niceResponse5;
        Continuation<? super LoadResponse> continuation4;
        String str95;
        MovieBoxProvider movieBoxProvider3;
        JsonNode jsonNode22;
        Map map6;
        Object obj20;
        int i11;
        if (continuation instanceof MovieBoxProvider$load$1) {
            movieBoxProvider$load$1 = (MovieBoxProvider$load$1) continuation;
            if ((movieBoxProvider$load$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProvider$load$1.label -= Integer.MIN_VALUE;
                movieBoxProvider$load$12 = movieBoxProvider$load$1;
                Object obj21 = movieBoxProvider$load$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                String str96 = "title";
                String str97 = "data";
                switch (movieBoxProvider$load$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj21);
                        MatchResult find$default = Regex.find$default(new Regex("subjectId=([^&]+)"), str, 0, 2, (Object) null);
                        if (find$default == null || (groupValues = find$default.getGroupValues()) == null || (substringAfterLast$default = (String) groupValues.get(1)) == null) {
                            substringAfterLast$default = StringsKt.substringAfterLast$default(str, '/', (String) null, 2, (Object) null);
                        }
                        String str98 = getMainUrl() + "/wefeed-mobile-bff/subject-api/get?subjectId=" + substringAfterLast$default;
                        movieBoxProvider$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        movieBoxProvider$load$12.L$1 = substringAfterLast$default;
                        movieBoxProvider$load$12.L$2 = str98;
                        i = 1;
                        movieBoxProvider$load$12.label = 1;
                        l = null;
                        fetchAnonymousToken$default = fetchAnonymousToken$default(this, false, movieBoxProvider$load$12, 1, null);
                        if (fetchAnonymousToken$default == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        str2 = substringAfterLast$default;
                        str3 = str98;
                        str4 = str;
                        String generateXClientToken$default = generateXClientToken$default(this, l, i, l);
                        String str99 = str3;
                        String str100 = str2;
                        str5 = str97;
                        str6 = (String) fetchAnonymousToken$default;
                        generateXTrSignature$default = generateXTrSignature$default(this, "GET", "application/json", "application/json", str99, null, false, null, 112, null);
                        Map mutableMapOf = MapsKt.mutableMapOf(new Pair[]{TuplesKt.to("user-agent", "com.community.mbox.in/50020042 (Linux; U; Android 16; en_IN; " + randomBrandModel() + "; Build/BP22.250325.006; Cronet/133.0.6876.3)"), TuplesKt.to("accept", "application/json"), TuplesKt.to("content-type", "application/json"), TuplesKt.to("connection", "keep-alive"), TuplesKt.to("x-client-token", generateXClientToken$default), TuplesKt.to("x-tr-signature", generateXTrSignature$default), TuplesKt.to("x-client-info", "{\"package_name\":\"com.community.mbox.in\",\"version_name\":\"3.0.03.0529.03\",\"version_code\":50020042,\"os\":\"android\",\"os_version\":\"16\",\"device_id\":\"" + this.deviceId + "\",\"install_store\":\"ps\",\"gaid\":\"d7578036d13336cc\",\"brand\":\"google\",\"model\":\"" + randomBrandModel() + "\",\"system_language\":\"en\",\"net\":\"NETWORK_WIFI\",\"region\":\"IN\",\"timezone\":\"Asia/Calcutta\",\"sp_code\":\"\"}"), TuplesKt.to("x-client-status", "0"), TuplesKt.to("x-play-mode", "2")});
                        str7 = str6;
                        if (!(str7 != null || StringsKt.isBlank(str7))) {
                            mutableMapOf.put("Authorization", "Bearer " + str6);
                        }
                        Requests app = MainActivityKt.getApp();
                        movieBoxProvider$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                        str8 = str100;
                        movieBoxProvider$load$12.L$1 = str8;
                        movieBoxProvider$load$12.L$2 = str99;
                        movieBoxProvider$load$12.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                        movieBoxProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(generateXClientToken$default);
                        movieBoxProvider$load$12.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProvider$load$12.L$6 = mutableMapOf;
                        movieBoxProvider$load$12.label = 2;
                        str9 = "description";
                        obj = obj21;
                        str10 = str96;
                        str11 = generateXClientToken$default;
                        str12 = "Bearer ";
                        obj2 = "x-tr-signature";
                        obj3 = "Authorization";
                        obj4 = coroutine_suspended;
                        i2 = 3;
                        obj5 = Requests.get$default(app, str99, mutableMapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$load$12, 4092, (Object) null);
                        map = mutableMapOf;
                        str13 = str99;
                        movieBoxProvider$load$13 = movieBoxProvider$load$12;
                        if (obj5 == obj4) {
                            return obj4;
                        }
                        niceResponse = (NiceResponse) obj5;
                        if (niceResponse.getCode() != 441 || niceResponse.getCode() == 401) {
                            movieBoxProvider$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                            movieBoxProvider$load$13.L$1 = str8;
                            movieBoxProvider$load$13.L$2 = str13;
                            movieBoxProvider$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                            movieBoxProvider$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                            movieBoxProvider$load$13.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                            movieBoxProvider$load$13.L$6 = map;
                            movieBoxProvider$load$13.L$7 = niceResponse;
                            movieBoxProvider$load$13.label = i2;
                            fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProvider$load$13);
                            if (fetchAnonymousToken == obj4) {
                                return obj4;
                            }
                            str14 = str8;
                            obj6 = fetchAnonymousToken;
                            niceResponse2 = niceResponse;
                            str15 = str11;
                            str16 = generateXTrSignature$default;
                            str17 = str13;
                            map2 = map;
                            String str101 = (String) obj6;
                            str21 = str101;
                            if (str21 != null || StringsKt.isBlank(str21)) {
                                map2.put(obj3, str12 + str101);
                                Object obj22 = obj4;
                                String str102 = str17;
                                String generateXTrSignature$default2 = generateXTrSignature$default(this, "GET", "application/json", "application/json", str102, null, false, null, 112, null);
                                obj7 = obj2;
                                map2.put(obj7, generateXTrSignature$default2);
                                Requests app2 = MainActivityKt.getApp();
                                movieBoxProvider$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                                movieBoxProvider$load$13.L$1 = str14;
                                movieBoxProvider$load$13.L$2 = str102;
                                movieBoxProvider$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str101);
                                movieBoxProvider$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                movieBoxProvider$load$13.L$5 = SpillingKt.nullOutSpilledVariable(str16);
                                movieBoxProvider$load$13.L$6 = map2;
                                movieBoxProvider$load$13.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                                movieBoxProvider$load$13.L$8 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default2);
                                movieBoxProvider$load$13.label = 4;
                                str9 = str9;
                                MovieBoxProvider$load$1 movieBoxProvider$load$17 = movieBoxProvider$load$13;
                                str20 = str10;
                                str18 = str102;
                                String str103 = str14;
                                obj4 = obj22;
                                Object obj23 = Requests.get$default(app2, str18, map2, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$load$17, 4092, (Object) null);
                                movieBoxProvider$load$14 = movieBoxProvider$load$17;
                                if (obj23 == obj4) {
                                    return obj4;
                                }
                                Map map7 = map2;
                                str22 = str103;
                                map3 = map7;
                                obj8 = obj23;
                                str23 = str101;
                                generateXTrSignature$default = str16;
                                String str104 = str22;
                                map2 = map3;
                                str19 = str104;
                                niceResponse3 = (NiceResponse) obj8;
                                str6 = str23;
                                if (niceResponse3.getCode() == 200) {
                                    throw new ErrorLoadingException("Failed to load data: " + niceResponse3.getBody().string());
                                }
                                String string = niceResponse3.getBody().string();
                                jacksonObjectMapper = ExtensionsKt.jacksonObjectMapper();
                                JsonNode readTree = jacksonObjectMapper.readTree(string);
                                String str105 = str5;
                                JsonNode jsonNode23 = readTree.get(str105);
                                if (jsonNode23 == null) {
                                    throw new ErrorLoadingException("No data");
                                }
                                JsonNode jsonNode24 = jsonNode23.get(str20);
                                if (jsonNode24 != null) {
                                    niceResponse4 = niceResponse3;
                                    String asText9 = jsonNode24.asText();
                                    if (asText9 != null) {
                                        str24 = generateXTrSignature$default;
                                        str96 = str20;
                                        str25 = str6;
                                        String substringBefore$default = StringsKt.substringBefore$default(asText9, "[", (String) null, 2, (Object) null);
                                        if (substringBefore$default != null) {
                                            JsonNode jsonNode25 = jsonNode23.get(str9);
                                            String asText10 = jsonNode25 != null ? jsonNode25.asText() : null;
                                            JsonNode jsonNode26 = jsonNode23.get("releaseDate");
                                            String asText11 = jsonNode26 != null ? jsonNode26.asText() : null;
                                            JsonNode jsonNode27 = jsonNode23.get("duration");
                                            String asText12 = jsonNode27 != null ? jsonNode27.asText() : null;
                                            str26 = str105;
                                            JsonNode jsonNode28 = jsonNode23.get("genre");
                                            asText = jsonNode28 != null ? jsonNode28.asText() : null;
                                            JsonNode jsonNode29 = jsonNode23.get("imdbRatingValue");
                                            if (jsonNode29 == null || (asText3 = jsonNode29.asText()) == null || (doubleOrNull = StringsKt.toDoubleOrNull(asText3)) == null) {
                                                str27 = asText12;
                                                str28 = string;
                                                num = null;
                                            } else {
                                                str27 = asText12;
                                                str28 = string;
                                                num = Boxing.boxInt((int) (10 * doubleOrNull.doubleValue()));
                                            }
                                            if (asText11 != null) {
                                                jsonNode = readTree;
                                                String substring = asText11.substring(0, 4);
                                                Intrinsics.checkNotNullExpressionValue(substring, "substring(...)");
                                                if (substring != null) {
                                                    num2 = StringsKt.toIntOrNull(substring);
                                                    JsonNode jsonNode30 = jsonNode23.get("cover");
                                                    String asText13 = (jsonNode30 != null || (jsonNode4 = jsonNode30.get("url")) == null) ? null : jsonNode4.asText();
                                                    jsonNode2 = jsonNode23.get("cover");
                                                    if (jsonNode2 == null) {
                                                        str29 = str9;
                                                        JsonNode jsonNode31 = jsonNode2.get("url");
                                                        if (jsonNode31 != null) {
                                                            str30 = jsonNode31.asText();
                                                            JsonNode jsonNode32 = jsonNode23.get("subjectType");
                                                            int asInt = jsonNode32 != null ? jsonNode32.asInt() : 1;
                                                            obj9 = obj7;
                                                            iterable = jsonNode23.get("staffList");
                                                            if (iterable != null) {
                                                                Iterable<JsonNode> iterable3 = iterable;
                                                                ArrayList arrayList2 = new ArrayList();
                                                                for (JsonNode jsonNode33 : iterable3) {
                                                                    Iterable iterable4 = iterable3;
                                                                    Object obj24 = obj4;
                                                                    JsonNode jsonNode34 = jsonNode33.get("staffType");
                                                                    Integer boxInt = jsonNode34 != null ? Boxing.boxInt(jsonNode34.asInt()) : null;
                                                                    if (boxInt == null) {
                                                                        i5 = asInt;
                                                                    } else {
                                                                        i5 = asInt;
                                                                        if (boxInt.intValue() == 1) {
                                                                            JsonNode jsonNode35 = jsonNode33.get("name");
                                                                            if (jsonNode35 != null && (asText2 = jsonNode35.asText()) != null) {
                                                                                JsonNode jsonNode36 = jsonNode33.get("character");
                                                                                String asText14 = jsonNode36 != null ? jsonNode36.asText() : null;
                                                                                JsonNode jsonNode37 = jsonNode33.get("avatarUrl");
                                                                                actorData = new ActorData(new Actor(asText2, jsonNode37 != null ? jsonNode37.asText() : null), (ActorRole) null, asText14, (Actor) null, 10, (DefaultConstructorMarker) null);
                                                                                if (actorData != null) {
                                                                                    arrayList = arrayList2;
                                                                                    arrayList.add(actorData);
                                                                                } else {
                                                                                    arrayList = arrayList2;
                                                                                }
                                                                                arrayList2 = arrayList;
                                                                                iterable3 = iterable4;
                                                                                obj4 = obj24;
                                                                                asInt = i5;
                                                                            }
                                                                            actorData = null;
                                                                            if (actorData != null) {
                                                                            }
                                                                            arrayList2 = arrayList;
                                                                            iterable3 = iterable4;
                                                                            obj4 = obj24;
                                                                            asInt = i5;
                                                                        }
                                                                    }
                                                                    actorData = null;
                                                                    if (actorData != null) {
                                                                    }
                                                                    arrayList2 = arrayList;
                                                                    iterable3 = iterable4;
                                                                    obj4 = obj24;
                                                                    asInt = i5;
                                                                }
                                                                obj10 = obj4;
                                                                i3 = asInt;
                                                                ArrayList arrayList3 = arrayList2;
                                                                boolean z3 = false;
                                                                HashSet hashSet = new HashSet();
                                                                ArrayList arrayList4 = new ArrayList();
                                                                for (Object obj25 : arrayList3) {
                                                                    ArrayList arrayList5 = arrayList3;
                                                                    boolean z4 = z3;
                                                                    if (hashSet.add(((ActorData) obj25).getActor().getName())) {
                                                                        arrayList4.add(obj25);
                                                                        arrayList3 = arrayList5;
                                                                        z3 = z4;
                                                                    } else {
                                                                        arrayList3 = arrayList5;
                                                                        z3 = z4;
                                                                    }
                                                                }
                                                                emptyList = arrayList4;
                                                            } else {
                                                                obj10 = obj4;
                                                                i3 = asInt;
                                                                emptyList = CollectionsKt.emptyList();
                                                            }
                                                            if (asText != null || (split$default = StringsKt.split$default(asText, new String[]{","}, false, 0, 6, (Object) null)) == null) {
                                                                list = emptyList;
                                                                str31 = str30;
                                                                emptyList2 = CollectionsKt.emptyList();
                                                            } else {
                                                                List list16 = split$default;
                                                                list = emptyList;
                                                                str31 = str30;
                                                                ArrayList arrayList6 = new ArrayList(CollectionsKt.collectionSizeOrDefault(list16, 10));
                                                                Iterator it3 = list16.iterator();
                                                                while (it3.hasNext()) {
                                                                    arrayList6.add(StringsKt.trim((String) it3.next()).toString());
                                                                }
                                                                emptyList2 = arrayList6;
                                                            }
                                                            if (str27 != null) {
                                                                String str106 = str27;
                                                                list2 = emptyList2;
                                                                str32 = asText13;
                                                                MatchResult find$default2 = Regex.find$default(new Regex("(\\d+)h\\s*(\\d+)m"), str106, 0, 2, (Object) null);
                                                                if (find$default2 != null) {
                                                                    Integer intOrNull = StringsKt.toIntOrNull((String) find$default2.getGroupValues().get(1));
                                                                    int intValue = intOrNull != null ? intOrNull.intValue() : 0;
                                                                    Integer intOrNull2 = StringsKt.toIntOrNull((String) find$default2.getGroupValues().get(2));
                                                                    num3 = Boxing.boxInt((intValue * 60) + (intOrNull2 != null ? intOrNull2.intValue() : 0));
                                                                } else {
                                                                    num3 = StringsKt.toIntOrNull(StringsKt.replace$default(str106, "m", "", false, 4, (Object) null));
                                                                }
                                                            } else {
                                                                list2 = emptyList2;
                                                                str32 = asText13;
                                                                num3 = null;
                                                            }
                                                            switch (i3) {
                                                                case 1:
                                                                    tvType = TvType.Movie;
                                                                    break;
                                                                case 2:
                                                                    tvType = TvType.TvSeries;
                                                                    break;
                                                                case 7:
                                                                    tvType = TvType.TvSeries;
                                                                    break;
                                                                default:
                                                                    tvType = TvType.Movie;
                                                                    break;
                                                            }
                                                            String substringBefore$default2 = StringsKt.substringBefore$default(StringsKt.substringBefore$default(substringBefore$default, "(", (String) null, 2, (Object) null), "[", (String) null, 2, (Object) null);
                                                            Integer intOrNull3 = (asText11 != null || (take = StringsKt.take(asText11, 4)) == null) ? null : StringsKt.toIntOrNull(take);
                                                            if (num != null) {
                                                                str33 = substringBefore$default2;
                                                                str34 = asText11;
                                                                d = Boxing.boxDouble(num.intValue());
                                                            } else {
                                                                str33 = substringBefore$default2;
                                                                str34 = asText11;
                                                                d = null;
                                                            }
                                                            movieBoxProvider$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                                                            movieBoxProvider$load$14.L$1 = str19;
                                                            movieBoxProvider$load$14.L$2 = str18;
                                                            movieBoxProvider$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                                            movieBoxProvider$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                            movieBoxProvider$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                            movieBoxProvider$load$14.L$6 = map2;
                                                            movieBoxProvider$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                            movieBoxProvider$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                                                            movieBoxProvider$load$14.L$9 = jacksonObjectMapper;
                                                            movieBoxProvider$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                            movieBoxProvider$load$14.L$11 = jsonNode23;
                                                            movieBoxProvider$load$14.L$12 = substringBefore$default;
                                                            movieBoxProvider$load$14.L$13 = asText10;
                                                            movieBoxProvider$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str34);
                                                            movieBoxProvider$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str27);
                                                            movieBoxProvider$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                                                            movieBoxProvider$load$14.L$17 = num;
                                                            movieBoxProvider$load$14.L$18 = num2;
                                                            String str107 = str32;
                                                            movieBoxProvider$load$14.L$19 = str107;
                                                            movieBoxProvider$load$14.L$20 = str31;
                                                            movieBoxProvider$load$14.L$21 = list;
                                                            List list17 = list2;
                                                            movieBoxProvider$load$14.L$22 = list17;
                                                            movieBoxProvider$load$14.L$23 = num3;
                                                            movieBoxProvider$load$14.L$24 = tvType;
                                                            Integer num23 = num3;
                                                            movieBoxProvider$load$14.I$0 = i3;
                                                            movieBoxProvider$load$14.label = 5;
                                                            identifyID = MovieBoxProviderKt.identifyID(str33, intOrNull3, d, movieBoxProvider$load$14);
                                                            obj11 = obj10;
                                                            if (identifyID == obj11) {
                                                                return obj11;
                                                            }
                                                            obj12 = identifyID;
                                                            jsonNode3 = jsonNode23;
                                                            tvType2 = tvType;
                                                            str35 = str107;
                                                            str36 = substringBefore$default;
                                                            obj13 = obj11;
                                                            num4 = num;
                                                            str37 = str27;
                                                            str38 = str4;
                                                            str39 = str31;
                                                            i4 = i3;
                                                            str40 = asText10;
                                                            list3 = list17;
                                                            str41 = str19;
                                                            num5 = num23;
                                                            str42 = str34;
                                                            num6 = num2;
                                                            list4 = list;
                                                            Pair pair = (Pair) obj12;
                                                            TvType tvType7 = tvType2;
                                                            Integer num24 = (Integer) pair.component1();
                                                            String str108 = (String) pair.component2();
                                                            movieBoxProvider$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str38);
                                                            movieBoxProvider$load$14.L$1 = str41;
                                                            movieBoxProvider$load$14.L$2 = str18;
                                                            movieBoxProvider$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                                            movieBoxProvider$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                            movieBoxProvider$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                            movieBoxProvider$load$14.L$6 = map2;
                                                            movieBoxProvider$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                            movieBoxProvider$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                                                            movieBoxProvider$load$14.L$9 = jacksonObjectMapper;
                                                            movieBoxProvider$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                            movieBoxProvider$load$14.L$11 = jsonNode3;
                                                            movieBoxProvider$load$14.L$12 = str36;
                                                            movieBoxProvider$load$14.L$13 = str40;
                                                            movieBoxProvider$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                            movieBoxProvider$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                            movieBoxProvider$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                                                            movieBoxProvider$load$14.L$17 = num4;
                                                            movieBoxProvider$load$14.L$18 = num6;
                                                            movieBoxProvider$load$14.L$19 = str35;
                                                            movieBoxProvider$load$14.L$20 = str39;
                                                            movieBoxProvider$load$14.L$21 = list4;
                                                            movieBoxProvider$load$14.L$22 = list3;
                                                            movieBoxProvider$load$14.L$23 = num5;
                                                            movieBoxProvider$load$14.L$24 = tvType7;
                                                            JsonNode jsonNode38 = jsonNode3;
                                                            movieBoxProvider$load$14.L$25 = num24;
                                                            movieBoxProvider$load$14.L$26 = str108;
                                                            movieBoxProvider$load$14.I$0 = i4;
                                                            movieBoxProvider$load$14.label = 6;
                                                            Integer num25 = num5;
                                                            List list18 = list4;
                                                            String str109 = str35;
                                                            String str110 = str96;
                                                            List list19 = list3;
                                                            String str111 = str41;
                                                            String str112 = str39;
                                                            String str113 = str36;
                                                            movieBoxProvider$load$15 = movieBoxProvider$load$14;
                                                            String str114 = str26;
                                                            num7 = num4;
                                                            obj14 = obj13;
                                                            fetchTmdbLogoUrl = MovieBoxProviderKt.fetchTmdbLogoUrl("https://api.themoviedb.org/3", "98ae14df2b8d8f8f8136499daf79f0e0", tvType7, num24, "en", movieBoxProvider$load$15);
                                                            if (fetchTmdbLogoUrl != obj14) {
                                                                return obj14;
                                                            }
                                                            str97 = str114;
                                                            tvType3 = tvType7;
                                                            str43 = str113;
                                                            str44 = str28;
                                                            str45 = str108;
                                                            num8 = num24;
                                                            str46 = str109;
                                                            obj15 = obj14;
                                                            list5 = list19;
                                                            num9 = num25;
                                                            jsonNode5 = jsonNode38;
                                                            list6 = list18;
                                                            str96 = str110;
                                                            str47 = asText;
                                                            i6 = i4;
                                                            str48 = str112;
                                                            obj16 = fetchTmdbLogoUrl;
                                                            str49 = str111;
                                                            TvType tvType8 = tvType3;
                                                            String str115 = (String) obj16;
                                                            str50 = str45;
                                                            if (str50 != null || StringsKt.isBlank(str50)) {
                                                                str61 = str115;
                                                                movieBoxProvider$load$15.L$0 = SpillingKt.nullOutSpilledVariable(str38);
                                                                movieBoxProvider$load$15.L$1 = str49;
                                                                movieBoxProvider$load$15.L$2 = str18;
                                                                movieBoxProvider$load$15.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                                                movieBoxProvider$load$15.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                                movieBoxProvider$load$15.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                                movieBoxProvider$load$15.L$6 = map2;
                                                                movieBoxProvider$load$15.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                                movieBoxProvider$load$15.L$8 = SpillingKt.nullOutSpilledVariable(str44);
                                                                movieBoxProvider$load$15.L$9 = jacksonObjectMapper;
                                                                movieBoxProvider$load$15.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                                movieBoxProvider$load$15.L$11 = jsonNode5;
                                                                movieBoxProvider$load$15.L$12 = str43;
                                                                movieBoxProvider$load$15.L$13 = str40;
                                                                movieBoxProvider$load$15.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                                movieBoxProvider$load$15.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                                movieBoxProvider$load$15.L$16 = SpillingKt.nullOutSpilledVariable(str47);
                                                                movieBoxProvider$load$15.L$17 = num7;
                                                                movieBoxProvider$load$15.L$18 = num6;
                                                                movieBoxProvider$load$15.L$19 = str46;
                                                                movieBoxProvider$load$15.L$20 = str48;
                                                                movieBoxProvider$load$15.L$21 = list6;
                                                                movieBoxProvider$load$15.L$22 = list5;
                                                                movieBoxProvider$load$15.L$23 = num9;
                                                                movieBoxProvider$load$15.L$24 = tvType8;
                                                                Integer num26 = num9;
                                                                movieBoxProvider$load$15.L$25 = num8;
                                                                String str116 = str45;
                                                                movieBoxProvider$load$15.L$26 = str116;
                                                                String str117 = str48;
                                                                movieBoxProvider$load$15.L$27 = str61;
                                                                movieBoxProvider$load$15.I$0 = i6;
                                                                movieBoxProvider$load$15.label = 7;
                                                                fetchMetaData = MovieBoxProviderKt.fetchMetaData(str116, tvType8, movieBoxProvider$load$15);
                                                                obj17 = obj15;
                                                                if (fetchMetaData == obj17) {
                                                                    return obj17;
                                                                }
                                                                str62 = str49;
                                                                str63 = str18;
                                                                str64 = str37;
                                                                obj21 = fetchMetaData;
                                                                num14 = num6;
                                                                str53 = str38;
                                                                i8 = i6;
                                                                list9 = list6;
                                                                num15 = num26;
                                                                jsonNode8 = jsonNode;
                                                                str65 = str40;
                                                                list10 = list5;
                                                                tvType5 = tvType8;
                                                                str66 = str46;
                                                                str67 = str116;
                                                                num16 = num7;
                                                                str68 = str117;
                                                                jsonNode7 = (JsonNode) obj21;
                                                                str51 = str66;
                                                                str55 = str67;
                                                                tvType4 = tvType5;
                                                                num12 = num15;
                                                                list7 = list10;
                                                                list8 = list9;
                                                                str54 = str68;
                                                                i7 = i8;
                                                                num10 = num14;
                                                                num11 = num16;
                                                                str52 = str65;
                                                                jsonNode = jsonNode8;
                                                                str56 = str62;
                                                                str37 = str64;
                                                                jsonNode6 = jsonNode5;
                                                                str57 = str43;
                                                                str58 = str63;
                                                                str60 = str25;
                                                                str59 = str61;
                                                                num13 = num8;
                                                                if (jsonNode7 != null || (iterable2 = jsonNode7.get("videos")) == null || (r3 = CollectionsKt.toList(iterable2)) == null) {
                                                                    List emptyList3 = CollectionsKt.emptyList();
                                                                }
                                                                List list20 = emptyList3;
                                                                String str118 = (jsonNode7 != null || (jsonNode14 = jsonNode7.get("poster")) == null || (asText6 = jsonNode14.asText()) == null) ? str51 : asText6;
                                                                String str119 = (jsonNode7 != null || (jsonNode13 = jsonNode7.get("background")) == null || (asText5 = jsonNode13.asText()) == null) ? str54 : asText5;
                                                                String str120 = (jsonNode7 != null || (jsonNode12 = jsonNode7.get("overview")) == null || (asText4 = jsonNode12.asText()) == null) ? str52 : asText4;
                                                                String asText15 = (jsonNode7 != null || (jsonNode11 = jsonNode7.get("imdbRating")) == null) ? null : jsonNode11.asText();
                                                                if (tvType4 != TvType.TvSeries) {
                                                                    MovieBoxProvider$load$7 movieBoxProvider$load$7 = new MovieBoxProvider$load$7(str51, str118, str119, str54, str59, str120, str52, num10, list7, list8, asText15, num11, num12, str55, num13, null);
                                                                    movieBoxProvider$load$15.L$0 = SpillingKt.nullOutSpilledVariable(str53);
                                                                    movieBoxProvider$load$15.L$1 = SpillingKt.nullOutSpilledVariable(str56);
                                                                    movieBoxProvider$load$15.L$2 = SpillingKt.nullOutSpilledVariable(str58);
                                                                    movieBoxProvider$load$15.L$3 = SpillingKt.nullOutSpilledVariable(str60);
                                                                    movieBoxProvider$load$15.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                                    movieBoxProvider$load$15.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                                    movieBoxProvider$load$15.L$6 = SpillingKt.nullOutSpilledVariable(map2);
                                                                    movieBoxProvider$load$15.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                                    movieBoxProvider$load$15.L$8 = SpillingKt.nullOutSpilledVariable(str44);
                                                                    movieBoxProvider$load$15.L$9 = SpillingKt.nullOutSpilledVariable(jacksonObjectMapper);
                                                                    movieBoxProvider$load$15.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                                    movieBoxProvider$load$15.L$11 = SpillingKt.nullOutSpilledVariable(jsonNode6);
                                                                    movieBoxProvider$load$15.L$12 = SpillingKt.nullOutSpilledVariable(str57);
                                                                    movieBoxProvider$load$15.L$13 = SpillingKt.nullOutSpilledVariable(str52);
                                                                    movieBoxProvider$load$15.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                                    movieBoxProvider$load$15.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                                    movieBoxProvider$load$15.L$16 = SpillingKt.nullOutSpilledVariable(str47);
                                                                    movieBoxProvider$load$15.L$17 = SpillingKt.nullOutSpilledVariable(num11);
                                                                    movieBoxProvider$load$15.L$18 = SpillingKt.nullOutSpilledVariable(num10);
                                                                    movieBoxProvider$load$15.L$19 = SpillingKt.nullOutSpilledVariable(str51);
                                                                    movieBoxProvider$load$15.L$20 = SpillingKt.nullOutSpilledVariable(str54);
                                                                    movieBoxProvider$load$15.L$21 = SpillingKt.nullOutSpilledVariable(list8);
                                                                    movieBoxProvider$load$15.L$22 = SpillingKt.nullOutSpilledVariable(list7);
                                                                    movieBoxProvider$load$15.L$23 = SpillingKt.nullOutSpilledVariable(num12);
                                                                    movieBoxProvider$load$15.L$24 = SpillingKt.nullOutSpilledVariable(tvType4);
                                                                    movieBoxProvider$load$15.L$25 = SpillingKt.nullOutSpilledVariable(num13);
                                                                    movieBoxProvider$load$15.L$26 = SpillingKt.nullOutSpilledVariable(str55);
                                                                    movieBoxProvider$load$15.L$27 = SpillingKt.nullOutSpilledVariable(str59);
                                                                    movieBoxProvider$load$15.L$28 = SpillingKt.nullOutSpilledVariable(jsonNode7);
                                                                    movieBoxProvider$load$15.L$29 = SpillingKt.nullOutSpilledVariable(list20);
                                                                    movieBoxProvider$load$15.L$30 = SpillingKt.nullOutSpilledVariable(str118);
                                                                    movieBoxProvider$load$15.L$31 = SpillingKt.nullOutSpilledVariable(str119);
                                                                    movieBoxProvider$load$15.L$32 = SpillingKt.nullOutSpilledVariable(str120);
                                                                    movieBoxProvider$load$15.L$33 = SpillingKt.nullOutSpilledVariable(asText15);
                                                                    movieBoxProvider$load$15.I$0 = i7;
                                                                    movieBoxProvider$load$15.label = 10;
                                                                    Object newMovieLoadResponse = MainAPIKt.newMovieLoadResponse(this, str57, str58, tvType4, str56, movieBoxProvider$load$7, movieBoxProvider$load$15);
                                                                    return newMovieLoadResponse == obj17 ? obj17 : newMovieLoadResponse;
                                                                }
                                                                ArrayList arrayList7 = new ArrayList();
                                                                arrayList7.add(str56);
                                                                Iterable iterable5 = jsonNode6.get("dubs");
                                                                if (iterable5 != null) {
                                                                    Iterator it4 = iterable5.iterator();
                                                                    while (it4.hasNext()) {
                                                                        String str121 = str53;
                                                                        String str122 = str60;
                                                                        JsonNode jsonNode39 = ((JsonNode) it4.next()).get("subjectId");
                                                                        String asText16 = jsonNode39 != null ? jsonNode39.asText() : null;
                                                                        String str123 = asText16;
                                                                        if (!(str123 == null || StringsKt.isBlank(str123)) && !arrayList7.contains(asText16)) {
                                                                            arrayList7.add(asText16);
                                                                        }
                                                                        str53 = str121;
                                                                        str60 = str122;
                                                                    }
                                                                    str69 = str53;
                                                                    str70 = str60;
                                                                    Unit unit = Unit.INSTANCE;
                                                                } else {
                                                                    str69 = str53;
                                                                    str70 = str60;
                                                                }
                                                                LinkedHashMap linkedHashMap3 = new LinkedHashMap();
                                                                Iterator it5 = arrayList7.iterator();
                                                                collection = arrayList7;
                                                                str71 = str57;
                                                                movieBoxProvider$load$16 = movieBoxProvider$load$15;
                                                                jsonNode9 = jsonNode7;
                                                                str72 = str51;
                                                                str73 = str118;
                                                                str74 = str119;
                                                                str75 = str54;
                                                                str76 = str59;
                                                                str77 = str120;
                                                                str78 = str52;
                                                                num17 = num10;
                                                                list11 = list7;
                                                                list12 = list8;
                                                                str79 = asText15;
                                                                num18 = num11;
                                                                num19 = num12;
                                                                str80 = str55;
                                                                num20 = num13;
                                                                linkedHashMap = linkedHashMap3;
                                                                it = it5;
                                                                str81 = str58;
                                                                tvType6 = tvType4;
                                                                i9 = i7;
                                                                list13 = list20;
                                                                movieBoxProvider = this;
                                                                str82 = str56;
                                                                jsonNode10 = jsonNode6;
                                                                obj18 = obj17;
                                                                continuation2 = continuation;
                                                                if (it.hasNext()) {
                                                                    String str124 = (String) it.next();
                                                                    Continuation<? super LoadResponse> continuation5 = continuation2;
                                                                    String str125 = movieBoxProvider.getMainUrl() + "/wefeed-mobile-bff/subject-api/season-info?subjectId=" + str124;
                                                                    int i12 = i9;
                                                                    String str126 = str71;
                                                                    linkedHashMap2 = linkedHashMap;
                                                                    List list21 = list13;
                                                                    Object obj26 = obj18;
                                                                    Iterator it6 = it;
                                                                    TvType tvType9 = tvType6;
                                                                    String str127 = str73;
                                                                    String str128 = str74;
                                                                    String str129 = str75;
                                                                    String str130 = str76;
                                                                    String str131 = str77;
                                                                    String str132 = str78;
                                                                    Integer num27 = num17;
                                                                    List list22 = list11;
                                                                    List list23 = list12;
                                                                    String str133 = str79;
                                                                    Integer num28 = num18;
                                                                    Integer num29 = num19;
                                                                    String str134 = str80;
                                                                    Integer num30 = num20;
                                                                    JsonNode jsonNode40 = jsonNode10;
                                                                    MovieBoxProvider$load$1 movieBoxProvider$load$18 = movieBoxProvider$load$16;
                                                                    String generateXTrSignature$default3 = generateXTrSignature$default(movieBoxProvider, "GET", "application/json", "application/json", str125, null, false, null, 112, null);
                                                                    Map mutableMap = MapsKt.toMutableMap(map2);
                                                                    obj19 = obj9;
                                                                    mutableMap.put(obj19, generateXTrSignature$default3);
                                                                    Requests app3 = MainActivityKt.getApp();
                                                                    movieBoxProvider$load$18.L$0 = SpillingKt.nullOutSpilledVariable(str69);
                                                                    movieBoxProvider$load$18.L$1 = str82;
                                                                    movieBoxProvider$load$18.L$2 = str81;
                                                                    movieBoxProvider$load$18.L$3 = SpillingKt.nullOutSpilledVariable(str70);
                                                                    movieBoxProvider$load$18.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                                    movieBoxProvider$load$18.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                                    movieBoxProvider$load$18.L$6 = map2;
                                                                    movieBoxProvider$load$18.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                                    movieBoxProvider$load$18.L$8 = SpillingKt.nullOutSpilledVariable(str44);
                                                                    movieBoxProvider$load$18.L$9 = jacksonObjectMapper;
                                                                    movieBoxProvider$load$18.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                                    movieBoxProvider$load$18.L$11 = SpillingKt.nullOutSpilledVariable(jsonNode40);
                                                                    movieBoxProvider$load$18.L$12 = str126;
                                                                    movieBoxProvider$load$18.L$13 = str132;
                                                                    movieBoxProvider$load$18.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                                    movieBoxProvider$load$18.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                                    movieBoxProvider$load$18.L$16 = SpillingKt.nullOutSpilledVariable(str47);
                                                                    movieBoxProvider$load$18.L$17 = num28;
                                                                    movieBoxProvider$load$18.L$18 = num27;
                                                                    String str135 = str72;
                                                                    movieBoxProvider$load$18.L$19 = str135;
                                                                    movieBoxProvider$load$18.L$20 = str129;
                                                                    movieBoxProvider$load$18.L$21 = list23;
                                                                    movieBoxProvider$load$18.L$22 = list22;
                                                                    movieBoxProvider$load$18.L$23 = num29;
                                                                    movieBoxProvider$load$18.L$24 = tvType9;
                                                                    movieBoxProvider$load$18.L$25 = num30;
                                                                    movieBoxProvider$load$18.L$26 = str134;
                                                                    movieBoxProvider$load$18.L$27 = str130;
                                                                    movieBoxProvider$load$18.L$28 = SpillingKt.nullOutSpilledVariable(jsonNode9);
                                                                    movieBoxProvider$load$18.L$29 = list21;
                                                                    movieBoxProvider$load$18.L$30 = str127;
                                                                    movieBoxProvider$load$18.L$31 = str128;
                                                                    movieBoxProvider$load$18.L$32 = str131;
                                                                    movieBoxProvider$load$18.L$33 = str133;
                                                                    movieBoxProvider$load$18.L$34 = SpillingKt.nullOutSpilledVariable(collection);
                                                                    movieBoxProvider$load$18.L$35 = linkedHashMap2;
                                                                    movieBoxProvider$load$18.L$36 = it6;
                                                                    movieBoxProvider$load$18.L$37 = SpillingKt.nullOutSpilledVariable(str124);
                                                                    movieBoxProvider$load$18.L$38 = SpillingKt.nullOutSpilledVariable(str125);
                                                                    movieBoxProvider$load$18.L$39 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default3);
                                                                    movieBoxProvider$load$18.L$40 = SpillingKt.nullOutSpilledVariable(mutableMap);
                                                                    movieBoxProvider$load$18.I$0 = i12;
                                                                    MovieBoxProvider movieBoxProvider4 = movieBoxProvider;
                                                                    movieBoxProvider$load$18.label = 8;
                                                                    Object obj27 = Requests.get$default(app3, str125, mutableMap, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$load$16, 4092, (Object) null);
                                                                    MovieBoxProvider$load$1 movieBoxProvider$load$19 = movieBoxProvider$load$16;
                                                                    if (obj27 == obj26) {
                                                                        return obj26;
                                                                    }
                                                                    str78 = str132;
                                                                    num18 = num28;
                                                                    num17 = num27;
                                                                    str72 = str135;
                                                                    movieBoxProvider$load$16 = movieBoxProvider$load$19;
                                                                    list12 = list23;
                                                                    list11 = list22;
                                                                    str93 = str81;
                                                                    map4 = map2;
                                                                    obj21 = obj27;
                                                                    jsonNode21 = jsonNode40;
                                                                    str94 = str126;
                                                                    it2 = it6;
                                                                    map5 = mutableMap;
                                                                    str75 = str129;
                                                                    num19 = num29;
                                                                    tvType6 = tvType9;
                                                                    num20 = num30;
                                                                    str80 = str134;
                                                                    str76 = str130;
                                                                    list13 = list21;
                                                                    str73 = str127;
                                                                    str74 = str128;
                                                                    str77 = str131;
                                                                    str79 = str133;
                                                                    i10 = i12;
                                                                    coroutine_suspended = obj26;
                                                                    movieBoxProvider2 = movieBoxProvider4;
                                                                    continuation3 = continuation5;
                                                                    niceResponse5 = (NiceResponse) obj21;
                                                                    int i13 = i10;
                                                                    if (niceResponse5.getCode() != 200) {
                                                                        continuation4 = continuation3;
                                                                        String str136 = str97;
                                                                        JsonNode jsonNode41 = jacksonObjectMapper.readTree(niceResponse5.getBody().string()).get(str136);
                                                                        if (jsonNode41 != null) {
                                                                            movieBoxProvider3 = movieBoxProvider2;
                                                                            jsonNode22 = jsonNode41.get("seasons");
                                                                        } else {
                                                                            movieBoxProvider3 = movieBoxProvider2;
                                                                            jsonNode22 = null;
                                                                        }
                                                                        if (jsonNode22 == null || !jsonNode22.isArray()) {
                                                                            str95 = str136;
                                                                        } else if (jsonNode22.size() == 0) {
                                                                            str95 = str136;
                                                                        } else {
                                                                            Iterable<JsonNode> iterable6 = (Iterable) jsonNode22;
                                                                            for (JsonNode jsonNode42 : iterable6) {
                                                                                JsonNode jsonNode43 = jsonNode22;
                                                                                Iterable iterable7 = iterable6;
                                                                                JsonNode jsonNode44 = jsonNode42.get("se");
                                                                                int asInt2 = jsonNode44 != null ? jsonNode44.asInt() : 1;
                                                                                JsonNode jsonNode45 = jsonNode42.get("maxEp");
                                                                                int asInt3 = jsonNode45 != null ? jsonNode45.asInt() : 1;
                                                                                Integer boxInt2 = Boxing.boxInt(asInt2);
                                                                                LinkedHashMap linkedHashMap4 = linkedHashMap2;
                                                                                String str137 = str136;
                                                                                Object obj28 = linkedHashMap4.get(boxInt2);
                                                                                if (obj28 == null) {
                                                                                    obj20 = new LinkedHashSet();
                                                                                    map6 = map5;
                                                                                    linkedHashMap4.put(boxInt2, obj20);
                                                                                } else {
                                                                                    map6 = map5;
                                                                                    obj20 = obj28;
                                                                                }
                                                                                Set set = (Set) obj20;
                                                                                if (1 <= asInt3) {
                                                                                    while (true) {
                                                                                        set.add(Boxing.boxInt(i11));
                                                                                        i11 = i11 != asInt3 ? i11 + 1 : 1;
                                                                                    }
                                                                                }
                                                                                jsonNode22 = jsonNode43;
                                                                                iterable6 = iterable7;
                                                                                map5 = map6;
                                                                                str136 = str137;
                                                                            }
                                                                            str95 = str136;
                                                                        }
                                                                    } else {
                                                                        continuation4 = continuation3;
                                                                        str95 = str97;
                                                                        movieBoxProvider3 = movieBoxProvider2;
                                                                    }
                                                                    i9 = i13;
                                                                    obj9 = obj19;
                                                                    obj18 = coroutine_suspended;
                                                                    it = it2;
                                                                    continuation2 = continuation4;
                                                                    movieBoxProvider = movieBoxProvider3;
                                                                    linkedHashMap = linkedHashMap2;
                                                                    str71 = str94;
                                                                    jsonNode10 = jsonNode21;
                                                                    map2 = map4;
                                                                    str81 = str93;
                                                                    str97 = str95;
                                                                    if (it.hasNext()) {
                                                                        MovieBoxProvider movieBoxProvider5 = movieBoxProvider;
                                                                        int i14 = i9;
                                                                        String str138 = str71;
                                                                        LinkedHashMap linkedHashMap5 = linkedHashMap;
                                                                        TvType tvType10 = tvType6;
                                                                        List list24 = list13;
                                                                        Object obj29 = obj18;
                                                                        JsonNode jsonNode46 = jsonNode10;
                                                                        String str139 = str72;
                                                                        String str140 = str73;
                                                                        String str141 = str74;
                                                                        String str142 = str75;
                                                                        String str143 = str76;
                                                                        String str144 = str77;
                                                                        String str145 = str78;
                                                                        Integer num31 = num17;
                                                                        List list25 = list11;
                                                                        List list26 = list12;
                                                                        String str146 = str79;
                                                                        Integer num32 = num18;
                                                                        Integer num33 = num19;
                                                                        String str147 = str80;
                                                                        Integer num34 = num20;
                                                                        MovieBoxProvider$load$1 movieBoxProvider$load$110 = movieBoxProvider$load$16;
                                                                        ArrayList arrayList8 = new ArrayList();
                                                                        boolean z5 = false;
                                                                        for (Map.Entry entry : linkedHashMap.entrySet()) {
                                                                            Integer num35 = num32;
                                                                            int intValue2 = ((Number) entry.getKey()).intValue();
                                                                            Iterator it7 = CollectionsKt.sorted((Set) entry.getValue()).iterator();
                                                                            while (it7.hasNext()) {
                                                                                LinkedHashMap linkedHashMap6 = linkedHashMap;
                                                                                int intValue3 = ((Number) it7.next()).intValue();
                                                                                Iterator it8 = list24.iterator();
                                                                                while (true) {
                                                                                    if (it8.hasNext()) {
                                                                                        ?? next = it8.next();
                                                                                        num21 = num31;
                                                                                        JsonNode jsonNode47 = (JsonNode) next;
                                                                                        z = z5;
                                                                                        JsonNode jsonNode48 = jsonNode47.get("season");
                                                                                        if (jsonNode48 != null && jsonNode48.asInt() == intValue2) {
                                                                                            JsonNode jsonNode49 = jsonNode47.get("episode");
                                                                                            if (jsonNode49 != null && jsonNode49.asInt() == intValue3) {
                                                                                                z2 = true;
                                                                                                if (z2) {
                                                                                                    z5 = z;
                                                                                                    num31 = num21;
                                                                                                } else {
                                                                                                    jsonNode15 = next;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        z2 = false;
                                                                                        if (z2) {
                                                                                        }
                                                                                    } else {
                                                                                        num21 = num31;
                                                                                        z = z5;
                                                                                        jsonNode15 = null;
                                                                                    }
                                                                                }
                                                                                JsonNode jsonNode50 = jsonNode15;
                                                                                if (jsonNode50 == null || (jsonNode20 = jsonNode50.get("name")) == null || (asText8 = jsonNode20.asText()) == null) {
                                                                                    if (jsonNode50 != null) {
                                                                                        str83 = str96;
                                                                                        JsonNode jsonNode51 = jsonNode50.get(str83);
                                                                                        if (jsonNode51 != null && (str84 = jsonNode51.asText()) != null) {
                                                                                            if (StringsKt.isBlank(str84)) {
                                                                                                str84 = null;
                                                                                            }
                                                                                            if (str84 != null) {
                                                                                                str85 = str83;
                                                                                                list14 = list26;
                                                                                                str86 = "S" + intValue2 + "E" + intValue3;
                                                                                            } else {
                                                                                                str85 = str83;
                                                                                                list14 = list26;
                                                                                                str86 = str84;
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        str83 = str96;
                                                                                    }
                                                                                    str84 = null;
                                                                                    if (str84 != null) {
                                                                                    }
                                                                                } else {
                                                                                    str86 = asText8;
                                                                                    list14 = list26;
                                                                                    str85 = str96;
                                                                                }
                                                                                if (jsonNode50 == null || (jsonNode19 = jsonNode50.get("overview")) == null || (str88 = jsonNode19.asText()) == null) {
                                                                                    if (jsonNode50 != null) {
                                                                                        str87 = str29;
                                                                                        JsonNode jsonNode52 = jsonNode50.get(str87);
                                                                                        if (jsonNode52 != null) {
                                                                                            str88 = jsonNode52.asText();
                                                                                            if (str88 != null) {
                                                                                                str29 = str87;
                                                                                                str89 = "Season " + intValue2 + " Episode " + intValue3;
                                                                                                if (jsonNode50 != null && (jsonNode18 = jsonNode50.get("thumbnail")) != null && (asText7 = jsonNode18.asText()) != null) {
                                                                                                    if (StringsKt.isBlank(asText7)) {
                                                                                                        asText7 = null;
                                                                                                    }
                                                                                                    if (asText7 != null) {
                                                                                                        str90 = asText7;
                                                                                                        if (jsonNode50 != null || (jsonNode17 = jsonNode50.get("runtime")) == null) {
                                                                                                            list15 = list25;
                                                                                                            str91 = str81;
                                                                                                        } else {
                                                                                                            String asText17 = jsonNode17.asText();
                                                                                                            if (asText17 != null) {
                                                                                                                String str148 = asText17;
                                                                                                                StringBuilder sb = new StringBuilder();
                                                                                                                list15 = list25;
                                                                                                                int length = str148.length();
                                                                                                                str91 = str81;
                                                                                                                int i15 = 0;
                                                                                                                while (i15 < length) {
                                                                                                                    int i16 = length;
                                                                                                                    char charAt = str148.charAt(i15);
                                                                                                                    String str149 = str148;
                                                                                                                    int i17 = i15;
                                                                                                                    if (Character.isDigit(charAt)) {
                                                                                                                        sb.append(charAt);
                                                                                                                    }
                                                                                                                    i15 = i17 + 1;
                                                                                                                    length = i16;
                                                                                                                    str148 = str149;
                                                                                                                }
                                                                                                                String sb2 = sb.toString();
                                                                                                                if (sb2 != null) {
                                                                                                                    num22 = StringsKt.toIntOrNull(sb2);
                                                                                                                    if (jsonNode50 != null && (jsonNode16 = jsonNode50.get("released")) != null && (str92 = jsonNode16.asText()) != null) {
                                                                                                                        if (StringsKt.isBlank(str92)) {
                                                                                                                            str92 = null;
                                                                                                                        }
                                                                                                                        if (str92 != null) {
                                                                                                                            int i18 = intValue2;
                                                                                                                            arrayList8.add(MainAPIKt.newEpisode(movieBoxProvider5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProvider$$ExternalSyntheticLambda3(str86, i18, intValue3, str90, str89, num22, str92)));
                                                                                                                            linkedHashMap = linkedHashMap6;
                                                                                                                            intValue2 = i18;
                                                                                                                            z5 = z;
                                                                                                                            str96 = str85;
                                                                                                                            str81 = str91;
                                                                                                                            num31 = num21;
                                                                                                                            list26 = list14;
                                                                                                                            list25 = list15;
                                                                                                                        }
                                                                                                                    }
                                                                                                                    str92 = "";
                                                                                                                    int i182 = intValue2;
                                                                                                                    arrayList8.add(MainAPIKt.newEpisode(movieBoxProvider5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProvider$$ExternalSyntheticLambda3(str86, i182, intValue3, str90, str89, num22, str92)));
                                                                                                                    linkedHashMap = linkedHashMap6;
                                                                                                                    intValue2 = i182;
                                                                                                                    z5 = z;
                                                                                                                    str96 = str85;
                                                                                                                    str81 = str91;
                                                                                                                    num31 = num21;
                                                                                                                    list26 = list14;
                                                                                                                    list25 = list15;
                                                                                                                }
                                                                                                            } else {
                                                                                                                list15 = list25;
                                                                                                                str91 = str81;
                                                                                                            }
                                                                                                        }
                                                                                                        num22 = null;
                                                                                                        if (jsonNode50 != null) {
                                                                                                            if (StringsKt.isBlank(str92)) {
                                                                                                            }
                                                                                                            if (str92 != null) {
                                                                                                            }
                                                                                                        }
                                                                                                        str92 = "";
                                                                                                        int i1822 = intValue2;
                                                                                                        arrayList8.add(MainAPIKt.newEpisode(movieBoxProvider5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProvider$$ExternalSyntheticLambda3(str86, i1822, intValue3, str90, str89, num22, str92)));
                                                                                                        linkedHashMap = linkedHashMap6;
                                                                                                        intValue2 = i1822;
                                                                                                        z5 = z;
                                                                                                        str96 = str85;
                                                                                                        str81 = str91;
                                                                                                        num31 = num21;
                                                                                                        list26 = list14;
                                                                                                        list25 = list15;
                                                                                                    }
                                                                                                }
                                                                                                str90 = str139;
                                                                                                if (jsonNode50 != null) {
                                                                                                }
                                                                                                list15 = list25;
                                                                                                str91 = str81;
                                                                                                num22 = null;
                                                                                                if (jsonNode50 != null) {
                                                                                                }
                                                                                                str92 = "";
                                                                                                int i18222 = intValue2;
                                                                                                arrayList8.add(MainAPIKt.newEpisode(movieBoxProvider5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProvider$$ExternalSyntheticLambda3(str86, i18222, intValue3, str90, str89, num22, str92)));
                                                                                                linkedHashMap = linkedHashMap6;
                                                                                                intValue2 = i18222;
                                                                                                z5 = z;
                                                                                                str96 = str85;
                                                                                                str81 = str91;
                                                                                                num31 = num21;
                                                                                                list26 = list14;
                                                                                                list25 = list15;
                                                                                            } else {
                                                                                                str29 = str87;
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        str87 = str29;
                                                                                    }
                                                                                    str88 = null;
                                                                                    if (str88 != null) {
                                                                                    }
                                                                                }
                                                                                str89 = str88;
                                                                                if (jsonNode50 != null) {
                                                                                    if (StringsKt.isBlank(asText7)) {
                                                                                    }
                                                                                    if (asText7 != null) {
                                                                                    }
                                                                                }
                                                                                str90 = str139;
                                                                                if (jsonNode50 != null) {
                                                                                }
                                                                                list15 = list25;
                                                                                str91 = str81;
                                                                                num22 = null;
                                                                                if (jsonNode50 != null) {
                                                                                }
                                                                                str92 = "";
                                                                                int i182222 = intValue2;
                                                                                arrayList8.add(MainAPIKt.newEpisode(movieBoxProvider5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProvider$$ExternalSyntheticLambda3(str86, i182222, intValue3, str90, str89, num22, str92)));
                                                                                linkedHashMap = linkedHashMap6;
                                                                                intValue2 = i182222;
                                                                                z5 = z;
                                                                                str96 = str85;
                                                                                str81 = str91;
                                                                                num31 = num21;
                                                                                list26 = list14;
                                                                                list25 = list15;
                                                                            }
                                                                            num32 = num35;
                                                                        }
                                                                        Integer num36 = num32;
                                                                        Integer num37 = num31;
                                                                        List list27 = list26;
                                                                        List list28 = list25;
                                                                        String str150 = str81;
                                                                        if (arrayList8.isEmpty()) {
                                                                            arrayList8.add(MainAPIKt.newEpisode(movieBoxProvider5, str82 + "|1|1", new MovieBoxProvider$$ExternalSyntheticLambda4(str139)));
                                                                        }
                                                                        MovieBoxProvider$load$6 movieBoxProvider$load$6 = new MovieBoxProvider$load$6(str139, str140, str141, str142, str143, str144, str145, num37, list28, list27, str146, num36, num33, str147, num34, null);
                                                                        movieBoxProvider$load$110.L$0 = SpillingKt.nullOutSpilledVariable(str69);
                                                                        movieBoxProvider$load$110.L$1 = SpillingKt.nullOutSpilledVariable(str82);
                                                                        movieBoxProvider$load$110.L$2 = SpillingKt.nullOutSpilledVariable(str150);
                                                                        movieBoxProvider$load$110.L$3 = SpillingKt.nullOutSpilledVariable(str70);
                                                                        movieBoxProvider$load$110.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                                        movieBoxProvider$load$110.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                                        movieBoxProvider$load$110.L$6 = SpillingKt.nullOutSpilledVariable(map2);
                                                                        movieBoxProvider$load$110.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                                        movieBoxProvider$load$110.L$8 = SpillingKt.nullOutSpilledVariable(str44);
                                                                        movieBoxProvider$load$110.L$9 = SpillingKt.nullOutSpilledVariable(jacksonObjectMapper);
                                                                        movieBoxProvider$load$110.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                                        movieBoxProvider$load$110.L$11 = SpillingKt.nullOutSpilledVariable(jsonNode46);
                                                                        movieBoxProvider$load$110.L$12 = SpillingKt.nullOutSpilledVariable(str138);
                                                                        movieBoxProvider$load$110.L$13 = SpillingKt.nullOutSpilledVariable(str145);
                                                                        movieBoxProvider$load$110.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                                        movieBoxProvider$load$110.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                                        movieBoxProvider$load$110.L$16 = SpillingKt.nullOutSpilledVariable(str47);
                                                                        movieBoxProvider$load$110.L$17 = SpillingKt.nullOutSpilledVariable(num36);
                                                                        movieBoxProvider$load$110.L$18 = SpillingKt.nullOutSpilledVariable(num37);
                                                                        movieBoxProvider$load$110.L$19 = SpillingKt.nullOutSpilledVariable(str139);
                                                                        movieBoxProvider$load$110.L$20 = SpillingKt.nullOutSpilledVariable(str142);
                                                                        movieBoxProvider$load$110.L$21 = SpillingKt.nullOutSpilledVariable(list27);
                                                                        movieBoxProvider$load$110.L$22 = SpillingKt.nullOutSpilledVariable(list28);
                                                                        movieBoxProvider$load$110.L$23 = SpillingKt.nullOutSpilledVariable(num33);
                                                                        movieBoxProvider$load$110.L$24 = SpillingKt.nullOutSpilledVariable(tvType10);
                                                                        movieBoxProvider$load$110.L$25 = SpillingKt.nullOutSpilledVariable(num34);
                                                                        movieBoxProvider$load$110.L$26 = SpillingKt.nullOutSpilledVariable(str147);
                                                                        movieBoxProvider$load$110.L$27 = SpillingKt.nullOutSpilledVariable(str143);
                                                                        movieBoxProvider$load$110.L$28 = SpillingKt.nullOutSpilledVariable(jsonNode9);
                                                                        movieBoxProvider$load$110.L$29 = SpillingKt.nullOutSpilledVariable(list24);
                                                                        movieBoxProvider$load$110.L$30 = SpillingKt.nullOutSpilledVariable(str140);
                                                                        movieBoxProvider$load$110.L$31 = SpillingKt.nullOutSpilledVariable(str141);
                                                                        movieBoxProvider$load$110.L$32 = SpillingKt.nullOutSpilledVariable(str144);
                                                                        movieBoxProvider$load$110.L$33 = SpillingKt.nullOutSpilledVariable(str146);
                                                                        movieBoxProvider$load$110.L$34 = SpillingKt.nullOutSpilledVariable(collection);
                                                                        movieBoxProvider$load$110.L$35 = SpillingKt.nullOutSpilledVariable(linkedHashMap5);
                                                                        movieBoxProvider$load$110.L$36 = SpillingKt.nullOutSpilledVariable(arrayList8);
                                                                        movieBoxProvider$load$110.L$37 = null;
                                                                        movieBoxProvider$load$110.L$38 = null;
                                                                        movieBoxProvider$load$110.L$39 = null;
                                                                        movieBoxProvider$load$110.L$40 = null;
                                                                        movieBoxProvider$load$110.I$0 = i14;
                                                                        movieBoxProvider$load$110.label = 9;
                                                                        Object newTvSeriesLoadResponse = MainAPIKt.newTvSeriesLoadResponse(movieBoxProvider5, str138, str150, tvType10, arrayList8, movieBoxProvider$load$6, movieBoxProvider$load$110);
                                                                        return newTvSeriesLoadResponse == obj29 ? obj29 : newTvSeriesLoadResponse;
                                                                    }
                                                                }
                                                            } else {
                                                                String str151 = str45;
                                                                Integer num38 = num9;
                                                                String str152 = str48;
                                                                obj17 = obj15;
                                                                str51 = str46;
                                                                list7 = list5;
                                                                list8 = list6;
                                                                str52 = str40;
                                                                num10 = num6;
                                                                num11 = num7;
                                                                str53 = str38;
                                                                i7 = i6;
                                                                str54 = str152;
                                                                str55 = str151;
                                                                num12 = num38;
                                                                tvType4 = tvType8;
                                                                str56 = str49;
                                                                jsonNode6 = jsonNode5;
                                                                str57 = str43;
                                                                str58 = str18;
                                                                jsonNode7 = null;
                                                                str59 = str115;
                                                                num13 = num8;
                                                                str60 = str25;
                                                                if (jsonNode7 != null) {
                                                                }
                                                                List emptyList32 = CollectionsKt.emptyList();
                                                                List list202 = emptyList32;
                                                                if (jsonNode7 != null) {
                                                                }
                                                                if (jsonNode7 != null) {
                                                                }
                                                                if (jsonNode7 != null) {
                                                                }
                                                                if (jsonNode7 != null) {
                                                                }
                                                                if (tvType4 != TvType.TvSeries) {
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        str29 = str9;
                                                    }
                                                    str30 = null;
                                                    JsonNode jsonNode322 = jsonNode23.get("subjectType");
                                                    if (jsonNode322 != null) {
                                                    }
                                                    obj9 = obj7;
                                                    iterable = jsonNode23.get("staffList");
                                                    if (iterable != null) {
                                                    }
                                                    if (asText != null) {
                                                    }
                                                    list = emptyList;
                                                    str31 = str30;
                                                    emptyList2 = CollectionsKt.emptyList();
                                                    if (str27 != null) {
                                                    }
                                                    switch (i3) {
                                                        case 1:
                                                            break;
                                                        case 2:
                                                            break;
                                                        case 7:
                                                            break;
                                                    }
                                                    String substringBefore$default22 = StringsKt.substringBefore$default(StringsKt.substringBefore$default(substringBefore$default, "(", (String) null, 2, (Object) null), "[", (String) null, 2, (Object) null);
                                                    if (asText11 != null) {
                                                    }
                                                    if (num != null) {
                                                    }
                                                    movieBoxProvider$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                                                    movieBoxProvider$load$14.L$1 = str19;
                                                    movieBoxProvider$load$14.L$2 = str18;
                                                    movieBoxProvider$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                                    movieBoxProvider$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                    movieBoxProvider$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                    movieBoxProvider$load$14.L$6 = map2;
                                                    movieBoxProvider$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                    movieBoxProvider$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                                                    movieBoxProvider$load$14.L$9 = jacksonObjectMapper;
                                                    movieBoxProvider$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                    movieBoxProvider$load$14.L$11 = jsonNode23;
                                                    movieBoxProvider$load$14.L$12 = substringBefore$default;
                                                    movieBoxProvider$load$14.L$13 = asText10;
                                                    movieBoxProvider$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str34);
                                                    movieBoxProvider$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str27);
                                                    movieBoxProvider$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                                                    movieBoxProvider$load$14.L$17 = num;
                                                    movieBoxProvider$load$14.L$18 = num2;
                                                    String str1072 = str32;
                                                    movieBoxProvider$load$14.L$19 = str1072;
                                                    movieBoxProvider$load$14.L$20 = str31;
                                                    movieBoxProvider$load$14.L$21 = list;
                                                    List list172 = list2;
                                                    movieBoxProvider$load$14.L$22 = list172;
                                                    movieBoxProvider$load$14.L$23 = num3;
                                                    movieBoxProvider$load$14.L$24 = tvType;
                                                    Integer num232 = num3;
                                                    movieBoxProvider$load$14.I$0 = i3;
                                                    movieBoxProvider$load$14.label = 5;
                                                    identifyID = MovieBoxProviderKt.identifyID(str33, intOrNull3, d, movieBoxProvider$load$14);
                                                    obj11 = obj10;
                                                    if (identifyID == obj11) {
                                                    }
                                                }
                                            } else {
                                                jsonNode = readTree;
                                            }
                                            num2 = null;
                                            JsonNode jsonNode302 = jsonNode23.get("cover");
                                            if (jsonNode302 != null) {
                                            }
                                            jsonNode2 = jsonNode23.get("cover");
                                            if (jsonNode2 == null) {
                                            }
                                            str30 = null;
                                            JsonNode jsonNode3222 = jsonNode23.get("subjectType");
                                            if (jsonNode3222 != null) {
                                            }
                                            obj9 = obj7;
                                            iterable = jsonNode23.get("staffList");
                                            if (iterable != null) {
                                            }
                                            if (asText != null) {
                                            }
                                            list = emptyList;
                                            str31 = str30;
                                            emptyList2 = CollectionsKt.emptyList();
                                            if (str27 != null) {
                                            }
                                            switch (i3) {
                                                case 1:
                                                    break;
                                                case 2:
                                                    break;
                                                case 7:
                                                    break;
                                            }
                                            String substringBefore$default222 = StringsKt.substringBefore$default(StringsKt.substringBefore$default(substringBefore$default, "(", (String) null, 2, (Object) null), "[", (String) null, 2, (Object) null);
                                            if (asText11 != null) {
                                            }
                                            if (num != null) {
                                            }
                                            movieBoxProvider$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                                            movieBoxProvider$load$14.L$1 = str19;
                                            movieBoxProvider$load$14.L$2 = str18;
                                            movieBoxProvider$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                            movieBoxProvider$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                            movieBoxProvider$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                            movieBoxProvider$load$14.L$6 = map2;
                                            movieBoxProvider$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                            movieBoxProvider$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                                            movieBoxProvider$load$14.L$9 = jacksonObjectMapper;
                                            movieBoxProvider$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                            movieBoxProvider$load$14.L$11 = jsonNode23;
                                            movieBoxProvider$load$14.L$12 = substringBefore$default;
                                            movieBoxProvider$load$14.L$13 = asText10;
                                            movieBoxProvider$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str34);
                                            movieBoxProvider$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str27);
                                            movieBoxProvider$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                                            movieBoxProvider$load$14.L$17 = num;
                                            movieBoxProvider$load$14.L$18 = num2;
                                            String str10722 = str32;
                                            movieBoxProvider$load$14.L$19 = str10722;
                                            movieBoxProvider$load$14.L$20 = str31;
                                            movieBoxProvider$load$14.L$21 = list;
                                            List list1722 = list2;
                                            movieBoxProvider$load$14.L$22 = list1722;
                                            movieBoxProvider$load$14.L$23 = num3;
                                            movieBoxProvider$load$14.L$24 = tvType;
                                            Integer num2322 = num3;
                                            movieBoxProvider$load$14.I$0 = i3;
                                            movieBoxProvider$load$14.label = 5;
                                            identifyID = MovieBoxProviderKt.identifyID(str33, intOrNull3, d, movieBoxProvider$load$14);
                                            obj11 = obj10;
                                            if (identifyID == obj11) {
                                            }
                                        }
                                    }
                                }
                                throw new ErrorLoadingException("No title found");
                            }
                            str19 = str14;
                            str20 = str10;
                            obj7 = obj2;
                            movieBoxProvider$load$14 = movieBoxProvider$load$13;
                            str18 = str17;
                            niceResponse3 = niceResponse2;
                            str6 = str101;
                            generateXTrSignature$default = str16;
                            if (niceResponse3.getCode() == 200) {
                            }
                        } else {
                            Map map8 = map;
                            movieBoxProvider$load$14 = movieBoxProvider$load$13;
                            str18 = str13;
                            map2 = map8;
                            str15 = str11;
                            str19 = str8;
                            niceResponse3 = niceResponse;
                            str20 = str10;
                            obj7 = obj2;
                            if (niceResponse3.getCode() == 200) {
                            }
                        }
                        break;
                    case 1:
                        str3 = (String) movieBoxProvider$load$12.L$2;
                        String str153 = (String) movieBoxProvider$load$12.L$1;
                        String str154 = (String) movieBoxProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str2 = str153;
                        fetchAnonymousToken$default = obj21;
                        i = 1;
                        l = null;
                        str4 = str154;
                        String generateXClientToken$default2 = generateXClientToken$default(this, l, i, l);
                        String str992 = str3;
                        String str1002 = str2;
                        str5 = str97;
                        str6 = (String) fetchAnonymousToken$default;
                        generateXTrSignature$default = generateXTrSignature$default(this, "GET", "application/json", "application/json", str992, null, false, null, 112, null);
                        Map mutableMapOf2 = MapsKt.mutableMapOf(new Pair[]{TuplesKt.to("user-agent", "com.community.mbox.in/50020042 (Linux; U; Android 16; en_IN; " + randomBrandModel() + "; Build/BP22.250325.006; Cronet/133.0.6876.3)"), TuplesKt.to("accept", "application/json"), TuplesKt.to("content-type", "application/json"), TuplesKt.to("connection", "keep-alive"), TuplesKt.to("x-client-token", generateXClientToken$default2), TuplesKt.to("x-tr-signature", generateXTrSignature$default), TuplesKt.to("x-client-info", "{\"package_name\":\"com.community.mbox.in\",\"version_name\":\"3.0.03.0529.03\",\"version_code\":50020042,\"os\":\"android\",\"os_version\":\"16\",\"device_id\":\"" + this.deviceId + "\",\"install_store\":\"ps\",\"gaid\":\"d7578036d13336cc\",\"brand\":\"google\",\"model\":\"" + randomBrandModel() + "\",\"system_language\":\"en\",\"net\":\"NETWORK_WIFI\",\"region\":\"IN\",\"timezone\":\"Asia/Calcutta\",\"sp_code\":\"\"}"), TuplesKt.to("x-client-status", "0"), TuplesKt.to("x-play-mode", "2")});
                        str7 = str6;
                        if (str7 != null) {
                            break;
                        }
                        if (!(str7 != null || StringsKt.isBlank(str7))) {
                        }
                        Requests app4 = MainActivityKt.getApp();
                        movieBoxProvider$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                        str8 = str1002;
                        movieBoxProvider$load$12.L$1 = str8;
                        movieBoxProvider$load$12.L$2 = str992;
                        movieBoxProvider$load$12.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                        movieBoxProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(generateXClientToken$default2);
                        movieBoxProvider$load$12.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProvider$load$12.L$6 = mutableMapOf2;
                        movieBoxProvider$load$12.label = 2;
                        str9 = "description";
                        obj = obj21;
                        str10 = str96;
                        str11 = generateXClientToken$default2;
                        str12 = "Bearer ";
                        obj2 = "x-tr-signature";
                        obj3 = "Authorization";
                        obj4 = coroutine_suspended;
                        i2 = 3;
                        obj5 = Requests.get$default(app4, str992, mutableMapOf2, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$load$12, 4092, (Object) null);
                        map = mutableMapOf2;
                        str13 = str992;
                        movieBoxProvider$load$13 = movieBoxProvider$load$12;
                        if (obj5 == obj4) {
                        }
                        niceResponse = (NiceResponse) obj5;
                        if (niceResponse.getCode() != 441) {
                            break;
                        }
                        movieBoxProvider$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                        movieBoxProvider$load$13.L$1 = str8;
                        movieBoxProvider$load$13.L$2 = str13;
                        movieBoxProvider$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                        movieBoxProvider$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                        movieBoxProvider$load$13.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProvider$load$13.L$6 = map;
                        movieBoxProvider$load$13.L$7 = niceResponse;
                        movieBoxProvider$load$13.label = i2;
                        fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProvider$load$13);
                        if (fetchAnonymousToken == obj4) {
                        }
                        break;
                    case 2:
                        Map map9 = (Map) movieBoxProvider$load$12.L$6;
                        generateXTrSignature$default = (String) movieBoxProvider$load$12.L$5;
                        String str155 = (String) movieBoxProvider$load$12.L$4;
                        String str156 = (String) movieBoxProvider$load$12.L$3;
                        String str157 = (String) movieBoxProvider$load$12.L$2;
                        String str158 = (String) movieBoxProvider$load$12.L$1;
                        String str159 = (String) movieBoxProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str4 = str159;
                        map = map9;
                        str11 = str155;
                        str6 = str156;
                        obj = obj21;
                        obj4 = coroutine_suspended;
                        obj2 = "x-tr-signature";
                        str10 = str96;
                        str5 = str97;
                        obj3 = "Authorization";
                        str13 = str157;
                        movieBoxProvider$load$13 = movieBoxProvider$load$12;
                        obj5 = obj;
                        i2 = 3;
                        str12 = "Bearer ";
                        str8 = str158;
                        str9 = "description";
                        niceResponse = (NiceResponse) obj5;
                        if (niceResponse.getCode() != 441) {
                        }
                        movieBoxProvider$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                        movieBoxProvider$load$13.L$1 = str8;
                        movieBoxProvider$load$13.L$2 = str13;
                        movieBoxProvider$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                        movieBoxProvider$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                        movieBoxProvider$load$13.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProvider$load$13.L$6 = map;
                        movieBoxProvider$load$13.L$7 = niceResponse;
                        movieBoxProvider$load$13.label = i2;
                        fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProvider$load$13);
                        if (fetchAnonymousToken == obj4) {
                        }
                        break;
                    case 3:
                        NiceResponse niceResponse6 = (NiceResponse) movieBoxProvider$load$12.L$7;
                        Map map10 = (Map) movieBoxProvider$load$12.L$6;
                        String str160 = (String) movieBoxProvider$load$12.L$5;
                        String str161 = (String) movieBoxProvider$load$12.L$4;
                        String str162 = (String) movieBoxProvider$load$12.L$2;
                        String str163 = (String) movieBoxProvider$load$12.L$1;
                        String str164 = (String) movieBoxProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str4 = str164;
                        str16 = str160;
                        str15 = str161;
                        str17 = str162;
                        obj = obj21;
                        obj4 = coroutine_suspended;
                        obj2 = "x-tr-signature";
                        str9 = "description";
                        niceResponse2 = niceResponse6;
                        str10 = str96;
                        str5 = str97;
                        obj3 = "Authorization";
                        map2 = map10;
                        movieBoxProvider$load$13 = movieBoxProvider$load$12;
                        str14 = str163;
                        str12 = "Bearer ";
                        obj6 = obj;
                        String str1012 = (String) obj6;
                        str21 = str1012;
                        if (str21 != null) {
                            break;
                        }
                        if (str21 != null || StringsKt.isBlank(str21)) {
                        }
                        break;
                    case 4:
                        map3 = (Map) movieBoxProvider$load$12.L$6;
                        generateXTrSignature$default = (String) movieBoxProvider$load$12.L$5;
                        String str165 = (String) movieBoxProvider$load$12.L$4;
                        String str166 = (String) movieBoxProvider$load$12.L$3;
                        String str167 = (String) movieBoxProvider$load$12.L$2;
                        String str168 = (String) movieBoxProvider$load$12.L$1;
                        String str169 = (String) movieBoxProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str4 = str169;
                        str15 = str165;
                        movieBoxProvider$load$14 = movieBoxProvider$load$12;
                        obj = obj21;
                        obj4 = coroutine_suspended;
                        obj7 = "x-tr-signature";
                        str20 = str96;
                        str5 = str97;
                        str23 = str166;
                        str18 = str167;
                        str22 = str168;
                        obj8 = obj;
                        str9 = "description";
                        String str1042 = str22;
                        map2 = map3;
                        str19 = str1042;
                        niceResponse3 = (NiceResponse) obj8;
                        str6 = str23;
                        if (niceResponse3.getCode() == 200) {
                        }
                        break;
                    case 5:
                        int i19 = movieBoxProvider$load$12.I$0;
                        tvType2 = (TvType) movieBoxProvider$load$12.L$24;
                        num5 = (Integer) movieBoxProvider$load$12.L$23;
                        List list29 = (List) movieBoxProvider$load$12.L$22;
                        List list30 = (List) movieBoxProvider$load$12.L$21;
                        String str170 = (String) movieBoxProvider$load$12.L$20;
                        String str171 = (String) movieBoxProvider$load$12.L$19;
                        Integer num39 = (Integer) movieBoxProvider$load$12.L$18;
                        Integer num40 = (Integer) movieBoxProvider$load$12.L$17;
                        String str172 = (String) movieBoxProvider$load$12.L$16;
                        str37 = (String) movieBoxProvider$load$12.L$15;
                        str42 = (String) movieBoxProvider$load$12.L$14;
                        String str173 = (String) movieBoxProvider$load$12.L$13;
                        String str174 = (String) movieBoxProvider$load$12.L$12;
                        JsonNode jsonNode53 = (JsonNode) movieBoxProvider$load$12.L$11;
                        JsonNode jsonNode54 = (JsonNode) movieBoxProvider$load$12.L$10;
                        ObjectMapper objectMapper = (ObjectMapper) movieBoxProvider$load$12.L$9;
                        String str175 = (String) movieBoxProvider$load$12.L$8;
                        NiceResponse niceResponse7 = (NiceResponse) movieBoxProvider$load$12.L$7;
                        Map map11 = (Map) movieBoxProvider$load$12.L$6;
                        String str176 = (String) movieBoxProvider$load$12.L$5;
                        String str177 = (String) movieBoxProvider$load$12.L$4;
                        String str178 = (String) movieBoxProvider$load$12.L$3;
                        String str179 = (String) movieBoxProvider$load$12.L$2;
                        String str180 = (String) movieBoxProvider$load$12.L$1;
                        String str181 = (String) movieBoxProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        movieBoxProvider$load$14 = movieBoxProvider$load$12;
                        num6 = num39;
                        obj13 = coroutine_suspended;
                        obj9 = "x-tr-signature";
                        i4 = i19;
                        asText = str172;
                        str26 = str97;
                        str40 = str173;
                        jsonNode3 = jsonNode53;
                        jacksonObjectMapper = objectMapper;
                        str28 = str175;
                        niceResponse4 = niceResponse7;
                        map2 = map11;
                        str15 = str177;
                        str25 = str178;
                        str41 = str180;
                        str38 = str181;
                        str35 = str171;
                        obj12 = obj21;
                        obj = obj12;
                        str29 = "description";
                        str24 = str176;
                        str18 = str179;
                        str39 = str170;
                        list4 = list30;
                        list3 = list29;
                        num4 = num40;
                        str36 = str174;
                        jsonNode = jsonNode54;
                        Pair pair2 = (Pair) obj12;
                        TvType tvType72 = tvType2;
                        Integer num242 = (Integer) pair2.component1();
                        String str1082 = (String) pair2.component2();
                        movieBoxProvider$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str38);
                        movieBoxProvider$load$14.L$1 = str41;
                        movieBoxProvider$load$14.L$2 = str18;
                        movieBoxProvider$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                        movieBoxProvider$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                        movieBoxProvider$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                        movieBoxProvider$load$14.L$6 = map2;
                        movieBoxProvider$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                        movieBoxProvider$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                        movieBoxProvider$load$14.L$9 = jacksonObjectMapper;
                        movieBoxProvider$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                        movieBoxProvider$load$14.L$11 = jsonNode3;
                        movieBoxProvider$load$14.L$12 = str36;
                        movieBoxProvider$load$14.L$13 = str40;
                        movieBoxProvider$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                        movieBoxProvider$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                        movieBoxProvider$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                        movieBoxProvider$load$14.L$17 = num4;
                        movieBoxProvider$load$14.L$18 = num6;
                        movieBoxProvider$load$14.L$19 = str35;
                        movieBoxProvider$load$14.L$20 = str39;
                        movieBoxProvider$load$14.L$21 = list4;
                        movieBoxProvider$load$14.L$22 = list3;
                        movieBoxProvider$load$14.L$23 = num5;
                        movieBoxProvider$load$14.L$24 = tvType72;
                        JsonNode jsonNode382 = jsonNode3;
                        movieBoxProvider$load$14.L$25 = num242;
                        movieBoxProvider$load$14.L$26 = str1082;
                        movieBoxProvider$load$14.I$0 = i4;
                        movieBoxProvider$load$14.label = 6;
                        Integer num252 = num5;
                        List list182 = list4;
                        String str1092 = str35;
                        String str1102 = str96;
                        List list192 = list3;
                        String str1112 = str41;
                        String str1122 = str39;
                        String str1132 = str36;
                        movieBoxProvider$load$15 = movieBoxProvider$load$14;
                        String str1142 = str26;
                        num7 = num4;
                        obj14 = obj13;
                        fetchTmdbLogoUrl = MovieBoxProviderKt.fetchTmdbLogoUrl("https://api.themoviedb.org/3", "98ae14df2b8d8f8f8136499daf79f0e0", tvType72, num242, "en", movieBoxProvider$load$15);
                        if (fetchTmdbLogoUrl != obj14) {
                        }
                        break;
                    case 6:
                        int i20 = movieBoxProvider$load$12.I$0;
                        String str182 = (String) movieBoxProvider$load$12.L$26;
                        Integer num41 = (Integer) movieBoxProvider$load$12.L$25;
                        TvType tvType11 = (TvType) movieBoxProvider$load$12.L$24;
                        Integer num42 = (Integer) movieBoxProvider$load$12.L$23;
                        list5 = (List) movieBoxProvider$load$12.L$22;
                        list6 = (List) movieBoxProvider$load$12.L$21;
                        String str183 = (String) movieBoxProvider$load$12.L$20;
                        String str184 = (String) movieBoxProvider$load$12.L$19;
                        Integer num43 = (Integer) movieBoxProvider$load$12.L$18;
                        Integer num44 = (Integer) movieBoxProvider$load$12.L$17;
                        str47 = (String) movieBoxProvider$load$12.L$16;
                        String str185 = (String) movieBoxProvider$load$12.L$15;
                        String str186 = (String) movieBoxProvider$load$12.L$14;
                        String str187 = (String) movieBoxProvider$load$12.L$13;
                        String str188 = (String) movieBoxProvider$load$12.L$12;
                        JsonNode jsonNode55 = (JsonNode) movieBoxProvider$load$12.L$11;
                        JsonNode jsonNode56 = (JsonNode) movieBoxProvider$load$12.L$10;
                        ObjectMapper objectMapper2 = (ObjectMapper) movieBoxProvider$load$12.L$9;
                        str44 = (String) movieBoxProvider$load$12.L$8;
                        NiceResponse niceResponse8 = (NiceResponse) movieBoxProvider$load$12.L$7;
                        Map map12 = (Map) movieBoxProvider$load$12.L$6;
                        String str189 = (String) movieBoxProvider$load$12.L$5;
                        String str190 = (String) movieBoxProvider$load$12.L$4;
                        String str191 = (String) movieBoxProvider$load$12.L$3;
                        String str192 = (String) movieBoxProvider$load$12.L$2;
                        String str193 = (String) movieBoxProvider$load$12.L$1;
                        String str194 = (String) movieBoxProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str45 = str182;
                        num8 = num41;
                        tvType3 = tvType11;
                        num9 = num42;
                        str46 = str184;
                        movieBoxProvider$load$15 = movieBoxProvider$load$12;
                        obj16 = obj21;
                        obj15 = coroutine_suspended;
                        obj9 = "x-tr-signature";
                        i6 = i20;
                        num6 = num43;
                        str40 = str187;
                        str43 = str188;
                        jsonNode5 = jsonNode55;
                        jsonNode = jsonNode56;
                        jacksonObjectMapper = objectMapper2;
                        niceResponse4 = niceResponse8;
                        map2 = map12;
                        str25 = str191;
                        str49 = str193;
                        str38 = str194;
                        obj = obj16;
                        str48 = str183;
                        str29 = "description";
                        num7 = num44;
                        str37 = str185;
                        str42 = str186;
                        str24 = str189;
                        str15 = str190;
                        str18 = str192;
                        TvType tvType82 = tvType3;
                        String str1152 = (String) obj16;
                        str50 = str45;
                        if (str50 != null) {
                            break;
                        }
                        if (str50 != null || StringsKt.isBlank(str50)) {
                        }
                        break;
                    case 7:
                        int i21 = movieBoxProvider$load$12.I$0;
                        String str195 = (String) movieBoxProvider$load$12.L$27;
                        String str196 = (String) movieBoxProvider$load$12.L$26;
                        Integer num45 = (Integer) movieBoxProvider$load$12.L$25;
                        TvType tvType12 = (TvType) movieBoxProvider$load$12.L$24;
                        Integer num46 = (Integer) movieBoxProvider$load$12.L$23;
                        List list31 = (List) movieBoxProvider$load$12.L$22;
                        List list32 = (List) movieBoxProvider$load$12.L$21;
                        str68 = (String) movieBoxProvider$load$12.L$20;
                        i8 = i21;
                        String str197 = (String) movieBoxProvider$load$12.L$19;
                        num14 = (Integer) movieBoxProvider$load$12.L$18;
                        num16 = (Integer) movieBoxProvider$load$12.L$17;
                        str47 = (String) movieBoxProvider$load$12.L$16;
                        String str198 = (String) movieBoxProvider$load$12.L$15;
                        String str199 = (String) movieBoxProvider$load$12.L$14;
                        str65 = (String) movieBoxProvider$load$12.L$13;
                        String str200 = (String) movieBoxProvider$load$12.L$12;
                        JsonNode jsonNode57 = (JsonNode) movieBoxProvider$load$12.L$11;
                        jsonNode8 = (JsonNode) movieBoxProvider$load$12.L$10;
                        ObjectMapper objectMapper3 = (ObjectMapper) movieBoxProvider$load$12.L$9;
                        str44 = (String) movieBoxProvider$load$12.L$8;
                        NiceResponse niceResponse9 = (NiceResponse) movieBoxProvider$load$12.L$7;
                        Map map13 = (Map) movieBoxProvider$load$12.L$6;
                        String str201 = (String) movieBoxProvider$load$12.L$5;
                        String str202 = (String) movieBoxProvider$load$12.L$4;
                        String str203 = (String) movieBoxProvider$load$12.L$3;
                        str63 = (String) movieBoxProvider$load$12.L$2;
                        str62 = (String) movieBoxProvider$load$12.L$1;
                        str53 = (String) movieBoxProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str61 = str195;
                        num8 = num45;
                        list10 = list31;
                        movieBoxProvider$load$15 = movieBoxProvider$load$12;
                        obj17 = coroutine_suspended;
                        obj9 = "x-tr-signature";
                        str66 = str197;
                        str64 = str198;
                        str42 = str199;
                        str43 = str200;
                        jacksonObjectMapper = objectMapper3;
                        niceResponse4 = niceResponse9;
                        map2 = map13;
                        str15 = str202;
                        str25 = str203;
                        list9 = list32;
                        obj = obj21;
                        str29 = "description";
                        str24 = str201;
                        num15 = num46;
                        tvType5 = tvType12;
                        str67 = str196;
                        jsonNode5 = jsonNode57;
                        jsonNode7 = (JsonNode) obj21;
                        str51 = str66;
                        str55 = str67;
                        tvType4 = tvType5;
                        num12 = num15;
                        list7 = list10;
                        list8 = list9;
                        str54 = str68;
                        i7 = i8;
                        num10 = num14;
                        num11 = num16;
                        str52 = str65;
                        jsonNode = jsonNode8;
                        str56 = str62;
                        str37 = str64;
                        jsonNode6 = jsonNode5;
                        str57 = str43;
                        str58 = str63;
                        str60 = str25;
                        str59 = str61;
                        num13 = num8;
                        if (jsonNode7 != null) {
                        }
                        List emptyList322 = CollectionsKt.emptyList();
                        List list2022 = emptyList322;
                        if (jsonNode7 != null) {
                        }
                        if (jsonNode7 != null) {
                        }
                        if (jsonNode7 != null) {
                        }
                        if (jsonNode7 != null) {
                        }
                        if (tvType4 != TvType.TvSeries) {
                        }
                        break;
                    case 8:
                        int i22 = movieBoxProvider$load$12.I$0;
                        map5 = (Map) movieBoxProvider$load$12.L$40;
                        it2 = (Iterator) movieBoxProvider$load$12.L$36;
                        ?? r6 = (Map) movieBoxProvider$load$12.L$35;
                        Collection collection2 = (List) movieBoxProvider$load$12.L$34;
                        String str204 = (String) movieBoxProvider$load$12.L$33;
                        String str205 = (String) movieBoxProvider$load$12.L$32;
                        String str206 = (String) movieBoxProvider$load$12.L$31;
                        String str207 = (String) movieBoxProvider$load$12.L$30;
                        List list33 = (List) movieBoxProvider$load$12.L$29;
                        JsonNode jsonNode58 = (JsonNode) movieBoxProvider$load$12.L$28;
                        String str208 = (String) movieBoxProvider$load$12.L$27;
                        String str209 = (String) movieBoxProvider$load$12.L$26;
                        Integer num47 = (Integer) movieBoxProvider$load$12.L$25;
                        TvType tvType13 = (TvType) movieBoxProvider$load$12.L$24;
                        Integer num48 = (Integer) movieBoxProvider$load$12.L$23;
                        List list34 = (List) movieBoxProvider$load$12.L$22;
                        List list35 = (List) movieBoxProvider$load$12.L$21;
                        String str210 = (String) movieBoxProvider$load$12.L$20;
                        String str211 = (String) movieBoxProvider$load$12.L$19;
                        Integer num49 = (Integer) movieBoxProvider$load$12.L$18;
                        Integer num50 = (Integer) movieBoxProvider$load$12.L$17;
                        String str212 = (String) movieBoxProvider$load$12.L$16;
                        String str213 = (String) movieBoxProvider$load$12.L$15;
                        String str214 = (String) movieBoxProvider$load$12.L$14;
                        String str215 = (String) movieBoxProvider$load$12.L$13;
                        str94 = (String) movieBoxProvider$load$12.L$12;
                        jsonNode21 = (JsonNode) movieBoxProvider$load$12.L$11;
                        JsonNode jsonNode59 = (JsonNode) movieBoxProvider$load$12.L$10;
                        ObjectMapper objectMapper4 = (ObjectMapper) movieBoxProvider$load$12.L$9;
                        String str216 = (String) movieBoxProvider$load$12.L$8;
                        NiceResponse niceResponse10 = (NiceResponse) movieBoxProvider$load$12.L$7;
                        map4 = (Map) movieBoxProvider$load$12.L$6;
                        String str217 = (String) movieBoxProvider$load$12.L$5;
                        String str218 = (String) movieBoxProvider$load$12.L$4;
                        String str219 = (String) movieBoxProvider$load$12.L$3;
                        str93 = (String) movieBoxProvider$load$12.L$2;
                        String str220 = (String) movieBoxProvider$load$12.L$1;
                        String str221 = (String) movieBoxProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        linkedHashMap2 = r6;
                        list13 = list33;
                        str69 = str221;
                        str79 = str204;
                        movieBoxProvider$load$16 = movieBoxProvider$load$12;
                        obj19 = "x-tr-signature";
                        i10 = i22;
                        str77 = str205;
                        str74 = str206;
                        str73 = str207;
                        jsonNode9 = jsonNode58;
                        str76 = str208;
                        str80 = str209;
                        num20 = num47;
                        num19 = num48;
                        list11 = list34;
                        list12 = list35;
                        str75 = str210;
                        str72 = str211;
                        num17 = num49;
                        num18 = num50;
                        str47 = str212;
                        str37 = str213;
                        str42 = str214;
                        str78 = str215;
                        jsonNode = jsonNode59;
                        jacksonObjectMapper = objectMapper4;
                        str44 = str216;
                        niceResponse4 = niceResponse10;
                        str15 = str218;
                        str70 = str219;
                        str82 = str220;
                        movieBoxProvider2 = this;
                        collection = collection2;
                        continuation3 = continuation;
                        str29 = "description";
                        tvType6 = tvType13;
                        str24 = str217;
                        obj = obj21;
                        niceResponse5 = (NiceResponse) obj21;
                        int i132 = i10;
                        if (niceResponse5.getCode() != 200) {
                        }
                        i9 = i132;
                        obj9 = obj19;
                        obj18 = coroutine_suspended;
                        it = it2;
                        continuation2 = continuation4;
                        movieBoxProvider = movieBoxProvider3;
                        linkedHashMap = linkedHashMap2;
                        str71 = str94;
                        jsonNode10 = jsonNode21;
                        map2 = map4;
                        str81 = str93;
                        str97 = str95;
                        if (it.hasNext()) {
                        }
                        break;
                    case 9:
                        int i23 = movieBoxProvider$load$12.I$0;
                        ResultKt.throwOnFailure(obj21);
                        return obj21;
                    case 10:
                        int i24 = movieBoxProvider$load$12.I$0;
                        ResultKt.throwOnFailure(obj21);
                        return obj21;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        movieBoxProvider$load$1 = new MovieBoxProvider$load$1(this, continuation);
        movieBoxProvider$load$12 = movieBoxProvider$load$1;
        Object obj212 = movieBoxProvider$load$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        String str962 = "title";
        String str972 = "data";
        switch (movieBoxProvider$load$12.label) {
        }
    }

    /*  JADX ERROR: Types fix failed
        java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryPossibleTypes(FixTypesVisitor.java:183)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.deduceType(FixTypesVisitor.java:242)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryDeduceTypes(FixTypesVisitor.java:221)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:91)
        */
    /* JADX WARN: Not initialized variable reg: 35, insn: 0x0742: MOVE (r2 I:??[OBJECT, ARRAY]) = (r35 I:??[OBJECT, ARRAY]), block:B:864:0x073c */
    /* JADX WARN: Not initialized variable reg: 36, insn: 0x0744: MOVE (r35 I:??[OBJECT, ARRAY]) = (r36 I:??[OBJECT, ARRAY]), block:B:864:0x073c */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 11 */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 12 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:161:0x1b14 -> B:17:0x1b4e). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:162:0x1eb4 -> B:52:0x102a). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:165:0x1ee1 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:202:0x18c1 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:205:0x18f2 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:208:0x192f -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:211:0x1970 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:214:0x19b3 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:217:0x19f6 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:221:0x1a72 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:224:0x1ab5 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:227:0x1f02 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:264:0x14da -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:267:0x150f -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:270:0x154e -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:273:0x1590 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:276:0x15d4 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:279:0x161a -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:283:0x16a4 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:286:0x16ea -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:289:0x1f29 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:292:0x1f55 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:295:0x1fb5 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:298:0x1fe1 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:301:0x200f -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:304:0x2031 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:307:0x2055 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:310:0x2079 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:313:0x209d -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:334:0x11b4 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:337:0x11d3 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:340:0x11f4 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:347:0x1172 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:368:0x2139 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:371:0x2163 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:378:0x1075 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:389:0x21d2 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:392:0x21fc -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:395:0x2226 -> B:51:0x2250). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:47:0x1c7d -> B:16:0x1cb2). Please report as a decompilation issue!!! */
    @org.jetbrains.annotations.Nullable
    public java.lang.Object loadLinks(@org.jetbrains.annotations.NotNull java.lang.String r83, boolean r84, @org.jetbrains.annotations.NotNull kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> r85, @org.jetbrains.annotations.NotNull kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> r86, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.lang.Boolean> r87) {
        /*
            Method dump skipped, instructions count: 9318
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.cncverse.MovieBoxProvider.loadLinks(java.lang.String, boolean, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
    }

    @NotNull
    public final BrandModel randomBrandModel() {
        String str = (String) CollectionsKt.random(this.brandModels.keySet(), Random.Default);
        List<String> list = this.brandModels.get(str);
        Intrinsics.checkNotNull(list);
        return new BrandModel(str, (String) CollectionsKt.random(list, Random.Default));
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002e. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:105:0x024c  */
    /* JADX WARN: Removed duplicated region for block: B:108:0x02f5 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:109:0x02f6  */
    /* JADX WARN: Removed duplicated region for block: B:110:0x0263  */
    /* JADX WARN: Removed duplicated region for block: B:112:0x011d  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0039  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0452  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x04e4  */
    /* JADX WARN: Removed duplicated region for block: B:55:0x04ed  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x04f2  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x04f7  */
    /* JADX WARN: Removed duplicated region for block: B:60:0x04e9  */
    /* JADX WARN: Removed duplicated region for block: B:75:0x0549  */
    /* JADX WARN: Removed duplicated region for block: B:76:0x006c  */
    /* JADX WARN: Removed duplicated region for block: B:82:0x036d  */
    /* JADX WARN: Removed duplicated region for block: B:86:0x0428  */
    /* JADX WARN: Removed duplicated region for block: B:88:0x00b7  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0031  */
    /* JADX WARN: Removed duplicated region for block: B:96:0x034d A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:97:0x034e  */
    /* JADX WARN: Removed duplicated region for block: B:98:0x00f6  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object search(@NotNull String str, int i, @NotNull Continuation<? super SearchResponseList> continuation) {
        MovieBoxProvider$search$2 movieBoxProvider$search$2;
        MovieBoxProvider$search$2 movieBoxProvider$search$22;
        String str2;
        Object obj;
        Object obj2;
        Object obj3;
        char c;
        Object fetchAnonymousToken$default;
        String str3;
        String str4;
        String str5;
        String str6;
        int i2;
        boolean z;
        Object obj4;
        String str7;
        Object obj5;
        Map map;
        String str8;
        Object obj6;
        Object post$default;
        String str9;
        MovieBoxProvider$search$2 movieBoxProvider$search$23;
        String str10;
        Object obj7;
        int i3;
        String str11;
        String str12;
        String str13;
        String str14;
        RequestBody requestBody;
        NiceResponse niceResponse;
        String str15;
        Object fetchAnonymousToken;
        String str16;
        int i4;
        NiceResponse niceResponse2;
        RequestBody requestBody2;
        String str17;
        String str18;
        String str19;
        String str20;
        String str21;
        String str22;
        String str23;
        String str24;
        String str25;
        int i5;
        Map map2;
        RequestBody requestBody3;
        String str26;
        String str27;
        String str28;
        JsonNode jsonNode;
        Boolean bool;
        String asText;
        String asText2;
        int i6;
        String str29;
        JsonNode jsonNode2;
        TvType tvType;
        if (continuation instanceof MovieBoxProvider$search$2) {
            movieBoxProvider$search$2 = (MovieBoxProvider$search$2) continuation;
            if ((movieBoxProvider$search$2.label & Integer.MIN_VALUE) != 0) {
                movieBoxProvider$search$2.label -= Integer.MIN_VALUE;
                movieBoxProvider$search$22 = movieBoxProvider$search$2;
                Object obj8 = movieBoxProvider$search$22.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProvider$search$22.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj8);
                        String str30 = getMainUrl() + "/wefeed-mobile-bff/subject-api/search/v2";
                        String generateXClientToken$default = generateXClientToken$default(this, null, 1, null);
                        str2 = "{\"page\": " + i + ", \"perPage\": 20, \"keyword\": \"" + str + "\"}";
                        obj = obj8;
                        obj2 = "Authorization";
                        obj3 = "x-tr-signature";
                        c = 1;
                        String generateXTrSignature$default = generateXTrSignature$default(this, "POST", "application/json", "application/json; charset=utf-8", str30, str2, false, null, 96, null);
                        movieBoxProvider$search$22.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        movieBoxProvider$search$22.L$1 = str30;
                        movieBoxProvider$search$22.L$2 = str2;
                        movieBoxProvider$search$22.L$3 = generateXClientToken$default;
                        movieBoxProvider$search$22.L$4 = generateXTrSignature$default;
                        movieBoxProvider$search$22.I$0 = i;
                        movieBoxProvider$search$22.label = 1;
                        fetchAnonymousToken$default = fetchAnonymousToken$default(this, false, movieBoxProvider$search$22, 1, null);
                        if (fetchAnonymousToken$default == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        str3 = str;
                        str4 = generateXClientToken$default;
                        str5 = str30;
                        str6 = generateXTrSignature$default;
                        i2 = i;
                        String str31 = (String) fetchAnonymousToken$default;
                        Pair[] pairArr = new Pair[8];
                        z = false;
                        pairArr[0] = TuplesKt.to("user-agent", "com.community.mbox.in/50020042 (Linux; U; Android 16; en_IN; sdk_gphone64_x86_64; Build/BP22.250325.006; Cronet/133.0.6876.3)");
                        pairArr[c] = TuplesKt.to("accept", "application/json");
                        pairArr[2] = TuplesKt.to("content-type", "application/json; charset=utf-8");
                        pairArr[3] = TuplesKt.to("connection", "keep-alive");
                        pairArr[4] = TuplesKt.to("x-client-token", str4);
                        obj4 = obj3;
                        pairArr[5] = TuplesKt.to(obj4, str6);
                        String str32 = str6;
                        pairArr[6] = TuplesKt.to("x-client-info", "{\"package_name\":\"com.community.mbox.in\",\"version_name\":\"3.0.03.0529.03\",\"version_code\":50020042,\"os\":\"android\",\"os_version\":\"16\",\"device_id\":\"" + this.deviceId + "\",\"install_store\":\"ps\",\"gaid\":\"d7578036d13336cc\",\"brand\":\"google\",\"model\":\"" + randomBrandModel() + "\",\"system_language\":\"en\",\"net\":\"NETWORK_WIFI\",\"region\":\"IN\",\"timezone\":\"Asia/Calcutta\",\"sp_code\":\"\"}");
                        pairArr[7] = TuplesKt.to("x-client-status", "0");
                        Map mutableMapOf = MapsKt.mutableMapOf(pairArr);
                        str7 = str31;
                        if (str7 != null || StringsKt.isBlank(str7)) {
                            obj5 = obj2;
                            mutableMapOf.put(obj5, "Bearer " + str31);
                        } else {
                            obj5 = obj2;
                        }
                        RequestBody create = RequestBody.Companion.create(str2, MediaType.Companion.get("application/json; charset=utf-8"));
                        Requests app = MainActivityKt.getApp();
                        movieBoxProvider$search$22.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                        movieBoxProvider$search$22.L$1 = str5;
                        movieBoxProvider$search$22.L$2 = str2;
                        movieBoxProvider$search$22.L$3 = SpillingKt.nullOutSpilledVariable(str4);
                        movieBoxProvider$search$22.L$4 = SpillingKt.nullOutSpilledVariable(str32);
                        movieBoxProvider$search$22.L$5 = SpillingKt.nullOutSpilledVariable(str31);
                        movieBoxProvider$search$22.L$6 = mutableMapOf;
                        movieBoxProvider$search$22.L$7 = create;
                        movieBoxProvider$search$22.I$0 = i2;
                        movieBoxProvider$search$22.label = 2;
                        map = mutableMapOf;
                        str8 = "Bearer ";
                        obj6 = coroutine_suspended;
                        post$default = Requests.post$default(app, str5, map, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, create, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$search$22, 65276, (Object) null);
                        str9 = str5;
                        movieBoxProvider$search$23 = movieBoxProvider$search$22;
                        if (post$default != obj6) {
                            return obj6;
                        }
                        str10 = str31;
                        obj7 = post$default;
                        i3 = i2;
                        str11 = str3;
                        str12 = str4;
                        str13 = str32;
                        str14 = str2;
                        requestBody = create;
                        niceResponse = (NiceResponse) obj7;
                        str15 = str11;
                        if (niceResponse.getCode() == 441 && niceResponse.getCode() != 401) {
                            str21 = str15;
                            String string = niceResponse.getBody().string();
                            jsonNode = ExtensionsKt.jacksonObjectMapper().readTree(string).get("data");
                            if (jsonNode == null) {
                            }
                            return MainAPIKt.newSearchResponseList$default(CollectionsKt.emptyList(), bool, 2, bool);
                        }
                        movieBoxProvider$search$23.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                        movieBoxProvider$search$23.L$1 = str9;
                        movieBoxProvider$search$23.L$2 = str14;
                        movieBoxProvider$search$23.L$3 = SpillingKt.nullOutSpilledVariable(str12);
                        movieBoxProvider$search$23.L$4 = SpillingKt.nullOutSpilledVariable(str13);
                        movieBoxProvider$search$23.L$5 = SpillingKt.nullOutSpilledVariable(str10);
                        movieBoxProvider$search$23.L$6 = map;
                        movieBoxProvider$search$23.L$7 = requestBody;
                        movieBoxProvider$search$23.L$8 = niceResponse;
                        movieBoxProvider$search$23.I$0 = i3;
                        movieBoxProvider$search$23.label = 3;
                        fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProvider$search$23);
                        if (fetchAnonymousToken != obj6) {
                            return obj6;
                        }
                        str16 = str15;
                        i4 = i3;
                        niceResponse2 = niceResponse;
                        requestBody2 = requestBody;
                        str17 = str13;
                        str18 = str12;
                        str19 = str9;
                        str20 = str14;
                        String str33 = (String) fetchAnonymousToken;
                        str22 = str33;
                        if (str22 != null || StringsKt.isBlank(str22)) {
                            z = true;
                        }
                        if (!z) {
                            i3 = i4;
                            niceResponse = niceResponse2;
                            str21 = str16;
                            String string2 = niceResponse.getBody().string();
                            jsonNode = ExtensionsKt.jacksonObjectMapper().readTree(string2).get("data");
                            if (jsonNode == null) {
                            }
                            return MainAPIKt.newSearchResponseList$default(CollectionsKt.emptyList(), bool, 2, bool);
                        }
                        map.put(obj5, str8 + str33);
                        String generateXTrSignature$default2 = generateXTrSignature$default(this, "POST", "application/json", "application/json; charset=utf-8", str19, str20, false, null, 96, null);
                        map.put(obj4, generateXTrSignature$default2);
                        Requests app2 = MainActivityKt.getApp();
                        movieBoxProvider$search$23.L$0 = SpillingKt.nullOutSpilledVariable(str16);
                        movieBoxProvider$search$23.L$1 = SpillingKt.nullOutSpilledVariable(str19);
                        movieBoxProvider$search$23.L$2 = SpillingKt.nullOutSpilledVariable(str20);
                        movieBoxProvider$search$23.L$3 = SpillingKt.nullOutSpilledVariable(str18);
                        movieBoxProvider$search$23.L$4 = SpillingKt.nullOutSpilledVariable(str17);
                        movieBoxProvider$search$23.L$5 = SpillingKt.nullOutSpilledVariable(str33);
                        movieBoxProvider$search$23.L$6 = SpillingKt.nullOutSpilledVariable(map);
                        movieBoxProvider$search$23.L$7 = SpillingKt.nullOutSpilledVariable(requestBody2);
                        movieBoxProvider$search$23.L$8 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                        movieBoxProvider$search$23.L$9 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default2);
                        movieBoxProvider$search$23.I$0 = i4;
                        movieBoxProvider$search$23.label = 4;
                        Object post$default2 = Requests.post$default(app2, str19, map, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, requestBody2, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$search$23, 65276, (Object) null);
                        if (post$default2 == obj6) {
                            return obj6;
                        }
                        str23 = str33;
                        obj8 = post$default2;
                        str24 = str19;
                        str25 = str20;
                        i5 = i4;
                        map2 = map;
                        requestBody3 = requestBody2;
                        str26 = str16;
                        str27 = str17;
                        str28 = str18;
                        i3 = i5;
                        str21 = str26;
                        niceResponse = (NiceResponse) obj8;
                        String string22 = niceResponse.getBody().string();
                        jsonNode = ExtensionsKt.jacksonObjectMapper().readTree(string22).get("data");
                        if (jsonNode == null) {
                            JsonNode jsonNode3 = jsonNode.get("results");
                            if (jsonNode3 != null) {
                                ArrayList arrayList = new ArrayList();
                                Iterator it = jsonNode3.iterator();
                                while (it.hasNext()) {
                                    String str34 = string22;
                                    JsonNode jsonNode4 = ((JsonNode) it.next()).get("subjects");
                                    if (jsonNode4 == null) {
                                        string22 = str34;
                                    } else {
                                        Iterator it2 = jsonNode4.iterator();
                                        while (it2.hasNext()) {
                                            JsonNode jsonNode5 = jsonNode4;
                                            JsonNode jsonNode6 = (JsonNode) it2.next();
                                            String str35 = str21;
                                            JsonNode jsonNode7 = jsonNode6.get("title");
                                            if (jsonNode7 == null || (asText = jsonNode7.asText()) == null) {
                                                jsonNode4 = jsonNode5;
                                                str21 = str35;
                                                i3 = i3;
                                            } else {
                                                JsonNode jsonNode8 = jsonNode6.get("subjectId");
                                                if (jsonNode8 == null || (asText2 = jsonNode8.asText()) == null) {
                                                    jsonNode4 = jsonNode5;
                                                    str21 = str35;
                                                    i3 = i3;
                                                } else {
                                                    JsonNode jsonNode9 = jsonNode6.get("cover");
                                                    if (jsonNode9 != null) {
                                                        i6 = i3;
                                                        JsonNode jsonNode10 = jsonNode9.get("url");
                                                        if (jsonNode10 != null) {
                                                            str29 = jsonNode10.asText();
                                                            jsonNode2 = jsonNode6.get("subjectType");
                                                            switch (jsonNode2 == null ? jsonNode2.asInt() : 1) {
                                                                case 1:
                                                                    tvType = TvType.Movie;
                                                                    break;
                                                                case 2:
                                                                    tvType = TvType.TvSeries;
                                                                    break;
                                                                default:
                                                                    tvType = TvType.Movie;
                                                                    break;
                                                            }
                                                            arrayList.add(MainAPIKt.newMovieSearchResponse$default(this, asText, asText2, tvType, false, new MovieBoxProvider$$ExternalSyntheticLambda6(str29, jsonNode6), 8, (Object) null));
                                                            jsonNode4 = jsonNode5;
                                                            str21 = str35;
                                                            i3 = i6;
                                                        }
                                                    } else {
                                                        i6 = i3;
                                                    }
                                                    str29 = null;
                                                    jsonNode2 = jsonNode6.get("subjectType");
                                                    switch (jsonNode2 == null ? jsonNode2.asInt() : 1) {
                                                    }
                                                    arrayList.add(MainAPIKt.newMovieSearchResponse$default(this, asText, asText2, tvType, false, new MovieBoxProvider$$ExternalSyntheticLambda6(str29, jsonNode6), 8, (Object) null));
                                                    jsonNode4 = jsonNode5;
                                                    str21 = str35;
                                                    i3 = i6;
                                                }
                                            }
                                        }
                                        string22 = str34;
                                    }
                                }
                                return MainAPIKt.toNewSearchResponseList$default(arrayList, (Boolean) null, 1, (Object) null);
                            }
                            bool = null;
                        } else {
                            bool = null;
                        }
                        return MainAPIKt.newSearchResponseList$default(CollectionsKt.emptyList(), bool, 2, bool);
                    case 1:
                        i2 = movieBoxProvider$search$22.I$0;
                        str6 = (String) movieBoxProvider$search$22.L$4;
                        String str36 = (String) movieBoxProvider$search$22.L$3;
                        String str37 = (String) movieBoxProvider$search$22.L$2;
                        String str38 = (String) movieBoxProvider$search$22.L$1;
                        str3 = (String) movieBoxProvider$search$22.L$0;
                        ResultKt.throwOnFailure(obj8);
                        obj2 = "Authorization";
                        obj3 = "x-tr-signature";
                        str4 = str36;
                        str2 = str37;
                        fetchAnonymousToken$default = obj8;
                        obj = fetchAnonymousToken$default;
                        c = 1;
                        str5 = str38;
                        String str312 = (String) fetchAnonymousToken$default;
                        Pair[] pairArr2 = new Pair[8];
                        z = false;
                        pairArr2[0] = TuplesKt.to("user-agent", "com.community.mbox.in/50020042 (Linux; U; Android 16; en_IN; sdk_gphone64_x86_64; Build/BP22.250325.006; Cronet/133.0.6876.3)");
                        pairArr2[c] = TuplesKt.to("accept", "application/json");
                        pairArr2[2] = TuplesKt.to("content-type", "application/json; charset=utf-8");
                        pairArr2[3] = TuplesKt.to("connection", "keep-alive");
                        pairArr2[4] = TuplesKt.to("x-client-token", str4);
                        obj4 = obj3;
                        pairArr2[5] = TuplesKt.to(obj4, str6);
                        String str322 = str6;
                        pairArr2[6] = TuplesKt.to("x-client-info", "{\"package_name\":\"com.community.mbox.in\",\"version_name\":\"3.0.03.0529.03\",\"version_code\":50020042,\"os\":\"android\",\"os_version\":\"16\",\"device_id\":\"" + this.deviceId + "\",\"install_store\":\"ps\",\"gaid\":\"d7578036d13336cc\",\"brand\":\"google\",\"model\":\"" + randomBrandModel() + "\",\"system_language\":\"en\",\"net\":\"NETWORK_WIFI\",\"region\":\"IN\",\"timezone\":\"Asia/Calcutta\",\"sp_code\":\"\"}");
                        pairArr2[7] = TuplesKt.to("x-client-status", "0");
                        Map mutableMapOf2 = MapsKt.mutableMapOf(pairArr2);
                        str7 = str312;
                        if (str7 != null) {
                            break;
                        }
                        if (str7 != null || StringsKt.isBlank(str7)) {
                        }
                        RequestBody create2 = RequestBody.Companion.create(str2, MediaType.Companion.get("application/json; charset=utf-8"));
                        Requests app3 = MainActivityKt.getApp();
                        movieBoxProvider$search$22.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                        movieBoxProvider$search$22.L$1 = str5;
                        movieBoxProvider$search$22.L$2 = str2;
                        movieBoxProvider$search$22.L$3 = SpillingKt.nullOutSpilledVariable(str4);
                        movieBoxProvider$search$22.L$4 = SpillingKt.nullOutSpilledVariable(str322);
                        movieBoxProvider$search$22.L$5 = SpillingKt.nullOutSpilledVariable(str312);
                        movieBoxProvider$search$22.L$6 = mutableMapOf2;
                        movieBoxProvider$search$22.L$7 = create2;
                        movieBoxProvider$search$22.I$0 = i2;
                        movieBoxProvider$search$22.label = 2;
                        map = mutableMapOf2;
                        str8 = "Bearer ";
                        obj6 = coroutine_suspended;
                        post$default = Requests.post$default(app3, str5, map, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, create2, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProvider$search$22, 65276, (Object) null);
                        str9 = str5;
                        movieBoxProvider$search$23 = movieBoxProvider$search$22;
                        if (post$default != obj6) {
                        }
                        break;
                    case 2:
                        int i7 = movieBoxProvider$search$22.I$0;
                        RequestBody requestBody4 = (RequestBody) movieBoxProvider$search$22.L$7;
                        Map map3 = (Map) movieBoxProvider$search$22.L$6;
                        String str39 = (String) movieBoxProvider$search$22.L$5;
                        str13 = (String) movieBoxProvider$search$22.L$4;
                        String str40 = (String) movieBoxProvider$search$22.L$3;
                        String str41 = (String) movieBoxProvider$search$22.L$2;
                        String str42 = (String) movieBoxProvider$search$22.L$1;
                        str11 = (String) movieBoxProvider$search$22.L$0;
                        ResultKt.throwOnFailure(obj8);
                        str10 = str39;
                        obj7 = obj8;
                        z = false;
                        obj4 = "x-tr-signature";
                        movieBoxProvider$search$23 = movieBoxProvider$search$22;
                        str8 = "Bearer ";
                        str9 = str42;
                        str12 = str40;
                        str14 = str41;
                        obj6 = coroutine_suspended;
                        requestBody = requestBody4;
                        map = map3;
                        obj5 = "Authorization";
                        i3 = i7;
                        niceResponse = (NiceResponse) obj7;
                        str15 = str11;
                        if (niceResponse.getCode() == 441) {
                            break;
                        }
                        movieBoxProvider$search$23.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                        movieBoxProvider$search$23.L$1 = str9;
                        movieBoxProvider$search$23.L$2 = str14;
                        movieBoxProvider$search$23.L$3 = SpillingKt.nullOutSpilledVariable(str12);
                        movieBoxProvider$search$23.L$4 = SpillingKt.nullOutSpilledVariable(str13);
                        movieBoxProvider$search$23.L$5 = SpillingKt.nullOutSpilledVariable(str10);
                        movieBoxProvider$search$23.L$6 = map;
                        movieBoxProvider$search$23.L$7 = requestBody;
                        movieBoxProvider$search$23.L$8 = niceResponse;
                        movieBoxProvider$search$23.I$0 = i3;
                        movieBoxProvider$search$23.label = 3;
                        fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProvider$search$23);
                        if (fetchAnonymousToken != obj6) {
                        }
                        break;
                    case 3:
                        int i8 = movieBoxProvider$search$22.I$0;
                        NiceResponse niceResponse3 = (NiceResponse) movieBoxProvider$search$22.L$8;
                        RequestBody requestBody5 = (RequestBody) movieBoxProvider$search$22.L$7;
                        Map map4 = (Map) movieBoxProvider$search$22.L$6;
                        String str43 = (String) movieBoxProvider$search$22.L$4;
                        String str44 = (String) movieBoxProvider$search$22.L$3;
                        String str45 = (String) movieBoxProvider$search$22.L$2;
                        String str46 = (String) movieBoxProvider$search$22.L$1;
                        String str47 = (String) movieBoxProvider$search$22.L$0;
                        ResultKt.throwOnFailure(obj8);
                        str16 = str47;
                        str17 = str43;
                        niceResponse2 = niceResponse3;
                        str18 = str44;
                        fetchAnonymousToken = obj8;
                        obj6 = coroutine_suspended;
                        i4 = i8;
                        z = false;
                        str19 = str46;
                        obj4 = "x-tr-signature";
                        requestBody2 = requestBody5;
                        map = map4;
                        movieBoxProvider$search$23 = movieBoxProvider$search$22;
                        str8 = "Bearer ";
                        obj5 = "Authorization";
                        str20 = str45;
                        String str332 = (String) fetchAnonymousToken;
                        str22 = str332;
                        if (str22 != null) {
                            break;
                        }
                        z = true;
                        if (!z) {
                        }
                        break;
                    case 4:
                        i5 = movieBoxProvider$search$22.I$0;
                        requestBody3 = (RequestBody) movieBoxProvider$search$22.L$7;
                        map2 = (Map) movieBoxProvider$search$22.L$6;
                        str23 = (String) movieBoxProvider$search$22.L$5;
                        str27 = (String) movieBoxProvider$search$22.L$4;
                        str28 = (String) movieBoxProvider$search$22.L$3;
                        str25 = (String) movieBoxProvider$search$22.L$2;
                        str24 = (String) movieBoxProvider$search$22.L$1;
                        str26 = (String) movieBoxProvider$search$22.L$0;
                        ResultKt.throwOnFailure(obj8);
                        i3 = i5;
                        str21 = str26;
                        niceResponse = (NiceResponse) obj8;
                        String string222 = niceResponse.getBody().string();
                        jsonNode = ExtensionsKt.jacksonObjectMapper().readTree(string222).get("data");
                        if (jsonNode == null) {
                        }
                        return MainAPIKt.newSearchResponseList$default(CollectionsKt.emptyList(), bool, 2, bool);
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        movieBoxProvider$search$2 = new MovieBoxProvider$search$2(this, continuation);
        movieBoxProvider$search$22 = movieBoxProvider$search$2;
        Object obj82 = movieBoxProvider$search$22.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProvider$search$22.label) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x002c  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0036  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object search(@NotNull String str, @NotNull Continuation<? super List<? extends SearchResponse>> continuation) {
        MovieBoxProvider$search$1 movieBoxProvider$search$1;
        Object search;
        if (continuation instanceof MovieBoxProvider$search$1) {
            movieBoxProvider$search$1 = (MovieBoxProvider$search$1) continuation;
            if ((movieBoxProvider$search$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProvider$search$1.label -= Integer.MIN_VALUE;
                Object obj = movieBoxProvider$search$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProvider$search$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        movieBoxProvider$search$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        movieBoxProvider$search$1.label = 1;
                        search = search(str, 1, movieBoxProvider$search$1);
                        if (search == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        break;
                    case 1:
                        ResultKt.throwOnFailure(obj);
                        search = obj;
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                return ((SearchResponseList) search).getItems();
            }
        }
        movieBoxProvider$search$1 = new MovieBoxProvider$search$1(this, continuation);
        Object obj2 = movieBoxProvider$search$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProvider$search$1.label) {
        }
        return ((SearchResponseList) search).getItems();
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
