import { useState, useEffect } from 'react';
import styled from 'styled-components';
import Header from '../components/Header.jsx';
import { searchFestivals } from '../api/festivalsApi';
import FestivalSearchBar from '../features/festivals/FestivalSearchBar.jsx';
import FestivalGrid from '../features/festivals/FestivalGrid.jsx';



const MainPage = () => {
  const [festivals, setFestivals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  useEffect(() => {

    const cachedFestivals = sessionStorage.getItem('cachedFestivals');
    const cachedRegion = sessionStorage.getItem('cachedRegion');

    if (cachedFestivals && cachedRegion) {
      setFestivals(JSON.parse(cachedFestivals));
      setSearched(true);
    } else {
      handleSearch({ region: 'all' });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearch = async (params) => {
    // console.log(
    //   "SearchParams:", params
    // )
  setLoading(true);
  setSearched(true);
  try {
    const response = await searchFestivals(params);

    const festivalData = response.data || [];
    setFestivals(festivalData); 

    sessionStorage.setItem('cachedFestivals', JSON.stringify(festivalData));
    sessionStorage.setItem('cachedRegion', params.region);
    
  } catch (err) {
    console.error("Failed to fetch festivals:", err);
  } finally {
    setLoading(false); 
  }
};

  return (
    <>
      <Header />
      <HeroSection>
        <HeroContent>
          <HeroTitle>
            <span>Find Your Family's</span>
            <span className="highlight">Perfect Festival</span>
          </HeroTitle>
          
          <HeroSubtitle>
            우리 가족에게 딱 맞는 축제를 찾아보세요
          </HeroSubtitle>
          <FestivalSearchBar
            onSearch={handleSearch}
            initialRegion={sessionStorage.getItem('cachedRegion') || 'all'}
          />
        </HeroContent>
      </HeroSection>
      <MainContent>
        <FestivalGrid festivals={searched ? festivals : []} loading={loading} />
      </MainContent>
    </>
  );
};

export default MainPage;

// Styles (기존 코드와 동일하게 유지)
const HeroSection = styled.div`
  background: linear-gradient(
    105deg, 
    #322219 0%,  
    #2a2c1f 40%, 
    #224430 75%, 
    #25583b 100%   
  );
  padding: 55px 20px;
  text-align: center;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: -150px;
    right: -150px;
    width: 450px;
    height: 450px;
    background: radial-gradient(
      circle, 
      rgba(59, 133, 96, 0.5) 0%,
      rgba(59, 133, 96, 0.18) 50%,   
      transparent 75%                
    );
    pointer-events: none; 
  }
`;

const HeroContent = styled.div`
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const HeroTitle = styled.h1`
  font-size: 2.5rem;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column; 
  gap: 4px;
  
  color: #ffffff;

  .highlight {
    color: #dfb15b; 
  }
`;

const HeroSubtitle = styled.p`
  font-size: 1rem;
  color: #aaaaaa;
  max-width: 600px;
  line-height: 1.6;
  margin-bottom: 40px;
`;

const MainContent = styled.main`
  padding: ${({ theme }) => theme.spacing.xl};
  max-width: ${({ theme }) => theme.layout.maxWidth};
  margin: 0 auto;
`;