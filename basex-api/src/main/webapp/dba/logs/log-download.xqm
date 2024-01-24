(:~
 : Download log file.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/logs';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Downloads database logs.
 : @param  $names  names (dates) of log files
 : @return single or zipped file
 :)
declare
  %rest:GET
  %rest:path('/dba/log-download')
  %rest:query-param('name', '{$names}')
function dba:log-download(
  $names  as xs:string*
) as item()+ {
  if (count($names) = 1) then (
    web:response-header(
      map { 'media-type': 'text/plain' },
      map { 'Content-Disposition': 'attachment; filename=' || $names || '.log' }
    ),
    file:read-binary(db:option('dbpath') || '/.logs/' || $names || '.log')
  ) else (
    web:response-header(
      map { 'media-type': 'application/zip' },
      map { 'Content-Disposition': 'attachment; filename=' ||
        string-join(sort($names)[position() = (1, last())], '_') || '.zip' }
    ),
    let $logs := $names ! (. || '.log')
    return archive:create(
      $logs,
      $logs ! file:read-binary(db:option('dbpath') || '/.logs/' || .)
    )
  )
};
