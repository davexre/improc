package org.swtchart.ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxis.Direction;
import org.swtchart.Range;

/**
 * An interactive chart which provides the following abilities.
 * <ul>
 * <li>scroll with arrow keys</li>
 * <li>zoom in and out with ctrl + arrow up/down keys</li>
 * <li>context menus for adjusting axis range and zooming in/out.</li>
 * <li>file selector dialog to save chart to image file.</li>
 * <li>properties dialog to configure the chart settings</li>
 * </ul>
 */
public class InteractiveChart extends Chart implements PaintListener {

    /** the filter extensions */
    private static final String[] EXTENSIONS = new String[] { "*.jpeg",
            "*.jpg", "*.png" };

    /** the selection rectangle for zoom in/out */
    protected SelectionRectangle selection;

    /** the clicked time in milliseconds */
    private long clickedTime;

    /** the resources created with properties dialog */
    private PropertiesResources resources;

    /**
     * Constructor.
     * 
     * @param parent
     *            the parent composite
     * @param style
     *            the style
     */
    public InteractiveChart(Composite parent, int style) {
        super(parent, style);
        init();
    }

    /**
     * Initializes.
     */
    private void init() {
        selection = new SelectionRectangle();
        resources = new PropertiesResources();

        Composite plot = getPlotArea();
        plot.addListener(SWT.Resize, this);
        plot.addListener(SWT.MouseMove, this);
        plot.addListener(SWT.MouseDown, this);
        plot.addListener(SWT.MouseUp, this);
        plot.addListener(SWT.MouseWheel, this);
        plot.addListener(SWT.KeyDown, this);

        plot.addPaintListener(this);

        createMenuItems();
    }

    /**
     * Creates menu items.
     */
    private void createMenuItems() {
        Menu menu = new Menu(getPlotArea());
        getPlotArea().setMenu(menu);

        // adjust axis range menu group
        MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
        menuItem.setText(Messages.ADJUST_AXIS_RANGE_GROUP);
        Menu adjustAxisRangeMenu = new Menu(menuItem);
        menuItem.setMenu(adjustAxisRangeMenu);

        // adjust axis range
        menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
        menuItem.setText(Messages.ADJUST_AXIS_RANGE);
        menuItem.addListener(SWT.Selection, this);

        // adjust X axis range
        menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
        menuItem.setText(Messages.ADJUST_X_AXIS_RANGE);
        menuItem.addListener(SWT.Selection, this);

        // adjust Y axis range
        menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
        menuItem.setText(Messages.ADJUST_Y_AXIS_RANGE);
        menuItem.addListener(SWT.Selection, this);

        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // zoom in menu group
        menuItem = new MenuItem(menu, SWT.CASCADE);
        menuItem.setText(Messages.ZOOMIN_GROUP);
        Menu zoomInMenu = new Menu(menuItem);
        menuItem.setMenu(zoomInMenu);

        // zoom in both axes
        menuItem = new MenuItem(zoomInMenu, SWT.PUSH);
        menuItem.setText(Messages.ZOOMIN);
        menuItem.addListener(SWT.Selection, this);

        // zoom in X axis
        menuItem = new MenuItem(zoomInMenu, SWT.PUSH);
        menuItem.setText(Messages.ZOOMIN_X);
        menuItem.addListener(SWT.Selection, this);

        // zoom in Y axis
        menuItem = new MenuItem(zoomInMenu, SWT.PUSH);
        menuItem.setText(Messages.ZOOMIN_Y);
        menuItem.addListener(SWT.Selection, this);

        // zoom out menu group
        menuItem = new MenuItem(menu, SWT.CASCADE);
        menuItem.setText(Messages.ZOOMOUT_GROUP);
        Menu zoomOutMenu = new Menu(menuItem);
        menuItem.setMenu(zoomOutMenu);

        // zoom out both axes
        menuItem = new MenuItem(zoomOutMenu, SWT.PUSH);
        menuItem.setText(Messages.ZOOMOUT);
        menuItem.addListener(SWT.Selection, this);

        // zoom out X axis
        menuItem = new MenuItem(zoomOutMenu, SWT.PUSH);
        menuItem.setText(Messages.ZOOMOUT_X);
        menuItem.addListener(SWT.Selection, this);

        // zoom out Y axis
        menuItem = new MenuItem(zoomOutMenu, SWT.PUSH);
        menuItem.setText(Messages.ZOOMOUT_Y);
        menuItem.addListener(SWT.Selection, this);

        menuItem = new MenuItem(menu, SWT.SEPARATOR);

        // save as
        menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(Messages.SAVE_AS);
        menuItem.addListener(SWT.Selection, this);
    }

    /*
     * @see PaintListener#paintControl(PaintEvent)
     */
    public void paintControl(PaintEvent e) {
        selection.draw(e.gc);
    }

    /*
     * @see Listener#handleEvent(Event)
     */
    @Override
    public void handleEvent(Event event) {
        super.handleEvent(event);

        switch (event.type) {
        case SWT.MouseMove:
            handleMouseMoveEvent(event);
            break;
        case SWT.MouseDown:
            handleMouseDownEvent(event);
            break;
        case SWT.MouseUp:
            handleMouseUpEvent(event);
            break;
        case SWT.MouseWheel:
            handleMouseWheel(event);
            break;
        case SWT.KeyDown:
            handleKeyDownEvent(event);
            break;
        case SWT.Selection:
            handleSelectionEvent(event);
            break;
        default:
            break;
        }
    }

    /*
     * @see Chart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        resources.dispose();
    }

    /**
     * Handles mouse move event.
     * 
     * @param event
     *            the mouse move event
     */
    private void handleMouseMoveEvent(Event event) {
        if (!selection.isDisposed()) {
            selection.setEndPoint(event.x, event.y);
            redraw();
        }
    }

    /**
     * Handles the mouse down event.
     * 
     * @param event
     *            the mouse down event
     */
    private void handleMouseDownEvent(Event event) {
        if (event.button == 1) {
            selection.setStartPoint(event.x, event.y);
            clickedTime = System.currentTimeMillis();
        }
    }

    /**
     * Handles the mouse up event.
     * 
     * @param event
     *            the mouse up event
     */
    private void handleMouseUpEvent(Event event) {
        if (event.button == 1 && System.currentTimeMillis() - clickedTime > 100) {
            for (IAxis axis : getAxisSet().getAxes()) {
                Point range = null;
                if ((getOrientation() == SWT.HORIZONTAL && axis.getDirection() == Direction.X)
                        || (getOrientation() == SWT.VERTICAL && axis
                                .getDirection() == Direction.Y)) {
                    range = selection.getHorizontalRange();
                } else {
                    range = selection.getVerticalRange();
                }

                if (range != null && range.x != range.y) {
                    setRange(range, axis);
                }
            }
        }
        selection.dispose();
        redraw();
    }

    /**
     * Handles mouse wheel event.
     * 
     * @param event
     *            the mouse wheel event
     */
    private void handleMouseWheel(Event event) {
        for (IAxis axis : getAxes(SWT.HORIZONTAL)) {
            double coordinate = axis.getDataCoordinate(event.x);
            if (event.count > 0) {
                axis.zoomIn(coordinate);
            } else {
                axis.zoomOut(coordinate);
            }
        }

        for (IAxis axis : getAxes(SWT.VERTICAL)) {
            double coordinate = axis.getDataCoordinate(event.y);
            if (event.count > 0) {
                axis.zoomIn(coordinate);
            } else {
                axis.zoomOut(coordinate);
            }
        }
        redraw();
    }

    /**
     * Handles the key down event.
     * 
     * @param event
     *            the key down event
     */
    private void handleKeyDownEvent(Event event) {
        if (event.keyCode == SWT.ARROW_DOWN) {
        	switch (event.stateMask) {
        	case SWT.CTRL | SWT.SHIFT:
	            for (IAxis axis : getAxisSet().getYAxes()) {
	                axis.zoomOut();
	            }
	            break;
        	case SWT.CTRL:
                getAxisSet().zoomOut();
       			break;
        	case 0:
                for (IAxis axis : getAxes(SWT.VERTICAL)) {
                    axis.scrollDown();
                }
        	}
            redraw();
        } else if (event.keyCode == SWT.ARROW_UP) {
        	switch (event.stateMask) {
        	case SWT.CTRL | SWT.SHIFT:
	            for (IAxis axis : getAxisSet().getYAxes()) {
	                axis.zoomIn();
	            }
	            break;
        	case SWT.CTRL:
                getAxisSet().zoomIn();
       			break;
        	case 0:
                for (IAxis axis : getAxes(SWT.VERTICAL)) {
                    axis.scrollUp();
                }
        	}
            redraw();
        } else if (event.keyCode == SWT.ARROW_LEFT) {
        	switch (event.stateMask) {
        	case SWT.CTRL | SWT.SHIFT:
	            for (IAxis axis : getAxisSet().getXAxes()) {
	                axis.zoomIn();
	            }
	            break;
        	case 0:
            	for (IAxis axis : getAxes(SWT.HORIZONTAL)) {
                    axis.scrollDown();
                }
        	}
            redraw();
        } else if (event.keyCode == SWT.ARROW_RIGHT) {
        	switch (event.stateMask) {
        	case SWT.CTRL | SWT.SHIFT:
	            for (IAxis axis : getAxisSet().getXAxes()) {
	                axis.zoomOut();
	            }
	            break;
        	case 0:
            	for (IAxis axis : getAxes(SWT.HORIZONTAL)) {
                    axis.scrollUp();
                }
        	}
            redraw();
        } else if ((event.keyCode == 'A') || (event.keyCode == 'a')) {
        	if (event.stateMask == SWT.CTRL)
        		getAxisSet().adjustRange();
        }
    }

    /**
     * Gets the axes for given orientation.
     * 
     * @param orientation
     *            the orientation
     * @return the axes
     */
    private IAxis[] getAxes(int orientation) {
        IAxis[] axes;
        if (getOrientation() == orientation) {
            axes = getAxisSet().getXAxes();
        } else {
            axes = getAxisSet().getYAxes();
        }
        return axes;
    }

    /**
     * Handles the selection event.
     * 
     * @param event
     *            the event
     */
    private void handleSelectionEvent(Event event) {

        if (!(event.widget instanceof MenuItem)) {
            return;
        }
        MenuItem menuItem = (MenuItem) event.widget;

        if (menuItem.getText().equals(Messages.ADJUST_AXIS_RANGE)) {
            getAxisSet().adjustRange();
        } else if (menuItem.getText().equals(Messages.ADJUST_X_AXIS_RANGE)) {
            for (IAxis axis : getAxisSet().getXAxes()) {
                axis.adjustRange();
            }
        } else if (menuItem.getText().equals(Messages.ADJUST_Y_AXIS_RANGE)) {
            for (IAxis axis : getAxisSet().getYAxes()) {
                axis.adjustRange();
            }
        } else if (menuItem.getText().equals(Messages.ZOOMIN)) {
            getAxisSet().zoomIn();
        } else if (menuItem.getText().equals(Messages.ZOOMIN_X)) {
            for (IAxis axis : getAxisSet().getXAxes()) {
                axis.zoomIn();
            }
        } else if (menuItem.getText().equals(Messages.ZOOMIN_Y)) {
            for (IAxis axis : getAxisSet().getYAxes()) {
                axis.zoomIn();
            }
        } else if (menuItem.getText().equals(Messages.ZOOMOUT)) {
            getAxisSet().zoomOut();
        } else if (menuItem.getText().equals(Messages.ZOOMOUT_X)) {
            for (IAxis axis : getAxisSet().getXAxes()) {
                axis.zoomOut();
            }
        } else if (menuItem.getText().equals(Messages.ZOOMOUT_Y)) {
            for (IAxis axis : getAxisSet().getYAxes()) {
                axis.zoomOut();
            }
        } else if (menuItem.getText().equals(Messages.SAVE_AS)) {
            openSaveAsDialog();
        }
        redraw();
    }

    /**
     * Opens the Save As dialog.
     */
    private void openSaveAsDialog() {
        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        dialog.setText(Messages.SAVE_AS_DIALOG_TITLE);
        dialog.setFilterExtensions(EXTENSIONS);

        String filename = dialog.open();
        if (filename == null) {
            return;
        }

        int format;
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            format = SWT.IMAGE_JPEG;
        } else if (filename.endsWith(".png")) {
            format = SWT.IMAGE_PNG;
        } else {
            format = SWT.IMAGE_UNDEFINED;
        }

        if (format != SWT.IMAGE_UNDEFINED) {
            save(filename, format);
        }
    }

    /**
     * Sets the axis range.
     * 
     * @param range
     *            the axis range in pixels
     * @param axis
     *            the axis to set range
     */
    private void setRange(Point range, IAxis axis) {
        if (range == null) {
            return;
        }

        double min = axis.getDataCoordinate(range.x);
        double max = axis.getDataCoordinate(range.y);

        axis.setRange(new Range(min, max));
    }
}
