package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.Locale;

import org.basex.core.User;

/**
 * Evaluates the 'get' command and return the value of a database property.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Get extends AGet {
  /**
   * Default constructor.
   * @param key property
   */
  public Get(final Object key) {
    super(User.READ, (key instanceof Object[] ? ((Object[]) key)[0] : key).toString());
  }

  @Override
  protected boolean run() throws IOException {
    final String key = args[0].toUpperCase(Locale.ENGLISH);
    Object type = prop.get(key);
    if(type == null && !context.client()) type = mprop.get(key);
    if(type == null) return error(prop.unknown(key));
    out.println(key + COLS + type);
    return true;
  }
}
