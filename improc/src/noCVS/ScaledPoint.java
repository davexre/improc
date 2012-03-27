package noCVS;

import java.awt.geom.Point2D;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Curve.java


class ScaledPoint
{

    public ScaledPoint(int i, int j)
    {
        x = i;
        y = j;
    }

    public ScaledPoint(Point2D.Double realpoint)
    {
        double d = realpoint.x;
        x = (int)(d *= 1 << ScaledInt.scale);
        d = realpoint.y;
        y = (int)(d *= 1 << ScaledInt.scale);
    }

    public static ScaledPoint average(ScaledPoint scaledpoint, ScaledPoint scaledpoint1)
    {
        int i = (int) ((scaledpoint.x + scaledpoint1.x) / 2.0);
        int j = (int) ((scaledpoint.y + scaledpoint1.y) / 2.0);
        ScaledPoint scaledpoint2 = new ScaledPoint(i, j);
        return scaledpoint2;
    }

    double x;
    double y;
}
