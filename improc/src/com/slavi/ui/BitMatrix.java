package com.slavi.ui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.slavi.matrix.Matrix;

public class BitMatrix extends JComponent {
  private static final long serialVersionUID = 3643680302268014549L;

  protected Matrix matrix = null;
  protected double checkedValue = 1;
  protected double unCheckedValue = 0;
  protected boolean usingGradientColors = true;
  
  public double getCheckedValue() {
    return checkedValue;
  }

  public void setCheckedValue(double checkedValue) {
    this.checkedValue = checkedValue;
    repaint();
  }

  public Matrix getMatrix() {
    return matrix;
  }

  public void setMatrix(Matrix matrix) {
    this.matrix = matrix;
    repaint();
  }

  public double getUnCheckedValue() {
    return unCheckedValue;
  }

  public void setUnCheckedValue(double unCheckedValue) {
    this.unCheckedValue = unCheckedValue;
    repaint();
  }

  public boolean isUsingGradientColors() {
    return usingGradientColors;
  }

  public void setUsingGradientColors(boolean useGradientColors) {
    this.usingGradientColors = useGradientColors;
    repaint();
  }

  protected void drawX(Graphics g) {
    int toX = this.getWidth() - 1;
    int toY = this.getHeight() - 1;
    g.fillRect(0, 0, toX, toY);
    g.drawLine(0, 0, toX, toY);
    g.drawLine(0, toY, toX, 0);
  }
  
  public void paint(Graphics g) {
    if (matrix == null) {
      drawX(g);
      return;
    }
    double mmin = matrix.min();
    double mmax = matrix.max();
    double middle = (mmin + mmax) / 2;
    double delta = mmax - mmin;
    if (delta == 0) {
      drawX(g);
      return;
    }

    double dX = (double)(this.getWidth()) / (double)(matrix.getSizeX());    
    double dY = (double)(this.getHeight()) / (double)(matrix.getSizeY());
    
    int fc = this.getForeground().getRGB();
    int bc = this.getBackground().getRGB();
    
    for (int i = matrix.getSizeX() - 1; i >= 0; i--)
      for (int j = matrix.getSizeY() - 1; j >= 0; j--)  
        if (usingGradientColors) {
          double t = (matrix.getItem(i, j) - mmin) / delta;
          int c = 
            (((((bc    )& 0xff) + ((int)((((fc-bc)    )& 0xff)*t))&0xff))      ) |
            (((((bc>> 8)& 0xff) + ((int)((((fc-bc)>> 8)& 0xff)*t))&0xff)) <<  8) |
            (((((bc>>16)& 0xff) + ((int)((((fc-bc)>>16)& 0xff)*t))&0xff)) << 16);
          g.setColor(new Color(c));
          g.fillRect(
              (int)(i * dX), 
              (int)(j * dY), 
              (int)((i + 1) * dX) - (int)(i * dX), 
              (int)((j + 1) * dY) - (int)(j * dY));
        } else {
          if (matrix.getItem(i, j) > middle)
            g.fillRect(
                (int)(i * dX), 
                (int)(j * dY), 
                (int)((i + 1) * dX) - (int)(i * dX), 
                (int)((j + 1) * dY) - (int)(j * dY));
        }
  }
  
  public static void main(String[] args) {
    JFrame f = new JFrame("BitMatrix test");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    BitMatrix theComponent = new BitMatrix();
    
    Matrix m = new Matrix(20,20);
        //(int)(Math.random() * 5 + 5), 
        //(int)(Math.random() * 5 + 5));
    for (int i = m.getSizeX() - 1; i >= 0; i--)
      for (int j = m.getSizeY() - 1; j >= 0; j--)
        //m.setItem(i, j, Math.random());
        m.setItem(i, j, (int)((double)(i * j) / (double)(m.getVectorSize())));
    theComponent.setMatrix(m);
        
    f.add(theComponent);
    f.setSize(400, 300);
    f.setVisible(true);    
  }
}
