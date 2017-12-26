package com.slavi.ann.test.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.slavi.math.matrix.Matrix;

public class Network extends Layer implements Iterable<Layer>{

	protected List<Layer> layers;
	
	public Network(List<Layer> layers) {
		this.layers = Collections.unmodifiableList(layers);
	}

	public Network(Layer ... layers) {
		this.layers = Collections.unmodifiableList(Arrays.asList(layers));
	}

	public int size() {
		return layers.size();
	}

	public Layer get(int index) {
		return layers.get(index);
	}

	public Iterator<Layer> iterator() {
		return layers.iterator();
	}

	public int[] getOutputSize(int inputSize[]) {
		for (Layer l : layers) {
			inputSize = l.getOutputSize(inputSize);
		}
		return inputSize;
	}

	@Override
	public NetWorkSpace createWorkspace() {
		return new NetWorkSpace();
	}

	@Override
	public void applyWorkspaces(List<LayerWorkspace> workspaces) {
		List<LayerWorkspace> tmp = new ArrayList<>(workspaces.size());
		for (int l = 0; l < layers.size(); l++) {
			for (LayerWorkspace i : workspaces) {
				NetWorkSpace ws = (NetWorkSpace) i;
				tmp.add(ws.workspaces.get(l));
			}
			layers.get(l).applyWorkspaces(tmp);
			tmp.clear();
		}
		for (LayerWorkspace i : workspaces) {
			NetWorkSpace ws = (NetWorkSpace) i;
			ws.resetEpoch();
		}
	}
	
	public class NetWorkSpace extends LayerWorkspace {
		public List<LayerWorkspace> workspaces;
		
		protected NetWorkSpace() {
			workspaces = new ArrayList<>(layers.size());
			for (Layer layer : layers)
				workspaces.add(layer.createWorkspace());
		}

		@Override
		public Matrix feedForward(Matrix input) {
			for (LayerWorkspace workspace : workspaces) {
				input = workspace.feedForward(input);
			}
			return input;
		}

		@Override
		public Matrix backPropagate(Matrix error) {
			for (int i = workspaces.size() - 1; i >= 0; i--) {
				LayerWorkspace workspace = workspaces.get(i);
				error = workspace.backPropagate(error);
			}
			return error;
		}

		@Override
		protected void resetEpoch() {
		}
	}
}
