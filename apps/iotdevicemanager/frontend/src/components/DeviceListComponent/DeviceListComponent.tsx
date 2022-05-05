import React from 'react'
import './DeviceListComponent.css'

interface DeviceListProps {
  id: string
  componentItems: {
    icon: string
    label: string
    info: string
  }
}

const DeviceListComponent: React.FC<DeviceListProps> = ({
  componentItems,
  id,
}) => {
  return (
    <div className="device-list-component-container">
      <div className="device-list-component-left">
        <img
          className="device-list-component-img"
          src={componentItems.icon}
        ></img>
        <div className="device-list-component-textcontainer">
          <h2 className="device-list-component-label">
            {componentItems.label}
          </h2>
          <p className="device-list-component-info">{componentItems.info}</p>
        </div>
      </div>
      <div className="device-list-component-right">
        <p> {'>'} </p>
      </div>
    </div>
  )
}

export default DeviceListComponent
