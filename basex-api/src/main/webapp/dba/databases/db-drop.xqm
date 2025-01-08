(:~
 : Drop databases.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';

(:~
 : Drops databases.
 : @param  $names  names of databases
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/dbs-drop')
  %rest:form-param('name', '{$names}')
function dba:dbs-drop(
  $names  as xs:string*
) {
  try {
    $names ! db:drop(.),
    utils:redirect($dba:CAT, { 'info': utils:info($names, 'database', 'dropped') })
  } catch * {
    utils:redirect($dba:CAT, { 'error': $err:description })
  }
};
