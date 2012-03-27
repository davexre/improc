package noCVS;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Curve.java

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

class Curve
{

    public Curve(Point2D.Double point, Point2D.Double point1, Point2D.Double point2, Point2D.Double point3)
    {
        control0 = point;
        control1 = point1;
        control2 = point2;
        control3 = point3;
        curveTo();
    }

    public void curveTo()
    {
    	polygon = new Polygon();
        polygon.addPoint((int)control0.x, (int)control0.y);

        int l = 0;
        ArrayList<ControlSet> acontrolset = new ArrayList<ControlSet>();
        ArrayList<ControlSet> acontrolset1 = new ArrayList<ControlSet>();
        
    	myPath = new Path2D.Double();
    	myPath.moveTo(control0.x, control0.y);
    	myPath.curveTo(
    			control1.x, control1.y,
    			control2.x, control2.y,
    			control3.x, control3.y);
        
        ScaledPoint scaledpoint = new ScaledPoint(control0);
        ScaledPoint scaledpoint1 = new ScaledPoint(control1);
        ScaledPoint scaledpoint2 = new ScaledPoint(control2);
        ScaledPoint scaledpoint3 = new ScaledPoint(control3);
        ControlSet controlset = new ControlSet(scaledpoint, scaledpoint1, scaledpoint2, scaledpoint3);
        controlset = new ControlSet(controlset.point0, controlset.point1, controlset.point2, controlset.point3);
        acontrolset1.add(controlset);
        l++;
        while(l > 0) 
        {
            ControlSet controlset1 = acontrolset1.get(--l);
            if(controlset1.breadth() > resolution)
            {
                ControlSet controlset3 = controlset1.bisect();
                acontrolset1.add(controlset1);
                l++;
                acontrolset1.add(controlset3);
                l++;
            } else
                acontrolset.add(controlset1);
        }
        for(ControlSet controlset2 : acontrolset)
        {
            int k1 = (int) controlset2.point3.x;
            k1 >>= ScaledInt.scale;
            int l1 = (int) controlset2.point3.y;
            l1 >>= ScaledInt.scale;
            polygon.addPoint(k1, l1);
        }
    }

    public void reset(int i, int j, int k)
    {
        switch(i)
        {
        case 0: // '\0'
            control0 = new Point2D.Double(j, k);
            break;

        case 1: // '\001'
            control1 = new Point2D.Double(j, k);
            break;

        case 2: // '\002'
            control2 = new Point2D.Double(j, k);
            break;

        case 3: // '\003'
            control3 = new Point2D.Double(j, k);
            break;
        }
        curveTo();
    }

    public void draw(Graphics g, Color color)
    {
//        g.drawPolygon(polygon);
    	Graphics2D g2 = (Graphics2D) g;
    	g2.draw(myPath);
        g.setColor(color);
        for(int i = 0; i < polygon.npoints; i++)
            g.fillRect(polygon.xpoints[i] - 1, polygon.ypoints[i] - 1, 2, 2);

    }

    static double resolution = 0.5D;
    Path2D.Double myPath;
    Polygon polygon;
    Point2D.Double control0;
    Point2D.Double control1;
    Point2D.Double control2;
    Point2D.Double control3;

}
