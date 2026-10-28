export const reviewKeys = {
  all: ['reviews'],
  byFestival: (festivalId) => [...reviewKeys.all, String(festivalId)],
};
