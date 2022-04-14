EVALANGUAGEDEFINITION = """
start: evaluate+

evaluate: "evaluate" selectexpression "using" templatename [("," strictness )]		-> evaluatething


selectexpression : "name" "=" ELEMENTNAME 								-> selectexpressionname
              	 | "type" ":" typelist							-> selectexpressiontype
              	 | "eid"  "=" UUIDNAME								-> selectexpressionelementid


templatename : NAME 											-> templatenamething				

strictness : "logic" "=" SVALUE  -> selectstrictness
 
SVALUE :  "strict" | "flexible" | "loose" 

typelist:  "[" [NAME ("," NAME)*] "]"   -> selecttypelist    


ELEMENTNAME : (LETTER+|DIGIT+|"-"|"_")+
UUIDNAME : (LETTER+|DIGIT+|"-")+


%import common.CNAME -> NAME
%import common.LETTER
%import common.WORD
%import common.NUMBER
%import common.DIGIT
%import common.STRING
%import common.WS_INLINE
%import common.WS
%import common.ESCAPED_STRING   -> STRING
%ignore WS_INLINE
%ignore WS
"""

ATTLANGUAGEDEFINITION = """
start: template+ 


value: dict
         | list
         | STRING
         | NUMBER
         | "true" | "false" | "null"

list : "[" [value ("," value)*] "]"
dict : "{" [pair ("," pair)*] "}"
pair : STRING ":" value

tlist : "[" [NAME ("," NAME)*] "]"


template:  "template" templatename attestsection decisionsection?        -> templatething
templatename: NAME                                                      -> templatenamething

attestsection: "attest" policy+       
policy: policyname "," callparams ["," paramfunction ]+ rulesection?                          -> policything
policyname: POLICYNAME                                                        -> policynamething  
paramfunction: NAME   -> paramfunctionnamething
callparams: dict                                                        -> policycpthing

rulesection: "[[" rule+ "]]"             
rule: rulevariablemame "<-" rulename "," ruleparams                     -> rulething
rulevariablemame : RULEVARIABLENAME                                                 -> rulevariablenamething  
rulename : RULENAME                                                         -> rulenamething  
ruleparams: dict                                                        -> rulerpsthing

decisionsection: "decision" logicexpr                           -> decisionthing

?logicexpr:  logicexpr "^" logicexpr -> and_
          |  logicexpr "v" logicexpr -> or_
          |  logicexpr "=>" logicexpr -> decimpl
          |  logicexpr "<=>" logicexpr -> decequiv         
          |  "!" logicexpr -> not_
          |  "(" logicexpr ")"
          |   RULEVARIABLENAME  -> decvariable


RULEVARIABLENAME : (LETTER+|DIGIT+|"-"|"_"|"/")+

POLICYNAME : (LETTER+|DIGIT+|"-"|"_"|"/")+
ELEMENTNAME : (LETTER+|DIGIT+|"-"|"_"|"/")+
RULENAME : (LETTER+|DIGIT+|"-"|"_"|"/")+
UUIDNAME : (LETTER+|DIGIT+|"-")+

%import common.CNAME -> NAME
%import common.LETTER
%import common.WORD
%import common.NUMBER
%import common.DIGIT
%import common.STRING
%import common.WS_INLINE
%import common.WS
%import common.ESCAPED_STRING   -> STRING
%ignore WS_INLINE
%ignore WS
"""
