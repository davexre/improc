import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.VideoInputFrameGrabber;

public class JavaCV {

	// final int INTERVAL=1000;///you may use interval
	IplImage image;
	CanvasFrame canvas;

	public void run() {
		VideoInputFrameGrabber grabber = new VideoInputFrameGrabber(0);
		try {
			grabber.start();
			Frame frame;
			while (true) {
				frame = grabber.grab();
				if (frame != null) {
					//cvFlip(frame, frame, 1);// l-r = 90_degrees_steps_anti_clockwise
					//cvSaveImage((i++) + "-capture.jpg", frame);
					// show image on window
					canvas.showImage(frame);
				}
				// Thread.sleep(INTERVAL);
			}
		} catch (Exception e) {
		}
	}

	void doIt() throws Exception {
		canvas = new CanvasFrame("Web Cam");
		canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		canvas.show();
		run();
	}

	public static void main(String[] args) throws Exception {
		new JavaCV().doIt();
		System.out.println("Done.");
	}
}
