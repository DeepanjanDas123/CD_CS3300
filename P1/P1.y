%{
    #include <bits/stdc++.h>

    using namespace std;

    // Forward declaration for yylex
    int yylex(void);
    void yyerror(const char *s);

    extern int yylineno;
    extern char* yytext;

    int global_ind = 0;

    char* indent() {
        int spaces = global_ind * 4;
        char* res = (char*)malloc(spaces + 1);
        memset(res, ' ', spaces);
        res[spaces] = '\0';
        return res;
    } 

    char* safe_dup(const char* s) {return s ? strdup(s) : strdup(""); }

    //struct for tokens, useful in macro replacing
    struct Token{
        char* type;
        char* lexeme;
    };

    //helper functions for macro capture and expansion
    //note that we only do expansion during capture, thus handling for redefining macros between use in separate macro and calling of the implementing macro

    struct Macro{
        char* macro_name;
        vector<string> paramlist;
        string macro_body;

        bool isExpr;

        Macro(char* name, char* paramlistopt, char* body, bool bl) : macro_name(name), macro_body(body), isExpr(bl) {
            //resolve paramlistopt to vector<char*>
            if (paramlistopt) {
                stringstream ss(paramlistopt);
                string token;
                while (getline(ss, token, ',')) {
                    token.erase(0, token.find_first_not_of(" \t\n")); // trim left
                    token.erase(token.find_last_not_of(" \t\n")+1);   // trim right
                    if (!token.empty()) paramlist.push_back(token);
                }
            }
        } 

        string expand(const char* arglist_cstr);

    };

    unordered_map<string, Macro*> macro_table;
    unordered_map<string, Macro*> st_macro_table;
    unordered_map<string, Macro*> exp_macro_table;

    string Macro::expand(const char* arglist_cstr){
        string arglist = arglist_cstr ? string(arglist_cstr) : "";

        vector<string> args;

        if (!arglist.empty()) {
            stringstream ss(arglist);
            string token;
            int depth = 0;
            string current;
            for (char c : arglist) {
                if (c == ',' && depth == 0) {
                    current.erase(0, current.find_first_not_of(" \t\n"));
                    current.erase(current.find_last_not_of(" \t\n")+1);
                    if (isExpr && !current.empty() && current.front() != '('){
                      regex identifier_re(R"(^[A-Za-z_]\w*$)");  // matches a simple identifier
                      if (!regex_match(current, identifier_re)) {
                          current = "(" + current + ")";
                      }
                    }
                    args.push_back(current);
                    current.clear();
                } else {
                    if (c == '(') depth++;
                    if (c == ')') depth--;
                    current += c;
                }
            }
            if (!current.empty()) {
                current.erase(0, current.find_first_not_of(" \t\n"));
                current.erase(current.find_last_not_of(" \t\n")+1);
                if (!current.empty() && current.front() != '('){
                  regex identifier_re(R"(^[A-Za-z_]\w*$)");  // matches a simple identifier
                  if (!regex_match(current, identifier_re)) {
                      current = "(" + current + ")";
                  }
                }
                args.push_back(current);
            }
        }

        if (args.size() != paramlist.size()) {
            yyerror("Wrong argument list");
        }

        // map param -> arg
        unordered_map<string, string> repl;
        for (size_t i = 0; i < paramlist.size(); ++i) {
            repl[paramlist[i]] = args[i];
        }

        // replace all param occurrences in macro_body with args
        string expanded = macro_body;
        for (auto &p : repl) {
            regex re("\\b" + p.first + "\\b");  // only match whole words
            expanded = regex_replace(expanded, re, p.second);
        }

        return expanded;

    }

%}

%define parse.error verbose

%locations

%union {       
    char *val; 
}

%token CLASS PUBLIC STATIC VOID MAIN STRING
%token RETURN IF ELSE WHILE
%token TRUEVAL FALSEVAL THIS NEW INT BOOLEAN EXTENDS
%token IMPORT JAVA UTIL SFUNCTION FUNCTION
%token DEFINE 
%glr-parser
%token SYSTEM OUT PRINTLN

%token <val> IDENTIFIER
%token <val> INTEGER_LITERAL


%token AND OR NEQ LEQ
%token <val> ADD MUL 
%token ASSIGN NOT
%token ARROW     
%token YYEOF      


%token LPAREN RPAREN LBRACE RBRACE LBRACK RBRACK
%token DOT COMMA SCN LT GT

%type <val> Goal OptImport MacroDefList MacroDef MacroDefStatement MacroDefExpression ParamListOpt ParamList MainClass TypeDeclList TypeDecl VarDeclList VarDecl MethodDeclList MethodDecl FormalListOpt FormalList StatementList Statement MatchedStmt UnmatchedStmt Block PrintStmt AssignmentStmt IfThenElseStmt WhileStmt MacroInvocationStmt Expression OrExpr AndExpr EqExpr RelExpr AddExpr MulExpr UnaryExpr PostfixExpr ArgListOpt ArgList Primary MacroInvocationSt MacroInvocationExp Type LambdaExpr



%left OR
%left AND
%left NEQ LEQ
%left ADD
%left MUL
%right NOT


%start Goal

%%

Goal
  : OptImport MacroDefList MainClass TypeDeclList YYEOF {
    cout<<$1<<$3<<"\n\n"<<$4<<'\n';
  }
  ;

OptImport
  : IMPORT JAVA DOT UTIL DOT SFUNCTION DOT FUNCTION SCN{
    size_t len = 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "import java.util.function.Function;\n\n");

    $$ = act;
  }
  | {
    $$ = "";
  }
  ;

MacroDefList
  : MacroDef MacroDefList {
    $$ = NULL;
  }
  | { $$ = NULL;}
  ;

MacroDef
  : MacroDefStatement {$$ = NULL;}
  | MacroDefExpression {$$ = NULL;}
  ;

MacroDefStatement
  : DEFINE IDENTIFIER LPAREN ParamListOpt RPAREN Block {
    Macro* m = new Macro($2, $4, $6, false);
    st_macro_table[m->macro_name] = m;
    macro_table[m->macro_name] = m;
  }
  ;

MacroDefExpression
  : DEFINE IDENTIFIER LPAREN ParamListOpt RPAREN Expression {
    Macro* m = new Macro($2, $4, $6, true);
    exp_macro_table[m->macro_name] = m;
    macro_table[m->macro_name] = m;
  }
  ;

ParamListOpt
  : ParamList { $$ = safe_dup($1); }
  | /* empty */{ $$ = NULL; }
  ;

ParamList
  : IDENTIFIER { $$ = safe_dup($1); }
  | IDENTIFIER COMMA ParamList {
    char* id = safe_dup($1);

    char* paramlist = safe_dup($3);

    size_t len = strlen(id) + strlen(paramlist) + 10;

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "%s, %s", id, paramlist);

    free(id); free(paramlist);

    $$ = act;
  }
  ;

MainClass
  : CLASS IDENTIFIER
    LBRACE
      PUBLIC STATIC VOID MAIN
      LPAREN STRING LBRACK RBRACK IDENTIFIER RPAREN
      LBRACE {global_ind++; } PrintStmt {global_ind --; } RBRACE
    RBRACE {
        char* ind = indent();
        char* cls = safe_dup($2);
        char* arg = safe_dup($12);
        char* expr = safe_dup($16);
        size_t len = strlen(ind) + strlen("class ") + strlen(cls) + 5 + strlen(ind)
                     + 200 + strlen(expr) + 50;
        char* out = (char*)malloc(len);
        snprintf(out, len,
                 "%sclass %s {\n%s\tpublic static void main(String[] %s) {\n%s\t%s%s\t}\n%s}",
                 ind, cls, ind, arg, ind, expr, ind, ind);
        free(ind);
        free(expr);
        $$ = out;
    }
  ;

TypeDeclList
  : TypeDecl TypeDeclList{
    char* typedec = safe_dup($1);
    char* typedecl = safe_dup($2);

    size_t len = strlen(typedec) + strlen(typedecl) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "%s\n%s", typedec, typedecl);


    free(typedec); free(typedecl);

    $$ = act;
  }
  | /* empty */{ $$ = NULL; }
  ;

TypeDecl
  : CLASS IDENTIFIER LBRACE {global_ind++; } VarDeclList MethodDeclList {global_ind--; } RBRACE {
    char* id = safe_dup($2);

    char* varlist = safe_dup($5);

    char* methodlist = safe_dup($6);

    size_t len = strlen(varlist) + strlen(methodlist) + strlen(id) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "class %s{\n%s\n%s\n}\n", id, varlist, methodlist);

    free(id); free(varlist); free(methodlist);

    $$ = act;
  }
  | CLASS IDENTIFIER EXTENDS IDENTIFIER LBRACE {global_ind++; } VarDeclList MethodDeclList {global_ind--; } RBRACE { 
    char* id1 = safe_dup($2);
    char* id2 = safe_dup($4);

    char* varlist = safe_dup($7);

    char* methodlist = safe_dup($8);

    size_t len = strlen(varlist) + strlen(methodlist) + strlen(id1) + strlen(id2) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "class %s extends %s{\n%s\n%s\n}\n", id1, id2, varlist, methodlist);

    free(id1); free(id2); free(varlist); free(methodlist);

    $$ = act;
  }
  ;

VarDeclList
  : VarDecl VarDeclList {
    char* var = safe_dup($1);

    char* varlist = safe_dup($2);

    size_t len = strlen(var) + strlen(varlist) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "%s%s", var, varlist);

    free(varlist); free(var);

    $$ = act;
  }
  | /* empty */{ $$ = NULL; }
  ;

VarDecl
  : Type IDENTIFIER SCN {
    char* ind = indent();

    char* type = safe_dup($1);

    char* id = safe_dup($2);

    size_t len = strlen(type) + strlen(id) + strlen(ind) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "%s%s %s;\n", ind, type, id);

    free(type); free(id); free(ind);

    $$ = act;
  }
  ;

MethodDeclList
  : MethodDecl MethodDeclList{
    char* method = safe_dup($1);

    char* mlist = safe_dup($2);

    size_t len = strlen(method) + strlen(mlist) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "%s%s", method, mlist);

    free(method); free(mlist);

    $$ = act;
  }
  | /* empty */{ $$ = NULL; }
  ;

MethodDecl
  : PUBLIC Type IDENTIFIER
    LPAREN FormalListOpt RPAREN
    LBRACE {global_ind++;} VarDeclList StatementList RETURN Expression SCN {global_ind--;} RBRACE {
        char* ind = indent();

        char* type = safe_dup($2);

        char* id = safe_dup($3);

        char* form = safe_dup($5);

        char* varlist = safe_dup($9);

        char* statlist = safe_dup($10);

        char* return_exp = safe_dup($12);

        size_t len = strlen(ind) + strlen(type) + strlen(id) + strlen(form) + strlen(varlist) + strlen(statlist) + strlen(return_exp) + 100;

        char* act = (char*)malloc(len);

        snprintf(act, len,
                "%spublic %s %s(%s) {\n%s\n%s%s\treturn %s;\n%s}\n", ind, type, id, form, varlist, statlist, ind, return_exp, ind);

        free(ind); free(type); free(id); free(form); free(varlist); free(statlist); free(return_exp);

        $$ = act;
    }
  ;

FormalListOpt
  : FormalList { $$ = safe_dup($1); }
  | /* empty */{ $$ = NULL; }
  ;

FormalList
  : Type IDENTIFIER {
    char* type = safe_dup($1);

    char* id = safe_dup($2);

    size_t len = strlen(type) + strlen(id) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "%s %s", type, id);

    free(type); free(id);

    $$ = act;
  }
  | Type IDENTIFIER COMMA FormalList {
    char* type = safe_dup($1);

    char* id = safe_dup($2);

    char* flist = safe_dup ($4);

    size_t len = strlen(type) + strlen(id) + strlen(flist) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "%s %s, %s", type, id, flist);

    free(type); free(id); free(flist);

    $$ = act;
  }
  ;

/* ---------------- Statements ---------------- */

StatementList
  : Statement StatementList {
    char* st = safe_dup($1);
    char* s2 = safe_dup($2);

    size_t len = strlen(st) + strlen(s2) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "%s%s", st, s2);

    free(st); free(s2);

    $$ = act;
  }
  | /* empty */{ $$ = NULL; }
  ;

Statement
  : MatchedStmt { $$ = safe_dup($1); }
  | UnmatchedStmt { $$ = safe_dup($1); }
  ;

MatchedStmt
  : Block { $$ = safe_dup($1); }
  | PrintStmt { $$ = safe_dup($1); }
  | AssignmentStmt { $$ = safe_dup($1); }
  | WhileStmt { $$ = safe_dup($1); }
  | MacroInvocationStmt { $$ = safe_dup($1); }
  | IfThenElseStmt { $$ = safe_dup($1); }
  ;

UnmatchedStmt
  : IF LPAREN Expression RPAREN {global_ind++;} Statement {global_ind--;} {
    char* ind = indent();

    char* exp = safe_dup($3);

    char* matched1 = safe_dup($6);

    size_t len = strlen(ind) + strlen(exp) + strlen(matched1) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "%sif(%s)\n%s", ind, exp, matched1);

    free(ind); free(exp); free(matched1);

    $$ = act;
  }
  | IF LPAREN Expression RPAREN {global_ind++;} MatchedStmt {global_ind--;} ELSE {global_ind++;} UnmatchedStmt {global_ind--;} {
    char* ind = indent();

    char* exp = safe_dup($3);

    char* matched1 = safe_dup($6);

    char* matched2 = safe_dup($10);

    size_t len = 2 * strlen(ind) + strlen(exp) + strlen(matched1) + strlen(matched2) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "%sif(%s)\n%s%selse\n%s", ind, exp, matched1, ind, matched2);

    free(ind); free(exp); free(matched1); free(matched2);

    $$ = act;
  }
  ;

Block
  : LBRACE {global_ind++;} StatementList {global_ind--;} RBRACE {
    char* ind = indent();

    char* statlist = safe_dup($3);

    size_t len = strlen(ind) + strlen(ind) + strlen(statlist) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "%s{\n%s%s}\n", ind, statlist, ind);

    free(ind); free(statlist);

    $$ = act;
  }
  ;

PrintStmt
  : SYSTEM DOT OUT DOT PRINTLN LPAREN Expression RPAREN SCN {
    char* ind = indent();
    char* expr = safe_dup($7);
    size_t len = strlen(ind) + 200 + strlen(expr);
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%sSystem.out.println(%s);\n", ind, expr);

    free(ind);
    free(expr);

    $$ = act;
  }
  ;

AssignmentStmt
  : PostfixExpr ASSIGN Expression SCN {
    char* ind = indent();

    char* post = safe_dup($1);

    char* exp = safe_dup($3);

    size_t len = strlen(ind) + strlen(post) + strlen(exp) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "%s%s = %s;\n", ind, post, exp);

    free(ind); free(exp); free(post);

    $$ = act;
  }
  ;


IfThenElseStmt
  : IF LPAREN Expression RPAREN {global_ind++; } MatchedStmt {global_ind--;} ELSE {global_ind++;} MatchedStmt {global_ind--;} {
    char* ind = indent();

    char* exp = safe_dup($3);

    char* matched1 = safe_dup($6);

    char* matched2 = safe_dup($10);

    size_t len = 2 * strlen(ind) + strlen(exp) + strlen(matched1) + strlen(matched2) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "%sif(%s)\n%s\n%selse\n%s\n", ind, exp, matched1, ind, matched2);

    free(ind); free(exp); free(matched1); free(matched2);

    $$ = act;
  }
  ;

WhileStmt
  : WHILE LPAREN Expression RPAREN {global_ind++; } MatchedStmt {global_ind--;} {
    char* ind = indent();

    char* exp = safe_dup($3);

    char* matched = safe_dup($6);

    size_t len = 2 * strlen(ind) + strlen(exp) + strlen(matched) + 100;

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "%swhile(%s)\n%s\n", ind, exp, matched);

    free(ind); free(exp); free(matched);

    $$ = act;
  }
  ;

MacroInvocationStmt
  : MacroInvocationSt SCN {

    char* ind = indent();

    char* method = safe_dup($1);

    size_t len = strlen(method) + strlen(ind) + 5;

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "%s%s", ind, method);

    free(ind); free(method);

    $$ = act;
  }
  ;

/* ---------------- Expressions ---------------- */

Expression
  : OrExpr { $$ = strdup($1); }
  | LambdaExpr { $$ = strdup($1); }
  ;

OrExpr
  : AndExpr {$$ = strdup($1);}
  | OrExpr OR AndExpr{
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    size_t len = strlen(left) + strlen(right) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s || %s", left, right);

    free(left); free(right);

    $$ = act;
  }
  ;

AndExpr
  : EqExpr {$$ = strdup($1);}
  | AndExpr AND EqExpr {
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    size_t len = strlen(left) + strlen(right) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s && %s", left, right);

    free(left); free(right);

    $$ = act;
  }
  ;

EqExpr
  : RelExpr {$$ = strdup($1);}
  | EqExpr NEQ RelExpr {
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    size_t len = strlen(left) + strlen(right) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s != %s", left, right);

    free(left); free(right);

    $$ = act;
  }
  ;

RelExpr
  : AddExpr {$$ = strdup($1);}
  | RelExpr LEQ AddExpr {
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    size_t len = strlen(left) + strlen(right) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s <= %s", left, right);

    free(left); free(right);

    $$ = act;
  }
  ;

AddExpr
  : MulExpr {$$ = strdup($1);}
  | AddExpr ADD MulExpr {
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    char* op = safe_dup($2);
    size_t len = strlen(left) + strlen(right) + strlen(op) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s %s %s", left, op, right);

    free(left); free(right); free(op);

    $$ = act;
  }
  ;

MulExpr
  : UnaryExpr {$$ = strdup($1);}
  | MulExpr MUL UnaryExpr {
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    char* op = safe_dup($2);
    size_t len = strlen(left) + strlen(right) + strlen(op) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s %s %s", left, op, right);

    free(left); free(right); free(op);

    $$ = act;
  }
  ;

UnaryExpr
  : NOT UnaryExpr{
    char* right = safe_dup($2);
    size_t len = strlen(right) + 5;
    char* act = (char*)malloc(len);
    snprintf(act, len,
            "! %s", right);

    free(right);

    $$ = act;
  }
  | PostfixExpr {$$ = $1;}
  ;
  
PostfixExpr
  : Primary { $$ = strdup($1); }
  | PostfixExpr LBRACK Expression RBRACK{
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    size_t len = strlen(left) + strlen(right) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s [%s]", left, right);

    free(left); free(right);

    $$ = act;
  }
  | PostfixExpr DOT IDENTIFIER{
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    size_t len = strlen(left) + strlen(right) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s.%s", left, right);

    free(left); free(right);

    $$ = act;
  }
  | PostfixExpr DOT IDENTIFIER LPAREN ArgListOpt RPAREN{
    char* left = safe_dup($1);
    char* right = safe_dup($3);
    char* op = safe_dup($5);
    size_t len = strlen(left) + strlen(right) + strlen(op) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s.%s(%s)", left, right, op);

    free(left); free(right);
    free(op);

    $$ = act;
  }
  ;


ArgListOpt
  : ArgList { $$ = strdup($1); }
  | { $$ = NULL; }
  ;

ArgList
  : Expression { $$ = $1; }
  | Expression COMMA ArgList{
    char* left = $1 ? $1 : strdup("");
    char* right = $3 ? $3 : strdup("");
    size_t len = strlen(left) + strlen(right) + 100;
    char* act = (char*)malloc(len);
    snprintf(act, len, 
            "%s , %s", left, right);

    free(left); free(right);

    $$ = act;
  }
  ;

Primary
  : INTEGER_LITERAL { $$ = strdup($1); }
  | TRUEVAL { $$ = strdup("true"); }
  | FALSEVAL { $$ = strdup("false"); }
  | IDENTIFIER { $$ = strdup($1); }
  | THIS { $$ = strdup("this"); }
  | NEW INT LBRACK Expression RBRACK {
    char* expr = $4 ? $4 : strdup("");
    size_t len = 100 + strlen(expr);

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "new int[%s]", expr);

    free(expr);

    $$ = act;

  }
  | NEW IDENTIFIER LPAREN RPAREN {

    char* id = safe_dup($2);

    size_t len = 100 + strlen(id);

    char* act = (char*)malloc(len);

    snprintf(act, len,
            "new %s()", id);

    free(id);

    $$ = act;


  }
  | LPAREN Expression RPAREN {
    char* expr = safe_dup($2);

    size_t len = 100 + strlen(expr);

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "(%s)", expr);

    free(expr);

    $$ = act;

  }

  | MacroInvocationExp { $$ = strdup($1); }
  ;

LambdaExpr
  : LPAREN IDENTIFIER RPAREN ARROW Expression {
    char* id = safe_dup($2);
    char* expr = safe_dup($5);

    size_t len = 100 + strlen(id) + strlen(expr);

    char *act = (char*)malloc(len);

    snprintf(act, len, 
            "((%s) -> %s)", id, expr);

    free(id); free(expr);

    $$ = act;
  }
  ;

MacroInvocationSt
  : IDENTIFIER LPAREN ArgListOpt RPAREN {
    string name($1);  // $1 is char*, wrap in string

    auto it = st_macro_table.find(name);
    if (it == st_macro_table.end()) {
        yyerror("Incorrect macro type");
    } else {
        Macro* m = it->second;

        try {
            string args = ($3 ? string($3) : "");
            string res = m->expand(args.c_str());
            $$ = strdup(res.c_str());
        } catch (const runtime_error &e) {
            yyerror(e.what());
            YYERROR;
        }
    }
  }
  ;

MacroInvocationExp
  : IDENTIFIER LPAREN ArgListOpt RPAREN {
    string name($1);  // $1 is char*, wrap in string

    auto it = exp_macro_table.find(name);
    if (it == exp_macro_table.end()) {
        yyerror("Incorrect macro type");
    } else {
        Macro* m = it->second;

        try {
            string args = ($3 ? string($3) : "");
            string res = m->expand(args.c_str());
            $$ = strdup(res.c_str());
        } catch (const runtime_error &e) {
            yyerror(e.what());
            YYERROR;
        }
    }
  }
  ;

Type
  : INT LBRACK RBRACK {
    $$ = strdup("int[]");
  }
  | BOOLEAN { $$ = strdup("boolean"); }
  | INT { $$ = strdup("int"); }
  | IDENTIFIER { $$ = strdup($1); }
  | FUNCTION LT IDENTIFIER COMMA IDENTIFIER GT {
    char* left = safe_dup($3);

    char* right = safe_dup($5);

    size_t len = 100 + strlen(left) + strlen(right);

    char* act = (char*)malloc(len);

    snprintf(act, len, 
            "Function <%s, %s>", left, right);

    free(left); free(right);

    $$ = act;
  }
  ;

%%

int main(void) {
    return yyparse();
}

void yyerror(const char *s) {
    printf("// Failed to parse macrojava code.\n");
    exit(1);
}