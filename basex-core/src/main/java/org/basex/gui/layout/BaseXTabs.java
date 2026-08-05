package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.*;

/**
 * Project specific TabbedPane implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXTabs extends JTabbedPane {
  /** Index of currently dragged tab (default: {@code -1}). */
  private int draggedTab = -1;

  /**
   * Default constructor.
   * @param win parent window
   */
  public BaseXTabs(final BaseXWindow win) {
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
   * Indicates if a tab is currently being dragged.
   * @return result of check
   */
  public boolean dragged() {
    return draggedTab != -1;
  }

  /**
   * Adds drag and drop support.
   */
  public void addDragDrop() {
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(final MouseEvent e) {
        if(draggedTab == -1) {
          final int t = getUI().tabForCoordinate(BaseXTabs.this, e.getX(), e.getY());
          if(t != -1 && getTabCount() > 1) {
            draggedTab = t;
            setCursor(GUIConstants.CURSORMOVE);
          }
        } else {
          drop(e);
        }
        refreshTabs();
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(final MouseEvent e) {
        if(draggedTab < 0) return;
        drop(e);
        draggedTab = -1;
        setCursor(GUIConstants.CURSORARROW);
        refreshTabs();
      }
    });
  }

  /**
   * Drops the current tab.
   * @param e mouse event
   */
  private void drop(final MouseEvent e) {
    final int newTab = Math.min(getTabCount() - 1,
        getUI().tabForCoordinate(this, e.getX(), e.getY()));
    if(newTab < 0) return;

    // swap adjacent tabs, one at a time
    while(draggedTab != newTab) {
      final int tab = draggedTab + (newTab > draggedTab ? 1 : -1);
      swap(draggedTab, tab);
      draggedTab = tab;
    }
  }

  /**
   * Swaps two adjacent tabs.
   * @param tab1 index of first tab
   * @param tab2 index of second tab
   */
  private void swap(final int tab1, final int tab2) {
    // re-insert the unselected tab: removing the selected one would repaint its contents
    final int source = getSelectedIndex() == tab1 ? tab2 : tab1;
    final int target = source == tab1 ? tab2 : tab1;
    final Component comp = getComponentAt(source), head = getTabComponentAt(source);
    removeTabAt(source);
    add(comp, target);
    setTabComponentAt(target, head);
  }

  /**
   * Refreshes the appearance of all tabs.
   */
  private void refreshTabs() {
    final int tabs = getTabCount();
    for(int t = 0; t < tabs; t++) {
      final Component tab = getTabComponentAt(t);
      if(tab instanceof final Container cont) {
        final int comps = cont.getComponentCount();
        for(int c = 0; c < comps; c++) {
          cont.getComponent(c).setEnabled(draggedTab == -1 || t == draggedTab);
        }
      }
    }
  }
}
