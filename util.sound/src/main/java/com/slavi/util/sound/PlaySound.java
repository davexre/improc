package com.slavi.util.sound;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PlaySound {
	
	/**
	 * Returns the value if min <= value <= max else returns min or max respecively.
	 */
	static int clipValue(int value, int min, int max) {
		return Math.min(max, Math.max(min, value));
	}

	/*
	 * Needed library from http://www.javazoom.net/vorbisspi/sources.html
	 */
	
	public static SoundScheme soundScheme = new SoundScheme();
	
	static String availableSounds[] = {
			"/ubuntu/dialog-warning.ogg",
			"/ubuntu/window-slide.ogg",
			"/ubuntu/dialog-error.ogg",
			"/ubuntu/dialog-question.ogg",
			"/ubuntu/button-pressed.ogg",
			"/gnome/victory.ogg",
			"/gnome/lines3.ogg",
			"/gnome/glass.ogg",
			"/gnome/bark.ogg",
			"/gnome/laughter.ogg",
			"/gnome/sonar.ogg",
			"/gnome/gameover.ogg",
			"/gnome/flip-piece.ogg",
	};

	public static void playSoundStream(InputStream soundStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (soundStream == null) {
			throw new NullPointerException();
		}
		AudioInputStream sound = AudioSystem.getAudioInputStream(new BufferedInputStream(soundStream));

		AudioFormat baseFormat = sound.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
				16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, sound);

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
		SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

		byte[] data = new byte[1024];
		line.open(decodedFormat);
		line.start();
		int len;
		while ((len = din.read(data)) >= 0) {
			if (len > 0)
				line.write(data, 0, len);
		}
		line.drain();
		line.stop();
		line.close();
	}

	public static void playSound(String fname) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		InputStream fin = new FileInputStream(fname);
		playSoundStream(fin);
		fin.close();
	}

	public static void playSound(Sound sound) {
		try {
			InputStream fin = soundScheme.getSound(sound);
			playSoundStream(fin);
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		for (Sound i : Sound.values()) {
			System.out.println(i);
			playSound(i);
		}
		System.out.println("Done");
	}
	
	public static void main2(String[] args) throws Exception {
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		for (String sound : availableSounds) {
			System.out.println(sound + " (press enter)");
			r.readLine();
			InputStream is = PlaySound.class.getResourceAsStream(sound);
			playSoundStream(is);
			is.close();
		}
		System.out.println("Done");
	}
}
