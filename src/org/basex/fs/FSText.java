package org.basex.fs;

/**
 * This interface organizes textual information for the fs package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public interface FSText {
  
  /** Help text. */
  String HEAD = "deepFS General Commands Manual";
  /** Filesystem Help. */
  String HELP = "Bash Commands:";
  /** Filesystem Help. */
  String USAGE = "Usage: ";
  /** Filesystem Help. */
  String NAME = "NAME";
  /** Filesystem Help. */
  String SYNO = "SYNOPSIS";
  /** Filesystem Help. */
  String DESC = "DESCRIPTION";
  
  // Note that these strings are called from {@link FSCmd} via reflection....
  
  /** Help text. */
  String[] CAT = {
    "Concatenate and print files",
    "[-bn] [file ...]",
    "The cat utility reads files, writing them to the standard output.", "",
    "The options are as follows:",
    "-b    Number the non-blank output lines, starting at 1",
    "-n    Number the output lines, starting at 1"
  };
  
  /** Help text. */
  String[] CD = {
    "Changes working directory",
    "[dir]",
    "Changes the working directory"
  };
    
  /** Help text. */
  String[] CP = {
    "Copy files and directories",
    "[-R] [source] [target]",
    "In the first synopsis form, the cp utility copies the contents of the",
    "source to target. In the second synopsis form, the contents of each named",
    "source file is copied to the destination target. The names of the files",
    "themselves are not changed. If cp detects an attempt to copy a file to",
    "itself, the copy will fail.", "",
    "The options are as follows:",
    "-r    copy directories recursively",
  }; 
    
  /** Help text. */
  String[] DU = {
    "Display disk usage statistics",
    "[-ah] [dir]",
    "The du utility displays the file system block usage for each file argu-",
    "ment and for each directory in the file hierarchy rooted in each direc-",
    "tory argument. If no file is specified, the block usage of the hierarchy",
    "rooted in the current directory is displayed.", "",
    "The options are as follows:",
    "-a    Display an entry for each file in a file hierarchy",
    "-h    Print sizes in human readable format",
    "-s    Display only a total for each argument"
  };
  
  /** Help text. */
  String[] LOCATE = {
    "Locate filenames",
    "[-l limit] [-c] -V [1|2] pattern ...",
    "The locate program searches a database for all pathnames which match the",
    "specified pattern. The database is recomputed periodically (usually",
    "weekly or daily), and contains the pathnames of all files which are pub-",
    "licly accessible.", "",
    "Shell globbing and quoting characters ('*', '?', '\', '[' and ']' may",
    "be used in pattern, although they will have to be escaped from the shell.",
    "Preceding any character with a backslash ('\') eliminates any special" +
    "meaning which it may have. The matching differs in that no characters" +
    "must be matched explicitly, including slashes ('/').", "",
    "As a special case, a pattern containing no globbing characters ('foo')" +
    "is matched as though it were '*foo*'.", "",
    "The following options are available:", "",
    "-c         Suppress output; instead print a count of matching file names",
    "-l number  Limit output to number of file names and exit",
    "-V number  1 = use locate with direct access to the data table",
    "           2 = use XPath (no wildcards atm)"
  };
  
  /** Help text. */
  String[] LS = {
    "List directory contents",
    "[-ahlrRSt] [file ...]",
    "For each operand ls displays its name as well as any requested,",
    "associated information.", "",
    "The following options are available:",
    "-a     Include directory entries whose names begin with a dot ('.')",
    "-h     Print sizes in human readable format",
    "-l     List files in the long format",
    "-R     Recursively list subdirectories encountered",
    "-S     Sort by file size",
    "-t     Sort by modification date"
  };
  
  /** Help text. */
  String[] MKDIR = {
    "Make directories",
    "[dir]",
    "The mkdir utility creates directories, if they do not already exist."
  };
  
  /** Help text. */
  String[] PWD = {
    "Return working directory name",
    "[path]",
    "The pwd utility writes the absolute pathname of the current working",
    "directory to the standard output.",
  };
  
  /** Help text. */
  String[] RM = {
    "Remove directory entries",
    "[-R] file ...",
    "The rm utility removes files specified on the command line.",
    "The options are as follows:",
    "-R    Attempt to remove the file hierarchy rooted in each file argument"
  };
  
  /** Help text. */
  String[] TOUCH = {
    "Change file access and modification times",
    "[file]",
    "The touch utility sets the modification and access times of files.",
    "If any file does not exist, it is created."
  };

  /** File System errors. */
  String[][] CODES = {
      {   "0", "Undefined error: 0." },
      {   "1", "Operation not permitted." },
      {   "2", "No such file or directory." },
      {   "5", "Input/output error." },
      {  "13", "Permission denied." },
      {  "17", "File/directory exists." },
      {  "20", "Not a directory." },
      {  "21", "Is a directory." },
      {  "22", "Invalid argument." },
      {  "30", "Read-only file system." },
      {  "34", "Result too large." },
      {  "63", "File name too long." },
      {  "66", "File name not allowed." },
      {  "79", "Directory not empty." },
      {  "99", "Inappropriate file type or format." },
      { "100", "Missing argument." },
      { "101", "Omitting directory" },
      { "102", "Invalid option" }      
  };
}
