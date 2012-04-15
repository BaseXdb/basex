package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Util.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
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
  /** Date format. */
  public static final SimpleDateFormat DATE = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  /**
   * Default constructor.
   */
  public InfoDB() {
    super(Perm.READ, true);
  }

  @Override
  protected boolean run() throws IOException {
    final boolean create = context.user.has(Perm.CREATE);
    out.print(db(context.data().meta, false, true, create));
    return true;
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
    format(tb, NAME, meta.name);
    format(tb, SIZE, Performance.format(meta.dbsize()));
    format(tb, NODES, Integer.toString(meta.size));

    // count number of raw files
    final int bin = meta.binaries().descendants().size();
    format(tb, DOCUMENTS, Integer.toString(meta.ndocs));
    format(tb, BINARIES, Integer.toString(bin));
    format(tb, TIMESTAMP, formatDate(new Date(meta.dbtime()), DATE));
    if(meta.corrupt) tb.add(' ' + DB_CORRUPT + NL);

    tb.add(NL).addExt(header, RESOURCE_PROPS);
    if(create && !meta.original.isEmpty())
      format(tb, INPUT_PATH, meta.original);
    if(meta.filesize != 0)
      format(tb, INPUT_SIZE, Performance.format(meta.filesize));
    format(tb, TIMESTAMP, formatDate(new Date(meta.time), DATE));
    format(tb, ENCODING, meta.encoding);
    format(tb, WS_CHOPPING, Util.flag(meta.chop));

    if(index) {
      tb.add(NL).addExt(header, INDEXES);
      if(meta.oldindex) {
        tb.add(' ' + H_INDEX_FORMAT + NL);
      } else {
        format(tb, UP_TO_DATE, String.valueOf(meta.uptodate));
        format(tb, TEXT_INDEX, Util.flag(meta.textindex));
        format(tb, ATTRIBUTE_INDEX, Util.flag(meta.attrindex));
        format(tb, FULLTEXT_INDEX, Util.flag(meta.ftxtindex) +
            (meta.ftxtindex && meta.wildcards ? " (" + WILDCARDS + ')' : ""));
      }
    }
    return tb.toString();
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.DB);
  }
}
