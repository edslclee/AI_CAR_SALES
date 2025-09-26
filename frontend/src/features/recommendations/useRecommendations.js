import { useCallback, useEffect, useMemo, useState } from 'react';
import { fetchRecommendations, toggleFavorite } from './api/recommendationsApi.js';
import { removeFavorite, saveFavorite, fetchFavorites } from '../favorites/favoritesApi.js';

const MAX_COMPARE = 4;

export function useRecommendations(recommendationId = 'demo-001') {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [compareList, setCompareList] = useState([]);
  const [favorites, setFavorites] = useState(new Set());

  useEffect(() => {
    let isMounted = true;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const [recommendation, favoriteItems] = await Promise.all([
          fetchRecommendations(recommendationId),
          fetchFavorites()
        ]);
        if (!isMounted) return;
        setItems(recommendation.items);
        setFavorites(new Set(favoriteItems.map((fav) => fav.id)));
      } catch (err) {
        if (isMounted) {
          setError('추천 정보를 불러오지 못했습니다.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }
    load();
    return () => {
      isMounted = false;
    };
  }, [recommendationId]);

  const toggleCompare = useCallback((carId) => {
    setCompareList((prev) => {
      const exists = prev.includes(carId);
      if (exists) {
        return prev.filter((id) => id !== carId);
      }
      if (prev.length >= MAX_COMPARE) {
        const next = [...prev];
        next.shift();
        next.push(carId);
        return next;
      }
      return [...prev, carId];
    });
  }, []);

  const toggleFavoriteCar = useCallback(async (car) => {
    const shouldFavorite = !favorites.has(car.id);
    try {
      await toggleFavorite(car.id, shouldFavorite);
      if (shouldFavorite) {
        await saveFavorite(car);
      } else {
        await removeFavorite(car.id);
      }
      setFavorites((prev) => {
        const next = new Set(prev);
        if (shouldFavorite) {
          next.add(car.id);
        } else {
          next.delete(car.id);
        }
        return next;
      });
    } catch (err) {
      setError('즐겨찾기 처리 중 문제가 발생했습니다.');
    }
  }, [favorites]);

  const compareItems = useMemo(() => items.filter((item) => compareList.includes(item.id)), [items, compareList]);

  return {
    items,
    compareList,
    compareItems,
    favorites,
    loading,
    error,
    toggleCompare,
    toggleFavorite: toggleFavoriteCar
  };
}

export { MAX_COMPARE };
