const FAVORITES_STORE = new Map();

export async function fetchFavorites() {
  await delay(200);
  return Array.from(FAVORITES_STORE.values());
}

export async function saveFavorite(car) {
  await delay(150);
  FAVORITES_STORE.set(car.id, {
    id: car.id,
    name: car.name,
    trim: car.trim,
    price: car.price,
    bodyType: car.bodyType,
    fuelType: car.fuelType,
    image: car.image,
    savedAt: new Date().toISOString()
  });
  return FAVORITES_STORE.get(car.id);
}

export async function removeFavorite(carId) {
  await delay(150);
  FAVORITES_STORE.delete(carId);
  return { carId };
}

function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
