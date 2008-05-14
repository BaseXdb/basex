package org.basex.gui.view.xpath;

import static org.basex.gui.GUIConstants.*;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import org.basex.core.Commands;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.view.View;

/**
 * This class offers a real tree view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author ...
 */
public final class XPathView extends View {
  /** Input field for XPath queries. */
  protected final BaseXTextField input;
  /** Header string. */
  protected final BaseXLabel header;
  /** Button box. */
  protected final BaseXBack back;
  
  /**
   * Default Constructor.
   */
  public XPathView() {
    super(null);

    back = new BaseXBack(FILL.NONE);
    back.setLayout(new BorderLayout());
    header = new BaseXLabel(GUIConstants.XPATHVIEW, 10);
    back.add(header, BorderLayout.NORTH);

    input = new BaseXTextField(null);
    input.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        final String query = input.getText();
        GUI.get().execute(Commands.XPATH, query);
      }
    });
    
    setBorder(10, 10, 10, 10);
    setLayout(new BorderLayout(0, 4));
    back.add(input, BorderLayout.CENTER);
    
    add(back, BorderLayout.NORTH);

    refreshLayout();
  }
  
  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void refreshFocus() {
    repaint();
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void refreshInit() {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    header.setForeground(COLORS[16]);
  }

  @Override
  protected void refreshMark() {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void refreshUpdate() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
  }
}
