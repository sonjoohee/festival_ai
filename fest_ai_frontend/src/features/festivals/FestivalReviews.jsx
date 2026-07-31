import { useState, useEffect } from 'react';
import styled from 'styled-components';
import { FaStar } from 'react-icons/fa';
// import { useAuth } from '../../contexts/AuthContext'; // Assume AuthContext provides user info

// Mock API functions - replace with actual API calls
const getReviews = async (festivalId, page = 1, limit = 5) => {
  console.log(`Fetching reviews for festival ${festivalId}, page: ${page}, limit: ${limit}`);
  // Mock response based on backend spec
  return {
    reviews: [
      {
        reviewId: 1,
        author: { userId: 123, nickname: "행복한가족" },
        rating: 5,
        comment: "아이들이 정말 좋아했어요! 유모차 끌고 다니기도 편했습니다.",
        photos: [],
        createdAt: "2026-07-28T10:00:00Z"
      },
      {
        reviewId: 2,
        author: { userId: 124, nickname: "축제매니아" },
        rating: 4,
        comment: "음식은 맛있었지만, 주차장이 너무 붐볐어요. 그래도 볼거리는 많아서 좋았습니다. 아이들이랑 갈 때는 대중교통 이용하는 걸 추천해요.",
        photos: [],
        createdAt: "2026-07-29T11:00:00Z"
      }
    ],
    totalPages: 1,
    currentPage: 1,
    averageRating: 4.5,
    totalReviews: 2,
  };
};

const postReview = async (festivalId, { rating, comment, photos }) => {
  console.log(`Posting review for festival ${festivalId}`, { rating, comment, photos });
  // Mock response
  return {
    reviewId: Math.random(),
    author: { userId: 125, nickname: "새로운방문자" },
    rating,
    comment,
    photos: [],
    createdAt: new Date().toISOString(),
  };
};

const StarRating = ({ rating }) => (
  <StarContainer>
    {[...Array(5)].map((_, i) => (
      <FaStar key={i} color={i < rating ? '#ffc107' : '#e4e5e9'} />
    ))}
  </StarContainer>
);

const FestivalReviews = ({ festivalId }) => {
  // const { user } = useAuth(); // Check if user is logged in
  const user = { nickname: '테스트유저' }; // Mock user for UI testing
  const [reviews, setReviews] = useState([]);
  const [stats, setStats] = useState({ total: 0, avg: 0 });
  const [newReview, setNewReview] = useState({ rating: 0, comment: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const fetchReviews = async () => {
      try {
        const response = await getReviews(festivalId);
        setReviews(response.reviews);
        setStats({ total: response.totalReviews, avg: response.averageRating });
      } catch (error) {
        console.error("Failed to fetch reviews:", error);
      }
    };
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
      const posted = await postReview(festivalId, newReview);
      setReviews(prev => [posted, ...prev]);
      setNewReview({ rating: 0, comment: '' });
    } catch (error) {
      console.error("Failed to post review:", error);
      alert('후기 작성에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <ReviewContainer>
      <Title>방문 후기 <Count>({stats.total})</Count></Title>
      {stats.total > 0 && (
        <AverageSection>
          <StarRating rating={stats.avg} />
          <AverageText>{stats.avg.toFixed(1)} / 5.0</AverageText>
        </AverageSection>
      )}

      {user && (
        <ReviewForm onSubmit={handleReviewSubmit}>
          <StarRatingInput>
            {[...Array(5)].map((_, i) => {
              const ratingValue = i + 1;
              return (
                <label key={i}>
                  <input type="radio" name="rating" value={ratingValue} onClick={() => setNewReview({...newReview, rating: ratingValue})} />
                  <FaStar size={24} color={ratingValue <= newReview.rating ? '#ffc107' : '#e4e5e9'} />
                </label>
              );
            })}
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
          <ReviewItem key={review.reviewId}>
            <ReviewHeader>
              <strong>{review.author.nickname}</strong>
              <StarRating rating={review.rating} />
            </ReviewHeader>
            <p>{review.comment}</p>
            <small>{new Date(review.createdAt).toLocaleDateString()}</small>
          </ReviewItem>
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
  & input { display: none; }
  & label { cursor: pointer; }
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

const StarContainer = styled.div`
  display: flex;
  gap: 2px;
`;