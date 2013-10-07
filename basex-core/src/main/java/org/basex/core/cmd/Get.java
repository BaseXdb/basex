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
        for(final Option o : goptions) out.println(o.name + COLS + goptions.get(o));
      }
      out.println(NL + OPTIONS + COL);
      for(final Option o : options) out.println(o.name + COLS + options.get(o));
    } else {
      // retrieve value of specific option
      final String key = args[0].toUpperCase(Locale.ENGLISH);
      Options opts = options;
      Option opt = opts.option(key);
      if(opt == null && context.user.has(Perm.ADMIN)) {
        opts = goptions;
        opt = opts.option(key);
      }
      if(opt == null) return error(options.error(key));
      out.println(opt.name + COLS + opts.get(opt));
    }
    return true;
  }
}
