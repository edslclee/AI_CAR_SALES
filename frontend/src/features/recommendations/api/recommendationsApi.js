const MOCK_RECOMMENDATIONS = [
  {
    id: 'car-101',
    rank: 1,
    name: 'Hyundai Ioniq 5',
    trim: 'Long Range AWD',
    price: 5850,
    bodyType: 'SUV',
    fuelType: 'EV',
    efficiency: '5.2 km/kWh',
    score: 92,
    scoreBreakdown: {
      budget: 95,
      bodyType: 90,
      options: 88
    },
    image: 'https://images.unsplash.com/photo-1523987355523-c7b5b84b37d4?auto=format&w=600&q=80'
  },
  {
    id: 'car-102',
    rank: 2,
    name: 'Kia EV6',
    trim: 'GT-Line',
    price: 6330,
    bodyType: 'Crossover',
    fuelType: 'EV',
    efficiency: '4.8 km/kWh',
    score: 88,
    scoreBreakdown: {
      budget: 85,
      bodyType: 92,
      options: 87
    },
    image: 'https://images.unsplash.com/photo-1617813489386-3944c1b19c4b?auto=format&w=600&q=80'
  },
  {
    id: 'car-103',
    rank: 3,
    name: 'Genesis GV80',
    trim: '3.5T AWD',
    price: 7680,
    bodyType: 'SUV',
    fuelType: 'Gasoline',
    efficiency: '8.6 km/L',
    score: 81,
    scoreBreakdown: {
      budget: 70,
      bodyType: 94,
      options: 85
    },
    image: 'https://images.unsplash.com/photo-1617814072575-ef488f0ccc01?auto=format&w=600&q=80'
  },
  {
    id: 'car-104',
    rank: 4,
    name: 'BMW 320i',
    trim: 'M Sport',
    price: 6290,
    bodyType: 'Sedan',
    fuelType: 'Gasoline',
    efficiency: '12.4 km/L',
    score: 79,
    scoreBreakdown: {
      budget: 82,
      bodyType: 75,
      options: 80
    },
    image: 'https://images.unsplash.com/photo-1583121274602-3e282f08f3c1?auto=format&w=600&q=80'
  }
];

export async function fetchRecommendations(recommendationId) {
  await delay(400);
  return {
    recommendationId,
    generatedAt: new Date().toISOString(),
    rationale: '예산, 차종 선호, 옵션 적합도를 기준으로 생성된 추천 결과입니다.',
    items: MOCK_RECOMMENDATIONS
  };
}

export async function toggleFavorite(carId, shouldFavorite) {
  await delay(200);
  return {
    carId,
    favorited: shouldFavorite
  };
}

function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
