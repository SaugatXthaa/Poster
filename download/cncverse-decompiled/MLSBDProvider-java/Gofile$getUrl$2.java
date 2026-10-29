package com.cncverse;

import com.lagradost.cloudstream3.utils.ExtractorLink;
import kotlin.Metadata;
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
/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.Gofile$getUrl$2", f = "Extractors.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
/* loaded from: classes.dex */
public final class Gofile$getUrl$2 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $fileName;
    final /* synthetic */ String $token;
    private /* synthetic */ Object L$0;
    int label;
    final /* synthetic */ Gofile this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public Gofile$getUrl$2(Gofile gofile, String str, String str2, Continuation<? super Gofile$getUrl$2> continuation) {
        super(2, continuation);
        this.this$0 = gofile;
        this.$fileName = str;
        this.$token = str2;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Continuation<Unit> gofile$getUrl$2 = new Gofile$getUrl$2(this.this$0, this.$fileName, this.$token, continuation);
        gofile$getUrl$2.L$0 = obj;
        return gofile$getUrl$2;
    }

    public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
        return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object obj) {
        int quality;
        ExtractorLink extractorLink = (ExtractorLink) this.L$0;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                quality = this.this$0.getQuality(this.$fileName);
                extractorLink.setQuality(quality);
                extractorLink.setHeaders(MapsKt.mapOf(TuplesKt.to("Cookie", "accountToken=" + this.$token)));
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
