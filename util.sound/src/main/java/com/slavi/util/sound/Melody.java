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

public class Melody {

	static final int samplesPerSecond = 44000;

	static final AudioFormat audioFormat = new AudioFormat(samplesPerSecond, 8, 1, true, false);

	static class Tone {
		public final double pitch;
		public final int octave;
		public final String name;
		
		public Tone(double pitch, int octave, String name) {
			this.pitch = pitch;
			this.octave = octave;
			this.name = name;
		}		
	};
	
	public static final Tone NoTone	= new Tone(0, 0, "pause");
	
	public static final Tone C4		= new Tone(262, 4, "C4"); 
	public static final Tone D4b	= new Tone(277, 4, "C#4"); 
	public static final Tone D4		= new Tone(294, 4, "D4"); 
	public static final Tone E4b	= new Tone(311, 4, "D#4"); 
	public static final Tone E4		= new Tone(330, 4, "E4"); 
	public static final Tone F4		= new Tone(349, 4, "F4");
	public static final Tone G4b	= new Tone(370, 4, "F#4");
	public static final Tone G4		= new Tone(392, 4, "G4");
	public static final Tone A4b	= new Tone(415, 4, "G#4");
	public static final Tone A4		= new Tone(440, 4, "A4");
	public static final Tone B4b	= new Tone(466, 4, "A#4");
	public static final Tone B4		= new Tone(494, 4, "B4");
	public static final Tone C5		= new Tone(524, 5, "C5");
	
	static Tone[] Octaves = {
		C4, D4, E4, F4, G4, A4, B4,
		C5
	};
	
	static class Note {
		public Tone tone;
		public int durationMillis;
		public int loudness; // in percent [0..100]
		
		public Note(Tone tone, int duration, int loudness) {
			this.tone = tone;
			this.durationMillis = duration;
			this.loudness = loudness;
		}
	}
	
	public static byte[] makeMelody(double globalLoudness, final Note... notes) {
		globalLoudness = MathUtil.clipValue(globalLoudness, 0, 1);
		
		int clipDuration = 0;
		for (Note note : notes) {
			clipDuration += note.durationMillis;
		}
		byte result[] = new byte[samplesPerSecond * clipDuration / 1000];

		int i = 0;
		int getNextAtIndex = 0;
		clipDuration = 0;
		double omega = 0.0;
		double volumeInPersent = 0.0;
		
		for (int index = 0; index < result.length; index++) {
			if (index == getNextAtIndex) {
				Note note = notes[i++];
				clipDuration += note.durationMillis;
				omega = (2.0 * Math.PI * note.tone.pitch) / samplesPerSecond;
				volumeInPersent = MathUtil.clipValue(globalLoudness * note.loudness, 0, 100) * 127 / 100;
				getNextAtIndex = samplesPerSecond * clipDuration / 1000;
			}
			result[index] = (byte) (volumeInPersent * Math.sin(omega * index));
		}
		return result;
	}
	
	public static void playMelody(double globalLoudness, final Note... notes ) {
		Clip clip = null;
		try {
			DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
			clip = (Clip) AudioSystem.getLine(info);
			byte[] beepData = makeMelody(0.8, notes);
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

	public static void playMelody(int toneLoudness, int toneDurationMillis, final Tone... tones) {
		Note melody[] = new Note[tones.length];
		for (int i = 0; i < tones.length; i++) {
			melody[i] = new Note(tones[i], toneDurationMillis, toneLoudness);
		}
		playMelody(1, melody);
	}
	
	public static void main(String[] args) {
//		playMelody(80, 250, Octaves);
		playMelody(80, 120, C5, C5, NoTone, A4, NoTone, A4, NoTone, A4);
	}
}
