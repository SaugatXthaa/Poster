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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderIN", f = "MovieBoxProviderIN.kt", i = {0, 0}, l = {870}, m = "fetchDubDetailPaths", n = {"detailPath", "mapper"}, nl = {881}, s = {"L$0", "L$1"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderIN$fetchDubDetailPaths$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MovieBoxProviderIN this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProviderIN$fetchDubDetailPaths$1(MovieBoxProviderIN movieBoxProviderIN, Continuation<? super MovieBoxProviderIN$fetchDubDetailPaths$1> continuation) {
        super(continuation);
        this.this$0 = movieBoxProviderIN;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object fetchDubDetailPaths;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        fetchDubDetailPaths = this.this$0.fetchDubDetailPaths(null, null, (Continuation) this);
        return fetchDubDetailPaths;
    }
}
