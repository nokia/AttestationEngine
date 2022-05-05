import {ISensorData} from './sensorDataType'

export interface IDeviceSensor {
  sensorType: string
  sensorData?: [
    {
      sensorValue: string
      timestamp: string
    }
  ]
  _id?: Object
}
