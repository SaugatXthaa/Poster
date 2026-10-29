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
@DebugMetadata(c = "com.cncverse.MlsbdProvider", f = "MlsbdProvider.kt", i = {0, 0, 0}, l = {396}, m = "getMainPage", n = {"request", "url", "page"}, nl = {397}, s = {"L$0", "L$1", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class MlsbdProvider$getMainPage$1 extends ContinuationImpl {
    int I$0;
    Object L$0;
    Object L$1;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MlsbdProvider$getMainPage$1(MlsbdProvider mlsbdProvider, Continuation<? super MlsbdProvider$getMainPage$1> continuation) {
        super(continuation);
        this.this$0 = mlsbdProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.getMainPage(0, null, (Continuation) this);
    }
}
