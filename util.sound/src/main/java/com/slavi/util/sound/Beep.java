package com.slavi.util.sound;

import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

import com.slavi.math.MathUtil;
import com.slavi.util.Marker;

public class Beep {

	static final int defaultBeepDuration = 500;		// milliseconds
	
	static final int defaultBeepLoudness = 80;		// percentage
	
	static final int defaultBeepPitch = 1900;		// Hertz
	
	static final int samplesPerSecond = 44000;
	
	static final AudioFormat audioFormat = new AudioFormat(samplesPerSecond, 8, 1, true, false);
	
	static final byte[] beepData = new byte[samplesPerSecond / 10]; 
	static final byte[] beepData2 = new byte[samplesPerSecond / 10]; 
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
	
	public static byte[] makeTone2(int beepLoudness, int ... data) {
		int clipDuration = 0;
		int i = 0;
		while (i < data.length - 1) {
			i++;
			clipDuration += data[i++];
		}
		System.out.println(clipDuration);
		byte result[] = new byte[samplesPerSecond * clipDuration / 1000];
		double volumeInPersent = MathUtil.clipValue(beepLoudness, 0, 100) * 127 / 100;

		i = 0;
		int getNextAtIndex = 0;
		int pitch = 0;
		int duration = 0;
		clipDuration = 0;
		double omega = 0.0;
		
		for (int index = 0; index < result.length; index++) {
			if (index == getNextAtIndex) {
				System.out.println(index + "\t" + i);
				pitch = data[i++];
				duration = data[i++];
				clipDuration += duration;
				omega = (2.0 * Math.PI * pitch) / samplesPerSecond;
				getNextAtIndex = samplesPerSecond * clipDuration / 1000;
			}
			result[index] = (byte) (volumeInPersent * Math.sin(omega * index));
		}
		return result;
	}
	
	public synchronized static void beep2(int beepLoudness, int ... data ) {
		Clip clip = null;
		try {
			DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
			clip = (Clip) AudioSystem.getLine(info);
			byte[] beepData = makeTone2(beepLoudness, data);
			clip.open(audioFormat, beepData, 0, beepData.length);
			final CountDownLatch latch = new CountDownLatch(1);
			clip.addLineListener(new LineListener() {
				public void update(LineEvent event) {
					if (event.getType() == Type.STOP) {
						latch.countDown();
					}
				}
			});
			clip.start();
			latch.await();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (clip != null) {
				clip.stop();
				clip.close();
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		FileInputStream is = new FileInputStream("/usr/lib/openoffice/basis3.2/share/gallery/sounds/pluck.wav");
//		playSoundStream(is, 0);
		Marker.mark();
		int d = 450;
//		int pitch = 1499;
//		Beep.beep2(80, 900, d, 1000, d, 500, d, 1500, d);
//		Beep.beep2(80, pitch, d, 0, 100, pitch, 250, 0, 100, pitch, 250);
		Beep.beep2(80, 700, d, 1000, d, 700, d, 1000, d, 700, d);
//		Beep.beep(1000, 400, 80);
//		Beep.beep(1900, 300, 80);
		Marker.release();
//		Beep.beep();
//		Beep.beep();
//		Beep.beep();
		System.out.println("Done");
	}
}
