package com.cncverse;

import com.lagradost.api.Log;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.cloudstream3.ParCollectionsKt;
import com.lagradost.cloudstream3.SubtitleFile;
import com.lagradost.cloudstream3.utils.ExtractorApiKt;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import com.lagradost.cloudstream3.utils.ExtractorLinkType;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.RequestBody;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: Extractors.kt */
@Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u00012\u000b\u0010\u0002\u001a\u00070\u0003¢\u0006\u0002\b\u0004H\n"}, d2 = {"<anonymous>", "", "anchor", "Lorg/jsoup/nodes/Element;", "Lkotlin/jvm/internal/EnhancedNullability;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2", f = "Extractors.kt", i = {0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 7, 7, 7}, l = {426, 435, 447, 457, 458, 482, 528, 529}, m = "invokeSuspend", n = {"anchor", "text", "link", "anchor", "text", "link", "decodedLink", "anchor", "text", "link", "baseUrlLink", "finalURL", "anchor", "text", "link", "anchor", "text", "link", "anchor", "text", "link", "driveLink", "id", "doId", "baseUrls", "anchor", "text", "link", "anchor", "text", "link"}, nl = {425, 434, 446, 458, 470, 521, 529, 535}, s = {"L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$0", "L$1", "L$2", "L$0", "L$1", "L$2"}, v = 2)
/* loaded from: classes.dex */
public final class GDFlix$getUrl$2 extends SuspendLambda implements Function2<Element, Continuation<? super Object>, Object> {
    final /* synthetic */ Function1<ExtractorLink, Unit> $callback;
    final /* synthetic */ String $fileName;
    final /* synthetic */ String $fileSize;
    final /* synthetic */ String $latestUrl;
    final /* synthetic */ int $quality;
    final /* synthetic */ Function1<SubtitleFile, Unit> $subtitleCallback;
    /* synthetic */ Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    int label;
    final /* synthetic */ GDFlix this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Extractors.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 3, 0}, xi = 48)
    @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$1", f = "Extractors.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
    /* renamed from: com.cncverse.GDFlix$getUrl$2$1, reason: invalid class name */
    /* loaded from: classes.dex */
    public static final class AnonymousClass1 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
        final /* synthetic */ int $quality;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(int i, Continuation<? super AnonymousClass1> continuation) {
            super(2, continuation);
            this.$quality = i;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass1 = new AnonymousClass1(this.$quality, continuation);
            anonymousClass1.L$0 = obj;
            return anonymousClass1;
        }

        public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
            return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object obj) {
            ExtractorLink extractorLink = (ExtractorLink) this.L$0;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    extractorLink.setQuality(this.$quality);
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Extractors.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 3, 0}, xi = 48)
    @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$2", f = "Extractors.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
    /* renamed from: com.cncverse.GDFlix$getUrl$2$2, reason: invalid class name */
    /* loaded from: classes.dex */
    public static final class AnonymousClass2 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
        final /* synthetic */ int $quality;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(int i, Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
            this.$quality = i;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass2 = new AnonymousClass2(this.$quality, continuation);
            anonymousClass2.L$0 = obj;
            return anonymousClass2;
        }

        public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
            return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object obj) {
            ExtractorLink extractorLink = (ExtractorLink) this.L$0;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    extractorLink.setQuality(this.$quality);
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Extractors.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 3, 0}, xi = 48)
    @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$3", f = "Extractors.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
    /* renamed from: com.cncverse.GDFlix$getUrl$2$3, reason: invalid class name */
    /* loaded from: classes.dex */
    public static final class AnonymousClass3 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
        final /* synthetic */ int $quality;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass3(int i, Continuation<? super AnonymousClass3> continuation) {
            super(2, continuation);
            this.$quality = i;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass3 = new AnonymousClass3(this.$quality, continuation);
            anonymousClass3.L$0 = obj;
            return anonymousClass3;
        }

        public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
            return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object obj) {
            ExtractorLink extractorLink = (ExtractorLink) this.L$0;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    extractorLink.setQuality(this.$quality);
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Extractors.kt */
    @Metadata(d1 = {"\u0000\u0014\n\u0000\n\u0002\u0010 \n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\u000b\u0010\u0003\u001a\u00070\u0004¢\u0006\u0002\b\u0005H\n"}, d2 = {"<anonymous>", "", "", "btn", "Lorg/jsoup/nodes/Element;", "Lkotlin/jvm/internal/EnhancedNullability;"}, k = 3, mv = {2, 3, 0}, xi = 48)
    @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$4", f = "Extractors.kt", i = {0, 0, 1, 1}, l = {460, 461}, m = "invokeSuspend", n = {"btn", "serverUrl", "btn", "serverUrl"}, nl = {461, 468}, s = {"L$0", "L$1", "L$0", "L$1"}, v = 2)
    /* renamed from: com.cncverse.GDFlix$getUrl$2$4, reason: invalid class name */
    /* loaded from: classes.dex */
    public static final class AnonymousClass4 extends SuspendLambda implements Function2<Element, Continuation<? super List<? extends Unit>>, Object> {
        final /* synthetic */ Function1<ExtractorLink, Unit> $callback;
        final /* synthetic */ String $fileName;
        final /* synthetic */ String $fileSize;
        final /* synthetic */ String $latestUrl;
        final /* synthetic */ int $quality;
        /* synthetic */ Object L$0;
        Object L$1;
        int label;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* compiled from: Extractors.kt */
        @Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u00012\u000b\u0010\u0002\u001a\u00070\u0003¢\u0006\u0002\b\u0004H\n"}, d2 = {"<anonymous>", "", "sourceAnchor", "Lorg/jsoup/nodes/Element;", "Lkotlin/jvm/internal/EnhancedNullability;"}, k = 3, mv = {2, 3, 0}, xi = 48)
        @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$4$1", f = "Extractors.kt", i = {0, 0}, l = {464}, m = "invokeSuspend", n = {"sourceAnchor", "source"}, nl = {463}, s = {"L$0", "L$1"}, v = 2)
        /* renamed from: com.cncverse.GDFlix$getUrl$2$4$1, reason: invalid class name */
        /* loaded from: classes.dex */
        public static final class AnonymousClass1 extends SuspendLambda implements Function2<Element, Continuation<? super Unit>, Object> {
            final /* synthetic */ Function1<ExtractorLink, Unit> $callback;
            final /* synthetic */ String $fileName;
            final /* synthetic */ String $fileSize;
            final /* synthetic */ int $quality;
            /* synthetic */ Object L$0;
            Object L$1;
            Object L$2;
            int label;

            /* JADX INFO: Access modifiers changed from: package-private */
            /* compiled from: Extractors.kt */
            @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 3, 0}, xi = 48)
            @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$4$1$1", f = "Extractors.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
            /* renamed from: com.cncverse.GDFlix$getUrl$2$4$1$1, reason: invalid class name and collision with other inner class name */
            /* loaded from: classes.dex */
            public static final class C00001 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
                final /* synthetic */ int $quality;
                private /* synthetic */ Object L$0;
                int label;

                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                C00001(int i, Continuation<? super C00001> continuation) {
                    super(2, continuation);
                    this.$quality = i;
                }

                public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
                    Continuation<Unit> c00001 = new C00001(this.$quality, continuation);
                    c00001.L$0 = obj;
                    return c00001;
                }

                public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
                    return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
                }

                public final Object invokeSuspend(Object obj) {
                    ExtractorLink extractorLink = (ExtractorLink) this.L$0;
                    IntrinsicsKt.getCOROUTINE_SUSPENDED();
                    switch (this.label) {
                        case 0:
                            ResultKt.throwOnFailure(obj);
                            extractorLink.setQuality(this.$quality);
                            return Unit.INSTANCE;
                        default:
                            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                }
            }

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            AnonymousClass1(Function1<? super ExtractorLink, Unit> function1, String str, String str2, int i, Continuation<? super AnonymousClass1> continuation) {
                super(2, continuation);
                this.$callback = function1;
                this.$fileName = str;
                this.$fileSize = str2;
                this.$quality = i;
            }

            public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
                Continuation<Unit> anonymousClass1 = new AnonymousClass1(this.$callback, this.$fileName, this.$fileSize, this.$quality, continuation);
                anonymousClass1.L$0 = obj;
                return anonymousClass1;
            }

            public final Object invoke(Element element, Continuation<? super Unit> continuation) {
                return create(element, continuation).invokeSuspend(Unit.INSTANCE);
            }

            public final Object invokeSuspend(Object obj) {
                Object newExtractorLink$default;
                Function1<ExtractorLink, Unit> function1;
                Element element = (Element) this.L$0;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        String attr = element.attr("href");
                        Function1<ExtractorLink, Unit> function12 = this.$callback;
                        String str = this.$fileName;
                        String str2 = this.$fileSize;
                        this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                        this.L$1 = SpillingKt.nullOutSpilledVariable(attr);
                        this.L$2 = function12;
                        this.label = 1;
                        newExtractorLink$default = ExtractorApiKt.newExtractorLink$default("GDFlix[Index]", "GDFlix[Index] " + str + "[" + str2 + "]", attr, (ExtractorLinkType) null, new C00001(this.$quality, null), (Continuation) this, 8, (Object) null);
                        if (newExtractorLink$default != coroutine_suspended) {
                            function1 = function12;
                            break;
                        } else {
                            return coroutine_suspended;
                        }
                    case 1:
                        function1 = (Function1) this.L$2;
                        ResultKt.throwOnFailure(obj);
                        newExtractorLink$default = obj;
                        break;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                function1.invoke(newExtractorLink$default);
                return Unit.INSTANCE;
            }
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass4(String str, Function1<? super ExtractorLink, Unit> function1, String str2, String str3, int i, Continuation<? super AnonymousClass4> continuation) {
            super(2, continuation);
            this.$latestUrl = str;
            this.$callback = function1;
            this.$fileName = str2;
            this.$fileSize = str3;
            this.$quality = i;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass4 = new AnonymousClass4(this.$latestUrl, this.$callback, this.$fileName, this.$fileSize, this.$quality, continuation);
            anonymousClass4.L$0 = obj;
            return anonymousClass4;
        }

        public final Object invoke(Element element, Continuation<? super List<Unit>> continuation) {
            return create(element, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object obj) {
            Object obj2;
            String str;
            Element element = (Element) this.L$0;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    String str2 = this.$latestUrl + element.attr("href");
                    this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                    this.L$1 = SpillingKt.nullOutSpilledVariable(str2);
                    this.label = 1;
                    Object obj3 = Requests.get$default(MainActivityKt.getApp(), str2, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, (Continuation) this, 4094, (Object) null);
                    if (obj3 != coroutine_suspended) {
                        obj2 = obj3;
                        str = str2;
                        break;
                    } else {
                        return coroutine_suspended;
                    }
                case 1:
                    str = (String) this.L$1;
                    ResultKt.throwOnFailure(obj);
                    obj2 = obj;
                    break;
                case 2:
                    ResultKt.throwOnFailure(obj);
                    return obj;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            this.L$0 = SpillingKt.nullOutSpilledVariable(element);
            this.L$1 = SpillingKt.nullOutSpilledVariable(str);
            this.label = 2;
            Object amap = ParCollectionsKt.amap(((NiceResponse) obj2).getDocument().select("div.mb-4 > a"), new AnonymousClass1(this.$callback, this.$fileName, this.$fileSize, this.$quality, null), (Continuation) this);
            return amap == coroutine_suspended ? coroutine_suspended : amap;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Extractors.kt */
    @Metadata(d1 = {"\u0000\f\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\n"}, d2 = {"<anonymous>", "", "baseUrl", ""}, k = 3, mv = {2, 3, 0}, xi = 48)
    @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$5", f = "Extractors.kt", i = {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}, l = {484, 503, 514}, m = "invokeSuspend", n = {"baseUrl", "indexbotLink", "baseUrl", "indexbotLink", "indexbotResponse", "cookiesSSID", "indexbotDoc", "token", "postId", "requestBody", "headers", "cookies", "baseUrl", "indexbotLink", "indexbotResponse", "cookiesSSID", "indexbotDoc", "token", "postId", "requestBody", "headers", "cookies", "downloadLink"}, nl = {486, 509, 513}, s = {"L$0", "L$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10"}, v = 2)
    /* renamed from: com.cncverse.GDFlix$getUrl$2$5, reason: invalid class name */
    /* loaded from: classes.dex */
    public static final class AnonymousClass5 extends SuspendLambda implements Function2<String, Continuation<? super Unit>, Object> {
        final /* synthetic */ Function1<ExtractorLink, Unit> $callback;
        final /* synthetic */ String $doId;
        final /* synthetic */ String $fileName;
        final /* synthetic */ String $fileSize;
        final /* synthetic */ String $id;
        final /* synthetic */ int $quality;
        /* synthetic */ Object L$0;
        Object L$1;
        Object L$10;
        Object L$11;
        Object L$2;
        Object L$3;
        Object L$4;
        Object L$5;
        Object L$6;
        Object L$7;
        Object L$8;
        Object L$9;
        int label;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* compiled from: Extractors.kt */
        @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;"}, k = 3, mv = {2, 3, 0}, xi = 48)
        @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$5$1", f = "Extractors.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
        /* renamed from: com.cncverse.GDFlix$getUrl$2$5$1, reason: invalid class name */
        /* loaded from: classes.dex */
        public static final class AnonymousClass1 extends SuspendLambda implements Function2<ExtractorLink, Continuation<? super Unit>, Object> {
            final /* synthetic */ String $baseUrl;
            final /* synthetic */ int $quality;
            private /* synthetic */ Object L$0;
            int label;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            AnonymousClass1(String str, int i, Continuation<? super AnonymousClass1> continuation) {
                super(2, continuation);
                this.$baseUrl = str;
                this.$quality = i;
            }

            public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
                Continuation<Unit> anonymousClass1 = new AnonymousClass1(this.$baseUrl, this.$quality, continuation);
                anonymousClass1.L$0 = obj;
                return anonymousClass1;
            }

            public final Object invoke(ExtractorLink extractorLink, Continuation<? super Unit> continuation) {
                return create(extractorLink, continuation).invokeSuspend(Unit.INSTANCE);
            }

            public final Object invokeSuspend(Object obj) {
                ExtractorLink extractorLink = (ExtractorLink) this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        extractorLink.setReferer(this.$baseUrl);
                        extractorLink.setQuality(this.$quality);
                        return Unit.INSTANCE;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass5(String str, String str2, Function1<? super ExtractorLink, Unit> function1, String str3, String str4, int i, Continuation<? super AnonymousClass5> continuation) {
            super(2, continuation);
            this.$id = str;
            this.$doId = str2;
            this.$callback = function1;
            this.$fileName = str3;
            this.$fileSize = str4;
            this.$quality = i;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass5 = new AnonymousClass5(this.$id, this.$doId, this.$callback, this.$fileName, this.$fileSize, this.$quality, continuation);
            anonymousClass5.L$0 = obj;
            return anonymousClass5;
        }

        public final Object invoke(String str, Continuation<? super Unit> continuation) {
            return create(str, continuation).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Failed to find 'out' block for switch in B:2:0x0013. Please report as an issue. */
        /* JADX WARN: Removed duplicated region for block: B:19:0x024f  */
        /* JADX WARN: Removed duplicated region for block: B:22:0x02e2 A[RETURN] */
        /* JADX WARN: Removed duplicated region for block: B:23:0x02e3  */
        /* JADX WARN: Removed duplicated region for block: B:28:0x00f2  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public final Object invokeSuspend(Object obj) {
            Object obj2;
            String str;
            NiceResponse niceResponse;
            String str2;
            String str3;
            Map mapOf;
            Object post$default;
            String str4;
            FormBody formBody;
            Document document;
            String str5;
            NiceResponse niceResponse2;
            Map map;
            List groupValues;
            List groupValues2;
            String replace$default;
            Object newExtractorLink$default;
            Function1<ExtractorLink, Unit> function1;
            String str6;
            List groupValues3;
            String str7;
            String str8 = (String) this.L$0;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    String str9 = str8 + "/download?id=" + this.$id + "&do=" + this.$doId;
                    this.L$0 = str8;
                    this.L$1 = str9;
                    this.label = 1;
                    obj2 = Requests.get$default(MainActivityKt.getApp(), str9, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 100L, (Interceptor) null, false, (ResponseParser) null, (Continuation) this, 3838, (Object) null);
                    if (obj2 == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                    str = str9;
                    niceResponse = (NiceResponse) obj2;
                    if (niceResponse.isSuccessful()) {
                        str2 = (String) niceResponse.getCookies().get("PHPSESSID");
                        Document document2 = niceResponse.getDocument();
                        MatchResult find$default = Regex.find$default(new Regex("formData\\.append\\('token', '([a-f0-9]+)'\\)"), document2.toString(), 0, 2, (Object) null);
                        String str10 = (find$default == null || (groupValues2 = find$default.getGroupValues()) == null) ? null : (String) groupValues2.get(1);
                        if (str10 == null) {
                            str10 = "";
                        }
                        MatchResult find$default2 = Regex.find$default(new Regex("fetch\\('/download\\?id=([a-zA-Z0-9/+]+)'"), document2.toString(), 0, 2, (Object) null);
                        str3 = (find$default2 == null || (groupValues = find$default2.getGroupValues()) == null) ? null : (String) groupValues.get(1);
                        if (str3 == null) {
                            str3 = "";
                        }
                        FormBody build = new FormBody.Builder((Charset) null, 1, (DefaultConstructorMarker) null).add("token", str10).build();
                        mapOf = MapsKt.mapOf(TuplesKt.to("Referer", str));
                        Map mapOf2 = MapsKt.mapOf(TuplesKt.to("PHPSESSID", String.valueOf(str2)));
                        this.L$0 = str8;
                        this.L$1 = SpillingKt.nullOutSpilledVariable(str);
                        this.L$2 = SpillingKt.nullOutSpilledVariable(niceResponse);
                        this.L$3 = SpillingKt.nullOutSpilledVariable(str2);
                        this.L$4 = SpillingKt.nullOutSpilledVariable(document2);
                        this.L$5 = SpillingKt.nullOutSpilledVariable(str10);
                        this.L$6 = SpillingKt.nullOutSpilledVariable(str3);
                        this.L$7 = SpillingKt.nullOutSpilledVariable(build);
                        this.L$8 = SpillingKt.nullOutSpilledVariable(mapOf);
                        this.L$9 = SpillingKt.nullOutSpilledVariable(mapOf2);
                        this.label = 2;
                        post$default = Requests.post$default(MainActivityKt.getApp(), str8 + "/download?id=" + str3, mapOf, (String) null, (Map) null, mapOf2, (Map) null, (List) null, (Object) null, (RequestBody) build, false, 0, (TimeUnit) null, 100L, (Interceptor) null, false, (ResponseParser) null, (Continuation) this, 61164, (Object) null);
                        if (post$default == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        str4 = str;
                        formBody = build;
                        String str11 = str10;
                        document = document2;
                        str5 = str11;
                        niceResponse2 = niceResponse;
                        map = mapOf2;
                        Map map2 = map;
                        MatchResult find$default3 = Regex.find$default(new Regex("url\":\"(.*?)\""), ((NiceResponse) post$default).getText(), 0, 2, (Object) null);
                        replace$default = (find$default3 != null || (groupValues3 = find$default3.getGroupValues()) == null || (str7 = (String) groupValues3.get(1)) == null) ? null : StringsKt.replace$default(str7, "\\", "", false, 4, (Object) null);
                        if (replace$default == null) {
                            replace$default = "";
                        }
                        String str12 = replace$default;
                        Function1<ExtractorLink, Unit> function12 = this.$callback;
                        String str13 = this.$fileName;
                        String str14 = this.$fileSize;
                        this.L$0 = SpillingKt.nullOutSpilledVariable(str8);
                        this.L$1 = SpillingKt.nullOutSpilledVariable(str4);
                        this.L$2 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                        this.L$3 = SpillingKt.nullOutSpilledVariable(str2);
                        this.L$4 = SpillingKt.nullOutSpilledVariable(document);
                        this.L$5 = SpillingKt.nullOutSpilledVariable(str5);
                        this.L$6 = SpillingKt.nullOutSpilledVariable(str3);
                        this.L$7 = SpillingKt.nullOutSpilledVariable(formBody);
                        this.L$8 = SpillingKt.nullOutSpilledVariable(mapOf);
                        this.L$9 = SpillingKt.nullOutSpilledVariable(map2);
                        this.L$10 = SpillingKt.nullOutSpilledVariable(str12);
                        this.L$11 = function12;
                        this.label = 3;
                        newExtractorLink$default = ExtractorApiKt.newExtractorLink$default("GDFlix[DriveBot]", "GDFlix[DriveBot] " + str13 + "[" + str14 + "]", str12, (ExtractorLinkType) null, new AnonymousClass1(str8, this.$quality, null), (Continuation) this, 8, (Object) null);
                        if (newExtractorLink$default != coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        function1 = function12;
                        str6 = str4;
                        function1.invoke(newExtractorLink$default);
                    }
                    return Unit.INSTANCE;
                case 1:
                    String str15 = (String) this.L$1;
                    ResultKt.throwOnFailure(obj);
                    str = str15;
                    obj2 = obj;
                    niceResponse = (NiceResponse) obj2;
                    if (niceResponse.isSuccessful()) {
                    }
                    return Unit.INSTANCE;
                case 2:
                    map = (Map) this.L$9;
                    Map map3 = (Map) this.L$8;
                    FormBody formBody2 = (FormBody) this.L$7;
                    String str16 = (String) this.L$6;
                    str5 = (String) this.L$5;
                    document = (Document) this.L$4;
                    String str17 = (String) this.L$3;
                    NiceResponse niceResponse3 = (NiceResponse) this.L$2;
                    String str18 = (String) this.L$1;
                    ResultKt.throwOnFailure(obj);
                    str4 = str18;
                    formBody = formBody2;
                    str2 = str17;
                    mapOf = map3;
                    str3 = str16;
                    niceResponse2 = niceResponse3;
                    post$default = obj;
                    Map map22 = map;
                    MatchResult find$default32 = Regex.find$default(new Regex("url\":\"(.*?)\""), ((NiceResponse) post$default).getText(), 0, 2, (Object) null);
                    if (find$default32 != null) {
                        break;
                    }
                    if (replace$default == null) {
                    }
                    String str122 = replace$default;
                    Function1<ExtractorLink, Unit> function122 = this.$callback;
                    String str132 = this.$fileName;
                    String str142 = this.$fileSize;
                    this.L$0 = SpillingKt.nullOutSpilledVariable(str8);
                    this.L$1 = SpillingKt.nullOutSpilledVariable(str4);
                    this.L$2 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                    this.L$3 = SpillingKt.nullOutSpilledVariable(str2);
                    this.L$4 = SpillingKt.nullOutSpilledVariable(document);
                    this.L$5 = SpillingKt.nullOutSpilledVariable(str5);
                    this.L$6 = SpillingKt.nullOutSpilledVariable(str3);
                    this.L$7 = SpillingKt.nullOutSpilledVariable(formBody);
                    this.L$8 = SpillingKt.nullOutSpilledVariable(mapOf);
                    this.L$9 = SpillingKt.nullOutSpilledVariable(map22);
                    this.L$10 = SpillingKt.nullOutSpilledVariable(str122);
                    this.L$11 = function122;
                    this.label = 3;
                    newExtractorLink$default = ExtractorApiKt.newExtractorLink$default("GDFlix[DriveBot]", "GDFlix[DriveBot] " + str132 + "[" + str142 + "]", str122, (ExtractorLinkType) null, new AnonymousClass1(str8, this.$quality, null), (Continuation) this, 8, (Object) null);
                    if (newExtractorLink$default != coroutine_suspended) {
                    }
                    break;
                case 3:
                    function1 = (Function1) this.L$11;
                    niceResponse2 = (NiceResponse) this.L$2;
                    str6 = (String) this.L$1;
                    ResultKt.throwOnFailure(obj);
                    newExtractorLink$default = obj;
                    function1.invoke(newExtractorLink$default);
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: Extractors.kt */
    @Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u00012\u000b\u0010\u0002\u001a\u00070\u0003¢\u0006\u0002\b\u0004H\n"}, d2 = {"<anonymous>", "", "gofileAnchor", "Lorg/jsoup/nodes/Element;", "Lkotlin/jvm/internal/EnhancedNullability;"}, k = 3, mv = {2, 3, 0}, xi = 48)
    @DebugMetadata(c = "com.cncverse.GDFlix$getUrl$2$6", f = "Extractors.kt", i = {0, 0}, l = {532}, m = "invokeSuspend", n = {"gofileAnchor", "gLink"}, nl = {534}, s = {"L$0", "L$1"}, v = 2)
    /* renamed from: com.cncverse.GDFlix$getUrl$2$6, reason: invalid class name */
    /* loaded from: classes.dex */
    public static final class AnonymousClass6 extends SuspendLambda implements Function2<Element, Continuation<? super Unit>, Object> {
        final /* synthetic */ Function1<ExtractorLink, Unit> $callback;
        final /* synthetic */ Function1<SubtitleFile, Unit> $subtitleCallback;
        /* synthetic */ Object L$0;
        Object L$1;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass6(Function1<? super SubtitleFile, Unit> function1, Function1<? super ExtractorLink, Unit> function12, Continuation<? super AnonymousClass6> continuation) {
            super(2, continuation);
            this.$subtitleCallback = function1;
            this.$callback = function12;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            Continuation<Unit> anonymousClass6 = new AnonymousClass6(this.$subtitleCallback, this.$callback, continuation);
            anonymousClass6.L$0 = obj;
            return anonymousClass6;
        }

        public final Object invoke(Element element, Continuation<? super Unit> continuation) {
            return create(element, continuation).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Failed to find 'out' block for switch in B:2:0x000a. Please report as an issue. */
        public final Object invokeSuspend(Object obj) {
            String str;
            Element element = (Element) this.L$0;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    String attr = element.attr("href");
                    if (StringsKt.contains$default(attr, "gofile", false, 2, (Object) null)) {
                        this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                        this.L$1 = SpillingKt.nullOutSpilledVariable(attr);
                        this.label = 1;
                        if (new Gofile().getUrl(attr, "", this.$subtitleCallback, this.$callback, (Continuation) this) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        str = attr;
                    }
                    return Unit.INSTANCE;
                case 1:
                    str = (String) this.L$1;
                    ResultKt.throwOnFailure(obj);
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public GDFlix$getUrl$2(Function1<? super ExtractorLink, Unit> function1, String str, String str2, GDFlix gDFlix, String str3, int i, Function1<? super SubtitleFile, Unit> function12, Continuation<? super GDFlix$getUrl$2> continuation) {
        super(2, continuation);
        this.$callback = function1;
        this.$fileName = str;
        this.$fileSize = str2;
        this.this$0 = gDFlix;
        this.$latestUrl = str3;
        this.$quality = i;
        this.$subtitleCallback = function12;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        Continuation<Unit> gDFlix$getUrl$2 = new GDFlix$getUrl$2(this.$callback, this.$fileName, this.$fileSize, this.this$0, this.$latestUrl, this.$quality, this.$subtitleCallback, continuation);
        gDFlix$getUrl$2.L$0 = obj;
        return gDFlix$getUrl$2;
    }

    public final Object invoke(Element element, Continuation<Object> continuation) {
        return create(element, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:3:0x0010. Please report as an issue. */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:18:0x03ce A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:45:0x029a A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:46:0x029b  */
    /* JADX WARN: Type inference failed for: r3v0, types: [int] */
    /* JADX WARN: Type inference failed for: r3v1 */
    /* JADX WARN: Type inference failed for: r3v10 */
    /* JADX WARN: Type inference failed for: r3v11 */
    /* JADX WARN: Type inference failed for: r3v12 */
    /* JADX WARN: Type inference failed for: r3v25, types: [java.lang.Object] */
    /* JADX WARN: Type inference failed for: r3v3 */
    /* JADX WARN: Type inference failed for: r3v38, types: [java.lang.Object] */
    /* JADX WARN: Type inference failed for: r3v47 */
    /* JADX WARN: Type inference failed for: r3v50 */
    /* JADX WARN: Type inference failed for: r3v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        String str;
        Object newExtractorLink$default;
        Function1<ExtractorLink, Unit> function1;
        String str2;
        Object obj2;
        String str3;
        Object obj3;
        String str4;
        String str5;
        String str6;
        Object amap;
        Object newExtractorLink$default2;
        String str7;
        Function1<ExtractorLink, Unit> function12;
        String str8;
        Object newExtractorLink$default3;
        Function1<ExtractorLink, Unit> function13;
        String str9;
        String str10;
        Object amap2;
        String str11;
        String str12;
        Object amap3;
        String str13;
        Element element = (Element) this.L$0;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        ?? r3 = this.label;
        String str14 = "Index Links";
        String str15 = null;
        try {
            try {
                switch (r3) {
                    case 0:
                        ResultKt.throwOnFailure(obj);
                        String text = element.select("a").text();
                        String attr = element.attr("href");
                        if (StringsKt.contains$default(text, "DIRECT DL", false, 2, (Object) null)) {
                            Function1<ExtractorLink, Unit> function14 = this.$callback;
                            String str16 = this.$fileName;
                            String str17 = this.$fileSize;
                            this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                            this.L$1 = SpillingKt.nullOutSpilledVariable(text);
                            this.L$2 = SpillingKt.nullOutSpilledVariable(attr);
                            this.L$3 = function14;
                            this.label = 1;
                            newExtractorLink$default3 = ExtractorApiKt.newExtractorLink$default("GDFlix[Direct]", "GDFlix[Direct] " + str16 + "[" + str17 + "]", attr, (ExtractorLinkType) null, new AnonymousClass1(this.$quality, null), (Continuation) this, 8, (Object) null);
                            if (newExtractorLink$default3 == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            function13 = function14;
                            str9 = text;
                            str10 = attr;
                            function13.invoke(newExtractorLink$default3);
                            return Unit.INSTANCE;
                        }
                        if (StringsKt.contains$default(text, "CLOUD DOWNLOAD [R2]", false, 2, (Object) null)) {
                            String decode = URLDecoder.decode(StringsKt.substringAfter$default(attr, "url=", (String) null, 2, (Object) null), StandardCharsets.UTF_8.toString());
                            Function1<ExtractorLink, Unit> function15 = this.$callback;
                            String str18 = this.$fileName;
                            String str19 = this.$fileSize;
                            this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                            this.L$1 = SpillingKt.nullOutSpilledVariable(text);
                            this.L$2 = SpillingKt.nullOutSpilledVariable(attr);
                            this.L$3 = SpillingKt.nullOutSpilledVariable(decode);
                            this.L$4 = function15;
                            this.label = 2;
                            newExtractorLink$default2 = ExtractorApiKt.newExtractorLink$default("GDFlix[Cloud]", "GDFlix[Cloud] " + str18 + "[" + str19 + "]", decode, (ExtractorLinkType) null, new AnonymousClass2(this.$quality, null), (Continuation) this, 8, (Object) null);
                            if (newExtractorLink$default2 == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            str7 = text;
                            function12 = function15;
                            str8 = attr;
                            function12.invoke(newExtractorLink$default2);
                            return Unit.INSTANCE;
                        }
                        if (StringsKt.contains(text, "pixeldra", true) || StringsKt.contains(text, "pixel", true)) {
                            String baseUrl = this.this$0.getBaseUrl(attr);
                            if (StringsKt.contains(attr, "download", true)) {
                                str = attr;
                            } else {
                                str = baseUrl + "/api/file/" + StringsKt.substringAfterLast$default(attr, "/", (String) null, 2, (Object) null) + "?download";
                            }
                            String str20 = str;
                            Function1<ExtractorLink, Unit> function16 = this.$callback;
                            String str21 = this.$fileName;
                            String str22 = this.$fileSize;
                            this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                            this.L$1 = SpillingKt.nullOutSpilledVariable(text);
                            this.L$2 = SpillingKt.nullOutSpilledVariable(attr);
                            this.L$3 = SpillingKt.nullOutSpilledVariable(baseUrl);
                            this.L$4 = SpillingKt.nullOutSpilledVariable(str20);
                            this.L$5 = function16;
                            this.label = 3;
                            newExtractorLink$default = ExtractorApiKt.newExtractorLink$default("GDFlix Pixeldrain", "GDFlix Pixeldrain " + str21 + "[" + str22 + "]", str20, (ExtractorLinkType) null, new AnonymousClass3(this.$quality, null), (Continuation) this, 8, (Object) null);
                            if (newExtractorLink$default == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            function1 = function16;
                            str2 = text;
                            function1.invoke(newExtractorLink$default);
                            return Unit.INSTANCE;
                        }
                        if (StringsKt.contains$default(text, "Index Links", false, 2, (Object) null)) {
                            try {
                                Requests app = MainActivityKt.getApp();
                                String str23 = this.$latestUrl;
                                this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                                this.L$1 = SpillingKt.nullOutSpilledVariable(text);
                                this.L$2 = SpillingKt.nullOutSpilledVariable(attr);
                                this.label = 4;
                                obj2 = Requests.get$default(app, str23 + attr, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, (Continuation) this, 4094, (Object) null);
                                if (obj2 == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                str3 = text;
                                r3 = attr;
                                try {
                                    this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                                    this.L$1 = SpillingKt.nullOutSpilledVariable(str3);
                                    this.L$2 = SpillingKt.nullOutSpilledVariable((Object) r3);
                                    this.label = 5;
                                    amap2 = ParCollectionsKt.amap(((NiceResponse) obj2).getDocument().select("a.btn.btn-outline-info"), new AnonymousClass4(this.$latestUrl, this.$callback, this.$fileName, this.$fileSize, this.$quality, null), (Continuation) this);
                                    if (amap2 != coroutine_suspended) {
                                        return coroutine_suspended;
                                    }
                                    str11 = str3;
                                    str12 = r3;
                                    return amap2;
                                } catch (Exception e) {
                                    e = e;
                                    str15 = str3;
                                    Log.INSTANCE.d("Index Links", e.toString());
                                    return Unit.INSTANCE;
                                }
                            } catch (Exception e2) {
                                e = e2;
                                str15 = text;
                                r3 = attr;
                                Log.INSTANCE.d("Index Links", e.toString());
                                return Unit.INSTANCE;
                            }
                        }
                        if (StringsKt.contains$default(text, "DRIVEBOT", false, 2, (Object) null)) {
                            try {
                                String substringBefore$default = StringsKt.substringBefore$default(StringsKt.substringAfter$default(attr, "id=", (String) null, 2, (Object) null), "&", (String) null, 2, (Object) null);
                                String substringBefore$default2 = StringsKt.substringBefore$default(StringsKt.substringAfter$default(attr, "do=", (String) null, 2, (Object) null), "==", (String) null, 2, (Object) null);
                                List listOf = CollectionsKt.listOf(new String[]{"https://drivebot.sbs", "https://indexbot.site"});
                                this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                                this.L$1 = SpillingKt.nullOutSpilledVariable(text);
                                this.L$2 = SpillingKt.nullOutSpilledVariable(attr);
                                this.L$3 = SpillingKt.nullOutSpilledVariable(attr);
                                this.L$4 = SpillingKt.nullOutSpilledVariable(substringBefore$default);
                                this.L$5 = SpillingKt.nullOutSpilledVariable(substringBefore$default2);
                                this.L$6 = SpillingKt.nullOutSpilledVariable(listOf);
                                this.label = 6;
                                amap = ParCollectionsKt.amap(listOf, new AnonymousClass5(substringBefore$default, substringBefore$default2, this.$callback, this.$fileName, this.$fileSize, this.$quality, null), (Continuation) this);
                                if (amap == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                str5 = text;
                                return amap;
                            } catch (Exception e3) {
                                e = e3;
                                str5 = text;
                                str6 = attr;
                                Log.INSTANCE.d("DriveBot", e.toString());
                                return Unit.INSTANCE;
                            }
                        }
                        if (!StringsKt.contains$default(text, "GoFile", false, 2, (Object) null)) {
                            Log.INSTANCE.d("Error", "No Server matched");
                            return Unit.INSTANCE;
                        }
                        try {
                            this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                            this.L$1 = SpillingKt.nullOutSpilledVariable(text);
                            this.L$2 = SpillingKt.nullOutSpilledVariable(attr);
                            this.label = 7;
                            obj3 = Requests.get$default(MainActivityKt.getApp(), attr, (Map) null, (String) null, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, (Continuation) this, 4094, (Object) null);
                            if (obj3 == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            str4 = text;
                            r3 = attr;
                            try {
                                this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                                this.L$1 = SpillingKt.nullOutSpilledVariable(str4);
                                this.L$2 = SpillingKt.nullOutSpilledVariable((Object) r3);
                                this.label = 8;
                                amap3 = ParCollectionsKt.amap(((NiceResponse) obj3).getDocument().select(".row .row a"), new AnonymousClass6(this.$subtitleCallback, this.$callback, null), (Continuation) this);
                                str13 = r3;
                                if (amap3 == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                return amap3;
                            } catch (Exception e4) {
                                e = e4;
                                str14 = str4;
                                Log.INSTANCE.d("Gofile", e.toString());
                                return Unit.INSTANCE;
                            }
                        } catch (Exception e5) {
                            e = e5;
                            str14 = text;
                            r3 = attr;
                            Log.INSTANCE.d("Gofile", e.toString());
                            return Unit.INSTANCE;
                        }
                    case 1:
                        function13 = (Function1) this.L$3;
                        str10 = (String) this.L$2;
                        str9 = (String) this.L$1;
                        ResultKt.throwOnFailure(obj);
                        newExtractorLink$default3 = obj;
                        function13.invoke(newExtractorLink$default3);
                        return Unit.INSTANCE;
                    case 2:
                        function12 = (Function1) this.L$4;
                        str8 = (String) this.L$2;
                        String str24 = (String) this.L$1;
                        ResultKt.throwOnFailure(obj);
                        str7 = str24;
                        newExtractorLink$default2 = obj;
                        function12.invoke(newExtractorLink$default2);
                        return Unit.INSTANCE;
                    case 3:
                        function1 = (Function1) this.L$5;
                        str2 = (String) this.L$1;
                        ResultKt.throwOnFailure(obj);
                        newExtractorLink$default = obj;
                        function1.invoke(newExtractorLink$default);
                        return Unit.INSTANCE;
                    case 4:
                        String str25 = (String) this.L$2;
                        String str26 = (String) this.L$1;
                        ResultKt.throwOnFailure(obj);
                        str3 = str26;
                        obj2 = obj;
                        r3 = str25;
                        this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                        this.L$1 = SpillingKt.nullOutSpilledVariable(str3);
                        this.L$2 = SpillingKt.nullOutSpilledVariable((Object) r3);
                        this.label = 5;
                        amap2 = ParCollectionsKt.amap(((NiceResponse) obj2).getDocument().select("a.btn.btn-outline-info"), new AnonymousClass4(this.$latestUrl, this.$callback, this.$fileName, this.$fileSize, this.$quality, null), (Continuation) this);
                        if (amap2 != coroutine_suspended) {
                        }
                        break;
                    case 5:
                        String str27 = (String) this.L$2;
                        str11 = (String) this.L$1;
                        ResultKt.throwOnFailure(obj);
                        amap2 = obj;
                        str12 = str27;
                        return amap2;
                    case 6:
                        str6 = (String) this.L$2;
                        str5 = (String) this.L$1;
                        try {
                            ResultKt.throwOnFailure(obj);
                            amap = obj;
                            return amap;
                        } catch (Exception e6) {
                            e = e6;
                            Log.INSTANCE.d("DriveBot", e.toString());
                            return Unit.INSTANCE;
                        }
                    case 7:
                        String str28 = (String) this.L$2;
                        String str29 = (String) this.L$1;
                        ResultKt.throwOnFailure(obj);
                        str4 = str29;
                        obj3 = obj;
                        r3 = str28;
                        this.L$0 = SpillingKt.nullOutSpilledVariable(element);
                        this.L$1 = SpillingKt.nullOutSpilledVariable(str4);
                        this.L$2 = SpillingKt.nullOutSpilledVariable((Object) r3);
                        this.label = 8;
                        amap3 = ParCollectionsKt.amap(((NiceResponse) obj3).getDocument().select(".row .row a"), new AnonymousClass6(this.$subtitleCallback, this.$callback, null), (Continuation) this);
                        str13 = r3;
                        if (amap3 == coroutine_suspended) {
                        }
                        return amap3;
                    case 8:
                        String str30 = (String) this.L$2;
                        String str31 = (String) this.L$1;
                        ResultKt.throwOnFailure(obj);
                        str4 = str31;
                        amap3 = obj;
                        str13 = str30;
                        return amap3;
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            } catch (Exception e7) {
                e = e7;
            }
        } catch (Exception e8) {
            e = e8;
        }
    }
}
