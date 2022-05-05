import { IDevice } from "./deviceType";

export interface ISubscribedChannel {
    name: string,
    devices: IDevice[]
}