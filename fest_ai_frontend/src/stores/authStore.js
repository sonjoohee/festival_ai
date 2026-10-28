import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      loginId: null,
      userId: null,
      name: null,
      _hasHydrated: false,
      setHasHydrated: (value) => set({ _hasHydrated: value }),
      login: ({ token, loginId, userId, name }) =>
        set({ token, loginId, userId, name }),
      logout: () =>
        set({ token: null, loginId: null, userId: null, name: null }),
    }),
    {
      name: 'fest-auth',
      partialize: (state) => ({
        token: state.token,
        loginId: state.loginId,
        userId: state.userId,
        name: state.name,
      }),
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated(true);
      },
    },
  ),
);

export default useAuthStore;
