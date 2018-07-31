package com.slavi.ann.test.v2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import com.slavi.ann.test.v2.Layer.LayerParameters;
import com.slavi.ann.test.v2.Layer.LayerWorkspace;
import com.slavi.ann.test.v2.activation.DebugLayer;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.ColorConversion;
import com.slavi.util.MatrixUtil;

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

	static abstract class DrawLayer {
		public void setWidth(int imgSize[], int width) {
			imgSize[0] = Math.max(imgSize[0], width);
		}

		public abstract LayerParameters calcSize(LayerWorkspace ws, LayerParameters inputLayerParameters, int imgSize[]);
		public abstract LayerParameters draw(Graphics2D g, LayerWorkspace ws, LayerParameters inputLayerParameters);
	}

	static class DrawConvolutionLayer extends DrawLayer {
		public LayerParameters calcSize(LayerWorkspace ws, LayerParameters inputLayerParameters, int imgSize[]) {
			ConvolutionLayer ll = (ConvolutionLayer) ws.getLayer();
			ConvolutionLayer.Workspace wws = (ConvolutionLayer.Workspace) ws;

			Matrix m = ll.kernel;
			setWidth(imgSize, m.getSizeX());
			imgSize[1] += m.getSizeY() + spaceBetweenLayers;

			m = wws.outputError;
			imgSize[1] += m.getSizeY();
			setWidth(imgSize, m.getSizeX());

			return ll.getLayerParams(inputLayerParameters);
		}

		public LayerParameters draw(Graphics2D g, LayerWorkspace ws, LayerParameters inputLayerParameters) {
			ConvolutionLayer ll = (ConvolutionLayer) ws.getLayer();
			ConvolutionLayer.Workspace wws = (ConvolutionLayer.Workspace) ws;

			Matrix m = ll.kernel;
			BufferedImage bi = MatrixUtil.toImage(m, 0, Math.max(1, m.max()), inputBaseColor, null);
			g.drawImage(bi, 0, 0, null);
			g.translate(0, m.getSizeY() + spaceBetweenLayers);

			m = new Matrix();
			wws.outputError.termAbs(m);
			bi = MatrixUtil.toImage(m, 0, 1, errorBaseColor, null);
			g.drawImage(bi, 0, 0, null);
			g.translate(0, m.getSizeY());

			return ll.getLayerParams(inputLayerParameters);
		}
	}

	static class DrawFullyConnectedLayer extends DrawLayer {
		public LayerParameters calcSize(LayerWorkspace ws, LayerParameters inputLayerParameters, int imgSize[]) {
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

			return ll.getLayerParams(inputLayerParameters);
		}

		public LayerParameters draw(Graphics2D g, LayerWorkspace ws, LayerParameters inputLayerParameters) {
			FullyConnectedLayer ll = (FullyConnectedLayer) ws.getLayer();
			FullyConnectedLayer.Workspace wws = (FullyConnectedLayer.Workspace) ws;

			Matrix m = ll.weight;
			BufferedImage bi = MatrixUtil.toImage(m, Math.min(0, m.min()), Math.max(1, m.max()), inputBaseColor, null);
			g.drawImage(bi, 0, 0, null);

			m = wws.dW.makeCopy();
			bi = MatrixUtil.toImage(m, Math.min(0, m.min()), Math.max(0.01, m.max()), errorBaseColor, null);
			g.drawImage(bi, ll.weight.getSizeX() + spaceBetweenLayers, 0, null);
			g.translate(0, m.getSizeY() + spaceBetweenLayers);

			m = new Matrix();
			wws.outputError.termAbs(m);
			bi = MatrixUtil.toImage(m, 0, 1, errorBaseColor, null);
			g.drawImage(bi, 0, 0, null);
			g.translate(0, m.getSizeY());

			return ll.getLayerParams(inputLayerParameters);
		}
	}

	static class DrawDebugLayer extends DrawLayer {
		public LayerParameters calcSize(LayerWorkspace ws, LayerParameters inputLayerParameters, int imgSize[]) {
			DebugLayer ll = (DebugLayer) ws.getLayer();
			DebugLayer.Workspace wws = (DebugLayer.Workspace) ws;

			Matrix m = wws.stInput.getMaxX();
			int width = m.getSizeX();
			imgSize[1] += m.getSizeY() + spaceBetweenLayers;

			width += spaceBetweenLayers + m.getSizeX();
			setWidth(imgSize, width);

			return ll.getLayerParams(inputLayerParameters);
		}

		public LayerParameters draw(Graphics2D g, LayerWorkspace ws, LayerParameters inputLayerParameters) {
			DebugLayer ll = (DebugLayer) ws.getLayer();
			DebugLayer.Workspace wws = (DebugLayer.Workspace) ws;

			Matrix m = wws.stInput.getMaxX();
			BufferedImage bi = MatrixUtil.toImage(m, Math.min(0, m.min()), Math.max(1, m.max()), inputBaseColor, null);
			g.drawImage(bi, 0, 0, null);

			m = wws.stError.getMaxX();
			bi = MatrixUtil.toImage(m, Math.min(0, m.min()), Math.max(0.01, m.max()), errorBaseColor, null);
			g.drawImage(bi, m.getSizeX() + spaceBetweenLayers, 0, null);
			g.translate(0, m.getSizeY() + spaceBetweenLayers);

			return ll.getLayerParams(inputLayerParameters);
		}
	}

	static class DrawNetwork extends DrawLayer {
		public LayerParameters calcSize(LayerWorkspace ws, LayerParameters inputLayerParameters, int imgSize[]) {
			Network.NetWorkSpace wws = (Network.NetWorkSpace) ws;
			int vspace = 0;
			for (int i = 0; i < wws.workspaces.size(); i++) {
				LayerWorkspace ws2 = wws.workspaces.get(i);
				DrawLayer dl = drawMap.get(ws2.getLayer().getClass());
				if (dl != null) {
					imgSize[1] += vspace;
					inputLayerParameters = dl.calcSize(ws2, inputLayerParameters, imgSize);
					vspace = spaceBetweenLayers;
				} else {
					inputLayerParameters = ws2.getLayer().getLayerParams(inputLayerParameters);
					vspace = 0;
				}
			}
			return inputLayerParameters;
		}

		public LayerParameters draw(Graphics2D g, LayerWorkspace ws, LayerParameters inputLayerParameters) {
			Network.NetWorkSpace wws = (Network.NetWorkSpace) ws;
			int vspace = 0;
			for (int i = 0; i < wws.workspaces.size(); i++) {
				LayerWorkspace ws2 = wws.workspaces.get(i);
				DrawLayer dl = drawMap.get(ws2.getLayer().getClass());
				if (dl != null) {
					g.translate(0, vspace);
					inputLayerParameters = dl.draw(g, ws2, inputLayerParameters);
					vspace = spaceBetweenLayers;
				} else {
					inputLayerParameters = ws2.getLayer().getLayerParams(inputLayerParameters);
					vspace = 0;
				}
			}
			return inputLayerParameters;
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
		LayerParameters inputLayerParameters = new LayerParameters(inputSize, 0);
		dl.calcSize(ws, inputLayerParameters, imgSize);
		BufferedImage bi = new BufferedImage(imgSize[0], imgSize[1], BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		dl.draw(g, ws, inputLayerParameters);
		return bi;
	}
}
