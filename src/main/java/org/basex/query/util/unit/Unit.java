package org.basex.query.util.unit;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.unit.Constants.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.io.*;
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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Unit {
  /** Query context. */
  private final QueryContext ctx;
  /** Input info. */
  private final InputInfo info;

  /** Tests performed before a function. */
  private final ArrayList<UserFunc> before = new ArrayList<UserFunc>(1);
  /** Tests performed after a function. */
  private final ArrayList<UserFunc> after = new ArrayList<UserFunc>(1);
  /** Tests performed before a module. */
  private final ArrayList<UserFunc> beforeModule = new ArrayList<UserFunc>(1);
  /** Tests performed after a module. */
  private final ArrayList<UserFunc> afterModule = new ArrayList<UserFunc>(1);
  /** Currently processed function. */
  private UserFunc current;

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
   * Performs the run function.
   * @return resulting value
   * @throws QueryException query exception
   */
  public FElem test() throws QueryException {
    final FElem tests = new FElem(Q_TESTSUITE);
    tests.add(Q_NAME, ctx.sc.baseURI().string());
    int t = 0, e = 0, f = 0, s = 0;

    final IO file = ctx.sc.baseIO();
    final Performance p = new Performance();

    // loop through all functions
    for(final UserFunc uf : ctx.funcs.funcs()) {
      // consider only functions that are defined in the same file
      if(!file.eq(new IOFile(uf.info.file()))) continue;

      // find XQUnit annotations
      final Ann ann = uf.ann;
      final int as = ann.size();
      boolean xq = false;
      for(int a = 0; !xq && a < as; a++) {
        xq |= eq(ann.names[a].uri(), QueryText.XQUNITURI);
      }
      if(!xq) continue;

      // XQUnit function:
      if(uf.updating) UNIT_UPDATE.thrw(info, uf.name.local());
      if(uf.args.length > 0) UNIT_ARGS.thrw(info, uf.name.local());

      if(indexOf(uf, BEFORE) != -1) before.add(uf);
      if(indexOf(uf, AFTER) != -1) after.add(uf);
      if(indexOf(uf, BEFORE_MODULE) != -1) beforeModule.add(uf);
      if(indexOf(uf, AFTER_MODULE) != -1) afterModule.add(uf);
    }

    try {
      // call initializing functions before first test
      for(final UserFunc uf : beforeModule) eval(uf);

      for(final UserFunc uf : ctx.funcs.funcs()) {
        // consider only test functions that are defined in the same file
        if(!file.eq(new IOFile(uf.info.file()))) continue;
        // find test annotation
        final int pos = indexOf(uf, TEST);
        if(pos == -1) continue;

        // check arguments
        final Value values = uf.ann.values[pos];
        final long vs = values.size();

        // expected error code
        byte[] code = null;
        if(vs != 0) {
          if(vs == 2 && eq(EXPECTED, values.itemAt(0).string(info))) {
            code = values.itemAt(1).string(info);
          } else {
            UNIT_ANN.thrw(info, '%', uf.ann.names[0]);
          }
        }

        final FElem test = new FElem(Q_TESTCASE).add(Q_NAME, uf.name.local());
        t++;

        final Performance pt = new Performance();
        final int skip = indexOf(uf, IGNORE);
        if(skip != -1) {
          // skip test
          final FElem skipped = new FElem(Q_SKIPPED);
          final Value sv = uf.ann.values[skip];
          if(sv.size() > 0) skipped.add(Q_MESSAGE, sv.itemAt(0).string(info));
          test.add(skipped);
          s++;
        } else {
          try {
            // call functions marked with "before"
            for(final UserFunc fn : before) eval(fn);
            // call functions
            eval(uf);
            // call functions marked with "after"
            for(final UserFunc fn : after) eval(fn);

            if(code != null) {
              f++;
              final FElem error = new FElem(Q_FAILURE);
              error.add(Q_MESSAGE, "Error expected.");
              error.add(Q_TYPE, code);
              test.add(error);
            }
          } catch(final QueryException ex) {
            final QNm name = ex.qname();
            if(code == null || !eq(code, name.local())) {
              final boolean failure = eq(name.uri(), QueryText.XQUNITURI);
              if(failure) f++;
              else e++;

              final QNm nm = failure ? Q_FAILURE : Q_ERROR;
              final FElem error = new FElem(nm);
              error.add(Q_MESSAGE, ex.getLocalizedMessage());
              error.add(Q_TYPE, ex.qname().local());
              test.add(error);
            }
          }
        }
        test.add(Q_TIME, time(pt));
        tests.add(test);
      }

      // run finalizing tests
      for(final UserFunc uf : afterModule) eval(uf);

    } catch(final QueryException ex) {
      // handle initializers
      final FElem test = new FElem(Q_TESTCASE).add(Q_NAME, current.name.local());
      test.add(Q_TIME, time(p));
      tests.add(test);
    }

    tests.add(Q_TIME, time(p));
    tests.add(Q_TESTS, token(t));
    tests.add(Q_FAILURES, token(f));
    tests.add(Q_ERRORS, token(e));
    tests.add(Q_SKIPPED, token(s));
    return tests;
  }

  /**
   * Evaluates a function.
   * @param fn function to evaluate
   * @throws QueryException query exception
   */
  private void eval(final UserFunc fn) throws QueryException {
    current = fn;
    final Iter ir = fn.iter(ctx);
    for(Item it; (it = ir.next()) != null;) it.materialize(info);
  }

  /**
   * Checks if an XQUnit annotation has been specified.
   * If positive, returns its offset in the annotation array.
   *
   * @param func user function
   * @param name name of annotation to be found
   * @return value
   * @throws QueryException query exception
   */
  private int indexOf(final UserFunc func, final byte[] name) throws QueryException {
    final Ann ann = func.ann;
    final int as = ann.size();
    int pos = -1;
    for(int a = 0; a < as; a++) {
      final QNm nm = ann.names[a];
      if(eq(nm.uri(), QueryText.XQUNITURI) && eq(nm.local(), name)) {
        if(pos != -1) UNIT_TWICE.thrw(info, '%', nm.local());
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
