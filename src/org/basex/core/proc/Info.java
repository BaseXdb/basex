package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'info' command and returns general database information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Info extends AInfo {
  /**
   * Default constructor.
   */
  public Info() {
    super(PRINTING);
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    Performance.gc(1);

    final TokenBuilder tb = new TokenBuilder();
    tb.add(INFOGENERAL + NL);
    format(tb, INFODBPATH, prop.get(Prop.DBPATH));
    format(tb, INFOMEM, Performance.getMem());
    format(tb, INFOINFO, Main.flag(prop.is(Prop.INFO)) +
        (prop.is(Prop.ALLINFO) ? " (" + INFOALL + ")" : ""));

    tb.add(NL + INFOCREATE + NL);
    format(tb, INFOCHOP, Main.flag(prop.is(Prop.CHOP)));
    format(tb, INFOENTITY, Main.flag(prop.is(Prop.ENTITY)));

    tb.add(NL + INFOINDEX + NL);
    format(tb, INFOPATHINDEX, Main.flag(prop.is(Prop.PATHINDEX)));
    format(tb, INFOTEXTINDEX, Main.flag(prop.is(Prop.TEXTINDEX)));
    format(tb, INFOATTRINDEX, Main.flag(prop.is(Prop.ATTRINDEX)));
    format(tb, INFOFTINDEX, Main.flag(prop.is(Prop.FTINDEX)) +
        (prop.is(Prop.FTINDEX) && prop.is(Prop.FTFUZZY) ?
        " (" + INFOFZINDEX + ")" : ""));
    out.print(tb.finish());
  }
}
