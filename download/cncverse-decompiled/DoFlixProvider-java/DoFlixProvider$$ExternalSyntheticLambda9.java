package com.cncverse;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import com.cncverse.DoFlixProvider;
import com.lagradost.cloudstream3.Episode;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class DoFlixProvider$$ExternalSyntheticLambda9 implements Function1 {
    public final /* synthetic */ DoFlixProvider.TvEpisode f$0;
    public final /* synthetic */ DoFlixProvider.SeasonInfo f$1;

    public /* synthetic */ DoFlixProvider$$ExternalSyntheticLambda9(DoFlixProvider.TvEpisode tvEpisode, DoFlixProvider.SeasonInfo seasonInfo) {
        this.f$0 = tvEpisode;
        this.f$1 = seasonInfo;
    }

    public final Object invoke(Object obj) {
        Unit load$lambda$1$0;
        load$lambda$1$0 = DoFlixProvider.load$lambda$1$0(this.f$0, this.f$1, (Episode) obj);
        return load$lambda$1$0;
    }
}
