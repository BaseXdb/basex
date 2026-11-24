package org.basex.query.func.sql;

import org.basex.util.options.*;

/**
 * Statement options.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class StatementOptions extends Options {
  /** Query timeout. */
  public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
  /** Return auto-generated keys instead of the update count. */
  public static final BooleanOption GENERATED_KEYS = new BooleanOption("generated-keys", false);
  /** Represent null values as empty columns instead of omitting them. */
  public static final BooleanOption NULL = new BooleanOption("null", false);
}
