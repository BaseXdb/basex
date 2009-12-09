package org.basex.core.proc;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;
import org.basex.util.Table;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * Evaluates the 'info table' command and returns the table representation
 * of the currently opened database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class InfoTable extends AInfo {
  /**
   * Default constructor.
   * @param arg optional arguments
   */
  public InfoTable(final String... arg) {
    super(DATAREF | User.READ, arg);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    // evaluate input as number range or xquery
    if(args[0] != null && toInt(args[0]) == Integer.MIN_VALUE) {
      queryNodes();
      if(result == null) return false;
    }

    final Data data = context.data;
    if(result != null) {
      final Table table = th();
      for(final int n : ((Nodes) result).nodes) table(table, data, n);
      out.print(table.finish());
    } else {
      int ps = 0;
      int pe = data.meta.size;

      if(args[0] != null) {
        if(args[1] != null) {
          ps = toInt(args[0]);
          pe = toInt(args[1]) + 1;
        } else {
          ps = toInt(args[0]);
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
    for(int p = ps; p < pe; p++) table(table, data, p);
    tb.add(table.finish());

    final byte[] ns = data.ns.table(ps, pe);
    if(ns.length != 0) {
      tb.add(NL);
      tb.add(ns);
      tb.add(data.ns.toString(ps, pe));
      tb.add(NL);
    }
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
    for(int i = 0; i < 6; i++) t.align.add(true);
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
    final TokenList sl = new TokenList();
    sl.add(p);
    sl.add(p - data.parent(p, k));
    sl.add(data.size(p, k));
    sl.add(data.attSize(p, k));
    final int u = data.uri(p, k);
    if(data.nsFlag(p)) sl.add("+" + u);
    else sl.add(u);
    sl.add(TABLEKINDS[k]);
    sl.add(replace(chop(k == Data.ELEM ? data.name(p, k) : k != Data.ATTR ?
        data.text(p, true) : concat(data.name(p, k), ATT1,
        data.text(p, false), ATT2), 64), '\n', ' '));
    t.contents.add(sl);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Cmd.INFO + " " + CmdInfo.TABLE);
    if(args[0] != null) sb.append(' ' + args[0]);
    if(args[1] != null) sb.append(' ' + args[1]);
    return sb.toString();
  }
}
