import styled, { keyframes } from 'styled-components';
import FestivalCard from './FestivalCard';

const FestivalGrid = ({ festivals = [], loading = false }) => {
  if (loading) {

    return (
      <LoadingContainer>
        <Spinner />
        <LoadingText>
          축제를 검색 중입니다. 잠시만 기다려주세요...
        </LoadingText>
      </LoadingContainer>
    );
  }

  if (festivals.length === 0) {
    return <EmptyMessage>검색 결과가 없습니다. 지역명을 입력해 보세요.</EmptyMessage>;
  }

  return (
    <GridContainer>
      <Grid>
        {festivals.map((festival) => (
          <FestivalCard key={festival.contentId || festival.id} festival={festival} />
        ))}
      </Grid>
    </GridContainer>
  );
};

export default FestivalGrid;

// Styles
const GridContainer = styled.div`
  width: 100%;
  max-width: 1200px; 
  margin: 0 auto;
  padding: 40px ${({ theme }) => theme.spacing.md};
  box-sizing: border-box;
  display: flex;
  justify-content: center; 
`;

const Grid = styled.div`
  display: grid; 
  grid-template-columns: repeat(3, 1fr); 
  gap: 24px;
  width: 100%;
`;

const EmptyMessage = styled.p`
  text-align: center;
  padding: ${({ theme }) => theme.spacing.xxl} 0;
  color: ${({ theme }) => theme.colors.textLight};
  font-size: ${({ theme }) => theme.fontSizes.lg};
`;


const LoadingContainer = styled.div`
  display: flex;
  flex-direction: column; 
  justify-content: center;
  align-items: center;
  padding: ${({ theme }) => theme.spacing.xxl} 0;
`;


const spin = keyframes`
  to { transform: rotate(360deg); }
`;

const Spinner = styled.div`
  width: 40px;
  height: 40px;
  border: 5px solid #f4ebe1; 
  border-top-color: #c05c36; 
  border-radius: 50%;
  animation: ${spin} 1s linear infinite;
`;


const LoadingText = styled.p`
  margin-top: ${({ theme }) => theme.spacing.md};
  font-size: ${({ theme }) => theme.fontSizes.md};
  color: ${({ theme }) => theme.colors.textLight};
`;