#Copyright 2021 Nokia
#Licensed under the BSD 3-Clause Clear License.
#SPDX-License-Identifier: BSD-3-Clear

import setuptools

with open("README.md", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name="a10",
    version="2022.1.27.3",
    author="Ian Oliver",
    author_email="ian.oliver@nokia-bell-labs.com",
    description="Attestation Services Engine and Libraries",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/nokia/AttestationEngine",
    packages=setuptools.find_packages(),
    install_requires=["pymongo", "paho-mqtt", "pyserial", "requests"],
    classifiers=["Programming Language :: Python :: 3", "Operating System :: Linux",],
    python_requires=">=3.8",
)
