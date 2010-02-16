package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdIndexInfo;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.Data;
import org.basex.data.Data.IndexType;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'info index' command and returns information on the indexes
 * of the currently opened database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class InfoIndex extends AInfo {
  /**
   * Default constructor.
   */
  public InfoIndex() {
    this(null);
  }

  /**
   * Default constructor.
   * @param type optional index type, defined in {@link CmdIndexInfo}
   */
  public InfoIndex(final Object type) {
    super(DATAREF | User.READ, type != null && type != CmdIndexInfo.NULL ?
        type.toString() : null);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    if(args[0] != null) return info(getOption(CmdIndexInfo.class), out);

    info(CmdIndexInfo.TAG, out);
    info(CmdIndexInfo.ATTNAME, out);
    info(CmdIndexInfo.TEXT, out);
    info(CmdIndexInfo.ATTRIBUTE, out);
    info(CmdIndexInfo.FULLTEXT, out);
    info(CmdIndexInfo.PATH, out);
    return true;
  }

  /**
   * Prints information on the specified index.
   * @param idx index type
   * @param out output stream
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean info(final CmdIndexInfo idx, final PrintOutput out)
      throws IOException {
    
    final Data data = context.data;
    switch(idx) {
      case TAG:
        out.println(INFOTAGS);
        out.println(data.info(IndexType.TAG));
        return true;
      case ATTNAME:
        out.println(INFOATTS);
        out.println(data.info(IndexType.ATN));
        return true;
      case TEXT:
        out.println(INFOTEXTINDEX);
        if(data.meta.txtindex) out.println(data.info(IndexType.TXT));
        return true;
      case ATTRIBUTE:
        out.println(INFOATTRINDEX);
        if(data.meta.atvindex) out.println(data.info(IndexType.ATV));
        return true;
      case FULLTEXT:
        out.println(INFOFTINDEX);
        if(data.meta.ftxindex) out.println(data.info(IndexType.FTX));
        return true;
      case PATH:
        out.println(INFOPATHINDEX);
        if(data.meta.pthindex) out.println(data.info(IndexType.PTH));
        return true;
      default:
        return false;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Cmd.INFO + " " + CmdInfo.INDEX);
    if(args[0] != null) sb.append(" " + args[0]);
    return sb.toString();
  }
}
