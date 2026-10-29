package com.cncverse;

import android.content.Context;
import com.cncverse.donation.DonationManager;
import com.lagradost.cloudstream3.Episode;
import com.lagradost.cloudstream3.HomePageList;
import com.lagradost.cloudstream3.HomePageResponse;
import com.lagradost.cloudstream3.LoadResponse;
import com.lagradost.cloudstream3.MainAPI;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.MainPageData;
import com.lagradost.cloudstream3.MainPageRequest;
import com.lagradost.cloudstream3.MovieSearchResponse;
import com.lagradost.cloudstream3.SearchResponse;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.TvType;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.MatchGroup;
import kotlin.text.MatchGroupCollection;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.RegexOption;
import kotlin.text.StringsKt;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.sync.Mutex;
import kotlinx.coroutines.sync.MutexKt;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* compiled from: MlsbdProvider.kt */
@Metadata(d1 = {"\u0000|\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 E2\u00020\u0001:\u0001EB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010#\u001a\u00020\u00052\u0006\u0010$\u001a\u00020\u0005H\u0002J\u0016\u0010%\u001a\u00020&2\u0006\u0010'\u001a\u00020\u0005H\u0082@¢\u0006\u0002\u0010(J\u0018\u0010)\u001a\u0004\u0018\u00010&2\u0006\u0010'\u001a\u00020\u0005H\u0082@¢\u0006\u0002\u0010(J\u001e\u0010\u001b\u001a\u00020*2\u0006\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020.H\u0096@¢\u0006\u0002\u0010/J\u0012\u00100\u001a\u0004\u0018\u0001012\u0006\u00102\u001a\u000203H\u0002J\u001c\u00104\u001a\b\u0012\u0004\u0012\u0002010\u00192\u0006\u00105\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010(J\u0016\u00106\u001a\u0002072\u0006\u0010'\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u0010(JF\u00108\u001a\u00020\u00112\u0006\u00109\u001a\u00020\u00052\u0006\u0010:\u001a\u00020\u00112\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020=\u0012\u0004\u0012\u00020>0<2\u0012\u0010?\u001a\u000e\u0012\u0004\u0012\u00020@\u0012\u0004\u0012\u00020>0<H\u0096@¢\u0006\u0002\u0010AJF\u0010B\u001a\u00020>2\u0006\u0010'\u001a\u00020\u00052\u0006\u0010C\u001a\u00020\u00052\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020=\u0012\u0004\u0012\u00020>0<2\u0012\u0010?\u001a\u000e\u0012\u0004\u0012\u00020@\u0012\u0004\u0012\u00020>0<H\u0082@¢\u0006\u0002\u0010DR\u001a\u0010\u0004\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u001a\u0010\r\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u0007\"\u0004\b\u000f\u0010\tR\u0014\u0010\u0010\u001a\u00020\u0011X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0014\u0010\u0014\u001a\u00020\u0011X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u0014\u0010\u0016\u001a\u00020\u0011X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0013R\u001a\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u001a\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001eX\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u000e\u0010\"\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000¨\u0006F"}, d2 = {"Lcom/cncverse/MlsbdProvider;", "Lcom/lagradost/cloudstream3/MainAPI;", "<init>", "()V", "mainUrl", "", "getMainUrl", "()Ljava/lang/String;", "setMainUrl", "(Ljava/lang/String;)V", "name", "getName", "setName", "lang", "getLang", "setLang", "hasMainPage", "", "getHasMainPage", "()Z", "hasDownloadSupport", "getHasDownloadSupport", "hasQuickSearch", "getHasQuickSearch", "mainPage", "", "Lcom/lagradost/cloudstream3/MainPageData;", "getMainPage", "()Ljava/util/List;", "supportedTypes", "", "Lcom/lagradost/cloudstream3/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "defaultUserAgent", "unescapeJsString", "raw", "fetchHtml", "Lorg/jsoup/nodes/Document;", "url", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchViaWebView", "Lcom/lagradost/cloudstream3/HomePageResponse;", "page", "", "request", "Lcom/lagradost/cloudstream3/MainPageRequest;", "(ILcom/lagradost/cloudstream3/MainPageRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toResult", "Lcom/lagradost/cloudstream3/SearchResponse;", "post", "Lorg/jsoup/nodes/Element;", "search", "query", "load", "Lcom/lagradost/cloudstream3/LoadResponse;", "loadLinks", "data", "isCasting", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resolveCustomExtractor", "qualityLabel", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nMlsbdProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MlsbdProvider.kt\ncom/cncverse/MlsbdProvider\n+ 2 Mutex.kt\nkotlinx/coroutines/sync/MutexKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 4 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,643:1\n116#2,11:644\n1642#3,10:655\n1915#3:665\n1916#3:667\n1652#3:668\n1642#3,10:669\n1915#3:679\n1916#3:681\n1652#3:682\n1807#3,3:684\n1915#3,2:687\n1915#3:689\n1915#3,2:690\n1916#3:692\n1#4:666\n1#4:680\n1#4:683\n*S KotlinDebug\n*F\n+ 1 MlsbdProvider.kt\ncom/cncverse/MlsbdProvider\n*L\n130#1:644,11\n398#1:655,10\n398#1:665\n398#1:667\n398#1:668\n425#1:669,10\n425#1:679\n425#1:681\n425#1:682\n442#1:684,3\n521#1:687,2\n548#1:689\n567#1:690,2\n548#1:692\n398#1:666\n425#1:680\n*E\n"})
/* loaded from: classes.dex */
public final class MlsbdProvider extends MainAPI {

    @Nullable
    private static Context appContext;
    private final boolean hasQuickSearch;

    /* renamed from: Companion, reason: from kotlin metadata */
    @NotNull
    public static final Companion INSTANCE = new Companion(null);

    @NotNull
    private static final Mutex cfMutex = MutexKt.Mutex$default(false, 1, (Object) null);

    @NotNull
    private String mainUrl = "https://mlsbd.co";

    @NotNull
    private String name = "Mlsbd";

    @NotNull
    private String lang = "bn";
    private final boolean hasMainPage = true;
    private final boolean hasDownloadSupport = true;

    @NotNull
    private final List<MainPageData> mainPage = MainAPIKt.mainPageOf(new Pair[]{TuplesKt.to("", "Latest Movies"), TuplesKt.to("category/bangla-dubbed", "Bangla Dubbed"), TuplesKt.to("category/dual-audio-movies", "Multi Audio Movies"), TuplesKt.to("category/tv-series", "TV Series"), TuplesKt.to("category/bollywood-movies", "Bollywood Movies"), TuplesKt.to("category/bangla-movies", "Bengali Movies"), TuplesKt.to("category/hollywood-movies", "Hollywood Movies")});

    @NotNull
    private final Set<TvType> supportedTypes = SetsKt.setOf(new TvType[]{TvType.Movie, TvType.TvSeries, TvType.AsianDrama, TvType.AnimeMovie});

    @NotNull
    private final String defaultUserAgent = "Mozilla/5.0 (Linux; Android 13; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36";

    /* compiled from: MlsbdProvider.kt */
    @Metadata(d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\f"}, d2 = {"Lcom/cncverse/MlsbdProvider$Companion;", "", "<init>", "()V", "appContext", "Landroid/content/Context;", "getAppContext", "()Landroid/content/Context;", "setAppContext", "(Landroid/content/Context;)V", "cfMutex", "Lkotlinx/coroutines/sync/Mutex;", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @Nullable
        public final Context getAppContext() {
            return MlsbdProvider.appContext;
        }

        public final void setAppContext(@Nullable Context context) {
            MlsbdProvider.appContext = context;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't wrap try/catch for region: R(12:39|40|41|(3:61|62|(3:64|19|20))|43|44|45|46|47|48|49|(1:51)(7:52|15|16|17|18|19|20)) */
    /* JADX WARN: Can't wrap try/catch for region: R(13:86|39|40|41|(3:61|62|(3:64|19|20))|43|44|45|46|47|48|49|(1:51)(7:52|15|16|17|18|19|20)) */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x0585, code lost:
    
        r16 = r22;
        r11 = r26;
        r10 = r21;
        r9 = r18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:56:0x0573, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:57:0x0574, code lost:
    
        r2 = r22;
        r7 = r32;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x059f, code lost:
    
        r32 = r13;
        r16 = r22;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0030. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:111:0x0117  */
    /* JADX WARN: Removed duplicated region for block: B:118:0x02c0 A[Catch: all -> 0x02ac, TRY_LEAVE, TryCatch #3 {all -> 0x02ac, blocks: (B:158:0x02a3, B:118:0x02c0), top: B:157:0x02a3 }] */
    /* JADX WARN: Removed duplicated region for block: B:11:0x003b  */
    /* JADX WARN: Removed duplicated region for block: B:127:0x0443  */
    /* JADX WARN: Removed duplicated region for block: B:128:0x02d0 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:157:0x02a3 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:165:0x013e  */
    /* JADX WARN: Removed duplicated region for block: B:173:0x0236  */
    /* JADX WARN: Removed duplicated region for block: B:181:0x027c A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:182:0x027d  */
    /* JADX WARN: Removed duplicated region for block: B:189:0x0162  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x0089  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x054b A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:52:0x054c  */
    /* JADX WARN: Removed duplicated region for block: B:61:0x04a9 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:73:0x00d1  */
    /* JADX WARN: Removed duplicated region for block: B:85:0x0491 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:86:0x0492  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0033  */
    /* JADX WARN: Removed duplicated region for block: B:90:0x0405 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object fetchHtml(String str, Continuation<? super Document> continuation) {
        MlsbdProvider$fetchHtml$1 mlsbdProvider$fetchHtml$1;
        MlsbdProvider$fetchHtml$1 mlsbdProvider$fetchHtml$12;
        String str2;
        long longValue;
        int i;
        Object obj;
        String str3;
        int i2;
        int i3;
        String str4;
        int i4;
        String str5;
        Requests app;
        Map<String, String> fetchHtml$buildHeaders;
        Object obj2;
        NiceResponse niceResponse;
        Mutex mutex;
        Object obj3;
        int i5;
        String str6;
        int i6;
        String str7;
        Mutex mutex2;
        long j;
        Object obj4;
        Mutex mutex3;
        String str8;
        String str9;
        boolean z;
        int i7;
        int i8;
        int i9;
        long j2;
        Mutex mutex4;
        String str10;
        int i10;
        int i11;
        long j3;
        Mutex mutex5;
        int i12;
        String str11;
        long j4;
        int i13;
        String str12;
        int i14;
        String str13;
        String str14;
        int i15;
        Object obj5;
        Mutex mutex6;
        String str15;
        long j5;
        int i16;
        long j6;
        int i17;
        long j7;
        Mutex mutex7;
        String str16;
        int i18;
        int i19;
        Object obj6;
        long j8;
        int i20;
        String str17;
        long j9;
        long j10;
        int i21;
        int i22;
        long j11;
        int i23;
        Object fetchViaWebView;
        Mutex mutex8;
        long j12;
        int i24;
        String str18;
        String str19;
        int i25;
        int i26;
        Object obj7;
        String str20;
        Document document;
        Mutex mutex9;
        Object obj8;
        Document document2;
        Mutex mutex10;
        Object obj9;
        Object obj10;
        MlsbdProvider mlsbdProvider = this;
        if (continuation instanceof MlsbdProvider$fetchHtml$1) {
            mlsbdProvider$fetchHtml$1 = (MlsbdProvider$fetchHtml$1) continuation;
            if ((mlsbdProvider$fetchHtml$1.label & Integer.MIN_VALUE) != 0) {
                mlsbdProvider$fetchHtml$1.label -= Integer.MIN_VALUE;
                mlsbdProvider$fetchHtml$12 = mlsbdProvider$fetchHtml$1;
                Object obj11 = mlsbdProvider$fetchHtml$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                NiceResponse niceResponse2 = null;
                int i27 = 0;
                switch (mlsbdProvider$fetchHtml$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj11);
                        Pair<String, Long> cfCookie = MlsbdStorage.INSTANCE.getCfCookie();
                        str2 = (String) cfCookie.component1();
                        longValue = ((Number) cfCookie.component2()).longValue();
                        String str21 = str2;
                        i = ((str21 == null || str21.length() == 0) || System.currentTimeMillis() - longValue >= 82800000) ? 0 : 1;
                        if (i != 0) {
                            try {
                                app = MainActivityKt.getApp();
                            } catch (Exception e) {
                                obj = coroutine_suspended;
                                str3 = str2;
                                i2 = i;
                            }
                            try {
                                fetchHtml$buildHeaders = fetchHtml$buildHeaders(mlsbdProvider, str2);
                                mlsbdProvider$fetchHtml$12.L$0 = str;
                                mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                                mlsbdProvider$fetchHtml$12.J$0 = longValue;
                                mlsbdProvider$fetchHtml$12.I$0 = i;
                                mlsbdProvider$fetchHtml$12.label = 1;
                                obj = coroutine_suspended;
                                i3 = 200;
                            } catch (Exception e2) {
                                str3 = str2;
                                i2 = i;
                                obj = coroutine_suspended;
                                i3 = 200;
                                str4 = str;
                                i4 = i2;
                                str5 = str3;
                                String str22 = str5;
                                i = i4;
                                str2 = str22;
                                niceResponse = null;
                                if (niceResponse != null) {
                                }
                                mutex = cfMutex;
                                mlsbdProvider$fetchHtml$12.L$0 = str4;
                                mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                                mlsbdProvider$fetchHtml$12.L$2 = mutex;
                                mlsbdProvider$fetchHtml$12.J$0 = longValue;
                                mlsbdProvider$fetchHtml$12.I$0 = i;
                                mlsbdProvider$fetchHtml$12.I$1 = 0;
                                mlsbdProvider$fetchHtml$12.label = 2;
                                if (mutex.lock((Object) null, mlsbdProvider$fetchHtml$12) == obj) {
                                }
                            }
                            try {
                                obj2 = Requests.get$default(app, str, fetchHtml$buildHeaders, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 30L, (Interceptor) null, false, (ResponseParser) null, mlsbdProvider$fetchHtml$12, 3836, (Object) null);
                                mlsbdProvider$fetchHtml$12 = mlsbdProvider$fetchHtml$12;
                            } catch (Exception e3) {
                                mlsbdProvider$fetchHtml$12 = mlsbdProvider$fetchHtml$12;
                                str4 = str;
                                i4 = i;
                                str5 = str2;
                                longValue = longValue;
                                String str222 = str5;
                                i = i4;
                                str2 = str222;
                                niceResponse = null;
                                if (niceResponse != null) {
                                }
                                mutex = cfMutex;
                                mlsbdProvider$fetchHtml$12.L$0 = str4;
                                mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                                mlsbdProvider$fetchHtml$12.L$2 = mutex;
                                mlsbdProvider$fetchHtml$12.J$0 = longValue;
                                mlsbdProvider$fetchHtml$12.I$0 = i;
                                mlsbdProvider$fetchHtml$12.I$1 = 0;
                                mlsbdProvider$fetchHtml$12.label = 2;
                                if (mutex.lock((Object) null, mlsbdProvider$fetchHtml$12) == obj) {
                                }
                            }
                            if (obj2 == obj) {
                                return obj;
                            }
                            str4 = str;
                            obj11 = obj2;
                            i4 = i;
                            str5 = str2;
                            longValue = longValue;
                            try {
                                niceResponse = (NiceResponse) obj11;
                                String str23 = str5;
                                i = i4;
                                str2 = str23;
                            } catch (Exception e4) {
                                String str2222 = str5;
                                i = i4;
                                str2 = str2222;
                                niceResponse = null;
                                if (niceResponse != null) {
                                }
                                mutex = cfMutex;
                                mlsbdProvider$fetchHtml$12.L$0 = str4;
                                mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                                mlsbdProvider$fetchHtml$12.L$2 = mutex;
                                mlsbdProvider$fetchHtml$12.J$0 = longValue;
                                mlsbdProvider$fetchHtml$12.I$0 = i;
                                mlsbdProvider$fetchHtml$12.I$1 = 0;
                                mlsbdProvider$fetchHtml$12.label = 2;
                                if (mutex.lock((Object) null, mlsbdProvider$fetchHtml$12) == obj) {
                                }
                            }
                            if (niceResponse != null && niceResponse.getCode() == i3 && !fetchHtml$isCloudflare(niceResponse.getText(), niceResponse.getCode())) {
                                return niceResponse.getDocument();
                            }
                            mutex = cfMutex;
                            mlsbdProvider$fetchHtml$12.L$0 = str4;
                            mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                            mlsbdProvider$fetchHtml$12.L$2 = mutex;
                            mlsbdProvider$fetchHtml$12.J$0 = longValue;
                            mlsbdProvider$fetchHtml$12.I$0 = i;
                            mlsbdProvider$fetchHtml$12.I$1 = 0;
                            mlsbdProvider$fetchHtml$12.label = 2;
                            if (mutex.lock((Object) null, mlsbdProvider$fetchHtml$12) == obj) {
                                return obj;
                            }
                            obj3 = null;
                            i5 = 0;
                            str6 = str2;
                            i6 = i;
                            str7 = str4;
                            mutex2 = mutex;
                            j = longValue;
                            try {
                                Pair<String, Long> cfCookie2 = MlsbdStorage.INSTANCE.getCfCookie();
                                str8 = (String) cfCookie2.component1();
                                long longValue2 = ((Number) cfCookie2.component2()).longValue();
                                str9 = str8;
                                if (str9 != null) {
                                    try {
                                        if (str9.length() != 0) {
                                            z = false;
                                            if (!z) {
                                                if (System.currentTimeMillis() - longValue2 < 82800000) {
                                                    i27 = 1;
                                                }
                                            }
                                            i7 = i27;
                                            if (i7 != 0) {
                                                try {
                                                    Requests app2 = MainActivityKt.getApp();
                                                    try {
                                                        Map<String, String> fetchHtml$buildHeaders2 = fetchHtml$buildHeaders(this, str8);
                                                        mlsbdProvider$fetchHtml$12.L$0 = str7;
                                                        mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str6);
                                                        mlsbdProvider$fetchHtml$12.L$2 = mutex2;
                                                        mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                                                        mlsbdProvider$fetchHtml$12.J$0 = j;
                                                        mlsbdProvider$fetchHtml$12.I$0 = i6;
                                                        mlsbdProvider$fetchHtml$12.I$1 = i5;
                                                        mlsbdProvider$fetchHtml$12.I$2 = 0;
                                                        mlsbdProvider$fetchHtml$12.J$1 = longValue2;
                                                        mlsbdProvider$fetchHtml$12.I$3 = i7;
                                                        mlsbdProvider$fetchHtml$12.label = 3;
                                                        j7 = j;
                                                        mutex7 = mutex2;
                                                        int i28 = i5;
                                                        str16 = str7;
                                                        i18 = i6;
                                                        i19 = i28;
                                                        MlsbdProvider$fetchHtml$1 mlsbdProvider$fetchHtml$13 = mlsbdProvider$fetchHtml$12;
                                                        mlsbdProvider = this;
                                                        try {
                                                            obj6 = Requests.get$default(app2, str16, fetchHtml$buildHeaders2, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 30L, (Interceptor) null, false, (ResponseParser) null, mlsbdProvider$fetchHtml$13, 3836, (Object) null);
                                                            mlsbdProvider$fetchHtml$12 = mlsbdProvider$fetchHtml$13;
                                                        } catch (Exception e5) {
                                                            mlsbdProvider$fetchHtml$12 = mlsbdProvider$fetchHtml$13;
                                                            i14 = 0;
                                                            str13 = str16;
                                                            str14 = str8;
                                                            i15 = i7;
                                                            obj5 = obj3;
                                                            mutex6 = mutex7;
                                                            str15 = str6;
                                                            j5 = longValue2;
                                                            i16 = i18;
                                                            j6 = j7;
                                                            i17 = i19;
                                                            i19 = i17;
                                                            i18 = i16;
                                                            j11 = j5;
                                                            i23 = i14;
                                                            str8 = str14;
                                                            if (niceResponse2 != null) {
                                                            }
                                                            i20 = i23;
                                                            j9 = j11;
                                                            j10 = j6;
                                                            str17 = str13;
                                                            i7 = i15;
                                                            mutex3 = mutex6;
                                                            i21 = i18;
                                                            i22 = i19;
                                                            try {
                                                                MlsbdStorage.INSTANCE.clearCfCookie();
                                                                mlsbdProvider$fetchHtml$12.L$0 = str17;
                                                                mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str15);
                                                                mlsbdProvider$fetchHtml$12.L$2 = mutex3;
                                                                mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                                                                mlsbdProvider$fetchHtml$12.J$0 = j10;
                                                                mlsbdProvider$fetchHtml$12.I$0 = i21;
                                                                mlsbdProvider$fetchHtml$12.I$1 = i22;
                                                                mlsbdProvider$fetchHtml$12.I$2 = i20;
                                                                mlsbdProvider$fetchHtml$12.J$1 = j9;
                                                                mlsbdProvider$fetchHtml$12.I$3 = i7;
                                                                mlsbdProvider$fetchHtml$12.label = 4;
                                                                fetchViaWebView = mlsbdProvider.fetchViaWebView(str17, mlsbdProvider$fetchHtml$12);
                                                                if (fetchViaWebView == obj) {
                                                                }
                                                            } catch (Throwable th) {
                                                                th = th;
                                                                obj4 = obj5;
                                                                mutex3.unlock(obj4);
                                                                throw th;
                                                            }
                                                        } catch (Throwable th2) {
                                                            th = th2;
                                                            obj4 = obj3;
                                                            mutex3 = mutex7;
                                                            mutex3.unlock(obj4);
                                                            throw th;
                                                        }
                                                    } catch (Exception e6) {
                                                        i10 = i5;
                                                        i11 = i6;
                                                        j3 = j;
                                                        mutex5 = mutex2;
                                                        i12 = 0;
                                                        str11 = str8;
                                                        j4 = longValue2;
                                                        i13 = i7;
                                                        str12 = str7;
                                                        mlsbdProvider = this;
                                                        i14 = i12;
                                                        str13 = str12;
                                                        str14 = str11;
                                                        i15 = i13;
                                                        obj5 = obj3;
                                                        mutex6 = mutex5;
                                                        str15 = str6;
                                                        j5 = j4;
                                                        i16 = i11;
                                                        j6 = j3;
                                                        i17 = i10;
                                                        i19 = i17;
                                                        i18 = i16;
                                                        j11 = j5;
                                                        i23 = i14;
                                                        str8 = str14;
                                                        if (niceResponse2 != null) {
                                                        }
                                                        i20 = i23;
                                                        j9 = j11;
                                                        j10 = j6;
                                                        str17 = str13;
                                                        i7 = i15;
                                                        mutex3 = mutex6;
                                                        i21 = i18;
                                                        i22 = i19;
                                                        MlsbdStorage.INSTANCE.clearCfCookie();
                                                        mlsbdProvider$fetchHtml$12.L$0 = str17;
                                                        mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str15);
                                                        mlsbdProvider$fetchHtml$12.L$2 = mutex3;
                                                        mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                                                        mlsbdProvider$fetchHtml$12.J$0 = j10;
                                                        mlsbdProvider$fetchHtml$12.I$0 = i21;
                                                        mlsbdProvider$fetchHtml$12.I$1 = i22;
                                                        mlsbdProvider$fetchHtml$12.I$2 = i20;
                                                        mlsbdProvider$fetchHtml$12.J$1 = j9;
                                                        mlsbdProvider$fetchHtml$12.I$3 = i7;
                                                        mlsbdProvider$fetchHtml$12.label = 4;
                                                        fetchViaWebView = mlsbdProvider.fetchViaWebView(str17, mlsbdProvider$fetchHtml$12);
                                                        if (fetchViaWebView == obj) {
                                                        }
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        i8 = i5;
                                                        i9 = i6;
                                                        j2 = j;
                                                        mutex4 = mutex2;
                                                        str10 = str7;
                                                        obj4 = obj3;
                                                        mutex3 = mutex4;
                                                        mutex3.unlock(obj4);
                                                        throw th;
                                                    }
                                                } catch (Exception e7) {
                                                    mlsbdProvider = this;
                                                    i10 = i5;
                                                    i11 = i6;
                                                    j3 = j;
                                                    mutex5 = mutex2;
                                                    i12 = 0;
                                                    str11 = str8;
                                                    j4 = longValue2;
                                                    i13 = i7;
                                                    str12 = str7;
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    i8 = i5;
                                                    i9 = i6;
                                                    j2 = j;
                                                    mutex4 = mutex2;
                                                    str10 = str7;
                                                }
                                                if (obj6 == obj) {
                                                    return obj;
                                                }
                                                i14 = 0;
                                                str13 = str16;
                                                str14 = str8;
                                                i15 = i7;
                                                obj5 = obj3;
                                                mutex6 = mutex7;
                                                str15 = str6;
                                                j5 = longValue2;
                                                j8 = j7;
                                                obj11 = obj6;
                                                try {
                                                    niceResponse2 = (NiceResponse) obj11;
                                                    j6 = j8;
                                                    j11 = j5;
                                                    i23 = i14;
                                                    str8 = str14;
                                                } catch (Exception e8) {
                                                    j6 = j8;
                                                    i16 = i18;
                                                    i17 = i19;
                                                    i19 = i17;
                                                    i18 = i16;
                                                    j11 = j5;
                                                    i23 = i14;
                                                    str8 = str14;
                                                    if (niceResponse2 != null) {
                                                    }
                                                    i20 = i23;
                                                    j9 = j11;
                                                    j10 = j6;
                                                    str17 = str13;
                                                    i7 = i15;
                                                    mutex3 = mutex6;
                                                    i21 = i18;
                                                    i22 = i19;
                                                    MlsbdStorage.INSTANCE.clearCfCookie();
                                                    mlsbdProvider$fetchHtml$12.L$0 = str17;
                                                    mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str15);
                                                    mlsbdProvider$fetchHtml$12.L$2 = mutex3;
                                                    mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                                                    mlsbdProvider$fetchHtml$12.J$0 = j10;
                                                    mlsbdProvider$fetchHtml$12.I$0 = i21;
                                                    mlsbdProvider$fetchHtml$12.I$1 = i22;
                                                    mlsbdProvider$fetchHtml$12.I$2 = i20;
                                                    mlsbdProvider$fetchHtml$12.J$1 = j9;
                                                    mlsbdProvider$fetchHtml$12.I$3 = i7;
                                                    mlsbdProvider$fetchHtml$12.label = 4;
                                                    fetchViaWebView = mlsbdProvider.fetchViaWebView(str17, mlsbdProvider$fetchHtml$12);
                                                    if (fetchViaWebView == obj) {
                                                    }
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    obj4 = obj5;
                                                    mutex3 = mutex6;
                                                    mutex3.unlock(obj4);
                                                    throw th;
                                                }
                                                if (niceResponse2 != null) {
                                                    try {
                                                        if (niceResponse2.getCode() == 200 && !fetchHtml$isCloudflare(niceResponse2.getText(), niceResponse2.getCode())) {
                                                            document = niceResponse2.getDocument();
                                                            mutex9 = mutex6;
                                                            obj8 = obj5;
                                                            mutex9.unlock(obj8);
                                                            return document;
                                                        }
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        obj4 = obj5;
                                                        mutex3 = mutex6;
                                                        mutex3.unlock(obj4);
                                                        throw th;
                                                    }
                                                }
                                                i20 = i23;
                                                j9 = j11;
                                                j10 = j6;
                                                str17 = str13;
                                                i7 = i15;
                                                mutex3 = mutex6;
                                                i21 = i18;
                                                i22 = i19;
                                                MlsbdStorage.INSTANCE.clearCfCookie();
                                                mlsbdProvider$fetchHtml$12.L$0 = str17;
                                                mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str15);
                                                mlsbdProvider$fetchHtml$12.L$2 = mutex3;
                                                mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                                                mlsbdProvider$fetchHtml$12.J$0 = j10;
                                                mlsbdProvider$fetchHtml$12.I$0 = i21;
                                                mlsbdProvider$fetchHtml$12.I$1 = i22;
                                                mlsbdProvider$fetchHtml$12.I$2 = i20;
                                                mlsbdProvider$fetchHtml$12.J$1 = j9;
                                                mlsbdProvider$fetchHtml$12.I$3 = i7;
                                                mlsbdProvider$fetchHtml$12.label = 4;
                                                fetchViaWebView = mlsbdProvider.fetchViaWebView(str17, mlsbdProvider$fetchHtml$12);
                                                if (fetchViaWebView == obj) {
                                                    return obj;
                                                }
                                                int i29 = i7;
                                                mutex8 = mutex3;
                                                j12 = j9;
                                                i24 = i29;
                                                str18 = str17;
                                                str19 = str8;
                                                i25 = i20;
                                                i26 = i21;
                                                obj11 = fetchViaWebView;
                                                obj7 = obj5;
                                                str20 = str15;
                                                try {
                                                    document2 = (Document) obj11;
                                                    if (document2 != null) {
                                                        try {
                                                            if (!fetchHtml$isCloudflare(document2.html(), 200)) {
                                                                mutex9 = mutex8;
                                                                obj8 = obj7;
                                                                document = document2;
                                                                mutex9.unlock(obj8);
                                                                return document;
                                                            }
                                                        } catch (Throwable th7) {
                                                            th = th7;
                                                            mutex3 = mutex8;
                                                            obj4 = obj7;
                                                            mutex3.unlock(obj4);
                                                            throw th;
                                                        }
                                                    }
                                                    String str24 = (String) MlsbdStorage.INSTANCE.getCfCookie().component1();
                                                    Requests app3 = MainActivityKt.getApp();
                                                    Map<String, String> fetchHtml$buildHeaders3 = fetchHtml$buildHeaders(mlsbdProvider, str24);
                                                    mlsbdProvider$fetchHtml$12.L$0 = SpillingKt.nullOutSpilledVariable(str18);
                                                    mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str20);
                                                    mlsbdProvider$fetchHtml$12.L$2 = mutex8;
                                                    mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str19);
                                                    mlsbdProvider$fetchHtml$12.L$4 = SpillingKt.nullOutSpilledVariable(document2);
                                                    mlsbdProvider$fetchHtml$12.L$5 = SpillingKt.nullOutSpilledVariable(str24);
                                                    mlsbdProvider$fetchHtml$12.J$0 = j10;
                                                    mlsbdProvider$fetchHtml$12.I$0 = i26;
                                                    mlsbdProvider$fetchHtml$12.I$1 = i22;
                                                    mlsbdProvider$fetchHtml$12.I$2 = i25;
                                                    mlsbdProvider$fetchHtml$12.J$1 = j12;
                                                    mlsbdProvider$fetchHtml$12.I$3 = i24;
                                                    mlsbdProvider$fetchHtml$12.label = 5;
                                                    int i30 = i22;
                                                    int i31 = i26;
                                                    long j13 = j10;
                                                    mutex10 = mutex8;
                                                    obj10 = Requests.get$default(app3, str18, fetchHtml$buildHeaders3, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 30L, (Interceptor) null, false, (ResponseParser) null, mlsbdProvider$fetchHtml$12, 3836, (Object) null);
                                                    if (obj10 != obj) {
                                                        return obj;
                                                    }
                                                    obj9 = obj7;
                                                    j10 = j13;
                                                    i26 = i31;
                                                    i22 = i30;
                                                    obj11 = obj10;
                                                    try {
                                                        try {
                                                            document = ((NiceResponse) obj11).getDocument();
                                                        } catch (Exception e9) {
                                                            document = Jsoup.parse("");
                                                            obj8 = obj9;
                                                            mutex9 = mutex10;
                                                            mutex9.unlock(obj8);
                                                            return document;
                                                        }
                                                        obj8 = obj9;
                                                        mutex9 = mutex10;
                                                        mutex9.unlock(obj8);
                                                        return document;
                                                    } catch (Throwable th8) {
                                                        th = th8;
                                                        obj4 = obj9;
                                                        mutex3 = mutex10;
                                                        mutex3.unlock(obj4);
                                                        throw th;
                                                    }
                                                } catch (Throwable th9) {
                                                    th = th9;
                                                    obj4 = obj7;
                                                    mutex3 = mutex8;
                                                }
                                            } else {
                                                mlsbdProvider = this;
                                                int i32 = i5;
                                                int i33 = i6;
                                                Mutex mutex11 = mutex2;
                                                i20 = 0;
                                                str17 = str7;
                                                obj5 = obj3;
                                                mutex3 = mutex11;
                                                str15 = str6;
                                                j9 = longValue2;
                                                j10 = j;
                                                i21 = i33;
                                                i22 = i32;
                                                MlsbdStorage.INSTANCE.clearCfCookie();
                                                mlsbdProvider$fetchHtml$12.L$0 = str17;
                                                mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str15);
                                                mlsbdProvider$fetchHtml$12.L$2 = mutex3;
                                                mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                                                mlsbdProvider$fetchHtml$12.J$0 = j10;
                                                mlsbdProvider$fetchHtml$12.I$0 = i21;
                                                mlsbdProvider$fetchHtml$12.I$1 = i22;
                                                mlsbdProvider$fetchHtml$12.I$2 = i20;
                                                mlsbdProvider$fetchHtml$12.J$1 = j9;
                                                mlsbdProvider$fetchHtml$12.I$3 = i7;
                                                mlsbdProvider$fetchHtml$12.label = 4;
                                                fetchViaWebView = mlsbdProvider.fetchViaWebView(str17, mlsbdProvider$fetchHtml$12);
                                                if (fetchViaWebView == obj) {
                                                }
                                            }
                                        }
                                    } catch (Throwable th10) {
                                        th = th10;
                                        mutex3 = mutex2;
                                        obj4 = obj3;
                                        mutex3.unlock(obj4);
                                        throw th;
                                    }
                                }
                                z = true;
                                if (!z) {
                                }
                                i7 = i27;
                                if (i7 != 0) {
                                }
                            } catch (Throwable th11) {
                                th = th11;
                                obj4 = obj3;
                                mutex3 = mutex2;
                            }
                        } else {
                            obj = coroutine_suspended;
                            str4 = str;
                            mutex = cfMutex;
                            mlsbdProvider$fetchHtml$12.L$0 = str4;
                            mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                            mlsbdProvider$fetchHtml$12.L$2 = mutex;
                            mlsbdProvider$fetchHtml$12.J$0 = longValue;
                            mlsbdProvider$fetchHtml$12.I$0 = i;
                            mlsbdProvider$fetchHtml$12.I$1 = 0;
                            mlsbdProvider$fetchHtml$12.label = 2;
                            if (mutex.lock((Object) null, mlsbdProvider$fetchHtml$12) == obj) {
                            }
                        }
                        break;
                    case 1:
                        i4 = mlsbdProvider$fetchHtml$12.I$0;
                        longValue = mlsbdProvider$fetchHtml$12.J$0;
                        str5 = (String) mlsbdProvider$fetchHtml$12.L$1;
                        str4 = (String) mlsbdProvider$fetchHtml$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj11);
                            obj = coroutine_suspended;
                            i3 = 200;
                            niceResponse = (NiceResponse) obj11;
                            String str232 = str5;
                            i = i4;
                            str2 = str232;
                        } catch (Exception e10) {
                            obj = coroutine_suspended;
                            i3 = 200;
                            String str22222 = str5;
                            i = i4;
                            str2 = str22222;
                            niceResponse = null;
                            if (niceResponse != null) {
                            }
                            mutex = cfMutex;
                            mlsbdProvider$fetchHtml$12.L$0 = str4;
                            mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                            mlsbdProvider$fetchHtml$12.L$2 = mutex;
                            mlsbdProvider$fetchHtml$12.J$0 = longValue;
                            mlsbdProvider$fetchHtml$12.I$0 = i;
                            mlsbdProvider$fetchHtml$12.I$1 = 0;
                            mlsbdProvider$fetchHtml$12.label = 2;
                            if (mutex.lock((Object) null, mlsbdProvider$fetchHtml$12) == obj) {
                            }
                        }
                        if (niceResponse != null) {
                            return niceResponse.getDocument();
                        }
                        mutex = cfMutex;
                        mlsbdProvider$fetchHtml$12.L$0 = str4;
                        mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                        mlsbdProvider$fetchHtml$12.L$2 = mutex;
                        mlsbdProvider$fetchHtml$12.J$0 = longValue;
                        mlsbdProvider$fetchHtml$12.I$0 = i;
                        mlsbdProvider$fetchHtml$12.I$1 = 0;
                        mlsbdProvider$fetchHtml$12.label = 2;
                        if (mutex.lock((Object) null, mlsbdProvider$fetchHtml$12) == obj) {
                        }
                        break;
                    case 2:
                        int i34 = mlsbdProvider$fetchHtml$12.I$1;
                        int i35 = mlsbdProvider$fetchHtml$12.I$0;
                        long j14 = mlsbdProvider$fetchHtml$12.J$0;
                        Mutex mutex12 = (Mutex) mlsbdProvider$fetchHtml$12.L$2;
                        String str25 = (String) mlsbdProvider$fetchHtml$12.L$1;
                        str7 = (String) mlsbdProvider$fetchHtml$12.L$0;
                        ResultKt.throwOnFailure(obj11);
                        obj = coroutine_suspended;
                        i6 = i35;
                        obj3 = null;
                        mutex2 = mutex12;
                        str6 = str25;
                        i5 = i34;
                        j = j14;
                        Pair<String, Long> cfCookie22 = MlsbdStorage.INSTANCE.getCfCookie();
                        str8 = (String) cfCookie22.component1();
                        long longValue22 = ((Number) cfCookie22.component2()).longValue();
                        str9 = str8;
                        if (str9 != null) {
                        }
                        z = true;
                        if (!z) {
                        }
                        i7 = i27;
                        if (i7 != 0) {
                        }
                        break;
                    case 3:
                        i15 = mlsbdProvider$fetchHtml$12.I$3;
                        j5 = mlsbdProvider$fetchHtml$12.J$1;
                        i14 = mlsbdProvider$fetchHtml$12.I$2;
                        i17 = mlsbdProvider$fetchHtml$12.I$1;
                        i16 = mlsbdProvider$fetchHtml$12.I$0;
                        j6 = mlsbdProvider$fetchHtml$12.J$0;
                        str14 = (String) mlsbdProvider$fetchHtml$12.L$3;
                        obj5 = null;
                        mutex6 = (Mutex) mlsbdProvider$fetchHtml$12.L$2;
                        str15 = (String) mlsbdProvider$fetchHtml$12.L$1;
                        str13 = (String) mlsbdProvider$fetchHtml$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj11);
                            obj = coroutine_suspended;
                            i19 = i17;
                            i18 = i16;
                            j8 = j6;
                            niceResponse2 = (NiceResponse) obj11;
                            j6 = j8;
                            j11 = j5;
                            i23 = i14;
                            str8 = str14;
                        } catch (Exception e11) {
                            obj = coroutine_suspended;
                            i19 = i17;
                            i18 = i16;
                            j11 = j5;
                            i23 = i14;
                            str8 = str14;
                            if (niceResponse2 != null) {
                            }
                            i20 = i23;
                            j9 = j11;
                            j10 = j6;
                            str17 = str13;
                            i7 = i15;
                            mutex3 = mutex6;
                            i21 = i18;
                            i22 = i19;
                            MlsbdStorage.INSTANCE.clearCfCookie();
                            mlsbdProvider$fetchHtml$12.L$0 = str17;
                            mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str15);
                            mlsbdProvider$fetchHtml$12.L$2 = mutex3;
                            mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                            mlsbdProvider$fetchHtml$12.J$0 = j10;
                            mlsbdProvider$fetchHtml$12.I$0 = i21;
                            mlsbdProvider$fetchHtml$12.I$1 = i22;
                            mlsbdProvider$fetchHtml$12.I$2 = i20;
                            mlsbdProvider$fetchHtml$12.J$1 = j9;
                            mlsbdProvider$fetchHtml$12.I$3 = i7;
                            mlsbdProvider$fetchHtml$12.label = 4;
                            fetchViaWebView = mlsbdProvider.fetchViaWebView(str17, mlsbdProvider$fetchHtml$12);
                            if (fetchViaWebView == obj) {
                            }
                        } catch (Throwable th12) {
                            th = th12;
                            obj4 = null;
                            mutex3 = mutex6;
                            mutex3.unlock(obj4);
                            throw th;
                        }
                        if (niceResponse2 != null) {
                        }
                        i20 = i23;
                        j9 = j11;
                        j10 = j6;
                        str17 = str13;
                        i7 = i15;
                        mutex3 = mutex6;
                        i21 = i18;
                        i22 = i19;
                        MlsbdStorage.INSTANCE.clearCfCookie();
                        mlsbdProvider$fetchHtml$12.L$0 = str17;
                        mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str15);
                        mlsbdProvider$fetchHtml$12.L$2 = mutex3;
                        mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str8);
                        mlsbdProvider$fetchHtml$12.J$0 = j10;
                        mlsbdProvider$fetchHtml$12.I$0 = i21;
                        mlsbdProvider$fetchHtml$12.I$1 = i22;
                        mlsbdProvider$fetchHtml$12.I$2 = i20;
                        mlsbdProvider$fetchHtml$12.J$1 = j9;
                        mlsbdProvider$fetchHtml$12.I$3 = i7;
                        mlsbdProvider$fetchHtml$12.label = 4;
                        fetchViaWebView = mlsbdProvider.fetchViaWebView(str17, mlsbdProvider$fetchHtml$12);
                        if (fetchViaWebView == obj) {
                        }
                        break;
                    case 4:
                        int i36 = mlsbdProvider$fetchHtml$12.I$3;
                        long j15 = mlsbdProvider$fetchHtml$12.J$1;
                        int i37 = mlsbdProvider$fetchHtml$12.I$2;
                        int i38 = mlsbdProvider$fetchHtml$12.I$1;
                        int i39 = mlsbdProvider$fetchHtml$12.I$0;
                        long j16 = mlsbdProvider$fetchHtml$12.J$0;
                        String str26 = (String) mlsbdProvider$fetchHtml$12.L$3;
                        Mutex mutex13 = (Mutex) mlsbdProvider$fetchHtml$12.L$2;
                        String str27 = (String) mlsbdProvider$fetchHtml$12.L$1;
                        str18 = (String) mlsbdProvider$fetchHtml$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj11);
                            obj = coroutine_suspended;
                            j12 = j15;
                            i25 = i37;
                            i22 = i38;
                            i26 = i39;
                            j10 = j16;
                            str19 = str26;
                            mutex8 = mutex13;
                            i24 = i36;
                            obj7 = null;
                            str20 = str27;
                            document2 = (Document) obj11;
                            if (document2 != null) {
                            }
                            String str242 = (String) MlsbdStorage.INSTANCE.getCfCookie().component1();
                            Requests app32 = MainActivityKt.getApp();
                            Map<String, String> fetchHtml$buildHeaders32 = fetchHtml$buildHeaders(mlsbdProvider, str242);
                            mlsbdProvider$fetchHtml$12.L$0 = SpillingKt.nullOutSpilledVariable(str18);
                            mlsbdProvider$fetchHtml$12.L$1 = SpillingKt.nullOutSpilledVariable(str20);
                            mlsbdProvider$fetchHtml$12.L$2 = mutex8;
                            mlsbdProvider$fetchHtml$12.L$3 = SpillingKt.nullOutSpilledVariable(str19);
                            mlsbdProvider$fetchHtml$12.L$4 = SpillingKt.nullOutSpilledVariable(document2);
                            mlsbdProvider$fetchHtml$12.L$5 = SpillingKt.nullOutSpilledVariable(str242);
                            mlsbdProvider$fetchHtml$12.J$0 = j10;
                            mlsbdProvider$fetchHtml$12.I$0 = i26;
                            mlsbdProvider$fetchHtml$12.I$1 = i22;
                            mlsbdProvider$fetchHtml$12.I$2 = i25;
                            mlsbdProvider$fetchHtml$12.J$1 = j12;
                            mlsbdProvider$fetchHtml$12.I$3 = i24;
                            mlsbdProvider$fetchHtml$12.label = 5;
                            int i302 = i22;
                            int i312 = i26;
                            long j132 = j10;
                            mutex10 = mutex8;
                            obj10 = Requests.get$default(app32, str18, fetchHtml$buildHeaders32, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 30L, (Interceptor) null, false, (ResponseParser) null, mlsbdProvider$fetchHtml$12, 3836, (Object) null);
                            if (obj10 != obj) {
                            }
                        } catch (Throwable th13) {
                            th = th13;
                            obj4 = null;
                            mutex3 = mutex13;
                            mutex3.unlock(obj4);
                            throw th;
                        }
                        break;
                    case 5:
                        int i40 = mlsbdProvider$fetchHtml$12.I$3;
                        long j17 = mlsbdProvider$fetchHtml$12.J$1;
                        int i41 = mlsbdProvider$fetchHtml$12.I$2;
                        i22 = mlsbdProvider$fetchHtml$12.I$1;
                        i26 = mlsbdProvider$fetchHtml$12.I$0;
                        j10 = mlsbdProvider$fetchHtml$12.J$0;
                        obj9 = null;
                        Mutex mutex14 = (Mutex) mlsbdProvider$fetchHtml$12.L$2;
                        try {
                            ResultKt.throwOnFailure(obj11);
                            mutex10 = mutex14;
                            document = ((NiceResponse) obj11).getDocument();
                        } catch (Exception e12) {
                            mutex10 = mutex14;
                            document = Jsoup.parse("");
                            obj8 = obj9;
                            mutex9 = mutex10;
                            mutex9.unlock(obj8);
                            return document;
                        } catch (Throwable th14) {
                            th = th14;
                            obj4 = null;
                            mutex3 = mutex14;
                            mutex3.unlock(obj4);
                            throw th;
                        }
                        obj8 = obj9;
                        mutex9 = mutex10;
                        mutex9.unlock(obj8);
                        return document;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        mlsbdProvider$fetchHtml$1 = new MlsbdProvider$fetchHtml$1(mlsbdProvider, continuation);
        mlsbdProvider$fetchHtml$12 = mlsbdProvider$fetchHtml$1;
        Object obj112 = mlsbdProvider$fetchHtml$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        NiceResponse niceResponse22 = null;
        int i272 = 0;
        switch (mlsbdProvider$fetchHtml$12.label) {
        }
    }

    private static final Map<String, String> fetchHtml$buildHeaders(MlsbdProvider mlsbdProvider, String str) {
        Map<String, String> mutableMapOf = MapsKt.mutableMapOf(new Pair[]{TuplesKt.to("User-Agent", mlsbdProvider.defaultUserAgent), TuplesKt.to("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"), TuplesKt.to("Accept-Language", "en-US,en;q=0.9,bn;q=0.8")});
        String str2 = str;
        if (!(str2 == null || str2.length() == 0)) {
            mutableMapOf.put("Cookie", "cf_clearance=" + str);
        }
        return mutableMapOf;
    }

    static /* synthetic */ Map fetchHtml$buildHeaders$default(MlsbdProvider mlsbdProvider, String str, int i, Object obj) {
        if ((i & 2) != 0) {
            str = null;
        }
        return fetchHtml$buildHeaders(mlsbdProvider, str);
    }

    private static final boolean fetchHtml$isCloudflare(String str, int i) {
        switch (i) {
            case 403:
            case 503:
                return true;
            default:
                if (str.length() < 6000) {
                    return StringsKt.contains(str, "cf-browser-verification", true) || StringsKt.contains(str, "Checking if the site connection is secure", true) || StringsKt.contains(str, "Just a moment", true) || StringsKt.contains(str, "Attention Required! | Cloudflare", true) || StringsKt.contains(str, "Enable JavaScript and cookies to continue", true);
                }
                return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Object fetchViaWebView(String str, Continuation<? super Document> continuation) {
        Context context = appContext;
        if (context == null) {
            return null;
        }
        return BuildersKt.withContext(Dispatchers.getMain(), new MlsbdProvider$fetchViaWebView$2(context, str, this, null), continuation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit load$lambda$2(int i, Episode episode) {
        episode.setName("Episode " + i);
        episode.setSeason(1);
        episode.setEpisode(Integer.valueOf(i));
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
    /* JADX WARN: Failed to calculate best type for var: r19v0 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v2 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v25 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v26 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v32 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v33 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v34 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v40 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v41 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v50 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v51 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v53 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v6 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v86 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v90 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v91 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v95 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r2v96 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r3v16 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.applyInvokeTypes(TypeUpdate.java:390)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.invokeListener(TypeUpdate.java:355)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.applyInvokeTypes(TypeUpdate.java:372)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.invokeListener(TypeUpdate.java:355)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r3v17 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.applyInvokeTypes(TypeUpdate.java:390)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.invokeListener(TypeUpdate.java:355)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r4v57 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r9v12 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to set immutable type for var: r19v0 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:473)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.allSameListener(TypeUpdate.java:466)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:447)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:188)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.applyWithWiderIgnSame(TypeUpdate.java:70)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setImmutableType(TypeInferenceVisitor.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$1(TypeInferenceVisitor.java:100)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:100)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Not initialized variable reg: 4, insn: 0x0159: MOVE (r16 I:??[OBJECT, ARRAY]) = (r4 I:??[OBJECT, ARRAY]), block:B:178:0x0159 */
    public final java.lang.Object resolveCustomExtractor(java.lang.String r18, java.lang.String r19, kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> r20, kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> r21, kotlin.coroutines.Continuation<? super kotlin.Unit> r22) {
        /*
            Method dump skipped, instructions count: 1396
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.cncverse.MlsbdProvider.resolveCustomExtractor(java.lang.String, java.lang.String, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private final SearchResponse toResult(Element post) {
        Element selectFirst;
        String attr;
        Element selectFirst2 = post.selectFirst("h2, .post-title, .entry-title");
        if (selectFirst2 == null) {
            return null;
        }
        String obj = StringsKt.trim(selectFirst2.text()).toString();
        if ((obj.length() == 0) || (selectFirst = post.selectFirst("a[href]")) == null || (attr = selectFirst.attr("href")) == null) {
            return null;
        }
        Element selectFirst3 = post.selectFirst("picture img, img");
        String str = "";
        if (selectFirst3 != null) {
            String attr2 = selectFirst3.attr("data-src");
            if (attr2.length() == 0) {
                String attr3 = selectFirst3.attr("src");
                if (attr3.length() == 0) {
                    String str2 = (String) CollectionsKt.firstOrNull(StringsKt.split$default(selectFirst3.attr("srcset"), new String[]{" "}, false, 0, 6, (Object) null));
                    attr3 = str2 == null ? "" : str2;
                }
                attr2 = attr3;
            }
            String str3 = attr2;
            if (str3 != null) {
                str = str3;
            }
        }
        return MainAPIKt.newMovieSearchResponse$default(this, obj, attr, TvType.Movie, false, new MlsbdProvider$$ExternalSyntheticLambda1(str), 8, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit toResult$lambda$1(String str, MovieSearchResponse movieSearchResponse) {
        movieSearchResponse.setPosterUrl(str);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String unescapeJsString(String raw) {
        if (!StringsKt.startsWith$default(raw, "\"", false, 2, (Object) null) || !StringsKt.endsWith$default(raw, "\"", false, 2, (Object) null)) {
            return raw;
        }
        try {
            return new JSONTokener(raw).nextValue().toString();
        } catch (Exception e) {
            return StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.removeSurrounding(raw, "\""), "\\\"", "\"", false, 4, (Object) null), "\\n", "\n", false, 4, (Object) null), "\\t", "\t", false, 4, (Object) null), "\\\\", "\\", false, 4, (Object) null);
        }
    }

    public boolean getHasDownloadSupport() {
        return this.hasDownloadSupport;
    }

    public boolean getHasMainPage() {
        return this.hasMainPage;
    }

    public boolean getHasQuickSearch() {
        return this.hasQuickSearch;
    }

    @NotNull
    public String getLang() {
        return this.lang;
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0113  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0045  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getMainPage(int i, @NotNull MainPageRequest mainPageRequest, @NotNull Continuation<? super HomePageResponse> continuation) {
        MlsbdProvider$getMainPage$1 mlsbdProvider$getMainPage$1;
        String sb;
        Object fetchHtml;
        MainPageRequest mainPageRequest2;
        Iterator it;
        if (continuation instanceof MlsbdProvider$getMainPage$1) {
            mlsbdProvider$getMainPage$1 = (MlsbdProvider$getMainPage$1) continuation;
            if ((mlsbdProvider$getMainPage$1.label & Integer.MIN_VALUE) != 0) {
                mlsbdProvider$getMainPage$1.label -= Integer.MIN_VALUE;
                Object obj = mlsbdProvider$getMainPage$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (mlsbdProvider$getMainPage$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        DonationManager.INSTANCE.checkAndShow(getName());
                        if (StringsKt.isBlank(mainPageRequest.getData())) {
                            String mainUrl = getMainUrl();
                            sb = (i <= 1 ? new StringBuilder().append(mainUrl) : new StringBuilder().append(mainUrl).append("/page/").append(i)).append("/").toString();
                        } else {
                            String trim = StringsKt.trim(mainPageRequest.getData(), new char[]{'/'});
                            sb = (i <= 1 ? new StringBuilder().append(getMainUrl()).append("/").append(trim) : new StringBuilder().append(getMainUrl()).append("/").append(trim).append("/page/").append(i)).append("/").toString();
                        }
                        mlsbdProvider$getMainPage$1.L$0 = mainPageRequest;
                        mlsbdProvider$getMainPage$1.L$1 = SpillingKt.nullOutSpilledVariable(sb);
                        mlsbdProvider$getMainPage$1.I$0 = i;
                        mlsbdProvider$getMainPage$1.label = 1;
                        fetchHtml = fetchHtml(sb, mlsbdProvider$getMainPage$1);
                        if (fetchHtml != coroutine_suspended) {
                            mainPageRequest2 = mainPageRequest;
                            break;
                        } else {
                            return coroutine_suspended;
                        }
                    case 1:
                        int i2 = mlsbdProvider$getMainPage$1.I$0;
                        mainPageRequest2 = (MainPageRequest) mlsbdProvider$getMainPage$1.L$0;
                        ResultKt.throwOnFailure(obj);
                        fetchHtml = obj;
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                Iterable select = ((Document) fetchHtml).select("article, .post-item, .item, div.single-post");
                ArrayList arrayList = new ArrayList();
                it = select.iterator();
                while (it.hasNext()) {
                    SearchResponse result = toResult((Element) it.next());
                    if (result != null) {
                        arrayList.add(result);
                    }
                }
                return MainAPIKt.newHomePageResponse(new HomePageList(mainPageRequest2.getName(), arrayList, true), Boxing.boxBoolean(!r7.isEmpty()));
            }
        }
        mlsbdProvider$getMainPage$1 = new MlsbdProvider$getMainPage$1(this, continuation);
        Object obj2 = mlsbdProvider$getMainPage$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (mlsbdProvider$getMainPage$1.label) {
        }
        Iterable select2 = ((Document) fetchHtml).select("article, .post-item, .item, div.single-post");
        ArrayList arrayList2 = new ArrayList();
        it = select2.iterator();
        while (it.hasNext()) {
        }
        return MainAPIKt.newHomePageResponse(new HomePageList(mainPageRequest2.getName(), arrayList2, true), Boxing.boxBoolean(!r7.isEmpty()));
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

    /* JADX WARN: Code restructure failed: missing block: B:35:0x012c, code lost:
    
        if (r9 == null) goto L47;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0029. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0034  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0062  */
    /* JADX WARN: Removed duplicated region for block: B:144:0x028c  */
    /* JADX WARN: Removed duplicated region for block: B:148:0x0455 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:152:0x0406 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0090  */
    /* JADX WARN: Removed duplicated region for block: B:184:0x0519  */
    /* JADX WARN: Removed duplicated region for block: B:218:0x0621  */
    /* JADX WARN: Removed duplicated region for block: B:221:0x0625  */
    /* JADX WARN: Removed duplicated region for block: B:256:0x06cf  */
    /* JADX WARN: Removed duplicated region for block: B:293:0x0869 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:294:0x086a  */
    /* JADX WARN: Removed duplicated region for block: B:295:0x07f5  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x010d  */
    /* JADX WARN: Removed duplicated region for block: B:301:0x0179  */
    /* JADX WARN: Removed duplicated region for block: B:306:0x0195 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:317:0x009b  */
    /* JADX WARN: Removed duplicated region for block: B:88:0x0287  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002c  */
    /* JADX WARN: Removed duplicated region for block: B:92:0x029c  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object load(@NotNull String str, @NotNull Continuation<? super LoadResponse> continuation) {
        MlsbdProvider$load$1 mlsbdProvider$load$1;
        MlsbdProvider$load$1 mlsbdProvider$load$12;
        Object fetchHtml;
        String str2;
        Document document;
        Element selectFirst;
        String str3;
        String obj;
        String text;
        Element selectFirst2;
        String str4;
        Element selectFirst3;
        Regex regex;
        Collection collection;
        Iterator it;
        boolean z;
        int i;
        String str5;
        String str6;
        Object obj2;
        String str7;
        Regex regex2;
        String str8;
        String str9;
        ArrayList arrayList;
        Iterator it2;
        Document document2;
        String str10;
        String str11;
        boolean z2;
        String str12;
        Iterator it3;
        Document document3;
        Element element;
        Iterator it4;
        String str13;
        String str14;
        String str15;
        int i2;
        Element nextElementSibling;
        ArrayList arrayList2;
        Element element2;
        String str16;
        String str17;
        int i3;
        Element element3;
        MatchGroupCollection groups;
        MatchGroup matchGroup;
        String value;
        Integer intOrNull;
        MatchGroupCollection groups2;
        String value2;
        Integer intOrNull2;
        String text2;
        String obj3;
        String value3;
        String text3;
        if (continuation instanceof MlsbdProvider$load$1) {
            mlsbdProvider$load$1 = (MlsbdProvider$load$1) continuation;
            if ((mlsbdProvider$load$1.label & Integer.MIN_VALUE) != 0) {
                mlsbdProvider$load$1.label -= Integer.MIN_VALUE;
                mlsbdProvider$load$12 = mlsbdProvider$load$1;
                Object obj4 = mlsbdProvider$load$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (mlsbdProvider$load$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj4);
                        mlsbdProvider$load$12.L$0 = str;
                        mlsbdProvider$load$12.label = 1;
                        fetchHtml = fetchHtml(str, mlsbdProvider$load$12);
                        if (fetchHtml == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        str2 = str;
                        document = (Document) fetchHtml;
                        selectFirst = document.selectFirst("h1.entry-title, h1, .name");
                        str3 = "";
                        if (selectFirst != null || (text3 = selectFirst.text()) == null || (obj = StringsKt.trim(text3).toString()) == null) {
                            Element selectFirst4 = document.selectFirst("title");
                            obj = (selectFirst4 != null || (text = selectFirst4.text()) == null) ? "" : StringsKt.trim(text).toString();
                        }
                        MatchResult find$default = Regex.find$default(new Regex("(?<=\\()\\d{4}(?=\\))"), obj, 0, 2, (Object) null);
                        Integer intOrNull3 = (find$default != null || (value3 = find$default.getValue()) == null) ? null : StringsKt.toIntOrNull(value3);
                        selectFirst2 = document.selectFirst("img.aligncenter, .entry-content img, picture img");
                        if (selectFirst2 != null) {
                            String attr = selectFirst2.attr("src");
                            if (attr.length() == 0) {
                                attr = selectFirst2.attr("data-src");
                            }
                            str4 = attr;
                            break;
                        }
                        str4 = "";
                        String str18 = str4;
                        selectFirst3 = document.selectFirst(".storyline, .details, .entry-content");
                        if (selectFirst3 != null && (text2 = selectFirst3.text()) != null && (obj3 = StringsKt.trim(text2).toString()) != null) {
                            str3 = obj3;
                        }
                        Collection select = document.select("div.post-section-title.download, div.post-section-title");
                        regex = new Regex("Epi-?\\s*(\\d+)(?:-(\\d+))?", RegexOption.IGNORE_CASE);
                        collection = select;
                        if ((collection instanceof Collection) || !collection.isEmpty()) {
                            it = collection.iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    z = false;
                                } else if (regex.containsMatchIn(((Element) it.next()).text())) {
                                    z = true;
                                }
                            }
                        } else {
                            z = false;
                        }
                        i = (!z || StringsKt.contains(obj, "Season", true) || StringsKt.contains$default(str2, "/tv-series/", false, 2, (Object) null)) ? 1 : 0;
                        String str19 = "hd";
                        String str20 = "sd";
                        String str21 = "Dbtn";
                        String str22 = "post-section-title";
                        str5 = obj;
                        String str23 = "toLowerCase(...)";
                        String str24 = " ";
                        String str25 = str2;
                        String str26 = "href";
                        String str27 = "a";
                        if (i != 0 || select.isEmpty()) {
                            str6 = str3;
                            obj2 = coroutine_suspended;
                            str7 = "hd";
                            regex2 = regex;
                            str8 = "sd";
                            str9 = str5;
                        } else {
                            ArrayList arrayList3 = new ArrayList();
                            Iterator it5 = select.iterator();
                            while (it5.hasNext()) {
                                Element element4 = (Element) it5.next();
                                int i4 = i;
                                String str28 = str3;
                                MlsbdProvider$load$1 mlsbdProvider$load$13 = mlsbdProvider$load$12;
                                String str29 = str18;
                                MatchResult find$default2 = Regex.find$default(regex, element4.text(), 0, 2, (Object) null);
                                if (find$default2 != null && (groups2 = find$default2.getGroups()) != null) {
                                    MatchGroup matchGroup2 = groups2.get(1);
                                    if (matchGroup2 != null && (value2 = matchGroup2.getValue()) != null && (intOrNull2 = StringsKt.toIntOrNull(value2)) != null) {
                                        i2 = intOrNull2.intValue();
                                        int intValue = (find$default2 != null || (groups = find$default2.getGroups()) == null || (matchGroup = groups.get(2)) == null || (value = matchGroup.getValue()) == null || (intOrNull = StringsKt.toIntOrNull(value)) == null) ? i2 : intOrNull.intValue();
                                        nextElementSibling = element4.nextElementSibling();
                                        arrayList2 = new ArrayList();
                                        while (nextElementSibling != null && !nextElementSibling.hasClass("post-section-title")) {
                                            int i5 = i2;
                                            List<Element> listOf = !Intrinsics.areEqual(nextElementSibling.tagName(), "a") ? CollectionsKt.listOf(nextElementSibling) : nextElementSibling.select("a");
                                            for (Element element5 : listOf) {
                                                List list = listOf;
                                                Regex regex3 = regex;
                                                String obj5 = StringsKt.trim(element5.attr("href")).toString();
                                                if (obj5.length() > 0) {
                                                    if (element5.hasClass("Dbtn") || element5.hasClass(str20) || element5.hasClass(str19)) {
                                                        element2 = element5;
                                                        str16 = str19;
                                                        str17 = str20;
                                                        i3 = intValue;
                                                        element3 = nextElementSibling;
                                                    } else {
                                                        i3 = intValue;
                                                        if (element5.hasClass("hevc") || element5.hasClass("watch")) {
                                                            element2 = element5;
                                                            str16 = str19;
                                                            str17 = str20;
                                                            element3 = nextElementSibling;
                                                        } else {
                                                            element2 = element5;
                                                            str16 = str19;
                                                            str17 = str20;
                                                            element3 = nextElementSibling;
                                                            if (!StringsKt.contains$default(obj5, "savelinks", false, 2, (Object) null)) {
                                                                intValue = i3;
                                                                listOf = list;
                                                                regex = regex3;
                                                                nextElementSibling = element3;
                                                                str19 = str16;
                                                                str20 = str17;
                                                            }
                                                        }
                                                    }
                                                    String lowerCase = (element2.text() + " " + element2.className()).toLowerCase(Locale.ROOT);
                                                    Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
                                                    arrayList2.add((StringsKt.contains$default(lowerCase, "1080", false, 2, (Object) null) ? "1080p" : StringsKt.contains$default(lowerCase, "720", false, 2, (Object) null) ? "720p" : StringsKt.contains$default(lowerCase, "480", false, 2, (Object) null) ? "480p" : (StringsKt.contains$default(lowerCase, "4k", false, 2, (Object) null) || StringsKt.contains$default(lowerCase, "2160", false, 2, (Object) null)) ? "4K" : "Unknown") + "|" + obj5);
                                                    intValue = i3;
                                                    listOf = list;
                                                    regex = regex3;
                                                    nextElementSibling = element3;
                                                    str19 = str16;
                                                    str20 = str17;
                                                } else {
                                                    listOf = list;
                                                    regex = regex3;
                                                }
                                            }
                                            nextElementSibling = nextElementSibling.nextElementSibling();
                                            i2 = i5;
                                        }
                                        int i6 = i2;
                                        String str30 = str19;
                                        Regex regex4 = regex;
                                        String str31 = str20;
                                        int i7 = intValue;
                                        if (arrayList2.isEmpty()) {
                                            String joinToString$default = CollectionsKt.joinToString$default(arrayList2, " ; ", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null);
                                            int i8 = i6 <= 0 ? 1 : i6;
                                            int i9 = i7 <= 0 ? i8 : i7;
                                            int i10 = i8;
                                            if (i10 <= i9) {
                                                while (true) {
                                                    arrayList3.add(MainAPIKt.newEpisode(this, joinToString$default, new MlsbdProvider$$ExternalSyntheticLambda0(i10)));
                                                    if (i10 != i9) {
                                                        i10++;
                                                    }
                                                }
                                            }
                                            i = i4;
                                            mlsbdProvider$load$12 = mlsbdProvider$load$13;
                                            str3 = str28;
                                            str18 = str29;
                                            regex = regex4;
                                            str19 = str30;
                                            str20 = str31;
                                        } else {
                                            i = i4;
                                            mlsbdProvider$load$12 = mlsbdProvider$load$13;
                                            str3 = str28;
                                            str18 = str29;
                                            regex = regex4;
                                            str19 = str30;
                                            str20 = str31;
                                        }
                                    }
                                }
                                i2 = 0;
                                if (find$default2 != null) {
                                }
                                nextElementSibling = element4.nextElementSibling();
                                arrayList2 = new ArrayList();
                                while (nextElementSibling != null) {
                                    int i52 = i2;
                                    if (!Intrinsics.areEqual(nextElementSibling.tagName(), "a")) {
                                    }
                                    while (r39.hasNext()) {
                                    }
                                    nextElementSibling = nextElementSibling.nextElementSibling();
                                    i2 = i52;
                                }
                                int i62 = i2;
                                String str302 = str19;
                                Regex regex42 = regex;
                                String str312 = str20;
                                int i72 = intValue;
                                if (arrayList2.isEmpty()) {
                                }
                            }
                            int i11 = i;
                            String str32 = str3;
                            str7 = str19;
                            regex2 = regex;
                            str8 = str20;
                            MlsbdProvider$load$1 mlsbdProvider$load$14 = mlsbdProvider$load$12;
                            String str33 = str18;
                            if (!arrayList3.isEmpty()) {
                                TvType tvType = TvType.TvSeries;
                                MlsbdProvider$load$3 mlsbdProvider$load$3 = new MlsbdProvider$load$3(str33, intOrNull3, str32, null);
                                mlsbdProvider$load$14.L$0 = SpillingKt.nullOutSpilledVariable(str25);
                                mlsbdProvider$load$14.L$1 = SpillingKt.nullOutSpilledVariable(document);
                                mlsbdProvider$load$14.L$2 = SpillingKt.nullOutSpilledVariable(str5);
                                mlsbdProvider$load$14.L$3 = SpillingKt.nullOutSpilledVariable(intOrNull3);
                                mlsbdProvider$load$14.L$4 = SpillingKt.nullOutSpilledVariable(str33);
                                mlsbdProvider$load$14.L$5 = SpillingKt.nullOutSpilledVariable(str32);
                                mlsbdProvider$load$14.L$6 = SpillingKt.nullOutSpilledVariable(select);
                                mlsbdProvider$load$14.L$7 = SpillingKt.nullOutSpilledVariable(regex2);
                                mlsbdProvider$load$14.L$8 = SpillingKt.nullOutSpilledVariable(arrayList3);
                                mlsbdProvider$load$14.I$0 = i11;
                                mlsbdProvider$load$14.label = 2;
                                Object newTvSeriesLoadResponse = MainAPIKt.newTvSeriesLoadResponse(this, str5, str25, tvType, arrayList3, mlsbdProvider$load$3, mlsbdProvider$load$14);
                                return newTvSeriesLoadResponse == coroutine_suspended ? coroutine_suspended : newTvSeriesLoadResponse;
                            }
                            str9 = str5;
                            obj2 = coroutine_suspended;
                            i = i11;
                            mlsbdProvider$load$12 = mlsbdProvider$load$14;
                            str6 = str32;
                            str18 = str33;
                        }
                        arrayList = new ArrayList();
                        it2 = select.iterator();
                        while (it2.hasNext()) {
                            Object obj6 = obj2;
                            Element nextElementSibling2 = ((Element) it2.next()).nextElementSibling();
                            while (nextElementSibling2 != null && !nextElementSibling2.hasClass(str22)) {
                                String str34 = str9;
                                List<Element> listOf2 = Intrinsics.areEqual(nextElementSibling2.tagName(), str27) ? CollectionsKt.listOf(nextElementSibling2) : nextElementSibling2.select(str27);
                                for (Element element6 : listOf2) {
                                    Element element7 = nextElementSibling2;
                                    List list2 = listOf2;
                                    String obj7 = StringsKt.trim(element6.attr("href")).toString();
                                    if (obj7.length() > 0) {
                                        if (element6.hasClass(str21)) {
                                            element = element6;
                                            it4 = it2;
                                        } else {
                                            it4 = it2;
                                            String str35 = str8;
                                            if (element6.hasClass(str35)) {
                                                element = element6;
                                                str8 = str35;
                                            } else {
                                                str8 = str35;
                                                String str36 = str7;
                                                if (element6.hasClass(str36)) {
                                                    element = element6;
                                                    str7 = str36;
                                                } else {
                                                    str7 = str36;
                                                    if (element6.hasClass("hevc") || element6.hasClass("watch")) {
                                                        element = element6;
                                                    } else {
                                                        element = element6;
                                                        str13 = str21;
                                                        str14 = str27;
                                                        str15 = str22;
                                                        if (StringsKt.contains$default(obj7, "savelinks", false, 2, (Object) null)) {
                                                            String lowerCase2 = (element.text() + " " + element.className()).toLowerCase(Locale.ROOT);
                                                            Intrinsics.checkNotNullExpressionValue(lowerCase2, "toLowerCase(...)");
                                                            arrayList.add((!StringsKt.contains$default(lowerCase2, "1080", false, 2, (Object) null) ? "1080p" : StringsKt.contains$default(lowerCase2, "720", false, 2, (Object) null) ? "720p" : StringsKt.contains$default(lowerCase2, "480", false, 2, (Object) null) ? "480p" : (StringsKt.contains$default(lowerCase2, "4k", false, 2, (Object) null) || StringsKt.contains$default(lowerCase2, "2160", false, 2, (Object) null)) ? "4K" : "Unknown") + "|" + obj7);
                                                            it2 = it4;
                                                            nextElementSibling2 = element7;
                                                            listOf2 = list2;
                                                            str21 = str13;
                                                            str27 = str14;
                                                            str22 = str15;
                                                        } else {
                                                            it2 = it4;
                                                            nextElementSibling2 = element7;
                                                            listOf2 = list2;
                                                            str21 = str13;
                                                            str27 = str14;
                                                            str22 = str15;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        str13 = str21;
                                        str14 = str27;
                                        str15 = str22;
                                        String lowerCase22 = (element.text() + " " + element.className()).toLowerCase(Locale.ROOT);
                                        Intrinsics.checkNotNullExpressionValue(lowerCase22, "toLowerCase(...)");
                                        arrayList.add((!StringsKt.contains$default(lowerCase22, "1080", false, 2, (Object) null) ? "1080p" : StringsKt.contains$default(lowerCase22, "720", false, 2, (Object) null) ? "720p" : StringsKt.contains$default(lowerCase22, "480", false, 2, (Object) null) ? "480p" : (StringsKt.contains$default(lowerCase22, "4k", false, 2, (Object) null) || StringsKt.contains$default(lowerCase22, "2160", false, 2, (Object) null)) ? "4K" : "Unknown") + "|" + obj7);
                                        it2 = it4;
                                        nextElementSibling2 = element7;
                                        listOf2 = list2;
                                        str21 = str13;
                                        str27 = str14;
                                        str22 = str15;
                                    } else {
                                        nextElementSibling2 = element7;
                                        listOf2 = list2;
                                    }
                                }
                                nextElementSibling2 = nextElementSibling2.nextElementSibling();
                                str9 = str34;
                            }
                            obj2 = obj6;
                        }
                        Object obj8 = obj2;
                        String str37 = str9;
                        if (arrayList.isEmpty()) {
                            document2 = document;
                        } else {
                            Document document4 = document;
                            Iterable select2 = document4.select("a.Dbtn, a.sd, a.hd, a.hevc");
                            boolean z3 = false;
                            Iterator it6 = select2.iterator();
                            while (it6.hasNext()) {
                                Element element8 = (Element) it6.next();
                                Iterable iterable = select2;
                                String obj9 = StringsKt.trim(element8.attr(str26)).toString();
                                if (obj9.length() > 0) {
                                    z2 = z3;
                                    str12 = str26;
                                    it3 = it6;
                                    document3 = document4;
                                    if (StringsKt.contains$default(obj9, "savelinks", false, 2, (Object) null) || StringsKt.startsWith$default(obj9, "http", false, 2, (Object) null)) {
                                        String str38 = "Unknown";
                                        String lowerCase3 = (element8.text() + str24 + element8.className()).toLowerCase(Locale.ROOT);
                                        Intrinsics.checkNotNullExpressionValue(lowerCase3, str23);
                                        str10 = str23;
                                        str11 = str24;
                                        if (StringsKt.contains$default(lowerCase3, "1080", false, 2, (Object) null)) {
                                            str38 = "1080p";
                                        } else if (StringsKt.contains$default(lowerCase3, "720", false, 2, (Object) null)) {
                                            str38 = "720p";
                                        } else if (StringsKt.contains$default(lowerCase3, "480", false, 2, (Object) null)) {
                                            str38 = "480p";
                                        } else if (StringsKt.contains$default(lowerCase3, "4k", false, 2, (Object) null) || StringsKt.contains$default(lowerCase3, "2160", false, 2, (Object) null)) {
                                            str38 = "4K";
                                        }
                                        arrayList.add(str38 + "|" + obj9);
                                    } else {
                                        str10 = str23;
                                        str11 = str24;
                                    }
                                } else {
                                    str10 = str23;
                                    str11 = str24;
                                    z2 = z3;
                                    str12 = str26;
                                    it3 = it6;
                                    document3 = document4;
                                }
                                str23 = str10;
                                str24 = str11;
                                z3 = z2;
                                select2 = iterable;
                                str26 = str12;
                                it6 = it3;
                                document4 = document3;
                            }
                            document2 = document4;
                        }
                        TvType tvType2 = TvType.Movie;
                        String joinToString$default2 = CollectionsKt.joinToString$default(arrayList, " ; ", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null);
                        MlsbdProvider$load$5 mlsbdProvider$load$5 = new MlsbdProvider$load$5(str18, intOrNull3, str6, null);
                        mlsbdProvider$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str25);
                        mlsbdProvider$load$12.L$1 = SpillingKt.nullOutSpilledVariable(document2);
                        mlsbdProvider$load$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                        mlsbdProvider$load$12.L$3 = SpillingKt.nullOutSpilledVariable(intOrNull3);
                        mlsbdProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(str18);
                        mlsbdProvider$load$12.L$5 = SpillingKt.nullOutSpilledVariable(str6);
                        mlsbdProvider$load$12.L$6 = SpillingKt.nullOutSpilledVariable(select);
                        mlsbdProvider$load$12.L$7 = SpillingKt.nullOutSpilledVariable(regex2);
                        mlsbdProvider$load$12.L$8 = SpillingKt.nullOutSpilledVariable(arrayList);
                        mlsbdProvider$load$12.I$0 = i;
                        mlsbdProvider$load$12.label = 3;
                        obj4 = MainAPIKt.newMovieLoadResponse(this, str37, str25, tvType2, joinToString$default2, mlsbdProvider$load$5, mlsbdProvider$load$12);
                        return obj4 != obj8 ? obj8 : obj4;
                    case 1:
                        String str39 = (String) mlsbdProvider$load$12.L$0;
                        ResultKt.throwOnFailure(obj4);
                        fetchHtml = obj4;
                        str2 = str39;
                        document = (Document) fetchHtml;
                        selectFirst = document.selectFirst("h1.entry-title, h1, .name");
                        str3 = "";
                        if (selectFirst != null) {
                            break;
                        }
                        Element selectFirst42 = document.selectFirst("title");
                        if (selectFirst42 != null) {
                            break;
                        }
                        MatchResult find$default3 = Regex.find$default(new Regex("(?<=\\()\\d{4}(?=\\))"), obj, 0, 2, (Object) null);
                        if (find$default3 != null) {
                            break;
                        }
                        Integer intOrNull32 = (find$default3 != null || (value3 = find$default3.getValue()) == null) ? null : StringsKt.toIntOrNull(value3);
                        selectFirst2 = document.selectFirst("img.aligncenter, .entry-content img, picture img");
                        if (selectFirst2 != null) {
                        }
                        str4 = "";
                        String str182 = str4;
                        selectFirst3 = document.selectFirst(".storyline, .details, .entry-content");
                        if (selectFirst3 != null) {
                            str3 = obj3;
                            break;
                        }
                        Collection select3 = document.select("div.post-section-title.download, div.post-section-title");
                        regex = new Regex("Epi-?\\s*(\\d+)(?:-(\\d+))?", RegexOption.IGNORE_CASE);
                        collection = select3;
                        if (collection instanceof Collection) {
                            break;
                        }
                        it = collection.iterator();
                        while (true) {
                            if (it.hasNext()) {
                            }
                        }
                        if (z) {
                            break;
                        }
                        String str192 = "hd";
                        String str202 = "sd";
                        String str212 = "Dbtn";
                        String str222 = "post-section-title";
                        str5 = obj;
                        String str232 = "toLowerCase(...)";
                        String str242 = " ";
                        String str252 = str2;
                        String str262 = "href";
                        String str272 = "a";
                        if (i != 0) {
                            break;
                        }
                        str6 = str3;
                        obj2 = coroutine_suspended;
                        str7 = "hd";
                        regex2 = regex;
                        str8 = "sd";
                        str9 = str5;
                        arrayList = new ArrayList();
                        it2 = select3.iterator();
                        while (it2.hasNext()) {
                        }
                        Object obj82 = obj2;
                        String str372 = str9;
                        if (arrayList.isEmpty()) {
                        }
                        TvType tvType22 = TvType.Movie;
                        String joinToString$default22 = CollectionsKt.joinToString$default(arrayList, " ; ", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null);
                        MlsbdProvider$load$5 mlsbdProvider$load$52 = new MlsbdProvider$load$5(str182, intOrNull32, str6, null);
                        mlsbdProvider$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str252);
                        mlsbdProvider$load$12.L$1 = SpillingKt.nullOutSpilledVariable(document2);
                        mlsbdProvider$load$12.L$2 = SpillingKt.nullOutSpilledVariable(str372);
                        mlsbdProvider$load$12.L$3 = SpillingKt.nullOutSpilledVariable(intOrNull32);
                        mlsbdProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(str182);
                        mlsbdProvider$load$12.L$5 = SpillingKt.nullOutSpilledVariable(str6);
                        mlsbdProvider$load$12.L$6 = SpillingKt.nullOutSpilledVariable(select3);
                        mlsbdProvider$load$12.L$7 = SpillingKt.nullOutSpilledVariable(regex2);
                        mlsbdProvider$load$12.L$8 = SpillingKt.nullOutSpilledVariable(arrayList);
                        mlsbdProvider$load$12.I$0 = i;
                        mlsbdProvider$load$12.label = 3;
                        obj4 = MainAPIKt.newMovieLoadResponse(this, str372, str252, tvType22, joinToString$default22, mlsbdProvider$load$52, mlsbdProvider$load$12);
                        if (obj4 != obj82) {
                        }
                        break;
                    case 2:
                        int i12 = mlsbdProvider$load$12.I$0;
                        ResultKt.throwOnFailure(obj4);
                        return obj4;
                    case 3:
                        int i13 = mlsbdProvider$load$12.I$0;
                        ResultKt.throwOnFailure(obj4);
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        mlsbdProvider$load$1 = new MlsbdProvider$load$1(this, continuation);
        mlsbdProvider$load$12 = mlsbdProvider$load$1;
        Object obj42 = mlsbdProvider$load$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (mlsbdProvider$load$12.label) {
        }
    }

    /* JADX WARN: Can't wrap try/catch for region: R(15:104|105|67|68|69|70|71|72|73|74|75|76|77|78|(0)(0)) */
    /* JADX WARN: Code restructure failed: missing block: B:102:0x041e, code lost:
    
        r23 = r1;
        r20 = r9;
        r22 = r10;
        r16 = r15;
        r10 = r21;
        r24 = r37;
        r9 = r38;
        r15 = r2;
        r21 = r18;
        r2 = r41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:92:0x03eb, code lost:
    
        r23 = r42;
        r20 = r9;
        r22 = r10;
        r16 = r15;
        r10 = r21;
        r3 = r34;
        r24 = r37;
        r9 = r38;
        r15 = r2;
        r21 = r18;
        r2 = r41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:96:0x0409, code lost:
    
        r23 = r42;
        r20 = r9;
        r22 = r10;
        r16 = r15;
        r10 = r21;
        r24 = r37;
        r9 = r38;
        r15 = r2;
        r21 = r18;
        r2 = r41;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0028. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:110:0x0669  */
    /* JADX WARN: Removed duplicated region for block: B:112:0x0085  */
    /* JADX WARN: Removed duplicated region for block: B:113:0x0116  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0033  */
    /* JADX WARN: Removed duplicated region for block: B:120:0x017b  */
    /* JADX WARN: Removed duplicated region for block: B:127:0x01eb  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x0221  */
    /* JADX WARN: Removed duplicated region for block: B:41:0x0450  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x0484  */
    /* JADX WARN: Removed duplicated region for block: B:63:0x05c2  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x03b1 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:81:0x03b2  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:102:0x064e -> B:13:0x0663). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:31:0x062f -> B:12:0x0645). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:58:0x05a0 -> B:13:0x0663). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:59:0x05c2 -> B:13:0x0663). Please report as a decompilation issue!!! */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object loadLinks(@NotNull String str, boolean z, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Boolean> continuation) {
        MlsbdProvider$loadLinks$1 mlsbdProvider$loadLinks$1;
        MlsbdProvider mlsbdProvider;
        MlsbdProvider$loadLinks$1 mlsbdProvider$loadLinks$12;
        Function1<? super ExtractorLink, Unit> function13;
        Object obj;
        Continuation<? super Boolean> continuation2;
        MlsbdProvider$loadLinks$1 mlsbdProvider$loadLinks$13;
        Object obj2;
        Object obj3;
        int i;
        Iterator it;
        String str2;
        boolean z2;
        Function1<? super SubtitleFile, Unit> function14;
        MlsbdProvider mlsbdProvider2;
        int i2;
        int i3;
        boolean z3;
        String str3;
        Iterator it2;
        String str4;
        List list;
        String str5;
        Function1<? super ExtractorLink, Unit> function15;
        String str6;
        Iterator it3;
        Object obj4;
        String str7;
        Object obj5;
        Continuation<? super Boolean> continuation3;
        Object obj6;
        Function1<? super SubtitleFile, Unit> function16;
        String str8;
        String str9;
        Object obj7;
        Function1<? super SubtitleFile, Unit> function17;
        Function1<? super ExtractorLink, Unit> function18;
        String str10;
        Exception exc;
        String str11;
        Iterator it4;
        Object obj8;
        Object obj9;
        String str12;
        Continuation<? super Boolean> continuation4;
        Exception exc2;
        boolean z4;
        int i4;
        int i5;
        Object obj10;
        MlsbdProvider mlsbdProvider3;
        String str13;
        List list2;
        Object obj11;
        Object obj12;
        String str14;
        Iterator it5;
        Iterable iterable;
        int i6;
        int i7;
        Iterator it6;
        Set set;
        Function1<? super SubtitleFile, Unit> function19;
        MlsbdProvider$loadLinks$1 mlsbdProvider$loadLinks$14;
        MlsbdProvider mlsbdProvider4;
        String str15;
        Document document;
        boolean z5;
        int i8;
        Function1<? super ExtractorLink, Unit> function110;
        Object obj13;
        int i9;
        String str16;
        Document document2;
        Document document3;
        Object obj14;
        Document document4;
        Function1<? super ExtractorLink, Unit> function111;
        Iterator it7;
        Set set2;
        Continuation<? super Boolean> continuation5;
        MlsbdProvider mlsbdProvider5;
        Object obj15;
        String str17;
        Function1<? super SubtitleFile, Unit> function112;
        int i10;
        int i11;
        boolean z6;
        Iterable iterable2;
        boolean z7;
        int i12;
        String str18;
        Object obj16;
        Continuation<? super Boolean> continuation6 = continuation;
        if (continuation6 instanceof MlsbdProvider$loadLinks$1) {
            mlsbdProvider$loadLinks$1 = (MlsbdProvider$loadLinks$1) continuation6;
            if ((mlsbdProvider$loadLinks$1.label & Integer.MIN_VALUE) != 0) {
                mlsbdProvider$loadLinks$1.label -= Integer.MIN_VALUE;
                mlsbdProvider = this;
                mlsbdProvider$loadLinks$12 = mlsbdProvider$loadLinks$1;
                Object obj17 = mlsbdProvider$loadLinks$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (mlsbdProvider$loadLinks$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj17);
                        List split$default = StringsKt.split$default(str, new String[]{" ; "}, false, 0, 6, (Object) null);
                        function13 = function12;
                        obj = split$default;
                        continuation2 = continuation6;
                        mlsbdProvider$loadLinks$13 = mlsbdProvider$loadLinks$12;
                        obj2 = obj17;
                        obj3 = coroutine_suspended;
                        i = 0;
                        it = split$default.iterator();
                        str2 = str;
                        z2 = z;
                        function14 = function1;
                        mlsbdProvider2 = mlsbdProvider;
                        if (it.hasNext()) {
                            obj7 = it.next();
                            str10 = (String) obj7;
                            List split$default2 = StringsKt.split$default(str10, new String[]{"|"}, false, 0, 6, (Object) null);
                            String str19 = str2;
                            String str20 = split$default2.size() > 1 ? (String) split$default2.get(0) : "";
                            String str21 = split$default2.size() > 1 ? (String) split$default2.get(1) : str10;
                            String obj18 = StringsKt.trim(str21).toString();
                            if (obj18.length() == 0) {
                                str2 = str19;
                                i9 = i;
                                obj13 = obj;
                                obj = obj13;
                                i = i9;
                                if (it.hasNext()) {
                                }
                            } else {
                                Continuation<? super Boolean> continuation7 = continuation2;
                                Object obj19 = obj2;
                                obj6 = obj;
                                if (StringsKt.contains$default(obj18, "savelinks", false, 2, (Object) null)) {
                                    try {
                                    } catch (Exception e) {
                                        e = e;
                                        str6 = obj18;
                                        str4 = str10;
                                        str8 = str19;
                                        str5 = str21;
                                        continuation3 = continuation7;
                                        str7 = str20;
                                        i2 = 0;
                                        function16 = function14;
                                        function15 = function13;
                                        i3 = i;
                                        obj4 = obj7;
                                        obj5 = obj19;
                                        z3 = z2;
                                        mlsbdProvider$loadLinks$12 = mlsbdProvider$loadLinks$13;
                                        it3 = it;
                                        list = split$default2;
                                        exc2 = e;
                                        Requests app = MainActivityKt.getApp();
                                        mlsbdProvider$loadLinks$12.L$0 = SpillingKt.nullOutSpilledVariable(str8);
                                        mlsbdProvider$loadLinks$12.L$1 = function16;
                                        mlsbdProvider$loadLinks$12.L$2 = function15;
                                        mlsbdProvider$loadLinks$12.L$3 = SpillingKt.nullOutSpilledVariable(obj6);
                                        mlsbdProvider$loadLinks$12.L$4 = it3;
                                        mlsbdProvider$loadLinks$12.L$5 = SpillingKt.nullOutSpilledVariable(obj4);
                                        mlsbdProvider$loadLinks$12.L$6 = SpillingKt.nullOutSpilledVariable(str4);
                                        mlsbdProvider$loadLinks$12.L$7 = str7;
                                        mlsbdProvider$loadLinks$12.L$8 = SpillingKt.nullOutSpilledVariable(list);
                                        mlsbdProvider$loadLinks$12.L$9 = SpillingKt.nullOutSpilledVariable(str5);
                                        mlsbdProvider$loadLinks$12.L$10 = SpillingKt.nullOutSpilledVariable(str6);
                                        mlsbdProvider$loadLinks$12.L$11 = SpillingKt.nullOutSpilledVariable(exc2);
                                        Function1<? super SubtitleFile, Unit> function113 = function16;
                                        mlsbdProvider$loadLinks$12.L$12 = null;
                                        mlsbdProvider$loadLinks$12.L$13 = null;
                                        mlsbdProvider$loadLinks$12.L$14 = null;
                                        mlsbdProvider$loadLinks$12.L$15 = null;
                                        mlsbdProvider$loadLinks$12.L$16 = null;
                                        mlsbdProvider$loadLinks$12.L$17 = null;
                                        mlsbdProvider$loadLinks$12.Z$0 = z3;
                                        mlsbdProvider$loadLinks$12.I$0 = i3;
                                        mlsbdProvider$loadLinks$12.I$1 = i2;
                                        mlsbdProvider$loadLinks$12.label = 2;
                                        MlsbdProvider$loadLinks$1 mlsbdProvider$loadLinks$15 = mlsbdProvider$loadLinks$12;
                                        obj14 = Requests.get$default(app, str6, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, mlsbdProvider$loadLinks$15, 4094, (Object) null);
                                        if (obj14 == obj3) {
                                        }
                                    }
                                    mlsbdProvider$loadLinks$13.L$0 = SpillingKt.nullOutSpilledVariable(str19);
                                    mlsbdProvider$loadLinks$13.L$1 = function14;
                                    mlsbdProvider$loadLinks$13.L$2 = function13;
                                    mlsbdProvider$loadLinks$13.L$3 = SpillingKt.nullOutSpilledVariable(obj6);
                                    mlsbdProvider$loadLinks$13.L$4 = it;
                                    mlsbdProvider$loadLinks$13.L$5 = SpillingKt.nullOutSpilledVariable(obj7);
                                    mlsbdProvider$loadLinks$13.L$6 = SpillingKt.nullOutSpilledVariable(str10);
                                    mlsbdProvider$loadLinks$13.L$7 = str20;
                                    mlsbdProvider$loadLinks$13.L$8 = SpillingKt.nullOutSpilledVariable(split$default2);
                                    mlsbdProvider$loadLinks$13.L$9 = SpillingKt.nullOutSpilledVariable(str21);
                                    mlsbdProvider$loadLinks$13.L$10 = obj18;
                                    mlsbdProvider$loadLinks$13.L$11 = null;
                                    mlsbdProvider$loadLinks$13.L$12 = null;
                                    mlsbdProvider$loadLinks$13.L$13 = null;
                                    mlsbdProvider$loadLinks$13.L$14 = null;
                                    mlsbdProvider$loadLinks$13.L$15 = null;
                                    mlsbdProvider$loadLinks$13.L$16 = null;
                                    mlsbdProvider$loadLinks$13.L$17 = null;
                                    mlsbdProvider$loadLinks$13.Z$0 = z2;
                                    mlsbdProvider$loadLinks$13.I$0 = i;
                                    mlsbdProvider$loadLinks$13.I$1 = 0;
                                    mlsbdProvider$loadLinks$13.label = 1;
                                    Object fetchHtml = mlsbdProvider2.fetchHtml(obj18, mlsbdProvider$loadLinks$13);
                                    if (fetchHtml == obj3) {
                                        return obj3;
                                    }
                                    str9 = str19;
                                    str5 = str21;
                                    str7 = str20;
                                    i2 = 0;
                                    function17 = function14;
                                    function18 = function13;
                                    i3 = i;
                                    continuation6 = continuation7;
                                    obj5 = obj19;
                                    z3 = z2;
                                    mlsbdProvider$loadLinks$12 = mlsbdProvider$loadLinks$13;
                                    it2 = it;
                                    list = split$default2;
                                    str3 = obj18;
                                    mlsbdProvider = mlsbdProvider2;
                                    obj17 = fetchHtml;
                                    try {
                                    } catch (Exception e2) {
                                        e = e2;
                                        continuation3 = continuation6;
                                        mlsbdProvider2 = mlsbdProvider;
                                        str4 = str10;
                                        function15 = function18;
                                        function16 = function17;
                                        str8 = str9;
                                        str6 = str3;
                                        it3 = it2;
                                        obj4 = obj7;
                                        exc2 = e;
                                        Requests app2 = MainActivityKt.getApp();
                                        mlsbdProvider$loadLinks$12.L$0 = SpillingKt.nullOutSpilledVariable(str8);
                                        mlsbdProvider$loadLinks$12.L$1 = function16;
                                        mlsbdProvider$loadLinks$12.L$2 = function15;
                                        mlsbdProvider$loadLinks$12.L$3 = SpillingKt.nullOutSpilledVariable(obj6);
                                        mlsbdProvider$loadLinks$12.L$4 = it3;
                                        mlsbdProvider$loadLinks$12.L$5 = SpillingKt.nullOutSpilledVariable(obj4);
                                        mlsbdProvider$loadLinks$12.L$6 = SpillingKt.nullOutSpilledVariable(str4);
                                        mlsbdProvider$loadLinks$12.L$7 = str7;
                                        mlsbdProvider$loadLinks$12.L$8 = SpillingKt.nullOutSpilledVariable(list);
                                        mlsbdProvider$loadLinks$12.L$9 = SpillingKt.nullOutSpilledVariable(str5);
                                        mlsbdProvider$loadLinks$12.L$10 = SpillingKt.nullOutSpilledVariable(str6);
                                        mlsbdProvider$loadLinks$12.L$11 = SpillingKt.nullOutSpilledVariable(exc2);
                                        Function1<? super SubtitleFile, Unit> function1132 = function16;
                                        mlsbdProvider$loadLinks$12.L$12 = null;
                                        mlsbdProvider$loadLinks$12.L$13 = null;
                                        mlsbdProvider$loadLinks$12.L$14 = null;
                                        mlsbdProvider$loadLinks$12.L$15 = null;
                                        mlsbdProvider$loadLinks$12.L$16 = null;
                                        mlsbdProvider$loadLinks$12.L$17 = null;
                                        mlsbdProvider$loadLinks$12.Z$0 = z3;
                                        mlsbdProvider$loadLinks$12.I$0 = i3;
                                        mlsbdProvider$loadLinks$12.I$1 = i2;
                                        mlsbdProvider$loadLinks$12.label = 2;
                                        MlsbdProvider$loadLinks$1 mlsbdProvider$loadLinks$152 = mlsbdProvider$loadLinks$12;
                                        obj14 = Requests.get$default(app2, str6, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, mlsbdProvider$loadLinks$152, 4094, (Object) null);
                                        if (obj14 == obj3) {
                                        }
                                    }
                                    document3 = (Document) obj17;
                                    i9 = i3;
                                    it6 = it2;
                                    obj13 = obj6;
                                    i5 = i2;
                                    obj10 = obj5;
                                    mlsbdProvider3 = mlsbdProvider;
                                    continuation4 = continuation6;
                                    if (document3 == null) {
                                        LinkedHashSet linkedHashSet = new LinkedHashSet();
                                        Iterable iterable3 = (Iterable) document3.select("ul li a[href^=http], a.btn[href^=http], .card a[href^=http], a[href*='file/'], a[href*='view/']");
                                        Iterator it8 = iterable3.iterator();
                                        continuation5 = continuation4;
                                        mlsbdProvider5 = mlsbdProvider3;
                                        obj15 = obj10;
                                        z6 = z3;
                                        function111 = function18;
                                        i11 = i9;
                                        iterable2 = iterable3;
                                        mlsbdProvider$loadLinks$14 = mlsbdProvider$loadLinks$12;
                                        str17 = str7;
                                        set2 = linkedHashSet;
                                        function112 = function17;
                                        i6 = 0;
                                        document4 = document3;
                                        i10 = i5;
                                        it7 = it8;
                                        while (it7.hasNext()) {
                                            Object next = it7.next();
                                            String str22 = str3;
                                            Element element = (Element) next;
                                            String str23 = str5;
                                            List list3 = list;
                                            String obj20 = StringsKt.trim(element.attr("href")).toString();
                                            if ((obj20.length() > 0) && set2.add(obj20)) {
                                                mlsbdProvider$loadLinks$14.L$0 = SpillingKt.nullOutSpilledVariable(str9);
                                                mlsbdProvider$loadLinks$14.L$1 = function112;
                                                mlsbdProvider$loadLinks$14.L$2 = function111;
                                                mlsbdProvider$loadLinks$14.L$3 = SpillingKt.nullOutSpilledVariable(obj13);
                                                mlsbdProvider$loadLinks$14.L$4 = it6;
                                                mlsbdProvider$loadLinks$14.L$5 = SpillingKt.nullOutSpilledVariable(obj7);
                                                mlsbdProvider$loadLinks$14.L$6 = SpillingKt.nullOutSpilledVariable(str10);
                                                mlsbdProvider$loadLinks$14.L$7 = str17;
                                                mlsbdProvider$loadLinks$14.L$8 = SpillingKt.nullOutSpilledVariable(list3);
                                                mlsbdProvider$loadLinks$14.L$9 = SpillingKt.nullOutSpilledVariable(str23);
                                                mlsbdProvider$loadLinks$14.L$10 = SpillingKt.nullOutSpilledVariable(str22);
                                                mlsbdProvider$loadLinks$14.L$11 = set2;
                                                mlsbdProvider$loadLinks$14.L$12 = SpillingKt.nullOutSpilledVariable(iterable2);
                                                mlsbdProvider$loadLinks$14.L$13 = it7;
                                                mlsbdProvider$loadLinks$14.L$14 = SpillingKt.nullOutSpilledVariable(document4);
                                                mlsbdProvider$loadLinks$14.L$15 = SpillingKt.nullOutSpilledVariable(next);
                                                mlsbdProvider$loadLinks$14.L$16 = SpillingKt.nullOutSpilledVariable(element);
                                                mlsbdProvider$loadLinks$14.L$17 = SpillingKt.nullOutSpilledVariable(obj20);
                                                mlsbdProvider$loadLinks$14.Z$0 = z6;
                                                mlsbdProvider$loadLinks$14.I$0 = i11;
                                                mlsbdProvider$loadLinks$14.I$1 = i10;
                                                mlsbdProvider$loadLinks$14.I$2 = i6;
                                                mlsbdProvider$loadLinks$14.I$3 = 0;
                                                mlsbdProvider$loadLinks$14.label = 3;
                                                int i13 = i11;
                                                str14 = str17;
                                                int i14 = i10;
                                                Iterator it9 = it7;
                                                function110 = function111;
                                                set = set2;
                                                boolean z8 = z6;
                                                function19 = function112;
                                                mlsbdProvider4 = mlsbdProvider5;
                                                if (mlsbdProvider4.resolveCustomExtractor(obj20, str14, function19, function110, mlsbdProvider$loadLinks$14) == obj3) {
                                                    return obj3;
                                                }
                                                document = document4;
                                                obj11 = obj7;
                                                list2 = list3;
                                                it5 = it9;
                                                obj12 = obj13;
                                                iterable = iterable2;
                                                str13 = str23;
                                                i7 = i13;
                                                z5 = z8;
                                                obj17 = obj15;
                                                str15 = str22;
                                                i8 = i14;
                                                continuation6 = continuation5;
                                                int i15 = i6;
                                                document4 = document;
                                                obj7 = obj11;
                                                Object obj21 = obj3;
                                                Iterator it10 = it6;
                                                String str24 = str15;
                                                function111 = function110;
                                                it7 = it5;
                                                set2 = set;
                                                continuation5 = continuation6;
                                                mlsbdProvider5 = mlsbdProvider4;
                                                obj15 = obj17;
                                                str17 = str14;
                                                function112 = function19;
                                                i10 = i8;
                                                str3 = str24;
                                                i11 = i7;
                                                it6 = it10;
                                                z6 = z5;
                                                obj3 = obj21;
                                                i6 = i15;
                                                str5 = str13;
                                                list = list2;
                                                obj13 = obj12;
                                                iterable2 = iterable;
                                                while (it7.hasNext()) {
                                                }
                                            } else {
                                                function111 = function111;
                                                it7 = it7;
                                                set2 = set2;
                                                continuation5 = continuation5;
                                                mlsbdProvider5 = mlsbdProvider5;
                                                obj15 = obj15;
                                                str17 = str17;
                                                function112 = function112;
                                                i10 = i10;
                                                str3 = str22;
                                                i11 = i11;
                                                it6 = it6;
                                                z6 = z6;
                                                obj3 = obj3;
                                                i6 = i6;
                                                str5 = str23;
                                                list = list3;
                                                obj13 = obj13;
                                                iterable2 = iterable2;
                                            }
                                        }
                                        boolean z9 = z6;
                                        Function1<? super SubtitleFile, Unit> function114 = function112;
                                        obj2 = obj15;
                                        i9 = i11;
                                        mlsbdProvider2 = mlsbdProvider5;
                                        z2 = z9;
                                        it = it6;
                                        str2 = str9;
                                        continuation2 = continuation5;
                                        mlsbdProvider$loadLinks$13 = mlsbdProvider$loadLinks$14;
                                        function13 = function111;
                                        function14 = function114;
                                        obj = obj13;
                                        i = i9;
                                        if (it.hasNext()) {
                                            return Boxing.boxBoolean(true);
                                        }
                                    } else {
                                        mlsbdProvider$loadLinks$13 = mlsbdProvider$loadLinks$12;
                                        mlsbdProvider2 = mlsbdProvider3;
                                        obj2 = obj10;
                                        z2 = z3;
                                        it = it6;
                                        function13 = function18;
                                        function14 = function17;
                                        str2 = str9;
                                        continuation2 = continuation4;
                                        obj = obj13;
                                        i = i9;
                                        if (it.hasNext()) {
                                        }
                                    }
                                } else {
                                    mlsbdProvider$loadLinks$13.L$0 = SpillingKt.nullOutSpilledVariable(str19);
                                    mlsbdProvider$loadLinks$13.L$1 = function14;
                                    mlsbdProvider$loadLinks$13.L$2 = function13;
                                    mlsbdProvider$loadLinks$13.L$3 = SpillingKt.nullOutSpilledVariable(obj6);
                                    mlsbdProvider$loadLinks$13.L$4 = it;
                                    mlsbdProvider$loadLinks$13.L$5 = SpillingKt.nullOutSpilledVariable(obj7);
                                    mlsbdProvider$loadLinks$13.L$6 = SpillingKt.nullOutSpilledVariable(str10);
                                    mlsbdProvider$loadLinks$13.L$7 = SpillingKt.nullOutSpilledVariable(str20);
                                    mlsbdProvider$loadLinks$13.L$8 = SpillingKt.nullOutSpilledVariable(split$default2);
                                    mlsbdProvider$loadLinks$13.L$9 = SpillingKt.nullOutSpilledVariable(str21);
                                    mlsbdProvider$loadLinks$13.L$10 = SpillingKt.nullOutSpilledVariable(obj18);
                                    mlsbdProvider$loadLinks$13.L$11 = null;
                                    mlsbdProvider$loadLinks$13.L$12 = null;
                                    mlsbdProvider$loadLinks$13.L$13 = null;
                                    mlsbdProvider$loadLinks$13.L$14 = null;
                                    mlsbdProvider$loadLinks$13.L$15 = null;
                                    mlsbdProvider$loadLinks$13.L$16 = null;
                                    mlsbdProvider$loadLinks$13.L$17 = null;
                                    mlsbdProvider$loadLinks$13.Z$0 = z2;
                                    mlsbdProvider$loadLinks$13.I$0 = i;
                                    mlsbdProvider$loadLinks$13.I$1 = 0;
                                    mlsbdProvider$loadLinks$13.label = 4;
                                    if (mlsbdProvider2.resolveCustomExtractor(obj18, str20, function14, function13, mlsbdProvider$loadLinks$13) == obj3) {
                                        return obj3;
                                    }
                                    z7 = z2;
                                    mlsbdProvider$loadLinks$12 = mlsbdProvider$loadLinks$13;
                                    i12 = i;
                                    continuation6 = continuation7;
                                    str18 = str19;
                                    obj16 = obj19;
                                    mlsbdProvider$loadLinks$13 = mlsbdProvider$loadLinks$12;
                                    i9 = i12;
                                    z2 = z7;
                                    obj13 = obj6;
                                    continuation2 = continuation6;
                                    str2 = str18;
                                    obj2 = obj16;
                                    obj = obj13;
                                    i = i9;
                                    if (it.hasNext()) {
                                    }
                                }
                            }
                        }
                        break;
                    case 1:
                        i2 = mlsbdProvider$loadLinks$12.I$1;
                        i3 = mlsbdProvider$loadLinks$12.I$0;
                        z3 = mlsbdProvider$loadLinks$12.Z$0;
                        str3 = (String) mlsbdProvider$loadLinks$12.L$10;
                        String str25 = (String) mlsbdProvider$loadLinks$12.L$9;
                        List list4 = (List) mlsbdProvider$loadLinks$12.L$8;
                        String str26 = (String) mlsbdProvider$loadLinks$12.L$7;
                        String str27 = (String) mlsbdProvider$loadLinks$12.L$6;
                        Object obj22 = mlsbdProvider$loadLinks$12.L$5;
                        it2 = (Iterator) mlsbdProvider$loadLinks$12.L$4;
                        Object obj23 = (Iterable) mlsbdProvider$loadLinks$12.L$3;
                        Function1<? super ExtractorLink, Unit> function115 = (Function1) mlsbdProvider$loadLinks$12.L$2;
                        Function1<? super SubtitleFile, Unit> function116 = (Function1) mlsbdProvider$loadLinks$12.L$1;
                        String str28 = (String) mlsbdProvider$loadLinks$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj17);
                            obj6 = obj23;
                            str9 = str28;
                            obj7 = obj22;
                            function17 = function116;
                            str7 = str26;
                            function18 = function115;
                            obj3 = coroutine_suspended;
                            str10 = str27;
                            obj5 = obj17;
                            list = list4;
                            str5 = str25;
                        } catch (Exception e3) {
                            e = e3;
                            mlsbdProvider2 = mlsbdProvider;
                            str4 = str27;
                            list = list4;
                            str5 = str25;
                            function15 = function115;
                            str6 = str3;
                            it3 = it2;
                            obj4 = obj22;
                            str7 = str26;
                            obj3 = coroutine_suspended;
                            obj5 = obj17;
                            continuation3 = continuation6;
                            obj6 = obj23;
                            function16 = function116;
                            str8 = str28;
                            exc2 = e;
                            Requests app22 = MainActivityKt.getApp();
                            mlsbdProvider$loadLinks$12.L$0 = SpillingKt.nullOutSpilledVariable(str8);
                            mlsbdProvider$loadLinks$12.L$1 = function16;
                            mlsbdProvider$loadLinks$12.L$2 = function15;
                            mlsbdProvider$loadLinks$12.L$3 = SpillingKt.nullOutSpilledVariable(obj6);
                            mlsbdProvider$loadLinks$12.L$4 = it3;
                            mlsbdProvider$loadLinks$12.L$5 = SpillingKt.nullOutSpilledVariable(obj4);
                            mlsbdProvider$loadLinks$12.L$6 = SpillingKt.nullOutSpilledVariable(str4);
                            mlsbdProvider$loadLinks$12.L$7 = str7;
                            mlsbdProvider$loadLinks$12.L$8 = SpillingKt.nullOutSpilledVariable(list);
                            mlsbdProvider$loadLinks$12.L$9 = SpillingKt.nullOutSpilledVariable(str5);
                            mlsbdProvider$loadLinks$12.L$10 = SpillingKt.nullOutSpilledVariable(str6);
                            mlsbdProvider$loadLinks$12.L$11 = SpillingKt.nullOutSpilledVariable(exc2);
                            Function1<? super SubtitleFile, Unit> function11322 = function16;
                            mlsbdProvider$loadLinks$12.L$12 = null;
                            mlsbdProvider$loadLinks$12.L$13 = null;
                            mlsbdProvider$loadLinks$12.L$14 = null;
                            mlsbdProvider$loadLinks$12.L$15 = null;
                            mlsbdProvider$loadLinks$12.L$16 = null;
                            mlsbdProvider$loadLinks$12.L$17 = null;
                            mlsbdProvider$loadLinks$12.Z$0 = z3;
                            mlsbdProvider$loadLinks$12.I$0 = i3;
                            mlsbdProvider$loadLinks$12.I$1 = i2;
                            mlsbdProvider$loadLinks$12.label = 2;
                            MlsbdProvider$loadLinks$1 mlsbdProvider$loadLinks$1522 = mlsbdProvider$loadLinks$12;
                            obj14 = Requests.get$default(app22, str6, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, mlsbdProvider$loadLinks$1522, 4094, (Object) null);
                            if (obj14 == obj3) {
                                return obj3;
                            }
                            function17 = function11322;
                            it4 = it3;
                            function18 = function15;
                            obj9 = obj4;
                            str11 = str6;
                            mlsbdProvider$loadLinks$12 = mlsbdProvider$loadLinks$1522;
                            str9 = str8;
                            str12 = str4;
                            z4 = z3;
                            obj8 = obj6;
                            continuation4 = continuation3;
                            i4 = i3;
                            i5 = i2;
                            obj10 = obj5;
                            mlsbdProvider3 = mlsbdProvider2;
                            obj17 = obj14;
                            try {
                                Document document5 = ((NiceResponse) obj17).getDocument();
                                Object obj24 = obj8;
                                str10 = str12;
                                obj13 = obj24;
                                str16 = str7;
                                document2 = document5;
                                i9 = i4;
                                z3 = z4;
                                str3 = str11;
                                it6 = it4;
                                obj7 = obj9;
                            } catch (Exception e4) {
                                mlsbdProvider2 = mlsbdProvider3;
                                obj5 = obj10;
                                i2 = i5;
                                i3 = i4;
                                z3 = z4;
                                exc = exc2;
                                Object obj25 = obj8;
                                str10 = str12;
                                obj13 = obj25;
                                i9 = i3;
                                i5 = i2;
                                obj10 = obj5;
                                mlsbdProvider3 = mlsbdProvider2;
                                str16 = str7;
                                document2 = null;
                                str3 = str11;
                                it6 = it4;
                                obj7 = obj9;
                                Document document6 = document2;
                                str7 = str16;
                                document3 = document6;
                                if (document3 == null) {
                                }
                            }
                            Document document62 = document2;
                            str7 = str16;
                            document3 = document62;
                            if (document3 == null) {
                            }
                        }
                        document3 = (Document) obj17;
                        i9 = i3;
                        it6 = it2;
                        obj13 = obj6;
                        i5 = i2;
                        obj10 = obj5;
                        mlsbdProvider3 = mlsbdProvider;
                        continuation4 = continuation6;
                        if (document3 == null) {
                        }
                        break;
                    case 2:
                        i2 = mlsbdProvider$loadLinks$12.I$1;
                        i3 = mlsbdProvider$loadLinks$12.I$0;
                        z3 = mlsbdProvider$loadLinks$12.Z$0;
                        exc = (Exception) mlsbdProvider$loadLinks$12.L$11;
                        str11 = (String) mlsbdProvider$loadLinks$12.L$10;
                        str5 = (String) mlsbdProvider$loadLinks$12.L$9;
                        List list5 = (List) mlsbdProvider$loadLinks$12.L$8;
                        String str29 = (String) mlsbdProvider$loadLinks$12.L$7;
                        String str30 = (String) mlsbdProvider$loadLinks$12.L$6;
                        Object obj26 = mlsbdProvider$loadLinks$12.L$5;
                        it4 = (Iterator) mlsbdProvider$loadLinks$12.L$4;
                        obj8 = (Iterable) mlsbdProvider$loadLinks$12.L$3;
                        function18 = (Function1) mlsbdProvider$loadLinks$12.L$2;
                        function17 = (Function1) mlsbdProvider$loadLinks$12.L$1;
                        str9 = (String) mlsbdProvider$loadLinks$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj17);
                            exc2 = exc;
                            obj9 = obj26;
                            z4 = z3;
                            str12 = str30;
                            i4 = i3;
                            str7 = str29;
                            i5 = i2;
                            list = list5;
                            obj10 = obj17;
                            obj3 = coroutine_suspended;
                            mlsbdProvider3 = mlsbdProvider;
                            continuation4 = continuation6;
                            Document document52 = ((NiceResponse) obj17).getDocument();
                            Object obj242 = obj8;
                            str10 = str12;
                            obj13 = obj242;
                            str16 = str7;
                            document2 = document52;
                            i9 = i4;
                            z3 = z4;
                            str3 = str11;
                            it6 = it4;
                            obj7 = obj9;
                        } catch (Exception e5) {
                            obj9 = obj26;
                            str12 = str30;
                            str7 = str29;
                            list = list5;
                            obj3 = coroutine_suspended;
                            obj5 = obj17;
                            mlsbdProvider2 = mlsbdProvider;
                            continuation4 = continuation6;
                            Object obj252 = obj8;
                            str10 = str12;
                            obj13 = obj252;
                            i9 = i3;
                            i5 = i2;
                            obj10 = obj5;
                            mlsbdProvider3 = mlsbdProvider2;
                            str16 = str7;
                            document2 = null;
                            str3 = str11;
                            it6 = it4;
                            obj7 = obj9;
                            Document document622 = document2;
                            str7 = str16;
                            document3 = document622;
                            if (document3 == null) {
                            }
                        }
                        Document document6222 = document2;
                        str7 = str16;
                        document3 = document6222;
                        if (document3 == null) {
                        }
                        break;
                    case 3:
                        int i16 = mlsbdProvider$loadLinks$12.I$3;
                        int i17 = mlsbdProvider$loadLinks$12.I$2;
                        int i18 = mlsbdProvider$loadLinks$12.I$1;
                        int i19 = mlsbdProvider$loadLinks$12.I$0;
                        boolean z10 = mlsbdProvider$loadLinks$12.Z$0;
                        Object obj27 = mlsbdProvider$loadLinks$12.L$15;
                        Document document7 = (Document) mlsbdProvider$loadLinks$12.L$14;
                        Iterator it11 = (Iterator) mlsbdProvider$loadLinks$12.L$13;
                        Iterable iterable4 = (Iterable) mlsbdProvider$loadLinks$12.L$12;
                        Set set3 = (Set) mlsbdProvider$loadLinks$12.L$11;
                        String str31 = (String) mlsbdProvider$loadLinks$12.L$10;
                        str13 = (String) mlsbdProvider$loadLinks$12.L$9;
                        list2 = (List) mlsbdProvider$loadLinks$12.L$8;
                        String str32 = (String) mlsbdProvider$loadLinks$12.L$7;
                        String str33 = (String) mlsbdProvider$loadLinks$12.L$6;
                        obj11 = mlsbdProvider$loadLinks$12.L$5;
                        Iterator it12 = (Iterator) mlsbdProvider$loadLinks$12.L$4;
                        obj12 = (Iterable) mlsbdProvider$loadLinks$12.L$3;
                        Function1<? super ExtractorLink, Unit> function117 = (Function1) mlsbdProvider$loadLinks$12.L$2;
                        Function1<? super SubtitleFile, Unit> function118 = (Function1) mlsbdProvider$loadLinks$12.L$1;
                        String str34 = (String) mlsbdProvider$loadLinks$12.L$0;
                        ResultKt.throwOnFailure(obj17);
                        str9 = str34;
                        obj3 = coroutine_suspended;
                        str14 = str32;
                        it5 = it11;
                        iterable = iterable4;
                        i6 = i17;
                        i7 = i19;
                        it6 = it12;
                        set = set3;
                        function19 = function118;
                        mlsbdProvider$loadLinks$14 = mlsbdProvider$loadLinks$12;
                        mlsbdProvider4 = mlsbdProvider;
                        str15 = str31;
                        str10 = str33;
                        document = document7;
                        z5 = z10;
                        i8 = i18;
                        function110 = function117;
                        int i152 = i6;
                        document4 = document;
                        obj7 = obj11;
                        Object obj212 = obj3;
                        Iterator it102 = it6;
                        String str242 = str15;
                        function111 = function110;
                        it7 = it5;
                        set2 = set;
                        continuation5 = continuation6;
                        mlsbdProvider5 = mlsbdProvider4;
                        obj15 = obj17;
                        str17 = str14;
                        function112 = function19;
                        i10 = i8;
                        str3 = str242;
                        i11 = i7;
                        it6 = it102;
                        z6 = z5;
                        obj3 = obj212;
                        i6 = i152;
                        str5 = str13;
                        list = list2;
                        obj13 = obj12;
                        iterable2 = iterable;
                        while (it7.hasNext()) {
                        }
                        boolean z92 = z6;
                        Function1<? super SubtitleFile, Unit> function1142 = function112;
                        obj2 = obj15;
                        i9 = i11;
                        mlsbdProvider2 = mlsbdProvider5;
                        z2 = z92;
                        it = it6;
                        str2 = str9;
                        continuation2 = continuation5;
                        mlsbdProvider$loadLinks$13 = mlsbdProvider$loadLinks$14;
                        function13 = function111;
                        function14 = function1142;
                        obj = obj13;
                        i = i9;
                        if (it.hasNext()) {
                        }
                        break;
                    case 4:
                        int i20 = mlsbdProvider$loadLinks$12.I$1;
                        i12 = mlsbdProvider$loadLinks$12.I$0;
                        z7 = mlsbdProvider$loadLinks$12.Z$0;
                        Object obj28 = mlsbdProvider$loadLinks$12.L$5;
                        Iterator it13 = (Iterator) mlsbdProvider$loadLinks$12.L$4;
                        Object obj29 = (Iterable) mlsbdProvider$loadLinks$12.L$3;
                        Function1<? super ExtractorLink, Unit> function119 = (Function1) mlsbdProvider$loadLinks$12.L$2;
                        Function1<? super SubtitleFile, Unit> function120 = (Function1) mlsbdProvider$loadLinks$12.L$1;
                        String str35 = (String) mlsbdProvider$loadLinks$12.L$0;
                        ResultKt.throwOnFailure(obj17);
                        obj6 = obj29;
                        function13 = function119;
                        it = it13;
                        function14 = function120;
                        obj3 = coroutine_suspended;
                        obj16 = obj17;
                        mlsbdProvider2 = mlsbdProvider;
                        str18 = str35;
                        mlsbdProvider$loadLinks$13 = mlsbdProvider$loadLinks$12;
                        i9 = i12;
                        z2 = z7;
                        obj13 = obj6;
                        continuation2 = continuation6;
                        str2 = str18;
                        obj2 = obj16;
                        obj = obj13;
                        i = i9;
                        if (it.hasNext()) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        mlsbdProvider = this;
        mlsbdProvider$loadLinks$1 = new MlsbdProvider$loadLinks$1(mlsbdProvider, continuation6);
        mlsbdProvider$loadLinks$12 = mlsbdProvider$loadLinks$1;
        Object obj172 = mlsbdProvider$loadLinks$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (mlsbdProvider$loadLinks$12.label) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0032  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x008d  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x003b  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0028  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object search(@NotNull String str, @NotNull Continuation<? super List<? extends SearchResponse>> continuation) {
        MlsbdProvider$search$1 mlsbdProvider$search$1;
        Object fetchHtml;
        Iterator it;
        if (continuation instanceof MlsbdProvider$search$1) {
            mlsbdProvider$search$1 = (MlsbdProvider$search$1) continuation;
            if ((mlsbdProvider$search$1.label & Integer.MIN_VALUE) != 0) {
                mlsbdProvider$search$1.label -= Integer.MIN_VALUE;
                Object obj = mlsbdProvider$search$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (mlsbdProvider$search$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        String str2 = getMainUrl() + "/?s=" + str;
                        mlsbdProvider$search$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        mlsbdProvider$search$1.label = 1;
                        fetchHtml = fetchHtml(str2, mlsbdProvider$search$1);
                        if (fetchHtml != coroutine_suspended) {
                            break;
                        } else {
                            return coroutine_suspended;
                        }
                    case 1:
                        ResultKt.throwOnFailure(obj);
                        fetchHtml = obj;
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                Iterable select = ((Document) fetchHtml).select("article, .post-item, .item, div.single-post");
                ArrayList arrayList = new ArrayList();
                it = select.iterator();
                while (it.hasNext()) {
                    SearchResponse result = toResult((Element) it.next());
                    if (result != null) {
                        arrayList.add(result);
                    }
                }
                return arrayList;
            }
        }
        mlsbdProvider$search$1 = new MlsbdProvider$search$1(this, continuation);
        Object obj2 = mlsbdProvider$search$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (mlsbdProvider$search$1.label) {
        }
        Iterable select2 = ((Document) fetchHtml).select("article, .post-item, .item, div.single-post");
        ArrayList arrayList2 = new ArrayList();
        it = select2.iterator();
        while (it.hasNext()) {
        }
        return arrayList2;
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
