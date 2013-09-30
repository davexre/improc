package com.slavi.util.sound;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class MidiSound {

	/**
	 * Plays a MIDI note for a specified duration and returns. 
	 * @param channel				[0..15]
	 * @param note					[0..127]
	 * @param velocity				[0..127]
	 */
	public static void playNote(int channel, int note, int velocity, int durationMillis) throws InterruptedException, MidiUnavailableException {
		channel = PlaySound.clipValue(channel, 0, 15);
		note = PlaySound.clipValue(note, 0, 127);
		velocity = PlaySound.clipValue(velocity, 0, 127);
				
		Synthesizer synth = MidiSystem.getSynthesizer();
		synth.open();
		MidiChannel[] channels = synth.getChannels();
		MidiChannel midiChannel = channels[channel];
		midiChannel.noteOn(note, velocity);
		Thread.sleep(durationMillis);
		midiChannel.noteOff(note);
		synth.close();
	}
	
	public static void main(String[] args) throws InterruptedException, MidiUnavailableException {
//		for (int channel = 0; channel <= 15; channel++) 
		for (int note = 0; note <= 127; note++) 
			playNote(10, note, 100, 500);
		System.out.println("Done.");
	}
}
