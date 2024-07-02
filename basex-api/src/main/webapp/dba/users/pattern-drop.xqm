(:~
 : Drop patterns.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:SUB := 'user';

(:~
 : Drops a pattern.
 : @param  $name      username
 : @param  $patterns  database patterns
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/pattern-drop')
  %rest:query-param('name',    '{$name}')
  %rest:query-param('pattern', '{$patterns}')
function dba:pattern-drop(
  $name      as xs:string,
  $patterns  as xs:string*
) as empty-sequence() {
  try {
    $patterns ! user:drop($name, .),
    utils:redirect($dba:SUB, { 'name': $name, 'info': utils:info($patterns, 'pattern', 'dropped') })
  } catch * {
    utils:redirect($dba:SUB, { 'error': $err:description })
  }
};
