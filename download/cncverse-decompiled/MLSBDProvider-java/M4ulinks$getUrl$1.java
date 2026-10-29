package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: Extractors.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.M4ulinks", f = "Extractors.kt", i = {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, l = {97, 100}, m = "getUrl", n = {"url", "referer", "subtitleCallback", "callback", "url", "referer", "subtitleCallback", "callback", "res", "$this$forEach$iv", "element$iv", "it", "href", "$i$f$forEach", "$i$a$-forEach-M4ulinks$getUrl$2"}, nl = {98, 101}, s = {"L$0", "L$1", "L$2", "L$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$7", "L$8", "L$9", "I$0", "I$1"}, v = 2)
/* loaded from: classes.dex */
public final class M4ulinks$getUrl$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ M4ulinks this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public M4ulinks$getUrl$1(M4ulinks m4ulinks, Continuation<? super M4ulinks$getUrl$1> continuation) {
        super(continuation);
        this.this$0 = m4ulinks;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.getUrl(null, null, null, null, (Continuation) this);
    }
}
