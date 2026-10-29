package com.cncverse;

import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import java.util.Map;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MovieBoxProvider.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MovieBoxProvider$loadLinks$emitNetStream$2", f = "MovieBoxProvider.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProvider$loadLinks$emitNetStream$2 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
    final /* synthetic */ ExtractorLinkType $linkType;
    final /* synthetic */ Integer $quality;
    final /* synthetic */ String $signCookie;
    final /* synthetic */ String $signHeaderKey;
    final /* synthetic */ String $subDomain;
    final /* synthetic */ String $subPath;
    final /* synthetic */ String $subjectId;
    private /* synthetic */ Object L$0;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProvider$loadLinks$emitNetStream$2(ExtractorLinkType extractorLinkType, Integer num, String str, String str2, String str3, String str4, String str5, Continuation<? super MovieBoxProvider$loadLinks$emitNetStream$2> continuation) {
        super(2, continuation);
        this.$linkType = extractorLinkType;
        this.$quality = num;
        this.$subDomain = str;
        this.$subPath = str2;
        this.$subjectId = str3;
        this.$signCookie = str4;
        this.$signHeaderKey = str5;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Continuation<Unit> movieBoxProvider$loadLinks$emitNetStream$2 = new MovieBoxProvider$loadLinks$emitNetStream$2(this.$linkType, this.$quality, this.$subDomain, this.$subPath, this.$subjectId, this.$signCookie, this.$signHeaderKey, continuation);
        movieBoxProvider$loadLinks$emitNetStream$2.L$0 = obj;
        return movieBoxProvider$loadLinks$emitNetStream$2;
    }

    public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
        return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object obj) {
        Map build;
        ExtractorLink extractorLink = (ExtractorLink) this.L$0;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                if (this.$linkType == ExtractorLinkType.VIDEO) {
                    build = MapsKt.mapOf(new Pair[]{TuplesKt.to("Referer", "https://movie-box.co/"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Safari/537.36")});
                } else {
                    String str = this.$subDomain;
                    String str2 = this.$subPath;
                    String str3 = this.$subjectId;
                    String str4 = this.$signCookie;
                    String str5 = this.$signHeaderKey;
                    Map createMapBuilder = MapsKt.createMapBuilder();
                    createMapBuilder.put("Origin", str);
                    createMapBuilder.put("Referer", str + "/movies/" + str2 + "?id=" + str3 + "&type=/movie/detail&detailSe=&detailEp=&lang=en");
                    createMapBuilder.put("User-Agent", "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Mobile Safari/537.36");
                    if (str4 != null) {
                        createMapBuilder.put(str5, str4);
                    }
                    build = MapsKt.build(createMapBuilder);
                }
                extractorLink.setHeaders(build);
                if (this.$quality != null) {
                    extractorLink.setQuality(this.$quality.intValue());
                }
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
