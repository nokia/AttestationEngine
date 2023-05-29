import analysisFunction
from dictdiffer import diff
import uuid
import datetime

from forensicsStructures import *

class SystemInformationDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"SystemInformationDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry


class SystemInformationDifference(analysisFunction.AnalysisFunction):
    NAME="SystemInformationDifference"

    def __init__(self):
        super().__init__()

    def filterclaims(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["systeminfo"]
            return True
        except:
            return False
        

    def apply(self,timeline,element,errors):
        f=0

        claimslist =  list(filter( self.filterclaims, timeline ))

        if len(claimslist) == 0:
            return AnalysisReturn( self.NAME, claimslist, 0 )
            
        #start from the element as it was then the forensics document was requested
        #but make it look like a claim
        currentclaim = claimslist[0]

        for c in claimslist:
            cl1 = currentclaim["claim"]["payload"]["payload"]["systeminfo"]
            cl2 = c["claim"]["payload"]["payload"]["systeminfo"]
            difference = list(diff(cl1,cl2))

            if difference != []:
                f=f+1
                e = SystemInformationDifferenceEntry(currentclaim,c,difference)
                timeline.append(e.get())
                currentclaim = c


        return AnalysisReturn( self.NAME, claimslist, f )
                      





class ThinkLMIDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"ThinkLMIDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry


class ThinkLMIDifference(analysisFunction.AnalysisFunction):
    NAME="ThinkLMIDifference"

    def __init__(self):
        super().__init__()

    def filterclaims(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["thinklmi"]
            return True
        except:
            return False
        

    def apply(self,timeline,element,errors):
        f=0

        claimslist =  list(filter( self.filterclaims, timeline ))

        if len(claimslist) == 0:
            return AnalysisReturn( self.NAME, claimslist, 0 )
            
        #start from the element as it was then the forensics document was requested
        #but make it look like a claim
        currentclaim = claimslist[0]

        for c in claimslist:
            cl1 = currentclaim["claim"]["payload"]["payload"]["thinklmi"]
            cl2 = c["claim"]["payload"]["payload"]["thinklmi"]
            difference = list(diff(cl1,cl2))

            if difference != []:
                f=f+1
                e = ThinkLMIDifferenceEntry(currentclaim,c,difference)
                timeline.append(e.get())
                currentclaim = c


        return AnalysisReturn( self.NAME, claimslist, f )
                      