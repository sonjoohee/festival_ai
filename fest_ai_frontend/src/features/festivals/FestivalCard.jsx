import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';

const FestivalCard = ({ festival }) => {
  const navigate = useNavigate();
  const { contentId, title, startDate, endDate, imageUrl, addr } = festival; 

  const formatDate = (dateString) => {
    if (!dateString || dateString.length !== 8) return dateString;
    return `${dateString.substring(0, 4)}.${dateString.substring(4, 6)}.${dateString.substring(6, 8)}`;
  };

  return (
     <Card onClick={() => navigate(`/festivals/${contentId}`)}>
      <CardImage $imageUrl={imageUrl}>
        {!imageUrl && '이미지 없음'}
      </CardImage>
      <CardBody>
        <CardTitle>{title}</CardTitle>
        <CardMeta>{addr || '지역 정보 없음'}</CardMeta>
        <CardMeta>
          {formatDate(startDate)} ~ {formatDate(endDate)}
        </CardMeta>
      </CardBody>
    </Card>
  );
};

export default FestivalCard;

// Styles
const Card = styled.article`
  background-color: ${({ theme }) => theme.colors.surface};
  border-radius: ${({ theme }) => theme.borderRadius.lg};
  overflow: hidden;
  box-shadow: ${({ theme }) => theme.shadows.md};
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  
  width: 320px;
  height: 370px;
  box-sizing: border-box;

  &:hover {
    transform: translateY(-4px);
    box-shadow: ${({ theme }) => theme.shadows.lg};
  }
`;

const CardImage = styled.div`
  width: 100%;
  height: 270px; 
  flex-shrink: 0;
  
  background-color: ${({ theme }) => theme.colors.border};
  background-image: ${({ $imageUrl }) =>
    $imageUrl ? `url(${$imageUrl})` : 'none'};
    
  background-size: cover; 
  background-position: top; 
  background-repeat: no-repeat;
  position: relative;
`;

const CardBody = styled.div`
  padding: 12px 16px; 
  display: flex;
  flex-direction: column;
  gap: 3px;
  height: 100px; 
  box-sizing: border-box;
  background-color: ${({ theme }) => theme.colors.surface};
  justify-content: center;
`;

const CardTitle = styled.h3`
  // font-family: ${({ theme }) => theme.fonts.heading};
  font-size: ${({ theme }) => theme.fontSizes.md}; 
  font-weight: 500;
  color: ${({ theme }) => theme.colors.text};
  margin: 0;
  
  display: -webkit-box;
  -webkit-line-clamp: 1; 
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
`;

const CardMeta = styled.p`
  margin: 0;
  font-size: ${({ theme }) => theme.fontSizes.xs}; 
  color: ${({ theme }) => theme.colors.textLight};
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
`;