package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Utility class for comparing XQuery values.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Compare {
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
   * @param ii input info
   */
  public Compare(final InputInfo ii) {
    info = ii;
  }

  /**
   * Sets the specified flag.
   * @param f flag
   * @return self reference
   */
  public Compare flag(final Mode f) {
    flags.add(f);
    return this;
  }

  /**
   * Sets a collation.
   * @param c collation
   * @return self reference
   */
  public Compare collation(final Collation c) {
    coll = c;
    return this;
  }

  /**
   * Checks items for deep equality.
   * @param val1 first value
   * @param val2 second value
   * @param info input info
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean deep(final Value val1, final Value val2, final InputInfo info)
      throws QueryException {
    return deep(val1.iter(), val2.iter(), info);
  }

  /**
   * Checks items for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @param info input info
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean deep(final Iter iter1, final Iter iter2, final InputInfo info)
      throws QueryException {
    return new Compare(info).deep(iter1, iter2);
  }

  /**
   * Checks values for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean deep(final Iter iter1, final Iter iter2) throws QueryException {
    while(true) {
      // check if one or both iterators are exhausted
      final Item it1 = iter1.next(), it2 = iter2.next();
      if(it1 == null || it2 == null) return it1 == null && it2 == null;

      // check functions
      if(it1 instanceof FItem || it2 instanceof FItem) {
        // maps are functions but have a defined deep-equality
        if(it1 instanceof Map && it2 instanceof Map) {
          if(!((Map) it1).deep(info, (Map) it2)) return false;
          continue;
        }
        throw FICMP.get(info, it1 instanceof FItem ? it1.type : it2.type);
      }

      // identical items are also equal
      if(it1 == it2) continue;

      // check atomic values
      if(!(it1 instanceof ANode || it2 instanceof ANode)) {
        if(!it1.equiv(it2, coll, info)) return false;
        continue;
      }

      // node types must be equal
      Type t1 = it1.type, t2 = it2.type;
      if(t1 != t2) return false;

      ANode s1 = (ANode) it1, s2 = (ANode) it2;
      if(s1.is(s2)) continue;
      AxisIter ch1 = s1.children(), ch2 = s2.children();

      final Stack<AxisIter> stack = new Stack<>();
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
              flags.contains(Mode.NAMESPACES) && !eq(n1.prefix(), n2.prefix())))
            return false;

          if(t1 == NodeType.TXT || t1 == NodeType.ATT || t1 == NodeType.COM ||
             t1 == NodeType.PI || t1 == NodeType.NSP) {
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
                if(flags.contains(Mode.NAMESPACES) &&
                    !eq(n1.prefix(), n2.prefix()) ||
                    !eq(a1.string(), a2.string())) return false;
                continue LOOP;
              }
              return false;
            }

            // compare namespaces
            if(flags.contains(Mode.NAMESPACES)) {
              final Atts ns1 = s1.namespaces();
              final Atts ns2 = s2.namespaces();
              if(ns1.size() != ns2.size()) return false;
              LOOP:
              for(int i1 = 0; i1 < ns1.size(); i1++) {
                for(int i2 = 0; i2 < ns2.size(); i2++) {
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
            ch1 = s1.children();
            ch2 = s2.children();
          }
        }

        // check next child
        s1 = ch1.next();
        s2 = ch2.next();
        skip = !flags.contains(Mode.ALLNODES);
      } while(!stack.isEmpty());
    }
  }
}
