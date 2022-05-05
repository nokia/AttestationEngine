import {IDevice} from "./deviceType";

export interface IChannel {
  name: string,
  devices?: IDevice[],
  _id?: object
}
