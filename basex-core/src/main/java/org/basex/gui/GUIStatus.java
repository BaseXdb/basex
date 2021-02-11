package org.basex.gui;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.layout.*;

/**
 * This is the status bar of the main window. It displays progress information
 * and includes a memory status.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class GUIStatus extends BaseXPanel {
  /** Status text. */
  private final BaseXLabel label;

  /**
   * Constructor.
   * @param gui reference to the main window
   */
  GUIStatus(final GUI gui) {
    super(gui);
    setPreferredSize(new Dimension(getPreferredSize().width, (int) (getFont().getSize() * 1.5)));
    addMouseListener(this);
    addMouseMotionListener(this);

    layout(new BorderLayout(4, 0));
    label = new BaseXLabel(OK).border(0, 4, 0, 0);
    add(label, BorderLayout.CENTER);
    add(new BaseXMem(gui, true), BorderLayout.EAST);
  }

  /**
   * Sets the status text.
   * @param txt the text to be set
   */
  public void setText(final String txt) {
    label.setText(txt);
    label.setForeground(GUIConstants.TEXT);
  }

  /**
   * Sets the status text.
   * @param txt the text to be set
   */
  public void setError(final String txt) {
    label.setText(txt);
    label.setForeground(GUIConstants.RED);
  }
}
