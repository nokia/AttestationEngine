"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.router = void 0;
const express_1 = __importDefault(require("express"));
const channelController_1 = require("../controllers/channelController");
exports.router = express_1.default.Router();
exports.router.route('/')
    .get(channelController_1.getChannels)
    .post(channelController_1.setChannel);
exports.router.route('/:id')
    .delete(channelController_1.deleteChannel)
    .put(channelController_1.updateChannel);
//# sourceMappingURL=channelRoutes.js.map