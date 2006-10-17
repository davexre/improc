package com.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class TestProperties {

  public static void main(String[] args) throws FileNotFoundException, IOException {
    Properties p = new Properties();
    for (int i = 5; i >= 0; i--)
      p.setProperty("ItemName" + i, "Item value is " + i);
    p.store(new FileOutputStream("output/testProperties.properties"), "Some comments");

  }

}
