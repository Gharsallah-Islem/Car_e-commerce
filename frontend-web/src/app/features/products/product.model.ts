export interface Product {
  id: number;
  sku?: string;
  name: string;
  price: number;
  description?: string;
  image?: string;
  stock?: number;
  category?: string;
  brand?: string;
  compatibility?: string[];
}