"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.DeviceNotification = void 0;
const mongoose_1 = require("mongoose");
const deviceNotificationSchema = new mongoose_1.Schema({
    deviceId: String,
    deviceName: String,
    deviceChannels: [String],
    timestamp: String,
    status: Number,
});
exports.DeviceNotification = (0, mongoose_1.model)('Device Notification', deviceNotificationSchema);
//# sourceMappingURL=deviceNotification.js.map