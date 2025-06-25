package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.GUIConstants.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Panel for adding new resources.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
final class DialogPut extends BaseXBack {
  /** Dialog reference. */
  private final DialogProps dialog;
  /** Target path. */
  final BaseXTextField target;

  /** General options. */
  private final DialogImport general;
  /** Add button. */
  private final BaseXButton put;

  /**
   * Constructor.
   * @param dialog dialog reference
   */
  DialogPut(final DialogProps dialog) {
    this.dialog = dialog;
    setLayout(new BorderLayout());

    add(new BaseXLabel(PUT_RESOURCES).large().border(0, 0, 16, 0), BorderLayout.NORTH);

    target = new BaseXTextField(dialog, "/");

    final BaseXBack pnl = new BaseXBack(new RowLayout());
    pnl.add(new BaseXLabel(TARGET_PATH + COLS, true, true).border(8, 0, 6, 0));
    pnl.add(target);

    // option panels
    final BaseXTabs tabs = new BaseXTabs(dialog);
    final DialogParsing parsing = new DialogParsing(dialog, tabs);
    general = new DialogImport(dialog, pnl, parsing);

    tabs.addTab(GENERAL, general);
    tabs.addTab(PARSING, parsing);
    add(tabs, BorderLayout.CENTER);

    // buttons
    put = new BaseXButton(dialog, PUT + DOTS);

    add(dialog.newButtons(put), BorderLayout.SOUTH);
    action(general.parsers);
  }

  /**
   * Reacts on user input.
   * @param comp the action component
   */
  void action(final Object comp) {
    final String src = general.input();
    final String trg = target.getText().trim();

    if(comp == put) {
      general.setOptions();
      final Runnable run = () -> dialog.resources.refreshNewFolder(trg);
      DialogProgress.execute(dialog, run, new Put(trg, src));
    } else {
      boolean ok = general.action(comp, false);
      if(comp == general.browse || comp == general.input) target.setText(general.dbName);

      final String inf;
      if(ok) {
        // check if target path is valid
        ok = MetaData.normPath(trg) != null;
        inf = ok ? null : Util.info(INVALID_X, TARGET_PATH);
      } else {
        inf = RES_NOT_FOUND;
      }
      general.info.setText(inf, Msg.ERROR);
      put.setEnabled(ok);
    }
  }
}
