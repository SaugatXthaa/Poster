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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderKt", f = "MovieBoxProvider.kt", i = {0, 0, 0}, l = {1041}, m = "searchAndPick$doSearch", n = {"endpoint", "extraParams", "url"}, nl = {1042}, s = {"L$0", "L$1", "L$2"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderKt$searchAndPick$doSearch$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    Object L$2;
    int label;
    /* synthetic */ Object result;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MovieBoxProviderKt$searchAndPick$doSearch$1(Continuation<? super MovieBoxProviderKt$searchAndPick$doSearch$1> continuation) {
        super(continuation);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object searchAndPick$doSearch;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        searchAndPick$doSearch = MovieBoxProviderKt.searchAndPick$doSearch(null, null, (Continuation) this);
        return searchAndPick$doSearch;
    }
}
