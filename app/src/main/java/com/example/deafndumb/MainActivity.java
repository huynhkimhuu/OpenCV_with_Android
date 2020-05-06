package com.example.deafndumb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import org.opencv.android.*;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    JavaCameraView javaCameraView;
    File cascFile;

    CascadeClassifier cascadeClassifier;

    private  Mat rgba, grey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = findViewById(R.id.openCV_camera);

        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseCallBack);
        }else{
            try {
                baseCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        javaCameraView.setCvCameraViewListener(this);


    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        rgba = new Mat();
        grey = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        rgba.release();
        grey.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        rgba = inputFrame.rgba();
        grey = inputFrame.gray();

        MatOfRect bodyDetection = new MatOfRect();
        cascadeClassifier.detectMultiScale(rgba, bodyDetection);

        for(Rect rect: bodyDetection.toArray()){
            Imgproc.rectangle(rgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255,0,0));
        }

        return rgba;
    }

    private BaseLoaderCallback baseCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status){
            case LoaderCallbackInterface.SUCCESS: {
                    InputStream inputStream = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    cascFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");

                    FileOutputStream fileOutputStream = new FileOutputStream(cascFile);
                    byte[] buffer = new byte[4096];
                    int byteRead;

                    while ((byteRead = inputStream.read(buffer)) != -1){
                        fileOutputStream.write(buffer, 0, byteRead);
                    }

                    inputStream.close();
                    fileOutputStream.close();

                    cascadeClassifier = new CascadeClassifier(cascFile.getAbsolutePath());

                    if(cascadeClassifier.empty()){
                        cascadeClassifier = null;
                    }else {
                        cascFile.delete();
                    }
                    javaCameraView.enableView();

                }
                break;

                default: {
                    super.onManagerConnected(status);
                }
                break;

            }
        }
    };

}
