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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderINKt", f = "MovieBoxProviderIN.kt", i = {0, 0, 0, 0, 0, 0, 0}, l = {1121}, m = "fetchTmdbLogoUrl", n = {"tmdbAPI", "apiKey", "type", "tmdbId", "appLangCode", "url", "$i$a$-runCatching-MovieBoxProviderINKt$fetchTmdbLogoUrl$json$1"}, nl = {1121}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderINKt$fetchTmdbLogoUrl$1 extends ContinuationImpl {
    int I$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    int label;
    /* synthetic */ Object result;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MovieBoxProviderINKt$fetchTmdbLogoUrl$1(Continuation<? super MovieBoxProviderINKt$fetchTmdbLogoUrl$1> continuation) {
        super(continuation);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return MovieBoxProviderINKt.fetchTmdbLogoUrl(null, null, null, null, null, (Continuation) this);
    }
}
