package com.cncverse;

import com.lagradost.cloudstream3.MovieLoadResponse;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MlsbdProvider.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/MovieLoadResponse;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MlsbdProvider$load$5", f = "MlsbdProvider.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
/* loaded from: classes.dex */
public final class MlsbdProvider$load$5 extends SuspendLambda implements Function2<MovieLoadResponse, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $image;
    final /* synthetic */ String $plot;
    final /* synthetic */ Integer $year;
    private /* synthetic */ Object L$0;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MlsbdProvider$load$5(String str, Integer num, String str2, Continuation<? super MlsbdProvider$load$5> continuation) {
        super(2, continuation);
        this.$image = str;
        this.$year = num;
        this.$plot = str2;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Continuation<Unit> mlsbdProvider$load$5 = new MlsbdProvider$load$5(this.$image, this.$year, this.$plot, continuation);
        mlsbdProvider$load$5.L$0 = obj;
        return mlsbdProvider$load$5;
    }

    public final Object invoke(MovieLoadResponse movieLoadResponse, Continuation<? super Unit> continuation) {
        return create(movieLoadResponse, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object obj) {
        MovieLoadResponse movieLoadResponse = (MovieLoadResponse) this.L$0;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                movieLoadResponse.setPosterUrl(this.$image);
                movieLoadResponse.setYear(this.$year);
                movieLoadResponse.setPlot(this.$plot);
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
