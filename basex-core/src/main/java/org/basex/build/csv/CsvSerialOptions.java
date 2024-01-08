package org.basex.build.csv;

import org.basex.util.options.*;

/**
 * Options for serializing CSV data.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CsvSerialOptions extends CsvOptions {
  /** Option: allow pattern. */
  public static final StringOption ALLOW = new StringOption("allow", "");
}
