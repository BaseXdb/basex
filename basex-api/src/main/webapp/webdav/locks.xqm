(:~
 : WebDAV write locks.
 :
 : Locks are kept in a global, in-memory map keyed by the lock token, shared by
 : all clients and sessions. Operations that inspect the existing locks before
 : they register or drop one are wrapped in a single webdav:lock-update call,
 : which makes them atomic. Locks are not persisted; expired entries are
 : discarded by lock:purge.
 :
 : @author BaseX Team, BSD License
 :)
module namespace lock = 'webdav/locks';

declare namespace DAV = 'DAV:';

(:~ Lock timeout if a client does not request one, in seconds. :)
declare variable $lock:TIMEOUT := 3600;

(:~ Longest lock timeout that will be granted, in seconds. :)
declare variable $lock:MAX-TIMEOUT := 604800;

(:~ A write lock. :)
declare record lock:lock(
  token    as xs:string,
  path     as xs:string,
  depth    as xs:string,
  scope    as xs:string,
  timeout  as xs:integer,
  expires  as xs:dateTime,
  owner    as element(DAV:owner)?
);

(:~
 : Returns all locks that have not expired yet.
 : @return locks
 :)
declare function lock:active() as lock:lock* {
  lock:locks(map:items(webdav:locks()))
};

(:~
 : Returns the lock with the given token.
 : @param  $token  lock token
 : @return lock, or empty sequence
 :)
declare function lock:get(
  $token  as xs:string
) as lock:lock? {
  lock:locks(webdav:locks()($token))
};

(:~
 : Returns the locks that apply to a path.
 : @param  $path  path below the WebDAV root
 : @return locks
 :)
declare function lock:covering(
  $path  as xs:string
) as lock:lock* {
  lock:covering(lock:active(), $path)
};

(:~
 : Returns the locks on a path and on all of its descendants.
 : @param  $path  path below the WebDAV root
 : @return locks
 :)
declare function lock:descendants(
  $path  as xs:string
) as lock:lock* {
  lock:descendants(lock:active(), $path)
};

(:~
 : Checks whether a request may modify a path.
 : @param  $path    path below the WebDAV root
 : @param  $tokens  lock tokens supplied by the client
 : @return result of check
 :)
declare function lock:allows(
  $path    as xs:string,
  $tokens  as xs:string*
) as xs:boolean {
  (: an exclusive lock admits only its own token, shared locks any of theirs :)
  let $locks := lock:covering($path)
  return empty($locks) or (
    (every $lock in $locks[?scope = 'exclusive'] satisfies $lock?token = $tokens) and
    $locks?token = $tokens
  )
};

(:~
 : Discards expired locks.
 : @return empty sequence
 :)
declare function lock:purge() as empty-sequence() {
  webdav:lock-update(fn($entries) {
    map:filter($entries, fn($token, $lock) { exists(lock:locks($lock)) })
  }) => void()
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
) as lock:lock? {
  let $token := 'urn:uuid:' || random:uuid()
  let $lock := lock:lock($token, $path, $depth, $scope, $timeout, lock:expires($timeout), $owner)
  let $granted := webdav:lock-update(fn($entries) {
    let $locks := lock:locks(map:items($entries))
    (: two shared locks can coexist; an exclusive lock conflicts with every other one :)
    let $conflicts := (
      lock:covering($locks, $path),
      if ($depth = 'infinity') { lock:descendants($locks, $path) }
    )[not(?token = $tokens) and ($scope = 'exclusive' or ?scope = 'exclusive')]
    return if (exists($conflicts)) { $entries } else { map:put($entries, $token, $lock) }
  })
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
) as lock:lock? {
  let $lock := lock:get($token)
  where exists($lock)
  let $refreshed := $lock +:= { 'timeout': $timeout, 'expires': lock:expires($timeout) }
  (: the lock may have been released in the meantime :)
  let $stored := webdav:lock-update(fn($entries) {
    if (exists(lock:locks($entries($token)))) {
      map:put($entries, $token, $refreshed)
    } else {
      $entries
    }
  })
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
  webdav:lock-update(fn($entries) { map:remove($entries, $token) }) => void()
};

(:~
 : Releases all locks on a path and on its descendants.
 : @param  $path  path below the WebDAV root
 : @return empty sequence
 :)
declare function lock:release(
  $path  as xs:string
) as empty-sequence() {
  webdav:lock-update(fn($entries) {
    let $locks := lock:descendants(lock:locks(map:items($entries)), $path)
    return map:remove($entries, $locks?token)
  }) => void()
};

(:~
 : Returns the timeout to be granted for the time types of a Timeout request header.
 : @param  $header  requested time types, in descending order of preference
 : @return timeout, in seconds
 :)
declare function lock:timeout(
  $header  as xs:string*
) as xs:integer {
  let $seconds := head($header[matches(., 'Second-\d+')]) !
    xs:integer(replace(., '^.*?Second-(\d+).*$', '$1'))
  return min((
    $seconds otherwise (if (empty($header)) { $lock:TIMEOUT } else { $lock:MAX-TIMEOUT }),
    $lock:MAX-TIMEOUT
  ))
};

(:~
 : Returns the locks of a sequence of store entries that have not expired yet.
 : @param  $entries  store entries
 : @return locks
 :)
declare %private function lock:locks(
  $entries  as item()*
) as lock:lock* {
  let $locks as lock:lock* := $entries[. instance of lock:lock]
  return $locks[?expires gt current-dateTime()]
};

(:~
 : Returns the locks that apply to a path: a lock on the path itself, or a lock
 : with infinite depth on one of its ancestors.
 : @param  $locks  candidate locks
 : @param  $path   path below the WebDAV root
 : @return locks
 :)
declare function lock:covering(
  $locks  as lock:lock*,
  $path   as xs:string
) as lock:lock* {
  $locks[?path = $path or (?depth = 'infinity' and
    ($path = '' or starts-with($path, ?path || '/')))]
};

(:~
 : Returns the locks on a path and on all of its descendants.
 : @param  $locks  candidate locks
 : @param  $path   path below the WebDAV root
 : @return locks
 :)
declare %private function lock:descendants(
  $locks  as lock:lock*,
  $path   as xs:string
) as lock:lock* {
  $locks[?path = $path or starts-with(?path, $path || '/')]
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
