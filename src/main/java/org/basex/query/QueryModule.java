package org.basex.query;

import java.lang.annotation.*;

/**
 * <p>The XQuery {@code import module} statement can be used to import XQuery modules
 * as well as Java instances, which will be treated as modules. Any class with a public,
 * empty constructor can be imported as module.</p>
 *
 * <p>If a class extends the {@link QueryModule} class, it inherits the {@link #context}
 * variable, which provides access to all properties of the current query. E.g., it
 * provides access to the current {@link QueryContext#value context item} or the
 * {@link QueryContext#sc static context} of a query. Next, the following default
 * properties of functions can be changed via annotations:</p>
 * <ul>
 *   <li>Java functions can only be executed by users with {@code ADMIN} permissions.
 *       You may annotate a function with {@link Requires}({@link Permission}) to
 *       also make it accessible to users with less privileges.</li>
 *   <li>Java code is treated as "non-deterministic", as its behavior cannot
 *       be predicted by the XQuery processor. You may annotate a function as
 *       {@link Deterministic} if you know that it will have no side-effects and will
 *       always yield the same result.</li>
 *   <li>Java code is treated as "context-independent". If a function accesses
 *       the specified {@link #context}, it should be annotated as
 *       {@link ContextDependent}.</li>
 *   <li>Java code is treated as "focus-independent". If a function accesses
 *       the current context item, position or size, it should be annotated as
 *       {@link FocusDependent}.</li>
 * </ul>
 *
 * <p>Please visit our documentation to find more details on
 * <a href="http://docs.basex.org/wiki/Packaging">Packaging</a>,
 * <a href="http://docs.basex.org/wiki/Java_Bindings">Java Bindings</a> and
 * <a href="http://docs.basex.org/wiki/User_Management">User Management</a>.
 * The XQuery 3.0 specification gives more insight into
 * <a href="http://www.w3.org/TR/xpath-functions-30/#properties-of-functions">function
 * properties</a>.</p>
 *
 * @author BaseX Team 2005-12, BSD License
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
    NONE;
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
   * {@link #context}, it should be annotated as {@link ContextDependent}.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface ContextDependent { }

  /**
   * Java code is treated as "focus-independent". If a function accesses the current
   * context item, position or size, it should be annotated as {@link FocusDependent}.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface FocusDependent { }

  /** Query context. */
  public QueryContext context;
}
