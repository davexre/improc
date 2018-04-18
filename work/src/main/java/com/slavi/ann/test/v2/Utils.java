package com.slavi.ann.test.v2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import com.slavi.ann.test.v2.Layer.LayerWorkspace;
import com.slavi.ann.test.v2.activation.DebugLayer;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.math.MathUtil;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.ColorConversion;

public class Utils {
	public static int fixColorLight(int baseColor, double light) {
		double hsl[] = new double[3];
		double drgb[] = new double[3];
		ColorConversion.RGB.fromRGB(baseColor, drgb);
		ColorConversion.HSL.fromDRGB(drgb, hsl);
		hsl[2] = light;
		ColorConversion.HSL.toDRGB(hsl, drgb);
		return ColorConversion.RGB.toRGB(drgb);
	}
	
	public static BufferedImage toImage(int baseColor, double minVal, double maxVal, Matrix m, BufferedImage dest) {
		double hsl[] = new double[3];
		double drgb[] = new double[3];
		ColorConversion.RGB.fromRGB(baseColor, drgb);
		ColorConversion.HSL.fromDRGB(drgb, hsl);
		double maxL = hsl[2];
		BufferedImage result = dest;
		if (result == null || 
			result.getWidth() != m.getSizeX() || 
			result.getHeight() != m.getSizeY() || 
			result.getType() != BufferedImage.TYPE_INT_RGB)
			result = new BufferedImage(m.getSizeX(), m.getSizeY(), BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < m.getSizeX(); i++)
			for (int j = 0; j < m.getSizeY(); j++) {
				hsl[2] = MathUtil.mapValue(m.getItem(i, j), minVal, maxVal, 1, maxL);
				ColorConversion.HSL.toDRGB(hsl, drgb);
				result.setRGB(i, j, ColorConversion.RGB.toRGB(drgb));
			}
		return result;
	}

	static abstract class DrawLayer {
		public void setWidth(int imgSize[], int width) {
			imgSize[0] = Math.max(imgSize[0], width);
		}
		
		public abstract int[] calcSize(LayerWorkspace ws, int inputSize[], int imgSize[]);
		public abstract int[] draw(Graphics2D g, LayerWorkspace ws, int inputSize[]);
	}
	
	static class DrawConvolutionLayer extends DrawLayer {
		public int[] calcSize(LayerWorkspace ws, int inputSize[], int imgSize[]) {
			ConvolutionLayer ll = (ConvolutionLayer) ws.getLayer();
			ConvolutionLayer.Workspace wws = (ConvolutionLayer.Workspace) ws;

			Matrix m = ll.kernel;
			setWidth(imgSize, m.getSizeX());
			imgSize[1] += m.getSizeY() + spaceBetweenLayers;

			m = wws.outputError;
			imgSize[1] += m.getSizeY();
			setWidth(imgSize, m.getSizeX());

			return ll.getOutputSize(inputSize);
		}

		public int[] draw(Graphics2D g, LayerWorkspace ws, int inputSize[]) {
			ConvolutionLayer ll = (ConvolutionLayer) ws.getLayer();
			ConvolutionLayer.Workspace wws = (ConvolutionLayer.Workspace) ws;

			Matrix m = ll.kernel;
			BufferedImage bi = toImage(inputBaseColor, 0, Math.max(1, m.max()), m, null);
			g.drawImage(bi, 0, 0, null);
			g.translate(0, m.getSizeY() + spaceBetweenLayers);
			
			m = new Matrix();
			wws.outputError.termAbs(m);
			bi = toImage(errorBaseColor, 0, 1, m, null);
			g.drawImage(bi, 0, 0, null);
			g.translate(0, m.getSizeY());

			return ll.getOutputSize(inputSize);
		}
	}
	
	static class DrawFullyConnectedLayer extends DrawLayer {
		public int[] calcSize(LayerWorkspace ws, int inputSize[], int imgSize[]) {
			FullyConnectedLayer ll = (FullyConnectedLayer) ws.getLayer();
			FullyConnectedLayer.Workspace wws = (FullyConnectedLayer.Workspace) ws;

			Matrix m = ll.weight;
			int width = m.getSizeX();
			imgSize[1] += m.getSizeY() + spaceBetweenLayers;

			m = wws.dW;
			width += spaceBetweenLayers + m.getSizeX();
			setWidth(imgSize, width);
			
			m = wws.outputError;
			imgSize[1] += m.getSizeY();
			setWidth(imgSize, m.getSizeX());

			return ll.getOutputSize(inputSize);
		}

		public int[] draw(Graphics2D g, LayerWorkspace ws, int inputSize[]) {
			FullyConnectedLayer ll = (FullyConnectedLayer) ws.getLayer();
			FullyConnectedLayer.Workspace wws = (FullyConnectedLayer.Workspace) ws;

			Matrix m = ll.weight;
			BufferedImage bi = toImage(inputBaseColor, Math.min(0, m.min()), Math.max(1, m.max()), m, null);
			g.drawImage(bi, 0, 0, null);

			m = wws.dW.makeCopy();
			bi = toImage(errorBaseColor, Math.min(0, m.min()), Math.max(0.01, m.max()), m, null);
			g.drawImage(bi, ll.weight.getSizeX() + spaceBetweenLayers, 0, null);
			g.translate(0, m.getSizeY() + spaceBetweenLayers);
			
			m = new Matrix();
			wws.outputError.termAbs(m);
			bi = toImage(errorBaseColor, 0, 1, m, null);
			g.drawImage(bi, 0, 0, null);
			g.translate(0, m.getSizeY());

			return ll.getOutputSize(inputSize);
		}
	}

	static class DrawDebugLayer extends DrawLayer {
		public int[] calcSize(LayerWorkspace ws, int inputSize[], int imgSize[]) {
			DebugLayer ll = (DebugLayer) ws.getLayer();
			DebugLayer.Workspace wws = (DebugLayer.Workspace) ws;

			Matrix m = wws.stInput.getMaxX();
			int width = m.getSizeX();
			imgSize[1] += m.getSizeY() + spaceBetweenLayers;

			width += spaceBetweenLayers + m.getSizeX();
			setWidth(imgSize, width);
			
			return ll.getOutputSize(inputSize);
		}

		public int[] draw(Graphics2D g, LayerWorkspace ws, int inputSize[]) {
			DebugLayer ll = (DebugLayer) ws.getLayer();
			DebugLayer.Workspace wws = (DebugLayer.Workspace) ws;

			Matrix m = wws.stInput.getMaxX();
			BufferedImage bi = toImage(inputBaseColor, Math.min(0, m.min()), Math.max(1, m.max()), m, null);
			g.drawImage(bi, 0, 0, null);

			m = wws.stError.getMaxX();
			bi = toImage(errorBaseColor, Math.min(0, m.min()), Math.max(0.01, m.max()), m, null);
			g.drawImage(bi, m.getSizeX() + spaceBetweenLayers, 0, null);
			g.translate(0, m.getSizeY() + spaceBetweenLayers);

			return ll.getOutputSize(inputSize);
		}
	}

	static class DrawNetwork extends DrawLayer {
		public int[] calcSize(LayerWorkspace ws, int inputSize[], int imgSize[]) {
			Network.NetWorkSpace wws = (Network.NetWorkSpace) ws;
			int vspace = 0;
			for (int i = 0; i < wws.workspaces.size(); i++) {
				LayerWorkspace ws2 = wws.workspaces.get(i);
				DrawLayer dl = drawMap.get(ws2.getLayer().getClass());
				if (dl != null) {
					imgSize[1] += vspace;
					inputSize = dl.calcSize(ws2, inputSize, imgSize);
					vspace = spaceBetweenLayers;
				} else {
					inputSize = ws2.getLayer().getOutputSize(inputSize);
					vspace = 0;
				}
			}
			return inputSize;
		}

		public int[] draw(Graphics2D g, LayerWorkspace ws, int inputSize[]) {
			Network.NetWorkSpace wws = (Network.NetWorkSpace) ws;
			int vspace = 0;
			for (int i = 0; i < wws.workspaces.size(); i++) {
				LayerWorkspace ws2 = wws.workspaces.get(i);
				DrawLayer dl = drawMap.get(ws2.getLayer().getClass());
				if (dl != null) {
					g.translate(0, vspace);
					inputSize = dl.draw(g, ws2, inputSize);
					vspace = spaceBetweenLayers;
				} else {
					inputSize = ws2.getLayer().getOutputSize(inputSize);
					vspace = 0;
				}
			}
			return inputSize;
		}
	}
	
	static final int spaceBetweenLayers = 10;
	static int inputBaseColor = fixColorLight(0x00B9FB, 0.2);
	static int errorBaseColor = fixColorLight(0xCB4154, 0.2);
	static HashMap<Class<? extends Layer>, DrawLayer> drawMap = new HashMap<>();
	
	static {
		drawMap.put(ConvolutionLayer.class, new DrawConvolutionLayer());
		drawMap.put(FullyConnectedLayer.class, new DrawFullyConnectedLayer());
		drawMap.put(DebugLayer.class, new DrawDebugLayer());
		drawMap.put(Network.class, new DrawNetwork());
	}
	
	public static BufferedImage draw(LayerWorkspace ws, int inputSize[]) {
		DrawLayer dl = drawMap.get(ws.getLayer().getClass());
		int imgSize[] = new int[2];
		dl.calcSize(ws, inputSize, imgSize);
		BufferedImage bi = new BufferedImage(imgSize[0], imgSize[1], BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		dl.draw(g, ws, inputSize);
		return bi;
	}
}
