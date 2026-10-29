package com.cncverse;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lagradost.cloudstream3.ui.settings.Globals;
import java.util.List;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.SafeContinuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.ranges.RangesKt;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;
import org.jsoup.nodes.Document;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MlsbdProvider.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u0004\u0018\u00010\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "Lorg/jsoup/nodes/Document;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.MlsbdProvider$fetchViaWebView$2", f = "MlsbdProvider.kt", i = {}, l = {168}, m = "invokeSuspend", n = {}, nl = {381}, s = {}, v = 2)
@SourceDebugExtension({"SMAP\nMlsbdProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MlsbdProvider.kt\ncom/cncverse/MlsbdProvider$fetchViaWebView$2\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,643:1\n1#2:644\n*E\n"})
/* loaded from: classes.dex */
public final class MlsbdProvider$fetchViaWebView$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Document>, Object> {
    final /* synthetic */ Context $ctx;
    final /* synthetic */ String $url;
    Object L$0;
    Object L$1;
    Object L$2;
    int label;
    final /* synthetic */ MlsbdProvider this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MlsbdProvider$fetchViaWebView$2(Context context, String str, MlsbdProvider mlsbdProvider, Continuation<? super MlsbdProvider$fetchViaWebView$2> continuation) {
        super(2, continuation);
        this.$ctx = context;
        this.$url = str;
        this.this$0 = mlsbdProvider;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void invokeSuspend$lambda$0$checkAndFinish(Ref.BooleanRef booleanRef, WebView webView, CookieManager cookieManager, String str, Ref.ObjectRef<AlertDialog> objectRef, Context context, LinearLayout linearLayout, WindowManager.LayoutParams layoutParams, Continuation<? super Document> continuation, MlsbdProvider mlsbdProvider, String str2) {
        List groupValues;
        if (booleanRef.element) {
            return;
        }
        String title = webView.getTitle();
        if (title == null) {
            title = "";
        }
        String str3 = title;
        boolean z = true;
        if (StringsKt.contains(str3, "Just a moment", true) || StringsKt.contains(str2, "challenge-platform", true) || StringsKt.contains(str3, "Cloudflare", true)) {
            invokeSuspend$lambda$0$showDialogIfNeeded(objectRef, booleanRef, context, linearLayout, layoutParams, webView, continuation);
            return;
        }
        String cookie = cookieManager.getCookie(str);
        String str4 = null;
        MatchResult find$default = Regex.find$default(new Regex("cf_clearance=([^;]+)"), cookie != null ? cookie : "", 0, 2, (Object) null);
        if (find$default != null && (groupValues = find$default.getGroupValues()) != null) {
            str4 = (String) groupValues.get(1);
        }
        String str5 = str4;
        String str6 = str5;
        if (str6 != null && str6.length() != 0) {
            z = false;
        }
        if (!z) {
            MlsbdStorage.INSTANCE.saveCfCookie(str5);
        }
        webView.evaluateJavascript("document.documentElement.outerHTML", new MlsbdProvider$fetchViaWebView$2$1$checkAndFinish$1(booleanRef, mlsbdProvider, objectRef, webView, str, continuation));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void invokeSuspend$lambda$0$clickAtCursor(Ref.FloatRef floatRef, Ref.FloatRef floatRef2, WebView webView) {
        long uptimeMillis = SystemClock.uptimeMillis();
        MotionEvent obtain = MotionEvent.obtain(uptimeMillis, uptimeMillis, 0, floatRef.element, floatRef2.element, 0);
        MotionEvent obtain2 = MotionEvent.obtain(uptimeMillis, 120 + uptimeMillis, 1, floatRef.element, floatRef2.element, 0);
        webView.dispatchTouchEvent(obtain);
        webView.dispatchTouchEvent(obtain2);
        obtain.recycle();
        obtain2.recycle();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void invokeSuspend$lambda$0$moveCursor(Ref.FloatRef floatRef, FrameLayout frameLayout, Ref.FloatRef floatRef2, View view, int i, float f, float f2) {
        floatRef.element = RangesKt.coerceIn(floatRef.element + f, 0.0f, frameLayout.getWidth());
        floatRef2.element = RangesKt.coerceIn(floatRef2.element + f2, 0.0f, frameLayout.getHeight());
        view.setTranslationX(floatRef.element - (i / 2.0f));
        view.setTranslationY(floatRef2.element - (i / 2.0f));
    }

    private static final void invokeSuspend$lambda$0$showDialogIfNeeded(Ref.ObjectRef<AlertDialog> objectRef, Ref.BooleanRef booleanRef, Context context, LinearLayout linearLayout, WindowManager.LayoutParams layoutParams, WebView webView, Continuation<? super Document> continuation) {
        Window window;
        if (objectRef.element != null || booleanRef.element) {
            return;
        }
        objectRef.element = new AlertDialog.Builder(context).setView(linearLayout).setCancelable(true).create();
        AlertDialog alertDialog = (AlertDialog) objectRef.element;
        if (alertDialog != null && (window = alertDialog.getWindow()) != null) {
            window.setBackgroundDrawable(new ColorDrawable(0));
            window.setLayout(layoutParams.width, layoutParams.height);
        }
        AlertDialog alertDialog2 = (AlertDialog) objectRef.element;
        if (alertDialog2 != null) {
            alertDialog2.setOnDismissListener(new MlsbdProvider$fetchViaWebView$2$1$showDialogIfNeeded$2(booleanRef, webView, continuation));
        }
        AlertDialog alertDialog3 = (AlertDialog) objectRef.element;
        if (alertDialog3 != null) {
            alertDialog3.show();
        }
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new MlsbdProvider$fetchViaWebView$2(this.$ctx, this.$url, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Document> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object obj) {
        String str;
        TextView textView;
        int i;
        String str2;
        View view;
        FrameLayout frameLayout;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                Context context = this.$ctx;
                String str3 = this.$url;
                MlsbdProvider mlsbdProvider = this.this$0;
                this.L$0 = context;
                this.L$1 = str3;
                this.L$2 = mlsbdProvider;
                this.label = 1;
                Continuation safeContinuation = new SafeContinuation(IntrinsicsKt.intercepted((Continuation) this));
                Continuation continuation = safeContinuation;
                try {
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.setAcceptCookie(true);
                    WebView webView = new WebView(context);
                    cookieManager.setAcceptThirdPartyCookies(webView, true);
                    WebSettings settings = webView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);
                    settings.setMixedContentMode(0);
                    str = mlsbdProvider.defaultUserAgent;
                    settings.setUserAgentString(str);
                    settings.setMediaPlaybackRequiresUserGesture(false);
                    webView.setWebChromeClient(new WebChromeClient());
                    Ref.BooleanRef booleanRef = new Ref.BooleanRef();
                    Ref.ObjectRef objectRef = new Ref.ObjectRef();
                    float f = context.getResources().getDisplayMetrics().density;
                    Object systemService = context.getSystemService("window");
                    Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.view.WindowManager");
                    WindowManager windowManager = (WindowManager) systemService;
                    windowManager.getDefaultDisplay().getSize(new Point());
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams((int) (r3.x * 0.95f), (int) (r3.y * 0.9f));
                    LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setOrientation(1);
                    linearLayout.setMinimumWidth((int) (r3.x * 0.95f));
                    linearLayout.setMinimumHeight((int) (r3.y * 0.9f));
                    TextView textView2 = new TextView(context);
                    textView2.setText("🔐 Solve the Cloudflare captcha — use D-pad to move cursor, OK to click.");
                    textView2.setTextColor(-1);
                    textView2.setBackgroundColor(Color.parseColor("#1A1A2E"));
                    textView2.setTextSize(13.0f);
                    int i2 = (int) (10 * f);
                    textView2.setPadding(i2, i2, i2, i2);
                    textView2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
                    int i3 = (int) (22 * f);
                    if (Globals.INSTANCE.isLayout(2)) {
                        View view2 = new View(context);
                        textView = textView2;
                        view2.setLayoutParams(new FrameLayout.LayoutParams(i3, i3));
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setShape(1);
                        i = i3;
                        str2 = str3;
                        gradientDrawable.setColor(Color.argb(160, 255, 50, 50));
                        gradientDrawable.setStroke((int) (2 * f), -1);
                        view2.setBackground(gradientDrawable);
                        view2.setElevation(999.0f);
                        view = view2;
                    } else {
                        textView = textView2;
                        i = i3;
                        str2 = str3;
                        view = null;
                    }
                    View view3 = view;
                    FrameLayout frameLayout2 = new FrameLayout(context);
                    LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -1);
                    layoutParams2.weight = 1.0f;
                    frameLayout2.setLayoutParams(layoutParams2);
                    frameLayout2.setFocusable(true);
                    frameLayout2.setFocusableInTouchMode(true);
                    webView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
                    frameLayout2.addView(webView);
                    if (view3 != null) {
                        frameLayout2.addView(view3);
                        Ref.FloatRef floatRef = new Ref.FloatRef();
                        floatRef.element = (r3.x * 0.95f) / 2.0f;
                        Ref.FloatRef floatRef2 = new Ref.FloatRef();
                        floatRef2.element = (r3.y * 0.9f) / 2.0f;
                        frameLayout2.getViewTreeObserver().addOnGlobalLayoutListener(new MlsbdProvider$fetchViaWebView$2$1$2(frameLayout2, floatRef, floatRef2, view3, i));
                        MlsbdProvider$fetchViaWebView$2$1$3 mlsbdProvider$fetchViaWebView$2$1$3 = new MlsbdProvider$fetchViaWebView$2$1$3(f, floatRef, frameLayout2, floatRef2, view3, i, webView);
                        frameLayout = frameLayout2;
                        frameLayout.setOnKeyListener(mlsbdProvider$fetchViaWebView$2$1$3);
                    } else {
                        frameLayout = frameLayout2;
                    }
                    linearLayout.addView(textView);
                    linearLayout.addView(frameLayout);
                    String str4 = str2;
                    webView.setWebViewClient(new MlsbdProvider$fetchViaWebView$2$1$4(booleanRef, webView, cookieManager, str4, objectRef, context, linearLayout, layoutParams, continuation, mlsbdProvider));
                    new Handler(Looper.getMainLooper()).postDelayed(new MlsbdProvider$fetchViaWebView$2$1$5(booleanRef, objectRef, webView, continuation), 120000L);
                    webView.loadUrl(str4);
                } catch (Exception e) {
                    Result.Companion companion = Result.Companion;
                    continuation.resumeWith(Result.constructor-impl((Object) null));
                }
                Object orThrow = safeContinuation.getOrThrow();
                if (orThrow == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                    DebugProbesKt.probeCoroutineSuspended((Continuation) this);
                }
                return orThrow == coroutine_suspended ? coroutine_suspended : orThrow;
            case 1:
                ResultKt.throwOnFailure(obj);
                return obj;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
