package com.cncverse;

import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import kotlin.Metadata;
import kotlin.jvm.internal.Ref;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MlsbdProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MlsbdProvider$fetchViaWebView$2$1$3 implements View.OnKeyListener {
    final /* synthetic */ FrameLayout $container;
    final /* synthetic */ int $cursorSize;
    final /* synthetic */ View $cursorView;
    final /* synthetic */ Ref.FloatRef $cursorX;
    final /* synthetic */ Ref.FloatRef $cursorY;
    final /* synthetic */ float $dp;
    final /* synthetic */ WebView $wv;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MlsbdProvider$fetchViaWebView$2$1$3(float f, Ref.FloatRef floatRef, FrameLayout frameLayout, Ref.FloatRef floatRef2, View view, int i, WebView webView) {
        this.$dp = f;
        this.$cursorX = floatRef;
        this.$container = frameLayout;
        this.$cursorY = floatRef2;
        this.$cursorView = view;
        this.$cursorSize = i;
        this.$wv = webView;
    }

    @Override // android.view.View.OnKeyListener
    public final boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() != 0) {
            return false;
        }
        float f = this.$dp * 10.0f;
        switch (i) {
            case 19:
                MlsbdProvider$fetchViaWebView$2.invokeSuspend$lambda$0$moveCursor(this.$cursorX, this.$container, this.$cursorY, this.$cursorView, this.$cursorSize, 0.0f, -f);
                return true;
            case 20:
                MlsbdProvider$fetchViaWebView$2.invokeSuspend$lambda$0$moveCursor(this.$cursorX, this.$container, this.$cursorY, this.$cursorView, this.$cursorSize, 0.0f, f);
                return true;
            case 21:
                MlsbdProvider$fetchViaWebView$2.invokeSuspend$lambda$0$moveCursor(this.$cursorX, this.$container, this.$cursorY, this.$cursorView, this.$cursorSize, -f, 0.0f);
                return true;
            case 22:
                MlsbdProvider$fetchViaWebView$2.invokeSuspend$lambda$0$moveCursor(this.$cursorX, this.$container, this.$cursorY, this.$cursorView, this.$cursorSize, f, 0.0f);
                return true;
            case 23:
            case 66:
                MlsbdProvider$fetchViaWebView$2.invokeSuspend$lambda$0$clickAtCursor(this.$cursorX, this.$cursorY, this.$wv);
                return true;
            default:
                return false;
        }
    }
}
