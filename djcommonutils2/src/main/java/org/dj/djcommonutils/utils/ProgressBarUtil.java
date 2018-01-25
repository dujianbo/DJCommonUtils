package org.dj.djcommonutils.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.WindowManager.BadTokenException;

public class ProgressBarUtil {

    private static ProgressDialog progressDialog;

    /**
     * 显示正在加载的进度条
     */
    public static void showProgressDialog(Context context) {
        dismissProgressDialog();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("拼命加载中...请稍后...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        try {
            progressDialog.show();
        } catch (BadTokenException exception) {
            exception.printStackTrace();
        }
    }

    public static void showProgressDialog(Context context, String msg) {
        dismissProgressDialog();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        try {
            progressDialog.show();
        } catch (BadTokenException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 隐藏正在加载的进度条
     */
    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            if(progressDialog.isShowing()) {
                try {
                    progressDialog.dismiss();
                } catch (IllegalArgumentException exception) {
                    exception.printStackTrace();
                }
            }
            progressDialog = null;
        }
    }
}
