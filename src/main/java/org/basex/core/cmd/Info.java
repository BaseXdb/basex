package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.AProp;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

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
    super(User.READ);
  }

  @Override
  protected boolean run() throws IOException {
    out.print(info(context));
    return true;
  }

  /**
   * Creates a database information string.
   * @param context database context
   * @return info string
   */
  public static String info(final Context context) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(GENERAL_INFO + NL);
    format(tb, VERSINFO, VERSION);
    if(context.user.perm(User.CREATE)) {
      Performance.gc(3);
      format(tb, USED_MEM, Performance.getMem());
    }
    if(context.user.perm(User.ADMIN)) {
      final AProp prop = context.mprop;
      tb.add(NL + MAIN_OPTIONS + NL);
      for(final String s : prop) format(tb, s, prop.get(s).toString());
    }
    final AProp prop = context.prop;
    tb.add(NL + OPTIONS + NL);
    for(final String s : prop) format(tb, s, prop.get(s).toString());
    return tb.toString();
  }
}
