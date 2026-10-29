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
@DebugMetadata(c = "com.cncverse.GDFlix", f = "Extractors.kt", i = {}, l = {381}, m = "getLatestUrl", n = {}, nl = {380}, s = {}, v = 2)
/* loaded from: classes.dex */
public final class GDFlix$getLatestUrl$1 extends ContinuationImpl {
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ GDFlix this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public GDFlix$getLatestUrl$1(GDFlix gDFlix, Continuation<? super GDFlix$getLatestUrl$1> continuation) {
        super(continuation);
        this.this$0 = gDFlix;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object latestUrl;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        latestUrl = this.this$0.getLatestUrl((Continuation) this);
        return latestUrl;
    }
}
