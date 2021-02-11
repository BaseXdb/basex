package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Utility class for comparing XQuery values.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DeepEqual {
  /** Flags. */
  public enum Mode {
    /** Compare all node types. */ ALLNODES,
    /** Compare namespaces.     */ NAMESPACES,
  }

  /** Input info. */
  private final InputInfo info;
  /** Flag values. */
  private final EnumSet<Mode> flags = EnumSet.noneOf(Mode.class);
  /** Collation. */
  private Collation coll;

  /**
   * Constructor.
   */
  public DeepEqual() {
    this(null);
  }

  /**
   * Constructor.
   * @param info input info
   */
  public DeepEqual(final InputInfo info) {
    this.info = info;
  }

  /**
   * Sets the specified flag.
   * @param flag flag
   * @return self reference
   */
  public DeepEqual flag(final Mode flag) {
    flags.add(flag);
    return this;
  }

  /**
   * Sets a collation.
   * @param cl collation
   * @return self reference
   */
  public DeepEqual collation(final Collation cl) {
    coll = cl;
    return this;
  }

  /**
   * Checks values for deep equality.
   * @param value1 first value
   * @param value2 second value
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Value value1, final Value value2) throws QueryException {
    return equal(value1.iter(), value2.iter());
  }

  /**
   * Checks values for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Iter iter1, final Iter iter2) throws QueryException {
    return equal(iter1, iter2, null);
  }

  /**
   * Checks values for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @param qc query context (allows interruption of process, can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Iter iter1, final Iter iter2, final QueryContext qc)
      throws QueryException {

    final long size1 = iter1.size(), size2 = iter2.size();
    if(size1 != -1 && size2 != -1 && size1 != size2) return false;

    while(true) {
      if(qc != null) qc.checkStop();

      // check if one or both iterators are exhausted
      final Item item1 = iter1.next(), item2 = iter2.next();
      if(item1 == null || item2 == null) return item1 == null && item2 == null;

      // check functions
      if(item1 instanceof FItem) {
        if(((FItem) item1).deep(item2, coll, info)) continue;
        return false;
      }
      if(item2 instanceof FItem) {
        if(((FItem) item2).deep(item1, coll, info)) continue;
        return false;
      }

      // identical items are also equal
      if(item1 == item2) continue;

      // check atomic values
      if(!(item1 instanceof ANode || item2 instanceof ANode)) {
        if(item1.equiv(item2, coll, info)) continue;
        return false;
      }

      // node types must be equal
      Type type1 = item1.type, type2 = item2.type;
      if(type1 != type2) return false;

      ANode node1 = (ANode) item1, node2 = (ANode) item2;
      if(node1.is(node2)) continue;
      BasicNodeIter ch1 = node1.childIter(), ch2 = node2.childIter();

      final Stack<BasicNodeIter> stack = new Stack<>();
      stack.push(ch1);
      stack.push(ch2);

      boolean skip = false;
      do {
        type1 = node1 != null ? node1.type : null;
        type2 = node2 != null ? node2.type : null;

        // skip comparison of descendant comments and processing instructions
        if(skip) {
          if(type1 == NodeType.COMMENT || type1 == NodeType.PROCESSING_INSTRUCTION) {
            node1 = ch1.next();
            continue;
          }
          if(type2 == NodeType.COMMENT || type2 == NodeType.PROCESSING_INSTRUCTION) {
            node2 = ch2.next();
            continue;
          }
        }

        if(node1 == null || node2 == null) {
          if(node1 != node2) return false;
          ch2 = stack.pop();
          ch1 = stack.pop();
        } else {
          // ensure that nodes have same type
          if(type1 != type2) return false;

          // compare names
          QNm n1 = node1.qname(), n2 = node2.qname();
          if(n1 != null && (!n1.eq(n2) ||
              flags.contains(Mode.NAMESPACES) && !eq(n1.prefix(), n2.prefix())))
            return false;

          if(type1 == NodeType.TEXT || type1 == NodeType.ATTRIBUTE || type1 == NodeType.COMMENT ||
             type1 == NodeType.PROCESSING_INSTRUCTION || type1 == NodeType.NAMESPACE_NODE) {
            // compare string values
            if(!eq(node1.string(), node2.string())) return false;
          } else if(type1 == NodeType.ELEMENT) {
            // compare attributes
            final BasicNodeIter ir1 = node1.attributeIter();
            BasicNodeIter ir2 = node2.attributeIter();
            if(ir1.size() != ir2.size()) return false;

            // compare names, values and prefixes
            LOOP:
            for(ANode a1; (a1 = ir1.next()) != null;) {
              n1 = a1.qname();
              for(ANode a2; (a2 = ir2.next()) != null;) {
                n2 = a2.qname();
                if(!n1.eq(n2)) continue;
                if(flags.contains(Mode.NAMESPACES) && !eq(n1.prefix(), n2.prefix()) ||
                    !eq(a1.string(), a2.string())) {
                  return false;
                }
                ir2 = node2.attributeIter();
                continue LOOP;
              }
              return false;
            }

            // compare namespaces
            if(flags.contains(Mode.NAMESPACES)) {
              final Atts ns1 = node1.namespaces(), ns2 = node2.namespaces();
              final int nl1 = ns1.size(), nl2 = ns2.size();
              if(nl1 != nl2) return false;
              LOOP:
              for(int i1 = 0; i1 < nl1; i1++) {
                for(int i2 = 0; i2 < nl2; i2++) {
                  if(!eq(ns1.name(i1), ns2.name(i2))) continue;
                  if(!eq(ns1.value(i1), ns2.value(i2))) return false;
                  continue LOOP;
                }
                return false;
              }
            }

            // check children
            stack.push(ch1);
            stack.push(ch2);
            ch1 = node1.childIter();
            ch2 = node2.childIter();
          }
        }

        // check next child
        node1 = ch1.next();
        node2 = ch2.next();
        skip = !flags.contains(Mode.ALLNODES);
      } while(!stack.isEmpty());
    }
  }
}
