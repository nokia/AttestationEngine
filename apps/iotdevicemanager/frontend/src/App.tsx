import React from 'react'
import './App.css'
import {NotificationProvider} from './contexts/NotificationContext'
import BaseView from './views/base/BaseView'

const App: React.FC = () => {
  return (
    <div className="App">
      <NotificationProvider>
        <BaseView />
      </NotificationProvider>
    </div>
  )
}

export default App
