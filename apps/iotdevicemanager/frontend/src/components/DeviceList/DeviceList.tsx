/* eslint-disable operator-linebreak */
/* eslint-disable indent */
import React, {useEffect, useState} from 'react'
import {Link} from 'react-router-dom'
import type {IDevice} from '../../types/deviceType'
import DeviceListComponent from '../DeviceListComponent/DeviceListComponent'
import StatusRedIcon from '../../assets/icons/status_red.svg'
import StatusGreenIcon from '../../assets/icons/status_green.svg'
import StatusYellowIcon from '../../assets/icons/status_yellow.svg'
import './DeviceList.css'

interface ListProps {
  devices: IDevice[]
  selectedChannel: string
  selectedStatus: string
}

const DeviceList: React.FC<ListProps> = ({
  devices,
  selectedChannel,
  selectedStatus,
}) => {
  const [statusFilter, setStatusFilter] = useState<number | null>(null)

  useEffect(() => {
    //  Simple solution for receiving data that we will get from the Nokia Attestation later on
    if (selectedStatus == 'All Devices') {
      setStatusFilter(null)
    }
    if (selectedStatus == 'Trusted') {
      setStatusFilter(0)
    }
    if (selectedStatus == 'Offline') {
      setStatusFilter(2)
    }
    if (selectedStatus == 'Untrusted') {
      setStatusFilter(3)
    }
  }, [selectedStatus])

  const mappedDevicesByChannel = devices.map((device) => {
    if (
      // eslint-disable-next-line operator-linebreak
      device.channels?.includes(selectedChannel) ||
      selectedChannel == 'All Channels'
    ) {
      return device
    }
  })

  const mappedDevicesByState = devices.map((device) => {
    if (
      device.trustedState != 0 &&
      device.trustedState != 2 &&
      statusFilter == 3
    ) {
      return device
    }
    if (device.trustedState == statusFilter || statusFilter == null) {
      return device
    }
  })

  //  Map that contains devices that are in both filtered lists
  const mappedDevices = devices.map((device) => {
    if (
      // eslint-disable-next-line operator-linebreak
      mappedDevicesByChannel.includes(device) &&
      mappedDevicesByState.includes(device)
    ) {
      return (
        <Link
          to={`/${device._id}`}
          key={device._id as number}
          style={{textDecoration: 'none'}}
        >
          <DeviceListComponent
            id={device._id as string}
            key={device._id as number}
            componentItems={{
              icon:
                device.trustedState == 0
                  ? StatusGreenIcon
                  : device.trustedState == 2
                  ? StatusRedIcon
                  : StatusYellowIcon,
              label: device.name || '',
              info:
                device.trustedState == 0
                  ? 'Device is trusted'
                  : device.trustedState == 2
                  ? 'Device is offline'
                  : 'Device is untrusted',
            }}
          />
        </Link>
      )
    }
  })
  return mappedDevices.filter(Boolean).length ? (
    <div className="device-list-container">{mappedDevices}</div>
  ) : (
    <div className="no-device">No devices</div>
  )
}

export default DeviceList
