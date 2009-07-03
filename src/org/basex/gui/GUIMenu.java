package org.basex.gui;

import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import org.basex.BaseX;
import org.basex.gui.layout.BaseXLabel;
import static org.basex.gui.GUIConstants.*;

/**
 * This is the menu bar of the main window.
 * The menu structure is defined in {@link GUIConstants#MENUBAR} and
 * {@link GUIConstants#MENUITEMS}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class GUIMenu extends JMenuBar {
  /** Referenced menu items. */
  private final JMenuItem[] items;
  /** Reference to main window. */
  final GUI gui;

  /**
   * Initializes the menu bar.
   * @param main reference to the main window
   */
  public GUIMenu(final GUI main) {
    gui = main;

    final String sm = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ==
      Event.META_MASK ? "meta" : "ctrl";

    // create menu for each top level menu entries
    int c = 0;
    for(int i = 0; i < MENUBAR.length; i++)
      for(int j = 0; j < MENUITEMS[i].length; j++) c++;
    items = new JMenuItem[c];

    c = 0;
    // loop through all menu entries
    for(int i = 0; i < MENUBAR.length; i++) {
      final JMenu menu = new JMenu(MENUBAR[i]);
      menu.setMnemonic((int) MENUBAR[i].charAt(0));

      // create menu point for each sub menu entry
      for(int j = 0; j < MENUITEMS[i].length; j++) {
        // add a separator
        final Object subEntry = MENUITEMS[i][j];
        if(subEntry == null) {
          menu.addSeparator();
        } else {
          JComponent comp = null;

          // add a menu entry
          if(subEntry instanceof String) {
            comp = new BaseXLabel((String) subEntry);
            comp.setFont(getFont());
            comp.setBorder(new EmptyBorder(2, 5, 2, 0));
          } else {
            final GUICommand cmd = (GUICommand) subEntry;
            final JMenuItem item = cmd.checked() ?
                new JCheckBoxMenuItem(cmd.desc()) : new JMenuItem(cmd.desc());
            item.addActionListener(new ActionListener() {
              public void actionPerformed(final ActionEvent e) {
                cmd.execute(gui);
              }
            });
            item.setMnemonic(cmd.desc().charAt(0));
            item.setToolTipText(cmd.help());

            final String sc = cmd.key();
            if(sc != null) {
              item.setAccelerator(KeyStroke.getKeyStroke(BaseX.info(sc, sm)));
            }
            comp = item;
            items[c++] = item;
          }
          menu.add(comp);
        }
      }
      add(menu);
    }
  }

  /**
   * Refreshes the menu items.
   */
  public void refresh() {
    int c = 0;
    for(int i = 0; i < MENUBAR.length; i++) {
      for(int j = 0; j < MENUITEMS[i].length; j++) {
        final Object item = MENUITEMS[i][j];
        if(!(item instanceof GUICommand)) continue;
        ((GUICommand) item).refresh(gui, items[c++]);
      }
    }
  }
}
