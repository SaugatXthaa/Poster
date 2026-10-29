package com.cncverse;

import com.lagradost.cloudstream3.SearchResponse;
import java.util.Comparator;
import kotlin.Metadata;
import kotlin.comparisons.ComparisonsKt;
import kotlin.jvm.internal.SourceDebugExtension;

/* compiled from: Comparisons.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nComparisons.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Comparisons.kt\nkotlin/comparisons/ComparisonsKt__ComparisonsKt$thenBy$1\n+ 2 PikashowProvider.kt\ncom/cncverse/PikashowProvider\n*L\n1#1,328:1\n436#2:329\n*E\n"})
/* loaded from: classes.dex */
public final class PikashowProvider$search$$inlined$thenBy$1<T> implements Comparator {
    final /* synthetic */ Comparator $this_thenBy;

    public PikashowProvider$search$$inlined$thenBy$1(Comparator comparator) {
        this.$this_thenBy = comparator;
    }

    @Override // java.util.Comparator
    public final int compare(T t, T t2) {
        int compare = this.$this_thenBy.compare(t, t2);
        return compare != 0 ? compare : ComparisonsKt.compareValues(((SearchResponse) t).getName(), ((SearchResponse) t2).getName());
    }
}
