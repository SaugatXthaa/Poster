package com.cncverse;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import kotlin.Metadata;
import kotlin.jvm.internal.Ref;

/* compiled from: MlsbdProvider.kt */
@Metadata(d1 = {"\u0000\u0011\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000*\u0001\u0000\b\n\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H\u0016¨\u0006\u0004"}, d2 = {"com/cncverse/MlsbdProvider$fetchViaWebView$2$1$2", "Landroid/view/ViewTreeObserver$OnGlobalLayoutListener;", "onGlobalLayout", "", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MlsbdProvider$fetchViaWebView$2$1$2 implements ViewTreeObserver.OnGlobalLayoutListener {
    final /* synthetic */ FrameLayout $container;
    final /* synthetic */ int $cursorSize;
    final /* synthetic */ View $cursorView;
    final /* synthetic */ Ref.FloatRef $cursorX;
    final /* synthetic */ Ref.FloatRef $cursorY;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MlsbdProvider$fetchViaWebView$2$1$2(FrameLayout frameLayout, Ref.FloatRef floatRef, Ref.FloatRef floatRef2, View view, int i) {
        this.$container = frameLayout;
        this.$cursorX = floatRef;
        this.$cursorY = floatRef2;
        this.$cursorView = view;
        this.$cursorSize = i;
    }

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
        this.$container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        this.$cursorX.element = this.$container.getWidth() / 2.0f;
        this.$cursorY.element = this.$container.getHeight() / 2.0f;
        this.$cursorView.setTranslationX(this.$cursorX.element - (this.$cursorSize / 2.0f));
        this.$cursorView.setTranslationY(this.$cursorY.element - (this.$cursorSize / 2.0f));
    }
}
