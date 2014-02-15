package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.*;

/**
 * Project specific TabbedPane implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXTabs extends JTabbedPane {
  /** Index of currently dragged tab (default: {@code -1}). */
  private int draggedTab = -1;

  /**
   * Default constructor.
   * @param win parent window
   */
  public BaseXTabs(final Window win) {
    BaseXLayout.addInteraction(this, win);
  }

  /**
   * Adds a component with a custom tab header.
   * @param content tab content
   * @param header tab header
   * @param index index
   */
  public void add(final Component content, final Component header, final int index) {
    add(content, index);
    setSelectedComponent(content);
    setTabComponentAt(getSelectedIndex(), header);
  }

  /**
   * Adds drag and drop support.
   * @param last include last tab
   */
  public void addDragDrop(final boolean last) {
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(final MouseEvent e) {
        if(draggedTab == -1) {
          int t = getUI().tabForCoordinate(BaseXTabs.this, e.getX(), e.getY());
          final int tabs = getTabCount();
          if(tabs == (last ? 1 : 2) || !last && t + 1 == tabs) t = -1;
          if(t != -1) {
            draggedTab = t;
            setCursor(GUIConstants.CURSORMOVE);
          }
        } else {
          drop(last, e);
        }
        refreshTabs();
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(final MouseEvent e) {
        if(draggedTab < 0) return;
        drop(last, e);
        draggedTab = -1;
        setCursor(GUIConstants.CURSORARROW);
        refreshTabs();
      }
    });
  }

  /**
   * Drops the current tab.
   * @param last include last tab
   * @param e mouse event
   */
  private void drop(final boolean last, final MouseEvent e) {
    final int newTab = Math.min(getTabCount() - (last ? 1 : 2),
        getUI().tabForCoordinate(BaseXTabs.this, e.getX(), e.getY()));

    if(newTab >= 0 && newTab != draggedTab) {
      final Component comp = getComponentAt(draggedTab);
      final Component head = getTabComponentAt(draggedTab);
      removeTabAt(draggedTab);
      add(comp, head, newTab);
      draggedTab = newTab;
    }
  }

  /**
   * Refreshes the appearance of all tabs.
   */
  private void refreshTabs() {
    final int tabs = getTabCount();
    for(int t = 0; t < tabs; t++) {
      final Component tab = getTabComponentAt(t);
      if(tab instanceof Container) {
        final Container cont = (Container) tab;
        final int comps = cont.getComponentCount();
        for(int c = 0; c < comps; c++) {
          cont.getComponent(c).setEnabled(draggedTab == -1 || t == draggedTab);
        }
      }
    }
  }
}
