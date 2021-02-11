package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.gui.layout.*;
import org.basex.index.*;
import org.basex.util.options.*;

/**
 * Value index creation dialog.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param type index type
   */
  DialogValues(final BaseXDialog dialog, final IndexType type) {
    super(dialog);

    layout(new RowLayout());

    final MainOptions opts = dialog.gui.context.options;
    final String text = type == IndexType.TOKEN ? H_TOKEN_INDEX : type == IndexType.TEXT
        ? H_TEXT_INDEX : H_ATTR_INDEX;
    add(new BaseXLabel(text, true, false).border(0, 0, 8, 0));

    inc = type == IndexType.TOKEN ? MainOptions.TOKENINCLUDE : type == IndexType.TEXT
        ? MainOptions.TEXTINCLUDE : MainOptions.ATTRINCLUDE;
    include = new BaseXTextField(dialog, opts.get(inc)).hint(QNAME_INPUT);
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
