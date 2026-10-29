package com.cncverse;

import android.content.Context;
import com.cncverse.donation.DonationManager;
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
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.MainPageRequest;
import com.lagradost.cloudstream3.MovieLoadResponse;
import com.lagradost.cloudstream3.MovieSearchResponse;
import com.lagradost.cloudstream3.SearchQuality;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.TvSeriesLoadResponse;
import com.lagradost.cloudstream3.TvSeriesSearchResponse;
import com.lagradost.cloudstream3.TvType;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import com.lagradost.cloudstream3.utils.Qualities;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.Charsets;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/* compiled from: PikashowProvider.kt */
@Metadata(d1 = {"\u0000\u008c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u001d\u0018\u0000 O2\u00020\u0001:\u0011OPQRSTUVWXYZ[\\]^_B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J%\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050 2\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\"H\u0002¢\u0006\u0002\u0010#J\u0014\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050 H\u0002J\u001e\u0010%\u001a\u00020&2\u0006\u0010'\u001a\u00020(2\u0006\u0010)\u001a\u00020*H\u0096@¢\u0006\u0002\u0010+J\u0014\u0010,\u001a\u0004\u0018\u00010-2\b\u0010.\u001a\u0004\u0018\u00010\u0005H\u0002J\u001c\u0010/\u001a\b\u0012\u0004\u0012\u000201002\u0006\u00102\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u00103J\u0018\u00104\u001a\u0004\u0018\u0001052\u0006\u00106\u001a\u00020\u0005H\u0096@¢\u0006\u0002\u00103JF\u00107\u001a\u00020\u000e2\u0006\u00108\u001a\u00020\u00052\u0006\u00109\u001a\u00020\u000e2\u0012\u0010:\u001a\u000e\u0012\u0004\u0012\u00020<\u0012\u0004\u0012\u00020=0;2\u0012\u0010>\u001a\u000e\u0012\u0004\u0012\u00020?\u0012\u0004\u0012\u00020=0;H\u0096@¢\u0006\u0002\u0010@J2\u0010A\u001a\u00020=2\u0006\u0010B\u001a\u00020C2\u0012\u0010>\u001a\u000e\u0012\u0004\u0012\u00020?\u0012\u0004\u0012\u00020=0;2\u0006\u0010D\u001a\u00020\u0005H\u0082@¢\u0006\u0002\u0010EJ\u0016\u0010F\u001a\u00020\u00052\u0006\u0010G\u001a\u00020\u0005H\u0082@¢\u0006\u0002\u00103J\u0010\u0010H\u001a\u00020\u00052\u0006\u00106\u001a\u00020\u0005H\u0002JF\u0010I\u001a\u00020=2\u0006\u0010B\u001a\u00020C2\u0012\u0010>\u001a\u000e\u0012\u0004\u0012\u00020?\u0012\u0004\u0012\u00020=0;2\u0006\u0010D\u001a\u00020\u00052\u0012\u0010J\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050 H\u0082@¢\u0006\u0002\u0010KJ\u0012\u0010L\u001a\u00020(2\b\u0010.\u001a\u0004\u0018\u00010\u0005H\u0002J\u0012\u0010M\u001a\u00020(2\b\u0010N\u001a\u0004\u0018\u00010\u0005H\u0002R\u001a\u0010\u0004\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u0014\u0010\r\u001a\u00020\u000eX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0011\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0007\"\u0004\b\u0013\u0010\tR\u001a\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u000e\u0010\u0019\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u001cX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006`"}, d2 = {"Lcom/cncverse/PikashowProvider;", "Lcom/lagradost/cloudstream3/MainAPI;", "<init>", "()V", "mainUrl", "", "getMainUrl", "()Ljava/lang/String;", "setMainUrl", "(Ljava/lang/String;)V", "name", "getName", "setName", "hasMainPage", "", "getHasMainPage", "()Z", "lang", "getLang", "setLang", "supportedTypes", "", "Lcom/lagradost/cloudstream3/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "apiKey", "hmacSecret", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "deviceUuid", "gaid", "generateSignature", "", "timestampMs", "", "(Ljava/lang/Long;)Ljava/util/Map;", "getPikashowHeaders", "getMainPage", "Lcom/lagradost/cloudstream3/HomePageResponse;", "page", "", "request", "Lcom/lagradost/cloudstream3/MainPageRequest;", "(ILcom/lagradost/cloudstream3/MainPageRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getQualityFromString", "Lcom/lagradost/cloudstream3/SearchQuality;", "qualityString", "search", "", "Lcom/lagradost/cloudstream3/SearchResponse;", "query", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "load", "Lcom/lagradost/cloudstream3/LoadResponse;", "url", "loadLinks", "data", "isCasting", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "addVideoLinksToCallback", "videoData", "Lcom/cncverse/PikashowProvider$VideoData;", "contentName", "(Lcom/cncverse/PikashowProvider$VideoData;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "parseHDBVPlayerUrl", "playerUrl", "extractImdbIdFromUrl", "fallbackToDirectUrls", "finalHeaders", "(Lcom/cncverse/PikashowProvider$VideoData;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getQualityValue", "getQualityValueFromLabel", "label", "Companion", "PikashowSeries", "SeasonDetail", "PikashowSeriesResponse", "PikashowMovie", "ClientUrl", "PikashowMovieResponse", "VideoApiResponse", "VideoData", "VideoSeasonDetail", "VideoEpisode", "Resolution", "Language", "Keys", "Season", "HDBVEpisode", "FileData", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nPikashowProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 PikashowProvider.kt\ncom/cncverse/PikashowProvider\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 Extensions.kt\ncom/fasterxml/jackson/module/kotlin/ExtensionsKt\n+ 4 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,988:1\n1915#2:989\n1642#2,10:1003\n1915#2:1013\n1916#2:1015\n1652#2:1016\n1642#2,10:1030\n1915#2:1040\n1916#2:1042\n1652#2:1043\n1916#2:1044\n1915#2:1045\n1915#2,2:1059\n1915#2,2:1074\n1916#2:1076\n1915#2,2:1091\n1807#2,3:1175\n1807#2,3:1178\n1915#2,2:1181\n1915#2:1183\n1915#2,2:1184\n1916#2:1186\n116#3:990\n54#3:991\n117#3:992\n61#3,8:993\n71#3:1002\n116#3:1017\n54#3:1018\n117#3:1019\n61#3,8:1020\n71#3:1029\n116#3:1046\n54#3:1047\n117#3:1048\n61#3,8:1049\n71#3:1058\n116#3:1061\n54#3:1062\n117#3:1063\n61#3,8:1064\n71#3:1073\n116#3:1077\n54#3:1078\n117#3:1079\n61#3,8:1080\n71#3:1089\n116#3:1093\n54#3:1094\n117#3:1095\n61#3,8:1096\n71#3:1105\n116#3:1107\n54#3:1108\n117#3:1109\n61#3,8:1110\n71#3:1119\n116#3:1120\n54#3:1121\n117#3:1122\n61#3,8:1123\n71#3:1132\n116#3:1134\n54#3:1135\n117#3:1136\n61#3,8:1137\n71#3:1146\n116#3:1148\n54#3:1149\n117#3:1150\n61#3,8:1151\n71#3:1160\n116#3:1162\n54#3:1163\n117#3:1164\n61#3,8:1165\n71#3:1174\n116#3:1187\n54#3:1188\n117#3:1189\n61#3,8:1190\n71#3:1199\n116#3:1200\n54#3:1201\n117#3:1202\n61#3,8:1203\n71#3:1212\n1#4:1001\n1#4:1014\n1#4:1028\n1#4:1041\n1#4:1057\n1#4:1072\n1#4:1088\n1#4:1090\n1#4:1104\n1#4:1106\n1#4:1118\n1#4:1131\n1#4:1133\n1#4:1145\n1#4:1147\n1#4:1159\n1#4:1161\n1#4:1173\n1#4:1198\n1#4:1211\n*S KotlinDebug\n*F\n+ 1 PikashowProvider.kt\ncom/cncverse/PikashowProvider\n*L\n222#1:989\n242#1:1003,10\n242#1:1013\n242#1:1015\n242#1:1016\n263#1:1030,10\n263#1:1040\n263#1:1042\n263#1:1043\n222#1:1044\n341#1:1045\n361#1:1059,2\n389#1:1074,2\n341#1:1076\n476#1:1091,2\n722#1:1175,3\n723#1:1178,3\n727#1:1181,2\n750#1:1183\n751#1:1184,2\n750#1:1186\n241#1:990\n241#1:991\n241#1:992\n241#1:993,8\n241#1:1002\n262#1:1017\n262#1:1018\n262#1:1019\n262#1:1020,8\n262#1:1029\n360#1:1046\n360#1:1047\n360#1:1048\n360#1:1049,8\n360#1:1058\n388#1:1061\n388#1:1062\n388#1:1063\n388#1:1064,8\n388#1:1073\n469#1:1077\n469#1:1078\n469#1:1079\n469#1:1080,8\n469#1:1089\n523#1:1093\n523#1:1094\n523#1:1095\n523#1:1096,8\n523#1:1105\n589#1:1107\n589#1:1108\n589#1:1109\n589#1:1110,8\n589#1:1119\n625#1:1120\n625#1:1121\n625#1:1122\n625#1:1123,8\n625#1:1132\n633#1:1134\n633#1:1135\n633#1:1136\n633#1:1137,8\n633#1:1146\n662#1:1148\n662#1:1149\n662#1:1150\n662#1:1151,8\n662#1:1160\n700#1:1162\n700#1:1163\n700#1:1164\n700#1:1165,8\n700#1:1174\n850#1:1187\n850#1:1188\n850#1:1189\n850#1:1190,8\n850#1:1199\n887#1:1200\n887#1:1201\n887#1:1202\n887#1:1203,8\n887#1:1212\n241#1:1001\n242#1:1014\n262#1:1028\n263#1:1041\n360#1:1057\n388#1:1072\n469#1:1088\n523#1:1104\n589#1:1118\n625#1:1131\n633#1:1145\n662#1:1159\n700#1:1173\n850#1:1198\n887#1:1211\n*E\n"})
/* loaded from: classes.dex */
public final class PikashowProvider extends MainAPI {

    /* renamed from: Companion, reason: from kotlin metadata */
    @NotNull
    public static final Companion INSTANCE = new Companion(null);

    @Nullable
    private static Context context;

    @NotNull
    private String mainUrl = "https://manoda.co";

    @NotNull
    private String name = "Pikashow";
    private final boolean hasMainPage = true;

    @NotNull
    private String lang = "ta";

    @NotNull
    private final Set<TvType> supportedTypes = SetsKt.setOf(new TvType[]{TvType.Movie, TvType.TvSeries});

    @NotNull
    private final String apiKey = "picashow-api-secret-key";

    @NotNull
    private final String hmacSecret = "picashow-api-secret-2025";

    @NotNull
    private final ObjectMapper mapper = ExtensionsKt.jacksonObjectMapper();

    @NotNull
    private final String deviceUuid = UUID.randomUUID().toString();

    @NotNull
    private final String gaid = UUID.randomUUID().toString();

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u000b\u0010\n\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u000b\u001a\u0004\u0018\u00010\u0003HÆ\u0003J!\u0010\f\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003HÆ\u0001J\u0014\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004J\n\u0010\u0012\u001a\u00020\u0003HÖ\u0081\u0004R\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0013"}, d2 = {"Lcom/cncverse/PikashowProvider$ClientUrl;", "", "label", "", "url", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getLabel", "()Ljava/lang/String;", "getUrl", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class ClientUrl {

        @JsonProperty("label")
        @Nullable
        private final String label;

        @JsonProperty("url")
        @Nullable
        private final String url;

        public ClientUrl() {
            this(null, null, 3, null);
        }

        public ClientUrl(@JsonProperty("label") @Nullable String str, @JsonProperty("url") @Nullable String str2) {
            this.label = str;
            this.url = str2;
        }

        public /* synthetic */ ClientUrl(String str, String str2, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : str, (i & 2) != 0 ? null : str2);
        }

        public static /* synthetic */ ClientUrl copy$default(ClientUrl clientUrl, String str, String str2, int i, Object obj) {
            if ((i & 1) != 0) {
                str = clientUrl.label;
            }
            if ((i & 2) != 0) {
                str2 = clientUrl.url;
            }
            return clientUrl.copy(str, str2);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getLabel() {
            return this.label;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getUrl() {
            return this.url;
        }

        @NotNull
        public final ClientUrl copy(@JsonProperty("label") @Nullable String label, @JsonProperty("url") @Nullable String url) {
            return new ClientUrl(label, url);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ClientUrl)) {
                return false;
            }
            ClientUrl clientUrl = (ClientUrl) other;
            return Intrinsics.areEqual(this.label, clientUrl.label) && Intrinsics.areEqual(this.url, clientUrl.url);
        }

        @Nullable
        public final String getLabel() {
            return this.label;
        }

        @Nullable
        public final String getUrl() {
            return this.url;
        }

        public int hashCode() {
            return ((this.label == null ? 0 : this.label.hashCode()) * 31) + (this.url != null ? this.url.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "ClientUrl(label=" + this.label + ", url=" + this.url + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t¨\u0006\n"}, d2 = {"Lcom/cncverse/PikashowProvider$Companion;", "", "<init>", "()V", "context", "Landroid/content/Context;", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @Nullable
        public final Context getContext() {
            return PikashowProvider.context;
        }

        public final void setContext(@Nullable Context context) {
            PikashowProvider.context = context;
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0011\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003HÆ\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\r\u001a\u00020\u000eHÖ\u0081\u0004J\n\u0010\u000f\u001a\u00020\u0003HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007¨\u0006\u0010"}, d2 = {"Lcom/cncverse/PikashowProvider$FileData;", "", "file", "", "<init>", "(Ljava/lang/String;)V", "getFile", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class FileData {

        @JsonProperty("file")
        @NotNull
        private final String file;

        public FileData(@JsonProperty("file") @NotNull String str) {
            this.file = str;
        }

        public static /* synthetic */ FileData copy$default(FileData fileData, String str, int i, Object obj) {
            if ((i & 1) != 0) {
                str = fileData.file;
            }
            return fileData.copy(str);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getFile() {
            return this.file;
        }

        @NotNull
        public final FileData copy(@JsonProperty("file") @NotNull String file) {
            return new FileData(file);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            return (other instanceof FileData) && Intrinsics.areEqual(this.file, ((FileData) other).file);
        }

        @NotNull
        public final String getFile() {
            return this.file;
        }

        public int hashCode() {
            return this.file.hashCode();
        }

        @NotNull
        public String toString() {
            return "FileData(file=" + this.file + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B!\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0001\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005¢\u0006\u0004\b\u0007\u0010\bJ\t\u0010\r\u001a\u00020\u0003HÆ\u0003J\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005HÆ\u0003J#\u0010\u000f\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\u000e\b\u0003\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005HÆ\u0001J\u0014\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0013\u001a\u00020\u0014HÖ\u0081\u0004J\n\u0010\u0015\u001a\u00020\u0003HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001c\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f¨\u0006\u0016"}, d2 = {"Lcom/cncverse/PikashowProvider$HDBVEpisode;", "", "episode", "", "folder", "", "Lcom/cncverse/PikashowProvider$FileData;", "<init>", "(Ljava/lang/String;Ljava/util/List;)V", "getEpisode", "()Ljava/lang/String;", "getFolder", "()Ljava/util/List;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class HDBVEpisode {

        @JsonProperty("episode")
        @NotNull
        private final String episode;

        @JsonProperty("folder")
        @NotNull
        private final List<FileData> folder;

        public HDBVEpisode(@JsonProperty("episode") @NotNull String str, @JsonProperty("folder") @NotNull List<FileData> list) {
            this.episode = str;
            this.folder = list;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ HDBVEpisode copy$default(HDBVEpisode hDBVEpisode, String str, List list, int i, Object obj) {
            if ((i & 1) != 0) {
                str = hDBVEpisode.episode;
            }
            if ((i & 2) != 0) {
                list = hDBVEpisode.folder;
            }
            return hDBVEpisode.copy(str, list);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getEpisode() {
            return this.episode;
        }

        @NotNull
        public final List<FileData> component2() {
            return this.folder;
        }

        @NotNull
        public final HDBVEpisode copy(@JsonProperty("episode") @NotNull String episode, @JsonProperty("folder") @NotNull List<FileData> folder) {
            return new HDBVEpisode(episode, folder);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof HDBVEpisode)) {
                return false;
            }
            HDBVEpisode hDBVEpisode = (HDBVEpisode) other;
            return Intrinsics.areEqual(this.episode, hDBVEpisode.episode) && Intrinsics.areEqual(this.folder, hDBVEpisode.folder);
        }

        @NotNull
        public final String getEpisode() {
            return this.episode;
        }

        @NotNull
        public final List<FileData> getFolder() {
            return this.folder;
        }

        public int hashCode() {
            return (this.episode.hashCode() * 31) + this.folder.hashCode();
        }

        @NotNull
        public String toString() {
            return "HDBVEpisode(episode=" + this.episode + ", folder=" + this.folder + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001b\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\n\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000b\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004J\n\u0010\u0012\u001a\u00020\u0003HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0016\u0010\u0004\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0013"}, d2 = {"Lcom/cncverse/PikashowProvider$Keys;", "", "file", "", "key", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getFile", "()Ljava/lang/String;", "getKey", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class Keys {

        @JsonProperty("file")
        @NotNull
        private final String file;

        @JsonProperty("key")
        @NotNull
        private final String key;

        public Keys(@JsonProperty("file") @NotNull String str, @JsonProperty("key") @NotNull String str2) {
            this.file = str;
            this.key = str2;
        }

        public static /* synthetic */ Keys copy$default(Keys keys, String str, String str2, int i, Object obj) {
            if ((i & 1) != 0) {
                str = keys.file;
            }
            if ((i & 2) != 0) {
                str2 = keys.key;
            }
            return keys.copy(str, str2);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getFile() {
            return this.file;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getKey() {
            return this.key;
        }

        @NotNull
        public final Keys copy(@JsonProperty("file") @NotNull String file, @JsonProperty("key") @NotNull String key) {
            return new Keys(file, key);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Keys)) {
                return false;
            }
            Keys keys = (Keys) other;
            return Intrinsics.areEqual(this.file, keys.file) && Intrinsics.areEqual(this.key, keys.key);
        }

        @NotNull
        public final String getFile() {
            return this.file;
        }

        @NotNull
        public final String getKey() {
            return this.key;
        }

        public int hashCode() {
            return (this.file.hashCode() * 31) + this.key.hashCode();
        }

        @NotNull
        public String toString() {
            return "Keys(file=" + this.file + ", key=" + this.key + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B1\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0003\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006¢\u0006\u0004\b\b\u0010\tJ\u000b\u0010\u000f\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0011\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006HÆ\u0003J3\u0010\u0012\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00032\u0010\b\u0003\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006HÆ\u0001J\u0014\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0016\u001a\u00020\u0017HÖ\u0081\u0004J\n\u0010\u0018\u001a\u00020\u0003HÖ\u0081\u0004R\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u001e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e¨\u0006\u0019"}, d2 = {"Lcom/cncverse/PikashowProvider$Language;", "", "language", "", "playUrl", "resolutions", "", "Lcom/cncverse/PikashowProvider$Resolution;", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V", "getLanguage", "()Ljava/lang/String;", "getPlayUrl", "getResolutions", "()Ljava/util/List;", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class Language {

        @JsonProperty("language")
        @Nullable
        private final String language;

        @JsonProperty("playUrl")
        @Nullable
        private final String playUrl;

        @JsonProperty("resolutions")
        @Nullable
        private final List<Resolution> resolutions;

        public Language() {
            this(null, null, null, 7, null);
        }

        public Language(@JsonProperty("language") @Nullable String str, @JsonProperty("playUrl") @Nullable String str2, @JsonProperty("resolutions") @Nullable List<Resolution> list) {
            this.language = str;
            this.playUrl = str2;
            this.resolutions = list;
        }

        public /* synthetic */ Language(String str, String str2, List list, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : str, (i & 2) != 0 ? null : str2, (i & 4) != 0 ? null : list);
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ Language copy$default(Language language, String str, String str2, List list, int i, Object obj) {
            if ((i & 1) != 0) {
                str = language.language;
            }
            if ((i & 2) != 0) {
                str2 = language.playUrl;
            }
            if ((i & 4) != 0) {
                list = language.resolutions;
            }
            return language.copy(str, str2, list);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getLanguage() {
            return this.language;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getPlayUrl() {
            return this.playUrl;
        }

        @Nullable
        public final List<Resolution> component3() {
            return this.resolutions;
        }

        @NotNull
        public final Language copy(@JsonProperty("language") @Nullable String language, @JsonProperty("playUrl") @Nullable String playUrl, @JsonProperty("resolutions") @Nullable List<Resolution> resolutions) {
            return new Language(language, playUrl, resolutions);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Language)) {
                return false;
            }
            Language language = (Language) other;
            return Intrinsics.areEqual(this.language, language.language) && Intrinsics.areEqual(this.playUrl, language.playUrl) && Intrinsics.areEqual(this.resolutions, language.resolutions);
        }

        @Nullable
        public final String getLanguage() {
            return this.language;
        }

        @Nullable
        public final String getPlayUrl() {
            return this.playUrl;
        }

        @Nullable
        public final List<Resolution> getResolutions() {
            return this.resolutions;
        }

        public int hashCode() {
            return ((((this.language == null ? 0 : this.language.hashCode()) * 31) + (this.playUrl == null ? 0 : this.playUrl.hashCode())) * 31) + (this.resolutions != null ? this.resolutions.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "Language(language=" + this.language + ", playUrl=" + this.playUrl + ", resolutions=" + this.resolutions + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u001b\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001By\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0003\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u000e\u0018\u00010\r¢\u0006\u0004\b\u000f\u0010\u0010J\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u0012J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010!\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u0012J\u000b\u0010\"\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010#\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010%\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u0012J\u0011\u0010&\u001a\n\u0012\u0004\u0012\u00020\u000e\u0018\u00010\rHÆ\u0003J\u0080\u0001\u0010'\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\u00032\u0010\b\u0003\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u000e\u0018\u00010\rHÆ\u0001¢\u0006\u0002\u0010(J\u0014\u0010)\u001a\u00020*2\b\u0010+\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010,\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010-\u001a\u00020\u0005HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0011\u0010\u0012R\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u001a\u0010\u0007\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0017\u0010\u0012R\u0018\u0010\b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0015R\u0018\u0010\t\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0015R\u0018\u0010\n\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0015R\u001a\u0010\u000b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u001b\u0010\u0012R\u001e\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u000e\u0018\u00010\r8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001d¨\u0006."}, d2 = {"Lcom/cncverse/PikashowProvider$PikashowMovie;", "", "sortOrder", "", "title", "", "genre", "year", "quality", "cover", "url", "format", "clientUrls", "", "Lcom/cncverse/PikashowProvider$ClientUrl;", "<init>", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;)V", "getSortOrder", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getTitle", "()Ljava/lang/String;", "getGenre", "getYear", "getQuality", "getCover", "getUrl", "getFormat", "getClientUrls", "()Ljava/util/List;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;)Lcom/cncverse/PikashowProvider$PikashowMovie;", "equals", "", "other", "hashCode", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class PikashowMovie {

        @JsonProperty("clientUrls")
        @Nullable
        private final List<ClientUrl> clientUrls;

        @JsonProperty("c")
        @Nullable
        private final String cover;

        @JsonProperty("f")
        @Nullable
        private final Integer format;

        @JsonProperty("g")
        @Nullable
        private final String genre;

        @JsonProperty("q")
        @Nullable
        private final String quality;

        @JsonProperty("so")
        @Nullable
        private final Integer sortOrder;

        @JsonProperty("t")
        @Nullable
        private final String title;

        @JsonProperty("url")
        @Nullable
        private final String url;

        @JsonProperty("y")
        @Nullable
        private final Integer year;

        public PikashowMovie() {
            this(null, null, null, null, null, null, null, null, null, 511, null);
        }

        public PikashowMovie(@JsonProperty("so") @Nullable Integer num, @JsonProperty("t") @Nullable String str, @JsonProperty("g") @Nullable String str2, @JsonProperty("y") @Nullable Integer num2, @JsonProperty("q") @Nullable String str3, @JsonProperty("c") @Nullable String str4, @JsonProperty("url") @Nullable String str5, @JsonProperty("f") @Nullable Integer num3, @JsonProperty("clientUrls") @Nullable List<ClientUrl> list) {
            this.sortOrder = num;
            this.title = str;
            this.genre = str2;
            this.year = num2;
            this.quality = str3;
            this.cover = str4;
            this.url = str5;
            this.format = num3;
            this.clientUrls = list;
        }

        public /* synthetic */ PikashowMovie(Integer num, String str, String str2, Integer num2, String str3, String str4, String str5, Integer num3, List list, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : num, (i & 2) != 0 ? null : str, (i & 4) != 0 ? null : str2, (i & 8) != 0 ? null : num2, (i & 16) != 0 ? null : str3, (i & 32) != 0 ? null : str4, (i & 64) != 0 ? null : str5, (i & 128) != 0 ? null : num3, (i & 256) != 0 ? null : list);
        }

        public static /* synthetic */ PikashowMovie copy$default(PikashowMovie pikashowMovie, Integer num, String str, String str2, Integer num2, String str3, String str4, String str5, Integer num3, List list, int i, Object obj) {
            if ((i & 1) != 0) {
                num = pikashowMovie.sortOrder;
            }
            if ((i & 2) != 0) {
                str = pikashowMovie.title;
            }
            if ((i & 4) != 0) {
                str2 = pikashowMovie.genre;
            }
            if ((i & 8) != 0) {
                num2 = pikashowMovie.year;
            }
            if ((i & 16) != 0) {
                str3 = pikashowMovie.quality;
            }
            if ((i & 32) != 0) {
                str4 = pikashowMovie.cover;
            }
            if ((i & 64) != 0) {
                str5 = pikashowMovie.url;
            }
            if ((i & 128) != 0) {
                num3 = pikashowMovie.format;
            }
            if ((i & 256) != 0) {
                list = pikashowMovie.clientUrls;
            }
            Integer num4 = num3;
            List list2 = list;
            String str6 = str4;
            String str7 = str5;
            String str8 = str3;
            String str9 = str2;
            return pikashowMovie.copy(num, str, str9, num2, str8, str6, str7, num4, list2);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final Integer getSortOrder() {
            return this.sortOrder;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final String getGenre() {
            return this.genre;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final Integer getYear() {
            return this.year;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getQuality() {
            return this.quality;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final String getCover() {
            return this.cover;
        }

        @Nullable
        /* renamed from: component7, reason: from getter */
        public final String getUrl() {
            return this.url;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final Integer getFormat() {
            return this.format;
        }

        @Nullable
        public final List<ClientUrl> component9() {
            return this.clientUrls;
        }

        @NotNull
        public final PikashowMovie copy(@JsonProperty("so") @Nullable Integer sortOrder, @JsonProperty("t") @Nullable String title, @JsonProperty("g") @Nullable String genre, @JsonProperty("y") @Nullable Integer year, @JsonProperty("q") @Nullable String quality, @JsonProperty("c") @Nullable String cover, @JsonProperty("url") @Nullable String url, @JsonProperty("f") @Nullable Integer format, @JsonProperty("clientUrls") @Nullable List<ClientUrl> clientUrls) {
            return new PikashowMovie(sortOrder, title, genre, year, quality, cover, url, format, clientUrls);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof PikashowMovie)) {
                return false;
            }
            PikashowMovie pikashowMovie = (PikashowMovie) other;
            return Intrinsics.areEqual(this.sortOrder, pikashowMovie.sortOrder) && Intrinsics.areEqual(this.title, pikashowMovie.title) && Intrinsics.areEqual(this.genre, pikashowMovie.genre) && Intrinsics.areEqual(this.year, pikashowMovie.year) && Intrinsics.areEqual(this.quality, pikashowMovie.quality) && Intrinsics.areEqual(this.cover, pikashowMovie.cover) && Intrinsics.areEqual(this.url, pikashowMovie.url) && Intrinsics.areEqual(this.format, pikashowMovie.format) && Intrinsics.areEqual(this.clientUrls, pikashowMovie.clientUrls);
        }

        @Nullable
        public final List<ClientUrl> getClientUrls() {
            return this.clientUrls;
        }

        @Nullable
        public final String getCover() {
            return this.cover;
        }

        @Nullable
        public final Integer getFormat() {
            return this.format;
        }

        @Nullable
        public final String getGenre() {
            return this.genre;
        }

        @Nullable
        public final String getQuality() {
            return this.quality;
        }

        @Nullable
        public final Integer getSortOrder() {
            return this.sortOrder;
        }

        @Nullable
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        public final String getUrl() {
            return this.url;
        }

        @Nullable
        public final Integer getYear() {
            return this.year;
        }

        public int hashCode() {
            return ((((((((((((((((this.sortOrder == null ? 0 : this.sortOrder.hashCode()) * 31) + (this.title == null ? 0 : this.title.hashCode())) * 31) + (this.genre == null ? 0 : this.genre.hashCode())) * 31) + (this.year == null ? 0 : this.year.hashCode())) * 31) + (this.quality == null ? 0 : this.quality.hashCode())) * 31) + (this.cover == null ? 0 : this.cover.hashCode())) * 31) + (this.url == null ? 0 : this.url.hashCode())) * 31) + (this.format == null ? 0 : this.format.hashCode())) * 31) + (this.clientUrls != null ? this.clientUrls.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "PikashowMovie(sortOrder=" + this.sortOrder + ", title=" + this.title + ", genre=" + this.genre + ", year=" + this.year + ", quality=" + this.quality + ", cover=" + this.cover + ", url=" + this.url + ", format=" + this.format + ", clientUrls=" + this.clientUrls + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0010\b\u0003\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u0011\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003HÆ\u0003J\u001b\u0010\n\u001a\u00020\u00002\u0010\b\u0003\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003HÆ\u0001J\u0014\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u000e\u001a\u00020\u000fHÖ\u0081\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004R\u001e\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b¨\u0006\u0012"}, d2 = {"Lcom/cncverse/PikashowProvider$PikashowMovieResponse;", "", "records", "", "Lcom/cncverse/PikashowProvider$PikashowMovie;", "<init>", "(Ljava/util/List;)V", "getRecords", "()Ljava/util/List;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class PikashowMovieResponse {

        @JsonProperty("records")
        @Nullable
        private final List<PikashowMovie> records;

        public PikashowMovieResponse() {
            this(null, 1, null);
        }

        public PikashowMovieResponse(@JsonProperty("records") @Nullable List<PikashowMovie> list) {
            this.records = list;
        }

        public /* synthetic */ PikashowMovieResponse(List list, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : list);
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ PikashowMovieResponse copy$default(PikashowMovieResponse pikashowMovieResponse, List list, int i, Object obj) {
            if ((i & 1) != 0) {
                list = pikashowMovieResponse.records;
            }
            return pikashowMovieResponse.copy(list);
        }

        @Nullable
        public final List<PikashowMovie> component1() {
            return this.records;
        }

        @NotNull
        public final PikashowMovieResponse copy(@JsonProperty("records") @Nullable List<PikashowMovie> records) {
            return new PikashowMovieResponse(records);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            return (other instanceof PikashowMovieResponse) && Intrinsics.areEqual(this.records, ((PikashowMovieResponse) other).records);
        }

        @Nullable
        public final List<PikashowMovie> getRecords() {
            return this.records;
        }

        public int hashCode() {
            if (this.records == null) {
                return 0;
            }
            return this.records.hashCode();
        }

        @NotNull
        public String toString() {
            return "PikashowMovieResponse(records=" + this.records + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0017\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001Ba\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0006\u0012\u0010\b\u0003\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b¢\u0006\u0004\b\r\u0010\u000eJ\u000b\u0010\u001a\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u001b\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\u0013J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0010\u0010\u001f\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\u0013J\u0011\u0010 \u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000bHÆ\u0003Jh\u0010!\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u00062\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00062\u0010\b\u0003\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000bHÆ\u0001¢\u0006\u0002\u0010\"J\u0014\u0010#\u001a\u00020$2\b\u0010%\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010&\u001a\u00020\u0006HÖ\u0081\u0004J\n\u0010'\u001a\u00020\u0003HÖ\u0081\u0004R\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0010R\u001a\u0010\u0005\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0014\u001a\u0004\b\u0012\u0010\u0013R\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0010R\u0018\u0010\b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010R\u001a\u0010\t\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u0014\u001a\u0004\b\u0017\u0010\u0013R\u001e\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019¨\u0006("}, d2 = {"Lcom/cncverse/PikashowProvider$PikashowSeries;", "", "title", "", "genre", "year", "", "cover", "imdbRating", "seasons", "details", "", "Lcom/cncverse/PikashowProvider$SeasonDetail;", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;)V", "getTitle", "()Ljava/lang/String;", "getGenre", "getYear", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getCover", "getImdbRating", "getSeasons", "getDetails", "()Ljava/util/List;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;)Lcom/cncverse/PikashowProvider$PikashowSeries;", "equals", "", "other", "hashCode", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class PikashowSeries {

        @JsonProperty("c")
        @Nullable
        private final String cover;

        @JsonProperty("detail")
        @Nullable
        private final List<SeasonDetail> details;

        @JsonProperty("g")
        @Nullable
        private final String genre;

        @JsonProperty("i")
        @Nullable
        private final String imdbRating;

        @JsonProperty("n")
        @Nullable
        private final Integer seasons;

        @JsonProperty("t")
        @Nullable
        private final String title;

        @JsonProperty("y")
        @Nullable
        private final Integer year;

        public PikashowSeries() {
            this(null, null, null, null, null, null, null, 127, null);
        }

        public PikashowSeries(@JsonProperty("t") @Nullable String str, @JsonProperty("g") @Nullable String str2, @JsonProperty("y") @Nullable Integer num, @JsonProperty("c") @Nullable String str3, @JsonProperty("i") @Nullable String str4, @JsonProperty("n") @Nullable Integer num2, @JsonProperty("detail") @Nullable List<SeasonDetail> list) {
            this.title = str;
            this.genre = str2;
            this.year = num;
            this.cover = str3;
            this.imdbRating = str4;
            this.seasons = num2;
            this.details = list;
        }

        public /* synthetic */ PikashowSeries(String str, String str2, Integer num, String str3, String str4, Integer num2, List list, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : str, (i & 2) != 0 ? null : str2, (i & 4) != 0 ? null : num, (i & 8) != 0 ? null : str3, (i & 16) != 0 ? null : str4, (i & 32) != 0 ? null : num2, (i & 64) != 0 ? null : list);
        }

        public static /* synthetic */ PikashowSeries copy$default(PikashowSeries pikashowSeries, String str, String str2, Integer num, String str3, String str4, Integer num2, List list, int i, Object obj) {
            if ((i & 1) != 0) {
                str = pikashowSeries.title;
            }
            if ((i & 2) != 0) {
                str2 = pikashowSeries.genre;
            }
            if ((i & 4) != 0) {
                num = pikashowSeries.year;
            }
            if ((i & 8) != 0) {
                str3 = pikashowSeries.cover;
            }
            if ((i & 16) != 0) {
                str4 = pikashowSeries.imdbRating;
            }
            if ((i & 32) != 0) {
                num2 = pikashowSeries.seasons;
            }
            if ((i & 64) != 0) {
                list = pikashowSeries.details;
            }
            Integer num3 = num2;
            List list2 = list;
            String str5 = str4;
            Integer num4 = num;
            return pikashowSeries.copy(str, str2, num4, str3, str5, num3, list2);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getGenre() {
            return this.genre;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final Integer getYear() {
            return this.year;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final String getCover() {
            return this.cover;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getImdbRating() {
            return this.imdbRating;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final Integer getSeasons() {
            return this.seasons;
        }

        @Nullable
        public final List<SeasonDetail> component7() {
            return this.details;
        }

        @NotNull
        public final PikashowSeries copy(@JsonProperty("t") @Nullable String title, @JsonProperty("g") @Nullable String genre, @JsonProperty("y") @Nullable Integer year, @JsonProperty("c") @Nullable String cover, @JsonProperty("i") @Nullable String imdbRating, @JsonProperty("n") @Nullable Integer seasons, @JsonProperty("detail") @Nullable List<SeasonDetail> details) {
            return new PikashowSeries(title, genre, year, cover, imdbRating, seasons, details);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof PikashowSeries)) {
                return false;
            }
            PikashowSeries pikashowSeries = (PikashowSeries) other;
            return Intrinsics.areEqual(this.title, pikashowSeries.title) && Intrinsics.areEqual(this.genre, pikashowSeries.genre) && Intrinsics.areEqual(this.year, pikashowSeries.year) && Intrinsics.areEqual(this.cover, pikashowSeries.cover) && Intrinsics.areEqual(this.imdbRating, pikashowSeries.imdbRating) && Intrinsics.areEqual(this.seasons, pikashowSeries.seasons) && Intrinsics.areEqual(this.details, pikashowSeries.details);
        }

        @Nullable
        public final String getCover() {
            return this.cover;
        }

        @Nullable
        public final List<SeasonDetail> getDetails() {
            return this.details;
        }

        @Nullable
        public final String getGenre() {
            return this.genre;
        }

        @Nullable
        public final String getImdbRating() {
            return this.imdbRating;
        }

        @Nullable
        public final Integer getSeasons() {
            return this.seasons;
        }

        @Nullable
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        public final Integer getYear() {
            return this.year;
        }

        public int hashCode() {
            return ((((((((((((this.title == null ? 0 : this.title.hashCode()) * 31) + (this.genre == null ? 0 : this.genre.hashCode())) * 31) + (this.year == null ? 0 : this.year.hashCode())) * 31) + (this.cover == null ? 0 : this.cover.hashCode())) * 31) + (this.imdbRating == null ? 0 : this.imdbRating.hashCode())) * 31) + (this.seasons == null ? 0 : this.seasons.hashCode())) * 31) + (this.details != null ? this.details.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "PikashowSeries(title=" + this.title + ", genre=" + this.genre + ", year=" + this.year + ", cover=" + this.cover + ", imdbRating=" + this.imdbRating + ", seasons=" + this.seasons + ", details=" + this.details + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0010\b\u0003\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u0011\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003HÆ\u0003J\u001b\u0010\n\u001a\u00020\u00002\u0010\b\u0003\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003HÆ\u0001J\u0014\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u000e\u001a\u00020\u000fHÖ\u0081\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004R\u001e\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b¨\u0006\u0012"}, d2 = {"Lcom/cncverse/PikashowProvider$PikashowSeriesResponse;", "", "series", "", "Lcom/cncverse/PikashowProvider$PikashowSeries;", "<init>", "(Ljava/util/List;)V", "getSeries", "()Ljava/util/List;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class PikashowSeriesResponse {

        @JsonProperty("series")
        @Nullable
        private final List<PikashowSeries> series;

        public PikashowSeriesResponse() {
            this(null, 1, null);
        }

        public PikashowSeriesResponse(@JsonProperty("series") @Nullable List<PikashowSeries> list) {
            this.series = list;
        }

        public /* synthetic */ PikashowSeriesResponse(List list, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : list);
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ PikashowSeriesResponse copy$default(PikashowSeriesResponse pikashowSeriesResponse, List list, int i, Object obj) {
            if ((i & 1) != 0) {
                list = pikashowSeriesResponse.series;
            }
            return pikashowSeriesResponse.copy(list);
        }

        @Nullable
        public final List<PikashowSeries> component1() {
            return this.series;
        }

        @NotNull
        public final PikashowSeriesResponse copy(@JsonProperty("series") @Nullable List<PikashowSeries> series) {
            return new PikashowSeriesResponse(series);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            return (other instanceof PikashowSeriesResponse) && Intrinsics.areEqual(this.series, ((PikashowSeriesResponse) other).series);
        }

        @Nullable
        public final List<PikashowSeries> getSeries() {
            return this.series;
        }

        public int hashCode() {
            if (this.series == null) {
                return 0;
            }
            return this.series.hashCode();
        }

        @NotNull
        public String toString() {
            return "PikashowSeriesResponse(series=" + this.series + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B7\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u0006¢\u0006\u0004\b\b\u0010\tJ\u000b\u0010\u0011\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u0012\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\u000eJ\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010\u000eJ>\u0010\u0015\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u00062\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u0006HÆ\u0001¢\u0006\u0002\u0010\u0016J\u0014\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u001a\u001a\u00020\u0006HÖ\u0081\u0004J\n\u0010\u001b\u001a\u00020\u0003HÖ\u0081\u0004R\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u001a\u0010\u0005\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\r\u0010\u000eR\u001a\u0010\u0007\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\u0010\u0010\u000e¨\u0006\u001c"}, d2 = {"Lcom/cncverse/PikashowProvider$Resolution;", "", "label", "", "url", "width", "", "height", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)V", "getLabel", "()Ljava/lang/String;", "getUrl", "getWidth", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getHeight", "component1", "component2", "component3", "component4", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Lcom/cncverse/PikashowProvider$Resolution;", "equals", "", "other", "hashCode", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class Resolution {

        @JsonProperty("height")
        @Nullable
        private final Integer height;

        @JsonProperty("label")
        @Nullable
        private final String label;

        @JsonProperty("url")
        @Nullable
        private final String url;

        @JsonProperty("width")
        @Nullable
        private final Integer width;

        public Resolution() {
            this(null, null, null, null, 15, null);
        }

        public Resolution(@JsonProperty("label") @Nullable String str, @JsonProperty("url") @Nullable String str2, @JsonProperty("width") @Nullable Integer num, @JsonProperty("height") @Nullable Integer num2) {
            this.label = str;
            this.url = str2;
            this.width = num;
            this.height = num2;
        }

        public /* synthetic */ Resolution(String str, String str2, Integer num, Integer num2, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : str, (i & 2) != 0 ? null : str2, (i & 4) != 0 ? null : num, (i & 8) != 0 ? null : num2);
        }

        public static /* synthetic */ Resolution copy$default(Resolution resolution, String str, String str2, Integer num, Integer num2, int i, Object obj) {
            if ((i & 1) != 0) {
                str = resolution.label;
            }
            if ((i & 2) != 0) {
                str2 = resolution.url;
            }
            if ((i & 4) != 0) {
                num = resolution.width;
            }
            if ((i & 8) != 0) {
                num2 = resolution.height;
            }
            return resolution.copy(str, str2, num, num2);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getLabel() {
            return this.label;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getUrl() {
            return this.url;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final Integer getWidth() {
            return this.width;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final Integer getHeight() {
            return this.height;
        }

        @NotNull
        public final Resolution copy(@JsonProperty("label") @Nullable String label, @JsonProperty("url") @Nullable String url, @JsonProperty("width") @Nullable Integer width, @JsonProperty("height") @Nullable Integer height) {
            return new Resolution(label, url, width, height);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Resolution)) {
                return false;
            }
            Resolution resolution = (Resolution) other;
            return Intrinsics.areEqual(this.label, resolution.label) && Intrinsics.areEqual(this.url, resolution.url) && Intrinsics.areEqual(this.width, resolution.width) && Intrinsics.areEqual(this.height, resolution.height);
        }

        @Nullable
        public final Integer getHeight() {
            return this.height;
        }

        @Nullable
        public final String getLabel() {
            return this.label;
        }

        @Nullable
        public final String getUrl() {
            return this.url;
        }

        @Nullable
        public final Integer getWidth() {
            return this.width;
        }

        public int hashCode() {
            return ((((((this.label == null ? 0 : this.label.hashCode()) * 31) + (this.url == null ? 0 : this.url.hashCode())) * 31) + (this.width == null ? 0 : this.width.hashCode())) * 31) + (this.height != null ? this.height.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "Resolution(label=" + this.label + ", url=" + this.url + ", width=" + this.width + ", height=" + this.height + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B!\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0001\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005¢\u0006\u0004\b\u0007\u0010\bJ\t\u0010\r\u001a\u00020\u0003HÆ\u0003J\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005HÆ\u0003J#\u0010\u000f\u001a\u00020\u00002\b\b\u0003\u0010\u0002\u001a\u00020\u00032\u000e\b\u0003\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005HÆ\u0001J\u0014\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0013\u001a\u00020\u0014HÖ\u0081\u0004J\n\u0010\u0015\u001a\u00020\u0003HÖ\u0081\u0004R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001c\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f¨\u0006\u0016"}, d2 = {"Lcom/cncverse/PikashowProvider$Season;", "", "id", "", "folder", "", "Lcom/cncverse/PikashowProvider$HDBVEpisode;", "<init>", "(Ljava/lang/String;Ljava/util/List;)V", "getId", "()Ljava/lang/String;", "getFolder", "()Ljava/util/List;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class Season {

        @JsonProperty("folder")
        @NotNull
        private final List<HDBVEpisode> folder;

        @JsonProperty("id")
        @NotNull
        private final String id;

        public Season(@JsonProperty("id") @NotNull String str, @JsonProperty("folder") @NotNull List<HDBVEpisode> list) {
            this.id = str;
            this.folder = list;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ Season copy$default(Season season, String str, List list, int i, Object obj) {
            if ((i & 1) != 0) {
                str = season.id;
            }
            if ((i & 2) != 0) {
                list = season.folder;
            }
            return season.copy(str, list);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getId() {
            return this.id;
        }

        @NotNull
        public final List<HDBVEpisode> component2() {
            return this.folder;
        }

        @NotNull
        public final Season copy(@JsonProperty("id") @NotNull String id, @JsonProperty("folder") @NotNull List<HDBVEpisode> folder) {
            return new Season(id, folder);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Season)) {
                return false;
            }
            Season season = (Season) other;
            return Intrinsics.areEqual(this.id, season.id) && Intrinsics.areEqual(this.folder, season.folder);
        }

        @NotNull
        public final List<HDBVEpisode> getFolder() {
            return this.folder;
        }

        @NotNull
        public final String getId() {
            return this.id;
        }

        public int hashCode() {
            return (this.id.hashCode() * 31) + this.folder.hashCode();
        }

        @NotNull
        public String toString() {
            return "Season(id=" + this.id + ", folder=" + this.folder + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B+\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0007\u0010\bJ\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\nJ\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\nJ2\u0010\u0012\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u0003HÆ\u0001¢\u0006\u0002\u0010\u0013J\u0014\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0017\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010\u0018\u001a\u00020\u0005HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u000b\u001a\u0004\b\t\u0010\nR\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u001a\u0010\u0006\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u000b\u001a\u0004\b\u000e\u0010\n¨\u0006\u0019"}, d2 = {"Lcom/cncverse/PikashowProvider$SeasonDetail;", "", "year", "", "season", "", "episodesCount", "<init>", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;)V", "getYear", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getSeason", "()Ljava/lang/String;", "getEpisodesCount", "component1", "component2", "component3", "copy", "(Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Integer;)Lcom/cncverse/PikashowProvider$SeasonDetail;", "equals", "", "other", "hashCode", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class SeasonDetail {

        @JsonProperty("episodes_count")
        @Nullable
        private final Integer episodesCount;

        @JsonProperty("season")
        @Nullable
        private final String season;

        @JsonProperty("year")
        @Nullable
        private final Integer year;

        public SeasonDetail() {
            this(null, null, null, 7, null);
        }

        public SeasonDetail(@JsonProperty("year") @Nullable Integer num, @JsonProperty("season") @Nullable String str, @JsonProperty("episodes_count") @Nullable Integer num2) {
            this.year = num;
            this.season = str;
            this.episodesCount = num2;
        }

        public /* synthetic */ SeasonDetail(Integer num, String str, Integer num2, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : num, (i & 2) != 0 ? null : str, (i & 4) != 0 ? null : num2);
        }

        public static /* synthetic */ SeasonDetail copy$default(SeasonDetail seasonDetail, Integer num, String str, Integer num2, int i, Object obj) {
            if ((i & 1) != 0) {
                num = seasonDetail.year;
            }
            if ((i & 2) != 0) {
                str = seasonDetail.season;
            }
            if ((i & 4) != 0) {
                num2 = seasonDetail.episodesCount;
            }
            return seasonDetail.copy(num, str, num2);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final Integer getYear() {
            return this.year;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getSeason() {
            return this.season;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final Integer getEpisodesCount() {
            return this.episodesCount;
        }

        @NotNull
        public final SeasonDetail copy(@JsonProperty("year") @Nullable Integer year, @JsonProperty("season") @Nullable String season, @JsonProperty("episodes_count") @Nullable Integer episodesCount) {
            return new SeasonDetail(year, season, episodesCount);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SeasonDetail)) {
                return false;
            }
            SeasonDetail seasonDetail = (SeasonDetail) other;
            return Intrinsics.areEqual(this.year, seasonDetail.year) && Intrinsics.areEqual(this.season, seasonDetail.season) && Intrinsics.areEqual(this.episodesCount, seasonDetail.episodesCount);
        }

        @Nullable
        public final Integer getEpisodesCount() {
            return this.episodesCount;
        }

        @Nullable
        public final String getSeason() {
            return this.season;
        }

        @Nullable
        public final Integer getYear() {
            return this.year;
        }

        public int hashCode() {
            return ((((this.year == null ? 0 : this.year.hashCode()) * 31) + (this.season == null ? 0 : this.season.hashCode())) * 31) + (this.episodesCount != null ? this.episodesCount.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "SeasonDetail(year=" + this.year + ", season=" + this.season + ", episodesCount=" + this.episodesCount + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B+\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u0007¢\u0006\u0004\b\b\u0010\tJ\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0003HÆ\u0003¢\u0006\u0002\u0010\u000bJ\u000b\u0010\u0012\u001a\u0004\u0018\u00010\u0005HÆ\u0003J\u000b\u0010\u0013\u001a\u0004\u0018\u00010\u0007HÆ\u0003J2\u0010\u0014\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0003\u0010\u0006\u001a\u0004\u0018\u00010\u0007HÆ\u0001¢\u0006\u0002\u0010\u0015J\u0014\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0019\u001a\u00020\u0003HÖ\u0081\u0004J\n\u0010\u001a\u001a\u00020\u0005HÖ\u0081\u0004R\u001a\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\f\u001a\u0004\b\n\u0010\u000bR\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010¨\u0006\u001b"}, d2 = {"Lcom/cncverse/PikashowProvider$VideoApiResponse;", "", "code", "", "message", "", "data", "Lcom/cncverse/PikashowProvider$VideoData;", "<init>", "(Ljava/lang/Integer;Ljava/lang/String;Lcom/cncverse/PikashowProvider$VideoData;)V", "getCode", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getMessage", "()Ljava/lang/String;", "getData", "()Lcom/cncverse/PikashowProvider$VideoData;", "component1", "component2", "component3", "copy", "(Ljava/lang/Integer;Ljava/lang/String;Lcom/cncverse/PikashowProvider$VideoData;)Lcom/cncverse/PikashowProvider$VideoApiResponse;", "equals", "", "other", "hashCode", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class VideoApiResponse {

        @JsonProperty("code")
        @Nullable
        private final Integer code;

        @JsonProperty("data")
        @Nullable
        private final VideoData data;

        @JsonProperty("message")
        @Nullable
        private final String message;

        public VideoApiResponse() {
            this(null, null, null, 7, null);
        }

        public VideoApiResponse(@JsonProperty("code") @Nullable Integer num, @JsonProperty("message") @Nullable String str, @JsonProperty("data") @Nullable VideoData videoData) {
            this.code = num;
            this.message = str;
            this.data = videoData;
        }

        public /* synthetic */ VideoApiResponse(Integer num, String str, VideoData videoData, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : num, (i & 2) != 0 ? null : str, (i & 4) != 0 ? null : videoData);
        }

        public static /* synthetic */ VideoApiResponse copy$default(VideoApiResponse videoApiResponse, Integer num, String str, VideoData videoData, int i, Object obj) {
            if ((i & 1) != 0) {
                num = videoApiResponse.code;
            }
            if ((i & 2) != 0) {
                str = videoApiResponse.message;
            }
            if ((i & 4) != 0) {
                videoData = videoApiResponse.data;
            }
            return videoApiResponse.copy(num, str, videoData);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final Integer getCode() {
            return this.code;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getMessage() {
            return this.message;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final VideoData getData() {
            return this.data;
        }

        @NotNull
        public final VideoApiResponse copy(@JsonProperty("code") @Nullable Integer code, @JsonProperty("message") @Nullable String message, @JsonProperty("data") @Nullable VideoData data) {
            return new VideoApiResponse(code, message, data);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof VideoApiResponse)) {
                return false;
            }
            VideoApiResponse videoApiResponse = (VideoApiResponse) other;
            return Intrinsics.areEqual(this.code, videoApiResponse.code) && Intrinsics.areEqual(this.message, videoApiResponse.message) && Intrinsics.areEqual(this.data, videoApiResponse.data);
        }

        @Nullable
        public final Integer getCode() {
            return this.code;
        }

        @Nullable
        public final VideoData getData() {
            return this.data;
        }

        @Nullable
        public final String getMessage() {
            return this.message;
        }

        public int hashCode() {
            return ((((this.code == null ? 0 : this.code.hashCode()) * 31) + (this.message == null ? 0 : this.message.hashCode())) * 31) + (this.data != null ? this.data.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "VideoApiResponse(code=" + this.code + ", message=" + this.message + ", data=" + this.data + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0002\bP\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B\u0093\u0003\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u0006\u0012\u0010\b\u0003\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b\u0012\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u0006\u0012\u0010\b\u0003\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u000b\u0012\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0003\u0010\u0015\u001a\n\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u000b\u0012\u0016\b\u0003\u0010\u0017\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0018\u0012\u0010\b\u0003\u0010\u0019\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u000b\u0012\u0010\b\u0003\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u000b\u0012\n\b\u0003\u0010\u001c\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u001d\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u001f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010 \u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010!\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\"\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010#\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0003\u0010$\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000b\u0012\n\b\u0003\u0010%\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010&\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b'\u0010(J\u000b\u0010K\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010L\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0010\u0010M\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010-J\u000b\u0010N\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010O\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0010\u0010P\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010-J\u0011\u0010Q\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000bHÆ\u0003J\u0010\u0010R\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010-J\u000b\u0010S\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010T\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0010\u0010U\u001a\u0004\u0018\u00010\u0006HÆ\u0003¢\u0006\u0002\u0010-J\u0011\u0010V\u001a\n\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u000bHÆ\u0003J\u000b\u0010W\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010X\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0011\u0010Y\u001a\n\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u000bHÆ\u0003J\u0017\u0010Z\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0018HÆ\u0003J\u0011\u0010[\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u000bHÆ\u0003J\u0011\u0010\\\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u000bHÆ\u0003J\u000b\u0010]\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010^\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010_\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010`\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010a\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010b\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010c\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010d\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0011\u0010e\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000bHÆ\u0003J\u000b\u0010f\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010g\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u009a\u0003\u0010h\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0005\u001a\u0004\u0018\u00010\u00062\n\b\u0003\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\t\u001a\u0004\u0018\u00010\u00062\u0010\b\u0003\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b2\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\u00062\n\b\u0003\u0010\u000e\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u000f\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\u00062\u0010\b\u0003\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u000b2\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0014\u001a\u0004\u0018\u00010\u00032\u0010\b\u0003\u0010\u0015\u001a\n\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u000b2\u0016\b\u0003\u0010\u0017\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00182\u0010\b\u0003\u0010\u0019\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u000b2\u0010\b\u0003\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u000b2\n\b\u0003\u0010\u001c\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u001d\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u001f\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010 \u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010!\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\"\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010#\u001a\u0004\u0018\u00010\u00032\u0010\b\u0003\u0010$\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000b2\n\b\u0003\u0010%\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010&\u001a\u0004\u0018\u00010\u0003HÆ\u0001¢\u0006\u0002\u0010iJ\u0014\u0010j\u001a\u00020k2\b\u0010l\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010m\u001a\u00020\u0006HÖ\u0081\u0004J\n\u0010n\u001a\u00020\u0003HÖ\u0081\u0004R\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b+\u0010*R\u001a\u0010\u0005\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010.\u001a\u0004\b,\u0010-R\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b/\u0010*R\u0018\u0010\b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b0\u0010*R\u001a\u0010\t\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010.\u001a\u0004\b1\u0010-R\u001e\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u001a\u0010\r\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010.\u001a\u0004\b4\u0010-R\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b5\u0010*R\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b6\u0010*R\u001a\u0010\u0010\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010.\u001a\u0004\b7\u0010-R\u001e\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b8\u00103R\u0018\u0010\u0013\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b9\u0010*R\u0018\u0010\u0014\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b:\u0010*R\u001e\u0010\u0015\u001a\n\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b;\u00103R$\u0010\u0017\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00188\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b<\u0010=R\u001e\u0010\u0019\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b>\u00103R\u001e\u0010\u001b\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b?\u00103R\u0018\u0010\u001c\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b@\u0010*R\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bA\u0010*R\u0018\u0010\u001e\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bB\u0010*R\u0018\u0010\u001f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bC\u0010*R\u0018\u0010 \u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bD\u0010*R\u0018\u0010!\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bE\u0010*R\u0018\u0010\"\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bF\u0010*R\u0018\u0010#\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bG\u0010*R\u001e\u0010$\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u000b8\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bH\u00103R\u0018\u0010%\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bI\u0010*R\u0018\u0010&\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\bJ\u0010*¨\u0006o"}, d2 = {"Lcom/cncverse/PikashowProvider$VideoData;", "", "title", "", "genre", "year", "", "cover", "imdbRating", "seasons", "details", "", "Lcom/cncverse/PikashowProvider$VideoSeasonDetail;", "sortOrder", "quality", "url", "format", "clientUrls", "Lcom/cncverse/PikashowProvider$ClientUrl;", "videoUrl", "playUrl", "resolutions", "Lcom/cncverse/PikashowProvider$Resolution;", "headers", "", "languages", "Lcom/cncverse/PikashowProvider$Language;", "languageOptions", "heastr", "uastr", "uaStr", "headerStr", "sourceType", "host", "file", "key", "supportedLanguages", "season", "episode", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/Map;Ljava/util/List;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;)V", "getTitle", "()Ljava/lang/String;", "getGenre", "getYear", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getCover", "getImdbRating", "getSeasons", "getDetails", "()Ljava/util/List;", "getSortOrder", "getQuality", "getUrl", "getFormat", "getClientUrls", "getVideoUrl", "getPlayUrl", "getResolutions", "getHeaders", "()Ljava/util/Map;", "getLanguages", "getLanguageOptions", "getHeastr", "getUastr", "getUaStr", "getHeaderStr", "getSourceType", "getHost", "getFile", "getKey", "getSupportedLanguages", "getSeason", "getEpisode", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/Map;Ljava/util/List;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;)Lcom/cncverse/PikashowProvider$VideoData;", "equals", "", "other", "hashCode", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class VideoData {

        @JsonProperty("clientUrls")
        @Nullable
        private final List<ClientUrl> clientUrls;

        @JsonProperty("c")
        @Nullable
        private final String cover;

        @JsonProperty("detail")
        @Nullable
        private final List<VideoSeasonDetail> details;

        @JsonProperty("episode")
        @Nullable
        private final String episode;

        @JsonProperty("file")
        @Nullable
        private final String file;

        @JsonProperty("f")
        @Nullable
        private final Integer format;

        @JsonProperty("g")
        @Nullable
        private final String genre;

        @JsonProperty("headerStr")
        @Nullable
        private final String headerStr;

        @JsonProperty("headers")
        @Nullable
        private final Map<String, String> headers;

        @JsonProperty("heastr")
        @Nullable
        private final String heastr;

        @JsonProperty("host")
        @Nullable
        private final String host;

        @JsonProperty("i")
        @Nullable
        private final String imdbRating;

        @JsonProperty("key")
        @Nullable
        private final String key;

        @JsonProperty("languageOptions")
        @Nullable
        private final List<Language> languageOptions;

        @JsonProperty("languages")
        @Nullable
        private final List<Language> languages;

        @JsonProperty("playUrl")
        @Nullable
        private final String playUrl;

        @JsonProperty("q")
        @Nullable
        private final String quality;

        @JsonProperty("resolutions")
        @Nullable
        private final List<Resolution> resolutions;

        @JsonProperty("season")
        @Nullable
        private final String season;

        @JsonProperty("n")
        @Nullable
        private final Integer seasons;

        @JsonProperty("so")
        @Nullable
        private final Integer sortOrder;

        @JsonProperty("sourceType")
        @Nullable
        private final String sourceType;

        @JsonProperty("supportedLanguages")
        @Nullable
        private final List<String> supportedLanguages;

        @JsonProperty("t")
        @Nullable
        private final String title;

        @JsonProperty("uaStr")
        @Nullable
        private final String uaStr;

        @JsonProperty("uastr")
        @Nullable
        private final String uastr;

        @JsonProperty("url")
        @Nullable
        private final String url;

        @JsonProperty("videoUrl")
        @Nullable
        private final String videoUrl;

        @JsonProperty("y")
        @Nullable
        private final Integer year;

        public VideoData() {
            this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 536870911, null);
        }

        public VideoData(@JsonProperty("t") @Nullable String str, @JsonProperty("g") @Nullable String str2, @JsonProperty("y") @Nullable Integer num, @JsonProperty("c") @Nullable String str3, @JsonProperty("i") @Nullable String str4, @JsonProperty("n") @Nullable Integer num2, @JsonProperty("detail") @Nullable List<VideoSeasonDetail> list, @JsonProperty("so") @Nullable Integer num3, @JsonProperty("q") @Nullable String str5, @JsonProperty("url") @Nullable String str6, @JsonProperty("f") @Nullable Integer num4, @JsonProperty("clientUrls") @Nullable List<ClientUrl> list2, @JsonProperty("videoUrl") @Nullable String str7, @JsonProperty("playUrl") @Nullable String str8, @JsonProperty("resolutions") @Nullable List<Resolution> list3, @JsonProperty("headers") @Nullable Map<String, String> map, @JsonProperty("languages") @Nullable List<Language> list4, @JsonProperty("languageOptions") @Nullable List<Language> list5, @JsonProperty("heastr") @Nullable String str9, @JsonProperty("uastr") @Nullable String str10, @JsonProperty("uaStr") @Nullable String str11, @JsonProperty("headerStr") @Nullable String str12, @JsonProperty("sourceType") @Nullable String str13, @JsonProperty("host") @Nullable String str14, @JsonProperty("file") @Nullable String str15, @JsonProperty("key") @Nullable String str16, @JsonProperty("supportedLanguages") @Nullable List<String> list6, @JsonProperty("season") @Nullable String str17, @JsonProperty("episode") @Nullable String str18) {
            this.title = str;
            this.genre = str2;
            this.year = num;
            this.cover = str3;
            this.imdbRating = str4;
            this.seasons = num2;
            this.details = list;
            this.sortOrder = num3;
            this.quality = str5;
            this.url = str6;
            this.format = num4;
            this.clientUrls = list2;
            this.videoUrl = str7;
            this.playUrl = str8;
            this.resolutions = list3;
            this.headers = map;
            this.languages = list4;
            this.languageOptions = list5;
            this.heastr = str9;
            this.uastr = str10;
            this.uaStr = str11;
            this.headerStr = str12;
            this.sourceType = str13;
            this.host = str14;
            this.file = str15;
            this.key = str16;
            this.supportedLanguages = list6;
            this.season = str17;
            this.episode = str18;
        }

        /* JADX WARN: Illegal instructions before constructor call */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public /* synthetic */ VideoData(String str, String str2, Integer num, String str3, String str4, Integer num2, List list, Integer num3, String str5, String str6, Integer num4, List list2, String str7, String str8, List list3, Map map, List list4, List list5, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, List list6, String str17, String str18, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(r1, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r2, (i & 32768) != 0 ? null : map, (i & 65536) != 0 ? null : list4, (i & 131072) != 0 ? null : list5, (i & 262144) != 0 ? null : str9, (i & 524288) != 0 ? null : str10, (i & 1048576) != 0 ? null : str11, (i & 2097152) != 0 ? null : str12, (i & 4194304) != 0 ? null : str13, (i & 8388608) != 0 ? null : str14, (i & 16777216) != 0 ? null : str15, (i & 33554432) != 0 ? null : str16, (i & 67108864) != 0 ? null : list6, (i & 134217728) != 0 ? null : str17, (i & 268435456) != 0 ? null : str18);
            String str19 = (i & 1) != 0 ? null : str;
            String str20 = (i & 2) != 0 ? null : str2;
            Integer num5 = (i & 4) != 0 ? null : num;
            String str21 = (i & 8) != 0 ? null : str3;
            String str22 = (i & 16) != 0 ? null : str4;
            Integer num6 = (i & 32) != 0 ? null : num2;
            List list7 = (i & 64) != 0 ? null : list;
            Integer num7 = (i & 128) != 0 ? null : num3;
            String str23 = (i & 256) != 0 ? null : str5;
            String str24 = (i & 512) != 0 ? null : str6;
            Integer num8 = (i & 1024) != 0 ? null : num4;
            List list8 = (i & 2048) != 0 ? null : list2;
            String str25 = (i & 4096) != 0 ? null : str7;
            String str26 = (i & 8192) != 0 ? null : str8;
            List list9 = (i & 16384) != 0 ? null : list3;
        }

        public static /* synthetic */ VideoData copy$default(VideoData videoData, String str, String str2, Integer num, String str3, String str4, Integer num2, List list, Integer num3, String str5, String str6, Integer num4, List list2, String str7, String str8, List list3, Map map, List list4, List list5, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, List list6, String str17, String str18, int i, Object obj) {
            String str19;
            String str20;
            String str21 = (i & 1) != 0 ? videoData.title : str;
            String str22 = (i & 2) != 0 ? videoData.genre : str2;
            Integer num5 = (i & 4) != 0 ? videoData.year : num;
            String str23 = (i & 8) != 0 ? videoData.cover : str3;
            String str24 = (i & 16) != 0 ? videoData.imdbRating : str4;
            Integer num6 = (i & 32) != 0 ? videoData.seasons : num2;
            List list7 = (i & 64) != 0 ? videoData.details : list;
            Integer num7 = (i & 128) != 0 ? videoData.sortOrder : num3;
            String str25 = (i & 256) != 0 ? videoData.quality : str5;
            String str26 = (i & 512) != 0 ? videoData.url : str6;
            Integer num8 = (i & 1024) != 0 ? videoData.format : num4;
            List list8 = (i & 2048) != 0 ? videoData.clientUrls : list2;
            String str27 = (i & 4096) != 0 ? videoData.videoUrl : str7;
            String str28 = (i & 8192) != 0 ? videoData.playUrl : str8;
            String str29 = str21;
            List list9 = (i & 16384) != 0 ? videoData.resolutions : list3;
            Map map2 = (i & 32768) != 0 ? videoData.headers : map;
            List list10 = (i & 65536) != 0 ? videoData.languages : list4;
            List list11 = (i & 131072) != 0 ? videoData.languageOptions : list5;
            String str30 = (i & 262144) != 0 ? videoData.heastr : str9;
            String str31 = (i & 524288) != 0 ? videoData.uastr : str10;
            String str32 = (i & 1048576) != 0 ? videoData.uaStr : str11;
            String str33 = (i & 2097152) != 0 ? videoData.headerStr : str12;
            String str34 = (i & 4194304) != 0 ? videoData.sourceType : str13;
            String str35 = (i & 8388608) != 0 ? videoData.host : str14;
            String str36 = (i & 16777216) != 0 ? videoData.file : str15;
            String str37 = (i & 33554432) != 0 ? videoData.key : str16;
            List list12 = (i & 67108864) != 0 ? videoData.supportedLanguages : list6;
            String str38 = (i & 134217728) != 0 ? videoData.season : str17;
            if ((i & 268435456) != 0) {
                str20 = str38;
                str19 = videoData.episode;
            } else {
                str19 = str18;
                str20 = str38;
            }
            return videoData.copy(str29, str22, num5, str23, str24, num6, list7, num7, str25, str26, num8, list8, str27, str28, list9, map2, list10, list11, str30, str31, str32, str33, str34, str35, str36, str37, list12, str20, str19);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        /* renamed from: component10, reason: from getter */
        public final String getUrl() {
            return this.url;
        }

        @Nullable
        /* renamed from: component11, reason: from getter */
        public final Integer getFormat() {
            return this.format;
        }

        @Nullable
        public final List<ClientUrl> component12() {
            return this.clientUrls;
        }

        @Nullable
        /* renamed from: component13, reason: from getter */
        public final String getVideoUrl() {
            return this.videoUrl;
        }

        @Nullable
        /* renamed from: component14, reason: from getter */
        public final String getPlayUrl() {
            return this.playUrl;
        }

        @Nullable
        public final List<Resolution> component15() {
            return this.resolutions;
        }

        @Nullable
        public final Map<String, String> component16() {
            return this.headers;
        }

        @Nullable
        public final List<Language> component17() {
            return this.languages;
        }

        @Nullable
        public final List<Language> component18() {
            return this.languageOptions;
        }

        @Nullable
        /* renamed from: component19, reason: from getter */
        public final String getHeastr() {
            return this.heastr;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getGenre() {
            return this.genre;
        }

        @Nullable
        /* renamed from: component20, reason: from getter */
        public final String getUastr() {
            return this.uastr;
        }

        @Nullable
        /* renamed from: component21, reason: from getter */
        public final String getUaStr() {
            return this.uaStr;
        }

        @Nullable
        /* renamed from: component22, reason: from getter */
        public final String getHeaderStr() {
            return this.headerStr;
        }

        @Nullable
        /* renamed from: component23, reason: from getter */
        public final String getSourceType() {
            return this.sourceType;
        }

        @Nullable
        /* renamed from: component24, reason: from getter */
        public final String getHost() {
            return this.host;
        }

        @Nullable
        /* renamed from: component25, reason: from getter */
        public final String getFile() {
            return this.file;
        }

        @Nullable
        /* renamed from: component26, reason: from getter */
        public final String getKey() {
            return this.key;
        }

        @Nullable
        public final List<String> component27() {
            return this.supportedLanguages;
        }

        @Nullable
        /* renamed from: component28, reason: from getter */
        public final String getSeason() {
            return this.season;
        }

        @Nullable
        /* renamed from: component29, reason: from getter */
        public final String getEpisode() {
            return this.episode;
        }

        @Nullable
        /* renamed from: component3, reason: from getter */
        public final Integer getYear() {
            return this.year;
        }

        @Nullable
        /* renamed from: component4, reason: from getter */
        public final String getCover() {
            return this.cover;
        }

        @Nullable
        /* renamed from: component5, reason: from getter */
        public final String getImdbRating() {
            return this.imdbRating;
        }

        @Nullable
        /* renamed from: component6, reason: from getter */
        public final Integer getSeasons() {
            return this.seasons;
        }

        @Nullable
        public final List<VideoSeasonDetail> component7() {
            return this.details;
        }

        @Nullable
        /* renamed from: component8, reason: from getter */
        public final Integer getSortOrder() {
            return this.sortOrder;
        }

        @Nullable
        /* renamed from: component9, reason: from getter */
        public final String getQuality() {
            return this.quality;
        }

        @NotNull
        public final VideoData copy(@JsonProperty("t") @Nullable String title, @JsonProperty("g") @Nullable String genre, @JsonProperty("y") @Nullable Integer year, @JsonProperty("c") @Nullable String cover, @JsonProperty("i") @Nullable String imdbRating, @JsonProperty("n") @Nullable Integer seasons, @JsonProperty("detail") @Nullable List<VideoSeasonDetail> details, @JsonProperty("so") @Nullable Integer sortOrder, @JsonProperty("q") @Nullable String quality, @JsonProperty("url") @Nullable String url, @JsonProperty("f") @Nullable Integer format, @JsonProperty("clientUrls") @Nullable List<ClientUrl> clientUrls, @JsonProperty("videoUrl") @Nullable String videoUrl, @JsonProperty("playUrl") @Nullable String playUrl, @JsonProperty("resolutions") @Nullable List<Resolution> resolutions, @JsonProperty("headers") @Nullable Map<String, String> headers, @JsonProperty("languages") @Nullable List<Language> languages, @JsonProperty("languageOptions") @Nullable List<Language> languageOptions, @JsonProperty("heastr") @Nullable String heastr, @JsonProperty("uastr") @Nullable String uastr, @JsonProperty("uaStr") @Nullable String uaStr, @JsonProperty("headerStr") @Nullable String headerStr, @JsonProperty("sourceType") @Nullable String sourceType, @JsonProperty("host") @Nullable String host, @JsonProperty("file") @Nullable String file, @JsonProperty("key") @Nullable String key, @JsonProperty("supportedLanguages") @Nullable List<String> supportedLanguages, @JsonProperty("season") @Nullable String season, @JsonProperty("episode") @Nullable String episode) {
            return new VideoData(title, genre, year, cover, imdbRating, seasons, details, sortOrder, quality, url, format, clientUrls, videoUrl, playUrl, resolutions, headers, languages, languageOptions, heastr, uastr, uaStr, headerStr, sourceType, host, file, key, supportedLanguages, season, episode);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof VideoData)) {
                return false;
            }
            VideoData videoData = (VideoData) other;
            return Intrinsics.areEqual(this.title, videoData.title) && Intrinsics.areEqual(this.genre, videoData.genre) && Intrinsics.areEqual(this.year, videoData.year) && Intrinsics.areEqual(this.cover, videoData.cover) && Intrinsics.areEqual(this.imdbRating, videoData.imdbRating) && Intrinsics.areEqual(this.seasons, videoData.seasons) && Intrinsics.areEqual(this.details, videoData.details) && Intrinsics.areEqual(this.sortOrder, videoData.sortOrder) && Intrinsics.areEqual(this.quality, videoData.quality) && Intrinsics.areEqual(this.url, videoData.url) && Intrinsics.areEqual(this.format, videoData.format) && Intrinsics.areEqual(this.clientUrls, videoData.clientUrls) && Intrinsics.areEqual(this.videoUrl, videoData.videoUrl) && Intrinsics.areEqual(this.playUrl, videoData.playUrl) && Intrinsics.areEqual(this.resolutions, videoData.resolutions) && Intrinsics.areEqual(this.headers, videoData.headers) && Intrinsics.areEqual(this.languages, videoData.languages) && Intrinsics.areEqual(this.languageOptions, videoData.languageOptions) && Intrinsics.areEqual(this.heastr, videoData.heastr) && Intrinsics.areEqual(this.uastr, videoData.uastr) && Intrinsics.areEqual(this.uaStr, videoData.uaStr) && Intrinsics.areEqual(this.headerStr, videoData.headerStr) && Intrinsics.areEqual(this.sourceType, videoData.sourceType) && Intrinsics.areEqual(this.host, videoData.host) && Intrinsics.areEqual(this.file, videoData.file) && Intrinsics.areEqual(this.key, videoData.key) && Intrinsics.areEqual(this.supportedLanguages, videoData.supportedLanguages) && Intrinsics.areEqual(this.season, videoData.season) && Intrinsics.areEqual(this.episode, videoData.episode);
        }

        @Nullable
        public final List<ClientUrl> getClientUrls() {
            return this.clientUrls;
        }

        @Nullable
        public final String getCover() {
            return this.cover;
        }

        @Nullable
        public final List<VideoSeasonDetail> getDetails() {
            return this.details;
        }

        @Nullable
        public final String getEpisode() {
            return this.episode;
        }

        @Nullable
        public final String getFile() {
            return this.file;
        }

        @Nullable
        public final Integer getFormat() {
            return this.format;
        }

        @Nullable
        public final String getGenre() {
            return this.genre;
        }

        @Nullable
        public final String getHeaderStr() {
            return this.headerStr;
        }

        @Nullable
        public final Map<String, String> getHeaders() {
            return this.headers;
        }

        @Nullable
        public final String getHeastr() {
            return this.heastr;
        }

        @Nullable
        public final String getHost() {
            return this.host;
        }

        @Nullable
        public final String getImdbRating() {
            return this.imdbRating;
        }

        @Nullable
        public final String getKey() {
            return this.key;
        }

        @Nullable
        public final List<Language> getLanguageOptions() {
            return this.languageOptions;
        }

        @Nullable
        public final List<Language> getLanguages() {
            return this.languages;
        }

        @Nullable
        public final String getPlayUrl() {
            return this.playUrl;
        }

        @Nullable
        public final String getQuality() {
            return this.quality;
        }

        @Nullable
        public final List<Resolution> getResolutions() {
            return this.resolutions;
        }

        @Nullable
        public final String getSeason() {
            return this.season;
        }

        @Nullable
        public final Integer getSeasons() {
            return this.seasons;
        }

        @Nullable
        public final Integer getSortOrder() {
            return this.sortOrder;
        }

        @Nullable
        public final String getSourceType() {
            return this.sourceType;
        }

        @Nullable
        public final List<String> getSupportedLanguages() {
            return this.supportedLanguages;
        }

        @Nullable
        public final String getTitle() {
            return this.title;
        }

        @Nullable
        public final String getUaStr() {
            return this.uaStr;
        }

        @Nullable
        public final String getUastr() {
            return this.uastr;
        }

        @Nullable
        public final String getUrl() {
            return this.url;
        }

        @Nullable
        public final String getVideoUrl() {
            return this.videoUrl;
        }

        @Nullable
        public final Integer getYear() {
            return this.year;
        }

        public int hashCode() {
            return ((((((((((((((((((((((((((((((((((((((((((((((((((((((((this.title == null ? 0 : this.title.hashCode()) * 31) + (this.genre == null ? 0 : this.genre.hashCode())) * 31) + (this.year == null ? 0 : this.year.hashCode())) * 31) + (this.cover == null ? 0 : this.cover.hashCode())) * 31) + (this.imdbRating == null ? 0 : this.imdbRating.hashCode())) * 31) + (this.seasons == null ? 0 : this.seasons.hashCode())) * 31) + (this.details == null ? 0 : this.details.hashCode())) * 31) + (this.sortOrder == null ? 0 : this.sortOrder.hashCode())) * 31) + (this.quality == null ? 0 : this.quality.hashCode())) * 31) + (this.url == null ? 0 : this.url.hashCode())) * 31) + (this.format == null ? 0 : this.format.hashCode())) * 31) + (this.clientUrls == null ? 0 : this.clientUrls.hashCode())) * 31) + (this.videoUrl == null ? 0 : this.videoUrl.hashCode())) * 31) + (this.playUrl == null ? 0 : this.playUrl.hashCode())) * 31) + (this.resolutions == null ? 0 : this.resolutions.hashCode())) * 31) + (this.headers == null ? 0 : this.headers.hashCode())) * 31) + (this.languages == null ? 0 : this.languages.hashCode())) * 31) + (this.languageOptions == null ? 0 : this.languageOptions.hashCode())) * 31) + (this.heastr == null ? 0 : this.heastr.hashCode())) * 31) + (this.uastr == null ? 0 : this.uastr.hashCode())) * 31) + (this.uaStr == null ? 0 : this.uaStr.hashCode())) * 31) + (this.headerStr == null ? 0 : this.headerStr.hashCode())) * 31) + (this.sourceType == null ? 0 : this.sourceType.hashCode())) * 31) + (this.host == null ? 0 : this.host.hashCode())) * 31) + (this.file == null ? 0 : this.file.hashCode())) * 31) + (this.key == null ? 0 : this.key.hashCode())) * 31) + (this.supportedLanguages == null ? 0 : this.supportedLanguages.hashCode())) * 31) + (this.season == null ? 0 : this.season.hashCode())) * 31) + (this.episode != null ? this.episode.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "VideoData(title=" + this.title + ", genre=" + this.genre + ", year=" + this.year + ", cover=" + this.cover + ", imdbRating=" + this.imdbRating + ", seasons=" + this.seasons + ", details=" + this.details + ", sortOrder=" + this.sortOrder + ", quality=" + this.quality + ", url=" + this.url + ", format=" + this.format + ", clientUrls=" + this.clientUrls + ", videoUrl=" + this.videoUrl + ", playUrl=" + this.playUrl + ", resolutions=" + this.resolutions + ", headers=" + this.headers + ", languages=" + this.languages + ", languageOptions=" + this.languageOptions + ", heastr=" + this.heastr + ", uastr=" + this.uastr + ", uaStr=" + this.uaStr + ", headerStr=" + this.headerStr + ", sourceType=" + this.sourceType + ", host=" + this.host + ", file=" + this.file + ", key=" + this.key + ", supportedLanguages=" + this.supportedLanguages + ", season=" + this.season + ", episode=" + this.episode + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u000b\u0010\n\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u000b\u001a\u0004\u0018\u00010\u0003HÆ\u0003J!\u0010\f\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0003HÆ\u0001J\u0014\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004J\n\u0010\u0012\u001a\u00020\u0003HÖ\u0081\u0004R\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0018\u0010\u0004\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0013"}, d2 = {"Lcom/cncverse/PikashowProvider$VideoEpisode;", "", "episode", "", "url", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getEpisode", "()Ljava/lang/String;", "getUrl", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class VideoEpisode {

        @JsonProperty("e")
        @Nullable
        private final String episode;

        @JsonProperty("url")
        @Nullable
        private final String url;

        public VideoEpisode() {
            this(null, null, 3, null);
        }

        public VideoEpisode(@JsonProperty("e") @Nullable String str, @JsonProperty("url") @Nullable String str2) {
            this.episode = str;
            this.url = str2;
        }

        public /* synthetic */ VideoEpisode(String str, String str2, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : str, (i & 2) != 0 ? null : str2);
        }

        public static /* synthetic */ VideoEpisode copy$default(VideoEpisode videoEpisode, String str, String str2, int i, Object obj) {
            if ((i & 1) != 0) {
                str = videoEpisode.episode;
            }
            if ((i & 2) != 0) {
                str2 = videoEpisode.url;
            }
            return videoEpisode.copy(str, str2);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getEpisode() {
            return this.episode;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final String getUrl() {
            return this.url;
        }

        @NotNull
        public final VideoEpisode copy(@JsonProperty("e") @Nullable String episode, @JsonProperty("url") @Nullable String url) {
            return new VideoEpisode(episode, url);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof VideoEpisode)) {
                return false;
            }
            VideoEpisode videoEpisode = (VideoEpisode) other;
            return Intrinsics.areEqual(this.episode, videoEpisode.episode) && Intrinsics.areEqual(this.url, videoEpisode.url);
        }

        @Nullable
        public final String getEpisode() {
            return this.episode;
        }

        @Nullable
        public final String getUrl() {
            return this.url;
        }

        public int hashCode() {
            return ((this.episode == null ? 0 : this.episode.hashCode()) * 31) + (this.url != null ? this.url.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "VideoEpisode(episode=" + this.episode + ", url=" + this.url + ")";
        }
    }

    /* compiled from: PikashowProvider.kt */
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B1\u0012\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\u0010\b\u0003\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007¢\u0006\u0004\b\t\u0010\nJ\u000b\u0010\u0012\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0005HÆ\u0003¢\u0006\u0002\u0010\u000eJ\u0011\u0010\u0014\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007HÆ\u0003J8\u0010\u0015\u001a\u00020\u00002\n\b\u0003\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0003\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0010\b\u0003\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007HÆ\u0001¢\u0006\u0002\u0010\u0016J\u0014\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u001a\u001a\u00020\u0005HÖ\u0081\u0004J\n\u0010\u001b\u001a\u00020\u0003HÖ\u0081\u0004R\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001a\u0010\u0004\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004¢\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\r\u0010\u000eR\u001e\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u00078\u0006X\u0087\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011¨\u0006\u001c"}, d2 = {"Lcom/cncverse/PikashowProvider$VideoSeasonDetail;", "", "season", "", "year", "", "episodes", "", "Lcom/cncverse/PikashowProvider$VideoEpisode;", "<init>", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;)V", "getSeason", "()Ljava/lang/String;", "getYear", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getEpisodes", "()Ljava/util/List;", "component1", "component2", "component3", "copy", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;)Lcom/cncverse/PikashowProvider$VideoSeasonDetail;", "equals", "", "other", "hashCode", "toString", "PikashowProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final /* data */ class VideoSeasonDetail {

        @JsonProperty("episodes")
        @Nullable
        private final List<VideoEpisode> episodes;

        @JsonProperty("season")
        @Nullable
        private final String season;

        @JsonProperty("year")
        @Nullable
        private final Integer year;

        public VideoSeasonDetail() {
            this(null, null, null, 7, null);
        }

        public VideoSeasonDetail(@JsonProperty("season") @Nullable String str, @JsonProperty("year") @Nullable Integer num, @JsonProperty("episodes") @Nullable List<VideoEpisode> list) {
            this.season = str;
            this.year = num;
            this.episodes = list;
        }

        public /* synthetic */ VideoSeasonDetail(String str, Integer num, List list, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : str, (i & 2) != 0 ? null : num, (i & 4) != 0 ? null : list);
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ VideoSeasonDetail copy$default(VideoSeasonDetail videoSeasonDetail, String str, Integer num, List list, int i, Object obj) {
            if ((i & 1) != 0) {
                str = videoSeasonDetail.season;
            }
            if ((i & 2) != 0) {
                num = videoSeasonDetail.year;
            }
            if ((i & 4) != 0) {
                list = videoSeasonDetail.episodes;
            }
            return videoSeasonDetail.copy(str, num, list);
        }

        @Nullable
        /* renamed from: component1, reason: from getter */
        public final String getSeason() {
            return this.season;
        }

        @Nullable
        /* renamed from: component2, reason: from getter */
        public final Integer getYear() {
            return this.year;
        }

        @Nullable
        public final List<VideoEpisode> component3() {
            return this.episodes;
        }

        @NotNull
        public final VideoSeasonDetail copy(@JsonProperty("season") @Nullable String season, @JsonProperty("year") @Nullable Integer year, @JsonProperty("episodes") @Nullable List<VideoEpisode> episodes) {
            return new VideoSeasonDetail(season, year, episodes);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof VideoSeasonDetail)) {
                return false;
            }
            VideoSeasonDetail videoSeasonDetail = (VideoSeasonDetail) other;
            return Intrinsics.areEqual(this.season, videoSeasonDetail.season) && Intrinsics.areEqual(this.year, videoSeasonDetail.year) && Intrinsics.areEqual(this.episodes, videoSeasonDetail.episodes);
        }

        @Nullable
        public final List<VideoEpisode> getEpisodes() {
            return this.episodes;
        }

        @Nullable
        public final String getSeason() {
            return this.season;
        }

        @Nullable
        public final Integer getYear() {
            return this.year;
        }

        public int hashCode() {
            return ((((this.season == null ? 0 : this.season.hashCode()) * 31) + (this.year == null ? 0 : this.year.hashCode())) * 31) + (this.episodes != null ? this.episodes.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "VideoSeasonDetail(season=" + this.season + ", year=" + this.year + ", episodes=" + this.episodes + ")";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0032. Please report as an issue. */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:105:0x0133  */
    /* JADX WARN: Removed duplicated region for block: B:110:0x0874  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x003d  */
    /* JADX WARN: Removed duplicated region for block: B:138:0x0a01  */
    /* JADX WARN: Removed duplicated region for block: B:142:0x083c  */
    /* JADX WARN: Removed duplicated region for block: B:149:0x01d9  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x005c  */
    /* JADX WARN: Removed duplicated region for block: B:154:0x06ca  */
    /* JADX WARN: Removed duplicated region for block: B:173:0x07eb  */
    /* JADX WARN: Removed duplicated region for block: B:176:0x081c  */
    /* JADX WARN: Removed duplicated region for block: B:178:0x0822  */
    /* JADX WARN: Removed duplicated region for block: B:179:0x0a44  */
    /* JADX WARN: Removed duplicated region for block: B:180:0x0252  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x007f  */
    /* JADX WARN: Removed duplicated region for block: B:208:0x0354  */
    /* JADX WARN: Removed duplicated region for block: B:225:0x03a8  */
    /* JADX WARN: Removed duplicated region for block: B:234:0x03ef  */
    /* JADX WARN: Removed duplicated region for block: B:245:0x043d  */
    /* JADX WARN: Removed duplicated region for block: B:248:0x0443  */
    /* JADX WARN: Removed duplicated region for block: B:252:0x06a9  */
    /* JADX WARN: Removed duplicated region for block: B:253:0x0805  */
    /* JADX WARN: Removed duplicated region for block: B:286:0x069f  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x063b A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:30:0x063c  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x00b0  */
    /* JADX WARN: Removed duplicated region for block: B:325:0x0388  */
    /* JADX WARN: Removed duplicated region for block: B:44:0x00ef  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x048d  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x0566  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x0492 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:98:0x048f  */
    /* JADX WARN: Type inference failed for: r0v138, types: [java.lang.Iterable] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:132:0x0995 -> B:106:0x09b7). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:137:0x09cf -> B:107:0x09ed). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:143:0x084c -> B:108:0x086e). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:166:0x079e -> B:147:0x07b2). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:169:0x07ca -> B:148:0x07e0). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object addVideoLinksToCallback(VideoData videoData, Function1<? super ExtractorLink, Unit> function1, String str, Continuation<? super Unit> continuation) {
        PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$1;
        PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$12;
        Map mutableMapOf;
        String str2;
        Map<String, String> mutableMap;
        int i;
        List<Language> languageOptions;
        boolean z;
        int i2;
        int i3;
        Map<String, String> map;
        Object obj;
        List<Resolution> resolutions;
        String str3;
        VideoData videoData2;
        Function1<? super ExtractorLink, Unit> function12;
        String str4;
        Map<String, String> map2;
        Object obj2;
        Continuation<? super Unit> continuation2;
        int i4;
        PikashowProvider pikashowProvider;
        Object obj3;
        int i5;
        Iterator it;
        PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$13;
        int i6;
        Object obj4;
        Object obj5;
        Map<String, String> map3;
        Continuation<? super Unit> continuation3;
        VideoData videoData3;
        Function1<? super ExtractorLink, Unit> function13;
        String str5;
        Object obj6;
        Object obj7;
        VideoData videoData4;
        Function1<? super ExtractorLink, Unit> function14;
        Map map4;
        Map<String, String> map5;
        String str6;
        String url;
        Object parseHDBVPlayerUrl;
        int i7;
        String str7;
        Map<String, String> map6;
        Function1<? super ExtractorLink, Unit> function15;
        int i8;
        boolean z2;
        boolean z3;
        boolean z4;
        Object readValue;
        List<Language> languageOptions2;
        Function1<? super ExtractorLink, Unit> function16;
        Map<String, String> map7;
        String str8;
        String str9;
        Map<String, String> map8;
        int i9;
        int i10;
        PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$14;
        Map map9;
        Iterator it2;
        Object obj8;
        Object obj9;
        int i11;
        String str10;
        Function1<? super ExtractorLink, Unit> function17;
        VideoData videoData5;
        int i12;
        Object obj10;
        Function1<? super ExtractorLink, Unit> function18;
        Object next;
        List<Resolution> list;
        Continuation<? super Unit> continuation4;
        Object obj11;
        Map map10;
        Object obj12;
        int i13;
        int i14;
        int i15;
        int i16;
        PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$15;
        Iterator it3;
        Language language;
        Object obj13;
        int i17;
        PikashowProvider pikashowProvider2;
        VideoData videoData6;
        Iterator it4;
        int i18;
        Map map11;
        List<Resolution> list2;
        int i19;
        Iterator it5;
        Object obj14;
        Map<String, String> map12;
        PikashowProvider pikashowProvider3;
        VideoData videoData7;
        Object obj15;
        Iterator it6;
        Object obj16;
        Object obj17;
        int i20;
        Function1<? super ExtractorLink, Unit> function19;
        Object obj18;
        PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$16;
        int i21;
        Map map13;
        int i22;
        String str11;
        PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$17;
        int i23;
        int i24;
        Map<String, String> map14;
        String str12;
        String name;
        String str13;
        ExtractorLinkType extractorLinkType;
        int i25;
        PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$18;
        int i26;
        Map<String, String> map15;
        Object newExtractorLink;
        Map<String, String> map16;
        Function1<? super ExtractorLink, Unit> function110;
        int i27;
        Map map17;
        Object obj19;
        Function1<? super ExtractorLink, Unit> function111;
        VideoData videoData8;
        Function1<? super ExtractorLink, Unit> function112;
        String str14;
        VideoData videoData9;
        Map map18;
        Map<String, String> map19;
        VideoData videoData10;
        Function1<? super ExtractorLink, Unit> function113;
        String str15;
        Map<String, String> map20;
        int i28;
        Map map21;
        int i29;
        Continuation<? super Unit> continuation5 = continuation;
        if (continuation5 instanceof PikashowProvider$addVideoLinksToCallback$1) {
            pikashowProvider$addVideoLinksToCallback$1 = (PikashowProvider$addVideoLinksToCallback$1) continuation5;
            if ((pikashowProvider$addVideoLinksToCallback$1.label & Integer.MIN_VALUE) != 0) {
                pikashowProvider$addVideoLinksToCallback$1.label -= Integer.MIN_VALUE;
                pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$1;
                Object obj20 = pikashowProvider$addVideoLinksToCallback$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                String str16 = "m3u8";
                switch (pikashowProvider$addVideoLinksToCallback$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj20);
                        mutableMapOf = MapsKt.mutableMapOf(new Pair[]{TuplesKt.to("Referer", "https://samui390dod.com/"), TuplesKt.to("Origin", "https://samui390dod.com")});
                        String heastr = videoData.getHeastr();
                        if (heastr != null) {
                            mutableMapOf.put("heastr", heastr);
                            Unit unit = Unit.INSTANCE;
                        }
                        String uastr = videoData.getUastr();
                        if (uastr != null) {
                            mutableMapOf.put("user-agent", uastr);
                            Unit unit2 = Unit.INSTANCE;
                        }
                        String uaStr = videoData.getUaStr();
                        if (uaStr != null) {
                            mutableMapOf.put("user-agent", uaStr);
                            Unit unit3 = Unit.INSTANCE;
                        }
                        String headerStr = videoData.getHeaderStr();
                        if (headerStr != null) {
                            try {
                                str2 = "Unknown";
                            } catch (Exception e) {
                                e = e;
                                str2 = "Unknown";
                            }
                            try {
                                readValue = this.mapper.readValue(headerStr, new PikashowProvider$addVideoLinksToCallback$lambda$3$$inlined$readValue$1());
                            } catch (Exception e2) {
                                e = e2;
                                System.out.println((Object) ("Failed to parse headerStr: " + e.getMessage()));
                                Unit unit4 = Unit.INSTANCE;
                                if (videoData.getHeaders() == null) {
                                }
                                Map<String, String> map22 = mutableMap;
                                List<Resolution> resolutions2 = videoData.getResolutions();
                                i = ((resolutions2 != null || resolutions2.isEmpty()) ? 1 : 0) ^ 1;
                                languageOptions = videoData.getLanguageOptions();
                                if (languageOptions != null) {
                                }
                                z = false;
                                if (!z) {
                                }
                                i2 = 1;
                                i3 = i2;
                                if (i == 0) {
                                }
                                resolutions = videoData.getResolutions();
                                if (resolutions == null) {
                                }
                            }
                            if (!(readValue instanceof Map)) {
                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(Map.class).getQualifiedName() + "(non-null) but was " + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : null));
                            }
                            mutableMapOf.putAll((Map) readValue);
                            Unit unit42 = Unit.INSTANCE;
                        } else {
                            str2 = "Unknown";
                        }
                        if (videoData.getHeaders() == null) {
                            Map mutableMap2 = MapsKt.toMutableMap(mutableMapOf);
                            mutableMap2.putAll(videoData.getHeaders());
                            String heastr2 = videoData.getHeastr();
                            if (heastr2 != null) {
                                mutableMap2.put("heastr", heastr2);
                                Unit unit5 = Unit.INSTANCE;
                            }
                            String uastr2 = videoData.getUastr();
                            if (uastr2 != null) {
                                mutableMap2.put("user-agent", uastr2);
                                Unit unit6 = Unit.INSTANCE;
                            }
                            String uaStr2 = videoData.getUaStr();
                            if (uaStr2 != null) {
                                mutableMap2.put("user-agent", uaStr2);
                                Unit unit7 = Unit.INSTANCE;
                            }
                            mutableMap = MapsKt.toMutableMap(mutableMap2);
                        } else {
                            mutableMap = MapsKt.toMutableMap(mutableMapOf);
                        }
                        Map<String, String> map222 = mutableMap;
                        List<Resolution> resolutions22 = videoData.getResolutions();
                        i = ((resolutions22 != null || resolutions22.isEmpty()) ? 1 : 0) ^ 1;
                        languageOptions = videoData.getLanguageOptions();
                        if (languageOptions != null) {
                            List<Language> list3 = languageOptions;
                            if ((list3 instanceof Collection) && list3.isEmpty()) {
                                z4 = false;
                            } else {
                                Iterator it7 = list3.iterator();
                                while (true) {
                                    if (it7.hasNext()) {
                                        List<Resolution> resolutions3 = ((Language) it7.next()).getResolutions();
                                        if (!(resolutions3 == null || resolutions3.isEmpty())) {
                                            z4 = true;
                                        }
                                    } else {
                                        z4 = false;
                                    }
                                }
                            }
                            if (z4) {
                                z = true;
                                if (!z) {
                                    List<Language> languages = videoData.getLanguages();
                                    if (languages != null) {
                                        List<Language> list4 = languages;
                                        if ((list4 instanceof Collection) && list4.isEmpty()) {
                                            z3 = false;
                                        } else {
                                            Iterator it8 = list4.iterator();
                                            while (true) {
                                                if (it8.hasNext()) {
                                                    List<Resolution> resolutions4 = ((Language) it8.next()).getResolutions();
                                                    if (!(resolutions4 == null || resolutions4.isEmpty())) {
                                                        z3 = true;
                                                    }
                                                } else {
                                                    z3 = false;
                                                }
                                            }
                                        }
                                        if (z3) {
                                            z2 = true;
                                            if (!z2) {
                                                i2 = 0;
                                                i3 = i2;
                                                if (i == 0) {
                                                    map = map222;
                                                    obj = obj20;
                                                } else {
                                                    if (i3 == 0) {
                                                        if (videoData.getUrl() == null) {
                                                            pikashowProvider$addVideoLinksToCallback$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$1 = SpillingKt.nullOutSpilledVariable(function1);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$2 = SpillingKt.nullOutSpilledVariable(str);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(mutableMapOf);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$4 = SpillingKt.nullOutSpilledVariable(map222);
                                                            pikashowProvider$addVideoLinksToCallback$12.I$0 = i;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$1 = i3;
                                                            pikashowProvider$addVideoLinksToCallback$12.label = 7;
                                                            return fallbackToDirectUrls(videoData, function1, str, map222, pikashowProvider$addVideoLinksToCallback$12) == coroutine_suspended ? coroutine_suspended : Unit.INSTANCE;
                                                        }
                                                        try {
                                                            url = videoData.getUrl();
                                                            videoData3 = videoData;
                                                        } catch (Exception e3) {
                                                            e = e3;
                                                            videoData3 = videoData;
                                                        }
                                                        try {
                                                            pikashowProvider$addVideoLinksToCallback$12.L$0 = videoData3;
                                                            function13 = function1;
                                                        } catch (Exception e4) {
                                                            e = e4;
                                                            function13 = function1;
                                                            str5 = str;
                                                            obj6 = obj20;
                                                            obj7 = null;
                                                            videoData4 = videoData3;
                                                            function14 = function13;
                                                            map4 = mutableMapOf;
                                                            map5 = map222;
                                                            str6 = str5;
                                                            System.out.println((Object) ("Error parsing HDBV player URL: " + e.getMessage()));
                                                            pikashowProvider$addVideoLinksToCallback$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$1 = SpillingKt.nullOutSpilledVariable(function14);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$2 = SpillingKt.nullOutSpilledVariable(str6);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(map4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$4 = SpillingKt.nullOutSpilledVariable(map5);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$5 = SpillingKt.nullOutSpilledVariable(e);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$6 = obj7;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$7 = obj7;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$0 = i;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$1 = i3;
                                                            pikashowProvider$addVideoLinksToCallback$12.label = 6;
                                                            if (fallbackToDirectUrls(videoData4, function14, str6, map5, pikashowProvider$addVideoLinksToCallback$12) != coroutine_suspended) {
                                                            }
                                                        }
                                                        try {
                                                            pikashowProvider$addVideoLinksToCallback$12.L$1 = function13;
                                                            str5 = str;
                                                        } catch (Exception e5) {
                                                            e = e5;
                                                            str5 = str;
                                                            obj6 = obj20;
                                                            obj7 = null;
                                                            videoData4 = videoData3;
                                                            function14 = function13;
                                                            map4 = mutableMapOf;
                                                            map5 = map222;
                                                            str6 = str5;
                                                            System.out.println((Object) ("Error parsing HDBV player URL: " + e.getMessage()));
                                                            pikashowProvider$addVideoLinksToCallback$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$1 = SpillingKt.nullOutSpilledVariable(function14);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$2 = SpillingKt.nullOutSpilledVariable(str6);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(map4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$4 = SpillingKt.nullOutSpilledVariable(map5);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$5 = SpillingKt.nullOutSpilledVariable(e);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$6 = obj7;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$7 = obj7;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$0 = i;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$1 = i3;
                                                            pikashowProvider$addVideoLinksToCallback$12.label = 6;
                                                            if (fallbackToDirectUrls(videoData4, function14, str6, map5, pikashowProvider$addVideoLinksToCallback$12) != coroutine_suspended) {
                                                            }
                                                        }
                                                        try {
                                                            pikashowProvider$addVideoLinksToCallback$12.L$2 = str5;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(mutableMapOf);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$4 = map222;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$0 = i;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$1 = i3;
                                                            pikashowProvider$addVideoLinksToCallback$12.label = 3;
                                                            parseHDBVPlayerUrl = parseHDBVPlayerUrl(url, pikashowProvider$addVideoLinksToCallback$12);
                                                        } catch (Exception e6) {
                                                            e = e6;
                                                            obj6 = obj20;
                                                            obj7 = null;
                                                            videoData4 = videoData3;
                                                            function14 = function13;
                                                            map4 = mutableMapOf;
                                                            map5 = map222;
                                                            str6 = str5;
                                                            System.out.println((Object) ("Error parsing HDBV player URL: " + e.getMessage()));
                                                            pikashowProvider$addVideoLinksToCallback$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$1 = SpillingKt.nullOutSpilledVariable(function14);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$2 = SpillingKt.nullOutSpilledVariable(str6);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(map4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$4 = SpillingKt.nullOutSpilledVariable(map5);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$5 = SpillingKt.nullOutSpilledVariable(e);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$6 = obj7;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$7 = obj7;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$0 = i;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$1 = i3;
                                                            pikashowProvider$addVideoLinksToCallback$12.label = 6;
                                                            if (fallbackToDirectUrls(videoData4, function14, str6, map5, pikashowProvider$addVideoLinksToCallback$12) != coroutine_suspended) {
                                                            }
                                                        }
                                                        if (parseHDBVPlayerUrl == coroutine_suspended) {
                                                            return coroutine_suspended;
                                                        }
                                                        i7 = i;
                                                        str7 = str5;
                                                        map4 = mutableMapOf;
                                                        map6 = map222;
                                                        function15 = function13;
                                                        i8 = i3;
                                                        try {
                                                            str11 = (String) parseHDBVPlayerUrl;
                                                        } catch (Exception e7) {
                                                            e = e7;
                                                            function14 = function15;
                                                            VideoData videoData11 = videoData3;
                                                            str6 = str7;
                                                            i3 = i8;
                                                            i = i7;
                                                            obj6 = obj20;
                                                            obj7 = null;
                                                            map5 = map6;
                                                            videoData4 = videoData11;
                                                        }
                                                        if (str11.length() <= 0) {
                                                            int i30 = i8;
                                                            int i31 = i7;
                                                            Map<String, String> map23 = map6;
                                                            obj6 = obj20;
                                                            obj7 = null;
                                                            try {
                                                                pikashowProvider$addVideoLinksToCallback$12.L$0 = videoData3;
                                                                pikashowProvider$addVideoLinksToCallback$12.L$1 = function15;
                                                                pikashowProvider$addVideoLinksToCallback$12.L$2 = str7;
                                                                pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(map4);
                                                                pikashowProvider$addVideoLinksToCallback$12.L$4 = map23;
                                                                pikashowProvider$addVideoLinksToCallback$12.L$5 = SpillingKt.nullOutSpilledVariable(str11);
                                                                pikashowProvider$addVideoLinksToCallback$12.I$0 = i31;
                                                                pikashowProvider$addVideoLinksToCallback$12.I$1 = i30;
                                                                pikashowProvider$addVideoLinksToCallback$12.label = 5;
                                                                videoData8 = videoData3;
                                                                map5 = map23;
                                                                String str17 = str7;
                                                                i = i31;
                                                                function14 = function15;
                                                                str6 = str17;
                                                                i3 = i30;
                                                                pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$12;
                                                                try {
                                                                } catch (Exception e8) {
                                                                    e = e8;
                                                                    videoData4 = videoData8;
                                                                }
                                                            } catch (Exception e9) {
                                                                e = e9;
                                                                String str18 = str7;
                                                                i = i31;
                                                                function14 = function15;
                                                                str6 = str18;
                                                                VideoData videoData12 = videoData3;
                                                                i3 = i30;
                                                                pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$12;
                                                                map5 = map23;
                                                                videoData4 = videoData12;
                                                            }
                                                            if (fallbackToDirectUrls(videoData8, function14, str6, map5, pikashowProvider$addVideoLinksToCallback$12) == coroutine_suspended) {
                                                                return coroutine_suspended;
                                                            }
                                                            function112 = function14;
                                                            str14 = str6;
                                                            i25 = i;
                                                            videoData9 = videoData8;
                                                            map18 = map4;
                                                            map19 = map5;
                                                            str7 = str14;
                                                            Unit unit8 = Unit.INSTANCE;
                                                            return Unit.INSTANCE;
                                                        }
                                                        try {
                                                            try {
                                                                str12 = StringsKt.substringBefore(videoData3.getUrl(), "/", "https://") + "://" + StringsKt.substringBefore$default(StringsKt.substringAfter$default(videoData3.getUrl(), "://", (String) null, 2, (Object) null), "/", (String) null, 2, (Object) null) + "/";
                                                                name = getName();
                                                                str13 = str7 + " - HDBV";
                                                                extractorLinkType = ExtractorLinkType.M3U8;
                                                            } catch (Exception e10) {
                                                                e = e10;
                                                                pikashowProvider$addVideoLinksToCallback$17 = pikashowProvider$addVideoLinksToCallback$12;
                                                                i23 = i8;
                                                                i24 = i7;
                                                                map14 = map6;
                                                                obj6 = obj20;
                                                                obj7 = null;
                                                            }
                                                        } catch (Exception e11) {
                                                            e = e11;
                                                            pikashowProvider$addVideoLinksToCallback$17 = pikashowProvider$addVideoLinksToCallback$12;
                                                            i23 = i8;
                                                            i24 = i7;
                                                            map14 = map6;
                                                            obj6 = obj20;
                                                            obj7 = null;
                                                        }
                                                        try {
                                                            PikashowProvider$addVideoLinksToCallback$8 pikashowProvider$addVideoLinksToCallback$8 = new PikashowProvider$addVideoLinksToCallback$8(str12, map6, null);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$0 = videoData3;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$1 = function15;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$2 = str7;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(map4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$4 = map6;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$5 = SpillingKt.nullOutSpilledVariable(str11);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$7 = function15;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$0 = i7;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$1 = i8;
                                                            pikashowProvider$addVideoLinksToCallback$12.label = 4;
                                                            i25 = i7;
                                                            pikashowProvider$addVideoLinksToCallback$18 = pikashowProvider$addVideoLinksToCallback$12;
                                                            i26 = i8;
                                                            obj6 = obj20;
                                                            obj7 = null;
                                                            map15 = map6;
                                                            try {
                                                                newExtractorLink = ExtractorApiKt.newExtractorLink(name, str13, str11, extractorLinkType, pikashowProvider$addVideoLinksToCallback$8, pikashowProvider$addVideoLinksToCallback$18);
                                                            } catch (Exception e12) {
                                                                e = e12;
                                                                VideoData videoData13 = videoData3;
                                                                map5 = map15;
                                                                videoData4 = videoData13;
                                                                String str19 = str7;
                                                                i = i25;
                                                                function14 = function15;
                                                                str6 = str19;
                                                                i3 = i26;
                                                                pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$18;
                                                            }
                                                        } catch (Exception e13) {
                                                            e = e13;
                                                            i24 = i7;
                                                            map14 = map6;
                                                            obj6 = obj20;
                                                            obj7 = null;
                                                            pikashowProvider$addVideoLinksToCallback$17 = pikashowProvider$addVideoLinksToCallback$12;
                                                            i23 = i8;
                                                            VideoData videoData14 = videoData3;
                                                            map5 = map14;
                                                            videoData4 = videoData14;
                                                            String str20 = str7;
                                                            i = i24;
                                                            function14 = function15;
                                                            str6 = str20;
                                                            i3 = i23;
                                                            pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$17;
                                                            System.out.println((Object) ("Error parsing HDBV player URL: " + e.getMessage()));
                                                            pikashowProvider$addVideoLinksToCallback$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$1 = SpillingKt.nullOutSpilledVariable(function14);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$2 = SpillingKt.nullOutSpilledVariable(str6);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(map4);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$4 = SpillingKt.nullOutSpilledVariable(map5);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$5 = SpillingKt.nullOutSpilledVariable(e);
                                                            pikashowProvider$addVideoLinksToCallback$12.L$6 = obj7;
                                                            pikashowProvider$addVideoLinksToCallback$12.L$7 = obj7;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$0 = i;
                                                            pikashowProvider$addVideoLinksToCallback$12.I$1 = i3;
                                                            pikashowProvider$addVideoLinksToCallback$12.label = 6;
                                                            if (fallbackToDirectUrls(videoData4, function14, str6, map5, pikashowProvider$addVideoLinksToCallback$12) != coroutine_suspended) {
                                                            }
                                                        }
                                                        if (newExtractorLink == coroutine_suspended) {
                                                            return coroutine_suspended;
                                                        }
                                                        map16 = map15;
                                                        function110 = function15;
                                                        i27 = i26;
                                                        map17 = map4;
                                                        obj19 = newExtractorLink;
                                                        function111 = function110;
                                                        try {
                                                            function111.invoke(obj19);
                                                        } catch (Exception e14) {
                                                            e = e14;
                                                            i3 = i27;
                                                            videoData4 = videoData3;
                                                            str6 = str7;
                                                            map5 = map16;
                                                            map4 = map17;
                                                            pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$18;
                                                            i = i25;
                                                            function14 = function110;
                                                        }
                                                        Unit unit82 = Unit.INSTANCE;
                                                        return Unit.INSTANCE;
                                                        System.out.println((Object) ("Error parsing HDBV player URL: " + e.getMessage()));
                                                        pikashowProvider$addVideoLinksToCallback$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData4);
                                                        pikashowProvider$addVideoLinksToCallback$12.L$1 = SpillingKt.nullOutSpilledVariable(function14);
                                                        pikashowProvider$addVideoLinksToCallback$12.L$2 = SpillingKt.nullOutSpilledVariable(str6);
                                                        pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(map4);
                                                        pikashowProvider$addVideoLinksToCallback$12.L$4 = SpillingKt.nullOutSpilledVariable(map5);
                                                        pikashowProvider$addVideoLinksToCallback$12.L$5 = SpillingKt.nullOutSpilledVariable(e);
                                                        pikashowProvider$addVideoLinksToCallback$12.L$6 = obj7;
                                                        pikashowProvider$addVideoLinksToCallback$12.L$7 = obj7;
                                                        pikashowProvider$addVideoLinksToCallback$12.I$0 = i;
                                                        pikashowProvider$addVideoLinksToCallback$12.I$1 = i3;
                                                        pikashowProvider$addVideoLinksToCallback$12.label = 6;
                                                        if (fallbackToDirectUrls(videoData4, function14, str6, map5, pikashowProvider$addVideoLinksToCallback$12) != coroutine_suspended) {
                                                            return coroutine_suspended;
                                                        }
                                                        videoData10 = videoData4;
                                                        function113 = function14;
                                                        str15 = str6;
                                                        map20 = map5;
                                                        i28 = i;
                                                        map21 = map4;
                                                        i29 = i3;
                                                        i25 = i28;
                                                        str7 = str15;
                                                        Unit unit822 = Unit.INSTANCE;
                                                        return Unit.INSTANCE;
                                                    }
                                                    map = map222;
                                                    obj = obj20;
                                                }
                                                resolutions = videoData.getResolutions();
                                                if (resolutions == null) {
                                                    List<Resolution> list5 = resolutions;
                                                    i5 = 0;
                                                    it = list5.iterator();
                                                    pikashowProvider$addVideoLinksToCallback$13 = pikashowProvider$addVideoLinksToCallback$12;
                                                    i6 = i;
                                                    obj4 = coroutine_suspended;
                                                    function12 = function1;
                                                    str4 = str;
                                                    obj5 = list5;
                                                    map3 = map;
                                                    i4 = i3;
                                                    pikashowProvider = this;
                                                    videoData2 = videoData;
                                                    continuation3 = continuation;
                                                    if (!it.hasNext()) {
                                                        Object next2 = it.next();
                                                        Resolution resolution = (Resolution) next2;
                                                        Continuation<? super Unit> continuation6 = continuation3;
                                                        String url2 = resolution.getUrl();
                                                        if (url2 != null) {
                                                            Object obj21 = obj5;
                                                            str9 = str16;
                                                            Map map24 = mutableMapOf;
                                                            int i32 = i5;
                                                            ExtractorLinkType extractorLinkType2 = (StringsKt.contains$default(url2, str9, false, 2, (Object) null) || Intrinsics.areEqual(videoData2.getSourceType(), "hls")) ? ExtractorLinkType.M3U8 : Intrinsics.areEqual(videoData2.getSourceType(), "direct") ? ExtractorLinkType.VIDEO : ExtractorLinkType.VIDEO;
                                                            String name2 = pikashowProvider.getName();
                                                            String label = resolution.getLabel();
                                                            if (label == null) {
                                                                label = str2;
                                                            }
                                                            String str21 = label + " - " + str4;
                                                            PikashowProvider$addVideoLinksToCallback$6$1$1 pikashowProvider$addVideoLinksToCallback$6$1$1 = new PikashowProvider$addVideoLinksToCallback$6$1$1(pikashowProvider, resolution, map3, null);
                                                            pikashowProvider$addVideoLinksToCallback$13.L$0 = videoData2;
                                                            pikashowProvider$addVideoLinksToCallback$13.L$1 = function12;
                                                            pikashowProvider$addVideoLinksToCallback$13.L$2 = str4;
                                                            pikashowProvider$addVideoLinksToCallback$13.L$3 = SpillingKt.nullOutSpilledVariable(map24);
                                                            pikashowProvider$addVideoLinksToCallback$13.L$4 = map3;
                                                            pikashowProvider$addVideoLinksToCallback$13.L$5 = SpillingKt.nullOutSpilledVariable(obj21);
                                                            pikashowProvider$addVideoLinksToCallback$13.L$6 = it;
                                                            pikashowProvider$addVideoLinksToCallback$13.L$7 = SpillingKt.nullOutSpilledVariable(next2);
                                                            pikashowProvider$addVideoLinksToCallback$13.L$8 = SpillingKt.nullOutSpilledVariable(resolution);
                                                            pikashowProvider$addVideoLinksToCallback$13.L$9 = SpillingKt.nullOutSpilledVariable(url2);
                                                            pikashowProvider$addVideoLinksToCallback$13.L$10 = SpillingKt.nullOutSpilledVariable(extractorLinkType2);
                                                            pikashowProvider$addVideoLinksToCallback$13.L$11 = function12;
                                                            int i33 = i6;
                                                            pikashowProvider$addVideoLinksToCallback$13.I$0 = i33;
                                                            pikashowProvider$addVideoLinksToCallback$13.I$1 = i4;
                                                            i9 = i32;
                                                            pikashowProvider$addVideoLinksToCallback$13.I$2 = i9;
                                                            i10 = 0;
                                                            pikashowProvider$addVideoLinksToCallback$13.I$3 = 0;
                                                            pikashowProvider$addVideoLinksToCallback$13.I$4 = 0;
                                                            pikashowProvider$addVideoLinksToCallback$13.label = 1;
                                                            pikashowProvider$addVideoLinksToCallback$14 = pikashowProvider$addVideoLinksToCallback$13;
                                                            Object newExtractorLink2 = ExtractorApiKt.newExtractorLink(name2, str21, url2, extractorLinkType2, pikashowProvider$addVideoLinksToCallback$6$1$1, pikashowProvider$addVideoLinksToCallback$14);
                                                            Object obj22 = obj4;
                                                            if (newExtractorLink2 == obj22) {
                                                                return obj22;
                                                            }
                                                            map9 = map24;
                                                            it2 = it;
                                                            map8 = map3;
                                                            obj8 = obj22;
                                                            continuation5 = continuation6;
                                                            obj9 = obj21;
                                                            i11 = i33;
                                                            str10 = str4;
                                                            function17 = function12;
                                                            videoData5 = videoData2;
                                                            i12 = i4;
                                                            obj10 = newExtractorLink2;
                                                            function18 = function17;
                                                            function18.invoke(obj10);
                                                            Unit unit9 = Unit.INSTANCE;
                                                            videoData2 = videoData5;
                                                            function12 = function17;
                                                            str4 = str10;
                                                            i4 = i12;
                                                            i6 = i11;
                                                            obj4 = obj8;
                                                            mutableMapOf = map9;
                                                            i5 = i9;
                                                            obj5 = obj9;
                                                            it = it2;
                                                            pikashowProvider$addVideoLinksToCallback$13 = pikashowProvider$addVideoLinksToCallback$14;
                                                            continuation3 = continuation5;
                                                            str16 = str9;
                                                            map3 = map8;
                                                            if (!it.hasNext()) {
                                                                str3 = str16;
                                                                obj2 = obj4;
                                                                Unit unit10 = Unit.INSTANCE;
                                                                map2 = map3;
                                                                pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$13;
                                                                i = i6;
                                                                continuation2 = continuation3;
                                                                obj3 = obj;
                                                                languageOptions2 = videoData2.getLanguageOptions();
                                                                if (languageOptions2 == null) {
                                                                    languageOptions2 = videoData2.getLanguages();
                                                                }
                                                                if (languageOptions2 != null) {
                                                                    return Unit.INSTANCE;
                                                                }
                                                                List<Language> list6 = languageOptions2;
                                                                Iterator it9 = list6.iterator();
                                                                Object obj23 = obj2;
                                                                function16 = function12;
                                                                PikashowProvider pikashowProvider4 = pikashowProvider;
                                                                Map map25 = mutableMapOf;
                                                                Object obj24 = list6;
                                                                map7 = map2;
                                                                str8 = str4;
                                                                int i34 = i4;
                                                                Object obj25 = obj3;
                                                                int i35 = 0;
                                                                Object obj26 = obj23;
                                                                while (it9.hasNext()) {
                                                                    next = it9.next();
                                                                    Language language2 = (Language) next;
                                                                    List<Resolution> resolutions5 = language2.getResolutions();
                                                                    if (resolutions5 != null) {
                                                                        list = resolutions5;
                                                                        continuation4 = continuation2;
                                                                        obj11 = obj24;
                                                                        map10 = map25;
                                                                        obj12 = obj26;
                                                                        i13 = i35;
                                                                        i14 = 0;
                                                                        i15 = 0;
                                                                        i16 = i34;
                                                                        pikashowProvider$addVideoLinksToCallback$15 = pikashowProvider$addVideoLinksToCallback$12;
                                                                        it3 = it9;
                                                                        language = language2;
                                                                        obj13 = obj25;
                                                                        i17 = i;
                                                                        pikashowProvider2 = pikashowProvider4;
                                                                        videoData6 = videoData2;
                                                                        it4 = list.iterator();
                                                                        if (!it4.hasNext()) {
                                                                            Object next3 = it4.next();
                                                                            i18 = i15;
                                                                            Resolution resolution2 = (Resolution) next3;
                                                                            int i36 = i14;
                                                                            String url3 = resolution2.getUrl();
                                                                            if (url3 != null) {
                                                                                int i37 = i13;
                                                                                Language language3 = language;
                                                                                int i38 = i16;
                                                                                int i39 = i17;
                                                                                ExtractorLinkType extractorLinkType3 = (StringsKt.contains$default(url3, str3, false, 2, (Object) null) || Intrinsics.areEqual(videoData6.getSourceType(), "hls")) ? ExtractorLinkType.M3U8 : Intrinsics.areEqual(videoData6.getSourceType(), "direct") ? ExtractorLinkType.VIDEO : ExtractorLinkType.VIDEO;
                                                                                String language4 = language3.getLanguage();
                                                                                String language5 = language4 == null || StringsKt.isBlank(language4) ? "Default" : language3.getLanguage();
                                                                                String name3 = pikashowProvider2.getName();
                                                                                String label2 = resolution2.getLabel();
                                                                                if (label2 == null) {
                                                                                    label2 = str2;
                                                                                }
                                                                                String str22 = label2 + " (" + language5 + ") - " + str8;
                                                                                PikashowProvider$addVideoLinksToCallback$7$1$1$1 pikashowProvider$addVideoLinksToCallback$7$1$1$1 = new PikashowProvider$addVideoLinksToCallback$7$1$1$1(pikashowProvider2, resolution2, map7, null);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$0 = videoData6;
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$1 = function16;
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$2 = str8;
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$3 = SpillingKt.nullOutSpilledVariable(map10);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$4 = map7;
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$5 = SpillingKt.nullOutSpilledVariable(obj11);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$6 = it3;
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$7 = SpillingKt.nullOutSpilledVariable(next);
                                                                                language = language3;
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$8 = language;
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$9 = SpillingKt.nullOutSpilledVariable(list);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$10 = it4;
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$11 = SpillingKt.nullOutSpilledVariable(next3);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$12 = SpillingKt.nullOutSpilledVariable(resolution2);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$13 = SpillingKt.nullOutSpilledVariable(url3);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$14 = SpillingKt.nullOutSpilledVariable(extractorLinkType3);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$15 = SpillingKt.nullOutSpilledVariable(language5);
                                                                                pikashowProvider$addVideoLinksToCallback$15.L$16 = function16;
                                                                                i17 = i39;
                                                                                pikashowProvider$addVideoLinksToCallback$15.I$0 = i17;
                                                                                pikashowProvider$addVideoLinksToCallback$15.I$1 = i38;
                                                                                pikashowProvider$addVideoLinksToCallback$15.I$2 = i37;
                                                                                Iterator it10 = it4;
                                                                                pikashowProvider$addVideoLinksToCallback$15.I$3 = i36;
                                                                                pikashowProvider$addVideoLinksToCallback$15.I$4 = i18;
                                                                                pikashowProvider$addVideoLinksToCallback$15.I$5 = 0;
                                                                                pikashowProvider$addVideoLinksToCallback$15.I$6 = 0;
                                                                                videoData7 = videoData6;
                                                                                pikashowProvider$addVideoLinksToCallback$15.label = 2;
                                                                                PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$19 = pikashowProvider$addVideoLinksToCallback$15;
                                                                                Object newExtractorLink3 = ExtractorApiKt.newExtractorLink(name3, str22, url3, extractorLinkType3, pikashowProvider$addVideoLinksToCallback$7$1$1$1, pikashowProvider$addVideoLinksToCallback$19);
                                                                                Object obj27 = obj12;
                                                                                if (newExtractorLink3 == obj27) {
                                                                                    return obj27;
                                                                                }
                                                                                obj15 = obj27;
                                                                                it6 = it3;
                                                                                obj16 = newExtractorLink3;
                                                                                obj17 = next;
                                                                                list2 = list;
                                                                                i19 = i36;
                                                                                it5 = it10;
                                                                                map12 = map7;
                                                                                i20 = i37;
                                                                                function19 = function16;
                                                                                obj18 = obj13;
                                                                                pikashowProvider$addVideoLinksToCallback$16 = pikashowProvider$addVideoLinksToCallback$19;
                                                                                obj14 = obj11;
                                                                                pikashowProvider3 = pikashowProvider2;
                                                                                i21 = i38;
                                                                                continuation5 = continuation4;
                                                                                map13 = map10;
                                                                                i22 = 0;
                                                                                function19.invoke(obj16);
                                                                                Unit unit11 = Unit.INSTANCE;
                                                                                map11 = map13;
                                                                                next = obj17;
                                                                                i13 = i20;
                                                                                it3 = it6;
                                                                                obj12 = obj15;
                                                                                pikashowProvider$addVideoLinksToCallback$15 = pikashowProvider$addVideoLinksToCallback$16;
                                                                                i16 = i21;
                                                                                obj13 = obj18;
                                                                                videoData6 = videoData7;
                                                                                map10 = map11;
                                                                                continuation4 = continuation5;
                                                                                pikashowProvider2 = pikashowProvider3;
                                                                                i15 = i18;
                                                                                i14 = i19;
                                                                                it4 = it5;
                                                                                list = list2;
                                                                                obj11 = obj14;
                                                                                map7 = map12;
                                                                                if (!it4.hasNext()) {
                                                                                    VideoData videoData15 = videoData6;
                                                                                    PikashowProvider$addVideoLinksToCallback$1 pikashowProvider$addVideoLinksToCallback$110 = pikashowProvider$addVideoLinksToCallback$15;
                                                                                    int i40 = i13;
                                                                                    Unit unit12 = Unit.INSTANCE;
                                                                                    continuation2 = continuation4;
                                                                                    obj26 = obj12;
                                                                                    pikashowProvider4 = pikashowProvider2;
                                                                                    i35 = i40;
                                                                                    i = i17;
                                                                                    obj25 = obj13;
                                                                                    pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$110;
                                                                                    videoData2 = videoData15;
                                                                                    map25 = map10;
                                                                                    it9 = it3;
                                                                                    i34 = i16;
                                                                                    obj24 = obj11;
                                                                                    while (it9.hasNext()) {
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                Iterator it11 = it4;
                                                                                map11 = map10;
                                                                                list2 = list;
                                                                                i19 = i36;
                                                                                it5 = it11;
                                                                                obj14 = obj11;
                                                                                map12 = map7;
                                                                                pikashowProvider3 = pikashowProvider2;
                                                                                continuation5 = continuation4;
                                                                                videoData6 = videoData6;
                                                                                map10 = map11;
                                                                                continuation4 = continuation5;
                                                                                pikashowProvider2 = pikashowProvider3;
                                                                                i15 = i18;
                                                                                i14 = i19;
                                                                                it4 = it5;
                                                                                list = list2;
                                                                                obj11 = obj14;
                                                                                map7 = map12;
                                                                                if (!it4.hasNext()) {
                                                                                }
                                                                            }
                                                                        }
                                                                    } else {
                                                                        obj24 = obj24;
                                                                        pikashowProvider4 = pikashowProvider4;
                                                                    }
                                                                }
                                                                Unit unit13 = Unit.INSTANCE;
                                                                return Unit.INSTANCE;
                                                            }
                                                        } else {
                                                            str9 = str16;
                                                            map8 = map3;
                                                            continuation5 = continuation6;
                                                            i5 = i5;
                                                            obj5 = obj5;
                                                            continuation3 = continuation5;
                                                            str16 = str9;
                                                            map3 = map8;
                                                            if (!it.hasNext()) {
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    str3 = "m3u8";
                                                    videoData2 = videoData;
                                                    function12 = function1;
                                                    str4 = str;
                                                    map2 = map;
                                                    obj2 = coroutine_suspended;
                                                    continuation2 = continuation;
                                                    i4 = i3;
                                                    pikashowProvider = this;
                                                    obj3 = obj;
                                                    languageOptions2 = videoData2.getLanguageOptions();
                                                    if (languageOptions2 == null) {
                                                    }
                                                    if (languageOptions2 != null) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    z2 = false;
                                    if (!z2) {
                                    }
                                }
                                i2 = 1;
                                i3 = i2;
                                if (i == 0) {
                                }
                                resolutions = videoData.getResolutions();
                                if (resolutions == null) {
                                }
                            }
                        }
                        z = false;
                        if (!z) {
                        }
                        i2 = 1;
                        i3 = i2;
                        if (i == 0) {
                        }
                        resolutions = videoData.getResolutions();
                        if (resolutions == null) {
                        }
                        break;
                    case 1:
                        int i41 = pikashowProvider$addVideoLinksToCallback$12.I$4;
                        int i42 = pikashowProvider$addVideoLinksToCallback$12.I$3;
                        int i43 = pikashowProvider$addVideoLinksToCallback$12.I$2;
                        i12 = pikashowProvider$addVideoLinksToCallback$12.I$1;
                        int i44 = pikashowProvider$addVideoLinksToCallback$12.I$0;
                        Function1<? super ExtractorLink, Unit> function114 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$11;
                        Object obj28 = pikashowProvider$addVideoLinksToCallback$12.L$7;
                        it2 = (Iterator) pikashowProvider$addVideoLinksToCallback$12.L$6;
                        Object obj29 = (Iterable) pikashowProvider$addVideoLinksToCallback$12.L$5;
                        map8 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$4;
                        map9 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$3;
                        String str23 = (String) pikashowProvider$addVideoLinksToCallback$12.L$2;
                        Function1<? super ExtractorLink, Unit> function115 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$1;
                        VideoData videoData16 = (VideoData) pikashowProvider$addVideoLinksToCallback$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        str2 = "Unknown";
                        obj9 = obj29;
                        pikashowProvider$addVideoLinksToCallback$14 = pikashowProvider$addVideoLinksToCallback$12;
                        obj = obj20;
                        i9 = i43;
                        function18 = function114;
                        str10 = str23;
                        str9 = "m3u8";
                        i11 = i44;
                        pikashowProvider = this;
                        i10 = i42;
                        function17 = function115;
                        videoData5 = videoData16;
                        obj8 = coroutine_suspended;
                        obj10 = obj;
                        function18.invoke(obj10);
                        Unit unit92 = Unit.INSTANCE;
                        videoData2 = videoData5;
                        function12 = function17;
                        str4 = str10;
                        i4 = i12;
                        i6 = i11;
                        obj4 = obj8;
                        mutableMapOf = map9;
                        i5 = i9;
                        obj5 = obj9;
                        it = it2;
                        pikashowProvider$addVideoLinksToCallback$13 = pikashowProvider$addVideoLinksToCallback$14;
                        continuation3 = continuation5;
                        str16 = str9;
                        map3 = map8;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 2:
                        int i45 = pikashowProvider$addVideoLinksToCallback$12.I$6;
                        int i46 = pikashowProvider$addVideoLinksToCallback$12.I$5;
                        int i47 = pikashowProvider$addVideoLinksToCallback$12.I$4;
                        int i48 = pikashowProvider$addVideoLinksToCallback$12.I$3;
                        int i49 = pikashowProvider$addVideoLinksToCallback$12.I$2;
                        int i50 = pikashowProvider$addVideoLinksToCallback$12.I$1;
                        int i51 = pikashowProvider$addVideoLinksToCallback$12.I$0;
                        Function1<? super ExtractorLink, Unit> function116 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$16;
                        Object obj30 = pikashowProvider$addVideoLinksToCallback$12.L$11;
                        it5 = (Iterator) pikashowProvider$addVideoLinksToCallback$12.L$10;
                        list2 = (Iterable) pikashowProvider$addVideoLinksToCallback$12.L$9;
                        Language language6 = (Language) pikashowProvider$addVideoLinksToCallback$12.L$8;
                        Object obj31 = pikashowProvider$addVideoLinksToCallback$12.L$7;
                        it6 = (Iterator) pikashowProvider$addVideoLinksToCallback$12.L$6;
                        obj14 = (Iterable) pikashowProvider$addVideoLinksToCallback$12.L$5;
                        map12 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$4;
                        Map map26 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$3;
                        String str24 = (String) pikashowProvider$addVideoLinksToCallback$12.L$2;
                        Function1<? super ExtractorLink, Unit> function117 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$1;
                        VideoData videoData17 = (VideoData) pikashowProvider$addVideoLinksToCallback$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        i21 = i50;
                        obj18 = obj20;
                        obj15 = coroutine_suspended;
                        map13 = map26;
                        function16 = function117;
                        videoData7 = videoData17;
                        i17 = i51;
                        i20 = i49;
                        function19 = function116;
                        i22 = i46;
                        pikashowProvider$addVideoLinksToCallback$16 = pikashowProvider$addVideoLinksToCallback$12;
                        language = language6;
                        obj17 = obj31;
                        i18 = i47;
                        i19 = i48;
                        str2 = "Unknown";
                        str3 = "m3u8";
                        str8 = str24;
                        pikashowProvider3 = this;
                        obj16 = obj18;
                        function19.invoke(obj16);
                        Unit unit112 = Unit.INSTANCE;
                        map11 = map13;
                        next = obj17;
                        i13 = i20;
                        it3 = it6;
                        obj12 = obj15;
                        pikashowProvider$addVideoLinksToCallback$15 = pikashowProvider$addVideoLinksToCallback$16;
                        i16 = i21;
                        obj13 = obj18;
                        videoData6 = videoData7;
                        map10 = map11;
                        continuation4 = continuation5;
                        pikashowProvider2 = pikashowProvider3;
                        i15 = i18;
                        i14 = i19;
                        it4 = it5;
                        list = list2;
                        obj11 = obj14;
                        map7 = map12;
                        if (!it4.hasNext()) {
                        }
                        break;
                    case 3:
                        int i52 = pikashowProvider$addVideoLinksToCallback$12.I$1;
                        int i53 = pikashowProvider$addVideoLinksToCallback$12.I$0;
                        Map<String, String> map27 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$4;
                        Map map28 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$3;
                        String str25 = (String) pikashowProvider$addVideoLinksToCallback$12.L$2;
                        Function1<? super ExtractorLink, Unit> function118 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$1;
                        VideoData videoData18 = (VideoData) pikashowProvider$addVideoLinksToCallback$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj20);
                            map6 = map27;
                            map4 = map28;
                            parseHDBVPlayerUrl = obj20;
                            str7 = str25;
                            i8 = i52;
                            i7 = i53;
                            function15 = function118;
                            videoData3 = videoData18;
                            str11 = (String) parseHDBVPlayerUrl;
                            if (str11.length() <= 0) {
                            }
                        } catch (Exception e15) {
                            e = e15;
                            i3 = i52;
                            i = i53;
                            map5 = map27;
                            map4 = map28;
                            str6 = str25;
                            function14 = function118;
                            videoData4 = videoData18;
                            obj6 = obj20;
                            obj7 = null;
                        }
                        System.out.println((Object) ("Error parsing HDBV player URL: " + e.getMessage()));
                        pikashowProvider$addVideoLinksToCallback$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData4);
                        pikashowProvider$addVideoLinksToCallback$12.L$1 = SpillingKt.nullOutSpilledVariable(function14);
                        pikashowProvider$addVideoLinksToCallback$12.L$2 = SpillingKt.nullOutSpilledVariable(str6);
                        pikashowProvider$addVideoLinksToCallback$12.L$3 = SpillingKt.nullOutSpilledVariable(map4);
                        pikashowProvider$addVideoLinksToCallback$12.L$4 = SpillingKt.nullOutSpilledVariable(map5);
                        pikashowProvider$addVideoLinksToCallback$12.L$5 = SpillingKt.nullOutSpilledVariable(e);
                        pikashowProvider$addVideoLinksToCallback$12.L$6 = obj7;
                        pikashowProvider$addVideoLinksToCallback$12.L$7 = obj7;
                        pikashowProvider$addVideoLinksToCallback$12.I$0 = i;
                        pikashowProvider$addVideoLinksToCallback$12.I$1 = i3;
                        pikashowProvider$addVideoLinksToCallback$12.label = 6;
                        if (fallbackToDirectUrls(videoData4, function14, str6, map5, pikashowProvider$addVideoLinksToCallback$12) != coroutine_suspended) {
                        }
                        break;
                    case 4:
                        i27 = pikashowProvider$addVideoLinksToCallback$12.I$1;
                        i25 = pikashowProvider$addVideoLinksToCallback$12.I$0;
                        function111 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$7;
                        map16 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$4;
                        map17 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$3;
                        String str26 = (String) pikashowProvider$addVideoLinksToCallback$12.L$2;
                        function110 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$1;
                        VideoData videoData19 = (VideoData) pikashowProvider$addVideoLinksToCallback$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj20);
                            str7 = str26;
                            obj19 = obj20;
                            obj6 = obj19;
                            obj7 = null;
                            videoData3 = videoData19;
                            pikashowProvider$addVideoLinksToCallback$18 = pikashowProvider$addVideoLinksToCallback$12;
                            function111.invoke(obj19);
                        } catch (Exception e16) {
                            e = e16;
                            i3 = i27;
                            i = i25;
                            map5 = map16;
                            map4 = map17;
                            str6 = str26;
                            function14 = function110;
                            videoData4 = videoData19;
                            obj6 = obj20;
                            obj7 = null;
                            break;
                        }
                        Unit unit8222 = Unit.INSTANCE;
                        return Unit.INSTANCE;
                    case 5:
                        int i54 = pikashowProvider$addVideoLinksToCallback$12.I$1;
                        i25 = pikashowProvider$addVideoLinksToCallback$12.I$0;
                        map19 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$4;
                        map18 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$3;
                        str14 = (String) pikashowProvider$addVideoLinksToCallback$12.L$2;
                        function112 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$1;
                        videoData9 = (VideoData) pikashowProvider$addVideoLinksToCallback$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj20);
                            obj6 = obj20;
                            str7 = str14;
                        } catch (Exception e17) {
                            e = e17;
                            i3 = i54;
                            i = i25;
                            map4 = map18;
                            function14 = function112;
                            videoData4 = videoData9;
                            obj6 = obj20;
                            obj7 = null;
                            map5 = map19;
                            str6 = str14;
                            break;
                        }
                        Unit unit82222 = Unit.INSTANCE;
                        return Unit.INSTANCE;
                    case 6:
                        i29 = pikashowProvider$addVideoLinksToCallback$12.I$1;
                        i28 = pikashowProvider$addVideoLinksToCallback$12.I$0;
                        map20 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$4;
                        map21 = (Map) pikashowProvider$addVideoLinksToCallback$12.L$3;
                        str15 = (String) pikashowProvider$addVideoLinksToCallback$12.L$2;
                        function113 = (Function1) pikashowProvider$addVideoLinksToCallback$12.L$1;
                        videoData10 = (VideoData) pikashowProvider$addVideoLinksToCallback$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        obj6 = obj20;
                        i25 = i28;
                        str7 = str15;
                        Unit unit822222 = Unit.INSTANCE;
                        return Unit.INSTANCE;
                    case 7:
                        int i55 = pikashowProvider$addVideoLinksToCallback$12.I$1;
                        int i56 = pikashowProvider$addVideoLinksToCallback$12.I$0;
                        ResultKt.throwOnFailure(obj20);
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        pikashowProvider$addVideoLinksToCallback$1 = new PikashowProvider$addVideoLinksToCallback$1(this, continuation5);
        pikashowProvider$addVideoLinksToCallback$12 = pikashowProvider$addVideoLinksToCallback$1;
        Object obj202 = pikashowProvider$addVideoLinksToCallback$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        String str162 = "m3u8";
        switch (pikashowProvider$addVideoLinksToCallback$12.label) {
        }
    }

    private final String extractImdbIdFromUrl(String url) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = url.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            return StringsKt.take(ArraysKt.joinToString$default(messageDigest.digest(bytes), "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new PikashowProvider$$ExternalSyntheticLambda6(), 30, (Object) null), 10);
        } catch (Exception e) {
            return "default";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence extractImdbIdFromUrl$lambda$0(byte b) {
        String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0028. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0039  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0066  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00a7  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x016f  */
    /* JADX WARN: Removed duplicated region for block: B:43:0x01d1 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:44:0x01d2  */
    /* JADX WARN: Removed duplicated region for block: B:47:0x00af  */
    /* JADX WARN: Removed duplicated region for block: B:55:0x00d1  */
    /* JADX WARN: Removed duplicated region for block: B:56:0x00d8  */
    /* JADX WARN: Removed duplicated region for block: B:64:0x00fa  */
    /* JADX WARN: Removed duplicated region for block: B:65:0x0101  */
    /* JADX WARN: Removed duplicated region for block: B:73:0x0123  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x012a  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object fallbackToDirectUrls(VideoData videoData, Function1<? super ExtractorLink, Unit> function1, String str, Map<String, String> map, Continuation<? super Unit> continuation) {
        PikashowProvider$fallbackToDirectUrls$1 pikashowProvider$fallbackToDirectUrls$1;
        PikashowProvider pikashowProvider;
        PikashowProvider$fallbackToDirectUrls$1 pikashowProvider$fallbackToDirectUrls$12;
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        int value;
        String host;
        Function1<? super ExtractorLink, Unit> function12;
        Object newExtractorLink;
        Object obj;
        Function1<? super ExtractorLink, Unit> function13;
        if (continuation instanceof PikashowProvider$fallbackToDirectUrls$1) {
            pikashowProvider$fallbackToDirectUrls$1 = (PikashowProvider$fallbackToDirectUrls$1) continuation;
            if ((pikashowProvider$fallbackToDirectUrls$1.label & Integer.MIN_VALUE) != 0) {
                pikashowProvider$fallbackToDirectUrls$1.label -= Integer.MIN_VALUE;
                pikashowProvider = this;
                pikashowProvider$fallbackToDirectUrls$12 = pikashowProvider$fallbackToDirectUrls$1;
                Object obj2 = pikashowProvider$fallbackToDirectUrls$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (pikashowProvider$fallbackToDirectUrls$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj2);
                        String playUrl = videoData.getPlayUrl();
                        if (playUrl == null && (playUrl = videoData.getVideoUrl()) == null) {
                            playUrl = videoData.getUrl();
                        }
                        String str2 = playUrl;
                        if (str2 == null) {
                            return Unit.INSTANCE;
                        }
                        String quality = videoData.getQuality();
                        if (quality != null) {
                            String lowerCase = quality.toLowerCase(Locale.ROOT);
                            Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
                            if (lowerCase != null && StringsKt.contains$default(lowerCase, "hd", false, 2, (Object) null)) {
                                z = true;
                                if (z) {
                                    String quality2 = videoData.getQuality();
                                    if (quality2 != null) {
                                        String lowerCase2 = quality2.toLowerCase(Locale.ROOT);
                                        Intrinsics.checkNotNullExpressionValue(lowerCase2, "toLowerCase(...)");
                                        if (lowerCase2 != null && StringsKt.contains$default(lowerCase2, "1080", false, 2, (Object) null)) {
                                            z2 = true;
                                            if (z2) {
                                                String quality3 = videoData.getQuality();
                                                if (quality3 != null) {
                                                    String lowerCase3 = quality3.toLowerCase(Locale.ROOT);
                                                    Intrinsics.checkNotNullExpressionValue(lowerCase3, "toLowerCase(...)");
                                                    if (lowerCase3 != null && StringsKt.contains$default(lowerCase3, "720", false, 2, (Object) null)) {
                                                        z3 = true;
                                                        if (z3) {
                                                            String quality4 = videoData.getQuality();
                                                            if (quality4 != null) {
                                                                String lowerCase4 = quality4.toLowerCase(Locale.ROOT);
                                                                Intrinsics.checkNotNullExpressionValue(lowerCase4, "toLowerCase(...)");
                                                                if (lowerCase4 != null && StringsKt.contains$default(lowerCase4, "480", false, 2, (Object) null)) {
                                                                    z4 = true;
                                                                    value = !z4 ? Qualities.P480.getValue() : Qualities.Unknown.getValue();
                                                                }
                                                            }
                                                            z4 = false;
                                                            if (!z4) {
                                                            }
                                                        } else {
                                                            value = Qualities.P720.getValue();
                                                        }
                                                    }
                                                }
                                                z3 = false;
                                                if (z3) {
                                                }
                                            } else {
                                                value = Qualities.P1080.getValue();
                                            }
                                        }
                                    }
                                    z2 = false;
                                    if (z2) {
                                    }
                                } else {
                                    value = Qualities.P720.getValue();
                                }
                                int i = value;
                                ExtractorLinkType extractorLinkType = (!StringsKt.contains$default(str2, "m3u8", false, 2, (Object) null) || Intrinsics.areEqual(videoData.getSourceType(), "hls")) ? ExtractorLinkType.M3U8 : Intrinsics.areEqual(videoData.getSourceType(), "direct") ? ExtractorLinkType.VIDEO : ExtractorLinkType.VIDEO;
                                String name = pikashowProvider.getName();
                                host = videoData.getHost();
                                if (host == null) {
                                    host = "Direct";
                                }
                                PikashowProvider$fallbackToDirectUrls$2$1 pikashowProvider$fallbackToDirectUrls$2$1 = new PikashowProvider$fallbackToDirectUrls$2$1(i, map, null);
                                pikashowProvider$fallbackToDirectUrls$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData);
                                pikashowProvider$fallbackToDirectUrls$12.L$1 = SpillingKt.nullOutSpilledVariable(function1);
                                pikashowProvider$fallbackToDirectUrls$12.L$2 = SpillingKt.nullOutSpilledVariable(str);
                                pikashowProvider$fallbackToDirectUrls$12.L$3 = SpillingKt.nullOutSpilledVariable(map);
                                pikashowProvider$fallbackToDirectUrls$12.L$4 = SpillingKt.nullOutSpilledVariable(str2);
                                pikashowProvider$fallbackToDirectUrls$12.L$5 = SpillingKt.nullOutSpilledVariable(str2);
                                pikashowProvider$fallbackToDirectUrls$12.L$6 = SpillingKt.nullOutSpilledVariable(extractorLinkType);
                                function12 = function1;
                                pikashowProvider$fallbackToDirectUrls$12.L$7 = function12;
                                pikashowProvider$fallbackToDirectUrls$12.I$0 = 0;
                                pikashowProvider$fallbackToDirectUrls$12.I$1 = i;
                                pikashowProvider$fallbackToDirectUrls$12.label = 1;
                                newExtractorLink = ExtractorApiKt.newExtractorLink(name, str + " - " + host, str2, extractorLinkType, pikashowProvider$fallbackToDirectUrls$2$1, pikashowProvider$fallbackToDirectUrls$12);
                                if (newExtractorLink != coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                obj = newExtractorLink;
                                function13 = function12;
                                function12.invoke(obj);
                                return Unit.INSTANCE;
                            }
                        }
                        z = false;
                        if (z) {
                        }
                        int i2 = value;
                        if (StringsKt.contains$default(str2, "m3u8", false, 2, (Object) null)) {
                        }
                        String name2 = pikashowProvider.getName();
                        host = videoData.getHost();
                        if (host == null) {
                        }
                        PikashowProvider$fallbackToDirectUrls$2$1 pikashowProvider$fallbackToDirectUrls$2$12 = new PikashowProvider$fallbackToDirectUrls$2$1(i2, map, null);
                        pikashowProvider$fallbackToDirectUrls$12.L$0 = SpillingKt.nullOutSpilledVariable(videoData);
                        pikashowProvider$fallbackToDirectUrls$12.L$1 = SpillingKt.nullOutSpilledVariable(function1);
                        pikashowProvider$fallbackToDirectUrls$12.L$2 = SpillingKt.nullOutSpilledVariable(str);
                        pikashowProvider$fallbackToDirectUrls$12.L$3 = SpillingKt.nullOutSpilledVariable(map);
                        pikashowProvider$fallbackToDirectUrls$12.L$4 = SpillingKt.nullOutSpilledVariable(str2);
                        pikashowProvider$fallbackToDirectUrls$12.L$5 = SpillingKt.nullOutSpilledVariable(str2);
                        pikashowProvider$fallbackToDirectUrls$12.L$6 = SpillingKt.nullOutSpilledVariable(extractorLinkType);
                        function12 = function1;
                        pikashowProvider$fallbackToDirectUrls$12.L$7 = function12;
                        pikashowProvider$fallbackToDirectUrls$12.I$0 = 0;
                        pikashowProvider$fallbackToDirectUrls$12.I$1 = i2;
                        pikashowProvider$fallbackToDirectUrls$12.label = 1;
                        newExtractorLink = ExtractorApiKt.newExtractorLink(name2, str + " - " + host, str2, extractorLinkType, pikashowProvider$fallbackToDirectUrls$2$12, pikashowProvider$fallbackToDirectUrls$12);
                        if (newExtractorLink != coroutine_suspended) {
                        }
                        break;
                    case 1:
                        int i3 = pikashowProvider$fallbackToDirectUrls$12.I$1;
                        int i4 = pikashowProvider$fallbackToDirectUrls$12.I$0;
                        Function1<? super ExtractorLink, Unit> function14 = (Function1) pikashowProvider$fallbackToDirectUrls$12.L$7;
                        Function1<? super ExtractorLink, Unit> function15 = (Function1) pikashowProvider$fallbackToDirectUrls$12.L$1;
                        ResultKt.throwOnFailure(obj2);
                        function13 = function15;
                        obj = obj2;
                        function12 = function14;
                        function12.invoke(obj);
                        return Unit.INSTANCE;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        pikashowProvider = this;
        pikashowProvider$fallbackToDirectUrls$1 = new PikashowProvider$fallbackToDirectUrls$1(pikashowProvider, continuation);
        pikashowProvider$fallbackToDirectUrls$12 = pikashowProvider$fallbackToDirectUrls$1;
        Object obj22 = pikashowProvider$fallbackToDirectUrls$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (pikashowProvider$fallbackToDirectUrls$12.label) {
        }
    }

    private final Map<String, String> generateSignature(Long timestampMs) {
        String valueOf = String.valueOf((timestampMs != null ? timestampMs.longValue() : System.currentTimeMillis()) / 1000);
        String str = this.apiKey + ":" + valueOf;
        Mac mac = Mac.getInstance("HmacSHA256");
        byte[] bytes = this.hmacSecret.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
        mac.init(new SecretKeySpec(bytes, "HmacSHA256"));
        byte[] bytes2 = str.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes2, "getBytes(...)");
        return MapsKt.mapOf(new Pair[]{TuplesKt.to("X-Timestamp", valueOf), TuplesKt.to("X-API-Key", this.apiKey), TuplesKt.to("X-Signature", ArraysKt.joinToString$default(mac.doFinal(bytes2), "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new PikashowProvider$$ExternalSyntheticLambda7(), 30, (Object) null))});
    }

    static /* synthetic */ Map generateSignature$default(PikashowProvider pikashowProvider, Long l, int i, Object obj) {
        if ((i & 1) != 0) {
            l = null;
        }
        return pikashowProvider.generateSignature(l);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence generateSignature$lambda$0(byte b) {
        String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$0$0$0$0(PikashowSeries pikashowSeries, TvSeriesSearchResponse tvSeriesSearchResponse) {
        tvSeriesSearchResponse.setPosterUrl(pikashowSeries.getCover());
        tvSeriesSearchResponse.setYear(pikashowSeries.getYear());
        tvSeriesSearchResponse.setQuality(SearchQuality.HD);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$0$1$0$0(PikashowMovie pikashowMovie, PikashowProvider pikashowProvider, MovieSearchResponse movieSearchResponse) {
        movieSearchResponse.setPosterUrl(pikashowMovie.getCover());
        movieSearchResponse.setYear(pikashowMovie.getYear());
        movieSearchResponse.setQuality(pikashowProvider.getQualityFromString(pikashowMovie.getQuality()));
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getMainPage$lambda$1(MovieSearchResponse movieSearchResponse) {
        movieSearchResponse.setPosterUrl((String) null);
        return Unit.INSTANCE;
    }

    private final Map<String, String> getPikashowHeaders() {
        Map generateSignature$default = generateSignature$default(this, null, 1, null);
        String str = this.gaid;
        String str2 = this.deviceUuid;
        Object obj = generateSignature$default.get("X-API-Key");
        Intrinsics.checkNotNull(obj);
        Object obj2 = generateSignature$default.get("X-Signature");
        Intrinsics.checkNotNull(obj2);
        Object obj3 = generateSignature$default.get("X-Timestamp");
        Intrinsics.checkNotNull(obj3);
        return MapsKt.mapOf(new Pair[]{TuplesKt.to("Host", "manoda.co"), TuplesKt.to("user-agent", "Pikashow/2509030 (Android 13; Pixel 5; Channel/pikashow; gaid/" + str + "); Uuid/" + str2), TuplesKt.to("X-API-Key", obj), TuplesKt.to("X-Signature", obj2), TuplesKt.to("X-Timestamp", obj3)});
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

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x0029, code lost:
    
        if (r0.equals("1080P") == false) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:?, code lost:
    
        return com.lagradost.cloudstream3.utils.Qualities.P1080.getValue();
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0032, code lost:
    
        if (r0.equals("720P") == false) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:?, code lost:
    
        return com.lagradost.cloudstream3.utils.Qualities.P720.getValue();
     */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x003b, code lost:
    
        if (r0.equals("480P") == false) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:?, code lost:
    
        return com.lagradost.cloudstream3.utils.Qualities.P480.getValue();
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x0044, code lost:
    
        if (r0.equals("FHD") == false) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x0054, code lost:
    
        if (r0.equals("SD") == false) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x0064, code lost:
    
        if (r0.equals("HD") == false) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x0074, code lost:
    
        if (r0.equals("4K") == false) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:7:0x0020, code lost:
    
        if (r0.equals("2160P") == false) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:?, code lost:
    
        return com.lagradost.cloudstream3.utils.Qualities.P2160.getValue();
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:5:0x0015. Please report as an issue. */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private final int getQualityValue(String qualityString) {
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
    public final int getQualityValueFromLabel(String label) {
        String str;
        if (label != null) {
            str = label.toLowerCase(Locale.ROOT);
            Intrinsics.checkNotNullExpressionValue(str, "toLowerCase(...)");
        } else {
            str = null;
        }
        if (str != null) {
            switch (str.hashCode()) {
                case 1572835:
                    if (str.equals("360p")) {
                        return Qualities.P360.getValue();
                    }
                    break;
                case 1604548:
                    if (str.equals("480p")) {
                        return Qualities.P480.getValue();
                    }
                    break;
                case 1688155:
                    if (str.equals("720p")) {
                        return Qualities.P720.getValue();
                    }
                    break;
                case 46737913:
                    if (str.equals("1080p")) {
                        return Qualities.P1080.getValue();
                    }
                    break;
                case 1544803905:
                    if (str.equals("default")) {
                        return Qualities.P720.getValue();
                    }
                    break;
            }
        }
        return Qualities.Unknown.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit load$lambda$1$0$0(int i, int i2, Episode episode) {
        episode.setName("Episode " + i);
        episode.setSeason(Integer.valueOf(i2));
        episode.setEpisode(Integer.valueOf(i));
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x004a. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0055  */
    /* JADX WARN: Removed duplicated region for block: B:132:0x0162  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0688 A[Catch: Exception -> 0x0699, TRY_LEAVE, TryCatch #6 {Exception -> 0x0699, blocks: (B:15:0x067e, B:17:0x0688, B:71:0x065d, B:117:0x0477), top: B:116:0x0477 }] */
    /* JADX WARN: Removed duplicated region for block: B:23:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:27:0x00cd  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0491 A[Catch: Exception -> 0x06ac, TRY_LEAVE, TryCatch #4 {Exception -> 0x06ac, blocks: (B:32:0x0487, B:34:0x0491, B:37:0x04b1, B:54:0x054e, B:57:0x0557, B:59:0x0563, B:61:0x0569, B:63:0x0571, B:65:0x0577, B:69:0x0590, B:36:0x04ad), top: B:31:0x0487 }] */
    /* JADX WARN: Removed duplicated region for block: B:79:0x06a5  */
    /* JADX WARN: Removed duplicated region for block: B:85:0x0136  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x004d  */
    /* JADX WARN: Removed duplicated region for block: B:92:0x0292 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:93:0x0293 A[Catch: Exception -> 0x06c1, TryCatch #10 {Exception -> 0x06c1, blocks: (B:90:0x0288, B:93:0x0293, B:96:0x02aa, B:98:0x02cd, B:111:0x0345), top: B:89:0x0288 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object parseHDBVPlayerUrl(String str, Continuation<? super String> continuation) {
        PikashowProvider$parseHDBVPlayerUrl$1 pikashowProvider$parseHDBVPlayerUrl$1;
        String str2;
        String str3;
        Object obj;
        Object obj2;
        Object obj3;
        Object obj4;
        Object obj5;
        Object obj6;
        Object obj7;
        String str4;
        int i;
        Object obj8;
        PikashowProvider$parseHDBVPlayerUrl$1 pikashowProvider$parseHDBVPlayerUrl$12;
        Object obj9;
        String str5;
        NiceResponse niceResponse;
        String element;
        MatchResult find$default;
        Keys keys;
        String str6;
        Map map;
        Elements elements;
        Regex regex;
        Document document;
        String str7;
        PikashowProvider$parseHDBVPlayerUrl$1 pikashowProvider$parseHDBVPlayerUrl$13;
        String str8;
        String str9;
        String str10;
        NiceResponse niceResponse2;
        String text;
        List<FileData> folder;
        FileData fileData;
        String file;
        PikashowProvider pikashowProvider = this;
        if (continuation instanceof PikashowProvider$parseHDBVPlayerUrl$1) {
            pikashowProvider$parseHDBVPlayerUrl$1 = (PikashowProvider$parseHDBVPlayerUrl$1) continuation;
            if ((pikashowProvider$parseHDBVPlayerUrl$1.label & Integer.MIN_VALUE) != 0) {
                pikashowProvider$parseHDBVPlayerUrl$1.label -= Integer.MIN_VALUE;
                Object obj10 = pikashowProvider$parseHDBVPlayerUrl$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (pikashowProvider$parseHDBVPlayerUrl$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj10);
                        try {
                            Requests app = MainActivityKt.getApp();
                            Map mapOf = MapsKt.mapOf(new Pair[]{TuplesKt.to("Accept", "*/*"), TuplesKt.to("Accept-Encoding", "gzip, deflate, br"), TuplesKt.to("Accept-Language", "en-CA,en;q=0.9;q=0.8;q=0.7,en-US;q=0.6"), TuplesKt.to("Connection", "keep-alive"), TuplesKt.to("Host", "samui390dod.com"), TuplesKt.to("Icy-MetaData", "1"), TuplesKt.to("Origin", "https://samui390dod.com"), TuplesKt.to("Referer", "https://samui390dod.com/"), TuplesKt.to("sec-ch-ua", "\"Chromium\";v=\"136\", \"Android WebView\";v=\"136\", \"Not.A/Brand\";v=\"99\""), TuplesKt.to("sec-ch-ua-mobile", "?1"), TuplesKt.to("sec-ch-ua-platform", "\"Android\""), TuplesKt.to("Sec-Fetch-Dest", "video"), TuplesKt.to("sec-fetch-mode", "cors"), TuplesKt.to("Sec-Fetch-Site", "cross-site"), TuplesKt.to("sec-fetch-user", "?1"), TuplesKt.to("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1"), TuplesKt.to("X-Requested-With", "com.offshore.pikachu")});
                            pikashowProvider$parseHDBVPlayerUrl$1.L$0 = str;
                            pikashowProvider$parseHDBVPlayerUrl$1.label = 1;
                            str3 = "/";
                            PikashowProvider$parseHDBVPlayerUrl$1 pikashowProvider$parseHDBVPlayerUrl$14 = pikashowProvider$parseHDBVPlayerUrl$1;
                            obj = coroutine_suspended;
                            obj2 = "Origin";
                            obj3 = "Accept-Language";
                            obj4 = "gzip, deflate, br";
                            obj5 = "Accept-Encoding";
                            obj6 = "*/*";
                            obj7 = "Accept";
                            str4 = "Deserialized value did not match the specified type; specified ";
                            i = 200;
                            try {
                                obj8 = Requests.get$default(app, str, mapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, pikashowProvider$parseHDBVPlayerUrl$14, 4092, (Object) null);
                                pikashowProvider$parseHDBVPlayerUrl$12 = pikashowProvider$parseHDBVPlayerUrl$14;
                            } catch (Exception e) {
                                e = e;
                                str2 = str;
                            }
                        } catch (Exception e2) {
                            e = e2;
                            str2 = str;
                        }
                        if (obj8 == obj) {
                            return obj;
                        }
                        obj9 = obj8;
                        str5 = str;
                        try {
                            niceResponse = (NiceResponse) obj9;
                        } catch (Exception e3) {
                            e = e3;
                            str2 = str5;
                        }
                        if (niceResponse.getCode() == i) {
                            return "";
                        }
                        Document parse = Jsoup.parse(niceResponse.getText());
                        Elements elementsByTag = parse.getElementsByTag("script");
                        if (elementsByTag.size() < 8) {
                            return "";
                        }
                        element = ((Element) elementsByTag.get(7)).toString();
                        Regex regex2 = new Regex("HDVBPlayer\\((.*?)\\);");
                        find$default = Regex.find$default(regex2, element, 0, 2, (Object) null);
                        if (find$default == null) {
                            return "";
                        }
                        String str11 = (String) find$default.getGroupValues().get(1);
                        Object readValue = pikashowProvider.mapper.readValue(str11, new PikashowProvider$parseHDBVPlayerUrl$$inlined$readValue$1());
                        if (readValue instanceof Keys) {
                            keys = (Keys) readValue;
                            String str12 = str3;
                            String str13 = StringsKt.substringBefore(str5, str12, "https://") + "://" + StringsKt.substringBefore$default(StringsKt.substringAfter$default(str5, "://", (String) null, 2, (Object) null), str12, (String) null, 2, (Object) null) + str12;
                            String str14 = str13 + keys.getFile();
                            Map mapOf2 = MapsKt.mapOf(new Pair[]{TuplesKt.to(obj7, obj6), TuplesKt.to(obj5, obj4), TuplesKt.to(obj3, "en-US,en;q=0.9"), TuplesKt.to("Content-Length", "0"), TuplesKt.to("Content-Type", "application/x-www-form-urlencoded"), TuplesKt.to(obj2, str13), TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36"), TuplesKt.to("X-Csrf-Token", keys.getKey())});
                            str6 = str5;
                            String str15 = str5;
                            try {
                                Requests app2 = MainActivityKt.getApp();
                                pikashowProvider$parseHDBVPlayerUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(parse);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(elementsByTag);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$4 = SpillingKt.nullOutSpilledVariable(element);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(regex2);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(find$default);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(str11);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(keys);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$9 = str13;
                                pikashowProvider$parseHDBVPlayerUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str14);
                                pikashowProvider$parseHDBVPlayerUrl$12.L$11 = mapOf2;
                                pikashowProvider$parseHDBVPlayerUrl$12.L$12 = str6;
                                pikashowProvider$parseHDBVPlayerUrl$12.label = 2;
                                PikashowProvider$parseHDBVPlayerUrl$1 pikashowProvider$parseHDBVPlayerUrl$15 = pikashowProvider$parseHDBVPlayerUrl$12;
                                map = mapOf2;
                                elements = elementsByTag;
                                str2 = str15;
                                regex = regex2;
                                document = parse;
                                str7 = null;
                                try {
                                    obj10 = Requests.post$default(app2, str14, map, str6, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, (RequestBody) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, pikashowProvider$parseHDBVPlayerUrl$15, 65528, (Object) null);
                                    pikashowProvider$parseHDBVPlayerUrl$13 = pikashowProvider$parseHDBVPlayerUrl$15;
                                } catch (Exception e4) {
                                    e = e4;
                                }
                            } catch (Exception e5) {
                                e = e5;
                                str2 = str15;
                            }
                            if (obj10 == obj) {
                                return obj;
                            }
                            str8 = str13;
                            str9 = str11;
                            str10 = str14;
                            try {
                                niceResponse2 = (NiceResponse) obj10;
                            } catch (Exception e6) {
                                e = e6;
                            }
                            if (niceResponse2.getCode() == i) {
                                return "";
                            }
                            if (Intrinsics.areEqual(niceResponse2.getHeaders().get("Content-Encoding"), "gzip")) {
                                try {
                                    text = niceResponse2.getText();
                                } catch (Exception e7) {
                                    e = e7;
                                }
                            } else {
                                text = niceResponse2.getText();
                            }
                            JSONArray jSONArray = new JSONArray(text);
                            ArrayList arrayList = new ArrayList();
                            int i2 = 0;
                            int length = jSONArray.length();
                            while (i2 < length) {
                                Object readValue2 = pikashowProvider.mapper.readValue(StringsKt.replace$default(jSONArray.getJSONObject(i2).toString(), "[]", "", false, 4, (Object) null), new PikashowProvider$parseHDBVPlayerUrl$$inlined$readValue$2());
                                if (!(readValue2 instanceof Season)) {
                                    throw new RuntimeJsonMappingException(str4 + Reflection.getOrCreateKotlinClass(Season.class).getQualifiedName() + "(non-null) but was " + (readValue2 != null ? Reflection.getOrCreateKotlinClass(readValue2.getClass()).getQualifiedName() : str7));
                                }
                                arrayList.add((Season) readValue2);
                                i2++;
                                pikashowProvider = this;
                            }
                            Season season = (Season) CollectionsKt.firstOrNull(arrayList);
                            if (season == null) {
                                return "";
                            }
                            HDBVEpisode hDBVEpisode = (HDBVEpisode) CollectionsKt.firstOrNull(season.getFolder());
                            if (hDBVEpisode != null && (folder = hDBVEpisode.getFolder()) != null && (fileData = (FileData) CollectionsKt.firstOrNull(folder)) != null && (file = fileData.getFile()) != null) {
                                String replace$default = StringsKt.replace$default(file, "~", "", false, 4, (Object) null);
                                if (replace$default != null) {
                                    String str16 = text;
                                    Requests app3 = MainActivityKt.getApp();
                                    String str17 = str8 + "playlist/" + replace$default + ".txt";
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(str2);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$2 = SpillingKt.nullOutSpilledVariable(document);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$3 = SpillingKt.nullOutSpilledVariable(elements);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$4 = SpillingKt.nullOutSpilledVariable(element);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(regex);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$6 = SpillingKt.nullOutSpilledVariable(find$default);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(str9);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(keys);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$9 = SpillingKt.nullOutSpilledVariable(str8);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$10 = SpillingKt.nullOutSpilledVariable(str10);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$11 = SpillingKt.nullOutSpilledVariable(map);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$12 = SpillingKt.nullOutSpilledVariable(str6);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$13 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$14 = SpillingKt.nullOutSpilledVariable(str16);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$15 = SpillingKt.nullOutSpilledVariable(jSONArray);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$16 = SpillingKt.nullOutSpilledVariable(arrayList);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$17 = SpillingKt.nullOutSpilledVariable(season);
                                    pikashowProvider$parseHDBVPlayerUrl$13.L$18 = SpillingKt.nullOutSpilledVariable(replace$default);
                                    pikashowProvider$parseHDBVPlayerUrl$13.label = 3;
                                    obj10 = Requests.post$default(app3, str17, map, str6, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, (RequestBody) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, pikashowProvider$parseHDBVPlayerUrl$13, 65528, (Object) null);
                                    if (obj10 == obj) {
                                        return obj;
                                    }
                                    NiceResponse niceResponse3 = (NiceResponse) obj10;
                                    return niceResponse3.getCode() != 200 ? StringsKt.trim(niceResponse3.getText()).toString() : "";
                                }
                            }
                            return "";
                        }
                        try {
                            throw new RuntimeJsonMappingException(str4 + Reflection.getOrCreateKotlinClass(Keys.class).getQualifiedName() + "(non-null) but was " + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : null));
                        } catch (Exception e8) {
                            e = e8;
                            str2 = str5;
                        }
                        System.out.println((Object) ("Error parsing HDBV player URL: " + e.getMessage()));
                        return "";
                    case 1:
                        String str18 = (String) pikashowProvider$parseHDBVPlayerUrl$1.L$0;
                        try {
                            ResultKt.throwOnFailure(obj10);
                            pikashowProvider$parseHDBVPlayerUrl$12 = pikashowProvider$parseHDBVPlayerUrl$1;
                            obj = coroutine_suspended;
                            str3 = "/";
                            obj2 = "Origin";
                            obj3 = "Accept-Language";
                            obj4 = "gzip, deflate, br";
                            obj5 = "Accept-Encoding";
                            obj6 = "*/*";
                            obj7 = "Accept";
                            str4 = "Deserialized value did not match the specified type; specified ";
                            i = 200;
                            str5 = str18;
                            obj9 = obj10;
                            niceResponse = (NiceResponse) obj9;
                            if (niceResponse.getCode() == i) {
                            }
                        } catch (Exception e9) {
                            e = e9;
                            str2 = str18;
                            break;
                        }
                        break;
                    case 2:
                        String str19 = (String) pikashowProvider$parseHDBVPlayerUrl$1.L$12;
                        map = (Map) pikashowProvider$parseHDBVPlayerUrl$1.L$11;
                        String str20 = (String) pikashowProvider$parseHDBVPlayerUrl$1.L$10;
                        str8 = (String) pikashowProvider$parseHDBVPlayerUrl$1.L$9;
                        Keys keys2 = (Keys) pikashowProvider$parseHDBVPlayerUrl$1.L$8;
                        String str21 = (String) pikashowProvider$parseHDBVPlayerUrl$1.L$7;
                        MatchResult matchResult = (MatchResult) pikashowProvider$parseHDBVPlayerUrl$1.L$6;
                        Regex regex3 = (Regex) pikashowProvider$parseHDBVPlayerUrl$1.L$5;
                        String str22 = (String) pikashowProvider$parseHDBVPlayerUrl$1.L$4;
                        Elements elements2 = (Elements) pikashowProvider$parseHDBVPlayerUrl$1.L$3;
                        Document document2 = (Document) pikashowProvider$parseHDBVPlayerUrl$1.L$2;
                        NiceResponse niceResponse4 = (NiceResponse) pikashowProvider$parseHDBVPlayerUrl$1.L$1;
                        String str23 = (String) pikashowProvider$parseHDBVPlayerUrl$1.L$0;
                        try {
                            ResultKt.throwOnFailure(obj10);
                            pikashowProvider$parseHDBVPlayerUrl$13 = pikashowProvider$parseHDBVPlayerUrl$1;
                            obj = coroutine_suspended;
                            str10 = str20;
                            keys = keys2;
                            str9 = str21;
                            find$default = matchResult;
                            regex = regex3;
                            element = str22;
                            document = document2;
                            elements = elements2;
                            str4 = "Deserialized value did not match the specified type; specified ";
                            str6 = str19;
                            niceResponse = niceResponse4;
                            str2 = str23;
                            i = 200;
                            str7 = null;
                            niceResponse2 = (NiceResponse) obj10;
                            if (niceResponse2.getCode() == i) {
                            }
                        } catch (Exception e10) {
                            e = e10;
                            str2 = str23;
                            break;
                        }
                        break;
                    case 3:
                        String str24 = (String) pikashowProvider$parseHDBVPlayerUrl$1.L$0;
                        try {
                            ResultKt.throwOnFailure(obj10);
                            str2 = str24;
                            NiceResponse niceResponse32 = (NiceResponse) obj10;
                            if (niceResponse32.getCode() != 200) {
                            }
                        } catch (Exception e11) {
                            e = e11;
                            str2 = str24;
                            break;
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        pikashowProvider$parseHDBVPlayerUrl$1 = new PikashowProvider$parseHDBVPlayerUrl$1(pikashowProvider, continuation);
        Object obj102 = pikashowProvider$parseHDBVPlayerUrl$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (pikashowProvider$parseHDBVPlayerUrl$1.label) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit search$lambda$0$0$0$0(PikashowSeries pikashowSeries, TvSeriesSearchResponse tvSeriesSearchResponse) {
        tvSeriesSearchResponse.setPosterUrl(pikashowSeries.getCover());
        tvSeriesSearchResponse.setYear(pikashowSeries.getYear());
        tvSeriesSearchResponse.setQuality(SearchQuality.HD);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit search$lambda$0$1$0$0(PikashowMovie pikashowMovie, PikashowProvider pikashowProvider, MovieSearchResponse movieSearchResponse) {
        movieSearchResponse.setPosterUrl(pikashowMovie.getCover());
        movieSearchResponse.setYear(pikashowMovie.getYear());
        movieSearchResponse.setQuality(pikashowProvider.getQualityFromString(pikashowMovie.getQuality()));
        return Unit.INSTANCE;
    }

    public boolean getHasMainPage() {
        return this.hasMainPage;
    }

    @NotNull
    public String getLang() {
        return this.lang;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(13:194|195|187|188|189|184|91|(0)|20|21|22|23|(0)(0)) */
    /* JADX WARN: Can't wrap try/catch for region: R(21:25|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46) */
    /* JADX WARN: Can't wrap try/catch for region: R(23:25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|(1:48)(10:49|15|16|17|(4:84|85|86|87)(1:19)|20|21|22|23|(3:80|60|61)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(6:102|103|104|105|106|(6:115|(8:117|(5:120|(1:122)(1:128)|(2:124|125)(1:127)|126|118)|129|130|(1:132)|134|91|(0))(1:135)|133|134|91|(0))(4:108|(1:110)(1:114)|111|112)) */
    /* JADX WARN: Code restructure failed: missing block: B:136:0x04b6, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:137:0x04bb, code lost:
    
        java.lang.System.out.println((java.lang.Object) ("Error parsing series response: " + r0.getMessage()));
        r0 = kotlin.collections.CollectionsKt.emptyList();
     */
    /* JADX WARN: Code restructure failed: missing block: B:147:0x0205, code lost:
    
        if (r10.equals(r7) == false) goto L54;
     */
    /* JADX WARN: Code restructure failed: missing block: B:148:0x022b, code lost:
    
        r31 = r6;
     */
    /* JADX WARN: Code restructure failed: missing block: B:151:0x023f, code lost:
    
        r32 = r7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:156:0x0249, code lost:
    
        r7 = r1.mapper.readValue(r4.getText(), new com.cncverse.PikashowProvider$getMainPage$lambda$0$$inlined$readValue$2());
     */
    /* JADX WARN: Code restructure failed: missing block: B:157:0x0254, code lost:
    
        if ((r7 instanceof com.cncverse.PikashowProvider.PikashowMovieResponse) != false) goto L68;
     */
    /* JADX WARN: Code restructure failed: missing block: B:158:0x0256, code lost:
    
        r3 = new java.lang.StringBuilder().append("Deserialized value did not match the specified type; specified ").append(kotlin.jvm.internal.Reflection.getOrCreateKotlinClass(com.cncverse.PikashowProvider.PikashowMovieResponse.class).getQualifiedName()).append("(non-null)").append(" but was ");
     */
    /* JADX WARN: Code restructure failed: missing block: B:159:0x027a, code lost:
    
        if (r7 == null) goto L65;
     */
    /* JADX WARN: Code restructure failed: missing block: B:160:0x027c, code lost:
    
        r6 = kotlin.jvm.internal.Reflection.getOrCreateKotlinClass(r7.getClass()).getQualifiedName();
     */
    /* JADX WARN: Code restructure failed: missing block: B:162:0x0299, code lost:
    
        throw new com.fasterxml.jackson.databind.RuntimeJsonMappingException(r3.append(r6).toString());
     */
    /* JADX WARN: Code restructure failed: missing block: B:164:0x028b, code lost:
    
        r6 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:165:0x029a, code lost:
    
        r4 = (com.cncverse.PikashowProvider.PikashowMovieResponse) r7;
        r5 = r4.getRecords();
     */
    /* JADX WARN: Code restructure failed: missing block: B:166:0x02a5, code lost:
    
        if (r5 == null) goto L84;
     */
    /* JADX WARN: Code restructure failed: missing block: B:167:0x02a7, code lost:
    
        r5 = r5;
        r7 = new java.util.ArrayList();
        r28 = r5.iterator();
     */
    /* JADX WARN: Code restructure failed: missing block: B:169:0x02c0, code lost:
    
        if (r28.hasNext() == false) goto L233;
     */
    /* JADX WARN: Code restructure failed: missing block: B:170:0x02c2, code lost:
    
        r36 = (com.cncverse.PikashowProvider.PikashowMovie) r28.next();
        r37 = r36.getTitle();
     */
    /* JADX WARN: Code restructure failed: missing block: B:171:0x02d6, code lost:
    
        if (r37 == null) goto L76;
     */
    /* JADX WARN: Code restructure failed: missing block: B:172:0x02d8, code lost:
    
        r48 = r4;
        r49 = r5;
        r4 = com.lagradost.cloudstream3.MainAPIKt.newMovieSearchResponse$default(r1, r37, "pikashow:" + r36.getSortOrder() + ":" + r10, com.lagradost.cloudstream3.TvType.Movie, false, new com.cncverse.PikashowProvider$$ExternalSyntheticLambda1(r36, r1), 8, (java.lang.Object) null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:173:0x0321, code lost:
    
        if (r4 == null) goto L235;
     */
    /* JADX WARN: Code restructure failed: missing block: B:174:0x0323, code lost:
    
        r7.add(r4);
     */
    /* JADX WARN: Code restructure failed: missing block: B:176:0x0327, code lost:
    
        r4 = r48;
        r5 = r49;
     */
    /* JADX WARN: Code restructure failed: missing block: B:178:0x0319, code lost:
    
        r48 = r4;
        r49 = r5;
        r4 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:180:0x032c, code lost:
    
        r0 = kotlin.collections.CollectionsKt.asReversed(r7);
     */
    /* JADX WARN: Code restructure failed: missing block: B:181:0x033c, code lost:
    
        if (r0 != null) goto L96;
     */
    /* JADX WARN: Code restructure failed: missing block: B:183:0x0341, code lost:
    
        r0 = kotlin.collections.CollectionsKt.emptyList();
     */
    /* JADX WARN: Code restructure failed: missing block: B:184:0x0370, code lost:
    
        r26 = r0;
        r34 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:186:0x0346, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:188:0x034f, code lost:
    
        java.lang.System.out.println((java.lang.Object) ("Error parsing movie response: " + r0.getMessage()));
     */
    /* JADX WARN: Code restructure failed: missing block: B:189:0x036f, code lost:
    
        r0 = kotlin.collections.CollectionsKt.emptyList();
     */
    /* JADX WARN: Code restructure failed: missing block: B:191:0x0376, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:192:0x0377, code lost:
    
        r3 = r51;
        r36 = r1;
        r23 = r12;
        r12 = r25;
        r5 = r30;
        r4 = r39;
     */
    /* JADX WARN: Code restructure failed: missing block: B:194:0x0348, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:197:0x034a, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:198:0x034b, code lost:
    
        r32 = r7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:199:0x0221, code lost:
    
        r34 = r1;
        r31 = r6;
        r32 = r7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:205:0x021f, code lost:
    
        if (r10.equals(r6) == false) goto L54;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x0561, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x0562, code lost:
    
        r31 = r6;
        r32 = r7;
        r17 = r51;
        r5 = r10;
        r20 = r12;
        r19 = r13;
        r23 = r21;
        r12 = r0;
        r21 = r15;
        r3 = r33;
        r4 = r39;
        r15 = r52;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x057e, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x057f, code lost:
    
        r33 = r5;
        r31 = r6;
        r32 = r7;
        r17 = r51;
        r5 = r10;
        r20 = r12;
        r19 = r13;
        r23 = r11;
        r12 = r0;
        r21 = r15;
        r3 = r33;
        r4 = r39;
        r15 = r52;
     */
    /* JADX WARN: Code restructure failed: missing block: B:66:0x059f, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:67:0x05a0, code lost:
    
        r33 = r5;
        r31 = r6;
        r32 = r7;
        r17 = r51;
        r5 = r10;
        r20 = r12;
        r19 = r13;
        r23 = r11;
        r12 = r0;
        r21 = r15;
        r3 = r33;
        r4 = r39;
        r15 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:69:0x05c2, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:70:0x05c3, code lost:
    
        r33 = r5;
        r31 = r6;
        r32 = r7;
        r17 = r51;
        r5 = r10;
        r20 = r12;
        r19 = r13;
        r23 = r11;
        r12 = r0;
        r21 = r15;
        r3 = r33;
        r4 = r39;
        r15 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:72:0x05e7, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:73:0x05e8, code lost:
    
        r33 = r5;
        r31 = r6;
        r32 = r7;
        r17 = r51;
        r5 = r10;
        r20 = r12;
        r19 = r13;
        r23 = r11;
        r12 = r0;
        r21 = r15;
        r3 = r33;
        r4 = r9;
        r15 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:75:0x060f, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:76:0x0610, code lost:
    
        r38 = r4;
        r33 = r5;
        r31 = r6;
        r32 = r7;
        r17 = r51;
        r5 = r10;
        r20 = r12;
        r19 = r13;
        r23 = r11;
        r12 = r0;
        r21 = r15;
        r3 = r33;
        r4 = r9;
        r15 = r1;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002e. Please report as an issue. */
    /* JADX WARN: Failed to find 'out' block for switch in B:87:0x01f4. Please report as an issue. */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0039  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0531  */
    /* JADX WARN: Removed duplicated region for block: B:218:0x00a0  */
    /* JADX WARN: Removed duplicated region for block: B:25:0x00fd A[Catch: Exception -> 0x06a9, TRY_LEAVE, TryCatch #4 {Exception -> 0x06a9, blocks: (B:23:0x00f7, B:25:0x00fd), top: B:22:0x00f7 }] */
    /* JADX WARN: Removed duplicated region for block: B:80:0x069b  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x01e0 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0031  */
    /* JADX WARN: Removed duplicated region for block: B:93:0x04fc A[Catch: Exception -> 0x050d, TRY_LEAVE, TryCatch #1 {Exception -> 0x050d, blocks: (B:90:0x04eb, B:91:0x04f1, B:93:0x04fc, B:137:0x04bb), top: B:89:0x04eb }] */
    /* JADX WARN: Type inference failed for: r22v4, types: [java.util.List] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:49:0x01ba -> B:15:0x01d4). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:55:0x065e -> B:21:0x066d). Please report as a decompilation issue!!! */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getMainPage(int i, @NotNull MainPageRequest mainPageRequest, @NotNull Continuation<? super HomePageResponse> continuation) {
        PikashowProvider$getMainPage$1 pikashowProvider$getMainPage$1;
        PikashowProvider pikashowProvider;
        PikashowProvider$getMainPage$1 pikashowProvider$getMainPage$12;
        char c;
        char c2;
        PikashowProvider pikashowProvider2;
        int i2;
        MainPageRequest mainPageRequest2;
        Continuation<? super HomePageResponse> continuation2;
        Iterator it;
        List list;
        ArrayList arrayList;
        int i3;
        Object obj;
        Object obj2;
        PikashowProvider$getMainPage$1 pikashowProvider$getMainPage$13;
        Map<String, String> map;
        MainPageRequest mainPageRequest3;
        Object obj3;
        Continuation<? super HomePageResponse> continuation3;
        int i4;
        String str;
        String str2;
        Map<String, String> map2;
        List list2;
        Object obj4;
        Iterator it2;
        String str3;
        Exception e;
        Object obj5;
        Map<String, String> map3;
        MainPageRequest mainPageRequest4;
        String str4;
        String str5;
        String str6;
        Object obj6;
        int i5;
        PikashowProvider$getMainPage$1 pikashowProvider$getMainPage$14;
        Map<String, String> map4;
        Object obj7;
        String str7;
        PikashowProvider pikashowProvider3;
        String str8;
        String str9;
        NiceResponse niceResponse;
        PikashowProvider pikashowProvider4;
        PikashowProvider pikashowProvider5;
        List list3;
        List emptyList;
        String str10;
        PikashowSeriesResponse pikashowSeriesResponse;
        TvSeriesSearchResponse tvSeriesSearchResponse;
        if (continuation instanceof PikashowProvider$getMainPage$1) {
            pikashowProvider$getMainPage$1 = (PikashowProvider$getMainPage$1) continuation;
            if ((pikashowProvider$getMainPage$1.label & Integer.MIN_VALUE) != 0) {
                pikashowProvider$getMainPage$1.label -= Integer.MIN_VALUE;
                pikashowProvider = this;
                pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$1;
                Object obj8 = pikashowProvider$getMainPage$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                String str11 = "bollywood";
                String str12 = "hollywood";
                switch (pikashowProvider$getMainPage$12.label) {
                    case 0:
                        c = 0;
                        c2 = 1;
                        ResultKt.throwOnFailure(obj8);
                        DonationManager.INSTANCE.checkAndShow(pikashowProvider.getName());
                        Map<String, String> pikashowHeaders = pikashowProvider.getPikashowHeaders();
                        ArrayList arrayList2 = new ArrayList();
                        try {
                            List listOf = CollectionsKt.listOf(new Pair[]{TuplesKt.to("series", "TV Series"), TuplesKt.to("hollywood", "Hollywood Movies"), TuplesKt.to("bollywood", "Bollywood Movies")});
                            List list4 = listOf;
                            it = list4.iterator();
                            list = listOf;
                            pikashowProvider2 = pikashowProvider;
                            arrayList = arrayList2;
                            i3 = 0;
                            obj = coroutine_suspended;
                            obj2 = list4;
                            pikashowProvider$getMainPage$13 = pikashowProvider$getMainPage$12;
                            map = pikashowHeaders;
                            mainPageRequest3 = mainPageRequest;
                            obj3 = obj8;
                            continuation3 = continuation;
                            i4 = i;
                            try {
                            } catch (Exception e2) {
                                e = e2;
                                continuation2 = continuation3;
                                Object obj9 = obj3;
                                mainPageRequest2 = mainPageRequest3;
                                i2 = i4;
                                arrayList2 = arrayList;
                                pikashowHeaders = map;
                                pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$13;
                                obj8 = obj9;
                            }
                        } catch (Exception e3) {
                            e = e3;
                            pikashowProvider2 = this;
                            i2 = i;
                            mainPageRequest2 = mainPageRequest;
                            continuation2 = continuation;
                            System.out.println((Object) ("Error in getMainPage: " + e.getMessage()));
                            arrayList2.add(new HomePageList("Status", CollectionsKt.listOf(MainAPIKt.newMovieSearchResponse$default(pikashowProvider2, "Pikashow Service", "error", TvType.Movie, false, new PikashowProvider$$ExternalSyntheticLambda2(), 8, (Object) null)), false, 4, (DefaultConstructorMarker) null));
                            arrayList = arrayList2;
                            return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                        }
                        if (it.hasNext()) {
                            try {
                                Object next = it.next();
                                Pair pair = (Pair) next;
                                str4 = (String) pair.component1();
                                str5 = (String) pair.component2();
                                continuation2 = continuation3;
                                str6 = pikashowProvider2.getMainUrl() + "/v1/api/videos";
                                Pair[] pairArr = new Pair[2];
                                obj6 = obj3;
                                pairArr[c] = TuplesKt.to("type", str4);
                                pairArr[c2] = TuplesKt.to("channel", "pikashow");
                                Map mapOf = MapsKt.mapOf(pairArr);
                                Requests app = MainActivityKt.getApp();
                                pikashowProvider$getMainPage$13.L$0 = SpillingKt.nullOutSpilledVariable(mainPageRequest4);
                                pikashowProvider$getMainPage$13.L$1 = map;
                                pikashowProvider$getMainPage$13.L$2 = arrayList;
                                pikashowProvider$getMainPage$13.L$3 = SpillingKt.nullOutSpilledVariable(list);
                                pikashowProvider$getMainPage$13.L$4 = SpillingKt.nullOutSpilledVariable(obj2);
                                pikashowProvider$getMainPage$13.L$5 = it;
                                pikashowProvider$getMainPage$13.L$6 = SpillingKt.nullOutSpilledVariable(next);
                                pikashowProvider$getMainPage$13.L$7 = str4;
                                pikashowProvider$getMainPage$13.L$8 = str5;
                                pikashowProvider$getMainPage$13.L$9 = SpillingKt.nullOutSpilledVariable(str6);
                                pikashowProvider$getMainPage$13.L$10 = SpillingKt.nullOutSpilledVariable(mapOf);
                                pikashowProvider$getMainPage$13.I$0 = i4;
                                pikashowProvider$getMainPage$13.I$1 = i3;
                                pikashowProvider$getMainPage$13.I$2 = 0;
                                i5 = i4;
                                pikashowProvider$getMainPage$13.label = 1;
                                pikashowProvider$getMainPage$14 = pikashowProvider$getMainPage$13;
                                map4 = map;
                                obj7 = Requests.get$default(app, str6, map4, (String) null, mapOf, (Map) null, false, 0, (TimeUnit) null, 30L, (Interceptor) null, false, (ResponseParser) null, pikashowProvider$getMainPage$14, 3828, (Object) null);
                            } catch (Exception e4) {
                                e = e4;
                                continuation2 = continuation3;
                                Object obj10 = obj3;
                                mainPageRequest2 = mainPageRequest4;
                                i2 = i4;
                                arrayList2 = arrayList;
                                pikashowHeaders = map;
                                pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$13;
                                obj8 = obj10;
                                System.out.println((Object) ("Error in getMainPage: " + e.getMessage()));
                                arrayList2.add(new HomePageList("Status", CollectionsKt.listOf(MainAPIKt.newMovieSearchResponse$default(pikashowProvider2, "Pikashow Service", "error", TvType.Movie, false, new PikashowProvider$$ExternalSyntheticLambda2(), 8, (Object) null)), false, 4, (DefaultConstructorMarker) null));
                                arrayList = arrayList2;
                                return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                            }
                            mainPageRequest4 = mainPageRequest3;
                            if (obj7 == obj) {
                                return obj;
                            }
                            mainPageRequest2 = mainPageRequest4;
                            str7 = str5;
                            coroutine_suspended = obj;
                            it2 = it;
                            pikashowProvider3 = pikashowProvider2;
                            obj8 = obj7;
                            str8 = str4;
                            str9 = str6;
                            pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$14;
                            obj4 = obj2;
                            map3 = map4;
                            list2 = list;
                            i2 = i5;
                            try {
                            } catch (Exception e5) {
                                e = e5;
                                str2 = str11;
                                str = str12;
                                map2 = map3;
                                str3 = str7;
                                pikashowProvider2 = pikashowProvider3;
                                obj8 = obj6;
                            }
                            niceResponse = (NiceResponse) obj8;
                            PikashowProvider$getMainPage$1 pikashowProvider$getMainPage$15 = pikashowProvider$getMainPage$12;
                            if (niceResponse.getCode() != 200) {
                                try {
                                } catch (Exception e6) {
                                    e = e6;
                                    pikashowProvider4 = pikashowProvider3;
                                    str2 = str11;
                                    str = str12;
                                    pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$15;
                                    map2 = map3;
                                    str3 = str7;
                                }
                                String str13 = "pikashow:";
                                obj5 = coroutine_suspended;
                                switch (str8.hashCode()) {
                                    case -905838985:
                                        str2 = str11;
                                        str = str12;
                                        try {
                                        } catch (Exception e7) {
                                            try {
                                                e = e7;
                                                pikashowProvider4 = pikashowProvider3;
                                                pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$15;
                                                map2 = map3;
                                                str3 = str7;
                                                coroutine_suspended = obj5;
                                                System.out.println((Object) ("Failed to fetch " + str3 + ": " + e.getMessage()));
                                                obj3 = obj8;
                                                obj5 = coroutine_suspended;
                                                map3 = map2;
                                                pikashowProvider$getMainPage$13 = pikashowProvider$getMainPage$12;
                                                i4 = i2;
                                                list = list2;
                                                continuation3 = continuation2;
                                                mainPageRequest3 = mainPageRequest2;
                                                map = map3;
                                                it = it2;
                                                obj2 = obj4;
                                                obj = obj5;
                                                str11 = str2;
                                                str12 = str;
                                                c2 = 1;
                                                if (it.hasNext()) {
                                                }
                                            } catch (Exception e8) {
                                                e = e8;
                                                arrayList2 = arrayList;
                                                pikashowHeaders = map2;
                                                System.out.println((Object) ("Error in getMainPage: " + e.getMessage()));
                                                arrayList2.add(new HomePageList("Status", CollectionsKt.listOf(MainAPIKt.newMovieSearchResponse$default(pikashowProvider2, "Pikashow Service", "error", TvType.Movie, false, new PikashowProvider$$ExternalSyntheticLambda2(), 8, (Object) null)), false, 4, (DefaultConstructorMarker) null));
                                                arrayList = arrayList2;
                                                return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                                            }
                                            pikashowProvider2 = pikashowProvider4;
                                            obj8 = obj6;
                                        }
                                        if (!str8.equals("series")) {
                                            pikashowProvider5 = pikashowProvider3;
                                            try {
                                            } catch (Exception e9) {
                                                e = e9;
                                                pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$15;
                                                map2 = map3;
                                                str3 = str7;
                                                coroutine_suspended = obj5;
                                                pikashowProvider2 = pikashowProvider5;
                                                obj8 = obj6;
                                                System.out.println((Object) ("Failed to fetch " + str3 + ": " + e.getMessage()));
                                                obj3 = obj8;
                                                obj5 = coroutine_suspended;
                                                map3 = map2;
                                                pikashowProvider$getMainPage$13 = pikashowProvider$getMainPage$12;
                                                i4 = i2;
                                                list = list2;
                                                continuation3 = continuation2;
                                                mainPageRequest3 = mainPageRequest2;
                                                map = map3;
                                                it = it2;
                                                obj2 = obj4;
                                                obj = obj5;
                                                str11 = str2;
                                                str12 = str;
                                                c2 = 1;
                                                if (it.hasNext()) {
                                                }
                                            }
                                            list3 = CollectionsKt.emptyList();
                                            if (!list3.isEmpty()) {
                                                arrayList.add(new HomePageList(str7, list3, false, 4, (DefaultConstructorMarker) null));
                                                break;
                                            }
                                        } else {
                                            try {
                                            } catch (Exception e10) {
                                                e = e10;
                                                pikashowProvider5 = pikashowProvider3;
                                                break;
                                            }
                                            pikashowProvider5 = pikashowProvider3;
                                            Object readValue = pikashowProvider3.mapper.readValue(niceResponse.getText(), new PikashowProvider$getMainPage$lambda$0$$inlined$readValue$1());
                                            if (!(readValue instanceof PikashowSeriesResponse)) {
                                                throw new RuntimeJsonMappingException("Deserialized value did not match the specified type; specified " + Reflection.getOrCreateKotlinClass(PikashowSeriesResponse.class).getQualifiedName() + "(non-null) but was " + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : null));
                                            }
                                            PikashowSeriesResponse pikashowSeriesResponse2 = (PikashowSeriesResponse) readValue;
                                            List<PikashowSeries> series = pikashowSeriesResponse2.getSeries();
                                            if (series != null) {
                                                ArrayList arrayList3 = new ArrayList();
                                                for (PikashowSeries pikashowSeries : series) {
                                                    String title = pikashowSeries.getTitle();
                                                    if (title != null) {
                                                        pikashowSeriesResponse = pikashowSeriesResponse2;
                                                        str10 = str13;
                                                        tvSeriesSearchResponse = MainAPIKt.newTvSeriesSearchResponse$default(pikashowProvider5, title, str13 + title + ":" + str8, TvType.TvSeries, false, new PikashowProvider$$ExternalSyntheticLambda0(pikashowSeries), 8, (Object) null);
                                                    } else {
                                                        str10 = str13;
                                                        pikashowSeriesResponse = pikashowSeriesResponse2;
                                                        tvSeriesSearchResponse = null;
                                                    }
                                                    if (tvSeriesSearchResponse != null) {
                                                        arrayList3.add(tvSeriesSearchResponse);
                                                    }
                                                    pikashowSeriesResponse2 = pikashowSeriesResponse;
                                                    str13 = str10;
                                                }
                                                emptyList = CollectionsKt.asReversed(arrayList3);
                                                if (emptyList == null) {
                                                }
                                                list3 = emptyList;
                                                if (!list3.isEmpty()) {
                                                }
                                            }
                                            emptyList = CollectionsKt.emptyList();
                                            list3 = emptyList;
                                            if (!list3.isEmpty()) {
                                            }
                                        }
                                        break;
                                    case -584959943:
                                        try {
                                        } catch (Exception e11) {
                                            e = e11;
                                            str2 = str11;
                                            str = str12;
                                            pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$15;
                                            pikashowProvider2 = pikashowProvider3;
                                            map2 = map3;
                                            str3 = str7;
                                            coroutine_suspended = obj5;
                                            obj8 = obj6;
                                            System.out.println((Object) ("Failed to fetch " + str3 + ": " + e.getMessage()));
                                            obj3 = obj8;
                                            obj5 = coroutine_suspended;
                                            map3 = map2;
                                            pikashowProvider$getMainPage$13 = pikashowProvider$getMainPage$12;
                                            i4 = i2;
                                            list = list2;
                                            continuation3 = continuation2;
                                            mainPageRequest3 = mainPageRequest2;
                                            map = map3;
                                            it = it2;
                                            obj2 = obj4;
                                            obj = obj5;
                                            str11 = str2;
                                            str12 = str;
                                            c2 = 1;
                                            if (it.hasNext()) {
                                            }
                                        }
                                        break;
                                    case 1455215167:
                                        try {
                                        } catch (Exception e12) {
                                            e = e12;
                                            pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$15;
                                            pikashowProvider2 = pikashowProvider3;
                                            str2 = str11;
                                            str = str12;
                                            map2 = map3;
                                            str3 = str7;
                                            coroutine_suspended = obj5;
                                            obj8 = obj6;
                                            System.out.println((Object) ("Failed to fetch " + str3 + ": " + e.getMessage()));
                                            obj3 = obj8;
                                            obj5 = coroutine_suspended;
                                            map3 = map2;
                                            pikashowProvider$getMainPage$13 = pikashowProvider$getMainPage$12;
                                            i4 = i2;
                                            list = list2;
                                            continuation3 = continuation2;
                                            mainPageRequest3 = mainPageRequest2;
                                            map = map3;
                                            it = it2;
                                            obj2 = obj4;
                                            obj = obj5;
                                            str11 = str2;
                                            str12 = str;
                                            c2 = 1;
                                            if (it.hasNext()) {
                                            }
                                        }
                                        break;
                                    default:
                                        pikashowProvider5 = pikashowProvider3;
                                        str2 = str11;
                                        str = str12;
                                        list3 = CollectionsKt.emptyList();
                                        if (!list3.isEmpty()) {
                                        }
                                        break;
                                }
                            } else {
                                pikashowProvider5 = pikashowProvider3;
                                obj5 = coroutine_suspended;
                                str2 = str11;
                                str = str12;
                            }
                            pikashowProvider$getMainPage$13 = pikashowProvider$getMainPage$15;
                            pikashowProvider2 = pikashowProvider5;
                            obj3 = obj6;
                            i4 = i2;
                            mainPageRequest3 = mainPageRequest2;
                            list = list2;
                            continuation3 = continuation2;
                            map = map3;
                            it = it2;
                            obj2 = obj4;
                            obj = obj5;
                            str11 = str2;
                            str12 = str;
                            c2 = 1;
                            if (it.hasNext()) {
                                return MainAPIKt.newHomePageResponse$default(arrayList, (Boolean) null, 2, (Object) null);
                            }
                        }
                    case 1:
                        int i6 = pikashowProvider$getMainPage$12.I$2;
                        i3 = pikashowProvider$getMainPage$12.I$1;
                        i2 = pikashowProvider$getMainPage$12.I$0;
                        c = 0;
                        str9 = (String) pikashowProvider$getMainPage$12.L$9;
                        str3 = (String) pikashowProvider$getMainPage$12.L$8;
                        str8 = (String) pikashowProvider$getMainPage$12.L$7;
                        Object obj11 = pikashowProvider$getMainPage$12.L$6;
                        it2 = (Iterator) pikashowProvider$getMainPage$12.L$5;
                        obj4 = (Iterable) pikashowProvider$getMainPage$12.L$4;
                        list2 = (List) pikashowProvider$getMainPage$12.L$3;
                        ?? r22 = (List) pikashowProvider$getMainPage$12.L$2;
                        map2 = (Map) pikashowProvider$getMainPage$12.L$1;
                        MainPageRequest mainPageRequest5 = (MainPageRequest) pikashowProvider$getMainPage$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            continuation2 = continuation;
                            pikashowProvider3 = pikashowProvider;
                            obj6 = obj8;
                            str7 = str3;
                            arrayList = r22;
                            map3 = map2;
                            mainPageRequest2 = mainPageRequest5;
                        } catch (Exception e13) {
                            e = e13;
                            continuation2 = continuation;
                            pikashowProvider2 = pikashowProvider;
                            str2 = "bollywood";
                            str = "hollywood";
                            arrayList = r22;
                            mainPageRequest2 = mainPageRequest5;
                            System.out.println((Object) ("Failed to fetch " + str3 + ": " + e.getMessage()));
                            obj3 = obj8;
                            obj5 = coroutine_suspended;
                            map3 = map2;
                            pikashowProvider$getMainPage$13 = pikashowProvider$getMainPage$12;
                            i4 = i2;
                            list = list2;
                            continuation3 = continuation2;
                            mainPageRequest3 = mainPageRequest2;
                            map = map3;
                            it = it2;
                            obj2 = obj4;
                            obj = obj5;
                            str11 = str2;
                            str12 = str;
                            c2 = 1;
                            if (it.hasNext()) {
                            }
                        }
                        niceResponse = (NiceResponse) obj8;
                        PikashowProvider$getMainPage$1 pikashowProvider$getMainPage$152 = pikashowProvider$getMainPage$12;
                        if (niceResponse.getCode() != 200) {
                        }
                        pikashowProvider$getMainPage$13 = pikashowProvider$getMainPage$152;
                        pikashowProvider2 = pikashowProvider5;
                        obj3 = obj6;
                        i4 = i2;
                        mainPageRequest3 = mainPageRequest2;
                        list = list2;
                        continuation3 = continuation2;
                        map = map3;
                        it = it2;
                        obj2 = obj4;
                        obj = obj5;
                        str11 = str2;
                        str12 = str;
                        c2 = 1;
                        if (it.hasNext()) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        pikashowProvider = this;
        pikashowProvider$getMainPage$1 = new PikashowProvider$getMainPage$1(pikashowProvider, continuation);
        pikashowProvider$getMainPage$12 = pikashowProvider$getMainPage$1;
        Object obj82 = pikashowProvider$getMainPage$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        String str112 = "bollywood";
        String str122 = "hollywood";
        switch (pikashowProvider$getMainPage$12.label) {
        }
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

    /* JADX WARN: Code restructure failed: missing block: B:220:0x01da, code lost:
    
        if (r20.equals("hollywood") == false) goto L43;
     */
    /* JADX WARN: Code restructure failed: missing block: B:221:0x01f9, code lost:
    
        r0 = getMainUrl() + "/v1/api/videos";
        r10 = kotlin.collections.MapsKt.mapOf(new kotlin.Pair[]{kotlin.TuplesKt.to("type", r20), kotlin.TuplesKt.to("channel", "pikashow")});
        r6 = com.lagradost.cloudstream3.MainActivityKt.getApp();
        r12.L$0 = r33;
        r12.L$1 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r7);
        r12.L$2 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r7);
        r12.L$3 = r15;
        r12.L$4 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r20);
        r12.L$5 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r20);
        r12.L$6 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r0);
        r12.L$7 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r10);
        r12.label = 3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:222:0x025a, code lost:
    
        r28 = r20;
        r25 = r7;
        r0 = r6;
        r3 = "Deserialized value did not match the specified type; specified ";
        r4 = " but was ";
        r5 = 200;
     */
    /* JADX WARN: Code restructure failed: missing block: B:224:0x028e, code lost:
    
        r6 = com.lagradost.nicehttp.Requests.get$default(r6, r0, r20, (java.lang.String) null, r10, (java.util.Map) null, false, 0, (java.util.concurrent.TimeUnit) null, 30, (okhttp3.Interceptor) null, false, (com.lagradost.nicehttp.ResponseParser) null, r12, 3828, (java.lang.Object) null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:225:0x0292, code lost:
    
        r12 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:226:0x0294, code lost:
    
        if (r6 != r0) goto L50;
     */
    /* JADX WARN: Code restructure failed: missing block: B:227:0x0296, code lost:
    
        return r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:228:0x0297, code lost:
    
        r13 = r0;
        r14 = r20;
        r15 = r7;
        r7 = r15;
        r8 = r33;
        r2 = r10;
     */
    /* JADX WARN: Code restructure failed: missing block: B:230:0x0411, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:231:0x0412, code lost:
    
        r2 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:235:0x01ec, code lost:
    
        r25 = r7;
        r30 = r15;
        r28 = r20;
        r8 = r20;
        r2 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:241:0x01ea, code lost:
    
        if (r20.equals("bollywood") == false) goto L43;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:195:0x01c4. Please report as an issue. */
    /* JADX WARN: Failed to find 'out' block for switch in B:8:0x0034. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:12:0x003f  */
    /* JADX WARN: Removed duplicated region for block: B:182:0x0710  */
    /* JADX WARN: Removed duplicated region for block: B:185:0x0153  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0088  */
    /* JADX WARN: Removed duplicated region for block: B:83:0x00c0  */
    /* JADX WARN: Removed duplicated region for block: B:93:0x0112  */
    /* JADX WARN: Removed duplicated region for block: B:99:0x04f4 A[Catch: Exception -> 0x0723, TRY_LEAVE, TryCatch #16 {Exception -> 0x0723, blocks: (B:97:0x04e8, B:99:0x04f4, B:108:0x0559, B:120:0x058b, B:122:0x0599, B:123:0x05a0, B:125:0x05a6, B:131:0x05c5, B:137:0x05e0), top: B:96:0x04e8 }] */
    /* JADX WARN: Removed duplicated region for block: B:9:0x0037  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object load(@NotNull String str, @NotNull Continuation<? super LoadResponse> continuation) {
        PikashowProvider$load$1 pikashowProvider$load$1;
        PikashowProvider$load$1 pikashowProvider$load$12;
        Object obj;
        List list;
        String str2;
        String str3;
        Map<String, String> map;
        Object obj2;
        String str4;
        List list2;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        String str10;
        Object obj3;
        Object obj4;
        String str11;
        Map<String, String> map2;
        Map map3;
        String str12;
        List list3;
        String str13;
        String str14;
        NiceResponse niceResponse;
        String str15;
        TvSeriesLoadResponse tvSeriesLoadResponse;
        PikashowSeries pikashowSeries;
        PikashowSeriesResponse pikashowSeriesResponse;
        PikashowSeries pikashowSeries2;
        String str16;
        Object newTvSeriesLoadResponse;
        String str17;
        List<SeasonDetail> list4;
        String str18;
        String str19;
        boolean z;
        Integer intOrNull;
        Object obj5;
        Object obj6;
        MovieLoadResponse movieLoadResponse;
        PikashowMovie pikashowMovie;
        PikashowProvider$load$3$1 pikashowProvider$load$3$1;
        Object newMovieLoadResponse;
        String str20;
        Map<String, String> map4;
        String str21;
        Object obj7;
        try {
            try {
                if (continuation instanceof PikashowProvider$load$1) {
                    pikashowProvider$load$1 = (PikashowProvider$load$1) continuation;
                    if ((pikashowProvider$load$1.label & Integer.MIN_VALUE) != 0) {
                        pikashowProvider$load$1.label -= Integer.MIN_VALUE;
                        pikashowProvider$load$12 = pikashowProvider$load$1;
                        Object obj8 = pikashowProvider$load$12.result;
                        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                        switch (pikashowProvider$load$12.label) {
                            case 0:
                                ResultKt.throwOnFailure(obj8);
                                try {
                                    String removePrefix = StringsKt.removePrefix(str, getMainUrl() + "/");
                                    List split$default = StringsKt.split$default(removePrefix, new String[]{":"}, false, 0, 6, (Object) null);
                                    if (split$default.size() == 3 && Intrinsics.areEqual(split$default.get(0), "pikashow")) {
                                        String str22 = (String) split$default.get(1);
                                        String str23 = (String) split$default.get(2);
                                        Map<String, String> pikashowHeaders = getPikashowHeaders();
                                        switch (str23.hashCode()) {
                                            case -905838985:
                                                list = split$default;
                                                str7 = "Deserialized value did not match the specified type; specified ";
                                                map = pikashowHeaders;
                                                try {
                                                    if (!str23.equals("series")) {
                                                        str2 = str22;
                                                        str3 = str23;
                                                        obj2 = null;
                                                        return obj2;
                                                    }
                                                    String str24 = getMainUrl() + "/v1/api/videos";
                                                    Map mapOf = MapsKt.mapOf(new Pair[]{TuplesKt.to("type", "series"), TuplesKt.to("channel", "pikashow")});
                                                    Requests app = MainActivityKt.getApp();
                                                    pikashowProvider$load$12.L$0 = str;
                                                    pikashowProvider$load$12.L$1 = SpillingKt.nullOutSpilledVariable(removePrefix);
                                                    pikashowProvider$load$12.L$2 = SpillingKt.nullOutSpilledVariable(list);
                                                    pikashowProvider$load$12.L$3 = str22;
                                                    pikashowProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(str23);
                                                    pikashowProvider$load$12.L$5 = SpillingKt.nullOutSpilledVariable(map);
                                                    pikashowProvider$load$12.L$6 = SpillingKt.nullOutSpilledVariable(str24);
                                                    pikashowProvider$load$12.L$7 = SpillingKt.nullOutSpilledVariable(mapOf);
                                                    pikashowProvider$load$12.label = 1;
                                                    str8 = str23;
                                                    str9 = " but was ";
                                                    str10 = ":";
                                                    obj3 = coroutine_suspended;
                                                    try {
                                                        obj4 = Requests.get$default(app, str24, map, (String) null, mapOf, (Map) null, false, 0, (TimeUnit) null, 30L, (Interceptor) null, false, (ResponseParser) null, pikashowProvider$load$12, 3828, (Object) null);
                                                        pikashowProvider$load$12 = pikashowProvider$load$12;
                                                        if (obj4 == obj3) {
                                                            return obj3;
                                                        }
                                                        str11 = str24;
                                                        map2 = map;
                                                        map3 = mapOf;
                                                        str12 = removePrefix;
                                                        list3 = list;
                                                        str13 = str22;
                                                        str14 = str;
                                                        try {
                                                            niceResponse = (NiceResponse) obj4;
                                                            if (niceResponse.getCode() != 200) {
                                                                Object readValue = this.mapper.readValue(niceResponse.getText(), new PikashowProvider$load$$inlined$readValue$1());
                                                                if (!(readValue instanceof PikashowSeriesResponse)) {
                                                                    throw new RuntimeJsonMappingException(str7 + Reflection.getOrCreateKotlinClass(PikashowSeriesResponse.class).getQualifiedName() + "(non-null)" + str9 + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : null));
                                                                }
                                                                PikashowSeriesResponse pikashowSeriesResponse2 = (PikashowSeriesResponse) readValue;
                                                                List<PikashowSeries> series = pikashowSeriesResponse2.getSeries();
                                                                if (series != null) {
                                                                    Iterator<T> it = series.iterator();
                                                                    while (true) {
                                                                        if (it.hasNext()) {
                                                                            Object next = it.next();
                                                                            if (Intrinsics.areEqual(((PikashowSeries) next).getTitle(), str13)) {
                                                                                obj5 = next;
                                                                            }
                                                                        } else {
                                                                            obj5 = null;
                                                                        }
                                                                    }
                                                                    pikashowSeries = (PikashowSeries) obj5;
                                                                } else {
                                                                    pikashowSeries = null;
                                                                }
                                                                if (pikashowSeries != null) {
                                                                    PikashowSeries pikashowSeries3 = pikashowSeries;
                                                                    ArrayList arrayList = new ArrayList();
                                                                    List<SeasonDetail> details = pikashowSeries3.getDetails();
                                                                    if (details != null) {
                                                                        List<SeasonDetail> list5 = details;
                                                                        boolean z2 = false;
                                                                        for (SeasonDetail seasonDetail : list5) {
                                                                            String season = seasonDetail.getSeason();
                                                                            int intValue = (season == null || (intOrNull = StringsKt.toIntOrNull(season)) == null) ? 1 : intOrNull.intValue();
                                                                            Integer episodesCount = seasonDetail.getEpisodesCount();
                                                                            int intValue2 = episodesCount != null ? episodesCount.intValue() : 1;
                                                                            PikashowSeriesResponse pikashowSeriesResponse3 = pikashowSeriesResponse2;
                                                                            int i = 1;
                                                                            PikashowSeries pikashowSeries4 = pikashowSeries;
                                                                            if (1 <= intValue2) {
                                                                                while (true) {
                                                                                    list4 = list5;
                                                                                    str18 = str13;
                                                                                    str19 = str14;
                                                                                    try {
                                                                                        z = z2;
                                                                                        int i2 = intValue;
                                                                                        arrayList.add(MainAPIKt.newEpisode(this, "pikashow_episode:" + pikashowSeries3.getTitle() + str10 + i2 + str10 + i, new PikashowProvider$$ExternalSyntheticLambda5(i, i2)));
                                                                                        if (i != intValue2) {
                                                                                            i++;
                                                                                            intValue = i2;
                                                                                            list5 = list4;
                                                                                            str13 = str18;
                                                                                            str14 = str19;
                                                                                            z2 = z;
                                                                                        }
                                                                                    } catch (Exception e) {
                                                                                        e = e;
                                                                                        obj = null;
                                                                                        System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                                                        return obj;
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                list4 = list5;
                                                                                str18 = str13;
                                                                                str19 = str14;
                                                                                z = z2;
                                                                            }
                                                                            pikashowSeriesResponse2 = pikashowSeriesResponse3;
                                                                            list5 = list4;
                                                                            pikashowSeries = pikashowSeries4;
                                                                            str13 = str18;
                                                                            str14 = str19;
                                                                            z2 = z;
                                                                        }
                                                                        pikashowSeriesResponse = pikashowSeriesResponse2;
                                                                        pikashowSeries2 = pikashowSeries;
                                                                        str16 = str13;
                                                                        str15 = str14;
                                                                    } else {
                                                                        pikashowSeriesResponse = pikashowSeriesResponse2;
                                                                        pikashowSeries2 = pikashowSeries;
                                                                        str16 = str13;
                                                                        str15 = str14;
                                                                    }
                                                                    try {
                                                                        PikashowProvider pikashowProvider = this;
                                                                        String title = pikashowSeries3.getTitle();
                                                                        if (title == null) {
                                                                            title = "Unknown Series";
                                                                        }
                                                                        String str25 = title;
                                                                        TvType tvType = TvType.TvSeries;
                                                                        obj = null;
                                                                        try {
                                                                            PikashowProvider$load$2$2 pikashowProvider$load$2$2 = new PikashowProvider$load$2$2(pikashowSeries3, null);
                                                                            pikashowProvider$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                                                                            pikashowProvider$load$12.L$1 = SpillingKt.nullOutSpilledVariable(str12);
                                                                            pikashowProvider$load$12.L$2 = SpillingKt.nullOutSpilledVariable(list3);
                                                                            pikashowProvider$load$12.L$3 = SpillingKt.nullOutSpilledVariable(str16);
                                                                            pikashowProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(str8);
                                                                            pikashowProvider$load$12.L$5 = SpillingKt.nullOutSpilledVariable(map2);
                                                                            pikashowProvider$load$12.L$6 = SpillingKt.nullOutSpilledVariable(str11);
                                                                            pikashowProvider$load$12.L$7 = SpillingKt.nullOutSpilledVariable(map3);
                                                                            pikashowProvider$load$12.L$8 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                                                            pikashowProvider$load$12.L$9 = SpillingKt.nullOutSpilledVariable(pikashowSeriesResponse);
                                                                            pikashowProvider$load$12.L$10 = SpillingKt.nullOutSpilledVariable(pikashowSeries2);
                                                                            pikashowProvider$load$12.L$11 = SpillingKt.nullOutSpilledVariable(pikashowSeries3);
                                                                            pikashowProvider$load$12.L$12 = SpillingKt.nullOutSpilledVariable(arrayList);
                                                                            pikashowProvider$load$12.I$0 = 0;
                                                                            pikashowProvider$load$12.label = 2;
                                                                            try {
                                                                                newTvSeriesLoadResponse = MainAPIKt.newTvSeriesLoadResponse(pikashowProvider, str25, str15, tvType, arrayList, pikashowProvider$load$2$2, pikashowProvider$load$12);
                                                                                if (newTvSeriesLoadResponse == obj3) {
                                                                                    return obj3;
                                                                                }
                                                                                str17 = str16;
                                                                                tvSeriesLoadResponse = (TvSeriesLoadResponse) newTvSeriesLoadResponse;
                                                                            } catch (Exception e2) {
                                                                                e = e2;
                                                                                System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                                                return obj;
                                                                            }
                                                                        } catch (Exception e3) {
                                                                            e = e3;
                                                                            System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                                            return obj;
                                                                        }
                                                                    } catch (Exception e4) {
                                                                        e = e4;
                                                                        obj = null;
                                                                        System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                                        return obj;
                                                                    }
                                                                } else {
                                                                    str15 = str14;
                                                                    obj = null;
                                                                    tvSeriesLoadResponse = null;
                                                                }
                                                            } else {
                                                                str15 = str14;
                                                                obj = null;
                                                                tvSeriesLoadResponse = null;
                                                            }
                                                            return (LoadResponse) tvSeriesLoadResponse;
                                                        } catch (Exception e5) {
                                                            e = e5;
                                                            obj = null;
                                                        }
                                                    } catch (Exception e6) {
                                                        e = e6;
                                                        obj = null;
                                                        System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                        return obj;
                                                    }
                                                } catch (Exception e7) {
                                                    e = e7;
                                                    obj = null;
                                                }
                                                break;
                                            case -584959943:
                                                try {
                                                    break;
                                                } catch (Exception e8) {
                                                    e = e8;
                                                    obj = null;
                                                    System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                    return obj;
                                                }
                                            case 1455215167:
                                                try {
                                                    break;
                                                } catch (Exception e9) {
                                                    e = e9;
                                                    obj = null;
                                                    System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                    return obj;
                                                }
                                            default:
                                                list = split$default;
                                                str2 = str22;
                                                str3 = str23;
                                                map = pikashowHeaders;
                                                obj2 = null;
                                                return obj2;
                                        }
                                    }
                                    return null;
                                } catch (Exception e10) {
                                    e = e10;
                                    obj = null;
                                }
                                break;
                            case 1:
                                Map map5 = (Map) pikashowProvider$load$12.L$7;
                                String str26 = (String) pikashowProvider$load$12.L$6;
                                Map<String, String> map6 = (Map) pikashowProvider$load$12.L$5;
                                String str27 = (String) pikashowProvider$load$12.L$4;
                                String str28 = (String) pikashowProvider$load$12.L$3;
                                list3 = (List) pikashowProvider$load$12.L$2;
                                str12 = (String) pikashowProvider$load$12.L$1;
                                map3 = map5;
                                String str29 = (String) pikashowProvider$load$12.L$0;
                                ResultKt.throwOnFailure(obj8);
                                obj3 = coroutine_suspended;
                                map2 = map6;
                                str7 = "Deserialized value did not match the specified type; specified ";
                                str8 = str27;
                                str13 = str28;
                                str11 = str26;
                                obj4 = obj8;
                                str9 = " but was ";
                                str14 = str29;
                                str10 = ":";
                                niceResponse = (NiceResponse) obj4;
                                if (niceResponse.getCode() != 200) {
                                }
                                return (LoadResponse) tvSeriesLoadResponse;
                            case 2:
                                int i3 = pikashowProvider$load$12.I$0;
                                Map<String, String> map7 = (Map) pikashowProvider$load$12.L$5;
                                String str30 = (String) pikashowProvider$load$12.L$4;
                                str17 = (String) pikashowProvider$load$12.L$3;
                                List list6 = (List) pikashowProvider$load$12.L$2;
                                String str31 = (String) pikashowProvider$load$12.L$1;
                                String str32 = (String) pikashowProvider$load$12.L$0;
                                ResultKt.throwOnFailure(obj8);
                                str15 = str32;
                                newTvSeriesLoadResponse = obj8;
                                map2 = map7;
                                str8 = str30;
                                list3 = list6;
                                str12 = str31;
                                obj = null;
                                tvSeriesLoadResponse = (TvSeriesLoadResponse) newTvSeriesLoadResponse;
                                return (LoadResponse) tvSeriesLoadResponse;
                            case 3:
                                Map map8 = (Map) pikashowProvider$load$12.L$7;
                                String str33 = (String) pikashowProvider$load$12.L$6;
                                Map<String, String> map9 = (Map) pikashowProvider$load$12.L$5;
                                String str34 = (String) pikashowProvider$load$12.L$4;
                                String str35 = (String) pikashowProvider$load$12.L$3;
                                List list7 = (List) pikashowProvider$load$12.L$2;
                                str5 = (String) pikashowProvider$load$12.L$1;
                                String str36 = (String) pikashowProvider$load$12.L$0;
                                ResultKt.throwOnFailure(obj8);
                                String str37 = "Deserialized value did not match the specified type; specified ";
                                str4 = str34;
                                list2 = list7;
                                Map<String, String> map10 = map9;
                                String str38 = str35;
                                int i4 = 200;
                                String str39 = str33;
                                String str40 = " but was ";
                                str6 = str36;
                                Map map11 = map8;
                                Object obj9 = coroutine_suspended;
                                Object obj10 = obj8;
                                try {
                                    NiceResponse niceResponse2 = (NiceResponse) obj10;
                                    if (niceResponse2.getCode() == i4) {
                                        Object readValue2 = this.mapper.readValue(niceResponse2.getText(), new PikashowProvider$load$$inlined$readValue$2());
                                        Map map12 = map11;
                                        if (!(readValue2 instanceof PikashowMovieResponse)) {
                                            throw new RuntimeJsonMappingException(str37 + Reflection.getOrCreateKotlinClass(PikashowMovieResponse.class).getQualifiedName() + "(non-null)" + str40 + (readValue2 != null ? Reflection.getOrCreateKotlinClass(readValue2.getClass()).getQualifiedName() : null));
                                        }
                                        PikashowMovieResponse pikashowMovieResponse = (PikashowMovieResponse) readValue2;
                                        List<PikashowMovie> records = pikashowMovieResponse.getRecords();
                                        if (records != null) {
                                            Iterator<T> it2 = records.iterator();
                                            while (true) {
                                                if (it2.hasNext()) {
                                                    Object next2 = it2.next();
                                                    if (Intrinsics.areEqual(String.valueOf(((PikashowMovie) next2).getSortOrder()), str38)) {
                                                        obj7 = next2;
                                                    }
                                                } else {
                                                    obj7 = null;
                                                }
                                            }
                                            pikashowMovie = (PikashowMovie) obj7;
                                        } else {
                                            pikashowMovie = null;
                                        }
                                        if (pikashowMovie != null) {
                                            PikashowMovie pikashowMovie2 = pikashowMovie;
                                            PikashowProvider pikashowProvider2 = this;
                                            String title2 = pikashowMovie2.getTitle();
                                            if (title2 == null) {
                                                title2 = "Unknown Movie";
                                            }
                                            String str41 = str38;
                                            String str42 = title2;
                                            TvType tvType2 = TvType.Movie;
                                            try {
                                                pikashowProvider$load$3$1 = new PikashowProvider$load$3$1(pikashowMovie2, null);
                                                pikashowProvider$load$12.L$0 = SpillingKt.nullOutSpilledVariable(str6);
                                                pikashowProvider$load$12.L$1 = SpillingKt.nullOutSpilledVariable(str5);
                                                pikashowProvider$load$12.L$2 = SpillingKt.nullOutSpilledVariable(list2);
                                                pikashowProvider$load$12.L$3 = SpillingKt.nullOutSpilledVariable(str41);
                                                pikashowProvider$load$12.L$4 = SpillingKt.nullOutSpilledVariable(str4);
                                                pikashowProvider$load$12.L$5 = SpillingKt.nullOutSpilledVariable(map10);
                                                pikashowProvider$load$12.L$6 = SpillingKt.nullOutSpilledVariable(str39);
                                                pikashowProvider$load$12.L$7 = SpillingKt.nullOutSpilledVariable(map12);
                                                pikashowProvider$load$12.L$8 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                                                pikashowProvider$load$12.L$9 = SpillingKt.nullOutSpilledVariable(pikashowMovieResponse);
                                                pikashowProvider$load$12.L$10 = SpillingKt.nullOutSpilledVariable(pikashowMovie);
                                                pikashowProvider$load$12.L$11 = SpillingKt.nullOutSpilledVariable(pikashowMovie2);
                                                pikashowProvider$load$12.I$0 = 0;
                                                pikashowProvider$load$12.label = 4;
                                                obj6 = null;
                                            } catch (Exception e11) {
                                                e = e11;
                                                obj = null;
                                                System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                return obj;
                                            }
                                            try {
                                                newMovieLoadResponse = MainAPIKt.newMovieLoadResponse(pikashowProvider2, str42, str6, tvType2, str6, pikashowProvider$load$3$1, pikashowProvider$load$12);
                                                if (newMovieLoadResponse == obj9) {
                                                    return obj9;
                                                }
                                                str20 = str6;
                                                map4 = map10;
                                                str21 = str4;
                                                try {
                                                    str6 = str20;
                                                    str4 = str21;
                                                    movieLoadResponse = (MovieLoadResponse) newMovieLoadResponse;
                                                } catch (Exception e12) {
                                                    e = e12;
                                                    obj = obj6;
                                                    System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                    return obj;
                                                }
                                            } catch (Exception e13) {
                                                e = e13;
                                                obj = obj6;
                                                System.out.println((Object) ("Error in load function: " + e.getMessage()));
                                                return obj;
                                            }
                                        } else {
                                            obj6 = null;
                                            movieLoadResponse = null;
                                        }
                                    } else {
                                        obj6 = null;
                                        movieLoadResponse = null;
                                    }
                                    return (LoadResponse) movieLoadResponse;
                                } catch (Exception e14) {
                                    e = e14;
                                    obj = null;
                                }
                            case 4:
                                int i5 = pikashowProvider$load$12.I$0;
                                map4 = (Map) pikashowProvider$load$12.L$5;
                                str21 = (String) pikashowProvider$load$12.L$4;
                                List list8 = (List) pikashowProvider$load$12.L$2;
                                String str43 = (String) pikashowProvider$load$12.L$1;
                                str20 = (String) pikashowProvider$load$12.L$0;
                                ResultKt.throwOnFailure(obj8);
                                list2 = list8;
                                str5 = str43;
                                obj6 = null;
                                newMovieLoadResponse = obj8;
                                str6 = str20;
                                str4 = str21;
                                movieLoadResponse = (MovieLoadResponse) newMovieLoadResponse;
                                return (LoadResponse) movieLoadResponse;
                            default:
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                        }
                    }
                }
                switch (pikashowProvider$load$12.label) {
                }
            } catch (Exception e15) {
                e = e15;
                obj = null;
            }
        } catch (Exception e16) {
            e = e16;
            obj = null;
        }
        pikashowProvider$load$1 = new PikashowProvider$load$1(this, continuation);
        pikashowProvider$load$12 = pikashowProvider$load$1;
        Object obj82 = pikashowProvider$load$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
    }

    /* JADX WARN: Code restructure failed: missing block: B:120:0x06b6, code lost:
    
        if (r9.equals("bollywood") == false) goto L108;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:70:0x069e. Please report as an issue. */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0044. Please report as an issue. */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0050  */
    /* JADX WARN: Removed duplicated region for block: B:150:0x0aa7  */
    /* JADX WARN: Removed duplicated region for block: B:156:0x01e7  */
    /* JADX WARN: Removed duplicated region for block: B:165:0x024d  */
    /* JADX WARN: Removed duplicated region for block: B:172:0x03f9  */
    /* JADX WARN: Removed duplicated region for block: B:192:0x0508  */
    /* JADX WARN: Removed duplicated region for block: B:199:0x02a5  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x00e7  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x096a A[Catch: Exception -> 0x0a89, TryCatch #3 {Exception -> 0x0a89, blocks: (B:31:0x095c, B:33:0x096a, B:35:0x098c, B:37:0x09b2, B:38:0x09c5, B:39:0x09d2, B:41:0x09d3, B:43:0x09e0), top: B:30:0x095c }] */
    /* JADX WARN: Removed duplicated region for block: B:55:0x0a7d  */
    /* JADX WARN: Removed duplicated region for block: B:61:0x0168  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x0690 A[Catch: Exception -> 0x0ab4, TRY_LEAVE, TryCatch #7 {Exception -> 0x0ab4, blocks: (B:66:0x0684, B:68:0x0690, B:72:0x0841, B:74:0x0845, B:76:0x0849), top: B:65:0x0684 }] */
    /* JADX WARN: Removed duplicated region for block: B:74:0x0845 A[Catch: Exception -> 0x0ab4, TryCatch #7 {Exception -> 0x0ab4, blocks: (B:66:0x0684, B:68:0x0690, B:72:0x0841, B:74:0x0845, B:76:0x0849), top: B:65:0x0684 }] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0047  */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 2 */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object loadLinks(@NotNull String str, boolean z, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Boolean> continuation) {
        PikashowProvider$loadLinks$1 pikashowProvider$loadLinks$1;
        PikashowProvider$loadLinks$1 pikashowProvider$loadLinks$12;
        String removePrefix;
        Map<String, String> pikashowHeaders;
        String str2;
        String str3;
        boolean z2;
        String str4;
        Map<String, String> map;
        int i;
        String str5;
        PikashowProvider$loadLinks$1 pikashowProvider$loadLinks$13;
        Object obj;
        String str6;
        Map map2;
        String str7;
        Function1<? super SubtitleFile, Unit> function13;
        String str8;
        String str9;
        String str10;
        List list;
        boolean z3;
        Function1<? super ExtractorLink, Unit> function14;
        String str11;
        Object obj2;
        String str12;
        Object obj3;
        Object obj4;
        Object obj5;
        Object obj6;
        Object obj7;
        Object obj8;
        String str13;
        char c;
        PikashowSeries pikashowSeries;
        PikashowProvider pikashowProvider;
        Object obj9;
        boolean z4;
        PikashowProvider$loadLinks$1 pikashowProvider$loadLinks$14;
        String str14;
        Map<String, String> map3;
        Map map4;
        String str15;
        String str16;
        String str17;
        Function1<? super SubtitleFile, Unit> function15;
        List list2;
        String str18;
        boolean z5;
        Function1<? super ExtractorLink, Unit> function16;
        PikashowProvider pikashowProvider2;
        NiceResponse niceResponse;
        Map<String, String> map5;
        NiceResponse niceResponse2;
        Map<String, String> map6;
        String str19;
        PikashowSeries pikashowSeries2;
        Object obj10;
        String str20;
        Object obj11;
        Object obj12;
        String str21;
        Map map7;
        String str22;
        PikashowProvider$loadLinks$1 pikashowProvider$loadLinks$15;
        String str23;
        String str24;
        Map<String, String> map8;
        String str25;
        Ref.ObjectRef objectRef;
        Ref.ObjectRef objectRef2;
        boolean z6;
        Function1<? super ExtractorLink, Unit> function17;
        String str26;
        String str27;
        NiceResponse niceResponse3;
        String str28;
        boolean z7 = z;
        Function1<? super ExtractorLink, Unit> function18 = function12;
        if (continuation instanceof PikashowProvider$loadLinks$1) {
            pikashowProvider$loadLinks$1 = (PikashowProvider$loadLinks$1) continuation;
            if ((pikashowProvider$loadLinks$1.label & Integer.MIN_VALUE) != 0) {
                pikashowProvider$loadLinks$1.label -= Integer.MIN_VALUE;
                pikashowProvider$loadLinks$12 = pikashowProvider$loadLinks$1;
                Object obj13 = pikashowProvider$loadLinks$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                Object obj14 = obj13;
                switch (pikashowProvider$loadLinks$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj14);
                        try {
                            removePrefix = StringsKt.removePrefix(str, getMainUrl() + "/");
                            pikashowHeaders = getPikashowHeaders();
                            str2 = " but was ";
                            try {
                            } catch (Exception e) {
                                e = e;
                                System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                                return Boxing.boxBoolean(false);
                            }
                        } catch (Exception e2) {
                            e = e2;
                        }
                        try {
                            if (StringsKt.startsWith$default(removePrefix, "pikashow_episode:", false, 2, (Object) null)) {
                                try {
                                    List split$default = StringsKt.split$default(removePrefix, new String[]{":"}, false, 0, 6, (Object) null);
                                    if (split$default.size() >= 4) {
                                        String str29 = (String) split$default.get(1);
                                        String str30 = (String) split$default.get(2);
                                        String str31 = (String) split$default.get(3);
                                        str3 = removePrefix;
                                        String str32 = getMainUrl() + "/v1/api/video";
                                        Map mapOf = MapsKt.mapOf(new Pair[]{TuplesKt.to("type", "series"), TuplesKt.to("videoId", "0"), TuplesKt.to("title", str29), TuplesKt.to("noseasons", str30), TuplesKt.to("noepisodes", str31)});
                                        Requests app = MainActivityKt.getApp();
                                        pikashowProvider$loadLinks$12.L$0 = SpillingKt.nullOutSpilledVariable(str);
                                        pikashowProvider$loadLinks$12.L$1 = SpillingKt.nullOutSpilledVariable(function1);
                                        pikashowProvider$loadLinks$12.L$2 = function18;
                                        pikashowProvider$loadLinks$12.L$3 = SpillingKt.nullOutSpilledVariable(str3);
                                        pikashowProvider$loadLinks$12.L$4 = SpillingKt.nullOutSpilledVariable(pikashowHeaders);
                                        pikashowProvider$loadLinks$12.L$5 = SpillingKt.nullOutSpilledVariable(split$default);
                                        pikashowProvider$loadLinks$12.L$6 = SpillingKt.nullOutSpilledVariable(str29);
                                        pikashowProvider$loadLinks$12.L$7 = SpillingKt.nullOutSpilledVariable(str30);
                                        pikashowProvider$loadLinks$12.L$8 = str31;
                                        pikashowProvider$loadLinks$12.L$9 = SpillingKt.nullOutSpilledVariable(str32);
                                        pikashowProvider$loadLinks$12.L$10 = SpillingKt.nullOutSpilledVariable(mapOf);
                                        pikashowProvider$loadLinks$12.Z$0 = z7;
                                        pikashowProvider$loadLinks$12.label = 1;
                                        z2 = true;
                                        str4 = "Deserialized value did not match the specified type; specified ";
                                        map = pikashowHeaders;
                                        i = 200;
                                        str5 = null;
                                        try {
                                            Object obj15 = Requests.get$default(app, str32, map, (String) null, mapOf, (Map) null, false, 0, (TimeUnit) null, 30L, (Interceptor) null, false, (ResponseParser) null, pikashowProvider$loadLinks$12, 3828, (Object) null);
                                            pikashowProvider$loadLinks$13 = pikashowProvider$loadLinks$12;
                                            obj = coroutine_suspended;
                                            if (obj15 == obj) {
                                                return obj;
                                            }
                                            str6 = str32;
                                            map2 = mapOf;
                                            str7 = str;
                                            function13 = function1;
                                            str8 = str29;
                                            obj14 = obj15;
                                            str9 = str31;
                                            str10 = str30;
                                            list = split$default;
                                            try {
                                                niceResponse = (NiceResponse) obj14;
                                                if (niceResponse.getCode() != i) {
                                                    pikashowProvider2 = this;
                                                    try {
                                                        map5 = map;
                                                        Object readValue = pikashowProvider2.mapper.readValue(niceResponse.getText(), new PikashowProvider$loadLinks$$inlined$readValue$1());
                                                        if (!(readValue instanceof VideoApiResponse)) {
                                                            throw new RuntimeJsonMappingException(str4 + Reflection.getOrCreateKotlinClass(VideoApiResponse.class).getQualifiedName() + "(non-null)" + str2 + (readValue != null ? Reflection.getOrCreateKotlinClass(readValue.getClass()).getQualifiedName() : str5));
                                                        }
                                                        Map map9 = map2;
                                                        VideoApiResponse videoApiResponse = (VideoApiResponse) readValue;
                                                        VideoData data = videoApiResponse.getData();
                                                        if (data != null) {
                                                            String str33 = "Episode " + str9;
                                                            pikashowProvider$loadLinks$13.L$0 = SpillingKt.nullOutSpilledVariable(str7);
                                                            pikashowProvider$loadLinks$13.L$1 = SpillingKt.nullOutSpilledVariable(function13);
                                                            pikashowProvider$loadLinks$13.L$2 = SpillingKt.nullOutSpilledVariable(function18);
                                                            pikashowProvider$loadLinks$13.L$3 = SpillingKt.nullOutSpilledVariable(str3);
                                                            pikashowProvider$loadLinks$13.L$4 = SpillingKt.nullOutSpilledVariable(map5);
                                                            pikashowProvider$loadLinks$13.L$5 = SpillingKt.nullOutSpilledVariable(list);
                                                            pikashowProvider$loadLinks$13.L$6 = SpillingKt.nullOutSpilledVariable(str8);
                                                            pikashowProvider$loadLinks$13.L$7 = SpillingKt.nullOutSpilledVariable(str10);
                                                            pikashowProvider$loadLinks$13.L$8 = SpillingKt.nullOutSpilledVariable(str9);
                                                            pikashowProvider$loadLinks$13.L$9 = SpillingKt.nullOutSpilledVariable(str6);
                                                            pikashowProvider$loadLinks$13.L$10 = SpillingKt.nullOutSpilledVariable(map9);
                                                            pikashowProvider$loadLinks$13.L$11 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                                            pikashowProvider$loadLinks$13.L$12 = SpillingKt.nullOutSpilledVariable(videoApiResponse);
                                                            pikashowProvider$loadLinks$13.L$13 = SpillingKt.nullOutSpilledVariable(data);
                                                            pikashowProvider$loadLinks$13.Z$0 = z7;
                                                            pikashowProvider$loadLinks$13.I$0 = 0;
                                                            pikashowProvider$loadLinks$13.label = 2;
                                                            return pikashowProvider2.addVideoLinksToCallback(data, function18, str33, pikashowProvider$loadLinks$13) == obj ? obj : Boxing.boxBoolean(z2);
                                                        }
                                                    } catch (Exception e3) {
                                                        e = e3;
                                                        System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                                                        return Boxing.boxBoolean(false);
                                                    }
                                                } else {
                                                    pikashowProvider2 = this;
                                                    map5 = map;
                                                }
                                                z3 = z7;
                                                function14 = function18;
                                                return Boxing.boxBoolean(false);
                                            } catch (Exception e4) {
                                                e = e4;
                                                pikashowProvider2 = this;
                                            }
                                        } catch (Exception e5) {
                                            e = e5;
                                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                                            return Boxing.boxBoolean(false);
                                        }
                                    }
                                } catch (Exception e6) {
                                    e = e6;
                                }
                            } else {
                                str11 = str2;
                                obj2 = coroutine_suspended;
                                try {
                                    if (StringsKt.startsWith$default(removePrefix, "pikashow:", false, 2, (Object) null)) {
                                        List split$default2 = StringsKt.split$default(removePrefix, new String[]{":"}, false, 0, 6, (Object) null);
                                        if (split$default2.size() >= 3) {
                                            String str34 = (String) split$default2.get(1);
                                            String str35 = (String) split$default2.get(2);
                                            String str36 = getMainUrl() + "/v1/api/videos";
                                            Map mapOf2 = MapsKt.mapOf(new Pair[]{TuplesKt.to("type", str35), TuplesKt.to("channel", "pikashow")});
                                            Requests app2 = MainActivityKt.getApp();
                                            pikashowProvider$loadLinks$12.L$0 = SpillingKt.nullOutSpilledVariable(str);
                                            pikashowProvider$loadLinks$12.L$1 = SpillingKt.nullOutSpilledVariable(function1);
                                            pikashowProvider$loadLinks$12.L$2 = function18;
                                            pikashowProvider$loadLinks$12.L$3 = SpillingKt.nullOutSpilledVariable(removePrefix);
                                            pikashowProvider$loadLinks$12.L$4 = pikashowHeaders;
                                            pikashowProvider$loadLinks$12.L$5 = SpillingKt.nullOutSpilledVariable(split$default2);
                                            pikashowProvider$loadLinks$12.L$6 = str34;
                                            pikashowProvider$loadLinks$12.L$7 = str35;
                                            pikashowProvider$loadLinks$12.L$8 = SpillingKt.nullOutSpilledVariable(str36);
                                            pikashowProvider$loadLinks$12.L$9 = SpillingKt.nullOutSpilledVariable(mapOf2);
                                            pikashowProvider$loadLinks$12.Z$0 = z7;
                                            pikashowProvider$loadLinks$12.label = 3;
                                            str12 = "/v1/api/video";
                                            obj3 = "noepisodes";
                                            obj4 = "title";
                                            obj5 = "noseasons";
                                            obj6 = "videoId";
                                            obj7 = "0";
                                            obj8 = "type";
                                            str13 = "Deserialized value did not match the specified type; specified ";
                                            c = 3;
                                            pikashowSeries = null;
                                            pikashowProvider = this;
                                            obj9 = "series";
                                            z4 = true;
                                            try {
                                                Object obj16 = Requests.get$default(app2, str36, pikashowHeaders, (String) null, mapOf2, (Map) null, false, 0, (TimeUnit) null, 30L, (Interceptor) null, false, (ResponseParser) null, pikashowProvider$loadLinks$12, 3828, (Object) null);
                                                pikashowProvider$loadLinks$14 = pikashowProvider$loadLinks$12;
                                                if (obj16 == obj2) {
                                                    return obj2;
                                                }
                                                obj14 = obj16;
                                                str14 = str36;
                                                map3 = pikashowHeaders;
                                                map4 = mapOf2;
                                                str15 = str35;
                                                str16 = removePrefix;
                                                str17 = str34;
                                                function15 = function1;
                                                list2 = split$default2;
                                                str18 = str;
                                                z5 = z;
                                                function16 = function12;
                                                try {
                                                    niceResponse2 = (NiceResponse) obj14;
                                                    if (niceResponse2.getCode() != 200) {
                                                        Ref.ObjectRef objectRef3 = new Ref.ObjectRef();
                                                        Ref.ObjectRef objectRef4 = new Ref.ObjectRef();
                                                        try {
                                                            switch (str15.hashCode()) {
                                                                case -905838985:
                                                                    map6 = map3;
                                                                    str19 = str11;
                                                                    if (str15.equals(obj9)) {
                                                                        Object readValue2 = pikashowProvider.mapper.readValue(niceResponse2.getText(), new PikashowProvider$loadLinks$$inlined$readValue$2());
                                                                        if (!(readValue2 instanceof PikashowSeriesResponse)) {
                                                                            throw new RuntimeJsonMappingException(str13 + Reflection.getOrCreateKotlinClass(PikashowSeriesResponse.class).getQualifiedName() + "(non-null)" + str19 + (readValue2 != null ? Reflection.getOrCreateKotlinClass(readValue2.getClass()).getQualifiedName() : pikashowSeries));
                                                                        }
                                                                        PikashowSeriesResponse pikashowSeriesResponse = (PikashowSeriesResponse) readValue2;
                                                                        List<PikashowSeries> series = pikashowSeriesResponse.getSeries();
                                                                        if (series != null) {
                                                                            Iterator<T> it = series.iterator();
                                                                            while (true) {
                                                                                if (it.hasNext()) {
                                                                                    Object next = it.next();
                                                                                    PikashowSeriesResponse pikashowSeriesResponse2 = pikashowSeriesResponse;
                                                                                    if (Intrinsics.areEqual(((PikashowSeries) next).getTitle(), str17)) {
                                                                                        obj10 = next;
                                                                                    } else {
                                                                                        pikashowSeriesResponse = pikashowSeriesResponse2;
                                                                                    }
                                                                                } else {
                                                                                    obj10 = pikashowSeries;
                                                                                }
                                                                            }
                                                                            pikashowSeries2 = (PikashowSeries) obj10;
                                                                        } else {
                                                                            pikashowSeries2 = pikashowSeries;
                                                                        }
                                                                        if (pikashowSeries2 != null) {
                                                                            objectRef3.element = obj7;
                                                                            objectRef4.element = pikashowSeries2.getTitle();
                                                                            Unit unit = Unit.INSTANCE;
                                                                        }
                                                                    }
                                                                    if (objectRef3.element == null && objectRef4.element != null) {
                                                                        String str37 = (String) objectRef4.element;
                                                                        String str38 = pikashowProvider.getMainUrl() + str12;
                                                                        Pair[] pairArr = new Pair[5];
                                                                        pairArr[0] = TuplesKt.to(obj8, str15);
                                                                        String str39 = str17;
                                                                        pairArr[z4 ? 1 : 0] = TuplesKt.to(obj6, objectRef3.element);
                                                                        pairArr[2] = TuplesKt.to(obj4, str37);
                                                                        pairArr[c] = TuplesKt.to(obj5, "1");
                                                                        pairArr[4] = TuplesKt.to(obj3, obj7);
                                                                        Map mapOf3 = MapsKt.mapOf(pairArr);
                                                                        Requests app3 = MainActivityKt.getApp();
                                                                        pikashowProvider$loadLinks$14.L$0 = SpillingKt.nullOutSpilledVariable(str18);
                                                                        pikashowProvider$loadLinks$14.L$1 = SpillingKt.nullOutSpilledVariable(function15);
                                                                        pikashowProvider$loadLinks$14.L$2 = function16;
                                                                        pikashowProvider$loadLinks$14.L$3 = SpillingKt.nullOutSpilledVariable(str16);
                                                                        pikashowProvider$loadLinks$14.L$4 = SpillingKt.nullOutSpilledVariable(map6);
                                                                        pikashowProvider$loadLinks$14.L$5 = SpillingKt.nullOutSpilledVariable(list2);
                                                                        pikashowProvider$loadLinks$14.L$6 = SpillingKt.nullOutSpilledVariable(str39);
                                                                        pikashowProvider$loadLinks$14.L$7 = SpillingKt.nullOutSpilledVariable(str15);
                                                                        pikashowProvider$loadLinks$14.L$8 = SpillingKt.nullOutSpilledVariable(str14);
                                                                        pikashowProvider$loadLinks$14.L$9 = SpillingKt.nullOutSpilledVariable(map4);
                                                                        pikashowProvider$loadLinks$14.L$10 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                                                                        pikashowProvider$loadLinks$14.L$11 = SpillingKt.nullOutSpilledVariable(objectRef3);
                                                                        pikashowProvider$loadLinks$14.L$12 = SpillingKt.nullOutSpilledVariable(objectRef4);
                                                                        pikashowProvider$loadLinks$14.L$13 = str37;
                                                                        pikashowProvider$loadLinks$14.L$14 = SpillingKt.nullOutSpilledVariable(str38);
                                                                        pikashowProvider$loadLinks$14.L$15 = SpillingKt.nullOutSpilledVariable(mapOf3);
                                                                        pikashowProvider$loadLinks$14.Z$0 = z5;
                                                                        pikashowProvider$loadLinks$14.label = 4;
                                                                        PikashowProvider$loadLinks$1 pikashowProvider$loadLinks$16 = pikashowProvider$loadLinks$14;
                                                                        Function1<? super ExtractorLink, Unit> function19 = function16;
                                                                        boolean z8 = z5;
                                                                        str21 = str39;
                                                                        Map<String, String> map10 = map6;
                                                                        String str40 = str15;
                                                                        map7 = mapOf3;
                                                                        str22 = str19;
                                                                        try {
                                                                            Object obj17 = Requests.get$default(app3, str38, map10, (String) null, map7, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, pikashowProvider$loadLinks$16, 4084, (Object) null);
                                                                            pikashowProvider$loadLinks$15 = pikashowProvider$loadLinks$16;
                                                                            if (obj17 == obj2) {
                                                                                return obj2;
                                                                            }
                                                                            str23 = str40;
                                                                            str24 = str37;
                                                                            obj14 = obj17;
                                                                            map8 = map10;
                                                                            str25 = str38;
                                                                            objectRef = objectRef3;
                                                                            objectRef2 = objectRef4;
                                                                            z6 = z8;
                                                                            function17 = function19;
                                                                            str26 = str16;
                                                                            str27 = str14;
                                                                            try {
                                                                                niceResponse3 = (NiceResponse) obj14;
                                                                                if (niceResponse3.getCode() == 404) {
                                                                                    Map map11 = map7;
                                                                                    Object readValue3 = pikashowProvider.mapper.readValue(niceResponse3.getText(), new PikashowProvider$loadLinks$$inlined$readValue$4());
                                                                                    if (!(readValue3 instanceof VideoApiResponse)) {
                                                                                        throw new RuntimeJsonMappingException(str13 + Reflection.getOrCreateKotlinClass(VideoApiResponse.class).getQualifiedName() + "(non-null)" + str22 + (readValue3 != null ? Reflection.getOrCreateKotlinClass(readValue3.getClass()).getQualifiedName() : pikashowSeries));
                                                                                    }
                                                                                    VideoApiResponse videoApiResponse2 = (VideoApiResponse) readValue3;
                                                                                    String str41 = str24;
                                                                                    VideoData data2 = videoApiResponse2.getData();
                                                                                    if (data2 != null) {
                                                                                        pikashowProvider$loadLinks$15.L$0 = SpillingKt.nullOutSpilledVariable(str18);
                                                                                        pikashowProvider$loadLinks$15.L$1 = SpillingKt.nullOutSpilledVariable(function15);
                                                                                        pikashowProvider$loadLinks$15.L$2 = SpillingKt.nullOutSpilledVariable(function17);
                                                                                        pikashowProvider$loadLinks$15.L$3 = SpillingKt.nullOutSpilledVariable(str26);
                                                                                        pikashowProvider$loadLinks$15.L$4 = SpillingKt.nullOutSpilledVariable(map8);
                                                                                        pikashowProvider$loadLinks$15.L$5 = SpillingKt.nullOutSpilledVariable(list2);
                                                                                        pikashowProvider$loadLinks$15.L$6 = SpillingKt.nullOutSpilledVariable(str21);
                                                                                        pikashowProvider$loadLinks$15.L$7 = SpillingKt.nullOutSpilledVariable(str23);
                                                                                        pikashowProvider$loadLinks$15.L$8 = SpillingKt.nullOutSpilledVariable(str27);
                                                                                        pikashowProvider$loadLinks$15.L$9 = SpillingKt.nullOutSpilledVariable(map4);
                                                                                        pikashowProvider$loadLinks$15.L$10 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                                                                                        pikashowProvider$loadLinks$15.L$11 = SpillingKt.nullOutSpilledVariable(objectRef);
                                                                                        pikashowProvider$loadLinks$15.L$12 = SpillingKt.nullOutSpilledVariable(objectRef2);
                                                                                        pikashowProvider$loadLinks$15.L$13 = SpillingKt.nullOutSpilledVariable(str24);
                                                                                        pikashowProvider$loadLinks$15.L$14 = SpillingKt.nullOutSpilledVariable(str25);
                                                                                        pikashowProvider$loadLinks$15.L$15 = SpillingKt.nullOutSpilledVariable(map11);
                                                                                        pikashowProvider$loadLinks$15.L$16 = SpillingKt.nullOutSpilledVariable(niceResponse3);
                                                                                        pikashowProvider$loadLinks$15.L$17 = SpillingKt.nullOutSpilledVariable(videoApiResponse2);
                                                                                        pikashowProvider$loadLinks$15.L$18 = SpillingKt.nullOutSpilledVariable(str41);
                                                                                        pikashowProvider$loadLinks$15.L$19 = SpillingKt.nullOutSpilledVariable(data2);
                                                                                        pikashowProvider$loadLinks$15.Z$0 = z6;
                                                                                        pikashowProvider$loadLinks$15.I$0 = 0;
                                                                                        pikashowProvider$loadLinks$15.label = 5;
                                                                                        if (pikashowProvider.addVideoLinksToCallback(data2, function17, str41, pikashowProvider$loadLinks$15) == obj2) {
                                                                                            return obj2;
                                                                                        }
                                                                                        str28 = str18;
                                                                                        try {
                                                                                            return Boxing.boxBoolean(z4);
                                                                                        } catch (Exception e7) {
                                                                                            e = e7;
                                                                                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                                                                                            return Boxing.boxBoolean(false);
                                                                                        }
                                                                                    }
                                                                                }
                                                                                z3 = z6;
                                                                                function14 = function17;
                                                                                return Boxing.boxBoolean(false);
                                                                            } catch (Exception e8) {
                                                                                e = e8;
                                                                                System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                                                                                return Boxing.boxBoolean(false);
                                                                            }
                                                                        } catch (Exception e9) {
                                                                            e = e9;
                                                                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                                                                            return Boxing.boxBoolean(false);
                                                                        }
                                                                    }
                                                                    z3 = z5;
                                                                    function14 = function16;
                                                                    break;
                                                                case -584959943:
                                                                    break;
                                                                case 1455215167:
                                                                    if (!str15.equals("hollywood")) {
                                                                        map6 = map3;
                                                                        str19 = str11;
                                                                        if (objectRef3.element == null) {
                                                                            break;
                                                                        }
                                                                        z3 = z5;
                                                                        function14 = function16;
                                                                        break;
                                                                    }
                                                                    map6 = map3;
                                                                    Object readValue4 = pikashowProvider.mapper.readValue(niceResponse2.getText(), new PikashowProvider$loadLinks$$inlined$readValue$3());
                                                                    if (!(readValue4 instanceof PikashowMovieResponse)) {
                                                                        throw new RuntimeJsonMappingException(str13 + Reflection.getOrCreateKotlinClass(PikashowMovieResponse.class).getQualifiedName() + "(non-null)" + str11 + (readValue4 != null ? Reflection.getOrCreateKotlinClass(readValue4.getClass()).getQualifiedName() : pikashowSeries));
                                                                    }
                                                                    String str42 = str11;
                                                                    List<PikashowMovie> records = ((PikashowMovieResponse) readValue4).getRecords();
                                                                    if (records != null) {
                                                                        Iterator<T> it2 = records.iterator();
                                                                        while (true) {
                                                                            if (it2.hasNext()) {
                                                                                obj12 = it2.next();
                                                                                str20 = str42;
                                                                                if (!Intrinsics.areEqual(String.valueOf(((PikashowMovie) obj12).getSortOrder()), str17)) {
                                                                                    str42 = str20;
                                                                                }
                                                                            } else {
                                                                                str20 = str42;
                                                                                obj12 = pikashowSeries;
                                                                            }
                                                                        }
                                                                        obj11 = (PikashowMovie) obj12;
                                                                    } else {
                                                                        str20 = str42;
                                                                        obj11 = pikashowSeries;
                                                                    }
                                                                    if (obj11 != null) {
                                                                        PikashowMovie pikashowMovie = obj11;
                                                                        objectRef3.element = String.valueOf(pikashowMovie.getSortOrder());
                                                                        objectRef4.element = pikashowMovie.getTitle();
                                                                        Unit unit2 = Unit.INSTANCE;
                                                                    }
                                                                    str19 = str20;
                                                                    if (objectRef3.element == null) {
                                                                    }
                                                                    z3 = z5;
                                                                    function14 = function16;
                                                                default:
                                                                    map6 = map3;
                                                                    str19 = str11;
                                                                    if (objectRef3.element == null) {
                                                                    }
                                                                    z3 = z5;
                                                                    function14 = function16;
                                                                    break;
                                                            }
                                                        } catch (Exception e10) {
                                                            e = e10;
                                                        }
                                                    } else {
                                                        z3 = z5;
                                                        function14 = function16;
                                                    }
                                                    return Boxing.boxBoolean(false);
                                                } catch (Exception e11) {
                                                    e = e11;
                                                }
                                            } catch (Exception e12) {
                                                e = e12;
                                                System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                                                return Boxing.boxBoolean(false);
                                            }
                                        }
                                    }
                                } catch (Exception e13) {
                                    e = e13;
                                }
                            }
                            return Boxing.boxBoolean(false);
                        } catch (Exception e14) {
                            e = e14;
                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                            return Boxing.boxBoolean(false);
                        }
                        z3 = z;
                        function14 = function12;
                    case 1:
                        z7 = pikashowProvider$loadLinks$12.Z$0;
                        Map map12 = (Map) pikashowProvider$loadLinks$12.L$10;
                        String str43 = (String) pikashowProvider$loadLinks$12.L$9;
                        String str44 = (String) pikashowProvider$loadLinks$12.L$8;
                        String str45 = (String) pikashowProvider$loadLinks$12.L$7;
                        String str46 = (String) pikashowProvider$loadLinks$12.L$6;
                        List list3 = (List) pikashowProvider$loadLinks$12.L$5;
                        Map<String, String> map13 = (Map) pikashowProvider$loadLinks$12.L$4;
                        String str47 = (String) pikashowProvider$loadLinks$12.L$3;
                        function18 = (Function1) pikashowProvider$loadLinks$12.L$2;
                        function13 = (Function1) pikashowProvider$loadLinks$12.L$1;
                        str7 = (String) pikashowProvider$loadLinks$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj14);
                            str2 = " but was ";
                            str9 = str44;
                            str3 = str47;
                            str4 = "Deserialized value did not match the specified type; specified ";
                            z2 = true;
                            str5 = null;
                            map2 = map12;
                            list = list3;
                            pikashowProvider$loadLinks$13 = pikashowProvider$loadLinks$12;
                            str10 = str45;
                            str6 = str43;
                            map = map13;
                            str8 = str46;
                            obj = coroutine_suspended;
                            i = 200;
                            niceResponse = (NiceResponse) obj14;
                            if (niceResponse.getCode() != i) {
                            }
                            z3 = z7;
                            function14 = function18;
                            return Boxing.boxBoolean(false);
                        } catch (Exception e15) {
                            e = e15;
                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                            return Boxing.boxBoolean(false);
                        }
                    case 2:
                        int i2 = pikashowProvider$loadLinks$12.I$0;
                        boolean z9 = pikashowProvider$loadLinks$12.Z$0;
                        try {
                            ResultKt.throwOnFailure(obj14);
                            z2 = true;
                        } catch (Exception e16) {
                            e = e16;
                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                            return Boxing.boxBoolean(false);
                        }
                    case 3:
                        boolean z10 = pikashowProvider$loadLinks$12.Z$0;
                        Map map14 = (Map) pikashowProvider$loadLinks$12.L$9;
                        String str48 = (String) pikashowProvider$loadLinks$12.L$8;
                        String str49 = (String) pikashowProvider$loadLinks$12.L$7;
                        String str50 = (String) pikashowProvider$loadLinks$12.L$6;
                        List list4 = (List) pikashowProvider$loadLinks$12.L$5;
                        Map<String, String> map15 = (Map) pikashowProvider$loadLinks$12.L$4;
                        String str51 = (String) pikashowProvider$loadLinks$12.L$3;
                        Function1<? super ExtractorLink, Unit> function110 = (Function1) pikashowProvider$loadLinks$12.L$2;
                        Function1<? super SubtitleFile, Unit> function111 = (Function1) pikashowProvider$loadLinks$12.L$1;
                        String str52 = (String) pikashowProvider$loadLinks$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj14);
                            str11 = " but was ";
                            map4 = map14;
                            str14 = str48;
                            obj3 = "noepisodes";
                            obj5 = "noseasons";
                            obj4 = "title";
                            str12 = "/v1/api/video";
                            obj9 = "series";
                            obj8 = "type";
                            str15 = str49;
                            str17 = str50;
                            map3 = map15;
                            str16 = str51;
                            z4 = true;
                            pikashowSeries = null;
                            str18 = str52;
                            function15 = function111;
                            pikashowProvider$loadLinks$14 = pikashowProvider$loadLinks$12;
                            obj6 = "videoId";
                            str13 = "Deserialized value did not match the specified type; specified ";
                            list2 = list4;
                            c = 3;
                            pikashowProvider = this;
                            obj2 = coroutine_suspended;
                            obj7 = "0";
                            function16 = function110;
                            z5 = z10;
                            niceResponse2 = (NiceResponse) obj14;
                            if (niceResponse2.getCode() != 200) {
                            }
                            return Boxing.boxBoolean(false);
                        } catch (Exception e17) {
                            e = e17;
                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                            return Boxing.boxBoolean(false);
                        }
                    case 4:
                        boolean z11 = pikashowProvider$loadLinks$12.Z$0;
                        Map map16 = (Map) pikashowProvider$loadLinks$12.L$15;
                        String str53 = (String) pikashowProvider$loadLinks$12.L$14;
                        String str54 = (String) pikashowProvider$loadLinks$12.L$13;
                        Ref.ObjectRef objectRef5 = (Ref.ObjectRef) pikashowProvider$loadLinks$12.L$12;
                        Ref.ObjectRef objectRef6 = (Ref.ObjectRef) pikashowProvider$loadLinks$12.L$11;
                        NiceResponse niceResponse4 = (NiceResponse) pikashowProvider$loadLinks$12.L$10;
                        Map map17 = (Map) pikashowProvider$loadLinks$12.L$9;
                        str27 = (String) pikashowProvider$loadLinks$12.L$8;
                        str23 = (String) pikashowProvider$loadLinks$12.L$7;
                        String str55 = (String) pikashowProvider$loadLinks$12.L$6;
                        List list5 = (List) pikashowProvider$loadLinks$12.L$5;
                        map8 = (Map) pikashowProvider$loadLinks$12.L$4;
                        str26 = (String) pikashowProvider$loadLinks$12.L$3;
                        Function1<? super ExtractorLink, Unit> function112 = (Function1) pikashowProvider$loadLinks$12.L$2;
                        Function1<? super SubtitleFile, Unit> function113 = (Function1) pikashowProvider$loadLinks$12.L$1;
                        String str56 = (String) pikashowProvider$loadLinks$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj14);
                            list2 = list5;
                            str18 = str56;
                            function15 = function113;
                            str21 = str55;
                            niceResponse2 = niceResponse4;
                            map4 = map17;
                            str13 = "Deserialized value did not match the specified type; specified ";
                            z4 = true;
                            pikashowSeries = null;
                            pikashowProvider = this;
                            pikashowProvider$loadLinks$15 = pikashowProvider$loadLinks$12;
                            objectRef2 = objectRef5;
                            objectRef = objectRef6;
                            function17 = function112;
                            obj2 = coroutine_suspended;
                            str22 = " but was ";
                            str25 = str53;
                            str24 = str54;
                            z6 = z11;
                            map7 = map16;
                            niceResponse3 = (NiceResponse) obj14;
                            if (niceResponse3.getCode() == 404) {
                            }
                            z3 = z6;
                            function14 = function17;
                            return Boxing.boxBoolean(false);
                        } catch (Exception e18) {
                            e = e18;
                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                            return Boxing.boxBoolean(false);
                        }
                    case 5:
                        int i3 = pikashowProvider$loadLinks$12.I$0;
                        boolean z12 = pikashowProvider$loadLinks$12.Z$0;
                        str28 = (String) pikashowProvider$loadLinks$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj14);
                            z6 = z12;
                            z4 = true;
                            return Boxing.boxBoolean(z4);
                        } catch (Exception e19) {
                            e = e19;
                            System.out.println((Object) ("Error in loadLinks: " + e.getMessage()));
                            return Boxing.boxBoolean(false);
                        }
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        pikashowProvider$loadLinks$1 = new PikashowProvider$loadLinks$1(this, continuation);
        pikashowProvider$loadLinks$12 = pikashowProvider$loadLinks$1;
        Object obj132 = pikashowProvider$loadLinks$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        Object obj142 = obj132;
        switch (pikashowProvider$loadLinks$12.label) {
        }
    }

    /*  JADX ERROR: Types fix failed
        jadx.core.utils.exceptions.JadxOverflowException: Type inference error: updates count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:59)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:19)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:96)
        */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:46:0x01da -> B:15:0x01f2). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:52:0x079a -> B:21:0x07a4). Please report as a decompilation issue!!! */
    @org.jetbrains.annotations.Nullable
    public java.lang.Object search(@org.jetbrains.annotations.NotNull java.lang.String r50, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.util.List<? extends com.lagradost.cloudstream3.SearchResponse>> r51) {
        /*
            Method dump skipped, instructions count: 2114
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.cncverse.PikashowProvider.search(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
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
