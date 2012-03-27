package noCVS;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Curve.java


class ControlSet
{

    public ControlSet(ScaledPoint scaledpoint, ScaledPoint scaledpoint1, ScaledPoint scaledpoint2, ScaledPoint scaledpoint3)
    {
        point0 = scaledpoint;
        point1 = scaledpoint1;
        point2 = scaledpoint2;
        point3 = scaledpoint3;
    }

    public static final double scale = 2;
    public static final double resolution = 1;
    public double breadth()
    {
        double x0 = point0.x / scale;
        double y0 = point0.y / scale;
        double x1 = point1.x / scale;
        double y1 = point1.y / scale;
        double x2 = point2.x / scale;
        double y2 = point2.y / scale;
        double x3 = point3.x / scale;
        double y3 = point3.y / scale;
        
        double dx = x3 - x0;
        double dy = y3 - y0;
        double dist = Math.sqrt(dy * dy + dx * dx);
        
        if (dist < resolution) {
            double f8 = Math.abs(x1 - x0) + Math.abs(y1 - y0);
            double f10 = Math.abs(x2 - x0) + Math.abs(y2 - y0);
            if (f10 > f8)
                return f10;
            else
                return f8;
        }
        double d2 = x3 * y0 - x0 * y3;
        double f9 =  Math.abs((dx * y2 - dy * x2) - d2) / dist;
        double f11 = Math.abs((dx * y1 - dy * x1) - d2) / dist;
        if(f9 > f11)
            return f9;
        else
            return f11;
    }

    public ControlSet bisect()
    {
        ScaledPoint scaledpoint = ScaledPoint.average(point0, point1);
        ScaledPoint scaledpoint1 = ScaledPoint.average(point1, point2);
        ScaledPoint scaledpoint2 = ScaledPoint.average(point2, point3);
        ScaledPoint scaledpoint3 = ScaledPoint.average(scaledpoint, scaledpoint1);
        ScaledPoint scaledpoint4 = ScaledPoint.average(scaledpoint1, scaledpoint2);
        ScaledPoint scaledpoint5 = ScaledPoint.average(scaledpoint3, scaledpoint4);
        ControlSet controlset = new ControlSet(scaledpoint5, scaledpoint4, scaledpoint2, point3);
        point1 = scaledpoint;
        point2 = scaledpoint3;
        point3 = scaledpoint5;
        return controlset;
    }

    ScaledPoint point0;
    ScaledPoint point1;
    ScaledPoint point2;
    ScaledPoint point3;
}
