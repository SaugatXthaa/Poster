package com.cncverse;

import com.cncverse.DoFlixProvider;
import com.lagradost.cloudstream3.Actor;
import com.lagradost.cloudstream3.ActorData;
import com.lagradost.cloudstream3.ActorRole;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.MovieLoadResponse;
import com.lagradost.cloudstream3.MovieSearchResponse;
import com.lagradost.cloudstream3.TvType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: DoFlixProvider.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/MovieLoadResponse;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.DoFlixProvider$load$2", f = "DoFlixProvider.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
@SourceDebugExtension({"SMAP\nDoFlixProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DoFlixProvider.kt\ncom/cncverse/DoFlixProvider$load$2\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,836:1\n1586#2:837\n1661#2,3:838\n1586#2:841\n1661#2,3:842\n1586#2:845\n1661#2,3:846\n*S KotlinDebug\n*F\n+ 1 DoFlixProvider.kt\ncom/cncverse/DoFlixProvider$load$2\n*L\n683#1:837\n683#1:838,3\n687#1:841\n687#1:842,3\n695#1:845\n695#1:846,3\n*E\n"})
/* loaded from: classes.dex */
public final class DoFlixProvider$load$2 extends SuspendLambda implements Function2<MovieLoadResponse, Continuation<? super Unit>, Object> {
    final /* synthetic */ DoFlixProvider.MovieDetails $details;
    private /* synthetic */ Object L$0;
    int label;
    final /* synthetic */ DoFlixProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DoFlixProvider$load$2(DoFlixProvider.MovieDetails movieDetails, DoFlixProvider doFlixProvider, Continuation<? super DoFlixProvider$load$2> continuation) {
        super(2, continuation);
        this.$details = movieDetails;
        this.this$0 = doFlixProvider;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit invokeSuspend$lambda$2$0(DoFlixProvider.MovieResult movieResult, MovieSearchResponse movieSearchResponse) {
        String substringBefore$default;
        movieSearchResponse.setPosterUrl(movieResult.getPosterPath());
        String releaseDate = movieResult.getReleaseDate();
        if (releaseDate == null) {
            releaseDate = movieResult.getFirstAirDate();
        }
        Integer num = null;
        if (releaseDate != null && (substringBefore$default = StringsKt.substringBefore$default(releaseDate, "-", (String) null, 2, (Object) null)) != null) {
            num = StringsKt.toIntOrNull(substringBefore$default);
        }
        movieSearchResponse.setYear(num);
        return Unit.INSTANCE;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Continuation<Unit> doFlixProvider$load$2 = new DoFlixProvider$load$2(this.$details, this.this$0, continuation);
        doFlixProvider$load$2.L$0 = obj;
        return doFlixProvider$load$2;
    }

    public final Object invoke(MovieLoadResponse movieLoadResponse, Continuation<? super Unit> continuation) {
        return create(movieLoadResponse, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object obj) {
        MovieLoadResponse movieLoadResponse = (MovieLoadResponse) this.L$0;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                movieLoadResponse.setPosterUrl(this.$details.getPosterPath());
                movieLoadResponse.setBackgroundPosterUrl(this.$details.getBackdropPath());
                movieLoadResponse.setYear(StringsKt.toIntOrNull(StringsKt.substringBefore$default(this.$details.getReleaseDate(), "-", (String) null, 2, (Object) null)));
                movieLoadResponse.setPlot(this.$details.getOverview());
                List<DoFlixProvider.TMDBGenre> genres = this.$details.getGenres();
                ArrayList arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(genres, 10));
                Iterator<T> it = genres.iterator();
                while (it.hasNext()) {
                    arrayList.add(((DoFlixProvider.TMDBGenre) it.next()).getName());
                }
                movieLoadResponse.setTags(arrayList);
                movieLoadResponse.setDuration(Boxing.boxInt(this.$details.getRuntime()));
                List<DoFlixProvider.CastMember> take = CollectionsKt.take(this.$details.getCredits().getCast(), 10);
                ArrayList arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(take, 10));
                for (DoFlixProvider.CastMember castMember : take) {
                    arrayList2.add(new ActorData(new Actor(castMember.getName(), castMember.getProfilePath()), (ActorRole) null, castMember.getCharacter(), (Actor) null, 10, (DefaultConstructorMarker) null));
                }
                movieLoadResponse.setActors(arrayList2);
                List<DoFlixProvider.MovieResult> take2 = CollectionsKt.take(this.$details.getSimilar().getResults(), 10);
                DoFlixProvider doFlixProvider = this.this$0;
                ArrayList arrayList3 = new ArrayList(CollectionsKt.collectionSizeOrDefault(take2, 10));
                for (DoFlixProvider.MovieResult movieResult : take2) {
                    DoFlixProvider doFlixProvider2 = doFlixProvider;
                    String title = movieResult.getTitle();
                    if (title == null && (title = movieResult.getName()) == null) {
                        title = "Unknown";
                    }
                    arrayList3.add(MainAPIKt.newMovieSearchResponse$default(doFlixProvider2, title, "movie," + movieResult.getId(), TvType.Movie, false, new DoFlixProvider$load$2$$ExternalSyntheticLambda0(movieResult), 8, (Object) null));
                }
                movieLoadResponse.setRecommendations(arrayList3);
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
