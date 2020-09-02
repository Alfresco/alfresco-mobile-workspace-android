package com.alfresco.content.viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class InputStreamImageDecoder implements ImageDecoder {

    public static class Factory implements DecoderFactory<InputStreamImageDecoder> {

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
        public InputStreamImageDecoder make()
                throws IllegalAccessException, InstantiationException, NoSuchMethodException,
                        InvocationTargetException {
            return new InputStreamImageDecoder(inputStream, bitmapConfig);
        }
    }

    private final InputStream inputStream;
    private final Config bitmapConfig;

    private InputStreamImageDecoder(InputStream inputStream, Config bitmapConfig) {
        this.inputStream = inputStream;
        if (bitmapConfig == null) {
            this.bitmapConfig = Config.RGB_565;
        } else {
            this.bitmapConfig = bitmapConfig;
        }
    }

    @Override
    public Bitmap decode(Context context, Uri uri) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = bitmapConfig;
        return BitmapFactory.decodeStream(inputStream, null, options);
    }
}
