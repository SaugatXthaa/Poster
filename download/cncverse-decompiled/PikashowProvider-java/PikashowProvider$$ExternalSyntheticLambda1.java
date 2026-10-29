package com.cncverse;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import com.cncverse.PikashowProvider;
import com.lagradost.cloudstream3.MovieSearchResponse;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class PikashowProvider$$ExternalSyntheticLambda1 implements Function1 {
    public final /* synthetic */ PikashowProvider.PikashowMovie f$0;
    public final /* synthetic */ PikashowProvider f$1;

    public /* synthetic */ PikashowProvider$$ExternalSyntheticLambda1(PikashowProvider.PikashowMovie pikashowMovie, PikashowProvider pikashowProvider) {
        this.f$0 = pikashowMovie;
        this.f$1 = pikashowProvider;
    }

    public final Object invoke(Object obj) {
        Unit mainPage$lambda$0$1$0$0;
        mainPage$lambda$0$1$0$0 = PikashowProvider.getMainPage$lambda$0$1$0$0(this.f$0, this.f$1, (MovieSearchResponse) obj);
        return mainPage$lambda$0$1$0$0;
    }
}
