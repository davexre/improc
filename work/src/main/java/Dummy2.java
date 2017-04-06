import com.slavi.util.Marker;

public class Dummy2 {
	public static double clipValue(double value, double min, double max) {
		return Math.min(max, Math.max(min, value));
	}
	
	public static double clipValue2(double value, double min, double max) {
		return
			value >= max ? max :
			value <= min ? min : value;
	}

	double a = 1.23;
	void doIt() throws Exception {
		arr = new Integer[148_116];
//		for (int i = 0; i < arr.length; i++)
//			arr[i] = new Integer(1_000_000 + i);
	}

	Integer arr[];
	
	public static void main(String[] args) throws Exception {
		Dummy2 d = new Dummy2();
		Marker.mark("main");
		d.doIt();
		Marker.release();
		System.out.println("Done.");
	}
}
