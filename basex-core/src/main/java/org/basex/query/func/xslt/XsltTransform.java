package org.basex.query.func.xslt;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Functions for performing XSLT transformations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class XsltTransform extends XsltFn {
  /** Element: parameters. */
  private static final QNm Q_PARAMETERS = QNm.get("parameters", XSLT_URI);

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
    final IO in = read(exprs[0], qc);
    final IO xsl = read(exprs[1], qc);
    final Options opts = toOptions(2, Q_PARAMETERS, new Options(), qc);

    final PrintStream tmp = System.err;
    final ArrayOutput ao = new ArrayOutput();
    try {
      System.setErr(new PrintStream(ao));
      return transform(in, xsl, opts.free());
    } catch(final TransformerException ex) {
      throw BXSL_ERROR_X.get(info, trim(utf8(ao.finish(), Prop.ENCODING)));
    } finally {
      System.setErr(tmp);
    }
  }

  /**
   * Returns an input reference (possibly cached) to the specified input.
   * @param ex expression to be evaluated
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  private IO read(final Expr ex, final QueryContext qc) throws QueryException {
    final Item it = toItem(ex, qc);
    if(it instanceof ANode) {
      try {
        final IO io = new IOContent(it.serialize(SerializerOptions.get(false)).finish());
        io.name(string(((ANode) it).baseURI()));
        return io;
      } catch(final QueryIOException e) {
        e.getCause(info);
      }
    }
    if(it.type.isStringOrUntyped()) return checkPath(it, qc);
    throw STRNOD_X_X.get(info, it.type, it);
  }

  /**
   * Uses Java's XSLT implementation to perform an XSL transformation.
   * @param in input
   * @param xsl style sheet
   * @param par parameters
   * @return transformed result
   * @throws TransformerException transformer exception
   */
  private static byte[] transform(final IO in, final IO xsl, final HashMap<String, String> par)
      throws TransformerException {

    // create transformer
    final TransformerFactory tc = TransformerFactory.newInstance();
    final Transformer tr =  tc.newTransformer(xsl.streamSource());

    // bind parameters
    for(final Entry<String, String> entry : par.entrySet())
      tr.setParameter(entry.getKey(), entry.getValue());

    // do transformation and return result
    final ArrayOutput ao = new ArrayOutput();
    tr.transform(in.streamSource(), new StreamResult(ao));
    return ao.finish();
  }
}
