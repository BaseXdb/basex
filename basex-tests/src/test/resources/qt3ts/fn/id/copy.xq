(:*******************************************************:)
(: Test: copy.xq                                         :)
(: Written By: Michael Kay                               :)
(: Date: 2011-08-10                                      :)
(: Purpose: A module containing one function, to copy    :)
(:  a node and thus create a parentless node             :)
(:*******************************************************:)

module namespace copy="http://www.w3.org/QT3/copy";

declare function copy:copy ($node as node())
{
   typeswitch ($node)
   case document-node()
       return document{$node/child::node()}
   case element()
       return element {node-name($node)} {$node/child::node()} 
   case attribute()
       return attribute {node-name($node)} {string($node)}
   case comment()
       return comment {string($node)}
   case processing-instruction()
       return processing-instruction {local-name($node)} {string($node)}
   case text()
       return text {string($node)}
   default
       return $node
};
