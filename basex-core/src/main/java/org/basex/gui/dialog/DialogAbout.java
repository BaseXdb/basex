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
 * @author BaseX Team, BSD License
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
    label.setIcon(BaseXImages.icon("logo_large"));
    label.setVerticalAlignment(SwingConstants.TOP);

    p.add(label, BorderLayout.WEST);

    final BaseXBack pp = new BaseXBack(new RowLayout());

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
    pp.add(new BaseXLabel(CHIEF_ARCHITECT, false, true));
    pp.add(new BaseXLabel(AUTHOR));
    pp.add(Box.createVerticalStrut(7));
    pp.add(new BaseXLabel(TEAM, false, true));
    final BaseXBack authors = new BaseXBack(new RowLayout());
    for(final String author : AUTHORS.split(",\\s+")) authors.add(new BaseXLabel("• " + author));
    pp.add(authors);
    pp.add(Box.createVerticalStrut(7));
    final String lang = gui.context.soptions.get(StaticOptions.LANG);
    pp.add(new BaseXLabel(TRANSLATION + " (" + lang + ")", false, true));
    pp.add(new BaseXLabel(DialogGeneralPrefs.credits(lang)));
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
