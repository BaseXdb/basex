package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DialogPrefs extends BaseXDialog {
  /** General preferences. */
  private final DialogGeneralPrefs general;
  /** Editor preferences. */
  private final DialogEditorPrefs editor;
  /** Visualization preferences. */
  private final DialogVisualPrefs visual;
  /** Tabs. */
  private final BaseXTabs tabs;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPrefs(final GUI main) {
    super(main, PREFERENCES, false);

    tabs = new BaseXTabs(this);
    general = new DialogGeneralPrefs(this);
    editor = new DialogEditorPrefs(this);
    visual = new DialogVisualPrefs(this);

    tabs.add(GENERAL, general);
    tabs.add(EDITOR, editor);
    tabs.add(VISUALIZATION, visual);
    tabs.setSelectedIndex(gui.gopts.get(GUIOptions.PREFTAB));

    set(tabs, BorderLayout.CENTER);
    action(null);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    general.action(cmp);
    editor.action();
    visual.action();
    gui.notify.layout();
  }

  @Override
  public void close() {
    cancel();
  }

  @Override
  public void cancel() {
    gui.gopts.set(GUIOptions.PREFTAB, tabs.getSelectedIndex());
    gui.context.soptions.write();
    super.cancel();
  }
}
