package org.basex.query;

import java.lang.annotation.*;

/**
 * <p>The XQuery {@code import module} statement can be used to import XQuery modules
 * as well as Java instances, which will be treated as modules. Any class with a public,
 * empty constructor can be imported as module.</p>
 *
 * <p>If a class extends the {@link QueryModule} class, it inherits the {@link #queryContext}
 * and {@link #staticContext} variables, which provide access to all properties of the
 * current query. E.g., they provide access to the current {@link QueryContext#value
 * context value} or the {@link StaticContext#ns declared namespaces} of a query.</p>
 *
 * <p>The default properties of functions can be overwritten via annotations:</p>
 * <ul>
 *   <li>Java functions can only be executed by users with {@code ADMIN} permissions.
 *       You may annotate a function with {@link Requires}({@link Permission}) to
 *       also make it accessible to users with less privileges.</li>
 *   <li>Java code is treated as "non-deterministic", as its behavior cannot
 *       be predicted by the XQuery processor. You may annotate a function as
 *       {@link Deterministic} if you know that it will have no side-effects and will
 *       always yield the same result.</li>
 *   <li>Java code is treated as "context-independent". If a function accesses
 *       the specified {@link #queryContext}, it should be annotated as
 *       {@link ContextDependent}.</li>
 *   <li>Java code is treated as "focus-independent". If a function accesses
 *       the current context value, position or size, it should be annotated as
 *       {@link FocusDependent}.</li>
 * </ul>
 *
 * If the {@link QueryResource} is implemented, its {@link QueryResource#close()} method will be
 * called after the query has been evaluated. It should always be implemented if a module opens
 * connections, resources, etc. that eventually need to be closed.
 *
 * <p>Please visit our documentation to find more details on
 * <a href="http://docs.basex.org/wiki/Packaging">Packaging</a>,
 * <a href="http://docs.basex.org/wiki/Java_Bindings">Java Bindings</a> and
 * <a href="http://docs.basex.org/wiki/User_Management">User Management</a>.
 * The XQuery 3.0 specification gives more insight into
 * <a href="http://www.w3.org/TR/xpath-functions-30/#properties-of-functions">function
 * properties</a>.</p>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class QueryModule {
  /**
   * Permission required to call a function.
   */
  public enum Permission {
    /** Admin permissions. */
    ADMIN,
    /** Create permissions. */
    CREATE,
    /** Write permissions. */
    WRITE,
    /** Read permissions. */
    READ,
    /** No permissions. */
    NONE
  }

  /**
   * Java functions can only be executed by users with {@code ADMIN} permissions.
   * You may annotate a function with {@link Requires}({@link Permission}) to
   * also make it accessible to other users with less permissions.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Requires {
    /**
     * Permission.
     * @return value
     */
    Permission value();
  }

  /**
   * Java code is treated as "non-deterministic", as its behavior cannot be predicted from
   * the XQuery processor. You may annotate a function as {@link Deterministic} if you
   * know that it will have no side-effects and will always yield the same result.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Deterministic { }

  /**
   * Java code is treated as "context-independent". If a function accesses the specified
   * {@link #queryContext}, it should be annotated as {@link ContextDependent}.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface ContextDependent { }

  /**
   * Java code is treated as "focus-independent". If a function accesses the current
   * context value, position or size, it should be annotated as {@link FocusDependent}.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface FocusDependent { }

  /**
   * Set additional locks to be fetched. These are useful if a module accesses external
   * resources which must be under concurrency control. These locks are in a "java module
   * namespace" and do not interfere with user locks or databases with the same name.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Lock {
    /**
     * Read locks.
     * @return read locks
     */
    String[] read() default { };

    /**
     * Write locks.
     * @return write locks
     */
    String[] write() default { };
  }

  /** Global query context. */
  public QueryContext queryContext;
  /** Static context. */
  public StaticContext staticContext;
}
