package org.basex.gui.layout;

import java.awt.Component;
import java.awt.Window;
import javax.swing.JTabbedPane;

/**
 * Project specific TabbedPane implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXTabs extends JTabbedPane {
  /**
   * Default constructor.
   * @param win parent window
   */
  public BaseXTabs(final Window win) {
    super();
    BaseXLayout.addInteraction(this, win);
  }

  /**
   * Adds the specified component.
   * @param content tab content
   * @param header tab header
   * @param index index
   */
  public void add(final Component content, final Component header,
      final int index) {

    add(content, index);
    setSelectedComponent(content);
    setTabComponentAt(getSelectedIndex(), header);
  }
}
