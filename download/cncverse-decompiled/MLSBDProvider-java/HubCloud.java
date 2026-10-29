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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.RegexOption;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\b\u0016\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JH\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00052\b\u0010\u0011\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u000f0\u00132\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@¢\u0006\u0002\u0010\u0017J\u0012\u0010\u0018\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u0005H\u0002J\u0010\u0010\u001b\u001a\u00020\u00052\u0006\u0010\u0010\u001a\u00020\u0005H\u0002J\u0010\u0010\u001c\u001a\u00020\u00052\u0006\u0010\u001d\u001a\u00020\u0005H\u0002R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u001e"}, d2 = {"Lcom/cncverse/HubCloud;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getIndexQuality", "", "str", "getBaseUrl", "cleanTitle", "title", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nExtractors.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Extractors.kt\ncom/cncverse/HubCloud\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,995:1\n1#2:996\n1#2:1015\n296#3,2:997\n1915#3,2:999\n1642#3,10:1001\n1915#3:1011\n1807#3,3:1012\n1916#3:1016\n1652#3:1017\n*S KotlinDebug\n*F\n+ 1 Extractors.kt\ncom/cncverse/HubCloud\n*L\n350#1:1015\n134#1:997,2\n165#1:999,2\n350#1:1001,10\n350#1:1011\n356#1:1012,3\n350#1:1016\n350#1:1017\n*E\n"})
/* loaded from: classes.dex */
public class HubCloud extends ExtractorApi {
    private final boolean requiresReferer;

    @NotNull
    private final String name = "Hub-Cloud";

    @NotNull
    private final String mainUrl = "https://hubcloud.foo";

    /* JADX WARN: Removed duplicated region for block: B:12:0x01d1 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:9:0x01cd  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private final String cleanTitle(String title) {
        List list;
        Set set;
        Set set2;
        String str;
        boolean z;
        String str2;
        String replace = new Regex("\\.[a-zA-Z0-9]{2,4}$").replace(title, "");
        String replace2 = new Regex("DDP[ .]?([0-9]\\.[0-9])", RegexOption.IGNORE_CASE).replace(new Regex("H[ .]?264", RegexOption.IGNORE_CASE).replace(new Regex("H[ .]?265", RegexOption.IGNORE_CASE).replace(new Regex("WEB[-_. ]?RIP", RegexOption.IGNORE_CASE).replace(new Regex("WEB[-_. ]?DL", RegexOption.IGNORE_CASE).replace(replace, "WEB-DL"), "WEBRIP"), "H265"), "H264"), "DDP$1");
        List split$default = StringsKt.split$default(replace2, new String[]{" ", "_", "."}, false, 0, 6, (Object) null);
        Set of = SetsKt.setOf(new String[]{"WEB-DL", "WEBRIP", "BLURAY", "HDRIP", "DVDRIP", "HDTV", "CAM", "TS", "BRRIP", "BDRIP"});
        Set of2 = SetsKt.setOf(new String[]{"H264", "H265", "X264", "X265", "HEVC", "AVC"});
        Set of3 = SetsKt.setOf(new String[]{"AAC", "AC3", "DTS", "MP3", "FLAC", "DD", "DDP", "EAC3"});
        Set of4 = SetsKt.setOf("ATMOS");
        Set of5 = SetsKt.setOf(new String[]{"SDR", "HDR", "HDR10", "HDR10+", "DV", "DOLBYVISION"});
        ArrayList arrayList = new ArrayList();
        Iterator it = split$default.iterator();
        while (it.hasNext()) {
            String str3 = replace;
            String str4 = replace2;
            String upperCase = ((String) it.next()).toUpperCase(Locale.ROOT);
            Intrinsics.checkNotNullExpressionValue(upperCase, "toUpperCase(...)");
            if (of.contains(upperCase)) {
                list = split$default;
                set = of;
                set2 = of2;
            } else if (of2.contains(upperCase)) {
                list = split$default;
                set = of;
                set2 = of2;
            } else {
                Set set3 = of3;
                list = split$default;
                if (!(set3 instanceof Collection) || !set3.isEmpty()) {
                    Iterator it2 = set3.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            set = of;
                            set2 = of2;
                            str = null;
                            z = false;
                            break;
                        }
                        Iterator it3 = it2;
                        set = of;
                        set2 = of2;
                        str = null;
                        if (StringsKt.startsWith$default(upperCase, (String) it2.next(), false, 2, (Object) null)) {
                            z = true;
                            break;
                        }
                        of2 = set2;
                        it2 = it3;
                        of = set;
                    }
                } else {
                    set = of;
                    set2 = of2;
                    z = false;
                    str = null;
                }
                if (!z && !of4.contains(upperCase)) {
                    if (of5.contains(upperCase)) {
                        if (Intrinsics.areEqual(upperCase, "DV") || Intrinsics.areEqual(upperCase, "DOLBYVISION")) {
                            str2 = "DOLBYVISION";
                            if (str2 != null) {
                                arrayList.add(str2);
                            }
                            replace = str3;
                            replace2 = str4;
                            split$default = list;
                            of2 = set2;
                            of = set;
                        }
                    } else if (!Intrinsics.areEqual(upperCase, "NF") && !Intrinsics.areEqual(upperCase, "CR")) {
                        str2 = str;
                        if (str2 != null) {
                        }
                        replace = str3;
                        replace2 = str4;
                        split$default = list;
                        of2 = set2;
                        of = set;
                    }
                }
            }
            str2 = upperCase;
            if (str2 != null) {
            }
            replace = str3;
            replace2 = str4;
            split$default = list;
            of2 = set2;
            of = set;
        }
        return CollectionsKt.joinToString$default(CollectionsKt.distinct(arrayList), " ", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null);
    }

    private final String getBaseUrl(String url) {
        Object obj;
        try {
            Result.Companion companion = Result.Companion;
            HubCloud hubCloud = this;
            URI uri = new URI(url);
            obj = Result.constructor-impl(uri.getScheme() + "://" + uri.getHost());
        } catch (Throwable th) {
            Result.Companion companion2 = Result.Companion;
            obj = Result.constructor-impl(ResultKt.createFailure(th));
        }
        if (Result.isFailure-impl(obj)) {
            obj = "";
        }
        return (String) obj;
    }

    private final int getIndexQuality(String str) {
        List groupValues;
        String str2;
        Integer intOrNull;
        MatchResult find$default = Regex.find$default(new Regex("(\\d{3,4})[pP]"), str == null ? "" : str, 0, 2, (Object) null);
        return (find$default == null || (groupValues = find$default.getGroupValues()) == null || (str2 = (String) CollectionsKt.getOrNull(groupValues, 1)) == null || (intOrNull = StringsKt.toIntOrNull(str2)) == null) ? Qualities.P2160.getValue() : intOrNull.intValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Can't wrap try/catch for region: R(20:310|(2:194|195)|(3:275|276|(10:278|221|(1:223)(3:242|(1:244)(1:257)|(5:246|247|248|249|250)(1:256))|224|225|226|227|(0)|230|(0)(0)))|197|(24:200|201|202|203|204|205|206|207|(1:262)(1:211)|(1:213)|214|215|(2:217|218)(1:259)|(1:220)|221|(0)(0)|224|225|226|227|(0)|230|(0)(0)|198)|272|273|214|215|(0)(0)|(0)|221|(0)(0)|224|225|226|227|(0)|230|(0)(0)) */
    /* JADX WARN: Can't wrap try/catch for region: R(7:(1:118)|119|120|121|122|123|(1:125)(3:126|127|128)) */
    /* JADX WARN: Code restructure failed: missing block: B:115:0x1b0a, code lost:
    
        r8 = r2;
        r2 = r3;
        r3 = r36;
        r39 = r7;
        r7 = r38;
        r26 = r22;
        r22 = r21;
        r21 = r15;
        r31 = r27;
        r27 = r35;
     */
    /* JADX WARN: Code restructure failed: missing block: B:130:0x16bf, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:131:0x16c0, code lost:
    
        r3 = r3;
        r11 = r9;
        r9 = 0;
        r29 = r28;
        r28 = r29;
        r10 = r6;
        r6 = r22;
        r12 = r34;
        r22 = r18;
        r34 = r23;
        r23 = r19;
        r19 = r5;
        r5 = r36;
        r7 = r2;
        r2 = r26;
        r26 = r35;
        r35 = r20;
        r20 = r17;
        r17 = r61;
        r1 = r4;
        r4 = r27;
        r27 = r21;
        r21 = r15;
        r15 = r11;
        r32 = r62;
        r14 = r43;
        r8 = r13;
        r13 = r31;
     */
    /* JADX WARN: Code restructure failed: missing block: B:260:0x0ba6, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:261:0x0ba7, code lost:
    
        r3 = r30;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x002e. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0039  */
    /* JADX WARN: Removed duplicated region for block: B:138:0x1b23  */
    /* JADX WARN: Removed duplicated region for block: B:140:0x00f0  */
    /* JADX WARN: Removed duplicated region for block: B:142:0x01b1  */
    /* JADX WARN: Removed duplicated region for block: B:149:0x0260  */
    /* JADX WARN: Removed duplicated region for block: B:151:0x031e  */
    /* JADX WARN: Removed duplicated region for block: B:153:0x03dc  */
    /* JADX WARN: Removed duplicated region for block: B:155:0x049a  */
    /* JADX WARN: Removed duplicated region for block: B:157:0x056a  */
    /* JADX WARN: Removed duplicated region for block: B:158:0x063d  */
    /* JADX WARN: Removed duplicated region for block: B:159:0x06fa  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0dc0  */
    /* JADX WARN: Removed duplicated region for block: B:161:0x07bd  */
    /* JADX WARN: Removed duplicated region for block: B:163:0x0880  */
    /* JADX WARN: Removed duplicated region for block: B:166:0x0cf7  */
    /* JADX WARN: Removed duplicated region for block: B:168:0x0d00  */
    /* JADX WARN: Removed duplicated region for block: B:171:0x0d0a  */
    /* JADX WARN: Removed duplicated region for block: B:173:0x0d12  */
    /* JADX WARN: Removed duplicated region for block: B:176:0x0d2f  */
    /* JADX WARN: Removed duplicated region for block: B:179:0x0d3e  */
    /* JADX WARN: Removed duplicated region for block: B:182:0x0d6d  */
    /* JADX WARN: Removed duplicated region for block: B:184:0x0d71  */
    /* JADX WARN: Removed duplicated region for block: B:186:0x0d5d  */
    /* JADX WARN: Removed duplicated region for block: B:187:0x0d32  */
    /* JADX WARN: Removed duplicated region for block: B:188:0x0d0f  */
    /* JADX WARN: Removed duplicated region for block: B:189:0x0cfd  */
    /* JADX WARN: Removed duplicated region for block: B:190:0x08c8  */
    /* JADX WARN: Removed duplicated region for block: B:200:0x0a81 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:217:0x0b04 A[Catch: all -> 0x0b0a, TRY_ENTER, TRY_LEAVE, TryCatch #6 {all -> 0x0b0a, blocks: (B:207:0x0aaa, B:209:0x0ab0, B:217:0x0b04), top: B:206:0x0aaa }] */
    /* JADX WARN: Removed duplicated region for block: B:220:0x0b16  */
    /* JADX WARN: Removed duplicated region for block: B:223:0x0b23  */
    /* JADX WARN: Removed duplicated region for block: B:229:0x0c44  */
    /* JADX WARN: Removed duplicated region for block: B:232:0x0c70  */
    /* JADX WARN: Removed duplicated region for block: B:234:0x0c73  */
    /* JADX WARN: Removed duplicated region for block: B:242:0x0b31 A[Catch: all -> 0x0ba6, TryCatch #5 {all -> 0x0ba6, blocks: (B:221:0x0b1a, B:242:0x0b31, B:246:0x0b3f, B:215:0x0afd), top: B:214:0x0afd }] */
    /* JADX WARN: Removed duplicated region for block: B:259:0x0b13  */
    /* JADX WARN: Removed duplicated region for block: B:275:0x0a4c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:287:0x0925  */
    /* JADX WARN: Removed duplicated region for block: B:295:0x0952  */
    /* JADX WARN: Removed duplicated region for block: B:320:0x1b27  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x110b  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x112b  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x120b  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x111d  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0031  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static /* synthetic */ Object getUrl$suspendImpl(HubCloud hubCloud, String str, String str2, Function1<? super SubtitleFile, Unit> function1, Function1<? super ExtractorLink, Unit> function12, Continuation<? super Unit> continuation) {
        HubCloud$getUrl$1 hubCloud$getUrl$1;
        HubCloud$getUrl$1 hubCloud$getUrl$12;
        String str3;
        Object obj;
        Throwable th;
        Object obj2;
        String str4;
        String str5;
        String str6;
        Object obj3;
        HubCloud$getUrl$1 hubCloud$getUrl$13;
        String str7;
        int i;
        Function1<? super SubtitleFile, Unit> function13;
        Function1<? super ExtractorLink, Unit> function14;
        String str8;
        URI uri;
        String str9;
        String str10;
        HubCloud hubCloud2;
        String str11;
        String str12;
        Object obj4;
        HubCloud hubCloud3;
        Object obj5;
        String str13;
        int i2;
        HubCloud hubCloud4;
        String str14;
        String str15;
        String str16;
        String str17;
        Object obj6;
        HubCloud hubCloud5;
        String str18;
        String str19;
        String str20;
        String str21;
        URI uri2;
        String str22;
        Function1<? super ExtractorLink, Unit> function15;
        Function1<? super SubtitleFile, Unit> function16;
        Throwable th2;
        String str23;
        HubCloud$getUrl$1 hubCloud$getUrl$14;
        String str24;
        boolean z;
        String str25;
        Object obj7;
        String str26;
        String str27;
        URI uri3;
        String str28;
        Function1<? super ExtractorLink, Unit> function17;
        Function1<? super SubtitleFile, Unit> function18;
        String str29;
        String str30;
        Document document;
        Element selectFirst;
        int i3;
        HubCloud hubCloud6;
        String str31;
        HubCloud hubCloud7;
        char c;
        Iterator it;
        Object obj8;
        String attr;
        String text;
        String text2;
        String cleanTitle;
        String str32;
        String str33;
        StringBuilder sb;
        Iterator it2;
        Iterable iterable;
        HubCloud$getUrl$1 hubCloud$getUrl$15;
        String str34;
        String str35;
        String str36;
        String str37;
        String str38;
        int i4;
        int i5;
        String str39;
        Function1<? super ExtractorLink, Unit> function19;
        Function1<? super SubtitleFile, Unit> function110;
        HubCloud hubCloud8;
        String str40;
        String str41;
        Document document2;
        Object obj9;
        Document document3;
        String str42;
        String str43;
        URI uri4;
        String str44;
        Function1<? super ExtractorLink, Unit> function111;
        Function1<? super SubtitleFile, Unit> function112;
        String str45;
        String str46;
        Object obj10;
        String str47;
        String str48;
        Element element;
        String str49;
        String str50;
        Object obj11;
        String str51;
        int i6;
        String str52;
        Object obj12;
        String str53;
        Object obj13;
        String str54;
        HubCloud$getUrl$1 hubCloud$getUrl$16;
        String str55;
        Object obj14;
        int i7;
        Object obj15;
        String str56;
        HubCloud hubCloud9;
        String str57;
        Function1<? super ExtractorLink, Unit> function113;
        String str58;
        String str59;
        int i8;
        int i9;
        int i10;
        String str60;
        String str61;
        String str62;
        Element element2;
        Object obj16;
        Iterator it3;
        Iterable iterable2;
        String str63;
        String str64;
        String str65;
        String str66;
        String str67;
        URI uri5;
        String str68;
        Function1<? super ExtractorLink, Unit> function114;
        String str69;
        String str70;
        HubCloud hubCloud10;
        String str71;
        Document document4;
        Function1<? super SubtitleFile, Unit> function115;
        String str72;
        Document document5;
        String str73;
        Function1<? super SubtitleFile, Unit> function116;
        String str74;
        String str75;
        String str76;
        Object obj17;
        String str77;
        String str78;
        String str79;
        Object obj18;
        String str80;
        String str81;
        Object obj19;
        String str82;
        if (continuation instanceof HubCloud$getUrl$1) {
            hubCloud$getUrl$1 = (HubCloud$getUrl$1) continuation;
            if ((hubCloud$getUrl$1.label & Integer.MIN_VALUE) != 0) {
                hubCloud$getUrl$1.label -= Integer.MIN_VALUE;
                hubCloud$getUrl$12 = hubCloud$getUrl$1;
                Object obj20 = hubCloud$getUrl$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (hubCloud$getUrl$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj20);
                        try {
                            Result.Companion companion = Result.Companion;
                            str3 = str;
                            try {
                                obj = Result.constructor-impl(new URI(str3));
                            } catch (Throwable th3) {
                                th = th3;
                                Result.Companion companion2 = Result.Companion;
                                obj = Result.constructor-impl(ResultKt.createFailure(th));
                                th = Result.exceptionOrNull-impl(obj);
                                if (th == null) {
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            str3 = str;
                        }
                        th = Result.exceptionOrNull-impl(obj);
                        if (th == null) {
                            Log.INSTANCE.e("HubCloud", "Invalid URL: " + th.getMessage());
                            return Unit.INSTANCE;
                        }
                        URI uri6 = (URI) obj;
                        String uri7 = uri6.toString();
                        String str83 = uri6.getScheme() + "://" + uri6.getHost();
                        try {
                            Result.Companion companion3 = Result.Companion;
                            try {
                            } catch (Throwable th5) {
                                th = th5;
                                hubCloud$getUrl$13 = hubCloud$getUrl$12;
                                str4 = "href";
                                str5 = "";
                                str6 = "HubCloud";
                                obj3 = obj20;
                                str7 = "/";
                                i = 2;
                                obj2 = coroutine_suspended;
                                str3 = str;
                                function13 = function1;
                                function14 = function12;
                                str8 = uri7;
                                uri = uri6;
                                str9 = "HubCloud";
                                str10 = str83;
                                hubCloud2 = hubCloud;
                                str11 = str2;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            obj2 = coroutine_suspended;
                            str4 = "href";
                            str5 = "";
                            str6 = "HubCloud";
                            obj3 = obj20;
                            hubCloud$getUrl$13 = hubCloud$getUrl$12;
                            str7 = "/";
                            i = 2;
                            str3 = str;
                            function13 = function1;
                            function14 = function12;
                            str8 = uri7;
                            uri = uri6;
                            str9 = "HubCloud";
                            str10 = str83;
                            hubCloud2 = hubCloud;
                            str11 = str2;
                        }
                        if (StringsKt.contains$default(uri7, "hubcloud.php", false, 2, (Object) null)) {
                            function13 = function1;
                            function14 = function12;
                            hubCloud2 = hubCloud;
                            hubCloud$getUrl$13 = hubCloud$getUrl$12;
                            str4 = "href";
                            str5 = "";
                            obj3 = obj20;
                            str7 = "/";
                            str15 = uri7;
                            str8 = str15;
                            i = 2;
                            str11 = str2;
                            str16 = "HubCloud";
                            obj2 = coroutine_suspended;
                            str17 = "HubCloud";
                            str10 = str83;
                            uri = uri6;
                            try {
                                obj6 = Result.constructor-impl(str15);
                                hubCloud5 = hubCloud2;
                                str18 = str10;
                                str20 = str17;
                                str19 = str16;
                                str21 = str8;
                                uri2 = uri;
                                str22 = str3;
                                function15 = function14;
                                function16 = function13;
                            } catch (Throwable th7) {
                                th = th7;
                                str6 = str17;
                                str9 = str16;
                                Result.Companion companion4 = Result.Companion;
                                obj6 = Result.constructor-impl(ResultKt.createFailure(th));
                                hubCloud5 = hubCloud2;
                                str18 = str10;
                                str19 = str9;
                                str20 = str6;
                                str21 = str8;
                                uri2 = uri;
                                str22 = str3;
                                function15 = function14;
                                function16 = function13;
                                th2 = Result.exceptionOrNull-impl(obj6);
                                if (th2 != null) {
                                }
                                str23 = (String) obj6;
                                if (!StringsKt.isBlank(str23)) {
                                }
                            }
                            th2 = Result.exceptionOrNull-impl(obj6);
                            if (th2 != null) {
                                Log.INSTANCE.e(str19, "Failed to extract href: " + th2.getMessage());
                                obj6 = str5;
                            }
                            str23 = (String) obj6;
                            if (!StringsKt.isBlank(str23)) {
                                return Unit.INSTANCE;
                            }
                            String str84 = str7;
                            Requests app = MainActivityKt.getApp();
                            hubCloud$getUrl$13.L$0 = hubCloud5;
                            hubCloud$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                            hubCloud$getUrl$13.L$2 = SpillingKt.nullOutSpilledVariable(str11);
                            hubCloud$getUrl$13.L$3 = function16;
                            hubCloud$getUrl$13.L$4 = function15;
                            hubCloud$getUrl$13.L$5 = str19;
                            hubCloud$getUrl$13.L$6 = str20;
                            hubCloud$getUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(uri2);
                            hubCloud$getUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(str21);
                            hubCloud$getUrl$13.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                            hubCloud$getUrl$13.L$10 = SpillingKt.nullOutSpilledVariable(str23);
                            hubCloud$getUrl$13.label = i;
                            hubCloud$getUrl$14 = hubCloud$getUrl$13;
                            String str85 = str20;
                            str24 = str23;
                            String str86 = str19;
                            Function1<? super ExtractorLink, Unit> function117 = function15;
                            Function1<? super SubtitleFile, Unit> function118 = function16;
                            z = true;
                            str25 = str84;
                            obj7 = Requests.get$default(app, str24, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, hubCloud$getUrl$14, 4094, (Object) null);
                            if (obj7 == obj2) {
                                return obj2;
                            }
                            str26 = str11;
                            str27 = str21;
                            uri3 = uri2;
                            str28 = str85;
                            function17 = function117;
                            function18 = function118;
                            str29 = str86;
                            Document document6 = ((NiceResponse) obj7).getDocument();
                            Element selectFirst2 = document6.selectFirst("i#size");
                            text = selectFirst2 == null ? selectFirst2.text() : null;
                            if (text == null) {
                                text = str5;
                            }
                            Element selectFirst3 = document6.selectFirst("div.card-header");
                            text2 = selectFirst3 == null ? selectFirst3.text() : null;
                            if (text2 == null) {
                                text2 = str5;
                            }
                            cleanTitle = hubCloud5.cleanTitle(text2);
                            int indexQuality = hubCloud5.getIndexQuality(text2);
                            StringBuilder sb2 = new StringBuilder();
                            HubCloud hubCloud11 = hubCloud5;
                            Object obj21 = obj2;
                            if (cleanTitle.length() <= 0) {
                                str32 = str24;
                                str33 = text2;
                                sb = sb2;
                            } else {
                                str32 = str24;
                                str33 = text2;
                                sb = sb2;
                                sb.append("[" + cleanTitle + "]");
                            }
                            if (text.length() <= 0) {
                                z = false;
                            }
                            if (z) {
                                sb.append("[" + text + "]");
                            }
                            String sb3 = sb2.toString();
                            Iterable select = document6.select("a.btn[href]");
                            it2 = select.iterator();
                            HubCloud$getUrl$1 hubCloud$getUrl$17 = hubCloud$getUrl$14;
                            iterable = select;
                            hubCloud$getUrl$15 = hubCloud$getUrl$17;
                            str34 = str33;
                            str35 = cleanTitle;
                            str36 = text;
                            str37 = str26;
                            str38 = str28;
                            i4 = 0;
                            i5 = indexQuality;
                            str39 = str27;
                            function19 = function17;
                            function110 = function18;
                            hubCloud8 = hubCloud11;
                            str40 = str32;
                            str41 = sb3;
                            document2 = document6;
                            obj9 = obj21;
                            if (it2.hasNext()) {
                                Object next = it2.next();
                                uri4 = uri3;
                                Element element3 = (Element) next;
                                str43 = str39;
                                String attr2 = element3.attr(str4);
                                str44 = str4;
                                String ownText = element3.ownText();
                                String lowerCase = ownText.toLowerCase(Locale.ROOT);
                                str55 = ownText;
                                Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
                                Log.INSTANCE.d("Phisher", lowerCase);
                                str52 = lowerCase;
                                Object obj22 = obj9;
                                String str87 = str40;
                                if (StringsKt.contains$default(lowerCase, "fsl server", false, 2, (Object) null)) {
                                    HubCloud$getUrl$2$1 hubCloud$getUrl$2$1 = new HubCloud$getUrl$2$1(i5, null);
                                    hubCloud$getUrl$15.L$0 = hubCloud8;
                                    hubCloud$getUrl$15.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                    hubCloud$getUrl$15.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                    hubCloud$getUrl$15.L$3 = function110;
                                    hubCloud$getUrl$15.L$4 = function19;
                                    hubCloud$getUrl$15.L$5 = str29;
                                    hubCloud$getUrl$15.L$6 = str38;
                                    hubCloud$getUrl$15.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                    hubCloud$getUrl$15.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                    hubCloud$getUrl$15.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                    hubCloud$getUrl$15.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                    hubCloud$getUrl$15.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                    hubCloud$getUrl$15.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                    hubCloud$getUrl$15.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                    hubCloud$getUrl$15.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                    hubCloud$getUrl$15.L$15 = str41;
                                    hubCloud$getUrl$15.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                    hubCloud$getUrl$15.L$17 = it2;
                                    hubCloud$getUrl$15.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                    hubCloud$getUrl$15.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                    hubCloud$getUrl$15.L$20 = SpillingKt.nullOutSpilledVariable(attr2);
                                    hubCloud$getUrl$15.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                    hubCloud$getUrl$15.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                    hubCloud$getUrl$15.L$23 = function19;
                                    hubCloud$getUrl$15.L$24 = null;
                                    hubCloud$getUrl$15.L$25 = null;
                                    hubCloud$getUrl$15.I$0 = i5;
                                    hubCloud$getUrl$15.I$1 = i4;
                                    hubCloud$getUrl$15.I$2 = 0;
                                    hubCloud$getUrl$15.label = 3;
                                    HubCloud$getUrl$1 hubCloud$getUrl$18 = hubCloud$getUrl$15;
                                    Object newExtractorLink$default = ExtractorApiKt.newExtractorLink$default(str38 + " [FSL Server]", str38 + " [FSL Server] " + str41, attr2, (ExtractorLinkType) null, hubCloud$getUrl$2$1, hubCloud$getUrl$18, 8, (Object) null);
                                    if (newExtractorLink$default == obj22) {
                                        return obj22;
                                    }
                                    HubCloud hubCloud12 = hubCloud8;
                                    document3 = document2;
                                    int i11 = i5;
                                    int i12 = i4;
                                    Object obj23 = obj3;
                                    str42 = str87;
                                    String str88 = str38;
                                    function19.invoke(newExtractorLink$default);
                                    Unit unit = Unit.INSTANCE;
                                    hubCloud8 = hubCloud12;
                                    function111 = function19;
                                    function112 = function110;
                                    str45 = str5;
                                    str38 = str88;
                                    str46 = str25;
                                    obj10 = obj23;
                                    hubCloud$getUrl$15 = hubCloud$getUrl$18;
                                    obj9 = obj22;
                                    i4 = i12;
                                    str47 = str18;
                                    i5 = i11;
                                    str48 = str41;
                                } else {
                                    hubCloud$getUrl$12 = hubCloud$getUrl$15;
                                    if (StringsKt.contains$default(str52, "download file", false, 2, (Object) null)) {
                                        HubCloud$getUrl$2$2 hubCloud$getUrl$2$2 = new HubCloud$getUrl$2$2(i5, null);
                                        hubCloud$getUrl$12.L$0 = hubCloud8;
                                        hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                        hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                        hubCloud$getUrl$12.L$3 = function110;
                                        hubCloud$getUrl$12.L$4 = function19;
                                        hubCloud$getUrl$12.L$5 = str29;
                                        hubCloud$getUrl$12.L$6 = str38;
                                        hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                        hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                        hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                        hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                        hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                        hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                        hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                        hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                        hubCloud$getUrl$12.L$15 = str41;
                                        hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                        hubCloud$getUrl$12.L$17 = it2;
                                        hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                        hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                        hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(attr2);
                                        hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                        hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                        hubCloud$getUrl$12.L$23 = function19;
                                        hubCloud$getUrl$12.L$24 = null;
                                        hubCloud$getUrl$12.L$25 = null;
                                        hubCloud$getUrl$12.I$0 = i5;
                                        hubCloud$getUrl$12.I$1 = i4;
                                        hubCloud$getUrl$12.I$2 = 0;
                                        hubCloud$getUrl$12.label = 4;
                                        String str89 = str38;
                                        Object newExtractorLink$default2 = ExtractorApiKt.newExtractorLink$default(str89, str38 + " " + str41, attr2, (ExtractorLinkType) null, hubCloud$getUrl$2$2, hubCloud$getUrl$12, 8, (Object) null);
                                        if (newExtractorLink$default2 == obj22) {
                                            return obj22;
                                        }
                                        HubCloud hubCloud13 = hubCloud8;
                                        document3 = document2;
                                        Object obj24 = obj3;
                                        str42 = str87;
                                        int i13 = i5;
                                        int i14 = i4;
                                        function19.invoke(newExtractorLink$default2);
                                        Unit unit2 = Unit.INSTANCE;
                                        hubCloud8 = hubCloud13;
                                        function111 = function19;
                                        i4 = i14;
                                        function112 = function110;
                                        str45 = str5;
                                        i5 = i13;
                                        str46 = str25;
                                        str47 = str18;
                                        str38 = str89;
                                        obj10 = obj24;
                                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                        obj9 = obj22;
                                        str48 = str41;
                                    } else {
                                        str53 = str38;
                                        if (StringsKt.contains$default(str52, "buzzserver", false, 2, (Object) null)) {
                                            hubCloud$getUrl$12.L$0 = hubCloud8;
                                            hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                            hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                            hubCloud$getUrl$12.L$3 = function110;
                                            hubCloud$getUrl$12.L$4 = function19;
                                            hubCloud$getUrl$12.L$5 = str29;
                                            hubCloud$getUrl$12.L$6 = str53;
                                            hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                            hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                            hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                            hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                            hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                            hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                            hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                            hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                            hubCloud$getUrl$12.L$15 = str41;
                                            hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                            hubCloud$getUrl$12.L$17 = it2;
                                            hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                            hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                            hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(attr2);
                                            hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                            hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                            hubCloud$getUrl$12.L$23 = null;
                                            hubCloud$getUrl$12.L$24 = null;
                                            hubCloud$getUrl$12.L$25 = null;
                                            hubCloud$getUrl$12.I$0 = i5;
                                            hubCloud$getUrl$12.I$1 = i4;
                                            i7 = 0;
                                            hubCloud$getUrl$12.I$2 = 0;
                                            hubCloud$getUrl$12.label = 5;
                                            Object obj25 = Requests.get$default(MainActivityKt.getApp(), attr2 + "/download", (Map) null, attr2, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, hubCloud$getUrl$12, 4058, (Object) null);
                                            if (obj25 == obj22) {
                                                return obj22;
                                            }
                                            function111 = function19;
                                            obj11 = obj22;
                                            i6 = i5;
                                            obj12 = obj3;
                                            obj13 = obj25;
                                            hubCloud$getUrl$16 = hubCloud$getUrl$12;
                                            str54 = str29;
                                            str50 = str18;
                                            obj14 = next;
                                            element = element3;
                                            str49 = str87;
                                            str51 = attr2;
                                            NiceResponse niceResponse = (NiceResponse) obj13;
                                            Object obj26 = obj14;
                                            str74 = niceResponse.getHeaders().get("hx-redirect");
                                            if (str74 != null) {
                                                str75 = str74;
                                            } else {
                                                String str90 = niceResponse.getHeaders().get("HX-Redirect");
                                                str75 = str90 == null ? str5 : str90;
                                            }
                                            if (StringsKt.isBlank(str75)) {
                                                HubCloud$getUrl$2$3 hubCloud$getUrl$2$3 = new HubCloud$getUrl$2$3(i6, null);
                                                hubCloud$getUrl$16.L$0 = hubCloud8;
                                                hubCloud$getUrl$16.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                                hubCloud$getUrl$16.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                                hubCloud$getUrl$16.L$3 = function110;
                                                hubCloud$getUrl$16.L$4 = function111;
                                                hubCloud$getUrl$16.L$5 = str54;
                                                hubCloud$getUrl$16.L$6 = str53;
                                                hubCloud$getUrl$16.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                                hubCloud$getUrl$16.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                                hubCloud$getUrl$16.L$9 = SpillingKt.nullOutSpilledVariable(str50);
                                                hubCloud$getUrl$16.L$10 = SpillingKt.nullOutSpilledVariable(str49);
                                                hubCloud$getUrl$16.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                                hubCloud$getUrl$16.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                                hubCloud$getUrl$16.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                                hubCloud$getUrl$16.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                                hubCloud$getUrl$16.L$15 = str41;
                                                hubCloud$getUrl$16.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                                hubCloud$getUrl$16.L$17 = it2;
                                                hubCloud$getUrl$16.L$18 = SpillingKt.nullOutSpilledVariable(obj26);
                                                hubCloud$getUrl$16.L$19 = SpillingKt.nullOutSpilledVariable(element);
                                                hubCloud$getUrl$16.L$20 = SpillingKt.nullOutSpilledVariable(str51);
                                                hubCloud$getUrl$16.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                                hubCloud$getUrl$16.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                                hubCloud$getUrl$16.L$23 = function111;
                                                hubCloud$getUrl$16.L$24 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                                hubCloud$getUrl$16.L$25 = SpillingKt.nullOutSpilledVariable(str75);
                                                hubCloud$getUrl$16.I$0 = i6;
                                                hubCloud$getUrl$16.I$1 = i4;
                                                hubCloud$getUrl$16.I$2 = i7;
                                                hubCloud$getUrl$16.label = 6;
                                                HubCloud$getUrl$1 hubCloud$getUrl$19 = hubCloud$getUrl$16;
                                                Object newExtractorLink$default3 = ExtractorApiKt.newExtractorLink$default(str53 + " [BuzzServer]", str53 + " [BuzzServer] " + str41, str75, (ExtractorLinkType) null, hubCloud$getUrl$2$3, hubCloud$getUrl$19, 8, (Object) null);
                                                if (newExtractorLink$default3 == obj11) {
                                                    return obj11;
                                                }
                                                function113 = function111;
                                                str57 = str51;
                                                str59 = str55;
                                                str58 = str52;
                                                obj15 = obj26;
                                                str56 = str54;
                                                hubCloud9 = hubCloud8;
                                                obj20 = newExtractorLink$default3;
                                                hubCloud$getUrl$12 = hubCloud$getUrl$19;
                                                function113.invoke(obj20);
                                                Object obj27 = obj12;
                                                int i15 = i4;
                                                Unit unit3 = Unit.INSTANCE;
                                                hubCloud8 = hubCloud9;
                                                str29 = str56;
                                                obj10 = obj27;
                                                String str91 = str49;
                                                document3 = document2;
                                                str47 = str50;
                                                str42 = str91;
                                                str38 = str53;
                                                hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                                i4 = i15;
                                                obj9 = obj11;
                                                str45 = str5;
                                                str46 = str25;
                                                i5 = i6;
                                                function112 = function110;
                                                str48 = str41;
                                            } else {
                                                HubCloud$getUrl$1 hubCloud$getUrl$110 = hubCloud$getUrl$16;
                                                Log.INSTANCE.w(str54, "BuzzServer: No redirect");
                                                String str92 = str54;
                                                HubCloud hubCloud14 = hubCloud8;
                                                Object obj28 = obj12;
                                                int i16 = i4;
                                                Unit unit4 = Unit.INSTANCE;
                                                hubCloud8 = hubCloud14;
                                                str29 = str92;
                                                obj10 = obj28;
                                                String str93 = str49;
                                                document3 = document2;
                                                str47 = str50;
                                                str42 = str93;
                                                str38 = str53;
                                                hubCloud$getUrl$15 = hubCloud$getUrl$110;
                                                i4 = i16;
                                                obj9 = obj11;
                                                str45 = str5;
                                                str46 = str25;
                                                i5 = i6;
                                                function112 = function110;
                                                str48 = str41;
                                            }
                                        } else {
                                            if (StringsKt.contains$default(str52, "pixeldra", false, 2, (Object) null) || StringsKt.contains$default(str52, "pixelserver", false, 2, (Object) null) || StringsKt.contains$default(str52, "pixel server", false, 2, (Object) null)) {
                                                function111 = function19;
                                                function112 = function110;
                                                str76 = str5;
                                                obj17 = obj22;
                                                str77 = attr2;
                                            } else if (StringsKt.contains$default(str52, "pixeldrain", false, 2, (Object) null)) {
                                                function111 = function19;
                                                function112 = function110;
                                                str76 = str5;
                                                obj17 = obj22;
                                                str77 = attr2;
                                            } else if (StringsKt.contains$default(str52, "s3 server", false, 2, (Object) null)) {
                                                HubCloud$getUrl$2$5 hubCloud$getUrl$2$5 = new HubCloud$getUrl$2$5(i5, null);
                                                hubCloud$getUrl$12.L$0 = hubCloud8;
                                                hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                                hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                                hubCloud$getUrl$12.L$3 = function110;
                                                hubCloud$getUrl$12.L$4 = function19;
                                                hubCloud$getUrl$12.L$5 = str29;
                                                hubCloud$getUrl$12.L$6 = str53;
                                                hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                                hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                                hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                                hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                                hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                                hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                                hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                                hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                                hubCloud$getUrl$12.L$15 = str41;
                                                hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                                hubCloud$getUrl$12.L$17 = it2;
                                                hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                                hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                                hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(attr2);
                                                hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                                hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                                hubCloud$getUrl$12.L$23 = function19;
                                                hubCloud$getUrl$12.L$24 = null;
                                                hubCloud$getUrl$12.L$25 = null;
                                                hubCloud$getUrl$12.I$0 = i5;
                                                hubCloud$getUrl$12.I$1 = i4;
                                                hubCloud$getUrl$12.I$2 = 0;
                                                hubCloud$getUrl$12.label = 8;
                                                Object newExtractorLink$default4 = ExtractorApiKt.newExtractorLink$default(str53 + " [S3 Server]", str53 + " [S3 Server] " + str41, attr2, (ExtractorLinkType) null, hubCloud$getUrl$2$5, hubCloud$getUrl$12, 8, (Object) null);
                                                if (newExtractorLink$default4 == obj22) {
                                                    return obj22;
                                                }
                                                HubCloud hubCloud15 = hubCloud8;
                                                int i17 = i5;
                                                int i18 = i4;
                                                Object obj29 = obj3;
                                                str42 = str87;
                                                document3 = document2;
                                                function19.invoke(newExtractorLink$default4);
                                                Unit unit5 = Unit.INSTANCE;
                                                i5 = i17;
                                                i4 = i18;
                                                String str94 = str18;
                                                str48 = str41;
                                                str47 = str94;
                                                hubCloud8 = hubCloud15;
                                                hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                                obj9 = obj22;
                                                str45 = str5;
                                                str38 = str53;
                                                obj10 = obj29;
                                                function111 = function19;
                                                function112 = function110;
                                                str46 = str25;
                                            } else if (StringsKt.contains$default(str52, "fslv2", false, 2, (Object) null)) {
                                                HubCloud$getUrl$2$6 hubCloud$getUrl$2$6 = new HubCloud$getUrl$2$6(i5, null);
                                                hubCloud$getUrl$12.L$0 = hubCloud8;
                                                hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                                hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                                hubCloud$getUrl$12.L$3 = function110;
                                                hubCloud$getUrl$12.L$4 = function19;
                                                hubCloud$getUrl$12.L$5 = str29;
                                                hubCloud$getUrl$12.L$6 = str53;
                                                hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                                hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                                hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                                hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                                hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                                hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                                hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                                hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                                hubCloud$getUrl$12.L$15 = str41;
                                                hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                                hubCloud$getUrl$12.L$17 = it2;
                                                hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                                hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                                hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(attr2);
                                                hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                                hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                                hubCloud$getUrl$12.L$23 = function19;
                                                hubCloud$getUrl$12.L$24 = null;
                                                hubCloud$getUrl$12.L$25 = null;
                                                hubCloud$getUrl$12.I$0 = i5;
                                                hubCloud$getUrl$12.I$1 = i4;
                                                hubCloud$getUrl$12.I$2 = 0;
                                                hubCloud$getUrl$12.label = 9;
                                                Object newExtractorLink$default5 = ExtractorApiKt.newExtractorLink$default(str53 + " [FSLv2]", str53 + " [FSLv2] " + str41, attr2, (ExtractorLinkType) null, hubCloud$getUrl$2$6, hubCloud$getUrl$12, 8, (Object) null);
                                                if (newExtractorLink$default5 == obj22) {
                                                    return obj22;
                                                }
                                                HubCloud hubCloud16 = hubCloud8;
                                                int i19 = i5;
                                                int i20 = i4;
                                                Object obj30 = obj3;
                                                str42 = str87;
                                                document3 = document2;
                                                function19.invoke(newExtractorLink$default5);
                                                Unit unit6 = Unit.INSTANCE;
                                                i5 = i19;
                                                i4 = i20;
                                                String str95 = str18;
                                                str48 = str41;
                                                str47 = str95;
                                                hubCloud8 = hubCloud16;
                                                hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                                obj9 = obj22;
                                                str45 = str5;
                                                str38 = str53;
                                                obj10 = obj30;
                                                function111 = function19;
                                                function112 = function110;
                                                str46 = str25;
                                            } else if (StringsKt.contains$default(str52, "mega server", false, 2, (Object) null)) {
                                                HubCloud$getUrl$2$7 hubCloud$getUrl$2$7 = new HubCloud$getUrl$2$7(i5, null);
                                                hubCloud$getUrl$12.L$0 = hubCloud8;
                                                hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                                hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                                hubCloud$getUrl$12.L$3 = function110;
                                                hubCloud$getUrl$12.L$4 = function19;
                                                hubCloud$getUrl$12.L$5 = str29;
                                                hubCloud$getUrl$12.L$6 = str53;
                                                hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                                hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                                hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                                hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                                hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                                hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                                hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                                hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                                hubCloud$getUrl$12.L$15 = str41;
                                                hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                                hubCloud$getUrl$12.L$17 = it2;
                                                hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                                hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                                hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(attr2);
                                                hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                                hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                                hubCloud$getUrl$12.L$23 = function19;
                                                hubCloud$getUrl$12.L$24 = null;
                                                hubCloud$getUrl$12.L$25 = null;
                                                hubCloud$getUrl$12.I$0 = i5;
                                                hubCloud$getUrl$12.I$1 = i4;
                                                hubCloud$getUrl$12.I$2 = 0;
                                                hubCloud$getUrl$12.label = 10;
                                                Object newExtractorLink$default6 = ExtractorApiKt.newExtractorLink$default(str53 + " [Mega Server]", str53 + " [Mega Server] " + str41, attr2, (ExtractorLinkType) null, hubCloud$getUrl$2$7, hubCloud$getUrl$12, 8, (Object) null);
                                                if (newExtractorLink$default6 == obj22) {
                                                    return obj22;
                                                }
                                                HubCloud hubCloud17 = hubCloud8;
                                                int i21 = i5;
                                                int i22 = i4;
                                                Object obj31 = obj3;
                                                str42 = str87;
                                                document3 = document2;
                                                function19.invoke(newExtractorLink$default6);
                                                Unit unit7 = Unit.INSTANCE;
                                                i5 = i21;
                                                i4 = i22;
                                                String str96 = str18;
                                                str48 = str41;
                                                str47 = str96;
                                                hubCloud8 = hubCloud17;
                                                hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                                obj9 = obj22;
                                                str45 = str5;
                                                str38 = str53;
                                                obj10 = obj31;
                                                function111 = function19;
                                                function112 = function110;
                                                str46 = str25;
                                            } else if (StringsKt.contains$default(str52, "gofile", false, 2, (Object) null)) {
                                                try {
                                                } catch (Exception e) {
                                                    e = e;
                                                    Function1<? super ExtractorLink, Unit> function119 = function19;
                                                    Function1<? super SubtitleFile, Unit> function120 = function110;
                                                    i10 = i5;
                                                    i8 = 0;
                                                    uri5 = uri4;
                                                    str67 = str43;
                                                    i9 = i4;
                                                    document4 = document2;
                                                    str60 = str52;
                                                    str63 = str35;
                                                    str69 = str37;
                                                    str64 = str34;
                                                    it3 = it2;
                                                    coroutine_suspended = obj22;
                                                    str71 = str53;
                                                    obj16 = next;
                                                    str65 = str87;
                                                    str70 = str22;
                                                    iterable2 = iterable;
                                                    function115 = function120;
                                                    hubCloud10 = hubCloud8;
                                                    obj20 = obj3;
                                                    str66 = str18;
                                                    str48 = str41;
                                                    element2 = element3;
                                                    function114 = function119;
                                                    str62 = attr2;
                                                    str72 = str29;
                                                    str61 = str55;
                                                    break;
                                                }
                                                Gofile gofile = new Gofile();
                                                hubCloud$getUrl$12.L$0 = hubCloud8;
                                                hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                                hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                                hubCloud$getUrl$12.L$3 = function110;
                                                hubCloud$getUrl$12.L$4 = function19;
                                                hubCloud$getUrl$12.L$5 = str29;
                                                hubCloud$getUrl$12.L$6 = str53;
                                                hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                                hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                                hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                                hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                                hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                                hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                                hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                                hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                                hubCloud$getUrl$12.L$15 = str41;
                                                hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                                hubCloud$getUrl$12.L$17 = it2;
                                                hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                                hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                                hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(attr2);
                                                hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                                hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                                hubCloud$getUrl$12.L$23 = null;
                                                hubCloud$getUrl$12.L$24 = null;
                                                hubCloud$getUrl$12.L$25 = null;
                                                hubCloud$getUrl$12.I$0 = i5;
                                                hubCloud$getUrl$12.I$1 = i4;
                                                hubCloud$getUrl$12.I$2 = 0;
                                                hubCloud$getUrl$12.label = 11;
                                                Function1<? super ExtractorLink, Unit> function121 = function19;
                                                Function1<? super SubtitleFile, Unit> function122 = function110;
                                                if (gofile.getUrl(attr2, str53, function122, function121, hubCloud$getUrl$12) == obj22) {
                                                    return obj22;
                                                }
                                                i10 = i5;
                                                i8 = 0;
                                                str68 = str29;
                                                str61 = str55;
                                                uri5 = uri4;
                                                str67 = str43;
                                                i9 = i4;
                                                document5 = document2;
                                                str60 = str52;
                                                str73 = str53;
                                                str63 = str35;
                                                str69 = str37;
                                                obj16 = next;
                                                str65 = str87;
                                                str64 = str34;
                                                str70 = str22;
                                                it3 = it2;
                                                coroutine_suspended = obj22;
                                                iterable2 = iterable;
                                                function116 = function122;
                                                hubCloud10 = hubCloud8;
                                                obj20 = obj3;
                                                str66 = str18;
                                                str48 = str41;
                                                element2 = element3;
                                                function114 = function121;
                                                str62 = attr2;
                                                HubCloud hubCloud18 = hubCloud10;
                                                Document document7 = document5;
                                                int i23 = i10;
                                                Function1<? super SubtitleFile, Unit> function123 = function116;
                                                Unit unit8 = Unit.INSTANCE;
                                                Object obj32 = obj20;
                                                hubCloud8 = hubCloud18;
                                                i5 = i23;
                                                str29 = str68;
                                                obj10 = obj32;
                                                hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                                obj9 = coroutine_suspended;
                                                it2 = it3;
                                                str34 = str64;
                                                str37 = str69;
                                                URI uri8 = uri5;
                                                str43 = str67;
                                                uri4 = uri8;
                                                str38 = str73;
                                                i4 = i9;
                                                str35 = str63;
                                                str45 = str5;
                                                str47 = str66;
                                                function111 = function114;
                                                str46 = str25;
                                                str42 = str65;
                                                document3 = document7;
                                                function112 = function123;
                                                iterable = iterable2;
                                                str22 = str70;
                                            } else {
                                                function111 = function19;
                                                function112 = function110;
                                                if (StringsKt.contains$default(str52, "10gbps", false, 2, (Object) null)) {
                                                    str79 = str5;
                                                    obj18 = obj22;
                                                    str80 = attr2;
                                                } else if (StringsKt.contains$default(attr2, "gpdl", false, 2, (Object) null)) {
                                                    str79 = str5;
                                                    obj18 = obj22;
                                                    str80 = attr2;
                                                } else {
                                                    if (StringsKt.contains$default(str52, "telegram", false, 2, (Object) null) || StringsKt.contains$default(attr2, "t.me", false, 2, (Object) null)) {
                                                        str81 = str5;
                                                        obj19 = obj22;
                                                        str82 = attr2;
                                                    } else if (StringsKt.contains$default(attr2, "tg/go", false, 2, (Object) null)) {
                                                        str81 = str5;
                                                        obj19 = obj22;
                                                        str82 = attr2;
                                                    } else {
                                                        hubCloud$getUrl$12.L$0 = hubCloud8;
                                                        hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                                        hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                                        hubCloud$getUrl$12.L$3 = function112;
                                                        hubCloud$getUrl$12.L$4 = function111;
                                                        hubCloud$getUrl$12.L$5 = str29;
                                                        hubCloud$getUrl$12.L$6 = str53;
                                                        hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                                        hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                                        hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                                        hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                                        hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                                        hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                                        hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                                        hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                                        hubCloud$getUrl$12.L$15 = str41;
                                                        hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                                        hubCloud$getUrl$12.L$17 = it2;
                                                        hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                                        hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                                        hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(attr2);
                                                        hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                                        hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                                        hubCloud$getUrl$12.L$23 = null;
                                                        hubCloud$getUrl$12.L$24 = null;
                                                        hubCloud$getUrl$12.L$25 = null;
                                                        hubCloud$getUrl$12.I$0 = i5;
                                                        hubCloud$getUrl$12.I$1 = i4;
                                                        hubCloud$getUrl$12.I$2 = 0;
                                                        hubCloud$getUrl$12.label = 13;
                                                        String str97 = str5;
                                                        if (ExtractorApiKt.loadExtractor(attr2, str97, function112, function111, hubCloud$getUrl$12) == obj22) {
                                                            return obj22;
                                                        }
                                                        str38 = str53;
                                                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                                        obj9 = obj19;
                                                        str45 = str81;
                                                        str46 = str25;
                                                        document3 = document2;
                                                        str47 = str18;
                                                        str48 = str41;
                                                        obj10 = obj3;
                                                        str42 = str87;
                                                    }
                                                    Unit unit9 = Unit.INSTANCE;
                                                    str38 = str53;
                                                    hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                                    obj9 = obj19;
                                                    str45 = str81;
                                                    str46 = str25;
                                                    document3 = document2;
                                                    str47 = str18;
                                                    str48 = str41;
                                                    obj10 = obj3;
                                                    str42 = str87;
                                                }
                                                HubCloud$getUrl$2$8 hubCloud$getUrl$2$8 = new HubCloud$getUrl$2$8(i5, null);
                                                hubCloud$getUrl$12.L$0 = hubCloud8;
                                                hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                                hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                                hubCloud$getUrl$12.L$3 = function112;
                                                hubCloud$getUrl$12.L$4 = function111;
                                                hubCloud$getUrl$12.L$5 = str29;
                                                hubCloud$getUrl$12.L$6 = str53;
                                                hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                                hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                                hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                                hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                                hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                                hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                                hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                                hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                                hubCloud$getUrl$12.L$15 = str41;
                                                hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                                hubCloud$getUrl$12.L$17 = it2;
                                                hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                                hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                                hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(str80);
                                                hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                                hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                                hubCloud$getUrl$12.L$23 = function111;
                                                hubCloud$getUrl$12.L$24 = null;
                                                hubCloud$getUrl$12.L$25 = null;
                                                hubCloud$getUrl$12.I$0 = i5;
                                                hubCloud$getUrl$12.I$1 = i4;
                                                hubCloud$getUrl$12.I$2 = 0;
                                                hubCloud$getUrl$12.label = 12;
                                                Object newExtractorLink$default7 = ExtractorApiKt.newExtractorLink$default(str53 + " [10Gbps]", str53 + " [10Gbps] " + str41, str80, (ExtractorLinkType) null, hubCloud$getUrl$2$8, hubCloud$getUrl$12, 8, (Object) null);
                                                if (newExtractorLink$default7 == obj18) {
                                                    return obj18;
                                                }
                                                HubCloud hubCloud19 = hubCloud8;
                                                Object obj33 = obj3;
                                                Document document8 = document2;
                                                String str98 = str36;
                                                int i24 = i5;
                                                int i25 = i4;
                                                function111.invoke(newExtractorLink$default7);
                                                Unit unit10 = Unit.INSTANCE;
                                                hubCloud8 = hubCloud19;
                                                str45 = str79;
                                                i4 = i25;
                                                i5 = i24;
                                                str36 = str98;
                                                document3 = document8;
                                                str42 = str87;
                                                str46 = str25;
                                                str47 = str18;
                                                str48 = str41;
                                                obj10 = obj33;
                                                hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                                obj9 = obj18;
                                                str38 = str53;
                                            }
                                            String baseUrl = hubCloud8.getBaseUrl(str77);
                                            str45 = str76;
                                            int i26 = i4;
                                            Object obj34 = obj17;
                                            if (StringsKt.contains$default(str77, "download", false, 2, (Object) null)) {
                                                str78 = str77;
                                                str46 = str25;
                                            } else {
                                                str46 = str25;
                                                str78 = baseUrl + "/api/file/" + StringsKt.substringAfterLast$default(str77, str46, (String) null, 2, (Object) null) + "?download";
                                            }
                                            HubCloud$getUrl$2$4 hubCloud$getUrl$2$4 = new HubCloud$getUrl$2$4(i5, null);
                                            hubCloud$getUrl$12.L$0 = hubCloud8;
                                            hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str22);
                                            hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str37);
                                            hubCloud$getUrl$12.L$3 = function112;
                                            hubCloud$getUrl$12.L$4 = function111;
                                            hubCloud$getUrl$12.L$5 = str29;
                                            hubCloud$getUrl$12.L$6 = str53;
                                            hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri4);
                                            hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(str43);
                                            hubCloud$getUrl$12.L$9 = SpillingKt.nullOutSpilledVariable(str18);
                                            hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(str87);
                                            hubCloud$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(document2);
                                            hubCloud$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                            hubCloud$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(str34);
                                            hubCloud$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str35);
                                            hubCloud$getUrl$12.L$15 = str41;
                                            hubCloud$getUrl$12.L$16 = SpillingKt.nullOutSpilledVariable(iterable);
                                            hubCloud$getUrl$12.L$17 = it2;
                                            hubCloud$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(next);
                                            hubCloud$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(element3);
                                            hubCloud$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(str77);
                                            hubCloud$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str55);
                                            hubCloud$getUrl$12.L$22 = SpillingKt.nullOutSpilledVariable(str52);
                                            hubCloud$getUrl$12.L$23 = function111;
                                            hubCloud$getUrl$12.L$24 = SpillingKt.nullOutSpilledVariable(baseUrl);
                                            hubCloud$getUrl$12.L$25 = SpillingKt.nullOutSpilledVariable(str78);
                                            hubCloud$getUrl$12.I$0 = i5;
                                            hubCloud$getUrl$12.I$1 = i26;
                                            hubCloud$getUrl$12.I$2 = 0;
                                            hubCloud$getUrl$12.label = 7;
                                            Object newExtractorLink$default8 = ExtractorApiKt.newExtractorLink$default(str53 + " Pixeldrain", str53 + " Pixeldrain " + str41, str78, (ExtractorLinkType) null, hubCloud$getUrl$2$4, hubCloud$getUrl$12, 8, (Object) null);
                                            if (newExtractorLink$default8 == obj34) {
                                                return obj34;
                                            }
                                            String str99 = str34;
                                            Object obj35 = obj3;
                                            String str100 = str36;
                                            int i27 = i5;
                                            HubCloud hubCloud20 = hubCloud8;
                                            function111.invoke(newExtractorLink$default8);
                                            Unit unit11 = Unit.INSTANCE;
                                            hubCloud8 = hubCloud20;
                                            i4 = i26;
                                            i5 = i27;
                                            str34 = str99;
                                            str36 = str100;
                                            str42 = str87;
                                            document3 = document2;
                                            str38 = str53;
                                            str47 = str18;
                                            str48 = str41;
                                            obj10 = obj35;
                                            hubCloud$getUrl$15 = hubCloud$getUrl$12;
                                            obj9 = obj34;
                                        }
                                    }
                                }
                                function110 = function112;
                                str25 = str46;
                                function19 = function111;
                                str41 = str48;
                                str18 = str47;
                                document2 = document3;
                                str40 = str42;
                                uri3 = uri4;
                                str39 = str43;
                                str4 = str44;
                                obj3 = obj10;
                                str5 = str45;
                                if (it2.hasNext()) {
                                    return Unit.INSTANCE;
                                }
                            }
                        } else {
                            Requests app2 = MainActivityKt.getApp();
                            hubCloud$getUrl$12.L$0 = hubCloud;
                            hubCloud$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str3);
                            hubCloud$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str2);
                            hubCloud$getUrl$12.L$3 = function1;
                            hubCloud$getUrl$12.L$4 = function12;
                            hubCloud$getUrl$12.L$5 = "HubCloud";
                            hubCloud$getUrl$12.L$6 = "HubCloud";
                            hubCloud$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri6);
                            hubCloud$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(uri7);
                            hubCloud$getUrl$12.L$9 = str83;
                            hubCloud$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(hubCloud);
                            hubCloud$getUrl$12.I$0 = 0;
                            hubCloud$getUrl$12.label = 1;
                            str5 = "";
                            str6 = "HubCloud";
                            obj3 = obj20;
                            str12 = "/";
                            str4 = "href";
                            obj2 = coroutine_suspended;
                            try {
                                obj4 = Requests.get$default(app2, uri7, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, hubCloud$getUrl$12, 4094, (Object) null);
                                hubCloud$getUrl$13 = hubCloud$getUrl$12;
                            } catch (Throwable th8) {
                                th = th8;
                                hubCloud$getUrl$13 = hubCloud$getUrl$12;
                                str7 = str12;
                                i = 2;
                                str3 = str;
                                function13 = function1;
                                function14 = function12;
                                str8 = uri7;
                                uri = uri6;
                                str9 = "HubCloud";
                                str10 = str83;
                                hubCloud2 = hubCloud;
                                str11 = str2;
                                Result.Companion companion42 = Result.Companion;
                                obj6 = Result.constructor-impl(ResultKt.createFailure(th));
                                hubCloud5 = hubCloud2;
                                str18 = str10;
                                str19 = str9;
                                str20 = str6;
                                str21 = str8;
                                uri2 = uri;
                                str22 = str3;
                                function15 = function14;
                                function16 = function13;
                                th2 = Result.exceptionOrNull-impl(obj6);
                                if (th2 != null) {
                                }
                                str23 = (String) obj6;
                                if (!StringsKt.isBlank(str23)) {
                                }
                            }
                            if (obj4 == obj2) {
                                return obj2;
                            }
                            function13 = function1;
                            function14 = function12;
                            hubCloud3 = hubCloud;
                            obj5 = obj4;
                            str8 = uri7;
                            uri = uri6;
                            str9 = "HubCloud";
                            str13 = str6;
                            str10 = str83;
                            i2 = 0;
                            hubCloud4 = hubCloud;
                            str14 = str;
                            str11 = str2;
                            try {
                                document = ((NiceResponse) obj5).getDocument();
                                selectFirst = document.selectFirst("#download");
                            } catch (Throwable th9) {
                                th = th9;
                                HubCloud hubCloud21 = hubCloud4;
                                str30 = str14;
                                str7 = str12;
                                i = 2;
                                hubCloud2 = hubCloud21;
                                str6 = str13;
                            }
                            if (selectFirst != null) {
                                try {
                                    str15 = selectFirst.attr(str4);
                                } catch (Throwable th10) {
                                    th = th10;
                                    str6 = str13;
                                    i = 2;
                                    str3 = str14;
                                    hubCloud2 = hubCloud4;
                                    str7 = str12;
                                    Result.Companion companion422 = Result.Companion;
                                    obj6 = Result.constructor-impl(ResultKt.createFailure(th));
                                    hubCloud5 = hubCloud2;
                                    str18 = str10;
                                    str19 = str9;
                                    str20 = str6;
                                    str21 = str8;
                                    uri2 = uri;
                                    str22 = str3;
                                    function15 = function14;
                                    function16 = function13;
                                    th2 = Result.exceptionOrNull-impl(obj6);
                                    if (th2 != null) {
                                    }
                                    str23 = (String) obj6;
                                    if (!StringsKt.isBlank(str23)) {
                                    }
                                }
                                if (str15 != null) {
                                    i3 = i2;
                                    hubCloud6 = hubCloud4;
                                    str31 = str14;
                                    hubCloud7 = hubCloud3;
                                    i = 2;
                                    c = 0;
                                    if (StringsKt.startsWith(str15, "http", true)) {
                                        if (str15.length() > 0) {
                                            char[] cArr = new char[1];
                                            cArr[c] = '/';
                                            String trimEnd = StringsKt.trimEnd(str10, cArr);
                                            char[] cArr2 = new char[1];
                                            cArr2[c] = '/';
                                            str7 = str12;
                                            try {
                                                String str101 = trimEnd + str7 + StringsKt.trimStart(str15, cArr2);
                                                hubCloud2 = hubCloud6;
                                                str15 = str101;
                                                str16 = str9;
                                                str17 = str13;
                                                str3 = str31;
                                            } catch (Throwable th11) {
                                                th = th11;
                                                hubCloud2 = hubCloud6;
                                                str6 = str13;
                                                str3 = str31;
                                                Result.Companion companion4222 = Result.Companion;
                                                obj6 = Result.constructor-impl(ResultKt.createFailure(th));
                                                hubCloud5 = hubCloud2;
                                                str18 = str10;
                                                str19 = str9;
                                                str20 = str6;
                                                str21 = str8;
                                                uri2 = uri;
                                                str22 = str3;
                                                function15 = function14;
                                                function16 = function13;
                                                th2 = Result.exceptionOrNull-impl(obj6);
                                                if (th2 != null) {
                                                }
                                                str23 = (String) obj6;
                                                if (!StringsKt.isBlank(str23)) {
                                                }
                                            }
                                        } else {
                                            str7 = str12;
                                            hubCloud2 = hubCloud6;
                                            str16 = str9;
                                            str15 = str5;
                                            str17 = str13;
                                            str3 = str31;
                                        }
                                    } else {
                                        hubCloud2 = hubCloud6;
                                        str16 = str9;
                                        str7 = str12;
                                        str17 = str13;
                                        str3 = str31;
                                    }
                                    obj6 = Result.constructor-impl(str15);
                                    hubCloud5 = hubCloud2;
                                    str18 = str10;
                                    str20 = str17;
                                    str19 = str16;
                                    str21 = str8;
                                    uri2 = uri;
                                    str22 = str3;
                                    function15 = function14;
                                    function16 = function13;
                                    th2 = Result.exceptionOrNull-impl(obj6);
                                    if (th2 != null) {
                                    }
                                    str23 = (String) obj6;
                                    if (!StringsKt.isBlank(str23)) {
                                    }
                                }
                            }
                            it = document.select("a[href]").iterator();
                            while (it.hasNext()) {
                                try {
                                    obj8 = it.next();
                                    i3 = i2;
                                    attr = ((Element) obj8).attr(str4);
                                    hubCloud6 = hubCloud4;
                                } catch (Throwable th12) {
                                    th = th12;
                                    str30 = str14;
                                    i = 2;
                                    hubCloud2 = hubCloud4;
                                    str6 = str13;
                                    str7 = str12;
                                    str3 = str30;
                                    Result.Companion companion42222 = Result.Companion;
                                    obj6 = Result.constructor-impl(ResultKt.createFailure(th));
                                    hubCloud5 = hubCloud2;
                                    str18 = str10;
                                    str19 = str9;
                                    str20 = str6;
                                    str21 = str8;
                                    uri2 = uri;
                                    str22 = str3;
                                    function15 = function14;
                                    function16 = function13;
                                    th2 = Result.exceptionOrNull-impl(obj6);
                                    if (th2 != null) {
                                    }
                                    str23 = (String) obj6;
                                    if (!StringsKt.isBlank(str23)) {
                                    }
                                }
                                try {
                                    str31 = str14;
                                    hubCloud7 = hubCloud3;
                                    Document document9 = document;
                                    i = 2;
                                    c = 0;
                                    try {
                                        if (!(StringsKt.contains$default(attr, "hubcloud.php", false, 2, (Object) null) || StringsKt.contains$default(attr, "gamerxyt.com", false, 2, (Object) null))) {
                                            hubCloud4 = hubCloud6;
                                            i2 = i3;
                                            hubCloud3 = hubCloud7;
                                            document = document9;
                                            str14 = str31;
                                        }
                                        Element element4 = (Element) obj8;
                                        str15 = element4 != null ? element4.attr(str4) : null;
                                        if (str15 == null) {
                                            str15 = str5;
                                        }
                                        if (StringsKt.startsWith(str15, "http", true)) {
                                        }
                                        obj6 = Result.constructor-impl(str15);
                                        hubCloud5 = hubCloud2;
                                        str18 = str10;
                                        str20 = str17;
                                        str19 = str16;
                                        str21 = str8;
                                        uri2 = uri;
                                        str22 = str3;
                                        function15 = function14;
                                        function16 = function13;
                                    } catch (Throwable th13) {
                                        th = th13;
                                        hubCloud2 = hubCloud6;
                                        str6 = str13;
                                        str7 = str12;
                                        str3 = str31;
                                        Result.Companion companion422222 = Result.Companion;
                                        obj6 = Result.constructor-impl(ResultKt.createFailure(th));
                                        hubCloud5 = hubCloud2;
                                        str18 = str10;
                                        str19 = str9;
                                        str20 = str6;
                                        str21 = str8;
                                        uri2 = uri;
                                        str22 = str3;
                                        function15 = function14;
                                        function16 = function13;
                                        th2 = Result.exceptionOrNull-impl(obj6);
                                        if (th2 != null) {
                                        }
                                        str23 = (String) obj6;
                                        if (!StringsKt.isBlank(str23)) {
                                        }
                                    }
                                } catch (Throwable th14) {
                                    th = th14;
                                    String str102 = str14;
                                    i = 2;
                                    hubCloud2 = hubCloud6;
                                    str6 = str13;
                                    str7 = str12;
                                    str3 = str102;
                                    Result.Companion companion4222222 = Result.Companion;
                                    obj6 = Result.constructor-impl(ResultKt.createFailure(th));
                                    hubCloud5 = hubCloud2;
                                    str18 = str10;
                                    str19 = str9;
                                    str20 = str6;
                                    str21 = str8;
                                    uri2 = uri;
                                    str22 = str3;
                                    function15 = function14;
                                    function16 = function13;
                                    th2 = Result.exceptionOrNull-impl(obj6);
                                    if (th2 != null) {
                                    }
                                    str23 = (String) obj6;
                                    if (!StringsKt.isBlank(str23)) {
                                    }
                                }
                                th2 = Result.exceptionOrNull-impl(obj6);
                                if (th2 != null) {
                                }
                                str23 = (String) obj6;
                                if (!StringsKt.isBlank(str23)) {
                                }
                            }
                            i3 = i2;
                            hubCloud6 = hubCloud4;
                            str31 = str14;
                            hubCloud7 = hubCloud3;
                            i = 2;
                            c = 0;
                            obj8 = null;
                            Element element42 = (Element) obj8;
                            if (element42 != null) {
                            }
                            if (str15 == null) {
                            }
                            if (StringsKt.startsWith(str15, "http", true)) {
                            }
                            obj6 = Result.constructor-impl(str15);
                            hubCloud5 = hubCloud2;
                            str18 = str10;
                            str20 = str17;
                            str19 = str16;
                            str21 = str8;
                            uri2 = uri;
                            str22 = str3;
                            function15 = function14;
                            function16 = function13;
                            th2 = Result.exceptionOrNull-impl(obj6);
                            if (th2 != null) {
                            }
                            str23 = (String) obj6;
                            if (!StringsKt.isBlank(str23)) {
                            }
                        }
                        break;
                    case 1:
                        int i28 = hubCloud$getUrl$12.I$0;
                        HubCloud hubCloud22 = (HubCloud) hubCloud$getUrl$12.L$10;
                        str10 = (String) hubCloud$getUrl$12.L$9;
                        str8 = (String) hubCloud$getUrl$12.L$8;
                        uri = (URI) hubCloud$getUrl$12.L$7;
                        str13 = (String) hubCloud$getUrl$12.L$6;
                        str9 = (String) hubCloud$getUrl$12.L$5;
                        function14 = (Function1) hubCloud$getUrl$12.L$4;
                        function13 = (Function1) hubCloud$getUrl$12.L$3;
                        str11 = (String) hubCloud$getUrl$12.L$2;
                        String str103 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud23 = (HubCloud) hubCloud$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj20);
                            obj3 = obj20;
                            str12 = "/";
                            str5 = "";
                            i2 = i28;
                            hubCloud3 = hubCloud22;
                            obj2 = coroutine_suspended;
                            str14 = str103;
                            hubCloud$getUrl$13 = hubCloud$getUrl$12;
                            hubCloud4 = hubCloud23;
                            str4 = "href";
                            obj5 = obj3;
                            document = ((NiceResponse) obj5).getDocument();
                            selectFirst = document.selectFirst("#download");
                            if (selectFirst != null) {
                            }
                            it = document.select("a[href]").iterator();
                            while (it.hasNext()) {
                            }
                            i3 = i2;
                            hubCloud6 = hubCloud4;
                            str31 = str14;
                            hubCloud7 = hubCloud3;
                            i = 2;
                            c = 0;
                            obj8 = null;
                            Element element422 = (Element) obj8;
                            if (element422 != null) {
                            }
                            if (str15 == null) {
                            }
                            if (StringsKt.startsWith(str15, "http", true)) {
                            }
                            obj6 = Result.constructor-impl(str15);
                            hubCloud5 = hubCloud2;
                            str18 = str10;
                            str20 = str17;
                            str19 = str16;
                            str21 = str8;
                            uri2 = uri;
                            str22 = str3;
                            function15 = function14;
                            function16 = function13;
                        } catch (Throwable th15) {
                            th = th15;
                            obj3 = obj20;
                            obj2 = coroutine_suspended;
                            str5 = "";
                            str6 = str13;
                            str3 = str103;
                            hubCloud2 = hubCloud23;
                            hubCloud$getUrl$13 = hubCloud$getUrl$12;
                            str7 = "/";
                            str4 = "href";
                            i = 2;
                            Result.Companion companion42222222 = Result.Companion;
                            obj6 = Result.constructor-impl(ResultKt.createFailure(th));
                            hubCloud5 = hubCloud2;
                            str18 = str10;
                            str19 = str9;
                            str20 = str6;
                            str21 = str8;
                            uri2 = uri;
                            str22 = str3;
                            function15 = function14;
                            function16 = function13;
                            th2 = Result.exceptionOrNull-impl(obj6);
                            if (th2 != null) {
                            }
                            str23 = (String) obj6;
                            if (!StringsKt.isBlank(str23)) {
                            }
                        }
                        th2 = Result.exceptionOrNull-impl(obj6);
                        if (th2 != null) {
                        }
                        str23 = (String) obj6;
                        if (!StringsKt.isBlank(str23)) {
                        }
                        break;
                    case 2:
                        String str104 = (String) hubCloud$getUrl$12.L$10;
                        String str105 = (String) hubCloud$getUrl$12.L$9;
                        str27 = (String) hubCloud$getUrl$12.L$8;
                        uri3 = (URI) hubCloud$getUrl$12.L$7;
                        str28 = (String) hubCloud$getUrl$12.L$6;
                        str29 = (String) hubCloud$getUrl$12.L$5;
                        function17 = (Function1) hubCloud$getUrl$12.L$4;
                        function18 = (Function1) hubCloud$getUrl$12.L$3;
                        str26 = (String) hubCloud$getUrl$12.L$2;
                        String str106 = (String) hubCloud$getUrl$12.L$1;
                        hubCloud5 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        str18 = str105;
                        hubCloud$getUrl$14 = hubCloud$getUrl$12;
                        obj7 = obj20;
                        obj3 = obj7;
                        obj2 = coroutine_suspended;
                        str25 = "/";
                        str4 = "href";
                        str5 = "";
                        str24 = str104;
                        z = true;
                        str22 = str106;
                        Document document62 = ((NiceResponse) obj7).getDocument();
                        Element selectFirst22 = document62.selectFirst("i#size");
                        if (selectFirst22 == null) {
                        }
                        if (text == null) {
                        }
                        Element selectFirst32 = document62.selectFirst("div.card-header");
                        if (selectFirst32 == null) {
                        }
                        if (text2 == null) {
                        }
                        cleanTitle = hubCloud5.cleanTitle(text2);
                        int indexQuality2 = hubCloud5.getIndexQuality(text2);
                        StringBuilder sb22 = new StringBuilder();
                        HubCloud hubCloud112 = hubCloud5;
                        Object obj212 = obj2;
                        if (cleanTitle.length() <= 0) {
                        }
                        if (text.length() <= 0) {
                        }
                        if (z) {
                        }
                        String sb32 = sb22.toString();
                        Iterable select2 = document62.select("a.btn[href]");
                        it2 = select2.iterator();
                        HubCloud$getUrl$1 hubCloud$getUrl$172 = hubCloud$getUrl$14;
                        iterable = select2;
                        hubCloud$getUrl$15 = hubCloud$getUrl$172;
                        str34 = str33;
                        str35 = cleanTitle;
                        str36 = text;
                        str37 = str26;
                        str38 = str28;
                        i4 = 0;
                        i5 = indexQuality2;
                        str39 = str27;
                        function19 = function17;
                        function110 = function18;
                        hubCloud8 = hubCloud112;
                        str40 = str32;
                        str41 = sb32;
                        document2 = document62;
                        obj9 = obj212;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 3:
                        int i29 = hubCloud$getUrl$12.I$2;
                        int i30 = hubCloud$getUrl$12.I$1;
                        int i31 = hubCloud$getUrl$12.I$0;
                        Function1 function124 = (Function1) hubCloud$getUrl$12.L$23;
                        Object obj36 = hubCloud$getUrl$12.L$18;
                        Iterator it4 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable3 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str107 = (String) hubCloud$getUrl$12.L$15;
                        String str108 = (String) hubCloud$getUrl$12.L$14;
                        String str109 = (String) hubCloud$getUrl$12.L$13;
                        String str110 = (String) hubCloud$getUrl$12.L$12;
                        document3 = (Document) hubCloud$getUrl$12.L$11;
                        str42 = (String) hubCloud$getUrl$12.L$10;
                        String str111 = (String) hubCloud$getUrl$12.L$9;
                        str43 = (String) hubCloud$getUrl$12.L$8;
                        URI uri9 = (URI) hubCloud$getUrl$12.L$7;
                        String str112 = (String) hubCloud$getUrl$12.L$6;
                        String str113 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function125 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function126 = (Function1) hubCloud$getUrl$12.L$3;
                        String str114 = (String) hubCloud$getUrl$12.L$2;
                        String str115 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud24 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        iterable = iterable3;
                        str35 = str108;
                        str34 = str109;
                        str36 = str110;
                        uri4 = uri9;
                        str29 = str113;
                        str37 = str114;
                        str44 = "href";
                        it2 = it4;
                        str22 = str115;
                        function124.invoke(obj20);
                        Unit unit12 = Unit.INSTANCE;
                        hubCloud8 = hubCloud24;
                        function111 = function125;
                        function112 = function126;
                        str45 = "";
                        str38 = str112;
                        str46 = "/";
                        obj10 = obj20;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        i4 = i30;
                        str47 = str111;
                        i5 = i31;
                        str48 = str107;
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 4:
                        int i32 = hubCloud$getUrl$12.I$2;
                        int i33 = hubCloud$getUrl$12.I$1;
                        int i34 = hubCloud$getUrl$12.I$0;
                        Function1 function127 = (Function1) hubCloud$getUrl$12.L$23;
                        Object obj37 = hubCloud$getUrl$12.L$18;
                        Iterator it5 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable4 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str116 = (String) hubCloud$getUrl$12.L$15;
                        String str117 = (String) hubCloud$getUrl$12.L$14;
                        String str118 = (String) hubCloud$getUrl$12.L$13;
                        String str119 = (String) hubCloud$getUrl$12.L$12;
                        document3 = (Document) hubCloud$getUrl$12.L$11;
                        str42 = (String) hubCloud$getUrl$12.L$10;
                        String str120 = (String) hubCloud$getUrl$12.L$9;
                        str43 = (String) hubCloud$getUrl$12.L$8;
                        URI uri10 = (URI) hubCloud$getUrl$12.L$7;
                        String str121 = (String) hubCloud$getUrl$12.L$6;
                        String str122 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function128 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function129 = (Function1) hubCloud$getUrl$12.L$3;
                        String str123 = (String) hubCloud$getUrl$12.L$2;
                        String str124 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud25 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        iterable = iterable4;
                        str35 = str117;
                        str34 = str118;
                        str36 = str119;
                        uri4 = uri10;
                        str37 = str123;
                        str44 = "href";
                        it2 = it5;
                        str29 = str122;
                        str22 = str124;
                        function127.invoke(obj20);
                        Unit unit13 = Unit.INSTANCE;
                        hubCloud8 = hubCloud25;
                        function111 = function128;
                        i4 = i33;
                        function112 = function129;
                        str45 = "";
                        i5 = i34;
                        str46 = "/";
                        str47 = str120;
                        str38 = str121;
                        obj10 = obj20;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        str48 = str116;
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 5:
                        int i35 = hubCloud$getUrl$12.I$2;
                        int i36 = hubCloud$getUrl$12.I$1;
                        int i37 = hubCloud$getUrl$12.I$0;
                        String str125 = (String) hubCloud$getUrl$12.L$22;
                        String str126 = (String) hubCloud$getUrl$12.L$21;
                        String str127 = (String) hubCloud$getUrl$12.L$20;
                        element = (Element) hubCloud$getUrl$12.L$19;
                        Object obj38 = hubCloud$getUrl$12.L$18;
                        Iterator it6 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable5 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str128 = (String) hubCloud$getUrl$12.L$15;
                        String str129 = (String) hubCloud$getUrl$12.L$14;
                        String str130 = (String) hubCloud$getUrl$12.L$13;
                        str36 = (String) hubCloud$getUrl$12.L$12;
                        Document document10 = (Document) hubCloud$getUrl$12.L$11;
                        str49 = (String) hubCloud$getUrl$12.L$10;
                        str50 = (String) hubCloud$getUrl$12.L$9;
                        String str131 = (String) hubCloud$getUrl$12.L$8;
                        URI uri11 = (URI) hubCloud$getUrl$12.L$7;
                        String str132 = (String) hubCloud$getUrl$12.L$6;
                        String str133 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function130 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function131 = (Function1) hubCloud$getUrl$12.L$3;
                        String str134 = (String) hubCloud$getUrl$12.L$2;
                        String str135 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud26 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        obj11 = coroutine_suspended;
                        it2 = it6;
                        str41 = str128;
                        str51 = str127;
                        str43 = str131;
                        uri4 = uri11;
                        str25 = "/";
                        i6 = i37;
                        str52 = str125;
                        iterable = iterable5;
                        str35 = str129;
                        document2 = document10;
                        str22 = str135;
                        function110 = function131;
                        i4 = i36;
                        obj12 = obj20;
                        str5 = "";
                        str53 = str132;
                        hubCloud8 = hubCloud26;
                        obj13 = obj12;
                        str44 = "href";
                        str54 = str133;
                        hubCloud$getUrl$16 = hubCloud$getUrl$12;
                        str55 = str126;
                        obj14 = obj38;
                        i7 = i35;
                        str34 = str130;
                        str37 = str134;
                        function111 = function130;
                        NiceResponse niceResponse2 = (NiceResponse) obj13;
                        Object obj262 = obj14;
                        str74 = niceResponse2.getHeaders().get("hx-redirect");
                        if (str74 != null) {
                        }
                        if (StringsKt.isBlank(str75)) {
                        }
                        break;
                    case 6:
                        int i38 = hubCloud$getUrl$12.I$2;
                        int i39 = hubCloud$getUrl$12.I$1;
                        int i40 = hubCloud$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function132 = (Function1) hubCloud$getUrl$12.L$23;
                        String str136 = (String) hubCloud$getUrl$12.L$22;
                        String str137 = (String) hubCloud$getUrl$12.L$21;
                        String str138 = (String) hubCloud$getUrl$12.L$20;
                        Element element5 = (Element) hubCloud$getUrl$12.L$19;
                        obj15 = hubCloud$getUrl$12.L$18;
                        Iterator it7 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable6 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str139 = (String) hubCloud$getUrl$12.L$15;
                        String str140 = (String) hubCloud$getUrl$12.L$14;
                        String str141 = (String) hubCloud$getUrl$12.L$13;
                        String str142 = (String) hubCloud$getUrl$12.L$12;
                        Document document11 = (Document) hubCloud$getUrl$12.L$11;
                        String str143 = (String) hubCloud$getUrl$12.L$10;
                        String str144 = (String) hubCloud$getUrl$12.L$9;
                        String str145 = (String) hubCloud$getUrl$12.L$8;
                        URI uri12 = (URI) hubCloud$getUrl$12.L$7;
                        String str146 = (String) hubCloud$getUrl$12.L$6;
                        str56 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function133 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function134 = (Function1) hubCloud$getUrl$12.L$3;
                        String str147 = (String) hubCloud$getUrl$12.L$2;
                        String str148 = (String) hubCloud$getUrl$12.L$1;
                        hubCloud9 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        str25 = "/";
                        i6 = i40;
                        str57 = str138;
                        iterable = iterable6;
                        str41 = str139;
                        str35 = str140;
                        str36 = str142;
                        str50 = str144;
                        str37 = str147;
                        function110 = function134;
                        i4 = i39;
                        obj12 = obj20;
                        str44 = "href";
                        str5 = "";
                        function113 = function132;
                        i7 = i38;
                        str34 = str141;
                        str49 = str143;
                        str43 = str145;
                        str53 = str146;
                        obj11 = coroutine_suspended;
                        str58 = str136;
                        element = element5;
                        it2 = it7;
                        document2 = document11;
                        uri4 = uri12;
                        str22 = str148;
                        str59 = str137;
                        function111 = function133;
                        function113.invoke(obj20);
                        Object obj272 = obj12;
                        int i152 = i4;
                        Unit unit32 = Unit.INSTANCE;
                        hubCloud8 = hubCloud9;
                        str29 = str56;
                        obj10 = obj272;
                        String str912 = str49;
                        document3 = document2;
                        str47 = str50;
                        str42 = str912;
                        str38 = str53;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        i4 = i152;
                        obj9 = obj11;
                        str45 = str5;
                        str46 = str25;
                        i5 = i6;
                        function112 = function110;
                        str48 = str41;
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 7:
                        int i41 = hubCloud$getUrl$12.I$2;
                        int i42 = hubCloud$getUrl$12.I$1;
                        int i43 = hubCloud$getUrl$12.I$0;
                        Function1 function135 = (Function1) hubCloud$getUrl$12.L$23;
                        Object obj39 = hubCloud$getUrl$12.L$18;
                        Iterator it8 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable7 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str149 = (String) hubCloud$getUrl$12.L$15;
                        String str150 = (String) hubCloud$getUrl$12.L$14;
                        String str151 = (String) hubCloud$getUrl$12.L$13;
                        String str152 = (String) hubCloud$getUrl$12.L$12;
                        Document document12 = (Document) hubCloud$getUrl$12.L$11;
                        String str153 = (String) hubCloud$getUrl$12.L$10;
                        String str154 = (String) hubCloud$getUrl$12.L$9;
                        String str155 = (String) hubCloud$getUrl$12.L$8;
                        URI uri13 = (URI) hubCloud$getUrl$12.L$7;
                        String str156 = (String) hubCloud$getUrl$12.L$6;
                        String str157 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function136 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function137 = (Function1) hubCloud$getUrl$12.L$3;
                        String str158 = (String) hubCloud$getUrl$12.L$2;
                        String str159 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud27 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        str29 = str157;
                        function112 = function137;
                        str45 = "";
                        iterable = iterable7;
                        str35 = str150;
                        str43 = str155;
                        str37 = str158;
                        str44 = "href";
                        function111 = function136;
                        str46 = "/";
                        it2 = it8;
                        uri4 = uri13;
                        str22 = str159;
                        function135.invoke(obj20);
                        Unit unit14 = Unit.INSTANCE;
                        hubCloud8 = hubCloud27;
                        i4 = i42;
                        i5 = i43;
                        str34 = str151;
                        str36 = str152;
                        str42 = str153;
                        document3 = document12;
                        str38 = str156;
                        str47 = str154;
                        str48 = str149;
                        obj10 = obj20;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 8:
                        int i44 = hubCloud$getUrl$12.I$2;
                        int i45 = hubCloud$getUrl$12.I$1;
                        int i46 = hubCloud$getUrl$12.I$0;
                        Function1 function138 = (Function1) hubCloud$getUrl$12.L$23;
                        Object obj40 = hubCloud$getUrl$12.L$18;
                        Iterator it9 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable8 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str160 = (String) hubCloud$getUrl$12.L$15;
                        String str161 = (String) hubCloud$getUrl$12.L$14;
                        String str162 = (String) hubCloud$getUrl$12.L$13;
                        String str163 = (String) hubCloud$getUrl$12.L$12;
                        document3 = (Document) hubCloud$getUrl$12.L$11;
                        str42 = (String) hubCloud$getUrl$12.L$10;
                        String str164 = (String) hubCloud$getUrl$12.L$9;
                        str43 = (String) hubCloud$getUrl$12.L$8;
                        URI uri14 = (URI) hubCloud$getUrl$12.L$7;
                        String str165 = (String) hubCloud$getUrl$12.L$6;
                        String str166 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function139 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function140 = (Function1) hubCloud$getUrl$12.L$3;
                        String str167 = (String) hubCloud$getUrl$12.L$2;
                        String str168 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud28 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        iterable = iterable8;
                        str35 = str161;
                        str34 = str162;
                        str36 = str163;
                        uri4 = uri14;
                        str29 = str166;
                        str37 = str167;
                        str44 = "href";
                        it2 = it9;
                        str22 = str168;
                        function138.invoke(obj20);
                        Unit unit15 = Unit.INSTANCE;
                        i5 = i46;
                        i4 = i45;
                        str48 = str160;
                        str47 = str164;
                        hubCloud8 = hubCloud28;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        str45 = "";
                        str38 = str165;
                        obj10 = obj20;
                        function111 = function139;
                        function112 = function140;
                        str46 = "/";
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 9:
                        int i47 = hubCloud$getUrl$12.I$2;
                        int i48 = hubCloud$getUrl$12.I$1;
                        int i49 = hubCloud$getUrl$12.I$0;
                        Function1 function141 = (Function1) hubCloud$getUrl$12.L$23;
                        Object obj41 = hubCloud$getUrl$12.L$18;
                        Iterator it10 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable9 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str169 = (String) hubCloud$getUrl$12.L$15;
                        String str170 = (String) hubCloud$getUrl$12.L$14;
                        String str171 = (String) hubCloud$getUrl$12.L$13;
                        String str172 = (String) hubCloud$getUrl$12.L$12;
                        document3 = (Document) hubCloud$getUrl$12.L$11;
                        str42 = (String) hubCloud$getUrl$12.L$10;
                        String str173 = (String) hubCloud$getUrl$12.L$9;
                        str43 = (String) hubCloud$getUrl$12.L$8;
                        URI uri15 = (URI) hubCloud$getUrl$12.L$7;
                        String str174 = (String) hubCloud$getUrl$12.L$6;
                        String str175 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function142 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function143 = (Function1) hubCloud$getUrl$12.L$3;
                        String str176 = (String) hubCloud$getUrl$12.L$2;
                        String str177 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud29 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        iterable = iterable9;
                        str35 = str170;
                        str34 = str171;
                        str36 = str172;
                        uri4 = uri15;
                        str29 = str175;
                        str37 = str176;
                        str44 = "href";
                        it2 = it10;
                        str22 = str177;
                        function141.invoke(obj20);
                        Unit unit16 = Unit.INSTANCE;
                        i5 = i49;
                        i4 = i48;
                        str48 = str169;
                        str47 = str173;
                        hubCloud8 = hubCloud29;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        str45 = "";
                        str38 = str174;
                        obj10 = obj20;
                        function111 = function142;
                        function112 = function143;
                        str46 = "/";
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 10:
                        int i50 = hubCloud$getUrl$12.I$2;
                        int i51 = hubCloud$getUrl$12.I$1;
                        int i52 = hubCloud$getUrl$12.I$0;
                        Function1 function144 = (Function1) hubCloud$getUrl$12.L$23;
                        Object obj42 = hubCloud$getUrl$12.L$18;
                        Iterator it11 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable10 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str178 = (String) hubCloud$getUrl$12.L$15;
                        String str179 = (String) hubCloud$getUrl$12.L$14;
                        String str180 = (String) hubCloud$getUrl$12.L$13;
                        String str181 = (String) hubCloud$getUrl$12.L$12;
                        document3 = (Document) hubCloud$getUrl$12.L$11;
                        str42 = (String) hubCloud$getUrl$12.L$10;
                        String str182 = (String) hubCloud$getUrl$12.L$9;
                        str43 = (String) hubCloud$getUrl$12.L$8;
                        URI uri16 = (URI) hubCloud$getUrl$12.L$7;
                        String str183 = (String) hubCloud$getUrl$12.L$6;
                        String str184 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function145 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function146 = (Function1) hubCloud$getUrl$12.L$3;
                        String str185 = (String) hubCloud$getUrl$12.L$2;
                        String str186 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud30 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        iterable = iterable10;
                        str35 = str179;
                        str34 = str180;
                        str36 = str181;
                        uri4 = uri16;
                        str29 = str184;
                        str37 = str185;
                        str44 = "href";
                        it2 = it11;
                        str22 = str186;
                        function144.invoke(obj20);
                        Unit unit17 = Unit.INSTANCE;
                        i5 = i52;
                        i4 = i51;
                        str48 = str178;
                        str47 = str182;
                        hubCloud8 = hubCloud30;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        str45 = "";
                        str38 = str183;
                        obj10 = obj20;
                        function111 = function145;
                        function112 = function146;
                        str46 = "/";
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 11:
                        i8 = hubCloud$getUrl$12.I$2;
                        i9 = hubCloud$getUrl$12.I$1;
                        i10 = hubCloud$getUrl$12.I$0;
                        str60 = (String) hubCloud$getUrl$12.L$22;
                        str61 = (String) hubCloud$getUrl$12.L$21;
                        str62 = (String) hubCloud$getUrl$12.L$20;
                        element2 = (Element) hubCloud$getUrl$12.L$19;
                        obj16 = hubCloud$getUrl$12.L$18;
                        it3 = (Iterator) hubCloud$getUrl$12.L$17;
                        iterable2 = (Iterable) hubCloud$getUrl$12.L$16;
                        str48 = (String) hubCloud$getUrl$12.L$15;
                        str63 = (String) hubCloud$getUrl$12.L$14;
                        str64 = (String) hubCloud$getUrl$12.L$13;
                        str36 = (String) hubCloud$getUrl$12.L$12;
                        Document document13 = (Document) hubCloud$getUrl$12.L$11;
                        str65 = (String) hubCloud$getUrl$12.L$10;
                        str66 = (String) hubCloud$getUrl$12.L$9;
                        str67 = (String) hubCloud$getUrl$12.L$8;
                        uri5 = (URI) hubCloud$getUrl$12.L$7;
                        String str187 = (String) hubCloud$getUrl$12.L$6;
                        str68 = (String) hubCloud$getUrl$12.L$5;
                        function114 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function147 = (Function1) hubCloud$getUrl$12.L$3;
                        str69 = (String) hubCloud$getUrl$12.L$2;
                        str70 = (String) hubCloud$getUrl$12.L$1;
                        hubCloud10 = (HubCloud) hubCloud$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj20);
                            str25 = "/";
                            document5 = document13;
                            str73 = str187;
                            str44 = "href";
                            str5 = "";
                            function116 = function147;
                        } catch (Exception e2) {
                            e = e2;
                            str44 = "href";
                            str71 = str187;
                            str25 = "/";
                            document4 = document13;
                            function115 = function147;
                            str5 = "";
                            str72 = str68;
                            HubCloud hubCloud31 = hubCloud10;
                            HubCloud$getUrl$1 hubCloud$getUrl$111 = hubCloud$getUrl$12;
                            Log.INSTANCE.e(str72, "Gofile error: " + e.getMessage());
                            int i53 = i10;
                            Document document14 = document4;
                            String str188 = str71;
                            String str189 = str72;
                            Unit unit18 = Unit.INSTANCE;
                            Object obj43 = obj20;
                            hubCloud8 = hubCloud31;
                            i5 = i53;
                            str29 = str189;
                            obj10 = obj43;
                            hubCloud$getUrl$15 = hubCloud$getUrl$111;
                            obj9 = coroutine_suspended;
                            it2 = it3;
                            str34 = str64;
                            str37 = str69;
                            URI uri17 = uri5;
                            str43 = str67;
                            uri4 = uri17;
                            str38 = str188;
                            i4 = i9;
                            str35 = str63;
                            str45 = str5;
                            str47 = str66;
                            function111 = function114;
                            str46 = str25;
                            str42 = str65;
                            document3 = document14;
                            function112 = function115;
                            iterable = iterable2;
                            str22 = str70;
                            function110 = function112;
                            str25 = str46;
                            function19 = function111;
                            str41 = str48;
                            str18 = str47;
                            document2 = document3;
                            str40 = str42;
                            uri3 = uri4;
                            str39 = str43;
                            str4 = str44;
                            obj3 = obj10;
                            str5 = str45;
                            if (it2.hasNext()) {
                            }
                        }
                        HubCloud hubCloud182 = hubCloud10;
                        Document document72 = document5;
                        int i232 = i10;
                        Function1<? super SubtitleFile, Unit> function1232 = function116;
                        Unit unit82 = Unit.INSTANCE;
                        Object obj322 = obj20;
                        hubCloud8 = hubCloud182;
                        i5 = i232;
                        str29 = str68;
                        obj10 = obj322;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        it2 = it3;
                        str34 = str64;
                        str37 = str69;
                        URI uri82 = uri5;
                        str43 = str67;
                        uri4 = uri82;
                        str38 = str73;
                        i4 = i9;
                        str35 = str63;
                        str45 = str5;
                        str47 = str66;
                        function111 = function114;
                        str46 = str25;
                        str42 = str65;
                        document3 = document72;
                        function112 = function1232;
                        iterable = iterable2;
                        str22 = str70;
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 12:
                        int i54 = hubCloud$getUrl$12.I$2;
                        int i55 = hubCloud$getUrl$12.I$1;
                        int i56 = hubCloud$getUrl$12.I$0;
                        Function1 function148 = (Function1) hubCloud$getUrl$12.L$23;
                        Object obj44 = hubCloud$getUrl$12.L$18;
                        Iterator it12 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable11 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str190 = (String) hubCloud$getUrl$12.L$15;
                        String str191 = (String) hubCloud$getUrl$12.L$14;
                        String str192 = (String) hubCloud$getUrl$12.L$13;
                        String str193 = (String) hubCloud$getUrl$12.L$12;
                        Document document15 = (Document) hubCloud$getUrl$12.L$11;
                        String str194 = (String) hubCloud$getUrl$12.L$10;
                        String str195 = (String) hubCloud$getUrl$12.L$9;
                        String str196 = (String) hubCloud$getUrl$12.L$8;
                        URI uri18 = (URI) hubCloud$getUrl$12.L$7;
                        String str197 = (String) hubCloud$getUrl$12.L$6;
                        String str198 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function149 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function150 = (Function1) hubCloud$getUrl$12.L$3;
                        String str199 = (String) hubCloud$getUrl$12.L$2;
                        String str200 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud32 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        function112 = function150;
                        iterable = iterable11;
                        str35 = str191;
                        str34 = str192;
                        uri4 = uri18;
                        str37 = str199;
                        str29 = str198;
                        str22 = str200;
                        it2 = it12;
                        str43 = str196;
                        str44 = "href";
                        function111 = function149;
                        function148.invoke(obj20);
                        Unit unit19 = Unit.INSTANCE;
                        hubCloud8 = hubCloud32;
                        str45 = "";
                        i4 = i55;
                        i5 = i56;
                        str36 = str193;
                        document3 = document15;
                        str42 = str194;
                        str46 = "/";
                        str47 = str195;
                        str48 = str190;
                        obj10 = obj20;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        str38 = str197;
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    case 13:
                        int i57 = hubCloud$getUrl$12.I$2;
                        int i58 = hubCloud$getUrl$12.I$1;
                        int i59 = hubCloud$getUrl$12.I$0;
                        Object obj45 = hubCloud$getUrl$12.L$18;
                        Iterator it13 = (Iterator) hubCloud$getUrl$12.L$17;
                        Iterable iterable12 = (Iterable) hubCloud$getUrl$12.L$16;
                        String str201 = (String) hubCloud$getUrl$12.L$15;
                        String str202 = (String) hubCloud$getUrl$12.L$14;
                        String str203 = (String) hubCloud$getUrl$12.L$13;
                        String str204 = (String) hubCloud$getUrl$12.L$12;
                        document3 = (Document) hubCloud$getUrl$12.L$11;
                        str42 = (String) hubCloud$getUrl$12.L$10;
                        String str205 = (String) hubCloud$getUrl$12.L$9;
                        str43 = (String) hubCloud$getUrl$12.L$8;
                        URI uri19 = (URI) hubCloud$getUrl$12.L$7;
                        String str206 = (String) hubCloud$getUrl$12.L$6;
                        String str207 = (String) hubCloud$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function151 = (Function1) hubCloud$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function152 = (Function1) hubCloud$getUrl$12.L$3;
                        String str208 = (String) hubCloud$getUrl$12.L$2;
                        String str209 = (String) hubCloud$getUrl$12.L$1;
                        HubCloud hubCloud33 = (HubCloud) hubCloud$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj20);
                        function112 = function152;
                        hubCloud$getUrl$15 = hubCloud$getUrl$12;
                        obj9 = coroutine_suspended;
                        str45 = "";
                        it2 = it13;
                        iterable = iterable12;
                        str48 = str201;
                        str35 = str202;
                        str47 = str205;
                        uri4 = uri19;
                        str38 = str206;
                        str22 = str209;
                        str37 = str208;
                        function111 = function151;
                        obj10 = obj20;
                        str44 = "href";
                        hubCloud8 = hubCloud33;
                        str46 = "/";
                        i4 = i58;
                        i5 = i59;
                        str34 = str203;
                        str36 = str204;
                        str29 = str207;
                        function110 = function112;
                        str25 = str46;
                        function19 = function111;
                        str41 = str48;
                        str18 = str47;
                        document2 = document3;
                        str40 = str42;
                        uri3 = uri4;
                        str39 = str43;
                        str4 = str44;
                        obj3 = obj10;
                        str5 = str45;
                        if (it2.hasNext()) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        hubCloud$getUrl$1 = new HubCloud$getUrl$1(hubCloud, continuation);
        hubCloud$getUrl$12 = hubCloud$getUrl$1;
        Object obj202 = hubCloud$getUrl$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (hubCloud$getUrl$12.label) {
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
