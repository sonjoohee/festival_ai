import { useEffect, useState } from 'react';
import styled from 'styled-components';
import { IoMdHeart, IoIosHeartEmpty } from 'react-icons/io'; 

const BookmarkButton = ({ isBookmarked, loading, onToggle }) => {
  const [active, setActive] = useState(isBookmarked);

  useEffect(() => {
    setActive(isBookmarked);
  }, [isBookmarked]);

  const handleClick = () => {
    if (loading) return;
    onToggle?.();
  };

  return (
    <BookmarkBtn 
      $active={active} 
      onClick={handleClick} 
      disabled={loading}
    >
      {loading ? (
        '저장 중...'
      ) : active ? (
        <>
          <IoMdHeart size={18} /> 찜 해제
        </>
      ) : (
        <>
          <IoIosHeartEmpty size={18} /> 찜 하기
        </>
      )}
    </BookmarkBtn>
  );
};

export default BookmarkButton;



const BookmarkBtn = styled.button`
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  min-width: 240px;
  padding: 14px 24px;
  font-size: 16px;
  font-weight: 600;
  
  border-radius: 100px; 
  cursor: pointer;
  transition: all 0.2s ease;


  ${({ $active }) =>
    $active
      ? `
    
        color: #ffffff;
        background-color: #d76d38;
        border: none;
        box-shadow: 0 4px 14px rgba(215, 109, 56, 0.2);
        
        &:hover {
          background-color: #c25e2e;
        }
      `
      : `
    
        color: #c92a2a;
        background-color: #fff0f0;
        border: 1px solid #ffc9c9;
        box-shadow: none;

        &:hover {
          background-color: #ffe3e3;
        }
      `}

  &:disabled {
    opacity: 0.8;
    cursor: not-allowed;
    &:hover {
      background-color: ${({ $active }) => ($active ? '#d76d38' : '#fff0f0')};
    }
  }
`;