package org.basex.query.func.proc;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Process options.
 */
public final class ProcOptions extends Options {
  /** Timeout in seconds. */
  public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
  /** Encoding of result. */
  public static final StringOption ENCODING = CommonOptions.ENCODING;
  /** Input. */
  public static final StringOption INPUT = new StringOption("input");
  /** Directory. */
  public static final StringOption DIR = new StringOption("dir");
  /** Environment variables. If empty, the environment of the current process is inherited. */
  public static final OptionsOption<Options> ENVIRONMENT =
      new OptionsOption<>("environment", new Options());
}
