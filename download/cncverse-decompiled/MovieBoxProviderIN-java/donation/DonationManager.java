package com.cncverse.donation;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.ranges.RangesKt;
import kotlin.text.Charsets;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.SupervisorKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/* compiled from: DonationManager.kt */
@Metadata(d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u001d\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010H\u001a\u00020I2\u0006\u0010J\u001a\u00020KJ\u000e\u0010L\u001a\u00020I2\u0006\u0010M\u001a\u00020\u0011J\u000e\u0010N\u001a\u00020I2\u0006\u0010M\u001a\u00020\u0011J\u000e\u0010O\u001a\u00020I2\u0006\u0010J\u001a\u00020KJ\u001e\u0010P\u001a\u00020Q2\u0006\u0010R\u001a\u00020:2\u0006\u0010M\u001a\u00020\u0011H\u0082@¢\u0006\u0002\u0010SJ\u0018\u0010T\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010U2\u0006\u0010V\u001a\u00020WH\u0002J\u0016\u0010X\u001a\b\u0012\u0004\u0012\u00020\u00010U2\u0006\u0010V\u001a\u00020WH\u0002J\u0018\u0010Y\u001a\u00020I2\u0006\u0010R\u001a\u00020:2\u0006\u0010Z\u001a\u00020QH\u0002R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001a\u0010\u0010\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u001a\u0010\u0016\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0013\"\u0004\b\u0018\u0010\u0015R\u001a\u0010\u0019\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u0013\"\u0004\b\u001b\u0010\u0015R\u001a\u0010\u001c\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u0013\"\u0004\b\u001e\u0010\u0015R\u001a\u0010\u001f\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b \u0010\u0013\"\u0004\b!\u0010\u0015R\u001a\u0010\"\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b#\u0010\u0013\"\u0004\b$\u0010\u0015R\u001a\u0010%\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\u0013\"\u0004\b'\u0010\u0015R\u001a\u0010(\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b)\u0010\u0013\"\u0004\b*\u0010\u0015R\u001a\u0010+\u001a\u00020\u0011X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b,\u0010\u0013\"\u0004\b-\u0010\u0015R\u001a\u0010.\u001a\u00020/X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b0\u00101\"\u0004\b2\u00103R\u0011\u00104\u001a\u00020\u000b8F¢\u0006\u0006\u001a\u0004\b5\u0010\rR\u000e\u00106\u001a\u000207X\u0082\u0004¢\u0006\u0002\n\u0000R\u0016\u00108\u001a\n\u0012\u0004\u0012\u00020:\u0018\u000109X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010;\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010<\u001a\u00020=X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010>\u001a\u00020=X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010?\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010@\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010A\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010B\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010C\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010D\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010E\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010F\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010G\u001a\u00020\u0011X\u0082T¢\u0006\u0002\n\u0000¨\u0006["}, d2 = {"Lcom/cncverse/donation/DonationManager;", "", "<init>", "()V", "testMode", "", "getTestMode", "()Z", "setTestMode", "(Z)V", "testProgressAmount", "", "getTestProgressAmount", "()D", "setTestProgressAmount", "(D)V", "primaryDonateUrl", "", "getPrimaryDonateUrl", "()Ljava/lang/String;", "setPrimaryDonateUrl", "(Ljava/lang/String;)V", "primaryButtonText", "getPrimaryButtonText", "setPrimaryButtonText", "secondaryDonateUrl", "getSecondaryDonateUrl", "setSecondaryDonateUrl", "secondaryButtonText", "getSecondaryButtonText", "setSecondaryButtonText", "adSupportUrl", "getAdSupportUrl", "setAdSupportUrl", "adSupportButtonText", "getAdSupportButtonText", "setAdSupportButtonText", "currency", "getCurrency", "setCurrency", "goalTitle", "getGoalTitle", "setGoalTitle", "goalDescription", "getGoalDescription", "setGoalDescription", "cooldownHours", "", "getCooldownHours", "()I", "setCooldownHours", "(I)V", "targetAmount", "getTargetAmount", "scope", "Lkotlinx/coroutines/CoroutineScope;", "activityRef", "Ljava/lang/ref/WeakReference;", "Landroidx/appcompat/app/AppCompatActivity;", "gateLock", "lastTriggerMs", "", "GATE_WINDOW_MS", "PREFS", "KEY_LAST_SHOWN_DAY", "KEY_ACHIEVED_MONTH", "KEY_CACHED_MONTH", "KEY_CACHED_AMOUNT", "KEY_CACHED_GOAL", "KEY_CACHED_SUPPORT", "KEY_CACHED_PERCENT", "STATS_URL", "setActivity", "", "context", "Landroid/content/Context;", "checkAndShow", "providerName", "showNow", "resetCooldown", "buildConfig", "Lcom/cncverse/donation/DonationConfig;", "activity", "(Landroidx/appcompat/app/AppCompatActivity;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchStats", "", "prefs", "Landroid/content/SharedPreferences;", "readCache", "showDialog", "config", "MovieBoxProviderIN_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nDonationManager.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DonationManager.kt\ncom/cncverse/donation/DonationManager\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,200:1\n1#2:201\n*E\n"})
/* loaded from: classes.dex */
public final class DonationManager {
    private static final long GATE_WINDOW_MS = 60000;

    @NotNull
    private static final String KEY_ACHIEVED_MONTH = "cncverse_donation_achieved_shown_month";

    @NotNull
    private static final String KEY_CACHED_AMOUNT = "cncverse_donation_cached_amount";

    @NotNull
    private static final String KEY_CACHED_GOAL = "cncverse_donation_cached_goal";

    @NotNull
    private static final String KEY_CACHED_MONTH = "cncverse_donation_cached_month";

    @NotNull
    private static final String KEY_CACHED_PERCENT = "cncverse_donation_cached_percent";

    @NotNull
    private static final String KEY_CACHED_SUPPORT = "cncverse_donation_cached_supporters";

    @NotNull
    private static final String KEY_LAST_SHOWN_DAY = "last_shown_day";

    @NotNull
    private static final String PREFS = "cncverse_donation";

    @NotNull
    private static final String STATS_URL = "https://cncverse.pages.dev/api/stats";

    @Nullable
    private static WeakReference<AppCompatActivity> activityRef;
    private static volatile long lastTriggerMs;
    private static boolean testMode;
    private static double testProgressAmount;

    @NotNull
    public static final DonationManager INSTANCE = new DonationManager();

    @NotNull
    private static String primaryDonateUrl = "https://cncverse.pages.dev";

    @NotNull
    private static String primaryButtonText = "☕ Keep It Alive";

    @NotNull
    private static String secondaryDonateUrl = "";

    @NotNull
    private static String secondaryButtonText = "⚡ Donate via UPI / Other";

    @NotNull
    private static String adSupportUrl = DonationConfig.INSTANCE.decodeOmg10();

    @NotNull
    private static String adSupportButtonText = "🎬 Can't donate? Watch an Ad to Support ↗";

    @NotNull
    private static String currency = "$";

    @NotNull
    private static String goalTitle = "Help Keep CNCVerse Alive";

    @NotNull
    private static String goalDescription = "";
    private static int cooldownHours = 24;

    @NotNull
    private static final CoroutineScope scope = CoroutineScopeKt.CoroutineScope(Dispatchers.getIO().plus(SupervisorKt.SupervisorJob$default((Job) null, 1, (Object) null)));

    @NotNull
    private static final Object gateLock = new Object();

    private DonationManager() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Object buildConfig(AppCompatActivity appCompatActivity, String str, Continuation<? super DonationConfig> continuation) {
        List<Object> fetchStats;
        SharedPreferences sharedPreferences = appCompatActivity.getSharedPreferences(PREFS, 0);
        if (!testMode || testProgressAmount <= 0.0d) {
            fetchStats = fetchStats(sharedPreferences);
            if (fetchStats == null) {
                fetchStats = readCache(sharedPreferences);
            }
        } else {
            fetchStats = CollectionsKt.listOf(new Object[]{Boxing.boxDouble(testProgressAmount), Boxing.boxDouble(getTargetAmount()), Boxing.boxDouble(RangesKt.coerceIn((int) ((testProgressAmount / r9) * 100), 0, 100)), Boxing.boxDouble(12.0d), "September"});
        }
        Object obj = fetchStats.get(0);
        Object obj2 = fetchStats.get(1);
        Object obj3 = fetchStats.get(2);
        Object obj4 = fetchStats.get(3);
        Object obj5 = fetchStats.get(4);
        if (!testMode) {
            sharedPreferences.edit().putString(KEY_LAST_SHOWN_DAY, new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date())).apply();
        }
        Intrinsics.checkNotNull(obj5, "null cannot be cast to non-null type kotlin.String");
        String str2 = goalTitle;
        String str3 = goalDescription;
        String str4 = currency;
        Intrinsics.checkNotNull(obj2, "null cannot be cast to non-null type kotlin.Double");
        double doubleValue = ((Double) obj2).doubleValue();
        Intrinsics.checkNotNull(obj, "null cannot be cast to non-null type kotlin.Double");
        double doubleValue2 = ((Double) obj).doubleValue();
        Intrinsics.checkNotNull(obj4, "null cannot be cast to non-null type kotlin.Double");
        int doubleValue3 = (int) ((Double) obj4).doubleValue();
        Intrinsics.checkNotNull(obj3, "null cannot be cast to non-null type kotlin.Double");
        return new DonationConfig(false, str, (String) obj5, str2, str3, str4, doubleValue, doubleValue2, doubleValue3, (int) ((Double) obj3).doubleValue(), primaryDonateUrl, primaryButtonText, secondaryDonateUrl, secondaryButtonText, adSupportUrl, adSupportButtonText, cooldownHours, 1, null);
    }

    private final List<Object> fetchStats(SharedPreferences prefs) {
        try {
            JSONObject jSONObject = new JSONObject(new String(TextStreamsKt.readBytes(new URL(STATS_URL)), Charsets.UTF_8));
            double optDouble = jSONObject.optDouble("totalUsd", 0.0d);
            double optDouble2 = jSONObject.optDouble("targetGoalUsd", getTargetAmount());
            int optInt = jSONObject.optInt("percent", -1);
            int optInt2 = jSONObject.optInt("supporterCount", 0);
            String optString = jSONObject.optString("month", "Monthly Goal");
            prefs.edit().putFloat(KEY_CACHED_AMOUNT, (float) optDouble).putFloat(KEY_CACHED_GOAL, (float) optDouble2).putInt(KEY_CACHED_SUPPORT, optInt2).putInt(KEY_CACHED_PERCENT, optInt).putString(KEY_CACHED_MONTH, optString).apply();
            return CollectionsKt.listOf(new Object[]{Double.valueOf(optDouble), Double.valueOf(optDouble2), Double.valueOf(optInt), Double.valueOf(optInt2), optString});
        } catch (Exception e) {
            return null;
        }
    }

    private final List<Object> readCache(SharedPreferences prefs) {
        Double valueOf = Double.valueOf(prefs.getFloat(KEY_CACHED_AMOUNT, 0.0f));
        Double valueOf2 = Double.valueOf(prefs.getFloat(KEY_CACHED_GOAL, (float) getTargetAmount()));
        Double valueOf3 = Double.valueOf(prefs.getInt(KEY_CACHED_PERCENT, -1));
        Double valueOf4 = Double.valueOf(prefs.getInt(KEY_CACHED_SUPPORT, 0));
        String string = prefs.getString(KEY_CACHED_MONTH, "Monthly Goal");
        return CollectionsKt.listOf(new Object[]{valueOf, valueOf2, valueOf3, valueOf4, string != null ? string : "Monthly Goal"});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void showDialog(AppCompatActivity activity, DonationConfig config) {
        try {
            if (activity.getSupportFragmentManager().findFragmentByTag("donation_dialog") != null) {
                return;
            }
            DonationDialogFragment.INSTANCE.newInstance(config).show(activity.getSupportFragmentManager(), "donation_dialog");
        } catch (Exception e) {
        }
    }

    public final void checkAndShow(@NotNull String providerName) {
        AppCompatActivity appCompatActivity;
        long currentTimeMillis = System.currentTimeMillis();
        synchronized (gateLock) {
            if (currentTimeMillis - lastTriggerMs < GATE_WINDOW_MS) {
                return;
            }
            lastTriggerMs = currentTimeMillis;
            Unit unit = Unit.INSTANCE;
            WeakReference<AppCompatActivity> weakReference = activityRef;
            if (weakReference == null || (appCompatActivity = weakReference.get()) == null) {
                return;
            }
            if (testMode || !Intrinsics.areEqual(new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()), appCompatActivity.getSharedPreferences(PREFS, 0).getString(KEY_LAST_SHOWN_DAY, ""))) {
                BuildersKt.launch$default(scope, (CoroutineContext) null, (CoroutineStart) null, new DonationManager$checkAndShow$2(appCompatActivity, providerName, null), 3, (Object) null);
            }
        }
    }

    @NotNull
    public final String getAdSupportButtonText() {
        return adSupportButtonText;
    }

    @NotNull
    public final String getAdSupportUrl() {
        return adSupportUrl;
    }

    public final int getCooldownHours() {
        return cooldownHours;
    }

    @NotNull
    public final String getCurrency() {
        return currency;
    }

    @NotNull
    public final String getGoalDescription() {
        return goalDescription;
    }

    @NotNull
    public final String getGoalTitle() {
        return goalTitle;
    }

    @NotNull
    public final String getPrimaryButtonText() {
        return primaryButtonText;
    }

    @NotNull
    public final String getPrimaryDonateUrl() {
        return primaryDonateUrl;
    }

    @NotNull
    public final String getSecondaryButtonText() {
        return secondaryButtonText;
    }

    @NotNull
    public final String getSecondaryDonateUrl() {
        return secondaryDonateUrl;
    }

    public final double getTargetAmount() {
        return DonationConfig.INSTANCE.dynamicTargetAmount();
    }

    public final boolean getTestMode() {
        return testMode;
    }

    public final double getTestProgressAmount() {
        return testProgressAmount;
    }

    public final void resetCooldown(@NotNull Context context) {
        context.getSharedPreferences(PREFS, 0).edit().remove(KEY_LAST_SHOWN_DAY).apply();
    }

    public final void setActivity(@NotNull Context context) {
        AppCompatActivity appCompatActivity = context instanceof AppCompatActivity ? (AppCompatActivity) context : null;
        if (appCompatActivity != null) {
            activityRef = new WeakReference<>(appCompatActivity);
        }
    }

    public final void setAdSupportButtonText(@NotNull String str) {
        adSupportButtonText = str;
    }

    public final void setAdSupportUrl(@NotNull String str) {
        adSupportUrl = str;
    }

    public final void setCooldownHours(int i) {
        cooldownHours = i;
    }

    public final void setCurrency(@NotNull String str) {
        currency = str;
    }

    public final void setGoalDescription(@NotNull String str) {
        goalDescription = str;
    }

    public final void setGoalTitle(@NotNull String str) {
        goalTitle = str;
    }

    public final void setPrimaryButtonText(@NotNull String str) {
        primaryButtonText = str;
    }

    public final void setPrimaryDonateUrl(@NotNull String str) {
        primaryDonateUrl = str;
    }

    public final void setSecondaryButtonText(@NotNull String str) {
        secondaryButtonText = str;
    }

    public final void setSecondaryDonateUrl(@NotNull String str) {
        secondaryDonateUrl = str;
    }

    public final void setTestMode(boolean z) {
        testMode = z;
    }

    public final void setTestProgressAmount(double d) {
        testProgressAmount = d;
    }

    public final void showNow(@NotNull String providerName) {
        AppCompatActivity appCompatActivity;
        WeakReference<AppCompatActivity> weakReference = activityRef;
        if (weakReference == null || (appCompatActivity = weakReference.get()) == null) {
            return;
        }
        BuildersKt.launch$default(scope, (CoroutineContext) null, (CoroutineStart) null, new DonationManager$showNow$1(appCompatActivity, providerName, null), 3, (Object) null);
    }
}
