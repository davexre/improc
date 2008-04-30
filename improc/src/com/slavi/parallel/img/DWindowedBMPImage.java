package com.slavi.parallel.img;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;

import com.slavi.img.DImageMap;

public class DWindowedBMPImage implements DWindowedImage {

	RandomAccessFile f;
	
	int sizeX;
	
	int sizeY;
	
	int dataoffset;
	
	int rowpadding;
	
	public Rectangle getExtent() {
		return new Rectangle(0, 0, sizeX, sizeY);
	}

	public int maxX() {
		return sizeX - 1;
	}

	public int maxY() {
		return sizeY - 1;
	}

	public int minX() {
		return 0;
	}

	public int minY() {
		return 0;
	}

	private long getFilePosition(int atX, int atY) {
		if ((atX < 0) || (atX > sizeX) || (atY < 0) || (atY > sizeY))
			throw new InvalidParameterException("Invalid coordinates");
		return dataoffset + atX * 3 + (sizeX * 3 + rowpadding) * (sizeY - atY - 1);
	}
	
	public double getPixel(int atX, int atY) {
		long pos = getFilePosition(atX, atY);
		try {
			f.seek(pos);
			return (f.readByte() + f.readByte() + f.readByte()) / (3.0 * 255.0); 
		} catch (IOException e) {
			throw new RuntimeException("Error getting pixel", e);
		}
	}
	
	public void setPixel(int atX, int atY, double value) {
		long pos = getFilePosition(atX, atY);
		try {
			f.seek(pos);
			byte v = (byte)Math.max(0, Math.min(255, Math.round(value * 255.0)));
			f.writeByte(v); 
			f.writeByte(v); 
			f.writeByte(v); 
		} catch (IOException e) {
			throw new RuntimeException("Error setting pixel", e);
		}
	}

	void readHeader() throws IOException {
		f.seek(0);
		if ((f.readByte() != 'B') || (f.readByte() == 'M'))
			throw new IOException("Invalid file format");
		readDWord();		// file size in bytes
		readDWord();		// application dependent constant
		dataoffset = readDWord();		// the offset, i.e. starting address, of the byte where the bitmap data can be found.
		
		// DIB header
		int headersize = readDWord();	// the size of this header (40 bytes)
		if (headersize != 40)
			throw new IOException("Unsupported file format");
		sizeX = readDWord(); 		// the bitmap width in pixels (signed integer).
		sizeY = readDWord();		// the bitmap height in pixels (signed integer).
		int colorplanes = readWord();	// 	the number of color planes being used. Must be set to 1
		int colordepth = readWord();	// the number of bits per pixel, which is the color depth of the image. Typical values are 1, 4, 8, 16, 24 and 32.
		int compression = readDWord();	// no compression
		readDWord();					// the image size. This is the size of the raw bitmap data (see below), and should not be confused with the file size.
		readDWord();					// the horizontal resolution of the image. (pixel per meter, signed integer)
		readDWord();					// the vertical resolution of the image. (pixel per meter, signed integer)
		readDWord();					// the number of colors in the color palette, or 0 to default to 2n.
		readDWord();					// the number of important colors used, or 0 when every color is important; generally ignored.
		
		if ((colorplanes != 1) || (colordepth != 24) || (compression != 0))
			throw new IOException("Unsupported file format");
		rowpadding = 4 - ((sizeX * 3) % 4);
		int computedImageDataSize = (sizeX * 3 + rowpadding) * sizeY;
		if (dataoffset + computedImageDataSize != f.length()) {
			throw new IOException("Incorrect file length");
		}
	}

	int readWord() throws IOException {
		return f.readByte() | ((int)f.readByte() << 8); 
	}
	
	void writeWord(int w) throws IOException {
		f.writeShort(
				((w << 8) & 0xff00) |
				((w >> 8) & 0x00ff));
	}

	int readDWord() throws IOException {
		return f.readByte() | ((int)f.readByte() << 8) | ((int)f.readByte() << 16) | ((int)f.readByte() << 24); 
	}
	
	void writeDWord(int dw) throws IOException {
		f.writeInt(
				((dw << 24) & 0xff000000) |
				((dw << 8) & 0x00ff0000) |
				((dw >> 8) & 0x0000ff00) |
				((dw >> 24) & 0x000000ff));
	}
	
	public DWindowedBMPImage(File theFile) throws IOException {
		this.f = new RandomAccessFile(theFile, "rw");
		readHeader();
	}
	
	public DWindowedBMPImage(File theFile, int sizeX, int sizeY) throws IOException {
		this.f = new RandomAccessFile(theFile, "rw");
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		rowpadding = 4 - ((sizeX * 3) % 4);
		int imageDataSize = (sizeX * 3 + rowpadding) * sizeY;
		dataoffset = 54;
		f.setLength(dataoffset + imageDataSize);
		
		// BMP header
		f.seek(0);
		f.writeByte('B');
		f.writeByte('M');
		writeDWord(dataoffset + imageDataSize);	// file size in bytes
		writeDWord(0);		// application dependent constant
		writeDWord(dataoffset);		// the offset, i.e. starting address, of the byte where the bitmap data can be found.
		
		// DIB header
		writeDWord(40);		// the size of this header (40 bytes)
		writeDWord(sizeX); 	// the bitmap width in pixels (signed integer).
		writeDWord(sizeY);		// the bitmap height in pixels (signed integer).
		writeWord(1);		// 	the number of color planes being used. Must be set to 1
		writeWord(24);		// the number of bits per pixel, which is the color depth of the image. Typical values are 1, 4, 8, 16, 24 and 32.
		writeDWord(0);		// no compression
		writeDWord(imageDataSize);		// the image size. This is the size of the raw bitmap data (see below), and should not be confused with the file size.
		writeDWord(0);		// the horizontal resolution of the image. (pixel per meter, signed integer)
		writeDWord(0);		// the vertical resolution of the image. (pixel per meter, signed integer)
		writeDWord(0);		// the number of colors in the color palette, or 0 to default to 2n.
		writeDWord(0);		// the number of important colors used, or 0 when every color is important; generally ignored.
	}
	
	public void close() throws IOException {
		f.close();
	}
	
	public static void main(String[] args) throws Exception {
		String fname = "D:/Users/s/kayak/me in the kayak.jpg";
		String fou = "D:/temp/ttt.bmp";
		DImageMap im = new DImageMap(new File(fname));
		DWindowedBMPImage out = new DWindowedBMPImage(new File(fou), im.getSizeX(), im.getSizeY());
		try {
			for (int i = im.getSizeX() - 1; i >= 0; i--)
				for (int j = im.getSizeY() - 1; j >= 0; j--) {
					double v = im.getPixel(i, j);
					out.setPixel(i, j, v);
				}
		} finally {
			out.close();
		}
		System.out.println("DONE.");
	}
}
