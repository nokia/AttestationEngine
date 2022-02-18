import a10.asvr.sessions

s = a10.asvr.sessions.openSession()
print("RC=",s.rc())
print("MSG=",s.msg())

ses=s.msg()

print("We have a new session ",ses)

s = a10.asvr.sessions.getOpenSessions()
print("open",s)

s = a10.asvr.sessions.getClosedSessions()

print("closed",s)

s = a10.asvr.sessions.getSession(ses)

print("This is the session just created ",s)

s = a10.asvr.sessions.closeSession(ses)
print("RC=",s.rc())
print("MSG=",s.msg())


s = a10.asvr.sessions.getOpenSessions()

print("open",s)

s = a10.asvr.sessions.getClosedSessions()

print("closed",s)

s = a10.asvr.sessions.getSession(ses)

print("This is the session again ",s)

print(s.rc())

print(s.msg())

print("Trying to close again . this should fail")
s = a10.asvr.sessions.closeSession(ses)
print("RC=",s.rc())
print("MSG=",s.msg())