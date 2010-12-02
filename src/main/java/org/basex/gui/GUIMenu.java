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

import org.basex.gui.layout.BaseXLabel;
import org.basex.util.Util;
import static org.basex.gui.GUIConstants.*;

/**
 * This is the menu bar of the main window.
 * The menu structure is defined in {@link GUIConstants#MENUBAR} and
 * {@link GUIConstants#MENUITEMS}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  GUIMenu(final GUI main) {
    gui = main;

    final String sm = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ==
      Event.META_MASK ? "meta" : "ctrl";

    // create menu for each top level menu entries
    int c = 0;
    for(int i = 0; i < MENUBAR.length; ++i)
      for(int j = 0; j < MENUITEMS[i].length; ++j) ++c;
    items = new JMenuItem[c];

    c = 0;
    // loop through all menu entries
    final StringBuilder gmnem = new StringBuilder();
    for(int b = 0; b < MENUBAR.length; ++b) {
      final JMenu menu = new JMenu(MENUBAR[b]);
      setMnemonic(menu, MENUBAR[b], gmnem);

      // create menu point for each sub menu entry
      final StringBuilder mnem = new StringBuilder();
      for(int i = 0; i < MENUITEMS[b].length; ++i) {
        // add a separator
        final Object sub = MENUITEMS[b][i];
        if(sub == null) continue;
        if(sub.equals(SEPARATOR)) {
          menu.addSeparator();
        } else {
          JComponent comp = null;

          // add a menu entry
          if(sub instanceof String) {
            final BaseXLabel label = new BaseXLabel((String) sub);
            label.border(2, 5, 2, 0).setFont(getFont());
            comp = label;
          } else {
            final GUICommand cmd = (GUICommand) sub;
            final JMenuItem item = newItem(cmd, gui, mnem);
            final String sc = cmd.key();
            if(sc != null) {
              item.setAccelerator(KeyStroke.getKeyStroke(Util.info(sc, sm)));
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
   * Creates a new menu item.
   * @param cmd command
   * @param gui gui reference
   * @param mnem assigned mnenomics
   * @return menu item
   */
  public static JMenuItem newItem(final GUICommand cmd, final GUI gui,
      final StringBuilder mnem) {

    final String desc = cmd.desc();
    final JMenuItem item = cmd.checked() ?
        new JCheckBoxMenuItem(desc) : new JMenuItem(desc);

    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        cmd.execute(gui);
      }
    });
    
    setMnemonic(item, desc, mnem);
    item.setToolTipText(cmd.help());
    return item;
  }
  
  /**
   * Sets a mnemomic.
   * @param item menu item
   * @param label menu label
   * @param mnem assigned mnemonics
   */
  private static void setMnemonic(final JMenuItem item, final String label,
      final StringBuilder mnem) {
    // find and assign unused mnemomic
    for(int d = 0; d < label.length(); d++) {
      final char ch = Character.toLowerCase(label.charAt(d));
      if(mnem.indexOf(Character.toString(ch)) == -1) {
        item.setMnemonic(ch);
        mnem.append(ch);
        break;
      }
    }
  }

  /**
   * Refreshes the menu items.
   */
  void refresh() {
    int c = 0;
    for(int i = 0; i < MENUBAR.length; ++i) {
      for(int j = 0; j < MENUITEMS[i].length; ++j) {
        final Object item = MENUITEMS[i][j];
        if(!(item instanceof GUICommand)) continue;
        ((GUICommand) item).refresh(gui, items[c++]);
      }
    }
  }
}
