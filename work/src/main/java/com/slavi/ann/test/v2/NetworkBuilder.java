package com.slavi.ann.test.v2;

import java.util.ArrayList;
import java.util.List;

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

	public final int inputSize[];
	int lastSize[];

	public NetworkBuilder(int inputSizeX, int inputSizeY) {
		inputSize = new int[] { inputSizeX, inputSizeY };
		lastSize = new int[] { inputSizeX, inputSizeY };
	}

	public NetworkBuilder addLayer(Layer l) {
		lastSize = l.getOutputSize(lastSize);
		layers.add(l);
		return this;
	}

	public String describe() {
		StringBuilder r = new StringBuilder();
		int size[] = new int[] { inputSize[0], inputSize[1] };
		int numUnknowns = 0;
		for (int i = 0; i < layers.size(); i++) {
			Layer l = layers.get(i);
			int params = l.getNumAdjustableParams();
			r.append(i).append(": ")
				.append(size[0]).append(':').append(size[1]).append(' ')
				.append(l.getClass().getSimpleName())
				.append(" (").append(params).append(")")
				.append('\n');
			size = l.getOutputSize(size);
			numUnknowns += params;
		}
		r.append("Total params:").append(numUnknowns);
		return r.toString();
	}

	public int[] getCurrentOutputSize() {
		return lastSize;
	}

	public NetworkBuilder addConvolutionLayer(int kernelSizeX, int kernelSizeY) {
		return addLayer(new ConvolutionLayer(kernelSizeX, kernelSizeY, 1));
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
		return addLayer(new FullyConnectedLayer(lastSize[0] * lastSize[1], outputSize, 1));
	}

	public NetworkBuilder addConstScaleAndBiasLayer() {
		int outputSize = lastSize[0] * lastSize[1];
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
