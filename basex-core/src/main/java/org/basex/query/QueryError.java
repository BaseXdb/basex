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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public enum QueryError {

  // Admin Module

  /** Error code. */
  ADMIN_DELETE_X(ADMIN, "delete", "Log file could not be deleted: %."),
  /** Error code. */
  ADMIN_TODAY(ADMIN, "today", "Today's log file cannot be deleted."),
  /** Error code. */
  ADMIN_TYPE_X(ADMIN, "type", "Log type must consist of uppercase letters: \"%\"."),

  // Archive Module

  /** Error code. */
  ARCHIVE_DESC1(ARCHIVE, "desc", "Name of ZIP entry must not be empty."),
  /** Error code. */
  ARCHIVE_DESC2_X(ARCHIVE, "desc", "Invalid compression level: '%'."),
  /** Error code. */
  ARCHIVE_DESC3_X(ARCHIVE, "desc", "xs:dateTime value is invalid : '%'."),
  /** Error code. */
  ARCHIVE_ENCODE1_X(ARCHIVE, "encode", "Unknown encoding '%'."),
  /** Error code. */
  ARCHIVE_ENCODE2_X(ARCHIVE, "encode", "String conversion: %."),
  /** Error code. */
  ARCHIVE_FAIL_X(ARCHIVE, "fail", "Operation failed: %."),
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

  // BaseX errors

  /** Error code. */
  BASX_GENERIC_X(BASX, 0, "%"),
  /** Error code. */
  BASX_PERM_X(BASX, 1, "% permission required."),
  /** Error code. */
  BASX_OPTIONS_X(BASX, 2, "Unknown database option '%'."),
  /** Error code. */
  BASX_VALUE_X_X(BASX, 2, "Database option '%' cannot be set to '%'."),
  /** Error code. */
  BASX_WHICH_X(BASX, 2, "%"),
  /** Error code. */
  BASX_RESTXQ_X(BASX, 3, "%"),
  /** Error code. */
  BASX_DBTRANSFORM(BASX, 4, "No database updates allowed within transform expression."),
  /** Error code. */
  BASX_STACKOVERFLOW(BASX, 5, "Stack Overflow: Try tail recursion?"),
  /** Error code. */
  BASX_ANNOT_X_X(BASX, 6, "Annotation %% is unknown."),
  /** Error code. */
  BASX_ANNNUM_X_X(BASX, 6, "%: % supplied."),
  /** Error code. */
  BASX_ANNTYPE_X_X_X(BASX, 6, "%: % expected, % found."),
  /** Error code. */
  BASX_TWICE_X_X(BASX, 6, "Annotation %% was declared twice."),
  /** Error code. */
  BASX_FITEM_X(BASX, 8, "Function items cannot be cached: %."),
  /** Error code. */
  BASX_UPMODIFY(BASX, 9, "Transform expressions must all be updating or return an empty sequence."),
  /** Error code. */
  BASX_OPTDECL_X(BASX, 10, "Database option not allowed in library module: %."),

  // Conversion Module

  /** Error code. */
  BXCO_STRING_X(BXCO, 1, "String conversion: %."),
  /** Error code. */
  BXCO_BASE64_X_X(BXCO, 1, "Conversion of '%' to encoding '%' failed."),
  /** Error code. */
  BXCO_ENCODING_X(BXCO, 2, "Unknown encoding '%'."),
  /** Error code. */
  BXCO_INVBASE_X(BXCO, 3, "Unsupported base: %."),
  /** Error code. */
  BXCO_INVBASEDIG_X_X(BXCO, 4, "Invalid digit for base %: %."),

  // CSV Module

  /** Error code. */
  BXCS_PARSE_X(BXCS, 1, "CSV parser: %."),
  /** Error code. */
  BXCS_SERIAL_X(BXCS, 2, "CSV serializer: %."),

  // Database Module

  /** Error code. */
  BXDB_NODB_X_X(BXDB, 1, "Database node expected, % found: %."),
  /** Error code. */
  BXDB_OPEN_X(BXDB, 2, "%"),
  /** Error code. */
  BXDB_WHICH_X(BXDB, 2, "Database '%' not found."),
  /** Error code. */
  BXDB_WHICHBACK_X(BXDB, 2, "No backup found for database '%'."),
  /** Error code. */
  BXDB_MEM_X(BXDB, 3, "Operation requires database '%' to be persistent."),
  /** Error code. */
  BXDB_INDEX_X(BXDB, 4, "Database '%' has no % index."),
  /** Error code. */
  BXDB_NOINDEX_X(BXDB, 5, "No index access possible: %."),
  /** Error code. */
  BXDB_DOC_X(BXDB, 5, "Root must yield document nodes: %."),
  /** Error code. */
  BXDB_NODOC_X(BXDB, 6, "Database path '%' yields no documents."),
  /** Error code. */
  BXDB_SINGLE_X(BXDB, 6, "Database path '%' points to more than one document."),
  /** Error code. */
  BXDB_OPENED_X(BXDB, 7, "Database '%' cannot be updated, as it is opened by another process."),
  /** Error code. */
  BXDB_LOCK_X(BXDB, 7, "%"),
  /** Error code. */
  BXDB_PATH_X(BXDB, 8, "Invalid path: %."),
  /** Error code. */
  BXDB_RANGE_X_X_X(BXDB, 9, "Database '%' has no node with % value %."),
  /** Error code. */
  BXDB_NAME_X(BXDB, 11, "Invalid database name '%'."),
  /** Error code. */
  BXDB_ALTERDROP_X(BXDB, 12, "Database '%' cannot be both altered and dropped."),
  /** Error code. */
  BXDB_ONCE_X_X(BXDB, 12, "Database '%' can only be % once."),
  /** Error code. */
  BXDB_ONCEBACK_X_X(BXDB, 12, "Backup '%' can only be % once."),
  /** Error code. */
  BXDB_CREATEARGS_X_X(BXDB, 13, "Number of specified inputs and paths differs: % vs. %."),
  /** Error code. */
  BXDB_REPLACE_X(BXDB, 14, "Invalid target path: %."),
  /** Error code. */
  BXDB_NOBACKUP_X(BXDB, 15, "No backup found: %."),
  /** Error code. */
  BXDB_SAME_X(BXDB, 16, "Name of source and target database is equal: %."),
  /** Error code. */
  BXDB_PROP_X(BXDB, 17, "Unknown database property: %."),

  // Fetch Module

  /** Error code. */
  BXFE_IO_X(BXFE, 1, "%"),
  /** Error code. */
  BXFE_ENCODING_X(BXFE, 2, "Unknown encoding '%'."),

  // Fulltext Module

  /** Error code. */
  BXFT_MATCH(BXFT, 1, "Wildcards and fuzzy option cannot be specified both."),

  // HTML Module

  /** Error code. */
  BXHL_IO_X(BXHL, 1, "%"),

  // Inspection Module

  /** Error code. */
  INSPECT_UNKNOWN_X(INSPECT, "unknown", "Component '%' does not exist."),

  // Jobs Module

  /** Error code. */
  JOBS_UNKNOWN_X(JOBS, "unknown", "Unknown job: %."),
  /** Error code. */
  JOBS_RUNNING_X(JOBS, "running", "Result is not available yet: %."),
  /** Error code. */
  JOBS_OVERFLOW(JOBS, "overflow", "Too many queries queued."),
  /** Error code. */
  JOBS_RANGE_X(JOBS, "range", "Value out of range: %."),
  /** Error code. */
  JOBS_CONFLICT(JOBS, "conflict", "Either 'cache' or 'interval' option is allowed."),
  /** Error code. */
  JOBS_ID_EXISTS_X(JOBS, "id", "Job id already exists: %."),
  /** Error code. */
  JOBS_ID_INVALID_X(JOBS, "id", "Invalid job name: %."),
  /** Error code. */
  JOBS_SELF_X(JOBS, "self", "Cannot wait for own job: %"),

  // JSON Module

  /** Error code. */
  BXJS_PARSE_X_X_X(BXJS, 1, "JSON parser (%:%): %."),
  /** Error code. */
  BXJS_DUPLICATE_X(BXJS, 1, "JSON parser: (%:%): %."),
  /** Error code. */
  BXJS_PARSEML_X(BXJS, 1, "JsonML parser: %."),
  /** Error code. */
  BXJS_SERIAL_X(BXJS, 2, "JSON serializer: %."),
  /** Error code. */
  BXJS_INVALID_X(BXJS, 1, "'%':'%' is not supported by the target format."),

  // Output Module

  /** Error code. */
  ERRFORMAT_X_X(BXOU, 1, "%: %."),

  // Process Module

  /** Error code. */
  BXPR_TIMEOUT(BXPR, 9998, "The timeout was exceeded."),
  /** Error code. */
  BXPR_ENC_X(BXPR, 9999, "Unknown encoding '%'."),

  // Random Module

  /** Error code. */
  BXRA_BOUNDS_X(BXRA, 1, "Maximum value is out of bounds: %."),
  /** Error code. */
  BXRA_NUM_X(BXRA, 2, "Number of values is negative: %."),

  // Repository Module

  /** Error code. */
  BXRE_WHICH_X(BXRE, 1, "Package '%' does not exist."),
  /** Error code. */
  BXRE_URI_X(BXRE, 2, "URI is invalid: '%'."),
  /** Error code. */
  BXRE_NOTINST_X(BXRE, 3, "Required package '%' is not installed."),
  /** Error code. */
  BXRE_DESC_X(BXRE, 4, "Package descriptor: %."),
  /** Error code. */
  BXRE_INST_X(BXRE, 5, "Module % is already installed within another package."),
  /** Error code. */
  BXRE_PARSE_X_X(BXRE, 6, "Package '%' could not be parsed: %."),
  /** Error code. */
  BXRE_MAIN_X(BXRE, 6, "No 'Main-Class' attribute found: %/META-INF/MANIFEST.MF."),
  /** Error code. */
  BXRE_DELETE_X(BXRE, 7, "File '%' could not be deleted."),
  /** Error code. */
  BXRE_DEP_X_X(BXRE, 8, "Package '%' depends on package '%'."),
  /** Error code. */
  BXRE_VERSION(BXRE, 9, "Package version is not supported."),
  /** Error code. */
  BXRE_JARDESC_X(BXRE, 10, "JAR descriptor: %."),
  /** Error code. */
  BXRE_JARFAIL_X(BXRE, 11, "Reading JAR descriptor failed: %."),

  // XSLT Module

  /** Error code. */
  BXSL_ERROR_X(BXSL, 1, "%"),

  // SQL Module

  /** Error code. */
  BXSQ_ERROR_X(BXSQ, 1, "An SQL exception occurred: '%'"),
  /** Error code. */
  BXSQ_CONN_X(BXSQ, 2, "No opened connection with id %"),
  /** Error code. */
  BXSQ_STATE_X(BXSQ, 2, "No prepared statement with id %"),
  /** Error code. */
  BXSQ_PARAMS(BXSQ, 3, "Number of parameters differs from number of placeholders"),
  /** Error code. */
  BXSQ_TYPE(BXSQ, 4, "No parameter type supplied."),
  /** Error code. */
  BXSQ_ATTR_X(BXSQ, 5, "Not expected attribute: %"),
  /** Error code. */
  BXSQ_FORMAT_X(BXSQ, 6, "Illegal % format"),
  /** Error code. */
  BXSQ_DRIVER_X(BXSQ, 7, "Could not initialize supplied driver: '%'"),

  // Validation Module

  /** Error code. */
  BXVA_FAIL_X(BXVA, 1, "Validation failed: %"),
  /** Error code. */
  BXVA_START_X(BXVA, 2, "Validation could not be started: %"),
  /** Error code. */
  BXVA_RELAXNG_X(BXVA, 3, "RelaxNG validation is not available."),
  /** Error code. */
  BXVA_XSDVERSION_X(BXVA, 4, "No XML Schema implementation found for version '%'."),

  // Web Module

  /** Error code. */
  BXWE_INVALID_X(BXWE, 2, "%."),
  /** Error code. */
  BXWE_CODES_X(BXWE, 2, "URL contains invalid characters: %"),

  // XQuery Module

  /** Error code. */
  BXXQ_UPDATING(BXXQ, 1, "No updating expression allowed."),
  /** Error code. */
  BXXQ_NOUPDATE(BXXQ, 2, "Updating expression expected."),
  /** Error code. */
  BXXQ_PERM_X(BXXQ, 3, "%"),
  /** Error code. */
  BXXQ_PERM2_X(BASX, 3, "% permission required."),
  /** Error code. */
  BXXQ_TIMEOUT(BXXQ, 4, "The timeout was exceeded."),
  /** Error code. */
  BXXQ_MEMORY(BXXQ, 4, "The memory limit was exceeded."),
  /** Error code. */
  BXXQ_NESTED(BXXQ, 5, "Nested query evaluation is not allowed."),
  /** Error code. */
  BXXQ_UNEXPECTED_X(BXXQ, 6, "Unexpected error: %"),

  // Unit Module

  /** Error code. */
  UNIT_ASSERT(UNIT, 1, "Assertion failed."),
  /** Error code. */
  UNIT_MESSAGE_X(UNIT, 1, "%"),
  /** Error code. */
  UNIT_ASSERT_EQUALS_X_X_X(UNIT, 1, "Item %: % expected, % returned."),
  /** Error code. */
  UNIT_ARGS_X(UNIT, 2, "Test function '%' must have no arguments."),
  /** Error code. */
  UNIT_PRIVATE_X(UNIT, 3, "Test function '%' must be public."),

  // User Module

  /** Error code. */
  USER_NAME_X(USER, "name", "Invalid user name: '%'."),
  /** Error code. */
  USER_PATTERN_X(USER, "pattern", "Invalid database pattern: '%'."),
  /** Error code. */
  USER_PERMISSION_X(USER, "permission", "Invalid permission: '%'."),
  /** Error code. */
  USER_UNKNOWN_X(USER, "unknown", "User '%' does not exist."),
  /** Error code. */
  USER_PASSWORD_X(USER, "password", "Wrong password supplied for user '%'."),
  /** Error code. */
  USER_ADMIN(USER, "admin", "User 'admin' cannot be modified."),
  /** Error code. */
  USER_EQUAL_X(USER, "equal", "Name of old and new user is equal: %."),
  /** Error code. */
  USER_LOCAL(USER, "local", "Local permission can only be 'none', 'read' or 'write'."),
  /** Error code. */
  USER_LOGGEDIN_X(USER, "logged-in", "User '%' is currently logged in."),
  /** Error code. */
  USER_UPDATE_X_X(USER, "update", "User '%' can only be % once."),
  /** Error code. */
  USER_SAMEPAT_X(USER, "update", "Pattern '%' is specified more than once."),
  /** Error code. */
  USER_SAMEPERM_X_X(USER, "update", "User '%' can only be % once."),
  /** Error code. */
  USER_CONFLICT_X(USER, "conflict", "User '%' cannot be both altered and dropped."),
  /** Error code. */
  USER_INFO_X(USER, "info", "Info can only be % once."),

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
  CX_INVHASH(CX, 13, "Hashing algorithm is not supported."),
  /** Error code. */
  CX_ENC(CX, 14, "The encoding method is not supported."),
  /** Error code. */
  CX_NOSIG(CX, 15, "Cannot find signature element."),
  /** Error code. */
  CX_NOPAD(CX, 16, "No such padding."),
  /** Error code. */
  CX_BADPAD(CX, 17, "Incorrect padding."),
  /** Error code. */
  CX_ENCTYP(CX, 18, "Encryption type is not supported."),
  /** Error code. */
  CX_KEYINV(CX, 19, "Secret key is invalid."),
  /** Error code. */
  CX_ILLBLO(CX, 20, "Illegal block size."),
  /** Error code. */
  CX_INVALGO(CX, 21, "Algorithm is not supported."),
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

  /** Error code. */
  HASH_ALG_X(HASH, 1, "Algorithm not supported: '%'."),

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

  /** Error code. */
  UTIL_NEGATIVE_X(UTIL, "negative", "Index '%' is negative."),

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
  INVALUE_X_X(FOCA, 2, "Cannot cast to %: \"%\"."),
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
  RESDIR_X(FODC, 2, "URI '%' points to a directory."),
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
  DIFFMAND_X(FODF, 1310, "Mandatory digits must be of the same group: '%'."),
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
  JSON_PARSE_X(FOJS, 1, "%"),
  /** Error code. */
  JSON_DUPLICATE_X(FOJS, 3, "%"),
  /** Error code. */
  MERGE_DUPLICATE_X(FOJS, 3, "Key % occurs more than once."),
  /** Error code. */
  JSON_OPT_X(FOJS, 5, "%"),
  /** Error code. */
  JSON_FUNC_OPT_X_X(FOJS, 5, "% expected, % found."),
  /** Error code. */
  JSON_INVALID_X(FOJS, 6, "%"),
  /** Error code. */
  JSON_ESCAPE_X(FOJS, 7, "Invalid escape sequence: %."),

  /** Error code. */
  NSDECL_X(FONS, 4, "No namespace declared for prefix '%'."),

  /** Error code. */
  INVALIDZONE_X(FORG, 1, "Invalid timezone: %."),
  /** Error code. */
  FUNCAST_X_X(FORG, 1, "Cannot cast to %: %."),
  /** Error code. */
  FUNCCAST_X_X_X(FORG, 1, "Cannot cast % to %: %."),
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
  UPDBOPTERR_X(FOUP, 2, "%"),

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
  WHICHCHARS_X(FOUT, 1200, "Resource contains invalid input: %."),

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
  FTDUP_X(FTST, 19, "Match option '%' was declared twice."),

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
  SERMAP_X(SEPM, 17, "Character map '%' is not defined."),
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
  NOCTX_X(XPDY, 2, "%: no context value bound."),
  /** Error code. */
  VAREMPTY_X(XPDY, 2, "No value assigned to %."),
  /** Error code. */
  CTXNODE(XPDY, 50, "Root of the context value must be a document node."),
  /** Error code. */
  NOTREAT_X_X_X(XPDY, 50, "Cannot treat % as %: %."),

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
  PIWRONG(XPST, 3, "Expecting name of processing-instruction."),
  /** Error code. */
  NOFTSELECT_X(XPST, 3, "Expecting quote or opening curly brace%."),
  /** Error code. */
  FUNCMISS_X(XPST, 3, "Expecting closing bracket: %."),
  /** Error code. */
  MAPTAAT_X(XPST, 3, "Expecting atomic key type for map, found '%'."),
  /** Error code. */
  TYPEINVALID(XPST, 3, "Expecting type declaration."),
  /** Error code. */
  NODECLFORM_X(XPST, 3, "Decimal-format property '%' is invalid."),
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
  FLWORRETURN(XPST, 3, "Incomplete FLWOR expression: expecting 'return'."),
  /** Error code. */
  NOSOME(XPST, 3, "Incomplete quantifier expression."),
  /** Error code. */
  IFPAR(XPST, 3, "Expecting '(' after 'if' expression."),
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
  PIXML_X(XPST, 3, "Processing instruction has illegal name: '%'."),
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
  SIMPLETYPE_X(XPST, 3, "Simple type expected, '%(' found."),
  /** Error code. */
  KEYSPEC(XPST, 3, "No specifier after lookup operator: '%'."),
  /** Error code. */
  ARROWSPEC(XPST, 3, "No specifier after arrow operator: '%'."),

  /** Error code. */
  VARUNDEF_X(XPST, 8, "Undefined variable %."),
  /** Error code. */
  CIRCREF_X(XPST, 8, "Static variable references itself: %"),
  /** Error code. */
  VARPRIVATE_X(XPST, 8, "Variable % is not visible from this module."),
  /** Error code. */
  TYPEUNDEF_X(XPST, 8, "Undefined type '%'."),
  /** Error code. */
  SCHEMAINV_X(XPST, 8, "Undefined schema name '%'."),

  /** Error code. */
  FUNCPRIVATE_X(XPST, 17, "Function not visible: %."),
  /** Error code. */
  FUNCSIMILAR_X_X(XPST, 17, "Unknown function: % (similar: %)."),
  /** Error code. */
  FUNCARGNUM_X_X(XPST, 17, "%: % supplied."),
  /** Error code. */
  FUNCTYPES_X_X_X(XPST, 17, "%: % supplied, % expected."),
  /** Error code. */
  WHICHFUNC_X(XPST, 17, "Unknown function: %."),
  /** Error code. */
  WHICHJAVA_X_X_X(XPST, 17, "Java function is unknown: %:%#%."),
  /** Error code. */
  WHICHCLASS_X(XPST, 17, "Java class is unknown: %."),
  /** Error code. */
  JAVAAMB_X_X_X(XPST, 17, "Java function is ambiguous: %:%#%."),
  /** Error code. */
  JAVACONSAMB_X(XPST, 17, "Java constructor is ambiguous: %."),
  /** Error code. */
  FUNCNOIMPL_X(XPST, 17, "External function not implemented: %."),
  /** Error code. */
  JAVAINIT_X_X(XPST, 17, "%: %."),

  /** Error code. */
  TYPEUNKNOWN_X(XPST, 51, "Unknown type: %."),
  /** Error code. */
  CASTUNKNOWN_X(XPST, 80, "Invalid cast type: %."),
  /** Error code. */
  NOURI_X(XPST, 81, "No namespace declared for '%'."),
  /** Error code. */
  NSMISS_X(XPST, 81, "QName '%' has no namespace."),

  /** Error code. */
  WHICHCONSTR_X_X(XPTY, 4, "Unknown Java constructor: %(%)."),
  /** Error code. */
  WHICHMETHOD_X_X(XPTY, 4, "Unknown Java method: %(%)."),
  /** Error code. */
  JAVAARGS_X_X_X(XPTY, 4, "Function %(%) cannot be called with (%)."),
  /** Error code. */
  JAVAERROR_X_X_X(XPTY, 4, "%(%): %."),

  /** Error code. */
  JAVAARITY_X_X_X_X(XPTY, 4, "Java function %:%#%: % supplied."),
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
  CPIWRONG_X_X(XPTY, 4, "String or NCName expected, % found: %."),
  /** Error code. */
  INVCAST_X_X_X(XPTY, 4, "Cannot cast % to %: %."),
  /** Error code. */
  INVPROMOTE_X(XPTY, 4, "%."),
  /** Error code. */
  INVPROMOTE_X_X_X(XPTY, 4, "%: Cannot promote % to %."),
  /** Error code. */
  CALCTYPE_X_X_X(XPTY, 4, "% not defined for % and %."),
  /** Error code. */
  INVFUNCITEM_X_X(XPTY, 4, "Function expected, % found: %."),
  /** Error code. */
  NOPAREN_X_X(XPTY, 4, "No parenthesis expected after %."),
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
  LOOKUP_X(XPTY, 4, "Input of lookup operator is not a map or array: %."),
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
  NSSENS_X_X(XPTY, 117, "Cannot cast % to %."),

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
  IMPLSCHEMA(XQST, 9, "Schema import not supported."),
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
  FUNCDUPL_X(XQST, 39, "Duplicate function argument %."),
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
  TYPE30_X(XQST, 52, "Unknown cast type: %."),
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
  INVDECSINGLE_X_X(XQST, 97, "Decimal-format property must be a single character: %='%'."),
  /** Error code. */
  INVDECZERO_X(XQST, 97, "Zero-digit property must be Unicode digit with value zero: '%'."),
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
  OUTMAP_X(XQST, 109, "Character map '%' is not defined."),
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
  OUTDOC_X(XQST, 119, "Serialization document '%' cannot be parsed."),
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
  UPNOTCOPIED_X(XUDY, 14, "Node was not created by copy clause: %."),
  /** Error code. */
  UPMULTREN_X(XUDY, 15, "Node can only be renamed once: %."),
  /** Error code. */
  UPPATHREN_X(XUDY, 15, "Path can only be renamed once: '%'."),
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
  UPMODIFY(XUST, 2, "Expressions must all be updating or return an empty sequence."),
  /** Error code. */
  UPEXPECTF(XUST, 2, "Function body must be an updating expression."),
  /** Error code. */
  DUPLREVAL(XUST, 3, "Duplicate 'revalidation' declaration."),
  /** Error code. */
  NOREVAL(XUST, 26, "Revalidation mode not supported."),
  /** Error code. */
  UUPFUNCTYPE(XUST, 28, "No return type allowed in updating functions."),

  /** Error code. */
  UPNOATTRPER_X(XUTY, 4, "Attribute does not follow root element: %."),
  /** Error code. */
  UPTRGTYP_X(XUTY, 5, "Target must be element or document: %."),
  /** Error code. */
  UPTRGSNGL_X(XUTY, 5, "Target must be single node: %."),
  /** Error code. */
  UPTRGTYP2_X(XUTY, 6, "Target must be element, text, comment or pi: %."),
  /** Error code. */
  UPTRGSNGL2_X(XUTY, 6, "Target must be single node: %."),
  /** Error code. */
  UPTRGDELEMPT_X(XUTY, 7, "Target must be node: %."),
  /** Error code. */
  UPTRGNODE_X(XUTY, 8, "Target must be element, text, attribute, comment or pi: %."),
  /** Error code. */
  UPTRGSINGLE_X(XUTY, 8, "Target must be single node: %."),
  /** Error code. */
  UPWRELM_X(XUTY, 10, "Node cannot be replaced with attribute: %."),
  /** Error code. */
  UPWRATTR_X(XUTY, 11, "Target is no attribute: %."),
  /** Error code. */
  UPWRTRGTYP_X(XUTY, 12, "Target must be element, attribute or pi: %."),
  /** Error code. */
  UPWRTRGSINGLE_X(XUTY, 12, "Target must be single node: %."),
  /** Error code. */
  UPSINGLE_X_X(XUTY, 13, "Value of $% must be single node: %."),
  /** Error code. */
  UPSOURCE_X(XUTY, 13, "Source must be node: %."),
  /** Error code. */
  UPATTELM2_X(XUTY, 22, "Attribute cannot be added to %.");

  /** Cached enums (faster). */
  private static final QueryError[] VALUES = values();

  /** Error code. */
  public final String code;
  /** Error URI. */
  private final byte[] uri;
  /** Error prefix. */
  private final String prefix;
  /** Error description. */
  public final String desc;

  /**
   * Constructor.
   * @param type error type
   * @param code error code
   * @param desc description
   */
  QueryError(final ErrType type, final String code, final String desc) {
    this.code = code;
    this.desc = desc;
    uri = type.uri;
    prefix = type.prefix;
  }

  /**
   * Constructor.
   * @param type error type
   * @param number error number
   * @param desc description
   */
  QueryError(final ErrType type, final int number, final String desc) {
    final StringBuilder sb = new StringBuilder(8).append(type);
    final String n = Integer.toString(number);
    final int s  = 4 - n.length();
    for(int i = 0; i < s; i++) sb.append('0');
    code = sb.append(n).toString();
    uri = type.uri;
    prefix = type.prefix;
    this.desc = desc;
  }

  /**
   * Throws a query exception. If {@link InputInfo#internal()} returns {@code true},
   * a static error instance ({@link QueryException#ERROR}) will be returned.
   * @param info input info (can be {@code null})
   * @param ext extended info
   * @return query exception
   */
  public QueryException get(final InputInfo info, final Object... ext) {
    return info != null && info.internal() ? QueryException.ERROR :
      new QueryException(info, this, ext);
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
   * @author BaseX Team 2005-17, BSD License
   * @author Leo Woerteler
   */
  public enum ErrType {
    // Project errors

    /** BASX Error type. */ BASX(BXERR_PREFIX, BXERRORS_URI),

    /** BXCO Error type. */ BXCO(BXERR_PREFIX, BXERRORS_URI),
    /** BXCS Error type. */ BXCS(BXERR_PREFIX, BXERRORS_URI),
    /** BXDB Error type. */ BXDB(BXERR_PREFIX, BXERRORS_URI),
    /** BXFE Error type. */ BXFE(BXERR_PREFIX, BXERRORS_URI),
    /** BXFT Error type. */ BXFT(BXERR_PREFIX, BXERRORS_URI),
    /** BXHL Error type. */ BXHL(BXERR_PREFIX, BXERRORS_URI),
    /** BXJS Error type. */ BXJS(BXERR_PREFIX, BXERRORS_URI),
    /** BXOU Error type. */ BXOU(BXERR_PREFIX, BXERRORS_URI),
    /** BXPR Error type. */ BXPR(BXERR_PREFIX, BXERRORS_URI),
    /** BXRA Error type. */ BXRA(BXERR_PREFIX, BXERRORS_URI),
    /** BXRE Error type. */ BXRE(BXERR_PREFIX, BXERRORS_URI),
    /** BXSL Error type. */ BXSL(BXERR_PREFIX, BXERRORS_URI),
    /** BXSQ Error type. */ BXSQ(BXERR_PREFIX, BXERRORS_URI),
    /** BXVA Error type. */ BXVA(BXERR_PREFIX, BXERRORS_URI),
    /** BXWE Error type. */ BXWE(BXERR_PREFIX, BXERRORS_URI),
    /** BXXQ Error type. */ BXXQ(BXERR_PREFIX, BXERRORS_URI),
    /** HASH Error type. */ HASH(BXERR_PREFIX, BXERRORS_URI),

    /** Error type. */ ADMIN(ADMIN_PREFIX, ADMIN_URI),
    /** Error type. */ ARCHIVE(ADMIN_PREFIX, ARCHIVE_URI),
    /** Error type. */ CLIENT(CLIENT_PREFIX, CLIENT_URI),
    /** Error type. */ INSPECT(INSPECT_PREFIX, INSPECT_URI),
    /** Error type. */ JOBS(JOBS_PREFIX, JOBS_URI),
    /** Error type. */ UNIT(UNIT_PREFIX, UNIT_URI),
    /** Error type. */ UTIL(UTIL_PREFIX, UTIL_URI),
    /** Error type. */ USER(USER_PREFIX, USER_URI),

    // EXPath errors

    /** Error type. */ BIN(BIN_PREFIX,     BIN_URI),
    /** Error type. */ CX(EXPERR_PREFIX,   EXPERROR_URI),
    /** Error type. */ FILE(FILE_PREFIX,   FILE_URI),
    /** Error type. */ HC(EXPERR_PREFIX,   EXPERROR_URI),
    /** Error type. */ ZIP(EXPERR_PREFIX,  EXPERROR_URI),

    // W3 errors

    /** FOAP Error type. */ FOAP,
    /** FOAR Error type. */ FOAR,
    /** FOAY Error type. */ FOAY,
    /** FOCA Error type. */ FOCA,
    /** FOCH Error type. */ FOCH,
    /** FODC Error type. */ FODC,
    /** FODF Error type. */ FODF,
    /** FODT Error type. */ FODT,
    /** FOFD Error type. */ FOFD,
    /** FOER Error type. */ FOER,
    /** FOJS Error type. */ FOJS,
    /** FONS Error type. */ FONS,
    /** FORG Error type. */ FORG,
    /** FORX Error type. */ FORX,
    /** FOTY Error type. */ FOTY,
    /** FOUP Error type. */ FOUP,
    /** FOUT Error type. */ FOUT,
    /** FTDY Error type. */ FTDY,
    /** FTST Error type. */ FTST,
    /** SENR Error type. */ SENR,
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
     * @param u error URI
     */
    ErrType(final byte[] pref, final byte[] u) {
      prefix = Token.string(pref);
      uri = u;
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
   * Checks if the error code is of the specified type.
   * @param type type
   * @return result of check
   */
  public final boolean is(final ErrType type) {
    return code.startsWith(type.name());
  }

  /**
   * Returns an error for the specified name.
   * @param name error name
   * @param msg error message
   * @param info input info
   * @return exception or null
   */
  public static QueryException get(final String name, final String msg, final InputInfo info) {
    for(final QueryError err : VALUES) {
      if(err.toString().equals(name)) return new QueryException(info, err.qname(), msg).error(err);
    }
    return null;
  }

  /**
   * Throws a comparison exception.
   * @param item1 first item
   * @param item2 second item
   * @param info input info
   * @return query exception
   */
  public static QueryException diffError(final Item item1, final Item item2, final InputInfo info) {
    final Type t1 = item1.type, t2 = item2.type;
    return t1 == t2 ? CMPTYPE_X.get(info, t1, item1) : CMPTYPES_X_X.get(info, t1, t2);
  }

  /**
   * Throws a type exception.
   * @param info input info
   * @param expr expression
   * @param type target type
   * @param name name (can be {@code null})
   * @return query exception
   */
  public static QueryException typeError(final Expr expr, final SeqType type, final QNm name,
      final InputInfo info) {

    final TokenBuilder tb = new TokenBuilder("Cannot ");
    if(name == null) {
      tb.add("return ").addExt(expr.seqType()).add(" as ");
    } else {
      tb.add("promote ").addExt(expr.seqType()).add(" to ").add('$').add(name.string()).add(" as ");
    }
    return INVPROMOTE_X.get(info, tb.addExt(type).add(": ").add(chop(expr, info)).finish());
  }

  /**
   * Throws a type cast exception.
   * @param value value
   * @param type expression cast type
   * @param info input info
   * @return query exception
   */
  public static QueryException castError(final Value value, final Type type, final InputInfo info) {
    return INVCAST_X_X_X.get(info, value.type, type, value);
  }

  /**
   * Throws a type cast exception.
   * @param type expression cast type
   * @param value value
   * @param info input info
   * @return query exception
   */
  public static QueryException castError(final Type type, final Object value,
      final InputInfo info) {
    return FUNCAST_X_X.get(info, type, chop(value, info));
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
   * @param info input info
   * @return query exception
   */
  public static QueryException numberError(final Item item, final InputInfo info) {
    return NONUMBER_X_X.get(info, item.type, item);
  }

  /**
   * Throws an invalid value exception.
   * @param type expected type
   * @param value value
   * @param info input info
   * @return query exception
   */
  public static QueryException valueError(final Type type, final byte[] value,
      final InputInfo info) {
    return INVALUE_X_X.get(info, type, value);
  }

  /**
   * Returns a plural suffix or an empty string.
   * @param number long number
   * @return suffix
   */
  public static String arguments(final long number) {
    final StringBuilder sb = new StringBuilder().append(number).append(" argument");
    if(number != 1) sb.append('s');
    return sb.toString();
  }

  /**
   * Chops the specified value to a maximum size.
   * @param value value
   * @param info input info (can be {@code null}; an empty string will be returned if
   * {@link InputInfo#internal()} returns null)
   * @return chopped or empty string
   */
  public static byte[] chop(final Object value, final InputInfo info) {
    return info != null && info.internal() ? Token.EMPTY :
           value instanceof byte[] ? chop((byte[]) value, info) :
           chop(value.toString(), info);
  }

  /**
   * Chops the specified string to a maximum size.
   * @param string string
   * @param info input info (can be {@code null}; an empty string will be returned if
   * {@link InputInfo#internal()} returns null)
   * @return chopped or empty string
   */
  public static byte[] chop(final String string, final InputInfo info) {
    return info != null && info.internal() ? Token.EMPTY : chop(Token.token(string), info);
  }

  /**
   * Chops the specified token to a maximum size.
   * @param token token
   * @param info input info (can be {@code null}; an empty string will be returned if
   * {@link InputInfo#internal()} returns null)
   * @return chopped or empty string
   */
  public static byte[] chop(final byte[] token, final InputInfo info) {
    if(info != null && info.internal()) return Token.EMPTY;

    final TokenBuilder tb = new TokenBuilder();
    byte l = 0;
    for(byte b : token) {
      final int ts = tb.size();
      if(ts == 40) {
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
