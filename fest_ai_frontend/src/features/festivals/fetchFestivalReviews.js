import { getReviewsByFestival } from '../../api/reviewsApi';

export async function fetchFestivalReviews(festivalId, { page = 1, limit = 10 } = {}) {
  const response = await getReviewsByFestival(festivalId, {
    page: page - 1,
    limit,
  });
  const { content, totalPages, number, totalElements } = response.data;

  return {
    reviews: content,
    totalPages,
    currentPage: number + 1,
    totalReviews: totalElements,
    averageRating: response.data.averageRating ?? 0,
  };
}
