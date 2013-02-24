package org.basex.query.util.unit;

import static org.basex.query.util.unit.Constants.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
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
 * XQuery Unit tests.
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
      if(uf.updating) BXUN_UPDATE.thrw(info, uf.name.local());
      if(uf.args.length > 0) BXUN_ARGS.thrw(info, uf.name.local());

      if(indexOf(uf, BEFORE) != -1) before.add(uf);
      if(indexOf(uf, AFTER) != -1) after.add(uf);
      if(indexOf(uf, BEFORE_MODULE) != -1) beforeModule.add(uf);
      if(indexOf(uf, AFTER_MODULE) != -1) afterModule.add(uf);
    }

    try {
      // call initializing functions before first test
      for(final UserFunc uf : beforeModule) eval(uf, 0);

      for(final UserFunc uf : ctx.funcs.funcs()) {
        // consider only test functions that are defined in the same file
        if(!file.eq(new IOFile(uf.info.file()))) continue;
        // find test annotation
        final int pos = indexOf(uf, TEST);
        if(pos == -1) continue;

        // check arguments
        final Value values = uf.ann.values[pos];
        final long vs = values.size();
        if(values.size() % 2 != 0) BXUN_ANN.thrw(info, '%', uf.ann.names[0]);

        // expected error code
        byte[] code = null;
        long time = 0;
        for(int v = 0; v < vs; v += 2) {
          final byte[] key = values.itemAt(v).string(info);
          final byte[] val = values.itemAt(v + 1).string(info);
          if(eq(key, EXPECTED)) {
            code = val;
          } else if(eq(key, TIMEOUT)) {
            time = toInt(val);
            if(time < 0) BXUN_ANN.thrw(info, '%', uf.ann.names[0]);
          }
        }

        final FElem test = new FElem(Q_TESTCASE).add(Q_NAME, uf.name.local());
        t++;

        final Performance pt = new Performance();
        if(indexOf(uf, IGNORE) != -1) {
          // skip test
          test.add(new FElem(Q_SKIPPED));
          s++;
        } else {
          try {
            // call functions marked with "before"
            for(final UserFunc fn : before) eval(fn, 0);
            // call functions
            eval(uf, time);
            // call functions marked with "after"
            for(final UserFunc fn : after) eval(fn, 0);

            if(code != null) {
              f++;
              final FElem error = new FElem(Q_FAILURE);
              error.add(Q_MESSAGE, "Error expected.");
              error.add(Q_TYPE, code);
              test.add(error);
            }

          } catch(final ProgressException ex) {
            final FElem error = new FElem(Q_ERROR);
            error.add(Q_MESSAGE, "Timed out (" + time + " ms)");
            error.add(Q_TYPE, QueryText.XQUNIT);
            test.add(error);

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
      for(final UserFunc uf : afterModule) eval(uf, 0);

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
   * @param to timeout (0: disabled)
   * @throws QueryException query exception
   */
  private void eval(final UserFunc fn, final long to) throws QueryException {
    current = fn;
    try {
      if(to > 0) ctx.startTimeout(to);
      final Iter ir = fn.iter(ctx);
      for(Item it; (it = ir.next()) != null;) it.materialize(info);
    } finally {
      if(to > 0) ctx.stopTimeout();
    }
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
        if(pos != -1) BXUN_TWICE.thrw(info, '%', nm.local());
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
