import styled from 'styled-components';
import Header from '../components/Header.jsx';
import LoginForm from '../features/auth/LoginForm';

const LoginPage = () => (
  <>
    <Header />
    <PageWrapper>
      <LoginCard>
        <LogoArea>
          <LogoIcon>🎡</LogoIcon>
          <LogoText>모아패밀리</LogoText>
          <LogoSubtext>우리 가족에게 딱 맞는 축제를 찾아보세요</LogoSubtext>
        </LogoArea>
        <LoginForm />
      </LoginCard>
    </PageWrapper>
  </>
);

export default LoginPage;

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

const LoginCard = styled.div`
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
  margin-bottom: ${({ theme }) => theme.spacing.lg};
`;

const LogoIcon = styled.div`
  font-size: 3rem;
  line-height: 1;
  margin-bottom: ${({ theme }) => theme.spacing.md};
`;

const LogoText = styled.h1`
  font-size: ${({ theme }) => theme.fontSizes.xxxl};
  color: ${({ theme }) => theme.colors.primary};
`;

const LogoSubtext = styled.p`
  font-size: ${({ theme }) => theme.fontSizes.md};
  color: ${({ theme }) => theme.colors.textLight};
  margin-top: ${({ theme }) => theme.spacing.sm};
`;
