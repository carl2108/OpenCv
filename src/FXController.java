import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.Utils;

public class FXController {
    @FXML
    private Button button;
    @FXML
    private CheckBox grayscale;
    @FXML
    private CheckBox flip;
    @FXML
    private CheckBox test;
    @FXML
    private ImageView currentFrame;

    private Filter currentFilter;

    @FXML
    private MenuButton addFilter;

    @FXML
    private Boolean channelSlidersEnabled;

    @FXML
    private Slider channelSlider0;
    @FXML
    private Slider channelSlider1;
    @FXML
    private Slider channelSlider2;

    public Slider getChannelSlider0() {
        return channelSlider0;
    }

    public Slider getChannelSlider1() {
        return channelSlider1;
    }

    public Slider getChannelSlider2() {
        return channelSlider2;
    }

    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;

    private List<Filter> filters = new ArrayList<>();

    @FXML
    protected void startCamera(ActionEvent event) {
        initControls();

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

    /*private Mat grabFrame() {       //grabs next frame from the video capture object
        Mat frame = new Mat();
        if(this.capture.isOpened()) {
            try {
                this.capture.read(frame);
                if(!frame.empty()) {
                    applyFilters(frame);
                }
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e);
            }
        }
        return frame;
    }*/

    private Mat grabFrame() {       //load stock image
        Mat frame = Imgcodecs.imread("C://Users//carl.oconnor//MyCode//OpenCv//src//res//dag.jpg", Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Imgproc.resize(frame, frame, new Size(800, 600));
        applyFilters(frame);
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

    private void applyFilters(Mat frame) {
        for(Filter f : filters)
            f.filter(frame, this);
        /*Mat ones = Mat.ones(frame.height(), frame.width(), CvType.CV_8U);

        if(flip.isSelected())
            Core.flip(frame, frame, 1);                                     //flip image

        if(hlsRadioButton.isSelected()) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2HLS);
        } else if(yuvRadioButton.isSelected()) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2YUV);
        } else {

        }

        List<Mat> channels = new ArrayList<>();
        Core.split(frame, channels);

        if(test.isSelected()) {
            channels.set(0, channels.get(0).mul(ones, channelSlider0.getValue()/channelSlider0.getMax()));
            channels.set(1, channels.get(1).mul(ones, channelSlider1.getValue()/channelSlider1.getMax()));
            channels.set(2, channels.get(2).mul(ones, channelSlider2.getValue()/channelSlider2.getMax()));
            Core.merge(channels, frame);
        }

        if(grayscale.isSelected())
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);         //convert image to greyscale*/
    }


    @FXML
    private void initControls() {


    }

    @FXML
    private void loadImage() {
        startCamera(new ActionEvent());
    }

    @FXML
    private void addFilter() {

    }

    @FXML
    private void addRGBFilter() {
        currentFilter = new RGBFilter();
        channelSlidersEnabled = true;
    }

    @FXML
    private void addHLSFilter() {
        currentFilter = new HLSFilter();
        channelSlidersEnabled = true;
    }

    @FXML
    private void addGrayscaleFilter() {
        currentFilter = new GrayscaleFilter();
        channelSlidersEnabled = false;

    }
}
