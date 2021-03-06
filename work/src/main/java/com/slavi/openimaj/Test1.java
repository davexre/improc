package com.slavi.openimaj;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.VideoDisplayStateListener;
import org.openimaj.video.capture.VideoCapture;

public class Test1 {

	void doIt() throws Exception {
		VideoCapture video = new VideoCapture(177, 140);
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
		display.addVideoDisplayStateListener(new VideoDisplayStateListener() {
			public void videoStopped(VideoDisplay<?> arg0) {
				video.close();
			}

			public void videoStateChanged(Mode arg0, VideoDisplay<?> arg1) {
			}

			public void videoPlaying(VideoDisplay<?> arg0) {
			}

			public void videoPaused(VideoDisplay<?> arg0) {
			}
		});
		//FaceDetector<DetectedFace,FImage> fd = new HaarCascadeDetector(40);
		//FaceDetector<KEDetectedFace,FImage> fd = new FKEFaceDetector();
		display.addVideoListener(
				  new VideoDisplayListener<MBFImage>() {
				    public void beforeUpdate(MBFImage frame) {
/*				    	List<KEDetectedFace> faces = fd.detectFaces(Transforms.calculateIntensity(frame));
				    	for( DetectedFace face : faces ) {
				    	    frame.drawShape(face.getBounds(), RGBColour.RED);
				    	}*/
				    	//ImageProcessor ip = new DifferenceOfGaussian();
				    	//ImageProcessor ip = new CannyEdgeDetector();
				    	//EuclideanDistanceTransform ip = new EuclideanDistanceTransform();
				        //frame.processInplace(new CannyEdgeDetector());
				    	FImage img = frame.flatten();

				    	//ip.analyseImage(img);
				    	//img = ip.getDistances();
				    	//img.normalise();

				        frame.drawImage(img.toRGB(), 0, 0);
				    }

				    public void afterUpdate(VideoDisplay<MBFImage> display) {
				    }
				  });

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
