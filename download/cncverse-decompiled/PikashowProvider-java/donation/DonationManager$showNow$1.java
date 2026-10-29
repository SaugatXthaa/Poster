package com.cncverse.donation;

import androidx.appcompat.app.AppCompatActivity;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;

/* compiled from: DonationManager.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 3, 0}, xi = 48)
@DebugMetadata(c = "com.cncverse.donation.DonationManager$showNow$1", f = "DonationManager.kt", i = {1}, l = {110, 111}, m = "invokeSuspend", n = {"config"}, nl = {111, 114}, s = {"L$0"}, v = 2)
/* loaded from: classes.dex */
final class DonationManager$showNow$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ AppCompatActivity $activity;
    final /* synthetic */ String $providerName;
    Object L$0;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: DonationManager.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 3, 0}, xi = 48)
    @DebugMetadata(c = "com.cncverse.donation.DonationManager$showNow$1$1", f = "DonationManager.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, nl = {}, s = {}, v = 2)
    /* renamed from: com.cncverse.donation.DonationManager$showNow$1$1, reason: invalid class name */
    /* loaded from: classes.dex */
    public static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ AppCompatActivity $activity;
        final /* synthetic */ DonationConfig $config;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(AppCompatActivity appCompatActivity, DonationConfig donationConfig, Continuation<? super AnonymousClass1> continuation) {
            super(2, continuation);
            this.$activity = appCompatActivity;
            this.$config = donationConfig;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return new AnonymousClass1(this.$activity, this.$config, continuation);
        }

        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    DonationManager.INSTANCE.showDialog(this.$activity, this.$config);
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DonationManager$showNow$1(AppCompatActivity appCompatActivity, String str, Continuation<? super DonationManager$showNow$1> continuation) {
        super(2, continuation);
        this.$activity = appCompatActivity;
        this.$providerName = str;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new DonationManager$showNow$1(this.$activity, this.$providerName, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:2:0x0006. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:12:0x0058 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0059  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        Object buildConfig;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                this.label = 1;
                buildConfig = DonationManager.INSTANCE.buildConfig(this.$activity, this.$providerName, (Continuation) this);
                if (buildConfig == coroutine_suspended) {
                    return coroutine_suspended;
                }
                DonationConfig donationConfig = (DonationConfig) buildConfig;
                this.L$0 = SpillingKt.nullOutSpilledVariable(donationConfig);
                this.label = 2;
                return BuildersKt.withContext(Dispatchers.getMain(), new AnonymousClass1(this.$activity, donationConfig, null), (Continuation) this) != coroutine_suspended ? coroutine_suspended : Unit.INSTANCE;
            case 1:
                ResultKt.throwOnFailure(obj);
                buildConfig = obj;
                DonationConfig donationConfig2 = (DonationConfig) buildConfig;
                this.L$0 = SpillingKt.nullOutSpilledVariable(donationConfig2);
                this.label = 2;
                if (BuildersKt.withContext(Dispatchers.getMain(), new AnonymousClass1(this.$activity, donationConfig2, null), (Continuation) this) != coroutine_suspended) {
                }
                break;
            case 2:
                ResultKt.throwOnFailure(obj);
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
