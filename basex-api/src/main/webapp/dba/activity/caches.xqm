(:~
 : Cache actions of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/caches';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'activity';

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
    'info': utils:info($args?cache, 'cache', 'deleted'),
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
    'info': 'All caches and their options were cleared.',
    'run' : %updating fn() { cache:clear() }
  } })
};
