(:~
 : Common RESTXQ access points.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/common';

import module namespace html = 'dba/html' at 'lib/html.xqm';
import module namespace utils = 'dba/utils' at 'lib/utils.xqm';

(:~
 : Redirects to the start page.
 : @return redirection
 :)
declare
  %rest:path('/dba')
function dba:redirect(
) as element(rest:response) {
  web:redirect('/dba/logs')
};

(:~
 : Returns a file.
 : @param  $file  file or unknown path
 : @return rest binary data
 :)
declare
  %rest:path('/dba/static/{$file=.+}')
  %output:method('basex')
  %perm:allow('public')
function dba:file(
  $file  as xs:string
) as item()+ {
  let $path := utils:safe-path(file:base-dir() || 'static/', $file)
  return if (file:is-file($path)) {
    web:response-header(
      { 'media-type': web:content-type($path) },
      { 'Cache-Control': 'max-age=3600,public', 'Content-Length': file:size($path) }
    ),
    file:read-binary($path)
  } else {
    web:error(404, 'Resource not found.')
  }
};

(:~
 : Shows a 'page not found' error.
 : @param  $path  path to unknown page
 : @return page
 :)
declare
  %rest:path('/dba/{$path}')
  %output:method('html')
function dba:unknown(
  $path  as xs:string
) as element()+ {
  web:response-header((), (), { 'status': 404 }),
  <tr>
    <td>
      <h2>Page not found:</h2>
      <ul>
        <li>Page: dba/{ $path }</li>
        <li>Method: { request:method() }</li>
      </ul>
    </td>
  </tr>
  => html:wrap()
};
