package com.cncverse;

import android.content.DialogInterface;
import android.webkit.WebView;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.Ref;
import org.jsoup.nodes.Document;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: MlsbdProvider.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
/* loaded from: classes.dex */
public final class MlsbdProvider$fetchViaWebView$2$1$showDialogIfNeeded$2 implements DialogInterface.OnDismissListener {
    final /* synthetic */ Continuation<Document> $cont;
    final /* synthetic */ Ref.BooleanRef $resolved;
    final /* synthetic */ WebView $wv;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MlsbdProvider$fetchViaWebView$2$1$showDialogIfNeeded$2(Ref.BooleanRef booleanRef, WebView webView, Continuation<? super Document> continuation) {
        this.$resolved = booleanRef;
        this.$wv = webView;
        this.$cont = continuation;
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public final void onDismiss(DialogInterface dialogInterface) {
        if (this.$resolved.element) {
            return;
        }
        this.$resolved.element = true;
        try {
            this.$wv.destroy();
        } catch (Exception e) {
        }
        Continuation<Document> continuation = this.$cont;
        Result.Companion companion = Result.Companion;
        continuation.resumeWith(Result.constructor-impl((Object) null));
    }
}
