package org.basex.query.func.file;

import org.basex.core.*;
import org.basex.query.value.type.*;
import org.basex.util.options.*;

/**
 * Options for reading text files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ReadTextOptions extends Options {
  /** Encoding option. */
  public static final StringOption ENCODING = new StringOption(CommonOptions.ENCODING, null,
      Types.STRING_ZO);
  /** Fallback option. */
  public static final BooleanOption FALLBACK = new BooleanOption(CommonOptions.FALLBACK, false);
  /** Normalize newlines. */
  public static final BooleanOption NORMALIZE_NEWLINES =
      new BooleanOption(CommonOptions.NORMALIZE_NEWLINES, true);

  @Override
  protected StringOption stringOption() {
    return ENCODING;
  }
}
