package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.util.options.*;

/**
 * Evaluates the 'get' command and return the value of a database option.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ShowOptions extends Command {
  /**
   * Empty constructor.
   */
  public ShowOptions() {
    this((String) null);
  }

  /**
   * Default constructor.
   * @param option option to be found
   */
  public ShowOptions(final Option<?> option) {
    this(option.name());
  }

  /**
   * Default constructor.
   * @param key key to be found (can be {@code null})
   */
  public ShowOptions(final String key) {
    super(Perm.NONE, key != null ? key : "");
  }

  @Override
  protected boolean run() throws IOException {
    if(args[0].isEmpty()) {
      // retrieve values of all options
      if(context.user().has(Perm.ADMIN)) {
        out.println(GLOBAL_OPTIONS + COL);
        for(final Option<?> o : soptions) out.println(o.name() + COLS + soptions.get(o));
      }
      out.println(NL + LOCAL_OPTIONS + COL);
      for(final Option<?> o : options) out.println(o.name() + COLS + options.get(o));
    } else {
      // retrieve value of specific option
      final String name = args[0].toUpperCase(Locale.ENGLISH);
      final Object value = get(name, context);
      if(value == null) return error(context.options.error(name));
      out.println(name + COLS + value);
    }
    return true;
  }

  /**
   * Returns the value of the specified option.
   * @param name name of option
   * @param ctx database context
   * @return value
   */
  public static Object get(final String name, final Context ctx) {
    Options opts = ctx.options;
    Option<?> opt = opts.option(name);
    if(opt == null && ctx.user().has(Perm.ADMIN)) {
      opts = ctx.soptions;
      opt = opts.option(name);
    }
    return opt == null ? null : opts.get(opt);
  }

  @Override
  public void addLocks() {
    // no locks needed
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.OPTIONS).arg(0);
  }
}
