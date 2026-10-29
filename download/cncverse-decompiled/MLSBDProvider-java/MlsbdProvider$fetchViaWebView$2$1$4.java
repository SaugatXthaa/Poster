package com.cncverse;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.Ref;
import org.jsoup.nodes.Document;

/* compiled from: MlsbdProvider.kt */
@Metadata(d1 = {"\u0000\u001d\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000*\u0001\u0000\b\n\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016¨\u0006\b"}, d2 = {"com/cncverse/MlsbdProvider$fetchViaWebView$2$1$4", "Landroid/webkit/WebViewClient;", "onPageFinished", "", "view", "Landroid/webkit/WebView;", "pageUrl", "", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MlsbdProvider$fetchViaWebView$2$1$4 extends WebViewClient {
    final /* synthetic */ Continuation<Document> $cont;
    final /* synthetic */ CookieManager $cookieManager;
    final /* synthetic */ Context $ctx;
    final /* synthetic */ Ref.ObjectRef<AlertDialog> $dialog;
    final /* synthetic */ WindowManager.LayoutParams $params;
    final /* synthetic */ Ref.BooleanRef $resolved;
    final /* synthetic */ String $url;
    final /* synthetic */ LinearLayout $wrapper;
    final /* synthetic */ WebView $wv;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MlsbdProvider$fetchViaWebView$2$1$4(Ref.BooleanRef booleanRef, WebView webView, CookieManager cookieManager, String str, Ref.ObjectRef<AlertDialog> objectRef, Context context, LinearLayout linearLayout, WindowManager.LayoutParams layoutParams, Continuation<? super Document> continuation, MlsbdProvider mlsbdProvider) {
        this.$resolved = booleanRef;
        this.$wv = webView;
        this.$cookieManager = cookieManager;
        this.$url = str;
        this.$dialog = objectRef;
        this.$ctx = context;
        this.$wrapper = linearLayout;
        this.$params = layoutParams;
        this.$cont = continuation;
        this.this$0 = mlsbdProvider;
    }

    @Override // android.webkit.WebViewClient
    public void onPageFinished(WebView view, String pageUrl) {
        super.onPageFinished(view, pageUrl);
        MlsbdProvider$fetchViaWebView$2.invokeSuspend$lambda$0$checkAndFinish(this.$resolved, this.$wv, this.$cookieManager, this.$url, this.$dialog, this.$ctx, this.$wrapper, this.$params, this.$cont, this.this$0, pageUrl);
        if (this.$resolved.element) {
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(new MlsbdProvider$fetchViaWebView$2$1$4$onPageFinished$1(this.$resolved, this.$wv, pageUrl, this.$cookieManager, this.$url, this.$dialog, this.$ctx, this.$wrapper, this.$params, this.$cont, this.this$0), 1000L);
    }
}
