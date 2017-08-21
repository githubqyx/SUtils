package com.liyi.sutils.utils.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * The permission administration tool class
 */

public class SPermissionUtil {
    private Activity mActivity;
    private static int mRequestCode;
    private String[] mPermissions;
    private static OnPermissionListener mListener;
    private static boolean isAutoShowTip;

    private SPermissionUtil(@NonNull Activity activity) {
        this.mActivity = activity;
    }

    private static boolean isNeedRequest() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Determine if you have certain permissions
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean hasPermissions(@NonNull Context context, @NonNull String... permissions) {
        if (isNeedRequest()) {
            for (String p : permissions) {
                if (!(ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED))
                    return false;
            }
            return true;
        }
        return true;
    }

    /**
     * Gets a missing list of permissions
     *
     * @param context
     * @param permissions
     * @return
     */
    public static String[] getDeniedPermissions(@NonNull Context context, @NonNull String... permissions) {
        ArrayList<String> permissionList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String p : permissions) {
                if (ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_DENIED) {
                    permissionList.add(p);
                }
            }
        }
        return permissionList.toArray(new String[permissionList.size()]);
    }

    /**
     * Whether or not a certain authority has been completely rejected
     */
    public static boolean hasAlwaysDeniedPermission(@NonNull Activity activity, @NonNull String... deniedPermissions) {
        if (isNeedRequest()) {
            for (String permission : deniedPermissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public static void showTipDialog(@NonNull final Context context) {
        if (isNeedRequest()) {
            new AlertDialog.Builder(context)
                    .setTitle("提示信息")
                    .setMessage("当前应用缺少必要权限，可能无法正常使用所有功能。请单击【确定】按钮前往设置中心进行权限授权")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startAppSettings(context);
                        }
                    }).show();
        }
    }

    /**
     * Start the current application Settings page
     */
    public static void startAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    public static SPermissionUtil with(@NonNull Activity activity) {
        return new SPermissionUtil(activity);
    }

    public static SPermissionUtil with(@NonNull Fragment fragment) {
        return new SPermissionUtil(fragment.getActivity());
    }

    public static SPermissionUtil with(@NonNull android.app.Fragment fragment) {
        return new SPermissionUtil(fragment.getActivity());
    }

    public SPermissionUtil requestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    public SPermissionUtil permissions(@NonNull String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    public SPermissionUtil callback(OnPermissionListener callback) {
        this.mListener = callback;
        return this;
    }

    public SPermissionUtil autoShowTip(boolean isAutoShowTip) {
        this.isAutoShowTip = isAutoShowTip;
        return this;
    }

    public void execute() {
        if (isNeedRequest()) {
            String[] deniedPermissions = getDeniedPermissions(mActivity, mPermissions);
            if (deniedPermissions.length > 0) {
                ActivityCompat.requestPermissions(mActivity, deniedPermissions, mRequestCode);
            } else {
                if (mListener != null) {
                    mListener.onPermissionGranted(mRequestCode, mPermissions);
                }
            }
        }
    }

    /**
     * Handle the result of the request permission returned
     */
    public static void handleRequestPermissionsResult(@NonNull Activity activity, int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (requestCode == mRequestCode) {
            if (mListener != null) {
                ArrayList<String> deniedPermissions = new ArrayList<String>();
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                    }
                }
                if (deniedPermissions.size() <= 0) {
                    mListener.onPermissionGranted(requestCode, permissions);
                } else {
                    String[] perms = deniedPermissions.toArray(new String[deniedPermissions.size()]);
                    boolean hasAlwaysDenied = hasAlwaysDeniedPermission(activity, perms);
                    if (isAutoShowTip && hasAlwaysDenied) {
                        showTipDialog(activity);
                    }
                    mListener.onPermissionDenied(requestCode, perms, hasAlwaysDenied);
                }
            }
        }
    }

    public interface OnPermissionListener extends ActivityCompat.OnRequestPermissionsResultCallback {
        /**
         * User agrees to authorize
         */
        void onPermissionGranted(int requestCode, String[] grantPermissions);

        /**
         * User reject authorization
         */
        void onPermissionDenied(int requestCode, String[] deniedPermissions, boolean hasAlwaysDenied);
    }
}