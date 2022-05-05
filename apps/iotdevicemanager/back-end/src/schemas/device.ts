import {Schema, model} from 'mongoose'
import {IDevice} from '../types/deviceType'

const deviceSchema = new Schema<IDevice>({
  name: String,
  _id: String,
  trustedState: Number,
  channels: [String],
  history: [
    {
      name: String,
      timestamp: String,
      trustedState: Number,
    },
  ],
  sensors: [String],
})

export const Device = model<IDevice>('Device', deviceSchema)
