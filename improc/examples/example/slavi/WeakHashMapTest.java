package example.slavi;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.WeakHashMap;

public class WeakHashMapTest {

	public static class DummyKey {
		public String name;
		public DummyKey(String name) {
			this.name = name;
		}
		
		protected void finalize() {
			System.out.println("Finalize " + name);
		}
	}
	
	WeakHashMap<DummyKey, String> map = new WeakHashMap<WeakHashMapTest.DummyKey, String>();
	
	private void doIt() {
		DummyKey k1 = new DummyKey("k1");
		map.put(k1, "k1");
		
		DummyKey k2 = new DummyKey("k2");
		map.put(k2, "k2");
		
		DummyKey k3 = new DummyKey("k3");
		map.put(k3, "k3");
		
		System.out.println(map.size());
		
		k1 = null;
		k2 = null;
		
		System.gc();
		
		ArrayList<byte[]> buffs = new ArrayList<byte[]>(500);
		MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		long useMemory = (long) ((memoryUsage.getMax() - memoryUsage.getUsed()) * 0.5);
		
		while (useMemory > 0) {
			int doUse = 0xffffff;
			if (doUse > useMemory)
				doUse = (int) useMemory;
			byte buf[] = new byte[doUse];
			useMemory -= doUse;
			buffs.add(buf);
		}

		System.out.println(map.size());
	}

	public static void main(String[] args) {
		new WeakHashMapTest().doIt();
		System.out.println("Done.");
	}
}
