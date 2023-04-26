package org.basex.query.func.xslt;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 * @author Liam Quin
 */
public class XsltTransform extends XsltFn {
  /** XSLT Options. */
  public static final class XsltOptions extends Options {
    /** Cache flag. */
    public static final BooleanOption CACHE = new BooleanOption("cache", false);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      final Str result = (Str) transform(qc, true);
      return new DBNode(new IOContent(result.string()));
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Performs an XSL transformation.
   * @param qc query context
   * @param simple simple processing (no report generation)
   * @return item (map or string)
   * @throws QueryException query exception
   */
  final Item transform(final QueryContext qc, final boolean simple) throws QueryException {
    final IO in = read(arg(0), qc), xsl = read(arg(1), qc);
    final HashMap<String, String> params = toOptions(arg(2), qc);
    final XsltOptions options = toOptions(arg(3), new XsltOptions(), true, qc);

    final ArrayOutput result = new ArrayOutput();
    final PrintStream errPS = System.err;
    final ArrayOutput err = new ArrayOutput();
    final XsltReport xr = simple ? null : new XsltReport(qc);
    try {
      // redirect errors
      System.setErr(new PrintStream(err));
      final StreamSource ss = xsl.streamSource();
      final String key = options.get(XsltOptions.CACHE) ? ss.getSystemId() : null;

      // retrieve new or cached templates object
      Templates templates = key != null ? MAP.get(key) : null;
      final URIResolver ur = Resolver.uris(qc.context.options);
      if(templates == null) {
        // no templates object cached: create new instance
        final TransformerFactory tf = TransformerFactory.newInstance();
        // assign catalog resolver (if defined)
        if(ur != null) tf.setURIResolver(ur);
        templates = tf.newTemplates(ss);
        if(key != null) MAP.put(key, templates);
      }

      // create transformer, assign catalog resolver (if defined)
      final Transformer tr = templates.newTransformer();
      if(ur != null) tr.setURIResolver(ur);

      // bind parameters
      params.forEach(tr::setParameter);

      // do transformation and return result
      if(simple) {
        tr.transform(in.streamSource(), new StreamResult(result));
        return Str.get(result.finish());
      }

      xr.register(tr);
      tr.transform(in.streamSource(), new StreamResult(result));
      xr.addMessage();
    } catch(final IllegalArgumentException ex) {
      // Saxon raises runtime exceptions for illegal parameters
      if(simple) throw XSLT_ERROR_X.get(info, ex);
      xr.addError(Str.get(Util.message(ex)));
    } catch(final TransformerException | TransformerFactoryConfigurationError ex) {
      Util.debug(ex);
      // catch transformation errors, throw them again or add them to report
      final StringList list = new StringList();
      final Consumer<String> add = string -> {
        final String normalized = string != null ? string.replaceAll("\\s+", " ").trim() : "";
        if(!normalized.isEmpty()) list.addUnique(normalized);
      };
      for(Throwable th = ex; th != null; th = th.getCause()) add.accept(th.getMessage());
      try {
        add.accept(new String(err.toArray(), Prop.ENCODING));
      } catch(final Exception e) {
        Util.debug(e);
        add.accept(e.getMessage());
      }

      final String error = String.join("; ", list.reverse().finish());
      if(simple) throw XSLT_ERROR_X.get(info, error);
      xr.addError(Str.get(error));
      xr.addMessage();
    } finally {
      System.setErr(errPS);
    }
    xr.addResult(result.finish());
    return xr.finish();
  }

  /**
   * Evaluates an expression (node, URI string) to a input reference.
   * @param expr expression
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  private IO read(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = toNodeOrAtomItem(expr, qc);
    if(item instanceof ANode) {
      try {
        final IO io = new IOContent(item.serialize().finish());
        io.name(string(((ANode) item).baseURI()));
        return io;
      } catch(final QueryIOException ex) {
        throw ex.getCause(info);
      }
    }
    final Type type = item.type;
    if(type.isStringOrUntyped()) return toIO(toString(item));
    throw STRNOD_X_X.get(info, type, item);
  }
}
