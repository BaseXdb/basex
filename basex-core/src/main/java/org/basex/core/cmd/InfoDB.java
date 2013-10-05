package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Evaluates the 'info database' command and returns information on the
 * currently opened database.
 *
 * @author BaseX Team 2005-12, BSD License
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
    final boolean create = context.user.has(Perm.CREATE);
    out.print(db(context.data().meta, false, true, create));
    return true;
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX);
  }

  /**
   * Creates a database information string.
   * @param meta meta data
   * @param bold header bold header flag
   * @param index add index information
   * @param create create permissions
   * @return info string
   */
  public static String db(final MetaData meta, final boolean bold,
      final boolean index, final boolean create) {

    final TokenBuilder tb = new TokenBuilder();
    final String header = (bold ?
        new TokenBuilder().bold().add('%').norm().toString() : "%") + NL;
    tb.addExt(header, DB_PROPS);
    info(tb, NAME, meta.name);
    info(tb, SIZE, Performance.format(meta.dbsize()));
    info(tb, NODES, meta.size);

    // count number of raw files
    info(tb, DOCUMENTS, meta.ndocs);
    final int b = meta.path != null ? meta.binaries().descendants().size() : 0;
    info(tb, BINARIES, b);
    info(tb, TIMESTAMP, DateTime.format(new Date(meta.dbtime()), DateTime.DATETIME));
    if(meta.corrupt) tb.add(' ' + DB_CORRUPT + NL);

    tb.add(NL).addExt(header, RES_PROPS);
    if(create && !meta.original.isEmpty()) info(tb, INPUT_PATH, meta.original);
    if(meta.filesize != 0) info(tb, INPUT_SIZE, Performance.format(meta.filesize));
    info(tb, TIMESTAMP, DateTime.format(new Date(meta.time), DateTime.DATETIME));
    info(tb, ENCODING, meta.encoding);
    info(tb, WS_CHOPPING, Util.flag(meta.chop));

    if(index) {
      tb.add(NL).addExt(header, INDEXES);
      if(meta.oldindex()) {
        tb.add(' ' + H_INDEX_FORMAT + NL);
      } else {
        info(tb, UP_TO_DATE, meta.uptodate);
        info(tb, TEXT_INDEX, Util.flag(meta.textindex));
        info(tb, ATTRIBUTE_INDEX, Util.flag(meta.attrindex));
        info(tb, FULLTEXT_INDEX, Util.flag(meta.ftxtindex));
        info(tb, Options.UPDINDEX.key, Util.flag(meta.updindex));
        info(tb, Options.MAXCATS.key, meta.maxcats);
        info(tb, Options.MAXLEN.key, meta.maxlen);
      }
    }
    return tb.toString();
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.DB);
  }
}
