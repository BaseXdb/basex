package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.MetaData;
import org.basex.index.Resources;
import org.basex.io.MimeTypes;
import org.basex.util.Table;
import org.basex.util.Util;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

/**
 * Evaluates the 'list' command and shows all documents in a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ListDB extends Command {
  /**
   * Default constructor.
   * @param path database name and optional path
   */
  public ListDB(final String path) {
    super(Perm.NONE, path);
  }

  @Override
  protected boolean run() throws IOException {
    final String str = args[0];
    final int s = str.indexOf('/');
    final String db = s == -1 ? str : str.substring(0, s);
    final String path = s == -1 ? "" : str.substring(s + 1);
    if(!MetaData.validName(db, false)) return error(NAME_INVALID_X, db);

    final Table table = new Table();
    table.description = RESOURCES;
    table.header.add(INPUT_PATH);
    table.header.add(TYPE);
    table.header.add(MimeTypes.CONTENT_TYPE);
    table.header.add(SIZE);

    try {
      // add xml documents
      final Data data = Open.open(db, context);
      final Resources res = data.resources;
      final IntList il = res.docs(path);
      for(int i = 0, ds = il.size(); i < ds; i++) {
        final int pre = il.get(i);
        final TokenList tl = new TokenList(3);
        final byte[] file = data.text(pre, true);
        tl.add(file);
        tl.add(DataText.M_XML);
        tl.add(MimeTypes.APP_XML);
        tl.add(data.size(pre, Data.DOC));
        table.contents.add(tl);
      }
      // add binary resources
      for(final byte[] file : res.binaries(path)) {
        final String f = string(file);
        final TokenList tl = new TokenList(3);
        tl.add(file);
        tl.add(DataText.M_RAW);
        tl.add(MimeTypes.get(f));
        tl.add(data.meta.binary(f).length());
        table.contents.add(tl);
      }
      Close.close(data, context);
    } catch(final IOException ex) {
      Util.debug(ex);
      final String msg = ex.getMessage();
      return msg.isEmpty() ? error(DB_NOT_OPENED_X, db) : error(msg);
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
