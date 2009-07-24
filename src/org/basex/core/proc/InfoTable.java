package org.basex.core.proc;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'info table' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class InfoTable extends AInfo {
  /**
   * Constructor.
   * @param a arguments
   */
  public InfoTable(final String... a) {
    super(DATAREF | PRINTING, a);
  }

  @Override
  protected boolean exec() {
    // evaluate input as number range or xquery
    if(args[0] != null && toInt(args[0]) == Integer.MIN_VALUE) {
      result = query(args[0], null);
      if(result == null) return false;
    }
    return true;
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    final Data data = context.data();

    if(result != null) {
      final int[] nodes = ((Nodes) result).nodes;
      tableHeader(out, data);
      for(final int n : nodes) table(out, data, n);
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
      table(out, data, ps, pe);
    }
  }

  /**
   * Prints the specified range of the table.
   * @param out output stream
   * @param data data reference
   * @param s first node to be printed
   * @param e last node to be printed
   * @throws IOException build or write error
   */
  private static void table(final PrintOutput out, final Data data,
      final int s, final int e) throws IOException {

    final boolean all = s == 0 && e == data.meta.size;
    data.ns.print(out, numDigits(data.meta.size) + 1, all);
    final int ps = Math.max(0, s);
    final int pe = Math.min(data.meta.size, e);
    tableHeader(out, data);
    for(int p = ps; p < pe; p++) table(out, data, p);
  }

  /**
   * Writes the header for the 'table' command.
   * @param out output stream
   * @param data data reference
   * @throws IOException build or write error
   */
  private static void tableHeader(final PrintOutput out, final Data data)
      throws IOException {
    // write table header
    final int len = Math.max(2, numDigits(data.meta.size));
    format(out, token(TABLEPRE), len + 1);
    format(out, token(TABLEDIST), len + 2);
    format(out, token(TABLESIZE), len + 2);
    format(out, token(TABLEATS), 4);
    format(out, token(TABLENS), 4);
    out.print(TABLEKIND);
  }

  /**
   * Writes the table representation of the database to the specified output
   * stream.
   * @param out output stream
   * @param data data reference
   * @param p node to be printed
   * @throws IOException build or write error
   */
  private static void table(final PrintOutput out, final Data data,
      final int p) throws IOException {

    final int len = Math.max(2, numDigits(data.meta.size));
    format(out, p, len + 1);
    final int k = data.kind(p);
    format(out, p - data.parent(p, k), len + 2);
    format(out, data.size(p, k), len + 2);
    format(out, data.attSize(p, k), 4);
    format(out, data.tagNS(p), 4);
    out.print("  ");
    out.print(TABLEKINDS[k]);

    out.print("  ");
    out.print(k == Data.ELEM ? data.tag(p) : k == Data.ATTR ?
      data.attName(p) : data.text(p));

    if(k == Data.ATTR) {
      out.print(ATT1);
      out.print(data.attValue(p));
      out.print(ATT2);
    }
    out.print(Prop.NL);
  }

  /**
   * Formats an integer value for the table output.
   * @param out output stream
   * @param n number to be formatted
   * @param left size of the table entry
   * @throws IOException build or write error
   */
  private static void format(final PrintOutput out, final int n,
      final int left) throws IOException {
    format(out, token(n), left);
  }

  /**
   * Formats an output string for the table output.
   * @param out output stream
   * @param t text to be formatted
   * @param s size of the table entry
   * @throws IOException build or write error
   */
  private static void format(final PrintOutput out, final byte[] t,
      final int s) throws IOException {
    for(int i = 0; i < s - t.length; i++) out.print(' ');
    out.print(t);
  }

  @Override
  public String toString() {
    return Cmd.INFO.name() + " " + CmdInfo.TABLE + args();
  }
}
