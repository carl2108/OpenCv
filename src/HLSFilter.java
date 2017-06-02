import javafx.scene.control.Slider;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carl.oconnor on 02/06/2017.
 */
public class HLSFilter implements Filter {

    public void filter(Mat frame, FXController input) {
        Mat ones = Mat.ones(frame.height(), frame.width(), CvType.CV_8U);
        List<Mat> channels = new ArrayList<>();

        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2HLS);
        Core.split(frame, channels);

        Slider channelSlider0 = input.getChannelSlider0();
        Slider channelSlider1 = input.getChannelSlider1();
        Slider channelSlider2 = input.getChannelSlider2();

        channels.set(0, channels.get(0).mul(ones, channelSlider0.getValue()/channelSlider0.getMax()));
        channels.set(1, channels.get(1).mul(ones, channelSlider1.getValue()/channelSlider1.getMax()));
        channels.set(2, channels.get(2).mul(ones, channelSlider2.getValue()/channelSlider2.getMax()));

        Core.merge(channels, frame);

        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_HLS2RGB);
    }

}
