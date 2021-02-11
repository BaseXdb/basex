package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;

/**
 * Dialog window for displaying information about the project.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogAbout extends BaseXDialog {
  /** Dialog. */
  private static DialogAbout dialog;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  private DialogAbout(final GUI gui) {
    super(gui, ABOUT, false);

    BaseXBack p = new BaseXBack(new BorderLayout(12, 0));
    p.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
        BaseXLayout.border(10, 10, 15, 22)));

    final BaseXLabel label = new BaseXLabel();
    label.setIcon(BaseXImages.icon("logo_256"));
    label.setVerticalAlignment(SwingConstants.TOP);

    p.add(label, BorderLayout.WEST);

    final BaseXBack pp = new BaseXBack(false).layout(new RowLayout());

    pp.add(new BaseXLabel(TITLE, false, true));
    final BaseXLabel url = new BaseXLabel("<html><u>" + PUBLIC_URL + "</u></html>");
    url.setForeground(GUIConstants.BLUE);
    url.setCursor(GUIConstants.CURSORHAND);
    url.addMouseListener((MouseClickedListener) e -> BaseXDialog.browse(gui, PUBLIC_URL));

    pp.add(url);
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(COPYRIGHT));
    pp.add(new BaseXLabel(LICENSE));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(CHIEF_ARCHITECT + COLS + AUTHOR));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(TEAM + COLS + AUTHORS1 + ','));
    pp.add(new BaseXLabel(AUTHORS2));
    pp.add(new BaseXLabel(AND_OTHERS));
    pp.add(Box.createVerticalStrut(7));
    final String lang = gui.context.soptions.get(StaticOptions.LANG);
    pp.add(new BaseXLabel(TRANSLATION + " (" + lang + "): " + DialogGeneralPrefs.creds(lang)));
    p.add(pp, BorderLayout.EAST);
    add(p, BorderLayout.NORTH);

    p = new BaseXBack();
    p.add(newButtons(B_OK));
    add(p, BorderLayout.EAST);

    finish();
  }

  /**
   * Activates the dialog window.
   * @param gui reference to the main window
   */
  public static void show(final GUI gui) {
    if(dialog == null) dialog = new DialogAbout(gui);
    dialog.setVisible(true);
  }
}
