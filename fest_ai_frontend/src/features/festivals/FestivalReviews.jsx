import { useState, useEffect } from 'react';
import styled from 'styled-components';
import { FaStar } from 'react-icons/fa';
import { 
  getReviewsByFestival,
  postReview as apiPostReview,
  updateReview as apiUpdateReview,
  deleteReview as apiDeleteReview,
} from '../../api/reviewsApi';
// import { useAuth } from '../../contexts/AuthContext'; // Assume AuthContext provides user info

// Mock API functions - replace with actual API calls
const getReviews = async (festivalId, page = 1, limit = 5) => {
  console.log(`Fetching reviews for festival ${festivalId}, page: ${page}, limit: ${limit}`);
  try {
    const response = await getReviewsByFestival(festivalId, { page: page - 1, limit });
    console.log('API Response:', response.data);
    // API 응답을 프론트엔드 형식에 맞게 변환
    return {
      reviews: response.data.content,
      totalPages: response.data.totalPages,
      currentPage: response.data.number + 1,
      totalReviews: response.data.totalElements,
      averageRating: response.data.averageRating || 0, // 백엔드 응답에 평균 평점이 없다면 0으로 처리
    };
  } catch (error) {
    console.error("Failed to fetch reviews:", error);
    // 에러 발생 시 빈 데이터 반환
    return { reviews: [], totalPages: 0, currentPage: 1, totalReviews: 0, averageRating: 0 };
  }
};

const StarRating = ({ rating }) => (
  <StarContainer>
    {[...Array(5)].map((_, i) => (
      <FaStar key={i} color={i < rating ? '#ffc107' : '#e4e5e9'} />
    ))}
  </StarContainer>
);

const FestivalReviews = ({ festivalId }) => {
  // const { user } = useAuth(); // 실제 앱에서는 Context API 등에서 사용자 정보를 가져옵니다.
  const user = { userId: 123, nickname: '행복한가족' }; // UI 테스트를 위한 모의 사용자
  const [reviews, setReviews] = useState([]);
  const [stats, setStats] = useState({ total: 0, avg: 0 });
  const [newReview, setNewReview] = useState({ rating: 0, comment: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [userHasReviewed, setUserHasReviewed] = useState(false);

  const [editingReviewId, setEditingReviewId] = useState(null);
  const [editedContent, setEditedContent] = useState({ rating: 0, comment: '' });

  const fetchReviews = async () => {
    try {
      const response = await getReviews(festivalId);
      setReviews(response.reviews);
      setUserHasReviewed(response.reviews.some(r => r.author === user.nickname));
      setStats({ total: response.totalReviews, avg: response.averageRating });
    } catch (error) {
      console.error("Failed to fetch reviews:", error);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [festivalId]);
  
  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (newReview.rating === 0 || !newReview.comment.trim()) {
      alert('별점과 후기 내용을 모두 입력해주세요.');
      return;
    }
    setIsSubmitting(true);
    try {
      await apiPostReview(Number(festivalId), newReview); // festivalId를 경로 파라미터로, newReview를 본문으로 전달
      setNewReview({ rating: 0, comment: '' });
      await fetchReviews(); // 리뷰 목록 새로고침
    } catch (error) {
      console.error("Failed to post review:", error);
      alert('후기 작성에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
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
      await apiUpdateReview(reviewId, editedContent);
      await fetchReviews(); // 리뷰 목록 새로고침
      handleCancelEdit();
    } catch (error) {
      console.error("Failed to update review:", error);
      alert('후기 수정에 실패했습니다.');
    }
  };

  const handleDelete = async (reviewId) => {
    // eslint-disable-next-line no-restricted-globals
    if (confirm('정말로 후기를 삭제하시겠습니까?')) {
      try {
        await apiDeleteReview(reviewId);
        await fetchReviews(); // 리뷰 목록 새로고침
      } catch (error) {
        console.error("Failed to delete review:", error);
        alert('후기 삭제에 실패했습니다.');
      }
    }
  };

  const renderStarInput = (rating, onRatingChange) => (
    [...Array(5)].map((_, i) => {
      const ratingValue = i + 1;
      return <FaStar key={i} size={24} color={ratingValue <= rating ? '#ffc107' : '#e4e5e9'} onClick={() => onRatingChange(ratingValue)} style={{ cursor: 'pointer' }} />;
    })
  );

  return (
    <ReviewContainer>
      <Title>방문 후기 <Count>({stats.total})</Count></Title>
      {stats.total > 0 && (
        <AverageSection>
          <StarRating rating={stats.avg} />
          <AverageText>{stats.avg.toFixed(1)} / 5.0</AverageText>
        </AverageSection>
      )}

      {user && !userHasReviewed && (
        <ReviewForm onSubmit={handleReviewSubmit}>
          <StarRatingInput>
            {renderStarInput(newReview.rating, (rating) => setNewReview({ ...newReview, rating }))}
          </StarRatingInput>
          <ReviewTextarea
            placeholder="생생한 후기를 남겨주세요! (예: 아이와 함께 갈 때 꿀팁)"
            value={newReview.comment}
            onChange={(e) => setNewReview({...newReview, comment: e.target.value})}
          />
          <SubmitButton type="submit" disabled={isSubmitting}>
            {isSubmitting ? '등록 중...' : '후기 등록'}
          </SubmitButton>
        </ReviewForm>
      )}

      <ReviewList>
        {reviews.map(review => (
          editingReviewId === review.reviewId ? (
            <ReviewItem key={review.reviewId}>
              <EditForm>
                <StarRatingInput>
                  {renderStarInput(editedContent.rating, (rating) => setEditedContent({ ...editedContent, rating }))}
                </StarRatingInput>
                <ReviewTextarea
                  value={editedContent.comment}
                  onChange={(e) => setEditedContent({ ...editedContent, comment: e.target.value })}
                />
                <ButtonGroup>
                  <EditButton type="button" onClick={handleCancelEdit}>취소</EditButton>
                  <EditButton type="button" $primary onClick={() => handleUpdateSubmit(review.reviewId)}>저장</EditButton>
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
                {user?.nickname === review.author && (
                  <ButtonGroup>
                    <EditButton onClick={() => handleEditClick(review)}>수정</EditButton>
                    <EditButton onClick={() => handleDelete(review.reviewId)}>삭제</EditButton>
                  </ButtonGroup>
                )}
              </ReviewFooter>
            </ReviewItem>
          )
        ))}
      </ReviewList>
    </ReviewContainer>
  );
};

export default FestivalReviews;

const ReviewContainer = styled.div`
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
`;

const Title = styled.h3`
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 16px;
`;

const Count = styled.span`
  color: ${({ theme }) => theme.colors.primary};
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
  & svg { cursor: pointer; }
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
  &:hover { background-color: #a04825; }
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
  color: ${({ theme, $primary }) => $primary ? theme.colors.primary : '#888'};
  cursor: pointer;
  font-size: 0.9rem;
  padding: 4px;
  &:hover {
    text-decoration: underline;
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