package com.slavi.ann.test.v2;

import java.util.ArrayList;
import java.util.List;

public class NetworkBuilder {
	List<Layer> layers = new ArrayList<>();
	
	int lastSize[];
	
	public NetworkBuilder(int inputSizeX, int inputSizeY) {
		lastSize = new int[] { inputSizeX, inputSizeY };
	}
	
	public NetworkBuilder addConvolutionLayer(int kernelSize) {
		Layer l = new ConvolutionLayer(kernelSize, kernelSize, 1);
		lastSize = l.getOutputSize(lastSize);
		layers.add(l);
		return this;
	}
	
	public NetworkBuilder addConvolutionSameSizeLayer(int kernelSize) {
		Layer l = new ConvolutionSameSizeLayer(kernelSize, kernelSize, 1);
		layers.add(l);
		return this;
	}
	
	public NetworkBuilder addConvolutionWithStrideLayer(int kernelSize, int stride) {
		Layer l = new ConvolutionWithStrideLayer(kernelSize, kernelSize, stride, stride, 1);
		lastSize = l.getOutputSize(lastSize);
		layers.add(l);
		return this;
	}
	
	public NetworkBuilder addSubsamplingAvgLayer(int kernelSize) {
		Layer l = new SubsamplingAvgLayer(kernelSize, kernelSize);
		lastSize = l.getOutputSize(lastSize);
		layers.add(l);
		return this;
	}

	public NetworkBuilder addSubsamplingMaxLayer(int kernelSize) {
		Layer l = new SubsamplingMaxLayer(kernelSize, kernelSize);
		lastSize = l.getOutputSize(lastSize);
		layers.add(l);
		return this;
	}

	public NetworkBuilder addFullyConnectedLayer(int outputSize) {
		Layer l = new FullyConnectedLayer(lastSize[0] * lastSize[1], outputSize, 1);
		lastSize = l.getOutputSize(lastSize);
		layers.add(l);
		return this;
	}
	
	public Network build() {
		return new Network(layers);
	}
}
