"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.headers = exports.cps = exports.policyId2 = exports.policyId = exports.a10RestApi = exports.originalTopic = exports.clientId = exports.connectUrl = void 0;
const host = 'test.mosquitto.org';
const mqttPort = '1883';
const clientId = `mqtt_${Math.random().toString(16).slice(3)}`;
exports.clientId = clientId;
const originalTopic = 'ANNOUNCEMENT';
exports.originalTopic = originalTopic;
const connectUrl = `mqtt://${host}:${mqttPort}`;
exports.connectUrl = connectUrl;
const a10RestApi = 'http://194.157.71.11:8520/v2';
exports.a10RestApi = a10RestApi;
//attestation a10 related variables
//PCread
const policyId = 'a2518bfc-0fd7-4a2e-a897-7de913616335';
exports.policyId = policyId;
//credential check
const policyId2 = '0b3dfe0d-28f1-4b1f-9013-40429654eae4';
exports.policyId2 = policyId2;
const cps = {
    a10_tpm_send_ssl: {
        key: '/var/attestation/iotpis',
        timeout: 20,
        username: 'pi',
    },
};
exports.cps = cps;
const headers = { headers: { 'Content-Type': 'application/json' } };
exports.headers = headers;
//# sourceMappingURL=GlobalVariables.js.map