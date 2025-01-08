(:~
 : Delete resources.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Deletes resources.
 : @param  $name      database
 : @param  $resource  resources
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-delete')
  %rest:form-param('name',     '{$name}')
  %rest:form-param('resource', '{$resources}')
function dba:db-delete(
  $name       as xs:string,
  $resources  as xs:string*
) {
  try {
    $resources ! db:delete($name, .),
    utils:redirect($dba:SUB, { 'name': $name, 'info': utils:info($resources, 'resource', 'deleted') })
  } catch * {
    utils:redirect($dba:SUB, { 'name': $name, 'error': $err:description })
  }
};
