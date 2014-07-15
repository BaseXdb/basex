package org.basex.query.util.unit;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.unit.Constants.*;
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
 * XQuery Unit tests: Testing single modules.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Unit {
  /** Database context. */
  private final Context ctx;
  /** File. */
  private final IOFile file;
  /** Parent process. */
  private final Proc proc;

  /** File contents. */
  private String input;
  /** Functions. */
  private StaticFunc[] funcs;
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
    final ArrayList<StaticFunc> before = new ArrayList<>(0);
    final ArrayList<StaticFunc> after = new ArrayList<>(0);
    final ArrayList<StaticFunc> beforeModule = new ArrayList<>(0);
    final ArrayList<StaticFunc> afterModule = new ArrayList<>(0);
    final ArrayList<StaticFunc> test = new ArrayList<>(0);
    final Performance perf = new Performance();

    try {
      final QueryContext qc = new QueryContext(ctx);
      input = string(file.read());
      qc.parse(input, file.path(), null);
      funcs = qc.funcs.funcs();

      // loop through all functions
      for(final StaticFunc sf : funcs) {
        // find Unit annotations
        final Ann ann = sf.ann;
        final int as = ann.size();
        boolean xq = false;
        for(int a = 0; !xq && a < as; a++) {
          xq |= eq(ann.names[a].uri(), QueryText.UNITURI);
        }
        if(!xq) continue;

        // Unit function:
        if(sf.ann.contains(Ann.Q_PRIVATE)) throw UNIT_PRIVATE.get(null, sf.name.local());
        if(sf.args.length > 0) throw UNIT_ARGS.get(null, sf.name.local());

        if(indexOf(sf, BEFORE) != -1) before.add(sf);
        if(indexOf(sf, AFTER) != -1) after.add(sf);
        if(indexOf(sf, BEFORE_MODULE) != -1) beforeModule.add(sf);
        if(indexOf(sf, AFTER_MODULE) != -1) afterModule.add(sf);
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
            throw UNIT_ANN.get(null, '%', sf.ann.names[0]);
          }
        }

        final FElem testcase = new FElem(TESTCASE).add(NAME, sf.name.local());
        tests++;

        final Performance perf2 = new Performance();
        final int skip = indexOf(sf, IGNORE);
        if(skip == -1) {
          try {
            // call functions marked with "before"
            for(final StaticFunc fn : before) eval(fn);
            // call functions
            eval(sf);
            // call functions marked with "after"
            for(final StaticFunc fn : after) eval(fn);

            if(code != null) {
              failures++;
              testcase.add(new FElem(FAILURE).add(new FElem(EXPECTED).add(code)));
            }
          } catch(final QueryException ex) {
            final QNm name = ex.qname();
            if(code == null || !eq(code, name.local())) {
              final FElem error;
              final boolean fail = Err.UNIT_ASSERT.eq(name);
              if(fail) {
                failures++;
                error = new FElem(FAILURE);
              } else {
                errors++;
                error = new FElem(ERROR);
              }
              addError(ex, error, fail);
              testcase.add(error);
            }
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
      final FElem error;
      if(current == null) {
        // handle errors caused by parsing or compiling a module
        error = new FElem(ERROR);
        try {
          addError(ex, error, true);
        } catch(final QueryException ignored) { }
      } else {
        // handle errors caused by initializing unit functions
        error = new FElem(TESTCASE).add(NAME, current.name.local());
        error.add(TIME, time(perf));
      }
      suite.add(error);
      errors++;
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
   * Adds error information to the specified node.
   * @param ex exception
   * @param error error element
   * @param fail fail (unit error) flag
   * @throws QueryException query exception
   */
  private void addError(final QueryException ex, final FElem error, final boolean fail)
      throws QueryException {

    error.add(LINE, token(ex.line()));
    error.add(COLUMN, token(ex.column()));
    final String url = IO.get(ex.file()).url();
    if(!file.url().equals(url)) error.add(URI, url);

    if(ex instanceof UnitException) {
      // unit exception: add expected and returned values
      final UnitException ue = (UnitException) ex;
      error.add(elem(ue.returned, RETURNED, ue.count));
      error.add(elem(ue.expected, EXPECTED, ue.count));
    } else if(fail) {
      // unit failure:
      final Value v = ex.value();
      if(v.isItem()) {
        // add attached value
        if(v.type.isNode()) {
          error.add((ANode) v);
        } else {
          error.add(((Item) v).string(null));
        }
      } else {
        // otherwise, add error message
        error.add(ex.getLocalizedMessage());
      }
    } else {
      // any other exception: add type and error message
      error.add(TYPE, ex.qname().prefixId(QueryText.ERRORURI));
      error.add(ex.getLocalizedMessage());
    }
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
      exp.add(it.string(null));
    }
    exp.add(ITEM, token(c));
    exp.add(TYPE, it.type.toString());
    return exp;
  }

  /**
   * Evaluates a function.
   * @param func function to evaluate
   * @throws QueryException query exception
   */
  private void eval(final StaticFunc func) throws QueryException {
    current = func;

    final QueryContext qctx = proc.proc(new QueryContext(ctx));
    qctx.listen = proc.listen;
    try {
      qctx.parse(input, file.path(), null);

      // assign main module and http context and register process
      final MainModule mm = MainModule.get(func, new Expr[0]);
      qctx.mainModule(mm);
      qctx.compile();

      final Iter iter = qctx.iter();
      while(iter.next() != null) throw UNIT_EMPTY.get(null, func.name.local());

    } finally {
      proc.proc(null);
      qctx.close();
    }
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
        if(pos != -1) throw UNIT_TWICE.get(null, '%', nm.local());
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
