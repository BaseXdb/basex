package org.basex.data;

import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import java.io.IOException;

import org.basex.index.FTTokenizer;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * This is an interface for serializing XML results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XMLSerializer extends Serializer {
  /** Indentation. */
  public static final String INDENT = "  ";

  /** Output stream. */
  public final PrintOutput out;
  /** Pretty printing flag. */
  private final boolean pretty;
  /** XML output flag. */
  private final boolean xml;
  /** Indent flag. */
  private boolean indent;

  /**
   * Constructor.
   * @param o output stream
   * @throws IOException exception
   */
  public XMLSerializer(final PrintOutput o) throws IOException {
    this(o, false, false);
  }

  /**
   * Constructor.
   * @param o output stream
   * @param x xml output
   * @param p pretty printing
   * @throws IOException exception
   */
  public XMLSerializer(final PrintOutput o, final boolean x, final boolean p)
      throws IOException {
    out = o;
    xml = x;
    pretty = p;
    if(xml) {
      // [CG] XML/Serialize: allow different encodings (incl. original one)
      out.println("<?xml version='1.0' encoding='" + UTF8 + "' ?>");
      openElement(RESULTS);
    }
  }

  @Override
  public void close() throws IOException {
    if(xml) closeElement();
  }

  @Override
  public void openResult() throws IOException {
    if(xml) openElement(RESULT);
  }

  @Override
  public void closeResult() throws IOException {
    if(xml) closeElement();
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) throws IOException {
    out.print(' ');
    out.print(n);
    out.print(ATT1);
    for(final byte ch : v) {
      switch(ch) {
        case '&': out.print(E_AMP); break;
        case '>': out.print(E_GT);  break;
        case '<': out.print(E_LT);  break;
        case '"': out.print(E_QU);  break;
        case 0x9: out.print(E_TAB); break;
        case 0xA: out.print(E_NL); break;
        case 0xD: out.print(E_CR); break;
        default:  out.write(ch);
      }
    }
    out.print(ATT2);
  }

  @Override
  public void text(final byte[] b) throws IOException {
    finishElement();
    for(final byte ch : b) ch(ch);
    indent = false;
  }

  @Override
  public void text(final byte[] b, final FTPosData ft, final int[][] ftd)
      throws IOException {

    finishElement();
    int c = -1, pp = 0, wl = 0;
    FTTokenizer ftt = new FTTokenizer(b);
    while(ftt.more()) {
      c++;
      for(int i = wl; i < ftt.p; i++) {
        if(Token.letterOrDigit(b[i]) && pp < ftd[0].length && c == ftd[0][pp]) {
          // write fulltext pointer in front of the token
          // used for coloring the token
          ft.addTextPos(out.size(), ftd[1][pp++]);
        }
        ch(b[i]);
      }
      wl = ftt.p;
    }

    while (wl < b.length) ch(b[wl++]);
    indent = false;
  }

  /**
   * Prints a single character.
   * @param b character to be printed
   * @throws IOException exception
   */
  private void ch(final byte b) throws IOException {
    switch(b) {
      case '&': out.print(E_AMP); break;
      case '>': out.print(E_GT); break;
      case '<': out.print(E_LT); break;
      case 0xD: out.print(E_CR); break;
      default : out.write(b);
    }
  }

  @Override
  public void comment(final byte[] n) throws IOException {
    finishElement();
    if(indent) indent(false);
    out.print(COM1);
    out.print(n);
    out.print(COM2);
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    finishElement();
    if(indent) indent(false);
    out.print(PI1);
    out.print(n);
    out.print(' ');
    out.print(v);
    out.print(PI2);
  }

  @Override
  public void item(final byte[] b) throws IOException {
    finishElement();
    if(indent) out.print(' ');
    for(int l = 0; l < b.length; l++) {
      final byte ch = b[l];
      if((ch & 0xF0) == 0xF0 && (b[l + 1] & 0x30) != 0) {
        final int v = (ch & 0x07) << 18 | (b[++l] & 0x3F) << 12 |
          (b[++l] & 0x3F) << 6 | (b[++l] & 0x3F);
        out.print('&');
        out.print('#');
        out.print(token(v));
        out.print(';');
        continue;
      }
      switch(ch) {
        case '&': out.print(E_AMP); break;
        case '>': out.print(E_GT); break;
        case '<': out.print(E_LT); break;
        case 0xD: out.print(E_CR); break;
        default: out.write(ch);
      }
    }
    indent = true;
  }

  @Override
  public boolean finished() {
    return out.finished();
  }

  @Override
  protected void start(final byte[] t) throws IOException {
    if(indent) indent(false);
    out.print(ELEM1);
    out.print(t);
    indent = pretty;
  }

  @Override
  protected void empty() throws IOException {
    out.print(ELEM4);
  }

  @Override
  protected void finish() throws IOException {
    out.print(ELEM2);
  }

  @Override
  protected void close(final byte[] t) throws IOException {
    if(indent) indent(true);
    out.print(ELEM3);
    out.print(t);
    out.print(ELEM2);
    indent = pretty;
  }

  /**
   * Prints the text declaration to the output stream.
   * @param close close flag
   * @throws IOException in case of problems with the PrintOutput
   */
  private void indent(final boolean close) throws IOException {
    out.println();
    final int s = tags.size + (close ? 1 : 0);
    for(int l = 1; l < s; l++) out.print(INDENT);
  }
}
