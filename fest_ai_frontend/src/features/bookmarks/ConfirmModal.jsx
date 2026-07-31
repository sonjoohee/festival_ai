import styled from 'styled-components';

const ConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = '확인',
  cancelText = '취소',
}) => {
  if (!isOpen) return null;

  return (
    <ModalBackdrop onClick={onClose}>
      <ModalContent onClick={(e) => e.stopPropagation()}>
        <ModalTitle>{title}</ModalTitle>
        <ModalMessage>{message}</ModalMessage>
        <ModalActions>
          <CancelButton onClick={onClose}>{cancelText}</CancelButton>
          <ConfirmButton onClick={onConfirm}>{confirmText}</ConfirmButton>
        </ModalActions>
      </ModalContent>
    </ModalBackdrop>
  );
};

export default ConfirmModal;

const ModalBackdrop = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
`;

const ModalContent = styled.div`
  background-color: ${({ theme }) => theme.colors.surface};
  padding: ${({ theme }) => theme.spacing.xl};
  border-radius: ${({ theme }) => theme.borderRadius.lg};
  box-shadow: ${({ theme }) => theme.shadows.xl};
  width: 100%;
  max-width: 360px;
  text-align: center;
`;

const ModalTitle = styled.h2`
  font-size: ${({ theme }) => theme.fontSizes.lg};
  font-weight: 600;
  color: ${({ theme }) => theme.colors.text};
  margin-bottom: ${({ theme }) => theme.spacing.sm};
`;

const ModalMessage = styled.p`
  font-size: ${({ theme }) => theme.fontSizes.md};
  color: ${({ theme }) => theme.colors.textLight};
  margin-bottom: ${({ theme }) => theme.spacing.lg};
  line-height: 1.5;
`;

const ModalActions = styled.div`
  display: flex;
  gap: ${({ theme }) => theme.spacing.sm};
`;

const CancelButton = styled.button`
  flex: 1;
  padding: ${({ theme }) => theme.spacing.md};
  font-size: ${({ theme }) => theme.fontSizes.md};
  font-weight: 600;
  border-radius: ${({ theme }) => theme.borderRadius.md};
  background-color: ${({ theme }) => theme.colors.background};
  color: ${({ theme }) => theme.colors.text};
  border: 1px solid ${({ theme }) => theme.colors.border};
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: ${({ theme }) => theme.colors.border};
  }
`;

const ConfirmButton = styled(CancelButton)`
  background-color: #c92a2a;
  color: #fff;
  border-color: #c92a2a;

  &:hover {
    background-color: #a52222;
  }
`;