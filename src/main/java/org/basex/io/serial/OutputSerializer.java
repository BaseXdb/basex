package org.basex.io.serial;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * This class serializes data to an output stream.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class OutputSerializer extends Serializer {
  /** System document type. */
  protected String docsys;
  /** Public document type. */
  private String docpub;
  /** Flag for printing content type. */
  protected int ct;
  /** Separator flag (used for formatting). */
  protected boolean sep;
  /** Item separator flag (used for formatting). */
  protected boolean isep;
  /** Script flag. */
  protected boolean script;

  /** HTML5 flag. */
  protected final boolean html5;
  /** URI escape flag. */
  protected final boolean escape;
  /** Standalone 'omit' flag. */
  protected final boolean saomit;
  /** Indentation flag. */
  protected final boolean indent;
  /** Include content type flag. */
  protected final boolean content;
  /** New line. */
  protected final byte[] nl;
  /** Output stream. */
  protected final PrintOutput out;

  /** Item flag (used for formatting). */
  private boolean item;
  /** UTF8 flag. */
  private final boolean utf8;
  /** CData elements. */
  private final TokenSet cdata = new TokenSet();
  /** Suppress indentation elements. */
  private final TokenSet suppress = new TokenSet();
  /** Media type. */
  private final String media;
  /** Charset. */
  private final Charset encoding;
  /** Item separator. */
  private final byte[] itemsep;

  // project specific properties

  /** Number of spaces to indent. */
  protected final int indents;
  /** Tabular character. */
  protected final char tab;

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
    final String ver = p.supported(S_VERSION, versions);
    final String htmlver = p.supported(S_HTML_VERSION, V40, V401, V50);
    html5 = htmlver.equals(V50) || ver.equals(V50);

    final boolean decl = !p.yes(S_OMIT_XML_DECLARATION);
    final boolean bom  = p.yes(S_BYTE_ORDER_MARK);
    final String sa = p.check(S_STANDALONE, YES, NO, OMIT);
    saomit = sa.equals(OMIT);
    p.check(S_NORMALIZATION_FORM, NFC, DataText.NONE);

    final String maps = p.get(S_USE_CHARACTER_MAPS);
    final String enc = normEncoding(p.get(S_ENCODING));
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
    wrap    = wPre.length != 0;
    final String eol = p.check(S_NEWLINE, S_NL, S_CR, S_CRNL);
    nl = utf8(token(eol.equals(S_NL) ? "\n" : eol.equals(S_CR) ? "\r" : "\r\n"), enc);
    String s = p.get(S_ITEM_SEPARATOR);
    if(s.equals(UNDEFINED)) s = p.get(S_SEPARATOR);
    itemsep = s.equals(UNDEFINED) ? null : token(s.indexOf('\\') != -1 ?
      s.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t") : s);

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
        if(!c.isEmpty()) suppress.add(c);
      }
    }

    // print document declaration
    if(this instanceof XMLSerializer || this instanceof XHTMLSerializer) {
      final String cdse = p.get(S_CDATA_SECTION_ELEMENTS);
      if(!cdse.isEmpty()) {
        for(final String c : cdse.split("\\s+")) {
          if(!c.isEmpty()) cdata.add(c);
        }
      }

      if(undecl && ver.equals(V10)) SERUNDECL.thrwSerial();
      if(decl) {
        print(PI_O);
        print(DOCDECL1);
        print(ver);
        print(DOCDECL2);
        print(p.get(S_ENCODING));
        if(!saomit) {
          print(DOCDECL3);
          print(sa);
        }
        print(ATT2);
        print(PI_C);
        sep = true;
      } else if(!saomit || !ver.equals(V10) && docsys != null) {
        SERSTAND.thrwSerial();
      }
    }

    // open results element
    if(wrap) {
      startElement(concat(wPre, COLON, T_RESULTS));
      namespace(wPre, token(p.get(S_WRAP_URI)));
    }
  }

  @Override
  public final void reset() {
    sep = false;
    item = false;
    isep = false;
  }

  @Override
  public void close() throws IOException {
    if(wrap) closeElement();
    out.flush();
  }

  @Override
  public final boolean finished() {
    return out.finished();
  }

  // PROTECTED METHODS ==================================================================

  @Override
  protected void openResult() throws IOException {
    final byte[] sp = itemsep;
    if(sp != null) {
      if(isep) {
        final int sl = sp.length;
        if(sl == 1) {
          printChar(sp[0]);
        } else {
          for(int s = 0; s < sl; s += cl(sp, s)) printChar(cp(sp, s));
        }
        sep = false;
      } else {
        isep = true;
      }
    }
    if(wrap) startElement(wPre.length != 0 ? concat(wPre, COLON, T_RESULT) : T_RESULT);
  }

  @Override
  protected void closeResult() throws IOException {
    if(wrap) closeElement();
  }

  @Override
  protected void attribute(final byte[] n, final byte[] v) throws IOException {
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
  protected void finishText(final byte[] b) throws IOException {
    if(cdata.isEmpty() || tags.isEmpty() || !cdata.contains(tags.peek())) {
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
    sep = false;
  }

  @Override
  protected void finishText(final byte[] b, final FTPos ftp) throws IOException {
    final FTLexer lex = new FTLexer().sc().init(b);
    while(lex.hasNext()) {
      final FTSpan span = lex.next();
      if(!span.special && ftp.contains(span.pos)) print((char) TokenBuilder.MARK);
      final byte[] t = span.text;
      for(int k = 0; k < t.length; k += cl(t, k)) code(cp(t, k));
    }
    sep = false;
  }

  @Override
  protected void finishComment(final byte[] n) throws IOException {
    if(sep) indent();
    print(COMM_O);
    print(n);
    print(COMM_C);
    sep = true;
  }

  @Override
  protected void finishPi(final byte[] n, final byte[] v) throws IOException {
    if(sep) indent();
    print(PI_O);
    print(n);
    print(' ');
    print(v);
    print(PI_C);
    sep = true;
  }

  @Override
  protected void atomic(final Item it) throws IOException {
    if(sep && item) print(' ');

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
    sep = true;
    item = true;
  }

  @Override
  protected void openDoc(final byte[] n) throws IOException {
    sep = false;
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
    doctype(t);
    if(sep) indent();
    print(ELEM_O);
    print(t);
    sep = true;
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
    if(sep) indent();
    print(ELEM_OS);
    print(elem);
    print(ELEM_C);
    sep = true;
  }

  /**
   * Encodes the specified character before printing it.
   * @param ch character to be encoded and printed
   * @throws IOException I/O exception
   */
  protected void code(final int ch) throws IOException {
    if(!format) {
      printChar(ch);
    } else if(ch < ' ' && ch != '\n' && ch != '\t' || ch >= 0x7F && ch < 0xA0) {
      hex(ch);
    } else if(ch == '&') {
      print(E_AMP);
    } else if(ch == '>') {
      print(E_GT);
    } else if(ch == '<') {
      print(E_LT);
    } else if(ch == 0x2028) {
      print(E_2028);
    } else {
      printChar(ch);
    }
  }

  /**
   * Prints the document type declaration.
   * @param dt document type, or {@code null} for html type
   * @return true if doctype was added
   * @throws IOException I/O exception
   */
  protected boolean doctype(final byte[] dt) throws IOException {
    if(level != 0 || docsys == null) return false;
    if(sep) indent();
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
    sep = true;
    return true;
  }

  /**
   * Indents the next text.
   * @throws IOException I/O exception
   */
  protected final void indent() throws IOException {
    if(item) {
      item = false;
    } else if(indent) {
      if(!suppress.isEmpty() && !tags.isEmpty()) {
        for(final byte[] t : tags) {
          if(suppress.contains(t)) return;
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
  protected final void hex(final int ch) throws IOException {
    print("&#x");
    final int h = ch >> 4;
    if(h != 0) print(HEX[h]);
    print(HEX[ch & 15]);
    print(';');
  }

  /**
   * Writes a character in the current encoding.
   * Converts newlines to the operating system default.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  protected final void printChar(final int ch) throws IOException {
    if(ch == '\n') out.write(nl);
    else print(ch);
  }

  /**
   * Writes a character in the current encoding.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  protected void print(final int ch) throws IOException {
    // comparison by reference
    if(utf8) out.utf8(ch);
    else out.write(new TokenBuilder(4).add(ch).toString().getBytes(encoding));
  }

  /**
   * Writes a token in the current encoding.
   * @param token token to be printed
   * @throws IOException I/O exception
   */
  protected final void print(final byte[] token) throws IOException {
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
  protected final void print(final String s) throws IOException {
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
  protected boolean ct(final boolean empty, final boolean html) throws IOException {
    if(ct != 1) return false;
    ct++;
    if(empty) finishOpen();
    level++;
    startOpen(META);
    attribute(HTTPEQUIV, token(MimeTypes.CONTENT_TYPE));
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
}
