package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
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

    final int l = maxLength(new String[] {
        INFODBPATH, INFOMEM, INFOMAINMEM, INFOINFO
    });

    final TokenBuilder tb = new TokenBuilder();
    tb.add(INFOGENERAL + NL);
    format(tb, INFODBPATH, prop.get(Prop.DBPATH), l);
    format(tb, INFOMEM, Performance.getMem(), l);
    format(tb, INFOINFO, BaseX.flag(prop.is(Prop.INFO)) +
        (prop.is(Prop.ALLINFO) ? " (" + INFOALL + ")" : ""), l);

    tb.add(NL + INFOCREATE + NL);
    format(tb, INFOCHOP, BaseX.flag(prop.is(Prop.CHOP)), 0);
    format(tb, INFOENTITY, BaseX.flag(prop.is(Prop.ENTITY)), 0);

    tb.add(NL + INFOINDEX + NL);
    format(tb, INFOPATHINDEX, BaseX.flag(prop.is(Prop.PATHINDEX)), 0);
    format(tb, INFOTEXTINDEX, BaseX.flag(prop.is(Prop.TEXTINDEX)), 0);
    format(tb, INFOATTRINDEX, BaseX.flag(prop.is(Prop.ATTRINDEX)), 0);
    format(tb, INFOFTINDEX, BaseX.flag(prop.is(Prop.FTINDEX)) +
        (prop.is(Prop.FTINDEX) && prop.is(Prop.FTFUZZY) ?
        " (" + INFOFZINDEX + ")" : ""), 0);
    out.print(tb.finish());
  }
}
