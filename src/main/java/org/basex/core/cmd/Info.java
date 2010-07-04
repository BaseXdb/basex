package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'info' command and returns general database information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    final TokenBuilder tb = new TokenBuilder();
    tb.add(INFOGENERAL + NL);
    if(context.user.perm(User.CREATE)) {
      Performance.gc(1);
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
    out.print(tb.finish());
    return true;
  }
}
