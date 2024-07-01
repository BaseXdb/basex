package org.basex.query.func.inspect;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.scope.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Abstract inspector class.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class Inspect {
  /** Supported documentation tags. */
  public static final byte[][] DOC_TAGS = tokens("description", "author", "version", "param",
      "return", "error", "deprecated", "see", "since");
  /** Documentation: description tag. */
  public static final byte[] DOC_DESCRIPTION = token("description");
  /** Documentation: param tag. */
  public static final byte[] DOC_PARAM = token("param");
  /** Documentation: return tag. */
  public static final byte[] DOC_RETURN = token("return");

  /** QName. */
  static final QNm Q_OCCURRENCE = new QNm("occurrence");
  /** QName. */
  static final QNm Q_TAG = new QNm("tag");
  /** QName. */
  static final QNm Q_PREFIX = new QNm("prefix");
  /** QName. */
  static final QNm Q_ARITY = new QNm("arity");
  /** QName. */
  static final QNm Q_EXTERNAL = new QNm("external");
  /** QName. */
  static final QNm Q_NAME = new QNm("name");
  /** QName. */
  static final QNm Q_URI = new QNm("uri");
  /** QName. */
  static final QNm Q_TYPE = new QNm("type");

  /** Query context. */
  final QueryContext qc;
  /** Input info (can be {@code null}). */
  final InputInfo info;

  /**
   * Constructor.
   * @param qc query context
   * @param info input info (can be {@code null})
   */
  Inspect(final QueryContext qc, final InputInfo info) {
    this.qc = qc;
    this.info = info;
  }

  /**
   * Parses a module and returns an inspection element.
   * @param content module content
   * @return inspection element
   * @throws QueryException query exception
   */
  public abstract FNode parse(IOContent content) throws QueryException;

  /**
   * Parses a module.
   * @param content module content
   * @return module
   * @throws QueryException query exception
   */
  final AModule parseModule(final IO content) throws QueryException {
    try(QueryContext qctx = new QueryContext(qc.context)) {
      return qctx.parse(content.toString(), content.path());
    } catch(final QueryException ex) {
      throw INSPECT_PARSE_X.get(info, ex);
    }
  }

  /**
   * Creates a comment sub element.
   * @param tags map with tags
   * @param parent parent element
   * @throws QueryException query exception
   */
  final void comment(final TokenObjMap<TokenList> tags, final FBuilder parent)
      throws QueryException {
    for(final byte[] tag : tags) {
      for(final byte[] value : tags.get(tag)) {
        final FBuilder elem = element(tag);
        add(value, elem);
        parent.add(elem);
      }
    }
  }

  /**
   * Creates annotation child elements.
   * @param anns annotations
   * @param parent parent element
   * @param uri include uri
   * @throws QueryException query exception
   */
  final void annotation(final AnnList anns, final FBuilder parent, final boolean uri)
      throws QueryException {

    for(final Ann ann : anns) {
      final FBuilder annotation = element("annotation");
      final QNm name = ann.name();
      annotation.add(Q_NAME, name.string());
      if(uri) annotation.add(Q_URI, name.uri());

      for(final Item arg : ann.value()) {
        annotation.add(element("literal").add(Q_TYPE, arg.type).add(arg.string(null)));
      }
      parent.add(annotation);
    }
  }

  /**
   * Creates an element.
   * @param name element name
   * @return element
   */
  protected abstract FBuilder element(byte[] name);

  /**
   * Creates an element.
   * @param name name of element
   * @return element node
   */
  protected abstract FBuilder element(String name);

  /**
   * Parses a string as XML and adds the resulting nodes to the specified parent.
   * @param value string to parse
   * @param elem element
   * @throws QueryException query exception
   */
  public static void add(final byte[] value, final FBuilder elem) throws QueryException {
    try {
      if(contains(value, '<')) {
        // contains angle brackets: add as XML structure
        final MainOptions mopts = new MainOptions();
        final ANode node = new DBNode(new XMLParser(new IOContent(value), mopts, true));
        for(final ANode child : node.childIter()) {
          elem.add(child.copy(mopts, null));
        }
      } else {
        // add as single text node
        elem.add(new FTxt(value));
      }
    } catch(final IOException ex) {
      // fallback: add string representation
      Util.debug(ex);
      elem.add(value);
    }
  }

  /**
   * Returns a value for the specified parameter or {@code null}.
   * @param doc documentation
   * @param name parameter name
   * @return documentation of specified variable or {@code null}
   */
  public static byte[] doc(final TokenObjMap<TokenList> doc, final byte[] name) {
    final TokenList params = doc != null ? doc.get(DOC_PARAM) : null;
    if(params != null) {
      for(final byte[] param : params) {
        final int vl = param.length;
        final int s = startsWith(param, '$') ? 1 : 0;
        for(int v = s; v < vl; v++) {
          if(!ws(param[v])) continue;
          if(!eq(substring(param, s, v), name)) break;
          return trim(substring(param, v + 1, vl));
        }
      }
    }
    return null;
  }
}
