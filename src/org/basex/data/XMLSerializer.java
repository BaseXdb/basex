package org.basex.data;

import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;
import org.basex.util.TokenBuilder;
import org.basex.util.Tokenizer;

/**
 * This is an interface for serializing XML results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XMLSerializer extends Serializer {
  /** Indentation. */
  private static final byte[] INDENT = { ' ', ' ' };
  /** Encoding. */
  private static final byte[] NL = token(Prop.NL);
  /** Encoding. */
  private String enc = UTF8;
  /** Output stream. */
  private final PrintOutput out;
  /** Pretty printing flag. */
  private final boolean pretty;
  /** XML output flag. */
  private final boolean xml;
  /** Indent flag. */
  private boolean indent;
  /** Item flag. */
  private boolean item;

  /**
   * Constructor.
   * @param o output stream reference
   * @throws IOException I/O exception
   */
  public XMLSerializer(final PrintOutput o) throws IOException {
    this(o, false, true);
  }

  /**
   * Constructor.
   * @param o output stream reference
   * @param x serialize result as well-formed xml
   * @param p pretty print the result
   * @throws IOException I/O exception
   */
  public XMLSerializer(final PrintOutput o, final boolean x, final boolean p)
      throws IOException {
    out = o;
    xml = x;
    pretty = p;
    if(xml) openElement(RESULTS);
  }

  /**
   * Sets the encoding and prints a document declaration.
   * Must be called at the beginning of a serialization.
   * @param e encoding
   * @throws IOException I/O exception
   */
  public void encoding(final String e) throws IOException {
    enc = enc(e);
    print(PI1);
    print(DOCDECL);
    print(enc);
    print('\'');
    print(PI2);
    print(NL);
  }

  /**
   * Doctype declaration.
   * @param t document root element tag
   * @param te external subset
   * @param ti internal subset
   * @throws IOException IOException
   */
  public void doctype(final byte[] t, final byte[] te,
      final byte[] ti) throws IOException {
    print(DOCTYPE);
    print(' ');
    print(t);
    if(te != null) print(" " + string(SYSTEM) + " \"" + string(te) + "\"");
    if(ti != null) print(" \"" + string(ti) + "\"");
    print(ELEM2);
    print(NL);
  }

  @Override
  public void cls() throws IOException {
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
    print(' ');
    print(n);
    print(ATT1);
    for(int k = 0; k < v.length; k += cl(v[k])) {
      final int ch = cp(v, k);
      switch(ch) {
        case '"': print(E_QU);  break;
        case 0x9: print(E_TAB); break;
        case 0xA: print(E_NL); break;
        default:  ch(ch);
      }
    }
    print(ATT2);
  }

  @Override
  public void text(final byte[] b) throws IOException {
    finishElement();
    for(int k = 0; k < b.length; k += cl(b[k])) ch(cp(b, k));
    indent = false;
  }

  @Override
  public void text(final byte[] b, final FTPos ftp) throws IOException {
    finishElement();

    int c = -1, wl = 0;
    final Tokenizer ftt = new Tokenizer(b, null);
    while(ftt.more()) {
      c++;
      for(int i = wl; i < ftt.p; i += cl(b[i])) {
        final int ch = cp(b, i);
        if(ftChar(ch) && ftp.contains(c)) print((char) 0x10);
        ch(ch);
      }
      wl = ftt.p;
    }
    while(wl < b.length) {
      ch(cp(b, wl));
      wl += cl(b[wl]);
    }
    indent = false;
  }

  @Override
  public void comment(final byte[] n) throws IOException {
    finishElement();
    if(indent) indent(true);
    print(COM1);
    print(n);
    print(COM2);
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    finishElement();
    if(indent) indent(true);
    print(PI1);
    print(n);
    print(' ');
    print(v);
    print(PI2);
  }

  @Override
  public void item(final byte[] b) throws IOException {
    finishElement();
    if(indent) print(' ');
    for(int k = 0; k < b.length; k += cl(b[k])) ch(cp(b, k));
    indent = true;
    item = true;
  }

  /**
   * Prints a single character.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  private void ch(final int ch) throws IOException {
    switch(ch) {
      case '&': print(E_AMP); break;
      case '>': print(E_GT); break;
      case '<': print(E_LT); break;
      case 0xD: print(E_CR); break;
      default : print(ch);
    }
  }

  @Override
  public boolean finished() {
    return out.finished();
  }

  @Override
  protected void start(final byte[] t) throws IOException {
    if(indent) indent(false);
    print(ELEM1);
    print(t);
    indent = pretty;
  }

  @Override
  protected void empty() throws IOException {
    print(ELEM4);
  }

  @Override
  protected void finish() throws IOException {
    print(ELEM2);
  }

  @Override
  protected void close(final byte[] t) throws IOException {
    if(indent) indent(true);
    print(ELEM3);
    print(t);
    print(ELEM2);
    indent = pretty;
  }

  /**
   * Prints the text declaration to the output stream.
   * @param close close flag
   * @throws IOException I/O exception
   */
  private void indent(final boolean close) throws IOException {
    if(item) {
      item = false;
      return;
    }
    print(NL);
    final int s = level() + (close ? 1 : 0);
    for(int l = 1; l < s; l++) print(INDENT);
  }

  /**
   * Writes a token in the current encoding.
   * @param token token to be printed
   * @throws IOException I/O exception
   */
  private void print(final byte[] token) throws IOException {
    // comparison by reference
    if(enc == UTF8) {
      out.write(token);
    } else {
      print(string(token));
    }
  }

  /**
   * Writes a character in the current encoding.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  private void print(final int ch) throws IOException {
    if(ch < 0x80) {
      out.write(ch);
    } else if(ch < 0xFFFF) {
      print(String.valueOf((char) ch));
    } else {
      print(new TokenBuilder().addUTF(ch).toString());
    }
  }

  /**
   * Writes a string in the current encoding.
   * @param s string to be printed
   * @throws IOException I/O exception
   */
  private void print(final String s) throws IOException {
    // comparison by reference
    if(enc == UTF8) {
      out.write(token(s));
    } else {
      final boolean le = enc == UTF16LE;
      if(enc == UTF16BE || le) {
        for(int i = 0; i < s.length(); i++) {
          final char ch = s.charAt(i);
          out.write(le ? ch & 0xFF : ch >>> 8);
          out.write(le ? ch >>> 8 : ch & 0xFF);
        }
      } else {
        out.print(s.getBytes(enc));
      }
    }
  }
}
