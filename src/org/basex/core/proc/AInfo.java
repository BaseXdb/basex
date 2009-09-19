package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Process;
import org.basex.util.TokenBuilder;

/**
 * Abstract class for database info.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class AInfo extends Process {
  /**
   * Protected constructor.
   * @param p properties
   * @param a arguments
   */
  protected AInfo(final int p, final String... a) {
    super(p, a);
  }

  /**
   * Formats the specified input.
   * @param tb token builder
   * @param key key
   * @param val value
   * @param i maximum indent
   */
  protected static void format(final TokenBuilder tb, final String key,
      final String val, final int i) {
    tb.add(' ');
    tb.add(key, i);
    tb.add(": " + val + NL);
  }
}
