import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom'; 
import styled from 'styled-components';
import Button from '../../components/Button';
import Input from '../../components/Input';
import { signup } from '../../api/authApi';

const SignUpForm = ({ onSuccess }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 1. 빈칸 검사 추가
    if (!name || !email || !password) {
      setError('모든 항목을 빈칸 없이 입력해주세요.');
      return;
    }
    // 2. 기존 비밀번호 길이 검사
    if (password.length < 8) {
      setError('비밀번호는 8자 이상이어야 합니다.');
      return;
    }
    
    setError('');
    setLoading(true);

    try {
      await signup({ loginId: email, password, name });
      if (onSuccess) {
        onSuccess();
      } else {
        navigate('/login');
      }
    } catch (err) {
      setError(err.message || '회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
    
  };

  return (
    <>
      <FormContainer onSubmit={handleSubmit}>
        <Input
          label="이름"
          type="text"
          name="name"
          placeholder="이름을 입력하세요"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <Input
          label="이메일"
          type="email"
          name="email"
          placeholder="이메일을 입력하세요"
          value={email} 
          onChange={(e) => setEmail(e.target.value)}
        />
        <Input
          label="비밀번호 (8자 이상)"
          type="password"
          name="password"
          placeholder="비밀번호를 입력하세요"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {error && <p style={{ color: '#E74C3C', fontSize: '0.875rem' }}>{error}</p>}
        <Button type="submit" pill fullWidth disabled={loading}>
          {loading ? '회원가입 중...' : '회원가입'}
        </Button>
      </FormContainer>
      <SignupLinkWrapper>
        이미 계정이 있으신가요?
        <SignupLink as={Link} to="/login">로그인</SignupLink>
      </SignupLinkWrapper>
    </>
  );
};

export default SignUpForm;

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