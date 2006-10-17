package com.slavi.ui;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class BarChart extends JComponent {
  private static final long serialVersionUID = -816849295673733161L;

  protected double data[];
  
  protected boolean scaleAutomatic = true;
  
  protected double minValue = 0;
  
  protected double maxValue = 0;
  
  public double[] getData() {
    return data;
  }

  public void setData(double[] data) {
    this.data = data;
    repaint();
  }

  public double getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(double maxValue) {
    this.maxValue = maxValue;
    repaint();
  }

  public double getMinValue() {
    return minValue;
  }

  public void setMinValue(double minValue) {
    this.minValue = minValue;
    repaint();
  }

  public boolean isScaleAutomatic() {
    return scaleAutomatic;
  }

  public void setScaleAutomatic(boolean scaleAutomatic) {
    this.scaleAutomatic = scaleAutomatic;
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
    if ((data == null) || (data.length < 1)) {
      drawX(g);
      return;
    }
    
    double curMin, curMax;
    if (scaleAutomatic) {
      curMax = curMin = data[0];
      for (int i = data.length -1; i >= 0; i--) {
        if (curMin > data[i]) curMin = data[i];
        if (curMax < data[i]) curMax = data[i];
      }
    } else {
      curMin = minValue < maxValue ? minValue : maxValue;
      curMax = minValue > maxValue ? minValue : maxValue;
    }

    double dX = Math.ceil((double)(this.getWidth()) / (double)(data.length));
    double dY = curMax - curMin;
    if (dY == 0) {
      drawX(g);
      return;
    }
    dY = this.getHeight() / dY;
    for (int i = data.length - 1; i >= 0; i--) {
      int atY = this.getHeight() - (int)((data[i] - curMin) * dY);
      atY = atY < 0 ? 0 : atY;
      atY = atY >= this.getHeight() ? this.getHeight() - 1 : atY;
      g.fillRect((int)(i * dX), atY, (int)dX, this.getHeight() - atY);
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame("Barchar test");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    BarChart bc = new BarChart();
    double[] arr = new double[(int)(Math.random() * 10 + 5)];
    for (int i = arr.length - 1; i >= 0; i--)
      arr[i] = Math.random();
    bc.data = arr;
    
    f.add(bc);
    f.setSize(400, 300);
    f.setVisible(true);    
  }
}
