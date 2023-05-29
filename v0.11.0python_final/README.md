# A10 - Nokia Attestation Engine

This is the source for the Nokia Attestation Engine A10.

This software is used as the remote attestation engine as part of a trusted computing environment. THis is the system that holds the known good values about devices and other elements, and provides the attestation and validation mechanisms.

The software here is provided as-is - there is no security (http for the win!) and the error checking in places is completely missing. The point of this was to explore more interesting mechanisms for remote attestation and to implement ideas from the IEFT RATS specification.

The engine itself in a future production environment would be effectively hidden by higher-level components providing integrations to other managemenet components etc.

## Contents

Each directory contains a local README.md file with more information

   * a10server - libraries for running an attestation server
   * a10structures - libraries for common data structures values etc.
   * u10 - user interface for low-level access
   * t10 - trust agents (note: plural!)
   * a10rest - the REST API server
   
   * docs - documentation   <-  READ THIS

   * apps - various apps and templates for application development
   * utilities - utilities, database setup & config, example a10.conf files, docker-compose files etc
   * tests - various tests that can be run for debugging


## Installation and Running
Refer to the main documentation directory for more information.

   * [Start Here](./docs/STARTHERE.md)
   * [Quickstart Installation](./docs/quickstart/installation.md)

## Prerequisites
An A10 server requires mongodb and mosquitto to be available.

U10 and A10REST require the server and structures libraries to be installed, and by implications mongodb and mosquitto.

## Configuration
The A10 libraries will look for a file called `/etc/a10.conf`

Ensure that the mqtt and mongo sections are correctly filed and that the logfile can be written to, eg: `/tmp`


# Use in a Production Environment
Don't.  This is not secure and many points where errors and exceptions should be captured are not implemented.
