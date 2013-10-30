package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.*;

/**
 * Project specific Popup menu implementation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class BaseXPopup extends JPopupMenu {
  /** Reference to main window. */
  private final GUI gui;

  /** Popup reference. */
  private final GUICmd[] popup;

  /**
   * Constructor.
   * @param comp component reference
   * @param pop popup reference
   */
  public BaseXPopup(final BaseXPanel comp, final GUICmd... pop) {
    this(comp, comp.gui, pop);
  }

  /**
   * Constructor.
   * @param comp component reference
   * @param g gui reference
   * @param pop popup reference
   */
  public BaseXPopup(final JComponent comp, final GUI g, final GUICmd... pop) {
    popup = pop;
    gui = g;

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

    for(final GUICmd c : pop) {
      if(c == null) {
        addSeparator();
      } else {
        final JMenuItem item = add(c.label());
        item.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            if(!gui.updating) c.execute(gui);
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
      if(popup[b] != null) popup[b].refresh(gui, (AbstractButton) getComponent(b));
    }
    super.show(comp, x, y);
  }
}
