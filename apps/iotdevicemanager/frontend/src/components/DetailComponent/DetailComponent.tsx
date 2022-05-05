/* eslint-disable indent */
import React from 'react'
import './DetailComponent.css'
import AttestationHistory from '../../assets/icons/attestation-history.svg'
import Temperature from '../../assets/icons/temperature.svg'
import Humidity from '../../assets/icons/humidity.svg'
import Light from '../../assets/icons/light.svg'
import Noise from '../../assets/icons/noise.svg'
import ChannelIcon from '../../assets/icons/channel_icon.svg'

interface DetailProps {
  componentItems: {
    label: string
  }
  clickHandler: Function
  selectedChannel: string
}

const DetailComponent: React.FC<DetailProps> = ({
  componentItems,
  clickHandler,
  selectedChannel,
}) => {
  //  Simple solution to handling icons based on label
  const checkComponentIcon = (componentItem: string) => {
    switch (componentItem) {
      case 'attestation history':
        return AttestationHistory
      case 'temperature':
        return Temperature
      case 'humidity':
        return Humidity
      case 'light':
        return Light
      case 'noise':
        return Noise
      default:
        return ChannelIcon
    }
  }
  return (
    <div
      className={
        selectedChannel == componentItems.label
          ? 'detail-component-container selected'
          : 'detail-component-container'
      }
      onClick={() => clickHandler(componentItems.label)}
    >
      <img
        className="detail-component-icon"
        src={checkComponentIcon(componentItems.label)}
      />
      <h2 className="detail-component-label">{componentItems.label}</h2>
    </div>
  )
}

export default DetailComponent
