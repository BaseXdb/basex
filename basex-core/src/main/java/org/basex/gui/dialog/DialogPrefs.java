package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class DialogPrefs extends BaseXDialog {
  /** Dialog. */
  private static DialogPrefs dialog;

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
   * @param gui reference to the main window
   */
  private DialogPrefs(final GUI gui) {
    super(gui, PREFERENCES, false);

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
    finish();
  }

  /**
   * Activates the dialog window.
   * @param gui reference to the main window
   */
  public static void show(final GUI gui) {
    if(dialog == null) dialog = new DialogPrefs(gui);
    dialog.visual.update();
    dialog.setVisible(true);
  }

  @Override
  public void action(final Object cmp) {
    // no short-circuiting, do all checks...
    ok = general.action(cmp) & editor.action() & visual.action();
    gui.notify.layout();
  }

  @Override
  public void close() {
    if(ok) cancel();
  }

  @Override
  public void cancel() {
    visual.cancel();
    gui.gopts.set(GUIOptions.PREFTAB, tabs.getSelectedIndex());
    gui.saveOptions();
    super.close();
  }
}
