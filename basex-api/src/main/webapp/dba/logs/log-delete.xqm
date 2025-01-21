(:~
 : Delete log files.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/logs';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Deletes database logs.
 : @param  $names  names of log files
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/logs-delete')
  %rest:form-param('name', '{$names}')
function dba:logs-delete(
  $names  as xs:string*
) as element(rest:response) {
  try {
    $names ! admin:delete-logs(.),
    web:redirect($dba:CAT, { 'info': utils:info($names, 'log', 'deleted') })
  } catch * {
    web:redirect($dba:CAT, { 'error': $err:description })
  }
};
