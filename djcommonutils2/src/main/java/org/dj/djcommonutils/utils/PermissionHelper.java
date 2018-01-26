package org.dj.djcommonutils.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.dj.djcommonutils.R;

import java.util.Arrays;
import java.util.List;

/**
 * 作者：DuJianBo on 2017/4/10 10:49
 * 邮箱：jianbo_du@foxmail.com
 */

public class PermissionHelper {
    private static final int REQUEST_PERMISSION_CODE = 1000;

    private Context mContext;

    private PermissionListener mListener;

    private List<String> mPermissionList;

    public PermissionHelper(@NonNull Context context){
        this.mContext = context;
    }


    /**
     * 权限授权申请
     * @param hintMessage
     *              要申请的权限的提示
     *
     * @param permissions
     *              要申请的权限
     *
     * @param listener
     *              申请成功之后的callback
     */
    public void requestPermissions(@NonNull CharSequence hintMessage,
                                   @Nullable PermissionListener listener,
                                   @NonNull final String... permissions){

        if(listener != null){
            mListener = listener;
        }

        mPermissionList = Arrays.asList(permissions);

        //没全部权限
        if (!hasPermissions(permissions)) {

            //需要向用户解释为什么申请这个权限
            boolean shouldShowRationale = false;
            for (String perm : permissions) {
                shouldShowRationale =
                        shouldShowRationale || shouldShowRequestPermissionRationale(mContext, perm);
            }

            if (shouldShowRationale) {
                showMessageOKCancel(hintMessage, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executePermissionsRequest(mContext, permissions,
                                REQUEST_PERMISSION_CODE);

                    }
                });
            } else {
                executePermissionsRequest(mContext, permissions,
                        REQUEST_PERMISSION_CODE);
            }
        } else if(mListener != null) { //有全部权限
            mListener.doAfterGrand(permissions);
        }
    }

    /**
     * 处理onRequestPermissionsResult
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void handleRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                boolean allGranted = true;
                for (int grant: grantResults) {
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted && mListener != null) {

                    mListener.doAfterGrand((String[])mPermissionList.toArray());

                } else if(!allGranted && mListener != null){
                    mListener.doAfterDenied((String[])mPermissionList.toArray());
                }
                break;
        }
    }

    /**
     * 判断是否具有某权限
     * @param perms
     * @return
     */
    private boolean hasPermissions(@NonNull String... perms) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(mContext, perm) ==
                    PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }

        return true;
    }


    /**
     * @param context
     * @param perm
     * @return
     */
    @TargetApi(23)
    private static boolean shouldShowRequestPermissionRationale(@NonNull Context context, @NonNull String perm) {
        return ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, perm);
    }

    /**
     * @param context
     * @param perms
     * @param requestCode
     */
    @TargetApi(23)
    private void executePermissionsRequest(@NonNull Context context, @NonNull String[] perms, int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, perms, requestCode);
    }

    private void showMessageOKCancel(CharSequence message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton(R.string.confirm, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    public interface PermissionListener {

        void doAfterGrand(String... permission);

        void doAfterDenied(String... permission);
    }
}
