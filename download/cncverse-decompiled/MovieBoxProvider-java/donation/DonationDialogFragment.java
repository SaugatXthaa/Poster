package com.cncverse.donation;

import android.R;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DonationDialogFragment.kt */
@Metadata(d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u0000 \u00142\u00020\u0001:\u0001\u0014B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016J\b\u0010\n\u001a\u00020\u0007H\u0016J$\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016J\u0010\u0010\u0011\u001a\u00020\u00072\u0006\u0010\u0012\u001a\u00020\u0013H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.¢\u0006\u0002\n\u0000¨\u0006\u0015"}, d2 = {"Lcom/cncverse/donation/DonationDialogFragment;", "Landroidx/fragment/app/DialogFragment;", "<init>", "()V", "config", "Lcom/cncverse/donation/DonationConfig;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onStart", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "openUrl", "url", "", "Companion", "MovieBoxProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nDonationDialogFragment.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DonationDialogFragment.kt\ncom/cncverse/donation/DonationDialogFragment\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,467:1\n1#2:468\n1924#3,3:469\n1915#3,2:472\n*S KotlinDebug\n*F\n+ 1 DonationDialogFragment.kt\ncom/cncverse/donation/DonationDialogFragment\n*L\n196#1:469,3\n359#1:472,2\n*E\n"})
/* loaded from: classes.dex */
public final class DonationDialogFragment extends DialogFragment {

    /* renamed from: Companion, reason: from kotlin metadata */
    @NotNull
    public static final Companion INSTANCE = new Companion(null);
    private DonationConfig config;

    /* compiled from: DonationDialogFragment.kt */
    @Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007¨\u0006\b"}, d2 = {"Lcom/cncverse/donation/DonationDialogFragment$Companion;", "", "<init>", "()V", "newInstance", "Lcom/cncverse/donation/DonationDialogFragment;", "config", "Lcom/cncverse/donation/DonationConfig;", "MovieBoxProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
    /* loaded from: classes.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final DonationDialogFragment newInstance(@NotNull DonationConfig config) {
            DonationDialogFragment donationDialogFragment = new DonationDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("enabled", config.getEnabled());
            bundle.putString("extensionName", config.getExtensionName());
            bundle.putString("month", config.getMonth());
            bundle.putString("title", config.getTitle());
            bundle.putString("currency", config.getCurrency());
            bundle.putDouble("targetAmount", config.getTargetAmount());
            bundle.putDouble("currentAmount", config.getCurrentAmount());
            bundle.putInt("supportersCount", config.getSupportersCount());
            bundle.putInt("apiPercent", config.getApiPercent());
            bundle.putString("primaryDonateUrl", config.getPrimaryDonateUrl());
            bundle.putString("primaryButtonText", config.getPrimaryButtonText());
            bundle.putString("secondaryDonateUrl", config.getSecondaryDonateUrl());
            bundle.putString("secondaryButtonText", config.getSecondaryButtonText());
            bundle.putString("adSupportUrl", config.getAdSupportUrl());
            bundle.putString("adSupportButtonText", config.getAdSupportButtonText());
            donationDialogFragment.setArguments(bundle);
            return donationDialogFragment;
        }
    }

    private static final TextView onCreateView$bullet(Context context, float f, String str) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("✦  " + str);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#A855F7")), 0, 1, 33);
        TextView textView = new TextView(context);
        textView.setText(spannableStringBuilder);
        textView.setTextColor(Color.parseColor("#E4DAFA"));
        textView.setTextSize(13.0f);
        textView.setLineSpacing(0.0f, 1.3f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.bottomMargin = onCreateView$dp(f, 5.0f);
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    private static final int onCreateView$dp(float f, float f2) {
        return (int) (f2 * f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final String onCreateView$lambda$16$0(DonationDialogFragment donationDialogFragment) {
        DonationConfig donationConfig = donationDialogFragment.config;
        if (donationConfig == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
            donationConfig = null;
        }
        String format = String.format("%.2f", Arrays.copyOf(new Object[]{Double.valueOf(donationConfig.getTargetAmount())}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreateView$lambda$2$1(View view) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreateView$lambda$25(int i, LinearLayout.LayoutParams layoutParams, LinearLayout.LayoutParams layoutParams2, TextView textView, String str, DonationDialogFragment donationDialogFragment, LinearLayout linearLayout) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, i);
        ofFloat.setDuration(900L);
        ofFloat.setStartDelay(150L);
        ofFloat.setInterpolator(new DecelerateInterpolator(1.4f));
        ofFloat.addUpdateListener(new DonationDialogFragment$$ExternalSyntheticLambda0(layoutParams, layoutParams2, i, textView, str, donationDialogFragment, linearLayout));
        ofFloat.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreateView$lambda$25$0$0(LinearLayout.LayoutParams layoutParams, LinearLayout.LayoutParams layoutParams2, int i, TextView textView, String str, DonationDialogFragment donationDialogFragment, LinearLayout linearLayout, ValueAnimator valueAnimator) {
        Object animatedValue = valueAnimator.getAnimatedValue();
        Intrinsics.checkNotNull(animatedValue, "null cannot be cast to non-null type kotlin.Float");
        float floatValue = ((Float) animatedValue).floatValue();
        layoutParams.weight = floatValue;
        layoutParams2.weight = 100.0f - floatValue;
        float f = i <= 0 ? 0.0f : floatValue / i;
        DonationConfig donationConfig = donationDialogFragment.config;
        DonationConfig donationConfig2 = null;
        if (donationConfig == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
            donationConfig = null;
        }
        String format = String.format("%.2f", Arrays.copyOf(new Object[]{Double.valueOf(donationConfig.getCurrentAmount() * f)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        DonationConfig donationConfig3 = donationDialogFragment.config;
        if (donationConfig3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
        } else {
            donationConfig2 = donationConfig3;
        }
        String format2 = String.format("%.2f", Arrays.copyOf(new Object[]{Double.valueOf(donationConfig2.getTargetAmount())}, 1));
        Intrinsics.checkNotNullExpressionValue(format2, "format(...)");
        textView.setText(str + format + " / " + str + format2);
        linearLayout.requestLayout();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreateView$lambda$30$0(DonationDialogFragment donationDialogFragment, View view) {
        donationDialogFragment.dismissAllowingStateLoss();
        DonationConfig donationConfig = donationDialogFragment.config;
        if (donationConfig == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
            donationConfig = null;
        }
        donationDialogFragment.openUrl(donationConfig.getPrimaryDonateUrl());
    }

    private static final TextView onCreateView$pillButton(Context context, float f, DonationDialogFragment donationDialogFragment, String str, String str2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#1E1138"));
        gradientDrawable.setCornerRadius(f * 12.0f);
        gradientDrawable.setStroke(onCreateView$dp(f, 1.0f), Color.parseColor("#5636A0"));
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextColor(Color.parseColor("#D3C4F7"));
        textView.setTextSize(13.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(17);
        textView.setBackground(gradientDrawable);
        textView.setPadding(onCreateView$dp(f, 10.0f), onCreateView$dp(f, 12.0f), onCreateView$dp(f, 10.0f), onCreateView$dp(f, 12.0f));
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setOnClickListener(new DonationDialogFragment$$ExternalSyntheticLambda1(donationDialogFragment, str2));
        return textView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreateView$pillButton$lambda$32$0(DonationDialogFragment donationDialogFragment, String str, View view) {
        donationDialogFragment.dismissAllowingStateLoss();
        donationDialogFragment.openUrl(str);
    }

    private final void openUrl(String url) {
        if (StringsKt.isBlank(url)) {
            return;
        }
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)).addFlags(268435456));
        } catch (Exception e) {
        }
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(1, R.style.Theme.Black.NoTitleBar);
        Bundle requireArguments = requireArguments();
        this.config = new DonationConfig(requireArguments.getBoolean("enabled", true), requireArguments.getString("extensionName", ""), requireArguments.getString("month", "This Month's"), requireArguments.getString("title", "Help Keep CNCVerse Alive"), null, requireArguments.getString("currency", "$"), requireArguments.getDouble("targetAmount", 100.0d), requireArguments.getDouble("currentAmount", 0.0d), requireArguments.getInt("supportersCount", 0), requireArguments.getInt("apiPercent", -1), requireArguments.getString("primaryDonateUrl", "https://cncverse.pages.dev"), requireArguments.getString("primaryButtonText", "Become a Supporter ☕"), requireArguments.getString("secondaryDonateUrl", ""), requireArguments.getString("secondaryButtonText", "⚡  Donate via UPI"), requireArguments.getString("adSupportUrl", ""), requireArguments.getString("adSupportButtonText", "🎬  Watch an Ad to Help"), 0, 65552, null);
    }

    @NotNull
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        GradientDrawable gradientDrawable;
        String str;
        LinearLayout linearLayout;
        Context requireContext = requireContext();
        float f = requireContext.getResources().getDisplayMetrics().density;
        DonationConfig donationConfig = this.config;
        String str2 = "config";
        if (donationConfig == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
            donationConfig = null;
        }
        boolean isGoalAchieved = donationConfig.isGoalAchieved();
        DonationConfig donationConfig2 = this.config;
        if (donationConfig2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
            donationConfig2 = null;
        }
        int coerceIn = RangesKt.coerceIn(donationConfig2.getProgressPercentage(), 0, 100);
        DonationConfig donationConfig3 = this.config;
        if (donationConfig3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
            donationConfig3 = null;
        }
        String currency = donationConfig3.getCurrency();
        DonationConfig donationConfig4 = this.config;
        if (donationConfig4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
            donationConfig4 = null;
        }
        boolean z = !StringsKt.isBlank(donationConfig4.getSecondaryDonateUrl());
        DonationConfig donationConfig5 = this.config;
        if (donationConfig5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("config");
            donationConfig5 = null;
        }
        boolean z2 = !StringsKt.isBlank(donationConfig5.getAdSupportUrl());
        FrameLayout frameLayout = new FrameLayout(requireContext);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        frameLayout.setOnClickListener(new DonationDialogFragment$$ExternalSyntheticLambda2(this));
        GradientDrawable gradientDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#221343"), Color.parseColor("#150C2C"), Color.parseColor("#0C0618")});
        gradientDrawable2.setCornerRadius(24.0f * f);
        gradientDrawable2.setStroke(onCreateView$dp(f, 1.0f), Color.parseColor("#6D28D9"));
        int min = Math.min((int) (requireContext.getResources().getDisplayMetrics().widthPixels * 0.92f), onCreateView$dp(f, 400.0f));
        LinearLayout linearLayout2 = new LinearLayout(requireContext);
        linearLayout2.setOrientation(1);
        linearLayout2.setBackground(gradientDrawable2);
        linearLayout2.setPadding(onCreateView$dp(f, 20.0f), onCreateView$dp(f, 18.0f), onCreateView$dp(f, 20.0f), onCreateView$dp(f, 18.0f));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(min, -2, 17);
        int onCreateView$dp = onCreateView$dp(f, 20.0f);
        layoutParams.setMargins(onCreateView$dp, onCreateView$dp, onCreateView$dp, onCreateView$dp);
        linearLayout2.setLayoutParams(layoutParams);
        linearLayout2.setOnClickListener(new DonationDialogFragment$$ExternalSyntheticLambda3());
        GradientDrawable gradientDrawable3 = new GradientDrawable();
        gradientDrawable3.setColor(Color.parseColor("#2E165B"));
        gradientDrawable3.setCornerRadius(f * 100.0f);
        gradientDrawable3.setStroke(onCreateView$dp(f, 1.0f), Color.parseColor("#7C3AED"));
        TextView textView = new TextView(requireContext);
        textView.setText(isGoalAchieved ? "🎉  GOAL ACHIEVED" : "💜  SUPPORT THE REPO");
        textView.setTextColor(Color.parseColor("#D8C7FD"));
        textView.setTextSize(10.5f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLetterSpacing(0.1f);
        textView.setBackground(gradientDrawable3);
        int onCreateView$dp2 = onCreateView$dp(f, 10.0f);
        int onCreateView$dp3 = onCreateView$dp(f, 5.0f);
        textView.setPadding(onCreateView$dp2, onCreateView$dp3, onCreateView$dp2, onCreateView$dp3);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams2.rightMargin = onCreateView$dp(f, 8.0f);
        textView.setLayoutParams(layoutParams2);
        GradientDrawable gradientDrawable4 = new GradientDrawable();
        gradientDrawable4.setShape(1);
        gradientDrawable4.setColor(Color.parseColor("#241543"));
        TextView textView2 = new TextView(requireContext);
        textView2.setText("✕");
        textView2.setTextColor(Color.parseColor("#9C8BC4"));
        textView2.setTextSize(14.0f);
        textView2.setTypeface(Typeface.DEFAULT_BOLD);
        textView2.setGravity(17);
        textView2.setBackground(gradientDrawable4);
        int onCreateView$dp4 = onCreateView$dp(f, 28.0f);
        textView2.setClickable(true);
        textView2.setFocusable(true);
        textView2.setOnClickListener(new DonationDialogFragment$$ExternalSyntheticLambda4(this));
        textView2.setLayoutParams(new LinearLayout.LayoutParams(onCreateView$dp4, onCreateView$dp4));
        LinearLayout linearLayout3 = new LinearLayout(requireContext);
        linearLayout3.setOrientation(0);
        linearLayout3.setGravity(16);
        linearLayout3.addView(textView);
        View view = new View(requireContext);
        view.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1.0f));
        linearLayout3.addView(view);
        linearLayout3.addView(textView2);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.bottomMargin = onCreateView$dp(f, 16.0f);
        linearLayout3.setLayoutParams(layoutParams3);
        TextView textView3 = new TextView(requireContext);
        textView3.setText(isGoalAchieved ? "You did it! 🎉" : "Keep CNCVerse Alive ⚡");
        textView3.setTextColor(-1);
        textView3.setTextSize(22.0f);
        textView3.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams4.bottomMargin = onCreateView$dp(f, 6.0f);
        textView3.setLayoutParams(layoutParams4);
        TextView textView4 = new TextView(requireContext);
        textView4.setText(isGoalAchieved ? "This month's goal is complete — CNCVerse lives on. Thank you, legend! 💜" : "No ads. No paywalls. Just community power. Your support keeps every extension alive & free.");
        textView4.setTextColor(Color.parseColor("#BCABE3"));
        textView4.setTextSize(13.0f);
        textView4.setLineSpacing(onCreateView$dp(f, 1.0f), 1.0f);
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams5.bottomMargin = onCreateView$dp(f, 14.0f);
        textView4.setLayoutParams(layoutParams5);
        List listOf = CollectionsKt.listOf(new Pair[]{TuplesKt.to("🛠️", "35+ Providers"), TuplesKt.to("🚫", "Zero Ads"), TuplesKt.to("🆓", "100% Free")});
        LinearLayout linearLayout4 = new LinearLayout(requireContext);
        linearLayout4.setOrientation(0);
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams6.bottomMargin = onCreateView$dp(f, 14.0f);
        linearLayout4.setLayoutParams(layoutParams6);
        List list = listOf;
        boolean z3 = false;
        int i = 0;
        Iterator it = list.iterator();
        while (true) {
            List list2 = listOf;
            if (!it.hasNext()) {
                break;
            }
            Object next = it.next();
            int i2 = i + 1;
            if (i < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            Pair pair = (Pair) next;
            String str3 = (String) pair.component1();
            String str4 = (String) pair.component2();
            GradientDrawable gradientDrawable5 = new GradientDrawable();
            List list3 = list;
            gradientDrawable5.setColor(Color.parseColor("#1B0F38"));
            gradientDrawable5.setCornerRadius(12.0f * f);
            boolean z4 = z3;
            gradientDrawable5.setStroke(onCreateView$dp(f, 1.0f), Color.parseColor("#38206B"));
            LinearLayout linearLayout5 = new LinearLayout(requireContext);
            linearLayout5.setOrientation(1);
            linearLayout5.setGravity(17);
            linearLayout5.setBackground(gradientDrawable5);
            Iterator it2 = it;
            int onCreateView$dp5 = onCreateView$dp(f, 10.0f);
            String str5 = str2;
            linearLayout5.setPadding(onCreateView$dp5, onCreateView$dp(f, 8.0f), onCreateView$dp5, onCreateView$dp(f, 8.0f));
            TextView textView5 = new TextView(requireContext);
            textView5.setText(str3);
            textView5.setTextSize(15.0f);
            textView5.setGravity(17);
            linearLayout5.addView(textView5);
            TextView textView6 = new TextView(requireContext);
            textView6.setText(str4);
            textView6.setTextColor(Color.parseColor("#D6C9F5"));
            textView6.setTextSize(11.0f);
            textView6.setTypeface(Typeface.DEFAULT_BOLD);
            textView6.setGravity(17);
            linearLayout5.addView(textView6);
            LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            if (i < CollectionsKt.getLastIndex(list2)) {
                layoutParams7.rightMargin = onCreateView$dp(f, 8.0f);
            }
            linearLayout5.setLayoutParams(layoutParams7);
            linearLayout4.addView(linearLayout5);
            listOf = list2;
            i = i2;
            z3 = z4;
            list = list3;
            it = it2;
            str2 = str5;
        }
        String str6 = str2;
        GradientDrawable gradientDrawable6 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#231243"), Color.parseColor("#150A2E")});
        gradientDrawable6.setCornerRadius(f * 14.0f);
        gradientDrawable6.setStroke(onCreateView$dp(f, 1.0f), Color.parseColor("#4C1D95"));
        LinearLayout linearLayout6 = new LinearLayout(requireContext);
        linearLayout6.setOrientation(1);
        linearLayout6.setBackground(gradientDrawable6);
        int onCreateView$dp6 = onCreateView$dp(f, 14.0f);
        linearLayout6.setPadding(onCreateView$dp6, onCreateView$dp6, onCreateView$dp6, onCreateView$dp6);
        LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams8.bottomMargin = onCreateView$dp(f, 14.0f);
        linearLayout6.setLayoutParams(layoutParams8);
        LinearLayout linearLayout7 = new LinearLayout(requireContext);
        linearLayout7.setOrientation(0);
        LinearLayout.LayoutParams layoutParams9 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams9.bottomMargin = onCreateView$dp(f, 10.0f);
        linearLayout7.setLayoutParams(layoutParams9);
        TextView textView7 = new TextView(requireContext);
        DonationConfig donationConfig6 = this.config;
        if (donationConfig6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException(str6);
            donationConfig6 = null;
        }
        textView7.setText(donationConfig6.getMonth() + " Mission");
        textView7.setTextColor(Color.parseColor("#C4B5FD"));
        textView7.setTextSize(12.5f);
        textView7.setTypeface(Typeface.DEFAULT_BOLD);
        textView7.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        Unit unit = Unit.INSTANCE;
        linearLayout7.addView(textView7);
        TextView textView8 = new TextView(requireContext);
        textView8.setText(currency + "0.00 / " + currency + new DonationDialogFragment$$ExternalSyntheticLambda5(this));
        textView8.setTextColor(-1);
        textView8.setTextSize(12.5f);
        textView8.setTypeface(Typeface.DEFAULT_BOLD);
        textView8.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        linearLayout7.addView(textView8);
        GradientDrawable gradientDrawable7 = new GradientDrawable();
        gradientDrawable7.setColor(Color.parseColor("#2A1450"));
        gradientDrawable7.setCornerRadius(f * 100.0f);
        LinearLayout linearLayout8 = new LinearLayout(requireContext);
        linearLayout8.setBackground(gradientDrawable7);
        linearLayout8.setClipToOutline(true);
        linearLayout8.setWeightSum(100.0f);
        LinearLayout.LayoutParams layoutParams10 = new LinearLayout.LayoutParams(-1, onCreateView$dp(f, 8.0f));
        layoutParams10.bottomMargin = onCreateView$dp(f, 8.0f);
        linearLayout8.setLayoutParams(layoutParams10);
        GradientDrawable gradientDrawable8 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, isGoalAchieved ? new int[]{Color.parseColor("#22C55E"), Color.parseColor("#4ADE80")} : new int[]{Color.parseColor("#7C3AED"), Color.parseColor("#A855F7")});
        gradientDrawable8.setCornerRadius(f * 100.0f);
        LinearLayout.LayoutParams layoutParams11 = new LinearLayout.LayoutParams(0, -1, 0.0f);
        View view2 = new View(requireContext);
        view2.setBackground(gradientDrawable8);
        view2.setLayoutParams(layoutParams11);
        LinearLayout.LayoutParams layoutParams12 = new LinearLayout.LayoutParams(0, -1, 100.0f);
        View view3 = new View(requireContext);
        view3.setLayoutParams(layoutParams12);
        linearLayout8.addView(view2);
        linearLayout8.addView(view3);
        GradientDrawable gradientDrawable9 = new GradientDrawable();
        gradientDrawable9.setColor(Color.parseColor("#3B1D6B"));
        gradientDrawable9.setCornerRadius(f * 100.0f);
        TextView textView9 = new TextView(requireContext);
        textView9.setText(coerceIn + "%");
        textView9.setTextColor(Color.parseColor("#E9D5FF"));
        textView9.setTextSize(11.0f);
        textView9.setTypeface(Typeface.DEFAULT_BOLD);
        textView9.setBackground(gradientDrawable9);
        int onCreateView$dp7 = onCreateView$dp(f, 8.0f);
        int onCreateView$dp8 = onCreateView$dp(f, 3.0f);
        textView9.setPadding(onCreateView$dp7, onCreateView$dp8, onCreateView$dp7, onCreateView$dp8);
        LinearLayout linearLayout9 = new LinearLayout(requireContext);
        linearLayout9.setOrientation(0);
        linearLayout9.setGravity(16);
        TextView textView10 = new TextView(requireContext);
        DonationConfig donationConfig7 = this.config;
        if (donationConfig7 == null) {
            Intrinsics.throwUninitializedPropertyAccessException(str6);
            donationConfig7 = null;
        }
        textView10.setText("👥 " + donationConfig7.getSupportersCount() + " awesome supporters");
        textView10.setTextColor(Color.parseColor("#A78BFA"));
        textView10.setTextSize(11.5f);
        textView10.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout9.addView(textView10);
        linearLayout9.addView(textView9);
        linearLayout6.addView(linearLayout7);
        linearLayout6.addView(linearLayout8);
        linearLayout6.addView(linearLayout9);
        linearLayout8.post(new DonationDialogFragment$$ExternalSyntheticLambda6(coerceIn, layoutParams11, layoutParams12, textView8, currency, this, linearLayout8));
        TextView textView11 = new TextView(requireContext);
        textView11.setText("YOUR SUPPORT POWERS");
        textView11.setTextColor(Color.parseColor("#8B74C9"));
        textView11.setTextSize(10.5f);
        textView11.setTypeface(Typeface.DEFAULT_BOLD);
        textView11.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams layoutParams13 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams13.bottomMargin = onCreateView$dp(f, 8.0f);
        textView11.setLayoutParams(layoutParams13);
        List listOf2 = isGoalAchieved ? CollectionsKt.listOf(new String[]{"Monthly goal complete — repo fully funded!", "Extra love funds new features & extensions", "You're officially a CNCVerse legend 🫡"}) : CollectionsKt.listOf(new String[]{"Weekly fixes for 35+ providers", "Zero ads, zero paywalls, forever", "Faster updates & fresh extensions"});
        LinearLayout linearLayout10 = new LinearLayout(requireContext);
        linearLayout10.setOrientation(1);
        linearLayout10.addView(textView11);
        Iterator it3 = listOf2.iterator();
        while (it3.hasNext()) {
            linearLayout10.addView(onCreateView$bullet(requireContext, f, (String) it3.next()));
            listOf2 = listOf2;
        }
        LinearLayout.LayoutParams layoutParams14 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams14.bottomMargin = onCreateView$dp(f, 16.0f);
        linearLayout10.setLayoutParams(layoutParams14);
        GradientDrawable gradientDrawable10 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#9F67FF"), Color.parseColor("#6D28D9")});
        gradientDrawable10.setCornerRadius(f * 14.0f);
        gradientDrawable10.setStroke(onCreateView$dp(f, 1.0f), Color.parseColor("#B083FF"));
        TextView textView12 = new TextView(requireContext);
        if (isGoalAchieved) {
            gradientDrawable = gradientDrawable10;
        } else {
            DonationConfig donationConfig8 = this.config;
            if (donationConfig8 == null) {
                Intrinsics.throwUninitializedPropertyAccessException(str6);
                donationConfig8 = null;
            }
            gradientDrawable = gradientDrawable10;
            str = "☕  " + donationConfig8.getPrimaryButtonText();
        }
        textView12.setText(str);
        textView12.setTextColor(-1);
        textView12.setTextSize(15.0f);
        textView12.setTypeface(Typeface.DEFAULT_BOLD);
        textView12.setGravity(17);
        textView12.setBackground(gradientDrawable);
        textView12.setPadding(onCreateView$dp(f, 16.0f), onCreateView$dp(f, 14.0f), onCreateView$dp(f, 16.0f), onCreateView$dp(f, 14.0f));
        textView12.setClickable(true);
        textView12.setFocusable(true);
        textView12.setOnClickListener(new DonationDialogFragment$$ExternalSyntheticLambda7(this));
        LinearLayout.LayoutParams layoutParams15 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams15.bottomMargin = onCreateView$dp(f, 10.0f);
        textView12.setLayoutParams(layoutParams15);
        LinearLayout linearLayout11 = new LinearLayout(requireContext);
        linearLayout11.setOrientation(0);
        linearLayout11.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        if (z) {
            DonationConfig donationConfig9 = this.config;
            if (donationConfig9 == null) {
                Intrinsics.throwUninitializedPropertyAccessException(str6);
                donationConfig9 = null;
            }
            String secondaryButtonText = donationConfig9.getSecondaryButtonText();
            DonationConfig donationConfig10 = this.config;
            if (donationConfig10 == null) {
                Intrinsics.throwUninitializedPropertyAccessException(str6);
                donationConfig10 = null;
            }
            TextView onCreateView$pillButton = onCreateView$pillButton(requireContext, f, this, secondaryButtonText, donationConfig10.getSecondaryDonateUrl());
            linearLayout = linearLayout11;
            LinearLayout.LayoutParams layoutParams16 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            if (z2) {
                layoutParams16.rightMargin = onCreateView$dp(f, 8.0f);
            }
            onCreateView$pillButton.setLayoutParams(layoutParams16);
            linearLayout11.addView(onCreateView$pillButton);
        } else {
            linearLayout = linearLayout11;
        }
        if (z2) {
            DonationConfig donationConfig11 = this.config;
            if (donationConfig11 == null) {
                Intrinsics.throwUninitializedPropertyAccessException(str6);
                donationConfig11 = null;
            }
            String adSupportButtonText = donationConfig11.getAdSupportButtonText();
            DonationConfig donationConfig12 = this.config;
            if (donationConfig12 == null) {
                Intrinsics.throwUninitializedPropertyAccessException(str6);
                donationConfig12 = null;
            }
            TextView onCreateView$pillButton2 = onCreateView$pillButton(requireContext, f, this, adSupportButtonText, donationConfig12.getAdSupportUrl());
            onCreateView$pillButton2.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
            linearLayout11.addView(onCreateView$pillButton2);
        }
        linearLayout11.setVisibility((z || z2) ? 0 : 8);
        TextView textView13 = new TextView(requireContext);
        textView13.setText(isGoalAchieved ? "Every bit goes straight into new features 💜" : "No pressure — CNCVerse stays free either way 💜");
        textView13.setTextColor(Color.parseColor("#7E6BAF"));
        textView13.setTextSize(11.0f);
        textView13.setGravity(17);
        LinearLayout.LayoutParams layoutParams17 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams17.topMargin = onCreateView$dp(f, 12.0f);
        textView13.setLayoutParams(layoutParams17);
        linearLayout2.addView(linearLayout3);
        linearLayout2.addView(textView3);
        linearLayout2.addView(textView4);
        linearLayout2.addView(linearLayout4);
        linearLayout2.addView(linearLayout6);
        linearLayout2.addView(linearLayout10);
        linearLayout2.addView(textView12);
        linearLayout2.addView(linearLayout);
        linearLayout2.addView(textView13);
        frameLayout.addView(linearLayout2);
        linearLayout2.setScaleX(0.9f);
        linearLayout2.setScaleY(0.9f);
        linearLayout2.setAlpha(0.0f);
        linearLayout2.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(240L).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        return frameLayout;
    }

    public void onStart() {
        Window window;
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || (window = dialog.getWindow()) == null) {
            return;
        }
        window.setLayout(-1, -1);
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#C2000000")));
        window.setDimAmount(0.0f);
    }
}
