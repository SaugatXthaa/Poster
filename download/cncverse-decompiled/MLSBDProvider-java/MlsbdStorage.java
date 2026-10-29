package com.cncverse;

import android.content.Context;
import android.content.SharedPreferences;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MlsbdStorage.kt */
@Metadata(d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0002\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\n\u0010\n\u001a\u0004\u0018\u00010\u0005H\u0002J\u000e\u0010\u000b\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\rJ\u0014\u0010\u000e\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\r\u0012\u0004\u0012\u00020\u00100\u000fJ\u0006\u0010\u0011\u001a\u00020\u0007R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u0012"}, d2 = {"Lcom/cncverse/MlsbdStorage;", "", "<init>", "()V", "prefs", "Landroid/content/SharedPreferences;", "init", "", "context", "Landroid/content/Context;", "getPrefs", "saveCfCookie", "cookie", "", "getCfCookie", "Lkotlin/Pair;", "", "clearCfCookie", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MlsbdStorage {

    @NotNull
    public static final MlsbdStorage INSTANCE = new MlsbdStorage();

    @Nullable
    private static SharedPreferences prefs;

    private MlsbdStorage() {
    }

    private final SharedPreferences getPrefs() {
        if (prefs == null && MlsbdProvider.INSTANCE.getAppContext() != null) {
            Context appContext = MlsbdProvider.INSTANCE.getAppContext();
            Intrinsics.checkNotNull(appContext);
            init(appContext);
        }
        return prefs;
    }

    public final void clearCfCookie() {
        SharedPreferences.Editor edit;
        SharedPreferences prefs2 = getPrefs();
        if (prefs2 == null || (edit = prefs2.edit()) == null) {
            return;
        }
        edit.remove("cf_clearance");
        edit.remove("cf_clearance_timestamp");
        edit.apply();
    }

    @NotNull
    public final Pair<String, Long> getCfCookie() {
        SharedPreferences prefs2 = getPrefs();
        return new Pair<>(prefs2 != null ? prefs2.getString("cf_clearance", null) : null, Long.valueOf(prefs2 != null ? prefs2.getLong("cf_clearance_timestamp", 0L) : 0L));
    }

    public final void init(@NotNull Context context) {
        if (prefs == null) {
            prefs = context.getApplicationContext().getSharedPreferences("MlsbdPrefs", 0);
        }
    }

    public final void saveCfCookie(@NotNull String cookie) {
        SharedPreferences.Editor edit;
        SharedPreferences prefs2 = getPrefs();
        if (prefs2 == null || (edit = prefs2.edit()) == null) {
            return;
        }
        edit.putString("cf_clearance", cookie);
        edit.putLong("cf_clearance_timestamp", System.currentTimeMillis());
        edit.apply();
    }
}
