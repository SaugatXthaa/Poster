package com.cncverse;

import com.lagradost.api.Log;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorApi;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import com.lagradost.cloudstream3.utils.Qualities;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.SequencesKt;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JH\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00052\b\u0010\u0012\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u00100\u00142\u0012\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00100\u0014H\u0096@¢\u0006\u0002\u0010\u0018J\u0012\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0005H\u0002R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u000e\u0010\u000e\u001a\u00020\u0005X\u0082D¢\u0006\u0002\n\u0000¨\u0006\u001c"}, d2 = {"Lcom/cncverse/Gofile;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "mainApi", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getQuality", "", "fileName", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
public final class Gofile extends ExtractorApi {
    private final boolean requiresReferer;

    @NotNull
    private final String name = "Gofile";

    @NotNull
    private final String mainUrl = "https://gofile.io";

    @NotNull
    private final String mainApi = "https://api.gofile.io";

    /* JADX INFO: Access modifiers changed from: private */
    public final int getQuality(String fileName) {
        List groupValues;
        String str;
        Integer intOrNull;
        MatchResult find$default = Regex.find$default(new Regex("(\\d{3,4})[pP]"), fileName == null ? "" : fileName, 0, 2, (Object) null);
        return (find$default == null || (groupValues = find$default.getGroupValues()) == null || (str = (String) CollectionsKt.getOrNull(groupValues, 1)) == null || (intOrNull = StringsKt.toIntOrNull(str)) == null) ? Qualities.Unknown.getValue() : intOrNull.intValue();
    }

    @NotNull
    public String getMainUrl() {
        return this.mainUrl;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public boolean getRequiresReferer() {
        return this.requiresReferer;
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0029. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:103:0x02c4 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:104:0x02c5  */
    /* JADX WARN: Removed duplicated region for block: B:108:0x018b  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0034  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x00c9  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x03f9 A[Catch: Exception -> 0x053a, TRY_ENTER, TryCatch #13 {Exception -> 0x053a, blocks: (B:31:0x03b2, B:34:0x03f9, B:35:0x0454, B:45:0x0426), top: B:30:0x03b2 }] */
    /* JADX WARN: Removed duplicated region for block: B:40:0x0519 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:41:0x051a  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x0426 A[Catch: Exception -> 0x053a, TryCatch #13 {Exception -> 0x053a, blocks: (B:31:0x03b2, B:34:0x03f9, B:35:0x0454, B:45:0x0426), top: B:30:0x03b2 }] */
    /* JADX WARN: Removed duplicated region for block: B:51:0x0114  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002c  */
    /* JADX WARN: Removed duplicated region for block: B:92:0x0158  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getUrl(@NotNull String str, @Nullable String str2, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Unit> continuation) {
        Gofile$getUrl$1 gofile$getUrl$1;
        Gofile$getUrl$1 gofile$getUrl$12;
        String str3;
        MatchResult find$default;
        List groupValues;
        String str4;
        int i;
        Object post$default;
        String str5;
        Function1<? super SubtitleFile, Unit> function13;
        Object obj;
        String str6;
        Function1<? super ExtractorLink, Unit> function14;
        String string;
        String str7;
        Gofile$getUrl$1 gofile$getUrl$13;
        JSONObject jSONObject;
        Function1<? super ExtractorLink, Unit> function15;
        String str8;
        Object obj2;
        Object obj3;
        String str9;
        String str10;
        Function1<? super ExtractorLink, Unit> function16;
        String text;
        MatchResult find$default2;
        Function1<? super ExtractorLink, Unit> function17;
        List groupValues2;
        String str11;
        char c;
        String str12;
        String str13;
        JSONObject jSONObject2;
        Function1<? super ExtractorLink, Unit> function18;
        String str14;
        long j;
        JSONObject jSONObject3;
        String str15;
        long j2;
        JSONObject jSONObject4;
        JSONObject jSONObject5;
        String format;
        Function1<? super ExtractorLink, Unit> function19;
        String str16;
        if (continuation instanceof Gofile$getUrl$1) {
            gofile$getUrl$1 = (Gofile$getUrl$1) continuation;
            if ((gofile$getUrl$1.label & Integer.MIN_VALUE) != 0) {
                gofile$getUrl$1.label -= Integer.MIN_VALUE;
                gofile$getUrl$12 = gofile$getUrl$1;
                Object obj4 = gofile$getUrl$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (gofile$getUrl$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj4);
                        try {
                            find$default = Regex.find$default(new Regex("/(?:\\?c=|d/)([\\da-zA-Z-]+)"), str, 0, 2, (Object) null);
                        } catch (Exception e) {
                            e = e;
                            str3 = str;
                        }
                        if (find$default != null && (groupValues = find$default.getGroupValues()) != null) {
                            String str17 = (String) groupValues.get(1);
                            if (str17 != null) {
                                try {
                                    Requests app = MainActivityKt.getApp();
                                    String str18 = this.mainApi + "/accounts";
                                    gofile$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(str);
                                    gofile$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                                    gofile$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(function1);
                                    gofile$getUrl$12.L$3 = function12;
                                    gofile$getUrl$12.L$4 = str17;
                                    gofile$getUrl$12.label = 1;
                                    str4 = "data";
                                    i = 2;
                                    try {
                                        post$default = Requests.post$default(app, str18, (Map) null, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, (RequestBody) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gofile$getUrl$12, 65534, (Object) null);
                                        gofile$getUrl$12 = gofile$getUrl$12;
                                    } catch (Exception e2) {
                                        e = e2;
                                        str3 = str;
                                        Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                                        return Unit.INSTANCE;
                                    }
                                } catch (Exception e3) {
                                    e = e3;
                                    str3 = str;
                                }
                                if (post$default == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                str3 = str;
                                str5 = str2;
                                function13 = function1;
                                obj = post$default;
                                str6 = str17;
                                function14 = function12;
                                try {
                                    String text2 = ((NiceResponse) obj).getText();
                                    JSONObject jSONObject6 = new JSONObject(text2);
                                    string = jSONObject6.getJSONObject(str4).getString("token");
                                    Requests app2 = MainActivityKt.getApp();
                                    String str19 = getMainUrl() + "/dist/js/global.js";
                                    gofile$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                                    gofile$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str5);
                                    gofile$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(function13);
                                    gofile$getUrl$12.L$3 = function14;
                                    gofile$getUrl$12.L$4 = str6;
                                    gofile$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(text2);
                                    gofile$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(jSONObject6);
                                    gofile$getUrl$12.L$7 = string;
                                    gofile$getUrl$12.label = i;
                                    str7 = str6;
                                    gofile$getUrl$13 = gofile$getUrl$12;
                                    jSONObject = jSONObject6;
                                    function15 = function14;
                                    str8 = text2;
                                    try {
                                        obj2 = Requests.get$default(app2, str19, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gofile$getUrl$13, 4094, (Object) null);
                                        gofile$getUrl$12 = gofile$getUrl$13;
                                    } catch (Exception e4) {
                                        e = e4;
                                        Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                                        return Unit.INSTANCE;
                                    }
                                } catch (Exception e5) {
                                    e = e5;
                                    function16 = function14;
                                    Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                                    return Unit.INSTANCE;
                                }
                                if (obj2 != coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                obj3 = obj2;
                                str9 = string;
                                function14 = function15;
                                str10 = str7;
                                text = ((NiceResponse) obj3).getText();
                                find$default2 = Regex.find$default(new Regex("appdata\\.wt\\s*=\\s*[\"']([^\"']+)[\"']"), text, 0, i, (Object) null);
                                if (find$default2 != null || (groupValues2 = find$default2.getGroupValues()) == null) {
                                    function17 = function14;
                                } else {
                                    String str20 = (String) CollectionsKt.getOrNull(groupValues2, 1);
                                    if (str20 != null) {
                                        Requests app3 = MainActivityKt.getApp();
                                        try {
                                            String str21 = this.mainApi + "/contents/" + str10 + "?wt=" + str20;
                                            Map mapOf = MapsKt.mapOf(TuplesKt.to("Authorization", "Bearer " + str9));
                                            gofile$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                                            gofile$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str5);
                                            gofile$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(function13);
                                            gofile$getUrl$12.L$3 = function14;
                                            gofile$getUrl$12.L$4 = SpillingKt.nullOutSpilledVariable(str10);
                                            gofile$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(str8);
                                            gofile$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(jSONObject);
                                            gofile$getUrl$12.L$7 = str9;
                                            gofile$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(text);
                                            gofile$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str20);
                                            gofile$getUrl$12.label = 3;
                                            str11 = str10;
                                            function15 = function14;
                                            gofile$getUrl$13 = gofile$getUrl$12;
                                            c = 0;
                                            str12 = str9;
                                            obj4 = Requests.get$default(app3, str21, mapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gofile$getUrl$13, 4092, (Object) null);
                                            gofile$getUrl$12 = gofile$getUrl$13;
                                        } catch (Exception e6) {
                                            e = e6;
                                            function16 = function14;
                                            Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                                            return Unit.INSTANCE;
                                        }
                                        if (obj4 == coroutine_suspended) {
                                            return coroutine_suspended;
                                        }
                                        str13 = str20;
                                        jSONObject2 = jSONObject;
                                        function18 = function15;
                                        str14 = str12;
                                        try {
                                            String text3 = ((NiceResponse) obj4).getText();
                                            JSONObject jSONObject7 = new JSONObject(text3);
                                            JSONObject jSONObject8 = jSONObject7.getJSONObject(str4);
                                            JSONObject jSONObject9 = jSONObject8.getJSONObject("children");
                                            String str22 = (String) SequencesKt.first(SequencesKt.asSequence(jSONObject9.keys()));
                                            JSONObject jSONObject10 = jSONObject9.getJSONObject(str22);
                                            String string2 = jSONObject10.getString("link");
                                            String string3 = jSONObject10.getString("name");
                                            j = jSONObject10.getLong("size");
                                            if (j >= 1073741824) {
                                                jSONObject3 = jSONObject10;
                                                str15 = string2;
                                                j2 = j;
                                                jSONObject4 = jSONObject8;
                                                jSONObject5 = jSONObject7;
                                                Object[] objArr = new Object[1];
                                                objArr[c] = Boxing.boxDouble((j2 / 1024.0d) / 1024);
                                                format = String.format("%.2f MB", Arrays.copyOf(objArr, 1));
                                                Intrinsics.checkNotNullExpressionValue(format, "format(...)");
                                            } else {
                                                jSONObject3 = jSONObject10;
                                                str15 = string2;
                                                j2 = j;
                                                jSONObject4 = jSONObject8;
                                                jSONObject5 = jSONObject7;
                                                double d = 1024;
                                                Double boxDouble = Boxing.boxDouble(((j2 / 1024.0d) / d) / d);
                                                Object[] objArr2 = new Object[1];
                                                objArr2[c] = boxDouble;
                                                format = String.format("%.2f GB", Arrays.copyOf(objArr2, 1));
                                                Intrinsics.checkNotNullExpressionValue(format, "format(...)");
                                            }
                                            String str23 = "Gofile [" + format + "]";
                                            String str24 = format;
                                            try {
                                                Gofile$getUrl$2 gofile$getUrl$2 = new Gofile$getUrl$2(this, string3, str14, null);
                                                gofile$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                                                gofile$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str5);
                                                gofile$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(function13);
                                                gofile$getUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(function18);
                                                gofile$getUrl$12.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                                                gofile$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(str8);
                                                gofile$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(jSONObject2);
                                                gofile$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(str14);
                                                gofile$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(text);
                                                gofile$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str13);
                                                gofile$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(text3);
                                                gofile$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(jSONObject5);
                                                gofile$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(jSONObject4);
                                                gofile$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(jSONObject9);
                                                gofile$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str22);
                                                gofile$getUrl$12.L$15 = SpillingKt.nullOutSpilledVariable(jSONObject3);
                                                gofile$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(str15);
                                                gofile$getUrl$12.L$17 = SpillingKt.nullOutSpilledVariable(string3);
                                                gofile$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(str24);
                                                gofile$getUrl$12.L$19 = function18;
                                                gofile$getUrl$12.J$0 = j2;
                                                gofile$getUrl$12.label = 4;
                                                obj4 = ExtractorApiKt.newExtractorLink$default("Gofile", str23, str15, (ExtractorLinkType) null, gofile$getUrl$2, gofile$getUrl$12, 8, (Object) null);
                                            } catch (Exception e7) {
                                                e = e7;
                                                Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                                                return Unit.INSTANCE;
                                            }
                                        } catch (Exception e8) {
                                            e = e8;
                                        }
                                        if (obj4 != coroutine_suspended) {
                                            return coroutine_suspended;
                                        }
                                        function19 = function18;
                                        str16 = str3;
                                        try {
                                            function19.invoke(obj4);
                                        } catch (Exception e9) {
                                            e = e9;
                                            str3 = str16;
                                            Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                                            return Unit.INSTANCE;
                                        }
                                        return Unit.INSTANCE;
                                    }
                                    function17 = function14;
                                }
                                try {
                                    return Unit.INSTANCE;
                                } catch (Exception e10) {
                                    e = e10;
                                    Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                                    return Unit.INSTANCE;
                                }
                            }
                        }
                        try {
                            return Unit.INSTANCE;
                        } catch (Exception e11) {
                            e = e11;
                            str3 = str;
                            Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                    case 1:
                        String str25 = (String) gofile$getUrl$12.L$4;
                        Function1<? super ExtractorLink, Unit> function110 = (Function1) gofile$getUrl$12.L$3;
                        Function1<? super SubtitleFile, Unit> function111 = (Function1) gofile$getUrl$12.L$2;
                        String str26 = (String) gofile$getUrl$12.L$1;
                        String str27 = (String) gofile$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj4);
                            str4 = "data";
                            function13 = function111;
                            str5 = str26;
                            str3 = str27;
                            i = 2;
                            str6 = str25;
                            function14 = function110;
                            obj = obj4;
                            String text22 = ((NiceResponse) obj).getText();
                            JSONObject jSONObject62 = new JSONObject(text22);
                            string = jSONObject62.getJSONObject(str4).getString("token");
                            Requests app22 = MainActivityKt.getApp();
                            String str192 = getMainUrl() + "/dist/js/global.js";
                            gofile$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                            gofile$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str5);
                            gofile$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(function13);
                            gofile$getUrl$12.L$3 = function14;
                            gofile$getUrl$12.L$4 = str6;
                            gofile$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(text22);
                            gofile$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(jSONObject62);
                            gofile$getUrl$12.L$7 = string;
                            gofile$getUrl$12.label = i;
                            str7 = str6;
                            gofile$getUrl$13 = gofile$getUrl$12;
                            jSONObject = jSONObject62;
                            function15 = function14;
                            str8 = text22;
                            obj2 = Requests.get$default(app22, str192, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gofile$getUrl$13, 4094, (Object) null);
                            gofile$getUrl$12 = gofile$getUrl$13;
                            if (obj2 != coroutine_suspended) {
                            }
                        } catch (Exception e12) {
                            e = e12;
                            str3 = str27;
                            Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        break;
                    case 2:
                        String str28 = (String) gofile$getUrl$12.L$7;
                        JSONObject jSONObject11 = (JSONObject) gofile$getUrl$12.L$6;
                        String str29 = (String) gofile$getUrl$12.L$5;
                        String str30 = (String) gofile$getUrl$12.L$4;
                        Function1<? super ExtractorLink, Unit> function112 = (Function1) gofile$getUrl$12.L$3;
                        Function1<? super SubtitleFile, Unit> function113 = (Function1) gofile$getUrl$12.L$2;
                        String str31 = (String) gofile$getUrl$12.L$1;
                        String str32 = (String) gofile$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj4);
                            str4 = "data";
                            jSONObject = jSONObject11;
                            str8 = str29;
                            str10 = str30;
                            function13 = function113;
                            str5 = str31;
                            str3 = str32;
                            i = 2;
                            str9 = str28;
                            function14 = function112;
                            obj3 = obj4;
                            text = ((NiceResponse) obj3).getText();
                            find$default2 = Regex.find$default(new Regex("appdata\\.wt\\s*=\\s*[\"']([^\"']+)[\"']"), text, 0, i, (Object) null);
                            if (find$default2 != null) {
                                break;
                            }
                            function17 = function14;
                            return Unit.INSTANCE;
                        } catch (Exception e13) {
                            e = e13;
                            str3 = str32;
                            Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                    case 3:
                        String str33 = (String) gofile$getUrl$12.L$9;
                        String str34 = (String) gofile$getUrl$12.L$8;
                        str14 = (String) gofile$getUrl$12.L$7;
                        jSONObject2 = (JSONObject) gofile$getUrl$12.L$6;
                        String str35 = (String) gofile$getUrl$12.L$5;
                        String str36 = (String) gofile$getUrl$12.L$4;
                        function18 = (Function1) gofile$getUrl$12.L$3;
                        Function1<? super SubtitleFile, Unit> function114 = (Function1) gofile$getUrl$12.L$2;
                        String str37 = (String) gofile$getUrl$12.L$1;
                        String str38 = (String) gofile$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj4);
                            str4 = "data";
                            str5 = str37;
                            text = str34;
                            str3 = str38;
                            function13 = function114;
                            str8 = str35;
                            str11 = str36;
                            c = 0;
                            str13 = str33;
                            String text32 = ((NiceResponse) obj4).getText();
                            JSONObject jSONObject72 = new JSONObject(text32);
                            JSONObject jSONObject82 = jSONObject72.getJSONObject(str4);
                            JSONObject jSONObject92 = jSONObject82.getJSONObject("children");
                            String str222 = (String) SequencesKt.first(SequencesKt.asSequence(jSONObject92.keys()));
                            JSONObject jSONObject102 = jSONObject92.getJSONObject(str222);
                            String string22 = jSONObject102.getString("link");
                            String string32 = jSONObject102.getString("name");
                            j = jSONObject102.getLong("size");
                            if (j >= 1073741824) {
                            }
                            String str232 = "Gofile [" + format + "]";
                            String str242 = format;
                            Gofile$getUrl$2 gofile$getUrl$22 = new Gofile$getUrl$2(this, string32, str14, null);
                            gofile$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                            gofile$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str5);
                            gofile$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(function13);
                            gofile$getUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(function18);
                            gofile$getUrl$12.L$4 = SpillingKt.nullOutSpilledVariable(str11);
                            gofile$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(str8);
                            gofile$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(jSONObject2);
                            gofile$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(str14);
                            gofile$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(text);
                            gofile$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str13);
                            gofile$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(text32);
                            gofile$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(jSONObject5);
                            gofile$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(jSONObject4);
                            gofile$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(jSONObject92);
                            gofile$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str222);
                            gofile$getUrl$12.L$15 = SpillingKt.nullOutSpilledVariable(jSONObject3);
                            gofile$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(str15);
                            gofile$getUrl$12.L$17 = SpillingKt.nullOutSpilledVariable(string32);
                            gofile$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(str242);
                            gofile$getUrl$12.L$19 = function18;
                            gofile$getUrl$12.J$0 = j2;
                            gofile$getUrl$12.label = 4;
                            obj4 = ExtractorApiKt.newExtractorLink$default("Gofile", str232, str15, (ExtractorLinkType) null, gofile$getUrl$22, gofile$getUrl$12, 8, (Object) null);
                            if (obj4 != coroutine_suspended) {
                            }
                        } catch (Exception e14) {
                            e = e14;
                            str3 = str38;
                            Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        break;
                    case 4:
                        long j3 = gofile$getUrl$12.J$0;
                        Function1<? super ExtractorLink, Unit> function115 = (Function1) gofile$getUrl$12.L$19;
                        str16 = (String) gofile$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj4);
                            function19 = function115;
                            function19.invoke(obj4);
                        } catch (Exception e15) {
                            e = e15;
                            str3 = str16;
                            Log.INSTANCE.e("Gofile", "Error occurred: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        return Unit.INSTANCE;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        gofile$getUrl$1 = new Gofile$getUrl$1(this, continuation);
        gofile$getUrl$12 = gofile$getUrl$1;
        Object obj42 = gofile$getUrl$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (gofile$getUrl$12.label) {
        }
    }
}
