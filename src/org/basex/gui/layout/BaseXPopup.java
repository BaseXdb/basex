package org.basex.gui.layout;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.basex.gui.GUICommand;
import org.basex.gui.view.View;

/**
 * Project specific Popup menu implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class BaseXPopup extends JPopupMenu {
  /** Popup reference. */
  private GUICommand[] popup;
  
  /**
   * Constructor.
   * @param comp component reference
   * @param pop popup reference
   */
  public BaseXPopup(final BaseXPanel comp, final GUICommand[] pop) {
    popup = pop;
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(final MouseEvent e) {
        if(!View.working && SwingUtilities.isRightMouseButton(e))
          show(e.getComponent(), e.getX() - 10, e.getY() - 15);
      }
    });
    comp.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        final int key = e.getKeyCode();
        if(!View.working && key == KeyEvent.VK_CONTEXT_MENU) {
          show(e.getComponent(), 10, 10);
        }
      }
    });

    for(final GUICommand c : pop) {
      if(c == null) {
        addSeparator();
      } else {
        final JMenuItem item = add(c.desc());
        item.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            if(!View.working) c.execute();
          }
        });
        item.setMnemonic(c.desc().charAt(0));
        item.setToolTipText(c.help());
      }
    }
  }
  
  @Override
  public void show(final Component comp, final int x, final int y) {
    for(int b = 0; b < popup.length; b++) {
      if(popup[b] == null) continue;
      popup[b].refresh((JMenuItem) getComponent(b));
    }
    super.show(comp, x, y);
  }
}
