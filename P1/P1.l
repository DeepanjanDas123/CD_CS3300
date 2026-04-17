%{
    #include <bits/stdc++.h>
    #include "P1.tab.h"
    using namespace std;
%}

%option noyywrap yylineno

%%

"import"                    { return IMPORT; }
"java"                      { return JAVA; }
"util"                      { return UTIL; }
"function"                  { return SFUNCTION; }
"class"                     { return CLASS; }
"public"                    { return PUBLIC; }
"static"                    { return STATIC; }
"void"                      { return VOID; }
"main"                      { return MAIN; }
"String"                    { return STRING; }
"return"                    { return RETURN; }
"if"                        { return IF; }
"else"                      { return ELSE; }
"while"                     { return WHILE; }
"new"                       { return NEW; }
"this"                      { return THIS; }
"int"                       { return INT; }
"boolean"                   { return BOOLEAN; }
"extends"                   { return EXTENDS; }
"Function"                  { return FUNCTION; }
"System"                    { return SYSTEM; }
"out"                       { return OUT; }
"println"                   { return PRINTLN; }
"#define"                   { return DEFINE; }

"true"                      { return TRUEVAL; }
"false"                     { return FALSEVAL; }

"//".*              { /* skip line comment */ }
"/*"([^*]|\*+[^*/])*\*+"/"   { /* skip */ }


"&&"                        { return AND; }
"||"                        { return OR; }
"!="                        { return NEQ; }
"<="                        { return LEQ; }
"+"                         { yylval.val = strdup(yytext); return ADD; }
"-"                         { yylval.val = strdup(yytext); return ADD; }
"*"                         { yylval.val = strdup(yytext); return MUL; }
"/"                         { yylval.val = strdup(yytext); return MUL; }
"="                         { return ASSIGN; }
"."                         { return DOT; }
"->"                        { return ARROW; }
"!"                         { return NOT; }

"("                         { return LPAREN; }
")"                         { return RPAREN; }
"{"                         { return LBRACE; }
"}"                         { return RBRACE; }
"["                         { return LBRACK; }
"]"                         { return RBRACK; }
";"                         { return SCN; }
","                         { return COMMA; }
"<"                         { return LT; }
">"                         { return GT; }

[a-zA-Z_][a-zA-Z0-9_]*       { yylval.val = strdup(yytext); return IDENTIFIER; }
(0|[1-9][0-9]*)        { yylval.val = strdup(yytext); return INTEGER_LITERAL; }


[ \t\r\n]+                  { /* skip whitespace */ }

.                           { /* ignore unknown characters */ }

%%
