package org.basex.data;

import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import static org.basex.data.SerializeProp.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.PrintOutput;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.TokenSet;
import org.basex.util.Tokenizer;

/**
 * This class serializes XML results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class XMLSerializer extends Serializer {
  /** Default serialization parameters. */
  private static final SerializeProp PROPS = new SerializeProp();
  /** Indentation. */
  private static final byte[] SPACES = { ' ', ' ' };
  /** New line. */
  private static final byte[] NL = token(Prop.NL);
  /** Colon. */
  private static final byte[] COL = { ':' };

  /** HTML: elements with an empty content model. */
  private static final TokenList EMPTY = new TokenList();
  /** HTML: script elements. */
  private static final TokenList SCRIPTS = new TokenList();
  /** HTML: boolean attributes. */
  private static final TokenSet BOOLEAN = new TokenSet();
  /** HTML: URI attributes. */
  private static final TokenSet URIS = new TokenSet();
  
  /** Output stream. */
  private final PrintOutput out;
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
  /** Serialization method. */
  private final String method;
  /** Encoding. */
  private final String enc;

  /** XML version. */
  private String version;
  /** System document type. */
  private String docsys = "";
  /** Public document type. */
  private String docpub = "";

  /** Temporary indentation flag. */
  private boolean ind;
  /** Temporary item flag. */
  private boolean item;
  /** Script flag. */
  private boolean script;
  
  /**
   * Constructor.
   * @param o output stream reference
   * @throws IOException I/O exception
   */
  public XMLSerializer(final OutputStream o) throws IOException {
    this(o, PROPS);
  }
  
  /**
   * Constructor, specifying serialization options.
   * @param o output stream reference
   * @param p serialization properties
   * @throws IOException I/O exception
   */
  public XMLSerializer(final OutputStream o, final SerializeProp p)
      throws IOException {

    out = o instanceof PrintOutput ? (PrintOutput) o : new PrintOutput(o);

    final String m = p.check(S_METHOD, M_XML, M_XHTML, M_HTML, M_TEXT);
    method  = m.equals(M_XML) ? M_XML : m.equals(M_XHTML) ?
        M_XHTML : m.equals(M_HTML) ? M_HTML : M_TEXT;

    version = p.get(S_VERSION).length() == 0 ? method == M_HTML ? V40 : V10 :
      method == M_HTML ? p.check(S_VERSION, V40, V401) :
      method != M_TEXT ? p.check(S_VERSION, V10, V11) : p.get(S_VERSION);
    
    for(final String c : p.get(S_CDATA_SECTION_ELEMENTS).split("\\s+"))
      if(c.length() != 0) cdata.add(token(c));

    boolean docdecl = p.check(S_OMIT_XML_DECLARATION, YES, NO).equals(NO);
    boolean bom     = p.check(S_BYTE_ORDER_MARK, YES, NO).equals(YES);
    String  sa      = p.check(S_STANDALONE, YES, NO, OMIT);
    p.check(S_NORMALIZATION_FORM, "NFC", "none");

    final String maps = p.get(S_USE_CHARACTER_MAPS);
    if(maps.length() != 0) error(SERMAPS, maps);
    
    enc     = enc(p.get(S_ENCODING));
    docsys  = p.get(S_DOCTYPE_SYSTEM);
    docpub  = p.get(S_DOCTYPE_PUBLIC);
    indent  = p.check(S_INDENT, YES, NO).equals(YES);
    undecl  = p.check(S_UNDECLARE_PREFIXES, YES, NO).equals(YES);
    escape  = p.check(S_ESCAPE_URI_ATTRIBUTES, YES, NO).equals(YES);
    content = p.check(S_INCLUDE_CONTENT_TYPE, YES, NO).equals(YES);
    wrapPre = token(p.get(S_WRAP_PRE));
    wrapUri = token(p.get(S_WRAP_URI));
    wrap    = wrapPre.length != 0;

    if(docsys.length() == 0) {
      docsys = null;
      docpub = null;
    }

    if(!Charset.isSupported(enc)) error(SERENCODING, enc);
    if(undecl && version.equals(V10)) error(SERUNDECL);

    if(bom) {
      if(enc == UTF8) {
        out.write(0xEF); out.write(0xBB); out.write(0xBF);
      } else if(enc == UTF16LE) {
        out.write(0xFF); out.write(0xFE);
      } else if(enc == UTF16BE) {
        out.write(0xFE); out.write(0xFF);
      }
    }
    
    if(docdecl && method != M_HTML && method != M_TEXT) {
      print(PI1);
      print(DOCDECL1);
      print(version);
      print(DOCDECL2);
      print(p.get(S_ENCODING));
      if(!sa.equals(OMIT)) {
        print(DOCDECL3);
        print(sa);
      }
      print('\'');
      print(PI2);
      ind = indent;
    } else if(!sa.equals(OMIT) || version.equals(V11) && docsys != null) {
      error(SERSTAND);
    }

    if(wrap) {
      openElement(concat(wrapPre, COL, RESULTS));
      namespace(wrapPre, wrapUri);
    }
  }
  
  /**
   * Global method, replacing all % characters
   * (see {@link TokenBuilder#add(Object, Object...)} for details.
   * @param str string to be extended
   * @param ext text text extensions
   * @throws IOException I/O exception
   */
  public static void error(final Object str, final Object... ext)
      throws IOException {
    throw new IOException(Main.info(str, ext));
  }

  @Override
  public void cls() throws IOException {
    if(wrap) closeElement();
  }

  @Override
  public void openResult() throws IOException {
    if(wrap) openElement(concat(wrapPre, COL, RESULT));;
  }

  @Override
  public void closeResult() throws IOException {
    if(wrap) closeElement();
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) throws IOException {
    if(method == M_TEXT) return;

    print(' ');
    print(n);

    final byte[] tagatt = concat(lc(tags.get(tags.size() - 1)), COL, lc(n));
    // don't append value for boolean attributes
    if(method == M_HTML && BOOLEAN.id(tagatt) != 0) return;
    // escape URI attributes
    final byte[] val = escape && (method == M_HTML || method == M_XHTML) &&
        URIS.id(tagatt) != 0 ? escape(v) : v;
    
    print(ATT1);
    for(int k = 0; k < val.length; k += cl(val[k])) {
      final int ch = cp(val, k);
      if(method == M_HTML && (ch == '<' ||
          ch == '&' && val[Math.min(k + 1, val.length - 1)] == '{')) {
        print(ch);
      } else {
        switch(ch) {
          case '"': print(E_QU);  break;
          case 0x9: print(E_TAB); break;
          case 0xA: print(E_NL); break;
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
        method != M_HTML && method != M_TEXT) {
      print("<![CDATA[");
      int c = 0;
      for(int k = 0; k < b.length; k += cl(b[k])) {
        final int ch = cp(b, k);
        if(ch == ']') {
          c++;
        } else {
          if(c > 1 && ch == '>') print("]]><![CDATA[");
          c = 0;
        }
        print(ch);
      }
      print("]]>");
    } else {
      for(int k = 0; k < b.length; k += cl(b[k])) ch(cp(b, k));
    }
    ind = false;
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
    ind = false;
  }

  @Override
  public void comment(final byte[] n) throws IOException {
    if(method == M_TEXT) return;
    finishElement();
    if(ind) indent(true);
    print(COM1);
    print(n);
    print(COM2);
  }

  @Override
  public void pi(final byte[] n, final byte[] v) throws IOException {
    if(method == M_TEXT) return;
    finishElement();
    if(ind) indent(true);
    if(method == M_HTML && contains(v, '>')) error(SERPI);
    print(PI1);
    print(n);
    print(' ');
    print(v);
    print(method == M_HTML ? ELEM2 : PI2);
  }

  @Override
  public void item(final byte[] b) throws IOException {
    finishElement();
    if(ind) print(' ');
    for(int k = 0; k < b.length; k += cl(b[k])) ch(cp(b, k));
    ind = true;
    item = true;
  }

  /**
   * Prints a single character.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  private void ch(final int ch) throws IOException {
    if(script && method == M_HTML) {
      print(ch);
    } else {
      switch(ch) {
        case '&': print(E_AMP); break;
        case '>': print(E_GT); break;
        case '<': print(E_LT); break;
        case 0xD: print(E_CR); break;
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
    if(method == M_TEXT) return;

    if(tags.size() == 1 && docsys != null) {
      if(ind) indent(false);
      print(DOCTYPE);
      if(method == M_TEXT) print(t);
      else print(M_HTML);
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
    
    if(method == M_HTML) script = SCRIPTS.contains(lc(t));

    // subsequent content type elements are currently ignored
    if(content && (method == M_HTML || method == M_XHTML) && eq(lc(t), HEAD))
      emptyElement(META, HTTPEQUIV, TEXTHTML, CHARSET, token(enc));
  }

  @Override
  protected void empty() throws IOException {
    if(method == M_TEXT) return;
    if(method == M_XML) {
      print(ELEM4);
    } else {
      print(ELEM2);
      final byte[] tag = tags.get(tags.size());
      if(method == M_HTML && EMPTY.contains(lc(tag))) return;
      ind = false;
      close(tag);
    }
  }

  @Override
  protected void finish() throws IOException {
    if(method == M_TEXT) return;
    print(ELEM2);
  }

  @Override
  protected void close(final byte[] t) throws IOException {
    if(method == M_TEXT) return;
    if(ind) indent(true);
    print(ELEM3);
    print(t);
    print(ELEM2);
    ind = indent;
    if(method == M_HTML) script &= !SCRIPTS.contains(lc(t));
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
    for(int l = 1; l < s; l++) print(SPACES);
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
    } else if(ch < 0xA0 && method == M_HTML) {
      error(SERILL, Integer.toHexString(ch));
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
      for(final byte b : token(s)) out.write(b);
    } else {
      final boolean le = enc == UTF16LE;
      if(enc == UTF16BE || le) {
        for(int i = 0; i < s.length(); i++) {
          final char ch = s.charAt(i);
          out.write(le ? ch & 0xFF : ch >>> 8);
          out.write(le ? ch >>> 8 : ch & 0xFF);
        }
      } else {
        out.write(s.getBytes(enc));
      }
    }
  }

  // HTML Serializer: cache elements
  static {
    EMPTY.add(token("area"));
    EMPTY.add(token("base"));
    EMPTY.add(token("br"));
    EMPTY.add(token("col"));
    EMPTY.add(token("hr"));
    EMPTY.add(token("img"));
    EMPTY.add(token("input"));
    EMPTY.add(token("link"));
    EMPTY.add(token("meta"));
    EMPTY.add(token("basefont"));
    EMPTY.add(token("frame"));
    EMPTY.add(token("isindex"));
    EMPTY.add(token("param"));
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
