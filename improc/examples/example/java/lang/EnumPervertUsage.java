package example.java.lang;

public class EnumPervertUsage {

	public enum MyEnum {
		ONE(1), TWO(2), THREE(3);
		
		public int valueGloballyModifiable;
		
		public final int value;
		
		MyEnum(int i) {
			valueGloballyModifiable = value = i;
		}
		
		public String toString() {
			return name() + "(" + valueGloballyModifiable + ", " + value + ")";
		}
	}
	
	public static void main(String[] args) {
		for (MyEnum i : MyEnum.values()) {
			System.out.println(i);
		}
		System.out.println("---------");
		for (MyEnum i : MyEnum.values()) {
			i.valueGloballyModifiable++;
		}
		for (MyEnum i : MyEnum.values()) {
			System.out.println(i);
		}
	}
}
