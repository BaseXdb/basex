package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;

/**
 * Evaluates the 'info index' command and returns information on the indexes
 * of the currently opened database.
 *
 * @author BaseX Team 2005-13, BSD License
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
    super(true, type != null && type != CmdIndexInfo.NULL ? type.toString() : null);
  }

  @Override
  protected boolean run() throws IOException {
    final Data data = context.data();
    if(args[0] != null) {
      final CmdIndexInfo ci = getOption(CmdIndexInfo.class);
      if(ci == null) return error(UNKNOWN_CMD_X, this);
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

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX);
  }

  /**
   * Prints information on the specified index.
   * @param idx index type
   * @param data data reference
   * @return success flag
   */
  private static byte[] info(final CmdIndexInfo idx, final Data data) {
    switch(idx) {
      case TAG:       return info(ELEMENTS, IndexType.TAG, data, true);
      case ATTNAME:   return info(ATTRIBUTES, IndexType.ATTNAME, data, true);
      case PATH:      return info(PATH_INDEX, IndexType.PATH, data, true);
      case TEXT:      return info(TEXT_INDEX, IndexType.TEXT, data, data.meta.textindex);
      case ATTRIBUTE: return info(ATTRIBUTE_INDEX, IndexType.ATTRIBUTE, data, data.meta.attrindex);
      case FULLTEXT:  return info(FULLTEXT_INDEX, IndexType.FULLTEXT, data, data.meta.ftxtindex);
      default:        return Token.token(LI + NOT_AVAILABLE);
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
  private static byte[] info(final String ds, final IndexType it, final Data data,
      final boolean avl) {

    final TokenBuilder tb = new TokenBuilder(ds).add(NL);
    if(avl) tb.add(data.info(it));
    else tb.add(LI).addExt(NOT_AVAILABLE, it).add(NL);
    return tb.add(NL).finish();
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.INDEX).args();
  }
}
