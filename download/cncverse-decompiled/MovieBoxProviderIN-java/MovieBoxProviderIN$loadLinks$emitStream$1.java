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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderIN", f = "MovieBoxProviderIN.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4}, l = {771, 810, 816, 834, 840}, m = "loadLinks$emitStream", n = {"subjectId", "$callback", "this$0", "langLabel", "token", "mapper", "$subtitleCallback", "subDomain", "subPath", "stream", "fmt", "link", "streamId", "resStr", "quality", "signCookieRaw", "signCookie", "signHeaderKey", "linkType", "season", "episode", "subjectId", "$callback", "this$0", "langLabel", "token", "mapper", "$subtitleCallback", "subDomain", "subPath", "stream", "fmt", "link", "streamId", "resStr", "quality", "signCookieRaw", "signCookie", "signHeaderKey", "linkType", "subLink", "subHeaders", "season", "episode", "subjectId", "$callback", "this$0", "langLabel", "token", "mapper", "$subtitleCallback", "subDomain", "subPath", "stream", "fmt", "link", "streamId", "resStr", "quality", "signCookieRaw", "signCookie", "signHeaderKey", "linkType", "subLink", "subHeaders", "subRoot", "extCaptions", "caption", "captionUrl", "lang", "season", "episode", "subjectId", "$callback", "this$0", "langLabel", "token", "mapper", "$subtitleCallback", "subDomain", "subPath", "stream", "fmt", "link", "streamId", "resStr", "quality", "signCookieRaw", "signCookie", "signHeaderKey", "linkType", "subLink1", "subHeaders1", "season", "episode", "subjectId", "$callback", "this$0", "langLabel", "token", "mapper", "$subtitleCallback", "subDomain", "subPath", "stream", "fmt", "link", "streamId", "resStr", "quality", "signCookieRaw", "signCookie", "signHeaderKey", "linkType", "subLink1", "subHeaders1", "subRoot1", "extCaptions1", "caption", "captionUrl", "lang", "season", "episode"}, nl = {770, 811, 819, 835, 843}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$24", "L$25", "L$26", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$24", "L$25", "L$26", "I$0", "I$1"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderIN$loadLinks$emitStream$1 extends ContinuationImpl {
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
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    /* synthetic */ Object result;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MovieBoxProviderIN$loadLinks$emitStream$1(Continuation<? super MovieBoxProviderIN$loadLinks$emitStream$1> continuation) {
        super(continuation);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object loadLinks$emitStream;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        loadLinks$emitStream = MovieBoxProviderIN.loadLinks$emitStream(null, 0, 0, null, null, null, null, null, null, null, null, null, null, (Continuation) this);
        return loadLinks$emitStream;
    }
}
