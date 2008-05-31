package org.basex.query.xpath;

import org.basex.core.Prop;

/**
 * This class assembles textual information of the XPath package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface XPText {
  /** Position info. */
  String POSINFO = "Stopped at line %, column %:" + Prop.NL;
  
  // XPath Parser =============================================================
  
  /** Node Test. */
  String NODE = "node";
  /** Node Test. */
  String TEXT = "text";
  /** Node Test. */
  String COMMENT = "comment";
  /** Node Test. */
  String PI = "processing-instruction";
  /** Element Test. */
  String ELEM = "*";
  /** Attribute name. */
  String TYPE = "type";
  /** Parser token. */
  String QUOTE = "quote";

  /** Parser token. */
  String DBLCOLON = "::";
  /** Parser token. */
  String OR = "or";
  /** Parser token. */
  String AND = "and";
  /** Parser token. */
  String FTCONTAINS = "ftcontains";
  /** Parser token. */
  String FTOR = "ftor";
  /** Parser token. */
  String FTAND = "ftand";
  /** Parser token. */
  String NOTIN = "not in";
  /** Parser token. */
  String FTNOT = "ftnot";
  /** Parser token. */
  String OCCURS = "occurs";
  /** Parser token. */
  String TIMES = "times";
  /** Parser token. */
  String CASEINSENS = "case insensitive";
  /** Parser token. */
  String CASESENS = "case sensitive";
  /** Parser token. */
  String LOWERCASE = "lowercase";
  /** Parser token. */
  String UPPERCASE = "uppercase";
  /** Parser token. */
  String WITHWILD = "with wildcards";
  /** Parser token. */
  String WITHOUTWILD = "without wildcards";
  /** Parser token. */
  String ORDERED = "ordered";
  /** Parser token. */
  String WINDOW = "window";
  /** Parser token. */
  String DISTANCE = "distance";
  /** Parser token. */
  String SAME = "same";
  /** Parser token. */
  String DIFFERENT = "different";
  /** Parser token. */
  String AT = "at";
  /** Parser token. */
  String START = "start";
  /** Parser token. */
  String END = "end";
  /** Parser token. */
  String ENTIRE = "entire";
  /** Parser token. */
  String CONTENT = "content";
  /** Parser token. */
  String ENTCONT = "entire content";
  /** Parser token. */
  String WORDS = "words";
  /** Parser token. */
  String SENTENCES = "sentences";
  /** Parser token. */
  String PARAGRAPHS = "paragraphs";
  /** Parser token. */
  String SENTENCE = "sentence";
  /** Parser token. */
  String PARAGRAPH = "paragraph";
  /** Parser token. */
  String EXACTLY = "exactly";
  /** Parser token. */
  String ATLEAST = "at least";
  /** Parser token. */
  String ATMOST = "at most";
  /** Parser token. */
  String FROM = "from";
  /** Parser token. */
  String TO = "to";
  /** Parser token. */
  String RANGE = "range";
  
  /** Parsing exception. */
  String QUERYDATA = "Please create database first.";
  /** Parsing exception. */
  String QUERYEND = "Unexpected end of query: \"%\".";
  /** Parsing exception. */
  String NOTEXTLEFT = "Expected \"%\", but no characters left.";
  /** Parsing exception. */
  String WRONGTEXT = "Expected \"%\", but \"%\" found.";
  /** Parsing exception. */
  String NOLOCSTEP = "Location step expected.";
  /** Parsing exception. */
  String NOATTNAME = "Attribute name expected.";
  /** Parsing exception. */
  String INVALIDLOCSTEP = "Invalid location step.";
  /** Parsing exception. */
  String PIBRACKETS = "Missing brackets for processing-instruction.";
  /** Parsing exception. */
  String PIQUOTE = "Missing quote in processing-instruction.";
  /** Parsing exception. */
  String UNKNOWNKIND = "Unknown node kind \"%\".";
  /** Evaluation Exception. */
  String NOTEST = "NodeTest missing.";
  /** Parsing exception. */
  String KINDCLOSE = "Unclosed node kind.";
  /** Parsing exception. */
  String QUOTECLOSE = "Unclosed quote.";
  /** Parsing exception. */
  String UNKNOWNAXIS = "Unknown axis \"%\".";
  /** Parsing exception. */
  String UNKNOWNCOMP = "Unknown comparison operator \"%\".";
  /** Parsing exception. */
  String NOPARENTHESIS = "Missing parenthesis.";
  /** Parsing exception. */
  String NOVARNAME = "Variable name expected.";
  /** Parsing exception. */
  String UNFINISHEDPRED = "Unfinished predicate.";
  /** Parsing exception. */
  String UNFINISHEDFUNC = "Unfinished function call.";
  /** Parsing exception. */
  String UNEXPECTEDEND = "Unexpected end of expression.";
  /** Parsing exception. */
  String UNKNOWNTOKEN = "Cannot recognize token: \"%\".";
  /** Parsing exception. */
  String EXPREND = "Unexpected end of expression.";
  /** Parsing exception. */
  String UNKNOWNVAR = "Unknown variable $%.";
  /** Parsing exception. */
  String UNKNOWNFUNC = "Unknown function %(...).";
  /** Parsing exception. */
  String FUNCEXCEPTION = "Cannot instantiate function %(...).";
  /** Parsing Exception. */
  String NONODESET = "NodeSet expected; found: %";
  /** Parsing exception. */
  String NOFT =
    "Please create a fulltext index or use XQuery for using 'ftcontains'.";
  /** Parsing exception. */
  String FTINCOMP =
    "Unfinished predicate, or 'ftcontains' option not supported yet.";

  /** Evaluation exception. */
  String NODATA = "No data available.";

  /** Evaluation Exception. */
  String NSSUPPORT = "Namespaces not supported yet.";
  /** Evaluation Exception. */
  String INVALIDPOS = "Invalid use of position().";
  /** Evaluation Exception. */
  String UNKNOWNREL = "Unknown Relational Operator.";
  /** Evaluation Exception. */
  String FUNCARGS = "Invalid function arguments; % expected";
  /** Evaluation Exception. */
  String INDEXEXC = "No index available or case insensitive search active. " +
      "Cannot use index() (TO BE FIXED)";
  /** Evaluation Exception. */
  String LASTEXC = "Invalid use of last().";
  /** Evaluation Exception. */
  String INVALIDPRE = "'%' is no valid pre value.";

  // XPath Optimizer ==========================================================

  /** Optimization info. */
  String OPTCALC = "pre-evaluating expression";
  /** Optimization info. */
  String OPTFUNC = "pre-evaluating %";
  /** Optimization info. */
  String OPTRELATIONAL = "pre-evaluating relational expression";
  /** Optimization info. */
  String OPTUNARY = "pre-evaluating unary expression";
  /** Optimization info. */
  String OPTLOC = "removing location path with no index results";
  /** Optimization info. */
  String OPTNAME = "removing unknown tag/attribute \"%\"";
  /** Optimization info. */
  String OPTAND1 = "removing always false AND expression";
  /** Optimization info. */
  String OPTAND2 = "removing always true operand in AND expression";
  /** Optimization info. */
  String OPTAND4 = "replacing AND expression by index access";
  /** Optimization info. */
  String OPTAND5 = "replacing AND by ALLOF expression";
  /** Optimization info. */
  String OPTALLOF = "replacing ALLOF expression by index access";
  /** Optimization info. */
  String OPTONEOF = "replacing ONEOF expression by index access";
  /** Optimization info. */
  String OPTRANGE = "replacing AND by RANGE expression";
  /** Optimization info. */
  String OPTOR1 = "removing always true OR expression";
  /** Optimization info. */
  String OPTOR2 = "removing always false operand in OR expression";
  /** Optimization info. */
  String OPTOR4 = "replacing OR expression by index access";
  /** Optimization info. */
  String OPTOR5 = "replacing OR by ONEOF expression";
  /** Optimization info. */
  String OPTEQ1 = "removing EQUALITY expression for empty nodeset";
  /** Optimization info. */
  String OPTEQ2 = "removing EQUALITY expression for constant comparison";
  /** Optimization info. */
  String OPTFT1 = "removing FTCONTAINS expression for empty nodeset";
  /** Optimization info. */
  String OPTFT2 = "removing FTCONTAINS expression for constant comparison";
  /** Optimization info. */
  String OPTSELF = "removing superfluous self axes";
  /** Optimization info. */
  String OPTPATHEXPR = "removing PATH for empty nodeset";
  /** Optimization info. */
  String OPTPRED = "removing always true predicate";
  /** Optimization info. */
  String OPTPOSPRED1 = "adding position predicate to evaluate only first node";
  /** Optimization info. */
  String OPTPOSPRED2 = "removing path with impossible position predicate";
  /** Optimization info. */
  String OPTEMPTY = "removing empty location path";
  /** Optimization info. */
  String OPTMERGE = "merging descendant-or-self and child steps";
  /** Optimization info. */
  String OPTINDEX = "choosing TEXTINDEX for textual match";
  /** Optimization info. */
  String OPTATTINDEX = "choosing ATTINDEX for attribute match";
  /** Optimization info. */
  String OPTFTINDEX = "choosing FTINDEX for fulltext search";
  /** Optimization info. */
  String OPTWORDINDEX = "choosing WORDINDEX for word-based search";
  /** Optimization info. */
  String OPTTEXT = "adding text() step";
  /** Optimization info. */
  String OPTPOS = "rewriting position predicate";
  
  /** Evaluation info. */
  String EVALSKIP = "rest of output skipped...";
}
