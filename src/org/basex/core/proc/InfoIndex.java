package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;

import org.basex.core.Commands.COMMANDS;
import org.basex.core.Commands.INFO;
import org.basex.data.Data;
import org.basex.index.IndexToken;
import org.basex.io.PrintOutput;

/**
 * Database info.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class InfoIndex extends AInfo {
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
    out.println(data.info(IndexToken.TYPE.TAG));
    out.println(INFOATNINDEX);
    out.println(data.info(IndexToken.TYPE.ATN));
    if(data.meta.txtindex) {
      out.println(INFOTXTINDEX);
      out.println(data.info(IndexToken.TYPE.TXT));
    }
    if(data.meta.atvindex) {
      out.println(INFOATVINDEX);
      out.println(data.info(IndexToken.TYPE.ATV));
    }
    if(data.meta.ftxindex) {
      out.println(INFOFTINDEX);
      out.println(data.info(IndexToken.TYPE.FTX));
    }
  }
  
  @Override
  public String toString() {
    return COMMANDS.INFO.name() + " " + INFO.INDEX + args();
  }
}
