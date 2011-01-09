package org.basex.data;

import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import static org.basex.data.SerializerProp.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.TokenSet;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTSpan;
import org.basex.util.Util;

/**
 * This class serializes trees as XML.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class XMLSerializer extends Serializer {
  /** Default serialization parameters. */
  private static final SerializerProp PROPS = new SerializerProp();
  /** New line. */
  private static final byte[] NL = token(Prop.NL);
  /** Colon. */
  private static final byte[] COL = { ':' };

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
  /** URI escape flag. */
  private final boolean escape;
  /** Include content type flag. */
  private final boolean content;
  /** URI for wrapped results. */
  private final byte[] wrapUri;
  /** Prefix for wrapped results. */
  private final byte[] wrapPre;
  /** Wrapper flag. */
  private final boolean wrap;
  /** Number of spaces to indent. */
  private final int spaces;
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

    for(final String c : p.get(S_CDATA_SECTION_ELEMENTS).split("\\s+"))
      if(!c.isEmpty()) cdata.add(token(c));

    final boolean decl = p.check(S_OMIT_XML_DECLARATION, YES, NO).equals(NO);
    final boolean bom  = p.check(S_BYTE_ORDER_MARK, YES, NO).equals(YES);
    final String sa = p.check(S_STANDALONE, YES, NO, OMIT);
    p.check(S_NORMALIZATION_FORM, NFC, NONE);

    final String maps = p.get(S_USE_CHARACTER_MAPS);
    if(!maps.isEmpty()) error(SERMAPS, maps);

    enc     = code(p.get(S_ENCODING), null);
    docsys  = p.get(S_DOCTYPE_SYSTEM);
    docpub  = p.get(S_DOCTYPE_PUBLIC);
    media   = p.get(S_MEDIA_TYPE);
    indent  = p.check(S_INDENT, YES, NO).equals(YES);
    undecl  = p.check(S_UNDECLARE_PREFIXES, YES, NO).equals(YES);
    escape  = p.check(S_ESCAPE_URI_ATTRIBUTES, YES, NO).equals(YES);
    content = p.check(S_INCLUDE_CONTENT_TYPE, YES, NO).equals(YES);
    spaces  = Math.max(0, toInt(p.get(S_INDENT_SPACES)));
    wrapPre = token(p.get(S_WRAP_PRE));
    wrapUri = token(p.get(S_WRAP_URI));
    wrap    = wrapPre.length != 0;

    if(docsys.isEmpty()) {
      docsys = null;
      docpub = null;
    } else if(docpub.isEmpty()) {
      docpub = null;
    }

    if(!Charset.isSupported(enc)) error(SERENCODING, enc);
    if(undecl && version.equals(V10)) error(SERUNDECL);

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
        print(PI1);
        print(DOCDECL1);
        print(version);
        print(DOCDECL2);
        print(p.get(S_ENCODING));
        if(!sa.equals(OMIT)) {
          print(DOCDECL3);
          print(sa);
        }
        print(ATT2);
        print(PI2);
        ind = indent;
      } else if(!sa.equals(OMIT) || version.equals(V11) && docsys != null) {
        error(SERSTAND);
      }
    }

    // open results element
    if(wrap) {
      openElement(concat(wrapPre, COL, RESULTS));
      namespace(wrapPre, wrapUri);
      finishElement();
    }
  }

  /**
   * Returns an I/O exception. Replaces all % characters in the input string
   * (see {@link TokenBuilder#addExt} for details).
   * @param str string to be extended
   * @param ext extensions
   * @throws IOException I/O exception
   */
  private static void error(final Object str, final Object... ext)
      throws IOException {
    throw new IOException(Util.info(str, ext));
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
      openElement(concat(wrapPre, COL, RESULT));
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

    // [LW] temporarily disabled; many tests rely on serializing attributes
    //if(!inTag) error(SERTAT);

    print(' ');
    print(n);

    final byte[] tagatt = mth == M_XML || tags.size() > 0 ? EMPTY :
      concat(lc(tags.get(tags.size() - 1)), COL, lc(n));

    // don't append value for boolean attributes
    if(mth == M_HTML && BOOLEAN.id(tagatt) != 0) return;
    // escape URI attributes
    final byte[] val = escape && (mth == M_HTML || mth == M_XHTML) &&
        URIS.id(tagatt) != 0 ? escape(v) : v;

    print(ATT1);
    for(int k = 0; k < val.length; k += cl(val, k)) {
      final int ch = cp(val, k);
      if(mth == M_HTML && (ch == '<' || ch == '&' &&
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
  public void text(final byte[] b) throws IOException {
    finishElement();

    if(cdata.size() != 0 && cdata.contains(tags.get(tags.size() - 1)) &&
        mth != M_HTML && mth != M_TEXT) {
      print(CDATA1);
      int c = 0;
      for(int k = 0; k < b.length; k += cl(b, k)) {
        final int ch = cp(b, k);
        if(ch == ']') {
          ++c;
        } else {
          if(c > 1 && ch == '>') {
            print(CDATA2);
            print(CDATA1);
          }
          c = 0;
        }
        print(ch);
      }
      print(CDATA2);
    } else {
      for(int k = 0; k < b.length; k += cl(b, k)) ch(cp(b, k));
    }
    ind = false;
  }

  @Override
  public void text(final byte[] b, final FTPos ftp) throws IOException {
    finishElement();

    final FTLexer lex = new FTLexer().sc().init(b);
    while(lex.hasNext()) {
      final FTSpan span = lex.next();
      if(!span.special && ftp.contains(span.pos)) print((char) 0x10);
      final byte[] t = span.text;
      for(int k = 0; k < t.length; k += cl(t, k)) ch(cp(t, k));
    }
    ind = false;
  }

  @Override
  public void comment(final byte[] n) throws IOException {
    if(mth == M_TEXT) return;
    finishElement();
    if(ind) indent(true);
    print(COM1);
    print(n);
    print(COM2);
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    if(mth == M_TEXT) return;
    finishElement();
    if(ind) indent(true);
    if(mth == M_HTML && contains(v, '>')) error(SERPI);
    print(PI1);
    print(n);
    print(' ');
    print(v);
    print(mth == M_HTML ? ELEM2 : PI2);
  }

  @Override
  public void item(final byte[] b) throws IOException {
    finishElement();
    if(ind) print(' ');
    for(int k = 0; k < b.length; k += cl(b, k)) ch(cp(b, k));
    ind = true;
    item = true;
  }

  /**
   * Prints a single character.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  private void ch(final int ch) throws IOException {
    if(mth == M_HTML) {
      if(script) {
        print(ch);
        return;
      }
      if(ch < 0x20 && ch != 0x09 && ch != 0x0A && ch != 0x0D) return;
      if(ch > 0x7F && ch < 0xA0) error(SERILL, Integer.toHexString(ch));
      if(ch == 0xA0) {
        print(E_NBSP);
        return;
      }
    }

    if(ch < 0x20 && ch != 0x09 && ch != 0x0A || ch > 0x7F && ch < 0xA0) {
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
  protected void start(final byte[] t) throws IOException {
    if(mth == M_TEXT) return;

    if(tags.size() == 1 && docsys != null) {
      if(ind) indent(false);
      print(DOCTYPE);
      if(mth == M_HTML) print(M_HTML);
      else print(t);
      if(docpub != null) {
        print(" " + PUBLIC + " \"" + docpub + "\"");
      } else {
        print(" " + SYSTEM);
      }
      print(" \"" + docsys + "\"");
      print(ELEM2);
      print(NL);
      docsys = null;
    }

    if(ind) indent(false);
    print(ELEM1);
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
  protected void empty() throws IOException {
    if(mth == M_TEXT) return;
    if(mth == M_XML) {
      print(ELEM4);
    } else {
      final byte[] tag = tags.get(tags.size());
      final boolean empty = EMPTIES.contains(lc(tag));
      if(mth == M_XHTML && empty) {
        print(' ');
        print(ELEM4);
      } else {
        print(ELEM2);
        if(mth == M_HTML && empty) return;
        ind = false;
        close(tag);
      }
    }
  }

  @Override
  protected void finish() throws IOException {
    if(mth != M_TEXT) print(ELEM2);
  }

  @Override
  protected void close(final byte[] t) throws IOException {
    if(mth == M_TEXT) return;
    if(ind) indent(true);
    print(ELEM3);
    print(t);
    print(ELEM2);
    ind = indent;
    if(mth == M_HTML) script &= !SCRIPTS.contains(lc(t));
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
      for(int l = 0, ls = (level() - (close ? 0 : 1)) * spaces; l < ls; ++l)
        print(' ');
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
    SCRIPTS.add(token("script"));
    SCRIPTS.add(token("style"));
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
