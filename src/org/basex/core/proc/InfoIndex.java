package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.Data;
import org.basex.index.IndexToken;
import org.basex.io.PrintOutput;

/**
 * Database info.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    out.println(INFOTAGINDEX);
    out.println(data.info(IndexToken.Type.TAG));
    out.println(INFOATNINDEX);
    out.println(data.info(IndexToken.Type.ATN));
    if(data.meta.txtindex) {
      out.println(INFOTEXTINDEX);
      out.println(data.info(IndexToken.Type.TXT));
    }
    if(data.meta.atvindex) {
      out.println(INFOATTRINDEX);
      out.println(data.info(IndexToken.Type.ATV));
    }
    if(data.meta.ftxindex) {
      out.println(INFOFTINDEX);
      out.println(data.info(IndexToken.Type.FTX));
    }
  }
  
  @Override
  public String toString() {
    return Cmd.INFO.name() + " " + CmdInfo.INDEX;
  }
}
