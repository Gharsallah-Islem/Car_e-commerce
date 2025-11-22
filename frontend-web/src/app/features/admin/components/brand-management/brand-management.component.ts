import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { BrandService, Brand } from '../../../../core/services/brand.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
    selector: 'app-brand-management',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatCardModule,
        MatDividerModule,
        MatDialogModule,
        MatTooltipModule
    ],
    templateUrl: './brand-management.component.html',
    styleUrl: './brand-management.component.scss'
})
export class BrandManagementComponent implements OnInit {
    // State
    brands = signal<Brand[]>([]);
    loading = signal<boolean>(false);
    showForm = signal<boolean>(false);
    editingBrand = signal<Brand | null>(null);

    // Form
    brandForm!: FormGroup;
    searchQuery = signal<string>('');

    // Table
    displayedColumns: string[] = ['id', 'name', 'country', 'description', 'productCount', 'actions'];

    // Pagination
    pageSize = signal<number>(10);
    pageIndex = signal<number>(0);
    totalBrands = signal<number>(0);

    constructor(
        private fb: FormBuilder,
        private brandService: BrandService,
        private notificationService: NotificationService,
        private dialog: MatDialog
    ) { }

    ngOnInit(): void {
        this.initForm();
        this.loadBrands();
    }

    initForm(): void {
        this.brandForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
            country: ['', [Validators.maxLength(100)]],
            logoUrl: ['', [Validators.pattern(/^https?:\/\/.+/)]],
            description: ['', [Validators.maxLength(500)]]
        });
    }

    loadBrands(): void {
        this.loading.set(true);
        this.brandService.getAllBrands().subscribe({
            next: (brands) => {
                this.brands.set(brands);
                this.totalBrands.set(brands.length);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading brands:', error);
                this.notificationService.error('Erreur lors du chargement des marques');
                this.loading.set(false);
            }
        });
    }

    openForm(brand?: Brand): void {
        if (brand) {
            this.editingBrand.set(brand);
            this.brandForm.patchValue({
                name: brand.name,
                country: brand.country,
                logoUrl: brand.logoUrl,
                description: brand.description
            });
        } else {
            this.editingBrand.set(null);
            this.brandForm.reset();
        }
        this.showForm.set(true);
    }

    closeForm(): void {
        this.showForm.set(false);
        this.editingBrand.set(null);
        this.brandForm.reset();
    }

    saveBrand(): void {
        if (this.brandForm.invalid) {
            this.notificationService.error('Veuillez remplir tous les champs requis');
            return;
        }

        const brandData: Brand = {
            ...this.brandForm.value
        };

        const editing = this.editingBrand();

        if (editing) {
            // Update existing brand
            this.brandService.updateBrand(editing.id!, brandData).subscribe({
                next: (updated) => {
                    this.brands.update(brands =>
                        brands.map(b => b.id === updated.id ? updated : b)
                    );
                    this.notificationService.success('Marque mise à jour avec succès');
                    this.closeForm();
                },
                error: (error) => {
                    console.error('Error updating brand:', error);
                    this.notificationService.error('Erreur lors de la mise à jour de la marque');
                }
            });
        } else {
            // Create new brand
            this.brandService.createBrand(brandData).subscribe({
                next: (created) => {
                    this.brands.update(brands => [...brands, created]);
                    this.totalBrands.update(count => count + 1);
                    this.notificationService.success('Marque créée avec succès');
                    this.closeForm();
                },
                error: (error) => {
                    console.error('Error creating brand:', error);
                    this.notificationService.error('Erreur lors de la création de la marque');
                }
            });
        }
    }

    deleteBrand(brand: Brand): void {
        if (!confirm(`Êtes-vous sûr de vouloir supprimer la marque "${brand.name}" ?`)) {
            return;
        }

        this.brandService.deleteBrand(brand.id!).subscribe({
            next: () => {
                this.brands.update(brands => brands.filter(b => b.id !== brand.id));
                this.totalBrands.update(count => count - 1);
                this.notificationService.success('Marque supprimée avec succès');
            },
            error: (error) => {
                console.error('Error deleting brand:', error);
                this.notificationService.error('Erreur lors de la suppression de la marque');
            }
        });
    }

    applySearch(): void {
        const query = this.searchQuery().toLowerCase();
        if (!query) {
            this.loadBrands();
            return;
        }

        const filtered = this.brands().filter(brand =>
            brand.name.toLowerCase().includes(query) ||
            brand.country?.toLowerCase().includes(query) ||
            brand.description?.toLowerCase().includes(query)
        );
        this.brands.set(filtered);
    }

    handlePageEvent(event: PageEvent): void {
        this.pageSize.set(event.pageSize);
        this.pageIndex.set(event.pageIndex);
    }

    getFilteredBrands(): Brand[] {
        const start = this.pageIndex() * this.pageSize();
        const end = start + this.pageSize();
        return this.brands().slice(start, end);
    }
}
