package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * This class serializes items as HTML.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class HTMLSerializer extends MarkupSerializer {
  /** (X)HTML: elements with an empty content model. */
  static final TokenSet EMPTIES;
  /** HTML5: elements with an empty content model. */
  static final TokenSet EMPTIES5;
  /** (X)HTML: formatted elements. */
  static final TokenSet FORMATTEDS;
  /** (X)HTML: inline elements. */
  static final TokenSet INLINES;
  /** (X)HTML: URI attributes. */
  static final TokenSet URIS;

  /** HTML: script elements. */
  private static final TokenSet SCRIPTS;
  /** HTML: boolean attributes. */
  private static final TokenSet BOOLEAN;

  // HTML Serializer: cache elements
  static {
    // script elements
    SCRIPTS = new TokenSet("script", "style");
    // boolean attributes
    BOOLEAN = new TokenSet("area@nohref", "audio@autoplay", "audio@controls",
        "audio@loop", "audio@muted", "button@disabled", "button@autofocus", "button@formnovalidate",
        "details@open", "dialog@open", "dir@compact", "dl@compact", "fieldset@disabled",
        "form@novalidate", "frame@noresize", "hr@noshade", "img@ismap", "input@checked",
        "input@disabled", "input@multiple", "input@readonly", "input@required", "input@autofocus",
        "input@formnovalidate", "iframe@seamless", "keygen@autofocus", "keygen@disabled",
        "menu@compact", "object@declare", "object@typemustmatch", "ol@compact", "ol@reversed",
        "optgroup@disabled", "option@selected", "option@disabled", "script@defer", "script@async",
        "select@multiple", "select@disabled", "select@autofocus", "select@required", "style@scoped",
        "td@nowrap", "textarea@disabled", "textarea@readonly", "textarea@autofocus",
        "textarea@required", "th@nowrap", "track@default", "ul@compact", "video@autoplay",
        "video@controls", "video@loop", "video@muted");
    // elements with an empty content model
    EMPTIES = new TokenSet("area", "base", "basefont", "br", "col", "embed", "frame",
        "hr", "img", "input", "isindex", "link", "meta", "param");
    // elements with an empty content model
    EMPTIES5 = new TokenSet("area", "base", "br", "col", "command", "embed", "hr",
        "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr");
    // formatted elements
    FORMATTEDS = new TokenSet("pre", "script", "style", "textarea", "title");
    // inline elements
    INLINES = new TokenSet("a", "abbr", "acronym", "applet", "area", "audio", "b", "basefont",
        "bdi", "bdo", "big", "br", "button", "canvas", "cite", "code", "data", "datalist", "del",
        "dfn", "em", "embed", "font", "i", "iframe", "img", "input", "ins", "kbd", "label", "link",
        "map", "mark", "math", "meta", "meter", "noscript", "object", "output", "picture",
        "progress", "q", "ruby", "s", "samp", "script", "select", "slot", "small", "span", "strike",
        "strong", "sub", "sup", "svg", "template", "textarea", "time", "tt", "u", "var", "video",
        "wbr");
    // URI attributes
    URIS = new TokenSet("a@href", "a@name", "applet@codebase", "area@href",
        "base@href", "blockquote@cite", "body@background", "button@datasrc", "del@cite",
        "div@datasrc", "form@action", "frame@longdesc", "frame@src", "head@profile",
        "iframe@longdesc", "iframe@src", "img@longdesc", "img@src", "img@usemap", "input@datasrc",
        "input@src", "input@usemap", "ins@cite", "link@href", "object@archive", "object@classid",
        "object@codebase", "object@data", "object@datasrc", "object@usemap", "q@cite", "script@for",
        "script@src", "select@datasrc", "span@datasrc", "table@datasrc", "textarea@datasrc");
  }

  /**
   * Constructor, specifying serialization options.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  HTMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, V40, V401, V50);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException {

    if(!standalone) out.print(' ');
    out.print(name);

    // don't append value for boolean attributes
    byte[] val = value;
    if(!BOOLEAN.isEmpty() || !URIS.isEmpty()) {
      final byte[] nm = concat(lc(elem.string()), ATT, lc(name));
      if(BOOLEAN.contains(nm) && eq(name, val)) return;
      // escape URI attributes
      if(escuri && URIS.contains(nm)) val = escape(val);
    }

    out.print(ATT1);
    final int vl = val.length;
    for(int v = 0; v < vl; v += cl(val, v)) {
      final int ch = cp(val, v);
      if(ch == '<' || ch == '&' && val[Math.min(v + 1, vl - 1)] == '{') {
        out.print(ch);
      } else if(ch == '"') {
        out.print(E_QUOT);
      } else if(ch == 0x9 || ch == 0xA) {
        printHex(ch);
      } else {
        printChar(ch);
      }
    }
    out.print(ATT2);
  }

  @Override
  protected void comment(final byte[] value) throws IOException {
    if(sep) indent();
    out.print(COMM_O);
    out.print(value);
    out.print(COMM_C);
  }

  @Override
  protected void pi(final byte[] name, final byte[] value) throws IOException {
    if(sep) indent();
    if(contains(value, '>')) throw SERPI.getIO();
    out.print(PI_O);
    out.print(name);
    out.print(' ');
    out.print(value);
    out.print(ELEM_C);
  }

  @Override
  protected void print(final int cp) throws IOException {
    if(script) out.print(cp);
    else if(cp > 0x7F && cp < 0xA0 && !html5) throw SERILL_X.getIO(Integer.toHexString(cp));
    else if(cp == 0xA0) out.print(E_NBSP);
    else super.print(cp);
  }

  @Override
  protected void startOpen(final QNm name) throws IOException {
    if(opened.isEmpty()) checkRoot(HTML);
    if(sep) indent();
    out.print(ELEM_O);
    out.print(name.string());
    sep = indent;
    script = SCRIPTS.contains(lc(name.local()));
    if(content && eq(lc(elem.local()), HEAD)) skip++;
  }

  @Override
  protected void finishOpen() throws IOException {
    super.finishOpen();
    printCT(false, true);
  }

  @Override
  protected void finishEmpty() throws IOException {
    if(printCT(true, true)) return;
    out.print(ELEM_C);
    final byte[] lc = lc(elem.local());
    if(html5) {
      if(EMPTIES5.contains(lc)) return;
    } else if(EMPTIES.contains(lc)) {
      final byte[] uri = nsUri(EMPTY);
      if(uri == null || uri.length == 0) return;
    }
    sep = false;
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    super.finishClose();
    script = script && !SCRIPTS.contains(lc(elem.local()));
  }

  @Override
  protected void doctype(final byte[] type) throws IOException {
    final boolean doc = docpub != null || docsys != null;
    if(doc) {
      printDoctype(type, docpub, docsys);
    } else if(html5) {
      printDoctype(type, null, null);
    }
  }

  @Override
  boolean inline() {
    return INLINES.contains(lc(closed.local())) ||
        opening && INLINES.contains(lc(elem.local())) ||
        super.inline();
  }

  @Override
  boolean suppressIndentation(final QNm qname) throws QueryIOException {
    return FORMATTEDS.contains(lc(qname.local())) || super.suppressIndentation(qname);
  }
}
