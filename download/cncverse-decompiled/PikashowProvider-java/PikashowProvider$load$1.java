package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: PikashowProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.PikashowProvider", f = "PikashowProvider.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}, l = {461, 491, 515, 527}, m = "load", n = {"url", "withoutUrlScheme", "parts", "identifier", "type", "headers", "seriesUrl", "params", "url", "withoutUrlScheme", "parts", "identifier", "type", "headers", "seriesUrl", "params", "response", "seriesResponse", "series", "seriesData", "episodes", "$i$a$-let-PikashowProvider$load$2", "url", "withoutUrlScheme", "parts", "identifier", "type", "headers", "movieUrl", "params", "url", "withoutUrlScheme", "parts", "identifier", "type", "headers", "movieUrl", "params", "response", "movieResponse", "movie", "movieData", "$i$a$-let-PikashowProvider$load$3"}, nl = {468, 502, 522, 538}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class PikashowProvider$load$1 extends ContinuationImpl {
    int I$0;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
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
    final /* synthetic */ PikashowProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public PikashowProvider$load$1(PikashowProvider pikashowProvider, Continuation<? super PikashowProvider$load$1> continuation) {
        super(continuation);
        this.this$0 = pikashowProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.load(null, (Continuation) this);
    }
}
