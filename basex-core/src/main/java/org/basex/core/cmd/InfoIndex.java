package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdIndexInfo;
import org.basex.core.parse.Commands.CmdInfo;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;

/**
 * Evaluates the 'info index' command and returns information on the indexes
 * of the currently opened database.
 *
 * @author BaseX Team 2005-21, BSD License
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
    super(true, type != null && type != CmdIndexInfo.NULL ? type.toString() : "");
  }

  @Override
  protected boolean run() throws IOException {
    final Data data = context.data();
    if(!args[0].isEmpty()) {
      final CmdIndexInfo ci = getOption(CmdIndexInfo.class);
      if(ci == null) return error(UNKNOWN_CMD_X, this);
      final byte[] inf = info(ci, data, options);
      out.print(inf);
      return inf.length != 0;
    }

    final TokenBuilder tb = new TokenBuilder();
    tb.add(info(CmdIndexInfo.ELEMNAME, data, options));
    tb.add(info(CmdIndexInfo.ATTRNAME, data, options));
    tb.add(info(CmdIndexInfo.TEXT, data, options));
    tb.add(info(CmdIndexInfo.ATTRIBUTE, data, options));
    tb.add(info(CmdIndexInfo.TOKEN, data, options));
    tb.add(info(CmdIndexInfo.FULLTEXT, data, options));
    tb.add(info(CmdIndexInfo.PATH, data, options));
    out.print(tb.finish());
    return true;
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }

  /**
   * Prints information on the specified index.
   * @param idx index type
   * @param data data reference
   * @param options options
   * @return success flag
   */
  private static byte[] info(final CmdIndexInfo idx, final Data data, final MainOptions options) {
    switch(idx) {
      case ELEMNAME:
        return info(ELEMENTS, IndexType.ELEMNAME, data, options, true);
      case ATTRNAME:
        return info(ATTRIBUTES, IndexType.ATTRNAME, data, options, true);
      case PATH:
        return info(PATH_INDEX, IndexType.PATH, data, options, true);
      case TEXT:
        return info(TEXT_INDEX, IndexType.TEXT, data, options, data.meta.textindex);
      case ATTRIBUTE:
        return info(ATTRIBUTE_INDEX, IndexType.ATTRIBUTE, data, options, data.meta.attrindex);
      case TOKEN:
        return info(TOKEN_INDEX, IndexType.TOKEN, data, options, data.meta.tokenindex);
      case FULLTEXT:
        return info(FULLTEXT_INDEX, IndexType.FULLTEXT, data, options, data.meta.ftindex);
      default:
        return Token.token(LI + NOT_AVAILABLE);
    }
  }

  /**
   * Returns the specified index information.
   * @param desc index description
   * @param it index type
   * @param data data reference
   * @param options options
   * @param avl states if index is available
   * @return information
   */
  private static byte[] info(final String desc, final IndexType it, final Data data,
      final MainOptions options, final boolean avl) {

    final TokenBuilder tb = new TokenBuilder().add(desc).add(NL);
    if(avl) tb.add(data.info(it, options));
    else tb.add(LI).addExt(NOT_AVAILABLE, it).add(NL);
    return tb.add(NL).finish();
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.INDEX).args();
  }
}
