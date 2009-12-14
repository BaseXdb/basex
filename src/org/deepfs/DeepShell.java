package org.deepfs;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.deepfs.jfuse.JFUSEAdapter.*;
import java.io.File;
import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.core.proc.InfoTable;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryProcessor;
import org.deepfs.fs.DeepFS;
import org.deepfs.jfuse.DeepStat;
import org.deepfs.util.FSWalker;
import org.deepfs.util.TreePrinter;

/**
 * Rudimentary shell to interact with a file hierarchy stored in XML.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek
 */
public final class DeepShell {
  /** Shell command description. */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Command {
    /** Description of expected arguments. */
    String args() default "";
    /** Shortcut key for command. */
    char shortcut();
    /** Short help message. */
    String help();
  }

  /** Filesystem reference. */
  private final DeepFS fs;

  /** Shell prompt. */
  private static final String PS1 = "$ ";

  /** Constructor. */
  DeepShell() {
    this("deepfs");
  }

  /** Constructor.
   * @param fsdbname DeepFS filesystem/database instance
   */
  DeepShell(final String fsdbname) {
    fs = new DeepFS(fsdbname, string(NOTMOUNTED));
    loop();
    fs.umount();
  }

  /** Rudimentary shell. */
  private void loop() {
    do {
      final String[] args = tokenize(input(PS1));
      if(args.length != 0) exec(args);
    } while(true);
  }

  /**
   * Command line-arguments if any.
   * @param args user arguments
   */
  private void exec(final String[] args) {
    try {
      final Method[] ms = getClass().getMethods();
      for(final Method m : ms) {
        if(m.isAnnotationPresent(Command.class)) {
          final Command c = m.getAnnotation(Command.class);
          if(args[0].equals(m.getName())
              || args[0].length() == 1 && args[0].charAt(0) == c.shortcut()) {
            m.invoke(this, (Object) args);
            return;
          }
        }
      }
      Main.out(
          "%: commmand not found. Type 'help' for available commands.\n",
          args[0] == null ? "" : args[0]);
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Returns the next user input.
   * @param prompt prompt string
   * @return user input
   */
  private String input(final String prompt) {
    Main.out(prompt);
    return new Scanner(System.in).nextLine();
  }

  /**
   * Tokenizes argument line.
   * @param line string to split in tokens
   * @return argument vector
   */
  private String[] tokenize(final String line) {
    final StringTokenizer st = new StringTokenizer(line);
    final String[] toks = new String[st.countTokens()];
    int i = 0;
    while(st.hasMoreTokens()) {
      toks[i++] = st.nextToken();
    }
    return toks;
  }

  /**
   * Makes new directory.
   * @param args argument vector
   */
  @Command(shortcut = 'm',
      args = "<directory_name>", help = "creates a new directory")
  public void mkdir(final String[] args) {
    if(args.length != 2) {
      help(new String[] { "help", "mkdir"});
      return;
    }
    final int err = fs.mkdir(args[1], getSIFDIR() | 0775);
    if(err == -1) System.err.printf("mkdir failed. %d\n", err);
  }

  /**
   * Removes existing directory.
   * @param args argument vector
   */
  @Command(shortcut = 'r',
      args = "<directory_name>", help = "remove existing directory")
  public void rmdir(final String[] args) {
    if(args.length != 2) {
      help(new String[] { "help", "rmdir"});
      return;
    }
    final int err = fs.rmdir(args[1]);
    if(err != 0) System.err.printf("rmdir failed. %d\n", err);
  }

  /**
   * Creates a file if it doesn't exist yet.
   * @param args argument vector
   */
  @Command(shortcut = 'c',
      args = "<file_name>", help = "create file (if it doesn't exist)")
  public void touch(final String[] args) {
    if(args.length != 2) {
      help(new String[] { "help", "touch"});
      return;
    }
    final int err = fs.create(args[1], 0100644);
    if(err < 0) System.err.printf("touch failed. %d\n", err);
  }

  /**
   * Prints stat information of file to stdout.
   * @param args argument vector
   */
  @Command(shortcut = 's',
      args = "<file_name>", help = "display file status")
  public void stat(final String[] args) {
    if(args.length != 2) {
      help(new String[] { "help", "stat"});
      return;
    }
    final DeepStat dst = fs.stat(args[1]);
    if(dst == null) {
      System.err.printf("stat failed.\n");
      return;
    }
    final PrintStream ps = new PrintStream(System.out);
    dst.printFields("deepshell: ", ps);
    ps.flush();
  }

  /**
   * Creates a file if it doesn't exist yet.
   * @param args argument vector
   */
  @Command(shortcut = 't',
      args = "<directory>", help = "tree(1)-like output directory hierarchy")
  public void tree(final String[] args) {
    if(args.length == 1) {
      new FSWalker(new TreePrinter()).traverse(new File(Prop.WORK));
      return;
    }
    if(args.length == 2) {
      final File d = new File(args[1]);
      if (d.isDirectory()) {
        new FSWalker(new TreePrinter()).traverse(d);
        return;
      }
      System.err.println("No such directory " + d.getAbsolutePath());
    }
    help(new String[] { "help", "tree"});
    return;
  }

  /**
   * Prints stat information of file to stdout.
   * @param args argument vector
   */
  @Command(shortcut = 'l',
      args = "<file_name>", help = "list directory")
  public void list(final String[] args) {
    if(args.length != 2) {
      help(new String[] { "help", "list"});
      return;
    }
    final byte[][] dents = fs.readdir(args[1]);
    if(dents == null) {
      System.err.printf("listing failed.\n");
      return;
    }
    for(byte[] de : dents) Main.out(">> " + string(de));
  }

  /**
   * Prints stat information of file to stdout.
   * @param args argument vector
   */
  @Command(shortcut = 'i',
      args = "", help = "info table (" + Text.NAME + " command)")
  public void info(final String[] args) {
    if(args.length != 1) {
      help(new String[] { "help", "info"});
      return;
    }
    try {
      new InfoTable(null, null).execute(fs.getContext(),
          new PrintOutput(System.out));
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Prints short help message for available commands.
   * @param args argument vector
   */
  @Command(shortcut = 'h', help = "print this message")
  public void help(final String[] args) {
    final Method[] ms = getClass().getMethods();
    for(final Method m : ms)
      if(m.isAnnotationPresent(Command.class)) {
        final Command c = m.getAnnotation(Command.class);
        if(args.length == 1 && args[0].charAt(0) == 'h'
            || args.length > 1 && m.getName().equals(args[1])
            || args.length > 1 && args[1].length() == 1
                && c.shortcut() == args[1].charAt(0))
          System.out.printf("%-40s %-40s\n",
              m.getName() + " " + c.args(),
              c.help() + " (" + c.shortcut() + ")");
      }
  }

  /**
   * Leaves the shell.
   * @param args argument vector (currently not used)
   */
  @Command(shortcut = 'q', help = "quit shell (unmounts fuse and closes db)")
  public void quit(@SuppressWarnings("unused") final String[] args) {
    fs.umount();
    Main.outln("cu");
    System.exit(0);
  }

  /**
   * Serializes the FS instance.
   * @param args argument vector (currently not used)
   */
  @Command(shortcut = 'x', help = "print file hierarchy as XML")
  public void serialize(@SuppressWarnings("unused") final String[] args) {
    try {
      final Nodes n = new QueryProcessor("/", fs.getContext()).queryNodes();
      final PrintOutput out = new PrintOutput(System.out);
      final XMLSerializer xml = new XMLSerializer(out);
      n.serialize(xml);
      out.flush();
      xml.close();
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * A file hierarchy stored as XML.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new DeepShell();
  }
}
