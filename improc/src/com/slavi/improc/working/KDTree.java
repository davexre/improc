package com.slavi.improc.working;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class KDTree {
	int dimensions;

	private double[][] hyperRectangle; // used by getNearestNeighbourhood()

	public KDNode root;
	
	/**
	 * WARNING! Mutates itmes (list is reorder). Mutates the elements in the list, i.e. 
	 * calls setLeft() and setRight() methods of the elements. 
	 * @param items
	 * @param dimensions
	 */
	public KDTree(ArrayList items, int dimensions) {
		this.dimensions = dimensions;
		this.hyperRectangle = new double[dimensions][2];
		this.root = balanceSegment(items, dimensions, 0, items.size() - 1, 0);
	}

	private static int deepSort(ArrayList items, int dimensions, int left, int right, int curDimension, int numberOfUnsuccessfullSorts) {
		if (left > right)
			return -1;
		int midIndex = (left + right) >> 1;

		int segmentEndIndex = right;
		double segmentEndValue = ((KDNode)items.get(segmentEndIndex)).getValue(curDimension);
		
		int segmentStartIndex = right;
		for (; segmentStartIndex >= left; segmentStartIndex--) {
			int minIndex = segmentStartIndex;
			double segmentStartValue = ((KDNode)items.get(segmentStartIndex)).getValue(curDimension);
			
			for (int j = segmentStartIndex - 1; j >= left; j--) {
				double tmp = ((KDNode)items.get(j)).getValue(curDimension);
				if (tmp >= segmentStartValue) {
					segmentStartValue = tmp;
					minIndex = j;
				}
			}
			if (minIndex != segmentStartIndex) {
				Object tmp = items.get(minIndex);
				items.set(minIndex, items.get(segmentStartIndex));
				items.set(segmentStartIndex, tmp);
			}
			if ((segmentStartIndex < midIndex) && (segmentStartValue < segmentEndValue)) {
				break;
			}
			if (segmentStartValue != segmentEndValue) {
				segmentEndValue = segmentStartValue;
				segmentEndIndex = segmentStartIndex;
			}
		}
		segmentStartIndex++;
		
		if (segmentStartIndex == segmentEndIndex)
			return segmentStartIndex;
		if ((left == segmentStartIndex) && (right == segmentEndIndex)) {
			numberOfUnsuccessfullSorts++;
			if (numberOfUnsuccessfullSorts >= dimensions)
				return midIndex;
		} else
			numberOfUnsuccessfullSorts = 0;
		int nextDimension = (curDimension + 1) % dimensions;
		return deepSort(items, dimensions, segmentStartIndex, segmentEndIndex, nextDimension, numberOfUnsuccessfullSorts);
	}
	
	private static KDNode balanceSegment(ArrayList items, int dimensions, int left, int right, int curDimension) {
		if (left > right)
			return null;
		int midIndex = deepSort(items, dimensions, left, right, curDimension, 0);
		double midValue = ((KDNode)items.get(midIndex)).getValue(curDimension);
		int startIndex = midIndex - 1;
		for (; startIndex >= left; startIndex--) 
			if (((KDNode)items.get(startIndex)).getValue(curDimension) != midValue) 
				break;
		startIndex++;
		if (startIndex != midIndex) {
			Object tmp = items.get(midIndex);
			items.set(midIndex, items.get(startIndex));
			items.set(startIndex, tmp);
		}
		int nextDimension = (curDimension + 1) % dimensions;
		KDNode result = (KDNode)items.get(startIndex);
		result.setLeft(balanceSegment(items, dimensions, left, startIndex - 1, nextDimension));
		result.setRight(balanceSegment(items, dimensions, startIndex + 1, right, nextDimension));
		return result;
	}
	
	private double getDistanceSquared(KDNode n1, KDNode n2) {
		double result = 0;
		for (int i = dimensions - 1; i >= 0; i--) {
			double d = n1.getValue(i) - n2.getValue(i);
			result += d * d;
		}
		return result;
	}

	private double getDistanceSquaredToHR(double[][] hr, KDNode target) {
		double result = 0;
		for (int i = dimensions - 1; i >= 0; i--) {
			double d = target.getValue(i);
			if (d < hr[i][0]) {
				d -= hr[i][0];
			} else if (d > hr[i][1]) {
				d -= hr[i][1];
			} else {
				d = 0;
			}
			result += d * d;
		}
		return result;
	}

	private void nearestSegment(NearestNeighbours nearest,
			double[][] hr, KDNode target, KDNode curNode, int dimension) {
		if (curNode == null) 
			return;
		usedSearchSteps++;

		if (target.canFindDistanceToPoint(curNode))
			nearest.add(curNode, getDistanceSquared(target, curNode));
		double curNodeValue = curNode.getValue(dimension);
		int nextDimension = (dimension + 1) % dimensions;

		if (target.getValue(dimension) < curNodeValue) {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][1];
			hr[dimension][1] = curNodeValue;
			nearestSegment(nearest, hr, target, curNode.getLeft(), nextDimension);
			hr[dimension][1] = tmp;
			// Prepare the "further" hyper rectangle
			tmp = hr[dimension][0];
			hr[dimension][0] = curNodeValue;
			// Check the "further" hyper rectangle:
			//   if capacity is not reached OR
			//   if distance from target to "further" hyper rectangle is smaller 
			//      than the maximum of the currently found neighbours
			if ((nearest.size() < nearest.getCapacity()) ||
					(getDistanceSquaredToHR(hr, target) < nearest.getValue(nearest.size() - 1))) 
				nearestSegment(nearest, hr, target, curNode.getRight(), nextDimension);
			// Restore the hyper rectangle
			hr[dimension][0] = tmp;
		} else {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][0];
			hr[dimension][0] = curNodeValue;			
			nearestSegment(nearest, hr, target, curNode.getRight(), nextDimension);
			hr[dimension][0] = tmp;
			// Prepare the "further" hyper rectangle
			tmp = hr[dimension][1];
			hr[dimension][1] = curNodeValue;
			// Check the "further" hyper rectangle:
			//   if capacity is not reached OR
			//   if distance from target to "further" hyper rectangle is smaller 
			//      than the maximum of the currently found neighbours
			if ((nearest.size() < nearest.getCapacity()) ||
					(getDistanceSquaredToHR(hr, target) < nearest.getValue(nearest.size() - 1))) 
				nearestSegment(nearest, hr, target, curNode.getLeft(), nextDimension);
			// Restore the hyper rectangle
			hr[dimension][1] = tmp;
		}
	}
	
	public NearestNeighbours getNearestNeighbours(KDNode target, int maxNeighbours) {
		NearestNeighbours result = new NearestNeighbours(target, maxNeighbours);
		for (int i = dimensions - 1; i >= 0; i--) {
			hyperRectangle[i][0] = Double.MIN_VALUE;
			hyperRectangle[i][1] = Double.MAX_VALUE;
		}
		usedSearchSteps = 0;
		nearestSegment(result, hyperRectangle, target, root, 0);
		if (usedSearchSteps > maxUsedSearchSteps)
			maxUsedSearchSteps = usedSearchSteps;
		return result;
	}

	public int searchSteps;
	public int usedSearchSteps;
	public int maxUsedSearchSteps = 0;

	private void nearestSegmentBBF(NearestNeighbours nearest,
			double[][] hr, KDNode target, KDNode curNode, int dimension) {
		if (curNode == null) 
			return;
		usedSearchSteps++;

		if (target.canFindDistanceToPoint(curNode))
			nearest.add(curNode, getDistanceSquared(target, curNode));
		double curNodeValue = curNode.getValue(dimension);
		int nextDimension = (dimension + 1) % dimensions;

		if (target.getValue(dimension) < curNodeValue) {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][1];
			hr[dimension][1] = curNodeValue;
			nearestSegmentBBF(nearest, hr, target, curNode.getLeft(), nextDimension);
			hr[dimension][1] = tmp;

			if (searchSteps > 0) {
				searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = hr[dimension][0];
				hr[dimension][0] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(hr, target) < nearest.getValue(nearest.size() - 1))) 
					nearestSegmentBBF(nearest, hr, target, curNode.getRight(), nextDimension);
				// Restore the hyper rectangle
				hr[dimension][0] = tmp;
			}
		} else {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][0];
			hr[dimension][0] = curNodeValue;			
			nearestSegmentBBF(nearest, hr, target, curNode.getRight(), nextDimension);
			hr[dimension][0] = tmp;

			if (searchSteps > 0) {
				searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = hr[dimension][1];
				hr[dimension][1] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(hr, target) < nearest.getValue(nearest.size() - 1))) 
					nearestSegmentBBF(nearest, hr, target, curNode.getLeft(), nextDimension);
				// Restore the hyper rectangle
				hr[dimension][1] = tmp;
			}
		}
	}
	
	public NearestNeighbours getNearestNeighboursBBF(KDNode target, int maxNeighbours, int maxSearchSteps) {
		NearestNeighbours result = new NearestNeighbours(target, maxNeighbours);
		for (int i = dimensions - 1; i >= 0; i--) {
			hyperRectangle[i][0] = Double.MIN_VALUE;
			hyperRectangle[i][1] = Double.MAX_VALUE;
		}
		usedSearchSteps = 0;
		this.searchSteps = maxSearchSteps; 
		nearestSegmentBBF(result, hyperRectangle, target, root, 0);
		if (usedSearchSteps > maxUsedSearchSteps)
			maxUsedSearchSteps = usedSearchSteps;
		return result;
	}

	private void nearestSegmentBBFOriginal(NearestNeighbours nearest,
			double[][] hr, KDNode target, KDNode curNode, int dimension) {
		if (curNode == null) 
			return;
		usedSearchSteps++;

		if (target.canFindDistanceToPoint(curNode))
			nearest.add(curNode, getDistanceSquared(target, curNode));
		double curNodeValue = curNode.getValue(dimension);
		int nextDimension = (dimension + 1) % dimensions;

		if (target.getValue(dimension) < curNodeValue) {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][1];
			hr[dimension][1] = curNodeValue;
			nearestSegmentBBFOriginal(nearest, hr, target, curNode.getLeft(), nextDimension);
			hr[dimension][1] = tmp;

			if (searchSteps > 0) {
				searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = hr[dimension][0];
				hr[dimension][0] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(hr, target) < nearest.getValue(nearest.size() - 1))) 
					nearestSegmentBBFOriginal(nearest, hr, target, curNode.getRight(), nextDimension);
				// Restore the hyper rectangle
				hr[dimension][0] = tmp;
			}
		} else {
			// Prepare and check the "nearer" hyper rectangle
			double tmp = hr[dimension][0];
			hr[dimension][0] = curNodeValue;			
			nearestSegmentBBFOriginal(nearest, hr, target, curNode.getRight(), nextDimension);
			hr[dimension][0] = tmp;

			if (searchSteps > 0) {
				searchSteps--;
				// Prepare the "further" hyper rectangle
				tmp = hr[dimension][1];
				hr[dimension][1] = curNodeValue;
				// Check the "further" hyper rectangle:
				//   if capacity is not reached OR
				//   if distance from target to "further" hyper rectangle is smaller 
				//      than the maximum of the currently found neighbours
				if ((nearest.size() < nearest.getCapacity()) ||
						(getDistanceSquaredToHR(hr, target) < nearest.getValue(nearest.size() - 1))) 
					nearestSegmentBBFOriginal(nearest, hr, target, curNode.getLeft(), nextDimension);
				// Restore the hyper rectangle
				hr[dimension][1] = tmp;
			}
		}
	}
	
	public NearestNeighbours getNearestNeighboursBBFOriginal(KDNode target, int maxNeighbours, int maxSearchSteps) {
		NearestNeighbours result = new NearestNeighbours(target, maxNeighbours);
		for (int i = dimensions - 1; i >= 0; i--) {
			hyperRectangle[i][0] = Double.MIN_VALUE;
			hyperRectangle[i][1] = Double.MAX_VALUE;
		}
		usedSearchSteps = 0;
		this.searchSteps = maxSearchSteps; 
		nearestSegmentBBFOriginal(result, hyperRectangle, target, root, 0);
		if (usedSearchSteps > maxUsedSearchSteps)
			maxUsedSearchSteps = usedSearchSteps;
		return result;
	}
}
