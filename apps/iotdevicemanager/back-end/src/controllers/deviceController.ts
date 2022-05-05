import startAttestation from '../a10/a10services'
import {updateAttestation, updateMongoDevice} from '../mongo/mongoServices'
import {Device} from '../schemas/device'
import {SensorData} from '../schemas/sensorData'
import {IDevice} from '../types/deviceType'
import {ISensorData} from '../types/sensorDataType'

/** Return list of all the devices from MongoDB */
export const getDevices = async (req, res) => {
  try {
    const devices = await Device.find({})
    res.status(200).json(devices)
  } catch (error) {
    console.log(error)
    res.status(400)
  }
}

export const getDevice = async (req, res) => {
  try {
    const device = await Device.findById(req.params.id)
    res.status(200).json(device)
  } catch (error) {
    res.status(404)
    throw new Error('Device not found!')
  }
}

export const getDeviceSensorData = async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*')
  try {
    const sensorData = (await SensorData.find({
      deviceId: req.params.id,
    })) as unknown as ISensorData
    res.status(200).json(sensorData)
  } catch (error) {
    res.status(404)
    throw new Error('Data for device not found!')
  }
}

/** Create a new device to MongoDB */
export const setDevice = async (newDevice: IDevice, req, res) => {
  if (!newDevice) {
    res.status(400)
    throw new Error('No device!')
  }
  try {
    const device = await Device.create({
      name: newDevice.name,
      trustedState: newDevice.trustedState,
      channels: newDevice.channels,
      history: newDevice.history,
      sensors: newDevice.sensors,
    })
    res.status(200).json(device)
  } catch (error) {
    console.log(error)
    res.status(400)
  }
}

/** Update a device in MongoDB by given id */
export const updateDevice = async (updatedDevice: IDevice, req, res) => {
  try {
    const device = await Device.findByIdAndUpdate(
      req.params.id,
      updatedDevice,
      {
        new: true,
      }
    )
    res.status(200).json(device)
  } catch (error) {
    console.log(error)
    res.status(400)
  }
}

/** Delete a device in MongoDB by given id */
export const deleteDevice = async (req, res) => {
  try {
    const device = await Device.findByIdAndDelete(req.params.id)
    res.status(200).json(device)
  } catch (error) {
    console.log(error)
    res.status(400)
  }
}

export const getAttestDevice = async (req, res) => {
  try {
    const attestationStatus = await startAttestation(req.params.id)
    const updateHistory = await updateAttestation(
      attestationStatus,
      req.params.id
    ).then(async () => {
      const newDevice = await Device.findById(req.params.id)
      res.status(200).json(newDevice)
    })
  } catch (error) {
    console.log(error)
    res.status(400)
  }
}
