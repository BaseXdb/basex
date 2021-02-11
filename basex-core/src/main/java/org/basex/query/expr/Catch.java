package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Catch clause.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Catch extends Single {
  /** Error QNames. */
  public static final QNm[] NAMES = {
    create(E_CODE), create(E_DESCRIPTION), create(E_VALUE), create(E_MODULE),
    create(E_LINE_NUMBER), create(E_COLUMN_NUMBER), create(E_ADDITIONAL)
  };
  /** Error types. */
  public static final SeqType[] TYPES = {
    SeqType.QNAME_O, SeqType.STRING_ZO, SeqType.ITEM_ZM, SeqType.STRING_ZO,
    SeqType.INTEGER_ZO, SeqType.INTEGER_ZO, SeqType.ITEM_ZM
  };

  /** Error tests. */
  private final ArrayList<NameTest> tests;
  /** Error variables. */
  private final Var[] vars;

  /**
   * Constructor.
   * @param info input info
   * @param tests error tests
   * @param vars variables to be bound
   */
  public Catch(final InputInfo info, final NameTest[] tests, final Var[] vars) {
    super(info, null, SeqType.ITEM_ZM);
    this.tests = new ArrayList<>(Arrays.asList(tests));
    this.vars = vars;
  }

  @Override
  public Catch compile(final CompileContext cc) {
    try {
      expr = expr.compile(cc);
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
    }
    return optimize(cc);
  }

  @Override
  public Catch optimize(final CompileContext cc) {
    return (Catch) adoptType(expr);
  }

  /**
   * Returns the value of the caught expression.
   * @param qc query context
   * @param qe thrown exception
   * @return resulting item
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc, final QueryException qe) throws QueryException {
    Util.debug(qe);
    int i = 0;
    for(final Value value : values(qe)) qc.set(vars[i++], value);
    return expr.value(qc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Var[] vrs = new Var[NAMES.length];
    final int vl = vrs.length;
    for(int v = 0; v < vl; v++) vrs[v] = cc.vs().addNew(NAMES[v], TYPES[v], false, cc.qc, info);
    final Catch ctch = new Catch(info, tests.toArray(new NameTest[0]), vrs);
    final int val = vars.length;
    for(int v = 0; v < val; v++) vm.put(vars[v].id, ctch.vars[v]);
    ctch.expr = expr.copy(cc, vm);
    return copyType(ctch);
  }

  @Override
  public Catch inline(final InlineContext ic) {
    try {
      final Expr inlined = expr.inline(ic);
      if(inlined == null) return null;
      expr = inlined;
    } catch(final QueryException qe) {
      expr = ic.cc.error(qe, expr);
    }
    return this;
  }

  /**
   * Returns the catch expression with inlined exception values.
   * @param qe caught exception
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  Expr inline(final QueryException qe, final CompileContext cc) throws QueryException {
    if(expr instanceof Value) return expr;

    Expr ex = expr;
    int v = 0;
    for(final Value value : values(qe)) {
      ex = new InlineContext(vars[v++], value, cc).inline(ex);
    }
    return ex;
  }

  /**
   * Returns all error values.
   * @param qe exception
   * @return values
   */
  public static Value[] values(final QueryException qe) {
    final byte[] io = qe.file() == null ? EMPTY : token(qe.file());
    final Value value = qe.value();
    return new Value[] {
      qe.qname(),
      Str.get(qe.getLocalizedMessage()),
      value == null ? Empty.VALUE : value,
      Str.get(io),
      Int.get(qe.line()),
      Int.get(qe.column()),
      Str.get(qe.getMessage().replaceAll("\r\n?", "\n"))
    };
  }

  /**
   * Removes redundant tests.
   * @param list current tests
   * @param cc compilation context
   * @return if catch clause contains relevant tests
   */
  boolean simplify(final ArrayList<NameTest> list, final CompileContext cc) {
    // check if all errors are already caught
    if(list.contains(null)) {
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, "*");
      return false;
    }

    // check if the current clause will catch all errors
    for(final NameTest test : tests) {
      if(test == null) {
        list.add(null);
        cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, "*");
        tests.clear();
        tests.add(null);
        return true;
      }
    }

    // remove redundant tests
    final Iterator<NameTest> iter = tests.iterator();
    while(iter.hasNext()) {
      final NameTest test = iter.next();
      if(list.contains(test)) {
        cc.info(OPTREMOVE_X_X, test != null ? test : "*", (Supplier<?>) this::description);
        iter.remove();
      } else {
        list.add(test);
      }
    }
    return !tests.isEmpty();
  }

  /**
   * Checks if one of the specified errors match the thrown error.
   * @param qe thrown error
   * @return result of check
   */
  boolean matches(final QueryException qe) {
    final QNm name = qe.qname();
    for(final NameTest test : tests) {
      if(test == null || test.matches(name)) return true;
    }
    return false;
  }

  /**
   * Creates an error QName with the specified name.
   * @param name name
   * @return QName
   */
  private static QNm create(final byte[] name) {
    return new QNm(concat(ERR_PREFIX, COLON, name), ERROR_URI);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Var var : vars) {
      if(!visitor.declared(var)) return false;
    }
    return visitAll(visitor, expr);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Catch)) return false;
    final Catch ctch = (Catch) obj;
    return Array.equals(vars, ctch.vars) && tests.equals(ctch.tests) && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(CATCH);
    int c = 0;
    for(final NameTest test : tests) {
      if(c++ > 0) qs.token('|');
      qs.token(test != null ? test.toString(false) : "*");
    }
    qs.brace(expr);
  }
}
