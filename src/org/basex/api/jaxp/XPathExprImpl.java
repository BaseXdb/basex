package org.basex.api.jaxp;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.basex.api.dom.BXNList;
import org.basex.core.Context;
import org.basex.core.proc.Check;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xquery.item.DBNode;
import org.basex.util.Token;
import org.xml.sax.InputSource;

/**
 * This class provides an API for standalone XPath processing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class XPathExprImpl implements XPathExpression {
  /** Query context. */
  private Context context = new Context();
  /** Query context. */
  private XPathProcessor xproc;

  /**
   * Constructor.
   * @param expr query expression
   */
  public XPathExprImpl(final String expr) {
    xproc = new XPathProcessor(expr);
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

    final Check check = new Check(is.getSystemId());
    if(check.execute(context)) return finish(eval(), res);
    throw new XPathExpressionException(check.info());
  }
  
  /**
   * Evaluates the current query.
   * @return result
   * @throws XPathExpressionException xpath exception
   */
  private Item eval() throws XPathExpressionException {
    try {
      return (Item) xproc.query(context.current());
    } catch(QueryException ex) {
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
  private Object finish(final Item item, final QName res)
      throws XPathExpressionException {
    
    Nod nodes = item instanceof Nod ? (Nod) item : null;
    if(res == XPathConstants.NODESET || res == XPathConstants.NODE) {
      if(nodes == null) throw new XPathExpressionException(
          "Result can't be cast to a nodeset");
      
      if(nodes.size == 0) return null;
      final Data data = nodes.data;
      return res == XPathConstants.NODESET ? new BXNList(nodes) :
        new DBNode(data, nodes.nodes[0]).java();
    }

    if(nodes != null) nodes.size = 1;
    if(res == XPathConstants.NUMBER) return Double.valueOf(item.num());
    if(res == XPathConstants.STRING) return Token.string(item.str());
    return Boolean.valueOf(item.bool());
  }
}

