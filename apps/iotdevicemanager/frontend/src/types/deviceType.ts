import {IDeviceHistory} from './deviceHistoryType'

export interface IDevice {
  name?: string
  trustedState?: number
  channels?: string[]
  history?: IDeviceHistory[]
  sensors?: string[]
  _id?: Object
}
