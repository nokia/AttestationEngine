import uuid
import datetime

class Report():
    def __init__(self):
        self.data={
            "reportID" : str(uuid.uuid4()),
            "created" : self.now(),
            "instructions" : {},
            "errors" : [],
            "ecrv" : [],
            "decisions" : []
        }

    def open(self):
        self.data["opened"]= self.now()

    def close(self):
        self.data["closed"]= self.now()

    def now(self):
        return str(datetime.datetime.now(datetime.timezone.utc))

    def addEndPoint(self,e):
        self.data["A10endpoint"] = e

    def addatt(self,a):
        self.data["instructions"]["att"] = a

    def addeva(self,e):
        self.data["instructions"]["eva"] = e

    def adderr(self,e):
        self.data["errors"].append(
              {"time": self.now(), "err":e}
              )

    def addECRV(self,e,c,r,v):
        self.data["ecrv"].append(
              {"e":e,
                "c":c,
                "r":r,
                "v":v
              }
              )

    def addDecision(self,d,e,t):
        self.data["decisions"].append(
              {"eid":e,
                "result":d,
                "template":t
              }
              )    


    def getReport(self):
        return self.data
