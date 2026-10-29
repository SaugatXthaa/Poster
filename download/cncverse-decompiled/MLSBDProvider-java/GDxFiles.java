package com.cncverse;

import com.lagradost.api.Log;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorApi;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0016\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JH\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00052\b\u0010\u0011\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u000f0\u00132\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@¢\u0006\u0002\u0010\u0017R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u0018"}, d2 = {"Lcom/cncverse/GDxFiles;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nExtractors.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Extractors.kt\ncom/cncverse/GDxFiles\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,995:1\n1#2:996\n*E\n"})
/* loaded from: classes.dex */
public class GDxFiles extends ExtractorApi {
    private final boolean requiresReferer;

    @NotNull
    private final String name = "GDxFiles";

    @NotNull
    private final String mainUrl = "https://gdxfiles.com";

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:54:0x0e11, code lost:
    
        r0 = kotlin.text.StringsKt.substringBefore$default(kotlin.text.StringsKt.substringAfterLast$default(r9, "/", (java.lang.String) null, 2, (java.lang.Object) null), "?", (java.lang.String) null, 2, (java.lang.Object) null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x0e2c, code lost:
    
        if (kotlin.text.StringsKt.contains$default(r9, "download", false, 2, (java.lang.Object) null) == false) goto L339;
     */
    /* JADX WARN: Code restructure failed: missing block: B:56:0x0e2e, code lost:
    
        r4 = r9;
     */
    /* JADX WARN: Code restructure failed: missing block: B:57:0x0e4a, code lost:
    
        r5 = com.lagradost.cloudstream3.utils.ExtractorApiKt.getINFER_TYPE();
        r6 = new com.cncverse.GDxFiles$getUrl$4(r12, null);
        r7.L$0 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r19);
        r7.L$1 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r22);
        r7.L$2 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r21);
        r7.L$3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r20);
        r7.L$4 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r8);
        r7.L$5 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r18);
        r7.L$6 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r17);
        r7.L$7 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r15);
        r7.L$8 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r16);
        r7.L$9 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r13);
        r7.L$10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r14);
        r7.L$11 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r11);
        r7.L$12 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r0);
        r7.L$13 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r9);
        r7.L$14 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r0);
        r7.L$15 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r4);
        r7.L$16 = r8;
        r7.I$0 = r12;
        r7.label = 10;
        r3 = com.lagradost.cloudstream3.utils.ExtractorApiKt.newExtractorLink("GDxFiles Pixeldrain", "GDxFiles Pixeldrain" + r15, r4, r5, r6, r7);
     */
    /* JADX WARN: Code restructure failed: missing block: B:58:0x0edd, code lost:
    
        if (r3 != r1) goto L343;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x0edf, code lost:
    
        return r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x0ee0, code lost:
    
        r2 = r19;
        r19 = r8;
        r0 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x0e30, code lost:
    
        r4 = "https://pixeldrain.com/api/file/" + r0 + "?download";
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002c. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0037  */
    /* JADX WARN: Removed duplicated region for block: B:120:0x018c  */
    /* JADX WARN: Removed duplicated region for block: B:127:0x0bc0  */
    /* JADX WARN: Removed duplicated region for block: B:134:0x0bd6 A[Catch: Exception -> 0x0bc8, TRY_ENTER, TRY_LEAVE, TryCatch #53 {Exception -> 0x0bc8, blocks: (B:129:0x0bc2, B:134:0x0bd6), top: B:128:0x0bc2 }] */
    /* JADX WARN: Removed duplicated region for block: B:138:0x0be2  */
    /* JADX WARN: Removed duplicated region for block: B:152:0x0c66  */
    /* JADX WARN: Removed duplicated region for block: B:160:0x0bce  */
    /* JADX WARN: Removed duplicated region for block: B:167:0x01f6  */
    /* JADX WARN: Removed duplicated region for block: B:178:0x0d1f  */
    /* JADX WARN: Removed duplicated region for block: B:179:0x0b1e A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:222:0x0270  */
    /* JADX WARN: Removed duplicated region for block: B:236:0x095f A[Catch: Exception -> 0x0943, TRY_ENTER, TRY_LEAVE, TryCatch #56 {Exception -> 0x0943, blocks: (B:261:0x093c, B:236:0x095f), top: B:260:0x093c }] */
    /* JADX WARN: Removed duplicated region for block: B:240:0x096b A[Catch: Exception -> 0x0a51, TRY_ENTER, TRY_LEAVE, TryCatch #49 {Exception -> 0x0a51, blocks: (B:230:0x092f, B:234:0x0959, B:240:0x096b), top: B:229:0x092f }] */
    /* JADX WARN: Removed duplicated region for block: B:251:0x0a3d  */
    /* JADX WARN: Removed duplicated region for block: B:256:0x0935 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:273:0x02d2  */
    /* JADX WARN: Removed duplicated region for block: B:284:0x088b  */
    /* JADX WARN: Removed duplicated region for block: B:306:0x0af9  */
    /* JADX WARN: Removed duplicated region for block: B:328:0x0351  */
    /* JADX WARN: Removed duplicated region for block: B:340:0x0740 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:341:0x0741  */
    /* JADX WARN: Removed duplicated region for block: B:346:0x069d A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:358:0x03bf  */
    /* JADX WARN: Removed duplicated region for block: B:365:0x05de  */
    /* JADX WARN: Removed duplicated region for block: B:371:0x05fe A[Catch: Exception -> 0x05e5, TRY_ENTER, TRY_LEAVE, TryCatch #13 {Exception -> 0x05e5, blocks: (B:367:0x05e0, B:371:0x05fe), top: B:366:0x05e0 }] */
    /* JADX WARN: Removed duplicated region for block: B:375:0x060a A[Catch: Exception -> 0x07b8, TRY_ENTER, TRY_LEAVE, TryCatch #35 {Exception -> 0x07b8, blocks: (B:369:0x05f9, B:375:0x060a), top: B:368:0x05f9 }] */
    /* JADX WARN: Removed duplicated region for block: B:387:0x0794  */
    /* JADX WARN: Removed duplicated region for block: B:395:0x05f6  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00a2  */
    /* JADX WARN: Removed duplicated region for block: B:402:0x0424  */
    /* JADX WARN: Removed duplicated region for block: B:409:0x04e8  */
    /* JADX WARN: Removed duplicated region for block: B:411:0x04ed  */
    /* JADX WARN: Removed duplicated region for block: B:414:0x04ff  */
    /* JADX WARN: Removed duplicated region for block: B:418:0x0508 A[Catch: Exception -> 0x051e, TRY_ENTER, TryCatch #39 {Exception -> 0x051e, blocks: (B:418:0x0508, B:419:0x0519, B:450:0x052e, B:455:0x053a), top: B:416:0x0506 }] */
    /* JADX WARN: Removed duplicated region for block: B:423:0x0558  */
    /* JADX WARN: Removed duplicated region for block: B:445:0x0862  */
    /* JADX WARN: Removed duplicated region for block: B:448:0x0529 A[Catch: Exception -> 0x0fbb, TRY_ENTER, TRY_LEAVE, TryCatch #51 {Exception -> 0x0fbb, blocks: (B:407:0x04c2, B:412:0x04ef, B:420:0x054e, B:448:0x0529), top: B:406:0x04c2 }] */
    /* JADX WARN: Removed duplicated region for block: B:455:0x053a A[Catch: Exception -> 0x051e, TRY_LEAVE, TryCatch #39 {Exception -> 0x051e, blocks: (B:418:0x0508, B:419:0x0519, B:450:0x052e, B:455:0x053a), top: B:416:0x0506 }] */
    /* JADX WARN: Removed duplicated region for block: B:456:0x054c  */
    /* JADX WARN: Removed duplicated region for block: B:460:0x0501  */
    /* JADX WARN: Removed duplicated region for block: B:461:0x04ea  */
    /* JADX WARN: Removed duplicated region for block: B:467:0x045f  */
    /* JADX WARN: Removed duplicated region for block: B:46:0x0df8 A[Catch: Exception -> 0x0f0c, TryCatch #17 {Exception -> 0x0f0c, blocks: (B:44:0x0de9, B:46:0x0df8, B:47:0x0e00, B:49:0x0e06, B:54:0x0e11, B:57:0x0e4a, B:62:0x0e30), top: B:43:0x0de9 }] */
    /* JADX WARN: Removed duplicated region for block: B:64:0x0dff  */
    /* JADX WARN: Removed duplicated region for block: B:70:0x0125  */
    /* JADX WARN: Removed duplicated region for block: B:79:0x0f95  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x0d40 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static /* synthetic */ Object getUrl$suspendImpl(GDxFiles gDxFiles, String str, String str2, Function1<? super SubtitleFile, Unit> function1, Function1<? super ExtractorLink, Unit> function12, Continuation<? super Unit> continuation) {
        GDxFiles$getUrl$1 gDxFiles$getUrl$1;
        GDxFiles gDxFiles2;
        GDxFiles$getUrl$1 gDxFiles$getUrl$12;
        String str3;
        String str4;
        Function1<? super ExtractorLink, Unit> function13;
        Requests app;
        Object obj;
        String str5;
        String str6;
        int i;
        Object obj2;
        GDxFiles gDxFiles3;
        String str7;
        Function1<? super SubtitleFile, Unit> function14;
        Function1<? super ExtractorLink, Unit> function15;
        Object obj3;
        String str8;
        Document document;
        String obj4;
        String str9;
        int indexQuality;
        boolean z;
        String str10;
        StringBuilder append;
        Element selectFirst;
        Element element;
        String str11;
        String str12;
        int i2;
        String str13;
        String str14;
        Function1<? super SubtitleFile, Unit> function16;
        Document document2;
        String str15;
        String attr;
        Function1<? super SubtitleFile, Unit> function17;
        int i3;
        String str16;
        Element element2;
        Function1<? super ExtractorLink, Unit> function18;
        String str17;
        String str18;
        Function1<? super ExtractorLink, Unit> function19;
        String str19;
        Object obj5;
        String str20;
        String str21;
        int i4;
        String str22;
        Function1<? super SubtitleFile, Unit> function110;
        Function1<? super ExtractorLink, Unit> function111;
        int i5;
        Function1<? super ExtractorLink, Unit> function112;
        String str23;
        Document document3;
        Element selectFirst2;
        String str24;
        String str25;
        String str26;
        boolean z2;
        Object obj6;
        Document document4;
        String str27;
        String str28;
        String str29;
        Element element3;
        int i6;
        String str30;
        Function1<? super SubtitleFile, Unit> function113;
        String str31;
        String str32;
        Function1<? super ExtractorLink, Unit> function114;
        GDxFiles gDxFiles4;
        Document document5;
        Function1<? super ExtractorLink, Unit> function115;
        Element selectFirst3;
        Element element4;
        String str33;
        String str34;
        String str35;
        Function1<? super ExtractorLink, Unit> function116;
        Document document6;
        int i7;
        String attr2;
        String str36;
        Element element5;
        String str37;
        Element element6;
        String str38;
        Function1<? super SubtitleFile, Unit> function117;
        Function1<? super ExtractorLink, Unit> function118;
        Document document7;
        int i8;
        String str39;
        Function1<? super ExtractorLink, Unit> function119;
        int i9;
        String str40;
        Document document8;
        Function1<? super SubtitleFile, Unit> function120;
        String str41;
        Object obj7;
        String str42;
        String str43;
        Element element7;
        Document document9;
        int i10;
        String text;
        Element element8;
        Element element9;
        MatchResult find$default;
        List groupValues;
        String str44;
        String str45;
        String str46;
        boolean z3;
        Function1<? super SubtitleFile, Unit> function121;
        String str47;
        GDxFiles gDxFiles5;
        Element element10;
        Function1<? super ExtractorLink, Unit> function122;
        Element selectFirst4;
        String str48;
        Function1<? super SubtitleFile, Unit> function123;
        Function1<? super ExtractorLink, Unit> function124;
        String str49;
        String str50;
        Document document10;
        int i11;
        Element element11;
        boolean z4;
        String str51;
        Element element12;
        String str52;
        Document document11;
        String str53;
        String str54;
        Function1<? super SubtitleFile, Unit> function125;
        Function1<? super ExtractorLink, Unit> function126;
        Element element13;
        int i12;
        Requests app2;
        String str55;
        Function1<? super ExtractorLink, Unit> function127;
        String str56;
        Document document12;
        int i13;
        Function1<? super SubtitleFile, Unit> function128;
        String str57;
        String str58;
        String str59;
        String str60;
        Function1<? super SubtitleFile, Unit> function129;
        Element element14;
        Function1<? super ExtractorLink, Unit> function130;
        int i14;
        Document document13;
        Element selectFirst5;
        String str61;
        String str62;
        String str63;
        boolean z5;
        Gofile gofile;
        Function1<? super SubtitleFile, Unit> function131;
        Function1<? super ExtractorLink, Unit> function132;
        GDxFiles gDxFiles6;
        Element element15;
        String str64;
        Element selectFirst6;
        Function1<? super ExtractorLink, Unit> function133;
        String str65;
        String str66;
        Document document14;
        Element element16;
        String str67;
        Function1<? super SubtitleFile, Unit> function134;
        Function1<? super ExtractorLink, Unit> function135;
        String str68;
        int i15;
        String str69;
        Document document15;
        String str70;
        Function1<? super ExtractorLink, Unit> function136;
        Element element17;
        String str71;
        Document document16;
        Element element18;
        Element element19;
        String str72;
        Element element20;
        int i16;
        String str73;
        Function1<? super ExtractorLink, Unit> function137;
        if (continuation instanceof GDxFiles$getUrl$1) {
            gDxFiles$getUrl$1 = (GDxFiles$getUrl$1) continuation;
            if ((gDxFiles$getUrl$1.label & Integer.MIN_VALUE) != 0) {
                gDxFiles$getUrl$1.label -= Integer.MIN_VALUE;
                gDxFiles2 = gDxFiles;
                gDxFiles$getUrl$12 = gDxFiles$getUrl$1;
                Object obj8 = gDxFiles$getUrl$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                String str74 = "href";
                switch (gDxFiles$getUrl$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj8);
                        try {
                            app = MainActivityKt.getApp();
                            gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles2);
                            gDxFiles$getUrl$12.L$1 = str;
                            gDxFiles$getUrl$12.L$2 = str2;
                            gDxFiles$getUrl$12.L$3 = function1;
                            gDxFiles$getUrl$12.L$4 = function12;
                            gDxFiles$getUrl$12.label = 1;
                            obj = coroutine_suspended;
                            str5 = "href";
                            str6 = "GDxFiles";
                            i = 2;
                        } catch (Exception e) {
                            e = e;
                            str3 = "GDxFiles";
                            str4 = str2;
                            function13 = function12;
                        }
                        try {
                            obj2 = Requests.get$default(app, str, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDxFiles$getUrl$12, 4094, (Object) null);
                            gDxFiles$getUrl$12 = gDxFiles$getUrl$12;
                        } catch (Exception e2) {
                            e = e2;
                            str3 = str6;
                            str4 = str2;
                            function13 = function12;
                            Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        if (obj2 == obj) {
                            return obj;
                        }
                        gDxFiles3 = gDxFiles;
                        str7 = str;
                        function14 = function1;
                        function15 = function12;
                        obj3 = obj2;
                        str8 = str2;
                        try {
                            document = ((NiceResponse) obj3).getDocument();
                            obj4 = StringsKt.trim(StringsKt.substringBefore$default(document.title(), "|", (String) null, i, (Object) null)).toString();
                            str9 = obj4;
                            if (str9.length() != 0) {
                                str9 = str8;
                            }
                            indexQuality = ExtractorsKt.getIndexQuality(str9);
                            try {
                            } catch (Exception e3) {
                                e = e3;
                                str4 = str8;
                                function13 = function15;
                                str3 = str6;
                            }
                        } catch (Exception e4) {
                            e = e4;
                            str4 = str8;
                            function13 = function15;
                            str3 = str6;
                        }
                        if (obj4.length() <= 0) {
                            String str75 = str8;
                            if (str75 != null && str75.length() != 0) {
                                z = false;
                                if (z) {
                                    append = new StringBuilder().append(" [").append(str8).append("]");
                                } else {
                                    str10 = "";
                                    String str76 = str10;
                                    selectFirst = document.selectFirst("a[href*='/file/r2/']");
                                    if (selectFirst != null) {
                                        String str77 = str5;
                                        try {
                                            attr = selectFirst.attr(str77);
                                            try {
                                                Requests app3 = MainActivityKt.getApp();
                                                gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                                                gDxFiles$getUrl$12.L$1 = str7;
                                                gDxFiles$getUrl$12.L$2 = str8;
                                                gDxFiles$getUrl$12.L$3 = function14;
                                                gDxFiles$getUrl$12.L$4 = function15;
                                                gDxFiles$getUrl$12.L$5 = document;
                                                gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(obj4);
                                                gDxFiles$getUrl$12.L$7 = str76;
                                                gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(selectFirst);
                                                gDxFiles$getUrl$12.L$9 = attr;
                                                gDxFiles$getUrl$12.I$0 = indexQuality;
                                                gDxFiles$getUrl$12.label = i;
                                                function19 = function15;
                                                GDxFiles$getUrl$1 gDxFiles$getUrl$13 = gDxFiles$getUrl$12;
                                                element = selectFirst;
                                                str4 = str8;
                                                function17 = function14;
                                                str19 = str77;
                                                try {
                                                    obj5 = Requests.get$default(app3, attr, (Map) null, str7, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDxFiles$getUrl$13, 4090, (Object) null);
                                                    gDxFiles$getUrl$12 = gDxFiles$getUrl$13;
                                                } catch (Exception e5) {
                                                    e = e5;
                                                    gDxFiles$getUrl$12 = gDxFiles$getUrl$13;
                                                    str12 = str19;
                                                    i3 = indexQuality;
                                                    str16 = str76;
                                                    element2 = element;
                                                    function18 = function19;
                                                    document2 = document;
                                                    str17 = str7;
                                                    str18 = obj4;
                                                    try {
                                                        str11 = str6;
                                                    } catch (Exception e6) {
                                                        e = e6;
                                                        str11 = str6;
                                                    }
                                                    try {
                                                        Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                                                        i2 = i3;
                                                        function15 = function18;
                                                        str13 = str16;
                                                        str15 = str18;
                                                        str7 = str17;
                                                        str14 = str4;
                                                        function16 = function17;
                                                        element = element2;
                                                        try {
                                                            selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                            if (selectFirst3 == null) {
                                                            }
                                                        } catch (Exception e7) {
                                                            e = e7;
                                                            str4 = str14;
                                                            str3 = str11;
                                                            function13 = function15;
                                                        }
                                                    } catch (Exception e8) {
                                                        e = e8;
                                                        str3 = str11;
                                                        function13 = function18;
                                                        Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                                                        return Unit.INSTANCE;
                                                    }
                                                }
                                            } catch (Exception e9) {
                                                e = e9;
                                                str4 = str8;
                                                function17 = function14;
                                                Function1<? super ExtractorLink, Unit> function138 = function15;
                                                str12 = str77;
                                                i3 = indexQuality;
                                                str16 = str76;
                                                element2 = selectFirst;
                                                function18 = function138;
                                                document2 = document;
                                                str17 = str7;
                                                str18 = obj4;
                                            }
                                        } catch (Exception e10) {
                                            e = e10;
                                            str4 = str8;
                                            function13 = function15;
                                            str3 = str6;
                                            Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                                            return Unit.INSTANCE;
                                        }
                                        if (obj5 == obj) {
                                            return obj;
                                        }
                                        str20 = attr;
                                        str21 = str7;
                                        i4 = indexQuality;
                                        str13 = str76;
                                        str22 = str4;
                                        function110 = function17;
                                        function111 = function19;
                                        document2 = document;
                                        obj8 = obj5;
                                        str18 = obj4;
                                        try {
                                            document3 = ((NiceResponse) obj8).getDocument();
                                            selectFirst2 = document3.selectFirst("a[href*='/download']");
                                            if (selectFirst2 == null) {
                                                str24 = str19;
                                                try {
                                                    str25 = selectFirst2.attr(str24);
                                                } catch (Exception e11) {
                                                    e = e11;
                                                    function17 = function110;
                                                    str4 = str22;
                                                    str16 = str13;
                                                    str12 = str24;
                                                    element2 = element;
                                                    i3 = i4;
                                                    function18 = function111;
                                                    str17 = str21;
                                                    str11 = str6;
                                                    Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                                                    i2 = i3;
                                                    function15 = function18;
                                                    str13 = str16;
                                                    str15 = str18;
                                                    str7 = str17;
                                                    str14 = str4;
                                                    function16 = function17;
                                                    element = element2;
                                                    selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                    if (selectFirst3 == null) {
                                                    }
                                                }
                                            } else {
                                                str24 = str19;
                                                str25 = null;
                                            }
                                            try {
                                                str26 = str25;
                                            } catch (Exception e12) {
                                                e = e12;
                                                i5 = i4;
                                                function112 = function111;
                                                function17 = function110;
                                                str4 = str22;
                                                str23 = str21;
                                                str16 = str13;
                                                str12 = str24;
                                                element2 = element;
                                                function18 = function112;
                                                i3 = i5;
                                                str17 = str23;
                                                str11 = str6;
                                                Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                                                i2 = i3;
                                                function15 = function18;
                                                str13 = str16;
                                                str15 = str18;
                                                str7 = str17;
                                                str14 = str4;
                                                function16 = function17;
                                                element = element2;
                                                selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                if (selectFirst3 == null) {
                                                }
                                            }
                                        } catch (Exception e13) {
                                            e = e13;
                                            i5 = i4;
                                            function112 = function111;
                                            function17 = function110;
                                            str4 = str22;
                                            str23 = str21;
                                            str16 = str13;
                                            str12 = str19;
                                        }
                                        if (str26 != null) {
                                            if (str26.length() != 0) {
                                                z2 = false;
                                                if (z2) {
                                                    int i17 = i4;
                                                    Function1<? super ExtractorLink, Unit> function139 = function111;
                                                    Function1<? super SubtitleFile, Unit> function140 = function110;
                                                    str12 = str24;
                                                    str7 = str21;
                                                    str15 = str18;
                                                    str14 = str22;
                                                    str11 = str6;
                                                    function16 = function140;
                                                    function15 = function139;
                                                    i2 = i17;
                                                    selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                    if (selectFirst3 == null) {
                                                    }
                                                } else {
                                                    Requests app4 = MainActivityKt.getApp();
                                                    gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                                                    gDxFiles$getUrl$12.L$1 = str21;
                                                    gDxFiles$getUrl$12.L$2 = str22;
                                                    gDxFiles$getUrl$12.L$3 = function110;
                                                    gDxFiles$getUrl$12.L$4 = function111;
                                                    gDxFiles$getUrl$12.L$5 = document2;
                                                    gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str18);
                                                    gDxFiles$getUrl$12.L$7 = str13;
                                                    gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(element);
                                                    gDxFiles$getUrl$12.L$9 = str20;
                                                    gDxFiles$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(document3);
                                                    gDxFiles$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(selectFirst2);
                                                    gDxFiles$getUrl$12.L$12 = str25;
                                                    gDxFiles$getUrl$12.I$0 = i4;
                                                    gDxFiles$getUrl$12.label = 3;
                                                    Function1<? super ExtractorLink, Unit> function141 = function111;
                                                    str4 = str22;
                                                    GDxFiles$getUrl$1 gDxFiles$getUrl$14 = gDxFiles$getUrl$12;
                                                    String str78 = str21;
                                                    str16 = str13;
                                                    Document document17 = document2;
                                                    String str79 = str25;
                                                    int i18 = i4;
                                                    function17 = function110;
                                                    String str80 = str20;
                                                    str12 = str24;
                                                    try {
                                                        obj6 = Requests.get$default(app4, str79, (Map) null, str80, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDxFiles$getUrl$14, 4058, (Object) null);
                                                        gDxFiles$getUrl$12 = gDxFiles$getUrl$14;
                                                    } catch (Exception e14) {
                                                        e = e14;
                                                        gDxFiles$getUrl$12 = gDxFiles$getUrl$14;
                                                        element2 = element;
                                                        function18 = function141;
                                                        document2 = document17;
                                                        i3 = i18;
                                                        str17 = str78;
                                                        str11 = str6;
                                                        Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                                                        i2 = i3;
                                                        function15 = function18;
                                                        str13 = str16;
                                                        str15 = str18;
                                                        str7 = str17;
                                                        str14 = str4;
                                                        function16 = function17;
                                                        element = element2;
                                                        selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                        if (selectFirst3 == null) {
                                                        }
                                                    }
                                                    if (obj6 == obj) {
                                                        return obj;
                                                    }
                                                    document4 = document3;
                                                    str27 = str80;
                                                    str28 = str78;
                                                    element2 = element;
                                                    str29 = str4;
                                                    function18 = function141;
                                                    document2 = document17;
                                                    element3 = selectFirst2;
                                                    i6 = i18;
                                                    str30 = str79;
                                                    obj8 = obj6;
                                                    function113 = function17;
                                                    str31 = str16;
                                                    try {
                                                        NiceResponse niceResponse = (NiceResponse) obj8;
                                                        str32 = niceResponse.getHeaders().get("location");
                                                        if (str32 == null) {
                                                            try {
                                                                str32 = niceResponse.getHeaders().get("Location");
                                                                if (str32 == null) {
                                                                    str32 = str30;
                                                                }
                                                            } catch (Exception e15) {
                                                                e = e15;
                                                                function17 = function113;
                                                                str4 = str29;
                                                                str17 = str28;
                                                                i3 = i6;
                                                                str16 = str31;
                                                                str11 = str6;
                                                                Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                                                                i2 = i3;
                                                                function15 = function18;
                                                                str13 = str16;
                                                                str15 = str18;
                                                                str7 = str17;
                                                                str14 = str4;
                                                                function16 = function17;
                                                                element = element2;
                                                                selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                                if (selectFirst3 == null) {
                                                                }
                                                            }
                                                        }
                                                        String str81 = str32;
                                                        String str82 = "GDxFiles [Direct R2]" + str31;
                                                        ExtractorLinkType infer_type = ExtractorApiKt.getINFER_TYPE();
                                                        GDxFiles$getUrl$2 gDxFiles$getUrl$2 = new GDxFiles$getUrl$2(i6, str27, null);
                                                        gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                                                        gDxFiles$getUrl$12.L$1 = str28;
                                                        gDxFiles$getUrl$12.L$2 = str29;
                                                        gDxFiles$getUrl$12.L$3 = function113;
                                                        gDxFiles$getUrl$12.L$4 = function18;
                                                        gDxFiles$getUrl$12.L$5 = document2;
                                                        gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str18);
                                                        gDxFiles$getUrl$12.L$7 = str31;
                                                        gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(element2);
                                                        gDxFiles$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str27);
                                                        gDxFiles$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(document4);
                                                        gDxFiles$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(element3);
                                                        gDxFiles$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str30);
                                                        gDxFiles$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                                        gDxFiles$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str81);
                                                        gDxFiles$getUrl$12.L$15 = function18;
                                                        gDxFiles$getUrl$12.I$0 = i6;
                                                        gDxFiles$getUrl$12.label = 4;
                                                        function17 = function113;
                                                        str4 = str29;
                                                        str17 = str28;
                                                    } catch (Exception e16) {
                                                        e = e16;
                                                        function17 = function113;
                                                        str4 = str29;
                                                        str17 = str28;
                                                        i3 = i6;
                                                        str16 = str31;
                                                    }
                                                    try {
                                                        obj8 = ExtractorApiKt.newExtractorLink("GDxFiles [Direct R2]", str82, str81, infer_type, gDxFiles$getUrl$2, gDxFiles$getUrl$12);
                                                    } catch (Exception e17) {
                                                        e = e17;
                                                        i3 = i6;
                                                        str16 = str31;
                                                        str11 = str6;
                                                        Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                                                        i2 = i3;
                                                        function15 = function18;
                                                        str13 = str16;
                                                        str15 = str18;
                                                        str7 = str17;
                                                        str14 = str4;
                                                        function16 = function17;
                                                        element = element2;
                                                        selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                        if (selectFirst3 == null) {
                                                        }
                                                    }
                                                    if (obj8 != obj) {
                                                        return obj;
                                                    }
                                                    i3 = i6;
                                                    str16 = str31;
                                                    function114 = function18;
                                                    gDxFiles4 = gDxFiles3;
                                                    str7 = str17;
                                                    document5 = document2;
                                                    function115 = function114;
                                                    try {
                                                        function114.invoke(obj8);
                                                        gDxFiles3 = gDxFiles4;
                                                        document2 = document5;
                                                        i2 = i3;
                                                        str13 = str16;
                                                        str15 = str18;
                                                        function15 = function115;
                                                        str14 = str4;
                                                        str11 = str6;
                                                        function16 = function17;
                                                        element = element2;
                                                    } catch (Exception e18) {
                                                        e = e18;
                                                        gDxFiles3 = gDxFiles4;
                                                        document2 = document5;
                                                        function18 = function115;
                                                        str17 = str7;
                                                        str11 = str6;
                                                        Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                                                        i2 = i3;
                                                        function15 = function18;
                                                        str13 = str16;
                                                        str15 = str18;
                                                        str7 = str17;
                                                        str14 = str4;
                                                        function16 = function17;
                                                        element = element2;
                                                        selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                        if (selectFirst3 == null) {
                                                        }
                                                    }
                                                    selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                                    if (selectFirst3 == null) {
                                                        String str83 = str12;
                                                        try {
                                                            attr2 = selectFirst3.attr(str83);
                                                            try {
                                                                Requests app5 = MainActivityKt.getApp();
                                                                gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                                                                gDxFiles$getUrl$12.L$1 = str7;
                                                                gDxFiles$getUrl$12.L$2 = str14;
                                                                gDxFiles$getUrl$12.L$3 = function16;
                                                                gDxFiles$getUrl$12.L$4 = function15;
                                                                gDxFiles$getUrl$12.L$5 = document2;
                                                                gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str15);
                                                                gDxFiles$getUrl$12.L$7 = str13;
                                                                gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(element);
                                                                gDxFiles$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(selectFirst3);
                                                                gDxFiles$getUrl$12.L$10 = attr2;
                                                                gDxFiles$getUrl$12.L$11 = null;
                                                                gDxFiles$getUrl$12.L$12 = null;
                                                                gDxFiles$getUrl$12.L$13 = null;
                                                                gDxFiles$getUrl$12.L$14 = null;
                                                                gDxFiles$getUrl$12.L$15 = null;
                                                                gDxFiles$getUrl$12.I$0 = i2;
                                                                gDxFiles$getUrl$12.label = 5;
                                                                function119 = function15;
                                                                GDxFiles$getUrl$1 gDxFiles$getUrl$15 = gDxFiles$getUrl$12;
                                                                i9 = i2;
                                                                str40 = str13;
                                                                document8 = document2;
                                                                function120 = function16;
                                                                str41 = str14;
                                                                str36 = str11;
                                                                str33 = str83;
                                                                try {
                                                                    obj7 = Requests.get$default(app5, attr2, (Map) null, str7, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDxFiles$getUrl$15, 4090, (Object) null);
                                                                    gDxFiles$getUrl$12 = gDxFiles$getUrl$15;
                                                                } catch (Exception e19) {
                                                                    e = e19;
                                                                    gDxFiles$getUrl$12 = gDxFiles$getUrl$15;
                                                                    element5 = selectFirst3;
                                                                    str37 = str40;
                                                                    element6 = element;
                                                                    str38 = str41;
                                                                    function117 = function120;
                                                                    function118 = function119;
                                                                    document7 = document8;
                                                                    i8 = i9;
                                                                    str39 = str7;
                                                                    try {
                                                                        str34 = str36;
                                                                    } catch (Exception e20) {
                                                                        e = e20;
                                                                        str34 = str36;
                                                                    }
                                                                    try {
                                                                        Log.INSTANCE.e(str34, "Watch error: " + e.getMessage());
                                                                        i7 = i8;
                                                                        function116 = function118;
                                                                        function16 = function117;
                                                                        element = element6;
                                                                        str35 = str37;
                                                                        str7 = str39;
                                                                        document6 = document7;
                                                                        element4 = element5;
                                                                        str14 = str38;
                                                                    } catch (Exception e21) {
                                                                        e = e21;
                                                                        str3 = str34;
                                                                        function13 = function118;
                                                                        str4 = str38;
                                                                        Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                                                                        return Unit.INSTANCE;
                                                                    }
                                                                    try {
                                                                        selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                                                                        if (selectFirst4 != null) {
                                                                        }
                                                                    } catch (Exception e22) {
                                                                        e = e22;
                                                                        function13 = function116;
                                                                        str3 = str34;
                                                                        str4 = str14;
                                                                        Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                                                                        return Unit.INSTANCE;
                                                                    }
                                                                }
                                                            } catch (Exception e23) {
                                                                e = e23;
                                                                String str84 = str14;
                                                                str36 = str11;
                                                                str33 = str83;
                                                                element5 = selectFirst3;
                                                                str37 = str13;
                                                                element6 = element;
                                                                str38 = str84;
                                                                function117 = function16;
                                                                function118 = function15;
                                                                document7 = document2;
                                                                i8 = i2;
                                                                str39 = str7;
                                                            }
                                                        } catch (Exception e24) {
                                                            e = e24;
                                                            str4 = str14;
                                                            function13 = function15;
                                                            str3 = str11;
                                                            Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                                                            return Unit.INSTANCE;
                                                        }
                                                        if (obj7 == obj) {
                                                            return obj;
                                                        }
                                                        str42 = attr2;
                                                        str43 = str7;
                                                        element7 = selectFirst3;
                                                        str37 = str40;
                                                        element6 = element;
                                                        str14 = str41;
                                                        function117 = function120;
                                                        function118 = function119;
                                                        document9 = document8;
                                                        i10 = i9;
                                                        obj8 = obj7;
                                                        try {
                                                            text = ((NiceResponse) obj8).getText();
                                                            element8 = element7;
                                                            element9 = element6;
                                                            try {
                                                                find$default = Regex.find$default(new Regex("const\\s+SRC\\s*=\\s*[\"']([^\"']+)[\"']"), text, 0, 2, (Object) null);
                                                            } catch (Exception e25) {
                                                                e = e25;
                                                                str38 = str14;
                                                                str39 = str43;
                                                                element5 = element8;
                                                                element6 = element9;
                                                                i8 = i10;
                                                                document7 = document9;
                                                            }
                                                        } catch (Exception e26) {
                                                            e = e26;
                                                            str39 = str43;
                                                            Element element21 = element7;
                                                            str38 = str14;
                                                            element5 = element21;
                                                            i8 = i10;
                                                            document7 = document9;
                                                        }
                                                        if (find$default != null) {
                                                            try {
                                                                groupValues = find$default.getGroupValues();
                                                            } catch (Exception e27) {
                                                                e = e27;
                                                                element6 = element9;
                                                                str38 = str14;
                                                                str39 = str43;
                                                                i8 = i10;
                                                                document7 = document9;
                                                                element5 = element8;
                                                                str34 = str36;
                                                                Log.INSTANCE.e(str34, "Watch error: " + e.getMessage());
                                                                i7 = i8;
                                                                function116 = function118;
                                                                function16 = function117;
                                                                element = element6;
                                                                str35 = str37;
                                                                str7 = str39;
                                                                document6 = document7;
                                                                element4 = element5;
                                                                str14 = str38;
                                                                selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                                                                if (selectFirst4 != null) {
                                                                }
                                                            }
                                                            if (groupValues != null) {
                                                                try {
                                                                    str44 = (String) groupValues.get(1);
                                                                    str45 = str44;
                                                                    str46 = str45;
                                                                } catch (Exception e28) {
                                                                    e = e28;
                                                                    element6 = element9;
                                                                    str38 = str14;
                                                                    str39 = str43;
                                                                    i8 = i10;
                                                                    document7 = document9;
                                                                    element5 = element8;
                                                                    str34 = str36;
                                                                    Log.INSTANCE.e(str34, "Watch error: " + e.getMessage());
                                                                    i7 = i8;
                                                                    function116 = function118;
                                                                    function16 = function117;
                                                                    element = element6;
                                                                    str35 = str37;
                                                                    str7 = str39;
                                                                    document6 = document7;
                                                                    element4 = element5;
                                                                    str14 = str38;
                                                                    selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                                                                    if (selectFirst4 != null) {
                                                                    }
                                                                }
                                                                if (str46 != null) {
                                                                    if (str46.length() != 0) {
                                                                        z3 = false;
                                                                        if (z3) {
                                                                            String replace$default = StringsKt.replace$default(StringsKt.replace$default(str45, "\\/", "/", false, 4, (Object) null), "\\u0026", "&", false, 4, (Object) null);
                                                                            String str85 = "GDxFiles [Stream]" + str37;
                                                                            ExtractorLinkType infer_type2 = ExtractorApiKt.getINFER_TYPE();
                                                                            GDxFiles$getUrl$3 gDxFiles$getUrl$3 = new GDxFiles$getUrl$3(i10, str42, null);
                                                                            gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                                                                            gDxFiles$getUrl$12.L$1 = str43;
                                                                            gDxFiles$getUrl$12.L$2 = str14;
                                                                            gDxFiles$getUrl$12.L$3 = function117;
                                                                            gDxFiles$getUrl$12.L$4 = function118;
                                                                            gDxFiles$getUrl$12.L$5 = document9;
                                                                            gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str15);
                                                                            gDxFiles$getUrl$12.L$7 = str37;
                                                                            gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(element9);
                                                                            gDxFiles$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(element8);
                                                                            gDxFiles$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str42);
                                                                            gDxFiles$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(text);
                                                                            gDxFiles$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str45);
                                                                            gDxFiles$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(replace$default);
                                                                            gDxFiles$getUrl$12.L$14 = function118;
                                                                            gDxFiles$getUrl$12.I$0 = i10;
                                                                            gDxFiles$getUrl$12.label = 6;
                                                                            str39 = str43;
                                                                            str38 = str14;
                                                                            try {
                                                                                obj8 = ExtractorApiKt.newExtractorLink("GDxFiles [Stream]", str85, replace$default, infer_type2, gDxFiles$getUrl$3, gDxFiles$getUrl$12);
                                                                            } catch (Exception e29) {
                                                                                e = e29;
                                                                                element5 = element8;
                                                                                element6 = element9;
                                                                                i8 = i10;
                                                                                document7 = document9;
                                                                                str34 = str36;
                                                                                Log.INSTANCE.e(str34, "Watch error: " + e.getMessage());
                                                                                i7 = i8;
                                                                                function116 = function118;
                                                                                function16 = function117;
                                                                                element = element6;
                                                                                str35 = str37;
                                                                                str7 = str39;
                                                                                document6 = document7;
                                                                                element4 = element5;
                                                                                str14 = str38;
                                                                                selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                                                                                if (selectFirst4 != null) {
                                                                                }
                                                                            }
                                                                            if (obj8 == obj) {
                                                                                return obj;
                                                                            }
                                                                            element6 = element9;
                                                                            i8 = i10;
                                                                            document7 = document9;
                                                                            function121 = function117;
                                                                            str47 = str38;
                                                                            gDxFiles5 = gDxFiles3;
                                                                            element10 = element8;
                                                                            function122 = function118;
                                                                            function116 = function122;
                                                                            try {
                                                                                function122.invoke(obj8);
                                                                                gDxFiles3 = gDxFiles5;
                                                                                str14 = str47;
                                                                                i7 = i8;
                                                                                str35 = str37;
                                                                                str7 = str39;
                                                                                document6 = document7;
                                                                                function16 = function121;
                                                                                str34 = str36;
                                                                                element4 = element10;
                                                                                element = element6;
                                                                            } catch (Exception e30) {
                                                                                e = e30;
                                                                                gDxFiles3 = gDxFiles5;
                                                                                function118 = function116;
                                                                                element5 = element10;
                                                                                function117 = function121;
                                                                                str38 = str47;
                                                                                str34 = str36;
                                                                                Log.INSTANCE.e(str34, "Watch error: " + e.getMessage());
                                                                                i7 = i8;
                                                                                function116 = function118;
                                                                                function16 = function117;
                                                                                element = element6;
                                                                                str35 = str37;
                                                                                str7 = str39;
                                                                                document6 = document7;
                                                                                element4 = element5;
                                                                                str14 = str38;
                                                                                selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                                                                                if (selectFirst4 != null) {
                                                                                }
                                                                            }
                                                                            selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                                                                            if (selectFirst4 != null) {
                                                                                try {
                                                                                    app2 = MainActivityKt.getApp();
                                                                                    str55 = str33;
                                                                                } catch (Exception e31) {
                                                                                    e = e31;
                                                                                    str48 = str14;
                                                                                    function123 = function16;
                                                                                    function124 = function116;
                                                                                    str49 = str35;
                                                                                    str50 = str34;
                                                                                    document10 = document6;
                                                                                    i11 = i7;
                                                                                    element11 = selectFirst4;
                                                                                    str74 = str33;
                                                                                }
                                                                                try {
                                                                                    String attr3 = selectFirst4.attr(str55);
                                                                                    gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                                                                                    gDxFiles$getUrl$12.L$1 = str7;
                                                                                    gDxFiles$getUrl$12.L$2 = str14;
                                                                                    gDxFiles$getUrl$12.L$3 = function16;
                                                                                    gDxFiles$getUrl$12.L$4 = function116;
                                                                                    gDxFiles$getUrl$12.L$5 = document6;
                                                                                    gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str15);
                                                                                    gDxFiles$getUrl$12.L$7 = str35;
                                                                                    gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(element);
                                                                                    gDxFiles$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(element4);
                                                                                    gDxFiles$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(selectFirst4);
                                                                                    gDxFiles$getUrl$12.L$11 = null;
                                                                                    gDxFiles$getUrl$12.L$12 = null;
                                                                                    gDxFiles$getUrl$12.L$13 = null;
                                                                                    gDxFiles$getUrl$12.L$14 = null;
                                                                                    gDxFiles$getUrl$12.L$15 = null;
                                                                                    gDxFiles$getUrl$12.I$0 = i7;
                                                                                    gDxFiles$getUrl$12.label = 7;
                                                                                    function127 = function116;
                                                                                    str56 = str35;
                                                                                    GDxFiles$getUrl$1 gDxFiles$getUrl$16 = gDxFiles$getUrl$12;
                                                                                    document12 = document6;
                                                                                    i13 = i7;
                                                                                    function128 = function16;
                                                                                    str50 = str34;
                                                                                    str57 = str55;
                                                                                    z4 = true;
                                                                                    str58 = str14;
                                                                                    try {
                                                                                        obj8 = Requests.get$default(app2, attr3, (Map) null, str7, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDxFiles$getUrl$16, 4090, (Object) null);
                                                                                        gDxFiles$getUrl$12 = gDxFiles$getUrl$16;
                                                                                    } catch (Exception e32) {
                                                                                        e = e32;
                                                                                        gDxFiles$getUrl$12 = gDxFiles$getUrl$16;
                                                                                        str74 = str57;
                                                                                        str51 = str7;
                                                                                        element12 = element4;
                                                                                        str52 = str15;
                                                                                        document11 = document12;
                                                                                        str53 = str56;
                                                                                        str54 = str58;
                                                                                        function125 = function128;
                                                                                        function126 = function127;
                                                                                        element13 = selectFirst4;
                                                                                        i12 = i13;
                                                                                        try {
                                                                                            str3 = str50;
                                                                                            try {
                                                                                                Log.INSTANCE.e(str3, "Gofile error: " + e.getMessage());
                                                                                                str60 = str54;
                                                                                                function129 = function125;
                                                                                                function130 = function126;
                                                                                                str35 = str53;
                                                                                                document6 = document11;
                                                                                                str15 = str52;
                                                                                                i14 = i12;
                                                                                                element14 = element13;
                                                                                                element4 = element12;
                                                                                                str7 = str51;
                                                                                            } catch (Exception e33) {
                                                                                                e = e33;
                                                                                                str4 = str54;
                                                                                                function13 = function126;
                                                                                                Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                                                                                                return Unit.INSTANCE;
                                                                                            }
                                                                                        } catch (Exception e34) {
                                                                                            e = e34;
                                                                                            str3 = str50;
                                                                                        }
                                                                                        try {
                                                                                            selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                                                                                            if (selectFirst6 != null) {
                                                                                            }
                                                                                        } catch (Exception e35) {
                                                                                            e = e35;
                                                                                            function13 = function130;
                                                                                            str4 = str60;
                                                                                            Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                                                                                            return Unit.INSTANCE;
                                                                                        }
                                                                                    }
                                                                                } catch (Exception e36) {
                                                                                    e = e36;
                                                                                    str48 = str14;
                                                                                    function123 = function16;
                                                                                    function124 = function116;
                                                                                    str49 = str35;
                                                                                    str50 = str34;
                                                                                    document10 = document6;
                                                                                    i11 = i7;
                                                                                    element11 = selectFirst4;
                                                                                    str74 = str55;
                                                                                    z4 = true;
                                                                                    str51 = str7;
                                                                                    element12 = element4;
                                                                                    str52 = str15;
                                                                                    document11 = document10;
                                                                                    str53 = str49;
                                                                                    str54 = str48;
                                                                                    function125 = function123;
                                                                                    function126 = function124;
                                                                                    element13 = element11;
                                                                                    i12 = i11;
                                                                                    str3 = str50;
                                                                                    Log.INSTANCE.e(str3, "Gofile error: " + e.getMessage());
                                                                                    str60 = str54;
                                                                                    function129 = function125;
                                                                                    function130 = function126;
                                                                                    str35 = str53;
                                                                                    document6 = document11;
                                                                                    str15 = str52;
                                                                                    i14 = i12;
                                                                                    element14 = element13;
                                                                                    element4 = element12;
                                                                                    str7 = str51;
                                                                                    selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                                                                                    if (selectFirst6 != null) {
                                                                                    }
                                                                                }
                                                                                if (obj8 == obj) {
                                                                                    return obj;
                                                                                }
                                                                                str59 = str7;
                                                                                element12 = element4;
                                                                                str52 = str15;
                                                                                document11 = document12;
                                                                                str53 = str56;
                                                                                str54 = str58;
                                                                                function125 = function128;
                                                                                function126 = function127;
                                                                                element13 = selectFirst4;
                                                                                i12 = i13;
                                                                                try {
                                                                                    document13 = ((NiceResponse) obj8).getDocument();
                                                                                    selectFirst5 = document13.selectFirst("a[href*='gofile.io']");
                                                                                    if (selectFirst5 == null) {
                                                                                        str61 = str57;
                                                                                        try {
                                                                                            str62 = selectFirst5.attr(str61);
                                                                                        } catch (Exception e37) {
                                                                                            e = e37;
                                                                                            str51 = str59;
                                                                                            str74 = str61;
                                                                                            str3 = str50;
                                                                                            Log.INSTANCE.e(str3, "Gofile error: " + e.getMessage());
                                                                                            str60 = str54;
                                                                                            function129 = function125;
                                                                                            function130 = function126;
                                                                                            str35 = str53;
                                                                                            document6 = document11;
                                                                                            str15 = str52;
                                                                                            i14 = i12;
                                                                                            element14 = element13;
                                                                                            element4 = element12;
                                                                                            str7 = str51;
                                                                                            selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                                                                                            if (selectFirst6 != null) {
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        str61 = str57;
                                                                                        str62 = null;
                                                                                    }
                                                                                    try {
                                                                                        str63 = str62;
                                                                                    } catch (Exception e38) {
                                                                                        e = e38;
                                                                                        str51 = str59;
                                                                                        str74 = str61;
                                                                                    }
                                                                                } catch (Exception e39) {
                                                                                    e = e39;
                                                                                    str51 = str59;
                                                                                    str74 = str57;
                                                                                }
                                                                                if (str63 != null) {
                                                                                    if (str63.length() != 0) {
                                                                                        z5 = false;
                                                                                        if (!z5) {
                                                                                            String str86 = str61;
                                                                                            try {
                                                                                                gofile = new Gofile();
                                                                                                gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                                                                                                gDxFiles$getUrl$12.L$1 = str59;
                                                                                                gDxFiles$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str54);
                                                                                                gDxFiles$getUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(function125);
                                                                                                gDxFiles$getUrl$12.L$4 = function126;
                                                                                                gDxFiles$getUrl$12.L$5 = document11;
                                                                                                gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str52);
                                                                                                gDxFiles$getUrl$12.L$7 = str53;
                                                                                                gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(element);
                                                                                                gDxFiles$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(element12);
                                                                                                gDxFiles$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(element13);
                                                                                                gDxFiles$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document13);
                                                                                                gDxFiles$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str62);
                                                                                                gDxFiles$getUrl$12.I$0 = i12;
                                                                                                gDxFiles$getUrl$12.label = 8;
                                                                                                str51 = str59;
                                                                                                str74 = str86;
                                                                                            } catch (Exception e40) {
                                                                                                e = e40;
                                                                                                str51 = str59;
                                                                                                str74 = str86;
                                                                                            }
                                                                                            try {
                                                                                            } catch (Exception e41) {
                                                                                                e = e41;
                                                                                                str3 = str50;
                                                                                                Log.INSTANCE.e(str3, "Gofile error: " + e.getMessage());
                                                                                                str60 = str54;
                                                                                                function129 = function125;
                                                                                                function130 = function126;
                                                                                                str35 = str53;
                                                                                                document6 = document11;
                                                                                                str15 = str52;
                                                                                                i14 = i12;
                                                                                                element14 = element13;
                                                                                                element4 = element12;
                                                                                                str7 = str51;
                                                                                                selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                                                                                                if (selectFirst6 != null) {
                                                                                                }
                                                                                            }
                                                                                            if (gofile.getUrl(str62, str54, function125, function126, gDxFiles$getUrl$12) == obj) {
                                                                                                return obj;
                                                                                            }
                                                                                            function131 = function125;
                                                                                            function132 = function126;
                                                                                            gDxFiles6 = gDxFiles3;
                                                                                            element15 = element;
                                                                                            str64 = str51;
                                                                                            gDxFiles3 = gDxFiles6;
                                                                                            str60 = str54;
                                                                                            i14 = i12;
                                                                                            element14 = element13;
                                                                                            element4 = element12;
                                                                                            function130 = function132;
                                                                                            function129 = function131;
                                                                                            str7 = str64;
                                                                                            str3 = str50;
                                                                                            element = element15;
                                                                                            str35 = str53;
                                                                                            document6 = document11;
                                                                                            str15 = str52;
                                                                                            selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                                                                                            if (selectFirst6 != null) {
                                                                                                return Unit.INSTANCE;
                                                                                            }
                                                                                            try {
                                                                                                Requests app6 = MainActivityKt.getApp();
                                                                                                String str87 = str3;
                                                                                                try {
                                                                                                    String attr4 = selectFirst6.attr(str74);
                                                                                                    gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                                                                                                    gDxFiles$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str7);
                                                                                                    gDxFiles$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str60);
                                                                                                    gDxFiles$getUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(function129);
                                                                                                    gDxFiles$getUrl$12.L$4 = function130;
                                                                                                    gDxFiles$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(document6);
                                                                                                    gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str15);
                                                                                                    gDxFiles$getUrl$12.L$7 = str35;
                                                                                                    gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(element);
                                                                                                    gDxFiles$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(element4);
                                                                                                    gDxFiles$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(element14);
                                                                                                    gDxFiles$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(selectFirst6);
                                                                                                    gDxFiles$getUrl$12.L$12 = null;
                                                                                                    gDxFiles$getUrl$12.L$13 = null;
                                                                                                    gDxFiles$getUrl$12.L$14 = null;
                                                                                                    gDxFiles$getUrl$12.L$15 = null;
                                                                                                    gDxFiles$getUrl$12.I$0 = i14;
                                                                                                    gDxFiles$getUrl$12.label = 9;
                                                                                                    i15 = i14;
                                                                                                    str69 = str35;
                                                                                                    GDxFiles$getUrl$1 gDxFiles$getUrl$17 = gDxFiles$getUrl$12;
                                                                                                    document15 = document6;
                                                                                                    str70 = str74;
                                                                                                    str65 = str87;
                                                                                                    function136 = function130;
                                                                                                    try {
                                                                                                        obj8 = Requests.get$default(app6, attr4, (Map) null, str7, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, gDxFiles$getUrl$17, 4090, (Object) null);
                                                                                                        gDxFiles$getUrl$12 = gDxFiles$getUrl$17;
                                                                                                    } catch (Exception e42) {
                                                                                                        e = e42;
                                                                                                        str67 = str60;
                                                                                                        function134 = function129;
                                                                                                        function135 = function136;
                                                                                                        str68 = str7;
                                                                                                        try {
                                                                                                            str3 = str65;
                                                                                                        } catch (Exception e43) {
                                                                                                            e = e43;
                                                                                                            str3 = str65;
                                                                                                        }
                                                                                                        try {
                                                                                                            Log.INSTANCE.e(str3, "Pixeldrain error: " + e.getMessage());
                                                                                                        } catch (Exception e44) {
                                                                                                            e = e44;
                                                                                                            function13 = function135;
                                                                                                            str4 = str67;
                                                                                                            Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                                                                                                            return Unit.INSTANCE;
                                                                                                        }
                                                                                                        return Unit.INSTANCE;
                                                                                                    }
                                                                                                } catch (Exception e45) {
                                                                                                    e = e45;
                                                                                                    function133 = function130;
                                                                                                    str66 = str35;
                                                                                                    document14 = document6;
                                                                                                    element16 = selectFirst6;
                                                                                                    str65 = str87;
                                                                                                    str67 = str60;
                                                                                                    function134 = function129;
                                                                                                    function135 = function133;
                                                                                                    str68 = str7;
                                                                                                    str3 = str65;
                                                                                                    Log.INSTANCE.e(str3, "Pixeldrain error: " + e.getMessage());
                                                                                                    return Unit.INSTANCE;
                                                                                                }
                                                                                            } catch (Exception e46) {
                                                                                                e = e46;
                                                                                                function133 = function130;
                                                                                                str65 = str3;
                                                                                                str66 = str35;
                                                                                                document14 = document6;
                                                                                                element16 = selectFirst6;
                                                                                            }
                                                                                            if (obj8 == obj) {
                                                                                                return obj;
                                                                                            }
                                                                                            element17 = element4;
                                                                                            str71 = str15;
                                                                                            document16 = document15;
                                                                                            element18 = element;
                                                                                            element19 = selectFirst6;
                                                                                            str67 = str60;
                                                                                            str72 = str69;
                                                                                            function134 = function129;
                                                                                            function135 = function136;
                                                                                            element20 = element14;
                                                                                            i16 = i15;
                                                                                            str68 = str7;
                                                                                            try {
                                                                                                Document document18 = ((NiceResponse) obj8).getDocument();
                                                                                                Element selectFirst7 = document18.selectFirst("a[href*='pixeldrain.com']");
                                                                                                String attr5 = selectFirst7 == null ? selectFirst7.attr(str70) : null;
                                                                                                str73 = attr5;
                                                                                                if (str73 != null && str73.length() != 0) {
                                                                                                    z4 = false;
                                                                                                    break;
                                                                                                }
                                                                                            } catch (Exception e47) {
                                                                                                e = e47;
                                                                                                str3 = str65;
                                                                                                Log.INSTANCE.e(str3, "Pixeldrain error: " + e.getMessage());
                                                                                                return Unit.INSTANCE;
                                                                                            }
                                                                                            return Unit.INSTANCE;
                                                                                        }
                                                                                        String str88 = str59;
                                                                                        str74 = str61;
                                                                                        str60 = str54;
                                                                                        function129 = function125;
                                                                                        function130 = function126;
                                                                                        str35 = str53;
                                                                                        document6 = document11;
                                                                                        str15 = str52;
                                                                                        i14 = i12;
                                                                                        element14 = element13;
                                                                                        element4 = element12;
                                                                                        str7 = str88;
                                                                                        str3 = str50;
                                                                                        selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                                                                                        if (selectFirst6 != null) {
                                                                                        }
                                                                                    }
                                                                                }
                                                                                z5 = true;
                                                                                if (!z5) {
                                                                                }
                                                                            } else {
                                                                                str60 = str14;
                                                                                function129 = function16;
                                                                                str3 = str34;
                                                                                element14 = selectFirst4;
                                                                                str74 = str33;
                                                                                z4 = true;
                                                                                function130 = function116;
                                                                                i14 = i7;
                                                                                selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                                                                                if (selectFirst6 != null) {
                                                                                }
                                                                            }
                                                                        } else {
                                                                            String str89 = str43;
                                                                            element = element9;
                                                                            function116 = function118;
                                                                            document6 = document9;
                                                                            function16 = function117;
                                                                            str35 = str37;
                                                                            str7 = str89;
                                                                            element4 = element8;
                                                                            i7 = i10;
                                                                            str34 = str36;
                                                                            selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                                                                            if (selectFirst4 != null) {
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                z3 = true;
                                                                if (z3) {
                                                                }
                                                            }
                                                        }
                                                        str44 = null;
                                                        str45 = str44;
                                                        str46 = str45;
                                                        if (str46 != null) {
                                                        }
                                                        z3 = true;
                                                        if (z3) {
                                                        }
                                                    } else {
                                                        Function1<? super ExtractorLink, Unit> function142 = function15;
                                                        int i19 = i2;
                                                        element4 = selectFirst3;
                                                        str33 = str12;
                                                        str34 = str11;
                                                        str35 = str13;
                                                        function116 = function142;
                                                        document6 = document2;
                                                        i7 = i19;
                                                        selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                                                        if (selectFirst4 != null) {
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        z2 = true;
                                        if (z2) {
                                        }
                                    } else {
                                        Function1<? super SubtitleFile, Unit> function143 = function14;
                                        element = selectFirst;
                                        str11 = str6;
                                        str12 = str5;
                                        i2 = indexQuality;
                                        str13 = str76;
                                        str14 = str8;
                                        function16 = function143;
                                        document2 = document;
                                        str15 = obj4;
                                        selectFirst3 = document2.selectFirst("a[href*='/watch']");
                                        if (selectFirst3 == null) {
                                        }
                                    }
                                }
                            }
                            z = true;
                            if (z) {
                            }
                        } else {
                            append = new StringBuilder().append(" [").append(obj4).append("]");
                        }
                        str10 = append.toString();
                        String str762 = str10;
                        selectFirst = document.selectFirst("a[href*='/file/r2/']");
                        if (selectFirst != null) {
                        }
                        break;
                    case 1:
                        function15 = (Function1) gDxFiles$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function144 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str90 = (String) gDxFiles$getUrl$12.L$2;
                        String str91 = (String) gDxFiles$getUrl$12.L$1;
                        GDxFiles gDxFiles7 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            gDxFiles3 = gDxFiles7;
                            obj3 = obj8;
                            obj = coroutine_suspended;
                            str6 = "GDxFiles";
                            str5 = "href";
                            function14 = function144;
                            str8 = str90;
                            str7 = str91;
                            i = 2;
                            document = ((NiceResponse) obj3).getDocument();
                            obj4 = StringsKt.trim(StringsKt.substringBefore$default(document.title(), "|", (String) null, i, (Object) null)).toString();
                            str9 = obj4;
                            if (str9.length() != 0) {
                            }
                            indexQuality = ExtractorsKt.getIndexQuality(str9);
                            if (obj4.length() <= 0) {
                            }
                            str10 = append.toString();
                            String str7622 = str10;
                            selectFirst = document.selectFirst("a[href*='/file/r2/']");
                            if (selectFirst != null) {
                            }
                        } catch (Exception e48) {
                            e = e48;
                            str3 = "GDxFiles";
                            function13 = function15;
                            str4 = str90;
                            Log.INSTANCE.e(str3, "Failed to extract GDxFiles: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        break;
                    case 2:
                        i3 = gDxFiles$getUrl$12.I$0;
                        str20 = (String) gDxFiles$getUrl$12.L$9;
                        element2 = (Element) gDxFiles$getUrl$12.L$8;
                        str16 = (String) gDxFiles$getUrl$12.L$7;
                        str18 = (String) gDxFiles$getUrl$12.L$6;
                        Document document19 = (Document) gDxFiles$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function145 = (Function1) gDxFiles$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function146 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str92 = (String) gDxFiles$getUrl$12.L$2;
                        String str93 = (String) gDxFiles$getUrl$12.L$1;
                        GDxFiles gDxFiles8 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            gDxFiles3 = gDxFiles8;
                            obj = coroutine_suspended;
                            str6 = "GDxFiles";
                            i4 = i3;
                            str19 = "href";
                            function111 = function145;
                            function110 = function146;
                            str22 = str92;
                            str21 = str93;
                            element = element2;
                            str13 = str16;
                            document2 = document19;
                            document3 = ((NiceResponse) obj8).getDocument();
                            selectFirst2 = document3.selectFirst("a[href*='/download']");
                            if (selectFirst2 == null) {
                            }
                            str26 = str25;
                            if (str26 != null) {
                            }
                            z2 = true;
                            if (z2) {
                            }
                        } catch (Exception e49) {
                            e = e49;
                            gDxFiles3 = gDxFiles8;
                            obj = coroutine_suspended;
                            str6 = "GDxFiles";
                            str12 = "href";
                            function17 = function146;
                            str4 = str92;
                            str17 = str93;
                            document2 = document19;
                            function18 = function145;
                            str11 = str6;
                            Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                            i2 = i3;
                            function15 = function18;
                            str13 = str16;
                            str15 = str18;
                            str7 = str17;
                            str14 = str4;
                            function16 = function17;
                            element = element2;
                            selectFirst3 = document2.selectFirst("a[href*='/watch']");
                            if (selectFirst3 == null) {
                            }
                        }
                        break;
                    case 3:
                        i3 = gDxFiles$getUrl$12.I$0;
                        String str94 = (String) gDxFiles$getUrl$12.L$12;
                        Element element22 = (Element) gDxFiles$getUrl$12.L$11;
                        Document document20 = (Document) gDxFiles$getUrl$12.L$10;
                        str27 = (String) gDxFiles$getUrl$12.L$9;
                        element2 = (Element) gDxFiles$getUrl$12.L$8;
                        str16 = (String) gDxFiles$getUrl$12.L$7;
                        str18 = (String) gDxFiles$getUrl$12.L$6;
                        Document document21 = (Document) gDxFiles$getUrl$12.L$5;
                        function18 = (Function1) gDxFiles$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function147 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str95 = (String) gDxFiles$getUrl$12.L$2;
                        str30 = str94;
                        str17 = (String) gDxFiles$getUrl$12.L$1;
                        GDxFiles gDxFiles9 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            gDxFiles3 = gDxFiles9;
                            obj = coroutine_suspended;
                            str6 = "GDxFiles";
                            str12 = "href";
                            function113 = function147;
                            str29 = str95;
                            str28 = str17;
                            i6 = i3;
                            element3 = element22;
                            document4 = document20;
                            document2 = document21;
                            str31 = str16;
                            NiceResponse niceResponse2 = (NiceResponse) obj8;
                            str32 = niceResponse2.getHeaders().get("location");
                            if (str32 == null) {
                            }
                            String str812 = str32;
                            String str822 = "GDxFiles [Direct R2]" + str31;
                            ExtractorLinkType infer_type3 = ExtractorApiKt.getINFER_TYPE();
                            GDxFiles$getUrl$2 gDxFiles$getUrl$22 = new GDxFiles$getUrl$2(i6, str27, null);
                            gDxFiles$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(gDxFiles3);
                            gDxFiles$getUrl$12.L$1 = str28;
                            gDxFiles$getUrl$12.L$2 = str29;
                            gDxFiles$getUrl$12.L$3 = function113;
                            gDxFiles$getUrl$12.L$4 = function18;
                            gDxFiles$getUrl$12.L$5 = document2;
                            gDxFiles$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str18);
                            gDxFiles$getUrl$12.L$7 = str31;
                            gDxFiles$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(element2);
                            gDxFiles$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str27);
                            gDxFiles$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(document4);
                            gDxFiles$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(element3);
                            gDxFiles$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str30);
                            gDxFiles$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                            gDxFiles$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str812);
                            gDxFiles$getUrl$12.L$15 = function18;
                            gDxFiles$getUrl$12.I$0 = i6;
                            gDxFiles$getUrl$12.label = 4;
                            function17 = function113;
                            str4 = str29;
                            str17 = str28;
                            obj8 = ExtractorApiKt.newExtractorLink("GDxFiles [Direct R2]", str822, str812, infer_type3, gDxFiles$getUrl$22, gDxFiles$getUrl$12);
                            if (obj8 != obj) {
                            }
                        } catch (Exception e50) {
                            e = e50;
                            gDxFiles3 = gDxFiles9;
                            obj = coroutine_suspended;
                            str6 = "GDxFiles";
                            str12 = "href";
                            function17 = function147;
                            str4 = str95;
                            document2 = document21;
                            str11 = str6;
                            Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                            i2 = i3;
                            function15 = function18;
                            str13 = str16;
                            str15 = str18;
                            str7 = str17;
                            str14 = str4;
                            function16 = function17;
                            element = element2;
                            selectFirst3 = document2.selectFirst("a[href*='/watch']");
                            if (selectFirst3 == null) {
                            }
                        }
                        break;
                    case 4:
                        i3 = gDxFiles$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function148 = (Function1) gDxFiles$getUrl$12.L$15;
                        element2 = (Element) gDxFiles$getUrl$12.L$8;
                        str16 = (String) gDxFiles$getUrl$12.L$7;
                        str18 = (String) gDxFiles$getUrl$12.L$6;
                        Document document22 = (Document) gDxFiles$getUrl$12.L$5;
                        function115 = (Function1) gDxFiles$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function149 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str96 = (String) gDxFiles$getUrl$12.L$2;
                        String str97 = (String) gDxFiles$getUrl$12.L$1;
                        gDxFiles4 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            obj = coroutine_suspended;
                            str6 = "GDxFiles";
                            str12 = "href";
                            function114 = function148;
                            document5 = document22;
                            function17 = function149;
                            str7 = str97;
                            str4 = str96;
                            function114.invoke(obj8);
                            gDxFiles3 = gDxFiles4;
                            document2 = document5;
                            i2 = i3;
                            str13 = str16;
                            str15 = str18;
                            function15 = function115;
                            str14 = str4;
                            str11 = str6;
                            function16 = function17;
                            element = element2;
                        } catch (Exception e51) {
                            e = e51;
                            gDxFiles3 = gDxFiles4;
                            obj = coroutine_suspended;
                            str6 = "GDxFiles";
                            str12 = "href";
                            document2 = document22;
                            function18 = function115;
                            function17 = function149;
                            str17 = str97;
                            str4 = str96;
                            str11 = str6;
                            Log.INSTANCE.e(str11, "R2 error: " + e.getMessage());
                            i2 = i3;
                            function15 = function18;
                            str13 = str16;
                            str15 = str18;
                            str7 = str17;
                            str14 = str4;
                            function16 = function17;
                            element = element2;
                            selectFirst3 = document2.selectFirst("a[href*='/watch']");
                            if (selectFirst3 == null) {
                            }
                        }
                        selectFirst3 = document2.selectFirst("a[href*='/watch']");
                        if (selectFirst3 == null) {
                        }
                        break;
                    case 5:
                        i8 = gDxFiles$getUrl$12.I$0;
                        str42 = (String) gDxFiles$getUrl$12.L$10;
                        element7 = (Element) gDxFiles$getUrl$12.L$9;
                        element6 = (Element) gDxFiles$getUrl$12.L$8;
                        str37 = (String) gDxFiles$getUrl$12.L$7;
                        str15 = (String) gDxFiles$getUrl$12.L$6;
                        document7 = (Document) gDxFiles$getUrl$12.L$5;
                        function118 = (Function1) gDxFiles$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function150 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str98 = (String) gDxFiles$getUrl$12.L$2;
                        str39 = (String) gDxFiles$getUrl$12.L$1;
                        GDxFiles gDxFiles10 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            gDxFiles3 = gDxFiles10;
                            obj = coroutine_suspended;
                            str36 = "GDxFiles";
                            str33 = "href";
                            str14 = str98;
                            str43 = str39;
                            i10 = i8;
                            function117 = function150;
                            document9 = document7;
                            text = ((NiceResponse) obj8).getText();
                            element8 = element7;
                            element9 = element6;
                            find$default = Regex.find$default(new Regex("const\\s+SRC\\s*=\\s*[\"']([^\"']+)[\"']"), text, 0, 2, (Object) null);
                            if (find$default != null) {
                            }
                            str44 = null;
                            str45 = str44;
                            str46 = str45;
                            if (str46 != null) {
                            }
                            z3 = true;
                            if (z3) {
                            }
                        } catch (Exception e52) {
                            e = e52;
                            gDxFiles3 = gDxFiles10;
                            obj = coroutine_suspended;
                            str36 = "GDxFiles";
                            str33 = "href";
                            element5 = element7;
                            str38 = str98;
                            function117 = function150;
                            str34 = str36;
                            Log.INSTANCE.e(str34, "Watch error: " + e.getMessage());
                            i7 = i8;
                            function116 = function118;
                            function16 = function117;
                            element = element6;
                            str35 = str37;
                            str7 = str39;
                            document6 = document7;
                            element4 = element5;
                            str14 = str38;
                            selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                            if (selectFirst4 != null) {
                            }
                        }
                        break;
                    case 6:
                        i8 = gDxFiles$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function151 = (Function1) gDxFiles$getUrl$12.L$14;
                        element10 = (Element) gDxFiles$getUrl$12.L$9;
                        element6 = (Element) gDxFiles$getUrl$12.L$8;
                        str37 = (String) gDxFiles$getUrl$12.L$7;
                        str15 = (String) gDxFiles$getUrl$12.L$6;
                        document7 = (Document) gDxFiles$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function152 = (Function1) gDxFiles$getUrl$12.L$4;
                        function121 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str99 = (String) gDxFiles$getUrl$12.L$2;
                        String str100 = (String) gDxFiles$getUrl$12.L$1;
                        gDxFiles5 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            obj = coroutine_suspended;
                            str36 = "GDxFiles";
                            str33 = "href";
                            function122 = function151;
                            function116 = function152;
                            str47 = str99;
                            str39 = str100;
                            function122.invoke(obj8);
                            gDxFiles3 = gDxFiles5;
                            str14 = str47;
                            i7 = i8;
                            str35 = str37;
                            str7 = str39;
                            document6 = document7;
                            function16 = function121;
                            str34 = str36;
                            element4 = element10;
                            element = element6;
                        } catch (Exception e53) {
                            e = e53;
                            gDxFiles3 = gDxFiles5;
                            obj = coroutine_suspended;
                            str36 = "GDxFiles";
                            str33 = "href";
                            element5 = element10;
                            function118 = function152;
                            function117 = function121;
                            str38 = str99;
                            str39 = str100;
                            str34 = str36;
                            Log.INSTANCE.e(str34, "Watch error: " + e.getMessage());
                            i7 = i8;
                            function116 = function118;
                            function16 = function117;
                            element = element6;
                            str35 = str37;
                            str7 = str39;
                            document6 = document7;
                            element4 = element5;
                            str14 = str38;
                            selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                            if (selectFirst4 != null) {
                            }
                        }
                        selectFirst4 = document6.selectFirst("a[href*='/file/gofile/']");
                        if (selectFirst4 != null) {
                        }
                        break;
                    case 7:
                        i12 = gDxFiles$getUrl$12.I$0;
                        element13 = (Element) gDxFiles$getUrl$12.L$10;
                        element12 = (Element) gDxFiles$getUrl$12.L$9;
                        Element element23 = (Element) gDxFiles$getUrl$12.L$8;
                        str53 = (String) gDxFiles$getUrl$12.L$7;
                        str52 = (String) gDxFiles$getUrl$12.L$6;
                        document11 = (Document) gDxFiles$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function153 = (Function1) gDxFiles$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function154 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str101 = (String) gDxFiles$getUrl$12.L$2;
                        String str102 = (String) gDxFiles$getUrl$12.L$1;
                        GDxFiles gDxFiles11 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            gDxFiles3 = gDxFiles11;
                            obj = coroutine_suspended;
                            str50 = "GDxFiles";
                            element = element23;
                            str57 = "href";
                            function126 = function153;
                            function125 = function154;
                            str54 = str101;
                            str59 = str102;
                            z4 = true;
                            document13 = ((NiceResponse) obj8).getDocument();
                            selectFirst5 = document13.selectFirst("a[href*='gofile.io']");
                            if (selectFirst5 == null) {
                            }
                            str63 = str62;
                            if (str63 != null) {
                            }
                            z5 = true;
                            if (!z5) {
                            }
                        } catch (Exception e54) {
                            e = e54;
                            gDxFiles3 = gDxFiles11;
                            obj = coroutine_suspended;
                            str50 = "GDxFiles";
                            element = element23;
                            function126 = function153;
                            function125 = function154;
                            str54 = str101;
                            str51 = str102;
                            z4 = true;
                            str3 = str50;
                            Log.INSTANCE.e(str3, "Gofile error: " + e.getMessage());
                            str60 = str54;
                            function129 = function125;
                            function130 = function126;
                            str35 = str53;
                            document6 = document11;
                            str15 = str52;
                            i14 = i12;
                            element14 = element13;
                            element4 = element12;
                            str7 = str51;
                            selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                            if (selectFirst6 != null) {
                            }
                        }
                        break;
                    case 8:
                        i12 = gDxFiles$getUrl$12.I$0;
                        element13 = (Element) gDxFiles$getUrl$12.L$10;
                        element12 = (Element) gDxFiles$getUrl$12.L$9;
                        element15 = (Element) gDxFiles$getUrl$12.L$8;
                        str53 = (String) gDxFiles$getUrl$12.L$7;
                        str52 = (String) gDxFiles$getUrl$12.L$6;
                        document11 = (Document) gDxFiles$getUrl$12.L$5;
                        function132 = (Function1) gDxFiles$getUrl$12.L$4;
                        function131 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str103 = (String) gDxFiles$getUrl$12.L$2;
                        str64 = (String) gDxFiles$getUrl$12.L$1;
                        gDxFiles6 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            obj = coroutine_suspended;
                            str50 = "GDxFiles";
                            str54 = str103;
                            z4 = true;
                            gDxFiles3 = gDxFiles6;
                            str60 = str54;
                            i14 = i12;
                            element14 = element13;
                            element4 = element12;
                            function130 = function132;
                            function129 = function131;
                            str7 = str64;
                            str3 = str50;
                            element = element15;
                            str35 = str53;
                            document6 = document11;
                            str15 = str52;
                        } catch (Exception e55) {
                            e = e55;
                            gDxFiles3 = gDxFiles6;
                            obj = coroutine_suspended;
                            str50 = "GDxFiles";
                            function125 = function131;
                            str54 = str103;
                            str51 = str64;
                            z4 = true;
                            element = element15;
                            function126 = function132;
                            str3 = str50;
                            Log.INSTANCE.e(str3, "Gofile error: " + e.getMessage());
                            str60 = str54;
                            function129 = function125;
                            function130 = function126;
                            str35 = str53;
                            document6 = document11;
                            str15 = str52;
                            i14 = i12;
                            element14 = element13;
                            element4 = element12;
                            str7 = str51;
                            selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                            if (selectFirst6 != null) {
                            }
                        }
                        selectFirst6 = document6.selectFirst("a[href*='/file/pixeldrain/']");
                        if (selectFirst6 != null) {
                        }
                        break;
                    case 9:
                        i16 = gDxFiles$getUrl$12.I$0;
                        Element element24 = (Element) gDxFiles$getUrl$12.L$11;
                        element20 = (Element) gDxFiles$getUrl$12.L$10;
                        Element element25 = (Element) gDxFiles$getUrl$12.L$9;
                        element18 = (Element) gDxFiles$getUrl$12.L$8;
                        String str104 = (String) gDxFiles$getUrl$12.L$7;
                        String str105 = (String) gDxFiles$getUrl$12.L$6;
                        Document document23 = (Document) gDxFiles$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function155 = (Function1) gDxFiles$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function156 = (Function1) gDxFiles$getUrl$12.L$3;
                        String str106 = (String) gDxFiles$getUrl$12.L$2;
                        String str107 = (String) gDxFiles$getUrl$12.L$1;
                        GDxFiles gDxFiles12 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            obj = coroutine_suspended;
                            str65 = "GDxFiles";
                            str70 = "href";
                            element19 = element24;
                            element17 = element25;
                            str72 = str104;
                            str71 = str105;
                            document16 = document23;
                            function135 = function155;
                            function134 = function156;
                            str67 = str106;
                            str68 = str107;
                            z4 = true;
                            gDxFiles3 = gDxFiles12;
                            Document document182 = ((NiceResponse) obj8).getDocument();
                            Element selectFirst72 = document182.selectFirst("a[href*='pixeldrain.com']");
                            String attr52 = selectFirst72 == null ? selectFirst72.attr(str70) : null;
                            str73 = attr52;
                            if (str73 != null) {
                                z4 = false;
                                break;
                            }
                        } catch (Exception e56) {
                            e = e56;
                            str65 = "GDxFiles";
                            function135 = function155;
                            function134 = function156;
                            str67 = str106;
                            str68 = str107;
                            str3 = str65;
                            Log.INSTANCE.e(str3, "Pixeldrain error: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        return Unit.INSTANCE;
                    case 10:
                        int i20 = gDxFiles$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function157 = (Function1) gDxFiles$getUrl$12.L$16;
                        Function1<? super ExtractorLink, Unit> function158 = (Function1) gDxFiles$getUrl$12.L$4;
                        function134 = (Function1) gDxFiles$getUrl$12.L$3;
                        str67 = (String) gDxFiles$getUrl$12.L$2;
                        str68 = (String) gDxFiles$getUrl$12.L$1;
                        GDxFiles gDxFiles13 = (GDxFiles) gDxFiles$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj8);
                            str65 = "GDxFiles";
                            function137 = function157;
                        } catch (Exception e57) {
                            e = e57;
                            str65 = "GDxFiles";
                            function135 = function158;
                            str3 = str65;
                            Log.INSTANCE.e(str3, "Pixeldrain error: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        try {
                            function137.invoke(obj8);
                        } catch (Exception e58) {
                            e = e58;
                            function135 = function158;
                            str3 = str65;
                            Log.INSTANCE.e(str3, "Pixeldrain error: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        return Unit.INSTANCE;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        gDxFiles2 = gDxFiles;
        gDxFiles$getUrl$1 = new GDxFiles$getUrl$1(gDxFiles2, continuation);
        gDxFiles$getUrl$12 = gDxFiles$getUrl$1;
        Object obj82 = gDxFiles$getUrl$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        String str742 = "href";
        switch (gDxFiles$getUrl$12.label) {
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

    @Nullable
    public Object getUrl(@NotNull String str, @Nullable String str2, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Unit> continuation) {
        return getUrl$suspendImpl(this, str, str2, function1, function12, continuation);
    }
}
