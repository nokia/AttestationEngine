# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import setuptools

with open("README.md", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name="a10",
    version="2021.11.3",
    author="Ian Oliver",
    author_email="ian.oliver@nokia-bell-labs.com",
    description="Attestation Services Engine and Libraries",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="See MS Teams",
    packages=setuptools.find_packages(),
    install_requires=["pymongo", "paho-mqtt", "pyserial", "requests"],
    classifiers=["Programming Language :: Python :: 3", "Operating System :: Linux",],
    python_requires=">=3.8",
)
