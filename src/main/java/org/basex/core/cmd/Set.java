package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.Locale;

import org.basex.core.*;
import org.basex.util.Util;

/**
 * Evaluates the 'set' command and modifies database properties.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Set extends AGet {
  /**
   * Default constructor.
   * @param key property
   * @param value value to set (optional, depending on the property)
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
      String v = prop.set(key, val);
      if(v == null && !context.client()) {
        // disallow modification of database path if any database is opened
        if(key.equals(MainProp.DBPATH[0]) && context.datas.size() > 0) {
          return error(INVALID_VALUE_X_X, key, val);
        }
        v = mprop.set(key, val);
      }
      return v == null ? error(prop.unknown(key)) : info(key + COLS + v);
    } catch(final Exception ex) {
      Util.debug(ex);
      return error(INVALID_VALUE_X_X, key, val);
    }
  }
}
