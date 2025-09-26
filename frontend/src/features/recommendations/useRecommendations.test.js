import { describe, expect, it } from 'vitest';
import { MAX_COMPARE } from './useRecommendations.js';

describe('useRecommendations constants', () => {
  it('should limit comparison selection to four items', () => {
    expect(MAX_COMPARE).toBe(4);
  });
});
