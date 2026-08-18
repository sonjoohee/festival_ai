import axiosInstance from './axiosInstance'; // 경로 수정

/**
 * 특정 축제의 리뷰 목록을 가져오는 API
 * @param {number} festivalId - 축제 ID
 * @param {object} params - 페이지, 리밋, 정렬 등의 파라미터 ({ page, limit, sortBy })
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const getReviewsByFestival = (festivalId, params) => {
  return axiosInstance.get(`api/festivals/${festivalId}/reviews`, { params });
};

/**
 * 특정 축제에 새로운 리뷰를 작성하는 API
 * @param {number} festivalId - 축제 ID
 * @param {object} reviewData - 리뷰 데이터 ({ rating, comment })
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const postReview = (festivalId, reviewData) => {
  return axiosInstance.post(`api/festivals/${festivalId}/reviews`, reviewData);
};

/**
 * 기존 리뷰를 수정하는 API
 * @param {number} reviewId - 리뷰 ID
 * @param {object} updateData - 수정할 리뷰 데이터 ({ rating, comment })
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const updateReview = (reviewId, updateData) => {
  return axiosInstance.put(`api/reviews/${reviewId}`, updateData);
};

/**
 * 리뷰를 삭제하는 API
 * @param {number} reviewId - 삭제할 리뷰 ID
 * @returns {Promise<axios.AxiosResponse<any>>}
 */
export const deleteReview = (reviewId) => {
  return axiosInstance.delete(`api/reviews/${reviewId}`);
};