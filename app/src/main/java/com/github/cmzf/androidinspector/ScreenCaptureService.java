package com.github.cmzf.androidinspector;

// https://github.com/mtsahakis/MediaProjectionDemo

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;


public class ScreenCaptureService {

    private static final String TAG = ScreenCaptureService.class.getCanonicalName();
    private static ScreenCaptureService sInstance;
    private final Object mScreenBitmapLock = new Object();
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private OrientationChangeCallback mOrientationChangeCallback;
    private int mRequestCode;
    private DisplayMetrics mMetrics = new DisplayMetrics();
    private Bitmap mScreenBitmap;

    public static ScreenCaptureService getInstance() {
        if (sInstance == null) {
            sInstance = new ScreenCaptureService();
        }
        return sInstance;
    }

    public void startProjection(int requestCode) {
        mRequestCode = requestCode;

        if (mProjectionManager != null) {
            return;
        }

        // call for the projection manager
        mProjectionManager = (MediaProjectionManager) Global.getMainActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        Global.getMainActivity().startActivityForResult(mProjectionManager.createScreenCaptureIntent(), mRequestCode);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mRequestCode) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

            if (mMediaProjection != null) {
                mDisplay = Global.getMainActivity().getWindowManager().getDefaultDisplay();

                // create virtual display depending on device width / height
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new OrientationChangeCallback(Global.getMainActivity());
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // register media projection stop callback
                mMediaProjection.registerCallback(new MediaProjectionStopCallback(), Global.getMainHandler());
            }
        }
    }

    public void stopProjection() {
        Global.getMainHandler().post(() -> {
            if (mMediaProjection != null) {
                mMediaProjection.stop();
            }
        });
    }

    private void createVirtualDisplay() {
        mDisplay.getRealMetrics(mMetrics);

        // start capture reader
        mImageReader = ImageReader.newInstance(mMetrics.widthPixels, mMetrics.heightPixels, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, mMetrics.widthPixels, mMetrics.heightPixels, mMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReader.getSurface(), null, Global.getMainHandler());
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), Global.getMainHandler());
    }

    public byte[] getScreenImage() {
        synchronized (mScreenBitmapLock) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mScreenBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            return stream.toByteArray();
        }
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
                int rowPadding = rowStride - pixelStride * mMetrics.widthPixels;

                // create bitmap
                Bitmap bitmap = Bitmap.createBitmap(mMetrics.widthPixels + rowPadding / pixelStride, mMetrics.heightPixels, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                synchronized (mScreenBitmapLock) {
                    // trim black border
                    mScreenBitmap = Bitmap.createBitmap(bitmap, 0, 0, mMetrics.widthPixels, mMetrics.heightPixels);
                }
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
            Global.getMainHandler().postDelayed(() -> {
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 2000);
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCaptureService", "stopping projection.");
            Global.getMainHandler().post(() -> {
                if (mVirtualDisplay != null) mVirtualDisplay.release();
                if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                mMediaProjection.stop();
                mMediaProjection = null;
                mProjectionManager = null;
            });
        }
    }
}