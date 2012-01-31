package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;

import org.basex.core.cmd.Add;
import org.basex.data.MetaData;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.util.Util;

/**
 * Panel for adding new resources.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public class DialogAdd extends BaseXBack {
  /** Directory path. */
  final BaseXTextField target;
  /** Dialog reference. */
  final DialogProps dialog;

  /** Filter button. */
  private final BaseXButton add;
  /** Import options. */
  private final DialogImport options;
  /** Parsing options. */
  private final DialogParsing parsing;

  /**
   * Constructor.
   * @param d dialog reference
   */
  public DialogAdd(final DialogProps d) {
    dialog = d;
    setLayout(new BorderLayout());

    target = new BaseXTextField("/", d);
    target.addKeyListener(d.keys);

    final BaseXBack pnl = new BaseXBack(new TableLayout(2, 1));
    pnl.add(new BaseXLabel(TARGET_PATH + COLS, true, true).border(8, 0, 4, 0));
    pnl.add(target);

    // option panels
    options = new DialogImport(d, pnl);
    parsing = new DialogParsing(d);

    final BaseXTabs tabs = new BaseXTabs(d);
    tabs.addTab(GENERAL, options);
    tabs.addTab(PARSING, parsing);
    add(tabs, BorderLayout.NORTH);

    // buttons
    add = new BaseXButton(ADD + DOTS, d);
    final BaseXBack buttons = new BaseXBack(Fill.NONE);
    buttons.add(add);

    final BaseXBack btn = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    btn.add(buttons, BorderLayout.EAST);
    add(btn, BorderLayout.EAST);
  }

  /**
   * Reacts on user input.
   * @param comp the action component
   */
  void action(final Object comp) {
    final String src = options.input();
    final String trg = target.getText().trim();

    if(comp == add) {
      options.setOptions();
      parsing.setOptions();
      final Thread t = new Thread() {
        @Override
        public void run() {
          dialog.resources.refreshNewFolder(trg);
        }
      };
      DialogProgress.execute(dialog, "", t, new Add(trg, src));
    } else {
      boolean ok = options.action(false);
      parsing.action(comp);

      String inf = !ok ? FILE_NOT_FOUND : !ok ? ENTER_DB_NAME : null;
      final Msg icon = Msg.ERROR;
      if(ok) {
        // check if target path is valid
        ok = MetaData.normPath(trg) != null;
        if(!ok) inf = Util.info(INVALID_X, TARGET_PATH);
      }
      options.info.setText(inf, icon);
      add.setEnabled(ok);
    }
  }
}
