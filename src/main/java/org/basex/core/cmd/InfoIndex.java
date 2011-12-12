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
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'info index' command and returns information on the indexes
 * of the currently opened database.
 *
 * @author BaseX Team 2005-11, BSD License
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
    final Data data = context.data();
    if(args[0] != null) {
      final CmdIndexInfo ci = getOption(CmdIndexInfo.class);
      if(ci == null) return error(CMDUNKNOWN, this);
      final byte[] inf = info(ci, data);
      out.print(inf);
      return inf.length != 0;
    }

    final TokenBuilder tb = new TokenBuilder();
    tb.add(info(CmdIndexInfo.TAG, data));
    tb.add(info(CmdIndexInfo.ATTNAME, data));
    tb.add(info(CmdIndexInfo.TEXT, data));
    tb.add(info(CmdIndexInfo.ATTRIBUTE, data));
    tb.add(info(CmdIndexInfo.FULLTEXT, data));
    tb.add(info(CmdIndexInfo.PATH, data));
    out.print(tb.finish());
    return true;
  }

  /**
   * Prints information on the specified index.
   * @param index index type
   * @return success flag
   */
  public static CmdIndexInfo info(final String index) {
    return getOption(index, CmdIndexInfo.class);
  }

  /**
   * Prints information on the specified index.
   * @param idx index type
   * @param data data reference
   * @return success flag
   */
  public static byte[] info(final CmdIndexInfo idx, final Data data) {
    switch(idx) {
      case TAG:       return info(INFOTAGS, IndexType.TAG, data, true);
      case ATTNAME:   return info(INFOATTS, IndexType.ATTNAME, data, true);
      case TEXT:      return info(INFOTEXTINDEX, IndexType.TEXT, data,
          data.meta.textindex);
      case ATTRIBUTE: return info(INFOATTRINDEX, IndexType.ATTRIBUTE, data,
          data.meta.attrindex);
      case FULLTEXT:  return info(INFOFTINDEX, IndexType.FULLTEXT, data,
          data.meta.ftxtindex);
      case PATH:      return info(INFOPATHINDEX, IndexType.PATH, data,
          data.meta.pathindex);
      default:        return Token.token(LI + INDNOTAVL);
    }
  }

  /**
   * Returns the specified index information.
   * @param ds index description
   * @param it index type
   * @param data data reference
   * @param avl states if index is available
   * @return information
   */
  private static byte[] info(final String ds, final IndexType it,
      final Data data, final boolean avl) {

    final TokenBuilder tb = new TokenBuilder(ds).add(NL);
    if(avl) tb.add(data.info(it));
    else tb.add(LI).addExt(INDNOTAVL, it).add(NL);
    return tb.add(NL).finish();
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.INDEX).args();
  }
}
