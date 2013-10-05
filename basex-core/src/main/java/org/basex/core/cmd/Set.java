package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Evaluates the 'set' command and modifies database options.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Set extends AGet {
  /**
   * Default constructor.
   * @param key key to be found (string or option)
   * @param value value to set (optional, depending on the option)
   */
  public Set(final Object key, final Object value) {
    super(Perm.NONE, (key instanceof Object[] ?
        ((Object[]) key)[0] : key).toString(), value == null ? "" : value.toString());
  }

  @Override
  protected boolean run() {
    final String key = args[0].toUpperCase(Locale.ENGLISH);
    final String val = args[1];
    try {
      final String v = options.set(key, val);
      if(v != null) return info(key + COLS + v);

      // retrieve values of all options
      if(context.user.has(Perm.ADMIN) && globalopts.get(key) != null) {
        return error(Text.GLOBAL_OPTION_X, key);
      }
      return error(options.unknown(key));
    } catch(final Exception ex) {
      Util.debug(ex);
      return error(INVALID_VALUE_X_X, key, val);
    }
  }
}
