import javafx.fxml.FXML;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.Utils;

public class FXController {
    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;

    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;

    @FXML
    protected void startCamera(ActionEvent event) {
        if(!this.cameraActive) {
            this.capture.open(cameraId);

            if(this.capture.isOpened()) {
                this.cameraActive = true;
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        Mat frame = grabFrame();
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                this.button.setText("Stop Camera");
            } else {
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            this.cameraActive = false;
            this.button.setText("Start Camera");
            this.stopAcquisition();
        }
    }

    private Mat grabFrame() {       //grabs next frame from the video capture object
        Mat frame = new Mat();
        if(this.capture.isOpened()) {
            try {
                this.capture.read(frame);
                if(!frame.empty()) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);         //convert image to greyscale
                    Core.flip(frame, frame, 1);                                     //flip image
                }
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e);
            }
        }
        return frame;
    }

    private void stopAcquisition() {                        //stop capturing frames and close video capture object
        if(this.timer!=null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }
        if(this.capture.isOpened()) {
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed() {
        this.stopAcquisition();
    }
}
