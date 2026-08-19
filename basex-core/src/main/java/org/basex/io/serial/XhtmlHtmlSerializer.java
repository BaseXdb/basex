package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;
import org.basex.util.http.*;

/**
 * This class contains the common behavior of XHTML and HTML serializers.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
abstract class XhtmlHtmlSerializer extends MarkupSerializer {
  /** (X)HTML: elements with an empty content model. */
  static final TokenSet EMPTIES = new TokenSet("area", "base", "basefont", "br", "col", "embed",
      "frame", "hr", "img", "input", "isindex", "link", "meta", "param");
  /** HTML5: elements with an empty content model. */
  static final TokenSet EMPTIES5 = new TokenSet("area", "base", "br", "col", "command", "embed",
      "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr");
  /** (X)HTML: formatted elements. */
  static final TokenSet FORMATTEDS = new TokenSet("pre", "script", "style", "textarea", "title");
  /** (X)HTML: inline elements. */
  static final TokenSet INLINES = new TokenSet("a", "abbr", "acronym", "b", "bdo", "big", "br",
      "button", "cite", "code", "del", "dfn", "em", "i", "img", "input", "ins", "kbd", "label",
      "map", "object", "q", "s", "samp", "script", "select", "small", "span", "strike", "strong",
      "sub", "sup", "textarea", "tt", "u", "var");
  /** HTML5: inline elements. */
  static final TokenSet INLINES5 = new TokenSet("a", "abbr", "area", "audio", "b", "bdi", "bdo",
      "br", "button", "canvas", "cite", "code", "data", "datalist", "del", "dfn", "em", "embed",
      "i", "iframe", "img", "input", "ins", "kbd", "keygen", "label", "map", "mark", "math",
      "meter", "noscript", "object", "output", "picture", "progress", "q", "rp", "rt", "ruby",
      "s", "samp", "script", "select", "slot", "small", "span", "strong", "sub", "sup", "svg",
      "template", "textarea", "time", "u", "var", "video", "wbr");
  /** (X)HTML: URI attributes. */
  static final TokenSet URIS = new TokenSet("a@href", "a@name", "applet@codebase", "area@href",
      "base@href", "blockquote@cite", "body@background", "button@datasrc", "del@cite",
      "div@datasrc", "form@action", "frame@longdesc", "frame@src", "head@profile",
      "iframe@longdesc", "iframe@src", "img@longdesc", "img@src", "img@usemap", "input@datasrc",
      "input@src", "input@usemap", "ins@cite", "link@href", "object@archive", "object@classid",
      "object@codebase", "object@data", "object@datasrc", "object@usemap", "q@cite", "script@for",
      "script@src", "select@datasrc", "span@datasrc", "table@datasrc", "textarea@datasrc");

  /** HTML method flag ({@code false} for the XHTML method). */
  final boolean htmlMethod;
  /** HTML5 flag. */
  final boolean html5;
  /** Version-dependent rules. */
  final HtmlRules rules;

  /** Media type. */
  private final String media;
  /** Namespace bindings that were discarded by prefix normalization. */
  private final TokenObjectMap<byte[]> discarded = new TokenObjectMap<>();

  /**
   * Rules that depend on the requested HTML version.
   *
   * @param empties elements with an empty content model
   * @param inlines inline elements
   * @param caseless caseless comparison of element names
   */
  record HtmlRules(TokenSet empties, TokenSet inlines, boolean caseless) { }

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @param html serialize the result as HTML
   * @param versions supported versions
   * @throws IOException I/O exception
   */
  protected XhtmlHtmlSerializer(final OutputStream os, final SerializerOptions sopts,
      final boolean html, final String... versions) throws IOException {

    super(os, sopts, !html, versions);

    htmlMethod = html;
    // requested HTML version: 'html-version', 'version' (HTML method only), 5.0 by default
    final String version = html ? sopts.get(VERSION) : "";
    final String requested = !htmlVersion.isEmpty() ? htmlVersion :
        version.isEmpty() ? V50 : version;
    html5 = requested.equals(V50);
    rules = new HtmlRules(html5 ? EMPTIES5 : EMPTIES, html5 ? INLINES5 : INLINES,
        html5 || html);
    media = sopts.get(MEDIA_TYPE);
  }

  /**
   * Prints the content type declaration.
   * @param empty empty flag
   * @return {@code true} if declaration was printed
   * @throws IOException I/O exception
   */
  protected final boolean printCT(final boolean empty) throws IOException {
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
    out.print(htmlMethod ? ELEM_C : ELEM_SC);
    level--;
    if(empty) finishClose();
    return true;
  }

  /**
   * Returns the lookup name of an element that is serialized as an HTML element.
   * @param name element name
   * @return local name, or {@code null} if the element is not serialized as an HTML element
   */
  abstract byte[] htmlName(QNm name);

  @Override
  protected final QNm elementName(final QNm name) {
    return mustRewrite(name) ? new QNm(name.local(), name.uri()) : name;
  }

  @Override
  protected final void adjustNamespaces(final QNm name) {
    // prefix normalization: namespace nodes are pruned per element, not in a preceding pass
    if(html5) {
      // discard bindings of non-empty prefixes to XHTML, MathML and SVG
      for(int i = namespaces.size() - 1; i >= 0; --i) {
        final Att ns = namespaces.get(i);
        final byte[] prefix = ns.name();
        if(prefix.length != 0 && eq(ns.uri(), XHTML_URI, MATHML_URI, SVG_URI) &&
            !attributePrefix(prefix)) {
          discarded.put(prefix, ns.uri());
          namespaces.remove(i);
        }
      }
      // restore bindings that are still referenced by attribute names
      if(!discarded.isEmpty()) {
        for(final Att att : attributes) {
          final byte[] prefix = prefix(att.name()), uri = discarded.get(prefix);
          if(uri != null && nsUri(prefix) == null && !declaredPrefix(prefix)) {
            addNamespace(prefix, uri);
          }
        }
      }
    }
    if(mustRewrite(name)) {
      for(int i = namespaces.size() - 1; i >= 0; --i) {
        if(namespaces.get(i).name().length == 0) namespaces.remove(i);
      }
      addNamespace(EMPTY, name.uri());
    }
  }

  @Override
  final boolean inline() {
    // namespaces are ignored: the inline elements of HTML5 include svg and math
    final TokenSet inlines = rules.inlines();
    return inlines.contains(localName(closed)) || opening && inlines.contains(localName(elem)) ||
        super.inline();
  }

  @Override
  final boolean suppressIndentation(final QNm qname) throws QueryIOException {
    return isElement(FORMATTEDS, qname) || super.suppressIndentation(qname);
  }

  /**
   * Checks if a token set contains the specified element.
   * @param elements element names
   * @param name element name
   * @return result of check
   */
  final boolean isElement(final TokenSet elements, final QNm name) {
    final byte[] local = htmlName(name);
    return local != null && elements.contains(local);
  }

  /**
   * Returns the name under which an element is registered in the (X)HTML element sets.
   * @param name element name
   * @return local name
   */
  final byte[] localName(final QNm name) {
    return rules.caseless() ? lc(name.local()) : name.local();
  }

  /**
   * Returns the key under which an attribute of the current element is registered.
   * @param name attribute name
   * @return key, or {@code null} if the element is not serialized as an HTML element
   */
  final byte[] attributeKey(final byte[] name) {
    final byte[] local = htmlName(elem);
    return local == null ? null : concat(local, AT, lc(name));
  }

  /**
   * Announces a head element to which a content type declaration will be added.
   */
  final void checkHead() {
    if(content && eq(htmlName(elem), HEAD)) skip++;
  }

  /**
   * Checks if the namespace prefix of an element name must be rewritten.
   * @param name name to be checked
   * @return result of check
   */
  private boolean mustRewrite(final QNm name) {
    return html5 && name.hasPrefix() && eq(name.uri(), XHTML_URI, MATHML_URI, SVG_URI);
  }

  /**
   * Checks if a prefix occurs in the name of an attribute of the current element.
   * @param prefix namespace prefix
   * @return result of check
   */
  private boolean attributePrefix(final byte[] prefix) {
    for(final Att att : attributes) {
      if(eq(prefix(att.name()), prefix)) return true;
    }
    return false;
  }

  /**
   * Checks if a prefix is bound by the current element.
   * @param prefix namespace prefix
   * @return result of check
   */
  private boolean declaredPrefix(final byte[] prefix) {
    for(final Att ns : namespaces) {
      if(eq(ns.name(), prefix)) return true;
    }
    return false;
  }
}
