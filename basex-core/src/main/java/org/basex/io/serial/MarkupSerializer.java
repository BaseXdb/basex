package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

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
  private String docpub;
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

  /** CData elements. */
  private final TokenSet cdata = new TokenSet();
  /** Suppress indentation elements. */
  private final TokenSet suppress = new TokenSet();

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

    final String supp = sopts.get(SUPPRESS_INDENTATION);
    if(!supp.isEmpty()) {
      for(final byte[] c : split(normalize(token(supp)), ' ')) {
        if(c.length != 0) suppress.add(c);
      }
    }

    // collect CData elements
    final boolean html = this instanceof HTMLSerializer;
    final boolean xml = this instanceof XMLSerializer || this instanceof XHTMLSerializer;
    if(xml || html) {
      final String cdse = sopts.get(CDATA_SECTION_ELEMENTS);
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
  protected void namespace(final byte[] prefix, final byte[] uri) throws IOException {
    if(undecl || prefix.length == 0 || uri.length != 0) super.namespace(prefix, uri);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) throws IOException {
    out.print(' ');
    out.print(name);
    out.print(ATT1);
    final byte[] val = norm(value);
    final int vl = val.length;
    for(int k = 0; k < vl; k += cl(val, k)) {
      final int cp = cp(val, k);
      if(cp == '"') {
        out.print(E_QUOT);
      } else if(cp == 0x9 || cp == 0xA) {
        hex(cp);
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
      if(cdata.isEmpty() || elems.isEmpty() || !cdata.contains(elems.peek())) {
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
  protected void startOpen(final byte[] name) throws IOException {
    doctype(name);
    if(sep) indent();
    out.print(ELEM_O);
    out.print(name);
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
    out.print(elem);
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
      hex(cp);
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
   * @return true if doctype was added
   * @throws IOException I/O exception
   */
  boolean doctype(final byte[] type) throws IOException {
    if(level != 0 || docsys == null && docpub == null) return false;
    if(sep) indent();
    out.print(DOCTYPE);
    if(type == null) out.print(HTML);
    else out.print(type);
    if(docpub != null) out.print(' ' + PUBLIC + " \"" + docpub + '"');
    else out.print(' ' + SYSTEM);
    if(docsys != null) out.print(" \"" + docsys + '"');
    out.print(ELEM_C);
    sep = true;
    return true;
  }

  @Override
  protected void indent() throws IOException {
    if(atomic) {
      atomic = false;
    } else if(indent) {
      if(!suppress.isEmpty() && !elems.isEmpty()) {
        for(final byte[] t : elems) if(suppress.contains(t)) return;
      }
      super.indent();
    }
  }

  /**
   * Returns a hex entity for the specified codepoint.
   * @param cp codepoint (00-FF)
   * @throws IOException I/O exception
   */
  final void hex(final int cp) throws IOException {
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
  boolean ct(final boolean empty, final boolean html) throws IOException {
    if(ct != 1) return false;
    ct++;
    if(empty) finishOpen();
    level++;
    startOpen(META);
    attribute(HTTPEQUIV, token(MimeTypes.CONTENT_TYPE));
    attribute(CONTENT, new TokenBuilder(media.isEmpty() ? MimeTypes.TEXT_HTML : media).
        add(CHARSET).addExt(out.encoding()).finish());
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

  @Override
  protected boolean ignore(final ANode node) {
    return ct > 0 && eq(node.name(), META) && node.attribute(HTTPEQUIV) != null;
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
}
