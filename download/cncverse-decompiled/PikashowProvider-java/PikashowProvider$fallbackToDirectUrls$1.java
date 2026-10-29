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
@DebugMetadata(c = "com.cncverse.PikashowProvider", f = "PikashowProvider.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0}, l = {952}, m = "fallbackToDirectUrls", n = {"videoData", "callback", "contentName", "finalHeaders", "directUrl", "url", "linkType", "$i$a$-let-PikashowProvider$fallbackToDirectUrls$2", "quality"}, nl = {951}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "I$0", "I$1"}, v = 2)
/* loaded from: classes.dex */
public final class PikashowProvider$fallbackToDirectUrls$1 extends ContinuationImpl {
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
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ PikashowProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public PikashowProvider$fallbackToDirectUrls$1(PikashowProvider pikashowProvider, Continuation<? super PikashowProvider$fallbackToDirectUrls$1> continuation) {
        super(continuation);
        this.this$0 = pikashowProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object fallbackToDirectUrls;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        fallbackToDirectUrls = this.this$0.fallbackToDirectUrls(null, null, null, null, (Continuation) this);
        return fallbackToDirectUrls;
    }
}
