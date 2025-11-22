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

import { CategoryService, Category } from '../../../../core/services/category.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
    selector: 'app-category-management',
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
    templateUrl: './category-management.component.html',
    styleUrl: './category-management.component.scss'
})
export class CategoryManagementComponent implements OnInit {
    // State
    categories = signal<Category[]>([]);
    loading = signal<boolean>(false);
    showForm = signal<boolean>(false);
    editingCategory = signal<Category | null>(null);

    // Form
    categoryForm!: FormGroup;
    searchQuery = signal<string>('');

    // Table
    displayedColumns: string[] = ['id', 'name', 'description', 'productCount', 'actions'];

    // Pagination
    pageSize = signal<number>(10);
    pageIndex = signal<number>(0);
    totalCategories = signal<number>(0);

    constructor(
        private fb: FormBuilder,
        private categoryService: CategoryService,
        private notificationService: NotificationService,
        private dialog: MatDialog
    ) { }

    ngOnInit(): void {
        this.initForm();
        this.loadCategories();
    }

    initForm(): void {
        this.categoryForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
            description: ['', [Validators.maxLength(500)]]
        });
    }

    loadCategories(): void {
        this.loading.set(true);
        this.categoryService.getAllCategories().subscribe({
            next: (categories) => {
                this.categories.set(categories);
                this.totalCategories.set(categories.length);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading categories:', error);
                this.notificationService.error('Erreur lors du chargement des catégories');
                this.loading.set(false);
            }
        });
    }

    openForm(category?: Category): void {
        if (category) {
            this.editingCategory.set(category);
            this.categoryForm.patchValue({
                name: category.name,
                description: category.description
            });
        } else {
            this.editingCategory.set(null);
            this.categoryForm.reset();
        }
        this.showForm.set(true);
    }

    closeForm(): void {
        this.showForm.set(false);
        this.editingCategory.set(null);
        this.categoryForm.reset();
    }

    saveCategory(): void {
        if (this.categoryForm.invalid) {
            this.notificationService.error('Veuillez remplir tous les champs requis');
            return;
        }

        const categoryData: Category = {
            ...this.categoryForm.value
        };

        const editing = this.editingCategory();

        if (editing) {
            // Update existing category
            this.categoryService.updateCategory(editing.id!, categoryData).subscribe({
                next: (updated) => {
                    this.categories.update(cats =>
                        cats.map(c => c.id === updated.id ? updated : c)
                    );
                    this.notificationService.success('Catégorie mise à jour avec succès');
                    this.closeForm();
                },
                error: (error) => {
                    console.error('Error updating category:', error);
                    this.notificationService.error('Erreur lors de la mise à jour de la catégorie');
                }
            });
        } else {
            // Create new category
            this.categoryService.createCategory(categoryData).subscribe({
                next: (created) => {
                    this.categories.update(cats => [...cats, created]);
                    this.totalCategories.update(count => count + 1);
                    this.notificationService.success('Catégorie créée avec succès');
                    this.closeForm();
                },
                error: (error) => {
                    console.error('Error creating category:', error);
                    this.notificationService.error('Erreur lors de la création de la catégorie');
                }
            });
        }
    }

    deleteCategory(category: Category): void {
        if (!confirm(`Êtes-vous sûr de vouloir supprimer la catégorie "${category.name}" ?`)) {
            return;
        }

        this.categoryService.deleteCategory(category.id!).subscribe({
            next: () => {
                this.categories.update(cats => cats.filter(c => c.id !== category.id));
                this.totalCategories.update(count => count - 1);
                this.notificationService.success('Catégorie supprimée avec succès');
            },
            error: (error) => {
                console.error('Error deleting category:', error);
                this.notificationService.error('Erreur lors de la suppression de la catégorie');
            }
        });
    }

    applySearch(): void {
        const query = this.searchQuery().toLowerCase();
        if (!query) {
            this.loadCategories();
            return;
        }

        const filtered = this.categories().filter(cat =>
            cat.name.toLowerCase().includes(query) ||
            cat.description?.toLowerCase().includes(query)
        );
        this.categories.set(filtered);
    }

    handlePageEvent(event: PageEvent): void {
        this.pageSize.set(event.pageSize);
        this.pageIndex.set(event.pageIndex);
    }

    getFilteredCategories(): Category[] {
        const start = this.pageIndex() * this.pageSize();
        const end = start + this.pageSize();
        return this.categories().slice(start, end);
    }
}
