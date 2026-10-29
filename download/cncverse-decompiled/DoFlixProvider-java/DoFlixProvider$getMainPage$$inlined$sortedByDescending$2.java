package com.cncverse;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.comparisons.ComparisonsKt;
import kotlin.jvm.internal.SourceDebugExtension;

/* compiled from: Comparisons.kt */
@Metadata(k = 3, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nComparisons.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Comparisons.kt\nkotlin/comparisons/ComparisonsKt__ComparisonsKt$compareByDescending$1\n+ 2 DoFlixProvider.kt\ncom/cncverse/DoFlixProvider\n*L\n1#1,328:1\n585#2:329\n*E\n"})
/* loaded from: classes.dex */
public final class DoFlixProvider$getMainPage$$inlined$sortedByDescending$2<T> implements Comparator {
    @Override // java.util.Comparator
    public final int compare(T t, T t2) {
        return ComparisonsKt.compareValues(Integer.valueOf(((List) ((Map.Entry) t2).getValue()).size()), Integer.valueOf(((List) ((Map.Entry) t).getValue()).size()));
    }
}
