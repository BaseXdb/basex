package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class serializes data as HTML.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class HTMLSerializer extends OutputSerializer {
  /** (X)HTML: elements with an empty content model. */
  static final TokenList EMPTIES = new TokenList();
  /** HTML5: elements with an empty content model. */
  static final TokenList EMPTIES5 = new TokenList();
  /** (X)HTML: URI attributes. */
  static final TokenSet URIS = new TokenSet();

  /** HTML: script elements. */
  private static final TokenList SCRIPTS = new TokenList();
  /** HTML: boolean attributes. */
  private static final TokenSet BOOLEAN = new TokenSet();

  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  HTMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, V40, V401, V50);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) throws IOException {
    print(' ');
    print(name);

    // don't append value for boolean attributes
    final byte[] nm = concat(lc(elem), COLON, lc(name));
    if(BOOLEAN.contains(nm) && eq(name, value)) return;
    // escape URI attributes
    final byte[] val = escuri && URIS.contains(nm) ? escape(value) : value;

    print(ATT1);
    final int vl = val.length;
    for(int v = 0; v < vl; v += cl(val, v)) {
      final int ch = cp(val, v);
      if(ch == '<' || ch == '&' && val[Math.min(v + 1, vl - 1)] == '{') {
        print(ch);
      } else if(ch == '"') {
        print(E_QUOT);
      } else if(ch == 0x9 || ch == 0xA) {
        hex(ch);
      } else {
        encode(ch);
      }
    }
    print(ATT2);
  }

  @Override
  protected void comment(final byte[] value) throws IOException {
    if(sep) indent();
    print(COMM_O);
    print(value);
    print(COMM_C);
  }

  @Override
  protected void pi(final byte[] name, final byte[] value) throws IOException {
    if(sep) indent();
    if(contains(value, '>')) throw SERPI.getIO();
    print(PI_O);
    print(name);
    print(' ');
    print(value);
    print(ELEM_C);
  }

  @Override
  protected void encode(final int cp) throws IOException {
    if(script) printChar(cp);
    else if(cp > 0x7F && cp < 0xA0 && !html5) throw SERILL_X.getIO(Integer.toHexString(cp));
    else if(cp == 0xA0) print(E_NBSP);
    else super.encode(cp);
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    doctype(null);
    if(sep) indent();
    print(ELEM_O);
    print(name);
    sep = indent;
    script = SCRIPTS.contains(lc(name));
    if(content && eq(lc(elem), HEAD)) ct++;
  }

  @Override
  protected void finishOpen() throws IOException {
    super.finishOpen();
    ct(false, true);
  }

  @Override
  protected void finishEmpty() throws IOException {
    if(ct(true, true)) return;
    print(ELEM_C);
    if(html5) {
      if(EMPTIES5.contains(lc(elem))) return;
    } else {
      if(EMPTIES.contains(lc(elem))) {
        final byte[] u = nsUri(EMPTY);
        if(u == null || u.length == 0) return;
      }
    }
    sep = false;
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    super.finishClose();
    script = script && !SCRIPTS.contains(lc(elem));
  }

  @Override
  boolean doctype(final byte[] type) throws IOException {
    if(lvl != 0) return false;
    if(!super.doctype(type) && html5) {
      if(sep) indent();
      print(DOCTYPE);
      if(type == null) print(HTML);
      else print(type);
      print(ELEM_C);
      if(indent) print(nl);
    }
    return true;
  }

  // HTML Serializer: cache elements
  static {
    // script elements
    SCRIPTS.add("script");
    SCRIPTS.add("style");
    // boolean attributes
    BOOLEAN.add("area:nohref");
    BOOLEAN.add("button:disabled");
    BOOLEAN.add("dir:compact");
    BOOLEAN.add("dl:compact");
    BOOLEAN.add("frame:noresize");
    BOOLEAN.add("hr:noshade");
    BOOLEAN.add("img:ismap");
    BOOLEAN.add("input:checked");
    BOOLEAN.add("input:disabled");
    BOOLEAN.add("input:readonly");
    BOOLEAN.add("menu:compact");
    BOOLEAN.add("object:declare");
    BOOLEAN.add("ol:compact");
    BOOLEAN.add("optgroup:disabled");
    BOOLEAN.add("option:selected");
    BOOLEAN.add("option:disabled");
    BOOLEAN.add("script:defer");
    BOOLEAN.add("select:multiple");
    BOOLEAN.add("select:disabled");
    BOOLEAN.add("td:nowrap");
    BOOLEAN.add("textarea:disabled");
    BOOLEAN.add("textarea:readonly");
    BOOLEAN.add("th:nowrap");
    BOOLEAN.add("ul:compact");
    // elements with an empty content model
    EMPTIES.add("area");
    EMPTIES.add("base");
    EMPTIES.add("br");
    EMPTIES.add("col");
    EMPTIES.add("embed");
    EMPTIES.add("hr");
    EMPTIES.add("img");
    EMPTIES.add("input");
    EMPTIES.add("link");
    EMPTIES.add("meta");
    EMPTIES.add("basefont");
    EMPTIES.add("frame");
    EMPTIES.add("isindex");
    EMPTIES.add("param");
    // elements with an empty content model
    EMPTIES5.add("area");
    EMPTIES5.add("base");
    EMPTIES5.add("br");
    EMPTIES5.add("col");
    EMPTIES5.add("command");
    EMPTIES5.add("embed");
    EMPTIES5.add("hr");
    EMPTIES5.add("img");
    EMPTIES5.add("input");
    EMPTIES5.add("keygen");
    EMPTIES5.add("link");
    EMPTIES5.add("meta");
    EMPTIES5.add("param");
    EMPTIES5.add("source");
    EMPTIES5.add("track");
    EMPTIES5.add("wbr");
    EMPTIES5.add("basefont");
    EMPTIES5.add("frame");
    EMPTIES5.add("isindex");
    // URI attributes
    URIS.add("a:href");
    URIS.add("a:name");
    URIS.add("applet:codebase");
    URIS.add("area:href");
    URIS.add("base:href");
    URIS.add("blockquote:cite");
    URIS.add("body:background");
    URIS.add("button:datasrc");
    URIS.add("del:cite");
    URIS.add("div:datasrc");
    URIS.add("form:action");
    URIS.add("frame:longdesc");
    URIS.add("frame:src");
    URIS.add("head:profile");
    URIS.add("iframe:longdesc");
    URIS.add("iframe:src");
    URIS.add("img:longdesc");
    URIS.add("img:src");
    URIS.add("img:usemap");
    URIS.add("input:datasrc");
    URIS.add("input:src");
    URIS.add("input:usemap");
    URIS.add("ins:cite");
    URIS.add("link:href");
    URIS.add("object:archive");
    URIS.add("object:classid");
    URIS.add("object:codebase");
    URIS.add("object:data");
    URIS.add("object:datasrc");
    URIS.add("object:usemap");
    URIS.add("q:cite");
    URIS.add("script:for");
    URIS.add("script:src");
    URIS.add("select:datasrc");
    URIS.add("span:datasrc");
    URIS.add("table:datasrc");
    URIS.add("textarea:datasrc");
  }
}
