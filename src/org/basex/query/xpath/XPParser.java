package org.basex.query.xpath;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xpath.XPText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.query.FTOpt;
import org.basex.query.FTPos;
import org.basex.query.QueryParser;
import org.basex.query.QueryException;
import org.basex.query.FTOpt.FTMode;
import org.basex.query.FTPos.FTUnit;
import org.basex.query.xpath.expr.And;
import org.basex.query.xpath.expr.Clc;
import org.basex.query.xpath.expr.Equality;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTAnd;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTContains;
import org.basex.query.xpath.expr.FTMildNot;
import org.basex.query.xpath.expr.FTOr;
import org.basex.query.xpath.expr.FTSelect;
import org.basex.query.xpath.expr.FTPosFilter;
import org.basex.query.xpath.expr.FTWords;
import org.basex.query.xpath.expr.FTNot;
import org.basex.query.xpath.expr.Filter;
import org.basex.query.xpath.expr.Or;
import org.basex.query.xpath.expr.Path;
import org.basex.query.xpath.expr.Relational;
import org.basex.query.xpath.expr.Unary;
import org.basex.query.xpath.expr.Union;
import org.basex.query.xpath.func.Func;
import org.basex.query.xpath.func.XPathContext;
import org.basex.query.xpath.item.Calc;
import org.basex.query.xpath.item.Comp;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.item.Dbl;
import org.basex.query.xpath.item.Str;
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
import org.basex.util.Array;
import org.basex.util.Set;
import org.basex.util.TokenBuilder;

/**
 * XPath Processor, containing the XPath parser. The {@link #parse()} method
 * evaluates the query and returns the parsed expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public class XPParser extends QueryParser {
  /** FTGrammar.
  private static final int FTUNARYNOT = 0; */
  /** FTGrammar. */
  private static final int FTTIMES = 1;
  
  /**
   * Constructor.
   * @param q query
   */
  public XPParser(final String q) {
    init(q);
  }

  /**
   * Parses the query and returns a query context.
   * @return query context
   * @throws QueryException parsing exception
   */
  public final XPContext parse() throws QueryException {
    return parse(true);
  }

  /**
   * Parses the query and returns a query context.
   * @param end if true, input must be completely evaluated
   * @return query context
   * @throws QueryException parsing exception
   */
  public final XPContext parse(final boolean end) throws QueryException {
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
  final Expr or() throws QueryException {
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
  final Expr and() throws QueryException {
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
  final Expr equality() throws QueryException {
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
  final Expr relational() throws QueryException {
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
  final Comp cmp(final Comp[] comp) {
    for(final Comp c : comp) if(consume(c.name)) return c;
    return null;
  }

  /**
   * Parses an FTContainsExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  final Expr ftContains() throws QueryException {
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
  final Expr additive() throws QueryException {
    Expr e = multiplicative();
    consumeWS();

    while(true) {
      final Calc c = calc(new Calc[] { Calc.PLUS, Calc.MINUS });
      if(c == null) return e;
      e = new Clc(e, multiplicative(), c);
    }
  }

  /**
   * Parses a MultiplicativeExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  final Expr multiplicative() throws QueryException {
    Expr e = unary();
    consumeWS();

    while(true) {
      final Calc c = calc(new Calc[] { Calc.MULT, Calc.DIV, Calc.MOD });
      if(c == null) return e;
      e = new Clc(e, unary(), c);
    }
  }

  /**
   * Parses a calculator.
   * @param calc array with available calculators
   * @return found comparator or null
   */
  final Calc calc(final Calc[] calc) {
    for(final Calc c : calc) if(consume(c.name)) return c;
    return null;
  }

  /**
   * Parses a UnaryExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  final Expr unary() throws QueryException {
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
  final Expr union() throws QueryException {
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
  final Expr path() throws QueryException {
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
  final LocPath locPath() throws QueryException {
    return curr('/') ? absLocPath(new LocPathAbs()) :
      relLocPath(new LocPathRel());
  }

  /**
   * Parses an AbsoluteLocationPath.
   * @param path location path
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  LocPath absLocPath(final LocPath path) throws QueryException {
    boolean more = false;
    while(consume('/')) {
      final Step step = step();
      if(step == null) {
        if(more) error(NOLOCSTEP);
        break;
      }
      path.steps.add(step);
      more = true;
    }
    return path;
  }

  /**
   * Parses a RelativeLocationPath.
   * @param path location path
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  LocPath relLocPath(final LocPath path) throws QueryException {
    do {
      final Step step = step();
      if(step == null) error(NOLOCSTEP);
      path.steps.add(step);
    } while(consume('/'));
    return path;
  }

  /**
   * Parses a LocationStep.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  final Step step() throws QueryException {
    Axis axis = null;
    Test test = null;

    if(curr('/')) {
      if(next() == '/') error(NOLOCSTEP);
      axis = Axis.DESCORSELF;
      test = TestNode.NODE;
    } else if(consume('.')) {
      axis = consume('.') ? Axis.PARENT : Axis.SELF;
      test = TestNode.NODE;
    } else if(consume('@')) {
      axis = Axis.ATTR;
      final String t = name();
      test = t.length() != 0 ? new TestName(token(t), false) :
        consume('*') ? new TestName(false) : null;
      if(test == null) {
        checkStep(axis, new TestName(EMPTY, false));
        error(NOATTNAME);
      }
    } else if(consume('*')) {
      axis = Axis.CHILD;
      test = new TestName(true);
    } else if(letter(curr())) {
      axis = axis();
      test = test(axis);
    }
    checkStep(axis, test);
    if(axis == null) return null;

    final Preds preds = new Preds();
    consumeWS();
    while(curr('[')) {
      preds.add(pred());
      consumeWS();
    }
    return Axis.create(axis, test, preds);
  }
  
  /**
   * Performs optional step checks.
   * @param axis axis
   * @param test test
   */
  @SuppressWarnings("unused")
  void checkStep(final Axis axis, final Test test) { }

  /**
   * Parses a NodeTest.
   * @param axis current axis
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  final Test test(final Axis axis) throws QueryException {
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
  final Axis axis() throws QueryException {
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
  Expr pred() throws QueryException {
    consume('[');
    final Expr p = or();
    return p != null && consume(']') ? p : error(UNFINISHEDPRED);
  }

  /**
   * Parses a FilterExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  final Expr filter() throws QueryException {
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
  final Expr primary() throws QueryException {
    consumeWS();
    if(consume('(')) {
      final Expr e = or();
      return consume(')') ? e : error(NOPARENTHESIS);
    }
    if(curr('"') || curr('\'')) return literal();
    if(digit(curr())) return number();
    return function();
  }

  /**
   * Parses a Str and expects the first character to be a quote.
   * @return resulting expression
   * @throws QueryException query exception
   */
  final Str literal() throws QueryException {
    final char delim = consume();
    if(!quote(delim)) error(WRONGTEXT, QUOTE, delim);
    tk.reset();
    while(!curr(0) && !curr(delim)) entity(tk);
    if(!consume(delim)) error(QUOTECLOSE);
    return new Str(tk.finish());
  }

  /**
   * Parses a number.
   * @return resulting expression
   */
  final Dbl number() {
    return new Dbl(num());
  }

  /**
   * Parses a number.
   * @return resulting expression
   */
  final double num() {
    tk.reset();
    boolean dot = true;
    boolean exp = true;
    char c = curr();
    while(c != 0 && (digit(c) || dot && exp && c == '.' ||
        exp && (c == 'E' || c == 'e'))) {
      if(c == '.') {
        dot = false;
      } else if(c == 'e' || c == 'E') {
        exp = false;
      }
      tk.add(consume());
      c = curr();
    }
    return toDouble(tk.finish());
  }

  /**
   * Parses a FunctionCall.
   * @return resulting expression
   * @throws QueryException query exception
   */
  final Func function() throws QueryException {
    final String func = name();
    //if(!end && func.length() == 0) return null;
    
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
  final String name() {
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
   * Check if the given expression evaluates to a Nod.
   * @param e Expression to check
   * @return argument
   * @throws QueryException in case no Nod is returned
   */
  final Expr nodeCheck(final Expr e) throws QueryException {
    if(e.returnedValue() != Nod.class) error(NONODESET, e.toString());
    return e;
  }

  /**
   * Parse and convert entities.
   * @param tb token builder
   * @throws QueryException query exception
   */
  final void entity(final TokenBuilder tb) throws QueryException {
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
  Expr error(final String err, final Object... arg) throws QueryException {
    throw new QueryException(err, arg);
  }

  /**
   * Jumps some characters back.
   * @param off number of characters to jump back.
   */
  final void back(final int off) {
    qp -= off;
  }

  /**
   * Parses a FTSelection expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  final FTArrayExpr ftSelection() throws QueryException {
    final FTArrayExpr e = ftOr();
    return new FTSelect(e, ftPosFilter());
  }

  /**
   * Parses an FTOr expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  final FTArrayExpr ftOr() throws QueryException {
    final FTArrayExpr e = ftAnd();
    if(!consume(FTOR)) return e;

    consumeWS();
    //if(consume(FTNOT)) error(FTNOTEXC);

    FTArrayExpr[] list = { e };
    do list = add(list, ftAnd()); while(consume(FTOR));
    return new FTOr(list);
  }

  /**
   * Parses an FTAnd expression.
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  final FTArrayExpr ftAnd() throws QueryException {
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
  final FTArrayExpr ftMildNot() throws QueryException {
    boolean[] notnext = new boolean[2];
    final FTArrayExpr e = ftUnaryNot(notnext);
    if(!consume(NOT)) return e;
    
    FTArrayExpr[] list = { e };
    do {
      consumeWS();
      if(!consume(IN)) error(WRONGTEXT, IN, curr());
      consumeWS();
      if(consume(FTNOT)) error(FTMILDNOT); //FTNOTEXC);
      notnext[FTTIMES] = true;
      list = add(list, ftUnaryNot(notnext));
    } while(consume(NOT));
    return new FTMildNot(list);
  }

  /**
   * Parses an FTUnaryNot expression.
   * @param notnext boolean[] array with illegal steps
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  final FTArrayExpr ftUnaryNot(final boolean[] notnext) throws QueryException {
    consumeWS();
    final boolean not = consume(FTNOT);
    consumeWS();
    final FTArrayExpr e = ftPrimaryWithOptions(notnext);
    return not ? new FTNot(e) : e;
  }

  /**
   * Parses an FTPrimaryWithOptions expression.
   * @param notnext boolean[] array with illegal steps
   * @return FTArrayExpr
   * @throws QueryException parsing exception
   */
  final FTArrayExpr ftPrimaryWithOptions(final boolean[] notnext)
  throws QueryException {
    final FTArrayExpr e = ftPrimary(notnext);
    e.fto = new FTOpt();
    ftMatchOption(e.fto);
    return e;
  }

  /**
   * Parses an FTPrimary expression.
   * @param notnext boolean[] array with illegal steps
   * @return FTPrimary expression
   * @throws QueryException parsing exception
   */
  final FTArrayExpr ftPrimary(final boolean[] notnext) throws QueryException {
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
      return new FTWords(word, mode, ftTimes(notnext));
    }
    error("\', \" or ( expected");
    return null;
  }

  /**
   * Parses an FTMatchOption.
   * @param opt container for fulltext options
   * @throws QueryException parsing exception
   */
  final void ftMatchOption(final FTOpt opt) throws QueryException {
      while(true) {
      consumeWS();
      if(consume(LOWERCASE)) {
        opt.set(FTOpt.CS, true);
        opt.set(FTOpt.LC, true);
      } else if(consume(UPPERCASE)) {
        opt.set(FTOpt.CS, true);
        opt.set(FTOpt.UC, true);
      } else if(consume(CASE)) {
        consumeWS();
        if(consume(SENSITIVE))        opt.set(FTOpt.CS, true);
        else if(consume(INSENSITIVE)) opt.set(FTOpt.CS, false);
        else error(FTCASE);
      } else if(consume(DIACRITICS)) {
        consumeWS();
        if(consume(SENSITIVE))        opt.set(FTOpt.DC, true);
        else if(consume(INSENSITIVE)) opt.set(FTOpt.DC, false);
        else error(FTDIA);
      } else if(consume(WITHOUT)) {
        consumeWS();
        if(consume(WILDCARDS)) opt.set(FTOpt.WC, false);
        else if(consume(FUZZY)) opt.set(FTOpt.FZ, false);
        else if(consume(STEMMING)) opt.set(FTOpt.ST, false);
        else if(consume(THESAURUS)) opt.set(FTOpt.TS, false);
        else if(consume(STOP)) {
          if(!consume(WORDS)) error(FTSTOP);
          opt.sw = new Set();
        }
      } else if(consume(WITH)) {
        consumeWS();
        if(consume(WILDCARDS)) opt.set(FTOpt.WC, true);
        else if(consume(FUZZY)) opt.set(FTOpt.FZ, true);
        else if(consume(STEMMING)) opt.set(FTOpt.ST, true);
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
              final IO fl = IO.get(string(literal().str()));
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
          opt.set(FTOpt.TS, true);
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
  final void ftThesaurusID() throws QueryException {
    literal();
    consumeWS();
    if(consume(RELATIONSHIP)) literal();
    consumeWS();
    if(ftRange() != null && !consume(LEVELS)) error(FTTHES);
  }

  /**
   * Parses an FTTimes expression.
   * @param notnext boolean[] array with illegal steps
   * @return FTPositionFilter
   * @throws QueryException parsing exception
   */
  final long[] ftTimes(final boolean[] notnext) throws QueryException {
    if(consume(OCCURS)) {
      if (notnext[FTTIMES]) error(FTMILDNOT);
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
  final long[] ftRange() throws QueryException {
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
  final FTPosFilter ftPosFilter() throws QueryException {
    final FTPos pos = new FTPos();
    final FTPosFilter ftpos = new FTPosFilter(pos);

    while(true) {
      if(consume(ORDERED)) { // FTOrdered
        consumeWS();
        pos.ordered = true;
        ftpos.lp = true;
      } else if(consume(WINDOW)) { // FTWindow
        consumeWS();
        ftpos.window = number();
        pos.wunit = ftUnit();
        ftpos.lp = true;
      } else if(consume(DISTANCE)) { // FTDistance
        consumeWS();
        ftpos.dist = ftRange();
        pos.dunit = ftUnit();
        ftpos.lp = true;
      } else if(consume(AT)) { // FTContent
        consumeWS();
        if(consume(START)) pos.start = true;
        else if(consume(END)) pos.end = true;
        else error(FTSCOPE);
        ftpos.lp = true;
      } else if(consume(ENTIRE)) { // FTContent
        consumeWS();
        if(consume(CONTENT)) pos.content = true;
        else error(FTSCOPE);
        ftpos.lp = true;
      } else if(consume(SAME)) { // FTScope
        consumeWS();
        pos.same = true;
        pos.sdunit = ftBigUnit();
        ftpos.lp = true;
      } else if (consume(DIFFERENT)) { // FTScope
        consumeWS();
        pos.different = true;
        pos.sdunit = ftBigUnit();
        ftpos.lp = true;
      } else {
        break;
      }
      consumeWS();
    }
    return ftpos;
  }

  /**
   * Parses a Unit.
   * @throws QueryException parsing exception
   * @return fulltext unit
   */
  final FTUnit ftUnit() throws QueryException {
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
  final FTUnit ftBigUnit() throws QueryException {
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
  static Expr[] add(final Expr[] a, final Expr e) {
    return Array.add(a, e);
  }

  /**
   * Adds a fulltext expression to the specified array.
   * @param a input array
   * @param e new expression
   * @return new array
   */
  static FTArrayExpr[] add(final FTArrayExpr[] a, final FTArrayExpr e) {
    return Array.add(a, e);
  }
}
