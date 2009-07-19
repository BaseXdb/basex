package org.basex.api.jaxp;

import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.basex.api.dom.BXNList;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.proc.CreateDB;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.DBNode;
import org.xml.sax.InputSource;

/**
 * This class provides an API for standalone XPath processing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BXPathExpression implements XPathExpression {
  /** Query context. */
  private final Context context = new Context();
  /** Query context. */
  private final QueryProcessor xproc;

  /**
   * Constructor.
   * @param expr query expression
   */
  public BXPathExpression(final String expr) {
    xproc = new QueryProcessor(expr, context.current());
  }

  public String evaluate(final Object item) throws XPathExpressionException {
    return evaluate(item, XPathConstants.STRING).toString();
  }

  public String evaluate(final InputSource is) throws XPathExpressionException {
    return evaluate(is, XPathConstants.STRING).toString();
  }

  public Object evaluate(final Object item, final QName res)
      throws XPathExpressionException {
    return finish(eval(), res);
  }

  public Object evaluate(final InputSource is, final QName res)
      throws XPathExpressionException {

    final Process check = new CreateDB(is.getSystemId());
    if(check.execute(context)) return finish(eval(), res);
    throw new XPathExpressionException(check.info());
  }

  /**
   * Evaluates the current query.
   * @return result
   * @throws XPathExpressionException xpath exception
   */
  private Result eval() throws XPathExpressionException {
    try {
      return xproc.query();
    } catch(final QueryException ex) {
      throw new XPathExpressionException(ex);
    }
  }

  /**
   * Finishes the query result.
   * @param item input
   * @param res result type
   * @return result
   * @throws XPathExpressionException xpath exception
   */
  private Object finish(final Result item, final QName res)
      throws XPathExpressionException {

    final Nodes nodes = item instanceof Nodes ? (Nodes) item : null;
    if(res == XPathConstants.NODESET || res == XPathConstants.NODE) {
      if(nodes == null) throw new XPathExpressionException(
          "Result can't be cast to a nodeset");

      if(nodes.size() == 0) return null;
      final Data data = nodes.data;
      return res == XPathConstants.NODESET ? new BXNList(nodes) :
        new DBNode(data, nodes.nodes[0]).java();
    }

    try {
      CachedOutput out = new CachedOutput();
      item.serialize(new XMLSerializer(out));
      String val = out.toString();
      if(res == XPathConstants.NUMBER) return Double.valueOf(val);
      if(res == XPathConstants.STRING) return val;
      if(res == XPathConstants.BOOLEAN) return Boolean.valueOf(val);
      throw new XPathExpressionException("Invalid type: " + res);
    } catch(final IOException ex) {
      throw new XPathExpressionException(ex);
    }
  }
}
