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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderIN", f = "MovieBoxProviderIN.kt", i = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}, l = {316, 331, 338, 343}, m = "search", n = {"query", "url", "jsonBody", "xClientToken", "xTrSignature", "page", "query", "url", "jsonBody", "xClientToken", "xTrSignature", "guestTok", "headers", "requestBody", "page", "query", "url", "jsonBody", "xClientToken", "xTrSignature", "guestTok", "headers", "requestBody", "response", "page", "query", "url", "jsonBody", "xClientToken", "xTrSignature", "guestTok", "headers", "requestBody", "response", "retrySig", "page"}, nl = {318, 337, 339, 347}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderIN$search$2 extends ContinuationImpl {
    int I$0;
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
    final /* synthetic */ MovieBoxProviderIN this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProviderIN$search$2(MovieBoxProviderIN movieBoxProviderIN, Continuation<? super MovieBoxProviderIN$search$2> continuation) {
        super(continuation);
        this.this$0 = movieBoxProviderIN;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.search(null, 0, (Continuation) this);
    }
}
