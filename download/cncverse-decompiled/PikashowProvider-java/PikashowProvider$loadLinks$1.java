package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: PikashowProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.PikashowProvider", f = "PikashowProvider.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4}, l = {581, 591, 612, 655, 665}, m = "loadLinks", n = {"data", "subtitleCallback", "callback", "withoutUrlScheme", "headers", "parts", "seriesTitle", "season", "episode", "videoUrl", "params", "isCasting", "data", "subtitleCallback", "callback", "withoutUrlScheme", "headers", "parts", "seriesTitle", "season", "episode", "videoUrl", "params", "response", "videoResponse", "videoData", "isCasting", "$i$a$-let-PikashowProvider$loadLinks$2", "data", "subtitleCallback", "callback", "withoutUrlScheme", "headers", "parts", "identifier", "type", "listUrl", "listParams", "isCasting", "data", "subtitleCallback", "callback", "withoutUrlScheme", "headers", "parts", "identifier", "type", "listUrl", "listParams", "listResponse", "videoId", "title", "safeTitle", "videoUrl", "videoParams", "isCasting", "data", "subtitleCallback", "callback", "withoutUrlScheme", "headers", "parts", "identifier", "type", "listUrl", "listParams", "listResponse", "videoId", "title", "safeTitle", "videoUrl", "videoParams", "videoResponse", "videoApiResponse", "contentNameLocal", "videoData", "isCasting", "$i$a$-let-PikashowProvider$loadLinks$5"}, nl = {588, 592, 619, 661, 666}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "Z$0", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "Z$0", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class PikashowProvider$loadLinks$1 extends ContinuationImpl {
    int I$0;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$13;
    Object L$14;
    Object L$15;
    Object L$16;
    Object L$17;
    Object L$18;
    Object L$19;
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
    final /* synthetic */ PikashowProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public PikashowProvider$loadLinks$1(PikashowProvider pikashowProvider, Continuation<? super PikashowProvider$loadLinks$1> continuation) {
        super(continuation);
        this.this$0 = pikashowProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.loadLinks(null, false, null, null, (Continuation) this);
    }
}
