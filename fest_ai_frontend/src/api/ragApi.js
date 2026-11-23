import axiosInstance from './axiosInstance';

// contentId를 넣으면 그 축제 정보와 FAQ 안에서만 찾아 답한다
export const askRag = ({ question, contentId }) =>
  axiosInstance.post('/api/rag/ask', { question, contentId });
