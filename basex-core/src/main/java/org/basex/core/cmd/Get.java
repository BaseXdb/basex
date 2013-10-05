package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Evaluates the 'get' command and return the value of a database option.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Get extends AGet {
  /**
   * Empty constructor.
   */
  public Get() {
    this((String) null);
  }

  /**
   * Default constructor.
   * @param option option to be found
   */
  public Get(final Option option) {
    this(option.name);
  }

  /**
   * Default constructor.
   * @param key key to be found
   */
  public Get(final String key) {
    super(Perm.NONE, key);
  }

  @Override
  protected boolean run() throws IOException {
    if(args[0] == null) {
      // retrieve values of all options
      if(context.user.has(Perm.ADMIN)) {
        out.println(MAIN_OPTIONS + COL);
        for(final String s : goptions) out.println(s + COLS + goptions.get(s));
      }
      out.println(NL + OPTIONS + COL);
      for(final String s : options) out.println(s + COLS + options.get(s));
    } else {
      // retrieve value of specific option
      final String key = args[0].toUpperCase(Locale.ENGLISH);
      Object type = options.get(key);
      if(type == null && context.user.has(Perm.ADMIN)) type = goptions.get(key);
      if(type == null) return error(options.unknown(key));
      out.println(key + COLS + type);
    }
    return true;
  }
}
