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
Object.defineProperty(exports, "__esModule", { value: true });
exports.deleteChannel = exports.updateChannel = exports.setChannel = exports.getChannels = void 0;
const SubscribedChannel_1 = require("../schemas/SubscribedChannel");
/** Return list of all the subscribed channels from MongoDB */
const getChannels = (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const channels = yield SubscribedChannel_1.SubscribedChannel.find({});
        res.status(200).JSON(channels);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.getChannels = getChannels;
/** Create a new channel to MongoDB */
const setChannel = (newChannel, req, res) => __awaiter(void 0, void 0, void 0, function* () {
    if (!newChannel) {
        res.status(400);
        throw new Error('No channel!');
    }
    try {
        const device = yield SubscribedChannel_1.SubscribedChannel.create({
            name: newChannel.name,
            devices: newChannel.devices
        });
        res.status(200).JSON(device);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.setChannel = setChannel;
/** Update a channel in MongoDB by given id */
const updateChannel = (updatedChannel, req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const channel = yield SubscribedChannel_1.SubscribedChannel.findByIdAndUpdate(req.params.id, updatedChannel, {
            new: true,
        });
        res.status(200).JSON(channel);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.updateChannel = updateChannel;
/** Delete a channel in MongoDB by given id */
const deleteChannel = (req, res) => __awaiter(void 0, void 0, void 0, function* () {
    try {
        const channel = yield SubscribedChannel_1.SubscribedChannel.findByIdAndDelete(req.params.id);
        res.status(200).JSON(channel);
    }
    catch (error) {
        console.log(error);
        res.status(400);
    }
});
exports.deleteChannel = deleteChannel;
//# sourceMappingURL=channelController.js.map