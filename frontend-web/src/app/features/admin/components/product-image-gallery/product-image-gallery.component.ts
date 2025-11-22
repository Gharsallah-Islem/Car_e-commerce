import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

import { ProductImageService, ProductImage } from '../../../../core/services/product-image.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
    selector: 'app-product-image-gallery',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatTooltipModule,
        DragDropModule
    ],
    templateUrl: './product-image-gallery.component.html',
    styleUrl: './product-image-gallery.component.scss'
})
export class ProductImageGalleryComponent implements OnInit {
    images = signal<ProductImage[]>([]);
    loading = signal<boolean>(false);
    showAddForm = signal<boolean>(false);
    imageForm!: FormGroup;

    constructor(
        @Inject(MAT_DIALOG_DATA) public data: { productId: string; productName: string },
        private dialogRef: MatDialogRef<ProductImageGalleryComponent>,
        private fb: FormBuilder,
        private productImageService: ProductImageService,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void {
        this.initForm();
        this.loadImages();
    }

    initForm(): void {
        this.imageForm = this.fb.group({
            imageUrl: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]]
        });
    }

    loadImages(): void {
        this.loading.set(true);
        this.productImageService.getProductImages(this.data.productId).subscribe({
            next: (images) => {
                this.images.set(images);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading images:', error);
                this.notificationService.error('Erreur lors du chargement des images');
                this.loading.set(false);
            }
        });
    }

    addImage(): void {
        if (this.imageForm.invalid) {
            this.notificationService.error('Veuillez entrer une URL valide');
            return;
        }

        const imageUrl = this.imageForm.value.imageUrl;
        const isPrimary = this.images().length === 0; // First image is primary

        this.productImageService.addProductImage(this.data.productId, imageUrl, isPrimary).subscribe({
            next: (newImage) => {
                this.images.update(imgs => [...imgs, newImage]);
                this.notificationService.success('Image ajoutée avec succès');
                this.imageForm.reset();
                this.showAddForm.set(false);
            },
            error: (error) => {
                console.error('Error adding image:', error);
                this.notificationService.error('Erreur lors de l\'ajout de l\'image');
            }
        });
    }

    deleteImage(imageId: string): void {
        if (!confirm('Êtes-vous sûr de vouloir supprimer cette image ?')) {
            return;
        }

        this.productImageService.deleteProductImage(this.data.productId, imageId).subscribe({
            next: () => {
                this.images.update(imgs => imgs.filter(img => img.id !== imageId));
                this.notificationService.success('Image supprimée avec succès');
            },
            error: (error) => {
                console.error('Error deleting image:', error);
                this.notificationService.error('Erreur lors de la suppression de l\'image');
            }
        });
    }

    setPrimary(imageId: string): void {
        this.productImageService.setPrimaryImage(this.data.productId, imageId).subscribe({
            next: (updatedImage) => {
                this.images.update(imgs =>
                    imgs.map(img => ({
                        ...img,
                        isPrimary: img.id === imageId
                    }))
                );
                this.notificationService.success('Image principale définie');
            },
            error: (error) => {
                console.error('Error setting primary image:', error);
                this.notificationService.error('Erreur lors de la définition de l\'image principale');
            }
        });
    }

    onDrop(event: CdkDragDrop<ProductImage[]>): void {
        const imagesCopy = [...this.images()];
        moveItemInArray(imagesCopy, event.previousIndex, event.currentIndex);

        // Update display order
        const imageOrders = imagesCopy.map((img, index) => ({
            id: img.id!,
            displayOrder: index
        }));

        this.productImageService.reorderImages(this.data.productId, imageOrders).subscribe({
            next: (reorderedImages) => {
                this.images.set(reorderedImages);
                this.notificationService.success('Images réorganisées');
            },
            error: (error) => {
                console.error('Error reordering images:', error);
                this.notificationService.error('Erreur lors de la réorganisation');
                // Revert on error
                this.loadImages();
            }
        });
    }

    close(): void {
        this.dialogRef.close(this.images());
    }
}
