package com.slavi.reprap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import com.slavi.util.Const;
import com.slavi.util.Util;

public class VisualizeGCode {

	public static class GCodeCommand {
		
		public static final int CommandFlad_G = (1<<0);
		public static final int CommandFlad_M = (1<<1);
		public static final int CommandFlad_P = (1<<2);
		public static final int CommandFlad_X = (1<<3);
		public static final int CommandFlad_Y = (1<<4);
		public static final int CommandFlad_Z = (1<<5);
		public static final int CommandFlad_I = (1<<6);
		public static final int CommandFlad_N = (1<<7);
		public static final int CommandFlad_CHECKSUM = (1<<8);
		public static final int CommandFlad_F = (1<<9);
		public static final int CommandFlad_S = (1<<10);
		public static final int CommandFlad_Q = (1<<11);
		public static final int CommandFlad_R = (1<<12);
		public static final int CommandFlad_E = (1<<13);
		public static final int CommandFlad_T = (1<<14);
		public static final int CommandFlad_J = (1<<15);
				
		public int gCode;
		public int mCode;
		public double X, Y, Z, E;
		public double feedRate;	// command F
		public double P;			// command P
		public double T;			// command T
		public double S;			// command S
		public double I;			// command I
		public double J;			// command J
		public double R;			// command R
		public double Q;			// command Q
		public int N;				// command N

		public int commandOccuraceFlag;

		public void parseLine(String line) {
			commandOccuraceFlag = 0;
			int comment = line.indexOf(';');
			if (comment >= 0)
				line = line.substring(0, comment);
			StringTokenizer st = new StringTokenizer(line, " ");

			while (st.hasMoreTokens()) {
				String item = st.nextToken().trim();
				if ("".equals(item))
					continue;
				char cmd = item.charAt(0);
				item = item.substring(1);
				
				switch (cmd) {
				case 'G':
					gCode = Integer.parseInt(item);
					commandOccuraceFlag |= CommandFlad_G;
					break;

				case 'M':
					mCode = Integer.parseInt(item);
					commandOccuraceFlag |= CommandFlad_M;
					break;
					
				case 'T':
					T = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_T;
					break;
					
				case 'S':
					S = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_S;
					break;
					
				case 'P':
					P = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_P;
					break;
					
				case 'X':
					X = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_X;
					break;
				case 'Y':
					Y = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_Y;
					break;
				case 'Z':
					Z = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_Z;
					break;
				case 'E':
					E = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_E;
					break;
					
				case 'I':
					I = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_I;
					break;
					
				case 'J':
					J = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_J;
					break;
					
				case 'F':
					feedRate = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_F;
					break;
					
				case 'R':
					R = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_R;
					break;
					
				case 'Q':
					Q = Double.parseDouble(item);
					commandOccuraceFlag |= CommandFlad_Q;
					break;
					
				case 'N':
					N = Integer.parseInt(item);
					commandOccuraceFlag |= CommandFlad_N;
					break;
					
				case '*':
				default:
				}
			}
		}
	}

	public static class ValueExtent {
		public double min, max;
		
		public ValueExtent() {
			initialize();
		} 
		
		public void initialize() {
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
		}
		
		public void add(double value) {
			if (value < min)
				min = value;
			if (value > max) 
				max = value;
		}
	}
	
	ValueExtent X, Y, Z;
	
	public void doIt(String outputDir, String finName) throws Exception{
		X = new ValueExtent();
		Y = new ValueExtent();
		Z = new ValueExtent();
		
		// Determine object extent
		BufferedReader reader = new BufferedReader(new FileReader(finName));
		GCodeCommand cmd = new GCodeCommand();
		while (reader.ready()) {
			String line = reader.readLine();
			cmd.parseLine(line);

			if ((cmd.commandOccuraceFlag & GCodeCommand.CommandFlad_X) != 0) {
				X.add(cmd.X);
			}
			if ((cmd.commandOccuraceFlag & GCodeCommand.CommandFlad_Y) != 0) {
				Y.add(cmd.Y);
			}
			if ((cmd.commandOccuraceFlag & GCodeCommand.CommandFlad_Z) != 0) {
				Z.add(cmd.Z);
			}
		}
		reader.close();
		
		int sizeX = (int) ((X.max - X.min) * 10);
		int sizeY = (int) ((Y.max - Y.min) * 10);
		int curZ = (int) (Z.min * 10);
		int curX = 0;
		int curY = 0;
		int curE = 0;
		boolean isExtruding = false;
		BufferedImage bi = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
		Graphics2D gr = bi.createGraphics();
		gr.setColor(Color.yellow);
		
		reader = new BufferedReader(new FileReader(finName));
		while (reader.ready()) {
			String line = reader.readLine();
			cmd.parseLine(line);

			if ((cmd.commandOccuraceFlag & GCodeCommand.CommandFlad_Z) != 0) {
				int newZ = (int) (cmd.Z * 10);
				if (newZ > curZ) {
					String fouName = String.format("%s/layer_%05d.png", outputDir, curZ);
					gr.dispose();
					ImageIO.write(bi, "png", new File(fouName));
					curZ = newZ;
					bi = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
					gr = bi.createGraphics();
					gr.setColor(Color.red);
					isExtruding = false;
				} else if (newZ < curZ) {
					throw new Exception("newZ < curZ");
				}
			}

			if ((cmd.commandOccuraceFlag & GCodeCommand.CommandFlad_E) != 0) {
				int newE = (int) (cmd.E * 10); 
				isExtruding = (newE > curE);
				curE = newE;
				gr.setColor(isExtruding ? Color.red : Color.yellow);
			}
			int newX = curX;
			int newY = curY;
			if ((cmd.commandOccuraceFlag & GCodeCommand.CommandFlad_X) != 0) {
				newX = (int) (cmd.X * 10);
			}
			if ((cmd.commandOccuraceFlag & GCodeCommand.CommandFlad_Y) != 0) {
				newY = (int) (cmd.Y * 10);
			}			

			if ((cmd.commandOccuraceFlag & GCodeCommand.CommandFlad_G) != 0) {
				if (cmd.gCode == 1 || cmd.gCode == 0) { // Controlled move
					gr.drawLine(curX, curY, newX, newY);
				}
				curX = newX;
				curY = newY;
			}
		}
		String fouName = String.format("%s/layer_%05d.png", outputDir, curZ);
		gr.dispose();
		ImageIO.write(bi, "png", new File(fouName));
		
		reader.close();
	}
		
	public static void main(String[] args) throws Exception {
		VisualizeGCode g = new VisualizeGCode();
		String finName = g.getClass().getResource("VisualizeGCode-drive-gear.gcode").getFile();
//		String finName = g.getClass().getResource("qube10.gcode").getFile();
		String outputDir = Const.workDir + "/gcode";
		File outputDirFile = new File(outputDir);
		Util.removeDirectory(outputDirFile);
		outputDirFile.mkdirs();
		
		g.doIt(outputDir, finName);
		System.out.println("Done.");
	}
}
