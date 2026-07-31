import { forwardRef } from 'react';
import styled from 'styled-components';

const MemoTextarea = forwardRef((props, ref) => {
  const {
    value = '',
    onChange,
    placeholder = '개인 메모를 입력하세요',
    readOnly = false,
    onClick,
    maxLength = 255, 
  } = props;

  return (
    <div onClick={onClick}>
      <MemoInput
        ref={ref}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        readOnly={readOnly}
        maxLength={maxLength}
      />
      {!readOnly && (
        <CharCounter $isError={value.length >= maxLength}>
          {value.length} / {maxLength}
        </CharCounter>
      )}
    </div>
  );
});

export default MemoTextarea;

// Styles
const MemoInput = styled.textarea`
  width: 100%;
  padding: ${({ theme }) => theme.spacing.sm} ${({ theme }) => theme.spacing.md};
  font-size: ${({ theme }) => theme.fontSizes.sm};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.borderRadius.md};
  background-color: ${({ theme }) => theme.colors.background};
  color: ${({ theme }) => theme.colors.text};
  resize: none;
  height: 80px; /* 🎨 높이를 늘려 글자 수 카운터 공간 확보 */
  box-sizing: border-box;
  line-height: 1.4;

  &[readOnly] {
    background-color: ${({ theme }) => theme.colors.background};
    border-color: ${({ theme }) => theme.colors.border};
  }

  &:focus {
    border-color: ${({ theme }) => theme.colors.secondary};
    outline: none;
  }
`;

const CharCounter = styled.div`
  text-align: right;
  font-size: 0.75rem;
  color: ${({ theme, $isError }) =>
    $isError ? theme.colors.danger : theme.colors.textLight};
  margin-top: 4px;
  padding-right: 4px;
`;
