package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.*;

/**
 * Dialog window for displaying information about the project.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DialogAbout extends BaseXDialog {
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogAbout(final GUI main) {
    super(main, ABOUT);

    BaseXBack p = new BaseXBack(new BorderLayout(12, 0));
    p.setBackground(Color.white);
    p.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(10, 10, 15, 22)));

    final BaseXLabel label = new BaseXLabel();
    label.setIcon(BaseXImages.icon("logo"));
    label.setVerticalAlignment(SwingConstants.TOP);

    p.add(label, BorderLayout.WEST);

    final BaseXBack pp = new BaseXBack(Fill.NONE).layout(new TableLayout(17, 1));

    pp.add(new BaseXLabel(TITLE, false, true));
    final BaseXLabel url = new BaseXLabel("<html><u>" + URL + "</u></html>");
    url.setForeground(GUIConstants.BLUE);
    url.setCursor(GUIConstants.CURSORHAND);
    url.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        BaseXDialog.browse(gui, URL);
      }
    });

    pp.add(url);
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(COPYRIGHT));
    pp.add(new BaseXLabel(LICENSE));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(CHIEF_ARCHITECT));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(TEAM1));
    pp.add(new BaseXLabel(TEAM2));
    pp.add(new BaseXLabel(AND_OTHERS));
    pp.add(Box.createVerticalStrut(7));
    final String lang = main.context.globalopts.get(GlobalOptions.LANG);
    pp.add(new BaseXLabel(TRANSLATION + " (" + lang + "): " + DialogGeneralPrefs.creds(lang)));
    p.add(pp, BorderLayout.EAST);
    add(p, BorderLayout.NORTH);

    p = new BaseXBack();
    p.add(newButtons(B_OK));
    add(p, BorderLayout.EAST);

    finish(null);
  }
}
