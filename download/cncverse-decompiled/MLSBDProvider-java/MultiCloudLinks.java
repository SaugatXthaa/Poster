package com.cncverse;

import com.lagradost.api.Log;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorApi;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0016\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JH\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00052\b\u0010\u0011\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u000f0\u00132\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@¢\u0006\u0002\u0010\u0017R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u0018"}, d2 = {"Lcom/cncverse/MultiCloudLinks;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nExtractors.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Extractors.kt\ncom/cncverse/MultiCloudLinks\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,995:1\n1#2:996\n1915#3,2:997\n*S KotlinDebug\n*F\n+ 1 Extractors.kt\ncom/cncverse/MultiCloudLinks\n*L\n693#1:997,2\n*E\n"})
/* loaded from: classes.dex */
public class MultiCloudLinks extends ExtractorApi {
    private final boolean requiresReferer;

    @NotNull
    private final String name = "MultiCloud";

    @NotNull
    private final String mainUrl = "https://new2.multicloudlinks.com";

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Can't wrap try/catch for region: R(12:342|343|344|345|346|347|348|349|350|351|352|(1:354)(8:355|356|357|358|18|19|20|(0)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(17:323|(1:324)|325|326|327|328|329|330|331|(3:399|400|(4:404|336|(1:390)(1:340)|(12:342|343|344|345|346|347|348|349|350|351|352|(1:354)(8:355|356|357|358|18|19|20|(0)(0)))(5:389|18|19|20|(0)(0))))|333|(3:391|392|(6:394|395|336|(1:338)|390|(0)(0)))|335|336|(0)|390|(0)(0)) */
    /* JADX WARN: Can't wrap try/catch for region: R(18:213|(1:214)|215|216|217|218|219|220|221|(5:260|261|262|263|264)(1:223)|224|225|(1:255)(1:229)|(4:231|232|233|(1:235)(2:236|237))(1:254)|18|19|20|(0)(0)) */
    /* JADX WARN: Can't wrap try/catch for region: R(18:213|214|215|216|217|218|219|220|221|(5:260|261|262|263|264)(1:223)|224|225|(1:255)(1:229)|(4:231|232|233|(1:235)(2:236|237))(1:254)|18|19|20|(0)(0)) */
    /* JADX WARN: Can't wrap try/catch for region: R(30:181|(3:308|309|(9:312|(3:423|424|(9:427|(3:447|448|(6:451|(7:470|471|472|473|474|475|(1:477)(2:478|479))(8:453|454|(1:459)|460|461|462|463|(1:465)(4:466|15|16|17))|18|19|20|(3:507|56|57)(0))(1:450))(1:429)|430|431|432|433|434|435|(1:437)(6:438|439|18|19|20|(0)(0)))(1:426))(1:314)|315|316|317|318|319|320|(1:322)(17:323|324|325|326|327|328|329|330|331|(3:399|400|(4:404|336|(1:390)(1:340)|(12:342|343|344|345|346|347|348|349|350|351|352|(1:354)(8:355|356|357|358|18|19|20|(0)(0)))(5:389|18|19|20|(0)(0))))|333|(3:391|392|(6:394|395|336|(1:338)|390|(0)(0)))|335|336|(0)|390|(0)(0)))(1:311))(1:183)|184|185|186|187|188|189|190|191|192|193|194|195|196|197|198|199|200|201|202|203|204|205|206|207|208|209|210|(1:212)(18:213|214|215|216|217|218|219|220|221|(5:260|261|262|263|264)(1:223)|224|225|(1:255)(1:229)|(4:231|232|233|(1:235)(2:236|237))(1:254)|18|19|20|(0)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(33:87|88|89|90|91|92|93|94|95|(3:158|159|(1:161))|97|98|99|100|101|102|103|104|105|106|107|(1:109)(1:147)|110|111|112|113|114|115|116|117|118|119|(1:121)(8:122|123|124|125|18|19|20|(0)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(4:(9:163|164|132|133|134|18|19|20|(0)(0))|129|130|131) */
    /* JADX WARN: Can't wrap try/catch for region: R(5:260|261|262|263|264) */
    /* JADX WARN: Can't wrap try/catch for region: R(7:(1:470)|471|472|473|474|475|(1:477)(2:478|479)) */
    /* JADX WARN: Can't wrap try/catch for region: R(7:1|(2:3|(4:5|6|7|8))|637|6|7|8|(1:(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(8:75|(1:496)(2:77|(30:181|(3:308|309|(9:312|(3:423|424|(9:427|(3:447|448|(6:451|(7:470|471|472|473|474|475|(1:477)(2:478|479))(8:453|454|(1:459)|460|461|462|463|(1:465)(4:466|15|16|17))|18|19|20|(3:507|56|57)(0))(1:450))(1:429)|430|431|432|433|434|435|(1:437)(6:438|439|18|19|20|(0)(0)))(1:426))(1:314)|315|316|317|318|319|320|(1:322)(17:323|324|325|326|327|328|329|330|331|(3:399|400|(4:404|336|(1:390)(1:340)|(12:342|343|344|345|346|347|348|349|350|351|352|(1:354)(8:355|356|357|358|18|19|20|(0)(0)))(5:389|18|19|20|(0)(0))))|333|(3:391|392|(6:394|395|336|(1:338)|390|(0)(0)))|335|336|(0)|390|(0)(0)))(1:311))(1:183)|184|185|186|187|188|189|190|191|192|193|194|195|196|197|198|199|200|201|202|203|204|205|206|207|208|209|210|(1:212)(18:213|214|215|216|217|218|219|220|221|(5:260|261|262|263|264)(1:223)|224|225|(1:255)(1:229)|(4:231|232|233|(1:235)(2:236|237))(1:254)|18|19|20|(0)(0)))(1:79))|80|81|82|83|84|(1:86)(33:87|88|89|90|91|92|93|94|95|(3:158|159|(1:161))|97|98|99|100|101|102|103|104|105|106|107|(1:109)(1:147)|110|111|112|113|114|115|116|117|118|119|(1:121)(8:122|123|124|125|18|19|20|(0)(0)))) */
    /* JADX WARN: Can't wrap try/catch for region: R(9:312|(3:423|424|(9:427|(3:447|448|(6:451|(7:470|471|472|473|474|475|(1:477)(2:478|479))(8:453|454|(1:459)|460|461|462|463|(1:465)(4:466|15|16|17))|18|19|20|(3:507|56|57)(0))(1:450))(1:429)|430|431|432|433|434|435|(1:437)(6:438|439|18|19|20|(0)(0)))(1:426))(1:314)|315|316|317|318|319|320|(1:322)(17:323|324|325|326|327|328|329|330|331|(3:399|400|(4:404|336|(1:390)(1:340)|(12:342|343|344|345|346|347|348|349|350|351|352|(1:354)(8:355|356|357|358|18|19|20|(0)(0)))(5:389|18|19|20|(0)(0))))|333|(3:391|392|(6:394|395|336|(1:338)|390|(0)(0)))|335|336|(0)|390|(0)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(9:427|(3:447|448|(6:451|(7:470|471|472|473|474|475|(1:477)(2:478|479))(8:453|454|(1:459)|460|461|462|463|(1:465)(4:466|15|16|17))|18|19|20|(3:507|56|57)(0))(1:450))(1:429)|430|431|432|433|434|435|(1:437)(6:438|439|18|19|20|(0)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(9:628|(11:580|581|(1:583)(1:614)|584|(1:586)(1:613)|587|(1:589)(1:612)|(1:591)|592|(1:594)(1:611)|595)|(1:597)(9:602|(7:607|(1:609)|599|600|19|20|(21:22|23|24|25|26|27|28|(3:69|70|(2:72|(8:75|(1:496)(2:77|(30:181|(3:308|309|(9:312|(3:423|424|(9:427|(3:447|448|(6:451|(7:470|471|472|473|474|475|(1:477)(2:478|479))(8:453|454|(1:459)|460|461|462|463|(1:465)(4:466|15|16|17))|18|19|20|(3:507|56|57)(0))(1:450))(1:429)|430|431|432|433|434|435|(1:437)(6:438|439|18|19|20|(0)(0)))(1:426))(1:314)|315|316|317|318|319|320|(1:322)(17:323|324|325|326|327|328|329|330|331|(3:399|400|(4:404|336|(1:390)(1:340)|(12:342|343|344|345|346|347|348|349|350|351|352|(1:354)(8:355|356|357|358|18|19|20|(0)(0)))(5:389|18|19|20|(0)(0))))|333|(3:391|392|(6:394|395|336|(1:338)|390|(0)(0)))|335|336|(0)|390|(0)(0)))(1:311))(1:183)|184|185|186|187|188|189|190|191|192|193|194|195|196|197|198|199|200|201|202|203|204|205|206|207|208|209|210|(1:212)(18:213|214|215|216|217|218|219|220|221|(5:260|261|262|263|264)(1:223)|224|225|(1:255)(1:229)|(4:231|232|233|(1:235)(2:236|237))(1:254)|18|19|20|(0)(0)))(1:79))|80|81|82|83|84|(1:86)(33:87|88|89|90|91|92|93|94|95|(3:158|159|(1:161))|97|98|99|100|101|102|103|104|105|106|107|(1:109)(1:147)|110|111|112|113|114|115|116|117|118|119|(1:121)(8:122|123|124|125|18|19|20|(0)(0))))(13:74|31|32|33|34|35|(1:62)(3:37|(1:39)(1:61)|40)|41|42|43|44|45|(1:47)(8:48|49|50|51|18|19|20|(0)(0)))))|30|31|32|33|34|35|(0)(0)|41|42|43|44|45|(0)(0))(0))|610|(0)|599|600|19|20|(0)(0))|598|599|600|19|20|(0)(0)) */
    /* JADX WARN: Code restructure failed: missing block: B:139:0x15d1, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:140:0x15d2, code lost:
    
        r3 = r5;
        r11 = r14;
        r4 = r25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:142:0x151d, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:143:0x151e, code lost:
    
        r5 = r66;
        r6 = r33;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:145:0x1526, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:146:0x1527, code lost:
    
        r35 = r35;
        r6 = r33;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:149:0x152f, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:150:0x1530, code lost:
    
        r6 = r33;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:152:0x1536, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:153:0x1537, code lost:
    
        r31 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:154:0x153b, code lost:
    
        r35 = r8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:155:0x154b, code lost:
    
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:157:0x153a, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:166:0x1542, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:167:0x1543, code lost:
    
        r35 = r8;
        r19 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:168:0x1547, code lost:
    
        r34 = r13;
        r20 = r15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:170:0x154e, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:171:0x154f, code lost:
    
        r35 = r8;
        r19 = r12;
        r34 = r13;
        r18 = r15;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:176:0x1568, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:177:0x1569, code lost:
    
        r14 = r1;
        r19 = r12;
        r34 = r13;
        r5 = r50;
        r1 = r11;
        r11 = r6;
        r6 = r14;
        r14 = r1;
        r35 = r8;
        r17 = r37;
        r1 = r21;
        r18 = r56;
     */
    /* JADX WARN: Code restructure failed: missing block: B:179:0x157f, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:180:0x1580, code lost:
    
        r14 = r1;
        r19 = r12;
        r34 = r13;
        r1 = r11;
        r11 = r6;
        r6 = r14;
        r14 = r1;
        r35 = r8;
        r17 = r15;
        r1 = r21;
        r18 = r56;
     */
    /* JADX WARN: Code restructure failed: missing block: B:257:0x11f4, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:259:0x11ff, code lost:
    
        r30 = r61;
        r56 = r62;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:266:0x1134, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:267:0x1135, code lost:
    
        r30 = r61;
        r56 = r62;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:272:0x11f8, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:273:0x11f9, code lost:
    
        r20 = r12;
        r12 = r32;
     */
    /* JADX WARN: Code restructure failed: missing block: B:275:0x1207, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:276:0x1208, code lost:
    
        r62 = r12;
        r12 = r32;
        r20 = r33;
        r30 = r61;
        r56 = r62;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:281:0x122b, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:282:0x122c, code lost:
    
        r12 = r32;
        r20 = r33;
        r15 = r37;
        r5 = r50;
        r14 = r6;
        r6 = r1;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:284:0x123a, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:285:0x123b, code lost:
    
        r12 = r32;
        r20 = r33;
        r15 = r37;
        r14 = r6;
        r6 = r1;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:287:0x1247, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:288:0x1248, code lost:
    
        r12 = r32;
        r20 = r33;
        r15 = r37;
        r8 = r57;
        r14 = r6;
        r6 = r1;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:290:0x1256, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:291:0x1257, code lost:
    
        r12 = r32;
        r20 = r33;
        r15 = r37;
        r9 = r53;
        r8 = r57;
        r14 = r6;
        r6 = r1;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:293:0x1267, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:294:0x1268, code lost:
    
        r12 = r32;
        r20 = r33;
        r6 = r35;
        r15 = r37;
        r9 = r53;
     */
    /* JADX WARN: Code restructure failed: missing block: B:295:0x1280, code lost:
    
        r8 = r57;
        r14 = r6;
        r6 = r1;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:297:0x1273, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:298:0x1274, code lost:
    
        r12 = r32;
        r20 = r33;
        r6 = r35;
        r15 = r37;
        r9 = r53;
        r4 = r54;
     */
    /* JADX WARN: Code restructure failed: missing block: B:300:0x1287, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:301:0x1288, code lost:
    
        r12 = r32;
        r20 = r33;
        r15 = r37;
        r9 = r53;
        r4 = r54;
        r3 = r55;
        r8 = r57;
        r14 = r35;
        r6 = r1;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:303:0x129d, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:304:0x129e, code lost:
    
        r12 = r32;
        r20 = r33;
        r15 = r37;
        r9 = r53;
        r4 = r54;
        r3 = r55;
        r8 = r57;
        r11 = r59;
        r14 = r35;
        r6 = r1;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:306:0x12b5, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:307:0x12b6, code lost:
    
        r12 = r32;
        r20 = r33;
        r15 = r37;
        r9 = r53;
        r4 = r54;
        r3 = r55;
        r8 = r57;
        r11 = r59;
        r10 = r60;
        r14 = r35;
        r6 = r1;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:381:0x0f2b, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:382:0x0f2c, code lost:
    
        r8 = r66;
        r9 = r19;
        r15 = r18;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:384:0x0f36, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:385:0x0f37, code lost:
    
        r9 = r19;
        r15 = r18;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:387:0x0f3f, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:388:0x0f40, code lost:
    
        r20 = r1;
        r9 = r19;
        r15 = r18;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:409:0x0f68, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:410:0x0f69, code lost:
    
        r9 = r19;
        r3 = r20;
        r20 = r1;
        r15 = r18;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:412:0x0f75, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:413:0x0f76, code lost:
    
        r20 = r1;
        r3 = r6;
        r1 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:418:0x0f8a, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:419:0x0f8b, code lost:
    
        r9 = r14;
        r8 = r18;
        r1 = r21;
        r11 = r36;
        r12 = r5;
        r10 = r20;
        r5 = r54;
        r20 = r10;
        r17 = r16;
        r3 = 0;
        r15 = r0;
        r14 = r59;
        r4 = r60;
     */
    /* JADX WARN: Code restructure failed: missing block: B:421:0x0fa8, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:422:0x0fa9, code lost:
    
        r59 = r1;
        r37 = r2;
        r2 = r8;
        r53 = r12;
        r8 = r3;
        r1 = r21;
        r11 = r13;
        r12 = r37;
        r10 = r53;
        r5 = r5;
        r20 = r6;
        r17 = r16;
        r3 = 0;
        r15 = r0;
        r14 = r59;
     */
    /* JADX WARN: Code restructure failed: missing block: B:442:0x0ca3, code lost:
    
        r8 = r63;
        r3 = r66;
        r26 = r65;
        r1 = r26;
        r14 = r62;
        r0 = r64;
        r10 = r12;
        r11 = r13;
        r13 = r0;
        r4 = r25;
        r12 = r29;
        r2 = r30;
        r29 = r7;
        r25 = r21;
        r21 = r5;
        r5 = r9;
     */
    /* JADX WARN: Code restructure failed: missing block: B:446:0x0cc7, code lost:
    
        r8 = r1;
        r26 = r11;
        r1 = r26;
        r14 = r37;
        r0 = r10;
        r10 = r12;
        r11 = r13;
        r13 = r0;
        r4 = r25;
        r12 = r29;
        r2 = r30;
        r29 = r7;
        r25 = r21;
        r21 = r5;
        r5 = r9;
     */
    /* JADX WARN: Code restructure failed: missing block: B:482:0x0a8b, code lost:
    
        r3 = r66;
        r0 = r64;
        r10 = r12;
        r4 = r25;
        r12 = r29;
        r29 = r7;
        r25 = r21;
        r7 = r63;
        r21 = r5;
        r5 = r9;
        r1 = r26;
        r26 = r65;
        r11 = r13;
        r13 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:509:0x1749, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:510:0x174a, code lost:
    
        r28 = r1;
        r30 = r3;
        r2 = r11;
        r11 = r4;
        r3 = r8;
        r4 = r25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:635:0x06ee, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:636:0x06ef, code lost:
    
        r2 = r63;
        r11 = r65;
        r28 = r66;
        r30 = r66;
        r34 = "MultiCloud";
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:8:0x0030. Please report as an issue. */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:109:0x1414  */
    /* JADX WARN: Removed duplicated region for block: B:121:0x14d2 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:122:0x14d3  */
    /* JADX WARN: Removed duplicated region for block: B:12:0x003b  */
    /* JADX WARN: Removed duplicated region for block: B:147:0x1417 A[Catch: Exception -> 0x152f, TryCatch #34 {Exception -> 0x152f, blocks: (B:107:0x140e, B:110:0x1430, B:115:0x14bc, B:147:0x1417), top: B:106:0x140e }] */
    /* JADX WARN: Removed duplicated region for block: B:158:0x13cc A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:223:0x1120  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x086d A[Catch: Exception -> 0x1749, TRY_LEAVE, TryCatch #64 {Exception -> 0x1749, blocks: (B:20:0x0867, B:22:0x086d), top: B:19:0x0867 }] */
    /* JADX WARN: Removed duplicated region for block: B:231:0x1141  */
    /* JADX WARN: Removed duplicated region for block: B:254:0x11da  */
    /* JADX WARN: Removed duplicated region for block: B:260:0x1106 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:338:0x0e4c A[Catch: Exception -> 0x0e08, TRY_ENTER, TRY_LEAVE, TryCatch #30 {Exception -> 0x0e08, blocks: (B:400:0x0df4, B:402:0x0dfa, B:338:0x0e4c, B:395:0x0e2f), top: B:399:0x0df4 }] */
    /* JADX WARN: Removed duplicated region for block: B:342:0x0e59 A[Catch: Exception -> 0x0f68, TRY_ENTER, TRY_LEAVE, TryCatch #13 {Exception -> 0x0f68, blocks: (B:331:0x0dee, B:336:0x0e47, B:342:0x0e59, B:333:0x0e15), top: B:330:0x0dee }] */
    /* JADX WARN: Removed duplicated region for block: B:37:0x160a A[Catch: Exception -> 0x1718, TryCatch #25 {Exception -> 0x1718, blocks: (B:35:0x1604, B:37:0x160a, B:39:0x161a, B:40:0x1639, B:41:0x1640, B:61:0x162a), top: B:34:0x1604 }] */
    /* JADX WARN: Removed duplicated region for block: B:389:0x0f4a  */
    /* JADX WARN: Removed duplicated region for block: B:391:0x0e28 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:399:0x0df4 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:47:0x16d4 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x16d5  */
    /* JADX WARN: Removed duplicated region for block: B:507:0x173a  */
    /* JADX WARN: Removed duplicated region for block: B:514:0x00bc  */
    /* JADX WARN: Removed duplicated region for block: B:522:0x0155  */
    /* JADX WARN: Removed duplicated region for block: B:530:0x01e2  */
    /* JADX WARN: Removed duplicated region for block: B:537:0x02a7  */
    /* JADX WARN: Removed duplicated region for block: B:544:0x0358  */
    /* JADX WARN: Removed duplicated region for block: B:552:0x041b  */
    /* JADX WARN: Removed duplicated region for block: B:559:0x04d1  */
    /* JADX WARN: Removed duplicated region for block: B:566:0x05a2  */
    /* JADX WARN: Removed duplicated region for block: B:573:0x065c  */
    /* JADX WARN: Removed duplicated region for block: B:576:0x06ff  */
    /* JADX WARN: Removed duplicated region for block: B:583:0x07b1 A[Catch: Exception -> 0x1755, TRY_LEAVE, TryCatch #66 {Exception -> 0x1755, blocks: (B:581:0x07a3, B:583:0x07b1, B:587:0x07c0, B:592:0x07f7, B:597:0x080f, B:598:0x0820, B:599:0x0848, B:602:0x0825, B:604:0x082a, B:609:0x0836), top: B:580:0x07a3 }] */
    /* JADX WARN: Removed duplicated region for block: B:586:0x07bb  */
    /* JADX WARN: Removed duplicated region for block: B:589:0x07f0  */
    /* JADX WARN: Removed duplicated region for block: B:591:0x07f5  */
    /* JADX WARN: Removed duplicated region for block: B:594:0x0806  */
    /* JADX WARN: Removed duplicated region for block: B:597:0x080f A[Catch: Exception -> 0x1755, TRY_ENTER, TryCatch #66 {Exception -> 0x1755, blocks: (B:581:0x07a3, B:583:0x07b1, B:587:0x07c0, B:592:0x07f7, B:597:0x080f, B:598:0x0820, B:599:0x0848, B:602:0x0825, B:604:0x082a, B:609:0x0836), top: B:580:0x07a3 }] */
    /* JADX WARN: Removed duplicated region for block: B:602:0x0825 A[Catch: Exception -> 0x1755, TryCatch #66 {Exception -> 0x1755, blocks: (B:581:0x07a3, B:583:0x07b1, B:587:0x07c0, B:592:0x07f7, B:597:0x080f, B:598:0x0820, B:599:0x0848, B:602:0x0825, B:604:0x082a, B:609:0x0836), top: B:580:0x07a3 }] */
    /* JADX WARN: Removed duplicated region for block: B:609:0x0836 A[Catch: Exception -> 0x1755, TryCatch #66 {Exception -> 0x1755, blocks: (B:581:0x07a3, B:583:0x07b1, B:587:0x07c0, B:592:0x07f7, B:597:0x080f, B:598:0x0820, B:599:0x0848, B:602:0x0825, B:604:0x082a, B:609:0x0836), top: B:580:0x07a3 }] */
    /* JADX WARN: Removed duplicated region for block: B:611:0x0808  */
    /* JADX WARN: Removed duplicated region for block: B:612:0x07f2  */
    /* JADX WARN: Removed duplicated region for block: B:613:0x07be  */
    /* JADX WARN: Removed duplicated region for block: B:614:0x07b6  */
    /* JADX WARN: Removed duplicated region for block: B:620:0x073a  */
    /* JADX WARN: Removed duplicated region for block: B:62:0x163e  */
    /* JADX WARN: Removed duplicated region for block: B:9:0x0033  */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 2 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:125:0x14eb -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:134:0x15af -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:247:0x12f2 -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:254:0x11da -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:358:0x0f01 -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:370:0x0ff7 -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:389:0x0f4a -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:443:0x0ce4 -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:459:0x0b0b -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:466:0x0ba8 -> B:15:0x0bb6). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:483:0x0ac6 -> B:18:0x1700). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:51:0x16e9 -> B:18:0x1700). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static /* synthetic */ Object getUrl$suspendImpl(MultiCloudLinks multiCloudLinks, String str, String str2, Function1<? super SubtitleFile, Unit> function1, Function1<? super ExtractorLink, Unit> function12, Continuation<? super Unit> continuation) {
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$1;
        MultiCloudLinks multiCloudLinks2;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$12;
        String str3;
        Function1<? super ExtractorLink, Unit> function13;
        Continuation<? super Unit> continuation2;
        MultiCloudLinks multiCloudLinks3;
        String str4;
        Requests app;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$13;
        String str5;
        String str6;
        String str7;
        String str8;
        Object obj;
        Object obj2;
        Object obj3;
        MultiCloudLinks multiCloudLinks4;
        String str9;
        String str10;
        Function1<? super SubtitleFile, Unit> function14;
        Function1<? super ExtractorLink, Unit> function15;
        String obj4;
        String str11;
        boolean z;
        StringBuilder append;
        Document document;
        String str12;
        Iterable iterable;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$14;
        Object obj5;
        String str13;
        Iterator it;
        int i;
        String str14;
        int i2;
        Object obj6;
        Continuation<? super Unit> continuation3;
        int i3;
        Element element;
        String str15;
        Document document2;
        String str16;
        String str17;
        String str18;
        int i4;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$15;
        Object obj7;
        String str19;
        Function1<? super ExtractorLink, Unit> function16;
        Object obj8;
        String str20;
        Object obj9;
        int i5;
        Iterator it2;
        int i6;
        Element element2;
        int i7;
        String str21;
        String str22;
        Element element3;
        int i8;
        Function1<? super ExtractorLink, Unit> function17;
        Object obj10;
        Document document3;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$16;
        Iterator it3;
        String str23;
        Object obj11;
        String str24;
        String str25;
        String str26;
        int i9;
        Object obj12;
        String str27;
        String str28;
        Object obj13;
        Iterator it4;
        String str29;
        Document document4;
        MultiCloudLinks multiCloudLinks5;
        Object obj14;
        Object obj15;
        Function1<? super ExtractorLink, Unit> function18;
        String str30;
        Element element4;
        int i10;
        Element element5;
        String str31;
        Object obj16;
        String str32;
        int i11;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$17;
        String str33;
        Document document5;
        String str34;
        int i12;
        Iterator it5;
        Object obj17;
        MultiCloudLinks multiCloudLinks6;
        int i13;
        int i14;
        String str35;
        String str36;
        Element element6;
        Element element7;
        int i15;
        String str37;
        Function1<? super ExtractorLink, Unit> function19;
        Document document6;
        Function1<? super SubtitleFile, Unit> function110;
        Object obj18;
        int i16;
        String str38;
        int i17;
        String str39;
        MultiCloudLinks multiCloudLinks7;
        String str40;
        Object obj19;
        Object obj20;
        Function1<? super ExtractorLink, Unit> function111;
        Element element8;
        int i18;
        int i19;
        String str41;
        String str42;
        Object obj21;
        Iterator it6;
        Document document7;
        Function1<? super ExtractorLink, Unit> function112;
        String str43;
        Function1<? super SubtitleFile, Unit> function113;
        String str44;
        String str45;
        String str46;
        MatchResult find$default;
        String str47;
        String str48;
        String str49;
        MatchResult find$default2;
        String str50;
        String str51;
        String str52;
        String str53;
        String str54;
        Object obj22;
        Object newExtractorLink;
        String obj23;
        String obj24;
        String lowerCase;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$18;
        Object obj25;
        int i20;
        Element element9;
        Function1<? super ExtractorLink, Unit> function114;
        String str55;
        String str56;
        Document document8;
        Object obj26;
        Function1<? super ExtractorLink, Unit> function115;
        int i21;
        int i22;
        String str57;
        int i23;
        String str58;
        String str59;
        String str60;
        Function1<? super ExtractorLink, Unit> function116;
        String str61;
        Function1<? super ExtractorLink, Unit> function117;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$19;
        String str62;
        String str63;
        Function1<? super ExtractorLink, Unit> function118;
        Iterator it7;
        String str64;
        Object obj27;
        Function1<? super SubtitleFile, Unit> function119;
        int i24;
        int i25;
        int i26;
        Object obj28;
        int i27;
        Element element10;
        String str65;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$110;
        int i28;
        String str66;
        String str67;
        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$111;
        Object newExtractorLink2;
        if (continuation instanceof MultiCloudLinks$getUrl$1) {
            multiCloudLinks$getUrl$1 = (MultiCloudLinks$getUrl$1) continuation;
            if ((multiCloudLinks$getUrl$1.label & Integer.MIN_VALUE) != 0) {
                multiCloudLinks$getUrl$1.label -= Integer.MIN_VALUE;
                multiCloudLinks2 = multiCloudLinks;
                multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$1;
                Object obj29 = multiCloudLinks$getUrl$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (multiCloudLinks$getUrl$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj29);
                        try {
                            app = MainActivityKt.getApp();
                            multiCloudLinks$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks2);
                            multiCloudLinks$getUrl$12.L$1 = str;
                            multiCloudLinks$getUrl$12.L$2 = str2;
                            multiCloudLinks$getUrl$12.L$3 = function1;
                            multiCloudLinks$getUrl$12.L$4 = function12;
                            multiCloudLinks$getUrl$12.label = 1;
                            multiCloudLinks$getUrl$13 = multiCloudLinks$getUrl$12;
                            str5 = "location";
                            str4 = "MultiCloud";
                            str6 = "?";
                            str7 = null;
                            str8 = "Location";
                            obj = obj29;
                            obj2 = coroutine_suspended;
                        } catch (Exception e) {
                            e = e;
                            str4 = "MultiCloud";
                            multiCloudLinks3 = multiCloudLinks;
                            str3 = str2;
                            function13 = function12;
                            continuation2 = continuation;
                        }
                        try {
                            obj3 = Requests.get$default(app, str, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, multiCloudLinks$getUrl$13, 4094, (Object) null);
                        } catch (Exception e2) {
                            e = e2;
                            multiCloudLinks3 = multiCloudLinks;
                            str3 = str2;
                            function13 = function12;
                            continuation2 = continuation;
                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$13;
                            obj29 = obj;
                            Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        if (obj3 == obj2) {
                            return obj2;
                        }
                        multiCloudLinks4 = multiCloudLinks;
                        str9 = str;
                        str10 = str2;
                        function14 = function1;
                        function15 = function12;
                        try {
                            Document document9 = ((NiceResponse) obj3).getDocument();
                            Element selectFirst = document9.selectFirst("h1, h2, h3, title");
                            String text = selectFirst == null ? selectFirst.text() : str7;
                            String str68 = "";
                            obj4 = StringsKt.trim(StringsKt.replace$default(StringsKt.replace$default(text != null ? "" : text, "Download Links - MultiCloud", "", false, 4, (Object) null), "Download Links", "", false, 4, (Object) null)).toString();
                            str11 = obj4;
                            if (str11.length() != 0) {
                                str11 = str10;
                            }
                            int indexQuality = ExtractorsKt.getIndexQuality(str11);
                        } catch (Exception e3) {
                            e = e3;
                            continuation2 = continuation;
                            multiCloudLinks3 = multiCloudLinks4;
                            str3 = str10;
                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$13;
                            function13 = function15;
                            obj29 = obj;
                            Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        if (obj4.length() <= 0) {
                            String str69 = str10;
                            if (str69 != null && str69.length() != 0) {
                                z = false;
                                if (!z) {
                                    append = new StringBuilder().append(" [").append(str10).append("]");
                                }
                                Iterable select = document9.select("a[href]");
                                Iterator it8 = select.iterator();
                                document = document9;
                                str12 = obj4;
                                iterable = select;
                                multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$13;
                                obj5 = obj;
                                str13 = str68;
                                it = it8;
                                i = indexQuality;
                                str14 = str9;
                                i2 = 0;
                                obj6 = obj2;
                                continuation3 = continuation;
                                if (!it.hasNext()) {
                                    try {
                                        try {
                                            try {
                                                try {
                                                    try {
                                                        obj11 = it.next();
                                                        Element element11 = (Element) obj11;
                                                        obj23 = StringsKt.trim(element11.attr("href")).toString();
                                                        obj24 = StringsKt.trim(element11.text()).toString();
                                                        element8 = element11;
                                                        lowerCase = (obj24 + " " + obj23).toLowerCase(Locale.ROOT);
                                                        Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
                                                        str26 = lowerCase;
                                                    } catch (Exception e4) {
                                                        e = e4;
                                                        multiCloudLinks3 = multiCloudLinks4;
                                                        str3 = str10;
                                                        function13 = function15;
                                                        multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                        obj29 = obj5;
                                                    }
                                                    continuation2 = continuation3;
                                                } catch (Exception e5) {
                                                    e = e5;
                                                    multiCloudLinks$getUrl$18 = multiCloudLinks$getUrl$14;
                                                    str3 = str10;
                                                }
                                                multiCloudLinks3 = multiCloudLinks4;
                                            } catch (Exception e6) {
                                                e = e6;
                                                str4 = str65;
                                                multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$110;
                                                obj29 = obj5;
                                            }
                                        } catch (Exception e7) {
                                            e = e7;
                                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$110;
                                            obj29 = obj5;
                                        }
                                    } catch (Exception e8) {
                                        e = e8;
                                        multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$111;
                                        obj29 = obj5;
                                        Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                                        return Unit.INSTANCE;
                                    }
                                    if (!StringsKt.contains$default(lowerCase, "turbo download", false, 2, (Object) null)) {
                                        try {
                                        } catch (Exception e9) {
                                            e = e9;
                                            multiCloudLinks$getUrl$18 = multiCloudLinks$getUrl$14;
                                            str3 = str10;
                                            function13 = function15;
                                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$18;
                                            obj29 = obj5;
                                            Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                                            return Unit.INSTANCE;
                                        }
                                        if (!StringsKt.contains$default(str26, "multidownload.rent", false, 2, (Object) null)) {
                                            if (StringsKt.contains$default(str26, "multidownload.website", false, 2, (Object) null)) {
                                                str20 = str13;
                                                obj28 = obj6;
                                                str3 = str10;
                                                i4 = i2;
                                                i27 = i;
                                                element10 = element8;
                                                str17 = obj24;
                                                str16 = str8;
                                                str19 = str5;
                                                str65 = str4;
                                                function13 = function15;
                                                it2 = it;
                                                multiCloudLinks$getUrl$110 = multiCloudLinks$getUrl$14;
                                                i28 = 0;
                                                str66 = obj23;
                                                str4 = str65;
                                                Object obj30 = obj28;
                                                if (StringsKt.contains$default(str66, "action=download", false, 2, (Object) null)) {
                                                    str67 = str66;
                                                } else {
                                                    str67 = (StringsKt.contains$default(str66, str6, false, 2, (Object) null) ? new StringBuilder().append(str66).append("&action=download") : new StringBuilder().append(str66).append("?action=download")).toString();
                                                }
                                                String str70 = "MultiCloud [Turbo R2]" + str20;
                                                ExtractorLinkType infer_type = ExtractorApiKt.getINFER_TYPE();
                                                MultiCloudLinks$getUrl$2$1 multiCloudLinks$getUrl$2$1 = new MultiCloudLinks$getUrl$2$1(i27, str14, null);
                                                multiCloudLinks$getUrl$110.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                multiCloudLinks$getUrl$110.L$1 = str14;
                                                multiCloudLinks$getUrl$110.L$2 = str3;
                                                multiCloudLinks$getUrl$110.L$3 = function14;
                                                multiCloudLinks$getUrl$110.L$4 = function13;
                                                multiCloudLinks$getUrl$110.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                multiCloudLinks$getUrl$110.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                multiCloudLinks$getUrl$110.L$7 = str20;
                                                multiCloudLinks$getUrl$110.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                multiCloudLinks$getUrl$110.L$9 = it2;
                                                multiCloudLinks$getUrl$110.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                multiCloudLinks$getUrl$110.L$11 = SpillingKt.nullOutSpilledVariable(element10);
                                                multiCloudLinks$getUrl$110.L$12 = SpillingKt.nullOutSpilledVariable(str66);
                                                multiCloudLinks$getUrl$110.L$13 = SpillingKt.nullOutSpilledVariable(str17);
                                                multiCloudLinks$getUrl$110.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                multiCloudLinks$getUrl$110.L$15 = SpillingKt.nullOutSpilledVariable(str67);
                                                multiCloudLinks$getUrl$110.L$16 = function13;
                                                multiCloudLinks$getUrl$110.L$17 = null;
                                                multiCloudLinks$getUrl$110.L$18 = null;
                                                multiCloudLinks$getUrl$110.L$19 = null;
                                                multiCloudLinks$getUrl$110.I$0 = i27;
                                                multiCloudLinks$getUrl$110.I$1 = i4;
                                                multiCloudLinks$getUrl$110.I$2 = i28;
                                                multiCloudLinks$getUrl$110.label = 2;
                                                multiCloudLinks$getUrl$111 = multiCloudLinks$getUrl$110;
                                                newExtractorLink2 = ExtractorApiKt.newExtractorLink("MultiCloud [Turbo R2]", str70, str67, infer_type, multiCloudLinks$getUrl$2$1, multiCloudLinks$getUrl$111);
                                                obj7 = obj30;
                                                if (newExtractorLink2 != obj7) {
                                                    return obj7;
                                                }
                                                int i29 = i28;
                                                obj9 = newExtractorLink2;
                                                i3 = i29;
                                                function16 = function13;
                                                str18 = str66;
                                                document2 = document;
                                                element = element10;
                                                i5 = i27;
                                                multiCloudLinks$getUrl$15 = multiCloudLinks$getUrl$110;
                                                obj8 = obj11;
                                                str15 = str12;
                                                try {
                                                    function16.invoke(obj9);
                                                    multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$15;
                                                    i = i5;
                                                    str12 = str15;
                                                    str13 = str20;
                                                    i2 = i4;
                                                    obj6 = obj7;
                                                    document = document2;
                                                    multiCloudLinks4 = multiCloudLinks3;
                                                    it = it2;
                                                    function15 = function13;
                                                    str10 = str3;
                                                    str8 = str16;
                                                    str5 = str19;
                                                    continuation3 = continuation2;
                                                    if (!it.hasNext()) {
                                                    }
                                                } catch (Exception e10) {
                                                    e = e10;
                                                    multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$15;
                                                    obj29 = obj5;
                                                    Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                                                    return Unit.INSTANCE;
                                                }
                                            } else {
                                                if (StringsKt.contains$default(str26, "pixeldra", false, 2, (Object) null)) {
                                                    str23 = str13;
                                                    obj25 = obj6;
                                                    str3 = str10;
                                                    i7 = i2;
                                                    i20 = i;
                                                    element9 = element8;
                                                    str21 = obj24;
                                                    str25 = str8;
                                                    str19 = str5;
                                                    str27 = str4;
                                                    function114 = function15;
                                                    it3 = it;
                                                    multiCloudLinks$getUrl$16 = multiCloudLinks$getUrl$14;
                                                    i9 = 0;
                                                    str55 = obj23;
                                                } else if (StringsKt.contains$default(str26, "pixel", false, 2, (Object) null)) {
                                                    str23 = str13;
                                                    obj25 = obj6;
                                                    str3 = str10;
                                                    i7 = i2;
                                                    i20 = i;
                                                    element9 = element8;
                                                    str21 = obj24;
                                                    str25 = str8;
                                                    str19 = str5;
                                                    str27 = str4;
                                                    function114 = function15;
                                                    it3 = it;
                                                    multiCloudLinks$getUrl$16 = multiCloudLinks$getUrl$14;
                                                    i9 = 0;
                                                    str55 = obj23;
                                                } else {
                                                    if (StringsKt.contains$default(str26, "viking", false, 2, (Object) null)) {
                                                        str63 = obj23;
                                                        function118 = function15;
                                                        it7 = it;
                                                        str64 = str13;
                                                        multiCloudLinks$getUrl$17 = multiCloudLinks$getUrl$14;
                                                        obj27 = obj6;
                                                        function119 = function14;
                                                        str3 = str10;
                                                        i24 = i2;
                                                        i25 = i;
                                                        i26 = 0;
                                                        element5 = element8;
                                                        str31 = obj24;
                                                        str34 = str4;
                                                    } else {
                                                        try {
                                                        } catch (Exception e11) {
                                                            e = e11;
                                                            Function1<? super ExtractorLink, Unit> function120 = function15;
                                                            str3 = str10;
                                                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                            obj29 = obj5;
                                                            function13 = function120;
                                                        }
                                                        if (StringsKt.contains$default(str26, "vikingfile", false, 2, (Object) null)) {
                                                            str63 = obj23;
                                                            function118 = function15;
                                                            it7 = it;
                                                            str64 = str13;
                                                            multiCloudLinks$getUrl$17 = multiCloudLinks$getUrl$14;
                                                            obj27 = obj6;
                                                            function119 = function14;
                                                            str3 = str10;
                                                            i24 = i2;
                                                            i25 = i;
                                                            i26 = 0;
                                                            element5 = element8;
                                                            str31 = obj24;
                                                            str34 = str4;
                                                        } else {
                                                            if (StringsKt.contains$default(str26, "watch online", false, 2, (Object) null)) {
                                                                str61 = obj23;
                                                                function117 = function15;
                                                                multiCloudLinks$getUrl$19 = multiCloudLinks$getUrl$14;
                                                                function110 = function14;
                                                                str62 = str10;
                                                            } else {
                                                                try {
                                                                } catch (Exception e12) {
                                                                    e = e12;
                                                                    multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                    String str71 = str10;
                                                                    function13 = function15;
                                                                    str3 = str71;
                                                                    obj29 = obj5;
                                                                }
                                                                if (StringsKt.contains$default(str26, "player.php", false, 2, (Object) null)) {
                                                                    str61 = obj23;
                                                                    function117 = function15;
                                                                    multiCloudLinks$getUrl$19 = multiCloudLinks$getUrl$14;
                                                                    function110 = function14;
                                                                    str62 = str10;
                                                                } else {
                                                                    if (StringsKt.contains$default(str26, "filepress", false, 2, (Object) null)) {
                                                                        str59 = obj23;
                                                                        multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                        str60 = str10;
                                                                        function116 = function15;
                                                                    } else {
                                                                        try {
                                                                        } catch (Exception e13) {
                                                                            e = e13;
                                                                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                            String str72 = str10;
                                                                            function13 = function15;
                                                                            str3 = str72;
                                                                            obj29 = obj5;
                                                                        }
                                                                        if (StringsKt.contains$default(str26, "filebee", false, 2, (Object) null)) {
                                                                            str59 = obj23;
                                                                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                            str60 = str10;
                                                                            function116 = function15;
                                                                        } else {
                                                                            if (StringsKt.contains$default(str26, "gdflix", false, 2, (Object) null)) {
                                                                                try {
                                                                                } catch (Exception e14) {
                                                                                    multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                                    String str73 = str10;
                                                                                    Function1<? super ExtractorLink, Unit> function121 = function15;
                                                                                    function113 = function14;
                                                                                    i18 = i2;
                                                                                    obj29 = obj5;
                                                                                    str41 = str26;
                                                                                    str43 = str14;
                                                                                    document7 = document;
                                                                                    str44 = str73;
                                                                                    it6 = it;
                                                                                    coroutine_suspended = obj6;
                                                                                    obj21 = obj11;
                                                                                    function112 = function121;
                                                                                    i19 = i;
                                                                                    str42 = obj24;
                                                                                    break;
                                                                                }
                                                                                GDFlix gDFlix = new GDFlix();
                                                                                multiCloudLinks$getUrl$14.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                                                multiCloudLinks$getUrl$14.L$1 = str14;
                                                                                multiCloudLinks$getUrl$14.L$2 = str10;
                                                                                multiCloudLinks$getUrl$14.L$3 = function14;
                                                                                multiCloudLinks$getUrl$14.L$4 = function15;
                                                                                multiCloudLinks$getUrl$14.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                                                multiCloudLinks$getUrl$14.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                                                multiCloudLinks$getUrl$14.L$7 = str13;
                                                                                multiCloudLinks$getUrl$14.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                                                multiCloudLinks$getUrl$14.L$9 = it;
                                                                                multiCloudLinks$getUrl$14.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                                                multiCloudLinks$getUrl$14.L$11 = SpillingKt.nullOutSpilledVariable(element8);
                                                                                multiCloudLinks$getUrl$14.L$12 = SpillingKt.nullOutSpilledVariable(obj23);
                                                                                multiCloudLinks$getUrl$14.L$13 = SpillingKt.nullOutSpilledVariable(obj24);
                                                                                multiCloudLinks$getUrl$14.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                                                multiCloudLinks$getUrl$14.L$15 = null;
                                                                                multiCloudLinks$getUrl$14.L$16 = null;
                                                                                multiCloudLinks$getUrl$14.L$17 = null;
                                                                                multiCloudLinks$getUrl$14.L$18 = null;
                                                                                multiCloudLinks$getUrl$14.L$19 = null;
                                                                                multiCloudLinks$getUrl$14.I$0 = i;
                                                                                multiCloudLinks$getUrl$14.I$1 = i2;
                                                                                multiCloudLinks$getUrl$14.I$2 = 0;
                                                                                multiCloudLinks$getUrl$14.label = 10;
                                                                                Function1<? super ExtractorLink, Unit> function122 = function15;
                                                                                MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$112 = multiCloudLinks$getUrl$14;
                                                                                Function1<? super SubtitleFile, Unit> function123 = function14;
                                                                                String str74 = str10;
                                                                                if (gDFlix.getUrl(obj23, str74, function123, function122, multiCloudLinks$getUrl$112) == obj6) {
                                                                                    return obj6;
                                                                                }
                                                                                multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$112;
                                                                                obj6 = obj6;
                                                                                it = it;
                                                                                document = document;
                                                                                multiCloudLinks4 = multiCloudLinks3;
                                                                                str16 = str8;
                                                                                str19 = str5;
                                                                                obj5 = obj5;
                                                                                i = i;
                                                                                function15 = function122;
                                                                                str10 = str74;
                                                                                str14 = str14;
                                                                                i2 = i2;
                                                                                function14 = function14;
                                                                            } else {
                                                                                try {
                                                                                    multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                                    str56 = str10;
                                                                                    function13 = function15;
                                                                                    if (!StringsKt.contains$default(str26, "10 gbps", false, 2, (Object) null) && !StringsKt.contains$default(str26, "instant download", false, 2, (Object) null)) {
                                                                                        multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                                                                                        function15 = function13;
                                                                                        multiCloudLinks4 = multiCloudLinks3;
                                                                                        str16 = str8;
                                                                                        str19 = str5;
                                                                                        str10 = str56;
                                                                                    }
                                                                                    String str75 = "MultiCloud [10Gbps]" + str13;
                                                                                    ExtractorLinkType infer_type2 = ExtractorApiKt.getINFER_TYPE();
                                                                                    MultiCloudLinks$getUrl$2$6 multiCloudLinks$getUrl$2$6 = new MultiCloudLinks$getUrl$2$6(i, str14, null);
                                                                                    multiCloudLinks$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                                                    multiCloudLinks$getUrl$12.L$1 = str14;
                                                                                    multiCloudLinks$getUrl$12.L$2 = str56;
                                                                                    multiCloudLinks$getUrl$12.L$3 = function14;
                                                                                    multiCloudLinks$getUrl$12.L$4 = function13;
                                                                                    multiCloudLinks$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                                                    multiCloudLinks$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                                                    multiCloudLinks$getUrl$12.L$7 = str13;
                                                                                    multiCloudLinks$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                                                    multiCloudLinks$getUrl$12.L$9 = it;
                                                                                    multiCloudLinks$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                                                    multiCloudLinks$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(element8);
                                                                                    multiCloudLinks$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(obj23);
                                                                                    multiCloudLinks$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(obj24);
                                                                                    multiCloudLinks$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                                                    multiCloudLinks$getUrl$12.L$15 = function13;
                                                                                    multiCloudLinks$getUrl$12.L$16 = null;
                                                                                    multiCloudLinks$getUrl$12.L$17 = null;
                                                                                    multiCloudLinks$getUrl$12.L$18 = null;
                                                                                    multiCloudLinks$getUrl$12.L$19 = null;
                                                                                    multiCloudLinks$getUrl$12.I$0 = i;
                                                                                    multiCloudLinks$getUrl$12.I$1 = i2;
                                                                                    multiCloudLinks$getUrl$12.I$2 = 0;
                                                                                    multiCloudLinks$getUrl$12.label = 11;
                                                                                    obj29 = ExtractorApiKt.newExtractorLink("MultiCloud [10Gbps]", str75, obj23, infer_type2, multiCloudLinks$getUrl$2$6, multiCloudLinks$getUrl$12);
                                                                                    if (obj29 == obj6) {
                                                                                        return obj6;
                                                                                    }
                                                                                    document8 = document;
                                                                                    obj26 = obj11;
                                                                                    function115 = function13;
                                                                                    i21 = i2;
                                                                                    i22 = 0;
                                                                                    str57 = str26;
                                                                                    i23 = i;
                                                                                    str58 = obj24;
                                                                                    try {
                                                                                        function115.invoke(obj29);
                                                                                        document = document8;
                                                                                        multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                                                                                        function15 = function13;
                                                                                        i2 = i21;
                                                                                        i = i23;
                                                                                        multiCloudLinks4 = multiCloudLinks3;
                                                                                        str16 = str8;
                                                                                        str19 = str5;
                                                                                        str10 = str56;
                                                                                    } catch (Exception e15) {
                                                                                        e = e15;
                                                                                        str3 = str56;
                                                                                        obj29 = obj5;
                                                                                        Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                                                                                        return Unit.INSTANCE;
                                                                                    }
                                                                                } catch (Exception e16) {
                                                                                    e = e16;
                                                                                    multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$12;
                                                                                    str3 = str56;
                                                                                    obj29 = obj5;
                                                                                    Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                                                                                    return Unit.INSTANCE;
                                                                                }
                                                                            }
                                                                            str8 = str16;
                                                                            str5 = str19;
                                                                            continuation3 = continuation2;
                                                                            if (!it.hasNext()) {
                                                                                return Unit.INSTANCE;
                                                                            }
                                                                        }
                                                                    }
                                                                    Filepress filepress = new Filepress();
                                                                    multiCloudLinks$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                                    multiCloudLinks$getUrl$12.L$1 = str14;
                                                                    multiCloudLinks$getUrl$12.L$2 = str60;
                                                                    multiCloudLinks$getUrl$12.L$3 = function14;
                                                                    multiCloudLinks$getUrl$12.L$4 = function116;
                                                                    multiCloudLinks$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                                    multiCloudLinks$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                                    multiCloudLinks$getUrl$12.L$7 = str13;
                                                                    multiCloudLinks$getUrl$12.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                                    multiCloudLinks$getUrl$12.L$9 = it;
                                                                    multiCloudLinks$getUrl$12.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                                    multiCloudLinks$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(element8);
                                                                    multiCloudLinks$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(str59);
                                                                    multiCloudLinks$getUrl$12.L$13 = SpillingKt.nullOutSpilledVariable(obj24);
                                                                    multiCloudLinks$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                                    multiCloudLinks$getUrl$12.L$15 = null;
                                                                    multiCloudLinks$getUrl$12.L$16 = null;
                                                                    multiCloudLinks$getUrl$12.L$17 = null;
                                                                    multiCloudLinks$getUrl$12.L$18 = null;
                                                                    multiCloudLinks$getUrl$12.L$19 = null;
                                                                    multiCloudLinks$getUrl$12.I$0 = i;
                                                                    multiCloudLinks$getUrl$12.I$1 = i2;
                                                                    multiCloudLinks$getUrl$12.I$2 = 0;
                                                                    multiCloudLinks$getUrl$12.label = 9;
                                                                    String str76 = str60;
                                                                    MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$113 = multiCloudLinks$getUrl$12;
                                                                    Function1<? super SubtitleFile, Unit> function124 = function14;
                                                                    Function1<? super ExtractorLink, Unit> function125 = function116;
                                                                    String str77 = str59;
                                                                    if (filepress.getUrl(str77, str76, function124, function125, multiCloudLinks$getUrl$113) == obj6) {
                                                                        return obj6;
                                                                    }
                                                                    obj6 = obj6;
                                                                    it = it;
                                                                    document = document;
                                                                    str14 = str14;
                                                                    str16 = str8;
                                                                    str19 = str5;
                                                                    obj5 = obj5;
                                                                    i = i;
                                                                    function15 = function125;
                                                                    str10 = str76;
                                                                    i2 = i2;
                                                                    function14 = function124;
                                                                    multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                                                                    multiCloudLinks4 = multiCloudLinks3;
                                                                    str8 = str16;
                                                                    str5 = str19;
                                                                    continuation3 = continuation2;
                                                                    if (!it.hasNext()) {
                                                                    }
                                                                }
                                                            }
                                                            Requests app2 = MainActivityKt.getApp();
                                                            multiCloudLinks$getUrl$19.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                            multiCloudLinks$getUrl$19.L$1 = str14;
                                                            multiCloudLinks$getUrl$19.L$2 = str62;
                                                            multiCloudLinks$getUrl$19.L$3 = function110;
                                                            multiCloudLinks$getUrl$19.L$4 = function117;
                                                            multiCloudLinks$getUrl$19.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                            multiCloudLinks$getUrl$19.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                            multiCloudLinks$getUrl$19.L$7 = str13;
                                                            multiCloudLinks$getUrl$19.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                            multiCloudLinks$getUrl$19.L$9 = it;
                                                            multiCloudLinks$getUrl$19.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                            multiCloudLinks$getUrl$19.L$11 = SpillingKt.nullOutSpilledVariable(element8);
                                                            multiCloudLinks$getUrl$19.L$12 = str61;
                                                            multiCloudLinks$getUrl$19.L$13 = SpillingKt.nullOutSpilledVariable(obj24);
                                                            multiCloudLinks$getUrl$19.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                            multiCloudLinks$getUrl$19.L$15 = null;
                                                            multiCloudLinks$getUrl$19.L$16 = null;
                                                            multiCloudLinks$getUrl$19.L$17 = null;
                                                            multiCloudLinks$getUrl$19.L$18 = null;
                                                            multiCloudLinks$getUrl$19.L$19 = null;
                                                            multiCloudLinks$getUrl$19.I$0 = i;
                                                            multiCloudLinks$getUrl$19.I$1 = i2;
                                                            multiCloudLinks$getUrl$19.I$2 = 0;
                                                            multiCloudLinks$getUrl$19.label = 7;
                                                            String str78 = str13;
                                                            Object obj31 = obj6;
                                                            int i30 = i2;
                                                            int i31 = i;
                                                            Function1<? super ExtractorLink, Unit> function126 = function117;
                                                            Function1<? super SubtitleFile, Unit> function127 = function110;
                                                            Iterator it9 = it;
                                                            String str79 = str61;
                                                            MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$114 = multiCloudLinks$getUrl$19;
                                                            str3 = str62;
                                                            Object obj32 = Requests.get$default(app2, str79, (Map) null, str14, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, multiCloudLinks$getUrl$114, 4090, (Object) null);
                                                            if (obj32 == obj31) {
                                                                return obj31;
                                                            }
                                                            obj6 = obj31;
                                                            obj18 = obj32;
                                                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$114;
                                                            i14 = i31;
                                                            str36 = str79;
                                                            i13 = i30;
                                                            it = it9;
                                                            str38 = str78;
                                                            element6 = element8;
                                                            i16 = 0;
                                                            str35 = obj24;
                                                            function19 = function126;
                                                            function110 = function127;
                                                            try {
                                                            } catch (Exception e17) {
                                                                e = e17;
                                                                str37 = str38;
                                                                i15 = i16;
                                                                element7 = element6;
                                                                document6 = document;
                                                            }
                                                            String text2 = ((NiceResponse) obj18).getText();
                                                            element7 = element6;
                                                            int i32 = i16;
                                                            Object obj33 = obj6;
                                                            str40 = str35;
                                                            find$default = Regex.find$default(new Regex("const\\s+streamSrc\\s*=\\s*[\"']([^\"']+)[\"']"), text2, 0, 2, (Object) null);
                                                            if (find$default != null) {
                                                                try {
                                                                } catch (Exception e18) {
                                                                    try {
                                                                        try {
                                                                            try {
                                                                                e = e18;
                                                                                str35 = str40;
                                                                                obj6 = obj33;
                                                                                i15 = i32;
                                                                                str37 = str38;
                                                                                document6 = document;
                                                                                Log.INSTANCE.e(str46, "Player error: " + e.getMessage());
                                                                                document = r61;
                                                                                str4 = str46;
                                                                                str13 = str37;
                                                                                str16 = str8;
                                                                                str19 = str5;
                                                                                i2 = i13;
                                                                                i = i14;
                                                                                multiCloudLinks4 = multiCloudLinks3;
                                                                                str10 = str45;
                                                                                function14 = function110;
                                                                                function15 = function19;
                                                                                str8 = str16;
                                                                                str5 = str19;
                                                                                continuation3 = continuation2;
                                                                                if (!it.hasNext()) {
                                                                                }
                                                                            } catch (Exception e19) {
                                                                                e = e19;
                                                                                str3 = str45;
                                                                                multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                                str4 = str46;
                                                                                function13 = function19;
                                                                                obj29 = obj5;
                                                                                Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                                                                                return Unit.INSTANCE;
                                                                            }
                                                                            str46 = str4;
                                                                        } catch (Exception e20) {
                                                                            e = e20;
                                                                            str3 = str45;
                                                                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                            function13 = function19;
                                                                            obj29 = obj5;
                                                                        }
                                                                        Document document10 = document6;
                                                                        str45 = str3;
                                                                    } catch (Exception e21) {
                                                                        e = e21;
                                                                        multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$14;
                                                                        function13 = function19;
                                                                        obj29 = obj5;
                                                                    }
                                                                }
                                                                List groupValues = find$default.getGroupValues();
                                                                if (groupValues != null && (str47 = (String) groupValues.get(1)) != null) {
                                                                    str48 = str47;
                                                                    str49 = str48;
                                                                    if (str49 != null || str49.length() == 0) {
                                                                        String str80 = "MultiCloud [Stream]" + str38;
                                                                        ExtractorLinkType infer_type3 = ExtractorApiKt.getINFER_TYPE();
                                                                        String str81 = str48;
                                                                        MultiCloudLinks$getUrl$2$3 multiCloudLinks$getUrl$2$3 = new MultiCloudLinks$getUrl$2$3(i14, str36, null);
                                                                        multiCloudLinks$getUrl$14.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                                        multiCloudLinks$getUrl$14.L$1 = str14;
                                                                        multiCloudLinks$getUrl$14.L$2 = str3;
                                                                        multiCloudLinks$getUrl$14.L$3 = function110;
                                                                        multiCloudLinks$getUrl$14.L$4 = function19;
                                                                        multiCloudLinks$getUrl$14.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                                        multiCloudLinks$getUrl$14.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                                        multiCloudLinks$getUrl$14.L$7 = str38;
                                                                        multiCloudLinks$getUrl$14.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                                        multiCloudLinks$getUrl$14.L$9 = it;
                                                                        multiCloudLinks$getUrl$14.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                                        multiCloudLinks$getUrl$14.L$11 = SpillingKt.nullOutSpilledVariable(element7);
                                                                        multiCloudLinks$getUrl$14.L$12 = SpillingKt.nullOutSpilledVariable(str36);
                                                                        multiCloudLinks$getUrl$14.L$13 = SpillingKt.nullOutSpilledVariable(str40);
                                                                        multiCloudLinks$getUrl$14.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                                        multiCloudLinks$getUrl$14.L$15 = SpillingKt.nullOutSpilledVariable(text2);
                                                                        multiCloudLinks$getUrl$14.L$16 = function19;
                                                                        multiCloudLinks$getUrl$14.L$17 = SpillingKt.nullOutSpilledVariable(str81);
                                                                        multiCloudLinks$getUrl$14.I$0 = i14;
                                                                        multiCloudLinks$getUrl$14.I$1 = i13;
                                                                        i15 = i32;
                                                                        multiCloudLinks$getUrl$14.I$2 = i15;
                                                                        str37 = str38;
                                                                        multiCloudLinks$getUrl$14.label = 8;
                                                                        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$115 = multiCloudLinks$getUrl$14;
                                                                        Object newExtractorLink3 = ExtractorApiKt.newExtractorLink("MultiCloud [Stream]", str80, str81, infer_type3, multiCloudLinks$getUrl$2$3, multiCloudLinks$getUrl$115);
                                                                        obj6 = obj33;
                                                                        if (newExtractorLink3 == obj6) {
                                                                            return obj6;
                                                                        }
                                                                        i17 = i15;
                                                                        obj20 = obj11;
                                                                        str39 = str37;
                                                                        function111 = function19;
                                                                        obj19 = newExtractorLink3;
                                                                        multiCloudLinks7 = multiCloudLinks3;
                                                                        try {
                                                                        } catch (Exception e22) {
                                                                            e = e22;
                                                                            i15 = i17;
                                                                            str37 = str39;
                                                                            multiCloudLinks3 = multiCloudLinks7;
                                                                            str35 = str40;
                                                                            document6 = document;
                                                                            Document document102 = document6;
                                                                            str45 = str3;
                                                                            str46 = str4;
                                                                            Log.INSTANCE.e(str46, "Player error: " + e.getMessage());
                                                                            document = document102;
                                                                            str4 = str46;
                                                                            str13 = str37;
                                                                            str16 = str8;
                                                                            str19 = str5;
                                                                            i2 = i13;
                                                                            i = i14;
                                                                            multiCloudLinks4 = multiCloudLinks3;
                                                                            str10 = str45;
                                                                            function14 = function110;
                                                                            function15 = function19;
                                                                            str8 = str16;
                                                                            str5 = str19;
                                                                            continuation3 = continuation2;
                                                                            if (!it.hasNext()) {
                                                                            }
                                                                        }
                                                                        function111.invoke(obj19);
                                                                        i = i14;
                                                                        str13 = str39;
                                                                        str16 = str8;
                                                                        str19 = str5;
                                                                        str10 = str3;
                                                                        i2 = i13;
                                                                        multiCloudLinks4 = multiCloudLinks7;
                                                                        function14 = function110;
                                                                        function15 = function19;
                                                                        str8 = str16;
                                                                        str5 = str19;
                                                                        continuation3 = continuation2;
                                                                        if (!it.hasNext()) {
                                                                        }
                                                                    } else {
                                                                        obj6 = obj33;
                                                                        i = i14;
                                                                        str13 = str38;
                                                                        multiCloudLinks4 = multiCloudLinks3;
                                                                        str16 = str8;
                                                                        str19 = str5;
                                                                        str10 = str3;
                                                                        i2 = i13;
                                                                        function14 = function110;
                                                                        function15 = function19;
                                                                        str8 = str16;
                                                                        str5 = str19;
                                                                        continuation3 = continuation2;
                                                                        if (!it.hasNext()) {
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            find$default2 = Regex.find$default(new Regex("const\\s+dbStreamSrc\\s*=\\s*[\"']([^\"']+)[\"']"), text2, 0, 2, (Object) null);
                                                            if (find$default2 != null) {
                                                                try {
                                                                } catch (Exception e23) {
                                                                    e = e23;
                                                                    str35 = str40;
                                                                    obj6 = obj33;
                                                                    i15 = i32;
                                                                    str37 = str38;
                                                                    document6 = document;
                                                                    Document document1022 = document6;
                                                                    str45 = str3;
                                                                    str46 = str4;
                                                                    Log.INSTANCE.e(str46, "Player error: " + e.getMessage());
                                                                    document = document1022;
                                                                    str4 = str46;
                                                                    str13 = str37;
                                                                    str16 = str8;
                                                                    str19 = str5;
                                                                    i2 = i13;
                                                                    i = i14;
                                                                    multiCloudLinks4 = multiCloudLinks3;
                                                                    str10 = str45;
                                                                    function14 = function110;
                                                                    function15 = function19;
                                                                    str8 = str16;
                                                                    str5 = str19;
                                                                    continuation3 = continuation2;
                                                                    if (!it.hasNext()) {
                                                                    }
                                                                }
                                                                List groupValues2 = find$default2.getGroupValues();
                                                                if (groupValues2 != null) {
                                                                    str48 = (String) groupValues2.get(1);
                                                                    str49 = str48;
                                                                    if (str49 != null || str49.length() == 0) {
                                                                    }
                                                                }
                                                            }
                                                            str48 = null;
                                                            str49 = str48;
                                                            if (str49 != null || str49.length() == 0) {
                                                            }
                                                        }
                                                    }
                                                    Requests app3 = MainActivityKt.getApp();
                                                    multiCloudLinks$getUrl$17.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                    multiCloudLinks$getUrl$17.L$1 = str14;
                                                    multiCloudLinks$getUrl$17.L$2 = str3;
                                                    function14 = function119;
                                                    multiCloudLinks$getUrl$17.L$3 = function14;
                                                    function13 = function118;
                                                    multiCloudLinks$getUrl$17.L$4 = function13;
                                                    multiCloudLinks$getUrl$17.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                    multiCloudLinks$getUrl$17.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                    str33 = str64;
                                                    multiCloudLinks$getUrl$17.L$7 = str33;
                                                    multiCloudLinks$getUrl$17.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                    it5 = it7;
                                                    multiCloudLinks$getUrl$17.L$9 = it5;
                                                    multiCloudLinks$getUrl$17.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                    multiCloudLinks$getUrl$17.L$11 = SpillingKt.nullOutSpilledVariable(element5);
                                                    multiCloudLinks$getUrl$17.L$12 = SpillingKt.nullOutSpilledVariable(str63);
                                                    multiCloudLinks$getUrl$17.L$13 = SpillingKt.nullOutSpilledVariable(str31);
                                                    multiCloudLinks$getUrl$17.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                    multiCloudLinks$getUrl$17.L$15 = null;
                                                    multiCloudLinks$getUrl$17.L$16 = null;
                                                    multiCloudLinks$getUrl$17.L$17 = null;
                                                    multiCloudLinks$getUrl$17.L$18 = null;
                                                    multiCloudLinks$getUrl$17.L$19 = null;
                                                    int i33 = i25;
                                                    multiCloudLinks$getUrl$17.I$0 = i33;
                                                    i12 = i24;
                                                    multiCloudLinks$getUrl$17.I$1 = i12;
                                                    i10 = i26;
                                                    multiCloudLinks$getUrl$17.I$2 = i10;
                                                    multiCloudLinks$getUrl$17.label = 5;
                                                    MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$116 = multiCloudLinks$getUrl$17;
                                                    obj17 = Requests.get$default(app3, str63, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, multiCloudLinks$getUrl$116, 4062, (Object) null);
                                                    str30 = str63;
                                                    if (obj17 == obj27) {
                                                        return obj27;
                                                    }
                                                    i11 = i33;
                                                    element4 = element5;
                                                    obj16 = obj27;
                                                    multiCloudLinks6 = multiCloudLinks3;
                                                    try {
                                                    } catch (Exception e24) {
                                                        e = e24;
                                                        Element element12 = element4;
                                                        str32 = str8;
                                                        str19 = str5;
                                                        multiCloudLinks3 = multiCloudLinks6;
                                                        element5 = element12;
                                                        document5 = document;
                                                    }
                                                    NiceResponse niceResponse = (NiceResponse) obj17;
                                                    MultiCloudLinks multiCloudLinks8 = multiCloudLinks6;
                                                    Element element13 = element4;
                                                    String str82 = str5;
                                                    str51 = niceResponse.getHeaders().get(str82);
                                                    if (str51 != null) {
                                                        try {
                                                        } catch (Exception e25) {
                                                            e = e25;
                                                            str19 = str82;
                                                            str32 = str8;
                                                            multiCloudLinks3 = multiCloudLinks8;
                                                            element5 = element13;
                                                            document5 = document;
                                                        }
                                                        str19 = str82;
                                                        str32 = str8;
                                                        str51 = niceResponse.getHeaders().get(str32);
                                                    } else {
                                                        str19 = str82;
                                                        str32 = str8;
                                                    }
                                                    str52 = str51;
                                                    if (str52 != null || str52.length() == 0) {
                                                        String str83 = str30;
                                                        try {
                                                        } catch (Exception e26) {
                                                            try {
                                                                try {
                                                                    e = e26;
                                                                    multiCloudLinks3 = multiCloudLinks8;
                                                                    element5 = element13;
                                                                    str30 = str83;
                                                                    document5 = document;
                                                                    String str84 = str33;
                                                                    Iterator it10 = it5;
                                                                    Log.INSTANCE.e(str34, "Viking error: " + e.getMessage());
                                                                    document = r61;
                                                                    function15 = function13;
                                                                    str16 = str32;
                                                                    str4 = str34;
                                                                    i = i11;
                                                                    multiCloudLinks4 = multiCloudLinks3;
                                                                    str10 = str50;
                                                                    i2 = i12;
                                                                    multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$17;
                                                                    obj6 = obj16;
                                                                    str13 = str84;
                                                                    it = it10;
                                                                    str8 = str16;
                                                                    str5 = str19;
                                                                    continuation3 = continuation2;
                                                                    if (!it.hasNext()) {
                                                                    }
                                                                } catch (Exception e27) {
                                                                    e = e27;
                                                                    str3 = str50;
                                                                    multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$17;
                                                                    str4 = str34;
                                                                    obj29 = obj5;
                                                                    Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                                                                    return Unit.INSTANCE;
                                                                }
                                                                str50 = str3;
                                                            } catch (Exception e28) {
                                                                e = e28;
                                                                multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$17;
                                                                str4 = str34;
                                                                obj29 = obj5;
                                                            }
                                                            Document document11 = document5;
                                                        }
                                                        multiCloudLinks$getUrl$17.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks8);
                                                        multiCloudLinks$getUrl$17.L$1 = str14;
                                                        multiCloudLinks$getUrl$17.L$2 = str3;
                                                        multiCloudLinks$getUrl$17.L$3 = function14;
                                                        multiCloudLinks$getUrl$17.L$4 = function13;
                                                        multiCloudLinks$getUrl$17.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                        multiCloudLinks$getUrl$17.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                        multiCloudLinks$getUrl$17.L$7 = str33;
                                                        multiCloudLinks$getUrl$17.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                        multiCloudLinks$getUrl$17.L$9 = it5;
                                                        multiCloudLinks$getUrl$17.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                        multiCloudLinks$getUrl$17.L$11 = SpillingKt.nullOutSpilledVariable(element13);
                                                        multiCloudLinks$getUrl$17.L$12 = SpillingKt.nullOutSpilledVariable(str83);
                                                        multiCloudLinks$getUrl$17.L$13 = SpillingKt.nullOutSpilledVariable(str31);
                                                        multiCloudLinks$getUrl$17.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                        multiCloudLinks$getUrl$17.L$15 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                                        multiCloudLinks$getUrl$17.L$16 = SpillingKt.nullOutSpilledVariable(str51);
                                                        multiCloudLinks$getUrl$17.I$0 = i11;
                                                        multiCloudLinks$getUrl$17.I$1 = i12;
                                                        multiCloudLinks$getUrl$17.I$2 = i10;
                                                        multiCloudLinks$getUrl$17.label = 6;
                                                        if (ExtractorApiKt.loadExtractor(str51, function14, function13, multiCloudLinks$getUrl$17) == obj16) {
                                                            return obj16;
                                                        }
                                                        int i34 = i12;
                                                        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$117 = multiCloudLinks$getUrl$17;
                                                        str4 = str34;
                                                        i = i11;
                                                        str12 = str12;
                                                        str16 = str32;
                                                        i2 = i34;
                                                        it = it5;
                                                        multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$117;
                                                        function15 = function13;
                                                        obj6 = obj16;
                                                        str10 = str3;
                                                        str13 = str33;
                                                        multiCloudLinks4 = multiCloudLinks8;
                                                    } else {
                                                        str16 = str32;
                                                        str4 = str34;
                                                        i = i11;
                                                        multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$17;
                                                        i2 = i12;
                                                        it = it5;
                                                        obj6 = obj16;
                                                        function15 = function13;
                                                        str10 = str3;
                                                        str13 = str33;
                                                        multiCloudLinks4 = multiCloudLinks8;
                                                    }
                                                    str8 = str16;
                                                    str5 = str19;
                                                    continuation3 = continuation2;
                                                    if (!it.hasNext()) {
                                                    }
                                                }
                                                Requests app4 = MainActivityKt.getApp();
                                                multiCloudLinks$getUrl$16.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                multiCloudLinks$getUrl$16.L$1 = str14;
                                                multiCloudLinks$getUrl$16.L$2 = str3;
                                                multiCloudLinks$getUrl$16.L$3 = function14;
                                                multiCloudLinks$getUrl$16.L$4 = function114;
                                                multiCloudLinks$getUrl$16.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                multiCloudLinks$getUrl$16.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                multiCloudLinks$getUrl$16.L$7 = str23;
                                                multiCloudLinks$getUrl$16.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                multiCloudLinks$getUrl$16.L$9 = it3;
                                                multiCloudLinks$getUrl$16.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                multiCloudLinks$getUrl$16.L$11 = SpillingKt.nullOutSpilledVariable(element9);
                                                multiCloudLinks$getUrl$16.L$12 = str55;
                                                multiCloudLinks$getUrl$16.L$13 = SpillingKt.nullOutSpilledVariable(str21);
                                                multiCloudLinks$getUrl$16.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                multiCloudLinks$getUrl$16.L$15 = null;
                                                multiCloudLinks$getUrl$16.L$16 = null;
                                                multiCloudLinks$getUrl$16.L$17 = null;
                                                multiCloudLinks$getUrl$16.L$18 = null;
                                                multiCloudLinks$getUrl$16.L$19 = null;
                                                multiCloudLinks$getUrl$16.I$0 = i20;
                                                multiCloudLinks$getUrl$16.I$1 = i7;
                                                multiCloudLinks$getUrl$16.I$2 = i9;
                                                multiCloudLinks$getUrl$16.label = 3;
                                                MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$118 = multiCloudLinks$getUrl$16;
                                                String str85 = str55;
                                                obj12 = Requests.get$default(app4, str85, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, multiCloudLinks$getUrl$118, 4062, (Object) null);
                                                if (obj12 == obj25) {
                                                    return obj25;
                                                }
                                                function17 = function114;
                                                i6 = i20;
                                                obj10 = obj25;
                                                str24 = str55;
                                                element2 = element9;
                                                try {
                                                } catch (Exception e29) {
                                                    e = e29;
                                                    str22 = str24;
                                                    i8 = i9;
                                                    str16 = str25;
                                                    str4 = str27;
                                                    element3 = element2;
                                                    document3 = document;
                                                }
                                                NiceResponse niceResponse2 = (NiceResponse) obj12;
                                                str22 = str24;
                                                element3 = element2;
                                                String str86 = str19;
                                                str54 = niceResponse2.getHeaders().get(str86);
                                                if (str54 == null) {
                                                    try {
                                                    } catch (Exception e30) {
                                                        try {
                                                            e = e30;
                                                            i8 = i9;
                                                            str16 = str25;
                                                            str4 = str27;
                                                            str19 = str86;
                                                            document3 = document;
                                                            Log.INSTANCE.e(str53, "Pixeldrain error: " + e.getMessage());
                                                            document = document3;
                                                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$16;
                                                            i2 = i7;
                                                            str4 = str53;
                                                            it = it3;
                                                            obj6 = obj10;
                                                            i = i6;
                                                            function15 = function17;
                                                            str10 = str3;
                                                            str13 = str23;
                                                            multiCloudLinks4 = multiCloudLinks3;
                                                            str8 = str16;
                                                            str5 = str19;
                                                            continuation3 = continuation2;
                                                            if (!it.hasNext()) {
                                                            }
                                                        } catch (Exception e31) {
                                                            e = e31;
                                                            multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$16;
                                                            str4 = str53;
                                                            function13 = function17;
                                                            obj29 = obj5;
                                                            Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                                                            return Unit.INSTANCE;
                                                        }
                                                        str53 = str4;
                                                    }
                                                    str54 = niceResponse2.getHeaders().get(str25);
                                                    if (str54 == null) {
                                                        str54 = str22;
                                                    }
                                                }
                                                str16 = str25;
                                                str4 = str27;
                                                str19 = str86;
                                                String substringAfterLast$default = StringsKt.substringAfterLast$default(str54, "/", (String) null, 2, (Object) null);
                                                String str87 = str54;
                                                String str88 = str6;
                                                String substringBefore$default = StringsKt.substringBefore$default(substringAfterLast$default, str88, (String) null, 2, (Object) null);
                                                str6 = str88;
                                                obj22 = obj10;
                                                i8 = i9;
                                                String str89 = !StringsKt.contains$default(str87, "download", false, 2, (Object) null) ? str87 : "https://pixeldrain.com/api/file/" + substringBefore$default + "?download";
                                                String str90 = "MultiCloud Pixeldrain" + str23;
                                                ExtractorLinkType infer_type4 = ExtractorApiKt.getINFER_TYPE();
                                                String str91 = str89;
                                                MultiCloudLinks$getUrl$2$2 multiCloudLinks$getUrl$2$2 = new MultiCloudLinks$getUrl$2$2(i6, null);
                                                multiCloudLinks$getUrl$16.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                                multiCloudLinks$getUrl$16.L$1 = str14;
                                                multiCloudLinks$getUrl$16.L$2 = str3;
                                                multiCloudLinks$getUrl$16.L$3 = function14;
                                                multiCloudLinks$getUrl$16.L$4 = function17;
                                                multiCloudLinks$getUrl$16.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                                multiCloudLinks$getUrl$16.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                                multiCloudLinks$getUrl$16.L$7 = str23;
                                                multiCloudLinks$getUrl$16.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                                multiCloudLinks$getUrl$16.L$9 = it3;
                                                multiCloudLinks$getUrl$16.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                                multiCloudLinks$getUrl$16.L$11 = SpillingKt.nullOutSpilledVariable(element3);
                                                multiCloudLinks$getUrl$16.L$12 = SpillingKt.nullOutSpilledVariable(str22);
                                                multiCloudLinks$getUrl$16.L$13 = SpillingKt.nullOutSpilledVariable(str21);
                                                multiCloudLinks$getUrl$16.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                                multiCloudLinks$getUrl$16.L$15 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                                                multiCloudLinks$getUrl$16.L$16 = function17;
                                                multiCloudLinks$getUrl$16.L$17 = SpillingKt.nullOutSpilledVariable(substringBefore$default);
                                                multiCloudLinks$getUrl$16.L$18 = SpillingKt.nullOutSpilledVariable(str91);
                                                multiCloudLinks$getUrl$16.L$19 = SpillingKt.nullOutSpilledVariable(str87);
                                                multiCloudLinks$getUrl$16.I$0 = i6;
                                                multiCloudLinks$getUrl$16.I$1 = i7;
                                                multiCloudLinks$getUrl$16.I$2 = i8;
                                                i8 = i8;
                                                multiCloudLinks$getUrl$16.label = 4;
                                                MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$119 = multiCloudLinks$getUrl$16;
                                                newExtractorLink = ExtractorApiKt.newExtractorLink("MultiCloud Pixeldrain", str90, str91, infer_type4, multiCloudLinks$getUrl$2$2, multiCloudLinks$getUrl$119);
                                                if (newExtractorLink != obj22) {
                                                    return obj22;
                                                }
                                                obj15 = obj22;
                                                function18 = function17;
                                                obj14 = obj5;
                                                obj13 = obj11;
                                                it4 = it3;
                                                obj29 = newExtractorLink;
                                                multiCloudLinks5 = multiCloudLinks3;
                                                document4 = document;
                                                str28 = str26;
                                                str29 = str12;
                                                try {
                                                } catch (Exception e32) {
                                                    e = e32;
                                                    Document document12 = document4;
                                                    multiCloudLinks3 = multiCloudLinks5;
                                                    document3 = document12;
                                                    it3 = it4;
                                                    str12 = str29;
                                                    obj5 = obj14;
                                                    obj10 = obj15;
                                                    str53 = str4;
                                                    Log.INSTANCE.e(str53, "Pixeldrain error: " + e.getMessage());
                                                    document = document3;
                                                    multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$16;
                                                    i2 = i7;
                                                    str4 = str53;
                                                    it = it3;
                                                    obj6 = obj10;
                                                    i = i6;
                                                    function15 = function17;
                                                    str10 = str3;
                                                    str13 = str23;
                                                    multiCloudLinks4 = multiCloudLinks3;
                                                    str8 = str16;
                                                    str5 = str19;
                                                    continuation3 = continuation2;
                                                    if (!it.hasNext()) {
                                                    }
                                                }
                                                function18.invoke(obj29);
                                                i2 = i7;
                                                i = i6;
                                                function15 = function17;
                                                str12 = str29;
                                                str10 = str3;
                                                obj6 = obj15;
                                                document = document4;
                                                multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$16;
                                                it = it4;
                                                obj5 = obj14;
                                                str13 = str23;
                                                multiCloudLinks4 = multiCloudLinks5;
                                                str8 = str16;
                                                str5 = str19;
                                                continuation3 = continuation2;
                                                if (!it.hasNext()) {
                                                }
                                            }
                                        }
                                    }
                                    str20 = str13;
                                    obj28 = obj6;
                                    str3 = str10;
                                    i4 = i2;
                                    i27 = i;
                                    element10 = element8;
                                    str17 = obj24;
                                    str16 = str8;
                                    str19 = str5;
                                    str65 = str4;
                                    function13 = function15;
                                    it2 = it;
                                    multiCloudLinks$getUrl$110 = multiCloudLinks$getUrl$14;
                                    i28 = 0;
                                    str66 = obj23;
                                    str4 = str65;
                                    Object obj302 = obj28;
                                    if (StringsKt.contains$default(str66, "action=download", false, 2, (Object) null)) {
                                    }
                                    String str702 = "MultiCloud [Turbo R2]" + str20;
                                    ExtractorLinkType infer_type5 = ExtractorApiKt.getINFER_TYPE();
                                    MultiCloudLinks$getUrl$2$1 multiCloudLinks$getUrl$2$12 = new MultiCloudLinks$getUrl$2$1(i27, str14, null);
                                    multiCloudLinks$getUrl$110.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                                    multiCloudLinks$getUrl$110.L$1 = str14;
                                    multiCloudLinks$getUrl$110.L$2 = str3;
                                    multiCloudLinks$getUrl$110.L$3 = function14;
                                    multiCloudLinks$getUrl$110.L$4 = function13;
                                    multiCloudLinks$getUrl$110.L$5 = SpillingKt.nullOutSpilledVariable(document);
                                    multiCloudLinks$getUrl$110.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                                    multiCloudLinks$getUrl$110.L$7 = str20;
                                    multiCloudLinks$getUrl$110.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                                    multiCloudLinks$getUrl$110.L$9 = it2;
                                    multiCloudLinks$getUrl$110.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                                    multiCloudLinks$getUrl$110.L$11 = SpillingKt.nullOutSpilledVariable(element10);
                                    multiCloudLinks$getUrl$110.L$12 = SpillingKt.nullOutSpilledVariable(str66);
                                    multiCloudLinks$getUrl$110.L$13 = SpillingKt.nullOutSpilledVariable(str17);
                                    multiCloudLinks$getUrl$110.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                                    multiCloudLinks$getUrl$110.L$15 = SpillingKt.nullOutSpilledVariable(str67);
                                    multiCloudLinks$getUrl$110.L$16 = function13;
                                    multiCloudLinks$getUrl$110.L$17 = null;
                                    multiCloudLinks$getUrl$110.L$18 = null;
                                    multiCloudLinks$getUrl$110.L$19 = null;
                                    multiCloudLinks$getUrl$110.I$0 = i27;
                                    multiCloudLinks$getUrl$110.I$1 = i4;
                                    multiCloudLinks$getUrl$110.I$2 = i28;
                                    multiCloudLinks$getUrl$110.label = 2;
                                    multiCloudLinks$getUrl$111 = multiCloudLinks$getUrl$110;
                                    newExtractorLink2 = ExtractorApiKt.newExtractorLink("MultiCloud [Turbo R2]", str702, str67, infer_type5, multiCloudLinks$getUrl$2$12, multiCloudLinks$getUrl$111);
                                    obj7 = obj302;
                                    if (newExtractorLink2 != obj7) {
                                    }
                                }
                            }
                            z = true;
                            if (!z) {
                            }
                            Iterable select2 = document9.select("a[href]");
                            Iterator it82 = select2.iterator();
                            document = document9;
                            str12 = obj4;
                            iterable = select2;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$13;
                            obj5 = obj;
                            str13 = str68;
                            it = it82;
                            i = indexQuality;
                            str14 = str9;
                            i2 = 0;
                            obj6 = obj2;
                            continuation3 = continuation;
                            if (!it.hasNext()) {
                            }
                        } else {
                            append = new StringBuilder().append(" [").append(obj4).append("]");
                        }
                        str68 = append.toString();
                        Iterable select22 = document9.select("a[href]");
                        Iterator it822 = select22.iterator();
                        document = document9;
                        str12 = obj4;
                        iterable = select22;
                        multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$13;
                        obj5 = obj;
                        str13 = str68;
                        it = it822;
                        i = indexQuality;
                        str14 = str9;
                        i2 = 0;
                        obj6 = obj2;
                        continuation3 = continuation;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 1:
                        Function1<? super ExtractorLink, Unit> function128 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        function14 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        str10 = (String) multiCloudLinks$getUrl$12.L$2;
                        str9 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks9 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            multiCloudLinks$getUrl$13 = multiCloudLinks$getUrl$12;
                            obj3 = obj29;
                            obj = obj3;
                            str6 = "?";
                            str8 = "Location";
                            str5 = "location";
                            str4 = "MultiCloud";
                            function15 = function128;
                            multiCloudLinks4 = multiCloudLinks9;
                            obj2 = coroutine_suspended;
                            str7 = null;
                            Document document92 = ((NiceResponse) obj3).getDocument();
                            Element selectFirst2 = document92.selectFirst("h1, h2, h3, title");
                            if (selectFirst2 == null) {
                            }
                            String str682 = "";
                            obj4 = StringsKt.trim(StringsKt.replace$default(StringsKt.replace$default(text != null ? "" : text, "Download Links - MultiCloud", "", false, 4, (Object) null), "Download Links", "", false, 4, (Object) null)).toString();
                            str11 = obj4;
                            if (str11.length() != 0) {
                            }
                            int indexQuality2 = ExtractorsKt.getIndexQuality(str11);
                            if (obj4.length() <= 0) {
                            }
                            str682 = append.toString();
                            Iterable select222 = document92.select("a[href]");
                            Iterator it8222 = select222.iterator();
                            document = document92;
                            str12 = obj4;
                            iterable = select222;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$13;
                            obj5 = obj;
                            str13 = str682;
                            it = it8222;
                            i = indexQuality2;
                            str14 = str9;
                            i2 = 0;
                            obj6 = obj2;
                            continuation3 = continuation;
                            if (!it.hasNext()) {
                            }
                        } catch (Exception e33) {
                            e = e33;
                            continuation2 = continuation;
                            multiCloudLinks3 = multiCloudLinks9;
                            str4 = "MultiCloud";
                            str3 = str10;
                            function13 = function128;
                            Log.INSTANCE.e(str4, "Error extracting MultiCloud: " + e.getMessage());
                            return Unit.INSTANCE;
                        }
                        break;
                    case 2:
                        i3 = multiCloudLinks$getUrl$12.I$2;
                        int i35 = multiCloudLinks$getUrl$12.I$1;
                        int i36 = multiCloudLinks$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function129 = (Function1) multiCloudLinks$getUrl$12.L$16;
                        String str92 = (String) multiCloudLinks$getUrl$12.L$13;
                        String str93 = (String) multiCloudLinks$getUrl$12.L$12;
                        element = (Element) multiCloudLinks$getUrl$12.L$11;
                        Object obj34 = multiCloudLinks$getUrl$12.L$10;
                        Iterator it11 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable2 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str94 = (String) multiCloudLinks$getUrl$12.L$7;
                        str15 = (String) multiCloudLinks$getUrl$12.L$6;
                        document2 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function130 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function131 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str95 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str96 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks10 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj29);
                        str3 = str95;
                        continuation2 = continuation;
                        multiCloudLinks3 = multiCloudLinks10;
                        str6 = "?";
                        str16 = "Location";
                        str4 = "MultiCloud";
                        str17 = str92;
                        str18 = str93;
                        i4 = i35;
                        str14 = str96;
                        multiCloudLinks$getUrl$15 = multiCloudLinks$getUrl$12;
                        obj7 = coroutine_suspended;
                        str19 = "location";
                        function16 = function129;
                        obj8 = obj34;
                        str20 = str94;
                        function13 = function130;
                        obj9 = obj29;
                        obj5 = obj9;
                        i5 = i36;
                        it2 = it11;
                        iterable = iterable2;
                        function14 = function131;
                        function16.invoke(obj9);
                        multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$15;
                        i = i5;
                        str12 = str15;
                        str13 = str20;
                        i2 = i4;
                        obj6 = obj7;
                        document = document2;
                        multiCloudLinks4 = multiCloudLinks3;
                        it = it2;
                        function15 = function13;
                        str10 = str3;
                        str8 = str16;
                        str5 = str19;
                        continuation3 = continuation2;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 3:
                        int i37 = multiCloudLinks$getUrl$12.I$2;
                        int i38 = multiCloudLinks$getUrl$12.I$1;
                        i6 = multiCloudLinks$getUrl$12.I$0;
                        String str97 = (String) multiCloudLinks$getUrl$12.L$14;
                        String str98 = (String) multiCloudLinks$getUrl$12.L$13;
                        String str99 = (String) multiCloudLinks$getUrl$12.L$12;
                        element2 = (Element) multiCloudLinks$getUrl$12.L$11;
                        Object obj35 = multiCloudLinks$getUrl$12.L$10;
                        Iterator it12 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable3 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str100 = (String) multiCloudLinks$getUrl$12.L$7;
                        str12 = (String) multiCloudLinks$getUrl$12.L$6;
                        Document document13 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function132 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function133 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str101 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str102 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks11 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            obj11 = obj35;
                            str24 = str99;
                            function17 = function132;
                            str25 = "Location";
                            str14 = str102;
                            str26 = str97;
                            str19 = "location";
                            i9 = i37;
                            multiCloudLinks3 = multiCloudLinks11;
                            obj12 = obj29;
                            str6 = "?";
                            str21 = str98;
                            str3 = str101;
                            continuation2 = continuation;
                            obj10 = coroutine_suspended;
                            str27 = "MultiCloud";
                            i7 = i38;
                            it3 = it12;
                            document = document13;
                            function14 = function133;
                            multiCloudLinks$getUrl$16 = multiCloudLinks$getUrl$12;
                            obj5 = obj12;
                            str23 = str100;
                            iterable = iterable3;
                        } catch (Exception e34) {
                            e = e34;
                            multiCloudLinks3 = multiCloudLinks11;
                            str6 = "?";
                            str16 = "Location";
                            str4 = "MultiCloud";
                            i7 = i38;
                            str21 = str98;
                            str22 = str99;
                            element3 = element2;
                            i8 = i37;
                            function17 = function132;
                            function14 = function133;
                            str3 = str101;
                            str14 = str102;
                            continuation2 = continuation;
                            obj10 = coroutine_suspended;
                            str19 = "location";
                            document3 = document13;
                            multiCloudLinks$getUrl$16 = multiCloudLinks$getUrl$12;
                            obj5 = obj29;
                            it3 = it12;
                            str23 = str100;
                            iterable = iterable3;
                            str53 = str4;
                            Log.INSTANCE.e(str53, "Pixeldrain error: " + e.getMessage());
                            document = document3;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$16;
                            i2 = i7;
                            str4 = str53;
                            it = it3;
                            obj6 = obj10;
                            i = i6;
                            function15 = function17;
                            str10 = str3;
                            str13 = str23;
                            multiCloudLinks4 = multiCloudLinks3;
                            str8 = str16;
                            str5 = str19;
                            continuation3 = continuation2;
                            if (!it.hasNext()) {
                            }
                        }
                        NiceResponse niceResponse22 = (NiceResponse) obj12;
                        str22 = str24;
                        element3 = element2;
                        String str862 = str19;
                        str54 = niceResponse22.getHeaders().get(str862);
                        if (str54 == null) {
                        }
                        str16 = str25;
                        str4 = str27;
                        str19 = str862;
                        String substringAfterLast$default2 = StringsKt.substringAfterLast$default(str54, "/", (String) null, 2, (Object) null);
                        String str872 = str54;
                        String str882 = str6;
                        String substringBefore$default2 = StringsKt.substringBefore$default(substringAfterLast$default2, str882, (String) null, 2, (Object) null);
                        str6 = str882;
                        obj22 = obj10;
                        i8 = i9;
                        if (!StringsKt.contains$default(str872, "download", false, 2, (Object) null)) {
                        }
                        String str902 = "MultiCloud Pixeldrain" + str23;
                        ExtractorLinkType infer_type42 = ExtractorApiKt.getINFER_TYPE();
                        String str912 = str89;
                        MultiCloudLinks$getUrl$2$2 multiCloudLinks$getUrl$2$22 = new MultiCloudLinks$getUrl$2$2(i6, null);
                        multiCloudLinks$getUrl$16.L$0 = SpillingKt.nullOutSpilledVariable(multiCloudLinks3);
                        multiCloudLinks$getUrl$16.L$1 = str14;
                        multiCloudLinks$getUrl$16.L$2 = str3;
                        multiCloudLinks$getUrl$16.L$3 = function14;
                        multiCloudLinks$getUrl$16.L$4 = function17;
                        multiCloudLinks$getUrl$16.L$5 = SpillingKt.nullOutSpilledVariable(document);
                        multiCloudLinks$getUrl$16.L$6 = SpillingKt.nullOutSpilledVariable(str12);
                        multiCloudLinks$getUrl$16.L$7 = str23;
                        multiCloudLinks$getUrl$16.L$8 = SpillingKt.nullOutSpilledVariable(iterable);
                        multiCloudLinks$getUrl$16.L$9 = it3;
                        multiCloudLinks$getUrl$16.L$10 = SpillingKt.nullOutSpilledVariable(obj11);
                        multiCloudLinks$getUrl$16.L$11 = SpillingKt.nullOutSpilledVariable(element3);
                        multiCloudLinks$getUrl$16.L$12 = SpillingKt.nullOutSpilledVariable(str22);
                        multiCloudLinks$getUrl$16.L$13 = SpillingKt.nullOutSpilledVariable(str21);
                        multiCloudLinks$getUrl$16.L$14 = SpillingKt.nullOutSpilledVariable(str26);
                        multiCloudLinks$getUrl$16.L$15 = SpillingKt.nullOutSpilledVariable(niceResponse22);
                        multiCloudLinks$getUrl$16.L$16 = function17;
                        multiCloudLinks$getUrl$16.L$17 = SpillingKt.nullOutSpilledVariable(substringBefore$default2);
                        multiCloudLinks$getUrl$16.L$18 = SpillingKt.nullOutSpilledVariable(str912);
                        multiCloudLinks$getUrl$16.L$19 = SpillingKt.nullOutSpilledVariable(str872);
                        multiCloudLinks$getUrl$16.I$0 = i6;
                        multiCloudLinks$getUrl$16.I$1 = i7;
                        multiCloudLinks$getUrl$16.I$2 = i8;
                        i8 = i8;
                        multiCloudLinks$getUrl$16.label = 4;
                        MultiCloudLinks$getUrl$1 multiCloudLinks$getUrl$1192 = multiCloudLinks$getUrl$16;
                        newExtractorLink = ExtractorApiKt.newExtractorLink("MultiCloud Pixeldrain", str902, str912, infer_type42, multiCloudLinks$getUrl$2$22, multiCloudLinks$getUrl$1192);
                        if (newExtractorLink != obj22) {
                        }
                        break;
                    case 4:
                        int i39 = multiCloudLinks$getUrl$12.I$2;
                        int i40 = multiCloudLinks$getUrl$12.I$1;
                        i6 = multiCloudLinks$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function134 = (Function1) multiCloudLinks$getUrl$12.L$16;
                        str28 = (String) multiCloudLinks$getUrl$12.L$14;
                        String str103 = (String) multiCloudLinks$getUrl$12.L$13;
                        String str104 = (String) multiCloudLinks$getUrl$12.L$12;
                        Element element14 = (Element) multiCloudLinks$getUrl$12.L$11;
                        obj13 = multiCloudLinks$getUrl$12.L$10;
                        it4 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable4 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str105 = (String) multiCloudLinks$getUrl$12.L$7;
                        str29 = (String) multiCloudLinks$getUrl$12.L$6;
                        document4 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function135 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function136 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str106 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str107 = (String) multiCloudLinks$getUrl$12.L$1;
                        multiCloudLinks5 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            str3 = str106;
                            str6 = "?";
                            str16 = "Location";
                            str4 = "MultiCloud";
                            i7 = i40;
                            i8 = i39;
                            str21 = str103;
                            str22 = str104;
                            element3 = element14;
                            iterable = iterable4;
                            str14 = str107;
                            function14 = function136;
                            obj14 = obj29;
                            str19 = "location";
                            obj15 = coroutine_suspended;
                            function18 = function134;
                            function17 = function135;
                            multiCloudLinks$getUrl$16 = multiCloudLinks$getUrl$12;
                            str23 = str105;
                            continuation2 = continuation;
                        } catch (Exception e35) {
                            e = e35;
                            multiCloudLinks3 = multiCloudLinks5;
                            document3 = document4;
                            obj5 = obj29;
                            it3 = it4;
                            str3 = str106;
                            function17 = function135;
                            str6 = "?";
                            str16 = "Location";
                            str4 = "MultiCloud";
                            i7 = i40;
                            i8 = i39;
                            str21 = str103;
                            str22 = str104;
                            element3 = element14;
                            iterable = iterable4;
                            str12 = str29;
                            str14 = str107;
                            function14 = function136;
                            obj10 = coroutine_suspended;
                            str19 = "location";
                            multiCloudLinks$getUrl$16 = multiCloudLinks$getUrl$12;
                            str23 = str105;
                            continuation2 = continuation;
                            str53 = str4;
                            Log.INSTANCE.e(str53, "Pixeldrain error: " + e.getMessage());
                            document = document3;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$16;
                            i2 = i7;
                            str4 = str53;
                            it = it3;
                            obj6 = obj10;
                            i = i6;
                            function15 = function17;
                            str10 = str3;
                            str13 = str23;
                            multiCloudLinks4 = multiCloudLinks3;
                            str8 = str16;
                            str5 = str19;
                            continuation3 = continuation2;
                            if (!it.hasNext()) {
                            }
                        }
                        function18.invoke(obj29);
                        i2 = i7;
                        i = i6;
                        function15 = function17;
                        str12 = str29;
                        str10 = str3;
                        obj6 = obj15;
                        document = document4;
                        multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$16;
                        it = it4;
                        obj5 = obj14;
                        str13 = str23;
                        multiCloudLinks4 = multiCloudLinks5;
                        str8 = str16;
                        str5 = str19;
                        continuation3 = continuation2;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 5:
                        int i41 = multiCloudLinks$getUrl$12.I$2;
                        int i42 = multiCloudLinks$getUrl$12.I$1;
                        int i43 = multiCloudLinks$getUrl$12.I$0;
                        String str108 = (String) multiCloudLinks$getUrl$12.L$14;
                        String str109 = (String) multiCloudLinks$getUrl$12.L$13;
                        str30 = (String) multiCloudLinks$getUrl$12.L$12;
                        element4 = (Element) multiCloudLinks$getUrl$12.L$11;
                        Object obj36 = multiCloudLinks$getUrl$12.L$10;
                        Iterator it13 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable5 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str110 = (String) multiCloudLinks$getUrl$12.L$7;
                        str12 = (String) multiCloudLinks$getUrl$12.L$6;
                        Document document14 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function137 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function138 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str111 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str112 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks12 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            obj17 = obj29;
                            str6 = "?";
                            str8 = "Location";
                            str5 = "location";
                            str31 = str109;
                            i10 = i41;
                            str14 = str112;
                            obj16 = coroutine_suspended;
                            i11 = i43;
                            str26 = str108;
                            it5 = it13;
                            document = document14;
                            function13 = function137;
                            obj11 = obj36;
                            multiCloudLinks6 = multiCloudLinks12;
                            multiCloudLinks$getUrl$17 = multiCloudLinks$getUrl$12;
                            obj5 = obj17;
                            str34 = "MultiCloud";
                            i12 = i42;
                            str33 = str110;
                            function14 = function138;
                            str3 = str111;
                            continuation2 = continuation;
                            iterable = iterable5;
                        } catch (Exception e36) {
                            e = e36;
                            str19 = "location";
                            i10 = i41;
                            multiCloudLinks3 = multiCloudLinks12;
                            str6 = "?";
                            element5 = element4;
                            str31 = str109;
                            str3 = str111;
                            continuation2 = continuation;
                            obj16 = coroutine_suspended;
                            str32 = "Location";
                            i11 = i43;
                            function13 = function137;
                            str14 = str112;
                            multiCloudLinks$getUrl$17 = multiCloudLinks$getUrl$12;
                            str33 = str110;
                            document5 = document14;
                            obj5 = obj29;
                            str34 = "MultiCloud";
                            i12 = i42;
                            it5 = it13;
                            iterable = iterable5;
                            function14 = function138;
                            Document document112 = document5;
                            str50 = str3;
                            String str842 = str33;
                            Iterator it102 = it5;
                            Log.INSTANCE.e(str34, "Viking error: " + e.getMessage());
                            document = document112;
                            function15 = function13;
                            str16 = str32;
                            str4 = str34;
                            i = i11;
                            multiCloudLinks4 = multiCloudLinks3;
                            str10 = str50;
                            i2 = i12;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$17;
                            obj6 = obj16;
                            str13 = str842;
                            it = it102;
                            str8 = str16;
                            str5 = str19;
                            continuation3 = continuation2;
                            if (!it.hasNext()) {
                            }
                        }
                        NiceResponse niceResponse3 = (NiceResponse) obj17;
                        MultiCloudLinks multiCloudLinks82 = multiCloudLinks6;
                        Element element132 = element4;
                        String str822 = str5;
                        str51 = niceResponse3.getHeaders().get(str822);
                        if (str51 != null) {
                        }
                        str52 = str51;
                        if (str52 != null) {
                            break;
                        }
                        if (str52 != null || str52.length() == 0) {
                        }
                        str8 = str16;
                        str5 = str19;
                        continuation3 = continuation2;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 6:
                        int i44 = multiCloudLinks$getUrl$12.I$2;
                        int i45 = multiCloudLinks$getUrl$12.I$1;
                        int i46 = multiCloudLinks$getUrl$12.I$0;
                        String str113 = (String) multiCloudLinks$getUrl$12.L$13;
                        str30 = (String) multiCloudLinks$getUrl$12.L$12;
                        Element element15 = (Element) multiCloudLinks$getUrl$12.L$11;
                        Object obj37 = multiCloudLinks$getUrl$12.L$10;
                        Iterator it14 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable6 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str114 = (String) multiCloudLinks$getUrl$12.L$7;
                        String str115 = (String) multiCloudLinks$getUrl$12.L$6;
                        Document document15 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function139 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function140 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str116 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str117 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks13 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            str19 = "location";
                            continuation2 = continuation;
                            str6 = "?";
                            document = document15;
                            str14 = str117;
                            function14 = function140;
                            obj5 = obj29;
                            iterable = iterable6;
                            str4 = "MultiCloud";
                            i = i46;
                            str12 = str115;
                            str16 = "Location";
                            i2 = i45;
                            it = it14;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            function15 = function139;
                            obj6 = coroutine_suspended;
                            str10 = str116;
                            str13 = str114;
                            multiCloudLinks4 = multiCloudLinks13;
                        } catch (Exception e37) {
                            e = e37;
                            str19 = "location";
                            i10 = i44;
                            str3 = str116;
                            continuation2 = continuation;
                            multiCloudLinks3 = multiCloudLinks13;
                            str6 = "?";
                            str32 = "Location";
                            str31 = str113;
                            element5 = element15;
                            document5 = document15;
                            str14 = str117;
                            obj16 = coroutine_suspended;
                            str34 = "MultiCloud";
                            i12 = i45;
                            i11 = i46;
                            function14 = function140;
                            function13 = function139;
                            multiCloudLinks$getUrl$17 = multiCloudLinks$getUrl$12;
                            str33 = str114;
                            obj5 = obj29;
                            it5 = it14;
                            iterable = iterable6;
                            str12 = str115;
                            Document document1122 = document5;
                            str50 = str3;
                            String str8422 = str33;
                            Iterator it1022 = it5;
                            Log.INSTANCE.e(str34, "Viking error: " + e.getMessage());
                            document = document1122;
                            function15 = function13;
                            str16 = str32;
                            str4 = str34;
                            i = i11;
                            multiCloudLinks4 = multiCloudLinks3;
                            str10 = str50;
                            i2 = i12;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$17;
                            obj6 = obj16;
                            str13 = str8422;
                            it = it1022;
                            str8 = str16;
                            str5 = str19;
                            continuation3 = continuation2;
                            if (!it.hasNext()) {
                            }
                        }
                        str8 = str16;
                        str5 = str19;
                        continuation3 = continuation2;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 7:
                        int i47 = multiCloudLinks$getUrl$12.I$2;
                        i13 = multiCloudLinks$getUrl$12.I$1;
                        i14 = multiCloudLinks$getUrl$12.I$0;
                        String str118 = (String) multiCloudLinks$getUrl$12.L$14;
                        str35 = (String) multiCloudLinks$getUrl$12.L$13;
                        str36 = (String) multiCloudLinks$getUrl$12.L$12;
                        element6 = (Element) multiCloudLinks$getUrl$12.L$11;
                        Object obj38 = multiCloudLinks$getUrl$12.L$10;
                        Iterator it15 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable7 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str119 = (String) multiCloudLinks$getUrl$12.L$7;
                        str12 = (String) multiCloudLinks$getUrl$12.L$6;
                        Document document16 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function141 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function142 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str120 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str121 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks14 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            multiCloudLinks3 = multiCloudLinks14;
                            obj18 = obj29;
                            str6 = "?";
                            str8 = "Location";
                            str5 = "location";
                            str4 = "MultiCloud";
                            i16 = i47;
                            str3 = str120;
                            str14 = str121;
                            continuation2 = continuation;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            obj6 = coroutine_suspended;
                            str26 = str118;
                            it = it15;
                            document = document16;
                            function19 = function141;
                            function110 = function142;
                            obj11 = obj38;
                            obj5 = obj18;
                            str38 = str119;
                            iterable = iterable7;
                        } catch (Exception e38) {
                            e = e38;
                            multiCloudLinks3 = multiCloudLinks14;
                            str6 = "?";
                            str8 = "Location";
                            str5 = "location";
                            str4 = "MultiCloud";
                            element7 = element6;
                            str3 = str120;
                            str14 = str121;
                            continuation2 = continuation;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            obj6 = coroutine_suspended;
                            i15 = i47;
                            it = it15;
                            str37 = str119;
                            function19 = function141;
                            iterable = iterable7;
                            document6 = document16;
                            obj5 = obj29;
                            function110 = function142;
                            Document document10222 = document6;
                            str45 = str3;
                            str46 = str4;
                            Log.INSTANCE.e(str46, "Player error: " + e.getMessage());
                            document = document10222;
                            str4 = str46;
                            str13 = str37;
                            str16 = str8;
                            str19 = str5;
                            i2 = i13;
                            i = i14;
                            multiCloudLinks4 = multiCloudLinks3;
                            str10 = str45;
                            function14 = function110;
                            function15 = function19;
                            str8 = str16;
                            str5 = str19;
                            continuation3 = continuation2;
                            if (!it.hasNext()) {
                            }
                        }
                        String text22 = ((NiceResponse) obj18).getText();
                        element7 = element6;
                        int i322 = i16;
                        Object obj332 = obj6;
                        str40 = str35;
                        find$default = Regex.find$default(new Regex("const\\s+streamSrc\\s*=\\s*[\"']([^\"']+)[\"']"), text22, 0, 2, (Object) null);
                        if (find$default != null) {
                        }
                        find$default2 = Regex.find$default(new Regex("const\\s+dbStreamSrc\\s*=\\s*[\"']([^\"']+)[\"']"), text22, 0, 2, (Object) null);
                        if (find$default2 != null) {
                        }
                        str48 = null;
                        str49 = str48;
                        if (str49 != null || str49.length() == 0) {
                        }
                        break;
                    case 8:
                        int i48 = multiCloudLinks$getUrl$12.I$2;
                        i13 = multiCloudLinks$getUrl$12.I$1;
                        i14 = multiCloudLinks$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function143 = (Function1) multiCloudLinks$getUrl$12.L$16;
                        str35 = (String) multiCloudLinks$getUrl$12.L$13;
                        i17 = i48;
                        String str122 = (String) multiCloudLinks$getUrl$12.L$12;
                        Element element16 = (Element) multiCloudLinks$getUrl$12.L$11;
                        Object obj39 = multiCloudLinks$getUrl$12.L$10;
                        Iterator it16 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable8 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        str39 = (String) multiCloudLinks$getUrl$12.L$7;
                        String str123 = (String) multiCloudLinks$getUrl$12.L$6;
                        Document document17 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function144 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function145 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str124 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str125 = (String) multiCloudLinks$getUrl$12.L$1;
                        multiCloudLinks7 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            str3 = str124;
                            str6 = "?";
                            str8 = "Location";
                            str5 = "location";
                            str4 = "MultiCloud";
                            str40 = str35;
                            element7 = element16;
                            str14 = str125;
                            function19 = function144;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            obj19 = obj29;
                            obj6 = coroutine_suspended;
                            obj20 = obj39;
                            it = it16;
                            iterable = iterable8;
                            str12 = str123;
                            function110 = function145;
                            obj5 = obj19;
                            function111 = function143;
                            str36 = str122;
                            document = document17;
                            continuation2 = continuation;
                        } catch (Exception e39) {
                            e = e39;
                            str3 = str124;
                            multiCloudLinks3 = multiCloudLinks7;
                            str6 = "?";
                            str8 = "Location";
                            str5 = "location";
                            str4 = "MultiCloud";
                            str36 = str122;
                            element7 = element16;
                            document6 = document17;
                            str14 = str125;
                            function19 = function144;
                            continuation2 = continuation;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            obj6 = coroutine_suspended;
                            i15 = i17;
                            it = it16;
                            str37 = str39;
                            str12 = str123;
                            iterable = iterable8;
                            obj5 = obj29;
                            function110 = function145;
                            Document document102222 = document6;
                            str45 = str3;
                            str46 = str4;
                            Log.INSTANCE.e(str46, "Player error: " + e.getMessage());
                            document = document102222;
                            str4 = str46;
                            str13 = str37;
                            str16 = str8;
                            str19 = str5;
                            i2 = i13;
                            i = i14;
                            multiCloudLinks4 = multiCloudLinks3;
                            str10 = str45;
                            function14 = function110;
                            function15 = function19;
                            str8 = str16;
                            str5 = str19;
                            continuation3 = continuation2;
                            if (!it.hasNext()) {
                            }
                        }
                        function111.invoke(obj19);
                        i = i14;
                        str13 = str39;
                        str16 = str8;
                        str19 = str5;
                        str10 = str3;
                        i2 = i13;
                        multiCloudLinks4 = multiCloudLinks7;
                        function14 = function110;
                        function15 = function19;
                        str8 = str16;
                        str5 = str19;
                        continuation3 = continuation2;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 9:
                        int i49 = multiCloudLinks$getUrl$12.I$2;
                        int i50 = multiCloudLinks$getUrl$12.I$1;
                        int i51 = multiCloudLinks$getUrl$12.I$0;
                        String str126 = (String) multiCloudLinks$getUrl$12.L$14;
                        String str127 = (String) multiCloudLinks$getUrl$12.L$13;
                        String str128 = (String) multiCloudLinks$getUrl$12.L$12;
                        Element element17 = (Element) multiCloudLinks$getUrl$12.L$11;
                        Object obj40 = multiCloudLinks$getUrl$12.L$10;
                        Iterator it17 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable9 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str129 = (String) multiCloudLinks$getUrl$12.L$7;
                        str12 = (String) multiCloudLinks$getUrl$12.L$6;
                        Document document18 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function146 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function147 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str130 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str131 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks15 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            str6 = "?";
                            str4 = "MultiCloud";
                            str13 = str129;
                            continuation2 = continuation;
                            iterable = iterable9;
                            obj6 = coroutine_suspended;
                            it = it17;
                            document = document18;
                            str14 = str131;
                            str16 = "Location";
                            str19 = "location";
                            obj5 = obj29;
                            i = i51;
                            function15 = function146;
                            str10 = str130;
                            i2 = i50;
                            function14 = function147;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            multiCloudLinks4 = multiCloudLinks15;
                        } catch (Exception e40) {
                            str6 = "?";
                            str8 = "Location";
                            str5 = "location";
                            str4 = "MultiCloud";
                            element8 = element17;
                            str13 = str129;
                            Function1<? super SubtitleFile, Unit> function148 = function147;
                            String str132 = str130;
                            continuation2 = continuation;
                            iterable = iterable9;
                            obj6 = coroutine_suspended;
                            it = it17;
                            document = document18;
                            str14 = str131;
                            str16 = str8;
                            str19 = str5;
                            obj5 = obj29;
                            i = i51;
                            function15 = function146;
                            str10 = str132;
                            i2 = i50;
                            function14 = function148;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            multiCloudLinks4 = multiCloudLinks15;
                            str8 = str16;
                            str5 = str19;
                            continuation3 = continuation2;
                            if (!it.hasNext()) {
                            }
                        }
                        str8 = str16;
                        str5 = str19;
                        continuation3 = continuation2;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 10:
                        int i52 = multiCloudLinks$getUrl$12.I$2;
                        i18 = multiCloudLinks$getUrl$12.I$1;
                        i19 = multiCloudLinks$getUrl$12.I$0;
                        str41 = (String) multiCloudLinks$getUrl$12.L$14;
                        str42 = (String) multiCloudLinks$getUrl$12.L$13;
                        Element element18 = (Element) multiCloudLinks$getUrl$12.L$11;
                        obj21 = multiCloudLinks$getUrl$12.L$10;
                        it6 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        Iterable iterable10 = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str133 = (String) multiCloudLinks$getUrl$12.L$7;
                        str12 = (String) multiCloudLinks$getUrl$12.L$6;
                        document7 = (Document) multiCloudLinks$getUrl$12.L$5;
                        function112 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function149 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str134 = (String) multiCloudLinks$getUrl$12.L$2;
                        str43 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks16 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj29);
                            str6 = "?";
                            str4 = "MultiCloud";
                            str13 = str133;
                            continuation2 = continuation;
                            iterable = iterable10;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            obj6 = coroutine_suspended;
                            it = it6;
                            document = document7;
                            multiCloudLinks4 = multiCloudLinks16;
                            str16 = "Location";
                            str19 = "location";
                            obj5 = obj29;
                            i = i19;
                            function15 = function112;
                            str10 = str134;
                            str14 = str43;
                            i2 = i18;
                            function14 = function149;
                        } catch (Exception e41) {
                            multiCloudLinks3 = multiCloudLinks16;
                            str6 = "?";
                            str8 = "Location";
                            str5 = "location";
                            str4 = "MultiCloud";
                            element8 = element18;
                            str13 = str133;
                            function113 = function149;
                            str44 = str134;
                            continuation2 = continuation;
                            iterable = iterable10;
                            multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                            obj6 = coroutine_suspended;
                            it = it6;
                            document = document7;
                            multiCloudLinks4 = multiCloudLinks3;
                            str16 = str8;
                            str19 = str5;
                            obj5 = obj29;
                            i = i19;
                            function15 = function112;
                            str10 = str44;
                            str14 = str43;
                            i2 = i18;
                            function14 = function113;
                            str8 = str16;
                            str5 = str19;
                            continuation3 = continuation2;
                            if (!it.hasNext()) {
                            }
                        }
                        str8 = str16;
                        str5 = str19;
                        continuation3 = continuation2;
                        if (!it.hasNext()) {
                        }
                        break;
                    case 11:
                        i22 = multiCloudLinks$getUrl$12.I$2;
                        i21 = multiCloudLinks$getUrl$12.I$1;
                        i23 = multiCloudLinks$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function150 = (Function1) multiCloudLinks$getUrl$12.L$15;
                        str57 = (String) multiCloudLinks$getUrl$12.L$14;
                        str58 = (String) multiCloudLinks$getUrl$12.L$13;
                        Element element19 = (Element) multiCloudLinks$getUrl$12.L$11;
                        obj26 = multiCloudLinks$getUrl$12.L$10;
                        Iterator it18 = (Iterator) multiCloudLinks$getUrl$12.L$9;
                        iterable = (Iterable) multiCloudLinks$getUrl$12.L$8;
                        String str135 = (String) multiCloudLinks$getUrl$12.L$7;
                        String str136 = (String) multiCloudLinks$getUrl$12.L$6;
                        document8 = (Document) multiCloudLinks$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function151 = (Function1) multiCloudLinks$getUrl$12.L$4;
                        Function1<? super SubtitleFile, Unit> function152 = (Function1) multiCloudLinks$getUrl$12.L$3;
                        String str137 = (String) multiCloudLinks$getUrl$12.L$2;
                        String str138 = (String) multiCloudLinks$getUrl$12.L$1;
                        MultiCloudLinks multiCloudLinks17 = (MultiCloudLinks) multiCloudLinks$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj29);
                        continuation2 = continuation;
                        multiCloudLinks3 = multiCloudLinks17;
                        str6 = "?";
                        str8 = "Location";
                        str5 = "location";
                        str4 = "MultiCloud";
                        function115 = function150;
                        element8 = element19;
                        str13 = str135;
                        str12 = str136;
                        str14 = str138;
                        str56 = str137;
                        function14 = function152;
                        function13 = function151;
                        obj5 = obj29;
                        obj6 = coroutine_suspended;
                        it = it18;
                        function115.invoke(obj29);
                        document = document8;
                        multiCloudLinks$getUrl$14 = multiCloudLinks$getUrl$12;
                        function15 = function13;
                        i2 = i21;
                        i = i23;
                        multiCloudLinks4 = multiCloudLinks3;
                        str16 = str8;
                        str19 = str5;
                        str10 = str56;
                        str8 = str16;
                        str5 = str19;
                        continuation3 = continuation2;
                        if (!it.hasNext()) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        multiCloudLinks2 = multiCloudLinks;
        multiCloudLinks$getUrl$1 = new MultiCloudLinks$getUrl$1(multiCloudLinks2, continuation);
        multiCloudLinks$getUrl$12 = multiCloudLinks$getUrl$1;
        Object obj292 = multiCloudLinks$getUrl$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (multiCloudLinks$getUrl$12.label) {
        }
    }

    @NotNull
    public String getMainUrl() {
        return this.mainUrl;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public boolean getRequiresReferer() {
        return this.requiresReferer;
    }

    @Nullable
    public Object getUrl(@NotNull String str, @Nullable String str2, @NotNull Function1<? super SubtitleFile, Unit> function1, @NotNull Function1<? super ExtractorLink, Unit> function12, @NotNull Continuation<? super Unit> continuation) {
        return getUrl$suspendImpl(this, str, str2, function1, function12, continuation);
    }
}
