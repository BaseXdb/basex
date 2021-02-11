package org.basex.query;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.scope.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Query plan builder.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryPlan {
  /** Root node. */
  private final FElem root;
  /** Node stack. */
  private final Stack<FElem> nodes = new Stack<>();
  /** Include comprehensive information. */
  private final boolean full;

  /**
   * Constructor.
   * @param compiled compiled flag
   * @param updating updating flag
   * @param full include comprehensive information
   */
  public QueryPlan(final boolean compiled, final boolean updating, final boolean full) {
    root = new FElem(QUERY_PLAN);
    root.add(COMPILED, token(compiled));
    root.add(UPDATING, token(updating));
    nodes.add(root);
    this.full = full;
  }

  /**
   * Returns the root node.
   * @return root node
   */
  public FElem root() {
    return root;
  }

  /**
   * Adds children to the specified element.
   * @param elem new element
   * @param children expressions to be added ({@code null} references will be ignored)
   */
  public void add(final FElem elem, final Object... children) {
    final FElem plan = nodes.peek();
    plan.add(elem);
    nodes.add(elem);

    for(final Object child : children) {
      if(child instanceof ExprInfo) {
        ((ExprInfo) child).plan(this);
      } else if(child instanceof ExprInfo[]) {
        for(final ExprInfo ex : (ExprInfo[]) child) {
          if(ex != null) ex.plan(this);
        }
      } else if(child instanceof byte[]) {
        elem.add((byte[]) child);
      } else if(child != null) {
        elem.add(child.toString());
      }
    }

    nodes.pop();
  }

  /**
   * Adds children to the specified element.
   * @param elem new element
   * @param children expressions ({@code null} references will be ignored)
   */
  public void add(final FElem elem, final ExprInfo... children) {
    add(elem, (Object[]) children);
  }

  /**
   * Creates a new element node to be added to the query plan.
   * @param expr calling expression
   * @param atts attribute names and values
   * @return element
   */
  public FElem create(final ExprInfo expr, final Object... atts) {
    final FElem elem = new FElem(Util.className(expr));
    final int al = atts.length;
    for(int a = 0; a < al - 1; a += 2) {
      addAttribute(elem, atts[a], atts[a + 1]);
    }
    if(expr instanceof Expr) {
      final Expr ex = (Expr) expr;
      attachType(elem, ex.seqType(), ex.size(), ex.data());
    } else if(expr instanceof StaticDecl) {
      attachType(elem, ((StaticDecl) expr).seqType(), -1, null);
    }
    if(expr instanceof ParseExpr) {
      attachInputInfo(elem, ((ParseExpr) expr).info);
    }
    return elem;
  }

  /**
   * Creates a new element node to be added to the query plan.
   * @param name name of element
   * @param var variable to attach (can be {@code null})
   * @return element
   */
  public FElem create(final String name, final Var var) {
    final FElem elem = new FElem(Strings.capitalize(name));
    if(var != null) attachInputInfo(attachVariable(elem, var, true), var.info);
    return elem;
  }

  /**
   * Adds an attribute to the specified element if the specified value is not {@code null}.
   * @param elem element to which the attribute will be added
   * @param name name
   * @param value value or {@code null}
   */
  public void addAttribute(final FElem elem, final Object name, final Object value) {
    if(value != null) elem.add(Util.inf(name), Util.inf(value));
  }

  /**
   * Adds a child element to the specified element.
   * @param elem element to which the attribute will be added
   * @param child child element
   */
  public void addElement(final FElem elem, final FElem child) {
    elem.add(child);
  }

  /**
   * Attaches variable information to the specified element.
   * @param elem element to which attributes will be added
   * @param var variable (can be {@code null})
   * @param type include type information
   * @return specified element
   */
  public FElem attachVariable(final FElem elem, final Var var, final boolean type) {
    if(var != null) {
      addAttribute(elem, NAME, var.toErrorString());
      addAttribute(elem, ID, var.id);
      if(var.declType != null) addAttribute(elem, AS, var.declType);
      if(var.promote) addAttribute(elem, PROMOTE, true);
      if(type) attachType(elem, var.seqType(), var.size(), var.data());
    }
    return elem;
  }

  /**
   * Attaches type information to the specified element.
   * @param elem element to which attributes will be added
   * @param seqType sequence type
   * @param size result size
   * @param data data reference (can be {@code null})
   */
  private void attachType(final FElem elem, final SeqType seqType, final long size,
      final Data data) {

    addAttribute(elem, TYPE, seqType);
    if(size != -1) addAttribute(elem, SIZE, size);
    if(data != null) addAttribute(elem, DATABASE, data.meta.name);
  }

  /**
   * Attaches input information to the specified element.
   * @param elem element to which attributes will be added
   * @param ii input info
   */
  private void attachInputInfo(final FElem elem, final InputInfo ii) {
    if(full) {
      addAttribute(elem, LINE, ii.line());
      addAttribute(elem, COLUMN, ii.column());
      addAttribute(elem, PATH, ii.path());
    }
  }
}
