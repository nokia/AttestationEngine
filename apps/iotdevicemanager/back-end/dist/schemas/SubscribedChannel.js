"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.SubscribedChannel = void 0;
const mongoose_1 = require("mongoose");
const subscribedChannelSchema = new mongoose_1.Schema({
    name: String,
    devices: [{}]
});
exports.SubscribedChannel = (0, mongoose_1.model)("Subscribed Channel", subscribedChannelSchema);
//# sourceMappingURL=subscribedChannel.js.map