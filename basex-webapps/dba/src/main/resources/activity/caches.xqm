(:~
 : Cache actions of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/caches';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'activity';

(:~
 : Returns the keys of a cache.
 : @param  $name  name of the cache
 : @return keys, and the total number of keys
 :)
declare
  %rest:path('/dba/cache-keys')
  %rest:query-param('name', '{$name}', '')
  %output:method('json')
function dba:cache-keys(
  $name  as xs:string
) as map(*) {
  let $keys := cache:keys($name)
  return {
    'keys' : array { sort($keys, '?lang=en')[position() <= $config:MAX-SHOWN] },
    'count': count($keys)
  }
};

(:~
 : Returns the value of a cache entry, as the expression that yields it again.
 : @param  $name  name of the cache
 : @param  $key   key of the entry
 : @return expression, and the reason why it cannot be edited
 :)
declare
  %rest:path('/dba/cache-value')
  %rest:query-param('name', '{$name}', '')
  %rest:query-param('key',  '{$key}')
  %output:method('json')
function dba:cache-value(
  $name  as xs:string,
  $key   as xs:string
) as map(*) {
  (: reading the value counts as a hit, and marks the entry as recently used :)
  utils:value(cache:get($key, $name))
};

(:~
 : Assigns a cache entry.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/caches/put')
function dba:put() {
  (: an entry holds any XQuery value: it is supplied as the expression that yields it :)
  utils:dispatch($dba:CAT, fn($args) { {
    'info': utils:info($args?key, 'entry', 'saved'),
    'run' : %updating fn() {
      cache:put($args?key, utils:evaluate($args?value), string($args?name))
    }
  } })
};

(:~
 : Removes cache entries.
 : @param  $name  name of the cache
 : @param  $keys  keys of the entries
 : @return info message
 :)
declare
  %rest:POST
  %rest:path('/dba/cache-remove')
  %rest:query-param('name', '{$name}', '')
  %rest:query-param('key',  '{$keys}')
  %output:method('text')
function dba:remove(
  $name  as xs:string,
  $keys  as xs:string*
) as xs:string {
  (: the dialog stays open: the outcome is reported, and the keys are listed again :)
  $keys ! cache:remove(., $name),
  utils:info($keys, 'entry', 'removed')
};

(:~
 : Deletes caches.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/caches/delete')
function dba:delete() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': utils:info($args?cache ! utils:label(.), 'cache', 'deleted'),
    'run' : %updating fn() { $args?cache ! cache:delete(.) }
  } })
};

(:~
 : Clears all caches.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/caches/clear')
function dba:clear() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': 'All caches and their options were deleted.',
    'run' : %updating fn() { cache:clear() }
  } })
};
