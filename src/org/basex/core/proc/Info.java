package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Nodes;
import org.basex.index.Index;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'info' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Info extends XPath {
  /** Date Format. */
  private static final SimpleDateFormat DATE =
    new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

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
    
    // no argument specified; show general info
    if(type == null) return true;
    // no database instance open...
    if(context.data() == null) return error(DBEMPTY);

    if(type.equals(TBL)) {
      // evaluate input as number range
      if(cmd.nrArgs() < 2 || Token.toInt(cmd.arg(1)) != Integer.MIN_VALUE)
        return true;

      // evaluate input as xpath
      cmd.arg(cmd.args().substring(TBL.length() + 1));
      return super.exec();
    }

    if(type.equals(IDX) || type.equals(DB)) return true;

    // no command argument found...
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
    Performance.gc(4);
    final int l = BaseX.max(new String[] {
        INFODBPATH, INFOMEM, INFOMM, INFOINFO
    });

    final TokenBuilder tb = new TokenBuilder();
    tb.add(INFOGENERAL + NL);
    format(tb, INFODBPATH, Prop.dbpath, true, l);
    format(tb, INFOMEM, Performance.getMem(), true, l);
    format(tb, INFOMM, BaseX.flag(Prop.mainmem), true, l);
    format(tb, INFOINFO, BaseX.flag(Prop.info) +
        (Prop.allInfo ? " (" + INFOALL + ")" : ""), true, l);
    
    tb.add(NL + INFOCREATE + NL);
    format(tb, INFOCHOP, BaseX.flag(Prop.chop), true, 0);
    format(tb, INFOENTITIES, BaseX.flag(Prop.entity), true, 0);

    tb.add(NL + INFOINDEX + NL);
    format(tb, INFOTXTINDEX, BaseX.flag(Prop.textindex), true, 0);
    format(tb, INFOATVINDEX, BaseX.flag(Prop.attrindex), true, 0);
    format(tb, INFOFTINDEX, BaseX.flag(Prop.ftindex) + (Prop.ftindex &&
        Prop.ftfuzzy ? " (" + INFOFZINDEX + ")" : ""), true, 0);
    out.print(tb.finish());
  }

  /**
   * Prints database information.
   * @param out output stream
   * @throws IOException I/O exception
   */
  private void db(final PrintOutput out) throws IOException {
    final Data data = context.data();
    out.print(db(data.meta, data.size, true, true));
  }

  /**
   * Creates a database information string.
   * @param meta meta data
   * @param size database size
   * @param header add header flag
   * @param index add index information
   * @return info string
   */
  public static byte[] db(final MetaData meta, final int size,
      final boolean header, final boolean index) {
    
    final File dir = IO.dbpath(meta.dbname);
    long len = 0;
    for(final File f : dir.listFiles()) len += f.length();

    final TokenBuilder tb = new TokenBuilder();
    final int l = BaseX.max(new String[] {
        INFODBNAME, INFODOC, INFOTIME, INFODOCSIZE, INFODBSIZE,
        INFOENCODING, INFONODES, INFOHEIGHT
    });

    if(header) {
      tb.add(INFODB + NL);
      format(tb, INFODBNAME, meta.dbname, header, l);
    }
    format(tb, INFODOC, meta.file.path(), header, l);
    format(tb, INFOTIME, DATE.format(new Date(meta.time)), header, l);
    format(tb, INFODOCSIZE, meta.filesize != 0 ?
        Performance.formatSize(meta.filesize) : "-", header, l);
    format(tb, INFODBSIZE, Performance.formatSize(len), header, l);
    format(tb, INFOENCODING, meta.encoding, header, l);
    format(tb, INFONODES, Integer.toString(size), header, l);
    format(tb, INFOHEIGHT, Integer.toString(meta.height), header, l);

    tb.add(NL + INFOCREATE + NL);
    format(tb, INFOCHOP, BaseX.flag(meta.chop), true, 0);
    format(tb, INFOENTITIES, BaseX.flag(meta.chop), true, 0);
    
    if(index) {
      tb.add(NL + INFOINDEX + NL);
      if(meta.newindex) {
        tb.add(" " + INDUPDATE + NL);
      } else {
        format(tb, INFOTXTINDEX, BaseX.flag(meta.txtindex), true, 0);
        format(tb, INFOATVINDEX, BaseX.flag(meta.atvindex), true, 0);
        format(tb, INFOFTINDEX, BaseX.flag(meta.ftxindex) + (meta.ftxindex &&
            meta.ftfuzzy ? " (" + INFOFZINDEX + ")" : ""), true, 0);
      }
    }
    return tb.finish();
  }

  /**
   * Formats the specified input.
   * @param tb token builder
   * @param key key
   * @param val value
   * @param h header flag
   * @param i maximum indent
   */
  private static void format(final TokenBuilder tb, final String key,
      final String val, final boolean h, final int i) {
    if(h) tb.add(' ');
    tb.add(key, i);
    tb.add(": " + val + NL);
  }

  /**
   * Prints index information.
   * @param out output stream
   * @throws IOException I/O exception
   */
  private void index(final PrintOutput out) throws IOException {
    final Data data = context.data();
    
    out.println(INFOTAGINDEX);
    out.println(data.info(Index.TYPE.TAG));
    out.println(INFOATNINDEX);
    out.println(data.info(Index.TYPE.ATN));
    if(data.meta.txtindex) {
      out.println(INFOTXTINDEX);
      out.println(data.info(Index.TYPE.TXT));
    }
    if(data.meta.atvindex) {
      out.println(INFOATVINDEX);
      out.println(data.info(Index.TYPE.ATV));
    }
    if(data.meta.ftxindex) {
      out.println(INFOFTINDEX);
      out.println(data.info(Index.TYPE.FTX));
    }
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
    for(int i = 0; i < s - t.length; i++) out.print(' ');
    out.print(t);
  }
}
