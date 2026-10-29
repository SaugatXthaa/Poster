package com.cncverse;

import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;

/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.Filesdl$getUrl$2$2", f = "Extractors.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
/* loaded from: classes.dex */
final class Filesdl$getUrl$2$2 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $quality;
    private /* synthetic */ Object L$0;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public Filesdl$getUrl$2$2(String str, Continuation<? super Filesdl$getUrl$2$2> continuation) {
        super(2, continuation);
        this.$quality = str;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Continuation<Unit> filesdl$getUrl$2$2 = new Filesdl$getUrl$2$2(this.$quality, continuation);
        filesdl$getUrl$2$2.L$0 = obj;
        return filesdl$getUrl$2$2;
    }

    public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
        return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object obj) {
        ExtractorLink extractorLink = (ExtractorLink) this.L$0;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                extractorLink.setQuality(ExtractorApiKt.getQualityFromName(this.$quality));
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
