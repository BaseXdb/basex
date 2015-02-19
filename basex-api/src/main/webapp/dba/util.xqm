(:~
 : Utility services.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/util';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace tmpl = 'dba/tmpl' at 'modules/tmpl.xqm';
import module namespace web = 'dba/web' at 'modules/web.xqm';

(:~
 : Redirects to the start page.
 :)
declare
  %updating
  %rest:path("dba")
function _:redirect(
) {
  web:redirect('dba/databases')
};

(:~
 : Returns a file.
 : @param  $file  file or unknown path
 : @return rest response and binary file
 :)
declare
  %rest:path("dba/files/{$file=.+}")
function _:file(
  $file as xs:string
) as item()+ {
  let $path := file:base-dir() || 'files/' || $file
  return (
    <rest:response>
      <http:response>
        <http:header name="Cache-Control" value="max-age=3600,public"/>
      </http:response>
      <output:serialization-parameters>
        <output:media-type value='{ web:mime-type($path) }'/>
        <output:method value='raw'/>
      </output:serialization-parameters>
    </rest:response>,
    file:read-binary($path)
  )
};

(:~
 : Shows a page not found error.
 : @param  $unknown  unknown page
 : @return page
 :)
declare
  %rest:path("dba/{$unknown}")
  %output:method("html")
function _:any(
  $unknown  as xs:string
) as element(html) {
  web:check(),
  tmpl:wrap(
    <tr>
      <td>
        <h3>Page not found!</h3>
        <ul>
          <li>Page: <code>dba/{ $unknown }</code></li>
          <li>Method: <code>{ Request:method() }</code></li>
        </ul>
      </td>
    </tr>
  )
};

(:~
 : Login error: redirects to the login page.
 :)
declare
  %updating
  %rest:error("basex:login")
function _:error-login(
) {
  web:redirect("login")
};
