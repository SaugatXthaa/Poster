package com.cncverse;

import com.cncverse.DoFlixProvider;
import com.lagradost.cloudstream3.Actor;
import com.lagradost.cloudstream3.ActorData;
import com.lagradost.cloudstream3.ActorRole;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.TvSeriesLoadResponse;
import com.lagradost.cloudstream3.TvSeriesSearchResponse;
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
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: DoFlixProvider.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/TvSeriesLoadResponse;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.DoFlixProvider$load$5", f = "DoFlixProvider.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
@SourceDebugExtension({"SMAP\nDoFlixProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DoFlixProvider.kt\ncom/cncverse/DoFlixProvider$load$5\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,836:1\n1586#2:837\n1661#2,3:838\n1586#2:841\n1661#2,3:842\n1586#2:845\n1661#2,3:846\n*S KotlinDebug\n*F\n+ 1 DoFlixProvider.kt\ncom/cncverse/DoFlixProvider$load$5\n*L\n747#1:837\n747#1:838,3\n750#1:841\n750#1:842,3\n758#1:845\n758#1:846,3\n*E\n"})
/* loaded from: classes.dex */
public final class DoFlixProvider$load$5 extends SuspendLambda implements Function2<TvSeriesLoadResponse, Continuation<? super Unit>, Object> {
    final /* synthetic */ DoFlixProvider.TvDetails $details;
    private /* synthetic */ Object L$0;
    int label;
    final /* synthetic */ DoFlixProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DoFlixProvider$load$5(DoFlixProvider.TvDetails tvDetails, DoFlixProvider doFlixProvider, Continuation<? super DoFlixProvider$load$5> continuation) {
        super(2, continuation);
        this.$details = tvDetails;
        this.this$0 = doFlixProvider;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit invokeSuspend$lambda$2$0(DoFlixProvider.MovieResult movieResult, TvSeriesSearchResponse tvSeriesSearchResponse) {
        String substringBefore$default;
        tvSeriesSearchResponse.setPosterUrl(movieResult.getPosterPath());
        String firstAirDate = movieResult.getFirstAirDate();
        if (firstAirDate == null) {
            firstAirDate = movieResult.getReleaseDate();
        }
        Integer num = null;
        if (firstAirDate != null && (substringBefore$default = StringsKt.substringBefore$default(firstAirDate, "-", (String) null, 2, (Object) null)) != null) {
            num = StringsKt.toIntOrNull(substringBefore$default);
        }
        tvSeriesSearchResponse.setYear(num);
        return Unit.INSTANCE;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Continuation<Unit> doFlixProvider$load$5 = new DoFlixProvider$load$5(this.$details, this.this$0, continuation);
        doFlixProvider$load$5.L$0 = obj;
        return doFlixProvider$load$5;
    }

    public final Object invoke(TvSeriesLoadResponse tvSeriesLoadResponse, Continuation<? super Unit> continuation) {
        return create(tvSeriesLoadResponse, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object obj) {
        ArrayList arrayList;
        List<DoFlixProvider.MovieResult> results;
        List take;
        TvSeriesLoadResponse tvSeriesLoadResponse = (TvSeriesLoadResponse) this.L$0;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                tvSeriesLoadResponse.setPosterUrl(this.$details.getPosterPath());
                tvSeriesLoadResponse.setBackgroundPosterUrl(this.$details.getBackdropPath());
                tvSeriesLoadResponse.setYear(StringsKt.toIntOrNull(StringsKt.substringBefore$default(this.$details.getFirstAirDate(), "-", (String) null, 2, (Object) null)));
                tvSeriesLoadResponse.setPlot(this.$details.getOverview());
                List<DoFlixProvider.TMDBGenre> genres = this.$details.getGenres();
                ArrayList arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(genres, 10));
                Iterator<T> it = genres.iterator();
                while (it.hasNext()) {
                    arrayList2.add(((DoFlixProvider.TMDBGenre) it.next()).getName());
                }
                tvSeriesLoadResponse.setTags(arrayList2);
                List<DoFlixProvider.CastMember> take2 = CollectionsKt.take(this.$details.getCredits().getCast(), 10);
                ArrayList arrayList3 = new ArrayList(CollectionsKt.collectionSizeOrDefault(take2, 10));
                for (DoFlixProvider.CastMember castMember : take2) {
                    arrayList3.add(new ActorData(new Actor(castMember.getName(), castMember.getProfilePath()), (ActorRole) null, castMember.getCharacter(), (Actor) null, 10, (DefaultConstructorMarker) null));
                }
                tvSeriesLoadResponse.setActors(arrayList3);
                DoFlixProvider.SimilarResults similar = this.$details.getSimilar();
                if (similar == null || (results = similar.getResults()) == null || (take = CollectionsKt.take(results, 10)) == null) {
                    arrayList = null;
                } else {
                    List<DoFlixProvider.MovieResult> list = take;
                    DoFlixProvider doFlixProvider = this.this$0;
                    ArrayList arrayList4 = new ArrayList(CollectionsKt.collectionSizeOrDefault(list, 10));
                    for (DoFlixProvider.MovieResult movieResult : list) {
                        DoFlixProvider doFlixProvider2 = doFlixProvider;
                        String name = movieResult.getName();
                        if (name == null && (name = movieResult.getTitle()) == null) {
                            name = "Unknown";
                        }
                        arrayList4.add(MainAPIKt.newTvSeriesSearchResponse$default(doFlixProvider2, name, "tvseries," + movieResult.getId(), TvType.TvSeries, false, new DoFlixProvider$load$5$$ExternalSyntheticLambda0(movieResult), 8, (Object) null));
                    }
                    arrayList = arrayList4;
                }
                tvSeriesLoadResponse.setRecommendations(arrayList);
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
