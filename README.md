# A10 - Nokia Attestation Engine

This is the source for the Nokia Attestation Engine A10.

This software is used as the remote attestation engine as part of a trusted computing environment. THis is the system that holds the known good values about devices and other elements, and provides the attestation and validation mechanisms.

The sofware here is provided as-in - there is no security (http for the win!) and the error checking in places is completely missing. The point of this was to explore more interesting mechanisms for remote attestation and to implement ideas from the IEFT RATS specification.

The engine itself in a future production environment would be effectively hidden by higher-level components providing integrations to other managemenet components etc.

## Contents

Each directory contains a local README.md file with more information

   * a10 - base libraries
      * asvr - the server libraries - low-level stuff you probably don't need these usually
      * structures - constants, helper classes etc - you probably will need these
   * u10 - user interface for low-level access
   * t10 - trust agents (note: plural!)
   * a10rest - the REST API server
   
   * apps - various apps and templates for application development
   * utilities - utilities, database setup & config, example a10.conf files, docker-compose files etc
   * tests - various tests that can be run for debugging

   
## Documents by Sphinx

Under a10 is the doc directory is a file called doc/build/index.html. That's the starting place for a10's documentation. See the a10 README.md for more information on how this is built


## Installation and Running

Refer to INSTALL.md for details about how to install with docker-compose

To install and run components individually refer to the README and INSTALL files for those specific components; eg, when testing u10 and modification to the a10 libraries.

A quick and dirty guide is presented below. This may be out of date with respect to what is presented in the specific README/INSTALL files for the components in question.

### Prerequisites
A10 requires mongodb and mosquitto to be available unless running with docker-compose in which case these are included.

### Configuration
The A10 libraries will look for a file called `/etc/a10.conf`

Ensure that the mqtt and mongo sections are correctly filed and that the logfile can be written to, eg: `/tmp`

### Installing a10 libraries locally

To install the libraries you can use `python3 setup.py` install from the a10 directory or use pip3 to download and install if a pypi server is available with the current/latest version of a10.

### Running
Various components need to be started
To run u10:

```bash
cd u10
python3 u10.py
```

To run a10rest:

```bash
cd a10rest
python3 a10rest.py
```

A trust agent needs to be run on all machines that you wish to attest. The trust agent is typically self-contained and does not require installation of the A10 libraries or any other component. See specific TA's and the protocols they support.

The reference TA is run:

```bash
cd t10/py
python3 ta.py
```

### Database Contents

Under utilities/Database are a set of files with starting information for a black attestation server. These include

   * Policies
   * Hash Values
   
Follow the README.md there



# Use in a Production Environment
Don't.  This is not secure and many points where errors and exceptions should be captured are not implemented.