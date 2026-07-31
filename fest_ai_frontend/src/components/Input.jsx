import React from 'react';
import styled from 'styled-components';

// ==========================================
// 1. 뼈대 및 UI 로직 컴포넌트 영역
// ==========================================

export const Input = ({
  label,
  type = 'text',
  placeholder,
  value,
  onChange,
  name,
  id,
  ...rest
}) => (
  <InputWrapper>
    {label && <Label htmlFor={id || name}>{label}</Label>}
    <StyledInput
      id={id || name}
      type={type}
      name={name}
      placeholder={placeholder}
      value={value}
      onChange={onChange}
      {...rest}
    />
  </InputWrapper>
);

export const Textarea = ({
  label,
  placeholder,
  value,
  onChange,
  name,
  id,
  rows = 3,
  ...rest
}) => (
  <InputWrapper>
    {label && <Label htmlFor={id || name}>{label}</Label>}
    <StyledTextarea
      id={id || name}
      name={name}
      placeholder={placeholder}
      value={value}
      onChange={onChange}
      rows={rows}
      {...rest}
    />
  </InputWrapper>
);

export default Input;

// ==========================================
// 2. 주희 팀의 테마 사양이 100% 반영된 스타일 영역
// ==========================================

const InputWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing.xs};
  width: 100%;
`;

const Label = styled.label`
  font-size: ${({ theme }) => theme.fontSizes.sm};
  font-weight: 500;
  color: ${({ theme }) => theme.colors.text};
`;

const StyledInput = styled.input`
  width: 100%;
  padding: ${({ theme }) => theme.spacing.md};
  font-size: ${({ theme }) => theme.fontSizes.md};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.borderRadius.md};
  background-color: ${({ theme }) => theme.colors.surface};
  color: ${({ theme }) => theme.colors.text};
  transition: border-color 0.2s ease;

  &::placeholder {
    color: ${({ theme }) => theme.colors.textLight};
  }

  &:focus {
    border-color: ${({ theme }) => theme.colors.primary};
    box-shadow: 0 0 0 3px rgba(74, 144, 217, 0.15);
  }
`;

const StyledTextarea = styled.textarea`
  width: 100%;
  padding: ${({ theme }) => theme.spacing.md};
  font-size: ${({ theme }) => theme.fontSizes.sm};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.borderRadius.md};
  background-color: ${({ theme }) => theme.colors.surface};
  color: ${({ theme }) => theme.colors.text};
  resize: vertical;
  min-height: 60px;
  line-height: 1.5;

  &::placeholder {
    color: ${({ theme }) => theme.colors.textLight};
  }

  &:focus {
    border-color: ${({ theme }) => theme.colors.secondary};
    box-shadow: 0 0 0 3px rgba(127, 219, 202, 0.2);
  }
`;