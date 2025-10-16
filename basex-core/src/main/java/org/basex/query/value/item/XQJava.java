package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.core.MainOptions.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.java.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function item, wrapping a Java object.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQJava extends FItem {
  /** Java object (can be {@code null}). */
  private final Object value;

  /**
   * Constructor.
   * @param value value (can be {@code null})
   */
  public XQJava(final Object value) {
    super(FuncType.JAVA);
    this.value = value;
  }

  @Override
  public AnnList annotations() {
    return AnnList.EMPTY;
  }

  @Override
  public Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {
    return JavaCall.toValue(value, qc, ii, WrapOptions.NONE);
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return Token.token(Objects.toString(value));
  }

  @Override
  public int stackFrameSize() {
    return 0;
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public QNm funcName() {
    final String name = value != null ? value.getClass().getCanonicalName() : null;
    return new QNm(JAVA_PREFIX, name != null ? name.replace("[]", DOTS) : "null", JAVA_URI);
  }

  @Override
  public String funcIdentity() {
    return new TokenBuilder(funcName().prefixId()).add('-').addInt(hashCode()).toString();
  }

  @Override
  public QNm paramName(final int pos) {
    return null;
  }

  @Override
  public Expr inline(final Expr[] exprs, final CompileContext cc) {
    return null;
  }

  @Override
  public boolean vacuousBody() {
    return false;
  }

  @Override
  boolean updating() {
    return false;
  }

  @Override
  public FItem coerceTo(final FuncType ft, final QueryContext qc, final CompileContext cc,
      final InputInfo ii) throws QueryException {
    if(type.instanceOf(ft)) return this;
    throw typeError(this, ft, ii);
  }

  @Override
  public boolean deepEqual(final Item item, final DeepEqual deep) {
    return this == item || item instanceof final XQJava java && Objects.equals(value, java.value);
  }

  @Override
  public Object toJava() {
    return value;
  }

  @Override
  public void toString(final QueryString qs) {
    qs.quoted(string(null));
  }
}
