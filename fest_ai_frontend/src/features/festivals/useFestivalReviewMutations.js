import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  postReview,
  updateReview,
  deleteReview,
} from '../../api/reviewsApi';
import { reviewKeys } from './reviewQueryKeys';

export function useFestivalReviewMutations(festivalId) {
  const queryClient = useQueryClient();
  const festivalKey = reviewKeys.byFestival(festivalId);

  const invalidateReviews = () =>
    queryClient.invalidateQueries({ queryKey: festivalKey });

  const createMutation = useMutation({
    mutationFn: (reviewData) => postReview(Number(festivalId), reviewData),
    onSuccess: invalidateReviews,
  });

  const updateMutation = useMutation({
    mutationFn: ({ reviewId, updateData }) =>
      updateReview(reviewId, updateData),
    onSuccess: invalidateReviews,
  });

  const deleteMutation = useMutation({
    mutationFn: (reviewId) => deleteReview(reviewId),
    onSuccess: invalidateReviews,
  });

  return {
    createMutation,
    updateMutation,
    deleteMutation,
  };
}
