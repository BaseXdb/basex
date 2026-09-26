package org.basex.query.expr.path;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Test for element and attribute nodes with a type annotation that untyped nodes never have.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AnnotationTest extends Test {
  /** Name test. */
  private final Test test;
  /** Type annotation. */
  private final Type type;

  /**
   * Constructor.
   * @param test name test
   * @param type type annotation
   */
  public AnnotationTest(final Test test, final Type type) {
    super(test.kind);
    this.test = test;
    this.type = type;
  }

  @Override
  public boolean matches(final GNode node) {
    return false;
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public Test intersect(final Test tst) {
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final AnnotationTest at && test.equals(at.test) &&
        type.eq(at.type);
  }

  @Override
  public String toString(final boolean tp) {
    final TokenBuilder tb = new TokenBuilder();
    for(final Test t : test instanceof final UnionTest ut ? ut.tests : new Test[] { test }) {
      if(!tb.isEmpty()) tb.add('|');
      tb.add(t instanceof final NameTest nt ? nt.nameString() : "*");
    }
    return kind.toString(tb.add(", ").add(type.toString()).toString());
  }
}
