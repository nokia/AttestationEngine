import json
import requests
from pathlib import Path


def read_config():
    p = Path(__file__).with_name('manager_config.json')
    with p.open('r') as f:
        return json.loads(f.read())


IP = read_config()["attestation_server_ip"]
PORT = read_config()["attestation_server_port"]
BASE_URL = f"http://{IP}:{PORT}"


def open_session() -> str:
    """Opens a session on the attestation server. The session is used to 
    store multiple requests eg. attest or verify requests.

    Returns
    -------
    string
        session as a JSON string.  """
    print("opening session")
    response = requests.post(f"{BASE_URL}/v2/sessions/open")
    if response.ok:
        print("New session created successfully")
        return response.json()
    else:
        print("session creation failed")
        return ''


def close_session(id):
    """
    Closes the request provided as a parameter

    Parameters
    ----------
    id : string
        id of the session to be closed.

    """

    # TODO: change this to return something when successful.
    close = requests.delete(f"{BASE_URL}/v2/session/{id}")
    print("New session closed")
    return close


def get_policy_id() -> str:
    """
    Retrieves the policy id from the API.

    Returns
    -------
    string
        policy id as a string.
    """
    print("get_policy_id sending request ")
    response = requests.get(
        f"{BASE_URL}/v2/policy/name/TPMIdentityAttestation")

    if response.ok:
        print("Policy id retrieved")
        return response.json()['itemid']
    else:
        print("policy id not retrieved")
        return ''


def create_dict(payload, sid) -> dict:
    """
    Creates a dictionary with the necessary information.

    The data is retrieved from the API and the values are stored in the correct
    place for the attestation to be successful.

    Returns
    -------
    dict 
        the newly created dictionary.
    """
    print("starting create_dict method")
    if payload["device"]["hostname"]:
        # hostname = payload["device"]["hostname"]
        itemid = payload["device"]["itemid"]
    else:
        print("create_dict payload hostname error")
        return {}
    response = requests.get(f"{BASE_URL}/v2/element/{itemid}")

    if not response.ok:
        print("create_dict response not ok")
        return {}

    element = response.json()

    print("create_dict element print: ", element)

    tpm = element["tpm2"]["tpm0"]
    eid = element["itemid"]

    pid = get_policy_id()

    akname = tpm["ekname"]
    ekpub = tpm["ekpem"]
    cps = {"a10_tpm_send_ssl": element["a10_tpm_send_ssl"],
           "akname": akname, "ekpub": ekpub}

    o = {"eid": eid, "pid": pid, "cps": cps, "sid": sid}

    print("create_dict printing o: ", o)

    return o


def attest(o: dict) -> str:
    """
    Main function for the attestation procedure. Posts given object to the
    /v2/attest endpoint.

    Parameters
    ----------
    o : dict 
        dictionary with the necessary fields for attest.
    """
    print("attest sending request")
    response = requests.post(f"{BASE_URL}/v2/attest",
                             json=o, headers={"Content-Type": "application/json"})
    if response.ok:
        return response.json()
    else:
        print("attest response not ok")
        return ''


def verify(o: dict) -> str:
    """
    Verifies the given dictionary. Makes a POST request to the API which then
    verifies the data.

    Parameters
    ----------
    o : dict
        dictionary with the data to be verified.

    Returns
    -------
    string
        JSON object.
    """

    print("verify sending request")
    response = requests.post(
        f"{BASE_URL}/v2/verify", headers={"Content-Type": "application/json"}, data=json.dumps(o))

    if response.ok:
        return response.json()
    else:
        print("verify response not ok")
        return ''


def check_validity(payload: dict):
    """
    Main function of the program. Creates a dictionary from data gathered from the API
    and verifies it. The ruleset is currently hard-coded as a TPM2CredentialVerify but
    this could be changed in the future. Publishes the results to the MQTT topic set as
    a constant.

    Parameters
    ----------
    payload : dict
        this is currently unused.
    """
    print("check_validity opening session")
    sid = open_session()

    print("check_validity creating dict")
    o = create_dict(payload, sid)

    if not o:
        payload.update({"event": "device validation fail"})
        payload["device"]["valid"] = False
        payload.update({"message": "object creation failed"})
        print("check_validity not o")
        return payload

    print("check_validity attesting")
    cid = attest(o)

    if not cid:
        payload.update({"event": "device validation fail"})
        payload["device"]["valid"] = False
        payload.update({"message": "attestation failed"})
        print("check_validity not cid")
        return json.dumps(payload)

    rul = ["tpm2rules/TPM2CredentialVerify", {}]

    o.update({"cid": cid['claim']})
    # This needed?
    o.update({"sid": sid})
    o.update({"rule": rul})

    result = verify(o)

    if not result:
        print("check_validity not result")
        return {"error": "verification failed"}

    o.update({"result": result["result"]})

    close_session(sid)

    payload["device"]["valid"] = True
    payload.update({"itemid": o.get("eid")})
    payload.update({"event": "device validation ok"})
    payload.update({"message": "validation successful"})

    print("check_validity: ", payload)

    return payload


if __name__ == "__main__":
    print(BASE_URL)
