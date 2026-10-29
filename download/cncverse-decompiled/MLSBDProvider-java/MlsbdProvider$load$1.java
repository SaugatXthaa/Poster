package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MlsbdProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MlsbdProvider", f = "MlsbdProvider.kt", i = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}, l = {429, 489, 535}, m = "load", n = {"url", "url", "doc", "title", "year", "image", "plot", "episodeDivs", "rangeRegex", "episodesData", "isTvSeries", "url", "doc", "title", "year", "image", "plot", "episodeDivs", "rangeRegex", "links", "isTvSeries"}, nl = {430, 498, -1}, s = {"L$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class MlsbdProvider$load$1 extends ContinuationImpl {
    int I$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MlsbdProvider$load$1(MlsbdProvider mlsbdProvider, Continuation<? super MlsbdProvider$load$1> continuation) {
        super(continuation);
        this.this$0 = mlsbdProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.load(null, (Continuation) this);
    }
}
