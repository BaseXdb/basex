package org.basex.core.proc;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.Commands.COMMANDS;
import org.basex.core.Commands.INFO;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Database info.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class InfoTable extends AInfo {
  /**
   * Constructor.
   * @param a arguments
   */
  public InfoTable(final String... a) {
    super(DATAREF | PRINTING, a);
  }

  @Override
  protected boolean exec() {
    // evaluate input as number range or xpath
    if(args[0] != null && Token.toInt(args[0]) == Integer.MIN_VALUE) {
      result = query(args[0], null);
      if(result == null) return false;
    }
    return true;
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    final Data data = context.data();

    if(result != null) {
      final Nodes nodes = (Nodes) result;
      tableHeader(out, data);
      for(int i = 0; i < nodes.size; i++) table(out, data, nodes.pre[i]);
    } else {
      int ps = 0;
      int pe = data.size;

      if(args[0] != null) {
        if(args[1] != null) {
          ps = Token.toInt(args[0]);
          pe = Token.toInt(args[1]) + 1;
        } else {
          ps = Token.toInt(args[0]);
          pe = ps + 1;
        }
      }
      table(out, data, Math.max(0, ps), Math.min(data.size, pe));
    }
  }

  /**
   * Prints the specified range of the table.
   * @param out output stream
   * @param data data reference
   * @param ps first node to be printed
   * @param pe last node to be printed
   * @throws IOException build or write error
   */
  public static void table(final PrintOutput out, final Data data,
      final int ps, final int pe) throws IOException {
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
    final int len = Token.numDigits(data.size);
    format(out, Token.token(TABLEHEAD1), len + 1);
    format(out, Token.token(TABLEHEAD2), len + 2);
    format(out, Token.token(TABLEHEAD3), len + 2);
    format(out, Token.token(TABLEHEAD4), 4);
    out.print(TABLEHEAD5);
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
    
    final int len = Token.numDigits(data.size);
    format(out, p, len + 1);
    final int k = data.kind(p);
    format(out, p - data.parent(p, k), len + 2);
    format(out, data.size(p, k), len + 2);
    format(out, data.attSize(p, k), 4);

    if(k == Data.ELEM) out.print(TABLEELEM);
    else if(k == Data.DOC) out.print(TABLEDOC);
    else if(k == Data.TEXT) out.print(TABLETEXT);
    else if(k == Data.COMM) out.print(TABLECOMM);
    else if(k == Data.ATTR) out.print(TABLEATTR);
    else if(k == Data.PI) out.print(TABLEPI);

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
    format(out, Token.token(n), left);
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
    return COMMANDS.INFO.name() + " " + INFO.TABLE + args();
  }
}
