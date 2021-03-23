package org.basex.query.func.xslt;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;
import org.basex.build.xml.*; // for CatalogWrapper

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
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
      return new DBNode(new IOContent(transform(qc)));
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Performs an XSL transformation.
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  final byte[] transform(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final IO in = read(0, qc), xsl = read(1, qc);
    final Options opts = toOptions(2, new Options(), qc);
    final XsltOptions xopts = toOptions(3, new XsltOptions(), qc);

    final PrintStream tmp = System.err;
    final ArrayOutput ao = new ArrayOutput();
    try {
      System.setErr(new PrintStream(ao));
      return transform(in, xsl, opts.free(), xopts, qc);
    } catch(final TransformerException ex) {
      Util.debug(ex);
      throw XSLT_ERROR_X.get(info, trim(utf8(ao.toArray(), Prop.ENCODING)));
    } catch(final IllegalArgumentException ex) {
      // Saxon raises runtime exceptions for illegal parameters
      Util.debug(ex);
      throw XSLT_ERROR_X.get(info, ex.getMessage());
    } finally {
      System.setErr(tmp);
    }
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
      } catch(final QueryIOException e) {
        e.getCause(info);
      }
    }
    if(item.type.isStringOrUntyped()) return checkPath(toToken(item));
    throw STRNOD_X_X.get(info, item.type, item);
  }

  /**
   * Performs an XSLT implementation.
   * @param in input
   * @param xsl style sheet
   * @param params parameters
   * @param xopts XSLT options
   * @param qc query context
   * @return transformed result
   * @throws TransformerException transformer exception
   */
  private static byte[] transform(final IO in, final IO xsl, final HashMap<String, String> params,
      final XsltOptions xopts, final QueryContext qc) throws TransformerException {

    final URIResolver ur = CatalogWrapper.getURIResolver(qc.context.options);

    // retrieve new or cached templates object
    Templates tmp = null;
    final StreamSource ss = xsl.streamSource();
    final String key = xopts.get(XsltOptions.CACHE) ? ss.getSystemId() : null;
    if(key != null) tmp = MAP.get(key);
    if(tmp == null) {
      // no templates object cached: create new instance
      final TransformerFactory tf = TransformerFactory.newInstance();
      // assign catalog resolver (if defined)
      if(ur != null) tf.setURIResolver(ur);
      tmp = tf.newTemplates(ss);
      if(key != null) MAP.put(key, tmp);
    }

    // create transformer, assign catalog resolver (if defined)
    final Transformer tr = tmp.newTransformer();
    if(ur != null) tr.setURIResolver(ur);

    // bind parameters
    params.forEach(tr::setParameter);

    // do transformation and return result
    final ArrayOutput ao = new ArrayOutput();
    tr.transform(in.streamSource(), new StreamResult(ao));
    return ao.finish();
  }
}
