(:~
 : WebDAV write locks, backed by the Store Module.
 :
 : Locks are kept in a named store, with the lock token as key. The store is
 : global and synchronized, so locks are shared by all clients and sessions.
 : Operations that inspect the existing locks before they register or drop one
 : are wrapped in a single store:update call, which makes them atomic. Stores
 : are written to disk when the database context is closed, so locks survive a
 : restart; expired entries are discarded by lock:purge.
 :
 : @author BaseX Team, BSD License
 :)
module namespace lock = 'webdav/locks';

declare namespace DAV = 'DAV:';

(:~ Name of the store that holds the locks. :)
declare variable $lock:STORE := 'webdav-locks';

(:~ Lock timeout if a client does not request one, in seconds. :)
declare variable $lock:TIMEOUT := 3600;

(:~ Longest lock timeout that will be granted, in seconds. :)
declare variable $lock:MAX-TIMEOUT := 604800;

(:~
 : Returns all locks that have not expired yet.
 : @return locks
 :)
declare function lock:active() as element(lock)* {
  lock:locks(store:keys($lock:STORE) ! store:get(., $lock:STORE))
};

(:~
 : Returns the lock with the given token.
 : @param  $token  lock token
 : @return lock, or empty sequence
 :)
declare function lock:get(
  $token  as xs:string
) as element(lock)? {
  lock:locks(store:get($token, $lock:STORE))
};

(:~
 : Returns the locks that apply to a path.
 : @param  $path  path below the WebDAV root
 : @return locks
 :)
declare function lock:covering(
  $path  as xs:string
) as element(lock)* {
  lock:covering(lock:active(), $path)
};

(:~
 : Returns the locks on a path and on all of its descendants.
 : @param  $path  path below the WebDAV root
 : @return locks
 :)
declare function lock:descendants(
  $path  as xs:string
) as element(lock)* {
  lock:descendants(lock:active(), $path)
};

(:~
 : Checks whether a request may modify a path: every lock that applies to it
 : must have been submitted in the If header.
 : @param  $path    path below the WebDAV root
 : @param  $tokens  lock tokens supplied by the client
 : @return result of check
 :)
declare function lock:allows(
  $path    as xs:string,
  $tokens  as xs:string*
) as xs:boolean {
  every $lock in lock:covering(lock:active(), $path) satisfies $lock/@token = $tokens
};

(:~
 : Discards expired locks.
 : @return empty sequence
 :)
declare function lock:purge() as empty-sequence() {
  store:update(fn($entries) {
    map:build(lock:locks(map:items($entries)), fn($lock) { string($lock/@token) })
  }, $lock:STORE) => void()
};

(:~
 : Registers a lock, unless a conflicting lock exists. The check and the write
 : are performed in a single atomic operation.
 : @param  $path     path below the WebDAV root
 : @param  $depth    lock depth ('0' or 'infinity')
 : @param  $scope    lock scope ('exclusive' or 'shared')
 : @param  $owner    owner supplied by the client
 : @param  $timeout  requested timeout, in seconds
 : @param  $tokens   lock tokens supplied by the client
 : @return new lock, or empty sequence if a conflicting lock exists
 :)
declare function lock:acquire(
  $path     as xs:string,
  $depth    as xs:string,
  $scope    as xs:string,
  $owner    as element(DAV:owner)?,
  $timeout  as xs:integer,
  $tokens   as xs:string*
) as element(lock)? {
  let $token := 'urn:uuid:' || random:uuid()
  let $lock :=
    <lock token='{ $token }' path='{ $path }' depth='{ $depth }' scope='{ $scope }'
          timeout='{ $timeout }' expires='{ lock:expires($timeout) }'>{ $owner }</lock>
  let $granted := store:update(fn($entries) {
    let $locks := lock:locks(map:items($entries))
    let $conflicts := (
      lock:covering($locks, $path),
      if ($depth = 'infinity') { lock:descendants($locks, $path) }
    )[not(@token = $tokens)]
    return if (exists($conflicts)) { $entries } else { map:put($entries, $token, $lock) }
  }, $lock:STORE)
  return if ($granted) { $lock }
};

(:~
 : Extends the lifetime of a lock.
 : @param  $token    lock token
 : @param  $timeout  requested timeout, in seconds
 : @return refreshed lock, or empty sequence if the token is unknown
 :)
declare function lock:refresh(
  $token    as xs:string,
  $timeout  as xs:integer
) as element(lock)? {
  let $lock := lock:get($token)
  where exists($lock)
  let $refreshed :=
    copy $copy := $lock
    modify (
      replace value of node $copy/@timeout with $timeout,
      replace value of node $copy/@expires with lock:expires($timeout)
    )
    return $copy
  (: the lock may have been released in the meantime :)
  let $stored := store:update(fn($entries) {
    if (exists(lock:locks($entries($token)))) {
      map:put($entries, $token, $refreshed)
    } else {
      $entries
    }
  }, $lock:STORE)
  return if ($stored) { $refreshed }
};

(:~
 : Releases a lock.
 : @param  $token  lock token
 : @return empty sequence
 :)
declare function lock:remove(
  $token  as xs:string
) as empty-sequence() {
  store:remove($token, $lock:STORE)
};

(:~
 : Releases all locks on a path and on its descendants.
 : @param  $path  path below the WebDAV root
 : @return empty sequence
 :)
declare function lock:release(
  $path  as xs:string
) as empty-sequence() {
  store:update(fn($entries) {
    let $locks := lock:descendants(lock:locks(map:items($entries)), $path)
    return map:remove($entries, $locks ! string(@token))
  }, $lock:STORE) => void()
};

(:~
 : Returns the timeout to be granted for a Timeout request header.
 : @param  $header  header value
 : @return timeout, in seconds
 :)
declare function lock:timeout(
  $header  as xs:string?
) as xs:integer {
  let $requested :=
    if (matches($header, 'Second-\d+')) {
      xs:integer(replace($header, '^.*?Second-(\d+).*$', '$1'))
    } else if ($header = '') {
      $lock:TIMEOUT
    } else {
      $lock:MAX-TIMEOUT
    }
  return min(($requested, $lock:MAX-TIMEOUT))
};

(:~
 : Returns the locks of a sequence of store entries that have not expired yet.
 : @param  $entries  store entries
 : @return locks
 :)
declare %private function lock:locks(
  $entries  as item()*
) as element(lock)* {
  let $now := current-dateTime()
  let $locks as element(lock)* :=
    for $entry in $entries
    where $entry instance of element(lock)
    return $entry
  return $locks[xs:dateTime(@expires) gt $now]
};

(:~
 : Returns the locks that apply to a path: a lock on the path itself, or a lock
 : with infinite depth on one of its ancestors.
 : @param  $locks  candidate locks
 : @param  $path   path below the WebDAV root
 : @return locks
 :)
declare %private function lock:covering(
  $locks  as element(lock)*,
  $path   as xs:string
) as element(lock)* {
  $locks[
    @path = $path or
    (@depth = 'infinity' and ($path = '' or @path = '' or starts-with($path, @path || '/')))
  ]
};

(:~
 : Returns the locks on a path and on all of its descendants.
 : @param  $locks  candidate locks
 : @param  $path   path below the WebDAV root
 : @return locks
 :)
declare %private function lock:descendants(
  $locks  as element(lock)*,
  $path   as xs:string
) as element(lock)* {
  $locks[@path = $path or starts-with(@path, $path || '/')]
};

(:~
 : Returns the point in time at which a lock will expire.
 : @param  $timeout  timeout, in seconds
 : @return timestamp
 :)
declare %private function lock:expires(
  $timeout  as xs:integer
) as xs:dateTime {
  current-dateTime() + xs:dayTimeDuration('PT' || $timeout || 'S')
};
