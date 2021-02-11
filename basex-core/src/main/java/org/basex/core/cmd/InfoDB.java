package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Evaluates the 'info database' command and returns information on the
 * currently opened database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InfoDB extends AInfo {
  /**
   * Default constructor.
   */
  public InfoDB() {
    super(true);
  }

  @Override
  protected boolean run() throws IOException {
    out.print(db(context.data().meta, false, true));
    return true;
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }

  /**
   * Creates a database information string.
   * @param meta meta data
   * @param bold header bold header flag
   * @param index add index information
   * @return info string
   */
  public static String db(final MetaData meta, final boolean bold, final boolean index) {
    final TokenBuilder tb = new TokenBuilder();
    final String header = (bold ? new TokenBuilder().bold().add('%').norm().toString() : "%") + NL;
    tb.addExt(header, DB_PROPS);
    info(tb, MetaProp.NAME, meta);
    info(tb, MetaProp.SIZE.name(), Performance.format(meta.dbSize()));
    info(tb, MetaProp.NODES, meta);

    // count number of raw files
    info(tb, MetaProp.DOCUMENTS, meta);
    info(tb, MetaProp.BINARIES, meta);
    info(tb, MetaProp.TIMESTAMP, meta);
    info(tb, MetaProp.UPTODATE, meta);
    if(meta.corrupt) tb.add(' ' + DB_CORRUPT + NL);

    tb.add(NL).addExt(header, RES_PROPS);
    info(tb, MetaProp.INPUTPATH, meta);
    info(tb, MetaProp.INPUTSIZE.name(), Performance.format(meta.inputsize));
    info(tb, MetaProp.INPUTDATE, meta);

    if(index) {
      tb.add(NL).addExt(header, INDEXES);
      if(meta.oldindex()) {
        tb.add(' ' + H_INDEX_FORMAT + NL);
      } else {
        for(final MetaProp prop : MetaProp.VALUES) {
          if(prop.index) info(tb, prop, meta);
        }
      }
    }
    return tb.toString();
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.DB);
  }
}
