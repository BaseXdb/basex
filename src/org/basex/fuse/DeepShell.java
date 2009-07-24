package org.basex.fuse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryProcessor;

/**
 * Rudimentary shell to interact with a file hierarchy stored in XML.
 *
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
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
  /** Context. */
  private Context ctx;

  /** Shell prompt. */
  private static final String PS1 = "$ ";

  /** Constructor. */
  DeepShell() {
    BaseX.errln("DeepShell");
    ctx = new Context();
    fs = new DeepFS(ctx, "deepshell");

    // initialize/mount filesystem
    //fs.init();
    loop();
    ctx.close();
  }

  /** Rudimentary shell. */
  private void loop() {
    do {
      final String[] args = tokenize(input(PS1));
      if(args.length != 0) exec(args);
    } while(true);
  }

  /**
   * Command line arguments if any.
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
      BaseX.out(
          "%s: commmand not found. Type 'help' for available commands.\n",
          args[0] == null ? "" : args[0]);
    } catch(final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the next user input.
   * @param prompt prompt string
   * @return user input
   */
  private String input(final String prompt) {
    System.out.print(prompt);
    // get user input
    try {
      final InputStreamReader isr = new InputStreamReader(System.in);
      return new BufferedReader(isr).readLine().trim();
    } catch(final Exception ex) {
      // also catches interruptions such as ctrl+c, etc.
      BaseX.outln();
      return null;
    }
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
    final int err = fs.mkdir(args[1], DeepFuse.S_IFDIR | 0775);
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
   * Leave shell.
   * @param args argument vector (not used currently)
   */
  @Command(shortcut = 'q', help = "quit shell (unmounts fuse and closes db)")
  public void quit(@SuppressWarnings("unused") final String[] args) {
    // unmount filesystem
    fs.destroy();
    BaseX.outln("cu");
    System.exit(0);
  }

  /**
   * Serialize FS instance.
   * @param args argument vector (currently not used)
   */
  @Command(shortcut = 's', help = "print file hierarchy as XML")
  public void serialize(@SuppressWarnings("unused") final String[] args) {
    try {
      Nodes n = new Nodes(0, fs.data);
      n = new QueryProcessor("/", n, ctx).queryNodes();
      final PrintOutput out = new PrintOutput(System.out);
      final XMLSerializer xml = new XMLSerializer(out, false, true);
      n.serialize(xml);
      xml.close();
    } catch(final Exception e) {
      e.printStackTrace();
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
