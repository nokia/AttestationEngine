from pcrDifferences import PCRDifference  
from systeminfoDifferences import *  
from quoteDifferences import *
from elementDifferences import *

ANALYSISRULES = {
    "PCRDifference":PCRDifference(),
    "QuoteResetDifference":QuoteResetDifference(),
    "QuoteRestartDifference":QuoteRestartDifference(),
    "PCRDQuoteSafeDifferenceifferemce":QuoteSafeDifference(),
    "QuoteFirmwareDifference":QuoteFirmwareDifference(),
    "QuoteQualifiedSignerDifference":QuoteQualifiedSignerDifference(),
    "QuoteClockCheck":QuoteClockCheck(),
    "QuotePCRDigestDifference":QuotePCRDigestDifference(),
    "ElementDifference":ElementDifference(),
    "SystemInformationDifference":SystemInformationDifference(),
    "ThinkLMIDifference":ThinkLMIDifference()

    }
                

def applyAllAnalysisRules(f):
    """
        f is a forensics document which gets modified in place as per python object semantics
    """

    for a in ANALYSISRULES.keys():
        f.analyse(ANALYSISRULES[a])

   
def applySelectedAnalysisRules(f,r):
    """
       f is a forensics document
       r is a list of list of str, where each element is a forensics rule
    
    TODO: make this a tree!

    """

    for outer in r:
        for inner in outer:
            try:
                f.analyse(ANALYSISRULES[inner])
            except:
                f.errors.append({ "err":"Uknown analysis rule: "+inner, "itemid":"", "function":"fcrules.applySelectedAnalysisRules"} )

