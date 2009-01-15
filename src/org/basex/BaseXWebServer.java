package org.basex;

import static org.basex.Text.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.data.XMLSerializer;
import org.basex.io.BufferedOutput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.util.Var;
import org.basex.util.Action;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This is a simple web server.
 * Files with an <code>.xq</code> suffix are evaluated as XQuery.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXWebServer {
  /** Header key. */
  static final Pattern QUERY = Pattern.compile("(GET|POST) (.*) HTTP.*");
  /** Xquery suffix. */
  static final String XQSUFFIX = "xq";
  /** PHP suffix. */
  static final String PHPSUFFIX = "php";
  /** Index files. */
  static final String[] INDEXFILES = {
    "index." + XQSUFFIX, "index." + PHPSUFFIX, "index.html", "index.htm",
  };

  /** Document header. */
  static final String DOCTYPE =
    "<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'" +
        " 'http://www.w3.org/TR/html4/loose.dtd'>";
  /** Document header. */
  static final String HEADER = DOCTYPE +
    "\n<html>\n<head><title>BaseX WebServer</title>\n" +
    "<meta http-equiv='Content-Type' content='text/html;charset=utf-8'n" +
    "</head>\n<body style='font-family:sans-serif;'>";
  /** Document footer. */
  static final String FOOTER = "</body></html>";

  /** Database Context. */
  final Context context = new Context();
  /** Flag for server activity. */
  boolean running = true;
  /** Verbose mode. */
  boolean verbose = false;
  /** Flag for caching queries. */
  boolean cache = false;

  /** XQuery Cache. */
  final HashMap<String, XQueryProcessor> map =
    new HashMap<String, XQueryProcessor>();

  /**
   * Main method.
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    new BaseXWebServer(args);
  }

  /**
   * Constructor.
   * @param args arguments
   */
  private BaseXWebServer(final String[] args) {
    Prop.server = true;
    try {
      if(!parseArguments(args)) return;

      final ServerSocket server = new ServerSocket(Prop.webport);
      BaseX.outln(WSERVERSTART);
      while(running) serve(server);
      BaseX.outln(WSERVERSTOPPED);

      Prop.write();
      context.close();
    } catch(final Exception ex) {
      BaseX.debug(ex);
      if(ex instanceof BindException) {
        BaseX.errln(SERVERBIND);
      } else if(ex instanceof IOException) {
        BaseX.errln(SERVERERR);
      } else {
        BaseX.errln(ex.getMessage());
      }
    }
  }

  /**
   * Waits for a HTTP request and evaluates the input.
   * @param server server reference
   */
  private void serve(final ServerSocket server) {
    try {
      // get socket and ip address
      final Socket s = server.accept();
      final Performance p = new Performance();
      // get command and arguments
      final InputStream is = s.getInputStream();
      final Request req = getRequest(is);

      // quit server
      if(req.code == 0) {
        s.getOutputStream().write(1);
        running = false;
        return;
      }

      // start session thread
      new Action() {
        public void run() {
          try {
            final IO file = req.file;
            final String path = absPath(file);

            // no file specified - try alternatives
            if(req.code == 404) {
              BaseX.debug("File not found: %", path);
              send("404 File Not Found", "", "Not found: " + path, s);
            } else if(req.code == 302) {
              send("302 Found", "Location: " + path,
                "Redirecting to <a href='" + path + "'>" + path + "</a>.", s);
            } else if(file.isDir()) {
              sendDir(file, s);
            } else if(req.suf.equals(XQSUFFIX)) {
              evalXQuery(req, s);
            } else if(req.suf.equals(PHPSUFFIX)) {
              evalPHP(req, s);
            } else {
              send(file, s);
            }
            is.close();

            if(verbose) {
              final InetAddress addr = s.getInetAddress();
              BaseX.outln("%:% => % [%]", addr.getHostAddress(),
                  s.getPort(), req.file, p.getTimer());
            }
          } catch(final Exception ex) {
            if(ex instanceof IOException) BaseX.errln(SERVERERR);
            else ex.printStackTrace();
          }
        }
      }.execute();
    } catch(final Exception ex) {
      if(ex instanceof IOException) BaseX.errln(SERVERERR);
      else ex.printStackTrace();
    }
  }

  /**
   * Returns the requested file.
   * @param is input stream
   * @return file
   * @throws IOException I/O exception
   */
  protected Request getRequest(final InputStream is) throws IOException {
    final BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String input = null;
    String line;
    boolean stop = false;
    while((line = br.readLine()) != null) {
      if(line.length() == 0) break;
      final Matcher m = QUERY.matcher(line);
      if(m.matches()) input = m.group(2);
      stop |= line.equals("STOP");
    }
    if(stop) return new Request(0);
    if(input == null) return new Request(400);

    // decode input...
    return new Request(URLDecoder.decode(input.replaceAll("&", "|"), "UTF-8"));
  }

  /**
   * Send requested file to output stream.
   * @param file reference
   * @param s socket reference
   * @throws IOException I/O exception
   */
  protected void send(final IO file, final Socket s) throws IOException {
    final OutputStream os = s.getOutputStream();
    os.write(file.content());
    os.close();

    /*
    final FileInputStream is = new FileInputStream(file.name);
    byte[] buf = new byte[s.getSendBufferSize()];
    int i;
    while((i = is.read(buf)) != -1) {
      os.write(buf, 0, i);
    }
    os.close();
    is.close();
    */
  }

  /**
   * Sends a file not found message to the client.
   * @param code code
   * @param head optional headers
   * @param msg message
   * @param s socket reference
   * @throws IOException I/O exception
   */
  protected void send(final String code, final String head, final String msg,
      final Socket s) throws IOException {

    final OutputStream os = s.getOutputStream();
    final PrintOutput out = new PrintOutput(new BufferedOutput(os));
    out.println("HTTP/1.1 " + code);
    if(head.length() != 0) out.println(head);
    out.println("Server: BaseXWebServer");
    out.println("Content-Type: text/html; charset=utf-8\n");
    out.println(HEADER);
    out.println("<h3>BaseX WebServer</h3>\n" + msg);
    out.println(FOOTER);
    out.close();
  }

  /**
   * Process an XQuery request.
   * @param req request
   * @param s socket reference
   * @throws IOException I/O exception
   */
  protected void evalXQuery(final Request req, final Socket s)
      throws IOException {

    // query and output result
    final OutputStream os = s.getOutputStream();
    final PrintOutput out = new PrintOutput(new BufferedOutput(os));
    out.println("HTTP/1.1 200 OK");
    out.println("Server: BaseXWebServer");
    out.println("Content-Type: text/html; charset=utf-8\n");
    out.println(DOCTYPE);

    try {
      XQueryProcessor xq = null;

      // cache compiled query or create new one
      if(cache) {
        final String key = req.file + "/" + req.file.date();
        xq = map.get(key);
        if(xq == null) {
          final String query = Token.string(req.file.content());
          xq = new XQueryProcessor(query, req.file);
          map.put(key, xq);
        }
      } else {
        final String query = Token.string(req.file.content());
        xq = new XQueryProcessor(query, req.file);
      }

      // assign parameters to the xquery processor
      for(final String[] arg : req.args) {
        final Var v = new Var(new QNm(Token.token(arg[0])));
        final String val = arg.length == 2 ? arg[1] : "";
        xq.ctx.vars.addGlobal(v.bind(Str.get(val), null));
      }
      xq.query(null).serialize(new XMLSerializer(out));
    } catch(final Exception ex) {
      if(ex instanceof IOException) {
        out.println(SERVERERR);
      } else if(ex instanceof XQException) {
        out.println(HEADER + "<pre><font color='red'>" +
            ex.getMessage() + "</font></pre>" + FOOTER);
      } else {
        out.println(ex.toString());
      }
      BaseX.debug(ex);
    }
    out.close();
  }

  /**
   * Evaluates a PHP script.
   * @param req request instance
   * @param s socket reference
   * @throws Exception exception
   */
  protected void evalPHP(final Request req, final Socket s) throws Exception {
    // using a helper script to pass arguments
    final String phphelp = "<?$f=$argv[1];$s=preg_split('/ /',$argv[2]);" +
      "for($i=0;$i<sizeof($s);$i+=2){$_GET[$s[$i]]=$s[$i+1];}include($f);?>";
    final IO file = IO.get(Prop.webpath + "/php.tmp");
    file.write(Token.token(phphelp));

    final StringBuilder args = new StringBuilder();
    for(final String[] arg : req.args) args.append(arg[0] + " " + arg[1] + " ");

    eval(s, Prop.phppath, file.path(), req.file.path(), args.toString());
    file.delete();
  }

  /**
   * Evaluate a process.
   * @param s socket reference
   * @param exec process executable and arguments
   * @throws Exception exception
   */
  private void eval(final Socket s, final String... exec) throws Exception {
    final OutputStream os = s.getOutputStream();

    final ProcessBuilder pb = new ProcessBuilder(exec);
    pb.redirectErrorStream(true);
    final InputStream is = pb.start().getInputStream();
    int i = 0;
    while((i = is.read()) != -1) os.write(i);
    os.close();
  }

  /**
   * Sends a directory output.
   * @param dir directory reference
   * @param s socket reference
   * @throws IOException I/O exception
   */
  protected void sendDir(final IO dir, final Socket s)
      throws IOException {

    final OutputStream os = s.getOutputStream();
    final PrintOutput out = new PrintOutput(new BufferedOutput(os));
    final String col = " style='background-color:grey; height:1px;'";
    final String dec = " style='text-decoration:none;'";
    out.println(HEADER);
    out.println("<h2>Index of " + absPath(dir) + "/</h2>");
    out.println("<table cellpadding='0' cellspacing='2'>");
    out.println("<tr><td><b>Filename</b></td><td width='30'></td>");
    out.println("<td><b>Last Modified</b></td><td width='30'></td>");
    out.println("<td align='right'><b>Size</b></td></tr>");
    out.println("<tr><td colspan='5'" + col + "></td></tr>");

    for(final IO f : dir.children()) {
      out.print("<tr><td><a" + dec + " href='" + f.name());
      if(f.isDir()) out.print("/");
      out.println("'>" + f.name() + "</a></td><td></td>");
      final SimpleDateFormat sdm = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
      final String mod = sdm.format(new Date(f.date()));
      out.println("<td>" + mod + "</td><td></td>");
      out.println("<td align='right'>" + f.length() + "</td></tr>");
    }
    out.println("<tr><td colspan='5'" + col + "></td></tr>");
    out.println("<tr><td colspan='5' align='right'><font size='1'>");
    out.println(NAME + " WebServer</font></td></tr>");
    out.println("</table></body></html>");
    out.close();
  }

  /**
   * Returns the absolute web server path.
   * @param f file reference
   * @return path
   */
  protected String absPath(final IO f) {
    return f.path().replace(Prop.webpath.replace('\\', '/'), "");
  }

  /**
   * Parses the command line arguments.
   * @param args the command line arguments
   * @return true if all arguments have been correctly parsed
   */
  private boolean parseArguments(final String[] args) {
    boolean ok = true;

    // loop through all arguments
    for(int a = 0; a < args.length; a++) {
      ok = false;
      if(args[a].startsWith("-")) {
        for(int i = 1; i < args[a].length(); i++) {
          final char c = args[a].charAt(i);
          if(c == 'p') {
            // parse server port
            if(++i == args[a].length()) {
              a++;
              i = 0;
            }
            if(a == args.length) break;
            final int p = Token.toInt(args[a].substring(i));
            if(p <= 0) {
              BaseX.errln(SERVERPORT + args[a].substring(i));
              break;
            }
            Prop.webport = p;
            i = args[a].length();
            ok = true;
          } else if(c == 'c') {
            cache = true;
            ok = true;
          } else if(c == 'd') {
            Prop.debug = true;
            ok = true;
          } else if(c == 'v') {
            verbose = true;
            ok = true;
          } else {
            break;
          }
        }
      } else if(args[a].equals("stop")) {
        try {
          // send the stop command
          final Socket s = new Socket("localhost", Prop.webport);
          s.getOutputStream().write(Token.token("STOP\n\n"));
          s.close();
        } catch(final Exception ex) {
          if(ex instanceof IOException) BaseX.errln(SERVERERR);
          else BaseX.errln(ex.getMessage());
          BaseX.debug(ex);
        }
        return false;
      }
      if(!ok) break;
    }
    if(!ok) BaseX.errln(WSERVERINFO);
    return ok;
  }

  /** This class provides information on a client request. */
  static final class Request {
    /** File. */
    IO file;
    /** Suffix. */
    String suf;
    /** Arguments. */
    String[][] args = {};
    /** Response code. */
    int code = 200;

    /**
     * Constructor.
     * @param c code
     */
    Request(final int c) {
      code = c;
    }

    /**
     * Constructor.
     * @param fn filename
     */
    Request(final String fn) {
      // no file specified - try alternatives
      file = IO.get(Prop.webpath + "/" + fn);

      if(file.isDir()) {
        for(final String index : INDEXFILES) {
          final IO f = IO.get(Prop.webpath + "/" + fn + "/" + index);
          if(f.exists()) {
            code = 302;
            file = f;
            return;
          }
        }
        if(!fn.endsWith("/")) {
          code = 302;
          return;
        }
      }

      String f = fn;
      int i = fn.indexOf("?");
      if(i != -1) {
        final String[] arg = fn.substring(i + 1).split("\\|");
        f = fn.substring(0, i);
        args = new String[arg.length][2];
        for(int a = 0; a < arg.length; a++) args[a] = arg[a].split("=", 2);
      }

      i = f.indexOf(".");
      file = IO.get(Prop.webpath + "/" + f);
      suf = i != -1 ? f.substring(i + 1) : "";

      if(!file.exists()) code = 404;
    }
  }
}
