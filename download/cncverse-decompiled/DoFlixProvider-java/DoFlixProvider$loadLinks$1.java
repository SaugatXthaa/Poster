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
@DebugMetadata(c = "com.cncverse.DoFlixProvider", f = "DoFlixProvider.kt", i = {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}, l = {787, 792, 812, 817}, m = "loadLinks", n = {"data", "subtitleCallback", "callback", "parts", "movieId", "linksUrl", "isCasting", "data", "subtitleCallback", "callback", "parts", "movieId", "linksUrl", "responseText", "linksResponse", "$this$forEach$iv", "element$iv", "link", "isCasting", "$i$f$forEach", "$i$a$-forEach-DoFlixProvider$loadLinks$2", "data", "subtitleCallback", "callback", "parts", "tvId", "seasonNum", "episodeNum", "linksUrl", "isCasting", "data", "subtitleCallback", "callback", "parts", "tvId", "seasonNum", "episodeNum", "linksUrl", "responseText", "linksResponse", "$this$forEach$iv", "element$iv", "link", "isCasting", "$i$f$forEach", "$i$a$-forEach-DoFlixProvider$loadLinks$3"}, nl = {788, 791, 813, 816}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$12", "L$13", "Z$0", "I$0", "I$1"}, v = 2)
/* loaded from: classes.dex */
public final class DoFlixProvider$loadLinks$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$13;
    Object L$14;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    boolean Z$0;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ DoFlixProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DoFlixProvider$loadLinks$1(DoFlixProvider doFlixProvider, Continuation<? super DoFlixProvider$loadLinks$1> continuation) {
        super(continuation);
        this.this$0 = doFlixProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.loadLinks(null, false, null, null, (Continuation) this);
    }
}
