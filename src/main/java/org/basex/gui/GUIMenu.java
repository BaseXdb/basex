package org.basex.gui;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * This is the menu bar of the main window.
 * The menu structure is defined in {@link GUIConstants#MENUBAR} and
 * {@link GUIConstants#MENUITEMS}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class GUIMenu extends JMenuBar {
  /** Referenced menu items. */
  private final JMenuItem[] items;
  /** Reference to main window. */
  private final GUI gui;

  /**
   * Initializes the menu bar.
   * @param main reference to the main window
   */
  GUIMenu(final GUI main) {
    gui = main;
    if(Prop.langright) setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    final String sm = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ==
      Event.META_MASK ? "meta" : "ctrl";

    // create menu for each top level menu entries
    int c = 0;
    for(int b = 0; b < MENUBAR.length; ++b) {
      for(int i = 0; i < MENUITEMS[b].length; ++i) ++c;
    }
    items = new JMenuItem[c];

    c = 0;
    // loop through all menu entries
    final StringBuilder gmnem = new StringBuilder();
    for(int b = 0; b < MENUBAR.length; ++b) {
      final JMenu menu = new JMenu(MENUBAR[b]);
      BaseXLayout.setMnemonic(menu, gmnem);

      // create menu point for each sub menu entry
      final StringBuilder mnem = new StringBuilder();
      for(int i = 0; i < MENUITEMS[b].length; ++i) {
        // add a separator
        final GUICommand cmd = MENUITEMS[b][i];
        if(cmd == GUICommand.EMPTY) {
          menu.addSeparator();
        } else if(cmd != null) {
          // add a menu entry
          final JMenuItem item = newItem(cmd, gui, mnem);
          final String sc = cmd.key();
          if(sc != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(Util.info(sc, sm)));
          }
          items[c++] = item;
          if(Prop.langright) {
            item.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
          }
          menu.add(item);
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

    final String desc = cmd.label();
    final JMenuItem item = cmd.checked() ?
        new JCheckBoxMenuItem(desc) : new JMenuItem(desc);

    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        cmd.execute(gui);
      }
    });
    BaseXLayout.setMnemonic(item, mnem);
    item.setToolTipText(cmd.help());
    return item;
  }

  /**
   * Refreshes the menu items.
   */
  void refresh() {
    int c = 0;
    for(int b = 0; b < MENUBAR.length; ++b) {
      for(int i = 0; i < MENUITEMS[b].length; ++i) {
        final GUICommand item = MENUITEMS[b][i];
        if(item != GUICommand.EMPTY && item != null) {
          item.refresh(gui, items[c++]);
        }
      }
    }
  }
}
