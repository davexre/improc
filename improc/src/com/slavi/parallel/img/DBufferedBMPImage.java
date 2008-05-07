package com.slavi.parallel.img;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import com.slavi.img.DImageMap;

public class DBufferedBMPImage extends DWindowedBMPImage {

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
	
	public DBufferedBMPImage(File theFile) throws IOException {
		super(theFile);
		makeRows();
	}

	public DBufferedBMPImage(File theFile, int sizeX, int sizeY) throws IOException {
		super(theFile, sizeX, sizeY);
		makeRows();
	}

	void makeRows() {
		rows = new ImageRow[sizeY];
		int bufferedRows = Math.min(10, sizeY);
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
		super.close();
	}
	
	public static void main(String[] args) throws Exception {
		String fname = "c:/Users/s/kayak/me in the kayak.jpg";
		String fou = "c:/temp/ttt.bmp";
		DImageMap im = new DImageMap(new File(fname));
		DBufferedBMPImage out = new DBufferedBMPImage(new File(fou), im.getSizeX(), im.getSizeY());
		try {
			for (int j = im.getSizeY() - 1; j >= 0; j--) 
				for (int i = im.getSizeX() - 1; i >= 0; i--) {
					double v = im.getPixel(i, j);
					out.setPixel(i, j, v);
				}
		} finally {
			out.close();
		}
		System.out.println("DONE.");
		System.out.println("Reads  " + out.rowReadCounter);
		System.out.println("Writes " + out.rowWriteCounter);
	}
}
