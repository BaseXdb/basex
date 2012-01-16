package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;

import org.basex.core.cmd.Add;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;

/**
 * Add document dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 */
public final class DialogAdd extends DialogImport {
  /** Directory path. */
  private final BaseXTextField target;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogAdd(final GUI main) {
    super(main, GUIADD);

    final BaseXBack p = new BaseXBack(new TableLayout(11, 2, 8, 0)).border(8);
    init(p);

    p.add(new BaseXLabel(CREATETARGET, true, true).border(8, 0, 4, 0));
    p.add(new BaseXLabel());

    target = new BaseXTextField("/", this);
    target.addKeyListener(keys);
    p.add(target);
    p.add(new BaseXLabel());

    info = new BaseXLabel(" ").border(18, 0, 0, 0);
    p.add(info);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, p);
    tabs.addTab(PARSEINFO, parsing);
    set(tabs, BorderLayout.CENTER);

    action(null);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    action(cmp, false);
  }

  /**
   * Returns the add command to be executed.
   * @return add command
   */
  public Add cmd() {
    return new Add(target.getText().trim(), path.getText().trim());
  }
}
