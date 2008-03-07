package com.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JUnitTestTest {
	
	public static abstract class BaseClass {
	    public BaseClass() {
	    	System.out.println("BaseClass:constructor");
	    }
		
	    @BeforeClass
	    public static void oneTimeSetUp() {
	        System.out.println("BaseClass:@BeforeClass");
	    }
	
	    @AfterClass
	    public static void oneTimeTearDown() {
	        System.out.println("BaseClass:@AfterClass");
	    }
	
	    @Before
	    public void setUp() {
	        System.out.println("BaseClass:@Before");
	    }
	
	    @After
	    public void tearDown() {
	        System.out.println("BaseClass:@After");
	    }
	
	    @Test
	    public abstract void test();
	
	    public int testATS() {
	    	oneTimeSetUp();
	    	setUp();
	    	test();
	    	tearDown();
	    	oneTimeTearDown();
	    	return 0;
	    }
	}
	
	public static class Child extends BaseClass {
		public Child() {
			System.out.println("Child:constructor");
		}

		public void test() {
			System.out.println("Child:test");
		}
	}
}
