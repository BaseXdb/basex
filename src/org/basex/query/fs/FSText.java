package org.basex.query.fs;

/**
 * This interface organizes textual information for the fs package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public interface FSText {
  /** Help text. */
  String FSMAN = "      deepFS General Commands Manual      ";
  /** Help text. */
  String FSCAT = "CAT(1)" + FSMAN + "CAT(1) \n\n  NAME\n" +
    "     cat -- concatenate and print files  \n\n  SYNOPSIS\n" +
    "     cat [-bhn] [file ...]  \n\n  DESCRIPTION\n     " +
    "The cat utility reads files, writing them to the standard output.\n\n" +
    "     The options are as follows:\n" +
    "     -b    Number the non-blank output lines, starting at 1.\n" +
    "     -h    Print this page.\n" +
    "     -n    Number the output lines, starting at 1.\n\n\n";
  /** Help text. */
  String FSCD = "CD(1)" + FSMAN + "CD(1) \n\n  NAME\n" +
    "     cd -- changes the working directory\n\n  SYNOPSIS\n" +
    "     cd [-h] dir  \n\n  DESCRIPTION\n" + 
    "     Changes the working directory.    \n\n" + 
    "     The options are as follows:\n" + 
    "     -h    Print this page.\n\n\n";
  /** Help text. */
  String FSCP = "CP(1)" + FSMAN + "CP(1)\n\n  NAME\n" +
    "     cp -- copy files\n\n  SYNOPSIS\n" +
    "     cp source_file target_file\n" +
    "     cp  source_file ... target_directory\n\n  DESCRIPTION\n     " +
    "In the first synopsis form, the cp utility copies the contents of the\n" +
    "     source_file to the target_file." +
    " In the second synopsis form, the con-\n" +
    "     tents of each named source_file is copied to the destination\n" +
    "     target_directory. The names of the files themselves are not " +
    "changed. If \n     cp detects an attempt to copy a file to itself," + 
    " the copy will fail.\n\n\n";
  /** Help text. */
  String FSDU = "DU(1)" + FSMAN + "DU(1)\n\n  NAME\n" +
    "     du -- display disk usage statistics\n\n  SYNOPSIS\n" +
    "     du [-ah] [dir]\n\n  DESCRIPTION\n" +
    "     The du utility displays the file system block usage for each" +
    " file argu-\n     ment and for each directory in the file hierarchy" +
    " rooted in each direc-\n     tory argument.  If no file is specified," +
    " the block usage of the hierarchy\n     rooted in the current directory" +
    " is displayed.\n\n     The options are as follows:\n\n" +
    "     -a    Display an entry for each file in a file hierarchy.\n" +
    "     -h    Print this page.\n\n\n";
  /** Help text. */
  String FSLOCATE = "LOCATE(1)" + FSMAN + "LOCATE(1)\n\n" +
    "  NAME\n     locate -- find filenames quickly\n\n  SYNOPSIS\n" +
    "     locate[-l limit] [-c] -V [1|2] pattern ...\n\n" +
    "  DESCRIPTION (Not Yet Tested !!!)\n" +
    "     The locate program searches a database for all pathnames " +
    "which match the\n     specified pattern.  The database is recomputed " +
    "periodically (usually\n     weekly or daily), and contains the " +
    "pathnames of all files which are pub-\n     licly accessible.\n\n" +
    "     Shell globbing and quoting characters (``*'', ``?'', ``\''," +
    " ``['' and\n     ``]'') may be used in pattern, although they will " +
    "have to be escaped from\n     the shell.  Preceding any character with " +
    "a backslash (``\'') eliminates\n     any special meaning which it may " +
    "have.  The matching differs in that no\n     " +
    "characters must be matched explicitly, including slashes (``/'').\n\n" +
    "     As a special case, a pattern containing no globbing characters " +
    "(``foo'')\n     is matched as though it were ``*foo*''.\n\n" +
    "     The following options are available:\n\n     " +
    "-c         Suppress normal output; instead print a count of matching " +
    "file names.\n     -h         Print this page.\n     -l number  Limit " +
    "output to number of file names and exit.\n     -V number  1 = use " +
    "locate with direct access to the data table\n" +
    "                2  = use XPath (no wildcards atm)\n\n\n";
  /** Help text. */
  String FSLS = "LS(1)" + FSMAN + " LS(1)\n\n  NAME\n" +
    "     ls -- list directory contents\n\n  SYNOPSIS\n" +
    "     ls [-ahlR] [file ...]\n\n  DESCRIPTION\n" +
    "     For each operand ls displays its name as well as any requested,\n" +
    "     associated information.\n\n" +
    "     The following options are available:\n     " +
    "-a     Include directory entries whose names begin with a dot (`.').\n" +
    "     -h     Print this page\n" +
    "     -l     List files in the long format.\n" +
    "     -R     Recursively list subdirectories encountered.\n\n\n";
  /** Help text. */
  String FSMKDIR = "MKDIR(1)" + FSMAN + "MKDIR(1))\n\n" +
    "    NAME\n      mkdir -- make directories)\n\n    SYNOPSIS\n" +
    "      mkdir directory_name)\n\n    DESCRIPTION\n      The mkdir utility " +
    "creates the directories.\n\n\n";
  /** Help text. */
  String FSPWD = "PWD(1)" + FSMAN + "PWD(1)\n\n  NAME\n" +
    "     pwd -- return working directory name\n\n  SYNOPSIS\n" +
    "     pwd\n\n  DESCRIPTION\n     The pwd utility writes the absolute " +
    "pathname of the current working\n     directory to the standard " +
    "output.\n\n     The options are as follows:\n\n" +
    "     -h    Print this page.\n\n\n";
  /** Help text. */
  String FSRM = "RM(1)" + FSMAN + "RM(1)\n\n  NAME\n" +
    "     rm, unlink -- remove directory entries\n\n  SYNOPSIS\n" +
    "     rm [-R] file ...\n\n  DESCRIPTION\n" +
    "     The rm utility attempts to remove the non-directory type files " +
    "specified\n     on the command line.  If the permissions of the file " +
    "do not permit writ-\n     ing, and the standard input device is a " +
    "terminal, the user is prompted\n     (on the standard error output) " +
    "for confirmation.\n\n     The options are as follows:\n\n" +
    "     -h    Print this page.\n" +
    "     -R     Attempt to remove the file hierarchy rooted in each " +
    "file argument.\n\n\n";
  /** Help text. */
  String FSTOUCH = "TOUCH(1)" + FSMAN + "TOUCH(1)\n\n" +
    "  NAME\n     touch -- change file access and modification times\n\n" +
    "  SYNOPSIS\n     touch file ...\n\n  DESCRIPTION\n" +
    "     The touch utility sets the modification and access times of files." +
    "  If\n     any file does not exist, it is created with default " +
    "permissions.\n\n     The options are as follows:\n\n" +
    "     -h    Print this page.\n\n\n";

  /** FS Error Message. */
  String EUND = "Undefined error: 0.";
  /** FS Error Message. */
  String EPERM = "Operation not permitted.";
  /** FS Error Message. */
  String ENOENT = "No such file or directory.";
  /** FS Error Message. */
  String EIO = "Input/output error.";
  /** FS Error Message. */
  String EACCES = "Permission denied.";
  /** FS Error Message. */
  String EEXIST = "File exists.";
  /** FS Error Message. */
  String ENOTDIR = "Not a directory.";
  /** FS Error Message. */
  String EISDIR = "Is a directory.";
  /** FS Error Message. */
  String EINVAL = "Invalid argument.";
  /** FS Error Message. */
  String EROFS = "Read-only file system.";
  /** FS Error Message. */
  String ERANGE = "Result too large.";
  /** FS Error Message. */
  String ENAMETOOLONG = "File name too long.";
  /** FS Error Message. */
  String ENOTEMPTY = "Directory not empty.";
  /** FS Error Message. */
  String EFTYPE = "Inappropriate file type or format.";
  /** FS Error Message. */
  String EMISSARG = "Missing argument.";
  /** FS Error Message. */
  String EOMDIR = "omitting directory";
}
