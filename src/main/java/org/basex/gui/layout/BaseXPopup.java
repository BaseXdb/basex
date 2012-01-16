package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.basex.gui.GUI;
import org.basex.gui.GUICommand;

/**
 * Project specific Popup menu implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class BaseXPopup extends JPopupMenu {
  /** Popup reference. */
  private final GUICommand[] popup;
  /** Reference to main window. */
  final GUI gui;

  /**
   * Constructor.
   * @param comp component reference
   * @param pop popup reference
   */
  public BaseXPopup(final BaseXPanel comp, final GUICommand[] pop) {
    popup = pop;
    gui = comp.gui;

    // both listeners must be implemented to support different platforms
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if(!gui.updating && e.isPopupTrigger())
          show(e.getComponent(), e.getX() - 10, e.getY() - 15);
      }
      @Override
      public void mouseReleased(final MouseEvent e) {
        mousePressed(e);
      }
    });
    comp.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(!gui.updating && CONTEXT.is(e)) show(e.getComponent(), 10, 10);
      }
    });

    for(final GUICommand c : pop) {
      if(c == null) {
        addSeparator();
      } else {
        final JMenuItem item = add(c.label());
        item.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            if(!gui.updating) c.execute(comp.gui);
          }
        });
        item.setMnemonic(c.label().charAt(0));
        item.setToolTipText(c.help());
      }
    }
  }

  @Override
  public void show(final Component comp, final int x, final int y) {
    for(int b = 0; b < popup.length; ++b) {
      if(popup[b] != null) popup[b].refresh(gui, (JMenuItem) getComponent(b));
    }
    super.show(comp, x, y);
  }
}
