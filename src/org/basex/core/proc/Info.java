package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'info' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Info extends AInfo {
  /**
   * Constructor.
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
    format(tb, INFODBPATH, Prop.dbpath, l);
    format(tb, INFOMEM, Performance.getMem(), l);
    //format(tb, INFOMM, BaseX.flag(Prop.mainmem), l);
    format(tb, INFOINFO, BaseX.flag(Prop.info) +
        (Prop.allInfo ? " (" + INFOALL + ")" : ""), l);

    tb.add(NL + INFOCREATE + NL);
    format(tb, INFOCHOP, BaseX.flag(Prop.chop), 0);
    format(tb, INFOENTITY, BaseX.flag(Prop.entity), 0);

    tb.add(NL + INFOINDEX + NL);
    format(tb, INFOTEXTINDEX, BaseX.flag(Prop.textindex), 0);
    format(tb, INFOATTRINDEX, BaseX.flag(Prop.attrindex), 0);
    format(tb, INFOFTINDEX, BaseX.flag(Prop.ftindex) + (Prop.ftindex &&
        Prop.ftfuzzy ? " (" + INFOFZINDEX + ")" : ""), 0);
    out.print(tb.finish());
  }
}
