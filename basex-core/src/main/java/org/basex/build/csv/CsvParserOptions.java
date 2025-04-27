package org.basex.build.csv;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Options for parsing and serializing CSV data.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvParserOptions extends CsvOptions {
  /** Option: encoding. */
  public static final StringOption ENCODING = CommonOptions.ENCODING;
  /** Option: skip empty fields. */
  public static final BooleanOption SKIP_EMPTY = new BooleanOption("skip-empty", false);

  /**
   * Default constructor.
   */
  public CsvParserOptions() {
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  public CsvParserOptions(final CsvParserOptions opts) {
    super(opts);
  }
}
