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
@DebugMetadata(c = "com.cncverse.MlsbdProvider", f = "MlsbdProvider.kt", i = {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4}, l = {120, 649, 136, 149, 157}, m = "fetchHtml", n = {"url", "savedCf", "savedCfTs", "hasValidCache", "url", "savedCf", "$this$withLock_u24default$iv", "savedCfTs", "hasValidCache", "$i$f$withLock", "url", "savedCf", "$this$withLock_u24default$iv", "freshCf", "savedCfTs", "hasValidCache", "$i$f$withLock", "$i$a$-withLock$default-MlsbdProvider$fetchHtml$2", "freshCfTs", "hasFreshCache", "url", "savedCf", "$this$withLock_u24default$iv", "freshCf", "savedCfTs", "hasValidCache", "$i$f$withLock", "$i$a$-withLock$default-MlsbdProvider$fetchHtml$2", "freshCfTs", "hasFreshCache", "url", "savedCf", "$this$withLock_u24default$iv", "freshCf", "wvDoc", "newCf", "savedCfTs", "hasValidCache", "$i$f$withLock", "$i$a$-withLock$default-MlsbdProvider$fetchHtml$2", "freshCfTs", "hasFreshCache"}, nl = {121, 650, 137, 150, 158}, s = {"L$0", "L$1", "J$0", "I$0", "L$0", "L$1", "L$2", "J$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "J$0", "I$0", "I$1", "I$2", "J$1", "I$3", "L$0", "L$1", "L$2", "L$3", "J$0", "I$0", "I$1", "I$2", "J$1", "I$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "J$0", "I$0", "I$1", "I$2", "J$1", "I$3"}, v = 2)
/* loaded from: classes.dex */
public final class MlsbdProvider$fetchHtml$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    int I$2;
    int I$3;
    long J$0;
    long J$1;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MlsbdProvider$fetchHtml$1(MlsbdProvider mlsbdProvider, Continuation<? super MlsbdProvider$fetchHtml$1> continuation) {
        super(continuation);
        this.this$0 = mlsbdProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object fetchHtml;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        fetchHtml = this.this$0.fetchHtml(null, (Continuation) this);
        return fetchHtml;
    }
}
