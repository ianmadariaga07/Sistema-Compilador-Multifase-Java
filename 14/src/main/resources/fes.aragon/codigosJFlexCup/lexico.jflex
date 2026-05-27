package fes.aragon.lexico;
import java_cup.runtime.Symbol;
import fes.aragon.sintactico.sym;

%%

%class Lexico
%public
%unicode
%cup
%line
%column

WhiteSpace = [ \t\r\n]+
Letra = [a-zA-Z_]
Digito = [0-9]
Id = {Letra}({Letra}|{Digito})*
Numero = {Digito}+

%%

"inicio"    { return new Symbol(sym.INICIO, yyline + 1, yycolumn + 1, yytext()); }
"derecha"   { return new Symbol(sym.DERECHA, yyline + 1, yycolumn + 1, yytext()); }
"izquierda" { return new Symbol(sym.IZQUIERDA, yyline + 1, yycolumn + 1, yytext()); }
"arriba"    { return new Symbol(sym.ARRIBA, yyline + 1, yycolumn + 1, yytext()); }
"abajo"     { return new Symbol(sym.ABAJO, yyline + 1, yycolumn + 1, yytext()); }
"mover"     { return new Symbol(sym.MOVER, yyline + 1, yycolumn + 1, yytext()); }
"repite"    { return new Symbol(sym.REPITE, yyline + 1, yycolumn + 1, yytext()); }
"hasta"     { return new Symbol(sym.HASTA, yyline + 1, yycolumn + 1, yytext()); }
"fin"       { return new Symbol(sym.FIN, yyline + 1, yycolumn + 1, yytext()); }

"="         { return new Symbol(sym.ASIGNACION, yyline + 1, yycolumn + 1, yytext()); }

{Numero}    { return new Symbol(sym.NUMERO, yyline + 1, yycolumn + 1, yytext()); }
{Id}        { return new Symbol(sym.ID, yyline + 1, yycolumn + 1, yytext()); }
{WhiteSpace} { }

[^]         { System.out.println("Error lexico: " + yytext() + " en linea " + (yyline+1) + " columna " + (yycolumn+1)); }