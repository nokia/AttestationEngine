import analysisFunction
from dictdiffer import diff
import uuid
import datetime
from functools import partial

from forensicsStructures import *


class QuoteResetDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"QuoteResetDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry




class QuoteResetDifference(analysisFunction.AnalysisFunction):
    NAME="QuoteResetDifference"

    def __init__(self):
        super().__init__()

    def filterquotes(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["quote"]
            return True
        except:
            return False



    def apply(self,timeline,element,errors):
        f=0
        quoteclaimslist =  list(filter( self.filterquotes, timeline ))
        
        if len(quoteclaimslist) == 0:
            return  AnalysisReturn( self.NAME, quoteclaimslist, f )      
        
        currentclaim = quoteclaimslist[0]

        for c in quoteclaimslist:
            try:
                cl1 = currentclaim["claim"]["payload"]["payload"]["quote"]["clockInfo"]["resetCount"]
                cl2 = c["claim"]["payload"]["payload"]["quote"]["clockInfo"]["resetCount"]
                if cl1 != cl2:
                    f=f+1
                    e = QuoteResetDifferenceEntry(c,currentclaim,{ "reset_old":cl2,"reset_new":cl1})
                    timeline.append(e.get())
                    currentclaim = c
            except Exception as ex:
                errors.append({ "err":str(ex), "itemid":currentclaim["itemid"], "2nd":cl2["itemid"], "function":self.NAME} )

        return AnalysisReturn( self.NAME, quoteclaimslist, f )













class QuoteRestartDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"QuoteRestartDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry




class QuoteRestartDifference(analysisFunction.AnalysisFunction):
    NAME="QuoteRestartDifference"

    def __init__(self):
        super().__init__()

    def filterquotes(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["quote"]
            return True
        except:
            return False

    def apply(self,timeline,element,errors):
        f=0
        quoteclaimslist =  list(filter( self.filterquotes, timeline ))
        
        if len(quoteclaimslist) == 0:
            return AnalysisReturn( self.NAME, quoteclaimslist, f )         
        
        currentclaim = quoteclaimslist[0]

        for c in quoteclaimslist:
            try:
                cl1 = currentclaim["claim"]["payload"]["payload"]["quote"]["clockInfo"]["restartCount"]
                cl2 = c["claim"]["payload"]["payload"]["quote"]["clockInfo"]["restartCount"]
                if cl1 != cl2:
                    f=f+1
                    e = QuoteRestartDifferenceEntry(c,currentclaim,{ "restart_old":cl2,"restart_new":cl1})
                    timeline.append(e.get())
                    currentclaim = c
            except Exception as ex:
                errors.append({ "err":str(ex), "itemid":currentclaim["itemid"], "2nd":cl2["itemid"], "function":self.NAME} )


        return AnalysisReturn( self.NAME, quoteclaimslist, f )













class QuoteSafeDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"QuoteSafeDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry




class QuoteSafeDifference(analysisFunction.AnalysisFunction):
    NAME="QuoteSafeDifference"

    def __init__(self):
        super().__init__()

    def filterquotes(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["quote"]
            return True
        except:
            return False

    def apply(self,timeline,element,errors):
        f=0
        quoteclaimslist =  list(filter( self.filterquotes, timeline ))
        
        if len(quoteclaimslist) == 0:
            return AnalysisReturn( self.NAME, quoteclaimslist, f )      
        
        currentclaim = quoteclaimslist[0]

        for c in quoteclaimslist:
            try:
                cl1 = currentclaim["claim"]["payload"]["payload"]["quote"]["clockInfo"]["safe"]
                cl2 = c["claim"]["payload"]["payload"]["quote"]["clockInfo"]["safe"]
                if cl1 != cl2:
                    f=f+1
                    e = QuoteResetDifferenceEntry(c,currentclaim,{ "safe_old":cl2,"safe_new":cl1})
                    timeline.append(e.get())
                    currentclaim = c
            except Exception as ex:
                errors.append({ "err":str(ex), "itemid":currentclaim["itemid"], "2nd":cl2["itemid"], "function":self.NAME} )

        return AnalysisReturn( self.NAME, quoteclaimslist, f )











class QuoteFirmwareDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"QuoteFirmwareDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry




class QuoteFirmwareDifference(analysisFunction.AnalysisFunction):
    NAME="QuoteFirmwareDifference"

    def __init__(self):
        super().__init__()

    def filterquotes(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["quote"]
            return True
        except:
            return False

    def apply(self,timeline,element,errors):
        f=0
        quoteclaimslist =  list(filter( self.filterquotes, timeline ))
        
        
        if len(quoteclaimslist) == 0:
            return AnalysisReturn( self.NAME, quoteclaimslist, f )   


        currentclaim = quoteclaimslist[0]

        for c in quoteclaimslist:
            try:
                cl1 = currentclaim["claim"]["payload"]["payload"]["quote"]["firmwareVersion"]
                cl2 = c["claim"]["payload"]["payload"]["quote"]["firmwareVersion"]
                
                if cl1 != cl2:
                    f=f+1
                    e = QuoteFirmwareDifferenceEntry(currentclaim,c,{ "firmwareVersion_old":cl2,"firmwareVersion_new":cl1})
                    timeline["timeline"].append(e.get())
                    currentclaim = c
            except Exception as ex:
                errors.append({ "err":str(ex),"function":self.NAME} )

        return AnalysisReturn( self.NAME, quoteclaimslist, f )








class QuoteQualifiedSignerDifferenceEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"QuoteQualifiedSignerDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry




class QuoteQualifiedSignerDifference(analysisFunction.AnalysisFunction):
    NAME="QuoteQualifiedSignerDifference"

    def __init__(self):
        super().__init__()

    def filterquotes(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["quote"]
            return True
        except:
            return False

    def apply(self,timeline,element,errors):
        f=0
        quoteclaimslist =  list(filter( self.filterquotes, timeline ))
        
        if len(quoteclaimslist) == 0:
            return AnalysisReturn( self.NAME, quoteclaimslist, f ) 

        currentclaim = quoteclaimslist[0]

        for c in quoteclaimslist:
            try:
                cl1 = currentclaim["claim"]["payload"]["payload"]["quote"]["qualifiedSigner"]
                cl2 = c["claim"]["payload"]["payload"]["quote"]["qualifiedSigner"]
                if cl1 != cl2:
                    f=f+1
                    e = QuoteQualifiedSignerDifferenceEntry(currentclaim,c,{ "qualifiedSigner_old":cl2,"qualifiedSigner_new":cl1})
                    timeline.append(e.get())
                    currentclaim = c
            except Exception as ex:
                errors.append({ "err":str(ex), "function":self.NAME} )

        return AnalysisReturn( self.NAME, quoteclaimslist, f )




































class QuoteClockCheckEntry:
    def __init__(self,oc,nc,d):
        self.claimTimelineEntry = { 
                "tl_type":"QuoteClockCheck",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d }

    def get(self):
        return self.claimTimelineEntry




class QuoteClockCheck(analysisFunction.AnalysisFunction):
    NAME="QuoteClockCheck"

    def __init__(self):
        super().__init__()

    def filterquotes(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["quote"]
            return True
        except:
            return False

    def apply(self,timeline,element,errors):
        f=0
        quoteclaimslist =  list(filter( self.filterquotes, timeline ))
        
        if len(quoteclaimslist) == 0:
            return AnalysisReturn( self.NAME, quoteclaimslist, f )         
        
        currentclaim = quoteclaimslist[0]

        for c in quoteclaimslist:
            try:
                cl1 = currentclaim["claim"]["payload"]["payload"]["quote"]["clockInfo"]["clock"]
                cl2 = c["claim"]["payload"]["payload"]["quote"]["clockInfo"]["clock"]
                if cl1 < cl2:
                    f=f+1
                    e = QuoteClockCheckEntry(c,currentclaim,{ "clock_old":cl2,"clock_new":cl1})
                    timeline.append(e.get())
                    currentclaim = c
            except Exception as ex:
                errors.append({ "err":str(ex), "itemid":currentclaim["itemid"], "2nd":cl2["itemid"], "function":self.NAME} )


        return AnalysisReturn( self.NAME, quoteclaimslist, f )













#TODO - fix this



class QuotePCRDigestDifferenceEntry:
    def __init__(self,oc,nc,d,p):
        self.claimTimelineEntry = { 
                "tl_type":"PCRDigestDifference",
                "itemid": str(uuid.uuid4()),
                "claim_old": oc["claim"]["itemid"],
                "tl_ts" : nc["claim"]["header"]["as_received"],
                "claim_new": nc["claim"]["itemid"],
                "difference" :d,
                "pcrselection":p }

    def get(self):
        return self.claimTimelineEntry





class QuotePCRDigestDifference(analysisFunction.AnalysisFunction):
    NAME="QuotePCRDigestDifference"

    def __init__(self):
        super().__init__()

    def filterquotes(self,x):
        try:
            y = x["claim"]["payload"]["payload"]["quote"]
            return True
        except:
            return False

    def filterpcrselections(self,c,pcrsel):
        #print("filter ",pcrsel,c["claim"]["payload"]["payload"]["quote"]["attested"]["quote"]["pcrSelect"])
        return c["claim"]["payload"]["payload"]["quote"]["attested"]["quote"]["pcrSelect"] == pcrsel
           

    def apply(self,timeline,element,errors):

        # First filter the quotes
        f=0
        quoteclaimslist =  list(filter( self.filterquotes, timeline ))
        
        if len(quoteclaimslist) == 0:
            return AnalysisReturn( self.NAME, quoteclaimslist, f )

        currentclaim = quoteclaimslist[0]

        # Then for all the quotes find all the various pcrselections used
        # using the pcrselect structure in the quote
        # the variable pcrselections is a set(!!!) of the used pcrselections

        pcrselections = []

        for c in quoteclaimslist:
            pcrselect = c["claim"]["payload"]["payload"]["quote"]["attested"]["quote"]["pcrSelect"]
            print("pcrselect ",pcrselect, " in ", pcrselect in pcrselections )
            if not( pcrselect in pcrselections):
                pcrselections.append(pcrselect)

        # This is the state of things:

        print("\nQuote Claim List is ",len(quoteclaimslist)," elements")
        print("# of PCR selections = ",len(pcrselections),"\n")
        
        #Now we have the set of all sections we iterate over this and find pcr digest differences

        for p in pcrselections:
            print("*** filtering for ",p)
            # filter quoteclaimslist by p here
            filteredquoteclaimslist =  list(filter( partial(self.filterpcrselections,pcrsel=p), quoteclaimslist ))

            print("Len of filteredquoteclaimslist is ",len(filteredquoteclaimslist)," for ",p)

            # check if filteredquoteclaimslist has enough quotes to actually check, ie: 2 or more
            if len(filteredquoteclaimslist)>1:

                currentclaim = filteredquoteclaimslist[0]

                for c in filteredquoteclaimslist:
                    try:
                        cl1 = currentclaim["claim"]["payload"]["payload"]["quote"]["attested"]["quote"]["pcrDigest"]
                        cl2 = c["claim"]["payload"]["payload"]["quote"]["attested"]["quote"]["pcrDigest"]
                        print(" ++++++ C  ",currentclaim["tl_ts"],"c",c["tl_ts"])
                        #print(" ------ cl1",cl1,"cl2",cl2)

                        if cl1 != cl2:
                            f=f+1
                            e = QuotePCRDigestDifferenceEntry(c,currentclaim,{ "pcrDigest_old":cl2,"pcrDigest_new":cl1},p)
                            timeline.append(e.get())
                            currentclaim = c

                    except Exception as ex:
                        print(currentclaim)
                        errors.append({ "err":str(ex), "itemid":currentclaim["itemid"], "2nd":cl2["itemid"], "function":self.NAME} )

        
        return AnalysisReturn( self.NAME, quoteclaimslist, f )

           