package org.basex.query.pf;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.util.Token;
import static org.basex.query.pf.PFT.*;

/**
 * Axis steps.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
enum ScA {
  /** Axis step. */
  ANCORSELF {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        do {
          final int k = d.kind(p);
          if(t.e(d, p, k)) c.a(p);
          p = d.parent(p, k);
        } while(p >= 0);
      }
    }
  },
  
  /** Axis step. */
  ANCESTOR {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        int k = d.kind(p);
        while(p != 0) {
          p = d.parent(p, k);
          k = d.kind(p);
          if(t.e(d, p, k)) c.a(p);
        }
      }
    }
  },
  
  /** Axis step. */
  ATTRIBUTE {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        final int s = p + d.attSize(p, d.kind(p));
        while(++p < s) if(t.e(d, p, Data.ATTR)) c.a(p);
      }
    }
  },
  
  /** Axis step. */
  CHILD {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        int k = d.kind(p);
        if(k == Data.ATTR) continue;
        final int l = p + d.size(p, k);
        p += d.attSize(p, k);
        while(p != l) {
          k = d.kind(p);
          if(t.e(d, p, k)) c.a(p);
          p += d.size(p, k);
        }
      }
    }
  },
  
  /** Axis step. */
  DESCORSELF {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        int k = d.kind(p);
        final int l = p + d.size(p, k);
        while(p != l) {
          k = d.kind(p);
          if(t.e(d, p, k)) c.a(p);
          p += d.attSize(p, k);
        }
      }
    }
  },
  
  /** Axis step. */
  DESCENDANT {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        int k = d.kind(p);
        if(k == Data.ATTR) continue;
        final int l = p + d.size(p, k);
        p += d.attSize(p, k);
        while(p != l) {
          k = d.kind(p);
          if(t.e(d, p, k)) c.a(p);
          p += d.attSize(p, k);
        }
      }
    }
  },
  
  /** Axis step. */
  FOLSIBLING {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        int k = d.kind(p);
        if(k == Data.ATTR) continue;
        final int z = d.parent(p, k);
        final int l = z + d.size(z, d.kind(z));
        p += d.size(p, k);
        while(p != l) {
          k = d.kind(p);
          if(t.e(d, p, k)) c.a(p);
          p += d.size(p, k);
        }
      }
    }
  },
  
  /** Axis step. */
  FOLLOWING {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        int k = d.kind(p);
        if(k == Data.ATTR) continue;
        p += d.size(p, k);
        final int s = d.size;
        while(p != s) {
          k = d.kind(p);
          if(t.e(d, p, k)) c.a(p);
          p += d.attSize(p, k);
        }
      }
    }
  },
  
  /** Axis step. */
  PARENT {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        if(p != 0) {
          p = d.parent(p, d.kind(p));
          if(t.e(d, p, d.kind(p))) c.a(p);
        }
      }
    }
  },
  
  /** Axis step. */
  PRECSIBLING {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        final int n = d.parent(p, d.kind(p));
        while(p-- != 0) {
          final int k = d.kind(p);
          final int z = d.parent(p, k);
          if(z < n) break;
          if(k == Data.ATTR || z != n) continue;
          if(t.e(d, p, k)) c.a(p);
        }
      }
    }
  },
  
  /** Axis step. */
  PRECEDING {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        int p = c.n();
        int n = d.parent(p, d.kind(p));
        while(p-- != 0) {
          final int k = d.kind(p);
          if(k == Data.ATTR) continue;
          if(p == n) n = d.parent(p, k);
          else if(t.e(d, p, k)) c.a(p);
        }
      }
    }
  },
  
  /** Axis step. */
  SELF {
    @Override
    void e(final Data d, final ScT t, final ScD c) {
      while(c.m()) {
        final int p = c.n();
        if(t.e(d, p, d.kind(p))) c.a(p);
      }
    }
  };
  
  /**
   * Processes a location step.
   * @param d data reference
   * @param t node test
   * @param c context set
   * @throws QueryException query exception
   */
  abstract void e(final Data d, final ScT t, final ScD c) throws QueryException;
  
  /**
   * Returns the correct axis for the specified token.
   * @param axis axis name
   * @return axis
   * @throws QueryException query exception
   */
  static ScA get(final byte[] axis) throws QueryException {
    if(Token.eq(axis, AANCORSELF))   return ScA.ANCORSELF;
    if(Token.eq(axis, AANCESTOR))    return ScA.ANCESTOR;
    if(Token.eq(axis, AATTRIBUTE))   return ScA.ATTRIBUTE;
    if(Token.eq(axis, ACHILD))       return ScA.CHILD;
    if(Token.eq(axis, ADESCORSELF))  return ScA.DESCORSELF;
    if(Token.eq(axis, ADESCENDANT))  return ScA.DESCENDANT;
    if(Token.eq(axis, AFOLSIBLING))  return ScA.FOLSIBLING;
    if(Token.eq(axis, AFOLLOWING))   return ScA.FOLLOWING;
    if(Token.eq(axis, APARENT))      return ScA.PARENT;
    if(Token.eq(axis, APRECSIBLING)) return ScA.PRECSIBLING;
    if(Token.eq(axis, APRECEDING))   return ScA.PRECEDING;
    if(Token.eq(axis, ASELF))        return ScA.SELF;
    throw new QueryException(PFAXIS, axis);
  }
}
