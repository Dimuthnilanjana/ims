package com.example.volte

import android.content.Context
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.PersistableBundle
import android.telephony.SubscriptionManager
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

object VolteApplier {
    fun apply(ctx: Context): String {
        try {
            if (!Shizuku.pingBinder()) return "Shizuku is not running. Start it first."
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                return "Shizuku permission not granted."
            if (ctx.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return "Phone permission not granted."
            HiddenApiBypass.addHiddenApiExemptions("L")
            val subs = ctx.getSystemService(SubscriptionManager::class.java)
                .activeSubscriptionInfoList
            if (subs.isNullOrEmpty()) return "No active SIM found."

            val binder = ShizukuBinderWrapper(SystemServiceHelper.getSystemService("carrier_config"))
            val stub = Class.forName("com.android.internal.telephony.ICarrierConfigLoader\$Stub")
            val loader = stub.getMethod("asInterface", IBinder::class.java).invoke(null, binder)
            val iface = Class.forName("com.android.internal.telephony.ICarrierConfigLoader")
            val method = iface.getMethod(
                "overrideConfig", Int::class.javaPrimitiveType,
                PersistableBundle::class.java, Boolean::class.javaPrimitiveType
            )
            val b = PersistableBundle().apply {
                putBoolean("carrier_volte_available_bool", true)
                putBoolean("carrier_volte_provisioning_required_bool", false)
                putBoolean("carrier_wfc_ims_available_bool", true)
                putBoolean("carrier_vt_available_bool", true)
                putBoolean("editable_enhanced_4g_lte_bool", true)
                putBoolean("hide_enhanced_4g_lte_bool", false)
                putBoolean("vonr_enabled_bool", true)
            }
            for (s in subs) method.invoke(loader, s.subscriptionId, b, true)
            return "VoLTE override applied to ${subs.size} SIM(s). Toggle mobile data/airplane mode if it doesn't register."
        } catch (t: Throwable) {
            return "Failed: ${t.cause ?: t}"
        }
    }
}
