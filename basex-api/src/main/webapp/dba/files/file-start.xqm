(:~
 : Start job.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Starts a job.
 : @param  $file  file name
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path('/dba/file-start')
  %rest:query-param('file', '{$file}', '')
function dba:file-start(
  $file  as xs:string
) as element(rest:response) {
  let $id := replace($file, '\.\.+|/|\\', '')
  let $uri := xs:anyURI(config:directory() || $id)
  let $params := try {
    (: stop running job before starting new job :)
    job:remove($id),
    job:wait($id),
    prof:void(job:eval($uri, (), map { 'cache': 'true', 'id': $id, 'log': 'DBA job' })),
    map { 'info': 'Job was started.', 'job': $id }
  } catch * {
    map { 'error': $err:description }
  }
  return web:redirect($dba:CAT, $params)
};
