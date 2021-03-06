package com.slavi.image;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;

public class DWindowedBMPImage implements DWindowedImage  {

	static class ImageRow {
		int callId;
		int row;
		boolean isModified;
		double[] data;
	}
	
	ImageRow[] rows;
	
	ImageRow[] allBufferedRows;
	
	int lastCallId;
	
	int rowWriteCounter;
	
	int rowReadCounter;
	
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

	protected long getFilePosition(int atX, int atY) {
		if ((atX < 0) || (atX > sizeX) || (atY < 0) || (atY > sizeY))
			throw new InvalidParameterException("Invalid coordinates");
		return dataoffset + atX * 3 + (sizeX * 3 + rowpadding) * (sizeY - atY - 1);
	}
	
	protected void readHeader(File theFile) throws IOException {
		f = new RandomAccessFile(theFile, "rw");
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

	protected int readWord() throws IOException {
		return f.readByte() | ((int)f.readByte() << 8); 
	}
	
	protected void writeWord(int w) throws IOException {
		f.writeShort(
				((w << 8) & 0xff00) |
				((w >> 8) & 0x00ff));
	}

	protected int readDWord() throws IOException {
		return f.readByte() | ((int)f.readByte() << 8) | ((int)f.readByte() << 16) | ((int)f.readByte() << 24); 
	}
	
	protected void writeDWord(int dw) throws IOException {
		f.writeInt(
				((dw << 24) & 0xff000000) |
				((dw << 8) & 0x00ff0000) |
				((dw >> 8) & 0x0000ff00) |
				((dw >> 24) & 0x000000ff));
	}
	
	protected DWindowedBMPImage() {
	}

	public static DWindowedBMPImage open(File theFile) throws IOException {
		DWindowedBMPImage result = new DWindowedBMPImage();
		result.readHeader(theFile);
		result.makeRows((int) (result.sizeY / 10));
		return result;
	}
	
	public static DWindowedBMPImage open(File theFile, int bufferedRows) throws IOException {
		DWindowedBMPImage result = new DWindowedBMPImage();
		result.readHeader(theFile);
		result.makeRows(bufferedRows);
		return result;
	} 

	protected void crateInFile_noMakeRows(File theFile, int sizeX, int sizeY) throws IOException {
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
	
	public static DWindowedBMPImage create(File theFile, int sizeX, int sizeY, int bufferedRows) throws IOException {
		DWindowedBMPImage result = new DWindowedBMPImage();
		result.crateInFile_noMakeRows(theFile, sizeX, sizeY);
		result.makeRows(bufferedRows);
		return result;
	}

	public static DWindowedBMPImage create(File theFile, int sizeX, int sizeY) throws IOException {
		DWindowedBMPImage result = new DWindowedBMPImage();
		result.crateInFile_noMakeRows(theFile, sizeX, sizeY);
		result.makeRows((int) (result.sizeY / 10));
		return result;
	}
	
	protected void makeRows(int bufferedRows) {
		rows = new ImageRow[sizeY];
		if (bufferedRows < 10)
			bufferedRows = 10;
		if (bufferedRows > sizeY)
			bufferedRows = sizeY;
		allBufferedRows = new ImageRow[bufferedRows];
		for (int i = bufferedRows - 1; i >= 0; i--) {
			ImageRow ir = new ImageRow();
			ir.callId = 0;
			ir.row = i;
			ir.isModified = false;
			ir.data = new double[sizeX];
			rows[i] = ir;
			allBufferedRows[i] = ir;
		}
		for (int i = sizeY - 1; i >= bufferedRows; i--) {
			rows[i] = null;
		}
		lastCallId = 0;
		rowReadCounter = 0;
		rowWriteCounter = 0;
	}

	ImageRow getRow(int row) throws IOException {
		ImageRow r = rows[row];
		if (r != null) {
			r.callId = ++lastCallId;
			return r;
		}
		// Select the oldest accessed row 
		int minId = lastCallId;
		for (int i = allBufferedRows.length - 1; i >= 0; i--) {
			ImageRow tmp = allBufferedRows[i];
			if (tmp.callId <= minId) {
				minId = tmp.callId;
				r = tmp;
			}
		}
		// If the selected row is modified, save it
		flushRow(r);
		// Assign the selected ImageRow a new row
		rows[r.row] = null;
		rows[row] = r;
		r.row = row;
		int decrementCallId = r.callId;
		lastCallId -= decrementCallId;
		for (int i = allBufferedRows.length - 1; i >= 0; i--) {
			allBufferedRows[i].callId -= decrementCallId;
		}
		// Read the pixel data for the new row
		long filePos = getFilePosition(0, row);
		f.seek(filePos);
		for (int i = 0; i < sizeX; i++) {
			r.data[i] = (f.readByte() + f.readByte() + f.readByte()) / (3.0 * 255.0); 
		}
		rowReadCounter++;
		r.callId = ++lastCallId;
		return r;
	}

	void flushRow(ImageRow r) throws IOException {
		if (r.isModified) {
			rowWriteCounter++;
			long filePos = getFilePosition(0, r.row);
			f.seek(filePos);
			for (int i = 0; i < sizeX; i++) {
				byte v = (byte)Math.max(0, Math.min(255, Math.round(r.data[i] * 255.0)));
				f.writeByte(v); 
				f.writeByte(v); 
				f.writeByte(v); 
			}
		}
	}
	
	public double getPixel(int atX, int atY) {
		if ((atX < 0) || (atX > sizeX) || (atY < 0) || (atY > sizeY))
			throw new InvalidParameterException("Invalid coordinates");
		try {
			return getRow(atY).data[atX];
		} catch (IOException e) {
			throw new RuntimeException("Error getting pixel", e);
		}
	}

	public void setPixel(int atX, int atY, double value) {
		if ((atX < 0) || (atX > sizeX) || (atY < 0) || (atY > sizeY))
			throw new InvalidParameterException("Invalid coordinates");
		try {
			ImageRow r = getRow(atY);
			r.data[atX] = value;
			r.isModified = true;
		} catch (IOException e) {
			throw new RuntimeException("Error setting pixel", e);
		}
	}

	public void close() throws IOException {
		for (int i = allBufferedRows.length - 1; i >= 0; i--) {
			flushRow(allBufferedRows[i]);
		}
		f.close();
	}

	public int getRowWriteCounter() {
		return rowWriteCounter;
	}

	public int getRowReadCounter() {
		return rowReadCounter;
	}
	
	public String toString() {
		return "DBufferedBMPImage: width:" + Integer.toString(sizeX) + 
				" height:" + Integer.toString(sizeY) +
				" rowBufSize:" + Integer.toString(allBufferedRows.length) + 
				" rowReads:" + Integer.toString(rowReadCounter) + 
				" rowWrites:" + Integer.toString(rowWriteCounter);
	}
}
