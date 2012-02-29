package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.basex.core.CommandBuilder;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Evaluates the 'info database' command and returns information on the
 * currently opened database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class InfoDB extends AInfo {
  /** Date format. */
  public static final SimpleDateFormat DATE =
    new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  /**
   * Default constructor.
   */
  public InfoDB() {
    super(DATAREF | User.READ);
  }

  @Override
  protected boolean run() throws IOException {
    final boolean create = context.user.perm(User.CREATE);
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
    format(tb, TIMESTAMP, DATE.format(new Date(meta.dbtime())));
    if(meta.corrupt) tb.add(' ' + DB_CORRUPT + NL);

    tb.add(NL).addExt(header, RESOURCE_PROPS);
    if(create && !meta.original.isEmpty())
      format(tb, INPUT_PATH, meta.original);
    if(meta.filesize != 0)
      format(tb, INPUT_SIZE, Performance.format(meta.filesize));
    format(tb, TIMESTAMP, DATE.format(new Date(meta.time)));
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
