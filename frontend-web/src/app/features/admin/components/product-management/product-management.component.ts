import { Component, OnInit, signal, model } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';

import { ProductService } from '../../../../core/services/product.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Product, ProductSpecification, VehicleCompatibility } from '../../../../core/models';
import { CategoryService, Category } from '../../../../core/services/category.service';
import { BrandService, Brand } from '../../../../core/services/brand.service';

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatDividerModule,
    MatSlideToggleModule,
    MatDialogModule,
    MatMenuModule,
    MatBadgeModule
  ],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss'
})
export class ProductManagementComponent implements OnInit {
  // Forms
  productForm!: FormGroup;
  searchForm!: FormGroup;

  // State
  loading = signal<boolean>(false);
  viewMode = signal<'grid' | 'list'>('grid');
  showForm = signal<boolean>(false);

  // Products
  products = signal<Product[]>([]);
  filteredProducts = signal<Product[]>([]);
  editingProduct = signal<Product | null>(null);
  productColumns: string[] = ['image', 'name', 'category', 'brand', 'price', 'stock', 'status', 'actions'];
  totalProducts = signal<number>(0);

  // Pagination & Sorting
  pageSize = signal<number>(12);
  pageIndex = signal<number>(0);
  sortField = signal<string>('');
  sortDirection = signal<'asc' | 'desc'>('asc');

  // Filters (using model for two-way binding)
  selectedCategory = model<string>('all');
  selectedBrand = model<string>('all');
  selectedStatus = model<string>('all');
  searchQuery = signal<string>('');

  // Categories and brands
  categories = signal<Category[]>([]);
  brands = signal<Brand[]>([]);

  statusOptions = [
    { value: 'all', label: 'Tous' },
    { value: 'in_stock', label: 'En stock' },
    { value: 'low_stock', label: 'Stock faible' },
    { value: 'out_of_stock', label: 'Rupture' }
  ];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private categoryService: CategoryService,
    private brandService: BrandService,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) {
    this.initForms();
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  initForms(): void {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      categoryId: ['', [Validators.required]],
      brandId: ['', [Validators.required]],
      price: [0, [Validators.required, Validators.min(0)]],
      discountPrice: [0, [Validators.min(0)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      imageUrl: ['', [Validators.required]],
      specifications: [''],
      compatibility: ['']
    });

    this.searchForm = this.fb.group({
      search: ['']
    });

    // Subscribe to search changes with debounce
    this.searchForm.get('search')?.valueChanges.subscribe(value => {
      this.searchQuery.set(value?.trim() || '');
      this.applyFilters();
    });
  }

  loadInitialData(): void {
    this.loading.set(true);
    // Load categories and brands first
    this.categoryService.getAllCategories().subscribe({
      next: (cats) => this.categories.set(cats),
      error: (err) => console.error('Error loading categories', err)
    });

    this.brandService.getAllBrands().subscribe({
      next: (brands) => this.brands.set(brands),
      error: (err) => console.error('Error loading brands', err)
    });

    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.productService.getProducts({
      page: this.pageIndex(),
      size: 100, // Load all for client-side filtering
      sort: this.sortField() ? (this.sortField() === 'price' ? (this.sortDirection() === 'asc' ? 'price_asc' : 'price_desc') : 'newest') : undefined
    }).subscribe({
      next: (response) => {
        this.products.set(response.content);
        this.totalProducts.set(response.totalElements);
        this.applyFilters();
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.notificationService.error('Erreur lors du chargement des produits');
        this.loading.set(false);
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.products()];

    // Search filter
    if (this.searchQuery()) {
      const query = this.searchQuery().toLowerCase();
      filtered = filtered.filter(p =>
        p.name.toLowerCase().includes(query) ||
        p.description.toLowerCase().includes(query) ||
        (p.category?.name?.toLowerCase().includes(query) || false) ||
        (p.brand?.name?.toLowerCase().includes(query) || false)
      );
    }

    // Category filter
    if (this.selectedCategory() !== 'all') {
      filtered = filtered.filter(p => p.category?.id?.toString() === this.selectedCategory());
    }

    // Brand filter
    if (this.selectedBrand() !== 'all') {
      filtered = filtered.filter(p => p.brand?.id?.toString() === this.selectedBrand());
    }

    // Status filter
    if (this.selectedStatus() !== 'all') {
      switch (this.selectedStatus()) {
        case 'in_stock':
          filtered = filtered.filter(p => p.stock >= 10);
          break;
        case 'low_stock':
          filtered = filtered.filter(p => p.stock > 0 && p.stock < 10);
          break;
        case 'out_of_stock':
          filtered = filtered.filter(p => p.stock === 0);
          break;
      }
    }

    this.filteredProducts.set(filtered);
  }

  toggleViewMode(): void {
    this.viewMode.set(this.viewMode() === 'grid' ? 'list' : 'grid');
  }

  openProductForm(product?: Product): void {
    this.showForm.set(true);
    if (product) {
      this.editingProduct.set(product);

      // Convert specifications array to JSON string
      const specsObj: { [key: string]: string } = {};
      product.specifications?.forEach(spec => {
        specsObj[spec.key] = spec.value;
      });

      // Compatibility is already a string from backend (compatibilityString)
      const compatStr = product.compatibilityString || '';

      // Calculate discount price from discount percentage
      const discountPrice = product.discount ? product.price * (1 - product.discount / 100) : 0;

      this.productForm.patchValue({
        name: product.name || '',
        description: product.description || '',
        categoryId: product.category?.id || null,
        brandId: product.brand?.id || null,
        price: product.price || 0,
        discountPrice: discountPrice || 0,
        stock: product.stock || 0,
        imageUrl: product.imageUrl || '',
        specifications: JSON.stringify(specsObj, null, 2),
        compatibility: compatStr
      });
    } else {
      this.editingProduct.set(null);
      this.productForm.reset();
    }
  }

  saveProduct(): void {
    if (this.productForm.invalid) {
      this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.loading.set(true);
    const formValue = this.productForm.value;

    // Parse specifications from JSON string to array
    let specifications: ProductSpecification[] = [];
    if (formValue.specifications) {
      try {
        const specObj = JSON.parse(formValue.specifications);
        specifications = Object.entries(specObj).map(([key, value]) => ({
          key,
          value: String(value)
        }));
      } catch (e) {
        this.notificationService.error('Format JSON des spécifications invalide');
        this.loading.set(false);
        return;
      }
    }

    // Parse compatibility from comma-separated string to array
    const compatibility: VehicleCompatibility[] = [];
    if (formValue.compatibility) {
      const compatList = formValue.compatibility.split(',').map((c: string) => c.trim());
      compatList.forEach((comp: string, index: number) => {
        const parts = comp.split(' ');
        compatibility.push({
          id: index + 1,
          brand: parts[0] || '',
          model: parts.slice(1).join(' ') || '',
          year: 2024
        });
      });
    }

    const productData: any = {
      name: formValue.name,
      description: formValue.description,
      categoryId: formValue.categoryId,
      brandId: formValue.brandId,
      price: formValue.price,
      stockQuantity: formValue.stock, // Backend expects stockQuantity
      imageUrl: formValue.imageUrl,
      vehicleCompatibility: compatibility.length > 0 ? { models: compatibility } : null
    };

    if (this.editingProduct()) {
      this.productService.updateProduct(this.editingProduct()!.id, productData).subscribe({
        next: (updatedProduct) => {
          this.products.update(products =>
            products.map(p => p.id === updatedProduct.id ? updatedProduct : p)
          );
          this.notificationService.success('Produit mis à jour avec succès');
          this.closeProductForm();
          this.applyFilters();
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error updating product:', error);
          this.notificationService.error('Erreur lors de la mise à jour du produit');
          this.loading.set(false);
        }
      });
    } else {
      this.productService.createProduct(productData).subscribe({
        next: (newProduct) => {
          this.products.update(products => [...products, newProduct]);
          this.notificationService.success('Produit ajouté avec succès');
          this.closeProductForm();
          this.applyFilters();
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error creating product:', error);
          this.notificationService.error('Erreur lors de la création du produit');
          this.loading.set(false);
        }
      });
    }
  }

  deleteProduct(productId: number | string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')) {
      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.products.update(products => products.filter(p => p.id !== productId));
          this.notificationService.success('Produit supprimé avec succès');
          this.applyFilters();
        },
        error: (error) => {
          console.error('Error deleting product:', error);
          this.notificationService.error('Erreur lors de la suppression du produit');
        }
      });
    }
  }

  closeProductForm(): void {
    this.showForm.set(false);
    this.editingProduct.set(null);
    this.productForm.reset();
  }

  handlePageEvent(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  handleSort(sort: Sort): void {
    this.sortField.set(sort.active);
    this.sortDirection.set(sort.direction as 'asc' | 'desc');
    this.loadProducts();
  }

  getStockStatus(stock: number): string {
    if (stock === 0) return 'Rupture';
    if (stock < 10) return 'Stock faible';
    return 'En stock';
  }

  getStockColor(stock: number): string {
    if (stock === 0) return 'warn';
    if (stock < 10) return 'accent';
    return 'primary';
  }

  formatPrice(price: number, discount?: number): string {
    const finalPrice = discount ? price * (1 - discount / 100) : price;
    return `${finalPrice.toFixed(2)} TND`;
  }

  clearFilters(): void {
    this.selectedCategory.set('all');
    this.selectedBrand.set('all');
    this.selectedStatus.set('all');
    this.searchForm.patchValue({ search: '' });
    this.searchQuery.set('');
    this.applyFilters();
  }
}
