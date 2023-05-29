# Copyright 2021 Nokia
# Licensed under the BSD 3-Clause License.
# SPDX-License-Identifier: BSD-3-Clause

import subprocess
import yaml
import tempfile


class TPM:
    def __init__(self, ek=None, ak=None):
        self.ekHandle = ek
        self.akHandle = ak

    def hello(self):
        return "hello"

    def call(self, cmd):
        cmdlist = cmd.split()
        print("CALLING SUBPROCESS")
        out = subprocess.check_output(cmdlist)
        return out

    def readEventLog(self):
        cmd = "tpm2_eventlog /sys/kernel/security/tpm0/binary_bios_measurements"
        y = yaml.load(self.call(cmd), Loader=yaml.BaseLoader)
        print("eventlog ", y)
        return y

    def getRandom(self, n, hex=True):
        cmd = "tpm2_getrandom " + str(n)
        if hex == True:
            cmd = cmd + " --hex"

        return self.call(cmd)

    def getNVlist(self):
        cmd = "tpm2_nvlist"
        # need to fix this
        y = yaml.load_all(str(self.call(cmd), "utf-8"))
        # y = self.call(cmd)
        return y

    def readPCRs(self):
        cmd = "tpm2_pcrread"

        return yaml.load(self.call(cmd), Loader=yaml.BaseLoader)

    def evictcontrol(self, handle):
        cmd = "tpm2_evictcontrol -c " + handle

        return self.call(cmd)

    def getcap(self, c):
        cmd = "tpm2_getcap " + c

        return self.call(cmd)

    def createek(self, handle, hash="rsa"):
        cmd = "tpm2_createek -G " + hash + " -c " + handle
        self.ekHandle = handle

        return self.call(cmd)

    def sign(self, handle, inputdata, hashalg="sha256", scheme="rsassa"):
        result_stdout = ""
        result_filecontents = ""

        with tempfile.NamedTemporaryFile() as inpf:
            with tempfile.NamedTemporaryFile() as outf:
                inpf.write(inputdata)
                inpf.flush()
                cmd = (
                    "tpm2_sign -c "
                    + handle
                    + " -g "
                    + hashalg
                    + " -s "
                    + scheme
                    + " -o "
                    + outf.name
                    + " "
                    + inpf.name
                )
                result_stdout = self.call(cmd)
                outf.flush()
                result_filecontents = outf.read()

        return (result_stdout, result_filecontents)

    # this expects the binary quote, note the yaml or stdout from quote
    # FIXME
    def tpms_attest_as_yaml(self, a):
        p_result = ""
        print("a", a)
        with tempfile.NamedTemporaryFile() as qf:
            qf.write(a.encode("utf-8"))
            qf.flush()
            cmd = "tpm2_print -t TPMS_ATTEST " + qf.name
            p_result = self.call(cmd)

        return p_result

    def quote(self, ownak=None, pcrs="sha1:0+sha256:0", hashfunction="sha256"):
        ak = None

        if ownak == None:
            ak = self.akHandle
        else:
            ak = ownak

        print("quoting ", ak, pcrs, hashfunction)
        with tempfile.NamedTemporaryFile() as qf:
            cmd = (
                "tpm2_quote -c "
                + ak
                + " -l "
                + pcrs
                + " -g "
                + hashfunction
                + " -m "
                + qf.name
            )
            q_result = self.call(cmd)
            qf.flush()
            r_result = qf.read()

            cmd = "tpm2_print -t TPMS_ATTEST " + qf.name
            p_result = self.call(cmd)

        # now construct the JSON as YAML is poo
        # print(yaml.dump(yaml.load(p_result), default_flow_style=False))
        yl = yaml.load(p_result, Loader=yaml.FullLoader)
        print("YAML", yl)

        return yl
