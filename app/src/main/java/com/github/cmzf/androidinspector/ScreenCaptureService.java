package com.github.cmzf.androidinspector;

// https://github.com/mtsahakis/MediaProjectionDemo

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;


public class ScreenCaptureService {

    private static final String TAG = ScreenCaptureService.class.getCanonicalName();
    private static ScreenCaptureService sInstance;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;
    private Activity mAppActivity;
    private byte[] mScreenImage;
    private int mRequestCode;

    public static ScreenCaptureService getInstance() {
        if (sInstance == null) {
            sInstance = new ScreenCaptureService();
        }
        return sInstance;
    }

    public void startProjection(Activity activity, int requestCode) {
        mAppActivity = activity;
        mRequestCode = requestCode;

        if (mProjectionManager != null) {
            return;
        }

        // call for the projection manager
        mProjectionManager = (MediaProjectionManager) mAppActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();

        mAppActivity.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), mRequestCode);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mRequestCode) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

            if (mMediaProjection != null) {
                // display metrics
                DisplayMetrics metrics = mAppActivity.getResources().getDisplayMetrics();
                mDensity = metrics.densityDpi;
                mDisplay = mAppActivity.getWindowManager().getDefaultDisplay();

                // create virtual display depending on device width / height
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new OrientationChangeCallback(mAppActivity);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // register media projection stop callback
                mMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
            }
        }
    }

    public void stopProjection() {
        mHandler.post(() -> {
            if (mMediaProjection != null) {
                mMediaProjection.stop();
            }
        });
    }

    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        mDisplay.getRealSize(size);
        mWidth = size.x;
        mHeight = size.y;

        System.out.println("mWidth:" + mWidth + ", mHeight:" + mHeight);

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, mWidth, mHeight, mDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
    }

    public byte[] getScreenImage() {
        return mScreenImage;
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * mWidth;

                // create bitmap
                Bitmap bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                // trim black border
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, mWidth, mHeight);

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteStream);
                mScreenImage = byteStream.toByteArray();
                bitmap.recycle();
            }

            if (image != null) {
                image.close();
            }
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCaptureService", "stopping projection.");
            mHandler.post(() -> {
                if (mVirtualDisplay != null) mVirtualDisplay.release();
                if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
            });
        }
    }
}