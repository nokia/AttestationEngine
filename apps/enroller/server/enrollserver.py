# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import tempfile
import uuid
import base64
import subprocess
import base64
from flask import Flask, request, jsonify

eapp = Flask(__name__)

enrollmentdb = {}

#
# Home
#

@eapp.route("/")
def hello():
	return "Hello from Enrollment Engine", 200


#
# Enrollment Functions
#

@eapp.route("/enroll/credentialcheck", methods=['POST'])
def credentialcheck():
	content = request.json
	#print("content", content)

	ekpub=content['ekpub']
	akname=content['akname']

	print("EKPUB ",ekpub)
	print("AKNAME ",akname)

	#bit of housekeeping
	#store ek in temporary file
	ektf = tempfile.NamedTemporaryFile()

	ektf.write(bytes(ekpub,'utf-8'))
	ektf.seek(0)

	#print("ektf",ektf.read().decode('ascii'))
	#ektf.seek(0)

	#generate secret
	secret = str(uuid.uuid4())

	secf = tempfile.NamedTemporaryFile()
	secf.write(bytes(secret,'ascii'))
	secf.seek(0)

	#temporary file for credential
	credf = tempfile.NamedTemporaryFile()


	#makecredential
	#cmd = "tpm2_makecredential -u "+ektf.name+" -s "+secf.name+" -n "+akname+" -G rsa -o "+credf.name
	cmd = "tpm2_makecredential -s "+secf.name+" -u "+ektf.name+" -n "+akname+" -G rsa -o "+credf.name

	print("CMD=",cmd)
	out=subprocess.check_output(cmd.split())


	#cmd="cp "+credf.name+" /tmp/credential"
	#out=subprocess.check_output(cmd.split())
	#print("cpout=",out)

	#if that worked then create a session ID and add that with the secret to the enrollmentdb

	sessionid = str(uuid.uuid4())
	enrollmentdb[sessionid]=secret		

	#read the credential
	credf.seek(0)
	cred = base64.b64encode(credf.read()).decode('utf-8')
	print("cred=",cred)
	
	#cleanup
	ektf.close()
	secf.close()
	credf.close()

	#send response

	res = { "session":sessionid,"credential":cred }

	print("Enrollment db ",len(enrollmentdb),"items")

	return res,200


#
# EnrolProcess
#


@eapp.route("/enroll/element/<sessionid>", methods=['POST'])
def enrollelement(sessionid):
	#First check if there is a valid session id
	try:
		sessionsecret = enrollmentdb[sessionid]
	except:
		return "Invalid Session",422


	content = request.json

	esecret = content['secret']
	eelement = content['element']
	print("esecret", esecret)

	if esecret != sessionsecret:
		return "Incorrect secret",400

	res={"test":"1"}

	return res,200


#
# Main
#


def main(cert, key, config_filename='eapp.conf'):
	eapp.config.from_pyfile(config_filename)
	if cert and key:
		eapp.run(debug=eapp.config['FLASKDEBUG'], threaded=eapp.config['FLASKTHREADED'],
					host=eapp.config['DEFAULTHOST'],
					port=eapp.config['DEFAULTPORT'], ssl_context=(cert, key))
	else:
		eapp.run(debug=eapp.config['FLASKDEBUG'], threaded=eapp.config['FLASKTHREADED'],
					host=eapp.config['DEFAULTHOST'],
					port=eapp.config['DEFAULTPORT'])


if __name__ == '__main__':
	print("EAPP Starting")
	main('', '')    