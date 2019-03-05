package org.basex.query.func.xslt;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.basex.core.*;
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
 * @author BaseX Team 2005-19, BSD License
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
      throw XSLT_ERROR_X.get(info, trim(utf8(ao.finish(), Prop.ENCODING)));
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
   * Uses Java's XSLT implementation to perform an XSL transformation.
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

    // retrieve new or cached transformer
    final Transformer tr = transformer(xsl.streamSource(), xopts.get(XsltOptions.CACHE));
    // bind parameters
    params.forEach(tr::setParameter);

    // set URI resolver
    final CatalogWrapper cw = CatalogWrapper.get(qc.context.options.get(MainOptions.CATFILE));
    if(cw != null) tr.setURIResolver(cw.getURIResolver());

    // do transformation and return result
    final ArrayOutput ao = new ArrayOutput();
    tr.transform(in.streamSource(), new StreamResult(ao));
    return ao.finish();
  }

  /**
   * Returns a new or cached transformer instance.
   * @param ss stream source
   * @param cache caching flag
   * @return transformer
   * @throws TransformerException transformer exception
   */
  private static Transformer transformer(final StreamSource ss, final boolean cache)
      throws TransformerException {

    // system id may be null
    final String key = cache ? ss.getSystemId() : null;
    Transformer tr = null;
    if(key != null) tr = MAP.get(key);
    if(tr == null) tr = TransformerFactory.newInstance().newTransformer(ss);
    if(key != null) MAP.put(key, tr);
    return tr;
  }
}
