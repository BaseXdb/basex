package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.view.text.XMLSyntax;
import org.basex.util.Token;

/**
 * Dialog window for displaying information about the project.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogHelp extends Dialog {
  /** Text Area. */
  private final BaseXText area;

  /**
   * Default Constructor.
   * @param main reference to the main window
   */
  public DialogHelp(final GUI main) {
    super(main, HELPTIT, false);
    panel.setBorder(5, 5, 5, 5);
    panel.setBackground(Color.white);

    area = new BaseXText(gui, Token.EMPTY, false);
    area.setSyntax(new XMLSyntax());
    set(area, BorderLayout.CENTER);

    setResizable(true);
    final int[] size = GUIProp.helpsize;
    setPreferredSize(new Dimension(size[0], size[1]));

    finish(GUIProp.helploc);
    refreshLayout();
  }

  /**
   * Sets the help text.
   * @param help help text
   */
  public void setText(final byte[] help) {
    area.setText(help);
  }

  /**
   * Called when GUI design has changed.
   */
  public void refreshLayout() {
    area.setFont(GUIConstants.font);
  }

  @Override
  public void close() {
    GUIProp.helpsize[0] = getWidth();
    GUIProp.helpsize[1] = getHeight();
    dispose();
  }

  @Override
  public void cancel() {
    close();
  }
}
