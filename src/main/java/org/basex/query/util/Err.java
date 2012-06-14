
package org.basex.query.util;

import static org.basex.query.util.Err.ErrType.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class contains all query error messages.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum Err {

  // General errors

  /** BASX0000. */
  BASX_GENERIC(BASX, 0, "%"),
  /** BASX0001. */
  BASX_PERM(BASX, 1, "% permission required."),
  /** BASX0002. */
  BASX_OPTIONS(BASX, 2, "Unknown database option '%'."),
  /** BASX0003. */
  BASX_RESTXQ(BASX, 3, "%"),

  // Client module

  /** BXCL0001. */
  BXCL_CONN(BXCL, 1, "Connection failed: %"),
  /** BXCL0002. */
  BXCL_NOTAVL(BXCL, 2, "Session is not available or has already been closed: %"),
  /** BXCL0003. */
  BXCL_COMM(BXCL, 3, "An error occurred: %"),
  /** BXCL0004. */
  BXCL_COMMAND(BXCL, 4, "Command could not be executed: %"),
  /** BXCL0005. */
  BXCL_QUERY(BXCL, 5, "Query could not be executed: %"),

  // Convert module (to be moved from Utility module)

  /** BXCO0001. */
  BXCO_STRING(BXCO, 1, "String conversion: %."),
  /** BXCO0001. */
  BXCO_BASE64(BXCO, 1, "String cannot be converted to the specified encoding."),
  /** BXCO0002. */
  BXCO_ENCODING(BXCO, 2, "Encoding '%' is not supported."),

  // Database module

  /** BXDB0001. */
  BXDB_NODB(BXDB, 1, "%: database node expected."),
  /** BXDB0002. */
  BXDB_OPEN(BXDB, 2, "%"),
  /** BXDB0003. */
  BXDB_MEM(BXDB, 3, "Operation requires database '%' to be persistent."),
  /** BXDB0004. */
  BXDB_INDEX(BXDB, 4, "Database '%' has no % index."),
  /** BXDB0005. */
  BXDB_DBRETURN(BXDB, 5, "Query must yield database nodes."),
  /** BXDB0006. */
  BXDB_SINGLE(BXDB, 6, "Database path '%' must point to a single document."),
  /** BXDB0007. */
  BXDB_OPENED(BXDB, 7, "Database '%' is opened by another process."),
  /** BXDB0008. */
  BXDB_EMPTY(BXDB, 8, "%: Database paths must not be empty."),
  /** BXDB0009. */
  BXDB_RANGE(BXDB, 9, "%: value '%' is out of range."),
  /** BXDB0010. */
  BXDB_EVENT(BXDB, 10, "Event '%' is unknown."),

  // Fulltext module

  /** BXFT0001. */
  BXFT_MATCH(BXFT, 1, "Either wildcards or fuzzy search supported."),

  // JSON module

  /** BXJS0001. */
  BXJS_PARSE(BXJS, 1, "JSON parser (%:%): %."),
  /** BXJS0001. */
  BXJS_PARSEML(BXJS, 1, "JsonML parser: %."),
  /** BXJS0002. */
  BXJS_SER(BXJS, 1, "JSON serialization: %."),

  /// Process module

  /** BXPR9999. */
  BXPR_ENC(BXPR, 9999, "Encoding not supported: '%'."),

  // Repository module

  /** BXRE0001. */
  BXRE_WHICH(BXRE, 1, "Package '%' does not exist."),
  /** BXRE0002. */
  BXRE_URI(BXRE, 2, "Namespace URI is invalid: '%'."),
  /** BXRE0003. */
  BXRE_NOTINST(BXRE, 3, "Required package '%' is not installed."),
  /** BXRE0004. */
  BXRE_DESC(BXRE, 4, "Package descriptor: %."),
  /** BXRE0005. */
  BXRE_INST(BXRE, 5, "Module % is already installed within another package."),
  /** BXRE0006. */
  BXRE_PARSE(BXRE, 6, "Package '%' could not be parsed: %."),
  /** BXRE0006. */
  BXRE_PARSENF(BXRE, 6, "Package '%' could not be parsed: '%' not found."),
  /** BXRE0007. */
  BXRE_DELETE(BXRE, 7, "File '%' could not be deleted."),
  /** BXRE0008. */
  BXRE_DEP(BXRE, 8, "Package '%' depends on package '%'."),
  /** BXRE0009. */
  BXRE_VERSION(BXRE, 9, "Package version is not supported."),
  /** BXRE0010. */
  BXRE_JARDESC(BXRE, 10, "JAR descriptor: %."),
  /** BXRE0011. */
  BXRE_JARFAIL(BXRE, 11, "Reading JAR descriptor failed: %."),

  // SQL module

  /** BXSQ0001. */
  BXSQ_ERROR(BXSQ, 1, "An SQL exception occurred: '%'"),
  /** BXSQ0002. */
  BXSQ_CONN(BXSQ, 2, "No opened connection with id %"),
  /** BXSQ0003. */
  BXSQ_PARAMS(BXSQ, 3, "Number of parameters differs from number of placeholders"),
  /** BXSQ0004. */
  BXSQ_TYPE(BXSQ, 4, "No parameter type specified."),
  /** BXSQ0005. */
  BXSQ_ATTR(BXSQ, 5, "Not expected attribute: %"),
  /** BXSQ0006. */
  BXSQ_FORMAT(BXSQ, 6, "Illegal % format"),
  /** BXSQ0007. */
  BXSQ_DRIVER(BXSQ, 7, "Could not initialize specified driver: '%'"),

  // Validation module

  /** BXVA0001. */
  BXVA_FAIL(BXVA, 1, "Validation failed: %"),

  // EXPath modules

  /** ARCH0001. */
  ARCH_DIFF(ARCH, 1, "Number of entries (%) and contents (%) differs."),
  /** ARCH0002. */
  ARCH_STRB64(ARCH, 2, "xs:string or xs:base64Binary expected, % found"),
  /** ARCH0003. */
  ARCH_NAME(ARCH, 3, "Name of ZIP entry must not be empty."),
  /** ARCH0003. */
  ARCH_LEVEL(ARCH, 3, "Invalid compression level: '%'."),
  /** ARCH0003. */
  ARCH_MODIFIED(ARCH, 3, "xs:dateTime value is invalid : '%'."),
  /** ARCH0004. */
  ARCH_ENCODING(ARCH, 4, "Encoding is not supported: '%'."),
  /** ARCH0004. */
  ARCH_ENCODE(ARCH, 4, "String conversion: %."),
  /** ARCH0005. */
  ARCH_SUPP(ARCH, 5, "% not supported: '%'."),
   /** ARCH9999. */
  ARCH_FAIL(ARCH, 9999, "Operation failed: %."),

  /** CX0001. */
  CX_CANINV(CX, 1, "Canonicalization algorithm is not supported."),
  /** CX0002. */
  CX_DIGINV(CX, 2, "Digest algorithm is not supported."),
  /** CX0003. */
  CX_SIGINV(CX, 3, "Signature algorithm is not supported."),
  /** CX0004. */
  CX_XPINV(CX, 4, "XPath expression is invalid."),
  /** CX0005. */
  CX_INVNM(CX, 5, "Invalid name for $digital-certificate root."),
  /** CX0007. */
  CX_KSNULL(CX, 7, "Key store is null."),
  /** CX0012. */
  CX_NOKEY(CX, 12, "Cannot find key for alias in given keystore."),
  /** CX0013. */
  CX_INVHASH(CX, 13, "Hashing algorithm is not supported."),
  /** CX0014. */
  CX_ENC(CX, 14, "The encoding method is not supported."),
  /** CX0015. */
  CX_NOSIG(CX, 15, "Cannot find signature element."),
  /** CX0016. */
  CX_NOPAD(CX, 16, "No such padding."),
  /** CX0017. */
  CX_BADPAD(CX, 17, "Incorrect padding."),
  /** CX0018. */
  CX_ENCTYP(CX, 18, "Encryption type is not supported."),
  /** CX0019. */
  CX_KEYINV(CX, 19, "Secret key is invalid."),
  /** CX0020. */
  CX_ILLBLO(CX, 20, "Illegal block size."),
  /** CX0021. */
  CX_INVALGO(CX, 21, "Algorithm is not supported."),
  /** CX0023. */
  CX_ALINV(CX, 23, "Invalid certificate alias %."),
  /** CX0024. */
  CX_ALGEXC(CX, 24, "Invalid algorithm."),
  /** CX0025. */
  CX_IOEXC(CX, 25, "IO Exception."),
  /** CX0026. */
  CX_KSEXC(CX, 26, "Keystore exception."),
  /** CX0027. */
  CX_SIGEXC(CX, 27, "Signature exception."),
  /** CX0028. */
  CX_SIGTYPINV(CX, 28, "Signature type is not supported."),

  /** FILE0001. */
  FILE_WHICH(FILE, 1, "Path '%' does not exist."),
  /** FILE0002. */
  FILE_EXISTS(FILE, 2, "File '%' already exists."),
  /** FILE0003. */
  FILE_NODIR(FILE, 3, "Path '%' is no directory."),
  /** FILE0004. */
  FILE_DIR(FILE, 4, "Path '%' is a directory."),
  /** FILE0004. */
  FILE_NEDIR(FILE, 4, "Path '%' is a non-empty directory."),
  /** FILE0005. */
  FILE_ENCODING(FILE, 5, "Encoding '%' is not supported."),
  /** FILE9999. */
  FILE_IO(FILE, 9999, "%"),
  /** FILE9999. */
  FILE_CREATE(FILE, 9999, "Directory '%' cannot be created."),
  /** FILE9999. */
  FILE_DEL(FILE, 9999, "Path '%' cannot be deleted."),
  /** FILE9999. */
  FILE_MOVE(FILE, 9999, "Moving '%' to '%' failed."),
  /** FILE9999. */
  FILE_LIST(FILE, 9999, "Files of '%' cannot be accessed."),
  /** FILE9999. */
  FILE_PATH(FILE, 9999, "Invalid file path: '%'."),

  /** HASH0001. */
  HASH_ALG(HASH, 1, "Algorithm not supported: '%'."),

  /** HC0001. */
  HC_ERROR(HC, 1, "An HTTP error occurred: %."),
  /** HC0003. */
  HC_ATTR(HC, 3, "No attribute beside 'src' and 'media-type' allowed."),
  /** HC0004. */
  HC_REQ(HC, 4, "Invalid request element: %."),
  /** HC0005. */
  HC_URL(HC, 5, "No URL specified."),
  /** HC0006. */
  HC_PARAMS(HC, 6, "Specify request element or HTTP URI."),

  /** ZIP0001. */
  ZIP_NOTFOUND(ZIP, 1, "Path '%' is not found."),
  /** ZIP0002. */
  ZIP_INVALID(ZIP, 2, "% element: % attribute expected."),
  /** ZIP0002. */
  ZIP_UNKNOWN(ZIP, 2, "ZIP definition: unknown element %."),
  /** ZIP0003. */
  ZIP_FAIL(ZIP, 3, "Operation failed: %."),

  // W3 Functions

  /** FOAR0001. */
  DIVZERO(FOAR, 1, "'%' was divided by zero."),
  /** FOAR0002. */
  DIVFLOW(FOAR, 2, "Invalid division result: % / %."),
  /** FOAR0002. */
  RANGE(FOAR, 2, "Value out of range: %."),

  /** FOCA0002. */
  INVALUE(FOCA, 2, "Invalid value for %: %."),
  /** FOCA0003. */
  INTRANGE(FOCA, 3, "Integer value out of range: %."),
  /** FOCA0005. */
  DATECALC(FOCA, 5, "Invalid % calculation: %."),

  /** FOCH0001. */
  INVCODE(FOCH, 1, "Invalid codepoint '%'."),
  /** FOCH0002. */
  IMPLCOL(FOCH, 2, "Unknown collation %."),
  /** FOCH0003. */
  NORMUNI(FOCH, 3, "Unsupported normalization form (%)."),

  /** FODC0001. */
  IDDOC(FODC, 1, "Root must be a document node."),
  /** FODC0002. */
  NODEERR(FODC, 2, "% could not be created (%)."),
  /** FODC0002. */
  NODEFCOLL(FODC, 2, "No default collection available."),
  /** FODC0002. */
  IOERR(FODC, 2, "%"),
  /** FODC0002. */
  WHICHRES(FODC, 2, "Resource '%' does not exist."),
  /** FODC0004. */
  INVCOLL(FODC, 4, "Invalid collection URI '%'."),
  /** FODC0005. */
  INVDOC(FODC, 5, "Invalid document URI '%'."),
  /** FODC0006. */
  SAXERR(FODC, 6, "SAX: %."),
  /** FODC0007. */
  BASEINV(FODC, 7, "Base URI % is invalid."),
  /** FODC0007. */
  RESINV(FODC, 7, "Resource path '%' is invalid."),
  /** FODC0007. */
  INVDB(FODC, 7, "Invalid database name: '%'."),

  /** FODF1280. */
  FORMNUM(FODF, 1280, "Unknown decimal format: %."),
  /** FODF1310. */
  PICNUM(FODF, 1310, "Invalid picture string: '%'."),

  /** FODT0002. */
  DATEZERO(FODT, 2, "Invalid % calculation: infinity/zero."),
  /** FODT0003. */
  INVALZONE(FODT, 3, "Invalid timezone: %."),

  /** FOER0000. */
  FUNERR1(FOER, 0, "Halted on error()."),


  /** FONS0004. */
  NSDECL(FONS, 4, "No namespace declared for prefix '%'."),

  /** FORG0001. */
  INVALIDZONE(FORG, 1, "Invalid timezone: %."),
  /** FORG0001. */
  FUNCAST(FORG, 1, "Invalid % cast: %."),
  /** FORG0001. */
  DATERANGE(FORG, 1, "%('%') out of range."),
  /** FORG0001. */
  DATEFORMAT(FORG, 1, "Wrong % format: '%' (try: '%')."),
  /** FORG0002. */
  URIINV(FORG, 2, "Invalid URI: %."),
  /** FORG0002. */
  URIABS(FORG, 2, "Base URI % is not absolute."),

  /** FORG0003. */
  EXPECTZ0(FORG, 3, "Zero or one value expected."),
  /** FORG0004. */
  EXPECTOM(FORG, 4, "One or more values expected."),
  /** FORG0005. */
  EXPECTO(FORG, 5, "Exactly one value expected."),
  /** FORG0006. */
  FUNCMP(FORG, 6, "%: % expected, % found."),
  /** FORG0006. */
  CONDTYPE(FORG, 6, "% not allowed as condition type."),
  /** FORG0006. */
  SUMTYPE(FORG, 6, "%: % not allowed as input type."),
  /** FORG0006. */
  FUNNUM(FORG, 6, "%: number expected, % found."),
  /** FORG0006. */
  FUNDUR(FORG, 6, "%: duration expected, % found."),
  /** FORG0006. */
  TYPECMP(FORG, 6, "% is not comparable."),
  /** FORG0006. */
  JAVAFUN(FORG, 6, "Invalid call of Java function: %(%)."),
  /** FORG0006. */
  JAVAMOD(FORG, 6, "Invalid arguments: % expected, % found."),
  /** FORG0006. */
  INVBASE(FORG, 6, "Unsupported base: %."),
  /** FORG0006. */
  INVDIG(FORG, 6, "Invalid digit for base %: %."),
  /** FORG0006. */
  JAVAERR(FORG, 6, "Java function call failed: %."),
  /** FORG0006. */
  ERRFORM(FORG, 6, "%: %."),
  /** FORG0008. */
  FUNZONE(FORG, 8, "% and % have different timezones."),

  /** FORX0001. */
  REGMOD(FORX, 1, "Invalid regular modifier: '%'."),
  /** FORX0003. */
  REGROUP(FORX, 3, "Pattern matches empty string."),
  /** FORX0004. */
  FUNREGREP(FORX, 4, "Invalid replacement expression."),
  /** FORX0004. */
  REGERR(FORX, 4, "Regular expression: '%'."),

  /** FOTY0012. */
  FIVALUE(FOTY, 12, "Item has no typed value: %."),
  /** FOTY0013. */
  FIATOM(FOTY, 13, "Function items cannot be atomized: %."),
  /** FOTY0013. */
  FIEQ(FOTY, 13, "Function items have no defined equality: %."),
  /** FOTY0013. */
  FISTR(FOTY, 14, "Function items have no string representation: %."),
  /** FOTY0013. */
  FICMP(FOTY, 15, "Function items cannot be compared: %."),

  /** FOUP0001. */
  UPFOTYPE(FOUP, 1, "Document or element expected, % found."),
  /** FOUP0001. */
  UPDOCTYPE(FOUP, 1, "Document expected, % found."),
  /** FOUP0002. */
  UPFOURI(FOUP, 2, "No valid URI: \"%\"."),
  /** FOUP0002. */
  UPPUTERR(FOUP, 2, "\"%\" could not be written."),
  /** FOUP0002. */
  UPDBPUTERR(FOUP, 2, "Resource \"%\" could not be written."),
  /** FOUP0002. */
  UPDBDELERR(FOUP, 2, "Resource \"%\" could not be deleted."),
  /** FOUP0002. */
  UPDBRENAMEERR(FOUP, 2, "Resource \"%\" could not be renamed."),
  /** FOUP0002. */
  UPDBOPTERR(FOUP, 2, "Resource \"%\" could not be optimized."),

  /** FTDY0016. */
  FTWEIGHT(FTDY, 16, "Weight value out of range: %."),
  /** FTDY0017. */
  FTMILD(FTDY, 17, "Invalid 'mild not' selection."),
  /** FTDY0020. */
  FTREG(FTDY, 20, "Invalid wildcard syntax: '%'."),

  /** FTST0007. */
  FTIGNORE(FTST, 7, "Ignore option not supported."),
  /** FTST0008. */
  NOSTOPFILE(FTST, 8, "Stop word file not found: '%'."),
  /** FTST0009. */
  FTNOSTEM(FTST, 9, "No stemmer available for language '%'."),
  /** FTST0009. */
  FTNOTOK(FTST, 9, "No tokenizer available for language '%'."),
  /** FTST0018. */
  NOTHES(FTST, 18, "Thesaurus not found: '%'."),
  /** FTST0019. */
  FTDUP(FTST, 19, "Match option '%' was declared twice."),

  /** SESU0007. */
  SERENCODING(SESU, 7, "Encoding not supported: '%'."),
  /** SEPM0009. */
  SERSTAND(SEPM, 9, "Invalid combination of 'omit-xml-declaration'."),
  /** SEPM0010. */
  SERUNDECL(SEPM, 10, "XML 1.0: undeclaring prefixes not allowed."),
  /** SERE0014. */
  SERILL(SERE, 14, "Illegal HTML character found: #x%."),
  /** SERE0015. */
  SERPI(SERE, 15, "Processing construction contains '>'."),
  /** SEPM0016. */
  SERINVALID(SEPM, 16, "Parameter '%' is unknown."),
  /** SEPM0016. */
  SERMAP(SEPM, 16, "Character map '%' is not defined."),
  /** SEPM0016. */
  SERANY(SEPM, 16, "%."),

  /** XPDY0002. */
  VAREMPTY(XPDY, 2, "No value assigned to %."),
  /** XPDY0002. */
  XPNOCTX(XPDY, 2, "No context item defined to evaluate '%'."),
  /** XPDY0050. */
  CTXNODE(XPDY, 50, "Root of the context item must be a document node."),
  /** XPDY0050. */
  NOTREAT(XPDY, 50, "%: % cannot be treated as %."),
  /** XPDY0050. */
  NOTREATS(XPDY, 50, "%: % expected, sequence found."),

  /** XPST0001. */
  STBASEURI(XPST, 1, "Static Base URI is undefined."),
  /** XPST0003. */
  QUERYEMPTY(XPST, 3, "Empty query."),
  /** XPST0003. */
  QUERYINV(XPST, 3, "Query contains an illegal character (#%)."),
  /** XPST0003. */
  NOQUOTE(XPST, 3, "Expecting quote%."),
  /** XPST0003. */
  NOVALIDATE(XPST, 3, "Invalid validation expression."),
  /** XPST0003. */
  NUMBERWS(XPST, 3, "Expecting separator after number."),
  /** XPST0003. */
  NUMBERINC(XPST, 3, "Incomplete double value: '%'."),
  /** XPST0003. */
  NUMBERITR(XPST, 3, "Unexpected decimal point."),
  /** XPST0003. */
  QUERYEND(XPST, 3, "Unexpected end of query: '%'."),
  /** XPST0003. */
  MODEXPR(XPST, 3, "No expression allowed in a library module."),
  /** XPST0003. */
  MAINMOD(XPST, 3, "Library modules cannot be evaluated."),
  /** XPST0003. */
  CMPEXPR(XPST, 3, "Comparison is incomplete."),
  /** XPST0003. */
  NOTAG(XPST, 3, "Expecting tag name."),
  /** XPST0003. */
  TAGNAME(XPST, 3, "Expecting tag name, '<%' found."),
  /** XPST0003. */
  NOATTNAME(XPST, 3, "Expecting attribute name."),
  /** XPST0003. */
  NOLOCSTEP(XPST, 3, "Incomplete location step."),
  /** XPST0003. */
  NOEXPR(XPST, 3, "Expecting expression."),
  /** XPST0003. */
  WRONGCHAR(XPST, 3, "Expecting '%'%."),
  /** XPST0003. */
  INVENTITY(XPST, 3, "Invalid entity '%'."),
  /** XPST0003. */
  INCOMPLETE(XPST, 3, "Incomplete expression."),
  /** XPST0003. */
  EVALUNARY(XPST, 3, "Unary operator expects a numeric value."),
  /** XPST0003. */
  PATHMISS(XPST, 3, "Expecting location path%."),
  /** XPST0003. */
  DECLINCOMPLETE(XPST, 3, "Expecting 'function', 'variable', ..."),
  /** XPST0003. */
  FUNCNAME(XPST, 3, "Expecting function name."),
  /** XPST0003. */
  RESERVED(XPST, 3, "% is a reserved function name."),
  /** XPST0003. */
  PREDMISSING(XPST, 3, "Expecting expression before predicate."),
  /** XPST0003. */
  NOVARNAME(XPST, 3, "Expecting variable name."),
  /** XPST0003. */
  NOVARDECL(XPST, 3, "Expecting variable declaration."),
  /** XPST0003. */
  TAGWRONG(XPST, 3, "Start and end tag are different: <%>...</%>."),
  /** XPST0003. */
  PIWRONG(XPST, 3, "Expecting name of processing-instruction."),
  /** XPST0003. */
  NSWRONG(XPST, 3, "Expecting namespace prefix."),
  /** XPST0003. */
  NOENCLEXPR(XPST, 3, "Expecting valid expression after '{'."),
  /** XPST0003. */
  NODOCCONS(XPST, 3, "Expecting expression in document constructor."),
  /** XPST0003. */
  NOTXTCONS(XPST, 3, "Expecting expression in text constructor."),
  /** XPST0003. */
  NOCOMCONS(XPST, 3, "Expecting expression in comment constructor."),
  /** XPST0003. */
  NOFUNBODY(XPST, 3, "Expecting function body."),
  /** XPST0003. */
  FUNCMISS(XPST, 3, "Expecting closing bracket for '%(...'."),
  /** XPST0003. */
  MAPTAAT(XPST, 3, "Expecting atomic key type for map(...), found '%'."),
  /** XPST0003. */
  TYPEINVALID(XPST, 3, "Expecting type declaration."),
  /** XPST0003. */
  NODECLFORM(XPST, 3, "Unknown decimal-format property: '%'."),
  /** XPST0003. */
  NOTYPESWITCH(XPST, 3, "Incomplete typeswitch expression."),
  /** XPST0003. */
  NOSWITCH(XPST, 3, "Incomplete switch expression."),
  /** XPST0003. */
  TYPEPAR(XPST, 3, "Expecting '(' after 'switch' or 'typeswitch'."),
  /** XPST0003. */
  PRAGMAINV(XPST, 3, "Invalid pragma expression."),
  /** XPST0003. */
  CALCEXPR(XPST, 3, "Calculation is incomplete."),
  /** XPST0003. */
  INVMAPKEY(XPST, 3, "Invalid key, simple expression expected."),
  /** XPST0003. */
  INVMAPVAL(XPST, 3, "Invalid value, simple expression expected."),
  /** XPST0003. */
  NORETURN(XPST, 3, "Expecting return value."),
  /** XPST0003. */
  NOWHERE(XPST, 3, "Expecting valid expression after 'where'."),
  /** XPST0003. */
  ORDERBY(XPST, 3, "Expecting valid expression after 'order by'."),
  /** XPST0003. */
  GRPBY(XPST, 3, "Expecting valid expression after 'group by'."),
  /** XPST0003. */
  FLWORWHERE(XPST, 3, "Expecting 'where', 'order' or 'return' expression."),
  /** XPST0003. */
  FLWORORD(XPST, 3, "Expecting 'order' or 'return' expression."),
  /** XPST0003. */
  FLWORRET(XPST, 3, "Expecting 'return' expression."),
  /** XPST0003. */
  NOSOME(XPST, 3, "Incomplete quantifier expression."),
  /** XPST0003. */
  IFPAR(XPST, 3, "Expecting '(' after 'if' expression."),
  /** XPST0003. */
  NOIF(XPST, 3, "Incomplete 'if' expression."),
  /** XPST0003. */
  NOFOR(XPST, 3, "Incomplete 'for' expression."),
  /** XPST0003. */
  NOLET(XPST, 3, "Incomplete 'let' expression."),
  /** XPST0003. */
  NOCLOSING(XPST, 3, "Expecting closing tag </%>."),
  /** XPST0003. */
  COMCLOSE(XPST, 3, "Unclosed XQuery comment (: ..."),
  /** XPST0003. */
  EXPREMPTY(XPST, 3, "Unknown function or expression."),
  /** XPST0003. */
  NOTYPE(XPST, 3, "Unknown type '%'."),
  /** XPST0003. */
  PIXML(XPST, 3, "Processing instruction has illegal name: '%'."),
  /** XPST0003. */
  QNAMEINV(XPST, 3, "Expecting QName, '%' found."),
  /** XPST0003. */
  PROLOGORDER(XPST, 3, "Default declarations must be declared first."),
  /** XPST0003. */
  FTRANGE(XPST, 3, "Expecting full-text range."),
  /** XPST0003. */
  FTSTOP(XPST, 3, "Stop words expected."),
  /** XPST0003. */
  FTMATCH(XPST, 3, "Unknown match option '%...'."),
  /** XPST0003. */
  INVALPI(XPST, 3, "Processing instruction has invalid name."),
  /** XPST0003. */
  INTEXP(XPST, 3, "Integer expected."),
  /** XPST0003. */
  VARFUNC(XPST, 3, "Variable or function declaration expected."),
  /** XPST0003. */
  NOANN(XPST, 3, "No annotation allowed here."),
  /** XPST0003. */
  NOCATCH(XPST, 3, "Expecting catch clause."),
  /** XPST0003. */
  ANNVALUE(XPST, 3, "Literal expected after annotation."),
  /** XPST0003. */
  UPDATINGVAR(XPST, 3, "Variable cannot be updating."),

  /** XPST0005. */
  COMPSELF(XPST, 5, "Warning: '%' will never yield results."),
  /** XPST0005. */
  ATTDESC(XPST, 5, "Warning: '%' cannot have descendants."),
  /** XPST0005. */
  DOCAXES(XPST, 5, "Warning: '%' cannot have % nodes."),

  /** XPST0008. */
  VARUNDEF(XPST, 8, "Undefined variable %."),
  /** XPST0008. */
  TYPEUNDEF(XPST, 8, "Undefined type '%'."),
  /** XPST0008. */
  SCHEMAINV(XPST, 8, "Undefined schema name '%'."),

  /** XPST0017. */
  XPARGS(XPST, 17, "%: wrong number of arguments."),
  /** XPST0017. */
  FUNSIMILAR(XPST, 17, "Unknown function '%'; similar: '%'."),
  /** XPST0017. */
  FUNCTYPE(XPST, 17, "%(...): wrong number of arguments."),
  /** XPST0003. */
  FEATURE30(XPST, 17, "Feature not available in XQuery 1.0."),
  /** XPST0017. */
  FUNCUNKNOWN(XPST, 17, "Unknown function '%(...)'."),
  /** XPST0017. */
  WHICHJAVA(XPST, 17, "Java function '%(...)' not found."),
  /** XPST0017. */
  JAVAAMB(XPST, 17, "Signature is ambiguous: '%(...)'."),
  /** XPST0017. */
  INITJAVA(XPST, 17, "Class cannot be initialized: %."),

  /** XPST0051. */
  TYPEUNKNOWN(XPST, 51, "Unknown type %."),
  /** XPST0080. */
  CASTUNKNOWN(XPST, 80, "Invalid cast type %."),
  /** XPST0081. */
  NOURI(XPST, 81, "No namespace declared for %."),
  /** XPST0081. */
  NSMISS(XPST, 81, "QName % has no namespace."),

  /** XPTY0004. */
  XPSEQ(XPTY, 4, "Single item expected, % found."),
  /** XPTY0004. */
  XPINVCAST(XPTY, 4, "Invalid cast from % to %: %."),
  /** XPTY0004. */
  XPINVPROM(XPTY, 4, "Cannot treat % as %: %."),
  /** XPTY0004. */
  XPTYPE(XPTY, 4, "%: % expected, % found."),
  /** XPTY0004. */
  CALCTYPE(XPTY, 4, "% not defined for % and %."),
  /** XPTY0004. */
  STRNODTYPE(XPTY, 4, "%: xs:string or node() expected, % found."),
  /** XPTY0004. */
  ELMMAPTYPE(XPTY, 4, "element(%) or map expected, % found"),
  /** XPTY0004. */
  BINARYTYPE(XPTY, 4, "Binary type expected, % found"),
  /** XPTY0004. */
  ELMOPTION(XPTY, 4, "Unknown option: %."),

  /** XPTY0004. */
  SIMPLDUR(XPTY, 4, "%: only supported on subtypes of xs:duration, not %."),
  /** XPTY0004. */
  XPEMPTY(XPTY, 4, "%: no empty sequence allowed."),
  /** XPTY0004. */
  XPEMPTYPE(XPTY, 4, "%: % expected, empty sequence found."),
  /** XPTY0004. */
  XPDUR(XPTY, 4, "%: duration expected, % found."),
  /** XPTY0004. */
  XPTYPECMP(XPTY, 4, "% and % cannot be compared."),
  /** XPTY0004. */
  XPTYPENUM(XPTY, 4, "%: number expected, % found."),
  /** XPTY0004. */
  XPNAME(XPTY, 4, "Expecting name."),
  /** XPTY0004. */
  XPATT(XPTY, 4, "Cannot add attributes to a document node."),
  /** XPTY0004. */
  XPNS(XPTY, 4, "Cannot add namespaces to a document node."),
  /** XPTY0004. */
  CPIWRONG(XPTY, 4, "Name has invalid type: '%'."),
  /** XPTY0004. */
  INVQNAME(XPTY, 4, "Invalid QName: '%'."),
  /** XPTY0004. */
  INVARITY(XPTY, 4, "Wrong number of arguments in %, expected %."),
  /** XPTY0004. */
  INVNCNAME(XPTY, 4, "Invalid NCName: '%'."),
  /** XPTY0004. */
  INVPOS(XPTY, 4, "Illegal argument position for %: %."),

  /** XPTY0018. */
  EVALNODESVALS(XPTY, 18, "Result yields both nodes and atomic values."),
  /** XPTY0019. */
  NODESPATH(XPTY, 19, "Context node required for %; % found."),

  /** XQDY0025. */
  CATTDUPL(XQDY, 25, "Duplicate attribute '%'."),
  /** XQDY0026. */
  CPICONT(XQDY, 26, "Processing instruction has invalid content: '%'."),
  /** XQDY0041. */
  CPIINVAL(XQDY, 41, "Processing instruction has invalid name: '%'."),
  /** XQDY0044. */
  CAXML(XQDY, 44, "XML prefix and namespace cannot be rebound."),
  /** XQDY0044. */
  CAINV(XQDY, 44, "Invalid attribute prefix/namespace '%'."),
  /** XQDY0064. */
  CPIXML(XQDY, 64, "Processing instruction has illegal name: '%'."),
  /** XQDY0072. */
  COMINVALID(XQDY, 72, "Invalid comment."),
  /** XQDY0074. */
  INVNAME(XQDY, 74, "Invalid name: '%'."),
  /** XQDY0074. */
  INVPREF(XQDY, 74, "No namespace declared for %."),
  /** XQDY0095. */
  XGRP(XQDY, 95, "No sequence allowed as grouping variable."),
  /** XQDY0096. */
  CEXML(XQDY, 96, "XML prefix and namespace cannot be rebound."),
  /** XQDY0096. */
  CEINV(XQDY, 96, "Invalid element prefix/namespace '%'."),
  /** XQDY0101. */
  CNXML(XQDY, 101, "XML prefix and namespace cannot be rebound."),
  /** XQDY0101. */
  CNINV(XQDY, 101, "Invalid prefix/namespace '%'."),
  /** XQDY0102. */
  DUPLNSCONS(XQDY, 102, "Duplicate namespace declaration: '%'."),

  /** XQST0009. */
  IMPLSCHEMA(XQST, 9, "Schema import not supported."),
  /** XQST0022. */
  NSCONS(XQST, 22, "Constant namespace value expected."),
  /** XQST0031. */
  XQUERYVER(XQST, 31, "XQuery version '%' not supported."),
  /** XQST0032. */
  DUPLBASE(XQST, 32, "Duplicate 'base-uri' declaration."),
  /** XQST0033. */
  DUPLNSDECL(XQST, 33, "Duplicate declaration of prefix '%'."),
  /** XQST0034. */
  FUNCDEFINED(XQST, 34, "Duplicate declaration of function %(...)."),
  /** XQST0038. */
  DUPLCOLL(XQST, 38, "Duplicate 'collation' declaration."),
  /** XQST0076. */
  COLLWHICH(XQST, 38, "Unknown collation '%'."),
  /** XQST0039. */
  FUNCDUPL(XQST, 39, "Duplicate function argument %."),
  /** XQST0040. */
  ATTDUPL(XQST, 40, "Duplicate attribute '%'."),
  /** XQST0045. */
  NAMERES(XQST, 45, "Function %(...) uses reserved namespace."),
  /** XQST0045. */
  ANNRES(XQST, 45, "Annotation % uses reserved namespace."),
  /** XQST0047. */
  DUPLMODULE(XQST, 47, "Module is declared twice: '%'."),
  /** XQST0047. */
  MODNS(XQST, 48, "Declaration % does not match the module namespace."),
  /** XQST0049. */
  VARDEFINE(XQST, 49, "Duplicate declaration of %."),
  /** XQST0054. */
  XPSTACK(XQST, 54, "Stack Overflow: circular variable declaration?"),
  /** XQST0055. */
  DUPLCOPYNS(XQST, 55, "Duplicate 'copy-namespace' declaration."),
  /** XQST0057. */
  NSEMPTY(XQST, 57, "Namespace URI cannot be empty."),
  /** XQST0059. */
  NOINST(XQST, 59, "Could not instantiate module '%'."),
  /** XQST0059. */
  NOMODULE(XQST, 59, "Module \"%\" not found."),
  /** XQST0059. */
  MODINIT(XQST, 59, "Module '%' not initialized."),
  /** XQST0059. */
  NOMODULEFILE(XQST, 59, "Could not retrieve module '%'."),
  /** XQST0059. */
  WRONGMODULE(XQST, 59, "Wrong URI '%' in imported module '%'."),
  /** XQST0060. */
  FUNNONS(XQST, 60, "Namespace needed for function %(...)."),
  /** XQST0065. */
  DUPLORD(XQST, 65, "Duplicate 'ordering' declaration."),
  /** XQST0066. */
  DUPLNS(XQST, 66, "Duplicate 'default namespace' declaration."),
  /** XQST0067. */
  DUPLCONS(XQST, 67, "Duplicate 'construction' declaration."),
  /** XQST0068. */
  DUPLBOUND(XQST, 68, "Duplicate 'boundary-space' declaration."),
  /** XQST0069. */
  DUPLORDEMP(XQST, 69, "Duplicate 'order empty' declaration."),
  /** XQST0070. */
  BINDXML(XQST, 70, "Prefix '%' cannot be rebound."),
  /** XQST0070. */
  BINDXMLURI(XQST, 70, "'%' can only be bound to '%'."),
  /** XQST0071. */
  DUPLNSDEF(XQST, 71, "Duplicate declaration of prefix '%'."),
  /** XQST0075. */
  IMPLVAL(XQST, 75, "Validation not supported."),
  /** XQST0076. */
  WHICHCOLL(XQST, 76, "Unknown collation '%'."),
  /** XQST0079. */
  NOPRAGMA(XQST, 79, "Expecting pragma expression."),
  /** XQST0085. */
  NSEMPTYURI(XQST, 85, "Namespace URI cannot be empty."),
  /** XQST0087. */
  XQUERYENC2(XQST, 87, "Unknown encoding '%'."),
  /** XQST0088. */
  NSMODURI(XQST, 88, "Module namespace cannot be empty."),
  /** XQST0089. */
  DUPLVAR(XQST, 89, "Duplicate declaration of %."),
  /** XQST0090. */
  INVCHARREF(XQST, 90, "Invalid character reference '%'."),
  /** XQST0093. */
  CIRCMODULE(XQST, 93, "Circular module declaration."),
  /** XPST0094. */
  GVARNOTDEFINED(XQST, 94, "Undeclared grouping variable '%'."),
  /** XPST0097. */
  INVDECFORM(XQST, 97, "Invalid decimal-format property: %='%'."),
  /** XPST0098. */
  DUPLDECFORM(XQST, 98, "Duplicate use of decimal-format '%'."),
  /** XQST0099. */
  DUPLITEM(XQST, 99, "Duplicate declaration of context item."),
  /** XQST0106. */
  DUPLUPD(XQST, 106, "More than one updating annotation declared."),
  /** XQST0106. */
  DUPLVIS(XQST, 106, "More than one visibility annotation declared."),
  /** XQST0107. */
  CTXINIT(XQST, 107, "Context item depends on itself."),
  /** XQST0108. */
  MODOUT(XQST, 108, "No output declarations allowed in modules."),
  /** XPST0109. */
  OUTWHICH(XQST, 109, "Unknown serialization parameter: '%'."),
  /** XPST0110. */
  OUTDUPL(XQST, 110, "Duplicate declaration of 'output:%'."),
  /** XPST0111. */
  DECDUPL(XQST, 111, "Duplicate decimal-format declaration."),
  /** XQST0113. */
  DECITEM(XQST, 113, "Context item cannot be specified in module."),
  /** XPST0111. */
  DECDUPLPROP(XQST, 114, "Duplicate decimal-format property '%'."),

  /** XQTY0024. */
  NOATTALL(XQTY, 24, "Attribute must follow the root element."),
  /** XQTY0024. */
  NONSALL(XQTY, 24, "Namespaces must follow the root element."),
  /** XQTY0105. */
  CONSFUNC(XQTY, 105, "Invalid content: %."),

  /** FOFD1340. */
  WRONGINT(FOFD, 1340, "Wrong integer format: '%'."),
  /** FOFD1340. */
  OPTAFTER(FOFD, 1340, "Optional digit follows mandatory digits: '%'."),
  /** FOFD1340. */
  DIFFMAND(FOFD, 1340, "Mandatory digits must be of the same group: '%'."),
  /** FOFD1340. */
  GROUPADJ(FOFD, 1340, "Adjacent grouping separators: '%'."),
  /** FOFD1340. */
  GROUPSTART(FOFD, 1340, "Picture begins with grouping separator: '%'."),
  /** FOFD1340. */
  GROUPEND(FOFD, 1340, "Picture ends with grouping separator: '%'."),
  /** FOFD1340. */
  NOMAND(FOFD, 1340, "No mandatory digit specified: '%'."),
  /** FOFD1340. */
  PICDATE(FOFD, 1340, "Invalid picture string: '%'."),
  /** FOFD1340. */
  ORDCLOSED(FOFD, 1340, "Ordinal is not closed: '%'."),
  /** FOFD1350. */
  PICCOMP(FOFD, 1350, "Invalid component in string: '%'."),
  /** FOUT1170. */
  RESNF(FOUT, 1170, "Resource '%' cannot be retrieved."),
  /** FOUT1170. */
  FRAGID(FOUT, 1170, "URI contains a fragment identifier: %"),
  /** FOUT1170. */
  INVURL(FOUT, 1170, "URI is invalid: %"),
  /** FOUT1190. */
  WHICHENC(FOUT, 1190, "Encoding '%' is not supported."),
  /** FOUT1190. */
  INVCHARS(FOUT, 1190, "%."),
  /** FOUT1200. */
  WHICHCHARS(FOUT, 1200, "Resource contains invalid input."),

  /** XUDY0009. */
  UPNOPAR(XUDY, 9, "Target % has no parent."),
  /** XUDY0014. */
  UPNOTCOPIED(XUDY, 14, "% was not created by copy clause."),
  /** XUDY0015. */
  UPMULTREN(XUDY, 15, "Node can only be renamed once: %."),
  /** XUDY0015. */
  UPPATHREN(XUDY, 15, "Path can only be renamed once: '%'."),
  /** XUDY0016. */
  UPMULTREPL(XUDY, 16, "Node can only be replaced once: %."),
  /** XUDY0017. */
  UPMULTREPV(XUDY, 17, "Node can only be replaced once: %"),
  /** XUDY0021. */
  UPATTDUPL(XUDY, 21, "Duplicate attribute %."),
  /** XUDY0023. */
  UPNSCONFL(XUDY, 23, "Conflicts with existing namespaces."),
  /** XUDY0024. */
  UPNSCONFL2(XUDY, 24, "New namespaces conflict with each other."),
  /** XUDY0027. */
  UPSEQEMP(XUDY, 27, "% target must not be empty."),
  /** XUDY0029. */
  UPPAREMPTY(XUDY, 29, "Target has no parent node."),
  /** XUDY0030. */
  UPATTELM(XUDY, 30, "Attributes cannot be inserted as child of a document."),
  /** XUDY0031. */
  UPURIDUP(XUDY, 31, "URI '%' is addressed multiple times."),

  /** XUST0001. */
  UPNOT(XUST, 1, "%: no updating expression allowed."),
  /** XUST0001. */
  UPCTX(XUST, 1, "Context item may not declare an updating expression."),
  /** XUST0002. */
  UPEXPECTT(XUST, 2, "Updating expression expected in modify clause."),
  /** XUST0002. */
  UPEXPECTF(XUST, 2, "Updating expression expected in function declaration."),
  /** XUST0003. */
  DUPLREVAL(XUST, 3, "Duplicate 'revalidation' declaration."),
  /** XUST0026. */
  NOREVAL(XUST, 26, "Revalidation mode not supported."),
  /** XUST0028. */
  UPFUNCTYPE(XUST, 28, "No return type allowed in updating functions."),

  /** XUTY0004. */
  UPNOATTRPER(XUTY, 4, "Attribute must follow the root element."),
  /** XUTY0005. */
  UPTRGTYP(XUTY, 5, "Single element or document expected as insert target."),
  /** XUTY0006. */
  UPTRGTYP2(XUTY, 6, "Single element, text, comment or pi expected as insert target."),
  /** XUTY0007. */
  UPTRGDELEMPT(XUTY, 7, "Only nodes can be deleted."),
  /** XUTY0008. */
  UPTRGMULT(XUTY, 8, "Single element, text, attribute, comment or pi expected"
      + " as replace target."),
  /** XUTY0010. */
  UPWRELM(XUTY, 10, "Replacing nodes must be no attribute nodes."),
  /** XUTY0011. */
  UPWRATTR(XUTY, 11, "Replacing nodes must be attribute nodes."),
  /** XUTY0012. */
  UPWRTRGTYP(XUTY, 12, "Single element, attribute or pi expected as rename target."),
  /** XUTY0013. */
  UPCOPYMULT(XUTY, 13, "Source expression in copy clause must return a single node."),
  /** XUTY0022. */
  UPATTELM2(XUTY, 22, "Insert target must be an element.");

  /** Error type. */
  public final ErrType type;
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
  Err(final ErrType t, final int n, final String d) {
    type = t;
    num = n;
    desc = d;
  }

  /**
   * Throws a query exception.
   * @param ii input info
   * @param ext extended info
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  public QueryException thrw(final InputInfo ii, final Object... ext)
      throws QueryException {
    throw new QueryException(ii, this, ext);
  }

  /**
   * Throws a serializer exception. Might be merged with {@link #thrw} in
   * future.
   * @param ext extended info
   * @return serializer exception (indicates that an error is raised)
   * @throws SerializerException serializer exception
   */
  public SerializerException thrwSerial(final Object... ext) throws SerializerException {
    throw new SerializerException(new QueryException(null, this, ext));
  }

  /**
   * Error types.
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  public enum ErrType {
    // Project errors

    /** BASX Error type. */ BASX(QueryText.BXERR, QueryText.BXERRORS),

    /** BXCL Error type. */ BXCL(QueryText.BXERR, QueryText.BXERRORS),
    /** BXCO Error type. */ BXCO(QueryText.BXERR, QueryText.BXERRORS),
    /** BXDB Error type. */ BXDB(QueryText.BXERR, QueryText.BXERRORS),
    /** BXFT Error type. */ BXFT(QueryText.BXERR, QueryText.BXERRORS),
    /** BXJS Error type. */ BXJS(QueryText.BXERR, QueryText.BXERRORS),
    /** BXPR Error type. */ BXPR(QueryText.BXERR, QueryText.BXERRORS),
    /** BXRE Error type. */ BXRE(QueryText.BXERR, QueryText.BXERRORS),
    /** BXSQ Error type. */ BXSQ(QueryText.BXERR, QueryText.BXERRORS),
    /** BXVA Error type. */ BXVA(QueryText.BXERR, QueryText.BXERRORS),
    /** HASH Error type. */ HASH(QueryText.BXERR, QueryText.BXERRORS),

    // EXPath errors

    /** CX Error type.   */ CX(QueryText.EXPERR, QueryText.EXPERROR),
    /** FILE Error type. */ FILE(QueryText.EXPERR, QueryText.EXPERROR),
    /** HC Error type.   */ HC(QueryText.EXPERR, QueryText.EXPERROR),
    /** ZIP Error type. */  ZIP(QueryText.EXPERR, QueryText.EXPERROR),
    /** ARCH Error type. */ ARCH(QueryText.EXPERR, QueryText.EXPERROR),

    // W3 errors

    /** FOAR Error type. */ FOAR,
    /** FOCA Error type. */ FOCA,
    /** FOCH Error type. */ FOCH,
    /** FODC Error type. */ FODC,
    /** FODF Error type. */ FODF,
    /** FODT Error type. */ FODT,
    /** FOFD Error type. */ FOFD,
    /** FOER Error type. */ FOER,
    /** FONS Error type. */ FONS,
    /** FORG Error type. */ FORG,
    /** FORX Error type. */ FORX,
    /** FOTY Error type. */ FOTY,
    /** FOUP Error type. */ FOUP,
    /** FOFD Error type. */ FOUT,
    /** FTDY Error type. */ FTDY,
    /** FTST Error type. */ FTST,
    /** SEPM Error type. */ SEPM,
    /** SERE Error type. */ SERE,
    /** SEPM Error type. */ SESU,
    /** XPDY Error type. */ XPDY,
    /** XPST Error type. */ XPST,
    /** XPTY Error type. */ XPTY,
    /** XQDY Error type. */ XQDY,
    /** XQST Error type. */ XQST,
    /** XQTY Error type. */ XQTY,
    /** XUDY Error type. */ XUDY,
    /** XUST Error type. */ XUST,
    /** XUTY Error type. */ XUTY;

    /** This error type's prefix. */
    public final String prefix;
    /** This error type's URI. */
    public final byte[] uri;

    /**
     * Constructor for non-standard errors.
     * @param pref QName prefix
     * @param euri error URI
     */
    ErrType(final byte[] pref, final byte[] euri) {
      prefix = Token.string(pref);
      uri = euri;
    }

    /**
     * Constructor for standard XQuery errors. The prefix is {@code err}, the URI is
     * {@code http://www.w3.org/2005/xqt-errors}.
     */
    ErrType() {
      this(QueryText.ERR, QueryText.ERRORURI);
    }

    /**
     * Creates a QName for the given error number.
     * @param num error number
     * @return constructed QName
     */
    public final QNm qname(final int num) {
      return new QNm(String.format("%s:%s%04d", prefix, name(), num), uri);
    }
  }

  /**
   * Returns the namespace URI of this error.
   * @return function
   */
  public final QNm qname() {
    return type.qname(num);
  }

  /**
   * Throws a comparison exception.
   * @param ii input info
   * @param it1 first item
   * @param it2 second item
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  public static QueryException diff(final InputInfo ii, final Item it1, final Item it2)
      throws QueryException {
    throw (it1 == it2 ? TYPECMP : XPTYPECMP).thrw(ii, it1.type, it2.type);
  }

  /**
   * Throws a type cast exception.
   * @param ii input info
   * @param t expression cast type
   * @param v value
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  public static QueryException cast(final InputInfo ii, final Type t, final Value v)
      throws QueryException {
    throw XPINVCAST.thrw(ii, v.type, t, v);
  }

  /**
   * Throws a type promoting exception.
   * @param ii input info
   * @param t expression cast type
   * @param v value
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  public static QueryException promote(final InputInfo ii, final SeqType t, final Value v)
      throws QueryException {
    throw XPINVPROM.thrw(ii, v.description(), t, v);
  }

  /**
   * Throws a type exception.
   * @param e parsing expression
   * @param t expected type
   * @param it found item
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  public static QueryException type(final ParseExpr e, final Type t, final Item it)
      throws QueryException {
    throw XPTYPE.thrw(e.info, e.description(), t, it.type);
  }

  /**
   * Throws a number exception.
   * @param e parsing expression
   * @param it found item
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  public static QueryException number(final ParseExpr e, final Item it)
      throws QueryException {
    throw XPTYPENUM.thrw(e.info, e.description(), it.type);
  }

  /**
   * Throws an invalid value exception.
   * @param ii input info
   * @param t expected type
   * @param v value
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  public static QueryException value(final InputInfo ii, final Type t, final Object v)
      throws QueryException {
    throw INVALUE.thrw(ii, t, v);
  }

  @Override
  public String toString() {
    return String.format("%s%04d", type, num);
  }
}
