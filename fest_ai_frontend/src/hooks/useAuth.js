import { useState, useEffect, useCallback } from 'react';

const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);


  useEffect(() => {
      const checkAuth = () => {
        const token = localStorage.getItem('token');
        if (token) {
          setIsAuthenticated(true);
        } else {
          setIsAuthenticated(false);
        }
        setIsLoading(false);
      };

      checkAuth();
    }, []); 


  const login = useCallback((token) => {
    localStorage.setItem('token', token);
    setIsAuthenticated(true);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
  }, []);

  return { isAuthenticated, isLoading, login, logout };
};

export default useAuth;
