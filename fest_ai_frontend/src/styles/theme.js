const theme = {
  colors: {
    primary: '#D96B3A', // Terracotta Orange (따뜻함, 축제의 에너지)
    secondary: '#2C6B4F', // Forest Green (자연, 신뢰성)
    accent: '#F4C142', // Golden Yellow (화려함, 강조)
    background: '#FDF8F3', // Warm Cream (따뜻한 베이지 베이스)
    surface: '#FFFFFF',
    text: '#2C3E50',
    textLight: '#7F8C8D',
    border: '#EAE0D5', // 부드러운 베이지/그레이 톤
    error: '#E74C3C', // 기존 유지
    danger: '#E57373', // 테라코타 계열의 핑크/레드
    dangerDark: '#D32F2F',
    success: '#A8E6CF', // 포레스트 그린 계열의 연두/민트
    successDark: '#2C6B4F',
    white: '#FFFFFF',
    black: '#000000',
  },
  fonts: {
    heading: "'DM Serif Display', serif",
    body: "'Plus Jakarta Sans', 'Noto Sans KR', sans-serif",
  },
  fontSizes: {
    xs: '0.75rem',
    sm: '0.875rem',
    md: '1rem',
    lg: '1.125rem',
    xl: '1.25rem',
    xxl: '1.5rem',
    xxxl: '2rem',
    display: '2.5rem',
    logo: '1.75rem',
  },
  spacing: {
    xs: '0.25rem',
    sm: '0.5rem',
    md: '1rem',
    lg: '1.5rem',
    xl: '2.5rem',
    xxl: '4rem',
  },
  borderRadius: {
    sm: '4px',
    md: '8px',
    lg: '12px',
    xl: '16px',
    pill: '9999px',
    round: '50%',
  },
  shadows: {
    sm: '0 1px 3px rgba(0, 0, 0, 0.08)',
    md: '0 4px 12px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 25px rgba(0, 0, 0, 0.1)',
  },
  layout: {
    maxWidth: '1280px',
    headerHeight: '64px',
    gridColumns: 4,
  },
  breakpoints: {
    mobile: '480px',
    tablet: '768px',
    desktop: '1024px',
  },
};

export default theme;
