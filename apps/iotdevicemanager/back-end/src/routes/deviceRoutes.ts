import {getAttestDevice} from '../controllers/deviceController'
import express from 'express'
import {
  getDevices,
  setDevice,
  updateDevice,
  deleteDevice,
  getDevice,
  getDeviceSensorData,
} from '../controllers/deviceController'

export const router = express.Router()

router.route('/').get(getDevices).post(setDevice)

router.route('/:id').delete(deleteDevice).put(updateDevice).get(getDevice)

router.route('/sensordata/:id').get(getDeviceSensorData)

router.route('/attest/:id').get(getAttestDevice)
