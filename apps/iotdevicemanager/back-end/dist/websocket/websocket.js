"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const ws_1 = __importDefault(require("ws"));
const WebSocketClient = () => {
    const wss = new ws_1.default.Server({ port: 8080 });
    wss.on('connection', (ws) => {
        console.log('new client connected');
        ws.on('message', (data) => {
            console.log(`Client has sent us: ${data}`);
        });
        ws.on('close', () => {
            console.log('the client has connected');
        });
        ws.onerror = function () {
            console.log('Some Error occurred');
        };
    });
    return { wss };
};
exports.default = WebSocketClient;
//# sourceMappingURL=websocket.js.map