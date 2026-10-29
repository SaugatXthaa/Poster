package com.cncverse;

import android.app.AlertDialog;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.Ref;
import kotlin.text.StringsKt;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MlsbdProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MlsbdProvider$fetchViaWebView$2$1$checkAndFinish$1<T> implements ValueCallback {
    final /* synthetic */ Continuation<Document> $cont;
    final /* synthetic */ Ref.ObjectRef<AlertDialog> $dialog;
    final /* synthetic */ Ref.BooleanRef $resolved;
    final /* synthetic */ String $url;
    final /* synthetic */ WebView $wv;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MlsbdProvider$fetchViaWebView$2$1$checkAndFinish$1(Ref.BooleanRef booleanRef, MlsbdProvider mlsbdProvider, Ref.ObjectRef<AlertDialog> objectRef, WebView webView, String str, Continuation<? super Document> continuation) {
        this.$resolved = booleanRef;
        this.this$0 = mlsbdProvider;
        this.$dialog = objectRef;
        this.$wv = webView;
        this.$url = str;
        this.$cont = continuation;
    }

    @Override // android.webkit.ValueCallback
    public final void onReceiveValue(String str) {
        String unescapeJsString;
        if (this.$resolved.element) {
            return;
        }
        unescapeJsString = this.this$0.unescapeJsString(str == null ? "" : str);
        if (!(unescapeJsString.length() > 0) || StringsKt.contains(unescapeJsString, "Just a moment", true)) {
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
        Document parse = Jsoup.parse(unescapeJsString, this.$url);
        Continuation<Document> continuation = this.$cont;
        Result.Companion companion = Result.Companion;
        continuation.resumeWith(Result.constructor-impl(parse));
    }
}
