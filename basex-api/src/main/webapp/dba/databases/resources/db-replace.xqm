(:~
 : Replace resource.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../modules/html.xqm';
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
  %rest:path("/dba/db-replace")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function dba:db-replace(
  $name      as xs:string,
  $resource  as xs:string,
  $error     as xs:string?
) as element(html) {
  html:wrap(map { 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form action="db-replace" method="post" enctype="multipart/form-data">
          <input type="hidden" name="name" value="{ $name }"/>
          <input type="hidden" name="resource" value="{ $resource }"/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:link($resource, $dba:SUB, map { 'name': $name, 'resource': $resource }), ' » ',
            html:button('db-replace', 'Replace')
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
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/db-replace")
  %rest:form-param("name",     "{$name}")
  %rest:form-param("resource", "{$resource}")
  %rest:form-param("file",     "{$file}")
function dba:db-replace-post(
  $name      as xs:string,
  $resource  as xs:string,
  $file      as map(*)?
) as empty-sequence() {
  try {
    let $key := map:keys($file)
    return if($key = '') then (
      error((), 'No input specified.')
    ) else (
      let $input := if(db:is-raw($name, $resource)) then (
        $file($key)
      ) else (
        fetch:xml-binary($file($key))
      )
      return db:replace($name, $resource, $input),
      util:redirect($dba:SUB, map {
        'name': $name, 'resource': $resource, 'info': 'Resource was replaced.'
      })
    )
  } catch * {
    util:redirect('db-replace', map {
      'name': $name, 'resource': $resource, 'error': $err:description
    })
  }
};
