package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IOConstants;
import org.basex.io.PrintOutput;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * Evaluates the 'info' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Info extends XPath {
  /** Info option. */
  public static final String DB = "database";
  /** Info option. */
  public static final String IDX = "index";
  /** Info option. */
  public static final String TBL = "table";
  /** Command type. */
  private String type;

  @Override
  protected boolean exec() {
    type = cmd.nrArgs() != 0 ? cmd.arg(0).toLowerCase() : null;
    if(type == null) return true;

    final int args = cmd.nrArgs();

    if(type.equals(TBL)) {
      if(context.data() == null) return error(DBEMPTY);
      if(args < 2 || Token.toInt(cmd.arg(1)) != Integer.MIN_VALUE) return true;

      // evaluate input as xpath
      cmd.arg(cmd.args().substring(TBL.length() + 1));
      return super.exec();
    }

    if(type.equals(IDX) || type.equals(DB)) {
      return context.data() == null ? error(DBEMPTY) : true;
    }
    throw new IllegalArgumentException();
  }

  @Override
  protected void out(final PrintOutput out) throws Exception {
    if(type == null) general(out);
    else if(type.equals(DB)) db(out);
    else if(type.equals(IDX)) index(out);
    else if(type.equals(TBL)) table(out);
  }

  /**
   * Prints general information.
   * @param out output stream
   * @throws IOException I/O exception
   */
  private void general(final PrintOutput out) throws IOException {
    prop(out, INFOGENERAL, false);
    prop(out, INFODBPATH + Prop.dbpath, true);
    prop(out, INFOMEM + Performance.getMem(), true);
    prop(out, Set.flag(INFOMAINMEM, Prop.mainmem), true);
    prop(out, Set.flag(INFOINFO, Prop.info) +
        (Prop.allInfo ? INFOALL : ""), true);

    prop(out, NL + INFOINDEXES, false);
    prop(out, Set.flag(INFOTXTINDEX, Prop.textindex), true);
    prop(out, Set.flag(INFOATVINDEX, Prop.attrindex), true);
    prop(out, Set.flag(INFOFTINDEX, Prop.ftindex), true);

    prop(out, NL + INFOCREATE, false);
    prop(out, Set.flag(INFOCHOP, Prop.chop), true);
    prop(out, Set.flag(INFOENTITIES, Prop.entity), true);
  }

  /**
   * Prints database information.
   * @param out output stream
   * @throws IOException I/O exception
   */
  private void db(final PrintOutput out) throws IOException {
    Performance.gc(4);

    final Data data = context.data();
    long fl = 0;
    final File file = IOConstants.dbpath(data.meta.dbname);
    if(file.exists()) {
      for(final File f : file.listFiles()) fl += f.length();
    }

    prop(out, INFODB, false);
    prop(out, INFODBNAME + data.meta.dbname, true);
    prop(out, INFODOC + data.meta.filename, true);
    prop(out, INFOTIME + new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").format(
        new Date(data.meta.time)), true);
    if(data.meta.filesize != 0) prop(out, INFODOCSIZE +
        Performance.formatSize(data.meta.filesize), true);
    if(fl != 0) prop(out, INFODBSIZE + Performance.formatSize(fl), true);
    prop(out, INFOENCODING + data.meta.encoding, true);
    prop(out, INFONODES + data.size, true);
    prop(out, INFOHEIGHT + data.meta.height, true);

    prop(out, NL + INFOINDEX, false);
    prop(out, Set.flag(INFOTXTINDEX, data.meta.txtindex), true);
    prop(out, Set.flag(INFOATVINDEX, data.meta.atvindex), true);
    prop(out, Set.flag(INFOFTINDEX, data.meta.ftxindex), true);

    prop(out, NL + INFOCREATE, false);
    prop(out, Set.flag(INFOCHOP, data.meta.chop), true);
    prop(out, Set.flag(INFOENTITIES, data.meta.entity), true);
  }

  /**
   * Adds a single property to the output.
   * @param out output stream
   * @param prop property to be printed
   * @param indent indentation flag
   * @throws IOException I/O exception
   */
  private void prop(final PrintOutput out, final String prop,
      final boolean indent) throws IOException {
    if(indent) out.print("  ");
    out.println(prop);
  }

  /**
   * Prints index information.
   * @param out output stream
   * @throws IOException I/O exception
   */
  private void index(final PrintOutput out) throws IOException {
    context.data().info(out);
  }

  /**
   * Prints table information.
   * @param out output stream
   * @throws Exception exception
   */
  private void table(final PrintOutput out) throws Exception {
    final Data data = context.data();

    if(result != null) {
      if(result instanceof Nodes) {
        final Nodes nodes = (Nodes) result;
        tableHeader(out, data);
        for(int i = 0; i < nodes.size; i++) table(out, data, nodes.pre[i]);
      } else {
        super.out(out);
      }
    } else {
      int ps = 0;
      int pe = data.size;

      if(cmd.nrArgs() != 1) {
        if(cmd.nrArgs() != 2) {
          ps = Math.max(0, Token.toInt(cmd.arg(1)));
          pe = Math.min(data.size, Token.toInt(cmd.arg(2)) + 1);
        } else {
          ps = Math.min(data.size - 1, Math.max(0, Token.toInt(cmd.arg(1))));
          pe = ps + 1;
        }
      }
      tableHeader(out, data);
      for(int p = ps; p < pe; p++) table(out, data, p);
    }
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
    out.print(k == Data.TEXT || k == Data.COMM || k == Data.PI ? data.text(p) :
      k == Data.ATTR ? data.attName(p) : data.tag(p));

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
    int l = s - t.length;
    while(l-- > 0) out.print(' ');
    out.print(t);
  }
}
