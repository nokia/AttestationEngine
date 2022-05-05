import React, {useState} from 'react'
import DeviceList from '../../components/DeviceList/DeviceList'
import DropDown from '../../components/DropDownComponent/DropDown'
import {useChannels, useDevices} from '../../hooks/ApiHooks'
import './DevicesView.css'

const DeviceView: React.FC = () => {
  const {devices} = useDevices()
  const {channels} = useChannels()

  //  Variables needed for Channel DropDown
  const initialChannel = 'All Channels'
  const [selectedChannel, setSelectedChannel] = useState(initialChannel)

  //  Variables needed for Device DropDown
  const initialStatus = 'All Devices'
  const [selectedStatus, setSelectedStatus] = useState(initialStatus)
  const listOfStatus = ['Trusted', 'Offline', 'Untrusted']
  return (
    <div className="device-list-container">
      <div className="device-list-title">Devices</div>
      <div className="dropdown-base-container">
        <DropDown
          elements={listOfStatus}
          selectedElement={selectedStatus}
          setSelectedElement={setSelectedStatus}
          initialElement={initialStatus}
        />
        <DropDown
          elements={channels}
          selectedElement={selectedChannel}
          setSelectedElement={setSelectedChannel}
          initialElement={initialChannel}
        />
      </div>
      <DeviceList
        devices={devices}
        selectedChannel={selectedChannel}
        selectedStatus={selectedStatus}
      />
    </div>
  )
}

export default DeviceView
