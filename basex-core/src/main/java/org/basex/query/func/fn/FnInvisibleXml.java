package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.nineml.coffeefilter.*;
import org.nineml.coffeefilter.exceptions.*;
import org.nineml.coffeegrinder.parser.*;

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
      for(final String className : Arrays.asList("org.nineml.coffeegrinder.parser.GearleyResult",
          "org.nineml.coffeefilter.exceptions.IxmlException",
          "org.nineml.coffeefilter.InvisibleXmlDocument",
          "org.nineml.coffeefilter.InvisibleXmlParser", "org.nineml.coffeefilter.InvisibleXml")) {
        if(!Reflect.available(className)) {
          throw BASEX_CLASSPATH_X_X.get(ii, definition.local(), className);
        }
      }
      generator = new Generator();
    }
    return generator.generate(qc, ii, sc, toString(arg(0), qc));
  }

  /**
   * Invisible XML parser generator.
   */
  private static final class Generator {
    /**
     * Generate a parser from an invisible XML grammar.
     * @param qc query context
     * @param ii input info
     * @param sc static context
     * @param grammar the invisible XML grammar
     * @return the parsing function
     * @throws QueryException query exception
     */
    public FuncItem generate(final QueryContext qc, final InputInfo ii, final StaticContext sc,
        final String grammar) throws QueryException {
      final InvisibleXmlParser parser = new InvisibleXml().getParserFromIxml(grammar);
      if(!parser.constructed()) {
        final Exception ex = parser.getException();
        if(ex != null) throw IXML_GEN_X.get(ii, ex);
        final InvisibleXmlDocument doc = parser.getFailedParse();
        final GearleyResult result = doc.getResult();
        throw IXML_GRM_X_X_X.get(ii, result.getLastToken(), doc.getLineNumber(),
            doc.getColumnNumber());
      }
      final Var[] params = { new VarScope(sc).addNew(new QNm("input"), STRING_O, true, qc, ii) };
      final Expr arg = new VarRef(ii, params[0]);
      final ParseInvisibleXml parseFunction = new ParseInvisibleXml(ii, parser, arg);
      final FuncType type = FuncType.get(parseFunction.seqType(), STRING_O);
      return new FuncItem(sc, null, null, params, type, parseFunction, params.length, ii);
    }
  }

  /**
   * Result function of fn:invisible-xml: parse invisible XML input.
   */
  private static final class ParseInvisibleXml extends Arr {
    /** Generated invisible XML parser. */
    private final InvisibleXmlParser parser;

    /**
     * Constructor.
     * @param info input info
     * @param args function arguments
     * @param parser generated invisible XML parser
     */
    private ParseInvisibleXml(final InputInfo info, final InvisibleXmlParser parser,
        final Expr... args) {
      super(info, DOCUMENT_NODE_O, args);
      this.parser = parser;
    }

    @Override
    public DBNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
      final String input = toString(arg(0), qc);
      final InvisibleXmlDocument doc = parser.parse(input);
      if(!doc.succeeded()) {
        final GearleyResult result = doc.getResult();
        throw IXML_INP_X_X_X.get(ii, result.getLastToken(), doc.getLineNumber(),
            doc.getColumnNumber());
      }
      final MemBuilder builder = new MemBuilder(Parser.emptyParser(new MainOptions())).init();
      try {
        doc.getTree(new SAXHandler(builder));
        return new DBNode(builder.data());
      } catch(final IxmlException ex) {
        throw IXML_RESULT_X.get(ii, ex);
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
}
