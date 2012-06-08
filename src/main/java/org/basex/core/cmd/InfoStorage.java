package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'info storage' command and returns the table representation
 * of the currently opened database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class InfoStorage extends AQuery {
  /**
   * Default constructor.
   * @param arg optional arguments
   */
  public InfoStorage(final String... arg) {
    super(Perm.READ, true, arg);
  }

  @Override
  protected boolean run() throws IOException {
    // get arguments
    final String start = args.length > 0 ? args[0] : null;
    final String end = args.length > 1 ? args[1] : null;

    // evaluate input as number range or xquery
    if(start != null && toInt(start) == Integer.MIN_VALUE) {
      queryNodes();
      if(result == null) return false;
    }

    final Data data = context.data();
    if(result != null) {
      final Table table = th();
      for(final int n : ((Nodes) result).pres) table(table, data, n);
      out.print(table.finish());
      result = null;
    } else {
      int ps = 0;
      int pe = 1000;

      if(start != null) {
        if(end != null) {
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

  /**
   * Prints the specified range of the table.
   * @param data data reference
   * @param s first node to be printed
   * @param e last node to be printed
   * @return table
   */
  public static byte[] table(final Data data, final int s, final int e) {
    final TokenBuilder tb = new TokenBuilder();
    final int ps = Math.max(0, s);
    final int pe = Math.min(data.meta.size, e);
    final Table table = th();
    for(int p = ps; p < pe; ++p) table(table, data, p);
    tb.add(table.finish());

    final byte[] ns = data.nspaces.table(ps, pe);
    if(ns.length != 0)
      tb.add(NL).add(ns).add(data.nspaces.toString(ps, pe)).add(NL);
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
    t.header.add(TABLENS);
    t.header.add(TABLEKND);
    t.header.add(TABLECON);
    for(int i = 0; i < 6; ++i) t.align.add(true);
    return t;
  }

  /**
   * Writes the entry for the specified pre value to the table.
   * @param t table reference
   * @param data data reference
   * @param p node to be printed
   */
  private static void table(final Table t, final Data data, final int p) {
    final int k = data.kind(p);
    final TokenList tl = new TokenList();
    tl.add(p);
    tl.add(p - data.parent(p, k));
    tl.add(data.size(p, k));
    tl.add(data.attSize(p, k));
    final int u = data.uri(p, k);
    if(data.nsFlag(p)) tl.add("+" + u);
    else tl.add(u);
    tl.add(TABLEKINDS[k]);

    final byte[] cont;
    if(k == Data.ELEM) {
      cont = data.name(p, k);
    } else if(k == Data.ATTR) {
      cont = new TokenBuilder(data.name(p, k)).add(ATT1).add(
          data.text(p, false)).add(ATT2).finish();
    } else {
      cont = data.text(p, true);
    }
    tl.add(replace(chop(cont, 64), '\n', ' '));
    t.contents.add(tl);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.INFO + " " + CmdInfo.STORAGE);
    if(args.length > 0 && args[0] != null && toInt(args[0]) ==
      Integer.MIN_VALUE) {
      cb.xquery(0);
    } else {
      cb.arg(0).arg(1);
    }
  }
}
