package org.basex.query.util.unit;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.unit.Constants.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XQuery Unit tests: Testing single modules.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Unit {
  /** Query context. */
  private final QueryContext ctx;
  /** Input info. */
  private final InputInfo info;
  /** Currently processed function. */
  private StaticFunc current;

  /**
   * Constructor.
   * @param ii input info
   * @param qc query context
   */
  public Unit(final QueryContext qc, final InputInfo ii) {
    info = ii;
    ctx = qc;
  }

  /**
   * Performs the test function.
   * @param sc static context
   * @return resulting value
   * @throws QueryException query exception
   */
  public FElem test(final StaticContext sc) throws QueryException {
    final ArrayList<StaticFunc> funcs = new ArrayList<>();
    Collections.addAll(funcs, ctx.funcs.funcs());
    return test(sc, funcs);
  }

  /**
   * Performs the test function.
   * @param sc static context
   * @param funcs functions to test
   * @return resulting value
   * @throws QueryException query exception
   */
  public FElem test(final StaticContext sc, final ArrayList<StaticFunc> funcs)
      throws QueryException {
    final FElem testsuite = new FElem(TESTSUITE).add(NAME, sc.baseURI().string());

    final ArrayList<StaticFunc> before = new ArrayList<>(1);
    final ArrayList<StaticFunc> after = new ArrayList<>(1);
    final ArrayList<StaticFunc> beforeModule = new ArrayList<>(1);
    final ArrayList<StaticFunc> afterModule = new ArrayList<>(1);
    final ArrayList<StaticFunc> tests = new ArrayList<>(1);

    // loop through all functions
    final Performance p = new Performance();
    for(final StaticFunc uf : funcs) {
      // find Unit annotations
      final Ann ann = uf.ann;
      final int as = ann.size();
      boolean xq = false;
      for(int a = 0; !xq && a < as; a++) {
        xq |= eq(ann.names[a].uri(), QueryText.UNITURI);
      }
      if(!xq) continue;

      // Unit function:
      if(uf.updating) throw UNIT_UPDATE.get(info, uf.name.local());
      if(uf.args.length > 0) throw UNIT_ARGS.get(info, uf.name.local());

      if(indexOf(uf, BEFORE) != -1) before.add(uf);
      if(indexOf(uf, AFTER) != -1) after.add(uf);
      if(indexOf(uf, BEFORE_MODULE) != -1) beforeModule.add(uf);
      if(indexOf(uf, AFTER_MODULE) != -1) afterModule.add(uf);
      if(indexOf(uf, TEST) != -1) tests.add(uf);
    }

    int s = 0;
    int f = 0;
    int e = 0;
    int t = 0;
    try {
      // call initializing functions before first test
      for(final StaticFunc uf : beforeModule) eval(uf);

      for(final StaticFunc uf : tests) {
        // check arguments
        final Value values = uf.ann.values[indexOf(uf, TEST)];
        final long vs = values.size();

        // expected error code
        byte[] code = null;
        if(vs != 0) {
          if(vs == 2 && eq(EXPECTED, values.itemAt(0).string(info))) {
            code = values.itemAt(1).string(info);
          } else {
            throw UNIT_ANN.get(info, '%', uf.ann.names[0]);
          }
        }

        final FElem testcase = new FElem(TESTCASE).add(NAME, uf.name.local());
        t++;

        final Performance pt = new Performance();
        final int skip = indexOf(uf, IGNORE);
        if(skip == -1) {
          try {
            // call functions marked with "before"
            for(final StaticFunc fn : before) eval(fn);
            // call functions
            eval(uf);
            // call functions marked with "after"
            for(final StaticFunc fn : after) eval(fn);

            if(code != null) {
              f++;
              testcase.add(new FElem(FAILURE).add(new FElem(EXPECTED).add(code)));
            }
          } catch(final QueryException ex) {
            final QNm name = ex.qname();
            if(code == null || !eq(code, name.local())) {
              final boolean failure = eq(name.uri(), QueryText.UNITURI);
              if(failure) f++;
              else e++;
              final FElem error = new FElem(failure ? FAILURE : ERROR);
              error.add(LINE, token(ex.line()));
              error.add(COLUMN, token(ex.column()));

              if(ex instanceof UnitException) {
                final UnitException ue = (UnitException) ex;
                error.add(elem(ue.returned, RETURNED, ue.count));
                error.add(elem(ue.expected, EXPECTED, ue.count));
              } else {
                error.add(TYPE, ex.qname().local());
                error.add(ex.getLocalizedMessage());
              }
              testcase.add(error);
            }
          }
        } else {
          // skip test
          final Value sv = uf.ann.values[skip];
          testcase.add(SKIPPED, sv.isEmpty() ? EMPTY : sv.itemAt(0).string(info));
          s++;
        }
        testcase.add(TIME, time(pt));
        testsuite.add(testcase);
      }

      // run finalizing tests
      for(final StaticFunc uf : afterModule) eval(uf);

    } catch(final QueryException ex) {
      // handle initializers
      final FElem test = new FElem(TESTCASE).add(NAME, current.name.local());
      test.add(TIME, time(p));
      testsuite.add(test);
    }

    testsuite.add(TIME, time(p));
    testsuite.add(TESTS, token(t));
    testsuite.add(FAILURES, token(f));
    testsuite.add(ERRORS, token(e));
    testsuite.add(SKIPPED, token(s));
    return testsuite;
  }

  /**
   * Creates a new element.
   * @param it item
   * @param name name (expected/returned)
   * @param c item count
   * @return new element
   * @throws QueryException query exception
   */
  private FElem elem(final Item it, final byte[] name, final int c) throws QueryException {
    final FElem exp = new FElem(name);
    if(it instanceof ANode) {
      exp.add((ANode) it);
    } else if(it != null) {
      exp.add(it.string(info));
    }
    exp.add(ITEM, token(c));
    return exp;
  }

  /**
   * Evaluates a function.
   * @param fn function to evaluate
   * @throws QueryException query exception
   */
  private void eval(final StaticFunc fn) throws QueryException {
    current = fn;
    final Iter ir = fn.invokeValue(ctx, info).iter();
    for(Item it; (it = ir.next()) != null;) it.materialize(info);
  }

  /**
   * Checks if a unit annotation has been specified.
   * If positive, returns its offset in the annotation array.
   *
   * @param func user function
   * @param name name of annotation to be found
   * @return value
   * @throws QueryException query exception
   */
  private int indexOf(final StaticFunc func, final byte[] name) throws QueryException {
    final Ann ann = func.ann;
    final int as = ann.size();
    int pos = -1;
    for(int a = 0; a < as; a++) {
      final QNm nm = ann.names[a];
      if(eq(nm.uri(), QueryText.UNITURI) && eq(nm.local(), name)) {
        if(pos != -1) throw UNIT_TWICE.get(info, '%', nm.local());
        pos = a;
      }
    }
    return pos;
  }

  /**
   * Returns a token representation of the measured time.
   *
   * @param p performance
   * @return time
   */
  private byte[] time(final Performance p) {
    return new DTDur(p.time() / 1000000).string(info);
  }
}
