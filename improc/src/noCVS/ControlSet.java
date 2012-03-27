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

    public double breadth()
    {
        double f =  point0.x / 64.0;
        double f4 = point0.y / 64.0;
        double f1 = point1.x / 64.0;
        double f5 = point1.y / 64.0;
        double f2 = point2.x / 64.0;
        double f6 = point2.y / 64.0;
        double f3 = point3.x / 64.0;
        double f7 = point3.y / 64.0;
        if((double)Math.abs(f - f3) < ScaledInt.resolution && (double)Math.abs(f4 - f4) < ScaledInt.resolution)
        {
            double f8 = Math.abs(f1 - f) + Math.abs(f5 - f4);
            double f10 = Math.abs(f2 - f) + Math.abs(f6 - f4);
            if(f10 > f8)
                return (double)f10;
            else
                return (double)f8;
        }
        double d = f4 - f7;
        double d1 = f3 - f;
        float f12 = (float)Math.sqrt(d * d + d1 * d1);
        double d2 = f3 * f4 - f * f7;
        float f9 = (float)Math.abs((d * (double)f2 + d1 * (double)f6) - d2) / f12;
        float f11 = (float)Math.abs((d * (double)f1 + d1 * (double)f5) - d2) / f12;
        if(f9 > f11)
            return (double)f9;
        else
            return (double)f11;
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
