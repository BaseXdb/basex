(:~
 : Stop jobs.
 :
 : @author Christian Grün, BaseX Team 2005-20, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Stops jobs.
 : @param  $ids  session ids
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path('/dba/file-stop')
  %rest:query-param('id',  '{$id}')
function dba:file-stop(
  $id  as xs:string
) as element(rest:response) {
  let $params := try {
    jobs:stop($id),
    map { 'info': util:info($id, 'job', 'stopped') }
  } catch * {
    map { 'error': $err:description }
  }
  return web:redirect($dba:CAT, $params)
};
