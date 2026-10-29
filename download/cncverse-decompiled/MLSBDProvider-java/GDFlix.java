package com.cncverse;

import com.lagradost.api.Log;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.ParCollectionsKt;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorApi;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u000e\u001a\u00020\u0005H\u0082@¢\u0006\u0002\u0010\u000fJH\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00052\b\u0010\u0013\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u00110\u00152\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00110\u0015H\u0096@¢\u0006\u0002\u0010\u0019J\u000e\u0010\u001a\u001a\u00020\u00052\u0006\u0010\u0012\u001a\u00020\u0005R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u001b"}, d2 = {"Lcom/cncverse/GDFlix;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "getLatestUrl", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBaseUrl", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nExtractors.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Extractors.kt\ncom/cncverse/GDFlix\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,995:1\n1#2:996\n*E\n"})
/* loaded from: classes.dex */
public final class GDFlix extends ExtractorApi {
    private final boolean requiresReferer;

    @NotNull
    private final String name = "GDFlix";

    @NotNull
    private final String mainUrl = "https://gdflix.dev";

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0029. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0047  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x00a5 A[Catch: Exception -> 0x00ab, TRY_LEAVE, TryCatch #2 {Exception -> 0x00ab, blocks: (B:22:0x0082, B:24:0x0099, B:29:0x00a5, B:19:0x007b), top: B:18:0x007b }] */
    /* JADX WARN: Removed duplicated region for block: B:33:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:44:0x0036 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object getLatestUrl(Continuation<? super String> continuation) {
        GDFlix$getLatestUrl$1 gDFlix$getLatestUrl$1;
        GDFlix gDFlix;
        GDFlix$getLatestUrl$1 gDFlix$getLatestUrl$12;
        String optString;
        String str;
        boolean z;
        if (continuation instanceof GDFlix$getLatestUrl$1) {
            gDFlix$getLatestUrl$1 = (GDFlix$getLatestUrl$1) continuation;
            if ((gDFlix$getLatestUrl$1.label & Integer.MIN_VALUE) != 0) {
                gDFlix$getLatestUrl$1.label -= Integer.MIN_VALUE;
                gDFlix = this;
                gDFlix$getLatestUrl$12 = gDFlix$getLatestUrl$1;
                Object obj = gDFlix$getLatestUrl$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (gDFlix$getLatestUrl$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        try {
                            Requests app = MainActivityKt.getApp();
                            try {
                                gDFlix$getLatestUrl$12.label = 1;
                            } catch (Exception e) {
                                return gDFlix.getMainUrl();
                            }
                            try {
                                obj = Requests.get$default(app, "https://raw.githubusercontent.com/SaurabhKaperwan/Utils/refs/heads/main/urls.json", (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDFlix$getLatestUrl$12, 4094, (Object) null);
                                if (obj == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                optString = new JSONObject(((NiceResponse) obj).getText()).optString("gdflix");
                                str = optString;
                                if (str != null && str.length() != 0) {
                                    z = false;
                                    return !z ? gDFlix.getMainUrl() : optString;
                                }
                                z = true;
                                if (!z) {
                                }
                            } catch (Exception e2) {
                                return gDFlix.getMainUrl();
                            }
                        } catch (Exception e3) {
                        }
                        break;
                    case 1:
                        try {
                            ResultKt.throwOnFailure(obj);
                            optString = new JSONObject(((NiceResponse) obj).getText()).optString("gdflix");
                            str = optString;
                            if (str != null) {
                                z = false;
                                if (!z) {
                                }
                                break;
                            }
                            z = true;
                            if (!z) {
                            }
                        } catch (Exception e4) {
                            return gDFlix.getMainUrl();
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        gDFlix = this;
        gDFlix$getLatestUrl$1 = new GDFlix$getLatestUrl$1(gDFlix, continuation);
        gDFlix$getLatestUrl$12 = gDFlix$getLatestUrl$1;
        Object obj2 = gDFlix$getLatestUrl$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (gDFlix$getLatestUrl$12.label) {
        }
    }

    @NotNull
    public final String getBaseUrl(@NotNull String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() + "://" + uri.getHost();
        } catch (Exception e) {
            return "";
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

    public boolean getRequiresReferer() {
        return this.requiresReferer;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(12:158|(4:93|94|95|(1:97)(1:131))|(3:116|117|(11:(2:124|125)(1:120)|121|122|100|101|102|103|104|105|106|(1:108)(13:109|64|65|66|28|29|30|31|32|33|34|35|(1:37)(3:38|15|16))))|99|100|101|102|103|104|105|106|(0)(0)) */
    /* JADX WARN: Can't wrap try/catch for region: R(15:158|93|94|95|(1:97)(1:131)|(3:116|117|(11:(2:124|125)(1:120)|121|122|100|101|102|103|104|105|106|(1:108)(13:109|64|65|66|28|29|30|31|32|33|34|35|(1:37)(3:38|15|16))))|99|100|101|102|103|104|105|106|(0)(0)) */
    /* JADX WARN: Code restructure failed: missing block: B:111:0x02a7, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:112:0x02a8, code lost:
    
        r10 = r2;
        r11 = r6;
        r6 = r11;
        r12 = r17;
        r13 = r20;
        r28 = r35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:114:0x02b4, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:115:0x02b5, code lost:
    
        r10 = r2;
        r30 = r11;
        r29 = r13;
        r2 = r17;
        r11 = r6;
        r6 = r15;
        r12 = r12;
        r13 = r30;
        r28 = r1;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002d. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:108:0x027c A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:109:0x027d  */
    /* JADX WARN: Removed duplicated region for block: B:116:0x01bc A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0039  */
    /* JADX WARN: Removed duplicated region for block: B:131:0x01b8  */
    /* JADX WARN: Removed duplicated region for block: B:138:0x013e  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x007f  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x03f9 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:38:0x03fa  */
    /* JADX WARN: Removed duplicated region for block: B:60:0x00c7  */
    /* JADX WARN: Removed duplicated region for block: B:78:0x033c A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:79:0x033d  */
    /* JADX WARN: Removed duplicated region for block: B:89:0x010e  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0030  */
    /* JADX WARN: Removed duplicated region for block: B:97:0x01b5  */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getUrl(@NotNull String str, @Nullable String str2, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Unit> continuation) {
        GDFlix$getUrl$1 gDFlix$getUrl$1;
        GDFlix$getUrl$1 gDFlix$getUrl$12;
        Object obj;
        Function1<? super SubtitleFile, Unit> function13;
        Function1<? super ExtractorLink, Unit> function14;
        String str3;
        String str4;
        Function1<? super SubtitleFile, Unit> function15;
        String host;
        Object latestUrl;
        String str5;
        Function1<? super SubtitleFile, Unit> function16;
        Function1<? super ExtractorLink, Unit> function17;
        URI uri;
        String str6;
        String str7;
        String str8;
        String scheme;
        String str9;
        String replace$default;
        GDFlix$getUrl$1 gDFlix$getUrl$13;
        String str10;
        String str11;
        String str12;
        String str13;
        Function1<? super ExtractorLink, Unit> function18;
        Function1<? super SubtitleFile, Unit> function19;
        String str14;
        String str15;
        Object obj2;
        URI uri2;
        Function1<? super ExtractorLink, Unit> function110;
        String str16;
        Function1<? super SubtitleFile, Unit> function111;
        Function1<? super ExtractorLink, Unit> function112;
        Function1<? super SubtitleFile, Unit> function113;
        Object obj3;
        String str17;
        String str18;
        URI uri3;
        String str19;
        String str20;
        Function1<? super SubtitleFile, Unit> function114;
        Function1<? super ExtractorLink, Unit> function115;
        Document document;
        String str21;
        URI uri4;
        String str22;
        List select;
        Function1<? super ExtractorLink, Unit> function116;
        GDFlix$getUrl$2 gDFlix$getUrl$2;
        if (continuation instanceof GDFlix$getUrl$1) {
            gDFlix$getUrl$1 = (GDFlix$getUrl$1) continuation;
            if ((gDFlix$getUrl$1.label & Integer.MIN_VALUE) != 0) {
                gDFlix$getUrl$1.label -= Integer.MIN_VALUE;
                gDFlix$getUrl$12 = gDFlix$getUrl$1;
                Object obj4 = gDFlix$getUrl$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (gDFlix$getUrl$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj4);
                        try {
                            try {
                                Result.Companion companion = Result.Companion;
                                GDFlix gDFlix = this;
                                obj = Result.constructor-impl(new URI(str));
                            } catch (Exception e) {
                                e = e;
                                function13 = function1;
                                function14 = function12;
                                str3 = "GDFlix";
                                str4 = str2;
                                function15 = function13;
                                Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                            }
                        } catch (Throwable th) {
                            Result.Companion companion2 = Result.Companion;
                            obj = Result.constructor-impl(ResultKt.createFailure(th));
                        }
                        if (Result.isFailure-impl(obj)) {
                            obj = null;
                        }
                        URI uri5 = (URI) obj;
                        if (uri5 != null) {
                            try {
                                host = uri5.getHost();
                            } catch (Exception e2) {
                                e = e2;
                                str4 = str2;
                                function14 = function12;
                                str3 = "GDFlix";
                                function15 = function1;
                                Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                            }
                        } else {
                            host = null;
                        }
                        if (host == null) {
                            host = "";
                        }
                        gDFlix$getUrl$12.L$0 = str;
                        gDFlix$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                        function13 = function1;
                        try {
                            gDFlix$getUrl$12.L$2 = function13;
                            function14 = function12;
                        } catch (Exception e3) {
                            e = e3;
                            function14 = function12;
                            str3 = "GDFlix";
                            str4 = str2;
                            function15 = function13;
                            Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                        }
                        try {
                            gDFlix$getUrl$12.L$3 = function14;
                            gDFlix$getUrl$12.L$4 = uri5;
                            gDFlix$getUrl$12.L$5 = host;
                            gDFlix$getUrl$12.label = 1;
                            latestUrl = getLatestUrl(gDFlix$getUrl$12);
                        } catch (Exception e4) {
                            e = e4;
                            str3 = "GDFlix";
                            str4 = str2;
                            function15 = function13;
                            Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                        }
                        if (latestUrl == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        str5 = host;
                        function16 = function13;
                        function17 = function14;
                        uri = uri5;
                        str6 = str;
                        str7 = str2;
                        try {
                            str8 = (String) latestUrl;
                        } catch (Exception e5) {
                            e = e5;
                            Function1<? super SubtitleFile, Unit> function117 = function16;
                            str3 = "GDFlix";
                            str4 = str7;
                            function14 = function17;
                            function15 = function117;
                        }
                        if (str5.length() <= 0) {
                            try {
                            } catch (Exception e6) {
                                e = e6;
                                Function1<? super SubtitleFile, Unit> function118 = function16;
                                str3 = "GDFlix";
                                function15 = function118;
                                str4 = str7;
                                function14 = function17;
                            }
                            if (!StringsKt.startsWith$default(str6, str8, false, 2, (Object) null)) {
                                if (uri != null) {
                                    try {
                                        scheme = uri.getScheme();
                                    } catch (Exception e7) {
                                        e = e7;
                                        Function1<? super SubtitleFile, Unit> function119 = function16;
                                        str3 = "GDFlix";
                                        function15 = function119;
                                        str4 = str7;
                                        function14 = function17;
                                        Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                                    }
                                } else {
                                    scheme = null;
                                }
                                str9 = str6;
                                replace$default = StringsKt.replace$default(str6, scheme + "://" + str5, str8, false, 4, (Object) null);
                                Requests app = MainActivityKt.getApp();
                                gDFlix$getUrl$12.L$0 = str9;
                                gDFlix$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str7);
                                gDFlix$getUrl$12.L$2 = function16;
                                gDFlix$getUrl$12.L$3 = function17;
                                gDFlix$getUrl$12.L$4 = SpillingKt.nullOutSpilledVariable(uri);
                                gDFlix$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(str5);
                                gDFlix$getUrl$12.L$6 = str8;
                                gDFlix$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(replace$default);
                                gDFlix$getUrl$12.label = 2;
                                Function1<? super ExtractorLink, Unit> function120 = function17;
                                Function1<? super SubtitleFile, Unit> function121 = function16;
                                str15 = replace$default;
                                str10 = str5;
                                str11 = str8;
                                String str23 = str7;
                                obj2 = Requests.get$default(app, str15, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDFlix$getUrl$12, 4094, (Object) null);
                                gDFlix$getUrl$13 = gDFlix$getUrl$12;
                                if (obj2 == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                str4 = str23;
                                uri2 = uri;
                                function110 = function120;
                                str16 = str10;
                                function111 = function121;
                                try {
                                    document = ((NiceResponse) obj2).getDocument();
                                    String str24 = str15;
                                    str12 = str9;
                                    str21 = "GDFlix";
                                    function15 = function111;
                                    uri4 = uri2;
                                    str20 = str16;
                                    str22 = str24;
                                } catch (Exception e8) {
                                    e = e8;
                                    String str25 = str15;
                                    str12 = str9;
                                    str13 = str25;
                                    str10 = str16;
                                    uri = uri2;
                                    function18 = function110;
                                    function19 = function111;
                                    str14 = str4;
                                    Exception exc = e;
                                    try {
                                        Requests app2 = MainActivityKt.getApp();
                                        gDFlix$getUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(str12);
                                        gDFlix$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str14);
                                        gDFlix$getUrl$13.L$2 = function19;
                                        gDFlix$getUrl$13.L$3 = function18;
                                        gDFlix$getUrl$13.L$4 = SpillingKt.nullOutSpilledVariable(uri);
                                        gDFlix$getUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(str10);
                                        gDFlix$getUrl$13.L$6 = str11;
                                        gDFlix$getUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(str13);
                                        gDFlix$getUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(exc);
                                        gDFlix$getUrl$13.label = 3;
                                        function113 = function19;
                                        function112 = function18;
                                        GDFlix$getUrl$1 gDFlix$getUrl$14 = gDFlix$getUrl$13;
                                        try {
                                            obj3 = Requests.get$default(app2, str12, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDFlix$getUrl$14, 4094, (Object) null);
                                            gDFlix$getUrl$13 = gDFlix$getUrl$14;
                                            if (obj3 == coroutine_suspended) {
                                                return coroutine_suspended;
                                            }
                                            str17 = str11;
                                            str18 = str13;
                                            uri3 = uri;
                                            str19 = str14;
                                            str20 = str10;
                                            function114 = function113;
                                            function115 = function112;
                                            try {
                                                document = ((NiceResponse) obj3).getDocument();
                                                String str26 = str17;
                                                str22 = str18;
                                                str11 = str26;
                                                Function1<? super ExtractorLink, Unit> function122 = function115;
                                                uri4 = uri3;
                                                function110 = function122;
                                                str21 = "GDFlix";
                                                function15 = function114;
                                                str4 = str19;
                                                String str27 = str11;
                                                String substringAfter$default = StringsKt.substringAfter$default(document.select("ul > li.list-group-item:contains(Name)").text(), "Name : ", (String) null, 2, (Object) null);
                                                String substringAfter$default2 = StringsKt.substringAfter$default(document.select("ul > li.list-group-item:contains(Size)").text(), "Size : ", (String) null, 2, (Object) null);
                                                int indexQuality = ExtractorsKt.getIndexQuality(substringAfter$default);
                                                select = document.select("div.text-center a");
                                                Document document2 = document;
                                                String str28 = str12;
                                                str3 = str21;
                                                function116 = function110;
                                                gDFlix$getUrl$2 = new GDFlix$getUrl$2(function116, substringAfter$default, substringAfter$default2, this, str27, indexQuality, function15, null);
                                                gDFlix$getUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(str28);
                                                gDFlix$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str4);
                                                gDFlix$getUrl$13.L$2 = SpillingKt.nullOutSpilledVariable(function15);
                                                gDFlix$getUrl$13.L$3 = SpillingKt.nullOutSpilledVariable(function116);
                                                gDFlix$getUrl$13.L$4 = SpillingKt.nullOutSpilledVariable(uri4);
                                                gDFlix$getUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(str20);
                                                gDFlix$getUrl$13.L$6 = SpillingKt.nullOutSpilledVariable(str27);
                                                gDFlix$getUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(str22);
                                                gDFlix$getUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(document2);
                                                gDFlix$getUrl$13.L$9 = SpillingKt.nullOutSpilledVariable(substringAfter$default);
                                                gDFlix$getUrl$13.L$10 = SpillingKt.nullOutSpilledVariable(substringAfter$default2);
                                                gDFlix$getUrl$13.I$0 = indexQuality;
                                                gDFlix$getUrl$13.label = 4;
                                                if (ParCollectionsKt.amap(select, gDFlix$getUrl$2, gDFlix$getUrl$13) != coroutine_suspended) {
                                                }
                                            } catch (Exception e9) {
                                                e = e9;
                                                str3 = "GDFlix";
                                                function112 = function115;
                                                function113 = function114;
                                                str14 = str19;
                                                try {
                                                    Log.INSTANCE.e(str3, "Failed to load GDFlix URL: " + e.getMessage());
                                                    return Unit.INSTANCE;
                                                } catch (Exception e10) {
                                                    e = e10;
                                                    str4 = str14;
                                                    function15 = function113;
                                                    function14 = function112;
                                                    Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                                                }
                                            }
                                        } catch (Exception e11) {
                                            e = e11;
                                            str3 = "GDFlix";
                                            Log.INSTANCE.e(str3, "Failed to load GDFlix URL: " + e.getMessage());
                                            return Unit.INSTANCE;
                                        }
                                    } catch (Exception e12) {
                                        e = e12;
                                        function112 = function18;
                                        function113 = function19;
                                        str3 = "GDFlix";
                                    }
                                }
                                try {
                                    String str272 = str11;
                                    try {
                                        String substringAfter$default3 = StringsKt.substringAfter$default(document.select("ul > li.list-group-item:contains(Name)").text(), "Name : ", (String) null, 2, (Object) null);
                                        String substringAfter$default22 = StringsKt.substringAfter$default(document.select("ul > li.list-group-item:contains(Size)").text(), "Size : ", (String) null, 2, (Object) null);
                                        int indexQuality2 = ExtractorsKt.getIndexQuality(substringAfter$default3);
                                        select = document.select("div.text-center a");
                                        Document document22 = document;
                                        String str282 = str12;
                                        str3 = str21;
                                        function116 = function110;
                                    } catch (Exception e13) {
                                        e = e13;
                                        str3 = str21;
                                        function14 = function110;
                                    }
                                } catch (Exception e14) {
                                    e = e14;
                                    str3 = str21;
                                    function14 = function110;
                                }
                                try {
                                    gDFlix$getUrl$2 = new GDFlix$getUrl$2(function116, substringAfter$default3, substringAfter$default22, this, str272, indexQuality2, function15, null);
                                    gDFlix$getUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(str282);
                                    gDFlix$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str4);
                                    gDFlix$getUrl$13.L$2 = SpillingKt.nullOutSpilledVariable(function15);
                                    gDFlix$getUrl$13.L$3 = SpillingKt.nullOutSpilledVariable(function116);
                                    gDFlix$getUrl$13.L$4 = SpillingKt.nullOutSpilledVariable(uri4);
                                    gDFlix$getUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(str20);
                                    gDFlix$getUrl$13.L$6 = SpillingKt.nullOutSpilledVariable(str272);
                                    gDFlix$getUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(str22);
                                    gDFlix$getUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(document22);
                                    gDFlix$getUrl$13.L$9 = SpillingKt.nullOutSpilledVariable(substringAfter$default3);
                                    gDFlix$getUrl$13.L$10 = SpillingKt.nullOutSpilledVariable(substringAfter$default22);
                                    gDFlix$getUrl$13.I$0 = indexQuality2;
                                    gDFlix$getUrl$13.label = 4;
                                } catch (Exception e15) {
                                    e = e15;
                                    function14 = function116;
                                    Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                                }
                                return ParCollectionsKt.amap(select, gDFlix$getUrl$2, gDFlix$getUrl$13) != coroutine_suspended ? coroutine_suspended : Unit.INSTANCE;
                            }
                        }
                        str9 = str6;
                        replace$default = str9;
                        Requests app3 = MainActivityKt.getApp();
                        gDFlix$getUrl$12.L$0 = str9;
                        gDFlix$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str7);
                        gDFlix$getUrl$12.L$2 = function16;
                        gDFlix$getUrl$12.L$3 = function17;
                        gDFlix$getUrl$12.L$4 = SpillingKt.nullOutSpilledVariable(uri);
                        gDFlix$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(str5);
                        gDFlix$getUrl$12.L$6 = str8;
                        gDFlix$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(replace$default);
                        gDFlix$getUrl$12.label = 2;
                        Function1<? super ExtractorLink, Unit> function1202 = function17;
                        Function1<? super SubtitleFile, Unit> function1212 = function16;
                        str15 = replace$default;
                        str10 = str5;
                        str11 = str8;
                        String str232 = str7;
                        obj2 = Requests.get$default(app3, str15, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDFlix$getUrl$12, 4094, (Object) null);
                        gDFlix$getUrl$13 = gDFlix$getUrl$12;
                        if (obj2 == coroutine_suspended) {
                        }
                    case 1:
                        String str29 = (String) gDFlix$getUrl$12.L$5;
                        URI uri6 = (URI) gDFlix$getUrl$12.L$4;
                        function17 = (Function1) gDFlix$getUrl$12.L$3;
                        Function1<? super SubtitleFile, Unit> function123 = (Function1) gDFlix$getUrl$12.L$2;
                        String str30 = (String) gDFlix$getUrl$12.L$1;
                        String str31 = (String) gDFlix$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj4);
                            str6 = str31;
                            uri = uri6;
                            function16 = function123;
                            str7 = str30;
                            str5 = str29;
                            latestUrl = obj4;
                            str8 = (String) latestUrl;
                            if (str5.length() <= 0) {
                            }
                            str9 = str6;
                            replace$default = str9;
                            Requests app32 = MainActivityKt.getApp();
                            gDFlix$getUrl$12.L$0 = str9;
                            gDFlix$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str7);
                            gDFlix$getUrl$12.L$2 = function16;
                            gDFlix$getUrl$12.L$3 = function17;
                            gDFlix$getUrl$12.L$4 = SpillingKt.nullOutSpilledVariable(uri);
                            gDFlix$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(str5);
                            gDFlix$getUrl$12.L$6 = str8;
                            gDFlix$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(replace$default);
                            gDFlix$getUrl$12.label = 2;
                            Function1<? super ExtractorLink, Unit> function12022 = function17;
                            Function1<? super SubtitleFile, Unit> function12122 = function16;
                            str15 = replace$default;
                            str10 = str5;
                            str11 = str8;
                            String str2322 = str7;
                            obj2 = Requests.get$default(app32, str15, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDFlix$getUrl$12, 4094, (Object) null);
                            gDFlix$getUrl$13 = gDFlix$getUrl$12;
                            if (obj2 == coroutine_suspended) {
                            }
                        } catch (Exception e16) {
                            e = e16;
                            str3 = "GDFlix";
                            function15 = function123;
                            str4 = str30;
                            function14 = function17;
                            Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                        }
                        break;
                    case 2:
                        str13 = (String) gDFlix$getUrl$12.L$7;
                        String str32 = (String) gDFlix$getUrl$12.L$6;
                        str16 = (String) gDFlix$getUrl$12.L$5;
                        uri2 = (URI) gDFlix$getUrl$12.L$4;
                        function110 = (Function1) gDFlix$getUrl$12.L$3;
                        function111 = (Function1) gDFlix$getUrl$12.L$2;
                        str4 = (String) gDFlix$getUrl$12.L$1;
                        String str33 = (String) gDFlix$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj4);
                            str9 = str33;
                            gDFlix$getUrl$13 = gDFlix$getUrl$12;
                            str11 = str32;
                            str15 = str13;
                            obj2 = obj4;
                            document = ((NiceResponse) obj2).getDocument();
                            String str242 = str15;
                            str12 = str9;
                            str21 = "GDFlix";
                            function15 = function111;
                            uri4 = uri2;
                            str20 = str16;
                            str22 = str242;
                        } catch (Exception e17) {
                            e = e17;
                            str12 = str33;
                            gDFlix$getUrl$13 = gDFlix$getUrl$12;
                            str11 = str32;
                            str10 = str16;
                            uri = uri2;
                            function18 = function110;
                            function19 = function111;
                            str14 = str4;
                            Exception exc2 = e;
                            Requests app22 = MainActivityKt.getApp();
                            gDFlix$getUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(str12);
                            gDFlix$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str14);
                            gDFlix$getUrl$13.L$2 = function19;
                            gDFlix$getUrl$13.L$3 = function18;
                            gDFlix$getUrl$13.L$4 = SpillingKt.nullOutSpilledVariable(uri);
                            gDFlix$getUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(str10);
                            gDFlix$getUrl$13.L$6 = str11;
                            gDFlix$getUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(str13);
                            gDFlix$getUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(exc2);
                            gDFlix$getUrl$13.label = 3;
                            function113 = function19;
                            function112 = function18;
                            GDFlix$getUrl$1 gDFlix$getUrl$142 = gDFlix$getUrl$13;
                            obj3 = Requests.get$default(app22, str12, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDFlix$getUrl$142, 4094, (Object) null);
                            gDFlix$getUrl$13 = gDFlix$getUrl$142;
                            if (obj3 == coroutine_suspended) {
                            }
                        }
                        String str2722 = str11;
                        String substringAfter$default32 = StringsKt.substringAfter$default(document.select("ul > li.list-group-item:contains(Name)").text(), "Name : ", (String) null, 2, (Object) null);
                        String substringAfter$default222 = StringsKt.substringAfter$default(document.select("ul > li.list-group-item:contains(Size)").text(), "Size : ", (String) null, 2, (Object) null);
                        int indexQuality22 = ExtractorsKt.getIndexQuality(substringAfter$default32);
                        select = document.select("div.text-center a");
                        Document document222 = document;
                        String str2822 = str12;
                        str3 = str21;
                        function116 = function110;
                        gDFlix$getUrl$2 = new GDFlix$getUrl$2(function116, substringAfter$default32, substringAfter$default222, this, str2722, indexQuality22, function15, null);
                        gDFlix$getUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(str2822);
                        gDFlix$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str4);
                        gDFlix$getUrl$13.L$2 = SpillingKt.nullOutSpilledVariable(function15);
                        gDFlix$getUrl$13.L$3 = SpillingKt.nullOutSpilledVariable(function116);
                        gDFlix$getUrl$13.L$4 = SpillingKt.nullOutSpilledVariable(uri4);
                        gDFlix$getUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(str20);
                        gDFlix$getUrl$13.L$6 = SpillingKt.nullOutSpilledVariable(str2722);
                        gDFlix$getUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(str22);
                        gDFlix$getUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(document222);
                        gDFlix$getUrl$13.L$9 = SpillingKt.nullOutSpilledVariable(substringAfter$default32);
                        gDFlix$getUrl$13.L$10 = SpillingKt.nullOutSpilledVariable(substringAfter$default222);
                        gDFlix$getUrl$13.I$0 = indexQuality22;
                        gDFlix$getUrl$13.label = 4;
                        if (ParCollectionsKt.amap(select, gDFlix$getUrl$2, gDFlix$getUrl$13) != coroutine_suspended) {
                        }
                        break;
                    case 3:
                        String str34 = (String) gDFlix$getUrl$12.L$7;
                        str17 = (String) gDFlix$getUrl$12.L$6;
                        str20 = (String) gDFlix$getUrl$12.L$5;
                        uri3 = (URI) gDFlix$getUrl$12.L$4;
                        function115 = (Function1) gDFlix$getUrl$12.L$3;
                        function114 = (Function1) gDFlix$getUrl$12.L$2;
                        str19 = (String) gDFlix$getUrl$12.L$1;
                        String str35 = (String) gDFlix$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj4);
                            str12 = str35;
                            gDFlix$getUrl$13 = gDFlix$getUrl$12;
                            str18 = str34;
                            obj3 = obj4;
                            document = ((NiceResponse) obj3).getDocument();
                            String str262 = str17;
                            str22 = str18;
                            str11 = str262;
                            Function1<? super ExtractorLink, Unit> function1222 = function115;
                            uri4 = uri3;
                            function110 = function1222;
                            str21 = "GDFlix";
                            function15 = function114;
                            str4 = str19;
                            String str27222 = str11;
                            String substringAfter$default322 = StringsKt.substringAfter$default(document.select("ul > li.list-group-item:contains(Name)").text(), "Name : ", (String) null, 2, (Object) null);
                            String substringAfter$default2222 = StringsKt.substringAfter$default(document.select("ul > li.list-group-item:contains(Size)").text(), "Size : ", (String) null, 2, (Object) null);
                            int indexQuality222 = ExtractorsKt.getIndexQuality(substringAfter$default322);
                            select = document.select("div.text-center a");
                            Document document2222 = document;
                            String str28222 = str12;
                            str3 = str21;
                            function116 = function110;
                            gDFlix$getUrl$2 = new GDFlix$getUrl$2(function116, substringAfter$default322, substringAfter$default2222, this, str27222, indexQuality222, function15, null);
                            gDFlix$getUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(str28222);
                            gDFlix$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str4);
                            gDFlix$getUrl$13.L$2 = SpillingKt.nullOutSpilledVariable(function15);
                            gDFlix$getUrl$13.L$3 = SpillingKt.nullOutSpilledVariable(function116);
                            gDFlix$getUrl$13.L$4 = SpillingKt.nullOutSpilledVariable(uri4);
                            gDFlix$getUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(str20);
                            gDFlix$getUrl$13.L$6 = SpillingKt.nullOutSpilledVariable(str27222);
                            gDFlix$getUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(str22);
                            gDFlix$getUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(document2222);
                            gDFlix$getUrl$13.L$9 = SpillingKt.nullOutSpilledVariable(substringAfter$default322);
                            gDFlix$getUrl$13.L$10 = SpillingKt.nullOutSpilledVariable(substringAfter$default2222);
                            gDFlix$getUrl$13.I$0 = indexQuality222;
                            gDFlix$getUrl$13.label = 4;
                            if (ParCollectionsKt.amap(select, gDFlix$getUrl$2, gDFlix$getUrl$13) != coroutine_suspended) {
                            }
                        } catch (Exception e18) {
                            e = e18;
                            function112 = function115;
                            function113 = function114;
                            str14 = str19;
                            str3 = "GDFlix";
                            Log.INSTANCE.e(str3, "Failed to load GDFlix URL: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        break;
                    case 4:
                        int i = gDFlix$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function124 = (Function1) gDFlix$getUrl$12.L$3;
                        Function1<? super SubtitleFile, Unit> function125 = (Function1) gDFlix$getUrl$12.L$2;
                        String str36 = (String) gDFlix$getUrl$12.L$1;
                        try {
                            ResultKt.throwOnFailure(obj4);
                        } catch (Exception e19) {
                            e = e19;
                            str3 = "GDFlix";
                            function14 = function124;
                            function15 = function125;
                            str4 = str36;
                            Log.INSTANCE.e(str3, "Error in GDFlix: " + e.getMessage());
                        }
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        gDFlix$getUrl$1 = new GDFlix$getUrl$1(this, continuation);
        gDFlix$getUrl$12 = gDFlix$getUrl$1;
        Object obj42 = gDFlix$getUrl$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (gDFlix$getUrl$12.label) {
        }
    }
}
