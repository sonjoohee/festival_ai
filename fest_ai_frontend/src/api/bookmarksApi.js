import axiosInstance from './axiosInstance';

export const getBookmarks = () =>
  axiosInstance.get('/api/bookmark');

export const createBookmark = (data) =>
  axiosInstance.post('/api/bookmark', data);

export const updateBookmark = (bookmarkId, data) =>
  axiosInstance.put(`/api/bookmark/${bookmarkId}`, data);

export const deleteBookmark = (bookmarkId) =>
  axiosInstance.delete(`/api/bookmark/${bookmarkId}`);
