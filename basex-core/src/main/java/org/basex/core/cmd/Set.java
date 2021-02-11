package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Evaluates the 'set' command and modifies database options.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Set extends AGet {
  /**
   * Default constructor.
   * @param <O> option type
   * @param <V> value type
   * @param option option to be found
   * @param value value to set (optional, depending on the option)
   */
  public <O extends Option<V>, V> Set(final O option, final V value) {
    this(option.name(), value);
  }

  /**
   * Default constructor.
   * @param name name of option
   * @param value value to set (optional, depending on the option)
   */
  public Set(final String name, final Object value) {
    super(name, value == null ? "" : value.toString());
  }

  @Override
  protected boolean run() {
    final String name = args[0].toUpperCase(Locale.ENGLISH), val = args[1];
    Options opts = options;

    // check static options: only "debug" can be changed by admin users
    boolean debug = false;
    if(context.user().has(Perm.ADMIN)) {
      final Option<?> opt = soptions.option(name);
      if(opt == StaticOptions.DEBUG) {
        debug = true;
        opts = soptions;
      } else if(opt != null) {
        return error(GLOBAL_OPTION_X, name);
      }
    }

    try {
      // set value and return info string with new value
      opts.assign(name, val);
      // assign static debugging flag
      if(debug) Prop.debug = opts.get(StaticOptions.DEBUG);
      return info(name + COLS + opts.get(opts.option(name)));
    } catch(final BaseXException ex) {
      Util.debug(ex);
      return error(Util.message(ex));
    }
  }
}
