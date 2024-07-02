(:~
 : Drop users.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Drops users.
 : @param  $names  names of users
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/user-drop')
  %rest:query-param('name', '{$names}')
function dba:user-drop(
  $names  as xs:string*
) as empty-sequence() {
  try {
    $names ! user:drop(.),
    utils:redirect($dba:CAT, { 'info': utils:info($names, 'user', 'dropped') })
  } catch * {
    utils:redirect($dba:CAT, { 'error': $err:description })
  }
};
