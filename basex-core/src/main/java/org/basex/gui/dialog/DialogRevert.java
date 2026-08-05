package org.basex.gui.dialog;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.util.*;

/**
 * Dialog window for confirming the restoration of replaced files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DialogRevert extends BaseXDialog {
  /**
   * Default constructor.
   * @param gui reference to the main window
   * @param files number of files to be restored
   */
  public DialogRevert(final GUI gui, final int files) {
    super(gui, Text.INFORMATION);

    panel.setLayout(new BorderLayout());

    final BaseXBack back = new BaseXBack(new RowLayout(8));
    final TextPanel header = new TextPanel(this,
        Util.info(Text.REVERT_FILES_X + ' ' + Text.ARE_YOU_SURE, BaseXLayout.format(files)), false);
    header.setFont(back.getFont());
    back.add(header);
    set(back, BorderLayout.CENTER);

    final BaseXBack bttns = okCancel();
    set(bttns, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(((Container) bttns.getComponent(0)).getComponent(0)::
      requestFocusInWindow);
    finish();
  }
}
