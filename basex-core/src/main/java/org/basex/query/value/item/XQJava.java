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
 * @author BaseX Team 2005-24, BSD License
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
    super(SeqType.JAVA);
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
    return new QNm(JAVA_PREFIX, value == null ? "null" :
      value.getClass().getCanonicalName().replace("[]", DOTS), JAVA_URI);
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
  public FItem coerceTo(final FuncType ft, final QueryContext qc, final InputInfo ii,
      final boolean optimize) throws QueryException {
    if(instanceOf(ft)) return this;
    throw typeError(this, ft, ii);
  }

  @Override
  public boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    if(item instanceof XQJava) return equals(item);
    throw FICOMPARE_X.get(deep.info, this);
  }

  @Override
  public Object toJava() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof XQJava && Objects.equals(value, ((XQJava) obj).value);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.quoted(string(null));
  }
}
