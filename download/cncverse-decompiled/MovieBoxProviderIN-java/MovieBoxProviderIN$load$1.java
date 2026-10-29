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
@DebugMetadata(c = "com.cncverse.MovieBoxProviderIN", f = "MovieBoxProviderIN.kt", i = {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9}, l = {386, 405, 407, 412, 474, 480, 488, 516, 592, 607}, m = "load", n = {"url", "id", "finalUrl", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "response", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "response", "retrySig", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "response", "body", "mapper", "root", "data", "title", "description", "releaseDate", "duration", "genre", "imdbRating", "year", "coverUrl", "backgroundUrl", "actors", "tags", "durationMinutes", "type", "subjectType", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "response", "body", "mapper", "root", "data", "title", "description", "releaseDate", "duration", "genre", "imdbRating", "year", "coverUrl", "backgroundUrl", "actors", "tags", "durationMinutes", "type", "tmdbId", "imdbId", "subjectType", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "response", "body", "mapper", "root", "data", "title", "description", "releaseDate", "duration", "genre", "imdbRating", "year", "coverUrl", "backgroundUrl", "actors", "tags", "durationMinutes", "type", "tmdbId", "imdbId", "logoUrl", "subjectType", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "response", "body", "mapper", "root", "data", "title", "description", "releaseDate", "duration", "genre", "imdbRating", "year", "coverUrl", "backgroundUrl", "actors", "tags", "durationMinutes", "type", "tmdbId", "imdbId", "logoUrl", "meta", "metaVideos", "Poster", "Background", "Description", "IMDBRating", "allSubjectIds", "episodeMap", "subjectId", "seasonUrl", "seasonSig", "seasonHeaders", "subjectType", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "response", "body", "mapper", "root", "data", "title", "description", "releaseDate", "duration", "genre", "imdbRating", "year", "coverUrl", "backgroundUrl", "actors", "tags", "durationMinutes", "type", "tmdbId", "imdbId", "logoUrl", "meta", "metaVideos", "Poster", "Background", "Description", "IMDBRating", "allSubjectIds", "episodeMap", "episodes", "subjectType", "url", "id", "finalUrl", "guestTok", "xClientToken", "xTrSignature", "headers", "response", "body", "mapper", "root", "data", "title", "description", "releaseDate", "duration", "genre", "imdbRating", "year", "coverUrl", "backgroundUrl", "actors", "tags", "durationMinutes", "type", "tmdbId", "imdbId", "logoUrl", "meta", "metaVideos", "Poster", "Background", "Description", "IMDBRating", "subjectType"}, nl = {387, 406, 408, 416, 481, 488, 489, 517, 607, -1}, s = {"L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "L$33", "L$34", "L$35", "L$37", "L$38", "L$39", "L$40", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "L$33", "L$34", "L$35", "L$36", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "L$33", "I$0"}, v = 2)
/* loaded from: classes.dex */
public final class MovieBoxProviderIN$load$1 extends ContinuationImpl {
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
    Object L$37;
    Object L$38;
    Object L$39;
    Object L$4;
    Object L$40;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ MovieBoxProviderIN this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProviderIN$load$1(MovieBoxProviderIN movieBoxProviderIN, Continuation<? super MovieBoxProviderIN$load$1> continuation) {
        super(continuation);
        this.this$0 = movieBoxProviderIN;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.load(null, (Continuation) this);
    }
}
