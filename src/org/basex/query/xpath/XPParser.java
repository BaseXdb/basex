package org.basex.query.xpath;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xpath.XPText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryException;
import org.basex.query.QueryParser;
import org.basex.query.xpath.expr.And;
import org.basex.query.xpath.expr.Calculation;
import org.basex.query.xpath.expr.Equality;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTAnd;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTContains;
import org.basex.query.xpath.expr.FTFuzzy;
import org.basex.query.xpath.expr.FTMildNot;
import org.basex.query.xpath.expr.FTOption;
import org.basex.query.xpath.expr.FTOr;
import org.basex.query.xpath.expr.FTPosFilter;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.expr.FTPrimary;
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
  /**
   * XPath Query Constructor.
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
  public XPContext parse() throws QueryException {
    try {
      final Expr expr = parseOr();
      if(qp != ql) error(QUERYEND, rest());
      return new XPContext(expr, qu);
    } catch(final QueryException ex) {
      ex.pos(this);
      throw ex;
    }
  }

  /**
   * Parses an OrExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseOr() throws QueryException {
    final Expr e = parseAnd();
    consumeWS();
    if(!consume(OR)) return e;

    // initialize list for all or'd expressions
    final ExprList exprs = new ExprList();
    // add initial two operands
    exprs.add(e);
    exprs.add(parseAnd());
    // search for further operands
    while(consume(OR)) exprs.add(parseAnd());
    // return OrExpr
    return new Or(exprs.get());
  }

  /**
   * Parses an AndExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseAnd() throws QueryException {
    final Expr e = parseEquality();
    consumeWS();
    if(!consume(AND)) return e;

    // initialize list for all and'd expressions
    final ExprList exprs = new ExprList();
    // add initial two operands
    exprs.add(e);
    exprs.add(parseEquality());
    // search for further operands
    while(consume(AND)) exprs.add(parseEquality());
    // return ANDExpr
    return new And(exprs.get());
  }

  /**
   * Parses an EqualityExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseEquality() throws QueryException {
    Expr e = parseRelational();
    consumeWS();

    while(true) {
      final Comp expr = cmp(new Comp[] {
          Comp.WORD, Comp.EQ, Comp.NE, Comp.APPRWORD, Comp.APPR });
      if(expr == null) return e;
      e = new Equality(e, parseRelational(), expr);
    }
  }

  /**
   * Parses a RelationalExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseRelational() throws QueryException {
    Expr e = parseFTContains();
    consumeWS();

    while(true) {
      final Comp expr = cmp(new Comp[] { Comp.LE, Comp.LT, Comp.GE, Comp.GT });
      if(expr == null) return e;
      e = new Relational(e, parseFTContains(), expr);
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
   * Parses an FTWords.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseFTWords() throws QueryException {
    if(curr('"') || curr('\'')) return parseLiteral();
    
    consumeWS();
    /*
    if (curr('f')) return  parseFTOr();
    else if (curr('o')) return parseFTTimes();
      return error(WRONGTEXT, "quote", (char) curr());
    */
    return curr('f') ? parseFTOr() : error(WRONGTEXT, QUOTE, curr());
  }
 
  /**
   * Parses an AdditiveExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseAdditive() throws QueryException {
    Expr e = parseMultiplicative();
    consumeWS();

    while(true) {
      final Calc expr = calc(new Calc[] { Calc.PLUS, Calc.MINUS });
      if(expr == null) return e;
      e = new Calculation(e, parseMultiplicative(), expr);
    }
  }

  /**
   * Parses a MultiplicativeExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseMultiplicative() throws QueryException {
    Expr e = parseUnary();
    consumeWS();

    while(true) {
      final Calc expr = calc(new Calc[] { Calc.MULT, Calc.DIV, Calc.MOD });
      if(expr == null) return e;
      e = new Calculation(e, parseUnary(), expr);
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
  private Expr parseUnary() throws QueryException {
    Expr e = null;
    consumeWS();

    while(consume('-')) e = new Unary(parseUnary());
    return e != null ? e : parseUnion();
  }

  /**
   * Parses a UnionExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseUnion() throws QueryException {
    final Expr e = parsePath();
    consumeWS();
    if(!consume('|')) return e;
    
    // initialize list for all and'd expressions
    final ExprList exprs = new ExprList();
    // add initial two operands
    checkNodesReturned(e);
    exprs.add(e);
    Expr tmp = parsePath();
    checkNodesReturned(tmp);
    exprs.add(tmp);
    // search for further operands
    while(consume('|')) {
      tmp = parsePath();
      checkNodesReturned(tmp);
      exprs.add(tmp);
    }
    return new Union(exprs.get());
  }

  /**
   * Parses a PathExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parsePath() throws QueryException {
    consumeWS();

    // simple LocationPath cases
    final char c = curr();
    if(c == '@' || c == '*' || c == '.' || c == '/') return parseLocPath();

    // other possible LocationPath cases
    final int p = qp;
    final String name = name();
    consumeWS();

    // if name is following
    if(name.length() != 0 && (curr() != '(' || name.equals(NODE) ||
        name.equals(TEXT) || name.equals(COMMENT) || name.equals(PI))) {
      qp = p;
      return parseLocPath();
    }
    
    qp = p;

    final Expr e = parseFilter();
    consumeWS();
    if(!consume('/')) return e;
    
    checkNodesReturned(e);
    final LocPath path = new LocPathRel();
    if(consume('/')) {
      path.steps.add(Axis.create(Axis.DESCORSELF, TestNode.NODE));
    }
    return new Path(e, parseRelLocPath(path));
  }

  /**
   * Parses a LocationPath.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private LocPath parseLocPath() throws QueryException {
    return curr('/') ? parseAbsLocPath(new LocPathAbs()) :
      parseRelLocPath(new LocPathRel());
  }

  /**
   * Parses an AbsoluteLocationPath.
   * @param path location path
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private LocPath parseAbsLocPath(final LocPath path) throws QueryException {
    boolean more = false;
    while(consume('/')) {
      final char c = curr();
      if(c != 0 && c != ' ' && c != ']' && c != ')' && c != '|' && c != ',') {
        path.steps.add(parseStep());
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
  private LocPath parseRelLocPath(final LocPath path) throws QueryException {
    path.steps.add(parseStep());
    while(consume('/')) path.steps.add(parseStep());
    return path;
  }

  /**
   * Parses a LocationStep.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Step parseStep() throws QueryException {
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
      axis = parseAxis();
      nodetest = parseNodeTest(axis);
    } else {
      error(NOLOCSTEP);
    }

    final Preds preds = new Preds();
    consumeWS();
    while(curr('[')) {
      preds.add(parsePred());
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
  private Test parseNodeTest(final Axis axis) throws QueryException {
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
  private Axis parseAxis() throws QueryException {
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
  private Expr parsePred() throws QueryException {
    consume('[');
    final Expr p = parseOr();
    return consume(']') ? p : p instanceof FTContains ? error(FTINCOMP)
        : error(UNFINISHEDPRED);
  }

  /**
   * Parses a FilterExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseFilter() throws QueryException {
    Expr e = parsePrimary();
    consumeWS();
    while(curr('[')) {
      checkNodesReturned(e);
      final Preds preds = new Preds();
      preds.add(parsePred());
      e = new Filter(e, preds);
    }
    return e;
  }

  /**
   * Parses a PrimaryExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parsePrimary() throws QueryException {
    consumeWS();
    if(consume('$')) return parseVarReference();
    if(consume('(')) {
      final Expr e = parseOr();
      return consume(')') ? e : error(NOPARENTHESIS);
    }
    if(curr('"') || curr('\'')) return parseLiteral();
    if(digit(curr())) return parseNumber();
    return parseFunction();
  }

  /**
   * Parses a VariableReference.
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Item parseVarReference() throws QueryException {
    final String var = name();
    if(var.length() == 0) error(NOVARNAME);
    return XPathContext.get().evalVariable(token(var));
  }

  /**
   * Parses a Literal and expects the first character to be a quote.
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Literal parseLiteral() throws QueryException {
    final char delim = consume();
    if(!quote(delim)) error(WRONGTEXT, QUOTE, delim);
    tok.reset();
    while(!curr(0) && !curr(delim)) {
      entity(tok);
    }
    if(!consume(delim)) error(QUOTECLOSE);
    return new Literal(tok.finish());
  }

  /**
   * Parses a Number.
   * @return resulting expression
   */
  private Item parseNumber() {
    tok.reset();
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
      tok.add(consume());
      curr = curr();
    }
    return new Num(toDouble(tok.finish()));
  }

  /**
   * Parses a FunctionCall.
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Func parseFunction() throws QueryException {
    final String func = name();
    consumeWS();
    if(!consume('(')) error(UNEXPECTEDEND);

    // name and opening bracket found
    final ExprList exprs = new ExprList();
    boolean more = false;
    while(!curr(0)) {
      consumeWS();
      if(consume(')')) {
        return XPathContext.get().getFunction(token(func), exprs.get());
      }
      if(more && !consume(',')) error(UNFINISHEDFUNC);
      exprs.add(parseOr());
      more = true;
    }
    error(UNFINISHEDFUNC);
    return null;
  }

  /**
   * Parses a name.
   * @return string
   */
  private String name() {
    tok.reset();
    // '-' not allowed as first char in name
    if(letter(curr())) {
      while(!curr(0)) {
        tok.add(consume());
        final char c = curr();
        if(c == ':' && next() != ':') continue;
        if(!letterOrDigit(c) && c != '-') break;
      }
    }
    return tok.toString();
  }

  /**
   * Check if the given expression evaluates to a NodeSet.
   * @param e Expression to check
   * @throws QueryException in case no NodeSet is returned
   */
  private void checkNodesReturned(final Expr e) throws QueryException {
    if(e.returnedValue() != NodeSet.class) error(NONODESET, e.toString());
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
    throw new QueryException(err, arg);
  }

  /**
   * Jumps some characters back.
   * @param off number of characters to jump back.
   */
  private void back(final int off) {
    qp -= off;
  }

  /**
   * Parses an FTContainsExpr.
   * @return resulting expression
   * @throws QueryException parsing exception
   */
  private Expr parseFTContains() throws QueryException {
    final Expr e = parseAdditive();
    consumeWS();
    if(consume("ftfuzzy")) {
      consumeWS();
      Literal lit = parseLiteral();
      consumeWS();
      //boolean fi = consume("fi".getBytes());
      //consume("fti".getBytes());
      //consumeWS();
      Num error = (Num) parseNumber();
      int ne = (int) error.num();
      if (ne == 0) ne = 3;
      return new FTFuzzy(e, lit, ne);
      
    }
    if(!consume(FTCONTAINS)) return e;
    //if(!context.data().meta.ftxindex) error(NOFT);
    consumeWS();

    Expr second = parseFTSelection();
    FTContains ftc = new FTContains(e, second);
    return ftc;
    /*final FTOption option = new FTOption();

    boolean fto = false;
    if (second instanceof Literal) {
      // parse optional FTTimes
      if (curr('o')) {
        parseFTTimes(option);
        fto = true;
      }
    }*/
  }
    
  /**
   * Parses a FTSelection expression.
   * @return FTArrayExpr  
   * @throws QueryException parsing exception
   */
  private Expr parseFTSelection() throws QueryException {
    Expr e = parseFTOr();
    // optional
    FTPositionFilter ftps = parseFTPosFilter();
    if (ftps != null) {
      return new FTPosFilter(new Expr[]{e}, ftps);
    }
    return e;
  }
  
  /**
   * Parses an FTOr expression.
   * @return FTArrayExpr  
   * @throws QueryException parsing exception
   */
  private Expr parseFTOr() throws QueryException {
    Expr e = parseFTAnd();
    consumeWS();
    
    if (!consume(FTOR)) return e;
    ExprList el = new ExprList();
    el.add(e);
    consumeWS();
    if(consume(FTNOT)) error(FTNOTEXC);
    el.add(parseFTAnd());
    consumeWS();
    
    while (consume(FTOR)) {
      consumeWS();
      el.add(parseFTAnd());
    }
    
    return new FTOr(el.get());
  }

  /**
   * Parses an FTAnd expression.
   * @return FTArrayExpr  
   * @throws QueryException parsing exception
   */
  private Expr parseFTAnd() throws QueryException {
    Expr e = parseFTMildNot();
    consumeWS();
    
    if (!consume(FTAND)) return e;
    consumeWS();
 
    ExprList el = new ExprList();
    el.add(e);
    el.add(parseFTMildNot());
    consumeWS();
    
    while (consume(FTAND)) {
      consumeWS();
      el.add(parseFTMildNot());
    }
    
    return new FTAnd(el.get(), false);
  }
  
  /**
   * Parses an FTMildNot expression.
   * @return FTArrayExpr  
   * @throws QueryException parsing exception
   */
  private Expr parseFTMildNot() throws QueryException {
    Expr e = parseFTUnaryNot();
    consumeWS();
    
    if(!consume(NOT)) return e;
    consumeWS();
    if(!consume(IN)) error(WRONGTEXT, IN, curr());
    ExprList el = new ExprList();
    el.add(e);
    consumeWS();
    if(consume(FTNOT)) error(FTNOTEXC);
    el.add(parseFTUnaryNot());
    consumeWS();
    
    while(consume(NOT)) {
      consumeWS();
      if(!consume(IN)) error(WRONGTEXT, IN, curr());
      consumeWS();
      el.add(parseFTUnaryNot());
    }
    
    return new FTMildNot(el.get());
  }
  
  /**
   * Parses an FTUnaryNot expression.
   * @return FTArrayExpr  
   * @throws QueryException parsing exception
   */
  private Expr parseFTUnaryNot() throws QueryException {
    Expr e;
    // optional
    if (consume(FTNOT)) {
      consumeWS();
      
      e = new FTUnaryNot(new Expr[]{parseFTPrimaryWithOptions()}); 
      return e;
    }
    
    return parseFTPrimaryWithOptions();
  }
  
  /**
   * Parses an FTPrimaryWithOptions expression.
   * @return FTArrayExpr  
   * @throws QueryException parsing exception
   */
  private Expr parseFTPrimaryWithOptions() throws QueryException {
    Expr e = parseFTPrimary();
    FTOption fto = new FTOption();
    parseFTMatchOption(fto);
    if (e instanceof FTArrayExpr)
      ((FTArrayExpr) e).fto = fto;
    return e;
    
  }
  
  /**
   * Parses an FTMatchOption.
   * @param opt container for fulltext options
   * @throws QueryException parsing exception
   */
  private void parseFTMatchOption(final FTOption opt) throws QueryException {
    while(true) {
      consumeWS();
      if(consume(CASE)) {
        consumeWS();
        if(consume(SENSITIVE))        opt.ftCasesen = true;
        else if(consume(INSENSITIVE)) opt.ftCasesen = false;
        else error(FTCASE);
      } else if(consume(LOWERCASE)) {
        opt.ftlc = true;
      } else if(consume(UPPERCASE)) {
        opt.ftuc = true;
      } else if(consume(WITH)) {
        consumeWS();
        if(consume(WILDCARDS)) opt.ftWild = true;
      } else if(consume(WITHOUT)) {
        consumeWS();
        if(consume(WILDCARDS)) opt.ftWild = false;
      } else {
        break;
      }
    }
  }
  
  /**
   * Parses an FTPrimary expression.
   * @return Expr  FTPrimary expression
   * @throws QueryException parsing exception
   */
  private Expr parseFTPrimary() throws QueryException {
    Expr e = null;
    if(curr('"') || curr('\'')) {
      e = parseFTWords();
      consumeWS();
      // optional
      // parseFTAnyallOptions();
      // optional 
      FTPositionFilter ftps = parseFTTimes();
      if (ftps != null)
        return new FTPrimary(new Expr[]{e}, ftps);
      return new FTPrimary(new Expr[]{e});
      
    } else if (consume('(')) {
      consumeWS();
      e = parseFTSelection();
      consumeWS();
      if (!consume(')')) {
        error(") expected");
      }
    } else {
        error("\', \" or ( expected");
    }
    return e;
  }
  
  /**
   * Parses an FTTimes expression.
   * @return FTPositionFilter
   * @throws QueryException parsing exception
   */
  private FTPositionFilter parseFTTimes() throws QueryException {
    if (consume(OCCURS)) {
      consumeWS();
      FTPositionFilter ftps = new FTPositionFilter();
      parseFTRange(ftps);
      if(!consume(TIMES)) 
        error("times expected");
      ftps.ftTimes = FTPositionFilter.CARDSEL.FTTIMES;
      consumeWS();
      return ftps;
    }
    return null;
  }
  
  /**
   * Parses an FTRange expression.
   * @param ftpf FTPositionFilter saves unit information
   * @throws QueryException parsing exception
   */
  private void parseFTRange(final FTPositionFilter ftpf) throws QueryException {
    if (consume(EXACTLY)) {
      consumeWS();
      ftpf.to = (Num) parseNumber();
      ftpf.from = ftpf.to;
      ftpf.ftRange = FTPositionFilter.RANGE.EXACTLY;
      consumeWS();
    } else if(consume(AT)) {
      consumeWS();
      if(consume(MOST)) {
        consumeWS();
        ftpf.from = new Num(0);
        ftpf.to = (Num) parseNumber();
        ftpf.ftRange = FTPositionFilter.RANGE.ATMOST;
        consumeWS();
      } else if(consume(LEAST)) {
        consumeWS();
        ftpf.from = (Num) parseNumber();
        ftpf.ftRange = FTPositionFilter.RANGE.ATLEAST;
        consumeWS();
      } else {
        error(FTRANGE);
      }
    } else if(consume(FROM)) {
      consumeWS();
      ftpf.from = (Num) parseNumber();
      consumeWS();
      if(consume(TO))  {
        consumeWS();
        ftpf.to = (Num) parseNumber();
      } else {
        error(FTRANGE);
      }
      ftpf.ftRange = FTPositionFilter.RANGE.FROMTO;
      consumeWS();
    } else {
      error(FTRANGE);
    }
  }
  
  /**
   * Parses an FTPositionFilter expression.
   * @return FTPositionFilter ft position filter information
   * @throws QueryException parsing exception
   */
  private FTPositionFilter parseFTPosFilter() throws QueryException {
    FTPositionFilter ftpf = new FTPositionFilter();
    if (consume(ORDERED)) {
      consumeWS();
      // FTOrdered
      ftpf.ftPosFilt = FTPositionFilter.POSFILTER.ORDERED;
    } else if (consume(WINDOW)) {
      consumeWS();
      // FTWindow
      ftpf.ftPosFilt = FTPositionFilter.POSFILTER.WINDOW;
      ftpf.from = (Num) parseNumber();
      parseFTUnit(ftpf);
    } else if (consume(DISTANCE)) {
      consumeWS();
      ftpf.ftPosFilt = FTPositionFilter.POSFILTER.DISTANCE;
      parseFTRange(ftpf);
      parseFTUnit(ftpf);
    } else if (consume(SAME)) {
      consumeWS();
      //FTScope
      ftpf.ftPosFilt = FTPositionFilter.POSFILTER.SCOPE;
      ftpf.ftScope = FTPositionFilter.SCOPE.SAME;
      parseFTBigUnit(ftpf);        
    } else if (consume(DIFFERENT)) {
      consumeWS();
      //FTScope
      ftpf.ftPosFilt = FTPositionFilter.POSFILTER.SCOPE;
      ftpf.ftScope = FTPositionFilter.SCOPE.DIFFERENT;
      parseFTBigUnit(ftpf);
    } else if (consume(AT) || consume(ENTIRE)) {
      ftpf.ftPosFilt = FTPositionFilter.POSFILTER.CONTENT;
      consumeWS();
      if (consume(START)) {
        ftpf.ftContent = FTPositionFilter.CONTENT.ATSTART;  
      } else if (consume(END)) {
        ftpf.ftContent = FTPositionFilter.CONTENT.ATEND;  
      } else if (consume(CONTENT)) {
        ftpf.ftContent = FTPositionFilter.CONTENT.ENTIRECONTENT;
      } else {
        error(FTSCOPE);
      }
    } else {
      return null;
    }
    return ftpf;
  }

  /**
   * Parses a Unit.
   * @param ftpf FTPositionFilter saves unit information
   * @throws QueryException parsing exception
   */
  private void parseFTUnit(final FTPositionFilter ftpf) throws QueryException {
    consumeWS();
    if (consume(WORDS)) {
      ftpf.ftUnit = FTPositionFilter.UNIT.WORDS;
    } else if (consume(SENTENCES)) {
      ftpf.ftUnit = FTPositionFilter.UNIT.SENTENCES;
    } else if (consume(PARAGRAPHS)) {
      ftpf.ftUnit = FTPositionFilter.UNIT.PARAGRAPHS;
    } else {
      error("words, sentences or paragraphs expected.");
    }
  }
  
  
  /**
   * Parses a BigUnit.
   * @param ftpf FTPositionFilter saves big unit information
   * @throws QueryException parsing exception
   */
  private void parseFTBigUnit(final FTPositionFilter ftpf) 
      throws QueryException {
    consumeWS();
    if (consume(SENTENCE)) {
      ftpf.ftBigUnit = FTPositionFilter.BIGUNIT.SENTENCE;
    } else if (consume(PARAGRAPH)) {
      ftpf.ftBigUnit = FTPositionFilter.BIGUNIT.PARAGRAPH;
    } else {
      error("sentece or paragraph expected.");
    }
  }
}
