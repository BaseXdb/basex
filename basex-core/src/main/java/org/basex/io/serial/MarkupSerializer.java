package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;

import java.io.*;

import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.http.*;
import org.basex.util.options.*;

/**
 * This class serializes items to in a markup language.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class MarkupSerializer extends StandardSerializer {
  /** System document type. */
  String docsys;
  /** Public document type. */
  String docpub;

  /** Indicates if root element has been serialized. */
  boolean root;
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
  private final boolean undecl;

  /** Media type. */
  private final String media;

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @param versions supported versions
   * @throws IOException I/O exception
   */
  protected MarkupSerializer(final OutputStream os, final SerializerOptions sopts,
      final String... versions) throws IOException {

    super(os, sopts);

    final String ver = supported(VERSION, sopts, versions);
    final String htmlver = supported(HTML_VERSION, sopts, V40, V401, V50);
    html5 = htmlver.equals(V50) || ver.equals(V50);

    final boolean omitDecl = sopts.yes(OMIT_XML_DECLARATION);
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
          out.print('\n');
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
        printChar(cp);
      }
    }
    out.print(ATT2);
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    if(elems.isEmpty()) checkRoot(null);
    final byte[] val = norm(value);
    if(ftp == null) {
      final QNmSet qnames = cdata();
      final int vl = val.length;
      if(qnames.isEmpty() || elems.isEmpty() || !qnames.contains(elems.peek())) {
        for(int k = 0; k < vl; k += cl(val, k)) {
          printChar(cp(val, k));
        }
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
          out.print(cp);
        }
        out.print(CDATA_C);
      }
    } else {
      final FTLexer lexer = new FTLexer().original().init(val);
      while(lexer.hasNext()) {
        final FTSpan span = lexer.next();
        if(!span.del && ftp.contains(span.pos)) out.print(TokenBuilder.MARK);
        final byte[] text = span.text;
        final int tl = text.length;
        for(int t = 0; t < tl; t += cl(text, t)) printChar(cp(text, t));
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
  protected void openDoc(final byte[] name) {
    sep = false;
  }

  @Override
  protected void startOpen(final QNm name) throws IOException {
    if(elems.isEmpty()) checkRoot(name.string());
    if(sep) indent();
    out.print(ELEM_O);
    out.print(name.string());
    sep = true;
  }

  /**
   * Checks if document serialization is valid.
   * @param name name of doctype (if {@code null}, no doctype declaration will be output)
   * @throws IOException I/O exception
   */
  final void checkRoot(final byte[] name) throws IOException {
    if(root) {
      if(!saomit) throw SERSA.getIO();
      if(docsys != null) throw SERDT.getIO();
    }
    if(name != null) doctype(name);
    root = true;
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
  protected void atomic(final Item item) throws IOException {
    if(elems.isEmpty()) checkRoot(null);
    super.atomic(item);
  }

  @Override
  protected void print(final int cp) throws IOException {
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
      try {
        super.print(cp);
      } catch(final QueryIOException ex) {
        if(ex.getCause().error() == SERENC_X_X) printHex(cp);
        else throw ex;
      }
    }
  }

  /**
   * Prints the document type declaration.
   * @param type document type
   * @throws IOException I/O exception
   */
  protected abstract void doctype(byte[] type) throws IOException;

  @Override
  protected boolean skipElement(final ANode node) {
    if(node.type == NodeType.ELEMENT && eq(node.name(), META)) {
      final byte[] value = node.attribute(HTTP_EQUIV);
      return value != null && eq(trim(value), CONTENT_TYPE);
    }
    return false;
  }

  /**
   * Prints the document type declaration.
   * @param type document type
   * @param pub doctype-public parameter
   * @param sys doctype-system parameter
   * @throws IOException I/O exception
   */
  protected final void printDoctype(final byte[] type, final String pub, final String sys)
      throws IOException {

    if(level != 0 || root) return;
    if(sep) indent();
    out.print(DOCTYPE);
    out.print(type);
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
      final QNmSet qnames = suppress();
      if(!qnames.isEmpty()) {
        for(final QNm qname : elems) {
          if(qnames.contains(qname)) return;
        }
      }
      super.indent();
    }
  }

  /**
   * Prints the content type declaration.
   * @param empty empty flag
   * @param html method
   * @return {@code true} if declaration was printed
   * @throws IOException I/O exception
   */
  protected final boolean printCT(final boolean empty, final boolean html) throws IOException {
    if(skip != 1) return false;
    skip++;
    if(empty) finishOpen();
    level++;
    startOpen(new QNm(META));
    attribute(HTTP_EQUIV, CONTENT_TYPE, false);
    attribute(CONTENT, concat(media.isEmpty() ? MediaType.TEXT_HTML : media, "; ",
      CHARSET, "=", encoding), false);
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

    final String string = opts.get(option);
    if(string.isEmpty()) return allowed.length > 0 ? allowed[0] : string;
    for(final String value : allowed) {
      if(value.equals(string)) return string;
    }
    throw SERNOTSUPP_X.getIO(Options.allowed(option, string, (Object[]) allowed));
  }

  /** CData elements. */
  private QNmSet cdata;

  /**
   * Initializes the CData elements.
   * @return list
   * @throws QueryIOException query I/O exception
   */
  private QNmSet cdata() throws QueryIOException {
    QNmSet list = cdata;
    if(list == null) {
      list = new QNmSet();
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
  private QNmSet suppress;

  /**
   * Initializes and returns the elements whose contents must not be indented.
   * @return list
   * @throws QueryIOException query I/O exception
   */
  private QNmSet suppress() throws QueryIOException {
    QNmSet list = suppress;
    if(list == null) {
      list = new QNmSet();
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
