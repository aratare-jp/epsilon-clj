grammar Altio;

// Parser
file: (text
| statement
| statement_newline
| expression
| expression_newline
| comment
| comment_newline)* EOF;
text: (TEXT | WHITESPACE | NEWLINE)+;

open_bracket_statement: OPEN_BRACKET_STATEMENT;
open_bracket_statement_newline: OPEN_BRACKET_STATEMENT_NEWLINE;
open_bracket_expression: OPEN_BRACKET_EXPRESSION;
open_bracket_expression_newline: OPEN_BRACKET_EXPRESSION_NEWLINE;
close_bracket: CLOSE_BRACKET;

open_bracket_comment: OPEN_BRACKET_COMMENT;
open_bracket_comment_newline: OPEN_BRACKET_COMMENT_NEWLINE;
close_bracket_comment: CLOSE_BRACKET_COMMENT;

statement: open_bracket_statement text close_bracket;
statement_newline: open_bracket_statement_newline text close_bracket;
expression: open_bracket_expression text close_bracket;
expression_newline: open_bracket_expression_newline text close_bracket;
comment: open_bracket_comment text close_bracket_comment;
comment_newline: open_bracket_comment_newline text close_bracket_comment;

fragment EXCLUDED: ;

// Lexer
TEXT: .;
WHITESPACE: (' ' | '\t') -> channel(HIDDEN) ;
NEWLINE: ('\n' | '\r\n' | '\r');

OPEN_BRACKET_STATEMENT: '[%';
OPEN_BRACKET_STATEMENT_NEWLINE: '[%n';
OPEN_BRACKET_EXPRESSION: '[%=';
OPEN_BRACKET_EXPRESSION_NEWLINE: '[%=n';
CLOSE_BRACKET: '%]';

OPEN_BRACKET_COMMENT: '[*';
OPEN_BRACKET_COMMENT_NEWLINE: '[*n';
CLOSE_BRACKET_COMMENT: '*]';
