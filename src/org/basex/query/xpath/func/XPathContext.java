package org.basex.query.xpath.func;

import static org.basex.query.xpath.XPText.*;

import org.basex.query.QueryException;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.Num;
import org.basex.query.xpath.values.Item;
import org.basex.util.Token;

/**
 * Global expression context.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class XPathContext {
  /** HashMap with XPath functions. */
  private XPathIndex<Class<? extends Func>> functions =
    new XPathIndex<Class<? extends Func>>();
  /** HashMap with XPath variables. */
  private XPathIndex<Item> variables =
    new XPathIndex<Item>();
  /** Singleton instance. */
  private static XPathContext instance;

  /**
   * Returns single instance of this class.
   * @return class instance
   */
  public static XPathContext get() {
    if(instance == null) instance = new XPathContext();
    return instance;
  }
  
  /**
   * Constructor, registering XPath functions.
   */
  private XPathContext() {
    addFunction("boolean", BooleanFunc.class);
    addFunction("ceiling", Ceiling.class);
    addFunction("concat", Concat.class);
    addFunction("contains", Contains.class);
    addFunction("count", Count.class);
    addFunction("doc", Doc.class);
    addFunction("false", False.class);
    addFunction("floor", Floor.class);
    addFunction("id", Id.class);
    addFunction("lang", Lang.class);
    addFunction("last", Last.class);
    addFunction("local-name", LocalName.class);
    addFunction("name", Name.class);
    addFunction("namespace-uri", NamespaceURI.class);
    addFunction("normalize-space", NormalizeSpace.class);
    addFunction("not", Not.class);
    addFunction("number", NumFunc.class);
    addFunction("position", Position.class);
    addFunction("round", Round.class);
    addFunction("starts-with", StartsWith.class);
    addFunction("string", StringFunc.class);
    addFunction("string-length", StringLength.class);
    addFunction("substring", SubStr.class);
    addFunction("substring-after", SubstringAfter.class);
    addFunction("substring-before", SubstringBefore.class);
    addFunction("sum", Sum.class);
    addFunction("translate", Translate.class);
    addFunction("true", True.class);
    // BaseX specific functions
    addFunction(ContainsLC.NAME, ContainsLC.class);
    addFunction("basex:distinct", Distinct.class);
    addFunction("basex:sort", Sort.class);
    addFunction("basex:random", Random.class);
    addFunction("basex:nodes", Nodes.class);
  }

  /**
   * Evaluates the specified function.
   * @param name name of the function
   * @param args optional arguments
   * @return resulting XPathValue
   * @throws QueryException query exception
   */
  public Func getFunction(final byte[] name, final Expr[] args)
      throws QueryException {
    final Class<? extends Func> functionClass = functions.get(name);
    if(null == functionClass) throw new QueryException(UNKNOWNFUNC, name);

    try {
      final Func f = functionClass.getConstructor(new Class[] {
          Expr[].class }).newInstance(new Object[] { args });
      if(!f.checkArguments()) throw new QueryException(FUNCARGS, f.desc);
      return f;
    } catch(final QueryException ex) {
      throw ex;
    } catch(final Exception ex) {
      throw new QueryException(FUNCEXCEPTION, name);
    }
  }

  /**
   * Evaluates a variable.
   * @param name name of the variable
   * @return resulting XPathValue
   * @throws QueryException query exception
   */
  public Item evalVariable(final byte[] name) throws QueryException {
    final Item v = variables.get(name);
    if(v == null) throw new QueryException(UNKNOWNVAR, name);
    return v;
  }

  /**
   * Registers a function.
   * @param name name of the function.
   * @param function instance of the function.
   */
  private void addFunction(final String name,
      final Class<? extends Func> function) {
    functions.index(Token.token(name), function);
  }

  /**
   * Registers a variable.
   * @param name name of the variable.
   * @param value variable as string.
   */
  public void addVariable(final String name, final byte[] value) {
    addVariable(name, new Literal(value));
  }

  /**
   * Registers a variable.
   * @param variableName name of the variable.
   * @param value double value of variable.
   */
  public void addVariable(final String variableName, final double value) {
    addVariable(variableName, new Num(value));
  }

  /**
   * Registers a variable.
   * @param name name of the variable.
   * @param value variable instance.
   */
  private void addVariable(final String name, final Item value) {
    variables.index(Token.token(name), value);
  }
}
