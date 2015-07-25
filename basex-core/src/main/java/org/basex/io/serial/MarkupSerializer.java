package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.http.*;
import org.basex.util.options.*;
import org.basex.util.options.Options.YesNoOmit;

/**
 * This class serializes items to in a markup language.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
abstract class MarkupSerializer extends StandardSerializer {
  /** System document type. */
  String docsys;
  /** Public document type. */
  String docpub;
  /** Flag for printing content type. */
  int ct;

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
  /** Undeclare prefixes. */
  final boolean undecl;

  /** Media type. */
  private final String media;

  /**
   * Constructor.
   * @param out print output
   * @param sopts serialization parameters
   * @param versions supported versions
   * @throws IOException I/O exception
   */
  protected MarkupSerializer(final PrintOutput out, final SerializerOptions sopts,
      final String... versions) throws IOException {

    super(out, sopts);

    final String ver = supported(VERSION, sopts, versions);
    final String htmlver = supported(HTML_VERSION, sopts, V40, V401, V50);
    html5 = htmlver.equals(V50) || ver.equals(V50);

    final boolean omitDecl = sopts.yes(OMIT_XML_DECLARATION);
    final boolean bom  = sopts.yes(BYTE_ORDER_MARK);
    final YesNoOmit sa = sopts.get(STANDALONE);
    saomit = sa == YesNoOmit.OMIT;

    docsys  = sopts.get(DOCTYPE_SYSTEM);
    docpub  = sopts.get(DOCTYPE_PUBLIC);
    media   = sopts.get(MEDIA_TYPE);
    escuri  = sopts.yes(ESCAPE_URI_ATTRIBUTES);
    content = sopts.yes(INCLUDE_CONTENT_TYPE);
    undecl  = sopts.yes(UNDECLARE_PREFIXES);

    if(docsys.isEmpty()) docsys = null;
    if(docpub.isEmpty()) docpub = null;

    if(bom) {
      // comparison by reference
      final String enc = out.encoding();
      if(enc == Strings.UTF8) {
        out.write(0xEF); out.write(0xBB); out.write(0xBF);
      } else if(enc == Strings.UTF16LE) {
        out.write(0xFF); out.write(0xFE);
      } else if(enc == Strings.UTF16BE) {
        out.write(0xFE); out.write(0xFF);
      }
    }

    final boolean html = this instanceof HTMLSerializer;
    final boolean xml = this instanceof XMLSerializer || this instanceof XHTMLSerializer;

    if(xml || html) {
      if(undecl && ver.equals(V10)) throw SERUNDECL.getIO();
      if(xml) {
        if(omitDecl) {
          if(!saomit || !ver.equals(V10) && docsys != null) throw SERSTAND.getIO();
        } else {
          out.print(PI_O);
          out.print(DOCDECL1);
          out.print(ver);
          out.print(DOCDECL2);
          out.print(sopts.get(ENCODING));
          if(!saomit) {
            out.print(DOCDECL3);
            out.print(sa.toString());
          }
          out.print(ATT2);
          out.print(PI_C);
          sep = true;
        }
      }
    }
  }

  // PROTECTED METHODS ============================================================================

  @Override
  protected void namespace(final byte[] prefix, final byte[] uri, final boolean standalone)
      throws IOException {
    if(undecl || prefix.length == 0 || uri.length != 0) super.namespace(prefix, uri, standalone);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException {

    if(!standalone) out.print(' ');
    out.print(name);
    out.print(ATT1);
    final byte[] val = norm(value);
    final int vl = val.length;
    for(int k = 0; k < vl; k += cl(val, k)) {
      final int cp = cp(val, k);
      if(cp == '"') {
        out.print(E_QUOT);
      } else if(cp == 0x9 || cp == 0xA) {
        printHex(cp);
      } else {
        encode(cp);
      }
    }
    out.print(ATT2);
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    final byte[] val = norm(value);
    final int vl = val.length;
    if(ftp == null) {
      final ArrayList<QNm> qnames = cdata();
      if(qnames.isEmpty() || elems.isEmpty() || !qnames.contains(elems.peek())) {
        for(int k = 0; k < vl; k += cl(val, k)) encode(cp(val, k));
      } else {
        out.print(CDATA_O);
        int c = 0;
        for(int k = 0; k < vl; k += cl(val, k)) {
          final int cp = cp(val, k);
          if(cp == ']') {
            ++c;
          } else {
            if(c > 1 && cp == '>') {
              out.print(CDATA_C);
              out.print(CDATA_O);
            }
            c = 0;
          }
          printChar(cp);
        }
        out.print(CDATA_C);
      }
    } else {
      final FTLexer lex = new FTLexer().original().init(val);
      while(lex.hasNext()) {
        final FTSpan span = lex.next();
        if(!span.del && ftp.contains(span.pos)) out.print((char) TokenBuilder.MARK);
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
    out.print(COMM_O);
    out.print(value);
    out.print(COMM_C);
    sep = true;
  }

  @Override
  protected void pi(final byte[] name, final byte[] value) throws IOException {
    if(sep) indent();
    out.print(PI_O);
    out.print(name);
    out.print(' ');
    out.print(value);
    out.print(PI_C);
    sep = true;
  }

  @Override
  protected void openDoc(final byte[] name) throws IOException {
    sep = false;
  }

  @Override
  protected void startOpen(final QNm name) throws IOException {
    doctype(name);
    if(sep) indent();
    out.print(ELEM_O);
    out.print(name.string());
    sep = true;
  }

  @Override
  protected void finishOpen() throws IOException {
    out.print(ELEM_C);
  }

  @Override
  protected void finishEmpty() throws IOException {
    out.print(ELEM_SC);
  }

  @Override
  protected void finishClose() throws IOException {
    if(sep) indent();
    out.print(ELEM_OS);
    out.print(elem.string());
    out.print(ELEM_C);
    sep = true;
  }

  @Override
  protected void encode(final int cp) throws IOException {
    // character map
    if(map != null) {
      final byte[] value = map.get(cp);
      if(value != null) {
        out.print(value);
        return;
      }
    }

    if(cp < ' ' && cp != '\n' && cp != '\t' || cp >= 0x7F && cp < 0xA0) {
      printHex(cp);
    } else if(cp == '&') {
      out.print(E_AMP);
    } else if(cp == '>') {
      out.print(E_GT);
    } else if(cp == '<') {
      out.print(E_LT);
    } else if(cp == 0x2028) {
      out.print(E_2028);
    } else {
      printChar(cp);
    }
  }

  /**
   * Prints the document type declaration.
   * @param type document type or {@code null} for html type
   * @throws IOException I/O exception
   */
  protected abstract void doctype(final QNm type) throws IOException;

  @Override
  protected boolean ignore(final ANode node) {
    return ct > 0 && eq(node.name(), META) && node.attribute(HTTPEQUIV) != null;
  }

  /**
   * Prints the document type declaration.
   * @param type document type or {@code null} for html type
   * @param pub doctype-public parameter
   * @param sys doctype-system parameter
   * @throws IOException I/O exception
   */
  protected final void printDoctype(final QNm type, final String pub, final String sys)
      throws IOException {

    if(level != 0) return;
    if(sep) indent();
    out.print(DOCTYPE);
    out.print(type == null ? HTML : type.string());
    if(sys != null || pub != null) {
      if(pub != null) out.print(' ' + PUBLIC + " \"" + pub + '"');
      else out.print(' ' + SYSTEM);
      if(sys != null) out.print(" \"" + sys + '"');
    }
    out.print(ELEM_C);
    sep = true;
  }

  @Override
  protected void indent() throws IOException {
    if(atomic) {
      atomic = false;
    } else if(indent) {
      final ArrayList<QNm> qnames = suppress();
      if(!qnames.isEmpty()) {
        for(final QNm e : elems) {
          if(qnames.contains(e)) return;
        }
      }
      super.indent();
    }
  }

  /**
   * Returns a hex entity for the specified codepoint.
   * @param cp codepoint (00-FF)
   * @throws IOException I/O exception
   */
  protected final void printHex(final int cp) throws IOException {
    out.print("&#x");
    if(cp > 0xF) out.print(HEX[cp >> 4]);
    out.print(HEX[cp & 0xF]);
    out.print(';');
  }

  /**
   * Prints the content type declaration.
   * @param empty empty flag
   * @param html method
   * @return {@code true} if declaration was printed
   * @throws IOException I/O exception
   */
  protected final boolean printCT(final boolean empty, final boolean html) throws IOException {
    if(ct != 1) return false;
    ct++;
    if(empty) finishOpen();
    level++;
    startOpen(new QNm(META));
    attribute(HTTPEQUIV, CONTENT_TYPE, false);
    attribute(CONTENT, new TokenBuilder(media.isEmpty() ? MediaType.TEXT_HTML.toString() : media).
        add("; ").add(CHARSET).add('=').addExt(out.encoding()).finish(), false);
    if(html) {
      out.print(ELEM_C);
    } else {
      out.print(' ');
      out.print(ELEM_SC);
    }
    level--;
    if(empty) finishClose();
    return true;
  }

  // PRIVATE METHODS ==============================================================================

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

  /** CData elements. */
  private ArrayList<QNm> cdata;

  /**
   * Initializes the CData elements.
   * @return list
   * @throws QueryIOException query I/O exception
   */
  private ArrayList<QNm> cdata() throws QueryIOException {
    ArrayList<QNm> list = cdata;
    if(list == null) {
      list = new ArrayList<>();
      final boolean html = this instanceof HTMLSerializer;
      final String cdse = sopts.get(CDATA_SECTION_ELEMENTS);
      for(final byte[] name : split(normalize(token(cdse)), ' ')) {
        if(name.length == 0) continue;
        final QNm qnm = resolve(name);
        if(!html || contains(name, ':') && (!html5 || !string(name).contains("html:"))) {
          list.add(qnm);
        }
      }
      cdata = list;
    }
    return list;
  }

  /** Suppress indentation elements. */
  private ArrayList<QNm> suppress;

  /**
   * Initializes and returns the elements whose contents must not be indented.
   * @return list
   * @throws QueryIOException query I/O exception
   */
  private ArrayList<QNm> suppress() throws QueryIOException {
    ArrayList<QNm> list = suppress;
    if(list == null) {
      list = new ArrayList<>();
      final String supp = sopts.get(SUPPRESS_INDENTATION);
      for(final byte[] name : split(normalize(token(supp)), ' ')) {
        if(name.length != 0) list.add(resolve(name));
      }
      suppress = list;
    }
    return list;
  }

  /**
   * Resolves a QName.
   * @param name name to be resolved
   * @return list
   * @throws QueryIOException query I/O exception
   */
  private QNm resolve(final byte[] name) throws QueryIOException {
    try {
      return QNm.resolve(name, sc == null ? null : sc.elemNS, sc, null);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
