export interface INotification {
  _id: string
  deviceId: string
  deviceName: string
  deviceChannels: string[]
  timestamp: string
  title: string
  status: number
}
