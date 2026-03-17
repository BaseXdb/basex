package org.basex.query.expr.path;

import java.util.*;

import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Union test for nodes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UnionTest extends Test {
  /** Tests. */
  public final Test[] tests;

  /**
   * Constructor.
   * @param tests tests
   */
  public UnionTest(final Test... tests) {
    super(unionKind(tests));
    this.tests = tests;
  }

  /**
   * Computes the supertype of the specified tests.
   * @param tests one or more tests
   * @return union kind
   */
  private static Kind unionKind(final Test[] tests) {
    Kind kind = tests[0].kind;
    for(int i = 1; i < tests.length && kind != Kind.GNODE; ++i) {
      final Kind kn = tests[i].kind;
      if(kn != kind) {
        kind = kn == Kind.JNODE || kind == Kind.JNODE ? Kind.GNODE : Kind.NODE;
      }
    }
    return kind;
  }

  @Override
  public Test optimize(final Kind kn, final Data data) {
    final ArrayList<Test> list = new ArrayList<>(tests.length);
    for(final Test test : tests) {
      final Test t = test.optimize(kn, data);
      if(t != null) list.add(t);
    }
    return get(list);
  }

  @Override
  public boolean matches(final GNode node) {
    for(final Test test : tests) {
      if(test.matches(node)) return true;
    }
    return false;
  }

  @Override
  public Boolean subsumes(final Type type) {
    for(final Test test : tests) {
      final Boolean b = test.subsumes(type);
      if(b != Boolean.FALSE) return b;
    }
    return Boolean.FALSE;
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public boolean instanceOf(final Test test) {
    if(this == test) return true;
    final UnionTest ut = test instanceof final UnionTest t ? t : null;
    for(final Test t : tests) {
      if(ut != null) {
        if(!((Checks<Test>) ts -> t.instanceOf(ts)).any(ut.tests)) return false;
      } else {
        if(!t.instanceOf(test)) return false;
      }
    }
    return true;
  }

  @Override
  public Test intersect(final Test test) {
    if(this == test) return this;
    final ArrayList<Test> list = new ArrayList<>(tests.length);
    for(final Test t : tests) {
      final Test t2 = t.intersect(test);
      if(t2 != null) list.add(t2);
    }
    return get(list);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final UnionTest ut && Arrays.equals(tests, ut.tests);
  }

  @Override
  public String toString(final boolean type) {
    final TokenBuilder tb = new TokenBuilder();
    for(final Test test : tests) {
      if(!tb.isEmpty()) tb.add('|');
      tb.add(test.toString(type));
    }
    return type ? "(" + tb + ")" : tb.toString();
  }
}
