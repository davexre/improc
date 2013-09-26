package com.slavi.util.sound;

public enum Sound {
	BEEP, WARNING, ERROR;
	
	public void play() {
		PlaySound.playSound(this);
	}
}
