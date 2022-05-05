"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const GlobalVariables_1 = require("../utils/GlobalVariables");
const axios_1 = __importDefault(require("axios"));
const url = GlobalVariables_1.a10RestApi;
let sessionId = '';
const getElement = (eid) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const result = yield axios_1.default.get(`${url}/element/${eid}`);
        return result.data;
    }
    catch (error) {
        console.log('error occurred fetching an element');
    }
});
const getProtocolParameters = (eid) => __awaiter(void 0, void 0, void 0, function* () {
    const element = yield getElement(eid);
    return {
        a10_tpm_send_ssl: {
            key: '/var/attestation/iotpis',
            timeout: 20,
            username: 'pi',
        },
        akname: element.tpm2.tpm0.akname,
        ekpub: element.tpm2.tpm0.ekpem,
    };
});
const openSession = () => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const results = yield axios_1.default.post(`${url}/sessions/open`);
        console.log('succesfully opened a session', results.data.itemid);
        return results.data.itemid;
    }
    catch (error) {
        console.log('error opening a session', error);
    }
});
const closeSession = (sid) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const results = yield axios_1.default.delete(`${url}/session/${sid}`);
        console.log('succesfully closed session', results.data);
    }
    catch (error) {
        console.log('error closing down the session', error);
    }
});
const runAttestAndVerify = (sid, eid) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        console.log('first eid', eid);
        const pcReadAttest = yield runAttest(sid, GlobalVariables_1.policyId, GlobalVariables_1.cps, eid);
        const cps2 = yield getProtocolParameters(eid);
        //const credentialCheckAttest = await runAttest(sid, policyId2, cps2)
        console.log('PcRead', pcReadAttest);
        //console.log('credentialCheckl', credentialCheckAttest)
        return { pcReadAttest: pcReadAttest };
    }
    catch (error) {
        console.log('error occurred doing multiple attests', error);
    }
});
const runAttest = (sid, policyId, claimPolicy, eid) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        console.log('second eid', eid);
        const body = JSON.stringify({
            eid: eid,
            pid: policyId,
            cps: claimPolicy,
            sid: sid,
        });
        console.log('body', {
            eid: eid,
            pid: policyId,
            cps: claimPolicy,
            sid: sid,
        });
        //const results = await axios.post(`${url}/attest`, body, headers)
        // const verify = await runVerify(results.data.claim, sid)
        //console.log('succesfully ran attest', results.data)
        //return verify
    }
    catch (error) {
        console.log('error attesting', error);
    }
});
const runVerify = (cid, sid) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const rul = [
            'tpm2rules/PCRsAllUnassigned',
            {
                bank: 'sha256',
            },
        ];
        const body = JSON.stringify({
            cid: cid,
            rule: rul,
            sid: sid,
        });
        const results = yield axios_1.default.post(`${url}/verify`, body, GlobalVariables_1.headers);
        console.log('succesfully ran verify', results.data);
        return results.data;
    }
    catch (error) {
        console.log('something went wrong running verify', error);
    }
});
const getResult = (resultId) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const results = yield axios_1.default.get(`${url}/result/${resultId}`);
        console.log('succesfully got results', results.data);
        return results.data.result;
    }
    catch (error) {
        console.log('something wrong with getting the result', error);
        return 1;
    }
});
const startAttestation = (eid) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        sessionId = yield openSession();
        const attest = yield runAttestAndVerify(sessionId, eid);
        const results = yield getResult(attest.pcReadAttest);
        if (results) {
            yield closeSession(sessionId);
            return results;
        }
        return;
    }
    catch (error) {
        console.log('something went wrong running the script', error);
    }
});
exports.default = startAttestation;
//# sourceMappingURL=a10services.js.map