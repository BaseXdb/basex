package org.basex.query.func.proc;

import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Process options.
 */
public final class ProcOptions extends Options {
  /** Timeout in seconds. */
  public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
  /** Encoding of result. */
  public static final StringOption ENCODING = new StringOption("encoding", Prop.ENCODING);
  /** Process input. */
  public static final StringOption INPUT = new StringOption("input");
  /** Process directory. */
  public static final StringOption DIR = new StringOption("dir");
}
