package com.slavi.openimaj;

import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

public class Test1 {

	void doIt() throws Exception {
		VideoCapture video = new VideoCapture(640, 480);
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
		
//		MBFImage image = ImageUtilities.readMBF(new File(Const.sourceImage));
//		System.out.println(image.colourSpace);
/*		MBFImage clone = image.clone();
		for (int y=0; y<image.getHeight(); y++) {
		    for(int x=0; x<image.getWidth(); x++) {
		        clone.getBand(1).pixels[y][x] = 0;
		        clone.getBand(2).pixels[y][x] = 0;
		    }
		}*/
/*		DoGSIFTEngine engine = new DoGSIFTEngine();	
		LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(image.flatten());
		
		
		System.out.println(queryKeypoints);
		
		//LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());
		
		//image.processInplace(new CannyEdgeDetector());
		DisplayUtilities.display(image.flatten());*/
	}

	public static void main(String[] args) throws Exception {
		new Test1().doIt();
		System.out.println("Done.");
	}
}
