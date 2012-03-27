package noCVS;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BezierApplet.java

import java.awt.*;
import java.awt.geom.Point2D;

class ControlNode
{

    ControlNode(Point2D.Double point)
    {
        colour = Color.lightGray;
        centre = new Point2D.Double(point.x, point.y);
    }

    boolean within(int i, int j)
    {
        return centre.x - wd <= i && i <= centre.x + wd && centre.y - ht <= j && j <= centre.y + ht;
    }

    public void draw(Graphics g)
    {
        g.setColor(colour);
        g.fillRect((int) (centre.x - wd), (int) (centre.y - ht), 2 * wd, 2 * ht);
        g.setColor(Color.black);
        g.drawRect((int) (centre.x - wd), (int) (centre.y - ht), 2 * wd, 2 * ht);
    }

    public Point2D.Double centre;
    public Color colour;
    static int wd = 3;
    static int ht = 3;

}
