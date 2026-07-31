import { Navigate, Outlet } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import styled, { keyframes } from 'styled-components';

const ProtectedRoute = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    // 인증 상태를 확인하는 동안 로딩 화면을 보여줍니다.
    return (
      <LoadingWrapper>
        <Spinner />
        <LoadingText>
          사용자 정보를 확인 중입니다...
        </LoadingText>
      </LoadingWrapper>
    );
  }

  // 인증되지 않은 사용자는 로그인 페이지로 보냅니다.
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;

const LoadingWrapper = styled.div`
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: center;
  height: 100vh;
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