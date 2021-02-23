package org.basex.query;

import static org.basex.query.QueryError.ErrType.*;
import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class contains all query error messages.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum QueryError {

  // BaseX errors

  /** Error code. */
  BASEX_ANNOTATION1_X_X(BASEX, "annotation", "Annotation %% is unknown."),
  /** Error code. */
  BASEX_ANNOTATION2_X_X(BASEX, "annotation", "%: % supplied."),
  /** Error code. */
  BASEX_ANNOTATION3_X_X(BASEX, "annotation", "Annotation %% is declared twice."),
  /** Error code. */
  BASEX_ANNOTATION_X_X_X(BASEX, "annotation", "%: % expected, % found."),
  /** Error code. */
  BASEX_DBPATH1_X(BASEX, "doc", "Database path '%' yields no documents."),
  /** Error code. */
  BASEX_DBPATH2_X(BASEX, "doc", "Database path '%' yields more than one document."),
  /** Error code. */
  BASEX_FUNCTION_X(BASEX, "function", "Function items cannot be cached: %."),
  /** Error code. */
  BASEX_ERROR_X(BASEX, "error", "%"),
  /** Error code. */
  BASEX_HTTP(BASEX, "http", "HTTP connection required."),
  /** Error code. */
  BASEX_OPTIONS1_X(BASEX, "options", "Unknown database option: %."),
  /** Error code. */
  BASEX_OPTIONS2_X(BASEX, "options", "%"),
  /** Error code. */
  BASEX_OPTIONS3_X(BASEX, "options", "Database option not allowed in library module: %."),
  /** Error code. */
  BASEX_OPTIONS_X_X(BASEX, "options", "Database option '%' cannot be set to '%'."),
  /** Error code. */
  BASEX_OVERFLOW(BASEX, "overflow", "Stack Overflow: Try tail recursion?"),
  /** Error code. */
  BASEX_PERMISSION_X(BASEX, "permission", "No % permission."),
  /** Error code. */
  BASEX_PERMISSION_X_X(BASEX, "permission", "No % permission: %."),
  /** Error code. */
  BASEX_RESTXQ_X(BASEX, "restxq", "%"),
  /** Error code. */
  BASEX_WS_X(BASEX, "ws", "%"),
  /** Error code. */
  BASEX_UPDATE(BASEX, "update", "Update target was not created by transform expression."),
  /** Error code. */
  BASEX_WS(BASEX, "ws", "WebSocket connection required."),

  // Admin Module

  /** Error code. */
  ADMIN_DELETE_X(ADMIN, "delete", "Log file could not be deleted: %."),
  /** Error code. */
  ADMIN_TODAY(ADMIN, "today", "Today's log file cannot be deleted."),
  /** Error code. */
  ADMIN_TYPE_X(ADMIN, "type", "Type string contains whitespaces: '%'."),

  // Archive Module

  /** Error code. */
  ARCHIVE_DESCRIPTOR1(ARCHIVE, "descriptor", "Name of ZIP entry must not be empty."),
  /** Error code. */
  ARCHIVE_DESCRIPTOR2_X(ARCHIVE, "descriptor", "Invalid compression level: '%'."),
  /** Error code. */
  ARCHIVE_DESCRIPTOR3_X(ARCHIVE, "descriptor", "xs:dateTime value is invalid : '%'."),
  /** Error code. */
  ARCHIVE_ENCODE1_X(ARCHIVE, "encode", "Unknown encoding '%'."),
  /** Error code. */
  ARCHIVE_ENCODE2_X(ARCHIVE, "encode", "String conversion: %."),
  /** Error code. */
  ARCHIVE_ERROR_X(ARCHIVE, "error", "Operation failed: %."),
  /** Error code. */
  ARCHIVE_FORMAT(ARCHIVE, "format", "Packing format not supported."),
  /** Error code. */
  ARCHIVE_FORMAT_X_X(ARCHIVE, "format", "% not supported: '%'."),
  /** Error code. */
  ARCHIVE_MODIFY_X(ARCHIVE, "modify", "Entries of % archive cannot be modified."),
  /** Error code. */
  ARCHIVE_NUMBER_X_X(ARCHIVE, "number", "Number of entries and contents differs: % vs. %."),
  /** Error code. */
  ARCHIVE_SINGLE_X(ARCHIVE, "single", "% archives are limited to a single entry."),

  // Binary Module

  /** Error code. */
  BIN_DLA_X_X(BIN, "differing-length-arguments", "Inputs are of different length (%/%)."),
  /** Error code. */
  BIN_IOOR_X_X(BIN, "index-out-of-range", "Index '%' is out of range (0-%)."),
  /** Error code. */
  BIN_NS_X(BIN, "negative-size", "Size '%' is negative."),
  /** Error code. */
  BIN_OOR_X(BIN, "octet-out-of-range", "Octet '%' is out of range."),
  /** Error code. */
  BIN_NNC(BIN, "non-numeric-character", "Invalid character in constructor string."),
  /** Error code. */
  BIN_UE_X(BIN, "unknown-encoding", "Unknown encoding '%'."),
  /** Error code. */
  BIN_CE_X(BIN, "conversion-error", "%."),
  /** Error code. */
  BIN_USO_X(BIN, "unknown-significance-order", "Unknown octet-order value: '%'."),

  // Client Module

  /** Error code. */
  CLIENT_COMMAND_X(CLIENT, "command", "Command could not be executed: %"),
  /** Error code. */
  CLIENT_CONNECT_X(CLIENT, "connect", "Connection failed: %"),
  /** Error code. */
  CLIENT_ERROR_X(CLIENT, "error", "An error occurred: %"),
  /** Error code. */
  CLIENT_FITEM_X(CLIENT, "function", "Result is a function item: %."),
  /** Error code. */
  CLIENT_ID_X(CLIENT, "id", "Session with ID % is not available or has been closed."),
  /** Error code. */
  CLIENT_QUERY_X(CLIENT, "query", "Query could not be executed: %"),

  // Conversion Module

  /** Error code. */
  CONVERT_STRING_X(CONVERT, "string", "String conversion: %."),
  /** Error code. */
  CONVERT_BINARY_X_X(CONVERT, "binary", "Conversion of '%' to encoding '%' failed."),
  /** Error code. */
  CONVERT_ENCODING_X(CONVERT, "encoding", "Unknown encoding '%'."),
  /** Error code. */
  CONVERT_BASE_X(CONVERT, "base", "Unsupported base: %."),
  /** Error code. */
  CONVERT_INTEGER_X_X(CONVERT, "integer", "Invalid digit for base %: %."),
  /** Error code. */
  CONVERT_KEY_X(CONVERT, "key", "Key could not be decoded: %."),

  // Cryptographic Module

  /** Error code. */
  CX_CANINV(CX, 1, "Canonicalization algorithm is not supported."),
  /** Error code. */
  CX_DIGINV(CX, 2, "Digest algorithm is not supported."),
  /** Error code. */
  CX_SIGINV(CX, 3, "Signature algorithm is not supported."),
  /** Error code. */
  CX_XPINV(CX, 4, "XPath expression is invalid."),
  /** Error code. */
  CX_INVNM(CX, 5, "Invalid name for $digital-certificate root."),
  /** Error code. */
  CX_KSNULL_X(CX, 7, "Key store is null: %"),
  /** Error code. */
  CX_NOKEY(CX, 12, "Cannot find key for alias in given keystore."),
  /** Error code. */
  CX_INVHASH_X(CX, 13, "Hashing algorithm is not supported: %."),
  /** Error code. */
  CX_ENC_X(CX, 14, "The encoding method is not supported: %."),
  /** Error code. */
  CX_NOSIG(CX, 15, "Cannot find signature element."),
  /** Error code. */
  CX_NOPAD_X(CX, 16, "No such padding: %."),
  /** Error code. */
  CX_BADPAD_X(CX, 17, "Incorrect padding: %."),
  /** Error code. */
  CX_ENCTYP_X(CX, 18, "Encryption type is not supported: %."),
  /** Error code. */
  CX_KEYINV_X(CX, 19, "Secret key is invalid: %."),
  /** Error code. */
  CX_ILLBLO_X(CX, 20, "Illegal block size: %."),
  /** Error code. */
  CX_INVALGO_X(CX, 21, "Algorithm is not supported: %."),
  /** Error code. */
  CX_ALINV_X(CX, 23, "Invalid certificate alias %."),
  /** Error code. */
  CX_ALGEXC(CX, 24, "Invalid algorithm."),
  /** Error code. */
  CX_IOEXC(CX, 25, "IO Exception."),
  /** Error code. */
  CX_KSEXC(CX, 26, "Keystore exception."),
  /** Error code. */
  CX_SIGEXC(CX, 27, "Signature exception."),
  /** Error code. */
  CX_SIGTYPINV(CX, 28, "Signature type is not supported."),

  // CSV Module

  /** Error code. */
  CSV_PARSE_X(CSV, "parse", "%"),
  /** Error code. */
  CSV_SERIALIZE_X(CSV, "serialize", "%."),

  // Database Module

  /** Error code. */
  DB_ARGS_X_X(DB, "args", "Number of specified inputs and paths differs: % vs. %."),
  /** Error code. */
  DB_CONFLICT1_X_X(DB, "conflict", "Database '%' can only be % once."),
  /** Error code. */
  DB_CONFLICT2_X_X(DB, "conflict", "Backup '%' can only be % once."),
  /** Error code. */
  DB_CONFLICT3_X(DB, "conflict", "Database '%' cannot be both altered and dropped."),
  /** Error code. */
  DB_CONFLICT4_X(DB, "conflict", "Name of source and target is equal: %."),
  /** Error code. */
  DB_LOCK1_X(DB, "lock", "Database '%' cannot be updated, it is opened by another process."),
  /** Error code. */
  DB_LOCK2_X(DB, "lock", "%"),
  /** Error code. */
  DB_MAINMEM_X(DB, "mainmem", "Database '%' is in main memory."),
  /** Error code. */
  DB_NAME_X(DB, "name", "Invalid database name: %."),
  /** Error code. */
  DB_NOBACKUP_X(DB, "no-backup", "No backup exists for database '%'."),
  /** Error code. */
  DB_NODE_X(DB, "node", "No database node: %."),
  /** Error code. */
  DB_NOINDEX_X_X(DB, "no-index", "Database '%' has no % index."),
  /** Error code. */
  DB_OPEN1_X(DB, "open", "Database '%' not found."),
  /** Error code. */
  DB_OPEN2_X(DB, "open", "%"),
  /** Error code. */
  DB_OPTION_X(DB, "option", "Unknown option: %."),
  /** Error code. */
  DB_PATH_X(DB, "path", "Invalid path: %."),
  /** Error code. */
  DB_PROPERTY_X(DB, "property", "Unknown database property: %."),
  /** Error code. */
  DB_RANGE_X_X(DB, "range", "Database '%' value out of range: %."),
  /** Error code. */
  DB_TARGET_X(DB, "target", "Invalid target path: %."),

  // Fetch Module

  /** Error code. */
  FETCH_OPEN_X(FETCH, "open", "%"),
  /** Error code. */
  FETCH_ENCODING_X(FETCH, "encoding", "Unknown encoding '%'."),

  // File Module

  /** Error code. */
  FILE_NOT_FOUND_X(FILE, "not-found", "'%' does not exist."),
  /** Error code. */
  FILE_EXISTS_X(FILE, "exists", "'%' already exists."),
  /** Error code. */
  FILE_NO_DIR_X(FILE, "no-dir", "'%' is no directory."),
  /** Error code. */
  FILE_IS_DIR_X(FILE, "is-dir", "'%' is a directory."),
  /** Error code. */
  FILE_ID_DIR2_X(FILE, "is-dir", "'%' is a non-empty directory."),
  /** Error code. */
  FILE_IS_RELATIVE_X(FILE, "is-relative", "Base directory is relative: '%'."),
  /** Error code. */
  FILE_UNKNOWN_ENCODING_X(FILE, "unknown-encoding", "Unknown encoding '%'."),
  /** Error code. */
  FILE_OUT_OF_RANGE_X_X(FILE, "out-of-range", "Requested file chunk [%,%] exceeds file bounds."),
  /** Error code. */
  FILE_INVALID_PATH_X(FILE, "invalid-path", "Invalid file path: '%'."),
  /** Error code. */
  FILE_IO_ERROR_X(FILE, "io-error", "%"),
  /** Error code. */
  FILE_IE_ERROR_ACCESS_X(FILE, "io-error", "Access to '%' is denied."),

  // Fulltext Module

  /** Error code. */
  FT_OPTIONS(FT, "options", "Wildcards and fuzzy option cannot be specified both."),

  // Geo Module

  /** Error code. */
  GEO_WHICH(GEO, 1, "Unrecognized geometry type: %."),
  /** Error code. */
  GEO_READ(GEO, 2, "Parsing GML 2.0: %."),
  /** Error code. */
  GEO_TYPE(GEO, 3, "Wrong geometry: % expected, % found."),
  /** Error code. */
  GEO_RANGE(GEO, 4, "Out of range input index: %."),
  /** Error code. */
  GEO_WRITE(GEO, 5, "%."),
  /** Error code. */
  GEO_ARG(GEO, 6, "Illegal argument: %."),

  // Hashing Module

  /** Error code. */
  HASH_ALGORITHM_X(HASH, "algorithm", "Algorithm not supported: '%'."),

  // HTML Module

  /** Error code. */
  HTML_PARSE_X(HTML, "parse", "%"),

  // HTTP Module

  /** Error code. */
  HC_ERROR_X(HC, 1, "%"),
  /** Error code. */
  HC_PARSE_X(HC, 2, "Conversion failed: %"),
  /** Error code. */
  HC_ATTR(HC, 3, "No attribute allowed beside 'src' and 'media-type'."),
  /** Error code. */
  HC_REQ_X(HC, 4, "%."),
  /** Error code. */
  HC_URL(HC, 5, "No URL supplied."),
  /** Error code. */
  HC_PARAMS(HC, 6, "Specify request element or HTTP URI."),

  // Inspection Module

  /** Error code. */
  INSPECT_UNKNOWN_X(INSPECT, "unknown", "Component '%' does not exist."),

  // Jobs Module

  /** Error code. */
  JOBS_ID_EXISTS_X(JOBS, "id", "Job id already exists: %."),
  /** Error code. */
  JOBS_ID_INVALID_X(JOBS, "id", "Invalid job name: %."),
  /** Error code. */
  JOBS_OPTIONS(JOBS, "options", "Either 'cache' or 'interval' option is allowed."),
  /** Error code. */
  JOBS_OVERFLOW(JOBS, "overflow", "Too many queries queued."),
  /** Error code. */
  JOBS_RANGE_X(JOBS, "range", "Value out of range: %."),
  /** Error code. */
  JOBS_RUNNING_X(JOBS, "running", "Result is not available yet: %."),
  /** Error code. */
  JOBS_SELF_X(JOBS, "self", "Cannot wait for own job: %"),
  /** Error code. */
  JOBS_UNKNOWN_X(JOBS, "unknown", "Unknown job: %."),
  /** Error code. */
  JOBS_SERVICE(JOBS, "service", "No variables allowed."),
  /** Error code. */
  JOBS_SERVICE_X_X(JOBS, "service", "Could not write service: %."),

  // JSON Module

  /** Error code. */
  JSON_PARSE_X(JSON, "parse", "%"),
  /** Error code. */
  JSON_DUPL_X_X_X(JSON, "parse", "(%:%): %."),
  /** Error code. */
  JSON_PARSE_X_X_X(JSON, "parse", "(%:%): %."),
  /** Error code. */
  JSON_SERIALIZE_X(JSON, "serialize", "%."),
  /** Error code. */
  JSON_OPTIONS_X(JSON, "options", "'%':'%' is not supported by the target format."),

  // Output Module

  /** Error code. */
  OUTPUT_FORMAT_X_X(OUTPUT, "format", "%: %."),

  // Process Module

  /** Error code. */
  PROC_ENCODING_X(PROC, "encoding", "Unknown encoding '%'."),
  /** Error code. */
  PROC_ERROR_X(PROC, "error", "%"),
  /** Error code. */
  PROC_TIMEOUT(PROC, "timeout", "The timeout was exceeded."),

  // Profiling Module

  /** Error code. */
  PROF_OPTION_X(PROF, "option", "Unknown option: %."),

  // Random Module

  /** Error code. */
  RANDOM_BOUNDS_X(RANDOM, "bounds", "Maximum value is out of bounds: %."),
  /** Error code. */
  RANGE_NEGATIVE_X(RANDOM, "negative", "Number of values is negative: %."),

  // Repository Module

  /** Error code. */
  REPO_DELETE_X(REPO, "delete", "File '%' could not be deleted."),
  /** Error code. */
  REPO_DELETE_X_X(REPO, "delete", "Package '%' depends on package '%'."),
  /** Error code. */
  REPO_DESCRIPTOR_X(REPO, "descriptor", "%."),
  /** Error code. */
  REPO_INSTALLED_X(REPO, "installed", "Module % is already installed within another package."),
  /** Error code. */
  REPO_NOTFOUND_X(REPO, "not-found", "Package '%' not found."),
  /** Error code. */
  REPO_PARSE_X_X(REPO, "parse", "%: %."),
  /** Error code. */
  REPO_VERSION(REPO, "version", "Package version is not supported."),

  // Request Module

  /** Error code. */
  REQUEST_PARAMETER(REQUEST, "parameter", "Query string cannot be decoded: %."),
  /** Error code. */
  REQUEST_ATTRIBUTE_X(REQUEST, "attribute", "Attribute cannot be stored: %."),

  // Session Module

  /** Error code. */
  SESSION_SET_X(SESSION, "set", "Item cannot be stored: %."),
  /** Error code. */
  SESSION_NOTFOUND(SESSIONS, "not-found", "Session not available."),

  // Sessions Module

  /** Error code. */
  SESSIONS_SET_X(SESSIONS, "set", "Item cannot be stored: %."),
  /** Error code. */
  SESSIONS_NOTFOUND_X(SESSIONS, "not-found", "Session not available: %."),

  // SQL Module

  /** Error code. */
  SQL_ATTRIBUTE_X(SQL, "attribute", "Attribute not expected: %."),
  /** Error code. */
  SQL_INIT_X(SQL, "init", "Could not find driver: %"),
  /** Error code. */
  SQL_ERROR_X(SQL, "error", "An SQL exception occurred: %"),
  /** Error code. */
  SQL_TIMEOUT_X(SQL, "timeout", "A query timeout has occurred: %"),
  /** Error code. */
  SQL_ID1_X(SQL, "id", "No connection with id %."),
  /** Error code. */
  SQL_ID2_X(SQL, "id", "No prepared statement with id %."),
  /** Error code. */
  SQL_PARAMETERS(SQL, "parameters", "No parameter type supplied."),
  /** Error code. */
  SQL_TYPE_X_X(SQL, "type", "Invalid type (%): %."),

  // Unit Module

  /** Error code. */
  UNIT_FAIL(UNIT, "fail", "Assertion failed."),
  /** Error code. */
  UNIT_FAIL_X(UNIT, "fail", "%"),
  /** Error code. */
  UNIT_FAIL_X_X_X(UNIT, "fail", "Item %: % expected, % returned."),
  /** Error code. */
  UNIT_NOARGS_X(UNIT, "no-args", "Test function '%' must have no arguments."),
  /** Error code. */
  UNIT_PRIVATE_X(UNIT, "private", "Test function '%' must not be private."),

  // User Module

  /** Error code. */
  USER_ADMIN(USER, "admin", "User 'admin' cannot be modified."),
  /** Error code. */
  USER_CONFLICT_X(USER, "conflict", "User '%' cannot be both altered and dropped."),
  /** Error code. */
  USER_EQUAL_X(USER, "equal", "Name of old and new user is equal: %."),
  /** Error code. */
  USER_INFO_X(USER, "info", "Info can only be % once."),
  /** Error code. */
  USER_LOCAL(USER, "local", "Local permission can only be 'none', 'read' or 'write'."),
  /** Error code. */
  USER_LOGGEDIN_X(USER, "logged-in", "User '%' is currently logged in."),
  /** Error code. */
  USER_NAME_X(USER, "name", "Invalid user name: '%'."),
  /** Error code. */
  USER_PASSWORD_X(USER, "password", "Wrong password supplied for user '%'."),
  /** Error code. */
  USER_PATTERN_X(USER, "pattern", "Invalid database pattern: '%'."),
  /** Error code. */
  USER_PERMISSION_X(USER, "permission", "Invalid permission: '%'."),
  /** Error code. */
  USER_UNKNOWN_X(USER, "unknown", "User '%' does not exist."),
  /** Error code. */
  USER_UPDATE1_X_X(USER, "update", "User '%' can only be % once."),
  /** Error code. */
  USER_UPDATE2_X(USER, "update", "Pattern '%' is specified more than once."),
  /** Error code. */
  USER_UPDATE3_X_X(USER, "update", "User '%' can only be % once."),

  // Validation Module

  /** Error code. */
  VALIDATE_ERROR_X(ErrType.VALIDATE, "error", "Validation failed: %"),
  /** Error code. */
  VALIDATE_START_X(ErrType.VALIDATE, "init", "Validation could not be started: %"),
  /** Error code. */
  VALIDATE_NOTFOUND_X(ErrType.VALIDATE, "not-found", "RelaxNG validation is not available."),

  // Web Module

  /** Error code. */
  WEB_INVALID1_X(WEB, "invalid", "URL contains invalid characters: %"),
  /** Error code. */
  WEB_INVALID2_X(WEB, "invalid", "%."),
  /** Error code. */
  WEB_STATUS_X(WEB, "status", "Invalid status code: %"),

  // WebSocket Module

  /** Error code. */
  WS_SET_X(WS, "set", "Function items cannot be stored: %."),
  /** Error code. */
  WS_NOTFOUND_X(WS, "not-found", "Unknown WebSocket: %."),

  // XQuery Module

  /** Error code. */
  XQUERY_UPDATE1(ErrType.XQUERY, "update", "No updating expression allowed."),
  /** Error code. */
  XQUERY_UPDATE2(ErrType.XQUERY, "update", "Updating expression expected."),
  /** Error code. */
  XQUERY_PERMISSION1_X(ErrType.XQUERY, "permission", "%"),
  /** Error code. */
  XQUERY_PERMISSION2_X(ErrType.XQUERY, "permission", "% permission required."),
  /** Error code. */
  XQUERY_TIMEOUT(ErrType.XQUERY, "timeout", "The timeout was exceeded."),
  /** Error code. */
  XQUERY_MEMORY(ErrType.XQUERY, "memory", "The memory limit was exceeded."),
  /** Error code. */
  XQUERY_NESTED(ErrType.XQUERY, "nested", "Nested query evaluation is not allowed."),
  /** Error code. */
  XQUERY_UNEXPECTED_X(ErrType.XQUERY, "error", "Unexpected error: %"),

  // XSLT Module

  /** Error code. */
  XSLT_ERROR_X(XSLT, "error", "%"),

  // ZIP Module

  /** Error code. */
  ZIP_NOTFOUND_X(ZIP, 1, "Path '%' not found."),
  /** Error code. */
  ZIP_INVALID_X_X(ZIP, 2, "% element: attribute '%' expected."),
  /** Error code. */
  ZIP_UNKNOWN_X(ZIP, 2, "ZIP definition: unknown element %."),
  /** Error code. */
  ZIP_FAIL_X(ZIP, 3, "Operation failed: %."),

  // W3 Functions

  /** Error code. */
  APPLY_X_X(FOAP, 1, "Arity differs from number of array members: % vs. %"),

  /** Error code. */
  DIVZERO_X(FOAR, 1, "% cannot be divided by zero."),
  /** Error code. */
  DIVFLOW_X(FOAR, 2, "Invalid division result: %."),
  /** Error code. */
  RANGE_X(FOAR, 2, "Value out of range: %."),

  /** Error code. */
  ARRAYBOUNDS_X_X(FOAY, 1, "Array index % out of bounds (1..%)."),
  /** Error code. */
  ARRAYEMPTY(FOAY, 1, "Array has no entries."),
  /** Error code. */
  ARRAYNEG_X(FOAY, 2, "Length is negative: %."),

  /** Error code. */
  INVALUE_X_X(FOCA, 2, "Cannot convert to %: %."),
  /** Error code. */
  INTRANGE_X(FOCA, 3, "Integer value out of range: %."),
  /** Error code. */
  DATECALC_X_X(FOCA, 5, "Invalid % calculation: %."),

  /** Error code. */
  INVCODE_X(FOCH, 1, "Invalid XML character '&#x%;'."),
  /** Error code. */
  WHICHCOLL_X(FOCH, 2, "%."),
  /** Error code. */
  NORMUNI_X(FOCH, 3, "Unsupported normalization form ('%')."),
  /** Error code. */
  CHARCOLL(FOCH, 4, "Collation does not operate on character-by-character basis."),

  /** Error code. */
  IDDOC(FODC, 1, "Specified node has no document node as root."),
  /** Error code. */
  NODEERR_X_X(FODC, 2, "% could not be created: %."),
  /** Error code. */
  NODEFCOLL(FODC, 2, "No default collection available."),
  /** Error code. */
  IOERR_X(FODC, 2, "%"),
  /** Error code. */
  WHICHRES_X(FODC, 2, "Resource '%' does not exist."),
  /** Error code. */
  RESDIR_X(FODC, 2, "URI '%' points to directory."),
  /** Error code. */
  INVCOLL_X(FODC, 4, "Invalid collection URI: '%'."),
  /** Error code. */
  INVDOC_X(FODC, 5, "Invalid document URI: '%'."),
  /** Error code. */
  SAXERR_X(FODC, 6, "SAX: %"),
  /** Error code. */
  RESINV_X(FODC, 7, "Resource path '%' is invalid."),
  /** Error code. */
  INVDB_X(FODC, 7, "Invalid database name: '%'."),

  /** Error code. */
  FORMNUM_X(FODF, 1280, "Unknown decimal format: '%'."),
  /** Error code. */
  PICEMPTY(FODF, 1310, "The picture string must not be empty: '%'"),
  /** Error code. */
  PICNUM_X(FODF, 1310, "Invalid picture string: '%'."),
  /** Error code. */
  OPTAFTER_X(FODF, 1310, "Optional digit sign follows mandatory digit signs: '%'."),
  /** Error code. */
  INVGROUP_X(FODF, 1310, "Invalid position of grouping separator signs: '%'."),
  /** Error code. */
  DIFFMAND_X(FODF, 1310, "Mandatory digits is not of the same group: '%'."),
  /** Error code. */
  INVORDINAL_X(FODF, 1310, "Invalid specification of ordinal numbering: '%'."),
  /** Error code. */
  INVDDPATTERN_X(FODF, 1310, "Invalid decimal-digit-pattern: '%'."),

  /** Error code. */
  DATERANGE_X_X(FODT, 1, "%: '%' out of range."),
  /** Error code. */
  YEARRANGE_X(FODT, 1, "Year '%' out of range."),
  /** Error code. */
  SECRANGE_X(FODT, 1, "Seconds '%' out of range."),
  /** Error code. */
  DURRANGE_X_X(FODT, 2, "%: '%' out of range."),
  /** Error code. */
  MONTHRANGE_X(FODT, 2, "Months '%' out of range."),
  /** Error code. */
  SECDURRANGE_X(FODT, 2, "Seconds '%' out of range."),
  /** Error code. */
  DATEZERO_X_X(FODT, 2, "Invalid % calculation: %."),
  /** Error code. */
  INVALZONE_X(FODT, 3, "Timezone out of range (-14:00 to +14:00): %."),
  /** Error code. */
  ZONESEC_X(FODT, 3, "No seconds allowed in timezone: %."),

  /** Error code. */
  FUNERR1(FOER, 0, "Halted on error()."),

  /** Error code. */
  INVCOMPSPEC_X(FOFD, 1340, "Invalid variable marker: '[%]'."),
  /** Error code. */
  PICDATE_X(FOFD, 1340, "Invalid picture string: '%'."),
  /** Error code. */
  CALWHICH_X(FOFD, 1340, "Unknown calendar: '%'."),
  /** Error code. */
  INVFDPATTERN_X(FOFD, 1340, "%"),
  /** Error code. */
  PICINVCOMP_X_X(FOFD, 1350, "Component '[%]' not applicable to % values."),

  /** Error code. */
  PARSE_JSON_X(FOJS, 1, "%"),
  /** Error code. */
  DUPLICATE_JSON_X(FOJS, 3, "%"),
  /** Error code. */
  MERGE_DUPLICATE_X(FOJS, 3, "Key % occurs more than once."),
  /** Error code. */
  OPTION_JSON_X(FOJS, 5, "%"),
  /** Error code. */
  FUNC_JSON_OPT_X_X(FOJS, 5, "% expected, % found."),
  /** Error code. */
  INVALID_JSON_X(FOJS, 6, "%"),
  /** Error code. */
  ESCAPE_JSON_X(FOJS, 7, "Invalid escape sequence: %."),

  /** Error code. */
  NSDECL_X(FONS, 4, "No namespace declared for prefix '%'."),

  /** Error code. */
  INVALIDZONE_X(FORG, 1, "Invalid timezone: %."),
  /** Error code. */
  FUNCCAST_X_X(FORG, 1, "Cannot convert to %: %."),
  /** Error code. */
  FUNCCAST_X_X_X(FORG, 1, "Cannot convert % to %: %."),
  /** Error code. */
  DATEFORMAT_X_X_X(FORG, 1, "Wrong % format: '%' (try e.g. '%')."),
  /** Error code. */
  URIARG_X(FORG, 2, "Invalid URI: %."),
  /** Error code. */
  BASEURIARG_X(FORG, 2, "Invalid base URI: %."),

  /** Error code. */
  ZEROORONE(FORG, 3, "Zero or one item expected."),
  /** Error code. */
  ONEORMORE(FORG, 4, "One or more item expected."),
  /** Error code. */
  EXACTLYONE(FORG, 5, "Exactly one item expected."),

  /** Error code. */
  CMP_X(FORG, 6, "Type % is not comparable."),
  /** Error code. */
  CMP_X_X_X(FORG, 6, "% expected, % found: %."),
  /** Error code. */
  EBV_X(FORG, 6, "Effective boolean value not defined for %."),
  /** Error code. */
  EBV_X_X(FORG, 6, "Effective boolean value not defined for %: %."),
  /** Error code. */
  SUM_X_X(FORG, 6, "Argument type % is invalid: %."),

  /** Error code. */
  FUNZONE_X_X(FORG, 8, "% and % have different timezones."),

  /** Error code. */
  IETF_PARSE_X_X_X(FORG, 10, "Invalid input (% expected, '%' found): '%'."),
  /** Error code. */
  IETF_INV_X(FORG, 10, "Invalid input: '%'."),

  /** Error code. */
  REGMOD_X(FORX, 1, "Invalid regular flag: '%'."),
  /** Error code. */
  REGPAT_X(FORX, 2, "Invalid regular expression: %."),
  /** Error code. */
  REGROUP(FORX, 3, "Pattern matches empty string."),
  /** Error code. */
  FUNREPBS_X(FORX, 4, "Invalid backslash in replacement string: %."),
  /** Error code. */
  FUNREPDOL_X(FORX, 4, "Invalid dollar sign in replacement string: %."),

  /** Error code. */
  FIATOM_X(FOTY, 13, "Items of type % cannot be atomized."),
  /** Error code. */
  FISTRING_X(FOTY, 14, "Items of type % have no string representation."),
  /** Error code. */
  FICMP_X(FOTY, 15, "Type % is not comparable."),

  /** Error code. */
  UPFOTYPE_X(FOUP, 1, "Document or element expected, % found."),
  /** Error code. */
  UPDOCTYPE_X(FOUP, 1, "Document expected, % found."),
  /** Error code. */
  UPFOURI_X(FOUP, 2, "Invalid URI: %."),
  /** Error code. */
  UPPUTERR_X(FOUP, 2, "File '%' could not be written."),
  /** Error code. */
  UPDBPUT_X(FOUP, 2, "Resource '%' could not be written."),
  /** Error code. */
  UPDROPBACK_X_X(FOUP, 2, "Backup '%' could not be %."),
  /** Error code. */
  UPDBERROR_X_X(FOUP, 2, "Database '%' could not be %."),
  /** Error code. */
  UPDBERROR_X(FOUP, 2, "%"),

  /** Error code. */
  RESNF_X(FOUT, 1170, "Resource '%' cannot be retrieved."),
  /** Error code. */
  FRAGID_X(FOUT, 1170, "URI contains a fragment identifier: %"),
  /** Error code. */
  INVURL_X(FOUT, 1170, "URI is invalid: %"),
  /** Error code. */
  STBASEURI(FOUT, 1170, "Static Base URI is undefined."),
  /** Error code. */
  ENCODING_X(FOUT, 1190, "Unknown encoding '%'."),
  /** Error code. */
  INVCHARS_X(FOUT, 1190, "%."),
  /** Error code. */
  WHICHCHARS_X(FOUT, 1200, "%."),

  /** Error code. */
  FTWEIGHT_X(FTDY, 16, "Weight value out of range: %."),
  /** Error code. */
  FTMILD(FTDY, 17, "Invalid 'mild not' selection."),
  /** Error code. */
  FTWILDCARD_X(FTDY, 20, "Invalid wildcard syntax: '%'."),

  /** Error code. */
  FTIGNORE(FTST, 7, "Ignore option not supported."),
  /** Error code. */
  NOSTOPFILE_X(FTST, 8, "Stop word file not found: '%'."),
  /** Error code. */
  FTNOSTEM_X(FTST, 9, "No stemmer available for language '%'."),
  /** Error code. */
  FTNOTOK_X(FTST, 9, "No tokenizer available for language '%'."),
  /** Error code. */
  NOTHES_X(FTST, 18, "Thesaurus not found: '%'."),
  /** Error code. */
  FTDUP_X(FTST, 19, "Match option '%' is declared twice."),

  /** Error code. */
  SERATTR_X(SENR, 1, "Attributes cannot be serialized:%."),
  /** Error code. */
  SERNS_X(SENR, 1, "Namespaces cannot be serialized:%."),
  /** Error code. */
  SERFUNC_X(SENR, 1, "Items of type % cannot be serialized."),
  /** Error code. */
  SERSA(SEPM, 4, "If 'standalone' is specified, the root must be a single element."),
  /** Error code. */
  SERDT(SEPM, 4, "If 'doctype-system' is specified, the root must be a single element."),
  /** Error code. */
  SERENCODING_X(SESU, 7, "Unknown encoding '%'."),
  /** Error code. */
  SERENC_X_X(SERE, 8, "Character '#x%;' cannot be mapped to '%'."),
  /** Error code. */
  SERSTAND(SEPM, 9, "Invalid combination of omit-xml-declaration and other parameters."),
  /** Error code. */
  SERUNDECL(SEPM, 10, "XML 1.0: undeclaring prefixes not allowed."),
  /** Error code. */
  SERNORM_X(SESU, 11, "Normalization form not supported: %."),
  /** Error code. */
  SERNOTSUPP_X(SESU, 13, "%"),
  /** Error code. */
  SERILL_X(SERE, 14, "Illegal HTML character found: #x%;."),
  /** Error code. */
  SERPI(SERE, 15, "Processing construction contains '>'."),
  /** Error code. */
  SER_X(SEPM, 16, "%"),
  /** Error code. */
  SERMAP_X(SEPM, 17, "Character map is not valid: %."),
  /** Error code. */
  SEROPT_X(SEPM, 17, "%"),
  /** Error code. */
  SEROPTION_X(SEPM, 17, "Serialization parameter '%' is invalid."),
  /** Error code. */
  SERNUMBER_X(SERE, 20, "Numeric value cannot be represented: '%'"),
  /** Error code. */
  SERJSONFUNC_X(SERE, 21, "Items of type % cannot be serialized."),
  /** Error code. */
  SERDUPL_X(SERE, 22, "Duplicate name found: '%'"),
  /** Error code. */
  SERJSON(SERE, 23, "Only one item can be serialized with JSON."),
  /** Error code. */
  SERJSONSEQ(SERE, 23, "Value has more than one item."),

  /** Error code. */
  NOCTX_X(XPDY, 2, "%: Context is undeclared."),
  /** Error code. */
  VAREMPTY_X(XPDY, 2, "No value assigned to %."),
  /** Error code. */
  NODOC_X(XPDY, 50, "Value has no document node: %."),
  /** Error code. */
  NOTREAT_X_X_X(XPDY, 50, "Cannot treat % as %: %."),
  /** Error code. */
  ARRAY_X_X(XPDY, 130, "Maximum size exceeded (%): %."),

  /** Error code. */
  QUERYEMPTY(XPST, 3, "Empty query."),
  /** Error code. */
  MODLEINV_X(XPST, 3, "Module contains illegal character: #%."),
  /** Error code. */
  NOQUOTE_X(XPST, 3, "Expecting quote%."),
  /** Error code. */
  ARITY_X(XPST, 3, "Expecting function arity, '%' found."),
  /** Error code. */
  NUMBERWS_X(XPST, 3, "Expecting separator after number: '%'."),
  /** Error code. */
  NUMBER_X(XPST, 3, "Incomplete number: '%'."),
  /** Error code. */
  NUMBERITR_X_X(XPST, 3, "Integer expected, % found: '%'."),
  /** Error code. */
  QUERYEND_X(XPST, 3, "Unexpected end of query: '%'."),
  /** Error code. */
  MODEXPR(XPST, 3, "No expression allowed in a library module."),
  /** Error code. */
  MAINMOD(XPST, 3, "Library modules cannot be evaluated."),
  /** Error code. */
  CMPEXPR(XPST, 3, "Comparison is incomplete."),
  /** Error code. */
  UPDATEEXPR(XPST, 3, "Expecting update expression."),
  /** Error code. */
  NOELEMNAME(XPST, 3, "Expecting element name."),
  /** Error code. */
  ELEMNAME_X(XPST, 3, "Expecting element name, '<%' found."),
  /** Error code. */
  NOATTNAME(XPST, 3, "Expecting attribute name."),
  /** Error code. */
  NOEXPR(XPST, 3, "Expecting expression."),
  /** Error code. */
  NOCONTENT(XPST, 3, "Expecting node content."),
  /** Error code. */
  WRONGCHAR_X_X(XPST, 3, "Expecting '%'%."),
  /** Error code. */
  INVENTITY_X(XPST, 3, "Invalid entity: '%'."),
  /** Error code. */
  INCOMPLETE(XPST, 3, "Incomplete expression."),
  /** Error code. */
  EVALUNARY(XPST, 3, "Unary operator expects a numeric value."),
  /** Error code. */
  STEPMISS_X(XPST, 3, "Expecting valid step%."),
  /** Error code. */
  AXISMISS_X(XPST, 3, "Expecting node test after % axis."),
  /** Error code. */
  DECLINCOMPLETE(XPST, 3, "Expecting 'function', 'variable', ..."),
  /** Error code. */
  FUNCNAME(XPST, 3, "Expecting function name."),
  /** Error code. */
  RESERVED_X(XPST, 3, "'%' is a reserved keyword."),
  /** Error code. */
  NOVARNAME(XPST, 3, "Variable name expected, '%' found."),
  /** Error code. */
  NOVARDECL(XPST, 3, "Expecting variable declaration."),
  /** Error code. */
  NOCIDECL(XPST, 3, "Expecting context item."),
  /** Error code. */
  PIWRONG(XPST, 3, "Expecting name of processing-instruction."),
  /** Error code. */
  NOFTSELECT_X(XPST, 3, "Expecting quote or opening curly brace%."),
  /** Error code. */
  FUNCARG_X(XPST, 3, "Expecting function argument%."),
  /** Error code. */
  MAPTAAT_X(XPST, 3, "Expecting atomic key type for map, found '%'."),
  /** Error code. */
  TYPEINVALID(XPST, 3, "Expecting type declaration."),
  /** Error code. */
  NOTYPESWITCH(XPST, 3, "Incomplete typeswitch expression."),
  /** Error code. */
  NOSWITCH(XPST, 3, "Incomplete switch expression."),
  /** Error code. */
  TYPEPAR(XPST, 3, "Expecting '(' after 'switch' or 'typeswitch'."),
  /** Error code. */
  PRAGMAINV(XPST, 3, "Invalid pragma expression."),
  /** Error code. */
  CALCEXPR(XPST, 3, "Calculation is incomplete."),
  /** Error code. */
  INVMAPKEY(XPST, 3, "Invalid key, simple expression expected."),
  /** Error code. */
  INVMAPVAL(XPST, 3, "Invalid value, simple expression expected."),
  /** Error code. */
  NORETURN(XPST, 3, "Expecting return value."),
  /** Error code. */
  NOWHERE(XPST, 3, "Expecting valid expression after 'where'."),
  /** Error code. */
  ORDERBY(XPST, 3, "Expecting valid expression after 'order by'."),
  /** Error code. */
  GRPBY(XPST, 3, "Expecting valid expression after 'group by'."),
  /** Error code. */
  FLWORRETURN(XPST, 3, "Incomplete FLWOR expression, expecting 'return'."),
  /** Error code. */
  NOSOME(XPST, 3, "Incomplete quantifier expression."),
  /** Error code. */
  IFPAR(XPST, 3, "Expecting '(' after 'if' expression."),
  /** Error code. */
  NOTERNARY(XPST, 3, "Incomplete ternary if expression."),
  /** Error code. */
  NOELVIS(XPST, 3, "Expecting default expression."),
  /** Error code. */
  NOIF(XPST, 3, "Incomplete 'if' expression."),
  /** Error code. */
  NOFOR(XPST, 3, "Incomplete 'for' expression."),
  /** Error code. */
  NOLET(XPST, 3, "Incomplete 'let' expression."),
  /** Error code. */
  NOWINDOW(XPST, 3, "Incomplete 'window' expression."),
  /** Error code. */
  NOCOUNT(XPST, 3, "Incomplete 'count' expression."),
  /** Error code. */
  NOCLOSING_X(XPST, 3, "Expecting closing tag </%>."),
  /** Error code. */
  COMCLOSE(XPST, 3, "Unclosed XQuery comment (: ..."),
  /** Error code. */
  EXPREMPTY(XPST, 3, "Unknown function or expression."),
  /** Error code. */
  WHICHTYPE_X(XPST, 3, "Unknown type: %."),
  /** Error code. */
  BINDNAME_X(XPST, 3, "Invalid name: '%'."),
  /** Error code. */
  PIXML_X(XPST, 3, "Processing instruction has illegal name: %."),
  /** Error code. */
  QNAME_X(XPST, 3, "Expecting QName, '%' found."),
  /** Error code. */
  PROLOGORDER(XPST, 3, "Default declarations must be declared first."),
  /** Error code. */
  FTRANGE(XPST, 3, "Expecting full-text range."),
  /** Error code. */
  FTSTOP(XPST, 3, "Stop words expected."),
  /** Error code. */
  FTMATCH_X(XPST, 3, "Unknown match option '%...'."),
  /** Error code. */
  INVALPI(XPST, 3, "Processing instruction has invalid name: '%' found."),
  /** Error code. */
  INTEXP(XPST, 3, "Integer expected."),
  /** Error code. */
  VARFUNC(XPST, 3, "Variable or function declaration expected."),
  /** Error code. */
  NOANN(XPST, 3, "No annotation allowed here."),
  /** Error code. */
  NOCATCH(XPST, 3, "Expecting catch clause."),
  /** Error code. */
  ANNVALUE(XPST, 3, "Literal expected, ')' found."),
  /** Error code. */
  UPDATINGVAR(XPST, 3, "Variable cannot be updating."),
  /** Error code. */
  SIMPLETYPE_X(XPST, 3, "Simple type expected, function found: %(."),
  /** Error code. */
  KEYSPEC(XPST, 3, "No specifier after lookup operator: '%'."),
  /** Error code. */
  ARROWSPEC(XPST, 3, "No specifier after arrow operator: '%'."),

  /** Error code. */
  STATIC_X(XPST, 5, "No XML Schema support: %."),

  /** Error code. */
  VARUNDEF_X(XPST, 8, "Undeclared variable: %."),
  /** Error code. */
  CIRCREF_X(XPST, 8, "Static variable references itself: %"),
  /** Error code. */
  VARPRIVATE_X(XPST, 8, "Variable % is not visible from this module."),
  /** Error code. */
  TYPEUNDEF_X(XPST, 8, "Undefined type annotation: %."),
  /** Error code. */
  SCHEMAINV_X(XPST, 8, "Undefined schema name: %."),

  /** Error code. */
  FUNCPRIVATE_X(XPST, 17, "Function not visible: %."),
  /** Error code. */
  FUNCARITY_X_X(XPST, 17, "%: % supplied."),
  /** Error code. */
  FUNCARITY_X_X_X(XPST, 17, "%: % supplied, % expected."),
  /** Error code. */
  WHICHFUNC_X(XPST, 17, "Unknown function: %."),
  /** Error code. */
  INVALIDFUNC_X(XPST, 17, "Invalid function: %."),
  /** Error code. */
  FUNCNOIMPL_X(XPST, 17, "External function not implemented: %."),
  /** Error code. */
  WHICHCLASS_X(XPST, 17, "Unknown class: %."),
  /** Error code. */
  JAVACONSTR_X_X(XPST, 17, "Unknown constructor: %#%."),
  /** Error code. */
  JAVAMULTIFUNC_X_X(XPST, 17, "%: Multiple functions with %."),
  /** Error code. */
  JAVAARGS_X_X(XPST, 17, "% cannot be called with (%)."),
  /** Error code. */
  JAVAINIT_X_X(XPST, 17, "%: %."),

  /** Error code. */
  TYPEUNKNOWN_X(XPST, 51, "Unknown type: %."),
  /** Error code. */
  INVALIDCAST_X(XPST, 80, "Invalid cast type: %."),
  /** Error code. */
  NOURI_X(XPST, 81, "No namespace declared for '%'."),
  /** Error code. */
  NSMISS_X(XPST, 81, "QName '%' has no namespace."),

  /** Error code. */
  DYNARGS_X_X(XPTY, 4, "% cannot be called with (%)."),
  /** Error code. */
  DYNMULTIFUNC_X_X(XPTY, 4, "%: Multiple functions with %."),
  /** Error code. */
  DYNMULTICONS_X_X(XPTY, 4, "%: Multiple constructors with %."),
  /** Error code. */
  JAVAARGS_X_X_X(XPTY, 4, "%(%) expected, (%) found."),
  /** Error code. */
  JAVAINVOKE_X_X(XPTY, 4, "% instance expected as first argument, % found."),
  /** Error code. */
  JAVAEVAL_X_X_X(XPTY, 4, "%. Caused by: %(%)."),

  /** Error code. */
  ZEROFUNCS_X_X(XPTY, 4, "Zero-arity functions expected, % found: %."),
  /** Error code. */
  NONAME_X(XPTY, 4, "Name expected, '%' found."),
  /** Error code. */
  EMPTYFOUND(XPTY, 4, "Item expected, empty sequence found."),
  /** Error code. */
  EMPTYFOUND_X(XPTY, 4, "% expected, empty sequence found."),
  /** Error code. */
  SEQFOUND_X(XPTY, 4, "Item expected, sequence found: %."),
  /** Error code. */
  NONUMBER_X_X(XPTY, 4, "Number expected, % found: %."),
  /** Error code. */
  NODUR_X_X(XPTY, 4, "Duration expected, % found: %."),
  /** Error code. */
  NOSUBDUR_X(XPTY, 4, "Subtype of xs:duration expected: %."),
  /** Error code. */
  STRQNM_X_X(XPTY, 4, "String or QName expected, % found: %."),
  /** Error code. */
  STRNCN_X_X(XPTY, 4, "String or NCName expected, % found: %."),
  /** Error code. */
  INVTYPE_X_X_X(XPTY, 4, "Cannot convert % to %: %."),
  /** Error code. */
  INVPROMOTE_X_X_X(XPTY, 4, "Cannot promote % to %: %."),
  /** Error code. */
  INVTREAT_X_X_X(XPTY, 4, "Cannot treat % as %: %."),
  /** Error code. */
  CALCTYPE_X_X_X(XPTY, 4, "% not defined for % and %."),
  /** Error code. */
  INVFUNCITEM_X_X(XPTY, 4, "Function expected, % found: %."),
  /** Error code. */
  CMPTYPE_X(XPTY, 4, "Type % is not comparable."),
  /** Error code. */
  CMPTYPES_X_X(XPTY, 4, "Types % and % are not comparable."),
  /** Error code. */
  DOCATTS_X(XPTY, 4, "Cannot add attributes to a document node: %."),
  /** Error code. */
  DOCNS_X(XPTY, 4, "Cannot add namespaces to a document node: %."),
  /** Error code. */
  INVARITY_X_X_X(XPTY, 4, "% supplied, % expected: %."),
  /** Error code. */
  FUNARITY_X_X(XPTY, 4, "Function with % supplied, % expected."),
  /** Error code. */
  INVNCNAME_X(XPTY, 4, "Invalid NCName: '%'."),
  /** Error code. */
  CITYPES_X_X(XPTY, 4, "Incompatible types in context value declarations: % vs. %."),
  /** Error code. */
  LOOKUP_X(XPTY, 4, "Input of lookup operator must be map or array: %."),
  /** Error code. */
  INVALIDOPT_X(XPTY, 4, "%"),
  /** Error code. */
  BINARY_X(XPTY, 4, "Binary expected, % found."),
  /** Error code. */
  STRNOD_X_X(XPTY, 4, "String or node expected, % found: %."),
  /** Error code. */
  MAP_X_X(XPTY, 4, "Map expected, % found: %."),
  /** Error code. */
  ELMMAP_X_X_X(XPTY, 4, "element(%) or map expected, % found: %."),
  /** Error code. */
  ELMSTR_X_X_X(XPTY, 4, "element(%) or string expected, % found: %."),
  /** Error code. */
  ELM_X_X(XPTY, 4, "element(%) expected: %."),
  /** Error code. */
  STRBIN_X_X(XPTY, 4, "String or binary expected, % found: %."),
  /** Error code. */
  INVALIDOPTION_X(XPTY, 4, "Unknown option '%'."),
  /** Error code. */
  FUNCUP_X(XPTY, 4, "Function must not be updating: %."),
  /** Error code. */
  FUNCNOTUP_X(XPTY, 4, "Function is not updating: %."),

  /** Error code. */
  MIXEDRESULTS(XPTY, 18, "Path returns both nodes and non-nodes."),
  /** Error code. */
  PATHNODE_X_X_X(XPTY, 19, "%: node expected, % found: %."),
  /** Error code. */
  STEPNODE_X_X_X(XPTY, 20, "%: node expected, % found: %."),
  /** Error code. */
  NSSENS_X_X(XPTY, 117, "Cannot convert % to %."),

  /** Error code. */
  CATTDUPL_X(XQDY, 25, "Duplicate attribute '%'."),
  /** Error code. */
  CPICONT_X(XQDY, 26, "Processing instruction has invalid content: '%'."),
  /** Error code. */
  CPIINVAL_X(XQDY, 41, "Processing instruction has invalid name: '%'."),
  /** Error code. */
  CAXML(XQDY, 44, "XML prefix and namespace cannot be rebound."),
  /** Error code. */
  CAINV_(XQDY, 44, "Invalid attribute prefix/namespace: '%'."),
  /** Error code. */
  CIRCVAR_X(XQDY, 54, "Static variable depends on itself: %"),
  /** Error code. */
  CIRCCTX(XQDY, 54, "Context value is not defined."),
  /** Error code. */
  CPIXML_X(XQDY, 64, "Processing instruction has illegal name: '%'."),
  /** Error code. */
  COMINVALID(XQDY, 72, "Comment must not contain '--' or end with '-'."),
  /** Error code. */
  INVNSNAME_X(XQDY, 74, "Invalid namespace prefix: '%'."),
  /** Error code. */
  INVNAME_X(XQDY, 74, "Invalid QName: '%'."),
  /** Error code. */
  INVPREF_X(XQDY, 74, "No namespace declared for %."),
  /** Error code. */
  CEXML(XQDY, 96, "XML prefix or namespace cannot be rebound: '%'/'%'."),
  /** Error code. */
  CEINV_X(XQDY, 96, "Invalid element prefix/namespace '%'."),
  /** Error code. */
  CNXML(XQDY, 101, "XML prefix and namespace cannot be rebound."),
  /** Error code. */
  CNINV_X(XQDY, 101, "Invalid namespace prefix '%'."),
  /** Error code. */
  CNINVNS_X(XQDY, 101, "Invalid namespace URI '%'."),
  /** Error code. */
  DUPLNSCONS_X(XQDY, 102, "Duplicate namespace declaration: '%'."),
  /** Error code. */
  MAPDUPLKEY_X_X_X(XQDY, 137, "Key % already exists in map (values: % vs. %)."),

  /** Error code. */
  IMPLSCHEMA(XQST, 9, "No XML Schema support."),
  /** Error code. */
  NSCONS(XQST, 22, "Constant namespace value expected."),
  /** Error code. */
  XQUERYVER_X(XQST, 31, "XQuery version '%' not supported."),
  /** Error code. */
  DUPLBASE(XQST, 32, "Duplicate 'base-uri' declaration."),
  /** Error code. */
  DUPLNSDECL_X(XQST, 33, "Duplicate declaration of prefix '%'."),
  /** Error code. */
  FUNCDEFINED_X(XQST, 34, "Duplicate declaration of function '%'."),
  /** Error code. */
  DUPLCOLL(XQST, 38, "Duplicate 'collation' declaration."),
  /** Error code. */
  WHICHDEFCOLL_X(XQST, 38, "%."),
  /** Error code. */
  FUNCDUPL_X(XQST, 39, "Duplicate parameter name: %."),
  /** Error code. */
  ATTDUPL_X(XQST, 40, "Duplicate attribute '%'."),
  /** Error code. */
  FNRESERVED_X(XQST, 45, "Function '%' is in reserved namespace."),
  /** Error code. */
  ANNWHICH_X_X(XQST, 45, "Annotation %% is in reserved namespace."),
  /** Error code. */
  INVURI_X(XQST, 46, "URI '%' is invalid."),
  /** Error code. */
  DUPLMODULE_X(XQST, 47, "Module namespace is declared twice: '%'."),
  /** Error code. */
  MODULENS_X(XQST, 48, "Declaration % does not match the module namespace."),
  /** Error code. */
  VARDUPL_X(XQST, 49, "Duplicate declaration of static variable $%."),
  /** Error code. */
  WHICHCAST_X(XQST, 52, "Unknown type: %."),
  /** Error code. */
  DUPLCOPYNS(XQST, 55, "Duplicate 'copy-namespace' declaration."),
  /** Error code. */
  NSEMPTY(XQST, 57, "Namespace URI cannot be empty."),
  /** Error code. */
  WHICHMOD_X(XQST, 59, "Module not found: %."),
  /** Error code. */
  WHICHMODCLASS_X(XQST, 59, "Java class not found: %."),
  /** Error code. */
  MODINIT_X_X_X(XQST, 59, "Could not initialize %: % (%)."),
  /** Error code. */
  MODINST_X_X(XQST, 59, "Could not instantiate %: %."),
  /** Error code. */
  WHICHMODFILE_X(XQST, 59, "Could not retrieve module: %."),
  /** Error code. */
  WRONGMODULE_X_X_X(XQST, 59, "Imported module '%' has unexpected namespace: '%' vs '%'."),
  /** Error code. */
  FUNNONS_X(XQST, 60, "Namespace needed for function '%'."),
  /** Error code. */
  DUPLORD(XQST, 65, "Duplicate 'ordering' declaration."),
  /** Error code. */
  DUPLNS(XQST, 66, "Duplicate 'default namespace' declaration."),
  /** Error code. */
  DUPLCONS(XQST, 67, "Duplicate 'construction' declaration."),
  /** Error code. */
  DUPLBOUND(XQST, 68, "Duplicate 'boundary-space' declaration."),
  /** Error code. */
  DUPLORDEMP(XQST, 69, "Duplicate 'order empty' declaration."),
  /** Error code. */
  BINDXML_X(XQST, 70, "Prefix '%' cannot be rebound."),
  /** Error code. */
  XMLNSDEF_X(XQST, 70, "'%' cannot be default namespace."),
  /** Error code. */
  BINDXMLURI_X_X(XQST, 70, "'%' can only be bound to '%'."),
  /** Error code. */
  ILLEGALEQNAME_X(XQST, 70, "Illegal namespace: %."),
  /** Error code. */
  DUPLNSDEF_X(XQST, 71, "Duplicate declaration of prefix '%'."),
  /** Error code. */
  IMPLVAL(XQST, 75, "Validation not supported."),
  /** Error code. */
  FLWORCOLL_X(XQST, 76, "%."),
  /** Error code. */
  NOPRAGMA(XQST, 79, "Expecting pragma expression."),
  /** Error code. */
  NSEMPTYURI(XQST, 85, "Namespace URI cannot be empty."),
  /** Error code. */
  XQUERYENC2_X(XQST, 87, "Unknown encoding '%'."),
  /** Error code. */
  NSMODURI(XQST, 88, "Module namespace cannot be empty."),
  /** Error code. */
  DUPLVAR_X(XQST, 89, "Duplicate declaration of %."),
  /** Error code. */
  INVCHARREF_X(XQST, 90, "Invalid character reference '%'."),
  /** Error code. */
  CIRCMODULE(XQST, 93, "Circular module declaration."),
  /** Error code. */
  GVARNOTDEFINED_X(XQST, 94, "Undeclared grouping variable '%'."),
  /** Error code. */
  INVDECFORM_X_X(XQST, 97, "Invalid decimal-format property: %='%'."),
  /** Error code. */
  INVDECSINGLE_X_X(XQST, 97, "Decimal-format property is no single character: %='%'."),
  /** Error code. */
  INVDECZERO_X(XQST, 97, "Zero-digit property is no Unicode digit with value zero: %."),
  /** Error code. */
  DUPLDECFORM_X(XQST, 98, "Clash of decimal format properties: '%'."),
  /** Error code. */
  DUPLITEM(XQST, 99, "Duplicate declaration of context value."),
  /** Error code. */
  DUPLWIND_X(XQST, 103, "Duplicate declaration of %."),
  /** Error code. */
  DUPLUPD(XQST, 106, "More than one updating annotation declared."),
  /** Error code. */
  DUPLFUNVIS(XQST, 106, "More than one visibility annotation declared."),
  /** Error code. */
  OPTDECL_X(XQST, 108, "Output declaration not allowed library module: %."),
  /** Error code. */
  OUTINVALID_X(XQST, 109, "%"),
  /** Error code. */
  OUTDUPL_X(XQST, 110, "Duplicate declaration of 'output:%'."),
  /** Error code. */
  DECDUPL(XQST, 111, "Duplicate decimal-format declaration."),
  /** Error code. */
  DECITEM(XQST, 113, "Context value cannot be bound in library module."),
  /** Error code. */
  DECDUPLPROP_X(XQST, 114, "Duplicate decimal-format property '%'."),
  /** Error code. */
  DUPLVARVIS(XQST, 116, "More than one visibility annotation declared."),
  /** Error code. */
  TAGWRONG_X_X(XQST, 118, "Different start and end tag: <%>...</%>."),
  /** Error code. */
  OUTDOC_X(XQST, 119, "Parameter document cannot be parsed: %."),
  /** Error code. */
  NOVISALLOWED(XQST, 125, "No visibility annotation allowed in inline function."),
  /** Error code. */
  NSAXIS(XQST, 134, "Namespace axis is not supported."),

  /** Error code. */
  NOATTALL_X(XQTY, 24, "Attribute does not follow root element: %."),
  /** Error code. */
  NONSALL_X(XQTY, 24, "Namespaces does not follow root element: %."),
  /** Error code. */
  CONSFUNC_X(XQTY, 105, "Invalid content: %."),

  /** Error code. */
  UPNOPAR_X(XUDY, 9, "Target has no parent: %."),
  /** Error code. */
  UPNOTCOPIED_X(XUDY, 14, "Node was not created by transform expression: %."),
  /** Error code. */
  UPMULTREN_X(XUDY, 15, "Node can only be renamed once: %."),
  /** Error code. */
  UPPATHREN_X(XUDY, 15, "Path can only be renamed once: %."),
  /** Error code. */
  UPMULTREPL_X(XUDY, 16, "Node can only be replaced once: %."),
  /** Error code. */
  UPMULTDOC_X_X(XUDY, 16, "Documents in path '%/%' can only be replaced once."),
  /** Error code. */
  UPMULTREPV_X(XUDY, 17, "Node can only be replaced once: %."),
  /** Error code. */
  UPATTDUPL_X(XUDY, 21, "Duplicate attribute: %."),
  /** Error code. */
  UPNSCONFL_X_X(XUDY, 23, "Namespace conflicts: % vs. %."),
  /** Error code. */
  UPNSCONFL2_X_X(XUDY, 24, "Namespaces conflicts: % vs. %."),
  /** Error code. */
  UPSEQEMP_X(XUDY, 27, "% target is an empty sequence."),
  /** Error code. */
  UPPAREMPTY_X(XUDY, 29, "Target has no parent: %."),
  /** Error code. */
  UPATTELM_X(XUDY, 30, "Attribute cannot be added to %."),
  /** Error code. */
  UPURIDUP_X(XUDY, 31, "URI '%' is addressed multiple times."),

  /** Error code. */
  UPNOT_X(XUST, 1, "%: no updating expression allowed."),
  /** Error code. */
  UPALL(XUST, 1, "Expressions must all be updating or return an empty sequence."),
  /** Error code. */
  UPCTX(XUST, 1, "Context value may not declare an updating expression."),

  /** Error code. */
  UPMODIFY(XUST, 2, "Transformations must all be updating or return an empty sequence."),
  /** Error code. */
  UPEXPECTF(XUST, 2, "Function body must be updating."),
  /** Error code. */
  DUPLREVAL(XUST, 3, "Duplicate 'revalidation' declaration."),
  /** Error code. */
  NOREVAL(XUST, 26, "Revalidation mode not supported."),
  /** Error code. */
  UUPFUNCTYPE(XUST, 28, "No return type allowed in updating functions."),

  /** Error code. */
  UPNOATTRPER_X(XUTY, 4, "Attribute does not follow root element: %."),
  /** Error code. */
  UPTRGTYP_X(XUTY, 5, "Target is not an element or document: %."),
  /** Error code. */
  UPTRGSNGL_X(XUTY, 5, "Target is not a single node: %."),
  /** Error code. */
  UPTRGTYP2_X(XUTY, 6, "Target is not an element, text, comment or pi: %."),
  /** Error code. */
  UPTRGSNGL2_X(XUTY, 6, "Target is not a single node: %."),
  /** Error code. */
  UPTRGDELEMPT_X(XUTY, 7, "Target is not a node: %."),
  /** Error code. */
  UPTRGNODE_X(XUTY, 8, "Target is not an element, text, attribute, comment or pi: %."),
  /** Error code. */
  UPTRGSINGLE_X(XUTY, 8, "Target is not a single node: %."),
  /** Error code. */
  UPWRELM_X(XUTY, 10, "Node cannot be replaced with attribute: %."),
  /** Error code. */
  UPWRATTR_X(XUTY, 11, "Target is no attribute: %."),
  /** Error code. */
  UPWRTRGTYP_X(XUTY, 12, "Target is not an element, attribute or pi: %."),
  /** Error code. */
  UPWRTRGSINGLE_X(XUTY, 12, "Target is not a single node: %."),
  /** Error code. */
  UPSINGLE_X_X(XUTY, 13, "Value of $% is not a single node: %."),
  /** Error code. */
  UPSOURCE_X(XUTY, 13, "Source is not a node: %."),
  /** Error code. */
  UPATTELM2_X(XUTY, 22, "Attribute cannot be added to %.");

  /** Cached enums (faster). */
  private static final QueryError[] VALUES = values();

  /** Error code. */
  private final String code;
  /** Error URI. */
  private final byte[] uri;
  /** Error prefix. */
  private final String prefix;
  /** Error message. */
  public final String message;

  /**
   * Constructor.
   * @param type error type
   * @param code error code
   * @param message message
   */
  QueryError(final ErrType type, final String code, final String message) {
    this.code = code;
    this.message = message;
    uri = type.uri;
    prefix = type.prefix;
  }

  /**
   * Constructor.
   * @param type error type
   * @param number error number
   * @param message message
   */
  QueryError(final ErrType type, final int number, final String message) {
    final StringBuilder sb = new StringBuilder(8).append(type);
    final String n = Integer.toString(number);
    final int s = 4 - n.length();
    for(int i = 0; i < s; i++) sb.append('0');
    code = sb.append(n).toString();
    uri = type.uri;
    prefix = type.prefix;
    this.message = message;
  }

  /**
   * Throws a query exception. If {@link InputInfo#internal()} returns {@code true},
   * a static error instance ({@link QueryException#ERROR}) will be returned.
   * @param ii input info (can be {@code null})
   * @param ext extended info
   * @return query exception
   */
  public QueryException get(final InputInfo ii, final Object... ext) {
    return ii != null && ii.internal() ? QueryException.ERROR : new QueryException(ii, this, ext);
  }

  /**
   * Throws a query I/O exception without {@link InputInfo} reference.
   * @param ext extended info
   * @return query I/O exception
   */
  public QueryIOException getIO(final Object... ext) {
    return new QueryIOException(get(null, ext));
  }

  /**
   * Checks if the error code equals the specified QName.
   * @param name name to compare
   * @return result of check
   */
  public final boolean eq(final QNm name) {
    return Token.eq(name.uri(), uri) && Token.eq(name.local(), Token.token(code));
  }

  /**
   * Error types.
   * @author BaseX Team 2005-21, BSD License
   * @author Leo Woerteler
   */
  public enum ErrType {
    // Project-specific errors

    /** Error type. */ ADMIN(ADMIN_PREFIX,       ADMIN_URI),
    /** Error type. */ BASEX(BASEX_PREFIX,       BASEX_URI),
    /** Error type. */ ARCHIVE(ADMIN_PREFIX,     ARCHIVE_URI),
    /** Error type. */ CLIENT(CLIENT_PREFIX,     CLIENT_URI),
    /** Error type. */ CONVERT(CONVERT_PREFIX,   CONVERT_URI),
    /** Error type. */ CSV(CSV_PREFIX,           CSV_URI),
    /** Error type. */ DB(DB_PREFIX,             DB_URI),
    /** Error type. */ FETCH(FETCH_PREFIX,       FETCH_URI),
    /** Error type. */ FT(FT_PREFIX,             FT_URI),
    /** Error type. */ GEO(GEO_PREFIX,           GEO_URI),
    /** Error type. */ HTML(HTML_PREFIX,         HTML_URI),
    /** Error type. */ HASH(HASH_PREFIX,         HASH_URI),
    /** Error type. */ INSPECT(INSPECT_PREFIX,   INSPECT_URI),
    /** Error type. */ JOBS(JOBS_PREFIX,         JOBS_URI),
    /** Error type. */ JSON(JSON_PREFIX,         JSON_URI),
    /** Error type. */ OUTPUT(OUTPUT_PREFIX,     OUTPUT_URI),
    /** Error type. */ PROC(PROC_PREFIX,         PROC_URI),
    /** Error type. */ PROF(PROF_PREFIX,         PROF_URI),
    /** Error type. */ RANDOM(RANDOM_PREFIX,     RANDOM_URI),
    /** Error type. */ REPO(REPO_PREFIX,         REPO_URI),
    /** Error type. */ REQUEST(REQUEST_PREFIX,   REQUEST_URI),
    /** Error type. */ SESSION(SESSION_PREFIX,   SESSION_URI),
    /** Error type. */ SESSIONS(SESSIONS_PREFIX, SESSIONS_URI),
    /** Error type. */ WS(WS_PREFIX,             WS_URI),
    /** Error type. */ SQL(SQL_PREFIX,           SQL_URI),
    /** Error type. */ UNIT(UNIT_PREFIX,         UNIT_URI),
    /** Error type. */ USER(USER_PREFIX,         USER_URI),
    /** Error type. */ VALIDATE(VALIDATE_PREFIX, VALIDATE_URI),
    /** Error type. */ WEB(WEB_PREFIX,           WEB_URI),
    /** Error type. */ XQUERY(XQUERY_PREFIX,     XQUERY_URI),
    /** Error type. */ XSLT(XSLT_PREFIX,         XSLT_URI),

    // EXPath errors

    /** Error type. */ BIN(BIN_PREFIX,    BIN_URI),
    /** Error type. */ CX(EXPERR_PREFIX,  EXPERROR_URI),
    /** Error type. */ FILE(FILE_PREFIX,  FILE_URI),
    /** Error type. */ HC(EXPERR_PREFIX,  EXPERROR_URI),
    /** Error type. */ ZIP(EXPERR_PREFIX, EXPERROR_URI),

    // W3 errors

    /** Error type. */ FOAP,
    /** Error type. */ FOAR,
    /** Error type. */ FOAY,
    /** Error type. */ FOCA,
    /** Error type. */ FOCH,
    /** Error type. */ FODC,
    /** Error type. */ FODF,
    /** Error type. */ FODT,
    /** Error type. */ FOFD,
    /** Error type. */ FOER,
    /** Error type. */ FOJS,
    /** Error type. */ FONS,
    /** Error type. */ FORG,
    /** Error type. */ FORX,
    /** Error type. */ FOTY,
    /** Error type. */ FOUP,
    /** Error type. */ FOUT,
    /** Error type. */ FTDY,
    /** Error type. */ FTST,
    /** Error type. */ SENR,
    /** Error type. */ SEPM,
    /** Error type. */ SERE,
    /** Error type. */ SESU,
    /** Error type. */ XPDY,
    /** Error type. */ XPST,
    /** Error type. */ XPTY,
    /** Error type. */ XQDY,
    /** Error type. */ XQST,
    /** Error type. */ XQTY,
    /** Error type. */ XUDY,
    /** Error type. */ XUST,
    /** Error type. */ XUTY;

    /** This error type's prefix. */
    public final String prefix;
    /** This error type's URI. */
    public final byte[] uri;

    /**
     * Constructor for non-standard errors.
     * @param prefix QName prefix
     * @param uri error URI
     */
    ErrType(final byte[] prefix, final byte[] uri) {
      this.prefix = Token.string(prefix);
      this.uri = uri;
    }

    /**
     * Constructor for standard XQuery errors. The prefix is {@code err}, the URI is
     * {@code http://www.w3.org/2005/xqt-errors}.
     */
    ErrType() {
      this(ERR_PREFIX, ERROR_URI);
    }
  }

  /**
   * Returns the namespace URI of this error.
   * @return function
   */
  public final QNm qname() {
    return new QNm(prefix + ':' + code, uri);
  }

  /**
   * Returns an error for the specified name.
   * @param name error name
   * @param msg error message
   * @param ii input info
   * @return exception or {@code null}
   */
  public static QueryException get(final String name, final String msg, final InputInfo ii) {
    for(final QueryError err : VALUES) {
      if(err.toString().equals(name)) return new QueryException(ii, err.qname(), msg).error(err);
    }
    return null;
  }

  /**
   * Throws a comparison exception.
   * @param item1 first item
   * @param item2 second item
   * @param ii input info
   * @return query exception
   */
  public static QueryException diffError(final Item item1, final Item item2, final InputInfo ii) {
    final Type type1 = item1.type, type2 = item2.type;
    return type1 == type2 ? CMPTYPE_X.get(ii, type1, item1) :
      CMPTYPES_X_X.get(ii, type1, type2);
  }

  /**
   * Throws a type exception.
   * @param ii input info
   * @param expr expression
   * @param type target type
   * @param name name (can be {@code null})
   * @param promote promote or treat as
   * @return query exception
   */
  public static QueryException typeError(final Expr expr, final SeqType type, final QNm name,
      final InputInfo ii, final boolean promote) {
    return typeError(expr, type, name, ii, promote ? INVPROMOTE_X_X_X : INVTREAT_X_X_X);
  }

  /**
   * Throws a type exception.
   * @param ii input info
   * @param expr expression
   * @param st target type
   * @param name variable name (can be {@code null})
   * @param error error code
   * @return query exception
   */
  public static QueryException typeError(final Expr expr, final SeqType st, final QNm name,
      final InputInfo ii, final QueryError error) {

    final TokenBuilder tb = new TokenBuilder();
    if(name != null) tb.add('$').add(name.string()).add(" := ");
    final byte[] value = tb.add(normalize(expr, ii)).finish();
    return error.get(ii, expr.seqType(), st, value);
  }

  /**
   * Throws a type cast exception.
   * @param value value
   * @param type target type
   * @param ii input info
   * @return query exception
   */
  public static QueryException typeError(final Value value, final Type type, final InputInfo ii) {
    return INVTYPE_X_X_X.get(ii, value.type, type, value);
  }

  /**
   * Throws a number exception.
   * @param expr parsing expression
   * @param item item
   * @return query exception
   */
  public static QueryException numberError(final ParseExpr expr, final Item item) {
    return numberError(item, expr.info);
  }

  /**
   * Throws a number exception.
   * @param item found item
   * @param ii input info
   * @return query exception
   */
  public static QueryException numberError(final Item item, final InputInfo ii) {
    return NONUMBER_X_X.get(ii, item.type, item);
  }

  /**
   * Throws an invalid value exception.
   * @param type expected type
   * @param value value
   * @param ii input info
   * @return query exception
   */
  public static QueryException valueError(final Type type, final byte[] value, final InputInfo ii) {
    return INVALUE_X_X.get(ii, type, value);
  }

  /**
   * Returns a plural suffix or an empty string.
   * @param number long number
   * @return suffix
   */
  public static byte[] arguments(final long number) {
    final TokenBuilder tb = new TokenBuilder().addLong(number).add(" argument");
    if(number != 1) tb.add('s');
    return tb.finish();
  }

  /**
   * Returns an info message for similar strings.
   * @param string original string
   * @param similar similar string (can be {@code null})
   * @return info message
   */
  public static byte[] similar(final Object string, final Object similar) {
    return similar == null ? Token.token(string) : Util.inf("% (similar: %)", string, similar);
  }

  /**
   * Removes whitespaces and chops the specified value to a maximum size.
   * @param value value
   * @param ii input info (can be {@code null}; an empty string will be returned if
   * {@link InputInfo#internal()} returns {@code true})
   * @return chopped or empty string
   */
  public static byte[] normalize(final Object value, final InputInfo ii) {
    return ii != null && ii.internal() ? Token.EMPTY :
           value instanceof byte[] ? normalize((byte[]) value, ii) :
             normalize(value.toString(), ii);
  }

  /**
   * Removes whitespaces and chops the specified string to a maximum size.
   * @param string string
   * @param ii input info (can be {@code null}; an empty string will be returned if
   * {@link InputInfo#internal()} returns {@code true})
   * @return chopped or empty string
   */
  public static byte[] normalize(final String string, final InputInfo ii) {
    return ii != null && ii.internal() ? Token.EMPTY : normalize(Token.token(string), ii);
  }

  /**
   * Removes whitespaces and chops the specified token to a maximum size.
   * @param token token
   * @param ii input info (can be {@code null}; an empty string will be returned if
   * {@link InputInfo#internal()} returns {@code true})
   * @return chopped or empty string
   */
  public static byte[] normalize(final byte[] token, final InputInfo ii) {
    if(ii != null && ii.internal()) return Token.EMPTY;

    final TokenBuilder tb = new TokenBuilder();
    byte l = 0;
    for(byte b : token) {
      final int ts = tb.size();
      if(ts == 100) {
        tb.add(Text.DOTS);
        break;
      }
      if(b == '\n' || b == '\r') b = ' ';
      if(b != ' ' || l != ' ') tb.addByte(b);
      l = b;
    }
    return tb.finish();
  }

  @Override
  public String toString() {
    return code;
  }
}
