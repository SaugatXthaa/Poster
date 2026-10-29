package com.cncverse;

import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorApi;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.functions.Function1;
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
@Metadata(d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JH\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00052\b\u0010\u0011\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u000f0\u00132\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@¢\u0006\u0002\u0010\u0017R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u0018"}, d2 = {"Lcom/cncverse/Filesdl;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nExtractors.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Extractors.kt\ncom/cncverse/Filesdl\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,995:1\n1586#2:996\n1661#2,3:997\n*S KotlinDebug\n*F\n+ 1 Extractors.kt\ncom/cncverse/Filesdl\n*L\n42#1:996\n42#1:997,3\n*E\n"})
/* loaded from: classes.dex */
public final class Filesdl extends ExtractorApi {

    @NotNull
    private final String name = "Filesdl";

    @NotNull
    private final String mainUrl = "https://new6.filesdl.site";
    private final boolean requiresReferer = true;

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

    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0027. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0034  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x0426  */
    /* JADX WARN: Removed duplicated region for block: B:50:0x0867  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x08f0 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:54:0x08f1  */
    /* JADX WARN: Removed duplicated region for block: B:56:0x086a  */
    /* JADX WARN: Removed duplicated region for block: B:65:0x093d  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x00c1  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x0161  */
    /* JADX WARN: Removed duplicated region for block: B:70:0x01e5  */
    /* JADX WARN: Removed duplicated region for block: B:72:0x0262  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x02dd  */
    /* JADX WARN: Removed duplicated region for block: B:76:0x0359  */
    /* JADX WARN: Removed duplicated region for block: B:83:0x0377  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002a  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:62:0x074c -> B:12:0x0766). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:63:0x078d -> B:13:0x0921). Please report as a decompilation issue!!! */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getUrl(@NotNull String str, @Nullable String str2, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Unit> continuation) {
        Filesdl$getUrl$1 filesdl$getUrl$1;
        Filesdl filesdl;
        Object obj;
        Filesdl$getUrl$1 filesdl$getUrl$12;
        Object obj2;
        int i;
        Object obj3;
        String str3;
        String str4;
        Function1<? super SubtitleFile, Unit> function13;
        Object obj4;
        Function1<? super ExtractorLink, Unit> function14;
        Document document;
        String text;
        Regex regex;
        MatchResult find$default;
        String str5;
        Iterable select;
        Iterable iterable;
        Filesdl filesdl2;
        int i2;
        Collection arrayList;
        int i3;
        Iterator it;
        Object obj5;
        Function1<? super ExtractorLink, Unit> function15;
        Function1<? super SubtitleFile, Unit> function16;
        Filesdl$getUrl$1 filesdl$getUrl$13;
        Object obj6;
        Continuation<? super Unit> continuation2;
        Document document2;
        String str6;
        Collection collection;
        Iterator it2;
        String str7;
        Filesdl filesdl3;
        String str8;
        Collection collection2;
        Continuation<? super Unit> continuation3;
        Iterable iterable2;
        Function1<? super SubtitleFile, Unit> function17;
        Object obj7;
        int i4;
        Collection collection3;
        Continuation<? super Unit> continuation4;
        String str9;
        String str10;
        String str11;
        Element element;
        Object obj8;
        Function1<? super ExtractorLink, Unit> function18;
        Filesdl$getUrl$1 filesdl$getUrl$14;
        int i5;
        Collection collection4;
        Iterator it3;
        Object obj9;
        Object obj10;
        int i6;
        int i7;
        Function1<? super SubtitleFile, Unit> function19;
        Object obj11;
        Object newExtractorLink;
        Filesdl$getUrl$1 filesdl$getUrl$15;
        String str12;
        Element element2;
        int i8;
        Collection collection5;
        Iterator it4;
        int i9;
        String str13;
        Function1<? super ExtractorLink, Unit> function110;
        Object obj12;
        Document document3;
        Object obj13;
        Collection collection6;
        int i10;
        String str14;
        Continuation<? super Unit> continuation5;
        Object obj14;
        if (continuation instanceof Filesdl$getUrl$1) {
            filesdl$getUrl$1 = (Filesdl$getUrl$1) continuation;
            if ((filesdl$getUrl$1.label & Integer.MIN_VALUE) != 0) {
                filesdl$getUrl$1.label -= Integer.MIN_VALUE;
                filesdl = this;
                Object obj15 = filesdl$getUrl$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (filesdl$getUrl$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj15);
                        Requests app = MainActivityKt.getApp();
                        filesdl$getUrl$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        filesdl$getUrl$1.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                        filesdl$getUrl$1.L$2 = function1;
                        filesdl$getUrl$1.L$3 = function12;
                        filesdl$getUrl$1.label = 1;
                        obj = obj15;
                        filesdl$getUrl$12 = filesdl$getUrl$1;
                        obj2 = coroutine_suspended;
                        i = 2;
                        obj3 = null;
                        Object obj16 = Requests.get$default(app, str, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, filesdl$getUrl$12, 4094, (Object) null);
                        if (obj16 == obj2) {
                            return obj2;
                        }
                        str3 = str;
                        str4 = str2;
                        function13 = function1;
                        obj4 = obj16;
                        function14 = function12;
                        document = ((NiceResponse) obj4).getDocument();
                        text = document.select("div.title").text();
                        regex = new Regex("(\\d{3,4}p)", RegexOption.IGNORE_CASE);
                        find$default = Regex.find$default(regex, text, 0, i, obj3);
                        if (find$default != null || (str5 = find$default.getValue()) == null) {
                            str5 = "Unknown";
                        }
                        select = document.select("div.container a");
                        iterable = select;
                        filesdl2 = this;
                        i2 = 0;
                        arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(select, 10));
                        i3 = 0;
                        it = iterable.iterator();
                        obj5 = obj2;
                        function15 = function14;
                        function16 = function13;
                        filesdl$getUrl$13 = filesdl$getUrl$12;
                        obj6 = obj;
                        continuation2 = continuation;
                        if (!it.hasNext()) {
                            Object next = it.next();
                            Continuation<? super Unit> continuation6 = continuation2;
                            Element element3 = (Element) next;
                            Document document4 = document;
                            i7 = 0;
                            str11 = element3.text();
                            Object obj17 = obj6;
                            String attr = element3.attr("href");
                            str6 = str4;
                            if (StringsKt.contains(str11, "Hubcloud", true)) {
                                HubCloud hubCloud = new HubCloud();
                                filesdl$getUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                                filesdl$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str6);
                                filesdl$getUrl$13.L$2 = function16;
                                filesdl$getUrl$13.L$3 = function15;
                                filesdl$getUrl$13.L$4 = SpillingKt.nullOutSpilledVariable(document4);
                                filesdl$getUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(text);
                                filesdl$getUrl$13.L$6 = SpillingKt.nullOutSpilledVariable(regex);
                                filesdl$getUrl$13.L$7 = str5;
                                filesdl$getUrl$13.L$8 = SpillingKt.nullOutSpilledVariable(select);
                                filesdl$getUrl$13.L$9 = SpillingKt.nullOutSpilledVariable(iterable);
                                filesdl$getUrl$13.L$10 = arrayList;
                                Iterator it5 = it;
                                filesdl$getUrl$13.L$11 = it5;
                                filesdl$getUrl$13.L$12 = SpillingKt.nullOutSpilledVariable(next);
                                filesdl$getUrl$13.L$13 = SpillingKt.nullOutSpilledVariable(element3);
                                filesdl$getUrl$13.L$14 = SpillingKt.nullOutSpilledVariable(str11);
                                filesdl$getUrl$13.L$15 = SpillingKt.nullOutSpilledVariable(attr);
                                filesdl$getUrl$13.L$16 = arrayList;
                                filesdl$getUrl$13.L$17 = null;
                                filesdl$getUrl$13.L$18 = null;
                                filesdl$getUrl$13.L$19 = null;
                                int i11 = i2;
                                filesdl$getUrl$13.I$0 = i11;
                                filesdl$getUrl$13.I$1 = i3;
                                filesdl$getUrl$13.I$2 = 0;
                                filesdl$getUrl$13.label = 2;
                                Filesdl$getUrl$1 filesdl$getUrl$16 = filesdl$getUrl$13;
                                Function1<? super SubtitleFile, Unit> function111 = function16;
                                if (hubCloud.getUrl(attr, "Filmyfiy", function111, function15, filesdl$getUrl$16) == obj5) {
                                    return obj5;
                                }
                                Object obj18 = obj5;
                                Iterable iterable3 = iterable;
                                Filesdl filesdl4 = filesdl2;
                                document2 = document4;
                                collection = arrayList;
                                it2 = it5;
                                str7 = text;
                                filesdl3 = filesdl4;
                                str8 = str3;
                                collection2 = arrayList;
                                continuation3 = continuation6;
                                i3 = i3;
                                obj6 = obj17;
                                filesdl$getUrl$13 = filesdl$getUrl$16;
                                select = select;
                                iterable2 = iterable3;
                                function17 = function111;
                                obj7 = obj18;
                                i4 = i11;
                            } else {
                                Iterator it6 = it;
                                Filesdl$getUrl$1 filesdl$getUrl$17 = filesdl$getUrl$13;
                                int i12 = i2;
                                String str15 = str3;
                                str7 = text;
                                if (StringsKt.contains(str11, "GDFLIX", true)) {
                                    GDFlix gDFlix = new GDFlix();
                                    filesdl$getUrl$17.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                                    filesdl$getUrl$17.L$1 = SpillingKt.nullOutSpilledVariable(str6);
                                    filesdl$getUrl$17.L$2 = function16;
                                    filesdl$getUrl$17.L$3 = function15;
                                    filesdl$getUrl$17.L$4 = SpillingKt.nullOutSpilledVariable(document4);
                                    filesdl$getUrl$17.L$5 = SpillingKt.nullOutSpilledVariable(str7);
                                    filesdl$getUrl$17.L$6 = SpillingKt.nullOutSpilledVariable(regex);
                                    filesdl$getUrl$17.L$7 = str5;
                                    filesdl$getUrl$17.L$8 = SpillingKt.nullOutSpilledVariable(select);
                                    filesdl$getUrl$17.L$9 = SpillingKt.nullOutSpilledVariable(iterable);
                                    filesdl$getUrl$17.L$10 = arrayList;
                                    filesdl$getUrl$17.L$11 = it6;
                                    filesdl$getUrl$17.L$12 = SpillingKt.nullOutSpilledVariable(next);
                                    filesdl$getUrl$17.L$13 = SpillingKt.nullOutSpilledVariable(element3);
                                    filesdl$getUrl$17.L$14 = SpillingKt.nullOutSpilledVariable(str11);
                                    filesdl$getUrl$17.L$15 = SpillingKt.nullOutSpilledVariable(attr);
                                    filesdl$getUrl$17.L$16 = arrayList;
                                    filesdl$getUrl$17.L$17 = null;
                                    filesdl$getUrl$17.L$18 = null;
                                    filesdl$getUrl$17.L$19 = null;
                                    filesdl$getUrl$17.I$0 = i12;
                                    filesdl$getUrl$17.I$1 = i3;
                                    filesdl$getUrl$17.I$2 = 0;
                                    filesdl$getUrl$17.label = 3;
                                    Function1<? super SubtitleFile, Unit> function112 = function16;
                                    Object url = gDFlix.getUrl(attr, "Filmyfiy", function112, function15, filesdl$getUrl$17);
                                    filesdl$getUrl$13 = filesdl$getUrl$17;
                                    if (url == obj5) {
                                        return obj5;
                                    }
                                    collection2 = arrayList;
                                    Object obj19 = obj5;
                                    Filesdl filesdl5 = filesdl2;
                                    document2 = document4;
                                    it2 = it6;
                                    iterable2 = iterable;
                                    collection = collection2;
                                    continuation3 = continuation6;
                                    i3 = i3;
                                    obj6 = obj17;
                                    filesdl3 = filesdl5;
                                    str8 = str15;
                                    select = select;
                                    function17 = function112;
                                    obj7 = obj19;
                                    i4 = i12;
                                } else if (StringsKt.contains(str11, "Gofile", true)) {
                                    Gofile gofile = new Gofile();
                                    filesdl$getUrl$17.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                                    filesdl$getUrl$17.L$1 = SpillingKt.nullOutSpilledVariable(str6);
                                    filesdl$getUrl$17.L$2 = function16;
                                    filesdl$getUrl$17.L$3 = function15;
                                    filesdl$getUrl$17.L$4 = SpillingKt.nullOutSpilledVariable(document4);
                                    filesdl$getUrl$17.L$5 = SpillingKt.nullOutSpilledVariable(str7);
                                    filesdl$getUrl$17.L$6 = SpillingKt.nullOutSpilledVariable(regex);
                                    filesdl$getUrl$17.L$7 = str5;
                                    filesdl$getUrl$17.L$8 = SpillingKt.nullOutSpilledVariable(select);
                                    filesdl$getUrl$17.L$9 = SpillingKt.nullOutSpilledVariable(iterable);
                                    filesdl$getUrl$17.L$10 = arrayList;
                                    filesdl$getUrl$17.L$11 = it6;
                                    filesdl$getUrl$17.L$12 = SpillingKt.nullOutSpilledVariable(next);
                                    filesdl$getUrl$17.L$13 = SpillingKt.nullOutSpilledVariable(element3);
                                    filesdl$getUrl$17.L$14 = SpillingKt.nullOutSpilledVariable(str11);
                                    filesdl$getUrl$17.L$15 = SpillingKt.nullOutSpilledVariable(attr);
                                    filesdl$getUrl$17.L$16 = arrayList;
                                    filesdl$getUrl$17.L$17 = null;
                                    filesdl$getUrl$17.L$18 = null;
                                    filesdl$getUrl$17.L$19 = null;
                                    filesdl$getUrl$17.I$0 = i12;
                                    filesdl$getUrl$17.I$1 = i3;
                                    filesdl$getUrl$17.I$2 = 0;
                                    filesdl$getUrl$17.label = 4;
                                    Function1<? super SubtitleFile, Unit> function113 = function16;
                                    if (gofile.getUrl(attr, "Filmyfiy", function113, function15, filesdl$getUrl$17) == obj5) {
                                        return obj5;
                                    }
                                    Collection collection7 = arrayList;
                                    Object obj20 = obj5;
                                    Iterable iterable4 = iterable;
                                    Filesdl filesdl6 = filesdl2;
                                    document2 = document4;
                                    collection = collection7;
                                    it2 = it6;
                                    continuation3 = continuation6;
                                    i3 = i3;
                                    obj6 = obj17;
                                    str8 = str15;
                                    select = select;
                                    function17 = function113;
                                    filesdl$getUrl$13 = filesdl$getUrl$17;
                                    collection2 = collection7;
                                    filesdl3 = filesdl6;
                                    iterable2 = iterable4;
                                    obj7 = obj20;
                                    i4 = i12;
                                } else {
                                    filesdl$getUrl$15 = filesdl$getUrl$17;
                                    function19 = function16;
                                    if (StringsKt.contains(str11, "Direct Download", true) || StringsKt.contains(str11, "Ultra FastDL", true)) {
                                        str12 = attr;
                                    } else if (StringsKt.contains(str11, "Fast Cloud-02", true)) {
                                        str12 = attr;
                                    } else if (StringsKt.contains(str11, "Fast Cloud", true)) {
                                        ExtractorLinkType infer_type = ExtractorApiKt.getINFER_TYPE();
                                        Filesdl$getUrl$2$2 filesdl$getUrl$2$2 = new Filesdl$getUrl$2$2(str5, null);
                                        filesdl$getUrl$15.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                                        filesdl$getUrl$15.L$1 = SpillingKt.nullOutSpilledVariable(str6);
                                        filesdl$getUrl$15.L$2 = function19;
                                        filesdl$getUrl$15.L$3 = function15;
                                        filesdl$getUrl$15.L$4 = SpillingKt.nullOutSpilledVariable(document4);
                                        filesdl$getUrl$15.L$5 = SpillingKt.nullOutSpilledVariable(str7);
                                        filesdl$getUrl$15.L$6 = SpillingKt.nullOutSpilledVariable(regex);
                                        filesdl$getUrl$15.L$7 = str5;
                                        filesdl$getUrl$15.L$8 = SpillingKt.nullOutSpilledVariable(select);
                                        filesdl$getUrl$15.L$9 = SpillingKt.nullOutSpilledVariable(iterable);
                                        filesdl$getUrl$15.L$10 = arrayList;
                                        filesdl$getUrl$15.L$11 = it6;
                                        filesdl$getUrl$15.L$12 = SpillingKt.nullOutSpilledVariable(next);
                                        filesdl$getUrl$15.L$13 = SpillingKt.nullOutSpilledVariable(element3);
                                        filesdl$getUrl$15.L$14 = SpillingKt.nullOutSpilledVariable(str11);
                                        filesdl$getUrl$15.L$15 = SpillingKt.nullOutSpilledVariable(attr);
                                        filesdl$getUrl$15.L$16 = function15;
                                        filesdl$getUrl$15.L$17 = arrayList;
                                        filesdl$getUrl$15.L$18 = null;
                                        filesdl$getUrl$15.L$19 = null;
                                        filesdl$getUrl$15.I$0 = i12;
                                        filesdl$getUrl$15.I$1 = i3;
                                        filesdl$getUrl$15.I$2 = 0;
                                        filesdl$getUrl$15.label = 7;
                                        Object newExtractorLink2 = ExtractorApiKt.newExtractorLink("Fast Cloud", "Filmyfiy [Fast Cloud]", attr, infer_type, filesdl$getUrl$2$2, filesdl$getUrl$15);
                                        if (newExtractorLink2 == obj5) {
                                            return obj5;
                                        }
                                        element2 = element3;
                                        i8 = i3;
                                        collection5 = arrayList;
                                        it4 = it6;
                                        i9 = i12;
                                        str13 = attr;
                                        function110 = function15;
                                        obj12 = next;
                                        document3 = document4;
                                        obj13 = newExtractorLink2;
                                        collection6 = collection5;
                                        i10 = 0;
                                        str14 = str15;
                                        continuation5 = continuation6;
                                        obj14 = obj17;
                                        function110.invoke(obj13);
                                        int i13 = i9;
                                        function17 = function19;
                                        filesdl$getUrl$13 = filesdl$getUrl$15;
                                        collection2 = collection5;
                                        i4 = i13;
                                        obj6 = obj14;
                                        Iterator it7 = it4;
                                        obj7 = obj5;
                                        it2 = it7;
                                        continuation3 = continuation5;
                                        i3 = i8;
                                        filesdl3 = filesdl2;
                                        document2 = document3;
                                        iterable2 = iterable;
                                        collection = collection6;
                                        str8 = str14;
                                    } else {
                                        function17 = function19;
                                        i4 = i12;
                                        filesdl$getUrl$13 = filesdl$getUrl$15;
                                        obj7 = obj5;
                                        iterable2 = iterable;
                                        filesdl3 = filesdl2;
                                        str8 = str15;
                                        document2 = document4;
                                        collection2 = arrayList;
                                        collection = collection2;
                                        it2 = it6;
                                        continuation3 = continuation6;
                                        obj6 = obj17;
                                    }
                                    Requests app2 = MainActivityKt.getApp();
                                    filesdl$getUrl$15.L$0 = SpillingKt.nullOutSpilledVariable(str15);
                                    filesdl$getUrl$15.L$1 = SpillingKt.nullOutSpilledVariable(str6);
                                    filesdl$getUrl$15.L$2 = function19;
                                    filesdl$getUrl$15.L$3 = function15;
                                    filesdl$getUrl$15.L$4 = SpillingKt.nullOutSpilledVariable(document4);
                                    filesdl$getUrl$15.L$5 = SpillingKt.nullOutSpilledVariable(str7);
                                    filesdl$getUrl$15.L$6 = SpillingKt.nullOutSpilledVariable(regex);
                                    filesdl$getUrl$15.L$7 = str5;
                                    filesdl$getUrl$15.L$8 = SpillingKt.nullOutSpilledVariable(select);
                                    filesdl$getUrl$15.L$9 = SpillingKt.nullOutSpilledVariable(iterable);
                                    filesdl$getUrl$15.L$10 = arrayList;
                                    filesdl$getUrl$15.L$11 = it6;
                                    filesdl$getUrl$15.L$12 = SpillingKt.nullOutSpilledVariable(next);
                                    filesdl$getUrl$15.L$13 = SpillingKt.nullOutSpilledVariable(element3);
                                    filesdl$getUrl$15.L$14 = SpillingKt.nullOutSpilledVariable(str11);
                                    filesdl$getUrl$15.L$15 = str12;
                                    filesdl$getUrl$15.L$16 = arrayList;
                                    filesdl$getUrl$15.L$17 = null;
                                    filesdl$getUrl$15.L$18 = null;
                                    filesdl$getUrl$15.L$19 = null;
                                    filesdl$getUrl$15.I$0 = i12;
                                    filesdl$getUrl$15.I$1 = i3;
                                    filesdl$getUrl$15.I$2 = 0;
                                    filesdl$getUrl$15.label = 5;
                                    String str16 = str12;
                                    Object obj21 = Requests.get$default(app2, str16, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, filesdl$getUrl$15, 4062, (Object) null);
                                    if (obj21 == obj5) {
                                        return obj5;
                                    }
                                    str9 = str15;
                                    continuation4 = continuation6;
                                    obj9 = obj21;
                                    it3 = it6;
                                    element = element3;
                                    i5 = i3;
                                    collection4 = arrayList;
                                    function18 = function15;
                                    obj10 = obj5;
                                    filesdl3 = filesdl2;
                                    obj8 = next;
                                    document2 = document4;
                                    collection3 = collection4;
                                    i6 = i12;
                                    str10 = str16;
                                    filesdl$getUrl$14 = filesdl$getUrl$15;
                                    obj11 = obj17;
                                    NiceResponse niceResponse = (NiceResponse) obj9;
                                    Object obj22 = obj11;
                                    String str17 = niceResponse.getHeaders().get("location");
                                    String str18 = str17 != null ? str10 : str17;
                                    ExtractorLinkType infer_type2 = ExtractorApiKt.getINFER_TYPE();
                                    Filesdl$getUrl$2$1 filesdl$getUrl$2$1 = new Filesdl$getUrl$2$1(str5, null);
                                    filesdl$getUrl$14.L$0 = SpillingKt.nullOutSpilledVariable(str9);
                                    filesdl$getUrl$14.L$1 = SpillingKt.nullOutSpilledVariable(str6);
                                    filesdl$getUrl$14.L$2 = function19;
                                    filesdl$getUrl$14.L$3 = function18;
                                    filesdl$getUrl$14.L$4 = SpillingKt.nullOutSpilledVariable(document2);
                                    filesdl$getUrl$14.L$5 = SpillingKt.nullOutSpilledVariable(str7);
                                    filesdl$getUrl$14.L$6 = SpillingKt.nullOutSpilledVariable(regex);
                                    filesdl$getUrl$14.L$7 = str5;
                                    filesdl$getUrl$14.L$8 = SpillingKt.nullOutSpilledVariable(select);
                                    filesdl$getUrl$14.L$9 = SpillingKt.nullOutSpilledVariable(iterable);
                                    filesdl$getUrl$14.L$10 = collection3;
                                    filesdl$getUrl$14.L$11 = it3;
                                    filesdl$getUrl$14.L$12 = SpillingKt.nullOutSpilledVariable(obj8);
                                    filesdl$getUrl$14.L$13 = SpillingKt.nullOutSpilledVariable(element);
                                    filesdl$getUrl$14.L$14 = SpillingKt.nullOutSpilledVariable(str11);
                                    filesdl$getUrl$14.L$15 = SpillingKt.nullOutSpilledVariable(str10);
                                    filesdl$getUrl$14.L$16 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                    filesdl$getUrl$14.L$17 = SpillingKt.nullOutSpilledVariable(str18);
                                    filesdl$getUrl$14.L$18 = function18;
                                    filesdl$getUrl$14.L$19 = collection4;
                                    filesdl$getUrl$14.I$0 = i6;
                                    filesdl$getUrl$14.I$1 = i5;
                                    filesdl$getUrl$14.I$2 = i7;
                                    filesdl$getUrl$14.label = 6;
                                    Collection collection8 = collection4;
                                    Filesdl$getUrl$1 filesdl$getUrl$18 = filesdl$getUrl$14;
                                    newExtractorLink = ExtractorApiKt.newExtractorLink("Direct Download", "Filmyfiy [Direct Download]", str18, infer_type2, filesdl$getUrl$2$1, filesdl$getUrl$18);
                                    if (newExtractorLink != obj10) {
                                        return obj10;
                                    }
                                    Function1<? super ExtractorLink, Unit> function114 = function18;
                                    Iterator it8 = it3;
                                    Continuation<? super Unit> continuation7 = continuation4;
                                    String str19 = str9;
                                    Regex regex2 = regex;
                                    obj6 = obj22;
                                    function114.invoke(newExtractorLink);
                                    function17 = function19;
                                    obj7 = obj10;
                                    collection2 = collection8;
                                    iterable2 = iterable;
                                    filesdl$getUrl$13 = filesdl$getUrl$18;
                                    regex = regex2;
                                    i4 = i6;
                                    collection = collection3;
                                    it2 = it8;
                                    function15 = function114;
                                    continuation3 = continuation7;
                                    i3 = i5;
                                    str8 = str19;
                                }
                            }
                            collection2.add(Unit.INSTANCE);
                            continuation2 = continuation3;
                            str3 = str8;
                            it = it2;
                            arrayList = collection;
                            obj5 = obj7;
                            iterable = iterable2;
                            function16 = function17;
                            document = document2;
                            str4 = str6;
                            filesdl2 = filesdl3;
                            text = str7;
                            i2 = i4;
                            if (!it.hasNext()) {
                                return Unit.INSTANCE;
                            }
                        }
                        break;
                    case 1:
                        Function1<? super ExtractorLink, Unit> function115 = (Function1) filesdl$getUrl$1.L$3;
                        function13 = (Function1) filesdl$getUrl$1.L$2;
                        str4 = (String) filesdl$getUrl$1.L$1;
                        str3 = (String) filesdl$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj15);
                        filesdl$getUrl$12 = filesdl$getUrl$1;
                        obj = obj15;
                        obj2 = coroutine_suspended;
                        obj3 = null;
                        function14 = function115;
                        obj4 = obj;
                        i = 2;
                        document = ((NiceResponse) obj4).getDocument();
                        text = document.select("div.title").text();
                        regex = new Regex("(\\d{3,4}p)", RegexOption.IGNORE_CASE);
                        find$default = Regex.find$default(regex, text, 0, i, obj3);
                        if (find$default != null) {
                            break;
                        }
                        str5 = "Unknown";
                        select = document.select("div.container a");
                        iterable = select;
                        filesdl2 = this;
                        i2 = 0;
                        arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(select, 10));
                        i3 = 0;
                        it = iterable.iterator();
                        obj5 = obj2;
                        function15 = function14;
                        function16 = function13;
                        filesdl$getUrl$13 = filesdl$getUrl$12;
                        obj6 = obj;
                        continuation2 = continuation;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 2:
                        int i14 = filesdl$getUrl$1.I$2;
                        int i15 = filesdl$getUrl$1.I$1;
                        int i16 = filesdl$getUrl$1.I$0;
                        Collection collection9 = (Collection) filesdl$getUrl$1.L$16;
                        Object obj23 = filesdl$getUrl$1.L$12;
                        Iterator it9 = (Iterator) filesdl$getUrl$1.L$11;
                        Collection collection10 = (Collection) filesdl$getUrl$1.L$10;
                        Iterable iterable5 = (Iterable) filesdl$getUrl$1.L$9;
                        Iterable iterable6 = (Iterable) filesdl$getUrl$1.L$8;
                        String str20 = (String) filesdl$getUrl$1.L$7;
                        Regex regex3 = (Regex) filesdl$getUrl$1.L$6;
                        String str21 = (String) filesdl$getUrl$1.L$5;
                        document2 = (Document) filesdl$getUrl$1.L$4;
                        Function1<? super ExtractorLink, Unit> function116 = (Function1) filesdl$getUrl$1.L$3;
                        Function1<? super SubtitleFile, Unit> function117 = (Function1) filesdl$getUrl$1.L$2;
                        String str22 = (String) filesdl$getUrl$1.L$1;
                        String str23 = (String) filesdl$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj15);
                        str6 = str22;
                        regex = regex3;
                        collection = collection10;
                        str5 = str20;
                        it2 = it9;
                        function15 = function116;
                        str7 = str21;
                        filesdl3 = filesdl;
                        str8 = str23;
                        collection2 = collection9;
                        continuation3 = continuation;
                        i3 = i15;
                        obj6 = obj15;
                        filesdl$getUrl$13 = filesdl$getUrl$1;
                        select = iterable6;
                        iterable2 = iterable5;
                        function17 = function117;
                        obj7 = coroutine_suspended;
                        i4 = i16;
                        collection2.add(Unit.INSTANCE);
                        continuation2 = continuation3;
                        str3 = str8;
                        it = it2;
                        arrayList = collection;
                        obj5 = obj7;
                        iterable = iterable2;
                        function16 = function17;
                        document = document2;
                        str4 = str6;
                        filesdl2 = filesdl3;
                        text = str7;
                        i2 = i4;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 3:
                        int i17 = filesdl$getUrl$1.I$2;
                        int i18 = filesdl$getUrl$1.I$1;
                        int i19 = filesdl$getUrl$1.I$0;
                        collection2 = (Collection) filesdl$getUrl$1.L$16;
                        Object obj24 = filesdl$getUrl$1.L$12;
                        Iterator it10 = (Iterator) filesdl$getUrl$1.L$11;
                        Collection collection11 = (Collection) filesdl$getUrl$1.L$10;
                        Iterable iterable7 = (Iterable) filesdl$getUrl$1.L$9;
                        Iterable iterable8 = (Iterable) filesdl$getUrl$1.L$8;
                        String str24 = (String) filesdl$getUrl$1.L$7;
                        Regex regex4 = (Regex) filesdl$getUrl$1.L$6;
                        String str25 = (String) filesdl$getUrl$1.L$5;
                        document2 = (Document) filesdl$getUrl$1.L$4;
                        Function1<? super ExtractorLink, Unit> function118 = (Function1) filesdl$getUrl$1.L$3;
                        Function1<? super SubtitleFile, Unit> function119 = (Function1) filesdl$getUrl$1.L$2;
                        String str26 = (String) filesdl$getUrl$1.L$1;
                        String str27 = (String) filesdl$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj15);
                        str6 = str26;
                        Filesdl filesdl7 = filesdl;
                        iterable2 = iterable7;
                        regex = regex4;
                        str7 = str25;
                        filesdl$getUrl$13 = filesdl$getUrl$1;
                        collection = collection11;
                        str5 = str24;
                        it2 = it10;
                        function15 = function118;
                        continuation3 = continuation;
                        i3 = i18;
                        obj6 = obj15;
                        filesdl3 = filesdl7;
                        str8 = str27;
                        select = iterable8;
                        function17 = function119;
                        obj7 = coroutine_suspended;
                        i4 = i19;
                        collection2.add(Unit.INSTANCE);
                        continuation2 = continuation3;
                        str3 = str8;
                        it = it2;
                        arrayList = collection;
                        obj5 = obj7;
                        iterable = iterable2;
                        function16 = function17;
                        document = document2;
                        str4 = str6;
                        filesdl2 = filesdl3;
                        text = str7;
                        i2 = i4;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 4:
                        int i20 = filesdl$getUrl$1.I$2;
                        int i21 = filesdl$getUrl$1.I$1;
                        int i22 = filesdl$getUrl$1.I$0;
                        Collection collection12 = (Collection) filesdl$getUrl$1.L$16;
                        Object obj25 = filesdl$getUrl$1.L$12;
                        Iterator it11 = (Iterator) filesdl$getUrl$1.L$11;
                        Collection collection13 = (Collection) filesdl$getUrl$1.L$10;
                        Iterable iterable9 = (Iterable) filesdl$getUrl$1.L$9;
                        Iterable iterable10 = (Iterable) filesdl$getUrl$1.L$8;
                        String str28 = (String) filesdl$getUrl$1.L$7;
                        Regex regex5 = (Regex) filesdl$getUrl$1.L$6;
                        String str29 = (String) filesdl$getUrl$1.L$5;
                        document2 = (Document) filesdl$getUrl$1.L$4;
                        Function1<? super ExtractorLink, Unit> function120 = (Function1) filesdl$getUrl$1.L$3;
                        Function1<? super SubtitleFile, Unit> function121 = (Function1) filesdl$getUrl$1.L$2;
                        String str30 = (String) filesdl$getUrl$1.L$1;
                        String str31 = (String) filesdl$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj15);
                        str6 = str30;
                        Filesdl filesdl8 = filesdl;
                        regex = regex5;
                        str7 = str29;
                        str5 = str28;
                        collection = collection13;
                        it2 = it11;
                        function15 = function120;
                        continuation3 = continuation;
                        i3 = i21;
                        obj6 = obj15;
                        str8 = str31;
                        select = iterable10;
                        function17 = function121;
                        filesdl$getUrl$13 = filesdl$getUrl$1;
                        collection2 = collection12;
                        filesdl3 = filesdl8;
                        iterable2 = iterable9;
                        obj7 = coroutine_suspended;
                        i4 = i22;
                        collection2.add(Unit.INSTANCE);
                        continuation2 = continuation3;
                        str3 = str8;
                        it = it2;
                        arrayList = collection;
                        obj5 = obj7;
                        iterable = iterable2;
                        function16 = function17;
                        document = document2;
                        str4 = str6;
                        filesdl2 = filesdl3;
                        text = str7;
                        i2 = i4;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 5:
                        int i23 = filesdl$getUrl$1.I$2;
                        int i24 = filesdl$getUrl$1.I$1;
                        int i25 = filesdl$getUrl$1.I$0;
                        Collection collection14 = (Collection) filesdl$getUrl$1.L$16;
                        String str32 = (String) filesdl$getUrl$1.L$15;
                        String str33 = (String) filesdl$getUrl$1.L$14;
                        Element element4 = (Element) filesdl$getUrl$1.L$13;
                        Object obj26 = filesdl$getUrl$1.L$12;
                        Iterator it12 = (Iterator) filesdl$getUrl$1.L$11;
                        collection3 = (Collection) filesdl$getUrl$1.L$10;
                        iterable = (Iterable) filesdl$getUrl$1.L$9;
                        Iterable iterable11 = (Iterable) filesdl$getUrl$1.L$8;
                        String str34 = (String) filesdl$getUrl$1.L$7;
                        Regex regex6 = (Regex) filesdl$getUrl$1.L$6;
                        String str35 = (String) filesdl$getUrl$1.L$5;
                        document2 = (Document) filesdl$getUrl$1.L$4;
                        Function1<? super ExtractorLink, Unit> function122 = (Function1) filesdl$getUrl$1.L$3;
                        Function1<? super SubtitleFile, Unit> function123 = (Function1) filesdl$getUrl$1.L$2;
                        String str36 = (String) filesdl$getUrl$1.L$1;
                        String str37 = (String) filesdl$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj15);
                        str6 = str36;
                        continuation4 = continuation;
                        str9 = str37;
                        filesdl3 = filesdl;
                        str10 = str32;
                        str11 = str33;
                        element = element4;
                        obj8 = obj26;
                        select = iterable11;
                        str5 = str34;
                        regex = regex6;
                        str7 = str35;
                        function18 = function122;
                        filesdl$getUrl$14 = filesdl$getUrl$1;
                        i5 = i24;
                        collection4 = collection14;
                        it3 = it12;
                        obj9 = obj15;
                        obj10 = coroutine_suspended;
                        i6 = i25;
                        i7 = i23;
                        function19 = function123;
                        obj11 = obj9;
                        NiceResponse niceResponse2 = (NiceResponse) obj9;
                        Object obj222 = obj11;
                        String str172 = niceResponse2.getHeaders().get("location");
                        if (str172 != null) {
                        }
                        ExtractorLinkType infer_type22 = ExtractorApiKt.getINFER_TYPE();
                        Filesdl$getUrl$2$1 filesdl$getUrl$2$12 = new Filesdl$getUrl$2$1(str5, null);
                        filesdl$getUrl$14.L$0 = SpillingKt.nullOutSpilledVariable(str9);
                        filesdl$getUrl$14.L$1 = SpillingKt.nullOutSpilledVariable(str6);
                        filesdl$getUrl$14.L$2 = function19;
                        filesdl$getUrl$14.L$3 = function18;
                        filesdl$getUrl$14.L$4 = SpillingKt.nullOutSpilledVariable(document2);
                        filesdl$getUrl$14.L$5 = SpillingKt.nullOutSpilledVariable(str7);
                        filesdl$getUrl$14.L$6 = SpillingKt.nullOutSpilledVariable(regex);
                        filesdl$getUrl$14.L$7 = str5;
                        filesdl$getUrl$14.L$8 = SpillingKt.nullOutSpilledVariable(select);
                        filesdl$getUrl$14.L$9 = SpillingKt.nullOutSpilledVariable(iterable);
                        filesdl$getUrl$14.L$10 = collection3;
                        filesdl$getUrl$14.L$11 = it3;
                        filesdl$getUrl$14.L$12 = SpillingKt.nullOutSpilledVariable(obj8);
                        filesdl$getUrl$14.L$13 = SpillingKt.nullOutSpilledVariable(element);
                        filesdl$getUrl$14.L$14 = SpillingKt.nullOutSpilledVariable(str11);
                        filesdl$getUrl$14.L$15 = SpillingKt.nullOutSpilledVariable(str10);
                        filesdl$getUrl$14.L$16 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                        filesdl$getUrl$14.L$17 = SpillingKt.nullOutSpilledVariable(str18);
                        filesdl$getUrl$14.L$18 = function18;
                        filesdl$getUrl$14.L$19 = collection4;
                        filesdl$getUrl$14.I$0 = i6;
                        filesdl$getUrl$14.I$1 = i5;
                        filesdl$getUrl$14.I$2 = i7;
                        filesdl$getUrl$14.label = 6;
                        Collection collection82 = collection4;
                        Filesdl$getUrl$1 filesdl$getUrl$182 = filesdl$getUrl$14;
                        newExtractorLink = ExtractorApiKt.newExtractorLink("Direct Download", "Filmyfiy [Direct Download]", str18, infer_type22, filesdl$getUrl$2$12, filesdl$getUrl$182);
                        if (newExtractorLink != obj10) {
                        }
                        break;
                    case 6:
                        int i26 = filesdl$getUrl$1.I$2;
                        int i27 = filesdl$getUrl$1.I$1;
                        int i28 = filesdl$getUrl$1.I$0;
                        Collection collection15 = (Collection) filesdl$getUrl$1.L$19;
                        Function1 function124 = (Function1) filesdl$getUrl$1.L$18;
                        Object obj27 = filesdl$getUrl$1.L$12;
                        Iterator it13 = (Iterator) filesdl$getUrl$1.L$11;
                        Collection collection16 = (Collection) filesdl$getUrl$1.L$10;
                        Iterable iterable12 = (Iterable) filesdl$getUrl$1.L$9;
                        Iterable iterable13 = (Iterable) filesdl$getUrl$1.L$8;
                        String str38 = (String) filesdl$getUrl$1.L$7;
                        Regex regex7 = (Regex) filesdl$getUrl$1.L$6;
                        String str39 = (String) filesdl$getUrl$1.L$5;
                        Document document5 = (Document) filesdl$getUrl$1.L$4;
                        Function1<? super ExtractorLink, Unit> function125 = (Function1) filesdl$getUrl$1.L$3;
                        Function1<? super SubtitleFile, Unit> function126 = (Function1) filesdl$getUrl$1.L$2;
                        String str40 = (String) filesdl$getUrl$1.L$1;
                        String str41 = (String) filesdl$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj15);
                        filesdl3 = filesdl;
                        select = iterable13;
                        str5 = str38;
                        str7 = str39;
                        document2 = document5;
                        str6 = str40;
                        Filesdl$getUrl$1 filesdl$getUrl$19 = filesdl$getUrl$1;
                        obj6 = obj15;
                        function124.invoke(obj15);
                        function17 = function126;
                        obj7 = coroutine_suspended;
                        collection2 = collection15;
                        iterable2 = iterable12;
                        filesdl$getUrl$13 = filesdl$getUrl$19;
                        regex = regex7;
                        i4 = i28;
                        collection = collection16;
                        it2 = it13;
                        function15 = function125;
                        continuation3 = continuation;
                        i3 = i27;
                        str8 = str41;
                        collection2.add(Unit.INSTANCE);
                        continuation2 = continuation3;
                        str3 = str8;
                        it = it2;
                        arrayList = collection;
                        obj5 = obj7;
                        iterable = iterable2;
                        function16 = function17;
                        document = document2;
                        str4 = str6;
                        filesdl2 = filesdl3;
                        text = str7;
                        i2 = i4;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 7:
                        i10 = filesdl$getUrl$1.I$2;
                        int i29 = filesdl$getUrl$1.I$1;
                        int i30 = filesdl$getUrl$1.I$0;
                        Collection collection17 = (Collection) filesdl$getUrl$1.L$17;
                        Function1<? super ExtractorLink, Unit> function127 = (Function1) filesdl$getUrl$1.L$16;
                        String str42 = (String) filesdl$getUrl$1.L$15;
                        Element element5 = (Element) filesdl$getUrl$1.L$13;
                        obj12 = filesdl$getUrl$1.L$12;
                        Iterator it14 = (Iterator) filesdl$getUrl$1.L$11;
                        Collection collection18 = (Collection) filesdl$getUrl$1.L$10;
                        Iterable iterable14 = (Iterable) filesdl$getUrl$1.L$9;
                        Iterable iterable15 = (Iterable) filesdl$getUrl$1.L$8;
                        String str43 = (String) filesdl$getUrl$1.L$7;
                        Regex regex8 = (Regex) filesdl$getUrl$1.L$6;
                        String str44 = (String) filesdl$getUrl$1.L$5;
                        document3 = (Document) filesdl$getUrl$1.L$4;
                        Function1<? super ExtractorLink, Unit> function128 = (Function1) filesdl$getUrl$1.L$3;
                        Function1<? super SubtitleFile, Unit> function129 = (Function1) filesdl$getUrl$1.L$2;
                        String str45 = (String) filesdl$getUrl$1.L$1;
                        String str46 = (String) filesdl$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj15);
                        str6 = str45;
                        continuation5 = continuation;
                        i8 = i29;
                        i9 = i30;
                        collection5 = collection17;
                        iterable = iterable14;
                        select = iterable15;
                        str5 = str43;
                        regex = regex8;
                        str7 = str44;
                        filesdl2 = filesdl;
                        obj13 = obj15;
                        str13 = str42;
                        element2 = element5;
                        function19 = function129;
                        obj5 = coroutine_suspended;
                        it4 = it14;
                        obj14 = obj13;
                        collection6 = collection18;
                        filesdl$getUrl$15 = filesdl$getUrl$1;
                        str14 = str46;
                        function110 = function127;
                        function15 = function128;
                        function110.invoke(obj13);
                        int i132 = i9;
                        function17 = function19;
                        filesdl$getUrl$13 = filesdl$getUrl$15;
                        collection2 = collection5;
                        i4 = i132;
                        obj6 = obj14;
                        Iterator it72 = it4;
                        obj7 = obj5;
                        it2 = it72;
                        continuation3 = continuation5;
                        i3 = i8;
                        filesdl3 = filesdl2;
                        document2 = document3;
                        iterable2 = iterable;
                        collection = collection6;
                        str8 = str14;
                        collection2.add(Unit.INSTANCE);
                        continuation2 = continuation3;
                        str3 = str8;
                        it = it2;
                        arrayList = collection;
                        obj5 = obj7;
                        iterable = iterable2;
                        function16 = function17;
                        document = document2;
                        str4 = str6;
                        filesdl2 = filesdl3;
                        text = str7;
                        i2 = i4;
                        if (!it.hasNext()) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        filesdl = this;
        filesdl$getUrl$1 = new Filesdl$getUrl$1(filesdl, continuation);
        Object obj152 = filesdl$getUrl$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (filesdl$getUrl$1.label) {
        }
    }
}
