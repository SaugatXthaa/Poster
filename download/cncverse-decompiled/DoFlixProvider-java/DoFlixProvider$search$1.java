package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: DoFlixProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.DoFlixProvider", f = "DoFlixProvider.kt", i = {0, 0, 0, 1, 1, 1, 1, 1, 1}, l = {619, 624}, m = "search", n = {"query", "searchResults", "movieSearchUrl", "query", "searchResults", "movieSearchUrl", "movieResponseText", "movieSearchData", "tvSearchUrl"}, nl = {620, 625}, s = {"L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5"}, v = 2)
/* loaded from: classes.dex */
public final class DoFlixProvider$search$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ DoFlixProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DoFlixProvider$search$1(DoFlixProvider doFlixProvider, Continuation<? super DoFlixProvider$search$1> continuation) {
        super(continuation);
        this.this$0 = doFlixProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.search(null, (Continuation) this);
    }
}
