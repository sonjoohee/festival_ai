import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import Button from '../../components/Button';
import Input from '../../components/Input';
import useAuth from '../../hooks/useAuth';
import { login } from '../../api/authApi';

const LoginForm = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login: setAuth } = useAuth();
  const navigate = useNavigate();


const handleSubmit = async (e) => {
  e.preventDefault();
  setError('');
  setLoading(true);

  try {
    const { data } = await login({ loginId: email, password }); 
    
    if (data && data.token) {
      setAuth(data.token); 
      navigate('/'); 
    } else {
      setError('서버 응답 형식이 올바르지 않습니다.');
    }

  } catch (err) {
    console.error("프론트엔드 함수 내부 에러:", err);
    setError(err.response?.data?.message || '로그인에 실패했습니다.');
  } finally {
    setLoading(false);
  }
};

  return (
    <>
      <FormContainer onSubmit={handleSubmit}>
        <Input
          label="이메일"
          type="email"
          name="email"
          placeholder="이메일을 입력하세요"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Input
          label="비밀번호"
          type="password"
          name="password"
          placeholder="비밀번호를 입력하세요"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {error && <p style={{ color: '#E74C3C', fontSize: '0.875rem' }}>{error}</p>}
        <Button type="submit" pill fullWidth disabled={loading}>
          {loading ? '로그인 중...' : '로그인'}
        </Button>
      </FormContainer>
      <SignupLinkWrapper>
        아직 계정이 없으신가요?
        <SignupLink as={Link} to="/signup">회원가입</SignupLink>
      </SignupLinkWrapper>
    </>
  );
};

export default LoginForm;

// Styles
const FormContainer = styled.form`
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing.md};
  margin-bottom: ${({ theme }) => theme.spacing.lg};
`;

const SignupLinkWrapper = styled.div`
  font-size: ${({ theme }) => theme.fontSizes.sm};
  color: ${({ theme }) => theme.colors.textLight};
  text-align: center;
`;

const SignupLink = styled.span`
  color: ${({ theme }) => theme.colors.primary};
  font-weight: 500;
  margin-left: ${({ theme }) => theme.spacing.xs};
  cursor: pointer;
  transition: color 0.2s ease;

  &:hover {
    color: ${({ theme }) => theme.colors.primaryDark};
  }
`;
