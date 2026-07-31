import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import SignUpForm from '../features/auth/SignUpForm';

const SignUpPage = () => {
  const navigate = useNavigate();

  const handleSignUpSuccess = () => {
    alert('회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.');
    navigate('/login');
  };

  return (
    <>
      <Header />
      <PageWrapper>
        <SignUpCard>
          <LogoArea>
            <LogoIcon>🎡</LogoIcon>
            <LogoText>모아패밀리</LogoText>
            <LogoSubtext>가족 맞춤 축제 큐레이션</LogoSubtext>
          </LogoArea>
          <SignUpForm onSuccess={handleSignUpSuccess} />
        </SignUpCard>
      </PageWrapper>
    </>
  );
};

export default SignUpPage;

// Styles
const PageWrapper = styled.main`
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: ${({ theme }) => theme.spacing.xxl} ${({ theme }) => theme.spacing.xl};
  min-height: calc(100vh - ${({ theme }) => theme.layout.headerHeight});
`;

const SignUpCard = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 420px;
  padding: ${({ theme }) => theme.spacing.xxl};
  background-color: ${({ theme }) => theme.colors.surface};
  border-radius: ${({ theme }) => theme.borderRadius.xl};
  box-shadow: ${({ theme }) => theme.shadows.lg};
`;

const LogoArea = styled.div`
  text-align: center;
  margin-bottom: ${({ theme }) => theme.spacing.xl};
`;

const LogoIcon = styled.div`
  font-size: ${({ theme }) => theme.fontSizes.logo};
  margin-bottom: ${({ theme }) => theme.spacing.sm};
`;

const LogoText = styled.h1`
  font-size: ${({ theme }) => theme.fontSizes.xxxl};
  font-weight: 700;
  color: ${({ theme }) => theme.colors.primary};
`;

const LogoSubtext = styled.p`
  font-size: ${({ theme }) => theme.fontSizes.md};
  color: ${({ theme }) => theme.colors.textLight};
  margin-top: ${({ theme }) => theme.spacing.sm};
`;