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
@DebugMetadata(c = "com.cncverse.MlsbdProvider", f = "MlsbdProvider.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}, l = {557, 560, 570, 575}, m = "loadLinks", n = {"data", "subtitleCallback", "callback", "$this$forEach$iv", "element$iv", "item", "qualityLabel", "parts", "link", "trimmed", "isCasting", "$i$f$forEach", "$i$a$-forEach-MlsbdProvider$loadLinks$2", "data", "subtitleCallback", "callback", "$this$forEach$iv", "element$iv", "item", "qualityLabel", "parts", "link", "trimmed", "e", "isCasting", "$i$f$forEach", "$i$a$-forEach-MlsbdProvider$loadLinks$2", "data", "subtitleCallback", "callback", "$this$forEach$iv", "element$iv", "item", "qualityLabel", "parts", "link", "trimmed", "seenUrls", "$this$forEach$iv", "doc", "element$iv", "it", "href", "isCasting", "$i$f$forEach", "$i$a$-forEach-MlsbdProvider$loadLinks$2", "$i$f$forEach", "$i$a$-forEach-MlsbdProvider$loadLinks$2$1", "data", "subtitleCallback", "callback", "$this$forEach$iv", "element$iv", "item", "qualityLabel", "parts", "link", "trimmed", "isCasting", "$i$f$forEach", "$i$a$-forEach-MlsbdProvider$loadLinks$2"}, nl = {558, 561, 572, 577}, s = {"L$0", "L$1", "L$2", "L$3", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$14", "L$15", "L$16", "L$17", "Z$0", "I$0", "I$1", "I$2", "I$3", "L$0", "L$1", "L$2", "L$3", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "Z$0", "I$0", "I$1"}, v = 2)
/* loaded from: classes.dex */
public final class MlsbdProvider$loadLinks$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    int I$2;
    int I$3;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$13;
    Object L$14;
    Object L$15;
    Object L$16;
    Object L$17;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    boolean Z$0;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MlsbdProvider$loadLinks$1(MlsbdProvider mlsbdProvider, Continuation<? super MlsbdProvider$loadLinks$1> continuation) {
        super(continuation);
        this.this$0 = mlsbdProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.loadLinks(null, false, null, null, (Continuation) this);
    }
}
