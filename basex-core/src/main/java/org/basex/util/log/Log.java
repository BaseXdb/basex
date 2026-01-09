package org.basex.util.log;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class processes log entries. The log format:
 * <ul>
 *   <li><b>Time</b>: timestamp (format: {@code xs:time})</li>
 *   <li><b>Address</b>: host name and port of the requesting client</li>
 *   <li><b>User</b>: username</li>
 *   <li><b>Type</b>: Type of log message: REQUEST, OK or ERROR</li>
 *   <li><b>Info</b>: Log message</li>
 *   <li><b>Performance</b>: Measured time in milliseconds</li>
 * </ul>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Log implements QueryTracer {
  /** Server string. */
  private static final String SERVER = "SERVER";

  /** Static options. */
  private final StaticOptions sopts;
  /** Logs messages to exclude. */
  private final Pattern exclude;
  /** Log messages to cut. */
  private final Pattern cut;
  /** Cached filtered entries. */
  private final HashMap<String, Long> cache = new HashMap<>();
  /** Mask IP address. */
  private final boolean maskip;
  /** Maximum length of log messages. */
  private final int maxLen;

  /** Log targets. */
  private Set<LogTarget> targets;
  /** Current (daily) log file. */
  LogFile file;

  /**
   * Constructor.
   * @param sopts static options
   */
  public Log(final StaticOptions sopts) {
    this.sopts = sopts;

    final Function<StringOption, Pattern> pattern = option -> {
      final String value = sopts.get(option);
      try {
        return value.isEmpty() ? null : Pattern.compile(value);
      } catch(final IllegalArgumentException ex) {
        Util.debug(ex);
        Util.errln("Invalid % pattern: %", option, value);
      }
      return null;
    };
    exclude = pattern.apply(StaticOptions.LOGEXCLUDE);
    cut = pattern.apply(StaticOptions.LOGCUT);
    maxLen = sopts.get(StaticOptions.LOGMSGMAXLEN);
    maskip = sopts.get(StaticOptions.LOGMASKIP);
  }

  /**
   * Returns a log file for the specified name (current or new instance).
   * @param name name of log file
   * @return log file, or {@code null} if it does not exist
   */
  public LogFile file(final String name) {
    LogFile lf = file;
    if(lf == null || !lf.valid(name)) lf = new LogFile(name, dir());
    return lf.exists() ? lf : null;
  }

  /**
   * Writes a server entry to the log file.
   * @param type log type
   * @param info info string (can be {@code null})
   */
  public void writeServer(final LogType type, final String info) {
    write(type, info, null, null, (String) null);
  }

  /**
   * Writes an entry to the log file.
   * @param type type ({@link LogType}, HTTP status code or custom string)
   * @param info info string (can be {@code null})
   * @param perf performance object (can be {@code null})
   * @param ctx database context
   */
  public void write(final Object type, final String info, final Performance perf,
      final Context ctx) {
    write(type, info, perf, ctx.clientAddress(), ctx);
  }

  /**
   * Writes an entry to the log file.
   * @param type type ({@link LogType}, HTTP status code or custom string)
   * @param info info string (can be {@code null})
   * @param perf performance object (can be {@code null})
   * @param address address/source (can be {@code null})
   * @param ctx database context
   */
  public void write(final Object type, final String info, final Performance perf,
      final String address, final Context ctx) {
    write(type, info, perf, address, ctx.clientName());
  }

  /**
   * Writes an entry to the log file.
   * @param type type ({@link LogType}, HTTP status code or custom string)
   * @param info info string (can be {@code null})
   * @param perf performance object (can be {@code null})
   * @param address address/source ({@code SERVER} is written if value is {@code null})
   * @param user user ({@code admin} is written if value is {@code null})
   */
  private synchronized void write(final Object type, final String info, final Performance perf,
      final String address, final String user) {

    if(noTargets()) return;

    String inf = info != null ? info.trim().replaceAll("\\s+", " ") : "";
    if(exclude(type, inf, address)) return;

    // normalize info string
    if(cut != null) inf = cut.matcher(inf).replaceAll("");
    final int len = inf.codePointCount(0, inf.length());
    if(len > maxLen) inf = inf.substring(0, inf.offsetByCodePoints(0, maxLen)) + "...";

    String addr = address != null ? address.replaceFirst("^/", "") : SERVER;
    if(maskip) {
      // examples for input strings:
      // IPv4: 192.128.0.1:54321
      // IPv6: [2001:db8:abcd:0012:0000:0000:1234:5678]:54321
      addr = addr.replaceAll("\\.\\d+:", ".0:").replaceAll("(\\w*:\\w*:\\w*)(:\\w*){5}", "$1::");
    }

    final LogEntry entry = new LogEntry();
    entry.date = new Date();
    entry.time = DateTime.format(entry.date, DateTime.TIME);
    entry.address = addr;
    entry.user = user != null ? user : UserText.ADMIN;
    entry.type = type.toString();
    entry.info = inf;
    if(perf != null) entry.runtime = perf.toString();

    // write log entry to requested targets
    for(final LogTarget target : targets) {
      try {
        target.write(this, entry);
      } catch(final IOException ex) {
        Util.stack(ex);
      }
    }
  }

  /**
   * Checks if logging is requested. Initializes the log targets if not done yet
   * (not part of the constructor to consider command-line arguments).
   * @return result of check
   */
  private boolean noTargets() {
    if(targets == null) {
      final String log = sopts.get(StaticOptions.LOG);
      final Set<LogTarget> set = EnumSet.noneOf(LogTarget.class);
      for(final String target : log.trim().toUpperCase(Locale.ENGLISH).split("\\s*,\\s*")) {
        final Boolean enable = Strings.toBoolean(target);
        if(enable == null) {
          for(final LogTarget lt : LogTarget.values()) {
            if(target.equals(lt.name())) set.add(lt);
          }
        } else if(enable) {
          set.add(LogTarget.DATA);
        } else {
          set.clear();
          break;
        }
      }
      targets = set;
    }
    return targets.isEmpty();
  }

  /**
   * Checks if a log entry is filtered out.
   * Maintains a cache to filter out matching log entries.
   * @param type type ({@link LogType}, HTTP status code or custom string)
   * @param info info string (can be {@code null})
   * @param address address/source ({@code SERVER} is written if value is {@code null})
   * @return result of check
   */
  private boolean exclude(final Object type, final String info, final String address) {
    if(exclude == null) return false;

    final boolean found = exclude.matcher(info).find();

    // cache entries that may have multiple log entries (log types, HTTP status; see AdminLogs#logs)
    if(address != null && (type == LogType.REQUEST || type == LogType.OK || type == LogType.ERROR ||
        type.toString().matches("\\d+"))) {
      // find matching entry, remove outdated entries
      final long ms = System.currentTimeMillis();
      final boolean cached = cache.containsKey(address);
      cache.values().removeIf(time -> cached || ms - time >= 3_600_000);
      if(cached) return true;
      // cache new entry
      if(found) cache.put(address, ms);
    }
    return found;
  }

  /**
   * Closes the log file.
   */
  public synchronized void close() {
    try {
      if(file != null) {
        file.close();
        file = null;
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Returns all log files.
   * @return log directory
   */
  public IOFile[] files() {
    return dir().children(".*\\" + IO.LOGSUFFIX);
  }

  /**
   * Writes an entry to the daily database log file.
   * @param entry log entry
   * @throws IOException I/O exception
   */
  public void write(final LogEntry entry) throws IOException {
    final String name = DateTime.format(entry.date, DateTime.DATE);
    if(file != null && !file.valid(name)) close();
    if(file == null) file = LogFile.create(name, dir());
    file.write(Token.token(entry + Prop.NL));
  }

  /**
   * Returns a reference to the log directory.
   * @return log directory
   */
  private IOFile dir() {
    return sopts.dbPath(".").resolve(sopts.get(StaticOptions.LOGPATH));
  }

  @Override
  public void printTrace(final String message) {
    writeServer(LogType.TRACE, message);
  }

  @Override
  public boolean cacheTrace() {
    return false;
  }

  @Override
  public boolean moreTraces(final int count) {
    return count <= 100;
  }
}
