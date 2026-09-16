package org.basex.query.func.proc;

import static org.basex.query.value.type.Types.*;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Process options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProcOptions extends Options {
  /** Timeout in seconds. */
  public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
  /** Encoding of input and result. */
  public static final StringOption ENCODING =
      new StringOption(CommonOptions.ENCODING, null, STRING_ZO);
  /** Input. */
  public static final ValueOption INPUT = new ValueOption("input", STRING_OR_BINARY_ZO);
  /** Directory. */
  public static final StringOption DIR = new StringOption("dir", null, STRING_ZO);
  /** Environment variables. If empty, the environment of the current process is inherited. */
  public static final OptionsOption<Options> ENVIRONMENT =
      new OptionsOption<>("environment", new Options());
  /** Return output as binary. */
  public static final BooleanOption BINARY = new BooleanOption("binary", false);
  /** Normalize newlines. */
  public static final BooleanOption NORMALIZE_NEWLINES =
      new BooleanOption(CommonOptions.NORMALIZE_NEWLINES, true);
  /** Replace invalid characters. */
  public static final BooleanOption FALLBACK = new BooleanOption(CommonOptions.FALLBACK, false);
}
