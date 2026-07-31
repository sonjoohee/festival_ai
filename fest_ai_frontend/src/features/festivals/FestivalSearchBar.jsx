import { useState, useRef, useEffect } from 'react';
import styled from 'styled-components';
import { REGION_CODES } from '../../utils/regionMapper.js';
import { FiChevronDown } from 'react-icons/fi';

const FestivalSearchBar = ({ onSearch, initialRegion = '' }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedRegion, setSelectedRegion] = useState(initialRegion);
  const dropdownRef = useRef(null);

  // 현재 선택된 지역의 이름 찾기
  let selectedLabel = '지역을 선택하세요';
  if (selectedRegion === 'all') {
    selectedLabel = '전체 지역';
  } else if (REGION_CODES[selectedRegion]) {
    selectedLabel = REGION_CODES[selectedRegion];
  } else if (selectedRegion === '') {
    selectedLabel = '지역을 선택하세요'; 
  }

  const handleToggle = () => setIsOpen(!isOpen);

  const handleSelect = (code) => {
    setSelectedRegion(code);
    setIsOpen(false);
    onSearch({ region: code });
  };


  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  return (
    <SelectWrapper ref={dropdownRef}>
      {/* 🔲 커스텀 셀렉트 버튼 (보여지는 상자) */}
      <SelectButton onClick={handleToggle} $isOpen={isOpen}>
        <span>{selectedLabel}</span>
        <FiChevronDown className="arrow-icon" />
      </SelectButton>

      {/* 📜 커스텀 옵션 리스트 (메뉴판) */}
      {isOpen && (
        <OptionList>
     
          <OptionItem
            $isSelected={selectedRegion === 'all'}
            onClick={() => handleSelect('all')}
          >
            전체 지역
          </OptionItem>
          {Object.entries(REGION_CODES).map(([code, name]) => (
            <OptionItem
              key={code}
              $isSelected={selectedRegion === code}
              onClick={() => handleSelect(code)}
            >
              {name}
            </OptionItem>
          ))}
        </OptionList>
      )}
    </SelectWrapper>
  );
};

export default FestivalSearchBar;

// Styles
const SelectWrapper = styled.div`
  width: 100%;
  max-width: 550px;
  margin: 0 auto;
  position: relative;
  font-family: inherit;
`;

const SelectButton = styled.div`
  width: 100%;
  padding: 14px 24px;
  font-size: 1rem;
  font-weight: 500;
  color: #333333;
  background-color: #ffffff;
  

  border: 1px solid ${({ $isOpen }) => ($isOpen ? '#c05c36' : '#e0e0e0')};
  border-radius: 100px; 
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s ease-in-out;

  &:hover {
    border-color: #c05c36;
  }


  .arrow-icon {
    font-size: 1.25rem;
    color: #666666;
    transition: transform 0.2s ease-in-out;
    transform: ${({ $isOpen }) => ($isOpen ? 'rotate(180deg)' : 'rotate(0deg)')};
  }
`;


const OptionList = styled.ul`
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 100%;
  max-height: 280px;
  overflow-y: auto;
  
  background-color: #ffffff;
  border: 1px solid #eaeaea;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  margin: 0;
  list-style: none;
  z-index: 100;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background-color: #dddddd;
    border-radius: 4px;
  }
`;


const OptionItem = styled.li`
  padding: 12px 24px;
  font-size: 0.95rem;
  font-weight: ${({ $isSelected }) => ($isSelected ? '600' : '500')};

  color: ${({ $isSelected }) => ($isSelected ? '#c05c36' : '#444444')};
  background-color: ${({ $isSelected }) => ($isSelected ? '#fdf0e9' : 'transparent')};
  
  cursor: pointer;
  transition: all 0.15s ease-in-out;

  &:hover {
    color: #c05c36;
    background-color: #fdf0e9;
  }
`;