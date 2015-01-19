package org.basex.query.func.unit;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.unit.Constants.*;
import static org.basex.query.ann.Annotation.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.Context;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XQUnit tests: Testing single modules.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class Unit {
  /** Database context. */
  private final Context ctx;
  /** File. */
  private final IOFile file;
  /** Parent process. */
  private final Proc proc;

  /** Query string. */
  private String input;
  /** Currently processed function. */
  private StaticFunc current;

  /** Failures. */
  int failures;
  /** Errors. */
  int errors;
  /** Skipped. */
  int skipped;
  /** Tests. */
  int tests;

  /**
   * Constructor.
   * @param file file
   * @param ctx database context
   * @param proc process
   */
  public Unit(final IOFile file, final Context ctx, final Proc proc) {
    this.file = file;
    this.ctx = ctx;
    this.proc = proc;
  }

  /**
   * Runs all tests.
   * @param suites root element
   * @throws IOException query exception
   */
  public void test(final FElem suites) throws IOException {
    final FElem suite = new FElem(TESTSUITE).add(NAME, file.url());
    final ArrayList<StaticFunc> beforeModule = new ArrayList<>(0);
    final ArrayList<StaticFunc> afterModule = new ArrayList<>(0);
    final ArrayList<StaticFunc> before = new ArrayList<>(0);
    final ArrayList<StaticFunc> after = new ArrayList<>(0);
    final ArrayList<QNm> beforeFilter = new ArrayList<>();
    final ArrayList<QNm> afterFilter = new ArrayList<>();
    final ArrayList<StaticFunc> test = new ArrayList<>(0);
    final Performance perf = new Performance();

    try(final QueryContext qc = new QueryContext(ctx)) {
      input = string(file.read());
      qc.parse(input, file.path(), null);

      // loop through all functions
      for(final StaticFunc sf : qc.funcs.funcs()) {
        // find Unit annotations
        final AnnList anns = sf.anns;
        boolean xq = false;
        for(final Ann ann : anns) xq |= eq(ann.sig.uri, QueryText.UNIT_URI);
        if(!xq) continue;

        // Unit function:
        if(anns.contains(PRIVATE)) throw UNIT_PRIVATE_X.get(null, sf.name.local());
        if(sf.args.length > 0) throw UNIT_ARGS_X.get(null, sf.name.local());

        if(anns.contains(_UNIT_BEFORE_MODULE)) beforeModule.add(sf);
        if(anns.contains(_UNIT_AFTER_MODULE)) afterModule.add(sf);
        final Ann b = anns.get(_UNIT_BEFORE);
        if(b != null) {
          before.add(sf);
          beforeFilter.add(name(sf, b));
        }
        final Ann a = anns.get(_UNIT_AFTER);
        if(a != null) {
          after.add(sf);
          afterFilter.add(name(sf, a));
        }
        if(anns.contains(_UNIT_TEST)) test.add(sf);
      }

      // call initializing functions before first test
      for(final StaticFunc sf : beforeModule) eval(sf);

      for(final StaticFunc sf : test) {
        // check arguments
        final AnnList anns = sf.anns;
        final Ann ann = anns.get(_UNIT_TEST);
        final Item[] args = ann.args;
        final int vs = args.length;

        // expected error code
        byte[] code = null;
        if(vs == 2 && eq(EXPECTED, args[0].string(null))) {
          code = args[1].string(null);
        } else if(vs != 0) {
          throw BASX_ANNNUM_X_X_X.get(ann.info, ann, vs, vs == 1 ? "" : "s");
        }

        final FElem testcase = new FElem(TESTCASE).add(NAME, sf.name.local());
        tests++;

        final Performance perf2 = new Performance();
        final Ann ignore = anns.get(Annotation._UNIT_IGNORE);
        if(ignore == null) {
          try {
            // call functions marked with "before"
            int l = before.size();
            for(int i = 0; i < l; i++) {
              final QNm name = beforeFilter.get(i);
              if(name == null || name.eq(sf.name)) eval(before.get(i));
            }
            // call function
            eval(sf);
            // call functions marked with "after"
            l = after.size();
            for(int i = 0; i < l; i++) {
              final QNm name = afterFilter.get(i);
              if(name == null || name.eq(sf.name)) eval(after.get(i));
            }

            if(code != null) {
              failures++;
              testcase.add(new FElem(FAILURE).add(new FElem(EXPECTED).add(code)));
            }
          } catch(final QueryException ex) {
            addError(ex, testcase, code);
          }
        } else {
          // skip test
          testcase.add(SKIPPED, ignore.args.length == 0 ? EMPTY : ignore.args[0].string(null));
          skipped++;
        }
        testcase.add(TIME, time(perf2));
        suite.add(testcase);
      }

      // run finalizing tests
      for(final StaticFunc sf : afterModule) eval(sf);

    } catch(final QueryException ex) {
      if(current == null) {
        // handle errors caused by parsing or compilation
        addError(ex, suite, null);
      } else {
        // handle errors caused by initializing or finalizing unit functions
        final FElem tc = new FElem(TESTINIT).add(NAME, current.name.local()).add(TIME, time(perf));
        suite.add(tc);
        addError(ex, tc, null);
      }
    }

    if(suite.hasChildren()) {
      suite.add(TIME, time(perf));
      suite.add(TESTS, token(tests));
      suite.add(FAILURES, token(failures));
      suite.add(ERRORS, token(errors));
      suite.add(SKIPPED, token(skipped));
      suites.add(suite);
    }
  }

  /**
   * Returns an annotation argument as QName.
   * @param sf static function
   * @param ann annotation
   * @return QName
   * @throws QueryException query exception
   */
  private QNm name(final StaticFunc sf, final Ann ann) throws QueryException {
    if(ann.args.length != 0) {
      final byte[] name = ann.args[0].string(null);
      if(name.length != 0) return QNm.resolve(name, sf.name.uri(), sf.sc, sf.info);
    }
    return null;
  }


  /**
   * Adds an error element to the specified test case.
   * @param ex exception
   * @param testcase testcase element
   * @param code error code (may be {@code null})
   */
  private void addError(final QueryException ex, final FElem testcase, final byte[] code) {
    final QNm name = ex.qname();
    if(code == null || !eq(code, name.local())) {
      final FElem error;
      final boolean fail = UNIT_ASSERT.eq(name);
      if(fail) {
        failures++;
        error = new FElem(FAILURE);
      } else {
        errors++;
        error = new FElem(ERROR);
      }
      error.add(LINE, token(ex.line()));
      error.add(COLUMN, token(ex.column()));
      final String url = IO.get(ex.file()).url();
      if(!file.url().equals(url)) error.add(URI, url);

      if(ex instanceof UnitException) {
        // unit exception: add expected and returned values
        final UnitException ue = (UnitException) ex;
        error.add(element(ue.returned, RETURNED, ue.count));
        error.add(element(ue.expected, EXPECTED, ue.count));
      } else if(!fail) {
        // exception other than failure: add type
        error.add(TYPE, ex.qname().prefixId(QueryText.ERROR_URI));
      }

      // add info
      final Value v = ex.value();
      if(v instanceof Item) {
        error.add(element((Item) v, INFO, -1));
      } else {
        // otherwise, add error message
        error.add(new FElem(INFO).add(ex.getLocalizedMessage()));
      }
      testcase.add(error);
    }
  }

  /**
   * Creates a new element.
   * @param it item
   * @param name name (expected/returned)
   * @param c item count (ignore it {@code -1})
   * @return new element
   */
  private static FElem element(final Item it, final byte[] name, final int c) {
    final FElem exp = new FElem(name);
    if(it != null) {
      if(it instanceof ANode) {
        exp.add((ANode) it);
      } else {
        try {
          exp.add(it.string(null));
        } catch(final QueryException ignored) {
          exp.add(chop(it.toString(), null));
        }
      }
      if(c != -1) exp.add(ITEM, token(c)).add(TYPE, it.type.toString());
    }
    return exp;
  }

  /**
   * Evaluates a function.
   * @param func function to evaluate
   * @throws QueryException query exception
   */
  private void eval(final StaticFunc func) throws QueryException {
    current = func;

    try(final QueryContext qctx = new QueryContext(ctx)) {
      qctx.listen = proc.listen;
      qctx.parse(input, file.path(), null);
      qctx.mainModule(new MainModule(find(qctx, func), new Expr[0]));
      // ignore results
      final Iter iter = qctx.iter();
      while(iter.next() != null);
    } finally {
      proc.proc(null);
    }
  }

  /**
   * Returns the specified function from the given query context.
   * @param qctx query context.
   * @param func function to be found
   * @return function
   */
  private static StaticFunc find(final QueryContext qctx, final StaticFunc func) {
    for(final StaticFunc sf : qctx.funcs.funcs()) {
      if(func.info.equals(sf.info)) return sf;
    }
    return null;
  }

  /**
   * Returns a token representation of the measured time.
   *
   * @param p performance
   * @return time
   */
  static byte[] time(final Performance p) {
    return new DTDur(p.time() / 1000000).string(null);
  }
}
