package org.basex.io.serial;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.basex.data.FTPos;
import org.basex.io.MimeTypes;
import org.basex.io.out.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.StrStream;
import org.basex.util.TokenBuilder;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTSpan;
import org.basex.util.hash.TokenSet;
import org.basex.util.list.TokenList;

/**
 * This class serializes data to an output stream.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class OutputSerializer extends Serializer {
  /** (X)HTML: elements with an empty content model. */
  static final TokenList EMPTIES = new TokenList();
  /** (X)HTML: URI attributes. */
  static final TokenSet URIS = new TokenSet();

  /** System document type. */
  private String docsys;
  /** Public document type. */
  private String docpub;
  /** Flag for printing content type. */
  int ct;
  /** Indentation flag (used for formatting). */
  boolean ind;
  /** Item flag (used for formatting). */
  private boolean item;
  /** Script flag. */
  boolean script;

  /** URI escape flag. */
  final boolean escape;
  /** CData elements. */
  private final TokenList cdata = new TokenList();
  /** Suppress indentation elements. */
  private final TokenList suppress = new TokenList();
  /** Indentation flag. */
  final boolean indent;
  /** Include content type flag. */
  final boolean content;
  /** Media type. */
  private final String media;
  /** Charset. */
  private final Charset encoding;
  /** New line. */
  final byte[] nl;
  /** Output stream. */
  final PrintOutput out;

  /** UTF8 flag. */
  private final boolean utf8;

  // project specific properties

  /** Number of spaces to indent. */
  final int indents;
  /** Tabular character. */
  final char tab;

  /** Format items. */
  private final boolean format;
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

    final boolean decl = !p.yes(S_OMIT_XML_DECLARATION);
    final boolean bom  = p.yes(S_BYTE_ORDER_MARK);
    final String sa = p.check(S_STANDALONE, YES, NO, OMIT);
    p.check(S_NORMALIZATION_FORM, NFC, NONE);

    final String maps = p.get(S_USE_CHARACTER_MAPS);
    final String enc = normEncoding(p.get(S_ENCODING), null);
    try {
      encoding = Charset.forName(enc);
    } catch(final Exception ex) {
      throw SERENCODING.thrwSerial(enc);
    }
    utf8 = enc == UTF8;

    // project specific properties
    indents = Math.max(0, toInt(p.get(S_INDENTS)));
    format  = p.yes(S_FORMAT);
    tab     = p.yes(S_TABULATOR) ? '\t' : ' ';
    wPre    = token(p.get(S_WRAP_PREFIX));
    /* URI for wrapped results. */
    final byte[] wUri = token(p.get(S_WRAP_URI));
    wrap    = wPre.length != 0;
    final String eol = p.check(S_NEWLINE, S_NL, S_CR, S_CRNL);
    nl = utf8(token(eol.equals(S_NL) ? "\n" : eol.equals(S_CR) ? "\r" : "\r\n"), enc);

    docsys  = p.get(S_DOCTYPE_SYSTEM);
    docpub  = p.get(S_DOCTYPE_PUBLIC);
    media   = p.get(S_MEDIA_TYPE);
    escape  = p.yes(S_ESCAPE_URI_ATTRIBUTES);
    content = p.yes(S_INCLUDE_CONTENT_TYPE);
    undecl  = p.yes(S_UNDECLARE_PREFIXES);
    indent  = p.yes(S_INDENT) && format;

    if(!maps.isEmpty()) SERMAP.thrwSerial(maps);

    if(docsys.isEmpty()) {
      docsys = null;
      docpub = null;
    } else if(docpub.isEmpty()) {
      docpub = null;
    }

    // print byte-order-mark
    out = PrintOutput.get(os);
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

    final String supp = p.get(S_SUPPRESS_INDENTATION);
    if(!supp.isEmpty()) {
      for(final String c : supp.split("\\s+")) {
        if(!c.isEmpty()) suppress.add(token(c));
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
      openElement(wPre.length != 0 ?
          concat(wPre, COLON, T_RESULTS) : T_RESULTS);
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
      openElement(wPre.length != 0 ? concat(wPre, COLON, T_RESULT) : T_RESULT);
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
        printChar(ch);
      } else if(ch == '"') {
        print(E_QU);
      } else if(ch == 0x9 || ch == 0xA) {
        hex(ch);
      } else {
        code(ch);
      }
    }
    print(ATT2);
  }

  @Override
  public void finishText(final byte[] b) throws IOException {
    if(cdata.empty() || tags.empty() || !cdata.contains(tags.peek())) {
      for(int k = 0; k < b.length; k += cl(b, k)) code(cp(b, k));
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
        printChar(ch);
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
      for(int k = 0; k < t.length; k += cl(t, k)) code(cp(t, k));
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
  public void finishAtomic(final Item it) throws IOException {
    if(ind) print(' ');

    try {
      if(it instanceof StrStream) {
        final InputStream ni = ((StrStream) it).input(null);
        try {
          for(int i; (i = ni.read()) != -1;) code(i);
        } finally {
          ni.close();
        }
      } else {
        final byte[] atom = it.string(null);
        for(int a = 0; a < atom.length; a += cl(atom, a)) code(cp(atom, a));
      }
    } catch(final QueryException ex) {
      throw new SerializerException(ex);
    }

    ind = format;
    item = true;
  }

  /**
   * Encode the specified character before printing it.
   * @param ch character to be encoded and printed
   * @throws IOException I/O exception
   */
  void code(final int ch) throws IOException {
    if(!format) {
      printChar(ch);
    } else if(ch < ' ' && ch != '\n' && ch != '\t' || ch > 0x7F && ch < 0xA0) {
      hex(ch);
    } else if(ch == '&') {
      print(E_AMP);
    } else if(ch == '>') {
      print(E_GT);
    } else if(ch == '<') {
      print(E_LT);
    } else {
      printChar(ch);
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
  void doctype(final byte[] dt) throws IOException {
    if(level != 0 || docsys == null) return;
    if(ind) indent();
    print(DOCTYPE);
    if(dt == null) print(M_HTML);
    else print(dt);
    if(docpub != null) {
      print(' ' + PUBLIC + " \"" + docpub + '"');
    } else {
      print(' ' + SYSTEM);
    }
    print(" \"" + docsys + '"');
    print(ELEM_C);
    print(nl);
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
  final void indent() throws IOException {
    if(!indent) return;

    if(item) {
      item = false;
    } else {
      if(!suppress.empty() && !tags.empty()) {
        for(final byte[] s : suppress) {
          if(tags.contains(s)) return;
        }
      }
      print(nl);
      final int ls = level * indents;
      for(int l = 0; l < ls; ++l) print(tab);
    }
  }

  /**
   * Returns a hex entity for the specified character.
   * @param ch character
   * @throws IOException I/O exception
   */
  final void hex(final int ch) throws IOException {
    print("&#x");
    print(HEX[ch >> 4]);
    print(HEX[ch & 15]);
    print(';');
  }

  /**
   * Writes a character in the current encoding.
   * Converts newlines to the operating system default.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  final void printChar(final int ch) throws IOException {
    if(ch == '\n') out.write(nl);
    else print(ch);
  }

  /**
   * Writes a character in the current encoding.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  void print(final int ch) throws IOException {
    // comparison by reference
    if(utf8) out.utf8(ch);
    else out.write(new TokenBuilder(4).add(ch).toString().getBytes(encoding));
  }

  /**
   * Writes a token in the current encoding.
   * @param token token to be printed
   * @throws IOException I/O exception
   */
  final void print(final byte[] token) throws IOException {
    // comparison by reference
    if(utf8) {
      for(final byte b : token) out.write(b);
    } else {
      out.write(string(token).getBytes(encoding));
    }
  }

  /**
   * Writes a string in the current encoding.
   * @param s string to be printed
   * @throws IOException I/O exception
   */
  final void print(final String s) throws IOException {
    // comparison by reference
    if(utf8) {
      for(final byte b : token(s)) out.write(b);
    } else {
      out.write(s.getBytes(encoding));
    }
  }

  /**
   * Prints the content type declaration.
   * @param empty empty flag
   * @param html method
   * @return {@code true} if declaration was printed
   * @throws IOException I/O exception
   */
  boolean ct(final boolean empty, final boolean html)
      throws IOException {

    if(ct != 1) return false;
    ct++;
    if(empty) finishOpen();
    level++;
    startOpen(META);
    attribute(HTTPEQUIV, token(CONTENT_TYPE));
    attribute(CONTENT, new TokenBuilder(media.isEmpty() ? MimeTypes.TEXT_HTML :
      media).add(CHARSET).addExt(encoding).finish());
    if(html) {
      print(ELEM_C);
    } else {
      print(' ');
      print(ELEM_SC);
    }
    level--;
    if(empty) finishClose();
    return true;
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
