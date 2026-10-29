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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderINKt", f = "MovieBoxProviderIN.kt", i = {0, 0, 0, 0}, l = {960}, m = "identifyID", n = {"title", "year", "imdbRatingValue", "normTitle"}, nl = {961}, s = {"L$0", "L$1", "L$2", "L$3"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderINKt$identifyID$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;
    /* synthetic */ Object result;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MovieBoxProviderINKt$identifyID$1(Continuation<? super MovieBoxProviderINKt$identifyID$1> continuation) {
        super(continuation);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object identifyID;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        identifyID = MovieBoxProviderINKt.identifyID(null, null, null, (Continuation) this);
        return identifyID;
    }
}
