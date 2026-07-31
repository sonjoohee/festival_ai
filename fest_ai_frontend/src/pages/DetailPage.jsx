import { useEffect, useState, useRef } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import Header from '../components/Header.jsx';
import { getFestivalDetail } from '../api/festivalsApi';
import {
  createBookmark,
  deleteBookmark,
} from '../api/bookmarksApi';
import FestivalDetailInfo from '../features/festivals/FestivalDetailInfo.jsx';
import AiSummaryBox from '../features/bookmarks/AiSummaryBox.jsx';
import BookmarkButton from '../features/bookmarks/BookmarkButton.jsx';


const DetailPage = () => {
  const { festivalId } = useParams();
  const [festival, setFestival] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isToggling, setIsToggling] = useState(false);
  

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [memo, setMemo] = useState('');
  const isMounted = useRef(false); 

  const fetchPageData = async () => {
    setIsLoading(true);
    try {
      const response = await getFestivalDetail(festivalId);
      console.log('Festival detail:', response.data);
      setFestival(response.data);
    } catch (err) {
      console.error('Failed to fetch festival detail:', err);
      setFestival(null); 
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {

    if (!isMounted.current) {
      fetchPageData();
      isMounted.current = true;
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [festivalId]);

  const handleBookmarkClick = async () => {
    if (isToggling || !festival) return;

    if (festival.bookmarked) {
  
      const targetBookmarkId = festival.bookmarkId;
      
      // console.log('실제 백엔드로 보낼 북마크 ID:', targetBookmarkId);
      
      if (!targetBookmarkId) {
        alert('북마크 ID가 없어 해제할 수 없습니다. 페이지를 새로고침 후 다시 시도해주세요.');
        return;
      }

      // setIsToggling(true);
      try {
        await deleteBookmark(targetBookmarkId);
        setFestival((prev) => ({ 
          ...prev, 
          bookmarked: false, 
          bookmarkId: null 
        }));
      } catch (err) {
        console.error('찜 해제 실패:', err);
        alert('찜 해제에 실패했습니다. 다시 시도해 주세요.');
      } finally {
        setIsToggling(false);
      }
    } else {
      setIsModalOpen(true);
    }
  };

  const handleModalSubmit = async () => {
    setIsToggling(true);
    try {
      await createBookmark({
        contentId: festival.contentId,
        title: festival.title,
        region: festival.region,
        imageUrl: festival.imageUrl,
        content: festival.content,
        aiInfo: festival.aiInfo,
        userMemo: memo, 
        addr: festival.addr || ''
      });
  
      await fetchPageData();
    } catch (err) {

      alert('찜하기에 실패했습니다. 입력 내용을 확인하거나 잠시 후 다시 시도해주세요.');
      console.error('찜하기 실패:', err);
    } finally {
      setIsModalOpen(false); 
      setMemo(''); 
      setIsToggling(false);
    }
  };

  return (
    <>
      <Header />
      <PageWrapper>
        <PageInner>
          <DetailLayout>
            <LeftColumn> 
              <FestivalDetailInfo festival={festival} loading={isLoading} />
            </LeftColumn>
            <RightColumn>
              <AiSummaryBox
                summary={festival?.aiInfo?.split('\n') || []}
                loading={isLoading}
              />
              <BookmarkSection>
                <BookmarkButtonWrapper
                  isBookmarked={festival?.bookmarked || false}
                  loading={isToggling}
                  onToggle={handleBookmarkClick}
                />
              </BookmarkSection>
            </RightColumn>
          </DetailLayout>
        </PageInner>
      </PageWrapper>


      {isModalOpen && (
        <ModalOverlay>
          <ModalContent>
            <h3>이 축제가 마음에 드시나요?</h3>
            <p style={{ fontSize: '0.9rem', color: '#666', marginBottom: '10px' }}>
              마이페이지에 함께 저장될 나만의 여행 계획이나 팁을 자유롭게 남겨보세요!
            </p>
            <MemoWrapper>
              <MemoInput 
                placeholder="예: 아이들 데리고 토요일 아침 일찍 출발하기" 
                value={memo}
               onChange={(e) => setMemo(e.target.value.substring(0, 255))}
              />
              <CharCounter $isError={memo.length >= 255}>{memo.length} / 255</CharCounter>
            </MemoWrapper>
            <ButtonGroup>
              <ModalButton onClick={() => setIsModalOpen(false)}>취소</ModalButton>
              <ModalButton $primary onClick={handleModalSubmit}>
                {isToggling ? '저장 중...' : '찜하고 메모 저장하기'}
              </ModalButton>
            </ButtonGroup>
          </ModalContent>
        </ModalOverlay>
      )}
    </>
  );
};

export default DetailPage;

// Styles (기존 스타일 유지)
const PageWrapper = styled.main`
  flex: 1;
  padding: ${({ theme }) => theme.spacing.xl};
`;

const PageInner = styled.div`
  max-width: 1100px;
  margin: 0 auto;
`;

const DetailLayout = styled.div`
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: ${({ theme }) => theme.spacing.xl};
  align-items: start;

  @media (max-width: ${({ theme }) => theme.breakpoints.desktop}) {
    grid-template-columns: 1fr;
  }
`;

const LeftColumn = styled.div`
  display: flex;
  flex-direction: column;
`;

const RightColumn = styled.aside`
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing.xl};
  position: sticky;
  top: calc(${({ theme }) => theme.layout.headerHeight} + ${({ theme }) => theme.spacing.xl});
`;

const BookmarkSection = styled.div` margin-top: auto; `;

const BookmarkButtonWrapper = styled(BookmarkButton)``;


const ModalOverlay = styled.div`
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
`;

const ModalContent = styled.div`
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 450px;
  max-width: 90%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
`;

const MemoWrapper = styled.div`
  position: relative;
  width: 100%;
  margin-bottom: 20px;
`;

const MemoInput = styled.textarea`
  width: 100%;
  height: 120px;
  padding: 15px;
  padding-bottom: 30px; /* Make space for the counter */
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  font-family: inherit;
  font-size: 0.95rem;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #c05c36;
  }
`;

const CharCounter = styled.div`
  position: absolute;
  bottom: 10px;
  right: 15px;
  font-size: 0.8rem;
  color: ${({ theme, $isError }) => $isError ? theme.colors.danger : theme.colors.textLight};
  pointer-events: none;
`;

const ButtonGroup = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 12px;
`;

const ModalButton = styled.button`
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-weight: 500;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
  

  background-color: ${(props) => (props.$primary ? '#c05c36' : '#e9ecef')};
  color: ${(props) => (props.$primary ? 'white' : '#333')};

  &:hover {
    background-color: ${(props) => (props.$primary ? '#a04825' : '#dee2e6')};
  }
`;
