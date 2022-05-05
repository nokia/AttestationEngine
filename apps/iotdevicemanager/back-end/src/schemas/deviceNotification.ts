import {Schema, model} from 'mongoose'
import { IDeviceNotification } from '../types/deviceNotificationType'

const deviceNotificationSchema = new Schema<IDeviceNotification>({
  deviceId: String,
  deviceName: String,
  deviceChannels: [String],
  timestamp: String,
  status: Number,
})

export const DeviceNotification = model<IDeviceNotification>('Device Notification', deviceNotificationSchema)
