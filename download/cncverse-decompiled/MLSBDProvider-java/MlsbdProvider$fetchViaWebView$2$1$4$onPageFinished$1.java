package com.cncverse;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.Ref;
import org.jsoup.nodes.Document;

/* compiled from: MlsbdProvider.kt */
@Metadata(d1 = {"\u0000\u0011\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000*\u0001\u0000\b\n\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H\u0016¨\u0006\u0004"}, d2 = {"com/cncverse/MlsbdProvider$fetchViaWebView$2$1$4$onPageFinished$1", "Ljava/lang/Runnable;", "run", "", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MlsbdProvider$fetchViaWebView$2$1$4$onPageFinished$1 implements Runnable {
    final /* synthetic */ Continuation<Document> $cont;
    final /* synthetic */ CookieManager $cookieManager;
    final /* synthetic */ Context $ctx;
    final /* synthetic */ Ref.ObjectRef<AlertDialog> $dialog;
    final /* synthetic */ String $pageUrl;
    final /* synthetic */ WindowManager.LayoutParams $params;
    final /* synthetic */ Ref.BooleanRef $resolved;
    final /* synthetic */ String $url;
    final /* synthetic */ LinearLayout $wrapper;
    final /* synthetic */ WebView $wv;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MlsbdProvider$fetchViaWebView$2$1$4$onPageFinished$1(Ref.BooleanRef booleanRef, WebView webView, String str, CookieManager cookieManager, String str2, Ref.ObjectRef<AlertDialog> objectRef, Context context, LinearLayout linearLayout, WindowManager.LayoutParams layoutParams, Continuation<? super Document> continuation, MlsbdProvider mlsbdProvider) {
        this.$resolved = booleanRef;
        this.$wv = webView;
        this.$pageUrl = str;
        this.$cookieManager = cookieManager;
        this.$url = str2;
        this.$dialog = objectRef;
        this.$ctx = context;
        this.$wrapper = linearLayout;
        this.$params = layoutParams;
        this.$cont = continuation;
        this.this$0 = mlsbdProvider;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.$resolved.element) {
            return;
        }
        Ref.BooleanRef booleanRef = this.$resolved;
        WebView webView = this.$wv;
        CookieManager cookieManager = this.$cookieManager;
        String str = this.$url;
        Ref.ObjectRef<AlertDialog> objectRef = this.$dialog;
        Context context = this.$ctx;
        LinearLayout linearLayout = this.$wrapper;
        WindowManager.LayoutParams layoutParams = this.$params;
        Continuation<Document> continuation = this.$cont;
        MlsbdProvider mlsbdProvider = this.this$0;
        String url = this.$wv.getUrl();
        if (url == null) {
            url = this.$pageUrl;
        }
        MlsbdProvider$fetchViaWebView$2.invokeSuspend$lambda$0$checkAndFinish(booleanRef, webView, cookieManager, str, objectRef, context, linearLayout, layoutParams, continuation, mlsbdProvider, url);
        if (this.$resolved.element) {
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(this, 1000L);
    }
}
