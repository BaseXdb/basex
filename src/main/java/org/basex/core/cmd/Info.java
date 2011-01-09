package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'info' command and returns general database information.
 *
 * @author BaseX Team 2005-11, ISC License
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
  public static byte[] info(final Context context) {
    final Prop prop = context.prop;
    final TokenBuilder tb = new TokenBuilder();
    tb.add(INFOGENERAL + NL);
    if(context.user.perm(User.CREATE)) {
      Performance.gc(3);
      format(tb, INFODBPATH, prop.get(Prop.DBPATH));
      format(tb, INFOMEM, Performance.getMem());
    }
    tb.add(NL + INFOCREATE + NL);
    format(tb, INFOCHOP, flag(prop.is(Prop.CHOP)));
    format(tb, INFOENTITY, flag(prop.is(Prop.ENTITY)));

    tb.add(NL + INFOINDEX + NL);
    format(tb, INFOPATHINDEX, flag(prop.is(Prop.PATHINDEX)));
    format(tb, INFOTEXTINDEX, flag(prop.is(Prop.TEXTINDEX)));
    format(tb, INFOATTRINDEX, flag(prop.is(Prop.ATTRINDEX)));
    format(tb, INFOFTINDEX, flag(prop.is(Prop.FTINDEX)) +
        (prop.is(Prop.FTINDEX) && prop.is(Prop.WILDCARDS) ?
        " (" + INFOWCINDEX + ")" : ""));
    return tb.finish();
  }
}
