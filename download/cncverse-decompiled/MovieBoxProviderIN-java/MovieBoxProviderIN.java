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
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MovieBoxProviderIN.kt */
@Metadata(d1 = {"\u0000\u009c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010$\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 \\2\u00020\u0001:\u0002\\]B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u001b\u001a\u00020\u00052\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J\u0010\u0010\u001e\u001a\u00020\u00052\u0006\u0010\u001c\u001a\u00020\u0005H\u0002J\u0019\u0010\u001f\u001a\u00020\u00052\n\b\u0002\u0010 \u001a\u0004\u0018\u00010!H\u0002¢\u0006\u0002\u0010\"J\u0006\u0010%\u001a\u00020\u0005J\u0006\u0010-\u001a\u00020.J>\u0010/\u001a\u00020\u00052\u0006\u00100\u001a\u00020\u00052\b\u00101\u001a\u0004\u0018\u00010\u00052\b\u00102\u001a\u0004\u0018\u00010\u00052\u0006\u00103\u001a\u00020\u00052\b\u00104\u001a\u0004\u0018\u00010\u00052\u0006\u00105\u001a\u00020!H\u0003JS\u00106\u001a\u00020\u00052\u0006\u00100\u001a\u00020\u00052\b\u00101\u001a\u0004\u0018\u00010\u00052\b\u00102\u001a\u0004\u0018\u00010\u00052\u0006\u00103\u001a\u00020\u00052\n\b\u0002\u00104\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u00107\u001a\u00020\u000e2\n\b\u0002\u0010 \u001a\u0004\u0018\u00010!H\u0002¢\u0006\u0002\u00108J\u0012\u00109\u001a\u0004\u0018\u00010\u00052\u0006\u0010:\u001a\u00020\u0005H\u0002J\u001e\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020>2\u0006\u0010?\u001a\u00020@H\u0096@¢\u0006\u0002\u0010AJ\u001c\u0010B\u001a\b\u0012\u0004\u0012\u00020C0,2\u0006\u0010D\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010EJ\u001e\u0010B\u001a\u00020F2\u0006\u0010D\u001a\u00020\u00052\u0006\u0010=\u001a\u00020>H\u0096@¢\u0006\u0002\u0010GJ\u0016\u0010H\u001a\u00020I2\u0006\u00103\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010EJF\u0010J\u001a\u00020\u000e2\u0006\u0010K\u001a\u00020\u00052\u0006\u0010L\u001a\u00020\u000e2\u0012\u0010M\u001a\u000e\u0012\u0004\u0012\u00020O\u0012\u0004\u0012\u00020P0N2\u0012\u0010Q\u001a\u000e\u0012\u0004\u0012\u00020R\u0012\u0004\u0012\u00020P0NH\u0096@¢\u0006\u0002\u0010SJ*\u0010T\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050+2\u0006\u0010U\u001a\u00020\u00052\u0006\u0010V\u001a\u00020WH\u0082@¢\u0006\u0002\u0010XJ\u001a\u0010Y\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010Z\u001a\u00020\u000eH\u0082@¢\u0006\u0002\u0010[R\u001a\u0010\u0004\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u0014\u0010\r\u001a\u00020\u000eX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0011\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0007\"\u0004\b\u0013\u0010\tR\u001a\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u000e\u0010\u0019\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010&\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b'\u0010\u0007R\u0010\u0010(\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020!X\u0082\u000e¢\u0006\u0002\n\u0000R \u0010*\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050,0+X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006^"}, d2 = {"Lcom/cncverse/MovieBoxProviderIN;", "Lcom/lagradost/cloudstream3/MainAPI;", "<init>", "()V", "mainUrl", "", "getMainUrl", "()Ljava/lang/String;", "setMainUrl", "(Ljava/lang/String;)V", "name", "getName", "setName", "hasMainPage", "", "getHasMainPage", "()Z", "lang", "getLang", "setLang", "supportedTypes", "", "Lcom/lagradost/cloudstream3/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "secretKeyDefault", "secretKeyAlt", "md5", "input", "", "reverseString", "generateXClientToken", "hardcodedTimestamp", "", "(Ljava/lang/Long;)Ljava/lang/String;", "random", "Ljava/security/SecureRandom;", "generateDeviceId", "deviceId", "getDeviceId", "cachedGuestToken", "tokenLastFetchMs", "brandModels", "", "", "randomBrandModel", "Lcom/cncverse/MovieBoxProviderIN$BrandModel;", "buildCanonicalString", "method", "accept", "contentType", "url", "body", "timestamp", "generateXTrSignature", "useAltKey", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Long;)Ljava/lang/String;", "extractPolicyResource", "cookie", "getMainPage", "Lcom/lagradost/cloudstream3/HomePageResponse;", "page", "", "request", "Lcom/lagradost/cloudstream3/MainPageRequest;", "(ILcom/lagradost/cloudstream3/MainPageRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "Lcom/lagradost/cloudstream3/SearchResponse;", "query", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lcom/lagradost/cloudstream3/SearchResponseList;", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "load", "Lcom/lagradost/cloudstream3/LoadResponse;", "loadLinks", "data", "isCasting", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchDubDetailPaths", "detailPath", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "(Ljava/lang/String;Lcom/fasterxml/jackson/databind/ObjectMapper;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchAnonymousToken", "forceRefresh", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "BrandModel", "MovieBoxProviderIN_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nMovieBoxProviderIN.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MovieBoxProviderIN.kt\ncom/cncverse/MovieBoxProviderIN\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 4 Maps.kt\nkotlin/collections/MapsKt__MapsKt\n+ 5 _Maps.kt\nkotlin/collections/MapsKt___MapsKt\n+ 6 _Strings.kt\nkotlin/text/StringsKt___StringsKt\n*L\n1#1,1181:1\n1642#2,10:1182\n1915#2:1192\n1642#2,10:1193\n1915#2:1203\n1916#2:1205\n1652#2:1206\n1642#2,10:1207\n1915#2:1217\n1916#2:1219\n1652#2:1220\n1642#2,10:1221\n1915#2:1231\n1916#2:1233\n1652#2:1234\n1916#2:1236\n1652#2:1237\n1642#2,10:1238\n1915#2:1248\n1916#2:1250\n1652#2:1251\n1696#2,8:1252\n1586#2:1260\n1661#2,3:1261\n1915#2,2:1264\n1915#2:1266\n1916#2:1274\n1915#2:1276\n296#2,2:1277\n1916#2:1286\n1#3:1204\n1#3:1218\n1#3:1232\n1#3:1235\n1#3:1249\n1#3:1279\n383#4,7:1267\n221#5:1275\n222#5:1287\n437#6:1280\n513#6,5:1281\n*S KotlinDebug\n*F\n+ 1 MovieBoxProviderIN.kt\ncom/cncverse/MovieBoxProviderIN\n*L\n274#1:1182,10\n274#1:1192\n283#1:1193,10\n283#1:1203\n283#1:1205\n283#1:1206\n285#1:1207,10\n285#1:1217\n285#1:1219\n285#1:1220\n287#1:1221,10\n287#1:1231\n287#1:1233\n287#1:1234\n274#1:1236\n274#1:1237\n439#1:1238,10\n439#1:1248\n439#1:1250\n439#1:1251\n451#1:1252,8\n455#1:1260\n455#1:1261,3\n499#1:1264,2\n526#1:1266\n526#1:1274\n541#1:1276\n543#1:1277,2\n541#1:1286\n283#1:1204\n285#1:1218\n287#1:1232\n274#1:1235\n439#1:1249\n530#1:1267,7\n540#1:1275\n540#1:1287\n560#1:1280\n560#1:1281,5\n*E\n"})
/* loaded from: classes.dex */
public final class MovieBoxProviderIN extends MainAPI {

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
    private String name = "MovieBoxIN";
    private final boolean hasMainPage = true;

    @NotNull
    private String lang = "ta";

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

    /* compiled from: MovieBoxProviderIN.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\n\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000b\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004J\n\u0010\u0012\u001a\u00020\u0003HÖ\u0081\u0004R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0013"}, d2 = {"Lcom/cncverse/MovieBoxProviderIN$BrandModel;", "", "brand", "", "model", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getBrand", "()Ljava/lang/String;", "getModel", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "MovieBoxProviderIN_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
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

    /* compiled from: MovieBoxProviderIN.kt */
    @Metadata(d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t¨\u0006\n"}, d2 = {"Lcom/cncverse/MovieBoxProviderIN$Companion;", "", "<init>", "()V", "context", "Landroid/content/Context;", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "MovieBoxProviderIN_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @Nullable
        public final Context getContext() {
            return MovieBoxProviderIN.context;
        }

        public final void setContext(@Nullable Context context) {
            MovieBoxProviderIN.context = context;
        }
    }

    /* compiled from: MovieBoxProviderIN.kt */
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
        String joinToString$default = !parse.getQueryParameterNames().isEmpty() ? CollectionsKt.joinToString$default(CollectionsKt.sorted(parse.getQueryParameterNames()), "&", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new MovieBoxProviderIN$$ExternalSyntheticLambda2(parse), 30, (Object) null) : "";
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
        return CollectionsKt.joinToString$default(uri.getQueryParameters(str), "&", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new MovieBoxProviderIN$$ExternalSyntheticLambda5(str), 30, (Object) null);
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
        MovieBoxProviderIN$fetchAnonymousToken$1 movieBoxProviderIN$fetchAnonymousToken$1;
        MovieBoxProviderIN$fetchAnonymousToken$1 movieBoxProviderIN$fetchAnonymousToken$12;
        long currentTimeMillis;
        Map mapOf;
        Requests app;
        long j;
        Object obj;
        String str;
        String str2;
        boolean z2;
        if (continuation instanceof MovieBoxProviderIN$fetchAnonymousToken$1) {
            movieBoxProviderIN$fetchAnonymousToken$1 = (MovieBoxProviderIN$fetchAnonymousToken$1) continuation;
            if ((movieBoxProviderIN$fetchAnonymousToken$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderIN$fetchAnonymousToken$1.label -= Integer.MIN_VALUE;
                movieBoxProviderIN$fetchAnonymousToken$12 = movieBoxProviderIN$fetchAnonymousToken$1;
                Object obj2 = movieBoxProviderIN$fetchAnonymousToken$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProviderIN$fetchAnonymousToken$12.label) {
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
                                    movieBoxProviderIN$fetchAnonymousToken$12.L$0 = SpillingKt.nullOutSpilledVariable("https://apig.inmoviebox.com/wefeed-mobile-bff/tab/ranking-list?tabId=0&categoryType=4516404531735022304&page=1&perPage=1");
                                    movieBoxProviderIN$fetchAnonymousToken$12.L$1 = SpillingKt.nullOutSpilledVariable(generateXClientToken);
                                    movieBoxProviderIN$fetchAnonymousToken$12.L$2 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                                    movieBoxProviderIN$fetchAnonymousToken$12.L$3 = SpillingKt.nullOutSpilledVariable(mapOf);
                                    movieBoxProviderIN$fetchAnonymousToken$12.Z$0 = z;
                                    j = currentTimeMillis;
                                    try {
                                        movieBoxProviderIN$fetchAnonymousToken$12.J$0 = j;
                                        movieBoxProviderIN$fetchAnonymousToken$12.label = 1;
                                    } catch (Exception e) {
                                        currentTimeMillis = j;
                                    }
                                } catch (Exception e2) {
                                    currentTimeMillis = currentTimeMillis;
                                }
                                try {
                                    obj = Requests.get$default(app, "https://apig.inmoviebox.com/wefeed-mobile-bff/tab/ranking-list?tabId=0&categoryType=4516404531735022304&page=1&perPage=1", mapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$fetchAnonymousToken$12, 4092, (Object) null);
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
                        currentTimeMillis = movieBoxProviderIN$fetchAnonymousToken$12.J$0;
                        boolean z3 = movieBoxProviderIN$fetchAnonymousToken$12.Z$0;
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
        movieBoxProviderIN$fetchAnonymousToken$1 = new MovieBoxProviderIN$fetchAnonymousToken$1(this, continuation);
        movieBoxProviderIN$fetchAnonymousToken$12 = movieBoxProviderIN$fetchAnonymousToken$1;
        Object obj22 = movieBoxProviderIN$fetchAnonymousToken$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderIN$fetchAnonymousToken$12.label) {
        }
    }

    static /* synthetic */ Object fetchAnonymousToken$default(MovieBoxProviderIN movieBoxProviderIN, boolean z, Continuation continuation, int i, Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        return movieBoxProviderIN.fetchAnonymousToken(z, continuation);
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
        MovieBoxProviderIN$fetchDubDetailPaths$1 movieBoxProviderIN$fetchDubDetailPaths$1;
        MovieBoxProviderIN$fetchDubDetailPaths$1 movieBoxProviderIN$fetchDubDetailPaths$12;
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
        if (continuation instanceof MovieBoxProviderIN$fetchDubDetailPaths$1) {
            movieBoxProviderIN$fetchDubDetailPaths$1 = (MovieBoxProviderIN$fetchDubDetailPaths$1) continuation;
            if ((movieBoxProviderIN$fetchDubDetailPaths$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderIN$fetchDubDetailPaths$1.label -= Integer.MIN_VALUE;
                movieBoxProviderIN$fetchDubDetailPaths$12 = movieBoxProviderIN$fetchDubDetailPaths$1;
                Object obj = movieBoxProviderIN$fetchDubDetailPaths$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProviderIN$fetchDubDetailPaths$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        try {
                            app = MainActivityKt.getApp();
                            str2 = "https://movie-box.co/wefeed-h5api-bff/detail?detailPath=" + str;
                            mapOf = MapsKt.mapOf(new Pair[]{TuplesKt.to("Accept", "application/json"), TuplesKt.to("Origin", "https://movie-box.co"), TuplesKt.to("Referer", "https://mzfi.me/"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Safari/537.36"), TuplesKt.to("x-client-info", "{\"timezone\":\"Asia/Calcutta\"}")});
                            movieBoxProviderIN$fetchDubDetailPaths$12.L$0 = SpillingKt.nullOutSpilledVariable(str);
                            movieBoxProviderIN$fetchDubDetailPaths$12.L$1 = objectMapper;
                            movieBoxProviderIN$fetchDubDetailPaths$12.label = 1;
                        } catch (Exception e) {
                        }
                        try {
                            obj = Requests.get$default(app, str2, mapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$fetchDubDetailPaths$12, 4092, (Object) null);
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
                        objectMapper2 = (ObjectMapper) movieBoxProviderIN$fetchDubDetailPaths$12.L$1;
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
        movieBoxProviderIN$fetchDubDetailPaths$1 = new MovieBoxProviderIN$fetchDubDetailPaths$1(this, continuation);
        movieBoxProviderIN$fetchDubDetailPaths$12 = movieBoxProviderIN$fetchDubDetailPaths$1;
        Object obj2 = movieBoxProviderIN$fetchDubDetailPaths$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderIN$fetchDubDetailPaths$12.label) {
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

    static /* synthetic */ String generateXClientToken$default(MovieBoxProviderIN movieBoxProviderIN, Long l, int i, Object obj) {
        if ((i & 1) != 0) {
            l = null;
        }
        return movieBoxProviderIN.generateXClientToken(l);
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

    static /* synthetic */ String generateXTrSignature$default(MovieBoxProviderIN movieBoxProviderIN, String str, String str2, String str3, String str4, String str5, boolean z, Long l, int i, Object obj) {
        if ((i & 16) != 0) {
            str5 = null;
        }
        if ((i & 32) != 0) {
            z = false;
        }
        if ((i & 64) != 0) {
            l = null;
        }
        return movieBoxProviderIN.generateXTrSignature(str, str2, str3, str4, str5, z, l);
    }

    private static final SearchResponse getMainPage$parseSubject(MovieBoxProviderIN movieBoxProviderIN, JsonNode jsonNode) {
        JsonNode jsonNode2;
        String asText;
        JsonNode jsonNode3;
        String asText2;
        JsonNode jsonNode4;
        if (jsonNode == null || (jsonNode2 = jsonNode.get("subjectId")) == null || (asText = jsonNode2.asText()) == null || (jsonNode3 = jsonNode.get("title")) == null || (asText2 = jsonNode3.asText()) == null) {
            return null;
        }
        JsonNode jsonNode5 = jsonNode.get("cover");
        String asText3 = (jsonNode5 == null || (jsonNode4 = jsonNode5.get("url")) == null) ? null : jsonNode4.asText();
        JsonNode jsonNode6 = jsonNode.get("subjectType");
        Integer valueOf = jsonNode6 != null ? Integer.valueOf(jsonNode6.asInt()) : null;
        return MainAPIKt.newMovieSearchResponse$default(movieBoxProviderIN, asText2, asText, (valueOf != null && valueOf.intValue() == 1) ? TvType.Movie : (valueOf != null && valueOf.intValue() == 2) ? TvType.TvSeries : TvType.Movie, false, new MovieBoxProviderIN$$ExternalSyntheticLambda4(asText3, jsonNode), 8, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$parseSubject$lambda$0(String str, JsonNode jsonNode, MovieSearchResponse movieSearchResponse) {
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
    public static final java.lang.Object loadLinks$emitStream(java.lang.String r81, int r82, int r83, kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> r84, com.cncverse.MovieBoxProviderIN r85, java.lang.String r86, kotlin.jvm.internal.Ref.ObjectRef<java.lang.String> r87, com.fasterxml.jackson.databind.ObjectMapper r88, kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> r89, java.lang.String r90, java.lang.String r91, com.fasterxml.jackson.databind.JsonNode r92, java.lang.String r93, kotlin.coroutines.Continuation<? super kotlin.Unit> r94) {
        /*
            Method dump skipped, instructions count: 4648
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.cncverse.MovieBoxProviderIN.loadLinks$emitStream(java.lang.String, int, int, kotlin.jvm.functions.Function1, com.cncverse.MovieBoxProviderIN, java.lang.String, kotlin.jvm.internal.Ref$ObjectRef, com.fasterxml.jackson.databind.ObjectMapper, kotlin.jvm.functions.Function1, java.lang.String, java.lang.String, com.fasterxml.jackson.databind.JsonNode, java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private final String md5(byte[] input) {
        return ArraysKt.joinToString$default(MessageDigest.getInstance("MD5").digest(input), "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new MovieBoxProviderIN$$ExternalSyntheticLambda7(), 30, (Object) null);
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
        return ArraysKt.joinToString$default(bArr, "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new MovieBoxProviderIN$$ExternalSyntheticLambda6(), 30, (Object) null);
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

    /* JADX WARN: Can't wrap try/catch for region: R(18:1|(2:3|(14:5|6|7|12|(1:167)|16|17|18|(6:33|(9:36|37|38|39|(1:155)(5:43|44|45|(2:47|48)|(1:50)(13:58|59|(2:147|148)(1:61)|(5:63|64|65|31|32)|146|71|72|(0)|82|(0)(0)|80|(0)(0)|55))|51|(0)(0)|55|34)|159|160|31|32)|23|24|25|26|27))|172|6|7|12|(1:14)|167|16|17|18|(1:162)(7:20|33|(1:34)|159|160|31|32)|23|24|25|26|27|(1:(0))) */
    /* JADX WARN: Code restructure failed: missing block: B:163:0x0444, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:164:0x0445, code lost:
    
        r2 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:165:0x0447, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:166:0x0448, code lost:
    
        r2 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x0442, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:65:0x023d. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x003a  */
    /* JADX WARN: Removed duplicated region for block: B:168:0x005c  */
    /* JADX WARN: Removed duplicated region for block: B:36:0x01e1 A[Catch: Exception -> 0x0447, TRY_LEAVE, TryCatch #2 {Exception -> 0x0447, blocks: (B:17:0x019e, B:20:0x01ae, B:33:0x01c0, B:34:0x01db, B:36:0x01e1), top: B:16:0x019e }] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x03fe A[Catch: Exception -> 0x0444, TryCatch #4 {Exception -> 0x0444, blocks: (B:23:0x0437, B:53:0x03fe, B:72:0x03c8, B:74:0x03cf, B:81:0x03dd, B:92:0x0298, B:94:0x02a2, B:100:0x02b4, B:107:0x02d5, B:110:0x02e5, B:112:0x02ed, B:114:0x02f5, B:115:0x030e, B:117:0x0314, B:119:0x0334, B:125:0x0346, B:129:0x035c, B:132:0x036b, B:134:0x0374, B:135:0x038a, B:137:0x0390, B:139:0x03aa, B:144:0x03b4, B:160:0x041f), top: B:18:0x01ac }] */
    /* JADX WARN: Removed duplicated region for block: B:56:0x0405  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x03cf A[Catch: Exception -> 0x0444, TryCatch #4 {Exception -> 0x0444, blocks: (B:23:0x0437, B:53:0x03fe, B:72:0x03c8, B:74:0x03cf, B:81:0x03dd, B:92:0x0298, B:94:0x02a2, B:100:0x02b4, B:107:0x02d5, B:110:0x02e5, B:112:0x02ed, B:114:0x02f5, B:115:0x030e, B:117:0x0314, B:119:0x0334, B:125:0x0346, B:129:0x035c, B:132:0x036b, B:134:0x0374, B:135:0x038a, B:137:0x0390, B:139:0x03aa, B:144:0x03b4, B:160:0x041f), top: B:18:0x01ac }] */
    /* JADX WARN: Removed duplicated region for block: B:79:0x03db  */
    /* JADX WARN: Removed duplicated region for block: B:81:0x03dd A[Catch: Exception -> 0x0444, TryCatch #4 {Exception -> 0x0444, blocks: (B:23:0x0437, B:53:0x03fe, B:72:0x03c8, B:74:0x03cf, B:81:0x03dd, B:92:0x0298, B:94:0x02a2, B:100:0x02b4, B:107:0x02d5, B:110:0x02e5, B:112:0x02ed, B:114:0x02f5, B:115:0x030e, B:117:0x0314, B:119:0x0334, B:125:0x0346, B:129:0x035c, B:132:0x036b, B:134:0x0374, B:135:0x038a, B:137:0x0390, B:139:0x03aa, B:144:0x03b4, B:160:0x041f), top: B:18:0x01ac }] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0032  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getMainPage(int i, @NotNull MainPageRequest mainPageRequest, @NotNull Continuation<? super HomePageResponse> continuation) {
        MovieBoxProviderIN$getMainPage$1 movieBoxProviderIN$getMainPage$1;
        MovieBoxProviderIN$getMainPage$1 movieBoxProviderIN$getMainPage$12;
        String str;
        int i2;
        Object obj;
        ResponseBody body;
        String str2;
        Boolean bool;
        ArrayList arrayList;
        JsonNode jsonNode;
        String str3;
        Iterable<JsonNode> iterable;
        JsonNode jsonNode2;
        String str4;
        NiceResponse niceResponse;
        int i3;
        String str5;
        HomePageList homePageList;
        ArrayList arrayList2;
        String asText;
        String asText2;
        ArrayList arrayList3;
        ArrayList arrayList4;
        boolean z;
        ArrayList arrayList5;
        ArrayList arrayList6;
        String str6 = "banner";
        if (continuation instanceof MovieBoxProviderIN$getMainPage$1) {
            movieBoxProviderIN$getMainPage$1 = (MovieBoxProviderIN$getMainPage$1) continuation;
            if ((movieBoxProviderIN$getMainPage$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderIN$getMainPage$1.label -= Integer.MIN_VALUE;
                movieBoxProviderIN$getMainPage$12 = movieBoxProviderIN$getMainPage$1;
                Object obj2 = movieBoxProviderIN$getMainPage$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProviderIN$getMainPage$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj2);
                        DonationManager.INSTANCE.checkAndShow(getName());
                        String str7 = getMainUrl() + "/wefeed-mobile-bff/tab-operating?page=1&tabId=0&version=";
                        String generateXClientToken$default = generateXClientToken$default(this, null, 1, null);
                        str = "items";
                        String generateXTrSignature$default = generateXTrSignature$default(this, "GET", "application/json", "application/json", str7, null, false, null, 112, null);
                        Map mapOf = MapsKt.mapOf(new Pair[]{TuplesKt.to("user-agent", "com.community.mbox.in/50020042 (Linux; U; Android 16; en_IN; sdk_gphone64_x86_64; Build/BP22.250325.006; Cronet/133.0.6876.3)"), TuplesKt.to("accept", "application/json"), TuplesKt.to("content-type", "application/json"), TuplesKt.to("connection", "keep-alive"), TuplesKt.to("x-client-token", generateXClientToken$default), TuplesKt.to("x-tr-signature", generateXTrSignature$default), TuplesKt.to("x-client-info", "{\"package_name\":\"com.community.mbox.in\",\"version_name\":\"3.0.03.0529.03\",\"version_code\":50020042,\"os\":\"android\",\"os_version\":\"16\",\"device_id\":\"" + this.deviceId + "\",\"install_store\":\"ps\",\"gaid\":\"d7578036d13336cc\",\"brand\":\"google\",\"model\":\"" + randomBrandModel() + "\",\"system_language\":\"en\",\"net\":\"NETWORK_WIFI\",\"region\":\"IN\",\"timezone\":\"Asia/Calcutta\",\"sp_code\":\"\"}"), TuplesKt.to("x-client-status", "0"), TuplesKt.to("x-play-mode", "2")});
                        Requests app = MainActivityKt.getApp();
                        movieBoxProviderIN$getMainPage$12.L$0 = SpillingKt.nullOutSpilledVariable(mainPageRequest);
                        movieBoxProviderIN$getMainPage$12.L$1 = SpillingKt.nullOutSpilledVariable(str7);
                        movieBoxProviderIN$getMainPage$12.L$2 = SpillingKt.nullOutSpilledVariable(generateXClientToken$default);
                        movieBoxProviderIN$getMainPage$12.L$3 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProviderIN$getMainPage$12.L$4 = SpillingKt.nullOutSpilledVariable(mapOf);
                        i2 = i;
                        movieBoxProviderIN$getMainPage$12.I$0 = i2;
                        movieBoxProviderIN$getMainPage$12.label = 1;
                        obj = obj2;
                        Object obj3 = Requests.get$default(app, str7, mapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$getMainPage$12, 4092, (Object) null);
                        if (obj3 == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        obj2 = obj3;
                        break;
                    case 1:
                        int i4 = movieBoxProviderIN$getMainPage$12.I$0;
                        ResultKt.throwOnFailure(obj2);
                        i2 = i4;
                        str = "items";
                        obj = obj2;
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                NiceResponse niceResponse2 = (NiceResponse) obj2;
                body = niceResponse2.getBody();
                if (body != null || (str2 = body.string()) == null) {
                    str2 = "";
                }
                jsonNode = ExtensionsKt.jacksonObjectMapper().readTree(str2).get("data");
                if (jsonNode != null && (iterable = jsonNode.get((str3 = str))) != null) {
                    ArrayList arrayList7 = new ArrayList();
                    for (JsonNode jsonNode3 : iterable) {
                        Object obj4 = obj;
                        try {
                            jsonNode2 = jsonNode3.get("title");
                        } catch (Exception e) {
                            e = e;
                            bool = null;
                        }
                        if (jsonNode2 == null || (asText = jsonNode2.asText()) == null) {
                            str4 = str6;
                            niceResponse = niceResponse2;
                            i3 = i2;
                            str5 = str2;
                        } else {
                            niceResponse = niceResponse2;
                            try {
                                if (StringsKt.equals(asText, str6, true)) {
                                    asText = "🔥Top Picks";
                                }
                            } catch (Exception e2) {
                                e = e2;
                                bool = null;
                            }
                            if (asText == null) {
                                str4 = str6;
                                i3 = i2;
                                str5 = str2;
                            } else {
                                String str8 = asText;
                                JsonNode jsonNode4 = jsonNode3.get("type");
                                if (jsonNode4 != null) {
                                    try {
                                        asText2 = jsonNode4.asText();
                                    } catch (Exception e3) {
                                        e = e3;
                                        bool = null;
                                        e.printStackTrace();
                                        arrayList = CollectionsKt.emptyList();
                                        return MainAPIKt.newHomePageResponse$default(arrayList, bool, 2, bool);
                                    }
                                } else {
                                    asText2 = null;
                                }
                                if (asText2 != null) {
                                    switch (asText2.hashCode()) {
                                        case -83035752:
                                            str4 = str6;
                                            i3 = i2;
                                            str5 = str2;
                                            if (asText2.equals("SUBJECTS_MOVIE")) {
                                                Iterable iterable2 = jsonNode3.get("subjects");
                                                if (iterable2 != null) {
                                                    Iterable iterable3 = iterable2;
                                                    ArrayList arrayList8 = new ArrayList();
                                                    Iterator it = iterable3.iterator();
                                                    while (it.hasNext()) {
                                                        Iterable iterable4 = iterable3;
                                                        SearchResponse mainPage$parseSubject = getMainPage$parseSubject(this, (JsonNode) it.next());
                                                        if (mainPage$parseSubject != null) {
                                                            arrayList8.add(mainPage$parseSubject);
                                                        }
                                                        iterable3 = iterable4;
                                                    }
                                                    arrayList3 = arrayList8;
                                                } else {
                                                    arrayList3 = null;
                                                }
                                                arrayList4 = arrayList3;
                                                if (arrayList4 != null) {
                                                    z = false;
                                                    if (z) {
                                                    }
                                                    if (homePageList == null) {
                                                    }
                                                    niceResponse2 = niceResponse;
                                                    arrayList7 = arrayList2;
                                                    obj = obj4;
                                                    i2 = i3;
                                                    str2 = str5;
                                                    str6 = str4;
                                                }
                                                z = true;
                                                if (z) {
                                                }
                                                if (homePageList == null) {
                                                }
                                                niceResponse2 = niceResponse;
                                                arrayList7 = arrayList2;
                                                obj = obj4;
                                                i2 = i3;
                                                str2 = str5;
                                                str6 = str4;
                                            } else {
                                                arrayList3 = null;
                                                arrayList4 = arrayList3;
                                                if (arrayList4 != null && !arrayList4.isEmpty()) {
                                                    z = false;
                                                    homePageList = z ? null : new HomePageList(str8, arrayList3, false, 4, (DefaultConstructorMarker) null);
                                                    if (homePageList == null) {
                                                        arrayList2 = arrayList7;
                                                        arrayList2.add(homePageList);
                                                    } else {
                                                        arrayList2 = arrayList7;
                                                    }
                                                    niceResponse2 = niceResponse;
                                                    arrayList7 = arrayList2;
                                                    obj = obj4;
                                                    i2 = i3;
                                                    str2 = str5;
                                                    str6 = str4;
                                                }
                                                z = true;
                                                if (z) {
                                                }
                                                if (homePageList == null) {
                                                }
                                                niceResponse2 = niceResponse;
                                                arrayList7 = arrayList2;
                                                obj = obj4;
                                                i2 = i3;
                                                str2 = str5;
                                                str6 = str4;
                                            }
                                        case 1951953708:
                                            i3 = i2;
                                            str5 = str2;
                                            if (asText2.equals("BANNER")) {
                                                JsonNode jsonNode5 = jsonNode3.get(str6);
                                                if (jsonNode5 != null) {
                                                    Iterable iterable5 = jsonNode5.get("banners");
                                                    if (iterable5 != null) {
                                                        Iterable iterable6 = iterable5;
                                                        ArrayList arrayList9 = new ArrayList();
                                                        Iterator it2 = iterable6.iterator();
                                                        while (it2.hasNext()) {
                                                            String str9 = str6;
                                                            Iterable iterable7 = iterable6;
                                                            SearchResponse mainPage$parseSubject2 = getMainPage$parseSubject(this, ((JsonNode) it2.next()).get("subject"));
                                                            if (mainPage$parseSubject2 != null) {
                                                                arrayList5 = arrayList9;
                                                                arrayList5.add(mainPage$parseSubject2);
                                                            } else {
                                                                arrayList5 = arrayList9;
                                                            }
                                                            arrayList9 = arrayList5;
                                                            iterable6 = iterable7;
                                                            str6 = str9;
                                                        }
                                                        str4 = str6;
                                                        arrayList3 = arrayList9;
                                                        arrayList4 = arrayList3;
                                                        if (arrayList4 != null) {
                                                        }
                                                        z = true;
                                                        if (z) {
                                                        }
                                                        if (homePageList == null) {
                                                        }
                                                        niceResponse2 = niceResponse;
                                                        arrayList7 = arrayList2;
                                                        obj = obj4;
                                                        i2 = i3;
                                                        str2 = str5;
                                                        str6 = str4;
                                                    } else {
                                                        str4 = str6;
                                                    }
                                                } else {
                                                    str4 = str6;
                                                }
                                                arrayList3 = null;
                                                arrayList4 = arrayList3;
                                                if (arrayList4 != null) {
                                                }
                                                z = true;
                                                if (z) {
                                                }
                                                if (homePageList == null) {
                                                }
                                                niceResponse2 = niceResponse;
                                                arrayList7 = arrayList2;
                                                obj = obj4;
                                                i2 = i3;
                                                str2 = str5;
                                                str6 = str4;
                                            } else {
                                                str4 = str6;
                                                arrayList3 = null;
                                                arrayList4 = arrayList3;
                                                if (arrayList4 != null) {
                                                }
                                                z = true;
                                                if (z) {
                                                }
                                                if (homePageList == null) {
                                                }
                                                niceResponse2 = niceResponse;
                                                arrayList7 = arrayList2;
                                                obj = obj4;
                                                i2 = i3;
                                                str2 = str5;
                                                str6 = str4;
                                            }
                                            break;
                                        case 1999208305:
                                            i3 = i2;
                                            try {
                                                if (asText2.equals("CUSTOM")) {
                                                    JsonNode jsonNode6 = jsonNode3.get("customData");
                                                    if (jsonNode6 != null) {
                                                        Iterable iterable8 = jsonNode6.get(str3);
                                                        if (iterable8 != null) {
                                                            Iterable iterable9 = iterable8;
                                                            ArrayList arrayList10 = new ArrayList();
                                                            Iterator it3 = iterable9.iterator();
                                                            while (it3.hasNext()) {
                                                                Iterable iterable10 = iterable9;
                                                                String str10 = str2;
                                                                SearchResponse mainPage$parseSubject3 = getMainPage$parseSubject(this, ((JsonNode) it3.next()).get("subject"));
                                                                if (mainPage$parseSubject3 != null) {
                                                                    arrayList6 = arrayList10;
                                                                    arrayList6.add(mainPage$parseSubject3);
                                                                } else {
                                                                    arrayList6 = arrayList10;
                                                                }
                                                                arrayList10 = arrayList6;
                                                                str2 = str10;
                                                                iterable9 = iterable10;
                                                            }
                                                            str5 = str2;
                                                            str4 = str6;
                                                            arrayList3 = arrayList10;
                                                            arrayList4 = arrayList3;
                                                            if (arrayList4 != null) {
                                                            }
                                                            z = true;
                                                            if (z) {
                                                            }
                                                            if (homePageList == null) {
                                                            }
                                                            niceResponse2 = niceResponse;
                                                            arrayList7 = arrayList2;
                                                            obj = obj4;
                                                            i2 = i3;
                                                            str2 = str5;
                                                            str6 = str4;
                                                        } else {
                                                            str5 = str2;
                                                        }
                                                    } else {
                                                        str5 = str2;
                                                    }
                                                    str4 = str6;
                                                    arrayList3 = null;
                                                    arrayList4 = arrayList3;
                                                    if (arrayList4 != null) {
                                                    }
                                                    z = true;
                                                    if (z) {
                                                    }
                                                    if (homePageList == null) {
                                                    }
                                                    niceResponse2 = niceResponse;
                                                    arrayList7 = arrayList2;
                                                    obj = obj4;
                                                    i2 = i3;
                                                    str2 = str5;
                                                    str6 = str4;
                                                } else {
                                                    str4 = str6;
                                                    str5 = str2;
                                                    arrayList3 = null;
                                                    arrayList4 = arrayList3;
                                                    if (arrayList4 != null) {
                                                    }
                                                    z = true;
                                                    if (z) {
                                                    }
                                                    if (homePageList == null) {
                                                    }
                                                    niceResponse2 = niceResponse;
                                                    arrayList7 = arrayList2;
                                                    obj = obj4;
                                                    i2 = i3;
                                                    str2 = str5;
                                                    str6 = str4;
                                                }
                                            } catch (Exception e4) {
                                                e = e4;
                                                bool = null;
                                                e.printStackTrace();
                                                arrayList = CollectionsKt.emptyList();
                                                return MainAPIKt.newHomePageResponse$default(arrayList, bool, 2, bool);
                                            }
                                            break;
                                    }
                                    return MainAPIKt.newHomePageResponse$default(arrayList, bool, 2, bool);
                                }
                                str4 = str6;
                                i3 = i2;
                                str5 = str2;
                                arrayList3 = null;
                                arrayList4 = arrayList3;
                                if (arrayList4 != null) {
                                }
                                z = true;
                                if (z) {
                                }
                                if (homePageList == null) {
                                }
                                niceResponse2 = niceResponse;
                                arrayList7 = arrayList2;
                                obj = obj4;
                                i2 = i3;
                                str2 = str5;
                                str6 = str4;
                            }
                        }
                        homePageList = null;
                        if (homePageList == null) {
                        }
                        niceResponse2 = niceResponse;
                        arrayList7 = arrayList2;
                        obj = obj4;
                        i2 = i3;
                        str2 = str5;
                        str6 = str4;
                    }
                    arrayList = arrayList7;
                    bool = null;
                    return MainAPIKt.newHomePageResponse$default(arrayList, bool, 2, bool);
                }
                bool = null;
                return MainAPIKt.newHomePageResponse$default(CollectionsKt.emptyList(), (Boolean) null, 2, (Object) null);
            }
        }
        movieBoxProviderIN$getMainPage$1 = new MovieBoxProviderIN$getMainPage$1(this, continuation);
        movieBoxProviderIN$getMainPage$12 = movieBoxProviderIN$getMainPage$1;
        Object obj22 = movieBoxProviderIN$getMainPage$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderIN$getMainPage$12.label) {
        }
        NiceResponse niceResponse22 = (NiceResponse) obj22;
        body = niceResponse22.getBody();
        if (body != null) {
        }
        str2 = "";
        jsonNode = ExtensionsKt.jacksonObjectMapper().readTree(str2).get("data");
        if (jsonNode != null) {
            ArrayList arrayList72 = new ArrayList();
            while (r22.hasNext()) {
            }
            arrayList = arrayList72;
            bool = null;
            return MainAPIKt.newHomePageResponse$default(arrayList, bool, 2, bool);
        }
        bool = null;
        return MainAPIKt.newHomePageResponse$default(CollectionsKt.emptyList(), (Boolean) null, 2, (Object) null);
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
        MovieBoxProviderIN$load$1 movieBoxProviderIN$load$1;
        MovieBoxProviderIN$load$1 movieBoxProviderIN$load$12;
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
        MovieBoxProviderIN$load$1 movieBoxProviderIN$load$13;
        NiceResponse niceResponse;
        Object fetchAnonymousToken;
        String str14;
        Object obj6;
        NiceResponse niceResponse2;
        String str15;
        String str16;
        String str17;
        Map map2;
        MovieBoxProviderIN$load$1 movieBoxProviderIN$load$14;
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
        MovieBoxProviderIN$load$1 movieBoxProviderIN$load$15;
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
        MovieBoxProviderIN$load$1 movieBoxProviderIN$load$16;
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
        MovieBoxProviderIN movieBoxProviderIN;
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
        MovieBoxProviderIN movieBoxProviderIN2;
        Continuation<? super LoadResponse> continuation3;
        NiceResponse niceResponse5;
        Continuation<? super LoadResponse> continuation4;
        String str95;
        MovieBoxProviderIN movieBoxProviderIN3;
        JsonNode jsonNode22;
        Map map6;
        Object obj20;
        int i11;
        if (continuation instanceof MovieBoxProviderIN$load$1) {
            movieBoxProviderIN$load$1 = (MovieBoxProviderIN$load$1) continuation;
            if ((movieBoxProviderIN$load$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderIN$load$1.label -= Integer.MIN_VALUE;
                movieBoxProviderIN$load$12 = movieBoxProviderIN$load$1;
                Object obj21 = movieBoxProviderIN$load$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                String str96 = "title";
                String str97 = "data";
                switch (movieBoxProviderIN$load$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj21);
                        MatchResult find$default = Regex.find$default(new Regex("subjectId=([^&]+)"), str, 0, 2, (Object) null);
                        if (find$default == null || (groupValues = find$default.getGroupValues()) == null || (substringAfterLast$default = (String) groupValues.get(1)) == null) {
                            substringAfterLast$default = StringsKt.substringAfterLast$default(str, '/', (String) null, 2, (Object) null);
                        }
                        String str98 = getMainUrl() + "/wefeed-mobile-bff/subject-api/get?subjectId=" + substringAfterLast$default;
                        movieBoxProviderIN$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        movieBoxProviderIN$load$12.L$1 = substringAfterLast$default;
                        movieBoxProviderIN$load$12.L$2 = str98;
                        i = 1;
                        movieBoxProviderIN$load$12.label = 1;
                        l = null;
                        fetchAnonymousToken$default = fetchAnonymousToken$default(this, false, movieBoxProviderIN$load$12, 1, null);
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
                        movieBoxProviderIN$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                        str8 = str100;
                        movieBoxProviderIN$load$12.L$1 = str8;
                        movieBoxProviderIN$load$12.L$2 = str99;
                        movieBoxProviderIN$load$12.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                        movieBoxProviderIN$load$12.L$4 = SpillingKt.nullOutSpilledVariable(generateXClientToken$default);
                        movieBoxProviderIN$load$12.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProviderIN$load$12.L$6 = mutableMapOf;
                        movieBoxProviderIN$load$12.label = 2;
                        str9 = "description";
                        obj = obj21;
                        str10 = str96;
                        str11 = generateXClientToken$default;
                        str12 = "Bearer ";
                        obj2 = "x-tr-signature";
                        obj3 = "Authorization";
                        obj4 = coroutine_suspended;
                        i2 = 3;
                        obj5 = Requests.get$default(app, str99, mutableMapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$load$12, 4092, (Object) null);
                        map = mutableMapOf;
                        str13 = str99;
                        movieBoxProviderIN$load$13 = movieBoxProviderIN$load$12;
                        if (obj5 == obj4) {
                            return obj4;
                        }
                        niceResponse = (NiceResponse) obj5;
                        if (niceResponse.getCode() != 441 || niceResponse.getCode() == 401) {
                            movieBoxProviderIN$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                            movieBoxProviderIN$load$13.L$1 = str8;
                            movieBoxProviderIN$load$13.L$2 = str13;
                            movieBoxProviderIN$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                            movieBoxProviderIN$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                            movieBoxProviderIN$load$13.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                            movieBoxProviderIN$load$13.L$6 = map;
                            movieBoxProviderIN$load$13.L$7 = niceResponse;
                            movieBoxProviderIN$load$13.label = i2;
                            fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProviderIN$load$13);
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
                                movieBoxProviderIN$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                                movieBoxProviderIN$load$13.L$1 = str14;
                                movieBoxProviderIN$load$13.L$2 = str102;
                                movieBoxProviderIN$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str101);
                                movieBoxProviderIN$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                movieBoxProviderIN$load$13.L$5 = SpillingKt.nullOutSpilledVariable(str16);
                                movieBoxProviderIN$load$13.L$6 = map2;
                                movieBoxProviderIN$load$13.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                                movieBoxProviderIN$load$13.L$8 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default2);
                                movieBoxProviderIN$load$13.label = 4;
                                str9 = str9;
                                MovieBoxProviderIN$load$1 movieBoxProviderIN$load$17 = movieBoxProviderIN$load$13;
                                str20 = str10;
                                str18 = str102;
                                String str103 = str14;
                                obj4 = obj22;
                                Object obj23 = Requests.get$default(app2, str18, map2, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$load$17, 4092, (Object) null);
                                movieBoxProviderIN$load$14 = movieBoxProviderIN$load$17;
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
                                                            movieBoxProviderIN$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                                                            movieBoxProviderIN$load$14.L$1 = str19;
                                                            movieBoxProviderIN$load$14.L$2 = str18;
                                                            movieBoxProviderIN$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                                            movieBoxProviderIN$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                            movieBoxProviderIN$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                            movieBoxProviderIN$load$14.L$6 = map2;
                                                            movieBoxProviderIN$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                            movieBoxProviderIN$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                                                            movieBoxProviderIN$load$14.L$9 = jacksonObjectMapper;
                                                            movieBoxProviderIN$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                            movieBoxProviderIN$load$14.L$11 = jsonNode23;
                                                            movieBoxProviderIN$load$14.L$12 = substringBefore$default;
                                                            movieBoxProviderIN$load$14.L$13 = asText10;
                                                            movieBoxProviderIN$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str34);
                                                            movieBoxProviderIN$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str27);
                                                            movieBoxProviderIN$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                                                            movieBoxProviderIN$load$14.L$17 = num;
                                                            movieBoxProviderIN$load$14.L$18 = num2;
                                                            String str107 = str32;
                                                            movieBoxProviderIN$load$14.L$19 = str107;
                                                            movieBoxProviderIN$load$14.L$20 = str31;
                                                            movieBoxProviderIN$load$14.L$21 = list;
                                                            List list17 = list2;
                                                            movieBoxProviderIN$load$14.L$22 = list17;
                                                            movieBoxProviderIN$load$14.L$23 = num3;
                                                            movieBoxProviderIN$load$14.L$24 = tvType;
                                                            Integer num23 = num3;
                                                            movieBoxProviderIN$load$14.I$0 = i3;
                                                            movieBoxProviderIN$load$14.label = 5;
                                                            identifyID = MovieBoxProviderINKt.identifyID(str33, intOrNull3, d, movieBoxProviderIN$load$14);
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
                                                            movieBoxProviderIN$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str38);
                                                            movieBoxProviderIN$load$14.L$1 = str41;
                                                            movieBoxProviderIN$load$14.L$2 = str18;
                                                            movieBoxProviderIN$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                                            movieBoxProviderIN$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                            movieBoxProviderIN$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                            movieBoxProviderIN$load$14.L$6 = map2;
                                                            movieBoxProviderIN$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                            movieBoxProviderIN$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                                                            movieBoxProviderIN$load$14.L$9 = jacksonObjectMapper;
                                                            movieBoxProviderIN$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                            movieBoxProviderIN$load$14.L$11 = jsonNode3;
                                                            movieBoxProviderIN$load$14.L$12 = str36;
                                                            movieBoxProviderIN$load$14.L$13 = str40;
                                                            movieBoxProviderIN$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                            movieBoxProviderIN$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                            movieBoxProviderIN$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                                                            movieBoxProviderIN$load$14.L$17 = num4;
                                                            movieBoxProviderIN$load$14.L$18 = num6;
                                                            movieBoxProviderIN$load$14.L$19 = str35;
                                                            movieBoxProviderIN$load$14.L$20 = str39;
                                                            movieBoxProviderIN$load$14.L$21 = list4;
                                                            movieBoxProviderIN$load$14.L$22 = list3;
                                                            movieBoxProviderIN$load$14.L$23 = num5;
                                                            movieBoxProviderIN$load$14.L$24 = tvType7;
                                                            JsonNode jsonNode38 = jsonNode3;
                                                            movieBoxProviderIN$load$14.L$25 = num24;
                                                            movieBoxProviderIN$load$14.L$26 = str108;
                                                            movieBoxProviderIN$load$14.I$0 = i4;
                                                            movieBoxProviderIN$load$14.label = 6;
                                                            Integer num25 = num5;
                                                            List list18 = list4;
                                                            String str109 = str35;
                                                            String str110 = str96;
                                                            List list19 = list3;
                                                            String str111 = str41;
                                                            String str112 = str39;
                                                            String str113 = str36;
                                                            movieBoxProviderIN$load$15 = movieBoxProviderIN$load$14;
                                                            String str114 = str26;
                                                            num7 = num4;
                                                            obj14 = obj13;
                                                            fetchTmdbLogoUrl = MovieBoxProviderINKt.fetchTmdbLogoUrl("https://api.themoviedb.org/3", "98ae14df2b8d8f8f8136499daf79f0e0", tvType7, num24, "en", movieBoxProviderIN$load$15);
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
                                                                movieBoxProviderIN$load$15.L$0 = SpillingKt.nullOutSpilledVariable(str38);
                                                                movieBoxProviderIN$load$15.L$1 = str49;
                                                                movieBoxProviderIN$load$15.L$2 = str18;
                                                                movieBoxProviderIN$load$15.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                                                movieBoxProviderIN$load$15.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                                movieBoxProviderIN$load$15.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                                movieBoxProviderIN$load$15.L$6 = map2;
                                                                movieBoxProviderIN$load$15.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                                movieBoxProviderIN$load$15.L$8 = SpillingKt.nullOutSpilledVariable(str44);
                                                                movieBoxProviderIN$load$15.L$9 = jacksonObjectMapper;
                                                                movieBoxProviderIN$load$15.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                                movieBoxProviderIN$load$15.L$11 = jsonNode5;
                                                                movieBoxProviderIN$load$15.L$12 = str43;
                                                                movieBoxProviderIN$load$15.L$13 = str40;
                                                                movieBoxProviderIN$load$15.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                                movieBoxProviderIN$load$15.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                                movieBoxProviderIN$load$15.L$16 = SpillingKt.nullOutSpilledVariable(str47);
                                                                movieBoxProviderIN$load$15.L$17 = num7;
                                                                movieBoxProviderIN$load$15.L$18 = num6;
                                                                movieBoxProviderIN$load$15.L$19 = str46;
                                                                movieBoxProviderIN$load$15.L$20 = str48;
                                                                movieBoxProviderIN$load$15.L$21 = list6;
                                                                movieBoxProviderIN$load$15.L$22 = list5;
                                                                movieBoxProviderIN$load$15.L$23 = num9;
                                                                movieBoxProviderIN$load$15.L$24 = tvType8;
                                                                Integer num26 = num9;
                                                                movieBoxProviderIN$load$15.L$25 = num8;
                                                                String str116 = str45;
                                                                movieBoxProviderIN$load$15.L$26 = str116;
                                                                String str117 = str48;
                                                                movieBoxProviderIN$load$15.L$27 = str61;
                                                                movieBoxProviderIN$load$15.I$0 = i6;
                                                                movieBoxProviderIN$load$15.label = 7;
                                                                fetchMetaData = MovieBoxProviderINKt.fetchMetaData(str116, tvType8, movieBoxProviderIN$load$15);
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
                                                                    MovieBoxProviderIN$load$7 movieBoxProviderIN$load$7 = new MovieBoxProviderIN$load$7(str51, str118, str119, str54, str59, str120, str52, num10, list7, list8, asText15, num11, num12, str55, num13, null);
                                                                    movieBoxProviderIN$load$15.L$0 = SpillingKt.nullOutSpilledVariable(str53);
                                                                    movieBoxProviderIN$load$15.L$1 = SpillingKt.nullOutSpilledVariable(str56);
                                                                    movieBoxProviderIN$load$15.L$2 = SpillingKt.nullOutSpilledVariable(str58);
                                                                    movieBoxProviderIN$load$15.L$3 = SpillingKt.nullOutSpilledVariable(str60);
                                                                    movieBoxProviderIN$load$15.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                                    movieBoxProviderIN$load$15.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                                    movieBoxProviderIN$load$15.L$6 = SpillingKt.nullOutSpilledVariable(map2);
                                                                    movieBoxProviderIN$load$15.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                                    movieBoxProviderIN$load$15.L$8 = SpillingKt.nullOutSpilledVariable(str44);
                                                                    movieBoxProviderIN$load$15.L$9 = SpillingKt.nullOutSpilledVariable(jacksonObjectMapper);
                                                                    movieBoxProviderIN$load$15.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                                    movieBoxProviderIN$load$15.L$11 = SpillingKt.nullOutSpilledVariable(jsonNode6);
                                                                    movieBoxProviderIN$load$15.L$12 = SpillingKt.nullOutSpilledVariable(str57);
                                                                    movieBoxProviderIN$load$15.L$13 = SpillingKt.nullOutSpilledVariable(str52);
                                                                    movieBoxProviderIN$load$15.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                                    movieBoxProviderIN$load$15.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                                    movieBoxProviderIN$load$15.L$16 = SpillingKt.nullOutSpilledVariable(str47);
                                                                    movieBoxProviderIN$load$15.L$17 = SpillingKt.nullOutSpilledVariable(num11);
                                                                    movieBoxProviderIN$load$15.L$18 = SpillingKt.nullOutSpilledVariable(num10);
                                                                    movieBoxProviderIN$load$15.L$19 = SpillingKt.nullOutSpilledVariable(str51);
                                                                    movieBoxProviderIN$load$15.L$20 = SpillingKt.nullOutSpilledVariable(str54);
                                                                    movieBoxProviderIN$load$15.L$21 = SpillingKt.nullOutSpilledVariable(list8);
                                                                    movieBoxProviderIN$load$15.L$22 = SpillingKt.nullOutSpilledVariable(list7);
                                                                    movieBoxProviderIN$load$15.L$23 = SpillingKt.nullOutSpilledVariable(num12);
                                                                    movieBoxProviderIN$load$15.L$24 = SpillingKt.nullOutSpilledVariable(tvType4);
                                                                    movieBoxProviderIN$load$15.L$25 = SpillingKt.nullOutSpilledVariable(num13);
                                                                    movieBoxProviderIN$load$15.L$26 = SpillingKt.nullOutSpilledVariable(str55);
                                                                    movieBoxProviderIN$load$15.L$27 = SpillingKt.nullOutSpilledVariable(str59);
                                                                    movieBoxProviderIN$load$15.L$28 = SpillingKt.nullOutSpilledVariable(jsonNode7);
                                                                    movieBoxProviderIN$load$15.L$29 = SpillingKt.nullOutSpilledVariable(list20);
                                                                    movieBoxProviderIN$load$15.L$30 = SpillingKt.nullOutSpilledVariable(str118);
                                                                    movieBoxProviderIN$load$15.L$31 = SpillingKt.nullOutSpilledVariable(str119);
                                                                    movieBoxProviderIN$load$15.L$32 = SpillingKt.nullOutSpilledVariable(str120);
                                                                    movieBoxProviderIN$load$15.L$33 = SpillingKt.nullOutSpilledVariable(asText15);
                                                                    movieBoxProviderIN$load$15.I$0 = i7;
                                                                    movieBoxProviderIN$load$15.label = 10;
                                                                    Object newMovieLoadResponse = MainAPIKt.newMovieLoadResponse(this, str57, str58, tvType4, str56, movieBoxProviderIN$load$7, movieBoxProviderIN$load$15);
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
                                                                movieBoxProviderIN$load$16 = movieBoxProviderIN$load$15;
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
                                                                movieBoxProviderIN = this;
                                                                str82 = str56;
                                                                jsonNode10 = jsonNode6;
                                                                obj18 = obj17;
                                                                continuation2 = continuation;
                                                                if (it.hasNext()) {
                                                                    String str124 = (String) it.next();
                                                                    Continuation<? super LoadResponse> continuation5 = continuation2;
                                                                    String str125 = movieBoxProviderIN.getMainUrl() + "/wefeed-mobile-bff/subject-api/season-info?subjectId=" + str124;
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
                                                                    MovieBoxProviderIN$load$1 movieBoxProviderIN$load$18 = movieBoxProviderIN$load$16;
                                                                    String generateXTrSignature$default3 = generateXTrSignature$default(movieBoxProviderIN, "GET", "application/json", "application/json", str125, null, false, null, 112, null);
                                                                    Map mutableMap = MapsKt.toMutableMap(map2);
                                                                    obj19 = obj9;
                                                                    mutableMap.put(obj19, generateXTrSignature$default3);
                                                                    Requests app3 = MainActivityKt.getApp();
                                                                    movieBoxProviderIN$load$18.L$0 = SpillingKt.nullOutSpilledVariable(str69);
                                                                    movieBoxProviderIN$load$18.L$1 = str82;
                                                                    movieBoxProviderIN$load$18.L$2 = str81;
                                                                    movieBoxProviderIN$load$18.L$3 = SpillingKt.nullOutSpilledVariable(str70);
                                                                    movieBoxProviderIN$load$18.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                                    movieBoxProviderIN$load$18.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                                    movieBoxProviderIN$load$18.L$6 = map2;
                                                                    movieBoxProviderIN$load$18.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                                    movieBoxProviderIN$load$18.L$8 = SpillingKt.nullOutSpilledVariable(str44);
                                                                    movieBoxProviderIN$load$18.L$9 = jacksonObjectMapper;
                                                                    movieBoxProviderIN$load$18.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                                    movieBoxProviderIN$load$18.L$11 = SpillingKt.nullOutSpilledVariable(jsonNode40);
                                                                    movieBoxProviderIN$load$18.L$12 = str126;
                                                                    movieBoxProviderIN$load$18.L$13 = str132;
                                                                    movieBoxProviderIN$load$18.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                                    movieBoxProviderIN$load$18.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                                    movieBoxProviderIN$load$18.L$16 = SpillingKt.nullOutSpilledVariable(str47);
                                                                    movieBoxProviderIN$load$18.L$17 = num28;
                                                                    movieBoxProviderIN$load$18.L$18 = num27;
                                                                    String str135 = str72;
                                                                    movieBoxProviderIN$load$18.L$19 = str135;
                                                                    movieBoxProviderIN$load$18.L$20 = str129;
                                                                    movieBoxProviderIN$load$18.L$21 = list23;
                                                                    movieBoxProviderIN$load$18.L$22 = list22;
                                                                    movieBoxProviderIN$load$18.L$23 = num29;
                                                                    movieBoxProviderIN$load$18.L$24 = tvType9;
                                                                    movieBoxProviderIN$load$18.L$25 = num30;
                                                                    movieBoxProviderIN$load$18.L$26 = str134;
                                                                    movieBoxProviderIN$load$18.L$27 = str130;
                                                                    movieBoxProviderIN$load$18.L$28 = SpillingKt.nullOutSpilledVariable(jsonNode9);
                                                                    movieBoxProviderIN$load$18.L$29 = list21;
                                                                    movieBoxProviderIN$load$18.L$30 = str127;
                                                                    movieBoxProviderIN$load$18.L$31 = str128;
                                                                    movieBoxProviderIN$load$18.L$32 = str131;
                                                                    movieBoxProviderIN$load$18.L$33 = str133;
                                                                    movieBoxProviderIN$load$18.L$34 = SpillingKt.nullOutSpilledVariable(collection);
                                                                    movieBoxProviderIN$load$18.L$35 = linkedHashMap2;
                                                                    movieBoxProviderIN$load$18.L$36 = it6;
                                                                    movieBoxProviderIN$load$18.L$37 = SpillingKt.nullOutSpilledVariable(str124);
                                                                    movieBoxProviderIN$load$18.L$38 = SpillingKt.nullOutSpilledVariable(str125);
                                                                    movieBoxProviderIN$load$18.L$39 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default3);
                                                                    movieBoxProviderIN$load$18.L$40 = SpillingKt.nullOutSpilledVariable(mutableMap);
                                                                    movieBoxProviderIN$load$18.I$0 = i12;
                                                                    MovieBoxProviderIN movieBoxProviderIN4 = movieBoxProviderIN;
                                                                    movieBoxProviderIN$load$18.label = 8;
                                                                    Object obj27 = Requests.get$default(app3, str125, mutableMap, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$load$16, 4092, (Object) null);
                                                                    MovieBoxProviderIN$load$1 movieBoxProviderIN$load$19 = movieBoxProviderIN$load$16;
                                                                    if (obj27 == obj26) {
                                                                        return obj26;
                                                                    }
                                                                    str78 = str132;
                                                                    num18 = num28;
                                                                    num17 = num27;
                                                                    str72 = str135;
                                                                    movieBoxProviderIN$load$16 = movieBoxProviderIN$load$19;
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
                                                                    movieBoxProviderIN2 = movieBoxProviderIN4;
                                                                    continuation3 = continuation5;
                                                                    niceResponse5 = (NiceResponse) obj21;
                                                                    int i13 = i10;
                                                                    if (niceResponse5.getCode() != 200) {
                                                                        continuation4 = continuation3;
                                                                        String str136 = str97;
                                                                        JsonNode jsonNode41 = jacksonObjectMapper.readTree(niceResponse5.getBody().string()).get(str136);
                                                                        if (jsonNode41 != null) {
                                                                            movieBoxProviderIN3 = movieBoxProviderIN2;
                                                                            jsonNode22 = jsonNode41.get("seasons");
                                                                        } else {
                                                                            movieBoxProviderIN3 = movieBoxProviderIN2;
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
                                                                        movieBoxProviderIN3 = movieBoxProviderIN2;
                                                                    }
                                                                    i9 = i13;
                                                                    obj9 = obj19;
                                                                    obj18 = coroutine_suspended;
                                                                    it = it2;
                                                                    continuation2 = continuation4;
                                                                    movieBoxProviderIN = movieBoxProviderIN3;
                                                                    linkedHashMap = linkedHashMap2;
                                                                    str71 = str94;
                                                                    jsonNode10 = jsonNode21;
                                                                    map2 = map4;
                                                                    str81 = str93;
                                                                    str97 = str95;
                                                                    if (it.hasNext()) {
                                                                        MovieBoxProviderIN movieBoxProviderIN5 = movieBoxProviderIN;
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
                                                                        MovieBoxProviderIN$load$1 movieBoxProviderIN$load$110 = movieBoxProviderIN$load$16;
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
                                                                                                                            arrayList8.add(MainAPIKt.newEpisode(movieBoxProviderIN5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProviderIN$$ExternalSyntheticLambda0(str86, i18, intValue3, str90, str89, num22, str92)));
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
                                                                                                                    arrayList8.add(MainAPIKt.newEpisode(movieBoxProviderIN5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProviderIN$$ExternalSyntheticLambda0(str86, i182, intValue3, str90, str89, num22, str92)));
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
                                                                                                        arrayList8.add(MainAPIKt.newEpisode(movieBoxProviderIN5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProviderIN$$ExternalSyntheticLambda0(str86, i1822, intValue3, str90, str89, num22, str92)));
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
                                                                                                arrayList8.add(MainAPIKt.newEpisode(movieBoxProviderIN5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProviderIN$$ExternalSyntheticLambda0(str86, i18222, intValue3, str90, str89, num22, str92)));
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
                                                                                arrayList8.add(MainAPIKt.newEpisode(movieBoxProviderIN5, str82 + "|" + intValue2 + "|" + intValue3, new MovieBoxProviderIN$$ExternalSyntheticLambda0(str86, i182222, intValue3, str90, str89, num22, str92)));
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
                                                                            arrayList8.add(MainAPIKt.newEpisode(movieBoxProviderIN5, str82 + "|1|1", new MovieBoxProviderIN$$ExternalSyntheticLambda1(str139)));
                                                                        }
                                                                        MovieBoxProviderIN$load$6 movieBoxProviderIN$load$6 = new MovieBoxProviderIN$load$6(str139, str140, str141, str142, str143, str144, str145, num37, list28, list27, str146, num36, num33, str147, num34, null);
                                                                        movieBoxProviderIN$load$110.L$0 = SpillingKt.nullOutSpilledVariable(str69);
                                                                        movieBoxProviderIN$load$110.L$1 = SpillingKt.nullOutSpilledVariable(str82);
                                                                        movieBoxProviderIN$load$110.L$2 = SpillingKt.nullOutSpilledVariable(str150);
                                                                        movieBoxProviderIN$load$110.L$3 = SpillingKt.nullOutSpilledVariable(str70);
                                                                        movieBoxProviderIN$load$110.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                                        movieBoxProviderIN$load$110.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                                        movieBoxProviderIN$load$110.L$6 = SpillingKt.nullOutSpilledVariable(map2);
                                                                        movieBoxProviderIN$load$110.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                                        movieBoxProviderIN$load$110.L$8 = SpillingKt.nullOutSpilledVariable(str44);
                                                                        movieBoxProviderIN$load$110.L$9 = SpillingKt.nullOutSpilledVariable(jacksonObjectMapper);
                                                                        movieBoxProviderIN$load$110.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                                        movieBoxProviderIN$load$110.L$11 = SpillingKt.nullOutSpilledVariable(jsonNode46);
                                                                        movieBoxProviderIN$load$110.L$12 = SpillingKt.nullOutSpilledVariable(str138);
                                                                        movieBoxProviderIN$load$110.L$13 = SpillingKt.nullOutSpilledVariable(str145);
                                                                        movieBoxProviderIN$load$110.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                                                                        movieBoxProviderIN$load$110.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                                                                        movieBoxProviderIN$load$110.L$16 = SpillingKt.nullOutSpilledVariable(str47);
                                                                        movieBoxProviderIN$load$110.L$17 = SpillingKt.nullOutSpilledVariable(num36);
                                                                        movieBoxProviderIN$load$110.L$18 = SpillingKt.nullOutSpilledVariable(num37);
                                                                        movieBoxProviderIN$load$110.L$19 = SpillingKt.nullOutSpilledVariable(str139);
                                                                        movieBoxProviderIN$load$110.L$20 = SpillingKt.nullOutSpilledVariable(str142);
                                                                        movieBoxProviderIN$load$110.L$21 = SpillingKt.nullOutSpilledVariable(list27);
                                                                        movieBoxProviderIN$load$110.L$22 = SpillingKt.nullOutSpilledVariable(list28);
                                                                        movieBoxProviderIN$load$110.L$23 = SpillingKt.nullOutSpilledVariable(num33);
                                                                        movieBoxProviderIN$load$110.L$24 = SpillingKt.nullOutSpilledVariable(tvType10);
                                                                        movieBoxProviderIN$load$110.L$25 = SpillingKt.nullOutSpilledVariable(num34);
                                                                        movieBoxProviderIN$load$110.L$26 = SpillingKt.nullOutSpilledVariable(str147);
                                                                        movieBoxProviderIN$load$110.L$27 = SpillingKt.nullOutSpilledVariable(str143);
                                                                        movieBoxProviderIN$load$110.L$28 = SpillingKt.nullOutSpilledVariable(jsonNode9);
                                                                        movieBoxProviderIN$load$110.L$29 = SpillingKt.nullOutSpilledVariable(list24);
                                                                        movieBoxProviderIN$load$110.L$30 = SpillingKt.nullOutSpilledVariable(str140);
                                                                        movieBoxProviderIN$load$110.L$31 = SpillingKt.nullOutSpilledVariable(str141);
                                                                        movieBoxProviderIN$load$110.L$32 = SpillingKt.nullOutSpilledVariable(str144);
                                                                        movieBoxProviderIN$load$110.L$33 = SpillingKt.nullOutSpilledVariable(str146);
                                                                        movieBoxProviderIN$load$110.L$34 = SpillingKt.nullOutSpilledVariable(collection);
                                                                        movieBoxProviderIN$load$110.L$35 = SpillingKt.nullOutSpilledVariable(linkedHashMap5);
                                                                        movieBoxProviderIN$load$110.L$36 = SpillingKt.nullOutSpilledVariable(arrayList8);
                                                                        movieBoxProviderIN$load$110.L$37 = null;
                                                                        movieBoxProviderIN$load$110.L$38 = null;
                                                                        movieBoxProviderIN$load$110.L$39 = null;
                                                                        movieBoxProviderIN$load$110.L$40 = null;
                                                                        movieBoxProviderIN$load$110.I$0 = i14;
                                                                        movieBoxProviderIN$load$110.label = 9;
                                                                        Object newTvSeriesLoadResponse = MainAPIKt.newTvSeriesLoadResponse(movieBoxProviderIN5, str138, str150, tvType10, arrayList8, movieBoxProviderIN$load$6, movieBoxProviderIN$load$110);
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
                                                    movieBoxProviderIN$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                                                    movieBoxProviderIN$load$14.L$1 = str19;
                                                    movieBoxProviderIN$load$14.L$2 = str18;
                                                    movieBoxProviderIN$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                                    movieBoxProviderIN$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                                    movieBoxProviderIN$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                    movieBoxProviderIN$load$14.L$6 = map2;
                                                    movieBoxProviderIN$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                                    movieBoxProviderIN$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                                                    movieBoxProviderIN$load$14.L$9 = jacksonObjectMapper;
                                                    movieBoxProviderIN$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                                    movieBoxProviderIN$load$14.L$11 = jsonNode23;
                                                    movieBoxProviderIN$load$14.L$12 = substringBefore$default;
                                                    movieBoxProviderIN$load$14.L$13 = asText10;
                                                    movieBoxProviderIN$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str34);
                                                    movieBoxProviderIN$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str27);
                                                    movieBoxProviderIN$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                                                    movieBoxProviderIN$load$14.L$17 = num;
                                                    movieBoxProviderIN$load$14.L$18 = num2;
                                                    String str1072 = str32;
                                                    movieBoxProviderIN$load$14.L$19 = str1072;
                                                    movieBoxProviderIN$load$14.L$20 = str31;
                                                    movieBoxProviderIN$load$14.L$21 = list;
                                                    List list172 = list2;
                                                    movieBoxProviderIN$load$14.L$22 = list172;
                                                    movieBoxProviderIN$load$14.L$23 = num3;
                                                    movieBoxProviderIN$load$14.L$24 = tvType;
                                                    Integer num232 = num3;
                                                    movieBoxProviderIN$load$14.I$0 = i3;
                                                    movieBoxProviderIN$load$14.label = 5;
                                                    identifyID = MovieBoxProviderINKt.identifyID(str33, intOrNull3, d, movieBoxProviderIN$load$14);
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
                                            movieBoxProviderIN$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                                            movieBoxProviderIN$load$14.L$1 = str19;
                                            movieBoxProviderIN$load$14.L$2 = str18;
                                            movieBoxProviderIN$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                                            movieBoxProviderIN$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                                            movieBoxProviderIN$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                            movieBoxProviderIN$load$14.L$6 = map2;
                                            movieBoxProviderIN$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                                            movieBoxProviderIN$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                                            movieBoxProviderIN$load$14.L$9 = jacksonObjectMapper;
                                            movieBoxProviderIN$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                                            movieBoxProviderIN$load$14.L$11 = jsonNode23;
                                            movieBoxProviderIN$load$14.L$12 = substringBefore$default;
                                            movieBoxProviderIN$load$14.L$13 = asText10;
                                            movieBoxProviderIN$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str34);
                                            movieBoxProviderIN$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str27);
                                            movieBoxProviderIN$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                                            movieBoxProviderIN$load$14.L$17 = num;
                                            movieBoxProviderIN$load$14.L$18 = num2;
                                            String str10722 = str32;
                                            movieBoxProviderIN$load$14.L$19 = str10722;
                                            movieBoxProviderIN$load$14.L$20 = str31;
                                            movieBoxProviderIN$load$14.L$21 = list;
                                            List list1722 = list2;
                                            movieBoxProviderIN$load$14.L$22 = list1722;
                                            movieBoxProviderIN$load$14.L$23 = num3;
                                            movieBoxProviderIN$load$14.L$24 = tvType;
                                            Integer num2322 = num3;
                                            movieBoxProviderIN$load$14.I$0 = i3;
                                            movieBoxProviderIN$load$14.label = 5;
                                            identifyID = MovieBoxProviderINKt.identifyID(str33, intOrNull3, d, movieBoxProviderIN$load$14);
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
                            movieBoxProviderIN$load$14 = movieBoxProviderIN$load$13;
                            str18 = str17;
                            niceResponse3 = niceResponse2;
                            str6 = str101;
                            generateXTrSignature$default = str16;
                            if (niceResponse3.getCode() == 200) {
                            }
                        } else {
                            Map map8 = map;
                            movieBoxProviderIN$load$14 = movieBoxProviderIN$load$13;
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
                        str3 = (String) movieBoxProviderIN$load$12.L$2;
                        String str153 = (String) movieBoxProviderIN$load$12.L$1;
                        String str154 = (String) movieBoxProviderIN$load$12.L$0;
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
                        movieBoxProviderIN$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                        str8 = str1002;
                        movieBoxProviderIN$load$12.L$1 = str8;
                        movieBoxProviderIN$load$12.L$2 = str992;
                        movieBoxProviderIN$load$12.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                        movieBoxProviderIN$load$12.L$4 = SpillingKt.nullOutSpilledVariable(generateXClientToken$default2);
                        movieBoxProviderIN$load$12.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProviderIN$load$12.L$6 = mutableMapOf2;
                        movieBoxProviderIN$load$12.label = 2;
                        str9 = "description";
                        obj = obj21;
                        str10 = str96;
                        str11 = generateXClientToken$default2;
                        str12 = "Bearer ";
                        obj2 = "x-tr-signature";
                        obj3 = "Authorization";
                        obj4 = coroutine_suspended;
                        i2 = 3;
                        obj5 = Requests.get$default(app4, str992, mutableMapOf2, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$load$12, 4092, (Object) null);
                        map = mutableMapOf2;
                        str13 = str992;
                        movieBoxProviderIN$load$13 = movieBoxProviderIN$load$12;
                        if (obj5 == obj4) {
                        }
                        niceResponse = (NiceResponse) obj5;
                        if (niceResponse.getCode() != 441) {
                            break;
                        }
                        movieBoxProviderIN$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                        movieBoxProviderIN$load$13.L$1 = str8;
                        movieBoxProviderIN$load$13.L$2 = str13;
                        movieBoxProviderIN$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                        movieBoxProviderIN$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                        movieBoxProviderIN$load$13.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProviderIN$load$13.L$6 = map;
                        movieBoxProviderIN$load$13.L$7 = niceResponse;
                        movieBoxProviderIN$load$13.label = i2;
                        fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProviderIN$load$13);
                        if (fetchAnonymousToken == obj4) {
                        }
                        break;
                    case 2:
                        Map map9 = (Map) movieBoxProviderIN$load$12.L$6;
                        generateXTrSignature$default = (String) movieBoxProviderIN$load$12.L$5;
                        String str155 = (String) movieBoxProviderIN$load$12.L$4;
                        String str156 = (String) movieBoxProviderIN$load$12.L$3;
                        String str157 = (String) movieBoxProviderIN$load$12.L$2;
                        String str158 = (String) movieBoxProviderIN$load$12.L$1;
                        String str159 = (String) movieBoxProviderIN$load$12.L$0;
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
                        movieBoxProviderIN$load$13 = movieBoxProviderIN$load$12;
                        obj5 = obj;
                        i2 = 3;
                        str12 = "Bearer ";
                        str8 = str158;
                        str9 = "description";
                        niceResponse = (NiceResponse) obj5;
                        if (niceResponse.getCode() != 441) {
                        }
                        movieBoxProviderIN$load$13.L$0 = SpillingKt.nullOutSpilledVariable(str4);
                        movieBoxProviderIN$load$13.L$1 = str8;
                        movieBoxProviderIN$load$13.L$2 = str13;
                        movieBoxProviderIN$load$13.L$3 = SpillingKt.nullOutSpilledVariable(str6);
                        movieBoxProviderIN$load$13.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                        movieBoxProviderIN$load$13.L$5 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default);
                        movieBoxProviderIN$load$13.L$6 = map;
                        movieBoxProviderIN$load$13.L$7 = niceResponse;
                        movieBoxProviderIN$load$13.label = i2;
                        fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProviderIN$load$13);
                        if (fetchAnonymousToken == obj4) {
                        }
                        break;
                    case 3:
                        NiceResponse niceResponse6 = (NiceResponse) movieBoxProviderIN$load$12.L$7;
                        Map map10 = (Map) movieBoxProviderIN$load$12.L$6;
                        String str160 = (String) movieBoxProviderIN$load$12.L$5;
                        String str161 = (String) movieBoxProviderIN$load$12.L$4;
                        String str162 = (String) movieBoxProviderIN$load$12.L$2;
                        String str163 = (String) movieBoxProviderIN$load$12.L$1;
                        String str164 = (String) movieBoxProviderIN$load$12.L$0;
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
                        movieBoxProviderIN$load$13 = movieBoxProviderIN$load$12;
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
                        map3 = (Map) movieBoxProviderIN$load$12.L$6;
                        generateXTrSignature$default = (String) movieBoxProviderIN$load$12.L$5;
                        String str165 = (String) movieBoxProviderIN$load$12.L$4;
                        String str166 = (String) movieBoxProviderIN$load$12.L$3;
                        String str167 = (String) movieBoxProviderIN$load$12.L$2;
                        String str168 = (String) movieBoxProviderIN$load$12.L$1;
                        String str169 = (String) movieBoxProviderIN$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str4 = str169;
                        str15 = str165;
                        movieBoxProviderIN$load$14 = movieBoxProviderIN$load$12;
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
                        int i19 = movieBoxProviderIN$load$12.I$0;
                        tvType2 = (TvType) movieBoxProviderIN$load$12.L$24;
                        num5 = (Integer) movieBoxProviderIN$load$12.L$23;
                        List list29 = (List) movieBoxProviderIN$load$12.L$22;
                        List list30 = (List) movieBoxProviderIN$load$12.L$21;
                        String str170 = (String) movieBoxProviderIN$load$12.L$20;
                        String str171 = (String) movieBoxProviderIN$load$12.L$19;
                        Integer num39 = (Integer) movieBoxProviderIN$load$12.L$18;
                        Integer num40 = (Integer) movieBoxProviderIN$load$12.L$17;
                        String str172 = (String) movieBoxProviderIN$load$12.L$16;
                        str37 = (String) movieBoxProviderIN$load$12.L$15;
                        str42 = (String) movieBoxProviderIN$load$12.L$14;
                        String str173 = (String) movieBoxProviderIN$load$12.L$13;
                        String str174 = (String) movieBoxProviderIN$load$12.L$12;
                        JsonNode jsonNode53 = (JsonNode) movieBoxProviderIN$load$12.L$11;
                        JsonNode jsonNode54 = (JsonNode) movieBoxProviderIN$load$12.L$10;
                        ObjectMapper objectMapper = (ObjectMapper) movieBoxProviderIN$load$12.L$9;
                        String str175 = (String) movieBoxProviderIN$load$12.L$8;
                        NiceResponse niceResponse7 = (NiceResponse) movieBoxProviderIN$load$12.L$7;
                        Map map11 = (Map) movieBoxProviderIN$load$12.L$6;
                        String str176 = (String) movieBoxProviderIN$load$12.L$5;
                        String str177 = (String) movieBoxProviderIN$load$12.L$4;
                        String str178 = (String) movieBoxProviderIN$load$12.L$3;
                        String str179 = (String) movieBoxProviderIN$load$12.L$2;
                        String str180 = (String) movieBoxProviderIN$load$12.L$1;
                        String str181 = (String) movieBoxProviderIN$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        movieBoxProviderIN$load$14 = movieBoxProviderIN$load$12;
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
                        movieBoxProviderIN$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str38);
                        movieBoxProviderIN$load$14.L$1 = str41;
                        movieBoxProviderIN$load$14.L$2 = str18;
                        movieBoxProviderIN$load$14.L$3 = SpillingKt.nullOutSpilledVariable(str25);
                        movieBoxProviderIN$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str15);
                        movieBoxProviderIN$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                        movieBoxProviderIN$load$14.L$6 = map2;
                        movieBoxProviderIN$load$14.L$7 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                        movieBoxProviderIN$load$14.L$8 = SpillingKt.nullOutSpilledVariable(str28);
                        movieBoxProviderIN$load$14.L$9 = jacksonObjectMapper;
                        movieBoxProviderIN$load$14.L$10 = SpillingKt.nullOutSpilledVariable(jsonNode);
                        movieBoxProviderIN$load$14.L$11 = jsonNode3;
                        movieBoxProviderIN$load$14.L$12 = str36;
                        movieBoxProviderIN$load$14.L$13 = str40;
                        movieBoxProviderIN$load$14.L$14 = SpillingKt.nullOutSpilledVariable(str42);
                        movieBoxProviderIN$load$14.L$15 = SpillingKt.nullOutSpilledVariable(str37);
                        movieBoxProviderIN$load$14.L$16 = SpillingKt.nullOutSpilledVariable(asText);
                        movieBoxProviderIN$load$14.L$17 = num4;
                        movieBoxProviderIN$load$14.L$18 = num6;
                        movieBoxProviderIN$load$14.L$19 = str35;
                        movieBoxProviderIN$load$14.L$20 = str39;
                        movieBoxProviderIN$load$14.L$21 = list4;
                        movieBoxProviderIN$load$14.L$22 = list3;
                        movieBoxProviderIN$load$14.L$23 = num5;
                        movieBoxProviderIN$load$14.L$24 = tvType72;
                        JsonNode jsonNode382 = jsonNode3;
                        movieBoxProviderIN$load$14.L$25 = num242;
                        movieBoxProviderIN$load$14.L$26 = str1082;
                        movieBoxProviderIN$load$14.I$0 = i4;
                        movieBoxProviderIN$load$14.label = 6;
                        Integer num252 = num5;
                        List list182 = list4;
                        String str1092 = str35;
                        String str1102 = str96;
                        List list192 = list3;
                        String str1112 = str41;
                        String str1122 = str39;
                        String str1132 = str36;
                        movieBoxProviderIN$load$15 = movieBoxProviderIN$load$14;
                        String str1142 = str26;
                        num7 = num4;
                        obj14 = obj13;
                        fetchTmdbLogoUrl = MovieBoxProviderINKt.fetchTmdbLogoUrl("https://api.themoviedb.org/3", "98ae14df2b8d8f8f8136499daf79f0e0", tvType72, num242, "en", movieBoxProviderIN$load$15);
                        if (fetchTmdbLogoUrl != obj14) {
                        }
                        break;
                    case 6:
                        int i20 = movieBoxProviderIN$load$12.I$0;
                        String str182 = (String) movieBoxProviderIN$load$12.L$26;
                        Integer num41 = (Integer) movieBoxProviderIN$load$12.L$25;
                        TvType tvType11 = (TvType) movieBoxProviderIN$load$12.L$24;
                        Integer num42 = (Integer) movieBoxProviderIN$load$12.L$23;
                        list5 = (List) movieBoxProviderIN$load$12.L$22;
                        list6 = (List) movieBoxProviderIN$load$12.L$21;
                        String str183 = (String) movieBoxProviderIN$load$12.L$20;
                        String str184 = (String) movieBoxProviderIN$load$12.L$19;
                        Integer num43 = (Integer) movieBoxProviderIN$load$12.L$18;
                        Integer num44 = (Integer) movieBoxProviderIN$load$12.L$17;
                        str47 = (String) movieBoxProviderIN$load$12.L$16;
                        String str185 = (String) movieBoxProviderIN$load$12.L$15;
                        String str186 = (String) movieBoxProviderIN$load$12.L$14;
                        String str187 = (String) movieBoxProviderIN$load$12.L$13;
                        String str188 = (String) movieBoxProviderIN$load$12.L$12;
                        JsonNode jsonNode55 = (JsonNode) movieBoxProviderIN$load$12.L$11;
                        JsonNode jsonNode56 = (JsonNode) movieBoxProviderIN$load$12.L$10;
                        ObjectMapper objectMapper2 = (ObjectMapper) movieBoxProviderIN$load$12.L$9;
                        str44 = (String) movieBoxProviderIN$load$12.L$8;
                        NiceResponse niceResponse8 = (NiceResponse) movieBoxProviderIN$load$12.L$7;
                        Map map12 = (Map) movieBoxProviderIN$load$12.L$6;
                        String str189 = (String) movieBoxProviderIN$load$12.L$5;
                        String str190 = (String) movieBoxProviderIN$load$12.L$4;
                        String str191 = (String) movieBoxProviderIN$load$12.L$3;
                        String str192 = (String) movieBoxProviderIN$load$12.L$2;
                        String str193 = (String) movieBoxProviderIN$load$12.L$1;
                        String str194 = (String) movieBoxProviderIN$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str45 = str182;
                        num8 = num41;
                        tvType3 = tvType11;
                        num9 = num42;
                        str46 = str184;
                        movieBoxProviderIN$load$15 = movieBoxProviderIN$load$12;
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
                        int i21 = movieBoxProviderIN$load$12.I$0;
                        String str195 = (String) movieBoxProviderIN$load$12.L$27;
                        String str196 = (String) movieBoxProviderIN$load$12.L$26;
                        Integer num45 = (Integer) movieBoxProviderIN$load$12.L$25;
                        TvType tvType12 = (TvType) movieBoxProviderIN$load$12.L$24;
                        Integer num46 = (Integer) movieBoxProviderIN$load$12.L$23;
                        List list31 = (List) movieBoxProviderIN$load$12.L$22;
                        List list32 = (List) movieBoxProviderIN$load$12.L$21;
                        str68 = (String) movieBoxProviderIN$load$12.L$20;
                        i8 = i21;
                        String str197 = (String) movieBoxProviderIN$load$12.L$19;
                        num14 = (Integer) movieBoxProviderIN$load$12.L$18;
                        num16 = (Integer) movieBoxProviderIN$load$12.L$17;
                        str47 = (String) movieBoxProviderIN$load$12.L$16;
                        String str198 = (String) movieBoxProviderIN$load$12.L$15;
                        String str199 = (String) movieBoxProviderIN$load$12.L$14;
                        str65 = (String) movieBoxProviderIN$load$12.L$13;
                        String str200 = (String) movieBoxProviderIN$load$12.L$12;
                        JsonNode jsonNode57 = (JsonNode) movieBoxProviderIN$load$12.L$11;
                        jsonNode8 = (JsonNode) movieBoxProviderIN$load$12.L$10;
                        ObjectMapper objectMapper3 = (ObjectMapper) movieBoxProviderIN$load$12.L$9;
                        str44 = (String) movieBoxProviderIN$load$12.L$8;
                        NiceResponse niceResponse9 = (NiceResponse) movieBoxProviderIN$load$12.L$7;
                        Map map13 = (Map) movieBoxProviderIN$load$12.L$6;
                        String str201 = (String) movieBoxProviderIN$load$12.L$5;
                        String str202 = (String) movieBoxProviderIN$load$12.L$4;
                        String str203 = (String) movieBoxProviderIN$load$12.L$3;
                        str63 = (String) movieBoxProviderIN$load$12.L$2;
                        str62 = (String) movieBoxProviderIN$load$12.L$1;
                        str53 = (String) movieBoxProviderIN$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        str61 = str195;
                        num8 = num45;
                        list10 = list31;
                        movieBoxProviderIN$load$15 = movieBoxProviderIN$load$12;
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
                        int i22 = movieBoxProviderIN$load$12.I$0;
                        map5 = (Map) movieBoxProviderIN$load$12.L$40;
                        it2 = (Iterator) movieBoxProviderIN$load$12.L$36;
                        ?? r6 = (Map) movieBoxProviderIN$load$12.L$35;
                        Collection collection2 = (List) movieBoxProviderIN$load$12.L$34;
                        String str204 = (String) movieBoxProviderIN$load$12.L$33;
                        String str205 = (String) movieBoxProviderIN$load$12.L$32;
                        String str206 = (String) movieBoxProviderIN$load$12.L$31;
                        String str207 = (String) movieBoxProviderIN$load$12.L$30;
                        List list33 = (List) movieBoxProviderIN$load$12.L$29;
                        JsonNode jsonNode58 = (JsonNode) movieBoxProviderIN$load$12.L$28;
                        String str208 = (String) movieBoxProviderIN$load$12.L$27;
                        String str209 = (String) movieBoxProviderIN$load$12.L$26;
                        Integer num47 = (Integer) movieBoxProviderIN$load$12.L$25;
                        TvType tvType13 = (TvType) movieBoxProviderIN$load$12.L$24;
                        Integer num48 = (Integer) movieBoxProviderIN$load$12.L$23;
                        List list34 = (List) movieBoxProviderIN$load$12.L$22;
                        List list35 = (List) movieBoxProviderIN$load$12.L$21;
                        String str210 = (String) movieBoxProviderIN$load$12.L$20;
                        String str211 = (String) movieBoxProviderIN$load$12.L$19;
                        Integer num49 = (Integer) movieBoxProviderIN$load$12.L$18;
                        Integer num50 = (Integer) movieBoxProviderIN$load$12.L$17;
                        String str212 = (String) movieBoxProviderIN$load$12.L$16;
                        String str213 = (String) movieBoxProviderIN$load$12.L$15;
                        String str214 = (String) movieBoxProviderIN$load$12.L$14;
                        String str215 = (String) movieBoxProviderIN$load$12.L$13;
                        str94 = (String) movieBoxProviderIN$load$12.L$12;
                        jsonNode21 = (JsonNode) movieBoxProviderIN$load$12.L$11;
                        JsonNode jsonNode59 = (JsonNode) movieBoxProviderIN$load$12.L$10;
                        ObjectMapper objectMapper4 = (ObjectMapper) movieBoxProviderIN$load$12.L$9;
                        String str216 = (String) movieBoxProviderIN$load$12.L$8;
                        NiceResponse niceResponse10 = (NiceResponse) movieBoxProviderIN$load$12.L$7;
                        map4 = (Map) movieBoxProviderIN$load$12.L$6;
                        String str217 = (String) movieBoxProviderIN$load$12.L$5;
                        String str218 = (String) movieBoxProviderIN$load$12.L$4;
                        String str219 = (String) movieBoxProviderIN$load$12.L$3;
                        str93 = (String) movieBoxProviderIN$load$12.L$2;
                        String str220 = (String) movieBoxProviderIN$load$12.L$1;
                        String str221 = (String) movieBoxProviderIN$load$12.L$0;
                        ResultKt.throwOnFailure(obj21);
                        linkedHashMap2 = r6;
                        list13 = list33;
                        str69 = str221;
                        str79 = str204;
                        movieBoxProviderIN$load$16 = movieBoxProviderIN$load$12;
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
                        movieBoxProviderIN2 = this;
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
                        movieBoxProviderIN = movieBoxProviderIN3;
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
                        int i23 = movieBoxProviderIN$load$12.I$0;
                        ResultKt.throwOnFailure(obj21);
                        return obj21;
                    case 10:
                        int i24 = movieBoxProviderIN$load$12.I$0;
                        ResultKt.throwOnFailure(obj21);
                        return obj21;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        movieBoxProviderIN$load$1 = new MovieBoxProviderIN$load$1(this, continuation);
        movieBoxProviderIN$load$12 = movieBoxProviderIN$load$1;
        Object obj212 = movieBoxProviderIN$load$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        String str962 = "title";
        String str972 = "data";
        switch (movieBoxProviderIN$load$12.label) {
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
        throw new UnsupportedOperationException("Method not decompiled: com.cncverse.MovieBoxProviderIN.loadLinks(java.lang.String, boolean, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
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
        MovieBoxProviderIN$search$2 movieBoxProviderIN$search$2;
        MovieBoxProviderIN$search$2 movieBoxProviderIN$search$22;
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
        MovieBoxProviderIN$search$2 movieBoxProviderIN$search$23;
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
        if (continuation instanceof MovieBoxProviderIN$search$2) {
            movieBoxProviderIN$search$2 = (MovieBoxProviderIN$search$2) continuation;
            if ((movieBoxProviderIN$search$2.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderIN$search$2.label -= Integer.MIN_VALUE;
                movieBoxProviderIN$search$22 = movieBoxProviderIN$search$2;
                Object obj8 = movieBoxProviderIN$search$22.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProviderIN$search$22.label) {
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
                        movieBoxProviderIN$search$22.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        movieBoxProviderIN$search$22.L$1 = str30;
                        movieBoxProviderIN$search$22.L$2 = str2;
                        movieBoxProviderIN$search$22.L$3 = generateXClientToken$default;
                        movieBoxProviderIN$search$22.L$4 = generateXTrSignature$default;
                        movieBoxProviderIN$search$22.I$0 = i;
                        movieBoxProviderIN$search$22.label = 1;
                        fetchAnonymousToken$default = fetchAnonymousToken$default(this, false, movieBoxProviderIN$search$22, 1, null);
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
                        movieBoxProviderIN$search$22.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                        movieBoxProviderIN$search$22.L$1 = str5;
                        movieBoxProviderIN$search$22.L$2 = str2;
                        movieBoxProviderIN$search$22.L$3 = SpillingKt.nullOutSpilledVariable(str4);
                        movieBoxProviderIN$search$22.L$4 = SpillingKt.nullOutSpilledVariable(str32);
                        movieBoxProviderIN$search$22.L$5 = SpillingKt.nullOutSpilledVariable(str31);
                        movieBoxProviderIN$search$22.L$6 = mutableMapOf;
                        movieBoxProviderIN$search$22.L$7 = create;
                        movieBoxProviderIN$search$22.I$0 = i2;
                        movieBoxProviderIN$search$22.label = 2;
                        map = mutableMapOf;
                        str8 = "Bearer ";
                        obj6 = coroutine_suspended;
                        post$default = Requests.post$default(app, str5, map, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, create, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$search$22, 65276, (Object) null);
                        str9 = str5;
                        movieBoxProviderIN$search$23 = movieBoxProviderIN$search$22;
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
                        movieBoxProviderIN$search$23.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                        movieBoxProviderIN$search$23.L$1 = str9;
                        movieBoxProviderIN$search$23.L$2 = str14;
                        movieBoxProviderIN$search$23.L$3 = SpillingKt.nullOutSpilledVariable(str12);
                        movieBoxProviderIN$search$23.L$4 = SpillingKt.nullOutSpilledVariable(str13);
                        movieBoxProviderIN$search$23.L$5 = SpillingKt.nullOutSpilledVariable(str10);
                        movieBoxProviderIN$search$23.L$6 = map;
                        movieBoxProviderIN$search$23.L$7 = requestBody;
                        movieBoxProviderIN$search$23.L$8 = niceResponse;
                        movieBoxProviderIN$search$23.I$0 = i3;
                        movieBoxProviderIN$search$23.label = 3;
                        fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProviderIN$search$23);
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
                        movieBoxProviderIN$search$23.L$0 = SpillingKt.nullOutSpilledVariable(str16);
                        movieBoxProviderIN$search$23.L$1 = SpillingKt.nullOutSpilledVariable(str19);
                        movieBoxProviderIN$search$23.L$2 = SpillingKt.nullOutSpilledVariable(str20);
                        movieBoxProviderIN$search$23.L$3 = SpillingKt.nullOutSpilledVariable(str18);
                        movieBoxProviderIN$search$23.L$4 = SpillingKt.nullOutSpilledVariable(str17);
                        movieBoxProviderIN$search$23.L$5 = SpillingKt.nullOutSpilledVariable(str33);
                        movieBoxProviderIN$search$23.L$6 = SpillingKt.nullOutSpilledVariable(map);
                        movieBoxProviderIN$search$23.L$7 = SpillingKt.nullOutSpilledVariable(requestBody2);
                        movieBoxProviderIN$search$23.L$8 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                        movieBoxProviderIN$search$23.L$9 = SpillingKt.nullOutSpilledVariable(generateXTrSignature$default2);
                        movieBoxProviderIN$search$23.I$0 = i4;
                        movieBoxProviderIN$search$23.label = 4;
                        Object post$default2 = Requests.post$default(app2, str19, map, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, requestBody2, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$search$23, 65276, (Object) null);
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
                                                            arrayList.add(MainAPIKt.newMovieSearchResponse$default(this, asText, asText2, tvType, false, new MovieBoxProviderIN$$ExternalSyntheticLambda3(str29, jsonNode6), 8, (Object) null));
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
                                                    arrayList.add(MainAPIKt.newMovieSearchResponse$default(this, asText, asText2, tvType, false, new MovieBoxProviderIN$$ExternalSyntheticLambda3(str29, jsonNode6), 8, (Object) null));
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
                        i2 = movieBoxProviderIN$search$22.I$0;
                        str6 = (String) movieBoxProviderIN$search$22.L$4;
                        String str36 = (String) movieBoxProviderIN$search$22.L$3;
                        String str37 = (String) movieBoxProviderIN$search$22.L$2;
                        String str38 = (String) movieBoxProviderIN$search$22.L$1;
                        str3 = (String) movieBoxProviderIN$search$22.L$0;
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
                        movieBoxProviderIN$search$22.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                        movieBoxProviderIN$search$22.L$1 = str5;
                        movieBoxProviderIN$search$22.L$2 = str2;
                        movieBoxProviderIN$search$22.L$3 = SpillingKt.nullOutSpilledVariable(str4);
                        movieBoxProviderIN$search$22.L$4 = SpillingKt.nullOutSpilledVariable(str322);
                        movieBoxProviderIN$search$22.L$5 = SpillingKt.nullOutSpilledVariable(str312);
                        movieBoxProviderIN$search$22.L$6 = mutableMapOf2;
                        movieBoxProviderIN$search$22.L$7 = create2;
                        movieBoxProviderIN$search$22.I$0 = i2;
                        movieBoxProviderIN$search$22.label = 2;
                        map = mutableMapOf2;
                        str8 = "Bearer ";
                        obj6 = coroutine_suspended;
                        post$default = Requests.post$default(app3, str5, map, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, create2, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderIN$search$22, 65276, (Object) null);
                        str9 = str5;
                        movieBoxProviderIN$search$23 = movieBoxProviderIN$search$22;
                        if (post$default != obj6) {
                        }
                        break;
                    case 2:
                        int i7 = movieBoxProviderIN$search$22.I$0;
                        RequestBody requestBody4 = (RequestBody) movieBoxProviderIN$search$22.L$7;
                        Map map3 = (Map) movieBoxProviderIN$search$22.L$6;
                        String str39 = (String) movieBoxProviderIN$search$22.L$5;
                        str13 = (String) movieBoxProviderIN$search$22.L$4;
                        String str40 = (String) movieBoxProviderIN$search$22.L$3;
                        String str41 = (String) movieBoxProviderIN$search$22.L$2;
                        String str42 = (String) movieBoxProviderIN$search$22.L$1;
                        str11 = (String) movieBoxProviderIN$search$22.L$0;
                        ResultKt.throwOnFailure(obj8);
                        str10 = str39;
                        obj7 = obj8;
                        z = false;
                        obj4 = "x-tr-signature";
                        movieBoxProviderIN$search$23 = movieBoxProviderIN$search$22;
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
                        movieBoxProviderIN$search$23.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                        movieBoxProviderIN$search$23.L$1 = str9;
                        movieBoxProviderIN$search$23.L$2 = str14;
                        movieBoxProviderIN$search$23.L$3 = SpillingKt.nullOutSpilledVariable(str12);
                        movieBoxProviderIN$search$23.L$4 = SpillingKt.nullOutSpilledVariable(str13);
                        movieBoxProviderIN$search$23.L$5 = SpillingKt.nullOutSpilledVariable(str10);
                        movieBoxProviderIN$search$23.L$6 = map;
                        movieBoxProviderIN$search$23.L$7 = requestBody;
                        movieBoxProviderIN$search$23.L$8 = niceResponse;
                        movieBoxProviderIN$search$23.I$0 = i3;
                        movieBoxProviderIN$search$23.label = 3;
                        fetchAnonymousToken = fetchAnonymousToken(true, movieBoxProviderIN$search$23);
                        if (fetchAnonymousToken != obj6) {
                        }
                        break;
                    case 3:
                        int i8 = movieBoxProviderIN$search$22.I$0;
                        NiceResponse niceResponse3 = (NiceResponse) movieBoxProviderIN$search$22.L$8;
                        RequestBody requestBody5 = (RequestBody) movieBoxProviderIN$search$22.L$7;
                        Map map4 = (Map) movieBoxProviderIN$search$22.L$6;
                        String str43 = (String) movieBoxProviderIN$search$22.L$4;
                        String str44 = (String) movieBoxProviderIN$search$22.L$3;
                        String str45 = (String) movieBoxProviderIN$search$22.L$2;
                        String str46 = (String) movieBoxProviderIN$search$22.L$1;
                        String str47 = (String) movieBoxProviderIN$search$22.L$0;
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
                        movieBoxProviderIN$search$23 = movieBoxProviderIN$search$22;
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
                        i5 = movieBoxProviderIN$search$22.I$0;
                        requestBody3 = (RequestBody) movieBoxProviderIN$search$22.L$7;
                        map2 = (Map) movieBoxProviderIN$search$22.L$6;
                        str23 = (String) movieBoxProviderIN$search$22.L$5;
                        str27 = (String) movieBoxProviderIN$search$22.L$4;
                        str28 = (String) movieBoxProviderIN$search$22.L$3;
                        str25 = (String) movieBoxProviderIN$search$22.L$2;
                        str24 = (String) movieBoxProviderIN$search$22.L$1;
                        str26 = (String) movieBoxProviderIN$search$22.L$0;
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
        movieBoxProviderIN$search$2 = new MovieBoxProviderIN$search$2(this, continuation);
        movieBoxProviderIN$search$22 = movieBoxProviderIN$search$2;
        Object obj82 = movieBoxProviderIN$search$22.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderIN$search$22.label) {
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
        MovieBoxProviderIN$search$1 movieBoxProviderIN$search$1;
        Object search;
        if (continuation instanceof MovieBoxProviderIN$search$1) {
            movieBoxProviderIN$search$1 = (MovieBoxProviderIN$search$1) continuation;
            if ((movieBoxProviderIN$search$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderIN$search$1.label -= Integer.MIN_VALUE;
                Object obj = movieBoxProviderIN$search$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProviderIN$search$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        movieBoxProviderIN$search$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        movieBoxProviderIN$search$1.label = 1;
                        search = search(str, 1, movieBoxProviderIN$search$1);
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
        movieBoxProviderIN$search$1 = new MovieBoxProviderIN$search$1(this, continuation);
        Object obj2 = movieBoxProviderIN$search$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderIN$search$1.label) {
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
