package org.basex.core.cmd;

import java.io.IOException;

/**
 * Evaluates the 'get' command and return the value of a database property.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Get extends AGet {
  /**
   * Default constructor.
   * @param key property
   */
  public Get(final Object key) {
    super((key instanceof Object[] ? ((Object[]) key)[0] : key).toString());
  }

  @Override
  protected boolean run() throws IOException {
    final String key = args[0].toUpperCase();
    final Object type = prop.get(key);
    if(type == null) return whichKey();
    out.println(key + ": " + type);
    return true;
  }
}
