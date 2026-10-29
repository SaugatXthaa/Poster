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
@DebugMetadata(c = "com.cncverse.PikashowProvider", f = "PikashowProvider.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6}, l = {735, 760, 778, 782, 795, 800, 804}, m = "addVideoLinksToCallback", n = {"videoData", "callback", "contentName", "baseHeaders", "finalHeaders", "$this$forEach$iv", "element$iv", "resolution", "url", "linkType", "hasResolutions", "hasLanguageResolutions", "$i$f$forEach", "$i$a$-forEach-PikashowProvider$addVideoLinksToCallback$6", "$i$a$-let-PikashowProvider$addVideoLinksToCallback$6$1", "videoData", "callback", "contentName", "baseHeaders", "finalHeaders", "$this$forEach$iv", "element$iv", "lang", "$this$forEach$iv", "element$iv", "resolution", "url", "linkType", "langName", "hasResolutions", "hasLanguageResolutions", "$i$f$forEach", "$i$a$-forEach-PikashowProvider$addVideoLinksToCallback$7", "$i$f$forEach", "$i$a$-forEach-PikashowProvider$addVideoLinksToCallback$7$1", "$i$a$-let-PikashowProvider$addVideoLinksToCallback$7$1$1", "videoData", "callback", "contentName", "baseHeaders", "finalHeaders", "hasResolutions", "hasLanguageResolutions", "videoData", "callback", "contentName", "baseHeaders", "finalHeaders", "streamingUrl", "urlOrigin", "hasResolutions", "hasLanguageResolutions", "videoData", "callback", "contentName", "baseHeaders", "finalHeaders", "streamingUrl", "hasResolutions", "hasLanguageResolutions", "videoData", "callback", "contentName", "baseHeaders", "finalHeaders", "e", "hasResolutions", "hasLanguageResolutions", "videoData", "callback", "contentName", "baseHeaders", "finalHeaders", "hasResolutions", "hasLanguageResolutions"}, nl = {734, 759, 779, 781, 797, 804, 809}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$7", "L$8", "L$9", "L$10", "I$0", "I$1", "I$2", "I$3", "I$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$7", "L$8", "L$9", "L$11", "L$12", "L$13", "L$14", "L$15", "I$0", "I$1", "I$2", "I$3", "I$4", "I$5", "I$6", "L$0", "L$1", "L$2", "L$3", "L$4", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "I$0", "I$1"}, v = 2)
/* loaded from: classes.dex */
public final class PikashowProvider$addVideoLinksToCallback$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    int I$2;
    int I$3;
    int I$4;
    int I$5;
    int I$6;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$13;
    Object L$14;
    Object L$15;
    Object L$16;
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
    final /* synthetic */ PikashowProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public PikashowProvider$addVideoLinksToCallback$1(PikashowProvider pikashowProvider, Continuation<? super PikashowProvider$addVideoLinksToCallback$1> continuation) {
        super(continuation);
        this.this$0 = pikashowProvider;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object addVideoLinksToCallback;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        addVideoLinksToCallback = this.this$0.addVideoLinksToCallback(null, null, null, (Continuation) this);
        return addVideoLinksToCallback;
    }
}
