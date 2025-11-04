import { Component, OnInit } from '@angular/core';
import { Product } from './product.model';
import { ProductService } from './product.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  loading = true;
  error = '';
  filterCat: string | null = null;

  constructor(private ps: ProductService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(q => (this.filterCat = q.get('cat')));
    this.ps.fetchProducts().subscribe({
      next: data => {
        this.products = this.filterCat ? data.filter(p => p.category?.toLowerCase() === this.filterCat) : data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur chargement produits';
        this.loading = false;
      }
    });
  }

  addToCart(p: Product) {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const existing = cart.find((c: any) => c.id === p.id);
    if (existing) existing.qty++;
    else cart.push({ id: p.id, name: p.name, price: p.price, qty: 1 });
    localStorage.setItem('cart', JSON.stringify(cart));
    alert(`${p.name} ajouté au panier (mock).`);
  }
}