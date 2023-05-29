def generateAnalytics(d):
	"""
	d is the JSON format of a Forensics Document

	This function returns a structure with various statistics to be graphed
	"""

	# Overview of contents
	nc = d["metadata"]["nclaims"]
	nr = d["metadata"]["nresults"]	
	nso = d["metadata"]["nsessionsopen"]	
	nsc = d["metadata"]["nsessionsclosed"]	
	nl = d["metadata"]["nlogentries"]	
	no = d["metadata"]["ntimelineentries"]-nc-nr-nso-nsc-nl

	contentoverview = { "nc":nc, "nr":nr, "nso":nso, "nsc":nsc,"no":no, "nl":nl }

	

	# Count results - yes I know I should use collections....
	frs = [ x for x in d["timeline"] if x["tl_type"]=="result"]
	rsucceed = len([x for x in frs if x["result"]["result"]==0])
	rfail = len([x for x in frs if x["result"]["result"]==9001])
	rerror = len([x for x in frs if x["result"]["result"]==9002])
	rnoresult = len([x for x in frs if x["result"]["result"]==9100])

	resultoverview = {"rs":rsucceed, "rf":rfail, "re":rerror, "rn":rnoresult}

	

	# Generate Clock Reset Values

	cs = [ x for x in d["timeline"] if x["tl_type"]=="claim"]
	qs = [ x for x in cs if x["claim"]["payload"]["payload"].get("quote")!=None ]

	
	clock = [ {"x":e["tl_ts"],
	           "y":e["claim"]["payload"]["payload"]["quote"]["clockInfo"]["clock"]}  
	           for e in qs ]
	reset = [ {"x":e["tl_ts"],
	           "y":e["claim"]["payload"]["payload"]["quote"]["clockInfo"]["resetCount"]}  
	           for e in qs ]
	return { "contentoverview":contentoverview,
	         "resultoverview": resultoverview,
	         "clock": clock,
	         "reset": reset }	