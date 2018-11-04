package a.myDelaunay;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class ZaSasho extends JApplet {

	public static class MyPointWithWeight extends Point2D.Double implements DataWithWeight {
		double weight = 1.0;
		
		public MyPointWithWeight() {
		}
		
		public MyPointWithWeight(double x, double y) {
			super(x, y);
		}
		
		public MyPointWithWeight(double x, double y, double weight) {
			super(x, y);
			this.weight = weight;
		}
		
		public double getWeight() {
			return weight;
		}
		
		public void setWeight(double weight) {
			this.weight = weight;
		}
	}
	
	MyDelaunayPanel delaunayPanel;
	JCheckBox cbTriangles;
	JCheckBox cbPolygons;
	
	class MyDelaunayPanel extends JComponent {
		protected boolean drawPointIndex = false;
		
		protected ArrayList<MyPointWithWeight> points = new ArrayList<MyPointWithWeight>();
		
		protected int selectedIndex = -1;
		
		private boolean mouseButton1Down = false;

		void fixPoint(Point2D p) {
			double x = p.getX();
			double y = p.getY();
			if (x < 0) 
				x = 0;
			if (y < 0)
				y = 0;
			if (x >= getWidth())
				x = getWidth();
			if (y >= getHeight())
				y = getHeight();
			p.setLocation(x, y);
		}
		
		public MyDelaunayPanel() {
			MouseAdapter listener = new MouseAdapter() {
				public void mouseDragged(MouseEvent e) {
					int x = e.getX();
					int y = e.getY();
					if ((selectedIndex >= 0) && mouseButton1Down) {
						Point2D p = points.get(selectedIndex);
						p.setLocation(x, y);
						fixPoint(p);
						repaint();
					}
				}

				public void mouseReleased(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						mouseButton1Down = false;
					}
					repaint();
				}
				
				public void mousePressed(MouseEvent e) {
					int x = e.getX();
					int y = e.getY();

					selectedIndex = -1;
					for (int i = 0; i < points.size(); i++) {
						Point2D p = points.get(i);
						if ((p.getX() - Utils.controlNodeWidth <= x) && 
							(x <= p.getX() + Utils.controlNodeWidth) && 
							(p.getY() - Utils.controlNodeHeight <= y) && 
							(y <= p.getY() + Utils.controlNodeHeight)) {
							selectedIndex = i;
							break;
						}
					}

					if (e.getButton() == MouseEvent.BUTTON1) {
						mouseButton1Down = true;
						
						if (selectedIndex >= 0) {
							if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
								// delete point
								points.remove(selectedIndex);
								selectedIndex = -1;
							} else {
								MyPointWithWeight p = points.get(selectedIndex);
								p.setLocation(x, y);
								fixPoint(p);
							}
							repaint();
						} else {
							MyPointWithWeight p = new MyPointWithWeight(x, y);
							points.add(p);
							selectedIndex = points.size() - 1;
							repaint();
						}
					}
				}
			};

			setRequestFocusEnabled(true);
			
			addMouseListener(listener);
			addMouseMotionListener(listener);
			AbstractAction keyListener = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					char c = e.getActionCommand().charAt(0);
					if ('1' <= c || c <= '9') {
						if (selectedIndex >= 0) {
							MyPointWithWeight p = (MyPointWithWeight) points.get(selectedIndex);
							p.setWeight(c - '0');
							repaint();
						}
					}
				}
			};
			
			for (Character c = '1'; c <= '9'; c++) {
				getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(c), c);
				getActionMap().put(c, keyListener);
			}
			drawPointIndex = true;
			
			points.add(new MyPointWithWeight(40.0, 80.0));
			points.add(new MyPointWithWeight(40.0, 160.0));
			points.add(new MyPointWithWeight(120.0, 80.0));
			points.add(new MyPointWithWeight(120.0, 160.0));
		}
	
		public void dumpPoints() {
			System.out.println("=== Dump points ===");
			for (MyPointWithWeight p : points) {
				System.out.println("points.add(new MyPointWithWeight(" + p.getX() + ", " + p.getY() + ", " + p.getWeight() + "));");
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
			for (int i = 0; i < points.size(); i++) {
				MyPointWithWeight p = (MyPointWithWeight) points.get(i);
				Utils.drawPoint(g, (int) p.getX(), (int) p.getY(), 
					i == selectedIndex ? Color.yellow : Color.black, 
					drawPointIndex ? Integer.toString(i) + "(" + Integer.toString((int) p.getWeight()) + ")" : null);
			}
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
				// delaunayPanel.dumpPoints();
				MyVoronoi.dummy = !MyVoronoi.dummy;
				repaint();
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
