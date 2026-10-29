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
@DebugMetadata(c = "com.cncverse.MlsbdProvider", f = "MlsbdProvider.kt", i = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8}, l = {599, 602, 605, 608, 611, 614, 617, 623, 634}, m = "resolveCustomExtractor", n = {"url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl", "url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl", "url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl", "url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl", "url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl", "url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl", "url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl", "url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl", "fileId", "directUrl", "url", "qualityLabel", "subtitleCallback", "callback", "cleanUrl"}, nl = {601, 604, 607, 610, 613, 616, 619, 622, 637}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$0", "L$1", "L$2", "L$3", "L$4"}, v = 2)
/* loaded from: classes.dex */
public final class MlsbdProvider$resolveCustomExtractor$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MlsbdProvider$resolveCustomExtractor$1(MlsbdProvider mlsbdProvider, Continuation<? super MlsbdProvider$resolveCustomExtractor$1> continuation) {
        super(continuation);
        this.this$0 = mlsbdProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object resolveCustomExtractor;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        resolveCustomExtractor = this.this$0.resolveCustomExtractor(null, null, null, null, (Continuation) this);
        return resolveCustomExtractor;
    }
}
