package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XQUnit functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNXQUnit extends StandardFunc {
  /** Token: test. */
  private static final byte[] A_TEST = token("test");
  /** Token: skipped. */
  private static final byte[] A_IGNORE = token("ignore");
  /** Token: before. */
  private static final byte[] A_BEFORE = token("before");
  /** Token: before. */
  private static final byte[] A_AFTER = token("after");
  /** Token: before. */
  private static final byte[] A_BEFORE_MODULE = token("before-module");
  /** Token: before. */
  private static final byte[] A_AFTER_MODULE = token("after-module");

  /** QName: testsuites. */
  private static final QNm Q_TESTSUITES = new QNm("testsuites");
  /** QName: testsuite. */
  private static final QNm Q_TESTSUITE = new QNm("testsuite");
  /** QName: testcase. */
  private static final QNm Q_TESTCASE = new QNm("testcase");
  /** QName: failure. */
  private static final QNm Q_FAILURE = new QNm("failure");
  /** QName: error. */
  private static final QNm Q_ERROR = new QNm("error");
  /** QName: failure. */
  private static final QNm Q_FAILURES = new QNm("failures");
  /** QName: error. */
  private static final QNm Q_ERRORS = new QNm("errors");
  /** QName: skipped. */
  private static final QNm Q_SKIPPED = new QNm("skipped");
  /** QName: error. */
  private static final QNm Q_TESTS = new QNm("tests");
  /** QName: name. */
  private static final QNm Q_NAME = new QNm("name");
  /** QName: message. */
  private static final QNm Q_MESSAGE = new QNm("message");
  /** QName: type. */
  private static final QNm Q_TYPE = new QNm("type");
  /** QName: time. */
  private static final QNm Q_TIME = new QNm("time");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNXQUnit(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _XQUNIT_ASSERT:         return assrt(ctx);
      case _XQUNIT_FAIL:           return fail(ctx);
      case _XQUNIT_TEST:           return test(ctx);
      case _XQUNIT_TEST_LIBRARIES: return testLibraries(ctx);
      default:                     return super.item(ctx, ii);
    }
  }

  /**
   * Performs the assert function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item assrt(final QueryContext ctx) throws QueryException {
    final byte[] str = expr.length < 2 ? null : checkStr(expr[1], ctx);
    if(expr[0].ebv(ctx, info).bool(info)) return null;
    throw str == null ? BXUN_ASSERT.thrw(info) : BXUN_ERROR.thrw(info, str);
  }

  /**
   * Performs the fail function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item fail(final QueryContext ctx) throws QueryException {
    throw BXUN_FAIL.thrw(info, checkStr(expr[0], ctx));
  }

  /**
   * Performs the run-all function (still experimental).
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item testLibraries(final QueryContext ctx) throws QueryException {
    final FElem suites = new FElem(Q_TESTSUITES);

    checkCreate(ctx);
    final Iter ir = ctx.iter(expr[0]);
    for(Item it; (it = ir.next()) != null;) {
      final String path = string(checkStr(it));
      final IO io = IO.get(path);

      if(!io.exists()) WHICHRES.thrw(info, path);
      try {
        final QueryContext qc = new QueryContext(ctx.context);
        qc.sc.baseURI(io.path());
        try {
          qc.module(string(io.read()));
          qc.compile();
          suites.add(test(qc));
        } finally {
          qc.close();
        }
      } catch(final IOException ex) {
        throw IOERR.thrw(info, ex);
      }
    }
    return suites;
  }

  /**
   * Performs the run function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private FElem test(final QueryContext ctx) throws QueryException {
    final FElem tests = new FElem(Q_TESTSUITE);
    tests.add(Q_NAME, ctx.sc.baseURI().string());
    int t = 0, e = 0, f = 0, s = 0;

    final IO file = ctx.sc.baseIO();
    final Performance p = new Performance();

    // check properties of test functions
    final ArrayList<UserFunc> before = new ArrayList<UserFunc>();
    final ArrayList<UserFunc> after = new ArrayList<UserFunc>();
    final ArrayList<UserFunc> beforeModule = new ArrayList<UserFunc>();
    final ArrayList<UserFunc> afterModule = new ArrayList<UserFunc>();

    for(final UserFunc uf : ctx.funcs.funcs()) {
      // consider only functions that are defined in the same file
      if(!file.eq(new IOFile(uf.info.file()))) continue;

      // find XQUnit annotations
      final Ann ann = uf.ann;
      final int as = ann.size();
      boolean xq = false;
      for(int a = 0; !xq && a < as; a++) xq |= eq(ann.names[a].uri(), XQUNITURI);
      if(!xq) continue;

      // XQUnit function:
      if(uf.updating) BXUN_UPDATE.thrw(info, uf.name.local());
      if(uf.args.length > 0) BXUN_ARGS.thrw(info, uf.name.local());

      if(find(uf, A_BEFORE)) before.add(uf);
      if(find(uf, A_AFTER)) after.add(uf);
      if(find(uf, A_BEFORE_MODULE)) beforeModule.add(uf);
      if(find(uf, A_AFTER_MODULE)) afterModule.add(uf);
    }

    boolean init = true;
    for(final UserFunc uf : ctx.funcs.funcs()) {
      // consider only test functions that are defined in the same file
      if(!file.eq(new IOFile(uf.info.file())) || !find(uf, A_TEST)) continue;

      final FElem test = new FElem(Q_TESTCASE).add(Q_NAME, uf.name.local());
      t++;

      final Performance pt = new Performance();
      if(find(uf, A_IGNORE)) {
        // skip test
        test.add(new FElem(Q_SKIPPED));
        s++;
      } else {
        try {
          // call initializing functions before first test
          if(init) {
            for(final UserFunc fn : beforeModule) fn.value(ctx);
            init = false;
          }
          // call functions marked with "before"
          for(final UserFunc fn : before) fn.value(ctx);
          // call functions
          uf.value(ctx);
          // call functions marked with "after"
          for(final UserFunc fn : after) fn.value(ctx);

        } catch(final QueryException ex) {
          final boolean failure = eq(ex.qname().uri(), XQUNITURI);
          if(failure) f++;
          else e++;

          final QNm nm = failure ? Q_FAILURE : Q_ERROR;
          final FElem error = new FElem(nm);
          error.add(Q_MESSAGE, ex.getLocalizedMessage());
          error.add(Q_TYPE, ex.qname().local());
          test.add(error);
        }
      }
      test.add(Q_TIME, time(pt));
      tests.add(test);
    }

    try {
      // run finalizing tests
      for(final UserFunc uf : afterModule) uf.value(ctx);
    } catch(final QueryException ignored) { }

    tests.add(Q_TIME, time(p));
    tests.add(Q_TESTS, token(t));
    tests.add(Q_FAILURES, token(f));
    tests.add(Q_ERRORS, token(e));
    tests.add(Q_SKIPPED, token(s));
    return tests;
  }

  /**
   * Returns the value of the specified XQUnit annotation, or {@code null}.
   *
   * @param func user function
   * @param name name of annotation to be found
   * @return value
   * @throws QueryException query exception
   */
  private boolean find(final UserFunc func, final byte[] name) throws QueryException {
    final Ann ann = func.ann;
    final int as = ann.size();
    boolean found = false;
    for(int a = 0; a < as; a++) {
      final QNm nm = ann.names[a];
      if(eq(nm.uri(), XQUNITURI) && eq(nm.local(), name)) {
        if(found) BXUN_TWICE.thrw(info, '%', nm.local());
        found = true;
      }
    }
    return found;
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

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT || super.uses(u);
  }
}
