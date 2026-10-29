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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderKt", f = "MovieBoxProvider.kt", i = {0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}, l = {1045, 1048, 1049, 1126}, m = "searchAndPick", n = {"normTitle", "year", "imdbRatingValue", "normTitle", "year", "imdbRatingValue", "multiResults", "normTitle", "year", "imdbRatingValue", "multiResults", "normTitle", "year", "imdbRatingValue", "multiResults", "searchQueues", "bestId", "detailKind", "detailUrl", "bestScore", "bestIsTv"}, nl = {1047, 1047, 1047, 1127}, s = {"L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$3", "L$0", "L$1", "L$2", "L$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "D$0", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderKt$searchAndPick$1 extends ContinuationImpl {
    double D$0;
    int I$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    int label;
    /* synthetic */ Object result;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MovieBoxProviderKt$searchAndPick$1(Continuation<? super MovieBoxProviderKt$searchAndPick$1> continuation) {
        super(continuation);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object searchAndPick;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        searchAndPick = MovieBoxProviderKt.searchAndPick(null, null, null, (Continuation) this);
        return searchAndPick;
    }
}
