import styled, { css } from 'styled-components'; 
import { Link, useNavigate, useLocation } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { FiSearch, FiHeart } from 'react-icons/fi';

const Header = () => {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation(); 


  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <HeaderContainer>
      <HeaderInner>
        <LogoContainer to="/">
          <LogoIcon>🎡</LogoIcon>
          <LogoText>모아패밀리</LogoText>
        </LogoContainer>
        <Nav>
          {isAuthenticated ? (
            <>
              {/* 🔍 축제 찾기 */}
              <NavLink $isActive={location.pathname === '/'} to="/">
                <FiSearch className="nav-icon" />
                <span>축제 찾기</span>
              </NavLink>
              
              {/* 🧡 찜 목록 */}
              <NavLink $isActive={location.pathname === '/mypage'} to="/mypage">
                <FiHeart className="nav-icon" />
                <span>찜 목록</span>
                {/* <Badge>3</Badge> */}
              </NavLink>
              
              {/* 🚪 로그아웃 */}
              <NavLink as="button" onClick={handleLogout}>
                <span>로그아웃</span>
              </NavLink>
            </>
          ) : (
            // 🔓 로그인
            <NavLink $isActive={location.pathname === '/login'} to="/login">
              <span>로그인</span>
            </NavLink>
          )}
        </Nav>
      </HeaderInner>
    </HeaderContainer>
  );
};

export default Header;

// Styles
const HeaderContainer = styled.header`
  position: sticky;
  top: 0;
  z-index: 100;
  height: ${({ theme }) => theme.layout?.headerHeight || '64px'};
  background-color: ${({ theme }) => theme.colors?.background || '#ffffff'};
  border-bottom: 1px solid ${({ theme }) => theme.colors?.border || '#eaeaea'};
  box-shadow: ${({ theme }) => theme.shadows?.sm || '0 2px 4px rgba(0,0,0,0.05)'};
  width: 100%;
`;

const HeaderInner = styled.div`
  width: 100%;
  max-width: 100%;
  height: 100%;
  margin: 0 auto;
  padding: 0 40px; 
  display: flex;
  align-items: center;
  justify-content: space-between; 
`;

const LogoContainer = styled(Link)`
  display: flex;
  align-items: center;
  gap: ${({ theme }) => theme.spacing?.sm || '8px'};
  text-decoration: none;
`;

const LogoIcon = styled.span`
  font-size: 1.75rem;
  line-height: 1;
`;

const LogoText = styled.span`
  font-size: ${({ theme }) => theme.fontSizes?.xl || '1.25rem'};
  font-weight: 700;
  color: ${({ theme }) => theme.colors?.text || '#333333'};
  letter-spacing: -0.5px;
`;

const Nav = styled.nav`
  display: flex;
  align-items: center;
  gap: ${({ theme }) => theme.spacing?.sm || '8px'};
`;


const NavLink = styled(Link)`
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  text-decoration: none;
  padding: 10px 20px;
  border: none;
  border-radius: 100px;
  background-color: transparent;
  color: #666666;
  cursor: pointer;
  transition: all 0.25s ease-in-out;

  .nav-icon {
    font-size: 1.15rem;
    stroke: #666666;
    stroke-width: 2.2;
    transition: stroke 0.25s ease-in-out;
  }

  &:hover {
    color: #c05c36;
    background-color: #fdf0e9;
    
    .nav-icon {
      stroke: #c05c36;
    }
  }

  ${({ $isActive }) =>
    $isActive &&
    css`
      color: #c05c36;
      background-color: #fdf0e9;

      .nav-icon {
        stroke: #c05c36;
      }
    `}
`;

