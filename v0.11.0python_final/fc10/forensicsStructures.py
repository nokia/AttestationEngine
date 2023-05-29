class AnalysisReturn:
	def __init__(self,n,a,d):
		"""
		Expects a string, a list and an int
		"""
		self.name = n    #str
		self.claimsAnalysed = len(a)   #list
		self.differences = d   #int

	def get(self):
		return { "name":self.name,
                 "claimsanalysed" : self.claimsAnalysed, "differencesfound": self.differences }  