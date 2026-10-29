package com.cncverse;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import com.lagradost.cloudstream3.Episode;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class MovieBoxProvider$$ExternalSyntheticLambda3 implements Function1 {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ int f$2;
    public final /* synthetic */ String f$3;
    public final /* synthetic */ String f$4;
    public final /* synthetic */ Integer f$5;
    public final /* synthetic */ String f$6;

    public /* synthetic */ MovieBoxProvider$$ExternalSyntheticLambda3(String str, int i, int i2, String str2, String str3, Integer num, String str4) {
        this.f$0 = str;
        this.f$1 = i;
        this.f$2 = i2;
        this.f$3 = str2;
        this.f$4 = str3;
        this.f$5 = num;
        this.f$6 = str4;
    }

    public final Object invoke(Object obj) {
        Unit load$lambda$7$0$5;
        load$lambda$7$0$5 = MovieBoxProvider.load$lambda$7$0$5(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, (Episode) obj);
        return load$lambda$7$0$5;
    }
}
