package com.cncverse;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import com.lagradost.cloudstream3.MovieSearchResponse;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class MlsbdProvider$$ExternalSyntheticLambda1 implements Function1 {
    public final /* synthetic */ String f$0;

    public /* synthetic */ MlsbdProvider$$ExternalSyntheticLambda1(String str) {
        this.f$0 = str;
    }

    public final Object invoke(Object obj) {
        Unit result$lambda$1;
        result$lambda$1 = MlsbdProvider.toResult$lambda$1(this.f$0, (MovieSearchResponse) obj);
        return result$lambda$1;
    }
}
