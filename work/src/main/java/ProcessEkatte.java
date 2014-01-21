import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


public class ProcessEkatte {
	void doIt() throws Exception {
		InputStream is = getClass().getResourceAsStream("EKATTE.txt");
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		String line = r.readLine();
		while (!"".equals(line = r.readLine())) {
			StringTokenizer st = new StringTokenizer(line);
			String ekatte = st.nextToken();
			ekatte = ekatte.substring(ekatte.length() - 5);
			//String type = st.nextToken();
			int num = st.countTokens() - 1;
			String name = "";
			String prefix = "";
			for (int i = 0; i < num; i++) {
				name += prefix + st.nextToken();
				prefix = " ";
			}
			//System.out.println(ekatte + "\t" + type + "\t" + name + "\t" + st.nextToken() + "\t" + st.nextToken());
			System.out.println(ekatte + "\t" + name + "\t" + st.nextToken());
		}
	}

	public static void main(String[] args) throws Exception {
		new ProcessEkatte().doIt();
		System.out.println("Done.");
	}
}
