package org.dj.djcommonutils.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.dj.djcommonutils.R;
import org.dj.djcommonutils.global.ConstantStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    private BitmapUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 保存图片到文件
     * @param context
     * @param fileName
     * @param bitmap
     * @return
     */
    public static File saveBitMapToFile(Context context, String fileName, Bitmap bitmap) {
        FileOutputStream fOut = null;
        try {
            File file = null;
            String fileDstPath = "";
            if (SDCardUtils.isSDCardEnable()) {
                // 保存到sd卡
                fileDstPath = SDCardUtils.getSDCardPath() + "tempFile" + File.separator + fileName;

                File homeDir = new File(SDCardUtils.getSDCardPath() + "tempFile" + File.separator);
                if (!homeDir.exists()) {
                    homeDir.mkdirs();
                }
            } else {
                // 保存到file目录
                fileDstPath = context.getFilesDir().getAbsolutePath()
                        + File.separator + "tempFile" + File.separator + fileName;

                File homeDir = new File(context.getFilesDir().getAbsolutePath()
                        + File.separator + "tempFile" + File.separator);
                if (!homeDir.exists()) {
                    homeDir.mkdir();
                }
            }

            file = new File(fileDstPath);

            // 简单起见，先删除老文件，不管它是否存在。
            file.delete();
            fOut = new FileOutputStream(file);
            if (fileName.endsWith(".jpg")) {
                bitmap.compress(CompressFormat.JPEG, 75, fOut);
            } else {
                bitmap.compress(CompressFormat.PNG, 100, fOut);
            }
            fOut.flush();

            Log.i("FileSave", "saveDrawableToFile " + fileName
                    + " success, save path is " + fileDstPath);
            return file;
        } catch (Exception e) {
            Log.e("FileSave", "saveDrawableToFile: " + fileName + " , error", e);
            return null;
        } finally {
            if(null != fOut) {
                try {
                    fOut.close();
                } catch (Exception e) {
                    Log.e("FileSave", "saveDrawableToFile, close error", e);
                }
            }
        }
    }

    /**
     * 质量+尺寸压缩
     * @param context
     * @param image
     * @return
     */
    public static Bitmap comp(Context context,Bitmap image) {
        return comp(context, image, 256);
    }

    public static Bitmap comp(Context context,Bitmap image, int size) {
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream isBm = null;
        try {
            baos = new ByteArrayOutputStream();
            image.compress(CompressFormat.JPEG, 100, baos);
            int options = 100;
            while (baos.toByteArray().length / 1024 > size) { // 循环判断如果压缩后图片是否大于256kb,大于继续压缩
                baos.reset();// 重置baos即清空baos
                options -= 10;// 每次都减少10
                image.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
                if(options <= 0) {
                    break;
                }
            }
            image.recycle();
            isBm = new ByteArrayInputStream(baos.toByteArray());
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            // 开始读入图片，此时把options.inJustDecodeBounds 设true
            newOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(isBm, null, newOpts);
            isBm.close();
            Bitmap bitmap = null;

            //原始图片的宽度与1080f的比值，然后向上取整
            int wRatio = (int) Math.ceil(newOpts.outWidth / ConstantStatus.IMAGE_DEFAULT_WIDTH);
            //原始图片的高度与1920f的比值，然后向上取整
            int hRatio = (int) Math.ceil(newOpts.outHeight / ConstantStatus.IMAGE_DEFAULT_HEIGHT);
            //获取采样率
            newOpts.inSampleSize = 1;
            if (wRatio > 1 && hRatio > 1) {
                if (wRatio > hRatio) {
                    newOpts.inSampleSize = wRatio;
                } else {
                    newOpts.inSampleSize = hRatio;
                }
            }
            newOpts.inPreferredConfig = Config.RGB_565;// 降低图片从ARGB888到RGB565

            // 重新读入图片，注意此时把options.inJustDecodeBounds 设回false
            isBm = new ByteArrayInputStream(baos.toByteArray());
            newOpts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) baos.close();
            } catch (IOException e) {}
            try {
                if (isBm != null) isBm.close();
            } catch (IOException e) {}
        }
        //  此处为修改  万一错误随时删除 现在处理为如果出现异常传app图标进行缓冲处理
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.home_failed_default);
    }

    /**
     * 质量压缩
     *
     * @param image
     * @return
     */
    private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 256) { // 循环判断如果压缩后图片是否大于256kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            image.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            if(options <= 0) {
                break;
            }
        }
        image.recycle();
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null);
    }
}