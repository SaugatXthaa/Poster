package com.cncverse;

import com.android.tools.r8.annotations.SynthesizedClassV2;
import kotlin.jvm.functions.Function0;
import okhttp3.OkHttpClient;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class DoFlixProvider$$ExternalSyntheticLambda8 implements Function0 {
    public final /* synthetic */ DoFlixProvider f$0;

    public /* synthetic */ DoFlixProvider$$ExternalSyntheticLambda8(DoFlixProvider doFlixProvider) {
        this.f$0 = doFlixProvider;
    }

    public final Object invoke() {
        OkHttpClient customClient_delegate$lambda$0;
        customClient_delegate$lambda$0 = DoFlixProvider.customClient_delegate$lambda$0(this.f$0);
        return customClient_delegate$lambda$0;
    }
}
