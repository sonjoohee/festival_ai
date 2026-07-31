import { useEffect, useState } from 'react';
import styled from 'styled-components';
import { IoMdHeart } from 'react-icons/io';
import Header from '../components/Header.jsx';
import { getBookmarks } from '../api/bookmarksApi';
import BookmarkCard from '../features/bookmarks/BookmarkCard.jsx';



const MyPage = () => {

  const [bookmarks, setBookmarks] = useState([]);

  useEffect(() => {
  const fetchBookmarks = async () => {
    try {
      const response = await getBookmarks();
      // console.log("북마크 전체 응답 구조:", response);

      if (response.data && response.data.data) {
        setBookmarks(response.data.data);
      } else {
        setBookmarks([]);
      }
      
    } catch (err) {
      console.error("Failed to fetch bookmarks:", err);
    } finally {
    }
  };

  fetchBookmarks();
}, []);

  const handleUpdate = (bookmarkId, updatedData) => {
    setBookmarks((prev) => 
      prev.map((item) => 
        item.bookmarkId === bookmarkId ? { ...item, ...updatedData } : item
      )
    );
  };

  const handleDelete = (bookmarkId) => {
    setBookmarks((prev) => prev.filter((item) => item.bookmarkId !== bookmarkId));
  };


  return (
    <>
      <Header />
      <PageWrapper>
        <PageInner>
          <TitleContainer>
            <TitleLeft>
              <SubTitle>나의 보관함</SubTitle>
              <PageTitle>내가 찜한 축제</PageTitle>
              <TotalDesc>{bookmarks.length}개의 축제를 보관 중이에요 · 카드를 누르면 상세 페이지로 이동합니다</TotalDesc>
            </TitleLeft>
            
            <CountBadge>
              <IoMdHeart className="heart-icon" size={16} />
              <span className="count-num">{bookmarks.length}</span>
              <span className="count-text">저장됨</span>
            </CountBadge>
          </TitleContainer>
          
          <ContentArea>
            {bookmarks.length === 0 ? (
              <EmptyState>
                아직 찜한 축제가 없습니다.<br/>
                메인 화면에서 마음에 드는 축제를 찾아 메모를 남겨보세요!
              </EmptyState>
            ) : (
              <CardGrid>
                {bookmarks.map((bookmark) => (
                  <BookmarkCard 
                    key={bookmark.bookmarkId} 
                    bookmark={bookmark} 
                    onUpdate={handleUpdate}
                    onDelete={handleDelete}
                  />
                ))}
              </CardGrid>
            )}
          </ContentArea>
        </PageInner>
      </PageWrapper>
    </>
  );
};

export default MyPage;

// --- 스타일 컴포넌트 ---
const PageWrapper = styled.main`
  flex: 1;
  padding: 60px 20px;
  /* background-color: #fcfcfc; 
  min-height: calc(100vh - 80px); 
`;

const PageInner = styled.div`
  max-width: 1000px;
  margin: 0 auto;
`;


const TitleContainer = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-end; 
  margin-bottom: ${({ theme }) => theme.spacing.xl};
  padding-bottom: ${({ theme }) => theme.spacing.md};
`;

const TitleLeft = styled.div`
  display: flex;
  flex-direction: column;
  text-align: left; 
`;

const SubTitle = styled.span`
  font-size: ${({ theme }) => theme.fontSizes.s};
  font-weight: 700;
  color: #d76d38; 
  letter-spacing: 0.05em;
  margin-bottom: 6px;
`;

const PageTitle = styled.h1`
  font-size: 36px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.text};
  margin-bottom: 8px;
`;

const TotalDesc = styled.p`
  font-size: ${({ theme }) => theme.fontSizes.sm};
  color: ${({ theme }) => theme.colors.textLight};
`;

const CountBadge = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #ffffff;
  padding: 8px 16px;
  border-radius: 100px;
  border: 1px solid #e9ded3;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  height: fit-content;
  margin-bottom: 6px; 

  .heart-icon {
    color: #c92a2a;
  }

  .count-num {
    font-size: ${({ theme }) => theme.fontSizes.sm};
    font-weight: 700;
    color: ${({ theme }) => theme.colors.text};
  }

  .count-text {
    font-size: ${({ theme }) => theme.fontSizes.sm};
    color: ${({ theme }) => theme.colors.textLight};
  }
`;

const ContentArea = styled.div`
  background-color: #ffffff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  min-height: 400px;
`;

const EmptyState = styled.div`
  text-align: center;
  color: #999;
  font-size: 1.1rem;
  line-height: 1.6;
  padding: 60px 0;
  background-color: #f9f9f9;
  border-radius: 12px;
  border: 1px dashed #ddd;
`;


const CardGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
`;
