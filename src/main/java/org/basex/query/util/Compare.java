package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.EnumSet;
import java.util.Stack;

import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.item.map.Map;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.Iter;
import org.basex.util.Atts;
import org.basex.util.InputInfo;

/**
 * Utility class for comparing XQuery values.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Compare {
  /** Flags. */
  public enum Flag {
    /** Compare all node types. */ ALLNODES,
    /** Compare namespaces.     */ NAMESPACES,
  };

  /** Input info. */
  private final InputInfo input;
  /** Flag values. */
  private final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);

  /**
   * Constructor.
   * @param ii input info
   */
  public Compare(final InputInfo ii) {
    input = ii;
  }

  /**
   * Sets the specified flag.
   * @param f flag
   * @return self reference
   */
  public Compare set(final Flag f) {
    flags.add(f);
    return this;
  }

  /**
   * Checks items for deep equality.
   * @param val1 first value
   * @param val2 second value
   * @param input input info
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean deep(final Value val1, final Value val2,
      final InputInfo input) throws QueryException {
    return new Compare(input).deep(val1.iter(), val2.iter());
  }

  /**
   * Checks items for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @param input input info
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean deep(final Iter iter1, final Iter iter2,
      final InputInfo input) throws QueryException {
    return new Compare(input).deep(iter1, iter2);
  }

  /**
   * Checks values for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean deep(final Iter iter1, final Iter iter2)
      throws QueryException {

    while(true) {
      // check if one or both iterators are exhausted
      final Item it1 = iter1.next(), it2 = iter2.next();
      if(it1 == null || it2 == null) return it1 == null && it2 == null;

      // check functions
      Type t1 = it1.type, t2 = it2.type;
      if(t1.isFunction() || t2.isFunction()) {
        // maps are functions but have a defined deep-equality
        if(t1.isMap() && t2.isMap()) {
          final Map map1 = (Map) it1, map2 = (Map) it2;
          if(!map1.deep(input, map2)) return false;
          continue;
        }
        FNCMP.thrw(input, t1.isFunction() ? it1 : it2);
      }

      // check atomic values
      if(!t1.isNode() && !t2.isNode()) {
        if(!it1.equiv(input, it2)) return false;
        continue;
      }

      // node types must be equal
      if(t1 != t2) return false;

      ANode s1 = (ANode) it1, s2 = (ANode) it2;
      AxisIter ch1 = s1.children(), ch2 = s2.children();

      final Stack<AxisIter> stack = new Stack<AxisIter>();
      stack.push(ch1);
      stack.push(ch2);

      boolean skip = false;
      do {
        t1 = s1 != null ? s1.type : null;
        t2 = s2 != null ? s2.type : null;

        // skip comparison of descendant comments and processing instructions
        if(skip) {
          if(t1 == NodeType.COM || t1 == NodeType.PI) {
            s1 = ch1.next();
            continue;
          }
          if(t2 == NodeType.COM || t2 == NodeType.PI) {
            s2 = ch2.next();
            continue;
          }
        }

        if(s1 == null || s2 == null) {
          if(s1 != s2) return false;
          ch2 = stack.pop();
          ch1 = stack.pop();
        } else {
          // ensure that nodes have same type
          if(t1 != t2) return false;

          // compare names
          QNm n1 = s1.qname(), n2 = s2.qname();
          if(n1 != null && (!n1.eq(n2) ||
              flags.contains(Flag.NAMESPACES) && !eq(n1.prefix(), n2.prefix())))
            return false;

          if(t1 == NodeType.TXT || t1 == NodeType.ATT ||
             t1 == NodeType.COM || t1 == NodeType.PI) {
            // compare string values
            if(!eq(s1.string(), s2.string())) return false;
          } else if(t1 == NodeType.ELM) {
            // compare attributes
            if(s1.attributes().value().size() !=
               s2.attributes().value().size()) return false;

            // compare names, values and prefixes
            final AxisIter ai1 = s1.attributes();
            LOOP:
            for(ANode a1; (a1 = ai1.next()) != null;) {
              n1 = a1.qname();
              final AxisIter ai2 = s2.attributes();
              for(ANode a2; (a2 = ai2.next()) != null;) {
                n2 = a2.qname();
                if(!n1.eq(n2)) continue;
                if(flags.contains(Flag.NAMESPACES) &&
                    !eq(n1.prefix(), n2.prefix()) ||
                    !eq(a1.string(), a2.string())) return false;
                continue LOOP;
              }
              return false;
            }

            // compare namespaces
            if(flags.contains(Flag.NAMESPACES)) {
              final Atts ns1 = s1.namespaces();
              final Atts ns2 = s2.namespaces();
              if(ns1.size() != ns2.size()) return false;
              LOOP:
              for(int i1 = 0; i1 < ns1.size(); i1++) {
                for(int i2 = 0; i2 < ns2.size(); i2++) {
                  if(!eq(ns1.key(i1), ns2.key(i2))) continue;
                  if(!eq(ns1.value(i1), ns2.value(i2))) return false;
                  continue LOOP;
                }
                return false;
              }
            }

            // check children
            stack.push(ch1);
            stack.push(ch2);
            ch1 = s1.children();
            ch2 = s2.children();
          }
        }

        // check next child
        s1 = ch1.next();
        s2 = ch2.next();
        skip = !flags.contains(Flag.ALLNODES);
      } while(!stack.isEmpty());
    }
  }
}
