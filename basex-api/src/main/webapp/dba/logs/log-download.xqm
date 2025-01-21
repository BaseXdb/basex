(:~
 : Download log file.
 :
 : @author Christian Grün, BaseX Team, BSD License
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
  %rest:POST
  %rest:path('/dba/logs-download')
  %rest:form-param('name', '{$names}')
function dba:logs-download(
  $names  as xs:string*
) as item()+ {
  if (count($names) = 1) {
    web:response-header(
      { 'media-type': 'text/plain' },
      { 'Content-Disposition': 'attachment; filename=' || $names || '.log' }
    ),
    file:read-binary(db:option('dbpath') || '/.logs/' || $names || '.log')
  } else {
    web:response-header(
      { 'media-type': 'application/zip' },
      { 'Content-Disposition': 'attachment; filename=' ||
        string-join(sort($names)[position() = (1, last())], '_') || '.zip' }
    ),
    let $logs := $names ! (. || '.log')
    return archive:create(
      $logs,
      $logs ! file:read-binary(db:option('dbpath') || '/.logs/' || .)
    )
  }
};
