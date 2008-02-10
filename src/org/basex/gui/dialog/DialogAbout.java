package org.basex.gui.dialog;

import static org.basex.gui.GUIConstants.*;
import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.Text;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for displaying information about the project.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogAbout extends Dialog {
  /**
   * Default Constructor.
   * @param parent parent frame
   */
  public DialogAbout(final JFrame parent) {
    super(parent, ABOUTTITLE, true);

    BaseXBack p = new BaseXBack();
    p.setBackground(Color.white);
    p.setLayout(new BorderLayout(12, 0));
    p.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 15, 22)));

    BaseXLabel label = new BaseXLabel(GUI.icon(IMGLOGO));
    label.setVerticalAlignment(SwingConstants.TOP);
    p.add(label, BorderLayout.WEST);

    final BaseXBack pp = new BaseXBack(GUIConstants.FILL.NONE);
    pp.setLayout(new TableLayout(13, 1));

    label = new BaseXLabel(Text.TITLE);
    label.setFont(getFont().deriveFont(1));
    pp.add(label);
    pp.add(new BaseXLabel(Text.URL));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(COPYRIGHT));
    pp.add(new BaseXLabel(LICENSE));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(DEVELOPER));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(CONTRIBUTE1));
    pp.add(new BaseXLabel(CONTRIBUTE2));

    p.add(pp, BorderLayout.EAST);
    add(p, BorderLayout.NORTH);

    p = new BaseXBack();
    p.add(new BaseXButton(BUTTONOK, null, this), BorderLayout.EAST);
    add(p, BorderLayout.EAST);

    finish(parent, null);
  }
}
