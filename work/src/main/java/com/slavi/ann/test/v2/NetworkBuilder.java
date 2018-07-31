package com.slavi.ann.test.v2;

import java.util.ArrayList;
import java.util.List;

import com.slavi.ann.test.v2.Layer.LayerParameters;
import com.slavi.ann.test.v2.activation.ConstScaleAndBiasLayer;
import com.slavi.ann.test.v2.activation.DebugLayer;
import com.slavi.ann.test.v2.activation.ReLULayer;
import com.slavi.ann.test.v2.activation.SigmoidLayer;
import com.slavi.ann.test.v2.connection.ConvolutionLayer;
import com.slavi.ann.test.v2.connection.ConvolutionSameSizeLayer;
import com.slavi.ann.test.v2.connection.ConvolutionWithStrideLayer;
import com.slavi.ann.test.v2.connection.FullyConnectedLayer;
import com.slavi.ann.test.v2.connection.SubsamplingAvgLayer;
import com.slavi.ann.test.v2.connection.SubsamplingMaxLayer;

public class NetworkBuilder {
	List<Layer> layers = new ArrayList<>();

	int inputSize[];
	LayerParameters lastParams;

	public NetworkBuilder(int inputSizeX, int inputSizeY) {
		inputSize = new int[] { inputSizeX, inputSizeY };
		lastParams = new LayerParameters(
				new int[] { inputSizeX, inputSizeY },
				0);
	}

	public NetworkBuilder addLayer(Layer l) {
		lastParams = l.getLayerParams(lastParams);
		layers.add(l);
		return this;
	}

	public String describe() {
		StringBuilder r = new StringBuilder();
		LayerParameters tmpParams = new LayerParameters(
				new int[] { inputSize[0], inputSize[1] },
				0);
		for (int i = 0; i < layers.size(); i++) {
			Layer l = layers.get(i);
			r.append(i).append(": ")
				.append(tmpParams.outputSize[0]).append(':')
				.append(tmpParams.outputSize[1]).append(' ')
				.append(l.getClass().getSimpleName())
				.append('\n');
			tmpParams = l.getLayerParams(tmpParams);
		}
		return r.toString();
	}

	public LayerParameters getLayerParameters() {
		return lastParams;
	}

	public NetworkBuilder addConvolutionLayer(int kernelSize) {
		return addLayer(new ConvolutionLayer(kernelSize, kernelSize, 1));
	}

	public NetworkBuilder addConvolutionSameSizeLayer(int kernelSize) {
		return addLayer(new ConvolutionSameSizeLayer(kernelSize, kernelSize, 1));
	}

	public NetworkBuilder addConvolutionWithStrideLayer(int kernelSize, int stride) {
		return addLayer(new ConvolutionWithStrideLayer(kernelSize, kernelSize, stride, stride, 1));
	}

	public NetworkBuilder addSubsamplingAvgLayer(int kernelSize) {
		return addLayer(new SubsamplingAvgLayer(kernelSize, kernelSize));
	}

	public NetworkBuilder addSubsamplingMaxLayer(int kernelSize) {
		return addLayer(new SubsamplingMaxLayer(kernelSize, kernelSize));
	}

	public NetworkBuilder addFullyConnectedLayer(int outputSize) {
		return addLayer(new FullyConnectedLayer(lastParams.outputSize[0] * lastParams.outputSize[1], outputSize, 1));
	}

	public NetworkBuilder addConstScaleAndBiasLayer() {
		int outputSize = lastParams.outputSize[0] * lastParams.outputSize[1];
		return addLayer(new ConstScaleAndBiasLayer(5.0 / outputSize, 0));
	}

	public NetworkBuilder addConstScaleAndBiasLayer(double scale, double bias) {
		return addLayer(new ConstScaleAndBiasLayer(scale, bias));
	}

	public NetworkBuilder addDebugLayer(String name) {
		return addLayer(new DebugLayer(name));
	}

	public NetworkBuilder addDebugLayer(String name, int style) {
		return addLayer(new DebugLayer(name, style));
	}

	public NetworkBuilder addDebugLayer(String name, int inputStyle, int errorStyle) {
		return addLayer(new DebugLayer(name, inputStyle, errorStyle));
	}

	public NetworkBuilder addSigmoidLayer() {
		return addLayer(new SigmoidLayer());
	}

	public NetworkBuilder addReLULayer() {
		return addLayer(new ReLULayer());
	}

	public Layer getLastLayer() {
		return layers.size() > 0 ? layers.get(layers.size() - 1) : null;
	}

	public Network build() {
		return new Network(layers);
	}
}
