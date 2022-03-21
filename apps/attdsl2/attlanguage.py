from lark import Lark
from lark.visitors import Interpreter, Visitor, Transformer

import requests

#
# Template Processing
#


class TemplateProcessor():
  def __init__(self):
    self.name = None
    self.attestPolicies = []
    self.decisionexpression = None

  def setName(self,n):
    self.name=n
  def getName(self):
    return self.name
  def addAttestPolicy(self,p):
    self.attestPolicies.append(p)
  def setDecisionExpression(self,d):
    self.decisionexpression = d


class PolicyProcessor():
  def __init__(self):
    self.name = "None"
    self.CP = "None"
    self.attestRules = []
    self.paramFunction = None

  def setName(self,n):
    self.name=n
  def setCP(self,cp):
    self.CP=cp
  def addAttestRules(self,r):
    self.attestRules.append(r)
  def setParamFunction(self,f):
    self.paramFunction = f

class RuleProcessor():
   def __init__(self):
    self.variablename = "None"
    self.rulename = "None"
    self.RP = "None"

   def setName(self,n):
    self.name=n
   def setRP(self,cp):
    self.RP=cp 
   def setVariableName(self,n):
    self.variablename=n
   def setRuleName(self,n):
    self.rulename=n

class InterpretTemplate(Interpreter):
  def __init__(self):
    self.template = None         # used for temporary storage during interpreting
    self.policy = None         # used for temporary storage during interpreting
    self.rules = None          # used for temporary storage during interpreting   

    #These will get returned at the end
    self.templates = {}

  def templatething(self,tree):
    self.template=TemplateProcessor()
    self.visit_children(tree)
    self.templates[ self.template.getName() ] = self.template

  def templatenamething(self,tree):
    self.template.setName(tree.children[0].value)

  def policything(self,tree):
    #len = 2 if there is nothing underneath, ie: no rules
    self.policy = PolicyProcessor()
    self.visit_children(tree)
    self.template.addAttestPolicy(self.policy)

  def policynamething(self,tree):
    self.policy.setName(tree.children[0].value)

  def paramfunctionnamething(self,tree):
    self.policy.setParamFunction(tree.children[0].value)

  def policycpthing(self,tree):
    self.policy.setCP(tree.children[0])


  def rulething(self,tree):
    self.rules = RuleProcessor()
    self.visit_children(tree)
    self.policy.addAttestRules(self.rules)

  def rulerpsthing(self,tree):
    self.rules.setRP(tree.children[0])

  def rulevariablenamething(self,tree):
    self.rules.setVariableName(tree.children[0].value)

  def rulenamething(self,tree):
    self.rules.setRuleName(tree.children[0].value)
    self.visit_children(tree)

  def decisionthing(self,tree):
    self.template.setDecisionExpression(tree)

#
# EVA Interpreter
#

class EvaluationProcessor():
  def __init__(self,ep):
    self.templatename=None
    self.a10restEndpoint=ep

  def getTemplateName(self):
    return self.templatename
  def setTemplateName(self,n):
    self.templatename=n

class EvaluationProcessorByName(EvaluationProcessor):
  def __init__(self,n,ep):
    super().__init__(ep)
    self.elementname=n

  def resolveElements(self):
    u = self.a10restEndpoint+"/element/name/"+self.elementname
    r = requests.get(u)
    if r.status_code == 200:
      return [ r.json()["itemid"] ]
    else:
      return None

class EvaluationProcessorByTypes(EvaluationProcessor):
  def __init__(self,ts,ep):
    super().__init__(ep)
    self.types=ts

  def resolveElements(self):
    return self.types



class EvaluationProcessorByEID(EvaluationProcessor):
  def __init__(self,e,ep):
    super().__init__(ep)
    self.elementid=e

  def resolveElements(self):
    return [ self.elementid ]



class InterpretEvalation(Interpreter):
  def __init__(self,ep):
    self.eplist = []
    self.a10restEndpoint = ep

  def evaluatething(self,tree):
    self.ep=EvaluationProcessor(self.a10restEndpoint)
    self.visit_children(tree)
    self.eplist.append(self.ep)

  def selectexpressionname(self,tree):
    self.ep=EvaluationProcessorByName(tree.children[0].value,self.a10restEndpoint)

  def selectexpressiontype(self,tree):
    self.ep=EvaluationProcessorByTypes(["a","list","of","things"],self.a10restEndpoint)
    #for this to work we need to resolve the list to a python list
    #self.ep=EvaluationProcessorByTypes(tree.children[0].value)

  def selectexpressionelementid(self,tree):
    self.ep=EvaluationProcessorByEID(tree.children[0].value,self.a10restEndpoint)

  def templatenamething(self,tree):
    self.ep.setTemplateName(tree.children[0].value)



#
# Decision Transformer
#

class CalculateDecision(Transformer):
  def __init__(self,t,v):
     self.variables = v
     self.tree = t

  def result(self):
    r = self.transform(self.tree)
    print("\nResult = ",r.children[0].children[0],"\n")
    return r.children[0]

  def decand(self,tree):
    #print("decAnd ",type(tree[0].children[0]), tree[1].children[0] )
    lhs = tree[0].children[0]
    rhs = tree[1].children[0]
    return lhs & rhs

  def decor(self,tree):
    lhs = tree[0].children[0]
    rhs = tree[1].children[0]
    return lhs | rhs 
  
  def decneg(self,tree):
    val = tree[0].children[0]
    return not(val)

  def decvariable(self,tree):
    print("decVariable ",tree[0].value)
    value = self.variables[tree[0].value]
    if value == 0:
      return True 
    else:  
      return False
#
# Atteststion Executor
#


class AttestationExecutor():
    def __init__(self,templatefile,evalfile,restendpoint):
      self.a10restEndpoint = restendpoint+"/v2"
      print("AE Initiated: ",self.a10restEndpoint)

      attlanguage=None
      self.attinstructions=None

      evalanguage=None
      evainstructions=None

      with open('att_language.lark','r') as f:
        attlanguage = Lark(f.read())
      with open(templatefile,'r') as f:
        attinstructions = attlanguage.parse(f.read())

      with open('eva_language.lark','r') as f:
        evalanguage = Lark(f.read())
      with open(evalfile,'r') as f:
        evainstructions = evalanguage.parse(f.read())

      self.att = InterpretTemplate()
      self.att.visit(attinstructions)  
      self.eva = InterpretEvalation(restendpoint)
      self.eva.visit(evainstructions)

    def prettyprint(self):
      print("Interpreting Template")

      for x in self.att.templates.keys():
        u = self.att.templates[x]
        print(x)
        print(" -> ",u.decisionexpression)  
        for p in u.attestPolicies:
          print(" +- ",p.name, "f:",p.paramFunction)
          for r in p.attestRules:
            print("    +- ",r.rulename,r.variablename)
      
      print("Interpreting EvaluationProcessor\n EPLIST ",self.eva.eplist)            

    def calculateDecision(self,t,v):
       # t is the decision tree and v is the dictionary of variables
       print("Calculating")
       c = CalculateDecision(t,v).result()
       print("Calculated Result is ",c)

       return c

    def resolveSSHProtocolCPS(self,r):
      """ 
      r is the item ID
      returns None if not using A10SSL protocol, otherwise a dictionary containing the required information
      """
      # get element
      er = requests.get(self.a10restEndpoint+"/element/"+r)
      # I should check here if it exists, but at this point if it doesn't we're in trouble anyway

      element = er.json()

      if element["protocol"]=="A10TPMSENDSSL":
        return element["a10_tpm_send_ssl"]
      else:
        return None

    def applyCopyCredentials(self,r):
      """ 
      r is the item ID
      returns None if not using A10SSL protocol, otherwise a dictionary containing the required information
      """
      # get element
      er = requests.get(self.a10restEndpoint+"/element/"+r)
      # I should check here if it exists, but at this point if it doesn't we're in trouble anyway

      element = er.json()

      #Now get the values for the credentials
      # dict if success, None otherwise

      ret={}
      try:
        ret["akname"]= element["tpm2"]["tpm0"]["akname"]
        ret["ekpub"]= element["tpm2"]["tpm0"]["ekpem"]
        return ret
      except:
        return None


    def execute(self,progress=0):
      # Iterate over the EvaluationProcessors
      #
      # Good luck, this is a mess
      #
      session = requests.post(self.a10restEndpoint+"/sessions/open")
      if session.status_code != 201:
        print("Failed to open a new session")
        return 1
      sessionOuter = session.json()["itemid"]

      counter=1
      elementlen=len(self.eva.eplist)

      for e in self.eva.eplist:
        if progress>0:
          print("Processing",counter,"of",elementlen)
        counter=counter+1

        rs = e.resolveElements()
        for r in rs:
          if progress>0:
            print("   Element",r,"of length",len(rs))          
          # This is used to store the variables for each element being processed
          variables={}


          template = self.att.templates[e.getTemplateName()]
          # Now we have the element IDs, we iterate across the policies to get the claims
          # Setup the inner session
          session = requests.post(self.a10restEndpoint+"/sessions/open") 
          if session.status_code != 201:
            print("Failed to open a new inner session")
            return 1
          sessionInner = session.json()["itemid"]     
          #Associate inner session with outer session

          session = requests.post(self.a10restEndpoint+"/session/"+sessionOuter+"/subsession/"+sessionInner)
          if session.status_code != 200:
            print("Failed to associate with outer session")
            return 1


          for ap in template.attestPolicies:
            if progress>1:
              print("   +--- ",ap)
            #for each of these we generate claims
            # get the policy ID
            pr = requests.get(self.a10restEndpoint+"/policy/name/"+ap.name)
            if pr.status_code == 200:
              
              # now make a claim
              eid = r
              pid = pr.json()["itemid"]

              cps = {}
              # Do all necessary preprocessing for the CPS structure here


              sshans = self.resolveSSHProtocolCPS(eid)
              if sshans != None:
                cps['a10_tpm_send_ssl']= sshans
              
              # Check if a policy param function is set
              #print("POLICY PARAM F is ",ap.paramFunction)

              #There's a much easier way of doing this I am sure, but for the moment
              #with a single convenience function it's fine

              if ap.paramFunction=="copycredentials":
                  pfr = self.applyCopyCredentials(eid)
                  if r!=None:
                    #print("   +-- applying copycrednentials with ",pfr)
                    cps["akname"]=pfr["akname"]
                    cps["ekpub"]=pfr["ekpub"]
                  else:
                    #print("   +-- Nothing to do")
                    pass
              # And continue 

              req = { "eid":eid, "pid":pid, "cps":cps, "sid":sessionInner }
              cl = requests.post(self.a10restEndpoint+"/attest", json=req)
              claimid = cl.json()["claim"]
              sesa = requests.post(self.a10restEndpoint+"/session/"+sessionInner+"/claim/"+claimid)

              # now iterate over any associated rules
              for ru in ap.attestRules:
                if progress>2:
                  print("   +------ ",ru)
                cid = cl.json()["claim"]
                rule = [ ru.rulename, {} ]

                req = { "cid":cid, "rule":rule, "sid":sessionInner }

                vr = requests.post(self.a10restEndpoint+"/verify", json=req)
                resultid = vr.json()["result"]
                sesr = requests.post(self.a10restEndpoint+"/session/"+sessionInner+"/result/"+resultid)

                #OK, now we have the result id we can store the result code in the associated variable

                vrr = requests.get(self.a10restEndpoint+"/result/"+resultid)
                resultvalue = vrr.json()["result"]["result"]
                print("result value is ",resultvalue)
                print("rule processor variable name is ",ru.variablename," <-- ",resultvalue )
                variables[ru.variablename]=resultvalue

            else:
              print("Unknown Policy")

          # here we call the decision function
          print("variables for this element are ",variables)
          #print("decision tree is", template.decisionexpression )

          d = self.calculateDecision(template.decisionexpression,variables)
          print("Final Decision is ",d)
          #self.makeDecision(variables)

          session = requests.delete(self.a10restEndpoint+"/session/"+sessionInner)
          if session.status_code != 200:
            print("Failed to close session "+sessionOuter)
            return 1


      session = requests.delete(self.a10restEndpoint+"/session/"+sessionOuter)
      if session.status_code != 200:
        print("Failed to close session "+sessionOuter)
        return 1
      return 0


