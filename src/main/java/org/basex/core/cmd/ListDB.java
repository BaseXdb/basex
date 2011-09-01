package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.File;
import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Commands.Cmd;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.util.Table;
import org.basex.util.Util;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

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
    table.header.add(INFOTYPE);
    table.header.add(INFODBSIZE);

    try {
      final Data data = Open.open(db, context);
      final IntList il = data.docs(path);
      for(int i = 0, is = il.size(); i < is; i++) {
        final int pre = il.get(i);
        final TokenList tl = new TokenList(2);
        tl.add(data.text(pre, true));
        tl.add(DataText.M_XML);
        tl.add(data.size(pre, Data.DOC));
        table.contents.add(tl);
      }
      // add binary files to list
      final int pref = data.meta.binaries().getPath().length() + 1;
      for(final File f : data.files(path)) {
        final TokenList tl = new TokenList(2);
        tl.add(f.getPath().substring(pref).replace('\\', '/'));
        tl.add(DataText.M_RAW);
        tl.add(f.length());
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
