package org.basex.data;

import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.util.TokenBuilder;

/**
 * This is an interface for serializing XML results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XMLSerializer extends Serializer {
  /** Ampersand Entity. */
  private static final byte[] E_AMP = token("&amp;");
  /** Quote Entity. */
  private static final byte[] E_QU = token("&quot;");
  /** GreaterThan Entity. */
  private static final byte[] E_GT = token("&gt;");
  /** LessThan Entity. */
  private static final byte[] E_LT = token("&lt;");
  /** Tab Entity. */
  private static final byte[] E_TAB = token("&#x9;");
  /** NewLine Entity. */
  private static final byte[] E_NL = token("&#xA;");
  /** CarriageReturn Entity. */
  private static final byte[] E_CR = token("&#xD;");
  /** Indentation. */
  private static final String INDENT = "  ";

  /** Output stream. */
  public final PrintOutput out;
  /** Pretty printing flag. */
  private final boolean pretty;
  /** Indent flag. */
  private boolean indent;
  /** Current level. */
  private int level;

  /**
   * Constructor.
   * @param o output stream
   */
  public XMLSerializer(final PrintOutput o) {
    this(o, false, false);
  }

  /**
   * Constructor.
   * @param o output stream
   * @param x xml output 
   * @param p pretty printing
   */
  public XMLSerializer(final PrintOutput o, final boolean x, 
      final boolean p) {
    out = o;
    xml = x;
    pretty = p;
  }
  
  @Override
  public void open(final int size) throws IOException {
    if(xml) {
      // [CG] XML/Serialize: convert back to original/specified encoding
      // (data.meta.encoding)
      out.println("<?xml version='1.0' encoding='" + UTF8 + "' ?>");
      startElement(RESULTS);
      if(size == 0) emptyElement();
      else finishElement();
    }
  }

  @Override
  public void close(final int s) throws IOException {
    if(xml && s != 0) closeElement(RESULTS);
  }

  @Override
  public void openResult() throws Exception {
    if(xml) openElement(RESULT);
  }

  @Override
  public void closeResult() throws IOException {
    if(xml) closeElement(RESULT);
  }

  @Override
  public void startElement(final byte[] t) throws IOException {
    if(indent) indent();
    out.print(ELEM1);
    out.print(t);
    indent = pretty;
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
  public void emptyElement() throws IOException {
    out.print(ELEM4);
  }

  @Override
  public void finishElement() throws IOException {
    out.print(ELEM2);
    level++;
  }

  @Override
  public void closeElement(final byte[] t) throws IOException {
    level--;
    if(indent) indent();
    out.print(ELEM3);
    out.print(t);
    out.print(ELEM2);
    indent = pretty;
  }

  @Override
  public void text(final byte[] b) throws IOException {
    for(final byte ch : b) {
      switch(ch) {
        case '&': out.print(E_AMP); break;
        case '>': out.print(E_GT); break;
        case '<': out.print(E_LT); break;
        case 0xD: out.print(E_CR); break;
        default: out.write(ch);
      }
    }
    indent = false;
  }

  @Override
  public void comment(final byte[] n) throws IOException {
    if(indent) indent();
    out.print(COM1);
    out.print(n);
    out.print(COM2);
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    if(indent) indent();
    out.print(PI1);
    out.print(n);
    out.print(' ');
    out.print(v);
    out.print(PI2);
  }

  @Override
  public void item(final byte[] b) throws IOException {
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

  /**
   * Prints the text declaration to the output stream.
   * @throws IOException in case of problems with the PrintOutput
   */
  public void indent() throws IOException {
    out.println();
    for(int l = 0; l < level; l++) out.print(INDENT);
  }
  
  /**
   * Returns the content of the current node.
   * @param data data reference
   * @param p pre value
   * @param s short representation
   * @return string representation
   */
  public static byte[] content(final Data data, final int p, final boolean s) {
    final int kind = data.kind(p);
    if(kind == Data.ELEM || kind == Data.DOC) {
      return data.tag(p);
    } else if(kind == Data.TEXT) {
      return s ? TEXT : data.text(p);
    } else if(kind == Data.COMM) {
      return s ? COMM : concat(COM1, data.text(p), COM2);
    } else if(kind == Data.PI) {
      return s ? PI : concat(PI1, data.text(p), PI2);
    }
    final TokenBuilder tb = new TokenBuilder();
    tb.add(ATT);
    tb.add(data.attName(p));
    if(!s) {
      tb.add(ATT1);
      tb.add(data.attValue(p));
      tb.add(ATT2);
    }
    return tb.finish();
  }
}
