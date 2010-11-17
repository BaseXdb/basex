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
    final Data data = context.data;
    if(args[0] != null) {
      final byte[] info = info(getOption(CmdIndexInfo.class), data);
      out.print(info);
      return info.length != 0;
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
   * @param data data reference
   * @return success flag
   */
  public static byte[] info(final String index, final Data data) {
    final CmdIndexInfo type = getOption(index, CmdIndexInfo.class);
    return type != null ? info(type, data) : Token.EMPTY;
  }

  /**
   * Prints information on the specified index.
   * @param idx index type
   * @param data data reference
   * @return success flag
   */
  private static byte[] info(final CmdIndexInfo idx, final Data data) {
    switch(idx) {
      case TAG:
        return info(INFOTAGS, IndexType.TAG, data);
      case ATTNAME:
        return info(INFOATTS, IndexType.ATTNAME, data);
      case TEXT:
        return !data.meta.textindex ? Token.EMPTY :
          info(INFOTEXTINDEX, IndexType.TEXT, data);
      case ATTRIBUTE:
        return !data.meta.attrindex ? Token.EMPTY :
          info(INFOATTRINDEX, IndexType.ATTRIBUTE, data);
      case FULLTEXT:
        return !data.meta.ftindex ? Token.EMPTY :
          info(INFOFTINDEX, IndexType.FULLTEXT, data);
      case PATH:
        return !data.meta.pathindex ? Token.EMPTY :
          info(INFOPATHINDEX, IndexType.PATH, data);
      default:
        return Token.EMPTY;
    }
  }

  /**
   * Returns the specified index information.
   * @param ds index description
   * @param it index type
   * @param data data reference
   * @return information
   */
  private static byte[] info(final String ds, final IndexType it,
      final Data data) {
    return new TokenBuilder().add(ds).add(NL).add(
        data.info(it)).add(NL).finish();
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.INDEX).args();
  }
}
