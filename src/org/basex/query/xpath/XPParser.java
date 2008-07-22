package org.basex.query.xpath;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xpath.XPText.*;
import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.FTOpt;
import org.basex.query.FTPos;
import org.basex.query.QueryParser;
import org.basex.query.QueryException;
import org.basex.query.FTOpt.FTMode;
import org.basex.query.FTPos.FTUnit;
import org.basex.query.xpath.expr.And;
import org.basex.query.xpath.expr.Calculation;
import org.basex.query.xpath.expr.Equality;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTAnd;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTContains;
import org.basex.query.xpath.expr.FTMildNot;
import org.basex.query.xpath.expr.FTOr;
import org.basex.query.xpath.expr.FTSelect;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.expr.FTWords;
import org.basex.query.xpath.expr.FTUnaryNot;
import org.basex.query.xpath.expr.Filter;
import org.basex.query.xpath.expr.Or;
import org.basex.query.xpath.expr.Path;
import org.basex.query.xpath.expr.Relational;
import org.basex.query.xpath.expr.Unary;
import org.basex.query.xpath.expr.Union;
import org.basex.query.xpath.func.Func;
import org.basex.query.xpath.func.XPathContext;
import org.basex.query.xpath.locpath.Axis;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathAbs;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Preds;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.Test;
import org.basex.query.xpath.locpath.TestName;
import org.basex.query.xpath.locpath.TestNode;
import org.basex.query.xpath.locpath.TestPI;
import org.basex.query.xpath.values.Calc;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Num;
import org.basex.util.Array;
import org.basex.util.Set;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * XPath Processor, containing the XPath parser. The {@link #parse()} method
 * evaluates the query and returns the parsed expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public final class XPParser extends QueryParser {
  /** Node reference. */
  Nodes nodes;
  
  /**
   * Constructor.
   * @param q query
   */
  public XPParser(final String q) {
    this(q, null);
  }
  
  /**
   * Constructor, specifying a node set.
   * @param q query
   * @param n context nodes
   */
  public XPParser(final String q, final Nodes n) {
    nodes = n;
    init(q);
  }

  /**
   * Parses the query and returns a query context.
   * @return query context
   * @throws QueryException parsing exception
   */
  public XPContext parse() throws QueryException {
    return parse(true);
  }

  /**
   * Parses the query and returns a query context.
   * @param end if true, input must be completely evaluated
   * @return query context
   * @throws QueryException parsing exception
   */
  public XPContext parse(final boolean end) throws QueryException {
    try {
      final Expr e = or();
      if(end && qp != ql) error(QUERYEND, rest());
      return new XPContext(e, qu);
    } catch(final QueryException ex) {
      mark();
      ex.pos(this);
      throw ex;
    }
  }

  /**
   * Parses an OrExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr or() throws QueryException {
    final Expr e = and();
    consumeWS();
    if(!consume(OR)) return e;

    Expr[] list = { e };
    do list = add(list, and()); while(consume(OR));
    return new Or(list);
  }

  /**
   * Parses an AndExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr and() throws QueryException {
    final Expr e = equality();
    consumeWS();
    if(!consume(AND)) return e;

    Expr[] list = { e };
    do list = add(list, equality()); while(consume(AND));
    return new And(list);
  }

  /**
   * Parses an EqualityExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr equality() throws QueryException {
    Expr e = relational();
    consumeWS();

    while(true) {
      final Comp c = cmp(new Comp[] {
          Comp.EQ, Comp.NE, Comp.APPRWORD, Comp.APPR });
      if(c == null) return e;
      e = new Equality(e, relational(), c);
    }
  }

  /**
   * Parses a RelationalExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr relational() throws QueryException {
    Expr e = ftContains();
    consumeWS();

    while(true) {
      final Comp c = cmp(new Comp[] { Comp.LE, Comp.LT, Comp.GE, Comp.GT });
      if(c == null) return e;
      e = new Relational(e, ftContains(), c);
    }
  }

  /**
   * Parses a comparator.
   * @param comp array with available comparators
   * @return found comparator or null
   */
  private Comp cmp(final Comp[] comp) {
    for(final Comp c : comp) if(consume(c.name)) return c;
    return null;
  }

  /**
   * Parses an FTContainsExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr ftContains() throws QueryException {
    final Expr e = additive();
    consumeWS();
    if(!consume(FTCONTAINS)) return e;

    consumeWS();
    return new FTContains(e, ftSelection());
  }

  /**
   * Parses an AdditiveExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr additive() throws QueryException {
    Expr e = multiplicative();
    consumeWS();

    while(true) {
      final Calc c = calc(new Calc[] { Calc.PLUS, Calc.MINUS });
      if(c == null) return e;
      e = new Calculation(e, multiplicative(), c);
    }
  }

  /**
   * Parses a MultiplicativeExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr multiplicative() throws QueryException {
    Expr e = unary();
    consumeWS();

    while(true) {
      final Calc c = calc(new Calc[] { Calc.MULT, Calc.DIV, Calc.MOD });
      if(c == null) return e;
      e = new Calculation(e, unary(), c);
    }
  }

  /**
   * Parses a calculator.
   * @param calc array with available calculators
   * @return found comparator or null
   */
  private Calc calc(final Calc[] calc) {
    for(final Calc c : calc) if(consume(c.name)) return c;
    return null;
  }

  /**
   * Parses a UnaryExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr unary() throws QueryException {
    Expr e = null;
    consumeWS();

    while(consume('-')) e = new Unary(unary());
    return e != null ? e : union();
  }

  /**
   * Parses a UnionExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr union() throws QueryException {
    final Expr e = path();
    consumeWS();
    if(!consume('|')) return e;

    Expr[] list = { nodeCheck(e) };
    do list = add(list, nodeCheck(path())); while(consume('|'));
    return new Union(list);
  }

  /**
   * Parses a PathExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr path() throws QueryException {
    consumeWS();

    // simple LocationPath cases
    final char c = curr();
    if(c == '@' || c == '*' || c == '.' || c == '/') return locPath();

    // other possible LocationPath cases
    final int p = qp;
    final String name = name();
    consumeWS();

    // if name is following
    if(name.length() != 0 && (curr() != '(' || name.equals(NODE) ||
        name.equals(TEXT) || name.equals(COMMENT) || name.equals(PI))) {
      qp = p;
      return locPath();
    }
    qp = p;

    final Expr e = filter();
    consumeWS();
    if(!consume('/')) return e;

    nodeCheck(e);
    final LocPath path = new LocPathRel();
    if(consume('/')) {
      path.steps.add(Axis.create(Axis.DESCORSELF, TestNode.NODE));
    }
    return new Path(e, relLocPath(path));
  }

  /**
   * Parses a LocationPath.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private LocPath locPath() throws QueryException {
    return curr('/') ? absLocPath(new LocPathAbs()) :
      relLocPath(new LocPathRel());
  }

  /**
   * Parses an AbsoluteLocationPath.
   * @param path location path
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private LocPath absLocPath(final LocPath path) throws QueryException {
    boolean more = false;
    while(consume('/')) {
      final char c = curr();
      if(c != 0 && c != ' ' && c != ']' && c != ')' && c != '|' && c != ',') {
        path.steps.add(step());
        more = true;
      } else if(more) {
        error(NOLOCSTEP);
      }
    }
    return path;
  }

  /**
   * Parses a RelativeLocationPath.
   * @param path location path
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private LocPath relLocPath(final LocPath path) throws QueryException {
    path.steps.add(step());
    while(consume('/')) path.steps.add(step());
    return path;
  }

  /**
   * Parses a LocationStep.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Step step() throws QueryException {
    if(curr('/')) {
      final char c = next();
      if(letter(c) || c == '@' || c == '*' || c == '.') {
        return Axis.create(Axis.DESCORSELF, TestNode.NODE);
      }
      error(NOLOCSTEP);
    }

    Axis axis = null;
    Test nodetest = null;

    if(consume('.')) {
      // AbbreviatedStep
      if(consume('.')) {
        // parent axis
        axis = Axis.PARENT;
      } else {
        // self axis
        axis = Axis.SELF;
      }
      nodetest = TestNode.NODE;
    } else if(curr('@')) {
      consume('@');
      axis = Axis.ATTR;
      final String test = name();
      if(test.length() != 0) {
        nodetest = new TestName(Token.token(test), false);
      } else if(consume('*')) {
        nodetest = new TestName(false);
      } else {
        error(NOATTNAME);
      }
    } else if(consume('*')) {
      axis = Axis.CHILD;
      nodetest = new TestName(true);
    } else if(letter(curr())) {
      axis = axis();
      nodetest = test(axis);
    } else {
      error(NOLOCSTEP);
    }

    final Preds preds = new Preds();
    consumeWS();
    while(curr('[')) {
      preds.add(pred());
      consumeWS();
    }

    return Axis.create(axis, nodetest, preds);
  }

  /**
   * Parses a NodeTest.
   * @param axis current axis
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Test test(final Axis axis) throws QueryException {
    final String test = name();

    // all elements/attributes
    if(test.length() == 0) {
      if(consume('*')) return new TestName(true);
      error(NOTEST);
    }

    consumeWS();
    if(consume('(')) {
      TestNode result = null;

      if(test.equals(PI)) {
        if(curr(0)) error(PIBRACKETS);

        final char delim = curr();
        if(delim != ')') {
          if(delim != '"' && delim != '\'') error(PIQUOTE);
          consume(delim);
          final String pi = name();
          if(!consume(delim)) error(PIQUOTE);
          if(!consume(')')) error(PIBRACKETS);
          return new TestPI(token(pi));
        }
        result = TestNode.PI;
      } else if(test.equals(NODE)) {
        result = TestNode.NODE;
      } else if(test.equals(COMMENT)) {
        result = TestNode.COMM;
      } else if(test.equals(TEXT)) {
        result = TestNode.TEXT;
      } else {
        error(UNKNOWNKIND, test);
      }
      consumeWS();
      if(consume(')')) return result;
      error(KINDCLOSE);
    }

    // NameTest
    return new TestName(token(test), axis != Axis.ATTR);
  }

  /**
   * Parses an Axis.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Axis axis() throws QueryException {
    // AxisName '::' or default axis (child)
    final String name = name();

    // Axis Specifier
    if(consume(DBLCOLON)) {
      // try to get axis reference
      final Axis ax = Axis.find(name);
      if(ax == null) error(UNKNOWNAXIS, name);
      return ax;
    }
    // default axis
    back(name.length());
    return Axis.CHILD;
  }

  /**
   * Parses a Predicate.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr pred() throws QueryException {
    consume('[');
    final Expr p = or();
    return consume(']') ? p : p instanceof FTContains ? error(FTINCOMP) :
      error(UNFINISHEDPRED);
  }

  /**
   * Parses a FilterExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr filter() throws QueryException {
    Expr e = primary();
    consumeWS();
    while(curr('[')) {
      nodeCheck(e);
      final Preds preds = new Preds();
      preds.add(pred());
      e = new Filter(e, preds);
    }
    return e;
  }

  /**
   * Parses a PrimaryExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr primary() throws QueryException {
    consumeWS();
    if(consume('$')) return varReference();
    if(consume('(')) {
      final Expr e = or();
      return consume(')') ? e : error(NOPARENTHESIS);
    }
    if(curr('"') || curr('\'')) return literal();
    if(digit(curr())) return number();
    return function();
  }

  /**
   * Parses a VariableReference.
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Item varReference() throws QueryException {
    final String var = name();
    if(var.length() == 0) error(NOVARNAME);
    return XPathContext.get().evalVariable(token(var));
  }

  /**
   * Parses a Literal and expects the first character to be a quote.
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Literal literal() throws QueryException {
    final char delim = consume();
    if(!quote(delim)) error(WRONGTEXT, QUOTE, delim);
    tk.reset();
    while(!curr(0) && !curr(delim)) entity(tk);
    if(!consume(delim)) error(QUOTECLOSE);
    return new Literal(tk.finish());
  }

  /**
   * Parses a Number.
   * @return resulting expression
   */
  private Num number() {
    return new Num(num());
  }

  /**
   * Parses a Number.
   * @return resulting expression
   */
  private double num() {
    tk.reset();
    boolean dot = true;
    boolean exp = true;
    char curr = curr();
    while(curr != 0 && (digit(curr) || dot && exp && curr == '.' ||
        exp && (curr == 'E' || curr == 'e'))) {
      if(curr == '.') {
        dot = false;
      } else if(curr == 'e' || curr == 'E') {
        exp = false;
      }
      tk.add(consume());
      curr = curr();
    }
    return toDouble(tk.finish());
  }

  /**
   * Parses a FunctionCall.
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Func function() throws QueryException {
    final String func = name();
    if(func.length() == 0) return null;
    
    consumeWS();
    if(!consume('(')) error(UNEXPECTEDEND);

    // name and opening bracket found
    Expr[] list = {};
    while(!curr(0)) {
      consumeWS();
      if(consume(')')) {
        return XPathContext.get().getFunction(token(func), list);
      }
      if(list.length != 0 && !consume(',')) error(UNFINISHEDFUNC);
      list = add(list, or());
    }
    error(UNFINISHEDFUNC);
    return null;
  }

  /**
   * Parses a name.
   * @return string
   */
  private String name() {
    tk.reset();
    // '-' not allowed as first char in name
    if(letter(curr())) {
      while(!curr(0)) {
        tk.add(consume());
        final char c = curr();
        if(c == ':' && next() != ':') continue;
        if(!letterOrDigit(c) && c != '-') break;
      }
    }
    return tk.toString();
  }

  /**
   * Check if the given expression evaluates to a NodeSet.
   * @param e Expression to check
   * @return argument
   * @throws QueryException in case no NodeSet is returned
   */
  private Expr nodeCheck(final Expr e) throws QueryException {
    if(e.returnedValue() != NodeSet.class) error(NONODESET, e.toString());
    return e;
  }

  /**
   * Parse and convert entities.
   * @param tb token builder
   * @throws QueryException query exception
   */
  void entity(final TokenBuilder tb) throws QueryException {
    final String ent = ent(tb);
    if(ent != null) error(ent);
  }

  /**
   * Throws the specified error.
   * @param err error to be thrown
   * @param arg error arguments
   * @return expression
   * @throws QueryException parse exception
   */
  private Expr error(final String err, final Object... arg)
      throws QueryException {
    return error(null, err, arg);
  }

  /**
   * Throws the specified error.
   * @param comp code completion
   * @param err error to be thrown
   * @param arg error arguments
   * @return expression
   * @throws QueryException parse exception
   */
  private Expr error(final StringList comp, final String err,
      final Object... arg) throws QueryException {
    
    final QueryException qe = new QueryException(err, arg);
    qe.complete(this, comp);
    throw qe;
  }

  /**
   * Jumps some characters back.
   * @param off number of characters to jump back.
   */
  private void back(final int off) {
    qp -= off;
  }

  /**
   * Parses a FTSelection expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  private FTArrayExpr ftSelection() throws QueryException {
    final FTArrayExpr e = ftOr();
    return new FTSelect(e, ftPosFilter());
  }

  /**
   * Parses an FTOr expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  private FTArrayExpr ftOr() throws QueryException {
    final FTArrayExpr e = ftAnd();
    if(!consume(FTOR)) return e;

    consumeWS();
    if(consume(FTNOT)) error(FTNOTEXC);

    FTArrayExpr[] list = { e };
    do list = add(list, ftAnd()); while(consume(FTOR));
    return new FTOr(list);
  }

  /**
   * Parses an FTAnd expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  private FTArrayExpr ftAnd() throws QueryException {
    final FTArrayExpr e = ftMildNot();
    if(!consume(FTAND)) return e;

    FTArrayExpr[] list = { e };
    do list = add(list, ftMildNot()); while(consume(FTAND));
    return new FTAnd(list);
  }

  /**
   * Parses an FTMildNot expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  private FTArrayExpr ftMildNot() throws QueryException {
    final FTArrayExpr e = ftUnaryNot();
    if(!consume(NOT)) return e;

    FTArrayExpr[] list = { e };
    do {
      consumeWS();
      if(!consume(IN)) error(WRONGTEXT, IN, curr());
      if(consume(FTNOT)) error(FTNOTEXC);
      list = add(list, ftUnaryNot());
    } while(consume(NOT));
    return new FTMildNot(list);
  }

  /**
   * Parses an FTUnaryNot expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  private FTArrayExpr ftUnaryNot() throws QueryException {
    consumeWS();
    final boolean not = consume(FTNOT);
    consumeWS();
    final FTArrayExpr e = ftPrimaryWithOptions();
    return not ? new FTUnaryNot(new FTArrayExpr[] { e }) : e;
  }

  /**
   * Parses an FTPrimaryWithOptions expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  private FTArrayExpr ftPrimaryWithOptions() throws QueryException {
    final FTArrayExpr e = ftPrimary();
    e.fto = new FTOpt();
    ftMatchOption(e.fto);
    return e;
  }

  /**
   * Parses an FTPrimary expression.
   * @return FTPrimary expression
   * @throws QueryException parsing exception
   */
  private FTArrayExpr ftPrimary() throws QueryException {
    if(consume('(')) {
      final FTArrayExpr e = ftSelection();
      consumeWS();
      if(!consume(')')) error(") expected");
      return e;
    }

    if(curr('"') || curr('\'')) {
      final byte[] word = literal().str();
      consumeWS();

      // FTAnyAllOption
      FTMode mode = FTMode.ANY;
      if(consume(ALL)) {
        consumeWS();
        mode = consume(WORDS) ? FTMode.ALLWORDS : FTMode.ALL;
      } else if(consume(ANY)) {
        consumeWS();
        mode = consume(WORD) ? FTMode.ANYWORD : FTMode.ANY;
      } else if(consume(PHRASE)) {
        consumeWS();
        mode = FTMode.PHRASE;
      }
      consumeWS();
      return new FTWords(word, mode, ftTimes());
    }
    error("\', \" or ( expected");
    return null;
  }

  /**
   * Parses an FTMatchOption.
   * @param opt container for fulltext options
   * @throws QueryException parsing exception
   */
  private void ftMatchOption(final FTOpt opt) throws QueryException {
    // [CG] XPath/FTMatchOptions: language, thesaurus, stemming, stop words

    while(true) {
      consumeWS();
      if(consume(LOWERCASE)) {
        opt.lc = true;
        opt.cs = true;
      } else if(consume(UPPERCASE)) {
        opt.uc = true;
        opt.cs = true;
      } else if(consume(CASE)) {
        consumeWS();
        if(consume(SENSITIVE))        opt.cs = true;
        else if(consume(INSENSITIVE)) opt.cs = false;
        else error(FTCASE);
      } else if(consume(DIACRITICS)) {
        consumeWS();
        if(consume(SENSITIVE))        opt.dc = true;
        else if(consume(INSENSITIVE)) opt.dc = false;
        else error(FTDIA);
      } else if(consume(WITHOUT)) {
        consumeWS();
        if(consume(WILDCARDS)) opt.wc = false;
        else if(consume(FUZZY)) opt.fz = false;
        else if(consume(STEMMING)) opt.st = false;
        else if(consume(THESAURUS)) opt.ts = false;
        else if(consume(STOP)) {
          if(!consume(WORDS)) error(FTSTOP);
          opt.sw = null;
        }
      } else if(consume(WITH)) {
        consumeWS();
        if(consume(WILDCARDS)) opt.wc = true;
        else if(consume(FUZZY)) opt.fz = true;
        else if(consume(STEMMING)) opt.st = true;
        else if(consume(STOP)) {
          consumeWS();
          if(!consume(WORDS)) error(FTSTOP);
          opt.sw = new Set();
          boolean union = false;
          boolean except = false;
          while(true) {
            consumeWS();
            if(consume('(')) {
              do {
                consumeWS();
                final byte[] sl = literal().str();
                if(except) opt.sw.delete(sl);
                else if(!union || opt.sw.id(sl) == 0) opt.sw.add(sl);
                consumeWS();
              } while(consume(','));
              if(!consume(')')) error(FTSTOP);
            } else if(consume(AT)) {
              IO fl = new IO(string(literal().str()));
              if(!fl.exists()) error(FTSWFILE, fl);
              try {
                for(final byte[] sl : split(norm(fl.content()), ' ')) {
                  if(except) opt.sw.delete(sl);
                  else if(!union || opt.sw.id(sl) == 0) opt.sw.add(sl);
                }
              } catch(final IOException ex) {
                error(FTSWFILE, fl);
              }
            } else if(!union && !except) {
              error(FTSTOP);
            }
            consumeWS();
            union = consume(UNION);
            except = !union && consume(EXCEPT);
            if(!union && !except) break;
          }
        } else if(consume(THESAURUS)) {
          consumeWS();
          opt.ts = true;
          final boolean par = consume('(');
          consumeWS();
          if(consume(AT)) {
            consumeWS();
            ftThesaurusID();
          } else {
            if(!consume(DEFAULT)) error(FTTHES);
          }
          while(par && consume(',')) {
            consumeWS();
            ftThesaurusID();
          }
          if(par && !consume(')')) error(FTTHES);
          error(FTTHES);
        }
      } else if(consume(LANGUAGE)) {
        consumeWS();
        opt.ln = lc(literal().str());
        if(!eq(opt.ln, EN)) error(FTLANG, opt.ln);
      } else if(consume(DEFAULT)) {
        consumeWS();
        if(!consume(STOP)) error(FTSTOP);
        consumeWS();
        if(!consume(WORDS)) error(FTSTOP);
      } else {
        break;
      }
    }
  }

  /**
   * Parses an FTThesaurusID.
   * @throws QueryException xquery exception
   */
  private void ftThesaurusID() throws QueryException {
    literal();
    consumeWS();
    if(consume(RELATIONSHIP)) literal();
    consumeWS();
    if(ftRange() != null && !consume(LEVELS)) error(FTTHES);
  }

  /**
   * Parses an FTTimes expression.
   * @return FTPositionFilter
   * @throws QueryException parsing exception
   */
  private long[] ftTimes() throws QueryException {
    if(consume(OCCURS)) {
      consumeWS();
      final long[] occ = ftRange();
      if(!consume(TIMES)) error("times expected");
      consumeWS();
      return occ;
    }
    return new long[] { 1, Long.MAX_VALUE };
  }

  /**
   * Parses an FTRange expression.
   * @return numeric range
   * @throws QueryException parsing exception
   */
  private long[] ftRange() throws QueryException {
    final long[] occ = { 1, Long.MAX_VALUE };
    if(consume(EXACTLY)) {
      consumeWS();
      occ[0] = (long) num();
      occ[1] = occ[0];
      consumeWS();
    } else if(consume(AT)) {
      consumeWS();
      if(consume(LEAST)) {
        consumeWS();
        occ[0] = (long) num();
        consumeWS();
      } else if(consume(MOST)) {
        consumeWS();
        occ[0] = 0;
        occ[1] = (long) num();
        consumeWS();
      } else {
        error(FTRANGE);
      }
    } else if(consume(FROM)) {
      consumeWS();
      occ[0] = (long) num();
      consumeWS();
      if(consume(TO))  {
        consumeWS();
        occ[1] = (long) num();
      } else {
        error(FTRANGE);
      }
      consumeWS();
    } else {
      error(FTRANGE);
    }
    return occ;
  }

  /**
   * Parses an FTPositionFilter expression.
   * @return FTPositionFilter ft position filter information
   * @throws QueryException parsing exception
   */
  private FTPositionFilter ftPosFilter() throws QueryException {
    final FTPos pos = new FTPos();
    final FTPositionFilter ftpos = new FTPositionFilter(pos);

    while(true) {
      if(consume(ORDERED)) { // FTOrdered
        consumeWS();
        pos.ordered = true;
      } else if(consume(WINDOW)) { // FTWindow
        consumeWS();
        ftpos.window = number();
        pos.wunit = ftUnit();
      } else if(consume(DISTANCE)) { // FTDistance
        consumeWS();
        ftpos.dist = ftRange();
        pos.dunit = ftUnit();
      } else if(consume(AT)) { // FTContent
        consumeWS();
        if(consume(START)) pos.start = true;
        else if(consume(END)) pos.end = true;
        else error(FTSCOPE);
      } else if(consume(ENTIRE)) { // FTContent
        consumeWS();
        if(consume(CONTENT)) pos.content = true;
        else error(FTSCOPE);
      } else if(consume(SAME)) { // FTScope
        consumeWS();
        pos.same = true;
        pos.sdunit = ftBigUnit();
      } else if (consume(DIFFERENT)) { // FTScope
        consumeWS();
        pos.different = true;
        pos.sdunit = ftBigUnit();
      } else {
        break;
      }
    }
    return ftpos;
  }

  /**
   * Parses a Unit.
   * @throws QueryException parsing exception
   * @return fulltext unit
   */
  private FTUnit ftUnit() throws QueryException {
    consumeWS();
    if(consume(WORDS)) return FTUnit.WORDS;
    if(consume(SENTENCES)) return FTUnit.SENTENCES;
    if(consume(PARAGRAPHS)) return FTUnit.PARAGRAPHS;
    error("words, sentences or paragraphs expected.");
    return null;
  }

  /**
   * Parses a BigUnit.
   * @return fulltext unit
   * @throws QueryException parsing exception
   */
  private FTUnit ftBigUnit() throws QueryException {
    consumeWS();
    if(consume(SENTENCE)) return FTUnit.SENTENCES;
    if(consume(PARAGRAPH)) return FTUnit.PARAGRAPHS;
    error("sentece or paragraph expected.");
    return null;
  }

  /**
   * Adds an expression to the specified array.
   * @param a input array
   * @param e new expression
   * @return new array
   */
  private static Expr[] add(final Expr[] a, final Expr e) {
    return Array.add(a, e);
  }

  /**
   * Adds a fulltext expression to the specified array.
   * @param a input array
   * @param e new expression
   * @return new array
   */
  private static FTArrayExpr[] add(final FTArrayExpr[] a, final FTArrayExpr e) {
    return Array.add(a, e);
  }
}
