package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.SeqType.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

import de.bottlecaps.markup.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public final class FnInvisibleXml extends StandardFunc {
  /** The invisible XML parser generator. */
  private Generator generator;

  @Override
  public FuncItem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(generator == null) {
      for(final String className : Arrays.asList("de.bottlecaps.markup.Blitz",
          "de.bottlecaps.markup.BlitzException", "de.bottlecaps.markup.BlitzParseException")) {
        if(!Reflect.available(className)) {
          throw BASEX_CLASSPATH_X_X.get(info, definition.local(), className);
        }
      }
      generator = new Generator();
    }
    return generator.generate(qc);
  }

  /**
   * Invisible XML parser generator.
   */
  private final class Generator {
    /**
     * Generate a parser from an invisible XML grammar.
     * @param qc query context
     * @return the parsing function
     * @throws QueryException query exception
     */
    public FuncItem generate(final QueryContext qc) throws QueryException {
      final Item value = arg(0).atomItem(qc, info);
      final String grammar = value.isEmpty()
          ? Blitz.ixmlGrammar()
          : FnInvisibleXml.this.toString(value);
      final IxmlOptions opts = toOptions(arg(1), new IxmlOptions(), true, qc);
      final de.bottlecaps.markup.blitz.Parser parser;
      try {
        parser = opts.get(IxmlOptions.FAIL_ON_ERROR)
            ? Blitz.generate(grammar, Blitz.Option.FAIL_ON_ERROR)
            : Blitz.generate(grammar);
      } catch(final BlitzParseException ex) {
        throw IXML_GRM_X_X_X.get(info, ex.getOffendingToken(), ex.getLine(), ex.getColumn());
      } catch(final BlitzException ex) {
        throw IXML_GEN_X.get(info, ex);
      }
      final Var[] params = { new VarScope(sc).addNew(new QNm("input"), STRING_O, true, qc, info)};
      final Expr arg = new VarRef(info, params[0]);
      final ParseInvisibleXml parseFunction = new ParseInvisibleXml(info, parser, arg);
      final FuncType ft = FuncType.get(parseFunction.seqType(), STRING_O);
      return new FuncItem(info, parseFunction, params, AnnList.EMPTY, ft, sc, params.length, null);
    }
  }

  /**
   * Result function of fn:invisible-xml: parse invisible XML input.
   */
  private static final class ParseInvisibleXml extends Arr {
    /** Generated invisible XML parser. */
    private final de.bottlecaps.markup.blitz.Parser parser;

    /**
     * Constructor.
     * @param info input info (can be {@code null})
     * @param args function arguments
     * @param parser generated invisible XML parser
     */
    private ParseInvisibleXml(final InputInfo info, final de.bottlecaps.markup.blitz.Parser parser,
        final Expr... args) {
      super(info, DOCUMENT_NODE_O, args);
      this.parser = parser;
    }

    @Override
    public DBNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
      final String input = toString(arg(0), qc);
      try {
        final String output = parser.parse(input);
        return new DBNode(IO.get(output));
      } catch(final BlitzParseException ex) {
        throw IXML_INP_X_X_X.get(ii, ex.getOffendingToken(), ex.getLine(), ex.getColumn());
      } catch(BlitzException | IOException ex) {
        throw IXML_RESULT_X.get(info, ex);
      }
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
      return copyType(new ParseInvisibleXml(info, parser, copyAll(cc, vm, args())));
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token("parse-invisible-xml").params(exprs);
    }
  }

  /**
   * Options for fn:invisible-xml.
   */
  public static final class IxmlOptions extends Options {
    /** Invisible XML option fail-on-error. */
    public static final BooleanOption FAIL_ON_ERROR = new BooleanOption("fail-on-error", false);
  }

}
