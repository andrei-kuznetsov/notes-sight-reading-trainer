grammar Arrays;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

topLevel: array;

array: '[' array_contents ']';

array_contents: (object (',' object)* (',')?)?;

object: '{' object_contents '}' | array;

object_contents: (object_property (',' object_property)* (',')?)?;

object_property: propName ':' propValue;

propName: quoted_string;

propValue: quoted_string;

quoted_string: StringLiteral+;

StringLiteral
 : '"' DoubleStringCharacter* '"'
 | '\'' SingleStringCharacter* '\''
 ;

fragment DoubleStringCharacter: ~["\\\r\n] 
                     | '\\' EscapeSequence
                     ;

fragment SingleStringCharacter: ~['\\\r\n] 
                     | '\\' EscapeSequence
                     ;

fragment EscapeSequence: CharacterEscapeSequence
 | '0' // no digit ahead! TODO
 | HexEscapeSequence
 | UnicodeEscapeSequence
 ;

fragment CharacterEscapeSequence
 : SingleEscapeCharacter
 | NonEscapeCharacter
 ;
fragment HexEscapeSequence
 : 'x' HexDigit HexDigit
 ;
fragment UnicodeEscapeSequence
 : 'u' HexDigit HexDigit HexDigit HexDigit
 ;

fragment SingleEscapeCharacter
 : ['"\\bfnrtv]
 ;

fragment NonEscapeCharacter
 : ~['"\\bfnrtv0-9xu\r\n]
 ;

fragment HexDigit
 : [0-9a-fA-F]
 ;