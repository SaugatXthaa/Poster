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
@DebugMetadata(c = "com.cncverse.Filesdl", f = "Extractors.kt", i = {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6}, l = {38, 47, 49, 51, 54, 57, 70}, m = "getUrl", n = {"url", "referer", "subtitleCallback", "callback", "url", "referer", "subtitleCallback", "callback", "res", "titleText", "qualityRegex", "quality", "$this$map$iv", "$this$mapTo$iv$iv", "destination$iv$iv", "item$iv$iv", "it", "source", "href", "$i$f$map", "$i$f$mapTo", "$i$a$-map-Filesdl$getUrl$2", "url", "referer", "subtitleCallback", "callback", "res", "titleText", "qualityRegex", "quality", "$this$map$iv", "$this$mapTo$iv$iv", "destination$iv$iv", "item$iv$iv", "it", "source", "href", "$i$f$map", "$i$f$mapTo", "$i$a$-map-Filesdl$getUrl$2", "url", "referer", "subtitleCallback", "callback", "res", "titleText", "qualityRegex", "quality", "$this$map$iv", "$this$mapTo$iv$iv", "destination$iv$iv", "item$iv$iv", "it", "source", "href", "$i$f$map", "$i$f$mapTo", "$i$a$-map-Filesdl$getUrl$2", "url", "referer", "subtitleCallback", "callback", "res", "titleText", "qualityRegex", "quality", "$this$map$iv", "$this$mapTo$iv$iv", "destination$iv$iv", "item$iv$iv", "it", "source", "href", "$i$f$map", "$i$f$mapTo", "$i$a$-map-Filesdl$getUrl$2", "url", "referer", "subtitleCallback", "callback", "res", "titleText", "qualityRegex", "quality", "$this$map$iv", "$this$mapTo$iv$iv", "destination$iv$iv", "item$iv$iv", "it", "source", "href", "response", "redirectUrl", "$i$f$map", "$i$f$mapTo", "$i$a$-map-Filesdl$getUrl$2", "url", "referer", "subtitleCallback", "callback", "res", "titleText", "qualityRegex", "quality", "$this$map$iv", "$this$mapTo$iv$iv", "destination$iv$iv", "item$iv$iv", "it", "source", "href", "$i$f$map", "$i$f$mapTo", "$i$a$-map-Filesdl$getUrl$2"}, nl = {39, 49, 51, 53, 55, 56, 69}, s = {"L$0", "L$1", "L$2", "L$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$12", "L$13", "L$14", "L$15", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$12", "L$13", "L$14", "L$15", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$12", "L$13", "L$14", "L$15", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$12", "L$13", "L$14", "L$15", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "I$0", "I$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$12", "L$13", "L$14", "L$15", "I$0", "I$1", "I$2"}, v = 2)
/* loaded from: classes.dex */
public final class Filesdl$getUrl$1 extends ContinuationImpl {
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
    final /* synthetic */ Filesdl this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public Filesdl$getUrl$1(Filesdl filesdl, Continuation<? super Filesdl$getUrl$1> continuation) {
        super(continuation);
        this.this$0 = filesdl;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.getUrl(null, null, null, null, (Continuation) this);
    }
}
