package org.basex.api.jaxp;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.basex.BaseX;
import org.basex.core.Context;
import org.xml.sax.InputSource;

/**
 * This class provides an API for standalone XPath processing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class XPathImpl implements XPath {
  /** Variables. */
  private XPathVariableResolver variables;
  /** Functions. */
  private XPathFunctionResolver functions;
  /** XPath processor. */
  private Context context;

  /**
   * Constructor.
   */
  public XPathImpl() {
    context = new Context();
  }
  
  public void reset() { }

  public void setXPathVariableResolver(final XPathVariableResolver var) {
    variables = var;
    BaseX.notimplemented();
  }

  public XPathVariableResolver getXPathVariableResolver() {
    return variables;
  }

  public void setXPathFunctionResolver(final XPathFunctionResolver fun) {
    functions = fun;
    BaseX.notimplemented();
  }

  public XPathFunctionResolver getXPathFunctionResolver() {
    return functions;
  }

  public void setNamespaceContext(final NamespaceContext ns) {
    BaseX.notimplemented();
  }

  public NamespaceContext getNamespaceContext() {
    return null;
  }

  public XPathExpression compile(final String expr) {
    return new XPathExprImpl(expr, context);
  }

  public Object evaluate(final String expr, final Object item,
      final QName res) throws XPathExpressionException {

    return new XPathExprImpl(expr, context).evaluate(item, res);
  }

  public String evaluate(final String expr, final Object item)
      throws XPathExpressionException {

    return new XPathExprImpl(expr, context).evaluate(item);
  }

  public Object evaluate(final String expr, final InputSource source,
      final QName res) throws XPathExpressionException {

    return new XPathExprImpl(expr, context).evaluate(source, res);
  }

  public String evaluate(final String expr, final InputSource source)
      throws XPathExpressionException {

    return new XPathExprImpl(expr, context).evaluate(source);
  }
}
