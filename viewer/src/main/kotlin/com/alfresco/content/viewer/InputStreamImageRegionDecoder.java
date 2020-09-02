package com.alfresco.content.viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Default implementation of {@link ImageRegionDecoder} using Android's {@link BitmapRegionDecoder},
 * based on the Skia library. This works well in most circumstances and has reasonable performance
 * due to the cached decoder instance, however it has some problems with grayscale, indexed and CMYK
 * images.
 */
public class InputStreamImageRegionDecoder implements ImageRegionDecoder {

    public static class Factory implements DecoderFactory<InputStreamImageRegionDecoder> {

        private final InputStream inputStream;
        private final Config bitmapConfig;

        public Factory(InputStream inputStream) {
            this(inputStream, null);
        }

        public Factory(InputStream inputStream, Config bitmapConfig) {
            this.inputStream = inputStream;
            this.bitmapConfig = bitmapConfig;
        }

        @Override
        public InputStreamImageRegionDecoder make()
                throws IllegalAccessException, InstantiationException, NoSuchMethodException,
                        InvocationTargetException {
            return new InputStreamImageRegionDecoder(inputStream, bitmapConfig);
        }
    }

    private BitmapRegionDecoder decoder;
    private final Object decoderLock = new Object();

    private final InputStream inputStream;
    private final Config bitmapConfig;

    private InputStreamImageRegionDecoder(InputStream inputStream, Config bitmapConfig) {
        this.inputStream = inputStream;
        if (bitmapConfig == null) {
            this.bitmapConfig = Config.RGB_565;
        } else {
            this.bitmapConfig = bitmapConfig;
        }
    }

    @Override
    public Point init(Context context, Uri uri) throws Exception {
        decoder = BitmapRegionDecoder.newInstance(inputStream, false);
        return new Point(decoder.getWidth(), decoder.getHeight());
    }

    @Override
    public Bitmap decodeRegion(Rect sRect, int sampleSize) {
        synchronized (decoderLock) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = bitmapConfig;
            Bitmap bitmap = decoder.decodeRegion(sRect, options);
            if (bitmap == null) {
                throw new RuntimeException(
                        "Skia image decoder returned null bitmap - image format may not be supported");
            }
            return bitmap;
        }
    }

    @Override
    public boolean isReady() {
        return decoder != null && !decoder.isRecycled();
    }

    @Override
    public void recycle() {
        decoder.recycle();
    }
}
