"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.SensorData = void 0;
const mongoose_1 = require("mongoose");
const sensorDataSchema = new mongoose_1.Schema({
    deviceId: String,
    sensorValue: String,
    timestamp: String,
    sensorType: String,
});
exports.SensorData = (0, mongoose_1.model)('Sensor Data', sensorDataSchema);
//# sourceMappingURL=sensorData.js.map