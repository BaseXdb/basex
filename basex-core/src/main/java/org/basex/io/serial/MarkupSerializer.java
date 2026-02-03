package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;

import java.io.*;
import java.util.*;

import org.basex.io.out.PrintOutput.*;
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
 * @author BaseX Team, BSD License
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
  int script;

  /** HTML5 flag. */
  final boolean html5;
  /** URI escape flag. */
  final boolean escape;
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

    String version = sopts.get(VERSION), hv = sopts.get(HTML_VERSION);
    if(hv.matches("\\d+(\\.\\d+)?")) hv = Double.toString(Double.parseDouble(hv));
    html5 = hv.equals(V50) || versions[0].equals(V50) && hv.isEmpty() &&
        (version.isEmpty() || version.equals(V50));
    version = checkVersion(VERSION, version, versions);
    checkVersion(VERSION, hv, V50, V401, V40);

    final boolean omitDecl = sopts.yes(OMIT_XML_DECLARATION);
    final YesNoOmit sa = sopts.get(STANDALONE);
    saomit = sa == YesNoOmit.OMIT;

    docsys  = sopts.get(DOCTYPE_SYSTEM);
    docpub  = sopts.get(DOCTYPE_PUBLIC);
    media   = sopts.get(MEDIA_TYPE);
    escape  = sopts.yes(ESCAPE_URI_ATTRIBUTES);
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
    if(item instanceof final XQArray array) {
      for(final Item it : flatten(array)) super.serialize(it);
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
    for(int v = 0; v < vl; v += cl(val, v)) {
      final int cp = cp(val, v);
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
   * Prints the delimiter preceding an attribute inside an opening or empty tag. This is attribute
   * indentation, if enabled, for all but the first attribute, but at least a single space.
   * @throws IOException I/O exception
   */
  protected void delimitAttribute() throws IOException {
    if(indAttr && out.lineLength() > indAttrLength) {
      out.print('\n');
      for(int i = 0; i < indAttrLength; ++i) out.print(' ');
    }
    out.print(' ');
  }

  /** Fallback function. */
  private final Fallback fallbackCDATA = cp -> {
    out.print(CDATA_C);
    printHex(cp);
    out.print(CDATA_O);
  };

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    if(opened.isEmpty()) checkRoot(null);
    final byte[] val = normalize(value, form);
    if(ftp == null) {
      final QNmSet qnames = cdata();
      final int vl = val.length;
      if(qnames.isEmpty() || opened.isEmpty() || !qnames.contains(opened.peek())) {
        for(int v = 0; v < vl; v += cl(val, v)) {
          printChar(cp(val, v));
        }
      } else {
        out.print(CDATA_O);
        int c = 0;
        for(int v = 0; v < vl; v += cl(val, v)) {
          final int cp = cp(val, v);
          if(cp == ']') {
            ++c;
          } else {
            if(c > 1 && cp == '>') {
              out.print(CDATA_C);
              out.print(CDATA_O);
            }
            c = 0;
          }
          out.print(cp, fallbackCDATA);
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
    out.print(value.length > 0 ? concat(name, cpToken(' '), value) : name);
    out.print(PI_C);
    sep = true;
  }

  @Override
  protected void openDoc(final byte[] name) {
    sep = false;
  }

  @Override
  protected void startOpen(final QNm name) throws IOException {
    if(opened.isEmpty()) checkRoot(name);
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
  final void checkRoot(final QNm name) throws IOException {
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
    if(canonical) {
      out.print(ELEM_C);
      out.print(ELEM_OS);
      out.print(elem.string());
      out.print(ELEM_C);
    } else {
      out.print(ELEM_SC);
    }
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

  /** Fallback function. */
  private final Fallback fallback = this::printHex;

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
      out.print(cp, fallback);
    }
  }

  /**
   * Prints the document type declaration.
   * @param name name of element
   * @throws IOException I/O exception
   */
  protected abstract void doctype(QNm name) throws IOException;

  @Override
  protected boolean skipElement(final XNode node) {
    if(node.type == NodeType.ELEMENT && eq(node.name(), META)) {
      if(node.attribute(new QNm(CHARSET)) != null) return true;
      final byte[] value = node.attribute(new QNm(HTTP_EQUIV));
      if(value != null && eq(lc(trim(value)), lc(CONTENT_TYPE))) return true;
    }
    return false;
  }

  /**
   * Prints the document type declaration.
   * @param name name of element
   * @param pub doctype-public parameter (can be {@code null})
   * @param sys doctype-system parameter (can be {@code null})
   * @throws IOException I/O exception
   */
  protected final void printDoctype(final byte[] name, final String pub, final String sys)
      throws IOException {

    if(level != 0 || root) return;
    if(sep) indent();
    final TokenBuilder tb = new TokenBuilder().add('<').add('!').add(DOCTYPE).add(' ').add(name);
    if(pub != null || sys != null) tb.add(' ').add(pub != null ? PUBLIC : SYSTEM);
    if(pub != null) tb.add(" \"").add(pub).add('"');
    if(sys != null) tb.add(" \"").add(sys).add('"');
    out.print(tb.finish());
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
   * @param html HTML/XHTML flag
   * @return {@code true} if declaration was printed
   * @throws IOException I/O exception
   */
  protected final boolean printCT(final boolean empty, final boolean html) throws IOException {
    if(skip != 1) return false;
    skip++;
    if(empty) finishOpen();
    level++;
    startOpen(new QNm(elem.hasPrefix() ? concat(elem.prefix(), ":", META) : META));
    if(html5) {
      attribute(CHARSET, token(encoding), false);
    } else {
      attribute(HTTP_EQUIV, CONTENT_TYPE, false);
      attribute(CONTENT, concat(media.isEmpty() ? MediaType.TEXT_HTML : media, "; ",
        CHARSET, "=", encoding), false);
    }
    out.print(html ? ELEM_C : ELEM_SC);
    level--;
    if(empty) finishClose();
    return true;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Checks if the specified version is valid and returns a normalized value.
   * @param option option
   * @param string value
   * @param allowed allowed values
   * @return value
   * @throws QueryIOException query I/O exception
   */
  static String checkVersion(final StringOption option, final String string,
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
   * Checks if indentation is to be suppressed for the specified QName.
   * @param qname qname to check
   * @return result of check
   * @throws QueryIOException query I/O exception
   */
  boolean suppressIndentation(final QNm qname) throws QueryIOException {
    if(suppress == null) {
      suppress = new QNmSet();
      for(final QNm name : qnames(SUPPRESS_INDENTATION)) {
        suppress.add(new QNm(lc(name.string()), name.uri()));
      }
    }
    return !suppress.isEmpty() && suppress.contains(new QNm(lc(qname.string()), qname.uri()));
  }

  /**
   * Returns the value of an option as a list of QNames.
   * @param option option to be found
   * @return QNames
   * @throws QueryIOException query I/O exception
   */
  private ArrayList<QNm> qnames(final StringOption option) throws QueryIOException {
    final ArrayList<QNm> list = new ArrayList<>();
    for(final byte[] name : distinctTokens(token(sopts.get(option)))) {
      try {
        list.add(QNm.parse(name, sc != null ? sc.elemNS : null, sc, null));
      } catch(final QueryException ex) {
        throw new QueryIOException(ex);
      }
    }
    return list;
  }
}
