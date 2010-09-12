package com.slavi.util;

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
import javax.sound.sampled.UnsupportedAudioFileException;

import com.slavi.math.MathUtil;

public class Beep {

	static final int defaultBeepDuration = 500;		// milliseconds
	
	static final int defaultBeepLoudness = 80;		// percentage
	
	static final int defaultBeepPitch = 1900;		// Hertz
	
	static final int samplesPerSecond = 44000;
	
	static final AudioFormat audioFormat = new AudioFormat(samplesPerSecond, 8, 1, true, false);
	
	static final byte[] beepData = new byte[samplesPerSecond / 10]; 
				//makeTone(1000, defaultBeepLoudness, samplesPerSecond, 100);
	
	public static void makeTone(double pitch, double volumeInPersent, int samplesPerSecond, byte dest[]) {
		double omega = (2.0 * Math.PI * pitch) / samplesPerSecond;
		volumeInPersent = MathUtil.clipValue(volumeInPersent, 0, 100) * 127 / 100;
		for (int i = 0; i < dest.length; i++) {
			dest[i] = (byte) (volumeInPersent * Math.sin(omega * i));
		}		
	}
	
	public static byte[] makeTone(double pitch, double volumeInPersent, int samplesPerSecond, int durationMillis) {
		byte dest[] = new byte[samplesPerSecond * durationMillis / 1000];
		makeTone(pitch, volumeInPersent, samplesPerSecond, dest);
		return dest;
	}

	public static byte[] makeTone(double pitch, double volumeInPersent, int durationMillis) {
		int samplesPerSecond = (int) (pitch * 10);
		byte dest[] = new byte[samplesPerSecond * durationMillis / 1000];
		makeTone(pitch, volumeInPersent, samplesPerSecond, dest);
		return dest;
	}
	
	public synchronized static void beep() {
		beep(defaultBeepPitch, defaultBeepDuration, defaultBeepLoudness);
	}

	public synchronized static void beep(double pitch, int durationMillis, int beepLoudness) {
		makeTone(pitch, beepLoudness, samplesPerSecond, beepData);
		Clip clip = null;
		try {
			DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(audioFormat, beepData, 0, beepData.length);
			clip.loop(-1);
			Thread.sleep(durationMillis, 0);
		} catch (Exception e) {
		} finally {
			if (clip != null)
				clip.stop();
		}
	}
	
	public synchronized static void beep2(int beepLoudness, int ... data ) {
		Clip clip1 = null;
		Clip clip2 = null;
		try {
			DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
			clip1 = (Clip) AudioSystem.getLine(info);
			clip2 = (Clip) AudioSystem.getLine(info);
			Clip tmp;
			int i = 0;
			long delay = 0;
			long last = 0;
			while (i < data.length - 1) {
				int pitch = data[i++];
				makeTone(pitch, beepLoudness, samplesPerSecond, beepData);
				clip1.open(audioFormat, beepData, 0, beepData.length);
				clip1.setFramePosition(0);
				if (last != 0) {
					delay -= System.currentTimeMillis() - last;
					if (delay > 0)
						Thread.sleep(delay);
					else {
						System.out.println(delay);
					}
				}
				clip1.loop(-1);
				clip2.stop();
				clip2.close();
				delay = data[i++];
				last = System.currentTimeMillis();
				tmp = clip2;
				clip2 = clip1;
				clip1 = tmp;
			}
			if (last != 0) {
				delay -= System.currentTimeMillis() - last;
				if (delay > 0)
					Thread.sleep(delay);
				else {
					System.out.println(delay);
				}
			}
//			Thread.sleep(delay);
//			System.out.println(delay);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (clip2 != null) {
				clip2.stop();
				clip2.close();
			}
			if (clip1 != null) {
				clip1.stop();
				clip1.close();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
//		FileInputStream is = new FileInputStream("/usr/lib/openoffice/basis3.2/share/gallery/sounds/pluck.wav");
//		playSoundStream(is, 0);
		Marker.mark();
		int d = 250;
//		Beep.beep2(80, 900, d, 1000, d, 500, d, 1500, d);
//		Beep.beep(1000, 400, 80);
//		Beep.beep(1900, 300, 80);
		Marker.release();
		Beep.beep();
		Beep.beep();
		System.out.println("Done");
	}
	
	/**
	 * code borrowed from
	 * http://www.java2s.com/Code/Java/Development-Class/AnexampleofloadingandplayingasoundusingaClip.htm
	 */
	public static void playSoundStream(InputStream soundStream, long timeout) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		// assuming the sound can be played by the audio system
		AudioInputStream sound = AudioSystem.getAudioInputStream(soundStream);

		// load the sound into memory (a Clip)
		DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
		Clip clip = (Clip) AudioSystem.getLine(info);
		clip.open(sound);

		final CountDownLatch latch = new CountDownLatch(1);
		clip.addLineListener(new LineListener() {
			public void update(LineEvent event) {
				if (event.getType() == LineEvent.Type.STOP) {
					event.getLine().close();
					latch.countDown();
				}
			}
		});
		clip.start();
		if (timeout > 0)
			latch.await(timeout, TimeUnit.MILLISECONDS);
		else 
			latch.await();
	}
}
