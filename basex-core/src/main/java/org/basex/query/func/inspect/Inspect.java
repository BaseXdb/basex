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
 * @author BaseX Team 2005-21, BSD License
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

  /** Query context. */
  final QueryContext qc;
  /** Input info. */
  final InputInfo info;

  /** Parsed main module. */
  AModule module;

  /**
   * Constructor.
   * @param qc query context
   * @param info input info
   */
  Inspect(final QueryContext qc, final InputInfo info) {
    this.qc = qc;
    this.info = info;
  }

  /**
   * Parses a module and returns an inspection element.
   * @param io input reference
   * @return inspection element
   * @throws QueryException query exception
   */
  public abstract FElem parse(IO io) throws QueryException;

  /**
   * Parses a module.
   * @param io input reference
   * @return query parser
   * @throws QueryException query exception
   */
  final QueryParser parseQuery(final IO io) throws QueryException {
    try(QueryContext qctx = new QueryContext(qc.context)) {
      final String input = string(io.read());
      // parse query
      final QueryParser qp = new QueryParser(input, io.path(), qctx, null);
      module = QueryProcessor.isLibrary(input) ? qp.parseLibrary(true) : qp.parseMain();
      return qp;
    } catch(final IOException | QueryException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Creates a comment sub element.
   * @param tags map with tags
   * @param parent parent element
   */
  final void comment(final TokenObjMap<TokenList> tags, final FElem parent) {
    for(final byte[] tag : tags) {
      for(final byte[] name : tags.get(tag)) add(name, elem(tag, parent));
    }
  }

  /**
   * Creates annotation child elements.
   * @param anns annotations
   * @param parent parent element
   * @param uri include uri
   * @throws QueryException query exception
   */
  final void annotation(final AnnList anns, final FElem parent, final boolean uri)
      throws QueryException {

    for(final Ann ann : anns) {
      final FElem annotation = elem("annotation", parent);
      final QNm name = ann.name();
      annotation.add("name", name.string());
      if(uri) annotation.add("uri", name.uri());

      for(final Item arg : ann.args()) {
        elem("literal", annotation).add("type", arg.type.toString()).add(arg.string(null));
      }
    }
  }

  /**
   * Creates a new element.
   * @param name element name
   * @param parent parent element
   * @return element
   */
  protected abstract FElem elem(byte[] name, FElem parent);

  /**
   * Creates an element.
   * @param name name of element
   * @param parent parent element
   * @return element node
   */
  protected abstract FElem elem(String name, FElem parent);

  /**
   * Parses a string as XML and adds the resulting nodes to the specified parent.
   * @param value string to parse
   * @param elem element
   */
  public static void add(final byte[] value, final FElem elem) {
    try {
      final MainOptions mopts = MainOptions.get();
      if(contains(value, '<')) {
        // contains angle brackets: add as XML structure
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
