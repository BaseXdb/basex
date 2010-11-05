package org.basex.query.util;

import static org.basex.query.util.Err.Type.*;
import org.basex.core.Text;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.util.InputInfo;

/**
 * This class assembles common error messages.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum Err {

  /** BASX0000: Evaluation exception. */
  NOTIMPL(BASX, 0, "Not implemented yet: %."),

  /** BASX0001: Evaluation exception. */
  NOIDX(BASX, 1, "Index not available: '%'."),
  /** BASX0002: Evaluation exception. */
  WHICHIDX(BASX, 2, "Unknown index: '%'."),
  /** BASX0003: Evaluation exception. */
  NODB(BASX, 3, "Database '%' not found."),
  /** BASX0004: Evaluation exception. */
  NOPRE(BASX, 4, "Pre value '%' out of range."),
  /** BASX0005: Command unknown. */
  CMDNO(BASX, 5, Text.CMDNO),
  /** BASX0006: No permissions. */
  PERMNO(BASX, 6, Text.PERMNO),
  /** BASX0007: Server timeout. */
  SERVERTIMEOUT(BASX, 7, Text.SERVERTIMEOUT),
  /** BASX0008: No nodes as result. */
  QUERYNODES(BASX, 8, Text.QUERYNODESERR),
  /** BASX0008: No nodes as result. */
  EXPSINGLE(BASX, 9, "Database contains more than one document."),
  /** BASX0009: No nodes as result. */
  SENTLISTPARSE(BASX, 10, "% could not be parsed: %."),
  /** BASX0099: Undefined error. */
  UNDEF(BASX, 99, "%."),

  /** FOAR0001: Evaluation exception. */
  DIVZERO(FOAR, 1, "'%' was divided by zero."),
  /** FOAR0002: Evaluation exception. */
  DIVFLOW(FOAR, 2, "Invalid division result: % / %."),
  /** FOAR0002: Parsing exception. */
  BOUNDS(FOAR, 2, "Integer value % out of bounds."),
  /** FOAR0002: Evaluation exception. */
  RANGE(FOAR, 2, "Value out of range: %."),

  /** FOCA0002: Evaluation exception. */
  INVALUE(FOCA, 2, "Invalid value for %: %."),
  /** FOCA0003: Evaluation exception. */
  INTRANGE(FOCA, 3, "Integer value out of range: %."),
  /** FOCA0005: Evaluation exception. */
  DATECALC(FOCA, 5, "Invalid % calculation: %."),

  /** FOCH0001: Evaluation exception. */
  INVCODE(FOCH, 1, "Invalid codepoint '%'."),
  /** FOCH0002: Evaluation exception. */
  IMPLCOL(FOCH, 2, "Unknown collation %."),
  /** FOCH0003: Evaluation exception. */
  NORMUNI(FOCH, 3, "Unsupported normalization form (%)."),

  /** FODC0001: Evaluation exception. */
  IDDOC(FODC, 1, "Root must be a document node."),
  /** FODC0002: Evaluation exception. */
  UNDOC(FODC, 2, "Document node could not be created (%)."),
  /** FODC0002: Evaluation exception. */
  COLLINV(FODC, 2, "Invalid collection \"%\"."),
  /** FODC0002: Evaluation exception. */
  NODEFCOLL(FODC, 2, "No default collection available."),
  /** FODC0002: Evaluation exception. */
  NODOC(FODC, 2, "%"),
  /** FODC0002: Evaluation exception. */
  DOCERR(FODC, 2, "\"%\" could not be opened."),
  /** FODC0004: Evaluation exception. */
  NOCOLL(FODC, 4, "%"),
  /** FODC0005: Evaluation exception. */
  INVDOC(FODC, 5, "Invalid document \"%\"."),
  /** FODC0006: Evaluation exception. */
  DOCWF(FODC, 6, "SAX: %."),
  /** FODC0007: Evaluation exception. */
  DOCBASE(FODC, 7, "Base-uri % is invalid."),

  /** FODF1280: Evaluation exception. */
  FORMNUM(FODF, 1280, "Unknown decimal format: %."),
  /** FODF1310: Evaluation exception. */
  PICNUM(FODF, 1310, "Invalid picture string: \"%\"."),

  /** FODT0002: Evaluation exception. */
  DATEZERO(FODT, 2, "Invalid % calculation: infinity/zero."),
  /** FODT0003: Evaluation exception. */
  INVALZONE(FODT, 3, "Invalid timezone: %."),

  /** FOER0000: Evaluation exception. */
  FUNERR1(FOER, 0, "Halted on error()."),

  /** FODF1340: Evaluation exception. */
  PICDATE(FODF, 1340, "Invalid picture string: \"%\"."),
  /** FODF1350: Evaluation exception. */
  PICCOMP(FODF, 1350, "Invalid component in string: \"%\"."),

  /** FOFL0001: Evaluation exception. */
  FILEREAD(FOFL, 1, "File '%' cannot be read."),
  /** FOFL0002: Evaluation exception. */
  FILEWRITE(FOFL, 2, "File '%' cannot be written."),
  /** FOFL0003: Evaluation exception. */
  FILELIST(FOFL, 3, "Files of '%' cannot be returned."),
  /** FOFL0004: Evaluation exception. */
  FILEPATTERN(FOFL, 4, "Invalid file name pattern '%'."),
  /** FOFL0005: Evaluation exception. */
  FILEDEL(FOFL, 5, "'%' cannot be deleted. Permission denied."),
  /** FOFL0006: Evaluation exception. */
  PATHNOTEXISTS(FOFL, 6, "File '%' does not exist."),
  /** FOFL0007: Evaluation exception. */
  FILEDELDIR(FOFL, 7, "Directory '%' is not empty."),
  /** FOFL0008: Evaluation exception. */
  FILEEXISTS(FOFL, 8, "File '%' already exists."),
  /** FOFL0009: Evaluation exception. */
  DIRMOVE(FOFL, 9, "A directory cannot be moved."),
  /** FOFL0010: Evaluation exception. */
  FILEMOVE(FOFL, 10, "'%' cannot be moved. '%' is write-protected."),
  /** FOFL0011: Evaluation exception. */
  MKDIR(FOFL, 11, "'%' cannot be created. '%' is write-protected."),
  /** FOFL0012: Evaluation exception. */
  CANNOTMKDIR(FOFL, 12, "Directory '%' cannot be created."),
  /** FOFL0013: Evaluation exception. */
  CANNOTDEL(FOFL, 13, "'%' cannot be deleted."),
  /** FOFL0014: Evaluation exception. */
  CANNOTMOVE(FOFL, 14, "File '%' cannot be moved."),
  /** FOFL0015: Evaluation exception. */
  DIRINV(FOFL, 15, "The path '%' is invalid in the filesystem."),
  /** FOFL0016: Evaluation exception. */
  COPYFAILED(FOFL, 16, "Copying from '%' to '%' failed: %."),
  /** FOFL0017: Evaluation exception. */
  ENCNOTEXISTS(FOFL, 17, "The encoding '%' is not supported."),

  /** FONS0004: Evaluation exception. */
  NSDECL(FONS, 4, "Namespace prefix not declared: \"%\"."),

  /** FORG0001: Evaluation exception. */
  INVALIDZONE(FORG, 1, "Invalid timezone: %."),
  /** FORG0001: Evaluation exception. */
  FUNCAST(FORG, 1, "Invalid % cast: %."),
  /** FORG0001: Evaluation exception. */
  DATERANGE(FORG, 1, "%(\"%\") out of range."),
  /** FORG0001: Evaluation exception. */
  DATEFORMAT(FORG, 1, "Wrong % format: \"%\" (try: \"%\")."),
  /** FORG0001: Evaluation exception. */
  QNMINV(FORG, 1, "Invalid QName: \"%\"."),
  /** FORG0002: Evaluation exception. */
  URIINV(FORG, 2, "Invalid URI: %."),

  /** FORG0003: Evaluation exception. */
  EXPECTZ0(FORG, 3, "Zero or one value expected."),
  /** FORG0004: Evaluation exception. */
  EXPECTOM(FORG, 4, "One or more values expected."),
  /** FORG0005: Evaluation exception. */
  EXPECTO(FORG, 5, "Exactly one value expected."),
  /** FORG0006: Evaluation exception. */
  FUNCMP(FORG, 6, "%: % expected, % found."),
  /** FORG0006: Evaluation exception. */
  CONDTYPE(FORG, 6, "% not allowed as condition type."),
  /** FORG0006: Evaluation exception. */
  SUMTYPE(FORG, 6, "%: % not allowed as input type."),
  /** FORG0006: Evaluation exception. */
  FUNNUM(FORG, 6, "%: number expected, % found."),
  /** FORG0006: Evaluation exception. */
  FUNDUR(FORG, 6, "%: duration expected, % found."),
  /** FORG0006: Evaluation exception. */
  TYPECMP(FORG, 6, "% is not comparable."),
  /** FORG0006: Evaluation exception. */
  FUNJAVA(FORG, 6, "Invalid arguments for %."),
  /** FORG0008: Function exception. */
  FUNZONE(FORG, 8, "% and % have different timezones."),

  /** FORX0001: Evaluation exception. */
  REGMOD(FORX, 1, "Invalid regular modifier: '%'."),
  /** FORX0002: Evaluation exception. */
  REGINV(FORX, 2, "Invalid regular expression: '%'."),
  /** FORX0003: Evaluation exception. */
  REGROUP(FORX, 3, "Pattern matches empty string."),
  /** FORX0004: Evaluation exception. */
  FUNREGREP(FORX, 4, "Invalid replacement expression."),
  /** FORX0004: Evaluation exception. */
  REGERR(FORX, 4, "Regular expression: '%'."),

  /** FOUP0001: Evaluation exception. */
  UPFOTYPE(FOUP, 1, "Document or element expected, % found."),
  /** FOUP0001: Evaluation exception. */
  UPFOEMPT(FOUP, 1, "Node expected: %."),
  /** FOUP0002: Evaluation exception. */
  UPFOURI(FOUP, 2, "No valid URI: '%'."),
  /** FOUP0002: Evaluation exception. */
  UPPUTERR(FOUP, 2, "'%' could not be written."),

  /** FTDY0016: Evaluation exception. */
  FTWEIGHT(FTDY, 16, "Invalid weight value: %."),
  /** FTDY0017: Evaluation exception. */
  FTMILD(FTDY, 17, "Invalid mild not selection."),
  /** FTDY0020: Evaluation exception. */
  FTREG(FTDY, 20, "Invalid regular expression: '%'."),

  /** FTST0000: Parsing exception. */
  FTFZWC(FTST, 0, "Only wildcards or Fuzzy search allowed."),
  /** FTST0000: Parsing exception. */
  THESRNG(FTST, 0, "Only integers allowed for thesaurus level."),
  /** FTST0007: Parsing exception. */
  FTIGNORE(FTST, 7, "Ignore option not supported."),
  /** FTST0008: Parsing exception. */
  NOSTOPFILE(FTST, 8, "Stop word file not found: \"%\"."),
  /** FTST0009: Parsing exception. */
  FTLAN(FTST, 9, "Language '%' not supported."),
  /** FTST0018: Parsing exception. */
  NOTHES(FTST, 18, "Thesaurus not found: \"%\"."),
  /** FTST0019: Parsing exception. */
  FTDUP(FTST, 19, "Match option '%' was defined twice."),

  /** XPDY0002: Evaluation Exception. */
  XPNOCTX(XPDY, 2, "No context item set for '%'."),
  /** XPDY0050: Evaluation exception. */
  CTXNODE(XPDY, 50, "Root of the context item must be a document node."),
  /** XPDY0050: Evaluation exception. */
  NOTREAT(XPDY, 50, "%: % expected, % found."),
  /** XPDY0050: Evaluation exception. */
  NOTREATS(XPDY, 50, "%: % expected, sequence found."),

  /** XPST0003: Parsing exception. */
  QUERYEMPTY(XPST, 3, "Empty query."),
  /** XPST0003: Parsing exception. */
  QUERYINV(XPST, 3, "Query contains an illegal character (#%)."),
  /** XPST0003: Parsing exception. */
  NOQUOTE(XPST, 3, "Expecting quote%."),
  /** XPST0003: Parsing exception. */
  NOVALIDATE(XPST, 3, "Invalid validation expression."),
  /** XPST0003: Parsing exception. */
  NUMBERWS(XPST, 3, "Expecting separator after number."),
  /** XPST0003: Parsing exception. */
  NUMBERINC(XPST, 3, "Incomplete double value."),
  /** XPST0003: Parsing exception. */
  QUERYEND(XPST, 3, "Unexpected end of query: '%'."),
  /** XPST0003: Parsing exception. */
  CMPEXPR(XPST, 3, "Comparison is incomplete."),
  /** XPST0003: Parsing exception. */
  NOTAGNAME(XPST, 3, "Expecting tag name."),
  /** XPST0003: Parsing exception. */
  NOATTNAME(XPST, 3, "Expecting attribute name."),
  /** XPST0003: Parsing exception. */
  NOLOCSTEP(XPST, 3, "Incomplete location step."),
  /** XPST0003: Parsing exception. */
  NOEXPR(XPST, 3, "Expecting expression."),
  /** XPST0003: Parsing exception. */
  WRONGCHAR(XPST, 3, "Expecting \"%\"%."),
  /** XPST0003: Parsing exception. */
  WRONGEND(XPST, 3, "Expecting \"%\"."),
  /** XPST0003: Parsing exception. */
  INVENTITY(XPST, 3, "Invalid entity \"%\"."),
  /** XPST0003: Parsing exception. */
  INCOMPLETE(XPST, 3, "Incomplete expression."),
  /** XPST0003: Evaluation exception. */
  EVALUNARY(XPST, 3, "Unary operator expects a numeric value."),
  /** XPST0003: Parsing exception. */
  PATHMISS(XPST, 3, "Expecting location path."),
  /** XPST0003: Parsing exception. */
  DECLINCOMPLETE(XPST, 3, "Incomplete declaration; expecting " +
      "'function', 'variable', ..."),
  /** XPST0003: Parsing exception. */
  FUNCNAME(XPST, 3, "Expecting function name."),
  /** XPST0003: Parsing exception. */
  PREDMISSING(XPST, 3, "Expecting expression before predicate."),
  /** XPST0003: Parsing exception. */
  NOVARNAME(XPST, 3, "Expecting variable name."),
  /** XPST0003: Parsing exception. */
  TAGWRONG(XPST, 3, "Start and end tag are different (%/%)."),
  /** XPST0003: Parsing exception. */
  PIWRONG(XPST, 3, "Expecting name of processing-instruction."),
  /** XPST0003: Parsing exception. */
  NOENCLEXPR(XPST, 3, "Expecting valid expression after \"{\"."),
  /** XPST0003: Parsing exception. */
  NODOCCONS(XPST, 3, "Expecting document construction."),
  /** XPST0003: Parsing exception. */
  NOTXTCONS(XPST, 3, "Expecting text construction."),
  /** XPST0003: Parsing exception. */
  NOCOMCONS(XPST, 3, "Expecting comment construction."),
  /** XPST0003: Parsing exception. */
  NOFUNBODY(XPST, 3, "Expecting function body."),
  /** XPST0003: Parsing exception. */
  FUNCMISS(XPST, 3, "Expecting closing bracket for \"%(...\"."),
  /** XPST0003: Parsing exception. */
  TYPEINVALID(XPST, 3, "Expecting type declaration."),
  /** XPST0003: Parsing exception. */
  NOTYPESWITCH(XPST, 3, "Incomplete typeswitch expression."),
  /** XPST0003: Parsing exception. */
  NOSWITCH(XPST, 3, "Incomplete switch expression."),
  /** XPST0003: Parsing exception. */
  TYPEPAR(XPST, 3, "Expecting '(' after 'switch' or 'typeswitch'."),
  /** XPST0003: Parsing exception. */
  PRAGMAINV(XPST, 3, "Invalid pragma expression."),
  /** XPST0003: Parsing exception. */
  TESTINCOMPLETE(XPST, 3, "Incomplete node test."),
  /** XPST0003: Parsing exception. */
  CALCEXPR(XPST, 3, "Calculation is incomplete."),
  /** XPST0003: Parsing exception. */
  NORETURN(XPST, 3, "Expecting return value."),
  /** XPST0003: Parsing exception. */
  NOWHERE(XPST, 3, "Expecting valid expression after 'where'."),
  /** XPST0003: Parsing exception. */
  ORDERBY(XPST, 3, "Expecting valid expression after 'order by'."),
  /** XPST0003: Parsing exception. */
  GRPBY(XPST, 3, "Expecting valid expression after 'group by'."),
  /** XPST0003: Parsing exception. */
  FLWORWHERE(XPST, 3, "Expecting 'where', 'order' or 'return' expression."),
  /** XPST0003: Parsing exception. */
  FLWORORD(XPST, 3, "Expecting 'order' or 'return' expression."),
  /** XPST0003: Parsing exception. */
  FLWORRET(XPST, 3, "Expecting 'return' expression."),
  /** XPST0003: Parsing exception. */
  NOSOME(XPST, 3, "Incomplete quantifier expression."),
  /** XPST0003: Parsing exception. */
  IFPAR(XPST, 3, "Expecting '(' after 'if' expression."),
  /** XPST0003: Parsing exception. */
  NOIF(XPST, 3, "Incomplete 'if' expression."),
  /** XPST0003: Parsing exception. */
  NOFOR(XPST, 3, "Incomplete 'for' expression."),
  /** XPST0003: Parsing exception. */
  NOLET(XPST, 3, "Incomplete 'let' expression."),
  /** XPST0003: Parsing exception. */
  NOCLOSING(XPST, 3, "Expecting closing tag </%>."),
  /** XPST0003: Parsing exception. */
  COMCLOSE(XPST, 3, "Unclosed XQuery comment (: ..."),
  /** XPST0003: Parsing exception. */
  EXPREMPTY(XPST, 3, "Unknown function or expression."),
  /** XPST0003: Parsing exception. */
  NOTYPE(XPST, 3, "Unknown type %."),
  /** XPST0003: Parsing exception. */
  PIXML(XPST, 3, "Illegal PI name: %."),
  /** XPST0003: Parsing exception. */
  EMPTYSEQOCC(XPST, 3, "No occurrence indicator defined for %."),
  /** XPST0003: Parsing exception. */
  TESTINVALID(XPST, 3, "Invalid % test: %."),
  /** XPST0003: Parsing exception. */
  QNAMEINV(XPST, 3, "Expecting QName."),
  /** XPST0003: Parsing exception. */
  PROLOGORDER(XPST, 3, "Default declaration must be declared first."),
  /** XPST0003: Parsing exception. */
  FTRANGE(XPST, 3, "Expecting full-text range."),
  /** XPST0003: Parsing exception. */
  FTSTOP(XPST, 3, "Stop words expected."),
  /** XPST0003: Parsing exception. */
  FTMATCH(XPST, 3, "Unknown match option '%...'."),
  /** XQDY0041: Evaluation exception. */
  INVALPI(XPST, 3, "Invalid PI name: \"%\"."),

  /** XPST0005: Parsing exception. */
  COMPSELF(XPST, 5, "Warning: '%' will not yield any results."),

  /** XPST0008: Parsing exception. */
  VARUNDEF(XPST, 8, "Undefined variable %."),
  /** XPST0008: Parsing exception. */
  TYPEUNDEF(XPST, 8, "Undefined type %."),
  /** XPST0017: Parsing Exception. */
  XPARGS(XPST, 17, "Wrong arguments: % expected."),
  /** XPST0017: Parsing exception. */
  FUNSIMILAR(XPST, 17, "Unknown function \"%\" (similar: %())."),
  /** XPST0017: Parsing Exception. */
  FUNCTYPE(XPST, 17, "Wrong arguments: %(value) expected."),
  /** XPST0003: Parsing exception. */
  FEATURE11(XPST, 17, "Feature not available in XQuery 1.0."),
  /** XPST0017: Parsing exception. */
  FUNCUNKNOWN(XPST, 17, "Unknown function \"%(...)\"."),
  /** XPST0017: Parsing exception. */
  FUNCJAVA(XPST, 17, "Unknown Java function \"%(...)\"."),
  /** XPST0051: Parsing exception. */
  TYPEUNKNOWN(XPST, 51, "Unknown type %."),
  /** XPST0080: Parsing exception. */
  CASTUNKNOWN(XPST, 80, "Invalid cast type %."),
  /** XPST0081: Parsing exception. */
  PREUNKNOWN(XPST, 81, "Unknown prefix: \"%\"."),
  /** XPST0081: Parsing exception. */
  NSMISS(XPST, 81, "Namespace missing."),

  /** XPTY0004: Typing exception. */
  XPSEQ(XPTY, 4, "No sequence % allowed."),
  /** XPTY0004: Typing exception. */
  XPINVCAST(XPTY, 4, "Invalid cast from % to %: %."),
  /** XPTY0004: Typing exception. */
  XPCAST(XPTY, 4, "Invalid %(%) cast."),
  /** XPTY0004: Typing Exception. */
  XPTYPE(XPTY, 4, "%: % expected, % found."),
  /** XPTY0004: Typing exception. */
  XPEMPTY(XPTY, 4, "%: no empty sequence allowed."),
  /** XPTY0004: Typing Exception. */
  XPEMPTYPE(XPTY, 4, "%: % expected, empty sequence found."),
  /** XPTY0004: Typing exception. */
  XPDUR(XPTY, 4, "%: duration expected, % found."),
  /** XPTY0004: Typing Exception. */
  XPTYPECMP(XPTY, 4, "% and % cannot be compared."),
  /** XPTY0004: Typing exception. */
  XPTYPENUM(XPTY, 4, "%: number expected, % found."),
  /** XPTY0004: Typing exception. */
  XPINVNAME(XPTY, 4, "Invalid name: %."),
  /** XPTY0004: Typing exception. */
  XPNAME(XPTY, 4, "Expecting name."),
  /** XPTY0004: Typing exception. */
  XPATT(XPTY, 4, "Cannot add attributes to a document node."),
  /** XPTY0004: Typing exception. */
  CPIWRONG(XPTY, 4, "% not allowed as PI name: \"%\"."),
  /** XPTY0004: Typing exception. */
  INVQNAME(XPTY, 4, "Invalid QName: \"%\"."),
  /** XPTY0004: Typing exception. */
  XPNODES(XPTY, 19, "%: nodes expected, % found."),

  /** XPTY0018: Typing exception. */
  EVALNODESVALS(XPTY, 18, "Result yields both nodes and atomic values."),
  /** XPTY0019: Typing exception. */
  EVALNODES(XPTY, 19, "Nodes needed for expression '%'."),
  /** XPTY0019: Typing exception. */
  NODESPATH(XPTY, 19, "Context node required for %; % found."),

  /** XPDY0002: Parsing exception. */
  VAREMPTY(XQDY, 2, "No value defined for \"%\"."),
  /** XQDY0026: Evaluation exception. */
  CPICONT(XQDY, 26, "Invalid PI content: \"%\"."),
  /** XQDY0041: Evaluation exception. */
  CPIINVAL(XQDY, 41, "Invalid PI name: \"%\"."),
  /** XQDY0044: Evaluation exception. */
  CAINS(XQDY, 44, "Invalid attribute namespace: \"%\" (URI \"%\")."),
  /** XQDY0064: Evaluation exception. */
  CPIXML(XQDY, 64, "Illegal PI name: \"%\"."),
  /** XQDY0072: Evaluation exception. */
  COMINVALID(XQDY, 72, "Invalid comment."),
  /** XQDY0074: Evaluation exception. */
  INVNAME(XQDY, 74, "Invalid name: \"%\"."),
  /** XPDY0095: resulting value for any grouping variable >> 1 item. */
  XGRP(XQDY, 95, "No sequence allowed as grouping variable."),
  /** XQDY0096: Invalid namespace in constructed element. */
  CEINS(XQDY, 96, "Invalid element namespace: \"%\" (URI \"%\")."),

  /** XQST0009: Parsing exception. */
  IMPLSCHEMA(XQST, 9, "Schema import not supported."),
  /** XQST0022: Parsing exception. */
  NSCONS(XQST, 22, "Constant namespace value expected."),
  /** XQST0031: Parsing exception. */
  XQUERYVER(XQST, 31, "XQuery version \"%\" not supported."),
  /** XQST0032: Parsing exception. */
  DUPLBASE(XQST, 32, "Duplicate 'base-uri' declaration."),
  /** XQST0033: Parsing exception. */
  DUPLNSDECL(XQST, 33, "Duplicate declaration of namespace %."),
  /** XQST0034: Parsing exception. */
  FUNCDEFINED(XQST, 34, "Duplicate declaration of function \"%\"."),
  /** XQST0038: Parsing exception. */
  DUPLCOLL(XQST, 38, "Duplicate 'collation' declaration."),
  /** XQST0076: Parsing exception. */
  COLLWHICH(XQST, 38, "Unknown collation \"%\"."),
  /** XQST0039: Parsing exception. */
  FUNCDUPL(XQST, 39, "Duplicate function argument $%."),
  /** XQST0040: Parsing exception. */
  ATTDUPL(XQST, 40, "Duplicate attribute \"%\"."),
  /** XQST0045: Parsing exception. */
  NAMERES(XQST, 45, "Function %(...) uses reserved namespace."),
  /** XQST0045: Parsing exception. */
  FUNCRES(XQST, 45, "Node type \"%()\" is reserved."),
  /** XQST0047: Parsing exception. */
  DUPLMODULE(XQST, 47, "Module is defined twice: %."),
  /** XQST0047: Parsing exception. */
  MODNS(XQST, 48, "Declaration % does not match the module namespace."),
  /** XQST0049: Parsing exception. */
  VARDEFINE(XQST, 49, "Duplicate definition of %."),
  /** XQST0054: Parsing exception. */
  XPSTACK(XQST, 54, "Circular variable definition?"),
  /** XQST0054: Parsing exception. */
  VARMISSING(XQST, 54, "Expecting variable declaration."),
  /** XQST0055: Parsing exception. */
  DUPLCOPYNS(XQST, 55, "Duplicate 'copy-namespace' declaration."),
  /** XQST0057: Parsing exception. */
  NSEMPTY(XQST, 57, "Namespace URI cannot be empty."),
  /** XQST0059: Parsing exception. */
  NOMODULE(XQST, 59, "Unknown module for namespace \"%\"."),
  /** XQST0059: Parsing exception. */
  NOMODULEFILE(XQST, 59, "Module not found: \"%\"."),
  /** XQST0059: Parsing exception. */
  WRONGMODULE(XQST, 59, "Wrong uri % in imported module \"%\"."),
  /** XQST0060: Parsing exception. */
  FUNNONS(XQST, 60, "Namespace needed for function %(...)."),
  /** XQST0065: Parsing exception. */
  DUPLORD(XQST, 65, "Duplicate 'ordering' declaration."),
  /** XQST0066: Parsing exception. */
  DUPLNS(XQST, 66, "Duplicate 'default namespace' declaration."),
  /** XQST0067: Parsing exception. */
  DUPLCONS(XQST, 67, "Duplicate 'construction' declaration."),
  /** XQST0068: Parsing exception. */
  DUPLBOUND(XQST, 68, "Duplicate 'boundary-space' declaration."),
  /** XQST0069: Parsing exception. */
  DUPLORDEMP(XQST, 69, "Duplicate 'order empty' declaration."),
  /** XQST0070: Parsing exception. */
  NSDEF(XQST, 70, "Cannot overwrite namespace %."),
  /** XQST0070: Parsing exception. */
  NOXMLNS(XQST, 70, "Cannot declare % namespace."),
  /** XQST0071: Parsing exception. */
  DUPLNSDEF(XQST, 71, "Duplicate declaration of namespace \"%\"."),
  /** XQST0073: Parsing exception. */
  CIRCMODULE(XQST, 73, "Circular module definition."),
  /** XQST0075: Parsing exception. */
  IMPLVAL(XQST, 75, "Validation not supported yet."),
  /** XQST0076: Parsing exception. */
  INVCOLL(XQST, 76, "Unknown collation \"%\"."),
  /** XQST0079: Parsing exception. */
  NOPRAGMA(XQST, 79, "Expecting pragma expression."),
  /** XQST0085: Parsing exception. */
  NSEMPTYURI(XQST, 85, "Namespace URI cannot be empty."),
  /** XQST0087: Parsing exception. */
  XQUERYENC2(XQST, 87, "Imprecise encoding definition '%'."),
  /** XQST0088: Parsing exception. */
  NSMODURI(XQST, 88, "Module namespace cannot be empty."),
  /** XQST0089: Parsing exception. */
  VARDEFINED(XQST, 89, "Duplicate definition of %."),
  /** XQST0090: Parsing exception. */
  INVCHARREF(XQST, 90, "Invalid character reference \"%\"."),
  /** XPST0094: Parsing exception. */
  GVARNOTDEFINED(XQST, 94, "Undefined grouping variable \"%\"."),
  /** XQST0108: Parsing exception. */
  MODOUT(XQST, 108, "No serialization parameters allowed in module."),
  /** XPST0109: Parsing exception. */
  OUTWHICH(XQST, 109, "Unknown serialization parameter: \"%\"."),
  /** XPST0110: Parsing exception. */
  OUTDUPL(XQST, 110, "Duplicate definition of \"output:%\"."),

  /** XQTY0024: Parsing exception. */
  NOATTALL(XQTY, 24, "Attribute must follow the root element."),

  /** XTDE0030: Parsing exception. */
  WRONGINT(XTDE, 30, "Wrong integer format: \"%\"."),

  /** XUDY0009: XQuery Update dynamic exception. */
  UPNOPAR(XUDY, 9, "Target % has no parent."),
  /** XUDY0014: XQuery Update dynamic exception. */
  UPNOTCOPIED(XUDY, 14, "% was not copied by copy clause."),
  /** XUDY0015: XQuery Update dynamic exception. */
  UPMULTREN(XUDY, 15, "Multiple renames on %."),
  /** XUDY0016: XQuery Update dynamic exception. */
  UPMULTREPL(XUDY, 16, "Multiple replaces on %."),
  /** XUDY0017: XQuery Update dynamic exception. */
  UPMULTREPV(XUDY, 17, "Multiple replaces on %."),
  /** XUDY0021: XQuery Update dynamic exception. */
  UPATTDUPL(XUDY, 21, "Duplicate attribute %."),
  /** XUDY0023: XQuery Update dynamic exception. */
  UPNSCONFL(XUDY, 23, "Conflicts with existing namespaces."),
  /** XUDY0024: XQuery Update dynamic exception. */
  UPNSCONFL2(XUDY, 24, "New namespaces conflict with each other."),
  /** XUDY0027: XQuery Update dynamic exception. */
  UPSEQEMP(XUDY, 27, "% target must not be empty."),
  /** XUDY0029: XQuery Update dynamic exception. */
  UPPAREMPTY(XUDY, 29, "Target has no parent node."),
  /** XUDY0030: XQuery Update dynamic exception. */
  UPATTELM(XUDY, 30, "Attributes must be inserted after/before an element."),
  /** XUDY0031: XQuery Update dynamic exception. */
  UPURIDUP(XUDY, 31, "Multiple use of URI: \"%\"."),

  /** XUST0001: Parsing exception. */
  UPNOT(XUST, 1, "%: no updating expression allowed."),
  /** XUST0002: Parsing exception. */
  UPEXPECTT(XUST, 2, "Updating expression expected in modify clause."),
  /** XUST0002: Parsing exception. */
  UPEXPECTF(XUST, 2, "Updating expression expected in function declaration."),
  /** XUST0003: Parsing exception. */
  DUPLREVAL(XUST, 3, "Duplicate 'revalidation' declaration."),
  /** XUST0026: Parsing exception. */
  NOREVAL(XUST, 26, "Revalidation mode not supported."),
  /** XUST0028: Parsing exception. */
  UPFUNCTYPE(XUST, 28, "No return type allowed in updating functions."),

  /** XUTY0004: XQuery Update type exception. */
  UPNOATTRPER(XUTY, 4, "Attribute must follow the root element."),
  /** XUTY0005: XQuery Update type exception. */
  UPTRGTYP(XUTY, 5, "Single element or document expected as insert target."),
  /** XUTY0006: XQuery Update type exception. */
  UPTRGTYP2(XUTY, 6,
      "Single element, text, comment or pi expected as insert target."),
  /** XUTY0007: XQuery Update type exception. */
  UPTRGDELEMPT(XUTY, 7, "Only nodes can be deleted."),
  /** XUTY0008: XQuery Update type exception. */
  UPTRGMULT(XUTY, 8, "Single element, text, attribute, comment or pi expected" +
      " as replace target."),
  /** XUTY0010: XQuery Update type exception. */
  UPWRELM(XUTY, 10, "Replace nodes must not be attribute nodes."),
  /** XUTY0011: XQuery Update type exception. */
  UPWRATTR(XUTY, 11, "Replace nodes must be attribute nodes."),
  /** XUTY0012: XQuery Update type exception. */
  UPWRTRGTYP(XUTY, 12,
      "Single element, attribute or pi expected as rename target."),
  /** XUTY0013: XQuery Update type exception. */
  UPCOPYMULT(XUTY, 13,
      "Source expression in copy clause must return a single node."),
  /** XUTY0022: XQuery Update type exception. */
  UPATTELM2(XUTY, 22, "Insert target must be an element.");

  /** Error type. */
  public final Type type;
  /** Error number. */
  public final int num;
  /** Error description. */
  public final String desc;

  /**
   * Constructor.
   * @param t error type
   * @param n error number
   * @param d description
   */
  Err(final Type t, final int n, final String d) {
    type = t;
    num = n;
    desc = d;
  }

  /**
   * Throws an exception.
   * @param ii input info
   * @param ext extended info
   * @throws QueryException query exception
   */
  public void thrw(final InputInfo ii, final Object... ext)
      throws QueryException {
    throw new QueryException(ii, this, ext);
  }

  /**
   * Returns the error code.
   * @return position
   */
  public String code() {
    return String.format("%s%04d", type, num);
  }

  /**
   * Error types.
   * @author Leo Woerteler
   */
  public static enum Type {
    /** BASX Error type. */ BASX,
    /** FOAR Error type. */ FOAR,
    /** FOCA Error type. */ FOCA,
    /** FOCH Error type. */ FOCH,
    /** FODC Error type. */ FODC,
    /** FODF Error type. */ FODF,
    /** FODT Error type. */ FODT,
    /** FOER Error type. */ FOER,
    /** FOFL Error type. */ FOFL,
    /** FONS Error type. */ FONS,
    /** FORG Error type. */ FORG,
    /** FORX Error type. */ FORX,
    /** FOUP Error type. */ FOUP,
    /** FTDY Error type. */ FTDY,
    /** FTST Error type. */ FTST,
    /** XPDY Error type. */ XPDY,
    /** XPST Error type. */ XPST,
    /** XPTY Error type. */ XPTY,
    /** XQDY Error type. */ XQDY,
    /** XQST Error type. */ XQST,
    /** XQTY Error type. */ XQTY,
    /** XQTD Error type. */ XTDE,
    /** XUDY Error type. */ XUDY,
    /** XUST Error type. */ XUST,
    /** XUTY Error type. */ XUTY;
  }

  /**
   * Throws a comparison exception.
   * @param ii input info
   * @param it1 first item
   * @param it2 second item
   * @throws QueryException query exception
   */
  public static void diff(final InputInfo ii, final Item it1, final Item it2)
      throws QueryException {
    if(it1 == it2) TYPECMP.thrw(ii, it1.type);
    else XPTYPECMP.thrw(ii, it1.type, it2.type);
  }

  /**
   * Throws a numeric type exception.
   * @param ii input info
   * @param t expression cast type
   * @param v value
   * @throws QueryException query exception
   */
  public static void cast(final InputInfo ii, final org.basex.query.item.Type t,
      final Value v) throws QueryException {
    XPINVCAST.thrw(ii, v.type, t, v);
  }

  /**
   * Throws a type exception.
   * @param ii input info
   * @param inf expression info
   * @param t expected type
   * @param it found item
   * @throws QueryException query exception
   */
  public static void type(final InputInfo ii, final String inf,
      final org.basex.query.item.Type t, final Item it) throws QueryException {
    XPTYPE.thrw(ii, inf, t, it.type);
  }

  /**
   * Throws a type exception.
   * @param e parsing expression
   * @param t expected type
   * @param it found item
   * @throws QueryException query exception
   */
  public static void type(final ParseExpr e, final org.basex.query.item.Type t,
      final Item it) throws QueryException {
    type(e.input, e.desc(), t, it);
  }

  /**
   * Throws a number exception.
   * @param e parsing expression
   * @param it found item
   * @throws QueryException query exception
   */
  public static void number(final ParseExpr e, final Item it)
      throws QueryException {
    XPTYPENUM.thrw(e.input, e.desc(), it.type);
  }

  /**
   * Throws an invalid value exception.
   * @param ii input info
   * @param t expected type
   * @param v value
   * @throws QueryException query exception
   */
  public static void value(final InputInfo ii,
      final org.basex.query.item.Type t, final Object v) throws QueryException {
    INVALUE.thrw(ii, t, v);
  }

  @Override
  public String toString() {
    return String.format("%s%04d", type, num);
  }
}
