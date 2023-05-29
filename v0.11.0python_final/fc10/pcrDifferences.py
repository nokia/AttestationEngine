import analysisFunction
from dictdiffer import diff
import uuid
import datetime

from forensicsStructures import *

class PCRDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"PCRDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry


class PCRDifference(analysisFunction.AnalysisFunction):
    NAME="PCRDifference"

    def __init__(self):
        super().__init__()

    def filterpcrs(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["pcrs"]
            return True
        except:
            return False
        

    def apply(self,timeline,element,errors):
        f=0

        pcrclaimslist =  list(filter( self.filterpcrs, timeline ))

        if len(pcrclaimslist) == 0:
            return  AnalysisReturn( self.NAME, pcrclaimslist, 0 )      

        currentclaim = pcrclaimslist[0]
        for c in pcrclaimslist:
            cl1 = currentclaim["claim"]["payload"]["payload"]["pcrs"]
            cl2 = c["claim"]["payload"]["payload"]["pcrs"]
            difference = list(diff(cl1,cl2))

            if difference != []:
                f=f+1
                e = PCRDifferenceEntry(c,currentclaim,difference)
                timeline.append(e.get())
                currentclaim = c


        return  AnalysisReturn( self.NAME, pcrclaimslist, f )               

