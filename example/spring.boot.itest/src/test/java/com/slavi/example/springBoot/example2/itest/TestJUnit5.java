package com.slavi.example.springBoot.example2.itest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//@RunWith(JUnitPlatform.class)
class TestJUnit5 {

	@BeforeAll
	static void beforeClass() {
		System.out.println("Before Class");
	}

	@AfterAll
	static void afterClass() {
		System.out.println("After class");
	}

	@BeforeEach
	void beforeTest() {
		System.out.println("Before");
	}

	@AfterEach
	void afterTest() {
		System.out.println("After");
	}

	@Test
	void test1() {
		System.out.println("Test 1");
	}

	@Test
	void test2() {
		System.out.println("Test 2");
	}

	@Test
	void test3() {
		System.out.println("Test 3");
	}
}
