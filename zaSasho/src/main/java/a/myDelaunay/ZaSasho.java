package a.myDelaunay;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.slavi.math.ColorSetPick;

public class ZaSasho extends JApplet {

	MyDelaunayPanel delaunayPanel;
	JCheckBox cbTriangles;
	JCheckBox cbPolygons;
	
	class MyDelaunayPanel extends BasePointsListPanel {
		public MyDelaunayPanel() {
			drawPointIndex = true;
			
			points.add(new Point2D.Double(40.0, 80.0));
			points.add(new Point2D.Double(40.0, 160.0));
			points.add(new Point2D.Double(120.0, 80.0));
			points.add(new Point2D.Double(120.0, 160.0));
		}
	
		public void dumpPoints() {
			System.out.println("=== Dump points ===");
			for (Point2D p : points) {
				System.out.println("points.add(new Point2D.Double(" + p.getX() + ", " + p.getY() + "));");
			}
			System.out.println("======");
		}
		
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.lightGray);
			g2.fill(g.getClipBounds());
			MyDelaunay d = new MyDelaunay() {
				public int getPointId(Point2D p) {
					return points.indexOf(p);
				}
			};
			ColorSetPick colorPick = new ColorSetPick();
			try {
				for (int i = 0; i < points.size(); i++) {
					Point2D p = points.get(i);
					d.insertPoint(p);
				}
				if (cbPolygons.isSelected()) {
					Rectangle2D extent = new Rectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1);
					ArrayList<Path2D> voronoi = MyVoronoi.computeVoroni(d, extent);
					for (int i = 0; i < voronoi.size(); i++) {
						if (i != 1)
							; //continue;
						Path2D path = voronoi.get(i);
						g2.setColor(colorPick.getNextColor(80));
						//g2.setColor(new Color(0x20ff0000, true));
						g2.fill(path);
						g2.setColor(Color.blue);
						g2.draw(path);
					}
				}
			} catch (Throwable t) {
				dumpPoints();
				t.printStackTrace();
			}
			// draw
			if (cbTriangles.isSelected()) {
				ArrayList<Triangle> triangles = new ArrayList<Triangle>(d.getTriangles());
				for (Triangle t : triangles) {
					Utils.drawTriangle(g, t);
				}
				for (int i = 0; i < triangles.size(); i++) {
					Triangle t = triangles.get(i);
					Utils.drawTriangleCenter(g, t, Integer.toString(i));
				}
			}
			super.paint(g);
		}
	}
	
	public void init() {
		GridBagConstraints c = new GridBagConstraints();
		JPanel rootPanel = new JPanel();
		rootPanel.setDoubleBuffered(true);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = c.weighty = 1.0;
		this.setLayout(new GridBagLayout());
		this.add(rootPanel, c);
		rootPanel.setLayout(new GridBagLayout());

		delaunayPanel = new MyDelaunayPanel();
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 1;
		rootPanel.add(delaunayPanel, c);
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		c.fill = GridBagConstraints.NONE;
		c.gridx++;
		JButton btn = new JButton("dump");
		panel.add(btn, c);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delaunayPanel.dumpPoints();
			}
		});
		
		ActionListener repaintListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		};
		cbTriangles = new JCheckBox("triangles");
		cbTriangles.addActionListener(repaintListener);
		cbTriangles.setSelected(true);
		c.fill = GridBagConstraints.NONE;
		c.gridx++;
		panel.add(cbTriangles, c);
		rootPanel.add(panel);
		
		cbPolygons = new JCheckBox("polygons");
		cbPolygons.addActionListener(repaintListener);
		cbPolygons.setSelected(true);
		c.fill = GridBagConstraints.NONE;
		c.gridx++;
		panel.add(cbPolygons, c);
		rootPanel.add(panel);

		setSize(400, 400);
	}
}
