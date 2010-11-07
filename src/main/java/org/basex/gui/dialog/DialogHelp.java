package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.view.text.XMLSyntax;

/**
 * Dialog window for displaying information about the project.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DialogHelp extends Dialog {
  /** Text area. */
  private final BaseXText area;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogHelp(final GUI main) {
    super(main, HELPTIT, false);
    panel.setBorder(5, 5, 5, 5);
    panel.setBackground(Color.white);

    area = new BaseXText(false, gui);
    area.setSyntax(new XMLSyntax());
    set(area, BorderLayout.CENTER);

    setResizable(true);
    final int[] size = main.prop.nums(GUIProp.HELPSIZE);
    setPreferredSize(new Dimension(size[0], size[1]));

    finish(main.prop.nums(GUIProp.HELPLOC));
    refresh();
  }

  /**
   * Sets the help text.
   * @param help help text
   */
  public void setText(final byte[] help) {
    area.setText(help);
  }

  /**
   * Refreshes the help area.
   */
  public void refresh() {
    area.setFont(GUIConstants.font);
  }

  @Override
  public void close() {
    gui.prop.set(GUIProp.SHOWHELP, false);
    gui.prop.set(GUIProp.HELPSIZE, new int[] { getWidth(), getHeight() });
    gui.refreshControls();
    gui.help = null;
    dispose();
  }

  @Override
  public void cancel() {
    close();
  }
}
