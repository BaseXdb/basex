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
 : Runs a cache action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/caches/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    'delete': fn($args) { {
      'info': utils:info($args?cache, 'cache', 'deleted'),
      'run' : %updating fn() { $args?cache ! cache:delete(.) }
    } },
    'clear': fn($args) { {
      'info': 'All caches were cleared.',
      'run' : %updating fn() { cache:clear() }
    } }
  })
};
