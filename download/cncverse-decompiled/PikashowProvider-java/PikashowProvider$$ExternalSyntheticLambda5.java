package com.cncverse;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import com.lagradost.cloudstream3.Episode;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class PikashowProvider$$ExternalSyntheticLambda5 implements Function1 {
    public final /* synthetic */ int f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ PikashowProvider$$ExternalSyntheticLambda5(int i, int i2) {
        this.f$0 = i;
        this.f$1 = i2;
    }

    public final Object invoke(Object obj) {
        Unit load$lambda$1$0$0;
        load$lambda$1$0$0 = PikashowProvider.load$lambda$1$0$0(this.f$0, this.f$1, (Episode) obj);
        return load$lambda$1$0$0;
    }
}
