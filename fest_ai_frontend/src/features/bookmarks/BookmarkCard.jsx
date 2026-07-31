import { useState, useRef } from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import { FiEdit3, FiTrash2 } from 'react-icons/fi'; 
import MemoTextarea from './MemoTextarea';
import { updateBookmark, deleteBookmark } from '../../api/bookmarksApi';

const BookmarkCard = ({ bookmark, onUpdate, onDelete }) => {
  const [memo, setMemo] = useState(bookmark.userMemo || '');
  const [isEditing, setIsEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const textareaRef = useRef(null);
  const navigate = useNavigate();

  const handleEditClick = () => {
    setIsEditing(true);

    setTimeout(() => textareaRef.current?.focus(), 0);
  };

  const handleSave = async () => {
    if (!isEditing) return;

    setSaving(true);
    try {
      await updateBookmark(bookmark.bookmarkId, { userMemo: memo });
      onUpdate?.(bookmark.bookmarkId, { userMemo: memo });
      setIsEditing(false); 
    } catch (err) {
      console.error('메모 수정 실패:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    // if (!window.confirm('이 축제를 찜 목록에서 삭제하시겠습니까?')) return;
    try {
      await deleteBookmark(bookmark.bookmarkId);
      onDelete?.(bookmark.bookmarkId);
    } catch (err) {
      console.error('삭제 실패:', err);
    }
  };

  const handleCardClick = (e) => {

    if (e.target.closest('button')) return;
   
    navigate(`/festivals/${bookmark.contentId}`);
  };

  return (
    <Card onClick={handleCardClick}>
      <CardImage $imageUrl={bookmark.imageUrl} />
      <CardBody>
        <CardTitle>{bookmark.title || '축제명'}</CardTitle>
    
        <MemoTextarea
          ref={textareaRef}
          value={memo}
          onChange={(e) => setMemo(e.target.value.substring(0, 255))}
          readOnly={!isEditing}
          onClick={(e) => {
            e.stopPropagation();
          }}
          style={{ cursor: isEditing ? 'text' : 'default' }}
        />
 
        <CardActions onClick={(e) => {
          e.stopPropagation();
        }}>
          {isEditing ? (
            <SaveButton onClick={handleSave} disabled={saving}>
              {saving ? '저장 중...' : '저장'}
            </SaveButton>
          ) : (
            <EditButton onClick={handleEditClick}>
              <FiEdit3 size={14} />
              수정
            </EditButton>
          )}
          {/* 🗑️ 삭제 버튼 영역 */}
          <DeleteButton onClick={handleDelete}>
            <FiTrash2 size={14} />
            
            찜 삭제
          </DeleteButton>
        </CardActions>
      </CardBody>
    </Card>
  );
};

export default BookmarkCard;


const Card = styled.article`
  background-color: ${({ theme }) => theme.colors.surface};
  border-radius: ${({ theme }) => theme.borderRadius.lg};
  overflow: hidden;
  box-shadow: ${({ theme }) => theme.shadows.md};
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: ${({ theme }) => theme.shadows.lg};
  }
`;

const CardImage = styled.div`
  width: 100%;
  height: 140px;
  background-color: ${({ theme }) => theme.colors.border};
  background-image: ${({ $imageUrl }) =>
    $imageUrl ? `url(${$imageUrl})` : 'none'};
  background-size: cover;
  background-position: center;
`;

const CardBody = styled.div`
  padding: ${({ theme }) => theme.spacing.md};
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing.md};
  flex: 1;
`;

const CardTitle = styled.h3`
  font-size: ${({ theme }) => theme.fontSizes.md};
  font-weight: 600;
  color: ${({ theme }) => theme.colors.text};
`;

const CardMeta = styled.p`
  font-size: ${({ theme }) => theme.fontSizes.xs};
  color: ${({ theme }) => theme.colors.textLight};
`;

const CardActions = styled.div`
  display: flex;
  justify-content: space-between;
  gap: ${({ theme }) => theme.spacing.sm};
  margin-top: auto;
`;

const ActionButton = styled.button`
  /* 공통 버튼 스타일 */
  padding: ${({ theme }) => theme.spacing.sm} ${({ theme }) => theme.spacing.md};
  font-size: ${({ theme }) => theme.fontSizes.sm};
  font-weight: 600;
  border-radius: 100px;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: background-color 0.2s ease, opacity 0.2s ease;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;


const EditButton = styled(ActionButton)`
  flex: 1.5;
  color: #4a3f35; 
  background-color: #f4ebe1; 
  
  &:hover:not(:disabled) {
    background-color: #e9ded3;
  }
`;

const SaveButton = styled(ActionButton)`
  flex: 1.5;
  color: ${({ theme }) => theme.colors.white};
  background-color: #d76d38;

  &:hover:not(:disabled) {
    background-color: #c25e2e;
  }
`;


const DeleteButton = styled(ActionButton)`
  flex: 0.8;
  color: #c92a2a; 
  background-color: #fff0f0; 
  
  &:hover {
    background-color: #ffe3e3;
  }
`;