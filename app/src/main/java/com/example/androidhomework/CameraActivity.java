package com.example.androidhomework;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.hardware.camera2.CameraDevice.StateCallback;
import static android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW;
import static android.hardware.camera2.CameraDevice.TEMPLATE_STILL_CAPTURE;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray() {
        {
            append(Surface.ROTATION_0, 90);
            append(Surface.ROTATION_90, 0);
            append(Surface.ROTATION_180, 270);
            append(Surface.ROTATION_270, 180);
        }
    };
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private TextureView textureView;
    private Size imageDimension;
    private File pictureFile;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });

        Button takePictureButton = findViewById(R.id.btnCapture);
        takePictureButton.setOnClickListener(v -> {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics characteristics = Objects.requireNonNull(manager).getCameraCharacteristics(cameraDevice.getId());
                Size[] jpegSizes = Objects.requireNonNull(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(ImageFormat.JPEG);

                int width = 640;    // default values
                int height = 480;   // default values

                if (jpegSizes != null && jpegSizes.length != 0) {
                    width = jpegSizes[0].getWidth();
                    height = jpegSizes[0].getHeight();
                }

                ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                List<Surface> outputSurfaces = new ArrayList<>(2);
                outputSurfaces.add(reader.getSurface());
                outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                // Orientation
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
                pictureFile = new File(Environment.getExternalStorageDirectory() + "/my_picture.jpg");

                reader.setOnImageAvailableListener((ImageReader) -> new ImageReader.OnImageAvailableListener() {
                            @Override
                            public void onImageAvailable(ImageReader reader) {
                                try (Image image = reader.acquireLatestImage()) {
                                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                    byte[] bytes = new byte[buffer.capacity()];
                                    buffer.get(bytes);
                                    save(bytes);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            private void save(byte[] bytes) throws IOException {
                                try (OutputStream output = new FileOutputStream(pictureFile)) {
                                    output.write(bytes);
                                }
                            }
                        },
                        mBackgroundHandler);

                cameraDevice.createCaptureSession(
                        outputSurfaces,
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                try {
                                    session.capture(
                                            captureBuilder.build(),
                                            new CaptureCallback() {
                                                @Override
                                                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                                    super.onCaptureCompleted(session, request, result);
                                                    Toast.makeText(CameraActivity.this, "Saved:" + pictureFile, Toast.LENGTH_SHORT).show();
                                                    createCameraPreview();
                                                }
                                            },
                                            mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            }
                        },
                        mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        });
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background Thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }

                    cameraCaptureSessions = cameraCaptureSession;

                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            String cameraId = Objects.requireNonNull(manager).getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimension = Objects.requireNonNull(map).getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        CameraActivity.this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
                return;
            }

            manager.openCamera(
                    cameraId,
                    new StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            cameraDevice = camera;
                            createCameraPreview();
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            cameraDevice.close();
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            cameraDevice.close();
                            cameraDevice = null;
                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(CameraActivity.this, "You can't use this app without granting permission!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        }
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }
}