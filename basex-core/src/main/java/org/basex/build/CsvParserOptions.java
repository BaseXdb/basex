package org.basex.build;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CsvParserOptions extends CsvOptions {
  /** Option: encoding. */
  public static final StringOption ENCODING = new StringOption("encoding");

  /**
   * Constructor.
   */
  public CsvParserOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws BaseXException database exception
   */
  public CsvParserOptions(final String opts) throws BaseXException {
    super(opts);
  }
}
