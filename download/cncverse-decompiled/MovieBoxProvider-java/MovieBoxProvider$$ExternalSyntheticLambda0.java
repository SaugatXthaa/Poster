package com.cncverse;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import com.fasterxml.jackson.databind.JsonNode;
import com.lagradost.cloudstream3.MovieSearchResponse;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class MovieBoxProvider$$ExternalSyntheticLambda0 implements Function1 {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ JsonNode f$1;

    public /* synthetic */ MovieBoxProvider$$ExternalSyntheticLambda0(String str, JsonNode jsonNode) {
        this.f$0 = str;
        this.f$1 = jsonNode;
    }

    public final Object invoke(Object obj) {
        Unit mainPage$lambda$1$0;
        mainPage$lambda$1$0 = MovieBoxProvider.getMainPage$lambda$1$0(this.f$0, this.f$1, (MovieSearchResponse) obj);
        return mainPage$lambda$1$0;
    }
}
