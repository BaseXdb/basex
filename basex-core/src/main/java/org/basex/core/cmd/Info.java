package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Evaluates the 'info' command and returns general database information.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Info extends AInfo {
  /**
   * Default constructor.
   */
  public Info() {
    super(false);
  }

  @Override
  protected boolean run() throws IOException {
    out.print(info(context));
    return true;
  }

  @Override
  public void addLocks() {
    // no locks needed
  }

  /**
   * Creates a database information string.
   * @param context database context
   * @return info string
   */
  public static String info(final Context context) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(GENERAL_INFO + COL + NL);
    info(tb, VERSINFO, Prop.VERSION);

    final User user = context.user();
    info(tb, USED_MEM, Performance.getMemory());

    if(user.has(Perm.ADMIN)) {
      final StaticOptions sopts = context.soptions;
      tb.add(NL + GLOBAL_OPTIONS + COL + NL);
      for(final Option<?> o : sopts) info(tb, o.name(), sopts.get(o));
    }

    final MainOptions opts = context.options;
    tb.add(NL + LOCAL_OPTIONS + NL);
    for(final Option<?> o : opts) info(tb, o.name(), opts.get(o));
    return tb.toString();
  }
}
