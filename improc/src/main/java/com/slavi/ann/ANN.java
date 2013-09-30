package com.slavi.ann;

public class ANN {
  
  public static final void randomizeArray(double [] m) {
    for (int i = m.length - 1; i >= 0; i--)
      m[i] = Math.random() - 0.5;
  }

  public static final void randomizeMatrix(double[][] m) {
    for (int i = m.length - 1; i >= 0; i--)
      randomizeArray(m[i]);
  }
  
  public static final void zeroArray(double[] m) {
    for (int i = m.length - 1; i >= 0; i--)
      m[i] = 0;
  }
  
  public static final void zeroMatrix(double[][] m) {
    for (int i = m.length - 1; i >= 0; i--)
      zeroArray(m[i]);
  }
}
