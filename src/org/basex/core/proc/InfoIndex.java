package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.Data;
import org.basex.data.Data.Type;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'info index' command and returns information on the indexes
 * of the currently opened database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class InfoIndex extends AInfo {
  /**
   * Default constructor.
   */
  public InfoIndex() {
    super(DATAREF | User.READ);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    final Data data = context.data;
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
    if(data.meta.pathindex) {
      out.println(INFOPATHINDEX);
      out.println(data.path.info(data));
    }
    return true;
  }

  @Override
  public String toString() {
    return Cmd.INFO + " " + CmdInfo.INDEX;
  }
}
