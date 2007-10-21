import java.lang.reflect.Constructor;

public class testArrays {
  private double[] m;
  
  public double[] getM() {
    return m;
  }

  public void setM(double[] m) {
    this.m = m;
  }

  public testArrays() {
    m = new double[5];
  }

  public void testTheTest(String s) {
    System.out.println(s);
  }
  
  public testArrays(String a) { System.out.println(a); };
  
  public static Object createLike(Class a) {
    Constructor c;
    Object o = null;
    try {
      c = a.getConstructor(new Class[] { String.class });
      if (c == null) {
        System.out.println("No constructor with one string param");
      } else {
        o = c.newInstance(new Object[] {"Calling the constructor with one String parameter"});
      }
    } catch (Exception e) {
      System.out.println("error " + e.getMessage());
    }
    return o;
  }
  
  public static void main(String[] args) {
    Object o = createLike(testArrays.class);
    testArrays a;
    //if (o.getClass(). is testArrays) {
    if (o == null) return;
    a = (testArrays)o;
    a.testTheTest("kuku");
    ((testArrays)o).testTheTest("kuku");
    //}
    
  }

}
