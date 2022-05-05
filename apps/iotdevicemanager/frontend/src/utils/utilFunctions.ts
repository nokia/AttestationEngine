import {IDevice} from '../types/deviceType'
const replaceElement = (element: IDevice, list: IDevice[]): IDevice[] => {
  const newDevices = list
  const singularDevice = list.filter((device) => device._id == element._id)
  const index = newDevices.indexOf(singularDevice[0])
  newDevices[index] = element
  return newDevices
}

export {replaceElement}
