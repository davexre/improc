package com.slavi.improc;

import java.io.File;
import java.io.FileInputStream;

import com.slavi.math.MathUtil;
import com.slavi.util.file.FindFileIterator;

public class ImageStatistics {

	
	public static void main(String[] args) throws Exception {
		String fnameBase = "D:\\Users\\S\\Java\\Images\\Image data\\*.jpg";
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(fnameBase, true, true);
		
		while (imagesIterator.hasNext()) {
			File file = imagesIterator.next();
			SafeImage im = new SafeImage(new FileInputStream(file));
			double brightness = 0.0;
			double red = 0.0;
			double green = 0.0;
			double blue = 0.0;
			
			for (int i = 0; i < im.sizeX; i++) {
				for (int j = 0; j < im.sizeY; j++) {
					int c = im.getRGB(i, j);
					
					int r = ((c >> 16) & 0x0ff);
					int g = ((c >> 8) & 0x0ff);
					int b = c & 0x0ff;
					
					int bw = r + g + b;
					
					brightness += bw;
					red += r;
					green += g;
					blue += b;
				}
			}
			int count = im.sizeX * im.sizeY;
			brightness /= count * 3;
			red /= count;
			green /= count;
			blue /= count;
	
			System.out.print("BR=\t" + MathUtil.d4(brightness));
			System.out.print("\tR =\t" + MathUtil.d4(red));
			System.out.print("\tG =\t" + MathUtil.d4(green));
			System.out.print("\tB =\t" + MathUtil.d4(blue));
			System.out.println("\t" + file.getAbsolutePath());
		}
	}
}
