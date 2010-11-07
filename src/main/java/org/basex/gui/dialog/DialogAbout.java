package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for displaying information about the project.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DialogAbout extends Dialog {
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogAbout(final GUI main) {
    super(main, ABOUTTITLE, true);

    BaseXBack p = new BaseXBack();
    p.setBackground(Color.white);
    p.setLayout(new BorderLayout(12, 0));
    p.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 15, 22)));

    BaseXLabel label = new BaseXLabel();
    label.setIcon(BaseXLayout.icon("logo"));
    label.setVerticalAlignment(SwingConstants.TOP);
    p.add(label, BorderLayout.WEST);

    final BaseXBack pp = new BaseXBack(GUIConstants.Fill.NONE);
    pp.setLayout(new TableLayout(15, 1));

    label = new BaseXLabel(Text.TITLE, false, true);
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
    pp.add(Box.createVerticalStrut(7));
    final String lang = main.context.prop.get(Prop.LANG);
    pp.add(new BaseXLabel(TRANSLATION + DialogPrefs.creds(lang)));
    p.add(pp, BorderLayout.EAST);
    add(p, BorderLayout.NORTH);

    p = new BaseXBack();
    p.add(new BaseXButton(BUTTONOK, this), BorderLayout.EAST);
    add(p, BorderLayout.EAST);

    finish(null);
  }
}
