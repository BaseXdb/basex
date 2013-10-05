package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Evaluates the 'info' command and returns general database information.
 *
 * @author BaseX Team 2005-12, BSD License
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
  public void databases(final LockResult lr) {
    // No locks needed
  }

  /**
   * Creates a database information string.
   * @param context database context
   * @return info string
   */
  public static String info(final Context context) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(GENERAL_INFO + NL);
    info(tb, VERSINFO, Prop.VERSION);
    if(context.user.has(Perm.CREATE)) {
      Performance.gc(2);
      info(tb, USED_MEM, Performance.getMemory());
    }
    if(context.user.has(Perm.ADMIN)) {
      final GlobalOptions gopts = context.globalopts;
      tb.add(NL + MAIN_OPTIONS + NL);
      for(final String s : gopts) info(tb, s, gopts.get(s));
    }
    final Options opts = context.options;
    tb.add(NL + OPTIONS + NL);
    for(final String s : opts) info(tb, s, opts.get(s));
    return tb.toString();
  }
}
