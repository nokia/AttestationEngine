# A10 - Nokia Attestation Engine

This is the source for the Nokia Attestation Engine A10.

This software is used as the remote attestation engine as part of a trusted and/or confidential computing environment. This is the system that holds the known good values about devices and other elements, and provides the attestation and validation mechanisms.

The software here is provided as-is - there is no security (http for the win!) and the error checking in places is completely missing. The point of this was to explore more interesting mechanisms for remote attestation and to implement ideas from the IEFT RATS specification. *READ* the security section!!!

Refer to the [contents](docs/contents.md) in the `docs` directory.