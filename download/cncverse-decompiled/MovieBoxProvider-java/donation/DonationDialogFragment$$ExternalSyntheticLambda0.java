package com.cncverse.donation;

import android.animation.ValueAnimator;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.tools.r8.annotations.SynthesizedClassV2;

/* compiled from: D8$$SyntheticClass */
@SynthesizedClassV2(apiLevel = -2, kind = 19, versionHash = "b849e8a9f6cceff267251a73644faacc801ad726cc8f22a9c323c56a203f5446")
/* loaded from: classes.dex */
public final /* synthetic */ class DonationDialogFragment$$ExternalSyntheticLambda0 implements ValueAnimator.AnimatorUpdateListener {
    public final /* synthetic */ LinearLayout.LayoutParams f$0;
    public final /* synthetic */ LinearLayout.LayoutParams f$1;
    public final /* synthetic */ int f$2;
    public final /* synthetic */ TextView f$3;
    public final /* synthetic */ String f$4;
    public final /* synthetic */ DonationDialogFragment f$5;
    public final /* synthetic */ LinearLayout f$6;

    public /* synthetic */ DonationDialogFragment$$ExternalSyntheticLambda0(LinearLayout.LayoutParams layoutParams, LinearLayout.LayoutParams layoutParams2, int i, TextView textView, String str, DonationDialogFragment donationDialogFragment, LinearLayout linearLayout) {
        this.f$0 = layoutParams;
        this.f$1 = layoutParams2;
        this.f$2 = i;
        this.f$3 = textView;
        this.f$4 = str;
        this.f$5 = donationDialogFragment;
        this.f$6 = linearLayout;
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        DonationDialogFragment.onCreateView$lambda$25$0$0(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, valueAnimator);
    }
}
