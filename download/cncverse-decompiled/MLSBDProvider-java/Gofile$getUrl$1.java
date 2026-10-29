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
@DebugMetadata(c = "com.cncverse.Gofile", f = "Extractors.kt", i = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}, l = {941, 945, 949, 972}, m = "getUrl", n = {"url", "referer", "subtitleCallback", "callback", "id", "url", "referer", "subtitleCallback", "callback", "id", "responseText", "json", "token", "url", "referer", "subtitleCallback", "callback", "id", "responseText", "json", "token", "globalJs", "wt", "url", "referer", "subtitleCallback", "callback", "id", "responseText", "json", "token", "globalJs", "wt", "responseTextfile", "fileDataJson", "data", "children", "firstFileId", "fileObj", "link", "fileName", "sizeFormatted", "fileSize"}, nl = {942, 946, 952, 971}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "J$0"}, v = 2)
/* loaded from: classes.dex */
public final class Gofile$getUrl$1 extends ContinuationImpl {
    long J$0;
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
    final /* synthetic */ Gofile this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public Gofile$getUrl$1(Gofile gofile, Continuation<? super Gofile$getUrl$1> continuation) {
        super(continuation);
        this.this$0 = gofile;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.getUrl(null, null, null, null, (Continuation) this);
    }
}
