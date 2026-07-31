import styled, { css } from 'styled-components';

const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  pill = false,
  fullWidth = false,
  type = 'button',
  onClick,
  disabled = false,
  ...rest
}) => (
  <StyledButton
    type={type}
    $variant={variant}
    $size={size}
    $pill={pill}
    $fullWidth={fullWidth}
    onClick={onClick}
    disabled={disabled}
    {...rest}
  >
    {children}
  </StyledButton>
);

export default Button;

// Styles
const primaryStyles = css`
  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  &:hover {
    background-color: ${({ theme }) => theme.colors.primaryDark};
  }
`;

const secondaryStyles = css`
  background-color: ${({ theme }) => theme.colors.secondary};
  color: ${({ theme }) => theme.colors.white};
  &:hover {
    background-color: ${({ theme }) => theme.colors.secondaryDark};
  }
`;

const sizeStyles = {
  sm: css`
    padding: ${({ theme }) => theme.spacing.xs} ${({ theme }) => theme.spacing.sm};
    font-size: ${({ theme }) => theme.fontSizes.sm};
  `,
  md: css`
    padding: ${({ theme }) => theme.spacing.sm} ${({ theme }) => theme.spacing.md};
    font-size: ${({ theme }) => theme.fontSizes.md};
  `,
  lg: css`
    padding: ${({ theme }) => theme.spacing.md} ${({ theme }) => theme.spacing.lg};
    font-size: ${({ theme }) => theme.fontSizes.lg};
  `,
};

const StyledButton = styled.button`
  border: none;
  border-radius: ${({ theme }) => theme.borderRadius.md};
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  ${({ $variant }) => {
    switch ($variant) {
      case 'primary':
        return primaryStyles;
      case 'secondary':
        return secondaryStyles;
      default:
        return primaryStyles;
    }
  }}

  ${({ $size }) => sizeStyles[$size]}

  ${({ $pill }) => $pill && css`
    border-radius: 9999px;
  `}

  ${({ $fullWidth }) => $fullWidth && css`
    width: 100%;
  `}

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;
