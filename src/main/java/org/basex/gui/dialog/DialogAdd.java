package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Panel for adding new resources.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
class DialogAdd extends BaseXBack {
  /** Dialog reference. */
  final DialogProps dialog;
  /** Target path. */
  final BaseXTextField target;

  /** General options. */
  private final DialogImport general;
  /** Add button. */
  private final BaseXButton add;
  /** Optimize button. */
  private final BaseXButton optimize;

  /**
   * Constructor.
   * @param d dialog reference
   */
  public DialogAdd(final DialogProps d) {
    dialog = d;
    setLayout(new BorderLayout());

    add(new BaseXLabel(ADD_RESOURCES).large().border(0,  0, 16, 0), BorderLayout.NORTH);

    target = new BaseXTextField("/", d);
    target.addKeyListener(d.keys);

    final BaseXBack pnl = new BaseXBack(new TableLayout(2, 1));
    pnl.add(new BaseXLabel(TARGET_PATH + COLS, true, true).border(8, 0, 6, 0));
    pnl.add(target);

    // option panels
    final BaseXTabs tabs = new BaseXTabs(d);
    final DialogParsing parsing = new DialogParsing(d, tabs);
    general = new DialogImport(d, pnl, parsing);

    tabs.addTab(GENERAL, general);
    tabs.addTab(PARSING, parsing);
    add(tabs, BorderLayout.CENTER);

    // buttons
    add = new BaseXButton(ADD + DOTS, d);
    optimize = new BaseXButton(OPTIMIZE + DOTS, d);

    add(d.newButtons(add, optimize), BorderLayout.SOUTH);
    action(general.parser);
  }

  /**
   * Reacts on user input.
   * @param comp the action component
   */
  void action(final Object comp) {
    final String src = general.input();
    final String trg = target.getText().trim();

    if(comp == add) {
      general.setOptions();
      final Runnable run = new Runnable() {
        @Override
        public void run() {
          dialog.resources.refreshNewFolder(trg);
        }
      };
      DialogProgress.execute(dialog, "", run, new Add(trg, src));

    } else if (comp == optimize) {
      DialogProgress.execute(dialog, "", new Optimize());

    } else {
      boolean ok = general.action(comp, false);
      if(comp == general.browse || comp == general.input)
        target.setText(general.dbname);

      String inf = !ok ? RESOURCE_NOT_FOUND : !ok ? ENTER_DB_NAME : null;
      final Msg icon = Msg.ERROR;
      if(ok) {
        // check if target path is valid
        ok = MetaData.normPath(trg) != null;
        if(!ok) inf = Util.info(INVALID_X, TARGET_PATH);
      }
      general.info.setText(inf, icon);
      add.setEnabled(ok);
      optimize.setEnabled(!dialog.gui.context.data().meta.uptodate);
    }
  }
}
