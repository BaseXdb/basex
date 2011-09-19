package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.basex.core.MainProp;
import org.basex.core.Text;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for displaying information about the project.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DialogAbout extends Dialog {
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogAbout(final GUI main) {
    super(main, ABOUTTITLE);

    BaseXBack p = new BaseXBack(new BorderLayout(12, 0));
    p.setBackground(Color.white);
    p.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 15, 22)));

    final BaseXLabel label = new BaseXLabel();
    label.setIcon(BaseXLayout.icon("logo"));
    label.setVerticalAlignment(SwingConstants.TOP);
    label.setCursor(GUIConstants.CURSORHAND);
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        Dialog.browse(gui, URL);
      }
    });

    p.add(label, BorderLayout.WEST);

    final BaseXBack pp = new BaseXBack(GUIConstants.Fill.NONE).layout(
        new TableLayout(16, 1));

    pp.add(new BaseXLabel(Text.TITLE, false, true));
    pp.add(new BaseXLabel(Text.URL));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(COPYRIGHT));
    pp.add(new BaseXLabel(LICENSE));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(DEVELOPER));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(CONTRIBUTE1));
    pp.add(new BaseXLabel(CONTRIBUTE2));
    pp.add(new BaseXLabel(CONTRIBUTE3));
    pp.add(Box.createVerticalStrut(7));
    final String lang = main.context.mprop.get(MainProp.LANGUAGE);
    pp.add(new BaseXLabel(TRANSLATION + DialogPrefs.creds(lang)));
    p.add(pp, BorderLayout.EAST);
    add(p, BorderLayout.NORTH);

    p = new BaseXBack();
    p.add(newButtons(this, BUTTONOK));
    add(p, BorderLayout.EAST);

    finish(null);
  }
}
