package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.SeqType.*;

import java.io.*;

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
import org.nineml.coffeefilter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public class FnInvisibleXml extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String grammar = toString(exprs[0], qc);
    final InvisibleXmlParser parser = new InvisibleXml().getParserFromIxml(grammar);
    if (!parser.constructed()) {
      final Exception ex = parser.getException();
      if (ex != null) throw IXML_UNEXPECTED_X.get(info, ex);
      InvisibleXmlDocument doc = parser.getFailedParse();
      throw IXML_GEN_X_X_X.get(info, doc.getResult().getLastToken(),
          doc.getLineNumber(), doc.getColumnNumber());
    }
    final Var[] params = {new VarScope(sc).addNew(new QNm("input"), STRING_O, true, qc, ii)};
    final Expr arg = new VarRef(ii, params[0]);
    final ParseInvisibleXml parseFunction = new ParseInvisibleXml(ii, arg, parser);
    final FuncType type = FuncType.get(parseFunction.seqType(), STRING_O);
    return new FuncItem(sc, new AnnList(), null, params, type, parseFunction, params.length, ii);
  }

  /**
   * Result function of fn:invisible-xml: parse invisible XML input.
   */
  private static class ParseInvisibleXml extends Arr {
    /** Generated invisible XML parser. */
    private final InvisibleXmlParser parser;

    /**
     * Constructor.
     * @param info input info
     * @param arg function argument
     * @param parser generated invisible XML parser
     */
    protected ParseInvisibleXml(final InputInfo info, final Expr arg,
        final InvisibleXmlParser parser) {
      super(info, DOCUMENT_NODE_O, arg);
      this.parser = parser;
    }

    @Override
    public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
      final String input = toString(exprs[0].atomItem(qc, ii), qc);
      final InvisibleXmlDocument doc = parser.parse(input);
      if (!doc.succeeded()) {
        throw IXML_INP_X_X_X.get(ii, doc.getResult().getLastToken(),
            doc.getLineNumber(), doc.getColumnNumber());
      }
      try {
        return new DBNode(IO.get(doc.getTree()));
      } catch(final IOException ex) {
        throw IXML_RESULT_X.get(ii, ex);
      }
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
      return copyType(new ParseInvisibleXml(info, exprs[0].copy(cc, vm), parser));
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token("parse-invisible-xml").params(exprs);
    }
  }
}
