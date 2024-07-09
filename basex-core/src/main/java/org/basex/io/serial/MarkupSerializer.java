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
import org.basex.query.value.array.*;
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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class MarkupSerializer extends StandardSerializer {
  /** Token. */
  private static final QNm Q_HTTP_EQUIV = new QNm(HTTP_EQUIV);

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
  /** Suppress indentation elements. */
  private QNmSet suppress;
  /** Media type. */
  private final String media;
  /** Indent attributes. */
  private final boolean indAttr;
  /** Attribute indentation length. */
  protected long indAttrLength;

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

    final String version = supported(VERSION, sopts.get(VERSION), versions);
    String hv = sopts.get(HTML_VERSION);
    if(hv.matches("\\d+(\\.\\d+)?")) hv = Double.toString(Double.parseDouble(hv));
    html5 = version.equals(V50) || supported(HTML_VERSION, hv, V40, V401, V50).equals(V50);

    final boolean omitDecl = sopts.yes(OMIT_XML_DECLARATION);
    final YesNoOmit sa = sopts.get(STANDALONE);
    saomit = sa == YesNoOmit.OMIT;

    docsys  = sopts.get(DOCTYPE_SYSTEM);
    docpub  = sopts.get(DOCTYPE_PUBLIC);
    media   = sopts.get(MEDIA_TYPE);
    escuri  = sopts.yes(ESCAPE_URI_ATTRIBUTES);
    content = sopts.yes(INCLUDE_CONTENT_TYPE);
    undecl  = sopts.yes(UNDECLARE_PREFIXES);
    indAttr = sopts.yes(INDENT_ATTRIBUTES);

    if(docsys.isEmpty()) docsys = null;
    if(docpub.isEmpty()) docpub = null;

    final boolean html = this instanceof HTMLSerializer;
    final boolean xml = this instanceof XMLSerializer || this instanceof XHTMLSerializer;
    if(xml || html) {
      if(undecl && version.equals(V10)) throw SERUNDECL.getIO();
      if(xml) {
        if(omitDecl) {
          if(!saomit || !version.equals(V10) && docsys != null) throw SERSTAND.getIO();
        } else {
          out.print(PI_O);
          out.print(DOCDECL1);
          out.print(version);
          out.print(DOCDECL2);
          out.print(sopts.get(ENCODING));
          if(!saomit) {
            out.print(DOCDECL3);
            out.print(sa.toString());
          }
          out.print(ATT2);
          out.print(PI_C);
          if(indent) out.print('\n');
        }
      }
    }
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(item instanceof XQArray) {
      for(final Item it : flatten((XQArray) item)) super.serialize(it);
    } else {
      super.serialize(item);
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

    if(!standalone) delimitAttribute();
    out.print(name);
    out.print(ATT1);
    final byte[] val = normalize(value, form);
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

  /**
   * Print the delimiter preceding an attribute inside of an opening or empty
   * tag. This is attribute indentation, if enabled, for all but the first
   * attribute, but at least a single space.
   * @throws IOException I/O exception
   */
  protected void delimitAttribute() throws IOException {
    if(indAttr && out.lineLength() > indAttrLength) {
      out.print('\n');
      for(int i = 0; i < indAttrLength; ++i) out.print(' ');
    }
    out.print(' ');
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    if(opened.isEmpty()) checkRoot(null);
    final byte[] val = normalize(value, form);
    if(ftp == null) {
      final QNmSet qnames = cdata();
      final int vl = val.length;
      if(qnames.isEmpty() || opened.isEmpty() || !qnames.contains(opened.peek())) {
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
    if(opened.isEmpty()) checkRoot(name.string());
    if(sep) indent();
    out.print(ELEM_O);
    out.print(name.string());
    indAttrLength = out.lineLength();
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
    if(opened.isEmpty()) checkRoot(null);
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
      final byte[] value = node.attribute(Q_HTTP_EQUIV);
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
      if(inline()) return;
      for(final QNm qname : opened) {
        if(suppressIndentation(qname)) return;
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
    out.print(html ? ELEM_C : ELEM_SC);
    level--;
    if(empty) finishClose();
    return true;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Retrieves a value from the specified option and checks for supported values.
   * @param option option
   * @param string value
   * @param allowed allowed values
   * @return value
   * @throws QueryIOException query I/O exception
   */
  private static String supported(final StringOption option, final String string,
      final String... allowed) throws QueryIOException {

    if(string.isEmpty()) return allowed.length > 0 ? allowed[0] : string;
    if(Strings.eq(string, allowed)) return string;
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
    if(cdata == null) {
      cdata = new QNmSet();
      final boolean html = this instanceof HTMLSerializer;
      for(final QNm name : qnames(CDATA_SECTION_ELEMENTS)) {
        final byte[] uri = name.uri();
        if(!html || uri.length != 0 && (!html5 || !eq(uri, XHTML_URI))) cdata.add(name);
      }
    }
    return cdata;
  }

  /**
   * Checks if the next element should be rendered inline with its context, i.e.
   * without indentation adjacent to it.
   * @return result of check
   */
  boolean inline() {
    return false;
  }

  /**
   * Checks if indentation is to be suppressed for the the specified QName.
   * @param qname qname to check
   * @return result of check
   * @throws QueryIOException query I/O exception
   */
  boolean suppressIndentation(final QNm qname) throws QueryIOException {
    if(suppress == null) suppress = qnames(SUPPRESS_INDENTATION);
    return suppress.contains(qname);
  }

  /**
   * Returns the values of an option as QNames.
   * @param option option to be found
   * @return set of QNames
   * @throws QueryIOException query I/O exception
   */
  private QNmSet qnames(final StringOption option) throws QueryIOException {
    try {
      return QNm.set(sopts.get(option), sc);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
