(:~
 : Downloads a job result.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Downloads the result of a job.
 : @param  $id    job id
 : @param  $file  file name (ignored, but relevant for client)
 : @return binary data
 :)
declare
  %rest:GET
  %rest:path("/dba/job-result/{$file}")
  %rest:query-param("id", "{$id}", "")
function dba:job-result(
  $id    as xs:string,
  $file  as xs:string
) as item()+ {
  cons:check(),
  let $details := jobs:list-details($id)
  return if(empty($details)) then (
    web:redirect('../' || $dba:CAT, map { 'error': 'Job is defunct.' })
  ) else if($details/@state != 'cached') then (
    web:redirect('../' || $dba:CAT, map { 'error': 'Result is not available yet.' })
  ) else (
    web:response-header(map { }, map { 'Cache-Control': '' }),
    try {
      jobs:result($id)
    } catch * {
      'Stopped at ' || $err:module || ', ' || $err:line-number || '/' ||
        $err:column-number || ':' || out:nl() || $err:description
    }
  )
};
