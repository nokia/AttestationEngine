#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

import pymongo
import d10conf

#
# Configuration
#

d10client = pymongo.MongoClient(d10conf.MONGODBURL)
db10db = d10client[d10conf.MONGODBNAME]

#
# Database Status
#

def getDatabaseStatus():
    """ Returns information on the state of the database

    :return: a structure containing information about the number of items stored in the database and other meta-data
    :rtype: dict

    """

    dbstatus = {}

    for c in ["att","eva",]:
        collection = db10db[c]
        count = collection.estimated_document_count()
        dbstatus[c] = str(count)

    dbstatus["dburl"]=d10conf.MONGODBURL
    dbstatus["dbname"]=d10conf.MONGODBNAME
    
    return dbstatus

#
# ATT: Attestation Templates
#

def addatt(name,script):
    collection = db10db["att"]
    e = { "name":name,"script":script }
    r = collection.insert_one(e)

    if r.inserted_id == None:
        return False
    else:
        return True

def getatt(name):
    collection = db10db["att"]
    e = collection.find_one({"name": name}, {"_id": False})
    return e   

def getatts():
    collection = db10db["att"]
    es = collection.find({}, {"_id": False})
    return list(es)

#
# EVA: Evauations
#

def addeva(name,script):
    collection = db10db["eva"]
    e = { "name":name,"script":script }
    r = collection.insert_one(e)

    if r.inserted_id == None:
        return False
    else:
        return True

def geteva(name):
    collection = db10db["eva"]
    e = collection.find_one({"name": name}, {"_id": False})
    return e   

def getevas():
    collection = db10db["eva"]
    es = collection.find({}, {"_id": False})
    return list(es)    