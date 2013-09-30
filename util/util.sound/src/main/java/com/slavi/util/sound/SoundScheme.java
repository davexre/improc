package com.slavi.util.sound;

import java.io.InputStream;

public class SoundScheme {
	public InputStream getSound(Sound sound) {
		String fname;
		switch (sound) {
		case BEEP: 
			fname = "/ubuntu/dialog-question.ogg"; 
			break;
		case WARNING: 
			fname = "/ubuntu/dialog-warning.ogg"; 
			break;
		case ERROR: 
			fname = "/ubuntu/dialog-error.ogg"; 
			break;
		default: 
			fname = "/ubuntu/dialog-error.ogg";
			break;
		}			
		return PlaySound.class.getResourceAsStream(fname);
	}
}
