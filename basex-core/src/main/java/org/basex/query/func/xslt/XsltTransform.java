package org.basex.query.func.xslt;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import javax.xml.transform.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author Liam Quin
 */
public class XsltTransform extends StandardFunc {
  /** XSLT Options. */
  public static final class XsltOptions extends Options {
    /** Cache flag. */
    public static final BooleanOption CACHE = new BooleanOption("cache", false);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XsltResult result = new XsltResult(EMPTY, qc.context.options);
    transform(result, null, qc);
    return result.node();
  }

  /**
   * Performs an XSL transformation. Errors are raised, or added to the report.
   * @param result transformation result
   * @param report report builder (can be {@code null})
   * @param qc query context
   * @throws QueryException query exception
   */
  final void transform(final Result result, final XsltReport report, final QueryContext qc)
      throws QueryException {

    final Source input = source(arg(0), qc);
    final IO stylesheet = read(arg(1), qc);
    final HashMap<String, String> arguments = toOptions(arg(2), qc);
    final XsltOptions options = toOptions(arg(3), new XsltOptions(), qc);

    final String error = Xslt.transform(stylesheet, input, result,
        options.get(XsltOptions.CACHE), true, qc, tr -> {
          if(report != null) report.register(tr);
          arguments.forEach(tr::setParameter);
        });
    if(error == null) return;
    if(report == null) throw XSLT_ERROR_X.get(info, error);
    report.addError(Str.get(error));
  }

  /**
   * Evaluates an expression (node, URI string) to an input source.
   * @param expr expression
   * @param qc query context
   * @return source
   * @throws QueryException query exception
   */
  private Source source(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = toNodeOrAtomItem(expr, false, qc);
    return item instanceof final XNode node ? Xslt.source(node, sc().baseURI(), info) :
      io(item).streamSource();
  }

  /**
   * Evaluates an expression (node, URI string) to an input reference.
   * @param expr expression
   * @param qc query context
   * @return input reference
   * @throws QueryException query exception
   */
  private IO read(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = toNodeOrAtomItem(expr, false, qc);
    return item instanceof final XNode node ? Xslt.io(node, sc().baseURI(), info) : io(item);
  }

  /**
   * Converts an item (URI string, string content) to an input reference.
   * @param item item
   * @return input reference
   * @throws QueryException query exception
   */
  private IO io(final Item item) throws QueryException {
    final Type type = item.type;
    if(type.isStringOrUntyped()) return toIO(toString(item), true);
    throw STRNOD_X_X.get(info, type, item);
  }
}
