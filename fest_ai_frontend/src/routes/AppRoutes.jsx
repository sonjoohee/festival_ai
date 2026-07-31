import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage.jsx';
import MainPage from '../pages/MainPage.jsx';
import DetailPage from '../pages/DetailPage.jsx';
import MyPage from '../pages/MyPage.jsx';
import ProtectedRoute from './ProtectedRoute';
import SignUpPage from '../pages/SignUpPage.jsx';

const AppRoutes = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/signup" element={<SignUpPage />} />
    
    <Route element={<ProtectedRoute />}>
      <Route path="/" element={<MainPage />} />
      <Route path="/festivals/:festivalId" element={<DetailPage />} />
      <Route path="/mypage" element={<MyPage />} />
    </Route>

    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
);

export default AppRoutes;
