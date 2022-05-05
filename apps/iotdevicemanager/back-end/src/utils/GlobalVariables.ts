const host = 'test.mosquitto.org'
const mqttPort = '1883'
const clientId = `mqtt_${Math.random().toString(16).slice(3)}`
const originalTopic = 'ANNOUNCEMENTS'
const connectUrl = `mqtt://${host}:${mqttPort}`


const a10RestApi = '' // ip address here
//attestation a10 related variables

//PCread
const policyId = 'a2518bfc-0fd7-4a2e-a897-7de913616335'
//credential check
const policyId2 = '0b3dfe0d-28f1-4b1f-9013-40429654eae4'
const cps = {
  a10_tpm_send_ssl: {
    key: '/var/attestation/iotpis',
    timeout: 20,
    username: 'pi',
  },
}
const headers = {headers: {'Content-Type': 'application/json'}}
const rul = [
  'tpm2rules/PCRsAllUnassigned',
  {
    bank: 'sha256',
  },
]

const credentialRule = ['tpm2rules/TPM2CredentialVerify', {}]

export {connectUrl, clientId, originalTopic, a10RestApi, policyId, policyId2, cps, headers, rul, credentialRule}
