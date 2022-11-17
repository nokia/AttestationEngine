import analysisFunction
from dictdiffer import diff
import uuid
import datetime

from forensicsStructures import *


class ElementDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"ElementDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry


class ElementDifference(analysisFunction.AnalysisFunction):
    NAME="ElementDifference"

    def __init__(self):
        super().__init__()

    def filterclaims(self,x):
        try:
            y = x["claim"]["header"]["element"]
            return True
        except:
            return False
        

    def apply(self,timeline,element,errors):
        f=0

        claimslist =  list(filter( self.filterclaims, timeline ))

        if len(claimslist) == 0:
            return  AnalysisReturn( self.NAME, claimslist, {} ) 

        #start from the element as it was then the forensics document was requested
        #but make it look like a claim
        currentclaim = {"claim": {"header": {"element":element}, "itemid":"0"}}

        for c in claimslist:
            cl1 = currentclaim["claim"]["header"]["element"]
            cl2 = c["claim"]["header"]["element"]
            difference = list(diff(cl1,cl2))

            if difference != []:
                f=f+1
                e = ElementDifferenceEntry(currentclaim,c,difference)
                timeline.append(e.get())
                currentclaim = c


        return  AnalysisReturn( self.NAME, claimslist, f )           

