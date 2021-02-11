package org.basex.util;

import java.util.function.*;

/**
 * Functional interface for boolean checks. Similar to {@link BooleanSupplier}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
@FunctionalInterface
public interface Check {
  /**
   * Returns the result of a check.
   * @return result of check
   */
  boolean ok();
}
