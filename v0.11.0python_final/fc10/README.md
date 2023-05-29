# ForensicsCapture

There are two ways of getting forensics, firstly running the server, or by way of the command line. See the sections below.

Read also the section on port forwarding.

## Running The Server

To run the server use

```bash
python fc.py

```

A server will run on port 5000.

To call use a URL like so:

```
http://192.168.71.128:5000/mfd?eid=845b34f4-fca2-49e1-b846-14ce9cdf37fb&asvr=http://192.168.71.128:8520/v2

```



## Running The Command Line

To run make sure that `forensics_capture.py` is executable ( `chmod a+x whateveryourusernameis`), and in the examples below replace the address and element itemid with a valid A10REST endpoint and an element itemid respectively.

To get help, type:

```bash
python3 forensics_capture.py -h
```

or `./forensics_capture.py -h` 

To generate a document use:

```bash
./forensics_capture.py http://127.0.0.1:8520/v2 845b34f4-fca2-49e1-b846-14ce9cdf37fb
```

or, by running it as a python script in its own right - this one might be easier


```bash
python3 forensics_capture.py http://127.0.0.1:8520/v2 845b34f4-fca2-49e1-b846-14ce9cdf37fb
```

This will print out the full document, for example, lots of JSON looking like this

```json
{'type': 'PCRDifference',
               'itemid': 'd2977806-5752-4fd6-8986-ab1d2b18ebd0',
               'claim1': '29fd76c6-60da-4b2e-bf97-907c9d48afd3',
               'timestamp': '1644599040.714355',
               'claim2': 'a0246e8e-38d3-43b5-9613-b82f2e9e1ce7',
               'difference': [('change',
                               'sha256.1',
                               ('0xECCD70101561E680E352D7C99CB04C7C81CDA837E9E0B9EF572CDF76343A5FB1',
                                '0x0000000000000000000000000000000000000000000000000000000000000000'))]},
              {'type': 'ElementDifference',
               'itemid': 'c6622bc4-f1ab-4003-bbf5-00cc3e355ab1',
               'claim': 'eb58f808-cf86-41ea-b6d4-f0a70f2d77d3',
               'timestamp': '1644441160.998893',
               'difference': [('add', 'type', [(3, 'letstrust')])]},
              {'type': 'ElementDifference',
               'itemid': 'b13a7571-cfd7-454d-9f9e-8184652f3fa6',
               'claim': '516f972e-0161-49b3-8bd9-1fd3fdbc7228',
               'timestamp': '1644441079.732645',
               'difference': [('change',
                               'endpoint',
                               ('http://192.168.1.82:8530',
                                'http://192.168.1.126'))]}]}
```

### Options

If you don't want to see ANY output use the quiet option `-q` on its own.

```bash
python3 forensics_capture.py http://127.0.0.1:8520/v2 845b34f4-fca2-49e1-b846-14ce9cdf37fb -q
```

If you  want to see SOME output use  `-m` to display the meta data and `-e` to display the errors. These options override `-q`. Here are three options. Actually including `-q` is usually pretty convenient as it is impossible to scroll through that much JSON

```bash
python3 forensics_capture.py http://127.0.0.1:8520/v2 845b34f4-fca2-49e1-b846-14ce9cdf37fb -q -m
python3 forensics_capture.py http://127.0.0.1:8520/v2 845b34f4-fca2-49e1-b846-14ce9cdf37fb -q -e
python3 forensics_capture.py http://127.0.0.1:8520/v2 845b34f4-fca2-49e1-b846-14ce9cdf37fb -q -m -e

```

If you want to write the output to a file, then use the `-o` option with a filename, for example, the first will just write everything to a file called `somefile` and `-q` will suppress all the output on the screen.

```bash
python3 forensics_capture.py http://127.0.0.1:8520/v2 845b34f4-fca2-49e1-b846-14ce9cdf37fb -q -o somefile

```

It is useful to include `-m` and `-e` when you do this so you get some idea that something happened and you know what

```bash
python3 forensics_capture.py http://127.0.0.1:8520/v2 845b34f4-fca2-49e1-b846-14ce9cdf37fb -q -m -e -o somefile 
```

You can load the file into any text editor, we recommend SublimeText because it has a really good JSON pretty printer built in. You can find this under the Tools menu, then Command Palette and search for Pretty JSON, eg: Pretty JSO: Format JSON


## Port Forwarding

Because the forensics server can talk to any REST endpoint there are situations where it is necessary to use use port forwarding.

For example, a server in running behind a router which forwards requests from the public internet over, say, port 8542 to that machine. The router has the address a.a.a.a and a.a.a.a:8542 is forwarded to a machine b.b.b.b:8542.

If you make a request for a.a.a.a:8542 as part of the asvr option then resolving a.a.a.a:8542 will fail as the machine handling the request, ie: b.b.b.b:8542, can't route to a.a.a.a:8542. So do the following on the machine b.b.b.b:

```bash
sudo sysctl net.ipv4.ip_forward=1
sudo iptables -t nat -A OUTPUT -p tcp --dport 8520 -j DNAT --to-destination 127.0.0.1:8520
sudo iptables -t nat -A OUTPUT -p tcp --dport 8542 -j DNAT --to-destination 127.0.0.1:8542
```

 The above routes requests for 8542 (default forensics server) and the A10 REST Endpoint on 8520.

 To make rules permanent, go here for instructions https://askubuntu.com/questions/66890/how-can-i-make-a-specific-set-of-iptables-rules-permanent