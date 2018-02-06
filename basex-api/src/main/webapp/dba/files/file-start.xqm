(:~
 : Start job.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace session = 'dba/session' at '../modules/session.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Starts a job.
 : @param  $file  file name
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/file-start")
  %rest:query-param("file", "{$file}", "")
function dba:file-start(
  $file  as xs:string
) as element(rest:response) {
  let $id := replace($file, '\.\.+|/|\\', '')
  let $params := try {
    (: stop running job before starting new job :)
    jobs:stop($id),
    prof:void(jobs:invoke(session:directory() || $id, (), map { 'cache': 'true', 'id': $file })),
    map { 'info': 'Job was started.', 'job': $id }
  } catch * {
    map { 'error': $err:description }
  }
  return web:redirect($dba:CAT, $params)
};
