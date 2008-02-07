package org.basex.gui.layout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.basex.gui.GUICommands;
import org.basex.gui.view.View;

/**
 * Project specific Popup menu implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Lukas Kircher
 */
public final class BaseXPopup extends JPopupMenu implements ActionListener {
  /** Popup reference. */
  public GUICommands[] popup;
  
  /**
   * Constructor.
   * @param comp component reference
   * @param pop popup reference
   */
  public BaseXPopup(final BaseXPanel comp,
      final GUICommands[] pop) {
    popup = pop;
    
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(final MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e))
          show(e.getComponent(), e.getX() - 10, e.getY() - 15);
      }
    });
    comp.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        final int key = e.getKeyCode();
        if(key == KeyEvent.VK_CONTEXT_MENU) {
          show(e.getComponent(), 10, 10);
        }
      }
    });

    removeAll();
    for(final GUICommands c : popup) {
      if(c == null) {
        addSeparator();
      } else {
        final JMenuItem item = add(c.entry);
        item.addActionListener(this);
        item.setMnemonic(c.entry.charAt(0));
        item.setActionCommand(c.name());
        item.setToolTipText(c.help);

        //if(c.key != null) item.setAccelerator(KeyStroke.getKeyStroke(c.key));
      }
    }
  }

  /**
   * Adds a single menu item.
   * @param s item text
   * @param enabled enabled flag
   */
  public void add(final String s, final boolean enabled) {
    final JMenuItem item = add(s);
    item.addActionListener(this);
    item.setMnemonic(s.charAt(0));
    BaseXLayout.enable(item, enabled);
  }

  /**
   * Refreshes the popup menu.
   */
  public void refresh() {
    for(int b = 0; b < popup.length; b++) {
      if(popup[b] == null) continue;
      final JMenuItem item = (JMenuItem) getComponent(b);
      popup[b].refresh(item, true);
    }
  }

  /** {@inheritDoc} */
  public void actionPerformed(final ActionEvent e) {
    if(View.working) return;
    GUICommands.get(e.getActionCommand()).execute();
  }
}
