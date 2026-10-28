import { useState } from 'react';
import styled from 'styled-components';
import { FaStar } from 'react-icons/fa';
import { useQuery } from '@tanstack/react-query';
import useAuth from '../../hooks/useAuth';
import { fetchFestivalReviews } from './fetchFestivalReviews';
import { reviewKeys } from './reviewQueryKeys';
import { useFestivalReviewMutations } from './useFestivalReviewMutations';

const StarRating = ({ rating }) => (
  <StarContainer>
    {[...Array(5)].map((_, i) => (
      <FaStar key={i} color={i < rating ? '#ffc107' : '#e4e5e9'} />
    ))}
  </StarContainer>
);

const FestivalReviews = ({ festivalId }) => {
  const { loginId, isAuthenticated } = useAuth();
  const [newReview, setNewReview] = useState({ rating: 0, comment: '' });
  const [editingReviewId, setEditingReviewId] = useState(null);
  const [editedContent, setEditedContent] = useState({ rating: 0, comment: '' });

  const { data, isLoading, isError } = useQuery({
    queryKey: reviewKeys.byFestival(festivalId),
    queryFn: () => fetchFestivalReviews(festivalId),
    enabled: Boolean(festivalId),
  });

  const { createMutation, updateMutation, deleteMutation } =
    useFestivalReviewMutations(festivalId);

  const reviews = data?.reviews ?? [];
  const stats = {
    total: data?.totalReviews ?? 0,
    avg: data?.averageRating ?? 0,
  };

  const userHasReviewed =
    isAuthenticated &&
    Boolean(loginId) &&
    reviews.some((review) => review.author === loginId);

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (newReview.rating === 0 || !newReview.comment.trim()) {
      alert('별점과 후기 내용을 모두 입력해주세요.');
      return;
    }
    try {
      await createMutation.mutateAsync(newReview);
      setNewReview({ rating: 0, comment: '' });
    } catch (error) {
      console.error('Failed to post review:', error);
      alert(
        error.response?.data?.message ||
          '후기 작성에 실패했습니다. 이미 작성한 후기가 있는지 확인해주세요.',
      );
    }
  };

  const handleEditClick = (review) => {
    setEditingReviewId(review.reviewId);
    setEditedContent({ rating: review.rating, comment: review.comment });
  };

  const handleCancelEdit = () => {
    setEditingReviewId(null);
    setEditedContent({ rating: 0, comment: '' });
  };

  const handleUpdateSubmit = async (reviewId) => {
    if (editedContent.rating === 0 || !editedContent.comment.trim()) {
      alert('별점과 후기 내용을 모두 입력해주세요.');
      return;
    }
    try {
      await updateMutation.mutateAsync({
        reviewId,
        updateData: editedContent,
      });
      handleCancelEdit();
    } catch (error) {
      console.error('Failed to update review:', error);
      alert('후기 수정에 실패했습니다.');
    }
  };

  const handleDelete = async (reviewId) => {
    // eslint-disable-next-line no-restricted-globals
    if (!confirm('정말로 후기를 삭제하시겠습니까?')) {
      return;
    }
    try {
      await deleteMutation.mutateAsync(reviewId);
    } catch (error) {
      console.error('Failed to delete review:', error);
      alert('후기 삭제에 실패했습니다.');
    }
  };

  const renderStarInput = (rating, onRatingChange) =>
    [...Array(5)].map((_, i) => {
      const ratingValue = i + 1;
      return (
        <FaStar
          key={i}
          size={24}
          color={ratingValue <= rating ? '#ffc107' : '#e4e5e9'}
          onClick={() => onRatingChange(ratingValue)}
          style={{ cursor: 'pointer' }}
        />
      );
    });

  if (isLoading) {
    return (
      <ReviewContainer>
        <Title>방문 후기</Title>
        <LoadingMessage>후기를 불러오는 중...</LoadingMessage>
      </ReviewContainer>
    );
  }

  if (isError) {
    return (
      <ReviewContainer>
        <Title>방문 후기</Title>
        <LoadingMessage>후기를 불러오지 못했습니다.</LoadingMessage>
      </ReviewContainer>
    );
  }

  return (
    <ReviewContainer>
      <Title>
        방문 후기 <Count>({stats.total})</Count>
      </Title>
      {stats.total > 0 && (
        <AverageSection>
          <StarRating rating={Math.round(stats.avg)} />
          <AverageText>{stats.avg.toFixed(1)} / 5.0</AverageText>
        </AverageSection>
      )}

      {isAuthenticated && loginId && !userHasReviewed && (
        <ReviewForm onSubmit={handleReviewSubmit}>
          <StarRatingInput>
            {renderStarInput(newReview.rating, (rating) =>
              setNewReview({ ...newReview, rating }),
            )}
          </StarRatingInput>
          <ReviewTextarea
            placeholder="생생한 후기를 남겨주세요! (예: 아이와 함께 갈 때 꿀팁)"
            value={newReview.comment}
            onChange={(e) =>
              setNewReview({ ...newReview, comment: e.target.value })
            }
          />
          <SubmitButton type="submit" disabled={createMutation.isPending}>
            {createMutation.isPending ? '등록 중...' : '후기 등록'}
          </SubmitButton>
        </ReviewForm>
      )}

      <ReviewList>
        {reviews.map((review) =>
          editingReviewId === review.reviewId ? (
            <ReviewItem key={review.reviewId}>
              <EditForm>
                <StarRatingInput>
                  {renderStarInput(editedContent.rating, (rating) =>
                    setEditedContent({ ...editedContent, rating }),
                  )}
                </StarRatingInput>
                <ReviewTextarea
                  value={editedContent.comment}
                  onChange={(e) =>
                    setEditedContent({
                      ...editedContent,
                      comment: e.target.value,
                    })
                  }
                />
                <ButtonGroup>
                  <EditButton type="button" onClick={handleCancelEdit}>
                    취소
                  </EditButton>
                  <EditButton
                    type="button"
                    $primary
                    onClick={() => handleUpdateSubmit(review.reviewId)}
                    disabled={updateMutation.isPending}
                  >
                    저장
                  </EditButton>
                </ButtonGroup>
              </EditForm>
            </ReviewItem>
          ) : (
            <ReviewItem key={review.reviewId}>
              <ReviewHeader>
                <strong>{review.author}</strong>
                <StarRating rating={review.rating} />
              </ReviewHeader>
              <p>{review.comment}</p>
              <ReviewFooter>
                <small>{new Date(review.createdAt).toLocaleDateString()}</small>
                {loginId && loginId === review.author && (
                  <ButtonGroup>
                    <EditButton onClick={() => handleEditClick(review)}>
                      수정
                    </EditButton>
                    <EditButton
                      onClick={() => handleDelete(review.reviewId)}
                      disabled={deleteMutation.isPending}
                    >
                      삭제
                    </EditButton>
                  </ButtonGroup>
                )}
              </ReviewFooter>
            </ReviewItem>
          ),
        )}
      </ReviewList>
    </ReviewContainer>
  );
};

export default FestivalReviews;

const ReviewContainer = styled.div`
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
`;

const Title = styled.h3`
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 16px;
`;

const Count = styled.span`
  color: ${({ theme }) => theme.colors.primary};
`;

const LoadingMessage = styled.p`
  color: #888;
  font-size: 0.95rem;
`;

const AverageSection = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
`;

const AverageText = styled.span`
  font-size: 1.1rem;
  font-weight: 700;
`;

const ReviewForm = styled.form`
  margin-bottom: 24px;
  border-top: 1px solid #eee;
  padding-top: 24px;
`;

const StarRatingInput = styled.div`
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  & svg {
    cursor: pointer;
  }
`;

const ReviewTextarea = styled.textarea`
  width: 100%;
  min-height: 80px;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #ddd;
  resize: vertical;
  margin-bottom: 12px;
`;

const SubmitButton = styled.button`
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background-color: ${({ theme }) => theme.colors.primary};
  color: white;
  font-weight: 700;
  cursor: pointer;
  &:hover {
    background-color: #a04825;
  }
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
`;

const ReviewList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

const ReviewItem = styled.div`
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
`;

const ReviewHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
`;

const ReviewFooter = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  color: #888;
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 8px;
`;

const EditButton = styled.button`
  background: none;
  border: none;
  color: ${({ theme, $primary }) => ($primary ? theme.colors.primary : '#888')};
  cursor: pointer;
  font-size: 0.9rem;
  padding: 4px;
  &:hover {
    text-decoration: underline;
  }
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

const EditForm = styled.div`
  display: flex;
  flex-direction: column;
`;

const StarContainer = styled.div`
  display: flex;
  gap: 2px;
`;
