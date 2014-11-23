package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;
import org.basex.util.options.Options.YesNoOmit;

/**
 * This class serializes data to an output stream.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class OutputSerializer extends Serializer {
  /** System document type. */
  String docsys;
  /** Public document type. */
  private String docpub;
  /** Flag for printing content type. */
  int ct;
  /** Separator flag (used for formatting). */
  protected boolean sep;
  /** Item separator flag (used for formatting). */
  private boolean isep;
  /** Script flag. */
  boolean script;

  /** HTML5 flag. */
  final boolean html5;
  /** URI escape flag. */
  final boolean escuri;
  /** Standalone 'omit' flag. */
  final boolean saomit;
  /** Include content type flag. */
  final boolean content;

  /** New line. */
  protected final byte[] nl;
  /** Output stream. */
  protected final PrintOutput out;

  /** Item flag (used for formatting). */
  private boolean atomic;
  /** Charset encoder. */
  private CharsetEncoder encoder;
  /** Encoding buffer. */
  private TokenBuilder encbuffer;

  /** Charset. */
  private final Charset encoding;
  /** UTF8 flag. */
  private final boolean utf8;
  /** CData elements. */
  private final TokenSet cdata = new TokenSet();
  /** Suppress indentation elements. */
  private final TokenSet suppress = new TokenSet();
  /** Media type. */
  private final String media;
  /** Item separator. */
  private final byte[] itemsep;
  /** WebDAV flag. */
  private final boolean webdav;

  // project specific parameters

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
   * Constructor.
   * @param os output stream reference
   * @param sopts serialization parameters
   * @param versions supported versions
   * @throws IOException I/O exception
   */
  protected OutputSerializer(final OutputStream os, final SerializerOptions sopts,
      final String... versions) throws IOException {

    final SerializerOptions opts = sopts == null ? SerializerOptions.get(true) : sopts;
    final String ver = supported(VERSION, opts, versions);
    final String htmlver = supported(HTML_VERSION, opts, V40, V401, V50);
    html5 = htmlver.equals(V50) || ver.equals(V50);

    final boolean omitDecl = opts.yes(OMIT_XML_DECLARATION);
    final boolean bom  = opts.yes(BYTE_ORDER_MARK);
    final YesNoOmit sa = opts.get(STANDALONE);
    saomit = sa == YesNoOmit.OMIT;

    final String maps = opts.get(USE_CHARACTER_MAPS);
    final String enc = normEncoding(opts.get(ENCODING), true);
    try {
      encoding = Charset.forName(enc);
    } catch(final Exception ex) {
      throw SERENCODING_X.getIO(enc);
    }
    utf8 = enc == UTF8;
    if(!utf8) {
      encoder = encoding.newEncoder();
      encbuffer = new TokenBuilder();
    }

    // project specific options
    indents = opts.get(INDENTS);
    format  = opts.yes(FORMAT);
    tab     = opts.yes(TABULATOR) ? '\t' : ' ';
    wPre    = token(opts.get(WRAP_PREFIX));
    wrap    = wPre.length != 0;

    nl = utf8(token(opts.get(NEWLINE).newline()), enc);
    itemsep = opts.contains(ITEM_SEPARATOR) ? token(opts.get(ITEM_SEPARATOR).replace("\\n", "\n").
        replace("\\r", "\r").replace("\\t", "\t")) : null;

    docsys  = opts.get(DOCTYPE_SYSTEM);
    docpub  = opts.get(DOCTYPE_PUBLIC);
    media   = opts.get(MEDIA_TYPE);
    escuri  = opts.yes(ESCAPE_URI_ATTRIBUTES);
    content = opts.yes(INCLUDE_CONTENT_TYPE);
    undecl  = opts.yes(UNDECLARE_PREFIXES);
    indent  = opts.yes(INDENT) && format;

    webdav = "webdav".equals(maps);
    if(!webdav && !maps.isEmpty()) throw SERMAP_X.getIO(maps);

    if(docsys.isEmpty()) docsys = null;
    if(docpub.isEmpty()) docpub = null;

    // print byte-order-mark
    out = PrintOutput.get(os);
    final int l = opts.get(LIMIT);
    if(l != -1) out.setLimit(l);

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

    final String supp = opts.get(SUPPRESS_INDENTATION);
    if(!supp.isEmpty()) {
      for(final byte[] c : split(normalize(token(supp)), ' ')) {
        if(c.length != 0) suppress.add(c);
      }
    }

    // collect CData elements
    final boolean html = this instanceof HTMLSerializer;
    final boolean xml = this instanceof XMLSerializer || this instanceof XHTMLSerializer;
    if(xml || html) {
      final String cdse = opts.get(CDATA_SECTION_ELEMENTS);
      if(!cdse.isEmpty()) {
        for(final byte[] c :  split(normalize(token(cdse)), ' ')) {
          if(c.length == 0) continue;
          if(!html || contains(c, ':') && (!html5 || !string(c).contains("html:"))) cdata.add(c);
        }
      }

      if(undecl && ver.equals(V10)) throw SERUNDECL.getIO();
      if(xml) {
        if(omitDecl) {
          if(!saomit || !ver.equals(V10) && docsys != null) throw SERSTAND.getIO();
        } else {
          print(PI_O);
          print(DOCDECL1);
          print(ver);
          print(DOCDECL2);
          print(opts.get(ENCODING));
          if(!saomit) {
            print(DOCDECL3);
            print(sa.toString());
          }
          print(ATT2);
          print(PI_C);
          sep = true;
        }
      }
    }

    // open results element
    if(wrap) {
      openElement(concat(wPre, COLON, T_RESULTS));
      namespace(wPre, token(opts.get(WRAP_URI)));
    }
  }

  @Override
  public final void reset() {
    sep = false;
    atomic = false;
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
    if(wrap) openElement(wPre.length == 0 ? T_RESULT : concat(wPre, COLON, T_RESULT));
  }

  @Override
  protected void closeResult() throws IOException {
    if(wrap) closeElement();
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) throws IOException {
    print(' ');
    print(name);
    print(ATT1);
    final int vl = value.length;
    for(int k = 0; k < vl; k += cl(value, k)) {
      final int ch = cp(value, k);
      if(!format) {
        printChar(ch);
      } else if(ch == '"') {
        print(E_QU);
      } else if(ch == 0x9 || ch == 0xA) {
        hex(ch);
      } else {
        encode(ch);
      }
    }
    print(ATT2);
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    if(ftp == null) {
      final int bl = value.length;
      if(cdata.isEmpty() || elems.isEmpty() || !cdata.contains(elems.peek())) {
        for(int k = 0; k < bl; k += cl(value, k)) encode(cp(value, k));
      } else {
        print(CDATA_O);
        int c = 0;
        final int vl = value.length;
        for(int k = 0; k < vl; k += cl(value, k)) {
          final int ch = cp(value, k);
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
    } else {
      final FTLexer lex = new FTLexer().sc().init(value);
      while(lex.hasNext()) {
        final FTSpan span = lex.next();
        if(!span.special && ftp.contains(span.pos)) print((char) TokenBuilder.MARK);
        final byte[] text = span.text;
        final int tl = text.length;
        for(int t = 0; t < tl; t += cl(text, t)) encode(cp(text, t));
      }
    }
    sep = false;
  }

  @Override
  protected void comment(final byte[] value) throws IOException {
    if(sep) indent();
    print(COMM_O);
    print(value);
    print(COMM_C);
    sep = true;
  }

  @Override
  protected void pi(final byte[] name, final byte[] value) throws IOException {
    if(sep) indent();
    print(PI_O);
    print(name);
    print(' ');
    print(value);
    print(PI_C);
    sep = true;
  }

  @Override
  protected void atomic(final Item it, final boolean iter) throws IOException {
    if(sep && atomic) print(' ');
    try {
      if(it instanceof StrStream) {
        try(final InputStream ni = ((StrStream) it).input(null)) {
          for(int cp; (cp = ni.read()) != -1;) {
            if(iter) print(cp); else encode(cp);
          }
        }
      } else {
        final byte[] atom = it.string(null);
        final int al = atom.length;
        for(int a = 0; a < al; a += cl(atom, a)) {
          final int cp = cp(atom, a);
          if(iter) print(cp); else encode(cp);
        }
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
    atomic = true;
  }

  @Override
  protected void openDoc(final byte[] name) throws IOException {
    sep = false;
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    doctype(name);
    if(sep) indent();
    print(ELEM_O);
    print(name);
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
  protected void encode(final int ch) throws IOException {
    if(!format) {
      printChar(ch);
    } else if(ch < ' ' && ch != '\n' && ch != '\t' || ch >= 0x7F && ch < 0xA0 ||
        webdav && ch == 0xA0) {
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
   * @param type document type or {@code null} for html type
   * @return true if doctype was added
   * @throws IOException I/O exception
   */
  boolean doctype(final byte[] type) throws IOException {
    if(lvl != 0 || docsys == null && docpub == null) return false;
    if(sep) indent();
    print(DOCTYPE);
    if(type == null) print(HTML);
    else print(type);
    if(docpub != null) print(' ' + PUBLIC + " \"" + docpub + '"');
    else print(' ' + SYSTEM);
    if(docsys != null) print(" \"" + docsys + '"');
    print(ELEM_C);
    sep = true;
    return true;
  }

  /**
   * Indents the next text.
   * @throws IOException I/O exception
   */
  protected void indent() throws IOException {
    if(atomic) {
      atomic = false;
    } else if(indent) {
      if(!suppress.isEmpty() && !elems.isEmpty()) {
        for(final byte[] t : elems) if(suppress.contains(t)) return;
      }
      print(nl);
      final int ls = lvl * indents;
      for(int l = 0; l < ls; l++) print(tab);
    }
  }

  /**
   * Returns a hex entity for the specified character.
   * @param ch character
   * @throws IOException I/O exception
   */
  final void hex(final int ch) throws IOException {
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
    if(utf8) {
      out.print(ch);
    } else {
      encbuffer.reset();
      encoder.reset();
      final ByteBuffer bb = encoder.encode(CharBuffer.wrap(encbuffer.add(ch).toString()));
      out.write(bb.array(), 0, bb.limit());
    }
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
   * @param string string to be printed
   * @throws IOException I/O exception
   */
  protected final void print(final String string) throws IOException {
    // comparison by reference
    if(utf8) {
      for(final byte b : token(string)) out.write(b);
    } else {
      out.write(string.getBytes(encoding));
    }
  }

  /**
   * Prints the content type declaration.
   * @param empty empty flag
   * @param html method
   * @return {@code true} if declaration was printed
   * @throws IOException I/O exception
   */
  boolean ct(final boolean empty, final boolean html) throws IOException {
    if(ct != 1) return false;
    ct++;
    if(empty) finishOpen();
    lvl++;
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
    lvl--;
    if(empty) finishClose();
    return true;
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Retrieves a value from the specified option and checks for supported values.
   * @param option option
   * @param opts options
   * @param allowed allowed values
   * @return value
   * @throws QueryIOException query I/O exception
   */
  private static String supported(final StringOption option, final Options opts,
      final String... allowed) throws QueryIOException {

    final String val = opts.get(option);
    if(val.isEmpty()) return allowed.length > 0 ? allowed[0] : val;
    for(final String a : allowed) if(a.equals(val)) return val;
    throw SERNOTSUPP_X.getIO(Options.allowed(option, (Object[]) allowed));
  }
}
