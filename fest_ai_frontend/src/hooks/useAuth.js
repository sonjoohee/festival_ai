import useAuthStore from '../stores/authStore';

const useAuth = () => {
  const token = useAuthStore((state) => state.token);
  const loginId = useAuthStore((state) => state.loginId);
  const userId = useAuthStore((state) => state.userId);
  const name = useAuthStore((state) => state.name);
  const hasHydrated = useAuthStore((state) => state._hasHydrated);
  const login = useAuthStore((state) => state.login);
  const logout = useAuthStore((state) => state.logout);

  return {
    isAuthenticated: Boolean(token),
    isLoading: !hasHydrated,
    loginId,
    userId,
    name,
    login,
    logout,
  };
};

export default useAuth;
