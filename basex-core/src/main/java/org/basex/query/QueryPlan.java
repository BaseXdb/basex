package org.basex.query;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.scope.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Query plan builder.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class QueryPlan {
  /** QName. */
  private static final QNm Q_QUERY_PLAN = new QNm("QueryPlan");
  /** QName. */
  private static final QNm Q_COMPILED = new QNm("compiled");
  /** QName. */
  private static final QNm Q_UPDATING = new QNm("updating");

  /** Root node. */
  private final FBuilder root;
  /** Node stack. */
  private final Stack<FBuilder> nodes = new Stack<>();
  /** Include comprehensive information. */
  private final boolean full;

  /**
   * Constructor.
   * @param compiled compiled flag
   * @param updating updating flag
   * @param full include comprehensive information
   */
  public QueryPlan(final boolean compiled, final boolean updating, final boolean full) {
    root = FElem.build(Q_QUERY_PLAN).add(Q_COMPILED, compiled).add(Q_UPDATING, updating);
    nodes.add(root);
    this.full = full;
  }

  /**
   * Returns the root node.
   * @return root node
   */
  public FNode root() {
    return root.finish();
  }

  /**
   * Adds children to the specified element.
   * @param elem new element
   * @param children expressions to be added ({@code null} references will be ignored)
   */
  public void add(final FBuilder elem, final Object... children) {
    final FBuilder plan = nodes.peek();
    nodes.add(elem);

    for(final Object child : children) {
      if(child instanceof ExprInfo) {
        ((ExprInfo) child).toXml(this);
      } else if(child instanceof ExprInfo[]) {
        for(final ExprInfo ex : (ExprInfo[]) child) {
          if(ex != null) ex.toXml(this);
        }
      } else if(child instanceof byte[]) {
        elem.add((byte[]) child);
      } else {
        elem.add(child);
      }
    }

    plan.add(elem);
    nodes.pop();
  }

  /**
   * Adds children to the specified element.
   * @param elem new element
   * @param children expressions ({@code null} references will be ignored)
   */
  public void add(final FBuilder elem, final ExprInfo... children) {
    add(elem, (Object[]) children);
  }

  /**
   * Creates a new element node to be added to the query plan.
   * @param expr calling expression
   * @param atts attribute names and values
   * @return element
   */
  public FBuilder create(final ExprInfo expr, final Object... atts) {
    final FBuilder elem = FElem.build(new QNm(Util.className(expr)));
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
    final InputInfo info = expr.info();
    if(info != null) attach(elem, info);

    return elem;
  }

  /**
   * Creates a new element node to be added to the query plan.
   * @param name name of element
   * @param var variable to attach (can be {@code null})
   * @return element
   */
  public FBuilder create(final String name, final Var var) {
    final FBuilder elem = FElem.build(new QNm(Strings.capitalize(name)));
    if(var != null) attach(attachVariable(elem, var, true), var.info);
    return elem;
  }

  /**
   * Adds an attribute to the specified element if the specified value is not {@code null}.
   * @param elem element to which the attribute will be added
   * @param name name
   * @param value value or {@code null}
   */
  public void addAttribute(final FBuilder elem, final Object name, final Object value) {
    if(value != null) elem.add(new QNm(Util.inf(name)), Util.inf(value));
  }

  /**
   * Attaches variable information to the specified element.
   * @param elem element to which attributes will be added
   * @param var variable (can be {@code null})
   * @param type include type information
   * @return specified element
   */
  public FBuilder attachVariable(final FBuilder elem, final Var var, final boolean type) {
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
  private void attachType(final FBuilder elem, final SeqType seqType, final long size,
      final Data data) {

    addAttribute(elem, TYPE, seqType);
    if(size != -1) addAttribute(elem, SIZE, size);
    if(data != null) addAttribute(elem, DATABASE, data.meta.name);
  }

  /**
   * Attaches input information to the specified element.
   * @param elem element to which attributes will be added
   * @param info input info (can be {@code null})
   */
  private void attach(final FBuilder elem, final InputInfo info) {
    if(full) {
      addAttribute(elem, LINE, info.line());
      addAttribute(elem, COLUMN, info.column());
      addAttribute(elem, PATH, info.path());
    }
  }
}
