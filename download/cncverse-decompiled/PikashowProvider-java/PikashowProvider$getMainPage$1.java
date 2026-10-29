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
@DebugMetadata(c = "com.cncverse.PikashowProvider", f = "PikashowProvider.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, l = {230}, m = "getMainPage", n = {"request", "headers", "homePageList", "categories", "$this$forEach$iv", "element$iv", "type", "displayName", "url", "params", "page", "$i$f$forEach", "$i$a$-forEach-PikashowProvider$getMainPage$2"}, nl = {237}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$6", "L$7", "L$8", "L$9", "L$10", "I$0", "I$1", "I$2"}, v = 2)
/* loaded from: classes.dex */
public final class PikashowProvider$getMainPage$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    int I$2;
    Object L$0;
    Object L$1;
    Object L$10;
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
    public PikashowProvider$getMainPage$1(PikashowProvider pikashowProvider, Continuation<? super PikashowProvider$getMainPage$1> continuation) {
        super(continuation);
        this.this$0 = pikashowProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.getMainPage(0, null, (Continuation) this);
    }
}
