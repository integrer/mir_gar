package org.mirgar.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Contract;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by n.bibik on 10.06.2018.
 */

public class BitmapManufacture extends BitmapFactory {
    private BitmapManufacture() {}

    private final static int COMPRESS_QUALITY = 85;

    public final static class Rect {
        private float width;
        private float height;

        public void set(float width, float height) {
            this.width = width;
            this.height = height;
        }

        @Contract(pure = true)
        public float getWidth() { return width; }

        @Contract(pure = true)
        public float getHeight() { return height; }

        public Rect(float width, float height) {
            set(width, height);
        }

        public Rect(Rect otherRect) {
            this(otherRect.width, otherRect.height);
        }

        public Rect() {
            this(0, 0);
        }
    }

    @Nullable
    public static Bitmap loadMinBitmap(final File src, final File dst, final Rect req) {
        if (dst.exists())
            return dst.isFile() ?
                    decodeFile(dst.getAbsolutePath()) :
                    null;
        else {

            Bitmap scaled = makeMinBitmap(src, req);
            scaled = Bitmap.createScaledBitmap(scaled, 600, (int)(scaled.getHeight() * (600.0 / scaled.getWidth())), true);

            try {
                if (!dst.exists())
                    if (dst.createNewFile()) {

                        FileOutputStream out = null;

                        try {
                            out = new FileOutputStream(dst);
                            scaled.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out);
                        } catch (Exception ex) {
                            Logger.wtf(BitmapManufacture.class, "Error on writing min picture", ex);
                        } finally {
                            try {
                                if (out != null)
                                    out.close();
                            } catch (IOException ex) {
                                Logger.wtf(BitmapManufacture.class, "Error on closing min picture output stream", ex);
                            }
                        }

                        scaled = decodeFile(dst.getAbsolutePath());

                    } else throw new IOException();
            } catch (IOException ex) {
                Logger.wtf(BitmapManufacture.class, "Error on creating min picture", ex);
            }

            return scaled;
        }
    }

    public static Bitmap makeMinBitmap (File src, Rect req) {
        String srcPath = src.getAbsolutePath();

        // Get the dimensions of the bitmap
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        decodeFile(srcPath, options);

        ajustOptions(options, req);

        return decodeFile(srcPath, options);
    }

    @Nullable
    public static Bitmap makeMinBitmap (Bitmap src, File dst, Rect req) {
        // Get the dimensions of the bitmap
        final Options options = new Options();
        try {
            options.inJustDecodeBounds = true;
            options.outWidth = src.getWidth();
            options.outHeight = src.getHeight();

            ajustOptions(options, req);

            OutputStream oStream = new FileOutputStream(dst);

            src.compress(Bitmap.CompressFormat.JPEG, 85, oStream);
            InputStream iStream = new FileInputStream(dst);
            //src.recycle();

            return decodeStream(iStream, null, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Deprecated
    public static Bitmap makeMinBitmap (Resources res, int resId, @Nullable Rect req)  {
        Options options = null;

        if(req != null) {
            options = new Options();
            options.inJustDecodeBounds = true;
            decodeResource(res, resId, options);

            ajustOptions(options, req);
        }

        return decodeResource(res, resId, options);
    }

    public static InputStream prepareImage(Bitmap src, File dst, short type) {
        try {
            Bitmap res = null;
            Logger.v(BitmapManufacture.class, String.format("1 res: %b", res));
            if (type == 2 || type == 3) {
                Rect start = new Rect();
                int size = Math.min(src.getHeight(), src.getWidth());
                if (size == src.getHeight()) {
                    start.height = 0;
                    start.width = (src.getWidth() - size) / 2;
                } else {
                    start.height = (src.getHeight() - size) / 2;
                    start.width = 0;
                }
                res = Bitmap.createBitmap(src, (int) start.width, (int) start.height, size, size);
                Logger.v(BitmapManufacture.class, String.format("2 res: %b", res));
                if(type == 2) {
                    res = makeMinBitmap(res, dst, new Rect(150, 150));
                    Logger.v(BitmapManufacture.class, String.format("3 res: %b", res));
                }
                else res = makeMinBitmap(res, dst, new Rect(56, 56));
            } else if (type == 1) {
                res = makeMinBitmap(src, dst, new Rect(600, 0));
                Logger.v(BitmapManufacture.class, String.format("4 res: %b", res));
            } else res = Bitmap.createBitmap(src);
            OutputStream outputStream = new org.apache.commons.io.output.ByteArrayOutputStream();
            InputStream inputStream = new ByteArrayInputStream(new byte[1024]);
            Logger.v(BitmapManufacture.class, String.format("5 res: %b", res));
            res.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            IOUtils.copy(inputStream, outputStream);
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void prepareImage(File src, File dst, short type) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(src);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        prepareImage(decodeStream(is), dst, type);
    }


        public static Bitmap makeBitmapFromDrawable (Resources res, @DrawableRes int drawableId, Resources.Theme theme) {
        Drawable source;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            source = res.getDrawable(drawableId, theme);
        else
            source = VectorDrawableCompat.create(res, drawableId, theme);

        if(source instanceof BitmapDrawable) {
            Bitmap sourceBitmap = ((BitmapDrawable) source).getBitmap();
            if (sourceBitmap != null)
                return sourceBitmap;
        }

        Bitmap result;
        if(source.getIntrinsicWidth() <= 0 || source.getIntrinsicHeight() <= 0) {
            result = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            result = Bitmap.createBitmap(source.getIntrinsicWidth(), source.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        source.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        source.draw(canvas);

        return result;
    }


    private static void ajustOptions (final Options options, Rect req) {
        options.inSampleSize = calculateSampleSize(options, req);//options.outWidth / 600;

        // Decode the image file into a Bitmap sized to fill the View
        options.inJustDecodeBounds = false;
//        options.inPurgeable = true;
    }

    private static int calculateSampleSize(Options options, Rect _order) {
        // Raw height and width of image
        Rect fact = new Rect(options.outWidth, options.outHeight);
        Rect order = new Rect(_order);
        if(order.width == 0) order.width = fact.width;
        if(order.height == 0) order.height = fact.height;

        int inSampleSize = 1;

        if (fact.height > order.height || fact.width > order.width) {

            final Rect half = new Rect(fact.width / inSampleSize + 1, fact.height / inSampleSize + 1);

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (half.height / inSampleSize >= order.height
                    && half.width / inSampleSize >= order.width) {
                inSampleSize++;
            }
        }

        return inSampleSize;

    }

}
