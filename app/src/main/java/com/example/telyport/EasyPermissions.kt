package com.example.telyport

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.ArrayList
import java.util.HashMap

object EasyPermissions {
    private const val PERMISSION_CODE = 1212
    private var mActivity: Activity? = null
    private var mCustomPermission: List<String>? = null
    private var mPerpermissionListener: PermissionListener? = null

    fun checkAndRequestPermission(
        activity: Activity,
        permissions: List<String>,
        permissionListener: PermissionListener
    ): Boolean {
        mActivity = activity
        mPerpermissionListener = permissionListener
        mCustomPermission = permissions
        if (Build.VERSION.SDK_INT >= 23) {
            val listPermissionsAssign: MutableList<String> =
                ArrayList()
            for (per in permissions) {
                if (ContextCompat.checkSelfPermission(
                        activity.applicationContext,
                        per
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    listPermissionsAssign.add(per)
                }
            }
            if (!listPermissionsAssign.isEmpty()) {
                ActivityCompat.requestPermissions(
                    activity,
                    listPermissionsAssign.toTypedArray(),
                    PERMISSION_CODE
                )
                return false
            }
        }
        return true
    }


    interface PermissionListener {
        fun onPermissionGranted(mCustomPermission: List<String>?)
        fun onPermissionDenied(mCustomPermission: List<String>?)
    }

        fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                val listPermissionsNeeded =
                    mCustomPermission
                val perms: MutableMap<String, Int> =
                    HashMap()
                for (permission in listPermissionsNeeded!!) {
                    perms[permission] = PackageManager.PERMISSION_GRANTED
                }
                if (grantResults.size > 0) {
                    var i = 0
                    while (i < permissions.size) {
                        perms[permissions[i]] = grantResults[i]
                        i++
                    }
                    var isAllGranted = true
                    for (permission in listPermissionsNeeded) {
                        if (perms[permission] == PackageManager.PERMISSION_DENIED) {
                            isAllGranted = false
                            break
                        }
                    }
                    if (isAllGranted) {
                        mPerpermissionListener!!.onPermissionGranted(mCustomPermission)
                    } else {
                        var shouldRequest = false
                        for (permission in listPermissionsNeeded) {
                            if (ActivityCompat
                                    .shouldShowRequestPermissionRationale(
                                        mActivity!!,
                                        permission
                                    )
                            ) {
                                shouldRequest = true
                                break
                            }
                        }
                        if (shouldRequest) {
                            ifCancelledAndCanRequest(mActivity)
                        } else {

                            ifCancelledAndCannotRequest(mActivity)
                        }
                    }
                }
            }
        }
    }

    private fun ifCancelledAndCanRequest(activity: Activity?) {
        showDialogOK(
            activity,
            "Permission required for this app, please grant all permission .",
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermission(
                        activity!!,
                        mCustomPermission!!,
                        mPerpermissionListener!!
                    )
                    DialogInterface.BUTTON_NEGATIVE -> mPerpermissionListener!!.onPermissionDenied(
                        mCustomPermission
                    )
                }
            })
    }

    private fun ifCancelledAndCannotRequest(activity: Activity?) {
        showDialogOK(
            activity,
            """
                You have forcefully denied some of the required permissions
                for this action. Please go to permissions and allow them.
                """.trimIndent(),
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            activity!!.packageName,
                            null
                        )
                        intent.data = uri
                        activity.startActivity(intent)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> mPerpermissionListener!!.onPermissionDenied(
                        mCustomPermission
                    )
                }
            })
    }

    private fun showDialogOK(
        activity: Activity?,
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(activity).setMessage(message)
            .setPositiveButton("OK", okListener).setNegativeButton("Cancel", okListener).create()
            .show()
    }
}