package org.basex.query.util.inspect;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains functions for inspecting XQuery modules and
 * generating XQuery documentation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
abstract class Inspect {
  /** Query context. */
  protected final QueryContext ctx;
  /** Input info. */
  protected final InputInfo info;

  /** Parsed main module. */
  protected StaticScope module;

  /**
   * Constructor.
   * @param qc query context
   * @param ii input info
   */
  protected Inspect(final QueryContext qc, final InputInfo ii) {
    ctx = qc;
    info = ii;
  }

  /**
   * Parses a module.
   * @param io input reference
   * @return query parser
   * @throws QueryException query exception
   */
  protected final QueryParser parseQuery(final IO io) throws QueryException {
    if(!io.exists()) WHICHRES.thrw(info, io);

    final QueryContext qc = new QueryContext(ctx.context);
    try {
      final String input = string(io.read());
      // parse query
      final QueryParser qp = new QueryParser(input, io.path(), qc);
      module = QueryProcessor.isLibrary(input) ? qp.parseLibrary(true) : qp.parseMain();
      return qp;
    } catch(final IOException ex) {
      throw IOERR.thrw(info, ex);
    } catch(final QueryException ex) {
      throw IOERR.thrw(info, ex);
    } finally {
      qc.close();
    }
  }

  /**
   * Creates a comment sub element.
   * @param tags map with tags
   * @param parent parent element
   */
  protected final void comment(final TokenObjMap<TokenList> tags, final FElem parent) {
    for(final byte[] key : tags) {
      for(final byte[] value : tags.get(key)) {
        try {
          final FElem elem = tag(key, parent);
          final IOContent io = new IOContent(trim(value));
          final ANode node = FNGen.parseXml(io, ctx, true);
          for(final ANode n : node.children()) elem.add(n.copy());
        } catch(final IOException ex) {
          // fallback: add string representation
          Util.debug(ex);
          elem(string(key), parent).add(trim(value));
        }
      }
    }
  }


  /**
   * Creates annotation child elements.
   * @param ann annotations
   * @param parent parent element
   * @throws QueryException query exception
   */
  protected final void annotation(final Ann ann, final FElem parent)
      throws QueryException {

    final int as = ann.size();
    for(int a = 0; a < as; a++) {
      final FElem annotation = elem("annotation", parent);
      annotation.add("name", ann.names[a].string());
      for(final Item it : ann.values[a]) {
        final FElem literal = elem("literal", annotation);
        literal.add("type", it.type.toString()).add(it.string(null));
      }
    }
  }
  /**
   * Creates a new element for the specified tag.
   * @param tag tag
   * @param parent parent element
   * @return element
   */
  protected abstract FElem tag(final byte[] tag, final FElem parent);

  /**
   * Creates an element.
   * @param name name of element
   * @param parent parent element
   * @return element node
   */
  protected abstract FElem elem(final String name, final FElem parent);
}
