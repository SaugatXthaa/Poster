package com.cncverse;

import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorApi;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.Iterator;
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
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JH\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00052\b\u0010\u0011\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u000f0\u00132\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@¢\u0006\u0002\u0010\u0017R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u0018"}, d2 = {"Lcom/cncverse/M4ulinks;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nExtractors.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Extractors.kt\ncom/cncverse/M4ulinks\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,995:1\n1915#2,2:996\n*S KotlinDebug\n*F\n+ 1 Extractors.kt\ncom/cncverse/M4ulinks\n*L\n98#1:996,2\n*E\n"})
/* loaded from: classes.dex */
public final class M4ulinks extends ExtractorApi {
    private final boolean requiresReferer;

    @NotNull
    private final String name = "M4ulinks";

    @NotNull
    private final String mainUrl = "https://m4ulinks.com";

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
    /* JADX WARN: Removed duplicated region for block: B:15:0x00f4  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0165  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x006f  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0089  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002a  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:18:0x0147 -> B:12:0x0158). Please report as a decompilation issue!!! */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object getUrl(@NotNull String str, @Nullable String str2, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Unit> continuation) {
        M4ulinks$getUrl$1 m4ulinks$getUrl$1;
        M4ulinks m4ulinks;
        Object obj;
        M4ulinks$getUrl$1 m4ulinks$getUrl$12;
        Object obj2;
        String str3;
        String str4;
        Function1<? super SubtitleFile, Unit> function13;
        Object obj3;
        Function1<? super ExtractorLink, Unit> function14;
        Iterator it;
        Function1<? super ExtractorLink, Unit> function15;
        Document document;
        Function1<? super SubtitleFile, Unit> function16;
        String str5;
        Iterable iterable;
        int i;
        M4ulinks m4ulinks2;
        Continuation<? super Unit> continuation2;
        String str6;
        Function1<? super SubtitleFile, Unit> function17;
        Function1<? super ExtractorLink, Unit> function18;
        Document document2;
        Iterable iterable2;
        Iterator it2;
        String str7;
        if (continuation instanceof M4ulinks$getUrl$1) {
            m4ulinks$getUrl$1 = (M4ulinks$getUrl$1) continuation;
            if ((m4ulinks$getUrl$1.label & Integer.MIN_VALUE) != 0) {
                m4ulinks$getUrl$1.label -= Integer.MIN_VALUE;
                m4ulinks = this;
                Object obj4 = m4ulinks$getUrl$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (m4ulinks$getUrl$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj4);
                        obj = obj4;
                        Requests app = MainActivityKt.getApp();
                        m4ulinks$getUrl$1.L$0 = SpillingKt.nullOutSpilledVariable(str);
                        m4ulinks$getUrl$1.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                        m4ulinks$getUrl$1.L$2 = function1;
                        m4ulinks$getUrl$1.L$3 = function12;
                        m4ulinks$getUrl$1.label = 1;
                        m4ulinks$getUrl$12 = m4ulinks$getUrl$1;
                        obj2 = coroutine_suspended;
                        Object obj5 = Requests.get$default(app, str, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, m4ulinks$getUrl$12, 4094, (Object) null);
                        if (obj5 == obj2) {
                            return obj2;
                        }
                        str3 = str;
                        str4 = str2;
                        function13 = function1;
                        obj3 = obj5;
                        function14 = function12;
                        Document document3 = ((NiceResponse) obj3).getDocument();
                        Iterable select = document3.select("div.downloads-btns-div a");
                        it = select.iterator();
                        function15 = function14;
                        document = document3;
                        function16 = function13;
                        str5 = str4;
                        iterable = select;
                        i = 0;
                        obj4 = obj;
                        coroutine_suspended = obj2;
                        m4ulinks2 = m4ulinks;
                        m4ulinks$getUrl$1 = m4ulinks$getUrl$12;
                        continuation2 = continuation;
                        if (!it.hasNext()) {
                            Object next = it.next();
                            Element element = (Element) next;
                            Continuation<? super Unit> continuation3 = continuation2;
                            String attr = element.attr("href");
                            Object obj6 = obj4;
                            m4ulinks$getUrl$1.L$0 = SpillingKt.nullOutSpilledVariable(str3);
                            m4ulinks$getUrl$1.L$1 = SpillingKt.nullOutSpilledVariable(str5);
                            m4ulinks$getUrl$1.L$2 = function16;
                            m4ulinks$getUrl$1.L$3 = function15;
                            m4ulinks$getUrl$1.L$4 = SpillingKt.nullOutSpilledVariable(document);
                            m4ulinks$getUrl$1.L$5 = SpillingKt.nullOutSpilledVariable(iterable);
                            m4ulinks$getUrl$1.L$6 = it;
                            m4ulinks$getUrl$1.L$7 = SpillingKt.nullOutSpilledVariable(next);
                            m4ulinks$getUrl$1.L$8 = SpillingKt.nullOutSpilledVariable(element);
                            m4ulinks$getUrl$1.L$9 = SpillingKt.nullOutSpilledVariable(attr);
                            m4ulinks$getUrl$1.I$0 = i;
                            m4ulinks$getUrl$1.I$1 = 0;
                            m4ulinks$getUrl$1.label = 2;
                            if (ExtractorApiKt.loadExtractor(attr, "", function16, function15, m4ulinks$getUrl$1) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            obj4 = obj6;
                            str6 = str5;
                            function17 = function16;
                            function18 = function15;
                            document2 = document;
                            iterable2 = iterable;
                            it2 = it;
                            str7 = str3;
                            m4ulinks = m4ulinks2;
                            continuation2 = continuation3;
                            m4ulinks2 = m4ulinks;
                            str3 = str7;
                            it = it2;
                            iterable = iterable2;
                            document = document2;
                            function15 = function18;
                            function16 = function17;
                            str5 = str6;
                            if (!it.hasNext()) {
                                return Unit.INSTANCE;
                            }
                        }
                    case 1:
                        Function1<? super ExtractorLink, Unit> function19 = (Function1) m4ulinks$getUrl$1.L$3;
                        function13 = (Function1) m4ulinks$getUrl$1.L$2;
                        str4 = (String) m4ulinks$getUrl$1.L$1;
                        str3 = (String) m4ulinks$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj4);
                        m4ulinks$getUrl$12 = m4ulinks$getUrl$1;
                        obj2 = coroutine_suspended;
                        obj3 = obj4;
                        function14 = function19;
                        obj = obj3;
                        Document document32 = ((NiceResponse) obj3).getDocument();
                        Iterable select2 = document32.select("div.downloads-btns-div a");
                        it = select2.iterator();
                        function15 = function14;
                        document = document32;
                        function16 = function13;
                        str5 = str4;
                        iterable = select2;
                        i = 0;
                        obj4 = obj;
                        coroutine_suspended = obj2;
                        m4ulinks2 = m4ulinks;
                        m4ulinks$getUrl$1 = m4ulinks$getUrl$12;
                        continuation2 = continuation;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 2:
                        int i2 = m4ulinks$getUrl$1.I$1;
                        i = m4ulinks$getUrl$1.I$0;
                        Object obj7 = m4ulinks$getUrl$1.L$7;
                        Iterator it3 = (Iterator) m4ulinks$getUrl$1.L$6;
                        Iterable iterable3 = (Iterable) m4ulinks$getUrl$1.L$5;
                        Document document4 = (Document) m4ulinks$getUrl$1.L$4;
                        Function1<? super ExtractorLink, Unit> function110 = (Function1) m4ulinks$getUrl$1.L$3;
                        Function1<? super SubtitleFile, Unit> function111 = (Function1) m4ulinks$getUrl$1.L$2;
                        String str8 = (String) m4ulinks$getUrl$1.L$1;
                        String str9 = (String) m4ulinks$getUrl$1.L$0;
                        ResultKt.throwOnFailure(obj4);
                        str6 = str8;
                        function17 = function111;
                        function18 = function110;
                        document2 = document4;
                        iterable2 = iterable3;
                        it2 = it3;
                        str7 = str9;
                        continuation2 = continuation;
                        m4ulinks2 = m4ulinks;
                        str3 = str7;
                        it = it2;
                        iterable = iterable2;
                        document = document2;
                        function15 = function18;
                        function16 = function17;
                        str5 = str6;
                        if (!it.hasNext()) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        m4ulinks = this;
        m4ulinks$getUrl$1 = new M4ulinks$getUrl$1(m4ulinks, continuation);
        Object obj42 = m4ulinks$getUrl$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (m4ulinks$getUrl$1.label) {
        }
    }
}
