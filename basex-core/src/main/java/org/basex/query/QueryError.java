package org.basex.query;

import static org.basex.query.QueryError.ErrType.*;
import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * This class contains all query error messages.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum QueryError {

  // General errors

  /** BASX0000. */
  BASX_GENERIC_X(BASX, 0, "%"),
  /** BASX0001. */
  BASX_PERM_X(BASX, 1, "% permission required."),
  /** BASX0002. */
  BASX_OPTIONS_X(BASX, 2, "Unknown database option '%'."),
  /** BASX0002. */
  BASX_VALUE_X_X(BASX, 2, "Database option '%' cannot be set to '%'."),
  /** BASX0003. */
  BASX_RESTXQ_X(BASX, 3, "%"),
  /** BASX0004. */
  BASX_DBTRANSFORM(BASX, 4, "No database updates allowed within transform expression."),
  /** BASX0005. */
  BASX_STACKOVERFLOW(BASX, 5, "Stack Overflow: Try tail recursion?"),
  /** BASX0006. */
  BASX_ANNOT_X_X(BASX, 6, "Annotation %% is unknown."),
  /** BASX0006. */
  BASX_ANNNUM_X_X_X(BASX, 6, "%: % argument% supplied."),
  /** BASX0006. */
  BASX_ANNTYPE_X_X_X(BASX, 6, "%: % expected, % found."),
  /** BASX0007. */
  BASX_TWICE_X_X(BASX, 6, "Annotation %% was declared twice."),

  /** XUST0002. */
  BASEX_MOD(XUST, 2, "All transform expressions must be updating or return an empty sequence."),

  // Client module

  /** BXCL0001. */
  BXCL_CONN_X(BXCL, 1, "Connection failed: %"),
  /** BXCL0002. */
  BXCL_NOTAVL_X(BXCL, 2, "Session with ID % is not available or has been closed."),
  /** BXCL0003. */
  BXCL_COMM_X(BXCL, 3, "An error occurred: %"),
  /** BXCL0004. */
  BXCL_COMMAND_X(BXCL, 4, "Command could not be executed: %"),
  /** BXCL0005. */
  BXCL_QUERY_X(BXCL, 5, "Query could not be executed: %"),

  // Conversion module

  /** BXCO0001. */
  BXCO_STRING_X(BXCO, 1, "String conversion: %."),
  /** BXCO0001. */
  BXCO_BASE64_X_X(BXCO, 1, "Conversion of '%' to encoding '%' failed."),
  /** BXCO0002. */
  BXCO_ENCODING_X(BXCO, 2, "Unknown encoding '%'."),

  // CSV module

  /** BXCS0001. */
  BXCS_PARSE_X(BXCS, 1, "CSV parser: %."),
  /** BXCS0002. */
  BXCS_SERIAL_X(BXCS, 2, "CSV serializer: %."),

  // Database module

  /** BXDB0001. */
  BXDB_NODB_X_X(BXDB, 1, "Database node expected, % found: %."),
  /** BXDB0002. */
  BXDB_OPEN_X(BXDB, 2, "%"),
  /** BXDB0002. */
  BXDB_WHICH_X(BXDB, 2, "Database '%' not found."),
  /** BXDB0002. */
  BXDB_WHICHBACK_X(BXDB, 2, "No backup found for database '%'."),
  /** BXDB0003. */
  BXDB_MEM_X(BXDB, 3, "Operation requires database '%' to be persistent."),
  /** BXDB0004. */
  BXDB_INDEX_X(BXDB, 4, "Database '%' has no % index."),
  /** BXDB0006. */
  BXDB_NODOC_X(BXDB, 6, "Database path '%' yields no documents."),
  /** BXDB0006. */
  BXDB_SINGLE_X(BXDB, 6, "Database path '%' points to more than one document."),
  /** BXDB0007. */
  BXDB_OPENED_X(BXDB, 7, "Database '%' cannot be updated, as it is opened by another process."),
  /** BXDB0007. */
  BXDB_LOCK_X(BXDB, 7, "%"),
  /** BXDB0008. */
  BXDB_RENAME_X(BXDB, 8, "Invalid target path: %."),
  /** BXDB0009. */
  BXDB_RANGE_X_X_X(BXDB, 9, "Database '%' has no node with % value %."),
  /** BXDB0010. */
  BXDB_EVENT_X(BXDB, 10, "Event '%' is unknown."),
  /** BXDB0011. */
  BXDB_NAME_X(BXDB, 11, "Invalid database name '%'."),
  /** BXDB0012. */
  BXDB_ALTERDROP_X(BXDB, 12, "Database '%' cannot be both altered and dropped."),
  /** BXDB0012. */
  BXDB_ONCE_X_X(BXDB, 12, "Database '%' can only be % once."),
  /** BXDB0012. */
  BXDB_ONCEBACK_X_X(BXDB, 12, "Backup '%' can only be % once."),
  /** BXDB0013. */
  BXDB_CREATEARGS_X_X(BXDB, 13, "Number of specified inputs and paths differs: % vs. %."),
  /** BXDB0014. */
  BXDB_REPLACE_X(BXDB, 14, "Invalid target path: %."),
  /** BXDB0015. */
  BXDB_NOBACKUP_X(BXDB, 15, "No backup found: %."),
  /** BXDB0016. */
  BXDB_SAME_X(BXDB, 16, "Name of source and target database is equal: %."),

  // Fetch module

  /** BXFE0001. */
  BXFE_IO_X(BXFE, 1, "%"),
  /** BXFE0002. */
  BXFE_ENCODING_X(BXFE, 2, "Unknown encoding '%'."),

  // Fulltext module

  /** BXFT0001. */
  BXFT_MATCH(BXFT, 1, "Wildcards and fuzzy option cannot be specified both."),

  // HTML module

  /** BXHL0001. */
  BXHL_IO_X(BXHL, 1, "%"),

  // JSON module

  /** BXJS0001. */
  BXJS_PARSE_X_X_X(BXJS, 1, "JSON parser (%:%): %."),
  /** BXJS0001. */
  BXJS_DUPLICATE_X(BXJS, 1, "JSON parser: (%:%): %."),
  /** BXJS0001. */
  BXJS_PARSEML_X(BXJS, 1, "JsonML parser: %."),
  /** BXJS0002. */
  BXJS_SERIAL_X(BXJS, 2, "JSON serializer: %."),

  // Process module

  /** BXPR9999. */
  BXPR_ENC_X(BXPR, 9999, "Unknown encoding '%'."),

  // Random module

  /** BXRA0001. */
  BXRA_BOUNDS_X(BXRA, 1, "Maximum value is out of bounds: %."),
  /** BXRA0001. */
  BXRA_NUM_X(BXRA, 2, "Number of values is negative: %."),

  // Repository module

  /** BXRE0001. */
  BXRE_WHICH_X(BXRE, 1, "Package '%' does not exist."),
  /** BXRE0002. */
  BXRE_URI_X(BXRE, 2, "URI is invalid or has no path component: '%'."),
  /** BXRE0003. */
  BXRE_NOTINST_X(BXRE, 3, "Required package '%' is not installed."),
  /** BXRE0004. */
  BXRE_DESC_X(BXRE, 4, "Package descriptor: %."),
  /** BXRE0005. */
  BXRE_INST_X(BXRE, 5, "Module % is already installed within another package."),
  /** BXRE0006. */
  BXRE_PARSE_X_X(BXRE, 6, "Package '%' could not be parsed: %."),
  /** BXRE0007. */
  BXRE_DELETE_X(BXRE, 7, "File '%' could not be deleted."),
  /** BXRE0008. */
  BXRE_DEP_X_X(BXRE, 8, "Package '%' depends on package '%'."),
  /** BXRE0009. */
  BXRE_VERSION(BXRE, 9, "Package version is not supported."),
  /** BXRE0010. */
  BXRE_JARDESC_X(BXRE, 10, "JAR descriptor: %."),
  /** BXRE0011. */
  BXRE_JARFAIL_X(BXRE, 11, "Reading JAR descriptor failed: %."),

  // XSLT module

  /** BXSL0001. */
  BXSL_ERROR_X(BXSL, 1, "%"),

  // SQL module

  /** BXSQ0001. */
  BXSQ_ERROR_X(BXSQ, 1, "An SQL exception occurred: '%'"),
  /** BXSQ0002. */
  BXSQ_CONN_X(BXSQ, 2, "No opened connection with id %"),
  /** BXSQ0002. */
  BXSQ_STATE_X(BXSQ, 2, "No prepared statement with id %"),
  /** BXSQ0003. */
  BXSQ_PARAMS(BXSQ, 3, "Number of parameters differs from number of placeholders"),
  /** BXSQ0004. */
  BXSQ_TYPE(BXSQ, 4, "No parameter type supplied."),
  /** BXSQ0005. */
  BXSQ_ATTR_X(BXSQ, 5, "Not expected attribute: %"),
  /** BXSQ0006. */
  BXSQ_FORMAT_X(BXSQ, 6, "Illegal % format"),
  /** BXSQ0007. */
  BXSQ_DRIVER_X(BXSQ, 7, "Could not initialize supplied driver: '%'"),

  // Validation module

  /** BXVA0001. */
  BXVA_FAIL_X(BXVA, 1, "Validation failed. %"),
  /** BXVA0002. */
  BXVA_START_X(BXVA, 2, "Validation could not be started. %"),

  // XQuery module

  /** BXXQ0001. */
  BXXQ_UPDATING(BXXQ, 1, "No updating expression allowed."),
  /** BXXQ0002. */
  BXXQ_NOUPDATE(BXXQ, 2, "Updating expression expected."),
  /** BXXQ0003. */
  BXXQ_PERM_X(BXXQ, 3, "%"),
  /** BXXQ0003. */
  BXXQ_PERM2_X(BASX, 3, "% permission required."),
  /** BXXQ0004. */
  BXXQ_STOPPED(BXXQ, 4, "Timeout or assigned memory was exceeded."),

  // Unit module

  /** UNIT0001. */
  UNIT_ASSERT(UNIT, 1, "Assertion failed."),
  /** UNIT0001. */
  UNIT_MESSAGE_X(UNIT, 1, "%"),
  /** UNIT0001. */
  UNIT_ASSERT_EQUALS_X_X_X(UNIT, 1, "Item %: % expected, % returned."),
  /** UNIT0002. */
  UNIT_ARGS_X(UNIT, 2, "Test function '%' must have no arguments."),
  /** UNIT0003. */
  UNIT_PRIVATE_X(UNIT, 3, "Test function '%' must be public."),

  // User Module

  /** User error. */
  USER_NAME_X(USER, "name", "Invalid user name: %."),
  /** User error. */
  USER_PATTERN_X(USER, "pattern", "Invalid database pattern: %."),
  /** User error. */
  USER_PERMISSION_X(USER, "permission", "Invalid permission: %."),
  /** User error. */
  USER_UNKNOWN_X(USER, "unknown", "User '%' does not exist."),
  /** User error. */
  USER_ADMIN(USER, "admin", "User 'admin' cannot be modified."),
  /** User error. */
  USER_EQUAL_X(USER, "equal", "Name of old and new user is equal: %."),
  /** User error. */
  USER_LOCAL(USER, "local", "Local permission can only be 'none', 'read' or 'write'."),
  /** User error. */
  USER_LOGGEDIN_X(USER, "logged-in", "User '%' is currently logged in."),
  /** User error. */
  USER_UPDATE_X_X(USER, "update", "User '%' can only be % once."),
  /** User error. */
  USER_UPDATE_X_X_X(USER, "update", "Pattern '%' of user '%' can only be % once."),
  /** User error. */
  USER_CONFLICT_X(USER, "conflict", "User '%' cannot be both altered and dropped."),

  // EXPath modules

  /** ARCH0001. */
  ARCH_DIFF_X_X(ARCH, 1, "Number of supplied entries and contents differs: % vs. %."),
  /** ARCH0002. */
  ARCH_UNKNOWN(ARCH, 2, "Packing format not supported."),
  /** ARCH0002. */
  ARCH_SUPP_X_X(ARCH, 2, "% not supported: '%'."),
  /** ARCH0003. */
  ARCH_EMPTY(ARCH, 3, "Name of ZIP entry must not be empty."),
  /** ARCH0003. */
  ARCH_LEVEL_X(ARCH, 3, "Invalid compression level: '%'."),
  /** ARCH0003. */
  ARCH_DATETIME_X(ARCH, 3, "xs:dateTime value is invalid : '%'."),
  /** ARCH0004. */
  ARCH_ENCODING_X(ARCH, 4, "Unknown encoding '%'."),
  /** ARCH0004. */
  ARCH_ENCODE_X(ARCH, 4, "String conversion: %."),
  /** ARCH0005. */
  ARCH_MODIFY_X(ARCH, 5, "Entries of % archive cannot be modified."),
  /** ARCH0006. */
  ARCH_ONE_X(ARCH, 6, "% archives are limited to a single entry."),
   /** ARCH9999. */
  ARCH_FAIL_X(ARCH, 9999, "Operation failed: %."),

  /** Binary error. */
  BIN_DLA_X_X(BIN, "differing-length-arguments", "Inputs are of different length (%/%)."),
  /** Binary error. */
  BIN_IOOR_X_X(BIN, "index-out-of-range", "Index '%' is out of range (0-%)."),
  /** Binary error. */
  BIN_NS_X(BIN, "negative-size", "Size '%' is negative."),
  /** Binary error. */
  BIN_OOR_X(BIN, "octet-out-of-range", "Octet '%' is out of range."),
  /** Binary error. */
  BIN_NNC(BIN, "non-numeric-character", "Invalid character in constructor string."),
  /** Binary error. */
  BIN_UE_X(BIN, "unknown-encoding", "Unknown encoding '%'."),
  /** Binary error. */
  BIN_CE_X(BIN, "conversion-error", "%."),
  /** Binary error. */
  BIN_USO_X(BIN, "unknown-significance-order", "Unknown octet-order value: '%'."),

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
  CX_KSNULL_X(CX, 7, "Key store is null: %"),
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
  CX_ALINV_X(CX, 23, "Invalid certificate alias %."),
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

  /** File error. */
  FILE_NOT_FOUND_X(FILE, "not-found", "File '%' does not exist."),
  /** File error. */
  FILE_EXISTS_X(FILE, "exists", "File '%' already exists."),
  /** File error. */
  FILE_NO_DIR_X(FILE, "no-dir", "Path '%' is no directory."),
  /** File error. */
  FILE_IS_DIR_X(FILE, "is-dir", "Path '%' is a directory."),
  /** File error. */
  FILE_ID_DIR2_X(FILE, "is-dir", "Path '%' is a non-empty directory."),
  /** File error. */
  FILE_UNKNOWN_ENCODING_X(FILE, "unknown-encoding", "Unknown encoding '%'."),
  /** File error. */
  FILE_OUT_OF_RANGE_X_X(FILE, "out-of-range", "Requested file chunk [%,%] exceeds file bounds."),
  /** File error. */
  FILE_INVALID_PATH_X(FILE, "invalid-path", "Invalid file path: '%'."),
  /** File error. */
  FILE_IO_ERROR_X(FILE, "io-error", "%"),
  /** File error. */
  FILE_IE_ERROR_ACCESS_X(FILE, "io-error", "Access to '%' is denied."),

  /** HASH0001. */
  HASH_ALG_X(HASH, 1, "Algorithm not supported: '%'."),

  /** HC0001. */
  HC_ERROR_X(HC, 1, "%"),
  /** HC0002. */
  HC_PARSE_X(HC, 2, "Conversion failed: %"),
  /** HC0003. */
  HC_ATTR(HC, 3, "No attribute allowed beside 'src' and 'media-type'."),
  /** HC0004. */
  HC_REQ_X(HC, 4, "Invalid request element: %."),
  /** HC0005. */
  HC_URL(HC, 5, "No URL supplied."),
  /** HC0006. */
  HC_PARAMS(HC, 6, "Specify request element or HTTP URI."),

  /** ZIP0001. */
  ZIP_NOTFOUND_X(ZIP, 1, "Path '%' not found."),
  /** ZIP0002. */
  ZIP_INVALID_X_X(ZIP, 2, "% element: attribute '%' expected."),
  /** ZIP0002. */
  ZIP_UNKNOWN_X(ZIP, 2, "ZIP definition: unknown element %."),
  /** ZIP0003. */
  ZIP_FAIL_X(ZIP, 3, "Operation failed: %."),

  // W3 Functions

  /** FOAP0001. */
  APPLY_X_X(FOAP, 1, "Arity and number of array members differs: % vs. %"),

  /** FOAR0001. */
  DIVZERO_X(FOAR, 1, "% cannot be divided by zero."),
  /** FOAR0002. */
  DIVFLOW_X(FOAR, 2, "Invalid division result: %."),
  /** FOAR0002. */
  RANGE_X(FOAR, 2, "Value out of range: %."),

  /** FOAY0001. */
  ARRAYBOUNDS_X_X(FOAY, 1, "Array index % out of bounds (1-%)."),
  /** FOAY0001. */
  ARRAYEMPTY(FOAY, 1, "Array has no entries."),
  /** FOAY0002. */
  ARRAYNEG_X(FOAY, 2, "Length is negative: %."),

  /** FOCA0002. */
  INVALUE_X_X(FOCA, 2, "Cannot cast to %: \"%\"."),
  /** FOCA0003. */
  INTRANGE_X(FOCA, 3, "Integer value out of range: %."),
  /** FOCA0005. */
  DATECALC_X_X(FOCA, 5, "Invalid % calculation: %."),

  /** FOCH0001. */
  INVCODE_X(FOCH, 1, "Invalid XML character '&#x%;'."),
  /** FOCH0002. */
  WHICHCOLL_X(FOCH, 2, "%."),
  /** FOCH0003. */
  NORMUNI_X(FOCH, 3, "Unsupported normalization form ('%')."),
  /** FOCH0004. */
  CHARCOLL(FOCH, 4, "Collation does not operate on character-by-character basis."),

  /** FODC0001. */
  IDDOC(FODC, 1, "Specified node has no document node as root."),
  /** FODC0002. */
  NODEERR_X_X(FODC, 2, "% could not be created: %."),
  /** FODC0002. */
  NODEFCOLL(FODC, 2, "No default collection available."),
  /** FODC0002. */
  IOERR_X(FODC, 2, "%"),
  /** FODC0002. */
  WHICHRES_X(FODC, 2, "Resource '%' does not exist."),
  /** FODC0004. */
  INVCOLL_X(FODC, 4, "Invalid collection URI: '%'."),
  /** FODC0005. */
  INVDOC_X(FODC, 5, "Invalid document URI: '%'."),
  /** FODC0006. */
  SAXERR_X(FODC, 6, "SAX: %"),
  /** FODC0007. */
  RESINV_X(FODC, 7, "Resource path '%' is invalid."),
  /** FODC0007. */
  INVDB_X(FODC, 7, "Invalid database name: '%'."),

  /** FODF1280. */
  FORMNUM_X(FODF, 1280, "Unknown decimal format: '%'."),
  /** FODF1310. */
  PICEMPTY(FODF, 1310, "The picture string must not be empty: '%'"),
  /** FODF1310. */
  PICNUM_X(FODF, 1310, "Invalid picture string: '%'."),
  /** FODF1310. */
  OPTAFTER_X(FODF, 1310, "Optional digit sign follows mandatory digit signs: '%'."),
  /** FODF1310. */
  INVGROUP_X(FODF, 1310, "Invalid position of grouping separator signs: '%'."),
  /** FODF1310. */
  DIFFMAND_X(FODF, 1310, "Mandatory digits must be of the same group: '%'."),
  /** FODF1310. */
  INVORDINAL_X(FODF, 1310, "Invalid specification of ordinal numbering: '%'."),
  /** FODF1310. */
  INVDDPATTERN_X(FODF, 1310, "Invalid decimal-digit-pattern: '%'."),

  /** FODT0001. */
  DATERANGE_X_X(FODT, 1, "%: '%' out of range."),
  /** FODT0001. */
  YEARRANGE_X(FODT, 1, "Year '%' out of range."),
  /** FODT0001. */
  SECRANGE_X(FODT, 1, "Seconds '%' out of range."),
  /** FODT0002. */
  DURRANGE_X_X(FODT, 2, "%: '%' out of range."),
  /** FODT0002. */
  MONTHRANGE_X(FODT, 2, "Months '%' out of range."),
  /** FODT0002. */
  SECDURRANGE_X(FODT, 2, "Seconds '%' out of range."),
  /** FODT0002. */
  DATEZERO_X_X(FODT, 2, "Invalid % calculation: %."),
  /** FODT0003. */
  INVALZONE_X(FODT, 3, "Timezone out of range (-14:00 to +14:00): %."),
  /** FODT0003. */
  ZONESEC_X(FODT, 3, "No seconds allowed in timezone: %."),

  /** FOER0000. */
  FUNERR1(FOER, 0, "Halted on error()."),

  /** FOFD1340. */
  INVCOMPSPEC_X(FOFD, 1340, "Invalid variable marker: '[%]'."),
  /** FOFD1340. */
  PICDATE_X(FOFD, 1340, "Invalid picture string: '%'."),
  /** FOFD1340. */
  CALQNAME_X(FOFD, 1340, "Invalid EQName: '%'."),
  /** FOFD1340. */
  CALWHICH_X(FOFD, 1340, "Unknown calendar: '%'."),
  /** FOFD1350. */
  PICINVCOMP_X_X(FOFD, 1350, "Component '[%]' not applicable to % values."),

  /** BXJS0001. */
  JSON_PARSE_X(FOJS, 1, "%"),
  /** BXJS0003. */
  JSON_DUPLICATE_X(FOJS, 3, "%"),

  /** FONS0004. */
  NSDECL_X(FONS, 4, "No namespace declared for prefix '%'."),

  /** FORG0001. */
  INVALIDZONE_X(FORG, 1, "Invalid timezone: %."),
  /** FORG0001. */
  FUNCAST_X_X(FORG, 1, "Cannot cast to %: \"%\"."),
  /** FORG0001. */
  FUNCCAST_X_X_X(FORG, 1, "Cannot cast % to %: %."),
  /** FORG0001. */
  DATEFORMAT_X_X_X(FORG, 1, "Wrong % format: '%' (try e.g. '%')."),
  /** FORG0002. */
  URIARG_X(FORG, 2, "URI argument is invalid: %."),
  /** FORG0002. */
  URINOTABS_X(FORG, 2, "Base URI is not absolute: %."),

  /** FORG0003. */
  ZEROORONE(FORG, 3, "Zero or one value expected."),
  /** FORG0004. */
  ONEORMORE(FORG, 4, "One or more values expected."),
  /** FORG0005. */
  EXACTLYONE(FORG, 5, "Exactly one value expected."),
  /** FORG0006. */
  EXPTYPE_X_X_X(FORG, 6, "% expected, % found: %."),
  /** FORG0006. */
  NOTCMP_X(FORG, 6, "Items of type % cannot be compared."),
  /** FORG0006. */
  EBV_X(FORG, 6, "Effective boolean value not defined for %."),
  /** FORG0006. */
  EBV_X_X(FORG, 6, "Effective boolean value not defined for %: %."),
  /** FORG0006. */
  SUM_X_X(FORG, 6, "Argument type % is invalid: %."),
  /** FORG0006. */
  SUMNUM_X_X(FORG, 6, "Number expected, % found: %."),
  /** FORG0006. */
  SUMDUR_X_X(FORG, 6, "Duration expected, % found: %."),
  /** FORG0006. */
  BINARY_X(FORG, 6, "Binary expected, % found."),
  /** FORG0006. */
  STRNOD_X_X(FORG, 6, "String or node expected, % found: %."),
  /** FORG0006. */
  ELMMAP_X_X_X(FORG, 6, "element(%) or map expected, % found: %."),
  /** FORG0006. */
  ELMSTR_X_X_X(FORG, 6, "element(%) or string expected, % found: %."),
  /** FORG0006. */
  STRBIN_X_X(FORG, 6, "String or binary expected, % found: %."),
  /** FORG0006. */
  JAVACONSTR_X_X(FORG, 6, "Unknown constructor: %(%)."),
  /** FORG0006. */
  JAVAMETHOD_X_X(FORG, 6, "Unknown method: %(%)."),
  /** FORG0006. */
  JAVACALL_X_X(FORG, 6, "Invalid call of Java function: %(%)."),
  /** FORG0006. */
  JAVAARGS_X_X(FORG, 6, "Invalid arguments: % expected, % found."),
  /** FORG0006. */
  JAVAERROR_X(FORG, 6, "Java function call failed: %."),
  /** FORG0006. */
  INVBASE_X(FORG, 6, "Unsupported base: %."),
  /** FORG0006. */
  INVBASEDIG_X_X(FORG, 6, "Invalid digit for base %: %."),
  /** FORG0006. */
  ERRFORMAT_X_X(FORG, 6, "%: %."),
  /** FORG0006. */
  INVALIDOPT_X(FORG, 6, "%"),
  /** FORG0006. */
  INVALIDOPTION_X(FORG, 6, "Unknown option '%'."),

  /** FORG0008. */
  FUNZONE_X_X(FORG, 8, "% and % have different timezones."),

  /** FORG0010. */
  IETF_PARSE_X_X_X(FORG, 10, "Invalid input (% expected, '%' found): '%'."),
  /** FORG0010. */
  IETF_INV_X(FORG, 10, "Invalid input: '%'."),

  /** FORX0001. */
  REGMOD_X(FORX, 1, "Invalid regular modifier: '%'."),
  /** FORX0002. */
  REGPAT_X(FORX, 2, "Invalid pattern: %."),
  /** FORX0003. */
  REGROUP(FORX, 3, "Pattern matches empty string."),
  /** FORX0004. */
  FUNREPBS_X(FORX, 4, "Invalid backslash in replacement string: %."),
  /** FORX0004. */
  FUNREPDOL_X(FORX, 4, "Invalid dollar sign in replacement string: %."),

  /** FOTY0013. */
  FIATOM_X(FOTY, 13, "Items of type % cannot be atomized."),
  /** FOTY0013. */
  FIEQ_X(FOTY, 13, "Items of type % have no defined equality."),
  /** FOTY0013. */
  FISTRING_X(FOTY, 14, "Items of type % have no string representation."),
  /** FOTY0013. */
  FICMP_X(FOTY, 15, "Items of type % cannot be compared."),

  /** FOUP0001. */
  UPFOTYPE_X(FOUP, 1, "Document or element expected, % found."),
  /** FOUP0001. */
  UPDOCTYPE_X(FOUP, 1, "Document expected, % found."),
  /** FOUP0002. */
  UPFOURI_X(FOUP, 2, "Invalid URI: %."),
  /** FOUP0002. */
  UPPUTERR_X(FOUP, 2, "File '%' could not be written."),
  /** FOUP0002. */
  UPDROPBACK_X_X(FOUP, 2, "Backup '%' could not be %."),
  /** FOUP0002. */
  UPDBERROR_X_X(FOUP, 2, "Database '%' could not be %."),
  /** FOUP0002. */
  UPDBPUT_X(FOUP, 2, "Resource '%' could not be written."),
  /** FOUP0002. */
  UPDBDELETE_X(FOUP, 2, "Resource '%' could not be deleted."),
  /** FOUP0002. */
  UPDBRENAME_X(FOUP, 2, "Resource '%' could not be renamed."),
  /** FOUP0002. */
  UPDBOPTERR_X(FOUP, 2, "%"),

  /** FOUT1170. */
  RESNF_X(FOUT, 1170, "Resource '%' cannot be retrieved."),
  /** FOUT1170. */
  FRAGID_X(FOUT, 1170, "URI contains a fragment identifier: %"),
  /** FOUT1170. */
  INVURL_X(FOUT, 1170, "URI is invalid: %"),
  /** FOUT1170. */
  STBASEURI(FOUT, 1170, "Static Base URI is undefined."),
  /** FOUT1190. */
  ENCODING_X(FOUT, 1190, "Unknown encoding '%'."),
  /** FOUT1190. */
  INVCHARS_X(FOUT, 1190, "%."),
  /** FOUT1200. */
  WHICHCHARS_X(FOUT, 1200, "Resource contains invalid input: %."),

  /** FTDY0016. */
  FTWEIGHT_X(FTDY, 16, "Weight value out of range: %."),
  /** FTDY0017. */
  FTMILD(FTDY, 17, "Invalid 'mild not' selection."),
  /** FTDY0020. */
  FTWILDCARD_X(FTDY, 20, "Invalid wildcard syntax: '%'."),

  /** FTST0007. */
  FTIGNORE(FTST, 7, "Ignore option not supported."),
  /** FTST0008. */
  NOSTOPFILE_X(FTST, 8, "Stop word file not found: '%'."),
  /** FTST0009. */
  FTNOSTEM_X(FTST, 9, "No stemmer available for language '%'."),
  /** FTST0009. */
  FTNOTOK_X(FTST, 9, "No tokenizer available for language '%'."),
  /** FTST0018. */
  NOTHES_X(FTST, 18, "Thesaurus not found: '%'."),
  /** FTST0019. */
  FTDUP_X(FTST, 19, "Match option '%' was declared twice."),

  /** SENR0001. */
  SERATTR_X(SENR, 1, "Attributes cannot be serialized:%."),
  /** SENR0001. */
  SERNS_X(SENR, 1, "Namespaces cannot be serialized:%."),
  /** SENR0001. */
  SERFUNC_X(SENR, 1, "Items of type % cannot be serialized."),
  /** SEPM0004. */
  SERSA(SEPM, 4, "If 'standalone' is specified, the root must be a single element."),
  /** SEPM0004. */
  SERDT(SEPM, 4, "If 'doctype-system' is specified, the root must be a single element."),
  /** SESU0007. */
  SERENCODING_X(SESU, 7, "Unknown encoding '%'."),
  /** SESU0007. */
  SERMAP_X_X(SESU, 7, "Character '#x%;' cannot be mapped to '%'."),
  /** SEPM0009. */
  SERSTAND(SEPM, 9, "Invalid combination of 'omit-xml-declaration'."),
  /** SEPM0010. */
  SERUNDECL(SEPM, 10, "XML 1.0: undeclaring prefixes not allowed."),
  /** SESU0011. */
  SERNORM_X(SESU, 11, "Normalization form not supported: %."),
  /** SESU0013. */
  SERNOTSUPP_X(SESU, 13, "%"),
  /** SERE0014. */
  SERILL_X(SERE, 14, "Illegal HTML character found: #x%;."),
  /** SERE0015. */
  SERPI(SERE, 15, "Processing construction contains '>'."),
  /** SEPM0016. */
  SER_X(SEPM, 16, "%"),
  /** SEPM0017. */
  SERMAP_X(SEPM, 17, "Character map '%' is not defined."),
  /** SEPM0017. */
  SEROPT_X(SEPM, 17, "%."),
  /** SEPM0017. */
  SEROPTION_X(SEPM, 17, "Serialization parameter '%' is invalid."),
  /** SERE0020. */
  SERNUMBER_X(SERE, 20, "Numeric value cannot be represented: '%'"),
  /** SENR0001. */
  SERJSONFUNC_X(SERE, 21, "Items of type % cannot be serialized."),
  /** SERE0022. */
  SERDUPL_X(SERE, 22, "Duplicate name found: '%'"),
  /** SERE0023. */
  SERJSON(SERE, 23, "Only one item can be serialized with JSON."),
  /** SERE0023. */
  SERJSONSEQ(SERE, 23, "Value has more than one item."),

  /** XPDY0002. */
  NOCTX_X(XPDY, 2, "%: no context value bound."),
  /** XPDY0002. */
  VAREMPTY_X(XPDY, 2, "No value assigned to %."),
  /** XPDY0050. */
  CTXNODE(XPDY, 50, "Root of the context value must be a document node."),
  /** XPDY0050. */
  NOTREAT_X_X_X(XPDY, 50, "Cannot treat % as %: %."),

  /** XPST0003. */
  QUERYEMPTY(XPST, 3, "Empty query."),
  /** XPST0003. */
  QUERYINV_X(XPST, 3, "Query contains an illegal character (#%)."),
  /** XPST0003. */
  NOQUOTE_X(XPST, 3, "Expecting quote%."),
  /** XPST0003. */
  NOVALIDATE(XPST, 3, "Invalid validation expression."),
  /** XPST0003. */
  NUMBERWS(XPST, 3, "Expecting separator after number."),
  /** XPST0003. */
  NUMBERDBL_X(XPST, 3, "Incomplete double value: '%'."),
  /** XPST0003. */
  NUMBERDEC_X(XPST, 3, "Incomplete decimal value: '%'."),
  /** XPST0003. */
  NUMBERITR(XPST, 3, "Unexpected decimal point."),
  /** XPST0003. */
  QUERYEND_X(XPST, 3, "Unexpected end of query: '%'."),
  /** XPST0003. */
  MODEXPR(XPST, 3, "No expression allowed in a library module."),
  /** XPST0003. */
  MAINMOD(XPST, 3, "Library modules cannot be evaluated."),
  /** XPST0003. */
  CMPEXPR(XPST, 3, "Comparison is incomplete."),
  /** XPST0003. */
  COPYEXPR(XPST, 3, "Copy expression is incomplete."),
  /** XPST0003. */
  NOELEMNAME(XPST, 3, "Expecting element name."),
  /** XPST0003. */
  ELEMNAME_X(XPST, 3, "Expecting element name, '<%' found."),
  /** XPST0003. */
  NOATTNAME(XPST, 3, "Expecting attribute name."),
  /** XPST0003. */
  NOEXPR(XPST, 3, "Expecting expression."),
  /** XPST0003. */
  WRONGCHAR_X_X(XPST, 3, "Expecting '%'%."),
  /** XPST0003. */
  INVENTITY_X(XPST, 3, "Invalid entity: '%'."),
  /** XPST0003. */
  INCOMPLETE(XPST, 3, "Incomplete expression."),
  /** XPST0003. */
  EVALUNARY(XPST, 3, "Unary operator expects a numeric value."),
  /** XPST0003. */
  STEPMISS_X(XPST, 3, "Expecting valid step%."),
  /** XPST0003. */
  AXISMISS_X(XPST, 3, "Expecting node test after % axis."),
  /** XPST0003. */
  DECLINCOMPLETE(XPST, 3, "Expecting 'function', 'variable', ..."),
  /** XPST0003. */
  FUNCNAME(XPST, 3, "Expecting function name."),
  /** XPST0003. */
  RESERVED_X(XPST, 3, "'%' is a reserved keyword."),
  /** XPST0003. */
  PREDMISSING(XPST, 3, "Expecting expression before predicate."),
  /** XPST0003. */
  NOVARNAME(XPST, 3, "Variable name expected, '%' found."),
  /** XPST0003. */
  NOVARDECL(XPST, 3, "Expecting variable declaration."),
  /** XPST0003. */
  PIWRONG(XPST, 3, "Expecting name of processing-instruction."),
  /** XPST0003. */
  NSWRONG(XPST, 3, "Expecting namespace prefix."),
  /** XPST0003. */
  NOENCLEXPR(XPST, 3, "Expecting valid expression after curly brace."),
  /** XPST0003. */
  NOFTSELECT_X(XPST, 3, "Expecting quote or opening curly brace%."),
  /** XPST0003. */
  NODOCCONS(XPST, 3, "Expecting expression in document constructor."),
  /** XPST0003. */
  NOTXTCONS(XPST, 3, "Expecting expression in text constructor."),
  /** XPST0003. */
  NOCOMCONS(XPST, 3, "Expecting expression in comment constructor."),
  /** XPST0003. */
  NOFUNBODY(XPST, 3, "Expecting function body."),
  /** XPST0003. */
  FUNCMISS_X(XPST, 3, "Expecting closing bracket for function '%'."),
  /** XPST0003. */
  MAPTAAT_X(XPST, 3, "Expecting atomic key type for map, found '%'."),
  /** XPST0003. */
  TYPEINVALID(XPST, 3, "Expecting type declaration."),
  /** XPST0003. */
  NODECLFORM_X(XPST, 3, "Decimal-format property '%' is invalid."),
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
  FLWORRETURN(XPST, 3, "Incomplete FLWOR expression: expecting 'return'."),
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
  NOWINDOW(XPST, 3, "Incomplete 'window' expression."),
  /** XPST0003. */
  NOCOUNT(XPST, 3, "Incomplete 'count' expression."),
  /** XPST0003. */
  NOCLOSING_X(XPST, 3, "Expecting closing tag </%>."),
  /** XPST0003. */
  COMCLOSE(XPST, 3, "Unclosed XQuery comment (: ..."),
  /** XPST0003. */
  EXPREMPTY(XPST, 3, "Unknown function or expression."),
  /** XPST0003. */
  WHICHTYPE_X(XPST, 3, "Unknown type: %."),
  /** XPST0003. */
  BINDNAME_X(XPST, 3, "Invalid name: '%'."),
  /** XPST0003. */
  PIXML_X(XPST, 3, "Processing instruction has illegal name: '%'."),
  /** XPST0003. */
  QNAME_X(XPST, 3, "Expecting QName, '%' found."),
  /** XPST0003. */
  PROLOGORDER(XPST, 3, "Default declarations must be declared first."),
  /** XPST0003. */
  FTRANGE(XPST, 3, "Expecting full-text range."),
  /** XPST0003. */
  FTSTOP(XPST, 3, "Stop words expected."),
  /** XPST0003. */
  FTMATCH_X(XPST, 3, "Unknown match option '%...'."),
  /** XPST0003. */
  INVALPI(XPST, 3, "Processing instruction has invalid name: '%' found."),
  /** XPST0003. */
  INTEXP(XPST, 3, "Integer expected."),
  /** XPST0003. */
  VARFUNC(XPST, 3, "Variable or function declaration expected."),
  /** XPST0003. */
  NOANN(XPST, 3, "No annotation allowed here."),
  /** XPST0003. */
  NOCATCH(XPST, 3, "Expecting catch clause."),
  /** XPST0003. */
  ANNVALUE(XPST, 3, "Literal expected, ')' found."),
  /** XPST0003. */
  UPDATINGVAR(XPST, 3, "Variable cannot be updating."),
  /** XPST0003. */
  SIMPLETYPE_X(XPST, 3, "Simple type expected, '%(' found."),
  /** XPST0003. */
  KEYSPEC(XPST, 3, "No specifier after lookup operator: '%'."),
  /** XPST0003. */
  ARROWSPEC(XPST, 3, "No specifier after arrow operator: '%'."),

  /** XPST0008. */
  VARUNDEF_X(XPST, 8, "Undefined variable %."),
  /** XPST0008. */
  CIRCREF_X(XPST, 8, "Static variable references itself: %"),
  /** XPST0008. */
  VARPRIVATE_X(XPST, 8, "Variable % is not visible from this module."),
  /** XPST0008. */
  TYPEUNDEF_X(XPST, 8, "Undefined type '%'."),
  /** XPST0008. */
  SCHEMAINV_X(XPST, 8, "Undefined schema name '%'."),

  /** XPST0017. */
  FUNCPRIVATE_X(XPST, 17, "Function '%' is not visible from this module."),
  /** XPST0017. */
  FUNCSIMILAR_X_X(XPST, 17, "Unknown function '%'; similar: '%'."),
  /** XPST0017. */
  FUNCARGNUM_X_X_X(XPST, 17, "%: % argument% supplied."),
  /** XPST0017. */
  FUNCTYPES_X_X_X_X(XPST, 17, "%: % argument% supplied (% expected)."),
  /** XPST0017. */
  FUNCUNKNOWN_X(XPST, 17, "Unknown function '%'."),
  /** XPST0017. */
  FUNCNOIMPL_X(XPST, 17, "Function '%' not implemented."),
  /** XPST0017. */
  FUNCJAVA_X(XPST, 17, "Java function '%' not found."),
  /** XPST0017. */
  JAVAAMBIG_X(XPST, 17, "Several implementations found for '%'."),
  /** XPST0017. */
  JAVAINIT_X(XPST, 17, "Class cannot be initialized: %."),

  /** XPST0051. */
  TYPEUNKNOWN_X(XPST, 51, "Unknown type: %."),
  /** XPST0080. */
  CASTUNKNOWN_X(XPST, 80, "Invalid cast type: %."),
  /** XPST0081. */
  NOURI_X(XPST, 81, "No namespace declared for '%'."),
  /** XPST0081. */
  NSMISS_X(XPST, 81, "QName '%' has no namespace."),

  /** XPTY0004. */
  NONAME_X(XPTY, 4, "Name expected, '%' found."),
  /** XPTY0004. */
  EMPTYFOUND(XPTY, 4, "Item expected, empty sequence found."),
  /** XPTY0004. */
  EMPTYFOUND_X(XPTY, 4, "% expected, empty sequence found."),
  /** XPTY0004. */
  SEQFOUND_X(XPTY, 4, "Item expected, sequence found: %."),
  /** XPTY0004. */
  NONUMBER_X_X(XPTY, 4, "Number expected, % found: %."),
  /** XPTY0004. */
  NODUR_X_X(XPTY, 4, "Duration expected, % found: %."),
  /** XPTY0004. */
  NOSUBDUR_X(XPTY, 4, "Subtype of xs:duration expected: %."),
  /** XPTY0004. */
  STRQNM_X_X(XPTY, 4, "String or QName expected, % found: %."),
  /** XPTY0004. */
  CPIWRONG_X_X(XPTY, 4, "String or NCName expected, % found: %."),

  /** XPTY0004. */
  INVCAST_X_X(XPTY, 4, "Cannot cast % to %."),
  /** XPTY0004. */
  INVCAST_X_X_X(XPTY, 4, "Cannot cast % to %: %."),
  /** XPTY0004. */
  INVTREAT_X_X(XPTY, 4, "Cannot treat % as %."),
  /** XPTY0004. */
  INVTREAT_X_X_X(XPTY, 4, "Cannot treat % as %: %."),
  /** XPTY0004. */
  CALCTYPE_X_X_X(XPTY, 4, "% not defined for % and %."),
  /** XPTY0004. */
  INVFUNCITEM_X(XPTY, 4, "Function expected, % found: %."),

  /** XPTY0004. */
  CMPTYPE_X(XPTY, 4, "Items of type % cannot be compared."),
  /** XPTY0004. */
  CMPTYPES_X_X(XPTY, 4, "Items of type % and % cannot be compared."),
  /** XPTY0004. */
  DOCATTS(XPTY, 4, "Cannot add attributes to a document node."),
  /** XPTY0004. */
  DOCNS(XPTY, 4, "Cannot add namespaces to a document node."),
  /** XPTY0004. */
  INVARITY_X_X_X_X(XPTY, 4, "%: % argument% supplied (% expected)."),
  /** XPTY0004. */
  INVNCNAME_X(XPTY, 4, "Invalid NCName: '%'."),
  /** XPTY0004. */
  CITYPES_X_X(XPTY, 4, "Incompatible types in context value declarations: % vs. %."),
  /** XPTY0004. */
  LOOKUP_X(XPTY, 4, "Input of lookup operator is not a map or array: %."),

  /** XPTY0018. */
  EVALNODESVALS(XPTY, 18, "Path yields both nodes and atomic values."),
  /** XPTY0019. */
  PATHNODE_X_X_X(XPTY, 19, "%: node expected as input, % found: %."),
  /** XPTY0020. */
  STEPNODE_X_X_X(XPTY, 20, "%: node expected as input, % found: %."),
  /** XPTY0117. */
  NSSENS_X_X(XPTY, 117, "Cannot cast % to %."),

  /** XQDY0025. */
  CATTDUPL_X(XQDY, 25, "Duplicate attribute '%'."),
  /** XQDY0026. */
  CPICONT_X(XQDY, 26, "Processing instruction has invalid content: '%'."),
  /** XQDY0041. */
  CPIINVAL_X(XQDY, 41, "Processing instruction has invalid name: '%'."),
  /** XQDY0044. */
  CAXML(XQDY, 44, "XML prefix and namespace cannot be rebound."),
  /** XQDY0044. */
  CAINV_(XQDY, 44, "Invalid attribute prefix/namespace: '%'."),
  /** XQDY0054. */
  CIRCVAR_X(XQDY, 54, "Static variable depends on itself: %"),
  /** XQDY0054. */
  CIRCCTX(XQDY, 54, "Context value is not defined."),
  /** XQDY0064. */
  CPIXML_X(XQDY, 64, "Processing instruction has illegal name: '%'."),
  /** XQDY0072. */
  COMINVALID(XQDY, 72, "Comment must not contain '--' or end with '-'."),
  /** XQDY0074. */
  INVNSNAME_X(XQDY, 74, "Invalid namespace prefix: '%'."),
  /** XQDY0074. */
  INVNAME_X(XQDY, 74, "Invalid QName: '%'."),
  /** XQDY0074. */
  INVPREF_X(XQDY, 74, "No namespace declared for %."),
  /** XQDY0096. */
  CEXML(XQDY, 96, "XML prefix or namespace cannot be rebound: '%'/'%'."),
  /** XQDY0096. */
  CEINV_X(XQDY, 96, "Invalid element prefix/namespace '%'."),
  /** XQDY0101. */
  CNXML(XQDY, 101, "XML prefix and namespace cannot be rebound."),
  /** XQDY0101. */
  CNINV_X(XQDY, 101, "Invalid namespace prefix '%'."),
  /** XQDY0101. */
  CNINVNS_X(XQDY, 101, "Invalid namespace URI '%'."),
  /** XQDY0102. */
  DUPLNSCONS_X(XQDY, 102, "Duplicate namespace declaration: '%'."),
  /** XQDY0137. */
  MAPDUPLKEY_X_X_X(XQDY, 137, "Key % already exists in map (values: % vs. %)."),
  /** XQDY0138. */
  MAPTZ(XQDY, 138, "Map contains keys with and without timezone."),

  /** XQST0009. */
  IMPLSCHEMA(XQST, 9, "Schema import not supported."),
  /** XQST0022. */
  NSCONS(XQST, 22, "Constant namespace value expected."),
  /** XQST0031. */
  XQUERYVER_X(XQST, 31, "XQuery version '%' not supported."),
  /** XQST0032. */
  DUPLBASE(XQST, 32, "Duplicate 'base-uri' declaration."),
  /** XQST0033. */
  DUPLNSDECL_X(XQST, 33, "Duplicate declaration of prefix '%'."),
  /** XQST0034. */
  FUNCDEFINED_X(XQST, 34, "Duplicate declaration of function '%'."),
  /** XQST0038. */
  DUPLCOLL(XQST, 38, "Duplicate 'collation' declaration."),
  /** XQST0038. */
  WHICHDEFCOLL_X(XQST, 38, "%."),
  /** XQST0039. */
  FUNCDUPL_X(XQST, 39, "Duplicate function argument %."),
  /** XQST0040. */
  ATTDUPL_X(XQST, 40, "Duplicate attribute '%'."),
  /** XQST0045. */
  NAMERES_X(XQST, 45, "Function '%' is in reserved namespace."),
  /** XQST0045. */
  ANNWHICH_X_X(XQST, 45, "Annotation %% is unknown."),
  /** XQST0046. */
  INVURI_X(XQST, 46, "URI '%' is invalid."),
  /** XQST0047. */
  DUPLMODULE_X(XQST, 47, "Module namespace is declared twice: '%'."),
  /** XQST0047. */
  MODULENS_X(XQST, 48, "Declaration % does not match the module namespace."),
  /** XQST0049. */
  VARDUPL_X(XQST, 49, "Duplicate declaration of static variable $%."),
  /** XQST0052. */
  TYPE30_X(XQST, 52, "Unknown cast type: %."),
  /** XQST0055. */
  DUPLCOPYNS(XQST, 55, "Duplicate 'copy-namespace' declaration."),
  /** XQST0057. */
  NSEMPTY(XQST, 57, "Namespace URI cannot be empty."),
  /** XQST0059. */
  INSTERR_X(XQST, 59, "Could not instantiate module '%'."),
  /** XQST0059. */
  WHICHMODULE_X(XQST, 59, "Module '%' not found."),
  /** XQST0059. */
  WHICHCLASS_X(XQST, 59, "Class not found: '%'."),
  /** XQST0059. */
  MODINITERR_X(XQST, 59, "Module not initialized: %."),
  /** XQST0059. */
  WHICHMODFILE_X(XQST, 59, "Could not retrieve module '%'."),
  /** XQST0059. */
  WRONGMODULE_X_X(XQST, 59, "Wrong URI '%' in imported module '%'."),
  /** XQST0060. */
  FUNNONS_X(XQST, 60, "Namespace needed for function '%'."),
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
  BINDXML_X(XQST, 70, "Prefix '%' cannot be rebound."),
  /** XQST0070. */
  XMLNSDEF_X(XQST, 70, "'%' cannot be default namespace."),
  /** XQST0070. */
  BINDXMLURI_X_X(XQST, 70, "'%' can only be bound to '%'."),
  /** XQST0071. */
  DUPLNSDEF_X(XQST, 71, "Duplicate declaration of prefix '%'."),
  /** XQST0075. */
  IMPLVAL(XQST, 75, "Validation not supported."),
  /** XQST0076. */
  FLWORCOLL_X(XQST, 76, "%."),
  /** XQST0079. */
  NOPRAGMA(XQST, 79, "Expecting pragma expression."),
  /** XQST0085. */
  NSEMPTYURI(XQST, 85, "Namespace URI cannot be empty."),
  /** XQST0087. */
  XQUERYENC2_X(XQST, 87, "Unknown encoding '%'."),
  /** XQST0088. */
  NSMODURI(XQST, 88, "Module namespace cannot be empty."),
  /** XQST0089. */
  DUPLVAR_X(XQST, 89, "Duplicate declaration of %."),
  /** XQST0090. */
  INVCHARREF_X(XQST, 90, "Invalid character reference '%'."),
  /** XQST0093. */
  CIRCMODULE(XQST, 93, "Circular module declaration."),
  /** XQST0094. */
  GVARNOTDEFINED_X(XQST, 94, "Undeclared grouping variable '%'."),
  /** XQST0097. */
  INVDECFORM_X_X(XQST, 97, "Invalid decimal-format property: %='%'."),
  /** XQST0097. */
  INVDECSINGLE_X_X(XQST, 97, "Decimal-format property must be a single character: %='%'."),
  /** XQST0097. */
  INVDECZERO_X(XQST, 97, "Zero-digit property must be Unicode digit with value zero: '%'."),
  /** XQST0098. */
  DUPLDECFORM_X(XQST, 98, "Duplicate use of decimal-format '%'."),
  /** XQST0099. */
  DUPLITEM(XQST, 99, "Duplicate declaration of context value."),
  /** XQST0103. */
  WINDOWUNIQ_X(XQST, 103, "Duplicate variable name in window clause: %"),
  /** XQST0106. */
  DUPLUPD(XQST, 106, "More than one updating annotation declared."),
  /** XQST0106. */
  DUPLFUNVIS(XQST, 106, "More than one visibility annotation declared."),
  /** XQST0108. */
  MODOUT(XQST, 108, "No output declarations allowed in library modules."),
  /** XQST0109. */
  OUTMAP_X(XQST, 109, "Character map '%' is not defined."),
  /** XQST0109. */
  OUTINVALID_X(XQST, 109, "%"),
  /** XQST0110. */
  OUTDUPL_X(XQST, 110, "Duplicate declaration of 'output:%'."),
  /** XQST0111. */
  DECDUPL(XQST, 111, "Duplicate decimal-format declaration."),
  /** XQST0113. */
  DECITEM(XQST, 113, "Context value cannot be bound in library module."),
  /** XQST0111. */
  DECDUPLPROP_X(XQST, 114, "Duplicate decimal-format property '%'."),
  /** XQST0116. */
  DUPLVARVIS(XQST, 116, "More than one visibility annotation declared."),
  /** XQST0118. */
  TAGWRONG_X_X(XQST, 118, "Different start and end tag: <%>...</%>."),
  /** XQST0119. */
  OUTDOC_X(XQST, 119, "Serialization document '%' cannot be parsed."),
  /** XQST0123. */
  DECLOPTION_X(XQST, 123, "Unknown option: %."),
  /** XQST0125. */
  INVISIBLE(XQST, 125, "No visibility annotation allowed in inline function."),
  /** XQST034. */
  NSAXIS(XQST, 134, "Namespace axis is not supported."),

  /** XQTY0024. */
  NOATTALL(XQTY, 24, "Attribute must follow the root element."),
  /** XQTY0024. */
  NONSALL(XQTY, 24, "Namespaces must follow the root element."),
  /** XQTY0105. */
  CONSFUNC_X(XQTY, 105, "Invalid content: %."),

  /** XUDY0009. */
  UPNOPAR_X(XUDY, 9, "Target % has no parent."),
  /** XUDY0014. */
  UPNOTCOPIED_X(XUDY, 14, "% was not created by copy clause."),
  /** XUDY0015. */
  UPMULTREN_X(XUDY, 15, "Node can only be renamed once: %."),
  /** XUDY0015. */
  UPPATHREN_X(XUDY, 15, "Path can only be renamed once: '%'."),
  /** XUDY0016. */
  UPMULTREPL_X(XUDY, 16, "Node can only be replaced once: %."),
  /** XUDY0016. */
  UPMULTDOC_X(XUDY, 16, "Document can only be replaced once: %."),
  /** XUDY0017. */
  UPMULTREPV_X(XUDY, 17, "Node can only be replaced once: %"),
  /** XUDY0021. */
  UPATTDUPL_X(XUDY, 21, "Duplicate attribute %."),
  /** XUDY0023. */
  UPNSCONFL(XUDY, 23, "Conflicts with existing namespaces."),
  /** XUDY0024. */
  UPNSCONFL2(XUDY, 24, "New namespaces conflict with each other."),
  /** XUDY0027. */
  UPSEQEMP_X(XUDY, 27, "% target must not be empty."),
  /** XUDY0029. */
  UPPAREMPTY(XUDY, 29, "Target has no parent node."),
  /** XUDY0030. */
  UPATTELM(XUDY, 30, "Attributes cannot be inserted as child of a document."),
  /** XUDY0031. */
  UPURIDUP_X(XUDY, 31, "URI '%' is addressed multiple times."),

  /** XUDY0032 (WIP). */
  UPFUNCNOTUP(XUDY, 32, "Function has no %updating annotation."),
  /** XUDY0032 (WIP). */
  UPFUNCUP(XUDY, 32, "No 'updating' keyword specified before function call."),

  /** XUST0001. */
  UPNOT_X(XUST, 1, "%: no updating expression allowed."),
  /** XUST0001. */
  UPALL_X(XUST, 1, "%: all expressions must be updating or return an empty sequence."),
  /** XUST0001. */
  UPCTX(XUST, 1, "Context value may not declare an updating expression."),
  /** XUST0002. */
  UPMODIFY(XUST, 2, "Modify clause: all expressions must be updating or return an empty sequence."),
  /** XUST0002. */
  UPEXPECTF(XUST, 2, "Function body must be an updating expression."),
  /** XUST0003. */
  DUPLREVAL(XUST, 3, "Duplicate 'revalidation' declaration."),
  /** XUST0026. */
  NOREVAL(XUST, 26, "Revalidation mode not supported."),
  /** XUST0028. */
  UUPFUNCTYPE(XUST, 28, "No return type allowed in updating functions."),

  /** XUTY0004. */
  UPNOATTRPER(XUTY, 4, "Attribute must follow the root element."),
  /** XUTY0005. */
  UPTRGTYP(XUTY, 5, "Single element or document expected as insert target."),
  /** XUTY0006. */
  UPTRGTYP2(XUTY, 6, "Single element, text, comment or pi expected as insert target."),
  /** XUTY0007. */
  UPTRGDELEMPT(XUTY, 7, "Only nodes can be deleted."),
  /** XUTY0008. */
  UPTRGMULT(XUTY, 8, "Single element, text, attribute, comment or pi expected as replace target."),
  /** XUTY0010. */
  UPWRELM(XUTY, 10, "Replacing nodes must be no attribute nodes."),
  /** XUTY0011. */
  UPWRATTR(XUTY, 11, "Replacing nodes must be attribute nodes."),
  /** XUTY0012. */
  UPWRTRGTYP(XUTY, 12, "Single element, attribute or pi expected as rename target."),
  /** XUTY0013. */
  UPCOPYMULT_X(XUTY, 13, "Value assigned to $% must be a single node."),
  /** XUTY0013. */
  UPSOURCE(XUTY, 13, "Source of transform expression must be a single node."),
  /** XUTY0022. */
  UPATTELM2(XUTY, 22, "Insert target must be an element.");

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
   * Throws a query exception. If {@link InputInfo#check()} returns {@code true},
   * a static error instance ({@link QueryException#ERROR}) will be returned.
   * @param ii input info
   * @param ext extended info
   * @return query exception (indicates that an error is raised)
   */
  public QueryException get(final InputInfo ii, final Object... ext) {
    return ii != null && ii.check() ? QueryException.ERROR : new QueryException(ii, this, ext);
  }

  /**
   * Throws a query I/O exception without {@link InputInfo} reference.
   * @param ext extended info
   * @return query I/O exception (indicates that an error is raised)
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
   * @author BaseX Team 2005-15, BSD License
   * @author Leo Woerteler
   */
  public enum ErrType {
    // Project errors

    /** BASX Error type. */ BASX(BXERR_PREFIX, BXERRORS_URI),

    /** BXCL Error type. */ BXCL(BXERR_PREFIX, BXERRORS_URI),
    /** BXCO Error type. */ BXCO(BXERR_PREFIX, BXERRORS_URI),
    /** BXCS Error type. */ BXCS(BXERR_PREFIX, BXERRORS_URI),
    /** BXDB Error type. */ BXDB(BXERR_PREFIX, BXERRORS_URI),
    /** BXFE Error type. */ BXFE(BXERR_PREFIX, BXERRORS_URI),
    /** BXFT Error type. */ BXFT(BXERR_PREFIX, BXERRORS_URI),
    /** BXHL Error type. */ BXHL(BXERR_PREFIX, BXERRORS_URI),
    /** BXJS Error type. */ BXJS(BXERR_PREFIX, BXERRORS_URI),
    /** BXPR Error type. */ BXPR(BXERR_PREFIX, BXERRORS_URI),
    /** BXRA Error type. */ BXRA(BXERR_PREFIX, BXERRORS_URI),
    /** BXRE Error type. */ BXRE(BXERR_PREFIX, BXERRORS_URI),
    /** BXSL Error type. */ BXSL(BXERR_PREFIX, BXERRORS_URI),
    /** BXSQ Error type. */ BXSQ(BXERR_PREFIX, BXERRORS_URI),
    /** BXVA Error type. */ BXVA(BXERR_PREFIX, BXERRORS_URI),
    /** BXXQ Error type. */ BXXQ(BXERR_PREFIX, BXERRORS_URI),
    /** HASH Error type. */ HASH(BXERR_PREFIX, BXERRORS_URI),
    /** UNIT Error type. */ UNIT(UNIT_PREFIX,  UNIT_URI),
    /** USER Error type. */ USER(USER_PREFIX,  USER_URI),

    // EXPath errors

    /** ARCH Error type. */ ARCH(EXPERR_PREFIX, EXPERROR_URI),
    /** BIN  Error type. */ BIN(BIN_PREFIX,     BIN_URI),
    /** CX   Error type. */ CX(EXPERR_PREFIX,   EXPERROR_URI),
    /** FILE Error type. */ FILE(FILE_PREFIX,   FILE_URI),
    /** HC   Error type. */ HC(EXPERR_PREFIX,   EXPERROR_URI),
    /** ZIP  Error type. */ ZIP(EXPERR_PREFIX,  EXPERROR_URI),

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
   * @param ii input info
   * @return exception or null
   */
  public static QueryException get(final String name, final String msg, final InputInfo ii) {
    for(final QueryError err : VALUES) {
      if(err.toString().equals(name)) return new QueryException(ii, err.qname(), msg).error(err);
    }
    return null;
  }

  /**
   * Throws a comparison exception.
   * @param ii input info
   * @param item1 first item
   * @param item2 second item
   * @return query exception (indicates that an error is raised)
   */
  public static QueryException diffError(final InputInfo ii, final Item item1, final Item item2) {
    final Type t1 = item1.type, t2 = item2.type;
    return (item1 == item2 ? NOTCMP_X : t1 == t2 ? CMPTYPE_X : CMPTYPES_X_X).get(ii, t1, t2);
  }

  /**
   * Throws a type promoting exception.
   * @param ii input info
   * @param value value
   * @param seqType sequence type
   * @return query exception (indicates that an error is raised)
   */
  public static QueryException treatError(final InputInfo ii, final Value value,
      final SeqType seqType) {
    return INVTREAT_X_X_X.get(ii, value.seqType(), seqType, value);
  }

  /**
   * Throws a type cast exception.
   * @param ii input info
   * @param value value
   * @param type expression cast type
   * @return query exception (indicates that an error is raised)
   */
  public static QueryException castError(final InputInfo ii, final Value value, final Type type) {
    return INVCAST_X_X_X.get(ii, value.type, type, value);
  }

  /**
   * Throws a type cast exception.
   * @param ii input info
   * @param value value
   * @param type expression cast type
   * @return query exception (indicates that an error is raised)
   * @throws QueryException query exception
   */
  public static QueryException funCastError(final InputInfo ii, final Type type, final Object value)
      throws QueryException {
    return FUNCAST_X_X.get(ii, type, chop(value, ii));
  }

  /**
   * Throws a number exception.
   * @param expr parsing expression
   * @param item item
   * @return query exception (indicates that an error is raised)
   */
  public static QueryException numberError(final ParseExpr expr, final Item item) {
    return numberError(expr.info, item);
  }

  /**
   * Throws a number exception.
   * @param ii input info
   * @param item found item
   * @return query exception (indicates that an error is raised)
   */
  public static QueryException numberError(final InputInfo ii, final Item item) {
    return NONUMBER_X_X.get(ii, item.type, item);
  }

  /**
   * Throws an invalid value exception.
   * @param ii input info
   * @param type expected type
   * @param value value
   * @return query exception (indicates that an error is raised)
   */
  public static QueryException valueError(final InputInfo ii, final Type type, final byte[] value) {
    return INVALUE_X_X.get(ii, type, value);
  }

  /**
   * Throws an exception for circular static variables.
   * @param var variable expression
   * @return never
   */
  public static QueryException circVarError(final StaticVar var) {
    return CIRCVAR_X.get(var.info, var);
  }

  /**
   * Chops the specified value to a maximum size.
   * @param value value
   * @param ii input info
   * @return exception or null
   * @throws QueryException query exception
   */
  public static byte[] chop(final Object value, final InputInfo ii) throws QueryException {
    return ii != null && ii.check() ? Token.EMPTY :
           value instanceof byte[] ? chop((byte[]) value, ii) :
           value instanceof Item ? chop(((Item) value).string(ii), ii) :
           chop(value.toString(), ii);
  }

  /**
   * Chops the specified string to a maximum size.
   * @param string string
   * @param ii input info
   * @return exception or null
   */
  public static byte[] chop(final String string, final InputInfo ii) {
    return ii != null && ii.check() ? Token.EMPTY : chop(Token.token(string), ii);
  }

  /**
   * Chops the specified token to a maximum size.
   * @param token token
   * @param ii input info
   * @return exception or null
   */
  public static byte[] chop(final byte[] token, final InputInfo ii) {
    if(ii != null && ii.check()) return Token.EMPTY;

    final TokenBuilder tb = new TokenBuilder();
    byte l = 0;
    for(byte b : token) {
      final int ts = tb.size();
      if(ts == 32) {
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
