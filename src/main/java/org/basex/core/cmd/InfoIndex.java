package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdIndexInfo;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.Data;
import org.basex.index.IndexToken.IndexType;

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
  protected boolean run() throws IOException {
    if(args[0] != null) return info(getOption(CmdIndexInfo.class));
    info(CmdIndexInfo.TAG);
    info(CmdIndexInfo.ATTNAME);
    info(CmdIndexInfo.TEXT);
    info(CmdIndexInfo.ATTRIBUTE);
    info(CmdIndexInfo.FULLTEXT);
    info(CmdIndexInfo.PATH);
    return true;
  }

  /**
   * Prints information on the specified index.
   * @param idx index type
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean info(final CmdIndexInfo idx) throws IOException {
    final Data data = context.data;
    switch(idx) {
      case TAG:
        out.println(INFOTAGS);
        out.println(data.info(IndexType.TAG));
        return true;
      case ATTNAME:
        out.println(INFOATTS);
        out.println(data.info(IndexType.ATTN));
        return true;
      case TEXT:
        out.println(INFOTEXTINDEX);
        if(data.meta.txtindex) out.println(data.info(IndexType.TEXT));
        return true;
      case ATTRIBUTE:
        out.println(INFOATTRINDEX);
        if(data.meta.atvindex) out.println(data.info(IndexType.ATTV));
        return true;
      case FULLTEXT:
        out.println(INFOFTINDEX);
        if(data.meta.ftxindex) out.println(data.info(IndexType.FTXT));
        return true;
      case PATH:
        out.println(INFOPATHINDEX);
        if(data.meta.pthindex) out.println(data.info(IndexType.PATH));
        return true;
      default:
        return false;
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.INDEX).args();
  }
}
