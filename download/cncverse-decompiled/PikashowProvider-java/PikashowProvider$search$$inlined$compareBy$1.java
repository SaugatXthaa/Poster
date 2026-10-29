package com.cncverse;

import com.lagradost.cloudstream3.SearchResponse;
import java.util.Comparator;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.comparisons.ComparisonsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;

/* compiled from: Comparisons.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nComparisons.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Comparisons.kt\nkotlin/comparisons/ComparisonsKt__ComparisonsKt$compareBy$2\n+ 2 PikashowProvider.kt\ncom/cncverse/PikashowProvider\n*L\n1#1,328:1\n425#2,11:329\n*E\n"})
/* loaded from: classes.dex */
public final class PikashowProvider$search$$inlined$compareBy$1<T> implements Comparator {
    final /* synthetic */ String $searchQuery$inlined;

    public PikashowProvider$search$$inlined$compareBy$1(String str) {
        this.$searchQuery$inlined = str;
    }

    @Override // java.util.Comparator
    public final int compare(T t, T t2) {
        String lowerCase = ((SearchResponse) t).getName().toLowerCase(Locale.ROOT);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
        Integer num = Intrinsics.areEqual(lowerCase, this.$searchQuery$inlined) ? (Comparable) 0 : StringsKt.startsWith$default(lowerCase, this.$searchQuery$inlined, false, 2, (Object) null) ? (Comparable) 1 : StringsKt.contains$default(lowerCase, this.$searchQuery$inlined, false, 2, (Object) null) ? (Comparable) 2 : (Comparable) 3;
        String lowerCase2 = ((SearchResponse) t2).getName().toLowerCase(Locale.ROOT);
        Intrinsics.checkNotNullExpressionValue(lowerCase2, "toLowerCase(...)");
        return ComparisonsKt.compareValues(num, Intrinsics.areEqual(lowerCase2, this.$searchQuery$inlined) ? (Comparable) 0 : StringsKt.startsWith$default(lowerCase2, this.$searchQuery$inlined, false, 2, (Object) null) ? (Comparable) 1 : StringsKt.contains$default(lowerCase2, this.$searchQuery$inlined, false, 2, (Object) null) ? (Comparable) 2 : (Comparable) 3);
    }
}
