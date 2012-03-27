package noCVS;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Curve.java

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
    	points.clear();
        points.add(control0);

        
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
        ArrayList<ControlSet> todo = new ArrayList<ControlSet>();
        todo.add(controlset);
        while(todo.size() > 0) 
        {
            ControlSet controlset1 = todo.remove(0);
            if(controlset1.breadth() > 0.5)
            {
                ControlSet controlset3 = controlset1.bisect();
                todo.add(controlset1);
                todo.add(controlset3);
            } else {
                points.add(new Point2D.Double(controlset1.point3.x / ControlSet.scale, controlset1.point3.y / ControlSet.scale));
            }
        }
        System.out.println(points.size());
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
        for (Point2D.Double point : points)
            g.fillRect((int) point.x - 1, (int) point.y - 1, 2, 2);

    }

    Path2D.Double myPath;
    ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
    Point2D.Double control0;
    Point2D.Double control1;
    Point2D.Double control2;
    Point2D.Double control3;

}
