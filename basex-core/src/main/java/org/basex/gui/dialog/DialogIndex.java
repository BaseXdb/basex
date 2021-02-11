package org.basex.gui.dialog;

import org.basex.gui.layout.*;

/**
 * Index creation dialog.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class DialogIndex extends BaseXBack {
  /** Index names. */
  protected static final String QNAME_INPUT = "name, *:name, Q{uri}*, Q{uri}name, *";
  /** Dialog reference. */
  protected final BaseXDialog dialog;

  /**
   * Constructor.
   * @param dialog dialog reference
   */
  DialogIndex(final BaseXDialog dialog) {
    this.dialog = dialog;
  }

  /**
   * Reacts on user input.
   * @param enabled enabled flag
   */
  abstract void action(boolean enabled);

  /**
   * Sets the chosen options.
   */
  abstract void setOptions();
}
