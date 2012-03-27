package noCVS;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BezierApplet.java

import java.applet.Applet;
import java.awt.*;
import java.awt.geom.Point2D;

public class BezierApplet extends Applet
{

    public void init()
    {
        initpt = new Point2D.Double[4];
        initpt[0] = new Point2D.Double(25, 25);
        initpt[1] = new Point2D.Double(125, 125);
        initpt[2] = new Point2D.Double(225, 125);
        initpt[3] = new Point2D.Double(325, 25);
        canvas = new BezierCanvas(this, initpt);
    }

    public void run()
    {
        do
            repaint();
        while(true);
    }

    public void update(Graphics g)
    {
        canvas.update(g);
    }

    public void paint(Graphics g)
    {
        canvas.update(g);
    }

    public boolean mouseDown(Event event, int i, int j)
    {
        canvas.mouseDown(i, j);
        repaint();
        return true;
    }

    public boolean mouseDrag(Event event, int i, int j)
    {
        if(canvas.active >= 0 && i >= 0 && i < canvas.wd && j >= 0 && j < canvas.ht)
        {
            canvas.node[canvas.active].centre.x = i;
            canvas.node[canvas.active].centre.y = j;
            canvas.path.reset(canvas.active, i, j);
            repaint();
            try
            {
                Thread.sleep(8L);
            }
            catch(InterruptedException _ex) { }
        }
        return true;
    }

    public boolean mouseUp(Event event, int i, int j)
    {
        if(canvas.active >= 0 && i >= 0 && i < canvas.wd && j >= 0 && j < canvas.ht)
        {
            canvas.node[canvas.active].centre.x = i;
            canvas.node[canvas.active].centre.y = j;
            canvas.path.reset(canvas.active, i, j);
            repaint();
            canvas.active = -1;
        }
        return true;
    }

    public BezierApplet()
    {
    }

    BezierCanvas canvas;
    Point2D.Double initpt[];
}
