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
@DebugMetadata(c = "com.cncverse.GDxFiles", f = "Extractors.kt", i = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9}, l = {570, 580, 584, 587, 608, 613, 633, 636, 647, 653}, m = "getUrl$suspendImpl", n = {"$this", "url", "referer", "subtitleCallback", "callback", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "r2PageUrl", "quality", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "r2PageUrl", "r2Doc", "dlBtn", "dlHref", "quality", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "r2PageUrl", "r2Doc", "dlBtn", "dlHref", "resp", "loc", "quality", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "watchBtn", "watchUrl", "quality", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "watchBtn", "watchUrl", "watchHtml", "srcMatch", "unescaped", "quality", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "watchBtn", "gofileBtn", "quality", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "watchBtn", "gofileBtn", "gofileDoc", "gofileLink", "quality", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "watchBtn", "gofileBtn", "pixelBtn", "quality", "$this", "url", "referer", "subtitleCallback", "callback", "doc", "titleText", "labelExtras", "r2Btn", "watchBtn", "gofileBtn", "pixelBtn", "pixelDoc", "pixelLink", "id", "dUrl", "quality"}, nl = {571, 581, 585, 586, 609, 612, 634, 638, 648, 652}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class GDxFiles$getUrl$1 extends ContinuationImpl {
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
    final /* synthetic */ GDxFiles this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public GDxFiles$getUrl$1(GDxFiles gDxFiles, Continuation<? super GDxFiles$getUrl$1> continuation) {
        super(continuation);
        this.this$0 = gDxFiles;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return GDxFiles.getUrl$suspendImpl(this.this$0, null, null, null, null, (Continuation) this);
    }
}
