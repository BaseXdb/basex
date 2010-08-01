package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Command;

/**
 * Evaluates the 'get' command and return the value of a database property.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Get extends Command {
  /**
   * Default constructor.
   * @param key property
   */
  public Get(final Object key) {
    super(STANDARD, (key instanceof Object[] ?
        ((Object[]) key)[0] : key).toString());
  }

  @Override
  protected boolean run() throws IOException {
    final String key = args[0].toUpperCase();
    final Object type = prop.get(key);
    if(type == null) return error(SETKEY, key);
    out.println(key + ": " + type);
    return true;
  }
}
