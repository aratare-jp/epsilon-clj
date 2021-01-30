lexer grammar Altio;

OPEN: '[%' -> pushMode(INSIDE);
TEXT: ANY_EXCEPT_OPEN+;
ANY_EXCEPT_OPEN
: '[' ~'%'
| ~'['
;

mode INSIDE;
CLOSE: '%]' -> popMode;
INSIDE_TEXT: ANY_EXCEPT_CLOSE+;
ANY_EXCEPT_CLOSE
: '%' ~']'
| ~'%'
;