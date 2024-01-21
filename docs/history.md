# History

The attestation engine has a long an varied history

## A1,A2 and A3

Early experiments, circle late 2014, early 2015. Written in Javascript with lots of pain regarding reactive, functional programming

## A4
    
First version that actually worked, also written in Javascript. Not very functional but there existed a trust agent for quoting and quite similar in functionality to systems such as OpenAttestation, Mt.Wilson, Charra and early Keylime.

## A5 and A6

First versions in python 2 and some of the code here ended up in A7. Some parts of A5 and A6 integrated with OpenStack's workflow and workload manager APIs - which worked as long as you didn't update OpenStack as APIs changed randomly betweent versions.

## A7

First properly working attestation server in a very traditional manner (cf: OpenAttestation etc). Written in a mix of python2 and python3. Backend utilised mongodb and assumed the TA talked directly to the tpm2_tools on the client. UI written using python flask. Integrated with OpenStack via a client which was a MITM/hack/kludge to access OpenStack's message bus. This version was demonstrated at ETSI SecurityWeek in 2018 and 2019. Contained a lot of technical debt which made integration hard in some cases.

## A8.1 and A8.2

These were an attempt to write everything in Erlang, and simulaneously in python with threads, proceses and using MQTT for interprocess communication in a complete asynchronous way. I don't think the source coude even exists anymore.

## A9

Was an experiment to use the ELKS analytics stack and the ELKS UI. Was demonstrated in January 2020 for the first time and actively developed for about 6 months until we needed to support new features. Much of this work reappears as plugins and data integration (as attesation apps in A10) into ELKS and ELKS-based tools such as Nokia's NetGuard.

## A10

A much more compliant with IEFT RATS in its data structures. Has a proper protocol backend which takes care of layer 7 and layer 6 of the attestation protocols so we can theoretically support any RoT/auditing system, eg: HSM, UEFI, TPM2.0, TPM1.2 etc, we can even integrate with other attestation engines' trust agents such as KeyLime, OpenAttestation, Go-Attestation etc. Libraries are much better written with a much more consistent API. UI and interfaces are separate components built on top of libraries. The whole thing is designed to integrate with MQTT, logstash, ELKS, whatever AI/ML analytics and the forensics system that is currently under development. Lots of cool features. Supports the notion of *attestation applications*</em>* for extensibility making integration with other systems such as Edge/NFV MANO very quick and easy.

## GA10

Even more compliant with IEFT RATS in its data structures and an improved protocol backend which takes care of layer 7 and layer 6 of the attestation protocols. All the cool stuff of A10 but now written in *Go*. Seriously strong typing is so important....Reworked UI, reworked REST API and reworked data structures. Oh so much better. AND, Go has a really nice cross compiler: arm, arm64, i386, amd64, ppc, and then Linux, Windows, Solaris, AIX and Plan 9 from Bell Labs. Also Go makes self-contained binaries so the trust agent is a single binary which makes distribution trivial and the whole server engine itself just needs the binary and the config file :-)  

Includes rules for working with confidential computing structures too.