package com.cncverse;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MovieBoxProviderIN.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MovieBoxProviderIN", f = "MovieBoxProviderIN.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8}, l = {647, 665, 667, 672, 726, 747, 847, 848, 849}, m = "loadLinks", n = {"data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "isCasting", "season", "episode", "data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "subjectUrl", "subjectXClientToken", "subjectXTrSignature", "subjectHeaders", "isCasting", "season", "episode", "data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "subjectUrl", "subjectXClientToken", "subjectXTrSignature", "subjectHeaders", "subjectResponse", "isCasting", "season", "episode", "data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "subjectUrl", "subjectXClientToken", "subjectXTrSignature", "subjectHeaders", "subjectResponse", "retrySig", "isCasting", "season", "episode", "data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "subjectUrl", "subjectXClientToken", "subjectXTrSignature", "subjectHeaders", "subjectResponse", "mapper", "subjectIds", "originalLanguageName", "detailPath", "detailDomain", "xUserHeader", "isCasting", "season", "episode", "data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "subjectUrl", "subjectXClientToken", "subjectXTrSignature", "subjectHeaders", "subjectResponse", "mapper", "subjectIds", "originalLanguageName", "detailPath", "detailDomain", "xUserHeader", "dubPaths", "subjectId", "language", "langLabel", "subDomain", "subPath", "mbBase", "mbUrl", "mbHeaders", "isCasting", "season", "episode", "data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "subjectUrl", "subjectXClientToken", "subjectXTrSignature", "subjectHeaders", "subjectResponse", "mapper", "subjectIds", "originalLanguageName", "detailPath", "detailDomain", "xUserHeader", "dubPaths", "subjectId", "language", "langLabel", "subDomain", "subPath", "mbBase", "mbUrl", "mbHeaders", "mbResponse", "mbRoot", "dataNode", "netDash", "s", "isCasting", "season", "episode", "data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "subjectUrl", "subjectXClientToken", "subjectXTrSignature", "subjectHeaders", "subjectResponse", "mapper", "subjectIds", "originalLanguageName", "detailPath", "detailDomain", "xUserHeader", "dubPaths", "subjectId", "language", "langLabel", "subDomain", "subPath", "mbBase", "mbUrl", "mbHeaders", "mbResponse", "mbRoot", "dataNode", "netDash", "netHls", "s", "isCasting", "season", "episode", "data", "subtitleCallback", "callback", "brand", "model", "parts", "originalSubjectId", "token", "subjectUrl", "subjectXClientToken", "subjectXTrSignature", "subjectHeaders", "subjectResponse", "mapper", "subjectIds", "originalLanguageName", "detailPath", "detailDomain", "xUserHeader", "dubPaths", "subjectId", "language", "langLabel", "subDomain", "subPath", "mbBase", "mbUrl", "mbHeaders", "mbResponse", "mbRoot", "dataNode", "netDash", "netHls", "netMp4", "s", "isCasting", "season", "episode"}, nl = {648, 666, 668, 676, 727, 748, 848, 849, 851}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "L$34", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "L$33", "L$35", "Z$0", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "L$33", "L$34", "L$36", "Z$0", "I$0", "I$1"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderIN$loadLinks$1 extends ContinuationImpl {
    int I$0;
    int I$1;
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
    Object L$20;
    Object L$21;
    Object L$22;
    Object L$23;
    Object L$24;
    Object L$25;
    Object L$26;
    Object L$27;
    Object L$28;
    Object L$29;
    Object L$3;
    Object L$30;
    Object L$31;
    Object L$32;
    Object L$33;
    Object L$34;
    Object L$35;
    Object L$36;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    boolean Z$0;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MovieBoxProviderIN this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProviderIN$loadLinks$1(MovieBoxProviderIN movieBoxProviderIN, Continuation<? super MovieBoxProviderIN$loadLinks$1> continuation) {
        super(continuation);
        this.this$0 = movieBoxProviderIN;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.loadLinks(null, false, null, null, (Continuation) this);
    }
}
