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
 * This class serializes trees as XML.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class XMLSerializer extends Serializer {
  /** Default serialization parameters. */
  static final SerializerProp PROPS = new SerializerProp();
  /** New line. */
  private static final byte[] NL = token(Prop.NL);

  /** HTML: elements with an empty content model. */
  private static final TokenList EMPTIES = new TokenList();
  /** HTML: script elements. */
  private static final TokenList SCRIPTS = new TokenList();
  /** HTML: boolean attributes. */
  private static final TokenSet BOOLEAN = new TokenSet();
  /** HTML: URI attributes. */
  private static final TokenSet URIS = new TokenSet();

  /** CData elements. */
  private final TokenList cdata = new TokenList();
  /** Indentation flag. */
  private final boolean indent;
  /** Format items. */
  private final boolean format;
  /** URI escape flag. */
  private final boolean escape;
  /** Include content type flag. */
  private final boolean content;
  /** URI for wrapped results. */
  private final byte[] wUri;
  /** Prefix for wrapped results. */
  private final byte[] wPre;
  /** Wrapper flag. */
  private final boolean wrap;
  /** Number of spaces to indent. */
  private final int indents;
  /** Tabular character. */
  private final char tab;
  /** Encoding. */
  private final String enc;
  /** XML version. */
  private final String version;
  /** Media type. */
  private final String media;
  /** Serialization method. */
  private final String mth;

  /** Output stream. */
  private final PrintOutput out;
  /** System document type. */
  private String docsys;
  /** Public document type. */
  private String docpub;

  /** Indentation flag (used for formatting). */
  private boolean ind;
  /** Item flag (used for formatting). */
  private boolean item;
  /** Script flag. */
  private boolean script;

  /**
   * Constructor.
   * @param os output stream reference
   * @throws IOException I/O exception
   */
  public XMLSerializer(final OutputStream os) throws IOException {
    this(os, null);
  }

  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param props serialization properties
   * @throws IOException I/O exception
   */
  public XMLSerializer(final OutputStream os, final SerializerProp props)
      throws IOException {

    out = PrintOutput.get(os);
    final SerializerProp p = props == null ? PROPS : props;

    final String m = p.check(S_METHOD, M_XML, M_XHTML, M_HTML, M_TEXT);
    mth = m.equals(M_XML) ? M_XML : m.equals(M_XHTML) ?
        M_XHTML : m.equals(M_HTML) ? M_HTML : M_TEXT;

    version = p.get(S_VERSION).isEmpty() ? mth == M_HTML ? V40 : V10 :
      mth == M_HTML ? p.check(S_VERSION, V40, V401) :
      mth != M_TEXT ? p.check(S_VERSION, V10, V11) : p.get(S_VERSION);

    final String cdse = p.get(S_CDATA_SECTION_ELEMENTS);
    if(!cdse.isEmpty()) {
      for(final String c : cdse.split("\\s+")) {
        if(!c.isEmpty()) cdata.add(token(c));
      }
    }

    final boolean decl = p.check(S_OMIT_XML_DECLARATION, YES, NO).equals(NO);
    final boolean bom  = p.check(S_BYTE_ORDER_MARK, YES, NO).equals(YES);
    final String sa = p.check(S_STANDALONE, YES, NO, OMIT);
    p.check(S_NORMALIZATION_FORM, NFC, NONE);

    final String maps = p.get(S_USE_CHARACTER_MAPS);
    if(!maps.isEmpty()) SERMAP.thrwSerial(maps);

    enc     = normEncoding(p.get(S_ENCODING), null);
    docsys  = p.get(S_DOCTYPE_SYSTEM);
    docpub  = p.get(S_DOCTYPE_PUBLIC);
    media   = p.get(S_MEDIA_TYPE);
    format  = p.check(S_FORMAT, YES, NO).equals(YES);
    indent  = p.check(S_INDENT, YES, NO).equals(YES) && format;
    undecl  = p.check(S_UNDECLARE_PREFIXES, YES, NO).equals(YES);
    escape  = p.check(S_ESCAPE_URI_ATTRIBUTES, YES, NO).equals(YES);
    content = p.check(S_INCLUDE_CONTENT_TYPE, YES, NO).equals(YES);
    indents = Math.max(0, toInt(p.get(S_INDENTS)));
    tab     = p.check(S_TABULATOR, YES, NO).equals(YES) ? '\t' : ' ';
    wPre = token(p.get(S_WRAP_PREFIX));
    wUri = token(p.get(S_WRAP_URI));
    wrap    = wPre.length != 0;

    if(docsys.isEmpty()) {
      docsys = null;
      docpub = null;
    } else if(docpub.isEmpty()) {
      docpub = null;
    }

    if(!supported(enc)) SERENCODING.thrwSerial(enc);
    if(undecl && version.equals(V10)) SERUNDECL.thrwSerial();

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
    if(mth != M_HTML && mth != M_TEXT) {
      if(decl) {
        print(PI_O);
        print(DOCDECL1);
        print(version);
        print(DOCDECL2);
        print(p.get(S_ENCODING));
        if(!sa.equals(OMIT)) {
          print(DOCDECL3);
          print(sa);
        }
        print(ATT2);
        print(PI_C);
        ind = indent;
      } else if(!sa.equals(OMIT) || version.equals(V11) && docsys != null) {
        SERSTAND.thrwSerial();
      }
    }

    // open results element
    if(wrap) {
      openElement(wPre.length != 0 ? concat(wPre, COLON, RESULTS) : RESULTS);
      namespace(wPre, wUri);
    }
  }

  /**
   * Initializes the serializer (resets current indentation).
   */
  public void init() {
    ind = false;
    item = false;
  }

  @Override
  public void cls() throws IOException {
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
    if(mth == M_TEXT) return;
    //if(!inTag) SERTAT.serial();

    print(' ');
    print(n);

    final byte[] tagatt = mth == M_XML || level() == 0 ? EMPTY :
      concat(lc(tags.peek()), COLON, lc(n));

    // don't append value for boolean attributes
    if(mth == M_HTML && BOOLEAN.id(tagatt) != 0) return;
    // escape URI attributes
    final byte[] val = escape && (mth == M_HTML || mth == M_XHTML) &&
        URIS.id(tagatt) != 0 ? escape(v) : v;

    print(ATT1);
    for(int k = 0; k < val.length; k += cl(val, k)) {
      final int ch = cp(val, k);
      if(!format || mth == M_HTML && (ch == '<' || ch == '&' &&
          val[Math.min(k + 1, val.length - 1)] == '{')) {
        print(ch);
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
    if(cdata.size() == 0 || !cdata.contains(tags.peek()) ||
        mth == M_HTML || mth == M_TEXT) {
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
    if(mth == M_TEXT) return;
    if(ind) indent(true);
    print(COMM_O);
    print(n);
    print(COMM_C);
  }

  @Override
  public void finishPi(final byte[] n, final byte[] v) throws IOException {
    if(mth == M_TEXT) return;
    if(ind) indent(true);
    if(mth == M_HTML && contains(v, '>')) SERPI.thrwSerial();
    print(PI_O);
    print(n);
    print(' ');
    print(v);
    print(mth == M_HTML ? ELEM_C : PI_C);
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
  private void ch(final int ch) throws IOException {
    if(ch == '\n') {
      print(NL);
      return;
    }

    if(mth == M_HTML) {
      if(script) {
        print(ch);
        return;
      }
      if(ch < ' ' && ch != '\t' && ch != '\n' && ch != '\r') return;
      if(ch > 0x7F && ch < 0xA0) SERILL.thrwSerial(Integer.toHexString(ch));
      if(ch == 0xA0) {
        print(E_NBSP);
        return;
      }
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
  public boolean finished() {
    return out.finished();
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
    if(mth == M_TEXT) return;

    if(level() == 0 && docsys != null) {
      if(ind) indent(true);
      print(DOCTYPE);
      if(mth == M_HTML) print(M_HTML);
      else print(t);
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

    if(ind) indent(true);
    print(ELEM_O);
    print(t);
    ind = indent;

    if(mth == M_HTML) script = SCRIPTS.contains(lc(t));

    // subsequent content type elements are currently ignored
    if(content && (mth == M_HTML || mth == M_XHTML) && eq(lc(t), HEAD)) {
      emptyElement(META, HTTPEQUIV, CONTTYPE, CONTENT,
          concat(token(media), CHARSET, token(enc)));
    }
  }

  @Override
  protected void finishOpen() throws IOException {
    if(mth != M_TEXT) print(ELEM_C);
  }

  @Override
  protected void finishClose(final boolean empty) throws IOException {
    if(mth == M_TEXT) return;

    boolean close = !empty;
    if(empty) {
      if(mth == M_XML) {
        print(ELEM_SC);
      } else {
        final boolean e = EMPTIES.contains(lc(tag));
        if(mth == M_XHTML && e) {
          print(' ');
          print(ELEM_SC);
        } else {
          print(ELEM_C);
          if(mth == M_HTML && e) return;
          ind = false;
          close = true;
        }
      }
    }

    if(close) {
      if(ind) indent(true);
      print(ELEM_OS);
      print(tag);
      print(ELEM_C);
      ind = indent;
      if(mth == M_HTML) script &= !SCRIPTS.contains(lc(tag));
    }
  }

  /**
   * Prints the text declaration to the output stream.
   * @param close close flag
   * @throws IOException I/O exception
   */
  private void indent(final boolean close) throws IOException {
    if(item) {
      item = false;
    } else {
      print(NL);
      final int ls = (level() - (close ? 0 : 1)) * indents;
      for(int l = 0; l < ls; ++l) print(tab);
    }
  }

  /**
   * Returns a hex entity for the specified character.
   * @param ch character
   * @throws IOException I/O exception
   */
  private void hex(final int ch) throws IOException {
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
  private void print(final byte[] token) throws IOException {
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
  private void print(final int ch) throws IOException {
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
  private void print(final String s) throws IOException {
    // comparison by reference
    if(enc == UTF8) {
      print(token(s));
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
    // script elements
    SCRIPTS.add(token("script"));
    SCRIPTS.add(token("style"));
    // boolean attributes
    BOOLEAN.add(token("area:nohref"));
    BOOLEAN.add(token("button:disabled"));
    BOOLEAN.add(token("dir:compact"));
    BOOLEAN.add(token("dl:compact"));
    BOOLEAN.add(token("frame:noresize"));
    BOOLEAN.add(token("hr:noshade"));
    BOOLEAN.add(token("img:ismap"));
    BOOLEAN.add(token("input:checked"));
    BOOLEAN.add(token("input:disabled"));
    BOOLEAN.add(token("input:readonly"));
    BOOLEAN.add(token("menu:compact"));
    BOOLEAN.add(token("object:declare"));
    BOOLEAN.add(token("ol:compact"));
    BOOLEAN.add(token("optgroup:disabled"));
    BOOLEAN.add(token("option:selected"));
    BOOLEAN.add(token("option:disabled"));
    BOOLEAN.add(token("script:defer"));
    BOOLEAN.add(token("select:multiple"));
    BOOLEAN.add(token("select:disabled"));
    BOOLEAN.add(token("td:nowrap"));
    BOOLEAN.add(token("textarea:disabled"));
    BOOLEAN.add(token("textarea:readonly"));
    BOOLEAN.add(token("th:nowrap"));
    BOOLEAN.add(token("ul:compact"));
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
