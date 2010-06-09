package com.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Properties;

public class TestProperties {

  public static void main(String[] args) throws FileNotFoundException, IOException {
    Properties p = new Properties();
    for (int i = 5; i >= 0; i--)
      p.setProperty("ItemName" + i, "Тази стойност е " + i);
//    PrintWriter fou = new PrintWriter("testProperties.properties");
//    FileOutputStream fou = new FileOutputStream("testProperties.properties");
    p.store(new PrintWriter(System.out), "Some comments");
//    fou.close();
  }

}
