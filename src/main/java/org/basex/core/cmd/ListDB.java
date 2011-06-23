package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.Commands.Cmd;
import org.basex.data.Data;
import org.basex.util.Table;
import org.basex.util.TokenList;
import org.basex.util.Util;

/**
 * Evaluates the 'list' command and shows all documents in a database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ListDB extends Command {
  /**
   * Default constructor.
   * @param path database name and optional path
   */
  public ListDB(final String path) {
    super(STANDARD, path);
  }

  @Override
  protected boolean run() throws IOException {
    final String str = args[0];
    final int s = str.indexOf('/');
    final String db = s == -1 ? str : str.substring(0, s);
    final String path = s == -1 ? "" : str.substring(s + 1);
    if(!validName(db, false)) return error(NAMEINVALID, db);

    final Table table = new Table();
    table.description = INFONDOCS;
    table.header.add(INFOPATH);
    table.header.add(INFONODES);

    try {
      final Data data = Open.open(db, context);
      for(final int pre : data.doc(path)) {
        final TokenList tl = new TokenList(2);
        tl.add(data.text(pre, true));
        tl.add(data.size(pre, Data.DOC));
        table.contents.add(tl);
      }
      Close.close(data, context);
    } catch(final IOException ex) {
      Util.debug(ex);
      final String msg = ex.getMessage();
      return msg.isEmpty() ? error(DBOPENERR, db) : error(msg);
    }
    table.sort();
    out.println(table.finish());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.LIST.toString()).args();
  }
}
