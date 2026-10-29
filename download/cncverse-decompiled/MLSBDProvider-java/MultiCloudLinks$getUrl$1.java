package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: Extractors.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MultiCloudLinks", f = "Extractors.kt", i = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}, l = {685, 704, 718, 723, 739, 742, 751, 756, 774, 780, 786}, m = "getUrl$suspendImpl", n = {"$this", "url", "referer", "subtitleCallback", "callback", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "streamUrl", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "headResp", "pId", "finalUrl", "loc", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "resp", "loc", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "playerHtml", "streamSrc", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2", "$this", "url", "referer", "subtitleCallback", "callback", "res", "pageTitle", "labelExtras", "$this$forEach$iv", "element$iv", "element", "href", "text", "lower", "quality", "$i$f$forEach", "$i$a$-forEach-MultiCloudLinks$getUrl$2"}, nl = {686, 703, 719, 722, 740, 744, 752, 755, 775, 781, 785}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$17", "L$18", "L$19", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$17", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "I$1", "I$2"}, v = 2)
/* loaded from: classes.dex */
public final class MultiCloudLinks$getUrl$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    int I$2;
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
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MultiCloudLinks this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MultiCloudLinks$getUrl$1(MultiCloudLinks multiCloudLinks, Continuation<? super MultiCloudLinks$getUrl$1> continuation) {
        super(continuation);
        this.this$0 = multiCloudLinks;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return MultiCloudLinks.getUrl$suspendImpl(this.this$0, null, null, null, null, (Continuation) this);
    }
}
