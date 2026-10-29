package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MovieBoxProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MovieBoxProvider", f = "MovieBoxProvider.kt", i = {0}, l = {368}, m = "search", n = {"query"}, nl = {-1}, s = {"L$0"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProvider$search$1 extends ContinuationImpl {
    Object L$0;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MovieBoxProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProvider$search$1(MovieBoxProvider movieBoxProvider, Continuation<? super MovieBoxProvider$search$1> continuation) {
        super(continuation);
        this.this$0 = movieBoxProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.search(null, (Continuation) this);
    }
}
