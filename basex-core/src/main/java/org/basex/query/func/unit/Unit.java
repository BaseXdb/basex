package org.basex.query.func.unit;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.unit.Constants.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.Context;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XQUnit tests: Testing single modules.
 *
 * @author BaseX Team 2005-14, BSD License
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
        final int as = sf.ann.size();
        boolean xq = false;
        for(int a = 0; !xq && a < as; a++) {
          xq |= eq(sf.ann.names[a].uri(), QueryText.UNIT_URI);
        }
        if(!xq) continue;

        // Unit function:
        if(sf.ann.contains(Ann.Q_PRIVATE)) throw UNIT_PRIVATE_X.get(null, sf.name.local());
        if(sf.args.length > 0) throw UNIT_ARGS_X.get(null, sf.name.local());

        if(indexOf(sf, BEFORE_MODULE) != -1) beforeModule.add(sf);
        if(indexOf(sf, AFTER_MODULE) != -1) afterModule.add(sf);
        int i = indexOf(sf, BEFORE);
        if(i != -1) {
          before.add(sf);
          beforeFilter.add(name(sf, i));
        }
        i = indexOf(sf, AFTER);
        if(i != -1) {
          after.add(sf);
          afterFilter.add(name(sf, i));
        }
        if(indexOf(sf, TEST) != -1) test.add(sf);
      }

      // call initializing functions before first test
      for(final StaticFunc sf : beforeModule) eval(sf);

      for(final StaticFunc sf : test) {
        // check arguments
        final Value values = sf.ann.values[indexOf(sf, TEST)];
        final long vs = values.size();

        // expected error code
        byte[] code = null;
        if(vs != 0) {
          if(vs == 2 && eq(EXPECTED, values.itemAt(0).string(null))) {
            code = values.itemAt(1).string(null);
          } else {
            throw UNIT_ANN_X_X.get(null, '%', sf.ann.names[0]);
          }
        }

        final FElem testcase = new FElem(TESTCASE).add(NAME, sf.name.local());
        tests++;

        final Performance perf2 = new Performance();
        final int skip = indexOf(sf, IGNORE);
        if(skip == -1) {
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
          final Value sv = sf.ann.values[skip];
          testcase.add(SKIPPED, sv.isEmpty() ? EMPTY : sv.itemAt(0).string(null));
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
   * Returns an annotation argument at the specified offset as QName.
   * @param sf static function
   * @param i index
   * @return QName
   * @throws QueryException query exception
   */
  private QNm name(final StaticFunc sf, final int i) throws QueryException {
    if(!sf.ann.values[i].isEmpty()) {
      final byte[] name = sf.ann.values[i].itemAt(i).string(null);
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
      qctx.compile();

      final Iter iter = qctx.iter();
      if(iter.next() != null) throw UNIT_EMPTY_X.get(null, func.name.local());
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
   * Checks if the specified unit annotation has been specified.
   * If positive, returns its offset in the annotation array.
   *
   * @param func user function
   * @param name name of annotation to be found
   * @return value
   * @throws QueryException query exception
   */
  private static int indexOf(final StaticFunc func, final byte[] name) throws QueryException {
    final Ann ann = func.ann;
    final int as = ann.size();
    int pos = -1;
    for(int a = 0; a < as; a++) {
      final QNm nm = ann.names[a];
      if(eq(nm.uri(), QueryText.UNIT_URI) && eq(nm.local(), name)) {
        if(pos != -1) throw UNIT_TWICE_X_X.get(null, '%', nm.local());
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
  static byte[] time(final Performance p) {
    return new DTDur(p.time() / 1000000).string(null);
  }
}
