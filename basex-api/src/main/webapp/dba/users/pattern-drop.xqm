(:~
 : Drop patterns.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:SUB := 'user';

(:~
 : Drop pattern.
 : @param  $name      username
 : @param  $patterns  database patterns
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/pattern-drop')
  %rest:form-param('name',    '{$name}')
  %rest:form-param('pattern', '{$patterns}')
function dba:pattern-drop(
  $name      as xs:string,
  $patterns  as xs:string*
) {
  try {
    $patterns ! user:drop($name, .),
    utils:redirect($dba:SUB, { 'name': $name, 'info': utils:info($patterns, 'pattern', 'dropped') })
  } catch * {
    utils:redirect($dba:SUB, { 'error': $err:description })
  }
};
