package noCVS;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BezierApplet.java

import java.applet.Applet;
import java.awt.*;
import java.awt.geom.Point2D;

class BezierCanvas extends Canvas
{

    BezierCanvas(Applet applet, Point2D.Double apoint[])
    {
        active = -1;
        parent = applet;
        path = new Curve(apoint[0], apoint[1], apoint[2], apoint[3]);
        node = new ControlNode[4];
        index = new int[4];
        for(int i = 0; i < 4; i++)
            index[i] = i;

        Dimension dimension = parent.size();
        wd = dimension.width;
        ht = dimension.height;
        for(int j = 0; j < 4; j++)
            node[j] = new ControlNode(apoint[j]);

        gfx = parent.getGraphics();
        gfx.setColor(Color.white);
        gfx.fillRect(0, 0, wd, ht);
        gfx.setColor(Color.lightGray);
        gfx.drawLine((int)apoint[0].x, (int)apoint[0].y, (int)apoint[1].x, (int)apoint[1].y);
        gfx.drawLine((int)apoint[1].x, (int)apoint[1].y, (int)apoint[2].x, (int)apoint[2].y);
        gfx.drawLine((int)apoint[2].x, (int)apoint[2].y, (int)apoint[3].x, (int)apoint[3].y);
        gfx.setColor(Color.red);
        for(int k = 0; k < 4; k++)
            node[k].draw(gfx);

        parent.repaint();
    }

    public void mouseDown(int i, int j)
    {
        for(int l = 4; l > 0; l--)
        {
            int k = index[l - 1];
            if(!node[k].within(i, j))
                continue;
            node[k].centre.x = i;
            node[k].centre.y = j;
            active = k;
            break;
        }

        if(active >= 0)
        {
            int i1;
            for(i1 = 0; index[i1] != active; i1++);
            for(; i1 < 3; i1++)
                index[i1] = index[i1 + 1];

            index[3] = active;
            update();
            parent.repaint();
        }
    }

    public void mouseDrag(int i, int j)
    {
        if(active >= 0 && i >= 0 && i < wd && j >= 0 && j < ht)
        {
            node[active].centre.x = i;
            node[active].centre.y = j;
            path.reset(active, i, j);
            parent.repaint();
        }
    }

    public void mouseUp(int i, int j)
    {
        if(active >= 0)
            active = -1;
        parent.repaint();
    }

    public void update()
    {
        gfx.setColor(Color.white);
        gfx.fillRect(0, 0, wd, ht);
        gfx.setColor(Color.lightGray);
        gfx.drawLine((int)node[0].centre.x, (int)node[0].centre.y, (int)node[1].centre.x, (int)node[1].centre.y);
        gfx.drawLine((int)node[1].centre.x, (int)node[1].centre.y, (int)node[2].centre.x, (int)node[2].centre.y);
        gfx.drawLine((int)node[2].centre.x, (int)node[2].centre.y, (int)node[3].centre.x, (int)node[3].centre.y);
        gfx.setColor(Color.red);
        path.draw(gfx, Color.black);
        for(int i = 0; i < 4; i++)
            node[index[i]].draw(gfx);

    }

    public void paint(Graphics g)
    {
        update();
    }

    Curve path;
    ControlNode node[];
    Graphics gfx;
    Applet parent;
    int wd;
    int ht;
    int active;
    int index[];
}
