(:~
 : Replace resource.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';
import module namespace html = 'dba/html' at '../../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for replacing a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/replace")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function dba:replace(
  $name      as xs:string,
  $resource  as xs:string,
  $error     as xs:string?
) as element(html) {
  cons:check(),
  tmpl:wrap(map { 'top': $dba:SUB, 'error': $error },
    <tr>
      <td>
        <form action="replace" method="post" enctype="multipart/form-data">
          <input type="hidden" name="name" value="{ $name }"/>
          <input type="hidden" name="resource" value="{ $resource }"/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:link($resource, $dba:SUB, map { 'name': $name, 'resource': $resource }), ' » ',
            html:button('replace', 'Replace')
          }</h2>
          <table>
            <tr>
              <td>
                <input type="file" name="file"/>
                { html:focus('file') }
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
 : @param  $file      file input
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/replace")
  %rest:form-param("name",     "{$name}")
  %rest:form-param("resource", "{$resource}")
  %rest:form-param("file",     "{$file}")
function dba:replace-upload(
  $name      as xs:string,
  $resource  as xs:string,
  $file      as map(*)?
) {
  cons:check(),
  try {
    let $key := map:keys($file)
    return if($key = '') then (
      error((), "No input specified.")
    ) else (
      let $input := if(db:is-raw($name, $resource)) then (
        $file($key)
      ) else (
        fetch:xml-binary($file($key))
      )
      return db:replace($name, $resource, $input),
      cons:redirect($dba:SUB, map {
        'name': $name, 'resource': $resource, 'info': 'Replaced resource: ' || $resource
      })
    )
  } catch * {
    cons:redirect("replace", map {
      'error': $err:description, 'name': $name, 'resource': $resource
    })
  }
};
