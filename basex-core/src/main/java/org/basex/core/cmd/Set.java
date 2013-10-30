package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Evaluates the 'set' command and modifies database options.
 *
 * @author BaseX Team 2005-13, BSD License
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
    // check if the option is a global, read-only option
    if(context.user.has(Perm.ADMIN) && goptions.option(name) != null)
      return error(Text.GLOBAL_OPTION_X, name);

    final Option<?> opt = options.option(name);
    try {
      // set value and return info string with new value
      options.assign(name, val);
      return info(name + COLS + options.get(opt));
    } catch(final BaseXException ex) {
      Util.debug(ex);
      return error(Util.message(ex));
    }
  }
}
