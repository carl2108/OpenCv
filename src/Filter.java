import org.opencv.core.Mat;

/**
 * Created by carl.oconnor on 02/06/2017.
 */
public interface Filter {

    public void filter(Mat frame, FXController input);

}
