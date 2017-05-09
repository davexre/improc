package com.kattis.solution2;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Solution2 {
	Set<Integer> primes = new HashSet<>();
	int lastPrime;
	
	int getPrimeDivisor(int val) {
		for (int prime : primes) {
			if (val % prime == 0)
				return prime;
		}
		return 0;
	}
	
	int nextPrime() {
		while (true) {
			lastPrime++;
			if (getPrimeDivisor(lastPrime) == 0)
				break;
		}
		primes.add(lastPrime);
		return lastPrime;
	}
	
	void primeFactors(int val, ArrayList<Integer> factors) {
		factors.clear();
		factors.add(val);
		int index = 0;
		while (index < factors.size()) {
			int cur = factors.get(index);
			if (cur == 1 || primes.contains(cur)) {
				index++;
				continue;
			}
			factors.remove(index);
			while (true) {
				int prime = getPrimeDivisor(cur);
				if (prime != 0) {
					factors.add(prime);
					factors.add(cur / prime);
					break;
				}
				nextPrime();
			}
		}
	}
	
	boolean isPrime(int val) {
		if (primes.contains(val))
			return true;
		while (lastPrime < val) {
			nextPrime();
			if (primes.contains(val))
				return true;
		}
		return false;
	}
	
	public void doIt(Scanner scanner) throws Exception {
		lastPrime = 2;
		primes.add(2);
		
		ArrayList<Integer> factors = new ArrayList<>();
		while (scanner.hasNext()) {
			int val = scanner.nextInt();
			if (val == 4l)
				return;

			int count = 1;
			boolean isValPrime = isPrime(val);
			while (!isValPrime) {
				primeFactors(val, factors);
				val = 0;
				for (int i : factors)
					val += i;
				isValPrime = isPrime(val);
				count++;
			}
			System.out.println(val + " " + count);
		}
	}

	public static void main(String[] args) throws Exception {
		//new Solution2().doIt(new Scanner(new BufferedInputStream(Solution1.class.getResourceAsStream("Solution2-data.txt"))));
		new Solution2().doIt(new Scanner(new BufferedInputStream(System.in)));
	}
}
