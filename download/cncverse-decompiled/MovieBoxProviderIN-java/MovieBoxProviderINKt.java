package com.cncverse;

import com.fasterxml.jackson.databind.JsonNode;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.TvType;
import com.lagradost.cloudstream3.utils.Qualities;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.random.Random;
import kotlin.ranges.RangesKt;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: MovieBoxProviderIN.kt */
@Metadata(d1 = {"\u00008\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\u001a\u0015\u0010\u0000\u001a\u0004\u0018\u00010\u00012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004\u001a\u0010\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0003H\u0002\u001a:\u0010\u0007\u001a\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u0001\u0012\u0006\u0012\u0004\u0018\u00010\u00030\b2\u0006\u0010\t\u001a\u00020\u00032\b\u0010\n\u001a\u0004\u0018\u00010\u00012\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0082@¢\u0006\u0002\u0010\r\u001a:\u0010\u000e\u001a\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u0001\u0012\u0006\u0012\u0004\u0018\u00010\u00030\b2\u0006\u0010\u000f\u001a\u00020\u00032\b\u0010\n\u001a\u0004\u0018\u00010\u00012\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0082@¢\u0006\u0002\u0010\r\u001a\u0018\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00032\u0006\u0010\u0013\u001a\u00020\u0003H\u0002\u001a\u0010\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0003H\u0002\u001a\"\u0010\u0015\u001a\u0004\u0018\u00010\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0018\u001a\u00020\u0019H\u0082@¢\u0006\u0002\u0010\u001a\u001a<\u0010\u001b\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u001d\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u00192\b\u0010\u001e\u001a\u0004\u0018\u00010\u00012\b\u0010\u001f\u001a\u0004\u0018\u00010\u0003H\u0086@¢\u0006\u0002\u0010 ¨\u0006!"}, d2 = {"getHighestQuality", "", "input", "", "(Ljava/lang/String;)Ljava/lang/Integer;", "cleanTitle", "s", "identifyID", "Lkotlin/Pair;", "title", "year", "imdbRatingValue", "", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Double;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchAndPick", "normTitle", "tokenEquals", "", "a", "b", "normalize", "fetchMetaData", "Lcom/fasterxml/jackson/databind/JsonNode;", "imdbId", "type", "Lcom/lagradost/cloudstream3/TvType;", "(Ljava/lang/String;Lcom/lagradost/cloudstream3/TvType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchTmdbLogoUrl", "tmdbAPI", "apiKey", "tmdbId", "appLangCode", "(Ljava/lang/String;Ljava/lang/String;Lcom/lagradost/cloudstream3/TvType;Ljava/lang/Integer;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "MovieBoxProviderIN_debug"}, k = 2, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nMovieBoxProviderIN.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MovieBoxProviderIN.kt\ncom/cncverse/MovieBoxProviderINKt\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,1181:1\n777#2:1182\n873#2,2:1183\n777#2:1185\n873#2,2:1186\n777#2:1188\n873#2,2:1189\n1#3:1191\n*S KotlinDebug\n*F\n+ 1 MovieBoxProviderIN.kt\ncom/cncverse/MovieBoxProviderINKt\n*L\n1014#1:1182\n1014#1:1183,2\n1073#1:1185\n1073#1:1186,2\n1074#1:1188\n1074#1:1189,2\n*E\n"})
/* loaded from: classes.dex */
public final class MovieBoxProviderINKt {
    private static final String cleanTitle(String str) {
        String lowerCase = str.toLowerCase(Locale.ROOT);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
        return StringsKt.trim(new Regex("\\s+").replace(new Regex("[^a-z0-9 ]").replace(lowerCase, " "), " ")).toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:25:0x0059  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final Object fetchMetaData(String str, TvType tvType, Continuation<? super JsonNode> continuation) {
        MovieBoxProviderINKt$fetchMetaData$1 movieBoxProviderINKt$fetchMetaData$1;
        MovieBoxProviderINKt$fetchMetaData$1 movieBoxProviderINKt$fetchMetaData$12;
        try {
            if (continuation instanceof MovieBoxProviderINKt$fetchMetaData$1) {
                movieBoxProviderINKt$fetchMetaData$1 = (MovieBoxProviderINKt$fetchMetaData$1) continuation;
                if ((movieBoxProviderINKt$fetchMetaData$1.label & Integer.MIN_VALUE) != 0) {
                    movieBoxProviderINKt$fetchMetaData$1.label -= Integer.MIN_VALUE;
                    movieBoxProviderINKt$fetchMetaData$12 = movieBoxProviderINKt$fetchMetaData$1;
                    Object obj = movieBoxProviderINKt$fetchMetaData$12.result;
                    Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                    switch (movieBoxProviderINKt$fetchMetaData$12.label) {
                        case 0:
                            ResultKt.throwOnFailure(obj);
                            String str2 = str;
                            if (str2 == null || StringsKt.isBlank(str2)) {
                                return null;
                            }
                            String str3 = tvType == TvType.TvSeries ? "series" : "movie";
                            String str4 = "https://v3-cinemeta.strem.io/meta/" + str3 + "/" + str + ".json";
                            try {
                                Requests app = MainActivityKt.getApp();
                                movieBoxProviderINKt$fetchMetaData$12.L$0 = SpillingKt.nullOutSpilledVariable(str);
                                movieBoxProviderINKt$fetchMetaData$12.L$1 = SpillingKt.nullOutSpilledVariable(tvType);
                                movieBoxProviderINKt$fetchMetaData$12.L$2 = SpillingKt.nullOutSpilledVariable(str3);
                                movieBoxProviderINKt$fetchMetaData$12.L$3 = SpillingKt.nullOutSpilledVariable(str4);
                                movieBoxProviderINKt$fetchMetaData$12.label = 1;
                                try {
                                    Object obj2 = Requests.get$default(app, str4, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderINKt$fetchMetaData$12, 4094, (Object) null);
                                    if (obj2 != coroutine_suspended) {
                                        obj = obj2;
                                        break;
                                    } else {
                                        return coroutine_suspended;
                                    }
                                } catch (Exception e) {
                                    return null;
                                }
                            } catch (Exception e2) {
                                return null;
                            }
                            break;
                        case 1:
                            try {
                                ResultKt.throwOnFailure(obj);
                                break;
                            } catch (Exception e3) {
                                return null;
                            }
                        default:
                            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    return MainAPIKt.getMapper().readTree(((NiceResponse) obj).getText()).get("meta");
                }
            }
            return MainAPIKt.getMapper().readTree(((NiceResponse) obj).getText()).get("meta");
        } catch (Exception e4) {
            return null;
        }
        movieBoxProviderINKt$fetchMetaData$1 = new MovieBoxProviderINKt$fetchMetaData$1(continuation);
        movieBoxProviderINKt$fetchMetaData$12 = movieBoxProviderINKt$fetchMetaData$1;
        Object obj3 = movieBoxProviderINKt$fetchMetaData$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderINKt$fetchMetaData$12.label) {
        }
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002c. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0039  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x014d  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0153 A[ADDED_TO_REGION, RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0154  */
    /* JADX WARN: Removed duplicated region for block: B:36:0x0189  */
    /* JADX WARN: Removed duplicated region for block: B:60:0x01e6  */
    /* JADX WARN: Removed duplicated region for block: B:62:0x01ed  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002f  */
    /* JADX WARN: Removed duplicated region for block: B:98:0x0066  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final Object fetchTmdbLogoUrl(@NotNull String str, @NotNull String str2, @NotNull TvType tvType, @Nullable Integer num, @Nullable String str3, @NotNull Continuation<? super String> continuation) {
        MovieBoxProviderINKt$fetchTmdbLogoUrl$1 movieBoxProviderINKt$fetchTmdbLogoUrl$1;
        MovieBoxProviderINKt$fetchTmdbLogoUrl$1 movieBoxProviderINKt$fetchTmdbLogoUrl$12;
        String str4;
        Requests app;
        Object obj;
        Object obj2;
        JSONObject jSONObject;
        JSONArray optJSONArray;
        String str5;
        JSONObject jSONObject2;
        int i;
        int length;
        JSONObject jSONObject3;
        String str6;
        String obj3;
        String str7 = str;
        if (continuation instanceof MovieBoxProviderINKt$fetchTmdbLogoUrl$1) {
            movieBoxProviderINKt$fetchTmdbLogoUrl$1 = (MovieBoxProviderINKt$fetchTmdbLogoUrl$1) continuation;
            if ((movieBoxProviderINKt$fetchTmdbLogoUrl$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderINKt$fetchTmdbLogoUrl$1.label -= Integer.MIN_VALUE;
                movieBoxProviderINKt$fetchTmdbLogoUrl$12 = movieBoxProviderINKt$fetchTmdbLogoUrl$1;
                Object obj4 = movieBoxProviderINKt$fetchTmdbLogoUrl$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProviderINKt$fetchTmdbLogoUrl$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj4);
                        if (num == null) {
                            return null;
                        }
                        String str8 = tvType == TvType.Movie ? str7 + "/movie/" + num + "/images?api_key=" + str2 : str7 + "/tv/" + num + "/images?api_key=" + str2;
                        try {
                            Result.Companion companion = Result.Companion;
                            app = MainActivityKt.getApp();
                            movieBoxProviderINKt$fetchTmdbLogoUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(str7);
                            movieBoxProviderINKt$fetchTmdbLogoUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                            movieBoxProviderINKt$fetchTmdbLogoUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(tvType);
                            movieBoxProviderINKt$fetchTmdbLogoUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(num);
                            movieBoxProviderINKt$fetchTmdbLogoUrl$12.L$4 = str3;
                            movieBoxProviderINKt$fetchTmdbLogoUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(str8);
                            movieBoxProviderINKt$fetchTmdbLogoUrl$12.I$0 = 0;
                            movieBoxProviderINKt$fetchTmdbLogoUrl$12.label = 1;
                        } catch (Throwable th) {
                            th = th;
                            str4 = str3;
                        }
                        try {
                            obj = Requests.get$default(app, str8, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderINKt$fetchTmdbLogoUrl$12, 4094, (Object) null);
                        } catch (Throwable th2) {
                            th = th2;
                            str4 = str3;
                            Result.Companion companion2 = Result.Companion;
                            obj2 = Result.constructor-impl(ResultKt.createFailure(th));
                            if (Result.isFailure-impl(obj2)) {
                            }
                            jSONObject = (JSONObject) obj2;
                            if (jSONObject == null) {
                            }
                        }
                        if (obj == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        str4 = str3;
                        obj4 = obj;
                        try {
                            obj2 = Result.constructor-impl(new JSONObject(((NiceResponse) obj4).getText()));
                        } catch (Throwable th3) {
                            th = th3;
                            Result.Companion companion22 = Result.Companion;
                            obj2 = Result.constructor-impl(ResultKt.createFailure(th));
                            if (Result.isFailure-impl(obj2)) {
                            }
                            jSONObject = (JSONObject) obj2;
                            if (jSONObject == null) {
                            }
                        }
                        if (Result.isFailure-impl(obj2)) {
                            obj2 = null;
                        }
                        jSONObject = (JSONObject) obj2;
                        if (jSONObject == null || (optJSONArray = jSONObject.optJSONArray("logos")) == null || optJSONArray.length() == 0) {
                            return null;
                        }
                        if (str4 != null || (obj3 = StringsKt.trim(str4).toString()) == null) {
                            str5 = null;
                        } else {
                            str5 = obj3.toLowerCase(Locale.ROOT);
                            Intrinsics.checkNotNullExpressionValue(str5, "toLowerCase(...)");
                        }
                        jSONObject2 = null;
                        i = 0;
                        length = optJSONArray.length();
                        while (i < length) {
                            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                            if (optJSONObject == null) {
                                jSONObject3 = jSONObject;
                                str6 = str7;
                            } else if (StringsKt.isBlank(optJSONObject.optString("file_path"))) {
                                jSONObject3 = jSONObject;
                                str6 = str7;
                            } else {
                                jSONObject3 = jSONObject;
                                str6 = str7;
                                String lowerCase = StringsKt.trim(optJSONObject.optString("iso_639_1")).toString().toLowerCase(Locale.ROOT);
                                Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
                                if (!Intrinsics.areEqual(lowerCase, str5)) {
                                    continue;
                                } else {
                                    if (!fetchTmdbLogoUrl$isSvg(optJSONObject)) {
                                        return fetchTmdbLogoUrl$urlOf(optJSONObject);
                                    }
                                    if (jSONObject2 == null) {
                                        jSONObject2 = optJSONObject;
                                    }
                                }
                            }
                            i++;
                            jSONObject = jSONObject3;
                            str7 = str6;
                        }
                        if (jSONObject2 == null) {
                            return fetchTmdbLogoUrl$urlOf(jSONObject2);
                        }
                        JSONObject jSONObject4 = null;
                        JSONObject jSONObject5 = null;
                        int length2 = optJSONArray.length();
                        for (int i2 = 0; i2 < length2; i2++) {
                            JSONObject optJSONObject2 = optJSONArray.optJSONObject(i2);
                            if (optJSONObject2 != null && fetchTmdbLogoUrl$voted(optJSONObject2)) {
                                if (fetchTmdbLogoUrl$isSvg(optJSONObject2)) {
                                    if (fetchTmdbLogoUrl$better(jSONObject5, optJSONObject2)) {
                                        jSONObject5 = optJSONObject2;
                                    }
                                } else if (fetchTmdbLogoUrl$better(jSONObject4, optJSONObject2)) {
                                    jSONObject4 = optJSONObject2;
                                }
                            }
                        }
                        if (jSONObject4 != null) {
                            return fetchTmdbLogoUrl$urlOf(jSONObject4);
                        }
                        if (jSONObject5 != null) {
                            return fetchTmdbLogoUrl$urlOf(jSONObject5);
                        }
                        return null;
                    case 1:
                        int i3 = movieBoxProviderINKt$fetchTmdbLogoUrl$12.I$0;
                        str4 = (String) movieBoxProviderINKt$fetchTmdbLogoUrl$12.L$4;
                        str7 = (String) movieBoxProviderINKt$fetchTmdbLogoUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj4);
                            obj2 = Result.constructor-impl(new JSONObject(((NiceResponse) obj4).getText()));
                        } catch (Throwable th4) {
                            th = th4;
                            Result.Companion companion222 = Result.Companion;
                            obj2 = Result.constructor-impl(ResultKt.createFailure(th));
                            if (Result.isFailure-impl(obj2)) {
                            }
                            jSONObject = (JSONObject) obj2;
                            if (jSONObject == null) {
                            }
                        }
                        if (Result.isFailure-impl(obj2)) {
                        }
                        jSONObject = (JSONObject) obj2;
                        if (jSONObject == null) {
                            return null;
                        }
                        if (str4 != null) {
                            break;
                        }
                        str5 = null;
                        jSONObject2 = null;
                        i = 0;
                        length = optJSONArray.length();
                        while (i < length) {
                        }
                        if (jSONObject2 == null) {
                        }
                        break;
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        movieBoxProviderINKt$fetchTmdbLogoUrl$1 = new MovieBoxProviderINKt$fetchTmdbLogoUrl$1(continuation);
        movieBoxProviderINKt$fetchTmdbLogoUrl$12 = movieBoxProviderINKt$fetchTmdbLogoUrl$1;
        Object obj42 = movieBoxProviderINKt$fetchTmdbLogoUrl$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderINKt$fetchTmdbLogoUrl$12.label) {
        }
    }

    private static final boolean fetchTmdbLogoUrl$better(JSONObject jSONObject, JSONObject jSONObject2) {
        if (jSONObject == null) {
            return true;
        }
        double optDouble = jSONObject.optDouble("vote_average", 0.0d);
        int optInt = jSONObject.optInt("vote_count", 0);
        double optDouble2 = jSONObject2.optDouble("vote_average", 0.0d);
        int optInt2 = jSONObject2.optInt("vote_count", 0);
        if (optDouble2 <= optDouble) {
            return ((optDouble2 > optDouble ? 1 : (optDouble2 == optDouble ? 0 : -1)) == 0) && optInt2 > optInt;
        }
        return true;
    }

    private static final boolean fetchTmdbLogoUrl$isSvg(JSONObject jSONObject) {
        return StringsKt.endsWith(jSONObject.optString("file_path"), ".svg", true);
    }

    private static final String fetchTmdbLogoUrl$urlOf(JSONObject jSONObject) {
        return "https://image.tmdb.org/t/p/w500" + jSONObject.optString("file_path");
    }

    private static final boolean fetchTmdbLogoUrl$voted(JSONObject jSONObject) {
        return jSONObject.optDouble("vote_average", 0.0d) > 0.0d && jSONObject.optInt("vote_count", 0) > 0;
    }

    @Nullable
    public static final Integer getHighestQuality(@NotNull String str) {
        for (Pair pair : CollectionsKt.listOf(new Pair[]{TuplesKt.to("2160", Integer.valueOf(Qualities.P2160.getValue())), TuplesKt.to("1440", Integer.valueOf(Qualities.P1440.getValue())), TuplesKt.to("1080", Integer.valueOf(Qualities.P1080.getValue())), TuplesKt.to("720", Integer.valueOf(Qualities.P720.getValue())), TuplesKt.to("480", Integer.valueOf(Qualities.P480.getValue())), TuplesKt.to("360", Integer.valueOf(Qualities.P360.getValue())), TuplesKt.to("240", Integer.valueOf(Qualities.P240.getValue()))})) {
            String str2 = (String) pair.component1();
            int intValue = ((Number) pair.component2()).intValue();
            if (StringsKt.contains(str, str2, true)) {
                return Integer.valueOf(intValue);
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:11:0x002c  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0077 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0078  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0044  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final Object identifyID(String str, Integer num, Double d, Continuation<? super Pair<Integer, String>> continuation) {
        MovieBoxProviderINKt$identifyID$1 movieBoxProviderINKt$identifyID$1;
        Object searchAndPick;
        if (continuation instanceof MovieBoxProviderINKt$identifyID$1) {
            movieBoxProviderINKt$identifyID$1 = (MovieBoxProviderINKt$identifyID$1) continuation;
            if ((movieBoxProviderINKt$identifyID$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderINKt$identifyID$1.label -= Integer.MIN_VALUE;
                Object obj = movieBoxProviderINKt$identifyID$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProviderINKt$identifyID$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        String normalize = normalize(str);
                        movieBoxProviderINKt$identifyID$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        movieBoxProviderINKt$identifyID$1.L$1 = SpillingKt.nullOutSpilledVariable(num);
                        movieBoxProviderINKt$identifyID$1.L$2 = SpillingKt.nullOutSpilledVariable(d);
                        movieBoxProviderINKt$identifyID$1.L$3 = SpillingKt.nullOutSpilledVariable(normalize);
                        movieBoxProviderINKt$identifyID$1.label = 1;
                        searchAndPick = searchAndPick(normalize, num, d, movieBoxProviderINKt$identifyID$1);
                        if (searchAndPick != coroutine_suspended) {
                            break;
                        } else {
                            return coroutine_suspended;
                        }
                    case 1:
                        ResultKt.throwOnFailure(obj);
                        searchAndPick = obj;
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                Pair pair = (Pair) searchAndPick;
                return pair.getFirst() == null ? pair : new Pair((Object) null, (Object) null);
            }
        }
        movieBoxProviderINKt$identifyID$1 = new MovieBoxProviderINKt$identifyID$1(continuation);
        Object obj2 = movieBoxProviderINKt$identifyID$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderINKt$identifyID$1.label) {
        }
        Pair pair2 = (Pair) searchAndPick;
        if (pair2.getFirst() == null) {
        }
    }

    private static final String normalize(String str) {
        String lowerCase = StringsKt.trim(new Regex("(?i)\\b(dub|dubbed|hd|4k|hindi|tamil|telugu|dual audio)\\b").replace(new Regex("\\(.*?\\)").replace(new Regex("\\[.*?]").replace(str, " "), " "), " ")).toString().toLowerCase(Locale.ROOT);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
        return new Regex("\\s+").replace(new Regex("\\p{Punct}").replace(StringsKt.replace$default(lowerCase, ":", " ", false, 4, (Object) null), " "), " ");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x003a. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:107:0x0446  */
    /* JADX WARN: Removed duplicated region for block: B:119:0x0516  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0047  */
    /* JADX WARN: Removed duplicated region for block: B:120:0x00a3  */
    /* JADX WARN: Removed duplicated region for block: B:123:0x01b3  */
    /* JADX WARN: Removed duplicated region for block: B:126:0x01fb A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:127:0x01fc  */
    /* JADX WARN: Removed duplicated region for block: B:128:0x01c5  */
    /* JADX WARN: Removed duplicated region for block: B:129:0x00dc  */
    /* JADX WARN: Removed duplicated region for block: B:132:0x0149  */
    /* JADX WARN: Removed duplicated region for block: B:135:0x019b A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:136:0x019c  */
    /* JADX WARN: Removed duplicated region for block: B:137:0x0161  */
    /* JADX WARN: Removed duplicated region for block: B:138:0x00ef  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x050a  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0077  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0222  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x003d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final Object searchAndPick(String str, Integer num, Double d, Continuation<? super Pair<Integer, String>> continuation) {
        MovieBoxProviderINKt$searchAndPick$1 movieBoxProviderINKt$searchAndPick$1;
        Object obj;
        Double d2;
        JSONArray jSONArray;
        String str2;
        Object searchAndPick$doSearch;
        JSONArray jSONArray2;
        Pair[] pairArr;
        Pair[] pairArr2;
        int i;
        Object obj2;
        Double d3;
        String str3;
        Object searchAndPick$doSearch2;
        Double d4;
        JSONArray jSONArray3;
        String str4;
        Pair[] pairArr3;
        Pair[] pairArr4;
        int i2;
        Iterator it;
        Integer num2;
        Object obj3;
        String str5;
        Integer num3;
        int i3;
        String str6;
        String str7;
        double d5;
        String str8;
        Iterator it2;
        String str9;
        JSONObject optJSONObject;
        String str10 = str;
        Integer num4 = num;
        if (continuation instanceof MovieBoxProviderINKt$searchAndPick$1) {
            movieBoxProviderINKt$searchAndPick$1 = (MovieBoxProviderINKt$searchAndPick$1) continuation;
            if ((movieBoxProviderINKt$searchAndPick$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderINKt$searchAndPick$1.label -= Integer.MIN_VALUE;
                Object obj4 = movieBoxProviderINKt$searchAndPick$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                String str11 = "multi";
                String str12 = "movie";
                String str13 = "";
                switch (movieBoxProviderINKt$searchAndPick$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj4);
                        String str14 = "&query=" + URLEncoder.encode(str10, "UTF-8") + (num4 != null ? "&year=" + num4 : "");
                        movieBoxProviderINKt$searchAndPick$1.L$0 = str10;
                        movieBoxProviderINKt$searchAndPick$1.L$1 = num4;
                        movieBoxProviderINKt$searchAndPick$1.L$2 = d;
                        movieBoxProviderINKt$searchAndPick$1.label = 1;
                        Object searchAndPick$doSearch3 = searchAndPick$doSearch("search/multi", str14, movieBoxProviderINKt$searchAndPick$1);
                        if (searchAndPick$doSearch3 == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        obj = searchAndPick$doSearch3;
                        d2 = d;
                        JSONArray jSONArray4 = (JSONArray) obj;
                        Pair[] pairArr5 = new Pair[3];
                        pairArr5[0] = TuplesKt.to("multi", jSONArray4);
                        String encode = URLEncoder.encode(str10, "UTF-8");
                        if (num4 == null) {
                            jSONArray = jSONArray4;
                            str2 = "&first_air_date_year=" + num4;
                        } else {
                            jSONArray = jSONArray4;
                            str2 = "";
                        }
                        movieBoxProviderINKt$searchAndPick$1.L$0 = str10;
                        movieBoxProviderINKt$searchAndPick$1.L$1 = num4;
                        movieBoxProviderINKt$searchAndPick$1.L$2 = d2;
                        movieBoxProviderINKt$searchAndPick$1.L$3 = SpillingKt.nullOutSpilledVariable(jSONArray);
                        movieBoxProviderINKt$searchAndPick$1.L$4 = pairArr5;
                        movieBoxProviderINKt$searchAndPick$1.L$5 = pairArr5;
                        movieBoxProviderINKt$searchAndPick$1.L$6 = "tv";
                        movieBoxProviderINKt$searchAndPick$1.I$0 = 1;
                        movieBoxProviderINKt$searchAndPick$1.label = 2;
                        searchAndPick$doSearch = searchAndPick$doSearch("search/tv", "&query=" + encode + str2, movieBoxProviderINKt$searchAndPick$1);
                        if (searchAndPick$doSearch != coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        jSONArray2 = jSONArray;
                        pairArr = pairArr5;
                        pairArr2 = pairArr;
                        i = 1;
                        obj2 = searchAndPick$doSearch;
                        d3 = d2;
                        str3 = "tv";
                        pairArr2[i] = TuplesKt.to(str3, obj2);
                        String str15 = "&query=" + URLEncoder.encode(str10, "UTF-8") + (num4 == null ? "&year=" + num4 : "");
                        movieBoxProviderINKt$searchAndPick$1.L$0 = str10;
                        movieBoxProviderINKt$searchAndPick$1.L$1 = num4;
                        movieBoxProviderINKt$searchAndPick$1.L$2 = d3;
                        movieBoxProviderINKt$searchAndPick$1.L$3 = SpillingKt.nullOutSpilledVariable(jSONArray2);
                        movieBoxProviderINKt$searchAndPick$1.L$4 = pairArr;
                        movieBoxProviderINKt$searchAndPick$1.L$5 = pairArr;
                        movieBoxProviderINKt$searchAndPick$1.L$6 = "movie";
                        movieBoxProviderINKt$searchAndPick$1.I$0 = 2;
                        movieBoxProviderINKt$searchAndPick$1.label = 3;
                        searchAndPick$doSearch2 = searchAndPick$doSearch("search/movie", str15, movieBoxProviderINKt$searchAndPick$1);
                        if (searchAndPick$doSearch2 != coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        d4 = d3;
                        jSONArray3 = jSONArray2;
                        str4 = "movie";
                        pairArr3 = pairArr;
                        pairArr4 = pairArr3;
                        i2 = 2;
                        pairArr3[i2] = TuplesKt.to(str4, searchAndPick$doSearch2);
                        List listOf = CollectionsKt.listOf(pairArr4);
                        double d6 = -1.0d;
                        it = listOf.iterator();
                        num2 = null;
                        int i4 = 0;
                        while (it.hasNext()) {
                            Pair pair = (Pair) it.next();
                            String str16 = (String) pair.component1();
                            JSONArray jSONArray5 = (JSONArray) pair.component2();
                            if (jSONArray5 != null) {
                                String str17 = str10;
                                int length = jSONArray5.length();
                                List list = listOf;
                                int i5 = 0;
                                while (i5 < length) {
                                    int i6 = length;
                                    JSONObject jSONObject = jSONArray5.getJSONObject(i5);
                                    if (Intrinsics.areEqual(str16, str11)) {
                                        i3 = i5;
                                        str6 = jSONObject.optString("media_type", str13);
                                    } else {
                                        i3 = i5;
                                        str6 = Intrinsics.areEqual(str16, "tv") ? "tv" : str12;
                                    }
                                    int i7 = i4;
                                    Object obj5 = coroutine_suspended;
                                    int optInt = jSONObject.optInt("id", -1);
                                    if (optInt != -1) {
                                        str7 = str11;
                                        d5 = d6;
                                        List listOf2 = CollectionsKt.listOf(new String[]{jSONObject.optString("title"), jSONObject.optString("name"), jSONObject.optString("original_title"), jSONObject.optString("original_name")});
                                        ArrayList arrayList = new ArrayList();
                                        for (Object obj6 : listOf2) {
                                            List list2 = listOf2;
                                            if (!StringsKt.isBlank((String) obj6)) {
                                                arrayList.add(obj6);
                                            }
                                            listOf2 = list2;
                                        }
                                        ArrayList arrayList2 = arrayList;
                                        Integer intOrNull = StringsKt.toIntOrNull(StringsKt.take(Intrinsics.areEqual(str6, "tv") ? jSONObject.optString("first_air_date", str13) : jSONObject.optString("release_date", str13), 4));
                                        ArrayList arrayList3 = arrayList2;
                                        double optDouble = jSONObject.optDouble("vote_average", Double.NaN);
                                        String cleanTitle = cleanTitle(str17);
                                        Iterator it3 = arrayList3.iterator();
                                        double d7 = 0.0d;
                                        while (true) {
                                            if (it3.hasNext()) {
                                                ArrayList arrayList4 = arrayList3;
                                                String cleanTitle2 = cleanTitle((String) it3.next());
                                                if (tokenEquals(cleanTitle2, cleanTitle)) {
                                                    d7 = 50.0d;
                                                    str8 = str12;
                                                    it2 = it;
                                                    str9 = str13;
                                                } else {
                                                    String str18 = cleanTitle;
                                                    String str19 = str12;
                                                    Iterator it4 = it;
                                                    String str20 = str13;
                                                    if (StringsKt.contains$default(cleanTitle2, str18, false, 2, (Object) null) || StringsKt.contains$default(str18, cleanTitle2, false, 2, (Object) null)) {
                                                        d7 = Math.max(d7, 20.0d);
                                                        it = it4;
                                                        str13 = str20;
                                                        arrayList3 = arrayList4;
                                                        cleanTitle = str18;
                                                        str12 = str19;
                                                    } else {
                                                        it = it4;
                                                        str13 = str20;
                                                        arrayList3 = arrayList4;
                                                        cleanTitle = str18;
                                                        str12 = str19;
                                                    }
                                                }
                                            } else {
                                                str8 = str12;
                                                it2 = it;
                                                str9 = str13;
                                            }
                                        }
                                        double d8 = 0.0d + d7;
                                        if (intOrNull != null && num4 != null && Intrinsics.areEqual(intOrNull, num4)) {
                                            d8 += 35.0d;
                                        }
                                        if (d4 != null && !Double.isNaN(optDouble)) {
                                            double abs = Math.abs(optDouble - d4.doubleValue());
                                            if (abs <= 0.5d) {
                                                d8 += 10.0d;
                                            } else if (abs <= 1.0d) {
                                                d8 += 5.0d;
                                            }
                                        }
                                        if (jSONObject.has("popularity")) {
                                            d8 += RangesKt.coerceAtMost(jSONObject.optDouble("popularity", 0.0d) / 100.0d, 5.0d);
                                        }
                                        if (d8 > d5) {
                                            d6 = d8;
                                            num2 = Boxing.boxInt(optInt);
                                            i4 = Intrinsics.areEqual(str6, "tv") ? 1 : 0;
                                            i5 = i3 + 1;
                                            it = it2;
                                            str13 = str9;
                                            length = i6;
                                            coroutine_suspended = obj5;
                                            str11 = str7;
                                            str12 = str8;
                                        }
                                    } else {
                                        str7 = str11;
                                        d5 = d6;
                                        str8 = str12;
                                        it2 = it;
                                        str9 = str13;
                                    }
                                    i4 = i7;
                                    d6 = d5;
                                    i5 = i3 + 1;
                                    it = it2;
                                    str13 = str9;
                                    length = i6;
                                    coroutine_suspended = obj5;
                                    str11 = str7;
                                    str12 = str8;
                                }
                                str10 = str17;
                                listOf = list;
                            }
                        }
                        String str21 = str10;
                        List list3 = listOf;
                        Object obj7 = coroutine_suspended;
                        String str22 = str12;
                        if (num2 != null) {
                            obj3 = null;
                        } else {
                            if (d6 >= 40.0d) {
                                String str23 = i4 != 0 ? "tv" : str22;
                                String str24 = "https://api.themoviedb.org/3/" + str23 + "/" + num2 + "?api_key=1865f43a0549ca50d341dd9ab8b29f49&append_to_response=external_ids";
                                Requests app = MainActivityKt.getApp();
                                movieBoxProviderINKt$searchAndPick$1.L$0 = SpillingKt.nullOutSpilledVariable(str21);
                                movieBoxProviderINKt$searchAndPick$1.L$1 = SpillingKt.nullOutSpilledVariable(num4);
                                movieBoxProviderINKt$searchAndPick$1.L$2 = SpillingKt.nullOutSpilledVariable(d4);
                                movieBoxProviderINKt$searchAndPick$1.L$3 = SpillingKt.nullOutSpilledVariable(jSONArray3);
                                movieBoxProviderINKt$searchAndPick$1.L$4 = SpillingKt.nullOutSpilledVariable(list3);
                                movieBoxProviderINKt$searchAndPick$1.L$5 = num2;
                                movieBoxProviderINKt$searchAndPick$1.L$6 = SpillingKt.nullOutSpilledVariable(str23);
                                movieBoxProviderINKt$searchAndPick$1.L$7 = SpillingKt.nullOutSpilledVariable(str24);
                                movieBoxProviderINKt$searchAndPick$1.D$0 = d6;
                                movieBoxProviderINKt$searchAndPick$1.I$0 = i4;
                                movieBoxProviderINKt$searchAndPick$1.label = 4;
                                Integer num5 = num2;
                                str5 = null;
                                obj4 = Requests.get$default(app, str24, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderINKt$searchAndPick$1, 4094, (Object) null);
                                if (obj4 == obj7) {
                                    return obj7;
                                }
                                num3 = num5;
                                optJSONObject = new JSONObject(((NiceResponse) obj4).getText()).optJSONObject("external_ids");
                                if (optJSONObject != null) {
                                    str5 = optJSONObject.optString("imdb_id");
                                }
                                return new Pair(num3, str5);
                            }
                            obj3 = null;
                        }
                        return new Pair(obj3, obj3);
                    case 1:
                        d2 = (Double) movieBoxProviderINKt$searchAndPick$1.L$2;
                        num4 = (Integer) movieBoxProviderINKt$searchAndPick$1.L$1;
                        str10 = (String) movieBoxProviderINKt$searchAndPick$1.L$0;
                        ResultKt.throwOnFailure(obj4);
                        obj = obj4;
                        JSONArray jSONArray42 = (JSONArray) obj;
                        Pair[] pairArr52 = new Pair[3];
                        pairArr52[0] = TuplesKt.to("multi", jSONArray42);
                        String encode2 = URLEncoder.encode(str10, "UTF-8");
                        if (num4 == null) {
                        }
                        movieBoxProviderINKt$searchAndPick$1.L$0 = str10;
                        movieBoxProviderINKt$searchAndPick$1.L$1 = num4;
                        movieBoxProviderINKt$searchAndPick$1.L$2 = d2;
                        movieBoxProviderINKt$searchAndPick$1.L$3 = SpillingKt.nullOutSpilledVariable(jSONArray);
                        movieBoxProviderINKt$searchAndPick$1.L$4 = pairArr52;
                        movieBoxProviderINKt$searchAndPick$1.L$5 = pairArr52;
                        movieBoxProviderINKt$searchAndPick$1.L$6 = "tv";
                        movieBoxProviderINKt$searchAndPick$1.I$0 = 1;
                        movieBoxProviderINKt$searchAndPick$1.label = 2;
                        searchAndPick$doSearch = searchAndPick$doSearch("search/tv", "&query=" + encode2 + str2, movieBoxProviderINKt$searchAndPick$1);
                        if (searchAndPick$doSearch != coroutine_suspended) {
                        }
                        break;
                    case 2:
                        int i8 = movieBoxProviderINKt$searchAndPick$1.I$0;
                        String str25 = (String) movieBoxProviderINKt$searchAndPick$1.L$6;
                        Pair[] pairArr6 = (Pair[]) movieBoxProviderINKt$searchAndPick$1.L$5;
                        Pair[] pairArr7 = (Pair[]) movieBoxProviderINKt$searchAndPick$1.L$4;
                        JSONArray jSONArray6 = (JSONArray) movieBoxProviderINKt$searchAndPick$1.L$3;
                        Double d9 = (Double) movieBoxProviderINKt$searchAndPick$1.L$2;
                        num4 = (Integer) movieBoxProviderINKt$searchAndPick$1.L$1;
                        str10 = (String) movieBoxProviderINKt$searchAndPick$1.L$0;
                        ResultKt.throwOnFailure(obj4);
                        pairArr2 = pairArr6;
                        str3 = str25;
                        pairArr = pairArr7;
                        i = i8;
                        jSONArray2 = jSONArray6;
                        obj2 = obj4;
                        d3 = d9;
                        pairArr2[i] = TuplesKt.to(str3, obj2);
                        String str152 = "&query=" + URLEncoder.encode(str10, "UTF-8") + (num4 == null ? "&year=" + num4 : "");
                        movieBoxProviderINKt$searchAndPick$1.L$0 = str10;
                        movieBoxProviderINKt$searchAndPick$1.L$1 = num4;
                        movieBoxProviderINKt$searchAndPick$1.L$2 = d3;
                        movieBoxProviderINKt$searchAndPick$1.L$3 = SpillingKt.nullOutSpilledVariable(jSONArray2);
                        movieBoxProviderINKt$searchAndPick$1.L$4 = pairArr;
                        movieBoxProviderINKt$searchAndPick$1.L$5 = pairArr;
                        movieBoxProviderINKt$searchAndPick$1.L$6 = "movie";
                        movieBoxProviderINKt$searchAndPick$1.I$0 = 2;
                        movieBoxProviderINKt$searchAndPick$1.label = 3;
                        searchAndPick$doSearch2 = searchAndPick$doSearch("search/movie", str152, movieBoxProviderINKt$searchAndPick$1);
                        if (searchAndPick$doSearch2 != coroutine_suspended) {
                        }
                        break;
                    case 3:
                        i2 = movieBoxProviderINKt$searchAndPick$1.I$0;
                        str4 = (String) movieBoxProviderINKt$searchAndPick$1.L$6;
                        pairArr3 = (Pair[]) movieBoxProviderINKt$searchAndPick$1.L$5;
                        pairArr4 = (Pair[]) movieBoxProviderINKt$searchAndPick$1.L$4;
                        JSONArray jSONArray7 = (JSONArray) movieBoxProviderINKt$searchAndPick$1.L$3;
                        Double d10 = (Double) movieBoxProviderINKt$searchAndPick$1.L$2;
                        num4 = (Integer) movieBoxProviderINKt$searchAndPick$1.L$1;
                        str10 = (String) movieBoxProviderINKt$searchAndPick$1.L$0;
                        ResultKt.throwOnFailure(obj4);
                        searchAndPick$doSearch2 = obj4;
                        jSONArray3 = jSONArray7;
                        d4 = d10;
                        pairArr3[i2] = TuplesKt.to(str4, searchAndPick$doSearch2);
                        List listOf3 = CollectionsKt.listOf(pairArr4);
                        double d62 = -1.0d;
                        it = listOf3.iterator();
                        num2 = null;
                        int i42 = 0;
                        while (it.hasNext()) {
                        }
                        String str212 = str10;
                        List list32 = listOf3;
                        Object obj72 = coroutine_suspended;
                        String str222 = str12;
                        if (num2 != null) {
                        }
                        return new Pair(obj3, obj3);
                    case 4:
                        int i9 = movieBoxProviderINKt$searchAndPick$1.I$0;
                        double d11 = movieBoxProviderINKt$searchAndPick$1.D$0;
                        num3 = (Integer) movieBoxProviderINKt$searchAndPick$1.L$5;
                        ResultKt.throwOnFailure(obj4);
                        str5 = null;
                        optJSONObject = new JSONObject(((NiceResponse) obj4).getText()).optJSONObject("external_ids");
                        if (optJSONObject != null) {
                        }
                        return new Pair(num3, str5);
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        movieBoxProviderINKt$searchAndPick$1 = new MovieBoxProviderINKt$searchAndPick$1(continuation);
        Object obj42 = movieBoxProviderINKt$searchAndPick$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        String str112 = "multi";
        String str122 = "movie";
        String str132 = "";
        switch (movieBoxProviderINKt$searchAndPick$1.label) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0032  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0047  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0026  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final Object searchAndPick$doSearch(String str, String str2, Continuation<? super JSONArray> continuation) {
        MovieBoxProviderINKt$searchAndPick$doSearch$1 movieBoxProviderINKt$searchAndPick$doSearch$1;
        if (continuation instanceof MovieBoxProviderINKt$searchAndPick$doSearch$1) {
            movieBoxProviderINKt$searchAndPick$doSearch$1 = (MovieBoxProviderINKt$searchAndPick$doSearch$1) continuation;
            if ((movieBoxProviderINKt$searchAndPick$doSearch$1.label & Integer.MIN_VALUE) != 0) {
                movieBoxProviderINKt$searchAndPick$doSearch$1.label -= Integer.MIN_VALUE;
                Object obj = movieBoxProviderINKt$searchAndPick$doSearch$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (movieBoxProviderINKt$searchAndPick$doSearch$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        StringBuilder sb = new StringBuilder();
                        sb.append("https://api.themoviedb.org/3/").append(str);
                        sb.append("?api_key=").append("1865f43a0549ca50d341dd9ab8b29f49");
                        sb.append(str2);
                        sb.append("&include_adult=false&page=1");
                        sb.append("&random=").append(Random.Default.nextInt());
                        String sb2 = sb.toString();
                        Requests app = MainActivityKt.getApp();
                        movieBoxProviderINKt$searchAndPick$doSearch$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        movieBoxProviderINKt$searchAndPick$doSearch$1.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                        movieBoxProviderINKt$searchAndPick$doSearch$1.L$2 = SpillingKt.nullOutSpilledVariable(sb2);
                        movieBoxProviderINKt$searchAndPick$doSearch$1.label = 1;
                        obj = Requests.get$default(app, sb2, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, movieBoxProviderINKt$searchAndPick$doSearch$1, 4094, (Object) null);
                        if (obj != coroutine_suspended) {
                            break;
                        } else {
                            return coroutine_suspended;
                        }
                    case 1:
                        ResultKt.throwOnFailure(obj);
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                return new JSONObject(((NiceResponse) obj).getText()).optJSONArray("results");
            }
        }
        movieBoxProviderINKt$searchAndPick$doSearch$1 = new MovieBoxProviderINKt$searchAndPick$doSearch$1(continuation);
        Object obj2 = movieBoxProviderINKt$searchAndPick$doSearch$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (movieBoxProviderINKt$searchAndPick$doSearch$1.label) {
        }
        return new JSONObject(((NiceResponse) obj2).getText()).optJSONArray("results");
    }

    static /* synthetic */ Object searchAndPick$doSearch$default(String str, String str2, Continuation continuation, int i, Object obj) {
        if ((i & 2) != 0) {
            str2 = "";
        }
        return searchAndPick$doSearch(str, str2, continuation);
    }

    private static final boolean tokenEquals(String str, String str2) {
        List split = new Regex("\\s+").split(str, 0);
        ArrayList arrayList = new ArrayList();
        for (Object obj : split) {
            if (!StringsKt.isBlank((String) obj)) {
                arrayList.add(obj);
            }
        }
        Set set = CollectionsKt.toSet(arrayList);
        List split2 = new Regex("\\s+").split(str2, 0);
        ArrayList arrayList2 = new ArrayList();
        for (Object obj2 : split2) {
            if (!StringsKt.isBlank((String) obj2)) {
                arrayList2.add(obj2);
            }
        }
        Set set2 = CollectionsKt.toSet(arrayList2);
        return (set.isEmpty() || set2.isEmpty() || CollectionsKt.intersect(set, set2).size() < Math.max(1, (Math.min(set.size(), set2.size()) * 3) / 4)) ? false : true;
    }
}
