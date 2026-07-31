import axiosInstance from './axiosInstance';

export const searchFestivals = (params) =>
  axiosInstance.get('/api/festivals', { 
    params, // { region: '...' } 객체를 그대로 전달
  });

export const getFestivalDetail = (festivalId) =>
  axiosInstance.get('/api/festivals/detail', {
    params: { contentId: festivalId },
  });
