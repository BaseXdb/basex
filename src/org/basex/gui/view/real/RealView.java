package org.basex.gui.view.real;

import java.awt.Color;
import java.awt.Graphics;

import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.view.View;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author ...
 */
public class RealView extends View {
  /**
   * Default Constructor.
   */
  public RealView() {
    super(null);
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
    // TODO Auto-generated method stub
    
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
    
    g.setColor(Color.black);
    
    Data data = GUI.context.data(); 
    if(data == null || focused == -1) return;
    
    if(data.kind(focused) == Data.TEXT) {
      byte[] txt = data.text(focused);
      g.drawString(Token.string(txt), 20, 20);
      
    }
    
  }
}
