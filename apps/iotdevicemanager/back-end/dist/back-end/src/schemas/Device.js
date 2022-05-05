"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Device = void 0;
const mongoose_1 = require("mongoose");
const deviceSchema = new mongoose_1.Schema({
    name: String,
    _id: String,
    trustedState: Number,
    channels: [String],
    history: [
        {
            name: String,
            timestamp: String,
            trustedState: Number,
        },
    ],
    sensors: [
        {
            sensorType: String,
            sensorValue: String,
            timestamp: String,
        },
    ],
});
exports.Device = (0, mongoose_1.model)('Device', deviceSchema);
//# sourceMappingURL=Device.js.map