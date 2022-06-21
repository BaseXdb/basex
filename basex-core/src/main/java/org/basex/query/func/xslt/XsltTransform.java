package org.basex.query.func.xslt;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.Util;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
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
    final IO in = read(0, qc), xsl = read(1, qc);
    final Options opts = toOptions(2, new Options(), qc);
    final XsltOptions xopts = toOptions(3, new XsltOptions(), qc);

    final ArrayOutput result = new ArrayOutput();
    final PrintStream errPS = System.err;
    final ArrayOutput err = new ArrayOutput();
    final XsltReport xr = simple ? null : new XsltReport(qc);
    try {
      // redirect errors
      System.setErr(new PrintStream(err));
      final StreamSource ss = xsl.streamSource();
      final String key = xopts.get(XsltOptions.CACHE) ? ss.getSystemId() : null;

      // retrieve new or cached templates object
      Templates templates = key != null ? MAP.get(key) : null;
      URIResolver ur = Resolver.uris(qc.context.options);
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
      opts.free().forEach(tr::setParameter);

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
    } catch(final TransformerException ex) {
      Util.debug(ex);
      // catch transformation errors, throw them again or add them to report
      byte[] error = trim(utf8(err.toArray(), Prop.ENCODING));
      if(error.length == 0) {
        Throwable th = ex;
        while(th.getCause() != null) th = th.getCause();
        error = token(th.getLocalizedMessage());
      }
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
   * Returns an input reference (possibly cached) to the specified input.
   * @param i index of argument
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  private IO read(final int i, final QueryContext qc) throws QueryException {
    final Item item = toNodeOrAtomItem(i, qc);
    if(item instanceof ANode) {
      try {
        final IO io = new IOContent(item.serialize(SerializerMode.NOINDENT.get()).finish());
        io.name(string(((ANode) item).baseURI()));
        return io;
      } catch(final QueryIOException ex) {
        throw ex.getCause(info);
      }
    }
    if(item.type.isStringOrUntyped()) return toIO(toToken(item));
    throw STRNOD_X_X.get(info, item.type, item);
  }
}
