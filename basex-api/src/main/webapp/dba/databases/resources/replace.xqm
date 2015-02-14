(:~
 : Replace resource.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace html = 'dba/html' at '../../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../../modules/tmpl.xqm';
import module namespace web = 'dba/web' at '../../modules/web.xqm';

(:~ Top category :)
declare variable $_:CAT := 'databases';
(:~ Sub category :)
declare variable $_:SUB := 'database';

(:~
 : Form for replacing a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("dba/replace")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function _:replace(
  $name      as xs:string,
  $resource  as xs:string,
  $error     as xs:string?
) as element(html) {
  web:check(),
  tmpl:wrap(map { 'top': $_:SUB, 'error': $error },
    <tr>
      <td>
        <form action="replace" method="post" enctype="multipart/form-data">
          <input type="hidden" name="name" value="{ $name }"/>
          <input type="hidden" name="resource" value="{ $resource }"/>
          <h2>
            <a href="{ $_:CAT }">Databases</a> »
            { html:link($name, $_:SUB, map { 'name': $name } ) } »
            { html:link($resource, $_:SUB, map { 'name': $name, 'resource': $resource }) } »
            { html:button('replace', 'Replace') }
          </h2>
          <table>
            <tr>
              <td>
                <input type="file" name="input"/>
                { html:focus('input') }
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Replaces a database resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $input     file input
 :)
declare
  %updating
  %rest:POST
  %rest:path("dba/replace")
  %rest:form-param("name",     "{$name}")
  %rest:form-param("resource", "{$resource}")
  %rest:form-param("input",    "{$input}")
function _:replace-upload(
  $name      as xs:string,
  $resource  as xs:string,
  $input     as map(*)?
) {
  web:check(),
  try {
    let $content := $input(map:keys($input))
    let $file := map:keys($input)
    let $content := $input($file)
    let $raw := web:eval("db:is-raw($n, $r)", map { 'n': $name, 'r': $resource })
    return if($file) then (
      try {
        let $i := if($raw) then (
          $content
        ) else (
          convert:binary-to-string($content)
        )
        return web:update("db:replace($n, $r, $i)",
          map { 'n': $name, 'r': $resource, 'i': $i }
        ),
        web:redirect($_:SUB, map {
          'redirect': $_:SUB,
          'name': $name,
          'resource': $resource,
          'info': 'Replaced resource: ' || $resource
        })
      } catch * {
        error($err:code, replace($err:description, '^.*\): ', ''))
      }
    ) else (
      web:redirect("replace", map {
        'redirect': $_:SUB,
        'name': $name,
        'resource': $resource,
        'error': 'Please select a file to upload.'
      })
    )
  } catch * {
    web:redirect("replace", map {
      'error': $err:description,
      'name': $name,
      'resource': $resource
    })
  }
};
