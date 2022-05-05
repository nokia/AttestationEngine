"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.originalTopic = exports.clientId = exports.connectUrl = void 0;
const host = 'test.mosquitto.org';
const mqttPort = '1883';
const clientId = `mqtt_${Math.random().toString(16).slice(3)}`;
exports.clientId = clientId;
const originalTopic = 'ANNOUNCEMENTS';
exports.originalTopic = originalTopic;
const connectUrl = `mqtt://${host}:${mqttPort}`;
exports.connectUrl = connectUrl;
//# sourceMappingURL=GlobalVariables.js.map