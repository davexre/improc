package com.slavi.util.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PlaySound {
	/**
	 * code borrowed from
	 * http://www.java2s.com/Code/Java/Development-Class/AnexampleofloadingandplayingasoundusingaClip.htm
	 */
	public static void playSound(InputStream soundStream, long timeout) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		// assuming the sound can be played by the audio system
		AudioInputStream sound = AudioSystem.getAudioInputStream(new BufferedInputStream(soundStream));

		AudioFormat baseFormat = sound.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
				16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, sound);

	/*	SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		
		(SourceDataLine)*/
//		AudioInputStream din = AudioSystem.getAudioInputStream(new BufferedInputStream(soundStream));
		// load the sound into memory (a Clip)
		
		DataLine.Info info = new DataLine.Info(Clip.class, din.getFormat());
		final Clip clip = (Clip) AudioSystem.getLine(info);

		final CountDownLatch latch = new CountDownLatch(1);
		clip.addLineListener(new LineListener() {
			public void update(LineEvent event) {
				if (event.getType() == LineEvent.Type.STOP) {
					event.getLine().close();
					clip.close();
					latch.countDown();
				}
			}
		});
		clip.open(din);
		clip.start();
		if (timeout < 0) {
			return;
		} else if (timeout > 0) {
			latch.await(timeout, TimeUnit.MILLISECONDS);
			clip.stop();
		} else { 
			latch.await();
		}
	}

	public static void playSound(String fname, long timeout) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		playSound(new FileInputStream(fname), timeout);
	}
	
	public static void testPlay(String filename) {
		try {
			File file = new File(filename);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			AudioInputStream din = null;
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
					16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			// Play now.
			rawplay(decodedFormat, din);
			in.close();
		} catch (Exception e) {
			// Handle exception.
		}
	}

	private static void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
		byte[] data = new byte[4096];
		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesRead != -1) {
				nBytesRead = din.read(data, 0, data.length);
				if (nBytesRead != -1)
					nBytesWritten = line.write(data, 0, nBytesRead);
			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
	}

	private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}
	
	public static void main(String[] args) throws Exception {
		String fname = "D:\\Users\\InOutBox\\ogg\\sounds\\laughter.ogg";
//		String fname = "D:\\Users\\S\\Music\\Amon Tobin\\Amon Tobin-2000-Supermodified\\Amon Tobin-2000-Supermodified-01 Get Your Snack On.mp3";
//		String fname = "C:\\Program Files\\OpenOffice.org 3\\Basis\\share\\gallery\\sounds\\pluck.wav";
//		String fname = "/usr/lib/openoffice/basis3.2/share/gallery/sounds/pluck.wav";
		playSound(fname, 0);
//		testPlay(fname);
//		for (int i = 0; i < 10; i++) {
//			Thread.sleep(200);
//		}
//		Thread.sleep(5000);
		System.out.println("Done");
	}
}
