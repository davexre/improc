package com.slavi.ann.test.v2;

import java.util.ArrayList;
import java.util.List;

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

	int lastSize[];

	public NetworkBuilder(int inputSizeX, int inputSizeY) {
		lastSize = new int[] { inputSizeX, inputSizeY };
	}

	public NetworkBuilder addLayer(Layer l) {
		lastSize = l.getOutputSize(lastSize);
		layers.add(l);
		return this;
	}

	public int[] getCurrentOutputSize() {
		return lastSize;
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

	public NetworkBuilder addSigmoidLayer() {
		return addLayer(new SigmoidLayer());
	}

	public NetworkBuilder addReLULayer() {
		return addLayer(new ReLULayer());
	}

	public Network build() {
		return new Network(layers);
	}
}
