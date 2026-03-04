package org.basex.query.value.node;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Generic node.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class GNode extends Item {
  /**
   * Constructor.
   * @param type item type
   */
  GNode(final Type type) {
    super(type);
  }

  /**
   * Returns the node kind.
   * @return node kind
   */
  public final Kind kind() {
    return type.kind();
  }

  @Override
  public final boolean instanceOf(final Type tp, final boolean coerce) {
    if(type.instanceOf(tp)) return true;
    if(tp instanceof final NodeType nt && nt.test != null) {
      return nt.test.matches(this);
    }
    return false;
  }

  /**
   * Checks if two nodes are identical.
   * @param node node to be compared
   * @return result of check
   */
  public abstract boolean is(GNode node);

  /**
   * Checks the document order of two nodes.
   * @param node node to be compared
   * @return {@code 0} if the nodes are identical, or {@code 1}/{@code -1}
   * if the node appears after/before the argument
   */
  public abstract int compare(GNode node);

  /**
   * Returns the root of a node (the topmost ancestor without parent node).
   * @return root node
   */
  public abstract GNode root();

  /**
   * Returns the parent node.
   * @return parent node or {@code null}
   */
  public abstract GNode parent();

  /**
   * Indicates if the node has children.
   * @return result of test
   */
  public abstract boolean hasChildren();

  /**
   * Returns an ancestor-or-self axis iterator.
   * @param self include self node
   * @return iterator
   */
  public BasicNodeIter ancestorIter(final boolean self) {
    return new BasicNodeIter() {
      private GNode node = GNode.this;
      private boolean slf = self;

      @Override
      public GNode next() {
        if(slf) {
          slf = false;
        } else {
          node = node.parent();
        }
        return node;
      }
    };
  }

  /**
   * Returns an attribute axis iterator with {@link Iter#size()} and
   * {@link Iter#get(long)} implemented.
   * @return iterator
   */
  public abstract BasicNodeIter attributeIter();

  /**
   * Returns a child axis iterator.
   * @param test node test (can be {@code null})
   * @param descendant part of descendant traversal
   * @return iterator
   */
  public abstract BasicNodeIter childIter(Test test, boolean descendant);

  /**
   * Returns a child axis iterator.
   * @return iterator
   */
  public final BasicNodeIter childIter() {
    return childIter(null, false);
  }

  /**
   * Returns an iterator for all descendant nodes.
   * @param self include self node
   * @param test node test (can be {@code null})
   * @return iterator
   */
  public BasicNodeIter descendantIter(final boolean self, final Test test) {
    return new BasicNodeIter() {
      private final Deque<BasicNodeIter> stack = new ArrayDeque<>();
      private boolean init;

      @Override
      public GNode next() {
        if(!init) {
          stack.push(self ? selfIter() : childIter(test, true));
          init = true;
        }
        while(!stack.isEmpty()) {
          final GNode node = stack.peek().next();
          if(node != null) {
            stack.push(node.childIter(test, true));
            return node;
          }
          stack.pop();
        }
        return null;
      }
    };
  }

  /**
   * Returns an iterator for all descendant nodes.
   * @param self include self node
   * @return iterator
   */
  public final BasicNodeIter descendantIter(final boolean self) {
    return descendantIter(self, null);
  }

  /**
   * Returns an iterator for all descendant nodes.
   * @param self include self node
   * @return node iterator
   */
  public BasicNodeIter followingIter(final boolean self) {
    return new BasicNodeIter() {
      private BasicNodeIter iter;

      @Override
      public GNode next() {
        if(iter == null) {
          final GNodeList list = new GNodeList();
          if(self) list.add(GNode.this);
          GNode node = GNode.this, root = node.parent();
          while(root != null) {
            final BasicNodeIter ir = root.childIter();
            if(node.kind() != Kind.ATTRIBUTE) {
              for(final GNode nd : ir) {
                if(nd.is(node)) break;
              }
            }
            for(final GNode nd : ir) {
              list.add(nd);
              addDescendants(nd.childIter(), list);
            }
            node = root;
            root = root.parent();
          }
          iter = list.iter();
        }
        return iter.next();
      }
    };
  }

  /**
   * Returns a following-sibling axis iterator.
   * @param self include self node
   * @return iterator
   */
  public BasicNodeIter followingSiblingIter(final boolean self) {
    final GNode root = parent();
    if(root == null || kind() == Kind.ATTRIBUTE) return self ? selfIter() : BasicNodeIter.EMPTY;

    return new BasicNodeIter() {
      private final BasicNodeIter iter = root.childIter();
      private boolean found;

      @Override
      public GNode next() {
        for(GNode n; !found && (n = iter.next()) != null;) {
          if(n.is(GNode.this)) {
            found = true;
            if(self) return n;
          }
        }
        return iter.next();
      }
    };
  }

  /**
   * Returns a parent axis iterator.
   * @return iterator
   */
  public final BasicNodeIter parentIter() {
    final GNode parent = parent();
    return parent != null ? singleIter(parent) : BasicNodeIter.EMPTY;
  }

  /**
   * Returns a preceding axis iterator.
   * @param self include self node
   * @return iterator
   */
  public BasicNodeIter precedingIter(final boolean self) {
    return new BasicNodeIter() {
      private BasicNodeIter iter;

      @Override
      public GNode next() {
        if(iter == null) {
          final GNodeList list = new GNodeList();
          if(self) list.add(GNode.this);
          GNode node = GNode.this, root = node.parent();
          while(root != null) {
            if(node.kind() != Kind.ATTRIBUTE) {
              final GNodeList lst = new GNodeList();
              for(final GNode ch : root.childIter()) {
                if(ch.is(node)) break;
                lst.add(ch);
                addDescendants(ch.childIter(), lst);
              }
              for(int t = lst.size() - 1; t >= 0; t--) list.add(lst.get(t));
            }
            node = root;
            root = root.parent();
          }
          iter = list.iter();
        }
        return iter.next();
      }
    };
  }

  /**
   * Returns a preceding-sibling axis iterator.
   * @param self include self node
   * @return iterator
   */
  public final BasicNodeIter precedingSiblingIter(final boolean self) {
    final GNode root = parent();
    if(root == null || kind() == Kind.ATTRIBUTE) return self ? selfIter() : BasicNodeIter.EMPTY;

    return new BasicNodeIter() {
      private GNodeList list;
      private int l;

      @Override
      public GNode next() {
        if(list == null) {
          list = new GNodeList();
          for(final GNode node : root.childIter()) {
            final boolean last = node.is(GNode.this);
            if(!last || self) list.add(node);
            if(last) break;
          }
          l = list.size();
        }
        return l > 0 ? list.get(--l) : null;
      }
    };
  }

  /**
   * Returns a self axis iterator.
   * @return iterator
   */
  public final BasicNodeIter selfIter() {
    return singleIter(this);
  }

  /**
   * Returns an iterator for a single item.
   * @param node node
   * @return iterator
   */
  final BasicNodeIter singleIter(final GNode node) {
    return new BasicNodeIter() {
      private boolean called;

      @Override
      public GNode next() {
        if(called) return null;
        called = true;
        return node;
      }
      @Override
      public long size() {
        return 1;
      }
      @Override
      public GNode get(final long i) {
        return node;
      }
      @Override
      public GNode value(final QueryContext qc, final Expr expr) {
        return node;
      }
    };
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public abstract byte[] string();

  /**
   * Returns the name (optional prefix, local name) of an attribute, element or
   * processing instruction. This function is possibly evaluated faster than {@link #qname()},
   * as no {@link QNm} instance may need to be created.
   * @return name, or {@code null} if node has no name
   */
  public abstract byte[] name();

  /**
   * Returns the QName (optional prefix, local name) of an attribute, element or
   * processing instruction.
   * @return name, or {@code null} if node has no QName
   */
  public abstract QNm qname();

  @Override
  public abstract Value materialize(Predicate<Data> test, InputInfo ii, QueryContext qc)
      throws QueryException;

  /**
   * Adds nodes of a child iterator and its descendants.
   * @param children child nodes
   * @param nodes node cache
   */
  private static void addDescendants(final BasicNodeIter children, final GNodeList nodes) {
    for(final GNode node : children) {
      nodes.add(node);
      addDescendants(node.childIter(), nodes);
    }
  }
}
