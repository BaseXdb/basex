package org.basex.core;

/**
 * Marks an external object as bound to the current client request. Such objects are dropped
 * from a {@link Context} that is {@link Context#detach() detached} for asynchronous execution,
 * so that a background job cannot access request state that the server may have recycled.
 *
 * @author BaseX Team, BSD License
 */
public interface RequestScope {
}
