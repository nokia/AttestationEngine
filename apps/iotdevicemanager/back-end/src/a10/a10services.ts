import {
  a10RestApi,
  policyId,
  policyId2,
  cps,
  headers,
  rul,
  credentialRule,
} from '../utils/GlobalVariables'
import axios from 'axios'

const url = a10RestApi
let sessionId = ''

const getElement = async (eid) => {
  try {
    const result = await axios.get(`${url}/element/${eid}`)
    return result.data
  } catch (error) {
    console.log('error occurred fetching an element')
  }
}

const getProtocolParameters = async (eid) => {
  const element = await getElement(eid)
  return {
    a10_tpm_send_ssl: {
      key: '/var/attestation/iotpis',
      timeout: 20,
      username: 'pi',
    },
    akname: element.tpm2.tpm0.akname,
    ekpub: element.tpm2.tpm0.ekpem,
  }
}

const openSession = async () => {
  try {
    const results = await axios.post(`${url}/sessions/open`)
    console.log('succesfully opened a session', results.data.itemid)
    return results.data.itemid
  } catch (error) {
    console.log('error opening a session', error)
  }
}

const closeSession = async (sid) => {
  try {
    const results = await axios.delete(`${url}/session/${sid}`)
    console.log('succesfully closed session', results.data)
  } catch (error) {
    console.log('error closing down the session', error)
  }
}

const runAttestAndVerify = async (sid, eid) => {
  try {
    const pcReadAttest = await runAttest(sid, policyId, cps, eid, rul)
    const cps2 = await getProtocolParameters(eid)
    const credentialCheckAttest = await runAttest(
      sid,
      policyId2,
      cps2,
      eid,
      credentialRule
    )
    console.log('PcRead', pcReadAttest)
    console.log('credentialCheckl', credentialCheckAttest)
    return credentialCheckAttest
  } catch (error) {
    console.log('error occurred doing multiple attests', error)
  }
}

const runAttest = async (sid, policyId, claimPolicy, eid, rul) => {
  try {
    const body = JSON.stringify({
      eid: eid,
      pid: policyId,
      cps: claimPolicy,
      sid: sid,
    })
    const results = await axios.post(`${url}/attest`, body, headers)
    const verify = await runVerify(results.data.claim, sid, rul)
    console.log('succesfully ran attest', results.data)
    return verify
  } catch (error) {
    console.log('error attesting', error)
  }
}

const runVerify = async (cid, sid, rul) => {
  try {
    const body = JSON.stringify({
      cid: cid,
      rule: rul,
      sid: sid,
    })
    const results = await axios.post(`${url}/verify`, body, headers)
    console.log('succesfully ran verify', results.data)
    return results.data
  } catch (error) {
    console.log('something went wrong running verify', error)
  }
}

const getResult = async (resultId): Promise<{result: number}> => {
  try {
    const results = await axios.get(`${url}/result/${resultId}`)
    console.log('succesfully got results', results.data)
    return results.data.result
  } catch (error) {
    console.log('something wrong with getting the result', error)
    return {result: 3}
  }
}

const startAttestation = async (eid: string) => {
  try {
    sessionId = await openSession()
    const attest = await runAttestAndVerify(sessionId, eid)
    const results = await getResult(attest.result)
    if (results) {

      await closeSession(sessionId)
      return results.result
    }
  } catch (error) {
    console.log('something went wrong running the script', error)
    return 3
  }
}

export default startAttestation
