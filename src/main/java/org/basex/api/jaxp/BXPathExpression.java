package org.basex.api.jaxp;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.basex.api.dom.BXNList;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.DBNode;
import org.xml.sax.InputSource;

/**
 * This class provides an API for standalone XPath processing.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class BXPathExpression implements XPathExpression {
  /** Query context. */
  private final Context context = new Context();
  /** Query context. */
  private final QueryProcessor xproc;

  /**
   * Constructor.
   * @param qu query
   */
  protected BXPathExpression(final String qu) {
    xproc = new QueryProcessor(qu, context);
  }

  @Override
  public String evaluate(final Object item) throws XPathExpressionException {
    return evaluate(item, XPathConstants.STRING).toString();
  }

  @Override
  public String evaluate(final InputSource is) throws XPathExpressionException {
    return evaluate(is, XPathConstants.STRING).toString();
  }

  @Override
  public Object evaluate(final Object item, final QName res)
      throws XPathExpressionException {
    return finish(execute(), res);
  }

  @Override
  public Object evaluate(final InputSource is, final QName res)
      throws XPathExpressionException {

    try {
      new CreateDB(is.getSystemId()).execute(context);
      return finish(execute(), res);
    } catch(final BaseXException ex) {
      throw new XPathExpressionException(ex.getMessage());
    }
  }

  /**
   * Executes the current query.
   * @return result
   * @throws XPathExpressionException xpath exception
   */
  private Result execute() throws XPathExpressionException {
    try {
      return xproc.execute();
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
        new DBNode(data, nodes.list[0]).toJava();
    }

    try {
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = Serializer.get(ao);
      item.serialize(ser);
      ser.close();
      final String val = ao.toString();
      if(res == XPathConstants.NUMBER) return Double.valueOf(val);
      if(res == XPathConstants.STRING) return val;
      if(res == XPathConstants.BOOLEAN) return Boolean.valueOf(val);
      throw new XPathExpressionException("Invalid type: " + res);
    } catch(final IOException ex) {
      throw new XPathExpressionException(ex);
    }
  }
}
