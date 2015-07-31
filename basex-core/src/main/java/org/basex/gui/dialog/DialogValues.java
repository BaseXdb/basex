package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.util.options.*;

/**
 * Value index creation dialog.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class DialogValues extends DialogIndex {
  /** Names to include. */
  private final BaseXTextField include;
  /** Option. */
  private final StringOption inc;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param text text/attribute flag
   */
  DialogValues(final BaseXDialog dialog, final boolean text) {
    super(dialog);

    layout(new TableLayout(2, 1));

    final MainOptions opts = dialog.gui.context.options;
    add(new BaseXLabel(text ? H_TEXT_INDEX : H_ATTR_INDEX, true, false).border(0, 0, 6, 0));

    inc = text ? MainOptions.TEXTINCLUDE : MainOptions.ATTRINCLUDE;
    include = new BaseXTextField(opts.get(inc), dialog).hint(QNAME_INPUT);
    add(include);
  }

  @Override
  void action(final boolean enabled) {
    include.setEnabled(enabled);
  }

  @Override
  void setOptions() {
    dialog.gui.set(inc, include.getText());
  }
}
