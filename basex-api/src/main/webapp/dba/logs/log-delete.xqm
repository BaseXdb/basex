(:~
 : Delete log files.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/logs';

import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Deletes database logs.
 : @param  $names  names of log files
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path('/dba/log-delete')
  %rest:query-param('name', '{$names}')
function dba:log-delete(
  $names  as xs:string*
) as element(rest:response) {
  try {
    $names ! admin:delete-logs(.),
    web:redirect($dba:CAT, map { 'info': util:info($names, 'log', 'deleted') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
