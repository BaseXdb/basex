package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'get' command and return the value of a database property.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Get extends AGet {
  /**
   * Empty constructor.
   */
  public Get() {
    this(null);
  }

  /**
   * Default constructor.
   * @param key property
   */
  public Get(final Object key) {
    super(Perm.NONE, key == null ? null :
      (key instanceof Object[] ? ((Object[]) key)[0] : key).toString()
    );
  }

  @Override
  protected boolean run() throws IOException {
    if(args[0] == null) {
      // retrieve values of all options
      if(context.user.has(Perm.ADMIN)) {
        out.println(MAIN_OPTIONS + COL);
        for(final String s : mprop) out.println(s + COLS + mprop.get(s));
      }
      out.println(NL + OPTIONS + COL);
      for(final String s : prop) out.println(s + COLS + prop.get(s));
    } else {
      // retrieve value of specific option
      final String key = args[0].toUpperCase(Locale.ENGLISH);
      Object type = prop.get(key);
      if(type == null && context.user.has(Perm.ADMIN)) type = mprop.get(key);
      if(type == null) return error(prop.unknown(key));
      out.println(key + COLS + type);
    }
    return true;
  }

  @Override
  protected boolean databases(final StringList db) {
    return true;
  }
}
