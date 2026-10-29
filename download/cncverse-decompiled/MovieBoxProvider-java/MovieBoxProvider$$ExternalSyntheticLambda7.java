package com.cncverse;

import android.net.Uri;
import com.android.tools.r8.annotations.SynthesizedClassV2;
import kotlin.jvm.functions.Function1;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class MovieBoxProvider$$ExternalSyntheticLambda7 implements Function1 {
    public final /* synthetic */ Uri f$0;

    public /* synthetic */ MovieBoxProvider$$ExternalSyntheticLambda7(Uri uri) {
        this.f$0 = uri;
    }

    public final Object invoke(Object obj) {
        CharSequence buildCanonicalString$lambda$0;
        buildCanonicalString$lambda$0 = MovieBoxProvider.buildCanonicalString$lambda$0(this.f$0, (String) obj);
        return buildCanonicalString$lambda$0;
    }
}
