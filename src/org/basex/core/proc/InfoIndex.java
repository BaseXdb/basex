package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.Data;
import org.basex.data.Data.Type;
import org.basex.io.PrintOutput;

/**
 * Database info.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class InfoIndex extends AInfo {
  /**
   * Constructor.
   */
  public InfoIndex() {
    super(DATAREF | PRINTING);
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    final Data data = context.data();
    out.println(INFOTAGS);
    out.println(data.info(Type.TAG));
    out.println(INFOATTS);
    out.println(data.info(Type.ATN));
    if(data.ns.size() != 0) {
      out.println(INFONS);
      out.println(data.ns.info());
    }
    if(data.meta.txtindex) {
      out.println(INFOTEXTINDEX);
      out.println(data.info(Type.TXT));
    }
    if(data.meta.atvindex) {
      out.println(INFOATTRINDEX);
      out.println(data.info(Type.ATV));
    }
    if(data.meta.ftxindex) {
      out.println(INFOFTINDEX);
      out.println(data.info(Type.FTX));
    }
  }

  @Override
  public String toString() {
    return Cmd.INFO.name() + " " + CmdInfo.INDEX;
  }
}
