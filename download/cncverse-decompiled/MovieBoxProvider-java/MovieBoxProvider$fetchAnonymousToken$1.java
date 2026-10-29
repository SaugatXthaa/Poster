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
@DebugMetadata(c = "com.cncverse.MovieBoxProvider", f = "MovieBoxProvider.kt", i = {0, 0, 0, 0, 0, 0}, l = {973}, m = "fetchAnonymousToken", n = {"tokenUrl", "xClientToken", "xTrSignature", "headers", "forceRefresh", "now"}, nl = {974}, s = {"L$0", "L$1", "L$2", "L$3", "Z$0", "J$0"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProvider$fetchAnonymousToken$1 extends ContinuationImpl {
    long J$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    boolean Z$0;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MovieBoxProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProvider$fetchAnonymousToken$1(MovieBoxProvider movieBoxProvider, Continuation<? super MovieBoxProvider$fetchAnonymousToken$1> continuation) {
        super(continuation);
        this.this$0 = movieBoxProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object fetchAnonymousToken;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        fetchAnonymousToken = this.this$0.fetchAnonymousToken(false, (Continuation) this);
        return fetchAnonymousToken;
    }
}
