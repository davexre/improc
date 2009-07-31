// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MapProjection.java

package com.test;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.slavi.math.MathUtil;

// Referenced classes of package mp:
//            MapPoint, PjMapProjection, Borderline

public class MapProjection extends Canvas
    implements MouseListener, MouseMotionListener, KeyListener
{

	public static class MapPoint
	{

	    public MapPoint()
	    {
	        x = 0.0D;
	        y = 0.0D;
	    }

	    public MapPoint(double d, double d1)
	    {
	        x = d;
	        y = d1;
	    }

	    public void translate(double d, double d1)
	    {
	        x += d;
	        y += d1;
	    }

	    public boolean equals(Object obj)
	    {
	        if(obj instanceof MapPoint)
	        {
	            MapPoint mappoint = (MapPoint)obj;
	            return x == mappoint.x && y == mappoint.y;
	        } else
	        {
	            return false;
	        }
	    }

	    public String toString()
	    {
	        return getClass().getName() + "[x = " + "x" + ", y = " + y + "]";
	    }

	    public double x;
	    public double y;
	}
	
    public MapProjection(int i, int j)
    {
        fpx = new double[800];
        fpy = new double[800];
        newp = new MapPoint();
        setBounds(0, 0, i, j);
        setBackground(Color.BLACK);
        MP_nCenterX = i / 2;
        MP_nCenterY = j / 2;
        MP_nCenterXInitial = MP_nCenterX;
        MP_nCenterYInitial = MP_nCenterY;
        Initialize();
        MP_dScaleCorrection = 1.0D;
        MP_dScale *= 0.5D;
        MP_dScaleInitial = MP_dScale;
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    public void Initialize()
    {
        MP_nLonDeg = 0;
        MP_nLonMin = 0;
        MP_nLatDeg = 0;
        MP_nLatMin = 0;
        MP_dLonCenter = 0.0D;
        MP_dLatCenter = 0.0D;
        MP_dObliqY = 90D;
        MP_nSp1Deg = 30;
        MP_nSp1Min = 0;
        MP_nSp2Deg = 15;
        MP_nSp2Min = 0;
        MP_dSp1 = 30D;
        MP_dSp2 = 15D;
        MP_dScale = 1000D;
        m_Tx = 80D;
        m_Ty = 160D;
        m_Tdist = 1.0D;
        m_Tphi = 0.0D;
        MP_dDistance = 1.5D;
        MP_nInterval = 1000;
        MP_bCoast = true;
        MP_bGraticule = true;
        MP_bOutline = true;
        MP_bIndicatrix = false;
        MP_bOblique = true;
        MP_bCircle = true;
        MP_bAzimuthal = false;
        MP_bGCircle = false;
        MP_nEqdLonDeg = 0;
        MP_nEqdLonMin = 0;
        MP_nEqdLatDeg = 0;
        MP_nEqdLatMin = 0;
        MP_nAzmLonDeg = 0;
        MP_nAzmLonMin = 0;
        MP_nAzmLatDeg = 0;
        MP_nAzmLatMin = 0;
        MP_nSrcLonDeg = 0;
        MP_nSrcLonMin = 0;
        MP_nSrcLatDeg = 45;
        MP_nSrcLatMin = 0;
        MP_nDstLonDeg = 135;
        MP_nDstLonMin = 0;
        MP_nDstLatDeg = 30;
        MP_nDstLatMin = 0;
    }

    public void paint(Graphics g)
    {
        update(g);
    }

    public void update(Graphics g)
    {
/*        MapPoint mappoint = new MapPoint();
        mappoint.x = MP_dLonCenter;
        mappoint.y = MP_dLatCenter;
        getNewPole(mappoint);
        offsize = getSize();
        offimg = createImage(offsize.width, offsize.height);
        offg = offimg.getGraphics();
        offg.setColor(Color.black);
        if(m_p.projectionOK)
        {
            if(m_p.viewCoastline)
                ProjectLines(offg, m_p.coastline);
            if(m_p.viewGraticule)
            {
                offg.setColor(Color.LIGHT_GRAY);
                ProjectGraticule(offg);
            }
            if(m_p.viewAfricaBnd)
                ProjectLines(offg, m_p.africa_bnd);
            if(m_p.viewAmericaBnd)
                ProjectLines(offg, m_p.america_bnd);
            if(m_p.viewEuropeBnd)
                ProjectLines(offg, m_p.europe_bnd);
            if(m_p.viewAsiaBnd)
                ProjectLines(offg, m_p.asia_bnd);
            if(m_p.viewOceaniaBnd)
                ProjectLines(offg, m_p.oceania_bnd);
            if(m_p.viewGreenwich)
            {
                offg.setColor(m_p.greenwich.getGlobalEdgeColor());
                ProjectMeridian(offg, 0.0D);
            }
            if(m_p.viewCancer)
            {
                offg.setColor(m_p.cancer.getGlobalEdgeColor());
                ProjectParallel(offg, 23.5D);
            }
            if(m_p.viewEquator)
            {
                offg.setColor(m_p.equator.getGlobalEdgeColor());
                ProjectParallel(offg, 0.0D);
            }
            if(m_p.viewCapricorn)
            {
                offg.setColor(m_p.capricorn.getGlobalEdgeColor());
                ProjectParallel(offg, -23.5D);
            }
            if(m_p.viewArctic)
            {
                offg.setColor(m_p.arctic_circle.getGlobalEdgeColor());
                ProjectParallel(offg, 66.299999999999997D);
            }
            if(m_p.viewAntarctic)
            {
                offg.setColor(m_p.antarctic_circle.getGlobalEdgeColor());
                ProjectParallel(offg, -66.299999999999997D);
            }
            offg.setColor(m_p.par_by_point.getGlobalEdgeColor());
            ProjectParallel(offg, m_p.picked_point_lat_in_deg);
            ProjectMeridian(offg, m_p.picked_point_lon_in_deg);
            if(m_p.show_geodesic)
            {
                ProjectLines(offg, m_p.geodesic);
                ProjectLines(offg, Color.CYAN, 2, m_p.city1_seg.getVertex(1).getEntry(0), m_p.city1_seg.getVertex(1).getEntry(1), m_p.city1_seg.getVertex(1).getEntry(2));
                ProjectLines(offg, Color.CYAN, 2, m_p.city2_seg.getVertex(1).getEntry(0), m_p.city2_seg.getVertex(1).getEntry(1), m_p.city2_seg.getVertex(1).getEntry(2));
            }
            if(m_p.show_loxodrome)
                ProjectLines(offg, m_p.loxodrome);
            if(m_p.viewTissotIndicatrices)
            {
                offg.setColor(new Color(250, 128, 114));
                ProjectIndicatrix(offg);
            }
            if(m_p.viewRing)
            {
                offg.setColor(Color.WHITE);
                ProjectRing(offg);
            }
            if(m_p.viewOutlines)
            {
                offg.setColor(Color.WHITE);
                ProjectOutline(offg);
            }
        }
        g.drawImage(offimg, 0, 0, this);*/
    }

    private void getNewPole(MapPoint mappoint)
    {
        if(mappoint.y > 0.0D)
        {
            newp.y = 90D - mappoint.y;
            if(mappoint.x > 0.0D && mappoint.x <= 180D)
                newp.x = mappoint.x - 180D;
            else
            if(mappoint.x >= -180D && mappoint.x <= 0.0D)
                newp.x = mappoint.x + 180D;
        } else
        if(mappoint.y < 0.0D)
        {
            newp.y = 90D - mappoint.y;
            newp.x = mappoint.x - 180D;
        } else
        if(mappoint.y == 0.0D)
        {
            newp.y = 90D;
            newp.x = mappoint.x;
        }
    }

    private double DegMin2Deg(int i, int j)
    {
        if(i >= 0)
            return (double)i + (double)j / 60D;
        else
            return (double)i - (double)j / 60D;
    }
/*
    private void ProjectLines(Graphics g, Borderline borderline)
    {
        for(int i = 0; i < borderline.getNumPolygons(); i++)
        {
            g.setColor(borderline.getColor());
            int j = borderline.getPolygon(i).getSize();
            MapPoint mappoint = new MapPoint();
            MapPoint mappoint1 = new MapPoint();
            MapPoint mappoint2 = new MapPoint();
            int ai[] = new int[j + 1];
            int ai1[] = new int[j + 1];
            dExpandrate = 300D / MP_dScale;
            int k = 0;
            double d;
            if(MP_nPJID == 4500 || MP_nPJID == 3850 || MP_nPJID == 2600 || MP_nPJID == 1350 || MP_nPJID == 4050 || MP_nPJID == 4750 || MP_nPJID == 1550 || MP_nPJID == 3250)
                d = 56D * dExpandrate;
            else
                d = 20D * dExpandrate;
            for(int l = 0; l < j; l++)
            {
                int i1 = borderline.getPolygon(i).getEntry(l);
                mappoint1.x = borderline.getVertex(i1).getEntry(3);
                mappoint1.y = borderline.getVertex(i1).getEntry(4);
                if(Projection(mappoint1))
                {
                    mappoint.x = (double)MP_nCenterX + mappoint1.x;
                    mappoint.y = (double)MP_nCenterY - mappoint1.y;
                    if(l == 0)
                    {
                        ai[k] = (int)mappoint.x;
                        ai1[k] = (int)mappoint.y;
                        k++;
                    } else
                    if(Dist(mappoint, mappoint2) < d)
                    {
                        ai[k] = (int)mappoint.x;
                        ai1[k] = (int)mappoint.y;
                        k++;
                        if(l == j - 1 && k > 1)
                            ProjectPolyline(g, ai, ai1, k);
                    } else
                    if(Dist(mappoint, mappoint2) >= d)
                    {
                        if(k > 1)
                            ProjectPolyline(g, ai, ai1, k);
                        ai[0] = (int)mappoint.x;
                        ai1[0] = (int)mappoint.y;
                        k = 1;
                    }
                    mappoint2.x = mappoint.x;
                    mappoint2.y = mappoint.y;
                    continue;
                }
                if(k > 1)
                    ProjectPolyline(g, ai, ai1, k);
                k = 0;
            }

        }

    }
*/
    public double[] pos2geo(double d, double d1, double d2)
    {
        double ad[] = new double[2];
        double d3;
        double d4;
        if(d <= 0.0D && d1 == 0.0D)
        {
            d4 = 180D;
            d3 = (Math.atan2(d2, Math.abs(d)) * 180D) / Math.PI;
        } else
        {
            d3 = (Math.atan2(d2, Math.sqrt(d * d + d1 * d1)) * 180D) / Math.PI;
            d4 = (Math.atan2(d1, d) * 180D) / Math.PI;
        }
        ad[0] = d4;
        ad[1] = d3;
        return ad;
    }

/*    private void ProjectLines(Graphics g, PgPolygon pgpolygon)
    {
        g.setColor(pgpolygon.getGlobalEdgeColor());
        int i = pgpolygon.getNumVertices();
        MapPoint mappoint = new MapPoint();
        MapPoint mappoint1 = new MapPoint();
        MapPoint mappoint2 = new MapPoint();
        int ai[] = new int[i + 1];
        int ai1[] = new int[i + 1];
        dExpandrate = 300D / MP_dScale;
        int j = 0;
        double d;
        if(MP_nPJID == 4500 || MP_nPJID == 3850 || MP_nPJID == 2600 || MP_nPJID == 1350 || MP_nPJID == 4050 || MP_nPJID == 4750 || MP_nPJID == 1550 || MP_nPJID == 3250)
            d = 56D * dExpandrate;
        else
            d = 20D * dExpandrate;
        for(int k = 0; k < i; k++)
        {
            double ad[] = pos2geo(pgpolygon.getVertex(k).getEntry(0), pgpolygon.getVertex(k).getEntry(1), pgpolygon.getVertex(k).getEntry(2));
            mappoint1.x = ad[0];
            mappoint1.y = ad[1];
            if(Projection(mappoint1))
            {
                mappoint.x = (double)MP_nCenterX + mappoint1.x;
                mappoint.y = (double)MP_nCenterY - mappoint1.y;
                if(k == 0)
                {
                    ai[j] = (int)mappoint.x;
                    ai1[j] = (int)mappoint.y;
                    j++;
                } else
                if(Dist(mappoint, mappoint2) < d)
                {
                    ai[j] = (int)mappoint.x;
                    ai1[j] = (int)mappoint.y;
                    j++;
                    if(k == i - 1 && j > 1)
                        ProjectPolyline(g, ai, ai1, j);
                } else
                if(Dist(mappoint, mappoint2) >= d)
                {
                    if(j > 1)
                        ProjectPolyline(g, ai, ai1, j);
                    ai[0] = (int)mappoint.x;
                    ai1[0] = (int)mappoint.y;
                    j = 1;
                }
                mappoint2.x = mappoint.x;
                mappoint2.y = mappoint.y;
                continue;
            }
            if(j > 1)
                ProjectPolyline(g, ai, ai1, j);
            j = 0;
        }

    }
*/
    private void ProjectLines(Graphics g, Color color, int i, double d, double d1, 
            double d2)
    {
        g.setColor(color);
        MapPoint mappoint = new MapPoint();
        MapPoint mappoint1 = new MapPoint();
        dExpandrate = 300D / MP_dScale;
        boolean flag = false;
        double d3;
        if(MP_nPJID == 4500 || MP_nPJID == 3850 || MP_nPJID == 2600 || MP_nPJID == 1350 || MP_nPJID == 4050 || MP_nPJID == 4750 || MP_nPJID == 1550 || MP_nPJID == 3250)
            d3 = 56D * dExpandrate;
        else
            d3 = 20D * dExpandrate;
        double ad[] = pos2geo(d, d1, d2);
        mappoint1.x = ad[0];
        mappoint1.y = ad[1];
        if(Projection(mappoint1))
        {
            mappoint.x = (double)MP_nCenterX + mappoint1.x;
            mappoint.y = (double)MP_nCenterY - mappoint1.y;
            g.fillOval((int)mappoint.x - i, (int)mappoint.y - i, 2 * i, 2 * i);
        }
    }

    private void ProjectLines(Graphics g, int i)
    {
        MapPoint mappoint = new MapPoint();
        MapPoint mappoint1 = new MapPoint();
        MapPoint mappoint2 = new MapPoint();
        int ai[] = new int[i + 1];
        int ai1[] = new int[i + 1];
        dExpandrate = 300D / MP_dScale;
        int j = 0;
        double d;
        if(MP_nPJID == 4500 || MP_nPJID == 3850 || MP_nPJID == 2600 || MP_nPJID == 1350 || MP_nPJID == 4050 || MP_nPJID == 4750 || MP_nPJID == 1550 || MP_nPJID == 3250)
            d = 56D * dExpandrate;
        else
            d = 20D * dExpandrate;
        for(int k = 0; k < i; k++)
        {
            mappoint1.x = fpx[k];
            mappoint1.y = fpy[k];
            if(Projection(mappoint1))
            {
                mappoint.x = (double)MP_nCenterX + mappoint1.x;
                mappoint.y = (double)MP_nCenterY - mappoint1.y;
                if(k == 0)
                {
                    ai[j] = (int)mappoint.x;
                    ai1[j] = (int)mappoint.y;
                    j++;
                } else
                if(Dist(mappoint, mappoint2) < d)
                {
                    ai[j] = (int)mappoint.x;
                    ai1[j] = (int)mappoint.y;
                    j++;
                    if(k == i - 1 && j > 1)
                        ProjectPolyline(g, ai, ai1, j);
                } else
                if(Dist(mappoint, mappoint2) >= d)
                {
                    if(j > 1)
                        ProjectPolyline(g, ai, ai1, j);
                    ai[0] = (int)mappoint.x;
                    ai1[0] = (int)mappoint.y;
                    j = 1;
                }
                mappoint2.x = mappoint.x;
                mappoint2.y = mappoint.y;
                continue;
            }
            if(j > 1)
                ProjectPolyline(g, ai, ai1, j);
            j = 0;
        }

    }

    private void ProjectPolyline(Graphics g, int ai[], int ai1[], int i)
    {
        for(int j = 0; j < i - 1; j++)
            g.drawLine(ai[j], ai1[j], ai[j + 1], ai1[j + 1]);

    }

    private double Dist(MapPoint mappoint, MapPoint mappoint1)
    {
        double d = Math.abs(mappoint.x - mappoint1.x);
        double d1 = Math.abs(mappoint.y - mappoint1.y);
        return Math.sqrt(d * d + d1 * d1);
    }

    private void ProjectOutline(Graphics g)
    {
        MapPoint mappoint = new MapPoint();
        MapPoint mappoint1 = new MapPoint();
        double d = MP_dObliqY;
        MP_dObliqY = 90D;
        if(MP_nPJID != 1550 && MP_nPJID != 3250 && MP_nPJID != 1500 && MP_nPJID != 1650 && MP_nPJID != 1600 && MP_nPJID != 1675)
        {
            int i = 0;
            MapPoint mappoint3 = new MapPoint();
            mappoint3.x = MP_dLonCenter;
            MP_dLonCenter = 0.0D;
            mappoint3.y = MP_dLatCenter;
            MP_dLatCenter = 0.0D;
            MapPoint mappoint4 = newp;
            newp.x = 0.0D;
            newp.y = 90D;
            double d2;
            double d4;
            if(MP_nPJID == 1700 || MP_nPJID == 2850 || MP_nPJID == 4000 || MP_nPJID == 1300)
            {
                d2 = 90D;
                d4 = 90D;
            } else
            if(MP_nPJID == 3700)
            {
                d2 = 180D;
                d4 = 85D;
            } else
            {
                d2 = 180D;
                d4 = 90D;
            }
            for(double d6 = -d2; d6 <= d2;)
            {
                fpx[i] = d6;
                fpy[i] = d4;
                d6 += 2D;
                i++;
            }

            for(double d7 = d4; d7 >= -d4;)
            {
                fpx[i] = d2;
                fpy[i] = d7;
                d7 -= 2D;
                i++;
            }

            for(double d8 = d2; d8 >= -d2;)
            {
                fpx[i] = d8;
                fpy[i] = -d4;
                d8 -= 2D;
                i++;
            }

            for(double d9 = -d4; d9 <= d4;)
            {
                fpx[i] = -d2;
                fpy[i] = d9;
                d9 += 2D;
                i++;
            }

            ProjectLines(g, i);
            MP_dLatCenter = mappoint3.y;
            MP_dLonCenter = mappoint3.x;
            newp = mappoint4;
        } else
        if(MP_nPJID == 1550 || MP_nPJID == 1600)
        {
            MapPoint mappoint2 = new MapPoint();
            newp.x = 0.0D;
            newp.y = 90D;
            if(MP_nPJID == 1550)
            {
                mappoint2.x = 0.0D;
                mappoint2.y = 89D;
            } else
            if(MP_nPJID == 3250)
            {
                mappoint2.x = 0.0D;
                mappoint2.y = 89D;
            } else
            if(MP_nPJID == 1600)
            {
                mappoint2.x = 0.0D;
                mappoint2.y = 30D;
            }
            if(Projection(mappoint2))
            {
                double d1 = mappoint2.x;
                double d3 = mappoint2.y;
                if(MP_nPJID == 1550)
                {
                    mappoint2.x = 0.0D;
                    mappoint2.y = -89D;
                } else
                if(MP_nPJID == 3250)
                {
                    mappoint2.x = 0.0D;
                    mappoint2.y = -35D;
                } else
                if(MP_nPJID == 1600)
                {
                    mappoint2.x = 0.0D;
                    mappoint2.y = -30D;
                }
                if(Projection(mappoint2))
                {
                    double d5 = mappoint2.x;
                    double d10 = mappoint2.y;
                    int j = (int)Math.abs(d10 - d3);
                    g.drawOval(MP_nCenterX - j, MP_nCenterY - j, 2 * j, 2 * j);
                }
            }
        }
        MP_dObliqY = d;
    }

    public double[] ComputeGeodesicalPosition(double d, double d1, double d2, double d3)
    {
        double ad[] = new double[2];
        double d6 = (d * Math.PI) / 180D;
        double d7 = (d1 * Math.PI) / 180D;
        double d8 = (d2 * Math.PI) / 180D;
        double d9 = d3 / 6378D;
        double d10 = d9;
        double d11 = d8;
        double d12 = d7;
        double d13 = d6;
        double d14 = d13 + Math.atan((Math.sin(d10) * Math.sin(d11)) / (Math.cos(d12) * Math.cos(d10) - Math.sin(d12) * Math.sin(d10) * Math.cos(d11)));
        double d15 = Math.asin(Math.sin(d12) * Math.cos(d10) + Math.cos(d12) * Math.sin(d10) * Math.cos(d11));
        double d5 = d15 / 0.017453292519943295D;
        double d4 = d14 / 0.017453292519943295D;
        if(d4 > 180D)
            d4 -= 360D;
        if(d4 <= -180D)
            d4 += 360D;
        if(d5 > 90D)
            d5 -= 180D;
        if(d5 <= -90D)
            d5 += 180D;
        d14 = (d4 * Math.PI) / 180D;
        d15 = (d5 * Math.PI) / 180D;
        double d16 = 6378D * Math.acos(Math.cos(d15) * Math.cos(d7) * Math.cos(d14 - d6) + Math.sin(d15) * Math.sin(d7));
        if(d16 < d3 - 0.01D * d3)
            if(d4 > 0.0D)
                d4 = -180D + d4;
            else
                d4 = 180D + d4;
        ad[0] = d4;
        ad[1] = d5;
        return ad;
    }

    private void ProjectIndicatrix(Graphics g)
    {
/*        for(int i = -180; i <= 180; i += 30)
        {
            for(int j = -60; j <= 60; j += 30)
            {
                double d = 0.0D;
                int k;
                for(k = 0; d <= 360D; k++)
                {
                    double ad[] = ComputeGeodesicalPosition(i, j, d, m_p.tissot_indicatrix_size);
                    fpx[k] = ad[0];
                    fpy[k] = ad[1];
                    d += 360D / (double)m_p.tissot_indicatrix_n_of_p;
                }

                ProjectLines(g, k);
            }

        }
*/
    }

    private void ProjectRing(Graphics g)
    {
/*        double d = m_p.ring_size;
        double d1 = 0.0D;
        int i;
        for(i = 0; d1 <= 360D; i++)
        {
            double ad[] = ComputeGeodesicalPosition(m_p.picked_point_lon_in_deg, m_p.picked_point_lat_in_deg, d1, d);
            fpx[i] = ad[0];
            fpy[i] = ad[1];
            d1 += 2D;
        }

        ProjectLines(g, i);
        for(int k = 0; k < m_p.ring_n_of_r; k++)
        {
            double d2 = (360D / (double)m_p.ring_n_of_r) * (double)k;
            int j;
            for(j = 0; j < m_p.ring_radii_n_of_p; j++)
            {
                double d3 = (m_p.ring_size / (double)(m_p.ring_radii_n_of_p - 1)) * (double)j;
                double ad1[] = ComputeGeodesicalPosition(m_p.picked_point_lon_in_deg, m_p.picked_point_lat_in_deg, d2, d3);
                fpx[j] = ad1[0];
                fpy[j] = ad1[1];
            }

            ProjectLines(g, j);
        }
*/
    }

    public void mouseDragged(MouseEvent mouseevent)
    {
        int i = mouseevent.getX();
        int j = mouseevent.getY();
        if(bPress)
        {
            int k = getLonDeg() - (int)(((double)(i - oldX) * MP_dScale) / 400D);
            int l = getLatDeg() + (int)(((double)(j - oldY) * MP_dScale) / 400D);
            if(k < -180)
                setLonDeg2(k + 360);
            else
            if(k > 180)
                setLonDeg2(k - 360);
            else
                setLonDeg(k);
            if(l < -90)
                setLatDeg2(-90);
            else
            if(l > 90)
                setLatDeg2(90);
            else
                setLatDeg(l);
            repaint();
            oldX = i;
            oldY = j;
        }
    }

    public void mousePressed(MouseEvent mouseevent)
    {
        int i = mouseevent.getX();
        int j = mouseevent.getY();
        bPress = true;
        oldX = i;
        oldY = j;
    }

    public void mouseReleased(MouseEvent mouseevent)
    {
        bPress = false;
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public void mouseEntered(MouseEvent mouseevent)
    {
    }

    public void mouseExited(MouseEvent mouseevent)
    {
    }

    public void mouseMoved(MouseEvent mouseevent)
    {
    }

    public void keyPressed(KeyEvent keyevent)
    {
        int i = keyevent.getKeyCode();
        char c = keyevent.getKeyChar();
        if(c == '+' || c == '=')
        {
            setScaleCorrection((getScaleCorrection() * 8D) / 9D);
            setScale(getScaleInitial() * getScaleCorrection());
            repaint();
        } else
        if(c == '-' || c == '_')
        {
            setScaleCorrection((getScaleCorrection() * 9D) / 8D);
            setScale(getScaleInitial() * getScaleCorrection());
            repaint();
        } else
        {
            KeyEvent _tmp = keyevent;
            if(i == 37)
            {
                setCenterX(getCenterX() + 10);
                repaint();
            } else
            {
                KeyEvent _tmp1 = keyevent;
                if(i == 39)
                {
                    setCenterX(getCenterX() - 10);
                    repaint();
                } else
                {
                    KeyEvent _tmp2 = keyevent;
                    if(i == 38)
                    {
                        setCenterY(getCenterY() + 10);
                        repaint();
                    } else
                    {
                        KeyEvent _tmp3 = keyevent;
                        if(i == 40)
                        {
                            setCenterY(getCenterY() - 10);
                            repaint();
                        } else
                        if(c == 'r' || c == 'R')
                        {
                            setScaleCorrection(1.0D);
                            setScale(getScaleInitial());
                            setCenterX(getCenterXInitial());
                            setCenterY(getCenterYInitial());
                            setLonCenter(0.0D);
                            setLatCenter(0.0D);
                            repaint();
                        }
                    }
                }
            }
        }
    }

    public void keyTyped(KeyEvent keyevent)
    {
    }

    public void keyReleased(KeyEvent keyevent)
    {
    }

    public void setPJID(int i)
    {
        MP_dScale = 300D;
        MP_nPJID = i;
    }

    public void setCoast(boolean flag)
    {
        MP_bCoast = flag;
    }

    public void setGraticule(boolean flag)
    {
        MP_bGraticule = flag;
    }

    public void setOutline(boolean flag)
    {
        MP_bOutline = flag;
    }

    public void setIndicatrix(boolean flag)
    {
        MP_bIndicatrix = flag;
    }

    public void setCircle(boolean flag)
    {
        MP_bCircle = flag;
    }

    public void setAzimuthal(boolean flag)
    {
        MP_bAzimuthal = flag;
    }

    public void setGCircle(boolean flag)
    {
        MP_bGCircle = flag;
    }

    public void setLonDeg(int i)
    {
        MP_nLonDeg = i;
        MP_dLonCenter = DegMin2Deg(MP_nLonDeg, MP_nLonMin);
    }

    public void setLonDeg2(int i)
    {
        MP_nLonDeg = i;
        MP_dLonCenter = DegMin2Deg(MP_nLonDeg, MP_nLonMin);
    }

    public void setLonMin(int i)
    {
        MP_nLonMin = i;
        MP_dLonCenter = DegMin2Deg(MP_nLonDeg, MP_nLonMin);
    }

    public void setLatDeg(int i)
    {
        MP_nLatDeg = i;
        MP_dLatCenter = DegMin2Deg(MP_nLatDeg, MP_nLatMin);
    }

    public void setLatDeg2(int i)
    {
        MP_nLatDeg = i;
        MP_dLatCenter = DegMin2Deg(MP_nLatDeg, MP_nLatMin);
    }

    public void setLatMin(int i)
    {
        MP_nLatMin = i;
        MP_dLatCenter = DegMin2Deg(MP_nLatDeg, MP_nLatMin);
    }

    public void setEqdLonDeg(int i)
    {
        MP_nEqdLonDeg = i;
    }

    public void setEqdLonMin(int i)
    {
        MP_nEqdLonMin = i;
    }

    public void setEqdLatDeg(int i)
    {
        MP_nEqdLatDeg = i;
    }

    public void setEqdLatMin(int i)
    {
        MP_nEqdLatMin = i;
    }

    public void setAzmLonDeg(int i)
    {
        MP_nAzmLonDeg = i;
    }

    public void setAzmLonMin(int i)
    {
        MP_nAzmLonMin = i;
    }

    public void setAzmLatDeg(int i)
    {
        MP_nAzmLatDeg = i;
    }

    public void setAzmLatMin(int i)
    {
        MP_nAzmLatMin = i;
    }

    public void setSrcLonDeg(int i)
    {
        MP_nSrcLonDeg = i;
    }

    public void setSrcLonMin(int i)
    {
        MP_nSrcLonMin = i;
    }

    public void setSrcLatDeg(int i)
    {
        MP_nSrcLatDeg = i;
    }

    public void setSrcLatMin(int i)
    {
        MP_nSrcLatMin = i;
    }

    public void setDstLonDeg(int i)
    {
        MP_nDstLonDeg = i;
    }

    public void setDstLonMin(int i)
    {
        MP_nDstLonMin = i;
    }

    public void setDstLatDeg(int i)
    {
        MP_nDstLatDeg = i;
    }

    public void setDstLatMin(int i)
    {
        MP_nDstLatMin = i;
    }

    public void setScale(double d)
    {
        MP_dScale = d;
    }

    public void setScaleCorrection(double d)
    {
        MP_dScaleCorrection = d;
    }

    public void setCenterX(int i)
    {
        MP_nCenterX = i;
    }

    public void setCenterY(int i)
    {
        MP_nCenterY = i;
    }

    public void setLonCenter(double d)
    {
        MP_dLonCenter = d;
    }

    public void setLatCenter(double d)
    {
        MP_dLatCenter = d;
    }

    public void setCenterXInitial(int i)
    {
        MP_nCenterXInitial = i;
    }

    public void setCenterYInitial(int i)
    {
        MP_nCenterYInitial = i;
    }

    public void setInterval(int i)
    {
        MP_nInterval = i;
    }

    public void setDistance(double d)
    {
        MP_dDistance = d;
    }

    public void setOblique(boolean flag)
    {
        MP_bOblique = flag;
    }

    public void setObliqueY(double d)
    {
        MP_dObliqY = d;
    }

    public void setSp1Deg(int i)
    {
        MP_nSp1Deg = i;
        MP_dSp1 = DegMin2Deg(MP_nSp1Deg, MP_nSp1Min);
    }

    public void setSp1Min(int i)
    {
        MP_nSp1Min = i;
        MP_dSp1 = DegMin2Deg(MP_nSp1Deg, MP_nSp1Min);
    }

    public void setSp2Deg(int i)
    {
        MP_nSp2Deg = i;
        MP_dSp2 = DegMin2Deg(MP_nSp2Deg, MP_nSp2Min);
    }

    public void setSp2Min(int i)
    {
        MP_nSp2Min = i;
        MP_dSp2 = DegMin2Deg(MP_nSp2Deg, MP_nSp2Min);
    }

    public void setTx(double d)
    {
        m_Tx = d;
    }

    public void setTy(double d)
    {
        m_Ty = d;
    }

    public void setTdist(double d)
    {
        m_Tdist = d;
    }

    public void setTphi(double d)
    {
        m_Tphi = d;
    }

    public int getPJID()
    {
        return MP_nPJID;
    }

    public boolean isCoast()
    {
        return MP_bCoast;
    }

    public boolean isGraticule()
    {
        return MP_bGraticule;
    }

    public boolean isOutline()
    {
        return MP_bOutline;
    }

    public boolean isIndicatrix()
    {
        return MP_bIndicatrix;
    }

    public boolean isCircle()
    {
        return MP_bCircle;
    }

    public boolean isAzimuthal()
    {
        return MP_bAzimuthal;
    }

    public boolean isGCircle()
    {
        return MP_bGCircle;
    }

    public int getLonDeg()
    {
        return MP_nLonDeg;
    }

    public int getLonMin()
    {
        return MP_nLonMin;
    }

    public int getLatDeg()
    {
        return MP_nLatDeg;
    }

    public int getLatMin()
    {
        return MP_nLatMin;
    }

    public int getEqdLonDeg()
    {
        return MP_nEqdLonDeg;
    }

    public int getEqdLonMin()
    {
        return MP_nEqdLonMin;
    }

    public int getEqdLatDeg()
    {
        return MP_nEqdLatDeg;
    }

    public int getEqdLatMin()
    {
        return MP_nEqdLatMin;
    }

    public int getAzmLonDeg()
    {
        return MP_nAzmLonDeg;
    }

    public int getAzmLonMin()
    {
        return MP_nAzmLonMin;
    }

    public int getAzmLatDeg()
    {
        return MP_nAzmLatDeg;
    }

    public int getAzmLatMin()
    {
        return MP_nAzmLatMin;
    }

    public int getSrcLonDeg()
    {
        return MP_nSrcLonDeg;
    }

    public int getSrcLonMin()
    {
        return MP_nSrcLonMin;
    }

    public int getSrcLatDeg()
    {
        return MP_nSrcLatDeg;
    }

    public int getSrcLatMin()
    {
        return MP_nSrcLatMin;
    }

    public int getDstLonDeg()
    {
        return MP_nDstLonDeg;
    }

    public int getDstLonMin()
    {
        return MP_nDstLonMin;
    }

    public int getDstLatDeg()
    {
        return MP_nDstLatDeg;
    }

    public int getDstLatMin()
    {
        return MP_nDstLatMin;
    }

    public double getScale()
    {
        return MP_dScale;
    }

    public double getScaleInitial()
    {
        return MP_dScaleInitial;
    }

    public double getScaleCorrection()
    {
        return MP_dScaleCorrection;
    }

    public int getCenterX()
    {
        return MP_nCenterX;
    }

    public int getCenterY()
    {
        return MP_nCenterY;
    }

    public int getCenterXInitial()
    {
        return MP_nCenterXInitial;
    }

    public int getCenterYInitial()
    {
        return MP_nCenterYInitial;
    }

    public double getDistance()
    {
        return MP_dDistance;
    }

    public double getTx()
    {
        return m_Tx;
    }

    public double getTy()
    {
        return m_Ty;
    }

    public double getTdist()
    {
        return m_Tdist;
    }

    public double getTphi()
    {
        return m_Tphi;
    }

    public int getInterval()
    {
        return MP_nInterval;
    }

    public int getSp1Deg()
    {
        return MP_nSp1Deg;
    }

    public int getSp1Min()
    {
        return MP_nSp1Min;
    }

    public int getSp2Deg()
    {
        return MP_nSp2Deg;
    }

    public int getSp2Min()
    {
        return MP_nSp2Min;
    }

    public boolean isOblique()
    {
        return MP_bOblique;
    }

/*    public void setProject(PjMapProjection pjmapprojection)
    {
        m_p = pjmapprojection;
    }*/

    private boolean Projection(MapPoint mappoint)
    {
        switch(MP_nPJID)
        {
        case 1100: 
            return MP_Adams(mappoint);

        case 1150: 
            return MP_Airy(mappoint);

        case 1200: 
            return MP_Aitoff(mappoint);

        case 1250: 
            return MP_Albers(mappoint);

        case 1300: 
            return MP_Apianus(mappoint);

        case 1350: 
            return MP_Apianus2(mappoint);

        case 1400: 
            return MP_Armadillo(mappoint);

        case 1450: 
            return MP_August(mappoint);

        case 1500: 
            return MP_AzimuthalCentral(mappoint);

        case 1550: 
            return MP_AzimuthalEquidistant(mappoint);

        case 1600: 
            return MP_AzimuthalOrthographic(mappoint);

        case 1650: 
            return MP_AzimuthalStereographic(mappoint);

        case 1700: 
            return MP_Bacon(mappoint);

        case 1750: 
            return MP_Behrmann(mappoint);

        case 1800: 
            return MP_Boggs(mappoint);

        case 1850: 
            return MP_Bonne(mappoint);

        case 1900: 
            return MP_Bolshoi(mappoint);

        case 1950: 
            return MP_Braun(mappoint);

        case 2000: 
            return MP_Briesemeister(mappoint);

        case 2050: 
            return MP_Collignon(mappoint);

        case 2100: 
            return MP_ConicEquidistant(mappoint);

        case 2150: 
            return MP_Craig(mappoint);

        case 2200: 
            return MP_Craster(mappoint);

        case 2250: 
            return MP_CylindricalCentral(mappoint);

        case 2300: 
            return MP_CylindricalEquidistant(mappoint);

        case 2350: 
            return MP_CylindricalOrthographic(mappoint);

        case 2400: 
            return MP_CylindricalPerspective(mappoint);

        case 2450: 
            return MP_Denoyer(mappoint);

        case 2500: 
            return MP_Eckert1(mappoint);

        case 2550: 
            return MP_Eckert2(mappoint);

        case 2600: 
            return MP_Eckert3(mappoint);

        case 2650: 
            return MP_Eckert4(mappoint);

        case 2700: 
            return MP_Eckert5(mappoint);

        case 2750: 
            return MP_Eckert6(mappoint);

        case 2800: 
            return MP_Fahey(mappoint);

        case 2850: 
            return MP_Fournier(mappoint);

        case 2900: 
            return MP_Gall(mappoint);

        case 2950: 
            return MP_Goode(mappoint);

        case 3000: 
            return MP_HammerAitoff(mappoint);

        case 3050: 
            return MP_HammerWagner(mappoint);

        case 3100: 
            return MP_Hatano(mappoint);

        case 3150: 
            return MP_Kavraisky5(mappoint);

        case 3200: 
            return MP_Lagrange1(mappoint);

        case 3250: 
            return MP_LambertAzimuthalEqualArea(mappoint);

        case 3300: 
            return MP_LambertConformalConic(mappoint);

        case 3350: 
            return MP_LambertConicEqualArea(mappoint);

        case 3400: 
            return MP_LambertCylindricalEqualArea(mappoint);

        case 3450: 
            return MP_Larrivee(mappoint);

        case 3500: 
            return MP_Laskowski(mappoint);

        case 3550: 
            return MP_Littrow(mappoint);

        case 3600: 
            return MP_Loximuthal(mappoint);

        case 3650: 
            return MP_McbrydeThomas1(mappoint);

        case 3700: 
            return MP_Mercator(mappoint);

        case 3750: 
            return MP_Miller1(mappoint);

        case 3800: 
            return MP_Miller2(mappoint);

        case 3850: 
            return MP_Mollweide(mappoint);

        case 3900: 
            return MP_NellHammer(mappoint);

        case 4000: 
            return MP_Nicolosi(mappoint);

        case 4050: 
            return MP_Ortelius(mappoint);

        case 4100: 
            return MP_Pavlov(mappoint);

        case 4150: 
            return MP_Peirce(mappoint);

        case 1675: 
            return MP_AzimuthalPerspective(mappoint);

        case 4200: 
            return MP_Peters(mappoint);

        case 4250: 
            return MP_PlateCarree(mappoint);

        case 4300: 
            return MP_Polyconic(mappoint);

        case 4350: 
            return MP_Quartic(mappoint);

        case 4400: 
            return MP_Robinson(mappoint);

        case 4450: 
            return MP_Sanson(mappoint);

        case 4500: 
            return MP_Urmaev3(mappoint);

        case 4550: 
            return MP_VanderGrinten1(mappoint);

        case 4600: 
            return MP_Wagner4(mappoint);

        case 4650: 
            return MP_Werenskiold1(mappoint);

        case 4700: 
            return MP_Winkel1(mappoint);

        case 4750: 
            return MP_Winkel2(mappoint);
        }
        return false;
    }

    private void PJInit(MapPoint mappoint)
    {
        MapPoint mappoint1 = new MapPoint();
        radius = 24076D / MP_dScale;
        mappoint1.x = MP_dLonCenter;
        mappoint1.y = MP_dLatCenter;
        new_x = getNewX(mappoint1, mappoint);
        new_y = getNewY(mappoint);
        if(MP_bOblique && MP_dObliqY != 90D)
            getOblique();
    }

    public void init()
    {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    private double getNewY(MapPoint mappoint)
    {
        double d = Math.sin(mappoint.y * MathUtil.deg2rad) * Math.sin(newp.y * MathUtil.deg2rad) + Math.cos(mappoint.y * MathUtil.deg2rad) * Math.cos(newp.y * MathUtil.deg2rad) * Math.cos((mappoint.x - newp.x) * MathUtil.deg2rad);
        double d1;
        if(d >= 1.0D)
            d1 = 0.0D;
        else
        if(d <= -1D)
            d1 = Math.PI;
        else
        if(d == 0.0D)
        {
            d1 = (Math.PI/2);
        } else
        {
            d1 = Math.acos(d);
            if(d1 < 0.0D)
                d1 += Math.PI;
        }
        d1 = (Math.PI/2) - d1;
        if(d1 > (Math.PI/2))
            d1 = (Math.PI/2);
        else
        if(d1 < -(Math.PI/2))
            d1 = -(Math.PI/2);
        return d1;
    }

    private double getNewX(MapPoint mappoint, MapPoint mappoint1)
    {
        double d = 0.0D;
        if(mappoint.y == 0.0D)
            return adjustCenter(mappoint1.x, mappoint.x) * MathUtil.deg2rad;
        double d1 = (Math.PI/2) - getNewY(mappoint1);
        if(d1 != 0.0D)
        {
            double d2 = (Math.sin((mappoint1.x - newp.x) * MathUtil.deg2rad) * Math.cos(mappoint1.y * MathUtil.deg2rad)) / Math.sin(d1);
            double d3 = (Math.cos(newp.y * MathUtil.deg2rad) * Math.sin(mappoint1.y * MathUtil.deg2rad) - Math.sin(newp.y * MathUtil.deg2rad) * Math.cos(mappoint1.y * MathUtil.deg2rad) * Math.cos((mappoint1.x - newp.x) * MathUtil.deg2rad)) / Math.sin(d1);
            if(d3 != 0.0D)
                d = Math.atan(d2 / d3);
            else
            if(d2 >= 0.0D)
                d = (Math.PI/2);
            else
            if(d2 < 0.0D)
                d = -(Math.PI/2);
            if(d3 < 0.0D)
                d += Math.PI;
            if(d > Math.PI)
                d -= (Math.PI*2);
            else
            if(d < -Math.PI)
                d += (Math.PI*2);
        } else
        {
            d = 0.0D;
        }
        return -d;
    }

    private double adjustCenter(double d, double d1)
    {
        if(d1 >= 0.0D && d1 < 180D)
        {
            if(d >= d1 - 180D)
                d -= d1;
            else
                d = (d - d1) + 360D;
        } else
        if(d1 < 0.0D && d1 > -180D)
        {
            if(d <= d1 + 180D && d > -180D)
                d -= d1;
            else
            if(d > d1 + 180D && d <= 180D)
                d = d - d1 - 360D;
        } else
        if(d1 == 180D)
        {
            if(d >= 0.0D && d <= 180D)
                d -= d1;
            else
            if(d < 0.0D && d >= -180D)
                d += d1;
        } else
        if(d1 == -180D)
            if(d >= 0.0D && d <= 180D)
                d += d1;
            else
            if(d < 0.0D && d >= -180D)
                d -= d1;
        return d;
    }

    public void getOblique()
    {
        double d = MP_dObliqY * MathUtil.deg2rad;
        double d1 = Math.cos(d) * Math.sin(new_y) - Math.sin(d) * Math.cos(new_y) * Math.sin(new_x);
        double d2 = Math.sin(d) * Math.sin(new_y) + Math.cos(d) * Math.cos(new_y) * Math.sin(new_x);
        double d3 = Math.atan2(Math.cos(new_y) * Math.cos(new_x), d1) - (Math.PI/2);
        if(d3 < -Math.PI)
            d3 += (Math.PI*2);
        if(d3 > Math.PI)
            d3 -= (Math.PI*2);
        double d4 = Math.asin(d2);
        new_x = d3;
        new_y = d4;
    }

    private void ProjectGraticule(Graphics g)
    {
        for(int i = -180; i <= 180; i += 30)
        {
            int k = -90;
            int i1;
            for(i1 = 0; k <= 90; i1++)
            {
                fpx[i1] = i;
                fpy[i1] = k;
                k++;
            }

            ProjectLines(g, i1);
        }

        for(int j = -90; j <= 90; j += 30)
        {
            int l = -180;
            int j1;
            for(j1 = 0; l <= 180; j1++)
            {
                fpx[j1] = l;
                fpy[j1] = j;
                l++;
            }

            ProjectLines(g, j1);
        }

    }

    private void ProjectParallel(Graphics g, double d)
    {
        int i = -180;
        int j;
        for(j = 0; i <= 180; j++)
        {
            fpx[j] = i;
            fpy[j] = d;
            i++;
        }

        ProjectLines(g, j);
    }

    private void ProjectMeridian(Graphics g, double d)
    {
        int i = -90;
        int j;
        for(j = 0; i <= 90; j++)
        {
            fpx[j] = d;
            fpy[j] = i;
            i++;
        }

        ProjectLines(g, j);
    }

    private double Approximate(int i, int j, double d)
    {
        double d1 = 0.0D;
        double d2 = 1.5700000000000001D;
        byte byte0 = (byte)(d >= 0.0D ? 1 : -1);
        for(int k = 1; k <= j; k++)
        {
            double d3 = f(i, d1, d, byte0);
            double d4 = f(i, d2, d, byte0);
            double d5 = (d1 + d2) / 2D;
            double d6 = f(i, d5, d, byte0);
            if(d3 == 0.0D)
                return d1;
            if(d6 == 0.0D)
                return d5;
            if(d4 == 0.0D)
                return d2;
            if(d6 * d3 < 0.0D)
                d2 = d5;
            else
                d1 = d5;
        }

        return (d1 + d2) / 2D;
    }

    private double f(int i, double d, double d1, int j)
    {
        switch(i)
        {
        case 39: // '\''
            return f_mol(d, d1, j);

        case 43: // '+'
            return f_eck4(d, d1, j);

        case 45: // '-'
            return f_eck6(d, d1, j);

        case 66: // 'B'
            return f_mcbr1(d, d1, j);

        case 78: // 'N'
            return f_wag4(d, d1, j);

        case 151: 
            return f_hatano_n(d, d1, j);

        case 150: 
            return f_hatano_s(d, d1, j);
        }
        return 0.0D;
    }

    private double f_mol(double d, double d1, int i)
    {
        return (2D * d + Math.sin(2D * d)) - (double)i * Math.PI * Math.sin(d1);
    }

    private double f_eck6(double d, double d1, int i)
    {
        return 0.38896199999999997D * (d + Math.sin(d)) - (double)i * Math.sin(d1);
    }

    private double f_eck4(double d, double d1, int i)
    {
        return (2D * d + 4D * Math.sin(d) + Math.sin(2D * d)) - (double)i * 7.1415899999999999D * Math.sin(d1);
    }

    private double f_mcbr1(double d, double d1, int i)
    {
        return (0.5D * d + Math.sin(d)) - (double)i * 1.7854000000000001D * Math.sin(d1);
    }

    private double f_wag4(double d, double d1, int i)
    {
        return (2D * d + Math.sin(2D * d)) - (double)i * Math.PI * ((2D * Math.acos(0.5D) + Math.sin(2D * Math.acos(0.5D))) / Math.PI) * Math.sin(d1);
    }

    private double f_hatano_n(double d, double d1, int i)
    {
        return (2D * d + Math.sin(2D * d)) - (double)i * 2.6759499999999998D * Math.sin(d1);
    }

    private double f_hatano_s(double d, double d1, int i)
    {
        return (2D * d + Math.sin(2D * d)) - (double)i * 2.43763D * Math.sin(d1);
    }

    private boolean MP_Adams(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 2D;
        double d1 = 2D;
        mappoint.x = (radius * (d1 / d) * new_x * Math.cos(new_y)) / Math.cos(new_y / d1);
        mappoint.y = radius * d * Math.sin(new_y / d1);
        return true;
    }

    private boolean MP_Airy(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.52359833333333328D;
        double d1 = 0.0D;
        double d2 = Math.acos(Math.sin(d1) * Math.sin(new_y) + Math.cos(d1) * Math.cos(new_y) * Math.cos(new_x));
        double d3 = Math.log((1.0D + Math.cos(d2)) / 2D);
        double d4 = Math.log(Math.cos(d));
        double d5 = Math.tan(d) * Math.tan(d);
        double d6 = d2 == 0.0D ? 0.5D - d4 / d5 : -(d3 / (1.0D - Math.cos(d2)) + (2D * d4) / (d5 * (1.0D + Math.cos(d2))));
        double d7 = Math.cos(d1) * Math.sin(new_y) - Math.sin(d1) * Math.cos(new_y) * Math.cos(new_x);
        mappoint.x = radius * d6 * Math.cos(new_y) * Math.sin(new_x);
        mappoint.y = radius * d6 * d7;
        return true;
    }

    private boolean MP_Aitoff(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Math.cos(new_y) * Math.cos(new_x / 2D);
        double d1 = Math.sqrt(1.0D - d * d);
        if(d1 != 0.0D)
        {
            double d2 = (Math.sin(new_x / 2D) * Math.cos(new_y)) / d1;
            double d3 = -Math.sin(new_y) / d1;
            double d4 = (Math.PI/2) - Math.asin(d);
            mappoint.x = 2D * radius * d4 * d2;
            mappoint.y = -radius * d4 * d3;
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_Albers(MapPoint mappoint)
    {
        PJInit(mappoint);
        lat = MP_dSp1 * MathUtil.deg2rad;
        double d = MP_dSp2 * MathUtil.deg2rad;
        double d1 = (Math.sin(lat) + Math.sin(d)) / 2D;
        if(lat + d != 0.0D)
        {
            if(d1 != 0.0D)
            {
                double d2 = (radius * Math.cos(lat)) / d1;
                double d4 = Math.sqrt(d2 * d2 + (2D * radius * radius * (Math.sin(lat) - Math.sin(new_y))) / d1);
                double d5 = d1 * new_x;
                if(d1 < 0.0D)
                {
                    mappoint.x = -d4 * Math.sin(d5);
                    mappoint.y = radius / Math.tan(d1) + d4 * Math.cos(d5);
                } else
                {
                    mappoint.x = d4 * Math.sin(d5);
                    mappoint.y = radius / Math.tan(d1) - d4 * Math.cos(d5);
                }
            }
        } else
        {
            double d3 = d >= 0.0D ? d : lat;
            mappoint.x = radius * new_x * Math.cos(d3);
            mappoint.y = (radius * Math.sin(new_y)) / Math.cos(d3);
        }
        return true;
    }

    private boolean MP_Apianus(MapPoint mappoint)
    {
        double d = (Math.PI/2);
        PJInit(mappoint);
        if(new_x > 1.5533417222222221D || new_x < -1.5533417222222221D)
            return false;
        mappoint.y = radius * new_y;
        if(new_x == 0.0D)
        {
            mappoint.x = 0.0D;
        } else
        {
            double d1 = new_x > 0.0D ? new_x : -new_x;
            double d2 = ((d * d) / d1 + d1) / 2D;
            double d3 = Math.sqrt(d2 * d2 - (mappoint.y * mappoint.y) / (radius * radius));
            mappoint.x = new_x > 0.0D ? radius * ((d1 - d2) + d3) : -radius * ((d1 - d2) + d3);
        }
        return true;
    }

    private boolean MP_Apianus2(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.y = radius * new_y;
        double d = Math.pow((radius * Math.PI) / 2D, 2D) - Math.pow(mappoint.y, 2D);
        mappoint.x = d > 0.0D ? (2D * new_x * Math.sqrt(d)) / Math.PI : 0.0D;
        return true;
    }

    private boolean MP_Armadillo(MapPoint mappoint)
    {
        double d = Math.sin(0.34906555555555552D);
        double d1 = Math.cos(0.34906555555555552D);
        double d2 = Math.tan(0.34906555555555552D);
        PJInit(mappoint);
        if(new_y < -Math.atan(Math.cos(new_x / 2D) * d2))
        {
            return false;
        } else
        {
            mappoint.x = radius * (1.0D + Math.cos(new_y)) * Math.sin(new_x / 2D);
            mappoint.y = radius * ((((1.0D + d) - d1) / 2D + Math.sin(new_y) * d1) - (1.0D + Math.cos(new_y)) * d * Math.cos(new_x / 2D));
            return true;
        }
    }

    private boolean MP_August(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Math.sqrt(1.0D - Math.tan(new_y / 2D) * Math.tan(new_y / 2D));
        double d1 = 1.0D + d * Math.cos(new_x / 2D);
        if(d1 == 0.0D)
        {
            return false;
        } else
        {
            double d2 = (Math.sin(new_x / 2D) * d) / d1;
            double d3 = Math.tan(new_y / 2D) / d1;
            mappoint.x = (4D * radius * d2 * ((3D + d2 * d2) - 3D * d3 * d3)) / 3D;
            mappoint.y = (4D * radius * d3 * ((3D + 3D * d2 * d2) - d3 * d3)) / 3D;
            return true;
        }
    }

    private boolean MP_AzimuthalCentral(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_x >= -1.1634354966666665D && new_x <= 1.1634354966666665D && new_y >= -1.1634354966666665D && new_y <= 1.1634354966666665D)
        {
            double d = 0.0D;
            double d1 = Math.acos(Math.sin(d) * Math.sin(new_y) + Math.cos(d) * Math.cos(new_y) * Math.cos(new_x));
            double d2 = 1.0D / Math.cos(d1);
            double d3 = Math.cos(d) * Math.sin(new_y) - Math.sin(d) * Math.cos(new_y) * Math.cos(new_x);
            mappoint.x = radius * d2 * Math.cos(new_y) * Math.sin(new_x);
            mappoint.y = radius * d2 * d3;
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_AzimuthalEquidistant(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.0D;
        double d1 = Math.acos(Math.sin(d) * Math.sin(new_y) + Math.cos(d) * Math.cos(new_y) * Math.cos(new_x));
        double d2 = d1 == 0.0D ? 0.0D : d1 / Math.sin(d1);
        double d3 = Math.cos(d) * Math.sin(new_y) - Math.sin(d) * Math.cos(new_y) * Math.cos(new_x);
        mappoint.x = radius * d2 * Math.cos(new_y) * Math.sin(new_x);
        mappoint.y = radius * d2 * d3;
        return true;
    }

    private boolean MP_AzimuthalOrthographic(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_x >= -(Math.PI/2) && new_x <= (Math.PI/2) && new_y >= -(Math.PI/2) && new_y <= (Math.PI/2))
        {
            double d = 0.0D;
            double d1 = Math.acos(Math.sin(d) * Math.sin(new_y) + Math.cos(d) * Math.cos(new_y) * Math.cos(new_x));
            double d2 = 1.0D;
            double d3 = Math.cos(d) * Math.sin(new_y) - Math.sin(d) * Math.cos(new_y) * Math.cos(new_x);
            mappoint.x = radius * d2 * Math.cos(new_y) * Math.sin(new_x);
            mappoint.y = radius * d2 * d3;
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_AzimuthalStereographic(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.0D;
        double d1 = 1.0D;
        double d2 = Math.acos(Math.sin(d) * Math.sin(new_y) + Math.cos(d) * Math.cos(new_y) * Math.cos(new_x));
        if(Math.cos(d2) == -1D)
        {
            return false;
        } else
        {
            double d3 = (2D * d1) / (1.0D + Math.cos(d2));
            double d4 = Math.cos(d) * Math.sin(new_y) - Math.sin(d) * Math.cos(new_y) * Math.cos(new_x);
            mappoint.x = radius * d3 * Math.cos(new_y) * Math.sin(new_x);
            mappoint.y = radius * d3 * Math.sin(new_y);
            return true;
        }
    }

    private boolean MP_Bacon(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = (Math.PI/2);
        if(new_x > (Math.PI/2) || new_x < -(Math.PI/2))
            return false;
        mappoint.y = radius * d * Math.sin(new_y);
        if(new_x == 0.0D)
        {
            mappoint.x = 0.0D;
        } else
        {
            double d1 = new_x > 0.0D ? new_x : -new_x;
            double d2 = ((d * d) / d1 + d1) / 2D;
            double d3 = Math.sqrt(d2 * d2 - (mappoint.y * mappoint.y) / (radius * radius));
            mappoint.x = new_x > 0.0D ? radius * ((d1 - d2) + d3) : -radius * ((d1 - d2) + d3);
        }
        return true;
    }

    private boolean MP_Behrmann(MapPoint mappoint)
    {
        double d = 30D;
        PJInit(mappoint);
        mappoint.x = radius * new_x * Math.cos(d * MathUtil.deg2rad);
        mappoint.y = (radius * Math.sin(new_y)) / Math.cos(d * MathUtil.deg2rad);
        return true;
    }

    private boolean MP_Boggs(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Approximate(39, 10, new_y);
        mappoint.y = new_y >= 0.0D ? radius * 0.49930999999999998D * (new_y + Math.sqrt(2D) * Math.sin(d)) : -radius * 0.49930999999999998D * (-new_y + Math.sqrt(2D) * Math.sin(d));
        if(Math.cos(new_y) == 0.0D)
        {
            mappoint.x = 0.0D;
        } else
        {
            double d1 = 1.0D / Math.cos(new_y) + 1.1107199999999999D / Math.cos(new_y);
            if(d1 == 0.0D)
                mappoint.x = 0.0D;
            mappoint.x = (2.0027599999999999D * radius * new_x) / d1;
        }
        return true;
    }

    private boolean MP_Bonne(MapPoint mappoint)
    {
        lat = MP_dSp1 * MathUtil.deg2rad;
        PJInit(mappoint);
        if(Math.sin(lat) != 0.0D)
        {
            double d = radius * ((Math.cos(lat) / Math.sin(lat) - new_y) + lat);
            double d1;
            if(d != 0.0D)
                d1 = (radius * new_x * Math.cos(new_y)) / d;
            else
                return false;
            mappoint.x = d * Math.sin(d1);
            mappoint.y = radius / Math.tan(lat) - d * Math.cos(d1);
        } else
        {
            mappoint.x = radius * (new_x * Math.cos(new_y));
            mappoint.y = radius * new_y;
        }
        return true;
    }

    private boolean MP_Bolshoi(MapPoint mappoint)
    {
        double d = 30D;
        PJInit(mappoint);
        mappoint.x = radius * new_x * Math.cos(d * MathUtil.deg2rad);
        mappoint.y = (1.0D + Math.cos(d * MathUtil.deg2rad)) * radius * Math.tan(new_y / 2D);
        return true;
    }

    private boolean MP_Braun(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.x = radius * new_x * Math.cos(MP_dSp1 * MathUtil.deg2rad);
        mappoint.y = (1.0D + Math.cos(MP_dSp1 * MathUtil.deg2rad)) * radius * Math.tan(new_y / 2D);
        return true;
    }

    private boolean MP_Briesemeister(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.93541399999999997D;
        double d1 = new_y;
        double d2 = new_x;
        mappoint.x = (2D * radius * Math.sqrt(2D) * d * Math.cos(d1) * Math.sin(d2 / 2D)) / Math.sqrt(1.0D + Math.cos(d1) * Math.cos(d2 / 2D));
        mappoint.y = (radius * Math.sqrt(2D) * Math.sin(d1)) / (d * Math.sqrt(1.0D + Math.cos(d1) * Math.cos(d2 / 2D)));
        return true;
    }

    private boolean MP_CylindricalCentral(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.x = radius * new_x * Math.cos(MP_dSp1 * MathUtil.deg2rad);
        mappoint.y = radius * Math.cos(MP_dSp1 * MathUtil.deg2rad) * Math.tan(new_y);
        return true;
    }

    private boolean MP_Collignon(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 1.1283791670955126D;
        double d1 = 1.7724538509055161D;
        double d2 = 1.0D - Math.sin(new_y);
        d2 = d2 <= 0.0D ? 0.0D : Math.sqrt(d2);
        mappoint.x = radius * d * new_x * d2;
        mappoint.y = radius * d1 * (1.0D - d2);
        return true;
    }

    private boolean MP_ConicEquidistant(MapPoint mappoint)
    {
        PJInit(mappoint);
        lat = MP_dSp1 * MathUtil.deg2rad;
        double d = MP_dSp2 * MathUtil.deg2rad;
        double d1 = Math.cos(lat);
        double d2 = Math.cos(d);
        if(d == -lat)
        {
            double d3 = d >= 0.0D ? d : lat;
            mappoint.x = radius * new_x * Math.cos(d3);
            mappoint.y = radius * new_y;
        } else
        if(d - lat != 0.0D)
        {
            double d4 = (d1 - d2) / (d - lat);
            double d6 = radius * ((d * d1 - lat * d2) / (d1 - d2));
            double d8 = radius * ((d * d1 - lat * d2) / (d1 - d2) - new_y);
            double d9 = new_x * d4;
            mappoint.x = d8 * Math.sin(d9);
            mappoint.y = d6 - d8 * Math.cos(d9);
        } else
        if(lat != 0.0D)
        {
            double d5 = radius * ((Math.cos(lat) / Math.sin(lat) - new_y) + lat);
            double d7 = new_x * Math.sin(lat);
            mappoint.x = d5 * Math.sin(d7);
            mappoint.y = radius / Math.tan(lat) - d5 * Math.cos(d7);
        }
        return true;
    }

    private boolean MP_Craig(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_y < -0.52359833333333328D)
            return false;
        if(new_x > (Math.PI/2) || new_x < -(Math.PI/2))
            return false;
        if(new_x == 0.0D)
        {
            mappoint.x = 0.0D;
            mappoint.y = radius * (Math.sin(new_y) - Math.cos(new_y) * Math.tan(MP_dSp1 * MathUtil.deg2rad));
        } else
        {
            mappoint.x = radius * new_x;
            mappoint.y = (radius * new_x * (Math.sin(new_y) * Math.cos(new_x) - Math.cos(new_y) * Math.tan(MP_dSp1 * MathUtil.deg2rad))) / Math.sin(new_x);
        }
        return true;
    }

    private boolean MP_Craster(MapPoint mappoint)
    {
        double d = 1.5349889999999999D;
        PJInit(mappoint);
        mappoint.x = (2D * radius * d * (2D * Math.cos((2D * new_y) / 3D) - 1.0D) * new_x) / Math.PI;
        mappoint.y = 2D * radius * d * Math.sin(new_y / 3D);
        return true;
    }

    private boolean MP_CylindricalEquidistant(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.y = radius * new_y;
        mappoint.x = radius * new_x * Math.cos(MP_dSp1 * MathUtil.deg2rad);
        return true;
    }

    private boolean MP_CylindricalOrthographic(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.x = radius * new_x * Math.cos(MP_dSp1 * MathUtil.deg2rad);
        mappoint.y = radius * Math.sin(new_y);
        return true;
    }

    private boolean MP_CylindricalPerspective(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = radius * (MP_dDistance - 1.0D);
        mappoint.x = radius * new_x * Math.cos(MP_dSp1 * MathUtil.deg2rad);
        mappoint.y = (radius * Math.sin(new_y) * (d + radius * (1.0D + Math.cos(MP_dSp1 * MathUtil.deg2rad)))) / (d + radius * (1.0D + Math.cos(new_y)));
        return true;
    }

    private boolean MP_Denoyer(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.94999999999999996D;
        double d1 = -0.083333333333333329D;
        double d2 = 0.0016666666666666666D;
        double d3 = 0.90000000000000002D;
        double d4 = 0.029999999999999999D;
        double d5 = Math.abs(new_x);
        double d6 = d1 + d5 * d5 * d2;
        double d7 = d3 + d4 * Math.pow(new_y, 4D);
        mappoint.x = radius * new_x * Math.cos((d + d5 * d6) * new_y * d7);
        mappoint.y = radius * new_y;
        return true;
    }

    private boolean MP_Eckert1(MapPoint mappoint)
    {
        double d = 1.4472D;
        PJInit(mappoint);
        mappoint.y = (2D * radius * d * new_y) / Math.PI;
        mappoint.x = new_y >= 0.0D ? (new_x * (2D * radius * d - mappoint.y)) / Math.PI : (new_x * (2D * radius * d + mappoint.y)) / Math.PI;
        return true;
    }

    private boolean MP_Eckert2(MapPoint mappoint)
    {
        double d = 1.4472D;
        PJInit(mappoint);
        if(new_y >= 0.0D)
        {
            mappoint.y = 2D * radius * d - Math.sqrt(4D * radius * d * radius * d - (Math.PI*2) * radius * radius * Math.sin(new_y));
            mappoint.x = (new_x * (2D * radius * d - mappoint.y)) / Math.PI;
        } else
        {
            mappoint.y = -(2D * radius * d - Math.sqrt(4D * radius * d * radius * d - (Math.PI*2) * radius * radius * Math.sin(-new_y)));
            mappoint.x = (new_x * (2D * radius * d + mappoint.y)) / Math.PI;
        }
        return true;
    }

    private boolean MP_Eckert3(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = radius * 1.3265D;
        mappoint.y = (2D * d * new_y) / Math.PI;
        if(d > mappoint.y)
        {
            double d1 = d * d - mappoint.y * mappoint.y;
            if(d1 > 0.0D)
            {
                double d2 = Math.sqrt(d1);
                mappoint.x = (new_x / Math.PI) * (d + d2);
            } else
            {
                return false;
            }
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_Eckert4(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Approximate(43, 8, new_y);
        double d1 = new_x / Math.PI;
        double d2 = radius * 1.3265D;
        if(new_y >= 0.0D)
        {
            mappoint.x = d2 * d1 * (1.0D + Math.cos(d));
            mappoint.y = d2 * Math.sin(d);
        } else
        if(new_y < 0.0D)
        {
            mappoint.x = d2 * d1 * (1.0D + Math.cos(d));
            mappoint.y = -d2 * Math.sin(d);
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_Eckert5(MapPoint mappoint)
    {
        double d = 1.385486D;
        PJInit(mappoint);
        d = 1.385486D;
        mappoint.x = (radius * d * new_x * (1.0D + Math.cos(new_y))) / Math.PI;
        mappoint.y = (2D * radius * d * new_y) / Math.PI;
        return true;
    }

    private boolean MP_Eckert6(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Approximate(45, 8, new_y);
        mappoint.y = new_y >= 0.0D ? radius * 0.88200000000000001D * d : -radius * 0.88200000000000001D * d;
        mappoint.x = radius * 0.441D * new_x * (1.0D + Math.cos(d));
        return true;
    }

    private boolean MP_Fahey(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Math.tan(0.5D * new_y);
        mappoint.x = radius * 0.81915199999999999D * new_x * Math.sqrt(1.0D - d * d);
        mappoint.y = radius * 1.8191520000000001D * d;
        return true;
    }

    private boolean MP_Fournier(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_x > (Math.PI/2) || new_x < -(Math.PI/2))
            return false;
        if(new_x == 0.0D)
        {
            mappoint.x = 0.0D;
            mappoint.y = radius * new_y;
        } else
        if(new_y == 0.0D)
        {
            mappoint.x = radius * new_x;
            mappoint.y = 0.0D;
        } else
        if(new_x == (Math.PI/2) || new_x == -(Math.PI/2))
        {
            mappoint.x = radius * new_x * Math.cos(new_y);
            mappoint.y = radius * (Math.PI/2) * Math.sin(new_y);
        } else
        if(new_y == (Math.PI/2) || new_y == -(Math.PI/2))
        {
            mappoint.x = 0.0D;
            mappoint.y = radius * new_y;
        } else
        {
            double d = new_y >= 0.0D ? new_y : -new_y;
            double d1 = 2.4673969320249998D;
            double d2 = Math.PI * Math.sin(new_y);
            if(d2 < 0.0D)
                d2 *= -1D;
            double d3 = (d1 - new_y * new_y) / (d2 - 2D * d);
            double d4 = (new_x * new_x) / d1 - 1.0D;
            double d5 = d4 * (d1 - d2 * d3 - new_x * new_x);
            if(d4 == 0.0D)
                return false;
            mappoint.y = new_y > 0.0D ? (radius * (Math.sqrt(d3 * d3 - d5) - d3)) / d4 : (-radius * (Math.sqrt(d3 * d3 - d5) - d3)) / d4;
            mappoint.x = radius * new_x * Math.sqrt(1.0D - (mappoint.y * mappoint.y) / (radius * radius * d1));
        }
        return true;
    }

    private boolean MP_Gall(MapPoint mappoint)
    {
        double d = 45D;
        PJInit(mappoint);
        mappoint.x = radius * new_x * Math.cos(d * MathUtil.deg2rad);
        mappoint.y = (1.0D + Math.cos(d * MathUtil.deg2rad)) * radius * Math.tan(new_y / 2D);
        return true;
    }

    private boolean MP_Goode(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Approximate(39, 8, new_y);
        if(mappoint.y <= 40.729999999999997D && mappoint.y >= -40.729999999999997D)
        {
            mappoint.x = radius * new_x * Math.cos(new_y);
            mappoint.y = radius * new_y;
        } else
        {
            mappoint.y = new_y >= 0.0D ? radius * Math.sqrt(2D) * Math.sin(d) : -radius * Math.sqrt(2D) * Math.sin(d);
            mappoint.x = (radius * 2D * Math.sqrt(2D) * new_x * Math.cos(d)) / Math.PI;
        }
        return true;
    }

    private boolean MP_HammerAitoff(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Math.cos(new_y) * Math.cos(new_x / 2D);
        double d1 = Math.sqrt(1.0D - d * d);
        if(d1 != 0.0D)
        {
            double d2 = (Math.sin(new_x / 2D) * Math.cos(new_y)) / d1;
            double d3 = -Math.sin(new_y) / d1;
            double d4 = (Math.PI/2) - Math.asin(d);
            mappoint.x = 4D * radius * Math.sin(d4 / 2D) * d2;
            mappoint.y = -2D * radius * Math.sin(d4 / 2D) * d3;
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_HammerWagner(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.90629999999999999D;
        double d1 = 0.33329999999999999D;
        double d2 = 1.466D;
        double d3 = Math.asin(d * Math.sin(new_y));
        double d4 = Math.asin(Math.cos(d1 * new_x) * Math.cos(d3));
        double d5 = (Math.PI/2) - d4;
        double d6 = Math.cos(d4) != 0.0D ? (Math.sin(d1 * new_x) * Math.cos(d3)) / Math.cos(d4) : 0.0D;
        double d7 = Math.cos(d4) != 0.0D ? -Math.sin(d3) / Math.cos(d4) : 0.0D;
        mappoint.x = ((2D * radius * d2) / Math.sqrt(d1 * d)) * Math.sin(d5 / 2D) * d6;
        mappoint.y = -((2D * radius) / (d2 * Math.sqrt(d1 * d))) * Math.sin(d5 / 2D) * d7;
        return true;
    }

    private boolean MP_Hatano(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Approximate(151, 10, new_y);
        double d1 = Approximate(150, 10, new_y);
        mappoint.y = new_y >= 0.0D ? radius * 1.7585900000000001D * Math.sin(d) : -radius * 1.93052D * Math.sin(d1);
        mappoint.x = radius * 0.84999999999999998D * new_x * Math.cos(d);
        return true;
    }

    private boolean MP_Kavraisky5(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 1.504875D;
        double d1 = 1.3543879999999999D;
        mappoint.x = (radius * (d1 / d) * new_x * Math.cos(new_y)) / Math.cos(new_y / d1);
        mappoint.y = radius * d * Math.sin(new_y / d1);
        return true;
    }

    private boolean MP_Lagrange1(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 2D;
        if(Math.sin(new_y) == 1.0D || Math.sin(MP_dSp1 * MathUtil.deg2rad) == 1.0D)
        {
            mappoint.x = 0.0D;
            mappoint.y = new_y > 0.0D ? 2D * radius : -2D * radius;
        } else
        {
            double d1 = 1.0D + Math.sin(MP_dSp1 * MathUtil.deg2rad);
            double d2 = 1.0D - Math.sin(MP_dSp1 * MathUtil.deg2rad);
            double d3 = 1.0D + Math.sin(new_y);
            double d4 = 1.0D - Math.sin(new_y);
            double d5 = Math.pow(d1 / d2, 1.0D / (2D * d));
            double d6 = Math.pow(d3 / d4, 1.0D / (2D * d));
            double d7 = d5 * d6;
            double d8 = (d7 + 1.0D / d7) / 2D + Math.cos(new_x / d);
            if(d8 == 0.0D)
                return false;
            mappoint.x = (2D * radius * Math.sin(new_x / d)) / d8;
            mappoint.y = (radius * (d7 - 1.0D / d7)) / d8;
        }
        return true;
    }

    private boolean MP_LambertAzimuthalEqualArea(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.0D;
        double d1 = Math.acos(Math.sin(d) * Math.sin(new_y) + Math.cos(d) * Math.cos(new_y) * Math.cos(new_x));
        double d2 = Math.sqrt(2D / (1.0D + Math.cos(d1)));
        double d3 = Math.cos(d) * Math.sin(new_y) - Math.sin(d) * Math.cos(new_y) * Math.cos(new_x);
        mappoint.x = radius * d2 * Math.cos(new_y) * Math.sin(new_x);
        mappoint.y = radius * d2 * d3;
        return true;
    }

    private boolean MP_LambertConformalConic(MapPoint mappoint)
    {
        double d = MP_dSp1;
        double d1 = MP_dSp2;
        if(MP_dSp1 >= 90D)
            MP_dSp1 = 89D;
        else
        if(MP_dSp1 <= -90D)
            MP_dSp1 = -89D;
        if(MP_dSp2 >= 90D)
            MP_dSp2 = 89D;
        else
        if(MP_dSp2 <= -90D)
            MP_dSp2 = -89D;
        PJInit(mappoint);
        lat = MP_dSp1 * MathUtil.deg2rad;
        double d2 = MP_dSp2 * MathUtil.deg2rad;
        double d3 = ((Math.PI/2) - new_y) / 2D;
        double d4 = ((Math.PI/2) - lat) / 2D;
        double d5 = Math.tan(d3);
        double d6 = Math.tan(d4);
        double d7 = ((Math.PI/2) - d2) / 2D;
        double d8 = Math.tan(d7);
        if(d2 == -lat)
        {
            if(new_y > -1.4137154999999999D && new_y < 1.4137154999999999D)
            {
                mappoint.x = radius * new_x;
                mappoint.y = radius * Math.log(Math.tan((Math.PI/4) + new_y / 2D));
            } else
            {
                MP_dSp1 = d;
                MP_dSp2 = d1;
                return false;
            }
        } else
        if(d2 - lat != 0.0D)
        {
            double d9 = (Math.log(Math.cos(lat)) - Math.log(Math.cos(d2))) / (Math.log(d6) - Math.log(d8));
            if(d9 != 0.0D)
            {
                double d11 = (radius * Math.cos(lat)) / d9;
                double d13 = Math.pow(d6, d9);
                double d15;
                if(d13 != 0.0D)
                    d15 = d11 / d13;
                else
                    return false;
                if(new_y > -1.4137154999999999D && new_y < 1.4137154999999999D)
                {
                    double d17 = d15 * Math.pow(d5, d9);
                    double d19 = d9 * new_x;
                    mappoint.x = d17 * Math.sin(d19);
                    double d21 = (Math.sin(lat) + Math.sin(d2)) / 2D;
                    mappoint.y = radius / Math.tan(d21) - d17 * Math.cos(d19);
                } else
                {
                    MP_dSp1 = d;
                    MP_dSp2 = d1;
                    return false;
                }
            } else
            {
                MP_dSp1 = d;
                MP_dSp2 = d1;
                return false;
            }
        } else
        if(lat != 0.0D)
        {
            double d10;
            if(d6 != 0.0D)
            {
                d10 = d5 / d6;
            } else
            {
                MP_dSp1 = d;
                MP_dSp2 = d1;
                return false;
            }
            if(new_y > -1.4137154999999999D && new_y < 1.4137154999999999D)
            {
                double d12 = Math.cos(lat);
                double d14 = Math.sin(lat);
                double d16 = Math.pow(d10, Math.sin(lat));
                double d18 = ((radius * Math.cos(lat)) / Math.sin(lat)) * Math.pow(d10, Math.sin(lat));
                double d20 = new_x * Math.sin(lat);
                mappoint.x = d18 * Math.sin(d20);
                mappoint.y = radius / Math.tan(lat) - d18 * Math.cos(d20);
            } else
            {
                MP_dSp1 = d;
                MP_dSp2 = d1;
                return false;
            }
        }
        MP_dSp1 = d;
        MP_dSp2 = d1;
        return true;
    }

    private boolean MP_LambertConicEqualArea(MapPoint mappoint)
    {
        PJInit(mappoint);
        lat = MP_dSp1 * MathUtil.deg2rad;
        double d;
        double d1;
        if(lat < 0.0D)
        {
            d = -((Math.PI/4) + new_y / 2D);
            d1 = -((Math.PI/4) + lat / 2D);
        } else
        {
            d = (Math.PI/4) - new_y / 2D;
            d1 = (Math.PI/4) - lat / 2D;
        }
        double d2 = Math.sin(d);
        double d3 = Math.cos(d1);
        if(d3 != 0.0D && lat != 0.0D)
        {
            double d4 = (2D * radius * d2) / d3;
            double d5 = new_x * d3 * d3;
            mappoint.x = d4 * Math.sin(d5);
            mappoint.y = -d4 * Math.cos(d5);
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_LambertCylindricalEqualArea(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.x = radius * new_x * Math.cos(MP_dSp1 * MathUtil.deg2rad);
        mappoint.y = (radius * Math.sin(new_y)) / Math.cos(MP_dSp1 * MathUtil.deg2rad);
        return true;
    }

    private boolean MP_Larrivee(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.x = 0.5D * radius * new_x * (1.0D + Math.sqrt(Math.cos(new_y)));
        mappoint.y = (radius * new_y) / (Math.cos(new_x / 6D) * Math.cos(new_y / 2D));
        return true;
    }

    private boolean MP_Laskowski(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = new_x * new_x;
        double d1 = new_y * new_y;
        double d2 = 0.97553400000000001D;
        double d3 = -0.119161D;
        double d4 = -0.0143059D;
        double d5 = -0.054700899999999997D;
        double d6 = 1.0038400000000001D;
        double d7 = 0.080289399999999997D;
        double d8 = 0.099890900000000005D;
        double d9 = 0.000199025D;
        double d10 = -0.028549999999999999D;
        double d11 = -0.0491032D;
        mappoint.x = radius * new_x * (d2 + d1 * (d3 + d * d4 + d1 * d5));
        mappoint.y = radius * new_y * (d6 + d * (d7 + d1 * d10 + d * d9) + d1 * (d8 + d1 * d11));
        return true;
    }

    private boolean MP_Littrow(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_y > 1.0471966666666666D || new_y < -1.0471966666666666D)
            return false;
        if(new_x > (Math.PI/2) || new_x < -(Math.PI/2))
            return false;
        if(Math.cos(new_y) == 0.0D)
        {
            return false;
        } else
        {
            mappoint.x = (radius * Math.sin(new_x)) / Math.cos(new_y);
            mappoint.y = radius * Math.tan(new_y) * Math.cos(new_x);
            return true;
        }
    }

    private boolean MP_Loximuthal(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.y = radius * (new_y - MP_dSp1 * MathUtil.deg2rad);
        if(new_y == MP_dSp1 * MathUtil.deg2rad)
        {
            mappoint.x = radius * new_x * Math.cos(MP_dSp1 * MathUtil.deg2rad);
        } else
        {
            double d = Math.tan((Math.PI/4) + new_y / 2D);
            double d1 = Math.tan((Math.PI/4) + (MP_dSp1 * MathUtil.deg2rad) / 2D);
            if(d1 == 0.0D)
                return false;
            double d2 = Math.log(d / d1);
            if(d2 == 0.0D)
                return false;
            mappoint.x = (radius * new_x * (new_y - MP_dSp1 * MathUtil.deg2rad)) / d2;
        }
        return true;
    }

    private boolean MP_McbrydeThomas1(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.91659999999999997D;
        double d1 = 0.5D;
        double d2 = Approximate(66, 8, new_y);
        mappoint.x = (radius * d * new_x * (d1 + Math.cos(d2))) / (d1 + 1.0D);
        mappoint.y = new_y >= 0.0D ? radius * d * d2 : -radius * d * d2;
        return true;
    }

    private boolean MP_Mercator(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_y > -1.483528611111111D && new_y < 1.483528611111111D)
        {
            mappoint.x = radius * new_x;
            mappoint.y = radius * Math.log(Math.tan((Math.PI/4) + new_y / 2D));
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_Miller1(MapPoint mappoint)
    {
        double d = 1.25D;
        PJInit(mappoint);
        mappoint.x = radius * new_x;
        mappoint.y = radius * d * Math.log(Math.tan((Math.PI/4) + new_y / (2D * d)));
        return true;
    }

    private boolean MP_Miller2(MapPoint mappoint)
    {
        double d = 1.5D;
        PJInit(mappoint);
        mappoint.x = radius * new_x;
        mappoint.y = radius * d * Math.log(Math.tan((Math.PI/4) + new_y / (2D * d)));
        return true;
    }

    private boolean MP_Mollweide(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Approximate(39, 8, new_y);
        mappoint.y = new_y >= 0.0D ? radius * Math.sqrt(2D) * Math.sin(d) : -radius * Math.sqrt(2D) * Math.sin(d);
        mappoint.x = (radius * 2D * Math.sqrt(2D) * new_x * Math.cos(d)) / Math.PI;
        return true;
    }

    private boolean MP_NellHammer(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.x = (radius * new_x * (1.0D + Math.cos(new_y))) / 2D;
        mappoint.y = 2D * radius * (new_y - Math.tan(new_y / 2D));
        return true;
    }

    private boolean MP_Nicolosi(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_x > (Math.PI/2) || new_x < -(Math.PI/2))
            return false;
        if(new_x == 0.0D)
        {
            mappoint.x = 0.0D;
            mappoint.y = radius * new_y;
        } else
        if(new_y == 0.0D)
        {
            mappoint.x = radius * new_x;
            mappoint.y = 0.0D;
        } else
        if(new_x == (Math.PI/2) || new_x == -(Math.PI/2))
        {
            mappoint.x = radius * new_x * Math.cos(new_y);
            mappoint.y = radius * (Math.PI/2) * Math.sin(new_y);
        } else
        if(new_y == (Math.PI/2) || new_y == -(Math.PI/2))
        {
            mappoint.x = 0.0D;
            mappoint.y = radius * new_y;
        } else
        {
            double d = Math.PI / (2D * new_x) - (2D * new_x) / Math.PI;
            double d1 = (2D * new_y) / Math.PI;
            if(d1 == Math.sin(new_y))
                return false;
            double d2 = (1.0D - d1 * d1) / (Math.sin(new_y) - d1);
            if(d == 0.0D || d2 == 0.0D)
                return false;
            double d3 = d * d;
            double d4 = d2 * d2;
            double d5 = ((d * Math.sin(new_y)) / d2 - d / 2D) / (1.0D + d3 / d4);
            double d6 = ((d4 * Math.sin(new_y)) / d3 + d2 / 2D) / (1.0D + d4 / d3);
            double d7 = Math.sqrt(d5 * d5 + (Math.cos(new_y) * Math.cos(new_y)) / (1.0D + d3 / d4));
            double d8 = Math.sqrt(d6 * d6 - (((d4 * Math.sin(new_y) * Math.sin(new_y)) / d3 + d2 * Math.sin(new_y)) - 1.0D) / (1.0D + d4 / d3));
            mappoint.x = new_x > 0.0D ? ((radius * Math.PI) / 2D) * (d5 + d7) : ((radius * Math.PI) / 2D) * (d5 - d7);
            mappoint.y = new_y > 0.0D ? ((radius * Math.PI) / 2D) * (d6 - d8) : ((radius * Math.PI) / 2D) * (d6 + d8);
        }
        return true;
    }

    private boolean MP_Ortelius(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.y = radius * new_y;
        double d = (radius * Math.PI) / 2D;
        double d1 = new_x != 0.0D ? (d * (new_x * new_x - 2.4673969320249998D)) / (Math.PI * new_x) : 0.0D;
        double d2 = (d1 * d1 - mappoint.y * mappoint.y) + d * d;
        double d3 = d2 >= 0.0D ? Math.sqrt(d2) : 0.0D;
        double d4 = 9.8695877280999991D - (mappoint.y * mappoint.y * Math.PI * Math.PI) / (d * d);
        double d5 = d4 >= 0.0D ? Math.sqrt(d4) : 0.0D;
        if(new_x == 0.0D)
            mappoint.x = 0.0D;
        else
        if(new_x >= -(Math.PI/2) && new_x < 0.0D)
            mappoint.x = d1 - d3;
        else
        if(new_x >= 0.0D && new_x <= (Math.PI/2))
            mappoint.x = d1 + d3;
        else
        if(new_x < -(Math.PI/2))
            mappoint.x = (d / Math.PI) * ((2D * new_x + Math.PI) - d5);
        else
        if(new_x > (Math.PI/2))
            mappoint.x = (d / Math.PI) * ((2D * new_x - Math.PI) + d5);
        return true;
    }

    private boolean MP_Pavlov(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = -0.15310000000000001D;
        double d1 = -0.026700000000000002D;
        double d2 = (d / 3D) * Math.pow(new_y, 3D);
        double d3 = (d1 / 5D) * Math.pow(new_y, 5D);
        mappoint.x = radius * new_x;
        mappoint.y = radius * (new_y + d2 + d3);
        return true;
    }

    double GetPeirceDist()
    {
        MapPoint mappoint = new MapPoint();
        MapPoint mappoint1 = new MapPoint();
        double d = (Math.cos(0.0D) * (Math.sin(-(Math.PI/2)) + Math.cos(-(Math.PI/2)))) / Math.sqrt(2D);
        double d1 = (Math.cos(0.0D) * (Math.sin(-(Math.PI/2)) - Math.cos(-(Math.PI/2)))) / Math.sqrt(2D);
        double d2 = Math.sqrt(1.0D - d * d);
        double d3 = Math.sqrt(1.0D - d1 * d1);
        double d4 = (1.0D + d * d1) - d2 * d3;
        double d5 = 1.0D - d * d1 - d2 * d3;
        double d6 = Math.sin(-(Math.PI/2)) >= 0.0D ? Math.asin(Math.sqrt(d4)) : Math.asin(-Math.sqrt(d4));
        double d7 = Math.cos(-(Math.PI/2)) <= 0.0D ? Math.asin(Math.sqrt(d5)) : Math.asin(-Math.sqrt(d5));
        mappoint.x = Math.sin(-(Math.PI/2)) == 0.0D ? 0.0D : radius * (1.0D / Math.sqrt(1.0D - 0.5D * d4)) * d6;
        d = (Math.cos(0.0D) * (Math.sin((Math.PI/2)) + Math.cos((Math.PI/2)))) / Math.sqrt(2D);
        d1 = (Math.cos(0.0D) * (Math.sin((Math.PI/2)) - Math.cos((Math.PI/2)))) / Math.sqrt(2D);
        d2 = Math.sqrt(1.0D - d * d);
        d3 = Math.sqrt(1.0D - d1 * d1);
        d4 = (1.0D + d * d1) - d2 * d3;
        d5 = 1.0D - d * d1 - d2 * d3;
        d6 = Math.sin((Math.PI/2)) >= 0.0D ? Math.asin(Math.sqrt(d4)) : Math.asin(-Math.sqrt(d4));
        d7 = Math.cos((Math.PI/2)) <= 0.0D ? Math.asin(Math.sqrt(d5)) : Math.asin(-Math.sqrt(d5));
        mappoint1.x = Math.sin((Math.PI/2)) == 0.0D ? 0.0D : radius * (1.0D / Math.sqrt(1.0D - 0.5D * d4)) * d6;
        return Math.abs(mappoint1.x - mappoint.x);
    }

    private boolean MP_Peirce(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_y >= 0.0D)
        {
            double d = (Math.cos(new_y) * (Math.sin(new_x) + Math.cos(new_x))) / Math.sqrt(2D);
            double d2 = (Math.cos(new_y) * (Math.sin(new_x) - Math.cos(new_x))) / Math.sqrt(2D);
            double d4 = Math.sqrt(1.0D - d * d);
            double d6 = Math.sqrt(1.0D - d2 * d2);
            double d8 = (1.0D + d * d2) - d4 * d6;
            double d10 = 1.0D - d * d2 - d4 * d6;
            double d12 = Math.sin(new_x) >= 0.0D ? Math.asin(Math.sqrt(d8)) : Math.asin(-Math.sqrt(d8));
            double d14 = Math.cos(new_x) <= 0.0D ? Math.asin(Math.sqrt(d10)) : Math.asin(-Math.sqrt(d10));
            mappoint.x = Math.sin(new_x) == 0.0D ? 0.0D : radius * (1.0D / Math.sqrt(1.0D - 0.5D * d8)) * d12;
            mappoint.y = radius * (1.0D / Math.sqrt(1.0D - 0.5D * d10)) * d14;
        } else
        {
            double d1 = (Math.cos(new_y) * (Math.sin(new_x) + Math.cos(new_x))) / Math.sqrt(2D);
            double d3 = (Math.cos(new_y) * (Math.sin(new_x) - Math.cos(new_x))) / Math.sqrt(2D);
            double d5 = Math.sqrt(1.0D - d1 * d1);
            double d7 = Math.sqrt(1.0D - d3 * d3);
            double d9 = (1.0D + d1 * d3) - d5 * d7;
            double d11 = 1.0D - d1 * d3 - d5 * d7;
            double d13 = Math.sin(new_x) >= 0.0D ? Math.asin(Math.sqrt(d9)) : Math.asin(-Math.sqrt(d9));
            double d15 = Math.cos(new_x) <= 0.0D ? Math.asin(Math.sqrt(d11)) : Math.asin(-Math.sqrt(d11));
            double d16 = GetPeirceDist();
            if(Math.sin(new_x) == 0.0D)
            {
                mappoint.x = 0.0D;
            } else
            {
                if(Math.sin(new_x + (Math.PI/4)) * Math.cos(new_x + (Math.PI/4)) >= 0.0D)
                    mappoint.x = radius * (1.0D / Math.sqrt(1.0D - 0.5D * d9)) * d13;
                else
                if(Math.sin(new_x + (Math.PI/4)) >= 0.0D)
                    mappoint.x = d16 - radius * (1.0D / Math.sqrt(1.0D - 0.5D * d9)) * d13;
                else
                    mappoint.x = -d16 - radius * (1.0D / Math.sqrt(1.0D - 0.5D * d9)) * d13;
                if(Math.sin(new_x + (Math.PI/4)) * Math.cos(new_x + (Math.PI/4)) < 0.0D)
                    mappoint.y = radius * (1.0D / Math.sqrt(1.0D - 0.5D * d11)) * d15;
                else
                if(Math.sin(new_x + (Math.PI/4)) >= 0.0D)
                    mappoint.y = -d16 - radius * (1.0D / Math.sqrt(1.0D - 0.5D * d11)) * d15;
                else
                    mappoint.y = d16 - radius * (1.0D / Math.sqrt(1.0D - 0.5D * d11)) * d15;
            }
        }
        return true;
    }

    private boolean MP_AzimuthalPerspective(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_x >= -(Math.PI/2) && new_x <= (Math.PI/2) && new_y >= -(Math.PI/2) && new_y <= (Math.PI/2))
        {
            double d = 0.0D;
            double d1 = Math.acos(Math.sin(d) * Math.sin(new_y) + Math.cos(d) * Math.cos(new_y) * Math.cos(new_x));
            double d2 = (MP_dDistance + 1.0D) / (MP_dDistance + Math.cos(d1));
            double d3 = Math.cos(d) * Math.sin(new_y) - Math.sin(d) * Math.cos(new_y) * Math.cos(new_x);
            mappoint.x = radius * d2 * Math.cos(new_y) * Math.sin(new_x);
            mappoint.y = radius * d2 * d3;
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_Peters(MapPoint mappoint)
    {
        double d = 45D;
        PJInit(mappoint);
        mappoint.x = radius * new_x * Math.cos(d * MathUtil.deg2rad);
        mappoint.y = (radius * Math.sin(new_y)) / Math.cos(d * MathUtil.deg2rad);
        return true;
    }

    private boolean MP_PlateCarree(MapPoint mappoint)
    {
        double d = 0.0D;
        PJInit(mappoint);
        mappoint.y = radius * new_y;
        mappoint.x = radius * new_x * Math.cos(d * MathUtil.deg2rad);
        return true;
    }

    private boolean MP_Polyconic(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(new_y != 0.0D)
        {
            double d = radius * (Math.cos(new_y) / Math.sin(new_y));
            double d1 = 2D * Math.atan(0.5D * new_x * Math.sin(new_y));
            mappoint.x = d * Math.sin(d1);
            mappoint.y = radius * new_y + 2D * d * Math.sin(d1 / 2D) * Math.sin(d1 / 2D);
        } else
        {
            return false;
        }
        return true;
    }

    private boolean MP_Quartic(MapPoint mappoint)
    {
        PJInit(mappoint);
        if(Math.cos(new_y / 2D) == 0.0D)
        {
            return false;
        } else
        {
            mappoint.y = 2D * radius * Math.sin(new_y / 2D);
            mappoint.x = (radius * new_x * Math.cos(new_y)) / Math.cos(new_y / 2D);
            return true;
        }
    }

    private boolean MP_Robinson(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.85070000000000001D;
        double d1 = 0.96419999999999995D;
        double d2 = -0.14499999999999999D;
        double d3 = -0.0012999999999999999D;
        double d4 = -0.0104D;
        double d5 = -0.0129D;
        mappoint.x = radius * new_x * (d + d2 * new_y * new_y + d4 * Math.pow(new_y, 4D));
        mappoint.y = radius * (d1 * new_y + d3 * Math.pow(new_y, 3D) + d5 * Math.pow(new_y, 5D));
        return true;
    }

    private boolean MP_Sanson(MapPoint mappoint)
    {
        PJInit(mappoint);
        mappoint.x = radius * (new_x * Math.cos(new_y));
        mappoint.y = radius * new_y;
        return true;
    }

    private boolean MP_Urmaev3(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.92810000000000004D;
        double d1 = 1.1143000000000001D;
        double d2 = (d1 / 3D) * Math.pow(new_y, 3D);
        mappoint.x = radius * new_x;
        mappoint.y = radius * (d * new_y + d2);
        return true;
    }

    private boolean MP_VanderGrinten1(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = new_y > 0.0D ? (2D * new_y) / Math.PI : (-2D * new_y) / Math.PI;
        double d1 = Math.sqrt(1.0D - d * d);
        if(new_y == 0.0D)
        {
            mappoint.x = radius * new_x;
            mappoint.y = 0.0D;
        } else
        if(new_x == 0.0D)
        {
            mappoint.x = 0.0D;
            mappoint.y = new_y > 0.0D ? (radius * Math.PI * d) / (1.0D + d1) : (-radius * Math.PI * d) / (1.0D + d1);
        } else
        if(new_y == -(Math.PI/2) || new_y == (Math.PI/2))
        {
            mappoint.x = 0.0D;
            mappoint.y = new_y > 0.0D ? radius * Math.PI : -radius * Math.PI;
        } else
        {
            if(d == 0.0D)
                return false;
            double d2 = Math.PI / new_x - new_x / Math.PI;
            double d3 = d2 > 0.0D ? d2 / 2D : -d2 / 2D;
            if((d + d1) - 1.0D == 0.0D)
                return false;
            double d4 = d1 / ((d + d1) - 1.0D);
            double d5 = d4 * (2D / d - 1.0D);
            double d6 = d3 * d3 + d4;
            double d7 = d5 * d5 + d3 * d3;
            double d8 = d4 - d5 * d5;
            double d9 = Math.sqrt(d3 * d3 * d8 * d8 - d7 * (d4 * d4 - d5 * d5));
            double d10 = d3 * Math.sqrt((d3 * d3 + 1.0D) * d7 - d6 * d6);
            if(d7 == 0.0D)
                return false;
            mappoint.x = new_x > 0.0D ? (radius * Math.PI * (d3 * d8 + d9)) / d7 : (-radius * Math.PI * (d3 * d8 + d9)) / d7;
            mappoint.y = new_y > 0.0D ? (radius * Math.PI * (d5 * d6 - d10)) / d7 : (-radius * Math.PI * (d5 * d6 - d10)) / d7;
        }
        return true;
    }

    private boolean MP_Wagner4(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.5D;
        double d1 = 0.5D;
        double d2 = (2D * Math.acos(d) + Math.sin(2D * Math.acos(d))) / Math.PI;
        double d3 = Math.sqrt(1.0D - d * d) / (2D * d1);
        double d4 = Approximate(78, 8, new_y);
        mappoint.x = (radius * ((d3 * new_x) / Math.PI) * 2D * Math.sqrt(2D) * Math.cos(d4)) / Math.sqrt(d3 * d2);
        mappoint.y = new_y >= 0.0D ? (radius * Math.sqrt(2D) * Math.sin(d4)) / Math.sqrt(d3 * d2) : (-radius * Math.sqrt(2D) * Math.sin(d4)) / Math.sqrt(d3 * d2);
        return true;
    }

    private boolean MP_Werenskiold1(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = Math.asin(0.625D * Math.sqrt(2D) * Math.sin(new_y));
        mappoint.x = (radius * new_x * Math.cos(d)) / Math.cos(d / 3D);
        mappoint.y = Math.PI * Math.sqrt(2D) * radius * Math.sin(d / 3D);
        return true;
    }

    private boolean MP_Winkel1(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.63660000000000005D;
        mappoint.x = 0.5D * radius * new_x * (d + Math.cos(new_y));
        mappoint.y = radius * new_y;
        return true;
    }

    private boolean MP_Winkel2(MapPoint mappoint)
    {
        PJInit(mappoint);
        double d = 0.63660000000000005D;
        mappoint.x = (4D * new_y * new_y) / 9.8695877280999991D <= 1.0D ? 0.5D * radius * new_x * (d + Math.sqrt(1.0D - (4D * new_y * new_y) / 9.8695877280999991D)) : 0.5D * radius * new_x * d;
        mappoint.y = radius * new_y;
        return true;
    }

    private Image offimg;
    private Dimension offsize;
    private Graphics offg;
//    private PjMapProjection m_p;
    public static final int ID_Adams = 1100;
    public static final int ID_Airy = 1150;
    public static final int ID_Aitoff = 1200;
    public static final int ID_Albers = 1250;
    public static final int ID_Apianus = 1300;
    public static final int ID_Apianus2 = 1350;
    public static final int ID_Armadillo = 1400;
    public static final int ID_August = 1450;
    public static final int ID_AzimuthalCentral = 1500;
    public static final int ID_AzimuthalEquidistant = 1550;
    public static final int ID_AzimuthalOrthographic = 1600;
    public static final int ID_AzimuthalStereographic = 1650;
    public static final int ID_AzimuthalPerspective = 1675;
    public static final int ID_Bacon = 1700;
    public static final int ID_Behrmann = 1750;
    public static final int ID_Boggs = 1800;
    public static final int ID_Bonne = 1850;
    public static final int ID_Bolshoi = 1900;
    public static final int ID_Braun = 1950;
    public static final int ID_Briesemeister = 2000;
    public static final int ID_Collignon = 2050;
    public static final int ID_ConicEquidistant = 2100;
    public static final int ID_Craig = 2150;
    public static final int ID_Craster = 2200;
    public static final int ID_CylindricalCentral = 2250;
    public static final int ID_CylindricalEquidistant = 2300;
    public static final int ID_CylindricalOrthographic = 2350;
    public static final int ID_CylindricalPerspective = 2400;
    public static final int ID_Denoyer = 2450;
    public static final int ID_Eckert1 = 2500;
    public static final int ID_Eckert2 = 2550;
    public static final int ID_Eckert3 = 2600;
    public static final int ID_Eckert4 = 2650;
    public static final int ID_Eckert5 = 2700;
    public static final int ID_Eckert6 = 2750;
    public static final int ID_Fahey = 2800;
    public static final int ID_Fournier = 2850;
    public static final int ID_Gall = 2900;
    public static final int ID_Goodes = 2950;
    public static final int ID_HammerAitoff = 3000;
    public static final int ID_HammerWagner = 3050;
    public static final int ID_Hatano = 3100;
    public static final int ID_Kavraisky5 = 3150;
    public static final int ID_Lagrange1 = 3200;
    public static final int ID_LambertAzimuthalEqualArea = 3250;
    public static final int ID_LambertConformalConic = 3300;
    public static final int ID_LambertConicEqualArea = 3350;
    public static final int ID_LambertCylindricalEqualArea = 3400;
    public static final int ID_Larrivee = 3450;
    public static final int ID_Laskowski = 3500;
    public static final int ID_Littrow = 3550;
    public static final int ID_Loximuthal = 3600;
    public static final int ID_McbrydeThomas1 = 3650;
    public static final int ID_Mercator = 3700;
    public static final int ID_Miller1 = 3750;
    public static final int ID_Miller2 = 3800;
    public static final int ID_Mollweide = 3850;
    public static final int ID_NellHammer = 3900;
    public static final int ID_Nicolosi = 4000;
    public static final int ID_Ortelius = 4050;
    public static final int ID_Pavlov = 4100;
    public static final int ID_Peirce = 4150;
    public static final int ID_Peters = 4200;
    public static final int ID_PlateCarree = 4250;
    public static final int ID_Polyconic = 4300;
    public static final int ID_Quartic = 4350;
    public static final int ID_Robinson = 4400;
    public static final int ID_Sanson = 4450;
    public static final int ID_Urmaev3 = 4500;
    public static final int ID_VanderGrinten1 = 4550;
    public static final int ID_Wagner4 = 4600;
    public static final int ID_Werenskiold1 = 4650;
    public static final int ID_Winkel1 = 4700;
    public static final int ID_Winkel2 = 4750;
    protected int MP_nPJID;
    private boolean MP_bCoast;
    private boolean MP_bGraticule;
    private boolean MP_bOutline;
    private boolean MP_bIndicatrix;
    private boolean MP_bCircle;
    private boolean MP_bAzimuthal;
    private boolean MP_bGCircle;
    private int MP_nLonDeg;
    private int MP_nLonMin;
    private int MP_nLatDeg;
    private int MP_nLatMin;
    private double MP_dLonCenter;
    private double MP_dLatCenter;
    private int MP_nEqdLonDeg;
    private int MP_nEqdLonMin;
    private int MP_nEqdLatDeg;
    private int MP_nEqdLatMin;
    private int MP_nAzmLonDeg;
    private int MP_nAzmLonMin;
    private int MP_nAzmLatDeg;
    private int MP_nAzmLatMin;
    private int MP_nSrcLonDeg;
    private int MP_nSrcLonMin;
    private int MP_nSrcLatDeg;
    private int MP_nSrcLatMin;
    private int MP_nDstLonDeg;
    private int MP_nDstLonMin;
    private int MP_nDstLatDeg;
    private int MP_nDstLatMin;
    private double MP_dScale;
    private double MP_dScaleInitial;
    private double MP_dScaleCorrection;
    private int MP_nCenterX;
    private int MP_nCenterY;
    private int MP_nCenterXInitial;
    private int MP_nCenterYInitial;
    private int MP_nInterval;
    private double MP_dDistance;
    private boolean MP_bOblique;
    private double MP_dObliqY;
    private int MP_nSp1Deg;
    private int MP_nSp1Min;
    private int MP_nSp2Deg;
    private int MP_nSp2Min;
    private double MP_dSp1;
    private double MP_dSp2;
    private double m_Tx;
    private double m_Ty;
    private double m_Tdist;
    private double m_Tphi;
    private double fpx[];
    private double fpy[];
    private double dExpandrate;
    private MapPoint newp;
    private double radius;
    private double lat;
    private double new_x;
    private double new_y;
    private int oldX;
    private int oldY;
    private boolean bPress;
}
