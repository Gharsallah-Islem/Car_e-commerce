import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { ProductsComponent } from './features/products/products.component';
import { CategoriesComponent } from './features/categories/categories.component';

@NgModule({
  declarations: [
    ProductsComponent,
    CategoriesComponent
  ],
  imports: [
    HttpClientModule
  ],
})
export class AppModule { }