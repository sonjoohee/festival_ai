import axiosInstance from './axiosInstance';

export const login = async (credentials) => {
  const response = await axiosInstance.post('/api/auth/login', credentials);
  return response.data;
};

export const signup = async (userInfo) => {
  const response = await axiosInstance.post('/api/auth/signup', userInfo); // Expects { loginId, password, name }
  return response.data;
};


export const logout = async () => {
  const response = await axiosInstance.post('/api/auth/logout'); // Expects empty body
  return response.data;
};