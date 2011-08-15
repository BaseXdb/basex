package org.basex.io.serial;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.Prop;
import org.basex.data.FTPos;
import org.basex.io.out.PrintOutput;
import org.basex.util.TokenBuilder;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTSpan;
import org.basex.util.hash.TokenSet;
import org.basex.util.list.TokenList;

/**
 * This class serializes data to an output stream.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class OutputSerializer extends Serializer {
  /** New line. */
  protected static final byte[] NL = token(Prop.NL);

  /** (X)HTML: elements with an empty content model. */
  protected static final TokenList EMPTIES = new TokenList();
  /** (X)HTML: URI attributes. */
  protected static final TokenSet URIS = new TokenSet();
  /** System document type. */
  protected String docsys;
  /** Public document type. */
  protected String docpub;

  /** Indentation flag (used for formatting). */
  protected boolean ind;
  /** Item flag (used for formatting). */
  protected boolean item;
  /** URI escape flag. */
  protected final boolean escape;
  /** CData elements. */
  protected final TokenList cdata = new TokenList();
  /** Script flag. */
  protected boolean script;
  /** Indentation flag. */
  protected final boolean indent;
  /** Include content type flag. */
  protected final boolean content;
  /** Media type. */
  protected final String media;
  /** Encoding. */
  protected final String enc;

  /** Output stream. */
  private final PrintOutput out;

  // project specific properties

  /** Number of spaces to indent. */
  protected final int indents;
  /** Tabular character. */
  protected final char tab;

  /** Format items. */
  private final boolean format;
  /** URI for wrapped results. */
  private final byte[] wUri;
  /** Prefix for wrapped results. */
  private final byte[] wPre;
  /** Wrapper flag. */
  private final boolean wrap;

  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param props serialization properties
   * @param versions supported versions
   * @throws IOException I/O exception
   */
  OutputSerializer(final OutputStream os, final SerializerProp props,
      final String... versions) throws IOException {

    final SerializerProp p = props == null ? PROPS : props;
    final String ver = p.get(S_VERSION).isEmpty() ?
        versions.length > 0 ? versions[0] : "" : p.check(S_VERSION, versions);

    // project specific properties
    indents = Math.max(0, toInt(p.get(S_INDENTS)));
    tab     = p.check(S_TABULATOR, YES, NO).equals(YES) ? '\t' : ' ';
    wPre    = token(p.get(S_WRAP_PREFIX));
    wUri    = token(p.get(S_WRAP_URI));
    wrap    = wPre.length != 0;
    out     = PrintOutput.get(os);

    final boolean decl = p.check(S_OMIT_XML_DECLARATION, YES, NO).equals(NO);
    final boolean bom  = p.check(S_BYTE_ORDER_MARK, YES, NO).equals(YES);
    final String sa = p.check(S_STANDALONE, YES, NO, OMIT);
    p.check(S_NORMALIZATION_FORM, NFC, NONE);

    final String maps = p.get(S_USE_CHARACTER_MAPS);
    enc     = normEncoding(p.get(S_ENCODING), null);
    docsys  = p.get(S_DOCTYPE_SYSTEM);
    docpub  = p.get(S_DOCTYPE_PUBLIC);
    media   = p.get(S_MEDIA_TYPE);
    format  = p.check(S_FORMAT, YES, NO).equals(YES);
    indent  = p.check(S_INDENT, YES, NO).equals(YES) && format;
    escape  = p.check(S_ESCAPE_URI_ATTRIBUTES, YES, NO).equals(YES);
    content = p.check(S_INCLUDE_CONTENT_TYPE, YES, NO).equals(YES);
    undecl  = p.check(S_UNDECLARE_PREFIXES, YES, NO).equals(YES);

    if(!maps.isEmpty()) SERMAP.thrwSerial(maps);
    if(!supported(enc)) SERENCODING.thrwSerial(enc);

    if(docsys.isEmpty()) {
      docsys = null;
      docpub = null;
    } else if(docpub.isEmpty()) {
      docpub = null;
    }

    // print byte-order-mark
    if(bom) {
      // comparison by reference
      if(enc == UTF8) {
        out.write(0xEF); out.write(0xBB); out.write(0xBF);
      } else if(enc == UTF16LE) {
        out.write(0xFF); out.write(0xFE);
      } else if(enc == UTF16BE) {
        out.write(0xFE); out.write(0xFF);
      }
    }

    // print document declaration
    if(this instanceof XMLSerializer || this instanceof XHTMLSerializer) {
      final String cdse = p.get(S_CDATA_SECTION_ELEMENTS);
      if(!cdse.isEmpty()) {
        for(final String c : cdse.split("\\s+")) {
          if(!c.isEmpty()) cdata.add(token(c));
        }
      }

      if(undecl && ver.equals(V10)) SERUNDECL.thrwSerial();
      if(decl) {
        print(PI_O);
        print(DOCDECL1);
        print(ver);
        print(DOCDECL2);
        print(p.get(S_ENCODING));
        if(!sa.equals(OMIT)) {
          print(DOCDECL3);
          print(sa);
        }
        print(ATT2);
        print(PI_C);
        ind = indent;
      } else if(!sa.equals(OMIT) || !ver.equals(V10) && docsys != null) {
        SERSTAND.thrwSerial();
      }
    }

    // open results element
    if(wrap) {
      openElement(wPre.length != 0 ? concat(wPre, COLON, RESULTS) : RESULTS);
      namespace(wPre, wUri);
    }
  }

  @Override
  public final void reset() {
    ind = false;
    item = false;
  }

  @Override
  public void close() throws IOException {
    if(wrap) closeElement();
    out.flush();
  }

  @Override
  public void openResult() throws IOException {
    if(wrap) {
      openElement(wPre.length != 0 ? concat(wPre, COLON, RESULT) : RESULT);
      ind = false;
    }
  }

  @Override
  public void closeResult() throws IOException {
    if(wrap) closeElement();
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) throws IOException {
    print(' ');
    print(n);
    print(ATT1);
    for(int k = 0; k < v.length; k += cl(v, k)) {
      final int ch = cp(v, k);
      if(!format) {
        ch(ch);
      } else {
        switch(ch) {
          case '"': print(E_QU);  break;
          case 0x9:
          case 0xA: hex(ch); break;
          default:  ch(ch);
        }
      }
    }
    print(ATT2);
  }

  @Override
  public void finishText(final byte[] b) throws IOException {
    if(cdata.size() == 0 || !cdata.contains(tags.peek())) {
      for(int k = 0; k < b.length; k += cl(b, k)) ch(cp(b, k));
    } else {
      print(CDATA_O);
      int c = 0;
      for(int k = 0; k < b.length; k += cl(b, k)) {
        final int ch = cp(b, k);
        if(ch == ']') {
          ++c;
        } else {
          if(c > 1 && ch == '>') {
            print(CDATA_C);
            print(CDATA_O);
          }
          c = 0;
        }
        print(ch);
      }
      print(CDATA_C);
    }
    ind = false;
  }

  @Override
  public void finishText(final byte[] b, final FTPos ftp) throws IOException {
    final FTLexer lex = new FTLexer().sc().init(b);
    while(lex.hasNext()) {
      final FTSpan span = lex.next();
      if(!span.special && ftp.contains(span.pos))
        print((char) TokenBuilder.MARK);
      final byte[] t = span.text;
      for(int k = 0; k < t.length; k += cl(t, k)) ch(cp(t, k));
    }
    ind = false;
  }

  @Override
  public void finishComment(final byte[] n) throws IOException {
    if(ind) indent();
    print(COMM_O);
    print(n);
    print(COMM_C);
  }

  @Override
  public void finishPi(final byte[] n, final byte[] v) throws IOException {
    if(ind) indent();
    print(PI_O);
    print(n);
    print(' ');
    print(v);
    print(PI_C);
  }

  @Override
  public void finishItem(final byte[] b) throws IOException {
    if(ind) print(' ');
    for(int k = 0; k < b.length; k += cl(b, k)) ch(cp(b, k));
    ind = format;
    item = true;
  }

  /**
   * Prints a single character.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  protected void ch(final int ch) throws IOException {
    if(ch == '\n') {
      print(NL);
      return;
    }
    if(!format) {
      print(ch);
    } else if(ch < ' ' && ch != '\t' && ch != '\n' || ch > 0x7F && ch < 0xA0) {
      hex(ch);
    } else {
      switch(ch) {
        case '&': print(E_AMP); break;
        case '>': print(E_GT); break;
        case '<': print(E_LT); break;
        default : print(ch);
      }
    }
  }

  @Override
  public final boolean finished() {
    return out.finished();
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
    doctype(t);
    if(ind) indent();
    print(ELEM_O);
    print(t);
    ind = indent;
  }

  /**
   * Prints the document type declaration.
   * @param dt document type, or {@code null} for html type
   * @throws IOException I/O exception
   */
  protected void doctype(final byte[] dt) throws IOException {
    if(level != 0 || docsys == null) return;
    if(ind) indent();
    print(DOCTYPE);
    if(dt == null) print(M_HTML);
    else print(dt);
    if(docpub != null) {
      print(" " + PUBLIC + " \"" + docpub + "\"");
    } else {
      print(" " + SYSTEM);
    }
    print(" \"" + docsys + "\"");
    print(ELEM_C);
    print(NL);
    docsys = null;
  }

  @Override
  protected void finishOpen() throws IOException {
    print(ELEM_C);
  }

  @Override
  protected void finishEmpty() throws IOException {
    print(ELEM_SC);
  }

  @Override
  protected void finishClose() throws IOException {
    if(ind) indent();
    print(ELEM_OS);
    print(tag);
    print(ELEM_C);
    ind = indent;
  }

  /**
   * Indents the next text.
   * @throws IOException I/O exception
   */
  protected final void indent() throws IOException {
    if(!indent) return;
    if(item) {
      item = false;
    } else {
      print(NL);
      final int ls = level * indents;
      for(int l = 0; l < ls; ++l) print(tab);
    }
  }

  /**
   * Returns a hex entity for the specified character.
   * @param ch character
   * @throws IOException I/O exception
   */
  protected final void hex(final int ch) throws IOException {
    print("&#x");
    print(HEX[ch >> 4]);
    print(HEX[ch & 15]);
    print(';');
  }

  /**
   * Writes a token in the current encoding.
   * @param token token to be printed
   * @throws IOException I/O exception
   */
  protected final void print(final byte[] token) throws IOException {
    // comparison by reference
    if(enc == UTF8) {
      for(final byte b : token) out.write(b);
    } else {
      print(string(token));
    }
  }

  /**
   * Writes a character in the current encoding.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  protected final void print(final int ch) throws IOException {
    // comparison by reference
    if(enc == UTF8) {
      if(ch <= 0x7F) {
        out.write(ch);
      } else if(ch <= 0x7FF) {
        out.write(ch >>  6 & 0x1F | 0xC0);
        out.write(ch >>  0 & 0x3F | 0x80);
      } else if(ch <= 0xFFFF) {
        out.write(ch >> 12 & 0x0F | 0xE0);
        out.write(ch >>  6 & 0x3F | 0x80);
        out.write(ch >>  0 & 0x3F | 0x80);
      } else {
        out.write(ch >> 18 & 0x07 | 0xF0);
        out.write(ch >> 12 & 0x3F | 0x80);
        out.write(ch >>  6 & 0x3F | 0x80);
        out.write(ch >>  0 & 0x3F | 0x80);
      }
    } else {
      print(new TokenBuilder(4).add(ch).toString());
    }
  }

  /**
   * Writes a string in the current encoding.
   * @param s string to be printed
   * @throws IOException I/O exception
   */
  protected final void print(final String s) throws IOException {
    // comparison by reference
    if(enc == UTF8) {
      for(final byte b : token(s)) out.write(b);
    } else if(enc == UTF16BE || enc == UTF16LE) {
      final boolean l = enc == UTF16LE;
      for(int i = 0; i < s.length(); ++i) {
        final char ch = s.charAt(i);
        out.write(l ? ch & 0xFF : ch >>> 8);
        out.write(l ? ch >>> 8 : ch & 0xFF);
      }
    } else {
      out.write(s.getBytes(enc));
    }
  }

  // HTML Serializer: cache elements
  static {
    // elements with an empty content model
    EMPTIES.add(token("area"));
    EMPTIES.add(token("base"));
    EMPTIES.add(token("br"));
    EMPTIES.add(token("col"));
    EMPTIES.add(token("hr"));
    EMPTIES.add(token("img"));
    EMPTIES.add(token("input"));
    EMPTIES.add(token("link"));
    EMPTIES.add(token("meta"));
    EMPTIES.add(token("basefont"));
    EMPTIES.add(token("frame"));
    EMPTIES.add(token("isindex"));
    EMPTIES.add(token("param"));
    // URI attributes
    URIS.add(token("a:href"));
    URIS.add(token("a:name"));
    URIS.add(token("applet:codebase"));
    URIS.add(token("area:href"));
    URIS.add(token("base:href"));
    URIS.add(token("blockquote:cite"));
    URIS.add(token("body:background"));
    URIS.add(token("button:datasrc"));
    URIS.add(token("del:cite"));
    URIS.add(token("div:datasrc"));
    URIS.add(token("form:action"));
    URIS.add(token("frame:longdesc"));
    URIS.add(token("frame:src"));
    URIS.add(token("head:profile"));
    URIS.add(token("iframe:longdesc"));
    URIS.add(token("iframe:src"));
    URIS.add(token("img:longdesc"));
    URIS.add(token("img:src"));
    URIS.add(token("img:usemap"));
    URIS.add(token("input:datasrc"));
    URIS.add(token("input:src"));
    URIS.add(token("input:usemap"));
    URIS.add(token("ins:cite"));
    URIS.add(token("link:href"));
    URIS.add(token("object:archive"));
    URIS.add(token("object:classid"));
    URIS.add(token("object:codebase"));
    URIS.add(token("object:data"));
    URIS.add(token("object:datasrc"));
    URIS.add(token("object:usemap"));
    URIS.add(token("q:cite"));
    URIS.add(token("script:for"));
    URIS.add(token("script:src"));
    URIS.add(token("select:datasrc"));
    URIS.add(token("span:datasrc"));
    URIS.add(token("table:datasrc"));
    URIS.add(token("textarea:datasrc"));
  }
}
