import java.io.File;


public class test {

  public void aaa() {
    byte b = (byte) 0x80;
    byte c = (byte) 0xff;
    System.out.println((b - c) & 0xff);
  }

  public void aab() {
    byte b = (byte) 0xff;
    b = (byte) (b + 1);
    System.out.println(b & 0xff);
  }
  
  public void aac() {
    System.out.println(System.getProperty("user.dir"));    
  }
  
  public void aad() {
    int a = 5;
    int b = 3;
    double d = a / b;
    System.out.println(d);
  }
  
  public static void main(String args[]) {
    test t = new test();
    System.out.println((new File("images/testSkin.properties")).getParentFile());

    t.aad();
  }
}