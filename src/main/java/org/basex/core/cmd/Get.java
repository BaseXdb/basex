package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;

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
    super(Perm.NONE,
        (key instanceof Object[] ? ((Object[]) key)[0] : key).toString());
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
