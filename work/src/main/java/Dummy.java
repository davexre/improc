import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import com.slavi.util.ColorConversion;
import com.slavi.util.Marker;


public class Dummy {
	
	double r[] = new double[256];
	double g[] = new double[256];
	double b[] = new double[256];
	double l[] = new double[256];
	double s[] = new double[256];
	double v[] = new double[256];
	
	double DRGB[] = new double[3];
	double HSL[] = new double[3];
//		double HSV[] = new double[3];
	public void doIt(BufferedImage bi) {

		final int sizeX = bi.getWidth() - 1;
		final int sizeY = bi.getHeight() - 1;
		
		int ffcount = 0;
		for (int j = sizeY; j >= 0; j--) {
			for (int i = sizeX; i >= 0; i--) {
				int color = bi.getRGB(i, j);
				if (color == 0xffffff)
					ffcount++;
				ColorConversion.RGB.fromRGB(color, DRGB);
				r[(int) Math.round(DRGB[0] * 255.0)]++;
				g[(int) Math.round(DRGB[1] * 255.0)]++;
				b[(int) Math.round(DRGB[2] * 255.0)]++;
				ColorConversion.HSL.fromDRGB(DRGB, HSL);
				s[(int) Math.round(HSL[1] * 255.0)]++;
				l[(int) Math.round(HSL[2] * 255.0)]++;
//				ColorConversion.HSV.fromDRGB(DRGB, HSV);
//				v[(int) Math.round(HSV[2] * 255.0)]++;
			}
		}
		
		// calc cumul
/*		{
			double cumul[] = s;
			double sum = 0;
			for (double d : cumul) {
				sum += d;
			}
			double c = 0;
			for (int i = 0; i < cumul.length; i++) {
				c += cumul[i];
				s[i] = c / sum;
			}
		}
		{
			double cumul[] = l;
			double sum = 0;
			for (double d : cumul) {
				sum += d;
			}
			double c = 0;
			for (int i = 0; i < cumul.length; i++) {
				c += cumul[i];
				v[i] = c / sum;
			}
		}*/
		
/*		System.out.println("FF=" + ffcount);
		System.out.println("R[255] = " + r[255]);
		System.out.println("G[255] = " + g[255]);
		System.out.println("B[255] = " + b[255]);
		System.out.println("L[255] = " + l[255]);
		System.out.println("S[255] = " + s[255]);
		System.out.println("V[255] = " + v[255]);
		System.out.println("Pixels = " + bi.getWidth() * bi.getHeight());*/
	}

	public static void main(String[] args) {
		String str = " asd   qwe  ,   zxcaaazzz bbb  ";
		// for (String s : StringUtils.split(str, ", ")) // str.split("( *, *)"))
		for (String s : str.split("((aaa)|,|(\\s))+", 0))
			System.out.println(s);
	}
	public static void main2(String[] args) throws Exception {
//		Thread.sleep(5000);
		String finName = "/home/spetrov/.S/Pictures/20150100 Ски на Добринище/Slavi/IMGP9361.JPG";
		File fin = new File(finName);
		BufferedImage bi = ImageIO.read(fin);
		System.out.println(bi.getWidth());
		System.out.println(bi.getHeight());
		System.out.println(bi.getWidth() * bi.getHeight());
		Dummy d = new Dummy();
		Marker.mark("calc");
		for (int i = 0; i < 10; i++) {
			d.doIt(bi);
		}
		Marker.release();
		System.out.println("Done.");
	}
}
