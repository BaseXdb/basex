(:~
 : Delete resources.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Deletes resources.
 : @param  $names     database
 : @param  $resource  resources
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-delete')
  %rest:query-param('name',     '{$name}')
  %rest:query-param('resource', '{$resources}')
function dba:db-delete(
  $name       as xs:string,
  $resources  as xs:string*
) as empty-sequence() {
  try {
    $resources ! db:delete($name, .),
    utils:redirect($dba:SUB, { 'name': $name, 'info': utils:info($resources, 'resource', 'deleted') })
  } catch * {
    utils:redirect($dba:SUB, { 'name': $name, 'error': $err:description })
  }
};
