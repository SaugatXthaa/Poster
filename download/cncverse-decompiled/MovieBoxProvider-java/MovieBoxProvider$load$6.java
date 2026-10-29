package com.cncverse;

import com.lagradost.cloudstream3.ActorData;
import com.lagradost.cloudstream3.LoadResponse;
import com.lagradost.cloudstream3.Score;
import com.lagradost.cloudstream3.TvSeriesLoadResponse;
import java.util.List;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.SourceDebugExtension;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MovieBoxProvider.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/TvSeriesLoadResponse;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MovieBoxProvider$load$6", f = "MovieBoxProvider.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
@SourceDebugExtension({"SMAP\nMovieBoxProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MovieBoxProvider.kt\ncom/cncverse/MovieBoxProvider$load$6\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,1242:1\n1#2:1243\n*E\n"})
/* loaded from: classes.dex */
public final class MovieBoxProvider$load$6 extends SuspendLambda implements Function2<TvSeriesLoadResponse, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $Background;
    final /* synthetic */ String $Description;
    final /* synthetic */ String $IMDBRating;
    final /* synthetic */ String $Poster;
    final /* synthetic */ List<ActorData> $actors;
    final /* synthetic */ String $backgroundUrl;
    final /* synthetic */ String $coverUrl;
    final /* synthetic */ String $description;
    final /* synthetic */ Integer $durationMinutes;
    final /* synthetic */ String $imdbId;
    final /* synthetic */ Integer $imdbRating;
    final /* synthetic */ String $logoUrl;
    final /* synthetic */ List<String> $tags;
    final /* synthetic */ Integer $tmdbId;
    final /* synthetic */ Integer $year;
    private /* synthetic */ Object L$0;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovieBoxProvider$load$6(String str, String str2, String str3, String str4, String str5, String str6, String str7, Integer num, List<String> list, List<ActorData> list2, String str8, Integer num2, Integer num3, String str9, Integer num4, Continuation<? super MovieBoxProvider$load$6> continuation) {
        super(2, continuation);
        this.$coverUrl = str;
        this.$Poster = str2;
        this.$Background = str3;
        this.$backgroundUrl = str4;
        this.$logoUrl = str5;
        this.$Description = str6;
        this.$description = str7;
        this.$year = num;
        this.$tags = list;
        this.$actors = list2;
        this.$IMDBRating = str8;
        this.$imdbRating = num2;
        this.$durationMinutes = num3;
        this.$imdbId = str9;
        this.$tmdbId = num4;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Continuation<Unit> movieBoxProvider$load$6 = new MovieBoxProvider$load$6(this.$coverUrl, this.$Poster, this.$Background, this.$backgroundUrl, this.$logoUrl, this.$Description, this.$description, this.$year, this.$tags, this.$actors, this.$IMDBRating, this.$imdbRating, this.$durationMinutes, this.$imdbId, this.$tmdbId, continuation);
        movieBoxProvider$load$6.L$0 = obj;
        return movieBoxProvider$load$6;
    }

    public final Object invoke(TvSeriesLoadResponse tvSeriesLoadResponse, Continuation<? super Unit> continuation) {
        return create(tvSeriesLoadResponse, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object obj) {
        LoadResponse loadResponse = (TvSeriesLoadResponse) this.L$0;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                String str = this.$coverUrl;
                if (str == null) {
                    str = this.$Poster;
                }
                loadResponse.setPosterUrl(str);
                String str2 = this.$Background;
                if (str2 == null && (str2 = this.$backgroundUrl) == null) {
                    str2 = this.$Poster;
                }
                loadResponse.setBackgroundPosterUrl(str2);
                try {
                    loadResponse.setLogoUrl(this.$logoUrl);
                } catch (Throwable th) {
                }
                String str3 = this.$Description;
                if (str3 == null) {
                    str3 = this.$description;
                }
                loadResponse.setPlot(str3);
                loadResponse.setYear(this.$year);
                loadResponse.setTags(this.$tags);
                loadResponse.setActors(this.$actors);
                Score from10 = Score.Companion.from10(this.$IMDBRating);
                if (from10 == null) {
                    Integer num = this.$imdbRating;
                    from10 = num != null ? Score.Companion.from10(Boxing.boxInt(num.intValue())) : null;
                }
                loadResponse.setScore(from10);
                loadResponse.setDuration(this.$durationMinutes);
                LoadResponse.Companion.addImdbId(loadResponse, this.$imdbId);
                LoadResponse.Companion.addTMDbId(loadResponse, String.valueOf(this.$tmdbId));
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
