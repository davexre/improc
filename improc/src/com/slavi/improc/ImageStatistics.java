package com.slavi.improc;

import java.io.File;
import java.io.FileInputStream;

import com.slavi.math.MathUtil;
import com.slavi.util.file.FindFileIterator;

public class ImageStatistics {

	
	public static void main(String[] args) throws Exception {
		String fnameBase = "D:\\Users\\S\\Java\\Images\\Image data\\20090801 Vodopad Skaklia\\Skaklia 1\\*.jpg";
		FindFileIterator imagesIterator = FindFileIterator.makeWithWildcard(fnameBase, true, true);
		
		System.out.println("BR\tR\tG\tB\tfile");
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
	
			System.out.println(
					MathUtil.d2(brightness) + "\t" +
					MathUtil.d2(red) + "\t" +
					MathUtil.d2(green) + "\t" +
					MathUtil.d2(blue) + "\t" +
					file.getAbsolutePath());
		}
	}
}
