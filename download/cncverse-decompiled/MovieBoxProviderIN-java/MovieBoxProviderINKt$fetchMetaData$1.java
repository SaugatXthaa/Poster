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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderINKt", f = "MovieBoxProviderIN.kt", i = {0, 0, 0, 0}, l = {1099}, m = "fetchMetaData", n = {"imdbId", "type", "metaType", "url"}, nl = {1100}, s = {"L$0", "L$1", "L$2", "L$3"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderINKt$fetchMetaData$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;
    /* synthetic */ Object result;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MovieBoxProviderINKt$fetchMetaData$1(Continuation<? super MovieBoxProviderINKt$fetchMetaData$1> continuation) {
        super(continuation);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object fetchMetaData;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        fetchMetaData = MovieBoxProviderINKt.fetchMetaData(null, null, (Continuation) this);
        return fetchMetaData;
    }
}
