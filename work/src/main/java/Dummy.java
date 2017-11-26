import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Dummy {
	void doIt() throws Exception {
		ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();
		
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 10; i++)
			list.add(i);
		
		list.stream().filter(c -> !map.contains(c)).forEach(c -> {
			for (int i = 0; i < 5; i++)
				map.put(c + i, Integer.toString(c) + " " + Integer.toString(i));
		});
		
		System.out.println(map);
	}

	public static void main(String[] args) throws Exception {
//		new Dummy().doIt();
		System.out.println(Math.ceil(-5.0/4.0));
		System.out.println("Done.");
	}
}
