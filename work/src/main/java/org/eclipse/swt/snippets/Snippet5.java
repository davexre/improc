/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.snippets;

/*
 * ScrolledComposite example snippet: scroll a control in a scrolled composite
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet5 {

public static void main (String [] args) 
{
    Display display = new Display ();
    final Shell shell = new Shell (display);
    shell.setLayout(new FillLayout());

    // this button is always 400 x 400. Scrollbars appear if the window is resized to be
    // too small to show part of the button
    ScrolledComposite c1 = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    Button b1 = new Button(c1, SWT.PUSH);
    b1.setText("fixed size button");
    b1.setSize(400, 400);
    c1.setContent(b1);

    // this button has a minimum size of 400 x 400. If the window is resized to be big
    // enough to show more than 400 x 400, the button will grow in size. If the window
    // is made too small to show 400 x 400, scrollbars will appear.
    ScrolledComposite c2 = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    final Composite c2inside = new Composite(c2, SWT.BORDER);
    c2inside.setLayout(new FillLayout(SWT.VERTICAL));
    c2inside.setLayout(new GridLayout(1, true));
    c2.setContent(c2inside);
    c2.setExpandHorizontal(true);
    c2.setExpandVertical(true);
    c2.setMinSize(400, -1);
//    c2.setShowFocusedControl(true);
    
//    Button b2 = new Button(c2inside, SWT.PUSH);
//    b2.setText("expanding button");
//    b2.setLayoutData(getLayoutData());
    b1.addSelectionListener(new SelectionAdapter() {
    	public void widgetSelected(SelectionEvent e) {
    	    Button tmp = new Button(c2inside, SWT.PUSH);
    	    tmp.setText("also expanding button");

    		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
//    		gd.minimumHeight = 50;
//    		gd.minimumWidth = 300;

    	    tmp.setLayoutData(gd);
    	    shell.layout(true, true);
    	}
	});

    shell.setSize(600, 300);
    shell.open ();
    while (!shell.isDisposed ()) {
        if (!display.readAndDispatch ()) display.sleep ();
    }
    display.dispose ();
}

}