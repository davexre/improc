package com.slavi.ann.test.v2;

import java.util.ArrayList;
import java.util.List;

import com.slavi.math.matrix.Matrix;

public class Network extends Layer {

	protected List<Layer> layers;
	
	public Network(List<Layer> layers) {
		this.layers = layers;
	}
	
	@Override
	public NetWorkSpace createWorkspace() {
		return new NetWorkSpace();
	}

	@Override
	public void applyWorkspace(Workspace workspace) {
		NetWorkSpace ws = (NetWorkSpace) workspace;
		for (Workspace i : ws.workspaces)
			i.resetEpoch();
	}
	
	protected class NetWorkSpace extends Workspace {
		protected List<Workspace> workspaces;
		
		protected NetWorkSpace() {
			workspaces = new ArrayList<>(layers.size());
			for (Layer layer : layers)
				workspaces.add(layer.createWorkspace());
		}

		@Override
		public Matrix feedForward(Matrix input) {
			for (Workspace workspace : workspaces) {
				input = workspace.feedForward(input);
			}
			return input;
		}

		@Override
		public Matrix backPropagate(Matrix error) {
			for (int i = workspaces.size() - 1; i >= 0; i--) {
				Workspace workspace = workspaces.get(i);
				error = workspace.backPropagate(error);
			}
			return error;
		}

		@Override
		protected void resetEpoch() {
			for (Workspace workspace : workspaces)
				workspace.resetEpoch();
		}
	}
}
