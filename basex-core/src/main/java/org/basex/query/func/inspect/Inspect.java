package org.basex.query.func.inspect;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Inspect function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Inspect {
  /** Query context. */
  final QueryContext qc;
  /** Input info. */
  final InputInfo info;

  /** Parsed main module. */
  StaticScope module;

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
  public abstract FElem parse(final IO io) throws QueryException;

  /**
   * Parses a module.
   * @param io input reference
   * @return query parser
   * @throws QueryException query exception
   */
  final QueryParser parseQuery(final IO io) throws QueryException {
    final QueryContext qctx = new QueryContext(qc.context);
    try {
      final String input = string(io.read());
      // parse query
      final QueryParser qp = new QueryParser(input, io.path(), qctx, null);
      module = QueryProcessor.isLibrary(input) ? qp.parseLibrary(true) : qp.parseMain();
      return qp;
    } catch(final IOException | QueryException ex) {
      throw IOERR_X.get(info, ex);
    } finally {
      qctx.close();
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
   * @param ann annotations
   * @param parent parent element
   * @param uri include uri
   * @throws QueryException query exception
   */
  final void annotation(final Ann ann, final FElem parent, final boolean uri)
      throws QueryException {

    final int as = ann.size();
    for(int a = 0; a < as; a++) {
      final FElem annotation = elem("annotation", parent);
      annotation.add("name", ann.names[a].string());
      if(uri) annotation.add("uri", ann.names[a].uri());
      for(final Item it : ann.values[a]) {
        final FElem literal = elem("literal", annotation);
        literal.add("type", it.type.toString()).add(it.string(null));
      }
    }
  }

  /**
   * Creates a new element.
   * @param name element name
   * @param parent parent element
   * @return element
   */
  protected abstract FElem elem(final byte[] name, final FElem parent);

  /**
   * Creates an element.
   * @param name name of element
   * @param parent parent element
   * @return element node
   */
  protected abstract FElem elem(final String name, final FElem parent);

  /**
   * Parses a string as XML and adds the resulting nodes to the specified parent.
   * @param value string to parse
   * @param elem element
   */
  public static void add(final byte[] value, final FElem elem) {
    try {
      final ANode node = new DBNode(new IOContent(value));
      for(final ANode n : node.children()) elem.add(n.copy());
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
   * @return documentation of specified variable
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
