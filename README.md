# A10 - Nokia Attestation Engine

This is the source for the Nokia Attestation Engine A10.

This software is used as the remote attestation engine as part of a trusted computing environment. This is the system that holds the known good values about devices and other elements, and provides the attestation and validation mechanisms.

The software here is provided as-is - there is no security (http for the win!) and the error checking in places is completely missing. The point of this was to explore more interesting mechanisms for remote attestation and to implement ideas from the IEFT RATS specification. *READ* the security section!!!

Each directory contains a local README.md file with more information

   * ga10 - The main server-side engine.
   * ta10 - A reference trust agent for /dev/tpm* devices
   * dist - contains useful files, eg: systemd examples etc.
   * docs - not a lot here at the moment
   * v0.11.0 - the older python3 based NAE.
  
## Compiling

Ensure that Go is installed and correctly configured. You will also need the Intel SGX SDK and Edgeless libraries.

The instructions presented here have been tested in Ubuntu 22.04 om AMD64.

### Install SGX SDK and Edgeless

Intel and Edgeless supply releases for Ubuntu and other operating systems. Here we should for Ubuntu Jammy (22.04). These commands might need to be run as `sudo`. Modify as appropriate for your operating system.

```bash
mkdir -p /etc/apt/keyrings 
wget -q https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key -O /etc/apt/keyrings/intel-sgx-keyring.asc 
echo "deb [signed-by=/etc/apt/keyrings/intel-sgx-keyring.asc arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu jammy main" > /etc/apt/sources.list.d/intel-sgx.list 
apt update  
wget https://github.com/edgelesssys/edgelessrt/releases/download/v0.4.1/edgelessrt_0.4.1_amd64_ubuntu-22.04.deb 
apt-get install -y ./$ERT_DEB build-essential cmake libssl-dev libsgx-dcap-default-qpl libsgx-dcap-ql libsgx-dcap-quote-verify
```

### Compiling GA10

Compilation requires an up-to-date mod file and the Edgeless environment variables - which should be installed if the above went correctly. *MAKE SURE* you are in the `ga10` directory when you run these commands:

```bash
go get -u
go mod tidy
. /opt/edgelessrt/share/openenclave/openenclaverc && GOOS=linux GOARCH=amd64 go build -o ga10
```

You will now get a file called `ga10` which is your executable.

If you wish to reduce the size of the binary, run `strip ga10`

## Compiling TA10

*MAKE SURE* you are in the `ta10` directory.  TA10 is much simpler than ga10 and requires just compilation. For your local operating system and architecture you can remove the `GOOS` and `GOARCH` variables, for example as shown below. The `strip` command is optional but it does reduce the binary size a little.

```bash
go get -u
go mod tidy
go build -o ta10
strip ta10
```

For other architectures, use `go tool dist list` for a list of operating system and architecture options. Listed below are a few common options - and we like to append this to the binary name when we're generating a few of these for the devices we have (remeber amd64 is 64-bit Intel/AMD x86 based chips, eg: Xeons, i9's, i7's, Threaripper etc etc)

```bash
GOOS=linux GOARCH=arm go build -o ta10_arm                 # eg: Pi 3s
GOOS=linux GOARCH=arm64 go build -o ta10_arm64             # eg: Pi 4, 5s in 64-bit mode (also 3's I think)
GOOS=windows GOARCH=amd64 go build -o ta10_win             # eg: Pretty much every Win10, Win11 machine
GOOS=plan9 GOARCH=386 go build -o ta10_belllabs            # Because we're Bell Labs....
GOOS=linux GOARCH=s390x go build -o ta10_mainframe         # Because you either have an z-Series in the basement or Hercules
GOOS=solaris GOARCH=amd64 go build -o ta10_solaris         # I still mourn the lost of the SparcStation and UltraSparcs, RIP Sun.
GOOS=opebsd GOARCH=amd64 go build -o ta10_openbsd          # BSD for security
GOOS=freebsd GOARCH=amd64 go build -o ta10_freeebsd        # More BSD fun
GOOS=darmin GOARCH=arm64 go build -o ta10_mac              # For the Apple people out there...no TPM, but if you figure out attesting a T2 let me know
GOOS=aix GOARCH=ppc64 go build -o ta10_aix                 # If you have an AIX box, let me know...DRTM is supported during boot and a TPM too?
```

## Running GA10

GA10 requires a configuration file and optionally keys for the https certs. We've supplied a temporary key in the dist folder...don't use these unless you're crazy. We also like triggering github to give us private key warnings because we've stored them there. Browsers will complain unless your certs a signed by a suitable authority, eg: LetsTrust.

An example configuration file with explanation is given below. Store this in the same place as the `ga10` binary and call it `config,yaml`

Generate some keys and store these in the same directory as the `ga10` binary too.

```bash
openssl genrsa 2048 > temporary.key
chmod 400 temporary.key 
openssl req -new -x509 -nodes -sha256 -days 365 -key ga10https.key -out ga10https.crt
```

To start `ga10` run and point the config option to where the config file is

```bash
./ga10 -config=config.yaml
```

You should see something similar to the following. If a config file is not found it will exit immediately. If Mosquitto or Mongo are not available then it will eventually time out. Finally if you are using https for the webui service, if the keys are not found it will exit with an error. The name of the database, location of config file and session identifier will all be different of course.

```bash
generating private, public key pair for claim signing - just for this session so no chance to verify later. THese keys MUST be external
GA10: Configuration file location:  /home/ian/config.yaml
GA10: initialising database MONGO connection
GA10: Database infrastructure MONGO is running
GA10: Initialising message infrastructure MQTT connection
GA10: Message infrastructure MQTT is running
GA10: MQTT connected

+========================================================================================
|  GA10 version
|   + linux O/S on amd64
|   + version v1.0rc1, build not set
|   + runing with name ASVR_GO_1_TEST
|   + session identifier is 5eddb86d-ce05-4319-91aa-d4815d61b008
+========================================================================================

X3270 service listening on port 3270
⇨ http server started on [::]:8520
⇨ http server started on [::]:8540
```

If that works, point your browser at the machine where this is running and port 8540.


## Running TA10

Running TA10 is simple, just use

```bash
./ta10
```

```bash
+========================================================================================
|  TA10 version - Starting
|   + linux O/S on amd64
|   + version v0.1, build not set
|   + session identifier is 19a14951-76c3-4641-b9ac-fa65683e5c36
|   + unsafe mode? false
+========================================================================================

⇨ http server started on [::]:8530
```

If you are running on Linux and need access to files such as the UEFI log file then you will need to run ta10 as sudo.

TA10 requires access to the TPM device, eg `/dev/tpm0` on Linux (Windows handles this internally), and so whichever user ta10 is running as needs access to that device.

```bash
sudo ./ta10
```

Read the section on advanced TA10 usage.

## GA10 Configuration File

Note the lines with "CHANGE ME":

   * The name of the system can set to anything you want.
   * The MQTT client ID must be unique if you indend running more than one instance
   * The MQTT port probably doesn't need chaning
   * The MOTT server must allow anonymous connections, read the Mosquitto documentation for this
   * Address of the monogoDB server must be set
   * Choose any name you want for your attestation database
   * crt and key files should have the full path to the keys generated earlier
   * usehttp -- set this to true if you want less#CHANGE ME security
   * X3270 -- don't worry about this, but if you have an X3270 terminal, the TUI might be useful (when we finish it)
   * Logging goes to a default place in /var/log or somewhere suitable on Windows

Read the sections on security and keys

```yaml
#Some general naming
system:
  name: ASVR_GO_1_TEST                              #CHANGE ME

#MongoDB Configuration
database:
  connection: mongodb://192.168.1.203:27017         #CHANGE ME
  name: attestationDatabase                         #CHANGE ME

#MQTT Configuration
messaging:
  broker: 192.168.1.203                             #CHANGE ME
  port: 1883
  clientid: attestationMQTTclient                   #CHANGE ME

#REST Interface Configuration
rest:
  port: 8520
  crt: temporary.crt                                #CHANGE ME
  key: temporary.key                                #CHANGE ME
  usehttp: false                                    #CHANGE ME

#Web Interface Configuration
web:
  port: 8540
  crt: temporary.crt                                #CHANGE ME
  key: temporary.key                                #CHANGE ME
  usehttp: false                                    #CHANGE ME


#X3270
x3270:
  port: 3270
  
#Log file
logging:
  logfilelocation: /var/log/ga10.log                #CHANGE ME
  sessionupdatelogging: false
```





## Advanced TA10 - Here be a good way to open your system to every hacker ever

TA10 CURRENTLY starts all the services, ie: it will happily offer TPM, IMA, UEFI services etc, even if these are not available. In a later version these will have be switched on specifically, but don't worry about this.

TA10 can read UEFI and IMA logs in non-standard places, but in order to do this, the element description in the GA10's database would have to refer to those specifically. TA10 by default operates in a *safe* mode where it will only use the standard locations in Linux's securityfs. You can turn off this mode:

```bash
sudo ./ta10 -unsafe=true
```

which responds with

```bash
$ sudo ./ta10 -unsafe=true
+========================================================================================
|  TA10 version - Starting
|   + linux O/S on amd64
|   + version v0.1, build not set
|   + session identifier is 4e85a08d-7d1c-450d-9a7a-659f29ab8380
|   + unsafe mode? true
+========================================================================================


!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
TA10 is running in UNSAFE file access mode.  Unsafe is set to true
Requests for log files, eg: UEFI, IMA, that supply a non default location will happily read that file
This is a HUGE security issue. YOU HAVE BEEN WARNED
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
⇨ http server started on [::]:8530
```

Really, *don't do this*...you've now given everyone with access to port 8530 (and it is all over http) root access to every file on your system.

## Security

Read carefully:

   * A self-signed key is provided called temporary.key/crt - DO NOT USE THIS IN PRODUCTION OR ANYWHERE. Browsers will complain if you use this.
   * THIS IS NOT SECURE!!!
   * PUTTING PRIVATE KEYS ON GITHUB FOR ANYTHING ELSE THAN A DEMONSTRATION IS CRAZY. DO NOT DO THIS.
   * TO SAVE YOURSELF, SET THE use http FIELDS TO true in the configuration file.
   * That isn't secure either...  
   * GENERATE YOUR OWN KEYS AND KEEP THEM SECURE and use https
   * TA10 runs over HTTP !!!!   
   * DO NOT USE TA10 IN UNSAFE MODE !!! (Even if you're not root!)
   * A10 signs claims, results, sessions etc...the keys are randomly generated each him the system is started and aren't recorded anywhere. There is some code to talk PKCS#11 and has been tested with a YubiHSM but it isn't used. Don't rely upon it, I haven't tested it and it is just placeholder at this time. Yes, I'll get arund to writing the proper functionality real soon now...volunteers?

Now read the section on the use of this software in a production environment.

## Use in a Production Environment

Don't.  This is not secure and many points where errors and exceptions should be captured are not implemented.


