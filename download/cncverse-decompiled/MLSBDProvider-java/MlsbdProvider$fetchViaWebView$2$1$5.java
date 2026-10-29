package com.cncverse;

import android.app.AlertDialog;
import android.webkit.WebView;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.Ref;
import org.jsoup.nodes.Document;

/* compiled from: MlsbdProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
final class MlsbdProvider$fetchViaWebView$2$1$5 implements Runnable {
    final /* synthetic */ Continuation<Document> $cont;
    final /* synthetic */ Ref.ObjectRef<AlertDialog> $dialog;
    final /* synthetic */ Ref.BooleanRef $resolved;
    final /* synthetic */ WebView $wv;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MlsbdProvider$fetchViaWebView$2$1$5(Ref.BooleanRef booleanRef, Ref.ObjectRef<AlertDialog> objectRef, WebView webView, Continuation<? super Document> continuation) {
        this.$resolved = booleanRef;
        this.$dialog = objectRef;
        this.$wv = webView;
        this.$cont = continuation;
    }

    @Override // java.lang.Runnable
    public final void run() {
        if (this.$resolved.element) {
            return;
        }
        this.$resolved.element = true;
        try {
            AlertDialog alertDialog = (AlertDialog) this.$dialog.element;
            if (alertDialog != null) {
                alertDialog.dismiss();
                Unit unit = Unit.INSTANCE;
            }
        } catch (Exception e) {
            Unit unit2 = Unit.INSTANCE;
        }
        try {
            this.$wv.destroy();
        } catch (Exception e2) {
        }
        Continuation<Document> continuation = this.$cont;
        Result.Companion companion = Result.Companion;
        continuation.resumeWith(Result.constructor-impl((Object) null));
    }
}
