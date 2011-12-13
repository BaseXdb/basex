package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.layout.TextSyntax;

/**
 * Dialog window for displaying information about the project.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DialogHelp extends Dialog {
  /** Text area. */
  private final BaseXEditor area;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogHelp(final GUI main) {
    super(main, HELPTIT, false);
    panel.border(5);
    panel.setBackground(Color.white);

    area = new BaseXEditor(false, gui);
    area.setSyntax(new TextSyntax());
    set(area, BorderLayout.CENTER);

    setResizable(true);
    final int[] size = main.gprop.nums(GUIProp.HELPSIZE);
    setPreferredSize(new Dimension(size[0], size[1]));

    finish(main.gprop.nums(GUIProp.HELPLOC));
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
    gui.gprop.set(GUIProp.SHOWHELP, false);
    gui.gprop.set(GUIProp.HELPSIZE, new int[] { getWidth(), getHeight() });
    gui.refreshControls();
    gui.help = null;
    dispose();
  }

  @Override
  public void cancel() {
    close();
  }
}
