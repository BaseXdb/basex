(:~
 : Downloads a job result.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/jobs';

(:~ Top category :)
declare variable $dba:CAT := 'jobs';

(:~
 : Downloads the result of a job.
 : @param  $id  job id
 : @return rest response and file content
 :)
declare
  %rest:GET
  %rest:path("/dba/job-result")
  %rest:query-param("id", "{$id}", "")
function dba:job-result(
  $id  as xs:string
) as item()+ {
  let $details := jobs:list-details($id)
  return if(empty($details)) then (
    dba:job-result($id, false(), 'Job is defunct.')
  ) else if($details/@state != 'cached') then (
    dba:job-result($id, false(), 'Result is not available yet.')
  ) else (
    try {
      dba:job-result($id, true(), jobs:result($id))
    } catch * {
      dba:job-result($id, false(),
        'Stopped at ' || $err:module || ', ' || $err:line-number || '/' ||
          $err:column-number || ':' || out:nl() || $err:description
      )
    }
  )
};

(:~
 : Returns a job result.
 : @param $id      job id
 : @param $ok      ok flag
 : @param $result  job result
 : @return rest response and file content
 :)
declare %private function dba:job-result(
  $id      as xs:string,
  $ok      as xs:boolean,
  $result  as item()*
) as item()+ {
  let $name := $id || (if($ok) then '.txt' else '.log')
  return web:response-header(
    map { 'media-type': 'application/octet-stream' },
    map { 'Content-Disposition': 'attachment; filename=' || $name }
  ),
  $result
};
