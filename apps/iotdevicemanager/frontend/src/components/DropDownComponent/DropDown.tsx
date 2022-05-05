import React, {useEffect, useRef, useState} from 'react'
import './DropDown.css'

interface DropDownProps {
  elements: string[]
  selectedElement: string
  setSelectedElement: React.Dispatch<React.SetStateAction<string>>
  initialElement: string
}

const DropDown: React.FC<DropDownProps> = ({
  elements,
  selectedElement,
  setSelectedElement,
  initialElement,
}) => {
  const [isDropDownOpen, setIsDropDownOpen] = useState(false)
  const [arrowClass, setArrowClass] = useState('arrow-container')
  const [selectedElementIndex, setSelectedElementIndex] = useState(0)
  const [dropDownContentWidth, setDropDownContentWidth] = useState(0)

  const dropDownRef = useRef<HTMLDivElement>(null)

  const handleClick = () => {
    setIsDropDownOpen(!isDropDownOpen)
    if (
      // eslint-disable-next-line operator-linebreak
      arrowClass == 'arrow-container' ||
      arrowClass == 'arrow-class animate-reset-dd'
    ) {
      setArrowClass('arrow-container animate-dd')
    } else if (arrowClass == 'arrow-container animate-dd') {
      setArrowClass('arrow-class animate-reset-dd')
    } else {
      setArrowClass('arrow-container')
    }
  }

  const handleChange = (element: string, index: number) => {
    setSelectedElement(element)
    setSelectedElementIndex(index)
    handleClick()
  }

  //  Sets the dropdown's open list width to match the initial element
  //  as it is positioned as absolute and doesn't have a parent element to match the width to
  //  Does the resizing on first mount and when window size is altered
  useEffect(() => {
    setDropDownContentWidth(dropDownRef.current?.offsetWidth as number)
    window.addEventListener('resize', handleResize)
  }, [])

  const handleResize = () => {
    setDropDownContentWidth(dropDownRef.current?.offsetWidth as number)
  }

  return (
    <div ref={dropDownRef} className="dropdown-wrapper">
      <button
        className={isDropDownOpen ? 'dropdown-button open' : 'dropdown-button'}
        onClick={handleClick}
      >
        <div className="dropdown-button-container">
          <div className="dropdown-button-text">{selectedElement}</div>{' '}
          <div className={arrowClass}>
            <i className="arrow"></i>
          </div>
        </div>
      </button>
      <div
        className={
          isDropDownOpen
            ? 'dropdown-list-content-active'
            : 'dropdown-list-content'
        }
        style={{width: dropDownContentWidth}}
      >
        <button
          className={
            selectedElement == initialElement
              ? 'dropdown-list-element active'
              : 'dropdown-list-element'
          }
          onClick={() => {
            handleChange(initialElement, 0)
          }}
        >
          {initialElement}
        </button>
        {elements.map((element, index) => (
          <button
            className={
              selectedElementIndex == index && selectedElement != initialElement
                ? 'dropdown-list-element active'
                : 'dropdown-list-element'
            }
            key={index}
            onClick={() => {
              handleChange(element.toString(), index)
            }}
          >
            {element}
          </button>
        ))}
      </div>
    </div>
  )
}

export default DropDown
