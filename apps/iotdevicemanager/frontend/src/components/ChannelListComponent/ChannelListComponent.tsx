import React from 'react'
import './ChannelListComponent.css'

interface ChannelListProps {
  componentItems: {
    icon: string
    label: string
  }
}

const ChannelListComponent: React.FC<ChannelListProps> = ({componentItems}) => {
  return (
    <div className="channel-list-component-container">
      <div className="channel-list-component-left">
        <img
          className="channel-list-component-img"
          src={componentItems.icon}
        ></img>
        <h2 className="channel-list-component-label">{componentItems.label}</h2>
      </div>
      <div className="channel-list-component-right">
        <p> {'>'} </p>
      </div>
    </div>
  )
}

export default ChannelListComponent
