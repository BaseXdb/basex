package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Strings.*;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdInfo;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'info storage' command and returns the table representation
 * of the currently opened database.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class InfoStorage extends AQuery {
  /**
   * Default constructor.
   * @param arg arguments (can be {@code null})
   */
  public InfoStorage(final String... arg) {
    super(Perm.READ, true, arg.length > 0 && arg[0] != null ? arg[0] : "",
                           arg.length > 1 && arg[1] != null ? arg[1] : "");
  }

  @Override
  protected boolean run() throws IOException {
    // get arguments
    final String start = args[0];
    final String end = args[1];

    DBNodes nodes = null;
    if(!start.isEmpty() && toInt(start) == Integer.MIN_VALUE) {
      try {
        // evaluate input as query
        final Value value = qp(args[0], context).value();
        if(value instanceof DBNodes) nodes = (DBNodes) value;
      } catch(final QueryException ex) {
        error(Util.message(ex));
      } finally {
        closeQp();
      }
    }

    final Data data = context.data();
    if(nodes != null) {
      final Table table = th();
      for(final int pre : nodes.pres()) table(table, data, pre);
      out.print(table.finish());
    } else {
      int ps = 0;
      int pe = 1000;

      if(!start.isEmpty()) {
        if(!end.isEmpty()) {
          ps = toInt(start);
          pe = toInt(end) + 1;
        } else {
          ps = toInt(start);
          pe = ps + 1;
        }
      }
      out.print(table(data, ps, pe));
    }
    return true;
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CONTEXT);
  }

  /**
   * Prints the specified range of the table.
   * @param data data reference
   * @param start first node to be printed
   * @param end last node to be printed
   * @return table
   */
  public static byte[] table(final Data data, final int start, final int end) {
    final TokenBuilder tb = new TokenBuilder();
    final int ps = Math.max(0, start);
    final int pe = Math.min(data.meta.size, end);
    final Table table = th();
    for(int p = ps; p < pe; ++p) table(table, data, p);
    tb.add(table.finish());

    final byte[] ns = data.nspaces.table(ps, pe);
    if(ns.length != 0) tb.add(NL).add(ns).add(data.nspaces.toString(ps, pe)).add(NL);
    return tb.finish();
  }

  /**
   * Writes the header for the 'table' command.
   * @return table
   */
  private static Table th() {
    final Table t = new Table();
    t.header.add(TABLEPRE);
    t.header.add(TABLEDIST);
    t.header.add(TABLESIZE);
    t.header.add(TABLEATS);
    t.header.add(TABLEID);
    t.header.add(TABLENS);
    t.header.add(TABLEKND);
    t.header.add(TABLECON);
    for(int i = 0; i < 6; ++i) t.align.add(true);
    return t;
  }

  /**
   * Writes the entry for the specified pre value to the table.
   * @param table table reference
   * @param data data reference
   * @param pre node to be printed
   */
  private static void table(final Table table, final Data data, final int pre) {
    final int k = data.kind(pre);
    final TokenList tl = new TokenList();
    tl.add(pre);
    tl.add(pre - data.parent(pre, k));
    tl.add(data.size(pre, k));
    tl.add(data.attSize(pre, k));
    tl.add(data.id(pre));
    final int uriId = data.uriId(pre, k);
    if(data.nsFlag(pre)) tl.add("+" + uriId);
    else tl.add(uriId);
    tl.add(TABLEKINDS[k]);

    final byte[] cont;
    if(k == Data.ELEM) {
      cont = data.name(pre, Data.ELEM);
    } else if(k == Data.ATTR) {
      cont = new TokenBuilder(data.name(pre, Data.ATTR)).add(ATT1).add(
          data.text(pre, false)).add(ATT2).finish();
    } else {
      cont = data.text(pre, true);
    }
    tl.add(Token.replace(Token.chop(cont, 64), '\n', ' '));
    table.contents.add(tl);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.STORAGE);
    if(!args[0].isEmpty() && toInt(args[0]) == Integer.MIN_VALUE) {
      cb.xquery(0);
    } else {
      cb.arg(0).arg(1);
    }
  }
}
