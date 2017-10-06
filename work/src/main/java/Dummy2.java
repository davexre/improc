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

	public static int hash(String value) {
        int h = 0;
        if (h == 0 && value.length() > 0) {
            for (int i = 0; i < value.length(); i++) {
                h = 31 * h + value.charAt(i);
            }
        }
        return h;
	}
	
	
	public static void main(String[] args) throws Exception {
		new Dummy2().doIt();
		System.out.println("Done.");
	}
}
