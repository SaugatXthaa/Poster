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
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0016\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JH\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00052\b\u0010\u0011\u001a\u0004\u0018\u00010\u00052\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u000f0\u00132\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@¢\u0006\u0002\u0010\u0017R\u0014\u0010\u0004\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\u0005X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007R\u0014\u0010\n\u001a\u00020\u000bX\u0096D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r¨\u0006\u0018"}, d2 = {"Lcom/cncverse/Filepress;", "Lcom/lagradost/cloudstream3/utils/ExtractorApi;", "<init>", "()V", "name", "", "getName", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "requiresReferer", "", "getRequiresReferer", "()Z", "getUrl", "", "url", "referer", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "MLSBDProvider_debug"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nExtractors.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Extractors.kt\ncom/cncverse/Filepress\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,995:1\n1#2:996\n*E\n"})
/* loaded from: classes.dex */
public class Filepress extends ExtractorApi {
    private final boolean requiresReferer;

    @NotNull
    private final String name = "FilePress";

    @NotNull
    private final String mainUrl = "https://new1.filepress.lat";

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Can't wrap try/catch for region: R(11:173|(1:174)|175|176|177|178|179|180|181|182|(1:184)(6:185|15|16|17|18|(2:20|(3:22|23|24)(1:25))(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(11:173|174|175|176|177|178|179|180|181|182|(1:184)(6:185|15|16|17|18|(2:20|(3:22|23|24)(1:25))(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(12:71|72|73|74|75|76|77|78|79|80|81|(1:83)(7:84|85|86|87|88|89|(2:97|98)(2:91|(2:93|94)(3:95|96|(6:167|168|169|170|171|(11:173|174|175|176|177|178|179|180|181|182|(1:184)(6:185|15|16|17|18|(2:20|(3:22|23|24)(1:25))(0)))(3:197|18|(0)(0)))(0))))) */
    /* JADX WARN: Can't wrap try/catch for region: R(13:47|(1:48)|49|50|51|52|53|54|55|(3:57|(2:59|(5:61|(1:63)(1:128)|(1:65)|66|67)(1:129))(3:130|131|132)|(2:69|70)(12:71|72|73|74|75|76|77|78|79|80|81|(1:83)(7:84|85|86|87|88|89|(2:97|98)(2:91|(2:93|94)(3:95|96|(6:167|168|169|170|171|(11:173|174|175|176|177|178|179|180|181|182|(1:184)(6:185|15|16|17|18|(2:20|(3:22|23|24)(1:25))(0)))(3:197|18|(0)(0)))(0))))))(2:136|137)|26|27|(3:163|23|24)(0)) */
    /* JADX WARN: Can't wrap try/catch for region: R(13:47|48|49|50|51|52|53|54|55|(3:57|(2:59|(5:61|(1:63)(1:128)|(1:65)|66|67)(1:129))(3:130|131|132)|(2:69|70)(12:71|72|73|74|75|76|77|78|79|80|81|(1:83)(7:84|85|86|87|88|89|(2:97|98)(2:91|(2:93|94)(3:95|96|(6:167|168|169|170|171|(11:173|174|175|176|177|178|179|180|181|182|(1:184)(6:185|15|16|17|18|(2:20|(3:22|23|24)(1:25))(0)))(3:197|18|(0)(0)))(0))))))(2:136|137)|26|27|(3:163|23|24)(0)) */
    /* JADX WARN: Can't wrap try/catch for region: R(17:29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|(1:46)(13:47|48|49|50|51|52|53|54|55|(3:57|(2:59|(5:61|(1:63)(1:128)|(1:65)|66|67)(1:129))(3:130|131|132)|(2:69|70)(12:71|72|73|74|75|76|77|78|79|80|81|(1:83)(7:84|85|86|87|88|89|(2:97|98)(2:91|(2:93|94)(3:95|96|(6:167|168|169|170|171|(11:173|174|175|176|177|178|179|180|181|182|(1:184)(6:185|15|16|17|18|(2:20|(3:22|23|24)(1:25))(0)))(3:197|18|(0)(0)))(0))))))(2:136|137)|26|27|(3:163|23|24)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(4:(8:134|135|105|106|107|26|27|(0)(0))|102|103|104) */
    /* JADX WARN: Can't wrap try/catch for region: R(6:167|168|169|170|171|(11:173|174|175|176|177|178|179|180|181|182|(1:184)(6:185|15|16|17|18|(2:20|(3:22|23|24)(1:25))(0)))(3:197|18|(0)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(7:1|(2:3|(4:5|6|7|8))|434|6|7|8|(1:(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(7:84|85|86|87|88|89|(2:97|98)(2:91|(2:93|94)(3:95|96|(6:167|168|169|170|171|(11:173|174|175|176|177|178|179|180|181|182|(1:184)(6:185|15|16|17|18|(2:20|(3:22|23|24)(1:25))(0)))(3:197|18|(0)(0)))(0)))) */
    /* JADX WARN: Code restructure failed: missing block: B:100:0x0c9a, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:101:0x0c9b, code lost:
    
        r50 = r2;
        r52 = r9;
        r49 = r15;
        r15 = r53;
        r1 = r77;
        r5 = r6;
        r7 = r8;
        r8 = r10;
        r10 = r11;
        r11 = r12;
        r6 = r16;
        r9 = r31;
        r14 = r38;
        r12 = r43;
        r3 = r57;
     */
    /* JADX WARN: Code restructure failed: missing block: B:113:0x0e36, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:114:0x0e37, code lost:
    
        r77 = r1;
        r1 = r0;
        r33 = r12;
        r27 = r44;
        r6 = r45;
        r8 = r46;
        r0 = r77;
     */
    /* JADX WARN: Code restructure failed: missing block: B:119:0x0cd6, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:120:0x0cd7, code lost:
    
        r50 = r2;
        r52 = r9;
        r49 = r15;
        r6 = r16;
        r15 = r53;
        r1 = r76;
        r9 = r5;
        r7 = r8;
        r8 = r10;
        r10 = r11;
        r11 = r12;
        r12 = r13;
        r3 = r57;
        r4 = r73;
        r5 = r77;
     */
    /* JADX WARN: Code restructure failed: missing block: B:122:0x0cf6, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:123:0x0d32, code lost:
    
        r50 = r2;
        r52 = r9;
        r43 = r13;
        r49 = r15;
        r6 = r16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:124:0x0d4f, code lost:
    
        r15 = r53;
        r1 = r76;
        r9 = r5;
        r7 = r8;
        r8 = r10;
        r10 = r11;
        r11 = r12;
        r12 = r43;
        r5 = r77;
     */
    /* JADX WARN: Code restructure failed: missing block: B:126:0x0cf8, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:127:0x0cf9, code lost:
    
        r21 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:139:0x0d2f, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:140:0x0d30, code lost:
    
        r24 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:142:0x0d41, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:143:0x0d42, code lost:
    
        r24 = r1;
        r50 = r2;
        r6 = r7;
        r52 = r9;
        r43 = r13;
     */
    /* JADX WARN: Code restructure failed: missing block: B:148:0x0d81, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:149:0x0d82, code lost:
    
        r24 = r1;
        r50 = r2;
        r52 = r9;
        r14 = r15;
        r15 = r53;
        r5 = r77;
        r9 = r0;
        r3 = r57;
        r4 = r73;
        r1 = r76;
     */
    /* JADX WARN: Code restructure failed: missing block: B:151:0x0d99, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:152:0x0d9a, code lost:
    
        r24 = r1;
        r50 = r2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:153:0x0dae, code lost:
    
        r52 = r9;
        r14 = r15;
        r15 = r53;
        r5 = r77;
        r9 = r0;
        r1 = r76;
     */
    /* JADX WARN: Code restructure failed: missing block: B:155:0x0da3, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:156:0x0da4, code lost:
    
        r24 = r1;
        r50 = r2;
        r21 = r5;
     */
    /* JADX WARN: Code restructure failed: missing block: B:158:0x0dbc, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:159:0x0dbd, code lost:
    
        r24 = r1;
        r50 = r2;
        r52 = r9;
        r14 = r15;
        r21 = r48;
        r15 = r53;
        r9 = r0;
        r1 = r76;
     */
    /* JADX WARN: Code restructure failed: missing block: B:161:0x0dd5, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:162:0x0dd6, code lost:
    
        r50 = r2;
        r52 = r9;
        r14 = r15;
        r21 = r48;
        r24 = r51;
        r15 = r53;
        r9 = r0;
        r1 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:187:0x0bc7, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:188:0x0bc8, code lost:
    
        r1 = r7;
        r7 = r3;
        r3 = r11;
        r11 = r6;
        r6 = r29;
        r12 = r1;
        r10 = r5;
        r14 = r8;
        r5 = r16;
        r1 = r25;
        r22 = r27;
        r8 = r4;
        r4 = r81;
     */
    /* JADX WARN: Code restructure failed: missing block: B:190:0x0be0, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:191:0x0be1, code lost:
    
        r2 = r12;
        r12 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:192:0x0beb, code lost:
    
        r1 = r7;
        r7 = r3;
        r3 = r11;
        r11 = r6;
        r6 = r12;
        r12 = r1;
        r10 = r5;
        r14 = r8;
        r5 = r16;
        r1 = r25;
        r22 = r27;
     */
    /* JADX WARN: Code restructure failed: missing block: B:193:0x0c38, code lost:
    
        r8 = r4;
        r4 = r2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:199:0x0c19, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:200:0x0c1a, code lost:
    
        r2 = r12;
        r12 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:201:0x0c26, code lost:
    
        r15 = r53;
        r1 = r7;
        r7 = r3;
        r3 = r11;
        r11 = r6;
        r6 = r12;
        r12 = r1;
        r10 = r5;
        r14 = r8;
        r5 = r16;
        r1 = r25;
        r22 = r27;
     */
    /* JADX WARN: Code restructure failed: missing block: B:350:0x049d, code lost:
    
        if (r0 == null) goto L96;
     */
    /* JADX WARN: Code restructure failed: missing block: B:408:0x0400, code lost:
    
        if (r0 == null) goto L65;
     */
    /* JADX WARN: Code restructure failed: missing block: B:432:0x0242, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:433:0x0243, code lost:
    
        r22 = r76;
        r1 = r2;
        r21 = "method";
        r49 = "status";
        r50 = "application/json";
        r24 = "id";
        r52 = "data";
        r15 = "FilePress";
        r6 = r16;
        r9 = r31;
        r10 = r32;
        r8 = r34;
        r3 = r37;
        r14 = r38;
        r11 = r39;
        r12 = r43;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:8:0x0038. Please report as an issue. */
    /* JADX WARN: Not initialized variable reg: 31, insn: 0x0253: MOVE (r9 I:??[OBJECT, ARRAY]) = (r31 I:??[OBJECT, ARRAY]), block:B:433:0x0243 */
    /* JADX WARN: Not initialized variable reg: 32, insn: 0x0255: MOVE (r10 I:??[OBJECT, ARRAY]) = (r32 I:??[OBJECT, ARRAY]), block:B:433:0x0243 */
    /* JADX WARN: Not initialized variable reg: 34, insn: 0x0257: MOVE (r8 I:??[OBJECT, ARRAY]) = (r34 I:??[OBJECT, ARRAY]), block:B:433:0x0243 */
    /* JADX WARN: Not initialized variable reg: 37, insn: 0x0259: MOVE (r3 I:??[OBJECT, ARRAY]) = (r37 I:??[OBJECT, ARRAY]), block:B:433:0x0243 */
    /* JADX WARN: Not initialized variable reg: 38, insn: 0x025b: MOVE (r14 I:??[OBJECT, ARRAY]) = (r38 I:??[OBJECT, ARRAY]), block:B:433:0x0243 */
    /* JADX WARN: Not initialized variable reg: 39, insn: 0x025d: MOVE (r11 I:??[OBJECT, ARRAY]) = (r39 I:??[OBJECT, ARRAY]), block:B:433:0x0243 */
    /* JADX WARN: Not initialized variable reg: 43, insn: 0x025f: MOVE (r12 I:??[OBJECT, ARRAY]) = (r43 I:??[OBJECT, ARRAY]), block:B:433:0x0243 */
    /* JADX WARN: Removed duplicated region for block: B:12:0x0043  */
    /* JADX WARN: Removed duplicated region for block: B:136:0x0cfc  */
    /* JADX WARN: Removed duplicated region for block: B:163:0x0e46  */
    /* JADX WARN: Removed duplicated region for block: B:167:0x0a88 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:208:0x0114  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x0c3c  */
    /* JADX WARN: Removed duplicated region for block: B:211:0x01b1  */
    /* JADX WARN: Removed duplicated region for block: B:214:0x0265  */
    /* JADX WARN: Removed duplicated region for block: B:224:0x05c1  */
    /* JADX WARN: Removed duplicated region for block: B:226:0x05c4  */
    /* JADX WARN: Removed duplicated region for block: B:230:0x05cc A[Catch: Exception -> 0x05b7, TRY_ENTER, TRY_LEAVE, TryCatch #7 {Exception -> 0x05b7, blocks: (B:315:0x05af, B:230:0x05cc), top: B:314:0x05af }] */
    /* JADX WARN: Removed duplicated region for block: B:233:0x05d8  */
    /* JADX WARN: Removed duplicated region for block: B:250:0x06dc  */
    /* JADX WARN: Removed duplicated region for block: B:252:0x06e1  */
    /* JADX WARN: Removed duplicated region for block: B:255:0x06fa  */
    /* JADX WARN: Removed duplicated region for block: B:259:0x0731  */
    /* JADX WARN: Removed duplicated region for block: B:262:0x073a A[Catch: Exception -> 0x0718, TRY_ENTER, TRY_LEAVE, TryCatch #28 {Exception -> 0x0718, blocks: (B:278:0x06ff, B:262:0x073a, B:269:0x075e, B:274:0x076a), top: B:277:0x06ff }] */
    /* JADX WARN: Removed duplicated region for block: B:266:0x0757  */
    /* JADX WARN: Removed duplicated region for block: B:274:0x076a A[Catch: Exception -> 0x0718, TRY_LEAVE, TryCatch #28 {Exception -> 0x0718, blocks: (B:278:0x06ff, B:262:0x073a, B:269:0x075e, B:274:0x076a), top: B:277:0x06ff }] */
    /* JADX WARN: Removed duplicated region for block: B:276:0x0733  */
    /* JADX WARN: Removed duplicated region for block: B:277:0x06ff A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:281:0x06fc  */
    /* JADX WARN: Removed duplicated region for block: B:282:0x06de  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x07be A[Catch: Exception -> 0x0e51, TRY_LEAVE, TryCatch #35 {Exception -> 0x0e51, blocks: (B:27:0x07b8, B:29:0x07be), top: B:26:0x07b8 }] */
    /* JADX WARN: Removed duplicated region for block: B:308:0x066a  */
    /* JADX WARN: Removed duplicated region for block: B:312:0x05d3  */
    /* JADX WARN: Removed duplicated region for block: B:314:0x05af A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:326:0x02e3  */
    /* JADX WARN: Removed duplicated region for block: B:341:0x0431  */
    /* JADX WARN: Removed duplicated region for block: B:347:0x0490 A[Catch: Exception -> 0x0403, TRY_ENTER, TryCatch #42 {Exception -> 0x0403, blocks: (B:400:0x03e9, B:402:0x03ef, B:407:0x03fe, B:382:0x0450, B:385:0x0457, B:387:0x045b, B:347:0x0490, B:349:0x0496, B:391:0x0446, B:381:0x0436), top: B:399:0x03e9, inners: #25 }] */
    /* JADX WARN: Removed duplicated region for block: B:369:0x04b0 A[Catch: Exception -> 0x0e93, TryCatch #32 {Exception -> 0x0e93, blocks: (B:339:0x042b, B:342:0x0432, B:345:0x0460, B:351:0x04bc, B:367:0x04a1, B:369:0x04b0, B:371:0x04b6, B:396:0x0421, B:338:0x0411), top: B:337:0x0411, inners: #1 }] */
    /* JADX WARN: Removed duplicated region for block: B:380:0x0436 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:399:0x03e9 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:416:0x032e  */
    /* JADX WARN: Removed duplicated region for block: B:57:0x08ca A[Catch: Exception -> 0x0d2f, TRY_LEAVE, TryCatch #3 {Exception -> 0x0d2f, blocks: (B:55:0x08c4, B:57:0x08ca, B:71:0x093a, B:59:0x08f6), top: B:54:0x08c4 }] */
    /* JADX WARN: Removed duplicated region for block: B:91:0x0a43 A[Catch: Exception -> 0x0c9a, TryCatch #16 {Exception -> 0x0c9a, blocks: (B:89:0x0a3d, B:91:0x0a43, B:95:0x0a55), top: B:88:0x0a3d }] */
    /* JADX WARN: Removed duplicated region for block: B:97:0x0c74  */
    /* JADX WARN: Removed duplicated region for block: B:9:0x003b  */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 6 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:106:0x0e19 -> B:26:0x07b8). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:183:0x0b6d -> B:15:0x0b8c). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:195:0x0bfc -> B:18:0x0c0f). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:96:0x0a5d -> B:19:0x0a86). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static /* synthetic */ Object getUrl$suspendImpl(Filepress filepress, String str, String str2, Function1<? super SubtitleFile, Unit> function1, Function1<? super ExtractorLink, Unit> function12, Continuation<? super Unit> continuation) {
        Filepress$getUrl$1 filepress$getUrl$1;
        Filepress$getUrl$1 filepress$getUrl$12;
        Filepress filepress2;
        Continuation<? super Unit> continuation2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        Iterator it;
        Map map;
        String str10;
        Function1<? super ExtractorLink, Unit> function13;
        Function1<? super ExtractorLink, Unit> function14;
        Function1<? super SubtitleFile, Unit> function15;
        String str11;
        String str12;
        Exception exc;
        Continuation<? super Unit> continuation3;
        String str13;
        String str14;
        Function1<? super SubtitleFile, Unit> function16;
        NiceResponse niceResponse;
        String str15;
        URI uri;
        Ref.ObjectRef objectRef;
        Ref.ObjectRef objectRef2;
        List list;
        int i;
        Continuation<? super Unit> continuation4;
        Continuation<? super Unit> continuation5;
        String str16;
        String str17;
        String str18;
        String str19;
        String str20;
        String str21;
        Map map2;
        Function1<? super ExtractorLink, Unit> function17;
        String str22;
        String str23;
        String str24;
        String str25;
        String str26;
        String str27;
        Iterator it2;
        String str28;
        int i2;
        JSONObject jSONObject;
        Object obj;
        String str29;
        String str30;
        JSONObject jSONObject2;
        JSONArray optJSONArray;
        Continuation<? super Unit> continuation6;
        JSONObject jSONObject3;
        Object obj2;
        int i3;
        Filepress filepress3;
        Function1<? super ExtractorLink, Unit> function18;
        JSONObject jSONObject4;
        String str31;
        Object obj3;
        int i4;
        String str32;
        int length;
        String str33;
        Filepress$getUrl$1 filepress$getUrl$13;
        String str34;
        String str35;
        Iterator it3;
        int i5;
        Map map3;
        String str36;
        Continuation<? super Unit> continuation7;
        String str37;
        Function1<? super ExtractorLink, Unit> function19;
        Object obj4;
        Object obj5;
        String str38;
        String str39;
        Object obj6;
        JSONObject jSONObject5;
        String str40;
        JSONObject jSONObject6;
        Filepress$getUrl$1 filepress$getUrl$14;
        String str41;
        int i6;
        int i7;
        int i8;
        int i9;
        Object obj7;
        String str42;
        String str43;
        Iterator it4;
        String str44;
        Function1<? super ExtractorLink, Unit> function110;
        String str45;
        Map map4;
        String str46;
        String str47;
        Function1<? super ExtractorLink, Unit> function111;
        Filepress filepress4;
        String str48;
        Object obj8;
        String str49;
        String str50;
        String str51;
        Object obj9;
        NiceResponse niceResponse2;
        Filepress filepress5;
        String str52;
        String str53;
        Function1<? super SubtitleFile, Unit> function112;
        Function1<? super ExtractorLink, Unit> function113;
        Object obj10;
        NiceResponse niceResponse3;
        Function1<? super ExtractorLink, Unit> function114;
        NiceResponse niceResponse4;
        String str54;
        Object obj11;
        URI uri2;
        Object obj12;
        URI uri3;
        MatchResult find$default;
        int i10;
        MatchResult find$default2;
        String str55;
        String str56;
        List groupValues;
        String str57;
        Object obj13;
        Filepress$getUrl$1 filepress$getUrl$15;
        Map map5;
        Filepress filepress6;
        Ref.ObjectRef objectRef3;
        String str58;
        Ref.ObjectRef objectRef4;
        String str59;
        String str60;
        Function1<? super ExtractorLink, Unit> function115;
        Requests app;
        String str61;
        String str62;
        Function1<? super ExtractorLink, Unit> function116;
        String str63;
        String str64;
        String str65;
        Ref.ObjectRef objectRef5;
        List groupValues2;
        Ref.ObjectRef objectRef6;
        Function1<? super ExtractorLink, Unit> function117;
        String str66;
        CharSequence charSequence;
        int i11;
        boolean z;
        JSONObject optJSONObject;
        String optString;
        long optLong;
        Ref.ObjectRef objectRef7;
        double d;
        String format;
        Continuation<? super Unit> continuation8 = continuation;
        if (continuation8 instanceof Filepress$getUrl$1) {
            filepress$getUrl$1 = (Filepress$getUrl$1) continuation8;
            if ((filepress$getUrl$1.label & Integer.MIN_VALUE) != 0) {
                filepress$getUrl$1.label -= Integer.MIN_VALUE;
                filepress$getUrl$12 = filepress$getUrl$1;
                Object obj14 = filepress$getUrl$12.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (filepress$getUrl$12.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj14);
                        try {
                            Requests app2 = MainActivityKt.getApp();
                            filepress$getUrl$12.L$0 = filepress;
                            filepress$getUrl$12.L$1 = str;
                            filepress$getUrl$12.L$2 = str2;
                            filepress$getUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(function1);
                            filepress$getUrl$12.L$4 = function12;
                            filepress$getUrl$12.label = 1;
                            str17 = "method";
                            str4 = "status";
                            str49 = "application/json";
                            str18 = "id";
                            str50 = "";
                            str51 = "data";
                            str16 = "FilePress";
                            obj9 = coroutine_suspended;
                            niceResponse2 = null;
                            obj8 = obj14;
                            try {
                                obj10 = Requests.get$default(app2, str, (Map) null, (String) null, (Map) null, (Map) null, true, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, filepress$getUrl$12, 4062, (Object) null);
                                filepress$getUrl$12 = filepress$getUrl$12;
                            } catch (Exception e) {
                                filepress$getUrl$12 = filepress$getUrl$12;
                                filepress5 = filepress;
                                str52 = str;
                                str53 = str2;
                                function112 = function1;
                                function113 = function12;
                                function15 = function112;
                                niceResponse3 = niceResponse2;
                                str11 = str52;
                                filepress2 = filepress5;
                                function114 = function113;
                                str12 = str53;
                                niceResponse4 = niceResponse3;
                                if (niceResponse4 != null) {
                                }
                                str54 = str11;
                                String str67 = str54;
                                try {
                                    try {
                                        Result.Companion companion = Result.Companion;
                                        obj11 = Result.constructor-impl(new URI(str67));
                                    } catch (Throwable th) {
                                        Result.Companion companion2 = Result.Companion;
                                        obj11 = Result.constructor-impl(ResultKt.createFailure(th));
                                    }
                                    if (Result.isFailure-impl(obj11)) {
                                    }
                                    uri2 = (URI) obj11;
                                    if (uri2 == null) {
                                    }
                                    uri3 = uri2;
                                    String str68 = uri3.getScheme() + "://" + uri3.getHost();
                                    find$default = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str67, 0, 2, niceResponse2);
                                    if (find$default == null) {
                                    }
                                    i10 = 1;
                                    find$default2 = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str11, 0, 2, niceResponse2);
                                    if (find$default2 != null) {
                                    }
                                    function14 = function114;
                                    str55 = str12;
                                    str56 = str11;
                                    str8 = str16;
                                } catch (Exception e2) {
                                    function14 = function114;
                                    str8 = str16;
                                    exc = e2;
                                    continuation3 = continuation;
                                    Log.INSTANCE.e(str8, "Failed to extract: " + exc.getMessage());
                                    return Unit.INSTANCE;
                                }
                                try {
                                    return Unit.INSTANCE;
                                } catch (Exception e3) {
                                    exc = e3;
                                    str12 = str55;
                                    str11 = str56;
                                    continuation3 = continuation;
                                    Log.INSTANCE.e(str8, "Failed to extract: " + exc.getMessage());
                                    return Unit.INSTANCE;
                                }
                            }
                        } catch (Exception e4) {
                            obj8 = obj14;
                            str17 = "method";
                            str4 = "status";
                            str49 = "application/json";
                            str18 = "id";
                            str50 = "";
                            str51 = "data";
                            str16 = "FilePress";
                            obj9 = coroutine_suspended;
                            niceResponse2 = null;
                            filepress5 = filepress;
                            str52 = str;
                            str53 = str2;
                            function112 = function1;
                            function113 = function12;
                        }
                        if (obj10 == obj9) {
                            return obj9;
                        }
                        filepress5 = filepress;
                        str52 = str;
                        str53 = str2;
                        function112 = function1;
                        function113 = function12;
                        try {
                            function15 = function112;
                            niceResponse3 = (NiceResponse) obj10;
                            str11 = str52;
                            filepress2 = filepress5;
                            function114 = function113;
                            str12 = str53;
                        } catch (Exception e5) {
                            function15 = function112;
                            niceResponse3 = niceResponse2;
                            str11 = str52;
                            filepress2 = filepress5;
                            function114 = function113;
                            str12 = str53;
                            niceResponse4 = niceResponse3;
                            if (niceResponse4 != null) {
                            }
                            str54 = str11;
                            String str672 = str54;
                            Result.Companion companion3 = Result.Companion;
                            obj11 = Result.constructor-impl(new URI(str672));
                            if (Result.isFailure-impl(obj11)) {
                            }
                            uri2 = (URI) obj11;
                            if (uri2 == null) {
                            }
                            uri3 = uri2;
                            String str682 = uri3.getScheme() + "://" + uri3.getHost();
                            find$default = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str672, 0, 2, niceResponse2);
                            if (find$default == null) {
                            }
                            i10 = 1;
                            find$default2 = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str11, 0, 2, niceResponse2);
                            if (find$default2 != null) {
                            }
                            function14 = function114;
                            str55 = str12;
                            str56 = str11;
                            str8 = str16;
                            return Unit.INSTANCE;
                        }
                        niceResponse4 = niceResponse3;
                        if (niceResponse4 != null) {
                            try {
                                String url = niceResponse4.getUrl();
                                if (url != null) {
                                    String str69 = url;
                                    if (str69.length() == 0) {
                                        str69 = str11;
                                    }
                                    str54 = str69;
                                    break;
                                }
                            } catch (Exception e6) {
                                exc = e6;
                                function14 = function114;
                                str8 = str16;
                                continuation3 = continuation;
                                Log.INSTANCE.e(str8, "Failed to extract: " + exc.getMessage());
                                return Unit.INSTANCE;
                            }
                        }
                        str54 = str11;
                        String str6722 = str54;
                        Result.Companion companion32 = Result.Companion;
                        obj11 = Result.constructor-impl(new URI(str6722));
                        if (Result.isFailure-impl(obj11)) {
                            obj11 = niceResponse2;
                        }
                        uri2 = (URI) obj11;
                        if (uri2 == null) {
                            try {
                                Result.Companion companion4 = Result.Companion;
                                obj12 = Result.constructor-impl(new URI(str11));
                            } catch (Throwable th2) {
                                Result.Companion companion5 = Result.Companion;
                                obj12 = Result.constructor-impl(ResultKt.createFailure(th2));
                            }
                            if (Result.isFailure-impl(obj12)) {
                                obj12 = niceResponse2;
                            }
                            uri2 = (URI) obj12;
                            if (uri2 == null) {
                                return Unit.INSTANCE;
                            }
                        }
                        uri3 = uri2;
                        String str6822 = uri3.getScheme() + "://" + uri3.getHost();
                        find$default = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str6722, 0, 2, niceResponse2);
                        if (find$default == null && (groupValues2 = find$default.getGroupValues()) != null) {
                            i10 = 1;
                            str57 = (String) groupValues2.get(1);
                            break;
                        } else {
                            i10 = 1;
                        }
                        find$default2 = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str11, 0, 2, niceResponse2);
                        if (find$default2 != null || (groupValues = find$default2.getGroupValues()) == null) {
                            function14 = function114;
                            str55 = str12;
                            str56 = str11;
                            str8 = str16;
                            return Unit.INSTANCE;
                        }
                        str57 = (String) groupValues.get(i10);
                        String str70 = str57;
                        Pair[] pairArr = new Pair[3];
                        String str71 = str49;
                        pairArr[0] = TuplesKt.to("Content-Type", str71);
                        pairArr[i10] = TuplesKt.to("Referer", str6722);
                        pairArr[2] = TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                        Map mapOf = MapsKt.mapOf(pairArr);
                        Ref.ObjectRef objectRef8 = new Ref.ObjectRef();
                        String str72 = str50;
                        objectRef8.element = str72;
                        Ref.ObjectRef objectRef9 = new Ref.ObjectRef();
                        objectRef9.element = str72;
                        try {
                            app = MainActivityKt.getApp();
                            str61 = str6822 + "/api/file/get/" + str70;
                            filepress$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(filepress2);
                            filepress$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str11);
                            filepress$getUrl$12.L$2 = str12;
                            filepress$getUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(function15);
                            filepress$getUrl$12.L$4 = function114;
                            filepress$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                            filepress$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str6722);
                            filepress$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri3);
                            filepress$getUrl$12.L$8 = str6822;
                            filepress$getUrl$12.L$9 = str70;
                            filepress$getUrl$12.L$10 = mapOf;
                            filepress$getUrl$12.L$11 = objectRef8;
                            filepress$getUrl$12.L$12 = objectRef9;
                            filepress$getUrl$12.label = 2;
                            str62 = str11;
                            function116 = function114;
                            str63 = str12;
                            str64 = str6822;
                            str65 = str72;
                            filepress$getUrl$15 = filepress$getUrl$12;
                            str19 = str71;
                        } catch (Exception e7) {
                            obj13 = obj9;
                            filepress$getUrl$15 = filepress$getUrl$12;
                            String str73 = str12;
                            str19 = str71;
                            str20 = str51;
                            str21 = str70;
                            map5 = mapOf;
                            filepress6 = filepress2;
                            objectRef3 = objectRef8;
                            str58 = str6822;
                            objectRef4 = objectRef9;
                            str59 = str73;
                            str60 = str6722;
                            function115 = function114;
                        }
                        try {
                            obj14 = Requests.get$default(app, str61, mapOf, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, filepress$getUrl$15, 4092, (Object) null);
                        } catch (Exception e8) {
                            obj13 = obj9;
                            str20 = str51;
                            str21 = str70;
                            map5 = mapOf;
                            filepress6 = filepress2;
                            str11 = str62;
                            objectRef3 = objectRef8;
                            str58 = str64;
                            objectRef4 = objectRef9;
                            str59 = str63;
                            str60 = str6722;
                            function115 = function116;
                            str64 = str58;
                            filepress2 = filepress6;
                            objectRef6 = objectRef4;
                            function117 = function115;
                            str66 = str59;
                            try {
                                charSequence = (CharSequence) objectRef3.element;
                                if (charSequence.length() == 0) {
                                }
                                int indexQuality = ExtractorsKt.getIndexQuality((String) charSequence);
                                StringBuilder sb = new StringBuilder();
                                if (((CharSequence) objectRef3.element).length() > 0) {
                                }
                                if (((CharSequence) objectRef6.element).length() > 0) {
                                }
                                String sb2 = sb.toString();
                                List listOf = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                                it = listOf.iterator();
                                str14 = str66;
                                list = listOf;
                                objectRef2 = objectRef6;
                                function13 = function117;
                                objectRef = objectRef3;
                                map = map5;
                                str13 = str11;
                                filepress$getUrl$12 = filepress$getUrl$15;
                                Object obj15 = obj13;
                                obj14 = obj8;
                                uri = uri3;
                                str15 = str60;
                                niceResponse = niceResponse4;
                                function16 = function15;
                                String str74 = str64;
                                i = i11;
                                continuation5 = continuation;
                                String str75 = sb2;
                            } catch (Exception e9) {
                                str8 = str16;
                                str12 = str66;
                                function14 = function117;
                                exc = e9;
                                continuation3 = continuation;
                            }
                            try {
                                if (it.hasNext()) {
                                }
                            } catch (Exception e10) {
                                Continuation<? super Unit> continuation9 = continuation5;
                                str8 = str16;
                                exc = e10;
                                function14 = function13;
                                function15 = function16;
                                str12 = str14;
                                str11 = str13;
                                continuation3 = continuation9;
                                Log.INSTANCE.e(str8, "Failed to extract: " + exc.getMessage());
                                return Unit.INSTANCE;
                            }
                        }
                        if (obj14 == obj9) {
                            return obj9;
                        }
                        str21 = str70;
                        map5 = mapOf;
                        str11 = str62;
                        objectRef3 = objectRef8;
                        objectRef5 = objectRef9;
                        str59 = str63;
                        str60 = str6722;
                        function115 = function116;
                        try {
                            str20 = str51;
                            try {
                                optJSONObject = new JSONObject(((NiceResponse) obj14).getText()).optJSONObject(str20);
                                if (optJSONObject == null) {
                                    try {
                                        optString = optJSONObject.optString("name");
                                    } catch (Exception e11) {
                                        obj13 = obj9;
                                        objectRef4 = objectRef5;
                                        filepress6 = filepress2;
                                        str58 = str64;
                                        str64 = str58;
                                        filepress2 = filepress6;
                                        objectRef6 = objectRef4;
                                        function117 = function115;
                                        str66 = str59;
                                        charSequence = (CharSequence) objectRef3.element;
                                        if (charSequence.length() == 0) {
                                        }
                                        int indexQuality2 = ExtractorsKt.getIndexQuality((String) charSequence);
                                        StringBuilder sb3 = new StringBuilder();
                                        if (((CharSequence) objectRef3.element).length() > 0) {
                                        }
                                        if (((CharSequence) objectRef6.element).length() > 0) {
                                        }
                                        String sb22 = sb3.toString();
                                        List listOf2 = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                                        it = listOf2.iterator();
                                        str14 = str66;
                                        list = listOf2;
                                        objectRef2 = objectRef6;
                                        function13 = function117;
                                        objectRef = objectRef3;
                                        map = map5;
                                        str13 = str11;
                                        filepress$getUrl$12 = filepress$getUrl$15;
                                        Object obj152 = obj13;
                                        obj14 = obj8;
                                        uri = uri3;
                                        str15 = str60;
                                        niceResponse = niceResponse4;
                                        function16 = function15;
                                        String str742 = str64;
                                        i = i11;
                                        continuation5 = continuation;
                                        String str752 = sb22;
                                        if (it.hasNext()) {
                                        }
                                    }
                                } else {
                                    optString = null;
                                }
                                if (optString == null) {
                                    optString = str65;
                                }
                                objectRef3.element = optString;
                                optLong = optJSONObject == null ? optJSONObject.optLong("size") : 0L;
                                if (optLong <= 0) {
                                    if (optLong < 1073741824) {
                                        objectRef7 = objectRef5;
                                        obj13 = obj9;
                                        try {
                                            try {
                                                format = String.format("%.2f MB", Arrays.copyOf(new Object[]{Boxing.boxDouble((optLong / 1024.0d) / 1024)}, 1));
                                                Intrinsics.checkNotNullExpressionValue(format, "format(...)");
                                            } catch (Exception e12) {
                                                objectRef4 = objectRef7;
                                                filepress6 = filepress2;
                                                str58 = str64;
                                                str64 = str58;
                                                filepress2 = filepress6;
                                                objectRef6 = objectRef4;
                                                function117 = function115;
                                                str66 = str59;
                                                charSequence = (CharSequence) objectRef3.element;
                                                if (charSequence.length() == 0) {
                                                }
                                                int indexQuality22 = ExtractorsKt.getIndexQuality((String) charSequence);
                                                StringBuilder sb32 = new StringBuilder();
                                                if (((CharSequence) objectRef3.element).length() > 0) {
                                                }
                                                if (((CharSequence) objectRef6.element).length() > 0) {
                                                }
                                                String sb222 = sb32.toString();
                                                List listOf22 = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                                                it = listOf22.iterator();
                                                str14 = str66;
                                                list = listOf22;
                                                objectRef2 = objectRef6;
                                                function13 = function117;
                                                objectRef = objectRef3;
                                                map = map5;
                                                str13 = str11;
                                                filepress$getUrl$12 = filepress$getUrl$15;
                                                Object obj1522 = obj13;
                                                obj14 = obj8;
                                                uri = uri3;
                                                str15 = str60;
                                                niceResponse = niceResponse4;
                                                function16 = function15;
                                                String str7422 = str64;
                                                i = i11;
                                                continuation5 = continuation;
                                                String str7522 = sb222;
                                                if (it.hasNext()) {
                                                }
                                            }
                                        } catch (Exception e13) {
                                            objectRef4 = objectRef7;
                                            filepress6 = filepress2;
                                            str58 = str64;
                                        }
                                    } else {
                                        obj13 = obj9;
                                        objectRef7 = objectRef5;
                                        try {
                                            d = 1024;
                                        } catch (Exception e14) {
                                            objectRef4 = objectRef7;
                                            filepress6 = filepress2;
                                            str58 = str64;
                                        }
                                        try {
                                            format = String.format("%.2f GB", Arrays.copyOf(new Object[]{Boxing.boxDouble(((optLong / 1024.0d) / d) / d)}, 1));
                                            Intrinsics.checkNotNullExpressionValue(format, "format(...)");
                                        } catch (Exception e15) {
                                            objectRef4 = objectRef7;
                                            filepress6 = filepress2;
                                            str58 = str64;
                                            str64 = str58;
                                            filepress2 = filepress6;
                                            objectRef6 = objectRef4;
                                            function117 = function115;
                                            str66 = str59;
                                            charSequence = (CharSequence) objectRef3.element;
                                            if (charSequence.length() == 0) {
                                            }
                                            int indexQuality222 = ExtractorsKt.getIndexQuality((String) charSequence);
                                            StringBuilder sb322 = new StringBuilder();
                                            if (((CharSequence) objectRef3.element).length() > 0) {
                                            }
                                            if (((CharSequence) objectRef6.element).length() > 0) {
                                            }
                                            String sb2222 = sb322.toString();
                                            List listOf222 = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                                            it = listOf222.iterator();
                                            str14 = str66;
                                            list = listOf222;
                                            objectRef2 = objectRef6;
                                            function13 = function117;
                                            objectRef = objectRef3;
                                            map = map5;
                                            str13 = str11;
                                            filepress$getUrl$12 = filepress$getUrl$15;
                                            Object obj15222 = obj13;
                                            obj14 = obj8;
                                            uri = uri3;
                                            str15 = str60;
                                            niceResponse = niceResponse4;
                                            function16 = function15;
                                            String str74222 = str64;
                                            i = i11;
                                            continuation5 = continuation;
                                            String str75222 = sb2222;
                                            if (it.hasNext()) {
                                            }
                                        }
                                    }
                                    objectRef4 = objectRef7;
                                    try {
                                        objectRef4.element = format;
                                    } catch (Exception e16) {
                                        filepress6 = filepress2;
                                        str58 = str64;
                                        str64 = str58;
                                        filepress2 = filepress6;
                                        objectRef6 = objectRef4;
                                        function117 = function115;
                                        str66 = str59;
                                        charSequence = (CharSequence) objectRef3.element;
                                        if (charSequence.length() == 0) {
                                        }
                                        int indexQuality2222 = ExtractorsKt.getIndexQuality((String) charSequence);
                                        StringBuilder sb3222 = new StringBuilder();
                                        if (((CharSequence) objectRef3.element).length() > 0) {
                                        }
                                        if (((CharSequence) objectRef6.element).length() > 0) {
                                        }
                                        String sb22222 = sb3222.toString();
                                        List listOf2222 = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                                        it = listOf2222.iterator();
                                        str14 = str66;
                                        list = listOf2222;
                                        objectRef2 = objectRef6;
                                        function13 = function117;
                                        objectRef = objectRef3;
                                        map = map5;
                                        str13 = str11;
                                        filepress$getUrl$12 = filepress$getUrl$15;
                                        Object obj152222 = obj13;
                                        obj14 = obj8;
                                        uri = uri3;
                                        str15 = str60;
                                        niceResponse = niceResponse4;
                                        function16 = function15;
                                        String str742222 = str64;
                                        i = i11;
                                        continuation5 = continuation;
                                        String str752222 = sb22222;
                                        if (it.hasNext()) {
                                        }
                                    }
                                } else {
                                    obj13 = obj9;
                                    objectRef4 = objectRef5;
                                }
                                objectRef6 = objectRef4;
                                function117 = function115;
                                str66 = str59;
                            } catch (Exception e17) {
                                obj13 = obj9;
                                objectRef4 = objectRef5;
                                filepress6 = filepress2;
                                str58 = str64;
                                str64 = str58;
                                filepress2 = filepress6;
                                objectRef6 = objectRef4;
                                function117 = function115;
                                str66 = str59;
                                charSequence = (CharSequence) objectRef3.element;
                                if (charSequence.length() == 0) {
                                }
                                int indexQuality22222 = ExtractorsKt.getIndexQuality((String) charSequence);
                                StringBuilder sb32222 = new StringBuilder();
                                if (((CharSequence) objectRef3.element).length() > 0) {
                                }
                                if (((CharSequence) objectRef6.element).length() > 0) {
                                }
                                String sb222222 = sb32222.toString();
                                List listOf22222 = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                                it = listOf22222.iterator();
                                str14 = str66;
                                list = listOf22222;
                                objectRef2 = objectRef6;
                                function13 = function117;
                                objectRef = objectRef3;
                                map = map5;
                                str13 = str11;
                                filepress$getUrl$12 = filepress$getUrl$15;
                                Object obj1522222 = obj13;
                                obj14 = obj8;
                                uri = uri3;
                                str15 = str60;
                                niceResponse = niceResponse4;
                                function16 = function15;
                                String str7422222 = str64;
                                i = i11;
                                continuation5 = continuation;
                                String str7522222 = sb222222;
                                if (it.hasNext()) {
                                }
                            }
                        } catch (Exception e18) {
                            obj13 = obj9;
                            objectRef4 = objectRef5;
                            str20 = str51;
                        }
                        charSequence = (CharSequence) objectRef3.element;
                        if (charSequence.length() == 0) {
                            charSequence = str66;
                        }
                        int indexQuality222222 = ExtractorsKt.getIndexQuality((String) charSequence);
                        StringBuilder sb322222 = new StringBuilder();
                        if (((CharSequence) objectRef3.element).length() > 0) {
                            try {
                                sb322222.append(" " + objectRef3.element);
                            } catch (Exception e19) {
                                str12 = str66;
                                function14 = function117;
                                str8 = str16;
                                exc = e19;
                                continuation3 = continuation;
                                Log.INSTANCE.e(str8, "Failed to extract: " + exc.getMessage());
                                return Unit.INSTANCE;
                            }
                        }
                        if (((CharSequence) objectRef6.element).length() > 0) {
                            i11 = indexQuality222222;
                            sb322222.append(" [" + objectRef6.element + "]");
                        } else {
                            i11 = indexQuality222222;
                            String str76 = str66;
                            if (str76 != null && str76.length() != 0) {
                                z = false;
                                if (!z) {
                                    sb322222.append(" [" + str66 + "]");
                                }
                            }
                            z = true;
                            if (!z) {
                            }
                        }
                        String sb2222222 = sb322222.toString();
                        List listOf222222 = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                        it = listOf222222.iterator();
                        str14 = str66;
                        list = listOf222222;
                        objectRef2 = objectRef6;
                        function13 = function117;
                        objectRef = objectRef3;
                        map = map5;
                        str13 = str11;
                        filepress$getUrl$12 = filepress$getUrl$15;
                        Object obj15222222 = obj13;
                        obj14 = obj8;
                        uri = uri3;
                        str15 = str60;
                        niceResponse = niceResponse4;
                        function16 = function15;
                        String str74222222 = str64;
                        i = i11;
                        continuation5 = continuation;
                        String str75222222 = sb2222222;
                        if (it.hasNext()) {
                            String str77 = (String) it.next();
                            JSONObject jSONObject7 = new JSONObject();
                            continuation7 = continuation5;
                            str39 = str18;
                            jSONObject7.put(str39, str21);
                            Object obj16 = obj14;
                            String str78 = str17;
                            jSONObject7.put(str78, str77);
                            String jSONObject8 = jSONObject7.toString();
                            Requests app3 = MainActivityKt.getApp();
                            str3 = str78;
                            String str79 = str74222222 + "/api/file/downlaod/";
                            RequestBody create = RequestBody.Companion.create(jSONObject8, MediaType.Companion.get(str19));
                            filepress$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(filepress2);
                            filepress$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str13);
                            filepress$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str14);
                            filepress$getUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(function16);
                            filepress$getUrl$12.L$4 = function13;
                            filepress$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(niceResponse);
                            filepress$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str15);
                            filepress$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri);
                            filepress$getUrl$12.L$8 = str74222222;
                            filepress$getUrl$12.L$9 = str21;
                            filepress$getUrl$12.L$10 = map;
                            filepress$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(objectRef);
                            filepress$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(objectRef2);
                            filepress$getUrl$12.L$13 = str75222222;
                            filepress$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(list);
                            filepress$getUrl$12.L$15 = it;
                            filepress$getUrl$12.L$16 = str77;
                            filepress$getUrl$12.L$17 = SpillingKt.nullOutSpilledVariable(jSONObject8);
                            filepress$getUrl$12.L$18 = null;
                            filepress$getUrl$12.L$19 = null;
                            filepress$getUrl$12.L$20 = null;
                            filepress$getUrl$12.L$21 = null;
                            filepress$getUrl$12.L$22 = null;
                            filepress$getUrl$12.L$23 = null;
                            filepress$getUrl$12.L$24 = null;
                            filepress$getUrl$12.L$25 = null;
                            filepress$getUrl$12.L$26 = null;
                            filepress$getUrl$12.I$0 = i;
                            filepress$getUrl$12.label = 3;
                            Map map6 = map;
                            Filepress$getUrl$1 filepress$getUrl$16 = filepress$getUrl$12;
                            Object post$default = Requests.post$default(app3, str79, map6, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, create, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, filepress$getUrl$16, 65276, (Object) null);
                            if (post$default == obj15222222) {
                                return obj15222222;
                            }
                            str38 = str77;
                            str10 = str21;
                            filepress$getUrl$12 = filepress$getUrl$16;
                            str37 = jSONObject8;
                            obj6 = post$default;
                            function19 = function13;
                            map = map6;
                            str27 = str74222222;
                            it2 = it;
                            str28 = str75222222;
                            i2 = i;
                            obj4 = obj15222222;
                            obj5 = obj16;
                            try {
                            } catch (Exception e20) {
                                e = e20;
                                str6 = str39;
                                str5 = str19;
                                Object obj17 = obj5;
                                obj15222222 = obj4;
                                str7 = str20;
                                str8 = str16;
                                continuation2 = continuation7;
                                str9 = str38;
                                i = i2;
                                str75222222 = str28;
                                it = it2;
                                str74222222 = str27;
                                function13 = function19;
                                obj14 = obj17;
                            }
                            String text = ((NiceResponse) obj6).getText();
                            Object obj18 = obj5;
                            jSONObject5 = new JSONObject(text);
                            Object obj19 = obj4;
                            String str80 = str37;
                            str24 = str4;
                            if (jSONObject5.optBoolean(str24, false)) {
                                String str81 = str39;
                                str4 = str24;
                                obj15222222 = obj19;
                                continuation5 = continuation7;
                                obj14 = obj18;
                                i = i2;
                                str75222222 = str28;
                                it = it2;
                                str74222222 = str27;
                                str16 = str16;
                                str17 = str3;
                                str18 = str81;
                                function13 = function19;
                                str19 = str19;
                                str20 = str20;
                                map = map;
                                filepress$getUrl$12 = filepress$getUrl$12;
                                str21 = str10;
                            } else {
                                Object opt = jSONObject5.opt(str20);
                                if (opt instanceof String) {
                                    try {
                                    } catch (Exception e21) {
                                        try {
                                            e = e21;
                                            str6 = str39;
                                            str5 = str19;
                                            i = i2;
                                            str7 = str20;
                                            str75222222 = str28;
                                            it = it2;
                                            str74222222 = str27;
                                            function13 = function19;
                                            str4 = str24;
                                            obj15222222 = obj19;
                                            str8 = str16;
                                            continuation2 = continuation7;
                                            str9 = str38;
                                            obj14 = obj18;
                                            Log.INSTANCE.e(str8, "Error in method " + str9 + ": " + e.getMessage());
                                            continuation5 = continuation4;
                                            str16 = str8;
                                            str17 = str3;
                                            str18 = str6;
                                            str19 = str5;
                                            str20 = str7;
                                            str21 = str10;
                                            if (it.hasNext()) {
                                            }
                                        } catch (Exception e22) {
                                            exc = e22;
                                            function14 = function13;
                                            function15 = function16;
                                            str12 = str14;
                                            str11 = str13;
                                            continuation3 = continuation4;
                                            Log.INSTANCE.e(str8, "Failed to extract: " + exc.getMessage());
                                            return Unit.INSTANCE;
                                        }
                                        continuation4 = continuation2;
                                    }
                                    str40 = (String) opt;
                                    jSONObject6 = jSONObject5;
                                } else if (opt instanceof JSONObject) {
                                    jSONObject6 = jSONObject5;
                                    String optString2 = ((JSONObject) opt).optString("downloadId");
                                    if (optString2.length() == 0) {
                                        optString2 = ((JSONObject) opt).optString(str39);
                                    }
                                    str40 = optString2;
                                } else {
                                    jSONObject6 = jSONObject5;
                                    str40 = null;
                                }
                                if (str40 == null) {
                                    String str82 = str39;
                                    str4 = str24;
                                    obj15222222 = obj19;
                                    continuation5 = continuation7;
                                    obj14 = obj18;
                                    i = i2;
                                    str75222222 = str28;
                                    it = it2;
                                    str74222222 = str27;
                                    str16 = str16;
                                    str17 = str3;
                                    str18 = str82;
                                    function13 = function19;
                                    str19 = str19;
                                    str20 = str20;
                                    map = map;
                                    filepress$getUrl$12 = filepress$getUrl$12;
                                    str21 = str10;
                                } else {
                                    JSONObject jSONObject9 = new JSONObject();
                                    jSONObject9.put(str39, str40);
                                    str6 = str39;
                                    String str83 = str3;
                                    jSONObject9.put(str83, str38);
                                    str30 = jSONObject9.toString();
                                    Requests app4 = MainActivityKt.getApp();
                                    str3 = str83;
                                    String str84 = str27 + "/api/file/downlaod2/";
                                    RequestBody create2 = RequestBody.Companion.create(str30, MediaType.Companion.get(str19));
                                    filepress$getUrl$12.L$0 = SpillingKt.nullOutSpilledVariable(filepress2);
                                    filepress$getUrl$12.L$1 = SpillingKt.nullOutSpilledVariable(str13);
                                    filepress$getUrl$12.L$2 = SpillingKt.nullOutSpilledVariable(str14);
                                    filepress$getUrl$12.L$3 = SpillingKt.nullOutSpilledVariable(function16);
                                    filepress$getUrl$12.L$4 = function19;
                                    filepress$getUrl$12.L$5 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                    filepress$getUrl$12.L$6 = SpillingKt.nullOutSpilledVariable(str15);
                                    filepress$getUrl$12.L$7 = SpillingKt.nullOutSpilledVariable(uri);
                                    filepress$getUrl$12.L$8 = str27;
                                    filepress$getUrl$12.L$9 = str10;
                                    filepress$getUrl$12.L$10 = map;
                                    filepress$getUrl$12.L$11 = SpillingKt.nullOutSpilledVariable(objectRef);
                                    filepress$getUrl$12.L$12 = SpillingKt.nullOutSpilledVariable(objectRef2);
                                    filepress$getUrl$12.L$13 = str28;
                                    filepress$getUrl$12.L$14 = SpillingKt.nullOutSpilledVariable(list);
                                    filepress$getUrl$12.L$15 = it2;
                                    filepress$getUrl$12.L$16 = str38;
                                    filepress$getUrl$12.L$17 = SpillingKt.nullOutSpilledVariable(str80);
                                    filepress$getUrl$12.L$18 = SpillingKt.nullOutSpilledVariable(text);
                                    filepress$getUrl$12.L$19 = SpillingKt.nullOutSpilledVariable(jSONObject6);
                                    filepress$getUrl$12.L$20 = SpillingKt.nullOutSpilledVariable(str40);
                                    filepress$getUrl$12.L$21 = SpillingKt.nullOutSpilledVariable(str30);
                                    filepress$getUrl$12.I$0 = i2;
                                    filepress$getUrl$12.label = 4;
                                    map2 = map;
                                    Filepress$getUrl$1 filepress$getUrl$17 = filepress$getUrl$12;
                                    Object post$default2 = Requests.post$default(app4, str84, map2, (String) null, (Map) null, (Map) null, (Map) null, (List) null, (Object) null, create2, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, filepress$getUrl$17, 65276, (Object) null);
                                    if (post$default2 == obj19) {
                                        return obj19;
                                    }
                                    str23 = str38;
                                    coroutine_suspended = obj19;
                                    str29 = str40;
                                    function17 = function19;
                                    str22 = str10;
                                    filepress$getUrl$12 = filepress$getUrl$17;
                                    obj = obj18;
                                    str25 = str80;
                                    str26 = text;
                                    jSONObject = jSONObject6;
                                    obj14 = post$default2;
                                    continuation2 = continuation7;
                                    try {
                                    } catch (Exception e23) {
                                        e = e23;
                                        str5 = str19;
                                        str7 = str20;
                                        str4 = str24;
                                        str8 = str16;
                                        obj14 = obj;
                                        i = i2;
                                        str75222222 = str28;
                                        it = it2;
                                        str74222222 = str27;
                                        obj15222222 = coroutine_suspended;
                                        str9 = str23;
                                        str10 = str22;
                                        function13 = function17;
                                        map = map2;
                                    }
                                    String text2 = ((NiceResponse) obj14).getText();
                                    String str85 = str30;
                                    jSONObject2 = new JSONObject(text2);
                                    Continuation<? super Unit> continuation10 = continuation2;
                                    if (jSONObject2.optBoolean(str24, false)) {
                                        optJSONArray = jSONObject2.optJSONArray(str20);
                                        if (optJSONArray == null) {
                                            str4 = str24;
                                            continuation5 = continuation10;
                                            obj14 = obj;
                                            i = i2;
                                            str75222222 = str28;
                                            it = it2;
                                            str74222222 = str27;
                                            str16 = str16;
                                            obj15222222 = coroutine_suspended;
                                            str17 = str3;
                                            str18 = str6;
                                            str21 = str22;
                                            function13 = function17;
                                            str19 = str19;
                                            str20 = str20;
                                            map = map2;
                                        } else {
                                            continuation6 = continuation10;
                                            str5 = str19;
                                            jSONObject3 = jSONObject;
                                            str7 = str20;
                                            obj2 = coroutine_suspended;
                                            i3 = 0;
                                            filepress3 = filepress2;
                                            str9 = str23;
                                            function18 = function17;
                                            jSONObject4 = jSONObject2;
                                            str31 = str29;
                                            obj3 = obj;
                                            i4 = i2;
                                            str32 = str27;
                                            length = optJSONArray.length();
                                            str33 = str22;
                                            filepress$getUrl$13 = filepress$getUrl$12;
                                            str34 = text2;
                                            str35 = str28;
                                            it3 = it2;
                                            i5 = 0;
                                            map3 = map2;
                                            str36 = str85;
                                            if (i5 >= length) {
                                                try {
                                                } catch (Exception e24) {
                                                    e = e24;
                                                    Filepress$getUrl$1 filepress$getUrl$18 = filepress$getUrl$13;
                                                    Object obj20 = obj2;
                                                    filepress$getUrl$14 = filepress$getUrl$18;
                                                    str4 = str24;
                                                    break;
                                                }
                                                String optString3 = optJSONArray.optString(i5);
                                                str41 = str26;
                                                Object obj21 = obj2;
                                                int i12 = i5;
                                                String str86 = str25;
                                                str4 = str24;
                                                if (StringsKt.startsWith$default(optString3, "http", false, 2, (Object) null)) {
                                                    i7 = 1;
                                                    try {
                                                    } catch (Exception e25) {
                                                        e = e25;
                                                        filepress$getUrl$14 = filepress$getUrl$13;
                                                        Object obj22 = obj21;
                                                        str8 = str16;
                                                        break;
                                                    }
                                                    str8 = str16;
                                                    String str87 = str8 + str35;
                                                    ExtractorLinkType infer_type = ExtractorApiKt.getINFER_TYPE();
                                                    Filepress$getUrl$3 filepress$getUrl$3 = new Filepress$getUrl$3(i4, str32, null);
                                                    filepress$getUrl$13.L$0 = SpillingKt.nullOutSpilledVariable(filepress3);
                                                    filepress$getUrl$13.L$1 = SpillingKt.nullOutSpilledVariable(str13);
                                                    filepress$getUrl$13.L$2 = SpillingKt.nullOutSpilledVariable(str14);
                                                    filepress$getUrl$13.L$3 = SpillingKt.nullOutSpilledVariable(function16);
                                                    filepress$getUrl$13.L$4 = function18;
                                                    filepress$getUrl$13.L$5 = SpillingKt.nullOutSpilledVariable(niceResponse);
                                                    filepress$getUrl$13.L$6 = SpillingKt.nullOutSpilledVariable(str15);
                                                    filepress$getUrl$13.L$7 = SpillingKt.nullOutSpilledVariable(uri);
                                                    filepress$getUrl$13.L$8 = str32;
                                                    filepress$getUrl$13.L$9 = str33;
                                                    filepress$getUrl$13.L$10 = map3;
                                                    filepress$getUrl$13.L$11 = SpillingKt.nullOutSpilledVariable(objectRef);
                                                    filepress$getUrl$13.L$12 = SpillingKt.nullOutSpilledVariable(objectRef2);
                                                    filepress$getUrl$13.L$13 = str35;
                                                    filepress$getUrl$13.L$14 = SpillingKt.nullOutSpilledVariable(list);
                                                    filepress$getUrl$13.L$15 = it3;
                                                    filepress$getUrl$13.L$16 = str9;
                                                    filepress$getUrl$13.L$17 = SpillingKt.nullOutSpilledVariable(str86);
                                                    filepress$getUrl$13.L$18 = SpillingKt.nullOutSpilledVariable(str41);
                                                    filepress$getUrl$13.L$19 = SpillingKt.nullOutSpilledVariable(jSONObject3);
                                                    filepress$getUrl$13.L$20 = SpillingKt.nullOutSpilledVariable(str31);
                                                    filepress$getUrl$13.L$21 = SpillingKt.nullOutSpilledVariable(str36);
                                                    filepress$getUrl$13.L$22 = SpillingKt.nullOutSpilledVariable(str34);
                                                    filepress$getUrl$13.L$23 = SpillingKt.nullOutSpilledVariable(jSONObject4);
                                                    filepress$getUrl$13.L$24 = optJSONArray;
                                                    filepress$getUrl$13.L$25 = SpillingKt.nullOutSpilledVariable(optString3);
                                                    filepress$getUrl$13.L$26 = function18;
                                                    filepress$getUrl$13.I$0 = i4;
                                                    filepress$getUrl$13.I$1 = 1;
                                                    i8 = i12;
                                                    filepress$getUrl$13.I$2 = i8;
                                                    filepress$getUrl$13.I$3 = length;
                                                    i9 = length;
                                                    filepress$getUrl$13.label = 5;
                                                    Filepress$getUrl$1 filepress$getUrl$19 = filepress$getUrl$13;
                                                    Object newExtractorLink = ExtractorApiKt.newExtractorLink("FilePress", str87, optString3, infer_type, filepress$getUrl$3, filepress$getUrl$19);
                                                    obj7 = obj21;
                                                    if (newExtractorLink == obj7) {
                                                        return obj7;
                                                    }
                                                    Iterator it5 = it3;
                                                    obj14 = newExtractorLink;
                                                    str42 = str86;
                                                    str43 = str9;
                                                    it4 = it5;
                                                    str44 = str32;
                                                    function110 = function18;
                                                    str45 = str33;
                                                    map4 = map3;
                                                    str46 = str36;
                                                    i = i4;
                                                    str47 = str35;
                                                    function111 = function110;
                                                    filepress4 = filepress3;
                                                    filepress$getUrl$12 = filepress$getUrl$19;
                                                    str48 = str31;
                                                    continuation8 = continuation6;
                                                    try {
                                                    } catch (Exception e26) {
                                                        e = e26;
                                                        continuation2 = continuation8;
                                                        filepress2 = filepress4;
                                                        it = it4;
                                                        str75222222 = str47;
                                                        obj15222222 = obj7;
                                                        obj14 = obj3;
                                                        str9 = str43;
                                                        map = map4;
                                                        str10 = str45;
                                                        str74222222 = str44;
                                                        function13 = function110;
                                                        continuation4 = continuation2;
                                                        Log.INSTANCE.e(str8, "Error in method " + str9 + ": " + e.getMessage());
                                                        continuation5 = continuation4;
                                                        str16 = str8;
                                                        str17 = str3;
                                                        str18 = str6;
                                                        str19 = str5;
                                                        str20 = str7;
                                                        str21 = str10;
                                                        if (it.hasNext()) {
                                                        }
                                                    }
                                                    function111.invoke(obj14);
                                                    str25 = str42;
                                                    continuation6 = continuation8;
                                                    it3 = it4;
                                                    obj2 = obj7;
                                                    str36 = str46;
                                                    str31 = str48;
                                                    str9 = str43;
                                                    str33 = str45;
                                                    str32 = str44;
                                                    filepress3 = filepress4;
                                                    filepress$getUrl$13 = filepress$getUrl$12;
                                                    i4 = i;
                                                    i3 = i7;
                                                    str35 = str47;
                                                    map3 = map4;
                                                    function18 = function110;
                                                    length = i9;
                                                    i6 = i8;
                                                    str26 = str41;
                                                    i5 = i6 + 1;
                                                    str16 = str8;
                                                    str24 = str4;
                                                    if (i5 >= length) {
                                                        Filepress$getUrl$1 filepress$getUrl$110 = filepress$getUrl$13;
                                                        Object obj23 = obj2;
                                                        str4 = str24;
                                                        String str88 = str16;
                                                        if (i3 != 0) {
                                                            return Unit.INSTANCE;
                                                        }
                                                        Function1<? super ExtractorLink, Unit> function118 = function18;
                                                        i = i4;
                                                        map = map3;
                                                        str74222222 = str32;
                                                        obj15222222 = obj23;
                                                        function13 = function118;
                                                        it = it3;
                                                        str16 = str88;
                                                        obj14 = obj3;
                                                        str17 = str3;
                                                        str18 = str6;
                                                        continuation5 = continuation6;
                                                        filepress2 = filepress3;
                                                        str20 = str7;
                                                        str21 = str33;
                                                        str75222222 = str35;
                                                        filepress$getUrl$12 = filepress$getUrl$110;
                                                        str19 = str5;
                                                    }
                                                } else {
                                                    str8 = str16;
                                                    filepress$getUrl$13 = filepress$getUrl$13;
                                                    obj2 = obj21;
                                                    str25 = str86;
                                                    i6 = i12;
                                                    str26 = str41;
                                                    length = length;
                                                    i5 = i6 + 1;
                                                    str16 = str8;
                                                    str24 = str4;
                                                    if (i5 >= length) {
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        str4 = str24;
                                        continuation5 = continuation10;
                                        obj14 = obj;
                                        i = i2;
                                        str75222222 = str28;
                                        it = it2;
                                        str74222222 = str27;
                                        str16 = str16;
                                        obj15222222 = coroutine_suspended;
                                        str17 = str3;
                                        str18 = str6;
                                        str21 = str22;
                                        function13 = function17;
                                        str19 = str19;
                                        str20 = str20;
                                        map = map2;
                                    }
                                }
                            }
                            if (it.hasNext()) {
                                return Unit.INSTANCE;
                            }
                        }
                        break;
                    case 1:
                        function113 = (Function1) filepress$getUrl$12.L$4;
                        function112 = (Function1) filepress$getUrl$12.L$3;
                        str53 = (String) filepress$getUrl$12.L$2;
                        str52 = (String) filepress$getUrl$12.L$1;
                        Filepress filepress7 = (Filepress) filepress$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj14);
                            obj10 = obj14;
                            obj8 = obj10;
                            str17 = "method";
                            str4 = "status";
                            str49 = "application/json";
                            str18 = "id";
                            str50 = "";
                            str51 = "data";
                            str16 = "FilePress";
                            niceResponse2 = null;
                            filepress5 = filepress7;
                            obj9 = coroutine_suspended;
                            function15 = function112;
                            niceResponse3 = (NiceResponse) obj10;
                            str11 = str52;
                            filepress2 = filepress5;
                            function114 = function113;
                            str12 = str53;
                        } catch (Exception e27) {
                            obj8 = obj14;
                            str17 = "method";
                            str4 = "status";
                            str49 = "application/json";
                            str18 = "id";
                            str50 = "";
                            str51 = "data";
                            str16 = "FilePress";
                            niceResponse2 = null;
                            filepress5 = filepress7;
                            obj9 = coroutine_suspended;
                            function15 = function112;
                            niceResponse3 = niceResponse2;
                            str11 = str52;
                            filepress2 = filepress5;
                            function114 = function113;
                            str12 = str53;
                            niceResponse4 = niceResponse3;
                            if (niceResponse4 != null) {
                            }
                            str54 = str11;
                            String str67222 = str54;
                            Result.Companion companion322 = Result.Companion;
                            obj11 = Result.constructor-impl(new URI(str67222));
                            if (Result.isFailure-impl(obj11)) {
                            }
                            uri2 = (URI) obj11;
                            if (uri2 == null) {
                            }
                            uri3 = uri2;
                            String str68222 = uri3.getScheme() + "://" + uri3.getHost();
                            find$default = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str67222, 0, 2, niceResponse2);
                            if (find$default == null) {
                            }
                            i10 = 1;
                            find$default2 = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str11, 0, 2, niceResponse2);
                            if (find$default2 != null) {
                            }
                            function14 = function114;
                            str55 = str12;
                            str56 = str11;
                            str8 = str16;
                            return Unit.INSTANCE;
                        }
                        niceResponse4 = niceResponse3;
                        if (niceResponse4 != null) {
                        }
                        str54 = str11;
                        String str672222 = str54;
                        Result.Companion companion3222 = Result.Companion;
                        obj11 = Result.constructor-impl(new URI(str672222));
                        if (Result.isFailure-impl(obj11)) {
                        }
                        uri2 = (URI) obj11;
                        if (uri2 == null) {
                        }
                        uri3 = uri2;
                        String str682222 = uri3.getScheme() + "://" + uri3.getHost();
                        find$default = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str672222, 0, 2, niceResponse2);
                        if (find$default == null) {
                            break;
                        }
                        i10 = 1;
                        find$default2 = Regex.find$default(new Regex("/file/([a-zA-Z0-9]+)"), str11, 0, 2, niceResponse2);
                        if (find$default2 != null) {
                            break;
                        }
                        function14 = function114;
                        str55 = str12;
                        str56 = str11;
                        str8 = str16;
                        return Unit.INSTANCE;
                    case 2:
                        objectRef4 = (Ref.ObjectRef) filepress$getUrl$12.L$12;
                        objectRef3 = (Ref.ObjectRef) filepress$getUrl$12.L$11;
                        map5 = (Map) filepress$getUrl$12.L$10;
                        str21 = (String) filepress$getUrl$12.L$9;
                        str58 = (String) filepress$getUrl$12.L$8;
                        uri3 = (URI) filepress$getUrl$12.L$7;
                        str60 = (String) filepress$getUrl$12.L$6;
                        niceResponse4 = (NiceResponse) filepress$getUrl$12.L$5;
                        function115 = (Function1) filepress$getUrl$12.L$4;
                        function15 = (Function1) filepress$getUrl$12.L$3;
                        str59 = (String) filepress$getUrl$12.L$2;
                        String str89 = (String) filepress$getUrl$12.L$1;
                        filepress6 = (Filepress) filepress$getUrl$12.L$0;
                        try {
                            ResultKt.throwOnFailure(obj14);
                            filepress$getUrl$15 = filepress$getUrl$12;
                            obj8 = obj14;
                            str17 = "method";
                            str4 = "status";
                            str19 = "application/json";
                            str18 = "id";
                            str65 = "";
                            str51 = "data";
                            str16 = "FilePress";
                            str64 = str58;
                            str11 = str89;
                            filepress2 = filepress6;
                            objectRef5 = objectRef4;
                            obj9 = coroutine_suspended;
                            str20 = str51;
                            optJSONObject = new JSONObject(((NiceResponse) obj14).getText()).optJSONObject(str20);
                            if (optJSONObject == null) {
                            }
                            if (optString == null) {
                            }
                            objectRef3.element = optString;
                            if (optJSONObject == null) {
                            }
                            if (optLong <= 0) {
                            }
                            objectRef6 = objectRef4;
                            function117 = function115;
                            str66 = str59;
                        } catch (Exception e28) {
                            filepress$getUrl$15 = filepress$getUrl$12;
                            obj8 = obj14;
                            str17 = "method";
                            str4 = "status";
                            str19 = "application/json";
                            str18 = "id";
                            str20 = "data";
                            str16 = "FilePress";
                            obj13 = coroutine_suspended;
                            str11 = str89;
                            str64 = str58;
                            filepress2 = filepress6;
                            objectRef6 = objectRef4;
                            function117 = function115;
                            str66 = str59;
                            charSequence = (CharSequence) objectRef3.element;
                            if (charSequence.length() == 0) {
                            }
                            int indexQuality2222222 = ExtractorsKt.getIndexQuality((String) charSequence);
                            StringBuilder sb3222222 = new StringBuilder();
                            if (((CharSequence) objectRef3.element).length() > 0) {
                            }
                            if (((CharSequence) objectRef6.element).length() > 0) {
                            }
                            String sb22222222 = sb3222222.toString();
                            List listOf2222222 = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                            it = listOf2222222.iterator();
                            str14 = str66;
                            list = listOf2222222;
                            objectRef2 = objectRef6;
                            function13 = function117;
                            objectRef = objectRef3;
                            map = map5;
                            str13 = str11;
                            filepress$getUrl$12 = filepress$getUrl$15;
                            Object obj152222222 = obj13;
                            obj14 = obj8;
                            uri = uri3;
                            str15 = str60;
                            niceResponse = niceResponse4;
                            function16 = function15;
                            String str742222222 = str64;
                            i = i11;
                            continuation5 = continuation;
                            String str752222222 = sb22222222;
                            if (it.hasNext()) {
                            }
                        }
                        charSequence = (CharSequence) objectRef3.element;
                        if (charSequence.length() == 0) {
                        }
                        int indexQuality22222222 = ExtractorsKt.getIndexQuality((String) charSequence);
                        StringBuilder sb32222222 = new StringBuilder();
                        if (((CharSequence) objectRef3.element).length() > 0) {
                        }
                        if (((CharSequence) objectRef6.element).length() > 0) {
                        }
                        String sb222222222 = sb32222222.toString();
                        List listOf22222222 = CollectionsKt.listOf(new String[]{"indexDownlaod", "cloudR2Downlaod"});
                        it = listOf22222222.iterator();
                        str14 = str66;
                        list = listOf22222222;
                        objectRef2 = objectRef6;
                        function13 = function117;
                        objectRef = objectRef3;
                        map = map5;
                        str13 = str11;
                        filepress$getUrl$12 = filepress$getUrl$15;
                        Object obj1522222222 = obj13;
                        obj14 = obj8;
                        uri = uri3;
                        str15 = str60;
                        niceResponse = niceResponse4;
                        function16 = function15;
                        String str7422222222 = str64;
                        i = i11;
                        continuation5 = continuation;
                        String str7522222222 = sb222222222;
                        if (it.hasNext()) {
                        }
                        break;
                    case 3:
                        int i13 = filepress$getUrl$12.I$0;
                        String str90 = (String) filepress$getUrl$12.L$17;
                        String str91 = (String) filepress$getUrl$12.L$16;
                        Iterator it6 = (Iterator) filepress$getUrl$12.L$15;
                        list = (List) filepress$getUrl$12.L$14;
                        String str92 = (String) filepress$getUrl$12.L$13;
                        objectRef2 = (Ref.ObjectRef) filepress$getUrl$12.L$12;
                        objectRef = (Ref.ObjectRef) filepress$getUrl$12.L$11;
                        Map map7 = (Map) filepress$getUrl$12.L$10;
                        String str93 = (String) filepress$getUrl$12.L$9;
                        String str94 = (String) filepress$getUrl$12.L$8;
                        uri = (URI) filepress$getUrl$12.L$7;
                        str15 = (String) filepress$getUrl$12.L$6;
                        niceResponse = (NiceResponse) filepress$getUrl$12.L$5;
                        Function1<? super ExtractorLink, Unit> function119 = (Function1) filepress$getUrl$12.L$4;
                        function16 = (Function1) filepress$getUrl$12.L$3;
                        str14 = (String) filepress$getUrl$12.L$2;
                        str13 = (String) filepress$getUrl$12.L$1;
                        Filepress filepress8 = (Filepress) filepress$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj14);
                        str37 = str90;
                        filepress2 = filepress8;
                        continuation7 = continuation8;
                        obj6 = obj14;
                        obj5 = obj6;
                        str3 = "method";
                        str4 = "status";
                        str19 = "application/json";
                        str39 = "id";
                        str20 = "data";
                        str16 = "FilePress";
                        str38 = str91;
                        it2 = it6;
                        str28 = str92;
                        map = map7;
                        str10 = str93;
                        str27 = str94;
                        function19 = function119;
                        i2 = i13;
                        obj4 = coroutine_suspended;
                        String text3 = ((NiceResponse) obj6).getText();
                        Object obj182 = obj5;
                        jSONObject5 = new JSONObject(text3);
                        Object obj192 = obj4;
                        String str802 = str37;
                        str24 = str4;
                        if (jSONObject5.optBoolean(str24, false)) {
                        }
                        if (it.hasNext()) {
                        }
                        break;
                    case 4:
                        int i14 = filepress$getUrl$12.I$0;
                        String str95 = (String) filepress$getUrl$12.L$21;
                        str29 = (String) filepress$getUrl$12.L$20;
                        JSONObject jSONObject10 = (JSONObject) filepress$getUrl$12.L$19;
                        String str96 = (String) filepress$getUrl$12.L$18;
                        String str97 = (String) filepress$getUrl$12.L$17;
                        str23 = (String) filepress$getUrl$12.L$16;
                        Iterator it7 = (Iterator) filepress$getUrl$12.L$15;
                        list = (List) filepress$getUrl$12.L$14;
                        String str98 = (String) filepress$getUrl$12.L$13;
                        objectRef2 = (Ref.ObjectRef) filepress$getUrl$12.L$12;
                        objectRef = (Ref.ObjectRef) filepress$getUrl$12.L$11;
                        Map map8 = (Map) filepress$getUrl$12.L$10;
                        str22 = (String) filepress$getUrl$12.L$9;
                        String str99 = (String) filepress$getUrl$12.L$8;
                        uri = (URI) filepress$getUrl$12.L$7;
                        str15 = (String) filepress$getUrl$12.L$6;
                        niceResponse = (NiceResponse) filepress$getUrl$12.L$5;
                        function17 = (Function1) filepress$getUrl$12.L$4;
                        function16 = (Function1) filepress$getUrl$12.L$3;
                        str14 = (String) filepress$getUrl$12.L$2;
                        str13 = (String) filepress$getUrl$12.L$1;
                        Filepress filepress9 = (Filepress) filepress$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj14);
                        str3 = "method";
                        str6 = "id";
                        str16 = "FilePress";
                        str25 = str97;
                        str30 = str95;
                        it2 = it7;
                        map2 = map8;
                        filepress2 = filepress9;
                        continuation2 = continuation8;
                        i2 = i14;
                        str24 = "status";
                        str19 = "application/json";
                        str20 = "data";
                        str28 = str98;
                        jSONObject = jSONObject10;
                        str26 = str96;
                        str27 = str99;
                        obj = obj14;
                        String text22 = ((NiceResponse) obj14).getText();
                        String str852 = str30;
                        jSONObject2 = new JSONObject(text22);
                        Continuation<? super Unit> continuation102 = continuation2;
                        if (jSONObject2.optBoolean(str24, false)) {
                        }
                        if (it.hasNext()) {
                        }
                        break;
                    case 5:
                        int i15 = filepress$getUrl$12.I$3;
                        int i16 = filepress$getUrl$12.I$2;
                        int i17 = filepress$getUrl$12.I$1;
                        i = filepress$getUrl$12.I$0;
                        Function1<? super ExtractorLink, Unit> function120 = (Function1) filepress$getUrl$12.L$26;
                        JSONArray jSONArray = (JSONArray) filepress$getUrl$12.L$24;
                        JSONObject jSONObject11 = (JSONObject) filepress$getUrl$12.L$23;
                        String str100 = (String) filepress$getUrl$12.L$22;
                        str46 = (String) filepress$getUrl$12.L$21;
                        str48 = (String) filepress$getUrl$12.L$20;
                        JSONObject jSONObject12 = (JSONObject) filepress$getUrl$12.L$19;
                        String str101 = (String) filepress$getUrl$12.L$18;
                        String str102 = (String) filepress$getUrl$12.L$17;
                        str43 = (String) filepress$getUrl$12.L$16;
                        Iterator it8 = (Iterator) filepress$getUrl$12.L$15;
                        list = (List) filepress$getUrl$12.L$14;
                        String str103 = (String) filepress$getUrl$12.L$13;
                        objectRef2 = (Ref.ObjectRef) filepress$getUrl$12.L$12;
                        objectRef = (Ref.ObjectRef) filepress$getUrl$12.L$11;
                        map4 = (Map) filepress$getUrl$12.L$10;
                        str45 = (String) filepress$getUrl$12.L$9;
                        str44 = (String) filepress$getUrl$12.L$8;
                        uri = (URI) filepress$getUrl$12.L$7;
                        str15 = (String) filepress$getUrl$12.L$6;
                        niceResponse = (NiceResponse) filepress$getUrl$12.L$5;
                        function110 = (Function1) filepress$getUrl$12.L$4;
                        function16 = (Function1) filepress$getUrl$12.L$3;
                        str14 = (String) filepress$getUrl$12.L$2;
                        str13 = (String) filepress$getUrl$12.L$1;
                        Filepress filepress10 = (Filepress) filepress$getUrl$12.L$0;
                        ResultKt.throwOnFailure(obj14);
                        str3 = "method";
                        str4 = "status";
                        str5 = "application/json";
                        i7 = i17;
                        str7 = "data";
                        function111 = function120;
                        obj7 = coroutine_suspended;
                        jSONObject4 = jSONObject11;
                        str34 = str100;
                        str42 = str102;
                        it4 = it8;
                        i8 = i16;
                        obj3 = obj14;
                        str6 = "id";
                        str8 = "FilePress";
                        i9 = i15;
                        str47 = str103;
                        filepress4 = filepress10;
                        optJSONArray = jSONArray;
                        jSONObject3 = jSONObject12;
                        str41 = str101;
                        function111.invoke(obj14);
                        str25 = str42;
                        continuation6 = continuation8;
                        it3 = it4;
                        obj2 = obj7;
                        str36 = str46;
                        str31 = str48;
                        str9 = str43;
                        str33 = str45;
                        str32 = str44;
                        filepress3 = filepress4;
                        filepress$getUrl$13 = filepress$getUrl$12;
                        i4 = i;
                        i3 = i7;
                        str35 = str47;
                        map3 = map4;
                        function18 = function110;
                        length = i9;
                        i6 = i8;
                        str26 = str41;
                        i5 = i6 + 1;
                        str16 = str8;
                        str24 = str4;
                        if (i5 >= length) {
                        }
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        filepress$getUrl$1 = new Filepress$getUrl$1(filepress, continuation8);
        filepress$getUrl$12 = filepress$getUrl$1;
        Object obj142 = filepress$getUrl$12.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (filepress$getUrl$12.label) {
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
