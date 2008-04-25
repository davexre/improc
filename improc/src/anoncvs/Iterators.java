package anoncvs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Iterators {

	public interface MyDataInterface {
		public double getValue();
	}
	
	public static class MyData implements MyDataInterface {
		
		double value;
		
		public MyData(double value) {
			this.value = value;
		}
		
		public double getValue() {
			return value;
		}
	}
	
	public static List<MyDataInterface> makeData() {
		List<MyDataInterface> data = new ArrayList<MyDataInterface>();
		for (int i = 1; i <= 5; i++) {
			data.add(new MyData(i));
		}
		return data;
	}

	public static void process(Iterable<MyDataInterface> data) {
		double sum = 0.0;
		for (MyDataInterface i : data) {
			sum += i.getValue();
		}
		System.out.println("sum=" + sum);
	}
	
	public static class MyDataCollection implements Iterable<MyDataInterface> {
		List<MyDataInterface> data;
		int size;
		
		class PrivateIterator implements Iterator<MyDataInterface>, MyDataInterface {
			int current = size;

			public boolean hasNext() {
				return current > 0;
			}

			public MyDataInterface next() {
				current--;
				return this;
			}

			public void remove() {
			}

			public double getValue() {
				return data.get(current).getValue();
			}
		}
		
		public MyDataCollection(List<MyDataInterface> data) {
			this.data = data;
			this.size = data.size();
		}
		
		public Iterator<MyDataInterface> iterator() {
			return new PrivateIterator();
		}
	}
	
	public static void main(String[] args) {
		List<MyDataInterface> data = makeData();
		MyDataCollection col = new MyDataCollection(data);
		process(col);
	}
}
