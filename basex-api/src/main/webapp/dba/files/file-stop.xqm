(:~
 : Evaluates a file.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Stops a running job.
 : @param  $id  job id
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/file-stop")
  %rest:query-param("id", "{$id}", "")
function dba:file-stop(
  $id  as xs:string
) as item()+ {
  cons:check(),
  let $params := try {
    jobs:stop($id),
    map { 'info': 'Job "' || $id || '" stopped.' }
  } catch * {
    map { 'error': $err:description }
  }
  return web:redirect($dba:CAT, $params)
};
