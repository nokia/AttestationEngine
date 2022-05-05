/* eslint-disable indent */
/* eslint-disable operator-linebreak */
import React, {useEffect, useState} from 'react'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'
import {Line} from 'react-chartjs-2'
import {ISensorData} from '../../types/sensorDataType'
import {useDevices} from '../../hooks/ApiHooks'
import {IDevice} from '../../types/deviceType'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

interface DataGraphProps {
  dataGraphItems: {
    selectedData?: string
    id?: string
  }
}

interface statusColors {
  color: string
  timestamp: number
}

const DataGraph: React.FC<DataGraphProps> = ({dataGraphItems}) => {
  const {fetchDeviceData, fetchDevice} = useDevices()
  const [deviceData, setDeviceData] = useState<ISensorData[]>([])
  const [historyColors, setHistoryColors] = useState<statusColors[]>()
  const [device, setDevice] = useState<IDevice>()

  useEffect(() => {
    const fetch = async () => {
      try {
        if (dataGraphItems.id) {
          const fetchedDeviceData = await fetchDeviceData(dataGraphItems.id)
          const fetchedDevice = await fetchDevice(dataGraphItems.id)
          setDevice(fetchedDevice)
          setDeviceData(fetchedDeviceData)
        }
      } catch (error) {
        console.log(error)
      }
    }
    fetch()
  }, [])

  useEffect(() => {
    const mappedHistory = device?.history?.map((device) => {
      let color: string
      if (device.trustedState == 0) {
        color = 'rgb(72, 200, 100)'
      } else if (device.trustedState == 2) {
        color = 'rgb(255, 99, 132)'
      } else {
        color = 'rgb(255, 205, 5)'
      }
      return {
        timestamp: Math.round(device.timestamp),
        color: color,
      }
    }) as statusColors[]
    setHistoryColors(mappedHistory)
  }, [device])

  const options = {
    responsive: true,

    interaction: {
      mode: 'index' as const,
      intersect: false,
    },
    stacked: false,
    plugins: {
      title: {
        display: true,
        text: 'Data Graph',
      },
    },
    scales: {
      y: {
        type: 'linear' as const,
        display: true,
        position: 'left' as const,
      },
      y1: {
        type: 'linear' as const,
        display: true,
        position: 'right' as const,
        grid: {
          drawOnChartArea: false,
        },
      },
      xAxes: {
        ticks: {
          autoSkip: true,
          maxRotation: 0,
          minRotation: 0,
        },
      },
    },
  }

  const getGraphData = (chosenData?: String) => {
    if (chosenData == 'attestation history') {
      const graphData = device?.history?.map((history) => {
        return history.trustedState
      })
      return graphData
    }

    const graphData = deviceData?.map((sensor) => {
      if (sensor.sensorType == chosenData) {
        return sensor.sensorValue
      }
    })
    return graphData
  }

  const parseDate = (timestamp: number) => {
    return new Date(timestamp * 1000).toLocaleString('fi-FI')
  }

  const getGraphDataLabels = (chosenData?: String) => {
    if (chosenData == 'attestation history') {
      const graphDataLabels = device?.history?.map((history) => {
        return parseDate(history.timestamp)
      })
      return graphDataLabels
    }

    const graphDataLabels = deviceData.map((sensor) => {
      if (sensor.sensorType == chosenData) {
        return new Date(Number(sensor.timestamp) * 1000).toLocaleString('fi-FI')
      }
    })
    return graphDataLabels
  }

  const labels = getGraphDataLabels(dataGraphItems.selectedData)

  const getDateStamps = () => {
    const array = deviceData.map((sensor) => {
      if (sensor.sensorType == dataGraphItems.selectedData) {
        return parseInt(sensor.timestamp)
      }
    })
    return array
  }

  const checkNumbers = (lower: number, upper: number, data: number) => {
    const isInRange = (value: number) => {
      return value >= lower && value <= upper
    }
    const val = isInRange(data)
    if (val && historyColors) {
      const color = historyColors.filter((el) => {
        return el.timestamp == lower
      })
      return color[0].color
    }
    return val
  }

  const trusted = (ctx: any): string => {
    if (dataGraphItems.selectedData == 'attestation history') {
      if (historyColors) {
        return historyColors[ctx.p1.parsed.x].color
      }
    } else {
      const timestamps = getDateStamps()
      const color = historyColors?.filter((e, i) => {
        if (timestamps) {
          if (historyColors[i + 1]) {
            const value = checkNumbers(
              e.timestamp,
              historyColors[i + 1].timestamp,
              timestamps[ctx.p1.parsed.x] || 0
            )
            if (value) {
              return value
            }
          } else {
            const value = checkNumbers(
              e.timestamp,
              timestamps[ctx.p1.parsed.x] || 0,
              timestamps[ctx.p1.parsed.x] || 0
            )
            if (value) {
              return value
            }
          }
        }
      })
      if (color) {
        return color[0].color
      }
    }

    return 'rgb(255, 99, 132)'
  }

  const data = {
    labels,
    datasets: [
      {
        label: dataGraphItems.selectedData,
        data: getGraphData(dataGraphItems.selectedData),
        spanGaps: true,
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.5)',
        yAxisID: 'y',
        segment: {
          borderColor: (ctx: any) => trusted(ctx),
        },
      },
    ],
  }

  return <Line options={options} data={data} />
}

export default DataGraph
