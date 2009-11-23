package org.basex.core.proc;

import static org.basex.core.Text.*;

import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.util.TokenBuilder;

/**
 * Abstract class for database info.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class AInfo extends Proc {
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
   */
  protected static void format(final TokenBuilder tb, final String key,
      final String val) {
    tb.add(' ');
    tb.add(key);
    tb.add(": " + val + Prop.NL);
  }

  /**
   * Returns an info message for the specified flag.
   * @param flag current flag status
   * @return ON/OFF message
   */
  public static String flag(final boolean flag) {
    return flag ? INFOON : INFOOFF;
  }
}
