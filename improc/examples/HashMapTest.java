import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import com.slavi.util.tree.KDTree;

public class HashMapTest {

	static class A {
		public double x, y;

		public A(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public int hashCode() {
			long bits = java.lang.Double.doubleToLongBits(x);
			bits ^= java.lang.Double.doubleToLongBits(y) * 31;
			return (((int) bits) ^ ((int) (bits >> 32)));
//			return (int) java.lang.Double.doubleToLongBits(x);
		}

		public boolean equals(Object obj) {
			if (obj instanceof A) {
				A p2d = (A) obj;
				return (x == p2d.x) && (y == p2d.y);
			}
			return super.equals(obj);
		}
	}

	public static void main(String[] args) {
		HashMap<A, String> map = new HashMap<HashMapTest.A, String>();
		ArrayList<A> points = new ArrayList<HashMapTest.A>();
		
		for (int x = 0; x < 10000; x++) {
			for (int y = 0; y < 10000; y++) {
				A a = new A(x, y);
				String id = x + ":" + y;
				map.put(a, id);
				points.add(a);
			}
		}
		
		KDTree<A> tree = new KDTree<HashMapTest.A>(2, points, true) {
			public boolean canFindDistanceBetween(A fromNode, A toNode) {
				return true;
			}

			public double getValue(A node, int dimensionIndex) {
				return dimensionIndex == 0 ? node.x : node.y;
			}
		};
		
		A a = new A(1, 2);
		A b = new A(1, 3);
		
		map.put(a, "A");
		map.put(b, "B");
		
		System.out.println(map.get(a));
		System.out.println(map.get(b));
	}
}
