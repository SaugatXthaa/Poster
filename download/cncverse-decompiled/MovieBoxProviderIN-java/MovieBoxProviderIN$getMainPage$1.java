package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MovieBoxProviderIN.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MovieBoxProviderIN", f = "MovieBoxProviderIN.kt", i = {0, 0, 0, 0, 0, 0}, l = {247}, m = "getMainPage", n = {"request", "url", "xClientToken", "xTrSignature", "headers", "page"}, nl = {248}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderIN$getMainPage$1 extends ContinuationImpl {
    int I$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MovieBoxProviderIN this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProviderIN$getMainPage$1(MovieBoxProviderIN movieBoxProviderIN, Continuation<? super MovieBoxProviderIN$getMainPage$1> continuation) {
        super(continuation);
        this.this$0 = movieBoxProviderIN;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.getMainPage(0, null, (Continuation) this);
    }
}
