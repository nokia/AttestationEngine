import uuid
import datetime
import requests

class ForensicsException(Exception):
   """forensics exception base class"""
   

# Covenience function for datetime
def futc(t):
    # formats a timestamp to a UTC date:
    return datetime.datetime.utcfromtimestamp(float(t)).strftime("%Y-%m-%d_%H:%M:%S")


class ForensicsDocument:
    def __init__(self,asvr,eid,limit=250):
        print("--init: ",limit)

        self.asvr = asvr
        self.elementID = eid
        self.claimslimit = limit

        self.timeline = []
        self.element = {}
        self.analysisfunctions = []
        self.documentID = str(uuid.uuid4())
        self.errors = []
        self.metadata = { "eid": eid,
                          "asvr": asvr,
                          "documentID": self.documentID }

        self.getDataFromASVR()

    def sortedTimeline(self):
        print("--sortedTimeline")
        # Also compute the UTC for each 
        for i  in self.timeline:
            i["tl_tsUTC"]=futc(i["tl_ts"])

        return sorted(self.timeline, key=lambda e: e["tl_ts"], reverse= True )

    def getDocument(self):
        print("--getDocument")
        self.metadata["nerrors"]=len(self.errors)

        return { "type":"ForensicsDocument",
                 "element":self.element,
                 "analysisfunctions":self.analysisfunctions,
                 "metadata":self.metadata,
                 "errors":self.errors,
                 "timeline":self.sortedTimeline()
                 }

    def getMetadata(self):
        print("--getMetadata")
        self.metadata["nerrors"]=len(self.errors)

        return { "type":"ForensicsDocumentMetaData",
                 "analysisfunctions":self.analysisfunctions,        
                 "metadata":self.metadata                 
               }



    def getDataFromASVR(self):
        print("--getDataFromASVR")

        print("----request element")
        e = requests.get(self.asvr+"/element/"+self.elementID+"?limit="+str(self.claimslimit))

        # Basically we kill the processing if the element doen't exist  by throwing an exception
        print("----result ",str(e.status_code))        
        if e.status_code != 200:
            raise Exception("Element "+self.elementID+" does not exist on "+self.asvr+" with status code "+str(e.status_code))

        self.element = e.json()

        print("----request claims")
        cs = requests.get(self.asvr+"/claims/element/"+self.elementID).json()["claims"]
        
        print("----generating metadata")        
        self.metadata["nclaims"] = len(cs) 
        self.metadata["nclaimslimit"] = self.claimslimit

        if len(cs)  == 0:
           raise ForensicsException("No claims found for element "+self.elementID)

        # threading here !!
        i=0
        rcount = 0
        seensessions=[]
        sessionsopen=0
        sessionsclosed=0

        print("----collecting", self.metadata["nclaims"],"claims and associated results")        
        for x in cs:
            print("------ claim",i,"of",self.metadata["nclaims"],"      ",(i/self.metadata["nclaims"])*100,"% complete")
            i=i+1
            c = requests.get(self.asvr+"/claim/"+x).json()
            c["tl_ts"] = c["claim"]["header"]["as_received"]
            c["tl_type"]="claim"
            self.timeline.append(c)
            cid = c["claim"]["itemid"]
            try:
                cse = c["claim"]["header"]["session"]
                if cse=="":
                    self.errors.append({ "err":"Session present but missing in claim", "itemid":cid,  "function":"getDataFromASVR"} )
            except:
                self.errors.append({ "err":"Session missing", "itemid":cid, "session":cse, "function":"getDataFromASVR"} )


            print("------collecting associated results")
            cid = c["claim"]["itemid"]
            rs = requests.get(self.asvr+"/claim/associatedresults/"+x).json()
            print("-------- associated results =",len(rs))

            for ri in rs["results"]:
                r = requests.get(self.asvr+"/result/"+ri["itemid"]).json()
                r["tl_ts"] = r["result"]["verifiedAt"]
                r["tl_type"]="result"
                rcount = rcount + 1
                self.timeline.append(r)
        
            if cse!=None:
                print("----collecting associated sessions")                    
                sr = requests.get(self.asvr+"/session/"+cse)

                #this is here because sometimes a session might not exist, from the
                #testing database...not in production I hope
                if sr.status_code == 200:

                    s=sr.json()

                    if cse not in seensessions:
                        seensessions.append(cse)
                        #if the closed attribute exists then this is a closed session
                        try:
                            s["tl_ts"] = s["closed"]
                            s["tl_type"]="session/closed"
                            self.timeline.append(s)
                            o={ "tl_type":"session/closed/start",
                                "tl_ts":s["opened"],
                                "itemid":s["itemid"]
                                }
                            sessionsclosed=sessionsclosed+1
                            self.timeline.append(o)   
                        except:
                            s["tl_ts"] = s["opened"]
                            s["tl_type"]="session/open"  
                            sessionsopen=sessionsopen+1
                            self.timeline.append(s)

        print("----collecting log entries")
        logentries = requests.get(self.asvr+"/log/itemid/"+self.elementID).json()["logentries"]
        for le in logentries:
            le["tl_ts"] = le["t"]
            le["tl_type"] = "logentry"
            self.timeline.append(le)

        print("----counting timeline entries")                 
        self.metadata["nsessions"] = len(seensessions)   
        self.metadata["nsessionsopen"] = sessionsopen
        self.metadata["nsessionsclosed"] = sessionsclosed 
        self.metadata["nlogentries"] = len(logentries)
        self.metadata["nresults"] = rcount           
        self.metadata["ntimelineentries"] = len(self.timeline)            




    def analyse(self,f):
        print("--analysing ",f.NAME)                            
        r = f.apply(self.timeline, self.element, self.errors)
        print("----done",f.NAME)        
        self.analysisfunctions.append(r.get())
