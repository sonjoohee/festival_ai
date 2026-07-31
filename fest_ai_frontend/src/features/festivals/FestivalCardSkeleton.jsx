import styled, { keyframes } from 'styled-components';

const shimmer = keyframes`
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
`;

const Skeleton = styled.div`
  background: linear-gradient(
    90deg,
    ${({ theme }) => theme.colors.border} 25%,
    #e9ded3 37%,
    ${({ theme }) => theme.colors.border} 63%
  );
  background-size: 200% 100%;
  animation: ${shimmer} 1.5s infinite linear;
  border-radius: ${({ theme }) => theme.borderRadius.md};
`;

const SkeletonCard = styled.div`
  width: 320px;
  height: 370px;
  background-color: ${({ theme }) => theme.colors.surface};
  border-radius: ${({ theme }) => theme.borderRadius.lg};
  overflow: hidden;
  box-shadow: ${({ theme }) => theme.shadows.md};
`;

const SkeletonImage = styled(Skeleton)`
  height: 270px;
`;

const SkeletonBody = styled.div`
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
  height: 100px;
`;

const SkeletonText = styled(Skeleton)`
  height: 20px;
  width: ${(props) => props.width || '100%'};
`;

const FestivalCardSkeleton = () => (
  <SkeletonCard>
    <SkeletonImage />
    <SkeletonBody>
      <SkeletonText width="80%" />
      <SkeletonText width="60%" />
      <SkeletonText width="70%" />
    </SkeletonBody>
  </SkeletonCard>
);

export default FestivalCardSkeleton;