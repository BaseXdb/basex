package org.basex.api.jaxp;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.basex.core.Main;
import org.xml.sax.InputSource;

/**
 * This class provides an API for standalone XPath processing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BXPath implements XPath {
  /** Variables. */
  private XPathVariableResolver variables;
  /** Functions. */
  private XPathFunctionResolver functions;

  public void reset() { }

  public void setXPathVariableResolver(final XPathVariableResolver var) {
    variables = var;
    Main.notimplemented();
  }

  public XPathVariableResolver getXPathVariableResolver() {
    return variables;
  }

  public void setXPathFunctionResolver(final XPathFunctionResolver fun) {
    functions = fun;
    Main.notimplemented();
  }

  public XPathFunctionResolver getXPathFunctionResolver() {
    return functions;
  }

  public void setNamespaceContext(final NamespaceContext ns) {
    Main.notimplemented();
  }

  public NamespaceContext getNamespaceContext() {
    return null;
  }

  public XPathExpression compile(final String expr) {
    return new BXPathExpression(expr);
  }

  public Object evaluate(final String expr, final Object item,
      final QName res) throws XPathExpressionException {

    return new BXPathExpression(expr).evaluate(item, res);
  }

  public String evaluate(final String expr, final Object item)
      throws XPathExpressionException {

    return new BXPathExpression(expr).evaluate(item);
  }

  public Object evaluate(final String expr, final InputSource source,
      final QName res) throws XPathExpressionException {

    return new BXPathExpression(expr).evaluate(source, res);
  }

  public String evaluate(final String expr, final InputSource source)
      throws XPathExpressionException {

    return new BXPathExpression(expr).evaluate(source);
  }
}
