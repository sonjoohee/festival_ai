import styled, { keyframes } from 'styled-components';
import { FaRegStar } from 'react-icons/fa'; // 상단 별, 체크 아이콘

const PLACEHOLDER_SUMMARY = ['관련 AI 분석 정보가 없습니다.'];

const AiSummaryBox = ({ summary, loading = false }) => {
  const lines = summary?.length > 0 ? summary : PLACEHOLDER_SUMMARY;

  return (
    <SummaryBox>
  
      <HeaderSection>
        <StarBadge>
          <FaRegStar size={14} />
        </StarBadge>
        <HeaderTitleDocs>
          <h4>AI 분석 결과</h4>
          <p>실시간 공공 데이터 기반</p>
        </HeaderTitleDocs>
      </HeaderSection>

      <ContentSection>
        {loading ? (
          <LoadingWrapper>
            <Spinner />
            <LoadingText>AI가 정보를 분석하고 있습니다...</LoadingText>
          </LoadingWrapper>
        ) : (
          <SummaryList>
            {lines.map(
              (line, index) => (
        
                <SummaryItem key={index}>
                  {line.trim() ? `✨ ${line}` : ' '}
                </SummaryItem>
              ),
            )}
          </SummaryList>
        )}
      </ContentSection>
    </SummaryBox>
  );
};

export default AiSummaryBox;


const SummaryBox = styled.div`
  background: #ffffff;
  border-radius: ${({ theme }) => theme.borderRadius.lg || '24px'};
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid #f4ebe1;
`;

const HeaderSection = styled.div`
  background-color: #33231a; 
  padding: 18px 24px;
  display: flex;
  align-items: center;
  gap: 14px;
`;

const StarBadge = styled.div`
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #dfa857; 
  border: 1px solid rgba(255, 255, 255, 0.15);
`;

const HeaderTitleDocs = styled.div`
  display: flex;
  flex-direction: column;
  
  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #ffffff;
    margin: 0;
  }
  
  p {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.5);
    margin: 2px 0 0 0;
  }
`;

const ContentSection = styled.div`
  padding: 40px 24px; 
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

const SummaryList = styled.ul`
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing.sm};
`;

const SummaryItem = styled.li`
  font-size: 14px;
  font-weight: 600;
  color: #1e3a2f;
  line-height: 1.5;
  margin: 0;
  // text-align: center; 
`;

const LoadingText = styled.p`
  font-size: 14px;
  color: #8c7e74;
  font-style: italic;
  text-align: center;
  padding: 20px 0;
  margin: 10px 0 0 0;
`;


const LoadingWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
`;


const spin = keyframes`
  to { transform: rotate(360deg); }
`;

const Spinner = styled.div`
  width: 32px;
  height: 32px;
  border: 4px solid #f4ebe1; 
  border-top-color: #c05c36; 
  border-radius: 50%;
  animation: ${spin} 1s linear infinite;
`;