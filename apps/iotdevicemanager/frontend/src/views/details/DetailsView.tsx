import React, {useEffect, useState} from 'react'
import './DetailsView.css'
import {useDevices} from '../../hooks/ApiHooks'
import {IDevice} from '../../types/deviceType'
import DetailComponent from '../../components/DetailComponent/DetailComponent'
import StatusRedIcon from '../../assets/icons/status_red.svg'
import StatusGreenIcon from '../../assets/icons/status_green.svg'
import StatusYellowIcon from '../../assets/icons/status_yellow.svg'
import DataGraph from '../../components/DataGraph/DataGraph'
import {Link, useLocation} from 'react-router-dom'

const DetailsView: React.FC = () => {
  const {fetchDevice, attestDevice} = useDevices()
  const [device, setDevice] = useState<IDevice>()
  const [statusIcon, setStatusIcon] = useState('')
  const [selectedData, setSelectedData] = useState('')
  const [sortableData, setSortableData] = useState<string[]>()
  const location = useLocation()
  const id = location.pathname.substring(1)

  useEffect(() => {
    const fetch = async () => {
      try {
        const sorted = ['attestation history']
        if (id) {
          const fetchedDevice = await fetchDevice(id)
          setDevice(fetchedDevice)
          if (fetchedDevice.channels) {
            fetchedDevice.channels.forEach((channel: string) => {
              sorted.push(channel)
            })
          }
        }
        setSortableData(sorted)
      } catch (error) {
        console.log(error)
      }
    }
    fetch()
  }, [])

  useEffect(() => {
    if (sortableData) {
      setSelectedData(sortableData[0])
    }
  }, [sortableData])

  useEffect(() => {
    if (device) {
      if (device.trustedState) {
        if (device.trustedState == 0) {
          setStatusIcon(StatusGreenIcon)
        }
        if (device.trustedState == 2) {
          setStatusIcon(StatusRedIcon)
        }
        if (device.trustedState != 2 && device.trustedState != 0) {
          setStatusIcon(StatusYellowIcon)
        }
      }
    }
  }, [device?.trustedState])

  const setSelectedGraphData = (dataLabel: string) => {
    setSelectedData(dataLabel)
  }

  const attest = async () => {
    if (
      window.confirm('Are you sure you want to manually attest the device?')
    ) {
      const newDevice = await attestDevice(id)
      if (newDevice) {
        setDevice(newDevice)
      }
    }
  }

  return (
    <div className="details-content-container">
      <div className="details-view-header">
        <div className="device-icon-and-title">
          <img className="details-view-status-img" src={statusIcon}></img>
          <h1>{device?.name}</h1>
        </div>
      </div>
      <div className="breadcrumb-container">
        <Link to={`/`} className="device-details-title">
          <button className="breadcrumb-button">{'< Back to devices'}</button>
        </Link>
        <div className="breadcrumb-button" onClick={attest}>
          Attest manually
        </div>
      </div>
      <div className="details-view-body">
        <div className="details-view-graph-container">
          <div className="graph-wrapper">
            <DataGraph
              dataGraphItems={{
                selectedData: selectedData,
                id: id,
              }}
            />
          </div>
        </div>
      </div>
      <div className="details-view-component-container">
        {sortableData ? (
          sortableData.map((channel, i) => (
            <DetailComponent
              key={i}
              componentItems={{
                label: channel,
              }}
              clickHandler={setSelectedGraphData}
              selectedChannel={selectedData}
            />
          ))
        ) : (
          <></>
        )}
      </div>
    </div>
  )
}
export default DetailsView
