package com.slavi.improc.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.slavi.improc.KeyPoint;
import com.slavi.improc.KeyPointList;
import com.slavi.improc.KeyPointPairBigTree;
import com.slavi.util.tree.KDTree;
import com.slavi.util.ui.SwtUtl;

public class FindImagePairsInBigTree implements Callable<Void> {

	KeyPointPairBigTree bt;
	
	public FindImagePairsInBigTree(KeyPointPairBigTree bt) {
		this.bt = bt;
	}
	
	static class pair {
		KeyPointList from, to;
		int count;
		String id;
		
		public pair(KeyPointList from, KeyPointList to) {
			this.from = from;
			this.to = to;
			this.count = 0;
			this.id = id(from, to);
		}
		
		public static String id(KeyPointList from, KeyPointList to) {
			String i1 = from.imageFileStamp.getFile().getName();
			String i2 = to.imageFileStamp.getFile().getName();
			if (i1.compareTo(i2) < 0)
				return i1 + "\t" + i2; 
			return i2 + "\t" + i1;
		}
	}
	
	public Void call() throws Exception {
		HashMap<String, pair> map = new HashMap<String, pair>();
		
		int searchSteps = (int) (Math.max(130.0, (Math.log(bt.kdtree.getSize()) / Math.log (1000.0)) * 130.0));
		int count = 0;
		String dummy = "/" + Integer.toString(bt.kdtree.getSize());
		for (KeyPoint kp : bt.kdtree) {
			count++;
			SwtUtl.activeWaitDialogSetStatus(Integer.toString(count) + dummy, count);
			KDTree.NearestNeighbours<KeyPoint> nnlst = bt.kdtree.getNearestNeighboursBBF(kp, 2, searchSteps);
			if (nnlst.size() < 2)
				continue;
			if (nnlst.getDistanceToTarget(0) > nnlst.getDistanceToTarget(1) * 0.6) {
				continue;
			}
			KeyPoint kp2 = nnlst.getItem(0);
			String id = pair.id(kp.keyPointList, kp2.keyPointList);
			pair p = map.get(id);
			if (p == null) {
				p = new pair(kp.keyPointList, kp2.keyPointList);
				map.put(id, p);
			}
			p.count++;
		}
		
		ArrayList<pair> lst = new ArrayList<pair>();
		for (pair p : map.values()) {
			lst.add(p);
		}
		Collections.sort(lst, new Comparator<pair>() {
			public int compare(pair o1, pair o2) {
				return o1.id.compareTo(o2.id);
			}
		});
		for (pair p : lst) {
			System.out.println(p.count + "\t" + p.from.kdtree.getSize() + "\t" + p.to.kdtree.getSize() + "\t" + p.id);
		}
		return null;
	}
}
