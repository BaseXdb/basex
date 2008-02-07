package org.basex.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import org.basex.gui.layout.BaseXLabel;
import static org.basex.gui.GUIConstants.*;

/**
 * This is the menu bar of the main window.
 * The menu structure is defined in {@link GUIConstants#MENUBAR} and
 * {@link GUIConstants#MENUITEMS}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class GUIMenu extends JMenuBar implements ActionListener {
  /** Referenced menu items. */
  private final JComponent[] menuItems;

  /**
   * Initializes the menu bar.
   */
  public GUIMenu() {
    // create menu for each top level menu entries
    int c = 0;
    for(int i = 0; i < MENUBAR.length; i++)
      for(int j = 0; j < MENUITEMS[i].length; j++) c++;
    menuItems = new JComponent[c];

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
            final GUICommands cmd = (GUICommands) subEntry;
            final JMenuItem item = cmd.checked() ?
                new JCheckBoxMenuItem(cmd.entry) : new JMenuItem(cmd.entry);
            item.addActionListener(this);
            item.setMnemonic(cmd.entry.charAt(0));
            item.setActionCommand(cmd.toString());
            item.setToolTipText(cmd.help);

            if(cmd.key != null) item.setAccelerator(
                KeyStroke.getKeyStroke(cmd.key));
            comp = item;
          }
          menuItems[c++] = comp;
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
    final boolean db = GUI.context.db();
    for(final JComponent comp : menuItems) {
      if(comp instanceof JMenuItem) {
        final JMenuItem item = (JMenuItem) comp;
        GUICommands.get(item.getActionCommand()).refresh(item, db);
      }
    }
  }

  /**
   * Reacts on a menu choice.
   * @param e action event
   */
  public void actionPerformed(final ActionEvent e) {
    GUICommands.get(e.getActionCommand()).execute();
  }
}
